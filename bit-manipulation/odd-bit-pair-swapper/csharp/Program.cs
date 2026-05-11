/*
 * Title: Odd Bit Pair Swapper
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an array of non-negative integers `nums`. Your task is to transform
 * each integer in the array by swapping every pair of adjacent bits. Specifically,
 * for each integer, the bit at position 0 swaps with the bit at position 1, the bit
 * at position 2 swaps with the bit at position 3, and so on for all 32 bits.
 *
 * After transforming all integers, return the count of pairs (i, j) where i < j
 * such that the XOR of nums[i] and nums[j] (after transformation) has exactly k bits
 * set to 1.
 *
 * Constraints:
 * - 1 <= nums.length <= 1000
 * - 0 <= nums[i] <= 2^30 - 1
 * - 0 <= k <= 30
 */

using System;
using System.Collections.Generic;

/// <summary>
/// Solution class encapsulating the Odd Bit Pair Swapper algorithm.
/// </summary>
public class Solution
{
    /// <summary>
    /// Counts pairs (i, j) where i &lt; j such that XOR of transformed nums[i] and nums[j]
    /// has exactly k bits set to 1.
    ///
    /// Time Complexity:  O(n^2) — we examine every unique pair of elements once.
    /// Space Complexity: O(n)   — we store the transformed array of n elements.
    /// </summary>
    /// <param name="nums">Array of non-negative integers to process.</param>
    /// <param name="k">Exact number of set bits required in the XOR result.</param>
    /// <returns>Count of qualifying pairs.</returns>
    public int CountPairsWithKBitsXor(int[] nums, int k)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Transform each number by swapping adjacent bit pairs.
        // -----------------------------------------------------------------------
        // We need to swap bits at positions (0,1), (2,3), (4,5), ... for every
        // 32-bit integer.
        //
        // The classic bit-manipulation trick for this uses two masks:
        //   - 0xAAAAAAAA (binary: 10101010...10) selects all ODD-position bits.
        //   - 0x55555555 (binary: 01010101...01) selects all EVEN-position bits.
        //
        // To swap adjacent pairs:
        //   1. Isolate the odd-position bits:  oddBits  = n & 0xAAAAAAAA
        //   2. Isolate the even-position bits: evenBits = n & 0x55555555
        //   3. Shift odd bits RIGHT by 1  → they move to even positions.
        //   4. Shift even bits LEFT  by 1 → they move to odd positions.
        //   5. OR the two shifted values together to get the swapped result.
        //
        // Why use uint? Because C# right-shifts signed integers with sign extension
        // (arithmetic shift), which would corrupt the high bits. Casting to uint
        // ensures a logical (zero-filling) right shift.

        int n = nums.Length;
        int[] transformed = new int[n]; // Stores the bit-swapped version of each number.

        for (int idx = 0; idx < n; idx++)
        {
            // Cast to uint so the >> operator performs a logical (unsigned) right shift,
            // preventing sign-bit propagation that would corrupt our result.
            uint val = (uint)nums[idx];

            // Isolate all bits that sit at ODD positions (1, 3, 5, ...):
            // 0xAAAAAAAA = 1010 1010 1010 1010 1010 1010 1010 1010 in binary.
            uint oddBits = val & 0xAAAAAAAAu;

            // Isolate all bits that sit at EVEN positions (0, 2, 4, ...):
            // 0x55555555 = 0101 0101 0101 0101 0101 0101 0101 0101 in binary.
            uint evenBits = val & 0x55555555u;

            // Move odd-position bits DOWN by 1 → they now occupy even positions.
            // Move even-position bits UP   by 1 → they now occupy odd positions.
            // Combining with OR gives us the fully swapped integer.
            uint swapped = (oddBits >> 1) | (evenBits << 1);

            // Store as int (safe because our inputs are <= 2^30 - 1, so the
            // transformed value also fits within a non-negative 32-bit integer).
            transformed[idx] = (int)swapped;
        }

        // -----------------------------------------------------------------------
        // STEP 2: Count all pairs (i, j) with i < j whose XOR has exactly k set bits.
        // -----------------------------------------------------------------------
        // We use a straightforward O(n^2) double loop because n <= 1000, making
        // the maximum number of pairs 1000*999/2 = 499,500 — very manageable.
        //
        // For each pair we:
        //   a) Compute the XOR of the two transformed values.
        //   b) Count the number of 1-bits in that XOR (popcount / Hamming weight).
        //   c) If the popcount equals k, increment our answer counter.

        int pairCount = 0; // Running total of qualifying pairs.

        for (int i = 0; i < n - 1; i++)
        {
            for (int j = i + 1; j < n; j++)
            {
                // XOR highlights every bit position where the two numbers differ.
                // The number of set bits in the XOR equals the Hamming distance
                // between the two transformed values.
                int xorResult = transformed[i] ^ transformed[j];

                // Count the set bits (1s) in xorResult.
                // We use Brian Kernighan's algorithm: repeatedly clear the lowest
                // set bit with  x = x & (x - 1)  until x becomes 0.
                // Each iteration removes exactly one set bit, so the loop runs
                // exactly popcount(xorResult) times — very efficient in practice.
                int setBitCount = CountSetBits(xorResult);

                // If the Hamming distance equals k, this pair qualifies.
                if (setBitCount == k)
                {
                    pairCount++;
                }
            }
        }

        return pairCount;
    }

    /// <summary>
    /// Counts the number of bits set to 1 in a non-negative integer using
    /// Brian Kernighan's bit-clearing algorithm.
    ///
    /// Key insight: (x &amp; (x - 1)) clears the lowest set bit of x in one operation.
    /// Repeating until x == 0 counts exactly how many set bits existed.
    ///
    /// Time Complexity: O(number of set bits) — at most O(32) for a 32-bit integer.
    /// </summary>
    private int CountSetBits(int x)
    {
        int count = 0;

        // While there is at least one set bit remaining...
        while (x != 0)
        {
            // Clear the lowest set bit.
            // Example: x = 0110 → x-1 = 0101 → x & (x-1) = 0100 (cleared bit 1).
            x = x & (x - 1);
            count++; // We just removed one set bit, so increment the counter.
        }

        return count;
    }
}

// =============================================================================
// DEMO / TEST CODE
// =============================================================================
// Trace verification before running:
//
// Example 1: nums = [2, 1, 3], k = 1
//   Transform:
//     2  = 0b...0010 → oddBits=0b0010, evenBits=0b0000 → (0b0001)|(0b0000) = 1
//     1  = 0b...0001 → oddBits=0b0000, evenBits=0b0001 → (0b0000)|(0b0010) = 2
//     3  = 0b...0011 → oddBits=0b0010, evenBits=0b0001 → (0b0001)|(0b0010) = 3
//   Transformed: [1, 2, 3]
//   Pairs:
//     (0,1): 1^2 = 3 = 0b11 → 2 set bits → NOT k=1
//     (0,2): 1^3 = 2 = 0b10 → 1 set bit  → COUNTS
//     (1,2): 2^3 = 1 = 0b01 → 1 set bit  → COUNTS
//   Result: 2 ✓
//
// Example 2: nums = [5, 10, 0], k = 2
//   Transform:
//     5  = 0b0101 → oddBits=0b0100, evenBits=0b0001 → (0b0010)|(0b0010) = 0b1010 = 10
//     10 = 0b1010 → oddBits=0b1010, evenBits=0b0000 → (0b0101)|(0b0000) = 0b0101 = 5
//     0  = 0b0000 → oddBits=0b0000, evenBits=0b0000 → 0
//   Transformed: [10, 5, 0]
//   Pairs:
//     (0,1): 10^5  = 15 = 0b1111 → 4 set bits → NOT k=2
//     (0,2): 10^0  = 10 = 0b1010 → 2 set bits → COUNTS
//     (1,2):  5^0  =  5 = 0b0101 → 2 set bits → COUNTS
//   Result: 2 ✓
// =============================================================================

var solution = new Solution();

// --- Example 1 ---
int[] nums1 = { 2, 1, 3 };
int k1 = 1;
int result1 = solution.CountPairsWithKBitsXor(nums1, k1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  Input:    nums = [{string.Join(", ", nums1)}], k = {k1}");
Console.WriteLine($"  Output:   {result1}");
Console.WriteLine($"  Expected: 2");
Console.WriteLine();

// --- Example 2 ---
int[] nums2 = { 5, 10, 0 };
int k2 = 2;
int result2 = solution.CountPairsWithKBitsXor(nums2, k2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  Input:    nums = [{string.Join(", ", nums2)}], k = {k2}");
Console.WriteLine($"  Output:   {result2}");
Console.WriteLine($"  Expected: 2");
Console.WriteLine();

// --- Edge Case: single element (no pairs possible) ---
int[] nums3 = { 7 };
int k3 = 1;
int result3 = solution.CountPairsWithKBitsXor(nums3, k3);
Console.WriteLine($"Edge Case (single element):");
Console.WriteLine($"  Input:    nums = [{string.Join(", ", nums3)}], k = {k3}");
Console.WriteLine($"  Output:   {result3}");
Console.WriteLine($"  Expected: 0");
Console.WriteLine();

// --- Edge Case: k = 0 (XOR must be 0, meaning identical transformed values) ---
int[] nums4 = { 3, 3, 5 };
int k4 = 0;
int result4 = solution.CountPairsWithKBitsXor(nums4, k4);
Console.WriteLine($"Edge Case (k=0, identical transformed values):");
Console.WriteLine($"  Input:    nums = [{string.Join(", ", nums4)}], k = {k4}");
Console.WriteLine($"  Output:   {result4}");
Console.WriteLine($"  Expected: 1  (only the pair (3,3) has XOR=0)");