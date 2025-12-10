# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Recent Updates (December 2024)

**Major Progress:**
- ✅ **Admin Module Complete**: Fully implemented with 6 navigation tabs, statistics dashboard, user management, and comprehensive course management features (previously was just a placeholder)
- ✅ **Backend Cart Endpoints**: Cart functionality implemented on backend (add, remove, checkout, status checking)
- ✅ **Backend Course Endpoints**: Full CRUD operations for courses with image upload support
- ✅ **Auth Endpoints Extended**: Added profile update and password change endpoints

**What Changed:**
- Admin module now has 3 activities, 10 fragments, 15+ adapters
- Backend server updated with cart and course management APIs
- File structure documentation updated to reflect admin implementation
- Integration roadmap reorganized to show current status

**Next Priorities:**
1. Create AsyncApiHelper utility (critical for preventing ANR)
2. Implement RemoteApiServices for Course, Cart, Lesson, Review modules
3. Wrap API calls in activities/fragments with AsyncApiHelper
4. Test complete backend integration

## Project Overview

This is an **Android Online Learning Platform (University Project)** - a video-based online course app built with Java/XML frontend and Node.js backend.

**Project Context:**
- **Purpose:** University coursework - demonstrating full-stack mobile app development
- **Development Approach:** Frontend and Backend developed in parallel by separate teams
- **Goal:** Functional demo app that connects to real backend and runs smoothly without crashes

**Current Status (December 2024):**
- ✅ All frontend modules complete: Student, Teacher, Admin (fully functional)
- ✅ Backend server implemented with Auth, Course, Cart endpoints
- ✅ AuthRemoteApiService integrated and working
- ⚠️ Other modules still using FakeApiService (Cart, Course, Lesson, Review)
- ⚠️ AsyncApiHelper not created yet (required before RemoteApi integration)
- 🎯 **Next Step**: Create AsyncApiHelper + implement RemoteApiServices for other modules

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
- Database: PostgreSQL (connection via environment variable DATABASE_URL)
- Port: 3000
- JWT Secret: Configured via environment variables
- Image Upload: Using multer middleware for course avatars
- CORS: Enabled for cross-origin requests

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

**Admin Module** ([feature/admin/](app/src/main/java/com/example/projectonlinecourseeducation/feature/admin/)):
- Main: `AdminHomeActivity` with BottomNavigationView (6 tabs)
- Key Features:
  - **Statistics Dashboard**: Course/Student/Teacher analytics with ViewPager2 + TabLayout
  - **User Management**: Student and Teacher detailed views (ViewPager2 tabs for Students/Teachers)
  - **Course Management**: View all courses with comprehensive details
    - Lesson list with video player and progress tracking
    - Review management with rating display
    - Student enrollment list with progress details
    - Long-press delete for courses, lessons, and reviews
  - **Course Approval**: Review and approve teacher-submitted courses (in development)
  - **Notifications**: System notifications (placeholder)
  - **Profile**: Admin profile management
- Key Activities:
  - `AdminManageCourseDetailActivity` - Full course details with lessons, reviews, enrolled students
  - `AdminLessonDetailActivity` - Lesson video player with student progress tracking
  - `AdminManageUserStudentDetailActivity` - Student profile with purchased/cart courses
  - `AdminManageUserTeacherDetailActivity` - Teacher profile with owned courses statistics
- Specialized Adapters: 15+ adapters for admin views (courses, lessons, users, stats, progress)

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
- ✅ `updateCurrentUserProfile()` → PUT /auth/profile
- ✅ `changeCurrentUserPassword()` → POST /auth/change-password
- ✅ `getCurrentUser()` → GET /auth/me

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

### Course Approval System (Three-Level Approval)

The app implements a comprehensive approval workflow for courses:

**Model Fields:**
- `isInitialApproved` (boolean) - Phê duyệt khởi tạo
- `isEditApproved` (boolean) - Phê duyệt chỉnh sửa
- `isDeleteRequested` (boolean) - Yêu cầu xóa đang chờ duyệt (soft delete)

**Workflow:**

1. **Teacher Creates New Course:**
   - Both approval fields set to `false` by `CourseFakeApiService.createCourse()`
   - Course NOT visible on Student home (filtered by `isInitialApproved`)
   - Admin sees "Chờ duyệt khởi tạo" tag
   - Admin must approve → both fields become `true`
   - Course now visible on Student home

2. **Teacher Edits Existing Course (Pending Changes System):**
   - Teacher submits edits → `CourseFakeApiService.updateCourse()` is called
   - **Pending changes saved to `pendingCourseEdits` Map** (clone of edited course)
   - **Original course UNCHANGED** - Student continues seeing old version
   - `isEditApproved` set to `false` on original course
   - Admin sees "Chờ duyệt chỉnh sửa" tag
   - Admin can view both versions:
     - Original via `getCourseDetail(id)`
     - Pending via `getPendingEdit(id)`
   - Admin approves → `approveCourseEdit()` applies pending changes to original
   - Admin rejects → `rejectCourseEdit()` discards pending changes
   - After approval: `isEditApproved = true`, changes visible to everyone

3. **Teacher Deletes Course (Soft Delete):**
   - `CourseFakeApiService.deleteCourse()` sets `isDeleteRequested = true`
   - Course hidden from Student home (filtered by `isDeleteRequested`)
   - Course still visible to Teacher and Admin with "Chờ duyệt xóa" tag
   - Admin can:
     - Approve delete → call `permanentlyDeleteCourse()` → course removed permanently
     - Reject delete → call `cancelDeleteRequest()` → course returns to normal

**Benefits:**
- Prevents inappropriate content from going live
- Allows continuous availability (edit approval doesn't hide course)
- Soft delete prevents accidental permanent deletion
- Clear status tracking via tags

**UI Implementation:**
- Teacher: Orange tag showing "Chờ duyệt khởi tạo", "Chờ duyệt chỉnh sửa", or "Chờ duyệt xóa" in [HomeCourseAdapter](app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/adapter/HomeCourseAdapter.java)
- Admin: Same tags in [AdminCourseAdapter](app/src/main/java/com/example/projectonlinecourseeducation/feature/admin/adapter/AdminCourseAdapter.java)
- Student: Only sees approved, non-deleted courses (filtered in `filterSearchSort()`)

**Helper Methods in Course Model:**
- `isPendingApproval()` - Returns true if awaiting any approval (create, edit, or delete)
- `getApprovalStatusText()` - Returns status string for UI tags
- `isDeleteRequested()` - Check if course is pending delete approval

**API Methods in CourseFakeApiService:**

*Course Edit Approval:*
- `updateCourse(id, course)` - Saves changes to pending, doesn't modify original
- `getPendingEdit(id)` - Get pending version for review (admin/teacher)
- `hasPendingEdit(id)` - Check if course has pending changes
- `approveCourseEdit(id)` - Apply pending changes to original (admin)
- `rejectCourseEdit(id)` - Discard pending changes (admin)

*Course Delete Approval:*
- `deleteCourse(id)` - Soft delete, sets `isDeleteRequested = true`
- `permanentlyDeleteCourse(id)` - Hard delete, removes from database (admin only)
- `cancelDeleteRequest(id)` - Reject delete request, restores course (admin only)

*Helper Methods:*
- `cloneCourse(course)` - Deep copy of course object
- Internal `pendingCourseEdits` Map stores all pending versions

**Admin Approval Page:**
- Currently placeholder in [AdminCourseApprovalFragment](app/src/main/java/com/example/projectonlinecourseeducation/feature/admin/fragment/AdminCourseApprovalFragment.java)
- To be implemented with three action types:

1. **Create Approval** (course with `isInitialApproved = false`):
   ```java
   course.setInitialApproved(true);
   course.setEditApproved(true);
   ```

2. **Edit Approval** (course with `isEditApproved = false` and pending changes):
   ```java
   // View both versions:
   Course original = ApiProvider.getCourseApi().getCourseDetail(courseId);
   Course pending = CourseFakeApiService.getInstance().getPendingEdit(courseId);

   // Approve or Reject:
   CourseFakeApiService.getInstance().approveCourseEdit(courseId);  // Approve
   CourseFakeApiService.getInstance().rejectCourseEdit(courseId);   // Reject
   ```

3. **Delete Approval** (course with `isDeleteRequested = true`):
   ```java
   // Approve delete:
   CourseFakeApiService.getInstance().permanentlyDeleteCourse(courseId);

   // Reject delete:
   CourseFakeApiService.getInstance().cancelDeleteRequest(courseId);
   ```

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
    └── admin/
    ├── activity/       # Home, CourseDetail, LessonDetail, UserDetails
    ├── adapter/        # 15+ adapters (Course, Lesson, User, Stats, Progress)
    └── fragment/       # 10 fragments (Statistics, UserManagement, CourseManagement, etc.)
```

## Backend Integration Roadmap

**Current Integration Status:**
```
Frontend Modules:          Backend APIs:           RemoteApiServices:
┌────────────────┐         ┌──────────────┐        ┌──────────────────┐
│ Student ✅     │ ───┐    │ Auth ✅      │ ────── │ Auth ✅          │
│ Teacher ✅     │    │    │ Course ✅    │ ─────┐ │ Course ⏳        │
│ Admin ✅       │    └──→ │ Cart ✅      │ ─────┤ │ Cart ⏳          │
└────────────────┘         │ Lesson ⏳    │ ─────┤ │ Lesson ⏳        │
                           │ Review ⏳    │ ─────┤ │ Review ⏳        │
                           └──────────────┘      └ │ MyCourse ⏳      │
                                                   └──────────────────┘
Legend:
✅ Complete    ⏳ TODO    🟢 Backend Ready    ⚠️ Critical Next Step
```

**Critical Blocker:**
- ⚠️ AsyncApiHelper must be created BEFORE RemoteApiServices can be safely used
- Without it: Network calls on main thread → ANR crash after 5 seconds
- With it: Network calls on background thread → smooth operation

---

### Phase 1: Auth Module ✅ (Complete)
- ✅ AuthRemoteApiService implemented
- ✅ Retrofit + OkHttp configured
- ✅ JWT token management (SessionManager)
- ✅ Login, Register, Password Reset endpoints
- ✅ Profile update and password change endpoints

### Phase 2: Backend Endpoints Implemented 🟢 (Backend Ready)
Backend server has the following endpoints ready for integration:
- ✅ **Auth**: POST /login, /signup, /forgot-password-request, /forgot-password-update
- ✅ **Auth Protected**: GET /auth/me, PUT /auth/profile, POST /auth/change-password
- ✅ **Course**: POST /course, GET /course, GET /course/:id, DELETE /course/:id
- ✅ **Cart**: GET /cart/:userId, POST /cart/add, POST /cart/remove, POST /cart/checkout
- ✅ **Course Status**: GET /course/:userId/:courseId/status, POST /course/:id/purchase

### Phase 3: Admin Module ✅ (Frontend Complete)
- ✅ AdminHomeActivity with 6 navigation tabs
- ✅ Statistics Dashboard (Course/Student/Teacher stats with charts)
- ✅ User Management (Student/Teacher detailed views)
- ✅ Course Management (view courses, lessons, reviews, enrolled students)
- ✅ Lesson details with video player and progress tracking
- ✅ 15+ specialized adapters for admin views

### Phase 4: Create RemoteApiServices (Next Priority)
Frontend activities are still using FakeApiService. Need to create RemoteApiService implementations:
- ⏳ Implement CourseRemoteApiService (backend endpoints ready)
- ⏳ Implement CartRemoteApiService (backend endpoints ready)
- ⏳ Implement MyCourseRemoteApiService
- ⏳ Implement LessonRemoteApiService
- ⏳ Implement LessonProgressRemoteApiService
- ⏳ Implement ReviewRemoteApiService

### Phase 5: ANR Prevention (Critical Before RemoteApi Integration)
- ⚠️ **MUST DO FIRST**: Create AsyncApiHelper utility class
- ⏳ Wrap API calls in ~11 Activity/Fragment files with AsyncApiHelper
- ⏳ Test with RemoteApiService to ensure no ANR crashes

### Phase 6: RemoteApi Integration Testing
- ⏳ Swap FakeApiService to RemoteApiService via ApiProvider
- ⏳ Test all features: Student, Teacher, Admin modules
- ⏳ Verify same behavior as FakeApiService (no lag, no crashes)
- ⏳ Handle Chapter → Lesson mapping (backend has chapters, app doesn't)

**Integration Pattern for Each Module:**
1. Create `XxxRemoteApiService implements XxxApi`
2. Create request/response DTOs in `remote/` package
3. Create Retrofit interface
4. Update Activities/Fragments with `AsyncApiHelper`
5. Test with `ApiProvider.setXxxApi(new XxxRemoteApiService())`

## Summary: Key Points for University Project

**What Has Been Accomplished (December 2024):**
1. ✅ **All Frontend Modules Complete**:
   - Student module: 5 tabs, course browsing, cart, video player, progress tracking
   - Teacher module: 4 tabs, course/lesson CRUD with staged changes pattern
   - Admin module: 6 tabs, statistics dashboard, user management, course management with full details
2. ✅ **Backend Server Functional**:
   - Auth endpoints (login, register, password reset, profile)
   - Course endpoints (CRUD operations)
   - Cart endpoints (add, remove, checkout, status)
   - PostgreSQL database with proper schema
3. ✅ **First RemoteApi Integration**:
   - AuthRemoteApiService implemented and working
   - RetrofitClient with JWT token management
   - SessionManager for user persistence
4. ✅ **FakeApiService Strategy**:
   - Enabled parallel FE/BE development
   - All features tested and working with in-memory data
   - Interface-based design ready for RemoteApi swap

**What Still Needs to Be Done:**
1. ⚠️ **CRITICAL - Create AsyncApiHelper** (1 file):
   - Required to prevent ANR crashes when using RemoteApiService
   - Simple utility class using ExecutorService + Handler
   - Must be created BEFORE integrating other RemoteApiServices
2. ⏳ **Implement RemoteApiServices** (5 services):
   - CourseRemoteApiService (backend endpoints ready ✅)
   - CartRemoteApiService (backend endpoints ready ✅)
   - MyCourseRemoteApiService
   - LessonRemoteApiService
   - ReviewRemoteApiService
3. ⏳ **Wrap API Calls in Activities** (~11 files):
   - Update Student, Teacher, Admin activities to use AsyncApiHelper
   - Replace direct API calls with background thread execution
4. ⏳ **Integration Testing**:
   - Swap FakeApi → RemoteApi via ApiProvider
   - Verify app runs smoothly with backend (no lag, no crashes)
   - Test all features: browsing, cart, purchase, video playback
5. ⏳ **Handle Backend Differences**:
   - Backend has Chapter model (app doesn't) - needs mapping
   - Category system may need synchronization

**Integration Approach:**
- Create `AsyncApiHelper` utility class (prevents ANR)
- Wrap API calls in ~11 Activity/Fragment files
- Initialize `RetrofitClient` in MainActivity
- Create RemoteApiService implementations
- Swap `ApiProvider.setXxxApi()` when ready to test

**Expected Result:**
- App runs smoothly with real backend
- No crashes from network operations
- Same logic and features as FakeApiService
- Demonstrates full-stack mobile app development for university project

**NOT Included (Beyond Project Scope):**
- MVVM architecture refactoring
- Comprehensive unit testing
- Offline caching with Room database
- Production-level security hardening
- Enterprise scalability patterns

This approach balances academic requirements (demonstrating full-stack integration) with practical development (quick iteration, working demo). The foundation is solid - just need to complete the RemoteApi integration layer.
