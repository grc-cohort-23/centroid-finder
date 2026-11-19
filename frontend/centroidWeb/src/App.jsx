import { useState } from 'react'
import './App.css'
import ColorPicker from './components/ColorPicker'
import ThresholdPicker from './components/ThresholdPicker'
import VideoPicker from './components/VideoPicker'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div>
      <section id="center">
        <h1>Preview Processing</h1>
        <div className='controls'>
          <div className='control'>
            <p>Target Color:</p>
            <ColorPicker/>
          </div>
          <div className='control'>
            <p>Threshold:</p>
            <ThresholdPicker/>
          </div>
        </div>
        <div className='frameContainer'>
          <div className='frame'>
            <h2>Original Frame (with centroid)</h2>
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
        <VideoPicker/>
      </section>
      <div className="ticks"></div>
    </div>
  )
}

export default App
