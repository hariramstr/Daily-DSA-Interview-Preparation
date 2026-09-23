/*
Title: Locate First Warehouse Shelf With Required Capacity

Problem Description:
A warehouse stores bins on shelves arranged from left to right. The shelves are indexed from 0 to n - 1,
and the capacity of each shelf is given in a non-decreasing integer array `capacities`, where
`capacities[i]` is the maximum weight that shelf `i` can safely hold.

Given `capacities` and an integer `requiredWeight`, return the index of the first shelf whose capacity
is greater than or equal to `requiredWeight`. If no shelf can hold that weight, return `-1`.

Because the array is sorted, the intended solution uses binary search to efficiently find the lower bound:
the first position where the value is at least `requiredWeight`.

Example 1:
capacities = [5, 8, 8, 12, 15], requiredWeight = 8
Output: 1

Example 2:
capacities = [3, 4, 6, 9], requiredWeight = 10
Output: -1
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the input array is already sorted in non-decreasing order.
    Our goal is not just to find any shelf with enough capacity, but specifically the FIRST shelf
    whose capacity is greater than or equal to requiredWeight.

    This is a classic "lower bound" binary search pattern.
    */
    public int FindFirstShelfWithRequiredCapacity(int[] capacities, int requiredWeight)
    {
        // `left` points to the beginning of the current search range.
        // We start at index 0 because the answer, if it exists, could be the very first shelf.
        int left = 0;

        // `right` points to the end of the current search range.
        // We start at the last valid index in the array.
        int right = capacities.Length - 1;

        // `answer` stores the best valid index found so far.
        // We initialize it to -1 to mean "no valid shelf found yet".
        // If we never find a shelf with enough capacity, we will return -1.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        // When left becomes greater than right, the search space is empty.
        while (left <= right)
        {
            // Compute the middle index of the current search range.
            // We use this form instead of (left + right) / 2 to avoid integer overflow
            // in general binary search implementations.
            int mid = left + (right - left) / 2;

            // Check whether the shelf at `mid` can hold the required weight.
            if (capacities[mid] >= requiredWeight)
            {
                // This shelf is valid because its capacity is large enough.
                // However, we are looking for the FIRST such shelf, not just any valid shelf.
                // So we record this index as a possible answer...
                answer = mid;

                // ...and then continue searching to the LEFT side to see whether there is
                // an earlier shelf that is also valid.
                //
                // Why is this safe?
                // Because the array is sorted. If capacities[mid] is already large enough,
                // then any earlier valid answer must be somewhere between left and mid - 1.
                right = mid - 1;
            }
            else
            {
                // capacities[mid] < requiredWeight
                //
                // This shelf cannot hold the required weight.
                // Because the array is sorted in non-decreasing order, every shelf to the LEFT
                // of `mid` must have capacity less than or equal to capacities[mid], which means
                // those shelves also cannot hold the required weight.
                //
                // Therefore, we can safely discard the entire left half including `mid`,
                // and continue searching only on the RIGHT side.
                left = mid + 1;
            }
        }

        // If we found at least one valid shelf, `answer` holds the first such index.
        // Otherwise, it is still -1.
        return answer;
    }
}

// Demo code:
// We create sample inputs from the problem statement, call the solution,
// and print the outputs so the program is fully runnable.

var solution = new Solution();

// Example 1:
// capacities = [5, 8, 8, 12, 15], requiredWeight = 8
// Expected output: 1
int[] capacities1 = { 5, 8, 8, 12, 15 };
int requiredWeight1 = 8;
int result1 = solution.FindFirstShelfWithRequiredCapacity(capacities1, requiredWeight1);
Console.WriteLine(result1);

// Example 2:
// capacities = [3, 4, 6, 9], requiredWeight = 10
// Expected output: -1
int[] capacities2 = { 3, 4, 6, 9 };
int requiredWeight2 = 10;
int result2 = solution.FindFirstShelfWithRequiredCapacity(capacities2, requiredWeight2);
Console.WriteLine(result2);