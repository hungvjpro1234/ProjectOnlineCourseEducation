# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an **Android Online Learning Platform (University Project)** - a video-based online course app built with Java/XML frontend and Node.js backend.

**Project Context:**
- **Purpose:** University coursework - demonstrating full-stack mobile app development
- **Development Approach:** Frontend and Backend developed in parallel by separate teams
- **Goal:** Functional demo app that connects to real backend and runs smoothly without crashes

**Current Status:**
- FakeApiService implementations provide in-memory data for frontend development
- Backend API exists but integration is in progress
- All logic and features are tested and working with FakeApiService
- Goal: Maintain same behavior and logic when switching to real backend

**Project Goals (NOT Production App):**
- ✅ Connect to real backend APIs
- ✅ Run smoothly like FakeApiService (no lag, no crashes)
- ✅ Maintain all existing logic and features
- ✅ Handle network operations properly (no ANR)
- ❌ NOT focused on: Scalability, advanced security, enterprise architecture
- ❌ NOT required: MVVM refactoring, unit testing, offline caching

## Build and Run Commands

### Android App

```bash
# Build the app
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

**App Configuration:**
- Package: `com.example.projectonlinecourseeducation`
- Min SDK: 30 (Android 11)
- Target SDK: 36 (Android 14)
- Language: Java (100%)
- Build System: Gradle Kotlin DSL

### Backend Server

```bash
# Navigate to backend directory
cd BackEndAppAndroid

# Install dependencies
npm install

# Start server (default port 3000)
node server.js
```

**Backend Configuration:**
- Database: PostgreSQL at `postgres://postgres:07052004@127.0.0.1:5432/online-learning2`
- Port: 3000
- JWT Secret: "apphoctap"

## Architecture

### 3-Tier Layered Architecture

The app follows a clean layered architecture pattern:

```
┌─────────────────────────────────────────┐
│   PRESENTATION (feature/)               │
│   Activities, Fragments, Adapters       │
└──────────────┬──────────────────────────┘
               │ uses
┌──────────────▼──────────────────────────┐
│   DATA LAYER (data/)                    │
│   ApiProvider → Fake/Remote Services    │
└──────────────┬──────────────────────────┘
               │ operates on
┌──────────────▼──────────────────────────┐
│   DOMAIN LAYER (core/)                  │
│   Business Models (POJOs) + Utils       │
└─────────────────────────────────────────┘
```

### Key Components

**ApiProvider Pattern:**
- Central singleton registry at [data/ApiProvider.java](app/src/main/java/com/example/projectonlinecourseeducation/data/ApiProvider.java)
- Provides instances of all API services
- Allows swapping FakeApi ↔ RemoteApi implementations:
  ```java
  ApiProvider.getAuthApi()      // Get current implementation
  ApiProvider.setAuthApi(...)   // Swap implementation (Fake or Remote)
  ```

**API Modules (data/):**
- `AuthApi` - Authentication (login, register, password reset)
- `CourseApi` - Course CRUD, filtering, search, sorting
- `LessonApi` - Lesson CRUD with Observer pattern for updates
- `CartApi` - Shopping cart management
- `MyCourseApi` - Purchased courses tracking
- `ReviewApi` - Course reviews and ratings
- `LessonProgressApi` - Video progress tracking

**All APIs return `ApiResult<T>`:**
```java
class ApiResult<T> {
    boolean success;
    String message;
    T data;
}
```

### Feature Modules

**Auth Module** ([feature/auth/](app/src/main/java/com/example/projectonlinecourseeducation/feature/auth/)):
- Entry point: `MainActivity2` (launcher activity)
- Login → Routes to StudentHome/TeacherHome/AdminHome based on role
- Seed users in FakeApiService:
  - Student: `student1` / `Pass123`
  - Teacher: `teacher` / `teacher`
  - Admin: `admin` / `Admin123`

**Student Module** ([feature/student/](app/src/main/java/com/example/projectonlinecourseeducation/feature/student/)):
- Main: `StudentHomeActivity` with BottomNavigationView (5 tabs)
- Fragments: Home, Cart, MyCourse, Notification, User
- Key activities:
  - `StudentCourseProductDetailActivity` - Course details with state-aware buttons
  - `StudentCourseLessonActivity` - Lesson list with progress tracking
  - `StudentLessonVideoActivity` - YouTube video player with progress tracking

**Teacher Module** ([feature/teacher/](app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/)):
- Main: `TeacherHomeActivity` with BottomNavigationView (4 tabs)
- Course management: Create, Edit, Delete
- Lesson management with **staged changes pattern**:
  - Changes are local until "Save" is clicked
  - Prevents accidental data loss
  - See [TeacherCourseEditActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/activity/TeacherCourseEditActivity.java)

**Course Status Resolution:**
- Uses `CourseStatusResolver` ([core/utils/CourseStatusResolver.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/CourseStatusResolver.java))
- Three states: PURCHASED, IN_CART, NOT_PURCHASED
- Determines button states in course detail views

### YouTube Integration

Uses `pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0`
- Video ID extraction via `YouTubeUtils` ([core/utils/YouTubeUtils.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/YouTubeUtils.java))
- Auto-duration calculation in `LessonFakeApiService` using `VideoDurationHelper`
- Observer pattern via `LessonUpdateListener` notifies UI when duration is calculated

## Parallel Development: FakeApiService Strategy

**Why FakeApiService Exists:**

Since frontend and backend are developed in parallel, FakeApiService allows frontend development to proceed independently:

1. **Frontend Team:** Build UI, implement features, test logic using in-memory FakeApiService
2. **Backend Team:** Develop REST APIs, database, business logic
3. **Integration:** Swap FakeApiService with RemoteApiService when backend ready

**Benefits:**
- ✅ No waiting for backend - frontend development continues
- ✅ Fast iteration - instant response (no network delay)
- ✅ Same interface - switching to RemoteApi requires minimal changes
- ✅ Logic tested - all features verified before backend integration

**Current State:**
- All FakeApiService implementations complete and tested
- AuthRemoteApiService implemented and ready
- Other RemoteApiServices to be implemented as backend features complete

## Backend Integration Guide

### Critical: Avoiding ANR (Application Not Responding)

**⚠️ IMPORTANT:** The ONLY mandatory change when integrating backend is handling network calls properly to avoid ANR crashes.

**The Problem:**
- FakeApiService is synchronous (instant, in-memory) → works on main thread
- RemoteApiService uses network calls → **CANNOT run on main thread**
- Running network on main thread causes:
  - ANR crash after 5 seconds
  - "Application Not Responding" dialog
  - `NetworkOnMainThreadException` (if StrictMode enabled)

**The Solution:** Use `AsyncApiHelper` to run API calls on background thread

### AsyncApiHelper Pattern

Create this helper class once, use everywhere:

**File:** [core/utils/AsyncApiHelper.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/AsyncApiHelper.java)

```java
package com.example.projectonlinecourseeducation.core.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper to run API calls on background thread without MVVM refactoring
 * Prevents ANR when using RemoteApiService
 */
public class AsyncApiHelper {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Execute API call on background thread, callback on main thread
     *
     * Usage:
     * AsyncApiHelper.execute(
     *     () -> ApiProvider.getAuthApi().login(username, password),
     *     new AsyncApiHelper.ApiCallback<ApiResult<User>>() {
     *         @Override
     *         public void onSuccess(ApiResult<User> result) {
     *             // Handle result on main thread
     *         }
     *
     *         @Override
     *         public void onError(Exception e) {
     *             // Handle error on main thread
     *         }
     *     }
     * );
     */
    public static <T> void execute(ApiCall<T> apiCall, ApiCallback<T> callback) {
        executor.execute(() -> {
            try {
                T result = apiCall.call();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public interface ApiCall<T> {
        T call();
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
```

### Usage Example: LoginActivity

**Before (works with FakeApi, crashes with RemoteApi):**
```java
public class LoginActivity extends AppCompatActivity {

    private void handleLogin() {
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();

        // ❌ PROBLEM: Runs on main thread - crashes with RemoteApiService
        ApiResult<User> result = ApiProvider.getAuthApi()
            .loginByUsername(username, password);

        if (result.isSuccess()) {
            User user = result.getData();
            navigateToHome(user);
        } else {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
```

**After (works with both FakeApi and RemoteApi):**
```java
public class LoginActivity extends AppCompatActivity {

    private void handleLogin() {
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();

        // ✅ SOLUTION: Wrap with AsyncApiHelper
        AsyncApiHelper.execute(
            // API call (runs on background thread)
            () -> ApiProvider.getAuthApi().loginByUsername(username, password),

            // Callback (runs on main thread - can update UI)
            new AsyncApiHelper.ApiCallback<ApiResult<User>>() {
                @Override
                public void onSuccess(ApiResult<User> result) {
                    if (result.isSuccess()) {
                        User user = result.getData();
                        navigateToHome(user);
                    } else {
                        Toast.makeText(LoginActivity.this,
                            result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(LoginActivity.this,
                        "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
}
```

### Files That Need AsyncApiHelper

When integrating RemoteApiService, wrap API calls in these files:

**Auth Module (3 files):**
1. [LoginActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/auth/activity/LoginActivity.java)
2. [RegisterActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/auth/activity/RegisterActivity.java)
3. [ForgotPasswordActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/auth/activity/ForgotPasswordActivity.java)

**Student Module (6 files):**
4. [StudentHomeFragment.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/student/fragment/StudentHomeFragment.java) - Course list loading
5. [StudentCourseProductDetailActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/student/activity/StudentCourseProductDetailActivity.java) - Course details, cart operations
6. [StudentCourseLessonActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/student/activity/StudentCourseLessonActivity.java) - Lesson list, review submission
7. [StudentLessonVideoActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/student/activity/StudentLessonVideoActivity.java) - Progress updates
8. [StudentCartFragment.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/student/fragment/StudentCartFragment.java) - Payment operations
9. [StudentEditProfileActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/student/activity/StudentEditProfileActivity.java) - Profile updates

**Teacher Module (2 files):**
10. [TeacherHomeFragment.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/fragment/TeacherHomeFragment.java) - Course list, delete operations
11. [TeacherCourseEditActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/activity/TeacherCourseEditActivity.java) - Save course/lessons

**Total:** ~11 files need AsyncApiHelper wrapper (minimal changes per file)

## Integrating Remote API (Auth Module Example)

The Auth module demonstrates complete backend integration with both FakeApiService and RemoteApiService.

### Setup and Configuration

**1. Start the Backend Server:**
```bash
cd BackEndAppAndroid
node server.js
```

**2. Configure Base URL in RetrofitClient:**

Edit [data/network/RetrofitClient.java](app/src/main/java/com/example/projectonlinecourseeducation/data/network/RetrofitClient.java):

```java
// For Android Emulator (AVD):
private static final String BASE_URL = "http://10.0.2.2:3000/";

// For Physical Device (use your computer's IP):
private static final String BASE_URL = "http://192.168.1.XXX:3000/";
```

**3. Initialize RetrofitClient in MainActivity:**

```java
public class MainActivity2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RetrofitClient (do this once at app startup)
        RetrofitClient.initialize(this);

        // Optional: Swap to RemoteApiService (default is FakeApiService)
        // ApiProvider.setAuthApi(new AuthRemoteApiService());

        // ... rest of code
    }
}
```

**4. Wrap API Calls with AsyncApiHelper:**

In LoginActivity, RegisterActivity, etc., wrap all API calls:

```java
// Replace direct calls like this:
ApiResult<User> result = ApiProvider.getAuthApi().loginByUsername(username, password);

// With AsyncApiHelper:
AsyncApiHelper.execute(
    () -> ApiProvider.getAuthApi().loginByUsername(username, password),
    new AsyncApiHelper.ApiCallback<ApiResult<User>>() {
        @Override
        public void onSuccess(ApiResult<User> result) {
            // Handle result
        }

        @Override
        public void onError(Exception e) {
            // Handle error
        }
    }
);
```

### How It Works

**Architecture:**
```
Activity/Fragment
    ↓ calls
AsyncApiHelper.execute()
    ↓ runs on background thread
ApiProvider.getAuthApi()
    ↓ returns
AuthRemoteApiService (implements AuthApi)
    ↓ uses Retrofit
RetrofitClient → AuthRetrofitService
    ↓ HTTP calls
Backend Server (Node.js + PostgreSQL)
    ↓ callback on main thread
Activity/Fragment receives result
```

**Session Management:**
- JWT token from `/login` is automatically saved in SharedPreferences
- Token is automatically added to all subsequent requests via OkHttp interceptor
- Current user is cached in SessionManager
- Session persists across app restarts

**Available Remote Endpoints:**
- ✅ `loginByUsername()` → POST /login
- ✅ `register()` → POST /signup
- ✅ `requestPasswordResetLink()` → POST /forgot-password-request
- ✅ `finalizeResetViaLink()` → POST /forgot-password-update
- ⚠️ `updateCurrentUserProfile()` - Backend endpoint not implemented (local update only)
- ⚠️ `changeCurrentUserPassword()` - Backend endpoint not implemented (local update only)

### Switching Between Fake and Remote API

```java
// Use FakeApiService (in-memory, no network, instant response)
ApiProvider.setAuthApi(AuthFakeApiService.getInstance());

// Use RemoteApiService (real backend, network calls)
RetrofitClient.initialize(context);
ApiProvider.setAuthApi(new AuthRemoteApiService());
```

**Note:** AsyncApiHelper works with BOTH FakeApi and RemoteApi, so you can switch freely without code changes.

### Error Handling

RemoteApiService handles:
- Network errors (IOException) → "Lỗi kết nối mạng..."
- HTTP errors (4xx, 5xx) → Error message from response body
- Parsing errors → "Lỗi không xác định..."
- All errors are logged with TAG "AuthRemoteApiService"

### Testing Checklist

When testing RemoteApiService:
1. ✅ Ensure backend is running (`node server.js`)
2. ✅ Verify BASE_URL is correct (10.0.2.2 for emulator)
3. ✅ Check PostgreSQL is running and accessible
4. ✅ Test with seed users:
   - Student: `student1` / `Pass123`
   - Teacher: `teacher` / `teacher`
   - Admin: `admin` / `Admin123`
5. ✅ Monitor Logcat for network logs (HttpLoggingInterceptor.Level.BODY)
6. ✅ Verify token is saved after login (check SharedPreferences)
7. ✅ Test on both emulator and physical device

### File Structure (Remote API)

```
data/
├── auth/
│   ├── AuthApi.java                    # Interface (contract)
│   ├── AuthFakeApiService.java         # In-memory implementation
│   ├── AuthRemoteApiService.java       # Retrofit implementation
│   ├── ApiResult.java                  # Response wrapper
│   └── remote/                         # Remote API models
│       ├── AuthApiResponse.java        # Generic backend response
│       ├── UserDto.java                # User DTO from backend
│       ├── AuthRetrofitService.java    # Retrofit interface
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       ├── ForgotPasswordRequest.java
│       └── ResetPasswordRequest.java
└── network/                            # Network layer
    ├── RetrofitClient.java             # Retrofit singleton
    └── SessionManager.java             # JWT token + user storage
```

## Common Development Patterns

### Adding a New API Module

1. Create interface in `data/xxx/XxxApi.java`
2. Create FakeApiService: `data/xxx/XxxFakeApiService.java` (singleton)
3. Register in `ApiProvider`:
   ```java
   private static XxxApi xxxApi = XxxFakeApiService.getInstance();
   public static XxxApi getXxxApi() { return xxxApi; }
   public static void setXxxApi(XxxApi api) { xxxApi = api; }
   ```
4. Use in UI: `ApiProvider.getXxxApi().someMethod()`

### Adding a New Feature Screen

1. Create layout: `res/layout/activity_[role]_[name].xml`
2. Create activity: `feature/[role]/activity/[Role][Name]Activity.java`
3. Register in `AndroidManifest.xml`
4. Get API reference: `ApiProvider.getXxxApi()`
5. Update data, notify adapter/UI

### Working with Courses

Course status is central to UX. Always use:
```java
CourseStatus status = CourseStatusResolver.getCourseStatus(
    course,
    ApiProvider.getMyCourseApi(),
    ApiProvider.getCartApi()
);
```

This determines button visibility/behavior in course detail views.

## Important Notes and Limitations

### Category System

31+ predefined categories in `CourseFakeApiService.FIXED_CATEGORIES`:
```java
"Java", "JavaScript", "Python", "C++", "C#", "Go", "Ruby", ...
```

Categories are **not** fetched from backend. When integrating backend:
- Backend may use different structure (may need mapping)
- Consider making categories dynamic from API

### Lesson Update Listener Pattern

`LessonApi` uses Observer pattern for async duration calculation:
```java
lessonApi.addLessonUpdateListener(lesson -> {
    // Update UI when duration is calculated
});
```

Used in `TeacherCourseEditActivity` and `LessonEditAdapter`.

### Model Differences: App vs Backend

**Backend has Chapter concept (not in app):**
- Backend: Course → Chapter → Lesson
- App: Course → Lesson (flat structure)

**When integrating, consider:**
- Flattening chapters to lessons, OR
- Adding Chapter model to app, OR
- Treating chapters as lesson groups

### Image Loading

Custom `ImageLoader` utility ([core/utils/ImageLoader.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/ImageLoader.java)):
- No caching (reloads on every scroll)
- Works fine for demo purposes

### Price Formatting

Backend uses numeric prices, app expects formatted strings like "1.499.000 VND"

## File Structure Reference

```
app/src/main/java/com/example/projectonlinecourseeducation/
├── core/
│   ├── model/          # POJOs: Course, Lesson, User, Review, etc.
│   └── utils/          # ImageLoader, YouTubeUtils, DialogHelpers, AsyncApiHelper
├── data/
│   ├── ApiProvider.java
│   ├── auth/           # AuthApi + AuthFakeApiService + AuthRemoteApiService
│   ├── course/         # CourseApi + CourseFakeApiService
│   ├── lesson/         # LessonApi + LessonProgressApi + implementations
│   ├── cart/           # CartApi + CartFakeApiService
│   ├── mycourse/       # MyCourseApi + MyCourseFakeApiService
│   ├── review/         # ReviewApi + ReviewFakeApiService
│   └── network/        # RetrofitClient, SessionManager
└── feature/
    ├── auth/activity/      # Login, Register, ForgotPassword, MainActivity2
    ├── student/
    │   ├── activity/       # 5 activities for student features
    │   ├── adapter/        # 8 RecyclerView adapters
    │   └── fragment/       # 5 fragments (Home, Cart, MyCourse, User, Notification)
    ├── teacher/
    │   ├── activity/       # Home, CourseCreate, CourseEdit
    │   ├── adapter/        # Course and lesson adapters
    │   └── fragment/       # 4 fragments
    └── admin/              # Placeholder (incomplete)
```

## Backend Integration Roadmap

### Phase 1: Auth Module ✅ (Complete)
- ✅ AuthRemoteApiService implemented
- ✅ Retrofit + OkHttp configured
- ✅ JWT token management (SessionManager)
- ✅ Login, Register, Password Reset endpoints

### Phase 2: Course Module (Next Priority)
- ⏳ Implement CourseRemoteApiService
- ⏳ Map backend responses to Course model
- ⏳ Handle category differences (backend structure vs fixed list)
- ⏳ Support search, filter, sort operations

### Phase 3: Lesson Module
- ⏳ Implement LessonRemoteApiService
- ⏳ Implement LessonProgressRemoteApiService
- ⏳ Handle Chapter → Lesson mapping
- ⏳ Maintain Observer pattern for duration updates

### Phase 4: Cart & Purchase
- ⏳ Implement CartRemoteApiService
- ⏳ Implement MyCourseRemoteApiService
- ⏳ Payment flow integration

### Phase 5: Review Module
- ⏳ Implement ReviewRemoteApiService
- ⏳ Rating calculation sync with backend

**Integration Pattern for Each Module:**
1. Create `XxxRemoteApiService implements XxxApi`
2. Create request/response DTOs in `remote/` package
3. Create Retrofit interface
4. Update Activities/Fragments with `AsyncApiHelper`
5. Test with `ApiProvider.setXxxApi(new XxxRemoteApiService())`

## Summary: Key Points for University Project

**What Makes This Work:**
1. ✅ **FakeApiService** - Enabled parallel FE/BE development
2. ✅ **Interface-based design** - Easy to swap Fake ↔ Remote
3. ✅ **ApiProvider pattern** - Central configuration point
4. ✅ **AsyncApiHelper** - Simple ANR prevention without architecture overhaul

**Minimal Integration Requirements:**
- Create `AsyncApiHelper` utility class (1 file)
- Wrap API calls in ~11 Activity/Fragment files
- Initialize `RetrofitClient` in MainActivity
- Swap `ApiProvider.setXxxApi()` when ready

**Result:**
- App runs smoothly with real backend
- No crashes from network operations
- Same logic and features as FakeApiService
- Suitable for university project demonstration

**NOT Included (Beyond Scope):**
- MVVM architecture refactoring
- Comprehensive unit testing
- Offline caching with Room database
- Production-level security hardening
- Enterprise scalability patterns

This approach balances academic requirements (demonstrating full-stack integration) with practical development (quick iteration, working demo).
