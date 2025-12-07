# 📚 Hướng dẫn Quản lý Khóa học cho Teacher

## 📋 Tổng quan

Tôi đã xây dựng giao diện quản lý khóa học cho teacher bao gồm 2 màn hình chính:

### **1. TeacherCourseDetailActivity** - Chi tiết khóa học
Địa điểm: `activity/TeacherCourseDetailActivity.java`
Layout: `activity_teacher_course_detail.xml`

**Gồm 4 khối chính:**

#### ✅ **Khối 1: Thông tin cơ bản khóa học**
- Thumbnail khóa học
- Tiêu đề, danh mục, giá
- Mô tả khóa học
- Thống kê: số học viên, rating, số bài giảng
- Thời lượng tổng cộng
- Ngày tạo khóa học
- Danh sách kỹ năng sẽ học
- Danh sách yêu cầu

#### ✅ **Khối 2: Quản lý học viên** (Expandable)
- RecyclerView với `TeacherStudentAdapter`
- Mỗi item hiển thị:
  - **Collapsed:** Tên, email, avatar, % tiến độ
  - **Expanded:** 
    - Progress bar tiến độ
    - Số bài hoàn thành / tổng bài
    - Lần cuối xem
    - Tổng thời gian học
    - Nested RecyclerView chi tiết tiến độ từng bài

#### ✅ **Khối 3: Quản lý bài giảng** (Expandable)
- RecyclerView với `TeacherLessonAdapter`
- Button "Thêm bài giảng"
- Mỗi item hiển thị:
  - **Collapsed:** Số thứ tự, tiêu đề, thời lượng
  - **Expanded:**
    - Mô tả bài học
    - Link video
    - Thống kê: Học viên xem / hoàn thành / tỉ lệ
    - Button "Xem bình luận" → Navigate tới TeacherLessonManageActivity
  - Buttons edit/delete

#### ✅ **Khối 4: Đánh giá khóa học** (Expandable)
- RecyclerView với `TeacherReviewAdapter`
- Mỗi item hiển thị:
  - **Collapsed:** Avatar, tên, rating stars, ngày review
  - **Expanded:**
    - Nội dung review
    - (Nếu có) Phản hồi từ teacher
    - EditText để teacher trả lời
    - Buttons: Trả lời / Xóa

---

### **2. TeacherLessonManageActivity** - Quản lý bài giảng cụ thể
Địa điểm: `activity/TeacherLessonManageActivity.java`
Layout: `activity_teacher_lesson_manage.xml`

**Gồm 3 khối chính:**

#### ✅ **Khối 1: Video Section**
- Preview thumbnail video
- Play button (TODO: Implement YouTube player)
- Thời lượng video
- Link video
- Button "Đổi video"

#### ✅ **Khối 2: Lesson Info Section**
- Tiêu đề bài học
- Mô tả bài học
- Button "Chỉnh sửa"

#### ✅ **Khối 3: Comments Section**
- RecyclerView với `LessonCommentAdapter`
- Hiển thị bình luận từ học viên
- Mỗi comment có:
  - Avatar, tên, role (Học viên)
  - Thời gian bình luận
  - Nội dung bình luận
  - (Nếu có) Phản hồi từ teacher (có thể xóa)
  - Buttons: Trả lời / Xóa

---

## 📂 Cấu trúc Files

### **Layouts (XML)**
```
res/layout/
├── activity_teacher_course_detail.xml      # Màn hình chi tiết khóa học
├── activity_teacher_lesson_manage.xml       # Màn hình quản lý bài giảng
├── item_teacher_student.xml                # Item học viên (expandable)
├── item_lesson_progress_detail.xml         # Item tiến độ bài học (nested)
├── item_teacher_lesson.xml                 # Item bài giảng (expandable)
├── item_teacher_review.xml                 # Item review (expandable)
└── item_lesson_comment.xml                 # Item bình luận bài học
```

### **Adapters**
```
feature/teacher/adapter/
├── TeacherStudentAdapter.java              # Adapter cho học viên
├── LessonProgressDetailAdapter.java        # Nested adapter cho tiến độ bài
├── TeacherLessonAdapter.java               # Adapter cho bài giảng
├── TeacherReviewAdapter.java               # Adapter cho reviews
└── LessonCommentAdapter.java               # Adapter cho bình luận
```

### **Activities**
```
feature/teacher/activity/
├── TeacherCourseDetailActivity.java        # Chi tiết khóa học
└── TeacherLessonManageActivity.java        # Quản lý bài giảng
```

---

## 🎨 Giao diện Expandable

Tất cả adapters đều sử dụng pattern **expand/collapse** bằng cách:
1. Click vào header item → toggle `layoutDetail` visibility
2. Animate icon từ 0° → 180°
3. Hiển thị chi tiết khi expanded

Ví dụ:
```java
layoutStudentHeader.setOnClickListener(v -> toggleExpand());

private void toggleExpand() {
    isExpanded = !isExpanded;
    if (isExpanded) {
        layoutStudentDetail.setVisibility(View.VISIBLE);
        imgExpand.animate().rotation(180).start();
    } else {
        layoutStudentDetail.setVisibility(View.GONE);
        imgExpand.animate().rotation(0).start();
    }
}
```

---

## 🔧 Integration Points (TODO)

### **Data Loading**
- [ ] Load course từ Intent → `getCourseFromIntent()`
- [ ] Load students từ API → `studentAdapter.setStudents()`
- [ ] Load lessons từ API → `lessonAdapter.setLessons()`
- [ ] Load reviews từ API → `reviewAdapter.setReviews()`
- [ ] Load comments từ API → `commentAdapter.setComments()`

### **Event Handlers (Toast placeholders)**
- [ ] Edit course → Navigate to TeacherCourseEditActivity
- [ ] Delete course → Confirm dialog + API call
- [ ] Add lesson → Navigate to lesson create activity
- [ ] Edit lesson → Open edit form
- [ ] Delete lesson → Confirm dialog + API call
- [ ] Reply review → Show form + submit to API
- [ ] Delete review → Confirm dialog + API call
- [ ] Reply comment → Show form + submit to API
- [ ] Delete comment → Confirm dialog + API call

### **Image Loading**
- [ ] Load avatar bằng Glide/Picasso
- [ ] Load course thumbnail
- [ ] Load video thumbnail

### **Navigation**
- [ ] TeacherCourseDetailActivity ← TeacherHomeFragment (click item)
- [ ] TeacherLessonManageActivity ← TeacherCourseDetailActivity (click lesson)

---

## 🧪 Mock Data

Tất cả activities đều có `loadMockData()` để test UI:
- 3 mock students với tiến độ 75%
- 5 mock lessons
- 5 mock comments

Có thể modify những giá trị này để test khác nhau.

---

## 💡 Design Patterns Sử dụng

1. **Adapter Pattern** - Riêng mỗi loại data có adapter riêng
2. **Expand/Collapse Pattern** - RecyclerView items có thể expand
3. **Nested RecyclerView** - Lesson progress trong student detail
4. **ViewHolder Pattern** - Cache views trong adapters
5. **Model Classes** - StudentProgressItem, LessonItem, ReviewItem

---

## 📝 Notes

- Tất cả click events hiện tại show `Toast` → Replace bằng action thực tế
- Models có getters/setters đầy đủ → Ready cho API integration
- Layout dùng `NestedScrollView` → Hỗ trợ scroll mượt khi nested
- CardView + dividers → UI clean và readable
- Responsive design → Adapt với màn hình khác nhau

---

## 🚀 Next Steps

1. **API Integration:**
   - Replace `loadMockData()` bằng API calls
   - Map CourseApi, LessonApi, ReviewApi responses

2. **Implement TODO items:**
   - Edit/Delete course
   - Edit/Delete lesson
   - Reply reviews/comments
   - Delete reviews/comments

3. **Add Features:**
   - Confirm dialogs trước delete
   - Loading indicators
   - Error handling
   - Image loading (Glide/Picasso)

4. **Navigation:**
   - Add Intent extras khi navigate
   - Handle back button properly

5. **Animations:**
   - Add shared element transitions
   - Smooth expand/collapse animations

---

## 📞 Helper Methods

**Format Duration:**
```java
private String formatDuration(int minutes) {
    int hours = minutes / 60;
    int mins = minutes % 60;
    if (hours > 0) {
        return hours + " giờ " + mins + " phút";
    } else {
        return mins + " phút";
    }
}
```

**Format Date:**
```java
private String formatDate(long timestamp) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    return sdf.format(new Date(timestamp));
}
```

---

Good luck! 🎉
