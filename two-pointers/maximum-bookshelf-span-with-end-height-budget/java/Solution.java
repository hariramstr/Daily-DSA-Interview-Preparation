/*
Problem Title: Maximum Bookshelf Span With End-Height Budget

Problem Description:
You are given an array heights where heights[i] is the height of the i-th bookshelf in a long archive hallway.
A moving ladder can be placed between any two different bookshelves i and j with i < j.
The ladder is considered stable only if the absolute difference between the heights of the two endpoint bookshelves is at most budget.

For every stable pair (i, j), define its span score as:

(j - i) * min(heights[i], heights[j])

Your task is to return the maximum possible span score among all stable pairs. If no stable pair exists, return 0.

This problem is designed to look similar to classic endpoint-selection tasks, but the added endpoint-height budget changes which pairs are valid.
A brute-force O(n^2) scan will time out for large inputs. You need to exploit the fact that only endpoint pairs matter and design an efficient strategy using two pointers, pruning, and careful reasoning about when moving the left or right pointer cannot improve the answer.

Constraints:
- 2 <= heights.length <= 200000
- 1 <= heights[i] <= 1000000000
- 0 <= budget <= 1000000000
- Return the maximum score as a 64-bit integer

Example 1:
Input: heights = [5, 1, 4, 6, 3], budget = 2
Output: 15
Explanation: Valid stable pairs must have endpoint heights differing by at most 2.
Pair (0, 3) is valid because |5 - 6| = 1, and its score is (3 - 0) * min(5, 6) = 3 * 5 = 15.
The maximum is 15.

Example 2:
Input: heights = [9, 2, 8, 1, 7], budget = 0
Output: 0
Explanation: With budget 0, the two endpoint bookshelves must have exactly equal heights.
No two different positions contain the same height, so no stable pair exists and the answer is 0.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum span score among all pairs of bookshelves whose endpoint heights
     * differ by at most the given budget.
     *
     * Core idea:
     * We process bookshelves in descending order of height. At any moment, all "active" indices
     * correspond to heights >= currentHeight. For a fixed currentHeight = h, any valid pair that
     * contributes area at least width * h must use two endpoints whose heights are both >= h and
     * whose actual endpoint heights differ by at most budget.
     *
     * To efficiently find the farthest valid partner for each newly activated index, we maintain:
     * 1) A TreeMap keyed by height, where each height stores the minimum and maximum index currently active.
     * 2) A segment tree over compressed heights that can answer, inside a height interval
     *    [h, h + budget], the global minimum active index and global maximum active index.
     *
     * Why this works:
     * - When processing height h, every newly activated index i has exact height h.
     * - Any valid pair involving i and some already active index j must satisfy:
     *      heights[j] in [h, h + budget]
     *   because active heights are >= h, and |heights[i] - heights[j]| <= budget.
     * - Among all such j, the best width for i is achieved by the farthest index in that range,
     *   which is either the minimum index in the range or the maximum index in the range.
     * - Since the score is width * min(heights[i], heights[j]) and heights[i] = h <= heights[j],
     *   the score for such a pair is width * h.
     * - Every valid pair is considered exactly when the smaller endpoint height becomes active.
     *
     * This yields an O(n log n) solution, which is fast enough for n up to 200000.
     *
     * @param heights the heights of the bookshelves
     * @param budget the maximum allowed absolute difference between endpoint heights
     * @return the maximum stable span score as a 64-bit integer; returns 0 if no valid pair exists
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumBookshelfSpan(int[] heights, int budget) {
        int n = heights.length;

        // Step 1:
        // Coordinate-compress the distinct heights so that we can build a compact segment tree.
        // We only need ordering and range queries by height, not the original large values directly.
        int[] sortedUniqueHeights = compressHeights(heights);

        // Step 2:
        // Group indices by their exact height.
        // We will process heights from largest to smallest, activating all indices of the same height together.
        TreeMap<Integer, List<Integer>> indicesByHeight = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            indicesByHeight.computeIfAbsent(heights[i], k -> new ArrayList<>()).add(i);
        }

        // Step 3:
        // Segment tree stores, for each compressed height position:
        // - the minimum active index having that height
        // - the maximum active index having that height
        //
        // If a height is not active yet, it contributes:
        // - min = +infinity
        // - max = -infinity
        SegmentTree segmentTree = new SegmentTree(sortedUniqueHeights.length);

        long answer = 0L;

        // Step 4:
        // Process heights in descending order.
        //
        // Important reasoning:
        // When we are at height h, all already-active heights are >= h.
        // For a newly activated index i with height h, any valid partner j among active indices
        // must have height in [h, h + budget].
        //
        // We first query using the current active set BEFORE inserting the current height group.
        // This ensures we only pair current-height indices with strictly higher heights at this stage.
        // Then after insertion, we also handle same-height pairs within the current group and with
        // previously inserted same-height indices naturally through the updated min/max.
        NavigableMap<Integer, List<Integer>> descending = indicesByHeight.descendingMap();

        for (Map.Entry<Integer, List<Integer>> entry : descending.entrySet()) {
            int currentHeight = entry.getKey();
            List<Integer> currentIndices = entry.getValue();

            // Determine the compressed range of heights [currentHeight, currentHeight + budget].
            int leftPos = lowerBound(sortedUniqueHeights, currentHeight);
            long upperHeightLong = (long) currentHeight + budget;
            int upperHeight = upperHeightLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) upperHeightLong;
            int rightExclusive = upperBound(sortedUniqueHeights, upperHeight);

            // Query the currently active indices in the allowed height range.
            // These are candidate partners with heights in [currentHeight, currentHeight + budget].
            SegmentTree.RangeResult beforeInsert = segmentTree.query(leftPos, rightExclusive - 1);

            // For every index of the current height, compute the best possible width against
            // already active valid partners.
            //
            // Since the score multiplier is currentHeight (the smaller endpoint height),
            // we only need the farthest valid partner by index distance.
            if (beforeInsert.hasAny()) {
                for (int index : currentIndices) {
                    long widthUsingMin = beforeInsert.minIndex == Integer.MAX_VALUE
                            ? -1L
                            : Math.abs((long) index - beforeInsert.minIndex);
                    long widthUsingMax = beforeInsert.maxIndex == Integer.MIN_VALUE
                            ? -1L
                            : Math.abs((long) index - beforeInsert.maxIndex);

                    long bestWidth = Math.max(widthUsingMin, widthUsingMax);
                    if (bestWidth > 0) {
                        answer = Math.max(answer, bestWidth * currentHeight);
                    }
                }
            }

            // Insert all indices of the current height into the segment tree.
            //
            // We only need the minimum and maximum index for this exact height, because for any future
            // query, only the farthest index matters, and that must be one of the extremes.
            int minIndexAtHeight = Integer.MAX_VALUE;
            int maxIndexAtHeight = Integer.MIN_VALUE;
            for (int index : currentIndices) {
                minIndexAtHeight = Math.min(minIndexAtHeight, index);
                maxIndexAtHeight = Math.max(maxIndexAtHeight, index);
            }

            int compressedPos = lowerBound(sortedUniqueHeights, currentHeight);
            segmentTree.update(compressedPos, minIndexAtHeight, maxIndexAtHeight);

            // After insertion, same-height pairs become possible as well.
            // Since same-height pairs always satisfy the budget condition when budget >= 0,
            // the best same-height pair for this height is simply the farthest two indices
            // among all active indices of this exact height, which are minIndexAtHeight and maxIndexAtHeight.
            //
            // If there are at least two occurrences of this height, this contributes:
            // (maxIndexAtHeight - minIndexAtHeight) * currentHeight
            if (currentIndices.size() >= 2) {
                long sameHeightWidth = (long) maxIndexAtHeight - minIndexAtHeight;
                if (sameHeightWidth > 0) {
                    answer = Math.max(answer, sameHeightWidth * currentHeight);
                }
            }
        }

        return answer;
    }

    /**
     * Builds a sorted array of distinct heights from the input.
     *
     * @param heights the original heights array
     * @return sorted distinct heights
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int[] compressHeights(int[] heights) {
        int[] copy = Arrays.copyOf(heights, heights.length);
        Arrays.sort(copy);

        int uniqueCount = 0;
        for (int value : copy) {
            if (uniqueCount == 0 || copy[uniqueCount - 1] != value) {
                copy[uniqueCount++] = value;
            }
        }
        return Arrays.copyOf(copy, uniqueCount);
    }

    /**
     * Returns the first index i such that array[i] >= target.
     *
     * @param array sorted array
     * @param target target value
     * @return lower-bound position
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] array, int target) {
        int left = 0;
        int right = array.length;
        while (left < right) {
            int mid = left + ((right - left) >>> 1);
            if (array[mid] >= target) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }

    /**
     * Returns the first index i such that array[i] > target.
     *
     * @param array sorted array
     * @param target target value
     * @return upper-bound position
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int upperBound(int[] array, int target) {
        int left = 0;
        int right = array.length;
        while (left < right) {
            int mid = left + ((right - left) >>> 1);
            if (array[mid] > target) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) for the demonstration itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] heights1 = {5, 1, 4, 6, 3};
        int budget1 = 2;
        long result1 = solution.maximumBookshelfSpan(heights1, budget1);
        System.out.println(result1); // Expected: 15

        int[] heights2 = {9, 2, 8, 1, 7};
        int budget2 = 0;
        long result2 = solution.maximumBookshelfSpan(heights2, budget2);
        System.out.println(result2); // Expected: 0
    }

    /**
     * Segment tree that supports:
     * - point update: activate/update one compressed height with its min/max index
     * - range query: obtain the global minimum active index and global maximum active index
     *   across a height interval
     *
     * This is exactly what we need because for a fixed endpoint index i, the farthest valid partner
     * inside a height range must be one of the extreme indices in that range.
     */
    static class SegmentTree {
        private final int size;
        private final int[] minTree;
        private final int[] maxTree;

        /**
         * Creates a segment tree for the given number of compressed height positions.
         *
         * @param n number of leaves
         * @return nothing
         * Time complexity: O(n)
         * Space complexity: O(n)
         */
        SegmentTree(int n) {
            int s = 1;
            while (s < n) {
                s <<= 1;
            }
            this.size = s;
            this.minTree = new int[size << 1];
            this.maxTree = new int[size << 1];

            Arrays.fill(minTree, Integer.MAX_VALUE);
            Arrays.fill(maxTree, Integer.MIN_VALUE);
        }

        /**
         * Updates one compressed height position with the given minimum and maximum index.
         *
         * @param pos compressed height position
         * @param minIndex minimum active index for this exact height
         * @param maxIndex maximum active index for this exact height
         * @return nothing
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        public void update(int pos, int minIndex, int maxIndex) {
            int node = pos + size;
            minTree[node] = minIndex;
            maxTree[node] = maxIndex;
            node >>= 1;

            while (node > 0) {
                minTree[node] = Math.min(minTree[node << 1], minTree[(node << 1) | 1]);
                maxTree[node] = Math.max(maxTree[node << 1], maxTree[(node << 1) | 1]);
                node >>= 1;
            }
        }

        /**
         * Queries the inclusive compressed height range [left, right].
         *
         * @param left left boundary (inclusive)
         * @param right right boundary (inclusive)
         * @return a RangeResult containing the minimum and maximum active index in the range
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        public RangeResult query(int left, int right) {
            if (left > right) {
                return new RangeResult(Integer.MAX_VALUE, Integer.MIN_VALUE);
            }

            int minValue = Integer.MAX_VALUE;
            int maxValue = Integer.MIN_VALUE;

            int l = left + size;
            int r = right + size;

            while (l <= r) {
                if ((l & 1) == 1) {
                    minValue = Math.min(minValue, minTree[l]);
                    maxValue = Math.max(maxValue, maxTree[l]);
                    l++;
                }
                if ((r & 1) == 0) {
                    minValue = Math.min(minValue, minTree[r]);
                    maxValue = Math.max(maxValue, maxTree[r]);
                    r--;
                }
                l >>= 1;
                r >>= 1;
            }

            return new RangeResult(minValue, maxValue);
        }

        /**
         * Small immutable holder for a range query result.
         */
        static class RangeResult {
            final int minIndex;
            final int maxIndex;

            /**
             * Creates a range result.
             *
             * @param minIndex minimum active index in the queried range
             * @param maxIndex maximum active index in the queried range
             * @return nothing
             * Time complexity: O(1)
             * Space complexity: O(1)
             */
            RangeResult(int minIndex, int maxIndex) {
                this.minIndex = minIndex;
                this.maxIndex = maxIndex;
            }

            /**
             * Checks whether the queried range contains at least one active index.
             *
             * @param none no parameters
             * @return true if at least one active index exists in the range; otherwise false
             * Time complexity: O(1)
             * Space complexity: O(1)
             */
            boolean hasAny() {
                return minIndex != Integer.MAX_VALUE;
            }
        }
    }
}