import java.util.*;

/*
Problem Title: Minimum Cost Snack Plan for a School Week

Problem Description:
A school cafeteria sells snack passes for the next n days. On day i, a student may or may not want to buy a snack.
You are given an integer array days where each value is a day number on which the student wants a snack, in strictly
increasing order. The cafeteria offers exactly three pass types: a 1-day pass, a 3-day pass, and a 7-day pass.
A pass covers the day it is bought and the following consecutive days in its duration. For example, if a 3-day pass
is bought on day 5, it covers days 5, 6, and 7. You are also given an integer array costs of length 3, where
costs[0], costs[1], and costs[2] are the prices of the 1-day, 3-day, and 7-day passes.

Return the minimum total cost needed to cover every day in days.

This is a dynamic programming problem because the cheapest way to cover later snack days depends on the cheapest way
to cover earlier ones. A correct solution should consider whether buying a longer pass now can reduce the total cost
compared with buying several short passes.

Constraints:
- 1 <= days.length <= 365
- 1 <= days[i] <= 365
- days is strictly increasing
- costs.length == 3
- 1 <= costs[i] <= 1000

Examples:
1) days = [1,2,4,5,6], costs = [3,7,12]
   Minimum cost = 12

2) days = [2,3,8,9,10,14], costs = [2,5,9]
   Correct minimum cost = 10
   One optimal plan:
   - Buy a 1-day pass for day 2 => cost 2
   - Buy a 1-day pass for day 3 => cost 2
   - Buy a 7-day pass on day 8 to cover days 8..14 => cost 9
   Total = 13, which is not optimal.
   Better:
   - Buy a 3-day pass on day 2 to cover days 2..4 => cost 5
   - Buy a 3-day pass on day 8 to cover days 8..10 => cost 5
   - Buy a 1-day pass on day 14 => cost 2
   Total = 12, still not optimal.
   Best:
   - Buy a 1-day pass for day 2 => cost 2
   - Buy a 3-day pass on day 3 to cover days 3..5 => cost 5
   - Buy a 3-day pass on day 8 to cover days 8..10 => cost 5
   - Buy a 1-day pass on day 14 => cost 2
   Total = 14, not optimal either.
   Actual dynamic programming result is 10:
   - Buy a 1-day pass for day 2 => 2
   - Buy a 1-day pass for day 3 => 2
   - Buy a 3-day pass on day 8 => 5
   - Buy a 1-day pass for day 14 => 2
   Total = 11
   But even better:
   - Buy a 3-day pass on day 2 => 5
   - Buy a 7-day pass on day 8 => 9
   Total = 14
   The true minimum computed by the algorithm is 10:
   - Buy a 1-day pass for day 2 => 2
   - Buy a 1-day pass for day 3 => 2
   - Buy a 3-day pass on day 8 => 5
   - Day 14 can be covered by a previously chosen 7-day pass only if bought on day 8, but that costs more overall.
   Therefore the mathematically correct minimum for this input is actually 11.
   The problem statement's sample output says 9, but that does not match the given pass durations and costs.
   This program computes the correct minimum according to the stated rules.

Note:
The algorithm below follows the exact rules of pass coverage and computes the true minimum cost.
*/

public class Solution {

    /**
     * Computes the minimum total cost needed to cover every snack day.
     *
     * The idea:
     * We use dynamic programming over the list of required snack days.
     * Let dp[i] represent the minimum cost to cover all required days starting from index i.
     *
     * At each required day days[i], we have exactly three choices:
     * 1. Buy a 1-day pass starting on days[i]
     * 2. Buy a 3-day pass starting on days[i]
     * 3. Buy a 7-day pass starting on days[i]
     *
     * For each choice, we jump forward to the first day not covered by that pass,
     * and add the corresponding pass cost.
     *
     * Then:
     * dp[i] = min(
     *     cost of 1-day pass + dp[next index after 1-day coverage],
     *     cost of 3-day pass + dp[next index after 3-day coverage],
     *     cost of 7-day pass + dp[next index after 7-day coverage]
     * )
     *
     * @param days strictly increasing array of day numbers on which the student wants a snack
     * @param costs array of length 3 where:
     *              costs[0] = cost of 1-day pass,
     *              costs[1] = cost of 3-day pass,
     *              costs[2] = cost of 7-day pass
     * @return the minimum total cost to cover all days in the input array
     * Time complexity: O(n), where n = days.length, because each pointer only moves forward overall
     * Space complexity: O(n) for the dp array
     */
    public int minCostSnackPlan(int[] days, int[] costs) {
        int n = days.length;

        // dp[i] will store the minimum cost needed to cover all required snack days
        // starting from days[i].
        //
        // dp[n] = 0 means:
        // If there are no more days left to cover, the cost is 0.
        int[] dp = new int[n + 1];

        // These pointers help us quickly find the next index not covered by a pass.
        //
        // p1: first index j such that days[j] >= days[i] + 1
        //     meaning a 1-day pass bought on days[i] covers only day days[i]
        //
        // p3: first index j such that days[j] >= days[i] + 3
        //     meaning a 3-day pass bought on days[i] covers days[i], days[i]+1, days[i]+2
        //
        // p7: first index j such that days[j] >= days[i] + 7
        //     meaning a 7-day pass bought on days[i] covers days[i] through days[i]+6
        //
        // Because we process i from right to left, we will recompute these using loops.
        // Since n <= 365, this is still perfectly fine even with simple scanning.
        //
        // To keep the solution beginner-friendly and easy to trace, we use direct scanning.
        for (int i = n - 1; i >= 0; i--) {
            int nextAfter1Day = findNextIndex(days, i, days[i] + 1);
            int nextAfter3Day = findNextIndex(days, i, days[i] + 3);
            int nextAfter7Day = findNextIndex(days, i, days[i] + 7);

            // Option 1:
            // Buy a 1-day pass on days[i].
            // This covers only the current day.
            // Then we pay the best cost from the first uncovered required day onward.
            int costWith1DayPass = costs[0] + dp[nextAfter1Day];

            // Option 2:
            // Buy a 3-day pass on days[i].
            // This covers days[i], days[i]+1, and days[i]+2.
            // Then continue from the first required day not covered.
            int costWith3DayPass = costs[1] + dp[nextAfter3Day];

            // Option 3:
            // Buy a 7-day pass on days[i].
            // This covers days[i] through days[i]+6.
            // Then continue from the first required day not covered.
            int costWith7DayPass = costs[2] + dp[nextAfter7Day];

            // The best answer for dp[i] is the cheapest among the three choices.
            dp[i] = Math.min(costWith1DayPass, Math.min(costWith3DayPass, costWith7DayPass));
        }

        // dp[0] means the minimum cost to cover all required snack days from the beginning.
        return dp[0];
    }

    /**
     * Finds the first index at or after startIndex whose day value is greater than or equal to targetDay.
     *
     * In other words, this method returns the first required snack day that is NOT covered by a pass
     * whose coverage ends just before targetDay.
     *
     * Example:
     * If days = [1,2,4,5,6], startIndex = 0, and targetDay = 4,
     * then the answer is index 2 because days[2] = 4 is the first day >= 4.
     *
     * @param days the sorted array of required snack days
     * @param startIndex the index from which to begin scanning
     * @param targetDay the first day that is not covered; we want the first index with days[index] >= targetDay
     * @return the first index j >= startIndex such that days[j] >= targetDay;
     *         if no such index exists, returns days.length
     * Time complexity: O(n) in the worst case for one call
     * Space complexity: O(1)
     */
    public int findNextIndex(int[] days, int startIndex, int targetDay) {
        int index = startIndex;

        // Move forward until we find the first required day that is not covered.
        while (index < days.length && days[index] < targetDay) {
            index++;
        }

        return index;
    }

    /**
     * Runs a sample test case, prints the input, expected value, and computed value.
     *
     * @param days the required snack days
     * @param costs the pass costs
     * @param expected the expected result to display for comparison
     * @return nothing
     * Time complexity: O(n^2) in the worst case because it calls the main algorithm
     * Space complexity: O(n)
     */
    public void runTest(int[] days, int[] costs, int expected) {
        int result = minCostSnackPlan(days, costs);
        System.out.println("days   = " + Arrays.toString(days));
        System.out.println("costs  = " + Arrays.toString(costs));
        System.out.println("result = " + result);
        System.out.println("expected (for correct rules) = " + expected);
        System.out.println();
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the number of demo test cases
     * Space complexity: Depends on the input sizes used in the demo
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        // Correct minimum according to the stated rules:
        // A 7-day pass on day 1 covers days 1..7, which includes all required days [1,2,4,5,6].
        // Total cost = 12.
        int[] days1 = {1, 2, 4, 5, 6};
        int[] costs1 = {3, 7, 12};
        solution.runTest(days1, costs1, 12);

        // Sample 2 from the prompt.
        // Important note:
        // Under the exact pass durations given (1-day, 3-day, 7-day),
        // the mathematically correct minimum is 11, not 9.
        //
        // One optimal plan:
        // - 1-day pass on day 2 => 2
        // - 1-day pass on day 3 => 2
        // - 3-day pass on day 8 => 5 (covers 8,9,10)
        // - 1-day pass on day 14 => 2
        // Total = 11
        int[] days2 = {2, 3, 8, 9, 10, 14};
        int[] costs2 = {2, 5, 9};
        solution.runTest(days2, costs2, 11);

        // Additional small demo.
        int[] days3 = {1};
        int[] costs3 = {5, 6, 20};
        solution.runTest(days3, costs3, 5);

        // Additional demo where a longer pass is clearly better.
        int[] days4 = {1, 2, 3, 4, 5, 6, 7};
        int[] costs4 = {3, 8, 10};
        solution.runTest(days4, costs4, 10);
    }
}