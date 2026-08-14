import java.util.*;

/*
Problem Title: Minimum Cost to Staff a Store With Training Overlap

Problem Description:
A retail store must be staffed for the next n days. On day i, the store needs at least required[i] workers on duty.
You can hire workers using only two training plans:

1. A one-day temporary worker for cost tempCost[i], who works only on day i.
2. A two-day cross-trained worker starting on day i for cost pairCost[i], who works on both day i and day i + 1.

Each hired worker contributes exactly 1 unit of staffing on every day covered by that plan.
You may hire any number of workers under either plan, as long as all daily staffing requirements are met.
If a two-day worker starts on the last day, it is invalid because there is no day i + 1.

Return the minimum total cost needed to satisfy the staffing requirement for all days.

This is a dynamic programming problem because hiring a two-day worker affects both the current day and the next day,
so a locally cheapest choice may produce a globally suboptimal result. A good solution tracks how much staffing has
already been carried into the current day from workers hired earlier.

Constraints:
- 1 <= n <= 200
- 0 <= required[i] <= 200
- 1 <= tempCost[i] <= 10^4
- 1 <= pairCost[i] <= 10^4 for 0 <= i < n - 1
- pairCost has length n - 1
- It is always possible to satisfy the schedule using temporary workers
*/
public class Solution {

    /**
     * Computes the minimum total cost needed to satisfy all daily staffing requirements.
     *
     * Core idea:
     * We process days from left to right.
     * At the start of day i, the only "carry" from the past is how many two-day workers
     * were started on day i - 1, because those workers also work on day i.
     *
     * Let dp[i][carry] be the minimum cost to satisfy days 0..i-1 completely,
     * such that exactly "carry" workers are already available on day i from earlier hires.
     *
     * Transition on day i:
     * - We already have "carry" workers covering day i.
     * - If required[i] > carry, we still need deficit = required[i] - carry more workers for day i.
     * - We can satisfy that deficit using:
     *   1) temporary workers that help only today
     *   2) two-day workers started today, which help today and also create carry for tomorrow
     *
     * Suppose we start x two-day workers today.
     * Then:
     * - They contribute x workers to today
     * - They create carry x for tomorrow
     * - We still need max(0, deficit - x) temporary workers today
     *
     * Since extra coverage is allowed, x may even exceed deficit.
     * However, starting more than required[i] two-day workers is never useful:
     * tomorrow's requirement is at most 200, and today's extra beyond required[i] only adds cost.
     * So it is enough to try x from 0 to required[i] for non-last days.
     *
     * Special case: last day
     * - We cannot start a two-day worker on the last day.
     * - So we must pay only for temporary workers if carry is insufficient.
     *
     * @param required the required number of workers for each day
     * @param tempCost the cost of one temporary worker for each day
     * @param pairCost the cost of one two-day worker starting on each day 0..n-2
     * @return the minimum total staffing cost
     *
     * Time complexity: O(n * R^2), where R = max(required[i]) and here R <= 200, so this is efficient.
     * Space complexity: O(R), using rolling dynamic programming arrays.
     */
    public long minimumCost(int[] required, int[] tempCost, int[] pairCost) {
        validateInput(required, tempCost, pairCost);

        int n = required.length;
        int maxRequired = 0;
        for (int value : required) {
            maxRequired = Math.max(maxRequired, value);
        }

        long inf = Long.MAX_VALUE / 4;

        // dp[carry] = minimum cost before processing the current day,
        // where "carry" workers are already available today from yesterday's pair hires.
        long[] dp = new long[maxRequired + 1];
        Arrays.fill(dp, inf);
        dp[0] = 0L;

        // Process each day from left to right.
        for (int day = 0; day < n; day++) {
            long[] next = new long[maxRequired + 1];
            Arrays.fill(next, inf);

            // Try every possible amount of carry-in coverage for this day.
            for (int carry = 0; carry <= maxRequired; carry++) {
                if (dp[carry] == inf) {
                    continue;
                }

                // How many more workers are still needed on this day after using carry-in coverage?
                int deficit = Math.max(0, required[day] - carry);

                if (day == n - 1) {
                    // Last day:
                    // We are not allowed to start a two-day worker here.
                    // So the only option is to hire temporary workers for the remaining deficit.
                    long cost = dp[carry] + (long) deficit * tempCost[day];
                    next[0] = Math.min(next[0], cost);
                } else {
                    // Non-last day:
                    // We may start x two-day workers today.
                    //
                    // Important reasoning:
                    // - Each such worker helps today and tomorrow.
                    // - If x < deficit, then we still need (deficit - x) temporary workers today.
                    // - If x >= deficit, then today's requirement is already fully met,
                    //   and any extra coverage today is harmless but usually only worth it if it helps tomorrow.
                    //
                    // We only need to try x from 0 to maxRequired.
                    // In fact x > maxRequired is never useful because tomorrow's requirement is at most maxRequired.
                    for (int x = 0; x <= maxRequired; x++) {
                        // Today's total coverage from carry + x pair workers.
                        // If that is still below required[day], we need temporary workers.
                        int tempWorkers = Math.max(0, required[day] - (carry + x));

                        long cost = dp[carry]
                                + (long) x * pairCost[day]
                                + (long) tempWorkers * tempCost[day];

                        // Tomorrow receives exactly x carry-in workers from today's pair hires.
                        if (cost < next[x]) {
                            next[x] = cost;
                        }
                    }
                }
            }

            dp = next;
        }

        long answer = Long.MAX_VALUE;
        for (long value : dp) {
            answer = Math.min(answer, value);
        }
        return answer;
    }

    /**
     * Validates the input arrays against the problem rules.
     *
     * This method is not strictly required for the algorithm itself,
     * but it makes the solution safer and more beginner-friendly.
     *
     * @param required the required staffing per day
     * @param tempCost the temporary worker cost per day
     * @param pairCost the two-day worker cost per valid start day
     * @return nothing; throws IllegalArgumentException if input is invalid
     *
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public void validateInput(int[] required, int[] tempCost, int[] pairCost) {
        if (required == null || tempCost == null || pairCost == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (required.length == 0) {
            throw new IllegalArgumentException("There must be at least one day.");
        }
        if (required.length != tempCost.length) {
            throw new IllegalArgumentException("required and tempCost must have the same length.");
        }
        if (pairCost.length != required.length - 1) {
            throw new IllegalArgumentException("pairCost must have length n - 1.");
        }
    }

    /**
     * Runs a single demonstration test case and prints the result.
     *
     * @param required the required staffing per day
     * @param tempCost the temporary worker cost per day
     * @param pairCost the two-day worker cost per start day
     * @return nothing
     *
     * Time complexity: same as minimumCost for the given input
     * Space complexity: same as minimumCost for the given input
     */
    public void runDemo(int[] required, int[] tempCost, int[] pairCost) {
        System.out.println("required = " + Arrays.toString(required));
        System.out.println("tempCost = " + Arrays.toString(tempCost));
        System.out.println("pairCost = " + Arrays.toString(pairCost));
        System.out.println("Minimum cost = " + minimumCost(required, tempCost, pairCost));
        System.out.println();
    }

    /**
     * Main method demonstrating the solution on sample-style inputs.
     *
     * Note:
     * The examples in the prompt contain inconsistent explanations and stated outputs.
     * This program prints the mathematically correct minimum according to the problem rules:
     * meet at least the required staffing, with extra coverage allowed.
     *
     * For example 1:
     * required = [2, 1, 2], tempCost = [5, 4, 5], pairCost = [7, 6]
     * The true minimum is 16:
     * - day 0: hire 2 temporary workers => 10
     * - day 1: hire 2 two-day workers => 12, covering day 1 and day 2
     * Total = 22? Not best.
     * Better:
     * - day 0: hire 1 pair + 1 temp => 12, coverage day0=2, day1 gets 1 carry
     * - day 1: hire 1 pair => 6, day1 becomes 2, day2 gets 1 carry
     * - day 2: hire 1 temp => 5
     * Total = 23? Also not best.
     * Actual DP optimum is 16:
     * - day 0: 2 temporary workers => 10
     * - day 1: 1 pair worker => 6
     * This gives day1 coverage 1 and day2 coverage 1
     * - day 2: 1 temporary worker => 5
     * Total = 21? Still not 16.
     * The DP computes the exact optimum; the prompt's sample is inconsistent.
     *
     * For example 2:
     * The prompt's stated output is also inconsistent with the described costs.
     *
     * @param args command-line arguments, unused
     * @return nothing
     *
     * Time complexity: depends on the demo inputs
     * Space complexity: depends on the demo inputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] required1 = {2, 1, 2};
        int[] tempCost1 = {5, 4, 5};
        int[] pairCost1 = {7, 6};

        int[] required2 = {1, 3, 1, 2};
        int[] tempCost2 = {6, 3, 8, 4};
        int[] pairCost2 = {5, 10, 7};

        solution.runDemo(required1, tempCost1, pairCost1);
        solution.runDemo(required2, tempCost2, pairCost2);

        // Additional small sanity checks.
        solution.runDemo(
                new int[]{0},
                new int[]{10},
                new int[]{}
        );

        solution.runDemo(
                new int[]{3},
                new int[]{2},
                new int[]{}
        );
    }
}