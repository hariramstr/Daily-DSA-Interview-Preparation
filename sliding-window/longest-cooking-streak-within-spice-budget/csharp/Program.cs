/*
Title: Longest Cooking Streak Within Spice Budget

Problem Description:
You are given an array heat of length n, where heat[i] represents the spice level added by the i-th dish cooked in order during a live kitchen session.
A chef wants to select one contiguous streak of dishes to present as a tasting sequence.
The total spice used in that streak must not exceed a given integer budget.

Your task is to return the maximum number of consecutive dishes the chef can include.

Formally:
Find the length of the longest contiguous subarray whose sum is less than or equal to budget.

Key observation:
Because all values in heat are non-negative, a sliding window works efficiently:
- Expanding the window to the right can only increase the sum or keep it the same.
- If the sum becomes too large, shrinking from the left reduces the sum.
This monotonic behavior is exactly why the sliding window technique is correct here.

Example 1:
Input: heat = [2, 1, 3, 2, 1, 1], budget = 5
Output: 3

Reasoning:
- Window [2,1] has sum 3, length 2
- Window [2,1,3] has sum 6, too large, so shrink
- Best valid windows include [2,1,1] at the end (values [2,1,1] from positions 3..5 in 0-based indexing: [2,1,1]), sum 4, length 3
- No valid window of length 4 exists
So the answer is 3.

Example 2:
Input: heat = [0, 4, 0, 2, 1, 0, 1], budget = 3
Output: 4

Reasoning:
- A candidate like [2,1,0,1] sums to 4, so it is invalid
- The valid window [0,2,1,0] sums to 3 and has length 4
- No longer valid window exists
So the answer is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once when the right pointer moves.
    - Each element is removed from the window at most once when the left pointer moves.
    - Therefore, the total amount of work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few variables: pointers, current sum, and best length.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestCookingStreak(int[] heat, long budget)
    {
        // The left boundary of our current sliding window.
        // The window always represents a contiguous subarray from left to right.
        int left = 0;

        // We store the sum of the current window.
        // This must be a long because:
        // - n can be as large as 200000
        // - each heat[i] can be as large as 100000
        // - the total can exceed the range of int
        long currentSum = 0;

        // This will store the maximum valid window length found so far.
        int bestLength = 0;

        // We expand the window one element at a time by moving the right pointer.
        for (int right = 0; right < heat.Length; right++)
        {
            // STEP 1: Include the new element at index "right" into the current window.
            // Why?
            // We are trying to explore all possible contiguous windows efficiently.
            // By extending the right side, we consider larger windows ending at "right".
            currentSum += heat[right];

            // STEP 2: If the current window sum exceeds the budget,
            // shrink the window from the left until it becomes valid again.
            //
            // Why is this correct?
            // Because all values are non-negative:
            // - Adding more elements on the right never decreases the sum.
            // - Removing elements from the left never increases the sum.
            //
            // So once the sum is too large, the only way to make it valid again
            // is to move the left pointer to the right.
            while (currentSum > budget)
            {
                // Remove the leftmost element from the window sum,
                // because that element is no longer part of the window.
                currentSum -= heat[left];

                // Move the left boundary rightward by one position.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed valid,
            // meaning its sum is <= budget.
            //
            // So we compute its length.
            int currentLength = right - left + 1;

            // STEP 4: Update the best answer if this valid window is longer
            // than any valid window we have seen before.
            //
            // Why do we do this here?
            // Because after the while-loop, the current window is the longest valid
            // window that ends at index "right" and starts no earlier than necessary.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After processing every possible right endpoint,
        // bestLength contains the maximum valid contiguous subarray length.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] heat1 = { 2, 1, 3, 2, 1, 1 };
long budget1 = 5;
int result1 = solution.LongestCookingStreak(heat1, budget1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] heat2 = { 0, 4, 0, 2, 1, 0, 1 };
long budget2 = 3;
int result2 = solution.LongestCookingStreak(heat2, budget2);
Console.WriteLine(result2); // Expected: 4

// Additional quick sanity checks
int[] heat3 = { 1, 2, 3 };
long budget3 = 0;
int result3 = solution.LongestCookingStreak(heat3, budget3);
Console.WriteLine(result3); // Expected: 0

int[] heat4 = { 0, 0, 0, 0 };
long budget4 = 0;
int result4 = solution.LongestCookingStreak(heat4, budget4);
Console.WriteLine(result4); // Expected: 4