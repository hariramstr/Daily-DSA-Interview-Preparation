/*
 * Title: Reconstruct Array from Bitwise OR Pairs
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an integer `n` and a 2D array `pairs` where each `pairs[i] = [index_i, value_i]`
 * represents that the bitwise OR of the element at position `index_i` with some unknown base value `B`
 * equals `value_i`. In other words, `arr[index_i] | B == value_i` for all pairs.
 *
 * Your task is to reconstruct the array `arr` of length `n` such that:
 * 1. There exists a non-negative integer `B` consistent with all the given pairs.
 * 2. `arr[index_i] | B == value_i` for every pair.
 * 3. Each `arr[i]` is minimized (i.e., use the smallest valid value for positions not constrained).
 *
 * If no valid array exists that satisfies all constraints simultaneously, return an empty array.
 *
 * Positions not referenced in any pair should be set to 0.
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ============================================================
// SOLUTION CLASS
// ============================================================
public class Solution
{
    /*
     * Method: ReconstructArray
     *
     * Time Complexity:  O(P * log(MaxVal)) where P = number of pairs, MaxVal = max value (~10^6)
     *                   The bit-scanning loop runs at most 20 iterations (log2(10^6) ≈ 20).
     * Space Complexity: O(n + P) for storing the grouped pairs and the result array.
     *
     * HIGH-LEVEL APPROACH:
     * --------------------
     * The key insight is understanding what `arr[i] | B = value_i` means in terms of bits:
     *
     *   - Bits that are SET in B must also be SET in value_i  (because OR with 1 always gives 1,
     *     so if B has a bit set, value_i must have that bit set too).
     *   - Bits that are SET in value_i but NOT in B must come from arr[i].
     *   - Bits that are NOT set in value_i cannot be set in B (otherwise OR would set them).
     *
     * So:
     *   B must be a subset of every value_i (all bits of B appear in every value_i).
     *   arr[i] = value_i XOR B  ... wait, not exactly. Let's think more carefully.
     *
     *   arr[i] | B = value_i
     *   => every bit in B is in value_i  (necessary condition)
     *   => every bit in value_i that is NOT in B must be in arr[i]
     *   => bits in arr[i] that ARE in B don't matter (OR absorbs them)
     *   => to MINIMIZE arr[i], we set arr[i] = value_i & ~B  (only the bits NOT covered by B)
     *
     * Steps:
     * 1. Group pairs by index. If the same index has two different values, return [] immediately.
     * 2. Determine the MAXIMUM possible B:
     *    B_max = AND of all value_i across all pairs.
     *    (B can only have bits that appear in EVERY value_i, otherwise some constraint fails.)
     * 3. B must be a SUBSET of B_max. We want to find the largest valid B to minimize arr[i].
     *    Actually, to minimize arr[i] = value_i & ~B, we want B to be as LARGE as possible
     *    (more bits in B means fewer bits left for arr[i]).
     *    The largest valid B is B_max itself, but we must verify it's consistent:
     *    For each pair (idx, val): (arr[idx] | B) must equal val.
     *    With arr[idx] = val & ~B, check: (val & ~B) | B == val.
     *    This simplifies to: (val & ~B) | B = val | B... hmm let's verify algebraically.
     *    (val & ~B) | B = val | B  (by absorption/distribution).
     *    We need val | B == val, which means B must be a subset of val (all bits of B in val).
     *    That's exactly our condition B ⊆ every val_i, which B_max satisfies by construction!
     * 4. So B = B_max (AND of all values) is the best choice.
     * 5. For each constrained index: arr[i] = value_i & ~B
     * 6. For unconstrained indices: arr[i] = 0
     * 7. Final validation: for each pair, confirm arr[idx] | B == value.
     */
    public int[] ReconstructArray(int n, int[][] pairs)
    {
        // -------------------------------------------------------
        // STEP 1: Group pairs by index and detect immediate conflicts.
        // If the same index appears with two DIFFERENT values, it's impossible
        // because arr[idx] | B can only equal one value at a time.
        // -------------------------------------------------------

        // Dictionary maps each index to the single value it must produce with B.
        var indexToValue = new Dictionary<int, int>();

        foreach (var pair in pairs)
        {
            int idx = pair[0];
            int val = pair[1];

            if (indexToValue.ContainsKey(idx))
            {
                // This index was already seen. Check if the value matches.
                if (indexToValue[idx] != val)
                {
                    // Contradiction: same index, different required OR results.
                    // No valid B can satisfy both simultaneously.
                    Console.WriteLine($"  [Debug] Conflict at index {idx}: " +
                                      $"previously {indexToValue[idx]}, now {val}. Returning [].");
                    return Array.Empty<int>();
                }
                // Same value again — redundant pair, no problem.
            }
            else
            {
                indexToValue[idx] = val;
            }
        }

        // -------------------------------------------------------
        // STEP 2: Compute B_max = AND of all values.
        //
        // Why AND? Because B can only have a bit set if EVERY value has that bit set.
        // If even one value is missing a bit, B cannot have that bit (otherwise
        // arr[idx] | B would set that bit in the result, making it != value).
        //
        // If there are no pairs at all, B can be anything (we choose B = 0 to minimize arr).
        // -------------------------------------------------------

        int B;

        if (indexToValue.Count == 0)
        {
            // No constraints at all — B = 0, all arr[i] = 0.
            B = 0;
        }
        else
        {
            // Start with all bits set (identity for AND), then AND with each value.
            B = ~0; // All bits set (in two's complement, this is -1 / 0xFFFFFFFF)

            foreach (var kvp in indexToValue)
            {
                int val = kvp.Value;
                // AND narrows down which bits can possibly be in B.
                // After this loop, B contains only bits present in ALL values.
                B &= val;
            }

            // Since values are non-negative and at most 10^6 (< 2^20),
            // mask B to non-negative 20-bit range to avoid sign issues.
            B &= 0xFFFFF; // Keep only lower 20 bits (covers up to ~1,048,575)
        }

        Console.WriteLine($"  [Debug] Computed B = {B} (binary: {Convert.ToString(B, 2)})");

        // -------------------------------------------------------
        // STEP 3: Validate B against all constraints.
        //
        // For each (idx, val) pair, verify that:
        //   (val & ~B) | B == val
        // This is equivalent to checking B is a subset of val (B & val == B),
        // which should already hold by construction, but we double-check.
        //
        // Also verify: arr[idx] | B == val, where arr[idx] = val & ~B.
        // -------------------------------------------------------

        foreach (var kvp in indexToValue)
        {
            int idx = kvp.Key;
            int val = kvp.Value;

            // Compute what arr[idx] would be: bits in val NOT covered by B.
            // We minimize arr[idx] by only including bits that B doesn't already provide.
            int arrVal = val & ~B;

            // Now verify the OR result matches the required value.
            int orResult = arrVal | B;

            if (orResult != val)
            {
                // This shouldn't happen with correct B computation, but guard anyway.
                Console.WriteLine($"  [Debug] Validation failed at index {idx}: " +
                                  $"arr[{idx}]={arrVal}, B={B}, OR={orResult}, expected={val}");
                return Array.Empty<int>();
            }

            Console.WriteLine($"  [Debug] Index {idx}: arr[{idx}]={arrVal} | B={B} = {orResult} ✓");
        }

        // -------------------------------------------------------
        // STEP 4: Build the result array.
        //
        // - For constrained indices: arr[i] = value_i & ~B
        //   (the bits in value_i that B doesn't already contribute)
        // - For unconstrained indices: arr[i] = 0
        //   (problem says minimize; 0 is the smallest non-negative integer)
        // -------------------------------------------------------

        int[] result = new int[n]; // Initialized to 0 by default in C#.

        foreach (var kvp in indexToValue)
        {
            int idx = kvp.Key;
            int val = kvp.Value;

            // arr[idx] gets only the bits from val that B doesn't supply.
            // This is the MINIMUM value for arr[idx] consistent with the constraint.
            result[idx] = val & ~B;
        }

        // -------------------------------------------------------
        // STEP 5: Return the reconstructed array.
        // -------------------------------------------------------
        return result;
    }
}

// ============================================================
// DEMO / TEST CODE (Top-Level Statements)
// ============================================================

var solution = new Solution();

Console.WriteLine("==============================================");
Console.WriteLine("  Reconstruct Array from Bitwise OR Pairs");
Console.WriteLine("==============================================\n");

// ------------------------------------------------------------------
// Example 1:
// n = 4, pairs = [[0,7],[1,5],[2,6]]
// Expected Output: [3, 1, 2, 0]
//
// Trace:
//   indexToValue = {0->7, 1->5, 2->6}
//   B = 7 & 5 & 6 = 0b111 & 0b101 & 0b110 = 0b100 = 4
//   arr[0] = 7 & ~4 = 0b111 & 0b011 = 0b011 = 3  => 3|4=7 ✓
//   arr[1] = 5 & ~4 = 0b101 & 0b011 = 0b001 = 1  => 1|4=5 ✓
//   arr[2] = 6 & ~4 = 0b110 & 0b011 = 0b010 = 2  => 2|4=6 ✓
//   arr[3] = 0 (unconstrained)
//   Result: [3, 1, 2, 0] ✓
// ------------------------------------------------------------------
Console.WriteLine("--- Example 1 ---");
Console.WriteLine("Input: n=4, pairs=[[0,7],[1,5],[2,6]]");
Console.WriteLine("Expected: [3, 1, 2, 0]");
int[] result1 = solution.ReconstructArray(4, new int[][] {
    new int[] { 0, 7 },
    new int[] { 1, 5 },
    new int[] { 2, 6 }
});
Console.WriteLine($"Output:   [{string.Join(", ", result1)}]");
Console.WriteLine();

// ------------------------------------------------------------------
// Example 2:
// n = 3, pairs = [[0,5],[0,3]]
// Expected Output: []
//
// Trace:
//   First pair: indexToValue[0] = 5
//   Second pair: indexToValue[0] already = 5, but new value = 3 ≠ 5 → conflict!
//   Return []
// ------------------------------------------------------------------
Console.WriteLine("--- Example 2 ---");
Console.WriteLine("Input: n=3, pairs=[[0,5],[0,3]]");
Console.WriteLine("Expected: []");
int[] result2 = solution.ReconstructArray(3, new int[][] {
    new int[] { 0, 5 },
    new int[] { 0, 3 }
});
Console.WriteLine($"Output:   [{string.Join(", ", result2)}]");
Console.WriteLine();

// ------------------------------------------------------------------
// Example 3 (Edge Case): No pairs at all.
// n = 3, pairs = []
// Expected: [0, 0, 0]  (all zeros, B=0, no constraints)
// ------------------------------------------------------------------
Console.WriteLine("--- Example 3 (No pairs) ---");
Console.WriteLine("Input: n=3, pairs=[]");
Console.WriteLine("Expected: [0, 0, 0]");
int[] result3 = solution.ReconstructArray(3, Array.Empty<int[]>());
Console.WriteLine($"Output:   [{string.Join(", ", result3)}]");
Console.WriteLine();

// ------------------------------------------------------------------
// Example 4 (Single pair):
// n = 2, pairs = [[0, 6]]
// B = 6 (AND of just one value = 6 itself)
// arr[0] = 6 & ~6 = 0  => 0|6=6 ✓
// arr[1] = 0 (unconstrained)
// Result: [0, 0]
// ------------------------------------------------------------------
Console.WriteLine("--- Example 4 (Single pair) ---");
Console.WriteLine("Input: n=2, pairs=[[0,6]]");
Console.WriteLine("Expected: [0, 0]");
int[] result4 = solution.ReconstructArray(2, new int[][] {
    new int[] { 0, 6 }
});
Console.WriteLine($"Output:   [{string.Join(", ", result4)}]");
Console.WriteLine();

// ------------------------------------------------------------------
// Example 5 (Duplicate consistent pairs):
// n = 2, pairs = [[0,7],[0,7],[1,5]]
// Both pairs for index 0 have same value 7 → no conflict.
// B = 7 & 7 & 5 = 5
// arr[0] = 7 & ~5 = 0b111 & 0b010 = 2  => 2|5=7 ✓
// arr[1] = 5 & ~5 = 0  => 0|5=5 ✓
// Result: [2, 0]
// ------------------------------------------------------------------
Console.WriteLine("--- Example 5 (Duplicate consistent pairs) ---");
Console.WriteLine("Input: n=2, pairs=[[0,7],[0,7],[1,5]]");
Console.WriteLine("Expected: [2, 0]");
int[] result5 = solution.ReconstructArray(2, new int[][] {
    new int[] { 0, 7 },
    new int[] { 0, 7 },
    new int[] { 1, 5 }
});
Console.WriteLine($"Output:   [{string.Join(", ", result5)}]");
Console.WriteLine();

Console.WriteLine("==============================================");
Console.WriteLine("  All examples completed.");
Console.WriteLine("==============================================");