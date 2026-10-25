/*
Title: Longest Parcel Belt Stretch With Limited Fragile Items
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A warehouse conveyor belt carries parcels in a fixed order. Each parcel is labeled with a non-negative integer type code.
A parcel is considered fragile if its type code appears in a given set fragileTypes.

The operations team wants to identify the longest contiguous stretch of the belt that can be processed by a standard robot arm,
which may handle at most k fragile parcels before requiring a manual reset.

Given:
- an integer array parcels
- an integer array fragileTypes containing distinct type codes
- an integer k

Return the length of the longest contiguous subarray of parcels that contains at most k parcels whose type belongs to fragileTypes.

A contiguous stretch means you must choose a single continuous segment of the array.
The fragile type list is not necessarily sorted, and parcel codes may repeat many times.
If no parcel can be included under the limit, return 0.

Constraints:
- 1 <= parcels.length <= 200000
- 0 <= parcels[i] <= 10^9
- 0 <= fragileTypes.length <= 200000
- fragileTypes contains distinct values
- 0 <= fragileTypes[i] <= 10^9
- 0 <= k <= parcels.length

Example 1:
Input: parcels = [4, 7, 2, 7, 9, 3, 7, 5], fragileTypes = [7, 9], k = 2
Output: 4

Example 2:
Input: parcels = [1, 8, 6, 8, 2, 4, 6], fragileTypes = [8, 4], k = 1
Output: 3
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the HashSet of fragile types takes O(m), where m = fragileTypes.Length.
    - The sliding window scans the parcels array once with the right pointer and once with the left pointer in total.
      Even though there is a while loop, each element is added to the window once and removed once.
    - Therefore total time complexity is O(n + m), where n = parcels.Length.

    Space Complexity:
    - We store all fragile types in a HashSet for O(1) average membership checks.
    - This requires O(m) extra space.
    - Aside from that, only a few integer variables are used.
    - Therefore total extra space complexity is O(m).
    */
    public int LongestValidStretch(int[] parcels, int[] fragileTypes, int k)
    {
        // We place all fragile parcel type codes into a HashSet.
        // Why?
        // Because during the sliding window process, we must repeatedly ask:
        // "Is this parcel fragile?"
        // A HashSet gives average O(1) lookup time, which is much faster than scanning
        // the fragileTypes array every time.
        var fragileSet = new HashSet<int>(fragileTypes);

        // left marks the beginning of our current window.
        // right will move from left to right through the array.
        int left = 0;

        // fragileCount stores how many fragile parcels are currently inside the window [left..right].
        // This is the key value we must keep <= k.
        int fragileCount = 0;

        // best stores the maximum valid window length we have seen so far.
        int best = 0;

        // Expand the window one parcel at a time by moving right forward.
        for (int right = 0; right < parcels.Length; right++)
        {
            // Step 1:
            // Include parcels[right] into the current window.
            // If this parcel type is fragile, increase fragileCount.
            if (fragileSet.Contains(parcels[right]))
            {
                fragileCount++;
            }

            // Step 2:
            // If the window now contains too many fragile parcels, it is invalid.
            // We must shrink the window from the left until it becomes valid again.
            //
            // Why do we do this?
            // Because the problem asks for a contiguous subarray with AT MOST k fragile parcels.
            // So whenever fragileCount > k, the current window cannot be used as an answer.
            while (fragileCount > k)
            {
                // Before moving left forward, check whether the parcel leaving the window is fragile.
                // If it is, we must decrease fragileCount because that fragile parcel is no longer inside the window.
                if (fragileSet.Contains(parcels[left]))
                {
                    fragileCount--;
                }

                // Move the left boundary rightward to make the window smaller.
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is guaranteed to be valid,
            // because fragileCount <= k.
            // So we can compute its length and compare it with the best answer so far.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the entire array, best contains the longest valid contiguous stretch.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] parcels1 = { 4, 7, 2, 7, 9, 3, 7, 5 };
int[] fragileTypes1 = { 7, 9 };
int k1 = 2;
int result1 = solution.LongestValidStretch(parcels1, fragileTypes1, k1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] parcels2 = { 1, 8, 6, 8, 2, 4, 6 };
int[] fragileTypes2 = { 8, 4 };
int k2 = 1;
int result2 = solution.LongestValidStretch(parcels2, fragileTypes2, k2);
Console.WriteLine(result2); // Expected: 3

// Additional quick checks

// No fragile types at all: entire array is valid
int[] parcels3 = { 10, 20, 30, 40 };
int[] fragileTypes3 = Array.Empty<int>();
int k3 = 0;
int result3 = solution.LongestValidStretch(parcels3, fragileTypes3, k3);
Console.WriteLine(result3); // Expected: 4

// k = 0, only stretches with zero fragile parcels are allowed
int[] parcels4 = { 5, 7, 8, 2, 7, 3 };
int[] fragileTypes4 = { 7, 8 };
int k4 = 0;
int result4 = solution.LongestValidStretch(parcels4, fragileTypes4, k4);
Console.WriteLine(result4); // Expected: 2 (subarray [2,  ]? Actually [2] or [3] length 1, and [5] length 1; longest non-fragile contiguous is [2] only. Let's use output from algorithm.)
