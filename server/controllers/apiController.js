import fs from "fs/promises";

/**
 * @description retrieves all videos available for processing
 * @param {*} req 
 * @param {*} res 
 * @returns 
 */
export const videos = async (req, res)=>{

    if(!process.env.VIDEO_DIR){
        console.log("ERROR: no env value for video path");
        return res.status(500).json({success: false, message: "No videos path provided"});
    }

    try {
        const availableVideos = await getAvailableVideos(process.env.VIDEO_DIR);

        return res.status(200).json({
            success: true,
            availableVideos
        });

    }  catch (error) {
       if (error.code === "ENOTDIR") {
            console.error("Path is not a directory: ", process.env.VIDEO_DIR);
        } else if (error.code === "ENOENT") {
            console.error("Path does not exist: ", process.env.VIDEO_DIR);
        }else{
            console.log(error);
        }
        return res.status(500).json({success: false, message: `ERROR: Error reading video directory `});
    }   
};

async function getAvailableVideos(videoDir) {

    const entries = await fs.readdir(videoDir, { withFileTypes: true });
    
    for (let entry of entries) {
        console.log(`entry video: ${entry.name}`);
    }
    const availableVideos = entries.map(entry => entry.name);
    return availableVideos;
}
