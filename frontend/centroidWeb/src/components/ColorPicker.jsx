export default function ColorPicker({value, onChange }) {

    return (
        <div><input
        type="color"
        value={value}
        onChange={(event) => onChange(event.target.value)}/>
        </div>
    );
}