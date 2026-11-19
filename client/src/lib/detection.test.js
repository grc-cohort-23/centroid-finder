import { describe, expect, it } from 'vitest';
import {
  MAX_DISTANCE,
  binarize,
  colorDistance,
  detect,
  findGroups,
  hexToRgb,
  largestCentroid,
  rgbToHex,
  sampleColor,
} from './detection.js';

// Builds RGBA pixel data from rows of hex colors.
function pixelsFrom(rows) {
  const height = rows.length;
  const width = rows[0].length;
  const data = new Uint8ClampedArray(width * height * 4);
  rows.flat().forEach((hex, i) => {
    const { r, g, b } = hexToRgb(hex);
    data.set([r, g, b, 255], i * 4);
  });
  return { data, width, height };
}

// Builds a 0/1 mask from strings like "1.1" (1 = match).
function maskFrom(lines) {
  const width = lines[0].length;
  const mask = new Uint8Array(width * lines.length);
  lines.join('').split('').forEach((ch, i) => { mask[i] = ch === '1' ? 1 : 0; });
  return { mask, width, height: lines.length };
}

describe('hex conversion', () => {
  it('parses hex with or without #', () => {
    expect(hexToRgb('ff8000')).toEqual({ r: 255, g: 128, b: 0 });
    expect(hexToRgb('#00FF7f')).toEqual({ r: 0, g: 255, b: 127 });
  });

  it('rejects malformed hex', () => {
    expect(() => hexToRgb('fff')).toThrow();
    expect(() => hexToRgb('gg0000')).toThrow();
  });

  it('round-trips through rgbToHex with zero padding', () => {
    expect(rgbToHex({ r: 1, g: 2, b: 3 })).toBe('010203');
    expect(rgbToHex(hexToRgb('a1b2c3'))).toBe('a1b2c3');
  });
});

describe('colorDistance (matches EuclideanColorDistance)', () => {
  it('is 0 for identical colors', () => {
    expect(colorDistance(hexToRgb('123456'), hexToRgb('123456'))).toBe(0);
  });

  it('computes the Euclidean RGB distance', () => {
    expect(colorDistance(hexToRgb('000000'), hexToRgb('030400'))).toBe(5);
  });

  it('max distance black to white is covered by MAX_DISTANCE', () => {
    const d = colorDistance(hexToRgb('000000'), hexToRgb('ffffff'));
    expect(d).toBeCloseTo(441.67, 1);
    expect(MAX_DISTANCE).toBeGreaterThanOrEqual(d);
  });
});

describe('binarize (matches DistanceImageBinarizer)', () => {
  it('marks pixels strictly closer than the threshold', () => {
    // 000000 -> 030400 is distance exactly 5.
    const { data, width, height } = pixelsFrom([['000000', '030400', 'ffffff']]);
    const target = hexToRgb('000000');
    expect(Array.from(binarize(data, width, height, target, 5))).toEqual([1, 0, 0]);
    expect(Array.from(binarize(data, width, height, target, 6))).toEqual([1, 1, 0]);
  });

  it('threshold 0 matches nothing, even exact colors', () => {
    const { data, width, height } = pixelsFrom([['ff0000']]);
    expect(Array.from(binarize(data, width, height, hexToRgb('ff0000'), 0))).toEqual([0]);
  });

  it('ignores the alpha channel', () => {
    const data = new Uint8ClampedArray([255, 0, 0, 0]);
    expect(Array.from(binarize(data, 1, 1, hexToRgb('ff0000'), 1))).toEqual([1]);
  });
});

describe('findGroups (matches DfsBinaryGroupFinder)', () => {
  it('connects horizontally and vertically but not diagonally', () => {
    const { mask, width, height } = maskFrom([
      '10',
      '01',
    ]);
    expect(findGroups(mask, width, height)).toHaveLength(2);
  });

  it('uses integer division for centroids', () => {
    // Pixels at x = 0,1 and y = 0 -> centroid x = 1/2 = 0.
    const { mask, width, height } = maskFrom(['11']);
    expect(findGroups(mask, width, height)).toEqual([{ size: 2, x: 0, y: 0 }]);
  });

  it('sorts by size, then x, then y, descending', () => {
    const { mask, width, height } = maskFrom([
      '1.1...1',
      '..1...1',
      '1.....1',
    ]);
    const groups = findGroups(mask, width, height);
    expect(groups.map((g) => g.size)).toEqual([3, 2, 1, 1]);
    // The two single pixels at x=0 tie on size and x; larger y comes first.
    expect(groups.slice(2)).toEqual([
      { size: 1, x: 0, y: 2 },
      { size: 1, x: 0, y: 0 },
    ]);
  });

  it('handles a group that winds around', () => {
    const { mask, width, height } = maskFrom([
      '111',
      '1.1',
      '1.1',
    ]);
    // x: 0+1+2+0+2+0+2 = 7 -> 7/7 = 1; y: 0+0+0+1+1+2+2 = 6 -> 6/7 = 0
    expect(findGroups(mask, width, height)).toEqual([{ size: 7, x: 1, y: 0 }]);
  });
});

describe('largestCentroid (matches LargestGroupSelector)', () => {
  it('returns (-1, -1) when there are no groups', () => {
    expect(largestCentroid([])).toEqual({ x: -1, y: -1 });
  });

  it('returns the first (largest) group centroid', () => {
    expect(largestCentroid([{ size: 9, x: 4, y: 5 }, { size: 1, x: 0, y: 0 }])).toEqual({ x: 4, y: 5 });
  });
});

describe('detect', () => {
  it('finds the centroid of the largest matching blob', () => {
    const R = 'ff0000';
    const K = '000000';
    const { data, width, height } = pixelsFrom([
      [R, K, K, K],
      [K, K, R, R],
      [K, K, R, R],
    ]);
    const result = detect(data, width, height, hexToRgb('ff0000'), 50);
    expect(result.groups).toHaveLength(2);
    expect(result.centroid).toEqual({ x: 2, y: 1 });
    expect(result.matchedFraction).toBeCloseTo(5 / 12);
  });
});

describe('sampleColor', () => {
  it('averages the neighbourhood and clamps at the edges', () => {
    const { data, width, height } = pixelsFrom([
      ['000000', '646464'],
      ['646464', '646464'],
    ]);
    // Corner sample with radius 1 covers all 4 pixels: (0 + 100*3) / 4 = 75.
    expect(sampleColor(data, width, height, 0, 0, 1)).toEqual({ r: 75, g: 75, b: 75 });
    expect(sampleColor(data, width, height, 0, 0, 0)).toEqual({ r: 0, g: 0, b: 0 });
  });
});
