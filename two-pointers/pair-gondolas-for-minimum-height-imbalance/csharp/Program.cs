/*
Title: Pair Gondolas for Minimum Height Imbalance

Problem Description:
A mountain resort is loading visitors into 2-person gondolas. Each gondola must contain exactly two people,
and the resort wants the ride experience to feel as balanced as possible. You are given an integer array heights
where heights[i] is the height of the i-th visitor. The number of visitors is always even.

Your task is to pair up all visitors so that every visitor is used exactly once, and the maximum height difference
inside any single gondola is as small as possible. Return that minimum possible maximum difference.

For example, if one pairing creates gondolas with height differences [1, 4, 2], then the quality of that pairing is 4
because the worst gondola has a difference of 4. Among all valid pairings, you must minimize this worst-case value.

A common interview insight is that after sorting the array, the best strategy is not arbitrary matching.
The challenge is to reason about which visitors should be paired together so the largest gap is minimized globally.

Constraints:
- 2 <= heights.length <= 200000
- heights.length is even
- 1 <= heights[i] <= 1000000000
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - Scanning adjacent pairs takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - O(1) extra space beyond the sort implementation details
    - We sort the input array in place and only use a few variables

    Key Idea:
    After sorting, the optimal strategy is to pair adjacent people:
    (a[0], a[1]), (a[2], a[3]), (a[4], a[5]), ...

    Why this works:
    - Sorting puts similar heights next to each other.
    - If we ever "cross" pairs, we risk creating a larger gap unnecessarily.
    - Pairing neighbors keeps every individual pair as tight as possible locally,
      and this also minimizes the worst pair globally.
    */
    public int MinimizeMaximumDifference(int[] heights)
    {
        // Step 1: Sort the heights.
        //
        // What this does:
        // - Rearranges the visitors from shortest to tallest.
        //
        // Why this is necessary:
        // - Without sorting, we have no structure to reason about.
        // - Once sorted, people with similar heights become neighbors.
        // - Since we want to minimize the largest difference inside any gondola,
        //   it is natural to try to pair people who are close in height.
        //
        // Data structure choice:
        // - We use the input array itself.
        // - Array.Sort is efficient and built into .NET.
        Array.Sort(heights);

        // Step 2: Track the largest difference among the chosen pairs.
        //
        // What this variable means:
        // - As we form each gondola, we compute that gondola's height difference.
        // - The answer for a complete pairing is the maximum of those differences.
        // - So we maintain the running maximum while scanning the pairs.
        int maxDifference = 0;

        // Step 3: Pair adjacent visitors in the sorted array.
        //
        // Example:
        // If sorted heights are [150, 155, 160, 168],
        // we pair:
        // - 150 with 155
        // - 160 with 168
        //
        // Why adjacent pairing is optimal:
        // - Suppose we have sorted values a <= b <= c <= d.
        // - Pairing adjacent gives differences:
        //   (b - a) and (d - c)
        // - A crossed pairing like (a, d) and (b, c) creates a very large gap (d - a),
        //   which can only make the worst-case maximum larger or equal.
        // - Repeating this logic across the full sorted array leads to the optimal strategy:
        //   pair neighbors.
        //
        // Why i += 2:
        // - Each gondola uses exactly two people.
        // - Once we pair heights[i] with heights[i + 1], both are consumed.
        for (int i = 0; i < heights.Length; i += 2)
        {
            // Step 3a: Compute the difference inside the current gondola.
            //
            // Since the array is sorted, heights[i + 1] >= heights[i],
            // so the difference is simply:
            int currentDifference = heights[i + 1] - heights[i];

            // Step 3b: Update the worst gondola seen so far.
            //
            // Why this is necessary:
            // - The quality of a full pairing is determined by its largest pair difference.
            // - So after each pair, we compare against the running maximum.
            if (currentDifference > maxDifference)
            {
                maxDifference = currentDifference;
            }
        }

        // Step 4: Return the minimum possible maximum difference.
        //
        // Because adjacent pairing after sorting is optimal,
        // the maxDifference we computed is the best achievable answer.
        return maxDifference;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] heights1 = { 150, 160, 155, 168 };
int result1 = solution.MinimizeMaximumDifference((int[])heights1.Clone());
Console.WriteLine(result1); // Expected: 8

// Example 2
int[] heights2 = { 172, 180, 169, 171, 175, 178 };
int result2 = solution.MinimizeMaximumDifference((int[])heights2.Clone());
Console.WriteLine(result2); // Expected: 3

// Additional demo
int[] heights3 = { 100, 101 };
int result3 = solution.MinimizeMaximumDifference((int[])heights3.Clone());
Console.WriteLine(result3); // Expected: 1