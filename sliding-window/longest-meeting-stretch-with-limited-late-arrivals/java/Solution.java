import java.util.*;

/*
 * Title: Longest Meeting Stretch With Limited Late Arrivals
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A company tracks a day of back-to-back meeting slots using a binary array arrivals,
 * where arrivals[i] = 1 means the attendee arrived on time for slot i, and arrivals[i] = 0
 * means they arrived late for that slot. Management wants to identify the longest contiguous
 * stretch of meeting slots that can still be treated as a "reliable attendance block" if they
 * are willing to excuse at most k late arrivals inside that stretch.
 *
 * Your task is to return the length of the longest contiguous subarray containing at most k zeros.
 *
 * In other words, find the maximum number of consecutive meeting slots such that no more than k
 * of them are late arrivals. The chosen block must be contiguous, and you may excuse any late
 * arrivals already inside the block, but you cannot reorder slots.
 *
 * This problem should be solved efficiently for large inputs, so solutions that check every
 * possible subarray will be too slow.
 *
 * Constraints:
 * - 1 <= arrivals.length <= 200000
 * - arrivals[i] is either 0 or 1
 * - 0 <= k <= arrivals.length
 *
 * Example 1:
 * Input: arrivals = [1,1,0,1,0,1,1,1], k = 1
 * Output: 5
 * Explanation: The longest valid block is [1,0,1,1,1], which contains exactly one late arrival.
 *
 * Example 2:
 * Input: arrivals = [0,0,1,1,1,0,1,1], k = 2
 * Output: 7
 * Explanation: The subarray [0,1,1,1,0,1,1] has length 7 and contains two late arrivals, which is allowed.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most k zeros.
     *
     * This method uses the sliding window technique:
     * - Expand the right boundary one step at a time.
     * - Count how many zeros are currently inside the window.
     * - If the number of zeros becomes greater than k, move the left boundary forward
     *   until the window becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * @param arrivals the binary array where 1 means on-time arrival and 0 means late arrival
     * @param k the maximum number of late arrivals (zeros) allowed inside the chosen contiguous block
     * @return the length of the longest contiguous subarray containing at most k zeros
     *
     * Time complexity: O(n), where n is the length of the arrivals array,
     * because each index is visited at most twice (once by right, once by left).
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestReliableAttendanceBlock(int[] arrivals, int k) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Number of zeros currently inside the window [left, right].
        int zeroCount = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from the start of the array to the end.
        for (int right = 0; right < arrivals.length; right++) {

            // Step 1:
            // Include arrivals[right] into the current window.
            // If it is a zero, then we now have one more late arrival inside the window.
            if (arrivals[right] == 0) {
                zeroCount++;
            }

            // Step 2:
            // If the window has too many zeros, it is invalid.
            // We must shrink it from the left until it becomes valid again.
            //
            // Why a while loop instead of if?
            // Because the window may still have more than k zeros even after moving left once.
            while (zeroCount > k) {

                // Before moving left forward, check whether the element leaving the window is zero.
                // If it is zero, then the zero count inside the window decreases by one.
                if (arrivals[left] == 0) {
                    zeroCount--;
                }

                // Actually shrink the window by moving the left boundary rightward.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to contain at most k zeros.
            // So it is a valid candidate answer.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer than anything seen before.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength stores the answer.
        return maxLength;
    }

    /**
     * A second public method with the same logic, provided as a convenience wrapper
     * using the exact problem wording.
     *
     * @param arrivals the binary array representing on-time (1) and late (0) arrivals
     * @param k the maximum number of zeros allowed in the contiguous subarray
     * @return the maximum length of a contiguous subarray containing at most k zeros
     *
     * Time complexity: O(n), where n is the length of the arrivals array.
     * Space complexity: O(1).
     */
    public int longestSubarrayWithAtMostKZeros(int[] arrivals, int k) {
        return longestReliableAttendanceBlock(arrivals, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the results can be visually verified.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call, where n is the input array length.
     * Space complexity: O(1), excluding the input arrays themselves.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // arrivals = [1,1,0,1,0,1,1,1], k = 1
        // Expected output: 5
        //
        // Quick correctness trace:
        // The best valid window is [1,0,1,1,1], length 5, with exactly one zero.
        int[] arrivals1 = {1, 1, 0, 1, 0, 1, 1, 1};
        int k1 = 1;
        int result1 = solution.longestReliableAttendanceBlock(arrivals1, k1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Example 1 Expected: 5");

        // Example 2:
        // arrivals = [0,0,1,1,1,0,1,1], k = 2
        // Expected output: 7
        //
        // Quick correctness trace:
        // The best valid window is [0,1,1,1,0,1,1], length 7, with exactly two zeros.
        int[] arrivals2 = {0, 0, 1, 1, 1, 0, 1, 1};
        int k2 = 2;
        int result2 = solution.longestReliableAttendanceBlock(arrivals2, k2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Example 2 Expected: 7");

        // Additional beginner-friendly checks:

        // Case: all on-time arrivals
        int[] arrivals3 = {1, 1, 1, 1};
        int k3 = 0;
        int result3 = solution.longestReliableAttendanceBlock(arrivals3, k3);
        System.out.println("All ones Result: " + result3);
        System.out.println("All ones Expected: 4");

        // Case: all late arrivals, but enough excuses to cover all of them
        int[] arrivals4 = {0, 0, 0, 0};
        int k4 = 4;
        int result4 = solution.longestReliableAttendanceBlock(arrivals4, k4);
        System.out.println("All zeros with enough k Result: " + result4);
        System.out.println("All zeros with enough k Expected: 4");

        // Case: no late arrivals can be excused
        int[] arrivals5 = {0, 1, 1, 0, 1, 1, 1, 0};
        int k5 = 0;
        int result5 = solution.longestReliableAttendanceBlock(arrivals5, k5);
        System.out.println("k = 0 Result: " + result5);
        System.out.println("k = 0 Expected: 3");
    }
}