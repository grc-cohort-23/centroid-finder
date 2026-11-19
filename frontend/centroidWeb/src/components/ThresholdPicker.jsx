export default function ThresholdPicker({value, onChange}) {

    return (
        <>
        <div>
            <input
            id="threshold"
            type="range"
            min="0"
            max="442"
            value={value}
            onChange={(event) => onChange(Number(event.target.value))}
            />
            </div>
             <p>{value}</p>
        </>
    )
}
