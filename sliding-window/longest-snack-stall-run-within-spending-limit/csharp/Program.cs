/*
Title: Longest Snack Stall Run Within Spending Limit
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are visiting a street market with a row of snack stalls. The i-th stall has a snack that costs costs[i] dollars.
You want to buy snacks from a single contiguous block of stalls, because walking back and forth is inconvenient.
However, you have a fixed budget and cannot spend more than budget dollars in total.

Return the length of the longest contiguous segment of stalls such that the sum of costs in that segment
is less than or equal to budget.

If no single stall can be afforded, return 0.

This problem is a natural fit for a sliding window approach:
- Expand the right end of the window one stall at a time
- Keep track of the running total cost inside the current window
- If the total becomes greater than budget, move the left end forward until the window becomes valid again
- Track the maximum valid window length seen during the scan

Constraints:
- 1 <= costs.length <= 100000
- 1 <= costs[i] <= 10000
- 1 <= budget <= 1000000000
- costs contains only positive integers

Example 1:
Input: costs = [2, 1, 3, 2, 1], budget = 5
Output: 2
Explanation:
Valid segments with total cost at most 5 include [2,1], [3,2], and [2,1].
The longest valid length is 2.
The segment [1,3,2] costs 6, so it is not allowed.

Example 2:
Input: costs = [4, 2, 1, 1, 3], budget = 6
Output: 3
Explanation:
One longest valid segment is [2,1,1], which costs 4.
The segment [4,2,1] costs 7, so it exceeds the budget.
No valid segment of length 4 exists.
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
    - We only use a few extra variables: left pointer, running sum, and best length.
    - No extra array or complex data structure is needed.

    Beginner-friendly idea:
    We maintain a "window" [left..right] representing the current contiguous segment of stalls.
    Since all costs are positive:
    - Expanding the window increases or keeps the sum larger
    - Shrinking the window decreases the sum
    This makes sliding window the perfect technique here.
    */
    public int LongestSnackStallRun(int[] costs, int budget)
    {
        // "left" marks the starting index of the current window.
        // The current window will always be from index "left" to index "right".
        int left = 0;

        // "currentSum" stores the total cost of all stalls currently inside the window.
        // We use long for extra safety, even though int would also fit under the given constraints.
        long currentSum = 0;

        // "maxLength" stores the best (largest) valid window length found so far.
        int maxLength = 0;

        // Move "right" from the beginning of the array to the end.
        // At each step, we try to include costs[right] into our current segment.
        for (int right = 0; right < costs.Length; right++)
        {
            // Step 1: Expand the window by including the stall at index "right".
            // Why?
            // We want to consider every possible ending position of a contiguous segment.
            // By adding this stall, we are testing windows that end at "right".
            currentSum += costs[right];

            // Step 2: If the window is too expensive, shrink it from the left.
            // Why?
            // The problem requires the total cost to be <= budget.
            // Since all values are positive, the only way to reduce the sum is to move "left" forward.
            // We keep shrinking until the window becomes valid again.
            while (currentSum > budget && left <= right)
            {
                // Remove the cost at the current left edge from the running total,
                // because that stall is no longer part of the window after we move left forward.
                currentSum -= costs[left];

                // Move the left edge one step to the right.
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid:
            // currentSum <= budget
            // So we can compute its length and compare it with the best answer so far.
            int currentLength = right - left + 1;

            // Update the maximum length if this valid window is larger.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // If no stall was affordable, maxLength will remain 0.
        // Otherwise, it will contain the longest valid contiguous segment length.
        return maxLength;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] costs1 = { 2, 1, 3, 2, 1 };
int budget1 = 5;
int result1 = solution.LongestSnackStallRun(costs1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 2

// Example 2
int[] costs2 = { 4, 2, 1, 1, 3 };
int budget2 = 6;
int result2 = solution.LongestSnackStallRun(costs2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional demo: no single stall can be afforded
int[] costs3 = { 7, 8, 9 };
int budget3 = 5;
int result3 = solution.LongestSnackStallRun(costs3, budget3);
Console.WriteLine("Additional Example Result: " + result3); // Expected: 0