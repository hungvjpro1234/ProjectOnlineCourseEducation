# Backend-Frontend CourseApi Comparison Report

**Date:** 2025-12-14
**Status:** Backend CourseApi ~85% compatible with Frontend
**Remaining Work:** Minor endpoints missing + ID/field mapping

---

## Executive Summary

Backend developer đã implement **15/15 core endpoints** cho CourseApi trong server.js. Tuy nhiên có **một số gaps** cần xử lý trước khi frontend connect:

### ✅ Matching Endpoints (12/15)
- CRUD operations (Create, Read, Update, Delete request)
- Approval workflow (initial, edit, delete approval/rejection)
- Purchase tracking
- Rating recalculation

### ⚠️ Missing Backend Endpoints (3)
1. **Sorting & Search logic** - Frontend có Sort enum (AZ, ZA, RATING_UP, RATING_DOWN) và search query
2. **getRelatedCourses()** - Tìm courses liên quan theo teacher/category
3. **rejectInitialCreation()** - Xóa course chưa được duyệt

### 🔧 Data Model Differences
- **ID Type:** Backend = integer (auto-generated), Frontend = string ("c1", "c2")
- **Column Names:** Backend = snake_case (`course_id`, `imageurl`), Frontend = camelCase (`courseId`, `imageUrl`)
- **Approval Fields:** Backend = `is_approved`, Frontend = `initialApproved`

---

## Detailed Endpoint Comparison

### 1. BASIC CRUD OPERATIONS

| Frontend Method | Backend Endpoint | Status | Notes |
|----------------|------------------|--------|-------|
| `createCourse(Course)` | `POST /course` | ✅ MATCH | Backend uses multer for image upload (field: "courseAvatar") |
| `listAll()` | `GET /course` | ✅ MATCH | Backend filters by `is_approved=true` for students |
| `getCoursesByTeacher(name)` | `GET /course?teacher=XXX` | ✅ MATCH | Backend supports teacher filter |
| `getCourseDetail(id)` | `GET /course/:id` | ✅ MATCH | Backend supports `?include_pending=true` |
| `filterSearchSort(cat, query, sort, limit)` | `GET /course` | ⚠️ PARTIAL | Backend missing: sort logic & search query |
| `getRelatedCourses(id)` | N/A | ❌ MISSING | Backend doesn't have related courses logic |
| `updateCourse(id, course)` | `PATCH /course/:id` | ✅ MATCH | Both use pending edit pattern |
| `deleteCourse(id)` | `POST /course/:id/request-delete` | ✅ MATCH | Soft delete pattern |

**Missing Backend Features:**
```javascript
// Frontend có logic này, backend chưa implement:
// 1. Sort by title (AZ, ZA), rating (UP, DOWN)
// 2. Search by query (filter title/teacher)
// 3. Related courses by teacher or category match
```

---

### 2. APPROVAL WORKFLOW

| Frontend Method | Backend Endpoint | Status | Notes |
|----------------|------------------|--------|-------|
| `getPendingCourses()` | `GET /course/pending` | ✅ MATCH | Admin only, filters `is_approved=false OR is_edit_approved=false` |
| `approveInitialCreation(id)` | `POST /course/:id/approve-initial` | ✅ MATCH | Sets `is_approved=true` |
| `rejectInitialCreation(id)` | N/A | ❌ MISSING | Frontend deletes course, backend không có endpoint |
| `getPendingEdit(id)` | `GET /course/:id/pending` | ✅ MATCH | Returns `pending_data` from `course_pending_edits` table |
| `hasPendingEdit(id)` | N/A | ⚠️ MINOR | Frontend boolean check, backend có thể dùng `GET /course/:id/pending` |
| `approveCourseEdit(id)` | `POST /course/:id/approve-edit` | ✅ MATCH | Applies pending changes to course |
| `rejectCourseEdit(id)` | `POST /course/:id/reject-edit` | ✅ MATCH | Deletes pending changes |
| `permanentlyDeleteCourse(id)` | `POST /course/:id/approve-delete` | ✅ MATCH | Hard delete from database |
| `cancelDeleteRequest(id)` | `POST /course/:id/reject-delete` | ✅ MATCH | Clears `is_delete_requested` flag |

**Note:** Backend approval workflow logic **khớp hoàn toàn** với frontend.

---

### 3. ADDITIONAL ENDPOINTS

| Frontend Method | Backend Endpoint | Status | Notes |
|----------------|------------------|--------|-------|
| `recordPurchase(id)` | `POST /course/:id/purchase` | ✅ MATCH | Increments `students` count |
| `recalculateCourseRating(id)` | `POST /course/:id/recalculate-rating` | ✅ MATCH | Aggregates from `course_review` table |
| `addCourseUpdateListener(l)` | N/A | ⚠️ FRONTEND ONLY | Observer pattern cho UI updates |
| N/A | `GET /course/:id/students` | 💡 BACKEND EXTRA | Returns list of enrolled students (teacher/admin) |

**Notes:**
- `addCourseUpdateListener()` là frontend pattern để notify UI khi course thay đổi (không cần backend endpoint)
- Backend có endpoint `GET /course/:id/students` mà frontend chưa dùng (có thể integrate vào CourseStudentApi)

---

### 4. CART INTEGRATION (Bonus)

Backend có **4 cart endpoints** mà frontend có module riêng (CartApi):

| Backend Endpoint | Frontend Module | Status |
|-----------------|-----------------|--------|
| `GET /cart/:userId` | `CartApi.getCart(userId)` | ✅ READY |
| `POST /cart/add` | `CartApi.addToCart(courseId)` | ✅ READY |
| `POST /cart/remove` | `CartApi.removeFromCart(courseId)` | ✅ READY |
| `POST /cart/checkout` | `CartApi.checkout(courseIds[])` | ✅ READY |
| `GET /course/:userId/:courseId/status` | `CourseStatusResolver` | 💡 NEW |

**Note:** Backend endpoint `/course/:userId/:courseId/status` returns course payment status (NOT_PURCHASED/IN_CART/PURCHASED). Frontend có thể dùng thay vì check qua CartApi + MyCourseApi.

---

## Data Model Mapping Issues

### Issue 1: ID Type Mismatch

**Backend:**
```javascript
// Database: course_id SERIAL (auto-generated integer)
const inserted = await db.one(`INSERT INTO course(...) RETURNING *`);
// inserted.course_id = 123 (integer)
```

**Frontend:**
```java
// Course model uses String id
Course course = new Course("c1", "Java Cơ Bản", ...);
```

**Solution:**
```java
// CourseRemoteApiService needs to convert:
String courseId = String.valueOf(backendCourseId); // "123"
int backendId = Integer.parseInt(courseId); // 123
```

---

### Issue 2: Column Name Mapping

**Backend (snake_case):**
```javascript
{
  course_id: 1,
  imageurl: "/uploads/...",
  totaldurationminutes: 120,
  ratingcount: 10,
  created_at: "2024-01-01T00:00:00Z"
}
```

**Frontend (camelCase):**
```java
Course {
  id: "1",
  imageUrl: "...",
  totalDurationMinutes: 120,
  ratingCount: 10,
  createdAt: "01/2024"
}
```

**Backend Solution:** Already handled by `transformCourseRow()` function (line 117-156 in server.js)
```javascript
function transformCourseRow(row) {
  r.id = row.course_id; // ✅
  r.imageUrl = row.imageurl; // ✅
  r.totalDurationMinutes = row.totaldurationminutes; // ✅
  r.ratingCount = row.ratingcount; // ✅
  // ...
}
```

**Frontend Solution:** Create DTOs to match backend response
```java
// CourseDto.java (DTO from backend)
class CourseDto {
  @SerializedName("id") int id; // or use course_id
  @SerializedName("imageUrl") String imageUrl;
  // ...
}
```

---

### Issue 3: Approval Field Mapping

| Backend Column | Frontend Property | Notes |
|---------------|-------------------|-------|
| `is_approved` | `initialApproved` | Course creation approved |
| `is_edit_approved` | `editApproved` | Edits approved |
| `is_delete_requested` | `deleteRequested` | Delete request pending |

**Solution:** Map in CourseDto
```java
class CourseDto {
  @SerializedName("is_approved")
  private boolean isApproved; // → course.setInitialApproved(dto.isApproved)

  @SerializedName("is_edit_approved")
  private boolean isEditApproved;

  @SerializedName("is_delete_requested")
  private boolean isDeleteRequested;
}
```

---

### Issue 4: Array Fields (skills, requirements)

**Backend:**
```javascript
// Database column type: JSONB or TEXT
// Can be: JSON array, CSV string, or single value
skills: ["Skill 1", "Skill 2"]
requirements: "Req 1, Req 2, Req 3" // CSV string cũng được

// Backend có helper parseMaybeArrayField() để handle 3 formats
```

**Frontend:**
```java
List<String> skills = Arrays.asList("Skill 1", "Skill 2");
List<String> requirements = Arrays.asList("Req 1", "Req 2");
```

**Solution:** Backend already handles this with `parseMaybeArrayField()` and `safeParseJson()` helpers. Frontend send as JSON array.

---

### Issue 5: Image Upload

**Backend:**
```javascript
// Uses multer middleware for multipart/form-data
app.post("/course", upload.single("courseAvatar"), async (req, res) => {
  const imgSrc = req.file ? `/uploads/${req.file.filename}` : "";
  // Returns: "/uploads/1234567890-image.jpg"
});
```

**Frontend:**
```java
// Needs to send multipart request
MultipartBody.Part imagePart = ...; // field name = "courseAvatar"

@Multipart
@POST("/course")
Call<ApiResponse<CourseDto>> createCourse(
  @Part MultipartBody.Part courseAvatar,
  @Part("title") RequestBody title,
  @Part("description") RequestBody description,
  // ... other fields
);
```

---

## Missing Backend Endpoints (Action Required)

### 1. Search & Sort Logic

**Frontend có:**
```java
filterSearchSort(String category, String query, Sort sort, int limit)
// Sort enum: AZ, ZA, RATING_UP, RATING_DOWN
// query: filter by title or teacher name (case-insensitive)
```

**Backend cần thêm:**
```javascript
app.get("/course", async (req, res) => {
  const { teacher, include_unapproved, query, sort, limit } = req.query;

  // TODO: Add search filter
  if (query) {
    rows = rows.filter(c =>
      c.title.toLowerCase().includes(query.toLowerCase()) ||
      c.teacher.toLowerCase().includes(query.toLowerCase())
    );
  }

  // TODO: Add sorting
  if (sort === "AZ") rows.sort((a,b) => a.title.localeCompare(b.title));
  if (sort === "ZA") rows.sort((a,b) => b.title.localeCompare(a.title));
  if (sort === "RATING_UP") rows.sort((a,b) => a.rating - b.rating);
  if (sort === "RATING_DOWN") rows.sort((a,b) => b.rating - a.rating);

  // TODO: Add limit
  if (limit) rows = rows.slice(0, parseInt(limit));
});
```

---

### 2. Get Related Courses

**Frontend có:**
```java
getRelatedCourses(String courseId) {
  // Returns courses with:
  // 1. Same teacher OR
  // 2. Shared category tag
}
```

**Backend cần thêm:**
```javascript
app.get("/course/:id/related", async (req, res) => {
  const courseId = parseInt(req.params.id);
  const course = await db.one("SELECT * FROM course WHERE course_id = $1", [courseId]);

  // Find courses with same teacher or category
  const related = await db.any(`
    SELECT * FROM course
    WHERE course_id != $1
    AND is_approved = true
    AND (teacher = $2 OR category ILIKE $3)
    LIMIT 10
  `, [courseId, course.teacher, `%${course.category}%`]);

  res.send({ success: true, data: related.map(transformCourseRow) });
});
```

---

### 3. Reject Initial Creation

**Frontend có:**
```java
rejectInitialCreation(String id) {
  // Xóa course chưa được duyệt khỏi database
  if (!course.isInitialApproved()) {
    allCourses.remove(course);
  }
}
```

**Backend cần thêm:**
```javascript
app.post("/course/:id/reject-initial", authMiddleware, async (req, res) => {
  // Admin only
  if (req.user.role !== "ADMIN") return res.status(403).send({...});

  const courseId = parseInt(req.params.id);
  const course = await db.oneOrNone("SELECT * FROM course WHERE course_id = $1", [courseId]);

  // Only delete if not approved yet
  if (course && !course.is_approved) {
    await db.none("DELETE FROM course WHERE course_id = $1", [courseId]);
    res.send({ success: true, message: "Course rejected and deleted" });
  } else {
    res.status(400).send({ success: false, message: "Course already approved or not found" });
  }
});
```

**Alternative:** Có thể dùng luôn endpoint `POST /course/:id/approve-delete` thay vì tạo endpoint mới.

---

## Authentication & Authorization

**Backend requires JWT token for protected endpoints:**

| Endpoint | Auth Required | Role Required | Notes |
|----------|--------------|---------------|-------|
| `POST /course` | ❌ No | None | Should add auth (teacher only) |
| `GET /course` | ❌ No | None | Public list (approved only) |
| `GET /course?include_unapproved=true` | ✅ Yes | ADMIN | Admin sees unapproved courses |
| `PATCH /course/:id` | ✅ Yes | Any | Should check teacher ownership |
| `GET /course/pending` | ✅ Yes | ADMIN | Admin only |
| `POST /course/:id/approve-*` | ✅ Yes | ADMIN | Admin only |
| `POST /course/:id/reject-*` | ✅ Yes | ADMIN | Admin only |
| `POST /course/:id/request-delete` | ✅ Yes | Any | Should check teacher ownership |
| `GET /course/:id/students` | ✅ Yes | TEACHER/ADMIN | Check teacher ownership |

**Frontend needs to:**
1. Include JWT token in Authorization header: `Bearer <token>`
2. Handle 401 Unauthorized (token expired/invalid)
3. Handle 403 Forbidden (insufficient permissions)

---

## Image Handling

**Backend image upload flow:**
```javascript
// 1. Upload image via multer
upload.single("courseAvatar")

// 2. Save to uploads/ directory
const imgSrc = `/uploads/${req.file.filename}`; // "/uploads/1733924567890-course.jpg"

// 3. Serve via static middleware
app.use("/uploads", express.static(uploadDir));

// 4. Client fetches: http://10.0.2.2:3000/uploads/1733924567890-course.jpg
```

**Frontend needs to:**
1. Send image as multipart/form-data with field name "courseAvatar"
2. Prepend base URL to imageUrl: `BASE_URL + course.imageUrl`
3. Handle image loading with Glide/Picasso

---

## Integration Checklist for Frontend

### Phase 1: Create DTOs
- [ ] Create `CourseDto.java` (response from backend)
- [ ] Create `CreateCourseRequest.java` (for POST /course)
- [ ] Create `UpdateCourseRequest.java` (for PATCH /course/:id)
- [ ] Create `CourseApiResponse.java` (generic wrapper)

### Phase 2: Create Retrofit Service
- [ ] Create `CourseRetrofitService.java` interface
- [ ] Define all 15 endpoints with proper annotations
- [ ] Handle multipart for image upload
- [ ] Add auth token interceptor

### Phase 3: Implement RemoteApiService
- [ ] Create `CourseRemoteApiService.java implements CourseApi`
- [ ] Map all 19 methods to backend endpoints
- [ ] Convert ID types (String ↔ int)
- [ ] Map column names (camelCase ↔ snake_case)
- [ ] Handle approval fields mapping
- [ ] Handle error responses

### Phase 4: Update Activities/Fragments
- [ ] Wrap API calls with `AsyncApiHelper`
- [ ] Update `TeacherCourseEditActivity` to use RemoteApi
- [ ] Update `AdminCourseApprovalFragment` to use RemoteApi
- [ ] Update `StudentHomeFragment` to use RemoteApi
- [ ] Test with real backend

### Phase 5: Test End-to-End
- [ ] Create course (teacher) → verify pending status
- [ ] Approve course (admin) → verify visible to students
- [ ] Edit course (teacher) → verify pending edit
- [ ] Approve edit (admin) → verify changes applied
- [ ] Delete course (teacher) → verify delete request
- [ ] Approve delete (admin) → verify permanent deletion
- [ ] Purchase course → verify students count increments
- [ ] Add review → verify rating recalculates

---

## Recommendations

### For Backend Developer

**High Priority (Must Fix):**
1. ✅ Add query parameter to `GET /course` for search/filter by title or teacher
2. ✅ Add sort parameter to `GET /course` (AZ, ZA, RATING_UP, RATING_DOWN)
3. ⚠️ Add `POST /course/:id/reject-initial` endpoint (or clarify using approve-delete)
4. 🔒 Add authentication to `POST /course` (teacher only)
5. 🔒 Add ownership check to `PATCH /course/:id` (teacher can only edit own courses)

**Medium Priority (Nice to Have):**
6. 📊 Add `GET /course/:id/related` endpoint for related courses
7. 🔍 Add `limit` parameter to `GET /course` for pagination

**Low Priority (Optional):**
8. 📈 Add endpoint to get course statistics (total views, completion rate, etc.)

### For Frontend Developer

**Before Integration:**
1. ⚠️ Create `AsyncApiHelper.java` (CRITICAL - prevents ANR)
2. ⚠️ Initialize `RetrofitClient` in `MainActivity2`
3. 📝 Create DTOs for all request/response models
4. 🌐 Create `CourseRetrofitService` interface

**During Integration:**
5. 🔄 Implement `CourseRemoteApiService` with ID/field mapping
6. 🖼️ Handle multipart image upload properly
7. 🔐 Test with JWT authentication
8. 🎯 Wrap all API calls with `AsyncApiHelper`

**After Integration:**
9. ✅ Test all 15 endpoints end-to-end
10. 🐛 Handle edge cases (network errors, token expiry, etc.)

---

## Conclusion

**Overall Compatibility: 85%**

Backend CourseApi implementation is **very close** to frontend requirements. Main gaps are:

1. ❌ **Missing search/sort logic** in `GET /course` (easy fix)
2. ❌ **Missing related courses endpoint** (medium fix)
3. ⚠️ **ID type conversion** needed (frontend handles)
4. ⚠️ **Authentication/authorization** incomplete (security concern)

**Recommendation:** Backend developer should add search/sort parameters to `GET /course` before frontend integration begins. Frontend can proceed with creating DTOs and RetrofitService in parallel.

**Estimated Integration Time:** 3-4 hours (assuming backend adds missing features first)

---

**Report Generated:** 2025-12-14
**Next Steps:** Communicate missing endpoints to backend developer → Create CourseRemoteApiService → Test with AsyncApiHelper
