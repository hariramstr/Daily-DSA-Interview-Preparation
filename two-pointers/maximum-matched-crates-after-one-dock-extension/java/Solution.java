import java.util.*;

/*
 * Maximum Matched Crates After One Dock Extension
 * Difficulty: Hard
 * Topic: Two Pointers
 *
 * Problem Description:
 * A warehouse has two sorted arrays: crates and docks. crates[i] is the size requirement
 * of the i-th outgoing crate, and docks[j] is the capacity of the j-th loading dock.
 * A crate can be assigned to at most one dock, and a dock can load at most one crate.
 * A crate can only use a dock whose capacity is at least the crate's size.
 *
 * Before assignments are made, the warehouse may perform at most one temporary dock extension.
 * This extension can be applied to exactly one dock, increasing its capacity by an integer value
 * boost, where 0 <= boost <= extraCapacity. The extension is used on only one dock and only for
 * this assignment batch.
 *
 * Return the maximum number of crates that can be matched after optimally choosing whether to use
 * the extension, which dock to apply it to, and how to pair crates with docks.
 *
 * Both arrays may contain duplicates, and the chosen dock does not need to remain in its original
 * relative position after being conceptually boosted; only the final matching count matters.
 * The solution must be efficient enough for large inputs.
 *
 * Constraints:
 * - 1 <= crates.length, docks.length <= 2 * 10^5
 * - 1 <= crates[i], docks[j] <= 10^9
 * - 0 <= extraCapacity <= 10^9
 * - crates is sorted in nondecreasing order
 * - docks is sorted in nondecreasing order
 *
 * Example 1:
 * Input: crates = [2, 4, 7, 9], docks = [3, 5, 8], extraCapacity = 2
 * Output: 3
 *
 * Example 2:
 * Input: crates = [3, 6, 6, 10], docks = [2, 6, 8, 8], extraCapacity = 3
 * Output: 4
 */

public class Solution {

    /**
     * Computes the maximum number of crate-dock matches after optionally extending
     * exactly one dock by at most extraCapacity.
     *
     * Core idea:
     * 1. We binary search the answer k = number of matches we want to achieve.
     * 2. For a fixed k, we check whether it is possible to match k crates using all docks,
     *    with at most one dock receiving the temporary extension.
     * 3. Because arrays are sorted and we only care about count, the best k crates to try
     *    to match are the k smallest crates among the largest possible prefix relevant to k.
     *    More precisely, for feasibility of matching k crates from sorted crates array of size n
     *    and docks array of size m, it is enough to consider the k smallest crates among the
     *    last k candidates that matter, which becomes crates[0..k-1] when we only ask whether
     *    some k crates can be matched. Since choosing smaller crates is always easier, if any
     *    k crates can be matched, then the k smallest crates can also be matched.
     * 4. The feasibility check is done greedily with two states:
     *    - dp0: maximum number of crates matched so far without having used the extension
     *    - dp1: maximum number of crates matched so far after having used the extension
     *    We process docks from left to right and update these states.
     *
     * Why the DP works:
     * - Since crates are sorted, if we have already matched x crates, then the next crate
     *   we would try to match is crates[x].
     * - For each dock, we may:
     *   a) skip it
     *   b) use it normally to match the next unmatched crate if capacity is enough
     *   c) if extension not used yet, extend this dock and match the next unmatched crate
     *      if dock + extraCapacity is enough
     * - Because each transition only depends on how many smallest crates have been matched,
     *   the DP remains one-dimensional in the matched count.
     *
     * @param crates sorted array of crate size requirements
     * @param docks sorted array of dock capacities
     * @param extraCapacity maximum extra capacity that can be added to one dock
     * @return maximum number of matched crate-dock pairs
     * Time complexity: O((n + m) * log(min(n, m)))
     * Space complexity: O(1)
     */
    public int maximumMatchedCrates(int[] crates, int[] docks, int extraCapacity) {
        int n = crates.length;
        int m = docks.length;
        int low = 0;
        int high = Math.min(n, m);

        // Standard binary search on the answer.
        // We search for the largest k such that "canMatchK(k)" is true.
        while (low < high) {
            int mid = low + (high - low + 1) / 2;
            if (canMatchK(crates, docks, extraCapacity, mid)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return low;
    }

    /**
     * Checks whether it is possible to match exactly k crates using the available docks,
     * when at most one dock may be extended by at most extraCapacity.
     *
     * Detailed DP interpretation:
     * - dp0 = the maximum number of the first k crates that can be matched after processing
     *         some prefix of docks, without using the extension yet.
     * - dp1 = the maximum number of the first k crates that can be matched after processing
     *         some prefix of docks, after already using the extension.
     *
     * Initially:
     * - dp0 = 0 because with no docks processed and no extension used, we matched 0 crates.
     * - dp1 = -1 meaning impossible state before any dock is processed.
     *
     * For each dock capacity d:
     * 1. We may skip the dock, so current states remain possible.
     * 2. From dp0:
     *    - If d >= crates[dp0], we can match the next crate normally and move to dp0 + 1.
     *    - If d + extraCapacity >= crates[dp0], we can use the extension here and move to dp1 = dp0 + 1.
     * 3. From dp1:
     *    - If d >= crates[dp1], we can match the next crate normally and stay in used-extension state.
     *
     * Since each dock can only be used once, we must compute next states from previous states.
     *
     * @param crates sorted array of crate size requirements
     * @param docks sorted array of dock capacities
     * @param extraCapacity maximum extra capacity that can be added to one dock
     * @param k target number of matches to test
     * @return true if k matches are achievable, otherwise false
     * Time complexity: O(docks.length)
     * Space complexity: O(1)
     */
    public boolean canMatchK(int[] crates, int[] docks, int extraCapacity, int k) {
        // Matching 0 crates is always possible.
        if (k == 0) {
            return true;
        }

        // If there are fewer than k docks, impossible immediately.
        if (docks.length < k || crates.length < k) {
            return false;
        }

        // dp0 = matched count without using extension
        // dp1 = matched count after using extension
        int dp0 = 0;
        int dp1 = -1;

        // We only need to match the first k smallest crates.
        // If these can be matched, then certainly some k crates can be matched.
        for (int dock : docks) {
            int next0 = dp0;
            int next1 = dp1;

            // Transition from state "extension not used yet".
            if (dp0 < k) {
                // Try matching the next required crate normally with this dock.
                if (dock >= crates[dp0]) {
                    next0 = Math.max(next0, dp0 + 1);
                }

                // Try using the one-time extension on this dock to match the next crate.
                long boosted = (long) dock + extraCapacity;
                if (boosted >= crates[dp0]) {
                    next1 = Math.max(next1, dp0 + 1);
                }
            }

            // Transition from state "extension already used".
            if (dp1 >= 0 && dp1 < k) {
                // We can only match normally now, because the extension is already spent.
                if (dock >= crates[dp1]) {
                    next1 = Math.max(next1, dp1 + 1);
                }
            }

            dp0 = next0;
            dp1 = next1;

            // Early exit if either state already reaches k.
            if (dp0 >= k || dp1 >= k) {
                return true;
            }
        }

        return dp0 >= k || dp1 >= k;
    }

    /**
     * Runs a demonstration using the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O((n + m) * log(min(n, m))) across the shown examples
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] crates1 = {2, 4, 7, 9};
        int[] docks1 = {3, 5, 8};
        int extraCapacity1 = 2;
        int result1 = solution.maximumMatchedCrates(crates1, docks1, extraCapacity1);
        System.out.println(result1); // Expected: 3

        int[] crates2 = {3, 6, 6, 10};
        int[] docks2 = {2, 6, 8, 8};
        int extraCapacity2 = 3;
        int result2 = solution.maximumMatchedCrates(crates2, docks2, extraCapacity2);
        System.out.println(result2); // Expected: 4
    }
}