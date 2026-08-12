/*
Problem Title: Longest Fruit Basket Refill Under Weight Limit

Problem Description:
A grocery store packs fruits onto a conveyor belt in a fixed order. The weight of each fruit is given in an integer array `weights`, where `weights[i]` is the weight of the `i`th fruit. A worker wants to refill a basket using one contiguous group of fruits from the belt. The basket can hold at most `maxWeight` total weight.

Your task is to return the length of the longest contiguous subarray whose sum is less than or equal to `maxWeight`.

Because the fruits must be taken in order and without skipping, this is a contiguous window problem. If multiple windows have the same maximum length, you only need to return the length, not the actual window.

You may assume all fruit weights are positive integers, which makes it possible to grow and shrink a sliding window efficiently.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= 10000
- 1 <= maxWeight <= 1000000000

Example 1:
Input: weights = [2, 1, 3, 2, 1], maxWeight = 5
Output: 2
Explanation: The longest valid contiguous groups include [2, 1], [3, 2], and [2, 1]. Any group of length 3 exceeds the basket limit.

Example 2:
Input: weights = [1, 1, 1, 1, 2], maxWeight = 4
Output: 4
Explanation: The subarray [1, 1, 1, 1] has total weight 4, so it fits exactly. No longer contiguous group stays within the limit.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the sliding window once by moving the right pointer.
    - Each element is removed from the sliding window at most once by moving the left pointer.
    - Because both pointers only move forward, the total work is linear.

    Space Complexity: O(1)
    - We only use a few variables: left pointer, running sum, and best length.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestFruitBasketRefill(int[] weights, int maxWeight)
    {
        // This pointer marks the beginning of our current sliding window.
        // The window always represents a contiguous subarray from index "left" to index "right".
        int left = 0;

        // This stores the total weight of all fruits currently inside the window.
        // We use long instead of int for extra safety, even though int would also be enough
        // under the given constraints. Using long avoids accidental overflow in similar problems.
        long currentSum = 0;

        // This keeps track of the longest valid window length we have seen so far.
        int maxLength = 0;

        // We expand the window one fruit at a time by moving the right pointer from left to right.
        // At every step, we include weights[right] into the current window.
        for (int right = 0; right < weights.Length; right++)
        {
            // Step 1: Add the new fruit at index "right" into the window.
            // Why? Because we are trying to grow the current contiguous group as much as possible.
            currentSum += weights[right];

            // Step 2: If the total weight is now too large, the window is invalid.
            // Since all weights are positive integers, the only way to make the sum smaller
            // is to remove fruits from the left side of the window.
            //
            // This positivity property is exactly why sliding window works efficiently here:
            // - Expanding to the right can only increase the sum.
            // - Shrinking from the left can only decrease the sum.
            //
            // We keep shrinking until the window becomes valid again.
            while (currentSum > maxWeight)
            {
                // Remove the fruit at the left boundary from the running sum,
                // because that fruit is no longer part of the window.
                currentSum -= weights[left];

                // Move the left boundary one step to the right.
                // This effectively shrinks the window from the left.
                left++;
            }

            // Step 3: At this point, the window from left..right is guaranteed valid,
            // meaning its total sum is <= maxWeight.
            //
            // So we can compute its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;

            // If this valid window is longer than any previous valid window,
            // update the answer.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking all possible right endpoints, maxLength contains
        // the length of the longest contiguous subarray whose sum is <= maxWeight.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] weights1 = { 2, 1, 3, 2, 1 };
int maxWeight1 = 5;
int result1 = solution.LongestFruitBasketRefill(weights1, maxWeight1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 2

// Example 2
int[] weights2 = { 1, 1, 1, 1, 2 };
int maxWeight2 = 4;
int result2 = solution.LongestFruitBasketRefill(weights2, maxWeight2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick checks
int[] weights3 = { 5 };
int maxWeight3 = 5;
int result3 = solution.LongestFruitBasketRefill(weights3, maxWeight3);
Console.WriteLine("Additional Check 1 Result: " + result3); // Expected: 1

int[] weights4 = { 4, 2, 1, 1 };
int maxWeight4 = 3;
int result4 = solution.LongestFruitBasketRefill(weights4, maxWeight4);
Console.WriteLine("Additional Check 2 Result: " + result4); // Expected: 2