# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an **Android Online Learning Platform (University Project)** - a video-based online course app built with Java/XML frontend and Node.js backend.

**Project Context:**
- **Purpose:** University coursework - demonstrating full-stack mobile app development
- **Development Approach:** Frontend and Backend developed in parallel by separate teams
- **Goal:** Functional demo app that connects to real backend and runs smoothly without crashes

**Current Status (Updated 2025-12-14):**
- ✅ **Frontend:** 100% COMPLETE - All UI, features, and logic fully implemented
- ✅ **All FakeApiService:** Complete and tested (9 modules)
- ✅ **Admin Statistics Module:** Complete with 3 tabs (Course, Student, Teacher analytics)
- ✅ **Lesson Approval Workflow:** Fixed - approve/reject now handles add/edit/delete correctly
- ✅ **Auth RemoteApiService:** Implemented (backend integration ready)
- ✅ **AsyncApiHelper:** Created and ready (prevents ANR crashes)
- ✅ **Course RemoteApiService:** Implemented with 15/15 endpoints (ready for testing)
- ⚠️ **Other RemoteApiServices:** Not implemented (Lesson, Cart, Review, etc.)
- ⚠️ **Backend APIs:** Auth + Course endpoints ready, others pending
- 🎯 **Next Step:** Test CourseRemoteApiService with backend, then implement remaining modules

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

**Dependencies:**
- AndroidX AppCompat, Material Components
- YouTube Player: `pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0`
- Retrofit 2 + OkHttp + Gson (for backend integration)
- **MPAndroidChart v3.1.0** (for statistics charts)

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
- `AuthApi` - Authentication (login, register, password reset) ✅ **FE Complete + RemoteApi Ready**
- `CourseApi` - Course CRUD, filtering, search, sorting ✅ **FE Complete**
- `LessonApi` - Lesson CRUD with approval workflow + Observer pattern ✅ **FE Complete**
- `CartApi` - Shopping cart management ✅ **FE Complete**
- `MyCourseApi` - Purchased courses tracking ✅ **FE Complete**
- `ReviewApi` - Course reviews and ratings ✅ **FE Complete**
- `LessonProgressApi` - Video progress tracking ✅ **FE Complete**
- `LessonCommentApi` - Lesson comments ✅ **FE Complete**
- `CourseStudentApi` - Course student tracking ✅ **FE Complete**

**Recent Updates (2025-12-14):**
- ✅ Fixed lesson approval bug: `LessonApi.approveAllPendingLessonsForCourse()` now properly handles ADD/EDIT/DELETE
- ✅ Added comprehensive statistics module with MPAndroidChart visualizations

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
- Course management: Create, Edit, Delete (with approval workflow)
- Lesson management with **staged changes pattern**:
  - Changes are local until "Save" is clicked
  - Prevents accidental data loss
  - See [TeacherCourseEditActivity.java](app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/activity/TeacherCourseEditActivity.java)

**Admin Module** ([feature/admin/](app/src/main/java/com/example/projectonlinecourseeducation/feature/admin/)):
- Main: `AdminHomeActivity` with TabLayout (6 tabs)
- **Statistics Tab** (NEW - 2025-12-14):
  - 3 sub-tabs with MPAndroidChart visualizations
  - **Tab 1: Course Statistics** - Revenue-focused with Pie/Bar charts, top courses list
  - **Tab 2: Student Statistics** - Purchase analytics with Horizontal Bar/Pie charts
  - **Tab 3: Teacher Statistics** - Teacher revenue with grouped analytics
- **Course Approval Tab**: Approve/Reject CREATE/EDIT/DELETE for courses + lessons
- **User Management Tabs**: Student, Teacher user management
- **Course Management Tab**: View/manage all courses

**Course Status Resolution:**
- Uses `CourseStatusResolver` ([core/utils/CourseStatusResolver.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/CourseStatusResolver.java))
- Three states: PURCHASED, IN_CART, NOT_PURCHASED
- Determines button states in course detail views

### YouTube Integration

Uses `pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0`
- Video ID extraction via `YouTubeUtils` ([core/utils/YouTubeUtils.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/YouTubeUtils.java))
- Auto-duration calculation in `LessonFakeApiService` using `VideoDurationHelper`
- Observer pattern via `LessonUpdateListener` notifies UI when duration is calculated

### Statistics & Analytics (NEW)

**MPAndroidChart Integration:**
- Library: `com.github.PhilJay:MPAndroidChart:v3.1.0`
- Repository: JitPack (already configured in settings.gradle.kts)

**Admin Statistics Module** ([feature/admin/fragment/](app/src/main/java/com/example/projectonlinecourseeducation/feature/admin/fragment/)):
- **AdminStatisticsCourseFragment**: Revenue analytics with Pie/Bar charts
- **AdminStatisticsStudentFragment**: Purchase/enrollment analytics
- **AdminStatisticsTeacherFragment**: Teacher performance analytics

**Key Features:**
- Real-time calculation from FakeApiService data
- Revenue = `course.price * course.students`
- Top 5/10 rankings with gold/silver/bronze badges
- Material Design color schemes
- Smooth animations (1000ms)

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

**Current State (FE 100% Complete):**
- ✅ All 9 FakeApiService implementations complete and tested
- ✅ All UI screens complete (Student, Teacher, Admin)
- ✅ All business logic working (approval workflows, status resolution, etc.)
- ✅ AuthRemoteApiService implemented (backend integration ready)
- ⚠️ AsyncApiHelper NOT created yet (needed before testing any RemoteApi)
- ⚠️ Other RemoteApiServices not implemented (Course, Lesson, Cart, etc.)
- ⚠️ Backend only has Auth + Course endpoints (Lesson, Cart, Review pending)

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

⚠️ **CRITICAL:** This helper class does NOT exist yet and must be created before testing RemoteApiService.

**File to create:** [core/utils/AsyncApiHelper.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/AsyncApiHelper.java)

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

⚠️ **STATUS:** None of these files have been updated yet. All still use synchronous API calls.

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

⚠️ **NOT YET DONE:** MainActivity2 does not currently initialize RetrofitClient.

Add this code to MainActivity2.onCreate():

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

⚠️ **NOT YET DONE:** LoginActivity, RegisterActivity, and all other files still use direct synchronous calls.

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

Auth Module (✅ Integrated):
- ✅ `loginByUsername()` → POST /login
- ✅ `register()` → POST /signup
- ✅ `requestPasswordResetLink()` → POST /forgot-password-request
- ✅ `finalizeResetViaLink()` → POST /forgot-password-update
- ⚠️ `updateCurrentUserProfile()` → PUT /auth/profile (exists in backend, not used in app)
- ⚠️ `changeCurrentUserPassword()` → POST /auth/change-password (exists in backend, not used in app)

Course Module (⏳ Backend ready, app not integrated):
- ⏳ POST /course - Create course (with image upload)
- ⏳ GET /course - List courses (with optional teacher filter)
- ⏳ GET /course/:id - Get course detail
- ⏳ PATCH /course/:id - Update course
- ⏳ DELETE /course/:id - Delete course
- ⏳ POST /course/:id/purchase - Record purchase

Lesson, Cart, Review, Progress Modules (❌ Backend not ready):
- ❌ No endpoints implemented yet

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

### Lesson Approval Workflow (Fixed 2025-12-14)

When admin approves course EDIT, ALL lesson changes are now handled correctly:

```java
// In AdminCourseApprovalFragment
case EDIT:
    courseApi.approveCourseEdit(courseId);

    // CRITICAL: Approve ALL pending lesson changes (add/edit/delete)
    lessonApi.approveAllPendingLessonsForCourse(courseId);
```

**What this does:**
1. Approves newly added lessons (marks `isInitialApproved = true`)
2. Applies pending edits to existing lessons
3. Permanently deletes lessons marked for deletion
4. Notifies all listeners to refresh UI

**Before the fix:** Only edited lessons were approved, added/deleted lessons were ignored.

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
│   └── utils/          # ImageLoader, YouTubeUtils, DialogHelpers, (AsyncApiHelper - todo)
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
    └── admin/
        ├── activity/       # AdminHomeActivity
        ├── adapter/        # Course/User/Statistics adapters (15+ adapters)
        └── fragment/       # 6 main tabs + 3 statistics sub-tabs
```

## Backend Integration Roadmap

### Frontend Status: ✅ 100% COMPLETE

**All FE features implemented and tested:**
- ✅ Auth flow (Login, Register, Password Reset)
- ✅ Student features (Browse, Cart, Purchase, Video Player, Progress)
- ✅ Teacher features (Course CRUD, Lesson management, Approval workflow)
- ✅ Admin features (User management, Course approval, Statistics dashboard)
- ✅ All business logic working with FakeApiService
- ✅ UI/UX polished and responsive

### Backend Integration: ⚠️ Pending

**Phase 0: Critical Blockers ✅ (COMPLETE)**
**Status: 3/3 complete**
- ✅ Create AsyncApiHelper.java utility class
- ✅ Initialize RetrofitClient in MainActivity2
- ✅ Test AuthRemoteApiService successfully

**Completed:** 2025-12-14

**Phase 1: Auth Module ✅ (COMPLETE)**
**Status: 4/4 complete**
- ✅ AuthRemoteApiService implemented
- ✅ Retrofit + OkHttp configured
- ✅ JWT token management (SessionManager)
- ✅ Login, Register, Password Reset endpoints
- ✅ AsyncApiHelper integration ready

**Completed:** 2025-12-14

**Phase 2: Course Module ✅ (COMPLETE - Ready for Testing)**
**Status: 5/5 complete**
- ✅ Created CourseRemoteApiService class (19/19 methods)
- ✅ Created CourseRetrofitService interface (15 endpoints)
- ✅ Created Course DTOs (CourseDto, CourseApiResponse)
- ✅ Mapped backend data to app models (ID conversion, field mapping)
- ✅ Added CourseRetrofitService to RetrofitClient

**Backend Status:** ✅ All 15 Course endpoints available
**Next Step:** Test with backend + wrap Activities/Fragments with AsyncApiHelper
**Completed:** 2025-12-14

**Phase 3: Cart & Purchase ✅ (COMPLETE - Ready for Testing)**
**Status: 8/8 complete**
- ✅ Added checkout() method to CartApi interface
- ✅ Implemented checkout() in CartFakeApiService
- ✅ Created CartDto and request/response models (6 DTOs)
- ✅ Created CartRetrofitService interface (5 endpoints)
- ✅ Created CartRemoteApiService implementation (14 methods)
- ✅ Updated RetrofitClient to add CartRetrofitService
- ✅ Refactored StudentCartFragment to use checkout() with AsyncApiHelper
- ✅ All Cart API operations ready for backend integration

**Backend Status:** ✅ 5/6 Cart endpoints available
**Available Endpoints:**
- GET /cart/:userId - Get cart items
- POST /cart/add - Add to cart
- POST /cart/remove - Remove from cart
- POST /cart/checkout - Checkout
- GET /course/:userId/:courseId/status - Check course status

**Missing Endpoints:**
- ⚠️ POST /cart/clear - Not implemented (using workaround: remove each item)

**Critical Backend Requirement:**
⚠️ GET /cart/:userId MUST JOIN with course table to return full Course data (not just payment_status records)

**Next Step:** Test CartRemoteApiService with backend
**Completed:** 2025-12-14

**Phase 4: Lesson Module (BLOCKED - Backend Not Ready)**
**Status: 0/6 complete**
- ❌ Backend: Implement Lesson CRUD endpoints (GET/POST/PATCH/DELETE /lesson)
- ❌ Backend: Implement Chapter → Lesson relationship
- ❌ App: Create LessonRemoteApiService
- ❌ App: Create LessonProgressRemoteApiService
- ❌ App: Handle Chapter → Lesson mapping (flatten or add Chapter model)
- ❌ App: Wrap StudentCourseLessonActivity, StudentLessonVideoActivity with AsyncApiHelper

**Backend Status:** ❌ No Lesson endpoints exist
**Blocker:** Backend team must implement Lesson API first
**Estimated Time:** 3-4 hours (after backend ready)

**Phase 5: Review & Comments (BLOCKED - Backend Not Ready)**
**Status: 0/4 complete**
- ❌ Backend: Implement Review CRUD endpoints
- ❌ Backend: Implement LessonComment CRUD endpoints
- ❌ App: Create ReviewRemoteApiService
- ❌ App: Create LessonCommentRemoteApiService

**Backend Status:** ❌ No Review/Comment endpoints exist
**Estimated Time:** 2-3 hours (after backend ready)

**Phase 6: Student Tracking (BLOCKED - Backend Not Ready)**
**Status: 0/2 complete**
- ❌ Backend: Implement CourseStudent tracking endpoints
- ❌ App: Create CourseStudentRemoteApiService

**Backend Status:** ❌ No CourseStudent endpoints exist
**Estimated Time:** 1-2 hours (after backend ready)

---

## Current Integration Status Summary

| Module | FE Status | RemoteApi Status | Backend API | Integration Status |
|--------|-----------|------------------|-------------|-------------------|
| Auth | ✅ Complete | ✅ Complete | ✅ Complete | ✅ Ready for Use |
| Course | ✅ Complete | ✅ Complete | ✅ Complete | 🟡 Ready for Testing |
| Cart | ✅ Complete | ✅ Complete | 🟡 Partial (5/6 endpoints) | 🟡 Ready for Testing |
| Lesson | ✅ Complete | ❌ Not created | ❌ Not ready | 🔴 Blocked by Backend |
| LessonProgress | ✅ Complete | ❌ Not created | ❌ Not ready | 🔴 Blocked by Backend |
| MyCourse | ✅ Complete | ❌ Not created | ❌ Not ready | 🔴 Blocked by Backend |
| Review | ✅ Complete | ❌ Not created | ❌ Not ready | 🔴 Blocked by Backend |
| LessonComment | ✅ Complete | ❌ Not created | ❌ Not ready | 🔴 Blocked by Backend |
| CourseStudent | ✅ Complete | ❌ Not created | ❌ Not ready | 🔴 Blocked by Backend |

**Frontend Progress:** 100% (All features complete)
**Backend Integration Progress:** 3/9 modules (33%) - Auth + Course + Cart complete

**Integration Pattern for Each Module:**
1. Create `XxxRemoteApiService implements XxxApi`
2. Create request/response DTOs in `remote/` package
3. Create Retrofit interface
4. Update Activities/Fragments with `AsyncApiHelper`
5. Test with `ApiProvider.setXxxApi(new XxxRemoteApiService())`

## Summary: Key Points for University Project

**What Makes This Work:**
1. ✅ **FakeApiService** - Enabled 100% parallel FE/BE development
2. ✅ **Interface-based design** - Easy to swap Fake ↔ Remote
3. ✅ **ApiProvider pattern** - Central configuration point
4. ⚠️ **AsyncApiHelper** - Simple ANR prevention (needs to be created)

**Frontend Achievements (100% Complete):**
- ✅ All UI screens implemented (Student, Teacher, Admin)
- ✅ All business logic working (approval workflows, cart, payments)
- ✅ Statistics dashboard with interactive charts
- ✅ YouTube video player with progress tracking
- ✅ Comprehensive course/lesson management
- ✅ User authentication and authorization
- ✅ Responsive Material Design UI

**Next Steps for Backend Integration:**
1. ✅ Create `AsyncApiHelper` utility class - **COMPLETE**
2. ✅ Initialize `RetrofitClient` in MainActivity2 - **COMPLETE**
3. 🟡 Wrap API calls in Activity/Fragment files (StudentCartFragment done, others pending)
4. ⏳ Test Auth + Course + Cart modules with backend
5. ⏳ Backend team: Implement remaining 6 API modules (Lesson, MyCourse, Review, Progress, Comments, Student tracking)
6. ⏳ Create RemoteApiService for each module when backend ready

**Expected Result (when backend complete):**
- App runs smoothly with real backend
- No crashes from network operations
- Same UX as FakeApiService (no lag, no ANR)
- Suitable for university project demonstration

**Current Gaps:**
- ✅ AsyncApiHelper created - ready for use (prevents ANR)
- ✅ 3/9 modules have RemoteApiService (Auth + Course + Cart complete)
- 🟡 Activities/Fragments: StudentCartFragment wrapped with AsyncApiHelper, others pending
- ❌ Backend missing 6/9 API modules (Lesson, MyCourse, Review, Progress, Comments, Student tracking)
- ⚠️ Backend Cart module: Missing POST /cart/clear endpoint + GET /cart/:userId needs JOIN with course table
- ⚠️ Backend Course module: Missing 3 endpoints (search/sort, related courses, reject-initial)

**NOT Included (Beyond Scope):**
- MVVM architecture refactoring
- Comprehensive unit testing
- Offline caching with Room database
- Production-level security hardening
- Enterprise scalability patterns

**This approach successfully balanced:**
- ✅ Academic requirements (demonstrating full-stack mobile development)
- ✅ Practical development (quick iteration with FakeApi)
- ✅ Professional quality (clean architecture, working features)
- ✅ Team collaboration (parallel FE/BE development)

**Ready for demo:** The frontend is 100% complete and fully functional with FakeApiService. Backend integration can proceed whenever APIs are ready, with minimal changes required (just add AsyncApiHelper wrapper to existing API calls).
