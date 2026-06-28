/*
Title: Longest Delivery Route Within Fuel Budget

Problem Description:
A courier company records the fuel cost of each stop along a planned route in an array costs,
where costs[i] is the amount of fuel needed to travel through stop i.

The driver wants to complete the longest possible consecutive portion of the route without
exceeding a total fuel budget.

Your task is to return the length of the longest contiguous subarray whose sum is less than
or equal to budget.

This is a practical sliding window problem because all fuel costs are non-negative.
As you expand a window to the right, the total fuel used only increases or stays the same.
If the total exceeds the budget, you can shrink the window from the left until the route
becomes affordable again.

Return 0 if no single stop can be included within the budget.

Constraints:
- 1 <= costs.length <= 100000
- 0 <= costs[i] <= 10000
- 0 <= budget <= 1000000000

Example 1:
Input: costs = [4, 2, 1, 7, 3, 2], budget = 8
Output: 3
Explanation: The longest valid consecutive route is [4, 2, 1] with total fuel 7.

Example 2:
Input: costs = [9, 1, 2, 1, 1], budget = 4
Output: 4
Explanation: The longest valid route is [1, 2, 1, 1], which uses exactly 4 units of fuel.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once when the right pointer moves forward.
    - Each element is removed from the window at most once when the left pointer moves forward.
    - Because both pointers only move from left to right, the total work is linear.

    Space Complexity: O(1)
    - We only use a few extra variables: left pointer, running sum, and best length.
    - No extra arrays or data structures are needed.
    */
    public int LongestRouteWithinBudget(int[] costs, int budget)
    {
        // This pointer marks the start of the current sliding window.
        // The window represents a contiguous segment of the route that we are currently testing.
        int left = 0;

        // This variable stores the total fuel cost of the current window.
        // We use long for extra safety, even though the given constraints fit in int.
        long currentSum = 0;

        // This variable stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one stop at a time by moving the right pointer from left to right.
        for (int right = 0; right < costs.Length; right++)
        {
            // Step 1: Include the new stop at index "right" into the current window.
            // Why?
            // We are trying to build the longest possible contiguous route,
            // so we first attempt to extend the current route by one stop.
            currentSum += costs[right];

            // Step 2: If the current window is too expensive, shrink it from the left.
            // Why?
            // Because all costs are non-negative, adding more elements can never reduce the sum.
            // So if the sum is over budget, the only way to make it valid again is to remove
            // elements from the left side of the window.
            while (currentSum > budget && left <= right)
            {
                // Remove the leftmost stop from the current window.
                currentSum -= costs[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid
            // (its sum is <= budget), or it may be empty in edge cases after shrinking.
            // We now compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            // Why?
            // The problem asks for the maximum length among all valid contiguous subarrays.
            if (currentSum <= budget && currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // If no valid stop was ever found, maxLength remains 0, which is exactly what we should return.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] costs1 = { 4, 2, 1, 7, 3, 2 };
int budget1 = 8;
int result1 = solution.LongestRouteWithinBudget(costs1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
int[] costs2 = { 9, 1, 2, 1, 1 };
int budget2 = 4;
int result2 = solution.LongestRouteWithinBudget(costs2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional demo: no single stop fits in budget
int[] costs3 = { 5, 6, 7 };
int budget3 = 4;
int result3 = solution.LongestRouteWithinBudget(costs3, budget3);
Console.WriteLine("Example 3 Result: " + result3); // Expected: 0

// Additional demo: all zeros with zero budget
int[] costs4 = { 0, 0, 0, 0 };
int budget4 = 0;
int result4 = solution.LongestRouteWithinBudget(costs4, budget4);
Console.WriteLine("Example 4 Result: " + result4); // Expected: 4