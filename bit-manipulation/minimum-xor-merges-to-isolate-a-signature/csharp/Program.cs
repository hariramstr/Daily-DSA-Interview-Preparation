/*
Title: Minimum XOR Merges to Isolate a Signature
Difficulty: Hard
Topic: Bit Manipulation

Problem Description:
You are given an array nums of n non-negative integers representing packet signatures. In one operation, you may choose any adjacent pair nums[i] and nums[i+1], remove both values, and replace them with a single value equal to their bitwise XOR. This reduces the array length by 1. You may repeat this operation any number of times until only one value remains, or stop earlier.

Your task is to find the minimum number of merge operations required so that the value x appears somewhere in the array at least once after performing the operations. If it is impossible, return -1.

A merge can only be performed on adjacent elements, and each merge changes the array structure, so the order of remaining segments must always be consistent with the original order. Equivalently, after several merges, every remaining element corresponds to the XOR of some contiguous subarray of the original array.

Return the minimum number of merges needed to make at least one remaining segment have XOR exactly x.

Constraints:
- 1 <= n <= 200000
- 0 <= nums[i] < 2^30
- 0 <= x < 2^30
- The solution is expected to be better than O(n^2).

Example 1:
Input: nums = [5, 1, 4, 1], x = 4
Output: 1
Explanation: Merge the first two elements: 5 XOR 1 = 4. The array becomes [4, 4, 1]. Now the value 4 appears, so the answer is 1.

Example 2:
Input: nums = [2, 7, 2, 7], x = 0
Output: 3
Explanation: The XOR of the entire array is 0, so merging all elements into one segment produces [0]. This takes 3 merges. No shorter sequence can create a segment with XOR 0.

Key Observation:
If a contiguous subarray nums[l..r] has XOR equal to x, then we can merge that entire subarray into one value x.
A subarray of length L requires exactly L - 1 merge operations to collapse into one segment.
Therefore, the problem becomes:

Find the shortest contiguous subarray whose XOR is x.
If its length is L, answer = L - 1.
If no such subarray exists, answer = -1.

Prefix XOR Fact:
Let prefix[i] be XOR of nums[0..i-1], with prefix[0] = 0.
Then XOR of subarray nums[l..r] is:
prefix[r + 1] XOR prefix[l]

We want:
prefix[r + 1] XOR prefix[l] = x
So:
prefix[l] = prefix[r + 1] XOR x

Thus, while scanning from left to right, for each current prefix value we need to know whether
(currentPrefix XOR x) appeared earlier, and if so, what is the largest index where it appeared,
because that gives the shortest subarray ending here.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Step-by-step idea:
    1. Compute prefix XOR values while scanning the array from left to right.
    2. For each position i (1-based in prefix terms), let currentPrefix be XOR of nums[0..i-1].
    3. We want a previous prefix index j such that:
           prefix[j] XOR currentPrefix = x
       which is equivalent to:
           prefix[j] = currentPrefix XOR x
    4. If such a j exists, then subarray nums[j..i-1] has XOR x.
       Its length is i - j, so merges needed are (i - j - 1).
    5. To minimize merges, we must minimize subarray length.
       For a fixed ending position i, the shortest valid subarray comes from the largest possible j.
       Therefore, for each prefix XOR value, we store the latest index where it appeared.
    6. Track the minimum subarray length over the whole scan.
    7. If no valid subarray is found, return -1. Otherwise return minLength - 1.

    Why storing the latest index works:
    - Suppose the needed prefix value appeared multiple times.
    - If we choose a later occurrence, the subarray becomes shorter.
    - Since we want the shortest subarray, the latest occurrence is always the best one.
    */
    public int MinimumXorMerges(int[] nums, int x)
    {
        // This dictionary maps:
        //   prefix XOR value -> latest index where this prefix occurred
        //
        // Important indexing detail:
        // - prefix index 0 means "before reading any elements"
        // - after reading nums[0], we are at prefix index 1
        // - after reading nums[1], we are at prefix index 2
        //
        // So if prefix index j and current prefix index i satisfy:
        //   prefix[j] XOR prefix[i] = x
        // then the subarray is nums[j..i-1], with length i - j.
        var latestIndexOfPrefix = new Dictionary<int, int>();

        // Before processing any numbers, prefix XOR is 0 at index 0.
        // This is necessary so subarrays starting at index 0 can be detected.
        latestIndexOfPrefix[0] = 0;

        // Running prefix XOR as we scan the array.
        int prefixXor = 0;

        // We will store the shortest valid subarray length found so far.
        // Start with a very large value meaning "not found yet".
        int minLength = int.MaxValue;

        // Scan through the array.
        // We use i from 1 to nums.Length in prefix indexing terms.
        for (int i = 1; i <= nums.Length; i++)
        {
            // Step 1:
            // Extend the prefix XOR by including the next array element.
            // After this line, prefixXor equals XOR of nums[0..i-1].
            prefixXor ^= nums[i - 1];

            // Step 2:
            // We want a previous prefix value such that:
            //   previousPrefix XOR prefixXor = x
            // Rearranging gives:
            //   previousPrefix = prefixXor XOR x
            //
            // If that previous prefix exists at some index j,
            // then nums[j..i-1] has XOR exactly x.
            int neededPreviousPrefix = prefixXor ^ x;

            // Step 3:
            // Check whether we have seen that needed prefix before.
            if (latestIndexOfPrefix.TryGetValue(neededPreviousPrefix, out int previousIndex))
            {
                // The subarray from previousIndex to i - 1 has XOR x.
                int currentLength = i - previousIndex;

                // Since we want the minimum number of merges,
                // and merges = length - 1,
                // we should minimize the subarray length first.
                if (currentLength < minLength)
                {
                    minLength = currentLength;
                }
            }

            // Step 4:
            // Update the latest occurrence of the current prefix XOR to this index i.
            //
            // Why latest, not earliest?
            // Because for future positions, using a larger previous index creates
            // a shorter subarray, which is exactly what we want.
            latestIndexOfPrefix[prefixXor] = i;
        }

        // If minLength was never updated, no valid subarray exists.
        if (minLength == int.MaxValue)
        {
            return -1;
        }

        // A subarray of length L can be merged into one value using exactly L - 1 merges.
        return minLength - 1;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// nums = [5, 1, 4, 1], x = 4
// Shortest subarray with XOR 4 is [4] (length 1) or [5,1] (length 2).
// Since [4] already exists, minimum merges = 0.
// This follows the problem statement note: if some nums[i] == x, answer is 0.
int[] nums1 = { 5, 1, 4, 1 };
int x1 = 4;
int result1 = solution.MinimumXorMerges(nums1, x1);
Console.WriteLine(result1);

// Example 2:
// nums = [2, 7, 2, 7], x = 0
// Entire array XOR is 0, length 4, so merges = 3.
// No shorter subarray has XOR 0.
int[] nums2 = { 2, 7, 2, 7 };
int x2 = 0;
int result2 = solution.MinimumXorMerges(nums2, x2);
Console.WriteLine(result2);

// Additional demo 1:
// Already contains x directly, so answer should be 0.
int[] nums3 = { 8, 3, 6 };
int x3 = 3;
int result3 = solution.MinimumXorMerges(nums3, x3);
Console.WriteLine(result3);

// Additional demo 2:
// No subarray XOR equals x, so answer should be -1.
int[] nums4 = { 1, 2, 4 };
int x4 = 7;
int result4 = solution.MinimumXorMerges(nums4, x4);
Console.WriteLine(result4);