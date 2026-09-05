import java.util.*;

/*
 * Title: Minimum Delay to Sync Caption Segments
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A video platform stores an automatically generated caption track as a string s
 * of lowercase English letters, where each character represents the dominant
 * word category spoken during one second of video. To improve readability, the
 * platform wants to split the caption track into contiguous segments. Each
 * segment must have length between minLen and maxLen inclusive.
 *
 * For any chosen segment, its delay cost is defined as the number of character
 * changes needed to make the entire segment consist of only one repeated
 * character. For example, the segment "abaca" has delay cost 2, because changing
 * the two non-'a' characters makes all characters equal. The total
 * synchronization delay is the sum of delay costs over all segments.
 *
 * Your task is to return the minimum possible total synchronization delay needed
 * to partition the entire string into valid segments. If it is impossible to
 * partition the full string using only segment lengths in the allowed range,
 * return -1.
 *
 * Constraints:
 * - 1 <= s.length <= 5000
 * - s contains only lowercase English letters
 * - 1 <= minLen <= maxLen <= 100
 *
 * Example 1:
 * Input: s = "abacbc", minLen = 2, maxLen = 3
 * Output: 2
 * Explanation:
 * One optimal partition is "aba" + "cbc".
 * Cost("aba") = 1 because it can become "aaa".
 * Cost("cbc") = 1 because it can become "ccc".
 * Total = 2.
 *
 * Example 2:
 * Input: s = "aaabbbcc", minLen = 3, maxLen = 3
 * Output: -1
 * Explanation:
 * We must partition using only segments of length exactly 3.
 * Length 8 cannot be fully covered by segments of length 3.
 * Therefore, partitioning is impossible.
 */

public class Solution {

    /**
     * Computes the minimum total synchronization delay needed to partition the
     * entire string into contiguous segments whose lengths are between minLen
     * and maxLen inclusive.
     *
     * Core idea:
     * 1. Use dynamic programming over prefix length.
     * 2. Let dp[i] = minimum cost to partition the first i characters of s.
     * 3. For every ending position i, try every valid segment length len in
     *    [minLen, maxLen], so the last segment is s[i-len .. i-1].
     * 4. Segment cost = segment length - maximum frequency of any character
     *    inside that segment.
     * 5. Transition:
     *      dp[i] = min(dp[i], dp[i-len] + cost(segment))
     *
     * Since maxLen <= 100, we can efficiently compute segment frequencies by
     * expanding backward from each end position and maintaining a 26-sized count
     * array.
     *
     * @param s the caption track string consisting of lowercase English letters
     * @param minLen the minimum allowed segment length
     * @param maxLen the maximum allowed segment length
     * @return the minimum total delay, or -1 if no valid partition exists
     *
     * Time complexity: O(n * maxLen * 26) in the straightforward bounded-window
     *                  approach, which is effectively O(n * maxLen) because 26 is constant.
     * Space complexity: O(n) for the DP array, plus O(26) temporary frequency storage.
     */
    public int minimumDelay(String s, int minLen, int maxLen) {
        int n = s.length();

        // A large value used to represent "impossible" states in DP.
        // We avoid Integer.MAX_VALUE to prevent overflow when adding costs.
        int INF = 1_000_000_000;

        // dp[i] = minimum cost to partition the prefix s[0..i-1]
        int[] dp = new int[n + 1];
        Arrays.fill(dp, INF);

        // Base case:
        // Empty prefix requires zero cost and zero segments.
        dp[0] = 0;

        // We process every possible end position of the prefix.
        // end means we are considering the prefix s[0..end-1].
        for (int end = 1; end <= n; end++) {
            // Frequency array for the current backward-growing segment.
            int[] freq = new int[26];

            // maxFreq tracks the highest frequency of any single character
            // in the current segment.
            int maxFreq = 0;

            // We grow the last segment backward from position end-1.
            // The segment length len ranges from 1 up to maxLen,
            // as long as it stays inside the string.
            for (int len = 1; len <= maxLen && end - len >= 0; len++) {
                char ch = s.charAt(end - len);
                int idx = ch - 'a';

                // Include this new character in the current segment.
                freq[idx]++;

                // Update the maximum frequency seen in this segment.
                if (freq[idx] > maxFreq) {
                    maxFreq = freq[idx];
                }

                // Only lengths within [minLen, maxLen] are valid segment sizes.
                if (len >= minLen) {
                    int start = end - len;

                    // If the prefix before this segment is impossible to partition,
                    // we cannot use this transition.
                    if (dp[start] == INF) {
                        continue;
                    }

                    // Cost to normalize this segment into one repeated character:
                    // change every character except the most frequent one.
                    int segmentCost = len - maxFreq;

                    // Try using this segment as the last segment.
                    dp[end] = Math.min(dp[end], dp[start] + segmentCost);
                }
            }
        }

        return dp[n] == INF ? -1 : dp[n];
    }

    /**
     * A helper method that computes the normalization cost of a single segment.
     * This is not required by the optimized DP above, but it is useful for
     * demonstration, testing, and beginner understanding.
     *
     * The cost is:
     * segment length - frequency of the most common character in the segment
     *
     * Example:
     * "abaca"
     * counts: a=3, b=1, c=1
     * max frequency = 3
     * cost = 5 - 3 = 2
     *
     * @param segment the input segment
     * @return the minimum number of character changes needed to make all
     *         characters in the segment equal
     *
     * Time complexity: O(segment.length())
     * Space complexity: O(1), because the alphabet size is fixed at 26
     */
    public int segmentCost(String segment) {
        int[] freq = new int[26];
        int maxFreq = 0;

        for (int i = 0; i < segment.length(); i++) {
            int idx = segment.charAt(i) - 'a';
            freq[idx]++;
            maxFreq = Math.max(maxFreq, freq[idx]);
        }

        return segment.length() - maxFreq;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a few additional sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input sizes used in the demo * maxLen)
     * Space complexity: O(max demo string length)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        String s1 = "abacbc";
        int minLen1 = 2;
        int maxLen1 = 3;
        int result1 = solution.minimumDelay(s1, minLen1, maxLen1);
        System.out.println("Input: s = \"" + s1 + "\", minLen = " + minLen1 + ", maxLen = " + maxLen1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        // Sample 2
        String s2 = "aaabbbcc";
        int minLen2 = 3;
        int maxLen2 = 3;
        int result2 = solution.minimumDelay(s2, minLen2, maxLen2);
        System.out.println("Input: s = \"" + s2 + "\", minLen = " + minLen2 + ", maxLen = " + maxLen2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: -1");
        System.out.println();

        // Additional examples for clarity

        // Entire string can be one segment.
        String s3 = "abaca";
        int minLen3 = 5;
        int maxLen3 = 5;
        int result3 = solution.minimumDelay(s3, minLen3, maxLen3);
        System.out.println("Input: s = \"" + s3 + "\", minLen = " + minLen3 + ", maxLen = " + maxLen3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 2");
        System.out.println();

        // Perfect partition with zero cost.
        String s4 = "aaaabbbb";
        int minLen4 = 4;
        int maxLen4 = 4;
        int result4 = solution.minimumDelay(s4, minLen4, maxLen4);
        System.out.println("Input: s = \"" + s4 + "\", minLen = " + minLen4 + ", maxLen = " + maxLen4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
        System.out.println();

        // Impossible because string length cannot be covered by valid segment sizes.
        String s5 = "abcde";
        int minLen5 = 2;
        int maxLen5 = 2;
        int result5 = solution.minimumDelay(s5, minLen5, maxLen5);
        System.out.println("Input: s = \"" + s5 + "\", minLen = " + minLen5 + ", maxLen = " + maxLen5);
        System.out.println("Output: " + result5);
        System.out.println("Expected: -1");
    }
}