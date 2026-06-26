/*
Title: Longest Viewing Streak With Limited Ad Categories
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A video platform records the category of the ad shown before each video in a user's session.
You are given an array ads where ads[i] is the category ID of the ad shown at minute i, and
an integer k. Your task is to find the length of the longest contiguous viewing streak such
that the number of distinct ad categories appearing in that streak is at most k.

A viewing streak is any contiguous subarray of ads. Distinct categories are counted by category ID,
so repeated appearances of the same category only count once toward the limit. Return the maximum
possible length of such a streak.

This problem models a real analytics scenario where the platform wants to identify the longest
period of time during which ad variety stayed within an acceptable threshold.

Constraints:
- 1 <= ads.length <= 200000
- 1 <= ads[i] <= 1000000000
- 1 <= k <= ads.length

Example 1:
Input: ads = [4, 2, 2, 5, 5, 2, 4], k = 2
Output: 5
Explanation: The longest valid streak is [2, 2, 5, 5, 2], which contains only 2 distinct categories: 2 and 5.

Example 2:
Input: ads = [1, 3, 1, 3, 2, 2, 4, 2], k = 3
Output: 6
Explanation:
The subarray [1, 3, 1, 3, 2, 2, 4] has 4 distinct categories, so it is invalid.
A valid longest streak is [1, 3, 1, 3, 2, 2], or [3, 1, 3, 2, 2, 4], each of length 6.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the sliding window once by the right pointer.
    - Each element is removed from the sliding window at most once by the left pointer.
    - Therefore, the total work is linear in the size of the array.

    Space Complexity: O(k) on average, O(n) in the worst case
    - We store frequencies of the distinct ad categories currently inside the window.
    - In the worst case, the window may contain many distinct values.
    */
    public int LongestViewingStreak(int[] ads, int k)
    {
        // This dictionary will store:
        // key   = ad category ID
        // value = how many times that category appears in the current window
        //
        // Why do we need this?
        // Because the rule is based on the number of DISTINCT categories.
        // A dictionary lets us:
        // 1. Quickly increase the count when we expand the window
        // 2. Quickly decrease the count when we shrink the window
        // 3. Know exactly when a category disappears from the window
        var frequency = new Dictionary<int, int>();

        // 'left' marks the start of the current sliding window.
        // The window will always be ads[left..right].
        int left = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We move 'right' from left to right across the array.
        // At each step, we include ads[right] into the current window.
        for (int right = 0; right < ads.Length; right++)
        {
            int currentCategory = ads[right];

            // Step 1: Add the new right-side element into the frequency map.
            //
            // If the category is already present, increase its count.
            // Otherwise, start its count at 1.
            //
            // This is necessary because our window has grown by one element,
            // so the data structure must reflect the new contents of the window.
            if (frequency.ContainsKey(currentCategory))
            {
                frequency[currentCategory]++;
            }
            else
            {
                frequency[currentCategory] = 1;
            }

            // Step 2: If the window has become invalid, shrink it from the left.
            //
            // The window is invalid when the number of distinct categories
            // is greater than k.
            //
            // Why use a while loop instead of an if?
            // Because removing just one element from the left may not be enough.
            // We must keep shrinking until the window becomes valid again.
            while (frequency.Count > k)
            {
                int leftCategory = ads[left];

                // We are removing ads[left] from the window,
                // so decrease its frequency.
                frequency[leftCategory]--;

                // If its frequency becomes 0, that means this category
                // no longer exists anywhere in the current window.
                //
                // At that moment, we must remove it from the dictionary.
                // This is very important because frequency.Count is being used
                // as the number of distinct categories in the current window.
                if (frequency[leftCategory] == 0)
                {
                    frequency.Remove(leftCategory);
                }

                // Move the left boundary one step to the right,
                // because that element has now been excluded from the window.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid.
            // It contains at most k distinct categories.
            //
            // So we can safely compute its length and compare it with the best answer.
            int currentLength = right - left + 1;

            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After processing all possible right endpoints,
        // maxLength contains the longest valid contiguous subarray length.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] ads1 = { 4, 2, 2, 5, 5, 2, 4 };
int k1 = 2;
int result1 = solution.LongestViewingStreak(ads1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] ads2 = { 1, 3, 1, 3, 2, 2, 4, 2 };
int k2 = 3;
int result2 = solution.LongestViewingStreak(ads2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 6

// Additional demo
int[] ads3 = { 7, 7, 7, 7 };
int k3 = 1;
int result3 = solution.LongestViewingStreak(ads3, k3);
Console.WriteLine("Additional Demo Result: " + result3); // Expected: 4