import SwiftUI
import Shared

struct ContentView: View {
    @State private var inputText = ""
    @State private var statusMessage = "Initializing..."
    @State private var isInitialized = false
    @State private var lastResult = ""
    @State private var isLoading = false
    
    // Resolve shared ViewModel from Koin (injected)
    private let viewModel = KoinHelperKt.getWordVerificationViewModel()
    // AuthViewModel will be injected inside LoginView via KoinSwiftUI
    
    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                // Simple gate: show login until authenticated
                // Note: Binding to Kotlin StateFlow requires bridging; using actions only here
                LoginView()
                // Header
                VStack {
                    Text("🇮🇷 Persian Word Checker")
                        .font(.title2)
                        .fontWeight(.bold)
                    Text("Bloom Filter Test")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(Color.blue.opacity(0.1))
                .cornerRadius(10)
                
                // Status
                VStack(alignment: .leading) {
                    if isLoading {
                        HStack {
                            ProgressView()
                                .scaleEffect(0.8)
                            Text("Processing...")
                        }
                    }
                    Text(statusMessage)
                        .font(.body)
                        .padding()
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(isInitialized ? Color.green.opacity(0.1) : Color.orange.opacity(0.1))
                .cornerRadius(10)
                
                // Input Section
                if isInitialized {
                    VStack {
                        TextField("Enter Persian word (5 letters)", text: $inputText)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                        
                        HStack {
                            Button("Verify Word") {
                                verifyWord()
                            }
                            .disabled(inputText.isEmpty || isLoading)
                            .buttonStyle(.borderedProminent)
                            
                            Button("Test Common") {
                                testCommonWords()
                            }
                            .disabled(isLoading)
                            .buttonStyle(.bordered)
                        }
                    }
                    .padding()
                    .background(Color.gray.opacity(0.05))
                    .cornerRadius(10)
                }
                
                // Results
                if !lastResult.isEmpty {
                    VStack(alignment: .leading) {
                        Text("Latest Result")
                            .font(.headline)
                        Text(lastResult)
                            .font(.body)
                            .padding(.top, 4)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                    .background(Color.blue.opacity(0.05))
                    .cornerRadius(10)
                }
                
                Spacer()
            }
            .padding()
            .navigationTitle("Word Checker")
        }
        .onAppear {
            initializeWordChecker()
        }
    }
    
    private func initializeWordChecker() {
        isLoading = true
        statusMessage = "Initializing word checker..."
        
        Task {
            do {
                let success = try await WordChecker.shared.initialize()
                
                DispatchQueue.main.async {
                    self.isLoading = false
                    if success as! Bool {
                        self.isInitialized = true
                        self.statusMessage = "✅ Word checker initialized successfully!\n🔍 Bloom filter loaded (116KB)\n📝 Ready to verify Persian words"
                    } else {
                        self.statusMessage = "❌ Failed to initialize word checker\nCheck if bloom.bin is in bundle"
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.statusMessage = "❌ Error initializing: \(error.localizedDescription)"
                }
            }
        }
    }
    
    private func verifyWord() {
        guard !inputText.isEmpty else { return }
        
        isLoading = true
        let word = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        
        Task {
            let isValid = WordChecker.shared.isWordPossiblyValid(word: word)
                
            DispatchQueue.main.async {
                self.isLoading = false
                let status = isValid ? "✅ Valid" : "❌ Invalid"
                let confidence = isValid ? "High (Bloom filter passed)" : "Certain (Not in word set)"
                let details = word.count != 5 ? "Must be exactly 5 letters" : 
                            isValid ? "Word might be valid (0.1% chance of false positive)" : 
                            "Word not found in dictionary"
                
                self.lastResult = """
                Word: \(word)
                Status: \(status)
                Confidence: \(confidence)
                Details: \(details)
                """
            }
        }
    }
    
    private func testCommonWords() {
        isLoading = true
        statusMessage = "🧪 Testing common words..."
        
        let testWords = ["داشتن", "ساختن", "گرفتن", "یافتن", "اوردن", "hello", "12345"]
        
        Task {
            var results: [String] = []
            var validCount = 0
            
            for word in testWords {
                let isValid = WordChecker.shared.isWordPossiblyValid(word: word)
                let status = isValid ? "✅" : "❌"
                results.append("\(word): \(status)")
                if isValid { validCount += 1 }
            }
            
            DispatchQueue.main.async {
                self.isLoading = false
                self.statusMessage = "🧪 Test completed: \(validCount)/\(testWords.count) words passed"
                self.lastResult = results.joined(separator: "\n")
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
