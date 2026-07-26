/*
Title: Maximum Score from Splitting an Array into Dominant Ranges

Problem Description:
You are given an integer array nums of length n and an integer k. You must split the array
into exactly k non-empty contiguous subarrays. The score of one subarray is defined as the
frequency of its most common value multiplied by the length of that subarray. The total score
of a split is the sum of the scores of all k chosen subarrays.

Your task is to return the maximum possible total score.

More formally, if a subarray nums[l..r] contains some value x that appears f times, and no
other value appears more than f times, then the subarray contributes f * (r - l + 1) to the
total. If multiple values tie for maximum frequency, the frequency value is still used only once.

Constraints:
- 1 <= n <= 350
- 1 <= k <= min(n, 50)
- 1 <= nums[i] <= 10^5
- Each split must use all elements of nums exactly once.

Key idea:
1. Precompute the score of every subarray nums[l..r].
2. Use dynamic programming:
   dp[p][i] = maximum score for splitting the first i elements into exactly p non-empty parts.
3. Transition:
   dp[p][i] = max over j from p-1 to i-1 of dp[p-1][j] + score[j][i-1]
   where the last part is nums[j..i-1].

This is efficient enough because:
- n <= 350, so O(n^2) subarray preprocessing is fine.
- DP with O(k * n^2) is also fine.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Precomputing all subarray scores: O(n^2)
      Explanation:
      For each starting index l, we extend the right end r from l to n-1.
      We maintain frequencies incrementally, so each pair (l, r) is processed once.
    - Dynamic programming transitions: O(k * n^2)
      For each number of parts p, and each prefix length i, we try every possible previous cut j.
    - Total: O(n^2 + k * n^2) = O(k * n^2)

    Space Complexity:
    - score table: O(n^2)
    - dp table: O(k * n)
    - frequency map during preprocessing: O(n) in the worst case for one starting point
    - Total: O(n^2 + k * n)
    */
    public long MaximumScore(int[] nums, int k)
    {
        int n = nums.Length;

        // score[l, r] will store the score of subarray nums[l..r].
        // That score is:
        //   (maximum frequency of any value in nums[l..r]) * (length of nums[l..r])
        //
        // We use long because although the constraints are small enough for int in practice,
        // using long is safer and more general for DP sums.
        long[,] score = new long[n, n];

        // ------------------------------------------------------------
        // STEP 1: Precompute the score of every subarray.
        // ------------------------------------------------------------
        //
        // Why do we do this?
        // During DP, we will repeatedly ask:
        //   "What is the score of the last segment nums[j..i-1]?"
        // If we compute that from scratch every time, the solution becomes too slow.
        //
        // So we precompute all subarray scores once, and then each DP transition
        // can read the answer in O(1).
        //
        // How do we compute score[l, r] efficiently?
        // For a fixed left boundary l:
        //   - Start with an empty frequency map.
        //   - Extend r from l to n-1.
        //   - Update the frequency of nums[r].
        //   - Track the current maximum frequency seen so far.
        //   - The subarray length is (r - l + 1).
        //   - Therefore score[l, r] = maxFrequency * length.
        //
        // This works in O(n^2) total because each pair (l, r) is processed once.
        for (int l = 0; l < n; l++)
        {
            var freq = new Dictionary<int, int>();
            int maxFreq = 0;

            for (int r = l; r < n; r++)
            {
                int value = nums[r];

                if (!freq.TryAdd(value, 1))
                {
                    freq[value]++;
                }

                // Update the dominant frequency of the current subarray nums[l..r].
                if (freq[value] > maxFreq)
                {
                    maxFreq = freq[value];
                }

                int length = r - l + 1;
                score[l, r] = (long)maxFreq * length;
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Dynamic Programming over prefixes and number of parts.
        // ------------------------------------------------------------
        //
        // Definition:
        // dp[p, i] = maximum score obtainable by splitting the first i elements
        //            (that is nums[0..i-1]) into exactly p non-empty contiguous parts.
        //
        // Important indexing note:
        // - i is a prefix length, so i ranges from 0 to n.
        // - The first i elements are nums[0], nums[1], ..., nums[i-1].
        //
        // Base case:
        // dp[0, 0] = 0
        //   Splitting zero elements into zero parts gives score 0.
        //
        // Invalid states:
        // dp[0, i] for i > 0 is impossible.
        // dp[p, i] when p > i is impossible because each part must be non-empty.
        //
        // Transition:
        // Suppose the last part starts at index j and ends at index i-1.
        // Then:
        // - The first j elements must be split into p-1 parts => dp[p-1, j]
        // - The last part contributes score[j, i-1]
        //
        // So:
        // dp[p, i] = max over j in [p-1, i-1] of:
        //            dp[p-1, j] + score[j, i-1]
        //
        // Why does j start at p-1?
        // Because the first j elements must be split into p-1 non-empty parts,
        // so we need at least p-1 elements there.
        long NEG = long.MinValue / 4;
        long[,] dp = new long[k + 1, n + 1];

        // Initialize all states as impossible.
        for (int p = 0; p <= k; p++)
        {
            for (int i = 0; i <= n; i++)
            {
                dp[p, i] = NEG;
            }
        }

        dp[0, 0] = 0;

        // Iterate over the number of parts we want to form.
        for (int parts = 1; parts <= k; parts++)
        {
            // To split i elements into 'parts' non-empty segments, we must have i >= parts.
            for (int i = parts; i <= n; i++)
            {
                long best = NEG;

                // Try every possible starting point j of the last segment.
                // The last segment is nums[j..i-1].
                //
                // j must be at least parts - 1 so that the first j elements can form
                // exactly parts - 1 non-empty segments.
                for (int j = parts - 1; j <= i - 1; j++)
                {
                    if (dp[parts - 1, j] == NEG)
                    {
                        // This previous state is impossible, so skip it.
                        continue;
                    }

                    long candidate = dp[parts - 1, j] + score[j, i - 1];

                    if (candidate > best)
                    {
                        best = candidate;
                    }
                }

                dp[parts, i] = best;
            }
        }

        return dp[k, n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

int[] nums1 = { 1, 2, 2, 1, 2 };
int k1 = 2;
long result1 = solution.MaximumScore(nums1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 13

int[] nums2 = { 4, 4, 3, 3, 3, 2, 2 };
int k2 = 3;
long result2 = solution.MaximumScore(nums2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional small sanity checks
int[] nums3 = { 5 };
int k3 = 1;
long result3 = solution.MaximumScore(nums3, k3);
Console.WriteLine($"Single element Result: {result3}"); // Expected: 1

int[] nums4 = { 1, 1, 1, 1 };
int k4 = 2;
long result4 = solution.MaximumScore(nums4, k4);
Console.WriteLine($"All same, 2 parts Result: {result4}");

int[] nums5 = { 1, 2, 3, 4 };
int k5 = 2;
long result5 = solution.MaximumScore(nums5, k5);
Console.WriteLine($"All distinct, 2 parts Result: {result5}");