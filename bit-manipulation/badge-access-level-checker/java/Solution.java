/*
 * Badge Access Level Checker
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A security system assigns access levels to employees using a bitmask. Each bit in the mask
 * represents a specific room or resource (bit 0 = lobby, bit 1 = office, bit 2 = server room, etc.).
 * An employee is granted access to a resource if the corresponding bit in their badge mask is set to 1.
 *
 * You are given two integers: `badge` (the employee's access bitmask) and `required` (the bitmask
 * representing the set of permissions needed to enter a restricted area). An employee is allowed to
 * enter the area if and only if they have ALL the required permissions (i.e., every bit set in
 * `required` is also set in `badge`). Additionally, return the number of EXTRA permissions the
 * employee has beyond what is required.
 *
 * Return a list [allowed, extras] where:
 * - allowed is 1 if the employee can enter, 0 otherwise.
 * - extras is the count of permission bits the employee has that are NOT in the required set.
 *
 * Constraints:
 * - 0 <= badge, required <= 10^9
 *
 * Example 1:
 * - Input: badge = 29 (binary: 11101), required = 21 (binary: 10101)
 * - Output: [1, 1]
 * - Explanation: badge & required == required (all required bits present).
 *   Extra bits in badge but not required: bit 1 is set in badge but not in required, so extras = 1.
 *
 * Example 2:
 * - Input: badge = 12 (binary: 01100), required = 21 (binary: 10101)
 * - Output: [0, 2]
 * - Explanation: Badge is missing bits 0 and 4 from required, so entry is denied.
 *   Badge has bits 2 and 3 which are not in required, so extras = 2.
 */

import java.util.Arrays;
import java.util.List;

/**
 * Solution class for the Badge Access Level Checker problem.
 *
 * <p>Core Bit Manipulation Concepts Used:
 * <ul>
 *   <li>AND (&): Used to check if all required bits are present in badge</li>
 *   <li>XOR (^) or AND with NOT (~): Used to find bits in badge but NOT in required</li>
 *   <li>Integer.bitCount(): Counts the number of set bits (1s) in an integer</li>
 * </ul>
 */
public class Solution {

    /**
     * Checks whether an employee's badge grants access to a restricted area and
     * counts how many extra permissions the employee has beyond what is required.
     *
     * <p>Algorithm Overview:
     * <ol>
     *   <li>Use bitwise AND to verify all required bits are present in badge.</li>
     *   <li>Use bitwise AND with complement to isolate extra bits in badge.</li>
     *   <li>Count the extra bits using Integer.bitCount().</li>
     * </ol>
     *
     * @param badge    the employee's access bitmask (0 <= badge <= 10^9)
     * @param required the bitmask of permissions needed to enter the area (0 <= required <= 10^9)
     * @return a List of two integers: [allowed, extras]
     *         where allowed = 1 if access is granted, 0 otherwise,
     *         and extras = count of bits set in badge but not in required
     *
     * Time Complexity:  O(1) — all operations (AND, bitCount) run in constant time
     *                   since integers have a fixed number of bits (32 bits in Java)
     * Space Complexity: O(1) — only a fixed number of integer variables are used
     */
    public List<Integer> checkAccess(int badge, int required) {

        // -----------------------------------------------------------------------
        // STEP 1: Check if the employee has ALL required permissions.
        //
        // We use the bitwise AND operator (&) between badge and required.
        // If every bit that is set (1) in `required` is also set (1) in `badge`,
        // then (badge & required) will equal `required` exactly.
        //
        // Example 1 trace:
        //   badge    = 29  = 11101 in binary
        //   required = 21  = 10101 in binary
        //   badge & required = 10101 = 21  --> equals required, so access ALLOWED
        //
        // Example 2 trace:
        //   badge    = 12  = 01100 in binary
        //   required = 21  = 10101 in binary
        //   badge & required = 00100 = 4   --> does NOT equal required (21), so DENIED
        // -----------------------------------------------------------------------
        boolean hasAllRequired = (badge & required) == required;

        // Convert the boolean result to the integer 1 (allowed) or 0 (denied)
        int allowed = hasAllRequired ? 1 : 0;

        // -----------------------------------------------------------------------
        // STEP 2: Find the bits that are set in `badge` but NOT in `required`.
        //
        // We want bits that are in badge but absent from required.
        // The expression (badge & ~required) does exactly this:
        //   ~required  flips all bits of required (bitwise NOT / complement)
        //   badge & ~required  keeps only the bits that are 1 in badge AND 0 in required
        //
        // Example 1 trace:
        //   badge    = 29  = ...011101 in binary
        //   required = 21  = ...010101 in binary
        //   ~required      = ...101010 in binary  (all bits flipped)
        //   badge & ~required = ...001000 = 2 in decimal
        //   That is bit 1 (value 2), which corresponds to the "office" permission.
        //   Integer.bitCount(2) = 1  --> extras = 1  ✓
        //
        // Example 2 trace:
        //   badge    = 12  = ...001100 in binary
        //   required = 21  = ...010101 in binary
        //   ~required      = ...101010 in binary
        //   badge & ~required = ...001000 = ... wait, let's be precise:
        //     badge    = 0...001100
        //     ~required= 1...101010  (note: upper bits of ~required are all 1s)
        //     badge & ~required: only bits where BOTH are 1
        //       bit 2: badge=1, ~required=0 (required bit2=0 so ~required bit2=1) 
        //       Let me redo carefully:
        //       required = 21 = 010101
        //       ~required bits (low 6): 101010
        //       badge    = 12 = 001100
        //       badge & ~required:
        //         bit 0: 0 & 1 = 0
        //         bit 1: 0 & 1 = 0
        //         bit 2: 1 & 0 = 0   (required bit2=1, so ~required bit2=0)
        //         bit 3: 1 & 1 = 1   (required bit3=0, so ~required bit3=1)
        //         bit 4: 0 & 0 = 0   (required bit4=1, so ~required bit4=0)
        //         bit 5: 0 & 1 = 0
        //       Result = 001000 = 8
        //       Wait, badge=12=001100, so bit2=1 and bit3=1.
        //       required=21=010101, so bit0=1, bit2=1, bit4=1.
        //       ~required: bit0=0, bit1=1, bit2=0, bit3=1, bit4=0, bit5=1, ...
        //       badge & ~required:
        //         bit2: badge=1, ~required=0 → 0
        //         bit3: badge=1, ~required=1 → 1
        //       Result has only bit3 set = 8
        //       Integer.bitCount(8) = 1 ... but expected extras = 2!
        //
        // Hmm, let me re-examine. badge=12=1100 in binary.
        //   bit0=0, bit1=0, bit2=1, bit3=1
        // required=21=10101 in binary.
        //   bit0=1, bit1=0, bit2=1, bit3=0, bit4=1
        // ~required (low bits): bit0=0, bit1=1, bit2=0, bit3=1, bit4=0
        // badge & ~required:
        //   bit0: 0&0=0
        //   bit1: 0&1=0
        //   bit2: 1&0=0
        //   bit3: 1&1=1
        //   bit4: 0&0=0
        // Result = bit3 only = 8, bitCount=1
        //
        // But the problem says extras=2 (bits 2 and 3 are not in required).
        // Wait: required=21=10101, so bit2 IS set in required (10101: bit0=1,bit2=1,bit4=1).
        // badge=12=01100: bit2=1, bit3=1.
        // Bits in badge NOT in required:
        //   bit2: badge=1, required=1 → NOT extra
        //   bit3: badge=1, required=0 → extra
        // That's only 1 extra bit (bit3). But the problem says extras=2.
        //
        // Let me re-read the problem explanation for Example 2:
        // "Badge has bits 2 and 3 which are not in required, so extras = 2."
        // But required=21=10101 has bit2 set! So bit2 IS in required.
        // This seems like an error in the problem statement.
        // Let me verify: 21 in binary = 16+4+1 = 10101. Bit positions: 4,2,0 are set.
        // badge=12=8+4=1100. Bit positions: 3,2 are set.
        // Bits in badge not in required: bit3 (since bit2 is in required).
        // So extras should be 1, not 2 as stated.
        //
        // However, the problem statement says extras=2. Let me re-read once more...
        // "Badge has bits 2 and 3 which are not in required"
        // Perhaps the problem is using 1-based bit numbering or a different convention?
        // Or perhaps the problem statement has an error.
        //
        // I will implement the mathematically correct version:
        // extras = number of bits set in badge but NOT set in required
        // = Integer.bitCount(badge & ~required)
        //
        // For Example 1: badge=29=11101, required=21=10101
        //   badge & ~required: bit1 is set in badge but not required → extras=1 ✓
        // For Example 2: badge=12=01100, required=21=10101
        //   badge & ~required: bit3 is set in badge but not required → extras=1
        //   (The problem says 2, but mathematically it's 1 based on the given values)
        //
        // I'll trust the mathematical definition and implement accordingly.
        // -----------------------------------------------------------------------

        // Compute the bitmask of permissions in badge that are NOT in required.
        // ~required flips all bits; ANDing with badge isolates badge-only bits.
        int extraBitsMask = badge & ~required;

        // Count how many bits are set in the extraBitsMask.
        // Integer.bitCount() counts the number of 1-bits (Hamming weight) in an int.
        int extras = Integer.bitCount(extraBitsMask);

        // -----------------------------------------------------------------------
        // STEP 3: Return the result as a list [allowed, extras].
        // -----------------------------------------------------------------------
        return Arrays.asList(allowed, extras);
    }

    /**
     * Main method to demonstrate the Badge Access Level Checker solution
     * with the provided sample inputs from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        //   badge = 29    binary: 11101
        //   required = 21 binary: 10101
        //   Expected Output: [1, 1]
        //
        //   Verification:
        //     badge & required = 11101 & 10101 = 10101 = 21 = required → allowed = 1
        //     badge & ~required = 11101 & 01010 = 01000 = 8 (only bit 3... wait)
        //     Let me redo: 29=11101, 21=10101
        //       ~21 in 5 bits = 01010
        //       29 & ~21 = 11101 & 01010 = 01000 = 8? No:
        //         bit0: 1&0=0
        //         bit1: 0&1=0  wait, 29=11101: bit0=1,bit1=0,bit2=1,bit3=1,bit4=1
        //         21=10101: bit0=1,bit1=0,bit2=1,bit3=0,bit4=1
        //         ~21: bit0=0,bit1=1,bit2=0,bit3=1,bit4=0
        //         29 & ~21:
        //           bit0: 1&0=0
        //           bit1: 0&1=0
        //           bit2: 1&0=0
        //           bit3: 1&1=1
        //           bit4: 1&0=0
        //         Result = bit3 = 8, bitCount=1 → extras=1 ✓
        // -----------------------------------------------------------------------
        System.out.println("=== Badge Access Level Checker ===");
        System.out.println();

        int badge1 = 29;
        int required1 = 21;
        List<Integer> result1 = solution.checkAccess(badge1, required1);
        System.out.println("Example 1:");
        System.out.println("  badge    = " + badge1 + " (binary: " + Integer.toBinaryString(badge1) + ")");
        System.out.println("  required = " + required1 + " (binary: " + Integer.toBinaryString(required1) + ")");
        System.out.println("  Output:  " + result1);
        System.out.println("  Expected: [1, 1]");
        System.out.println("  Match: " + result1.equals(Arrays.asList(1, 1)));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        //   badge = 12    binary: 01100
        //   required = 21 binary: 10101
        //   Expected Output: [0, 2] per problem, but mathematically [0, 1]
        //
        //   badge & required = 01100 & 10101 = 00100 = 4 ≠ 21 → allowed = 0
        //   badge & ~required:
        //     badge=12=01100: bit2=1,bit3=1
        //     required=21=10101: bit0=1,bit2=1,bit4=1
        //     ~required: bit0=0,bit1=1,bit2=0,bit3=1,bit4=0
        //     12 & ~21: bit3=1 only → extras=1
        //
        //   Note: The problem statement says extras=2 claiming "bits 2 and 3",
        //   but bit2 IS in required=21. The mathematically correct answer is [0,1].
        // -----------------------------------------------------------------------
        int badge2 = 12;
        int required2 = 21;
        List