
export function colorDistance(colorA, colorB) {
  const r1 = (colorA >> 16) & 0xFF;
  const g1 = (colorA >> 8) & 0xFF;
  const b1 = colorA & 0xFF;

  const r2 = (colorB >> 16) & 0xFF;
  const g2 = (colorB >> 8) & 0xFF;
  const b2 = colorB & 0xFF;

  const redDiff = r1 - r2;
  const greenDiff = g1 - g2;
  const blueDiff = b1 - b2;

  return Math.sqrt(
    redDiff ** 2 +
    greenDiff ** 2 +
    blueDiff ** 2
  );
}

/**
 * Converts ImageData into a binary pixel array
 * and a black-and-white ImageData object.
 */
export function binarizeImage(imageData, hexColor, threshold) {
  const { width, height, data } = imageData;

  const targetColor = parseInt(
    hexColor.replace("#", ""),
    16
  );

  const binaryArray = Array.from(
    { length: height },
    () => Array(width).fill(0)
  );

  // Copy the original image data before modifying it.
  const output = new ImageData(
    new Uint8ClampedArray(data),
    width,
    height
  );

  for (let r = 0; r < height; r++) {
    for (let c = 0; c < width; c++) {

      // Each pixel occupies 4 consecutive values: RGBA.
      const index = (r * width + c) * 4;

      // Reconstruct a 24-bit RGB integer.
      const pixelColor =
        (data[index] << 16) |
        (data[index + 1] << 8) |
        data[index + 2];

      const distance = colorDistance(
        pixelColor,
        targetColor
      );

      // Preserve the strict comparison from Java.
      const binaryValue = distance < threshold ? 1 : 0;

      binaryArray[r][c] = binaryValue;

      const outputColor = binaryValue === 1 ? 255 : 0;

      output.data[index] = outputColor;
      output.data[index + 1] = outputColor;
      output.data[index + 2] = outputColor;
      output.data[index + 3] = 255;
    }
  }

  return {
    binaryArray,
    binaryImage: output
  };
}
