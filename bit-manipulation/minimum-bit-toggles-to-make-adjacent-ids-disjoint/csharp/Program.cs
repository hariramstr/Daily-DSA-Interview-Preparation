/*
Title: Minimum Bit Toggles to Make Adjacent IDs Disjoint

Problem Description:
You are given an array nums of length n, where each nums[i] is a non-negative integer representing a device ID encoded as a bitmask.
Two neighboring device IDs are considered conflicting if they share at least one common set bit, meaning (nums[i] & nums[i+1]) != 0.

In one operation, you may toggle off exactly one set bit from any single element in the array.
In other words, if bit b is currently 1 in nums[i], you may change nums[i] to nums[i] ^ (1 << b).
You are not allowed to toggle a 0 bit on, and you may perform any number of operations.

Return the minimum number of bit-toggle operations required so that every adjacent pair becomes disjoint, i.e. for every i from 0 to n - 2,
(nums[i] & nums[i+1]) == 0.

Your goal is to minimize the total number of toggled bits across the entire array.

Constraints:
- 1 <= n <= 100000
- 0 <= nums[i] < 2^20
- The answer always fits in a 32-bit signed integer.

Key Insight:
For each position i, after performing operations, the final value kept at that position must be a submask of nums[i]
because we are only allowed to turn 1 bits off, never on.

So if we call the final kept mask at position i as keep[i], then:
1) keep[i] must satisfy: keep[i] ⊆ nums[i]
2) keep[i] & keep[i+1] == 0 for every adjacent pair
3) The number of operations used at position i is:
      popcount(nums[i]) - popcount(keep[i])
   Therefore minimizing removed bits is the same as maximizing total kept bits.

This becomes a dynamic programming problem over masks:
- dp[mask] = maximum total number of kept bits up to the previous position,
             when the previous kept mask is exactly "mask".

Transition:
For current original value x = nums[i], choose a kept submask s of x.
It is valid after previous mask p only if (p & s) == 0.
Then:
    newDp[s] = max(newDp[s], dp[p] + popcount(s))

To make this efficient, for each current submask s we need:
    max dp[p] over all p disjoint with s
That is the same as:
    max dp[p] over all p ⊆ (~s) within 20 bits

This can be answered using SOS DP (subset DP for maxima) on the previous dp array.

Because values are < 2^20, the mask universe size is 2^20 = 1,048,576, which is large but manageable.

*/

using System;

public class Solution
{
    private const int BITS = 20;
    private const int MASK_COUNT = 1 << BITS;
    private const int NEG_INF = int.MinValue / 4;

    /*
    Time Complexity:
    - Let M = 2^20.
    - For each array element:
        1) Build a max-over-subsets table using SOS DP in O(BITS * M)
        2) Enumerate all submasks of nums[i] in O(number of submasks of nums[i]))
    - Worst-case total:
        O(n * BITS * 2^20 + sum of submask counts)
      Since BITS = 20 is a small constant, this is effectively O(n * 20 * 2^20) in the worst theoretical form.

    Important practical note:
    - The problem statement strongly hints at a DP-over-bitmasks solution because each number has at most 20 bits.
    - This implementation is the clean, correct dynamic programming formulation.

    Space Complexity:
    - O(2^20) for dp arrays and helper arrays.
    */
    public int MinimumBitToggles(int[] nums)
    {
        // ------------------------------------------------------------
        // Step 1: Precompute popcount for every 20-bit mask.
        //
        // Why?
        // We repeatedly need popcount(mask), i.e. how many 1-bits are in a mask.
        // Since the mask range is fixed and not too large (2^20), precomputing once
        // makes later transitions much simpler and faster.
        // ------------------------------------------------------------
        int[] bitCount = new int[MASK_COUNT];
        for (int mask = 1; mask < MASK_COUNT; mask++)
        {
            bitCount[mask] = bitCount[mask >> 1] + (mask & 1);
        }

        // ------------------------------------------------------------
        // Step 2: dpPrev[mask] means:
        // maximum total number of kept bits for processed positions so far,
        // where the kept mask at the LAST processed position is exactly "mask".
        //
        // We initialize everything to negative infinity because those states
        // are initially unreachable.
        // ------------------------------------------------------------
        int[] dpPrev = new int[MASK_COUNT];
        Array.Fill(dpPrev, NEG_INF);

        // ------------------------------------------------------------
        // Step 3: Initialize using the first number.
        //
        // For the first position, there is no left neighbor, so ANY submask of nums[0]
        // is allowed as the kept mask.
        //
        // If we keep submask s, then we keep bitCount[s] bits.
        // ------------------------------------------------------------
        int first = nums[0];
        for (int sub = first; ; sub = (sub - 1) & first)
        {
            dpPrev[sub] = bitCount[sub];
            if (sub == 0) break;
        }

        // ------------------------------------------------------------
        // Step 4: Process positions 1..n-1 one by one.
        //
        // For each current number x:
        //   - We want to choose a submask s of x.
        //   - It must be disjoint from the previous kept mask p:
        //         (p & s) == 0
        //   - Among all such p, we want the one with maximum dpPrev[p].
        //
        // To answer "best previous p disjoint with s" efficiently, we build
        // a helper array bestSubset where:
        //     bestSubset[t] = max dpPrev[p] over all p ⊆ t
        //
        // Then for a chosen current submask s, all valid previous masks p must be
        // subsets of complement(s) within 20 bits.
        // So the best previous value is:
        //     bestSubset[fullMask ^ s]
        // ------------------------------------------------------------
        int fullMask = MASK_COUNT - 1;
        int[] bestSubset = new int[MASK_COUNT];
        int[] dpCurr = new int[MASK_COUNT];

        for (int i = 1; i < nums.Length; i++)
        {
            int x = nums[i];

            // --------------------------------------------------------
            // Step 4a: Copy dpPrev into bestSubset.
            //
            // We will transform bestSubset so that after SOS DP:
            // bestSubset[mask] = maximum dpPrev[sub] for all sub ⊆ mask
            // --------------------------------------------------------
            Array.Copy(dpPrev, bestSubset, MASK_COUNT);

            // --------------------------------------------------------
            // Step 4b: SOS DP for maximum over subsets.
            //
            // Standard subset DP idea:
            // For each bit, if that bit is present in mask, then any subset of
            // mask may either use that bit or not use it.
            //
            // Transition:
            //   bestSubset[mask] = max(bestSubset[mask], bestSubset[mask without bit])
            //
            // After processing all bits, bestSubset[mask] stores the maximum value
            // among all subset states of mask.
            // --------------------------------------------------------
            for (int bit = 0; bit < BITS; bit++)
            {
                int bitValue = 1 << bit;
                for (int mask = 0; mask < MASK_COUNT; mask++)
                {
                    if ((mask & bitValue) != 0)
                    {
                        int withoutBit = mask ^ bitValue;
                        if (bestSubset[withoutBit] > bestSubset[mask])
                        {
                            bestSubset[mask] = bestSubset[withoutBit];
                        }
                    }
                }
            }

            // --------------------------------------------------------
            // Step 4c: Reset current dp layer to unreachable.
            // --------------------------------------------------------
            Array.Fill(dpCurr, NEG_INF);

            // --------------------------------------------------------
            // Step 4d: Enumerate every submask s of x.
            //
            // Why only submasks?
            // Because we may only turn bits off, never on.
            //
            // For each candidate kept mask s:
            //   allowed previous masks p must satisfy p & s == 0
            //   equivalently p ⊆ complement(s)
            //
            // So:
            //   bestPrev = bestSubset[fullMask ^ s]
            //
            // If bestPrev is reachable, then:
            //   dpCurr[s] = bestPrev + bitCount[s]
            // --------------------------------------------------------
            for (int sub = x; ; sub = (sub - 1) & x)
            {
                int allowedMaskForPrevious = fullMask ^ sub;
                int bestPrev = bestSubset[allowedMaskForPrevious];

                if (bestPrev != NEG_INF)
                {
                    dpCurr[sub] = bestPrev + bitCount[sub];
                }

                if (sub == 0) break;
            }

            // --------------------------------------------------------
            // Step 4e: Move current layer into previous layer for next iteration.
            // We swap references instead of copying to save time.
            // --------------------------------------------------------
            var temp = dpPrev;
            dpPrev = dpCurr;
            dpCurr = temp;
        }

        // ------------------------------------------------------------
        // Step 5: Find the maximum total number of kept bits among all valid
        // final masks at the last position.
        // ------------------------------------------------------------
        int maxKeptBits = 0;
        for (int mask = 0; mask < MASK_COUNT; mask++)
        {
            if (dpPrev[mask] > maxKeptBits)
            {
                maxKeptBits = dpPrev[mask];
            }
        }

        // ------------------------------------------------------------
        // Step 6: Total operations = total original set bits - total kept bits.
        //
        // Every removed bit costs exactly one operation.
        // ------------------------------------------------------------
        int totalOriginalBits = 0;
        foreach (int num in nums)
        {
            totalOriginalBits += bitCount[num];
        }

        return totalOriginalBits - maxKeptBits;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// nums = [3, 6, 5]
// 3 = 011, 6 = 110, 5 = 101
// Expected output: 2
int[] nums1 = { 3, 6, 5 };
int result1 = solution.MinimumBitToggles(nums1);
Console.WriteLine(result1);

// Example 2:
// nums = [7, 7]
// 7 = 111, 7 = 111
// Expected output: 3
int[] nums2 = { 7, 7 };
int result2 = solution.MinimumBitToggles(nums2);
Console.WriteLine(result2);

// Additional quick sanity checks
int[] nums3 = { 0 };
Console.WriteLine(solution.MinimumBitToggles(nums3)); // Expected 0

int[] nums4 = { 1, 2, 4, 8 };
Console.WriteLine(solution.MinimumBitToggles(nums4)); // Expected 0 because all adjacent pairs are already disjoint

int[] nums5 = { 1, 1, 1 };
Console.WriteLine(solution.MinimumBitToggles(nums5)); // Expected 1: keep pattern [1,0,1] or [0,1,0] with one removal total