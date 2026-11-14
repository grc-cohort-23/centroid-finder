import ffmpeg from "ffmpeg";
import ffmpegPath from "ffmpeg-static";
import ffprobePath from "ffprobe-static";
import fs from "node:fs/promises";

ffmpeg.bin = ffmpegPath;
ffmpeg.ffprobeBin = ffprobePath.path;

export default async function getThumbnail(req, res){
    try{
        const path = req.params.filename;

        console.log(path);
        const video = await ffmpeg(`./videos/${path}`);
        const thumbnail = await video.fnExtractFrameToJPG("./thumbnails", {
            number: 1,
            file_name: `${path}-thumbnail`
        })

        const jpegBuffer = await fs.readFile(thumbnail[0]);

        return res.status(200).type("image/jpeg").send(jpegBuffer);
    }catch(e){
        res.status(500).json({error: "Error generating thumbnail"})
    }
}