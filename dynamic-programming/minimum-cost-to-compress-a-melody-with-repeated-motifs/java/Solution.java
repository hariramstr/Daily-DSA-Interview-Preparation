import java.util.*;

/*
 * Minimum Cost to Compress a Melody with Repeated Motifs
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A digital music editor stores a melody as an array of integers, where each integer
 * represents a note pitch. To reduce storage, the editor may encode the melody as a
 * sequence of blocks. A block can be stored in one of two ways:
 *
 * 1. Raw block: store every note directly. A raw block covering notes i..j costs
 *    (j - i + 1).
 *
 * 2. Motif block: if the subarray notes[i..j] is made of one smaller pattern repeated
 *    consecutively one or more times, it may be stored as:
 *       cost(pattern) + repeatPenalty
 *    where repeatPenalty is a fixed integer P, and cost(pattern) is the minimum
 *    compressed cost of that smaller pattern.
 *
 * You may recursively compress the pattern itself, and you may partition the melody
 * into any number of blocks. Your task is to compute the minimum total cost to encode
 * the entire melody.
 *
 * Formally, for any subarray notes[i..j], you may either keep it raw, split it into
 * two non-empty consecutive parts, or encode it as repeated copies of a shorter
 * subarray whose length divides (j - i + 1). A repeated motif block is valid only if
 * every copy is exactly identical.
 *
 * Return the minimum encoding cost for the full array.
 *
 * Constraints:
 * - 1 <= n <= 200
 * - 1 <= notes[i] <= 10^9
 * - 1 <= P <= 200
 * - Time complexity better than O(n^4 * n) is expected for a full solution
 *
 * Example 1:
 * Input: notes = [4, 7, 4, 7, 4, 7], P = 2
 * Output: 4
 * Explanation: The whole melody is 3 repetitions of [4, 7]. The pattern [4, 7]
 * costs 2 as raw. Encoding the repeated motif costs 2 + P = 4, which is better than
 * storing all 6 notes raw.
 *
 * Example 2:
 * Input: notes = [5, 5, 5, 8, 5, 5, 5, 8], P = 3
 * Output: 7
 * Explanation: The full melody is 2 repetitions of [5, 5, 5, 8]. That pattern costs
 * 4 raw, so encoding the whole array as a repeated motif costs 4 + 3 = 7. Splitting
 * into smaller parts does not do better.
 */

public class Solution {

    /**
     * Computes the minimum encoding cost for the full melody.
     *
     * Core idea:
     * We use interval dynamic programming.
     *
     * Let dp[i][j] be the minimum cost to encode notes[i..j].
     *
     * For each interval, we consider:
     * 1. Store it raw: cost = length
     * 2. Split it into two parts: dp[i][k] + dp[k+1][j]
     * 3. Encode it as repeated copies of a smaller pattern:
     *    if notes[i..j] consists of repeats of notes[i..i+d-1], then
     *    cost = dp[i][i+d-1] + P
     *
     * To make repetition checking efficient, we precompute an LCP table:
     * lcp[a][b] = length of the longest common prefix of suffixes starting at a and b.
     * Then we can test whether two equal-length blocks are identical in O(1).
     *
     * @param notes the melody represented as an integer array
     * @param p the fixed repeat penalty for encoding a repeated motif block
     * @return the minimum encoding cost for the entire array
     * Time complexity: O(n^3 log n) in the worst case due to interval DP and divisor checks
     * Space complexity: O(n^2)
     */
    public int minEncodingCost(int[] notes, int p) {
        int n = notes.length;
        if (n == 0) {
            return 0;
        }

        int[][] lcp = buildLcp(notes);
        List<Integer>[] divisors = buildProperDivisors(n);

        int[][] dp = new int[n][n];

        // We process intervals by increasing length.
        // This ensures that when we compute dp[i][j], all smaller intervals
        // needed for transitions have already been computed.
        for (int len = 1; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;

                // Option 1: store the whole interval raw.
                // Raw cost is simply the number of notes in the interval.
                dp[i][j] = len;

                // Option 2: split the interval into two non-empty consecutive parts.
                // We try every possible split point k:
                // [i..k] + [k+1..j]
                //
                // This is the standard interval DP partition transition.
                for (int k = i; k < j; k++) {
                    dp[i][j] = Math.min(dp[i][j], dp[i][k] + dp[k + 1][j]);
                }

                // Option 3: encode as repeated copies of a shorter pattern.
                //
                // If len has a proper divisor d, then the interval could potentially be
                // made of len / d copies of a block of length d.
                //
                // We only need to test proper divisors d < len.
                // If the interval is indeed periodic with period d, then:
                // cost = dp[i][i + d - 1] + p
                //
                // Important:
                // We use dp for the pattern itself, because the pattern may also be
                // recursively compressed.
                for (int d : divisors[len]) {
                    if (isRepeated(notes, lcp, i, len, d)) {
                        dp[i][j] = Math.min(dp[i][j], dp[i][i + d - 1] + p);
                    }
                }
            }
        }

        return dp[0][n - 1];
    }

    /**
     * Builds the LCP (Longest Common Prefix) table for the integer array.
     *
     * lcp[i][j] = number of equal consecutive elements starting from positions i and j.
     *
     * Example:
     * if notes[i] == notes[j], then
     * lcp[i][j] = 1 + lcp[i+1][j+1]
     * otherwise 0.
     *
     * We fill the table from bottom-right to top-left so that lcp[i+1][j+1]
     * is already known when computing lcp[i][j].
     *
     * @param notes the melody array
     * @return a 2D LCP table
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public int[][] buildLcp(int[] notes) {
        int n = notes.length;
        int[][] lcp = new int[n + 1][n + 1];

        // We iterate backwards because each state depends on the diagonal-down-right state.
        for (int i = n - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (notes[i] == notes[j]) {
                    lcp[i][j] = 1 + lcp[i + 1][j + 1];
                } else {
                    lcp[i][j] = 0;
                }
            }
        }

        return lcp;
    }

    /**
     * Precomputes all proper divisors for every length from 1 to n.
     *
     * For a given length len, we store all d such that:
     * - d divides len
     * - 1 <= d < len
     *
     * These are exactly the candidate pattern lengths for repeated motif encoding.
     *
     * @param n the maximum interval length
     * @return an array where divisors[len] contains all proper divisors of len
     * Time complexity: O(n log n)
     * Space complexity: O(n log n) total across all lists
     */
    @SuppressWarnings("unchecked")
    public List<Integer>[] buildProperDivisors(int n) {
        List<Integer>[] divisors = new ArrayList[n + 1];
        for (int i = 0; i <= n; i++) {
            divisors[i] = new ArrayList<>();
        }

        // For each possible divisor d, add it to all multiples > d.
        for (int d = 1; d <= n; d++) {
            for (int multiple = d + d; multiple <= n; multiple += d) {
                divisors[multiple].add(d);
            }
        }

        return divisors;
    }

    /**
     * Checks whether the interval notes[start .. start + len - 1] is composed of
     * repeated copies of its prefix of length blockLen.
     *
     * Since blockLen must divide len, the interval should contain:
     * copies = len / blockLen
     *
     * We verify that every adjacent block matches the first block.
     * Using the LCP table, equality of two blocks of length blockLen can be checked in O(1):
     * two blocks starting at a and b are equal iff lcp[a][b] >= blockLen.
     *
     * @param notes the melody array
     * @param lcp the precomputed LCP table
     * @param start the start index of the interval
     * @param len the interval length
     * @param blockLen the candidate repeating block length
     * @return true if the interval is exactly repeated copies of the first block, otherwise false
     * Time complexity: O(len / blockLen)
     * Space complexity: O(1) extra
     */
    public boolean isRepeated(int[] notes, int[][] lcp, int start, int len, int blockLen) {
        if (blockLen <= 0 || blockLen >= len || len % blockLen != 0) {
            return false;
        }

        int end = start + len;
        int first = start;

        // Compare every subsequent block with the first block.
        // If any block differs, the interval is not a valid repeated motif.
        for (int pos = start + blockLen; pos < end; pos += blockLen) {
            if (lcp[first][pos] < blockLen) {
                return false;
            }
        }

        return true;
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 4
     * Example 2 -> 7
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the algorithm calls
     * Space complexity: O(1) extra, excluding the algorithm's internal memory
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] notes1 = {4, 7, 4, 7, 4, 7};
        int p1 = 2;
        int result1 = solution.minEncodingCost(notes1, p1);
        System.out.println(result1); // Expected: 4

        int[] notes2 = {5, 5, 5, 8, 5, 5, 5, 8};
        int p2 = 3;
        int result2 = solution.minEncodingCost(notes2, p2);
        System.out.println(result2); // Expected: 7
    }
}