import java.util.*;

/*
 * Title: Find the First Day a Game Server Reaches Target Capacity
 * Difficulty: Easy
 * Topic: Binary Search
 *
 * Problem Description:
 * A gaming platform records the total number of active players on a server at the end of each day.
 * Because the game is steadily growing, the daily totals are sorted in non-decreasing order.
 * You are given an integer array players, where players[i] is the active player count on day i,
 * and an integer target representing a required server capacity milestone.
 *
 * Your task is to return the index of the first day on which the server reaches or exceeds target players.
 * If the server never reaches target, return -1.
 *
 * This problem should be solved efficiently using binary search rather than scanning from left to right.
 * Since the array is already sorted, you can repeatedly discard half of the remaining search space.
 *
 * Return the smallest index i such that players[i] >= target. If no such index exists, return -1.
 *
 * Constraints:
 * - 1 <= players.length <= 100000
 * - 0 <= players[i] <= 1000000000
 * - players is sorted in non-decreasing order
 * - 0 <= target <= 1000000000
 *
 * Example 1:
 * Input: players = [120, 150, 150, 210, 260], target = 151
 * Output: 3
 * Explanation: Day 3 is the first day with at least 151 active players, since players[3] = 210.
 *
 * Example 2:
 * Input: players = [40, 80, 80, 95], target = 100
 * Output: -1
 * Explanation: The server never reaches 100 active players, so the answer is -1.
 */

public class Solution {

    /**
     * Finds the first index at which the player count is greater than or equal to the target.
     *
     * This method uses binary search on a sorted array. Because the array is in non-decreasing order,
     * once we find a value that is at least the target, we know that any valid earlier answer must be
     * on the left side, so we continue searching there to find the first such index.
     *
     * @param players the sorted array of daily active player counts
     * @param target the required player milestone to reach or exceed
     * @return the smallest index i such that players[i] >= target; returns -1 if no such index exists
     *
     * Time complexity: O(log n), where n is the length of the players array
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int firstDayAtLeastTarget(int[] players, int target) {
        // We will search within the inclusive range [left, right].
        int left = 0;
        int right = players.length - 1;

        // This variable stores the best answer found so far.
        // We initialize it to -1 to mean "not found yet".
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        while (left <= right) {
            // Compute the middle index safely.
            // This avoids potential overflow compared to (left + right) / 2.
            int mid = left + (right - left) / 2;

            // If the middle value is large enough, then mid is a candidate answer.
            if (players[mid] >= target) {
                // Record this index as a possible answer.
                answer = mid;

                // But we are not done yet:
                // we want the FIRST index where players[i] >= target.
                // So we continue searching the LEFT half to see if there is an earlier valid index.
                right = mid - 1;
            } else {
                // If players[mid] is still less than target,
                // then every index from left through mid is also too small
                // because the array is sorted in non-decreasing order.
                // Therefore, we discard the left half including mid,
                // and search only in the right half.
                left = mid + 1;
            }
        }

        // If we found at least one valid index, answer holds the first one.
        // Otherwise it remains -1.
        return answer;
    }

    /**
     * Runs a single demonstration test case and prints the result.
     *
     * @param players the sorted array of daily active player counts
     * @param target the required player milestone
     * @return the computed first index where players[i] >= target, or -1 if none exists
     *
     * Time complexity: O(log n), where n is the length of the players array
     * Space complexity: O(1), excluding the input array
     */
    public int demonstrateCase(int[] players, int target) {
        int result = firstDayAtLeastTarget(players, target);
        System.out.println("players = " + Arrays.toString(players));
        System.out.println("target = " + target);
        System.out.println("first day index = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution using the sample inputs from the problem statement.
     *
     * It also prints expected outputs so a beginner can easily compare them with the actual results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(log n) per demonstration case
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // players = [120, 150, 150, 210, 260], target = 151
        // The first value >= 151 is 210 at index 3.
        int[] players1 = {120, 150, 150, 210, 260};
        int target1 = 151;
        System.out.println("Example 1:");
        int result1 = solution.demonstrateCase(players1, target1);
        System.out.println("Expected output: 3");
        System.out.println("Actual output: " + result1);
        System.out.println();

        // Example 2:
        // players = [40, 80, 80, 95], target = 100
        // No value is >= 100, so the answer is -1.
        int[] players2 = {40, 80, 80, 95};
        int target2 = 100;
        System.out.println("Example 2:");
        int result2 = solution.demonstrateCase(players2, target2);
        System.out.println("Expected output: -1");
        System.out.println("Actual output: " + result2);
        System.out.println();

        // Additional beginner-friendly checks.
        System.out.println("Additional checks:");

        int[] players3 = {5, 10, 15, 20};
        int target3 = 5;
        System.out.println("Case where target is reached on the first day:");
        solution.demonstrateCase(players3, target3);

        int[] players4 = {7, 7, 7, 7};
        int target4 = 7;
        System.out.println("Case with duplicates where the first valid index should be returned:");
        solution.demonstrateCase(players4, target4);

        int[] players5 = {1, 3, 5, 7, 9};
        int target5 = 6;
        System.out.println("Case where target falls between existing values:");
        solution.demonstrateCase(players5, target5);
    }
}