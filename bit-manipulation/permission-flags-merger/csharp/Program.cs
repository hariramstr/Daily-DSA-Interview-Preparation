/*
 * Permission Flags Merger
 * =======================
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * In a software system, user permissions are represented as bitmasks stored in an integer.
 * Each bit position corresponds to a specific permission:
 *   bit 0 = READ, bit 1 = WRITE, bit 2 = EXECUTE, bit 3 = DELETE, etc. (up to bit 29)
 *
 * Given two arrays, `granted` and `revoked`, each of length n:
 *   - granted[i]: a set of permissions being ADDED at step i
 *   - revoked[i]: a set of permissions being REMOVED at step i
 *
 * Starting from an initial permission mask of 0, process all n steps in order:
 *   1. Apply grant:      mask = mask | granted[i]       (turn ON the granted bits)
 *   2. Apply revocation: mask = mask & (~revoked[i])    (turn OFF the revoked bits)
 *
 * Return the final permission mask after all steps.
 *
 * Constraints:
 *   - 1 <= n <= 10^4
 *   - 0 <= granted[i], revoked[i] <= 2^30 - 1
 *   - granted[i] & revoked[i] == 0 for all i (no overlap between granted and revoked bits)
 *
 * Example 1:
 *   granted = [5, 2], revoked = [0, 1]
 *   Step 0: mask = 0 | 5 = 5 (101), then mask = 5 & ~0 = 5
 *   Step 1: mask = 5 | 2 = 7 (111), then mask = 7 & ~1 = 6 (110)
 *   Output: 6
 *
 * Example 2:
 *   granted = [15, 8, 4], revoked = [0, 3, 8]
 *   Step 0: mask = 0 | 15 = 15, then mask = 15 & ~0 = 15
 *   Step 1: mask = 15 | 8 = 15, then mask = 15 & ~3 = 12
 *   Step 2: mask = 12 | 4 = 12, then mask = 12 & ~8 = 4
 *   Output: 4
 */

using System;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Computes the final permission mask after applying all grant/revoke steps.
    ///
    /// Time Complexity:  O(n) — we iterate through the arrays exactly once.
    ///                   Each step performs two O(1) bitwise operations.
    ///
    /// Space Complexity: O(1) — we only use a single integer variable (mask)
    ///                   regardless of how large n is.
    /// </summary>
    /// <param name="granted">Array of permission bits to add at each step.</param>
    /// <param name="revoked">Array of permission bits to remove at each step.</param>
    /// <returns>The final integer permission mask.</returns>
    public int ComputeFinalMask(int[] granted, int[] revoked)
    {
        // ── STEP 1: Initialise the permission mask ────────────────────────────
        // We start with 0, meaning the user has NO permissions at all.
        // Every bit is 0 (off).  As we process each step we will flip bits
        // on (grant) or off (revoke).
        int mask = 0;

        // ── STEP 2: Determine how many steps to process ───────────────────────
        // Both arrays are guaranteed to have the same length n, so we can use
        // either array's Length property.  We'll use granted.Length for clarity.
        int n = granted.Length;

        // ── STEP 3: Iterate through every step in order ───────────────────────
        // The problem requires us to process steps 0, 1, 2, … n-1 sequentially.
        // The ORDER matters because a later step can grant permissions that an
        // earlier step revoked, or vice-versa.
        for (int i = 0; i < n; i++)
        {
            // ── STEP 3a: Apply the GRANT using bitwise OR ─────────────────────
            //
            // The OR operator ( | ) turns a bit ON if it is ON in EITHER operand.
            //
            //   mask     = ...existing permissions...
            //   granted[i] = ...new permissions to add...
            //   mask | granted[i] = every bit that was already set OR is newly granted
            //
            // Example: mask = 0101 (5), granted[i] = 0010 (2)
            //          result = 0111 (7)  ← both old and new bits are now set
            //
            // Why OR?  Because we want to ADD permissions without disturbing the
            // bits that are already set.  OR never clears a bit that is already 1.
            mask = mask | granted[i];

            // ── STEP 3b: Apply the REVOCATION using AND with bitwise NOT ──────
            //
            // To CLEAR specific bits we use:  mask & (~revoked[i])
            //
            // ~revoked[i]  flips every bit in revoked[i]:
            //   bits that were 1 (permissions to remove) become 0
            //   bits that were 0 (permissions to keep)   become 1
            //
            // Then AND-ing with the flipped value:
            //   - Any bit that is 0 in ~revoked[i] (i.e., was in revoked) → cleared to 0
            //   - Any bit that is 1 in ~revoked[i] (i.e., not in revoked) → unchanged
            //
            // Example: mask = 0111 (7), revoked[i] = 0001 (1)
            //          ~revoked[i] = ...11111110  (all 1s except bit 0)
            //          mask & ~revoked[i] = 0110 (6)  ← bit 0 (READ) is cleared
            //
            // Why AND with NOT?  Because AND only keeps a bit set when BOTH sides
            // are 1.  By flipping the revoked bits to 0 first, we guarantee those
            // positions become 0 in the result.
            mask = mask & (~revoked[i]);
        }

        // ── STEP 4: Return the accumulated permission mask ────────────────────
        // After all n steps the mask holds exactly the permissions the user has.
        return mask;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

Solution solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// granted = [5, 2], revoked = [0, 1]
// Expected output: 6
int[] granted1 = { 5, 2 };
int[] revoked1  = { 0, 1 };

int result1 = solution.ComputeFinalMask(granted1, revoked1);

Console.WriteLine("=== Example 1 ===");
Console.WriteLine($"granted = [{string.Join(", ", granted1)}]");
Console.WriteLine($"revoked = [{string.Join(", ", revoked1)}]");
Console.WriteLine($"Final permission mask : {result1}");          // Expected: 6
Console.WriteLine($"Binary representation : {Convert.ToString(result1, 2).PadLeft(8, '0')}");
Console.WriteLine();

// Manual trace for Example 1:
//   i=0: mask = 0 | 5 = 5  (0000_0101)
//        mask = 5 & ~0 = 5 & 0xFFFFFFFF = 5  (0000_0101)
//   i=1: mask = 5 | 2 = 7  (0000_0111)
//        mask = 7 & ~1 = 7 & 0xFFFFFFFE = 6  (0000_0110)
//   Result: 6  ✓

// ── Example 2 ────────────────────────────────────────────────────────────────
// granted = [15, 8, 4], revoked = [0, 3, 8]
// Expected output: 4
int[] granted2 = { 15, 8, 4 };
int[] revoked2  = { 0,  3, 8 };

int result2 = solution.ComputeFinalMask(granted2, revoked2);

Console.WriteLine("=== Example 2 ===");
Console.WriteLine($"granted = [{string.Join(", ", granted2)}]");
Console.WriteLine($"revoked = [{string.Join(", ", revoked2)}]");
Console.WriteLine($"Final permission mask : {result2}");          // Expected: 4
Console.WriteLine($"Binary representation : {Convert.ToString(result2, 2).PadLeft(8, '0')}");
Console.WriteLine();

// Manual trace for Example 2:
//   i=0: mask = 0  | 15 = 15  (0000_1111)
//        mask = 15 & ~0  = 15 (0000_1111)
//   i=1: mask = 15 | 8  = 15  (0000_1111)  — bit 3 already set
//        mask = 15 & ~3  = 15 & 0xFFFFFFFC = 12 (0000_1100)
//   i=2: mask = 12 | 4  = 12  (0000_1100)  — bit 2 already set
//        mask = 12 & ~8  = 12 & 0xFFFFFFF7 = 4  (0000_0100)
//   Result: 4  ✓

// ── Additional edge-case example ─────────────────────────────────────────────
// Single step: grant everything (all 30 bits), revoke nothing.
int[] granted3 = { (1 << 30) - 1 };   // 2^30 - 1  (all 30 bits set)
int[] revoked3  = { 0 };

int result3 = solution.ComputeFinalMask(granted3, revoked3);

Console.WriteLine("=== Edge Case: Grant all 30 bits, revoke nothing ===");
Console.WriteLine($"granted = [{granted3[0]}]  (all 30 bits set)");
Console.WriteLine($"revoked = [{revoked3[0]}]");
Console.WriteLine($"Final permission mask : {result3}");          // Expected: 1073741823
Console.WriteLine($"Binary representation : {Convert.ToString(result3, 2)}");
Console.WriteLine();

// ── Additional edge-case: grant then immediately revoke the same bits ─────────
// Because the constraint says granted[i] & revoked[i] == 0, this scenario
// cannot happen in a single step, but across two steps it can:
int[] granted4 = { 7, 0 };   // step 0: grant bits 0,1,2 ; step 1: grant nothing
int[] revoked4  = { 0, 7 };  // step 0: revoke nothing   ; step 1: revoke bits 0,1,2

int result4 = solution.ComputeFinalMask(granted4, revoked4);

Console.WriteLine("=== Edge Case: Grant bits 0-2, then revoke bits 0-2 ===");
Console.WriteLine($"granted = [{string.Join(", ", granted4)}]");
Console.WriteLine($"revoked = [{string.Join(", ", revoked4)}]");
Console.WriteLine($"Final permission mask : {result4}");          // Expected: 0
Console.WriteLine($"Binary representation : {Convert.ToString(result4, 2).PadLeft(8, '0')}");