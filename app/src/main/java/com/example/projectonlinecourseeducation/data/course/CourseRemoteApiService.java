package com.example.projectonlinecourseeducation.data.course;

import android.util.Log;

import com.example.projectonlinecourseeducation.core.model.course.Course;
import com.example.projectonlinecourseeducation.data.course.remote.CourseApiResponse;
import com.example.projectonlinecourseeducation.data.course.remote.CourseDto;
import com.example.projectonlinecourseeducation.data.course.remote.CourseRetrofitService;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * CourseRemoteApiService - Implementation of CourseApi using Retrofit for real backend calls
 *
 * Usage:
 * 1. Initialize RetrofitClient first: RetrofitClient.initialize(context);
 * 2. Swap in ApiProvider: ApiProvider.setCourseApi(new CourseRemoteApiService());
 * 3. Use normally: ApiProvider.getCourseApi().listAll();
 *
 * IMPORTANT: All methods in this class perform network calls and MUST be wrapped with AsyncApiHelper
 * to avoid ANR (Application Not Responding) crashes.
 */
public class CourseRemoteApiService implements CourseApi {

    private static final String TAG = "CourseRemoteApiService";

    private final CourseRetrofitService retrofitService;

    // Listeners (same as FakeApiService)
    private final List<CourseUpdateListener> courseUpdateListeners = new ArrayList<>();

    public CourseRemoteApiService() {
        this.retrofitService = RetrofitClient.getCourseService();
    }

    // ============ LIST / HOME ============

    @Override
    public List<Course> listAll() {
        try {
            // GET /course (only approved courses for students)
            Response<CourseApiResponse<List<CourseDto>>> response =
                retrofitService.listCourses(null, null).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<List<CourseDto>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return convertDtoListToCourseList(apiResponse.getData());
                } else {
                    Log.w(TAG, "listAll failed: " + apiResponse.getMessage());
                    return new ArrayList<>();
                }
            } else {
                Log.e(TAG, "listAll HTTP error: " + response.code());
                return new ArrayList<>();
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in listAll", e);
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in listAll", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Course> filterSearchSort(String categoryOrAll, String query, Sort sort, int limit) {
        // NOTE: Backend doesn't support query/sort/limit yet
        // Solution: Get all courses from listAll() and filter/sort on client side

        List<Course> allCourses = listAll();
        String cat = categoryOrAll == null ? "All" : categoryOrAll;
        String q = query == null ? "" : query.trim().toLowerCase();

        List<Course> filtered = new ArrayList<>();

        for (Course c : allCourses) {
            // Filter by category
            boolean catOk = cat.equals("All") || hasCategory(c.getCategory(), cat);

            // Filter by search query
            boolean matches = q.isEmpty()
                    || (c.getTitle() != null && c.getTitle().toLowerCase().contains(q))
                    || (c.getTeacher() != null && c.getTeacher().toLowerCase().contains(q));

            if (catOk && matches) {
                filtered.add(c);
            }
        }

        // Sort
        Comparator<Course> comparator;
        switch (sort) {
            case ZA:
                comparator = (a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle());
                break;
            case RATING_UP:
                comparator = (a, b) -> Double.compare(a.getRating(), b.getRating());
                break;
            case RATING_DOWN:
                comparator = (a, b) -> Double.compare(b.getRating(), a.getRating());
                break;
            case AZ:
            default:
                comparator = (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle());
        }
        filtered.sort(comparator);

        // Limit
        if (limit > 0 && filtered.size() > limit) {
            return new ArrayList<>(filtered.subList(0, limit));
        }

        return filtered;
    }

    @Override
    public List<Course> getCoursesByTeacher(String teacherName) {
        if (teacherName == null || teacherName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // GET /course?teacher=XXX
            Response<CourseApiResponse<List<CourseDto>>> response =
                retrofitService.listCourses(teacherName, null).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<List<CourseDto>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return convertDtoListToCourseList(apiResponse.getData());
                } else {
                    Log.w(TAG, "getCoursesByTeacher failed: " + apiResponse.getMessage());
                    return new ArrayList<>();
                }
            } else {
                Log.e(TAG, "getCoursesByTeacher HTTP error: " + response.code());
                return new ArrayList<>();
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in getCoursesByTeacher", e);
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in getCoursesByTeacher", e);
            return new ArrayList<>();
        }
    }

    // ============ DETAIL ============

    @Override
    public Course getCourseDetail(String courseId) {
        try {
            // GET /course/:id
            Response<CourseApiResponse<CourseDto>> response =
                retrofitService.getCourseDetail(courseId, null).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<CourseDto> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return convertDtoToCourse(apiResponse.getData());
                } else {
                    Log.w(TAG, "getCourseDetail failed: " + apiResponse.getMessage());
                    return null;
                }
            } else {
                Log.e(TAG, "getCourseDetail HTTP error: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in getCourseDetail", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in getCourseDetail", e);
            return null;
        }
    }

    @Override
    public List<Course> getRelatedCourses(String courseId) {
        // TODO: Backend doesn't have /course/:id/related endpoint yet
        // Workaround: Get course detail, then filter all courses by same teacher or category

        Course baseCourse = getCourseDetail(courseId);
        if (baseCourse == null) {
            Log.w(TAG, "getRelatedCourses: base course not found");
            return new ArrayList<>();
        }

        List<Course> allCourses = listAll();
        List<Course> related = new ArrayList<>();

        for (Course c : allCourses) {
            if (c.getId().equals(courseId)) continue; // Skip self

            boolean sameTeacher = c.getTeacher() != null
                    && baseCourse.getTeacher() != null
                    && c.getTeacher().equalsIgnoreCase(baseCourse.getTeacher());

            boolean sameCategory = shareCategory(c.getCategory(), baseCourse.getCategory());

            if (sameTeacher || sameCategory) {
                related.add(c);
            }
        }

        Log.i(TAG, "getRelatedCourses: found " + related.size() + " related courses for " + courseId);
        return related;
    }

    // ============ CRUD ============

    @Override
    public Course createCourse(Course newCourse) {
        if (newCourse == null) {
            Log.w(TAG, "createCourse: newCourse is null");
            return null;
        }

        try {
            // Prepare multipart parts
            MultipartBody.Part imagePart = null;
            if (newCourse.getImageUrl() != null && !newCourse.getImageUrl().isEmpty()) {
                // Check if imageUrl is a file path (starts with file:// or /)
                String imagePath = newCourse.getImageUrl();
                if (imagePath.startsWith("file://")) {
                    imagePath = imagePath.substring(7);
                }

                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    RequestBody imageBody = RequestBody.create(
                        MediaType.parse("image/*"),
                        imageFile
                    );
                    imagePart = MultipartBody.Part.createFormData(
                        "courseAvatar",
                        imageFile.getName(),
                        imageBody
                    );
                }
            }

            RequestBody title = createTextPart(newCourse.getTitle());
            RequestBody description = createTextPart(newCourse.getDescription());
            RequestBody teacher = createTextPart(newCourse.getTeacher());
            RequestBody category = createTextPart(newCourse.getCategory());
            RequestBody price = createTextPart(String.valueOf(newCourse.getPrice()));
            RequestBody skills = createJsonArrayPart(newCourse.getSkills());
            RequestBody requirements = createJsonArrayPart(newCourse.getRequirements());

            // POST /course
            Response<CourseApiResponse<CourseDto>> response =
                retrofitService.createCourse(
                    imagePart,
                    title,
                    description,
                    teacher,
                    category,
                    price,
                    skills,
                    requirements
                ).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<CourseDto> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    Course created = convertDtoToCourse(apiResponse.getData());
                    Log.i(TAG, "createCourse success: " + created.getId());

                    // Notify listeners
                    notifyCourseUpdated(created.getId(), created);

                    return created;
                } else {
                    Log.w(TAG, "createCourse failed: " + apiResponse.getMessage());
                    return null;
                }
            } else {
                Log.e(TAG, "createCourse HTTP error: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in createCourse", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in createCourse", e);
            return null;
        }
    }

    @Override
    public Course updateCourse(String id, Course updatedCourse) {
        if (id == null || updatedCourse == null) {
            Log.w(TAG, "updateCourse: invalid params");
            return null;
        }

        try {
            // Prepare multipart parts (same as createCourse)
            MultipartBody.Part imagePart = null;
            if (updatedCourse.getImageUrl() != null && !updatedCourse.getImageUrl().isEmpty()) {
                String imagePath = updatedCourse.getImageUrl();
                if (imagePath.startsWith("file://")) {
                    imagePath = imagePath.substring(7);
                }

                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    RequestBody imageBody = RequestBody.create(
                        MediaType.parse("image/*"),
                        imageFile
                    );
                    imagePart = MultipartBody.Part.createFormData(
                        "courseAvatar",
                        imageFile.getName(),
                        imageBody
                    );
                }
            }

            RequestBody title = createTextPart(updatedCourse.getTitle());
            RequestBody description = createTextPart(updatedCourse.getDescription());
            RequestBody teacher = createTextPart(updatedCourse.getTeacher());
            RequestBody category = createTextPart(updatedCourse.getCategory());
            RequestBody price = createTextPart(String.valueOf(updatedCourse.getPrice()));
            RequestBody skills = createJsonArrayPart(updatedCourse.getSkills());
            RequestBody requirements = createJsonArrayPart(updatedCourse.getRequirements());

            // PATCH /course/:id (creates pending edit)
            Response<CourseApiResponse<Object>> response =
                retrofitService.updateCourse(
                    id,
                    imagePart,
                    title,
                    description,
                    teacher,
                    category,
                    price,
                    skills,
                    requirements
                ).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<Object> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Log.i(TAG, "updateCourse success (pending edit created): " + id);

                    // Backend doesn't return updated course directly (pending edit created)
                    // Get course detail to return current state
                    Course current = getCourseDetail(id);

                    // Notify listeners
                    if (current != null) {
                        notifyCourseUpdated(id, current);
                    }

                    return current;
                } else {
                    Log.w(TAG, "updateCourse failed: " + apiResponse.getMessage());
                    return null;
                }
            } else {
                Log.e(TAG, "updateCourse HTTP error: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in updateCourse", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in updateCourse", e);
            return null;
        }
    }

    @Override
    public boolean deleteCourse(String id) {
        if (id == null) {
            Log.w(TAG, "deleteCourse: id is null");
            return false;
        }

        try {
            // POST /course/:id/request-delete (soft delete)
            Response<CourseApiResponse<Object>> response =
                retrofitService.requestDelete(id).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<Object> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Log.i(TAG, "deleteCourse success (delete requested): " + id);

                    // Get updated course to notify listeners
                    Course updated = getCourseDetail(id);
                    if (updated != null) {
                        notifyCourseUpdated(id, updated);
                    }

                    return true;
                } else {
                    Log.w(TAG, "deleteCourse failed: " + apiResponse.getMessage());
                    return false;
                }
            } else {
                Log.e(TAG, "deleteCourse HTTP error: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in deleteCourse", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in deleteCourse", e);
            return false;
        }
    }

    @Override
    public Course recalculateCourseRating(String courseId) {
        if (courseId == null) {
            Log.w(TAG, "recalculateCourseRating: courseId is null");
            return null;
        }

        try {
            // POST /course/:id/recalculate-rating
            Response<CourseApiResponse<CourseDto>> response =
                retrofitService.recalculateRating(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<CourseDto> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    Course updated = convertDtoToCourse(apiResponse.getData());
                    Log.i(TAG, "recalculateCourseRating success: " + courseId);

                    // Notify listeners
                    notifyCourseUpdated(courseId, updated);

                    return updated;
                } else {
                    Log.w(TAG, "recalculateCourseRating failed: " + apiResponse.getMessage());
                    return null;
                }
            } else {
                Log.e(TAG, "recalculateCourseRating HTTP error: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in recalculateCourseRating", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in recalculateCourseRating", e);
            return null;
        }
    }

    @Override
    public Course recordPurchase(String courseId) {
        if (courseId == null) {
            Log.w(TAG, "recordPurchase: courseId is null");
            return null;
        }

        try {
            // POST /course/:id/purchase
            Response<CourseApiResponse<CourseDto>> response =
                retrofitService.recordPurchase(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<CourseDto> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    Course updated = convertDtoToCourse(apiResponse.getData());
                    Log.i(TAG, "recordPurchase success: " + courseId + ", students=" + updated.getStudents());

                    // Notify listeners
                    notifyCourseUpdated(courseId, updated);

                    return updated;
                } else {
                    Log.w(TAG, "recordPurchase failed: " + apiResponse.getMessage());
                    return null;
                }
            } else {
                Log.e(TAG, "recordPurchase HTTP error: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in recordPurchase", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in recordPurchase", e);
            return null;
        }
    }

    // ============ APPROVAL WORKFLOW ============

    @Override
    public List<Course> getPendingCourses() {
        try {
            // GET /course/pending (admin only)
            Response<CourseApiResponse<List<CourseDto>>> response =
                retrofitService.getPendingCourses().execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<List<CourseDto>> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return convertDtoListToCourseList(apiResponse.getData());
                } else {
                    Log.w(TAG, "getPendingCourses failed: " + apiResponse.getMessage());
                    return new ArrayList<>();
                }
            } else {
                Log.e(TAG, "getPendingCourses HTTP error: " + response.code());
                return new ArrayList<>();
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in getPendingCourses", e);
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in getPendingCourses", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean approveInitialCreation(String courseId) {
        if (courseId == null) {
            Log.w(TAG, "approveInitialCreation: courseId is null");
            return false;
        }

        try {
            // POST /course/:id/approve-initial
            Response<CourseApiResponse<CourseDto>> response =
                retrofitService.approveInitialCreation(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<CourseDto> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Log.i(TAG, "approveInitialCreation success: " + courseId);

                    // Notify listeners
                    if (apiResponse.getData() != null) {
                        Course approved = convertDtoToCourse(apiResponse.getData());
                        notifyCourseUpdated(courseId, approved);
                    }

                    return true;
                } else {
                    Log.w(TAG, "approveInitialCreation failed: " + apiResponse.getMessage());
                    return false;
                }
            } else {
                Log.e(TAG, "approveInitialCreation HTTP error: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in approveInitialCreation", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in approveInitialCreation", e);
            return false;
        }
    }

    @Override
    public boolean rejectInitialCreation(String courseId) {
        // TODO: Backend doesn't have /course/:id/reject-initial endpoint yet
        // Workaround: Use approve-delete endpoint (which deletes permanently)

        Log.w(TAG, "rejectInitialCreation: Backend endpoint not implemented, using approve-delete as workaround");
        return permanentlyDeleteCourse(courseId);
    }

    @Override
    public boolean approveCourseEdit(String courseId) {
        if (courseId == null) {
            Log.w(TAG, "approveCourseEdit: courseId is null");
            return false;
        }

        try {
            // POST /course/:id/approve-edit
            Response<CourseApiResponse<CourseDto>> response =
                retrofitService.approveCourseEdit(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<CourseDto> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Log.i(TAG, "approveCourseEdit success: " + courseId);

                    // Notify listeners
                    if (apiResponse.getData() != null) {
                        Course approved = convertDtoToCourse(apiResponse.getData());
                        notifyCourseUpdated(courseId, approved);
                    }

                    return true;
                } else {
                    Log.w(TAG, "approveCourseEdit failed: " + apiResponse.getMessage());
                    return false;
                }
            } else {
                Log.e(TAG, "approveCourseEdit HTTP error: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in approveCourseEdit", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in approveCourseEdit", e);
            return false;
        }
    }

    @Override
    public boolean rejectCourseEdit(String courseId) {
        if (courseId == null) {
            Log.w(TAG, "rejectCourseEdit: courseId is null");
            return false;
        }

        try {
            // POST /course/:id/reject-edit
            Response<CourseApiResponse<Object>> response =
                retrofitService.rejectCourseEdit(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<Object> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Log.i(TAG, "rejectCourseEdit success: " + courseId);

                    // Get updated course to notify listeners
                    Course updated = getCourseDetail(courseId);
                    if (updated != null) {
                        notifyCourseUpdated(courseId, updated);
                    }

                    return true;
                } else {
                    Log.w(TAG, "rejectCourseEdit failed: " + apiResponse.getMessage());
                    return false;
                }
            } else {
                Log.e(TAG, "rejectCourseEdit HTTP error: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in rejectCourseEdit", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in rejectCourseEdit", e);
            return false;
        }
    }

    @Override
    public Course getPendingEdit(String courseId) {
        if (courseId == null) {
            Log.w(TAG, "getPendingEdit: courseId is null");
            return null;
        }

        try {
            // GET /course/:id/pending
            Response<CourseApiResponse<CourseDto>> response =
                retrofitService.getPendingEdit(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<CourseDto> apiResponse = response.body();

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return convertDtoToCourse(apiResponse.getData());
                } else {
                    // No pending edit (this is normal, not an error)
                    return null;
                }
            } else {
                Log.e(TAG, "getPendingEdit HTTP error: " + response.code());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in getPendingEdit", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in getPendingEdit", e);
            return null;
        }
    }

    @Override
    public boolean hasPendingEdit(String courseId) {
        // Simple check: try to get pending edit
        return getPendingEdit(courseId) != null;
    }

    @Override
    public boolean permanentlyDeleteCourse(String courseId) {
        if (courseId == null) {
            Log.w(TAG, "permanentlyDeleteCourse: courseId is null");
            return false;
        }

        try {
            // POST /course/:id/approve-delete
            Response<CourseApiResponse<CourseDto>> response =
                retrofitService.approveDelete(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<CourseDto> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Log.i(TAG, "permanentlyDeleteCourse success: " + courseId);

                    // Notify listeners that course was deleted (pass null)
                    notifyCourseUpdated(courseId, null);

                    return true;
                } else {
                    Log.w(TAG, "permanentlyDeleteCourse failed: " + apiResponse.getMessage());
                    return false;
                }
            } else {
                Log.e(TAG, "permanentlyDeleteCourse HTTP error: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in permanentlyDeleteCourse", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in permanentlyDeleteCourse", e);
            return false;
        }
    }

    @Override
    public boolean cancelDeleteRequest(String courseId) {
        if (courseId == null) {
            Log.w(TAG, "cancelDeleteRequest: courseId is null");
            return false;
        }

        try {
            // POST /course/:id/reject-delete
            Response<CourseApiResponse<Object>> response =
                retrofitService.rejectDelete(courseId).execute();

            if (response.isSuccessful() && response.body() != null) {
                CourseApiResponse<Object> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Log.i(TAG, "cancelDeleteRequest success: " + courseId);

                    // Get updated course to notify listeners
                    Course updated = getCourseDetail(courseId);
                    if (updated != null) {
                        notifyCourseUpdated(courseId, updated);
                    }

                    return true;
                } else {
                    Log.w(TAG, "cancelDeleteRequest failed: " + apiResponse.getMessage());
                    return false;
                }
            } else {
                Log.e(TAG, "cancelDeleteRequest HTTP error: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error in cancelDeleteRequest", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in cancelDeleteRequest", e);
            return false;
        }
    }

    // ============ LISTENER MANAGEMENT ============

    @Override
    public void addCourseUpdateListener(CourseUpdateListener l) {
        if (l != null && !courseUpdateListeners.contains(l)) {
            courseUpdateListeners.add(l);
        }
    }

    @Override
    public void removeCourseUpdateListener(CourseUpdateListener l) {
        courseUpdateListeners.remove(l);
    }

    private void notifyCourseUpdated(String courseId, Course course) {
        for (CourseUpdateListener l : new ArrayList<>(courseUpdateListeners)) {
            try {
                l.onCourseUpdated(courseId, course);
            } catch (Exception ignored) {}
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Convert CourseDto from backend to Course model for app
     */
    private Course convertDtoToCourse(CourseDto dto) {
        if (dto == null) return null;

        Course course = new Course(
            dto.getId(),
            dto.getTitle(),
            dto.getTeacher(),
            dto.getImageUrl(),
            dto.getCategory(),
            dto.getLectures(),
            dto.getStudents(),
            dto.getRating(),
            dto.getPrice(),
            dto.getDescription(),
            dto.getCreatedAt(),
            dto.getRatingCount(),
            dto.getTotalDurationMinutes(),
            dto.getSkills() != null ? dto.getSkills() : new ArrayList<>(),
            dto.getRequirements() != null ? dto.getRequirements() : new ArrayList<>()
        );

        // Map approval fields
        course.setInitialApproved(dto.getIsApproved() != null ? dto.getIsApproved() : false);
        course.setEditApproved(dto.getIsEditApproved() != null ? dto.getIsEditApproved() : false);
        course.setDeleteRequested(dto.getIsDeleteRequested() != null ? dto.getIsDeleteRequested() : false);

        return course;
    }

    /**
     * Convert list of CourseDtos to list of Courses
     */
    private List<Course> convertDtoListToCourseList(List<CourseDto> dtoList) {
        List<Course> courses = new ArrayList<>();
        if (dtoList != null) {
            for (CourseDto dto : dtoList) {
                Course c = convertDtoToCourse(dto);
                if (c != null) {
                    courses.add(c);
                }
            }
        }
        return courses;
    }

    /**
     * Create RequestBody for text/plain
     */
    private RequestBody createTextPart(String text) {
        if (text == null) text = "";
        return RequestBody.create(MediaType.parse("text/plain"), text);
    }

    /**
     * Create RequestBody for JSON array (skills, requirements)
     */
    private RequestBody createJsonArrayPart(List<String> items) {
        if (items == null || items.isEmpty()) {
            return RequestBody.create(MediaType.parse("text/plain"), "[]");
        }

        // Convert to JSON array string: ["item1", "item2", "item3"]
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            json.append("\"").append(items.get(i)).append("\"");
            if (i < items.size() - 1) json.append(",");
        }
        json.append("]");

        return RequestBody.create(MediaType.parse("text/plain"), json.toString());
    }

    /**
     * Check if course category contains wanted category (multi-tag support)
     */
    private boolean hasCategory(String categories, String wanted) {
        if (categories == null || wanted == null) return false;
        String[] parts = categories.split(",");
        for (String p : parts) {
            if (p != null && p.trim().equalsIgnoreCase(wanted.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if two category strings share any common category
     */
    private boolean shareCategory(String cat1, String cat2) {
        if (cat1 == null || cat2 == null) return false;
        String[] a1 = cat1.split(",");
        String[] a2 = cat2.split(",");
        for (String s1 : a1) {
            if (s1 == null) continue;
            String t1 = s1.trim();
            if (t1.isEmpty()) continue;
            for (String s2 : a2) {
                if (s2 == null) continue;
                if (t1.equalsIgnoreCase(s2.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
