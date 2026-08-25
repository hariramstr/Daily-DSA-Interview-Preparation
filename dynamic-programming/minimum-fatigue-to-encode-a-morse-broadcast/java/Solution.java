import java.util.*;

/*
 * Minimum Fatigue to Encode a Morse Broadcast
 *
 * Problem Description:
 * A rescue team needs to send a long emergency message using a custom telegraph key.
 * The message is represented as a string s consisting only of lowercase English letters.
 * Each letter must be encoded using standard Morse code.
 *
 * Pressing the key has a fatigue cost:
 * - If two consecutive Morse symbols in the final transmitted stream are the same,
 *   the second symbol costs sameCost fatigue.
 * - Otherwise, it costs switchCost fatigue.
 * - The very first symbol of the entire transmission always costs startCost,
 *   regardless of whether it is '.' or '-'.
 *
 * Before transmitting, you may partition the original string into any number of non-empty
 * contiguous groups. For each group, you are allowed to reverse the order of letters inside
 * that group exactly once or leave it unchanged. After choosing orientations for all groups,
 * concatenate the groups in their original group order and transmit the resulting Morse stream.
 *
 * Your task is to compute the minimum possible total fatigue.
 *
 * In other words, you may cut the string into segments, optionally reverse each segment,
 * and then encode the resulting letter order into Morse code. The Morse symbols themselves
 * may not be changed, only the order of letters through segment reversals.
 *
 * Constraints:
 * - 1 <= s.length <= 300
 * - 1 <= startCost, sameCost, switchCost <= 10^6
 * - s contains only lowercase English letters
 * - Standard Morse code mapping for the 26 lowercase letters must be used
 *
 * Key Insight:
 * Reversing arbitrary contiguous groups and then concatenating them in original group order
 * allows us to build the final letter order as a sequence of blocks, where each block is either:
 * - the substring in forward order, or
 * - the substring in reverse order.
 *
 * Therefore, dynamic programming over prefixes is natural:
 * - choose the last block [j..i]
 * - decide whether it is used forward or reversed
 * - combine its Morse-stream cost with the best answer for prefix ending at j - 1
 * - carefully account for the Morse-symbol transition across the boundary between blocks
 *
 * We precompute, for every substring and both orientations:
 * - total internal Morse cost assuming the first Morse symbol of the block is already paid
 * - first Morse symbol of the block
 * - last Morse symbol of the block
 *
 * Then DP joins blocks in O(1) per transition.
 */
public class Solution {

    /**
     * Standard Morse code mapping for lowercase English letters.
     */
    private static final String[] MORSE = {
            ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
            "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-",
            "..-", "...-", ".--", "-..-", "-.--", "--.."
    };

    /**
     * A large value used as infinity in dynamic programming.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Stores precomputed information for one substring in one orientation.
     *
     * internalCost:
     *   Cost of transmitting the whole Morse stream of this block EXCLUDING the cost of the
     *   very first Morse symbol of the block.
     *
     * first:
     *   First Morse symbol of the block, encoded as:
     *   0 -> '.'
     *   1 -> '-'
     *
     * last:
     *   Last Morse symbol of the block, encoded the same way.
     */
    private static class BlockInfo {
        long internalCost;
        int first;
        int last;

        BlockInfo(long internalCost, int first, int last) {
            this.internalCost = internalCost;
            this.first = first;
            this.last = last;
        }
    }

    /**
     * Computes the minimum possible fatigue to transmit the message after optimally partitioning
     * it into contiguous groups and optionally reversing each group.
     *
     * Dynamic programming idea:
     * dp[i][t] = minimum cost to transmit letters s[0..i], where the last transmitted Morse symbol
     *            of the entire prefix is t (0 for '.', 1 for '-').
     *
     * Transition:
     * - Choose the last block as substring s[j..i].
     * - Use it either forward or reversed.
     * - If j == 0, this block starts the whole transmission:
     *     cost = startCost + block.internalCost
     * - Otherwise, append it after an already solved prefix s[0..j-1]:
     *     the first symbol of the block is not a "start", it is just the next symbol after the
     *     previous block's last symbol.
     *     So the boundary contributes:
     *         sameCost   if previousLast == block.first
     *         switchCost otherwise
     *     Then add block.internalCost for the rest of the block.
     *
     * Because block.internalCost excludes the first symbol's cost, this composition is exact.
     *
     * @param s the original lowercase string
     * @param startCost fatigue cost of the very first Morse symbol of the entire transmission
     * @param sameCost fatigue cost when the current Morse symbol equals the previous one
     * @param switchCost fatigue cost when the current Morse symbol differs from the previous one
     * @return the minimum possible total fatigue
     * Time complexity: O(n^2), where n = s.length()
     * Space complexity: O(n^2) for substring precomputation
     */
    public long minimumFatigue(String s, int startCost, int sameCost, int switchCost) {
        int n = s.length();

        // Precompute block information for every substring [l..r] in forward orientation.
        BlockInfo[][] forward = precomputeForwardBlocks(s, sameCost, switchCost);

        // Precompute block information for every substring [l..r] in reversed orientation.
        // Reversed orientation of s[l..r] is exactly forward orientation of reverse(s)[n-1-r .. n-1-l].
        BlockInfo[][] reversed = precomputeReversedBlocks(s, sameCost, switchCost);

        // dp[i][0] = minimum cost for prefix ending at i, with final Morse symbol '.'
        // dp[i][1] = minimum cost for prefix ending at i, with final Morse symbol '-'
        long[][] dp = new long[n][2];
        for (int i = 0; i < n; i++) {
            Arrays.fill(dp[i], INF);
        }

        // Build answers for all prefixes ending at i.
        for (int i = 0; i < n; i++) {

            // Try every possible starting index j of the last block [j..i].
            for (int j = 0; j <= i; j++) {

                // There are two choices for the last block:
                // 1) keep substring s[j..i] in forward order
                // 2) reverse substring s[j..i]
                BlockInfo[] candidates = {forward[j][i], reversed[j][i]};

                for (BlockInfo block : candidates) {
                    if (j == 0) {
                        // This block is the entire transmission.
                        // The first Morse symbol pays startCost.
                        long total = (long) startCost + block.internalCost;
                        dp[i][block.last] = Math.min(dp[i][block.last], total);
                    } else {
                        // Append this block after an already transmitted prefix ending at j - 1.
                        for (int prevLast = 0; prevLast <= 1; prevLast++) {
                            if (dp[j - 1][prevLast] >= INF) {
                                continue;
                            }

                            // Cost of the first Morse symbol of this block when appended after prevLast.
                            long boundaryCost = (prevLast == block.first) ? sameCost : switchCost;

                            // Total = best prefix + boundary first-symbol cost + rest of block.
                            long total = dp[j - 1][prevLast] + boundaryCost + block.internalCost;
                            dp[i][block.last] = Math.min(dp[i][block.last], total);
                        }
                    }
                }
            }
        }

        return Math.min(dp[n - 1][0], dp[n - 1][1]);
    }

    /**
     * Precomputes block information for every substring in forward orientation.
     *
     * For each substring s[l..r], we consider the Morse stream obtained by concatenating
     * Morse encodings of s[l], s[l+1], ..., s[r].
     *
     * The stored internalCost excludes the cost of the first Morse symbol of the block.
     * Every later Morse symbol contributes:
     * - sameCost if it equals the previous Morse symbol
     * - switchCost otherwise
     *
     * This representation is extremely useful because when a block is appended after another block,
     * the first symbol's cost depends on the previous block's last symbol, not on startCost.
     *
     * @param s the original string
     * @param sameCost cost when consecutive Morse symbols are equal
     * @param switchCost cost when consecutive Morse symbols differ
     * @return a 2D table info[l][r] for all substrings in forward orientation
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public BlockInfo[][] precomputeForwardBlocks(String s, int sameCost, int switchCost) {
        int n = s.length();
        BlockInfo[][] info = new BlockInfo[n][n];

        // Convert each character to its Morse string once.
        String[] letterMorse = new String[n];
        for (int i = 0; i < n; i++) {
            letterMorse[i] = MORSE[s.charAt(i) - 'a'];
        }

        // For every starting position l, extend the substring one letter at a time.
        for (int l = 0; l < n; l++) {
            String firstLetter = letterMorse[l];

            // Initialize substring [l..l].
            int first = symbolToBit(firstLetter.charAt(0));
            int last = symbolToBit(firstLetter.charAt(firstLetter.length() - 1));
            long internal = internalCostOfSingleLetter(firstLetter, sameCost, switchCost);

            info[l][l] = new BlockInfo(internal, first, last);

            // Extend to [l..r] for r > l.
            int currentLast = last;
            long currentInternal = internal;
            int currentFirst = first;

            for (int r = l + 1; r < n; r++) {
                String next = letterMorse[r];
                int nextFirst = symbolToBit(next.charAt(0));
                int nextLast = symbolToBit(next.charAt(next.length() - 1));

                // When appending the next letter's Morse string:
                // 1) the first symbol of that letter is compared against currentLast
                currentInternal += (currentLast == nextFirst) ? sameCost : switchCost;

                // 2) the remaining symbols inside that letter contribute its own internal single-letter cost
                currentInternal += internalCostOfSingleLetter(next, sameCost, switchCost);

                currentLast = nextLast;
                info[l][r] = new BlockInfo(currentInternal, currentFirst, currentLast);
            }
        }

        return info;
    }

    /**
     * Precomputes block information for every substring in reversed orientation.
     *
     * If we reverse substring s[l..r], the letter order becomes:
     * s[r], s[r-1], ..., s[l]
     *
     * Instead of building all such streams directly, we use a neat trick:
     * let rev = reverse(s)
     *
     * Then reversed orientation of s[l..r] corresponds exactly to forward orientation of
     * rev[n - 1 - r .. n - 1 - l].
     *
     * So we:
     * 1) precompute forward blocks on reverse(s)
     * 2) map indices back
     *
     * @param s the original string
     * @param sameCost cost when consecutive Morse symbols are equal
     * @param switchCost cost when consecutive Morse symbols differ
     * @return a 2D table info[l][r] for all substrings in reversed orientation
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public BlockInfo[][] precomputeReversedBlocks(String s, int sameCost, int switchCost) {
        int n = s.length();
        String reversedString = new StringBuilder(s).reverse().toString();
        BlockInfo[][] revForward = precomputeForwardBlocks(reversedString, sameCost, switchCost);

        BlockInfo[][] result = new BlockInfo[n][n];
        for (int l = 0; l < n; l++) {
            for (int r = l; r < n; r++) {
                int rl = n - 1 - r;
                int rr = n - 1 - l;
                result[l][r] = revForward[rl][rr];
            }
        }
        return result;
    }

    /**
     * Computes the internal cost of a single letter's Morse code, excluding the first symbol.
     *
     * Example:
     * Morse = "-..."
     * We do NOT pay for the first '-'.
     * We only pay for transitions:
     * '-' -> '.'
     * '.' -> '.'
     * '.' -> '.'
     *
     * @param morse the Morse code string of one letter
     * @param sameCost cost when consecutive Morse symbols are equal
     * @param switchCost cost when consecutive Morse symbols differ
     * @return the cost of all symbols after the first one inside this letter
     * Time complexity: O(length of morse), which is O(1) because Morse letter length is bounded
     * Space complexity: O(1)
     */
    public long internalCostOfSingleLetter(String morse, int sameCost, int switchCost) {
        long cost = 0;
        for (int i = 1; i < morse.length(); i++) {
            cost += (morse.charAt(i) == morse.charAt(i - 1)) ? sameCost : switchCost;
        }
        return cost;
    }

    /**
     * Converts a Morse symbol to a compact integer representation.
     *
     * @param c the Morse symbol, either '.' or '-'
     * @return 0 for '.', 1 for '-'
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int symbolToBit(char c) {
        return c == '.' ? 0 : 1;
    }

    /**
     * Demonstrates the solution on the sample inputs from the statement.
     *
     * Note:
     * The algorithm is the source of truth for correctness. This main method simply prints
     * the computed results so the program is runnable and easy to test.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(1) outside the invoked solver calls
     * Space complexity: O(1) outside the invoked solver calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "cab";
        int startCost1 = 3;
        int sameCost1 = 1;
        int switchCost1 = 4;
        long answer1 = solution.minimumFatigue(s1, startCost1, sameCost1, switchCost1);
        System.out.println(answer1);

        String s2 = "azaz";
        int startCost2 = 2;
        int sameCost2 = 5;
        int switchCost2 = 1;
        long answer2 = solution.minimumFatigue(s2, startCost2, sameCost2, switchCost2);
        System.out.println(answer2);
    }
}