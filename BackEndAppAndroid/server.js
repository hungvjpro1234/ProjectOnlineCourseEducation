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
        rejectUnauthorized: false,
    },
};

const db = pgp(connection);

// ----------------- Cart helpers & enum check -----------------
/**
 * Lấy giá trị enum hiện có (dùng để đảm bảo enum tồn tại)
 * Trả mảng string.
 */
async function getEnumValues(enumName = "course_payment_status_enum") {
    const sql = `
      SELECT e.enumlabel
      FROM pg_type t
      JOIN pg_enum e ON t.oid = e.enumtypid
      WHERE t.typname = $1
      ORDER BY e.enumsortorder;
    `;
    const rows = await db.any(sql, [enumName]);
    return rows.map((r) => r.enumlabel);
}

// Allowed transitions (logical). Nếu enum DB khác tên, hơi thay đổi map này.

const allowedTransitions = {
    NOT_PURCHASED: ["IN_CART", "PURCHASED"],
    IN_CART: ["NOT_PURCHASED", "PURCHASED"], // ← thêm 'PURCHASED' ở đây
    PURCHASED: [],
};

/**
 * Lấy record course_payment_status cho user+course
 */
async function getCartRecord(userId, courseId) {
    return await db.oneOrNone(
        "SELECT * FROM course_payment_status WHERE user_id = $1 AND course_id = $2 LIMIT 1",
        [userId, courseId]
    );
}

/**
 * Upsert đơn giản: nếu tồn tại -> update status (và cập nhật snapshot nếu có),
 * nếu không -> insert record mới.
 */
async function upsertCartStatus(userId, courseId, status, extras = {}) {
    // Try update
    const updated = await db.oneOrNone(
        `UPDATE course_payment_status
       SET status=$1, price_snapshot = COALESCE($3, price_snapshot), course_name = COALESCE($4, course_name)
       WHERE user_id=$2 AND course_id=$5
       RETURNING *`,
        [
            status,
            userId,
            extras.price_snapshot || null,
            extras.course_name || null,
            courseId,
        ]
    );
    if (updated) return updated;

    // Insert
    const inserted = await db.one(
        `INSERT INTO course_payment_status (user_id, course_id, status, price_snapshot, quantity, course_name)
       VALUES($1,$2,$3,$4,$5,$6) RETURNING *`,
        [
            userId,
            courseId,
            status,
            extras.price_snapshot || null,
            extras.quantity || 1,
            extras.course_name || null,
        ]
    );
    return inserted;
}
// ----------------- end cart helpers -----------------

// --- Helper: normalize DB row to FE-friendly Course object ---
function safeParseJson(input) {
    if (!input) return [];
    if (Array.isArray(input)) return input;
    try {
        // If database stored a JSON string
        if (typeof input === "string") {
            return JSON.parse(input);
        }
        // already object
        return input;
    } catch (e) {
        // If it's a CSV string like "a,b,c" or simple string, try fallback
        if (typeof input === "string") {
            return input
                .split(",")
                .map((s) => s.trim())
                .filter((s) => s.length > 0);
        }
        return [];
    }
}

function transformCourseRow(row) {
    if (!row) return null;

    return {
        id: String(row.course_id),
        title: row.title || "",
        description: row.description || "",
        teacher: row.teacher || "",
        imageUrl: row.imageurl || "",
        category: row.category || "",
        lectures: Number(row.lectures || 0),
        students: Number(row.students || 0),
        rating: Number(row.rating || 0),
        price: Number(row.price || 0),
        createdAt: row.created_at || "",
        ratingCount: Number(row.ratingcount || 0),
        totalDurationMinutes: Number(row.totaldurationminutes || 0),

        skills: safeParseJson(row.skills),
        requirements: safeParseJson(row.requirements),

        // 🔥 QUAN TRỌNG
        initialApproved: !!row.is_approved,
        editApproved: !!row.is_edit_approved,
        deleteRequested: !!row.is_delete_requested,
    };
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

const path = require("path");
const fs = require("fs");
const multer = require("multer");

// tạo folder uploads nếu chưa có (đảm bảo có quyền ghi)
const uploadDir = path.join(__dirname, "uploads");
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
        const safeName =
            Date.now() + "-" + file.originalname.replace(/\s+/g, "_");
        cb(null, safeName);
    },
});

// filter file (tuỳ chọn): chấp nhận image thôi
const fileFilter = (req, file, cb) => {
    if (!file.mimetype.startsWith("image/")) {
        // reject non-image files
        return cb(null, false);
    }
    cb(null, true);
};

const upload = multer({ storage: storage, fileFilter: fileFilter });

// expose uploads static để client GET /uploads/filename
app.use("/uploads", express.static(uploadDir));
// ------------------ Helper để parse field có thể là JSON-array, CSV string, hoặc single value ------------------
function parseMaybeArrayField(value) {
    if (value === undefined || value === null) return [];
    if (Array.isArray(value)) return value;
    if (typeof value === "string") {
        const trimmed = value.trim();
        // JSON array string like '["a","b"]'
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                const parsed = JSON.parse(trimmed);
                return Array.isArray(parsed) ? parsed : [];
            } catch (e) {
                // fallthrough to try CSV
            }
        }
        // CSV: "A, B, C"
        if (trimmed.indexOf(",") >= 0) {
            return trimmed
                .split(",")
                .map((s) => s.trim())
                .filter((s) => s.length > 0);
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
        return res
            .status(401)
            .send({ success: false, message: "Không có token", data: null });
    }
    try {
        const payload = jwt.verify(token, secretKey);
        req.user = payload; // contains userId, role
        next();
    } catch (err) {
        return res.status(401).send({
            success: false,
            message: "Token không hợp lệ/đã hết hạn",
            data: null,
        });
    }
}

// ================= ADMIN: Get users by role =================
app.get("/admin/users", authMiddleware, async (req, res) => {
    try {
        // Check admin
        const role = req.user?.role?.toUpperCase();
        if (role !== "ADMIN") {
            return res.status(403).send({
                success: false,
                message: "Chỉ ADMIN mới có quyền truy cập",
            });
        }

        const { role: queryRole } = req.query;
        if (!queryRole) {
            return res.status(400).send({
                success: false,
                message: "Thiếu query param role",
            });
        }

        const rows = await db.any(
            `
            SELECT 
              u.user_id,
              u.username,
              u.email,
              u.created_at,
              r.role_name
            FROM appuser u
            JOIN role r ON u.role_id = r.role_id
            WHERE UPPER(r.role_name) = UPPER($1)
            ORDER BY u.created_at DESC
            `,
            [queryRole]
        );

        const users = rows.map((r) => ({
            id: String(r.user_id),
            name: r.username, // DB không có full_name
            username: r.username,
            email: r.email,
            verified: true, // DB không có cột verified
            avatar: null,
            role: r.role_name.toUpperCase(),
            createdAt: r.created_at,
        }));

        res.send({
            success: true,
            data: users,
        });
    } catch (err) {
        console.error("GET /admin/users error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi hệ thống",
        });
    }
});

// ADMIN: get purchased courses of a specific user
app.get("/admin/my-courses/:userId", authMiddleware, async (req, res) => {
    try {
        if (req.user.role !== "ADMIN") {
            return res.status(403).send({
                success: false,
                message: "Chỉ ADMIN",
            });
        }

        const userId = req.params.userId;

        const rows = await db.any(
            `
            SELECT c.*
            FROM course_student cs
            JOIN course c ON c.course_id = cs.course_id
            WHERE cs.user_id = $1
            ORDER BY cs.enrolled_at DESC
            `,
            [userId]
        );

        return res.send({
            success: true,
            data: rows.map(transformCourseRow),
        });
    } catch (err) {
        console.error("GET /admin/my-courses/:userId error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi lấy my courses cho user",
        });
    }
});


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
            return res.send({
                success: false,
                message: "Sai tài khoản/mật khẩu",
                data: null,
            });
        }

        const hashed = row.password;
        let passwordOk = false;
        if (hashed && hashed.startsWith("$2b$")) {
            passwordOk = await bcrypt.compare(password, hashed);
        } else {
            passwordOk = password === hashed;
        }

        if (!passwordOk) {
            return res.send({
                success: false,
                message: "Sai tài khoản/mật khẩu",
                data: null,
            });
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
            role: String(row.role_name).toUpperCase(),
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
            token,
        });
    } catch (err) {
        console.error(err);
        return res.send({
            success: false,
            message: "Lỗi hệ thống",
            data: null,
        });
    }
});

app.post("/signup", async (req, res) => {
    const { name, username, email, password, role } = req.body;

    if (!name || !username || !email || !password || !role) {
        return res.send({ success: false, message: "Thiếu field", data: null });
    }
    if (String(role).toUpperCase() === "ADMIN") {
        return res.send({
            success: false,
            message: "Không thể tự đăng ký ADMIN.",
            data: null,
        });
    }

    try {
        const existed = await db.oneOrNone(
            `SELECT username, email FROM appuser WHERE LOWER(username) = LOWER($1) OR LOWER(email) = LOWER($2)`,
            [username, email]
        );

        if (existed) {
            if (existed.email?.toLowerCase() === email.toLowerCase()) {
                return res.send({
                    success: false,
                    message: "Email đã tồn tại.",
                    data: null,
                });
            }
            if (existed.username?.toLowerCase() === username.toLowerCase()) {
                return res.send({
                    success: false,
                    message: "Username đã tồn tại.",
                    data: null,
                });
            }
        }

        // ← ===  <-- Đặt đoạn roleRow ở ngay đây (trước khi hash và insert)
        const roleRow = await db.oneOrNone(
            `SELECT role_id, role_name FROM role WHERE UPPER(role_name) = UPPER($1)`,
            [role]
        );

        if (!roleRow) {
            return res.send({
                success: false,
                message: "Role không hợp lệ",
                data: null,
            });
        }
        // === end roleRow

        // Hash password
        let pwToStore = password;
        try {
            const saltRounds = 10;
            pwToStore = await bcrypt.hash(password, saltRounds);
        } catch (e) {
            console.warn(
                "bcrypt hashing failed, storing plaintext (not recommended):",
                e
            );
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
            role: roleRow.role_name.toUpperCase(),
        };

        return res.send({
            success: true,
            message: "Đăng ký thành công. Bạn có thể đăng nhập.",
            data: user,
        });
    } catch (err) {
        console.error(err);
        return res.send({
            success: false,
            message: "Lỗi hệ thống",
            data: null,
        });
    }
});

app.post("/forgot-password-request", async (req, res) => {
    const { email } = req.body;
    if (!email)
        return res.send({
            success: false,
            message: "Email không có!",
            data: null,
        });

    try {
        const user = await db.oneOrNone(
            "SELECT user_id, email FROM appuser WHERE LOWER(email) = LOWER($1)",
            [email]
        );
        if (!user)
            return res.send({
                success: false,
                message: "Email không tồn tại trong hệ thống.",
                data: null,
            });

        const token = uuidv4();
        await db.none(
            "UPDATE appuser SET reset_token = $1 WHERE user_id = $2",
            [token, user.user_id]
        );

        // NOTE: link demo, update host as needed
        const resetLink = `http://127.0.0.1:5500/forgot-password-confirm.html?token=${token}`;

        await sendEmail(
            email,
            "Quên mật khẩu",
            `Truy cập link để đổi mật khẩu:\n${resetLink}`
        );

        return res.send({
            success: true,
            message: "Đã gửi link đặt lại mật khẩu (demo).",
            data: resetLink,
        });
    } catch (err) {
        console.error(err);
        return res.send({
            success: false,
            message: "Lỗi hệ thống",
            data: null,
        });
    }
});

app.post("/forgot-password-update", async (req, res) => {
    const { token, newPassword } = req.body;
    if (!token || !newPassword)
        return res.send({
            success: false,
            message: "Token không hợp lệ.",
            data: false,
        });

    try {
        const user = await db.oneOrNone(
            "SELECT user_id FROM appuser WHERE reset_token = $1",
            [token]
        );
        if (!user)
            return res.send({
                success: false,
                message: "Token không hợp lệ hoặc đã hết hạn.",
                data: false,
            });

        // Hash new password
        let pwToStore = newPassword;
        try {
            const saltRounds = 10;
            pwToStore = await bcrypt.hash(newPassword, saltRounds);
        } catch (e) {
            console.warn(
                "bcrypt hashing failed, storing plaintext (not recommended):",
                e
            );
        }

        await db.none(
            "UPDATE appuser SET password = $1, reset_token = NULL WHERE user_id = $2",
            [pwToStore, user.user_id]
        );
        return res.send({
            success: true,
            message: "Đổi mật khẩu thành công qua link.",
            data: true,
        });
    } catch (err) {
        console.error(err);
        return res.send({
            success: false,
            message: "Lỗi hệ thống",
            data: false,
        });
    }
});

// ============ New endpoints that FE may need ============

// ---------------------- Pending edits helpers ----------------------

// Insert or update pending edit for a course (teacher cập nhật)
async function upsertCoursePendingEdit(courseId, pendingData, createdBy) {
    // If exists -> replace pending_data & update created_at/status
    const existing = await db.oneOrNone(
        `SELECT * FROM course_pending_edits WHERE course_id = $1 ORDER BY created_at DESC LIMIT 1`,
        [courseId]
    );
    if (existing) {
        const updated = await db.one(
            `UPDATE course_pending_edits 
             SET pending_data = $1, created_by = $2, created_at = NOW(), status = 'PENDING'
             WHERE id = $3
             RETURNING *`,
            [pendingData, createdBy || null, existing.id]
        );
        return updated;
    } else {
        const inserted = await db.one(
            `INSERT INTO course_pending_edits(course_id, pending_data, created_by)
             VALUES($1,$2,$3) RETURNING *`,
            [courseId, pendingData, createdBy || null]
        );
        return inserted;
    }
}

async function getPendingEdit(courseId) {
    return await db.oneOrNone(
        `SELECT * FROM course_pending_edits WHERE course_id = $1 ORDER BY created_at DESC LIMIT 1`,
        [courseId]
    );
}

async function deletePendingEditById(id) {
    return await db.none(`DELETE FROM course_pending_edits WHERE id = $1`, [
        id,
    ]);
}

// ----------------- Helpers for lesson -> update course counters -----------------

/**
 * Parse duration string ("mm:ss" or "hh:mm:ss" or seconds) -> minutes (rounded)
 */
function parseDurationToMinutes(durationText) {
    if (!durationText) return 0;
    try {
        const parts = String(durationText)
            .split(":")
            .map((p) => parseInt(p, 10) || 0);
        let seconds = 0;
        if (parts.length === 2) {
            // mm:ss
            seconds = parts[0] * 60 + parts[1];
        } else if (parts.length === 3) {
            // hh:mm:ss
            seconds = parts[0] * 3600 + parts[1] * 60 + parts[2];
        } else {
            // plain number -> treat as seconds
            seconds = parts[0];
        }
        return Math.max(0, Math.round(seconds / 60));
    } catch (e) {
        return 0;
    }
}

function parseDurationToSeconds(duration) {
    if (!duration) return 0;

    // "mm:ss" | "hh:mm:ss"
    if (duration.includes(":")) {
        const parts = duration.split(":").map(Number);
        if (parts.length === 2) {
            return parts[0] * 60 + parts[1];
        }
        if (parts.length === 3) {
            return parts[0] * 3600 + parts[1] * 60 + parts[2];
        }
    }

    // numeric seconds
    return Number(duration) || 0;
}


/**
 * Add one lesson to course counters.
 * - courseId: integer
 * - durationText: string like "05:30" or "0:10:00" or "123" (seconds)
 */
async function addLessonToCourseDb(courseId, durationText) {
    const minutes = parseDurationToMinutes(durationText);
    await db.none(
        `UPDATE course
         SET lectures = COALESCE(lectures,0) + 1,
             totaldurationminutes = COALESCE(totaldurationminutes,0) + $1
         WHERE course_id = $2`,
        [minutes, courseId]
    );
}

/**
 * Remove one lesson from course counters (use when deleting a lesson).
 * - ensures values don't go below zero.
 */
async function removeLessonFromCourseDb(courseId, durationText) {
    const minutes = parseDurationToMinutes(durationText);
    await db.none(
        `UPDATE course
         SET lectures = GREATEST(COALESCE(lectures,0) - 1, 0),
             totaldurationminutes = GREATEST(COALESCE(totaldurationminutes,0) - $1, 0)
         WHERE course_id = $2`,
        [minutes, courseId]
    );
}

// List all courses that have pending edits or not approved (admin)
app.get("/course/pending", authMiddleware, async (req, res) => {
    try {
        // Optional: check admin role
        const role =
            req.user && req.user.role
                ? String(req.user.role).toUpperCase()
                : null;
        if (role !== "ADMIN")
            return res
                .status(403)
                .send({ success: false, message: "Chỉ admin" });

        // Get courses with is_approved=false OR is_edit_approved=false
        const rows = await db.any(
            `SELECT * FROM course WHERE is_approved = false OR is_edit_approved = false`
        );
        const data = rows.map(transformCourseRow);
        // Also attach pending edit if exists
        for (let c of data) {
            const pending = await getPendingEdit(c.id);
            c.pending = pending ? pending.pending_data : null;
        }
        res.send({ success: true, data });
    } catch (err) {
        console.error(err);
        res.status(500).send({
            success: false,
            message: "Lỗi lấy pending courses",
        });
    }
});

// Admin approve initial creation -> set is_approved = true and is_edit_approved = true
app.post("/course/:id/approve-initial", authMiddleware, async (req, res) => {
    try {
        const role =
            req.user && req.user.role
                ? String(req.user.role).toUpperCase()
                : null;
        if (role !== "ADMIN")
            return res
                .status(403)
                .send({ success: false, message: "Chỉ admin" });

        const courseId = parseInt(req.params.id, 10);
        if (!Number.isFinite(courseId))
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });

        const updated = await db.oneOrNone(
            `UPDATE course SET is_approved = true, is_edit_approved = true WHERE course_id = $1 RETURNING *`,
            [courseId]
        );
        if (!updated)
            return res
                .status(404)
                .send({ success: false, message: "Course not found" });

        res.send({
            success: true,
            message: "Đã duyệt khóa học",
            data: transformCourseRow(updated),
        });
    } catch (err) {
        console.error("POST /course/:id/approve-initial error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi duyệt khóa học",
        });
    }
});

app.post("/course/:id/reject-initial", authMiddleware, async (req, res) => {
    try {
        if (req.user.role !== "ADMIN") {
            return res
                .status(403)
                .send({ success: false, message: "Chỉ admin" });
        }

        const courseId = parseInt(req.params.id, 10);
        if (!Number.isFinite(courseId)) {
            return res
                .status(400)
                .send({ success: false, message: "Invalid id" });
        }

        const course = await db.oneOrNone(
            "SELECT * FROM course WHERE course_id=$1",
            [courseId]
        );
        if (!course) {
            return res
                .status(404)
                .send({ success: false, message: "Not found" });
        }

        if (course.is_approved) {
            return res.status(400).send({
                success: false,
                message: "Course đã được duyệt, không thể reject initial",
            });
        }

        await db.none("DELETE FROM course WHERE course_id=$1", [courseId]);
        await db.none("DELETE FROM course_pending_edits WHERE course_id=$1", [
            courseId,
        ]);

        res.send({ success: true, message: "Đã từ chối & xóa course" });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false, message: "Lỗi reject initial" });
    }
});

// Get pending edit for a course
app.get("/course/:id/pending", authMiddleware, async (req, res) => {
    try {
        const courseId = parseInt(req.params.id, 10);
        const pending = await getPendingEdit(courseId);
        if (!pending) return res.send({ success: true, data: null });
        res.send({ success: true, data: pending.pending_data, meta: pending });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false, message: "Lỗi" });
    }
});

// Approve pending edit (admin) -> apply pending_data to course row
app.post("/course/:id/approve-edit", authMiddleware, async (req, res) => {
    try {
        const role =
            req.user && req.user.role
                ? String(req.user.role).toUpperCase()
                : null;
        if (role !== "ADMIN")
            return res
                .status(403)
                .send({ success: false, message: "Chỉ admin" });

        const courseId = parseInt(req.params.id, 10);
        const pending = await getPendingEdit(courseId);
        if (!pending)
            return res
                .status(404)
                .send({ success: false, message: "No pending edit" });

        const pendingData = pending.pending_data || {};

        // Build SET list and values for UPDATE dynamically
        const setClauses = [];
        const values = [];
        let idx = 1;
        if (pendingData.title !== undefined) {
            setClauses.push(`title=$${idx++}`);
            values.push(pendingData.title);
        }
        if (pendingData.description !== undefined) {
            setClauses.push(`description=$${idx++}`);
            values.push(pendingData.description);
        }
        if (pendingData.teacher !== undefined) {
            setClauses.push(`teacher=$${idx++}`);
            values.push(pendingData.teacher);
        }
        if (pendingData.category !== undefined) {
            setClauses.push(`category=$${idx++}`);
            values.push(pendingData.category);
        }
        if (pendingData.lectures !== undefined) {
            setClauses.push(`lectures=$${idx++}`);
            values.push(pendingData.lectures);
        }
        if (pendingData.students !== undefined) {
            setClauses.push(`students=$${idx++}`);
            values.push(pendingData.students);
        }
        if (pendingData.rating !== undefined) {
            setClauses.push(`rating=$${idx++}`);
            values.push(pendingData.rating);
        }
        if (pendingData.price !== undefined) {
            setClauses.push(`price=$${idx++}`);
            values.push(pendingData.price);
        }
        if (pendingData.createdAt !== undefined) {
            setClauses.push(`created_at=$${idx++}`);
            values.push(new Date(pendingData.createdAt));
        }
        if (pendingData.ratingCount !== undefined) {
            setClauses.push(`ratingcount=$${idx++}`);
            values.push(pendingData.ratingCount);
        }
        if (pendingData.totalDurationMinutes !== undefined) {
            setClauses.push(`totaldurationminutes=$${idx++}`);
            values.push(pendingData.totalDurationMinutes);
        }
        if (pendingData.imageUrl !== undefined) {
            setClauses.push(`imageurl=$${idx++}`);
            values.push(pendingData.imageUrl);
        }
        if (pendingData.skills !== undefined) {
            setClauses.push(`skills=$${idx++}`);
            values.push(JSON.stringify(pendingData.skills));
        }
        if (pendingData.requirements !== undefined) {
            setClauses.push(`requirements=$${idx++}`);
            values.push(JSON.stringify(pendingData.requirements));
        }

        if (setClauses.length === 0) {
            // nothing to apply
            // mark edit approved anyway
            await db.none(
                `UPDATE course SET is_edit_approved = true WHERE course_id = $1`,
                [courseId]
            );
            await db.none(
                `UPDATE course_pending_edits SET status='APPROVED' WHERE id = $1`,
                [pending.id]
            );
            return res.send({
                success: true,
                message: "No fields to apply. Marked approved.",
            });
        }

        const sql = `UPDATE course SET ${setClauses.join(
            ", "
        )}, is_edit_approved = true WHERE course_id = $${idx} RETURNING *`;
        values.push(courseId);
        const updated = await db.one(sql, values);

        // mark pending as approved
        await db.none(
            `UPDATE course_pending_edits SET status='APPROVED' WHERE id = $1`,
            [pending.id]
        );

        res.send({ success: true, data: transformCourseRow(updated) });
    } catch (err) {
        console.error(err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi duyệt chỉnh sửa",
        });
    }
});

// Reject pending edit (admin)
app.post("/course/:id/reject-edit", authMiddleware, async (req, res) => {
    try {
        const role =
            req.user && req.user.role
                ? String(req.user.role).toUpperCase()
                : null;
        if (role !== "ADMIN")
            return res
                .status(403)
                .send({ success: false, message: "Chỉ admin" });

        const courseId = parseInt(req.params.id, 10);
        const pending = await getPendingEdit(courseId);
        if (!pending)
            return res
                .status(404)
                .send({ success: false, message: "No pending edit" });

        // delete pending or mark rejected
        await db.none(
            `UPDATE course_pending_edits SET status='REJECTED' WHERE id = $1`,
            [pending.id]
        );
        // reset is_edit_approved true (published stays)
        await db.none(
            `UPDATE course SET is_edit_approved = true WHERE course_id = $1`,
            [courseId]
        );

        res.send({ success: true, message: "Đã từ chối chỉnh sửa" });
    } catch (err) {
        console.error(err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi từ chối chỉnh sửa",
        });
    }
});

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
        if (!row)
            return res.send({
                success: false,
                message: "User not found",
                data: null,
            });

        const user = {
            id: String(row.user_id),
            name: row.username,
            username: row.username,
            email: row.email,
            verified: true,
            role: String(row.role_name).toUpperCase(),
        };
        return res.send({ success: true, message: "OK", data: user });
    } catch (err) {
        console.error(err);
        return res.send({
            success: false,
            message: "Lỗi hệ thống",
            data: null,
        });
    }
});

// ================= MY COURSES =================

// Get my purchased courses (current user)
app.get("/my-courses", authMiddleware, async (req, res) => {
    try {
        const userId = req.user.userId;

        const rows = await db.any(
            `
            SELECT c.*
            FROM course_student cs
            JOIN course c ON c.course_id = cs.course_id
            WHERE cs.user_id = $1
            ORDER BY cs.enrolled_at DESC
            `,
            [userId]
        );

        return res.send({
            success: true,
            data: rows.map(transformCourseRow),
        });
    } catch (err) {
        console.error("GET /my-courses error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi lấy khóa học đã mua",
        });
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
            return res.send({
                success: false,
                message:
                    "Email hoặc username đang được sử dụng bởi tài khoản khác.",
                data: null,
            });
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
            verified: true,
        };
        return res.send({
            success: true,
            message: "Cập nhật thông tin thành công.",
            data: user,
        });
    } catch (err) {
        console.error(err);
        return res.send({
            success: false,
            message: "Lỗi hệ thống",
            data: null,
        });
    }
});

// POST /auth/change-password => body: { oldPassword, newPassword }
app.post("/auth/change-password", authMiddleware, async (req, res) => {
    const { oldPassword, newPassword } = req.body;
    const uid = req.user.userId;
    if (!oldPassword || !newPassword) {
        return res.send({
            success: false,
            message: "Thiếu field",
            data: false,
        });
    }
    try {
        const row = await db.oneOrNone(
            "SELECT password FROM appuser WHERE user_id = $1",
            [uid]
        );
        if (!row)
            return res.send({
                success: false,
                message: "Không tìm thấy user.",
                data: false,
            });

        let passwordOk = false;
        if (row.password && row.password.startsWith("$2b$")) {
            passwordOk = await bcrypt.compare(oldPassword, row.password);
        } else {
            passwordOk = oldPassword === row.password;
        }

        if (!passwordOk)
            return res.send({
                success: false,
                message: "Mật khẩu cũ không chính xác.",
                data: false,
            });

        let pwToStore = newPassword;
        try {
            pwToStore = await bcrypt.hash(newPassword, 10);
        } catch (e) {
            console.warn(
                "bcrypt hashing failed, storing plaintext (not recommended):",
                e
            );
        }

        await db.none("UPDATE appuser SET password = $1 WHERE user_id = $2", [
            pwToStore,
            uid,
        ]);
        return res.send({
            success: true,
            message: "Đổi mật khẩu thành công.",
            data: true,
        });
    } catch (err) {
        console.error(err);
        return res.send({
            success: false,
            message: "Lỗi hệ thống",
            data: false,
        });
    }
});

// Check if current user purchased a course
app.get("/my-courses/:courseId/status", authMiddleware, async (req, res) => {
    try {
        const userId = req.user.userId;
        const courseId = parseInt(req.params.courseId, 10);

        const existed = await db.oneOrNone(
            `
            SELECT 1
            FROM course_student
            WHERE user_id = $1 AND course_id = $2
            `,
            [userId, courseId]
        );

        return res.send({
            success: true,
            purchased: !!existed,
        });
    } catch (err) {
        console.error("GET /my-courses/:id/status error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi kiểm tra trạng thái mua",
        });
    }
});


// CREATE
app.post("/course", upload.single("courseAvatar"), async (req, res) => {
    try {
        const payload = req.body || {};

        // image: note DB column is `imageurl` (snake_case / lowercase)
        const imageUrl = req.file
            ? `/uploads/${req.file.filename}`
            : "";


        // required fields
        const { title, description, teacher } = payload;
        if (!title || !description || !teacher) {
            return res.status(400).send({
                success: false,
                message:
                    "Khong du thong tin: title/description/teacher required",
            });
        }

        // parse numeric fields safely
        const lectures = Number.isFinite(Number(payload.lectures))
            ? parseInt(payload.lectures, 10)
            : 0;
        const students = Number.isFinite(Number(payload.students))
            ? parseInt(payload.students, 10)
            : 0;
        const rating =
            payload.rating !== undefined ? parseFloat(payload.rating) : 0;
        const ratingcount = Number.isFinite(
            Number(payload.ratingCount || payload.ratingcount)
        )
            ? parseInt(payload.ratingCount || payload.ratingcount, 10)
            : 0;
        const totaldurationminutes = Number.isFinite(
            Number(payload.totalDurationMinutes || payload.totaldurationminutes)
        )
            ? parseInt(
                  payload.totalDurationMinutes || payload.totaldurationminutes,
                  10
              )
            : 0;
        const price =
            payload.price !== undefined ? parseFloat(payload.price) : 0;

        // created_at: DB expects timestamp -> pass a JS Date or ISO string
        const created_at = new Date().toISOString();

        // INSERT: *DO NOT* insert course_id if DB auto-generates integer PK (serial/bigserial)
        const inserted = await db.one(
            `INSERT INTO course(
         title, description, teacher, imageurl, category, lectures,
         students, rating, price, created_at, ratingcount, totaldurationminutes, is_approved
       ) VALUES(
         $1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13
       ) RETURNING *`,
            [
                title,
                description,
                teacher,
                imageUrl,
                payload.category || "",
                lectures,
                students,
                rating,
                price,
                created_at,
                ratingcount,
                totaldurationminutes,
                false,
            ]
        );

        const result = transformCourseRow(inserted);
        res.send({
            success: true,
            message: "Them moi thanh cong",
            data: result,
        });
    } catch (error) {
        console.error(error);
        res.status(500).send({ success: false, message: "Lỗi tạo khóa học" });
    }
});

app.get("/course", async (req, res) => {
    try {
        const {
            teacher,
            query,
            sort = "AZ",
            limit,
            include_unapproved,
        } = req.query;

        let where = [];
        let values = [];
        let idx = 1;

        // chỉ course đã duyệt (student)
        if (include_unapproved !== "true") {
            where.push(`is_approved = true`);
        }

        if (teacher) {
            where.push(`LOWER(teacher) = LOWER($${idx++})`);
            values.push(teacher);
        }

        if (query) {
            where.push(`(
        LOWER(title) LIKE LOWER($${idx})
        OR LOWER(teacher) LIKE LOWER($${idx})
      )`);
            values.push(`%${query}%`);
            idx++;
        }

        let orderBy = "ORDER BY title ASC";
        switch (sort) {
            case "ZA":
                orderBy = "ORDER BY title DESC";
                break;
            case "RATING_UP":
                orderBy = "ORDER BY rating ASC";
                break;
            case "RATING_DOWN":
                orderBy = "ORDER BY rating DESC";
                break;
        }

        let limitSql = "";
        if (limit && Number.isFinite(+limit)) {
            limitSql = `LIMIT ${parseInt(limit, 10)}`;
        }

        const sql = `
      SELECT * FROM course
      ${where.length ? "WHERE " + where.join(" AND ") : ""}
      ${orderBy}
      ${limitSql}
    `;

        const rows = await db.any(sql, values);
        res.send({ success: true, data: rows.map(transformCourseRow) });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false, message: "Lỗi lấy course" });
    }
});

app.get("/course/:id/related", async (req, res) => {
    try {
        const courseId = parseInt(req.params.id, 10);
        if (!Number.isFinite(courseId)) {
            return res
                .status(400)
                .send({ success: false, message: "Invalid id" });
        }

        const base = await db.oneOrNone(
            "SELECT * FROM course WHERE course_id=$1 AND is_approved=true",
            [courseId]
        );
        if (!base) {
            return res
                .status(404)
                .send({ success: false, message: "Course not found" });
        }

        const rows = await db.any(
            `
      SELECT * FROM course
      WHERE is_approved = true
        AND course_id != $1
        AND (
          LOWER(teacher) = LOWER($2)
          OR category && string_to_array($3, ',')
        )
      `,
            [courseId, base.teacher, base.category]
        );

        res.send({
            success: true,
            data: rows.map(transformCourseRow),
        });
    } catch (err) {
        console.error(err);
        res.status(500).send({
            success: false,
            message: "Lỗi related courses",
        });
    }
});

app.get("/course/:id", async (req, res) => {
    try {
        const id = parseInt(req.params.id, 10);
        if (!Number.isFinite(id))
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });

        const include_pending = req.query.include_pending === "true";
        const course = await db.oneOrNone(
            "SELECT * FROM course WHERE course_id = $1",
            [id]
        );
        if (!course)
            return res
                .status(404)
                .send({ success: false, message: "Course not found" });

        const result = transformCourseRow(course);
        if (include_pending) {
            const pending = await getPendingEdit(id);
            result.pending = pending ? pending.pending_data : null;
        }
        res.send({ success: true, data: result });
    } catch (err) {
        console.error(err);
        res.status(500).send({
            success: false,
            message: "Lỗi lấy chi tiết khóa học",
        });
    }
});

// UPDATE -> create/update pending edit (teacher chỉnh)
app.patch(
    "/course/:id",
    upload.single("courseAvatar"),
    authMiddleware,
    async (req, res) => {
        try {
            const id = parseInt(req.params.id, 10);
            if (!Number.isFinite(id))
                return res
                    .status(400)
                    .send({ success: false, message: "Invalid course id" });

            const payload = req.body || {};
            // Build pending object from provided fields (only keep changed fields)
            const pending = {};

            // include image if new upload
            if (req.file) {
                pending.imageUrl = `/uploads/${req.file.filename}`;
            } else if (payload.imageUrl || payload.imageurl) {
                pending.imageUrl = payload.imageUrl || payload.imageurl;
            }

            // copy only fields provided (teacher may send partial)
            const optionalFields = [
                "title",
                "description",
                "teacher",
                "category",
                "lectures",
                "students",
                "rating",
                "price",
                "createdAt",
                "ratingCount",
                "totalDurationMinutes",
                "skills",
                "requirements",
            ];
            optionalFields.forEach((k) => {
                if (payload[k] !== undefined && payload[k] !== "") {
                    // try parse numeric fields
                    if (
                        [
                            "lectures",
                            "students",
                            "ratingCount",
                            "totalDurationMinutes",
                        ].includes(k)
                    ) {
                        pending[k] = Number.isFinite(Number(payload[k]))
                            ? Number(payload[k])
                            : payload[k];
                    } else if (k === "rating" || k === "price") {
                        pending[k] =
                            payload[k] !== undefined
                                ? parseFloat(payload[k])
                                : payload[k];
                    } else if (k === "skills" || k === "requirements") {
                        // accept JSON array string or CSV -> use parseMaybeArrayField helper
                        pending[k] = parseMaybeArrayField(payload[k]);
                    } else {
                        pending[k] = payload[k];
                    }
                }
            });

            // ensure pending not empty
            if (Object.keys(pending).length === 0) {
                return res
                    .status(400)
                    .send({
                        success: false,
                        message: "Không có thay đổi được gửi lên",
                    });
            }

            // Ensure course exists
            const existing = await db.oneOrNone(
                "SELECT * FROM course WHERE course_id = $1",
                [id]
            );
            if (!existing)
                return res
                    .status(404)
                    .send({ success: false, message: "Course not found" });

            // Save pending
            const userId = req.user ? req.user.userId : null;
            const saved = await upsertCoursePendingEdit(id, pending, userId);

            // Mark course as having pending edit (is_edit_approved = false)
            await db.none(
                `UPDATE course SET is_edit_approved = false WHERE course_id = $1`,
                [id]
            );

            return res.send({
                success: true,
                message: "Thay đổi đã được lưu chờ duyệt",
                data: saved,
            });
        } catch (err) {
            console.error(err);
            res.status(500).send({
                success: false,
                message: "Lỗi khi lưu thay đổi",
            });
        }
    }
);

// GET students of a course (teacher/admin)
app.get("/course/:id/students", authMiddleware, async (req, res) => {
    try {
        const courseId = parseInt(req.params.id, 10);
        if (!Number.isFinite(courseId))
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });

        // If course_student table doesn't exist, return empty list
        const rows = await db.any(
            `SELECT cs.user_id, cs.enrolled_at, u.username as name, u.email
       FROM course_student cs
       LEFT JOIN appuser u ON u.user_id = cs.user_id
       WHERE cs.course_id = $1
       ORDER BY cs.enrolled_at DESC`,
            [courseId]
        );

        res.send({ success: true, data: rows });
    } catch (err) {
        console.error("GET /course/:id/students error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi lấy danh sách học viên",
        });
    }
});

// Enroll student into course (after PURCHASED)
app.post("/course/:id/enroll", authMiddleware, async (req, res) => {
    try {
        const courseId = parseInt(req.params.id, 10);
        const userId = req.user.userId;

        if (!Number.isFinite(courseId)) {
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });
        }

        // check already enrolled
        const existed = await db.oneOrNone(
            `SELECT 1 FROM course_student WHERE course_id = $1 AND user_id = $2`,
            [courseId, userId]
        );

        if (existed) {
            return res.send({
                success: true,
                message: "User already enrolled",
            });
        }

        await db.none(
            `INSERT INTO course_student(course_id, user_id, enrolled_at)
       VALUES($1,$2,NOW())`,
            [courseId, userId]
        );

        res.send({
            success: true,
            message: "Enroll thành công",
        });
    } catch (err) {
        console.error("POST /course/:id/enroll error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi enroll học viên",
        });
    }
});

// Recalculate rating for a course from course_review table (admin or background job)
app.post("/course/:id/recalculate-rating", authMiddleware, async (req, res) => {
    try {
        const courseId = parseInt(req.params.id, 10);
        if (!Number.isFinite(courseId))
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });

        // optional auth check (allow admin or system)
        // const role = (req.user && req.user.role) ? String(req.user.role).toUpperCase() : null;
        // if (role !== "ADMIN") return res.status(403).send({ success:false, message:"Chỉ admin" });

        // aggregate from course_review table if exists
        const stats = await db.oneOrNone(
            `SELECT COUNT(*)::int as cnt, COALESCE(AVG(rating),0)::float as avg
       FROM course_review WHERE course_id = $1`,
            [courseId]
        );

        if (!stats || stats.cnt === 0) {
            // no reviews -> set 0 or skip update
            await db.none(
                `UPDATE course SET rating = 0, ratingcount = 0 WHERE course_id = $1`,
                [courseId]
            );
            return res.send({
                success: true,
                message: "Rating reset to 0 (no reviews)",
                data: { rating: 0, ratingCount: 0 },
            });
        }

        await db.none(
            `UPDATE course SET rating = $1, ratingcount = $2 WHERE course_id = $3`,
            [stats.avg, stats.cnt, courseId]
        );

        const updated = await db.one(
            `SELECT * FROM course WHERE course_id = $1`,
            [courseId]
        );
        res.send({ success: true, data: transformCourseRow(updated) });
    } catch (err) {
        console.error("POST /course/:id/recalculate-rating error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi tính lại rating",
        });
    }
});

// === REPLACE existing DELETE /course/:id handler with "request delete" + admin approve/reject ===

// Teacher requests delete (soft request) -> mark delete_requested & set is_edit_approved = false
app.post("/course/:id/request-delete", authMiddleware, async (req, res) => {
    try {
        const courseId = parseInt(req.params.id, 10);
        if (!Number.isFinite(courseId))
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });

        // Optionally: check permission: teacher owns course or admin can request on behalf
        // Here we just mark request-delete
        await db.none(
            `UPDATE course SET is_delete_requested = true, is_edit_approved = false WHERE course_id = $1`,
            [courseId]
        );

        res.send({
            success: true,
            message: "Yêu cầu xóa đã được ghi nhận. Chờ admin duyệt.",
        });
    } catch (err) {
        console.error("POST /course/:id/request-delete error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi gửi yêu cầu xóa",
        });
    }
});

// Admin approves & permanently deletes the course
app.post("/course/:id/approve-delete", authMiddleware, async (req, res) => {
    try {
        const role =
            req.user && req.user.role
                ? String(req.user.role).toUpperCase()
                : null;
        if (role !== "ADMIN")
            return res
                .status(403)
                .send({ success: false, message: "Chỉ admin" });

        const courseId = parseInt(req.params.id, 10);
        if (!Number.isFinite(courseId))
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });

        const deleted = await db.oneOrNone(
            "DELETE FROM course WHERE course_id = $1 RETURNING *",
            [courseId]
        );
        if (!deleted)
            return res
                .status(404)
                .send({ success: false, message: "Course not found" });

        // Also cleanup pending edits
        await db.none("DELETE FROM course_pending_edits WHERE course_id = $1", [
            courseId,
        ]);

        res.send({
            success: true,
            message: "Course permanently deleted",
            data: transformCourseRow(deleted),
        });
    } catch (err) {
        console.error("POST /course/:id/approve-delete error", err);
        res.status(500).send({ success: false, message: "Lỗi khi duyệt xóa" });
    }
});

// Admin rejects delete request -> clear flag, set edit_approved true to restore
app.post("/course/:id/reject-delete", authMiddleware, async (req, res) => {
    try {
        const role =
            req.user && req.user.role
                ? String(req.user.role).toUpperCase()
                : null;
        if (role !== "ADMIN")
            return res
                .status(403)
                .send({ success: false, message: "Chỉ admin" });

        const courseId = parseInt(req.params.id, 10);
        if (!Number.isFinite(courseId))
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });

        await db.none(
            `UPDATE course SET is_delete_requested = false, is_edit_approved = true WHERE course_id = $1`,
            [courseId]
        );

        res.send({ success: true, message: "Yêu cầu xóa đã bị từ chối" });
    } catch (err) {
        console.error("POST /course/:id/reject-delete error", err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi từ chối xóa",
        });
    }
});

// RECORD PURCHASE (increment students)
app.post("/course/:id/purchase", async (req, res) => {
    try {
        const id = parseInt(req.params.id, 10);
        if (!Number.isFinite(id))
            return res
                .status(400)
                .send({ success: false, message: "Invalid course id" });

        const updated = await db.oneOrNone(
            `UPDATE course
       SET students = COALESCE(students,0) + 1
       WHERE course_id = $1
       RETURNING *`,
            [id]
        );

        if (!updated) {
            return res
                .status(404)
                .send({ success: false, message: "Course not found" });
        }

        res.send({ success: true, data: transformCourseRow(updated) });
    } catch (err) {
        console.error(err);
        res.status(500).send({
            success: false,
            message: "Lỗi khi ghi nhận mua",
        });
    }
});

// ----------------- Cart endpoints -----------------

// Lấy toàn bộ giỏ hàng của user
// GET /cart/:userId
app.get("/cart/:userId", async (req, res) => {
    const rows = await db.any(`
        SELECT
          cps.course_id,
          cps.status,
          cps.price_snapshot,
          cps.quantity,
          cps.course_name,

          c.title,
          c.imageurl,
          c.price,
          c.teacher,
          c.rating
        FROM course_payment_status cps
        JOIN course c ON c.course_id = cps.course_id
        WHERE cps.user_id = $1
        ORDER BY cps.created_at DESC
    `, [req.params.userId]);

    const items = rows.map(r => ({
        courseId: r.course_id,
        status: r.status,
        priceSnapshot: r.price_snapshot,
        quantity: r.quantity,
        courseName: r.course_name,
        course: {
            id: String(r.course_id),
            title: r.title,
            imageUrl: r.imageurl,
            price: r.price,
            teacher: r.teacher,
            rating: r.rating
        }
    }));

    res.send({ success: true, data: items });
});



// Add to cart (thêm vào giỏ bật trạng thái IN_CART)
// POST /cart/add  body: { userId, courseId, price_snapshot?, course_name? }
app.post("/cart/add", async (req, res) => {
    const { userId, courseId, price_snapshot, course_name } = req.body;
    if (!userId || !courseId)
        return res
            .status(400)
            .send({ success: false, message: "userId và courseId bắt buộc" });

    try {
        const enumVals = await getEnumValues();
        if (!enumVals.includes("IN_CART")) {
            return res.status(500).send({
                success: false,
                message: "Enum không có IN_CART",
            });
        }

        const rec = await getCartRecord(userId, courseId);
        if (!rec) {
            const created = await upsertCartStatus(
                userId,
                courseId,
                "IN_CART",
                { price_snapshot, course_name }
            );
            return res.send({
                success: true,
                message: "Added to cart",
                data: created,
            });
        }

        if (rec.status === "PURCHASED") {
            return res.status(400).send({
                success: false,
                message: "Đã thanh toán, không thể add",
            });
        }
        if (rec.status === "IN_CART") {
            return res.send({
                success: true,
                message: "Đã có trong giỏ",
                data: rec,
            });
        }
        // từ NOT_PURCHASED -> IN_CART
        if (
            allowedTransitions[rec.status] &&
            allowedTransitions[rec.status].includes("IN_CART")
        ) {
            const updated = await upsertCartStatus(
                userId,
                courseId,
                "IN_CART",
                { price_snapshot, course_name }
            );
            return res.send({
                success: true,
                message: "Chuyển sang IN_CART",
                data: updated,
            });
        } else {
            return res.status(400).send({
                success: false,
                message: `Không thể chuyển ${rec.status} -> IN_CART`,
            });
        }
    } catch (err) {
        console.error("POST /cart/add error", err);
        return res
            .status(500)
            .send({ success: false, message: "Lỗi server khi add" });
    }
});

// Remove from cart (revert về NOT_PURCHASED)
// POST /cart/remove  body: { userId, courseId }
app.post("/cart/remove", async (req, res) => {
    const { userId, courseId } = req.body;
    if (!userId || !courseId)
        return res
            .status(400)
            .send({ success: false, message: "userId và courseId bắt buộc" });

    try {
        const rec = await getCartRecord(userId, courseId);
        if (!rec)
            return res
                .status(404)
                .send({ success: false, message: "Không tìm thấy record" });

        // Nếu đã PURCHASED -> không thể remove khỏi giỏ (đã mua rồi)
        if (rec.status === "PURCHASED")
            return res.status(400).send({
                success: false,
                message: "Không thể remove khóa học đã thanh toán",
            });

        // Nếu đang ở IN_CART -> revert về NOT_PURCHASED (giữ record để lưu price_snapshot nếu cần)
        if (rec.status === "IN_CART") {
            const updated = await upsertCartStatus(
                userId,
                courseId,
                "NOT_PURCHASED",
                {}
            );
            return res.send({
                success: true,
                message: "Đã remove khỏi giỏ",
                data: updated,
            });
        }

        // Các trạng thái khác (ví dụ NOT_PURCHASED) -> không có gì để remove
        return res.send({
            success: true,
            message: "Khóa học không nằm trong giỏ",
            data: rec,
        });
    } catch (err) {
        console.error("POST /cart/remove error", err);
        return res
            .status(500)
            .send({ success: false, message: "Lỗi server khi remove" });
    }
});

// Checkout (thanh toán) - chuyển status -> PURCHASED cho list khóa học
// POST /cart/checkout  body: { userId, courseIds: [1,2,3] }
app.post("/cart/checkout", async (req, res) => {
    const { userId, courseIds } = req.body;
    if (!userId || !Array.isArray(courseIds) || courseIds.length === 0)
        return res
            .status(400)
            .send({ success: false, message: "userId và courseIds bắt buộc" });

    try {
        const enumVals = await getEnumValues();
        if (!enumVals.includes("PURCHASED")) {
            return res
                .status(500)
                .send({ success: false, message: "Enum không có PURCHASED" });
        }

        // Dùng tx để atomic
        const results = await db.tx(async (t) => {
            const out = [];
            for (const cid of courseIds) {
                // lock bằng SELECT FOR UPDATE equivalent không trực tiếp trên pg-promise; ta dùng SELECT + UPDATE trong tx
                const rec = await t.oneOrNone(
                    "SELECT * FROM course_payment_status WHERE user_id=$1 AND course_id=$2",
                    [userId, cid]
                );
                if (!rec) {
                    // insert mới với PURCHASED (mua trực tiếp)
                    const ins = await t.one(
                        `INSERT INTO course_payment_status (user_id, course_id, status) VALUES($1,$2,$3) RETURNING *`,
                        [userId, cid, "PURCHASED"]
                    );

                    // enroll user to course
                    await t.none(
                        `INSERT INTO course_student(course_id, user_id, enrolled_at)
   VALUES($1,$2,NOW())
   ON CONFLICT DO NOTHING`,
                        [cid, userId]
                    );

                    // increment students
                    await t.none(
                        `UPDATE course
   SET students = COALESCE(students,0) + 1
   WHERE course_id = $1`,
                        [cid]
                    );

                    out.push({
                        courseId: cid,
                        note: "Inserted as PURCHASED",
                        item: ins,
                    });
                    continue;
                }
                if (rec.status === "PURCHASED") {
                    out.push({
                        courseId: cid,
                        note: "Already PURCHASED",
                        item: rec,
                    });
                    continue;
                }
                if (
                    allowedTransitions[rec.status] &&
                    allowedTransitions[rec.status].includes("PURCHASED")
                ) {
                    const upd = await t.one(
                        `UPDATE course_payment_status SET status=$1 WHERE user_id=$2 AND course_id=$3 RETURNING *`,
                        ["PURCHASED", userId, cid]
                    );
                    out.push({
                        courseId: cid,
                        note: "Set to PURCHASED",
                        item: upd,
                    });
                } else {
                    out.push({
                        courseId: cid,
                        note: `Cannot transition ${rec.status} -> PURCHASED`,
                        item: rec,
                    });
                }
            }
            return out;
        });

        return res.send({ success: true, results });
    } catch (err) {
        console.error("POST /cart/checkout error", err);
        return res
            .status(500)
            .send({ success: false, message: "Lỗi khi thanh toán" });
    }
});

// Lấy trạng thái 1 course cho 1 user (dùng FE hiển thị: NOT_PURCHASED / IN_CART / PURCHASED)
// GET /course/:userId/:courseId/status
app.get("/course/:userId/:courseId/status", async (req, res) => {
    const { userId, courseId } = req.params;
    try {
        const rec = await getCartRecord(userId, courseId);
        if (!rec) {
            // không có record -> coi như NOT_PURCHASED
            return res.send({ success: true, status: "NOT_PURCHASED" });
        }
        return res.send({ success: true, status: rec.status, item: rec });
    } catch (err) {
        console.error("GET /course/:userId/:courseId/status error", err);
        return res.status(500).send({ success: false, message: "Lỗi server" });
    }
});
// ----------------- end cart endpoints -----------------

function transformLessonRow(row) {
    if (!row) return null;
    return {
        id: String(row.lesson_id),
        courseId: String(row.course_id),
        title: row.title,
        description: row.description || "",
        videoUrl: row.video_url || "",
        duration: row.duration || "",
        order: row.lesson_order || 0,

        initialApproved: !!row.is_initial_approved,
        editApproved: !!row.is_edit_approved,
        deleteRequested: !!row.is_delete_requested,
    };
}

app.get("/lesson/course/:courseId", authMiddleware, async (req, res) => {
    try {
        const courseId = parseInt(req.params.courseId, 10);
        if (!Number.isFinite(courseId)) {
            return res.status(400).send({ success: false });
        }

        const role = String(req.user.role || "").toUpperCase();
        const isStudent = role === "STUDENT";

        const rows = await db.any(
            `
            SELECT * FROM lesson
            WHERE course_id = $1
            ${isStudent ? "AND is_initial_approved = true AND is_delete_requested = false" : ""}
            ORDER BY lesson_order ASC
            `,
            [courseId]
        );

        res.send({
            success: true,
            data: rows.map(transformLessonRow),
        });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false });
    }
});

app.post("/lesson", authMiddleware, async (req, res) => {
    try {
        const { courseId, title, description, videoUrl, duration, order } = req.body;
        if (!courseId || !title) {
            return res.status(400).send({ success: false });
        }

        const course = await db.oneOrNone(
            "SELECT is_approved FROM course WHERE course_id = $1",
            [courseId]
        );
        if (!course) {
            return res.status(404).send({ success: false });
        }

        const isInitialApproved = course.is_approved === false ? false : false;

        const row = await db.one(
            `
            INSERT INTO lesson
            (course_id, title, description, video_url, duration, lesson_order,
             is_initial_approved, is_edit_approved, is_delete_requested)
            VALUES($1,$2,$3,$4,$5,$6,$7,false,false)
            RETURNING *
            `,
            [
                courseId,
                title,
                description || "",
                videoUrl || "",
                duration || "",
                order || 0,
                isInitialApproved,
            ]
        );

        res.send({ success: true, data: transformLessonRow(row) });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false });
    }
});

app.patch("/lesson/:id", authMiddleware, async (req, res) => {
    try {
        const lessonId = parseInt(req.params.id, 10);
        const pendingData = req.body;

        await db.none(
            `INSERT INTO lesson_pending_edit(lesson_id, pending_data)
             VALUES($1,$2)
             ON CONFLICT DO NOTHING`,
            [lessonId, pendingData]
        );

        await db.none(
            `UPDATE lesson SET is_edit_approved = false WHERE lesson_id = $1`,
            [lessonId]
        );

        res.send({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false });
    }
});

app.post("/lesson/:id/approve-edit", authMiddleware, async (req, res) => {
    try {
        const lessonId = parseInt(req.params.id, 10);

        const pending = await db.oneOrNone(
            "SELECT * FROM lesson_pending_edit WHERE lesson_id = $1",
            [lessonId]
        );
        if (!pending) return res.send({ success: true });

        const p = pending.pending_data;

        await db.tx(async (t) => {
            await t.none(
                `
                UPDATE lesson SET
                    title = COALESCE($1,title),
                    description = COALESCE($2,description),
                    video_url = COALESCE($3,video_url),
                    duration = COALESCE($4,duration),
                    lesson_order = COALESCE($5,lesson_order),
                    is_edit_approved = true
                WHERE lesson_id = $6
                `,
                [
                    p.title,
                    p.description,
                    p.videoUrl,
                    p.duration,
                    p.order,
                    lessonId,
                ]
            );

            await t.none(
                "DELETE FROM lesson_pending_edit WHERE lesson_id = $1",
                [lessonId]
            );
        });

        res.send({ success: true });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false });
    }
});

app.get("/lesson/:lessonId/progress", authMiddleware, async (req, res) => {
    try {
        const userId = req.user.userId;
        const lessonId = parseInt(req.params.lessonId, 10);

        const progress = await db.oneOrNone(
            `
            SELECT * FROM lesson_progress
            WHERE user_id = $1 AND lesson_id = $2
            `,
            [userId, lessonId]
        );

        if (progress) {
            return res.send({ success: true, data: progress });
        }

        // chưa có → tạo default
        const lesson = await db.one(
            "SELECT lesson_id, course_id, duration FROM lesson WHERE lesson_id = $1",
            [lessonId]
        );

        const totalSecond = parseDurationToSeconds(lesson.duration);

        const inserted = await db.one(
            `
            INSERT INTO lesson_progress
            (user_id, lesson_id, course_id, total_second)
            VALUES($1,$2,$3,$4)
            RETURNING *
            `,
            [userId, lessonId, lesson.course_id, totalSecond]
        );

        res.send({ success: true, data: inserted });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false });
    }
});

app.post("/lesson/:lessonId/progress", authMiddleware, async (req, res) => {
    try {
        const userId = req.user.userId;
        const lessonId = parseInt(req.params.lessonId, 10);
        const { currentSecond } = req.body;

        const row = await db.one(
            `
            SELECT lp.*, l.duration
            FROM lesson_progress lp
            JOIN lesson l ON l.lesson_id = lp.lesson_id
            WHERE lp.user_id = $1 AND lp.lesson_id = $2
            `,
            [userId, lessonId]
        );

        const totalSecond =
            row.total_second > 0
                ? row.total_second
                : parseDurationToSeconds(row.duration);

        const bestCurrent = Math.max(row.current_second, currentSecond);
        let percent = totalSecond > 0
            ? Math.floor((bestCurrent / totalSecond) * 100)
            : 0;

        percent = Math.min(100, Math.max(0, percent));
        const completed = percent >= 90 || row.is_completed;

        const updated = await db.one(
            `
            UPDATE lesson_progress
            SET current_second=$1,
                total_second=$2,
                progress_percent=$3,
                is_completed=$4,
                last_watched=NOW()
            WHERE progress_id=$5
            RETURNING *
            `,
            [bestCurrent, totalSecond, percent, completed, row.progress_id]
        );

        res.send({ success: true, data: updated });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false });
    }
});

app.post("/lesson/:lessonId/complete", authMiddleware, async (req, res) => {
    try {
        const userId = req.user.userId;
        const lessonId = parseInt(req.params.lessonId, 10);

        const updated = await db.one(
            `
            UPDATE lesson_progress
            SET progress_percent=100,
                is_completed=true,
                last_watched=NOW()
            WHERE user_id=$1 AND lesson_id=$2
            RETURNING *
            `,
            [userId, lessonId]
        );

        res.send({ success: true, data: updated });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false });
    }
});

app.get("/course/:courseId/progress", authMiddleware, async (req, res) => {
    try {
        const userId = req.user.userId;
        const courseId = parseInt(req.params.courseId, 10);

        const rows = await db.any(
            `
            SELECT progress_percent
            FROM lesson_progress
            WHERE user_id=$1 AND course_id=$2
            `,
            [userId, courseId]
        );

        if (rows.length === 0) {
            return res.send({ success: true, progress: 0 });
        }

        const avg =
            rows.reduce((s, r) => s + r.progress_percent, 0) / rows.length;

        res.send({
            success: true,
            progress: Math.floor(avg),
        });
    } catch (err) {
        console.error(err);
        res.status(500).send({ success: false });
    }
});


app.listen(port, () => console.log(`Server listening on ${port}`));
