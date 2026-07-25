import java.util.*;

/*
 * Title: Maximum Sum of Non-Overlapping Value Bands
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums and an integer k. A value band is a contiguous subarray
 * nums[l..r] such that the difference between the maximum and minimum value inside that subarray
 * is at most k. You may choose any number of value bands, but no two chosen bands may overlap.
 * The score of a chosen band is the sum of its elements. Your task is to return the maximum total
 * score obtainable by selecting a set of non-overlapping value bands.
 *
 * A band of length 1 is always valid. You are allowed to skip elements entirely if doing so
 * increases the total score. Note that even if a subarray satisfies the value-band condition,
 * it may be better not to take it if its sum is negative or if taking smaller bands leads to a
 * larger total.
 *
 * Design an algorithm that works efficiently for large inputs.
 *
 * Constraints:
 * - 1 <= nums.length <= 2 * 10^5
 * - -10^9 <= nums[i] <= 10^9
 * - 0 <= k <= 10^9
 * - The answer fits in a signed 64-bit integer.
 *
 * Example 1:
 * Input: nums = [4, 2, 3, 7, 6, 5], k = 2
 * Output: 27
 * Explanation: The entire array is not valid because max - min = 7 - 2 = 5.
 * One optimal choice is [4, 2, 3] with sum 9 and [7, 6, 5] with sum 18.
 * These bands do not overlap, and the total is 27.
 *
 * Example 2:
 * Input: nums = [5, -4, 6, 6, -2, 7], k = 1
 * Output: 24
 * Explanation:
 * Valid high-scoring bands include [6, 6] with sum 12 and [7] with sum 7.
 * Also, [5] is a valid band of length 1 with sum 5.
 * Taking [5] + [6, 6] + [7] gives 24, and these bands are non-overlapping.
 * Therefore the optimal answer is 24.
 */

public class Solution {

    /**
     * Computes the maximum total score obtainable by selecting non-overlapping valid value bands.
     *
     * Core idea:
     * 1. For every ending index r, find the smallest left boundary left[r] such that every subarray
     *    [l..r] with l >= left[r] is valid (max - min <= k).
     * 2. Let prefix sums be used so subarray sums can be computed quickly:
     *       sum(l..r) = prefix[r + 1] - prefix[l]
     * 3. Dynamic programming:
     *       dp[i] = maximum score using the first i elements (indices 0..i-1)
     *    Then for position r = i - 1:
     *       dp[i] = max(
     *           dp[i - 1],                                  // skip nums[r]
     *           max over l in [left[r], r] of dp[l] + sum(l..r)
     *       )
     *    Rewrite:
     *       dp[l] + sum(l..r) = dp[l] + prefix[r + 1] - prefix[l]
     *                         = prefix[r + 1] + (dp[l] - prefix[l])
     *    So for each r we need the maximum value of (dp[l] - prefix[l]) over l in [left[r], r].
     * 4. Maintain that range maximum with a segment tree over indices l.
     *
     * This yields an efficient O(n log n) solution.
     *
     * @param nums the input array
     * @param k the maximum allowed difference between max and min inside a chosen band
     * @return the maximum total score as a 64-bit signed integer
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumSumOfNonOverlappingValueBands(int[] nums, int k) {
        int n = nums.length;

        // Step 1: Build prefix sums.
        // prefix[i] = sum of nums[0..i-1]
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }

        // Step 2: For each right endpoint r, compute the smallest valid left endpoint left[r].
        int[] left = computeSmallestValidLeft(nums, k);

        // Step 3: Dynamic programming array.
        // dp[i] = best answer using first i elements.
        long[] dp = new long[n + 1];

        // Step 4: Segment tree stores values:
        // valueAtIndex(l) = dp[l] - prefix[l]
        // We need range maximum queries on l in [left[r], r].
        SegmentTree segTree = new SegmentTree(n + 1);

        // Initially, dp[0] = 0, prefix[0] = 0, so value at index 0 is 0.
        segTree.update(0, dp[0] - prefix[0]);

        // Process each position from left to right.
        for (int r = 0; r < n; r++) {
            // Option 1: skip nums[r], so answer remains dp[r].
            long best = dp[r];

            // Option 2: choose a valid band ending at r.
            // Any l in [left[r], r] gives a valid subarray [l..r].
            long bestBase = segTree.query(left[r], r);
            long take = prefix[r + 1] + bestBase;

            best = Math.max(best, take);

            // Store result for first r+1 elements.
            dp[r + 1] = best;

            // Make index r+1 available for future transitions.
            // This corresponds to choosing future bands that start at l = r+1.
            segTree.update(r + 1, dp[r + 1] - prefix[r + 1]);
        }

        return dp[n];
    }

    /**
     * Computes, for each right endpoint r, the smallest left endpoint left[r] such that
     * every subarray [l..r] with l >= left[r] is valid under max - min <= k.
     *
     * We use a standard sliding window with two monotonic deques:
     * - one deque keeps values in decreasing order to get the current maximum
     * - one deque keeps values in increasing order to get the current minimum
     *
     * As we expand the right endpoint, we shrink the left endpoint until the window becomes valid.
     *
     * @param nums the input array
     * @param k the maximum allowed difference between max and min
     * @return an array left where left[r] is the smallest valid left boundary for endpoint r
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] computeSmallestValidLeft(int[] nums, int k) {
        int n = nums.length;
        int[] left = new int[n];

        Deque<Integer> maxDeque = new ArrayDeque<>();
        Deque<Integer> minDeque = new ArrayDeque<>();

        int l = 0;

        for (int r = 0; r < n; r++) {
            // Insert nums[r] into maxDeque:
            // remove smaller elements from the back because they can never become the maximum
            // while nums[r] remains in the window.
            while (!maxDeque.isEmpty() && nums[maxDeque.peekLast()] <= nums[r]) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(r);

            // Insert nums[r] into minDeque:
            // remove larger elements from the back because they can never become the minimum
            // while nums[r] remains in the window.
            while (!minDeque.isEmpty() && nums[minDeque.peekLast()] >= nums[r]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(r);

            // Shrink left boundary until the window satisfies max - min <= k.
            while ((long) nums[maxDeque.peekFirst()] - (long) nums[minDeque.peekFirst()] > k) {
                if (maxDeque.peekFirst() == l) {
                    maxDeque.pollFirst();
                }
                if (minDeque.peekFirst() == l) {
                    minDeque.pollFirst();
                }
                l++;
            }

            // Now [l..r] is valid, and it is the leftmost valid window ending at r.
            left[r] = l;
        }

        return left;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(m * n log n) across demonstrated test cases, where m is number of tests
     * Space complexity: O(n) per test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {4, 2, 3, 7, 6, 5};
        int k1 = 2;
        long ans1 = solution.maximumSumOfNonOverlappingValueBands(nums1, k1);
        System.out.println(ans1); // Expected: 27

        int[] nums2 = {5, -4, 6, 6, -2, 7};
        int k2 = 1;
        long ans2 = solution.maximumSumOfNonOverlappingValueBands(nums2, k2);
        System.out.println(ans2); // Expected: 24

        int[] nums3 = {-5, -1, -3};
        int k3 = 10;
        long ans3 = solution.maximumSumOfNonOverlappingValueBands(nums3, k3);
        System.out.println(ans3); // Expected: 0 (best is to skip everything)

        int[] nums4 = {8};
        int k4 = 0;
        long ans4 = solution.maximumSumOfNonOverlappingValueBands(nums4, k4);
        System.out.println(ans4); // Expected: 8
    }

    /**
     * A segment tree for range maximum query and point update on long values.
     *
     * This is used to maintain:
     *   value[l] = dp[l] - prefix[l]
     * so that for each right endpoint r we can query:
     *   max(value[l]) for l in [left[r], r]
     *
     * Time complexity per update/query: O(log n)
     * Space complexity: O(n)
     */
    static class SegmentTree {
        private final int size;
        private final long[] tree;
        private static final long NEG_INF = Long.MIN_VALUE / 4;

        /**
         * Creates a segment tree capable of storing values for indices [0, n-1].
         *
         * @param n number of positions
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
            this.tree = new long[size << 1];
            Arrays.fill(tree, NEG_INF);
        }

        /**
         * Sets the value at a single index.
         *
         * @param index the position to update
         * @param value the new value
         * @return nothing
         * Time complexity: O(log n)
         * Space complexity: O(1) auxiliary
         */
        public void update(int index, long value) {
            int pos = index + size;
            tree[pos] = value;
            pos >>= 1;

            while (pos > 0) {
                tree[pos] = Math.max(tree[pos << 1], tree[(pos << 1) | 1]);
                pos >>= 1;
            }
        }

        /**
         * Returns the maximum value in the inclusive range [left, right].
         *
         * @param left the left boundary of the query
         * @param right the right boundary of the query
         * @return the maximum value in the range
         * Time complexity: O(log n)
         * Space complexity: O(1) auxiliary
         */
        public long query(int left, int right) {
            long res = NEG_INF;
            int l = left + size;
            int r = right + size;

            while (l <= r) {
                if ((l & 1) == 1) {
                    res = Math.max(res, tree[l]);
                    l++;
                }
                if ((r & 1) == 0) {
                    res = Math.max(res, tree[r]);
                    r--;
                }
                l >>= 1;
                r >>= 1;
            }

            return res;
        }
    }
}