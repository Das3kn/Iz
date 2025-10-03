# Medya Paylaşım Özelliği

Bu güncelleme ile uygulamanıza fotoğraf ve video paylaşım özelliği eklenmiştir.

## Yeni Özellikler

### 1. Medya Seçimi
- **Galeriden Fotoğraf Seçme**: Cihazınızdan fotoğraf seçebilirsiniz
- **Kamera ile Fotoğraf Çekme**: Doğrudan kamera ile fotoğraf çekebilirsiniz
- **Galeriden Video Seçme**: Cihazınızdan video seçebilirsiniz
- **Kamera ile Video Çekme**: Doğrudan kamera ile video çekebilirsiniz

### 2. Medya Gösterimi
- **Tek Medya**: Tek fotoğraf veya video büyük boyutta gösterilir
- **Çoklu Medya**: Birden fazla medya dosyası yan yana gösterilir
- **Video Önizleme**: Videolar için play ikonu ile önizleme gösterilir
- **Karma Medya**: Hem fotoğraf hem video içeren postlar desteklenir

### 3. Medya Yönetimi
- **Medya Kaldırma**: Seçilen medya dosyalarını kaldırabilirsiniz
- **Otomatik Upload**: Medya dosyaları otomatik olarak Firebase Storage'a yüklenir
- **URL Oluşturma**: Yüklenen medya dosyaları için URL'ler otomatik oluşturulur

## Teknik Detaylar

### Eklenen Dosyalar
- `MediaPicker.kt`: Medya seçimi için utility sınıfı
- `file_paths.xml`: FileProvider için gerekli konfigürasyon

### Güncellenen Dosyalar
- `CreatePostScreen.kt`: Medya seçimi UI'ı eklendi
- `CreatePostViewModel.kt`: Medya işleme mantığı eklendi
- `PostRepository.kt`: Medya upload fonksiyonları eklendi
- `ListItem.kt`: Post gösteriminde medya desteği eklendi
- `SavedPostsScreen.kt`: Kaydedilen postlarda medya desteği eklendi
- `build.gradle.kts`: Gerekli kütüphaneler eklendi
- `AndroidManifest.xml`: FileProvider ve izinler eklendi

### Kullanılan Kütüphaneler
- `coil-compose`: Resim yükleme ve gösterimi için
- `androidx.activity:activity-compose`: Activity result launcher için
- `androidx.fragment:fragment-ktx`: Fragment desteği için

## Kullanım

### Post Oluşturma
1. CreatePost ekranına gidin
2. Metin yazın (isteğe bağlı)
3. Medya eklemek için aşağıdaki butonlardan birini kullanın:
   - **Fotoğraf**: Galeriden fotoğraf seçin
   - **Kamera**: Kamera ile fotoğraf çekin
   - **Video**: Galeriden video seçin
   - **Video Çek**: Kamera ile video çekin
4. Seçilen medya dosyalarını önizleyebilir ve kaldırabilirsiniz
5. "Paylaş" butonuna tıklayın

### Medya Görüntüleme
- Ana sayfada ve kaydedilen postlarda medya dosyaları otomatik olarak gösterilir
- Tek medya dosyası büyük boyutta gösterilir
- Çoklu medya dosyaları yan yana gösterilir
- Videolar için play ikonu gösterilir

## İzinler

Uygulama aşağıdaki izinleri kullanır:
- `READ_EXTERNAL_STORAGE`: Galeriden medya seçmek için
- `CAMERA`: Kamera ile medya çekmek için
- `WRITE_EXTERNAL_STORAGE`: Geçici dosyalar oluşturmak için

## Firebase Storage

Medya dosyaları Firebase Storage'da `media/` klasöründe saklanır. Dosya isimleri şu formatta oluşturulur:
- `media_<timestamp>_<index>`

## Notlar

- Medya dosyaları otomatik olarak Firebase Storage'a yüklenir
- Upload işlemi sırasında hata oluşursa post oluşturulmaz
- Medya dosyaları Coil kütüphanesi ile yüklenir ve gösterilir
- Video oynatma özelliği henüz eklenmemiştir (sadece önizleme)
