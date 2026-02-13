const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('./database');

const app = express();
const PORT = process.env.PORT || 3000;
const SECRET_KEY = process.env.SECRET_KEY || 'your_super_secret_key_change_me';

app.use(cors());
app.use(bodyParser.json());

// --- HELPERS ---
const runQuery = (query, params) => {
    return new Promise((resolve, reject) => {
        db.run(query, params, function (err) {
            if (err) reject(err);
            else resolve(this);
        });
    });
};

const getQuery = (query, params) => {
    return new Promise((resolve, reject) => {
        db.get(query, params, (err, row) => {
            if (err) reject(err);
            else resolve(row);
        });
    });
};

const logAudit = async (userId, action, details, status) => {
    try {
        await runQuery(
            'INSERT INTO audit_logs (user_id, action, details, status, timestamp) VALUES (?, ?, ?, ?, ?)',
            [userId, action, details, status, Date.now()]
        );
    } catch (err) {
        console.error('Audit log error:', err);
    }
};

// --- ROUTES ---

// 1. REGISTER
app.post('/register', async (req, res) => {
    const { email, username, password } = req.body;

    // Backend Validations
    if (!email || !username || !password) {
        return res.status(400).json({ error: 'Todos los campos son obligatorios' });
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        return res.status(400).json({ error: 'Formato de email inválido' });
    }

    if (username.length < 3 || /\s/.test(username) || !/^[a-zA-Z0-9]+$/.test(username)) {
        return res.status(400).json({ error: 'Nombre de usuario inválido' });
    }

    const passRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
    if (!passRegex.test(password)) {
        return res.status(400).json({ error: 'La contraseña no cumple los requisitos de seguridad' });
    }

    try {
        const existingUser = await getQuery('SELECT * FROM users WHERE email = ? OR username = ?', [email, username]);
        if (existingUser) {
            const conflict = existingUser.email === email ? 'El email' : 'El nombre de usuario';
            return res.status(409).json({ error: `${conflict} ya está registrado` });
        }

        const salt = await bcrypt.genSalt(10);
        const passwordHash = await bcrypt.hash(password, salt);
        const createdAt = Date.now();

        const result = await runQuery(
            'INSERT INTO users (email, username, password_hash, created_at) VALUES (?, ?, ?, ?)',
            [email, username, passwordHash, createdAt]
        );

        await logAudit(result.lastID, 'REGISTER', `User ${username} registered`, 'SUCCESS');

        const token = jwt.sign({ id: result.lastID, email, username }, SECRET_KEY, { expiresIn: '7d' });

        res.status(201).json({
            message: 'Usuario registrado con éxito',
            token,
            user: { id: result.lastID, email, username, createdAt }
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error interno del servidor' });
    }
});

// 2. LOGIN
app.post('/login', async (req, res) => {
    const { identifier, password } = req.body; // identifier can be email or username

    if (!identifier || !password) {
        return res.status(400).json({ error: 'Identificador y contraseña requeridos' });
    }

    try {
        const isEmail = identifier.includes('@');
        const user = await getQuery(
            isEmail ? 'SELECT * FROM users WHERE email = ?' : 'SELECT * FROM users WHERE username = ?',
            [identifier]
        );

        if (!user) {
            return res.status(401).json({ error: 'Credenciales inválidas' });
        }

        // Check lockout
        if (user.lockout_until > Date.now()) {
            const minutesLeft = Math.ceil((user.lockout_until - Date.now()) / 60000);
            return res.status(403).json({ error: `Cuenta bloqueada. Intenta de nuevo en ${minutesLeft} minutos` });
        }

        const isMatch = await bcrypt.compare(password, user.password_hash);

        if (!isMatch) {
            const newAttempts = (user.login_attempts || 0) + 1;
            let lockoutUntil = 0;
            let message = 'Credenciales inválidas';

            if (newAttempts >= 5) {
                lockoutUntil = Date.now() + (5 * 60 * 1000); // 5 minutes
                message = 'Demasiados intentos. Cuenta bloqueada por 5 minutos';
                await runQuery('UPDATE users SET login_attempts = 0, lockout_until = ? WHERE id = ?', [lockoutUntil, user.id]);
            } else {
                await runQuery('UPDATE users SET login_attempts = ? WHERE id = ?', [newAttempts, user.id]);
            }

            await logAudit(user.id, 'LOGIN', `Failed attempt for ${identifier}`, 'FAILURE');
            return res.status(401).json({ error: message, attemptsLeft: 5 - newAttempts });
        }

        // Success - reset attempts
        await runQuery('UPDATE users SET login_attempts = 0, lockout_until = 0 WHERE id = ?', [user.id]);
        await logAudit(user.id, 'LOGIN', `User ${user.username} logged in`, 'SUCCESS');

        const token = jwt.sign({ id: user.id, email: user.email, username: user.username }, SECRET_KEY, { expiresIn: '7d' });

        res.json({
            message: 'Login exitoso',
            token,
            user: { id: user.id, email: user.email, username: user.username }
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error interno del servidor' });
    }
});

// 3. FORGOT PASSWORD (Simulado)
app.post('/forgot-password', async (req, res) => {
    const { identifier } = req.body;
    try {
        const isEmail = identifier.includes('@');
        const user = await getQuery(
            isEmail ? 'SELECT * FROM users WHERE email = ?' : 'SELECT * FROM users WHERE username = ?',
            [identifier]
        );

        if (!user) {
            return res.status(404).json({ error: 'Usuario no encontrado' });
        }

        // En un caso real, aquí enviaríamos un email. Para el TFG, simulamos éxito.
        await logAudit(user.id, 'FORGOT_PASSWORD', `Password reset requested for ${identifier}`, 'SUCCESS');
        res.json({ message: `Se ha enviado un enlace de recuperación al correo asociado a ${user.username || 'tu cuenta'}` });
    } catch (err) {
        res.status(500).json({ error: 'Error interno' });
    }
});

// ... resto de rutas (scores, etc) ...
app.get('/me', async (req, res) => {
    const token = req.headers['authorization'];
    if (!token) return res.status(401).json({ error: 'No token provided' });

    try {
        const decoded = jwt.verify(token, SECRET_KEY);
        const user = await getQuery('SELECT id, email, username FROM users WHERE id = ?', [decoded.id]);
        if (!user) return res.status(404).json({ error: 'User not found' });
        res.json({ user });
    } catch (err) {
        res.status(401).json({ error: 'Invalid token' });
    }
});

app.post('/scores', async (req, res) => {
    let token = req.headers['authorization'];
    const { subject, score } = req.body;
    if (!token) return res.status(401).json({ error: 'No token provided' });

    // Remove 'Bearer ' prefix if present
    if (token.startsWith('Bearer ')) {
        token = token.slice(7);
    }

    try {
        const decoded = jwt.verify(token, SECRET_KEY);
        await runQuery(
            'INSERT INTO scores (user_id, email, subject, score, timestamp) VALUES (?, ?, ?, ?, ?)',
            [decoded.id, decoded.email, subject, score, Date.now()]
        );
        res.json({ message: 'Score saved' });
    } catch (err) {
        res.status(401).json({ error: 'Invalid token' });
    }
});

app.get('/scores', async (req, res) => {
    const { subject, period, userId } = req.query;
    console.log('Fetching scores with params:', { subject, period, userId });
    try {
        let query = 'SELECT s.*, u.username FROM scores s LEFT JOIN users u ON s.user_id = u.id WHERE 1=1';
        let params = [];

        if (subject && subject !== 'Todos' && subject !== 'todos') {
            query += ' AND s.subject = ?';
            params.push(subject);
        }

        if (userId) {
            query += ' AND s.user_id = ?';
            params.push(userId);
        }

        if (period && period !== 'Todos' && period !== 'todos') {
            const now = Date.now();
            let startTime = 0;
            if (period === 'Diario') startTime = now - (24 * 60 * 60 * 1000);
            else if (period === 'Semanal') startTime = now - (7 * 24 * 60 * 60 * 1000);
            else if (period === 'Mensual') startTime = now - (30 * 24 * 60 * 60 * 1000);
            
            if (startTime > 0) {
                query += ' AND s.timestamp >= ?';
                params.push(startTime);
            }
        }

        query += ' ORDER BY s.score DESC LIMIT 50';
        console.log('Final SQL Query:', query, 'Params:', params);

        const scores = await new Promise((resolve, reject) => {
            db.all(query, params, (err, rows) => {
                if (err) reject(err);
                else resolve(rows);
            });
        });
        
        console.log(`Found ${scores.length} scores`);
        res.json(scores);
    } catch (err) {
        console.error('Error fetching scores:', err);
        res.status(500).json({ error: 'Error fetching scores' });
    }
});

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
