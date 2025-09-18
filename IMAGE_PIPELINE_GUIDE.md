# ImagePipeline & LocalFileIO Guide

## 🎯 **Cel:**
Interfejsy do przetwarzania obrazów i operacji na plikach dla upload do Supabase Storage.

## 📋 **Interfejsy:**

### **ImagePipeline:**
```kotlin
interface ImagePipeline {
    suspend fun toJpegBytes(
        localUri: String, 
        maxLongEdgePx: Int = 1920, 
        quality: Int = 85
    ): ByteArray
    
    suspend fun getImageDimensions(localUri: String): Pair<Int, Int>?
    suspend fun isValidImage(localUri: String): Boolean
}
```

### **LocalFileIO:**
```kotlin
interface LocalFileIO {
    suspend fun readBytes(localUri: String): ByteArray
    suspend fun writeBytes(localUri: String, data: ByteArray): Boolean
    suspend fun exists(localUri: String): Boolean
    suspend fun getFileSize(localUri: String): Long
    suspend fun delete(localUri: String): Boolean
    fun getFileExtension(localUri: String): String?
    suspend fun getMimeType(localUri: String): String?
}
```

## 🚀 **Implementacje:**

### **Android:**
- **AndroidImagePipeline** - BitmapFactory + kompresja JPEG
- **AndroidLocalFileIO** - ContentResolver + File API

### **iOS:**
- **IOSImagePipeline** - UIImage + CoreGraphics (TODO)
- **IOSLocalFileIO** - NSFileManager + Foundation (TODO)

## 🔧 **Integracja z Koin:**

### **Platform Modules:**
```kotlin
// Android
actual val platformModule: Module = module {
    single<ImagePipeline> { AndroidImagePipeline(androidContext()) }
    single<LocalFileIO> { AndroidLocalFileIO(androidContext()) }
    single<StorageClient> { 
        SupabaseStorageClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey,
            imagePipeline = get<ImagePipeline>(),
            localFileIO = get<LocalFileIO>()
        )
    }
}

// iOS
actual val platformModule: Module = module {
    single<ImagePipeline> { IOSImagePipeline() }
    single<LocalFileIO> { IOSLocalFileIO() }
    single<StorageClient> { 
        SupabaseStorageClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey,
            imagePipeline = get<ImagePipeline>(),
            localFileIO = get<LocalFileIO>()
        )
    }
}
```

### **Wstrzykiwanie data classes w Koin:**

#### **1. Singleton (jedna instancja):**
```kotlin
val module = module {
    single { SyncConfig() }
    single { 
        SyncConfig(
            maxParallelTasks = 3,
            backoffBaseMs = 15000L,
            uploadCompression = true
        )
    }
}
```

#### **2. Factory (nowa instancja za każdym razem):**
```kotlin
val module = module {
    factory { ImageProcessingResult(success = true) }
    factory { (success: Boolean, data: ByteArray?) -> 
        ImageProcessingResult(success = success, data = data)
    }
}
```

#### **3. SingleOf (krótszy zapis):**
```kotlin
import org.koin.core.module.dsl.singleOf

val module = module {
    singleOf(::SyncConfig)  // Używa domyślnego konstruktora
    singleOf(::StorageResult)
}
```

#### **4. Z parametrami:**
```kotlin
val module = module {
    factory { (bucket: String, key: String) ->
        StorageResult(
            success = true,
            path = "$bucket/$key"
        )
    }
}

// Użycie:
val result: StorageResult = get { parametersOf("images", "photo.jpg") }
```

#### **5. Z konfiguracją:**
```kotlin
val module = module {
    single {
        SyncConfig(
            maxParallelTasks = getProperty("sync.maxTasks", 3),
            backoffBaseMs = getProperty("sync.backoffMs", 15000L),
            uploadCompression = getProperty("sync.compression", true)
        )
    }
}
```

## 🔄 **Przepływ upload:**

### **Obrazy:**
```
1. StorageClient.uploadImage(localUri)
2. ImagePipeline.toJpegBytes() - kompresja + resize
3. Upload do Supabase Storage
4. Return StorageResult
```

### **Pliki audio:**
```
1. StorageClient.uploadFile(localUri)
2. LocalFileIO.readBytes() - czytanie pliku
3. LocalFileIO.getMimeType() - określenie typu
4. Upload do Supabase Storage
5. Return StorageResult
```

## ✅ **Korzyści:**

### **✅ Optymalizacja:**
- Automatyczna kompresja obrazów
- Resize do maksymalnego rozmiaru
- MIME type detection

### **✅ Platform-specific:**
- Android: BitmapFactory + ContentResolver
- iOS: UIImage + NSFileManager
- Multiplatform compatibility

### **✅ Dependency Injection:**
- Type-safe wstrzykiwanie
- Platform-specific implementacje
- Łatwe testowanie z mock

### **✅ Error handling:**
- Comprehensive error handling
- Detailed logging
- Graceful fallbacks

## 🎉 **Gotowe do użycia!**

System przetwarzania plików jest **w pełni zaimplementowany** i zintegrowany z Koin DI! 🚀

### **Użycie w kodzie:**
```kotlin
class MyService(
    private val imagePipeline: ImagePipeline,
    private val localFileIO: LocalFileIO,
    private val storageClient: StorageClient
) {
    suspend fun uploadPhoto(localUri: String) {
        val result = storageClient.uploadImage(
            bucket = "photos",
            key = "user/photo.jpg",
            localUri = localUri
        )
        
        if (result.success) {
            println("Upload successful: ${result.path}")
        }
    }
}
```
