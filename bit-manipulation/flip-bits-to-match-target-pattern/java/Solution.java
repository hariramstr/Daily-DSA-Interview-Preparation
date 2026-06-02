/*
 * Flip Bits to Match Target Pattern
 * ==================================
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given two non-negative integers `source` and `target`. Your goal is to
 * determine the minimum number of bit flips required to convert `source` into `target`.
 *
 * A **bit flip** means changing a single bit in the binary representation of `source`
 * from `0` to `1`, or from `1` to `0`.
 *
 * To find which bits differ between `source` and `target`, consider using the XOR
 * operation. Two bits that are different will produce a `1` in the XOR result, and
 * two bits that are the same will produce a `0`. Therefore, the answer is simply the
 * number of `1` bits in the XOR of `source` and `target` (also known as the Hamming distance).
 *
 * Constraints:
 * - 0 <= source, target <= 10^9
 *
 * Example 1:
 * - Input: source = 10, target = 15
 * - Binary: source = 1010, target = 1111
 * - XOR result: 0101 → two `1` bits
 * - Output: 2
 *
 * Example 2:
 * - Input: source = 0, target = 8
 * - Binary: source = 0000, target = 1000
 * - XOR result: 1000 → one `1` bit
 * - Output: 1
 */

public class Solution {

    /**
     * Calculates the minimum number of bit flips required to convert source into target.
     *
     * <p>Algorithm Overview:
     * <ol>
     *   <li>XOR source and target: positions where bits differ will be 1, same will be 0.</li>
     *   <li>Count the number of 1-bits in the XOR result (Hamming distance).</li>
     * </ol>
     *
     * @param source the original non-negative integer
     * @param target the desired non-negative integer
     * @return the minimum number of bit flips to convert source to target
     *
     * Time Complexity:  O(log n) where n is the maximum of source and target,
     *                   since we examine each bit in the XOR result.
     *                   Alternatively O(1) since integers are bounded to 32 bits.
     * Space Complexity: O(1) — only a constant amount of extra space is used.
     */
    public int minBitFlips(int source, int target) {

        // -----------------------------------------------------------------------
        // Step 1: XOR source and target.
        //
        // XOR (^) compares each pair of corresponding bits:
        //   - If the bits are the SAME  (0^0 or 1^1), the result bit is 0.
        //   - If the bits are DIFFERENT (0^1 or 1^0), the result bit is 1.
        //
        // Example 1: source = 10 (1010), target = 15 (1111)
        //   XOR:  1010
        //       ^ 1111
        //       ------
        //         0101   ← bits at positions 0 and 2 differ → 2 flips needed
        //
        // Example 2: source = 0 (0000), target = 8 (1000)
        //   XOR:  0000
        //       ^ 1000
        //       ------
        //         1000   ← bit at position 3 differs → 1 flip needed
        // -----------------------------------------------------------------------
        int xorResult = source ^ target;

        // -----------------------------------------------------------------------
        // Step 2: Count the number of 1-bits in xorResult.
        //
        // Each 1-bit in xorResult represents a position where source and target
        // differ, meaning we need exactly one flip at that position.
        //
        // We use Java's built-in Integer.bitCount() which efficiently counts
        // the number of set bits (1-bits) in the binary representation of an int.
        //
        // Example 1: xorResult = 0101 (decimal 5) → bitCount(5) = 2
        // Example 2: xorResult = 1000 (decimal 8) → bitCount(8) = 1
        // -----------------------------------------------------------------------
        int flipsNeeded = Integer.bitCount(xorResult);

        // -----------------------------------------------------------------------
        // Step 3: Return the count of differing bits.
        //
        // This is the minimum number of flips because:
        //   - Each differing bit MUST be flipped (we can't avoid it).
        //   - Each same bit must NOT be flipped (flipping it would make things worse).
        //   - Therefore, flipping exactly the differing bits is both necessary
        //     and sufficient — giving us the minimum.
        // -----------------------------------------------------------------------
        return flipsNeeded;
    }

    /**
     * An alternative manual implementation that counts set bits without using
     * Integer.bitCount(), useful for understanding the underlying mechanics.
     *
     * <p>This method uses Brian Kernighan's bit trick:
     * {@code n & (n - 1)} clears the lowest set bit of n.
     * We repeat until n becomes 0, counting how many times we clear a bit.
     *
     * @param source the original non-negative integer
     * @param target the desired non-negative integer
     * @return the minimum number of bit flips to convert source to target
     *
     * Time Complexity:  O(k) where k is the number of set bits in (source XOR target).
     *                   In the worst case O(32) = O(1) for 32-bit integers.
     * Space Complexity: O(1) — only a constant amount of extra space is used.
     */
    public int minBitFlipsManual(int source, int target) {

        // Step 1: XOR to find all differing bit positions.
        int xorResult = source ^ target;

        // Step 2: Count set bits using Brian Kernighan's algorithm.
        int count = 0;

        // -----------------------------------------------------------------------
        // Brian Kernighan's Trick:
        //   n & (n - 1) removes the lowest set bit from n.
        //
        // Example: n = 0101 (5)
        //   Iteration 1: n = 0101, n-1 = 0100, n & (n-1) = 0100, count = 1
        //   Iteration 2: n = 0100, n-1 = 0011, n & (n-1) = 0000, count = 2
        //   Iteration 3: n = 0000 → loop ends
        //   Result: 2 set bits ✓
        // -----------------------------------------------------------------------
        while (xorResult != 0) {
            // Remove the lowest set bit
            xorResult = xorResult & (xorResult - 1);
            // Each removal corresponds to one set bit we found
            count++;
        }

        return count;
    }

    /**
     * Entry point — demonstrates both solution methods with the provided examples
     * and additional edge cases, printing results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Flip Bits to Match Target Pattern ===");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 1: source = 10, target = 15
        // Binary: source = 1010, target = 1111
        // XOR:    0101 → 2 set bits → 2 flips
        // Expected Output: 2
        // -----------------------------------------------------------------------
        int source1 = 10, target1 = 15;
        int result1 = solution.minBitFlips(source1, target1);
        System.out.println("Example 1:");
        System.out.println("  source = " + source1 + " (binary: " + Integer.toBinaryString(source1) + ")");
        System.out.println("  target = " + target1 + " (binary: " + Integer.toBinaryString(target1) + ")");
        System.out.println("  XOR    = " + (source1 ^ target1) + " (binary: " + Integer.toBinaryString(source1 ^ target1) + ")");
        System.out.println("  Minimum bit flips (built-in): " + result1);
        System.out.println("  Minimum bit flips (manual):   " + solution.minBitFlipsManual(source1, target1));
        System.out.println("  Expected: 2");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: source = 0, target = 8
        // Binary: source = 0000, target = 1000
        // XOR:    1000 → 1 set bit → 1 flip
        // Expected Output: 1
        // -----------------------------------------------------------------------
        int source2 = 0, target2 = 8;
        int result2 = solution.minBitFlips(source2, target2);
        System.out.println("Example 2:");
        System.out.println("  source = " + source2 + " (binary: " + Integer.toBinaryString(source2) + ")");
        System.out.println("  target = " + target2 + " (binary: " + Integer.toBinaryString(target2) + ")");
        System.out.println("  XOR    = " + (source2 ^ target2) + " (binary: " + Integer.toBinaryString(source2 ^ target2) + ")");
        System.out.println("  Minimum bit flips (built-in): " + result2);
        System.out.println("  Minimum bit flips (manual):   " + solution.minBitFlipsManual(source2, target2));
        System.out.println("  Expected: 1");
        System.out.println();

        // -----------------------------------------------------------------------
        // Edge Case 1: source == target → no flips needed
        // XOR = 0 → 0 set bits → 0 flips
        // Expected Output: 0
        // -----------------------------------------------------------------------
        int source3 = 42, target3 = 42;
        int result3 = solution.minBitFlips(source3, target3);
        System.out.println("Edge Case 1 (source == target):");
        System.out.println("  source = " + source3 + " (binary: " + Integer.toBinaryString(source3) + ")");
        System.out.println("  target = " + target3 + " (binary: " + Integer.toBinaryString(target3) + ")");
        System.out.println("  XOR    = " + (source3 ^ target3));
        System.out.println("  Minimum bit flips (built-in): " + result3);
        System.out.println("  Minimum bit flips (manual):   " + solution.minBitFlipsManual(source3, target3));
        System.out.println("  Expected: 0");
        System.out.println();

        // -----------------------------------------------------------------------
        // Edge Case 2: source = 0, target = 0 → no flips needed
        // Expected Output: 0
        // -----------------------------------------------------------------------
        int source4 = 0, target4 = 0;
        int result4 = solution.minBitFlips(source4, target4);
        System.out.println("Edge Case 2 (both zero):");
        System.out.println("  source = " + source4);
        System.out.println("  target = " + target4);
        System.out.println("  Minimum bit flips (built-in): " + result4);
        System.out.println("  Minimum bit flips (manual):   " + solution.minBitFlipsManual(source4, target4));
        System.out.println("  Expected: 0");
        System.out.println();

        // -----------------------------------------------------------------------
        // Edge Case 3: Large values near constraint boundary
        // source = 0, target = 1_000_000_000
        // -----------------------------------------------------------------------
        int source5 = 0, target5 = 1_000_000_000;
        int result5 = solution.minBitFlips(source5, target5);
        System.out.println("Edge Case 3 (large target):");
        System.out.println("  source = " + source5);
        System.out.println("  target = " + target5 + " (binary: " + Integer.toBinaryString(target5) + ")");
        System.out.println("  Minimum bit flips (built-in): " + result5);
        System.out.println("  Minimum bit flips (manual):   " + solution.minBitFlipsManual(source5, target5));
        System.out.println("  (Number of 1-bits in 1,000,000,000)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Edge Case 4: All bits differ — source = 0, target = Integer.MAX_VALUE
        // Integer.MAX_VALUE = 0111...1 (31 ones) → 31 flips
        // -----------------------------------------------------------------------
        int source6 = 0, target6 = Integer.MAX_VALUE;
        int result6 = solution.minBitFlips(source6, target6);
        System.out.println("Edge Case 4 (max flips scenario):");
        System.out.println("  source = " + source6);
        System.out.println("  target = " + target6 + " (Integer.MAX_VALUE)");
        System.out.println("  Minimum bit flips (built-in): " + result6);
        System.out.println("  Minimum bit flips (manual):   " + solution.minBitFlipsManual(source6, target6));
        System.out.println("  Expected: 31");
    }
}