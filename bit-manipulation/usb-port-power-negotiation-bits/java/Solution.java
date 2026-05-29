/*
 * USB Port Power Negotiation Bits
 * ================================
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A USB hub has `n` ports, each represented by a single bit in a 32-bit integer `status`.
 * A bit value of `1` means the port is currently active (drawing power), and `0` means idle.
 *
 * Given:
 *   - `status`: integer representing current port states
 *   - `mask`: integer representing ports to toggle (flip their state)
 *   - `k`: number of least-significant bit positions to check for active ports
 *
 * Steps:
 *   1. Toggle the bits in `status` that are set in `mask` using XOR (^).
 *   2. Count how many bits in positions 0 through k-1 of the new status are set to 1.
 *
 * Return: int[] { newStatus, activeCount }
 *
 * Constraints:
 *   - 0 <= status <= 2^31 - 1
 *   - 0 <= mask   <= 2^31 - 1
 *   - 1 <= k <= 32
 *
 * Example 1:
 *   status=13 (01101), mask=10 (01010), k=4
 *   newStatus = 13 ^ 10 = 7 (00111)
 *   active in first 4 bits: bits 0,1,2 → count=3
 *   Output: [7, 3]
 *
 * Example 2:
 *   status=255 (11111111), mask=170 (10101010), k=8
 *   newStatus = 255 ^ 170 = 85 (01010101)
 *   active in first 8 bits: bits 0,2,4,6 → count=4
 *   Output: [85, 4]
 */

public class Solution {

    /**
     * Toggles the ports indicated by {@code mask} in {@code status} using XOR,
     * then counts how many of the first {@code k} bit positions are active (1)
     * in the resulting value.
     *
     * @param status the current port state as a 32-bit non-negative integer
     * @param mask   the set of ports to toggle; each 1-bit in mask flips the
     *               corresponding bit in status
     * @param k      the number of least-significant bit positions (0 .. k-1)
     *               to inspect for active ports; 1 <= k <= 32
     * @return an int array [newStatus, activeCount] where newStatus is the
     *         result of status XOR mask, and activeCount is the number of
     *         1-bits among bit positions 0 through k-1 of newStatus
     *
     * Time Complexity:  O(k) — we iterate over at most k bit positions
     * Space Complexity: O(1) — only a fixed-size result array and a few variables
     */
    public int[] usbToggle(int status, int mask, int k) {

        // ---------------------------------------------------------------
        // STEP 1: Toggle the requested ports using bitwise XOR.
        //
        // XOR truth table for a single bit:
        //   0 ^ 0 = 0  (idle port, not in mask → stays idle)
        //   1 ^ 0 = 1  (active port, not in mask → stays active)
        //   0 ^ 1 = 1  (idle port, in mask → becomes active)
        //   1 ^ 1 = 0  (active port, in mask → becomes idle)
        //
        // So XOR-ing with mask flips exactly the bits where mask has a 1.
        // ---------------------------------------------------------------
        int newStatus = status ^ mask;

        // ---------------------------------------------------------------
        // STEP 2: Count active ports in bit positions 0 through k-1.
        //
        // We need to look at only the lowest k bits of newStatus.
        // Strategy: iterate i from 0 to k-1, check bit i with a right-shift
        // and AND with 1.
        //
        // Bit i of newStatus is: (newStatus >>> i) & 1
        //   - We use unsigned right-shift (>>>) so that if newStatus were
        //     negative (sign bit set) we still get 0s shifted in from the
        //     left rather than 1s.
        //   - AND with 1 isolates the least-significant bit after shifting.
        // ---------------------------------------------------------------
        int activeCount = 0;

        for (int i = 0; i < k; i++) {
            // Shift newStatus right by i positions (unsigned), then mask the LSB.
            // If the result is 1, bit position i is active.
            int bitValue = (newStatus >>> i) & 1;

            // Accumulate the count of active bits.
            activeCount += bitValue;
        }

        // ---------------------------------------------------------------
        // STEP 3: Package and return the result.
        // ---------------------------------------------------------------
        return new int[]{newStatus, activeCount};
    }

    /**
     * Entry point — demonstrates the solution with the two examples from the
     * problem description and prints the results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1 trace:
        //   status = 13  → binary ...0 1101
        //   mask   = 10  → binary ...0 1010
        //   XOR         → binary ...0 0111  = 7
        //   First k=4 bits of 7: 0 1 1 1  (positions 3,2,1,0)
        //     bit 0 = 1 ✓
        //     bit 1 = 1 ✓
        //     bit 2 = 1 ✓
        //     bit 3 = 0
        //   activeCount = 3
        //   Expected output: [7, 3]
        // ---------------------------------------------------------------
        int[] result1 = solution.usbToggle(13, 10, 4);
        System.out.println("Example 1:");
        System.out.println("  status  = 13  (binary: " + Integer.toBinaryString(13) + ")");
        System.out.println("  mask    = 10  (binary: " + Integer.toBinaryString(10) + ")");
        System.out.println("  k       = 4");
        System.out.println("  newStatus (13 ^ 10) = " + result1[0]
                + "  (binary: " + Integer.toBinaryString(result1[0]) + ")");
        System.out.println("  activeCount in first 4 bits = " + result1[1]);
        System.out.println("  Output: [" + result1[0] + ", " + result1[1] + "]");
        System.out.println("  Expected: [7, 3]");
        System.out.println("  PASS: " + (result1[0] == 7 && result1[1] == 3));
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2 trace:
        //   status = 255 → binary 1111 1111
        //   mask   = 170 → binary 1010 1010
        //   XOR         → binary 0101 0101  = 85
        //   First k=8 bits of 85: 0101 0101
        //     bit 0 = 1 ✓
        //     bit 1 = 0
        //     bit 2 = 1 ✓
        //     bit 3 = 0
        //     bit 4 = 1 ✓
        //     bit 5 = 0
        //     bit 6 = 1 ✓
        //     bit 7 = 0
        //   activeCount = 4
        //   Expected output: [85, 4]
        // ---------------------------------------------------------------
        int[] result2 = solution.usbToggle(255, 170, 8);
        System.out.println("Example 2:");
        System.out.println("  status  = 255 (binary: " + Integer.toBinaryString(255) + ")");
        System.out.println("  mask    = 170 (binary: " + Integer.toBinaryString(170) + ")");
        System.out.println("  k       = 8");
        System.out.println("  newStatus (255 ^ 170) = " + result2[0]
                + "  (binary: " + Integer.toBinaryString(result2[0]) + ")");
        System.out.println("  activeCount in first 8 bits = " + result2[1]);
        System.out.println("  Output: [" + result2[0] + ", " + result2[1] + "]");
        System.out.println("  Expected: [85, 4]");
        System.out.println("  PASS: " + (result2[0] == 85 && result2[1] == 4));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional edge-case: k=32, all bits checked.
        //   status = 0, mask = 0 → newStatus = 0, activeCount = 0
        // ---------------------------------------------------------------
        int[] result3 = solution.usbToggle(0, 0, 32);
        System.out.println("Edge Case (status=0, mask=0, k=32):");
        System.out.println("  newStatus = " + result3[0] + ", activeCount = " + result3[1]);
        System.out.println("  Expected: [0, 0]");
        System.out.println("  PASS: " + (result3[0] == 0 && result3[1] == 0));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional edge-case: k=32 with large values.
        //   status = 2147483647 (Integer.MAX_VALUE, all 31 lower bits set)
        //   mask   = 2147483647
        //   XOR    = 0
        //   activeCount in 32 bits = 0
        // ---------------------------------------------------------------
        int[] result4 = solution.usbToggle(Integer.MAX_VALUE, Integer.MAX_VALUE, 32);
        System.out.println("Edge Case (status=MAX_VALUE, mask=MAX_VALUE, k=32):");
        System.out.println("  newStatus = " + result4[0] + ", activeCount = " + result4[1]);
        System.out.println("  Expected: [0, 0]");
        System.out.println("  PASS: " + (result4[0] == 0 && result4[1] == 0));
    }
}