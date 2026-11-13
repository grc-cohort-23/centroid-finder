
import { Router } from "express";
import * as controller from "../controllers/controller.js";
import * as processorController from "../controllers/processorController.js";
import * as apiController from "../controllers/apiController.js";

// Main router
const router = new Router();
router.get("/", controller.home);
router.get("/health", controller.serverHealth);
router.get("/thumbnail/:filename", controller.thumbnail);

// API router
const apiRouter = new Router();
apiRouter.get("/videos", apiController.videos);

// Processor router
const processorRouter = new Router();
processorRouter.post("/:filename", processorController.processVideo);
processorRouter.get("/:jobId/status", processorController.status);

export { router, apiRouter, processorRouter };