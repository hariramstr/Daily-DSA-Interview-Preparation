import java.util.*;

/*
 * Title: Minimum Latency to Decode a Layered Signal Tape
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A telemetry system stores a long signal tape as a string s of length n, where each
 * character is an uppercase letter representing a frequency band.
 *
 * To decode the tape, a hardware decoder may process any contiguous segment [l, r]
 * in one pass if the first and last characters of that segment are the same.
 * During that pass, the decoder resolves both matching endpoints together, and the
 * inside of the segment may be decoded before, after, or split across additional passes.
 * The latency of one pass is equal to the length of the segment being processed,
 * i.e. r - l + 1.
 *
 * Your task is to compute the minimum total latency required to fully decode the
 * entire tape.
 *
 * More formally, you may repeatedly choose a contiguous substring whose current
 * leftmost and rightmost undecoded characters are equal, pay its length as cost,
 * and mark those two endpoints as decoded. All characters must eventually be decoded.
 * You may choose the order of passes arbitrarily, and optimal solutions often require
 * nesting or splitting decisions, so greedy approaches do not work.
 *
 * Return the minimum possible total latency.
 *
 * Constraints:
 * - 1 <= n <= 400
 * - s consists only of uppercase English letters 'A' to 'Z'
 * - The answer fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: s = "ABCA"
 * Output: 6
 * Explanation:
 * Decode the outer A...A segment in one pass with cost 4,
 * then decode B and C individually with costs 1 and 1.
 * Total = 4 + 1 + 1 = 6.
 *
 * Example 2:
 * Input: s = "ABBA"
 * Output: 6
 * Explanation:
 * Decode the outer A...A segment with cost 4,
 * then decode the inner B...B segment with cost 2.
 * Total = 6.
 *
 * Key Dynamic Programming Interpretation:
 * Let dp[l][r] be the minimum cost to fully decode substring s[l..r].
 *
 * For the leftmost character s[l], in any valid complete decoding of s[l..r],
 * it must eventually be resolved together with some position k in [l..r] such that
 * s[l] == s[k].
 *
 * If k == l, then s[l] is decoded alone with cost 1, and the rest costs dp[l+1][r].
 *
 * If k > l and s[l] == s[k], then we can perform one pass on segment [l, k],
 * paying cost (k - l + 1). Everything inside that segment, namely s[l+1..k-1],
 * must also be fully decoded, and everything after it, namely s[k+1..r], must
 * also be fully decoded. Therefore:
 *
 * dp[l][r] = min over all k in [l..r] with s[l] == s[k] of
 *            (k - l + 1) + dp[l+1][k-1] + dp[k+1][r]
 *
 * This recurrence exactly matches the examples:
 *
 * "ABCA":
 * - Pair A at 0 with A at 3: cost 4 + dp[1][2] + 0 = 4 + 2 = 6
 *
 * "ABBA":
 * - Pair A at 0 with A at 3: cost 4 + dp[1][2] + 0
 * - dp[1][2] = pair B with B => 2
 * - total = 6
 */

public class Solution {

    /**
     * Computes the minimum total latency required to fully decode the entire tape.
     *
     * Dynamic programming idea:
     * dp[l][r] = minimum cost to decode substring s[l..r].
     *
     * We decide which matching position k will be paired with the leftmost character s[l].
     * Since the operation must resolve equal endpoints, k must satisfy s[l] == s[k].
     *
     * Transition:
     * - If k == l, then s[l] is decoded alone, costing 1.
     *   Remaining cost is dp[l+1][r].
     * - If k > l, then we decode endpoints l and k together in one pass of length (k-l+1),
     *   and recursively decode:
     *     1) the inside substring s[l+1..k-1]
     *     2) the suffix substring s[k+1..r]
     *
     * Therefore:
     * dp[l][r] = min((k-l+1) + dp[l+1][k-1] + dp[k+1][r]) for all k in [l..r] with s[l] == s[k]
     *
     * @param s the signal tape string
     * @return the minimum possible total latency to fully decode the tape
     * @implNote Time complexity: O(n^3), because there are O(n^2) states and each state
     * scans O(n) possible matching positions.
     * @implNote Space complexity: O(n^2) for the DP table.
     */
    public int minimumLatency(String s) {
        int n = s.length();

        // dp[l][r] will store the minimum cost to decode substring s[l..r].
        int[][] dp = new int[n][n];

        // We build answers for shorter substrings first, then longer ones.
        // This ensures that when we compute dp[l][r], all smaller needed subproblems
        // such as dp[l+1][k-1] and dp[k+1][r] are already known.
        for (int len = 1; len <= n; len++) {
            for (int l = 0; l + len - 1 < n; l++) {
                int r = l + len - 1;

                // Start with a very large value because we are taking minimums.
                int best = Integer.MAX_VALUE;

                // Try every possible position k in [l..r] that can be paired with l.
                // The pair is valid only if s[l] == s[k].
                for (int k = l; k <= r; k++) {
                    if (s.charAt(l) != s.charAt(k)) {
                        continue;
                    }

                    // Cost of the pass that resolves positions l and k together.
                    // If k == l, this is just 1, meaning the character is decoded alone.
                    int current = k - l + 1;

                    // Add cost for the inside substring s[l+1..k-1], if it exists.
                    if (l + 1 <= k - 1) {
                        current += dp[l + 1][k - 1];
                    }

                    // Add cost for the remaining suffix s[k+1..r], if it exists.
                    if (k + 1 <= r) {
                        current += dp[k + 1][r];
                    }

                    // Keep the best possible choice.
                    best = Math.min(best, current);
                }

                dp[l][r] = best;
            }
        }

        return dp[0][n - 1];
    }

    /**
     * A helper method that prints the DP result for a given input string.
     *
     * @param s the input signal tape
     * @return the computed minimum latency for the given string
     * @implNote Time complexity: O(n^3), delegated to minimumLatency.
     * @implNote Space complexity: O(n^2), delegated to minimumLatency.
     */
    public int solveAndPrint(String s) {
        int answer = minimumLatency(s);
        System.out.println("Input: " + s);
        System.out.println("Minimum latency: " + answer);
        return answer;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a few additional examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * @implNote Time complexity: depends on the test strings used.
     * @implNote Space complexity: depends on the test strings used.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // "ABCA"
        // Best plan:
        // pair A...A with cost 4, then B alone (1), C alone (1) => total 6
        System.out.println("Sample 1:");
        solution.solveAndPrint("ABCA");
        System.out.println("Expected: 6");
        System.out.println();

        // Sample 2:
        // "ABBA"
        // Best plan:
        // pair A...A with cost 4, then pair B...B with cost 2 => total 6
        System.out.println("Sample 2:");
        solution.solveAndPrint("ABBA");
        System.out.println("Expected: 6");
        System.out.println();

        // Additional small sanity checks.
        System.out.println("Additional Tests:");
        solution.solveAndPrint("A");      // 1
        System.out.println("Expected: 1");
        System.out.println();

        solution.solveAndPrint("AA");     // pair both together => 2
        System.out.println("Expected: 2");
        System.out.println();

        solution.solveAndPrint("ABC");    // all alone => 3
        System.out.println("Expected: 3");
        System.out.println();

        solution.solveAndPrint("ABA");    // outer A...A => 3, B alone => 1, total 4
        System.out.println("Expected: 4");
    }
}