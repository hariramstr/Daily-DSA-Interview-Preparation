/*
Title: Count Products With a Unique Reviewer Set
Difficulty: Medium
Topic: Hashing

Problem Description:
An e-commerce platform stores product reviews as pairs of integers [productId, userId].
A product may be reviewed multiple times by the same user due to edits or updated ratings,
but for this problem only the set of distinct users who reviewed each product matters.

Two products are considered equivalent if they were reviewed by exactly the same set of users,
regardless of review order or duplicate entries.

Your task is to return the number of products whose reviewer set is unique among all products
that appear in the input. In other words, build the distinct reviewer set for every product,
group products by these sets, and count how many products belong to a group of size 1.

A product should be considered only if it appears in at least one review record.
Duplicate review records for the same (productId, userId) pair do not change the reviewer set.

Constraints:
- 1 <= reviews.length <= 2 * 10^5
- 1 <= productId, userId <= 10^9
- Each review is a pair [productId, userId]
- The answer must be computed in near-linear time relative to the number of review records

Examples:
1) reviews = [[101,1],[101,2],[102,2],[102,1],[103,3],[103,3],[104,4]]
   Product 101 -> {1,2}
   Product 102 -> {1,2}
   Product 103 -> {3}
   Product 104 -> {4}
   Unique groups are {3} and {4}, so answer = 2

2) reviews = [[10,7],[10,8],[11,7],[12,8],[12,8],[13,9],[13,10],[14,9],[14,10]]
   Product 10 -> {7,8}
   Product 11 -> {7}
   Product 12 -> {8}
   Product 13 -> {9,10}
   Product 14 -> {9,10}
   Unique groups are {7,8}, {7}, and {8}, so answer = 3
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Building the per-product reviewer sets: O(n) average, where n is the number of review records
    - Sorting each product's distinct reviewer list so that equal sets get the same canonical order:
      total O(sum(k_i log k_i)), where k_i is the number of distinct reviewers for product i
    - Grouping products by canonical reviewer-set key: O(total distinct reviewers) average
    Overall: near-linear in practice, with sorting needed to create a stable representation of each set

    Space Complexity:
    - O(total distinct (productId, userId) pairs) for storing reviewer sets
    - O(number of products) for grouping identical reviewer-set keys
    */
    public int CountProductsWithUniqueReviewerSet(int[][] reviews)
    {
        // Step 1:
        // Build a mapping from each product to the set of distinct users who reviewed it.
        //
        // Why this is necessary:
        // The problem explicitly says duplicate reviews by the same user for the same product
        // should not matter. That means we cannot simply count raw review rows.
        // We must first reduce each product's reviewers to a mathematical set.
        //
        // Data structure choice:
        // - Dictionary<int, HashSet<int>>
        //   Key   -> productId
        //   Value -> set of distinct userIds for that product
        //
        // This is a natural fit because:
        // - Dictionary gives fast average O(1) access by productId
        // - HashSet automatically removes duplicate userIds for the same product
        var productToReviewers = new Dictionary<int, HashSet<int>>();

        foreach (var review in reviews)
        {
            int productId = review[0];
            int userId = review[1];

            // If this is the first time we have seen this product,
            // create an empty reviewer set for it.
            if (!productToReviewers.TryGetValue(productId, out var reviewerSet))
            {
                reviewerSet = new HashSet<int>();
                productToReviewers[productId] = reviewerSet;
            }

            // Add the user to the set.
            // If the same (productId, userId) appears multiple times,
            // HashSet ignores duplicates automatically.
            reviewerSet.Add(userId);
        }

        // Step 2:
        // Convert each product's reviewer set into a canonical representation.
        //
        // Why this is necessary:
        // Two products are equivalent if they have exactly the same set of users.
        // But HashSet does not preserve order, and two equal sets may be enumerated
        // in different orders. Therefore, we need a stable representation so that:
        //
        //   {1,2} and {2,1}
        //
        // become the same key.
        //
        // The standard approach is:
        // - Copy the set to a list
        // - Sort the list
        // - Join the sorted values into a string key
        //
        // Example:
        //   reviewer set {2,1} -> sorted [1,2] -> key "1,2"
        //
        // Then all products with the same reviewer set produce the same key.
        //
        // Data structure choice:
        // - Dictionary<string, int>
        //   Key   -> canonical reviewer-set representation
        //   Value -> how many products have exactly that reviewer set
        var reviewerSetGroupCount = new Dictionary<string, int>();

        foreach (var entry in productToReviewers)
        {
            HashSet<int> reviewerSet = entry.Value;

            // Copy the set into a list so we can sort it.
            var sortedReviewers = reviewerSet.ToList();

            // Sorting is the key step that makes the representation canonical.
            // Without sorting, equal sets could produce different keys depending
            // on enumeration order, which would be incorrect.
            sortedReviewers.Sort();

            // Build a stable string key from the sorted reviewer IDs.
            // Because userId values are integers and we use a comma separator,
            // different lists produce different keys:
            // [1,23] -> "1,23"
            // [12,3] -> "12,3"
            string key = string.Join(",", sortedReviewers);

            // Count how many products share this exact reviewer set.
            if (reviewerSetGroupCount.ContainsKey(key))
            {
                reviewerSetGroupCount[key]++;
            }
            else
            {
                reviewerSetGroupCount[key] = 1;
            }
        }

        // Step 3:
        // Count how many products belong to a reviewer-set group of size 1.
        //
        // Why this is necessary:
        // The problem asks for the number of products whose reviewer set is unique
        // among all products in the input.
        //
        // If a key appears:
        // - once  -> that one product is unique, so contribute 1
        // - twice -> both products are not unique, contribute 0
        // - more  -> none of them are unique, contribute 0
        //
        // Since each group with count 1 contributes exactly one product,
        // we can simply count how many group counts equal 1.
        int uniqueProductCount = 0;

        foreach (var group in reviewerSetGroupCount)
        {
            if (group.Value == 1)
            {
                uniqueProductCount++;
            }
        }

        return uniqueProductCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] reviews1 =
{
    new[] { 101, 1 },
    new[] { 101, 2 },
    new[] { 102, 2 },
    new[] { 102, 1 },
    new[] { 103, 3 },
    new[] { 103, 3 },
    new[] { 104, 4 }
};

int result1 = solution.CountProductsWithUniqueReviewerSet(reviews1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[][] reviews2 =
{
    new[] { 10, 7 },
    new[] { 10, 8 },
    new[] { 11, 7 },
    new[] { 12, 8 },
    new[] { 12, 8 },
    new[] { 13, 9 },
    new[] { 13, 10 },
    new[] { 14, 9 },
    new[] { 14, 10 }
};

int result2 = solution.CountProductsWithUniqueReviewerSet(reviews2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity check
int[][] reviews3 =
{
    new[] { 1, 100 },
    new[] { 1, 100 },
    new[] { 2, 100 },
    new[] { 2, 100 }
};

int result3 = solution.CountProductsWithUniqueReviewerSet(reviews3);
Console.WriteLine(result3); // Expected: 0 because both products have reviewer set {100}