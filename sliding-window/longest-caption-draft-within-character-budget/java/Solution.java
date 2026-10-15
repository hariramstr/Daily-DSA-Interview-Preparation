import java.util.*;

/*
 * Title: Longest Caption Draft Within Character Budget
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A social media team is drafting a caption from a sequence of word fragments.
 * The i-th fragment contributes words[i].length characters to the final caption,
 * and fragments must be used in their original order. You are given an array
 * words of non-empty strings and an integer maxChars. Find the maximum number
 * of consecutive fragments that can be included so that the total number of
 * characters in the chosen window is at most maxChars.
 *
 * Only the characters inside each fragment count toward the budget. Ignore
 * spaces between fragments; assume they are inserted automatically and do not
 * consume any characters. Your task is to return the length of the longest
 * contiguous block of fragments whose combined character count does not exceed
 * maxChars.
 *
 * This problem is intended to be solved efficiently for large inputs. A
 * brute-force solution that checks every subarray will be too slow. Think about
 * maintaining a moving window and shrinking it only when the current total
 * exceeds the allowed budget.
 *
 * Constraints:
 * - 1 <= words.length <= 100000
 * - 1 <= words[i].length <= 10000
 * - 1 <= maxChars <= 10^9
 * - words[i] consists of lowercase English letters
 *
 * Note:
 * The first example text in the prompt contains an inconsistency in its stated
 * output. For words = ["go","viral","now","x","trend"] and maxChars = 8, the
 * correct answer is 2, not 3, because no valid window of length 3 fits within
 * the budget. This implementation returns the correct value.
 */

public class Solution {

    /**
     * Finds the maximum number of consecutive word fragments whose total character
     * count is at most maxChars.
     *
     * This method uses the classic sliding window / two-pointer technique:
     * - Expand the right end of the window one fragment at a time.
     * - Keep track of the current total character count inside the window.
     * - If the total exceeds maxChars, move the left end forward until the window
     *   becomes valid again.
     * - After each adjustment, update the best window length seen so far.
     *
     * @param words the array of non-empty word fragments, kept in original order
     * @param maxChars the maximum allowed total number of characters in a window
     * @return the maximum number of consecutive fragments whose combined length
     *         does not exceed maxChars
     *
     * Time complexity: O(n), where n is words.length, because each index moves
     * at most once from left to right.
     * Space complexity: O(1), ignoring the input array, because only a few
     * variables are used.
     */
    public int longestCaptionDraft(String[] words, int maxChars) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Best answer found so far: maximum valid window length.
        int best = 0;

        // Current total number of characters inside the window [left..right].
        // We use long for extra safety, even though int would also work under
        // the given constraints.
        long currentChars = 0;

        // Move the right boundary from the start to the end of the array.
        for (int right = 0; right < words.length; right++) {
            // Step 1:
            // Include the new fragment at index 'right' into the current window.
            currentChars += words[right].length();

            // Step 2:
            // If the window is invalid (too many characters), shrink it from
            // the left side until it becomes valid again.
            //
            // Why a while loop instead of an if?
            // Because adding one large fragment may require removing multiple
            // fragments from the left before the total fits within maxChars.
            while (currentChars > maxChars) {
                currentChars -= words[left].length();
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is guaranteed to be valid.
            // Its length is:
            int windowLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is longer than any
            // valid window seen before.
            if (windowLength > best) {
                best = windowLength;
            }
        }

        // After scanning all possible right boundaries, 'best' stores the
        // maximum valid number of consecutive fragments.
        return best;
    }

    /**
     * A helper method that prints a test case and the computed result.
     *
     * @param words the input array of word fragments
     * @param maxChars the character budget
     * @return the computed maximum valid window length
     *
     * Time complexity: O(n), where n is words.length, because it delegates to
     * the sliding window solution.
     * Space complexity: O(1), ignoring the input array.
     */
    public int demonstrateCase(String[] words, int maxChars) {
        int result = longestCaptionDraft(words, maxChars);
        System.out.println("words = " + Arrays.toString(words));
        System.out.println("maxChars = " + maxChars);
        System.out.println("Longest valid consecutive block length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point. Demonstrates the solution on sample inputs from the
     * prompt and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total number of words across demonstrated test cases).
     * Space complexity: O(1), excluding input storage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        // Careful verification:
        // ["go","viral","now","x","trend"], maxChars = 8
        // Lengths: 2, 5, 3, 1, 5
        //
        // Valid windows of length 2:
        // ["go","viral"] = 7
        // ["viral","now"] = 8
        // ["now","x"] = 4
        // ["x","trend"] = 6
        //
        // Any window of length 3:
        // ["go","viral","now"] = 10 -> invalid
        // ["viral","now","x"] = 9 -> invalid
        // ["now","x","trend"] = 9 -> invalid
        //
        // Therefore the correct answer is 2.
        String[] words1 = {"go", "viral", "now", "x", "trend"};
        int maxChars1 = 8;
        int result1 = solution.demonstrateCase(words1, maxChars1);
        System.out.println("Expected (correctly verified): 2");
        System.out.println("Actual: " + result1);
        System.out.println();

        // Sample 2 from the prompt.
        // ["a","bb","c","ddd","e"], maxChars = 4
        // Lengths: 1, 2, 1, 3, 1
        //
        // ["a","bb","c"] = 4 -> valid, length 3
        // Any length 4 window:
        // ["a","bb","c","ddd"] = 7 -> invalid
        // ["bb","c","ddd","e"] = 7 -> invalid
        //
        // Therefore the answer is 3.
        String[] words2 = {"a", "bb", "c", "ddd", "e"};
        int maxChars2 = 4;
        int result2 = solution.demonstrateCase(words2, maxChars2);
        System.out.println("Expected: 3");
        System.out.println("Actual: " + result2);
        System.out.println();

        // Additional beginner-friendly sanity checks.

        // Single fragment fits.
        String[] words3 = {"hello"};
        int maxChars3 = 5;
        int result3 = solution.demonstrateCase(words3, maxChars3);
        System.out.println("Expected: 1");
        System.out.println("Actual: " + result3);
        System.out.println();

        // Single fragment does not fit.
        String[] words4 = {"hello"};
        int maxChars4 = 4;
        int result4 = solution.demonstrateCase(words4, maxChars4);
        System.out.println("Expected: 0");
        System.out.println("Actual: " + result4);
        System.out.println();

        // Entire array fits.
        String[] words5 = {"a", "b", "c", "d"};
        int maxChars5 = 4;
        int result5 = solution.demonstrateCase(words5, maxChars5);
        System.out.println("Expected: 4");
        System.out.println("Actual: " + result5);
    }
}