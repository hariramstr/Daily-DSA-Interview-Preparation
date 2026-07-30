/*
Title: Minimum Merges to Form a Mountain Array
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array nums representing daily measurements. In one operation, you may merge any two adjacent elements into a single element whose value is their sum. After a merge, the array becomes shorter by one, and the relative order of all remaining elements stays the same.

Your goal is to transform the array into a mountain array using the minimum number of merge operations.

An array is considered a mountain array if there exists an index p such that:
- 0 < p < length - 1
- values strictly increase from index 0 to p
- values strictly decrease from index p to length - 1

In other words, the final array must have at least 3 elements and exactly one peak, with no equal adjacent values in either slope.

Return the minimum number of adjacent merges needed to make nums a mountain array. If it is impossible, return -1.

A merge can combine already-merged segments again later, so each final element corresponds to the sum of some contiguous block of the original array.

Constraints:
- 3 <= nums.length <= 200
- 1 <= nums[i] <= 10^6

Example 1:
Input: nums = [1, 2, 1]
Output: 0
Explanation: The array is already a mountain with peak at index 1.

Example 2:
Input: nums = [2, 1, 1, 2]
Output: -1
Explanation:
No sequence of adjacent merges can produce a valid mountain array with at least 3 elements.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n^3)
    Space Complexity: O(n^2)

    Why:
    - We first precompute all subarray sums in O(n^2).
    - Then we compute:
      1) the maximum number of contiguous blocks ending at each position whose sums are strictly increasing
      2) the maximum number of contiguous blocks starting at each position whose sums are strictly decreasing
    - Each of those dynamic programming passes checks all possible previous/next cut positions,
      leading to O(n^3) total work in the worst case.
    - With n <= 200, this is completely acceptable.

    Core idea:
    Every final element after merges is the sum of one contiguous block of the original array.
    So the problem becomes:

    "Partition the array into the maximum possible number of contiguous blocks such that
     the sequence of block sums forms a mountain."

    If we can keep K final blocks, then we used exactly (n - K) merges,
    because each merge reduces the array length by 1.

    Therefore:
    - maximize the number of blocks in a valid mountain partition
    - answer = n - maxBlocks
    - if no mountain partition exists, return -1
    */
    public int MinimumMountainMerges(int[] nums)
    {
        int n = nums.Length;

        // A mountain array must have at least 3 final elements.
        // Since the original array already has at least 3 elements by constraint,
        // we only need to ensure our chosen partition produces at least 3 blocks.
        if (n < 3)
        {
            return -1;
        }

        // ------------------------------------------------------------
        // STEP 1: Precompute prefix sums so we can get any subarray sum quickly.
        //
        // prefix[i] will store the sum of nums[0..i-1].
        // Then sum of nums[l..r] = prefix[r+1] - prefix[l].
        //
        // Why this is necessary:
        // We will repeatedly ask for sums of many contiguous blocks.
        // Without prefix sums, each sum query would cost O(n),
        // making the whole algorithm too slow.
        // ------------------------------------------------------------
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + nums[i];
        }

        // ------------------------------------------------------------
        // STEP 2: Precompute sum[l, r] for every contiguous subarray nums[l..r].
        //
        // sum[l, r] = sum of the block from index l to index r inclusive.
        //
        // Why store this in a 2D array?
        // It makes the later DP transitions easier to read and beginner-friendly.
        // Since n <= 200, O(n^2) memory is fine.
        // ------------------------------------------------------------
        long[,] sum = new long[n, n];
        for (int l = 0; l < n; l++)
        {
            for (int r = l; r < n; r++)
            {
                sum[l, r] = prefix[r + 1] - prefix[l];
            }
        }

        // ------------------------------------------------------------
        // STEP 3: Compute incCount[start, end]
        //
        // Meaning:
        // incCount[start, end] = maximum number of blocks in a partition of nums[0..end]
        // such that:
        //   - the last block is exactly nums[start..end]
        //   - the sequence of block sums is strictly increasing
        //
        // Example:
        // If incCount[3, 5] = 4, that means nums[0..5] can be partitioned into 4 blocks,
        // the last one is nums[3..5], and block sums strictly increase.
        //
        // Why we need this:
        // For a mountain, the left side up to the peak must be strictly increasing.
        // We will later combine this with a decreasing partition on the right side.
        // ------------------------------------------------------------
        int[,] incCount = new int[n, n];

        for (int start = 0; start < n; start++)
        {
            for (int end = start; end < n; end++)
            {
                long currentBlockSum = sum[start, end];

                // Base case:
                // If the current block starts at index 0, then nums[0..end] is just one block.
                // A single block is trivially a strictly increasing sequence of length 1.
                if (start == 0)
                {
                    incCount[start, end] = 1;
                    continue;
                }

                int best = 0;

                // We want the previous block to end at start - 1.
                // Let that previous block be nums[prevStart..start-1].
                // Then we need:
                //   previous block sum < current block sum
                // because the sequence must be strictly increasing.
                //
                // We try every possible prevStart.
                for (int prevStart = 0; prevStart < start; prevStart++)
                {
                    if (incCount[prevStart, start - 1] == 0)
                    {
                        // This previous state is not reachable as a valid increasing partition.
                        continue;
                    }

                    long previousBlockSum = sum[prevStart, start - 1];

                    if (previousBlockSum < currentBlockSum)
                    {
                        best = Math.Max(best, incCount[prevStart, start - 1] + 1);
                    }
                }

                incCount[start, end] = best;
            }
        }

        // ------------------------------------------------------------
        // STEP 4: Compute decCount[start, end]
        //
        // Meaning:
        // decCount[start, end] = maximum number of blocks in a partition of nums[start..n-1]
        // such that:
        //   - the first block is exactly nums[start..end]
        //   - the sequence of block sums is strictly decreasing
        //
        // Example:
        // If decCount[4, 6] = 3, that means nums[4..n-1] can be partitioned into 3 blocks,
        // the first one is nums[4..6], and block sums strictly decrease from there.
        //
        // Why we need this:
        // For a mountain, the right side starting at the peak must be strictly decreasing.
        // ------------------------------------------------------------
        int[,] decCount = new int[n, n];

        for (int start = n - 1; start >= 0; start--)
        {
            for (int end = n - 1; end >= start; end--)
            {
                long currentBlockSum = sum[start, end];

                // Base case:
                // If the current block ends at n - 1, then nums[start..n-1] is just one block.
                // A single block is trivially a strictly decreasing sequence of length 1.
                if (end == n - 1)
                {
                    decCount[start, end] = 1;
                    continue;
                }

                int best = 0;

                // The next block must start at end + 1.
                // Let that next block be nums[nextStart..nextEnd] where nextStart = end + 1.
                // Then we need:
                //   current block sum > next block sum
                // because the sequence must be strictly decreasing.
                //
                // We try every possible nextEnd.
                int nextStart = end + 1;
                for (int nextEnd = nextStart; nextEnd < n; nextEnd++)
                {
                    if (decCount[nextStart, nextEnd] == 0)
                    {
                        // This suffix state is not reachable as a valid decreasing partition.
                        continue;
                    }

                    long nextBlockSum = sum[nextStart, nextEnd];

                    if (currentBlockSum > nextBlockSum)
                    {
                        best = Math.Max(best, 1 + decCount[nextStart, nextEnd]);
                    }
                }

                decCount[start, end] = best;
            }
        }

        // ------------------------------------------------------------
        // STEP 5: Try every possible peak block nums[peakStart..peakEnd].
        //
        // Important observation:
        // In the final mountain partition, the peak is one whole block.
        // The left side consists of blocks before it with strictly increasing sums.
        // The right side consists of blocks after it with strictly decreasing sums.
        //
        // So for each candidate peak block:
        //   - find the best increasing partition on the left that ends right before the peak
        //   - ensure the last left block sum < peak sum
        //   - find the best decreasing partition on the right that starts right after the peak
        //   - ensure peak sum > first right block sum
        //
        // Then total blocks = leftBlocks + 1 + rightBlocks
        //
        // Also:
        // - leftBlocks must be at least 1
        // - rightBlocks must be at least 1
        // because a mountain needs at least one block on each side of the peak.
        // ------------------------------------------------------------
        int maxBlocks = 0;

        for (int peakStart = 1; peakStart <= n - 2; peakStart++)
        {
            for (int peakEnd = peakStart; peakEnd <= n - 2; peakEnd++)
            {
                long peakSum = sum[peakStart, peakEnd];

                int bestLeftBlocks = 0;
                int bestRightBlocks = 0;

                // ----------------------------------------------------
                // Find the best valid left partition for nums[0..peakStart-1]
                // whose last block sum is strictly less than peakSum.
                // ----------------------------------------------------
                for (int leftStart = 0; leftStart < peakStart; leftStart++)
                {
                    int leftBlocks = incCount[leftStart, peakStart - 1];
                    if (leftBlocks == 0)
                    {
                        continue;
                    }

                    long lastLeftBlockSum = sum[leftStart, peakStart - 1];

                    if (lastLeftBlockSum < peakSum)
                    {
                        bestLeftBlocks = Math.Max(bestLeftBlocks, leftBlocks);
                    }
                }

                // If there is no valid left side, this peak cannot form a mountain.
                if (bestLeftBlocks == 0)
                {
                    continue;
                }

                // ----------------------------------------------------
                // Find the best valid right partition for nums[peakEnd+1..n-1]
                // whose first block sum is strictly less than peakSum.
                // Since the right side must strictly decrease from the peak,
                // we need peakSum > firstRightBlockSum.
                // ----------------------------------------------------
                int rightStart = peakEnd + 1;
                for (int rightEnd = rightStart; rightEnd < n; rightEnd++)
                {
                    int rightBlocks = decCount[rightStart, rightEnd];
                    if (rightBlocks == 0)
                    {
                        continue;
                    }

                    long firstRightBlockSum = sum[rightStart, rightEnd];

                    if (peakSum > firstRightBlockSum)
                    {
                        bestRightBlocks = Math.Max(bestRightBlocks, rightBlocks);
                    }
                }

                // If there is no valid right side, this peak cannot form a mountain.
                if (bestRightBlocks == 0)
                {
                    continue;
                }

                int totalBlocks = bestLeftBlocks + 1 + bestRightBlocks;
                maxBlocks = Math.Max(maxBlocks, totalBlocks);
            }
        }

        // ------------------------------------------------------------
        // STEP 6: Convert "maximum number of final blocks" into
        // "minimum number of merges".
        //
        // If we keep K final blocks from an original array of length n,
        // then we performed exactly n - K merges.
        //
        // If maxBlocks is still 0, no valid mountain partition exists.
        // ------------------------------------------------------------
        if (maxBlocks < 3)
        {
            return -1;
        }

        return n - maxBlocks;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

int[] nums1 = { 1, 2, 1 };
int result1 = solution.MinimumMountainMerges(nums1);
Console.WriteLine($"Input: [{string.Join(", ", nums1)}]");
Console.WriteLine($"Minimum merges: {result1}");
Console.WriteLine();

int[] nums2 = { 2, 1, 1, 2 };
int result2 = solution.MinimumMountainMerges(nums2);
Console.WriteLine($"Input: [{string.Join(", ", nums2)}]");
Console.WriteLine($"Minimum merges: {result2}");
Console.WriteLine();

int[] nums3 = { 1, 1, 1 };
int result3 = solution.MinimumMountainMerges(nums3);
Console.WriteLine($"Input: [{string.Join(", ", nums3)}]");
Console.WriteLine($"Minimum merges: {result3}");
Console.WriteLine();

int[] nums4 = { 1, 1, 2, 1 };
int result4 = solution.MinimumMountainMerges(nums4);
Console.WriteLine($"Input: [{string.Join(", ", nums4)}]");
Console.WriteLine($"Minimum merges: {result4}");
Console.WriteLine();

int[] nums5 = { 3, 1, 2, 1, 1 };
int result5 = solution.MinimumMountainMerges(nums5);
Console.WriteLine($"Input: [{string.Join(", ", nums5)}]");
Console.WriteLine($"Minimum merges: {result5}");