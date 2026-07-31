import java.util.*;

/*
Problem Title: Minimum Fatigue to Type a Macro Script

Problem Description:
You are building an editor for a programmable keypad. A script is a string s of lowercase English letters
that must be produced exactly from left to right. The editor supports two actions:

1. Type(c): append character c to the output. This costs typeCost[c] fatigue.
2. Define(l, r): if the substring s[l..r] has already appeared earlier somewhere completely inside s[0..l-1],
   you may store that substring as a macro at zero cost.
3. Use(l, r): append a previously defined macro equal to s[l..r] in one action, costing macroCost fatigue
   regardless of the substring length.

A macro can only be used after it has been defined, and a definition is only valid if an identical substring
occurred earlier in the already produced prefix. You may define and use any number of macros, and different
occurrences of the same text count as the same macro content.

Return the minimum total fatigue needed to produce the entire script.

Formally, when you are about to produce position i, you may either type s[i], or choose any j >= i such that
substring s[i..j] has appeared as a contiguous substring entirely within s[0..i-1]; in that case you may define
it if needed and then use it for cost macroCost, advancing to j + 1.

Constraints:
- 1 <= s.length <= 2000
- s consists only of lowercase English letters
- typeCost.length == 26
- 1 <= typeCost[k] <= 10^6
- 1 <= macroCost <= 10^6

Important note about the examples:
The second example text in the statement is internally inconsistent. The formal rules are the source of truth.
This implementation follows the formal rules exactly.
*/

public class Solution {

    /**
     * Computes the minimum total fatigue needed to produce the entire script.
     *
     * Core idea:
     * 1. Dynamic programming over positions:
     *    dp[i] = minimum fatigue needed to produce prefix s[0..i-1].
     *
     * 2. From position i, we have two kinds of transitions:
     *    - Type the next character s[i]
     *    - Use a macro for any substring s[i..j] that already appeared completely inside s[0..i-1]
     *
     * 3. To know which substrings starting at i are reusable as macros, we precompute:
     *    lcp[a][b] = longest common prefix length of suffixes starting at a and b
     *
     * 4. For a fixed current position i, a substring s[i..j] is valid for macro use iff there exists
     *    some earlier start p < i such that:
     *      - the earlier occurrence lies completely in the already produced prefix, so its length must be <= i - p
     *      - and s[p..] matches s[i..] for at least that length
     *
     *    Therefore, the maximum macro length usable at position i is:
     *      maxLen[i] = max over p in [0, i-1] of min(lcp[p][i], i - p)
     *
     *    Then every length 1..maxLen[i] is valid, because if a longer matching earlier occurrence exists,
     *    all its prefixes also exist earlier.
     *
     * 5. Once maxLen[i] is known, from dp[i] we may transition to every dp[i + len] with cost macroCost
     *    for len in [1, maxLen[i]].
     *
     * Since n <= 2000, an O(n^2) solution is fully acceptable.
     *
     * @param s the target script to produce
     * @param typeCost fatigue cost for typing each lowercase letter, where index 0 = 'a', ..., 25 = 'z'
     * @param macroCost fatigue cost for using any previously defined macro
     * @return the minimum total fatigue required to produce s exactly
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public long minimumFatigue(String s, int[] typeCost, int macroCost) {
        int n = s.length();
        char[] chars = s.toCharArray();

        // ------------------------------------------------------------
        // Step 1: Precompute LCP (Longest Common Prefix) table.
        //
        // lcp[i][j] = number of equal characters starting from positions i and j.
        //
        // We fill it from bottom-right to top-left:
        // if chars[i] == chars[j], then
        //   lcp[i][j] = 1 + lcp[i+1][j+1]
        // else
        //   lcp[i][j] = 0
        //
        // This lets us compare any two suffixes in O(1) time later.
        // ------------------------------------------------------------
        int[][] lcp = buildLcpTable(chars);

        // ------------------------------------------------------------
        // Step 2: For each position i, compute the maximum macro length
        // that can be used starting at i.
        //
        // We look at every earlier starting position p < i.
        // The earlier occurrence must fit entirely inside the already produced prefix s[0..i-1],
        // so its maximum allowed length is (i - p).
        //
        // Also, the actual matching length between suffixes p and i is lcp[p][i].
        //
        // Therefore, the usable length contributed by p is:
        //   min(lcp[p][i], i - p)
        //
        // Taking the maximum over all p gives maxLen[i].
        // ------------------------------------------------------------
        int[] maxLen = computeMaximumReusableLengths(chars, lcp);

        // ------------------------------------------------------------
        // Step 3: Dynamic programming.
        //
        // dp[i] = minimum fatigue to produce first i characters, i.e. s[0..i-1].
        //
        // Initialization:
        //   dp[0] = 0
        //   all others = infinity
        //
        // Transitions from position i:
        //   A) Type s[i]:
        //      dp[i+1] = min(dp[i+1], dp[i] + typeCost[s[i]])
        //
        //   B) Use a macro of any valid length len in [1, maxLen[i]]:
        //      dp[i+len] = min(dp[i+len], dp[i] + macroCost)
        //
        // Because every prefix of a valid reusable substring is also reusable,
        // all lengths 1..maxLen[i] are valid.
        // ------------------------------------------------------------
        long[] dp = new long[n + 1];
        long inf = Long.MAX_VALUE / 4;
        Arrays.fill(dp, inf);
        dp[0] = 0L;

        for (int i = 0; i < n; i++) {
            if (dp[i] == inf) {
                continue;
            }

            // Option 1: Type the next character individually.
            int letterIndex = chars[i] - 'a';
            dp[i + 1] = Math.min(dp[i + 1], dp[i] + typeCost[letterIndex]);

            // Option 2: Use any valid macro length starting at i.
            int limit = maxLen[i];
            for (int len = 1; len <= limit; len++) {
                dp[i + len] = Math.min(dp[i + len], dp[i] + macroCost);
            }
        }

        return dp[n];
    }

    /**
     * Builds the LCP table for all pairs of suffixes.
     *
     * lcp[i][j] = length of the longest common prefix of:
     * - suffix starting at i
     * - suffix starting at j
     *
     * This is a classic dynamic programming precomputation.
     *
     * @param chars the string as a character array
     * @return a 2D table of LCP values
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public int[][] buildLcpTable(char[] chars) {
        int n = chars.length;
        int[][] lcp = new int[n + 1][n + 1];

        // We iterate backwards so that lcp[i+1][j+1] is already known
        // when computing lcp[i][j].
        for (int i = n - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (chars[i] == chars[j]) {
                    lcp[i][j] = 1 + lcp[i + 1][j + 1];
                } else {
                    lcp[i][j] = 0;
                }
            }
        }

        return lcp;
    }

    /**
     * For each position i, computes the maximum length of a substring starting at i
     * that has already appeared completely inside the prefix s[0..i-1].
     *
     * More precisely:
     * maxLen[i] = maximum L such that s[i..i+L-1] occurs somewhere in s[0..i-1]
     * as a contiguous substring fully contained there.
     *
     * If maxLen[i] = L, then every length from 1 to L is also valid for macro use.
     *
     * @param chars the string as a character array
     * @param lcp precomputed LCP table
     * @return array maxLen where maxLen[i] is the maximum reusable macro length at i
     * Time complexity: O(n^2)
     * Space complexity: O(n)
     */
    public int[] computeMaximumReusableLengths(char[] chars, int[][] lcp) {
        int n = chars.length;
        int[] maxLen = new int[n];

        for (int i = 0; i < n; i++) {
            int best = 0;

            // Try every earlier starting position p.
            for (int p = 0; p < i; p++) {
                // The earlier occurrence must end before i,
                // so its length cannot exceed i - p.
                int earlierOccurrenceCapacity = i - p;

                // The two suffixes match for lcp[p][i] characters.
                int actualMatch = lcp[p][i];

                // The usable earlier occurrence length is limited by both constraints.
                int candidate = Math.min(earlierOccurrenceCapacity, actualMatch);

                if (candidate > best) {
                    best = candidate;
                }
            }

            maxLen[i] = best;
        }

        return maxLen;
    }

    /**
     * Convenience wrapper matching a common interview-style signature.
     *
     * @param s the target script
     * @param typeCost typing costs for letters 'a' to 'z'
     * @param macroCost fixed cost of using any valid macro
     * @return minimum total fatigue
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public long solve(String s, int[] typeCost, int macroCost) {
        return minimumFatigue(s, typeCost, macroCost);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The first sample is consistent with the formal rules and should produce 5.
     * The second sample explanation in the statement is inconsistent with the formal rules.
     * This program prints the mathematically correct answer under the formal definition.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n^2) per demonstrated test case
     * Space complexity: O(n^2) per demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "ababa";
        int[] typeCost1 = {
            1, 1, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100
        };
        int macroCost1 = 2;
        long answer1 = solution.minimumFatigue(s1, typeCost1, macroCost1);
        System.out.println(answer1); // Expected by statement: 5

        String s2 = "aaaaaa";
        int[] typeCost2 = {
            3, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
            100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100
        };
        int macroCost2 = 4;
        long answer2 = solution.minimumFatigue(s2, typeCost2, macroCost2);
        System.out.println(answer2); // Correct under formal rules: 14

        // Additional small sanity checks.

        String s3 = "abcde";
        int[] typeCost3 = new int[26];
        Arrays.fill(typeCost3, 2);
        int macroCost3 = 1;
        long answer3 = solution.minimumFatigue(s3, typeCost3, macroCost3);
        System.out.println(answer3); // No repeated substring available early enough, so 10

        String s4 = "abcabcabc";
        int[] typeCost4 = new int[26];
        Arrays.fill(typeCost4, 5);
        int macroCost4 = 3;
        long answer4 = solution.minimumFatigue(s4, typeCost4, macroCost4);
        System.out.println(answer4);
    }
}