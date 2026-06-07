/*
 * Title: Partition Array into Balanced Halves by Digit Sum
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array `nums` of even length `n`. Your task is to partition
 * the array into two groups of exactly n/2 elements each, such that the absolute
 * difference between the total digit sum of the first group and the total digit sum
 * of the second group is minimized.
 *
 * The digit sum of a number is the sum of all its individual digits.
 * For example, the digit sum of 123 is 1 + 2 + 3 = 6.
 *
 * Return the minimum possible absolute difference between the total digit sums
 * of the two groups.
 *
 * Constraints:
 * - 2 <= nums.length <= 40 and nums.length is even
 * - 1 <= nums[i] <= 10^5
 *
 * Example 1:
 * Input: nums = [14, 21, 35, 9]
 * Output: 1
 *
 * Example 2:
 * Input: nums = [10, 22, 33, 45, 50, 67]
 * Output: 0
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// SOLUTION CLASS
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // =========================================================================
    // MinDifferenceByDigitSum
    //
    // Approach: Meet-in-the-Middle
    //
    // Why Meet-in-the-Middle?
    //   - n can be up to 40, so brute-force C(40,20) ≈ 137 billion — too slow.
    //   - We split the array into two halves of size n/2 each.
    //   - For each half we enumerate ALL subsets, recording (subset size, digit-sum).
    //   - Then we combine results from both halves:
    //       We need to pick exactly n/2 elements total across both halves.
    //       If we pick k elements from the LEFT half, we must pick (n/2 - k)
    //       from the RIGHT half.
    //   - For each left subset of size k with digit-sum S_L, we want a right
    //     subset of size (n/2 - k) whose digit-sum S_R makes |S_L - S_R| minimal,
    //     i.e., S_R as close to S_L as possible (since total diff = |2*S_L - totalSum|
    //     when we think of it globally — but we directly minimise |S_L - S_R|).
    //
    // Wait — let's be precise:
    //   totalDigitSum = sum of all digit-sums.
    //   Group1 digit-sum = G1, Group2 digit-sum = totalDigitSum - G1.
    //   |G1 - (totalDigitSum - G1)| = |2*G1 - totalDigitSum|.
    //   So we want G1 as close to totalDigitSum/2 as possible.
    //   G1 = (digit-sum of chosen left elements) + (digit-sum of chosen right elements).
    //   We enumerate all ways to pick exactly n/2 elements (k from left, n/2-k from right).
    //
    // Time Complexity:  O(2^(n/2) * log(2^(n/2))) = O(2^(n/2) * n/2)
    //                   For n=40 this is about 20 * 2^20 ≈ 20 million — very fast.
    // Space Complexity: O(2^(n/2)) for storing subset sums per size bucket.
    // =========================================================================
    public int MinDifferenceByDigitSum(int[] nums)
    {
        int n = nums.Length;
        int half = n / 2; // Each group must contain exactly this many elements.

        // ── Step 1: Compute digit sum for every element ──────────────────────
        // We convert each number to its digit sum once, so we don't repeat work.
        int[] ds = new int[n];
        for (int i = 0; i < n; i++)
        {
            ds[i] = DigitSum(nums[i]);
        }

        // ── Step 2: Compute the total digit sum of all elements ───────────────
        // This lets us express Group2's sum as (total - Group1's sum),
        // so the answer is |2 * G1 - total|, minimised over all valid G1.
        int total = 0;
        foreach (int d in ds) total += d;

        // ── Step 3: Split the digit-sum array into LEFT and RIGHT halves ──────
        // LEFT  = ds[0 .. half-1]   (first half of the array)
        // RIGHT = ds[half .. n-1]   (second half of the array)
        // We enumerate all 2^half subsets of each half independently.
        int leftSize  = half;       // number of elements in the left half
        int rightSize = n - half;   // number of elements in the right half (also half)

        // ── Step 4: Enumerate all subsets of the LEFT half ────────────────────
        // For each subset we record: how many elements it contains (count)
        //   and what their combined digit-sum is (sum).
        // We store results in a dictionary: count -> sorted list of sums.
        // The sorted list is needed later for binary search.
        var leftMap = new Dictionary<int, List<int>>(); // key = subset size
        int leftSubsets = 1 << leftSize; // 2^leftSize total subsets

        for (int mask = 0; mask < leftSubsets; mask++)
        {
            // Each bit in `mask` represents whether we include that element.
            int count = 0; // how many elements are in this subset
            int sum   = 0; // combined digit-sum of this subset

            for (int bit = 0; bit < leftSize; bit++)
            {
                if ((mask & (1 << bit)) != 0)
                {
                    count++;
                    sum += ds[bit]; // ds[bit] is the digit-sum of the bit-th left element
                }
            }

            // Store this (count, sum) pair in our map.
            if (!leftMap.ContainsKey(count))
                leftMap[count] = new List<int>();
            leftMap[count].Add(sum);
        }

        // ── Step 5: Sort each list in leftMap for binary search ───────────────
        // Binary search lets us quickly find the left sum closest to a target.
        foreach (var key in leftMap.Keys)
            leftMap[key].Sort();

        // ── Step 6: Enumerate all subsets of the RIGHT half ───────────────────
        // For each right subset of size r with digit-sum S_R:
        //   We need a left subset of size (half - r) with digit-sum S_L.
        //   Group1's total digit-sum = S_L + S_R.
        //   We want |2*(S_L + S_R) - total| minimised.
        //   Equivalently, we want S_L as close to (total/2 - S_R) as possible.
        //   target_SL = total/2.0 - S_R  (we work with integers, so we search
        //               for the closest integer in the sorted list).

        int answer = int.MaxValue; // We'll minimise this.
        int rightSubsets = 1 << rightSize; // 2^rightSize total subsets

        for (int mask = 0; mask < rightSubsets; mask++)
        {
            int rCount = 0; // number of elements chosen from the right half
            int rSum   = 0; // their combined digit-sum

            for (int bit = 0; bit < rightSize; bit++)
            {
                if ((mask & (1 << bit)) != 0)
                {
                    rCount++;
                    rSum += ds[leftSize + bit]; // offset by leftSize to index into ds[]
                }
            }

            // We need to pick (half - rCount) elements from the left half.
            int neededLeft = half - rCount;

            // If neededLeft is out of range, this combination is invalid.
            if (neededLeft < 0 || neededLeft > leftSize) continue;
            if (!leftMap.ContainsKey(neededLeft)) continue;

            // ── Step 7: Binary search for the best left sum ───────────────────
            // We want S_L such that 2*(S_L + rSum) - total is as close to 0 as possible.
            // Rearranging: S_L should be as close to (total - 2*rSum) / 2.0 as possible.
            // In integer terms, we search for the floor and ceiling of that value.
            List<int> candidates = leftMap[neededLeft];

            // The "ideal" left sum (as a double) that would make the difference 0.
            // We search for the nearest integer(s) in the sorted list.
            double ideal = (total - 2.0 * rSum) / 2.0;

            // Binary search: find the insertion point for `ideal` in the sorted list.
            int lo = 0, hi = candidates.Count - 1, pos = candidates.Count;
            while (lo <= hi)
            {
                int mid = (lo + hi) / 2;
                if (candidates[mid] >= ideal) // look left for a closer value
                {
                    pos = mid;
                    hi  = mid - 1;
                }
                else
                {
                    lo = mid + 1;
                }
            }

            // Check the candidate at `pos` and the one just before it (if they exist).
            // Both could be the closest to `ideal`.
            for (int p = pos - 1; p <= pos; p++)
            {
                if (p < 0 || p >= candidates.Count) continue;

                int lSum = candidates[p];
                // Group1 digit-sum = lSum + rSum
                // Group2 digit-sum = total - (lSum + rSum)
                // Absolute difference = |2*(lSum + rSum) - total|
                int diff = Math.Abs(2 * (lSum + rSum) - total);
                answer = Math.Min(answer, diff);

                // Early exit: can't do better than 0.
                if (answer == 0) return 0;
            }
        }

        return answer;
    }

    // ── Helper: Compute the digit sum of a non-negative integer ──────────────
    // e.g., DigitSum(123) = 1 + 2 + 3 = 6
    private int DigitSum(int num)
    {
        int sum = 0;
        while (num > 0)
        {
            sum += num % 10; // extract the last digit
            num  /= 10;      // remove the last digit
        }
        return sum;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DEMO / TEST CODE (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// nums = [14, 21, 35, 9]
// Digit sums: [5, 3, 8, 9]  total = 25
// Best partition: Group1=[14,35] → 5+8=13, Group2=[21,9] → 3+9=12
// |13 - 12| = 1  ✓
int[] nums1 = { 14, 21, 35, 9 };
int result1 = solution.MinDifferenceByDigitSum(nums1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input:    [{string.Join(", ", nums1)}]");
Console.WriteLine($"  Output:   {result1}");
Console.WriteLine($"  Expected: 1");
Console.WriteLine($"  Pass:     {result1 == 1}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// nums = [10, 22, 33, 45, 50, 67]
// Digit sums: [1, 4, 6, 9, 5, 13]  total = 38
// Best partition achieves difference 0 (two groups each summing to 19).
// e.g., Group1=[22,33,67] → 4+6+13=23? No. Let's check [10,22,67]=1+4+13=18, other=6+9+5=20 → diff=2
// Actually [33,45,50]=6+9+5=20, [10,22,67]=1+4+13=18 → diff=2
// [22,45,50]=4+9+5=18, [10,33,67]=1+6+13=20 → diff=2
// [10,45,67]=1+9+13=23, [22,33,50]=4+6+5=15 → diff=8
// [22,33,67]=4+6+13=23, [10,45,50]=1+9+5=15 → diff=8
// [10,33,45]=1+6+9=16, [22,50,67]=4+5+13=22 → diff=6
// [10,22,45]=1+4+9=14, [33,50,67]=6+5+13=24 → diff=10
// [10,22,33]=1+4+6=11, [45,50,67]=9+5+13=27 → diff=16
// [22,45,67]=4+9+13=26, [10,33,50]=1+6+5=12 → diff=14
// [10,50,67]=1+5+13=19, [22,33,45]=4+6+9=19 → diff=0  ✓
int[] nums2 = { 10, 22, 33, 45, 50, 67 };
int result2 = solution.MinDifferenceByDigitSum(nums2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input:    [{string.Join(", ", nums2)}]");
Console.WriteLine($"  Output:   {result2}");
Console.WriteLine($"  Expected: 0");
Console.WriteLine($"  Pass:     {result2 == 0}");
Console.WriteLine();

// ── Additional Edge Case: already balanced ────────────────────────────────────
// nums = [11, 22]  digit sums [2, 4]  total=6
// Only partition: Group1=[11], Group2=[22] → |2-4|=2
int[] nums3 = { 11, 22 };
int result3 = solution.MinDifferenceByDigitSum(nums3);
Console.WriteLine("Example 3 (edge case, n=2):");
Console.WriteLine($"  Input:    [{string.Join(", ", nums3)}]");
Console.WriteLine($"  Output:   {result3}");
Console.WriteLine($"  Expected: 2");
Console.WriteLine($"  Pass:     {result3 == 2}");
Console.WriteLine();

// ── Additional Edge Case: all equal digit sums ────────────────────────────────
// nums = [10, 10, 10, 10]  digit sums [1,1,1,1]  total=4
// Any partition gives |2-2|=0
int[] nums4 = { 10, 10, 10, 10 };
int result4 = solution.MinDifferenceByDigitSum(nums4);
Console.WriteLine("Example 4 (all equal digit sums):");
Console.WriteLine($"  Input:    [{string.Join(", ", nums4)}]");
Console.WriteLine($"  Output:   {result4}