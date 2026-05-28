/*
 * Permission Flags Merger
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * In a software system, user permissions are represented as bitmasks stored in an integer.
 * Each bit position corresponds to a specific permission:
 *   bit 0 = READ, bit 1 = WRITE, bit 2 = EXECUTE, bit 3 = DELETE, and so on up to bit 29.
 *
 * You are given two arrays, `granted` and `revoked`, each of length `n`.
 * The `granted[i]` value represents a set of permissions being added to a user,
 * and `revoked[i]` represents a set of permissions being removed from the same user at step `i`.
 *
 * Starting from an initial permission mask of `0`, process all `n` steps in order:
 *   1. First apply the grant:     mask = mask | granted[i]
 *   2. Then apply the revocation: mask = mask & (~revoked[i])
 *
 * Return the final permission mask after all steps have been processed.
 *
 * Constraints:
 *   - 1 <= n <= 10^4
 *   - 0 <= granted[i], revoked[i] <= 2^30 - 1
 *   - granted[i] & revoked[i] == 0 for all i (no shared bits)
 *
 * Example 1:
 *   Input:  granted = [5, 2], revoked = [0, 1]
 *   Output: 6
 *
 * Example 2:
 *   Input:  granted = [15, 8, 4], revoked = [0, 3, 8]
 *   Output: 4
 */

public class Solution {

    /**
     * Computes the final permission bitmask after processing all grant/revoke steps.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>Start with mask = 0 (no permissions).</li>
     *   <li>For each step i, OR the mask with granted[i] to add permissions.</li>
     *   <li>Then AND the mask with the bitwise NOT of revoked[i] to remove permissions.</li>
     *   <li>Return the resulting mask after all steps.</li>
     * </ol>
     *
     * @param granted an array where granted[i] is the set of permissions added at step i
     * @param revoked an array where revoked[i] is the set of permissions removed at step i
     * @return the final integer permission mask after all n steps
     *
     * Time Complexity:  O(n) — we iterate through each of the n steps exactly once.
     * Space Complexity: O(1) — we only use a single integer variable (mask) for state.
     */
    public int computeFinalPermissions(int[] granted, int[] revoked) {
        // Step 1: Initialize the permission mask to 0.
        // This means the user starts with NO permissions at all.
        int mask = 0;

        // Step 2: Determine the number of steps (both arrays have the same length n).
        int n = granted.length;

        // Step 3: Iterate through each step from index 0 to n-1.
        for (int i = 0; i < n; i++) {

            // ---- SUB-STEP A: Apply the GRANT ----
            // The OR operation (|) sets all bits that are set in granted[i].
            // Any bit that is 1 in granted[i] will become 1 in mask.
            // Bits already set in mask remain set; new bits from granted[i] are added.
            //
            // Example: mask = 0b101 (5), granted[i] = 0b010 (2)
            //          mask | granted[i] = 0b111 (7)  <-- WRITE permission added
            mask = mask | granted[i];

            // ---- SUB-STEP B: Apply the REVOCATION ----
            // The bitwise NOT (~) flips all bits of revoked[i].
            //   e.g., ~0b001 = 0b...11111110  (all bits set except bit 0)
            // The AND operation (&) then clears every bit that was set in revoked[i].
            // Bits NOT in revoked[i] are preserved; bits IN revoked[i] are cleared.
            //
            // Example: mask = 0b111 (7), revoked[i] = 0b001 (1)
            //          ~revoked[i] = 0b...11111110
            //          mask & ~revoked[i] = 0b110 (6)  <-- READ permission removed
            mask = mask & (~revoked[i]);
        }

        // Step 4: Return the final permission mask after all steps have been processed.
        return mask;
    }

    /**
     * Entry point — demonstrates the solution with the provided sample inputs
     * and prints the results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1 Trace:
        //   granted = [5, 2],  revoked = [0, 1]
        //
        //   Initial mask = 0  (binary: 000)
        //
        //   Step 0:
        //     Grant:   mask = 0 | 5 = 5   (binary: 101)
        //     Revoke:  mask = 5 & ~0 = 5  (~0 = all 1s, so nothing cleared)
        //              (binary: 101)
        //
        //   Step 1:
        //     Grant:   mask = 5 | 2 = 7   (binary: 111)
        //     Revoke:  mask = 7 & ~1 = 6  (~1 = ...11111110, clears bit 0)
        //              (binary: 110)
        //
        //   Final mask = 6  ✓
        // ---------------------------------------------------------------
        int[] granted1 = {5, 2};
        int[] revoked1 = {0, 1};
        int result1 = solution.computeFinalPermissions(granted1, revoked1);
        System.out.println("Example 1:");
        System.out.println("  granted = [5, 2], revoked = [0, 1]");
        System.out.println("  Expected: 6");
        System.out.println("  Got:      " + result1);
        System.out.println("  Pass: " + (result1 == 6));
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2 Trace:
        //   granted = [15, 8, 4],  revoked = [0, 3, 8]
        //
        //   Initial mask = 0  (binary: 0000)
        //
        //   Step 0:
        //     Grant:   mask = 0  | 15 = 15  (binary: 1111)
        //     Revoke:  mask = 15 & ~0  = 15 (~0 = all 1s, nothing cleared)
        //              (binary: 1111)
        //
        //   Step 1:
        //     Grant:   mask = 15 | 8  = 15  (binary: 1111, bit 3 already set)
        //     Revoke:  mask = 15 & ~3 = 12  (~3 = ...11111100, clears bits 0 and 1)
        //              (binary: 1100)
        //
        //   Step 2:
        //     Grant:   mask = 12 | 4  = 12  (binary: 1100, bit 2 already set? 
        //              Wait: 12 = 1100, 4 = 0100 → 12 | 4 = 1100 = 12)
        //              Actually bit 2 IS already set in 12 (1100), so OR changes nothing.
        //     Revoke:  mask = 12 & ~8 = 4   (~8 = ...11110111, clears bit 3)
        //              12 = 1100, ~8 = ...11110111 → 12 & ~8 = 0100 = 4
        //              (binary: 0100)
        //
        //   Final mask = 4  ✓
        // ---------------------------------------------------------------
        int[] granted2 = {15, 8, 4};
        int[] revoked2 = {0, 3, 8};
        int result2 = solution.computeFinalPermissions(granted2, revoked2);
        System.out.println("Example 2:");
        System.out.println("  granted = [15, 8, 4], revoked = [0, 3, 8]");
        System.out.println("  Expected: 4");
        System.out.println("  Got:      " + result2);
        System.out.println("  Pass: " + (result2 == 4));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional edge-case: single step, grant everything, revoke nothing
        // ---------------------------------------------------------------
        int[] granted3 = {0b111};   // 7 — READ + WRITE + EXECUTE
        int[] revoked3 = {0};       // revoke nothing
        int result3 = solution.computeFinalPermissions(granted3, revoked3);
        System.out.println("Edge Case (grant=7, revoke=0):");
        System.out.println("  Expected: 7");
        System.out.println("  Got:      " + result3);
        System.out.println("  Pass: " + (result3 == 7));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional edge-case: grant then immediately revoke the same bits
        // (granted & revoked == 0 is guaranteed, so this tests grant=0, revoke=all)
        // ---------------------------------------------------------------
        int[] granted4 = {0};
        int[] revoked4 = {0b1111};  // 15 — revoke READ, WRITE, EXECUTE, DELETE
        int result4 = solution.computeFinalPermissions(granted4, revoked4);
        System.out.println("Edge Case (grant=0, revoke=15):");
        System.out.println("  Expected: 0");
        System.out.println("  Got:      " + result4);
        System.out.println("  Pass: " + (result4 == 0));
    }
}