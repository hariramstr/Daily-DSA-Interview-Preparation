/*
Title: Longest Promo Window With Limited Duplicate Coupons
Difficulty: Medium
Topic: Sliding Window

Problem Description:
An e-commerce platform records the coupon code used in each order during a marketing campaign.
The analytics team wants to find the longest contiguous block of orders that is still considered
"diverse enough" for reporting. A block is valid if no coupon code appears more than k times
inside that block.

You are given an array coupons where coupons[i] is the coupon code used in the i-th order,
and an integer k. Return the length of the longest contiguous subarray such that every distinct
coupon code appears at most k times within that subarray.

This is a realistic streaming-style problem: orders arrive in sequence, and you need to maintain
a valid window efficiently as you scan from left to right. A brute-force check of every subarray
will be too slow for large inputs.

Constraints:
- 1 <= coupons.length <= 200000
- 1 <= coupons[i] <= 1000000000
- 1 <= k <= coupons.length
- coupons may contain many repeated values

Example 1:
Input: coupons = [4, 1, 4, 2, 4, 1, 2, 2], k = 2
Output: 5
Explanation: One longest valid window is [1, 4, 2, 4, 1], where coupon 1 appears 2 times,
coupon 4 appears 2 times, and coupon 2 appears 1 time. Any longer window would cause either
coupon 4 or coupon 2 to appear more than 2 times.

Example 2:
Input: coupons = [7, 7, 7, 8, 8, 9, 7], k = 1
Output: 3
Explanation: A valid longest window is [7, 8, 9] or [8, 9, 7]. In any valid window, each
coupon code must be unique because k = 1.

We only need to compute the maximum valid length, not the subarray itself.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the sliding window once by moving the right pointer.
    - Each element is removed from the sliding window at most once by moving the left pointer.
    - Therefore, the total work across the whole array is linear.

    Space Complexity: O(m)
    - We store frequencies of coupon codes currently inside the window.
    - m is the number of distinct coupon codes in the current window, and in the worst case
      it can be O(n).
    */
    public int MaxSubarrayLength(int[] coupons, int k)
    {
        // This dictionary maps:
        // coupon code -> how many times that coupon currently appears inside our sliding window.
        //
        // Why a dictionary?
        // Coupon values can be as large as 1,000,000,000, so we cannot use a simple array indexed
        // by coupon value efficiently. A Dictionary lets us store only the values that actually appear.
        var frequency = new Dictionary<int, int>();

        // left marks the beginning of the current window.
        // right will expand the window one step at a time.
        int left = 0;

        // best stores the maximum valid window length we have seen so far.
        int best = 0;

        // We scan from left to right using the right pointer.
        // At each step, we include coupons[right] into the current window.
        for (int right = 0; right < coupons.Length; right++)
        {
            int currentCoupon = coupons[right];

            // Step 1: Add the new coupon at position "right" into the window.
            //
            // This means we increase its count in the frequency map.
            // If it was not present before, we start its count at 0 and then increment.
            if (!frequency.ContainsKey(currentCoupon))
            {
                frequency[currentCoupon] = 0;
            }

            frequency[currentCoupon]++;

            // Step 2: Check whether the window is still valid.
            //
            // The problem says every distinct coupon code must appear at most k times.
            // Since we only changed the count of currentCoupon, the only possible violation
            // is that currentCoupon might now appear more than k times.
            //
            // If that happens, we must shrink the window from the left until the violation disappears.
            while (frequency[currentCoupon] > k)
            {
                int leftCoupon = coupons[left];

                // Remove the leftmost coupon from the window by decreasing its count.
                frequency[leftCoupon]--;

                // Move the left boundary one step to the right,
                // because that element is no longer part of the window.
                left++;

                // We do not need to check every key in the dictionary.
                // The window became invalid only because currentCoupon exceeded k,
                // so shrinking continues until currentCoupon is back within the allowed limit.
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid.
            //
            // Why?
            // - Before adding currentCoupon, the previous window was valid.
            // - After adding currentCoupon, only its count could have become invalid.
            // - The while loop shrank the window until currentCoupon's count was <= k again.
            //
            // Therefore every coupon code in the window now appears at most k times.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the entire array, best is the length of the longest valid subarray.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] coupons1 = { 4, 1, 4, 2, 4, 1, 2, 2 };
int k1 = 2;
int result1 = solution.MaxSubarrayLength(coupons1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 5

// Example 2
int[] coupons2 = { 7, 7, 7, 8, 8, 9, 7 };
int k2 = 1;
int result2 = solution.MaxSubarrayLength(coupons2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 3

// Additional quick demo
int[] coupons3 = { 1, 2, 3, 1, 2, 3, 1, 2 };
int k3 = 2;
int result3 = solution.MaxSubarrayLength(coupons3, k3);
Console.WriteLine($"Additional Demo Result: {result3}"); // One valid answer: 6