/*
Title: Find Products Bought by Exactly One Customer Pair
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given purchase records from an online store. Each record is a pair [customerId, productId], meaning that the customer bought that product at least once. The same customer may appear multiple times with the same product in the input due to duplicate logs, but for this problem, repeated purchases of the same product by the same customer should count only once.

A product is called pair-exclusive if it was purchased by exactly two distinct customers, and no other customer purchased it. Your task is to return all pair-exclusive products grouped by the customer pair that bought them.

More formally, for every product, consider the set of distinct customers who purchased it. If the size of that set is exactly 2, let those customers be a and b. Then that product belongs to the pair (min(a, b), max(a, b)). Build a mapping from each such customer pair to the list of productIds that are pair-exclusive for that pair.

Return the result as a list of entries in the form [customerA, customerB, sortedProductIds], sorted first by customerA, then by customerB. Each sortedProductIds list must be in increasing order.

Constraints:
- 1 <= records.length <= 200000
- 1 <= customerId, productId <= 10^9
- Duplicate records may exist
- The total number of distinct (customerId, productId) pairs is at most 200000

Example 1:
Input: records = [[1,101],[2,101],[1,102],[3,102],[2,103],[3,103],[2,104],[1,104],[2,104]]
Output: [[1,2,[101,104]],[1,3,[102]],[2,3,[103]]]

Example 2:
Input: records = [[5,200],[7,200],[8,200],[5,201],[7,201],[5,202],[5,202],[9,203],[10,203]]
Output: [[5,7,[201]],[9,10,[203]]]
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of input records.
    - Let d be the number of distinct (customerId, productId) pairs, where d <= 200000.
    - Building the product -> distinct customers mapping takes O(n) average time because each record is processed once.
    - Scanning all products and grouping valid ones takes O(d) overall because each distinct customer-product relation is stored once.
    - Sorting each product list contributes to a total of O(k log k) across groups, where k is the number of pair-exclusive products.
    - Sorting the final list of customer pairs takes O(p log p), where p is the number of distinct qualifying customer pairs.
    - Overall: O(n + d + totalSortWork), which is efficient for the given constraints.

    Space Complexity:
    - We store a mapping from productId to the set of distinct customers who bought it: O(d)
    - We store the final grouping from customer pair to product list: O(k)
    - Overall: O(d)
    */
    public List<List<object>> FindPairExclusiveProducts(int[][] records)
    {
        // Step 1:
        // Build a mapping from each product to the set of DISTINCT customers who bought it.
        //
        // Why this is necessary:
        // The problem explicitly says duplicate logs should count only once.
        // That means if we see [2, 104] multiple times, customer 2 should still only appear once
        // in the customer set for product 104.
        //
        // Data structure choice:
        // - Dictionary<int, HashSet<int>>
        //   Key   = productId
        //   Value = set of distinct customerIds who bought that product
        //
        // This is a very natural fit:
        // - Dictionary gives fast average O(1) access by productId.
        // - HashSet automatically removes duplicates for the same customer-product pair.
        var productToCustomers = new Dictionary<int, HashSet<int>>();

        foreach (var record in records)
        {
            int customerId = record[0];
            int productId = record[1];

            // If this product has not been seen before, create an empty set for it.
            if (!productToCustomers.ContainsKey(productId))
            {
                productToCustomers[productId] = new HashSet<int>();
            }

            // Add the customer to the product's customer set.
            // If the same [customerId, productId] appears again, HashSet ignores the duplicate.
            productToCustomers[productId].Add(customerId);
        }

        // Step 2:
        // For every product, check whether it was bought by EXACTLY TWO distinct customers.
        //
        // If yes:
        // - Sort the two customer IDs into (smaller, larger)
        // - Group this product under that customer pair
        //
        // Why this is necessary:
        // The definition of "pair-exclusive" is exactly:
        // "purchased by exactly two distinct customers, and no one else."
        //
        // Data structure choice:
        // - Dictionary<(int, int), List<int>>
        //   Key   = ordered customer pair (a, b) where a < b
        //   Value = list of productIds that belong to this pair
        //
        // We use a tuple as the key because a pair of integers is exactly what we need.
        // We always store it in sorted order so that (1,2) and (2,1) are treated as the same pair.
        var pairToProducts = new Dictionary<(int, int), List<int>>();

        foreach (var entry in productToCustomers)
        {
            int productId = entry.Key;
            HashSet<int> customers = entry.Value;

            // Only products bought by exactly two DISTINCT customers qualify.
            if (customers.Count == 2)
            {
                // Extract the two customer IDs.
                //
                // Since HashSet does not guarantee order, we explicitly sort them.
                // This ensures the pair is always stored as (min, max).
                int[] twoCustomers = customers.ToArray();
                int customerA = Math.Min(twoCustomers[0], twoCustomers[1]);
                int customerB = Math.Max(twoCustomers[0], twoCustomers[1]);

                var pair = (customerA, customerB);

                // If this pair has not been seen before, create a new product list for it.
                if (!pairToProducts.ContainsKey(pair))
                {
                    pairToProducts[pair] = new List<int>();
                }

                // Add the current product to this pair's list.
                pairToProducts[pair].Add(productId);
            }

            // If customers.Count is 1:
            // - only one distinct customer bought it, so it is NOT pair-exclusive.
            //
            // If customers.Count is 3 or more:
            // - more than two customers bought it, so it is also NOT pair-exclusive.
            //
            // In both cases, we intentionally do nothing.
        }

        // Step 3:
        // Sort the product lists for each customer pair.
        //
        // Why this is necessary:
        // The problem requires each sortedProductIds list to be in increasing order.
        foreach (var pairEntry in pairToProducts)
        {
            pairEntry.Value.Sort();
        }

        // Step 4:
        // Build the final answer and sort it by customerA, then by customerB.
        //
        // Why this is necessary:
        // The output format requires:
        // [customerA, customerB, sortedProductIds]
        // and the entries themselves must be sorted by customerA, then customerB.
        //
        // We first sort the dictionary entries, then convert them into the requested structure.
        var sortedPairs = pairToProducts
            .OrderBy(entry => entry.Key.Item1)
            .ThenBy(entry => entry.Key.Item2);

        var result = new List<List<object>>();

        foreach (var entry in sortedPairs)
        {
            int customerA = entry.Key.Item1;
            int customerB = entry.Key.Item2;
            List<int> products = entry.Value;

            result.Add(new List<object>
            {
                customerA,
                customerB,
                products
            });
        }

        return result;
    }
}

static string FormatResult(List<List<object>> result)
{
    var formattedEntries = new List<string>();

    foreach (var entry in result)
    {
        int customerA = (int)entry[0];
        int customerB = (int)entry[1];
        var products = (List<int>)entry[2];

        string productText = "[" + string.Join(",", products) + "]";
        formattedEntries.Add($"[{customerA},{customerB},{productText}]");
    }

    return "[" + string.Join(",", formattedEntries) + "]";
}

var solution = new Solution();

// Demo 1:
// records = [[1,101],[2,101],[1,102],[3,102],[2,103],[3,103],[2,104],[1,104],[2,104]]
// Expected:
// [[1,2,[101,104]],[1,3,[102]],[2,3,[103]]]
int[][] records1 =
{
    new[] { 1, 101 },
    new[] { 2, 101 },
    new[] { 1, 102 },
    new[] { 3, 102 },
    new[] { 2, 103 },
    new[] { 3, 103 },
    new[] { 2, 104 },
    new[] { 1, 104 },
    new[] { 2, 104 }
};

var result1 = solution.FindPairExclusiveProducts(records1);
Console.WriteLine("Example 1 Output:");
Console.WriteLine(FormatResult(result1));
Console.WriteLine("Expected:");
Console.WriteLine("[[1,2,[101,104]],[1,3,[102]],[2,3,[103]]]");
Console.WriteLine();

// Demo 2:
// records = [[5,200],[7,200],[8,200],[5,201],[7,201],[5,202],[5,202],[9,203],[10,203]]
// Expected:
// [[5,7,[201]],[9,10,[203]]]
int[][] records2 =
{
    new[] { 5, 200 },
    new[] { 7, 200 },
    new[] { 8, 200 },
    new[] { 5, 201 },
    new[] { 7, 201 },
    new[] { 5, 202 },
    new[] { 5, 202 },
    new[] { 9, 203 },
    new[] { 10, 203 }
};

var result2 = solution.FindPairExclusiveProducts(records2);
Console.WriteLine("Example 2 Output:");
Console.WriteLine(FormatResult(result2));
Console.WriteLine("Expected:");
Console.WriteLine("[[5,7,[201]],[9,10,[203]]]");