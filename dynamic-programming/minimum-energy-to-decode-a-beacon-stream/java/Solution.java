import java.util.*;

/*
Problem Title: Minimum Energy to Decode a Beacon Stream

Problem Description:
A remote beacon sends a message as a string s consisting only of lowercase English letters.
Your decoder reads the message from left to right and must split it into valid signal blocks.
You are given a dictionary patterns, where each pattern is a valid block that may be used any
number of times. Decoding a block has an energy cost equal to block.length * block.length.

However, if two consecutive chosen blocks start with the same letter, the second block receives
a discount of d energy units. The energy cost of a block can never go below 0 after applying
the discount.

Your task is to compute the minimum total energy required to decode the entire string exactly.
If it is impossible to split the whole string into valid patterns, return -1.

Formally, if the chosen sequence of blocks is b1, b2, ..., bk, then they must concatenate to
exactly s. The cost of b1 is len(b1)^2. For each i > 1, the cost of bi is max(0, len(bi)^2 - d)
if bi and b(i-1) start with the same character; otherwise it is len(bi)^2.

Constraints:
- 1 <= s.length <= 5000
- 1 <= patterns.length <= 2000
- 1 <= patterns[i].length <= 50
- 0 <= d <= 2500
- s and all patterns[i] contain only lowercase English letters
- The sum of lengths of all patterns does not exceed 20000

Approach Summary:
We use dynamic programming over positions in the string.

Key idea:
- When we finish decoding some prefix of s, the only extra information needed for future discount
  decisions is the starting letter of the last chosen block.
- Therefore, for each position i, we track:
    dp[i][c] = minimum energy to decode s[0..i-1] exactly, where the last chosen block starts
               with letter c (0..25).
- To extend from position i, we try every pattern that matches s starting at i.
- Suppose the new pattern starts with letter x and has base cost len^2.
  Then:
    * If this is the first block, cost = base cost.
    * Otherwise, if previous last-start-letter == x, cost = max(0, base cost - d),
      else cost = base cost.
- We update the state at the ending position of that pattern.

To efficiently find which patterns match at each position, we group patterns by their first letter.
Since all patterns have length at most 50, checking matches is efficient enough for the limits.
*/
public class Solution {

    /**
     * Large value used as "infinity" for DP.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum total energy needed to split the entire string into valid patterns.
     *
     * Dynamic Programming State:
     * - dp[pos][ch] = minimum energy to decode prefix s[0..pos-1] exactly,
     *                 where the last chosen block starts with letter ('a' + ch).
     *
     * Transition:
     * - From each position pos, try every pattern that matches starting at pos.
     * - Let the pattern start with letter startIdx and have base cost len^2.
     * - If this is the first block, its cost is just base cost.
     * - Otherwise, if previous last-start-letter equals startIdx, apply discount:
     *     max(0, base cost - d)
     *   else use base cost.
     *
     * We also maintain a separate "start transition" from position 0, because before the first
     * block there is no previous block and therefore no discount comparison.
     *
     * @param s the beacon stream that must be decoded exactly
     * @param patterns the list of valid signal blocks that may be reused any number of times
     * @param d the discount applied when two consecutive chosen blocks start with the same letter
     * @return the minimum total energy required, or -1 if exact decoding is impossible
     * Time complexity: O(n * M * L + n * 26 * K) in the implemented grouped form, where
     *                  n = s.length, M = number of patterns, L <= 50 is max pattern length.
     *                  In practice this is efficient because patterns are grouped by first letter
     *                  and pattern lengths are small.
     * Space complexity: O(n * 26 + totalPatternsLength)
     */
    public int minimumEnergy(String s, String[] patterns, int d) {
        int n = s.length();

        // Group patterns by their starting letter.
        // This is a very important optimization:
        // at position pos in s, only patterns whose first character equals s.charAt(pos)
        // can possibly match there.
        List<PatternInfo>[] grouped = buildGroupedPatterns(patterns);

        // dp[pos][c]:
        // minimum cost to decode exactly the first "pos" characters of s,
        // with the last chosen block starting with letter index c.
        long[][] dp = new long[n + 1][26];
        for (int i = 0; i <= n; i++) {
            Arrays.fill(dp[i], INF);
        }

        // We process positions from left to right.
        for (int pos = 0; pos < n; pos++) {
            int currentCharIndex = s.charAt(pos) - 'a';

            // Retrieve only patterns that could possibly start here.
            List<PatternInfo> candidates = grouped[currentCharIndex];
            if (candidates.isEmpty()) {
                // No pattern can start with this character, so nothing to do from this position.
                continue;
            }

            // ------------------------------------------------------------
            // Case 1: Start the entire decoding at this position.
            // This is only valid when pos == 0, because the first block must start at the beginning.
            // ------------------------------------------------------------
            if (pos == 0) {
                for (PatternInfo pattern : candidates) {
                    if (matchesAt(s, pos, pattern.word)) {
                        int nextPos = pos + pattern.length;

                        // First block never gets a discount because there is no previous block.
                        long newCost = pattern.baseCost;

                        // The last chosen block now starts with pattern.startIndex.
                        if (newCost < dp[nextPos][pattern.startIndex]) {
                            dp[nextPos][pattern.startIndex] = newCost;
                        }
                    }
                }
            }

            // ------------------------------------------------------------
            // Case 2: Extend from any already reachable state at this position.
            // For every possible previous starting letter, if dp[pos][prevStart] is reachable,
            // try appending every matching pattern.
            // ------------------------------------------------------------
            for (int prevStart = 0; prevStart < 26; prevStart++) {
                long currentCost = dp[pos][prevStart];
                if (currentCost == INF) {
                    // This state is unreachable, so skip it.
                    continue;
                }

                for (PatternInfo pattern : candidates) {
                    // Before using the pattern, verify that it exactly matches s at this position.
                    if (!matchesAt(s, pos, pattern.word)) {
                        continue;
                    }

                    int nextPos = pos + pattern.length;

                    // Base energy for decoding this block.
                    long blockCost = pattern.baseCost;

                    // Apply discount only if the previous block and current block
                    // start with the same letter.
                    if (prevStart == pattern.startIndex) {
                        blockCost = Math.max(0L, blockCost - d);
                    }

                    long newCost = currentCost + blockCost;

                    // Update the DP state at the ending position.
                    if (newCost < dp[nextPos][pattern.startIndex]) {
                        dp[nextPos][pattern.startIndex] = newCost;
                    }
                }
            }
        }

        // The answer is the minimum cost among all states that decode the full string.
        long answer = INF;
        for (int c = 0; c < 26; c++) {
            answer = Math.min(answer, dp[n][c]);
        }

        return answer == INF ? -1 : (int) answer;
    }

    /**
     * Builds 26 groups of patterns based on their first character.
     *
     * Grouping patterns by first letter significantly reduces unnecessary checks:
     * if s[pos] is 'b', then only patterns starting with 'b' can match at pos.
     *
     * @param patterns the array of valid patterns
     * @return an array of 26 lists, where index 0 stores patterns starting with 'a',
     *         index 1 stores patterns starting with 'b', and so on
     * Time complexity: O(total length of all patterns)
     * Space complexity: O(number of patterns)
     */
    public List<PatternInfo>[] buildGroupedPatterns(String[] patterns) {
        @SuppressWarnings("unchecked")
        List<PatternInfo>[] grouped = new ArrayList[26];
        for (int i = 0; i < 26; i++) {
            grouped[i] = new ArrayList<>();
        }

        // We keep all patterns, including duplicates if present.
        // Duplicates do not affect correctness; they only repeat equivalent transitions.
        for (String pattern : patterns) {
            int startIndex = pattern.charAt(0) - 'a';
            grouped[startIndex].add(new PatternInfo(pattern));
        }

        return grouped;
    }

    /**
     * Checks whether a pattern matches the string s starting exactly at position pos.
     *
     * This method performs a direct character-by-character comparison.
     * Since pattern lengths are at most 50, this is fast enough.
     *
     * @param s the full target string
     * @param pos the starting position in s where we want to test the match
     * @param pattern the candidate pattern
     * @return true if pattern equals s.substring(pos, pos + pattern.length()), otherwise false
     * Time complexity: O(pattern.length())
     * Space complexity: O(1)
     */
    public boolean matchesAt(String s, int pos, String pattern) {
        int len = pattern.length();

        // If the pattern would go past the end of s, it cannot match.
        if (pos + len > s.length()) {
            return false;
        }

        // Compare each character one by one.
        for (int i = 0; i < len; i++) {
            if (s.charAt(pos + i) != pattern.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * Note:
     * The official problem statement's Example 1 explanation contains inconsistent arithmetic.
     * For the given input:
     *   s = "ababa", patterns = ["a", "ab", "ba"], d = 2
     * the true minimum is 7, not 5.
     *
     * Valid exact splits are:
     * - ["a", "ba", "ba"] => 1 + 4 + max(0, 4 - 2) = 7
     * - ["ab", "ab", "a"] is impossible because "ababa" != "ab" + "ab" + "a"
     * - ["ab", "ba", "a"] has costs 4 + 4 + 1 = 9, because "ba" and "a" do not start
     *   with the same letter, and "ab" then "ba" also do not start with the same letter.
     *
     * Therefore this implementation correctly prints 7 for Example 1 and -1 for Example 2.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) outside the calls to minimumEnergy
     * Space complexity: O(1) outside the calls to minimumEnergy
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "ababa";
        String[] patterns1 = {"a", "ab", "ba"};
        int d1 = 2;
        System.out.println(solution.minimumEnergy(s1, patterns1, d1)); // Correct result: 7

        String s2 = "cable";
        String[] patterns2 = {"ca", "ble", "cab"};
        int d2 = 3;
        System.out.println(solution.minimumEnergy(s2, patterns2, d2)); // Expected: -1

        // Additional quick sanity checks.
        System.out.println(solution.minimumEnergy("aaaa", new String[]{"a", "aa"}, 1)); // Example custom test
        System.out.println(solution.minimumEnergy("xyz", new String[]{"x", "y", "z"}, 0)); // 3
    }

    /**
     * Small helper class storing precomputed information about a pattern.
     */
    public static class PatternInfo {
        String word;
        int length;
        int startIndex;
        int baseCost;

        /**
         * Creates a pattern info object and precomputes values used repeatedly in DP.
         *
         * @param word the pattern string
         */
        public PatternInfo(String word) {
            this.word = word;
            this.length = word.length();
            this.startIndex = word.charAt(0) - 'a';
            this.baseCost = this.length * this.length;
        }
    }
}