import { Router } from 'express';
import { readdir } from 'fs/promises';

const router = Router();

router.get('/api/videos', async (req, res) => {
    try {
        const files = await readdir(process.env.VIDEOS_DIR);
        res.json(files);
    } catch (err) {
        res.status(500).json({
            error: 'Could not read videos directory'
        });
    }
});

export default router;