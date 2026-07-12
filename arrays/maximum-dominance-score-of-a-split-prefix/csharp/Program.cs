/*
Title: Maximum Dominance Score of a Split Prefix

Problem Description:
You are given an integer array nums of length n. For any split point i where 0 <= i < n - 1,
divide the array into a left part nums[0..i] and a right part nums[i+1..n-1].

Define the dominance score of this split as:
1) the absolute difference between:
   - the maximum subarray sum entirely contained in the left part
   - the minimum subarray sum entirely contained in the right part

You must also consider the reverse direction:
2) the absolute difference between:
   - the minimum subarray sum in the left part
   - the maximum subarray sum in the right part

The score of split i is the larger of those two values.
Return the maximum score over all valid split points.

A subarray must be non-empty and contiguous.
The maximum and minimum subarray sums are computed independently within each side of the split;
they do not need to touch the split boundary.

Constraints:
- 2 <= n <= 200000
- -1000000000 <= nums[i] <= 1000000000
- The answer fits in a signed 64-bit integer
*/

using System;

public class Solution
{
    /*
        Time Complexity: O(n)
        Space Complexity: O(n)

        Why O(n)?
        - We make a constant number of linear passes over the array:
          1) Build best maximum subarray sums for every prefix
          2) Build best minimum subarray sums for every prefix
          3) Build best maximum subarray sums for every suffix
          4) Build best minimum subarray sums for every suffix
          5) Scan all split points once
        - Each pass is O(n), so total is still O(n).

        Why O(n) space?
        - We store four arrays of length n:
          prefixMax, prefixMin, suffixMax, suffixMin
        - Each array stores the best answer for a prefix or suffix ending/starting at each index.
    */
    public long MaximumDominanceScore(int[] nums)
    {
        int n = nums.Length;

        // These arrays will store, for every index:
        //
        // prefixMax[i] = maximum subarray sum found anywhere inside nums[0..i]
        // prefixMin[i] = minimum subarray sum found anywhere inside nums[0..i]
        // suffixMax[i] = maximum subarray sum found anywhere inside nums[i..n-1]
        // suffixMin[i] = minimum subarray sum found anywhere inside nums[i..n-1]
        //
        // Once we have these four arrays, every split becomes easy:
        // split after i means:
        //   left  = nums[0..i]
        //   right = nums[i+1..n-1]
        //
        // Then:
        //   candidate1 = |prefixMax[i] - suffixMin[i+1]|
        //   candidate2 = |prefixMin[i] - suffixMax[i+1]|
        //   score for this split = max(candidate1, candidate2)
        long[] prefixMax = new long[n];
        long[] prefixMin = new long[n];
        long[] suffixMax = new long[n];
        long[] suffixMin = new long[n];

        // ------------------------------------------------------------
        // STEP 1: Build prefixMax using Kadane's algorithm for maximum subarray sum.
        // ------------------------------------------------------------
        //
        // We want prefixMax[i] = best maximum subarray sum anywhere in nums[0..i].
        //
        // Standard Kadane idea:
        // - currentMaxEndingHere = best maximum subarray sum that MUST end at current index
        // - prefixMax[i]         = best maximum subarray sum seen anywhere so far
        //
        // Transition:
        // currentMaxEndingHere = max(nums[i], currentMaxEndingHere + nums[i])
        //
        // Why?
        // For a subarray ending at i, we have only two choices:
        // 1) start fresh at i
        // 2) extend the previous best subarray that ended at i-1
        //
        // Then prefixMax[i] = max(prefixMax[i-1], currentMaxEndingHere)
        long currentMaxEndingHere = nums[0];
        prefixMax[0] = nums[0];

        for (int i = 1; i < n; i++)
        {
            long value = nums[i];

            currentMaxEndingHere = Math.Max(value, currentMaxEndingHere + value);
            prefixMax[i] = Math.Max(prefixMax[i - 1], currentMaxEndingHere);
        }

        // ------------------------------------------------------------
        // STEP 2: Build prefixMin using the "minimum version" of Kadane's algorithm.
        // ------------------------------------------------------------
        //
        // We want prefixMin[i] = best minimum subarray sum anywhere in nums[0..i].
        //
        // Similar idea:
        // - currentMinEndingHere = best minimum subarray sum that MUST end at current index
        // - prefixMin[i]         = best minimum subarray sum seen anywhere so far
        //
        // Transition:
        // currentMinEndingHere = min(nums[i], currentMinEndingHere + nums[i])
        //
        // Why?
        // For a minimum-sum subarray ending at i, either:
        // 1) we start a new subarray at i
        // 2) we extend the previous minimum-ending subarray
        long currentMinEndingHere = nums[0];
        prefixMin[0] = nums[0];

        for (int i = 1; i < n; i++)
        {
            long value = nums[i];

            currentMinEndingHere = Math.Min(value, currentMinEndingHere + value);
            prefixMin[i] = Math.Min(prefixMin[i - 1], currentMinEndingHere);
        }

        // ------------------------------------------------------------
        // STEP 3: Build suffixMax from right to left.
        // ------------------------------------------------------------
        //
        // We want suffixMax[i] = maximum subarray sum found anywhere in nums[i..n-1].
        //
        // This is the same Kadane idea, but reversed.
        //
        // Let:
        // currentMaxStartingHere = best maximum subarray sum that MUST start at index i
        //
        // Transition when moving from right to left:
        // currentMaxStartingHere = max(nums[i], nums[i] + currentMaxStartingHere)
        //
        // Why?
        // A maximum subarray that starts at i either:
        // 1) is just nums[i]
        // 2) continues into the best maximum-starting subarray at i+1
        //
        // Then:
        // suffixMax[i] = max(suffixMax[i+1], currentMaxStartingHere)
        long currentMaxStartingHere = nums[n - 1];
        suffixMax[n - 1] = nums[n - 1];

        for (int i = n - 2; i >= 0; i--)
        {
            long value = nums[i];

            currentMaxStartingHere = Math.Max(value, value + currentMaxStartingHere);
            suffixMax[i] = Math.Max(suffixMax[i + 1], currentMaxStartingHere);
        }

        // ------------------------------------------------------------
        // STEP 4: Build suffixMin from right to left.
        // ------------------------------------------------------------
        //
        // We want suffixMin[i] = minimum subarray sum found anywhere in nums[i..n-1].
        //
        // Let:
        // currentMinStartingHere = best minimum subarray sum that MUST start at index i
        //
        // Transition:
        // currentMinStartingHere = min(nums[i], nums[i] + currentMinStartingHere)
        //
        // Then:
        // suffixMin[i] = min(suffixMin[i+1], currentMinStartingHere)
        long currentMinStartingHere = nums[n - 1];
        suffixMin[n - 1] = nums[n - 1];

        for (int i = n - 2; i >= 0; i--)
        {
            long value = nums[i];

            currentMinStartingHere = Math.Min(value, value + currentMinStartingHere);
            suffixMin[i] = Math.Min(suffixMin[i + 1], currentMinStartingHere);
        }

        // ------------------------------------------------------------
        // STEP 5: Try every valid split point.
        // ------------------------------------------------------------
        //
        // A valid split is after index i, where 0 <= i < n-1.
        //
        // Left side:
        //   nums[0..i]
        // Right side:
        //   nums[i+1..n-1]
        //
        // For this split, the problem asks us to consider two possibilities:
        //
        // A) left maximum vs right minimum
        //    |prefixMax[i] - suffixMin[i+1]|
        //
        // B) left minimum vs right maximum
        //    |prefixMin[i] - suffixMax[i+1]|
        //
        // The score of this split is the larger of those two.
        // We then keep the maximum score over all splits.
        long answer = long.MinValue;

        for (int i = 0; i < n - 1; i++)
        {
            long leftMax = prefixMax[i];
            long leftMin = prefixMin[i];
            long rightMax = suffixMax[i + 1];
            long rightMin = suffixMin[i + 1];

            long candidate1 = Math.Abs(leftMax - rightMin);
            long candidate2 = Math.Abs(leftMin - rightMax);

            long splitScore = Math.Max(candidate1, candidate2);
            answer = Math.Max(answer, splitScore);
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

int[] nums1 = { 2, -5, 4, -1, 3 };
long result1 = solution.MaximumDominanceScore(nums1);
Console.WriteLine(result1);

int[] nums2 = { 7, -2, -6, 5, -1, 4 };
long result2 = solution.MaximumDominanceScore(nums2);
Console.WriteLine(result2);

// Additional quick sanity checks
int[] nums3 = { 1, 2 };
Console.WriteLine(solution.MaximumDominanceScore(nums3));

int[] nums4 = { -3, -1, -2, -4 };
Console.WriteLine(solution.MaximumDominanceScore(nums4));