import dotenv from 'dotenv';
import app from "./app.js";

// Load environment variables from .env file
dotenv.config();

//start server
const PORT = Number(process.env.PORT) || 3000;



app.listen(PORT, () => {
  console.log(`Server is listening at http://localhost:${PORT}`);
});



