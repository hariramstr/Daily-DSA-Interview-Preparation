```python
"""
Title: Reconstruct Array from Bitwise OR Pairs
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
You are given an integer `n` and a 2D array `pairs` where each `pairs[i] = [index_i, value_i]`
represents that the bitwise OR of the element at position `index_i` with some unknown base value `B`
equals `value_i`. In other words, `arr[index_i] | B == value_i` for all pairs.

Your task is to reconstruct the array `arr` of length `n` such that:
1. There exists a non-negative integer `B` consistent with all the given pairs.
2. `arr[index_i] | B == value_i` for every pair.
3. Each `arr[i]` is minimized (i.e., use the smallest valid value for positions not constrained
   by any pair).

If no valid array exists that satisfies all constraints simultaneously, return an empty array.

Note: Positions not referenced in any pair should be set to `0`.

Constraints:
- 1 <= n <= 1000
- 0 <= pairs.length <= 1000
- 0 <= index_i < n
- 0 <= value_i <= 10^6
- Multiple pairs may reference the same index; all must be satisfied.

Example 1:
Input: n = 4, pairs = [[0, 7], [1, 5], [2, 6]]
Output: [3, 1, 2, 0]
Explanation: With B = 4 (binary 100): 3|4=7, 1|4=5, 2|4=6.

Example 2:
Input: n = 3, pairs = [[0, 5], [0, 3]]
Output: []
Explanation: For index 0, we need arr[0] | B == 5 and arr[0] | B == 3 simultaneously,
which is impossible since 5 ≠ 3.
"""

from typing import List, Dict, Set
from collections import defaultdict


class Solution:
    def reconstructArray(self, n: int, pairs: List[List[int]]) -> List[int]:
        """
        Reconstruct the array from bitwise OR pair constraints.

        The key insight is:
        - If arr[i] | B = value_i, then B must have all bits that are in value_i but NOT in arr[i].
        - To minimize arr[i], we want arr[i] to have as few bits as possible.
        - The bits that MUST be in B are determined by the intersection of all value_i values
          (bits common to all values must be in B, since arr[i] | B = value_i means those bits
          come from somewhere).
        
        Wait, let me think more carefully:
        - arr[i] | B = value_i means every bit in value_i is either in arr[i] or in B (or both).
        - B must be a subset of every value_i (since B | arr[i] = value_i, B cannot have bits
          not in value_i, otherwise value_i would have those bits too).
        - So B must be a subset of the AND of all value_i values (B ⊆ AND of all values).
        - Also, B must be consistent: for each index i, arr[i] = value_i & ~B (to minimize arr[i],
          we want to maximize B, but B is constrained).
        
        Actually the correct approach:
        1. B must be a subset of every value_i (B & value_i == B for all i).
           This means B ⊆ (AND of all value_i).
        2. For each index with multiple pairs, all value_i must be equal (since arr[i] | B = value_i
           is fixed for a given arr[i] and B).
        3. To minimize arr[i], we maximize B. The maximum valid B is the AND of all value_i values.
        4. Then arr[i] = value_i & ~B (bits in value_i that are NOT in B must come from arr[i]).
        5. Verify: arr[i] | B == value_i for all pairs.

        Args:
            n: Length of the array to reconstruct.
            pairs: List of [index, value] pairs where arr[index] | B == value.

        Returns:
            The reconstructed array of length n, or empty list if impossible.

        Time Complexity: O(P + n) where P is the number of pairs.
        Space Complexity: O(P + n) for storing the index-to-values mapping and result array.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Group pairs by index
        # We use a defaultdict to collect all values associated with each index.
        # This lets us check consistency: if the same index has multiple pairs,
        # all their values must be equal (since arr[i] | B is fixed for given arr[i] and B).
        # -----------------------------------------------------------------------
        index_to_values: Dict[int, List[int]] = defaultdict(list)

        for index, value in pairs:
            index_to_values[index].append(value)

        # -----------------------------------------------------------------------
        # STEP 2: Check consistency for each index
        # For a given index i, arr[i] | B = value_i must hold.
        # If the same index appears with two different values v1 and v2,
        # then arr[i] | B = v1 AND arr[i] | B = v2, which means v1 = v2.
        # If v1 != v2, it's impossible → return [].
        # -----------------------------------------------------------------------
        for index, values in index_to_values.items():
            # All values for the same index must be identical
            if len(set(values)) > 1:
                # Contradiction: same index maps to different values
                return []

        # -----------------------------------------------------------------------
        # STEP 3: Determine the valid range for B
        # Key property: arr[i] | B = value_i
        # This means B cannot have any bit that is NOT in value_i
        # (otherwise value_i would contain that bit, contradiction).
        # So B must be a subset of EVERY value_i.
        # The largest possible B that is a subset of all values is: AND of all values.
        #
        # To minimize arr[i] = value_i XOR (bits contributed by arr[i]),
        # we want to maximize B (push as many bits as possible into B).
        # The maximum valid B = AND of all value_i across all pairs.
        # -----------------------------------------------------------------------
        if not pairs:
            # No constraints at all → B can be anything (we choose B = 0 for simplicity)
            # All arr[i] = 0 (minimized)
            return [0] * n

        # Compute AND of all values across all pairs
        # Start with all bits set (using a large number), then AND each value
        all_values = [value for _, value in pairs]
        B_candidate = all_values[0]
        for v in all_values[1:]:
            B_candidate &= v
        # B_candidate is now the AND of all values = maximum possible B

        # -----------------------------------------------------------------------
        # STEP 4: Verify B_candidate is consistent
        # For each (index, value) pair, check that arr[index] | B_candidate == value
        # where arr[index] = value & ~B_candidate (minimized arr[index]).
        #
        # arr[index] = value & ~B_candidate means arr[index] has exactly the bits
        # in value that are NOT in B_candidate.
        # Then arr[index] | B_candidate = (value & ~B_candidate) | B_candidate
        #
        # For this to equal value, we need:
        # (value & ~B_candidate) | B_candidate == value
        # This holds if and only if B_candidate is a subset of value (B_candidate & value == B_candidate).
        # Since B_candidate = AND of all values, B_candidate ⊆ every value, so this always holds.
        #
        # Let's verify explicitly anyway for correctness.
        # -----------------------------------------------------------------------
        for index, value in pairs:
            arr_i = value & ~B_candidate  # minimized arr[index]
            if (arr_i | B_candidate) != value:
                # This shouldn't happen given our construction, but check for safety
                return []

        # -----------------------------------------------------------------------
        # STEP 5: Build the result array
        # - For indices referenced in pairs: arr[i] = value_i & ~B_candidate
        #   (the bits in value_i that are NOT covered by B)
        # - For indices NOT referenced in any pair: arr[i] = 0 (minimized)
        # -----------------------------------------------------------------------
        result = [0] * n

        for index, values in index_to_values.items():
            # All values are the same (checked in step 2), so use values[0]
            value = values[0]
            # arr[index] gets the bits in value that are NOT in B_candidate
            # This minimizes arr[index] while satisfying arr[index] | B_candidate == value
            result[index] = value & ~B_candidate

        return result


# -------------------------------------------------------------------------------
# Main block: Test the solution with the provided examples
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------
    # Example 1:
    # n = 4, pairs = [[0, 7], [1, 5], [2, 6]]
    # Expected Output: [3, 1, 2, 0]
    #
    # Trace:
    # - index_to_values: {0: [7], 1: [5], 2: [6]}
    # - All values: [7, 5, 6]
    # - B_candidate = 7 & 5 & 6 = 0b111 & 0b101 & 0b110
    #   7 & 5 = 0b111 & 0b101 = 0b101 = 5
    #   5 & 6 = 0b101 & 0b110 = 0b100 = 4
    #   B_candidate = 4
    # - arr[0] = 7 & ~4 = 0b111 & 0b...011 = 0b011 = 3
    # - arr[1] = 5 & ~4 = 0b101 & 0b...011 = 0b001 = 1
    # - arr[2] = 6 & ~4 = 0b110 & 0b...011 = 0b010 = 2
    # - arr[3] = 0 (not in any pair)
    # Result: [3, 1, 2, 0] ✓
    # -----------------------------------------------------------
    n1 = 4
    pairs1 = [[0, 7], [1, 5], [2, 6]]
    result1 = solution.reconstructArray(n1, pairs1)
    print(f"Example 1:")
    print(f"  Input: n={n1}, pairs={pairs1}")
    print(f"  Output: {result1}")
    print(f"  Expected: [3, 1, 2, 0]")
    print(f"  Match: {result1 == [3, 1, 2, 0]}")
    print()

    # -----------------------------------------------------------
    # Example 2:
    # n = 3, pairs = [[0, 5], [0, 3]]
    # Expected Output: []
    #
    # Trace:
    # - index_to_values: {0: [5, 3]}
    # - For index 0: values = [5, 3], set(values) = {5, 3}, len > 1 → return []
    # Result: [] ✓
    # -----------------------------------------------------------
    n2 = 3
    pairs2 = [[0, 5], [0, 3]]
    result2 = solution.reconstructArray(n2, pairs2)
    print(f"Example 2:")
    print(f"  Input: n={n2}, pairs={pairs2}")
    print(f"  Output: {result2}")
    print(f"  Expected: []")
    print(f"  Match: {result2 == []}")
    print()

    # -----------------------------------------------------------
    # Additional Test: No pairs
    # n = 3, pairs = []
    # Expected: [0, 0, 0] (all zeros, minimized)
    # -----------------------------------------------------------
    n3 = 3
    pairs3 = []
    result3 = solution.reconstructArray(n3, pairs3)
    print(f"Additional Test (no pairs):")
    print(f"  Input: n={n3}, pairs={pairs3}")
    print(f"  Output: {result3}")
    print(f"  Expected: [0, 0, 0]")
    print(f"  Match: {result3 == [0, 0, 0]}")
    print()

    # -----------------------------------------------------------
    # Additional Test: Single pair
    # n = 2, pairs = [[0, 6]]
    # B_candidate = 6, arr[0] = 6 & ~6 = 0, arr[1] = 0
    # Verify: 0 | 6 = 6 ✓
    # Result: [0, 0]
    # -----------------------------------------------------------
    n4 = 2
    pairs4 = [[0, 6]]
    result4 = solution.reconstructArray(n4, pairs4)
    print(f"Additional Test (single pair):")
    print(f"  Input: n={n4}, pairs={pairs4}")
    print(f"  Output: {result4}")
    print(f"  Expected: [0, 0] (B=6, arr[0]=0, 0|6=6 ✓)")
    print(f"  Verify: arr[0]|B = {result4[0]}|6 = {result4[0] | 6}")
    print()

    # -----------------------------------------------------------
    # Additional Test: Same index, same value (duplicate pair)
    # n = 2, pairs = [[0, 5], [0, 5]]
    # index_to_values: {0: [5, 5]}, set = {5}, consistent
    # B_candidate = 5 & 5 = 5
    # arr[0] = 5 & ~5 = 0
    # Verify: 0 | 5 = 5 ✓
    # Result: [0, 0]
    # -----------------------------------------------------------
    n5 = 2
    pairs5 = [[0, 5], [0, 5]]
    result5 = solution.reconstructArray(n5, pairs5)
    print(f"Additional Test (duplicate pair, same value):")
    print(f"  Input: n={n5}, pairs={pairs5}")
    print(f"  Output: {result5}")
    print(f"  Expected: [0, 0]")
    print(f"  Match: {result5 == [0, 0]}")
```