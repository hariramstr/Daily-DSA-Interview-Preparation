"""
Title: Minimum Cost to Compress a Melody with Repeated Motifs

Problem Description:
A digital music editor stores a melody as an array of integers, where each integer
represents a note pitch. To reduce storage, the editor may encode the melody as a
sequence of blocks. A block can be stored in one of two ways:

1. Raw block: store every note directly. A raw block covering notes i..j costs
   (j - i + 1).

2. Motif block: if the subarray notes[i..j] is made of one smaller pattern repeated
   consecutively one or more times, it may be stored as:
   cost(pattern) + repeatPenalty,
   where repeatPenalty is a fixed integer P, and cost(pattern) is the minimum
   compressed cost of that smaller pattern.

You may recursively compress the pattern itself, and you may partition the melody
into any number of blocks. Your task is to compute the minimum total cost to encode
the entire melody.

Formally, for any subarray notes[i..j], you may either keep it raw, split it into
two non-empty consecutive parts, or encode it as repeated copies of a shorter
subarray whose length divides (j - i + 1). A repeated motif block is valid only if
every copy is exactly identical.

Return the minimum encoding cost for the full array.

Constraints:
- 1 <= n <= 200
- 1 <= notes[i] <= 10^9
- 1 <= P <= 200
- Time complexity better than O(n^4 * n) is expected for a full solution
"""

from typing import List


class Solution:
    def _build_lcp(self, notes: List[int]) -> List[List[int]]:
        """
        Build an LCP (Longest Common Prefix) table for all suffix pairs.

        lcp[i][j] = length of the longest equal prefix of notes[i:] and notes[j:].

        This table lets us compare two subarrays of the same length in O(1):
        notes[a:a+length] == notes[b:b+length] iff lcp[a][b] >= length.

        Args:
            notes: The melody as a list of integers.

        Returns:
            A 2D list containing LCP values.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        n: int = len(notes)

        # We allocate one extra row and column so that lcp[i + 1][j + 1]
        # is always safe to access when filling from bottom-right to top-left.
        lcp: List[List[int]] = [[0] * (n + 1) for _ in range(n + 1)]

        # Fill in reverse order so that when we compute lcp[i][j],
        # the value lcp[i + 1][j + 1] is already known.
        for i in range(n - 1, -1, -1):
            for j in range(n - 1, -1, -1):
                if notes[i] == notes[j]:
                    lcp[i][j] = 1 + lcp[i + 1][j + 1]
                else:
                    lcp[i][j] = 0

        return lcp

    def _compute_is_repeat(self, notes: List[int], lcp: List[List[int]]) -> List[List[bool]]:
        """
        Precompute whether every subarray can be represented as repeated copies
        of a shorter pattern.

        is_repeat[start][length] is True if notes[start:start+length] consists of
        k >= 2 consecutive copies of some shorter block whose size divides length.

        We test all divisors d of length where d < length, and verify all adjacent
        blocks of size d are equal using the LCP table.

        Args:
            notes: The melody as a list of integers.
            lcp: Precomputed LCP table.

        Returns:
            A 2D boolean table indexed by [start][length].

        Time complexity:
            O(n^3)

        Space complexity:
            O(n^2)
        """
        n: int = len(notes)

        # We index by exact subarray length directly, so second dimension is n + 1.
        is_repeat: List[List[bool]] = [[False] * (n + 1) for _ in range(n)]

        # Consider every possible subarray length.
        for length in range(2, n + 1):
            # Try every possible starting position for this length.
            for start in range(0, n - length + 1):
                # We need to know whether this subarray can be split into
                # repeated copies of a smaller pattern.
                #
                # If pattern length is d, then:
                # - d must divide length
                # - d must be strictly smaller than length
                # - every block of size d must equal the first block
                #
                # We iterate over all divisors d of length.
                found_repeat: bool = False
                for d in range(1, length):
                    if length % d != 0:
                        continue

                    # Number of blocks if we use pattern size d.
                    blocks: int = length // d
                    if blocks < 2:
                        continue

                    # Check all adjacent blocks:
                    # block at start + t*d must equal block at start + (t+1)*d
                    #
                    # Using LCP, equality of two length-d blocks is:
                    # lcp[pos1][pos2] >= d
                    valid: bool = True
                    for t in range(blocks - 1):
                        left: int = start + t * d
                        right: int = start + (t + 1) * d
                        if lcp[left][right] < d:
                            valid = False
                            break

                    if valid:
                        found_repeat = True
                        break

                is_repeat[start][length] = found_repeat

        return is_repeat

    def minimum_compression_cost(self, notes: List[int], p: int) -> int:
        """
        Compute the minimum encoding cost for the full melody.

        We use interval dynamic programming:
        - dp[i][j] = minimum cost to encode notes[i..j]
        Transitions:
        1. Raw storage: length of the interval
        2. Split into two parts: dp[i][k] + dp[k+1][j]
        3. Repeated motif:
           if notes[i..j] is repeated copies of a shorter pattern of length d,
           then cost can be dp[i][i+d-1] + p

        To support fast repeated-pattern checks, we precompute:
        - lcp table for O(1) subarray equality checks
        - repeat validity information

        Args:
            notes: The melody as a list of integers.
            p: Fixed repeat penalty.

        Returns:
            Minimum encoding cost for the entire melody.

        Time complexity:
            O(n^3)

        Space complexity:
            O(n^2)
        """
        n: int = len(notes)

        # Edge case: a single note must be stored raw.
        if n == 1:
            return 1

        # ------------------------------------------------------------
        # Step 1: Precompute LCP table.
        #
        # Why?
        # We need to compare many subarrays while checking whether an interval
        # is made of repeated copies of a smaller pattern.
        #
        # Without preprocessing, each comparison could cost O(length),
        # making the full solution too slow.
        #
        # With LCP, equality of two same-length blocks becomes O(1).
        # ------------------------------------------------------------
        lcp: List[List[int]] = self._build_lcp(notes)

        # ------------------------------------------------------------
        # Step 2: Precompute which intervals are repeated structures.
        #
        # is_repeat[start][length] tells us whether the interval can be encoded
        # as repeated copies of some shorter pattern.
        #
        # This is not strictly enough by itself to know the best motif transition,
        # because we still need to know WHICH divisor lengths work.
        # So later, during DP, we will test divisors again using LCP.
        #
        # Still, this table is useful conceptually and keeps the structure clear.
        # ------------------------------------------------------------
        is_repeat: List[List[bool]] = self._compute_is_repeat(notes, lcp)

        # ------------------------------------------------------------
        # Step 3: Interval DP table.
        #
        # dp[i][j] = minimum cost to encode notes[i..j]
        #
        # Base idea:
        # - Worst case: store everything raw, cost = interval length
        # - Improve by splitting
        # - Improve by motif compression if interval is repeated
        # ------------------------------------------------------------
        dp: List[List[int]] = [[0] * n for _ in range(n)]

        # Length 1 intervals: cost is 1.
        for i in range(n):
            dp[i][i] = 1

        # ------------------------------------------------------------
        # Step 4: Fill DP by increasing interval length.
        #
        # This order is essential because:
        # - split transitions need smaller intervals
        # - motif transitions need the cost of the smaller pattern interval
        #
        # Since smaller intervals are computed first, all dependencies are ready.
        # ------------------------------------------------------------
        for length in range(2, n + 1):
            for start in range(0, n - length + 1):
                end: int = start + length - 1

                # ----------------------------------------------------
                # Option A: store the whole interval raw.
                #
                # This is always valid and gives us a safe initial answer.
                # ----------------------------------------------------
                best: int = length

                # ----------------------------------------------------
                # Option B: split the interval into two consecutive parts.
                #
                # We try every possible split point:
                # notes[start..mid] + notes[mid+1..end]
                #
                # This is the standard interval DP partition transition.
                # ----------------------------------------------------
                for mid in range(start, end):
                    candidate: int = dp[start][mid] + dp[mid + 1][end]
                    if candidate < best:
                        best = candidate

                # ----------------------------------------------------
                # Option C: encode as repeated motif.
                #
                # We only proceed if this interval is known to be some repetition.
                # Then we test each divisor d of 'length' and verify whether
                # the interval is made of repeated copies of the first d notes.
                #
                # If yes, the cost is:
                #   dp[start][start + d - 1] + p
                #
                # Important:
                # We use dp for the pattern itself, not just raw length d,
                # because the pattern may also be compressible recursively.
                # ----------------------------------------------------
                if is_repeat[start][length]:
                    for d in range(1, length):
                        if length % d != 0:
                            continue

                        blocks: int = length // d
                        if blocks < 2:
                            continue

                        valid: bool = True
                        for t in range(blocks - 1):
                            left: int = start + t * d
                            right: int = start + (t + 1) * d
                            if lcp[left][right] < d:
                                valid = False
                                break

                        if valid:
                            motif_cost: int = dp[start][start + d - 1] + p
                            if motif_cost < best:
                                best = motif_cost

                dp[start][end] = best

        return dp[0][n - 1]


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # notes = [4, 7, 4, 7, 4, 7], P = 2
    # Whole array = 3 copies of [4, 7]
    # Cost([4, 7]) = 2 raw
    # Repeated motif cost = 2 + 2 = 4
    notes1: List[int] = [4, 7, 4, 7, 4, 7]
    p1: int = 2
    result1: int = solution.minimum_compression_cost(notes1, p1)
    print(result1)  # Expected: 4

    # Example 2:
    # notes = [5, 5, 5, 8, 5, 5, 5, 8], P = 3
    # Whole array = 2 copies of [5, 5, 5, 8]
    # Cost([5, 5, 5, 8]) = 4 raw
    # Repeated motif cost = 4 + 3 = 7
    notes2: List[int] = [5, 5, 5, 8, 5, 5, 5, 8]
    p2: int = 3
    result2: int = solution.minimum_compression_cost(notes2, p2)
    print(result2)  # Expected: 7