/*
Title: Locate the First Train Arrival Not Earlier Than Target
Difficulty: Easy
Topic: Binary Search

Problem Description:
You are given a sorted array `arrivals` where `arrivals[i]` represents the scheduled arrival time of the `i`-th train in minutes after midnight. The array is sorted in non-decreasing order, so multiple trains may share the same arrival time. You are also given an integer `target`, representing the earliest time a passenger is willing to board.

Your task is to return the index of the first train whose arrival time is greater than or equal to `target`. If no such train exists, return `-1`.

This problem models a common lookup operation in booking and scheduling systems: finding the earliest available option that satisfies a minimum requirement. A linear scan works, but the input is already sorted, so an efficient binary search solution is expected.

Constraints:
- `1 <= arrivals.length <= 10^5`
- `0 <= arrivals[i] <= 1439`
- `arrivals` is sorted in non-decreasing order
- `0 <= target <= 1439`

Example 1:
Input: arrivals = [120, 180, 180, 240, 315], target = 181
Output: 3
Explanation: The first arrival not earlier than 181 is 240, which is at index 3.

Example 2:
Input: arrivals = [60, 90, 150, 150, 210], target = 150
Output: 2
Explanation: There are trains at time 150, and the first such train appears at index 2.

If every train arrives before `target`, the answer should be `-1`.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    - In each step, binary search cuts the remaining search range roughly in half.
    - Because of that, even for large arrays, the number of checks stays small.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra arrays, lists, or recursion are used.
    */
    public int FirstArrivalNotEarlierThan(int[] arrivals, int target)
    {
        // We use binary search because the array is already sorted.
        // That sorted order is the key property that lets us skip large portions
        // of the array instead of checking every element one by one.

        // 'left' marks the beginning of the current search range.
        int left = 0;

        // 'right' marks the end of the current search range.
        int right = arrivals.Length - 1;

        // We store the best valid answer found so far in 'answer'.
        // We start with -1, which means "no valid train found yet".
        // If we never find an arrival >= target, this value will remain -1,
        // which is exactly what the problem asks us to return.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        // When left becomes greater than right, it means the search space is empty.
        while (left <= right)
        {
            // Compute the middle index safely.
            // We use left + (right - left) / 2 instead of (left + right) / 2
            // as a standard safe binary search pattern.
            int mid = left + (right - left) / 2;

            // Read the value at the middle position once so the code is easier to follow.
            int currentArrival = arrivals[mid];

            // Case 1:
            // If the current arrival is greater than or equal to the target,
            // then this train is a VALID candidate.
            if (currentArrival >= target)
            {
                // Since this index works, record it as the current best answer.
                // But we are not done yet:
                // there might be another valid train even earlier in the array.
                // Because we need the FIRST index, we must continue searching left.
                answer = mid;

                // Move the right boundary to mid - 1.
                // Why?
                // Everything at mid and to the right is not needed for finding an earlier valid index.
                // We already know mid works, so now we try to improve the answer by looking left.
                right = mid - 1;
            }
            else
            {
                // Case 2:
                // If the current arrival is less than the target,
                // then this train is too early and cannot be the answer.
                //
                // Also, because the array is sorted in non-decreasing order,
                // every element to the LEFT of mid is <= currentArrival,
                // so those elements are also too early.
                //
                // Therefore, we can safely discard the entire left half including mid,
                // and continue searching only in the right half.
                left = mid + 1;
            }
        }

        // After the loop ends, 'answer' contains:
        // - the first index with arrivals[index] >= target, if one exists
        // - or -1 if no such index exists
        return answer;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// arrivals = [120, 180, 180, 240, 315], target = 181
// Expected output: 3
int[] arrivals1 = { 120, 180, 180, 240, 315 };
int target1 = 181;
int result1 = solution.FirstArrivalNotEarlierThan(arrivals1, target1);
Console.WriteLine(result1);

// Example 2:
// arrivals = [60, 90, 150, 150, 210], target = 150
// Expected output: 2
int[] arrivals2 = { 60, 90, 150, 150, 210 };
int target2 = 150;
int result2 = solution.FirstArrivalNotEarlierThan(arrivals2, target2);
Console.WriteLine(result2);

// Additional demo:
// Every train arrives before target, so expected output is -1
int[] arrivals3 = { 100, 200, 300 };
int target3 = 400;
int result3 = solution.FirstArrivalNotEarlierThan(arrivals3, target3);
Console.WriteLine(result3);