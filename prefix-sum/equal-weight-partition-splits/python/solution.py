```python
"""
Title: Equal Weight Partition Splits
Difficulty: Medium
Topic: Prefix Sum

Problem Description:
You are given an array of positive integers `weights` representing the weights of items
arranged in a line. You want to split the array into exactly three non-empty contiguous
subarrays such that the sum of weights in each subarray is equal.

Return the number of ways you can make such a split.

A split is defined by choosing two cut points i and j where 0 < i < j < n (0-based indexing),
such that:
- The first subarray is weights[0..i-1]
- The second subarray is weights[i..j-1]
- The third subarray is weights[j..n-1]

All three subarrays must have the same sum.

Constraints:
- 3 <= weights.length <= 10^5
- 1 <= weights[i] <= 10^4
"""

from typing import List


class Solution:
    def countWays(self, weights: List[int]) -> int:
        """
        Count the number of ways to split the array into three non-empty parts
        with equal sums using the prefix sum technique.

        The key insight:
        - Total sum must be divisible by 3; otherwise, answer is 0.
        - Each part must sum to total_sum // 3 (call it target).
        - We need to find cut point i such that prefix_sum[i] == target,
          and cut point j such that prefix_sum[j] == 2 * target,
          with i < j and both leaving non-empty subarrays.

        Strategy:
        1. Compute total sum; if not divisible by 3, return 0.
        2. Build prefix sums array.
        3. For each valid j (where prefix_sum[j] == 2 * target and j < n),
           count how many valid i positions exist (where prefix_sum[i] == target and i < j).
        4. We accumulate the count of valid i positions as we scan left to right.

        Args:
            weights: List of positive integers representing item weights.

        Returns:
            The number of valid ways to split into three equal-sum parts.

        Time Complexity: O(n) — single pass after computing prefix sums.
        Space Complexity: O(n) — for the prefix sums array.
        """

        # -----------------------------------------------------------------------
        # Step 1: Compute the total sum of all weights.
        # -----------------------------------------------------------------------
        n = len(weights)
        total_sum = sum(weights)

        # -----------------------------------------------------------------------
        # Step 2: Early exit if total_sum is not divisible by 3.
        # If the total can't be divided equally into 3 parts, no valid split exists.
        # -----------------------------------------------------------------------
        if total_sum % 3 != 0:
            return 0

        # Each of the three parts must sum to exactly this value.
        target = total_sum // 3

        # -----------------------------------------------------------------------
        # Step 3: Build the prefix sums array.
        # prefix[k] = sum of weights[0..k-1] (i.e., sum of first k elements).
        # prefix[0] = 0 (empty prefix)
        # prefix[n] = total_sum
        #
        # Why prefix sums?
        # The sum of subarray weights[a..b-1] = prefix[b] - prefix[a].
        # This lets us check subarray sums in O(1).
        # -----------------------------------------------------------------------
        prefix = [0] * (n + 1)
        for k in range(n):
            prefix[k + 1] = prefix[k] + weights[k]

        # -----------------------------------------------------------------------
        # Step 4: Count valid splits using a single left-to-right scan.
        #
        # We need:
        #   - Cut point i (1 <= i <= n-2): prefix[i] == target
        #     (first part is weights[0..i-1], which sums to target)
        #   - Cut point j (i+1 <= j <= n-1): prefix[j] == 2 * target
        #     (second part is weights[i..j-1], which sums to target,
        #      and third part is weights[j..n-1], which also sums to target)
        #
        # Note on index constraints:
        #   - i must be at least 1 (first part non-empty: at least weights[0])
        #   - i must be at most n-2 (second and third parts each need at least one element)
        #   - j must be at least i+1 (second part non-empty)
        #   - j must be at most n-1 (third part non-empty: at least weights[n-1])
        #
        # Approach:
        #   Scan index k from 1 to n-1 (representing possible cut points).
        #   - If prefix[k] == target AND k <= n-2, then k is a valid i position.
        #     We accumulate this count in `valid_i_count`.
        #   - If prefix[k] == 2 * target AND k <= n-1 (i.e., k >= 2 so j has room),
        #     then k is a valid j position. Every previously counted valid i
        #     (where i < k) contributes one valid split. Add valid_i_count to result.
        #
        # Why does this work?
        #   When we reach a valid j = k, all valid i positions we've seen so far
        #   satisfy i < j (since we process left to right and only count i before j).
        #   Also, the third part sum is automatically target because:
        #     total_sum - prefix[j] = 3*target - 2*target = target. ✓
        # -----------------------------------------------------------------------

        result = 0          # Total number of valid splits found
        valid_i_count = 0   # Number of valid first-cut positions seen so far

        # We iterate k from 1 to n-1 inclusive.
        # k represents a potential cut point (either i or j).
        for k in range(1, n):

            # -------------------------------------------------------------------
            # Check if k is a valid j (second cut point).
            # Conditions:
            #   - prefix[k] == 2 * target  (first two parts together sum to 2*target)
            #   - k <= n - 1               (third part weights[k..n-1] is non-empty)
            #     Since our loop goes up to n-1, this is always satisfied here.
            #   - k >= 2                   (we need at least one element in second part,
            #     meaning i < j, so j >= 2; but valid_i_count handles this naturally
            #     since i must be < k and i >= 1, so k >= 2 is implicit when
            #     valid_i_count > 0)
            #
            # We check j BEFORE updating valid_i_count for position k,
            # ensuring i < j (strict inequality).
            # -------------------------------------------------------------------
            if prefix[k] == 2 * target:
                # Every valid i we've accumulated so far pairs with this j = k.
                result += valid_i_count

            # -------------------------------------------------------------------
            # Check if k is a valid i (first cut point).
            # Conditions:
            #   - prefix[k] == target      (first part sums to target)
            #   - k <= n - 2               (second and third parts each need at least
            #                               one element, so i can be at most n-2)
            # -------------------------------------------------------------------
            if prefix[k] == target and k <= n - 2:
                valid_i_count += 1

        return result


# =============================================================================
# Main block: Test the solution with the provided examples and verify correctness.
# =============================================================================
if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1:
    # weights = [1, 2, 3, 0, 3]
    # Total sum = 9, target = 3
    # prefix = [0, 1, 3, 6, 6, 9]
    #
    # Scan:
    #   k=1: prefix[1]=1, not 2*3=6, not target=3
    #   k=2: prefix[2]=3, not 6; IS target=3 and k<=3 → valid_i_count=1
    #   k=3: prefix[3]=6, IS 2*target → result += 1 (result=1); prefix[3]=6 not target
    #   k=4: prefix[4]=6, IS 2*target → result += 1 (result=2); prefix[4]=6 not target
    #
    # Output: 2 ✓
    # -------------------------------------------------------------------------
    weights1 = [1, 2, 3, 0, 3]
    output1 = solution.countWays(weights1)
    print(f"Example 1: weights={weights1}")
    print(f"  Expected: 2, Got: {output1}")
    print(f"  {'PASS' if output1 == 2 else 'FAIL'}")
    print()

    # -------------------------------------------------------------------------
    # Example 2:
    # weights = [1, 1, 1, 1, 1, 1]
    # Total sum = 6, target = 2
    # prefix = [0, 1, 2, 3, 4, 5, 6]
    #
    # Scan:
    #   k=1: prefix[1]=1, not 4, not 2
    #   k=2: prefix[2]=2, not 4; IS target=2 and k<=4 → valid_i_count=1
    #   k=3: prefix[3]=3, not 4; not target
    #   k=4: prefix[4]=4, IS 2*target → result += 1 (result=1);
    #         prefix[4]=4 not target
    #   k=5: prefix[5]=5, not 4; not target (k=5 > n-2=4, so no)
    #
    # Hmm, that gives 1, but expected is 4. Let me re-trace carefully.
    #
    # weights = [1,1,1,1,1,1], n=6
    # prefix = [0,1,2,3,4,5,6]
    # target = 2, 2*target = 4
    #
    # k=1: prefix[1]=1 ≠ 4; prefix[1]=1 ≠ 2
    # k=2: prefix[2]=2 ≠ 4; prefix[2]=2 == target, k=2 <= n-2=4 → valid_i_count=1
    # k=3: prefix[3]=3 ≠ 4; prefix[3]=3 ≠ 2
    # k=4: prefix[4]=4 == 2*target → result += valid_i_count=1 → result=1
    #       prefix[4]=4 ≠ 2
    # k=5: prefix[5]=5 ≠ 4; prefix[5]=5 ≠ 2 (also k=5 > n-2=4)
    #
    # Result = 1, but expected = 4. Something is wrong!
    #
    # Let me reconsider. The valid splits for [1,1,1,1,1,1]:
    # We need i and j such that:
    #   sum(weights[0..i-1]) = 2  → prefix[i] = 2 → i=2
    #   sum(weights[i..j-1]) = 2  → prefix[j] - prefix[i] = 2 → prefix[j] = 4 → j=4
    #   sum(weights[j..5]) = 2    → 6 - prefix[j] = 2 → prefix[j] = 4 → j=4
    #
    # So only one valid (i=2, j=4)? But the problem says 4 splits...
    #
    # Wait, let me re-read the problem. The problem says:
    # "0 < i < j < n" where the split is:
    #   first: weights[0..i-1], second: weights[i..j-1], third: weights[j..n-1]
    #
    # For [1,1,1,1,1,1] with target=2:
    # Valid i: prefix[i] = 2 → i=2 (only one such i since all weights are 1)
    # Valid j: prefix[j] = 4 → j=4 (only one such j)
    # So only 1 split? But expected is 4...
    #
    # Hmm, let me reconsider. Maybe the problem allows zeros and the "equal sum"
    # condition with zeros creates multiple valid positions.
    #
    # Actually wait - [1,1,1,1,1,1] has no zeros. Let me re-examine the expected output.
    # The problem says output is 4 for this input. Let me think again...
    #
    # Oh wait! Maybe I'm misreading the cut point definition. Let me re-read:
    # "choosing two cut points i and j where 0 < i < j < n"
    # first: weights[0..i-1], second: weights[i..j-1], third: weights[j..n-1]
    #
    # For n=6, valid i in {1,2,3,4}, valid j in {i+1,...,5}
    # prefix[i] = 2 → i=2
    # prefix[j] = 4 → j=4
    # Only 1 split. But expected is 4.
    #
    # I must be misunderstanding the problem. Let me re-read more carefully.
    # "0 < i < j < n" — maybe i and j are indices INTO the array (not cut points
    # between elements)?
    #
    # Alternative interpretation: i and j are indices such that:
    # first: weights[0..i], second: weights[i+1..j-1], third: weights[j..n-1]
    # OR: first: weights[0..i], second: weights[i+1..j], third: weights[j+1..n-1]
    #
    # Let me try: first: weights[0..i-1], second: weights[i..j], third: weights[j+1..n-1]
    # For [1,1,1,1,1,1], target=2:
    # prefix[i] = 2 → i=2 (first part is weights[0..1])
    # prefix[j+1] - prefix[i] = 2 → prefix[j+1] = 4 → j+1=4 → j=3
    # total - prefix[j+1] = 2 → prefix[j+1] = 4 ✓
    # Only 1 split still.
    #
    # Hmm. Let me try yet another interpretation where zeros matter.
    # Actually, the example says "Valid splits exist at multiple (i, j) pairs"
    # for [1,1,1,1,1,1]. This is confusing because with all 1s and target=2,
    # there's only one way to get prefix sum = 2 (at index 2) and one way to
    # get prefix sum = 4 (at index 4).
    #
    # Unless... the problem is using a different definition where the cut points
    # can be between any elements and zeros are allowed to shift things?
    # But there are no zeros here.
    #
    # Wait - maybe I need to reconsider. Perhaps the problem statement's Example 2
    # is wrong or I'm misreading it. Let me try to figure out what 4 splits would be.
    #
    # For [1,1,1,1,1,1] with 4 valid splits, maybe the split definition is different:
    # Perhaps "i" and "j" are indices of elements (not gaps), and the split is:
    # first: weights[0..i], second: weights[i+1..j-1], third: weights[j..n-1]
    # with 0 <= i < j <= n-1, i >= 0, j <= n-1, and middle part non-empty means j > i+1.
    #
    # Hmm, let me try: the problem might mean something like L