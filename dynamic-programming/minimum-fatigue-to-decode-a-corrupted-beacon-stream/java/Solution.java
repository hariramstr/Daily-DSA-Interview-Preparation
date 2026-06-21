import java.util.*;

/*
Minimum Fatigue to Decode a Corrupted Beacon Stream

Problem Description:
A satellite receives a long beacon stream represented by a string s of length n, where each
character is a lowercase English letter. Due to interference, the stream may contain corruption.
You are also given a dictionary of valid beacon codes, where each code is a lowercase string
with an associated non-negative fatigue cost.

You may decode the stream by partitioning s into one or more contiguous pieces. For each piece,
you must choose exactly one dictionary code of the same length and pay a cost equal to:

    fatigue(code) + mismatch_count(piece, code)

where mismatch_count is the number of positions at which the two strings differ.

Your goal is to decode the entire stream with minimum total fatigue.

Return the minimum possible total fatigue, or -1 if the stream cannot be fully partitioned into
lengths that exist in the dictionary.

Constraints:
- 1 <= n <= 5000
- 1 <= dictionary.length <= 2000
- 1 <= code.length <= 50
- Sum of all dictionary code lengths <= 50000
- 0 <= fatigue(code) <= 10^6
- s and every code consist only of lowercase English letters

Examples:
1)
s = "abxdab"
dictionary = [["ab",1],["ax",2],["xd",1],["dab",3],["zz",0]]
Output: 3

2)
s = "abcde"
dictionary = [["ab",4],["c",2],["de",1],["xyz",0]]
Output: 7
*/

public class Solution {

    /**
     * Simple container for one dictionary entry.
     */
    static class WordCost {
        String word;
        int cost;

        WordCost(String word, int cost) {
            this.word = word;
            this.cost = cost;
        }
    }

    /**
     * Computes the minimum total fatigue needed to decode the full stream.
     *
     * Core idea:
     * 1. Group dictionary words by their length, because a substring can only be matched
     *    with dictionary words of the same length.
     * 2. Use dynamic programming on prefixes:
     *      dp[i] = minimum fatigue to decode the first i characters of s
     * 3. For every ending position i, try every dictionary length L that can end at i.
     *    If dp[i - L] is reachable, then compute the cheapest dictionary word of length L
     *    to match substring s[i-L .. i-1], where cost is:
     *         fatigue(word) + mismatch_count(substring, word)
     *    Then update dp[i].
     *
     * This is correct because every valid full decoding ends with exactly one final piece,
     * and the optimal cost for the prefix before that piece is exactly dp[i - L].
     *
     * @param s the corrupted beacon stream
     * @param dictionary a list of pairs [code, fatigue], represented as String[] where
     *                   index 0 is the code and index 1 is the fatigue as a decimal string
     * @return the minimum possible total fatigue, or -1 if full decoding is impossible
     * Time complexity: O(n * totalDictionaryCharacters), more precisely
     *                  O(n * sum(length of all dictionary words)),
     *                  which is at most about 2.5 * 10^8 primitive character comparisons
     *                  under the given constraints and is acceptable in Java with small max word length
     * Space complexity: O(n + total dictionary size)
     */
    public int minimumFatigue(String s, String[][] dictionary) {
        Map<Integer, List<WordCost>> byLength = buildDictionaryByLength(dictionary);
        return minimumFatigueInternal(s, byLength);
    }

    /**
     * Overloaded convenience method that accepts dictionary costs as int arrays.
     *
     * @param s the corrupted beacon stream
     * @param words array of dictionary words
     * @param costs array of fatigue costs corresponding to words
     * @return the minimum possible total fatigue, or -1 if full decoding is impossible
     * Time complexity: O(n * sum(length of all dictionary words))
     * Space complexity: O(n + total dictionary size)
     */
    public int minimumFatigue(String s, String[] words, int[] costs) {
        if (words == null || costs == null || words.length != costs.length) {
            throw new IllegalArgumentException("words and costs must be non-null and have the same length");
        }

        String[][] dictionary = new String[words.length][2];
        for (int i = 0; i < words.length; i++) {
            dictionary[i][0] = words[i];
            dictionary[i][1] = String.valueOf(costs[i]);
        }
        return minimumFatigue(s, dictionary);
    }

    /**
     * Builds a map from word length to all dictionary entries having that length.
     *
     * Grouping by length is essential because when we consider a substring of length L,
     * only dictionary words of length L are legal candidates.
     *
     * @param dictionary dictionary entries as [word, costString]
     * @return map from length to list of WordCost objects
     * Time complexity: O(m), ignoring string parsing cost, where m is number of dictionary entries
     * Space complexity: O(m)
     */
    public Map<Integer, List<WordCost>> buildDictionaryByLength(String[][] dictionary) {
        Map<Integer, List<WordCost>> byLength = new HashMap<>();

        for (String[] entry : dictionary) {
            if (entry == null || entry.length != 2) {
                throw new IllegalArgumentException("Each dictionary entry must have exactly 2 elements: [word, cost]");
            }

            String word = entry[0];
            int cost = Integer.parseInt(entry[1]);

            byLength.computeIfAbsent(word.length(), k -> new ArrayList<>()).add(new WordCost(word, cost));
        }

        return byLength;
    }

    /**
     * Internal dynamic programming solver using a pre-grouped dictionary.
     *
     * Detailed DP meaning:
     * - dp[i] stores the minimum cost to decode s[0..i-1]
     * - dp[0] = 0 because the empty prefix costs nothing
     * - For each i from 1 to n:
     *      For each available dictionary length L:
     *          If L <= i and dp[i-L] is reachable:
     *              Let piece = s[i-L .. i-1]
     *              Find the cheapest way to match this piece using any dictionary word of length L
     *              Then:
     *                  dp[i] = min(dp[i], dp[i-L] + bestMatchCost(piece))
     *
     * If dp[n] remains unreachable, return -1.
     *
     * @param s the corrupted beacon stream
     * @param byLength dictionary grouped by word length
     * @return minimum total fatigue, or -1 if impossible
     * Time complexity: O(n * sum(length of all dictionary words))
     * Space complexity: O(n)
     */
    public int minimumFatigueInternal(String s, Map<Integer, List<WordCost>> byLength) {
        int n = s.length();

        // We use a large long value to represent "unreachable".
        // long is safer than int because costs can accumulate over many pieces.
        long INF = Long.MAX_VALUE / 4;

        // dp[i] = minimum cost to decode first i characters.
        long[] dp = new long[n + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0;

        // Convert the input string once to a char array.
        // This avoids repeated charAt calls and makes inner loops faster and simpler.
        char[] source = s.toCharArray();

        // Extract and sort all available lengths.
        // Sorting is not required for correctness, but it makes the iteration deterministic
        // and slightly easier to reason about when reading the code.
        List<Integer> lengths = new ArrayList<>(byLength.keySet());
        Collections.sort(lengths);

        // Process prefixes from left to right.
        for (int end = 1; end <= n; end++) {

            // Try every possible dictionary word length.
            for (int len : lengths) {

                // If the current length is larger than the current prefix size,
                // then no substring of this length can end at position 'end'.
                if (len > end) {
                    break;
                }

                int start = end - len;

                // If the prefix before this piece is unreachable,
                // then we cannot form a valid partition ending here with this length.
                if (dp[start] == INF) {
                    continue;
                }

                // Compute the cheapest possible cost to match substring s[start..end-1]
                // with any dictionary word of this exact length.
                long pieceCost = bestMatchCost(source, start, byLength.get(len));

                // Combine:
                // cost to decode prefix before the piece + cost of decoding this piece
                long candidate = dp[start] + pieceCost;

                if (candidate < dp[end]) {
                    dp[end] = candidate;
                }
            }
        }

        return dp[n] >= INF ? -1 : (int) dp[n];
    }

    /**
     * Finds the minimum cost to decode one fixed substring against all dictionary words
     * of the same length.
     *
     * For each candidate word:
     *   total cost = fatigue(word) + number of mismatched positions
     * We return the minimum such value.
     *
     * Important detail:
     * The substring is not physically created as a new String. Instead, we compare directly
     * against the source char array using the given start index. This avoids unnecessary
     * object creation and improves performance.
     *
     * @param source the full source string as a char array
     * @param start the starting index of the substring in source
     * @param candidates all dictionary words having the same required length
     * @return the minimum matching cost for this substring
     * Time complexity: O(k * L), where k is number of candidate words of this length and L is the length
     * Space complexity: O(1)
     */
    public long bestMatchCost(char[] source, int start, List<WordCost> candidates) {
        long best = Long.MAX_VALUE / 4;

        // Every candidate in this list has the same length.
        for (WordCost candidate : candidates) {
            String word = candidate.word;
            int mismatches = 0;

            // Compare character by character.
            // Each differing position contributes 1 to the mismatch count.
            for (int i = 0; i < word.length(); i++) {
                if (source[start + i] != word.charAt(i)) {
                    mismatches++;
                }
            }

            long total = (long) candidate.cost + mismatches;
            if (total < best) {
                best = total;
            }
        }

        return best;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 3
     * Example 2 -> 7
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total work of the demonstrated calls)
     * Space complexity: O(space used by the demonstrated calls)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "abxdab";
        String[][] dictionary1 = {
                {"ab", "1"},
                {"ax", "2"},
                {"xd", "1"},
                {"dab", "3"},
                {"zz", "0"}
        };
        int result1 = solution.minimumFatigue(s1, dictionary1);
        System.out.println(result1); // Expected: 3

        String s2 = "abcde";
        String[][] dictionary2 = {
                {"ab", "4"},
                {"c", "2"},
                {"de", "1"},
                {"xyz", "0"}
        };
        int result2 = solution.minimumFatigue(s2, dictionary2);
        System.out.println(result2); // Expected: 7
    }
}