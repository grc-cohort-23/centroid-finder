import express from 'express';
import appRouter from './routes/app.routes.js';
import testRouter from './routes/test.routes.js';

const PORT = 4002;
const app = express();

app.use(express.static('public'));

app.use(appRouter);
app.use("/test", testRouter);

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
})