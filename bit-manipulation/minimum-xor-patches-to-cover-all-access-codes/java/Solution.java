import java.util.*;

/*
Problem Title: Minimum XOR Patches to Cover All Access Codes

Problem Description:
You are given an array codes of n non-negative integers, where each integer represents an access code supported by a legacy device. You are also given an integer m. A security team wants every value in the range [0, m] to be generatable as the XOR of some subset of the final set of codes.

In one patch operation, you may add any non-negative integer x to the array. After adding patches, consider all subset XOR values that can be formed from the resulting array. Your task is to return the minimum number of patch operations required so that every integer from 0 to m inclusive can be expressed as the XOR of some subset of the final array.

Unlike subset sum, XOR does not depend on order and duplicate values may or may not help depending on linear independence over bits. The problem asks for the smallest number of additional values needed, not the values themselves.

A subset may be empty, so 0 is always representable. If the current codes already span enough independent bit patterns, no patch is needed.

Constraints:
- 1 <= n <= 200000
- 0 <= codes[i] <= 10^18
- 0 <= m <= 10^18
- You should aim for an algorithm significantly faster than checking all subsets.

Example 1:
Input: codes = [1, 2], m = 7
Output: 1
Explanation: Using subset XORs of [1, 2], we can form {0, 1, 2, 3}. We cannot form 4, 5, 6, or 7. Adding a single patch 4 makes the independent set {1, 2, 4}, which can generate every value from 0 to 7.

Example 2:
Input: codes = [5, 10], m = 6
Output: 2
Explanation: The existing codes do not generate all values in [0, 6]. For instance, 1 is impossible. One optimal strategy is to add 1 and 2. Then the set spans enough low-bit values to generate every number from 0 to 6. Therefore the minimum number of patches is 2.
*/

/**
 * A complete runnable solution for the XOR patching problem.
 *
 * Core idea:
 * For subset XOR, the set of all reachable values forms a vector space over GF(2).
 * Therefore, after reducing the given numbers into a linear basis, the reachable values
 * are exactly all XOR combinations of those basis vectors.
 *
 * We need every value in [0, m] to be reachable.
 *
 * A key fact:
 * To generate every integer from 0 to m, it is sufficient and necessary that for
 * k = floor(log2(m)) + 1 (or k = 0 when m = 0), the reachable set contains every
 * k-bit number from 0 to 2^k - 1.
 *
 * Why?
 * Because [0, m] contains all powers of two below or equal to the highest bit of m,
 * and once all k-bit values are reachable, certainly every value in [0, m] is reachable.
 * Conversely, if every value in [0, m] is reachable, then in particular all powers of two
 * below the top bit are reachable, which implies the span projected to the low k bits has
 * full dimension k. That means all k-bit values are reachable.
 *
 * So the problem becomes:
 * How many additional independent low-bit vectors are needed so that the span on the low k bits
 * has dimension k?
 *
 * Answer:
 * k - rank(low-k-bit projections of codes), where rank is the linear basis rank over GF(2).
 */
public class Solution {

    /**
     * Computes the minimum number of patch operations required so that every value in [0, m]
     * can be represented as the XOR of some subset of the final array.
     *
     * Step-by-step logic:
     * 1. If m == 0, then the empty subset already generates 0, so answer is 0.
     * 2. Let k be the number of bits needed to represent m, i.e. floor(log2(m)) + 1.
     * 3. Only the lowest k bits matter for generating values in [0, m].
     *    Higher bits can be ignored because any target in [0, m] has no bits above k - 1.
     * 4. Build a binary linear basis from the given codes after masking to the lowest k bits.
     * 5. Let rank be the number of independent vectors in that basis.
     * 6. To generate all k-bit values, we need full rank k.
     * 7. Each patch can add at most one new independent vector.
     * 8. Therefore minimum patches = k - rank.
     *
     * @param codes the existing access codes
     * @param m the inclusive upper bound of values that must all be representable
     * @return the minimum number of patch operations required
     * Time complexity: O(n * B), where B <= 61 because values are up to 10^18
     * Space complexity: O(B)
     */
    public int minPatches(long[] codes, long m) {
        if (m == 0) {
            return 0;
        }

        int k = bitLength(m);

        // We only care about the lowest k bits.
        // Since k <= 60 for m <= 10^18, shifting is safe here.
        long mask = (1L << k) - 1;

        // basis[i] will store a basis vector whose highest set bit is exactly i.
        long[] basis = new long[61];

        int rank = 0;

        for (long code : codes) {
            long value = code & mask;

            // Insert this value into the XOR linear basis.
            if (insertIntoBasis(basis, value)) {
                rank++;
            }
        }

        return k - rank;
    }

    /**
     * Inserts a value into a binary linear basis if it is linearly independent
     * from the vectors already present.
     *
     * Detailed explanation:
     * - We process bits from high to low.
     * - Suppose the current highest set bit of value is b.
     *   - If basis[b] is empty, then value is independent and becomes the basis vector for bit b.
     *   - Otherwise, XOR value with basis[b] to eliminate bit b, and continue.
     * - If value becomes 0, it was dependent on existing basis vectors.
     *
     * This is exactly Gaussian elimination over GF(2), but implemented with bit operations.
     *
     * @param basis the current linear basis, indexed by highest set bit
     * @param value the value to insert
     * @return true if the value was independent and added to the basis; false otherwise
     * Time complexity: O(B), where B <= 61
     * Space complexity: O(1) additional space
     */
    public boolean insertIntoBasis(long[] basis, long value) {
        long x = value;

        for (int bit = 60; bit >= 0; bit--) {
            if (((x >> bit) & 1L) == 0L) {
                continue;
            }

            if (basis[bit] == 0L) {
                basis[bit] = x;
                return true;
            }

            x ^= basis[bit];
        }

        return false;
    }

    /**
     * Returns the number of bits needed to represent a non-negative long value.
     *
     * Examples:
     * - bitLength(1) = 1
     * - bitLength(2) = 2
     * - bitLength(7) = 3
     * - bitLength(8) = 4
     *
     * @param x a non-negative number
     * @return the bit length of x; returns 0 only when x == 0
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int bitLength(long x) {
        if (x == 0) {
            return 0;
        }
        return 64 - Long.numberOfLeadingZeros(x);
    }

    /**
     * Convenience overload for users who may have int arrays.
     *
     * @param codes the existing access codes as int values
     * @param m the inclusive upper bound
     * @return the minimum number of patch operations required
     * Time complexity: O(n * B), where B <= 61
     * Space complexity: O(B)
     */
    public int minPatches(int[] codes, long m) {
        long[] converted = new long[codes.length];
        for (int i = 0; i < codes.length; i++) {
            converted[i] = codes[i];
        }
        return minPatches(converted, m);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1: 1
     * Example 2: 2
     *
     * Also includes a few extra sanity checks.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] codes1 = {1, 2};
        long m1 = 7;
        int result1 = solution.minPatches(codes1, m1);
        System.out.println(result1); // Expected: 1

        long[] codes2 = {5, 10};
        long m2 = 6;
        int result2 = solution.minPatches(codes2, m2);
        System.out.println(result2); // Expected: 2

        long[] codes3 = {1, 2, 4};
        long m3 = 7;
        int result3 = solution.minPatches(codes3, m3);
        System.out.println(result3); // Expected: 0

        long[] codes4 = {0, 0, 0};
        long m4 = 3;
        int result4 = solution.minPatches(codes4, m4);
        System.out.println(result4); // Expected: 2

        long[] codes5 = {8};
        long m5 = 0;
        int result5 = solution.minPatches(codes5, m5);
        System.out.println(result5); // Expected: 0
    }
}