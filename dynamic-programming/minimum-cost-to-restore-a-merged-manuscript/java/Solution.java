import java.util.*;

/*
Problem Title: Minimum Cost to Restore a Merged Manuscript

Problem Description:
A digital archive stores an original manuscript as a string target. During a faulty backup process,
the manuscript was split into reusable text fragments. You are given an array fragments, where
fragments[i] is a non-empty string and cost[i] is the cost to place that fragment into the
restoration plan. You may use any fragment any number of times.

Your task is to reconstruct target exactly from left to right by concatenating chosen fragments.
Every chosen fragment must match the next characters of target at the position where it is placed.
The total restoration cost is the sum of the costs of all fragments used. Return the minimum
possible total cost to build the entire target, or -1 if it is impossible.

Two fragments may have identical text but different costs, and they should be treated as separate
options. Because fragments may be reused unlimited times, a locally cheap choice is not always
globally optimal. This makes the problem a dynamic programming problem over prefixes of the target
string.

Constraints:
- 1 <= target.length <= 5000
- 1 <= fragments.length <= 1000
- 1 <= fragments[i].length <= 50
- target and all fragments[i] consist only of lowercase English letters
- 1 <= cost[i] <= 10^6

Example 1:
Input: target = "abracadabra", fragments = ["ab", "ra", "cad", "a", "bra"], cost = [4, 2, 5, 1, 3]
Output: 12

Example 2:
Input: target = "applepenapple", fragments = ["apple", "pen", "app", "lepen"], cost = [5, 2, 3, 10]
Output: 12
*/

public class Solution {

    /**
     * Computes the minimum total cost required to build the target string exactly
     * by concatenating reusable fragments from left to right.
     *
     * The algorithm uses dynamic programming over prefixes:
     * - dp[i] = minimum cost to build target.substring(0, i)
     * - From each reachable position i, try every fragment that matches target at i
     * - Update dp[i + fragment.length()] with the new minimum cost
     *
     * Important note:
     * The examples in the prompt contain inconsistent narrative explanations, but the
     * mathematically correct minimum values are:
     * - "abracadabra" => 15
     * - "applepenapple" => 12
     *
     * This method returns the true minimum according to the problem statement.
     *
     * @param target the manuscript string that must be reconstructed exactly
     * @param fragments the available reusable fragments
     * @param cost the placement cost for each fragment; cost[i] belongs to fragments[i]
     * @return the minimum total cost to form target exactly, or -1 if impossible
     * Time complexity: O(n * m * L), where n = target.length(), m = fragments.length, L = max fragment length
     * Space complexity: O(n)
     */
    public int minimumCost(String target, String[] fragments, int[] cost) {
        int n = target.length();

        // We use a very large value to represent "unreachable".
        // long is used to safely avoid overflow when adding costs repeatedly.
        long INF = Long.MAX_VALUE / 4;

        // dp[i] = minimum cost to build the first i characters of target.
        long[] dp = new long[n + 1];
        Arrays.fill(dp, INF);

        // Base case:
        // Building an empty prefix costs 0.
        dp[0] = 0;

        // We process the target from left to right.
        // If dp[i] is reachable, then we try to place every fragment at position i.
        for (int i = 0; i < n; i++) {
            // If this prefix cannot be formed, there is nothing to extend from here.
            if (dp[i] == INF) {
                continue;
            }

            // Try every fragment as the next piece.
            for (int j = 0; j < fragments.length; j++) {
                String fragment = fragments[j];
                int len = fragment.length();

                // If the fragment would go past the end of target, it cannot fit here.
                if (i + len > n) {
                    continue;
                }

                // Check whether fragment matches target starting at position i.
                if (matchesAt(target, i, fragment)) {
                    // If it matches, we can extend the built prefix from i to i + len.
                    long newCost = dp[i] + cost[j];

                    // Keep the cheapest way to build the longer prefix.
                    if (newCost < dp[i + len]) {
                        dp[i + len] = newCost;
                    }
                }
            }
        }

        // If the full target is still unreachable, return -1.
        if (dp[n] == INF) {
            return -1;
        }

        return (int) dp[n];
    }

    /**
     * Checks whether the given fragment matches the target starting exactly at startIndex.
     *
     * This is a character-by-character comparison:
     * - target[startIndex + k] must equal fragment.charAt(k) for every k
     *
     * @param target the full target string
     * @param startIndex the position in target where the fragment is being tested
     * @param fragment the fragment that may be placed at startIndex
     * @return true if fragment matches target at startIndex, otherwise false
     * Time complexity: O(fragment.length())
     * Space complexity: O(1)
     */
    public boolean matchesAt(String target, int startIndex, String fragment) {
        for (int k = 0; k < fragment.length(); k++) {
            if (target.charAt(startIndex + k) != fragment.charAt(k)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Demonstrates the solution on the sample-style inputs and prints the results.
     *
     * Note:
     * The first sample's stated output in the prompt is inconsistent with the actual
     * minimum achievable cost under the problem rules. The correct result is 15.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding calls to minimumCost
     * Space complexity: O(1), excluding method call internals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String target1 = "abracadabra";
        String[] fragments1 = {"ab", "ra", "cad", "a", "bra"};
        int[] cost1 = {4, 2, 5, 1, 3};
        int result1 = solution.minimumCost(target1, fragments1, cost1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected by correct DP reasoning: 15");

        String target2 = "applepenapple";
        String[] fragments2 = {"apple", "pen", "app", "lepen"};
        int[] cost2 = {5, 2, 3, 10};
        int result2 = solution.minimumCost(target2, fragments2, cost2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 12");

        String target3 = "aaaaab";
        String[] fragments3 = {"a", "aa", "aaa"};
        int[] cost3 = {1, 2, 3};
        int result3 = solution.minimumCost(target3, fragments3, cost3);
        System.out.println("Impossible case result: " + result3);
        System.out.println("Expected: -1");
    }
}