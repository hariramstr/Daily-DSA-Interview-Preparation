/*
Title: Minimum Reorders to Group Expiring Coupons
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array coupons where each value represents the expiration day of a coupon in a checkout system.
The coupons are displayed in a fixed row, and you want all coupons with the same expiration day to appear in one contiguous block.
The relative order of coupons inside a block does not matter, and the blocks themselves may appear in any order.

In one operation, you may pick any single coupon from its current position and insert it at any other position in the row.
This shifts the surrounding elements as needed. Return the minimum number of such operations required so that,
in the final arrangement, all equal expiration days are grouped together.

For example, if the row is [3, 1, 3, 2, 1], a valid final arrangement could be [3, 3, 1, 1, 2] or [2, 1, 1, 3, 3].
Your goal is not to construct the arrangement, but to compute the fewest insert operations needed.

Constraints:
- 1 <= coupons.length <= 200
- 1 <= coupons[i] <= 20
- The answer fits in a 32-bit integer.

Example 1:
Input: coupons = [3, 1, 3, 2, 1]
Output: 2

Example 2:
Input: coupons = [4, 4, 2, 2, 3]
Output: 0

Key Idea:
We want to keep as many coupons as possible in their current relative order, while arranging the final row as a sequence of value-blocks.
If we can keep K coupons in place, then the remaining n - K coupons can be moved one-by-one using insert operations.
So the answer is:

    minimum operations = total coupons - maximum coupons we can keep

We solve this with dynamic programming over subsets of distinct values.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let:
      - n = coupons.Length
      - m = number of distinct coupon values

    Building helper data:
      - O(n + m * n)

    DP over subsets:
      - There are 2^m subsets
      - For each subset, we try adding each value not yet used
      - Transition cost is O(1)

    Total:
      - O(m * 2^m + m * n)

    Since coupon values are in [1..20], we have m <= 20, which is feasible.

    Space Complexity:
      - O(2^m + m + n)

    Explanation of the approach:
    --------------------------------
    Suppose we choose an order of blocks in the final arrangement, for example:
        value A block, then value B block, then value C block

    For that chosen block order, we ask:
        "How many coupons can stay where they are, without moving,
         if the final array must be all A's, then all B's, then all C's?"

    A coupon can stay if the kept coupons form a subsequence of the original array
    that already matches that block order.

    For a fixed block order, the maximum number we can keep can be computed by summing:
        count of value X that appear after all previously chosen blocks' kept coupons

    A classic and very effective way to compute this is:
      1. Compress distinct values to indices 0..m-1
      2. Precompute, for every value v and every position p,
         how many v's appear from position p to the end
      3. DP over subsets:
         dp[mask] = maximum number of coupons we can keep
                    after placing exactly the set of blocks in 'mask'
         len[mask] = total size of those blocks in the final arrangement

    Transition:
      If we have already placed blocks in 'mask', then the next block starts at final position len[mask].
      To keep coupons of value v as the next block, we can keep exactly the number of v's
      that appear in the original array at positions >= len[mask].
      Why? Because the first len[mask] positions in the final arrangement are already occupied by previous blocks,
      so any kept coupon for the next block must come from later positions in the original sequence.

    This yields:
      dp[mask | (1 << v)] = max(dp[mask | (1 << v)], dp[mask] + suffixCount[v][len[mask]])

    Finally:
      answer = n - dp[(1 << m) - 1]
    */
    public int MinimumReordersToGroupCoupons(int[] coupons)
    {
        // Step 1:
        // Collect all distinct coupon values and assign each distinct value a compact index.
        //
        // Why this is necessary:
        // The original coupon values can repeat and are not guaranteed to be 0..m-1.
        // Dynamic programming over subsets works naturally when each distinct value
        // is represented by a bit position. So we compress values into indices.
        var distinctValues = coupons.Distinct().ToList();
        int m = distinctValues.Count;
        int n = coupons.Length;

        var valueToIndex = new Dictionary<int, int>();
        for (int i = 0; i < m; i++)
        {
            valueToIndex[distinctValues[i]] = i;
        }

        // Step 2:
        // Convert the original coupon array into compressed indices.
        //
        // Example:
        // If distinct values are [3, 1, 2], then:
        //   3 -> 0
        //   1 -> 1
        //   2 -> 2
        //
        // This makes later array-based processing much simpler and faster.
        int[] compressed = new int[n];
        for (int i = 0; i < n; i++)
        {
            compressed[i] = valueToIndex[coupons[i]];
        }

        // Step 3:
        // Count how many times each distinct value appears.
        //
        // Why we need this:
        // For any subset of chosen blocks, we want to know the total length
        // already occupied in the final arrangement. That length is simply the sum
        // of frequencies of the chosen values.
        int[] frequency = new int[m];
        foreach (int idx in compressed)
        {
            frequency[idx]++;
        }

        // Step 4:
        // Build suffixCount[valueIndex][position]:
        //   number of occurrences of that value from 'position' to the end of the array.
        //
        // Why this is useful:
        // Suppose we have already placed some blocks whose total length is L.
        // If we now place value v as the next block, then the coupons of value v
        // that can remain unmoved are exactly those that appear in the original array
        // at positions >= L.
        //
        // So suffixCount[v][L] gives us the number of v's we can keep for that transition.
        int[][] suffixCount = new int[m][];
        for (int v = 0; v < m; v++)
        {
            suffixCount[v] = new int[n + 1];
        }

        // We fill suffix counts from right to left.
        // At each position i:
        //   suffixCount[v][i] = suffixCount[v][i + 1] + (compressed[i] == v ? 1 : 0)
        for (int v = 0; v < m; v++)
        {
            suffixCount[v][n] = 0;
            for (int i = n - 1; i >= 0; i--)
            {
                suffixCount[v][i] = suffixCount[v][i + 1] + (compressed[i] == v ? 1 : 0);
            }
        }

        // Step 5:
        // Precompute blockLength[mask]:
        //   total number of coupons contained in the set of values represented by 'mask'
        //
        // Why this is necessary:
        // During DP, when we are at subset 'mask', we need to know where the next block starts
        // in the final arrangement. That start position is exactly the total size of all blocks
        // already chosen, which is the sum of frequencies in the subset.
        //
        // Precomputing this avoids recomputing sums repeatedly inside the DP transitions.
        int totalMasks = 1 << m;
        int[] blockLength = new int[totalMasks];

        for (int mask = 1; mask < totalMasks; mask++)
        {
            // Extract the lowest set bit.
            int lowBit = mask & -mask;

            // Find which value index that bit corresponds to.
            int bitIndex = BitOperationsHelper.TrailingZeroCount(lowBit);

            // Remove that bit from the mask to get a smaller subset we already know.
            int previousMask = mask ^ lowBit;

            // The total length of this subset is:
            // length(previous subset) + frequency(of the newly added value)
            blockLength[mask] = blockLength[previousMask] + frequency[bitIndex];
        }

        // Step 6:
        // Dynamic Programming over subsets.
        //
        // dp[mask] = maximum number of coupons we can keep unmoved
        //            after arranging exactly the set of value-blocks in 'mask'
        //
        // Initial state:
        //   dp[0] = 0
        //   No blocks chosen, no coupons kept yet.
        //
        // We initialize all other states to a very small number so that max() works correctly.
        int[] dp = new int[totalMasks];
        for (int i = 0; i < totalMasks; i++)
        {
            dp[i] = int.MinValue / 4;
        }
        dp[0] = 0;

        // Step 7:
        // Process every subset and try appending one more value-block.
        //
        // For current subset 'mask':
        //   - The first blockLength[mask] positions in the final arrangement are already occupied.
        //   - If we place value 'nextValue' as the next block,
        //     then the number of coupons of that value we can keep is suffixCount[nextValue][blockLength[mask]].
        //
        // Why this transition is correct:
        // We are preserving a subsequence of the original array that matches the final block order.
        // Once previous blocks consume blockLength[mask] positions in the final arrangement,
        // the next block must be formed from coupons appearing later in the original sequence.
        for (int mask = 0; mask < totalMasks; mask++)
        {
            if (dp[mask] < 0)
            {
                continue;
            }

            int startPositionForNextBlock = blockLength[mask];

            for (int nextValue = 0; nextValue < m; nextValue++)
            {
                // If this value is already included in the subset, skip it.
                if ((mask & (1 << nextValue)) != 0)
                {
                    continue;
                }

                int nextMask = mask | (1 << nextValue);

                // Number of coupons of 'nextValue' that can stay in place
                // when this value becomes the next contiguous block.
                int keepThisStep = suffixCount[nextValue][startPositionForNextBlock];

                // Update the best result for the new subset.
                dp[nextMask] = Math.Max(dp[nextMask], dp[mask] + keepThisStep);
            }
        }

        // Step 8:
        // The full mask means all distinct values have been assigned a block.
        // dp[fullMask] is the maximum number of coupons we can keep unmoved.
        int fullMask = totalMasks - 1;
        int maxKeep = dp[fullMask];

        // Step 9:
        // Every coupon not kept must be moved using one insert operation.
        // Therefore:
        //   minimum operations = total coupons - maximum kept coupons
        return n - maxKeep;
    }
}

static class BitOperationsHelper
{
    // Small helper to get the index of the least significant set bit.
    // Example:
    //   x = 1   -> 0
    //   x = 2   -> 1
    //   x = 4   -> 2
    //   x = 8   -> 3
    public static int TrailingZeroCount(int x)
    {
        int count = 0;
        while ((x & 1) == 0)
        {
            x >>= 1;
            count++;
        }
        return count;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] coupons1 = { 3, 1, 3, 2, 1 };
int result1 = solution.MinimumReordersToGroupCoupons(coupons1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] coupons2 = { 4, 4, 2, 2, 3 };
int result2 = solution.MinimumReordersToGroupCoupons(coupons2);
Console.WriteLine(result2); // Expected: 0

// Additional quick checks
int[] coupons3 = { 1 };
Console.WriteLine(solution.MinimumReordersToGroupCoupons(coupons3)); // Expected: 0

int[] coupons4 = { 1, 2, 1, 2 };
Console.WriteLine(solution.MinimumReordersToGroupCoupons(coupons4)); // One optimal answer is 1

int[] coupons5 = { 5, 5, 5, 5 };
Console.WriteLine(solution.MinimumReordersToGroupCoupons(coupons5)); // Expected: 0