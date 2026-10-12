import java.util.*;

/*
 * Title: Maximum Consecutive Seats After Blocking One Broken Chair
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an array seats of length n where each element is either 0 or 1.
 * A value of 1 means the seat is usable, and 0 means the seat is broken.
 * During a small event, the venue manager is allowed to place exactly one temporary
 * cover over at most one broken seat so that it can be used for the event.
 * In other words, you may change at most one 0 in the array into a 1.
 *
 * Your task is to return the maximum number of consecutive usable seats that can be
 * formed after applying this operation optimally. If the array already contains only
 * usable seats, return the total number of seats. If there are multiple broken seats,
 * you may choose the best one to cover. If no broken seat is covered, that is also
 * allowed, since the operation is "at most one" change.
 *
 * This problem should be solved efficiently for large inputs.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - seats[i] is either 0 or 1
 *
 * Example 1:
 * Input: seats = [1,1,0,1,1,1,0,1]
 * Output: 6
 * Explanation: Cover the broken seat at index 2. The array becomes
 * [1,1,1,1,1,1,0,1], giving a longest consecutive block of 6 usable seats.
 *
 * Example 2:
 * Input: seats = [0,0,1,1,0,1]
 * Output: 4
 * Explanation: Cover the broken seat at index 4. The array becomes
 * [0,0,1,1,1,1], and the longest consecutive usable block is 4.
 *
 * Return the length of the longest contiguous subarray containing only 1s after
 * changing at most one 0 to 1.
 */

public class Solution {

    /**
     * Computes the maximum number of consecutive usable seats after changing
     * at most one broken seat (0) into a usable seat (1).
     *
     * This method uses the classic sliding window technique:
     * - We maintain a window [left, right].
     * - The window is allowed to contain at most one zero.
     * - If the window ever contains more than one zero, we move the left pointer
     *   forward until the window becomes valid again.
     * - Every valid window represents a contiguous segment that can be turned into
     *   all 1s by flipping at most one 0.
     * - We track the maximum valid window length seen.
     *
     * @param seats the array of seats where 1 means usable and 0 means broken
     * @return the maximum length of a contiguous subarray containing only 1s
     *         after changing at most one 0 to 1
     *
     * Time complexity: O(n), because each index is visited at most twice
     *                  (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few variables are used.
     */
    public int maxConsecutiveSeats(int[] seats) {
        // Defensive handling for completeness.
        // According to constraints, n >= 1, but this makes the method safer.
        if (seats == null || seats.length == 0) {
            return 0;
        }

        // 'left' marks the beginning of the current sliding window.
        int left = 0;

        // 'zeroCount' stores how many broken seats (0s) are currently inside the window.
        // We are allowed to have at most one zero in the window,
        // because we may cover at most one broken seat.
        int zeroCount = 0;

        // 'best' stores the maximum valid window length found so far.
        int best = 0;

        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < seats.length; right++) {

            // Step 1:
            // Include seats[right] in the current window.
            // If it is a broken seat, increase zeroCount.
            if (seats[right] == 0) {
                zeroCount++;
            }

            // Step 2:
            // If the window has more than one zero, it is no longer valid.
            // That would mean we would need to flip more than one broken seat,
            // which is not allowed.
            //
            // So we shrink the window from the left until zeroCount <= 1 again.
            while (zeroCount > 1) {
                // If the seat leaving the window is a zero,
                // we must decrease zeroCount.
                if (seats[left] == 0) {
                    zeroCount--;
                }

                // Move the left boundary forward to shrink the window.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is valid:
            // it contains at most one zero.
            //
            // Therefore, by flipping that zero (if it exists),
            // the entire window can become all 1s.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        // 'best' is the maximum length of a valid window found.
        return best;
    }

    /**
     * Helper method to convert an int array into a readable string.
     * This is used only for demonstration in main.
     *
     * @param arr the input integer array
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs and a few additional tests.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(k * n) across all demonstrated test cases,
     *                  where k is the number of test arrays.
     * Space complexity: O(1) extra space excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1
        int[] seats1 = {1, 1, 0, 1, 1, 1, 0, 1};
        int result1 = solution.maxConsecutiveSeats(seats1);
        System.out.println("Input:  " + solution.arrayToString(seats1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        // Sample Input 2
        // Note:
        // The problem statement's explanation says the best segment length is 4,
        // so the correct output is 4.
        int[] seats2 = {0, 0, 1, 1, 0, 1};
        int result2 = solution.maxConsecutiveSeats(seats2);
        System.out.println("Input:  " + solution.arrayToString(seats2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional test: all seats already usable
        int[] seats3 = {1, 1, 1, 1};
        int result3 = solution.maxConsecutiveSeats(seats3);
        System.out.println("Input:  " + solution.arrayToString(seats3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional test: all seats broken
        int[] seats4 = {0, 0, 0, 0};
        int result4 = solution.maxConsecutiveSeats(seats4);
        System.out.println("Input:  " + solution.arrayToString(seats4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional test: single seat usable
        int[] seats5 = {1};
        int result5 = solution.maxConsecutiveSeats(seats5);
        System.out.println("Input:  " + solution.arrayToString(seats5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional test: single seat broken
        int[] seats6 = {0};
        int result6 = solution.maxConsecutiveSeats(seats6);
        System.out.println("Input:  " + solution.arrayToString(seats6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 1");
    }
}