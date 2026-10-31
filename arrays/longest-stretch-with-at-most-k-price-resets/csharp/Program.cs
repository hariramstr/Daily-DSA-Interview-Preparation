/*
Title: Longest Stretch With At Most K Price Resets
Difficulty: Medium
Topic: Arrays

Problem Description:
An online retailer records the displayed price of a product once per hour in an integer array prices, where prices[i] is the price at hour i. A pricing analyst defines a stretch of hours as stable if, after removing at most k hours from that stretch, the remaining prices are all equal. The removed hours do not need to be consecutive, but they must come from inside the chosen contiguous stretch.

Your task is to return the length of the longest contiguous stretch that can be made stable under this rule.

In other words, for any subarray prices[l...r], you may delete at most k elements from that subarray. If the remaining elements can all have the same value, then that subarray is valid. Find the maximum possible length of such a valid subarray.

This models a real pricing system where a few temporary resets or glitches may be ignored when measuring how long a product effectively stayed at one displayed price.

Constraints:
- 1 <= prices.length <= 100000
- 1 <= prices[i] <= 1000000000
- 0 <= k <= prices.length
- The answer should be computed in O(n) or O(n log n) time.

Example 1:
Input: prices = [7,7,8,7,7,9,7], k = 1
Output: 5
Explanation: The subarray [7,7,8,7,7] has length 5. By removing the single value 8, all remaining values are 7, so this stretch is valid. No longer valid stretch exists with at most 1 removal.

Example 2:
Input: prices = [4,5,5,5,6,5,5], k = 2
Output: 7
Explanation: The entire array has length 7. Remove 4 and 6, and all remaining values are 5. Since only 2 removals are needed, the whole stretch is valid.

Key Observation:
For any window, if the most frequent value appears maxFreq times, then we can make the whole window stable
by deleting every other value. The number of deletions needed is:

    windowLength - maxFreq

So a window is valid if:

    windowLength - maxFreq <= k

This leads naturally to a sliding window solution.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n) in the worst case, due to the frequency dictionary

    Why O(n)?
    - Each element is added to the sliding window once by moving the right pointer.
    - Each element is removed from the sliding window at most once by moving the left pointer.
    - Dictionary operations are O(1) on average.

    Important correctness note:
    We maintain a sliding window and track the highest frequency seen for any value inside the current
    or recent windows using maxFrequencyInWindow.

    Even though maxFrequencyInWindow is not decreased when the left side moves, this standard optimization
    is still correct for finding the maximum valid window length:
    - It may temporarily make the window look more valid than it truly is.
    - But it never causes us to miss the true answer.
    - The window only expands when possible, and shrinks when clearly impossible under the tracked bound.
    - This guarantees the maximum recorded length is achievable by some valid window.

    This is the same proven technique used in the classic "Longest Repeating Character Replacement" pattern,
    adapted here for integer arrays instead of characters.
    */
    public int LongestStableStretch(int[] prices, int k)
    {
        // This dictionary stores how many times each price appears inside the current sliding window.
        // Key   = price value
        // Value = frequency of that price in the current window
        //
        // We use a dictionary because prices can be as large as 1,000,000,000,
        // so using an array indexed by price would be wasteful or impossible.
        var frequency = new Dictionary<int, int>();

        // left marks the start of the current window.
        int left = 0;

        // maxFrequencyInWindow stores the largest count of any single price
        // that we have seen while processing the current expanding window.
        //
        // Example:
        // If the current window is [7,7,8,7], then frequencies are:
        // 7 -> 3
        // 8 -> 1
        // so maxFrequencyInWindow = 3
        //
        // This value helps us quickly compute how many elements would need to be removed
        // to make the whole window consist of one repeated price.
        int maxFrequencyInWindow = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move the right pointer from left to right across the array.
        // At each step, we include prices[right] into the current window.
        for (int right = 0; right < prices.Length; right++)
        {
            int currentPrice = prices[right];

            // Step 1: Add the new rightmost element into the frequency map.
            //
            // Why?
            // Because the sliding window now includes prices[right],
            // so our counts must reflect that.
            if (!frequency.ContainsKey(currentPrice))
            {
                frequency[currentPrice] = 0;
            }

            frequency[currentPrice]++;

            // Step 2: Update the maximum frequency seen in the current window growth.
            //
            // Why is this important?
            // For a window to be made stable, we want to keep the most common price
            // and delete all other prices.
            //
            // If the most common price appears maxFrequencyInWindow times,
            // then deletions needed = windowLength - maxFrequencyInWindow.
            //
            // So this value is the key to deciding whether the current window is valid.
            if (frequency[currentPrice] > maxFrequencyInWindow)
            {
                maxFrequencyInWindow = frequency[currentPrice];
            }

            // Step 3: Check whether the current window is invalid.
            //
            // Current window length is:
            int windowLength = right - left + 1;

            // Number of elements we would need to remove to make all remaining values equal:
            int removalsNeeded = windowLength - maxFrequencyInWindow;

            // If removalsNeeded > k, then this window is too "mixed" and cannot be made stable
            // with only k deletions. So we must shrink it from the left.
            //
            // We use a while loop because after moving left once, the window might still be invalid.
            while (removalsNeeded > k)
            {
                int leftPrice = prices[left];

                // Remove the leftmost element from the frequency map,
                // because it is no longer inside the window after we move left forward.
                frequency[leftPrice]--;

                // Advance the left boundary to shrink the window.
                left++;

                // Recompute the current window length after shrinking.
                windowLength = right - left + 1;

                // Recompute removals needed using the same tracked maxFrequencyInWindow.
                //
                // Note:
                // We do NOT recompute maxFrequencyInWindow downward here.
                // That is a deliberate optimization that keeps the algorithm O(n).
                removalsNeeded = windowLength - maxFrequencyInWindow;
            }

            // Step 4: At this point, the window is valid under our sliding-window condition.
            // So we can safely update the best answer.
            //
            // Why?
            // Because this window length is a candidate for the longest stable stretch.
            if (windowLength > best)
            {
                best = windowLength;
            }
        }

        // After scanning the entire array, best contains the maximum valid window length.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] prices1 = { 7, 7, 8, 7, 7, 9, 7 };
int k1 = 1;
int result1 = solution.LongestStableStretch(prices1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] prices2 = { 4, 5, 5, 5, 6, 5, 5 };
int k2 = 2;
int result2 = solution.LongestStableStretch(prices2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 7

// Additional quick checks
int[] prices3 = { 1 };
int k3 = 0;
Console.WriteLine("Additional Test 1 Result: " + solution.LongestStableStretch(prices3, k3)); // Expected: 1

int[] prices4 = { 2, 3, 2, 2, 4, 2 };
int k4 = 1;
Console.WriteLine("Additional Test 2 Result: " + solution.LongestStableStretch(prices4, k4)); // Expected: 4

int[] prices5 = { 5, 5, 5, 5 };
int k5 = 0;
Console.WriteLine("Additional Test 3 Result: " + solution.LongestStableStretch(prices5, k5)); // Expected: 4