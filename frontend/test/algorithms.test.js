
import assert from 'node:assert/strict';

import { colorDistance } from '../src/utils/imageProcessing.js';
import { findConnectedGroups } from '../src/utils/groupFinder.js';


/* 
test expected results for colorDistance() and findConnectedGroups().    
*/
describe('Centroid Finder Algorithms', () => {

  describe('colorDistance()', () => {

    it('returns 0 for identical colors', () => {
      const result = colorDistance(0xFF0000, 0xFF0000);

      assert.equal(result, 0);
    });
    
/////////////////////////////////////////////////////////////
    it('calculates the distance between red and black', () => {
      const result = colorDistance(0xFF0000, 0x000000);

      assert.equal(result, 255);
    });

  });

  describe('findConnectedGroups()', () => {

/////////////////////////////////////////////////////////////
    it('finds the largest connected group', () => {
      const binaryArray = [
        [1, 1, 0, 0],
        [0, 1, 0, 1],
        [0, 0, 0, 1]
      ];

      const groups = findConnectedGroups(binaryArray);

      assert.equal(groups[0].size, 3);
      assert.equal(groups[1].size, 2);
    });

/////////////////////////////////////////////////////////////
    it('calculates a groups centroid', () => {
      const binaryArray = [
        [0, 0, 0],
        [1, 1, 0],
        [0, 1, 0]
      ];

      const groups = findConnectedGroups(binaryArray);

      assert.equal(groups[0].size, 3);
      assert.equal(groups[0].x, 0);
      assert.equal(groups[0].y, 1);
    });

/////////////////////////////////////////////////////////////
    it('returns no groups when every pixel is black', () => {
    const binaryArray = [
        [0, 0, 0],
        [0, 0, 0]
    ];

    const groups = findConnectedGroups(binaryArray);

    assert.equal(groups.length, 0);
    });

/////////////////////////////////////////////////////////////
    it('does not connect diagonally adjacent pixels', () => {
    const binaryArray = [
        [1, 0],
        [0, 1]
    ];

    const groups = findConnectedGroups(binaryArray);

    assert.equal(groups.length, 2);
    assert.equal(groups[0].size, 1);
    assert.equal(groups[1].size, 1);
    });
  });

});
