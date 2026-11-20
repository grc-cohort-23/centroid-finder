
/**
 * Finds connected groups of white pixels (1).
 * Uses four-directional connectivity, not diagonals.
 */
export function findConnectedGroups(binaryArray) {
  const height = binaryArray.length;
  if (height === 0) return [];

  const width = binaryArray[0].length;

  const visited = Array.from(
    { length: height },
    () => Array(width).fill(false)
  );

  const directions = [
    [-1, 0], // Up
    [1, 0],  // Down
    [0, -1], // Left
    [0, 1]   // Right
  ];

  const groups = [];

  for (let r = 0; r < height; r++) {
    for (let c = 0; c < width; c++) {

      // Only begin a search at an unvisited white pixel.
      if (binaryArray[r][c] === 0 || visited[r][c]) {
        continue;
      }

      const stack = [[r, c]];
      visited[r][c] = true;

      let size = 0;
      let sumX = 0;
      let sumY = 0;

      while (stack.length > 0) {
        const [row, col] = stack.pop();

        size++;
        sumX += col;
        sumY += row;

        for (const [dr, dc] of directions) {
          const nextRow = row + dr;
          const nextCol = col + dc;

          if (
            nextRow >= 0 &&
            nextRow < height &&
            nextCol >= 0 &&
            nextCol < width &&
            binaryArray[nextRow][nextCol] === 1 &&
            !visited[nextRow][nextCol]
          ) {
            visited[nextRow][nextCol] = true;
            stack.push([nextRow, nextCol]);
          }
        }
      }

      groups.push({
        size,
        x: Math.floor(sumX / size),
        y: Math.floor(sumY / size)
      });
    }
  }

  // Largest groups first.
  groups.sort((a, b) => b.size - a.size);

  return groups;
}
