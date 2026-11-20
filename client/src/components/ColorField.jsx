import { useEffect, useState } from 'react';

// Swatch (opens the native color picker) plus a hex text box.
// `value` is 6 hex digits without '#'.
export default function ColorField({ value, onChange }) {
  const [draft, setDraft] = useState(value);

  useEffect(() => setDraft(value), [value]);

  function commit(text) {
    const clean = text.trim().replace(/^#/, '').toLowerCase();
    if (/^[0-9a-f]{6}$/.test(clean)) onChange(clean);
    else setDraft(value);
  }

  return (
    <div className="color-field">
      <label className="swatch-btn" style={{ background: `#${value}` }} title="Choose a color">
        <input
          type="color"
          value={`#${value}`}
          onChange={(e) => onChange(e.target.value.slice(1).toLowerCase())}
          aria-label="Target color picker"
        />
      </label>
      <span className="hash mono">#</span>
      <input
        className="hex mono"
        value={draft}
        maxLength={7}
        spellCheck="false"
        onChange={(e) => setDraft(e.target.value)}
        onBlur={(e) => commit(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && commit(e.currentTarget.value)}
        aria-label="Target color hex"
      />
    </div>
  );
}
