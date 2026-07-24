/*
Title: Maximum Upgrade Score from One Contiguous Patch Window

Problem Description:
A software team tracks the impact score of each available patch in the order the patches must be applied.
The array `impact` contains positive, negative, or zero values, where `impact[i]` is the score contributed
by the i-th patch.

The team is allowed to choose exactly one contiguous window of patches to deploy together.
If the chosen window has length L, the final score is:

    sum(window) - L * penalty

For every pair of indices l and r with 0 <= l <= r < n:

    score(l, r) = impact[l] + impact[l+1] + ... + impact[r] - (r - l + 1) * penalty

We must return the maximum possible score over all non-empty contiguous windows.

Key observation:
For a fixed window, subtracting `penalty` once per included element is the same as transforming
each element:

    transformed[i] = impact[i] - penalty

Then:

    score(l, r) = transformed[l] + transformed[l+1] + ... + transformed[r]

So the problem becomes:
Find the maximum sum of any non-empty contiguous subarray in the transformed array.

That is the classic "maximum subarray sum" problem, solved efficiently with Kadane's algorithm.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We scan the array exactly once.
    We only keep a few running variables, so extra memory usage is constant.
    */
    public long MaximumUpgradeScore(int[] impact, int penalty)
    {
        // Because the problem guarantees at least one element, we can safely initialize
        // our running values from the first transformed element.
        //
        // We use long everywhere in the computation because:
        // - impact[i] can be as large as 1e9 in magnitude
        // - n can be as large as 200000
        // - sums can therefore exceed the range of int
        long firstTransformed = (long)impact[0] - penalty;

        // currentBestEndingHere:
        // This stores the maximum score of a non-empty contiguous window that MUST end
        // at the current index.
        //
        // Why do we need this?
        // Kadane's algorithm works by deciding, at each position:
        // - either extend the previous best window ending at the previous index
        // - or start a brand-new window at the current index
        long currentBestEndingHere = firstTransformed;

        // globalBest:
        // This stores the best score seen anywhere so far among all windows examined.
        //
        // Why separate from currentBestEndingHere?
        // Because the best overall window may end earlier than the current index.
        long globalBest = firstTransformed;

        // Process the rest of the array from left to right.
        for (int i = 1; i < impact.Length; i++)
        {
            // Transform the current value by subtracting the penalty once.
            //
            // This is the crucial reduction:
            // Including one more patch in the chosen window adds impact[i] to the raw sum,
            // but also adds one more unit of penalty.
            // So the net contribution of this patch is exactly:
            //     impact[i] - penalty
            long transformed = (long)impact[i] - penalty;

            // Decide the best non-empty window that ends exactly at index i.
            //
            // There are only two possibilities:
            //
            // 1) Start a new window at i
            //    Score = transformed
            //
            // 2) Extend the best window that ended at i - 1
            //    Score = currentBestEndingHere + transformed
            //
            // We choose the larger of these two.
            //
            // Why is this correct?
            // Any window ending at i either:
            // - contains only element i, or
            // - has some earlier start, meaning it is an extension of a window ending at i-1
            currentBestEndingHere = Math.Max(transformed, currentBestEndingHere + transformed);

            // Update the global best answer if the best window ending at i
            // is better than every window we have seen before.
            globalBest = Math.Max(globalBest, currentBestEndingHere);
        }

        // globalBest now contains the maximum score over all non-empty contiguous windows.
        return globalBest;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] impact1 = { 8, -1, 3, -2, 4 };
int penalty1 = 2;
long result1 = solution.MaximumUpgradeScore(impact1, penalty1);
Console.WriteLine(result1); // Expected: 4

// Example 2
// The problem statement contains a corrected explanation showing the true answer is 4.
int[] impact2 = { -5, 7, -1, 7, -6 };
int penalty2 = 3;
long result2 = solution.MaximumUpgradeScore(impact2, penalty2);
Console.WriteLine(result2); // Expected: 4

// Additional quick sanity checks

// Single element
int[] impact3 = { 10 };
int penalty3 = 4;
long result3 = solution.MaximumUpgradeScore(impact3, penalty3);
Console.WriteLine(result3); // Expected: 6

// All values become negative after penalty, so best is the least negative single element
int[] impact4 = { 1, 2, 3 };
int penalty4 = 10;
long result4 = solution.MaximumUpgradeScore(impact4, penalty4);
Console.WriteLine(result4); // Expected: -7

// Mixed values
int[] impact5 = { 5, -2, 6, -1, 4 };
int penalty5 = 1;
long result5 = solution.MaximumUpgradeScore(impact5, penalty5);
Console.WriteLine(result5); // Expected transformed: [4,-3,5,-2,3], best = 7