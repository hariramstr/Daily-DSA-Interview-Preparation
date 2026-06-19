import java.util.*;

/*
Problem Title: Maximum Score from Choosing Endpoints with Growing Penalties

Problem Description:
You are given an integer array nums of length n. You must remove every element from the array, one at a time.
At each step, you may remove either the leftmost remaining element or the rightmost remaining element.
If this is the t-th removal (1-indexed), removing a value x adds x * t to your score.

However, there is an additional penalty for switching sides. If your previous removal was from the left
and your current removal is from the right, or vice versa, then you pay a fixed penalty p for that step.
No penalty is paid on the first removal because there is no previous side.

Return the maximum total score you can obtain after removing all elements.

The challenge is to choose both the order of removals and when to switch sides. A greedy approach is not sufficient,
because taking a large value early may reduce the multiplier available for other values, and unnecessary side switches
may erase the gain.

Constraints:
- 1 <= n <= 2000
- -10^9 <= nums[i] <= 10^9
- 0 <= p <= 10^9
- The answer fits in a signed 64-bit integer.
*/

public class Solution {

    /**
     * Computes the maximum total score obtainable by repeatedly removing either
     * the leftmost or rightmost remaining element, where the t-th removed value x
     * contributes x * t, and switching removal side costs penalty p.
     *
     * Core dynamic programming idea:
     * Let dpL[l][r] be the best score obtainable after removing everything outside
     * subarray [l..r], with the most recent removal taken from the LEFT side.
     * Let dpR[l][r] be the same, but with the most recent removal taken from the RIGHT side.
     *
     * Equivalently, [l..r] is the remaining interval.
     * The number of already removed elements is:
     *     removed = n - (r - l + 1)
     * Therefore, the next removal happens at time:
     *     t = removed + 1
     *
     * From state [l..r], we can remove:
     * - nums[l] from the left, moving to [l+1..r]
     * - nums[r] from the right, moving to [l..r-1]
     *
     * We process intervals from larger to smaller, because transitions go from a larger
     * remaining interval to a smaller remaining interval.
     *
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     *
     * @param nums the input array
     * @param p the fixed penalty paid whenever the chosen side differs from the previous removal side
     * @return the maximum achievable total score
     */
    public long maximumScore(int[] nums, long p) {
        int n = nums.length;

        // Special case:
        // If there is only one element, we remove it at t = 1 and there is no switching penalty.
        if (n == 1) {
            return nums[0];
        }

        // We use a very negative value to represent "unreachable".
        // We avoid Long.MIN_VALUE directly to stay safe when adding values later.
        final long NEG_INF = Long.MIN_VALUE / 4;

        // dpL[l][r]:
        // Best score after removing all elements outside [l..r],
        // where the LAST removal performed was from the LEFT side.
        long[][] dpL = new long[n][n];

        // dpR[l][r]:
        // Best score after removing all elements outside [l..r],
        // where the LAST removal performed was from the RIGHT side.
        long[][] dpR = new long[n][n];

        // Initialize all states as unreachable.
        for (int i = 0; i < n; i++) {
            Arrays.fill(dpL[i], NEG_INF);
            Arrays.fill(dpR[i], NEG_INF);
        }

        // Base transitions from the full interval [0..n-1]:
        // The first move has no previous side, so no switching penalty can apply.
        //
        // If we remove the leftmost element nums[0] at t = 1,
        // the remaining interval becomes [1..n-1], and the last side is LEFT.
        dpL[1][n - 1] = (long) nums[0];

        // If we remove the rightmost element nums[n-1] at t = 1,
        // the remaining interval becomes [0..n-2], and the last side is RIGHT.
        dpR[0][n - 2] = (long) nums[n - 1];

        // We now process all remaining intervals by decreasing length.
        // Why decreasing?
        // Because from interval [l..r], removing one endpoint leads to a smaller interval.
        // So to propagate values correctly, larger intervals must be processed before smaller ones.
        for (int len = n - 1; len >= 1; len--) {
            for (int l = 0; l + len - 1 < n; l++) {
                int r = l + len - 1;

                // Number of elements already removed before this state.
                int removed = n - len;

                // The next removal will be the (removed + 1)-th removal.
                long t = removed + 1L;

                // ------------------------------------------------------------
                // Transition from a state whose last removal was from LEFT.
                // ------------------------------------------------------------
                if (dpL[l][r] != NEG_INF) {
                    long current = dpL[l][r];

                    // Option 1: remove left endpoint nums[l].
                    // Since previous side was LEFT and current side is also LEFT,
                    // there is NO switching penalty.
                    if (l + 1 <= r) {
                        long candidate = current + (long) nums[l] * t;
                        if (candidate > dpL[l + 1][r]) {
                            dpL[l + 1][r] = candidate;
                        }
                    } else {
                        // This means [l..r] has exactly one element.
                        // Removing it finishes the process.
                        // We handle final answers later through len == 1 states.
                    }

                    // Option 2: remove right endpoint nums[r].
                    // Previous side was LEFT, current side is RIGHT,
                    // so we must pay the switching penalty p.
                    if (l <= r - 1) {
                        long candidate = current + (long) nums[r] * t - p;
                        if (candidate > dpR[l][r - 1]) {
                            dpR[l][r - 1] = candidate;
                        }
                    }
                }

                // ------------------------------------------------------------
                // Transition from a state whose last removal was from RIGHT.
                // ------------------------------------------------------------
                if (dpR[l][r] != NEG_INF) {
                    long current = dpR[l][r];

                    // Option 1: remove left endpoint nums[l].
                    // Previous side was RIGHT, current side is LEFT,
                    // so we pay the switching penalty p.
                    if (l + 1 <= r) {
                        long candidate = current + (long) nums[l] * t - p;
                        if (candidate > dpL[l + 1][r]) {
                            dpL[l + 1][r] = candidate;
                        }
                    }

                    // Option 2: remove right endpoint nums[r].
                    // Previous side was RIGHT and current side is also RIGHT,
                    // so there is NO switching penalty.
                    if (l <= r - 1) {
                        long candidate = current + (long) nums[r] * t;
                        if (candidate > dpR[l][r - 1]) {
                            dpR[l][r - 1] = candidate;
                        }
                    }
                }
            }
        }

        // At this point, the only remaining intervals of length 1 are [i..i].
        // We still need to remove that final single element at time t = n.
        long answer = NEG_INF;

        for (int i = 0; i < n; i++) {
            long t = n;

            // If the previous removal side was LEFT, then:
            // - removing the final element from LEFT causes no penalty
            // - removing it from RIGHT would be the same physical element, but in a length-1 interval
            //   there is no meaningful distinction in terms of endpoint identity.
            // To keep the DP consistent, we can think of removing the only remaining element
            // from either side, and if the side differs from the previous side, penalty applies.
            //
            // Since the single element is both leftmost and rightmost, the optimal choice is:
            // from dpL[i][i], choose max(
            //     remove as LEFT  => + nums[i] * n
            //     remove as RIGHT => + nums[i] * n - p
            // ) which is always the LEFT option because p >= 0.
            //
            // Similarly, from dpR[i][i], the RIGHT option is always at least as good.
            //
            // Therefore:
            if (dpL[i][i] != NEG_INF) {
                answer = Math.max(answer, dpL[i][i] + (long) nums[i] * t);
            }
            if (dpR[i][i] != NEG_INF) {
                answer = Math.max(answer, dpR[i][i] + (long) nums[i] * t);
            }
        }

        return answer;
    }

    /**
     * Convenience overload accepting int penalty.
     *
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     *
     * @param nums the input array
     * @param p the switching penalty
     * @return the maximum achievable total score
     */
    public long maximumScore(int[] nums, int p) {
        return maximumScore(nums, (long) p);
    }

    /**
     * Brute-force verifier for small arrays.
     * This method explores all valid endpoint-removal sequences recursively.
     * It is intended only for testing correctness on tiny inputs.
     *
     * Time complexity: O(2^n)
     * Space complexity: O(n) recursion depth
     *
     * @param nums the input array
     * @param p the switching penalty
     * @return the exact maximum score by exhaustive search
     */
    public long maximumScoreBruteForce(int[] nums, long p) {
        return dfsBrute(nums, 0, nums.length - 1, 1, 0, false, p);
    }

    /**
     * Recursive helper for brute-force verification.
     *
     * lastSide encoding:
     * - 0 means previous removal was from LEFT
     * - 1 means previous removal was from RIGHT
     *
     * Time complexity: O(2^n)
     * Space complexity: O(n)
     *
     * @param nums the input array
     * @param l current left index
     * @param r current right index
     * @param t current removal number (1-indexed)
     * @param lastSide previous side used
     * @param hasLast whether a previous side exists yet
     * @param p switching penalty
     * @return best score from this state onward
     */
    public long dfsBrute(int[] nums, int l, int r, int t, int lastSide, boolean hasLast, long p) {
        if (l > r) {
            return 0L;
        }

        long penaltyLeft = (hasLast && lastSide != 0) ? p : 0L;
        long takeLeft = (long) nums[l] * t - penaltyLeft
                + dfsBrute(nums, l + 1, r, t + 1, 0, true, p);

        long penaltyRight = (hasLast && lastSide != 1) ? p : 0L;
        long takeRight = (long) nums[r] * t - penaltyRight
                + dfsBrute(nums, l, r - 1, t + 1, 1, true, p);

        return Math.max(takeLeft, takeRight);
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * Time complexity: dominated by the called methods
     * Space complexity: dominated by the called methods
     *
     * @param args command-line arguments, unused
     * @return nothing
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int[] nums1 = {4, 2, 9};
        long p1 = 3;
        long ans1 = sol.maximumScore(nums1, p1);
        System.out.println("nums = " + Arrays.toString(nums1) + ", p = " + p1);
        System.out.println("Maximum score = " + ans1);
        System.out.println("Expected for sample 1 = 35");
        System.out.println();

        int[] nums2 = {8, -5, 7, 3};
        long p2 = 4;
        long ans2 = sol.maximumScore(nums2, p2);
        System.out.println("nums = " + Arrays.toString(nums2) + ", p = " + p2);
        System.out.println("Maximum score = " + ans2);
        System.out.println();

        // Small brute-force cross-checks for confidence.
        // These are especially useful because the second sample statement's claimed
        // output is inconsistent with direct exhaustive evaluation.
        long brute1 = sol.maximumScoreBruteForce(nums1, p1);
        long brute2 = sol.maximumScoreBruteForce(nums2, p2);

        System.out.println("Brute-force check sample 1 = " + brute1);
        System.out.println("DP check sample 1          = " + ans1);
        System.out.println();

        System.out.println("Brute-force check sample 2 = " + brute2);
        System.out.println("DP check sample 2          = " + ans2);
        System.out.println();

        int[] nums3 = {1};
        long p3 = 100;
        System.out.println("nums = " + Arrays.toString(nums3) + ", p = " + p3);
        System.out.println("Maximum score = " + sol.maximumScore(nums3, p3));
        System.out.println();

        int[] nums4 = {-1, -2, -3};
        long p4 = 5;
        System.out.println("nums = " + Arrays.toString(nums4) + ", p = " + p4);
        System.out.println("Maximum score = " + sol.maximumScore(nums4, p4));
    }
}