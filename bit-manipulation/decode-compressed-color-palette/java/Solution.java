/*
 * Title: Decode Compressed Color Palette
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A graphics application stores colors in a compact 32-bit integer format.
 * Each color is encoded as follows:
 *   - Bits 24–31 (8 bits): Alpha channel (0–255)
 *   - Bits 16–23 (8 bits): Red channel (0–255)
 *   - Bits 8–15  (8 bits): Green channel (0–255)
 *   - Bits 0–7   (8 bits): Blue channel (0–255)
 *
 * You are given an array of encoded color integers and a list of transformation
 * operations. Each operation is a tuple [channel, value] where channel is one of
 * 'A', 'R', 'G', 'B', and value is an integer (0–255). The operation sets that
 * specific channel to value for ALL colors in the array using bitwise operations
 * only (no arithmetic).
 *
 * After applying all operations in order, return the array of final encoded
 * 32-bit color integers.
 *
 * Constraints:
 *   - 1 <= colors.length <= 10^5
 *   - 0 <= colors[i] <= 2^32 - 1
 *   - 1 <= operations.length <= 100
 *   - Each channel is one of 'A', 'R', 'G', 'B'
 *   - 0 <= value <= 255
 */

import java.util.*;

public class Solution {

    /**
     * Returns the bit shift amount for a given channel character.
     *
     * Channel layout in the 32-bit integer:
     *   'A' -> bits 24-31 -> shift 24
     *   'R' -> bits 16-23 -> shift 16
     *   'G' -> bits  8-15 -> shift  8
     *   'B' -> bits  0-7  -> shift  0
     *
     * @param channel one of 'A', 'R', 'G', 'B'
     * @return the number of bits to shift for that channel
     * Time complexity:  O(1)
     * Space complexity: O(1)
     */
    public int getShift(char channel) {
        // Use a switch expression (Java 14+) to map each channel letter to its shift
        return switch (channel) {
            case 'A' -> 24;   // Alpha occupies the most-significant byte
            case 'R' -> 16;   // Red occupies the second byte
            case 'G' ->  8;   // Green occupies the third byte
            case 'B' ->  0;   // Blue occupies the least-significant byte
            default  -> throw new IllegalArgumentException("Unknown channel: " + channel);
        };
    }

    /**
     * Applies a series of channel-set operations to every color in the palette
     * using only bitwise operations.
     *
     * Algorithm overview for each operation [channel, value]:
     *   1. Determine the bit-shift for the target channel.
     *   2. Build a CLEAR MASK  – all 1s except the 8 bits of that channel (set to 0).
     *   3. Build a SET MASK    – the new value shifted into the correct bit position.
     *   4. For every color:
     *        color = (color & clearMask) | setMask
     *      The AND clears the old channel bits; the OR writes the new value.
     *
     * @param colors     array of 32-bit ARGB-encoded color integers (stored as long
     *                   to handle the full unsigned 32-bit range without sign issues)
     * @param operations list of [channel, value] pairs; channel is a Character,
     *                   value is an Integer
     * @return           the modified colors array after all operations are applied
     * Time complexity:  O(C * N) where C = number of operations, N = colors.length
     * Space complexity: O(1) extra (the result is written back into the input array)
     */
    public long[] applyOperations(long[] colors, List<Object[]> operations) {

        // -----------------------------------------------------------------------
        // Iterate over every operation in the given order
        // -----------------------------------------------------------------------
        for (Object[] op : operations) {

            // Step 1 – Extract the channel character and the new value from the tuple
            char channel = (char) op[0];
            int  value   = (int)  op[1];

            // Step 2 – Find how many bits we need to shift for this channel
            int shift = getShift(channel);

            // Step 3 – Build the CLEAR MASK
            //   0xFF          = 0x00000000_000000FF  (8 ones in the lowest byte)
            //   0xFF << shift = those 8 ones moved to the target channel position
            //   ~(0xFF << shift) = flip every bit → all ones EXCEPT the target channel
            //
            //   Example for Green (shift=8):
            //     0xFF << 8          = 0x00000000_0000FF00
            //     ~(0xFF << 8)       = 0xFFFFFFFF_FFFF00FF  (as a long)
            //   ANDing a color with this mask zeroes out only the green bits.
            //
            //   We work with long (64-bit) to avoid Java's signed-int complications
            //   when the alpha bit 31 is set.
            long clearMask = ~((long) 0xFF << shift);

            // Step 4 – Build the SET MASK
            //   Place the new 8-bit value into the correct bit position.
            //   Example: value=128 (0x80), shift=8  →  setMask = 0x0000_8000
            long setMask = ((long) value & 0xFF) << shift;

            // Step 5 – Apply both masks to every color in the array
            for (int i = 0; i < colors.length; i++) {

                // (a) AND with clearMask  → zeroes out the old channel bits,
                //                           leaves all other bits unchanged
                // (b) OR  with setMask   → writes the new channel value into
                //                           those now-zeroed bits
                colors[i] = (colors[i] & clearMask) | setMask;
            }
        }

        // Return the modified array (same reference; modified in-place)
        return colors;
    }

    // =========================================================================
    // Helper – convert a long color value to a readable 0xAARRGGBB hex string
    // =========================================================================
    private static String toHex(long color) {
        return String.format("0x%08X", color);
    }

    // =========================================================================
    // Main – demonstrates the solution with the examples from the problem
    // =========================================================================
    /**
     * Entry point: runs both provided examples and prints the results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution sol = new Solution();

        // -----------------------------------------------------------------------
        // Example 1
        //   colors     = [0xFFFF0000, 0x00FF00FF]
        //   operations = [['G', 128], ['A', 0]]
        //   Expected   = [0x00FF8000, 0x00FF80FF]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");

        long[] colors1 = { 0xFFFF0000L, 0x00FF00FFL };

        // Build the operations list; each entry is Object[]{channel, value}
        List<Object[]> ops1 = new ArrayList<>();
        ops1.add(new Object[]{ 'G', 128 });   // set green to 128 (0x80)
        ops1.add(new Object[]{ 'A',   0 });   // set alpha to 0

        System.out.println("Input colors:");
        for (long c : colors1) System.out.println("  " + toHex(c));

        long[] result1 = sol.applyOperations(colors1, ops1);

        System.out.println("Output colors:");
        for (long c : result1) System.out.println("  " + toHex(c));

        // Manual trace:
        //   0xFFFF0000 → green=0x80 → 0xFFFF8000 → alpha=0 → 0x00FF8000  ✓
        //   0x00FF00FF → green=0x80 → 0x00FF80FF → alpha=0 → 0x00FF80FF  ✓
        System.out.println("Expected: [0x00FF8000, 0x00FF80FF]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2
        //   colors     = [0x12345678]
        //   operations = [['B', 255], ['R', 0]]
        //   Expected   = [0x120000FF]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");

        long[] colors2 = { 0x12345678L };

        List<Object[]> ops2 = new ArrayList<>();
        ops2.add(new Object[]{ 'B', 255 });   // set blue  to 255 (0xFF)
        ops2.add(new Object[]{ 'R',   0 });   // set red   to 0

        System.out.println("Input colors:");
        for (long c : colors2) System.out.println("  " + toHex(c));

        long[] result2 = sol.applyOperations(colors2, ops2);

        System.out.println("Output colors:");
        for (long c : result2) System.out.println("  " + toHex(c));

        // Manual trace:
        //   0x12345678 → blue=0xFF → 0x123456FF → red=0 → 0x120000FF  ✓
        System.out.println("Expected: [0x120000FF]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Extra edge-case: set every channel explicitly
        // -----------------------------------------------------------------------
        System.out.println("=== Extra: set all channels ===");

        long[] colors3 = { 0xDEADBEEFL };

        List<Object[]> ops3 = new ArrayList<>();
        ops3.add(new Object[]{ 'A', 0xFF });
        ops3.add(new Object[]{ 'R', 0x00 });
        ops3.add(new Object[]{ 'G', 0x7F });
        ops3.add(new Object[]{ 'B', 0x01 });

        System.out.println("Input colors:");
        for (long c : colors3) System.out.println("  " + toHex(c));

        long[] result3 = sol.applyOperations(colors3, ops3);

        System.out.println("Output colors:");
        for (long c : result3) System.out.println("  " + toHex(c));
        // Expected: A=FF, R=00, G=7F, B=01 → 0xFF007F01
        System.out.println("Expected: [0xFF007F01]");
    }
}