import java.util.*;

/*
 * Title: Minimum Font Size to Fit a Banner
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * You are building a layout engine for digital signage. A banner must display a fixed message
 * on a screen with width W and height H. The message is split into words, and words must remain
 * in order. For a chosen integer font size s, each character occupies exactly s units of width
 * and each line occupies exactly s units of height. A word with length L therefore needs L * s width.
 * Adjacent words on the same line must have exactly one space between them, and a space also consumes
 * s width. You may wrap to the next line only between words; a word cannot be split across lines.
 *
 * Return the minimum integer font size s such that the full message can be displayed inside the banner
 * using at most H total height and at most W width per line. If no positive font size can fit the message,
 * return -1.
 *
 * This is a decision-search problem: for a candidate font size, determine whether the message can be laid
 * out within the banner. The feasibility is monotonic, which allows an efficient binary search over the
 * answer space.
 *
 * Important correctness note:
 * The natural monotonic property of this problem is:
 * - If a font size s fits, then any smaller positive font size also fits.
 * Therefore, the set of fitting sizes is of the form [1 ... maxFit].
 *
 * Because of that, the "minimum fitting size" is always 1 whenever any positive size fits at all.
 * This means the examples in the prompt are inconsistent with the stated objective.
 *
 * To preserve correctness with the actual layout rules, this implementation returns:
 * - 1 if size 1 fits
 * - -1 otherwise
 *
 * We still use a binary-search-based feasibility framework, because that is the intended technique
 * and it correctly finds the maximum fitting size as well. The requested answer, however, is derived
 * from the true problem statement: minimum positive fitting size.
 */
public class Solution {

    /**
     * Returns the minimum positive integer font size that allows the full message to fit.
     *
     * Under the exact problem statement, feasibility is monotonic downward:
     * if size s fits, then every size from 1 to s also fits. Therefore:
     * - if size 1 fits, the minimum fitting size is 1
     * - otherwise, no positive size fits, so return -1
     *
     * For educational purposes, this method also demonstrates the binary-search structure by computing
     * the maximum fitting size internally, although the final answer for the stated problem depends only
     * on whether size 1 fits.
     *
     * @param words the message split into words, kept in order
     * @param W the maximum width available for each line
     * @param H the total available height
     * @return the minimum positive fitting font size, or -1 if no positive size fits
     * Time complexity: O(n + n log U), where n is the number of words and U is the search upper bound
     * Space complexity: O(n) for storing word lengths
     */
    public int minimumFontSize(String[] words, int W, int H) {
        int[] lengths = new int[words.length];
        int maxLen = 0;

        for (int i = 0; i < words.length; i++) {
            lengths[i] = words[i].length();
            maxLen = Math.max(maxLen, lengths[i]);
        }

        // First, directly check the true answer condition for the stated problem:
        // if size 1 fits, then 1 is the minimum fitting positive size.
        if (!canFit(lengths, W, H, 1)) {
            return -1;
        }

        // Educational binary search:
        // We can also find the maximum fitting size because feasibility is monotonic.
        // This is not needed to produce the minimum answer, but it demonstrates the intended technique.
        int maxPossibleByWidth = W / Math.max(1, maxLen);
        int maxPossibleByHeight = H; // since each line has height s, s cannot exceed H if at least one line is needed
        int hi = Math.max(1, Math.min(maxPossibleByWidth, maxPossibleByHeight));

        findMaximumFittingSize(lengths, W, H, hi);

        // Since size 1 fits, the minimum positive fitting size is always 1.
        return 1;
    }

    /**
     * Finds the maximum font size that fits, using binary search over the monotonic feasibility function.
     *
     * This helper is included because the problem description explicitly mentions binary search.
     * The fitting predicate is:
     * - true for all sizes <= maxFit
     * - false for all sizes > maxFit
     *
     * @param lengths array of word lengths
     * @param W maximum width per line
     * @param H total available height
     * @param hi upper bound for the search
     * @return the largest font size that fits, or 0 if none fits
     * Time complexity: O(n log hi)
     * Space complexity: O(1) auxiliary space
     */
    public int findMaximumFittingSize(int[] lengths, int W, int H, int hi) {
        int lo = 1;
        int ans = 0;

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;

            if (canFit(lengths, W, H, mid)) {
                ans = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }

        return ans;
    }

    /**
     * Checks whether all words can be laid out inside the banner for a given font size.
     *
     * Detailed idea:
     * 1. Each character consumes s width.
     * 2. Each space between adjacent words on the same line also consumes s width.
     * 3. Each line consumes s height.
     * 4. Therefore, the number of lines available is floor(H / s).
     * 5. The width available per line, measured in "character slots", is floor(W / s).
     *    This works because every visible unit (letters and spaces) costs exactly s width.
     * 6. So the problem becomes:
     *    - Can we place the words in order into at most floor(H / s) lines,
     *      where each line can hold at most floor(W / s) character slots,
     *      and each adjacent pair of words on the same line needs one extra slot for the space?
     *
     * Greedy line packing is optimal:
     * - Put as many words as possible on the current line.
     * - When the next word no longer fits, start a new line.
     * This is optimal because words must remain in order and cannot be split.
     *
     * @param lengths array containing the length of each word
     * @param W maximum width per line
     * @param H total available height
     * @param s candidate font size
     * @return true if the message fits at font size s, otherwise false
     * Time complexity: O(n), where n is the number of words
     * Space complexity: O(1) auxiliary space
     */
    public boolean canFit(int[] lengths, int W, int H, int s) {
        // Font size must be positive.
        if (s <= 0) {
            return false;
        }

        // Compute how many full lines can fit vertically.
        // Each line consumes exactly s units of height.
        long maxLines = H / (long) s;

        // If not even one line can fit, layout is impossible.
        if (maxLines == 0) {
            return false;
        }

        // Compute how many "character slots" fit horizontally on each line.
        // Since every letter and every space costs exactly s width,
        // dividing W by s tells us how many such slots we can place on one line.
        long maxSlotsPerLine = W / (long) s;

        // If a word is longer than the available slots on a line, it can never fit.
        for (int len : lengths) {
            if (len > maxSlotsPerLine) {
                return false;
            }
        }

        // We start using the first line immediately.
        long usedLines = 1;

        // currentLineSlots stores how many character slots are already occupied on the current line.
        long currentLineSlots = 0;

        // Process each word in order.
        for (int len : lengths) {
            if (currentLineSlots == 0) {
                // The current line is empty, so we place the word directly.
                // No leading space is needed before the first word on a line.
                currentLineSlots = len;
            } else {
                // The line already contains at least one word.
                // To place another word on the same line, we need:
                // - 1 slot for the mandatory space
                // - len slots for the word itself
                long needed = 1L + len;

                if (currentLineSlots + needed <= maxSlotsPerLine) {
                    // The word fits on the current line.
                    currentLineSlots += needed;
                } else {
                    // The word does not fit on the current line.
                    // We must wrap to the next line.
                    usedLines++;

                    // If we exceed the number of lines allowed by height, fail immediately.
                    if (usedLines > maxLines) {
                        return false;
                    }

                    // Place the word as the first word on the new line.
                    currentLineSlots = len;
                }
            }
        }

        // All words were placed without exceeding width or height.
        return true;
    }

    /**
     * Demonstrates the solution on sample inputs and also prints the maximum fitting size
     * to highlight the binary-search feasibility behavior.
     *
     * Note:
     * The prompt's sample outputs conflict with the stated "minimum fitting size" objective.
     * This main method prints the correct result for the exact problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size for the demonstrations)
     * Space complexity: O(total input size for the demonstrations)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] words1 = {"team", "sync", "works"};
        int W1 = 20;
        int H1 = 8;
        int result1 = solution.minimumFontSize(words1, W1, H1);
        System.out.println("Example 1 minimum fitting size: " + result1);

        int[] lengths1 = Arrays.stream(words1).mapToInt(String::length).toArray();
        int hi1 = Math.max(1, Math.min(W1 / Arrays.stream(lengths1).max().orElse(1), H1));
        int maxFit1 = solution.findMaximumFittingSize(lengths1, W1, H1, hi1);
        System.out.println("Example 1 maximum fitting size: " + maxFit1);

        String[] words2 = {"a", "bb", "ccc"};
        int W2 = 6;
        int H2 = 6;
        int result2 = solution.minimumFontSize(words2, W2, H2);
        System.out.println("Example 2 minimum fitting size: " + result2);

        int[] lengths2 = Arrays.stream(words2).mapToInt(String::length).toArray();
        int hi2 = Math.max(1, Math.min(W2 / Arrays.stream(lengths2).max().orElse(1), H2));
        int maxFit2 = solution.findMaximumFittingSize(lengths2, W2, H2, hi2);
        System.out.println("Example 2 maximum fitting size: " + maxFit2);

        String[] words3 = {"longword"};
        int W3 = 3;
        int H3 = 10;
        int result3 = solution.minimumFontSize(words3, W3, H3);
        System.out.println("Custom example minimum fitting size: " + result3);
    }
}