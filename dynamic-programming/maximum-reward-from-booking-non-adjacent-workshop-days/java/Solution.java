/*
Problem Title: Maximum Reward from Booking Non-Adjacent Workshop Days

Problem Description:
A training company offers a sequence of one-day workshops over the next n days.
If you book the workshop on day i, you earn rewards[i] points. However, preparing
for a workshop uses the entire following day, so you are not allowed to book
workshops on two adjacent days.

Your task is to return the maximum total reward points you can earn by choosing
a subset of workshop days under this rule.

Formally, given an integer array rewards where rewards[i] is the reward for
booking the workshop on day i, choose a set of indices such that no two chosen
indices differ by 1, and the sum of their rewards is as large as possible.

This is not just about greedily taking the largest reward. A smaller reward today
may allow a better combination later, so you must consider overlapping subproblems
efficiently.

Constraints:
- 1 <= rewards.length <= 100000
- 0 <= rewards[i] <= 1000000000
- The answer fits in a 64-bit signed integer

Example 1:
Input: rewards = [4, 10, 3, 1, 5]
Output: 15
Explanation: Book days 1 and 4 for a total of 10 + 5 = 15. Booking day 0, 2,
and 4 gives 12, which is smaller.

Example 2:
Input: rewards = [2, 7, 9, 3, 1]
Output: 12
Explanation: The best choice is day 0, day 2, and day 4 for 2 + 9 + 1 = 12.

Return only the maximum total reward. An O(n) dynamic programming solution is expected.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum total reward that can be earned by booking workshops
     * on non-adjacent days.
     *
     * This method uses dynamic programming with space optimization.
     * Instead of storing the best answer for every index in an array, it keeps
     * only the two most recent states that are needed to compute the next one.
     *
     * Core idea:
     * For each day, we have exactly two choices:
     * 1. Skip the current day:
     *    Then the best total remains whatever was best up to the previous day.
     * 2. Book the current day:
     *    Then we must skip the previous day, so we add rewards[i] to the best
     *    total up to day i - 2.
     *
     * Recurrence:
     * dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])
     *
     * @param rewards an array where rewards[i] is the reward earned by booking
     *                the workshop on day i
     * @return the maximum total reward obtainable without booking adjacent days
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(1), excluding the input array
     */
    public long maxReward(int[] rewards) {
        // Defensive handling:
        // The problem guarantees at least one element, but checking for null or empty
        // makes the method safer and more beginner-friendly.
        if (rewards == null || rewards.length == 0) {
            return 0L;
        }

        // If there is only one day, the answer is simply that day's reward.
        if (rewards.length == 1) {
            return rewards[0];
        }

        // "prevTwo" represents the best answer considering days up to index i - 2.
        // At the start, for day 0, the best we can do is take rewards[0].
        long prevTwo = rewards[0];

        // "prevOne" represents the best answer considering days up to index i - 1.
        // For the first two days, we must choose the better of day 0 or day 1,
        // because we cannot take both since they are adjacent.
        long prevOne = Math.max((long) rewards[0], (long) rewards[1]);

        // Now process each day from index 2 onward.
        for (int i = 2; i < rewards.length; i++) {
            // Option 1: skip the current day.
            // If we skip day i, then the best total remains the same as the best
            // total up to day i - 1.
            long skipCurrent = prevOne;

            // Option 2: take the current day.
            // If we take day i, we are not allowed to take day i - 1.
            // Therefore, we add rewards[i] to the best total up to day i - 2.
            long takeCurrent = prevTwo + rewards[i];

            // The best answer up to day i is the better of the two choices.
            long current = Math.max(skipCurrent, takeCurrent);

            // Shift the window forward:
            // - The old prevOne becomes the new prevTwo
            // - The newly computed current becomes the new prevOne
            prevTwo = prevOne;
            prevOne = current;
        }

        // After processing all days, prevOne holds the best answer for the full array.
        return prevOne;
    }

    /**
     * Computes the maximum total reward using a full dynamic programming array.
     *
     * This version is especially useful for learning because it explicitly stores
     * the best answer for every prefix of the array.
     *
     * dp[i] means:
     * the maximum reward obtainable considering days 0 through i inclusive.
     *
     * Transition:
     * - If we skip day i, total is dp[i - 1]
     * - If we take day i, total is dp[i - 2] + rewards[i]
     * So:
     * dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])
     *
     * @param rewards an array where rewards[i] is the reward earned by booking
     *                the workshop on day i
     * @return the maximum total reward obtainable without booking adjacent days
     * Time complexity: O(n), where n is the number of days
     * Space complexity: O(n), due to the DP array
     */
    public long maxRewardWithDpArray(int[] rewards) {
        if (rewards == null || rewards.length == 0) {
            return 0L;
        }

        if (rewards.length == 1) {
            return rewards[0];
        }

        long[] dp = new long[rewards.length];

        // Base case for the first day:
        // We can either take it (reward = rewards[0]) or take nothing.
        // Since rewards[i] is non-negative, taking it is always at least as good.
        dp[0] = rewards[0];

        // Base case for the second day:
        // We cannot take both day 0 and day 1, so choose the larger reward.
        dp[1] = Math.max((long) rewards[0], (long) rewards[1]);

        // Fill the DP table from left to right.
        for (int i = 2; i < rewards.length; i++) {
            // If we skip the current day, best total remains dp[i - 1].
            long skipCurrent = dp[i - 1];

            // If we take the current day, we must add it to dp[i - 2].
            long takeCurrent = dp[i - 2] + rewards[i];

            // Store the better choice.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        return dp[rewards.length - 1];
    }

    /**
     * Converts an integer array to a readable string.
     *
     * This helper method is used only for demonstration in main.
     *
     * @param arr the input integer array
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), for the generated string content
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * The printed outputs are verified against the expected answers:
     * - [4, 10, 3, 1, 5] -> 15
     * - [2, 7, 9, 3, 1] -> 12
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(1) extra for the optimized method
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] rewards1 = {4, 10, 3, 1, 5};
        long result1 = solution.maxReward(rewards1);
        System.out.println("Input: " + solution.arrayToString(rewards1));
        System.out.println("Maximum total reward: " + result1);
        System.out.println("Expected: 15");
        System.out.println();

        int[] rewards2 = {2, 7, 9, 3, 1};
        long result2 = solution.maxReward(rewards2);
        System.out.println("Input: " + solution.arrayToString(rewards2));
        System.out.println("Maximum total reward: " + result2);
        System.out.println("Expected: 12");
        System.out.println();

        // Additional small demonstrations for beginners.

        int[] rewards3 = {5};
        long result3 = solution.maxReward(rewards3);
        System.out.println("Input: " + solution.arrayToString(rewards3));
        System.out.println("Maximum total reward: " + result3);
        System.out.println("Expected: 5");
        System.out.println();

        int[] rewards4 = {8, 1};
        long result4 = solution.maxReward(rewards4);
        System.out.println("Input: " + solution.arrayToString(rewards4));
        System.out.println("Maximum total reward: " + result4);
        System.out.println("Expected: 8");
        System.out.println();

        int[] rewards5 = {0, 0, 0, 0};
        long result5 = solution.maxReward(rewards5);
        System.out.println("Input: " + solution.arrayToString(rewards5));
        System.out.println("Maximum total reward: " + result5);
        System.out.println("Expected: 0");
    }
}