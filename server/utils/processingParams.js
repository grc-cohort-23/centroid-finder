
export function parseProcessingParams({
  filename,
  outputCsv = "output.csv",
  targetColor,
  threshold
}) {

  // Validate video filename
  if (typeof filename !== "string" ||
      !/^[a-zA-Z0-9_-]+\.mp4$/i.test(filename)) {
    return { ok: false, message: "Invalid video filename" };
  }

  // Validate CSV filename
  if (typeof outputCsv !== "string" ||
      !/^[a-zA-Z0-9_-]+\.csv$/i.test(outputCsv)) {
    return { ok: false, message: "Invalid CSV filename" };
  }

  // Validate RRGGBB hexadecimal color
  if (typeof targetColor !== "string" ||
      !/^[0-9a-fA-F]{6}$/.test(targetColor)) {
    return { ok: false, message: "Invalid target color" };
  }

  // Validate threshold format
  if (typeof threshold !== "string" ||
      !/^(0|[1-9]\d*)$/.test(threshold)) {
    return { ok: false, message: "Threshold must be an integer" };
  }

  const parsedThreshold = Number(threshold);

  // Validate threshold range
  if (parsedThreshold > 442) {
    return { ok: false, message: "Threshold must be between 0 and 442" };
  }

  // All parameters passed validation
  return {
    ok: true,
    params: {
      videoFile: filename,
      outputCsv,
      targetColor,
      threshold: parsedThreshold
    }
  };
}
