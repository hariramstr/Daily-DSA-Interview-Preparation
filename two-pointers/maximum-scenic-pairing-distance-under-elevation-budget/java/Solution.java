import java.util.*;

/*
Problem Title: Maximum Scenic Pairing Distance Under Elevation Budget

Problem Description:
You are given an array elevations of length n, where elevations[i] is the height of the i-th viewpoint along a mountain road.
A tourism agency wants to choose two viewpoints i and j with i < j to place synchronized photo beacons.
The pair is considered valid if the elevation difference between the two viewpoints is at most budget, that is:

|elevations[i] - elevations[j]| <= budget

The scenic value of a valid pair is defined as the distance between the viewpoints multiplied by the lower of the two elevations:

scenicValue(i, j) = (j - i) * min(elevations[i], elevations[j])

Return the maximum scenic value among all valid pairs. If no valid pair exists, return 0.

A brute-force O(n^2) search will time out. The challenge is to exploit the structure of the score and the elevation-difference constraint efficiently.
Note that the array is not sorted, and the best answer may involve skipping many endpoints that look locally attractive.
An optimal solution is expected to combine two-pointer reasoning with additional preprocessing or data-structure support.

Constraints:
- 2 <= n <= 200000
- 1 <= elevations[i] <= 1000000000
- 0 <= budget <= 1000000000

Example 1:
Input: elevations = [8, 1, 6, 2, 5, 7], budget = 2
Output: 35

Explanation:
The pair (0, 5) has heights 8 and 7, difference 1 <= 2, and scenic value = (5 - 0) * min(8, 7) = 5 * 7 = 35.
This is the optimal valid pair.

Example 2:
Input: elevations = [3, 10, 4, 9, 2], budget = 0
Output: 0

Explanation:
A budget of 0 means the two chosen viewpoints must have exactly the same elevation.
No two positions share the same height, so no valid pair exists.
*/

public class Solution {

    /**
     * Computes the maximum scenic value among all valid pairs.
     *
     * Core idea:
     * We process viewpoints in descending order of elevation.
     * Suppose we are currently handling height h at index idx.
     * Any previously processed viewpoint has height >= h.
     * Therefore, for any valid pair formed with the current viewpoint as the lower endpoint,
     * the scenic value is:
     *
     *     distance * h
     *
     * because min(h, higherHeight) = h.
     *
     * So for the current viewpoint, we only need the farthest previously processed index whose
     * height lies in [h, h + budget].
     *
     * To support this efficiently:
     * 1. Coordinate-compress all distinct heights.
     * 2. Process indices grouped by equal height from largest height to smallest height.
     * 3. Maintain a segment tree over compressed heights.
     *    For each height value already activated, store:
     *      - minimum index where this height appeared
     *      - maximum index where this height appeared
     * 4. For current height h, query the segment tree over compressed heights in [h, h + budget]
     *    to get the global minimum and maximum index among all already-processed valid partner heights.
     * 5. The best distance to current index idx is:
     *      max(abs(idx - minIndex), abs(idx - maxIndex))
     *    and scenic value is that distance multiplied by h.
     *
     * Important processing detail:
     * We must first query all indices of the current height group, and only after that insert them
     * into the segment tree. This guarantees that when budget = 0, equal-height pairs are still found,
     * but only with strictly different indices from earlier occurrences of the same height group?
     *
     * Actually, to allow equal-height pairs among the same height, we need a careful approach:
     * - First, query against strictly higher heights already inserted.
     * - Then, for the current equal-height group itself, the best equal-height pair is simply
     *   (maxIndexInGroup - minIndexInGroup) * h, because all equal heights satisfy the budget if budget >= 0.
     * - Finally, insert the whole group.
     *
     * This handles all cases correctly.
     *
     * @param elevations the array of viewpoint elevations
     * @param budget the maximum allowed absolute elevation difference
     * @return the maximum scenic value among all valid pairs; 0 if no valid pair exists
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumScenicValue(int[] elevations, int budget) {
        int n = elevations.length;

        // Step 1:
        // Create an array of (height, index) pairs so that we can sort viewpoints by height.
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Point(elevations[i], i);
        }

        // Sort by height ascending first.
        // We will later process from the end toward the beginning, which means descending heights.
        Arrays.sort(points, Comparator.comparingInt(a -> a.height));

        // Step 2:
        // Build the sorted list of distinct heights for coordinate compression.
        int[] distinctHeights = buildDistinctSortedHeights(points);

        // Segment tree stores, for each compressed height position:
        // - the minimum index seen so far for that height
        // - the maximum index seen so far for that height
        SegmentTree segTree = new SegmentTree(distinctHeights.length);

        long answer = 0L;

        // Step 3:
        // Process heights in descending order, grouped by equal height.
        int p = n - 1;
        while (p >= 0) {
            int currentHeight = points[p].height;
            int groupEnd = p;

            // Move left to find the full group of equal heights.
            while (p >= 0 && points[p].height == currentHeight) {
                p--;
            }
            int groupStart = p + 1;

            // The current group is points[groupStart..groupEnd], all with the same height.
            // We will:
            //   A) query each index in the group against already inserted strictly higher heights
            //   B) compute best pair entirely inside this equal-height group
            //   C) insert the whole group into the segment tree

            // Find compressed range [currentHeight, currentHeight + budget].
            int leftCompressed = lowerBound(distinctHeights, currentHeight);
            long upperHeightLong = (long) currentHeight + budget;
            int upperHeight = upperHeightLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) upperHeightLong;
            int rightCompressedExclusive = upperBound(distinctHeights, upperHeight);

            // A) Query against strictly higher heights already inserted.
            // Since current group is not inserted yet, the tree contains only heights > currentHeight.
            if (leftCompressed < rightCompressedExclusive) {
                SegmentTree.Result rangeResult = segTree.query(leftCompressed, rightCompressedExclusive - 1);

                if (rangeResult.hasAny()) {
                    for (int i = groupStart; i <= groupEnd; i++) {
                        int idx = points[i].index;

                        // The farthest valid partner among already inserted heights
                        // must be either the smallest index or the largest index in the valid height range.
                        long bestDistance = 0L;
                        if (rangeResult.minIndex != SegmentTree.INF) {
                            bestDistance = Math.max(bestDistance, Math.abs((long) idx - rangeResult.minIndex));
                        }
                        if (rangeResult.maxIndex != SegmentTree.NEG_INF) {
                            bestDistance = Math.max(bestDistance, Math.abs((long) idx - rangeResult.maxIndex));
                        }

                        answer = Math.max(answer, bestDistance * currentHeight);
                    }
                }
            }

            // B) Handle pairs entirely inside the current equal-height group.
            // Since equal heights always satisfy |h - h| = 0 <= budget, these pairs are valid for every budget >= 0.
            // The best such pair uses the leftmost and rightmost indices in the group.
            int minIndexInGroup = Integer.MAX_VALUE;
            int maxIndexInGroup = Integer.MIN_VALUE;
            for (int i = groupStart; i <= groupEnd; i++) {
                minIndexInGroup = Math.min(minIndexInGroup, points[i].index);
                maxIndexInGroup = Math.max(maxIndexInGroup, points[i].index);
            }
            if (maxIndexInGroup > minIndexInGroup) {
                answer = Math.max(answer, (long) (maxIndexInGroup - minIndexInGroup) * currentHeight);
            }

            // C) Insert the whole current group into the segment tree.
            // After this, lower heights can use these heights as valid "higher or equal" partners.
            int compressedPos = lowerBound(distinctHeights, currentHeight);
            segTree.update(compressedPos, minIndexInGroup, maxIndexInGroup);
        }

        return answer;
    }

    /**
     * Builds a sorted array of distinct heights from the sorted points array.
     *
     * @param points array sorted by height in ascending order
     * @return sorted distinct heights
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildDistinctSortedHeights(Point[] points) {
        int n = points.length;
        int[] temp = new int[n];
        int size = 0;

        for (Point point : points) {
            if (size == 0 || temp[size - 1] != point.height) {
                temp[size++] = point.height;
            }
        }

        return Arrays.copyOf(temp, size);
    }

    /**
     * Finds the first index i such that arr[i] >= target.
     *
     * @param arr sorted array
     * @param target target value
     * @return first position with value >= target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] arr, int target) {
        int left = 0;
        int right = arr.length;

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] >= target) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Finds the first index i such that arr[i] > target.
     *
     * @param arr sorted array
     * @param target target value
     * @return first position with value > target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int upperBound(int[] arr, int target) {
        int left = 0;
        int right = arr.length;

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] > target) {
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
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called method work
     * Space complexity: O(1), excluding called method work
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] elevations1 = {8, 1, 6, 2, 5, 7};
        int budget1 = 2;
        System.out.println(solution.maximumScenicValue(elevations1, budget1)); // Expected: 35

        int[] elevations2 = {3, 10, 4, 9, 2};
        int budget2 = 0;
        System.out.println(solution.maximumScenicValue(elevations2, budget2)); // Expected: 0

        int[] elevations3 = {5, 5};
        int budget3 = 0;
        System.out.println(solution.maximumScenicValue(elevations3, budget3)); // Expected: 5

        int[] elevations4 = {1, 100, 1, 100, 1};
        int budget4 = 0;
        System.out.println(solution.maximumScenicValue(elevations4, budget4)); // Expected: 200
    }

    /**
     * Simple helper class representing one viewpoint.
     */
    static class Point {
        int height;
        int index;

        Point(int height, int index) {
            this.height = height;
            this.index = index;
        }
    }

    /**
     * Segment tree that supports:
     * - point update with a min index and max index
     * - range query returning the global min index and global max index
     *
     * This is exactly what we need because for a fixed current index,
     * the farthest partner in a set of candidate indices is always one of the extremes.
     */
    static class SegmentTree {
        static final int INF = Integer.MAX_VALUE;
        static final int NEG_INF = Integer.MIN_VALUE;

        int size;
        int[] minTree;
        int[] maxTree;

        SegmentTree(int n) {
            size = 1;
            while (size < n) {
                size <<= 1;
            }

            minTree = new int[size << 1];
            maxTree = new int[size << 1];

            Arrays.fill(minTree, INF);
            Arrays.fill(maxTree, NEG_INF);
        }

        /**
         * Updates one compressed height position with the given minimum and maximum index.
         * If the position already contains data, we merge by taking:
         * - smaller minimum index
         * - larger maximum index
         *
         * @param pos compressed height position
         * @param minIndex minimum index for this height
         * @param maxIndex maximum index for this height
         * @return nothing
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        void update(int pos, int minIndex, int maxIndex) {
            int node = pos + size;

            minTree[node] = Math.min(minTree[node], minIndex);
            maxTree[node] = Math.max(maxTree[node], maxIndex);

            node >>= 1;
            while (node >= 1) {
                minTree[node] = Math.min(minTree[node << 1], minTree[(node << 1) | 1]);
                maxTree[node] = Math.max(maxTree[node << 1], maxTree[(node << 1) | 1]);
                node >>= 1;
            }
        }

        /**
         * Queries the inclusive range [left, right].
         *
         * @param left left endpoint, inclusive
         * @param right right endpoint, inclusive
         * @return a Result containing the minimum and maximum index found in the range
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        Result query(int left, int right) {
            left += size;
            right += size;

            int bestMin = INF;
            int bestMax = NEG_INF;

            while (left <= right) {
                if ((left & 1) == 1) {
                    bestMin = Math.min(bestMin, minTree[left]);
                    bestMax = Math.max(bestMax, maxTree[left]);
                    left++;
                }
                if ((right & 1) == 0) {
                    bestMin = Math.min(bestMin, minTree[right]);
                    bestMax = Math.max(bestMax, maxTree[right]);
                    right--;
                }
                left >>= 1;
                right >>= 1;
            }

            return new Result(bestMin, bestMax);
        }

        /**
         * Result of a segment tree query.
         */
        static class Result {
            int minIndex;
            int maxIndex;

            Result(int minIndex, int maxIndex) {
                this.minIndex = minIndex;
                this.maxIndex = maxIndex;
            }

            boolean hasAny() {
                return minIndex != INF || maxIndex != NEG_INF;
            }
        }
    }
}