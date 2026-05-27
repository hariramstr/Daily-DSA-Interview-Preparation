/*
 * ============================================================
 * Title: Checkered Flag Bit Counter
 * Difficulty: Easy
 * Topic: Bit Manipulation
 * ============================================================
 * Problem Description:
 * You are given a list of race car IDs represented as non-negative integers.
 * A race car is considered 'flagged' if the number of set bits (1s) in its
 * binary representation is strictly greater than the number of unset bits (0s)
 * when considering only the bits from position 0 up to the position of the
 * most significant bit (inclusive).
 *
 * For example:
 *   - 7  → binary '111'  → 3 set bits, 0 unset bits → flagged
 *   - 5  → binary '101'  → 2 set bits, 1 unset bit  → flagged
 *   - 4  → binary '100'  → 1 set bit,  2 unset bits → NOT flagged
 *   - 0  → special case  → NOT flagged (no set bits)
 *
 * Given an integer array carIds, return the count of flagged car IDs.
 *
 * Constraints:
 *   - 1 <= carIds.length <= 10^4
 *   - 0 <= carIds[i] <= 10^6
 *   - The number 0 is considered NOT flagged.
 *
 * Examples:
 *   Input:  [7, 5, 4, 6, 1]  → Output: 4
 *   Input:  [0, 2, 8, 15]    → Output: 1
 * ============================================================
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Counts how many car IDs in the array are "flagged".
    /// A car ID is flagged when its count of 1-bits (set bits) is
    /// STRICTLY GREATER than its count of 0-bits, considering only
    /// the bits from bit-position 0 up to the most significant bit.
    ///
    /// Time Complexity:  O(n * log(max_value))
    ///   - n = number of car IDs in the array
    ///   - log(max_value) ≈ 20 for values up to 10^6 (since 2^20 > 10^6)
    ///   - For each car ID we inspect at most ~20 bits
    ///
    /// Space Complexity: O(1)
    ///   - We only use a fixed number of integer variables regardless of input size
    /// </summary>
    public int CountFlaggedCars(int[] carIds)
    {
        // ── Step 1: Initialize the result counter ──────────────────────────
        // We will increment this every time we find a flagged car ID.
        int flaggedCount = 0;

        // ── Step 2: Iterate over every car ID in the input array ───────────
        // We need to evaluate each car ID independently.
        foreach (int carId in carIds)
        {
            // ── Step 3: Handle the special case of 0 ──────────────────────
            // The problem explicitly states that 0 is NOT flagged.
            // Also, our bit-counting loop below would never execute for 0
            // (since 0 has no most-significant bit), so this guard is both
            // a correctness requirement and a clarity aid.
            if (carId == 0)
            {
                // 0 is never flagged — skip to the next car ID
                continue;
            }

            // ── Step 4: Find the total number of bits to consider ──────────
            // We only look at bits from position 0 up to the most significant
            // bit (MSB). The total bit-width is: floor(log2(carId)) + 1.
            //
            // Example: carId = 6 → binary '110'
            //   floor(log2(6)) = 2, so total bits = 3  ✓
            //
            // We compute this by finding the position of the highest set bit.
            // Bit-shifting right until the value becomes 0 tells us how many
            // bit positions exist.
            int totalBits = 0;
            int temp = carId;

            // Keep shifting right by 1 (dividing by 2) until temp reaches 0.
            // Each shift represents moving past one bit position.
            while (temp > 0)
            {
                totalBits++; // count this bit position
                temp >>= 1;  // shift right: move to the next higher bit
            }
            // After this loop, totalBits = number of bits from bit-0 to MSB.
            // Example: carId=6 (binary 110) → totalBits=3

            // ── Step 5: Count the number of SET bits (1s) ─────────────────
            // We use the built-in BitOperations.PopCount or a manual approach.
            // Here we use a classic bit-trick: (n & (n-1)) clears the lowest
            // set bit. We count how many times we can do this before n = 0.
            //
            // Alternatively, we count bits by checking each bit with a mask.
            // We'll use the straightforward mask approach for clarity:
            int setBits = 0;
            int value = carId;

            // Check each bit from position 0 to (totalBits - 1).
            // The mask starts at 1 (binary ...0001) and shifts left each iteration.
            for (int bit = 0; bit < totalBits; bit++)
            {
                // (value & 1) isolates the least significant bit:
                //   result is 1 if that bit is set, 0 if it is unset.
                if ((value & 1) == 1)
                {
                    setBits++; // this bit is a 1 → increment set-bit counter
                }
                // Shift value right to examine the next bit in the next iteration
                value >>= 1;
            }

            // ── Step 6: Compute the number of UNSET bits (0s) ─────────────
            // Within the bit-width we care about (totalBits), the unset bits
            // are simply the remaining positions that are NOT set bits.
            //
            // unsetBits = totalBits - setBits
            //
            // Example: carId=5 (binary '101'), totalBits=3, setBits=2
            //   unsetBits = 3 - 2 = 1  ✓
            int unsetBits = totalBits - setBits;

            // ── Step 7: Apply the flagging condition ───────────────────────
            // A car is flagged if setBits > unsetBits (strictly greater than).
            // This is equivalent to: setBits > totalBits / 2
            // (i.e., more than half the bits within the MSB-width are 1s)
            if (setBits > unsetBits)
            {
                flaggedCount++; // this car ID is flagged!
            }

            // ── Trace through examples for verification ────────────────────
            // carId=7  → binary '111'  → totalBits=3, setBits=3, unsetBits=0 → 3>0 ✓ flagged
            // carId=5  → binary '101'  → totalBits=3, setBits=2, unsetBits=1 → 2>1 ✓ flagged
            // carId=4  → binary '100'  → totalBits=3, setBits=1, unsetBits=2 → 1>2 ✗ not flagged
            // carId=6  → binary '110'  → totalBits=3, setBits=2, unsetBits=1 → 2>1 ✓ flagged
            // carId=1  → binary '1'    → totalBits=1, setBits=1, unsetBits=0 → 1>0 ✓ flagged
            // → Total flagged for [7,5,4,6,1] = 4  ✓ matches expected output
            //
            // carId=0  → special case → not flagged
            // carId=2  → binary '10'  → totalBits=2, setBits=1, unsetBits=1 → 1>1 ✗ not flagged
            // carId=8  → binary '1000'→ totalBits=4, setBits=1, unsetBits=3 → 1>3 ✗ not flagged
            // carId=15 → binary '1111'→ totalBits=4, setBits=4, unsetBits=0 → 4>0 ✓ flagged
            // → Total flagged for [0,2,8,15] = 1  ✓ matches expected output
        }

        // ── Step 8: Return the final count of flagged car IDs ─────────────
        return flaggedCount;
    }
}

// ─────────────────────────────────────────────────────────────
// Demo / Driver Code (top-level statements)
// ─────────────────────────────────────────────────────────────

Console.WriteLine("==============================================");
Console.WriteLine("       Checkered Flag Bit Counter Demo        ");
Console.WriteLine("==============================================");

Solution solution = new Solution();

// ── Example 1 ──────────────────────────────────────────────────
// Input:  [7, 5, 4, 6, 1]
// Expected Output: 4
// Breakdown:
//   7  → '111'  → 3 set, 0 unset → flagged
//   5  → '101'  → 2 set, 1 unset → flagged
//   4  → '100'  → 1 set, 2 unset → NOT flagged
//   6  → '110'  → 2 set, 1 unset → flagged
//   1  → '1'    → 1 set, 0 unset → flagged
//   Flagged count = 4
int[] example1 = { 7, 5, 4, 6, 1 };
int result1 = solution.CountFlaggedCars(example1);
Console.WriteLine($"\nExample 1:");
Console.WriteLine($"  Input:    [{string.Join(", ", example1)}]");
Console.WriteLine($"  Output:   {result1}");
Console.WriteLine($"  Expected: 4");
Console.WriteLine($"  Pass:     {(result1 == 4 ? "✓ YES" : "✗ NO")}");

// ── Example 2 ──────────────────────────────────────────────────
// Input:  [0, 2, 8, 15]
// Expected Output: 1
// Breakdown:
//   0  → special case → NOT flagged
//   2  → '10'   → 1 set, 1 unset → NOT flagged (not strictly greater)
//   8  → '1000' → 1 set, 3 unset → NOT flagged
//   15 → '1111' → 4 set, 0 unset → flagged
//   Flagged count = 1
int[] example2 = { 0, 2, 8, 15 };
int result2 = solution.CountFlaggedCars(example2);
Console.WriteLine($"\nExample 2:");
Console.WriteLine($"  Input:    [{string.Join(", ", example2)}]");
Console.WriteLine($"  Output:   {result2}");
Console.WriteLine($"  Expected: 1");
Console.WriteLine($"  Pass:     {(result2 == 1 ? "✓ YES" : "✗ NO")}");

// ── Additional Edge Cases ───────────────────────────────────────

// Edge case: single element, 0
int[] edge1 = { 0 };
int resultEdge1 = solution.CountFlaggedCars(edge1);
Console.WriteLine($"\nEdge Case 1 (single zero):");
Console.WriteLine($"  Input:    [0]");
Console.WriteLine($"  Output:   {resultEdge1}");
Console.WriteLine($"  Expected: 0");
Console.WriteLine($"  Pass:     {(resultEdge1 == 0 ? "✓ YES" : "✗ NO")}");

// Edge case: single element, 1 (binary '1' → 1 set, 0 unset → flagged)
int[] edge2 = { 1 };
int resultEdge2 = solution.CountFlaggedCars(edge2);
Console.WriteLine($"\nEdge Case 2 (single one):");
Console.WriteLine($"  Input:    [1]");
Console.WriteLine($"  Output:   {resultEdge2}");
Console.WriteLine($"  Expected: 1");
Console.WriteLine($"  Pass:     {(resultEdge2 == 1 ? "✓ YES" : "✗ NO")}");

// Edge case: large value near constraint max
// 1000000 in binary: let's check
// 1000000 = 0b11110100001001000000 → 20 bits
// Set bits: count of 1s in 11110100001001000000
// = 1+1+1+1+0+1+0+0+0+0+1+0+0+1+0+0+0+0+0+0 = 7 set bits, 13 unset bits → NOT flagged
int[] edge3 = { 1000000 };
int resultEdge3 = solution.CountFlaggedCars(edge3);
Console.WriteLine($"\nEdge Case 3 (value = 1,000,000):");
Console.WriteLine($"  Input:    [1000000]");
Console.WriteLine($"  Binary:   {Convert.ToString(1000000, 2)}");
Console.WriteLine($"  Output:   {resultEdge3}");
Console.WriteLine($"  Expected: 0  (7 set bits, 13 unset bits → not flagged)");
Console.WriteLine($"  Pass:     {(resultEdge3 == 0 ? "✓ YES" : "✗ NO")}");

// Edge case: all flagged
// 3 → '11' → 2 set, 0 unset → flagged
// 7 → '111'→ 3 set, 0 unset → flagged
// 15→ '1111'→4 set, 0 unset → flagged
int[] edge4 = { 3, 7, 15 };
int resultEdge4 = solution.CountFlaggedCars(edge4);
Console.WriteLine($"\nEdge Case 4 (all flagged):");
Console.WriteLine($"  Input:    [{string.Join(", ", edge4)}]");
Console.WriteLine($"  Output:   {resultEdge4}");
Console.WriteLine($"  Expected: 3");
Console.WriteLine($"  Pass:     {(resultEdge4 == 3 ? "✓ YES" : "✗ NO")}");

Console.WriteLine("\n==============================================");
Console.WriteLine("                 Demo Complete               ");
Console.WriteLine("==============================================");