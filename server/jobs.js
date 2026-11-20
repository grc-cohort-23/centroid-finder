const Database = require('better-sqlite3');
const config = require('./config');

// Opening the file creates it if it doesn't exist yet.
const db = new Database(config.DB_PATH);

db.exec(`
  CREATE TABLE IF NOT EXISTS jobs (
    id           TEXT PRIMARY KEY,
    filename     TEXT NOT NULL,
    target_color TEXT NOT NULL,
    threshold    INTEGER NOT NULL,
    status       TEXT NOT NULL,
    result       TEXT,
    error        TEXT,
    created_at   TEXT DEFAULT CURRENT_TIMESTAMP
  )
`);

// Prepared statements are compiled once and reused. The ? placeholders
// (and @names) are filled in safely by SQLite, which prevents SQL injection.
const insertStmt = db.prepare(`
  INSERT INTO jobs (id, filename, target_color, threshold, status)
  VALUES (@id, @filename, @targetColor, @threshold, 'processing')
`);
const updateStmt = db.prepare(`
  UPDATE jobs SET status = @status, result = @result, error = @error WHERE id = @id
`);
const getStmt = db.prepare('SELECT * FROM jobs WHERE id = ?');

function createJob({ id, filename, targetColor, threshold }) {
  insertStmt.run({ id, filename, targetColor, threshold });
}

function updateJob(id, { status, result = null, error = null }) {
  updateStmt.run({ id, status, result, error });
}

// Returns the job, or null if there's no job with that ID.
function getJob(id) {
  const row = getStmt.get(id);
  if (!row) return null;
  return {
    id: row.id,
    filename: row.filename,
    targetColor: row.target_color,
    threshold: row.threshold,
    status: row.status,
    result: row.result,
    error: row.error,
    createdAt: row.created_at,
  };
}

module.exports = { createJob, updateJob, getJob };
