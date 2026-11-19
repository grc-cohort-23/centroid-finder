import { useState } from 'react'
import './App.css'
import ColorPicker from './components/ColorPicker'
import ThresholdPicker from './components/ThresholdPicker'
import VideoPicker from './components/VideoPicker'
import VideoFrame from "./components/VideoFrame";

function App() {
  const [Threshold, setThreshold] = useState(30);
  const [targetColor, setTargetColor] =useState("#FFA200");
  const [selectedVideo, setSelectedVideo] = useState("");




  return (
    <div>
      <section id="center">
        <h1>Preview Processing</h1>
        <div className='controls'>
          <div className='control'>
            <p>Target Color:</p>
            <ColorPicker
            value={targetColor}
            onChange={setTargetColor}
            />

          </div>
          <div className='control'>
            <p>Threshold:</p>
            <ThresholdPicker
            value={Threshold}
            onChange={setThreshold}
            />
          </div>
        </div>
        <div className='frameContainer'>
          <div className='frame'>
            <h2>Original Frame </h2>
            <VideoFrame filename={selectedVideo} />
            {/* <img src=''/> */}
          </div>
          <div className='frame'>
            <h2>Binarized Frame (with centroid)</h2>
            {/* <img src=''/> */}
          </div>
        </div>
        <button>Process Video with These Settings</button>
      </section>
      <section id="videos">
        <h1>Available Videos</h1>
        <VideoPicker onSelect={setSelectedVideo} />

        {selectedVideo && (
          <p>Selected video: {selectedVideo}</p>

        )}
      </section>
      <div className="ticks"></div>
    </div>
  )
}

export default App
