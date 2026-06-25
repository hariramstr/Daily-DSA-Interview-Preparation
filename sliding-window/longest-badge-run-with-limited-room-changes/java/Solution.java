import java.util.*;

/*
Problem Title: Longest Badge Run With Limited Room Changes

Problem Description:
A security team records the sequence of room IDs visited by an employee during a single day.
The sequence is stored in an integer array rooms, where rooms[i] is the room entered at time i.
For auditing, the team wants to find the longest contiguous time interval during which the employee
visited at most k distinct rooms.

Your task is to return the length of the longest contiguous subarray of rooms that contains
no more than k distinct values.

This models a real monitoring scenario where frequent movement across too many different rooms
may indicate unusual behavior, while a long interval with only a few room types may represent
normal work patterns.

A contiguous interval means you may only choose consecutive entries from the log.
If k is 0, then no room can be included, so the answer is 0.

Constraints:
- 1 <= rooms.length <= 200000
- 0 <= rooms[i] <= 1000000000
- 0 <= k <= rooms.length
- The expected solution should run in O(n) time using a sliding window and a frequency map.

Example 1:
Input: rooms = [4, 2, 2, 7, 2, 4, 4, 7], k = 2
Output: 4
Explanation: The longest valid interval is [2, 2, 7, 2], which contains only the distinct room IDs
{2, 7}. Its length is 4.

Example 2:
Input: rooms = [9, 9, 1, 3, 1, 1, 3, 9], k = 3
Output: 8
Explanation: The entire array contains exactly 3 distinct room IDs: {9, 1, 3}.
Therefore the whole log is valid, and the answer is 8.
*/

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains
     * at most k distinct room IDs.
     *
     * This method uses the classic sliding window technique:
     * 1. Expand the right side of the window one element at a time.
     * 2. Track frequencies of room IDs inside the current window.
     * 3. If the window becomes invalid (more than k distinct room IDs),
     *    shrink it from the left until it becomes valid again.
     * 4. After each expansion/shrinking step, record the maximum valid window length seen so far.
     *
     * @param rooms the array of room IDs visited over time
     * @param k the maximum number of distinct room IDs allowed in the chosen interval
     * @return the length of the longest contiguous subarray containing at most k distinct values
     * Time complexity: O(n), where n is the length of rooms, because each element is added
     * and removed from the sliding window at most once.
     * Space complexity: O(k) on average for the frequency map of values currently in the window,
     * and in the worst case O(n) if many distinct values appear while processing.
     */
    public int longestBadgeRunWithLimitedRoomChanges(int[] rooms, int k) {
        // If no rooms are allowed in the interval, the answer must be 0.
        // This directly handles the special case required by the problem.
        if (k == 0 || rooms == null || rooms.length == 0) {
            return 0;
        }

        // This map stores:
        // key   -> room ID
        // value -> how many times that room ID appears in the current window
        Map<Integer, Integer> frequency = new HashMap<>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to rooms.length - 1.
        for (int right = 0; right < rooms.length; right++) {
            int currentRoom = rooms[right];

            // Add the new room at index right into the frequency map.
            // If it is not already present, start its count at 0, then add 1.
            frequency.put(currentRoom, frequency.getOrDefault(currentRoom, 0) + 1);

            // At this point, the window is [left, right].
            // It may or may not be valid.
            //
            // A valid window must contain at most k distinct room IDs.
            // The number of distinct room IDs is exactly frequency.size().
            //
            // If the window has too many distinct room IDs, we must shrink it
            // from the left until it becomes valid again.
            while (frequency.size() > k) {
                int leftRoom = rooms[left];

                // Remove one occurrence of the room at the left edge.
                frequency.put(leftRoom, frequency.get(leftRoom) - 1);

                // If its count becomes 0, it no longer exists in the window,
                // so we remove it from the map completely.
                // This is very important because frequency.size() represents
                // the number of distinct room IDs currently inside the window.
                if (frequency.get(leftRoom) == 0) {
                    frequency.remove(leftRoom);
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // Now the window [left, right] is guaranteed to be valid:
            // it contains at most k distinct room IDs.
            //
            // Its length is:
            // right - left + 1
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is the largest so far.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * Helper method to convert an int array into a readable string.
     * This is used only for demonstration in main.
     *
     * @param array the input integer array
     * @return a human-readable string representation of the array
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to the generated string content.
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * It also verifies by inspection that:
     * - Example 1 returns 4
     * - Example 2 returns 8
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for the demonstrated test cases.
     * Space complexity: O(n) in the worst case due to the frequency map and printed strings.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] rooms1 = {4, 2, 2, 7, 2, 4, 4, 7};
        int k1 = 2;
        int result1 = solution.longestBadgeRunWithLimitedRoomChanges(rooms1, k1);

        System.out.println("Example 1");
        System.out.println("rooms = " + solution.arrayToString(rooms1));
        System.out.println("k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Example 2
        int[] rooms2 = {9, 9, 1, 3, 1, 1, 3, 9};
        int k2 = 3;
        int result2 = solution.longestBadgeRunWithLimitedRoomChanges(rooms2, k2);

        System.out.println("Example 2");
        System.out.println("rooms = " + solution.arrayToString(rooms2));
        System.out.println("k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 8");
        System.out.println();

        // Additional edge case: k = 0
        int[] rooms3 = {1, 2, 3};
        int k3 = 0;
        int result3 = solution.longestBadgeRunWithLimitedRoomChanges(rooms3, k3);

        System.out.println("Edge Case");
        System.out.println("rooms = " + solution.arrayToString(rooms3));
        System.out.println("k = " + k3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
    }
}