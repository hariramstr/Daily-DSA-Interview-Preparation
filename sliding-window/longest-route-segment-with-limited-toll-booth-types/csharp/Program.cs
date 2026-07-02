/*
Title: Longest Route Segment With Limited Toll Booth Types

Problem Description:
A navigation company records the sequence of toll booths a truck passes during a long highway trip.
Each toll booth is labeled by an integer type representing the toll operator that manages it.

For billing simplification, the company wants to analyze the longest contiguous segment of the trip
that uses at most k distinct toll booth types.

Given an integer array booths where booths[i] is the type of the i-th toll booth encountered, and an
integer k, return the length of the longest contiguous subarray containing at most k distinct values.

A segment is contiguous if it consists of consecutive toll booths in the original trip log.
If k is 0, then no toll booth type can be included, so the answer is 0.

An efficient O(n) or O(n log n) solution is expected.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the length of the booths array.
    Each element is added to the sliding window once by the right pointer,
    and removed from the sliding window at most once by the left pointer.

    Space Complexity:
    O(k) on average for the frequency map in the active window,
    or more precisely O(min(n, number of distinct values in booths)).
    */
    public int LongestSegmentWithAtMostKDistinct(int[] booths, int k)
    {
        // If k is 0, the problem states that we cannot include any toll booth type.
        // That means the longest valid contiguous segment has length 0.
        if (k == 0 || booths.Length == 0)
        {
            return 0;
        }

        // This dictionary stores:
        // key   = toll booth type
        // value = how many times that type currently appears inside the sliding window
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know
        // whether removing one booth completely removes that type from the window,
        // or whether that type still exists elsewhere in the current window.
        var frequency = new Dictionary<int, int>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to booths.Length - 1.
        for (int right = 0; right < booths.Length; right++)
        {
            // Step 1: Include booths[right] in the current window.
            //
            // We are extending the window to the right by one element.
            // So we must update the frequency map to reflect that this booth type
            // is now part of the current window.
            int currentType = booths[right];

            if (frequency.ContainsKey(currentType))
            {
                frequency[currentType]++;
            }
            else
            {
                frequency[currentType] = 1;
            }

            // Step 2: If the window has become invalid, shrink it from the left.
            //
            // A window is invalid when it contains more than k distinct booth types.
            // While that is true, we keep moving left forward until the window becomes valid again.
            //
            // This is the key sliding window idea:
            // - right only moves forward
            // - left only moves forward
            // Therefore the total work is linear.
            while (frequency.Count > k)
            {
                // The booth type at the left edge is about to leave the window.
                int leftType = booths[left];

                // Decrease its count because it is no longer inside the window.
                frequency[leftType]--;

                // If its count becomes 0, that type no longer exists in the window at all.
                // We remove it from the dictionary so frequency.Count correctly reflects
                // the number of distinct booth types in the current window.
                if (frequency[leftType] == 0)
                {
                    frequency.Remove(leftType);
                }

                // Move the left boundary rightward by one position.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid.
            // It contains at most k distinct booth types.
            //
            // So we compute its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the entire array, best holds the length of the longest
        // contiguous subarray with at most k distinct values.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] booths1 = { 4, 7, 4, 4, 9, 7, 9, 9 };
int k1 = 2;
int result1 = solution.LongestSegmentWithAtMostKDistinct(booths1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] booths2 = { 5, 5, 1, 2, 1, 2, 3 };
int k2 = 3;
int result2 = solution.LongestSegmentWithAtMostKDistinct(booths2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo: k = 0
int[] booths3 = { 1, 2, 3, 4 };
int k3 = 0;
int result3 = solution.LongestSegmentWithAtMostKDistinct(booths3, k3);
Console.WriteLine($"Additional Demo (k=0) Result: {result3}");