import { Pool } from "pg";

// Makes the pool of connections to our Neon database.
export function createPool(connectionString: string): Pool {
    const pool = new Pool({ connectionString });

    // If a connection drops while it's waiting to be used (like Neon restarting),
    // pg sends an "error" event. Without this listener, that error would crash
    // the whole server, so we log it instead. The pool makes a new connection next time.
    pool.on("error", (error) => {
        console.error("Database connection error:", error.message);
    });

    return pool;
}
