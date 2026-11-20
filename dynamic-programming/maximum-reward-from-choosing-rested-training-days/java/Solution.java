/*
Problem Title: Maximum Reward from Choosing Rested Training Days

Problem Description:
A sports analytics team is building a planner for an athlete preparing for a tournament.
You are given an integer array rewards where rewards[i] is the performance benefit of
training on day i. Some days may even have negative values, meaning training that day
would hurt recovery more than it helps. The athlete may choose any subset of days to
train, but cannot train on two consecutive days because each training day requires a
full recovery day afterward.

Your task is to return the maximum total reward the athlete can earn.

Unlike a simple greedy choice, the best answer may require skipping a large positive day
in order to combine several other days more effectively. If all values are negative, the
athlete is allowed to train on no days at all, so the answer can be 0.

Write a function that computes this maximum reward.

Constraints:
- 1 <= rewards.length <= 100000
- -10000 <= rewards[i] <= 10000
- Your solution should run in O(n) time
- Aim for O(1) extra space if possible

Example 1:
Input: rewards = [5, 1, 1, 5]
Output: 10
Explanation: Train on days 0 and 3 for a total reward of 5 + 5 = 10. Training on adjacent
days is not allowed.

Example 2:
Input: rewards = [4, -2, 7, 3, -1, 8]
Output: 19
Explanation: One optimal plan is to train on days 0, 2, and 5. The total reward is
4 + 7 + 8 = 19.

This problem is about finding the best value up to each position while deciding whether
to take or skip the current day. A correct solution should carefully handle negative
values and the option of selecting no days at all.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum total reward the athlete can earn without training on
     * two consecutive days.
     *
     * The idea is dynamic programming with constant extra space:
     * - Let prevTwo represent the best answer considering days up to i - 2.
     * - Let prevOne represent the best answer considering days up to i - 1.
     * - For the current day i, we have two choices:
     *   1) Skip day i, so the best total remains prevOne.
     *   2) Train on day i, so we must add rewards[i] to prevTwo.
     * - The best answer at day i is the maximum of those two choices.
     *
     * We also naturally support the "train on no days" option because the running
     * best value never needs to go below 0.
     *
     * @param rewards an array where rewards[i] is the reward for training on day i
     * @return the maximum total reward obtainable without choosing adjacent days
     * Time complexity: O(n), where n is the length of rewards
     * Space complexity: O(1), ignoring input storage
     */
    public int maxReward(int[] rewards) {
        // Defensive handling:
        // Although the constraints guarantee at least one element, it is good
        // beginner-friendly practice to safely handle null or empty input.
        if (rewards == null || rewards.length == 0) {
            return 0;
        }

        // prevTwo = best answer up to day i - 2
        // At the very beginning, before processing any day, the best reward is 0
        // because the athlete may choose not to train at all.
        int prevTwo = 0;

        // prevOne = best answer up to day i - 1
        // Initially also 0, because before processing the first day, no reward
        // has been collected yet.
        int prevOne = 0;

        // Process each day exactly once from left to right.
        for (int i = 0; i < rewards.length; i++) {
            // Option 1: skip the current day.
            // If we skip day i, then the best total reward remains whatever the
            // best answer was up to day i - 1.
            int skipCurrentDay = prevOne;

            // Option 2: train on the current day.
            // If we train on day i, then day i - 1 cannot be chosen.
            // Therefore, the best compatible previous total is prevTwo.
            // We add the current day's reward to that.
            int takeCurrentDay = prevTwo + rewards[i];

            // The best answer up to the current day is the better of:
            // - skipping this day
            // - taking this day
            //
            // This automatically handles negative values:
            // if rewards[i] is negative and hurts the total, skipping will be better.
            int currentBest = Math.max(skipCurrentDay, takeCurrentDay);

            // Move the rolling window forward:
            // - The old prevOne becomes the new prevTwo
            // - currentBest becomes the new prevOne
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // prevOne now stores the best answer considering all days.
        return prevOne;
    }

    /**
     * A second version of the same algorithm that explicitly builds a DP array.
     * This is less space-efficient than maxReward, but it can be easier for
     * beginners to understand because it stores the best answer for every index.
     *
     * dp[i] = maximum reward obtainable considering days 0..i
     *
     * Transition:
     * - Skip day i: dp[i - 1]
     * - Take day i: rewards[i] + dp[i - 2] (or just rewards[i] if i < 2)
     * - dp[i] = max(skip, take)
     *
     * We also ensure the answer never drops below 0, which represents choosing
     * no training days at all.
     *
     * @param rewards an array where rewards[i] is the reward for training on day i
     * @return the maximum total reward obtainable without choosing adjacent days
     * Time complexity: O(n), where n is the length of rewards
     * Space complexity: O(n)
     */
    public int maxRewardWithDpArray(int[] rewards) {
        if (rewards == null || rewards.length == 0) {
            return 0;
        }

        int n = rewards.length;
        int[] dp = new int[n];

        // Base case for day 0:
        // Either train on day 0 and get rewards[0], or train on no days and get 0.
        dp[0] = Math.max(0, rewards[0]);

        if (n == 1) {
            return dp[0];
        }

        // Base case for day 1:
        // We cannot take both day 0 and day 1 together because they are adjacent.
        // So the best among:
        // - previous best dp[0]
        // - taking only day 1 => rewards[1]
        // - taking no days => 0
        dp[1] = Math.max(dp[0], Math.max(0, rewards[1]));

        // Fill the DP table from left to right.
        for (int i = 2; i < n; i++) {
            int skipCurrentDay = dp[i - 1];
            int takeCurrentDay = dp[i - 2] + rewards[i];
            dp[i] = Math.max(skipCurrentDay, takeCurrentDay);
        }

        return dp[n - 1];
    }

    /**
     * Converts an int array to a readable string for printing.
     *
     * @param arr the array to convert
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases shown here
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement
        int[] rewards1 = {5, 1, 1, 5};
        int result1 = solution.maxReward(rewards1);
        System.out.println("Input: rewards = " + solution.arrayToString(rewards1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 10");
        System.out.println();

        // Quick correctness trace for Example 1:
        // Day 0: best = max(0, 5) = 5
        // Day 1: best = max(5, 0 + 1) = 5
        // Day 2: best = max(5, 5 + 1) = 6
        // Day 3: best = max(6, 5 + 5) = 10
        // Final answer = 10

        // Example 2 from the problem statement
        int[] rewards2 = {4, -2, 7, 3, -1, 8};
        int result2 = solution.maxReward(rewards2);
        System.out.println("Input: rewards = " + solution.arrayToString(rewards2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 19");
        System.out.println();

        // Quick correctness trace for Example 2:
        // Day 0: best = max(0, 4) = 4
        // Day 1: best = max(4, 0 + (-2)) = 4
        // Day 2: best = max(4, 4 + 7) = 11
        // Day 3: best = max(11, 4 + 3) = 11
        // Day 4: best = max(11, 11 + (-1)) = 11
        // Day 5: best = max(11, 11 + 8) = 19
        // Final answer = 19

        // Extra test: all negative values
        int[] rewards3 = {-5, -1, -8};
        int result3 = solution.maxReward(rewards3);
        System.out.println("Input: rewards = " + solution.arrayToString(rewards3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Extra test: single positive day
        int[] rewards4 = {9};
        int result4 = solution.maxReward(rewards4);
        System.out.println("Input: rewards = " + solution.arrayToString(rewards4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 9");
        System.out.println();

        // Extra test: single negative day
        int[] rewards5 = {-9};
        int result5 = solution.maxReward(rewards5);
        System.out.println("Input: rewards = " + solution.arrayToString(rewards5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 0");
        System.out.println();

        // Extra test: compare both implementations on the same input
        int[] rewards6 = {2, 1, 4, 9, 2};
        int result6a = solution.maxReward(rewards6);
        int result6b = solution.maxRewardWithDpArray(rewards6);
        System.out.println("Input: rewards = " + solution.arrayToString(rewards6));
        System.out.println("Output using O(1) space DP: " + result6a);
        System.out.println("Output using DP array: " + result6b);
        System.out.println("Expected: 11");
    }
}