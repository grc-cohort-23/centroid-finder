// One-time check that our server can reach the Neon database.
// Run it from server/ with: npx tsx src/checkDb.ts
import path from "node:path";
import dotenv from "dotenv";
import { Pool } from "pg";

// .env is in the project root, two folders up from server/src
dotenv.config({ path: path.join(import.meta.dirname, "../../.env") });

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

try {
  // the simplest possible query, just to prove we're connected
  const ping = await pool.query("SELECT 1 AS ok");
  console.log("Connected to Neon:", ping.rows[0]);

  // proves the jobs table exists
  const jobs = await pool.query("SELECT COUNT(*) AS count FROM jobs");
  console.log("Rows in jobs table:", jobs.rows[0]);
} catch (error) {
  console.error("Could not reach the database:", error);
} finally {
  // close the connections so the script finishes instead of hanging
  await pool.end();
}
