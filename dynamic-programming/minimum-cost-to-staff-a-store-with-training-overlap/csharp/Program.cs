/*
Title: Minimum Cost to Staff a Store With Training Overlap

Problem Description:
A retail store must be staffed for the next n days. On day i, the store needs at least required[i] workers on duty. You can hire workers using only two training plans:

1. A one-day temporary worker for cost tempCost[i], who works only on day i.
2. A two-day cross-trained worker starting on day i for cost pairCost[i], who works on both day i and day i + 1.

Each hired worker contributes exactly 1 unit of staffing on every day covered by that plan. You may hire any number of workers under either plan, as long as all daily staffing requirements are met. If a two-day worker starts on the last day, it is invalid because there is no day i + 1.

Return the minimum total cost needed to satisfy the staffing requirement for all days.

Why dynamic programming fits:
A two-day worker hired today also affects tomorrow. That means a decision on day i changes the state of day i + 1.
So we cannot safely make greedy local choices. Instead, we track how much staffing has already been carried into the current day.

Important note about the examples:
The narrative explanations in the prompt are inconsistent, but the intended task is clear:
find the true minimum cost that satisfies all daily requirements, allowing extra harmless coverage.
This implementation computes that true minimum.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * R^2), where:
    - n is the number of days
    - R is the maximum required staffing on any day
    Since n <= 200 and required[i] <= 200, this is efficient enough.

    Space Complexity:
    O(R)
    We only keep DP for the current day and the next day.

    Beginner-friendly idea:
    -----------------------
    We process days from left to right.

    State meaning:
    dp[carry] = minimum cost after finishing previous days,
                where "carry" workers are already available on the current day
                because they were hired yesterday as two-day workers.

    On day i:
    - We already have "carry" workers covering today.
    - If required[i] is larger than carry, we still need more workers today.
    - We can fill that shortage using:
        1) temporary workers for today only
        2) two-day workers starting today, which help today and also create carry for tomorrow

    Key observation:
    If we decide to create x carry workers for tomorrow (by hiring x pair workers today),
    then today receives x coverage from those same pair workers too.
    So the total coverage available today becomes carry + x.
    If that is still below required[i], we must add temporary workers for the remaining shortage.

    Cost for choosing x pair workers today:
    - x * pairCost[i]   (except on the last day, where pair workers are invalid)
    - plus temp workers needed to reach today's requirement

    Transition:
    nextCarry = x
    tempNeeded = max(0, required[i] - (carry + x))
    costAdd = x * pairCost[i] + tempNeeded * tempCost[i]

    On the last day:
    - pair workers are not allowed
    - so x must be 0
    - we only pay for any remaining shortage with temporary workers
    */
    public long MinimumCost(int[] required, int[] tempCost, int[] pairCost)
    {
        int n = required.Length;

        // We only ever need to track carry values from 0 up to the maximum requirement.
        // Why is that enough?
        // Because having more carry than today's requirement is never more useful than
        // having exactly today's requirement:
        // extra coverage on a day does not stack into future days unless it came from
        // pair workers started yesterday, and tomorrow's carry is explicitly represented
        // by the number of pair workers started today.
        // Therefore, the useful carry range is bounded by max(required).
        int maxRequired = 0;
        for (int i = 0; i < n; i++)
        {
            if (required[i] > maxRequired)
            {
                maxRequired = required[i];
            }
        }

        long inf = long.MaxValue / 4;

        // dp[carry] = minimum cost before processing the current day,
        // with "carry" workers already covering the current day.
        long[] dp = new long[maxRequired + 1];
        Array.Fill(dp, inf);

        // Before day 0 starts, there is no carry from a previous day.
        dp[0] = 0;

        // Process each day one by one.
        for (int day = 0; day < n; day++)
        {
            long[] next = new long[maxRequired + 1];
            Array.Fill(next, inf);

            // Try every possible amount of carry already available today.
            for (int carry = 0; carry <= maxRequired; carry++)
            {
                if (dp[carry] == inf)
                {
                    // This state was never reached, so skip it.
                    continue;
                }

                if (day == n - 1)
                {
                    // Last day special case:
                    // We are NOT allowed to start a two-day worker here,
                    // because there is no day day + 1.
                    // So the only option is to use temporary workers for any shortage.

                    int tempNeeded = Math.Max(0, required[day] - carry);
                    long totalCost = dp[carry] + (long)tempNeeded * tempCost[day];

                    // After the last day, there is no meaningful next carry.
                    // We store the result in next[0].
                    if (totalCost < next[0])
                    {
                        next[0] = totalCost;
                    }
                }
                else
                {
                    // General day:
                    // We choose how many pair workers to start today.
                    // Let that number be x.
                    //
                    // Those x workers:
                    // - help cover today
                    // - become carry for tomorrow
                    //
                    // Since tomorrow only needs at most maxRequired workers,
                    // there is no reason to consider x > maxRequired.
                    for (int x = 0; x <= maxRequired; x++)
                    {
                        // Coverage available today:
                        // - "carry" from yesterday's pair workers
                        // - "x" from today's newly started pair workers
                        int coveredToday = carry + x;

                        // If today's coverage is still short, we must buy temporary workers.
                        int tempNeeded = Math.Max(0, required[day] - coveredToday);

                        long addCost =
                            (long)x * pairCost[day] +
                            (long)tempNeeded * tempCost[day];

                        long totalCost = dp[carry] + addCost;

                        // Tomorrow receives exactly x carry workers from the pair workers started today.
                        if (totalCost < next[x])
                        {
                            next[x] = totalCost;
                        }
                    }
                }
            }

            dp = next;
        }

        // After processing all days, the answer is the minimum reachable final state.
        // In practice next[0] holds it after the last day, but taking min is safe and clear.
        long answer = inf;
        for (int carry = 0; carry <= maxRequired; carry++)
        {
            if (dp[carry] < answer)
            {
                answer = dp[carry];
            }
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] required1 = { 2, 1, 2 };
int[] tempCost1 = { 5, 4, 5 };
int[] pairCost1 = { 7, 6 };

long result1 = solution.MinimumCost(required1, tempCost1, pairCost1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2
int[] required2 = { 1, 3, 1, 2 };
int[] tempCost2 = { 6, 3, 8, 4 };
int[] pairCost2 = { 5, 10, 7 };

long result2 = solution.MinimumCost(required2, tempCost2, pairCost2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional small sanity check
int[] required3 = { 0, 0, 0 };
int[] tempCost3 = { 5, 5, 5 };
int[] pairCost3 = { 3, 3 };

long result3 = solution.MinimumCost(required3, tempCost3, pairCost3);
Console.WriteLine("Sanity Check Result: " + result3);