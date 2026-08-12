import java.util.*;

/*
 * Title: Minimum Rewrites to Match a Layered Bit Template
 * Difficulty: Hard
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given two integers, n and target, and an array costs of length n.
 * You must build a nonnegative integer x using exactly n bits, where bit i
 * (0-indexed from the least significant bit) may be rewritten at a cost of costs[i].
 * Initially, x is equal to 0, so every bit is unset.
 *
 * A security system checks x through a layered template rule. For every k from 1 to n,
 * let low(k) be the integer formed by the lowest k bits of x. The template is satisfied if,
 * for every k, the number of set bits in low(k) has the same parity as the k-th lowest bit
 * of target. In other words, for each prefix of bits from the least significant side,
 * the parity of that prefix in x must match a required parity sequence derived from target.
 *
 * You may choose any bits of x to flip from 0 to 1, paying the corresponding rewrite costs.
 * Return the minimum total cost needed to construct such an x. If no such x exists, return -1.
 *
 * Key Observation:
 * Let p[k] be the required parity for the lowest k bits of x, where p[k] is exactly
 * the bit (k - 1) of target.
 *
 * If we define p[0] = 0, then:
 *   parity(low(k)) = parity(low(k - 1)) XOR bit(k - 1 of x)
 *
 * Therefore:
 *   x[k - 1] = p[k] XOR p[k - 1]
 *
 * This means every bit of x is uniquely determined by adjacent parity differences.
 * So there is never more than one valid x.
 *
 * Since x starts as all zeros and we may only flip bits from 0 to 1, the minimum cost is:
 *   sum(costs[i]) for every i where the uniquely determined x[i] = 1
 *
 * A valid solution always exists because each required bit of x can simply be chosen
 * according to the parity-difference formula.
 *
 * Important note about the examples in the prompt:
 * The written explanations are internally inconsistent. The mathematically correct rule
 * implies a unique solution determined by parity differences. This implementation follows
 * the exact formal statement of the problem and computes the correct answer from it.
 */

public class Solution {

    /**
     * Computes the minimum total rewrite cost needed to build an n-bit number x such that
     * for every prefix length k (1 <= k <= n), the parity of the lowest k bits of x matches
     * the (k - 1)-th bit of target.
     *
     * Detailed idea:
     * 1. Let requiredParity[k] be the parity required for the lowest k bits of x.
     *    From the statement, requiredParity[k] is simply bit (k - 1) of target.
     *
     * 2. Let requiredParity[0] = 0 because the empty prefix has zero set bits, which is even.
     *
     * 3. When we extend from k - 1 bits to k bits, the new bit x[k - 1] changes parity exactly when it is 1.
     *    So:
     *       requiredParity[k] = requiredParity[k - 1] XOR x[k - 1]
     *    Rearranging:
     *       x[k - 1] = requiredParity[k] XOR requiredParity[k - 1]
     *
     * 4. Thus each bit of x is uniquely determined.
     *
     * 5. Since x initially is all zeros and setting bit i to 1 costs costs[i], the minimum total cost
     *    is simply the sum of costs[i] over all positions where the derived x[i] = 1.
     *
     * 6. Under the formal statement, this construction is always feasible, so -1 is never needed
     *    unless invalid input is provided.
     *
     * @param n the number of bits to use
     * @param target the integer whose bits define the required prefix parities
     * @param costs costs[i] is the cost to set bit i from 0 to 1
     * @return the minimum total rewrite cost, or -1 if the input is invalid
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public long minimumRewriteCost(int n, long target, int[] costs) {
        // Basic input validation.
        if (n < 1 || costs == null || costs.length != n) {
            return -1L;
        }

        // If target is supposed to fit in n bits, then any set bit at position >= n would violate the constraint.
        // We validate that here. For n == 64 this check would need special handling, but n can be large in the
        // statement while target is still passed as a Java long in this runnable demo. So we validate only when
        // the shift is safe.
        if (n < 63) {
            long upperBits = target >>> n;
            if (upperBits != 0L) {
                return -1L;
            }
        } else if (target < 0) {
            // Negative long would mean the sign bit is set, which is not a valid nonnegative target.
            return -1L;
        }

        long totalCost = 0L;

        // previousParity represents requiredParity[k - 1].
        // For k = 1, requiredParity[0] = 0 because the empty prefix has even parity.
        int previousParity = 0;

        // We process bits from least significant to most significant.
        for (int i = 0; i < n; i++) {
            // currentParity is requiredParity[i + 1], which is exactly bit i of target.
            int currentParity = (int) ((target >>> i) & 1L);

            // The uniquely forced bit x[i] is the XOR difference between consecutive prefix parities.
            int bitOfX = previousParity ^ currentParity;

            // If the forced bit is 1, we must pay its rewrite cost.
            if (bitOfX == 1) {
                totalCost += costs[i];
            }

            // Move forward: this current required parity becomes the previous one for the next step.
            previousParity = currentParity;
        }

        return totalCost;
    }

    /**
     * Builds and returns the unique valid bit array x implied by the parity constraints.
     * This is mainly useful for demonstration, debugging, and educational tracing.
     *
     * @param n the number of bits
     * @param target the integer whose bits define required prefix parities
     * @return an int array of length n where each entry is 0 or 1, representing the unique valid x
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildForcedBits(int n, long target) {
        int[] bits = new int[n];
        int previousParity = 0;

        for (int i = 0; i < n; i++) {
            int currentParity = (int) ((target >>> i) & 1L);
            bits[i] = previousParity ^ currentParity;
            previousParity = currentParity;
        }

        return bits;
    }

    /**
     * Verifies whether a given bit assignment satisfies the layered parity template.
     *
     * @param bits the candidate bit array, where bits[i] is the i-th least significant bit
     * @param target the integer whose bits define the required prefix parities
     * @return true if the bit array satisfies all prefix parity constraints; false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean verifyTemplate(int[] bits, long target) {
        int parity = 0;

        for (int i = 0; i < bits.length; i++) {
            parity ^= (bits[i] & 1);
            int requiredParity = (int) ((target >>> i) & 1L);
            if (parity != requiredParity) {
                return false;
            }
        }

        return true;
    }

    /**
     * Computes the total cost of setting all positions where bits[i] = 1.
     *
     * @param bits the bit array representing x
     * @param costs the rewrite costs
     * @return the total cost of all set bits
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long computeCost(int[] bits, int[] costs) {
        long total = 0L;
        for (int i = 0; i < bits.length; i++) {
            if (bits[i] == 1) {
                total += costs[i];
            }
        }
        return total;
    }

    /**
     * Converts a least-significant-bit-first bit array into a readable binary string
     * from most significant bit to least significant bit.
     *
     * @param bits the bit array where bits[0] is the least significant bit
     * @return a binary string representation
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String bitsToBinaryString(int[] bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = bits.length - 1; i >= 0; i--) {
            sb.append(bits[i]);
        }
        return sb.toString();
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The examples in the prompt contain contradictory explanations and outputs.
     * This main method prints the mathematically correct results according to the formal rule.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(n) for displayed bit arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Demonstration 1
        int n1 = 4;
        long target1 = 11L; // binary: 1011
        int[] costs1 = {5, 2, 7, 1};

        long answer1 = solution.minimumRewriteCost(n1, target1, costs1);
        int[] bits1 = solution.buildForcedBits(n1, target1);

        System.out.println("Example 1:");
        System.out.println("n = " + n1 + ", target = " + target1 + ", costs = " + Arrays.toString(costs1));
        System.out.println("Forced x bits (MSB->LSB): " + solution.bitsToBinaryString(bits1));
        System.out.println("Valid: " + solution.verifyTemplate(bits1, target1));
        System.out.println("Minimum rewrite cost (formal rule): " + answer1);
        System.out.println();

        // Demonstration 2
        int n2 = 5;
        long target2 = 6L; // binary: 00110
        int[] costs2 = {4, 9, 1, 3, 8};

        long answer2 = solution.minimumRewriteCost(n2, target2, costs2);
        int[] bits2 = solution.buildForcedBits(n2, target2);

        System.out.println("Example 2:");
        System.out.println("n = " + n2 + ", target = " + target2 + ", costs = " + Arrays.toString(costs2));
        System.out.println("Forced x bits (MSB->LSB): " + solution.bitsToBinaryString(bits2));
        System.out.println("Valid: " + solution.verifyTemplate(bits2, target2));
        System.out.println("Minimum rewrite cost (formal rule): " + answer2);
        System.out.println();

        // Additional small sanity check
        int n3 = 3;
        long target3 = 5L; // binary: 101
        int[] costs3 = {3, 4, 2};

        long answer3 = solution.minimumRewriteCost(n3, target3, costs3);
        int[] bits3 = solution.buildForcedBits(n3, target3);

        System.out.println("Additional Test:");
        System.out.println("n = " + n3 + ", target = " + target3 + ", costs = " + Arrays.toString(costs3));
        System.out.println("Forced x bits (MSB->LSB): " + solution.bitsToBinaryString(bits3));
        System.out.println("Valid: " + solution.verifyTemplate(bits3, target3));
        System.out.println("Minimum rewrite cost: " + answer3);
    }
}