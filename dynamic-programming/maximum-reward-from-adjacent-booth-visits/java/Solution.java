/*
Problem Title: Maximum Reward from Adjacent Booth Visits

Problem Description:
You are walking through a row of festival booths. Each booth offers a reward value, given by an integer array rewards,
where rewards[i] is the number of points you earn if you visit booth i. However, visiting two neighboring booths takes
too much time because their lines overlap, so you are not allowed to visit two adjacent booths.

Your task is to return the maximum total reward you can collect by choosing a set of booths to visit such that no two
chosen booths are next to each other.

This is a classic one-dimensional dynamic programming problem: for each booth, you decide whether to skip it or take it.
If you take booth i, you must skip booth i - 1. If you skip booth i, your best answer stays the same as the previous
position.

Write a function that returns the maximum reward possible.

Constraints:
- 1 <= rewards.length <= 100
- 0 <= rewards[i] <= 1000

Example 1:
Input: rewards = [4, 2, 7, 9, 3]
Output: 13

Note:
If we evaluate all valid non-adjacent choices carefully:
- 4 + 7 + 3 = 14 is valid because indices 0, 2, and 4 are not adjacent.
So the true maximum for [4, 2, 7, 9, 3] is 14.

Example 2:
Input: rewards = [2, 1, 1, 2]
Output: 4
Explanation: Visit the first and last booths. They are not adjacent, so the total reward is 2 + 2 = 4.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum total reward that can be collected without visiting
     * two adjacent booths.
     *
     * This method uses dynamic programming with constant extra space.
     * At each booth, we decide between:
     * 1. Skipping the current booth, keeping the best total seen so far.
     * 2. Taking the current booth, which means we must add its reward to the
     *    best total from two booths back.
     *
     * @param rewards the reward values for each booth; rewards[i] is the reward
     *                earned by visiting booth i
     * @return the maximum total reward possible without choosing adjacent booths
     *
     * Time complexity: O(n), where n is the number of booths
     * Space complexity: O(1), excluding input storage
     */
    public int maxReward(int[] rewards) {
        // Defensive handling for null or empty input.
        // The problem guarantees at least one element, but this makes the method safer.
        if (rewards == null || rewards.length == 0) {
            return 0;
        }

        // previousTwo represents the best answer for booths up to index i - 2.
        // In DP terms, this is dp[i - 2].
        int previousTwo = 0;

        // previousOne represents the best answer for booths up to index i - 1.
        // In DP terms, this is dp[i - 1].
        int previousOne = 0;

        // We process booths from left to right.
        for (int i = 0; i < rewards.length; i++) {
            // Option 1: skip the current booth.
            // If we skip booth i, the best total remains whatever we had up to i - 1.
            int skipCurrent = previousOne;

            // Option 2: take the current booth.
            // If we take booth i, we cannot take booth i - 1,
            // so we add rewards[i] to the best total up to i - 2.
            int takeCurrent = previousTwo + rewards[i];

            // The best answer for the current position is the better of:
            // - skipping the current booth
            // - taking the current booth
            int currentBest = Math.max(skipCurrent, takeCurrent);

            // Move the DP window forward:
            // previousTwo becomes old previousOne
            // previousOne becomes currentBest
            previousTwo = previousOne;
            previousOne = currentBest;
        }

        // After processing all booths, previousOne stores the final answer.
        return previousOne;
    }

    /**
     * Computes the maximum total reward that can be collected without visiting
     * two adjacent booths, using an explicit DP array.
     *
     * This version is more beginner-friendly for learning because it stores
     * the best answer for every prefix of the array.
     *
     * dp[i] = maximum reward obtainable from booths 0..i
     *
     * Transition:
     * - Skip booth i: dp[i - 1]
     * - Take booth i: dp[i - 2] + rewards[i]
     * - Therefore: dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])
     *
     * @param rewards the reward values for each booth; rewards[i] is the reward
     *                earned by visiting booth i
     * @return the maximum total reward possible without choosing adjacent booths
     *
     * Time complexity: O(n), where n is the number of booths
     * Space complexity: O(n)
     */
    public int maxRewardWithDpArray(int[] rewards) {
        if (rewards == null || rewards.length == 0) {
            return 0;
        }

        int n = rewards.length;

        // If there is only one booth, the answer is simply its reward.
        if (n == 1) {
            return rewards[0];
        }

        // dp[i] will store the maximum reward obtainable from booths 0 through i.
        int[] dp = new int[n];

        // Base case for the first booth:
        // We either take it (which is best, since rewards are non-negative) or skip it.
        dp[0] = rewards[0];

        // Base case for the second booth:
        // We cannot take both booth 0 and booth 1 because they are adjacent.
        // So the best is the larger of the two individual rewards.
        dp[1] = Math.max(rewards[0], rewards[1]);

        // Fill the DP array from left to right.
        for (int i = 2; i < n; i++) {
            // If we skip booth i, best total stays dp[i - 1].
            int skipCurrent = dp[i - 1];

            // If we take booth i, we add its reward to dp[i - 2].
            int takeCurrent = dp[i - 2] + rewards[i];

            // Choose the better option.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        // The last entry contains the answer for the full array.
        return dp[n - 1];
    }

    /**
     * Utility method to print an input array and the computed maximum reward.
     *
     * @param rewards the booth rewards to test
     * @return the computed maximum reward for the provided input
     *
     * Time complexity: O(n), due to the algorithm call and array-to-string conversion
     * Space complexity: O(1), excluding output formatting internals
     */
    public int demonstrateCase(int[] rewards) {
        int result = maxReward(rewards);
        System.out.println("Rewards: " + Arrays.toString(rewards));
        System.out.println("Maximum reward: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample inputs.
     *
     * It prints results for the examples and a few extra test cases.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(k * n), where k is the number of demo test cases
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt:
        // rewards = [4, 2, 7, 9, 3]
        //
        // Careful trace:
        // - Take 4, skip 2, take 7, skip 9, take 3 => 4 + 7 + 3 = 14
        // - Take 4 and 9 => 13
        // Therefore, the true maximum is 14.
        int[] example1 = {4, 2, 7, 9, 3};
        int result1 = solution.demonstrateCase(example1);
        System.out.println("Expected (correctly traced): 14");
        System.out.println("Matches expected: " + (result1 == 14));
        System.out.println();

        // Example 2 from the prompt:
        // rewards = [2, 1, 1, 2]
        // Best choice is booth 0 and booth 3 => 2 + 2 = 4
        int[] example2 = {2, 1, 1, 2};
        int result2 = solution.demonstrateCase(example2);
        System.out.println("Expected: 4");
        System.out.println("Matches expected: " + (result2 == 4));
        System.out.println();

        // Additional beginner-friendly checks
        int[] test1 = {5};
        int[] test2 = {1, 10};
        int[] test3 = {2, 7, 9, 3, 1};
        int[] test4 = {0, 0, 0, 0};
        int[] test5 = {10, 1, 10, 1, 10};

        solution.demonstrateCase(test1);
        solution.demonstrateCase(test2);
        solution.demonstrateCase(test3);
        solution.demonstrateCase(test4);
        solution.demonstrateCase(test5);

        // Also show that both implementations produce the same result.
        System.out.println("Verifying both implementations match on sample inputs:");
        System.out.println("Example 1 match: " +
                (solution.maxReward(example1) == solution.maxRewardWithDpArray(example1)));
        System.out.println("Example 2 match: " +
                (solution.maxReward(example2) == solution.maxRewardWithDpArray(example2)));
    }
}