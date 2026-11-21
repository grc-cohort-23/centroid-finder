import fs from "fs";
import path from "path";
import { Router } from "express";

// The version from server/package.json. It's one folder up from both src/ (npm run dev)
// and dist/ (npm start), so this path works for both.
function readVersion(): string {
    const packagePath = path.join(import.meta.dirname, "..", "package.json");
    const packageJson = JSON.parse(fs.readFileSync(packagePath, "utf8")) as { version?: string };
    return packageJson.version ?? "unknown";
}

// GET /api/health
// A quick "is the server up?" check for the frontend. It doesn't touch the database or the JAR.
export function healthRoute(): Router {
    const router = Router();
    const version = readVersion(); // read once, when the app starts

    router.get("/api/health", (req, res) => {
        res.json({ ok: true, version });
    });

    return router;
}
