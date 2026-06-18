import java.util.*;

/*
 * Title: Minimum Cost to Build a Palindrome from Fragments
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a string s representing a raw message and an integer array cost of the same length,
 * where cost[i] is the cost to keep character s[i] in the final message. You may delete any characters
 * you want, and deleting a character has no cost. Your goal is to build a palindrome subsequence from s
 * such that the sum of costs of the kept characters is as small as possible. Among all non-empty palindrome
 * subsequences that can be formed, return the minimum possible total keep-cost.
 *
 * A subsequence is formed by deleting zero or more characters without changing the order of the remaining
 * characters. A palindrome reads the same forward and backward.
 *
 * Constraints:
 * - 1 <= s.length <= 1000
 * - s.length == cost.length
 * - 1 <= cost[i] <= 10^6
 * - s contains only lowercase English letters
 *
 * Important correctness note:
 * Any single character is itself a non-empty palindrome subsequence. Therefore, the minimum possible keep-cost
 * over all non-empty palindromic subsequences is simply the minimum cost of any single character in the string.
 *
 * Even though the answer can be derived directly from that observation, this solution also includes a quadratic
 * dynamic programming method that computes the same result over all substrings, as requested.
 */

public class Solution {

    /**
     * Computes the minimum keep-cost of any non-empty palindromic subsequence using dynamic programming.
     *
     * Core DP idea:
     * dp[i][j] = minimum keep-cost of any non-empty palindromic subsequence that can be formed
     *            from the substring s[i..j].
     *
     * Transition:
     * 1. We may ignore s[i], so dp[i][j] can be dp[i + 1][j].
     * 2. We may ignore s[j], so dp[i][j] can be dp[i][j - 1].
     * 3. We may keep only s[i], giving cost[i].
     * 4. We may keep only s[j], giving cost[j].
     * 5. If s[i] == s[j], then we may form a palindrome using both ends:
     *    - If i == j, that is just one character.
     *    - If j == i + 1, then the pair itself is a palindrome with cost[i] + cost[j].
     *    - Otherwise, we can wrap any palindrome from inside with these matching characters.
     *
     * However, because a single character is always a valid palindrome, the global optimum will always be
     * the minimum single-character cost. This DP still correctly computes that value.
     *
     * @param s the input string
     * @param cost the keep-cost array, where cost[i] is the cost of keeping s.charAt(i)
     * @return the minimum total keep-cost of any non-empty palindromic subsequence
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public int minimumCostPalindromeSubsequence(String s, int[] cost) {
        validateInput(s, cost);

        int n = s.length();

        // dp[i][j] will store the minimum keep-cost of any non-empty palindromic subsequence
        // that can be built from substring s[i..j].
        long[][] dp = new long[n][n];

        // Base case:
        // A substring of length 1 contains exactly one non-empty palindromic subsequence:
        // the single character itself.
        for (int i = 0; i < n; i++) {
            dp[i][i] = cost[i];
        }

        // We now build answers for longer substrings.
        // len is the current substring length.
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;

                // Start with a very large value.
                long best = Long.MAX_VALUE;

                // Option 1:
                // Ignore the left character s[i].
                // Then we are free to choose the best palindrome from s[i+1..j].
                best = Math.min(best, dp[i + 1][j]);

                // Option 2:
                // Ignore the right character s[j].
                // Then we are free to choose the best palindrome from s[i..j-1].
                best = Math.min(best, dp[i][j - 1]);

                // Option 3:
                // Keep only the left character.
                // A single character is always a palindrome.
                best = Math.min(best, cost[i]);

                // Option 4:
                // Keep only the right character.
                best = Math.min(best, cost[j]);

                // Option 5:
                // If the two end characters match, we may use them together.
                if (s.charAt(i) == s.charAt(j)) {
                    if (len == 2) {
                        // Two equal adjacent characters form a palindrome of length 2.
                        best = Math.min(best, (long) cost[i] + cost[j]);
                    } else {
                        // Wrap the best palindrome from the inside with matching ends.
                        // This creates another valid palindrome subsequence.
                        best = Math.min(best, dp[i + 1][j - 1] + cost[i] + cost[j]);
                    }
                }

                dp[i][j] = best;
            }
        }

        return (int) dp[0][n - 1];
    }

    /**
     * Computes the answer using the key observation:
     * every single character is a valid non-empty palindrome subsequence,
     * so the minimum possible keep-cost is simply the minimum value in the cost array.
     *
     * This method is included both as a correctness cross-check and as the simplest optimal solution.
     *
     * @param s the input string
     * @param cost the keep-cost array
     * @return the minimum total keep-cost of any non-empty palindromic subsequence
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minimumCostPalindromeSubsequenceOptimized(String s, int[] cost) {
        validateInput(s, cost);

        int answer = Integer.MAX_VALUE;
        for (int value : cost) {
            answer = Math.min(answer, value);
        }
        return answer;
    }

    /**
     * Validates the input according to the problem constraints.
     *
     * @param s the input string
     * @param cost the cost array
     * @return nothing; throws an exception if input is invalid
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public void validateInput(String s, int[] cost) {
        if (s == null || cost == null) {
            throw new IllegalArgumentException("String and cost array must not be null.");
        }
        if (s.length() == 0) {
            throw new IllegalArgumentException("String length must be at least 1.");
        }
        if (s.length() != cost.length) {
            throw new IllegalArgumentException("String length and cost array length must match.");
        }
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < 'a' || ch > 'z') {
                throw new IllegalArgumentException("String must contain only lowercase English letters.");
            }
            if (cost[i] < 1) {
                throw new IllegalArgumentException("Each cost must be at least 1.");
            }
        }
    }

    /**
     * Runs a single demonstration test case and prints both the DP result and the optimized result.
     *
     * @param s the input string
     * @param cost the cost array
     * @return nothing
     * Time complexity: O(n^2) because it invokes the DP method
     * Space complexity: O(n^2) because it invokes the DP method
     */
    public void runDemo(String s, int[] cost) {
        int dpAnswer = minimumCostPalindromeSubsequence(s, cost);
        int optimizedAnswer = minimumCostPalindromeSubsequenceOptimized(s, cost);

        System.out.println("s = " + s);
        System.out.println("cost = " + Arrays.toString(cost));
        System.out.println("DP answer = " + dpAnswer);
        System.out.println("Optimized answer = " + optimizedAnswer);
        System.out.println();
    }

    /**
     * Main method demonstrating the solution on sample-style inputs.
     *
     * Note:
     * The original statement's first example contains an internal contradiction:
     * it says keeping "b" costs 2, but then claims the answer is 3.
     * Since any single character is a valid palindrome subsequence, the correct answer for:
     * s = "abca", cost = [4, 2, 7, 3]
     * is 2.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: depends on the demo inputs; each demo call is O(n^2)
     * Space complexity: depends on the demo inputs; each demo call is O(n^2)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Demonstration 1:
        // Single-character palindromes are: 'a'(4), 'b'(2), 'c'(7), 'a'(3)
        // The minimum is 2, so the correct answer is 2.
        solution.runDemo("abca", new int[]{4, 2, 7, 3});

        // Demonstration 2:
        // The whole string is a palindrome, but keeping only 'e' costs 1, which is smaller.
        solution.runDemo("racecar", new int[]{8, 6, 5, 1, 5, 6, 8});

        // Additional quick checks.
        solution.runDemo("a", new int[]{5});
        solution.runDemo("aaaa", new int[]{9, 4, 7, 2});
        solution.runDemo("xyz", new int[]{10, 3, 8});
    }
}