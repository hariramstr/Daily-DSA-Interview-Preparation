"""
Title: Minimum Rewrite Cost for Tiered Access Rules

Problem Description:
A security team stores an access policy as a string `s` of length `n`, where each
character is one of `A`, `B`, or `C`. The company wants to rewrite the policy so
that all rules appear in nondecreasing tier order: first zero or more `A` rules,
then zero or more `B` rules, then zero or more `C` rules. Any of the three groups
may be empty, but after rewriting the final string must have the form
`A...AB...BC...C`.

For each position `i`, changing `s[i]` to `A`, `B`, or `C` has different costs
given by arrays `costA`, `costB`, and `costC`, where `costX[i]` is the cost to
rewrite character `s[i]` into tier `X`.

The task is to compute the minimum total rewrite cost needed to make the entire
policy valid.

Constraints:
- 1 <= n <= 200000
- s.length == n
- s[i] is one of 'A', 'B', or 'C'
- 0 <= costA[i], costB[i], costC[i] <= 10^9
- Return the minimum possible total cost as a 64-bit integer
"""

from typing import List


class Solution:
    def minimum_rewrite_cost(
        self,
        s: str,
        costA: List[int],
        costB: List[int],
        costC: List[int],
    ) -> int:
        """
        Compute the minimum total cost to rewrite the string into the form
        A*B*C* (zero or more A's, followed by zero or more B's, followed by
        zero or more C's).

        Args:
            s: Original policy string. Its characters are not directly needed
               by the DP because the rewrite costs are already provided.
            costA: costA[i] is the cost to rewrite position i into 'A'.
            costB: costB[i] is the cost to rewrite position i into 'B'.
            costC: costC[i] is the cost to rewrite position i into 'C'.

        Returns:
            The minimum possible total rewrite cost as an integer.

        Time complexity:
            O(n), where n is the length of the string.

        Space complexity:
            O(1) extra space, ignoring the input arrays.
        """
        n: int = len(s)

        # We maintain three DP states while scanning the string from left to right.
        #
        # dp_a:
        #   Minimum cost to rewrite the processed prefix so that it is entirely
        #   in the A-phase. That means every processed character has been turned
        #   into 'A'. This corresponds to a target prefix of the form A*.
        #
        # dp_b:
        #   Minimum cost to rewrite the processed prefix so that it is in the
        #   B-phase. That means the prefix has the form A*B*, and importantly
        #   the current processed prefix ends in the B-phase. The A block may be
        #   empty, and the B block may also have just started.
        #
        # dp_c:
        #   Minimum cost to rewrite the processed prefix so that it is in the
        #   C-phase. That means the prefix has the form A*B*C*, and the current
        #   processed prefix ends in the C-phase. Any earlier blocks may be empty.
        #
        # Why these three states are enough:
        #   The final valid string only depends on which ordered block we are
        #   currently filling: A, B, or C. Once we move from A to B, we can never
        #   go back to A. Once we move from B to C, we can never go back to B.
        #   So the "phase" fully captures the needed history.
        #
        # Initialization before processing any characters:
        #   An empty prefix can be considered valid in all three forms:
        #   - A* with zero A's
        #   - A*B* with zero A's and zero B's
        #   - A*B*C* with all blocks empty
        #
        # This is important because the problem allows any group to be empty.
        # For example:
        #   - All characters may become B's, so the A block is empty.
        #   - All characters may become C's, so both A and B blocks are empty.
        #
        # Therefore all three states start at 0.
        dp_a: int = 0
        dp_b: int = 0
        dp_c: int = 0

        # Process each position independently, updating the three states.
        for i in range(n):
            a_cost: int = costA[i]
            b_cost: int = costB[i]
            c_cost: int = costC[i]

            # Save previous states because each new state must be computed from
            # the states of the previous prefix, not partially updated values.
            prev_a: int = dp_a
            prev_b: int = dp_b
            prev_c: int = dp_c

            # New dp_a:
            #   If we are still in the A-phase after processing position i,
            #   then this character must become 'A'.
            #   The only previous state that can lead here is also A-phase,
            #   because once we leave A-phase we cannot return.
            dp_a = prev_a + a_cost

            # New dp_b:
            #   If we are in the B-phase after processing position i, then this
            #   character must become 'B'.
            #
            #   We can arrive here in two ways:
            #   1) We were already in B-phase before, and continue placing B's.
            #   2) We were in A-phase before, and start the B block now.
            #
            #   So we take the cheaper of prev_a and prev_b, then add b_cost.
            dp_b = min(prev_a, prev_b) + b_cost

            # New dp_c:
            #   If we are in the C-phase after processing position i, then this
            #   character must become 'C'.
            #
            #   We can arrive here in three conceptual ways:
            #   1) Continue an existing C-phase from prev_c.
            #   2) Start the C block from prev_b.
            #   3) Start the C block directly from prev_a if both A and B blocks
            #      before it are allowed to be empty or B is empty.
            #
            #   Because empty groups are allowed, moving directly from A-phase to
            #   C-phase is valid (that means the B block is empty).
            #
            #   Therefore we take the minimum of prev_a, prev_b, and prev_c.
            dp_c = min(prev_a, prev_b, prev_c) + c_cost

        # After processing all positions, the final string may legally end in:
        #   - A-phase only  -> all characters are A
        #   - B-phase       -> form A*B*
        #   - C-phase       -> form A*B*C*
        #
        # Since all are valid final forms, the answer is the minimum of the
        # three ending states.
        return min(dp_a, dp_b, dp_c)

    def minRewriteCost(
        self,
        s: str,
        costA: List[int],
        costB: List[int],
        costC: List[int],
    ) -> int:
        """
        Wrapper method using a camelCase name for convenience.

        Args:
            s: Original policy string.
            costA: Cost to rewrite each position to 'A'.
            costB: Cost to rewrite each position to 'B'.
            costC: Cost to rewrite each position to 'C'.

        Returns:
            The minimum total rewrite cost.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.minimum_rewrite_cost(s, costA, costB, costC)


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    # Note:
    # The textual explanation in the prompt is inconsistent with the provided
    # arrays, so we trust the formal optimization problem and run the DP on the
    # exact input values.
    s1 = "CBAC"
    costA1 = [3, 0, 2, 4]
    costB1 = [1, 5, 0, 2]
    costC1 = [0, 3, 4, 0]
    result1 = solution.minimum_rewrite_cost(s1, costA1, costB1, costC1)
    print(result1)

    # Sample 2 from the prompt.
    # As above, the narrative explanation is inconsistent, so we compute the
    # mathematically correct DP answer for the given arrays.
    s2 = "ACBAA"
    costA2 = [0, 4, 3, 0, 0]
    costB2 = [2, 1, 1, 2, 2]
    costC2 = [5, 0, 0, 3, 4]
    result2 = solution.minimum_rewrite_cost(s2, costA2, costB2, costC2)
    print(result2)