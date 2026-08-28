/*
Title: Find Insertion Slot for a Sorted Event Timeline
Difficulty: Easy
Topic: Binary Search

Problem Description:
You are given a sorted array `times` representing event start times in minutes from the beginning of the day.
The array is sorted in non-decreasing order, and duplicate values may exist because multiple events can start
at the same minute. You are also given an integer `target`, representing the start time of a new event.

Return the index where `target` should be inserted so that the array remains sorted after insertion.
If `target` already exists, return the leftmost index where it appears. In other words, you must find
the first position `i` such that `times[i] >= target`. If no such position exists, return `times.length`.

Your solution should run in O(log n) time, which makes binary search the intended approach.

Constraints:
- 0 <= times.length <= 100000
- 0 <= times[i] <= 1440
- times is sorted in non-decreasing order
- 0 <= target <= 1440

Example 1:
Input: times = [15, 30, 30, 45, 90], target = 30
Output: 1
Explanation: The value 30 already exists, and the leftmost valid insertion position is index 1.

Example 2:
Input: times = [10, 20, 40, 80], target = 35
Output: 2
Explanation: Inserting 35 at index 2 gives [10, 20, 35, 40, 80], which is still sorted.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the input array is already sorted.
    Binary search repeatedly cuts the search space in half, which is why it is much faster
    than checking every element one by one for large arrays.

    Our goal is NOT just to find whether target exists.
    Our real goal is to find the FIRST index where times[index] >= target.

    This is sometimes called:
    - lower bound
    - leftmost insertion position
    - first valid insertion slot
    */
    public int SearchInsert(int[] times, int target)
    {
        // "left" marks the beginning of the current search range.
        // Initially, we can search from index 0.
        int left = 0;

        // "right" marks the end of the current search range.
        // Initially, we can search up to the last valid index in the array.
        int right = times.Length - 1;

        // We keep searching while there is still a valid range to inspect.
        // When left becomes greater than right, it means the search range is empty,
        // and at that moment "left" will be exactly the correct insertion index.
        while (left <= right)
        {
            // We calculate the middle index of the current search range.
            // This version avoids potential integer overflow:
            // instead of (left + right) / 2, we use left + (right - left) / 2.
            int mid = left + (right - left) / 2;

            // Case 1:
            // If the middle value is LESS than target,
            // then mid cannot be the answer, and neither can anything to its left,
            // because all those values are also <= times[mid] and therefore still < target
            // due to the array being sorted.
            //
            // So the first position where value >= target must be to the RIGHT of mid.
            if (times[mid] < target)
            {
                left = mid + 1;
            }
            else
            {
                // Case 2:
                // If times[mid] is GREATER than or EQUAL to target,
                // then mid is a possible answer.
                //
                // But we are asked for the LEFTMOST such index.
                // So we do not stop here.
                // Instead, we continue searching on the LEFT side
                // to see whether there is an earlier valid position.
                right = mid - 1;
            }
        }

        // At the end of the loop:
        // - every index before "left" contains a value < target
        // - every index at or after "left" is a candidate position for value >= target
        //
        // Therefore, "left" is exactly the first index where target can be inserted
        // while keeping the array sorted.
        //
        // This also correctly handles:
        // - empty array: returns 0
        // - target smaller than all elements: returns 0
        // - target larger than all elements: returns times.Length
        // - duplicates: returns the leftmost matching index
        return left;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the problem statement:
// times = [15, 30, 30, 45, 90], target = 30
// Expected output: 1
int[] times1 = { 15, 30, 30, 45, 90 };
int target1 = 30;
int result1 = solution.SearchInsert(times1, target1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2 from the problem statement:
// times = [10, 20, 40, 80], target = 35
// Expected output: 2
int[] times2 = { 10, 20, 40, 80 };
int target2 = 35;
int result2 = solution.SearchInsert(times2, target2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo 1:
// Insert at the beginning
int[] times3 = { 20, 40, 60 };
int target3 = 10;
int result3 = solution.SearchInsert(times3, target3);
Console.WriteLine($"Additional Demo 1 Result: {result3}");

// Additional demo 2:
// Insert at the end
int[] times4 = { 20, 40, 60 };
int target4 = 100;
int result4 = solution.SearchInsert(times4, target4);
Console.WriteLine($"Additional Demo 2 Result: {result4}");

// Additional demo 3:
// Empty array
int[] times5 = Array.Empty<int>();
int target5 = 50;
int result5 = solution.SearchInsert(times5, target5);
Console.WriteLine($"Additional Demo 3 Result: {result5}");