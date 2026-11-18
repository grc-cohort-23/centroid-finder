import { useState } from 'react'
import './App.css'
import ColorPicker from './components/ColorPicker'
import ThresholdPicker from './components/ThresholdPicker'
import VideoPicker from './components/VideoPicker'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div>
      <section id="center" style={{flex: 0}}>
        <div>
          <h1>Preview Processing</h1>
          <div>
            <p>Target Color:</p>
            <ColorPicker/>
          </div>
          <div>
            <p>Threshold:</p>
            <ThresholdPicker/>
          </div>
        </div>
        <div id='frameContainer'>
          <div>
            <h2>Original Frame (with centroid)</h2>
            {/* <img src=''/> */}
          </div>
          <div>
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
      <section id="spacer"></section>
    </div>
  )
}

export default App
