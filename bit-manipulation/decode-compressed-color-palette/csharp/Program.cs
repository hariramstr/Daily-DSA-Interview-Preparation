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
 * Given an array of encoded color integers and a list of transformation operations,
 * each operation sets a specific channel (A, R, G, B) to a given value (0–255)
 * for ALL colors using bitwise operations only.
 *
 * After applying all operations in order, return the array of final encoded 32-bit
 * color integers.
 */

using System;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    // -------------------------------------------------------------------------
    // Method: ApplyColorOperations
    //
    // Time Complexity:  O(C * N)  where C = number of operations, N = number of colors
    //                  Each operation iterates over every color once.
    //
    // Space Complexity: O(N)  — we work in-place on the input array (or a copy),
    //                  so no extra space beyond the output array itself.
    // -------------------------------------------------------------------------
    public uint[] ApplyColorOperations(uint[] colors, (char channel, int value)[] operations)
    {
        // ── Step 1: Work on a copy so we don't mutate the caller's original array.
        //    This is good practice and makes the function "pure" (no side effects).
        uint[] result = (uint[])colors.Clone();

        // ── Step 2: Process each operation one at a time, in order.
        //    Operations are applied sequentially, so the output of one feeds
        //    into the next — order matters!
        foreach (var (channel, value) in operations)
        {
            // ── Step 3: Determine the bit-shift amount for the target channel.
            //
            //    The 32-bit layout is:  [AAAAAAAA][RRRRRRRR][GGGGGGGG][BBBBBBBB]
            //                           bits 31-24  23-16     15-8      7-0
            //
            //    To reach a channel we need to know how many bits it is from
            //    the least-significant bit (bit 0):
            //      Alpha  → shift 24
            //      Red    → shift 16
            //      Green  → shift  8
            //      Blue   → shift  0
            int shift = channel switch
            {
                'A' => 24,
                'R' => 16,
                'G' => 8,
                'B' => 0,
                _   => throw new ArgumentException($"Unknown channel: {channel}")
            };

            // ── Step 4: Build a CLEAR MASK for the target channel.
            //
            //    We want a 32-bit mask that has 0s in the 8 bits of the target
            //    channel and 1s everywhere else.  This lets us zero-out just
            //    that channel while leaving all other channels untouched.
            //
            //    0xFF        = 0000_0000_0000_0000_0000_0000_1111_1111  (8 ones)
            //    0xFF << shift moves those 8 ones to the correct position.
            //    ~(...)      flips all bits → 8 zeros at the channel position,
            //                ones everywhere else.
            //
            //    Example for Green (shift=8):
            //      0xFF << 8  = 0x0000FF00
            //      ~0x0000FF00 = 0xFFFF00FF   ← clear mask for green
            uint clearMask = ~((uint)0xFF << shift);

            // ── Step 5: Build a SET MASK for the new channel value.
            //
            //    We take the new value (0–255) and shift it into the correct
            //    bit position so it aligns with the target channel.
            //
            //    Example: value=128 (0x80), Green (shift=8):
            //      (uint)128 << 8 = 0x00008000   ← set mask
            uint setMask = (uint)value << shift;

            // ── Step 6: Apply both masks to every color in the array.
            //
            //    For each color we do a two-step bitwise update:
            //
            //    a) (color & clearMask)  — zeros out the 8 bits of the target
            //       channel while keeping all other channels intact.
            //
            //    b) | setMask            — writes the new channel value into
            //       those now-zero bits.
            //
            //    Because we cleared first and then OR'd, no other channel is
            //    affected.  This is the standard "clear-then-set" bit pattern.
            for (int i = 0; i < result.Length; i++)
            {
                // Clear the target channel bits, then set them to the new value.
                result[i] = (result[i] & clearMask) | setMask;
            }
        }

        // ── Step 7: Return the fully transformed color array.
        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solver = new Solution();

// ── Helper: print a uint array as hex values
static void PrintColors(string label, uint[] arr)
{
    Console.Write($"{label}: [");
    for (int i = 0; i < arr.Length; i++)
    {
        Console.Write($"0x{arr[i]:X8}");
        if (i < arr.Length - 1) Console.Write(", ");
    }
    Console.WriteLine("]");
}

// ─────────────────────────────────────────────────────────────────────────────
// Example 1
// Input:  colors = [0xFFFF0000, 0x00FF00FF]
//         operations = [('G', 128), ('A', 0)]
// Expected Output: [0x00FF8000, 0x00FF80FF]
//
// Trace:
//   Color 0xFFFF0000:
//     Set G=128(0x80): clearMask=0xFFFF00FF, setMask=0x00008000
//       (0xFFFF0000 & 0xFFFF00FF) | 0x00008000
//       = 0xFFFF0000 | 0x00008000
//       = 0xFFFF8000
//     Set A=0:         clearMask=0x00FFFFFF, setMask=0x00000000
//       (0xFFFF8000 & 0x00FFFFFF) | 0x00000000
//       = 0x00FF8000
//   Color 0x00FF00FF:
//     Set G=128(0x80): (0x00FF00FF & 0xFFFF00FF) | 0x00008000
//       = 0x00FF00FF | 0x00008000
//       = 0x00FF80FF
//     Set A=0:         (0x00FF80FF & 0x00FFFFFF) | 0x00000000
//       = 0x00FF80FF
// ─────────────────────────────────────────────────────────────────────────────
Console.WriteLine("=== Example 1 ===");
uint[] colors1 = [0xFFFF0000, 0x00FF00FF];
(char, int)[] ops1 = [('G', 128), ('A', 0)];
uint[] result1 = solver.ApplyColorOperations(colors1, ops1);
PrintColors("Input ", colors1);
PrintColors("Output", result1);
Console.WriteLine($"Expected: [0x00FF8000, 0x00FF80FF]");
Console.WriteLine($"Correct:  {result1[0] == 0x00FF8000u && result1[1] == 0x00FF80FFu}");
Console.WriteLine();

// ─────────────────────────────────────────────────────────────────────────────
// Example 2
// Input:  colors = [0x12345678]
//         operations = [('B', 255), ('R', 0)]
// Expected Output: [0x120000FF]
//
// Trace:
//   Color 0x12345678:
//     Set B=255(0xFF): clearMask=0xFFFFFF00, setMask=0x000000FF
//       (0x12345678 & 0xFFFFFF00) | 0x000000FF
//       = 0x12345600 | 0x000000FF
//       = 0x123456FF
//     Set R=0:         clearMask=0xFF00FFFF, setMask=0x00000000
//       (0x123456FF & 0xFF00FFFF) | 0x00000000
//       = 0x120056FF ... wait, let me re-check.
//       0x123456FF in binary:
//         A=0x12, R=0x34, G=0x56, B=0xFF
//       clearMask for R (shift=16): ~(0xFF << 16) = ~0x00FF0000 = 0xFF00FFFF
//       0x123456FF & 0xFF00FFFF = 0x120056FF
//       | 0x00000000 = 0x120056FF
//       Hmm — but expected is 0x120000FF.
//       Wait: G channel is 0x56 and should remain. Let me re-read the expected.
//       Expected: 0x120000FF → A=0x12, R=0x00, G=0x00, B=0xFF
//       But only R is set to 0, G should stay 0x56 → 0x120056FF?
//       Re-reading problem: "Red channel set to 0: 0x120000FF"
//       0x123456FF: A=0x12, R=0x34, G=0x56, B=0xFF
//       After R=0: A=0x12, R=0x00, G=0x56, B=0xFF → 0x120056FF
//       The problem statement says 0x120000FF which seems wrong in the problem,
//       but let's trust the problem. Actually re-reading: "0x123456FF. Red channel
//       set to 0: 0x120000FF" — this looks like a typo in the problem (G should
//       stay 0x56). Our algorithm correctly produces 0x120056FF.
//       We'll show both and note the discrepancy.
// ─────────────────────────────────────────────────────────────────────────────
Console.WriteLine("=== Example 2 ===");
uint[] colors2 = [0x12345678];
(char, int)[] ops2 = [('B', 255), ('R', 0)];
uint[] result2 = solver.ApplyColorOperations(colors2, ops2);
PrintColors("Input ", colors2);
PrintColors("Output", result2);
// Our algorithm: B=255 → 0x123456FF, R=0 → 0x120056FF (G=0x56 preserved correctly)
Console.WriteLine($"Our result:          0x{result2[0]:X8}");
Console.WriteLine($"Problem says:        0x120000FF  (likely a typo; G=0x56 should be preserved)");
Console.WriteLine($"Algorithmically correct (G preserved): {result2[0] == 0x120056FFu}");
Console.WriteLine();

// ─────────────────────────────────────────────────────────────────────────────
// Additional Test: All channels set explicitly
// ─────────────────────────────────────────────────────────────────────────────
Console.WriteLine("=== Additional Test: Set all channels ===");
uint[] colors3 = [0xDEADBEEF, 0x00000000, 0xFFFFFFFF];
(char, int)[] ops3 = [('A', 255), ('R', 100), ('G', 150), ('B', 200)];
uint[] result3 = solver.ApplyColorOperations(colors3, ops3);
PrintColors("Input ", colors3);
PrintColors("Output", result3);
// Expected: all colors become 0xFF6496C8
//   A=255=0xFF, R=100=0x64, G=150=0x96, B=200=0xC8 → 0xFF6496C8
Console.WriteLine($"Expected: all 0xFF6496C8");
Console.WriteLine($"Correct:  {result3[0] == 0xFF6496C8u && result3[1] == 0xFF6496C8u && result3[2] == 0xFF6496C8u}");
Console.WriteLine();

// ─────────────────────────────────────────────────────────────────────────────
// Additional Test: Single color, no-op (set channel to its existing value)
// ─────────────────────────────────────────────────────────────────────────────
Console.WriteLine("=== Additional Test: No-op (set to existing value) ===");
uint[] colors4 = [0xAABBCCDD];
(char, int)[] ops4 = [('A', 0xAA), ('R', 0xBB), ('G', 0xCC), ('B', 0xDD)];
uint[] result4 = solver.ApplyColorOperations(colors4, ops4);
PrintColors("Input ", colors4);
PrintColors("Output", result4);
Console.WriteLine($"Expected: [0xAABBCCDD]");
Console.WriteLine($"Correct:  {result4[0] == 0xAABBCCDDu}");