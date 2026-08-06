/*
Title: Maximum Score from Choosing a Pivoted Quadruple

Problem Description:
You are given an integer array nums of length n. A pivoted quadruple is a choice of four indices
(a, b, c, d) such that 0 <= a < b < c < d < n and b and c act as the two middle anchors of the quadruple.

The score of such a quadruple is defined as:
(nums[a] - nums[b]) * (nums[c] - nums[d])

Your task is to return the maximum possible score over all valid pivoted quadruples.
If every possible quadruple has a negative score, you must still return the largest value among them.
It is guaranteed that n >= 4.

Key observation:
For a fixed pair (b, c), the expression splits into two independent parts:

Left part:  nums[a] - nums[b], where a < b
Right part: nums[c] - nums[d], where d > c

So for each possible middle pair (b, c), we want:
bestLeftAtB  = max over a < b of (nums[a] - nums[b])
bestRightAtC = max over d > c of (nums[c] - nums[d])

Then the score for that pair is:
bestLeftAtB * bestRightAtC

However, because multiplication depends on signs, the maximum product for a fixed (b, c)
may come from:
- largest positive left * largest positive right
or
- most negative left * most negative right

Therefore, for every b we need both:
- maximum possible left difference
- minimum possible left difference

And for every c we need both:
- maximum possible right difference
- minimum possible right difference

Then for each valid b < c, the best score is the maximum of:
leftMax[b] * rightMax[c]
leftMin[b] * rightMin[c]
leftMax[b] * rightMin[c]
leftMin[b] * rightMax[c]

Actually, to be fully correct for all sign combinations, we simply test all four products.

This yields an O(n^2) combination if we try all b and c, which is too slow.
We need to exploit the structure further.

Let:
For each c, define:
suffixBestProductFromCWithAnyEarlierB = max over b < c of best score using that c

But we can do even better by scanning c from left to right while maintaining the best and worst
left values seen so far among all valid b before c.

For each b:
leftDiffMaxAtB = prefixMaxBeforeB - nums[b]
leftDiffMinAtB = prefixMinBeforeB - nums[b]

When we move c from left to right, all b < c become eligible.
So we maintain among eligible b:
globalLeftMax = maximum of leftDiffMaxAtB
globalLeftMin = minimum of leftDiffMinAtB

For the current c, the right side depends only on c and some d > c:
rightDiffMaxAtC = nums[c] - suffixMinAfterC
rightDiffMinAtC = nums[c] - suffixMaxAfterC

Then the best score using this c and any valid earlier b is the maximum of:
globalLeftMax * rightDiffMaxAtC
globalLeftMax * rightDiffMinAtC
globalLeftMin * rightDiffMaxAtC
globalLeftMin * rightDiffMinAtC

This is O(n), after precomputing prefix/suffix extrema.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of the approach:
    1. Precompute, for every index b, the best and worst possible left difference:
         nums[a] - nums[b], where a < b
       This uses prefix maximum and prefix minimum values.

    2. Precompute, for every index c, the best and worst possible right difference:
         nums[c] - nums[d], where d > c
       This uses suffix minimum and suffix maximum values.

    3. Scan c from left to right.
       Before processing c, add index b = c - 1 into the pool of eligible middle-left anchors.
       This guarantees the ordering b < c.

    4. Maintain:
         globalLeftMax = largest left difference among all eligible b
         globalLeftMin = smallest left difference among all eligible b
       These two values are enough because the maximum product with the current right difference
       can come from either extreme depending on signs.

    5. For each c, combine the current right extremes with the maintained left extremes,
       test all four products, and update the answer.
    */
    public long MaximumScore(int[] nums)
    {
        int n = nums.Length;

        // These arrays store, for each index i:
        // leftMax[i] = maximum value of nums[a] - nums[i] for any a < i
        // leftMin[i] = minimum value of nums[a] - nums[i] for any a < i
        //
        // Why do we need both?
        // Because when multiplying by the right-side difference, the best product could come
        // from a large positive number or from a large-magnitude negative number.
        long[] leftMax = new long[n];
        long[] leftMin = new long[n];

        // These arrays store, for each index i:
        // rightMax[i] = maximum value of nums[i] - nums[d] for any d > i
        // rightMin[i] = minimum value of nums[i] - nums[d] for any d > i
        //
        // Again, we keep both extremes because sign matters in multiplication.
        long[] rightMax = new long[n];
        long[] rightMin = new long[n];

        // -----------------------------
        // Step 1: Build left-side extremes
        // -----------------------------
        //
        // For each position b, we need:
        //   max over a < b of (nums[a] - nums[b])
        //   min over a < b of (nums[a] - nums[b])
        //
        // If we know:
        //   prefixMax = maximum nums[a] seen before b
        //   prefixMin = minimum nums[a] seen before b
        //
        // then:
        //   leftMax[b] = prefixMax - nums[b]
        //   leftMin[b] = prefixMin - nums[b]
        //
        // We start from index 1 because index 0 has no earlier a.
        long prefixMax = nums[0];
        long prefixMin = nums[0];

        for (int b = 1; b < n; b++)
        {
            leftMax[b] = prefixMax - nums[b];
            leftMin[b] = prefixMin - nums[b];

            if (nums[b] > prefixMax) prefixMax = nums[b];
            if (nums[b] < prefixMin) prefixMin = nums[b];
        }

        // -----------------------------
        // Step 2: Build right-side extremes
        // -----------------------------
        //
        // For each position c, we need:
        //   max over d > c of (nums[c] - nums[d])
        //   min over d > c of (nums[c] - nums[d])
        //
        // If we know:
        //   suffixMin = minimum nums[d] seen after c
        //   suffixMax = maximum nums[d] seen after c
        //
        // then:
        //   rightMax[c] = nums[c] - suffixMin
        //   rightMin[c] = nums[c] - suffixMax
        //
        // We start from index n-2 because index n-1 has no later d.
        long suffixMin = nums[n - 1];
        long suffixMax = nums[n - 1];

        for (int c = n - 2; c >= 0; c--)
        {
            rightMax[c] = nums[c] - suffixMin;
            rightMin[c] = nums[c] - suffixMax;

            if (nums[c] < suffixMin) suffixMin = nums[c];
            if (nums[c] > suffixMax) suffixMax = nums[c];
        }

        // -----------------------------
        // Step 3: Sweep c from left to right
        // -----------------------------
        //
        // We need valid ordering:
        //   a < b < c < d
        //
        // For a fixed c, valid b values are all indices < c, but b itself must also have
        // at least one earlier a, so b must be at least 1.
        //
        // Also c must have at least one later d, so c can be at most n-2.
        //
        // We maintain among all eligible b:
        //   globalLeftMax = maximum left difference seen so far
        //   globalLeftMin = minimum left difference seen so far
        //
        // When processing c, the newly eligible b is c-1.
        // So before evaluating c, we insert b = c-1.
        long answer = long.MinValue;

        long globalLeftMax = long.MinValue;
        long globalLeftMin = long.MaxValue;

        for (int c = 2; c <= n - 2; c++)
        {
            int b = c - 1;

            // Add this b into the pool of valid left anchors.
            // This is safe because:
            // - b = c - 1 ensures b < c
            // - since c >= 2, we have b >= 1, so there exists at least one a < b
            if (leftMax[b] > globalLeftMax) globalLeftMax = leftMax[b];
            if (leftMin[b] < globalLeftMin) globalLeftMin = leftMin[b];

            // Current right-side extremes for this c.
            long rMax = rightMax[c];
            long rMin = rightMin[c];

            // To be fully correct for all sign combinations, test all four products.
            long p1 = globalLeftMax * rMax;
            long p2 = globalLeftMax * rMin;
            long p3 = globalLeftMin * rMax;
            long p4 = globalLeftMin * rMin;

            if (p1 > answer) answer = p1;
            if (p2 > answer) answer = p2;
            if (p3 > answer) answer = p3;
            if (p4 > answer) answer = p4;
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 8, 1, 9, 2, 7 };
Console.WriteLine(solution.MaximumScore(nums1));

int[] nums2 = { 5, 10, 3, 8, 1, 6 };
Console.WriteLine(solution.MaximumScore(nums2));

int[] nums3 = { 8, 1, 9, 1, 2 };
Console.WriteLine(solution.MaximumScore(nums3));