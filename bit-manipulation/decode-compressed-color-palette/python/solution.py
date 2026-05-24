"""
Decode Compressed Color Palette
================================
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
A graphics application stores colors in a compact 32-bit integer format. Each color is encoded as follows:
- Bits 24–31 (8 bits): Alpha channel (0–255)
- Bits 16–23 (8 bits): Red channel (0–255)
- Bits 8–15 (8 bits): Green channel (0–255)
- Bits 0–7 (8 bits): Blue channel (0–255)

You are given an array of encoded color integers and a list of transformation operations.
Each operation is a tuple [channel, value] where channel is one of 'A', 'R', 'G', 'B',
and value is an integer (0–255). The operation sets that specific channel to value for ALL
colors in the array using bitwise operations only (no arithmetic).

After applying all operations in order, return the array of final encoded 32-bit color integers.

Constraints:
- 1 <= colors.length <= 10^5
- 0 <= colors[i] <= 2^32 - 1
- 1 <= operations.length <= 100
- Each channel is one of 'A', 'R', 'G', 'B'
- 0 <= value <= 255
"""

from typing import List, Dict, Tuple


class Solution:
    def decode_compressed_palette(
        self, colors: List[int], operations: List[List]
    ) -> List[int]:
        """
        Apply a series of channel-set operations to an array of 32-bit ARGB color integers.

        Each operation sets a specific 8-bit channel (Alpha, Red, Green, or Blue) to a
        given value for every color in the array, using only bitwise operations.

        Args:
            colors (List[int]): List of 32-bit encoded color integers in ARGB format.
            operations (List[List]): List of [channel, value] pairs where channel is
                                     one of 'A', 'R', 'G', 'B' and value is 0–255.

        Returns:
            List[int]: The modified list of 32-bit encoded color integers after all
                       operations have been applied.

        Time Complexity: O(C * N) where C is the number of operations and N is the
                         number of colors. Each operation iterates over all colors.
        Space Complexity: O(N) for the result list (or O(1) extra if modifying in place).
        """

        # -----------------------------------------------------------------------
        # STEP 1: Define channel metadata
        # -----------------------------------------------------------------------
        # For each channel name, we need two things:
        #   - shift: how many bits to shift left to position the 8-bit value
        #   - mask:  a 32-bit mask with 0s in the channel's 8 bits and 1s elsewhere
        #
        # Why a mask with 0s in the channel position?
        #   To CLEAR the existing channel bits before OR-ing in the new value.
        #   This is the standard "clear then set" bitwise pattern:
        #       new_color = (old_color & clear_mask) | (new_value << shift)
        #
        # Channel positions in the 32-bit integer:
        #   Alpha  → bits 24–31  → shift = 24
        #   Red    → bits 16–23  → shift = 16
        #   Green  → bits  8–15  → shift =  8
        #   Blue   → bits  0–7   → shift =  0
        #
        # The clear mask for each channel is the bitwise NOT of (0xFF << shift),
        # but since Python integers are arbitrary precision (not fixed 32-bit),
        # we AND with 0xFFFFFFFF to keep only 32 bits in the mask.

        channel_info: Dict[str, Tuple[int, int]] = {
            # channel_name: (shift_amount, clear_mask)
            'A': (24, ~(0xFF << 24) & 0xFFFFFFFF),  # Clear bits 24–31
            'R': (16, ~(0xFF << 16) & 0xFFFFFFFF),  # Clear bits 16–23
            'G': ( 8, ~(0xFF <<  8) & 0xFFFFFFFF),  # Clear bits  8–15
            'B': ( 0, ~(0xFF <<  0) & 0xFFFFFFFF),  # Clear bits  0–7
        }

        # -----------------------------------------------------------------------
        # STEP 2: Work on a copy of the colors list so we don't mutate the input
        # -----------------------------------------------------------------------
        # This is good practice: callers generally don't expect their input
        # list to be modified by a function they call.
        result: List[int] = list(colors)

        # -----------------------------------------------------------------------
        # STEP 3: Apply each operation in order
        # -----------------------------------------------------------------------
        # Operations must be applied sequentially because a later operation may
        # overwrite a channel set by an earlier one.
        for op in operations:
            channel: str = op[0]   # e.g., 'G'
            value: int   = op[1]   # e.g., 128

            # ------------------------------------------------------------------
            # STEP 3a: Look up the shift and clear mask for this channel
            # ------------------------------------------------------------------
            shift, clear_mask = channel_info[channel]

            # ------------------------------------------------------------------
            # STEP 3b: Pre-compute the "set bits" for this operation
            # ------------------------------------------------------------------
            # Instead of recomputing (value << shift) for every color, we
            # compute it once here. This is a minor optimization.
            #
            # Example: channel='G', value=128 (0x80), shift=8
            #   set_bits = 0x80 << 8 = 0x00008000
            set_bits: int = (value & 0xFF) << shift
            # We AND value with 0xFF as a safety measure to ensure it fits in 8 bits,
            # even though the problem guarantees 0 <= value <= 255.

            # ------------------------------------------------------------------
            # STEP 3c: Apply the "clear then set" pattern to every color
            # ------------------------------------------------------------------
            # For each color:
            #   1. (color & clear_mask) → zeroes out the target channel's 8 bits,
            #      leaving all other channels unchanged.
            #   2. | set_bits           → writes the new channel value into those
            #      now-zeroed bits.
            #   3. & 0xFFFFFFFF         → ensures the result stays within 32 bits
            #      (important in Python where integers have unlimited precision).
            for i in range(len(result)):
                result[i] = (result[i] & clear_mask) | set_bits
                # No need to AND with 0xFFFFFFFF here because:
                #   - clear_mask already has at most 32 significant bits (we ensured this above)
                #   - set_bits has at most 32 significant bits
                #   - The original color values are guaranteed <= 2^32 - 1
                # But to be extra safe and explicit, we can add it:
                result[i] &= 0xFFFFFFFF

        # -----------------------------------------------------------------------
        # STEP 4: Return the transformed color list
        # -----------------------------------------------------------------------
        return result


# =============================================================================
# MANUAL TRACE / VERIFICATION
# =============================================================================
# Example 1:
#   colors = [0xFFFF0000, 0x00FF00FF]
#   operations = [['G', 128], ['A', 0]]
#
#   channel_info:
#     'G': shift=8,  clear_mask = ~(0xFF << 8)  & 0xFFFFFFFF = 0xFFFF00FF
#     'A': shift=24, clear_mask = ~(0xFF << 24) & 0xFFFFFFFF = 0x00FFFFFF
#
#   Operation ['G', 128]:
#     set_bits = 128 << 8 = 0x00008000
#     color 0xFFFF0000:
#       (0xFFFF0000 & 0xFFFF00FF) | 0x00008000
#       = 0xFFFF0000 | 0x00008000   ← wait, let me redo:
#       0xFFFF0000 & 0xFFFF00FF:
#         F F F F 0 0 0 0
#       & F F F F 0 0 F F
#       = F F F F 0 0 0 0   → 0xFFFF0000
#       | 0x00008000        → 0xFFFF8000  ✓
#     color 0x00FF00FF:
#       (0x00FF00FF & 0xFFFF00FF) | 0x00008000
#       0x00FF00FF & 0xFFFF00FF = 0x00FF00FF
#       | 0x00008000 = 0x00FF80FF  ✓
#
#   Operation ['A', 0]:
#     set_bits = 0 << 24 = 0x00000000
#     color 0xFFFF8000:
#       (0xFFFF8000 & 0x00FFFFFF) | 0x00000000
#       = 0x00FF8000  ✓
#     color 0x00FF80FF:
#       (0x00FF80FF & 0x00FFFFFF) | 0x00000000
#       = 0x00FF80FF  ✓
#
#   Output: [0x00FF8000, 0x00FF80FF]  ✓ matches expected
#
# Example 2:
#   colors = [0x12345678]
#   operations = [['B', 255], ['R', 0]]
#
#   channel_info:
#     'B': shift=0,  clear_mask = ~(0xFF << 0)  & 0xFFFFFFFF = 0xFFFFFF00
#     'R': shift=16, clear_mask = ~(0xFF << 16) & 0xFFFFFFFF = 0xFF00FFFF
#
#   Operation ['B', 255]:
#     set_bits = 255 << 0 = 0x000000FF
#     color 0x12345678:
#       (0x12345678 & 0xFFFFFF00) | 0x000000FF
#       = 0x12345600 | 0x000000FF
#       = 0x123456FF  ✓
#
#   Operation ['R', 0]:
#     set_bits = 0 << 16 = 0x00000000
#     color 0x123456FF:
#       (0x123456FF & 0xFF00FFFF) | 0x00000000
#       = 0x120056FF  ← Hmm, let me check:
#       0x123456FF & 0xFF00FFFF:
#         1 2 3 4 5 6 F F
#       & F F 0 0 F F F F
#       = 1 2 0 0 5 6 F F  → 0x120056FF
#
#   But expected output is 0x120000FF ...
#   Wait — let me re-read the problem. Red is bits 16–23.
#   0x123456FF:
#     bits 31–24 (Alpha): 0x12
#     bits 23–16 (Red):   0x34
#     bits 15–8  (Green): 0x56
#     bits  7–0  (Blue):  0xFF
#   Setting Red to 0:
#     clear_mask for R = ~(0xFF << 16) & 0xFFFFFFFF = 0xFF00FFFF
#     0x123456FF & 0xFF00FFFF:
#       12 34 56 FF
#     & FF 00 FF FF
#     = 12 00 56 FF → 0x120056FF
#
#   But expected is 0x120000FF. That means Green also became 0?
#   Let me re-read Example 2 explanation:
#     "Blue channel set to 255 (0xFF): 0x123456FF. Red channel set to 0: 0x120000FF."
#   0x120000FF means Alpha=0x12, Red=0x00, Green=0x00, Blue=0xFF.
#   But we only set Red to 0, not Green. Green was 0x56 originally.
#
#   Hmm, let me re-examine the original color 0x12345678:
#     0x12 = Alpha, 0x34 = Red, 0x56 = Green, 0x78 = Blue
#   After ['B', 255]: Blue → 0xFF → 0x123456FF  (Green still 0x56)
#   After ['R', 0]:   Red  → 0x00 → 0x120056FF
#
#   So the expected output 0x120000FF seems wrong in the problem statement,
#   OR I'm misreading the bit layout.
#
#   Wait — maybe the problem statement's Example 2 has a typo/error.
#   Let me re-check: 0x12345678 with R=0 should give 0x120056FF not 0x120000FF.
#   I'll trust my bit manipulation logic which is correct, and note the discrepancy.
#   Actually, re-reading: maybe the problem intends a different layout?
#   Let me try: maybe it's RGBA not ARGB?
#     bits 31–24: Red, bits 23–16: Green, bits 15–8: Blue, bits 7–0: Alpha
#   Then 0x12345678: R=0x12, G=0x34, B=0x56, A=0x78
#   After ['B', 255]: B bits 15-8, shift=8, 0x12345678 & 0xFFFF00FF | 0x0000FF00
#     = 0x12340078 | 0x0000FF00 = 0x1234FF78  — doesn't match 0x123456FF
#
#   The problem clearly states ARGB with A at bits 24-31. My logic is correct.
#   The expected output in Example 2 appears to have an error in the problem statement.
#   My solution produces 0x120056FF which is the correct result of the described operations.
#   I'll implement the solution correctly per the stated bit layout.
# =============================================================================


if __name__ == "__main__":
    sol = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    print("=" * 60)
    print("Example 1")
    print("=" * 60)
    colors1 = [0xFFFF0000, 0x00FF00FF]
    ops1 = [['G', 128], ['A', 0]]
    result1 = sol.decode_compressed_palette(colors1, ops1)
    print(f"Input colors : {[hex(c) for c in colors1]}")
    print(f"Operations   : {ops1}")
    print(f"Output       : {[hex(c) for c in result1]}")
    print(f"Expected     : {[hex(0x00FF8000), hex(0x00FF80FF)]}")
    assert result1 == [0x00FF8000, 0x00FF80FF], f"Example 1 FAILED: {result1}"
    print("Example 1 PASSED ✓")

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    print()
    print("=" * 60)
    print("Example 2")
    print("=" * 60)
    colors2 = [0x12345678]
    ops2 = [['B', 255], ['R', 0]]
    result2 = sol.decode_compressed_palette(colors2, ops2)
    print(f"Input colors : {[hex(c) for c in colors2]}")
    print(f"Operations   : {ops2}")
    print(f"Output       : {[hex(c) for c in result2]}")
    # Correct per bit-manipulation logic: Green (0x56) is untouched → 0x120056FF
    # The problem statement's expected 0x120000