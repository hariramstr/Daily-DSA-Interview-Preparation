/*
Title: Longest Audio Queue Within Memory Budget
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A media player buffers a sequence of audio clips before playback. The i-th clip requires memory[i] megabytes to keep in RAM, and the player must preserve the original order of clips. Given an array memory and an integer budget, return the length of the longest contiguous block of clips that can be buffered at the same time without exceeding the total memory budget.

You may choose any contiguous subarray of memory, but the sum of its values must be less than or equal to budget. Your task is to compute the maximum possible number of clips in such a block.

This problem is intended to be solved efficiently for large inputs. A brute-force solution that checks every possible subarray will be too slow. Think about how to maintain a valid range while expanding and shrinking a window.

Constraints:
- 1 <= memory.length <= 200000
- 1 <= memory[i] <= 1000000000
- 1 <= budget <= 100000000000000
- The answer always fits in a 32-bit signed integer.

Example 1:
Input: memory = [4, 2, 1, 7, 3, 2], budget = 8
Output: 3
Explanation: The longest valid block is [4, 2, 1] with total memory 7. Other length-3 blocks like [2, 1, 7] or [7, 3, 2] exceed the budget.

Example 2:
Input: memory = [5, 1, 1, 1, 5], budget = 7
Output: 3
Explanation: One optimal block is [1, 1, 5] with total memory 7. The full array uses 13, and no valid block of length 4 exists.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once when the right pointer moves forward.
    - Each element is removed from the window at most once when the left pointer moves forward.
    - Because both pointers only move from left to right, the total work is linear.

    Space Complexity: O(1)
    - We only use a few variables: left pointer, running sum, and best answer.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestAudioQueueWithinBudget(int[] memory, long budget)
    {
        // This pointer marks the beginning of our current sliding window.
        // The window will always represent a contiguous block of clips.
        int left = 0;

        // We store the sum of the current window in a long.
        // This is very important because:
        // - memory[i] can be as large as 1,000,000,000
        // - the array can be very large
        // - the total can exceed the range of int
        long currentSum = 0;

        // This variable stores the best (largest) valid window length we have seen so far.
        int maxLength = 0;

        // We expand the window one element at a time by moving the right pointer.
        // At each step, we include memory[right] in the current window.
        for (int right = 0; right < memory.Length; right++)
        {
            // Step 1: Add the new rightmost clip into the running sum.
            // Why?
            // Because the window now includes this clip, so its memory usage must be counted.
            currentSum += memory[right];

            // Step 2: If the window is invalid (sum > budget), shrink it from the left.
            // Why do we shrink from the left?
            // Because the problem requires a contiguous subarray, and the sliding window
            // technique maintains contiguity by only moving the boundaries inward/outward.
            //
            // Why use a while loop instead of an if statement?
            // Because adding one large element might make the sum exceed the budget by a lot,
            // so we may need to remove multiple elements from the left before the window
            // becomes valid again.
            while (currentSum > budget)
            {
                // Remove the leftmost clip from the running sum because it is no longer
                // part of the window after we move the left boundary forward.
                currentSum -= memory[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // At this point, the window [left..right] is guaranteed to be valid:
            // currentSum <= budget

            // Step 3: Compute the current valid window length.
            // Since both left and right are inclusive indices, the length is:
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is larger than
            // any valid window we have seen before.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After processing all possible right endpoints, maxLength contains the answer.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] memory1 = { 4, 2, 1, 7, 3, 2 };
long budget1 = 8;
int result1 = solution.LongestAudioQueueWithinBudget(memory1, budget1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] memory2 = { 5, 1, 1, 1, 5 };
long budget2 = 7;
int result2 = solution.LongestAudioQueueWithinBudget(memory2, budget2);
Console.WriteLine(result2); // Expected: 3

// Additional quick demo
int[] memory3 = { 2, 2, 2, 2 };
long budget3 = 4;
int result3 = solution.LongestAudioQueueWithinBudget(memory3, budget3);
Console.WriteLine(result3); // Expected: 2