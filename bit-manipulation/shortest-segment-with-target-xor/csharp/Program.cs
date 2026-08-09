/*
Title: Shortest Segment With Target XOR

Problem Description:
You are given an array `nums` of non-negative integers and an integer `target`.
A contiguous segment of the array is called valid if the bitwise XOR of all values
in that segment is exactly equal to `target`.

Return the length of the shortest valid segment. If no such segment exists, return `-1`.

A segment must contain at least one element. The XOR of a segment `nums[l..r]` is defined as:
nums[l] ^ nums[l+1] ^ ... ^ nums[r]

Key idea:
Use prefix XOR.

If prefixXor[i] means XOR of the first i elements, then:
XOR of subarray nums[l..r] = prefixXor[r + 1] ^ prefixXor[l]

We want:
prefixXor[r + 1] ^ prefixXor[l] = target

Rearrange:
prefixXor[l] = prefixXor[r + 1] ^ target

So while scanning from left to right, for each current prefix XOR value, we need to know
whether the needed previous prefix XOR has appeared before.

Because we want the SHORTEST valid segment, for each prefix XOR value we should remember
the MOST RECENT index where it appeared. That gives the smallest distance to the current index.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation:
    - We scan the array once.
    - Each step performs O(1) average-time dictionary operations.
    - The dictionary stores at most one entry per distinct prefix XOR value.
    */
    public int ShortestSegmentWithTargetXor(int[] nums, int target)
    {
        // This dictionary maps:
        //   prefix XOR value -> latest index where this prefix XOR occurred
        //
        // Important indexing detail:
        // We define prefix XOR over "positions between elements".
        //
        // prefix index 0 means: XOR of zero elements = 0
        // prefix index 1 means: XOR of nums[0]
        // prefix index 2 means: XOR of nums[0] ^ nums[1]
        // ...
        //
        // If a subarray is nums[l..r], then its XOR is:
        // prefix[r + 1] ^ prefix[l]
        //
        // So if we are currently at prefix index i = r + 1,
        // we need an earlier prefix index j = l such that:
        // prefix[j] = prefix[i] ^ target
        //
        // To minimize subarray length i - j, we want j as large as possible,
        // which is why we store the latest occurrence of each prefix XOR.
        var latestIndexByPrefixXor = new Dictionary<int, int>();

        // Before reading any elements, prefix XOR is 0 at prefix index 0.
        // This allows subarrays that start at index 0 to be handled naturally.
        latestIndexByPrefixXor[0] = 0;

        int prefixXor = 0;

        // We will keep track of the best (smallest) valid segment length found so far.
        int answer = int.MaxValue;

        // We iterate through the array.
        // Let i be the array index (0-based).
        // After processing nums[i], the corresponding prefix index is i + 1.
        for (int i = 0; i < nums.Length; i++)
        {
            // Step 1:
            // Extend the running prefix XOR by including nums[i].
            //
            // Why?
            // prefixXor should always represent XOR of nums[0..i].
            prefixXor ^= nums[i];

            // Current prefix position is i + 1.
            int currentPrefixIndex = i + 1;

            // Step 2:
            // Figure out which previous prefix XOR value we need.
            //
            // We want:
            // subarray XOR = target
            // prefix[current] ^ prefix[previous] = target
            //
            // Therefore:
            // prefix[previous] = prefix[current] ^ target
            int neededPreviousPrefixXor = prefixXor ^ target;

            // Step 3:
            // If that needed prefix XOR has appeared before,
            // then we found a valid subarray ending at i.
            if (latestIndexByPrefixXor.TryGetValue(neededPreviousPrefixXor, out int previousPrefixIndex))
            {
                // The subarray is from:
                // l = previousPrefixIndex
                // r = currentPrefixIndex - 1 = i
                //
                // Its length is:
                // currentPrefixIndex - previousPrefixIndex
                int length = currentPrefixIndex - previousPrefixIndex;

                // Update the best answer if this segment is shorter.
                if (length < answer)
                {
                    answer = length;
                }
            }

            // Step 4:
            // Record the current prefix XOR at the current prefix index.
            //
            // Why overwrite with the latest index?
            // Because for future positions, using the latest matching previous prefix
            // gives the shortest possible segment.
            //
            // Example:
            // If the same prefix XOR appeared at indices 2 and 7,
            // and later we are at index 10,
            // then length using 7 is 3, which is shorter than using 2 (length 8).
            latestIndexByPrefixXor[prefixXor] = currentPrefixIndex;
        }

        // If answer was never updated, no valid segment exists.
        return answer == int.MaxValue ? -1 : answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// nums = [5, 1, 2, 1, 5], target = 3
// Valid shortest segment is [1, 2], length = 2
int[] nums1 = { 5, 1, 2, 1, 5 };
int target1 = 3;
int result1 = solution.ShortestSegmentWithTargetXor(nums1, target1);
Console.WriteLine(result1); // Expected: 2

// Example 2:
// nums = [4, 7, 4, 7], target = 0
// Whole array XOR is 0, and no shorter contiguous segment has XOR 0
// So answer = 4
int[] nums2 = { 4, 7, 4, 7 };
int target2 = 0;
int result2 = solution.ShortestSegmentWithTargetXor(nums2, target2);
Console.WriteLine(result2); // Expected: 4

// Additional quick checks

// Single element equals target
int[] nums3 = { 8 };
int target3 = 8;
Console.WriteLine(solution.ShortestSegmentWithTargetXor(nums3, target3)); // Expected: 1

// No valid segment
int[] nums4 = { 1, 2, 4 };
int target4 = 7;
Console.WriteLine(solution.ShortestSegmentWithTargetXor(nums4, target4)); // Expected: 3 because 1^2^4 = 7

// Truly no valid segment
int[] nums5 = { 1, 2, 4 };
int target5 = 6;
Console.WriteLine(solution.ShortestSegmentWithTargetXor(nums5, target5)); // Expected: 2 because 2^4 = 6

// Another no-solution example
int[] nums6 = { 1, 2, 4 };
int target6 = 8;
Console.WriteLine(solution.ShortestSegmentWithTargetXor(nums6, target6)); // Expected: -1