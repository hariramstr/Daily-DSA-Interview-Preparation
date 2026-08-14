import java.util.*;

/*
 * Title: Minimum Toggles to Match a Parity Beacon
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A monitoring system stores the state of n beacon modules in a binary array bits,
 * where bits[i] is either 0 or 1. You are also given a target binary array target
 * of the same length. In one operation, you may choose any index i and toggle bits[i].
 * However, toggling index i also automatically toggles every index j > i such that
 * j and i have the same parity (both even or both odd). In other words, choosing i
 * flips bits[i], bits[i+2], bits[i+4], and so on.
 *
 * Your task is to return the minimum number of operations required to transform bits
 * into target. If it is impossible, return -1.
 *
 * Key Insight:
 * Even indices and odd indices are completely independent.
 * - Toggling an even index affects only even indices to its right.
 * - Toggling an odd index affects only odd indices to its right.
 *
 * Therefore, the problem splits into two separate chains:
 * - even chain: indices 0, 2, 4, ...
 * - odd chain: indices 1, 3, 5, ...
 *
 * Within one chain, toggling at a position flips that position and all later positions
 * in the same chain. This is exactly the classic suffix-flip greedy pattern:
 * - Process the chain from left to right.
 * - Track whether an odd number of previous flips has already affected the current value.
 * - If the effective current bit does not match the target bit, we must flip here.
 *   This choice is forced and also optimal.
 *
 * Since every position can always be fixed greedily and the two chains are independent,
 * the answer is always achievable, so -1 is never needed under the given rules.
 */

public class Solution {

    /**
     * Computes the minimum number of operations required to transform the given
     * binary array into the target array.
     *
     * The algorithm works by splitting the array into two independent parity chains:
     * one for even indices and one for odd indices. Each chain is solved greedily.
     *
     * @param bits the starting binary array
     * @param target the desired binary array
     * @return the minimum number of toggle operations, or -1 if input is invalid
     *
     * Time complexity: O(n), because each index is processed exactly once.
     * Space complexity: O(1), excluding input storage.
     */
    public int minimumToggles(int[] bits, int[] target) {
        if (bits == null || target == null || bits.length != target.length) {
            return -1;
        }

        int n = bits.length;

        // Optional input validation to ensure the arrays are truly binary.
        for (int i = 0; i < n; i++) {
            if ((bits[i] != 0 && bits[i] != 1) || (target[i] != 0 && target[i] != 1)) {
                return -1;
            }
        }

        // Solve the even-index chain and odd-index chain independently,
        // then add their minimum operation counts.
        int evenOps = solveParityChain(bits, target, 0);
        int oddOps = solveParityChain(bits, target, 1);

        // Under valid input, both chains are always solvable.
        if (evenOps == -1 || oddOps == -1) {
            return -1;
        }

        return evenOps + oddOps;
    }

    /**
     * Solves one parity chain greedily.
     *
     * For example:
     * - startParity = 0 processes indices 0, 2, 4, ...
     * - startParity = 1 processes indices 1, 3, 5, ...
     *
     * Detailed idea:
     * 1. We scan the chain from left to right.
     * 2. We maintain whether the current position has been flipped an odd number
     *    of times by earlier operations in the same chain.
     * 3. If the effective current bit differs from the target bit, then we are forced
     *    to toggle at this index. This is because later toggles cannot affect this
     *    position anymore; only a toggle at the current position can fix it now.
     * 4. Each such forced toggle changes the flip-state for all later positions in
     *    the same chain.
     *
     * @param bits the starting binary array
     * @param target the desired binary array
     * @param startParity 0 for even chain, 1 for odd chain
     * @return the minimum number of operations for this chain
     *
     * Time complexity: O(k), where k is the number of indices in this parity chain.
     * Space complexity: O(1).
     */
    public int solveParityChain(int[] bits, int[] target, int startParity) {
        int operations = 0;

        // This variable stores whether an odd number of previous toggles in this chain
        // has affected the current position.
        //
        // false -> no net flip currently active
        // true  -> current bit should be interpreted as flipped
        boolean flipped = false;

        // Walk through only one parity chain: startParity, startParity+2, startParity+4, ...
        for (int i = startParity; i < bits.length; i += 2) {

            // Compute the effective value at index i after considering all earlier
            // toggles in the same parity chain.
            //
            // If flipped == false, effective bit is just bits[i].
            // If flipped == true, effective bit is 1 - bits[i].
            int effectiveBit = flipped ? (bits[i] ^ 1) : bits[i];

            // If the effective bit already matches the target, do nothing.
            // If it does not match, we are forced to toggle at i.
            if (effectiveBit != target[i]) {
                operations++;

                // Toggling at i flips this position and every later position in the
                // same parity chain, so from now on the flip-state is inverted.
                flipped = !flipped;
            }
        }

        return operations;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n).
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated examples).
     * Space complexity: O(1), excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        // bits = [1,0,1,1,0], target = [0,0,0,1,1]
        // Toggle index 0 flips positions 0,2,4:
        // [1,0,1,1,0] -> [0,0,0,1,1]
        // So the true minimum is 1.
        int[] bits1 = {1, 0, 1, 1, 0};
        int[] target1 = {0, 0, 0, 1, 1};
        System.out.println("Example 1");
        System.out.println("bits   = " + solution.arrayToString(bits1));
        System.out.println("target = " + solution.arrayToString(target1));
        System.out.println("Minimum toggles = " + solution.minimumToggles(bits1, target1));
        System.out.println();

        // Example 2 from the prompt.
        // bits = [0,1,0,1], target = [1,0,1,0]
        // Toggle index 0 -> [1,1,1,1]
        // Toggle index 1 -> [1,0,1,0]
        // Minimum is 2.
        int[] bits2 = {0, 1, 0, 1};
        int[] target2 = {1, 0, 1, 0};
        System.out.println("Example 2");
        System.out.println("bits   = " + solution.arrayToString(bits2));
        System.out.println("target = " + solution.arrayToString(target2));
        System.out.println("Minimum toggles = " + solution.minimumToggles(bits2, target2));
        System.out.println();

        // Additional simple test: already equal, answer should be 0.
        int[] bits3 = {1, 1, 0, 0, 1};
        int[] target3 = {1, 1, 0, 0, 1};
        System.out.println("Additional Test 1");
        System.out.println("bits   = " + solution.arrayToString(bits3));
        System.out.println("target = " + solution.arrayToString(target3));
        System.out.println("Minimum toggles = " + solution.minimumToggles(bits3, target3));
        System.out.println();

        // Additional test: single element.
        int[] bits4 = {0};
        int[] target4 = {1};
        System.out.println("Additional Test 2");
        System.out.println("bits   = " + solution.arrayToString(bits4));
        System.out.println("target = " + solution.arrayToString(target4));
        System.out.println("Minimum toggles = " + solution.minimumToggles(bits4, target4));
    }
}