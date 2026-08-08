import java.util.*;

/*
 * Title: Minimum Toggles to Match a Device XOR Fingerprint
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A hardware lab stores the state of n devices as an integer array states, where states[i]
 * is a non-negative 32-bit integer. The lab wants the XOR of all device states to become
 * exactly target. In one operation, you may choose any single device and toggle exactly
 * one bit in its binary representation (change a 0 to 1 or a 1 to 0 at one bit position).
 *
 * Return the minimum number of bit toggles required so that the XOR of the entire array
 * equals target.
 *
 * This is not asking you to transform each number into a specific value. You may toggle
 * bits on any devices in any order, and only the final XOR of all numbers matters.
 * A toggle on one device affects the global XOR at exactly that bit position.
 * Because XOR is independent across bit positions, the answer depends only on which bits
 * differ between the current overall XOR and target.
 *
 * Formally, let current = states[0] XOR states[1] XOR ... XOR states[n - 1].
 * Find the minimum number of single-bit toggles needed to make current become target.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 0 <= states[i] <= 10^9
 * - 0 <= target <= 10^9
 * - Your solution should run in O(n) time and use O(1) extra space, excluding input storage.
 *
 * Example 1:
 * Input: states = [5, 1, 2], target = 0
 * Output: 2
 * Explanation:
 * current XOR = 5 XOR 1 XOR 2 = 6.
 * Binary 6 is 110, while target 0 is 000.
 * Two bit positions differ, so two single-bit toggles are sufficient.
 *
 * Example 2:
 * Input: states = [7, 7, 7], target = 7
 * Output: 0
 * Explanation:
 * current XOR = 7 XOR 7 XOR 7 = 7, which already matches target,
 * so no operation is needed.
 */

public class Solution {

    /**
     * Computes the minimum number of single-bit toggles needed so that the XOR of all
     * values in the array becomes exactly target.
     *
     * Core idea:
     * 1. Compute the current XOR of the entire array.
     * 2. Compare that XOR with target.
     * 3. Every bit position where they differ must be toggled exactly once somewhere
     *    in the array.
     * 4. Therefore, the answer is simply the number of set bits in:
     *       currentXor XOR target
     *
     * @param states the array of device states
     * @param target the desired final XOR of the entire array
     * @return the minimum number of single-bit toggles required
     *
     * Time complexity: O(n), where n is the number of elements in states
     * Space complexity: O(1), ignoring input storage
     */
    public int minimumToggles(int[] states, int target) {
        // This variable will store the XOR of all device states.
        int currentXor = 0;

        // Step 1:
        // Compute the XOR of the entire array.
        //
        // Why does this help?
        // The problem only cares about the FINAL XOR of all numbers,
        // not the exact final value of each individual number.
        //
        // XOR has a very useful property:
        // If we toggle one bit in one device, then the global XOR changes
        // at exactly that same bit position.
        //
        // So instead of thinking about many numbers separately,
        // we can think only about the current overall XOR.
        for (int state : states) {
            currentXor ^= state;
        }

        // Step 2:
        // Find which bit positions are different between currentXor and target.
        //
        // XORing two numbers gives 1 at exactly the positions where they differ.
        //
        // Example:
        // currentXor = 6  -> binary 110
        // target     = 0  -> binary 000
        // diff       = 6  -> binary 110
        //
        // That means bit 2 and bit 1 differ, so we need 2 toggles.
        int differingBits = currentXor ^ target;

        // Step 3:
        // Count how many bits are set to 1 in differingBits.
        //
        // Each set bit means:
        // "This bit in the global XOR must be flipped."
        //
        // Since one operation flips exactly one bit in exactly one device,
        // one differing global bit requires at least one operation.
        //
        // Also, one operation is always sufficient for one differing bit:
        // just toggle that bit in any one device.
        //
        // Therefore, the minimum number of operations is exactly the number
        // of set bits in differingBits.
        return Integer.bitCount(differingBits);
    }

    /**
     * Computes the XOR of all values in the given array.
     * This helper method is useful for demonstration and tracing.
     *
     * @param states the array of device states
     * @return the XOR of all elements in states
     *
     * Time complexity: O(n), where n is the number of elements in states
     * Space complexity: O(1)
     */
    public int computeArrayXor(int[] states) {
        int xor = 0;
        for (int state : states) {
            xor ^= state;
        }
        return xor;
    }

    /**
     * Converts an integer array into a readable string representation.
     * This is used only for printing example inputs in main.
     *
     * @param arr the input integer array
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the number of elements in arr
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     * It also prints the intermediate XOR values so the reasoning is easy to follow.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) across the demonstrated examples
     * Space complexity: O(1), excluding the sample input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] states1 = {5, 1, 2};
        int target1 = 0;
        int currentXor1 = solution.computeArrayXor(states1);
        int answer1 = solution.minimumToggles(states1, target1);

        System.out.println("Example 1");
        System.out.println("states = " + solution.arrayToString(states1));
        System.out.println("target = " + target1);
        System.out.println("current XOR = " + currentXor1);
        System.out.println("minimum toggles = " + answer1);
        System.out.println("Expected = 2");
        System.out.println();

        // Verification for Example 1:
        // 5 XOR 1 = 4
        // 4 XOR 2 = 6
        // current XOR = 6
        // 6 XOR 0 = 6
        // bitCount(6) = bitCount(binary 110) = 2
        // So the output is correctly 2.

        // Example 2
        int[] states2 = {7, 7, 7};
        int target2 = 7;
        int currentXor2 = solution.computeArrayXor(states2);
        int answer2 = solution.minimumToggles(states2, target2);

        System.out.println("Example 2");
        System.out.println("states = " + solution.arrayToString(states2));
        System.out.println("target = " + target2);
        System.out.println("current XOR = " + currentXor2);
        System.out.println("minimum toggles = " + answer2);
        System.out.println("Expected = 0");
        System.out.println();

        // Verification for Example 2:
        // 7 XOR 7 = 0
        // 0 XOR 7 = 7
        // current XOR = 7
        // 7 XOR 7 = 0
        // bitCount(0) = 0
        // So the output is correctly 0.

        // Additional small demonstration
        int[] states3 = {0};
        int target3 = 5;
        int currentXor3 = solution.computeArrayXor(states3);
        int answer3 = solution.minimumToggles(states3, target3);

        System.out.println("Additional Demo");
        System.out.println("states = " + solution.arrayToString(states3));
        System.out.println("target = " + target3);
        System.out.println("current XOR = " + currentXor3);
        System.out.println("minimum toggles = " + answer3);
        System.out.println("Explanation: current XOR 0 differs from 5 (binary 101) in 2 bit positions.");
    }
}