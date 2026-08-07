/*
Title: Count Pairs of Orders With the Same Item Set

Problem Description:
You are given a list of customer orders from an online store. Each order is represented as a list of item IDs.
The same item ID may appear multiple times inside one order if the customer bought more than one copy of that item.

Two orders are considered equivalent if they contain exactly the same distinct item IDs, regardless of:
1. The order of items in the list
2. How many times each item appears

So each order should be treated as a set of item IDs, not a multiset.

Your task is to return the number of unordered pairs of equivalent orders.

Example:
[4, 2, 4, 7], [7, 2, 4], and [2, 7, 7, 4]
all normalize to the same distinct item set: {2, 4, 7}
So they contribute 3 pairs total:
- order1 with order2
- order1 with order3
- order2 with order3

Efficient idea:
Normalize each order into a canonical representation of its distinct items,
then count how many times each representation appears using hashing.
If one normalized representation appears k times, it contributes:
k * (k - 1) / 2 pairs
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let T be the total number of item IDs across all orders.
    Let k_i be the size of order i.

    For each order:
    - We remove duplicates using a HashSet: O(k_i) average
    - We sort the distinct items: O(d_i log d_i), where d_i is the number of distinct items in that order
    - We build a canonical string key from the sorted distinct items

    Total:
    O(sum over all orders of (k_i + d_i log d_i)))
    Since total item count is bounded, this is efficient for the given constraints.

    Space Complexity:
    - HashSet for one order: O(d_i)
    - Dictionary storing normalized keys: O(number of distinct normalized orders)
    - Key strings together represent the canonical forms

    Overall auxiliary space:
    O(total number of distinct normalized representations + max distinct items in one order)
    */
    public long CountEquivalentOrderPairs(IList<IList<int>> orders)
    {
        // This dictionary is the core hashing structure of the solution.
        //
        // Key:
        //   A canonical representation of an order's DISTINCT item set.
        //   Example:
        //   [3,1,2], [1,2,2,3], and [2,3,1,1] all become the same key: "1,2,3"
        //
        // Value:
        //   How many previous orders have already produced this exact normalized key.
        //
        // Why a dictionary?
        //   Because we want very fast average O(1) lookup and update by normalized order.
        var frequencyByNormalizedOrder = new Dictionary<string, long>();

        // This will store the final answer.
        //
        // We use long instead of int because the number of pairs can be large.
        // For example, if many orders are equivalent, the pair count can exceed int range.
        long pairCount = 0;

        // Process each order one by one.
        foreach (var order in orders)
        {
            // STEP 1: Remove duplicates inside the current order.
            //
            // Why is this necessary?
            // The problem says an order should be treated as a SET of item IDs.
            // That means:
            //   [1,2,2,3] and [1,2,3] are equivalent
            // because duplicates do not matter.
            //
            // A HashSet automatically keeps only distinct values.
            //
            // Example:
            //   order = [4, 2, 4, 7]
            //   distinctItems becomes {4, 2, 7}
            var distinctItems = new HashSet<int>(order);

            // STEP 2: Convert the set into a list so we can sort it.
            //
            // Why sort?
            // Sets do not have a guaranteed order.
            // If we directly build a key from a set, then:
            //   {1,2,3} might appear as "1,2,3" in one case
            //   and "2,1,3" in another
            // which would incorrectly look different.
            //
            // Sorting guarantees that equivalent sets always produce the same order.
            //
            // Example:
            //   {3,1,2} -> [1,2,3]
            var normalizedList = distinctItems.ToList();

            // STEP 3: Sort the distinct items.
            //
            // This creates a canonical ordering.
            // Canonical means: one standard form for all equivalent orders.
            normalizedList.Sort();

            // STEP 4: Build a canonical string key.
            //
            // Example:
            //   [1,2,3] -> "1,2,3"
            //
            // Why use a string key?
            // - It is simple and reliable.
            // - Two equivalent normalized lists will produce exactly the same string.
            // - It works well as a dictionary key.
            //
            // We use ',' as a separator so values do not merge ambiguously.
            // For example:
            //   [1,23] -> "1,23"
            //   [12,3] -> "12,3"
            // These remain distinct.
            string key = string.Join(",", normalizedList);

            // STEP 5: Count how many previous orders had the same normalized key.
            //
            // If this key has already appeared 'f' times,
            // then the current order forms exactly 'f' new pairs:
            //
            // current order pairs with each previous equivalent order.
            //
            // Example:
            // Suppose key "1,2,3" has appeared 2 times already.
            // The current order creates 2 new pairs.
            //
            // This is more efficient than storing all orders and comparing them later.
            if (frequencyByNormalizedOrder.TryGetValue(key, out long existingCount))
            {
                // Add the number of new pairs formed with all previous equivalent orders.
                pairCount += existingCount;

                // Then update the frequency for future orders.
                frequencyByNormalizedOrder[key] = existingCount + 1;
            }
            else
            {
                // First time we see this normalized order.
                // It forms no pair yet, because there is no previous equivalent order.
                frequencyByNormalizedOrder[key] = 1;
            }
        }

        // After processing all orders, pairCount contains the total number of unordered equivalent pairs.
        return pairCount;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// orders = [[1,2,2,3],[3,1,2],[4,4],[4,5],[5,4,4]]
//
// Normalized forms:
// [1,2,2,3] -> {1,2,3} -> "1,2,3"
// [3,1,2]   -> {1,2,3} -> "1,2,3"   => 1 pair so far
// [4,4]     -> {4}     -> "4"
// [4,5]     -> {4,5}   -> "4,5"
// [5,4,4]   -> {4,5}   -> "4,5"     => +1 pair
//
// Total = 2
var orders1 = new List<IList<int>>
{
    new List<int> { 1, 2, 2, 3 },
    new List<int> { 3, 1, 2 },
    new List<int> { 4, 4 },
    new List<int> { 4, 5 },
    new List<int> { 5, 4, 4 }
};

long result1 = solution.CountEquivalentOrderPairs(orders1);
Console.WriteLine(result1); // Expected: 2

// Example 2:
// orders = [[8],[8,8],[1,2],[2,1],[1,1,2,2],[3]]
//
// Normalized forms:
// [8]         -> {8}   -> "8"
// [8,8]       -> {8}   -> "8"       => 1 pair
// [1,2]       -> {1,2} -> "1,2"
// [2,1]       -> {1,2} -> "1,2"     => +1 pair
// [1,1,2,2]   -> {1,2} -> "1,2"     => +2 pairs (with both previous "1,2" orders)
// [3]         -> {3}   -> "3"
//
// Total = 1 + 1 + 2 = 4
var orders2 = new List<IList<int>>
{
    new List<int> { 8 },
    new List<int> { 8, 8 },
    new List<int> { 1, 2 },
    new List<int> { 2, 1 },
    new List<int> { 1, 1, 2, 2 },
    new List<int> { 3 }
};

long result2 = solution.CountEquivalentOrderPairs(orders2);
Console.WriteLine(result2); // Expected: 4

// Additional demo from the description:
// [4,2,4,7], [7,2,4], [2,7,7,4]
//
// All normalize to {2,4,7}
// Number of unordered pairs among 3 equivalent orders = 3
var orders3 = new List<IList<int>>
{
    new List<int> { 4, 2, 4, 7 },
    new List<int> { 7, 2, 4 },
    new List<int> { 2, 7, 7, 4 }
};

long result3 = solution.CountEquivalentOrderPairs(orders3);
Console.WriteLine(result3); // Expected: 3