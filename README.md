# İZ - Sosyal Medya Platformu

İZ, insanların buluşlarını ve yeni keşfettikleri şeyleri paylaşabileceği bir sosyal medya platformudur.

## Özellikler

### 🚀 Ana Özellikler
- **İçerik Paylaşımı**: Fotoğraf, video, ses ve metin içerikleri paylaşabilme
- **Sohbet Sistemi**: Kullanıcılar arası gerçek zamanlı mesajlaşma
- **Firebase Cloud Messaging**: Anlık bildirimler
- **Kullanıcı Kimlik Doğrulama**: Güvenli giriş ve kayıt sistemi

### 📱 Teknik Özellikler
- **Jetpack Compose**: Modern Android UI framework
- **MVVM Architecture**: Model-View-ViewModel mimarisi
- **Hilt**: Dependency injection
- **Firebase**: Backend servisleri
  - Authentication
  - Firestore (veritabanı)
  - Storage (dosya depolama)
  - Cloud Messaging (bildirimler)

## Kurulum

### Gereksinimler
- Android Studio Hedgehog veya üzeri
- Android SDK 24+
- Kotlin 1.9.0+

### Adımlar

1. **Projeyi klonlayın**
   ```bash
   git clone <repository-url>
   cd Iz
   ```

2. **Firebase projesi oluşturun**
   - [Firebase Console](https://console.firebase.google.com/)'a gidin
   - Yeni proje oluşturun
   - Android uygulaması ekleyin (package: com.das3kn.iz)
   - `google-services.json` dosyasını indirin ve `app/` klasörüne yerleştirin

3. **Firebase servislerini etkinleştirin**
   - Authentication (Email/Password)
   - Firestore Database
   - Storage
   - Cloud Messaging

4. **Projeyi derleyin**
   ```bash
   ./gradlew build
   ```

## Proje Yapısı

```
app/
├── src/main/
│   ├── java/com/das3kn/iz/
│   │   ├── data/
│   │   │   ├── model/          # Veri modelleri
│   │   │   └── repository/     # Repository sınıfları
│   │   ├── di/                 # Hilt modülleri
│   │   ├── service/            # Firebase servisleri
│   │   └── ui/presentation/    # UI ekranları
│   │       ├── chat/           # Sohbet ekranları
│   │       ├── home/           # Ana sayfa
│   │       └── navigation/     # Navigation
│   └── res/                    # Kaynaklar
└── build.gradle.kts            # Bağımlılıklar
```

## Kullanım

### Kullanıcı Kaydı ve Giriş
- Uygulama ilk açıldığında kayıt ekranı görünür
- Email ve şifre ile hesap oluşturulabilir
- Mevcut hesapla giriş yapılabilir

### İçerik Paylaşımı
- Ana sayfada "+" butonuna tıklayarak yeni içerik oluşturulabilir
- Fotoğraf, video, ses dosyaları eklenebilir
- Metin açıklaması yazılabilir
- Etiketler eklenebilir

### Sohbet
- Sohbetler sekmesinden mevcut sohbetler görüntülenebilir
- Yeni sohbet başlatılabilir
- Gerçek zamanlı mesajlaşma yapılabilir
- Bildirimler alınabilir

## Güvenlik

- Firebase Authentication ile güvenli kullanıcı yönetimi
- Firestore güvenlik kuralları ile veri koruması
- Storage güvenlik kuralları ile dosya erişim kontrolü

## Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## İletişim

- Proje Sahibi: [Adınız]
- Email: [email@example.com]
- Proje Linki: [https://github.com/username/Iz](https://github.com/username/Iz)
