import java.util.*;

/*
Problem Title: Maximum Coins from Non-Adjacent Arcade Machines

Problem Description:
You are managing a row of arcade machines in a game center. Each machine contains a certain
number of collectible coins, given by the array coins, where coins[i] is the number of coins
inside the ith machine. If you collect coins from one machine, the security system prevents
you from collecting from its immediate neighboring machines on the same night.

Your task is to determine the maximum number of coins you can collect in one night without
collecting from two adjacent machines.

This is a classic decision-making problem where, for each machine, you can either skip it
or collect from it and then skip its neighbor. Return the largest total number of coins possible.

Constraints:
- 1 <= coins.length <= 100
- 0 <= coins[i] <= 1000

Example 1:
Input: coins = [4, 2, 7, 9, 3]
Output: 14

Explanation:
We cannot collect from adjacent machines.
Possible strong choices include:
- 4 + 7 + 3 = 14
- 4 + 9 = 13
- 7 + 3 = 10
The maximum valid total is 14.

Example 2:
Input: coins = [10, 1, 1, 10]
Output: 20

Explanation:
Collect from the first and last machines.
They are not adjacent, so the total is 10 + 10 = 20.

Notes:
- If there is only one machine, the answer is simply the number of coins in that machine.
- Machines may contain 0 coins, and it may be better to skip several machines.
- An efficient solution should use dynamic programming to build the best answer from smaller
  prefixes of the array.
*/

public class Solution {

    /**
     * Computes the maximum number of coins that can be collected from a row of arcade machines
     * such that no two collected machines are adjacent.
     *
     * The dynamic programming idea is:
     * For each machine index i, we decide between:
     * 1) Skipping machine i, so the best total remains the same as for the previous machine.
     * 2) Taking machine i, so we add coins[i] to the best total from i - 2.
     *
     * We store:
     * dp[i] = maximum coins collectable considering machines from index 0 to index i.
     *
     * Transition:
     * dp[i] = max(dp[i - 1], dp[i - 2] + coins[i])
     *
     * @param coins an array where coins[i] is the number of coins in the ith arcade machine
     * @return the maximum number of coins that can be collected without taking from adjacent machines
     * Time complexity: O(n), where n is the number of machines
     * Space complexity: O(n), due to the DP array
     */
    public int maxCoins(int[] coins) {
        // Defensive handling:
        // Although the constraints guarantee at least one element,
        // this check makes the method safer and easier to reuse.
        if (coins == null || coins.length == 0) {
            return 0;
        }

        // If there is only one machine, the answer is simply its coin count.
        if (coins.length == 1) {
            return coins[0];
        }

        // Create a DP array where:
        // dp[i] = best answer considering machines from 0 through i.
        int[] dp = new int[coins.length];

        // Base case for the first machine:
        // If we only look at machine 0, the best we can do is take it.
        dp[0] = coins[0];

        // Base case for the second machine:
        // We cannot take both machine 0 and machine 1 because they are adjacent.
        // So the best answer is the larger of the two individual values.
        dp[1] = Math.max(coins[0], coins[1]);

        // Process the rest of the machines one by one.
        for (int i = 2; i < coins.length; i++) {
            // Option 1: Skip the current machine.
            // Then our total remains whatever was best up to the previous machine.
            int skipCurrent = dp[i - 1];

            // Option 2: Take the current machine.
            // Then we must skip the previous machine, so we add coins[i]
            // to the best answer up to i - 2.
            int takeCurrent = dp[i - 2] + coins[i];

            // Choose the better of the two options.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        // The last DP entry contains the best answer for the full array.
        return dp[coins.length - 1];
    }

    /**
     * Computes the maximum number of coins that can be collected without taking from adjacent
     * machines, using an optimized dynamic programming approach with constant extra space.
     *
     * Instead of storing the entire DP array, we only keep track of:
     * - prevTwo: the best answer up to index i - 2
     * - prevOne: the best answer up to index i - 1
     *
     * For each machine, the current best is:
     * current = max(prevOne, prevTwo + coins[i])
     *
     * This works because each DP state depends only on the previous two states.
     *
     * @param coins an array where coins[i] is the number of coins in the ith arcade machine
     * @return the maximum number of coins that can be collected without taking from adjacent machines
     * Time complexity: O(n), where n is the number of machines
     * Space complexity: O(1), excluding input storage
     */
    public int maxCoinsOptimized(int[] coins) {
        // Safe handling for empty or null input.
        if (coins == null || coins.length == 0) {
            return 0;
        }

        // If there is only one machine, return its value directly.
        if (coins.length == 1) {
            return coins[0];
        }

        // prevTwo represents dp[i - 2].
        int prevTwo = coins[0];

        // prevOne represents dp[i - 1].
        int prevOne = Math.max(coins[0], coins[1]);

        // Iterate from the third machine onward.
        for (int i = 2; i < coins.length; i++) {
            // If we take the current machine, we add its coins to prevTwo.
            int takeCurrent = prevTwo + coins[i];

            // If we skip the current machine, the best remains prevOne.
            int skipCurrent = prevOne;

            // Current best answer for this position.
            int current = Math.max(takeCurrent, skipCurrent);

            // Shift the window forward:
            // old prevOne becomes new prevTwo,
            // current becomes new prevOne.
            prevTwo = prevOne;
            prevOne = current;
        }

        // prevOne now holds the best answer for the full array.
        return prevOne;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * This method also includes a few extra test cases to help beginners see how
     * the algorithm behaves on edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration set, or O(k * n) in general
     *                  for k test arrays each of length n
     * Space complexity: O(1) extra space beyond the arrays used in the demo
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] coins1 = {4, 2, 7, 9, 3};
        int result1 = solution.maxCoins(coins1);
        System.out.println("Input: " + solution.arrayToString(coins1));
        System.out.println("Maximum coins: " + result1);
        System.out.println("Expected: 14");
        System.out.println();

        // Sample input 2 from the problem statement.
        int[] coins2 = {10, 1, 1, 10};
        int result2 = solution.maxCoins(coins2);
        System.out.println("Input: " + solution.arrayToString(coins2));
        System.out.println("Maximum coins: " + result2);
        System.out.println("Expected: 20");
        System.out.println();

        // Extra test: single machine.
        int[] coins3 = {8};
        int result3 = solution.maxCoins(coins3);
        System.out.println("Input: " + solution.arrayToString(coins3));
        System.out.println("Maximum coins: " + result3);
        System.out.println("Expected: 8");
        System.out.println();

        // Extra test: includes zeros.
        int[] coins4 = {0, 0, 10, 0, 5};
        int result4 = solution.maxCoins(coins4);
        System.out.println("Input: " + solution.arrayToString(coins4));
        System.out.println("Maximum coins: " + result4);
        System.out.println("Expected: 15");
        System.out.println();

        // Extra test using the optimized method to show it matches.
        int[] coins5 = {2, 1, 4, 9};
        int result5 = solution.maxCoinsOptimized(coins5);
        System.out.println("Input: " + solution.arrayToString(coins5));
        System.out.println("Maximum coins (optimized): " + result5);
        System.out.println("Expected: 11");
    }
}