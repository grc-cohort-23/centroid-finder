import {describe, it, expect} from "vitest";
import { buildJarArgs, runInBackground, statusFromExitCode } from "./jarRunner.js";

describe("buildJarArgs", () => {
    it("puts the arguments in the order the JAR expects", () => {
        // Arrange
        const jarPath = "/jars/videoprocessor.jar";

        // Act 
        const args = buildJarArgs(jarPath, "/videos/intro.mp4", "/results/abc.csv", "ff0000", "120");

        // Assert
        expect(args).toEqual(["-jar", jarPath, "/videos/intro.mp4", "/results/abc.csv", "ff0000", "120"]);
    });
});

describe("runInBackground", () => {
    it("calls onExit with 0 when the program succeeds", async () => {
        //Arrange + Act: run a tiny node program that exits with 0,
        // and wait until onExit is called
        const code = await new Promise<number | null>((resolve) => {
            runInBackground(process.execPath, ["-e", "process.exit(0)"], resolve);
        });

        // Assert
        expect(code).toBe(0);
    });


    it("calls onExit with 3 when the program exits with 3", async () => {
        //Arrange + Act: run a tiny node program that exits with 3, 
        // and wait until onExit is called
        const code = await new Promise<number | null>((resolve) => {
            runInBackground(process.execPath, ["-e", "process.exit(3)"], resolve);
        });

        // Assert
        expect(code).toBe(3);
    });

    it("calls onExit with null when the program can't start", async () => {
        //Arrange + Act: a command that doesn't exist
        const code = await new Promise<number | null>((resolve) => {
            runInBackground("no-such-program", [], resolve);
        });

        // Assert
        expect(code).toBeNull();
    });
});

describe("statusFromExitCode", () => {
    it("returns 'done' when the exit code is 0", () => {
        // Arrange
        const exitCode = 0;

        // Act
        const status = statusFromExitCode(exitCode);

        // Assert
        expect(status).toBe("done");
    });

    it("returns 'error' when the exit code is non-zero", () => {
        // Arrange
        const exitCode = 3;

        // Act
        const status = statusFromExitCode(exitCode);

        // Assert
        expect(status).toBe("error");
    });

    it("returns 'error' when the exit code is null", () => {
        // Arrange
        const exitCode = null;

        // Act
        const status = statusFromExitCode(exitCode);

        // Assert
        expect(status).toBe("error");
    });
});