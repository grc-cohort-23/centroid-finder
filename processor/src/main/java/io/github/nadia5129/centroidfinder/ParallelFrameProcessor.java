package io.github.nadia5129.centroidfinder;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Finds the centroid of every video frame on several threads at the same time,
 * but still writes the CSV rows in frame order.
 *
 * How it works:
 * - The calling thread reads the frames one by one (the video reader is not thread-safe)
 *   and reads each frame's timestamp right after it.
 * - Each frame is COPIED, then handed to a pool of worker threads. The copy matters:
 *   the video reader reuses the same image for every frame.
 * - Every worker thread makes its own ImageCentroidTracker, so no tracker is shared.
 * - Results are written oldest first, so the rows come out in frame order.
 * - At most FRAMES_PER_THREAD * threadCount frames wait at once, so memory stays small.
 */
public class ParallelFrameProcessor {

    //How many frames may be waiting per worker thread. 2 keeps every worker busy
    //(it always has a next frame ready) without keeping a lot of frames in memory.
    static final int FRAMES_PER_THREAD = 2;

    //The thread that reads (decodes) the video also needs a core, so the workers get the rest
    static final int READER_THREADS = 1;

    //Optional environment variable to choose the number of worker threads (handy for testing)
    static final String THREADS_ENV_VARIABLE = "CENTROID_THREADS";

    private final Supplier<ImageCentroidTracker> trackerMaker;
    private final int threadCount;

    //trackerMaker builds a new tracker; it is called once per worker thread
    public ParallelFrameProcessor(Supplier<ImageCentroidTracker> trackerMaker, int threadCount) {
        if (threadCount < 1) {
            throw new IllegalArgumentException("threadCount must be at least 1, but got: " + threadCount);
        }
        this.trackerMaker = trackerMaker;
        this.threadCount = threadCount;
    }

    //One frame that was handed to the pool: its number, its timestamp, and its centroid (once it is ready)
    private record PendingFrame(int frameNumber, double seconds, Future<Coordinate> centroid) {
    }

    //Reads every frame, finds the centroids in parallel, and writes one row per frame in order.
    //If a frame fails, it stops and throws an IOException that says which frame failed.
    public void process(FrameReader reader, CSVOutputWriter writer) throws IOException {
        //each worker thread makes its own tracker the first time it needs one
        ThreadLocal<ImageCentroidTracker> trackers = ThreadLocal.withInitial(trackerMaker);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount, ParallelFrameProcessor::makeWorkerThread);
        //frames that are being worked on, oldest first
        Queue<PendingFrame> pending = new ArrayDeque<>();
        int maxFramesInFlight = threadCount * FRAMES_PER_THREAD;

        try {
            int frameNumber = 0;
            BufferedImage frame;
            //nextFrame() returns null when the video is over
            while ((frame = reader.nextFrame()) != null) {
                //read the timestamp now, before the reader moves on to the next frame
                double seconds = reader.getTimestamp();
                //copy the frame, because the reader will draw the next frame into the same image
                BufferedImage copy = copyImage(frame);
                Future<Coordinate> centroid = pool.submit(() -> trackers.get().findCentroid(copy));
                pending.add(new PendingFrame(frameNumber, seconds, centroid));
                frameNumber++;

                //too many frames waiting: wait for the oldest one and write it before reading more
                if (pending.size() >= maxFramesInFlight) {
                    writeOldest(pending, writer);
                }
            }
            //the video is over: write the frames that are still waiting
            while (!pending.isEmpty()) {
                writeOldest(pending, writer);
            }
        } finally {
            //always stop the worker threads, even if something failed
            pool.shutdownNow();
        }
    }

    //Waits for the oldest frame to be done, then writes its row
    private static void writeOldest(Queue<PendingFrame> pending, CSVOutputWriter writer) throws IOException {
        PendingFrame oldest = pending.remove();
        try {
            writer.writeRow(oldest.seconds(), oldest.centroid().get());
        } catch (ExecutionException e) {
            //the worker threw an exception: e.getCause() is the real problem
            throw new IOException("frame " + oldest.frameNumber() + " could not be processed: " + e.getCause(),
                e.getCause());
        } catch (InterruptedException e) {
            //someone asked this thread to stop: remember that, and stop
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while waiting for frame " + oldest.frameNumber(), e);
        }
    }

    //Worker threads are "daemon" threads, so they can never keep the program running after main ends
    private static Thread makeWorkerThread(Runnable task) {
        Thread thread = new Thread(task, "frame-worker");
        thread.setDaemon(true);
        return thread;
    }

    //Makes a full copy of an image (new pixel memory), with the same image type and colors
    static BufferedImage copyImage(BufferedImage image) {
        //Fast way: JavaCV gives us TYPE_3BYTE_BGR frames, whose pixels are one byte[]
        //(blue, green, red, blue, green, red, ...). Copying that array with one
        //System.arraycopy is about 15x faster than copyData, which copies sample by sample.
        if (BgrImages.isPlainBgrImage(image)) {
            BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            byte[] from = BgrImages.bytes(image);
            byte[] to = BgrImages.bytes(copy);
            System.arraycopy(from, 0, to, 0, from.length);
            return copy;
        }

        //Safe way for any other kind of image
        ColorModel colorModel = image.getColorModel();
        //Make an empty raster of the right size that starts at (0, 0), then copy the pixels
        //into it. (copyData(null) would keep a sub-image's position inside its parent image,
        //and new BufferedImage refuses a raster that doesn't start at (0, 0).)
        WritableRaster pixels = colorModel.createCompatibleWritableRaster(image.getWidth(), image.getHeight());
        image.copyData(pixels);
        return new BufferedImage(colorModel, pixels, colorModel.isAlphaPremultiplied(), null);
    }

    //Chooses the number of worker threads from the CENTROID_THREADS variable and the number of cores
    public static int chooseThreadCount() {
        return chooseThreadCount(System.getenv(THREADS_ENV_VARIABLE), Runtime.getRuntime().availableProcessors());
    }

    //Uses envValue if it is a whole number of at least 1. Otherwise uses one thread per core,
    //minus the reader thread's core, but never less than 1.
    static int chooseThreadCount(String envValue, int processors) {
        if (envValue != null) {
            try {
                int requested = Integer.parseInt(envValue.trim());
                if (requested >= 1) {
                    return requested;
                }
            } catch (NumberFormatException e) {
                //not a number: ignore it and use the default below
            }
        }
        return Math.max(1, processors - READER_THREADS);
    }
}
