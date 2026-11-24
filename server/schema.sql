-- The jobs table in our Neon database (one row per video processing job).
-- To create it, run this in Neon's SQL Editor.

CREATE TABLE IF NOT EXISTS jobs (
    id            UUID PRIMARY KEY,                -- the jobId from uuid
    filename      TEXT NOT NULL,                   -- the video, like intro.mp4
    target_color  TEXT NOT NULL,                   -- like ff0000
    threshold     INTEGER NOT NULL,                -- like 120
    status        TEXT NOT NULL
                  CHECK (status IN ('processing', 'done', 'error')),
    result        TEXT,                            -- path to the CSV, empty until done
    error         TEXT,                            -- error message, empty unless it failed
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
