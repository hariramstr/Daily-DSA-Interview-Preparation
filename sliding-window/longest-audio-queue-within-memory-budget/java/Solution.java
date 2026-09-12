import java.util.*;

/*
 * Title: Longest Audio Queue Within Memory Budget
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A media player buffers a sequence of audio clips before playback. The i-th clip requires
 * memory[i] megabytes to keep in RAM, and the player must preserve the original order of clips.
 * Given an array memory and an integer budget, return the length of the longest contiguous block
 * of clips that can be buffered at the same time without exceeding the total memory budget.
 *
 * You may choose any contiguous subarray of memory, but the sum of its values must be less than
 * or equal to budget. Your task is to compute the maximum possible number of clips in such a block.
 *
 * This problem is intended to be solved efficiently for large inputs. A brute-force solution that
 * checks every possible subarray will be too slow. Think about how to maintain a valid range while
 * expanding and shrinking a window.
 *
 * Constraints:
 * - 1 <= memory.length <= 200000
 * - 1 <= memory[i] <= 1000000000
 * - 1 <= budget <= 100000000000000
 * - The answer always fits in a 32-bit signed integer.
 *
 * Example 1:
 * Input: memory = [4, 2, 1, 7, 3, 2], budget = 8
 * Output: 3
 * Explanation: The longest valid block is [4, 2, 1] with total memory 7.
 *
 * Example 2:
 * Input: memory = [5, 1, 1, 1, 5], budget = 7
 * Output: 3
 * Explanation: One optimal block is [1, 1, 5] with total memory 7.
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous subarray whose sum is less than or equal to
     * the given memory budget.
     *
     * This method uses the classic sliding window / two-pointer technique.
     * Because all memory values are positive, once the current window sum becomes too large,
     * moving the left boundary to the right is guaranteed to reduce the sum. This property
     * makes the sliding window approach correct and efficient.
     *
     * @param memory the array where memory[i] is the RAM required by the i-th audio clip
     * @param budget the maximum total RAM allowed for any buffered contiguous block
     * @return the length of the longest contiguous block whose total memory usage is at most budget
     * Time complexity: O(n), where n is memory.length, because each index is visited at most twice
     * Space complexity: O(1), ignoring input storage, because only a few variables are used
     */
    public int longestAudioQueueWithinBudget(int[] memory, long budget) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Running sum of the current window [left..right].
        // We use long because:
        // - memory[i] can be as large as 1,000,000,000
        // - the array can be large
        // - the budget itself can be up to 100,000,000,000,000
        long currentSum = 0L;

        // Best answer found so far.
        int maxLength = 0;

        // Expand the window one element at a time by moving "right".
        for (int right = 0; right < memory.length; right++) {
            // Step 1:
            // Include memory[right] in the current window.
            currentSum += memory[right];

            // Step 2:
            // If the window is invalid (sum > budget), shrink it from the left
            // until it becomes valid again.
            //
            // This works because all values are positive:
            // removing elements from the left strictly decreases the sum.
            while (currentSum > budget) {
                currentSum -= memory[left];
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is valid.
            // Its length is:
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * A small helper method to print an input array in a readable format.
     *
     * @param array the integer array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is array.length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected answers so the output can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] memory1 = {4, 2, 1, 7, 3, 2};
        long budget1 = 8L;
        int result1 = solution.longestAudioQueueWithinBudget(memory1, budget1);

        System.out.println("Sample 1:");
        System.out.println("memory = " + solution.arrayToString(memory1));
        System.out.println("budget = " + budget1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Sample 2
        int[] memory2 = {5, 1, 1, 1, 5};
        long budget2 = 7L;
        int result2 = solution.longestAudioQueueWithinBudget(memory2, budget2);

        System.out.println("Sample 2:");
        System.out.println("memory = " + solution.arrayToString(memory2));
        System.out.println("budget = " + budget2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional quick checks
        int[] memory3 = {1};
        long budget3 = 1L;
        System.out.println("Additional Check 1:");
        System.out.println("memory = " + solution.arrayToString(memory3));
        System.out.println("budget = " + budget3);
        System.out.println("Output = " + solution.longestAudioQueueWithinBudget(memory3, budget3));
        System.out.println("Expected = 1");
        System.out.println();

        int[] memory4 = {10, 20, 30};
        long budget4 = 15L;
        System.out.println("Additional Check 2:");
        System.out.println("memory = " + solution.arrayToString(memory4));
        System.out.println("budget = " + budget4);
        System.out.println("Output = " + solution.longestAudioQueueWithinBudget(memory4, budget4));
        System.out.println("Expected = 1");
    }
}