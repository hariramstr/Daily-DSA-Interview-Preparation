/*
Title: Shortest Restock Span Covering All Essential Products

Problem Description:
A warehouse records the product ID of each item restocked during a day in the order they arrive.
You are given an integer array restocks, where restocks[i] is the product ID of the i-th restocked item,
and an integer array essentials containing the list of product IDs that must all appear in a report.

Your task is to return the length of the shortest contiguous span of restocks that contains every product ID
from essentials at least once. If no such span exists, return -1.

Duplicate values may appear in restocks, but essentials contains distinct product IDs.
The order of products inside the chosen span does not matter.
The goal is to find the smallest window in the array that covers the full required set.

This problem models a realistic inventory monitoring task where analysts need the smallest time interval
that proves all critical products were replenished.

Constraints:
- 1 <= restocks.length <= 200000
- 1 <= essentials.length <= 200000
- 1 <= product ID <= 1000000000
- essentials contains distinct values

Example 1:
Input: restocks = [7, 2, 5, 2, 9, 5, 1, 9, 7], essentials = [5, 1, 7]
Output: 4
Explanation: The shortest valid span is [5, 1, 9, 7], which has length 4.

Example 2:
Input: restocks = [4, 4, 3, 8, 6, 3, 2], essentials = [3, 2, 5]
Output: -1
Explanation: Product 5 never appears in restocks, so no contiguous span can cover all essential products.

Return only the minimum length, not the subarray itself.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m), where:
      n = restocks.Length
      m = essentials.Length
    Explanation:
    - We first load all essential product IDs into a HashSet in O(m).
    - Then we scan the restocks array with a sliding window.
    - Each index moves from left to right at most once, so the total work is O(n).

    Space Complexity:
    - O(m)
    Explanation:
    - We store the essential product IDs in a HashSet.
    - We also store counts for only the essential IDs currently tracked in the window.
    */
    public int ShortestRestockSpan(int[] restocks, int[] essentials)
    {
        // Step 1:
        // Put all required product IDs into a HashSet.
        //
        // Why?
        // We need very fast membership checks:
        // "Is this restocked product one of the essential products?"
        //
        // A HashSet gives average O(1) lookup time, which is ideal for large inputs.
        var required = new HashSet<int>(essentials);

        // This is the total number of distinct essential products we must cover.
        int totalRequired = required.Count;

        // Step 2:
        // Use a dictionary to count how many times each essential product appears
        // inside the current sliding window.
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know
        // whether removing one occurrence causes that product to disappear completely
        // from the window.
        var windowCounts = new Dictionary<int, int>();

        // This variable tells us how many distinct essential product IDs are currently
        // satisfied inside the window.
        //
        // Example:
        // If essentials = [5, 1, 7]
        // and the current window contains 5 and 7 but not 1,
        // then formed = 2.
        int formed = 0;

        // Left pointer of the sliding window.
        int left = 0;

        // Best answer found so far.
        // We start with int.MaxValue to mean "no valid window found yet".
        int bestLength = int.MaxValue;

        // Step 3:
        // Expand the window by moving the right pointer from left to right.
        //
        // This is the classic sliding window pattern:
        // - Expand right to include more elements
        // - Once the window becomes valid, shrink left to make it as small as possible
        for (int right = 0; right < restocks.Length; right++)
        {
            int currentProduct = restocks[right];

            // Step 3a:
            // Only care about products that are essential.
            //
            // Non-essential products can still sit inside the window,
            // but they do not help satisfy the requirement,
            // so we do not need to count them in our dictionary.
            if (required.Contains(currentProduct))
            {
                // Add/update the count of this essential product in the current window.
                if (!windowCounts.ContainsKey(currentProduct))
                {
                    windowCounts[currentProduct] = 0;
                }

                windowCounts[currentProduct]++;

                // If the count just became 1, that means this essential product
                // is now present in the window for the first time.
                //
                // That increases the number of satisfied distinct essentials.
                if (windowCounts[currentProduct] == 1)
                {
                    formed++;
                }
            }

            // Step 4:
            // If formed == totalRequired, then the current window [left..right]
            // contains every essential product at least once.
            //
            // Now we try to shrink it from the left while it remains valid,
            // because we want the shortest such window.
            while (formed == totalRequired)
            {
                // Current window length.
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                int leftProduct = restocks[left];

                // We are about to remove restocks[left] from the window
                // by moving left forward.
                //
                // If that product is essential, we must update its count.
                if (required.Contains(leftProduct))
                {
                    windowCounts[leftProduct]--;

                    // If the count becomes 0, then after removing this element,
                    // the window no longer contains that essential product.
                    //
                    // Therefore, the window stops being valid,
                    // and formed must decrease.
                    if (windowCounts[leftProduct] == 0)
                    {
                        formed--;
                    }
                }

                // Actually shrink the window from the left.
                left++;
            }
        }

        // Step 5:
        // If bestLength was never updated, then no valid window exists.
        // Return -1 as required by the problem.
        if (bestLength == int.MaxValue)
        {
            return -1;
        }

        // Otherwise return the minimum valid window length found.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] restocks1 = { 7, 2, 5, 2, 9, 5, 1, 9, 7 };
int[] essentials1 = { 5, 1, 7 };
int result1 = solution.ShortestRestockSpan(restocks1, essentials1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] restocks2 = { 4, 4, 3, 8, 6, 3, 2 };
int[] essentials2 = { 3, 2, 5 };
int result2 = solution.ShortestRestockSpan(restocks2, essentials2);
Console.WriteLine(result2); // Expected: -1