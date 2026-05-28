"""
Permission Flags Merger
=======================
Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
In a software system, user permissions are represented as bitmasks stored in an integer.
Each bit position corresponds to a specific permission:
  bit 0 = READ, bit 1 = WRITE, bit 2 = EXECUTE, bit 3 = DELETE, and so on up to bit 29.

You are given two arrays, `granted` and `revoked`, each of length `n`.
The `granted[i]` value represents a set of permissions being added to a user,
and `revoked[i]` represents a set of permissions being removed from the same user at step `i`.

Starting from an initial permission mask of `0`, process all `n` steps in order:
  1. First apply the grant:      mask = mask | granted[i]
  2. Then apply the revocation:  mask = mask & (~revoked[i])

Return the final permission mask after all steps have been processed.

Constraints:
  - 1 <= n <= 10^4
  - 0 <= granted[i], revoked[i] <= 2^30 - 1
  - granted[i] & revoked[i] == 0 for all i (no shared set bits)

Example 1:
  granted = [5, 2], revoked = [0, 1]
  Step 0: mask = 0 | 5 = 5 (binary: 101), then mask = 5 & ~0 = 5
  Step 1: mask = 5 | 2 = 7 (binary: 111), then mask = 7 & ~1 = 6 (binary: 110)
  Output: 6

Example 2:
  granted = [15, 8, 4], revoked = [0, 3, 8]
  Step 0: mask = 0 | 15 = 15, then mask = 15 & ~0 = 15
  Step 1: mask = 15 | 8 = 15, then mask = 15 & ~3 = 12
  Step 2: mask = 12 | 4 = 12, then mask = 12 & ~8 = 4
  Output: 4
"""

from typing import List


class Solution:
    def mergePermissionFlags(self, granted: List[int], revoked: List[int]) -> int:
        """
        Process a series of permission grant and revoke operations on a bitmask.

        Starting from mask = 0, for each step i:
          - OR the mask with granted[i]  → turns ON the specified permission bits
          - AND the mask with ~revoked[i] → turns OFF the specified permission bits

        Args:
            granted (List[int]): List of permission bitmasks to add at each step.
            revoked (List[int]): List of permission bitmasks to remove at each step.

        Returns:
            int: The final permission mask after all steps are applied.

        Time Complexity:  O(n) — we iterate through all n steps exactly once.
        Space Complexity: O(1) — we only use a single integer (mask) as extra storage.
        """

        # -----------------------------------------------------------------------
        # INITIALIZATION
        # -----------------------------------------------------------------------
        # Start with no permissions at all.
        # A mask of 0 means every bit is 0 → no READ, no WRITE, no EXECUTE, etc.
        mask: int = 0

        # -----------------------------------------------------------------------
        # DETERMINE THE NUMBER OF STEPS
        # -----------------------------------------------------------------------
        # Both `granted` and `revoked` have the same length n.
        # We use len(granted) to drive our loop.
        n: int = len(granted)

        # -----------------------------------------------------------------------
        # MAIN LOOP — process each step in order
        # -----------------------------------------------------------------------
        for i in range(n):
            # -------------------------------------------------------------------
            # STEP 1: GRANT PERMISSIONS  (bitwise OR)
            # -------------------------------------------------------------------
            # The OR operation sets bits to 1 wherever granted[i] has a 1.
            # Bits that are already 1 in mask stay 1.
            # Bits that are 0 in both mask and granted[i] stay 0.
            #
            # Example (step 0 of Example 1):
            #   mask     = 000  (0 in decimal)
            #   granted  = 101  (5 in decimal)
            #   result   = 101  (5 in decimal)  ← READ and EXECUTE are now ON
            #
            # Why OR?  OR is the standard "add flags" operation in bitmask logic.
            # It never accidentally clears a bit that was already set.
            mask = mask | granted[i]

            # -------------------------------------------------------------------
            # STEP 2: REVOKE PERMISSIONS  (bitwise AND with bitwise NOT)
            # -------------------------------------------------------------------
            # ~revoked[i] flips every bit in revoked[i]:
            #   - Bits that were 1 (permissions to remove) become 0.
            #   - Bits that were 0 (permissions to keep)   become 1.
            #
            # ANDing mask with this "inverted revoke mask" clears exactly the
            # bits we want to remove, while leaving all other bits unchanged.
            #
            # Example (step 1 of Example 1):
            #   mask         = 111  (7 in decimal)
            #   revoked[1]   = 001  (1 in decimal)
            #   ~revoked[1]  = ...11111110  (all 1s except bit 0)
            #   mask & ~rev  = 110  (6 in decimal)  ← READ (bit 0) is now OFF
            #
            # Why AND with NOT?  This is the standard "clear flags" idiom.
            # It surgically turns off only the specified bits without touching
            # any other bits in the mask.
            #
            # Note on Python integers:
            #   Python integers have arbitrary precision, so ~revoked[i] produces
            #   a negative number (two's complement interpretation).  However,
            #   because mask is always non-negative and we only ever set bits
            #   within the 0–29 range, the AND operation still works correctly:
            #   the extra high bits introduced by ~ are all 1s, so they don't
            #   affect the lower 30 bits of mask.
            mask = mask & (~revoked[i])

        # -----------------------------------------------------------------------
        # RETURN THE FINAL MASK
        # -----------------------------------------------------------------------
        # After processing all n steps, `mask` holds the accumulated permission
        # state.  Return it directly.
        return mask


# ---------------------------------------------------------------------------
# MAIN — demonstrate the solution with the provided examples
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # -----------------------------------------------------------------------
    # granted = [5, 2], revoked = [0, 1]
    # Expected output: 6
    #
    # Trace:
    #   Initial mask = 0
    #   Step 0: mask = 0 | 5 = 5  →  mask = 5 & ~0 = 5   (binary: 101)
    #   Step 1: mask = 5 | 2 = 7  →  mask = 7 & ~1 = 6   (binary: 110)
    # -----------------------------------------------------------------------
    granted1 = [5, 2]
    revoked1 = [0, 1]
    result1 = solution.mergePermissionFlags(granted1, revoked1)
    print("Example 1")
    print(f"  granted = {granted1}")
    print(f"  revoked = {revoked1}")
    print(f"  Final mask (decimal) : {result1}")
    print(f"  Final mask (binary)  : {bin(result1)}")
    print(f"  Expected             : 6")
    print(f"  Correct?             : {result1 == 6}")
    print()

    # -----------------------------------------------------------------------
    # Example 2
    # -----------------------------------------------------------------------
    # granted = [15, 8, 4], revoked = [0, 3, 8]
    # Expected output: 4
    #
    # Trace:
    #   Initial mask = 0
    #   Step 0: mask = 0  | 15 = 15  →  mask = 15 & ~0  = 15  (binary: 1111)
    #   Step 1: mask = 15 |  8 = 15  →  mask = 15 & ~3  = 12  (binary: 1100)
    #   Step 2: mask = 12 |  4 = 12  →  mask = 12 & ~8  =  4  (binary: 0100)
    # -----------------------------------------------------------------------
    granted2 = [15, 8, 4]
    revoked2 = [0, 3, 8]
    result2 = solution.mergePermissionFlags(granted2, revoked2)
    print("Example 2")
    print(f"  granted = {granted2}")
    print(f"  revoked = {revoked2}")
    print(f"  Final mask (decimal) : {result2}")
    print(f"  Final mask (binary)  : {bin(result2)}")
    print(f"  Expected             : 4")
    print(f"  Correct?             : {result2 == 4}")
    print()

    # -----------------------------------------------------------------------
    # Extra edge-case: single step, grant everything, revoke nothing
    # -----------------------------------------------------------------------
    granted3 = [0b111111111111111111111111111111]  # all 30 bits set
    revoked3 = [0]
    result3 = solution.mergePermissionFlags(granted3, revoked3)
    print("Edge Case — grant all 30 bits, revoke nothing")
    print(f"  granted = {granted3}")
    print(f"  revoked = {revoked3}")
    print(f"  Final mask (decimal) : {result3}")
    print(f"  Final mask (binary)  : {bin(result3)}")
    expected3 = (1 << 30) - 1
    print(f"  Expected             : {expected3}")
    print(f"  Correct?             : {result3 == expected3}")