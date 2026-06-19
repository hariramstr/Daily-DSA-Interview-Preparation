/*
Title: Minimum Cost to Archive Logs with Integrity Checkpoints

Problem Description:
A company stores a sequence of daily log batches in order. You are given an array `size` where `size[i]` is the size of the `i`-th batch, and an array `risk` where `risk[i]` is the corruption risk score of that batch. The company wants to partition the batches into exactly `k` contiguous archive files. For each archive file covering batches from index `l` to `r` inclusive, its storage cost is defined as:

    (sum of size[l..r]) * (maximum risk[l..r])

In addition, every archive file except the last must end with an integrity checkpoint. The company has a list of allowed checkpoint positions given by a binary string `checkpoint`, where `checkpoint[i] = '1'` means a file is allowed to end at batch `i`. The last archive file must always end at the final batch `n - 1`, even if `checkpoint[n - 1] = '0'`.

Return the minimum total storage cost to partition all batches into exactly `k` contiguous archive files while respecting the checkpoint rule. If it is impossible, return `-1`.

A partition is valid if every file is non-empty, files cover all batches exactly once, and for the first `k - 1` files the ending index must be an allowed checkpoint.

Constraints:
- 1 <= n <= 2000
- 1 <= k <= min(n, 50)
- 1 <= size[i] <= 10^6
- 1 <= risk[i] <= 10^6
- checkpoint.length == n
- checkpoint[i] is either '0' or '1'
*/

using System;

class Solution
{
    /*
    Time Complexity:
    - Precomputing all segment costs: O(n^2)
    - Dynamic programming transitions: O(k * n^2)
    - Total: O(k * n^2)

    Space Complexity:
    - Segment cost table: O(n^2)
    - DP arrays: O(n)
    - Total: O(n^2)

    Why this is acceptable:
    - n <= 2000 and k <= 50
    - O(n^2) preprocessing plus O(k * n^2) DP is practical in optimized C#
    - We use rolling DP arrays to keep the DP memory small
    */
    public long MinArchiveCost(int[] size, int[] risk, string checkpoint, int k)
    {
        int n = size.Length;

        // Basic impossibility checks.
        // We need exactly k non-empty files, so k cannot exceed n.
        if (k < 1 || k > n) return -1;

        // Count how many legal internal cut positions exist.
        // To create exactly k files, we need exactly k - 1 internal cuts.
        // Each internal cut must end at an index i where checkpoint[i] == '1'.
        // The final file ends at n - 1 automatically and does NOT need checkpoint[n - 1] == '1'.
        int allowedInternalEnds = 0;
        for (int i = 0; i < n - 1; i++)
        {
            if (checkpoint[i] == '1')
                allowedInternalEnds++;
        }

        if (allowedInternalEnds < k - 1)
            return -1;

        // Prefix sums let us compute sum(size[l..r]) in O(1):
        // sum = prefix[r + 1] - prefix[l]
        long[] prefixSize = new long[n + 1];
        for (int i = 0; i < n; i++)
            prefixSize[i + 1] = prefixSize[i] + size[i];

        // Precompute cost[l, r] = (sum of size[l..r]) * (max risk[l..r]).
        //
        // Why precompute?
        // During DP we will ask for many segment costs repeatedly.
        // If we recompute max risk and sum every time, transitions become too slow.
        //
        // How do we compute efficiently?
        // For each fixed left boundary l, we extend r from l to n - 1.
        // We maintain the running maximum risk in that interval.
        long[,] cost = new long[n, n];
        for (int l = 0; l < n; l++)
        {
            int maxRisk = 0;
            for (int r = l; r < n; r++)
            {
                if (risk[r] > maxRisk)
                    maxRisk = risk[r];

                long segmentSum = prefixSize[r + 1] - prefixSize[l];
                cost[l, r] = segmentSum * maxRisk;
            }
        }

        // We use a very large number as "infinity" for impossible states.
        long INF = long.MaxValue / 4;

        // DP meaning:
        // prev[end] = minimum cost to partition batches [0..end] into exactly (parts - 1) files
        // curr[end] = minimum cost to partition batches [0..end] into exactly parts files
        //
        // We build the answer file-count by file-count.
        long[] prev = new long[n];
        long[] curr = new long[n];

        // Initialize all states as impossible.
        for (int i = 0; i < n; i++)
            prev[i] = INF;

        // Base case: exactly 1 file.
        // Then the whole prefix [0..end] must be one single file.
        // This is always valid because only internal file endings need checkpoints,
        // and with 1 file there are no internal endings.
        for (int end = 0; end < n; end++)
            prev[end] = cost[0, end];

        // Build DP for 2 files, 3 files, ..., k files.
        for (int parts = 2; parts <= k; parts++)
        {
            // Reset current layer to impossible.
            for (int i = 0; i < n; i++)
                curr[i] = INF;

            // We want curr[end]:
            // minimum cost to partition [0..end] into exactly "parts" files.
            //
            // The last file must start at some index start and end at "end".
            // Then the previous "parts - 1" files cover [0..start - 1].
            //
            // Transition:
            // curr[end] = min over start in [parts - 1 .. end]:
            //             prev[start - 1] + cost[start, end]
            //
            // But there is an extra rule:
            // The previous file ends at start - 1, and since that is an internal ending
            // (unless this is the final overall file count logic, but in DP it is internal
            // whenever parts >= 2), it must be an allowed checkpoint.
            //
            // Therefore, start - 1 must satisfy checkpoint[start - 1] == '1'.
            //
            // Also, to have exactly "parts" non-empty files ending at "end",
            // we need at least "parts" elements total, so end >= parts - 1.
            for (int end = parts - 1; end < n; end++)
            {
                long best = INF;

                // Try every possible start of the last file.
                // start must be at least parts - 1 so that the first parts - 1 files
                // each get at least one element.
                for (int start = parts - 1; start <= end; start++)
                {
                    int previousEnd = start - 1;

                    // The boundary between file (parts - 1) and file parts is at previousEnd.
                    // This is an internal cut, so it must be allowed by checkpoint.
                    if (checkpoint[previousEnd] != '1')
                        continue;

                    long previousCost = prev[previousEnd];
                    if (previousCost == INF)
                        continue;

                    long candidate = previousCost + cost[start, end];
                    if (candidate < best)
                        best = candidate;
                }

                curr[end] = best;
            }

            // Move current layer into previous for the next iteration.
            var temp = prev;
            prev = curr;
            curr = temp;
        }

        // Final answer must cover the entire array, so it must end at n - 1.
        long answer = prev[n - 1];
        return answer >= INF ? -1 : answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] size1 = { 4, 2, 7, 3 };
int[] risk1 = { 5, 1, 4, 2 };
string checkpoint1 = "1010";
int k1 = 2;
long result1 = solution.MinArchiveCost(size1, risk1, checkpoint1, k1);
Console.WriteLine(result1);

// Example 2
int[] size2 = { 3, 6, 2, 5, 4 };
int[] risk2 = { 2, 7, 1, 3, 6 };
string checkpoint2 = "01010";
int k2 = 3;
long result2 = solution.MinArchiveCost(size2, risk2, checkpoint2, k2);
Console.WriteLine(result2);

// Additional quick sanity checks

// Single file: always valid
int[] size3 = { 5, 1, 2 };
int[] risk3 = { 3, 4, 2 };
string checkpoint3 = "000";
int k3 = 1;
Console.WriteLine(solution.MinArchiveCost(size3, risk3, checkpoint3, k3));

// Impossible because not enough allowed internal checkpoints
int[] size4 = { 1, 1, 1, 1 };
int[] risk4 = { 1, 1, 1, 1 };
string checkpoint4 = "0000";
int k4 = 2;
Console.WriteLine(solution.MinArchiveCost(size4, risk4, checkpoint4, k4));