/*
Title: Longest Stream Window With Pairwise Bitwise Overlap Budget

Problem Description:
You are given an array nums of length n, where each nums[i] is a non-negative integer representing the feature mask of the i-th event in a real-time stream.
Two events are considered conflicting if their bitwise AND is non-zero, meaning they share at least one enabled feature bit.

For any contiguous window nums[l..r], define its overlap cost as the total number of conflicting pairs inside that window.
In other words, for all pairs (i, j) such that l <= i < j <= r, count 1 if (nums[i] & nums[j]) != 0, and 0 otherwise.
The overlap cost of the window is the sum of those counts.

Your task is to return the length of the longest contiguous window whose overlap cost is at most k.

This problem is harder than a standard sliding window because adding one value may create conflicts with many earlier values,
and the number of conflicts depends on shared bits across the whole window.
A correct solution must efficiently maintain the number of conflicting pairs while expanding and shrinking the window.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= nums[i] < 2^20
- 0 <= k <= n * (n - 1) / 2
- nums may contain duplicates

Example 1:
Input: nums = [1, 2, 3, 8, 10], k = 2
Output: 4

Example 2:
Input: nums = [5, 1, 4, 2, 8, 3], k = 1
Output: 3
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n * 2^b), where b = number of bits (here b = 20)
    - Since 2^20 = 1,048,576 and each number has at most 20 bits, this is practical for the given constraints.
    - More precisely:
      * For each element added to the window, we compute how many existing elements conflict with it.
      * We do this using inclusion-exclusion over the set bits of that number.
      * A number with t set bits requires O(2^t) work, and t <= 20.
      * Each element is added once and removed once in the sliding window.

    Space Complexity:
    - O(2^20) for frequency counts of exact masks
    - O(2^20) for subset zeta-transformed counts used to answer "how many masks are disjoint with x?"
    - Total O(2^20)
    */
    public int LongestWindowWithOverlapBudget(int[] nums, long k)
    {
        // There are only 20 possible bit positions because nums[i] < 2^20.
        const int BITS = 20;
        int maxMask = 1 << BITS;
        int fullMask = maxMask - 1;

        // exactCount[mask] = how many numbers currently in the sliding window are exactly equal to "mask".
        //
        // Why we need this:
        // - When we add/remove a number, we update the count of its exact mask.
        // - This is the most direct representation of the multiset of values in the current window.
        int[] exactCount = new int[maxMask];

        // subsetCount[mask] will maintain:
        // "How many numbers currently in the window are subsets of 'mask'?"
        //
        // In other words, if a value y satisfies (y & ~mask) == 0, then y contributes to subsetCount[mask].
        //
        // Why this is useful:
        // - To know how many existing numbers are DISJOINT with x, we need numbers y such that (x & y) == 0.
        // - That means y can only use bits from the complement of x.
        // - So the number of disjoint values is subsetCount[fullMask ^ x].
        //
        // Then:
        // conflicts created by adding x = currentWindowSize - disjointCount
        //
        // This is the key trick that makes the sliding window efficient.
        int[] subsetCount = new int[maxMask];

        int left = 0;
        int best = 0;

        // currentPairs = current overlap cost of the window:
        // number of pairs (i, j) inside the current window such that nums[i] & nums[j] != 0.
        long currentPairs = 0;

        // windowSize is tracked explicitly because we frequently need:
        // conflicts when adding x = windowSize - disjointCount
        int windowSize = 0;

        for (int right = 0; right < nums.Length; right++)
        {
            int x = nums[right];

            // STEP 1: Determine how many existing values in the current window conflict with x.
            //
            // A value y is disjoint with x if (x & y) == 0.
            // Such a y must be a subset of the complement of x.
            //
            // complementMask = all bit positions that x does NOT use.
            int complementMask = fullMask ^ x;

            // subsetCount[complementMask] tells us exactly how many current window values are disjoint with x.
            int disjointCount = subsetCount[complementMask];

            // Every existing value either:
            // - is disjoint with x  -> no new conflicting pair
            // - is not disjoint     -> creates exactly one new conflicting pair with x
            //
            // Therefore:
            int newConflicts = windowSize - disjointCount;

            // Add those newly created conflicting pairs to the window's total overlap cost.
            currentPairs += newConflicts;

            // STEP 2: Actually insert x into our data structures.
            //
            // We must update:
            // - exactCount[x]
            // - subsetCount[s] for every superset s of x
            //
            // Why every superset?
            // Because subsetCount[s] means "how many current values are subsets of s".
            // If x is added, then x contributes to every s where x is a subset of s.
            AddMask(x, exactCount, subsetCount, BITS, fullMask);

            windowSize++;

            // STEP 3: If the overlap cost is too large, shrink from the left until valid again.
            //
            // This is the standard sliding window pattern:
            // - expand right
            // - while invalid, move left
            //
            // The challenge here is correctly removing the contribution of nums[left].
            while (currentPairs > k)
            {
                int y = nums[left];

                // Before removing y, we need to know how many conflicting pairs currently involve y.
                //
                // In the CURRENT window, including y:
                // - total other elements = windowSize - 1
                // - disjoint others with y = count of values disjoint with y, excluding y itself
                //
                // subsetCount[fullMask ^ y] counts all current values disjoint with y.
                // Note:
                // - y itself is included in that count only if y == 0, because only 0 is disjoint with itself.
                //
                // A cleaner and always-correct way:
                // conflicts involving y = number of current values z with (y & z) != 0, excluding y itself
                //
                // We can compute:
                // disjointIncludingSelf = subsetCount[fullMask ^ y]
                // nonDisjointIncludingSelf = windowSize - disjointIncludingSelf
                //
                // If y != 0, then y is counted among non-disjoint values with itself, so subtract 1.
                // If y == 0, then y is counted among disjoint values with itself, so non-disjoint count already excludes self.
                int disjointIncludingSelf = subsetCount[fullMask ^ y];
                int conflictsWithY = windowSize - disjointIncludingSelf - (y == 0 ? 0 : 1);

                // Removing y deletes exactly all conflicting pairs that involve y.
                currentPairs -= conflictsWithY;

                // Now physically remove y from the data structures.
                RemoveMask(y, exactCount, subsetCount, BITS, fullMask);

                windowSize--;
                left++;
            }

            // STEP 4: The current window [left..right] is valid, so update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }

    private static void AddMask(int mask, int[] exactCount, int[] subsetCount, int bits, int fullMask)
    {
        // Increase exact frequency of this exact value.
        exactCount[mask]++;

        // We must add this value to subsetCount[s] for every superset s of mask.
        //
        // Standard superset enumeration trick:
        // Let missing = bits not present in mask.
        // Any superset of mask can be written as:
        //   mask | extra
        // where extra is any subset of missing.
        int missing = fullMask ^ mask;
        int extra = missing;

        while (true)
        {
            int superset = mask | extra;
            subsetCount[superset]++;

            if (extra == 0)
            {
                break;
            }

            extra = (extra - 1) & missing;
        }
    }

    private static void RemoveMask(int mask, int[] exactCount, int[] subsetCount, int bits, int fullMask)
    {
        // Decrease exact frequency of this exact value.
        exactCount[mask]--;

        // Symmetric to AddMask:
        // remove this value from subsetCount[s] for every superset s of mask.
        int missing = fullMask ^ mask;
        int extra = missing;

        while (true)
        {
            int superset = mask | extra;
            subsetCount[superset]--;

            if (extra == 0)
            {
                break;
            }

            extra = (extra - 1) & missing;
        }
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] nums1 = { 1, 2, 3, 8, 10 };
long k1 = 2;
int result1 = solution.LongestWindowWithOverlapBudget(nums1, k1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] nums2 = { 5, 1, 4, 2, 8, 3 };
long k2 = 1;
int result2 = solution.LongestWindowWithOverlapBudget(nums2, k2);
Console.WriteLine(result2); // Expected: 3

// Additional quick checks
int[] nums3 = { 0, 0, 0 };
long k3 = 0;
int result3 = solution.LongestWindowWithOverlapBudget(nums3, k3);
Console.WriteLine(result3); // Expected: 3

int[] nums4 = { 1, 1, 1 };
long k4 = 1;
int result4 = solution.LongestWindowWithOverlapBudget(nums4, k4);
Console.WriteLine(result4); // Expected: 2