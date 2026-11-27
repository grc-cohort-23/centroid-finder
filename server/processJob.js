const fs = require("node:fs/promises");
const { spawn } = require("node:child_process");

async function saveJob(jobPath, job) {
  const temporaryPath = `${jobPath}.tmp`;
  await fs.writeFile(temporaryPath, JSON.stringify(job, null, 2));
  await fs.rename(temporaryPath, jobPath);
}

async function processJob(jobPath) {
  const job = JSON.parse(await fs.readFile(jobPath, "utf8"));

  try {
    await new Promise((resolve, reject) => {
      const child = spawn(job.javaBin, [
        "-Xmx512m",
        "-jar",
        job.jarPath,
        job.inputPath,
        job.outputPath,
        job.targetColor,
        String(job.threshold),
      ], {
        stdio: "ignore",
      });

      child.once("error", reject);

      child.once("close", (code) => {
        if (code === 0) {
          resolve();
        } else {
          reject(new Error(`Java stopped with exit code ${code}`));
        }
      });
    });

    const output = await fs.stat(job.outputPath);

    if (output.size === 0) {
      throw new Error("The CSV file is empty.");
    }

    job.status = "done";
  } catch (error) {
    job.status = "error";
    job.error = error.message;
  }

  await saveJob(jobPath, job);
}

processJob(process.argv[2]).catch((error) => {
  console.error(error);
  process.exitCode = 1;
});