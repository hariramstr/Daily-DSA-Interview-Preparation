import java.util.*;

/*
 * Title: Minimum Edit Cost to Form K Palindrome Chapters
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a lowercase string s representing a draft manuscript and an integer k.
 * The editor wants to divide the manuscript into exactly k non-empty contiguous chapters.
 * After the split, each chapter may be edited independently so that the final text of every
 * chapter is a palindrome.
 *
 * In one edit, you may change any single character in a chapter to any other lowercase
 * English letter. The cost of a chapter is the minimum number of character changes required
 * to turn that chapter into a palindrome. Your task is to return the minimum total edit cost
 * over all ways to split s into exactly k chapters.
 *
 * A palindrome reads the same forward and backward. For example, the chapter "abca" can be
 * turned into a palindrome with 1 edit, because changing either 'b' to 'c' or 'c' to 'b'
 * makes it symmetric. The split must preserve order, and every character of s must belong
 * to exactly one chapter.
 *
 * Return the minimum possible total number of edits.
 *
 * Constraints:
 * - 1 <= s.length <= 200
 * - 1 <= k <= s.length
 * - s consists only of lowercase English letters
 *
 * Example 1:
 * Input: s = "abcac", k = 2
 * Output: 1
 * Explanation: Split as "ab" + "cac". The first chapter needs 1 edit to become a palindrome,
 * and the second chapter is already a palindrome. Total cost = 1.
 *
 * Example 2:
 * Input: s = "leetcode", k = 3
 * Output: 2
 *
 * Core idea:
 * 1. Precompute cost[left][right] = minimum edits needed to make substring s[left..right]
 *    a palindrome.
 * 2. Use dynamic programming:
 *    dp[i][parts] = minimum total cost to split the first i characters of s into exactly
 *    parts non-empty chapters.
 * 3. Transition:
 *    Try every possible starting point of the last chapter.
 */

public class Solution {

    /**
     * Computes the minimum total number of edits required to split the given string into
     * exactly k non-empty contiguous chapters such that each chapter can be transformed
     * into a palindrome with minimum edit cost.
     *
     * Time complexity: O(n^2 + k * n^2), where n is s.length()
     * Space complexity: O(n^2 + k * n)
     *
     * @param s the input lowercase string representing the manuscript
     * @param k the exact number of non-empty contiguous chapters to split into
     * @return the minimum total edit cost needed so every chapter can be made palindromic
     */
    public int palindromePartition(String s, int k) {
        int n = s.length();

        // Step 1:
        // Precompute the cost to convert every substring s[left..right] into a palindrome.
        //
        // cost[left][right] means:
        // "How many character changes are needed, at minimum, to make the substring
        //  from index left to index right inclusive become a palindrome?"
        //
        // Example:
        // substring "abca"
        // compare pairs:
        //   a vs a -> already equal, no cost
        //   b vs c -> different, +1 cost
        // total = 1
        int[][] cost = computePalindromeCosts(s);

        // We use a large number to represent "impossible" or "not yet computed".
        int INF = 1_000_000_000;

        // dp[i][parts] =
        // minimum cost to split the first i characters (s[0..i-1]) into exactly "parts" chapters.
        //
        // Dimensions:
        // i ranges from 0 to n
        // parts ranges from 0 to k
        int[][] dp = new int[n + 1][k + 1];

        // Initialize all states as impossible first.
        for (int i = 0; i <= n; i++) {
            Arrays.fill(dp[i], INF);
        }

        // Base case:
        // 0 characters split into 0 chapters costs 0.
        dp[0][0] = 0;

        // Step 2:
        // Fill the DP table.
        //
        // We build answers for increasing prefix lengths and chapter counts.
        for (int i = 1; i <= n; i++) {
            // We cannot split i characters into more than i non-empty chapters.
            int maxParts = Math.min(k, i);

            for (int parts = 1; parts <= maxParts; parts++) {
                // We want the last chapter to be s[start..i-1].
                //
                // Then the previous part must cover the first "start" characters,
                // split into exactly (parts - 1) chapters.
                //
                // So transition is:
                // dp[i][parts] = min over all valid start:
                //                dp[start][parts - 1] + cost[start][i - 1]
                //
                // Validity condition:
                // We need enough characters before "start" to form (parts - 1) non-empty chapters.
                // Therefore start must be at least (parts - 1).
                for (int start = parts - 1; start < i; start++) {
                    if (dp[start][parts - 1] == INF) {
                        continue;
                    }

                    int candidate = dp[start][parts - 1] + cost[start][i - 1];
                    if (candidate < dp[i][parts]) {
                        dp[i][parts] = candidate;
                    }
                }
            }
        }

        return dp[n][k];
    }

    /**
     * Precomputes the minimum edit cost for every substring of the given string to become
     * a palindrome.
     *
     * The recurrence is:
     * - If s[left] == s[right], then
     *   cost[left][right] = cost[left + 1][right - 1]
     * - Otherwise,
     *   cost[left][right] = cost[left + 1][right - 1] + 1
     *
     * This works because for a palindrome, mirrored characters must match. If the two ends
     * differ, exactly one edit is enough to make that pair match.
     *
     * Time complexity: O(n^2), where n is s.length()
     * Space complexity: O(n^2)
     *
     * @param s the input string
     * @return a 2D array where result[left][right] is the minimum number of edits needed
     *         to make substring s[left..right] a palindrome
     */
    public int[][] computePalindromeCosts(String s) {
        int n = s.length();
        int[][] cost = new int[n][n];

        // We process substrings by increasing length.
        //
        // Length 1:
        // Any single character is already a palindrome, so cost = 0.
        //
        // Length 2 and above:
        // Use the inner substring result.
        for (int length = 2; length <= n; length++) {
            for (int left = 0; left + length - 1 < n; left++) {
                int right = left + length - 1;

                // If the two boundary characters are different,
                // we need one edit for this mirrored pair.
                int mismatchCost = (s.charAt(left) == s.charAt(right)) ? 0 : 1;

                // For length 2, there is no inner substring, so inner cost is 0.
                // For length >= 3, add the cost of the inside substring.
                if (length == 2) {
                    cost[left][right] = mismatchCost;
                } else {
                    cost[left][right] = cost[left + 1][right - 1] + mismatchCost;
                }
            }
        }

        return cost;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement and a few
     * additional sanity checks.
     *
     * Time complexity: O(total input sizes processed by the demonstration)
     * Space complexity: O(max auxiliary space used by a single call)
     *
     * @param args command-line arguments (not used)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "abcac";
        int k1 = 2;
        int result1 = solution.palindromePartition(s1, k1);
        System.out.println("Input: s = \"" + s1 + "\", k = " + k1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 1");
        System.out.println();

        String s2 = "leetcode";
        int k2 = 3;
        int result2 = solution.palindromePartition(s2, k2);
        System.out.println("Input: s = \"" + s2 + "\", k = " + k2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 2");
        System.out.println();

        // Additional quick checks:
        String s3 = "a";
        int k3 = 1;
        int result3 = solution.palindromePartition(s3, k3);
        System.out.println("Input: s = \"" + s3 + "\", k = " + k3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        String s4 = "abc";
        int k4 = 3;
        int result4 = solution.palindromePartition(s4, k4);
        System.out.println("Input: s = \"" + s4 + "\", k = " + k4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
        System.out.println();

        String s5 = "abc";
        int k5 = 1;
        int result5 = solution.palindromePartition(s5, k5);
        System.out.println("Input: s = \"" + s5 + "\", k = " + k5);
        System.out.println("Output: " + result5);
        System.out.println("Expected: 1");
    }
}