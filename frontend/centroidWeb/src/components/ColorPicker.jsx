export default function ColorPicker({value, onChange }) {

    return (
        <div><input
        id="target-color"
        type="color"
        value={value.toLowerCase()}
        onChange={(event) => onChange(event.target.value)}/>
        </div>
    );
}
