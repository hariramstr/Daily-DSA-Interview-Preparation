"""
Title: Maximum Secure Relay Chain by XOR Signature

Problem Description:
You are given an array signatures of length n, where signatures[i] is a non-negative
32-bit integer representing the security signature of the i-th relay node in a
communication line. You want to choose a subsequence of nodes in increasing index
order to form a secure relay chain.

A chain is considered valid if for every pair of consecutive chosen nodes i < j in
the chain, the value signatures[i] XOR signatures[j] is strictly greater than every
XOR value used earlier in that same chain. In other words, if the chosen indices are
p1 < p2 < ... < pk, then the sequence

(signatures[p1] XOR signatures[p2]),
(signatures[p2] XOR signatures[p3]),
...,
(signatures[p{k-1}] XOR signatures[pk])

must be strictly increasing.

Return the maximum possible length of a valid secure relay chain.

A chain of length 1 is always valid.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= signatures[i] < 2^30
- The expected solution should be significantly faster than O(n^2)

Key idea used in this solution:
For a fixed current value x and a previous edge XOR threshold t, we need to know
whether there exists an earlier node value y such that (y XOR x) > t. Instead of
tracking all possible thresholds directly, we exploit a bit-based DP compression.

Define:
dp_end[i][b] = maximum chain length of a subsequence ending at index i such that
the last XOR used in that chain is strictly less than 2^b.

Then, when we want to append signatures[i] after some earlier endpoint j, the new
edge value w = signatures[j] XOR signatures[i]. If we already have a chain ending at
j whose last XOR is < w, then we may extend it by one.

Now observe:
For any integer w > 0, if msb(w) = h (highest set bit index), then:
2^h <= w < 2^(h+1)

Therefore, "last XOR < w" is guaranteed if we only know "last XOR < 2^h".
This gives a safe and exact transition:
new_length = dp_end[j][h] + 1, where h = msb(signatures[j] XOR signatures[i])

The challenge is still to avoid checking all j.

We process values through bitwise tries. For each bit level b, we maintain a trie-like
aggregation that can answer:
among all previous values y, what is the maximum dp_end_of_that_value[b] for values y
such that msb(y XOR x) = b ?

This condition means:
- y and x share all bits above b
- they differ at bit b

That can be answered by grouping previous values by their prefix above bit b and by
their bit b value. For each b and each prefix, we store the best chain length for
numbers having bit b = 0 and bit b = 1. Then for current x, the best predecessor with
msb XOR exactly b is found by taking the opposite bit group under the same higher-bit
prefix.

This yields O(n * B) time with B = 30, which is easily fast enough.
"""

from typing import Dict, List, Tuple


class Solution:
    def maximumSecureRelayChain(self, signatures: List[int]) -> int:
        """
        Compute the maximum length of a valid secure relay chain.

        We use dynamic programming compressed by powers of two and bit-prefix grouping.

        Definitions:
        - Let dp_less[b] for a chain ending at current index mean:
          maximum chain length whose last XOR edge is strictly less than 2^b.
        - A chain of length 1 has no previous XOR edge, so it is valid for every b.

        Transition:
        - Suppose we want to append current value x after a previous endpoint with value y.
        - Let w = x XOR y and h = msb(w).
        - Since 2^h <= w, any chain ending at y whose last XOR < 2^h can be extended.
        - So candidate length is previous_dp_less_at_y[h] + 1.

        To avoid O(n^2), for each bit h we group previous values by:
        - prefix above bit h: value >> (h + 1)
        - bit h itself: 0 or 1

        Then msb(x XOR y) = h exactly when:
        - prefixes above h are equal
        - bit h differs

        So for current x, for each h we can query the best previous chain length in:
        best[h][prefix][opposite_bit]

        Args:
            signatures: List of non-negative integers.

        Returns:
            Maximum valid chain length.

        Time complexity:
            O(n * B), where B = 30.

        Space complexity:
            O(B * n) in the worst case for the prefix maps.
        """
        # We only need 30 bits because signatures[i] < 2^30.
        max_bits: int = 30

        # For each bit position b, we maintain a dictionary:
        #   key   = prefix of the number above bit b, i.e. value >> (b + 1)
        #   value = [best_for_bit_0, best_for_bit_1]
        #
        # Interpretation:
        # For all previously processed numbers y that share the same higher-bit prefix,
        # we store the maximum dp_less[b] among those with bit b = 0 and bit b = 1.
        #
        # Then for current x:
        # - prefix = x >> (b + 1)
        # - bit = (x >> b) & 1
        # - any previous y with same prefix and opposite bit has msb(x XOR y) = b
        # - therefore candidate chain length is stored in the opposite bucket
        grouped_best: List[Dict[int, List[int]]] = [dict() for _ in range(max_bits)]

        # Global answer. A single node is always a valid chain.
        answer: int = 1

        # Process nodes from left to right to respect subsequence order.
        for x in signatures:
            # dp_less[b] for the current endpoint x:
            # maximum chain length ending at x whose last XOR is < 2^b.
            #
            # Important base case:
            # A chain of length 1 has no last XOR edge, so it vacuously satisfies
            # "last XOR < 2^b" for every b. Therefore initialize all entries to 1.
            dp_less: List[int] = [1] * max_bits

            # For each possible highest differing bit b, try to connect x to some
            # earlier endpoint y such that msb(x XOR y) = b.
            #
            # If that is possible, then the new edge value w satisfies:
            #   2^b <= w < 2^(b+1)
            #
            # Any chain ending at y with previous last XOR < 2^b can be extended.
            for b in range(max_bits):
                prefix: int = x >> (b + 1)
                bit: int = (x >> b) & 1

                # Look up previous values with the same higher-bit prefix.
                bucket: List[int] | None = grouped_best[b].get(prefix)

                if bucket is not None:
                    # We need previous values whose bit b is opposite to x's bit b.
                    # That guarantees the highest differing bit is exactly b because:
                    # - all higher bits are equal (same prefix)
                    # - bit b differs
                    candidate_prev: int = bucket[bit ^ 1]

                    if candidate_prev > 0:
                        # Extend that best chain by appending x.
                        # candidate_prev already represents the best chain length ending
                        # at some previous y with previous last XOR < 2^b.
                        dp_less[b] = max(dp_less[b], candidate_prev + 1)

            # dp_less must be monotone non-decreasing in b:
            # If a chain has last XOR < 2^b, then certainly it also has last XOR < 2^(b+1).
            #
            # This monotonicity is essential because later transitions query dp_less[h].
            for b in range(1, max_bits):
                if dp_less[b] < dp_less[b - 1]:
                    dp_less[b] = dp_less[b - 1]

            # Update the global answer using the best chain ending at x.
            # The largest threshold 2^(max_bits-1) is not enough to cover all possible
            # XOR values, but dp_less is monotone and every actual chain length ending
            # at x appears in some dp_less[b] where the previous edge is below 2^b.
            # Since all XOR values are < 2^30, dp_less[29] is sufficient.
            if dp_less[max_bits - 1] > answer:
                answer = dp_less[max_bits - 1]

            # Insert current value x into all grouped structures so future elements can
            # use x as a predecessor endpoint.
            #
            # For each bit b:
            # - prefix above b identifies the group of numbers that match on all higher bits
            # - bit b identifies which side of the split x belongs to
            # - stored value is dp_less[b], because future transitions with msb = b need
            #   exactly the best chain ending at x whose previous last XOR is < 2^b
            for b in range(max_bits):
                prefix = x >> (b + 1)
                bit = (x >> b) & 1

                if prefix not in grouped_best[b]:
                    grouped_best[b][prefix] = [0, 0]

                if dp_less[b] > grouped_best[b][prefix][bit]:
                    grouped_best[b][prefix][bit] = dp_less[b]

        return answer


def trace_examples() -> None:
    """
    Run the examples from the statement and print the results.

    Args:
        None

    Returns:
        None

    Time complexity:
        O(total_n * 30)

    Space complexity:
        O(total_n * 30)
    """
    solver = Solution()

    examples: List[Tuple[List[int], int]] = [
        ([1, 2, 7, 3], 3),
        ([4, 1, 6, 14, 2], 4),
    ]

    for arr, expected in examples:
        result = solver.maximumSecureRelayChain(arr)
        print(f"signatures = {arr}")
        print(f"maximum secure relay chain length = {result}")
        print(f"expected = {expected}")
        print(f"match = {result == expected}")
        print("-" * 60)


if __name__ == "__main__":
    trace_examples()