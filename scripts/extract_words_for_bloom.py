#!/usr/bin/env python3
"""
Extract words from rich Persian word JSON for Bloom filter generation
Reads from persian_packs_v2.json and outputs clean word list for make_bloom.py
"""

import json
import sys
from pathlib import Path

def extract_words_for_bloom(input_file: str, output_file: str = "words_for_bloom.txt"):
    """Extract just the words from rich JSON format for Bloom filter"""
    
    try:
        # Read the rich JSON data
        with open(input_file, 'r', encoding='utf-8') as f:
            words_data = json.load(f)
        
        print(f"📚 Loaded {len(words_data)} words from {input_file}")
        
        # Extract unique words (in case there are duplicates)
        unique_words = set()
        pack_counts = {}
        difficulty_counts = {"easy": 0, "medium": 0, "hard": 0}
        
        for entry in words_data:
            word = entry.get("word", "").strip()
            difficulty = entry.get("difficulty", "unknown")
            pack = entry.get("pack", "unknown")
            
            if word and len(word) == 5:  # Ensure 5-letter words only
                unique_words.add(word)
                
                # Count by difficulty
                if difficulty in difficulty_counts:
                    difficulty_counts[difficulty] += 1
                
                # Count by pack
                pack_counts[pack] = pack_counts.get(pack, 0) + 1
        
        # Sort words for consistent output
        sorted_words = sorted(unique_words)
        
        # Write words to output file (one per line)
        with open(output_file, 'w', encoding='utf-8') as f:
            for word in sorted_words:
                f.write(f"{word}\n")
        
        # Print statistics
        print(f"✅ Extracted {len(sorted_words)} unique 5-letter words")
        print(f"📁 Output written to: {output_file}")
        print(f"📊 Difficulty distribution:")
        for diff, count in difficulty_counts.items():
            percentage = (count / len(words_data)) * 100
            print(f"   {diff.capitalize()}: {count:,} ({percentage:.1f}%)")
        
        print(f"📦 Found {len(pack_counts)} packs")
        print(f"📏 File size: {Path(output_file).stat().st_size / 1024:.1f} KB")
        
        # Validate for Bloom filter requirements
        if len(sorted_words) > 70000:
            print("⚠️  Warning: More than 70k words - consider adjusting Bloom filter size")
        elif len(sorted_words) < 60000:
            print("ℹ️  Note: Fewer than 60k words - Bloom filter might be oversized")
        else:
            print("✅ Word count is optimal for current Bloom filter settings")
        
        return sorted_words
        
    except FileNotFoundError:
        print(f"❌ Error: File {input_file} not found")
        return None
    except json.JSONDecodeError as e:
        print(f"❌ Error: Invalid JSON in {input_file}: {e}")
        return None
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return None

def create_bloom_filter_script(words_list: list, output_dir: str = "."):
    """Create the make_bloom.py script with the extracted words"""
    
    script_content = f'''#!/usr/bin/env python3
"""
Generate Bloom filter for {len(words_list)} Persian 5-letter words
Usage: python make_bloom.py
Outputs: bloom.bin (~120KB) and key.hex (32 chars)
"""

import hashlib
import secrets
import struct
import math
from pathlib import Path

# Bloom filter parameters
EXPECTED_ELEMENTS = {len(words_list)}
FALSE_POSITIVE_RATE = 0.001  # 0.1%
HASH_FUNCTIONS = 10

def calculate_bloom_size():
    """Calculate optimal bloom filter size"""
    # m = -n * ln(p) / (ln(2)^2)
    ln2_squared = (math.log(2) ** 2)
    optimal_bits = int(-EXPECTED_ELEMENTS * math.log(FALSE_POSITIVE_RATE) / ln2_squared)
    return optimal_bits

def siphash_24(data: bytes, key: bytes, seed: int = 0) -> int:
    """SipHash-2-4 implementation for consistent hashing"""
    # Simplified implementation - use a proper library in production
    import hmac
    combined = data + seed.to_bytes(4, 'little')
    hash_obj = hmac.new(key, combined, hashlib.sha256)
    return int.from_bytes(hash_obj.digest()[:8], 'little', signed=False)

def create_bloom_filter():
    """Create the bloom filter with Persian words"""
    
    # Calculate optimal size
    bloom_size_bits = calculate_bloom_size()
    bloom_size_bytes = (bloom_size_bits + 7) // 8
    
    print(f"🔧 Creating Bloom filter...")
    print(f"   Words: {{EXPECTED_ELEMENTS:,}}")
    print(f"   False positive rate: {{FALSE_POSITIVE_RATE * 100:.3f}}%")
    print(f"   Size: {{bloom_size_bits:,}} bits ({{bloom_size_bytes / 1024:.1f}} KB)")
    print(f"   Hash functions: {{HASH_FUNCTIONS}}")
    
    # Generate random 128-bit key for SipHash
    sip_key = secrets.token_bytes(16)
    
    # Initialize bloom filter (all zeros)
    bloom_filter = bytearray(bloom_size_bytes)
    
    # Words to add to the filter
    words = {repr(words_list)}
    
    # Add each word to the bloom filter
    for i, word in enumerate(words):
        if i % 5000 == 0:
            print(f"   Processing word {{i + 1:,}}/{{len(words):,}}...")
        
        word_bytes = word.encode('utf-8')
        
        # Generate hash functions using SipHash with different seeds
        for hash_func in range(HASH_FUNCTIONS):
            hash_value = siphash_24(word_bytes, sip_key, hash_func)
            bit_position = hash_value % bloom_size_bits
            
            # Set the bit
            byte_index = bit_position // 8
            bit_offset = bit_position % 8
            bloom_filter[byte_index] |= (1 << bit_offset)
    
    # Write bloom filter to file
    with open('bloom.bin', 'wb') as f:
        f.write(bloom_filter)
    
    # Write key to hex file  
    with open('key.hex', 'w') as f:
        f.write(sip_key.hex().upper())
    
    print(f"✅ Bloom filter created successfully!")
    print(f"   bloom.bin: {{Path('bloom.bin').stat().st_size / 1024:.1f}} KB")
    print(f"   key.hex: {{sip_key.hex().upper()}}")
    print(f"")
    print(f"🔐 Add this key to your WordChecker.kt:")
    print(f'   private const val K_HEX = "{{sip_key.hex().upper()}}"')

if __name__ == "__main__":
    create_bloom_filter()
'''
    
    script_path = Path(output_dir) / "make_bloom.py"
    with open(script_path, 'w', encoding='utf-8') as f:
        f.write(script_content)
    
    # Make executable
    script_path.chmod(0o755)
    
    print(f"📝 Created make_bloom.py script at: {script_path}")
    return script_path

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python extract_words_for_bloom.py <input_json_file> [output_words_file]")
        print("Example: python extract_words_for_bloom.py /Users/pooyan/Desktop/persian_packs_v2.json")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2] if len(sys.argv) > 2 else "words_for_bloom.txt"
    
    words = extract_words_for_bloom(input_file, output_file)
    
    if words:
        # Also create the bloom filter generation script
        create_bloom_filter_script(words)
        print(f"")
        print(f"🚀 Next steps:")
        print(f"   1. Run: python make_bloom.py")
        print(f"   2. Copy bloom.bin to shared/src/commonMain/resources/")
        print(f"   3. Update K_HEX in WordChecker.kt with the generated key") 