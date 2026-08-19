/*
Title: Longest Coffee Order Run Within Sugar Limit
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A coffee shop records the sugar added to each drink in the order it was prepared.
You are given an integer array sugars where sugars[i] is the number of sugar packets
used in the i-th order, and an integer limit representing the maximum total sugar allowed.

Find the length of the longest contiguous sequence of orders whose total sugar does not exceed limit.

In other words, you must choose a subarray sugars[left...right] such that the sum of its values
is less than or equal to limit, and the number of elements in that subarray is as large as possible.

This problem is a classic sliding window pattern:
- We scan from left to right using a "right" pointer.
- We keep track of the current window sum.
- Because all values are non-negative, if the sum becomes too large, moving the "left" pointer
  forward is the correct way to reduce the sum until the window becomes valid again.
- At each step, once the window is valid, we update the best length found so far.

Example 1:
Input: sugars = [1, 2, 1, 1, 3], limit = 4
Output: 3
Explanation:
- [1, 2, 1] has sum 4 and length 3
- [2, 1, 1] has sum 4 and length 3
So the answer is 3.

Example 2:
Input: sugars = [4, 1, 1, 1, 2], limit = 3
Output: 3
Explanation:
- The first element 4 is already greater than the limit, so it cannot be in a valid window.
- [1, 1, 1] has sum 3 and length 3
So the answer is 3.
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
    - We only use a few extra variables: left, currentSum, and maxLength.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestCoffeeOrderRunWithinSugarLimit(int[] sugars, int limit)
    {
        // "left" marks the beginning of our current sliding window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // "currentSum" stores the total sugar inside the current window.
        // We use long to be extra safe, even though int would still fit under the given constraints.
        long currentSum = 0;

        // "maxLength" stores the best (largest) valid window length found so far.
        int maxLength = 0;

        // Move the "right" pointer from the start of the array to the end.
        // At each step, we expand the window by including sugars[right].
        for (int right = 0; right < sugars.Length; right++)
        {
            // Step 1: Expand the window to include the current order at index "right".
            // Why?
            // We want to consider every possible ending position of a valid subarray.
            // By adding sugars[right], we are testing windows that end at "right".
            currentSum += sugars[right];

            // Step 2: If the window is invalid (sum > limit), shrink it from the left.
            // Why is this correct?
            // Because all sugar values are non-negative.
            // That means adding more elements can never reduce the sum.
            // So once the sum is too large, the only way to make it valid again
            // is to remove elements from the left side.
            while (currentSum > limit && left <= right)
            {
                // Remove the leftmost element from the current window.
                currentSum -= sugars[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // currentSum <= limit
            //
            // So we can compute its length and compare it with the best answer so far.
            int currentLength = right - left + 1;

            // If this valid window is longer than any previously seen valid window,
            // update the answer.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength contains the length
        // of the longest valid contiguous sequence.
        return maxLength;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1:
// sugars = [1, 2, 1, 1, 3], limit = 4
// Valid longest windows include [1, 2, 1] and [2, 1, 1], both length 3.
int[] sugars1 = { 1, 2, 1, 1, 3 };
int limit1 = 4;
int result1 = solution.LongestCoffeeOrderRunWithinSugarLimit(sugars1, limit1);
Console.WriteLine(result1); // Expected: 3

// Example 2:
// sugars = [4, 1, 1, 1, 2], limit = 3
// The first element 4 is too large by itself.
// The longest valid window is [1, 1, 1], length 3.
int[] sugars2 = { 4, 1, 1, 1, 2 };
int limit2 = 3;
int result2 = solution.LongestCoffeeOrderRunWithinSugarLimit(sugars2, limit2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity check:
// sugars = [0, 0, 0], limit = 0
// Entire array is valid, so answer should be 3.
int[] sugars3 = { 0, 0, 0 };
int limit3 = 0;
int result3 = solution.LongestCoffeeOrderRunWithinSugarLimit(sugars3, limit3);
Console.WriteLine(result3); // Expected: 3