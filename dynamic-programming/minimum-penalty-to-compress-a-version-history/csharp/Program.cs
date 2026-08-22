/*
Title: Minimum Penalty to Compress a Version History

Problem Description:
A software team stores the sizes of consecutive document revisions in an array `sizes`, where `sizes[i]` is the size of the `i`-th saved version. To reduce storage, the team wants to compress the full version history into exactly `k` archive blocks. Each archive block must contain a contiguous range of versions.

If versions from index `l` to `r` are placed into one archive block, then all versions in that block are represented using the largest version size inside the block. The storage penalty of that block is defined as:

penalty(l, r) = (max(sizes[l..r]) * (r - l + 1)) - sum(sizes[l..r])

In other words, every version in the block is padded up to the maximum size in that block, and the penalty is the total extra space introduced by that padding.

Your task is to return the minimum total penalty needed to partition the entire array into exactly `k` contiguous archive blocks.

If `k > n`, where `n = sizes.length`, then it is impossible to create exactly `k` non-empty blocks.

Return the minimum total penalty, or `-1` if the partition is impossible.

Constraints:
- 1 <= n <= 400
- 1 <= sizes[i] <= 10^9
- 1 <= k <= 400
- Each archive block must be non-empty
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing interval penalties: O(n^2)
    - Dynamic programming transitions: O(k * n^2)
    - Total: O(n^2 + k * n^2) = O(k * n^2)

    Space Complexity:
    - Interval penalty table: O(n^2)
    - DP table: O(k * n)
    - Total: O(n^2 + k * n)

    Why this is efficient enough:
    - n <= 400, k <= 400
    - O(k * n^2) is acceptable for these constraints
    */
    public long MinimumPenalty(int[] sizes, int k)
    {
        int n = sizes.Length;

        // If we need more non-empty blocks than there are elements,
        // it is impossible because every block must contain at least one version.
        if (k > n)
        {
            return -1;
        }

        // ------------------------------------------------------------
        // STEP 1: Precompute the penalty for every interval [l..r]
        // ------------------------------------------------------------
        //
        // Why do this?
        // During dynamic programming, we will repeatedly ask:
        // "What is the penalty if the last block starts at index x and ends at index y?"
        //
        // If we compute that penalty from scratch every time, the DP becomes too slow.
        // So we precompute all interval penalties once, store them in a table,
        // and then each DP transition can read the answer in O(1) time.
        //
        // cost[l, r] = penalty of putting sizes[l..r] into one block
        //
        // We compute each row by expanding the right endpoint:
        // - keep track of the current maximum in the interval
        // - keep track of the current sum in the interval
        // Then:
        // penalty = max * length - sum
        //
        // We use long because:
        // - sizes[i] can be up to 1e9
        // - n can be up to 400
        // - max * length can exceed int range
        long[,] cost = new long[n, n];

        for (int l = 0; l < n; l++)
        {
            long currentMax = 0;
            long currentSum = 0;

            for (int r = l; r < n; r++)
            {
                // Expand interval [l..r] by including sizes[r].
                currentMax = Math.Max(currentMax, sizes[r]);
                currentSum += sizes[r];

                int length = r - l + 1;
                cost[l, r] = currentMax * length - currentSum;
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Build the DP table
        // ------------------------------------------------------------
        //
        // DP definition:
        // dp[blocks, i] = minimum penalty to partition the first i elements
        //                 (that means indices [0..i-1]) into exactly "blocks" blocks.
        //
        // Important indexing note:
        // - i is a prefix length, not an array index.
        // - So:
        //   * i = 0 means "no elements"
        //   * i = n means "all elements"
        //
        // Transition idea:
        // Suppose the last block starts at position j and ends at i-1.
        // Then:
        // - the first j elements must be partitioned into blocks - 1 blocks
        // - the last block contributes cost[j, i-1]
        //
        // Therefore:
        // dp[blocks, i] = min over j from blocks-1 to i-1 of
        //                 dp[blocks - 1, j] + cost[j, i - 1]
        //
        // Why does j start at blocks - 1?
        // Because to split the first j elements into blocks - 1 non-empty blocks,
        // we need at least blocks - 1 elements.
        //
        // We initialize all states to a very large number (INF),
        // meaning "currently unreachable / not yet improved".
        long INF = long.MaxValue / 4;
        long[,] dp = new long[k + 1, n + 1];

        for (int blocks = 0; blocks <= k; blocks++)
        {
            for (int i = 0; i <= n; i++)
            {
                dp[blocks, i] = INF;
            }
        }

        // Base case:
        // Partitioning 0 elements into 0 blocks costs 0.
        dp[0, 0] = 0;

        // ------------------------------------------------------------
        // STEP 3: Fill the DP table block by block
        // ------------------------------------------------------------
        //
        // We compute answers for:
        // - 1 block
        // - 2 blocks
        // - ...
        // - k blocks
        //
        // For each number of blocks, we compute all valid prefix lengths.
        for (int blocks = 1; blocks <= k; blocks++)
        {
            // To split i elements into exactly "blocks" non-empty blocks:
            // - minimum i is "blocks" (each block gets one element)
            // - maximum i is n
            for (int i = blocks; i <= n; i++)
            {
                // Try every possible starting point j of the last block.
                // The last block is [j..i-1].
                //
                // j must be at least blocks - 1 so that the first j elements
                // can form blocks - 1 non-empty blocks.
                for (int j = blocks - 1; j <= i - 1; j++)
                {
                    // If the previous state is unreachable, skip it.
                    if (dp[blocks - 1, j] == INF)
                    {
                        continue;
                    }

                    long candidate = dp[blocks - 1, j] + cost[j, i - 1];

                    // Keep the best (minimum) total penalty.
                    if (candidate < dp[blocks, i])
                    {
                        dp[blocks, i] = candidate;
                    }
                }
            }
        }

        // The answer is the minimum penalty to partition all n elements into exactly k blocks.
        return dp[k, n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt
int[] sizes1 = { 5, 2, 4, 6, 3 };
int k1 = 2;
long result1 = solution.MinimumPenalty(sizes1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2 from the prompt
int[] sizes2 = { 8, 8, 8, 8 };
int k2 = 3;
long result2 = solution.MinimumPenalty(sizes2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo: impossible case
int[] sizes3 = { 10, 20 };
int k3 = 3;
long result3 = solution.MinimumPenalty(sizes3, k3);
Console.WriteLine($"Impossible Case Result: {result3}");

// Additional demo: each element in its own block => zero penalty
int[] sizes4 = { 7, 1, 9, 2 };
int k4 = 4;
long result4 = solution.MinimumPenalty(sizes4, k4);
Console.WriteLine($"Each Element Separate Result: {result4}");