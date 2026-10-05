/*
Title: Minimum Daily Packaging Rate for Sequential Orders

Problem Description:
A fulfillment center must prepare customer orders in the exact order they appear in an array `orders`,
where `orders[i]` is the number of items in the `i`-th order.

The center operates for exactly `d` days. Each day, it chooses a consecutive block of remaining orders
and packages them from left to right. However, the total number of items packaged on a single day
cannot exceed a fixed daily rate `R`.

Important rule:
- Orders must remain in the original order.
- Each day handles a contiguous sequence of the remaining orders.
- An individual order cannot be split across multiple days.

Task:
Find the minimum integer daily rate `R` such that all orders can be completed within `d` days.

Key insight:
This problem has a monotonic feasibility property:
- If a rate `R` is enough to finish within `d` days,
- then any larger rate is also enough.

That makes binary search the correct and efficient approach.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of orders.
    - Each feasibility check scans the array once: O(n)
    - Binary search runs over the answer range from max(orders) to sum(orders),
      which takes O(log(sum(orders))) iterations.
    - Total: O(n * log(sum(orders)))

    Space Complexity:
    - O(1) extra space
    - We only use a few variables and do not allocate extra data structures
    */
    public long MinimumDailyRate(int[] orders, long d)
    {
        // Step 1:
        // Establish the binary search boundaries.
        //
        // Why do we need boundaries?
        // Binary search always works on a search interval [left, right].
        // We need:
        // - a guaranteed impossible-or-minimum starting point
        // - a guaranteed possible ending point
        //
        // Lower bound:
        // The daily rate can never be smaller than the largest single order,
        // because an order cannot be split across days.
        //
        // Upper bound:
        // The daily rate can always be the sum of all orders.
        // In that case, everything can be done in one day, which is certainly
        // within d days whenever d >= 1.
        long left = 0;
        long right = 0;

        foreach (int order in orders)
        {
            // Update the lower bound to the maximum order size seen so far.
            if (order > left)
            {
                left = order;
            }

            // Add to the total sum for the upper bound.
            right += order;
        }

        // Step 2:
        // Perform binary search on the answer.
        //
        // Invariant we maintain:
        // - Any value < left is known to be too small or not yet proven feasible.
        // - right is a candidate upper boundary for the minimum feasible answer.
        //
        // We continue until left == right.
        // At that point, both pointers converge to the smallest feasible rate.
        while (left < right)
        {
            // Compute the middle carefully.
            // This avoids overflow compared to (left + right) / 2,
            // even though long already gives us a large safe range.
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate daily rate "mid" is sufficient.
            //
            // If it is sufficient:
            // - We try to find a smaller feasible answer on the left side.
            //
            // If it is not sufficient:
            // - We must increase the rate, so we search the right side.
            if (CanFinishWithinDays(orders, d, mid))
            {
                // mid works, so the minimum answer is in [left, mid]
                right = mid;
            }
            else
            {
                // mid does not work, so the minimum answer is in [mid + 1, right]
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the smallest feasible rate.
        return left;
    }

    private bool CanFinishWithinDays(int[] orders, long d, long rate)
    {
        // Step 1:
        // Simulate packaging orders from left to right using the given daily rate.
        //
        // Why simulation works:
        // For a fixed rate, the best strategy is greedy:
        // keep placing orders into the current day until the next order would exceed the rate,
        // then start a new day.
        //
        // Why is greedy correct here?
        // Because orders must stay in order and cannot be split.
        // Deliberately leaving unused capacity in a day can never help reduce the number of days.
        // So packing as much as possible each day gives the minimum number of days needed
        // for this fixed rate.
        long daysUsed = 1;      // We start using day 1 immediately.
        long currentDayLoad = 0;

        foreach (int order in orders)
        {
            // Step 2:
            // Try to place the current order into the current day.
            //
            // If adding this order stays within the rate, we keep it in the same day.
            if (currentDayLoad + order <= rate)
            {
                currentDayLoad += order;
            }
            else
            {
                // Step 3:
                // Otherwise, this order does not fit in the current day.
                // We must start a new day and place this order there.
                daysUsed++;
                currentDayLoad = order;

                // Step 4:
                // Early stopping optimization:
                // If we already exceeded the allowed number of days,
                // there is no need to continue scanning.
                if (daysUsed > d)
                {
                    return false;
                }
            }
        }

        // If we finished scanning all orders without exceeding d days,
        // then this rate is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// orders = [7, 2, 5, 10, 8], d = 2
// Expected output: 18
//
// Quick verification:
// - Rate 18 works:
//   Day 1: 7 + 2 + 5 = 14
//   Day 2: 10 + 8 = 18
// - Any smaller rate fails.
// So the correct answer is 18.
int[] orders1 = { 7, 2, 5, 10, 8 };
long d1 = 2;
long result1 = solution.MinimumDailyRate(orders1, d1);
Console.WriteLine(result1);

// Example 2:
// orders = [3, 6, 7, 11], d = 4
// Expected output: 11
//
// Quick verification:
// - Since orders cannot be split, rate must be at least max order = 11.
// - Rate 11 works:
//   [3], [6], [7], [11]
// So the correct answer is 11.
int[] orders2 = { 3, 6, 7, 11 };
long d2 = 4;
long result2 = solution.MinimumDailyRate(orders2, d2);
Console.WriteLine(result2);