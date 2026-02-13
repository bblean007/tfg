const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const dbPath = path.resolve(__dirname, 'database.sqlite');
const db = new sqlite3.Database(dbPath);

db.serialize(() => {
    console.log('Iniciando migración...');

    // 1. Añadir columna username si no existe
    db.run(`ALTER TABLE users ADD COLUMN username TEXT`, (err) => {
        if (err) {
            if (err.message.includes('duplicate column name')) {
                console.log('La columna username ya existe.');
            } else {
                console.error('Error al añadir columna username:', err.message);
            }
        } else {
            console.log('Columna username añadida con éxito.');
        }
    });

    // 2. Añadir columnas para control de intentos de login
    db.run(`ALTER TABLE users ADD COLUMN login_attempts INTEGER DEFAULT 0`, (err) => {
        if (!err || err.message.includes('duplicate column name')) {
            console.log('Columna login_attempts lista.');
        }
    });

    db.run(`ALTER TABLE users ADD COLUMN lockout_until INTEGER DEFAULT 0`, (err) => {
        if (!err || err.message.includes('duplicate column name')) {
            console.log('Columna lockout_until lista.');
        }
    });

    // 3. Actualizar usuario existente "Alex"
    db.run(`UPDATE users SET username = 'Alex' WHERE username IS NULL OR username = ''`, (err) => {
        if (err) console.error('Error al actualizar usuario Alex:', err.message);
        else console.log('Usuario Alex actualizado.');
    });

    // 4. Crear tabla de logs de auditoría
    db.run(`CREATE TABLE IF NOT EXISTS audit_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        action TEXT,
        details TEXT,
        status TEXT,
        timestamp INTEGER
    )`, (err) => {
        if (err) console.error('Error al crear tabla audit_logs:', err.message);
        else console.log('Tabla audit_logs lista.');
    });
});

db.close(() => {
    console.log('Migración finalizada.');
});
