/*
Title: Minimum Subscription Tier to Reach Target Revenue
Difficulty: Medium
Topic: Binary Search

Problem Description:
A software company offers a product with several subscription tiers. Each customer has a maximum tier they are willing to buy, represented by an integer in the array limits, where limits[i] is the highest price tier customer i will accept.

If the company sets a global tier price p:
- Every customer with limits[i] >= p subscribes
- Every customer with limits[i] < p does not

So the total revenue is:
    revenue = p * (number of customers willing to pay at least p)

Given the array limits and an integer targetRevenue, return the minimum integer tier price p such that the total revenue is at least targetRevenue.
If no such tier exists, return -1.

Important note:
The revenue function is NOT globally monotonic over all prices, because increasing the price can reduce the number of customers. So a direct binary search on all prices is not always correct.

Key observation:
For any fixed number of buyers k, the minimum price that keeps at least k buyers is any price up to the k-th largest customer limit.
To achieve targetRevenue with exactly "at least k buyers", we need:
    p * k >= targetRevenue
So the smallest such integer price is:
    p = ceil(targetRevenue / k)

This price is feasible only if at least k customers can pay it, which means:
    p <= k-th largest limit

Therefore, we can sort the limits and test each possible buyer count efficiently.

Example 1:
limits = [3, 8, 6, 6, 10], targetRevenue = 18

Sorted ascending: [3, 6, 6, 8, 10]

Try k = 1:
  minimum price needed = ceil(18 / 1) = 18
  largest limit among top 1 buyer = 10
  18 > 10, not feasible

Try k = 2:
  minimum price needed = ceil(18 / 2) = 9
  2nd largest limit = 8
  9 > 8, not feasible

Try k = 3:
  minimum price needed = ceil(18 / 3) = 6
  3rd largest limit = 6
  feasible, answer = 6

Check smaller prices:
  p = 5 gives 4 buyers => revenue 20, also feasible
  p = 4 gives 4 buyers => revenue 16, not enough
So the true minimum is 5.

Our scan over k will also find this:
  k = 4:
    ceil(18 / 4) = 5
    4th largest limit = 6
    feasible, answer becomes 5
  k = 5:
    ceil(18 / 5) = 4
    5th largest limit = 3
    not feasible
Final answer = 5

Example 2:
limits = [2, 2, 2], targetRevenue = 10

Sorted ascending: [2, 2, 2]

k = 1 => ceil(10/1)=10 > 2, not feasible
k = 2 => ceil(10/2)=5 > 2, not feasible
k = 3 => ceil(10/3)=4 > 2, not feasible

No feasible price exists, so answer = -1
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - Scanning all possible buyer counts takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, extra auxiliary space is O(1)
      (ignoring the internal stack usage of the sorting implementation)
    */
    public int MinimumTierPrice(int[] limits, long targetRevenue)
    {
        // Step 1:
        // Sort the customer limits in ascending order.
        //
        // Why do we sort?
        // Because after sorting, we can quickly reason about how many customers
        // can afford a given price.
        //
        // If the array is sorted ascending:
        //   limits[0] <= limits[1] <= ... <= limits[n - 1]
        //
        // Then for any buyer count k:
        // - The customers who can pay are the k customers with the largest limits.
        // - The smallest limit among those k customers is:
        //       limits[n - k]
        //
        // That value is extremely important:
        // it is the maximum price we can charge while still keeping at least k buyers.
        Array.Sort(limits);

        int n = limits.Length;

        // Step 2:
        // We will try every possible number of buyers k from 1 to n.
        //
        // Why iterate over buyer counts instead of prices?
        // Because the revenue function:
        //     revenue(p) = p * count(limits[i] >= p)
        // is not monotonic in p.
        //
        // That means a plain binary search over price is unsafe.
        //
        // But for a fixed buyer count k, the minimum price needed to reach the target is:
        //     ceil(targetRevenue / k)
        //
        // Then we only need to check whether that price is feasible with k buyers.
        //
        // If it is feasible, it is a candidate answer.
        long bestAnswer = long.MaxValue;

        for (int k = 1; k <= n; k++)
        {
            // Step 3:
            // Compute the minimum integer price p such that:
            //     p * k >= targetRevenue
            //
            // This is the mathematical ceiling of targetRevenue / k.
            //
            // We compute ceiling division safely using:
            //     (targetRevenue + k - 1) / k
            //
            // Example:
            //   targetRevenue = 18, k = 4
            //   ceil(18 / 4) = 5
            long requiredPrice = (targetRevenue + k - 1L) / k;

            // Step 4:
            // Determine the highest price that still keeps at least k buyers.
            //
            // Since the array is sorted ascending, the k customers with the largest limits
            // are the suffix of length k:
            //   limits[n - k], limits[n - k + 1], ..., limits[n - 1]
            //
            // The smallest limit in that suffix is limits[n - k].
            //
            // Therefore:
            // - Any price <= limits[n - k] keeps at least k buyers
            // - Any price > limits[n - k] cannot keep k buyers
            long maxFeasiblePriceForKBuyers = limits[n - k];

            // Step 5:
            // Check whether the required price is feasible.
            //
            // If:
            //     requiredPrice <= maxFeasiblePriceForKBuyers
            // then at least k customers can pay requiredPrice,
            // and the revenue will be at least:
            //     requiredPrice * k >= targetRevenue
            //
            // So this requiredPrice is a valid candidate answer.
            if (requiredPrice <= maxFeasiblePriceForKBuyers)
            {
                // Step 6:
                // We want the minimum valid price overall,
                // so keep the smallest candidate we have seen.
                if (requiredPrice < bestAnswer)
                {
                    bestAnswer = requiredPrice;
                }
            }
        }

        // Step 7:
        // If bestAnswer was never updated, then no valid price exists.
        //
        // Return -1 in that case.
        if (bestAnswer == long.MaxValue)
        {
            return -1;
        }

        // Step 8:
        // The problem states the answer is an integer tier price.
        // The method returns int, and the feasible answer must fit in int
        // for typical problem expectations because prices come from integer tiers.
        return (int)bestAnswer;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] limits1 = { 3, 8, 6, 6, 10 };
long targetRevenue1 = 18;
int result1 = solution.MinimumTierPrice((int[])limits1.Clone(), targetRevenue1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] limits2 = { 2, 2, 2 };
long targetRevenue2 = 10;
int result2 = solution.MinimumTierPrice((int[])limits2.Clone(), targetRevenue2);
Console.WriteLine(result2); // Expected: -1

// Additional demo
int[] limits3 = { 5, 5, 5, 5 };
long targetRevenue3 = 16;
int result3 = solution.MinimumTierPrice((int[])limits3.Clone(), targetRevenue3);
Console.WriteLine(result3); // Expected: 4