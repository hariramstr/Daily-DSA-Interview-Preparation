import java.util.*;

/*
Problem Title: Maximum Reward from Skipping Adjacent Milestones

Problem Description:
You are planning a roadshow with n milestone events arranged in a fixed order along a route.
Attending milestone i gives you a reward value rewards[i], which may be positive, zero, or negative.
However, due to travel fatigue and scheduling limits, you are not allowed to attend two adjacent milestones.
You may also choose to skip any milestone.

Your task is to return the maximum total reward you can collect.

In addition to the maximum reward, think carefully about edge cases: if all rewards are negative,
it is valid to attend no milestones at all, resulting in a total reward of 0. The order of milestones
cannot be changed.

Formally, choose a subset of indices such that no two chosen indices are adjacent, and the sum of
their reward values is maximized.

Constraints:
- 1 <= n <= 100000
- -10000 <= rewards[i] <= 10000
- The solution should run in O(n) time.
- Aim for O(1) extra space if possible.

Example 1:
Input: rewards = [5, 1, 2, 10, 6]
Output: 15
Explanation: Attend milestones with rewards 5 and 10 for a total of 15. Attending 5, 2, and 6 is only 13.

Example 2:
Input: rewards = [-4, -2, -7]
Output: 0
Explanation: Every milestone reduces the total reward, so the best choice is to skip all of them.
*/

public class Solution {

    /**
     * Computes the maximum total reward that can be collected by choosing
     * non-adjacent milestones.
     *
     * The key dynamic programming idea is:
     * For each milestone, we have exactly two choices:
     * 1. Skip it -> keep the best answer we already had up to the previous milestone.
     * 2. Take it -> add its reward to the best answer from two milestones earlier.
     *
     * Because we only ever need the previous two DP states, we can optimize space
     * from O(n) down to O(1).
     *
     * Important detail:
     * We allow the answer to be 0, which represents taking no milestones at all.
     * This is necessary when all rewards are negative.
     *
     * @param rewards the reward values for milestones in fixed order; rewards[i] is the reward for milestone i
     * @return the maximum total reward obtainable without selecting adjacent milestones
     * Time complexity: O(n), where n is the number of milestones
     * Space complexity: O(1), excluding input storage
     */
    public int maxReward(int[] rewards) {
        // Defensive handling:
        // Although the constraints guarantee at least one element, it is still good
        // beginner-friendly practice to safely handle null or empty input.
        if (rewards == null || rewards.length == 0) {
            return 0;
        }

        // prevTwo represents the best answer considering milestones up to index i - 2.
        // At the very beginning, before processing anything, the best reward is 0
        // because we are allowed to attend no milestones.
        int prevTwo = 0;

        // prevOne represents the best answer considering milestones up to index i - 1.
        // Initially this is also 0 for the same reason.
        int prevOne = 0;

        // Process milestones from left to right.
        for (int reward : rewards) {
            // Option 1: skip the current milestone.
            // If we skip it, the best total remains whatever the best was up to the previous milestone.
            int skipCurrent = prevOne;

            // Option 2: take the current milestone.
            // If we take it, we cannot take the previous milestone,
            // so we add the current reward to the best answer from two positions back.
            int takeCurrent = prevTwo + reward;

            // The best answer at this position is the better of:
            // - skipping the current milestone
            // - taking the current milestone
            //
            // Because prevOne starts at 0 and always stores the best valid answer so far,
            // the result can never go below 0. This naturally handles all-negative arrays.
            int currentBest = Math.max(skipCurrent, takeCurrent);

            // Shift the window forward:
            // - what used to be "previous one" becomes "previous two"
            // - current best becomes the new "previous one"
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all milestones, prevOne holds the best answer for the full array.
        return prevOne;
    }

    /**
     * A second version using an explicit DP array.
     *
     * This method is not required for optimal space, but it is often easier for beginners
     * to understand because it stores the best answer for every prefix of the array.
     *
     * DP definition:
     * dp[i] = maximum reward obtainable from the first i milestones
     *         (that is, considering rewards[0] through rewards[i - 1])
     *
     * Transition:
     * dp[i] = max(
     *     dp[i - 1],                  // skip current milestone
     *     dp[i - 2] + rewards[i - 1]  // take current milestone
     * )
     *
     * Base cases:
     * dp[0] = 0  -> no milestones available
     * dp[1] = max(0, rewards[0]) -> either take the first milestone or skip it
     *
     * @param rewards the reward values for milestones in fixed order
     * @return the maximum total reward obtainable without selecting adjacent milestones
     * Time complexity: O(n), where n is the number of milestones
     * Space complexity: O(n)
     */
    public int maxRewardWithDpArray(int[] rewards) {
        if (rewards == null || rewards.length == 0) {
            return 0;
        }

        int n = rewards.length;

        // dp[i] means best answer using first i elements.
        int[] dp = new int[n + 1];

        // No milestones -> reward 0.
        dp[0] = 0;

        // First milestone only:
        // either take it or skip it.
        dp[1] = Math.max(0, rewards[0]);

        // Build the DP table from left to right.
        for (int i = 2; i <= n; i++) {
            int skipCurrent = dp[i - 1];
            int takeCurrent = dp[i - 2] + rewards[i - 1];
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        return dp[n];
    }

    /**
     * Converts an int array into a readable string representation.
     *
     * This helper is used only for demonstration output in main.
     *
     * @param arr the array to convert into a string
     * @return a human-readable string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional edge cases.
     *
     * This method prints:
     * - the input array
     * - the computed maximum reward
     *
     * Verified sample traces:
     * 1) [5, 1, 2, 10, 6] -> 15
     *    Best choice is 5 + 10 = 15
     *
     * 2) [-4, -2, -7] -> 0
     *    Best choice is to skip all milestones
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) overall for k demo arrays of average size n
     * Space complexity: O(1) extra, excluding printing and array literals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sample1 = {5, 1, 2, 10, 6};
        int[] sample2 = {-4, -2, -7};

        System.out.println("Sample 1:");
        System.out.println("Rewards: " + solution.arrayToString(sample1));
        System.out.println("Maximum reward: " + solution.maxReward(sample1));
        System.out.println();

        System.out.println("Sample 2:");
        System.out.println("Rewards: " + solution.arrayToString(sample2));
        System.out.println("Maximum reward: " + solution.maxReward(sample2));
        System.out.println();

        // Additional demonstrations for beginner-friendly validation.
        int[] test1 = {2};
        int[] test2 = {-5};
        int[] test3 = {2, 7, 9, 3, 1};
        int[] test4 = {0, 0, 0, 0};
        int[] test5 = {4, -1, 3, -2, 8};

        System.out.println("Additional Test 1:");
        System.out.println("Rewards: " + solution.arrayToString(test1));
        System.out.println("Maximum reward: " + solution.maxReward(test1));
        System.out.println();

        System.out.println("Additional Test 2:");
        System.out.println("Rewards: " + solution.arrayToString(test2));
        System.out.println("Maximum reward: " + solution.maxReward(test2));
        System.out.println();

        System.out.println("Additional Test 3:");
        System.out.println("Rewards: " + solution.arrayToString(test3));
        System.out.println("Maximum reward: " + solution.maxReward(test3));
        System.out.println();

        System.out.println("Additional Test 4:");
        System.out.println("Rewards: " + solution.arrayToString(test4));
        System.out.println("Maximum reward: " + solution.maxReward(test4));
        System.out.println();

        System.out.println("Additional Test 5:");
        System.out.println("Rewards: " + solution.arrayToString(test5));
        System.out.println("Maximum reward: " + solution.maxReward(test5));
    }
}