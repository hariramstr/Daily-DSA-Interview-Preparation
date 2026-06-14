import java.util.*;

/*
 * Title: Minimum Toggles to Equalize Binary Counters
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an array counters of n non-negative integers. Each integer represents
 * the current value of a hardware counter. In one operation, you may choose a bit
 * position b (0-indexed) and toggle that bit in exactly two different counters.
 * Toggling means changing a 0 to 1 or a 1 to 0 at that bit position.
 *
 * Your task is to determine the minimum number of operations required to make all
 * counters equal. If it is impossible, return -1.
 *
 * This operation models a paired electrical pulse: each pulse must affect the same
 * bit position in two different devices at the same time. Because of this restriction,
 * you cannot freely change bits one counter at a time.
 *
 * Return the smallest number of such paired toggles needed so that every value in the
 * array becomes identical.
 *
 * Important observations:
 * - You may perform operations on different bit positions independently.
 * - The final common value does not need to be one of the original values.
 * - Two counters chosen in an operation must be distinct.
 *
 * Constraints:
 * - 1 <= n <= 10^5
 * - 0 <= counters[i] <= 10^9
 *
 * Example 1:
 * Input: counters = [1, 0, 1, 0]
 * Output: 1
 *
 * Example 2:
 * Input: counters = [3, 3, 1]
 * Output: -1
 */

public class Solution {

    /**
     * Computes the minimum number of paired bit-toggle operations needed to make
     * all counters equal, or returns -1 if it is impossible.
     *
     * Core idea:
     * Each bit position can be analyzed independently because an operation affects
     * exactly one chosen bit position.
     *
     * For a fixed bit position:
     * - Let ones = number of counters whose bit is 1.
     * - Let zeros = n - ones.
     *
     * In one operation at this bit:
     * - toggling two 1s decreases ones by 2
     * - toggling two 0s increases ones by 2
     * - toggling one 1 and one 0 keeps ones unchanged
     *
     * Therefore, the parity of ones never changes.
     *
     * To make all counters equal, at every bit position all counters must end up
     * either all 0 or all 1.
     *
     * That means:
     * - target all 0 is possible only if ones is even
     * - target all 1 is possible only if zeros is even
     *
     * Since zeros = n - ones:
     * - if n is odd, exactly one of these may be possible depending on parity
     * - if n is even, both ones and zeros have the same parity
     *
     * Minimum operations for one bit:
     * - To make all 0: need to flip every current 1 to 0.
     *   Each operation can flip at most two 1s to 0 by choosing two counters with bit 1.
     *   So cost = ones / 2.
     * - To make all 1: similarly cost = zeros / 2.
     *
     * We choose the cheaper valid target for each bit and sum over all bits.
     *
     * @param counters the array of non-negative counter values
     * @return the minimum number of operations required, or -1 if impossible
     * Time complexity: O(n * B), where B is the number of processed bit positions (31 here)
     * Space complexity: O(1), excluding input storage
     */
    public long minimumTogglesToEqualize(int[] counters) {
        int n = counters.length;

        // Special case:
        // If there is only one counter, it is already equal to itself.
        if (n == 1) {
            return 0L;
        }

        long totalOperations = 0L;

        // Since counters[i] <= 1e9, bits 0..30 are sufficient.
        // We still process 31 bits explicitly for clarity and correctness.
        for (int bit = 0; bit <= 30; bit++) {
            int ones = countOnesAtBit(counters, bit);
            int zeros = n - ones;

            // We will compute the minimum valid cost for this bit.
            long bestForThisBit = Long.MAX_VALUE;

            // Option 1: make this bit 0 in every counter.
            // This is possible only if the number of current 1s is even,
            // because each operation changes the count of 1s by 0 or 2.
            if ((ones & 1) == 0) {
                bestForThisBit = Math.min(bestForThisBit, ones / 2L);
            }

            // Option 2: make this bit 1 in every counter.
            // This is possible only if the number of current 0s is even.
            if ((zeros & 1) == 0) {
                bestForThisBit = Math.min(bestForThisBit, zeros / 2L);
            }

            // If neither target is possible, then this bit can never become uniform,
            // so making all counters equal is impossible.
            if (bestForThisBit == Long.MAX_VALUE) {
                return -1L;
            }

            totalOperations += bestForThisBit;
        }

        return totalOperations;
    }

    /**
     * Counts how many numbers in the array have a 1 at the specified bit position.
     *
     * @param counters the array of counter values
     * @param bit the bit position to inspect
     * @return the number of counters whose bit at position {@code bit} is 1
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int countOnesAtBit(int[] counters, int bit) {
        int ones = 0;

        for (int value : counters) {
            // Shift the desired bit to the least significant position,
            // then mask with 1 to extract it.
            ones += (value >> bit) & 1;
        }

        return ones;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param counters the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] counters) {
        return Arrays.toString(counters);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size across demonstrated examples * 31)
     * Space complexity: O(1), excluding temporary output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sample1 = {1, 0, 1, 0};
        int[] sample2 = {3, 3, 1};

        System.out.println("Sample 1: counters = " + solution.arrayToString(sample1));
        System.out.println("Minimum operations = " + solution.minimumTogglesToEqualize(sample1));
        System.out.println("Expected = 1");
        System.out.println();

        System.out.println("Sample 2: counters = " + solution.arrayToString(sample2));
        System.out.println("Minimum operations = " + solution.minimumTogglesToEqualize(sample2));
        System.out.println("Expected = -1");
        System.out.println();

        int[] extra1 = {0};
        int[] extra2 = {2, 2, 2};
        int[] extra3 = {1, 1, 0, 0};
        int[] extra4 = {5, 2, 7, 0};

        System.out.println("Extra 1: counters = " + solution.arrayToString(extra1));
        System.out.println("Minimum operations = " + solution.minimumTogglesToEqualize(extra1));
        System.out.println();

        System.out.println("Extra 2: counters = " + solution.arrayToString(extra2));
        System.out.println("Minimum operations = " + solution.minimumTogglesToEqualize(extra2));
        System.out.println();

        System.out.println("Extra 3: counters = " + solution.arrayToString(extra3));
        System.out.println("Minimum operations = " + solution.minimumTogglesToEqualize(extra3));
        System.out.println();

        System.out.println("Extra 4: counters = " + solution.arrayToString(extra4));
        System.out.println("Minimum operations = " + solution.minimumTogglesToEqualize(extra4));
    }
}