/*
Title: Longest Even-Parity Access Window
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
A security system records a sequence of access events. Each event is labeled with an integer from 0 to 19 representing one of 20 badge categories. You are given an array events where events[i] is the category of the i-th event. A contiguous window of events is called balanced if, for every badge category, the number of times it appears inside that window is even.

Your task is to return the length of the longest balanced contiguous window.

Because there are only 20 possible categories, an efficient solution should take advantage of bit manipulation. One useful observation is that while scanning the array from left to right, you can represent the parity (even or odd count) of each category seen so far as a bitmask of length 20. If the same parity mask appears at two different positions, then the subarray between those positions has even frequency for every category.

Return 0 if no non-empty balanced window exists.

Constraints:
- 1 <= events.length <= 200000
- 0 <= events[i] < 20
- Expected time complexity: O(n)
- Expected extra space: O(min(n, 2^20))

Example 1:
Input: events = [3, 5, 3, 5, 7, 7]
Output: 6
Explanation: In the full array, categories 3, 5, and 7 each appear exactly twice, so the entire window is balanced.

Example 2:
Input: events = [1, 2, 1, 4, 2, 4, 4]
Output: 6
Explanation: The subarray [1, 2, 1, 4, 2, 4] is balanced because categories 1, 2, and 4 each appear twice. The full array is not balanced because category 4 appears three times.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(min(n, 2^20))

    Idea:
    - We scan the array from left to right.
    - We maintain a 20-bit integer called "mask".
    - Bit k in the mask tells us whether category k has appeared an odd number of times
      in the prefix processed so far:
        0 -> even count so far
        1 -> odd count so far

    Why this works:
    - Suppose the same mask appears at two different prefix positions i and j.
    - That means the parity state of every category is identical at both positions.
    - Therefore, between i+1 and j, every category must have changed parity an even number of times.
    - So the subarray between those two positions is balanced.

    Key data structure:
    - A dictionary from mask -> earliest index where this mask was first seen.
    - We store only the earliest index because that gives the longest possible subarray
      when we see the same mask again later.
    */
    public int LongestBalancedWindow(int[] events)
    {
        // This dictionary remembers the FIRST index where each parity mask appeared.
        // Why first index?
        // Because if the same mask appears again later, using the earliest occurrence
        // gives the maximum possible length for the balanced subarray ending here.
        var firstSeen = new Dictionary<int, int>();

        // Before processing any elements, the prefix is empty.
        // In the empty prefix, every category count is 0, which is even.
        // So the parity mask is 0.
        //
        // We treat this empty prefix as being at index -1.
        // This is extremely important because if a balanced subarray starts at index 0,
        // we can compute its length correctly as currentIndex - (-1) = currentIndex + 1.
        firstSeen[0] = -1;

        // Current parity mask while scanning the array.
        int mask = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Process each event one by one.
        for (int i = 0; i < events.Length; i++)
        {
            // Current category value is guaranteed to be in [0, 19].
            int category = events[i];

            // We flip the bit corresponding to this category.
            //
            // Why flip?
            // Every time we see the same category, its count changes parity:
            // even -> odd, or odd -> even.
            //
            // XOR with (1 << category) toggles exactly that one bit.
            mask ^= (1 << category);

            // If we have seen this exact mask before, then the subarray between
            // the previous occurrence + 1 and the current index i is balanced.
            if (firstSeen.TryGetValue(mask, out int earliestIndex))
            {
                // Length of the balanced subarray.
                int length = i - earliestIndex;

                // Update the best answer if this one is longer.
                if (length > maxLength)
                {
                    maxLength = length;
                }
            }
            else
            {
                // If this mask has never been seen before, store the current index
                // as its earliest occurrence.
                //
                // We do NOT overwrite existing entries because earliest occurrence
                // is always the most useful for maximizing future subarray lengths.
                firstSeen[mask] = i;
            }
        }

        // If no non-empty balanced subarray exists, maxLength remains 0,
        // which matches the problem requirement.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// events = [3, 5, 3, 5, 7, 7]
// Counts in the full array:
// 3 -> 2 times
// 5 -> 2 times
// 7 -> 2 times
// All are even, so answer should be 6.
int[] events1 = { 3, 5, 3, 5, 7, 7 };
int result1 = solution.LongestBalancedWindow(events1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// events = [1, 2, 1, 4, 2, 4, 4]
// The subarray [1, 2, 1, 4, 2, 4] has:
// 1 -> 2 times
// 2 -> 2 times
// 4 -> 2 times
// So answer should be 6.
int[] events2 = { 1, 2, 1, 4, 2, 4, 4 };
int result2 = solution.LongestBalancedWindow(events2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick checks

// No non-empty balanced subarray:
// [0] has category 0 appearing once, which is odd.
// So answer should be 0.
int[] events3 = { 0 };
int result3 = solution.LongestBalancedWindow(events3);
Console.WriteLine($"Additional Check 1 Result: {result3}");

// Entire array balanced:
// [2, 2, 3, 3, 2, 2] -> category 2 appears 4 times, category 3 appears 2 times.
int[] events4 = { 2, 2, 3, 3, 2, 2 };
int result4 = solution.LongestBalancedWindow(events4);
Console.WriteLine($"Additional Check 2 Result: {result4}");