/*
Title: Minimum Energy to Merge Subtitle Fragments
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You are given a list of subtitle fragments extracted from a long video. The fragments must be merged in their original order into a single final transcript. Merging fragments is expensive because each merge requires re-rendering and timing adjustments.

Suppose the length of fragment i is lengths[i]. You may only merge adjacent groups of fragments. When you merge one contiguous group of fragments into a single larger group, the energy cost of that operation is equal to the total number of characters in that group. After the merge, that group behaves like a single fragment and can be merged again later.

Your task is to compute the minimum total energy needed to merge all fragments into exactly one fragment.

For example, if the fragments have lengths [4, 2, 7], you could first merge 4 and 2 for cost 6, producing [6, 7], then merge them for cost 13, for a total of 19. A different order may produce a different total, so you must find the minimum possible total.

Return the minimum total energy.

Constraints:
- 1 <= lengths.length <= 300
- 1 <= lengths[i] <= 10^4
- The answer fits in a 32-bit signed integer

Example 1:
Input: lengths = [4,2,7]
Output: 19
Explanation: Merge [4,2] first for cost 6, then merge [6,7] for cost 13. Total = 19. Merging [2,7] first would cost 9, then merging [4,9] would cost 13, total = 22.

Example 2:
Input: lengths = [3,5,1,2]
Output: 22
Explanation: One optimal strategy is to merge [1,2] first for cost 3, giving [3,5,3]. Then merge [5,3] for cost 8, giving [3,8]. Finally merge [3,8] for cost 11. Total = 3 + 8 + 11 = 22.

Key Idea:
This is the classic "merge adjacent piles / files / stones" interval dynamic programming problem.

If dp[i, j] represents the minimum energy needed to merge the subarray lengths[i..j] into one fragment,
then to compute dp[i, j], we try every possible final split point k where:
- lengths[i..k] is merged into one fragment
- lengths[k+1..j] is merged into one fragment
- then those two resulting fragments are merged together

So:
dp[i, j] = min over k in [i, j-1] of:
           dp[i, k] + dp[k+1, j] + sum(lengths[i..j])

We use prefix sums so sum(lengths[i..j]) can be computed in O(1).
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^3)
    - There are O(n^2) intervals (i, j).
    - For each interval, we try O(n) split points.
    - Therefore total time is O(n^3).

    Space Complexity: O(n^2)
    - We store a 2D DP table of size n x n.
    - We also store a prefix sum array of size n + 1.
    */
    public int MinimumEnergyToMerge(int[] lengths)
    {
        // Step 1:
        // Handle the smallest possible input.
        //
        // If there is only one fragment, it is already fully merged.
        // No merge operation is needed, so the energy cost is 0.
        int n = lengths.Length;
        if (n <= 1)
        {
            return 0;
        }

        // Step 2:
        // Build a prefix sum array so we can quickly compute the total length
        // of any contiguous interval.
        //
        // Why do we need this?
        // Every time we compute dp[i, j], we must add the total size of the interval
        // lengths[i..j], because after merging the left part and right part into
        // single fragments, we must perform one final merge of the whole interval,
        // and that final merge costs the total number of characters in that interval.
        //
        // Without prefix sums, computing interval sums repeatedly would cost O(n)
        // each time, making the solution slower.
        //
        // prefix[t] will store the sum of lengths[0] through lengths[t - 1].
        // Therefore:
        // sum of lengths[i..j] = prefix[j + 1] - prefix[i]
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + lengths[i];
        }

        // Step 3:
        // Create the DP table.
        //
        // dp[i, j] means:
        // the minimum energy required to merge the contiguous subarray
        // lengths[i..j] into exactly one fragment.
        //
        // Important base case:
        // dp[i, i] = 0
        // because a single fragment is already one fragment, so no merge is needed.
        //
        // In C#, int arrays are initialized to 0 by default, so dp[i, i] is already 0.
        int[,] dp = new int[n, n];

        // Step 4:
        // Process intervals in increasing order of length.
        //
        // Why this order?
        // To compute dp[i, j], we need smaller subproblems like dp[i, k] and dp[k+1, j].
        // Those subproblems must already be computed before we compute the larger interval.
        //
        // We start from interval length 2 because length 1 intervals are the base case.
        for (int len = 2; len <= n; len++)
        {
            // For each interval length, slide the window across the array.
            for (int i = 0; i + len - 1 < n; i++)
            {
                // j is the right endpoint of the current interval.
                int j = i + len - 1;

                // Initialize this DP cell to a very large value,
                // because we are going to minimize over all possible split points.
                dp[i, j] = int.MaxValue;

                // Compute the total length of the current interval lengths[i..j].
                //
                // This is the cost of the final merge step after the left and right
                // subintervals have each been merged into one fragment.
                int totalLength = prefix[j + 1] - prefix[i];

                // Step 5:
                // Try every possible split point k.
                //
                // If we split at k, then:
                // - left interval is [i..k]
                // - right interval is [k+1..j]
                //
                // We first optimally merge the left interval into one fragment:
                //   cost = dp[i, k]
                //
                // We also optimally merge the right interval into one fragment:
                //   cost = dp[k+1, j]
                //
                // Then we merge those two resulting fragments together:
                //   cost = totalLength
                //
                // So total candidate cost is:
                //   dp[i, k] + dp[k+1, j] + totalLength
                //
                // We take the minimum over all possible k.
                for (int k = i; k < j; k++)
                {
                    int candidate = dp[i, k] + dp[k + 1, j] + totalLength;

                    if (candidate < dp[i, j])
                    {
                        dp[i, j] = candidate;
                    }
                }
            }
        }

        // Step 6:
        // The answer for the whole problem is the minimum cost to merge
        // the entire array lengths[0..n-1] into one fragment.
        return dp[0, n - 1];
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// lengths = [4, 2, 7]
// Optimal:
// merge [4,2] => cost 6
// merge [6,7] => cost 13
// total = 19
int[] lengths1 = { 4, 2, 7 };
int result1 = solution.MinimumEnergyToMerge(lengths1);
Console.WriteLine($"Input: [{string.Join(",", lengths1)}]");
Console.WriteLine($"Minimum total energy: {result1}");
Console.WriteLine("Expected: 19");
Console.WriteLine();

// Example 2:
// lengths = [3, 5, 1, 2]
// One optimal strategy:
// merge [1,2] => 3
// array behaves like [3,5,3]
// merge [5,3] => 8
// array behaves like [3,8]
// merge [3,8] => 11
// total = 22
int[] lengths2 = { 3, 5, 1, 2 };
int result2 = solution.MinimumEnergyToMerge(lengths2);
Console.WriteLine($"Input: [{string.Join(",", lengths2)}]");
Console.WriteLine($"Minimum total energy: {result2}");
Console.WriteLine("Expected: 22");
Console.WriteLine();

// Additional small demo:
// Single fragment requires no merge.
int[] lengths3 = { 10 };
int result3 = solution.MinimumEnergyToMerge(lengths3);
Console.WriteLine($"Input: [{string.Join(",", lengths3)}]");
Console.WriteLine($"Minimum total energy: {result3}");
Console.WriteLine("Expected: 0");