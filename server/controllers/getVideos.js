import fs from "fs/promises"

export default async function getVideos(req, res){
    try{
        const videos = await fs.readdir("./videos");
        res.status(200).send(videos);
    }catch(err){
        res.status(500).json({error: "Error reading video directory"});
    }
}