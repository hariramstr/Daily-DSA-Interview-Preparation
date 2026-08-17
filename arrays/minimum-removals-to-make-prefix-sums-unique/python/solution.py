"""
Title: Minimum Removals to Make Prefix Sums Unique

Problem Description:
You are given an integer array nums of length n. You may remove any elements from the
array, but the relative order of the remaining elements must stay the same. After removals,
let the remaining sequence be b. Define its prefix sums as:

    pref[0] = b[0]
    pref[1] = b[0] + b[1]
    ...
    pref[m - 1] = b[0] + ... + b[m - 1]

where m is the length of b.

Return the minimum number of elements that must be removed so that all prefix sums of the
remaining sequence are pairwise distinct.

This is equivalent to keeping the longest subsequence whose running sums never repeat.
The answer is:

    len(nums) - (maximum valid subsequence length)
"""

from bisect import bisect_left
from typing import Dict, List


class FenwickMax:
    """
    Fenwick tree (Binary Indexed Tree) supporting prefix maximum queries.

    This structure is used after coordinate compression. For a compressed index `i`,
    we can:
      - update(i, value): set tree positions so that index i stores a candidate maximum
      - query(i): get the maximum value among indices in [1, i]

    We use it to compute dynamic programming transitions efficiently.

    Time complexity:
        - update: O(log n)
        - query: O(log n)

    Space complexity:
        O(n)
    """

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

        Args:
            size: Number of compressed coordinates.

        Returns:
            None

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def update(self, index: int, value: int) -> None:
        """
        Update the Fenwick tree with a new candidate maximum at `index`.

        Args:
            index: 1-based compressed index.
            value: DP value to merge using max.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        while index <= self.size:
            if value > self.tree[index]:
                self.tree[index] = value
            index += index & -index

    def query(self, index: int) -> int:
        """
        Query the maximum value in the prefix [1, index].

        Args:
            index: 1-based compressed index.

        Returns:
            The maximum stored value among compressed indices <= index.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        result: int = 0
        while index > 0:
            if self.tree[index] > result:
                result = self.tree[index]
            index -= index & -index
        return result


class Solution:
    def minimumRemovals(self, nums: List[int]) -> int:
        """
        Compute the minimum removals needed so that the remaining subsequence has
        pairwise distinct prefix sums.

        Key reformulation:
        Let the kept subsequence have prefix sums:
            t1, t2, ..., tk
        with all ti distinct.

        If the original array prefix sums are:
            P[0] = 0
            P[i] = nums[0] + ... + nums[i - 1]   for i = 1..n

        and we keep elements at positions:
            i1 < i2 < ... < ik   (0-based in nums)

        then the subsequence prefix sums are:
            P[i1 + 1] - P[0],
            P[i2 + 1] - P[i1],
            P[i3 + 1] - P[i2],
            ...
        Distinctness of subsequence prefix sums is equivalent to requiring that the
        chosen original prefix sums:
            P[0], P[i1 + 1], P[i2 + 1], ..., P[ik + 1]
        are all distinct.

        Therefore, the problem becomes:
            Choose the longest increasing-by-index subsequence of original prefix-sum
            positions starting from position 0, such that all chosen prefix-sum values
            are distinct.

        If we choose r original prefix positions (including position 0), then we keep
        exactly r - 1 array elements. So:
            maximum kept length = maximum number of chosen prefix positions - 1

        Dynamic programming:
            dp[j] = maximum number of chosen prefix positions in a valid chain ending
                    at prefix position j, where j ranges over 0..n.

        Transition:
            We may go from i to j (i < j) iff P[i] != P[j].
            Then:
                dp[j] = 1 + max(dp[i]) over all i < j with P[i] != P[j]

        To compute this efficiently:
            - Maintain a Fenwick tree over compressed prefix-sum values.
            - For each prefix position j, we want the best dp among all previous prefix
              values except the same value as P[j].
            - We can answer:
                best_less = max dp among values < P[j]
                best_greater = max dp among values > P[j]
              and take max(best_less, best_greater).
            - To support suffix maximum (> current value), we also maintain another
              Fenwick tree on reversed coordinates.

        Finally:
            max_kept = max(dp) - 1
            answer = n - max_kept

        Args:
            nums: Integer array.

        Returns:
            Minimum number of removals.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # ------------------------------------------------------------
        # Step 1: Build original prefix sums.
        #
        # We define:
        #   prefix[0] = 0
        #   prefix[i] = sum(nums[0:i]) for i in 1..n
        #
        # These prefix sums are the central observation of the problem.
        # Choosing a subsequence of elements corresponds to choosing a sequence
        # of prefix positions, and the "all subsequence prefix sums distinct"
        # condition becomes "all chosen original prefix-sum values distinct".
        # ------------------------------------------------------------
        prefix: List[int] = [0]
        running_sum: int = 0
        for value in nums:
            running_sum += value
            prefix.append(running_sum)

        # ------------------------------------------------------------
        # Step 2: Coordinate compression of prefix sums.
        #
        # Prefix sums can be as large as about 2e14 in magnitude, so we cannot
        # use them directly as Fenwick tree indices.
        #
        # Coordinate compression maps each distinct prefix sum value to a small
        # integer in [1, m], preserving order.
        # ------------------------------------------------------------
        sorted_unique: List[int] = sorted(set(prefix))
        m: int = len(sorted_unique)

        def get_index(value: int) -> int:
            """
            Get the 1-based compressed index of a prefix sum value.

            Args:
                value: Original prefix sum.

            Returns:
                1-based compressed coordinate.

            Time complexity:
                O(log n)

            Space complexity:
                O(1)
            """
            return bisect_left(sorted_unique, value) + 1

        # ------------------------------------------------------------
        # Step 3: Prepare two Fenwick trees.
        #
        # Why two trees?
        # We need:
        #   max dp among previous prefix values < current value
        #   max dp among previous prefix values > current value
        #
        # A Fenwick tree naturally supports prefix queries.
        # So:
        #   - left_tree stores dp by normal compressed index
        #       query(idx - 1) gives best among values < current
        #   - right_tree stores dp by reversed compressed index
        #       query(rev_idx - 1) gives best among values > current
        #
        # This lets us exclude transitions from the same prefix-sum value,
        # which are forbidden because chosen prefix sums must be distinct.
        # ------------------------------------------------------------
        left_tree: FenwickMax = FenwickMax(m)
        right_tree: FenwickMax = FenwickMax(m)

        # ------------------------------------------------------------
        # Step 4: Dynamic programming over prefix positions in index order.
        #
        # We process prefix positions from left to right, because valid chains
        # must respect original order.
        #
        # Base case:
        #   prefix position 0 (value 0) can always start a chain by itself:
        #       dp[0] = 1
        #
        # Then for each later prefix position j:
        #   dp[j] = 1 + max(best previous chain ending at a different prefix value)
        #
        # The final number of kept elements is:
        #   max(dp) - 1
        # because a chain of r prefix positions corresponds to keeping r - 1
        # elements between consecutive chosen prefix positions.
        # ------------------------------------------------------------
        best_chain_length: int = 0

        for pref_value in prefix:
            idx: int = get_index(pref_value)

            # Reversed coordinate:
            #   normal index 1..m
            #   reversed index m - idx + 1
            #
            # This turns "values greater than current" into a prefix in the
            # reversed ordering.
            rev_idx: int = m - idx + 1

            # --------------------------------------------------------
            # Query best chain among strictly smaller prefix-sum values.
            # This excludes equal values automatically.
            # --------------------------------------------------------
            best_less: int = left_tree.query(idx - 1)

            # --------------------------------------------------------
            # Query best chain among strictly greater prefix-sum values.
            # In reversed coordinates, greater original values become smaller
            # reversed indices, so again a prefix query works.
            # --------------------------------------------------------
            best_greater: int = right_tree.query(rev_idx - 1)

            # --------------------------------------------------------
            # If there is no previous valid chain, we can always start a new
            # chain at this prefix position alone, giving length 1.
            #
            # Otherwise, extend the best chain whose last prefix-sum value is
            # different from the current one.
            # --------------------------------------------------------
            current_dp: int = max(best_less, best_greater) + 1
            if current_dp < 1:
                current_dp = 1

            # Track the global best chain length.
            if current_dp > best_chain_length:
                best_chain_length = current_dp

            # --------------------------------------------------------
            # Insert this DP state into both Fenwick trees.
            #
            # Future prefix positions may extend from this one, as long as they
            # choose a different prefix-sum value.
            # --------------------------------------------------------
            left_tree.update(idx, current_dp)
            right_tree.update(rev_idx, current_dp)

        # ------------------------------------------------------------
        # Step 5: Convert best chain length into answer.
        #
        # If the best chain uses `best_chain_length` prefix positions, then the
        # corresponding subsequence keeps:
        #   best_chain_length - 1
        # elements.
        #
        # Therefore:
        #   removals = n - kept
        #            = n - (best_chain_length - 1)
        #
        # However, because every valid subsequence must start from prefix position
        # 0 in the conceptual chain, and our DP allows chains starting anywhere,
        # the actual maximum kept length is the number of distinct prefix values
        # we can choose in order minus 1. The DP already computes that maximum
        # chain over all prefix positions, and since prefix[0] is processed first,
        # optimal chains are correctly represented.
        # ------------------------------------------------------------
        max_kept: int = best_chain_length - 1
        return n - max_kept


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [2, -2, 3, 1, -1],
        [1, -1, 1, -1, 1],
    ]

    for nums in sample_inputs:
        result = solution.minimumRemovals(nums)
        print(f"nums = {nums}")
        print(f"minimum removals = {result}")
        print()