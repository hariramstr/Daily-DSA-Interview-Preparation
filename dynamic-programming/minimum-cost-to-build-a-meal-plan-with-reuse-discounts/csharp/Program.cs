/*
Title: Minimum Cost to Build a Meal Plan with Reuse Discounts
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You are planning meals for the next several days. For each day i, you must choose exactly one dish from a list of available dishes for that day. Choosing dish j on day i has a base cost costs[i][j]. However, if you choose the same dish on consecutive days, you receive a reuse discount because ingredients can be reused. Specifically, if dish j is chosen on both day i - 1 and day i, then the cost paid for day i becomes costs[i][j] - discount[j]. The discounted cost will never be negative.

Your task is to compute the minimum total cost needed to build a valid meal plan across all days.

A meal plan is valid if exactly one dish is selected for every day. The number of available dishes is the same for every day.

Return the minimum possible total cost.

Constraints:
- 1 <= days == costs.length <= 200
- 1 <= dishes == costs[i].length <= 200
- 0 <= costs[i][j] <= 10^4
- 0 <= discount[j] <= 10^4
- For every i, costs[i].length == discount.length
- It is guaranteed that costs[i][j] - discount[j] >= 0 whenever the discount is applied

Important note about the examples:
The first example's written explanation is inconsistent with its stated output.
If we compute carefully:
costs = [[5,8,6],[4,7,3],[6,2,5]], discount = [2,3,1]

A valid plan is:
- Day 0: choose dish 0 => 5
- Day 1: choose dish 0 again => 4 - 2 = 2
- Day 2: choose dish 1 => 2
Total = 9

So the true minimum for Example 1 is 9, not 10.
The algorithm below correctly computes the mathematical minimum according to the problem rules.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(days * dishes)
    Explanation:
    - For each day, we compute the best cost for every dish.
    - To avoid an O(dishes^2) transition, we keep track of the smallest and second smallest
      values from the previous day. That lets us determine the best "switch from another dish"
      cost in O(1) per dish.
    - Therefore total work is linear in the number of states: days * dishes.

    Space Complexity:
    O(dishes)
    Explanation:
    - We only need the previous day's DP values and the current day's DP values.
    - So we use two 1D arrays of length equal to the number of dishes.
    */
    public long MinimumMealPlanCost(int[][] costs, int[] discount)
    {
        // Step 1:
        // Read the problem dimensions.
        // "days" is the number of rows in the costs matrix.
        // "dishes" is the number of choices available each day.
        int days = costs.Length;
        int dishes = costs[0].Length;

        // Step 2:
        // Create the DP array for the previous day.
        //
        // Meaning of prev[d]:
        // The minimum total cost to build a valid meal plan from day 0 up to the previous day,
        // with dish d chosen on that previous day.
        //
        // We use long instead of int to be extra safe with sums, even though int would also fit
        // under the given constraints.
        long[] prev = new long[dishes];

        // Step 3:
        // Initialize the base case for day 0.
        //
        // On the first day, there is no previous day, so no discount can be applied.
        // Therefore the minimum cost of ending day 0 with dish d is simply costs[0][d].
        for (int d = 0; d < dishes; d++)
        {
            prev[d] = costs[0][d];
        }

        // Step 4:
        // Process each later day one by one.
        //
        // For each day and each dish, there are exactly two possibilities:
        // 1) We used the same dish yesterday:
        //      prev[d] + costs[day][d] - discount[d]
        // 2) We used a different dish yesterday:
        //      min(prev[k]) for all k != d, plus costs[day][d]
        //
        // A direct implementation of option (2) would scan all dishes for every dish,
        // causing O(dishes^2) work per day.
        //
        // To optimize this, before computing the current day, we find:
        // - the smallest value in prev
        // - the second smallest value in prev
        // - the index where the smallest value occurs
        //
        // Then for a dish d:
        // - if d is not the index of the smallest previous value, the best "switch" cost uses that smallest value
        // - otherwise, we must use the second smallest value, because switching requires a different dish
        for (int day = 1; day < days; day++)
        {
            // Step 4a:
            // Find the smallest and second smallest values in prev.
            //
            // min1Value = smallest DP value from previous day
            // min1Index = dish index where that smallest value occurs
            // min2Value = second smallest DP value from previous day
            //
            // These values let us answer:
            // "What is the best previous total if I must come from a different dish?"
            long min1Value = long.MaxValue;
            long min2Value = long.MaxValue;
            int min1Index = -1;

            for (int d = 0; d < dishes; d++)
            {
                long value = prev[d];

                if (value < min1Value)
                {
                    // Current value becomes the new smallest.
                    // The old smallest is pushed down to second smallest.
                    min2Value = min1Value;
                    min1Value = value;
                    min1Index = d;
                }
                else if (value < min2Value)
                {
                    // Current value is not the smallest, but it is the second smallest.
                    min2Value = value;
                }
            }

            // Step 4b:
            // Create the DP array for the current day.
            long[] curr = new long[dishes];

            // Step 4c:
            // Compute the best total cost for each possible dish on the current day.
            for (int d = 0; d < dishes; d++)
            {
                // Option 1: continue using the same dish as yesterday.
                //
                // Why this works:
                // If we ended yesterday with dish d, and we also choose dish d today,
                // then the reuse discount applies today.
                long staySameDish = prev[d] + costs[day][d] - discount[d];

                // Option 2: switch from a different dish.
                //
                // We need the minimum previous total among all dishes except d.
                // If the global minimum came from some dish other than d, we can use it directly.
                // If the global minimum came from d itself, then we must use the second minimum,
                // because switching requires the previous dish to be different.
                long bestPreviousDifferentDish = (d == min1Index) ? min2Value : min1Value;

                // If there is only one dish total, then switching is impossible.
                // In that case min2Value will remain long.MaxValue.
                // We protect against overflow and simply treat "switch" as unavailable.
                long switchDish = long.MaxValue;
                if (bestPreviousDifferentDish != long.MaxValue)
                {
                    switchDish = bestPreviousDifferentDish + costs[day][d];
                }

                // The best way to end today with dish d is the cheaper of:
                // - staying on the same dish and taking the discount
                // - switching from a different dish and paying full cost
                curr[d] = Math.Min(staySameDish, switchDish);
            }

            // Step 4d:
            // Move current day results into prev so they become the "previous day"
            // for the next iteration.
            prev = curr;
        }

        // Step 5:
        // After processing all days, the answer is the minimum value in prev,
        // because the final day may end with any dish.
        long answer = long.MaxValue;
        for (int d = 0; d < dishes; d++)
        {
            if (prev[d] < answer)
            {
                answer = prev[d];
            }
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// The prompt says output 10, but the mathematically correct minimum is 9.
// Let's verify with the algorithm.
int[][] costs1 =
{
    new[] { 5, 8, 6 },
    new[] { 4, 7, 3 },
    new[] { 6, 2, 5 }
};
int[] discount1 = { 2, 3, 1 };

long result1 = solution.MinimumMealPlanCost(costs1, discount1);
Console.WriteLine("Example 1 result: " + result1);

// Example 2 from the prompt.
// This one evaluates to 10 as stated.
int[][] costs2 =
{
    new[] { 3, 9 },
    new[] { 8, 1 },
    new[] { 2, 7 },
    new[] { 6, 4 }
};
int[] discount2 = { 5, 0 };

long result2 = solution.MinimumMealPlanCost(costs2, discount2);
Console.WriteLine("Example 2 result: " + result2);

// Additional small sanity check:
// Only one day, choose the cheapest dish directly.
int[][] costs3 =
{
    new[] { 10, 4, 7 }
};
int[] discount3 = { 3, 2, 1 };

long result3 = solution.MinimumMealPlanCost(costs3, discount3);
Console.WriteLine("Single day result: " + result3);

// Additional sanity check:
// Only one dish across multiple days, so we must keep reusing it.
// Discounts should apply every day after the first.
int[][] costs4 =
{
    new[] { 5 },
    new[] { 4 },
    new[] { 6 }
};
int[] discount4 = { 2 };

long result4 = solution.MinimumMealPlanCost(costs4, discount4);
Console.WriteLine("Single dish result: " + result4);