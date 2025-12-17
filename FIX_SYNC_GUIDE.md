# 🔴 VẤN ĐỀ NGHIÊM TRỌNG: APP KHÔNG SYNC VỚI BACKEND

## NGUYÊN NHÂN

1. **MyCourse cache chưa được init khi app start**
2. **CourseStatusResolver.isPurchased() check cache local (rỗng) thay vì backend**
3. **StudentMyCourseFragment chỉ load 1 lần khi create, không sync với backend**

## FIX STRATEGY

### Fix 1: Force init MyCourse cache khi login
### Fix 2: Thêm refresh button cho MyCourseFragment
### Fix 3: Debug logs để track vấn đề

---

## TEST DEBUG

1. Check Logcat với filter: `MyCourseRemoteApi`
2. Xem logs:
   - `getMyCourses: synced X courses` → Backend trả về X courses
   - `isPurchased(cX): cache hit = true/false` → Check cache
   - `isPurchased(cX): cache not ready, calling backend` → Cache chưa init

3. Nếu KHÔNG thấy log `getMyCourses` → Backend không được gọi!
4. Nếu thấy `synced 0 courses` → Backend trả về rỗng (kiểm tra token/database)
