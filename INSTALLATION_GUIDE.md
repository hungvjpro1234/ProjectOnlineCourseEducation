# 🚀 HƯỚNG DẪN COPY CODE VÀO ANDROID STUDIO

## 📦 TÓM TẮT: 13 FILES ĐÃ THAY ĐỔI/TẠO MỚI

### ✅ BƯỚC 1: CẬP NHẬT DEPENDENCIES (2 files)

#### 1.1. File: `gradle/libs.versions.toml`

**Vị trí thêm code:** Sau dòng `androidYoutubePlayerCore = "13.0.0"`

Thêm 3 dòng này vào section `[versions]`:
```toml
retrofit = "2.9.0"
okhttp = "4.12.0"
gson = "2.10.1"
```

**Vị trí thêm code:** Sau dòng `android-youtube-player-core = { ... }`

Thêm vào section `[libraries]`:
```toml
# Retrofit + OkHttp + Gson:
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }
```

---

#### 1.2. File: `app/build.gradle.kts`

**Vị trí thêm code:** Trong section `dependencies`, sau dòng `implementation(libs.android.youtube.player.core)`

Thêm:
```kotlin
    // Retrofit + OkHttp + Gson
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
```

**⚠️ SAU KHI THÊM:** Click "Sync Now" trong Android Studio

---

### ✅ BƯỚC 2: TẠO THƯ MỤC MỚI

Trong Android Studio, tạo 2 package mới:

1. **Package:** `data.auth.remote`
   - Cách tạo: Right-click `data/auth` → New → Package → Nhập `remote`

2. **Package:** `data.network`
   - Cách tạo: Right-click `data` → New → Package → Nhập `network`

---

### ✅ BƯỚC 3: TẠO 11 FILE JAVA MỚI

Copy nội dung từ các file sau đây:

#### 📁 Trong package `data.auth.remote` (6 files):

**3.1. AuthApiResponse.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/auth/remote/AuthApiResponse.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\auth\remote\AuthApiResponse.java
```

**3.2. UserDto.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/auth/remote/UserDto.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\auth\remote\UserDto.java
```

**3.3. LoginRequest.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/auth/remote/LoginRequest.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\auth\remote\LoginRequest.java
```

**3.4. RegisterRequest.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/auth/remote/RegisterRequest.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\auth\remote\RegisterRequest.java
```

**3.5. ForgotPasswordRequest.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/auth/remote/ForgotPasswordRequest.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\auth\remote\ForgotPasswordRequest.java
```

**3.6. ResetPasswordRequest.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/auth/remote/ResetPasswordRequest.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\auth\remote\ResetPasswordRequest.java
```

**3.7. AuthRetrofitService.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/auth/remote/AuthRetrofitService.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\auth\remote\AuthRetrofitService.java
```

---

#### 📁 Trong package `data.network` (2 files):

**3.8. RetrofitClient.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/network/RetrofitClient.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\network\RetrofitClient.java
```

**3.9. SessionManager.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/network/SessionManager.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\network\SessionManager.java
```

---

#### 📁 Trong package `data.auth` (1 file):

**3.10. AuthRemoteApiService.java**
```
📍 Location: app/src/main/java/com/example/projectonlinecourseeducation/data/auth/AuthRemoteApiService.java
📄 Copy từ: d:\ProjectOnlineCourseEducation\app\src\main\java\...\data\auth\AuthRemoteApiService.java
```

---

### ✅ BƯỚC 4: SỬA FILE MainActivity2.java

**File:** `feature/auth/activity/MainActivity2.java`

**Thêm import** ở đầu file:
```java
import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.data.auth.AuthRemoteApiService;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;
```

**Thêm code** trong `onCreate()`, TRƯỚC tất cả code khác:
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // THÊM 2 DÒNG NÀY:
    RetrofitClient.initialize(this);
    ApiProvider.setAuthApi(new AuthRemoteApiService());

    // ... phần code cũ của bạn ...
}
```

---

### ✅ BƯỚC 5: CHỈNH BASE_URL (NẾU CẦN)

**File:** `data/network/RetrofitClient.java`

**Dòng 25-26:**
```java
private static final String BASE_URL = "http://10.0.2.2:3000/";
```

**Giải thích:**
- `10.0.2.2:3000` → Cho Android Emulator (AVD)
- Nếu dùng **thiết bị thật**, đổi thành IP máy tính của bạn:
  ```java
  private static final String BASE_URL = "http://192.168.1.XXX:3000/";
  ```
  (Thay XXX bằng IP máy tính của bạn)

---

### ✅ BƯỚC 6: START BACKEND

Trước khi chạy app, start backend server:

```bash
cd BackEndAppAndroid
node server.js
```

Phải thấy message:
```
Server đang chạy tại http://localhost:3000
```

---

## 🎯 CHECKLIST HOÀN THÀNH

- [ ] 1. Sửa `gradle/libs.versions.toml` (thêm versions + libraries)
- [ ] 2. Sửa `app/build.gradle.kts` (thêm dependencies)
- [ ] 3. Click "Sync Now" trong Android Studio
- [ ] 4. Tạo package `data.auth.remote`
- [ ] 5. Tạo package `data.network`
- [ ] 6. Copy 7 files vào `data.auth.remote/`
- [ ] 7. Copy 2 files vào `data.network/`
- [ ] 8. Copy 1 file vào `data.auth/`
- [ ] 9. Sửa `MainActivity2.java` (thêm initialize code)
- [ ] 10. Kiểm tra BASE_URL trong `RetrofitClient.java`
- [ ] 11. Start backend server (`node server.js`)
- [ ] 12. Build & Run app

---

## 📂 CẤU TRÚC THƯ MỤC SAU KHI HOÀN THÀNH

```
app/src/main/java/com/example/projectonlinecourseeducation/
├── data/
│   ├── ApiProvider.java
│   ├── auth/
│   │   ├── AuthApi.java
│   │   ├── AuthFakeApiService.java
│   │   ├── AuthRemoteApiService.java          ✨ NEW
│   │   ├── ApiResult.java
│   │   └── remote/                            ✨ NEW FOLDER
│   │       ├── AuthApiResponse.java           ✨ NEW
│   │       ├── UserDto.java                   ✨ NEW
│   │       ├── AuthRetrofitService.java       ✨ NEW
│   │       ├── LoginRequest.java              ✨ NEW
│   │       ├── RegisterRequest.java           ✨ NEW
│   │       ├── ForgotPasswordRequest.java     ✨ NEW
│   │       └── ResetPasswordRequest.java      ✨ NEW
│   └── network/                               ✨ NEW FOLDER
│       ├── RetrofitClient.java                ✨ NEW
│       └── SessionManager.java                ✨ NEW
```

---

## 🧪 CÁCH TEST

1. **Build app:** Menu → Build → Make Project
2. **Chạy app:** Shift+F10 hoặc click nút Run
3. **Test login:**
   - Username: `student1`
   - Password: `Pass123`
4. **Xem log:** Mở Logcat, filter "OkHttp" để thấy network requests

---

## 🆘 TROUBLESHOOTING

### Lỗi: "Cannot resolve symbol 'retrofit2'"
→ **Fix:** Chưa sync Gradle. Click "Sync Now" ở góc phải trên.

### Lỗi: "RetrofitClient not initialized"
→ **Fix:** Chưa thêm `RetrofitClient.initialize(this)` vào MainActivity2.

### Lỗi: "Failed to connect to /10.0.2.2:3000"
→ **Fix:** Backend chưa chạy. Chạy `node server.js` trong folder BackEndAppAndroid.

### Lỗi: Build failed
→ **Fix:** Clean project: Menu → Build → Clean Project, rồi Rebuild.

---

## 📚 TÀI LIỆU THAM KHẢO

- Chi tiết hơn: Xem file `REMOTE_API_USAGE_EXAMPLE.md`
- Architecture: Xem file `CLAUDE.md` (section "Integrating Remote API")

---

## ✨ KẾT QUẢ MONG ĐỢI

Sau khi hoàn thành:
- ✅ App connect với backend thật
- ✅ Login lưu JWT token vào SharedPreferences
- ✅ Session persist khi restart app
- ✅ Thấy network logs trong Logcat
- ✅ Không cần sửa code ở LoginActivity, RegisterActivity

**Good luck! 🎉**