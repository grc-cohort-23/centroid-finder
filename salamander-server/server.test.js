const request = require("supertest");
const app = require("./server");

describe("Salamander API", () => {

    test("GET / should return server running message", async () => {
        const response = await request(app).get("/");

        expect(response.statusCode).toBe(200);
        expect(response.text).toBe("Salamander Video Server is running!");
    });

    test("GET /api/videos should return a list of videos", async () => {
        const response = await request(app).get("/api/videos");

        expect(response.statusCode).toBe(200);
        expect(response.body).toHaveProperty("videos");
        expect(Array.isArray(response.body.videos)).toBe(true);
        expect(response.body.videos).toContain("test.mp4");
    });

    test("GET /thumbnail should return 404 for missing video", async () => {
        const response = await request(app).get("/thumbnail/missing.mp4");

        expect(response.statusCode).toBe(404);
        expect(response.body.error).toBe("Video not found");
    });


    test("GET /thumbnail should create a thumbnail for a valid video", async () => {
        const response = await request(app).get("/thumbnail/test.mp4");

        expect(response.statusCode).toBe(200);
        expect(response.headers["content-type"]).toContain("image/jpeg");
    });

    test("POST /process should complete a video processing job", async () => {
        const response = await request(app)
            .post("/process/test.mp4")
            .send({
                targetColor: "FF0000",
                threshold: 50
            });

        expect(response.statusCode).toBe(202);
        expect(response.body).toHaveProperty("jobId");
        expect(response.body.status).toBe("running");

        const jobId = response.body.jobId;
        let status = "running";

        // Wait for Java processing to finish
        for (let i = 0; i < 30 && status === "running"; i++) {
            await new Promise(resolve => setTimeout(resolve, 500));

            const statusResponse = await request(app)
                .get(`/process/${jobId}/status`);

            expect(statusResponse.statusCode).toBe(200);
            status = statusResponse.body.status;
        }

        expect(status).toBe("completed");
    }, 20000);

    test("GET /process/:jobId/status should return 404 for unknown job", async () => {
        const response = await request(app)
            .get("/process/unknown-job-id/status");

        expect(response.statusCode).toBe(404);
        expect(response.body.error).toBe("Job not found");
    });


    test("POST /process should reject invalid color and threshold", async () => {
        const response = await request(app)
            .post("/process/test.mp4")
            .send({
                targetColor: "NOTACOLOR",
                threshold: -10
            });

        expect(response.statusCode).toBe(400);
    });


    test("POST /process should reject missing parameters", async () => {
        const response = await request(app)
            .post("/process/test.mp4")
            .send({});

        expect(response.statusCode).toBe(400);
        expect(response.body.error).toBe(
            "Target color and threshold are required"
        );
    });

    test("POST /process should return 404 for missing video", async () => {
        const response = await request(app)
            .post("/process/missing.mp4")
            .send({
                targetColor: "FF0000",
                threshold: 50
            });

        expect(response.statusCode).toBe(404);
        expect(response.body.error).toBe("Video not found");
    });


    
    test("GET /results should return a CSV file", async () => {
        const fs = require("fs");
        const path = require("path");

        const resultsFolder = path.join(__dirname, "results");
        const testFile = path.join(resultsFolder, "test-result.csv");

        fs.mkdirSync(resultsFolder, { recursive: true });
        fs.writeFileSync(testFile, "frame,x,y\n1,100,200");

        try {
            const response = await request(app)
                .get("/results/test-result.csv");

            expect(response.statusCode).toBe(200);
            expect(response.headers["content-type"]).toContain("text/csv");
            expect(response.text).toContain("1,100,200");
        } finally {
            fs.unlinkSync(testFile);
        }
    });



});