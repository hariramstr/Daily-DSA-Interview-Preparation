import java.util.*;

/*
 * Maximum Font Scale for Digital Signage
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A mall uses a digital signage system to display a single message on one line.
 * For each candidate font scale s, the width of a character c is given by
 * widths[c][s], where widths are nondecreasing as the scale increases.
 * The display panel has a fixed pixel width W, and the entire message must fit
 * on the screen at the same scale.
 *
 * You are given:
 * - a string message consisting of lowercase English letters,
 * - an integer W,
 * - an array scales of available font scales sorted in strictly increasing order,
 * - a 2D integer array charWidths where charWidths[i][j] is the width of
 *   character ('a' + i) at scales[j].
 *
 * Return the largest scale value from scales such that the total rendered width
 * of message is at most W. If the message does not fit even at the smallest
 * scale, return -1.
 *
 * This problem is designed so that checking whether a scale works is
 * straightforward, but doing it for every scale may be too slow when many scales
 * are available. Use the monotonic nature of font sizes: if a message fits at
 * some scale, it will also fit at every smaller scale.
 *
 * Constraints:
 * - 1 <= message.length <= 10^5
 * - 1 <= W <= 10^15
 * - 1 <= scales.length <= 10^5
 * - scales is strictly increasing
 * - charWidths.length == 26
 * - charWidths[i].length == scales.length
 * - 1 <= charWidths[i][j] <= 10^9
 * - For every character i, charWidths[i][j] <= charWidths[i][j + 1]
 */
public class Solution {

    /**
     * Finds the largest font scale value such that the full message fits within width W.
     *
     * The key idea is:
     * 1. For any fixed scale index, we can compute the total width of the message.
     * 2. Because character widths are nondecreasing as scale increases, the total
     *    message width is also nondecreasing.
     * 3. Therefore, the predicate "message fits at this scale" is monotonic:
     *    if it fits at index j, it fits at all smaller indices too.
     * 4. This allows binary search for the largest valid scale.
     *
     * To make each width check efficient, we first count how many times each letter
     * appears in the message. Then for a given scale index j, total width is:
     * sum over letters i of frequency[i] * charWidths[i][j]
     *
     * @param message the lowercase message to render
     * @param W the maximum allowed total width
     * @param scales strictly increasing available scale values
     * @param charWidths charWidths[i][j] is width of ('a' + i) at scales[j]
     * @return the largest scale value that fits, or -1 if even the smallest scale does not fit
     * Time complexity: O(message.length + 26 * log(scales.length))
     * Space complexity: O(26)
     */
    public int maximumFontScale(String message, long W, int[] scales, int[][] charWidths) {
        // Step 1:
        // Count how many times each lowercase letter appears in the message.
        // This is very important because the message can be as large as 100,000 characters.
        // If we recomputed the width by scanning the whole message for every binary-search step,
        // that would be too slow in the worst case.
        //
        // Instead, we compress the message into 26 frequencies:
        // frequency[0] = count of 'a'
        // frequency[1] = count of 'b'
        // ...
        // frequency[25] = count of 'z'
        long[] frequency = buildFrequency(message);

        // Step 2:
        // Before doing binary search, check the smallest scale.
        // If the message does not fit even at the smallest scale, the answer is immediately -1.
        if (!fitsAtScaleIndex(frequency, W, 0, charWidths)) {
            return -1;
        }

        // Step 3:
        // Binary search for the largest index that still fits.
        //
        // Invariant:
        // - All indices <= answer fit.
        // - All indices > answer do not fit.
        //
        // Since we already know index 0 fits, there is at least one valid answer.
        int left = 0;
        int right = scales.length - 1;
        int bestIndex = 0;

        while (left <= right) {
            // Standard midpoint calculation that avoids overflow.
            int mid = left + (right - left) / 2;

            // Check whether the message fits at this scale index.
            if (fitsAtScaleIndex(frequency, W, mid, charWidths)) {
                // If it fits, this scale is a valid candidate.
                // But we want the largest valid scale, so move right.
                bestIndex = mid;
                left = mid + 1;
            } else {
                // If it does not fit, then every larger scale also does not fit
                // because widths are nondecreasing.
                // So we must search on the left side.
                right = mid - 1;
            }
        }

        // Step 4:
        // bestIndex now stores the largest scale index that fits.
        return scales[bestIndex];
    }

    /**
     * Builds a frequency array for the message.
     *
     * frequency[i] stores how many times character ('a' + i) appears.
     *
     * @param message the lowercase message
     * @return an array of length 26 containing character frequencies
     * Time complexity: O(message.length)
     * Space complexity: O(26)
     */
    public long[] buildFrequency(String message) {
        long[] frequency = new long[26];

        // Walk through every character once and count it.
        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            frequency[ch - 'a']++;
        }

        return frequency;
    }

    /**
     * Checks whether the message fits within width W at a specific scale index.
     *
     * We compute:
     * totalWidth = sum(frequency[i] * charWidths[i][scaleIndex]) for i in [0..25]
     *
     * Important implementation detail:
     * - We use long for the running total because values can be very large.
     * - We also stop early as soon as totalWidth exceeds W, which saves time.
     *
     * @param frequency frequency of each lowercase letter in the message
     * @param W maximum allowed width
     * @param scaleIndex index into the scales array / charWidths columns
     * @param charWidths width table for characters at each scale
     * @return true if the message fits at this scale index, false otherwise
     * Time complexity: O(26)
     * Space complexity: O(1)
     */
    public boolean fitsAtScaleIndex(long[] frequency, long W, int scaleIndex, int[][] charWidths) {
        long totalWidth = 0L;

        // There are only 26 lowercase letters, so this loop is constant-sized.
        for (int letter = 0; letter < 26; letter++) {
            if (frequency[letter] == 0) {
                // If this character does not appear in the message,
                // it contributes nothing to the total width.
                continue;
            }

            // Width contribution of this letter at the chosen scale:
            // count(letter) * width(letter, scaleIndex)
            totalWidth += frequency[letter] * (long) charWidths[letter][scaleIndex];

            // Early exit:
            // The moment total width exceeds W, we already know it does not fit.
            if (totalWidth > W) {
                return false;
            }
        }

        return totalWidth <= W;
    }

    /**
     * Demonstrates the solution using sample-style inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called methods
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        // message = "cab"
        // W = 10
        // scales = [1, 2, 3]
        //
        // Relevant widths:
        // 'a' -> [1, 2, 4]
        // 'b' -> [2, 3, 5]
        // 'c' -> [3, 4, 6]
        //
        // Totals:
        // scale 1 => 3 + 1 + 2 = 6  fits
        // scale 2 => 4 + 2 + 3 = 9  fits
        // scale 3 => 6 + 4 + 5 = 15 does not fit
        // Expected answer: 2
        String message1 = "cab";
        long W1 = 10L;
        int[] scales1 = {1, 2, 3};
        int[][] charWidths1 = new int[26][3];

        // Fill all letters with some valid nondecreasing widths.
        for (int i = 0; i < 26; i++) {
            charWidths1[i][0] = 1;
            charWidths1[i][1] = 2;
            charWidths1[i][2] = 3;
        }

        // Override the letters used in the example.
        charWidths1[0] = new int[]{1, 2, 4}; // 'a'
        charWidths1[1] = new int[]{2, 3, 5}; // 'b'
        charWidths1[2] = new int[]{3, 4, 6}; // 'c'

        int result1 = solution.maximumFontScale(message1, W1, scales1, charWidths1);
        System.out.println(result1); // Expected: 2

        // Example 2
        // message = "zzzz"
        // W = 7
        // scales = [2, 4]
        // 'z' -> [2, 3]
        //
        // Totals:
        // scale 2 => 4 * 2 = 8, does not fit
        // Therefore answer is -1
        String message2 = "zzzz";
        long W2 = 7L;
        int[] scales2 = {2, 4};
        int[][] charWidths2 = new int[26][2];

        // Fill all letters with valid nondecreasing widths.
        for (int i = 0; i < 26; i++) {
            charWidths2[i][0] = 1;
            charWidths2[i][1] = 2;
        }

        // Override 'z'
        charWidths2[25] = new int[]{2, 3};

        int result2 = solution.maximumFontScale(message2, W2, scales2, charWidths2);
        System.out.println(result2); // Expected: -1
    }
}