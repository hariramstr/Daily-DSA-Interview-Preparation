/*
Title: Minimum Cost to Compress a Melody with Repeated Motifs
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A digital music editor stores a melody as an array of integers, where each integer represents a note pitch.
To reduce storage, the editor may encode the melody as a sequence of blocks. A block can be stored in one of two ways:

1. Raw block: store every note directly. A raw block covering notes i..j costs (j - i + 1).
2. Motif block: if the subarray notes[i..j] is made of one smaller pattern repeated consecutively one or more times,
   it may be stored as: cost(pattern) + repeatPenalty, where repeatPenalty is a fixed integer P, and cost(pattern)
   is the minimum compressed cost of that smaller pattern.

You may recursively compress the pattern itself, and you may partition the melody into any number of blocks.
Your task is to compute the minimum total cost to encode the entire melody.

Formally, for any subarray notes[i..j], you may either keep it raw, split it into two non-empty consecutive parts,
or encode it as repeated copies of a shorter subarray whose length divides (j - i + 1). A repeated motif block is valid
only if every copy is exactly identical.

Return the minimum encoding cost for the full array.

Constraints:
- 1 <= n <= 200
- 1 <= notes[i] <= 10^9
- 1 <= P <= 200
- Time complexity better than O(n^4 * n) is expected for a full solution

Example 1:
Input: notes = [4, 7, 4, 7, 4, 7], P = 2
Output: 4

Example 2:
Input: notes = [5, 5, 5, 8, 5, 5, 5, 8], P = 3
Output: 7
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the longest-common-prefix table for all suffix pairs: O(n^2)
    - Interval DP over all subarrays:
        * There are O(n^2) intervals.
        * For each interval, we try all split points: O(n)
        * For each interval, we also try all divisors / candidate pattern lengths: O(n) in the worst case
      Total: O(n^3)

    Space Complexity:
    - LCP table: O(n^2)
    - DP table: O(n^2)
    Total: O(n^2)

    Why this is efficient enough:
    - n <= 200, so O(n^3) is very safe.
    - We avoid expensive element-by-element repeated-pattern checking by using an LCP table.
    */
    public int MinimumCompressionCost(int[] notes, int repeatPenalty)
    {
        int n = notes.Length;

        // ------------------------------------------------------------
        // Step 1: Build an LCP (Longest Common Prefix) table for suffixes.
        //
        // lcp[i, j] = length of the longest equal prefix between:
        //   notes[i..]
        // and
        //   notes[j..]
        //
        // Example:
        // if notes = [4,7,4,7,4,7]
        // then lcp[0,2] = 4 because:
        //   notes[0..] = [4,7,4,7,4,7]
        //   notes[2..] = [4,7,4,7]
        // and the first 4 elements match.
        //
        // Why do we need this?
        // Because when we want to test whether a subarray is made of repeated copies
        // of a smaller pattern of length d, we need to compare adjacent blocks quickly.
        // With LCP, we can check equality of two length-d blocks in O(1):
        //   blocks are equal iff lcp[start1, start2] >= d
        //
        // How do we compute it?
        // Standard reverse DP:
        // if notes[i] == notes[j], then
        //   lcp[i, j] = 1 + lcp[i+1, j+1]
        // else
        //   lcp[i, j] = 0
        //
        // We fill from bottom-right toward top-left so that lcp[i+1, j+1]
        // is already known when computing lcp[i, j].
        // ------------------------------------------------------------
        int[,] lcp = new int[n + 1, n + 1];

        for (int i = n - 1; i >= 0; i--)
        {
            for (int j = n - 1; j >= 0; j--)
            {
                if (notes[i] == notes[j])
                {
                    lcp[i, j] = 1 + lcp[i + 1, j + 1];
                }
                else
                {
                    lcp[i, j] = 0;
                }
            }
        }

        // ------------------------------------------------------------
        // Step 2: Prepare the interval DP table.
        //
        // dp[i, j] = minimum cost to compress the subarray notes[i..j]
        //
        // This is a classic interval DP:
        // - Base/raw option: keep the whole interval raw, cost = length
        // - Split option: split at every possible k, cost = dp[i, k] + dp[k+1, j]
        // - Repeated motif option:
        //     if notes[i..j] = repeated copies of notes[i..i+d-1]
        //     then cost = dp[i, i+d-1] + repeatPenalty
        //
        // Important detail:
        // We compute intervals in increasing order of length.
        // That ensures when we need dp for smaller intervals, it is already computed.
        // ------------------------------------------------------------
        int[,] dp = new int[n, n];

        for (int len = 1; len <= n; len++)
        {
            for (int i = 0; i + len - 1 < n; i++)
            {
                int j = i + len - 1;

                // ----------------------------------------------------
                // Step 2a: Start with the raw cost.
                //
                // If we do not compress this interval at all,
                // we simply store each note directly.
                // Cost = number of notes in the interval = len.
                // ----------------------------------------------------
                int best = len;

                // ----------------------------------------------------
                // Step 2b: Try splitting the interval into two parts.
                //
                // For every split point k:
                //   left  = notes[i..k]
                //   right = notes[k+1..j]
                //
                // Since the problem allows partitioning into any number of blocks,
                // every optimal solution can be represented by recursively splitting.
                //
                // So we try all possible k and take the minimum:
                //   dp[i, j] = min(dp[i, k] + dp[k+1, j])
                // ----------------------------------------------------
                for (int k = i; k < j; k++)
                {
                    int candidate = dp[i, k] + dp[k + 1, j];
                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                // ----------------------------------------------------
                // Step 2c: Try encoding the whole interval as a repeated motif.
                //
                // Suppose current interval length is len.
                // We need to find a smaller pattern length d such that:
                //   1) d < len
                //   2) d divides len
                //   3) notes[i..j] consists of len/d identical consecutive blocks,
                //      each of length d
                //
                // If valid, then:
                //   cost = dp[i, i+d-1] + repeatPenalty
                //
                // Notice:
                // We use dp[i, i+d-1], not just d,
                // because the pattern itself may also be compressible.
                //
                // Example:
                // If the pattern is itself repeated, recursive compression is allowed.
                // ----------------------------------------------------
                for (int d = 1; d < len; d++)
                {
                    // A repeated motif must use a pattern length that divides the total length.
                    if (len % d != 0)
                    {
                        continue;
                    }

                    // ------------------------------------------------
                    // We now verify whether every block of length d
                    // is identical to the first block.
                    //
                    // Blocks:
                    //   block 0 starts at i
                    //   block 1 starts at i + d
                    //   block 2 starts at i + 2d
                    //   ...
                    //
                    // To confirm repetition, it is enough to check that
                    // each adjacent pair of blocks is equal.
                    //
                    // Equality of two length-d blocks starting at a and b:
                    //   lcp[a, b] >= d
                    //
                    // If any adjacent pair fails, this d is not a valid motif length.
                    // ------------------------------------------------
                    bool isRepeated = true;

                    for (int start = i + d; start <= j; start += d)
                    {
                        if (lcp[i, start] < d)
                        {
                            isRepeated = false;
                            break;
                        }
                    }

                    if (!isRepeated)
                    {
                        continue;
                    }

                    // ------------------------------------------------
                    // Valid repeated motif found.
                    //
                    // The whole interval can be represented as:
                    //   compressed(pattern) + repeatPenalty
                    //
                    // The pattern is notes[i..i+d-1].
                    // Since d < len, its dp value has already been computed.
                    // ------------------------------------------------
                    int repeatedCost = dp[i, i + d - 1] + repeatPenalty;
                    if (repeatedCost < best)
                    {
                        best = repeatedCost;
                    }
                }

                // Store the best answer for this interval.
                dp[i, j] = best;
            }
        }

        // The answer for the full array is the interval covering everything.
        return dp[0, n - 1];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Example 1:
// notes = [4, 7, 4, 7, 4, 7], P = 2
// Whole array is 3 repeats of [4, 7].
// Pattern [4, 7] raw cost = 2
// Repeated motif cost = 2 + 2 = 4
// Expected output: 4
var solution = new Solution();

int[] notes1 = { 4, 7, 4, 7, 4, 7 };
int p1 = 2;
int result1 = solution.MinimumCompressionCost(notes1, p1);
Console.WriteLine(result1);

// Example 2:
// notes = [5, 5, 5, 8, 5, 5, 5, 8], P = 3
// Whole array is 2 repeats of [5, 5, 5, 8].
// Pattern raw cost = 4
// Repeated motif cost = 4 + 3 = 7
// Expected output: 7
int[] notes2 = { 5, 5, 5, 8, 5, 5, 5, 8 };
int p2 = 3;
int result2 = solution.MinimumCompressionCost(notes2, p2);
Console.WriteLine(result2);