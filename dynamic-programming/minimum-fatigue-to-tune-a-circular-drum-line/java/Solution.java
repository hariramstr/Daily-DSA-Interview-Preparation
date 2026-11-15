import java.util.*;

/*
 * Minimum Fatigue to Tune a Circular Drum Line
 *
 * Problem Description:
 * A festival stage has n drums arranged in a circle. Drum i currently has pitch current[i],
 * and it must be retuned to target[i]. You may tune the drums in any order, but because
 * the drums are mounted on a shared circular frame, tuning one drum affects the effort
 * needed to tune its neighbors.
 *
 * If you tune a contiguous block of drums from index l to index r in clockwise order as one phase,
 * then during that phase you may choose exactly one drum k in that block to tune last.
 * All other drums in the block must be completely tuned before drum k is finalized.
 * The fatigue cost contributed by drum k when it is finalized is:
 *
 *     abs(current[k] - target[k]) + leftPenalty + rightPenalty
 *
 * where leftPenalty is 0 if there is no untuned drum remaining on the clockwise-left side
 * inside the current block, otherwise it is abs(target[k] - target[left]), where left is the
 * nearest drum in the block still not finalized before k; similarly, rightPenalty is defined
 * using the nearest untuned drum on the clockwise-right side inside the current block.
 *
 * Your task is to compute the minimum total fatigue required to finalize all drums.
 *
 * Because the drums are arranged in a circle, drum 0 and drum n - 1 are adjacent.
 * You may break the circle at any position before planning phases, but the resulting total
 * fatigue must respect the original circular adjacency.
 *
 * Constraints:
 * - 1 <= n <= 300
 * - 0 <= current[i], target[i] <= 1e9
 *
 * Key observation:
 * The unavoidable part is:
 *
 *     sum(abs(current[i] - target[i]))
 *
 * The optimization only concerns the extra structural penalties caused by the order in which
 * drums are finalized.
 *
 * Interval DP idea for a line:
 * Let dp[l][r] be the minimum extra penalty needed to completely finalize all drums in the
 * contiguous interval [l, r], assuming drums outside the interval do not exist.
 *
 * If the last finalized drum in [l, r] is k, then before finalizing k, the interval [l, r]
 * has been split into:
 *   - left subinterval [l, k - 1]
 *   - right subinterval [k + 1, r]
 *
 * At that final moment:
 *   - if k > l, the nearest remaining drum on the left boundary side is l, so we pay
 *     abs(target[k] - target[l])
 *   - if k < r, the nearest remaining drum on the right boundary side is r, so we pay
 *     abs(target[k] - target[r])
 *
 * Therefore:
 *
 *   dp[l][r] = min over k in [l, r] of
 *              dp[l][k - 1] + dp[k + 1][r]
 *              + (k > l ? abs(target[k] - target[l]) : 0)
 *              + (k < r ? abs(target[k] - target[r]) : 0)
 *
 * For the circular case:
 * We duplicate the target array to length 2n. Any break point corresponds to taking one
 * length-n window [s, s + n - 1] in the duplicated array and solving the linear interval DP
 * on that window. The answer is the minimum over all such windows.
 *
 * Since n <= 300, an O(n^4) implementation is acceptable:
 *   - O(n^3) to solve one length-n linear interval DP
 *   - repeated for n possible break points gives O(n^4)
 *
 * This is simple, clear, and safe for the given constraints.
 */
public class Solution {

    /**
     * Computes the minimum total fatigue for the circular drum line.
     *
     * The method first adds the unavoidable base retuning cost:
     *     sum(abs(current[i] - target[i]))
     *
     * Then it computes the minimum additional structural penalty caused by the order of
     * finalization on the circle. To handle the circle, we try every possible break point,
     * convert the circle into a line, solve the interval DP for that line, and take the minimum.
     *
     * @param current the current pitches of the drums
     * @param target the desired target pitches of the drums
     * @return the minimum possible total fatigue
     *
     * Time complexity: O(n^4), where n is the number of drums
     * Space complexity: O(n^2) for each linear DP solve, plus O(n) duplicated arrays
     */
    public long minimumFatigue(int[] current, int[] target) {
        int n = current.length;

        // Step 1:
        // Compute the unavoidable base cost.
        // Every drum must eventually be retuned from current[i] to target[i],
        // so this part is paid no matter what order we choose.
        long baseCost = 0L;
        for (int i = 0; i < n; i++) {
            baseCost += Math.abs((long) current[i] - target[i]);
        }

        // Special case:
        // If there is only one drum, there are no neighbors and therefore no extra penalty.
        if (n == 1) {
            return baseCost;
        }

        // Step 2:
        // Duplicate the target array so that every circular segment of length n
        // becomes a normal contiguous subarray in the duplicated array.
        int[] doubled = new int[2 * n];
        for (int i = 0; i < 2 * n; i++) {
            doubled[i] = target[i % n];
        }

        // Step 3:
        // Try every possible break point.
        // A break point "start" means we linearize the circle as:
        // target[start], target[start+1], ..., target[start+n-1]
        long bestExtra = Long.MAX_VALUE;
        for (int start = 0; start < n; start++) {
            long extra = solveLinearIntervalDP(doubled, start, start + n - 1);
            bestExtra = Math.min(bestExtra, extra);
        }

        return baseCost + bestExtra;
    }

    /**
     * Solves the interval DP for a linear segment arr[leftBound..rightBound].
     *
     * Definition:
     *   dp[l][r] = minimum extra structural penalty to finalize all drums in interval [l, r]
     *              when treated as a standalone line.
     *
     * Transition:
     *   Choose k as the last drum finalized in [l, r].
     *   Then [l, k-1] and [k+1, r] must already be fully finalized.
     *   At the moment k is finalized:
     *     - if there is a left side inside the interval, the nearest remaining left boundary
     *       drum is l, so we pay abs(arr[k] - arr[l])
     *     - if there is a right side inside the interval, the nearest remaining right boundary
     *       drum is r, so we pay abs(arr[k] - arr[r])
     *
     * Therefore:
     *   dp[l][r] = min over k in [l, r] of
     *              dp[l][k-1] + dp[k+1][r]
     *              + (k > l ? abs(arr[k] - arr[l]) : 0)
     *              + (k < r ? abs(arr[k] - arr[r]) : 0)
     *
     * Base case:
     *   dp[i][i] = 0
     * because a single drum has no remaining neighbors inside the interval when it is finalized.
     *
     * @param arr the duplicated target array
     * @param leftBound the left index of the chosen linear window
     * @param rightBound the right index of the chosen linear window
     * @return the minimum extra structural penalty for this linearized arrangement
     *
     * Time complexity: O(m^3), where m = rightBound - leftBound + 1
     * Space complexity: O(m^2)
     */
    public long solveLinearIntervalDP(int[] arr, int leftBound, int rightBound) {
        int m = rightBound - leftBound + 1;

        // We build a local array "a" of length m for simpler indexing from 0 to m-1.
        int[] a = new int[m];
        for (int i = 0; i < m; i++) {
            a[i] = arr[leftBound + i];
        }

        // dp[l][r] stores the minimum extra penalty for interval [l, r] in this local array.
        long[][] dp = new long[m][m];

        // Base case:
        // dp[i][i] = 0 by default in Java for long arrays.
        // A single drum has no internal left/right neighbors remaining.

        // We process intervals by increasing length.
        // This guarantees that when computing dp[l][r], all smaller subintervals
        // such as dp[l][k-1] and dp[k+1][r] are already known.
        for (int len = 2; len <= m; len++) {
            for (int l = 0; l + len - 1 < m; l++) {
                int r = l + len - 1;

                long best = Long.MAX_VALUE;

                // Try every possible last finalized drum k in [l, r].
                for (int k = l; k <= r; k++) {
                    long candidate = 0L;

                    // Cost of fully resolving the left subinterval [l, k-1].
                    if (k > l) {
                        candidate += dp[l][k - 1];
                    }

                    // Cost of fully resolving the right subinterval [k+1, r].
                    if (k < r) {
                        candidate += dp[k + 1][r];
                    }

                    // Finalizing k now sees the left boundary l if there is any left side.
                    if (k > l) {
                        candidate += Math.abs((long) a[k] - a[l]);
                    }

                    // Finalizing k now sees the right boundary r if there is any right side.
                    if (k < r) {
                        candidate += Math.abs((long) a[k] - a[r]);
                    }

                    best = Math.min(best, candidate);
                }

                dp[l][r] = best;
            }
        }

        return dp[0][m - 1];
    }

    /**
     * Convenience wrapper matching a common interview-style method name.
     *
     * @param current the current pitches
     * @param target the target pitches
     * @return the minimum total fatigue
     *
     * Time complexity: O(n^4)
     * Space complexity: O(n^2)
     */
    public long minFatigue(int[] current, int[] target) {
        return minimumFatigue(current, target);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) for the demonstration itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] current1 = {5, 1, 4};
        int[] target1 = {3, 2, 6};
        long answer1 = solution.minimumFatigue(current1, target1);
        System.out.println(answer1); // Expected: 7

        int[] current2 = {8, 8, 8, 8};
        int[] target2 = {1, 10, 1, 10};
        long answer2 = solution.minimumFatigue(current2, target2);
        System.out.println(answer2); // Expected: 23
    }
}