/*
Title: Longest Toll-Free Highway Stretch

Problem Description:
You are given an array costs where costs[i] is the toll fee charged at the i-th highway checkpoint
on a road trip. A traveler wants to drive through one contiguous stretch of checkpoints while
spending at most budget total toll money.

Return the length of the longest contiguous subarray of costs whose sum is less than or equal to budget.

This is a classic sliding window problem:
- All values are non-negative
- That means when we expand the window to the right, the sum can only stay the same or increase
- If the sum becomes too large, we can safely move the left side forward until the window becomes valid again

Constraints:
- 1 <= costs.length <= 100000
- 0 <= costs[i] <= 10000
- 0 <= budget <= 1000000000
- Return the number of checkpoints in the longest valid contiguous stretch

Example 1:
costs = [4, 2, 1, 3, 2], budget = 6
Output: 3
Explanation: The longest valid stretch is [2, 1, 3] with total cost 6.

Example 2:
costs = [1, 1, 1, 1, 1], budget = 3
Output: 3
Explanation: Any 3 consecutive checkpoints cost 3, but any 4 consecutive checkpoints cost 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the running sum once when the right pointer moves forward.
    - Each element is removed from the running sum at most once when the left pointer moves forward.
    - Therefore, both pointers together move at most 2n times, which is linear.

    Space Complexity: O(1)
    - We only use a few extra variables: left, maxLength, and currentSum.
    - No extra arrays or data structures are needed.
    */
    public int LongestTollFreeStretch(int[] costs, int budget)
    {
        // The left pointer marks the beginning of the current window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // This variable stores the best answer found so far:
        // the maximum length of any valid window whose sum is <= budget.
        int maxLength = 0;

        // We use long for safety.
        // Even though the given constraints fit inside int, using long avoids accidental overflow
        // if constraints are changed or if many values are summed together.
        long currentSum = 0;

        // Move the right pointer from the start of the array to the end.
        // At every step, we try to include costs[right] in the current window.
        for (int right = 0; right < costs.Length; right++)
        {
            // STEP 1: Expand the window to include the new rightmost element.
            // Why?
            // We are exploring all possible contiguous windows ending at index "right".
            // Adding this value updates the total cost of the current window.
            currentSum += costs[right];

            // STEP 2: If the window is too expensive, shrink it from the left.
            // Why?
            // The problem requires the sum to be <= budget.
            // Since all costs are non-negative, once the sum exceeds budget,
            // the only way to make it valid again is to remove elements from the left side.
            //
            // Important sliding window property:
            // Because values are non-negative, removing elements from the left can only decrease
            // or keep the sum the same. This guarantees correctness and efficiency.
            while (currentSum > budget && left <= right)
            {
                // Remove the leftmost element from the current window.
                currentSum -= costs[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is valid.
            // That means its sum is <= budget.
            //
            // Now compute its length.
            int currentLength = right - left + 1;

            // STEP 4: Update the best answer if this valid window is longer than any previous one.
            // Why?
            // The problem asks for the longest valid contiguous subarray.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking every possible right boundary, maxLength contains the answer.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] costs1 = { 4, 2, 1, 3, 2 };
int budget1 = 6;
int result1 = solution.LongestTollFreeStretch(costs1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
int[] costs2 = { 1, 1, 1, 1, 1 };
int budget2 = 3;
int result2 = solution.LongestTollFreeStretch(costs2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional quick checks
int[] costs3 = { 0, 0, 0, 0 };
int budget3 = 0;
int result3 = solution.LongestTollFreeStretch(costs3, budget3);
Console.WriteLine("Additional Test 1 Result: " + result3); // Expected: 4

int[] costs4 = { 10, 20, 30 };
int budget4 = 5;
int result4 = solution.LongestTollFreeStretch(costs4, budget4);
Console.WriteLine("Additional Test 2 Result: " + result4); // Expected: 0

int[] costs5 = { 5 };
int budget5 = 5;
int result5 = solution.LongestTollFreeStretch(costs5, budget5);
Console.WriteLine("Additional Test 3 Result: " + result5); // Expected: 1