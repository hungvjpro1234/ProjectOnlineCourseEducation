# Remote API Usage Example

This file demonstrates how to use the AuthRemoteApiService with the backend.

## Step-by-Step Integration

### 1. Start Backend Server

```bash
cd BackEndAppAndroid
node server.js
```

Expected output:
```
Server đang chạy tại http://localhost:3000
```

### 2. Configure BASE_URL

Edit `app/src/main/java/.../data/network/RetrofitClient.java`:

```java
// For Android Emulator:
private static final String BASE_URL = "http://10.0.2.2:3000/";

// OR for Physical Device (replace with your computer's IP):
// private static final String BASE_URL = "http://192.168.1.100:3000/";
```

### 3. Initialize in MainActivity2

Edit `app/src/main/java/.../feature/auth/activity/MainActivity2.java`:

```java
package com.example.projectonlinecourseeducation.feature.auth.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.data.ApiProvider;
import com.example.projectonlinecourseeducation.core.apiservice.AuthService;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // STEP 1: Initialize RetrofitClient
        RetrofitClient.initialize(this);

        // STEP 2: Swap to RemoteApiService
        ApiProvider.setAuthApi(new AuthRemoteApiService());

        // STEP 3: Use as normal (no code changes needed in other activities!)
        // Now all ApiProvider.getAuthApi() calls will use RemoteApiService

        // ... rest of your code
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
```

### 4. No Code Changes Needed in Login/Register Activities!

The beauty of the ApiProvider pattern is that you don't need to change any code in:
- `LoginActivity`
- `RegisterActivity`
- `ForgotPasswordActivity`
- Any other Activity/Fragment that uses AuthApi

They all call `ApiProvider.getAuthApi()` which now returns `AuthRemoteApiService` instead of `AuthFakeApiService`.

### 5. Test Login

Run the app and login with:
- Username: `student1`
- Password: `Pass123`

Check Logcat for network logs:
```
D/OkHttp: --> POST http://10.0.2.2:3000/login
D/OkHttp: {"username":"student1","password":"Pass123"}
D/OkHttp: <-- 200 OK http://10.0.2.2:3000/login
D/OkHttp: {"success":true,"message":"Đăng nhập thành công","data":{...},"token":"eyJhbGc..."}
```

### 6. Check Session Persistence

After login:
1. Close the app (kill process)
2. Reopen the app
3. The user should still be logged in (token persists in SharedPreferences)

To verify, add this in any Activity:
```java
SessionManager sessionManager = RetrofitClient.getSessionManager();
User currentUser = sessionManager.getCurrentUser();
String token = sessionManager.getToken();
Log.d("Session", "User: " + (currentUser != null ? currentUser.getName() : "null"));
Log.d("Session", "Token: " + (token != null ? "exists" : "null"));
```

## Switching Between Fake and Remote

### Use FakeApiService (In-Memory)

```java
// In MainActivity2.onCreate() or Application
ApiProvider.setAuthApi(AuthFakeApiService.getInstance());
```

Benefits:
- No network required
- Instant response
- Works offline
- Good for UI development

### Use RemoteApiService (Real Backend)

```java
// In MainActivity2.onCreate() or Application
RetrofitClient.initialize(this);
ApiProvider.setAuthApi(new AuthRemoteApiService());
```

Benefits:
- Real data persistence
- Database storage
- Production-ready
- Multi-device sync

## Troubleshooting

### Error: "Failed to connect to /10.0.2.2:3000"

**Solution:**
1. Check backend is running: `node server.js`
2. Check PostgreSQL is running
3. For physical device, use your computer's IP instead of 10.0.2.2

### Error: "RetrofitClient not initialized"

**Solution:**
Add `RetrofitClient.initialize(this);` in MainActivity2.onCreate() before using any API.

### Error: "Lỗi kết nối mạng"

**Solution:**
1. Check internet permission in AndroidManifest.xml:
   ```xml
   <uses-permission android:name="android.permission.INTERNET"/>
   ```
2. Check network security config if using HTTP (not HTTPS)
3. Check firewall/antivirus blocking port 3000

### Error: Database connection failed

**Solution:**
1. Ensure PostgreSQL is running
2. Check database credentials in `server.js`:
   ```javascript
   const db = pgp("postgres://postgres:07052004@127.0.0.1:5432/online-learning2");
   ```
3. Create database if not exists:
   ```sql
   CREATE DATABASE "online-learning2";
   ```

## Backend Endpoints Reference

### POST /login
**Request:**
```json
{
  "username": "student1",
  "password": "Pass123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "id": "1",
    "name": "Student One",
    "username": "student1",
    "email": "student1@example.com",
    "verified": true,
    "role": "STUDENT"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### POST /signup
**Request:**
```json
{
  "name": "New User",
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "role": "STUDENT"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký thành công. Bạn có thể đăng nhập.",
  "data": {
    "id": "10",
    "name": "New User",
    "username": "newuser",
    "email": "newuser@example.com",
    "verified": true,
    "role": "STUDENT"
  }
}
```

### POST /forgot-password-request
**Request:**
```json
{
  "email": "student1@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã gửi link đặt lại mật khẩu (demo).",
  "data": "http://127.0.0.1:5500/forgot-password-confirm.html?token=uuid-here"
}
```

### POST /forgot-password-update
**Request:**
```json
{
  "token": "uuid-here",
  "newPassword": "newpassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công qua link.",
  "data": true
}
```

## Performance Tips

### Enable/Disable Logging

In production, disable detailed logging:

Edit `RetrofitClient.java`:
```java
// Development (verbose logs)
loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

// Production (minimal logs)
loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
```

### Timeout Configuration

Default timeouts are 30 seconds. Adjust if needed:

```java
OkHttpClient okHttpClient = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();
```

## Next Steps

1. Implement other RemoteApiService classes:
   - CourseRemoteApiService
   - LessonRemoteApiService
   - CartRemoteApiService
   - MyCourseRemoteApiService
   - ReviewRemoteApiService

2. Add offline caching with Room database

3. Add proper loading states in UI

4. Add retry logic for failed requests

5. Implement token refresh mechanism