
import { test } from "node:test";
import assert from "node:assert/strict";

import { parseProcessingParams } from "../utils/processingParams.js";

// Shared valid input for all tests.
const validParams = {
    filename: "FlowField_Example.mp4",
    outputCsv: "output.csv",
    targetColor: "FF0000",
    threshold: "50"
};

// Test 1: Valid parameters
test("Accepts valid processing parameters", () => {
    const result = parseProcessingParams(validParams);

    assert.equal(result.ok, true);
    assert.deepEqual(result.params, {
        videoFile: "FlowField_Example.mp4",
        outputCsv: "output.csv",
        targetColor: "FF0000",
        threshold: 50
    });
});

// Test 2: Invalid hexadecimal color
test("Rejects an invalid hex color", () => {
    const result = parseProcessingParams({
        ...validParams,
        targetColor: "ZZZZZZ"
    });

    assert.equal(result.ok, false);
    assert.equal(result.message, "Invalid target color");
});

// Test 3: Invalid threshold
test("Rejects a negative threshold", () => {
    const result = parseProcessingParams({
        ...validParams,
        threshold: "-5"
    });

    assert.equal(result.ok, false);
    assert.equal(result.message, "Threshold must be an integer");
});


test("Rejects threshold above maximum range", () => {
    const result = parseProcessingParams({
        ...validParams,
        threshold: "443"
    });

    assert.equal(result.ok, false);
    assert.equal(result.message, "Threshold must be between 0 and 442");
});

test("Accepts threshold at maximum boundary", () => {
    const result = parseProcessingParams({
        ...validParams,
        threshold: "442"
    });

    assert.equal(result.ok, true);
    assert.equal(result.params.threshold, 442);
});

test("Rejects decimal threshold values", () => {
    const result = parseProcessingParams({
        ...validParams,
        threshold: "50.5"
    });

    assert.equal(result.ok, false);
    assert.equal(result.message, "Threshold must be an integer");
});

test("Rejects path traversal in CSV filenames", () => {
    const result = parseProcessingParams({
        ...validParams,
        outputCsv: "../output.csv"
    });

    assert.equal(result.ok, false);
    assert.equal(result.message, "Invalid CSV filename");
});

test("Rejects malformed video extensions", () => {
    const result = parseProcessingParams({
        ...validParams,
        filename: "example.avi"
    });

    assert.equal(result.ok, false);
    assert.equal(result.message, "Invalid video filename");
});

test("Uses default CSV filename when omitted", () => {
    const { outputCsv, ...paramsWithoutCsv } = validParams;

    const result = parseProcessingParams(paramsWithoutCsv);

    assert.equal(result.ok, true);
    assert.equal(result.params.outputCsv, "output.csv");
});
