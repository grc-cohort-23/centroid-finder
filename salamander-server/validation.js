
function isValidColor(color) {
    return typeof color === "string" &&
        /^[0-9A-Fa-f]{6}$/.test(color);
}

function isValidThreshold(threshold) {
    return typeof threshold === "number" &&
        Number.isFinite(threshold) &&
        threshold >= 0;
}

module.exports = {
    isValidColor,
    isValidThreshold
};
