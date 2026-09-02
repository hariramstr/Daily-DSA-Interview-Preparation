import java.util.*;

/*
Title: Minimum Energy to Finish a Workout Plan

Problem Description:
You are given a workout plan represented by an integer array energy, where energy[i] is the energy cost of completing exercise i.
You start before the first exercise and want to finish by reaching just beyond the last exercise.
On each move, you may complete either the next 1 exercise or the next 2 exercises.
If you land on an exercise, you must pay its energy cost.

Your goal is to find the minimum total energy needed to finish the plan.

If you skip directly over an exercise by taking a 2-exercise move, you do not pay the cost of the skipped exercise.
This means you should choose a path that minimizes the sum of the costs of the exercises you actually land on.

Constraints:
- 2 <= energy.length <= 1000
- 0 <= energy[i] <= 999
- The answer fits in a 32-bit integer.

Examples:
1) Input: energy = [4, 1, 6, 2]
   Output: 3
   Explanation: One optimal path is to land on exercise 1 (cost 1), then exercise 3 (cost 2), then finish.
   Total energy = 1 + 2 = 3.

2) Input: energy = [3, 5, 2, 1, 4]
   Output: 6
   Explanation: One optimal path is to land on exercise 0 (cost 3), then exercise 2 (cost 2), then exercise 3 (cost 1), then finish.
   Total energy = 6.

Dynamic Programming Idea:
Let dp[i] represent the minimum energy needed to land on exercise i.
To land on exercise i, we must come from:
- exercise i - 1, or
- exercise i - 2

So:
dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])

Base cases:
- dp[0] = energy[0]
- dp[1] = energy[1]

Why is dp[1] = energy[1]?
Because from the starting position (before index 0), we are allowed to move 2 exercises directly and land on index 1,
paying only energy[1].

Finally, to finish the workout plan, we can move beyond the last exercise from either:
- the last exercise, or
- the second-to-last exercise

Therefore, the answer is:
min(dp[n - 1], dp[n - 2])
*/

public class Solution {

    /**
     * Computes the minimum total energy required to finish the workout plan
     * using dynamic programming with O(n) extra space.
     *
     * The key idea is:
     * - dp[i] stores the minimum energy needed to land exactly on exercise i.
     * - To reach exercise i, we can come from exercise i - 1 or i - 2.
     * - If we land on i, we must pay energy[i].
     *
     * Transition:
     * dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])
     *
     * Base cases:
     * dp[0] = energy[0]
     * dp[1] = energy[1]
     *
     * Final answer:
     * We finish by stepping beyond the last exercise.
     * That final move can start from either:
     * - the last exercise (n - 1), or
     * - the second-to-last exercise (n - 2)
     *
     * So the result is:
     * min(dp[n - 1], dp[n - 2])
     *
     * @param energy an array where energy[i] is the energy cost of exercise i
     * @return the minimum total energy needed to finish the workout plan
     * Time complexity: O(n), because we process each exercise once
     * Space complexity: O(n), because we store a DP array of size n
     */
    public int minEnergy(int[] energy) {
        int n = energy.length;

        // dp[i] will store the minimum energy required to land on exercise i.
        int[] dp = new int[n];

        // Base case 1:
        // If we land on exercise 0, we must pay its cost.
        dp[0] = energy[0];

        // Base case 2:
        // We can jump directly from the start to exercise 1 in one 2-exercise move.
        // In that case, we only pay energy[1].
        dp[1] = energy[1];

        // Fill the DP table from left to right.
        for (int i = 2; i < n; i++) {
            // To land on exercise i:
            // - either come from i - 1
            // - or come from i - 2
            //
            // We choose the cheaper of those two ways, then add the cost of
            // landing on the current exercise i.
            dp[i] = energy[i] + Math.min(dp[i - 1], dp[i - 2]);
        }

        // To finish the workout, we move beyond the last exercise.
        // That final move can be made from either:
        // - the last exercise (index n - 1), or
        // - the second-to-last exercise (index n - 2)
        //
        // We do NOT pay any extra cost for moving beyond the array.
        return Math.min(dp[n - 1], dp[n - 2]);
    }

    /**
     * Computes the minimum total energy required to finish the workout plan
     * using dynamic programming with constant extra space.
     *
     * This method is an optimized version of the DP solution.
     * Instead of storing the entire dp array, we only keep track of:
     * - the minimum cost to land on the previous exercise
     * - the minimum cost to land on the exercise before that
     *
     * Since each state depends only on the previous two states,
     * this is enough.
     *
     * @param energy an array where energy[i] is the energy cost of exercise i
     * @return the minimum total energy needed to finish the workout plan
     * Time complexity: O(n), because we process each exercise once
     * Space complexity: O(1), because we use only a few variables
     */
    public int minEnergyOptimized(int[] energy) {
        int n = energy.length;

        // prev2 represents dp[i - 2]
        // Initially, for i = 2, dp[0] = energy[0]
        int prev2 = energy[0];

        // prev1 represents dp[i - 1]
        // Initially, for i = 2, dp[1] = energy[1]
        int prev1 = energy[1];

        // Process exercises from index 2 onward.
        for (int i = 2; i < n; i++) {
            // Current minimum cost to land on exercise i.
            int current = energy[i] + Math.min(prev1, prev2);

            // Shift the window forward:
            // old prev1 becomes new prev2
            // current becomes new prev1
            prev2 = prev1;
            prev1 = current;
        }

        // Just like in the full DP solution, we can finish from either
        // the last or second-to-last exercise.
        return Math.min(prev1, prev2);
    }

    /**
     * Helper method to print an input array and the computed minimum energy.
     * This is used only for demonstration in main.
     *
     * @param energy the workout energy array to test
     * @return the computed minimum energy for the given input
     * Time complexity: O(n), because the underlying algorithm is O(n)
     * Space complexity: O(1), excluding the input array
     */
    public int demonstrate(int[] energy) {
        int result = minEnergyOptimized(energy);
        System.out.println("energy = " + Arrays.toString(energy));
        System.out.println("Minimum energy = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample inputs and a few extra tests.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) for k demonstrations, each taking O(n)
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        // energy = [4, 1, 6, 2]
        // Optimal path:
        // start -> index 1 (cost 1) -> index 3 (cost 2) -> finish
        // total = 3
        int[] energy1 = {4, 1, 6, 2};
        solution.demonstrate(energy1);

        // Sample input 2 from the problem statement.
        // energy = [3, 5, 2, 1, 4]
        // One optimal path:
        // start -> index 0 (3) -> index 2 (2) -> index 3 (1) -> finish
        // total = 6
        int[] energy2 = {3, 5, 2, 1, 4};
        solution.demonstrate(energy2);

        // Additional beginner-friendly test:
        // We can jump to index 1 directly, then finish.
        int[] energy3 = {10, 2};
        solution.demonstrate(energy3);

        // Additional test with zeros.
        int[] energy4 = {0, 0, 1, 0};
        solution.demonstrate(energy4);

        // Verify both implementations produce the same result on sample inputs.
        System.out.println("Verification:");
        System.out.println("Sample 1 -> DP array method: " + solution.minEnergy(energy1)
                + ", Optimized method: " + solution.minEnergyOptimized(energy1));
        System.out.println("Sample 2 -> DP array method: " + solution.minEnergy(energy2)
                + ", Optimized method: " + solution.minEnergyOptimized(energy2));
    }
}