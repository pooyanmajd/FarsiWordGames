# 🏛️ Implementation Guide: Persepolis Wordle Logos

## Quick Start

### 1. Generate PNG Assets
```bash
cd assets/logos
chmod +x generate-assets.sh
./generate-assets.sh
```

### 2. iOS Implementation

#### Copy App Icons
```bash
# Copy generated iOS assets
cp ios-assets/AppIcon.appiconset/* composeApp/src/iosMain/resources/
```

#### Update Info.plist
```xml
<key>CFBundleIcons</key>
<dict>
    <key>CFBundlePrimaryIcon</key>
    <dict>
        <key>CFBundleIconFiles</key>
        <array>
            <string>icon-60</string>
            <string>icon-76</string>
            <string>icon-83.5</string>
        </array>
    </dict>
</dict>
```

#### Launch Screen (SwiftUI)
```swift
// In ContentView.swift or LaunchScreen.swift
Image("logo-horizontal")
    .resizable()
    .aspectRatio(contentMode: .fit)
    .frame(maxWidth: 300)
```

### 3. Android Implementation

#### Copy App Icons
```bash
# Copy generated Android assets
cp -r android-assets/* composeApp/src/androidMain/res/
```

#### Update AndroidManifest.xml
```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher"
    android:label="@string/app_name">
```

#### Splash Screen (Compose)
```kotlin
// In your Compose app
Image(
    painter = painterResource(R.drawable.logo_horizontal),
    contentDescription = "Persepolis Wordle",
    modifier = Modifier.fillMaxWidth(0.8f)
)
```

## Compose Multiplatform Usage

### Loading SVG Logos
```kotlin
// In commonMain
@Composable
expect fun PersepolisLogo(
    modifier: Modifier = Modifier,
    contentDescription: String = "Persepolis Wordle"
)

// In androidMain
@Composable
actual fun PersepolisLogo(
    modifier: Modifier,
    contentDescription: String
) {
    Image(
        painter = painterResource(R.drawable.logo_horizontal),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

// In iosMain
@Composable
actual fun PersepolisLogo(
    modifier: Modifier,
    contentDescription: String
) {
    // Load from iOS bundle
    AsyncImage(
        model = "logo-horizontal.png",
        contentDescription = contentDescription,
        modifier = modifier
    )
}
```

### Theme-Aware Logo
```kotlin
@Composable
fun ThemedPersepolisLogo(
    modifier: Modifier = Modifier
) {
    val logoResource = if (isSystemInDarkTheme()) {
        "logo-horizontal" // Dark theme version
    } else {
        "logo-light-theme" // Light theme version
    }
    
    Image(
        painter = painterResource(logoResource),
        contentDescription = "Persepolis Wordle",
        modifier = modifier
    )
}
```

## File Structure
```
assets/logos/
├── 📁 Source Files
│   ├── app-icon.svg (1024×1024)
│   ├── logo-horizontal.svg (800×200)
│   ├── logo-light-theme.svg (800×200)
│   └── logo-compact.svg (400×120)
├── 📁 Generated Assets
│   ├── ios-assets/
│   ├── android-assets/
│   ├── web-assets/
│   └── marketing-assets/
└── 📁 Documentation
    ├── brand-guidelines.md
    ├── implementation-guide.md
    └── generate-assets.sh
```

## App Store Assets Checklist

### iOS App Store
- [ ] 1024×1024 app icon (icon-1024.png)
- [ ] All required icon sizes generated
- [ ] Screenshots with logo header
- [ ] App Store Connect assets

### Google Play Store
- [ ] 512×512 app icon (ic_launcher_playstore.png)
- [ ] Feature graphic with logo
- [ ] All density assets (mdpi to xxxhdpi)
- [ ] Adaptive icon support

## Marketing Materials

### Social Media
- **Facebook/Twitter Banner**: 1200×630px
- **Instagram Story**: 1080×1920px  
- **LinkedIn Banner**: 1584×396px

### App Store Screenshots
- **iOS**: 1284×2778px (iPhone 13 Pro)
- **Android**: 1080×1920px (Standard)

## Quality Checklist

### Before Release
- [ ] All logos display correctly in dark/light themes
- [ ] App icons are crisp at all sizes (60px to 1024px)
- [ ] Persian text renders properly on all devices
- [ ] Colors match brand guidelines exactly
- [ ] No pixelation or blurriness
- [ ] Proper aspect ratios maintained

### Accessibility
- [ ] Sufficient color contrast (4.5:1 minimum)
- [ ] Logo readable at smallest size (60px)
- [ ] Alternative text descriptions provided
- [ ] Works with screen readers

## Maintenance

### Version Updates
- Keep SVG source files as master
- Regenerate PNG assets when updating
- Test on multiple devices/screen densities
- Update brand guidelines if colors change

### Performance
- Optimize PNG file sizes for mobile
- Use WebP format for web if supported
- Consider SVG for scalable web usage
- Cache logo assets appropriately

---

**🏛️ Your Persepolis Wordle brand is ready to celebrate Persian heritage in the digital age!** 