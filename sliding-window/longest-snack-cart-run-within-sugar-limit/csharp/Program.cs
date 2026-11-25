/*
Title: Longest Snack Cart Run Within Sugar Limit
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A company cafeteria records the sugar content of each snack item placed on a moving cart during the day.
You are given an integer array sugars where sugars[i] is the sugar amount of the i-th snack in grams,
and an integer limit representing the maximum total sugar a nutrition program allows in one consecutive selection.

Your task is to find the length of the longest contiguous segment of the cart such that the sum of sugar
values in that segment is less than or equal to limit. In other words, choose a consecutive block of snacks
with total sugar at most limit, and return the maximum number of snacks that can be included.

This problem models real-time monitoring where the cart is scanned from left to right and all sugar values
are non-negative. Because of that property, an efficient sliding window solution is expected.

Return 0 if no snack can be included without exceeding the limit.

Constraints:
- 1 <= sugars.length <= 100000
- 0 <= sugars[i] <= 100000
- 0 <= limit <= 1000000000
- All values in sugars are non-negative integers.

Important correctness note about the examples:
- Example 1 is correct: sugars = [2, 1, 3, 2, 1, 1], limit = 5 => answer = 3
  One valid longest segment is [2, 1, 1] (indices 3..5), sum = 4, length = 3.
- Example 2's stated output in the prompt is inconsistent:
  sugars = [6, 2, 2, 1], limit = 4
  The prompt first says output = 3, but then correctly explains the best segment length is 2.
  The correct answer is 2, because:
    [6] = 6 > 4
    [2] = 2
    [2,2] = 4
    [2,2,1] = 5 > 4
    [2,1] = 3
  So the maximum valid length is 2.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element enters the sliding window once when the right pointer moves forward.
    - Each element leaves the sliding window at most once when the left pointer moves forward.
    - Therefore, the total work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few variables for pointers, the running sum, and the best answer.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestSnackCartRunWithinSugarLimit(int[] sugars, int limit)
    {
        // This pointer marks the beginning of the current window.
        // The "window" is the current contiguous segment we are considering.
        int left = 0;

        // This variable stores the sum of all values currently inside the window.
        // We use long instead of int for extra safety, even though the given constraints
        // would still fit in int. Using long is a good habit when repeatedly adding values.
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one element at a time by moving the right pointer.
        // At every step, sugars[right] is added into the current window.
        for (int right = 0; right < sugars.Length; right++)
        {
            // Step 1: Include the new element at position "right" into the window.
            // Why this is necessary:
            // We are trying every possible window ending at "right".
            // By adding sugars[right], we extend the current contiguous segment.
            currentSum += sugars[right];

            // Step 2: If the window is invalid (sum > limit), shrink it from the left.
            // Why this works:
            // All sugar values are non-negative.
            // That means adding more elements can only keep the sum the same or increase it.
            // So if the sum is too large, the only way to make it valid again is to remove
            // elements from the left side of the window.
            while (currentSum > limit && left <= right)
            {
                // Remove the leftmost element from the current window.
                currentSum -= sugars[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid
            // (or empty in edge situations, though here it remains a valid range after shrinking).
            // So we can compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any
            // valid window we have seen before.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // If no valid snack can be included, maxLength will remain 0.
        // Otherwise, it contains the length of the longest valid contiguous segment.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] sugars1 = { 2, 1, 3, 2, 1, 1 };
int limit1 = 5;
int result1 = solution.LongestSnackCartRunWithinSugarLimit(sugars1, limit1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
// The prompt contains a contradiction, but the correct answer is 2.
int[] sugars2 = { 6, 2, 2, 1 };
int limit2 = 4;
int result2 = solution.LongestSnackCartRunWithinSugarLimit(sugars2, limit2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional edge case: limit = 0 with zeros present
int[] sugars3 = { 0, 0, 1, 0, 0 };
int limit3 = 0;
int result3 = solution.LongestSnackCartRunWithinSugarLimit(sugars3, limit3);
Console.WriteLine($"Edge Case 1 Result: {result3}"); // Expected: 2

// Additional edge case: all items exceed the limit individually
int[] sugars4 = { 5, 6, 7 };
int limit4 = 4;
int result4 = solution.LongestSnackCartRunWithinSugarLimit(sugars4, limit4);
Console.WriteLine($"Edge Case 2 Result: {result4}"); // Expected: 0

// Additional edge case: all zeros
int[] sugars5 = { 0, 0, 0, 0 };
int limit5 = 0;
int result5 = solution.LongestSnackCartRunWithinSugarLimit(sugars5, limit5);
Console.WriteLine($"Edge Case 3 Result: {result5}"); // Expected: 4