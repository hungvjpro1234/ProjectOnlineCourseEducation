// server.js
const express = require("express");
const nodemailer = require("nodemailer");
const jwt = require("jsonwebtoken");
const pgp = require("pg-promise")();
const cors = require("cors");
const { v4: uuidv4 } = require("uuid");
const bcrypt = require("bcrypt");

require("dotenv").config();

const connection = {
  connectionString: (process.env.DATABASE_URL || "").trim(),
  ssl: {
    rejectUnauthorized: false
  }
};

const db = pgp(connection);

// --- Helper: normalize DB row to FE-friendly Course object ---
function safeParseJson(input) {
  if (!input) return [];
  if (Array.isArray(input)) return input;
  try {
    // If database stored a JSON string
    if (typeof input === 'string') {
      return JSON.parse(input);
    }
    // already object
    return input;
  } catch (e) {
    // If it's a CSV string like "a,b,c" or simple string, try fallback
    if (typeof input === 'string') {
      return input.split(',').map(s => s.trim()).filter(s => s.length > 0);
    }
    return [];
  }
}

function transformCourseRow(row) {
  if (!row) return null;
  // Map DB columns (course_id) to FE shape (id), normalize casing
  const r = {};
  r.id = row.course_id || row.id || row.courseId || null;
  r.title = row.title || '';
  r.description = row.description || '';
  r.teacher = row.teacher || '';
  // imageUrl might come as imageurl or imageUrl
  r.imageUrl = row.imageUrl || row.imageurl || '';
  r.category = row.category || '';
  r.lectures = Number.isFinite(Number(row.lectures)) ? parseInt(row.lectures) : 0;
  r.students = Number.isFinite(Number(row.students)) ? parseInt(row.students) : 0;
  r.rating = row.rating !== undefined ? parseFloat(row.rating) : 0;
  r.price = row.price !== undefined ? parseFloat(row.price) : 0;
  r.createdAt = row.createdAt || row.created_at || '';
  r.ratingCount = row.ratingCount !== undefined ? parseInt(row.ratingCount) : (row.ratingcount !== undefined ? parseInt(row.ratingcount) : 0);
  r.totalDurationMinutes = row.totalDurationMinutes !== undefined ? parseInt(row.totalDurationMinutes) : (row.totaldurationminutes !== undefined ? parseInt(row.totaldurationminutes) : 0);

  // Parse skills/requirements (could be JSON-string, array, or CSV)
  r.skills = safeParseJson(row.skills);
  r.requirements = safeParseJson(row.requirements);

  // Keep any other fields if needed
  return r;
}


// optional: test connect once at startup
(async () => {
  try {
    const c = await db.connect();
    console.log("pg-promise connected OK");
    c.done();
  } catch (err) {
    console.error("pg-promise connect error", err);
  }
})();

const app = express();
const port = process.env.PORT || 3000;
const secretKey = process.env.JWT_SECRET || "apphoctap"; // move to env in prod

app.use(express.json());
// --- Thêm / dán vào server.js (đặt BEFORE các route dùng upload, tức trước app.post("/course", ...) ) ---

const path = require('path');
const fs = require('fs');
const multer = require('multer');

// tạo folder uploads nếu chưa có (đảm bảo có quyền ghi)
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// cấu hình storage để giữ extension file
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    // lưu filename: timestamp-originalname (an toàn hơn)
    const safeName = Date.now() + '-' + file.originalname.replace(/\s+/g, '_');
    cb(null, safeName);
  }
});

// filter file (tuỳ chọn): chấp nhận image thôi
const fileFilter = (req, file, cb) => {
  if (!file.mimetype.startsWith('image/')) {
    // reject non-image files
    return cb(null, false);
  }
  cb(null, true);
};

const upload = multer({ storage: storage, fileFilter: fileFilter });

// expose uploads static để client GET /uploads/filename
app.use('/uploads', express.static(uploadDir));
// ------------------ Helper để parse field có thể là JSON-array, CSV string, hoặc single value ------------------
function parseMaybeArrayField(value) {
  if (value === undefined || value === null) return [];
  if (Array.isArray(value)) return value;
  if (typeof value === 'string') {
    const trimmed = value.trim();
    // JSON array string like '["a","b"]'
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      try {
        const parsed = JSON.parse(trimmed);
        return Array.isArray(parsed) ? parsed : [];
      } catch (e) {
        // fallthrough to try CSV
      }
    }
    // CSV: "A, B, C"
    if (trimmed.indexOf(',') >= 0) {
      return trimmed.split(',').map(s => s.trim()).filter(s => s.length > 0);
    }
    // Single value string
    return trimmed.length ? [trimmed] : [];
  }
  // fallback
  return [String(value)];
}
// ------------------ end helper ------------------

// --- End paste ---

app.use(express.urlencoded({ extended: true }));
app.use(cors({ origin: "*", methods: ["GET", "POST", "PUT", "DELETE"] }));

// Simple sendEmail helper (no-op if not configured)
async function sendEmail(to, subject, text) {
  try {
    // If you want real email, configure transporter below
    // const transporter = nodemailer.createTransport({
    //   host: "...", port: 587, auth: { user: "...", pass: "..." }
    // });
    // await transporter.sendMail({ from: '"App" <no-reply@example.com>', to, subject, text });
    console.log(`[sendEmail] to=${to} subject=${subject} text=${text}`);
    return true;
  } catch (err) {
    console.error("sendEmail error:", err);
    return false;
  }
}

// JWT auth middleware
function authMiddleware(req, res, next) {
  const authHeader = req.headers.authorization || "";
  const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7) : null;
  if (!token) {
    return res.status(401).send({ success: false, message: "Không có token", data: null });
  }
  try {
    const payload = jwt.verify(token, secretKey);
    req.user = payload; // contains userId, role
    next();
  } catch (err) {
    return res.status(401).send({ success: false, message: "Token không hợp lệ/đã hết hạn", data: null });
  }
}

app.get("/", (req, res) => {
  res.send("Hello World");
});

app.post("/login", async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) {
    return res.send({ success: false, message: "Thiếu field", data: null });
  }

  try {
    // Select according to schema (no full_name, no verified)
    const row = await db.oneOrNone(
      `SELECT u.user_id,
              u.username,
              u.email,
              u.password,
              u.role_id,
              r.role_name
       FROM appuser u
       JOIN role r ON u.role_id = r.role_id
       WHERE u.username = $1`,
      [username]
    );

    if (!row) {
      return res.send({ success: false, message: "Sai tài khoản/mật khẩu", data: null });
    }

    const hashed = row.password;
    let passwordOk = false;
    if (hashed && hashed.startsWith("$2b$")) {
      passwordOk = await bcrypt.compare(password, hashed);
    } else {
      passwordOk = password === hashed;
    }

    if (!passwordOk) {
      return res.send({ success: false, message: "Sai tài khoản/mật khẩu", data: null });
    }

    const user = {
      id: String(row.user_id),
      // DB không có full_name -> dùng username
      name: row.username,
      username: row.username,
      email: row.email,
      password: null,
      verified: true, // giả sử true vì DB không có cột
      avatar: null,
      role: String(row.role_name).toUpperCase()
    };

    const token = jwt.sign({ userId: user.id, role: user.role }, secretKey, { expiresIn: "1h" });

    return res.send({ success: true, message: "Đăng nhập thành công", data: user, token });
  } catch (err) {
    console.error(err);
    return res.send({ success: false, message: "Lỗi hệ thống", data: null });
  }
});

app.post("/signup", async (req, res) => {
  const { name, username, email, password, role } = req.body;

  if (!name || !username || !email || !password || !role) {
    return res.send({ success: false, message: "Thiếu field", data: null });
  }
  if (String(role).toUpperCase() === "ADMIN") {
    return res.send({ success: false, message: "Không thể tự đăng ký ADMIN.", data: null });
  }

  try {
    const existed = await db.oneOrNone(
      `SELECT username, email FROM appuser WHERE LOWER(username) = LOWER($1) OR LOWER(email) = LOWER($2)`,
      [username, email]
    );

    if (existed) {
      if (existed.email?.toLowerCase() === email.toLowerCase()) {
        return res.send({ success: false, message: "Email đã tồn tại.", data: null });
      }
      if (existed.username?.toLowerCase() === username.toLowerCase()) {
        return res.send({ success: false, message: "Username đã tồn tại.", data: null });
      }
    }

    // ← ===  <-- Đặt đoạn roleRow ở ngay đây (trước khi hash và insert)
    const roleRow = await db.oneOrNone(
      `SELECT role_id, role_name FROM role WHERE UPPER(role_name) = UPPER($1)`,
      [role]
    );

    if (!roleRow) {
      return res.send({ success: false, message: "Role không hợp lệ", data: null });
    }
    // === end roleRow

    // Hash password
    let pwToStore = password;
    try {
      const saltRounds = 10;
      pwToStore = await bcrypt.hash(password, saltRounds);
    } catch (e) {
      console.warn("bcrypt hashing failed, storing plaintext (not recommended):", e);
    }

    // Insert according to current schema (no full_name, no verified)
    const row = await db.one(
      `INSERT INTO appuser(username, email, password, role_id, created_at)
       VALUES($1, $2, $3, $4, NOW())
       RETURNING user_id, username, email`,
      [username, email, pwToStore, roleRow.role_id]
    );

    // ...

    const user = {
      id: row.user_id.toString(),
      // sử dụng username làm name vì DB hiện tại không có full_name
      name: row.username,
      username: row.username,
      email: row.email,
      password: null,
      verified: true,
      avatar: null,
      role: roleRow.role_name.toUpperCase()
    };

    return res.send({ success: true, message: "Đăng ký thành công. Bạn có thể đăng nhập.", data: user });
  } catch (err) {
    console.error(err);
    return res.send({ success: false, message: "Lỗi hệ thống", data: null });
  }
});

app.post("/forgot-password-request", async (req, res) => {
  const { email } = req.body;
  if (!email) return res.send({ success: false, message: "Email không có!", data: null });

  try {
    const user = await db.oneOrNone("SELECT user_id, email FROM appuser WHERE LOWER(email) = LOWER($1)", [email]);
    if (!user) return res.send({ success: false, message: "Email không tồn tại trong hệ thống.", data: null });

    const token = uuidv4();
    await db.none("UPDATE appuser SET reset_token = $1 WHERE user_id = $2", [token, user.user_id]);

    // NOTE: link demo, update host as needed
    const resetLink = `http://127.0.0.1:5500/forgot-password-confirm.html?token=${token}`;

    await sendEmail(email, "Quên mật khẩu", `Truy cập link để đổi mật khẩu:\n${resetLink}`);

    return res.send({ success: true, message: "Đã gửi link đặt lại mật khẩu (demo).", data: resetLink });
  } catch (err) {
    console.error(err);
    return res.send({ success: false, message: "Lỗi hệ thống", data: null });
  }
});

app.post("/forgot-password-update", async (req, res) => {
  const { token, newPassword } = req.body;
  if (!token || !newPassword) return res.send({ success: false, message: "Token không hợp lệ.", data: false });

  try {
    const user = await db.oneOrNone("SELECT user_id FROM appuser WHERE reset_token = $1", [token]);
    if (!user) return res.send({ success: false, message: "Token không hợp lệ hoặc đã hết hạn.", data: false });

    // Hash new password
    let pwToStore = newPassword;
    try {
      const saltRounds = 10;
      pwToStore = await bcrypt.hash(newPassword, saltRounds);
    } catch (e) {
      console.warn("bcrypt hashing failed, storing plaintext (not recommended):", e);
    }

    await db.none("UPDATE appuser SET password = $1, reset_token = NULL WHERE user_id = $2", [pwToStore, user.user_id]);
    return res.send({ success: true, message: "Đổi mật khẩu thành công qua link.", data: true });
  } catch (err) {
    console.error(err);
    return res.send({ success: false, message: "Lỗi hệ thống", data: false });
  }
});

// ============ New endpoints that FE may need ============

// GET current user by token
app.get("/auth/me", authMiddleware, async (req, res) => {
  try {
    const uid = req.user.userId;
    const row = await db.oneOrNone(
      `SELECT u.user_id, u.username, u.email, r.role_name
       FROM appuser u JOIN role r ON u.role_id = r.role_id
       WHERE u.user_id = $1`,
      [uid]
    );
    if (!row) return res.send({ success: false, message: "User not found", data: null });

    const user = {
      id: String(row.user_id),
      name: row.username,
      username: row.username,
      email: row.email,
      verified: true,
      role: String(row.role_name).toUpperCase()
    };
    return res.send({ success: true, message: "OK", data: user });
  } catch (err) {
    console.error(err);
    return res.send({ success: false, message: "Lỗi hệ thống", data: null });
  }
});

// PUT /auth/profile  => body: { newName, newEmail, newUsername }
app.put("/auth/profile", authMiddleware, async (req, res) => {
  const { newName, newEmail, newUsername } = req.body;
  const uid = req.user.userId;
  if (!newName || !newEmail || !newUsername) {
    return res.send({ success: false, message: "Thiếu field", data: null });
  }
  try {
    // check uniqueness
    const conflict = await db.oneOrNone(
      `SELECT user_id FROM appuser WHERE (LOWER(email)=LOWER($1) OR LOWER(username)=LOWER($2)) AND user_id != $3`,
      [newEmail, newUsername, uid]
    );
    if (conflict) {
      return res.send({ success: false, message: "Email hoặc username đang được sử dụng bởi tài khoản khác.", data: null });
    }

    // DB doesn't have full_name column, update username & email only
    const row = await db.one(
      `UPDATE appuser SET username = $1, email = $2 WHERE user_id = $3 RETURNING user_id, username, email`,
      [newUsername.trim(), newEmail.trim(), uid]
    );

    const user = {
      id: String(row.user_id),
      name: row.username,
      username: row.username,
      email: row.email,
      verified: true
    };
    return res.send({ success: true, message: "Cập nhật thông tin thành công.", data: user });
  } catch (err) {
    console.error(err);
    return res.send({ success: false, message: "Lỗi hệ thống", data: null });
  }
});

// POST /auth/change-password => body: { oldPassword, newPassword }
app.post("/auth/change-password", authMiddleware, async (req, res) => {
  const { oldPassword, newPassword } = req.body;
  const uid = req.user.userId;
  if (!oldPassword || !newPassword) {
    return res.send({ success: false, message: "Thiếu field", data: false });
  }
  try {
    const row = await db.oneOrNone("SELECT password FROM appuser WHERE user_id = $1", [uid]);
    if (!row) return res.send({ success: false, message: "Không tìm thấy user.", data: false });

    let passwordOk = false;
    if (row.password && row.password.startsWith("$2b$")) {
      passwordOk = await bcrypt.compare(oldPassword, row.password);
    } else {
      passwordOk = oldPassword === row.password;
    }

    if (!passwordOk) return res.send({ success: false, message: "Mật khẩu cũ không chính xác.", data: false });

    let pwToStore = newPassword;
    try {
      pwToStore = await bcrypt.hash(newPassword, 10);
    } catch (e) {
      console.warn("bcrypt hashing failed, storing plaintext (not recommended):", e);
    }

    await db.none("UPDATE appuser SET password = $1 WHERE user_id = $2", [pwToStore, uid]);
    return res.send({ success: true, message: "Đổi mật khẩu thành công.", data: true });
  } catch (err) {
    console.error(err);
    return res.send({ success: false, message: "Lỗi hệ thống", data: false });
  }
});


// CREATE
app.post("/course", upload.single('courseAvatar'), async (req, res) => {
  try {
    const payload = req.body || {};

    // image: note DB column is `imageurl` (snake_case / lowercase)
    const imgSrc = req.file ? `/uploads/${req.file.filename}` : (payload.imageUrl || payload.imageurl || '');

    // required fields
    const { title, description, teacher } = payload;
    if (!title || !description || !teacher) {
      return res.status(400).send({ success: false, message: "Khong du thong tin: title/description/teacher required" });
    }

    // parse numeric fields safely
    const lectures = Number.isFinite(Number(payload.lectures)) ? parseInt(payload.lectures, 10) : 0;
    const students = Number.isFinite(Number(payload.students)) ? parseInt(payload.students, 10) : 0;
    const rating = payload.rating !== undefined ? parseFloat(payload.rating) : 0;
    const ratingcount = Number.isFinite(Number(payload.ratingCount || payload.ratingcount)) ? parseInt(payload.ratingCount || payload.ratingcount, 10) : 0;
    const total_duration_minutes = Number.isFinite(Number(payload.totalDurationMinutes || payload.total_duration_minutes)) ? parseInt(payload.totalDurationMinutes || payload.total_duration_minutes, 10) : 0;
    const price = payload.price !== undefined ? parseFloat(payload.price) : 0;

    // created_at: DB expects timestamp -> pass a JS Date or ISO string
    const created_at = payload.createdAt ? new Date(payload.createdAt) : new Date();

    // INSERT: *DO NOT* insert course_id if DB auto-generates integer PK (serial/bigserial)
    const inserted = await db.one(
      `INSERT INTO course(
         title, description, teacher, imageurl, category, lectures,
         students, rating, price, created_at, ratingcount, total_duration_minutes, is_approved
       ) VALUES(
         $1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13
       ) RETURNING *`,
      [
        title, description, teacher, imgSrc, payload.category || '',
        lectures, students, rating, price, created_at, ratingcount, total_duration_minutes, false
      ]
    );

    const result = transformCourseRow(inserted);
    res.send({ success: true, message: "Them moi thanh cong", data: result });
  } catch (error) {
    console.error(error);
    res.status(500).send({ success: false, message: "Lỗi tạo khóa học" });
  }
});

// READ list
app.get("/course", async (req, res) => {
  try {
    const { teacher } = req.query;
    let rows;
    if (teacher && teacher.trim() !== "") {
      rows = await db.any("SELECT * FROM course WHERE teacher = $1", [teacher]);
    } else {
      rows = await db.any("SELECT * FROM course");
    }
    const data = rows.map(transformCourseRow);
    res.send({ success: true, data });
  } catch (err) {
    console.error(err);
    res.status(500).send({ success: false, message: "Lỗi lấy danh sách khóa học" });
  }
});

// GET detail by id (course_id is integer)
app.get("/course/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id, 10);
    if (!Number.isFinite(id)) return res.status(400).send({ success: false, message: "Invalid course id" });

    const course = await db.oneOrNone("SELECT * FROM course WHERE course_id = $1", [id]);
    if (!course) {
      return res.status(404).send({ success: false, message: "Course not found" });
    }
    res.send({ success: true, data: transformCourseRow(course) });
  } catch (err) {
    console.error(err);
    res.status(500).send({ success: false, message: "Lỗi lấy chi tiết khóa học" });
  }
});

// UPDATE
app.patch("/course/:id", upload.single('courseAvatar'), async (req, res) => {
  try {
    const id = parseInt(req.params.id, 10);
    if (!Number.isFinite(id)) return res.status(400).send({ success: false, message: "Invalid course id" });

    const payload = req.body || {};
    const imgSrc = req.file ? `/uploads/${req.file.filename}` : (payload.imageUrl || payload.imageurl || null);

    // get existing row
    const existing = await db.oneOrNone("SELECT * FROM course WHERE course_id = $1", [id]);
    if (!existing) return res.status(404).send({ success: false, message: "Course not found" });

    // choose values: if payload provides value (not empty) use it; otherwise keep existing
    const title = (payload.title !== undefined && payload.title !== '') ? payload.title : existing.title;
    const description = (payload.description !== undefined && payload.description !== '') ? payload.description : existing.description;
    const teacher = payload.teacher !== undefined ? payload.teacher : existing.teacher;
    const category = payload.category !== undefined ? payload.category : existing.category;
    const lectures = payload.lectures !== undefined ? parseInt(payload.lectures, 10) : existing.lectures;
    const students = payload.students !== undefined ? parseInt(payload.students, 10) : existing.students;
    const rating = payload.rating !== undefined ? parseFloat(payload.rating) : existing.rating;
    const price = payload.price !== undefined ? parseFloat(payload.price) : existing.price;
    const created_at = payload.createdAt !== undefined ? new Date(payload.createdAt) : existing.created_at || existing.createdAt;
    const ratingcount = payload.ratingCount !== undefined ? parseInt(payload.ratingCount, 10) : (existing.ratingcount || existing.ratingCount || 0);
    const total_duration_minutes = payload.totalDurationMinutes !== undefined ? parseInt(payload.totalDurationMinutes, 10) : (existing.total_duration_minutes || existing.totalDurationMinutes || 0);

    const imageFinal = imgSrc !== null ? imgSrc : (existing.imageurl || existing.imageUrl || '');

    const updated = await db.one(
      `UPDATE course SET
         title=$1, description=$2, teacher=$3, category=$4, lectures=$5,
         students=$6, rating=$7, price=$8, created_at=$9, ratingcount=$10,
         total_duration_minutes=$11, imageurl=$12
       WHERE course_id=$13 RETURNING *`,
      [
        title, description, teacher, category, lectures,
        students, rating, price, created_at, ratingcount,
        total_duration_minutes, imageFinal, id
      ]
    );

    res.send({ success: true, data: transformCourseRow(updated) });
  } catch (err) {
    console.error(err);
    res.status(500).send({ success: false, message: "Lỗi khi cập nhật khóa học" });
  }
});

// DELETE
app.delete("/course/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id, 10);
    if (!Number.isFinite(id)) return res.status(400).send({ success: false, message: "Invalid course id" });

    const deletedCourse = await db.oneOrNone("DELETE FROM course WHERE course_id = $1 RETURNING *", [id]);
    if (!deletedCourse) return res.status(404).send({ success: false, message: "Course not found" });
    res.send({ success: true, data: transformCourseRow(deletedCourse) });
  } catch (err) {
    console.error(err);
    res.status(500).send({ success: false, message: "Lỗi khi xóa khóa học" });
  }
});

// RECORD PURCHASE (increment students)
app.post("/course/:id/purchase", async (req, res) => {
  try {
    const id = parseInt(req.params.id, 10);
    if (!Number.isFinite(id)) return res.status(400).send({ success: false, message: "Invalid course id" });

    const updated = await db.oneOrNone(
      `UPDATE course
       SET students = COALESCE(students,0) + 1
       WHERE course_id = $1
       RETURNING *`,
      [id]
    );

    if (!updated) {
      return res.status(404).send({ success: false, message: "Course not found" });
    }

    res.send({ success: true, data: transformCourseRow(updated) });
  } catch (err) {
    console.error(err);
    res.status(500).send({ success: false, message: "Lỗi khi ghi nhận mua" });
  }
});


app.listen(port, () => console.log(`Server listening on ${port}`));
