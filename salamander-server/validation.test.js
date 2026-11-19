
const {
    isValidColor,
    isValidThreshold
} = require("./validation");

describe("Input validation unit tests", () => {

    test("accepts valid hexadecimal colors", () => {
        expect(isValidColor("FF0000")).toBe(true);
        expect(isValidColor("00ff00")).toBe(true);
    });

    test("rejects invalid colors", () => {
        expect(isValidColor("RED")).toBe(false);
        expect(isValidColor("GG0000")).toBe(false);
        expect(isValidColor(123456)).toBe(false);
    });

    test("accepts valid thresholds", () => {
        expect(isValidThreshold(50)).toBe(true);
        expect(isValidThreshold(0)).toBe(true);
    });

    test("rejects invalid thresholds", () => {
        expect(isValidThreshold(-10)).toBe(false);
        expect(isValidThreshold("50")).toBe(false);
        expect(isValidThreshold(NaN)).toBe(false);
    });

});
