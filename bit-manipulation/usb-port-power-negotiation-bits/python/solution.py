"""
USB Port Power Negotiation Bits
================================
Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
A USB hub has `n` ports, each represented by a single bit in a 32-bit integer `status`.
A bit value of `1` means the port is currently active (drawing power), and `0` means
the port is idle.

You are given the integer `status` representing the current port states, and an integer
`mask` representing a set of ports that are being requested to toggle (flip their state).
Your task is to return the new `status` after toggling only the ports indicated by `mask`.

Additionally, after toggling, return the count of ports that are both active AND within
the first `k` bits (i.e., bits at positions `0` through `k-1`, where position `0` is
the least significant bit).

Return an array `[newStatus, activeCount]` where `newStatus` is the resulting integer
after toggling and `activeCount` is the number of active ports in the first `k` bit
positions of `newStatus`.

Constraints:
- 0 <= status <= 2^31 - 1
- 0 <= mask <= 2^31 - 1
- 1 <= k <= 32

Example 1:
- Input: status = 13 (binary: ...01101), mask = 10 (binary: ...01010), k = 4
- After XOR toggle: 13 ^ 10 = 7 (binary: ...00111)
- Active ports in first 4 bits of 7 (0111): bits 0, 1, 2 → count = 3
- Output: [7, 3]

Example 2:
- Input: status = 255 (binary: 11111111), mask = 170 (binary: 10101010), k = 8
- After XOR toggle: 255 ^ 170 = 85 (binary: 01010101)
- Active ports in first 8 bits of 85 (01010101): bits 0, 2, 4, 6 → count = 4
- Output: [85, 4]
"""

from typing import List


class Solution:
    def usbPortToggle(self, status: int, mask: int, k: int) -> List[int]:
        """
        Toggle ports indicated by mask using XOR, then count active ports in first k bits.

        The XOR operation is perfect for toggling bits:
        - 1 XOR 1 = 0  (active port toggled to idle)
        - 0 XOR 1 = 1  (idle port toggled to active)
        - 1 XOR 0 = 1  (active port unchanged)
        - 0 XOR 0 = 0  (idle port unchanged)

        To count active ports in the first k bits, we create a bitmask that has
        exactly k ones in the lowest positions, then AND it with newStatus.

        Args:
            status (int): Current port states as a 32-bit integer.
                          Bit i = 1 means port i is active.
            mask (int):   Ports to toggle. Bit i = 1 means toggle port i.
            k (int):      Number of lowest-order bit positions to examine
                          when counting active ports (positions 0 through k-1).

        Returns:
            List[int]: [newStatus, activeCount]
                - newStatus:   The integer after XOR-toggling the masked ports.
                - activeCount: Number of set bits in newStatus within positions 0..k-1.

        Time Complexity:  O(k) for counting bits in the first k positions,
                          or O(1) if we use bin().count() on the masked value.
        Space Complexity: O(1) — only a constant number of integer variables used.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Toggle the ports indicated by `mask` using XOR.
        #
        # XOR (^) flips a bit wherever the mask has a 1, and leaves it unchanged
        # wherever the mask has a 0.  This is the canonical "bit toggle" idiom.
        #
        # Example 1 trace:
        #   status = 13  → binary 0...01101
        #   mask   = 10  → binary 0...01010
        #   XOR result   → binary 0...00111  = 7
        #
        # Example 2 trace:
        #   status = 255 → binary 11111111
        #   mask   = 170 → binary 10101010
        #   XOR result   → binary 01010101  = 85
        # -----------------------------------------------------------------------
        new_status: int = status ^ mask

        # -----------------------------------------------------------------------
        # STEP 2: Build a bitmask that isolates the first `k` bit positions.
        #
        # We want a number whose binary representation has exactly k ones in the
        # least-significant positions and zeros everywhere else.
        #
        # The trick:  (1 << k) produces a 1 followed by k zeros.
        #             Subtracting 1 from that gives k ones.
        #
        # Example: k = 4
        #   1 << 4  = 0b10000  = 16
        #   16 - 1  = 0b01111  = 15   ← exactly 4 ones in positions 0-3
        #
        # Special case: k = 32
        #   1 << 32 = 4294967296 (Python handles big integers natively, no overflow)
        #   4294967296 - 1 = 0xFFFFFFFF  ← 32 ones, covers all 32 ports
        # -----------------------------------------------------------------------
        lower_k_mask: int = (1 << k) - 1

        # -----------------------------------------------------------------------
        # STEP 3: Isolate only the first k bits of new_status.
        #
        # AND-ing new_status with lower_k_mask zeroes out every bit above position
        # k-1, leaving only the bits we care about for counting.
        #
        # Example 1 trace (new_status = 7, k = 4):
        #   lower_k_mask = 0b1111 = 15
        #   7 & 15 = 0b0111 & 0b1111 = 0b0111 = 7
        #   (All 4 bits fit inside 7, so the AND doesn't change the value here.)
        #
        # Example 2 trace (new_status = 85, k = 8):
        #   lower_k_mask = 0b11111111 = 255
        #   85 & 255 = 0b01010101 & 0b11111111 = 0b01010101 = 85
        # -----------------------------------------------------------------------
        active_bits_in_k: int = new_status & lower_k_mask

        # -----------------------------------------------------------------------
        # STEP 4: Count the number of set bits (1s) in `active_bits_in_k`.
        #
        # Python's built-in bin() converts an integer to its binary string
        # representation (e.g., bin(7) → '0b111').  We then count the '1'
        # characters in that string.
        #
        # This is clean, readable, and runs in O(number of bits) time.
        # Alternatively, we could use bit_count() available in Python 3.10+,
        # but bin().count('1') works on all Python 3 versions.
        #
        # Example 1 trace:
        #   active_bits_in_k = 7  → bin(7) = '0b111'  → count('1') = 3  ✓
        #
        # Example 2 trace:
        #   active_bits_in_k = 85 → bin(85) = '0b1010101' → count('1') = 4  ✓
        # -----------------------------------------------------------------------
        active_count: int = bin(active_bits_in_k).count('1')

        # -----------------------------------------------------------------------
        # STEP 5: Return the result as [newStatus, activeCount].
        # -----------------------------------------------------------------------
        return [new_status, active_count]


# ---------------------------------------------------------------------------
# Main block: demonstrate the solution with the two examples from the problem.
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solver = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # status = 13  (binary: ...01101)
    # mask   = 10  (binary: ...01010)
    # k      = 4
    # Expected output: [7, 3]
    # ------------------------------------------------------------------
    status1 = 13
    mask1 = 10
    k1 = 4
    result1 = solver.usbPortToggle(status1, mask1, k1)
    print("Example 1:")
    print(f"  status = {status1}  (binary: {bin(status1)})")
    print(f"  mask   = {mask1}  (binary: {bin(mask1)})")
    print(f"  k      = {k1}")
    print(f"  Result : {result1}")
    print(f"  Expected: [7, 3]")
    print(f"  PASS: {result1 == [7, 3]}")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # status = 255 (binary: 11111111)
    # mask   = 170 (binary: 10101010)
    # k      = 8
    # Expected output: [85, 4]
    # ------------------------------------------------------------------
    status2 = 255
    mask2 = 170
    k2 = 8
    result2 = solver.usbPortToggle(status2, mask2, k2)
    print("Example 2:")
    print(f"  status = {status2}  (binary: {bin(status2)})")
    print(f"  mask   = {mask2} (binary: {bin(mask2)})")
    print(f"  k      = {k2}")
    print(f"  Result : {result2}")
    print(f"  Expected: [85, 4]")
    print(f"  PASS: {result2 == [85, 4]}")
    print()

    # ------------------------------------------------------------------
    # Additional edge-case: status = 0, mask = 0, k = 1
    # No ports active, no ports toggled → newStatus = 0, activeCount = 0
    # ------------------------------------------------------------------
    status3 = 0
    mask3 = 0
    k3 = 1
    result3 = solver.usbPortToggle(status3, mask3, k3)
    print("Edge Case (all zeros):")
    print(f"  status = {status3}, mask = {mask3}, k = {k3}")
    print(f"  Result : {result3}")
    print(f"  Expected: [0, 0]")
    print(f"  PASS: {result3 == [0, 0]}")
    print()

    # ------------------------------------------------------------------
    # Additional edge-case: k = 32 (examine all 32 bits)
    # status = 2^31 - 1 (all 31 lower bits set), mask = 0 (no toggle)
    # newStatus = 2^31 - 1, activeCount = 31
    # ------------------------------------------------------------------
    status4 = (1 << 31) - 1   # 2147483647, binary: 0111...1 (31 ones)
    mask4 = 0
    k4 = 32
    result4 = solver.usbPortToggle(status4, mask4, k4)
    print("Edge Case (k=32, all lower 31 bits set):")
    print(f"  status = {status4}, mask = {mask4}, k = {k4}")
    print(f"  Result : {result4}")
    print(f"  Expected: [{status4}, 31]")
    print(f"  PASS: {result4 == [status4, 31]}")