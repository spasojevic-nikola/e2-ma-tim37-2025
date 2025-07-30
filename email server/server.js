const express = require('express');
const cors = require('cors');
const sqlite3 = require('sqlite3').verbose();
const nodemailer = require('nodemailer');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Povezivanje sa SQLite bazom
const db = new sqlite3.Database('./mydatabase.db', (err) => {
  if (err) {
    console.error('Greška pri povezivanju sa bazom:', err.message);
  } else {
    console.log('Povezano sa SQLite bazom.');
  }
});

// Kreiranje tabele ako ne postoji
  db.run(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT NOT NULL,
      email TEXT NOT NULL UNIQUE,
      password TEXT NOT NULL,
      avatar INTEGER,
      token TEXT,
      active INTEGER DEFAULT 0
    )
  `);

  // Nodemailer transporter (podesi stvarne kredencijale)
  const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
          user: 'slike.rimini2024@gmail.com',        
          pass: 'zncd plez nwok cnhz'           // dobio sam app password, NIJE obična lozinka
      }
  });

  app.post('/register', (req, res) => {
    const { username, email, password, avatar } = req.body;
    const token = uuidv4();

    console.log('Received body:', req.body);

    const insertUser = `
      INSERT INTO users (username, email, password, avatar, token)
      VALUES (?, ?, ?, ?, ?)
    `;

    db.run(insertUser, [username, email, password, avatar, token], function (err) {
      if (err) {
        console.error('Greška pri upisu u bazu:', err.message);
        return res.status(500).json({ error: 'Registracija neuspešna. Email možda već postoji.' });
      }

      const activationLink = `http://localhost:3000/activate?token=${token}`;

      const mailOptions = {
        from: 'slike.rimini2024@gmail.com',
        to: email,
        subject: 'Aktivacija naloga',
        html: `<p>Klikni na link za aktivaciju naloga: <a href="${activationLink}">${activationLink}</a></p>`
      };

      transporter.sendMail(mailOptions, (err, info) => {
        if (err) {
          console.error('Greška pri slanju emaila:', err.message);
          return res.status(500).json({ error: 'Registracija uspešna, ali email nije poslat.' });
        } else {
          console.log('Email poslat:', info.response);
          return res.status(200).json({ message: 'Registracija uspešna! Proveri email za aktivaciju.' });
        }
      });
    });
  });

  app.get('/activate', (req, res) => {
    const token = req.query.token;

    if (!token) {
      return res.status(400).json({ error: 'Token nije prosleđen.' });
    }

    const activateUser = `
      UPDATE users SET active = 1 WHERE token = ?
    `;

    db.run(activateUser, [token], function (err) {
      if (err) {
        console.error('Greška pri aktivaciji:', err.message);
        return res.status(500).json({ error: 'Greška pri aktivaciji.' });
      }

      if (this.changes === 0) {
        return res.status(400).json({ error: 'Nevažeći token.' });
      }

      res.status(200).json({ message: 'Nalog je uspešno aktiviran!' });
    });
  });

  app.post('/login', (req, res) => {
  const { email, password } = req.body;

  const query = `SELECT * FROM users WHERE email = ?`;

  db.get(query, [email], (err, row) => {
  if (err) {
    console.error('Greška pri pristupu bazi:', err.message);
    return res.status(500).json({ error: 'Greška na serveru' });
  }

  if (!row) {
    console.log("Korisnik nije pronađen za email:", email);
    return res.status(401).json({ error: 'Pogrešan email ili lozinka' });
  }

  console.log("Nađen korisnik:", row);

  if (row.password !== password) {
    console.log("Lozinka ne odgovara");
    return res.status(401).json({ error: 'Pogrešan email ili lozinka' });
  }

  if (row.active !== 1) {
    console.log("Nalog nije aktiviran");
    return res.status(403).json({ error: 'Nalog nije aktiviran' });
  }

    // uspešna prijava
    return res.status(200).json({
      message: 'Uspešna prijava',
      user: {
        id: row.id,
        username: row.username,
        email: row.email,
        avatar: row.avatar
      }
    });
  });
});


  app.listen(PORT, () => {
    console.log(`Backend radi na http://localhost:${PORT}`);
  });
