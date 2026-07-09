/*
Title: Longest Reading Streak Within Late Fee Budget
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are given an array lateFees where lateFees[i] is the late fee charged on day i for a borrowed library item.
A student wants to study over a consecutive block of days, but the total late fees during that block must not
exceed a given budget B.

Return the length of the longest contiguous subarray whose sum is less than or equal to B.

In other words, find the maximum number of consecutive days the student can keep the item while ensuring the
total accumulated late fees in that chosen window stay within budget.

This problem is designed to be solved efficiently using a sliding window technique. Since all late fees are
non-negative, once the current window exceeds the budget, moving the left boundary to the right is the correct
way to restore validity.

Constraints:
- 1 <= lateFees.length <= 100000
- 0 <= lateFees[i] <= 10000
- 0 <= B <= 1000000000
- lateFees contains only non-negative integers

Example 1:
Input: lateFees = [2, 1, 3, 2, 1], B = 5
Output: 2
Explanation: Valid windows include [2,1], [3,2], and [2,1]. Any window of length 3 has total fee greater than 5,
so the answer is 2.

Example 2:
Input: lateFees = [0, 1, 1, 0, 2, 1], B = 3
Output: 4
Explanation: One optimal window is [1,1,0,1] formed by days with fees [1,1,0,1], which sums to 3.
No longer contiguous window stays within the budget.
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
    - We only use a few variables: left pointer, running sum, and best length.
    - No extra array or data structure proportional to input size is needed.
    */
    public int LongestReadingStreakWithinBudget(int[] lateFees, int budget)
    {
        // The left boundary of our current sliding window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // This variable stores the sum of all values currently inside the window.
        // We use long instead of int to be extra safe when summing many values,
        // even though the given constraints would still fit in int.
        long currentSum = 0;

        // This keeps track of the maximum valid window length we have seen so far.
        int maxLength = 0;

        // We expand the window one step at a time by moving the right boundary.
        // For every position 'right', we include lateFees[right] in the current window.
        for (int right = 0; right < lateFees.Length; right++)
        {
            // Step 1: Add the new day at index 'right' into the current window.
            // Why?
            // We are exploring all possible contiguous windows that end at 'right'.
            currentSum += lateFees[right];

            // Step 2: If the window is invalid (sum > budget), shrink it from the left.
            // Why is shrinking from the left correct?
            // Because all late fees are non-negative.
            // That means adding more elements can never reduce the sum.
            // So once we exceed the budget, the only way to get back within budget
            // is to remove elements from the left side of the current window.
            while (currentSum > budget && left <= right)
            {
                // Remove the leftmost element from the running sum,
                // because that day is no longer part of the window.
                currentSum -= lateFees[left];

                // Move the left boundary one step to the right,
                // making the window smaller.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed valid:
            // currentSum <= budget.
            // So we can compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer
            // than any valid window we have seen before.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking every possible right boundary, maxLength stores
        // the length of the longest valid contiguous subarray.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] lateFees1 = { 2, 1, 3, 2, 1 };
int budget1 = 5;
int result1 = solution.LongestReadingStreakWithinBudget(lateFees1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 2

// Example 2
int[] lateFees2 = { 0, 1, 1, 0, 2, 1 };
int budget2 = 3;
int result2 = solution.LongestReadingStreakWithinBudget(lateFees2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional demo
int[] lateFees3 = { 1, 2, 1, 1, 1 };
int budget3 = 4;
int result3 = solution.LongestReadingStreakWithinBudget(lateFees3, budget3);
Console.WriteLine("Additional Demo Result: " + result3); // Expected: 3