/*
Title: Longest Checkout Line With Limited Coupon Types
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A supermarket records the coupon type used by each customer in the order they join a checkout line.
You are given an integer array coupons where coupons[i] is the coupon type used by the i-th customer,
and an integer k. Your task is to find the length of the longest contiguous block of customers such
that the block contains at most k distinct coupon types.

This problem models a cashier lane that can efficiently process only a limited variety of coupon rules
at once. A valid block may contain repeated coupon types any number of times, but the total number of
different coupon types appearing inside the block must not exceed k.

Return the maximum possible length of such a contiguous block. If k is 0, no customer can be included,
so the answer is 0.

Constraints:
- 1 <= coupons.length <= 200000
- 1 <= coupons[i] <= 1000000000
- 0 <= k <= coupons.length

Example 1:
Input: coupons = [4, 2, 2, 5, 5, 2, 4, 4], k = 2
Output: 5
Explanation: The longest valid block is [2, 2, 5, 5, 2], which contains only coupon types 2 and 5.

Example 2:
Input: coupons = [1, 3, 1, 3, 2, 2, 2, 4], k = 3
Output: 7
Explanation: The block [1, 3, 1, 3, 2, 2, 2] contains exactly 3 distinct coupon types: 1, 3, and 2.
No longer contiguous block satisfies the limit.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the length of the coupons array.
    - Each element is added to the sliding window once by moving the right pointer,
      and removed from the sliding window at most once by moving the left pointer.
      So the total amount of work is linear.

    Space Complexity:
    - O(k) in the typical sliding-window sense, because the dictionary stores counts
      for the distinct coupon types currently inside the window.
    - In the worst case it can hold up to O(min(n, number of unique coupon types)) entries,
      but because we always shrink when distinct types exceed k, the active window tracks
      only the needed coupon types.
    */
    public int LongestCheckoutLine(int[] coupons, int k)
    {
        // If k is 0, we are not allowed to include any coupon type at all.
        // That means no customer can be part of a valid block.
        // So the answer is immediately 0.
        if (k == 0)
        {
            return 0;
        }

        // This dictionary maps:
        // coupon type -> how many times that coupon type appears in the current window.
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know
        // whether removing one customer completely removes a coupon type from the window
        // or whether that coupon type still exists elsewhere in the window.
        //
        // Example:
        // Window contains [2, 2, 5]
        // If we remove one 2 from the left, coupon type 2 still remains in the window.
        // So we must track frequencies, not just presence/absence.
        var frequency = new Dictionary<int, int>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from left to right across the array.
        for (int right = 0; right < coupons.Length; right++)
        {
            // Step 1: Include the new coupon at index "right" into the window.
            int currentCoupon = coupons[right];

            // If this coupon type is already in the window, increase its count.
            // Otherwise, add it with count 1.
            if (frequency.ContainsKey(currentCoupon))
            {
                frequency[currentCoupon]++;
            }
            else
            {
                frequency[currentCoupon] = 1;
            }

            // Step 2: If the window now contains more than k distinct coupon types,
            // it is invalid and must be shrunk from the left until it becomes valid again.
            //
            // Why a while loop and not an if?
            // Because adding one new coupon type can make the window invalid by exactly one
            // distinct type, but removing just one left element may not be enough to fix it.
            // We keep shrinking until the number of distinct coupon types is at most k.
            while (frequency.Count > k)
            {
                // Identify the coupon type at the left edge of the window.
                int leftCoupon = coupons[left];

                // We are removing this customer from the current window,
                // so decrease the frequency of that coupon type.
                frequency[leftCoupon]--;

                // If the count becomes 0, that coupon type no longer exists in the window.
                // We must remove it from the dictionary entirely.
                //
                // This is extremely important because frequency.Count is how we know
                // how many distinct coupon types are currently inside the window.
                if (frequency[leftCoupon] == 0)
                {
                    frequency.Remove(leftCoupon);
                }

                // Move the left boundary one step to the right,
                // because that customer is no longer part of the window.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid,
            // meaning it contains at most k distinct coupon types.
            //
            // So we can safely compute its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;

            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After processing all possible right endpoints, best contains the maximum
        // length of any valid contiguous block.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] coupons1 = { 4, 2, 2, 5, 5, 2, 4, 4 };
int k1 = 2;
int result1 = solution.LongestCheckoutLine(coupons1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] coupons2 = { 1, 3, 1, 3, 2, 2, 2, 4 };
int k2 = 3;
int result2 = solution.LongestCheckoutLine(coupons2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 7

// Additional demo: k = 0
int[] coupons3 = { 7, 7, 8 };
int k3 = 0;
int result3 = solution.LongestCheckoutLine(coupons3, k3);
Console.WriteLine("Example 3 Result: " + result3); // Expected: 0

// Additional demo: all same coupon type
int[] coupons4 = { 9, 9, 9, 9 };
int k4 = 1;
int result4 = solution.LongestCheckoutLine(coupons4, k4);
Console.WriteLine("Example 4 Result: " + result4); // Expected: 4