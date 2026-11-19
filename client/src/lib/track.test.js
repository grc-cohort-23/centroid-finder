import { describe, expect, it } from 'vitest';
import { parseTrackCsv, rowIndexAt, summarize } from './track.js';

describe('parseTrackCsv', () => {
  it('parses seconds,x,y rows and flags missing frames', () => {
    const rows = parseTrackCsv('0.0,10,20\n0.033,-1,-1\r\n0.066,12,22\n');
    expect(rows).toEqual([
      { t: 0, x: 10, y: 20, found: true },
      { t: 0.033, x: -1, y: -1, found: false },
      { t: 0.066, x: 12, y: 22, found: true },
    ]);
  });

  it('skips blank and malformed lines', () => {
    expect(parseTrackCsv('\nseconds,x,y\n1,2,3\n\n')).toEqual([{ t: 1, x: 2, y: 3, found: true }]);
  });
});

describe('rowIndexAt', () => {
  const rows = parseTrackCsv('0,0,0\n1,0,0\n2,0,0');

  it('returns the last row at or before t', () => {
    expect(rowIndexAt(rows, 0)).toBe(0);
    expect(rowIndexAt(rows, 1.5)).toBe(1);
    expect(rowIndexAt(rows, 2)).toBe(2);
    expect(rowIndexAt(rows, 99)).toBe(2);
  });

  it('returns -1 before the first row or for no rows', () => {
    expect(rowIndexAt(rows, -0.1)).toBe(-1);
    expect(rowIndexAt([], 1)).toBe(-1);
  });
});

describe('summarize', () => {
  it('counts frames where the object was found', () => {
    const s = summarize(parseTrackCsv('0,1,1\n0.5,-1,-1\n1,2,2\n1.5,3,3'));
    expect(s).toEqual({ frames: 4, found: 3, foundFraction: 0.75, duration: 1.5 });
  });

  it('handles an empty result', () => {
    expect(summarize([])).toEqual({ frames: 0, found: 0, foundFraction: 0, duration: 0 });
  });
});
