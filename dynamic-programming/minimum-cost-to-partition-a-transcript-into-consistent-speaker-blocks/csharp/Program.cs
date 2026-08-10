/*
Title: Minimum Cost to Partition a Transcript into Consistent Speaker Blocks
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
You are given a transcript of a meeting represented by an array labels of length n,
where labels[i] is the speaker ID of the i-th utterance.

You want to split the transcript into one or more contiguous blocks.
For each block, you assign exactly one speaker as the block's "owner".

Inside a block:
- Every utterance spoken by the owner costs 0
- Every utterance spoken by a different speaker costs 1

Additionally, every block incurs a fixed overhead cost called overhead.

So, for a block from index l to r:
cost(block) = overhead + (block length - maximum frequency of any speaker in that block)

The task is to compute the minimum total cost to partition the entire transcript.

Key insight:
For any chosen block, the best owner is simply the speaker that appears most often in that block.
So the only real decision is where to place the cuts between blocks.

Constraints:
- 1 <= n <= 5000
- 1 <= labels[i] <= 5000
- 0 <= overhead <= 10^9
- The answer fits in a 64-bit signed integer

Examples:
1) labels = [1, 2, 1, 1, 3], overhead = 2
   Whole array as one block:
   max frequency = 3 (speaker 1), length = 5
   cost = 2 + (5 - 3) = 4
   Answer = 4

2) labels = [4, 4, 2, 2, 2, 4, 4], overhead = 1
   Partition [4,4] | [2,2,2] | [4,4]
   Each block has all same speaker, so mismatch cost = 0
   Total = 1 + 1 + 1 = 3
   Answer = 3
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^2)
    Space Complexity: O(n + V), where V is the maximum possible label value used for counting.
                      Under the given constraints, V <= 5000, so this is efficient.

    Explanation of the approach:
    --------------------------------
    We use dynamic programming.

    Let dp[i] = minimum cost to partition the first i utterances
                (that is, labels[0..i-1]).

    Then:
        dp[0] = 0   // no utterances, no cost

    For every ending position i, we try every possible starting position j
    for the last block:
        block = labels[j..i-1]

    If we know:
        length of block = i - j
        max frequency of any speaker inside that block = maxFreq

    then:
        cost of this block = overhead + (length - maxFreq)

    So:
        dp[i] = min over all j from 0 to i-1 of
                dp[j] + overhead + ((i - j) - maxFreq(labels[j..i-1]))

    The challenge is computing maxFreq efficiently while trying all j.
    We do this by fixing i and expanding the block backwards:
    - Start with j = i-1
    - Then j = i-2
    - Then j = i-3
    - ...
    While expanding, we maintain frequency counts of labels in the current block,
    and also maintain the current maximum frequency.

    Because n <= 5000, O(n^2) is acceptable.
    */
    public long MinimumPartitionCost(int[] labels, long overhead)
    {
        int n = labels.Length;

        // dp[i] means:
        // minimum total cost to partition the prefix labels[0..i-1]
        long[] dp = new long[n + 1];

        // We initialize all states to a very large value,
        // because we are going to minimize over many choices.
        long inf = long.MaxValue / 4;
        for (int i = 0; i <= n; i++)
        {
            dp[i] = inf;
        }

        // Base case:
        // Empty prefix costs 0 because there is nothing to partition.
        dp[0] = 0;

        // The labels are guaranteed to be between 1 and 5000.
        // We use a counting array for frequencies inside the current block.
        // This is faster than a dictionary and very simple here.
        int maxLabelValue = 5000;

        // We compute dp[1], dp[2], ..., dp[n]
        for (int i = 1; i <= n; i++)
        {
            // freq[x] = how many times speaker x appears in the current block
            // while we expand the block backwards ending at i-1.
            int[] freq = new int[maxLabelValue + 1];

            // maxFreq = maximum frequency of any speaker in the current block
            int maxFreq = 0;

            // We now try every possible starting point j for the last block.
            // The last block will be labels[j..i-1].
            //
            // We iterate backwards so we can update frequencies incrementally:
            // - first block is just [i-1..i-1]
            // - then [i-2..i-1]
            // - then [i-3..i-1]
            // and so on.
            for (int j = i - 1; j >= 0; j--)
            {
                int speaker = labels[j];

                // Add labels[j] into the current block's frequency table.
                freq[speaker]++;

                // If this speaker now appears more often than any previous speaker
                // in the current block, update maxFreq.
                if (freq[speaker] > maxFreq)
                {
                    maxFreq = freq[speaker];
                }

                // Current block length is from j to i-1 inclusive.
                int blockLength = i - j;

                // Mismatch cost inside this block:
                // all utterances except those belonging to the most frequent speaker
                long mismatchCost = blockLength - maxFreq;

                // Total cost if we cut before j and make labels[j..i-1] the last block:
                long candidate = dp[j] + overhead + mismatchCost;

                // Keep the best possible partition cost for the first i elements.
                if (candidate < dp[i])
                {
                    dp[i] = candidate;
                }
            }
        }

        // dp[n] is the minimum cost for the entire array.
        return dp[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] labels1 = { 1, 2, 1, 1, 3 };
long overhead1 = 2;
long result1 = solution.MinimumPartitionCost(labels1, overhead1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] labels2 = { 4, 4, 2, 2, 2, 4, 4 };
long overhead2 = 1;
long result2 = solution.MinimumPartitionCost(labels2, overhead2);
Console.WriteLine(result2); // Expected: 3

// Additional small sanity checks

// Single utterance: one block, no mismatch, only overhead
int[] labels3 = { 7 };
long overhead3 = 5;
long result3 = solution.MinimumPartitionCost(labels3, overhead3);
Console.WriteLine(result3); // Expected: 5

// All same speaker: best is often one block if overhead > 0
int[] labels4 = { 2, 2, 2, 2 };
long overhead4 = 3;
long result4 = solution.MinimumPartitionCost(labels4, overhead4);
Console.WriteLine(result4); // Expected: 3

// Alternating speakers with zero overhead:
// Can split into singletons, each cost 0, so total should be 0
int[] labels5 = { 1, 2, 1, 2 };
long overhead5 = 0;
long result5 = solution.MinimumPartitionCost(labels5, overhead5);
Console.WriteLine(result5); // Expected: 0