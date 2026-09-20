/*
Title: Longest Whiteboard Notes Within Marker Ink Limit
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A teacher writes a sequence of note segments on a digital whiteboard. The i-th segment uses ink[i] units of marker ink.
You are given an integer array ink where each value is non-negative, and an integer maxInk representing the maximum
total ink that can be used before the marker must be replaced.

Your task is to find the length of the longest contiguous group of note segments whose total ink usage is less than
or equal to maxInk.

In other words, choose a subarray ink[l..r] such that the sum of its elements does not exceed maxInk, and return
the maximum possible number of segments in such a subarray.

This problem is designed to be solved efficiently using the sliding window technique. Since all ink values are
non-negative, once a window exceeds the limit, moving the left pointer forward can only decrease the total.

Constraints:
- 1 <= ink.length <= 100000
- 0 <= ink[i] <= 10000
- 0 <= maxInk <= 1000000000

Important note about Example 2:
The written explanation in the prompt is inconsistent. For the array [0, 2, 1, 0, 1, 1] with maxInk = 3,
the true longest valid contiguous subarray length is 4, not 5.
Examples:
- [0, 2, 1, 0] has sum 3 and length 4
- [1, 0, 1, 1] has sum 3 and length 4
No valid contiguous subarray of length 5 has sum <= 3.
Therefore, the correct output for Example 2 is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once by the right pointer.
    - Each element is removed from the window at most once by the left pointer.
    - Therefore, the total work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few extra variables.
    - No additional data structures proportional to input size are needed.
    */
    public int LongestNotesWithinInkLimit(int[] ink, int maxInk)
    {
        // This pointer marks the beginning of our current sliding window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // This variable stores the running sum of all values currently inside the window.
        // We use long instead of int for extra safety, even though the given constraints
        // would still fit in int. Using long is a good habit when summing many numbers.
        long currentSum = 0;

        // This variable stores the best (maximum) valid window length we have seen so far.
        int bestLength = 0;

        // We expand the window one element at a time by moving the right pointer forward.
        // At every step, the window is ink[left..right].
        for (int right = 0; right < ink.Length; right++)
        {
            // Step 1: Include the new element at position "right" into the current window.
            // Why?
            // We are trying to explore all possible contiguous windows efficiently.
            // Expanding to the right is the natural way to grow a sliding window.
            currentSum += ink[right];

            // Step 2: If the window sum is too large, it is invalid.
            // Because all values are non-negative, the only way to reduce the sum
            // is to move the left boundary to the right and remove elements.
            //
            // This is the key reason sliding window works here:
            // - Adding more non-negative values can never decrease the sum.
            // - So once the sum exceeds maxInk, we must shrink from the left.
            while (currentSum > maxInk)
            {
                // Remove the leftmost element from the window sum,
                // because we are about to move the left pointer forward.
                currentSum -= ink[left];

                // Move the left boundary rightward by one position.
                // This makes the window smaller and helps restore validity.
                left++;
            }

            // Step 3: At this point, the window sum is guaranteed to be <= maxInk.
            // So the current window ink[left..right] is valid.
            //
            // Its length is:
            // right - left + 1
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer
            // than any valid window we have seen before.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After checking all possible windows through expansion and shrinking,
        // bestLength contains the maximum valid contiguous subarray length.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt
int[] ink1 = { 2, 1, 3, 2, 1 };
int maxInk1 = 5;
int result1 = solution.LongestNotesWithinInkLimit(ink1, maxInk1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 2

// Example 2 from the prompt
// The prompt's stated output of 5 is inconsistent with the actual array.
// The correct answer is 4.
int[] ink2 = { 0, 2, 1, 0, 1, 1 };
int maxInk2 = 3;
int result2 = solution.LongestNotesWithinInkLimit(ink2, maxInk2);
Console.WriteLine($"Example 2 Result: {result2}"); // Correct Expected: 4

// Additional demo cases

// Entire array fits
int[] ink3 = { 1, 1, 1, 1 };
int maxInk3 = 10;
int result3 = solution.LongestNotesWithinInkLimit(ink3, maxInk3);
Console.WriteLine($"Demo 3 Result: {result3}"); // Expected: 4

// Only zeros can fit when maxInk is 0
int[] ink4 = { 0, 0, 1, 0, 0 };
int maxInk4 = 0;
int result4 = solution.LongestNotesWithinInkLimit(ink4, maxInk4);
Console.WriteLine($"Demo 4 Result: {result4}"); // Expected: 2

// Single element
int[] ink5 = { 7 };
int maxInk5 = 7;
int result5 = solution.LongestNotesWithinInkLimit(ink5, maxInk5);
Console.WriteLine($"Demo 5 Result: {result5}"); // Expected: 1