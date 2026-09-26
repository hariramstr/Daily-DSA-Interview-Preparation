/*
Title: Longest Grocery Run Within Bag Capacity
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are given an array weights where weights[i] represents the weight of the i-th grocery item picked up in order while walking through a store. You also have an integer capacity representing the maximum total weight that can fit in your shopping bag at one time.

Your task is to find the length of the longest contiguous sequence of items you can pick such that the sum of their weights is less than or equal to capacity. In other words, choose a subarray with the largest possible length whose total weight does not exceed the bag limit.

This is a realistic sliding window problem because all item weights are non-negative, so if a window becomes too heavy, you can move its left boundary forward until it becomes valid again.

Return the maximum number of consecutive items that can fit in the bag.

Constraints:
- 1 <= weights.length <= 100000
- 0 <= weights[i] <= 10000
- 0 <= capacity <= 1000000000

Example 1:
Input: weights = [2, 1, 3, 2, 1], capacity = 5
Output: 2
Explanation: Valid contiguous runs include [2,1], [3,2], and [2,1]. Each has total weight 5 or less and length 2. No length-3 subarray fits within the capacity.

Example 2:
Input: weights = [1, 1, 1, 1, 2], capacity = 4
Output: 4
Explanation: The subarray [1,1,1,1] has total weight 4, so the answer is 4. The full array has total weight 6, which exceeds the capacity.

Approach Summary:
Because all weights are non-negative, we can use a sliding window:
- Expand the window to the right by adding the next item.
- If the total weight becomes too large, shrink from the left until the window is valid again.
- Track the maximum valid window length seen so far.

Correctness check on examples:
1) weights = [2,1,3,2,1], capacity = 5
   - Best valid window length found is 2.
2) weights = [1,1,1,1,2], capacity = 4
   - Best valid window length found is 4.
So the algorithm matches the required outputs.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once by the right pointer.
    - Each element is removed from the window at most once by the left pointer.
    - Therefore, the total work is linear in the number of items.

    Space Complexity: O(1)
    - We only use a few variables for pointers, the running sum, and the answer.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestGroceryRun(int[] weights, int capacity)
    {
        // This pointer marks the left boundary of our current sliding window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // This variable stores the total weight of all items currently inside the window.
        // We use long instead of int for extra safety when summing many values,
        // even though the given constraints would still fit in int.
        long currentSum = 0;

        // This keeps track of the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We move the right boundary one step at a time across the array.
        // At each step, we "include" weights[right] into the current window.
        for (int right = 0; right < weights.Length; right++)
        {
            // Step 1: Expand the window to include the new item at index 'right'.
            // Why necessary:
            // We want to consider every possible contiguous run ending at 'right'.
            currentSum += weights[right];

            // Step 2: If the window is too heavy, shrink it from the left.
            // Why this works:
            // All weights are non-negative. That means adding more items can never reduce the sum.
            // So once the sum exceeds capacity, the only way to make it valid again is to remove
            // items from the left side of the window.
            while (currentSum > capacity && left <= right)
            {
                // Remove the leftmost item from the running sum because it is leaving the window.
                currentSum -= weights[left];

                // Move the left boundary rightward to reflect the smaller window.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid
            // because the while-loop stopped only when currentSum <= capacity.
            // So we can compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            // Why necessary:
            // The problem asks for the maximum length among all valid contiguous subarrays.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking all possible right endpoints, maxLength is the answer.
        return maxLength;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] weights1 = { 2, 1, 3, 2, 1 };
int capacity1 = 5;
int result1 = solution.LongestGroceryRun(weights1, capacity1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] weights2 = { 1, 1, 1, 1, 2 };
int capacity2 = 4;
int result2 = solution.LongestGroceryRun(weights2, capacity2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo
int[] weights3 = { 0, 0, 0, 5, 0, 1 };
int capacity3 = 5;
int result3 = solution.LongestGroceryRun(weights3, capacity3);
Console.WriteLine($"Additional Demo Result: {result3}");