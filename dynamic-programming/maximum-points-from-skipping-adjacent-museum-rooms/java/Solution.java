/*
Problem Title: Maximum Points from Skipping Adjacent Museum Rooms

Problem Description:
A museum curator is planning a guided tour through a straight hallway of exhibit rooms.
Each room has a popularity score, given in an integer array rooms, where rooms[i] is
the number of visitor points earned if room i is included in the tour.

To avoid crowding and noise overlap, the curator cannot include two adjacent rooms in
the same tour.

Your task is to return the maximum total visitor points the curator can earn by choosing
a subset of rooms such that no two chosen rooms are next to each other.

You may choose to skip any room, and it is also valid to choose no rooms at all if every
score is negative or zero. In other words, the answer should never be less than 0.

This is an interview-style dynamic programming problem. A correct solution should
efficiently decide, for each position, whether it is better to include the current room
and skip the previous one, or skip the current room and keep the best answer seen so far.

Constraints:
- 1 <= rooms.length <= 100000
- -1000 <= rooms[i] <= 1000

Example 1:
Input: rooms = [4, 2, 7, 9, 3]
Output: 13
Explanation: Choose rooms with scores 4, 9, and 3 is not allowed because 9 and 3 are
adjacent. The best valid choice is 4 + 9 = 13.

Example 2:
Input: rooms = [5, 1, 1, 5]
Output: 10
Explanation: Choose the first and last rooms for a total of 10. They are not adjacent,
so this is valid.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum total visitor points that can be earned by selecting
     * non-adjacent rooms.
     *
     * Dynamic programming idea:
     * For each room, we have two choices:
     * 1. Skip the current room -> keep the best answer up to the previous room.
     * 2. Take the current room -> add its value to the best answer up to two rooms back.
     *
     * Because choosing no rooms is allowed, the answer is never allowed to go below 0.
     *
     * @param rooms the array of room popularity scores; rooms[i] is the score of room i
     * @return the maximum total score obtainable without choosing adjacent rooms, never less than 0
     * Time complexity: O(n), where n is the number of rooms
     * Space complexity: O(1), because only a constant amount of extra memory is used
     */
    public int maxPoints(int[] rooms) {
        // Defensive handling:
        // The constraints guarantee at least one element, but in beginner-friendly code
        // it is still good practice to safely handle null or empty input.
        if (rooms == null || rooms.length == 0) {
            return 0;
        }

        // prevTwo represents the best answer for the subarray ending at index i - 2.
        // In other words, before processing the current room, this stores:
        // "What is the maximum score we could have earned up to two positions earlier?"
        int prevTwo = 0;

        // prevOne represents the best answer for the subarray ending at index i - 1.
        // In other words:
        // "What is the maximum score we could have earned up to the previous room?"
        int prevOne = 0;

        // We now process each room from left to right.
        for (int i = 0; i < rooms.length; i++) {
            // Option 1: skip the current room.
            // If we skip room i, then the best total remains whatever we had up to room i - 1.
            int skipCurrent = prevOne;

            // Option 2: take the current room.
            // If we take room i, we are NOT allowed to take room i - 1.
            // Therefore, we add rooms[i] to the best answer up to room i - 2.
            int takeCurrent = prevTwo + rooms[i];

            // We are also allowed to choose no rooms at all.
            // This matters when values are negative.
            // Example: rooms = [-5, -2, -8]
            // We should return 0, not a negative number.
            //
            // So the best answer ending at this position is the maximum among:
            // - skipping the current room
            // - taking the current room
            // - taking nothing at all (0)
            int currentBest = Math.max(0, Math.max(skipCurrent, takeCurrent));

            // Shift the window forward for the next iteration:
            // - the old prevOne becomes the new prevTwo
            // - currentBest becomes the new prevOne
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all rooms, prevOne stores the best answer for the full array.
        return prevOne;
    }

    /**
     * Alternative beginner-friendly dynamic programming version using an explicit DP array.
     * This version is slightly more verbose but can be easier to understand when learning.
     *
     * dp[i] means:
     * the maximum score obtainable from the first i rooms (that is, considering indices 0 to i - 1).
     *
     * Transition:
     * - Skip room i - 1: dp[i] = dp[i - 1]
     * - Take room i - 1: dp[i] = dp[i - 2] + rooms[i - 1]
     *
     * Then take the maximum of those choices, while never going below 0.
     *
     * @param rooms the array of room popularity scores
     * @return the maximum total score obtainable without choosing adjacent rooms, never less than 0
     * Time complexity: O(n), where n is the number of rooms
     * Space complexity: O(n), due to the DP array
     */
    public int maxPointsWithDpArray(int[] rooms) {
        if (rooms == null || rooms.length == 0) {
            return 0;
        }

        int n = rooms.length;

        // dp[0] = 0 means:
        // considering zero rooms, the best score is 0.
        int[] dp = new int[n + 1];
        dp[0] = 0;

        // dp[1] means:
        // considering only the first room, the best is either:
        // - skip it -> 0
        // - take it -> rooms[0]
        // Since answer cannot be negative, compare with 0 as well.
        dp[1] = Math.max(0, rooms[0]);

        // Fill the DP table from left to right.
        for (int i = 2; i <= n; i++) {
            // Skip the current room (which is rooms[i - 1]).
            int skipCurrent = dp[i - 1];

            // Take the current room, so we must add it to dp[i - 2].
            int takeCurrent = dp[i - 2] + rooms[i - 1];

            // Best of the two choices, never below 0.
            dp[i] = Math.max(0, Math.max(skipCurrent, takeCurrent));
        }

        return dp[n];
    }

    /**
     * Utility method to print an integer array in a readable format.
     *
     * @param rooms the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to the created string content
     */
    public String arrayToString(int[] rooms) {
        return Arrays.toString(rooms);
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) overall for the demonstrated test cases
     * Space complexity: O(1) extra space excluding output and test data
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] rooms1 = {4, 2, 7, 9, 3};
        int result1 = solution.maxPoints(rooms1);
        System.out.println("Input: rooms = " + solution.arrayToString(rooms1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 13");
        System.out.println();

        // Verification by reasoning:
        // Possible strong choices include:
        // - 4 + 7 + 3 = 14, but this is actually valid because indices 0, 2, 4 are non-adjacent.
        // Therefore the mathematically correct maximum for this input is 14.
        //
        // The problem statement says 13 by choosing 4 + 9, but that overlooks 4 + 7 + 3.
        // Our algorithm correctly returns the true maximum under the stated rules.

        // Sample input 2 from the problem statement.
        int[] rooms2 = {5, 1, 1, 5};
        int result2 = solution.maxPoints(rooms2);
        System.out.println("Input: rooms = " + solution.arrayToString(rooms2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 10");
        System.out.println();

        // Extra test: all negative values.
        // Since choosing no rooms is allowed, answer should be 0.
        int[] rooms3 = {-5, -1, -8};
        int result3 = solution.maxPoints(rooms3);
        System.out.println("Input: rooms = " + solution.arrayToString(rooms3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Extra test: mixed positive and negative values.
        int[] rooms4 = {2, -1, 3, -4, 5};
        int result4 = solution.maxPoints(rooms4);
        System.out.println("Input: rooms = " + solution.arrayToString(rooms4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 10");
        System.out.println();

        // Extra test: single room.
        int[] rooms5 = {7};
        int result5 = solution.maxPoints(rooms5);
        System.out.println("Input: rooms = " + solution.arrayToString(rooms5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 7");
        System.out.println();

        // Extra test: single non-positive room.
        int[] rooms6 = {-3};
        int result6 = solution.maxPoints(rooms6);
        System.out.println("Input: rooms = " + solution.arrayToString(rooms6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 0");
        System.out.println();

        // Also demonstrate the DP-array version on one sample.
        int dpArrayResult = solution.maxPointsWithDpArray(rooms2);
        System.out.println("DP-array version on rooms = " + solution.arrayToString(rooms2));
        System.out.println("Output: " + dpArrayResult);
        System.out.println("Expected: 10");
    }
}