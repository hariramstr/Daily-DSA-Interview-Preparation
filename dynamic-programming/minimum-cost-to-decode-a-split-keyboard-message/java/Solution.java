import java.util.*;

/*
 * Minimum Cost to Decode a Split Keyboard Message
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A mobile device recorded a typed message, but its keyboard firmware was corrupted.
 * Instead of storing the intended characters directly, the device stored a string s
 * of lowercase letters representing raw key scan groups.
 *
 * You are also given a dictionary of valid words. Each dictionary word w has:
 *   - a positive integer decodeCost
 *   - a positive integer splitPenalty
 *
 * A word w can decode a contiguous substring of s if that substring can be partitioned
 * into one or more non-empty pieces whose concatenation is exactly w, and every piece
 * is either kept in order or reversed before being matched. If the word is split into
 * k pieces, the total cost contributed by this usage is:
 *
 *   decodeCost + (k - 1) * splitPenalty
 *
 * A word may be reused any number of times.
 *
 * Goal:
 * Decode the entire string s into a sequence of dictionary words with minimum total cost.
 * If impossible, return -1.
 *
 * Constraints:
 *   - 1 <= s.length <= 400
 *   - 1 <= dictionary.length <= 120
 *   - Sum of lengths of all dictionary words <= 2500
 *   - 1 <= word.length <= 40
 *   - 1 <= decodeCost, splitPenalty <= 10^6
 *
 * Important interpretation used in this solution:
 * For a dictionary word w and a target substring t of the same length, we may split w
 * into contiguous pieces. The target substring t must then be exactly the concatenation
 * of those pieces in the same order, where each individual piece may appear either
 * normally or reversed.
 *
 * Example:
 *   w = "tablet"
 *   split as "tab" | "let"
 *   then a valid decoded substring could be:
 *      "tab" + reverse("let") = "tabtel"
 *   or reverse("tab") + "let" = "batlet"
 *   etc.
 *
 * This solution:
 *   1) Precomputes, for every dictionary word and every substring position in s with
 *      matching length, the minimum number of pieces needed for that word to decode
 *      that substring.
 *   2) Uses classic DP over the prefix of s to compute the minimum total cost.
 */
public class Solution {

    /**
     * Simple container for one dictionary entry.
     */
    static class WordInfo {
        String word;
        int decodeCost;
        int splitPenalty;

        WordInfo(String word, int decodeCost, int splitPenalty) {
            this.word = word;
            this.decodeCost = decodeCost;
            this.splitPenalty = splitPenalty;
        }
    }

    /**
     * Solves the problem for a given raw string and dictionary.
     *
     * The dictionary format is:
     *   dictionary[i][0] = word
     *   dictionary[i][1] = decodeCost as string
     *   dictionary[i][2] = splitPenalty as string
     *
     * High-level algorithm:
     *   - Let dp[i] = minimum cost to decode prefix s[0..i-1]
     *   - For each position i, try every dictionary word w
     *   - If w can decode substring s[i..i+len(w)-1], determine the minimum number of
     *     pieces k needed
     *   - Transition:
     *       dp[i + len(w)] = min(dp[i + len(w)],
     *                            dp[i] + decodeCost + (k - 1) * splitPenalty)
     *
     * The key subproblem is computing the minimum number of pieces needed for one word
     * to decode one substring. We solve that with dynamic programming on prefixes of
     * the word/substring:
     *
     *   pieceDp[p] = minimum number of pieces to decode the first p characters
     *
     * Transition:
     *   For every next segment [p, q):
     *     if word[p..q) equals target[p..q) OR reverse(word[p..q)) equals target[p..q),
     *     then pieceDp[q] = min(pieceDp[q], pieceDp[p] + 1)
     *
     * Because the word and target substring must preserve piece order, both indices
     * advance together.
     *
     * @param s the recorded raw string
     * @param dictionary the dictionary entries as {word, decodeCost, splitPenalty}
     * @return the minimum total decoding cost, or -1 if impossible
     * Time complexity:
     *   O(n * totalWordLength * L^2) in practical bounded form, where
     *   n <= 400 and L <= 40.
     *   More precisely, for each word and each valid start position, we run an
     *   O(L^3) check in the straightforward implementation below, but with L <= 40
     *   and total dictionary length <= 2500, this is fully acceptable.
     * Space complexity:
     *   O(n + totalMatches) for DP and precomputed match costs, plus O(L) temporary
     *   space per check.
     */
    public long minimumDecodeCost(String s, String[][] dictionary) {
        List<WordInfo> words = new ArrayList<>();
        for (String[] entry : dictionary) {
            words.add(new WordInfo(entry[0], Integer.parseInt(entry[1]), Integer.parseInt(entry[2])));
        }

        int n = s.length();
        long INF = Long.MAX_VALUE / 4;

        /*
         * matchCost[start] will store all possible transitions that begin at "start".
         * Each transition says:
         *   - it ends at "end"
         *   - it costs "cost"
         *
         * We precompute these transitions so the final DP becomes very clean.
         */
        List<long[]>[] transitions = new ArrayList[n + 1];
        for (int i = 0; i <= n; i++) {
            transitions[i] = new ArrayList<>();
        }

        /*
         * For every dictionary word, try every substring of s having the same length.
         * If the word can decode that substring, compute the minimum number of pieces,
         * then convert that into a monetary cost and store the transition.
         */
        for (WordInfo info : words) {
            int len = info.word.length();
            if (len > n) {
                continue;
            }

            for (int start = 0; start + len <= n; start++) {
                String target = s.substring(start, start + len);

                int minPieces = minimumPiecesToDecode(info.word, target);
                if (minPieces != Integer.MAX_VALUE) {
                    long cost = (long) info.decodeCost + (long) (minPieces - 1) * info.splitPenalty;
                    transitions[start].add(new long[]{start + len, cost});
                }
            }
        }

        /*
         * Standard prefix DP:
         *   dp[i] = minimum cost to decode s[0..i-1]
         */
        long[] dp = new long[n + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0;

        for (int i = 0; i < n; i++) {
            if (dp[i] == INF) {
                continue;
            }

            for (long[] tr : transitions[i]) {
                int end = (int) tr[0];
                long cost = tr[1];
                dp[end] = Math.min(dp[end], dp[i] + cost);
            }
        }

        return dp[n] == INF ? -1 : dp[n];
    }

    /**
     * Computes the minimum number of pieces needed so that the given word can decode
     * the given target substring.
     *
     * Both strings must have the same length. We split the word into contiguous pieces.
     * For each piece, the corresponding target piece must be either:
     *   - exactly the same as the word piece, or
     *   - exactly the reverse of the word piece
     *
     * The order of pieces is preserved.
     *
     * Example:
     *   word   = "abcd"
     *   target = "abdc"
     *
     * One valid split is "ab" | "cd"
     *   piece 1: "ab" -> "ab" (kept)
     *   piece 2: "cd" -> "dc" (reversed)
     * Therefore the minimum number of pieces is 2.
     *
     * This method uses DP on prefix length:
     *   dp[i] = minimum number of pieces needed to decode the first i characters
     *
     * Transition:
     *   from prefix i, try every non-empty next piece ending at j
     *   if word[i..j) matches target[i..j) directly or reversed,
     *   then dp[j] = min(dp[j], dp[i] + 1)
     *
     * @param word the dictionary word
     * @param target the substring of s with the same length as word
     * @return the minimum number of pieces, or Integer.MAX_VALUE if impossible
     * Time complexity:
     *   O(L^3) in this direct beginner-friendly implementation, where L = word.length()
     * Space complexity:
     *   O(L)
     */
    public int minimumPiecesToDecode(String word, String target) {
        int m = word.length();
        if (m != target.length()) {
            return Integer.MAX_VALUE;
        }

        int INF = Integer.MAX_VALUE / 4;
        int[] dp = new int[m + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0;

        /*
         * We build the answer from left to right.
         * If dp[i] is reachable, then we try to choose the next piece as any segment
         * word[i..j), where j > i.
         */
        for (int i = 0; i < m; i++) {
            if (dp[i] == INF) {
                continue;
            }

            for (int j = i + 1; j <= m; j++) {
                /*
                 * Check whether the segment word[i..j) can produce target[i..j)
                 * either directly or by reversal.
                 */
                if (segmentMatches(word, target, i, j)) {
                    dp[j] = Math.min(dp[j], dp[i] + 1);
                }
            }
        }

        return dp[m] == INF ? Integer.MAX_VALUE : dp[m];
    }

    /**
     * Checks whether the segment word[left..right) can match target[left..right)
     * either:
     *   1) in normal order, or
     *   2) in reversed order
     *
     * This method compares characters directly without creating extra substring objects.
     *
     * @param word the source dictionary word
     * @param target the target substring
     * @param left inclusive left boundary
     * @param right exclusive right boundary
     * @return true if the segment matches directly or reversed; false otherwise
     * Time complexity:
     *   O(right - left)
     * Space complexity:
     *   O(1)
     */
    public boolean segmentMatches(String word, String target, int left, int right) {
        boolean same = true;
        for (int a = left; a < right; a++) {
            if (word.charAt(a) != target.charAt(a)) {
                same = false;
                break;
            }
        }
        if (same) {
            return true;
        }

        int len = right - left;
        for (int k = 0; k < len; k++) {
            if (word.charAt(left + k) != target.charAt(right - 1 - k)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convenience wrapper that accepts a more interview-friendly dictionary structure:
     * each entry is a list [word, decodeCost, splitPenalty].
     *
     * @param s the recorded raw string
     * @param dictionaryList dictionary entries as lists of strings
     * @return the minimum total decoding cost, or -1 if impossible
     * Time complexity:
     *   Same as minimumDecodeCost(String, String[][])
     * Space complexity:
     *   Same as minimumDecodeCost(String, String[][])
     */
    public long minimumDecodeCost(String s, List<List<String>> dictionaryList) {
        String[][] arr = new String[dictionaryList.size()][3];
        for (int i = 0; i < dictionaryList.size(); i++) {
            List<String> row = dictionaryList.get(i);
            arr[i][0] = row.get(0);
            arr[i][1] = row.get(1);
            arr[i][2] = row.get(2);
        }
        return minimumDecodeCost(s, arr);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The textual examples in the prompt contain inconsistencies in their explanation.
     * Under the formal rule implemented here:
     *   - "abcd" CAN decode "abdc" by splitting into "ab" | "cd", with the second piece reversed.
     *
     * Therefore, to remain correctness-first, this main method prints the actual results
     * produced by the formal interpretation used by the algorithm.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity:
     *   Depends on the sample sizes; negligible for demonstration.
     * Space complexity:
     *   Negligible beyond the solver's normal usage.
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        String s1 = "tabeltcode";
        String[][] dict1 = {
                {"tablet", "5", "2"},
                {"code", "3", "1"},
                {"tab", "4", "1"},
                {"let", "2", "1"}
        };
        System.out.println(sol.minimumDecodeCost(s1, dict1));

        String s2 = "abdc";
        String[][] dict2 = {
                {"abcd", "6", "5"},
                {"ab", "2", "1"},
                {"cd", "2", "1"}
        };
        System.out.println(sol.minimumDecodeCost(s2, dict2));

        String s3 = "abdc";
        String[][] dict3 = {
                {"ab", "2", "1"},
                {"cd", "2", "1"}
        };
        System.out.println(sol.minimumDecodeCost(s3, dict3));
    }
}