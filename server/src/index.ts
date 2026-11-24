import fs from "fs";
import path from "path";
import dotenv from "dotenv";
import { createApp } from "./app.js";
import { createPool } from "./database.js";
import { NeonJobStore } from "./neonJobStore.js";

dotenv.config({path: path.join(import.meta.dirname,"..","..",".env")});

const videoDir = process.env.VIDEOS_DIR;
const resultsDir = process.env.RESULTS_DIR;
const jarPath = process.env.JAR_PATH;
const databaseUrl = process.env.DATABASE_URL;


//Stop right away if .env is missing a setting
if(!videoDir || !resultsDir || !jarPath || !databaseUrl){
    console.error("VIDEOS_DIR, RESULTS_DIR, JAR_PATH, and DATABASE_URL must be set in .env");
    process.exit(1);
}
//Make the results folder if it isn't there yet
fs.mkdirSync(resultsDir, {recursive: true});


const port = process.env.PORT || 3000;
// the real job store, using our Neon database
const store = new NeonJobStore(createPool(databaseUrl));

const app = createApp({videoDir, resultsDir, jarPath, store});


app.listen(port, () =>{
    console.log(`Server running at http://localhost:${port} `);
});