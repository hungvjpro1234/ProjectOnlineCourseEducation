# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an **Android Online Learning Platform** with a Node.js backend. The app supports three user roles: Student, Teacher, and Admin. Currently using **FakeApiService** (in-memory data) for development, with a real backend available but not yet integrated.

**Critical Note:** The app is currently in **demo/development stage**. All data is stored in-memory and does not persist between app restarts. The backend exists but is not connected to the Android app.

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
│   Repository Pattern via Interfaces     │
│   ApiProvider → FakeApiService          │
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
  ApiProvider.setAuthApi(...)   // Swap implementation
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
  - Student: `student1@example.com` / `Pass123`
  - Teacher: `teacher@example.com` / `teacher`
  - Admin: `admin@example.com` / `Admin123`

**Student Module** ([feature/student/](app/src/main/java/com/example/projectonlinecourseeducation/feature/student/)):
- Main: `StudentHomeActivity` with BottomNavigationView (5 tabs)
- Fragments: Home, Cart, MyCourse, Notification, User
- Key activities:
  - `StudentCourseProductDetailActivity` - Course details with state-aware buttons
  - `StudentCourseLessonActivity` - Lesson list
  - `StudentLessonVideoActivity` - YouTube video player with progress tracking

**Teacher Module** ([feature/teacher/](app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/)):
- Main: `TeacherHomeActivity` with BottomNavigationView (4 tabs)
- Course management: Create, Edit, Delete
- Lesson management with **staged changes pattern**:
  - Changes are local until "Save" is clicked
  - Prevents accidental data loss
  - See [TeacherCourseEditActivity.java:720](app/src/main/java/com/example/projectonlinecourseeducation/feature/teacher/activity/TeacherCourseEditActivity.java)

**Course Status Resolution:**
- Uses `CourseStatusResolver` ([core/utils/CourseStatusResolver.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/CourseStatusResolver.java))
- Three states: PURCHASED, IN_CART, NOT_PURCHASED
- Determines button states in course detail views

### YouTube Integration

Uses `pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0`
- Video ID extraction via `YouTubeUtils` ([core/utils/YouTubeUtils.java](app/src/main/java/com/example/projectonlinecourseeducation/core/utils/YouTubeUtils.java))
- Auto-duration calculation in `LessonFakeApiService` using `VideoDurationHelper`
- Observer pattern via `LessonUpdateListener` notifies UI when duration is calculated

## Important Development Notes

### Current State: FakeApiService Only

**All data is in-memory and non-persistent.** The app uses FakeApiService implementations:
- Data resets on app restart
- No actual network calls
- Synchronous operations (instant response)

**When integrating the real backend:**
1. Implement `*RemoteApiService` classes using Retrofit
2. Add OkHttp for HTTP client
3. Add proper error handling and loading states
4. Use `ApiProvider.setXxxApi(new RemoteApiService(...))` to swap implementations
5. Handle async operations with callbacks/coroutines

### Category System

31+ predefined categories in `CourseFakeApiService.FIXED_CATEGORIES`:
```java
"Java", "JavaScript", "Python", "C++", "C#", "Go", "Ruby", ...
```

Categories are **not** fetched from backend. When integrating backend:
- Backend uses different structure (may need mapping)
- Categories should be dynamic from API

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
- Consider replacing with Glide/Coil for production

### Price Formatting

Backend uses numeric prices, app expects formatted strings like "1.499.000 VND"

### Testing

Currently **zero test coverage**. Test infrastructure exists:
- JUnit 4.13.2 for unit tests
- Espresso 3.7.0 for UI tests

When adding tests:
- API interfaces are already testable (can mock)
- Consider adding ViewModel layer first for easier testing

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

## File Structure Reference

```
app/src/main/java/com/example/projectonlinecourseeducation/
├── core/
│   ├── model/          # POJOs: Course, Lesson, User, Review, etc.
│   └── utils/          # ImageLoader, YouTubeUtils, DialogHelpers
├── data/
│   ├── ApiProvider.java
│   ├── auth/           # AuthApi + AuthFakeApiService
│   ├── course/         # CourseApi + CourseFakeApiService
│   ├── lesson/         # LessonApi + LessonProgressApi + implementations
│   ├── cart/           # CartApi + CartFakeApiService
│   ├── mycourse/       # MyCourseApi + MyCourseFakeApiService
│   └── review/         # ReviewApi + ReviewFakeApiService
└── feature/
    ├── auth/activity/      # Login, Register, ForgotPassword, MainActivity2
    ├── student/
    │   ├── activity/       # 6 activities for student features
    │   ├── adapter/        # 8 RecyclerView adapters
    │   └── fragment/       # 5 fragments (Home, Cart, MyCourse, User, Notification)
    ├── teacher/
    │   ├── activity/       # Home, CourseCreate, CourseEdit
    │   ├── adapter/        # Course and lesson adapters
    │   └── fragment/       # 4 fragments
    └── admin/              # Placeholder (incomplete)
```

## Known Issues and Limitations

1. **No persistence** - All data lost on app restart
2. **No authentication tokens** - Session only in memory
3. **Plaintext passwords** - No hashing (demo only)
4. **No offline support** - No Room/SQLite database
5. **No MVVM** - Business logic in Activities/Fragments
6. **No loading states** - FakeApi is synchronous
7. **Backend not integrated** - Retrofit not implemented
8. **Image loading not cached** - Custom loader without optimization
9. **No multi-language support** - Strings hardcoded
10. **Admin module incomplete** - Only placeholder

## Integrating Remote API (Auth Module)

The Auth module now has **both FakeApiService and RemoteApiService** implementations. RemoteApiService is ready to use with the backend.

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

**3. Initialize RetrofitClient in Application or MainActivity:**

```java
public class MainActivity2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RetrofitClient (do this once at app startup)
        RetrofitClient.initialize(this);

        // ... rest of code
    }
}
```

**4. Swap to RemoteApiService:**

```java
// In MainActivity2 or Application class, after RetrofitClient.initialize()
ApiProvider.setAuthApi(new AuthRemoteApiService());
```

Now all authentication calls will use the real backend!

### How It Works

**Architecture:**
```
Activity/Fragment
    ↓ calls
ApiProvider.getAuthApi()
    ↓ returns
AuthRemoteApiService (implements AuthApi)
    ↓ uses
RetrofitClient → AuthRetrofitService
    ↓ HTTP calls
Backend Server (Node.js + PostgreSQL)
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
// Use FakeApiService (in-memory, no network)
ApiProvider.setAuthApi(AuthFakeApiService.getInstance());

// Use RemoteApiService (real backend)
RetrofitClient.initialize(context);
ApiProvider.setAuthApi(new AuthRemoteApiService());
```

### Error Handling

RemoteApiService handles:
- Network errors (IOException) → "Lỗi kết nối mạng..."
- HTTP errors (4xx, 5xx) → Error message from response
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

### File Structure (Remote API)

```
data/
├── auth/
│   ├── AuthApi.java                    # Interface
│   ├── AuthFakeApiService.java         # In-memory implementation
│   ├── AuthRemoteApiService.java       # ✨ NEW: Retrofit implementation
│   ├── ApiResult.java                  # Response wrapper
│   └── remote/                         # ✨ NEW: Remote API models
│       ├── AuthApiResponse.java        # Generic backend response
│       ├── UserDto.java                # User DTO from backend
│       ├── AuthRetrofitService.java    # Retrofit interface
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       ├── ForgotPasswordRequest.java
│       └── ResetPasswordRequest.java
└── network/                            # ✨ NEW: Network layer
    ├── RetrofitClient.java             # Retrofit singleton
    └── SessionManager.java             # JWT token + user storage
```

## Next Steps for Production

To make this production-ready, prioritize:

1. **Complete Backend Integration**
   - ✅ Auth module: AuthRemoteApiService implemented
   - ⏳ Course module: Implement CourseRemoteApiService
   - ⏳ Lesson module: Implement LessonRemoteApiService
   - ⏳ Cart/MyCourse modules: Implement RemoteApiServices
   - ⏳ Review module: Implement ReviewRemoteApiService
   - Note: Follow the same pattern as AuthRemoteApiService

2. **Add Persistence**
   - Room database for offline support
   - Cache courses, cart, purchased courses
   - Token storage (SharedPreferences/DataStore)

3. **Add MVVM Architecture**
   - ViewModels for each screen
   - LiveData/StateFlow for reactive updates
   - Separate business logic from UI

4. **Security**
   - Remove hardcoded credentials
   - Implement proper JWT handling
   - Password hashing (backend already uses plaintext - fix this!)

5. **Testing**
   - Unit tests for business logic
   - UI tests for critical flows
   - Integration tests for API
