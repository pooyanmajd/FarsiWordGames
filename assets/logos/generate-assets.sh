#!/bin/bash

# 🏛️ Persepolis Wordle Asset Generator
# Converts SVG logos to PNG for iOS and Android

echo "🏛️ Generating Persepolis Wordle logo assets..."

# Create output directories
mkdir -p ios-assets/AppIcon.appiconset
mkdir -p android-assets/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}
mkdir -p web-assets
mkdir -p marketing-assets

# Generate iOS App Icons (using app-icon.svg)
echo "📱 Generating iOS app icons..."
rsvg-convert -w 60 -h 60 app-icon.svg > ios-assets/AppIcon.appiconset/icon-60.png
rsvg-convert -w 120 -h 120 app-icon.svg > ios-assets/AppIcon.appiconset/icon-60@2x.png
rsvg-convert -w 180 -h 180 app-icon.svg > ios-assets/AppIcon.appiconset/icon-60@3x.png
rsvg-convert -w 76 -h 76 app-icon.svg > ios-assets/AppIcon.appiconset/icon-76.png
rsvg-convert -w 152 -h 152 app-icon.svg > ios-assets/AppIcon.appiconset/icon-76@2x.png
rsvg-convert -w 167 -h 167 app-icon.svg > ios-assets/AppIcon.appiconset/icon-83.5@2x.png
rsvg-convert -w 1024 -h 1024 app-icon.svg > ios-assets/AppIcon.appiconset/icon-1024.png

# Generate Android Icons (using app-icon.svg)
echo "🤖 Generating Android app icons..."
rsvg-convert -w 48 -h 48 app-icon.svg > android-assets/mipmap-mdpi/ic_launcher.png
rsvg-convert -w 72 -h 72 app-icon.svg > android-assets/mipmap-hdpi/ic_launcher.png
rsvg-convert -w 96 -h 96 app-icon.svg > android-assets/mipmap-xhdpi/ic_launcher.png
rsvg-convert -w 144 -h 144 app-icon.svg > android-assets/mipmap-xxhdpi/ic_launcher.png
rsvg-convert -w 192 -h 192 app-icon.svg > android-assets/mipmap-xxxhdpi/ic_launcher.png
rsvg-convert -w 512 -h 512 app-icon.svg > android-assets/ic_launcher_playstore.png

# Generate Logo Variations
echo "🎨 Generating logo variations..."
rsvg-convert -w 800 -h 200 logo-horizontal.svg > web-assets/logo-horizontal.png
rsvg-convert -w 800 -h 200 logo-light-theme.svg > web-assets/logo-light-theme.png
rsvg-convert -w 400 -h 120 logo-compact.svg > web-assets/logo-compact.png

# Generate Marketing Assets
echo "📢 Generating marketing assets..."
rsvg-convert -w 1200 -h 630 logo-horizontal.svg > marketing-assets/social-media-banner.png
rsvg-convert -w 2048 -h 1536 logo-horizontal.svg > marketing-assets/app-store-screenshot-header.png

echo "✅ Asset generation complete!"
echo ""
echo "📁 Generated files:"
echo "   📱 iOS: ios-assets/AppIcon.appiconset/"
echo "   🤖 Android: android-assets/"
echo "   🌐 Web: web-assets/"
echo "   📢 Marketing: marketing-assets/"
echo ""
echo "📋 Next steps:"
echo "   1. Copy iOS assets to composeApp/src/iosMain/resources/"
echo "   2. Copy Android assets to composeApp/src/androidMain/res/"
echo "   3. Update app icon references in project files"
echo ""
echo "🏛️ Persepolis Wordle branding ready!" 