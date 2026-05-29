/*
 * USB Port Power Negotiation Bits
 * ================================
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A USB hub has n ports, each represented by a single bit in a 32-bit integer `status`.
 * A bit value of 1 means the port is currently active (drawing power), and 0 means idle.
 *
 * Given:
 *   - `status`: integer representing current port states
 *   - `mask`: integer representing ports to toggle (flip their state)
 *   - `k`: number of low-order bit positions to examine for active count
 *
 * Steps:
 *   1. XOR status with mask to toggle the indicated bits → newStatus
 *   2. Count bits that are 1 in positions 0 through k-1 of newStatus → activeCount
 *
 * Return [newStatus, activeCount]
 *
 * Constraints:
 *   0 <= status <= 2^31 - 1
 *   0 <= mask   <= 2^31 - 1
 *   1 <= k <= 32
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

using System;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Toggles the bits indicated by <paramref name="mask"/> in <paramref name="status"/>,
    /// then counts how many of the first <paramref name="k"/> bits are active (1) in the result.
    /// </summary>
    /// <param name="status">Current port states as a 32-bit non-negative integer.</param>
    /// <param name="mask">Ports to toggle; a 1-bit means "flip this port".</param>
    /// <param name="k">Number of low-order bit positions (0 … k-1) to inspect.</param>
    /// <returns>int[] { newStatus, activeCount }</returns>
    ///
    /// Time Complexity  : O(k)  – we iterate over at most k bit positions (k ≤ 32, so O(1) in practice)
    /// Space Complexity : O(1)  – only a fixed number of integer variables are used
    public int[] ToggleAndCount(int status, int mask, int k)
    {
        // ── Step 1: Toggle the ports indicated by mask ────────────────────────
        //
        // XOR (^) is the perfect tool for toggling individual bits:
        //   0 ^ 1 = 1  (idle port becomes active)
        //   1 ^ 1 = 0  (active port becomes idle)
        //   0 ^ 0 = 0  (port not in mask → unchanged)
        //   1 ^ 0 = 1  (port not in mask → unchanged)
        //
        // So XOR-ing status with mask flips exactly the bits where mask has a 1,
        // and leaves all other bits untouched.
        int newStatus = status ^ mask;

        // ── Step 2: Count active ports in the first k bit positions ───────────
        //
        // We need to examine bits at positions 0, 1, 2, …, k-1
        // (position 0 is the Least Significant Bit).
        //
        // Strategy: iterate i from 0 to k-1.
        //   For each position i, shift newStatus right by i places so that
        //   the bit we care about lands at position 0, then AND with 1 to
        //   isolate it.  If the result is 1, that port is active → increment counter.
        //
        //   Example: newStatus = 7 (binary 0111), k = 4
        //     i=0: (7 >> 0) & 1 = 7 & 1 = 1  → active
        //     i=1: (7 >> 1) & 1 = 3 & 1 = 1  → active
        //     i=2: (7 >> 2) & 1 = 1 & 1 = 1  → active
        //     i=3: (7 >> 3) & 1 = 0 & 1 = 0  → idle
        //   activeCount = 3  ✓

        int activeCount = 0; // accumulator for active-port count

        for (int i = 0; i < k; i++)
        {
            // Shift newStatus right by i positions so bit i is now at position 0.
            // We use the unsigned right-shift operator (>>>) to avoid sign-extension
            // issues when the most-significant bit (bit 31) is set.
            // Then AND with 1 to read only that single bit.
            int bit = (newStatus >>> i) & 1;

            // If the isolated bit is 1, this port is active → count it.
            if (bit == 1)
            {
                activeCount++;
            }
        }

        // ── Step 3: Return the results as a two-element array ─────────────────
        //
        // Index 0 → newStatus  (the toggled port-state integer)
        // Index 1 → activeCount (number of active ports in positions 0…k-1)
        return new int[] { newStatus, activeCount };
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// status = 13  → binary ...01101
// mask   = 10  → binary ...01010
// k      = 4
// XOR    = 7   → binary ...00111
// Active in first 4 bits (0111): positions 0,1,2 → count = 3
// Expected output: [7, 3]
int status1 = 13, mask1 = 10, k1 = 4;
int[] result1 = solution.ToggleAndCount(status1, mask1, k1);
Console.WriteLine("=== Example 1 ===");
Console.WriteLine($"  status  = {status1,3}  (binary: {Convert.ToString(status1, 2).PadLeft(8, '0')})");
Console.WriteLine($"  mask    = {mask1,3}  (binary: {Convert.ToString(mask1,   2).PadLeft(8, '0')})");
Console.WriteLine($"  k       = {k1}");
Console.WriteLine($"  XOR     = {result1[0],3}  (binary: {Convert.ToString(result1[0], 2).PadLeft(8, '0')})");
Console.WriteLine($"  Output  : [newStatus={result1[0]}, activeCount={result1[1]}]");
Console.WriteLine($"  Expected: [newStatus=7, activeCount=3]");
Console.WriteLine($"  PASS    : {result1[0] == 7 && result1[1] == 3}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// status = 255 → binary 11111111
// mask   = 170 → binary 10101010
// k      = 8
// XOR    = 85  → binary 01010101
// Active in first 8 bits (01010101): positions 0,2,4,6 → count = 4
// Expected output: [85, 4]
int status2 = 255, mask2 = 170, k2 = 8;
int[] result2 = solution.ToggleAndCount(status2, mask2, k2);
Console.WriteLine("=== Example 2 ===");
Console.WriteLine($"  status  = {status2,3}  (binary: {Convert.ToString(status2, 2).PadLeft(8, '0')})");
Console.WriteLine($"  mask    = {mask2,3}  (binary: {Convert.ToString(mask2,   2).PadLeft(8, '0')})");
Console.WriteLine($"  k       = {k2}");
Console.WriteLine($"  XOR     = {result2[0],3}  (binary: {Convert.ToString(result2[0], 2).PadLeft(8, '0')})");
Console.WriteLine($"  Output  : [newStatus={result2[0]}, activeCount={result2[1]}]");
Console.WriteLine($"  Expected: [newStatus=85, activeCount=4]");
Console.WriteLine($"  PASS    : {result2[0] == 85 && result2[1] == 4}");
Console.WriteLine();

// ── Extra edge-case: all bits toggled, k = 32 ────────────────────────────────
// status = 0, mask = int.MaxValue (all 31 low bits set), k = 32
// XOR    = int.MaxValue (binary 0111...1111)
// Active in first 32 bits: 31 bits set → count = 31
int status3 = 0, mask3 = int.MaxValue, k3 = 32;
int[] result3 = solution.ToggleAndCount(status3, mask3, k3);
Console.WriteLine("=== Edge Case: status=0, mask=int.MaxValue, k=32 ===");
Console.WriteLine($"  newStatus   = {result3[0]}  (expected {int.MaxValue})");
Console.WriteLine($"  activeCount = {result3[1]}  (expected 31)");
Console.WriteLine($"  PASS        : {result3[0] == int.MaxValue && result3[1] == 31}");
Console.WriteLine();

// ── Extra edge-case: no bits toggled ─────────────────────────────────────────
// status = 7 (0111), mask = 0, k = 3
// XOR    = 7 (unchanged)
// Active in first 3 bits: positions 0,1,2 → count = 3
int status4 = 7, mask4 = 0, k4 = 3;
int[] result4 = solution.ToggleAndCount(status4, mask4, k4);
Console.WriteLine("=== Edge Case: status=7, mask=0, k=3 ===");
Console.WriteLine($"  newStatus   = {result4[0]}  (expected 7)");
Console.WriteLine($"  activeCount = {result4[1]}  (expected 3)");
Console.WriteLine($"  PASS        : {result4[0] == 7 && result4[1] == 3}");