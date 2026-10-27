import java.util.*;

/*
 * Title: Minimum Editing Cost for Subtitle Line Breaks
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A video platform stores a spoken transcript as an array of words. To generate subtitles,
 * the transcript must be split into lines while preserving the original word order.
 * You may place a line break between any two adjacent words, but each subtitle line must
 * contain at most maxWidth characters, including single spaces between consecutive words
 * on the same line.
 *
 * The cost of a line is defined as the square of its unused space:
 *     (maxWidth - usedCharacters)^2
 *
 * However, the final line is displayed without padding penalties, so its cost is 0
 * as long as it fits within maxWidth.
 *
 * Formally, if words i...j are placed on one line, then the line length is:
 *     sum(length(words[k])) for k in [i..j] + (j - i) spaces
 *
 * This line is valid only if that total is less than or equal to maxWidth.
 *
 * Return the minimum possible sum of line costs over all lines.
 *
 * Constraints:
 * - 1 <= words.length <= 500
 * - 1 <= words[i].length <= maxWidth <= 1000
 * - words[i] contains only English letters, digits, or punctuation without spaces
 *
 * Notes about the provided examples:
 * The textual explanations in the prompt contain arithmetic inconsistencies.
 * This implementation follows the formal problem definition exactly:
 * - every non-final line contributes (unusedSpace)^2
 * - the final line contributes 0
 * - line length includes single spaces between adjacent words on the same line
 */

public class Solution {

    /**
     * Computes the minimum total editing cost needed to split the given words into valid subtitle lines.
     *
     * Dynamic programming idea:
     * Let dp[i] represent the minimum cost to format the suffix starting at word index i.
     * For each starting index i, we try to place words i..j on the current line as long as
     * the total line length does not exceed maxWidth.
     *
     * If j is the last word, then this is the final line and its cost is 0.
     * Otherwise, the cost is:
     *     (maxWidth - usedLength)^2 + dp[j + 1]
     *
     * We take the minimum over all valid j.
     *
     * @param words the transcript words in original order
     * @param maxWidth the maximum allowed width of each subtitle line
     * @return the minimum total editing cost
     * Time complexity: O(n^2), where n is the number of words
     * Space complexity: O(n)
     */
    public int minimumEditingCost(String[] words, int maxWidth) {
        int n = words.length;

        // dp[i] = minimum cost to format words from index i to the end.
        // We need one extra slot so that dp[n] = 0 naturally represents
        // "no words left to place".
        long[] dp = new long[n + 1];

        // A very large number used as "infinity" for minimization.
        long INF = Long.MAX_VALUE / 4;

        // Base case:
        // If there are no words left, the cost is 0.
        dp[n] = 0;

        // We fill the DP table from right to left because dp[i] depends on dp[j + 1],
        // where j + 1 is a larger index.
        for (int i = n - 1; i >= 0; i--) {
            dp[i] = INF;

            // currentLineLength will track the exact number of characters used
            // when we place words i..j on the same line.
            //
            // We build this incrementally:
            // - when j == i, length is just words[i].length()
            // - for each next word, we add:
            //     1 for the space before it
            //     + length of that word
            int currentLineLength = 0;

            // Try every possible ending position j for the current line,
            // starting from i and extending rightward until the line becomes too long.
            for (int j = i; j < n; j++) {
                if (j == i) {
                    // First word on the line: no leading space.
                    currentLineLength = words[j].length();
                } else {
                    // Every additional word needs:
                    // - one separating space
                    // - the word's own characters
                    currentLineLength += 1 + words[j].length();
                }

                // If the line is too long, then any larger j will also be too long,
                // because adding more words only increases the length.
                // So we can stop early.
                if (currentLineLength > maxWidth) {
                    break;
                }

                long cost;
                if (j == n - 1) {
                    // If this line reaches the final word, then it is the last line.
                    // By the problem statement, the last line has zero cost.
                    cost = 0;
                } else {
                    // Non-final line:
                    // unused space = maxWidth - currentLineLength
                    // line cost = unused^2
                    long unused = maxWidth - currentLineLength;
                    long lineCost = unused * unused;

                    // Total cost = cost of this line + best cost for remaining suffix.
                    cost = lineCost + dp[j + 1];
                }

                // Keep the best (minimum) arrangement.
                dp[i] = Math.min(dp[i], cost);
            }
        }

        return (int) dp[0];
    }

    /**
     * Reconstructs one optimal line arrangement that achieves the minimum editing cost.
     * This is useful for demonstration and debugging in main.
     *
     * The method uses the same DP idea as minimumEditingCost, but also stores the chosen
     * ending index for each starting position so that the actual lines can be rebuilt.
     *
     * @param words the transcript words in original order
     * @param maxWidth the maximum allowed width of each subtitle line
     * @return a list of strings representing one optimal subtitle formatting
     * Time complexity: O(n^2), where n is the number of words
     * Space complexity: O(n)
     */
    public List<String> reconstructOptimalLines(String[] words, int maxWidth) {
        int n = words.length;
        long[] dp = new long[n + 1];
        int[] nextBreak = new int[n];
        long INF = Long.MAX_VALUE / 4;

        dp[n] = 0;

        for (int i = n - 1; i >= 0; i--) {
            dp[i] = INF;
            nextBreak[i] = i;

            int currentLineLength = 0;

            for (int j = i; j < n; j++) {
                if (j == i) {
                    currentLineLength = words[j].length();
                } else {
                    currentLineLength += 1 + words[j].length();
                }

                if (currentLineLength > maxWidth) {
                    break;
                }

                long cost;
                if (j == n - 1) {
                    cost = 0;
                } else {
                    long unused = maxWidth - currentLineLength;
                    cost = unused * unused + dp[j + 1];
                }

                if (cost < dp[i]) {
                    dp[i] = cost;
                    nextBreak[i] = j;
                }
            }
        }

        List<String> lines = new ArrayList<>();
        int index = 0;

        while (index < n) {
            int end = nextBreak[index];
            StringBuilder line = new StringBuilder();

            for (int k = index; k <= end; k++) {
                if (k > index) {
                    line.append(' ');
                }
                line.append(words[k]);
            }

            lines.add(line.toString());
            index = end + 1;
        }

        return lines;
    }

    /**
     * Computes the exact character length of a line formed by joining words[start..end]
     * with single spaces.
     *
     * @param words the transcript words
     * @param start the starting index of the line
     * @param end the ending index of the line
     * @return the total used character count for that line
     * Time complexity: O(end - start + 1)
     * Space complexity: O(1)
     */
    public int lineLength(String[] words, int start, int end) {
        int length = 0;
        for (int i = start; i <= end; i++) {
            length += words[i].length();
        }
        length += (end - start);
        return length;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations shown here, excluding the DP calls
     * Space complexity: O(1), excluding the DP helper structures
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] words1 = {"AI", "helps", "teams", "ship", "faster"};
        int maxWidth1 = 10;
        int result1 = solution.minimumEditingCost(words1, maxWidth1);
        List<String> lines1 = solution.reconstructOptimalLines(words1, maxWidth1);

        System.out.println("Example 1:");
        System.out.println("Words: " + Arrays.toString(words1));
        System.out.println("maxWidth: " + maxWidth1);
        System.out.println("Minimum editing cost: " + result1);
        System.out.println("One optimal arrangement:");
        for (String line : lines1) {
            System.out.println(line);
        }
        System.out.println();

        String[] words2 = {"code", "review", "needs", "clear", "notes"};
        int maxWidth2 = 12;
        int result2 = solution.minimumEditingCost(words2, maxWidth2);
        List<String> lines2 = solution.reconstructOptimalLines(words2, maxWidth2);

        System.out.println("Example 2:");
        System.out.println("Words: " + Arrays.toString(words2));
        System.out.println("maxWidth: " + maxWidth2);
        System.out.println("Minimum editing cost: " + result2);
        System.out.println("One optimal arrangement:");
        for (String line : lines2) {
            System.out.println(line);
        }
        System.out.println();

        String[] words3 = {"dynamic", "programming", "is", "powerful"};
        int maxWidth3 = 15;
        int result3 = solution.minimumEditingCost(words3, maxWidth3);
        List<String> lines3 = solution.reconstructOptimalLines(words3, maxWidth3);

        System.out.println("Additional Demo:");
        System.out.println("Words: " + Arrays.toString(words3));
        System.out.println("maxWidth: " + maxWidth3);
        System.out.println("Minimum editing cost: " + result3);
        System.out.println("One optimal arrangement:");
        for (String line : lines3) {
            System.out.println(line);
        }
    }
}