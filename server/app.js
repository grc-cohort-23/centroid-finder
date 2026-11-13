import express from 'express';
import { router, apiRouter, processorRouter} from "./routers/router.js";

//configure Express.js app
const app = express();

//middleware
app.use(express.json());
//middleware to parse incoming request bodies so that we can access the data in req.body
app.use(express.urlencoded({ extended: true }));

//cool debugging middle ware that logs an incoming request and where it is requesting and the method
app.use((req, res, next) =>{
  console.log(`api endpoint reached: ${req.method} ${req.url}`);
  next();
})

//mounting routers
app.use("/", router);
app.use("/api", apiRouter);
app.use("/process", processorRouter);

export default app;