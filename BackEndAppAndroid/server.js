const express = require("express");
const nodemailer = require("nodemailer");
const jwt = require("jsonwebtoken");
const pgp = require("pg-promise")();
const cors = require("cors");

const db = pgp("postgres://postgres:07052004@127.0.0.1:5432/online-learning2");

const app = express();
const port = 3000;
const secretKey = "apphoctap";

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cors({ origin: "*", methods: ["GET", "POST", "PUT", "DELETE"] }));

app.get("/", (req, res) => {
  res.send("Hello World");
});

app.post("/login", async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) {
    return res.send({
      success: false,
      message: "Thiếu field",
      data: null
    });
  }

  try {
    const row = await db.oneOrNone(
      `SELECT u.user_id,
              u.username,
              u.full_name,     -- nếu có cột full_name
              u.email,
              u.verified,      -- nếu có cột verified (boolean)
              u.role_id,
              r.role_name
       FROM appuser u
       JOIN role r ON u.role_id = r.role_id
       WHERE u.username = $1 AND u.password = $2`,
      [username, password]
    );

    if (!row) {
      return res.send({
        success: false,
        message: "Sai tài khoản/mật khẩu",
        data: null
      });
    }

    // Map DB -> User giống bên Android
    const user = {
      id: row.user_id.toString(),                  // "u1", "u2"... FE dùng String
      name: row.full_name || row.username,         // giống "name" trong seed JSON
      username: row.username,
      email: row.email,
      // KHÔNG gửi password thực, để null/không gửi cũng được
      password: null,
      verified: row.verified ?? true,              // nếu chưa có cột thì default true
      avatar: null,                                // hoặc field thứ 7 trong User
      role: String(row.role_name).toUpperCase()    // STUDENT / TEACHER / ADMIN
    };

    const token = jwt.sign(
      { userId: user.id, role: user.role },
      secretKey,
      { expiresIn: "1h" }
    );

    return res.send({
      success: true,
      message: "Đăng nhập thành công",
      data: user,
      token: token
    });
  } catch (err) {
    console.error(err);
    return res.send({
      success: false,
      message: "Lỗi hệ thống",
      data: null
    });
  }
});

app.post("/signup", async (req, res) => {
  const { name, username, email, password, role } = req.body;

  // 1. Validate basic field
  if (!name || !username || !email || !password || !role) {
    return res.send({
      success: false,
      message: "Thiếu field",
      data: null
    });
  }

  // 2. Không cho tự đăng ký ADMIN
  if (String(role).toUpperCase() === "ADMIN") {
    return res.send({
      success: false,
      message: "Không thể tự đăng ký ADMIN.",
      data: null
    });
  }

  try {
    // 3. Check trùng email + username
    const existed = await db.oneOrNone(
      `SELECT username, email
       FROM appuser
       WHERE LOWER(username) = LOWER($1)
          OR LOWER(email) = LOWER($2)`,
      [username, email]
    );

    if (existed) {
      if (existed.email?.toLowerCase() === email.toLowerCase()) {
        return res.send({
          success: false,
          message: "Email đã tồn tại.",
          data: null
        });
      }
      if (existed.username?.toLowerCase() === username.toLowerCase()) {
        return res.send({
          success: false,
          message: "Username đã tồn tại.",
          data: null
        });
      }
    }

    // 4. Tìm role_id tương ứng với role string
    const roleRow = await db.one(
      `SELECT role_id, role_name
       FROM role
       WHERE UPPER(role_name) = UPPER($1)`,
       [role]   // STUDENT / TEACHER
    );

    // 5. Insert user mới (verified = true giống fake)
    const row = await db.one(
      `INSERT INTO appuser(username, email, password, full_name, verified, role_id, created_at)
       VALUES($1, $2, $3, $4, $5, $6, NOW())
       RETURNING user_id, username, full_name, email, verified`,
      [username, email, password, name, true, roleRow.role_id]
    );

    // 6. Map sang User như FE
    const user = {
      id: row.user_id.toString(),
      name: row.full_name,
      username: row.username,
      email: row.email,
      password: null,              // không nên trả mật khẩu thật
      verified: row.verified,
      avatar: null,
      role: roleRow.role_name.toUpperCase()
    };

    return res.send({
      success: true,
      message: "Đăng ký thành công. Bạn có thể đăng nhập.",
      data: user
    });
  } catch (err) {
    console.error(err);
    return res.send({
      success: false,
      message: "Lỗi hệ thống",
      data: null
    });
  }
});


app.post("/forgot-password-request", async (req, res) => {
  const { email } = req.body;
  if (!email) {
    return res.send({
      success: false,
      message: "Email không có!",
      data: null
    });
  }

  try {
    // Tìm user theo email
    const user = await db.oneOrNone(
      "SELECT user_id, email FROM appuser WHERE LOWER(email) = LOWER($1)",
      [email]
    );

    if (!user) {
      return res.send({
        success: false,
        message: "Email không tồn tại trong hệ thống.",
        data: null
      });
    }

    // Tạo token reset và lưu DB
    const token = uuidv4();
    await db.none(
      "UPDATE appuser SET reset_token = $1 WHERE user_id = $2",
      [token, user.user_id]
    );

    // Link reset (dev test): FE sẽ nhận được link này trong data
    const resetLink = `http://127.0.0.1:5500/forgot-password-confirm.html?token=${token}`;

    // Gửi email (prod)
    await sendEmail(
      email,
      "Quên mật khẩu",
      `Truy cập link để đổi mật khẩu:\n${resetLink}`
    );

    return res.send({
      success: true,
      message: "Đã gửi link đặt lại mật khẩu (demo).",
      data: resetLink          // khớp ApiResult<String>
    });
  } catch (err) {
    console.error(err);
    return res.send({
      success: false,
      message: "Lỗi hệ thống",
      data: null
    });
  }
});

app.post("/forgot-password-update", async (req, res) => {
  const { token, newPassword } = req.body;

  if (!token || !newPassword) {
    return res.send({
      success: false,
      message: "Token không hợp lệ.",
      data: false
    });
  }

  try {
    const user = await db.oneOrNone(
      "SELECT user_id FROM appuser WHERE reset_token = $1",
      [token]
    );

    if (!user) {
      return res.send({
        success: false,
        message: "Token không hợp lệ hoặc đã hết hạn.",
        data: false
      });
    }

    await db.none(
      "UPDATE appuser SET password = $1, reset_token = NULL WHERE user_id = $2",
      [newPassword, user.user_id]
    );

    return res.send({
      success: true,
      message: "Đổi mật khẩu thành công qua link.",
      data: true
    });
  } catch (err) {
    console.error(err);
    return res.send({
      success: false,
      message: "Lỗi hệ thống",
      data: false
    });
  }
});

// CRUD course

// create
app.post("/course", upload.single('courseAvatar'), async (req, res) => {
    const { title, description, teacher_id } = req.body;
    const imgSrc = req.file ? `/uploads/${req.file.filename}` : '';

    if (!title || !description || !teacher_id)
        return res.send({
            message: "Khong du thong tin",
            success: false,
        });

    try {
        const course = await db.one(
            "INSERT INTO course(title, description, teacher_id, course_avatar) VALUES($1, $2, $3, $4) RETURNING *",
            [title, description, teacher_id, imgSrc]
        );

        res.send({
            success: true,
            message: "Them moi thanh cong",
            data: course,
        });
    } catch (error) {
        console.log(error);
        res.send({ success: false, message: "Lỗi tạo khóa học" });
    }
});


// read
app.get("/course", async (req, res) => {
    const { teacher_id } = req.query;

    if (!teacher_id) {
        return res.send({
            success: false,
            message: "Khong ro Giao vien",
        });
    }

    const course = await db.any(
        "SELECT * FROM course WHERE teacher_id = $1",
        [teacher_id]
    );

    res.send({
        success: true,
        data: course,
    });
});

// get detail
app.get("/course/:id", async (req, res) => {
    const { id } = req.params;
    const course = await db.oneOrNone(
        "SELECT * FROM course WHERE course_id = $1",
        [id]
    );

    res.send({
        success: true,
        data: course,
    });
});

// update
app.patch("/course/:id", upload.single('courseAvatar'), async (req, res) => {
    const { id } = req.params;
    const { title, description } = req.body;
    const courseAvatar = req.file ? `/uploads/${req.file.filename}` : null;

    if (!title || !description) {
        return res.send({ success: false, message: "Khong du thong tin" });
    }

    let query, values;
    if (courseAvatar) {
        query = `
            UPDATE course
            SET title = $1, description = $2, course_avatar = $3
            WHERE course_id = $4 RETURNING *`;
        values = [title, description, courseAvatar, id];
    } else {
        query = `
            UPDATE course
            SET title = $1, description = $2
            WHERE course_id = $3 RETURNING *`;
        values = [title, description, id];
    }

    try {
        const updated = await db.one(query, values);
        res.send({ success: true, data: updated });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Lỗi khi cập nhật khóa học" });
    }
});


// delete
app.delete("/course/:id", async (req, res) => {
    const { id } = req.params;
    const deletedCourse = await db.oneOrNone(
        "DELETE FROM course WHERE course_id = $1 RETURNING *",
        [id]
    );
    res.send({
        success: true,
        data: deletedCourse,
    });
});

//API about Student

//Lấy tất cả các course
app.get("/courses", async (req, res) => {
    console.log('>>> [GET /courses]');
    const courses = await db.any("SELECT * FROM course");
    res.send({
        success: true,
        data: courses,
    });
});

//Xử lý Course theo người dùng
app.get("/my-courses", async (req, res) => {
    const { email } = req.query;

    console.log(`>>> [GET /my-courses] email: ${email}`);

    if (!email) {
        return res.send({
            success: false,
            message: "Email ko co",
        });
    }

    const user = await db.oneOrNone("SELECT user_id FROM appuser WHERE email = $1", [email]);
    if (!user) {
        return res.send({
            success: false,
            message: "Nguoi dung ko ton tai",
        });
    }

    const enrollments = await db.any(
        "SELECT course_id FROM courseenrollment WHERE user_id = $1",
        [user.user_id]
    );

    const courseIds = enrollments.map(e => e.course_id);

    console.log(`>>> [GET /my-courses] user_id: ${user.user_id}, courses:`, courseIds);

    res.send({
        success: true,
        data: courseIds,
    });
});

// POST /book-course
app.post("/book-course", async (req, res) => {
    const { email, courseId } = req.body;

    console.log(`>>> [POST /book-course] email: ${email}, courseId: ${courseId}`);

    if (!email || !courseId) {
        return res.send({
            success: false,
            message: "Khong co Email hoac Course",
        });
    }

    const user = await db.oneOrNone("SELECT user_id FROM appuser WHERE email = $1", [email]);
    if (!user) {
        return res.send({
            success: false,
            message: "User ko ton tai",
        });
    }

    // Kiem tra neu da ghi danh roi
    const existing = await db.oneOrNone(
        "SELECT * FROM courseenrollment WHERE user_id = $1 AND course_id = $2",
        [user.user_id, courseId]
    );

    if (existing) {
        console.log(`>>> [POST /book-course] Already enrolled! user_id: ${user.user_id}, course_id: ${courseId}`);
        return res.send({
            success: false,
            message: "Da ghi danh roi",
        });
    }

    // Insert enrollment
    const enrollment = await db.one(
        "INSERT INTO courseenrollment(user_id, course_id) VALUES($1, $2) RETURNING *",
        [user.user_id, courseId]
    );

    console.log(`>>> [POST /book-course] Enrollment created:`, enrollment);

    res.send({
        success: true,
        message: "Ghi danh thanh cong",
        data: enrollment,
    });
});

// CREATE chapter
app.post("/chapter", async (req, res) => {
    const { title, course_id } = req.body;
    if (!title || !course_id)
        return res.send({ success: false, message: "Thiếu thông tin" });

    try {
        const chapter = await db.one(
            "INSERT INTO chapter(title, course_id) VALUES($1, $2) RETURNING *",
            [title, course_id]
        );
        res.send({ success: true, data: chapter });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Tạo chương thất bại" });
    }
});

app.get("/chapter", async (req, res) => {
    const { course_id } = req.query;

    if (!course_id) {
        return res.send({
            success: false,
            message: "Khong co course",
        });
    }

    const chapter = await db.any(
        "SELECT * FROM chapter WHERE course_id = $1",
        [course_id]
    );

    res.send({
        success: true,
        data: chapter,
    });
});

// GET chapters by course_id
app.get("/chapters/:course_id", async (req, res) => {
    const { course_id } = req.params;
    try {
        const chapters = await db.any(
            "SELECT * FROM chapter WHERE course_id = $1",
            [course_id]
        );
        res.send({ success: true, data: chapters });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Lấy chương thất bại" });
    }
});

// UPDATE chapter
app.patch("/chapter/:id", async (req, res) => {
    const { id } = req.params;
    const { title } = req.body;
    try {
        const updated = await db.oneOrNone(
            "UPDATE chapter SET title = $1 WHERE chapter_id = $2 RETURNING *",
            [title, id]
        );
        res.send({ success: true, data: updated });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Cập nhật thất bại" });
    }
});

// DELETE chapter
app.delete("/chapter/:id", async (req, res) => {
    const { id } = req.params;
    try {
        const deleted = await db.oneOrNone(
            "DELETE FROM chapter WHERE chapter_id = $1 RETURNING *",
            [id]
        );
        res.send({ success: true, data: deleted });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Xoá thất bại" });
    }
});

// CREATE lesson
app.post("/lesson", async (req, res) => {
    const { title, content, chapter_id, video_url } = req.body;
    if (!title || !chapter_id)
        return res.send({ success: false, message: "Thiếu thông tin" });

    try {
        const lesson = await db.one(
            `INSERT INTO lesson(title, content, chapter_id, video_url)
             VALUES($1, $2, $3, $4) RETURNING *`,
            [title, content || '', chapter_id, video_url || null]
        );
        res.send({ success: true, data: lesson });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Tạo bài học thất bại" });
    }
});

// GET lessons by chapter_id
app.get("/lessons/:chapter_id", async (req, res) => {
    const { chapter_id } = req.params;
    try {
        const lessons = await db.any(
            "SELECT * FROM lesson WHERE chapter_id = $1",
            [chapter_id]
        );
        res.send({ success: true, data: lessons });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Lấy bài học thất bại" });
    }
});

// UPDATE lesson
app.patch("/lesson/:id", async (req, res) => {
    const { id } = req.params;
    const { title, content, video_url } = req.body;
    try {
        const updated = await db.oneOrNone(
            `UPDATE lesson SET title = $1, content = $2, video_url = $3
             WHERE lesson_id = $4 RETURNING *`,
            [title, content, video_url, id]
        );
        res.send({ success: true, data: updated });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Cập nhật bài học thất bại" });
    }
});

// DELETE lesson
app.delete("/lesson/:id", async (req, res) => {
    const { id } = req.params;
    try {
        const deleted = await db.oneOrNone(
            "DELETE FROM lesson WHERE lesson_id = $1 RETURNING *",
            [id]
        );
        res.send({ success: true, data: deleted });
    } catch (err) {
        console.log(err);
        res.send({ success: false, message: "Xoá bài học thất bại" });
    }
});

app.post("/upload-video/:lesson_id", uploadVideo.single("video"), async (req, res) => {
    const { lesson_id } = req.params;

    if (!req.file) {
        return res.send({ success: false, message: "Không có video nào được tải lên!" });
    }

    const videoUrl = `/uploads/videos/${req.file.filename}`;

    try {
        const updated = await db.oneOrNone(
            "UPDATE lesson SET video_url = $1 WHERE lesson_id = $2 RETURNING *",
            [videoUrl, lesson_id]
        );

        if (!updated) {
            return res.send({ success: false, message: "Không tìm thấy bài học để cập nhật!" });
        }

        res.send({
            success: true,
            message: "Upload và cập nhật bài học thành công!",
            data: updated
        });
    } catch (err) {
        console.log(err);
        res.status(500).send({ success: false, message: "Upload video thất bại" });
    }
});

app.listen(port, () => console.log(`Server running at port ${port}`));
