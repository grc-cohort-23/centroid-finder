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

/**
//  * @description retrieves a single video by name
//  * @param {*} req 
//  * @param {*} res 
//  * @returns 
//  */
// export const singleVideo = async (req, res)=>{

//     const video = req.params.video;

//     if(!video){
//         console.log("ERROR: no video provided");
//         return res.status(400).json({success: false, message: "No video provided"});
//     }

//     console.log(`video provided: ${video} video directory: ${process.env.VIDEO_DIR}`);
//     const videoIsAvailable = await readFolder(process.env.VIDEO_DIR, video);

//     if(!videoIsAvailable){
//         console.log(`ERROR: video not available: ${video}`);
//         return res.status(404).json({success: false, message: "video not available"});
//     }
//     return res.status(200).json({success: true, message: `success! available videos: ${"probably an array of videos from a collection"} `});
// };
// async function readFolder(targetPath, video) {
//     try {
//         const entries = await fs.readdir(targetPath, { withFileTypes: true });
        
//         for (let entry of entries) {
//             console.log(`entry video: ${entry.name}`);
//             if (entry.name === video) {
//                 console.log(`Found video: ${entry.name}`);
//                 return true;
//             } 
//         }
//     } catch (err) {
//         console.error('Unable to scan directory:', err);
//         return false;
//     }
//     return false;
// }