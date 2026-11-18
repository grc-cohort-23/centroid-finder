import { useState } from "react"


export default function ThresholdPicker() {
    const [current, setCurrent] = useState(30);
    return (
        <>
            <div>
                {/* Enter slider here */}
            </div>
            <p>
                {current.toString()}
            </p>
        </>
    )
}