/*
Title: Maximum Completed Orders with Shared Stock

Problem Description:
You are managing a warehouse that stores items of different product types. The array `stock` represents how many units are currently available for each product type, where `stock[i]` is the number of units of type `i`. Each customer order must be fulfilled using units from exactly one product type, and every fulfilled order must contain the same number of units, called the order size.

You are also given an integer `k`, the number of customer orders that must be fulfilled. You may split the inventory of a single product type across multiple orders, but you cannot combine units from different product types to form one order. For example, if `stock[i] = 11` and the order size is `3`, that product type can support `3` full orders, with `2` units left unused.

Return the maximum possible order size such that at least `k` orders can be fulfilled.

If it is impossible to fulfill `k` orders even with order size `1`, return `0`.

This problem asks you to determine the largest feasible uniform order size. A brute-force search over all possible sizes may be too slow when stock counts are large, so an efficient solution is expected.

Constraints:
- 1 <= stock.length <= 100000
- 1 <= stock[i] <= 1000000000
- 1 <= k <= 1000000000000

Example 1:
Input: stock = [8, 5, 6], k = 5
Output: 3
Explanation: With order size 3, the product types contribute 2, 1, and 2 orders respectively, for a total of 5 orders. Order size 4 would produce only 2 + 1 + 1 = 4 orders, which is not enough.

Example 2:
Input: stock = [2, 3], k = 6
Output: 0
Explanation: Even with order size 1, only 5 total orders can be fulfilled, so it is impossible to reach 6 orders.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
      where:
      - n = number of product types in the stock array
      - M = maximum stock value in the array
    Explanation:
    We use binary search on the answer (the order size). For each candidate size,
    we scan the entire stock array once to count how many orders can be formed.

    Space Complexity:
    - O(1)
    Explanation:
    We only use a few extra variables and do not allocate any additional data structures
    proportional to the input size.
    */
    public long MaximumOrderSize(int[] stock, long k)
    {
        // Step 1:
        // First, we check whether fulfilling k orders is even possible when the order size is 1.
        //
        // Why this matters:
        // If order size = 1 still cannot produce at least k orders, then no larger order size can work.
        // Larger order sizes always produce the same number or fewer orders.
        //
        // We use long for totals because:
        // - stock[i] can be as large as 1,000,000,000
        // - there can be up to 100,000 product types
        // - the total sum can therefore exceed the range of int
        long totalUnits = 0;
        int maxStock = 0;

        foreach (int units in stock)
        {
            totalUnits += units;

            // We also track the maximum stock value.
            // Why:
            // The answer cannot be larger than the largest single product type count,
            // because one order must come entirely from one product type.
            if (units > maxStock)
            {
                maxStock = units;
            }
        }

        // If even size 1 cannot produce k orders, return 0 immediately.
        if (totalUnits < k)
        {
            return 0;
        }

        // Step 2:
        // We now binary search for the largest feasible order size.
        //
        // Why binary search works:
        // Define a function feasible(size):
        //   "Can we make at least k orders of this size?"
        //
        // If a certain size is feasible, then any smaller size is also feasible,
        // because smaller orders require fewer units each.
        //
        // This creates a monotonic pattern:
        // feasible feasible feasible ... feasible | infeasible infeasible ...
        // Therefore, binary search is the correct tool.
        long left = 1;
        long right = maxStock;
        long answer = 0;

        while (left <= right)
        {
            // Standard binary search midpoint calculation.
            // We use this form to avoid overflow in general:
            long mid = left + (right - left) / 2;

            // Step 3:
            // Count how many orders of size "mid" can be formed.
            //
            // For each product type:
            //   stock[i] / mid
            // gives the number of full orders that product type can support.
            //
            // Important rule from the problem:
            // We may split one product type across multiple orders,
            // but we may NOT combine different product types into one order.
            //
            // So summing stock[i] / mid over all i is exactly the total number of valid orders.
            long ordersMade = 0;

            foreach (int units in stock)
            {
                ordersMade += units / mid;

                // Small optimization:
                // As soon as we already know we can make at least k orders,
                // we can stop counting early.
                //
                // Why this is safe:
                // For the binary search decision, we only need to know whether
                // ordersMade >= k, not the exact final total beyond that point.
                if (ordersMade >= k)
                {
                    break;
                }
            }

            // Step 4:
            // Use the feasibility result to move the binary search boundaries.
            if (ordersMade >= k)
            {
                // Current size "mid" works.
                // Since we want the MAXIMUM possible order size,
                // we record it and try a larger size.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // Current size "mid" does NOT work.
                // We must try smaller sizes.
                right = mid - 1;
            }
        }

        // After binary search finishes, "answer" holds the largest feasible size.
        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] stock1 = { 8, 5, 6 };
long k1 = 5;
long result1 = solution.MaximumOrderSize(stock1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int[] stock2 = { 2, 3 };
long k2 = 6;
long result2 = solution.MaximumOrderSize(stock2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 0

// Additional demo
int[] stock3 = { 11 };
long k3 = 3;
long result3 = solution.MaximumOrderSize(stock3, k3);
Console.WriteLine($"Additional Example Result: {result3}"); // Expected: 3 because 11 / 3 = 3 orders, but 11 / 4 = 2 orders