"""
Title: Minimum Rewrites to Match a Layered Bit Template

Problem Description:
You are given two integers, n and target, and an array costs of length n. You must
build a nonnegative integer x using exactly n bits, where bit i (0-indexed from the
least significant bit) may be rewritten at a cost of costs[i]. Initially, x is equal
to 0, so every bit is unset.

A security system checks x through a layered template rule. For every k from 1 to n,
let low(k) be the integer formed by the lowest k bits of x. The template is satisfied
if, for every k, the number of set bits in low(k) has the same parity as the k-th
lowest bit of target. In other words, for each prefix of bits from the least
significant side, the parity of that prefix in x must match a required parity sequence
derived from target.

You may choose any bits of x to flip from 0 to 1, paying the corresponding rewrite
costs. Return the minimum total cost needed to construct such an x. If no such x
exists, return -1.

This is a hard bit-manipulation problem because the parity constraints on all prefixes
are coupled: choosing one bit affects every larger prefix. A brute-force search over
all 2^n bitmasks will not pass.

Constraints:
- 1 <= n <= 200000
- 0 <= target < 2^n
- costs.length == n
- 1 <= costs[i] <= 10^9

Key Observation:
Let p_k be the required parity for the lowest k bits of x. The problem states that
p_k equals the (k-1)-th bit of target.

If we define p_0 = 0 (the empty prefix has even parity), then for each bit i:
    x_i = p_i XOR p_{i+1}
because adding bit i changes prefix parity exactly when x_i = 1.

Since every required prefix parity is fixed by target, every bit of x is determined
uniquely. Therefore:
- There is always exactly one valid x.
- The minimum cost is simply the sum of costs[i] for every position i where that
  uniquely determined bit x_i equals 1.

Using target bits directly:
- p_{i+1} = bit_i(target)
- p_i = bit_{i-1}(target) for i >= 1, and p_0 = 0

So:
- x_0 = bit_0(target)
- x_i = bit_{i-1}(target) XOR bit_i(target) for i >= 1
"""

from typing import List


class Solution:
    def minimum_rewrite_cost(self, n: int, target: int, costs: List[int]) -> int:
        """
        Compute the minimum total cost to build an n-bit integer x whose prefix-parity
        sequence matches the bits of target.

        The parity constraints uniquely determine every bit of x:
        - Let p_k be the parity of the lowest k bits of x.
        - The problem requires p_k to equal the (k-1)-th bit of target.
        - Since p_0 = 0, each bit x_i is:
              x_i = p_i XOR p_{i+1}
        Therefore, there is exactly one valid x, and the answer is the sum of costs
        for the positions where that x has a 1 bit.

        Args:
            n: Number of bits in x.
            target: Integer whose bits define the required prefix parities.
            costs: costs[i] is the cost to set bit i from 0 to 1.

        Returns:
            The minimum total rewrite cost. Returns -1 only if the input shape is
            invalid; under the stated constraints, a valid x always exists.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        # Basic input validation. Under the official constraints this should never fail,
        # but returning -1 here makes the function robust and self-contained.
        if n < 1 or len(costs) != n:
            return -1

        # The target must fit in n bits according to the problem statement.
        # If it does not, the required parity sequence would reference bits outside
        # the allowed n-bit construction.
        if target < 0 or target >= (1 << n):
            return -1

        # This variable will accumulate the total cost of all positions where the
        # uniquely determined valid x has a 1 bit.
        total_cost: int = 0

        # prev_parity_bit represents p_i while we iterate to compute x_i.
        #
        # Definition:
        #   p_k = required parity of the lowest k bits of x
        #
        # We know:
        #   p_0 = 0
        #
        # So before processing bit 0, the "previous parity" is the empty-prefix parity.
        prev_parity_bit: int = 0

        # We process bits from least significant to most significant because the
        # prefix-parity definition is naturally built in that direction.
        for i in range(n):
            # current_parity_bit is p_{i+1}, which by the problem statement equals
            # the i-th bit of target.
            current_parity_bit: int = (target >> i) & 1

            # The i-th bit of x is determined by whether the required parity changes
            # when we extend the prefix from length i to length i+1.
            #
            # If parity changes, x_i must be 1.
            # If parity stays the same, x_i must be 0.
            #
            # Algebraically:
            #   x_i = p_i XOR p_{i+1}
            x_bit: int = prev_parity_bit ^ current_parity_bit

            # If the uniquely required bit is 1, we must pay the rewrite cost for
            # this position because x starts as all zeros and only 0 -> 1 flips cost.
            if x_bit == 1:
                total_cost += costs[i]

            # Move forward: the current required parity becomes the previous parity
            # for the next iteration.
            prev_parity_bit = current_parity_bit

        # At this point we have summed exactly the costs of the unique valid x.
        return total_cost


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    #
    # Important note:
    # The narrative in the prompt is internally inconsistent. The mathematically
    # correct interpretation of the stated rule gives a unique x and therefore a
    # deterministic answer.
    #
    # For n = 4, target = 11 (binary 1011, low-to-high bits: 1,1,0,1):
    # required prefix parities are:
    #   p1=1, p2=1, p3=0, p4=1
    # with p0=0
    #
    # Then:
    #   x0 = p0 XOR p1 = 0 XOR 1 = 1
    #   x1 = p1 XOR p2 = 1 XOR 1 = 0
    #   x2 = p2 XOR p3 = 1 XOR 0 = 1
    #   x3 = p3 XOR p4 = 0 XOR 1 = 1
    #
    # So x = 1101 (from high to low), and cost = costs[0] + costs[2] + costs[3]
    # = 5 + 7 + 1 = 13.
    n1 = 4
    target1 = 11
    costs1 = [5, 2, 7, 1]
    result1 = solution.minimum_rewrite_cost(n1, target1, costs1)
    print(result1)  # Correct under the stated rule: 13

    # Sample 2 from the prompt.
    #
    # target = 6 = binary 00110, low-to-high bits: 0,1,1,0,0
    # required prefix parities:
    #   p1=0, p2=1, p3=1, p4=0, p5=0
    # with p0=0
    #
    # Then:
    #   x0 = 0 XOR 0 = 0
    #   x1 = 0 XOR 1 = 1
    #   x2 = 1 XOR 1 = 0
    #   x3 = 1 XOR 0 = 1
    #   x4 = 0 XOR 0 = 0
    #
    # So set bits are 1 and 3, and total cost = 9 + 3 = 12.
    #
    # The prompt's stated output 4 is inconsistent with the written rule.
    n2 = 5
    target2 = 6
    costs2 = [4, 9, 1, 3, 8]
    result2 = solution.minimum_rewrite_cost(n2, target2, costs2)
    print(result2)  # Correct under the stated rule: 12