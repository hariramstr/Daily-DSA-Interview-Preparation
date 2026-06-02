/*
 * Title: Flip Bits to Match Target Pattern
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given two non-negative integers `source` and `target`. Your goal is to
 * determine the minimum number of bit flips required to convert `source` into `target`.
 *
 * A bit flip means changing a single bit in the binary representation of `source`
 * from 0 to 1, or from 1 to 0.
 *
 * To find which bits differ between `source` and `target`, we use the XOR operation.
 * Two bits that are different produce a 1 in the XOR result, and two bits that are
 * the same produce a 0. Therefore, the answer is simply the number of 1 bits in the
 * XOR of `source` and `target` (also known as the Hamming distance).
 *
 * Constraints:
 *   0 <= source, target <= 10^9
 *
 * Example 1:
 *   Input:  source = 10, target = 15
 *   Binary: source = 1010, target = 1111
 *   XOR:    0101 → two 1 bits
 *   Output: 2
 *
 * Example 2:
 *   Input:  source = 0, target = 8
 *   Binary: source = 0000, target = 1000
 *   XOR:    1000 → one 1 bit
 *   Output: 1
 */

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Returns the minimum number of bit flips needed to convert source into target.
    ///
    /// Time Complexity:  O(1) — We process at most 32 bits (fixed-width integer).
    ///                          The loop runs a constant number of times regardless
    ///                          of the input values.
    ///
    /// Space Complexity: O(1) — We only use a handful of integer variables; no
    ///                          extra data structures are allocated.
    /// </summary>
    public int MinBitFlips(int source, int target)
    {
        // ── Step 1: XOR source and target ────────────────────────────────────
        // XOR (^) compares each pair of corresponding bits:
        //   • If both bits are the SAME  → result bit is 0  (no flip needed)
        //   • If the bits are DIFFERENT  → result bit is 1  (a flip IS needed)
        //
        // Why XOR? Because it isolates exactly the positions where source and
        // target disagree. Each 1-bit in the XOR result represents one required
        // bit flip.
        //
        // Example 1: source=10 (1010), target=15 (1111)
        //   1010
        // ^ 1111
        // ------
        //   0101   ← positions 0 and 2 differ → 2 flips needed
        //
        // Example 2: source=0 (0000), target=8 (1000)
        //   0000
        // ^ 1000
        // ------
        //   1000   ← position 3 differs → 1 flip needed
        int xorResult = source ^ target;

        // ── Step 2: Count the number of 1-bits in xorResult ──────────────────
        // Each 1-bit in xorResult corresponds to one position where source and
        // target differ, meaning one bit flip is required at that position.
        //
        // We use Brian Kernighan's algorithm to count set bits efficiently:
        //   The trick: (n & (n - 1)) clears the LOWEST set bit of n.
        //   By repeatedly doing this until n becomes 0, we count how many
        //   set bits existed — each iteration removes exactly one set bit.
        //
        // Why this trick works:
        //   Subtracting 1 from n flips the lowest set bit to 0 and sets all
        //   lower bits to 1. ANDing with the original n then clears those bits.
        //   Example: n = 0101
        //     n - 1 = 0100
        //     n & (n-1) = 0100  ← lowest set bit (bit 0) is cleared
        //   Next iteration: n = 0100
        //     n - 1 = 0011
        //     n & (n-1) = 0000  ← lowest set bit (bit 2) is cleared → done
        //   Two iterations → two set bits counted ✓
        int flipCount = 0;

        // Continue as long as there are still 1-bits remaining in xorResult.
        while (xorResult != 0)
        {
            // Clear the lowest set bit. This is the core of Kernighan's trick.
            // Each time we enter this loop body, we are "consuming" one 1-bit,
            // which corresponds to one required bit flip.
            xorResult = xorResult & (xorResult - 1);

            // Increment our flip counter because we just processed one differing bit.
            flipCount++;
        }

        // ── Step 3: Return the total flip count ──────────────────────────────
        // flipCount now holds the Hamming distance between source and target,
        // which is the minimum number of bit flips required.
        return flipCount;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────
var solution = new Solution();

// ── Test Case 1 ──────────────────────────────────────────────────────────────
// source = 10  → binary 1010
// target = 15  → binary 1111
// XOR    = 0101 → two 1-bits → expected output: 2
int source1 = 10;
int target1 = 15;
int result1 = solution.MinBitFlips(source1, target1);
Console.WriteLine($"Test 1: source={source1} ({Convert.ToString(source1, 2).PadLeft(4, '0')}), " +
                  $"target={target1} ({Convert.ToString(target1, 2).PadLeft(4, '0')})");
Console.WriteLine($"        XOR = {Convert.ToString(source1 ^ target1, 2).PadLeft(4, '0')}");
Console.WriteLine($"        Minimum bit flips = {result1}  (expected: 2)");
Console.WriteLine();

// ── Test Case 2 ──────────────────────────────────────────────────────────────
// source = 0  → binary 0000
// target = 8  → binary 1000
// XOR    = 1000 → one 1-bit → expected output: 1
int source2 = 0;
int target2 = 8;
int result2 = solution.MinBitFlips(source2, target2);
Console.WriteLine($"Test 2: source={source2} ({Convert.ToString(source2, 2).PadLeft(4, '0')}), " +
                  $"target={target2} ({Convert.ToString(target2, 2).PadLeft(4, '0')})");
Console.WriteLine($"        XOR = {Convert.ToString(source2 ^ target2, 2).PadLeft(4, '0')}");
Console.WriteLine($"        Minimum bit flips = {result2}  (expected: 1)");
Console.WriteLine();

// ── Additional Edge Cases ─────────────────────────────────────────────────────

// Edge Case: source == target → no flips needed
int source3 = 42;
int target3 = 42;
int result3 = solution.MinBitFlips(source3, target3);
Console.WriteLine($"Edge Case 1: source={source3}, target={target3} (identical)");
Console.WriteLine($"             Minimum bit flips = {result3}  (expected: 0)");
Console.WriteLine();

// Edge Case: source = 0, target = 0 → no flips needed
int source4 = 0;
int target4 = 0;
int result4 = solution.MinBitFlips(source4, target4);
Console.WriteLine($"Edge Case 2: source={source4}, target={target4} (both zero)");
Console.WriteLine($"             Minimum bit flips = {result4}  (expected: 0)");
Console.WriteLine();

// Edge Case: large values near constraint boundary
// source = 1_000_000_000 (≈ 0011_1010_1001_1010_1100_1010_0000_0000)
// target = 999_999_999
int source5 = 1_000_000_000;
int target5 = 999_999_999;
int result5 = solution.MinBitFlips(source5, target5);
Console.WriteLine($"Edge Case 3: source={source5}, target={target5}");
Console.WriteLine($"             XOR = {source5 ^ target5} " +
                  $"({Convert.ToString(source5 ^ target5, 2)})");
Console.WriteLine($"             Minimum bit flips = {result5}");