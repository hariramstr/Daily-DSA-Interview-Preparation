/*
Title: Maximum Median Quality After Budgeted Increments

Problem Description:
You are given an integer array quality where quality[i] is the current quality score of the i-th manufactured part.
You are also given an integer budget representing the total number of increment operations available.
In one operation, you may choose any single part and increase its quality by 1.
You may distribute the operations across the array in any way.

Your goal is to maximize the median quality score of the array after using at most budget operations.
The median is defined as the middle element after sorting the array in non-decreasing order.
For an array of length n, use the element at index n / 2 in 0-based indexing after sorting.
For example, when n = 5, the median is the 3rd element; when n = 6, use the 4th element after sorting.

You may reorder the array only for the purpose of evaluating the median; the actual increment operations can be applied to any indices.
Return the maximum possible median value.

Constraints:
- 1 <= quality.length <= 200000
- 1 <= quality[i] <= 1000000000
- 0 <= budget <= 1000000000000

Important correctness note:
The mathematically correct interpretation of this classic problem is:
to make the median at least X after sorting, we must ensure that every element from the median index to the end
that is currently below X is raised up to X as needed. This is because those elements form the upper half that
determines whether the middle sorted element can reach X.

Under that correct interpretation:
- Example 1: quality = [1, 3, 5], budget = 4
  Sorted = [1, 3, 5], median index = 1
  To make median at least 6:
    raise 3 -> 6 costs 3
    raise 5 -> 6 costs 1
    total = 4, possible
  To make median at least 7:
    raise 3 -> 7 costs 4
    raise 5 -> 7 costs 2
    total = 6, not possible
  So the correct maximum median is 6, not 7.

- Example 2: quality = [2, 2, 8, 9, 9], budget = 6
  Sorted = [2, 2, 8, 9, 9], median index = 2
  To make median at least 10:
    raise 8 -> 10 costs 2
    raise 9 -> 10 costs 1
    raise 9 -> 10 costs 1
    total = 4, possible
  To make median at least 11:
    raise 8 -> 11 costs 3
    raise 9 -> 11 costs 2
    raise 9 -> 11 costs 2
    total = 7, not possible
  So the correct maximum median is 10.

This solution implements the correct algorithm and therefore prints:
- 6 for Example 1
- 10 for Example 2
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - Each binary search check scans the upper half of the array, which is O(n)
    - The number of binary search steps is O(log(budget + maxValue))
    - Total: O(n log n + n log(budget + maxValue))

    Space Complexity:
    - O(1) extra space beyond the sort implementation details
    - We sort the input array in place
    */
    public long MaxMedianQuality(int[] quality, long budget)
    {
        // Step 1:
        // Sort the array so that we can reason about the median position.
        //
        // Why sorting is necessary:
        // The median is defined after sorting.
        // Once the array is sorted, the median is simply the element at index n / 2.
        // Also, when we try to increase the median, the only elements that matter are
        // the median itself and the elements to its right.
        Array.Sort(quality);

        int n = quality.Length;

        // Step 2:
        // Compute the median index using 0-based indexing exactly as required.
        int medianIndex = n / 2;

        // Step 3:
        // The current median is the minimum answer we can always achieve.
        long left = quality[medianIndex];

        // Step 4:
        // Build a safe upper bound for binary search.
        //
        // Why this works:
        // In the absolute best case, if we spent every operation only on the median element,
        // the median could not exceed currentMedian + budget.
        // This is a valid upper bound even though in reality we often must also raise
        // some elements to the right to keep the median high after sorting.
        long right = quality[medianIndex] + budget;

        // Step 5:
        // Binary search on the answer.
        //
        // We ask:
        // "Is it possible to make the median at least 'mid' using at most 'budget' increments?"
        //
        // If yes, try a larger answer.
        // If no, try a smaller answer.
        while (left < right)
        {
            // We use the "upper mid" pattern to avoid infinite loops:
            // when left and right are adjacent, this ensures progress.
            long mid = left + (right - left + 1) / 2;

            // Check whether target median = mid is achievable.
            if (CanReachMedian(quality, medianIndex, budget, mid))
            {
                // If achievable, mid is a valid answer.
                // Move left up to search for an even larger median.
                left = mid;
            }
            else
            {
                // If not achievable, mid is too large.
                // Search the smaller half.
                right = mid - 1;
            }
        }

        // At the end of binary search, left == right and is the maximum achievable median.
        return left;
    }

    private bool CanReachMedian(int[] quality, int medianIndex, long budget, long target)
    {
        // This variable tracks how many increments are required in total
        // to make the median at least 'target'.
        long required = 0;

        // We only need to inspect the median position and everything to its right.
        //
        // Why only this range?
        // After sorting, the median is the middle element.
        // Elements to the left of the median do not help increase the median value,
        // because even if we raise them, they remain on the left side or may reshuffle,
        // but they do not reduce the amount needed for the upper half to support a larger median.
        //
        // To guarantee that the sorted middle element is at least 'target',
        // every element in the upper half that is below 'target' must be raised to 'target'.
        for (int i = medianIndex; i < quality.Length; i++)
        {
            // If this element is already at least target, it needs no work.
            if (quality[i] >= target)
            {
                continue;
            }

            // Otherwise, compute how many increments are needed to bring it up to target.
            required += target - quality[i];

            // Early stopping optimization:
            // If we already exceed the budget, there is no need to continue scanning.
            if (required > budget)
            {
                return false;
            }
        }

        // If total required increments fit within the budget, target is achievable.
        return required <= budget;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// Important note:
// The prompt claims the answer is 7, but that is not achievable under the correct median definition.
// The correct maximum median is 6.
int[] quality1 = { 1, 3, 5 };
long budget1 = 4;
long result1 = solution.MaxMedianQuality(quality1, budget1);
Console.WriteLine(result1); // Correct output: 6

// Example 2 from the prompt.
// This one is correct as written: the maximum median is 10.
int[] quality2 = { 2, 2, 8, 9, 9 };
long budget2 = 6;
long result2 = solution.MaxMedianQuality(quality2, budget2);
Console.WriteLine(result2); // Correct output: 10

// Additional quick demo
int[] quality3 = { 5 };
long budget3 = 100;
long result3 = solution.MaxMedianQuality(quality3, budget3);
Console.WriteLine(result3); // 105