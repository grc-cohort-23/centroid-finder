import { describe, it, expect } from "vitest";
import { FakeJobStore } from "./fakeJobStore.js";
import { finishJob, startJob } from "./startJob.js";


describe("finishJob", () => {
    it("marks the job done with the result URL when the exit code is 0", async () => {
        // Arrange: a job that is still processing
        const store = new FakeJobStore();
        await store.createJob("job-1", "intro.mp4", "ff0000", 120);

        // Act: the JAR exited with 0
        await finishJob(store, "job-1", 0);

        // Assert
        const job = await store.findJob("job-1");
        expect(job?.status).toBe("done");
        expect(job?.result).toBe("/results/job-1.csv");
    });

    it("marks the job as an error when the exit code is 1", async () => {
        // Arrange: a job that is still processing
        const store = new FakeJobStore();
        await store.createJob("job-1", "intro.mp4", "ff0000", 120);

        // Act: the JAR exited with 1
        await finishJob(store, "job-1", 1);

        // Assert
        const job = await store.findJob("job-1");
        expect(job?.status).toBe("error");
        expect(job?.error).not.toBeNull();

    });

    it("marks the job as an error when the JAR couldn't start", async () => {
        // Arrange: a job that is still processing
        const store = new FakeJobStore();
        await store.createJob("job-1", "intro.mp4", "ff0000", 120);

        // Act: the JAR couldn't start
        await finishJob(store, "job-1", null);

        // Assert
        const job = await store.findJob("job-1");
        expect(job?.status).toBe("error");
        expect(job?.error).not.toBeNull();
    });
});

describe("startJob", () => {
    // the folders and JAR path the real server would get from .env
    const settings = {
        jarPath: "/jars/videoprocessor.jar",
        videosDir: "/videos",
        resultsDir: "/results-folder",
    };

    it("saves the job as processing and starts the JAR with the right arguments", async () => {
        // Arrange: a fake runner that remembers what it was asked to run (instead of starting java)
        const store = new FakeJobStore();
        let ranCommand = "";
        let ranArgs: string[] = [];
        const fakeRun = (command: string, args: string[]) => {
            ranCommand = command;
            ranArgs = args;
        };

        // Act
        const jobId = await startJob(store, settings, "intro.mp4", "ff0000", 120, fakeRun);

        // Assert: the job was saved as processing
        const job = await store.findJob(jobId);
        expect(job?.status).toBe("processing");

        // Assert: java was started with the full paths, and the CSV named after the jobId
        expect(ranCommand).toBe("java");
        expect(ranArgs).toEqual([
            "-jar", "/jars/videoprocessor.jar",
            "/videos/intro.mp4",
            `/results-folder/${jobId}.csv`,
            "ff0000", "120",
        ]);
    });

    it("starting two jobs gives two different jobIds", async () => {
        // Arrange
        const store = new FakeJobStore();
        const fakeRun = () => {};

        // Act
        const firstJobId = await startJob(store, settings, "intro.mp4", "ff0000", 120, fakeRun);
        const secondJobId = await startJob(store, settings, "intro.mp4", "ff0000", 120, fakeRun);

        // Assert
        expect(firstJobId).not.toBe(secondJobId);
    });
});
