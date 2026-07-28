/*
Title: Minimum Refill Rate for a Timed Irrigation Plan

Problem Description:
A farm manager needs to water fields in a fixed order using a mobile tank. The tank is refilled to the same amount at the start of each day, and the manager waters consecutive fields from left to right. Each field i requires water[i] liters, and splitting a single field across multiple days is not allowed. If the next field does not fit in the remaining water for the current day, the manager must stop for that day and continue from the next field on the following day with a freshly refilled tank.

Given an array water where water[i] is the amount of water needed for the i-th field, and an integer days, return the minimum tank refill capacity needed so that all fields can be watered within at most days days.

The order of fields cannot be changed. Every day starts with a full tank of the chosen capacity. A capacity is feasible if the full watering plan can be completed in days or fewer.

This problem is designed for an efficient solution using binary search on the answer. The key observation is that if a certain tank capacity works, then any larger capacity will also work.

Constraints:
- 1 <= water.length <= 100000
- 1 <= water[i] <= 1000000000
- 1 <= days <= water.length

Example 1:
Input: water = [7,2,5,10,8], days = 2
Output: 18
Explanation: With capacity 18, day 1 can water fields [7,2,5] and day 2 can water [10,8]. Any capacity smaller than 18 would require more than 2 days.

Example 2:
Input: water = [3,1,4,1,5,9], days = 3
Output: 9
Explanation: One optimal schedule is [3,1,4], [1,5], [9]. Capacity 8 is not enough because the field requiring 9 liters cannot be split across days.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log(S))
      where n is the number of fields and S is the search range of possible capacities
      (from max(water) to sum(water)).
      For each binary search guess, we scan the array once to test feasibility.

    Space Complexity:
    - O(1)
      We only use a few variables and do not create extra data structures that grow with input size.
    */
    public long MinimumRefillCapacity(int[] water, int days)
    {
        // Step 1:
        // We need to determine the smallest possible tank capacity.
        // Instead of trying every capacity one by one, we use binary search.
        //
        // Why binary search works:
        // - If a capacity C is enough to finish within "days", then any capacity larger than C
        //   will also be enough.
        // - This creates a monotonic true/false pattern:
        //   false, false, false, ..., true, true, true
        // - Binary search is perfect for finding the first "true" value in such a pattern.

        // "left" is the minimum possible valid capacity.
        // It must be at least the largest single field requirement,
        // because a field cannot be split across days.
        long left = 0;

        // "right" is the maximum possible capacity we would ever need.
        // If the tank can hold the total sum, then all fields can be watered in one day.
        long right = 0;

        // We scan once to compute both bounds.
        foreach (int amount in water)
        {
            // The capacity must be large enough to fit the biggest single field.
            if (amount > left)
            {
                left = amount;
            }

            // The total sum is a guaranteed upper bound.
            right += amount;
        }

        // Step 2:
        // Perform binary search on the answer range [left, right].
        // We want the minimum feasible capacity.
        while (left < right)
        {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this guessed capacity "mid" is feasible.
            // If it is feasible, we try smaller values by moving "right" down.
            // If it is not feasible, we must increase capacity by moving "left" up.
            if (CanFinishWithinDays(water, days, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // When left == right, binary search has found the smallest feasible capacity.
        return left;
    }

    private bool CanFinishWithinDays(int[] water, int days, long capacity)
    {
        // This helper method simulates the watering process from left to right.
        //
        // Goal:
        // Determine whether the given tank capacity allows all fields
        // to be watered in at most "days" days.
        //
        // Important rule:
        // - Fields must stay in order.
        // - A field cannot be split across days.
        // - Each day starts with a full tank of size "capacity".

        // We start on day 1.
        int usedDays = 1;

        // "currentLoad" tracks how much water has already been used on the current day.
        long currentLoad = 0;

        // We process each field in order.
        foreach (int amount in water)
        {
            // Step A:
            // Try to place the current field into the current day.
            //
            // If adding this field would exceed the tank capacity,
            // then this field cannot be watered today.
            // So we must start a new day and place this field there.
            if (currentLoad + amount > capacity)
            {
                usedDays++;

                // If we already need more than the allowed number of days,
                // then this capacity is not feasible.
                if (usedDays > days)
                {
                    return false;
                }

                // Start the new day with this field already assigned to it.
                currentLoad = amount;
            }
            else
            {
                // Step B:
                // If the field fits in the current day, simply add it.
                currentLoad += amount;
            }
        }

        // If we finished assigning all fields without exceeding "days",
        // then this capacity works.
        return true;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// water = [7,2,5,10,8], days = 2
// Expected answer: 18
int[] water1 = { 7, 2, 5, 10, 8 };
int days1 = 2;
long result1 = solution.MinimumRefillCapacity(water1, days1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// water = [3,1,4,1,5,9], days = 3
// Expected answer: 9
int[] water2 = { 3, 1, 4, 1, 5, 9 };
int days2 = 3;
long result2 = solution.MinimumRefillCapacity(water2, days2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick sanity check:
// If days equals number of fields, answer should be max(water).
int[] water3 = { 4, 8, 2, 6 };
int days3 = 4;
long result3 = solution.MinimumRefillCapacity(water3, days3);
Console.WriteLine($"Additional Check Result: {result3}");