import java.util.*;

/*
 * Title: Minimum XOR Patches to Reach Every Permission Mask
 * Difficulty: Hard
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A security platform stores user roles as bitmasks. You are given an array roles of length n,
 * where each value is an integer in the range [0, 2^b - 1] representing a currently deployed
 * role mask over b permission bits. You may deploy additional role masks, called patches.
 *
 * After patching, the system is considered fully expressive if every mask in [0, 2^b - 1]
 * can be formed as the bitwise XOR of some subset of the deployed masks (original roles plus patches).
 * Each deployed mask may be used at most once in a subset, and the empty subset produces 0.
 *
 * Return the minimum number of patches required to make the system fully expressive.
 *
 * This is not asking you to construct all subsets explicitly. A correct solution must exploit
 * properties of XOR spaces over bits. In particular, two masks may be redundant if one can already
 * be produced by XOR-ing others. Your task is to determine how many new independent masks must be
 * added so that the deployed set spans the entire b-bit space.
 *
 * Key Insight:
 * The set of all XOR combinations of the deployed masks forms a vector space over GF(2).
 * If the current masks have linear rank r, then they can generate exactly 2^r distinct masks.
 * To generate every b-bit mask, we need rank b. Therefore, the minimum number of patches needed is:
 *
 *     b - currentRank
 *
 * where currentRank is the number of linearly independent masks in the input.
 */

public class Solution {

    /**
     * Computes the minimum number of additional masks (patches) needed so that
     * every b-bit mask can be represented as the XOR of some subset of the deployed masks.
     *
     * The algorithm builds a linear basis over GF(2) using Gaussian-elimination-like insertion
     * on bits. Each independent mask increases the rank by 1. If the final rank is r, then
     * the deployed masks span a space of size 2^r. To span the full b-bit space, we need rank b,
     * so the answer is b - r.
     *
     * @param roles the currently deployed role masks
     * @param b the number of permission bits
     * @return the minimum number of patches required
     *
     * Time complexity: O(n * b)
     * Space complexity: O(b)
     */
    public int minimumXorPatches(long[] roles, int b) {
        long[] basis = new long[b];
        int rank = 0;

        for (long role : roles) {
            if (insertIntoBasis(role, basis, b)) {
                rank++;
            }
        }

        return b - rank;
    }

    /**
     * Inserts a value into the XOR linear basis if it is linearly independent
     * from the values already present in the basis.
     *
     * The basis array is organized so that basis[bit] stores a number whose highest
     * set bit is exactly 'bit'. This is analogous to row-echelon form in binary linear algebra.
     *
     * Step-by-step idea:
     * 1. Start with the incoming value x.
     * 2. Look at its highest relevant bits from high to low.
     * 3. If bit i is set in x:
     *    - If basis[i] already exists, XOR x with basis[i] to eliminate bit i.
     *    - Otherwise, x is independent, so store it as basis[i] and return true.
     * 4. If x becomes 0, then it was representable by existing basis vectors, so return false.
     *
     * @param value the mask to insert
     * @param basis the current XOR basis
     * @param b the number of bits to consider
     * @return true if the value was independent and inserted, false otherwise
     *
     * Time complexity: O(b)
     * Space complexity: O(1) auxiliary, excluding the provided basis array
     */
    public boolean insertIntoBasis(long value, long[] basis, int b) {
        long x = value;

        for (int bit = b - 1; bit >= 0; bit--) {
            if (((x >> bit) & 1L) == 0L) {
                continue;
            }

            if (basis[bit] != 0L) {
                x ^= basis[bit];
            } else {
                basis[bit] = x;
                return true;
            }
        }

        return false;
    }

    /**
     * Convenience overload for int[] input.
     *
     * This method converts the int array to long values and delegates to the main implementation.
     * It is useful for small examples and beginner-friendly demonstrations.
     *
     * @param roles the currently deployed role masks as int values
     * @param b the number of permission bits
     * @return the minimum number of patches required
     *
     * Time complexity: O(n * b)
     * Space complexity: O(n + b)
     */
    public int minimumXorPatches(int[] roles, int b) {
        long[] converted = new long[roles.length];
        for (int i = 0; i < roles.length; i++) {
            converted[i] = roles[i];
        }
        return minimumXorPatches(converted, b);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Example 1:
     * roles = [1, 2], b = 3
     * Independent masks are 1 and 2, so rank = 2.
     * Needed patches = 3 - 2 = 1.
     *
     * Example 2:
     * roles = [3, 5, 6], b = 3
     * Since 3 XOR 5 = 6, only two of them are independent, so rank = 2.
     * Needed patches = 3 - 2 = 1.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(1) for the fixed demo inputs
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] roles1 = {1, 2};
        int b1 = 3;
        int result1 = solution.minimumXorPatches(roles1, b1);
        System.out.println("Example 1:");
        System.out.println("roles = " + Arrays.toString(roles1) + ", b = " + b1);
        System.out.println("Minimum patches required = " + result1);
        System.out.println("Expected = 1");
        System.out.println();

        int[] roles2 = {3, 5, 6};
        int b2 = 3;
        int result2 = solution.minimumXorPatches(roles2, b2);
        System.out.println("Example 2:");
        System.out.println("roles = " + Arrays.toString(roles2) + ", b = " + b2);
        System.out.println("Minimum patches required = " + result2);
        System.out.println("Expected = 1");
        System.out.println();

        int[] roles3 = {1, 2, 4};
        int b3 = 3;
        int result3 = solution.minimumXorPatches(roles3, b3);
        System.out.println("Additional Demo:");
        System.out.println("roles = " + Arrays.toString(roles3) + ", b = " + b3);
        System.out.println("Minimum patches required = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        int[] roles4 = {0, 0, 0};
        int b4 = 5;
        int result4 = solution.minimumXorPatches(roles4, b4);
        System.out.println("Additional Demo:");
        System.out.println("roles = " + Arrays.toString(roles4) + ", b = " + b4);
        System.out.println("Minimum patches required = " + result4);
        System.out.println("Expected = 5");
    }
}