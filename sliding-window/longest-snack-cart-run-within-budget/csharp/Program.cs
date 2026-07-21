/*
Title: Longest Snack Cart Run Within Budget

Problem Description:
A company campus has a row of snack carts, and each cart has a fixed price for buying one item.
You are given an integer array prices where prices[i] is the cost at the i-th cart, and an integer budget.
During a break, an employee wants to visit a contiguous sequence of carts and buy exactly one item from each
cart in that sequence.

Your task is to return the maximum number of consecutive carts the employee can include without the total
cost exceeding budget.

In other words, find the length of the longest contiguous subarray whose sum is less than or equal to budget.

This problem is designed to be solved efficiently using a sliding window. Since all prices are positive,
once the current window exceeds the budget, moving the left side forward is guaranteed to reduce the total.

Constraints:
- 1 <= prices.length <= 100000
- 1 <= prices[i] <= 10000
- 1 <= budget <= 1000000000

Example 1:
Input: prices = [2, 1, 3, 2, 1], budget = 6
Output: 3
Explanation: One valid longest segment is [1, 3, 2], whose total cost is 6.
No contiguous segment of length 4 or more stays within the budget.

Example 2:
Input: prices = [5, 2, 2, 1, 4], budget = 5
Output: 2
Explanation: The longest affordable contiguous segment has length 2, such as [2, 2] or [1, 4].
A segment of length 3 would exceed the budget.

Goal:
Return only the maximum length. If even a single cart is too expensive, return 0.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once by moving the right pointer.
    - Each element is removed from the window at most once by moving the left pointer.
    - Therefore, the total amount of work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few variables: left pointer, running sum, and best length.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestAffordableRun(int[] prices, int budget)
    {
        // The left boundary of our sliding window.
        // The window will always represent a contiguous segment from left to right.
        int left = 0;

        // This stores the total cost of the current window.
        // We use long instead of int to be extra safe when summing many values,
        // even though the given constraints would still fit in int.
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one cart at a time by moving the right boundary.
        for (int right = 0; right < prices.Length; right++)
        {
            // STEP 1: Include the new cart at index "right" into the current window.
            // Why?
            // We are trying all possible contiguous segments efficiently.
            // By extending the right side, we consider larger segments ending at "right".
            currentSum += prices[right];

            // STEP 2: If the current window is too expensive, shrink it from the left.
            // Why is this valid?
            // Because all prices are positive.
            // That means adding more carts can only increase the sum,
            // and removing carts from the left will decrease the sum.
            //
            // We keep shrinking until the window becomes affordable again.
            while (currentSum > budget && left <= right)
            {
                // Remove the leftmost cart from the running total,
                // because that cart is no longer part of the window.
                currentSum -= prices[left];

                // Move the left boundary one step to the right.
                // This makes the window smaller and helps reduce the total cost.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed to have
            // sum <= budget, so it is a valid candidate.
            //
            // We compute its length:
            // number of elements from left to right inclusive = right - left + 1
            int currentLength = right - left + 1;

            // STEP 4: Update the best answer if this valid window is longer
            // than any valid window we have seen before.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking all possible windows through the sliding process,
        // maxLength contains the length of the longest affordable contiguous segment.
        return maxLength;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] prices1 = { 2, 1, 3, 2, 1 };
int budget1 = 6;
int result1 = solution.LongestAffordableRun(prices1, budget1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] prices2 = { 5, 2, 2, 1, 4 };
int budget2 = 5;
int result2 = solution.LongestAffordableRun(prices2, budget2);
Console.WriteLine(result2); // Expected: 2

// Additional demo: no single cart is affordable
int[] prices3 = { 7, 8, 9 };
int budget3 = 5;
int result3 = solution.LongestAffordableRun(prices3, budget3);
Console.WriteLine(result3); // Expected: 0

// Additional demo: entire array is affordable
int[] prices4 = { 1, 1, 1, 1 };
int budget4 = 10;
int result4 = solution.LongestAffordableRun(prices4, budget4);
Console.WriteLine(result4); // Expected: 4