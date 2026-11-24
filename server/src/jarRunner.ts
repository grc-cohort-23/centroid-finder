import { spawn } from "node:child_process";
import type { JobStatus } from "./jobStore.js";


// Builds the arguments for: java -jar <jarPath> <inputPath> <outputCsv> <targetColor> <threshold>
// "java" itself is the command, so it's not in this list.
export function buildJarArgs(
    jarPath: string, 
    inputPath: string, 
    outputCsv: string, 
    targetColor: string, 
    threshold: string
): string[] {

    return ["-jar", jarPath, inputPath, outputCsv, targetColor, threshold];
}

// Starts a program in the background, so the server doesn't wait for it to finish.
// When the program is done, onExit gets its exit code (0 = success, anything else = failed).
// If the program can't start at all (like java isn't installed), onExit gets null.
export function runInBackground(
    command: string,
    args: string[],
    onExit: (code: number | null) => void
): void {
    const child = spawn(command, args, {
        detached: true,  // runs on its own, not tied to the server
        stdio: "ignore", // we don't read its output (the CSV is our result)
    });

    // the program ran and finished
    child.on("exit", (code) => onExit(code));

    // the program couldn't start. We must listen for this, or the whole server crashes
    child.on("error", () => onExit(null));

    // lets the server stop without waiting for this program
    child.unref();
}

// Turns the JAR's exit code into the job's status.
// 0 means the JAR finished without problems. Anything else, or null (it couldn't start), is an error.
export function statusFromExitCode(code: number | null): JobStatus {
    if (code === 0) {
        return "done";
    }
    return "error";
}
