/*
Title: Maximum Sum of a Distinct-Value Window
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array nums and an integer k. A window is any contiguous subarray of length exactly k.
A window is called valid if all k elements inside it are pairwise distinct, meaning no value appears more than once in that window.

Your task is to return the maximum possible sum among all valid windows of length k.
If there is no valid window of length k, return 0.

This problem models a situation where you want to choose exactly k consecutive records, but duplicate values
in the chosen range are not allowed because they would represent repeated IDs, repeated product codes, or duplicate events.
The challenge is to evaluate every length-k range efficiently without recomputing its sum and uniqueness from scratch.

A correct solution should work efficiently for large inputs. In particular, iterating over every window and checking duplicates
naively may be too slow. Think about how to maintain both the current window sum and the frequency of values as the window slides by one position.

Constraints:
- 1 <= nums.length <= 200000
- 1 <= nums[i] <= 1000000000
- 1 <= k <= nums.length

Example 1:
Input: nums = [5,2,3,5,4,6], k = 3
Output: 15
Explanation: The length-3 windows are [5,2,3], [2,3,5], [3,5,4], and [5,4,6].
All of them contain distinct values. Their sums are 10, 10, 12, and 15.
The maximum valid sum is 15.

Example 2:
Input: nums = [4,4,2,1,2], k = 3
Output: 7
Explanation: The windows are [4,4,2], [4,2,1], and [2,1,2].
The first and third windows are invalid because they contain duplicates.
The only valid window is [4,2,1], whose sum is 7.

Return the maximum sum of any valid contiguous subarray of length exactly k.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the sliding window once and removed once.
    - Dictionary operations are O(1) on average.
    - Therefore the full scan is linear in the size of the array.

    Space Complexity: O(k)
    - The frequency dictionary stores at most the elements currently inside the window.
    - In the worst case, all k elements are distinct, so the dictionary holds k entries.
    */
    public long MaximumSubarraySum(int[] nums, int k)
    {
        // This dictionary will store:
        // key   = a number currently inside the sliding window
        // value = how many times that number appears in the current window
        //
        // Why do we need this?
        // Because a window is valid only when every value appears exactly once.
        // If any value appears more than once, the window contains duplicates and is invalid.
        //
        // Using a dictionary lets us update counts efficiently as the window moves.
        var frequency = new Dictionary<int, int>();

        // currentSum stores the sum of the numbers currently inside the window.
        //
        // Why long instead of int?
        // nums[i] can be as large as 1,000,000,000 and k can be large too,
        // so the sum can exceed the range of int.
        long currentSum = 0;

        // bestSum stores the maximum sum found among all valid windows.
        // If no valid window exists, it will remain 0, which matches the problem requirement.
        long bestSum = 0;

        // We will use a classic sliding window:
        // - right expands the window by adding a new element
        // - once the window becomes larger than k, we remove the leftmost element
        //
        // After each step, if the window size is exactly k and all values are distinct,
        // we consider its sum as a candidate answer.
        for (int right = 0; right < nums.Length; right++)
        {
            int incomingValue = nums[right];

            // Step 1: Add the new rightmost element into the window sum.
            //
            // This keeps currentSum equal to the sum of the current window contents.
            currentSum += incomingValue;

            // Step 2: Update the frequency of the incoming value.
            //
            // If the value is already present, increase its count.
            // Otherwise, start its count at 1.
            if (frequency.ContainsKey(incomingValue))
            {
                frequency[incomingValue]++;
            }
            else
            {
                frequency[incomingValue] = 1;
            }

            // Step 3: If the window size is now larger than k, remove the leftmost element.
            //
            // Current window boundaries before removal would be:
            // left = right - k
            // because when size becomes k + 1, the extra element is exactly at index right - k.
            //
            // This is necessary because the problem only allows windows of length exactly k.
            if (right >= k)
            {
                int outgoingValue = nums[right - k];

                // Remove the outgoing value from the running sum,
                // because it is no longer part of the window.
                currentSum -= outgoingValue;

                // Decrease its frequency count in the dictionary.
                frequency[outgoingValue]--;

                // If its count becomes zero, remove the key completely.
                //
                // Why remove it?
                // Because the dictionary should represent only values currently in the window.
                // This also makes frequency.Count equal to the number of distinct values in the window,
                // which becomes very useful for checking validity.
                if (frequency[outgoingValue] == 0)
                {
                    frequency.Remove(outgoingValue);
                }
            }

            // Step 4: Check whether we currently have a full window of size exactly k.
            //
            // The first full window ends when right == k - 1.
            if (right >= k - 1)
            {
                // A window of size k is valid if all k elements are distinct.
                //
                // Since frequency.Count tells us how many distinct values are in the current window:
                // - if frequency.Count == k, then every element is unique
                // - if frequency.Count < k, then at least one duplicate exists
                //
                // This works because the window length is exactly k at this point.
                if (frequency.Count == k)
                {
                    // The current window is valid, so update the best answer if needed.
                    if (currentSum > bestSum)
                    {
                        bestSum = currentSum;
                    }
                }
            }
        }

        // If no valid window was ever found, bestSum is still 0.
        return bestSum;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// Windows of length 3:
// [5,2,3] -> distinct, sum = 10
// [2,3,5] -> distinct, sum = 10
// [3,5,4] -> distinct, sum = 12
// [5,4,6] -> distinct, sum = 15
// Maximum = 15
int[] nums1 = { 5, 2, 3, 5, 4, 6 };
int k1 = 3;
long result1 = solution.MaximumSubarraySum(nums1, k1);
Console.WriteLine(result1);

// Example 2:
// Windows of length 3:
// [4,4,2] -> invalid because 4 repeats
// [4,2,1] -> valid, sum = 7
// [2,1,2] -> invalid because 2 repeats
// Maximum valid sum = 7
int[] nums2 = { 4, 4, 2, 1, 2 };
int k2 = 3;
long result2 = solution.MaximumSubarraySum(nums2, k2);
Console.WriteLine(result2);

// Additional demo:
// No valid window of length 2 because every window has duplicates.
int[] nums3 = { 8, 8, 8 };
int k3 = 2;
long result3 = solution.MaximumSubarraySum(nums3, k3);
Console.WriteLine(result3);