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

    const roleRow = await db.one(
      `SELECT role_id, role_name FROM role WHERE UPPER(role_name) = UPPER($1)`,
      [role]
    );

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

app.listen(port, () => console.log(`Server listening on ${port}`));
