import java.util.*;

/*
Problem Title: Minimum Cost to Schedule Factory Robots with Mode Switch Fees

Problem Description:
A factory has n production hours to fill. For each hour i, exactly one robot mode must be active:
mode A or mode B. Running mode A during hour i costs aCost[i], and running mode B during hour i
costs bCost[i]. In addition, switching the active mode between two consecutive hours has an extra
fee switchFee. If the same mode is used in consecutive hours, no switch fee is charged.

Your task is to compute the minimum total cost to schedule all n hours.

More formally, choose a sequence of modes of length n where each entry is either A or B.
The total cost is the sum of the operating cost of the chosen mode at each hour, plus switchFee
for every index i > 0 where the mode at hour i differs from the mode at hour i - 1.

Return the minimum possible total cost.

This is a dynamic programming problem because the best choice for the current hour depends on the
mode chosen in the previous hour. An efficient solution should run in O(n) time.

Constraints:
- 1 <= n <= 100000
- aCost.length == n
- bCost.length == n
- 0 <= aCost[i], bCost[i] <= 1000000000
- 0 <= switchFee <= 1000000000
- The answer fits in a 64-bit signed integer

Important note about the examples:
The textual explanations in the prompt contain arithmetic inconsistencies.
The correct minimum values are determined by the formal problem statement:
- Example 1:
  aCost = [3, 8, 2, 5], bCost = [4, 1, 6, 1], switchFee = 3
  Correct output = 12
- Example 2:
  aCost = [10, 2, 10, 2], bCost = [1, 9, 1, 9], switchFee = 2
  Correct output = 12
*/

public class Solution {

    /**
     * Computes the minimum total cost to schedule robot modes over all hours.
     *
     * Dynamic programming idea:
     * For each hour, we only need to know the minimum total cost if we end that hour in:
     * 1) mode A
     * 2) mode B
     *
     * Let:
     * - dpA = minimum total cost after processing the current hour, with current hour using mode A
     * - dpB = minimum total cost after processing the current hour, with current hour using mode B
     *
     * Transition for hour i:
     * - To end in A at hour i:
     *   We can either:
     *   a) stay in A from hour i - 1, paying no switch fee
     *   b) switch from B to A, paying switchFee
     *   Then add aCost[i]
     *
     *   newDpA = min(dpA + aCost[i], dpB + switchFee + aCost[i])
     *
     * - To end in B at hour i:
     *   We can either:
     *   a) stay in B from hour i - 1, paying no switch fee
     *   b) switch from A to B, paying switchFee
     *   Then add bCost[i]
     *
     *   newDpB = min(dpB + bCost[i], dpA + switchFee + bCost[i])
     *
     * Base case:
     * At hour 0, there is no previous hour, so no switch fee applies.
     * - dpA = aCost[0]
     * - dpB = bCost[0]
     *
     * Final answer:
     * The minimum total cost after the last hour is min(dpA, dpB).
     *
     * @param aCost the operating cost of mode A for each hour
     * @param bCost the operating cost of mode B for each hour
     * @param switchFee the extra fee paid whenever the mode changes between consecutive hours
     * @return the minimum possible total cost to schedule all hours
     * Time complexity: O(n), where n is the number of hours
     * Space complexity: O(1), ignoring input storage
     */
    public long minimumCost(int[] aCost, int[] bCost, int switchFee) {
        validateInput(aCost, bCost);

        int n = aCost.length;

        // Base case:
        // For the very first hour, there is no previous hour.
        // So if we choose mode A, total cost is simply aCost[0].
        // If we choose mode B, total cost is simply bCost[0].
        long dpA = aCost[0];
        long dpB = bCost[0];

        // Process each remaining hour one by one.
        for (int i = 1; i < n; i++) {
            // If we want hour i to use mode A, there are exactly two possibilities:
            //
            // 1) Previous hour also ended in A:
            //    cost = previous dpA + current A operating cost
            //
            // 2) Previous hour ended in B, and we switch to A:
            //    cost = previous dpB + switchFee + current A operating cost
            //
            // We choose the cheaper of these two possibilities.
            long newDpA = Math.min(
                    dpA + aCost[i],
                    dpB + (long) switchFee + aCost[i]
            );

            // Similarly, if we want hour i to use mode B:
            //
            // 1) Stay in B:
            //    cost = previous dpB + current B operating cost
            //
            // 2) Switch from A to B:
            //    cost = previous dpA + switchFee + current B operating cost
            //
            // Again, choose the cheaper option.
            long newDpB = Math.min(
                    dpB + bCost[i],
                    dpA + (long) switchFee + bCost[i]
            );

            // Move the DP window forward:
            // the newly computed values become the current best values.
            dpA = newDpA;
            dpB = newDpB;
        }

        // After processing all hours, the final schedule may end in either A or B.
        // We return the cheaper of the two.
        return Math.min(dpA, dpB);
    }

    /**
     * Validates that the input arrays are non-null, non-empty, and have the same length.
     *
     * @param aCost the operating cost array for mode A
     * @param bCost the operating cost array for mode B
     * @return nothing; throws an exception if input is invalid
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] aCost, int[] bCost) {
        if (aCost == null || bCost == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (aCost.length == 0 || bCost.length == 0) {
            throw new IllegalArgumentException("Input arrays must not be empty.");
        }
        if (aCost.length != bCost.length) {
            throw new IllegalArgumentException("Input arrays must have the same length.");
        }
    }

    /**
     * Runs a single demonstration test case and prints the result.
     *
     * @param aCost the operating cost array for mode A
     * @param bCost the operating cost array for mode B
     * @param switchFee the switching fee
     * @param expected the expected minimum cost for demonstration purposes
     * @return nothing
     * Time complexity: O(n), where n is the number of hours in the test case
     * Space complexity: O(1), ignoring input storage
     */
    public void runDemo(int[] aCost, int[] bCost, int switchFee, long expected) {
        long result = minimumCost(aCost, bCost, switchFee);
        System.out.println("aCost      = " + Arrays.toString(aCost));
        System.out.println("bCost      = " + Arrays.toString(bCost));
        System.out.println("switchFee  = " + switchFee);
        System.out.println("Min cost   = " + result);
        System.out.println("Expected   = " + expected);
        System.out.println("Matches?   = " + (result == expected));
        System.out.println();
    }

    /**
     * Demonstrates the solution on sample inputs.
     *
     * Note:
     * The prompt's example narratives contain inconsistent arithmetic.
     * This main method prints the mathematically correct results according to the formal problem definition.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size across demos)
     * Space complexity: O(1), ignoring input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt:
        // Correct minimum is 12.
        solution.runDemo(
                new int[]{3, 8, 2, 5},
                new int[]{4, 1, 6, 1},
                3,
                12L
        );

        // Example 2 from the prompt:
        // Correct minimum is 12.
        solution.runDemo(
                new int[]{10, 2, 10, 2},
                new int[]{1, 9, 1, 9},
                2,
                12L
        );

        // Additional small sanity check:
        // If switch fee is 0, each hour can independently choose the cheaper mode.
        solution.runDemo(
                new int[]{3, 8, 2, 5},
                new int[]{4, 1, 6, 1},
                0,
                7L
        );
    }
}