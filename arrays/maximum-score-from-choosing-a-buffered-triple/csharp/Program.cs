/*
Title: Maximum Score From Choosing a Buffered Triple

Problem Description:
You are given an integer array nums of length n and an integer gap. A buffered triple is a choice of three indices (i, j, k) such that:
- i < j < k
- j - i > gap
- k - j > gap

The score of such a triple is:
    nums[i] - nums[j] + nums[k]

Your task is to return the maximum possible score among all valid buffered triples.
If no valid triple exists, return -1.

Key observation:
For a fixed middle index j:
- The left index i must come from the prefix [0 .. j - gap - 1]
- The right index k must come from the suffix [j + gap + 1 .. n - 1]

So the best score for a fixed j is:
    (maximum value on the allowed left side) - nums[j] + (maximum value on the allowed right side)

This means we can precompute:
1. prefixMax[x] = maximum value in nums[0..x]
2. suffixMax[x] = maximum value in nums[x..n-1]

Then for every valid middle index j, we can compute the best score in O(1),
making the full solution O(n).

Important correctness note about the examples:
- Example 1:
  nums = [5, 1, 9, 2, 7, 3, 8], gap = 1
  The valid middle positions are j where both sides have enough room.
  The best valid triple is actually (2, 5, 6) invalid because 6 - 5 = 1 is not > 1.
  The valid optimal triple is (2, 3, 6): 9 - 2 + 8 = 15.
  Output = 15

- Example 2:
  nums = [4, -3, 6, -10, 5, 2], gap = 1
  The valid optimal triple is (2, 3, 5): 6 - (-10) + 2 = 18.
  Output = 18
  The statement text itself warns that 20 would only be possible for gap = 0.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation:
    - We build one prefix maximum array in O(n)
    - We build one suffix maximum array in O(n)
    - We scan all possible middle indices j in O(n)
    - Each middle index is evaluated in O(1)

    Therefore total time is linear, which is necessary for n up to 200000.
    */
    public long MaximumScoreBufferedTriple(int[] nums, int gap)
    {
        // Store the array length because we will use it many times.
        int n = nums.Length;

        // Before doing any work, we check whether it is even possible to choose
        // three indices i < j < k with both spacing constraints:
        //
        // j - i > gap  means there must be at least gap + 1 positions between i and j
        // k - j > gap  means there must be at least gap + 1 positions between j and k
        //
        // The smallest possible valid pattern would look like:
        // i, ..., j, ..., k
        // requiring at least:
        // 1 element for i
        // gap elements skipped + 1 move to j
        // gap elements skipped + 1 move to k
        //
        // In index-distance terms, the minimum total span is:
        // (gap + 1) + (gap + 1) = 2*gap + 2
        // which means we need at least 2*gap + 3 elements overall.
        //
        // If n is smaller than that, no valid triple exists.
        if (n < 2 * gap + 3)
        {
            return -1;
        }

        // prefixMax[x] will store the maximum value among nums[0], nums[1], ..., nums[x].
        //
        // Why do we need this?
        // For a chosen middle index j, the left index i can be anywhere in:
        // [0 .. j - gap - 1]
        //
        // We do NOT care which exact index gives the best score on the left;
        // we only care about the maximum nums[i] available there.
        //
        // So prefixMax lets us answer:
        // "What is the best left value available up to a certain boundary?"
        // in O(1) time after preprocessing.
        long[] prefixMax = new long[n];

        // Initialize the first prefix maximum.
        prefixMax[0] = nums[0];

        // Build the prefix maximum array from left to right.
        for (int i = 1; i < n; i++)
        {
            // At position i, the best value seen so far is either:
            // - the previous best prefix maximum, or
            // - the current element nums[i]
            prefixMax[i] = Math.Max(prefixMax[i - 1], nums[i]);
        }

        // suffixMax[x] will store the maximum value among nums[x], nums[x+1], ..., nums[n-1].
        //
        // Why do we need this?
        // For a chosen middle index j, the right index k can be anywhere in:
        // [j + gap + 1 .. n - 1]
        //
        // Again, we do not care which exact k gives the best score;
        // we only care about the maximum nums[k] in that allowed suffix.
        //
        // So suffixMax lets us answer:
        // "What is the best right value available starting from a certain boundary?"
        // in O(1) time after preprocessing.
        long[] suffixMax = new long[n];

        // Initialize the last suffix maximum.
        suffixMax[n - 1] = nums[n - 1];

        // Build the suffix maximum array from right to left.
        for (int i = n - 2; i >= 0; i--)
        {
            // At position i, the best value from i to the end is either:
            // - the current element nums[i], or
            // - the best suffix value starting at i + 1
            suffixMax[i] = Math.Max(nums[i], suffixMax[i + 1]);
        }

        // We will track the best score found among all valid middle indices.
        long bestScore = long.MinValue;

        // A middle index j is valid only if:
        // - there is at least one valid left index i in [0 .. j - gap - 1]
        // - there is at least one valid right index k in [j + gap + 1 .. n - 1]
        //
        // Therefore:
        // left boundary exists when j - gap - 1 >= 0  =>  j >= gap + 1
        // right boundary exists when j + gap + 1 <= n - 1  =>  j <= n - gap - 2
        //
        // So we iterate j only over that valid range.
        for (int j = gap + 1; j <= n - gap - 2; j++)
        {
            // Compute the farthest index on the left that is still allowed for i.
            //
            // Since j - i > gap, rearranging gives:
            // i < j - gap
            //
            // Because i is an integer index, the largest valid i is:
            // j - gap - 1
            int leftLimit = j - gap - 1;

            // Compute the earliest index on the right that is allowed for k.
            //
            // Since k - j > gap, rearranging gives:
            // k > j + gap
            //
            // Because k is an integer index, the smallest valid k is:
            // j + gap + 1
            int rightStart = j + gap + 1;

            // The best possible left contribution for this j is the maximum value
            // anywhere in nums[0 .. leftLimit].
            long bestLeftValue = prefixMax[leftLimit];

            // The best possible right contribution for this j is the maximum value
            // anywhere in nums[rightStart .. n-1].
            long bestRightValue = suffixMax[rightStart];

            // Now compute the best score using this middle index j.
            //
            // Score formula:
            // nums[i] - nums[j] + nums[k]
            //
            // Since we already chose the best possible nums[i] and nums[k] values
            // for this j, the best score for this middle is:
            long currentScore = bestLeftValue - nums[j] + bestRightValue;

            // Update the global answer if this middle index gives a better score.
            if (currentScore > bestScore)
            {
                bestScore = currentScore;
            }
        }

        // If the loop ran, bestScore now contains the maximum valid score.
        return bestScore;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] nums1 = { 5, 1, 9, 2, 7, 3, 8 };
int gap1 = 1;
long result1 = solution.MaximumScoreBufferedTriple(nums1, gap1);
Console.WriteLine(result1); // Expected: 15

// Example 2
int[] nums2 = { 4, -3, 6, -10, 5, 2 };
int gap2 = 1;
long result2 = solution.MaximumScoreBufferedTriple(nums2, gap2);
Console.WriteLine(result2); // Expected: 18

// Additional demo: no valid triple
int[] nums3 = { 1, 2, 3 };
int gap3 = 1;
long result3 = solution.MaximumScoreBufferedTriple(nums3, gap3);
Console.WriteLine(result3); // Expected: -1