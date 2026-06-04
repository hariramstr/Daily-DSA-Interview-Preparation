/*
 * Find the Longest Uniform Subarray After One Replacement
 * ========================================================
 * Given an integer array nums, you are allowed to replace at most one element
 * in the array with any value of your choice. Return the length of the longest
 * subarray where all elements are equal after performing at most one such replacement.
 *
 * A subarray is a contiguous part of the array.
 *
 * Example 1:
 *   Input:  nums = [1, 1, 2, 1, 1]
 *   Output: 5
 *   Explanation: Replace index 2 (value 2) with 1 → entire array is uniform.
 *
 * Example 2:
 *   Input:  nums = [3, 3, 5, 5, 5, 3]
 *   Output: 4
 *   Explanation: Replace index 3 (value 5) with 5 gives [5,5,5,5] of length 4.
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// ALGORITHM OVERVIEW
// ─────────────────────────────────────────────────────────────────────────────
// We use a SLIDING WINDOW approach.
//
// Key insight: For a window [left, right] to be a valid "uniform after one
// replacement" subarray, it must contain at most ONE distinct value that differs
// from the majority value — and that differing value can appear AT MOST ONCE
// (because we only get one replacement).
//
// More precisely, within the window we want:
//   (window length) - (count of the most frequent element in window) <= 1
//
// If the number of elements that are NOT the most frequent element is <= 1,
// we can replace that one "odd" element to make the whole window uniform.
//
// We iterate over all possible "target" values (the value we want the window
// to be filled with). For each target value, we slide a window and allow at
// most one element that is NOT the target value inside the window.
//
// Why iterate over each unique value?
//   Because the optimal answer's uniform value could be any value present in
//   the array. By trying each candidate value, we guarantee we find the best.
//
// Time Complexity:  O(n * k)  where n = array length, k = number of distinct values
//                  In the worst case k = n, giving O(n²), but for typical inputs
//                  with bounded values (1..10^4) and the sliding window being O(n)
//                  per value, it is efficient in practice.
//                  A pure O(n) solution also exists (see note at bottom).
// Space Complexity: O(k) for storing distinct values — O(1) extra per window pass.

public class Solution
{
    /// <summary>
    /// Returns the length of the longest uniform subarray achievable
    /// by replacing at most one element.
    /// 
    /// Time Complexity:  O(n * k) where k = number of distinct values in nums
    /// Space Complexity: O(k) for the set of distinct values
    /// </summary>
    public int LongestUniformSubarrayAfterOneReplacement(int[] nums)
    {
        // ── Step 1: Handle edge case ──────────────────────────────────────────
        // If the array has 0 or 1 element, it is already uniform.
        // Return its length immediately.
        if (nums == null || nums.Length == 0) return 0;
        if (nums.Length == 1) return 1;

        // ── Step 2: Collect all distinct values ───────────────────────────────
        // We need to try each distinct value as the "target" — the value we want
        // the entire window to equal. We only need to consider values that already
        // appear in the array, because replacing an element with a brand-new value
        // that never appears would never extend a run beyond what we can achieve
        // by using an existing value.
        //
        // Example: [1,1,2,1,1] — distinct values are {1, 2}.
        // We try target=1 and target=2 separately.
        var distinctValues = new HashSet<int>(nums);

        // ── Step 3: Track the global best answer ──────────────────────────────
        // We will update this as we find longer valid windows.
        int globalBest = 0;

        // ── Step 4: Sliding window for each target value ──────────────────────
        // For each candidate target value, we slide a window [left, right] over
        // the array. We allow at most ONE element inside the window that differs
        // from the target (that element will be our "one replacement").
        foreach (int target in distinctValues)
        {
            // 'left' is the left boundary of our current window (inclusive).
            int left = 0;

            // 'diffCount' tracks how many elements in the current window
            // are NOT equal to 'target'. We allow at most 1.
            int diffCount = 0;

            // Expand the window by moving 'right' one step at a time.
            for (int right = 0; right < nums.Length; right++)
            {
                // ── Step 4a: Include nums[right] in the window ────────────────
                // If the current element differs from our target, increment diffCount.
                // This element would need to be replaced to make the window uniform.
                if (nums[right] != target)
                {
                    diffCount++;
                }

                // ── Step 4b: Shrink window from the left if invalid ───────────
                // If diffCount exceeds 1, the window has more than one "bad" element.
                // We cannot fix it with a single replacement, so we must shrink
                // the window from the left until diffCount <= 1 again.
                //
                // We move 'left' rightward, and if the element we're removing was
                // a "bad" element (not equal to target), we decrement diffCount.
                while (diffCount > 1)
                {
                    // The element at 'left' is leaving the window.
                    if (nums[left] != target)
                    {
                        diffCount--;
                    }
                    // Advance the left boundary.
                    left++;
                }

                // ── Step 4c: Update the best answer ───────────────────────────
                // At this point, the window [left, right] is valid:
                //   - It contains at most 1 element that differs from 'target'.
                //   - We can replace that one element to make the whole window uniform.
                // The window length is (right - left + 1).
                int windowLength = right - left + 1;
                if (windowLength > globalBest)
                {
                    globalBest = windowLength;
                }
            }
            // After processing all positions for this target, reset for next target.
            // (left and diffCount are local to each foreach iteration via re-declaration)
        }

        // ── Step 5: Return the best length found ──────────────────────────────
        // This is the longest uniform subarray achievable with at most one replacement.
        return globalBest;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DEMO / TEST CODE
// ─────────────────────────────────────────────────────────────────────────────
// Trace verification:
//
// Example 1: nums = [1, 1, 2, 1, 1]
//   target=1: window expands, diffCount hits 1 at index 2 (value 2).
//             Window [0..4] has diffCount=1 → length 5. ✓
//   target=2: best window is [2..2] length 1 (or [2..2] with one replacement).
//   globalBest = 5. ✓
//
// Example 2: nums = [3, 3, 5, 5, 5, 3]
//   target=3: 
//     right=0 (3): diff=0, window=[0,0] len=1
//     right=1 (3): diff=0, window=[0,1] len=2
//     right=2 (5): diff=1, window=[0,2] len=3
//     right=3 (5): diff=2 → shrink: left=0→nums[0]=3,diff stays 2,left=1
//                           left=1→nums[1]=3,diff stays 2,left=2
//                           left=2→nums[2]=5,diff=1,left=3
//                           window=[3,3] len=1
//     right=4 (5): diff=2 → shrink: left=3→nums[3]=5,diff=1,left=4
//                           window=[4,4] len=1
//     right=5 (3): diff=1, window=[4,5] len=2
//     best for target=3: 3
//   target=5:
//     right=0 (3): diff=1, window=[0,0] len=1
//     right=1 (3): diff=2 → shrink: left=0→nums[0]=3,diff=1,left=1
//                           window=[1,1] len=1
//     right=2 (5): diff=1, window=[1,2] len=2
//     right=3 (5): diff=1, window=[1,3] len=3
//     right=4 (5): diff=1, window=[1,4] len=4  ← best!
//     right=5 (3): diff=2 → shrink: left=1→nums[1]=3,diff=1,left=2
//                           window=[2,5] len=4
//     best for target=5: 4
//   globalBest = 4. ✓

var solution = new Solution();

// ── Test Case 1 ──────────────────────────────────────────────────────────────
int[] nums1 = [1, 1, 2, 1, 1];
int result1 = solution.LongestUniformSubarrayAfterOneReplacement(nums1);
Console.WriteLine($"Example 1: nums = [1, 1, 2, 1, 1]");
Console.WriteLine($"  Expected: 5");
Console.WriteLine($"  Got:      {result1}");
Console.WriteLine($"  Pass: {result1 == 5}");
Console.WriteLine();

// ── Test Case 2 ──────────────────────────────────────────────────────────────
int[] nums2 = [3, 3, 5, 5, 5, 3];
int result2 = solution.LongestUniformSubarrayAfterOneReplacement(nums2);
Console.WriteLine($"Example 2: nums = [3, 3, 5, 5, 5, 3]");
Console.WriteLine($"  Expected: 4");
Console.WriteLine($"  Got:      {result2}");
Console.WriteLine($"  Pass: {result2 == 4}");
Console.WriteLine();

// ── Test Case 3: Already uniform array ───────────────────────────────────────
int[] nums3 = [7, 7, 7, 7];
int result3 = solution.LongestUniformSubarrayAfterOneReplacement(nums3);
Console.WriteLine($"Example 3: nums = [7, 7, 7, 7]  (already uniform)");
Console.WriteLine($"  Expected: 4");
Console.WriteLine($"  Got:      {result3}");
Console.WriteLine($"  Pass: {result3 == 4}");
Console.WriteLine();

// ── Test Case 4: Single element ───────────────────────────────────────────────
int[] nums4 = [42];
int result4 = solution.LongestUniformSubarrayAfterOneReplacement(nums4);
Console.WriteLine($"Example 4: nums = [42]  (single element)");
Console.WriteLine($"  Expected: 1");
Console.WriteLine($"  Got:      {result4}");
Console.WriteLine($"  Pass: {result4 == 1}");
Console.WriteLine();

// ── Test Case 5: All different elements ──────────────────────────────────────
int[] nums5 = [1, 2, 3, 4, 5];
int result5 = solution.LongestUniformSubarrayAfterOneReplacement(nums5);
Console.WriteLine($"Example 5: nums = [1, 2, 3, 4, 5]  (all different)");
Console.WriteLine($"  Expected: 2");
Console.WriteLine($"  Got:      {result5}");
Console.WriteLine($"  Pass: {result5 == 2}");
Console.WriteLine();

// ── Test Case 6: Two elements ─────────────────────────────────────────────────
int[] nums6 = [1, 2];
int result6 = solution.LongestUniformSubarrayAfterOneReplacement(nums6);
Console.WriteLine($"Example 6: nums = [1, 2]");
Console.WriteLine($"  Expected: 2");
Console.WriteLine($"  Got:      {result6}");
Console.WriteLine($"  Pass: {result6 == 2}");
Console.WriteLine();

// ── Test Case 7: Replacement at the boundary ─────────────────────────────────
int[] nums7 = [2, 1, 1, 1, 1];
int result7 = solution.LongestUniformSubarrayAfterOneReplacement(nums7);
Console.WriteLine($"Example 7: nums = [2, 1, 1, 1, 1]  (replace first element)");
Console.WriteLine($"  Expected: 5");
Console.WriteLine($"  Got:      {result7}");
Console.WriteLine($"  Pass: {result7 == 5}");