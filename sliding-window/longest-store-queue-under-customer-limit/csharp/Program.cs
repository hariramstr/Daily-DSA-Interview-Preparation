/*
Title: Longest Store Queue Under Customer Limit
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A supermarket records the number of customers joining a checkout line each minute.
You are given an integer array customers where customers[i] is the number of new
customers who joined during the i-th minute, and an integer limit representing the
maximum total number of customers the manager is willing to handle in one continuous
observation period.

Your task is to find the length of the longest contiguous block of minutes such that
the sum of customers in that block is less than or equal to limit.

In other words, among all subarrays of customers whose total sum does not exceed limit,
return the maximum possible subarray length.

This problem models a common real-world monitoring task where a team wants to identify
the longest time span during which demand stayed within a manageable threshold. Since
all customer counts are non-negative, an efficient sliding window solution can expand
and shrink a window while maintaining the current sum.

Constraints:
- 1 <= customers.length <= 100000
- 0 <= customers[i] <= 10000
- 0 <= limit <= 1000000000

Example 1:
Input: customers = [2, 1, 3, 2, 1], limit = 5
Output: 2
Explanation: Valid windows include [2,1], [3,2], and [2,1]. Any window of length 3
has sum greater than 5, so the answer is 2.

Example 2:
Input: customers = [1, 0, 1, 1, 0, 1], limit = 3
Output: 5
Explanation: The window [1,0,1,1,0] has sum 3 and length 5, which is the longest
valid contiguous block.

Return only the maximum length. If no minute can be included because every single
value is greater than limit, return 0.
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
        - No extra arrays, lists, or other data structures are needed.

        Beginner-friendly idea:
        We maintain a "window" [left..right] that represents a contiguous block of minutes.
        Because all values are non-negative, when the sum becomes too large, the only way
        to make it smaller is to move the left side forward and remove values from the sum.
        This property is exactly why sliding window works efficiently here.
    */
    public int LongestQueueWithinLimit(int[] customers, int limit)
    {
        // 'left' is the starting index of our current sliding window.
        // We will expand the window by moving 'right' forward in the loop below.
        int left = 0;

        // 'currentSum' stores the total number of customers inside the current window.
        // We use long to be extra safe, even though int would also fit under the given constraints.
        long currentSum = 0;

        // 'maxLength' stores the best (largest) valid window length we have found so far.
        int maxLength = 0;

        // Move 'right' from the beginning of the array to the end.
        // At each step, we try to include customers[right] in the current window.
        for (int right = 0; right < customers.Length; right++)
        {
            // Step 1: Expand the window to include the new rightmost element.
            // Why?
            // We want to examine all possible contiguous subarrays, and sliding window
            // does this by gradually growing the right side.
            currentSum += customers[right];

            // Step 2: If the window sum is too large, shrink the window from the left.
            // Why from the left?
            // The window must remain contiguous. Once we decide to keep 'right' fixed,
            // the only way to reduce the sum is to remove elements from the beginning.
            //
            // Why is this correct?
            // Because all customer counts are non-negative:
            // - Adding more elements to the right can only keep the sum the same or increase it.
            // - So if the current sum is already too large, we must move 'left' forward.
            while (currentSum > limit && left <= right)
            {
                // Remove the leftmost value from the running sum,
                // because that element is no longer part of the window.
                currentSum -= customers[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid:
            // currentSum <= limit
            //
            // So we can compute its length and compare it with the best answer so far.
            int currentLength = right - left + 1;

            // If this valid window is longer than any valid window we have seen before,
            // update the answer.
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
int[] customers1 = { 2, 1, 3, 2, 1 };
int limit1 = 5;
int result1 = solution.LongestQueueWithinLimit(customers1, limit1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] customers2 = { 1, 0, 1, 1, 0, 1 };
int limit2 = 3;
int result2 = solution.LongestQueueWithinLimit(customers2, limit2);
Console.WriteLine(result2); // Expected: 5

// Additional demo: no single minute can be included
int[] customers3 = { 6, 7, 8 };
int limit3 = 5;
int result3 = solution.LongestQueueWithinLimit(customers3, limit3);
Console.WriteLine(result3); // Expected: 0