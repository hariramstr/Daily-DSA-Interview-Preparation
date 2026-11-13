import java.util.*;

/*
 * Title: Minimum Screen Width for Wrapped Chat Messages
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * You are building a chat application that displays messages in a fixed-width text area.
 * Each message is a string containing lowercase letters and single spaces between words,
 * with no leading or trailing spaces. A message is shown using word wrapping: each line
 * can contain one or more whole words separated by a single space, and a word cannot be
 * split across lines. The width of a line is the total number of characters in that line,
 * including spaces between words.
 *
 * Given an array messages and an integer maxLines, find the minimum screen width W such
 * that every message can be displayed using at most maxLines lines. All messages must fit
 * under the same width W.
 *
 * If a message contains a word of length L, then any valid width must satisfy W >= L.
 * Your task is to return the smallest such width.
 *
 * This problem should be solved efficiently. A common approach is to binary search the
 * answer W and check whether all messages can be wrapped within maxLines lines for that width.
 *
 * Constraints:
 * - 1 <= messages.length <= 100000
 * - 1 <= messages[i].length <= 100000
 * - The total length of all messages combined does not exceed 200000
 * - 1 <= maxLines <= 100000
 * - messages[i] contains only lowercase English letters and single spaces
 * - Each word length is at least 1
 *
 * Example 1:
 * Input: messages = ["chat app design", "binary search works"], maxLines = 2
 * Output: 12
 * Explanation:
 * With width 12:
 * - "chat app design" can be wrapped as "chat app" / "design"
 * - "binary search works" can be wrapped as "binary" / "search works"
 * Width 11 fails because "search works" has length 12, forcing the second message to use 3 lines.
 *
 * Example 2:
 * Input: messages = ["a bb ccc", "dddd e"], maxLines = 3
 * Output: 4
 * Explanation:
 * With width 4:
 * - "a bb ccc" can be wrapped as "a bb" / "ccc" (2 lines)
 * - "dddd e" can be wrapped as "dddd" / "e" (2 lines)
 * Width 3 is invalid because the word "dddd" does not fit on any line.
 */

public class Solution {

    /**
     * Computes the minimum screen width such that every message can be wrapped
     * into at most maxLines lines.
     *
     * The key idea:
     * 1. Any valid width must be at least the length of the longest word in all messages.
     * 2. Any width larger than or equal to the longest full message length is definitely enough.
     * 3. The feasibility condition is monotonic:
     *    - If width W works, then any width > W also works.
     *    - If width W does not work, then any width < W also does not work.
     * 4. Because of this monotonic behavior, binary search is the natural tool.
     *
     * @param messages the array of chat messages to display
     * @param maxLines the maximum number of lines allowed for each message
     * @return the smallest width that allows every message to fit within maxLines lines
     * Time complexity: O(T log M), where T is the total number of characters across all messages
     * and M is the search range of widths (at most the maximum message length)
     * Space complexity: O(1) extra space, ignoring the input storage
     */
    public int minimumScreenWidth(String[] messages, int maxLines) {
        // We will determine the binary search range [left, right].
        // left  = longest single word across all messages
        // right = longest entire message length across all messages
        //
        // Why?
        // - A width smaller than the longest word is impossible.
        // - A width equal to the full message length always allows that message on one line,
        //   so the longest message length is definitely a valid upper bound for all messages.

        int left = 1;
        int right = 1;

        for (String message : messages) {
            right = Math.max(right, message.length());

            int longestWordInMessage = getLongestWordLength(message);
            left = Math.max(left, longestWordInMessage);
        }

        // Standard binary search for the first valid width.
        while (left < right) {
            int mid = left + (right - left) / 2;

            // If all messages fit within maxLines using width = mid,
            // then mid is a valid candidate, and we try to find an even smaller valid width.
            if (canAllMessagesFit(messages, maxLines, mid)) {
                right = mid;
            } else {
                // Otherwise, mid is too small, so we must search larger widths.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the minimum valid width.
        return left;
    }

    /**
     * Checks whether every message can be wrapped using at most maxLines lines
     * under the given width.
     *
     * @param messages the array of messages to test
     * @param maxLines the maximum allowed lines per message
     * @param width the candidate screen width
     * @return true if all messages fit within maxLines lines at this width; false otherwise
     * Time complexity: O(T), where T is the total number of characters across all messages
     * Space complexity: O(1) extra space
     */
    public boolean canAllMessagesFit(String[] messages, int maxLines, int width) {
        // We test each message independently.
        // If even one message needs more than maxLines lines, then this width fails.
        for (String message : messages) {
            int neededLines = countLinesNeeded(message, width);

            if (neededLines > maxLines) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts how many lines are needed to wrap one message under a given width.
     *
     * This method performs a greedy word-wrapping simulation:
     * - Put as many words as possible on the current line.
     * - When the next word does not fit, start a new line.
     *
     * This greedy approach is optimal for minimizing the number of lines because
     * delaying a word to a later line never helps reduce the total number of lines.
     *
     * @param message the message to wrap
     * @param width the line width
     * @return the number of lines required to display the message
     * Time complexity: O(L), where L is the length of the message
     * Space complexity: O(1) extra space
     */
    public int countLinesNeeded(String message, int width) {
        // We scan the message character by character and extract word lengths
        // without using split(), which avoids creating many temporary strings.
        //
        // Variables:
        // - lines: how many lines we have used so far
        // - currentLineLength: current occupied width on the active line
        //
        // Important detail:
        // - The first word on a line contributes only its word length.
        // - Every additional word contributes 1 extra character for the space,
        //   plus the word length.

        int n = message.length();
        int i = 0;

        int lines = 1;
        int currentLineLength = 0;

        while (i < n) {
            // Find the length of the next word.
            int wordLength = 0;
            while (i < n && message.charAt(i) != ' ') {
                wordLength++;
                i++;
            }

            // If the word itself is longer than width, wrapping is impossible.
            // We return a very large number so the caller will treat this width as invalid.
            if (wordLength > width) {
                return Integer.MAX_VALUE;
            }

            if (currentLineLength == 0) {
                // The current line is empty, so this word starts the line.
                currentLineLength = wordLength;
            } else if (currentLineLength + 1 + wordLength <= width) {
                // The word fits on the current line with one separating space.
                currentLineLength += 1 + wordLength;
            } else {
                // The word does not fit on the current line.
                // Therefore, we must start a new line with this word.
                lines++;
                currentLineLength = wordLength;
            }

            // Skip the single space between words, if present.
            if (i < n && message.charAt(i) == ' ') {
                i++;
            }
        }

        return lines;
    }

    /**
     * Finds the length of the longest word in a message.
     *
     * @param message the message to inspect
     * @return the maximum word length in the message
     * Time complexity: O(L), where L is the length of the message
     * Space complexity: O(1) extra space
     */
    public int getLongestWordLength(String message) {
        int longest = 0;
        int current = 0;

        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);

            if (ch == ' ') {
                longest = Math.max(longest, current);
                current = 0;
            } else {
                current++;
            }
        }

        // Final word must also be considered.
        longest = Math.max(longest, current);
        return longest;
    }

    /**
     * Demonstrates the solution using the sample test cases from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(T log M) across the demonstrated examples
     * Space complexity: O(1) extra space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] messages1 = {"chat app design", "binary search works"};
        int maxLines1 = 2;
        int result1 = solution.minimumScreenWidth(messages1, maxLines1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 12");
        System.out.println();

        String[] messages2 = {"a bb ccc", "dddd e"};
        int maxLines2 = 3;
        int result2 = solution.minimumScreenWidth(messages2, maxLines2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional quick sanity checks for beginners:
        String[] messages3 = {"hello"};
        int maxLines3 = 1;
        int result3 = solution.minimumScreenWidth(messages3, maxLines3);
        System.out.println("Single message, one line: " + result3);
        System.out.println("Expected: 5");
        System.out.println();

        String[] messages4 = {"a a a a"};
        int maxLines4 = 2;
        int result4 = solution.minimumScreenWidth(messages4, maxLines4);
        System.out.println("Repeated short words: " + result4);
        System.out.println("Expected: 3");
    }
}