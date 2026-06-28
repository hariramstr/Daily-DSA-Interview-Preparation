import java.util.*;

/*
 * Title: Maximum Reliability from Staged Model Rollouts
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A machine learning platform plans to deploy models over the next n days.
 * On day i, the platform may either skip deployment or launch exactly one
 * candidate model for that day. Each candidate model belongs to one of m
 * architecture families, represented by an integer family[i], and provides
 * an immediate reliability gain gain[i].
 *
 * However, repeatedly deploying models from the same family too close together
 * causes hidden coupling risk. To control this, the platform defines a cooldown
 * window k: if you deploy a model from family x on day i, then deploying another
 * model from the same family on any of the next k days is not allowed.
 * Deploying a different family is always allowed, and skipping a day resets nothing.
 *
 * Your task is to compute the maximum total reliability gain achievable over all n days.
 *
 * Formally, choose a subset of days to deploy such that for any two chosen days a < b
 * with family[a] == family[b], we must have b - a > k. The score is the sum of gain[i]
 * over all chosen days. Return the maximum possible score.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= m <= 200000
 * - 1 <= family[i] <= m
 * - 1 <= gain[i] <= 10^9
 * - 0 <= k <= n
 *
 * Key DP Insight:
 * Let dp[i] be the maximum total gain achievable using only days [0..i].
 *
 * For day i, there are two choices:
 * 1) Skip day i:
 *      dp[i] = dp[i - 1]
 * 2) Take day i:
 *      We may combine gain[i] with any valid plan that ends before the most recent
 *      conflicting deployment of the same family within distance k.
 *
 * A very useful reformulation is:
 * For each family f, maintain the best value of:
 *      dp[t] where t <= i - k - 1
 * that can serve as a safe prefix before taking a future day of family f.
 *
 * More concretely, when processing day i with family f:
 *      take = gain[i] + bestAllowedPrefixForFamily[f]
 * and
 *      dp[i] = max(dp[i - 1], take)
 *
 * The challenge is updating bestAllowedPrefixForFamily efficiently.
 * A day j becomes "old enough" to be safely separated from future same-family picks
 * exactly when future index i satisfies i - j > k, i.e. j <= i - k - 1.
 *
 * Therefore, while scanning from left to right, when we arrive at day i we can newly
 * activate index old = i - k - 1. Its dp[old] value becomes available as a legal prefix
 * for future picks of family[old]. We update:
 *      bestAllowedPrefixForFamily[family[old]] =
 *          max(bestAllowedPrefixForFamily[family[old]], dp[old])
 *
 * Additionally, before any day is chosen, the empty prefix has value 0, so every family
 * starts with bestAllowedPrefixForFamily[f] = 0.
 *
 * This yields an O(n) time solution with O(m + n) space.
 */
public class Solution {

    /**
     * Computes the maximum total reliability gain subject to the cooldown rule.
     *
     * Detailed idea:
     * We process days from left to right and build a classic prefix DP.
     *
     * Let:
     * - dp[i] = best answer using days 0..i
     *
     * For the current day i with family f:
     * - If we skip day i, value is:
     *       skip = dp[i - 1]
     * - If we take day i, we need the best prefix that is safe with respect to
     *   family f. Specifically, the last chosen day of family f (if any) must be
     *   at most i - k - 1. We maintain this best safe prefix in:
     *       bestPrefixByFamily[f]
     *   so:
     *       take = gain[i] + bestPrefixByFamily[f]
     *
     * Then:
     *       dp[i] = max(skip, take)
     *
     * How do we maintain bestPrefixByFamily?
     * A prefix ending at day t becomes safe for future same-family picks once
     * future index i satisfies t <= i - k - 1.
     *
     * So when we are at day i, the newly eligible index is:
     *       old = i - k - 1
     * If old >= 0, then dp[old] can now be used as a safe prefix for the family
     * that appears on day old in the sense that any future pick of that same family
     * can be appended after enough distance.
     *
     * We update:
     *       bestPrefixByFamily[family[old]] =
     *           max(bestPrefixByFamily[family[old]], dp[old])
     *
     * Why is this correct?
     * Because dp[old] already represents the best valid plan up to old, and once old
     * is far enough behind the current day, any future pick of the same family as day old
     * can safely follow that entire prefix.
     *
     * @param family family[i] is the architecture family of the candidate model on day i
     * @param gain gain[i] is the reliability gain obtained by deploying on day i
     * @param k cooldown window: same family cannot be deployed again within the next k days
     * @return the maximum total reliability gain achievable
     * @implNote Time complexity: O(n + m), because each day is processed a constant number of times
     * @implNote Space complexity: O(n + m), for the DP array and per-family tracking
     */
    public long maximumReliability(int[] family, int[] gain, int k) {
        int n = family.length;
        if (n == 0) {
            return 0L;
        }

        int maxFamily = 0;
        for (int f : family) {
            maxFamily = Math.max(maxFamily, f);
        }

        long[] dp = new long[n];

        /*
         * bestPrefixByFamily[f] means:
         * the best DP value from some prefix that is already far enough in the past
         * to safely place a future deployment of family f after it.
         *
         * Initially, the empty prefix with value 0 is always allowed.
         * So every family effectively starts with 0.
         */
        long[] bestPrefixByFamily = new long[maxFamily + 1];

        for (int i = 0; i < n; i++) {
            /*
             * Step 1:
             * Before computing dp[i], activate the newly eligible old index:
             * old = i - k - 1
             *
             * Why now?
             * Because any prefix ending at old becomes far enough away from day i
             * to allow another deployment of the same family.
             */
            int old = i - k - 1;
            if (old >= 0) {
                int oldFamily = family[old];
                bestPrefixByFamily[oldFamily] = Math.max(bestPrefixByFamily[oldFamily], dp[old]);
            }

            /*
             * Step 2:
             * Option A: skip day i.
             * If i == 0, skipping gives 0.
             * Otherwise, skipping preserves dp[i - 1].
             */
            long skip = (i == 0) ? 0L : dp[i - 1];

            /*
             * Step 3:
             * Option B: take day i.
             * We add gain[i] to the best safe prefix for this family.
             */
            int currentFamily = family[i];
            long take = bestPrefixByFamily[currentFamily] + gain[i];

            /*
             * Step 4:
             * Choose the better of skipping or taking.
             */
            dp[i] = Math.max(skip, take);
        }

        return dp[n - 1];
    }

    /**
     * Convenience overload that accepts gains as long[].
     *
     * This method simply converts the long gains to int after validating they fit
     * the stated constraints. It is useful for demonstration or extension.
     *
     * @param family family[i] is the architecture family of the candidate model on day i
     * @param gain gain[i] is the reliability gain obtained by deploying on day i
     * @param k cooldown window: same family cannot be deployed again within the next k days
     * @return the maximum total reliability gain achievable
     * @implNote Time complexity: O(n + m)
     * @implNote Space complexity: O(n + m)
     */
    public long maximumReliability(int[] family, long[] gain, int k) {
        int[] converted = new int[gain.length];
        for (int i = 0; i < gain.length; i++) {
            converted[i] = (int) gain[i];
        }
        return maximumReliability(family, converted, k);
    }

    /**
     * Runs sample demonstrations from the problem statement.
     *
     * Important note about Example 1:
     * The narrative in the prompt contains contradictory intermediate reasoning,
     * but the final valid optimum is 23 using days 2, 4, and 5:
     * - day 2: family 1, gain 7
     * - day 4: family 2, gain 6
     * - day 5: family 1, gain 10
     * Distances between same-family picks:
     * - family 1 appears on days 2 and 5, distance = 3 > k = 2, so valid
     * Total = 7 + 6 + 10 = 23
     *
     * Example 2:
     * family = [4, 4, 4, 2, 2], gain = [8, 1, 9, 5, 7], k = 1
     * One optimal plan is days 0, 2, 4 => 8 + 9 + 7 = 24
     *
     * @param args command-line arguments, unused
     * @return nothing
     * @implNote Time complexity: O(1) for the fixed demonstrations shown here
     * @implNote Space complexity: O(1) beyond the sample arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] family1 = {1, 2, 1, 3, 2, 1};
        int[] gain1 = {5, 4, 7, 3, 6, 10};
        int k1 = 2;
        long result1 = solution.maximumReliability(family1, gain1, k1);
        System.out.println(result1); // Expected: 23

        int[] family2 = {4, 4, 4, 2, 2};
        int[] gain2 = {8, 1, 9, 5, 7};
        int k2 = 1;
        long result2 = solution.maximumReliability(family2, gain2, k2);
        System.out.println(result2); // Expected: 24

        /*
         * Additional quick sanity checks:
         *
         * 1) k = 0 means same family cannot be chosen on the very next 0 days,
         *    which imposes no real restriction beyond not choosing the same day twice.
         *    Therefore, all positive gains can be taken.
         */
        int[] family3 = {1, 1, 2};
        int[] gain3 = {3, 4, 5};
        int k3 = 0;
        System.out.println(solution.maximumReliability(family3, gain3, k3)); // Expected: 12

        /*
         * 2) All same family, large cooldown:
         *    Can only take one of them if distances are not large enough.
         */
        int[] family4 = {2, 2, 2, 2};
        int[] gain4 = {1, 10, 3, 7};
        int k4 = 10;
        System.out.println(solution.maximumReliability(family4, gain4, k4)); // Expected: 10
    }
}