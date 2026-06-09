/*
 * ============================================================
 * Title: Find the Single Displaced Element in a Rotated Sequence
 * ============================================================
 *
 * Problem Description:
 * You are given an array `nums` of `n` integers that was originally a sequence
 * of consecutive integers starting from 1 (i.e., [1, 2, 3, ..., n]).
 * The array was then rotated at some unknown pivot point, and exactly one element
 * was replaced with a value that does not belong to the original sequence.
 *
 * Your task is to find and return the displaced (replaced) element — the one
 * element in `nums` that does not fit the rotated consecutive sequence.
 *
 * A rotated consecutive sequence means the array looks like:
 *   [k, k+1, ..., n, 1, 2, ..., k-1] for some rotation point k.
 *
 * Constraints:
 *   - 2 <= n <= 10^4
 *   - 1 <= nums[i] <= 2 * n
 *   - Exactly one element is displaced (does not belong to the rotated sequence of 1 to n)
 *   - All other elements are distinct and form a valid rotated consecutive sequence
 *
 * Examples:
 *   Input:  [4, 5, 6, 7, 99, 1, 2, 3]  =>  Output: 99
 *   Input:  [3, 4, 0, 1, 2]            =>  Output: 0
 * ============================================================
 */

using System;
using System.Collections.Generic;

// ---------------------------------------------------------------
// Solution Class
// ---------------------------------------------------------------
public class Solution
{
    /*
     * Method: FindDisplacedElement
     *
     * Time Complexity:  O(n) — We iterate through the array a constant number of times.
     * Space Complexity: O(n) — We use a HashSet to store the expected values 1..n.
     *
     * High-Level Approach:
     * ---------------------------------------------------------------
     * The original (un-rotated) sequence contains exactly the integers {1, 2, 3, ..., n}.
     * After rotation, the same set of integers is present — just in a different order.
     * One element has been "displaced" (replaced), so the array contains n-1 of the
     * original integers plus one "intruder" value.
     *
     * Strategy:
     *   1. Build a HashSet of all expected values: {1, 2, ..., n}.
     *   2. Walk through nums[]. For each element:
     *        - If it IS in the expected set  → it's a legitimate value; remove it.
     *        - If it is NOT in the expected set → it's the displaced (intruder) element.
     *   3. Return the intruder the moment we find it.
     *
     * Why a HashSet?
     *   - O(1) average lookup and removal, keeping the overall algorithm O(n).
     *   - It naturally tracks which legitimate values have already been "used up",
     *     preventing a duplicate legitimate value from being mistaken for the intruder.
     *     (The problem guarantees all other elements are distinct, but being explicit
     *      about this makes the logic airtight.)
     *
     * Trace — Example 1: nums = [4, 5, 6, 7, 99, 1, 2, 3], n = 8
     *   expected = {1,2,3,4,5,6,7,8}
     *   i=0: 4  ∈ expected → remove 4  → {1,2,3,5,6,7,8}
     *   i=1: 5  ∈ expected → remove 5  → {1,2,3,6,7,8}
     *   i=2: 6  ∈ expected → remove 6  → {1,2,3,7,8}
     *   i=3: 7  ∈ expected → remove 7  → {1,2,3,8}
     *   i=4: 99 ∉ expected → RETURN 99  ✓
     *
     * Trace — Example 2: nums = [3, 4, 0, 1, 2], n = 5
     *   expected = {1,2,3,4,5}
     *   i=0: 3 ∈ expected → remove 3 → {1,2,4,5}
     *   i=1: 4 ∈ expected → remove 4 → {1,2,5}
     *   i=2: 0 ∉ expected → RETURN 0  ✓
     * ---------------------------------------------------------------
     */
    public int FindDisplacedElement(int[] nums)
    {
        // ── Step 1: Determine n (the length of the array). ──────────────────────
        // The original sequence was [1..n], so n equals the array length.
        int n = nums.Length;

        // ── Step 2: Build a HashSet of all "legitimate" values {1, 2, ..., n}. ──
        // We use a HashSet<int> because:
        //   • Contains() is O(1) average — fast membership test.
        //   • Remove()   is O(1) average — lets us "consume" each legitimate value
        //     so we don't accidentally count a value twice.
        HashSet<int> expected = new HashSet<int>(n);
        for (int v = 1; v <= n; v++)
        {
            // Add every integer from 1 to n into the set.
            // After this loop: expected = {1, 2, 3, ..., n}
            expected.Add(v);
        }

        // ── Step 3: Scan nums[] and identify the intruder. ──────────────────────
        // We iterate over every element in the input array exactly once.
        foreach (int num in nums)
        {
            // ── Step 3a: Check whether the current element is a legitimate value. ──
            // If `num` is in the expected set, it belongs to the original sequence.
            if (expected.Contains(num))
            {
                // This element is legitimate.
                // Remove it from the set so we don't accidentally match it again
                // (guards against hypothetical duplicates and keeps the set accurate).
                expected.Remove(num);
            }
            else
            {
                // ── Step 3b: The current element is NOT in the expected set. ────
                // This means `num` is either:
                //   (a) Out of range (< 1 or > n), like 0 or 99 in the examples, OR
                //   (b) A value in [1..n] that was already consumed (duplicate).
                // Either way, it does not belong to the rotated sequence → it's the
                // displaced element we are looking for.
                //
                // Return it immediately — the problem guarantees exactly one such element.
                return num;
            }
        }

        // ── Step 4: Fallback (should never be reached given valid input). ────────
        // The problem guarantees exactly one displaced element exists, so the loop
        // above will always return before reaching here.
        // We throw an exception to signal a contract violation rather than silently
        // returning a wrong answer.
        throw new InvalidOperationException(
            "No displaced element found — input does not satisfy problem constraints.");
    }
}

// ---------------------------------------------------------------
// Demo / Test Code (top-level statements)
// ---------------------------------------------------------------

Solution solution = new Solution();

// ── Test Case 1 ─────────────────────────────────────────────────
// Rotated sequence would be [4,5,6,7,8,1,2,3]; 99 replaced 8.
int[] nums1 = { 4, 5, 6, 7, 99, 1, 2, 3 };
int result1 = solution.FindDisplacedElement(nums1);
Console.WriteLine("=== Test Case 1 ===");
Console.WriteLine($"Input:    [{string.Join(", ", nums1)}]");
Console.WriteLine($"Output:   {result1}");
Console.WriteLine($"Expected: 99");
Console.WriteLine($"Pass:     {result1 == 99}");
Console.WriteLine();

// ── Test Case 2 ─────────────────────────────────────────────────
// Rotated sequence would be [3,4,5,1,2]; 0 replaced 5.
int[] nums2 = { 3, 4, 0, 1, 2 };
int result2 = solution.FindDisplacedElement(nums2);
Console.WriteLine("=== Test Case 2 ===");
Console.WriteLine($"Input:    [{string.Join(", ", nums2)}]");
Console.WriteLine($"Output:   {result2}");
Console.WriteLine($"Expected: 0");
Console.WriteLine($"Pass:     {result2 == 0}");
Console.WriteLine();

// ── Test Case 3: Displaced element at the very beginning ────────
// Rotated sequence would be [1,2,3,4,5]; 10 replaced 1.
int[] nums3 = { 10, 2, 3, 4, 5 };
int result3 = solution.FindDisplacedElement(nums3);
Console.WriteLine("=== Test Case 3 ===");
Console.WriteLine($"Input:    [{string.Join(", ", nums3)}]");
Console.WriteLine($"Output:   {result3}");
Console.WriteLine($"Expected: 10");
Console.WriteLine($"Pass:     {result3 == 10}");
Console.WriteLine();

// ── Test Case 4: Displaced element at the very end ──────────────
// Rotated sequence would be [3,4,5,1,2]; 7 replaced 2.
int[] nums4 = { 3, 4, 5, 1, 7 };
int result4 = solution.FindDisplacedElement(nums4);
Console.WriteLine("=== Test Case 4 ===");
Console.WriteLine($"Input:    [{string.Join(", ", nums4)}]");
Console.WriteLine($"Output:   {result4}");
Console.WriteLine($"Expected: 7");
Console.WriteLine($"Pass:     {result4 == 7}");
Console.WriteLine();

// ── Test Case 5: Minimum size array (n=2) ───────────────────────
// Rotated sequence would be [2,1]; 5 replaced 2.
int[] nums5 = { 5, 1 };
int result5 = solution.FindDisplacedElement(nums5);
Console.WriteLine("=== Test Case 5 ===");
Console.WriteLine($"Input:    [{string.Join(", ", nums5)}]");
Console.WriteLine($"Output:   {result5}");
Console.WriteLine($"Expected: 5");
Console.WriteLine($"Pass:     {result5 == 5}");
Console.WriteLine();

// ── Test Case 6: Displaced element is 0 (below valid range) ─────
// Rotated sequence would be [2,3,4,5,1]; 0 replaced 2.
int[] nums6 = { 0, 3, 4, 5, 1 };
int result6 = solution.FindDisplacedElement(nums6);
Console.WriteLine("=== Test Case 6 ===");
Console.WriteLine($"Input:    [{string.Join(", ", nums6)}]");
Console.WriteLine($"Output:   {result6}");
Console.WriteLine($"Expected: 0");
Console.WriteLine($"Pass:     {result6 == 0}");