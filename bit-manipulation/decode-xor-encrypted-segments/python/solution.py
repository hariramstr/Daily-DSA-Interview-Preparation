```python
"""
Title: Decode XOR Encrypted Segments
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
You are given an integer array `encoded` of length `n - 1` and an integer `first`,
where `encoded[i] = arr[i] XOR arr[i + 1]` represents a pairwise XOR encoding of
an original array `arr` of length `n`. However, there is a twist: the array `arr`
is divided into segments of length `k`. Within each segment, consecutive elements
are XOR-encoded as usual, but between segments, a secret key `secret` is XOR-applied
once to the transition element before encoding.

Specifically:
  encoded[k-1]   = arr[k-1]   XOR secret XOR arr[k]
  encoded[2k-1]  = arr[2k-1]  XOR secret XOR arr[2k]
  ...and so on for every segment boundary.

Given `encoded`, `first` (the value of `arr[0]`), `k` (segment length), and `secret`,
reconstruct and return the original array `arr`.

Constraints:
  - 2 <= n <= 10^5
  - n is divisible by k
  - 1 <= k <= n
  - 0 <= encoded[i] <= 10^5
  - 0 <= first <= 10^5
  - 0 <= secret <= 10^5

Example 1:
  Input:  encoded = [1, 2, 7, 3, 4], first = 4, k = 3, secret = 5
  Output: [4, 5, 7, 1, 2]

Example 2:
  Input:  encoded = [3, 1, 0, 2], first = 2, k = 2, secret = 3
  Output: [2, 1, 3, 3]
"""

from typing import List


class Solution:
    def decode_xor_segments(
        self,
        encoded: List[int],
        first: int,
        k: int,
        secret: int
    ) -> List[int]:
        """
        Reconstruct the original array from its XOR-encoded form with segment boundaries.

        The encoding rules are:
          - Within a segment: encoded[i] = arr[i] XOR arr[i+1]
          - At segment boundaries (indices k-1, 2k-1, 3k-1, ...):
            encoded[boundary] = arr[boundary] XOR secret XOR arr[boundary+1]

        To decode:
          - Within a segment: arr[i+1] = arr[i] XOR encoded[i]
          - At a segment boundary: arr[boundary+1] = arr[boundary] XOR secret XOR encoded[boundary]

        Args:
            encoded (List[int]): The encoded array of length n-1.
            first (int): The value of arr[0], the starting element.
            k (int): The segment length.
            secret (int): The secret key applied at segment boundaries.

        Returns:
            List[int]: The reconstructed original array `arr` of length n.

        Time Complexity:  O(n) — We iterate through the encoded array exactly once.
        Space Complexity: O(n) — We store the reconstructed array of length n.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Determine the length of the original array.
        # Since encoded has length n-1, the original array has length n = len(encoded) + 1.
        # -----------------------------------------------------------------------
        n = len(encoded) + 1  # Original array length

        # -----------------------------------------------------------------------
        # STEP 2: Initialize the result array with `first` as the first element.
        # We know arr[0] = first, so we start building from there.
        # -----------------------------------------------------------------------
        arr = [0] * n       # Pre-allocate the result array with zeros
        arr[0] = first      # Set the known starting value

        # -----------------------------------------------------------------------
        # STEP 3: Iterate through each encoded value and reconstruct arr[i+1].
        #
        # For each index i in encoded (0-indexed), we need to figure out if
        # index i is a "segment boundary" or a "normal" position.
        #
        # A segment boundary in encoded occurs at indices: k-1, 2k-1, 3k-1, ...
        # In other words, index i is a boundary if (i + 1) % k == 0.
        # (Because the boundary is the LAST element of a segment, which is at
        #  position k-1, 2k-1, etc. within the encoded array.)
        #
        # Why (i + 1) % k == 0?
        #   - Segment 1 covers arr[0..k-1], so the boundary encoded index is k-1.
        #     (k-1 + 1) % k = k % k = 0  ✓
        #   - Segment 2 covers arr[k..2k-1], so the boundary encoded index is 2k-1.
        #     (2k-1 + 1) % k = 2k % k = 0  ✓
        # -----------------------------------------------------------------------
        for i in range(len(encoded)):
            # ------------------------------------------------------------------
            # Check if the current encoded index `i` is at a segment boundary.
            # A boundary occurs when (i + 1) is a multiple of k.
            # ------------------------------------------------------------------
            if (i + 1) % k == 0:
                # --------------------------------------------------------------
                # BOUNDARY CASE:
                # The encoding formula at a boundary is:
                #   encoded[i] = arr[i] XOR secret XOR arr[i+1]
                #
                # Solving for arr[i+1]:
                #   arr[i+1] = arr[i] XOR secret XOR encoded[i]
                #
                # Why? Because XOR is its own inverse:
                #   If A XOR B XOR C = D, then C = A XOR B XOR D
                # --------------------------------------------------------------
                arr[i + 1] = arr[i] ^ secret ^ encoded[i]
            else:
                # --------------------------------------------------------------
                # NORMAL CASE (within a segment):
                # The encoding formula is:
                #   encoded[i] = arr[i] XOR arr[i+1]
                #
                # Solving for arr[i+1]:
                #   arr[i+1] = arr[i] XOR encoded[i]
                #
                # Why? Because A XOR B = C implies B = A XOR C
                # (XOR is self-inverse: A XOR A = 0, and A XOR 0 = A)
                # --------------------------------------------------------------
                arr[i + 1] = arr[i] ^ encoded[i]

        # -----------------------------------------------------------------------
        # STEP 4: Return the fully reconstructed array.
        # -----------------------------------------------------------------------
        return arr


# =============================================================================
# VERIFICATION / TRACING THROUGH EXAMPLES
# =============================================================================
#
# Example 1:
#   encoded = [1, 2, 7, 3, 4], first = 4, k = 3, secret = 5
#   n = 6, arr = [4, 0, 0, 0, 0, 0]
#
#   i=0: (0+1)%3 = 1 ≠ 0 → normal → arr[1] = arr[0] ^ encoded[0] = 4 ^ 1 = 5
#   i=1: (1+1)%3 = 2 ≠ 0 → normal → arr[2] = arr[1] ^ encoded[1] = 5 ^ 2 = 7
#   i=2: (2+1)%3 = 0     → boundary → arr[3] = arr[2] ^ secret ^ encoded[2] = 7 ^ 5 ^ 7 = 5
#         Wait: 7 ^ 5 = 2, 2 ^ 7 = 5? Let me recheck.
#         7  = 0b0111
#         5  = 0b0101
#         7^5= 0b0010 = 2
#         2^7= 0b0101 = 5? No: 0b0010 ^ 0b0111 = 0b0101 = 5. Hmm but expected arr[3]=1.
#
#   Wait, let me re-read the problem. encoded[k-1] = arr[k-1] XOR secret XOR arr[k].
#   k=3, so encoded[2] = arr[2] XOR secret XOR arr[3].
#   encoded[2] = 7, arr[2] = 7, secret = 5.
#   arr[3] = arr[2] ^ secret ^ encoded[2] = 7 ^ 5 ^ 7 = 5.
#   But expected output is [4, 5, 7, 1, 2].
#
#   Hmm, arr[3] should be 1 but we get 5. Let me re-examine the example explanation:
#   "Boundary: arr[3]=7 XOR 5 XOR 7=1"
#   7 XOR 5 XOR 7: 7^5=2, 2^7=5. That's 5, not 1. The example explanation seems wrong.
#
#   Let me verify from the output: arr = [4, 5, 7, 1, 2]
#   encoded[0] = arr[0]^arr[1] = 4^5 = 1 ✓
#   encoded[1] = arr[1]^arr[2] = 5^7 = 2 ✓
#   encoded[2] = arr[2]^secret^arr[3] = 7^5^1 = 3. But encoded[2]=7. ✗
#
#   Something is off with the example. Let me try without secret at boundary:
#   encoded[2] = arr[2]^arr[3] = 7^1 = 6 ≠ 7.
#
#   Let me try: maybe secret is XOR'd differently. Perhaps encoded[i] = arr[i] XOR arr[i+1] XOR secret
#   at boundaries, meaning arr[i+1] = arr[i] ^ encoded[i] ^ secret.
#   arr[3] = 7 ^ 7 ^ 5 = 5. Still 5.
#
#   Let me just try to find what encoded would produce [4,5,7,1,2] with k=3, secret=5:
#   encoded[0]=4^5=1, encoded[1]=5^7=2, encoded[2]=7^1=6 (normal) or 7^5^1=3 (with secret).
#   Neither gives encoded[2]=7.
#
#   The example seems inconsistent. Let me try to work backwards from the given
#   encoded=[1,2,7,3,4] and output=[4,5,7,1,2]:
#   encoded[2] should be 7. arr[2]=7, arr[3]=1.
#   7 = 7 ^ ? ^ 1 → ? = 7^7^1 = 1. So secret would be 1, not 5.
#   OR: 7 = 7 ^ 1 (no secret) → 7^1=6 ≠ 7.
#
#   Alternatively maybe the boundary formula is: encoded[i] = arr[i] XOR arr[i+1] (no secret)
#   and secret is applied differently. Let me try: arr[3] = arr[2] ^ encoded[2] = 7^7=0. No.
#
#   I think the examples in the problem description may have errors. Let me implement
#   the algorithm as described (boundary: arr[i+1] = arr[i] ^ secret ^ encoded[i])
#   and verify Example 2 instead.
#
# Example 2:
#   encoded = [3, 1, 0, 2], first = 2, k = 2, secret = 3
#   Expected: [2, 1, 3, 3]
#
#   n=5, arr=[2,0,0,0,0]
#   i=0: (0+1)%2=1≠0 → normal → arr[1]=2^3=1 ✓
#   i=1: (1+1)%2=0 → boundary → arr[2]=arr[1]^secret^encoded[1]=1^3^1=3 ✓
#   i=2: (2+1)%2=1≠0 → normal → arr[3]=arr[2]^encoded[2]=3^0=3 ✓
#   i=3: (3+1)%2=0 → boundary → arr[4]=arr[3]^secret^encoded[3]=3^3^2=2
#   Result: [2,1,3,3,2] but n=5 and encoded has 4 elements so n=5.
#   Expected is [2,1,3,3] which has length 4, but encoded has length 4 so n=5.
#   The expected output length seems wrong in the problem too.
#
#   Actually wait: encoded has length n-1. If encoded=[3,1,0,2] has length 4, then n=5.
#   But expected output [2,1,3,3] has length 4. That's inconsistent.
#
#   The problem examples seem to have errors. I'll implement the algorithm correctly
#   based on the mathematical description and verify Example 2 partially.
#   [2, 1, 3, 3] matches arr[0..3] of our result [2,1,3,3,2].
#
# =============================================================================


if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------------
    # Test Case 1 (from problem description)
    # Note: The problem's example explanation appears to have arithmetic errors,
    # but we implement the algorithm as mathematically described.
    # -------------------------------------------------------------------------
    print("=" * 60)
    print("Test Case 1:")
    encoded1 = [1, 2, 7, 3, 4]
    first1 = 4
    k1 = 3
    secret1 = 5
    result1 = solution.decode_xor_segments(encoded1, first1, k1, secret1)
    print(f"  encoded = {encoded1}")
    print(f"  first   = {first1}")
    print(f"  k       = {k1}")
    print(f"  secret  = {secret1}")
    print(f"  Result  = {result1}")
    # Verify the encoding of result1 matches encoded1
    print("  Verification (re-encoding result):")
    for i in range(len(encoded1)):
        if (i + 1) % k1 == 0:
            reenc = result1[i] ^ secret1 ^ result1[i + 1]
            print(f"    encoded[{i}] (boundary): {result1[i]} ^ {secret1} ^ {result1[i+1]} = {reenc} (expected {encoded1[i]})")
        else:
            reenc = result1[i] ^ result1[i + 1]
            print(f"    encoded[{i}] (normal):   {result1[i]} ^ {result1[i+1]} = {reenc} (expected {encoded1[i]})")

    # -------------------------------------------------------------------------
    # Test Case 2 (from problem description)
    # -------------------------------------------------------------------------
    print()
    print("=" * 60)
    print("Test Case 2:")
    encoded2 = [3, 1, 0, 2]
    first2 = 2
    k2 = 2
    secret2 = 3
    result2 = solution.decode_xor_segments(encoded2, first2, k2, secret2)
    print(f"  encoded = {encoded2}")
    print(f"  first   = {first2}")
    print(f"  k       = {k2}")
    print(f"  secret  = {secret2}")
    print(f"  Result  = {result2}")
    print("  Verification (re-encoding result):")
    for i in range(len(encoded2)):
        if (i + 1) % k2 == 0:
            reenc = result2[i