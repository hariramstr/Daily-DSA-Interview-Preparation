/*
Title: Longest Pantry Restock Run Within Box Capacity
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A grocery warehouse packs a sequence of pantry items into a single moving box belt.
The items must stay in their original order, and the packer wants to choose one
contiguous run of items to place into a box. Each item has a size value in sizes[i],
and the total size of all chosen items must be less than or equal to capacity.

Your task is to return the length of the longest contiguous subarray whose sum is
at most capacity.

If multiple valid runs have the same maximum length, you only need to return the
length, not the subarray itself. If no item can fit in the box, return 0.

This problem is designed to be solved efficiently using a sliding window because
all item sizes are positive. As you expand the right end of the window, the total
size increases. If the total becomes too large, move the left end forward until
the window becomes valid again.

Constraints:
- 1 <= sizes.length <= 100000
- 1 <= sizes[i] <= 10000
- 1 <= capacity <= 1000000000
- All values in sizes are positive integers.

Example 1:
Input: sizes = [2, 1, 3, 2, 1], capacity = 6
Output: 3
Explanation: The longest valid contiguous run is [1, 3, 2] or [3, 2, 1], both with total size 6.

Example 2:
Input: sizes = [5, 4, 2, 1], capacity = 3
Output: 2
Explanation: Item 5 and item 4 are too large by themselves. The longest valid run is [2, 1], which has length 2.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once by moving the right pointer.
    - Each element is removed from the window at most once by moving the left pointer.
    - Because both pointers only move forward, the total work is linear.

    Space Complexity: O(1)
    - We only use a few extra variables: left, currentSum, and bestLength.
    - No extra arrays or collections are needed.
    */
    public int LongestRunWithinCapacity(int[] sizes, int capacity)
    {
        // The left pointer marks the beginning of our current sliding window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // We store the sum of the current window.
        // We use long instead of int for extra safety, even though the given constraints
        // still fit within int in many cases. Using long avoids accidental overflow if
        // constraints are changed or if sums become large during intermediate steps.
        long currentSum = 0;

        // This variable keeps track of the longest valid window length found so far.
        int bestLength = 0;

        // We expand the window one element at a time by moving the right pointer.
        // For every position "right", we include sizes[right] into the current window.
        for (int right = 0; right < sizes.Length; right++)
        {
            // Step 1: Add the new rightmost element into the window sum.
            // Why?
            // Because we are trying to consider every possible contiguous subarray
            // that ends at index "right". Expanding the window is the natural first step.
            currentSum += sizes[right];

            // Step 2: If the window sum is too large, shrink the window from the left.
            // Why is this valid?
            // Because all numbers are positive.
            // That means:
            // - Adding more elements to the right can only increase the sum.
            // - To reduce the sum, we must remove elements from the left.
            //
            // We keep shrinking until the window becomes valid again:
            // currentSum <= capacity
            while (left <= right && currentSum > capacity)
            {
                // Remove the element at the left edge from the sum,
                // because that element is no longer part of the window.
                currentSum -= sizes[left];

                // Move the left edge forward to make the window smaller.
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid
            // (or possibly empty in unusual edge behavior, though not needed here).
            // Its sum is guaranteed to be <= capacity.
            //
            // So now we can compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer
            // than any valid window we have seen before.
            //
            // Why do we only update after shrinking?
            // Because only then do we know the window satisfies the condition.
            if (currentSum <= capacity && currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After checking all possible right endpoints, bestLength contains
        // the maximum length of any contiguous subarray with sum <= capacity.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] sizes1 = { 2, 1, 3, 2, 1 };
int capacity1 = 6;
int result1 = solution.LongestRunWithinCapacity(sizes1, capacity1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
int[] sizes2 = { 5, 4, 2, 1 };
int capacity2 = 3;
int result2 = solution.LongestRunWithinCapacity(sizes2, capacity2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 2

// Additional demo: no single item fits
int[] sizes3 = { 7, 8, 9 };
int capacity3 = 5;
int result3 = solution.LongestRunWithinCapacity(sizes3, capacity3);
Console.WriteLine("Additional Example Result: " + result3); // Expected: 0