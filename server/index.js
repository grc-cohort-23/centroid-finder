import { mkdir } from 'fs/promises';
import app from './app.js';
import { config } from './config.js';

await mkdir(config.resultsDir, { recursive: true });
await mkdir(config.jobsDir, { recursive: true });

app.listen(config.port, () => {
  console.log(`Server running on port ${config.port}`);
});

