# --- !Ups

CREATE TABLE polls (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT NOT NULL
);

CREATE TABLE questions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  poll_id INTEGER NOT NULL,
  text TEXT NOT NULL,
  allow_multiple BOOLEAN NOT NULL,
  FOREIGN KEY (poll_id) REFERENCES polls(id)
);

CREATE TABLE options (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  question_id INTEGER NOT NULL,
  text TEXT NOT NULL,
  FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT,           -- nepovinné jméno
  email TEXT           -- nepovinný e-mail
);

CREATE TABLE answers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  poll_id INTEGER NOT NULL,
  user_id INTEGER,            -- může být NULL pro anonymní
  question_id INTEGER NOT NULL,
  option_id INTEGER NOT NULL,
  FOREIGN KEY (poll_id) REFERENCES polls(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (question_id) REFERENCES questions(id),
  FOREIGN KEY (option_id) REFERENCES options(id)
);

-- TADY přidáš ALTER TABLE
ALTER TABLE answers ADD COLUMN created_at TEXT NOT NULL DEFAULT (datetime('now'));
ALTER TABLE answers ADD COLUMN submission_id TEXT NOT NULL DEFAULT '';


# --- !Downs


DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS options;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS polls;

