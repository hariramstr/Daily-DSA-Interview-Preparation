import java.util.*;

/*
 * Title: Maximum Signal Score from Choosing K Relay Towers
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array heights where heights[i] is the elevation of the i-th relay tower
 * along a straight highway. You must choose exactly k towers, keeping their original left-to-right order.
 * If the chosen tower indices are i1 < i2 < ... < ik, then the total signal score is defined as the sum
 * of the minimum height of every adjacent chosen pair:
 *
 * score = min(heights[i1], heights[i2]) + min(heights[i2], heights[i3]) + ... + min(heights[i(k-1)], heights[ik]).
 *
 * Your task is to return the maximum possible signal score.
 *
 * This is not the same as choosing a contiguous subarray: you may skip any number of towers between two chosen towers,
 * but the relative order must remain unchanged. Because each pair contributes the smaller of the two heights,
 * a locally tall tower may still be a poor choice if it forces weak pairings elsewhere. The challenge is to optimize
 * globally for exactly k selections.
 *
 * Return the maximum score as a 64-bit integer.
 *
 * Constraints:
 * - 2 <= heights.length <= 200000
 * - 1 <= heights[i] <= 1000000000
 * - 2 <= k <= min(heights.length, 200)
 * - The answer may exceed 32-bit signed integer range
 *
 * Example 1:
 * Input: heights = [5, 1, 4, 6, 3], k = 3
 * Output: 8
 * Explanation: Choose towers at indices [0, 2, 3] with heights [5, 4, 6].
 * The score is min(5,4) + min(4,6) = 4 + 4 = 8.
 *
 * Example 2:
 * Input: heights = [2, 7, 3, 9, 5, 8], k = 4
 * Output: 17
 * Explanation: Choose towers at indices [1, 3, 4, 5] with heights [7, 9, 5, 8].
 * The score is min(7,9) + min(9,5) + min(5,8) = 7 + 5 + 5 = 17.
 */

public class Solution {

    /**
     * A very negative value used as "negative infinity" for impossible DP states.
     * We keep it far from overflow when adding heights.
     */
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    /**
     * Computes the maximum possible signal score when choosing exactly k towers in order.
     *
     * Core idea:
     * For a chosen sequence of heights a1, a2, ..., ak, the score is:
     * min(a1, a2) + min(a2, a3) + ... + min(a(k-1), ak)
     *
     * We process towers from left to right and build dynamic programming states for sequences
     * ending at the current tower.
     *
     * Let dp[t][i] be the best score for choosing exactly t towers, with the t-th chosen tower at index i.
     * Then:
     * dp[1][i] = 0
     * dp[t][i] = max over j < i of (dp[t-1][j] + min(heights[j], heights[i]))
     *
     * The direct O(n^2 * k) transition is too slow.
     *
     * We optimize the transition:
     * For fixed t and current i with x = heights[i],
     *
     * max_j (prev[j] + min(heights[j], x))
     *
     * Split previous towers j into two groups:
     * 1) heights[j] <= x  => contribution = prev[j] + heights[j]
     * 2) heights[j] >  x  => contribution = prev[j] + x
     *
     * So the transition becomes:
     * max(
     *     max over j with heights[j] <= x of (prev[j] + heights[j]),
     *     x + max over j with heights[j] > x of prev[j]
     * )
     *
     * We coordinate-compress heights and use:
     * - a prefix maximum structure for values (prev[j] + heights[j])
     * - a suffix maximum structure for values prev[j]
     *
     * Both are maintained with segment trees while scanning left to right.
     *
     * @param heights the elevations of the relay towers
     * @param k the exact number of towers to choose
     * @return the maximum possible signal score as a 64-bit integer
     * Time complexity: O(k * n * log n)
     * Space complexity: O(n + m), where m is the number of distinct heights
     */
    public long maximumSignalScore(int[] heights, int k) {
        int n = heights.length;

        // Coordinate compression of heights:
        // We only need relative ordering of heights for <= and > queries.
        int[] sortedUnique = compressValues(heights);
        int m = sortedUnique.length;

        // rank[i] = compressed position of heights[i], in range [0, m-1]
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[i] = lowerBound(sortedUnique, heights[i]);
        }

        // prev[i] represents dp[t-1][i] for the current layer.
        // For t = 1, choosing exactly one tower ending at any i gives score 0.
        long[] prev = new long[n];
        Arrays.fill(prev, 0L);

        // We build layers from t = 2 up to k.
        for (int chosen = 2; chosen <= k; chosen++) {
            long[] curr = new long[n];
            Arrays.fill(curr, NEG_INF);

            // Segment tree 1:
            // At compressed height position r, we store the maximum of (prev[j] + heights[j])
            // among processed indices j with that exact height rank.
            // Query on [0 .. rank[i]] gives best previous j with heights[j] <= heights[i].
            SegmentTree prefixBest = new SegmentTree(m);

            // Segment tree 2:
            // At compressed height position r, we store the maximum of prev[j]
            // among processed indices j with that exact height rank.
            // Query on [rank[i] + 1 .. m-1] gives best previous j with heights[j] > heights[i].
            SegmentTree suffixBest = new SegmentTree(m);

            // We scan i from left to right.
            // Before processing i, the trees contain only indices j < i, which is exactly what we need.
            for (int i = 0; i < n; i++) {
                int r = rank[i];
                long best = NEG_INF;

                // Case 1: previous chosen tower j has height <= current height.
                // Then added pair contribution is heights[j].
                long candidateFromSmallerOrEqual = prefixBest.query(0, r);
                if (candidateFromSmallerOrEqual != NEG_INF) {
                    best = Math.max(best, candidateFromSmallerOrEqual);
                }

                // Case 2: previous chosen tower j has height > current height.
                // Then added pair contribution is current height.
                if (r + 1 <= m - 1) {
                    long bestPrevAmongGreaterHeights = suffixBest.query(r + 1, m - 1);
                    if (bestPrevAmongGreaterHeights != NEG_INF) {
                        best = Math.max(best, bestPrevAmongGreaterHeights + heights[i]);
                    }
                }

                curr[i] = best;

                // Now insert index i into the structures for future positions.
                // But only if prev[i] is a valid state:
                // it means we can choose exactly (chosen - 1) towers ending at i.
                if (prev[i] != NEG_INF) {
                    prefixBest.update(r, prev[i] + heights[i]);
                    suffixBest.update(r, prev[i]);
                }
            }

            prev = curr;
        }

        // The answer is the best score among all sequences of exactly k towers ending anywhere.
        long answer = 0L;
        for (long value : prev) {
            answer = Math.max(answer, value);
        }
        return answer;
    }

    /**
     * Creates a sorted array of distinct values from the input array.
     * This is used for coordinate compression.
     *
     * @param values the original values
     * @return sorted distinct values
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int[] compressValues(int[] values) {
        int[] copy = values.clone();
        Arrays.sort(copy);

        int uniqueCount = 0;
        for (int i = 0; i < copy.length; i++) {
            if (i == 0 || copy[i] != copy[i - 1]) {
                copy[uniqueCount++] = copy[i];
            }
        }
        return Arrays.copyOf(copy, uniqueCount);
    }

    /**
     * Returns the first index at which target could be inserted in sorted array
     * without violating order. Since target is guaranteed to exist in the compressed
     * array for this problem, this effectively returns its exact rank.
     *
     * @param arr sorted array
     * @param target value to search
     * @return lower bound index
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] arr, int target) {
        int left = 0;
        int right = arr.length;
        while (left < right) {
            int mid = left + ((right - left) >>> 1);
            if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called methods
     * Space complexity: O(1) for the demonstration itself, excluding called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] heights1 = {5, 1, 4, 6, 3};
        int k1 = 3;
        long result1 = solution.maximumSignalScore(heights1, k1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 8");

        int[] heights2 = {2, 7, 3, 9, 5, 8};
        int k2 = 4;
        long result2 = solution.maximumSignalScore(heights2, k2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 17");
    }

    /**
     * A segment tree supporting:
     * - point update with max
     * - range maximum query
     *
     * We use it because we need fast maximum queries over compressed height ranges.
     */
    static class SegmentTree {
        private final int size;
        private final long[] tree;

        /**
         * Constructs a segment tree for a given number of positions.
         *
         * @param n number of positions
         */
        SegmentTree(int n) {
            int s = 1;
            while (s < n) {
                s <<= 1;
            }
            this.size = s;
            this.tree = new long[size << 1];
            Arrays.fill(this.tree, NEG_INF);
        }

        /**
         * Updates one position with the maximum of its current value and the given value.
         *
         * @param index the position to update
         * @param value the candidate value
         * @return nothing
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        public void update(int index, long value) {
            int pos = index + size;
            tree[pos] = Math.max(tree[pos], value);
            pos >>= 1;

            while (pos > 0) {
                tree[pos] = Math.max(tree[pos << 1], tree[(pos << 1) | 1]);
                pos >>= 1;
            }
        }

        /**
         * Returns the maximum value in the inclusive range [left, right].
         *
         * @param left left boundary, inclusive
         * @param right right boundary, inclusive
         * @return maximum value in the range, or NEG_INF if the range is empty / has no valid values
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        public long query(int left, int right) {
            if (left > right) {
                return NEG_INF;
            }

            long result = NEG_INF;
            int l = left + size;
            int r = right + size;

            while (l <= r) {
                if ((l & 1) == 1) {
                    result = Math.max(result, tree[l]);
                    l++;
                }
                if ((r & 1) == 0) {
                    result = Math.max(result, tree[r]);
                    r--;
                }
                l >>= 1;
                r >>= 1;
            }

            return result;
        }
    }
}