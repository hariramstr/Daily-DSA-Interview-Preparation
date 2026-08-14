/*
Title: Minimum Reorders to Group Equal Package Colors

Problem Description:
A warehouse conveyor outputs packages as an array `colors`, where `colors[i]` is the color code of the `i`th package.
The same color may appear many times in different positions. The sorting machine wants all packages of the same color
to appear in one contiguous block, but the relative order of those color blocks does not matter.

In one operation, you may remove a single package from its current position and insert it at any position in the array.
Return the minimum number of such operations needed so that, in the final array, every distinct color appears in exactly
one contiguous segment.

You are not asked to output the final arrangement, only the minimum number of moves.

Key idea:
A package can stay in place if it belongs to some subsequence that already matches a valid final arrangement:
that final arrangement consists of color blocks, and each color appears in exactly one block.
So we want to keep the largest possible subsequence of the original array that can be written as:
all occurrences of color A that we keep, then all occurrences of color B that we keep, then all occurrences of color C, ...
with no color reappearing after we move on to another chosen color.

A crucial observation:
For any color, in an optimal kept subsequence, we either:
- keep a contiguous run of that color from the original array's "color-only projection" between two neighboring chosen colors, or
- more simply in the compressed block representation, we choose a sequence of color blocks where each chosen color appears in one contiguous interval.

This becomes equivalent to:
Compress the array into consecutive color blocks.
Then find the maximum total number of elements we can keep by choosing an order of distinct colors such that
whenever we choose a color, all kept blocks of that color form one contiguous interval in the compressed block list,
and chosen colors appear in nondecreasing order of their first chosen block.

This can be solved with dynamic programming over colors using the intervals of each color in the compressed block array.

A much simpler and correct formulation:
Let the compressed blocks be b[0..m-1], where each block has:
- color c
- length len

For each color c, its occurrences in the compressed array are at positions p1 < p2 < ... < pk.
If we decide to keep color c as one block in the final subsequence, then among these k blocks we may keep any contiguous
subsequence of them: pi..pj. The total kept contribution is the sum of lengths of those blocks.
The chosen intervals for different colors must be disjoint and ordered by block index.

This is weighted interval scheduling over all possible color-intervals in the compressed block array.
Naively there are too many intervals, but for each color we can optimize with prefix sums and DP.

Let dp[t] = maximum kept elements using only compressed block positions < t.
Transition:
For every color c and every occurrence index j of c at compressed position pos[j],
consider ending c's chosen interval at j. Best start i gives:
dp[pos[i]] + (prefixLen[j+1] - prefixLen[i])
where prefixLen is prefix sum over this color's block lengths.
This equals:
prefixLen[j+1] + max over i<=j of (dp[pos[i]] - prefixLen[i])

We process compressed positions from left to right.
When we are at a block of color c that is the j-th occurrence of c:
- we can update the best value for starting/continuing an interval of color c
- then use it to end an interval at this occurrence
- and update dp for the next position

This yields O(m), where m is number of compressed blocks, and m <= n.

Example 1:
colors = [3,1,3,2,1,2]
Compressed blocks are identical, each length 1.
The best keep is 3, so answer = 6 - 3 = 3.

Example 2:
colors = [4,4,2,2,3,3]
Compressed blocks: (4,2), (2,2), (3,2)
Best keep = 6, so answer = 0.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the length of the input array.
    Reason:
    1. We compress the array into consecutive blocks in one pass.
    2. We process each compressed block exactly once in the dynamic programming pass.
    3. All dictionary operations are average O(1).

    Space Complexity:
    O(m + d), where:
    - m is the number of compressed blocks (m <= n)
    - d is the number of distinct colors
    In the worst case this is O(n).

    Beginner-friendly explanation of the method:
    -------------------------------------------
    We do NOT directly simulate moving packages.
    Instead, we ask:
    "What is the largest number of packages we can leave where they already are,
     so that those kept packages already form a valid subsequence of some final grouped arrangement?"

    If we can keep K packages in place, then all other n - K packages must be moved.
    So the answer is:
        minimum moves = n - maximum keepable subsequence length

    The final grouped arrangement consists of color blocks:
        [all of one color][all of another color][all of another color]...

    To reason about this efficiently, we first compress consecutive equal values into blocks.
    Example:
        [4,4,2,2,3,3] -> blocks:
        (4,length 2), (2,length 2), (3,length 2)

    Why compression helps:
    If equal colors are already consecutive, they behave as one unit for "keeping in place".
    This dramatically simplifies the structure.

    Dynamic programming idea:
    -------------------------
    We scan compressed blocks from left to right.

    Let dpBefore mean:
        the best number of elements we can keep using only blocks strictly before the current position.

    For each color c, we maintain a helper value:
        bestStart[c]

    Interpretation of bestStart[c]:
        Suppose we want to build one kept interval for color c in the compressed block list.
        If we start that interval at some earlier occurrence of color c, then:
            total kept = dp before that start + sum of lengths of chosen c-blocks from start to current occurrence
        bestStart[c] stores the best possible value of:
            dp before start - prefixSumOfColorBeforeStart
        so that when we reach a later occurrence, we can instantly compute the best interval ending here.

    This is a standard "transform the formula so each step is O(1)" trick.
    */
    public int MinimumReordersToGroupEqualPackageColors(int[] colors)
    {
        int n = colors.Length;
        if (n <= 1)
        {
            return 0;
        }

        // ------------------------------------------------------------
        // STEP 1: Compress the original array into consecutive blocks.
        //
        // Example:
        //   colors = [3,1,3,2,1,2]
        //   blocks = [(3,1), (1,1), (3,1), (2,1), (1,1), (2,1)]
        //
        // Example:
        //   colors = [4,4,2,2,3,3]
        //   blocks = [(4,2), (2,2), (3,2)]
        //
        // Why this is necessary:
        // If several equal colors are already adjacent, they are naturally part of one
        // contiguous segment and should be treated together.
        // ------------------------------------------------------------
        var blockColors = new List<int>();
        var blockLens = new List<int>();

        int currentColor = colors[0];
        int currentLen = 1;

        for (int i = 1; i < n; i++)
        {
            if (colors[i] == currentColor)
            {
                currentLen++;
            }
            else
            {
                blockColors.Add(currentColor);
                blockLens.Add(currentLen);

                currentColor = colors[i];
                currentLen = 1;
            }
        }

        blockColors.Add(currentColor);
        blockLens.Add(currentLen);

        int m = blockColors.Count;

        // ------------------------------------------------------------
        // STEP 2: Prepare per-color prefix sums over its own block lengths.
        //
        // For each color c, as we encounter its blocks in compressed order,
        // we maintain:
        //   seenWeight[c] = total length of c-blocks seen so far
        //
        // This acts like a prefix sum for that color only.
        //
        // Example for color 3 in blocks [(3,2), (1,1), (3,4)]:
        //   before first 3-block: prefix = 0
        //   after first 3-block : prefix = 2
        //   after second 3-block: prefix = 6
        //
        // We will use this to compute the total kept contribution of choosing
        // a contiguous interval of occurrences of the same color.
        // ------------------------------------------------------------

        // bestStart[color] stores the best value of:
        //   dpBeforeStart - colorPrefixBeforeStart
        //
        // Intuition:
        // If we start keeping color 'c' at some occurrence,
        // then later when we end at current occurrence with colorPrefixAfterCurrent,
        // the total becomes:
        //   colorPrefixAfterCurrent + bestStart[c]
        //
        // This lets us evaluate the best interval ending "here" in O(1).
        var bestStart = new Dictionary<int, int>();

        // seenWeight[color] = total length of this color's blocks processed so far.
        var seenWeight = new Dictionary<int, int>();

        // dpPrefix[pos] conceptually means:
        // best keepable total using blocks [0 .. pos-1]
        //
        // We only need the running current value because we process left to right.
        int dpBeforeCurrentBlock = 0;

        // ------------------------------------------------------------
        // STEP 3: Dynamic programming over compressed blocks.
        //
        // At each block:
        // 1. We know the best answer using all previous blocks: dpBeforeCurrentBlock
        // 2. We consider the current block's color c and length len
        // 3. We may:
        //    - ignore this block, keeping dp unchanged
        //    - or use it as the end of a chosen interval for color c
        //
        // To use it as the end of a chosen interval:
        //   candidate = bestStart[c] + prefixAfterCurrent
        //
        // where:
        //   prefixAfterCurrent = total length of c-blocks seen up to and including this block
        //
        // Before evaluating candidate, we must ensure bestStart[c] includes the option
        // to START a new interval at the current occurrence.
        //
        // If current color-prefix before this block is prefixBefore,
        // then starting here gives:
        //   dpBeforeCurrentBlock - prefixBefore
        //
        // So we update:
        //   bestStart[c] = max(bestStart[c], dpBeforeCurrentBlock - prefixBefore)
        //
        // Then after adding current block length:
        //   prefixAfter = prefixBefore + len
        //   candidate = bestStart[c] + prefixAfter
        //
        // Finally:
        //   dpAfterCurrent = max(dpBeforeCurrentBlock, candidate)
        //
        // and continue.
        // ------------------------------------------------------------
        for (int blockIndex = 0; blockIndex < m; blockIndex++)
        {
            int color = blockColors[blockIndex];
            int len = blockLens[blockIndex];

            // Get how much total length of this color has appeared in earlier blocks.
            // If this is the first block of this color, the prefix before it is 0.
            int prefixBefore = seenWeight.TryGetValue(color, out int existingSeen) ? existingSeen : 0;

            // If we want to START a kept interval for this color at the current block,
            // the transformed value is:
            //   dpBeforeCurrentBlock - prefixBefore
            //
            // Why:
            // Later, when we end the interval at some occurrence with prefixAfter,
            // the kept contribution from this color interval is:
            //   prefixAfter - prefixBefore
            //
            // Adding the best solution before the start:
            //   dpBeforeCurrentBlock + (prefixAfter - prefixBefore)
            // = prefixAfter + (dpBeforeCurrentBlock - prefixBefore)
            //
            // So we store the parenthesized part in bestStart[color].
            int startValue = dpBeforeCurrentBlock - prefixBefore;

            if (bestStart.TryGetValue(color, out int oldBestStart))
            {
                if (startValue > oldBestStart)
                {
                    bestStart[color] = startValue;
                }
            }
            else
            {
                bestStart[color] = startValue;
            }

            // Now include the current block in this color's prefix sum.
            int prefixAfter = prefixBefore + len;
            seenWeight[color] = prefixAfter;

            // End a chosen interval of this color at the current block.
            // This gives one candidate for the best keepable subsequence using blocks up to here.
            int candidateKeep = bestStart[color] + prefixAfter;

            // We may also choose to ignore this block for the optimal subsequence,
            // so the DP after this block is the maximum of:
            // - previous best
            // - best interval ending here
            int dpAfterCurrentBlock = Math.Max(dpBeforeCurrentBlock, candidateKeep);

            // Move forward.
            dpBeforeCurrentBlock = dpAfterCurrentBlock;
        }

        int maxKeepable = dpBeforeCurrentBlock;
        int minMoves = n - maxKeepable;
        return minMoves;
    }
}

// Demo code
var solution = new Solution();

int[] colors1 = { 3, 1, 3, 2, 1, 2 };
int result1 = solution.MinimumReordersToGroupEqualPackageColors(colors1);
Console.WriteLine($"Input: [{string.Join(", ", colors1)}]");
Console.WriteLine($"Minimum moves: {result1}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

int[] colors2 = { 4, 4, 2, 2, 3, 3 };
int result2 = solution.MinimumReordersToGroupEqualPackageColors(colors2);
Console.WriteLine($"Input: [{string.Join(", ", colors2)}]");
Console.WriteLine($"Minimum moves: {result2}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

int[] colors3 = { 2, 1, 2, 1 };
int result3 = solution.MinimumReordersToGroupEqualPackageColors(colors3);
Console.WriteLine($"Input: [{string.Join(", ", colors3)}]");
Console.WriteLine($"Minimum moves: {result3}");
Console.WriteLine("One valid grouped arrangement is [1,1,2,2] or [2,2,1,1]");
Console.WriteLine();

int[] colors4 = { 1 };
int result4 = solution.MinimumReordersToGroupEqualPackageColors(colors4);
Console.WriteLine($"Input: [{string.Join(", ", colors4)}]");
Console.WriteLine($"Minimum moves: {result4}");
Console.WriteLine();

int[] colors5 = { 1, 2, 1, 1, 2, 2, 3, 1, 3 };
int result5 = solution.MinimumReordersToGroupEqualPackageColors(colors5);
Console.WriteLine($"Input: [{string.Join(", ", colors5)}]");
Console.WriteLine($"Minimum moves: {result5}");