/*
Title: Count Servers With Pairwise-Unique Capability Masks
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
You are given an integer array masks where masks[i] represents the enabled capabilities of the i-th server as a bitmask.
Two servers are considered compatible for a special deployment if they do not share any enabled capability bit.
In other words, for servers i and j, they are compatible if (masks[i] & masks[j]) == 0.

Return the number of unordered pairs of distinct servers that are compatible.

This problem is designed for situations where each server has a small fixed set of possible capability bits,
but the number of servers can be large. A brute-force O(n^2) comparison over all pairs may be too slow.
You should take advantage of the bitmask structure to count valid pairs efficiently.

The answer can be large, so return it as a 64-bit integer.

Constraints:
- 1 <= masks.length <= 200000
- 0 <= masks[i] < 2^20
- Capability bits are numbered from 0 to 19
- Multiple servers may have the same mask value

Example 1:
Input: masks = [1, 2, 3, 4]
Output: 4

Explanation:
The compatible pairs are:
- (1, 2) because 001 & 010 = 0
- (1, 4) because 001 & 100 = 0
- (2, 4) because 010 & 100 = 0
- (3, 4) because 011 & 100 = 0
So the answer is 4.

Example 2:
Input: masks = [0, 1, 1, 2, 6]
Output: 6

Explanation:
A mask of 0 is compatible with every other mask. The valid unordered pairs are:
- (0, 1) using the first 1
- (0, 1) using the second 1
- (0, 2)
- (0, 6)
- (1, 2) using the first 1
- (1, 2) using the second 1
No other pair has bitwise AND equal to 0.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Building the frequency array: O(n)
    - SOS DP (Sum Over Subsets dynamic programming): O(B * 2^B), where B = 20
    - Final counting pass over all possible masks: O(2^B)
    Overall: O(n + B * 2^B)

    Since B = 20 is fixed, this is efficient enough for n up to 200000.

    Space Complexity:
    - O(2^B) for the frequency array
    - O(2^B) for the subset-sum helper array
    Overall: O(2^B)
    */
    public long CountCompatiblePairs(int[] masks)
    {
        // There are exactly 20 possible capability bits: 0 through 19.
        // That means every mask value is in the range [0, 2^20 - 1].
        const int bitCount = 20;

        // Total number of distinct mask values possible.
        // Example: if bitCount = 3, masks range from 0 to 7.
        int size = 1 << bitCount;

        // This mask has all 20 bits set to 1.
        // We will use it to compute complements inside the 20-bit universe.
        int fullMask = size - 1;

        // freq[x] = how many servers have exactly mask x.
        // We use a long[] instead of int[] to be extra safe during later arithmetic.
        long[] freq = new long[size];

        // Step 1: Count how many times each exact mask appears.
        // Why this is necessary:
        // The input can contain many duplicate masks, and counting by frequency lets us
        // process all identical servers together instead of one by one.
        foreach (int mask in masks)
        {
            freq[mask]++;
        }

        // Step 2: Prepare an array for SOS DP.
        //
        // subsetCount[s] will eventually mean:
        // "How many servers have a mask that is a subset of s?"
        //
        // Why is that useful?
        // For a server with mask m, another server is compatible if it uses only bits
        // that are NOT present in m.
        //
        // Let allowed = complement of m within 20 bits.
        // Then any compatible partner must have a mask that is a subset of allowed.
        //
        // So if we can quickly answer:
        // "How many input masks are subsets of allowed?"
        // then we can count compatible partners efficiently.
        long[] subsetCount = new long[size];

        // Initially copy exact frequencies.
        // Before DP, subsetCount[s] only knows about masks exactly equal to s.
        Array.Copy(freq, subsetCount, size);

        // Step 3: Run SOS DP (Sum Over Subsets DP).
        //
        // Goal:
        // Transform subsetCount so that after processing,
        // subsetCount[s] = sum of freq[t] for all t where t is a subset of s.
        //
        // How it works:
        // We iterate over each bit position.
        // For every mask s that has the current bit set,
        // we add the count from s with that bit removed.
        //
        // This gradually accumulates counts from all subsets.
        //
        // Example idea:
        // If s = 10110, then subsets of s can be formed by optionally removing any set bit.
        // The DP systematically aggregates those possibilities.
        for (int bit = 0; bit < bitCount; bit++)
        {
            for (int mask = 0; mask < size; mask++)
            {
                // We only do work when the current bit is present in mask.
                // Then mask ^ (1 << bit) is the same mask with that bit turned off,
                // which is one of its smaller subsets.
                if ((mask & (1 << bit)) != 0)
                {
                    subsetCount[mask] += subsetCount[mask ^ (1 << bit)];
                }
            }
        }

        // Step 4: Count ordered compatible pairs using the precomputed subset sums.
        //
        // For each distinct mask m that appears freq[m] times:
        // - The set of bits a compatible partner may use is exactly the complement of m.
        // - Therefore, the number of servers compatible with one server of mask m is:
        //       subsetCount[fullMask ^ m]
        //   because that counts all masks that are subsets of the complement.
        //
        // If freq[m] > 0, then all servers with mask m contribute:
        //       freq[m] * subsetCount[fullMask ^ m]
        // ordered pairs.
        //
        // Important subtlety:
        // This counts ordered pairs (a, b), not unordered pairs {a, b}.
        // So every valid pair of distinct servers is counted twice:
        // once from the perspective of the first server,
        // and once from the perspective of the second server.
        //
        // Also, what about pairing a server with itself?
        // That can only happen when mask m is compatible with itself,
        // which requires (m & m) == 0, meaning m == 0.
        //
        // For mask 0:
        // - subsetCount[fullMask] includes all servers, including the same zero-mask server itself.
        // - So each zero-mask server contributes one invalid self-pair.
        // - There are freq[0] such self-pairs in total.
        //
        // We will subtract those invalid self-pairs, then divide by 2.
        long orderedPairsIncludingZeroSelfPairs = 0;

        for (int mask = 0; mask < size; mask++)
        {
            if (freq[mask] == 0)
            {
                // Skip masks that do not appear in the input.
                continue;
            }

            // Compute the 20-bit complement of the current mask.
            // These are exactly the bits a compatible partner is allowed to use.
            int complement = fullMask ^ mask;

            // subsetCount[complement] tells us how many input masks are subsets of complement,
            // which is exactly the number of compatible partners for one server with this mask.
            orderedPairsIncludingZeroSelfPairs += freq[mask] * subsetCount[complement];
        }

        // Step 5: Remove invalid self-pairs.
        //
        // Only mask 0 can be compatible with itself.
        // If there are z zero-mask servers, the ordered counting above includes exactly z
        // self-pairs of the form (server, same server), which are not allowed because
        // the problem asks for pairs of distinct servers.
        long zeroSelfPairs = freq[0];

        long orderedPairsDistinctOnly = orderedPairsIncludingZeroSelfPairs - zeroSelfPairs;

        // Step 6: Convert ordered pairs to unordered pairs.
        //
        // Every valid unordered pair {i, j} with i != j appears exactly twice in ordered form:
        // - once as (i, j)
        // - once as (j, i)
        //
        // Therefore, divide by 2.
        long unorderedPairs = orderedPairsDistinctOnly / 2;

        return unorderedPairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// masks = [1, 2, 3, 4]
// Compatible unordered pairs:
// (1,2), (1,4), (2,4), (3,4) => 4
int[] masks1 = { 1, 2, 3, 4 };
long result1 = solution.CountCompatiblePairs(masks1);
Console.WriteLine(result1);

// Example 2:
// masks = [0, 1, 1, 2, 6]
// Valid unordered pairs:
// (0,1 first), (0,1 second), (0,2), (0,6), (1 first,2), (1 second,2) => 6
int[] masks2 = { 0, 1, 1, 2, 6 };
long result2 = solution.CountCompatiblePairs(masks2);
Console.WriteLine(result2);

// Additional quick sanity check:
// All zero masks: every pair is compatible.
// For 3 servers, answer should be 3 choose 2 = 3.
int[] masks3 = { 0, 0, 0 };
long result3 = solution.CountCompatiblePairs(masks3);
Console.WriteLine(result3);