/*
Title: Find the First Duplicate SKU
Difficulty: Easy
Topic: Hashing

Problem Description:
You are given a list of product SKU codes representing items scanned at a warehouse receiving station,
in the exact order they were scanned. A SKU code is a string containing letters, digits, or hyphens.
Your task is to return the first SKU that appears more than once in the scan history.

The phrase first duplicate means the duplicate whose second appearance happens earliest in the list.
In other words, scan the list from left to right and return the first SKU that has already been seen before.
If no SKU appears twice, return an empty string.

This problem is useful for detecting the earliest repeated item in a real-time stream of inventory events.
An efficient solution should avoid comparing every pair of strings and should instead use a hash-based
structure to track which SKUs have already appeared.

Constraints:
- 1 <= skus.length <= 100000
- 1 <= skus[i].length <= 50
- Each skus[i] consists of English letters, digits, and '-' only
- Comparison is case-sensitive, so "ab-1" and "AB-1" are different

Example 1:
Input: skus = ["BX-12", "A7", "Q9", "A7", "BX-12"]
Output: "A7"
Explanation: "A7" is the first SKU whose second occurrence is encountered while scanning from left to right.

Example 2:
Input: skus = ["P1", "R2", "S3", "T4"]
Output: ""
Explanation: No SKU is repeated, so return an empty string.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the SKU list exactly once.
    - Each HashSet operation (Contains / Add) is O(1) on average.

    Space Complexity: O(n)
    - In the worst case, all SKUs are unique, so we store all of them in the HashSet.

    Beginner-friendly intuition:
    - We move from left to right through the scan history.
    - We remember every SKU we have seen before.
    - The moment we find a SKU that is already remembered, that SKU is the answer.
    - This works because we are scanning in order, so the first repeated SKU we encounter
      is exactly the one whose second appearance happens earliest.
    */
    public string FirstDuplicateSku(string[] skus)
    {
        // We use a HashSet<string> because it is a very efficient data structure
        // for answering the question:
        // "Have I seen this exact SKU before?"
        //
        // Why HashSet?
        // - It stores unique values only.
        // - It gives very fast average-time lookups.
        // - This is much better than checking a List every time, which would be slower.
        var seenSkus = new HashSet<string>();

        // We scan the array from left to right because the problem defines
        // the answer based on the earliest second appearance.
        //
        // That means order matters:
        // - As soon as we find a SKU that has already appeared before,
        //   we should immediately return it.
        foreach (var sku in skus)
        {
            // Step 1: Check whether the current SKU is already in our set of seen SKUs.
            //
            // What this is doing:
            // - It asks: "Have we scanned this SKU earlier in the list?"
            //
            // Why this is necessary:
            // - A duplicate means the same SKU appears again after already being seen once.
            // - If the current SKU is already in the set, then this current position is its
            //   second (or later) appearance.
            //
            // If true, this is the first duplicate encountered during our left-to-right scan,
            // so it is exactly the correct answer.
            if (seenSkus.Contains(sku))
            {
                return sku;
            }

            // Step 2: If the SKU was not seen before, add it to the set.
            //
            // What this is doing:
            // - We record that this SKU has now appeared in the scan history.
            //
            // Why this is necessary:
            // - Future elements need to know whether this SKU has already been encountered.
            // - Without storing it, we would not be able to detect duplicates efficiently.
            seenSkus.Add(sku);
        }

        // If we finish scanning the entire list and never find a repeated SKU,
        // then there is no duplicate at all.
        //
        // The problem asks us to return an empty string in that case.
        return "";
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem description:
// Scan order:
// 1. "BX-12" -> first time, store it
// 2. "A7"    -> first time, store it
// 3. "Q9"    -> first time, store it
// 4. "A7"    -> already seen, so this is the first duplicate
string[] skus1 = ["BX-12", "A7", "Q9", "A7", "BX-12"];
string result1 = solution.FirstDuplicateSku(skus1);
Console.WriteLine(result1); // Expected: A7

// Example 2 from the problem description:
// Every SKU appears only once, so the answer should be an empty string.
string[] skus2 = ["P1", "R2", "S3", "T4"];
string result2 = solution.FirstDuplicateSku(skus2);
Console.WriteLine(result2); // Expected: (empty line)

// Additional demo:
// The second appearance of "X-1" happens before the second appearance of "Y-2",
// so "X-1" should be returned.
string[] skus3 = ["X-1", "Y-2", "Z-3", "X-1", "Y-2"];
string result3 = solution.FirstDuplicateSku(skus3);
Console.WriteLine(result3); // Expected: X-1