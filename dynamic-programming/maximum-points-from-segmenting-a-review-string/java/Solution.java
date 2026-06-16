import java.util.*;

/*
Problem Title: Maximum Points from Segmenting a Review String

Problem Description:
You are building a text scoring system for customer reviews. A review is represented as a lowercase string s.
You are also given a dictionary of approved phrases, where each phrase has an integer score.
Your task is to split the entire string into a sequence of non-overlapping dictionary phrases so that every
character in s belongs to exactly one chosen phrase. Among all valid segmentations, return the maximum total score.
If it is impossible to segment the full string using only the given phrases, return -1.

Each phrase may be used any number of times as long as it matches a substring exactly. Scores may be positive,
zero, or negative, so a valid solution is not always the one with the fewest segments. This means a greedy choice
is not sufficient; you must consider future decisions when choosing a phrase at a position.

Return only the maximum score, not the actual segmentation.

Constraints:
- 1 <= s.length <= 5000
- 1 <= phrases.length <= 2000
- Sum of lengths of all phrases <= 20000
- 1 <= phrase.length <= 50
- -10^4 <= score <= 10^4
- s and all phrases consist only of lowercase English letters
- Phrase strings in the input are unique

Example 1:
Input:
s = "applepieapple"
phrases = [["apple", 5], ["pie", 3], ["app", 2], ["lepie", 4]]
Output:
13

Explanation:
One optimal segmentation is "apple" + "pie" + "apple", with score 5 + 3 + 5 = 13.
Although "app" matches the prefix, choosing it does not lead to a better full segmentation.

Example 2:
Input:
s = "catsandog"
phrases = [["cat", 4], ["cats", 7], ["and", 3], ["sand", 5], ["dog", 6]]
Output:
-1

Explanation:
No sequence of dictionary phrases can cover the entire string exactly. Even though several prefixes match,
the remaining suffix cannot be fully segmented.

Approach:
We use dynamic programming on suffixes.

Let dp[i] = maximum score obtainable by segmenting the suffix s[i...n-1] completely.
If the suffix cannot be segmented, dp[i] is treated as negative infinity (an impossible state).

Transition:
For every position i, try every dictionary phrase that starts at s[i].
If a phrase matches s starting at i and dp[i + phrase.length] is possible,
then we can update:
dp[i] = max(dp[i], phrase.score + dp[i + phrase.length])

Base case:
dp[n] = 0, because the empty suffix needs no phrases and contributes zero score.

To reduce unnecessary checks, phrases are grouped by their starting character.
Then at position i, we only test phrases whose first character equals s.charAt(i).

This handles negative scores correctly because:
- We must cover the entire string exactly.
- Even if a phrase has negative score, it may still be required to complete a valid segmentation.
*/
public class Solution {

    /**
     * A small helper class representing one dictionary phrase and its score.
     */
    private static class Phrase {
        String text;
        int score;

        Phrase(String text, int score) {
            this.text = text;
            this.score = score;
        }
    }

    /**
     * Computes the maximum total score obtainable by segmenting the entire string into dictionary phrases.
     *
     * Dynamic Programming idea:
     * - dp[i] stores the best score for segmenting the suffix starting at index i.
     * - We fill dp from right to left so that when computing dp[i], all future states dp[j] are already known.
     * - If no valid segmentation exists from i, dp[i] remains "impossible".
     *
     * Optimization:
     * - Group phrases by starting character so that at position i we only test phrases that could possibly match.
     *
     * @param s the review string that must be fully segmented
     * @param phrases a 2D array where each entry is [phraseString, scoreString]
     * @return the maximum total score for a full valid segmentation, or -1 if impossible
     * Time complexity note: O(n * k * L) in the worst case, where n is s.length, k is the number of candidate phrases
     * at a position, and L is phrase length comparison cost; with grouping by first character this is efficient in practice
     * under the given constraints
     * Space complexity note: O(n + total dictionary size)
     */
    public int maxScore(String s, String[][] phrases) {
        int n = s.length();

        // We group phrases by their first character.
        // Since all strings are lowercase English letters, we use an array of 26 lists.
        List<Phrase>[] grouped = buildGroupedPhrases(phrases);

        // We need a very small value to represent "impossible".
        // We do not use Integer.MIN_VALUE directly because adding a score to it could overflow.
        final int NEG_INF = Integer.MIN_VALUE / 4;

        // dp[i] = best score for segmenting s[i..n-1].
        int[] dp = new int[n + 1];
        Arrays.fill(dp, NEG_INF);

        // Base case:
        // The empty suffix can always be segmented with score 0 by choosing nothing.
        dp[n] = 0;

        // Process positions from right to left.
        // This ensures that when we are computing dp[i], all dp[i + len] values are already known.
        for (int i = n - 1; i >= 0; i--) {
            char currentChar = s.charAt(i);

            // Only phrases starting with the same character can match here.
            List<Phrase> candidates = grouped[currentChar - 'a'];

            // Try every candidate phrase.
            for (Phrase phrase : candidates) {
                int len = phrase.text.length();
                int nextIndex = i + len;

                // First, check bounds.
                // If the phrase would go past the end of the string, it cannot match.
                if (nextIndex > n) {
                    continue;
                }

                // Next, check whether the phrase exactly matches the substring starting at i.
                if (!matchesAt(s, i, phrase.text)) {
                    continue;
                }

                // If the remaining suffix after this phrase is impossible to segment,
                // then this choice cannot lead to a valid full segmentation.
                if (dp[nextIndex] == NEG_INF) {
                    continue;
                }

                // Otherwise, this is a valid transition:
                // score of current phrase + best score of the remaining suffix.
                int candidateScore = phrase.score + dp[nextIndex];

                // Keep the best possible score.
                dp[i] = Math.max(dp[i], candidateScore);
            }
        }

        // If dp[0] is still impossible, the whole string cannot be segmented.
        return dp[0] == NEG_INF ? -1 : dp[0];
    }

    /**
     * Builds phrase groups by starting character to reduce unnecessary matching attempts.
     *
     * Example:
     * - All phrases starting with 'a' go into grouped[0]
     * - All phrases starting with 'b' go into grouped[1]
     * - ...
     *
     * @param phrases a 2D array where each entry is [phraseString, scoreString]
     * @return an array of 26 lists, each containing phrases that start with that letter
     * Time complexity note: O(m), where m is the number of phrases
     * Space complexity note: O(m)
     */
    public List<Phrase>[] buildGroupedPhrases(String[][] phrases) {
        @SuppressWarnings("unchecked")
        List<Phrase>[] grouped = new ArrayList[26];

        for (int i = 0; i < 26; i++) {
            grouped[i] = new ArrayList<>();
        }

        for (String[] entry : phrases) {
            String text = entry[0];
            int score = Integer.parseInt(entry[1]);
            grouped[text.charAt(0) - 'a'].add(new Phrase(text, score));
        }

        return grouped;
    }

    /**
     * Checks whether the given phrase matches the string s starting exactly at index start.
     *
     * This method performs a character-by-character comparison.
     *
     * @param s the main string
     * @param start the starting index in s where we want to test the match
     * @param phrase the phrase to compare against s
     * @return true if phrase matches s at index start, otherwise false
     * Time complexity note: O(L), where L is phrase.length()
     * Space complexity note: O(1)
     */
    public boolean matchesAt(String s, int start, String phrase) {
        for (int i = 0; i < phrase.length(); i++) {
            if (s.charAt(start + i) != phrase.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 13
     * - Example 2: -1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity note: Depends on the sample sizes; negligible for demonstration
     * Space complexity note: Negligible beyond the sample input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "applepieapple";
        String[][] phrases1 = {
            {"apple", "5"},
            {"pie", "3"},
            {"app", "2"},
            {"lepie", "4"}
        };
        System.out.println(solution.maxScore(s1, phrases1)); // Expected: 13

        String s2 = "catsandog";
        String[][] phrases2 = {
            {"cat", "4"},
            {"cats", "7"},
            {"and", "3"},
            {"sand", "5"},
            {"dog", "6"}
        };
        System.out.println(solution.maxScore(s2, phrases2)); // Expected: -1

        // Additional small demonstration showing negative scores are handled correctly.
        String s3 = "aaaa";
        String[][] phrases3 = {
            {"aa", "-2"},
            {"a", "-1"}
        };
        // Best full segmentation is "a" + "a" + "a" + "a" = -4
        // "aa" + "aa" also gives -4, so answer is -4.
        System.out.println(solution.maxScore(s3, phrases3)); // Expected: -4
    }
}