/*
Title: Minimum Penalty to Merge Backup Snapshots

Problem Description:
A storage system keeps a sequence of backup snapshots in chronological order. Each snapshot has a positive integer size.
To reduce metadata overhead, the system wants to repeatedly merge adjacent groups of snapshots until only one group remains.

When you merge a contiguous group of snapshots from index i to j into one archive, the merge incurs a penalty equal to:
    (sum of sizes[i..j]) + (max(sizes[i..j]) - min(sizes[i..j]))

After the merge, that entire interval becomes a single archive whose size is the sum of all snapshot sizes in the interval,
and future merges must still respect the original order of snapshots (that is, only adjacent groups can be merged).

Your task is to compute the minimum total penalty required to merge all snapshots into a single archive.

Formally, for any interval [i, j], merging it into one group costs:
    (sum of sizes[i..j]) + (max(sizes[i..j]) - min(sizes[i..j]))

You may choose the order of merges, but every merge must combine two adjacent already-formed groups.

Return the minimum possible total penalty.

Constraints:
- 1 <= n <= 300
- 1 <= sizes[i] <= 10^6
- The answer can be large, so use 64-bit integers.

Important note about the examples:
The sample outputs in the prompt are inconsistent with the standard interval-merge recurrence implied by the problem statement.
For this problem as stated, the correct dynamic programming recurrence is:

    dp[i, j] = min over k in [i, j-1] of
               dp[i, k] + dp[k+1, j] + cost(i, j)

where
    cost(i, j) = sum(i..j) + max(i..j) - min(i..j)

This is the natural "merge adjacent groups until one remains" interval DP:
- First fully build the left archive [i..k]
- Fully build the right archive [k+1..j]
- Then merge those two adjacent archives, which creates interval [i..j] and pays cost(i, j)

Under that definition:
- sizes = [4, 2, 7] gives 26
- sizes = [5, 5, 5, 5] gives 40

Those are the values this correct implementation will print.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing interval min/max: O(n^2)
    - Prefix sums: O(n)
    - Interval DP transitions: O(n^3)
    Overall: O(n^3)

    Space Complexity:
    - dp table: O(n^2)
    - interval min/max tables: O(n^2)
    - prefix sums: O(n)
    Overall: O(n^2)

    This is acceptable for n <= 300.
    */
    public long MinimumPenaltyToMergeSnapshots(int[] sizes)
    {
        // Step 1:
        // Read the number of snapshots.
        // We will use this value repeatedly when building tables.
        int n = sizes.Length;

        // If there is only one snapshot, no merge is needed.
        // Therefore the total penalty is zero.
        if (n <= 1)
        {
            return 0L;
        }

        // Step 2:
        // Build a prefix sum array so we can compute the sum of any interval [i..j]
        // in O(1) time after O(n) preprocessing.
        //
        // Why this is necessary:
        // The merge cost for every interval includes the total sum of that interval.
        // During the DP, we will ask for interval sums many times.
        // Without prefix sums, each sum query would cost O(length),
        // making the whole algorithm too slow.
        //
        // prefix[t] will store the sum of the first t elements:
        // prefix[0] = 0
        // prefix[1] = sizes[0]
        // prefix[2] = sizes[0] + sizes[1]
        // ...
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + sizes[i];
        }

        // Step 3:
        // Precompute the minimum and maximum value for every interval [i..j].
        //
        // Why this is necessary:
        // The merge cost is:
        //     sum(i..j) + max(i..j) - min(i..j)
        //
        // During DP, we need this interval cost very often.
        // If we recomputed min and max from scratch for every query,
        // the solution would become too slow.
        //
        // We use two 2D arrays:
        // - intervalMin[i, j] = minimum value in sizes[i..j]
        // - intervalMax[i, j] = maximum value in sizes[i..j]
        //
        // We fill them in O(n^2) by extending intervals to the right.
        int[,] intervalMin = new int[n, n];
        int[,] intervalMax = new int[n, n];

        for (int i = 0; i < n; i++)
        {
            // An interval of length 1 has both min and max equal to the element itself.
            intervalMin[i, i] = sizes[i];
            intervalMax[i, i] = sizes[i];

            // Extend the interval one position at a time:
            // [i..i], [i..i+1], [i..i+2], ...
            for (int j = i + 1; j < n; j++)
            {
                intervalMin[i, j] = Math.Min(intervalMin[i, j - 1], sizes[j]);
                intervalMax[i, j] = Math.Max(intervalMax[i, j - 1], sizes[j]);
            }
        }

        // Step 4:
        // Create the DP table.
        //
        // dp[i, j] means:
        // the minimum total penalty required to merge the subarray sizes[i..j]
        // into exactly one archive.
        //
        // Base case:
        // A single snapshot is already one archive, so no merge is needed.
        // Therefore:
        //     dp[i, i] = 0
        //
        // Transition:
        // To merge interval [i..j], the final operation must merge two adjacent
        // already-formed archives:
        //     [i..k] and [k+1..j]
        //
        // So the total cost is:
        //     dp[i, k] + dp[k+1, j] + cost(i, j)
        //
        // We try every possible split point k and take the minimum.
        long[,] dp = new long[n, n];

        // Step 5:
        // Fill the DP table by increasing interval length.
        //
        // Why increasing length is necessary:
        // dp[i, j] depends on smaller intervals dp[i, k] and dp[k+1, j].
        // Therefore, before computing a larger interval, all smaller intervals
        // must already be computed.
        for (int length = 2; length <= n; length++)
        {
            for (int i = 0; i + length - 1 < n; i++)
            {
                int j = i + length - 1;

                // Compute the merge cost for the whole interval [i..j].
                // This is the cost paid in the FINAL merge that combines
                // the left and right already-built archives into one archive.
                long intervalSum = prefix[j + 1] - prefix[i];
                long intervalCost = intervalSum + (long)intervalMax[i, j] - intervalMin[i, j];

                // Start with a very large value because we are looking for a minimum.
                long best = long.MaxValue;

                // Try every possible final split point k.
                //
                // That means:
                // - First merge [i..k] into one archive with minimum cost dp[i, k]
                // - First merge [k+1..j] into one archive with minimum cost dp[k+1, j]
                // - Then merge those two adjacent archives, paying intervalCost
                for (int k = i; k < j; k++)
                {
                    long candidate = dp[i, k] + dp[k + 1, j] + intervalCost;
                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                dp[i, j] = best;
            }
        }

        // Step 6:
        // The answer for the entire array is the minimum cost to merge [0..n-1].
        return dp[0, n - 1];
    }
}

// Demo code:
// We create sample inputs, call the solution, and print the results.

var solution = new Solution();

int[] sizes1 = { 4, 2, 7 };
long result1 = solution.MinimumPenaltyToMergeSnapshots(sizes1);
Console.WriteLine(result1);

int[] sizes2 = { 5, 5, 5, 5 };
long result2 = solution.MinimumPenaltyToMergeSnapshots(sizes2);
Console.WriteLine(result2);

// Additional small sanity checks for learning:
int[] sizes3 = { 10 };
Console.WriteLine(solution.MinimumPenaltyToMergeSnapshots(sizes3)); // 0

int[] sizes4 = { 1, 3 };
Console.WriteLine(solution.MinimumPenaltyToMergeSnapshots(sizes4)); // (1+3)+(3-1)=6