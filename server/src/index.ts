import fs from "fs";
import path from "path";
import dotenv from "dotenv";
import { createApp } from "./app.js";
import { Pool } from "pg";
import { NeonJobStore } from "./neonJobStore.js";

dotenv.config({path: path.join(import.meta.dirname,"..","..",".env")});

const videoDir = process.env.VIDEOS_DIR;
const resultsDir = process.env.RESULTS_DIR;
const jarPath = process.env.JAR_PATH;


//Stop right away if .env is missing a folder
if(!videoDir || !resultsDir || !jarPath){
    console.error("VIDEOS_DIR, RESULTS_DIR, and JAR_PATH must be set in .env");
    process.exit(1);
}
//Make the results folder if it isn't there yet
fs.mkdirSync(resultsDir, {recursive: true});


const port = process.env.PORT || 3000;
// the real job store, using our Neon database
const store = new NeonJobStore(new Pool({ connectionString: process.env.DATABASE_URL }));

const app = createApp({videoDir, resultsDir, jarPath, store});


app.listen(port, () =>{
    console.log(`Server running at http://localhost:${port} `);
});