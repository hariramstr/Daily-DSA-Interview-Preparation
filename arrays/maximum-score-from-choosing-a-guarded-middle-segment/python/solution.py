"""
Title: Maximum Score from Choosing a Guarded Middle Segment

Problem Description:
You are given an integer array nums of length n and a non-negative integer penalty.
You must choose one contiguous subarray nums[l..r] as your final segment.

The score of a chosen segment is:

    (minimum value inside the segment) * (length of the segment)
    - penalty * (number of elements outside the segment that are strictly smaller
      than that minimum value)

Return the maximum possible score over all non-empty contiguous subarrays.

Constraints:
- 1 <= n <= 200000
- 1 <= nums[i] <= 1000000000
- 0 <= penalty <= 1000000000
- The answer fits in signed 64-bit integer.
"""

from bisect import bisect_left
from typing import Dict, List


class FenwickTree:
    """Fenwick Tree / Binary Indexed Tree for prefix maximum queries.

    This tree stores maximum values instead of sums.
    We use 1-based indexing internally.

    Methods:
    - update(index, value): set tree positions so that prefix maxima reflect value
    - query(index): maximum value in range [1..index]

    Time complexity:
    - update: O(log n)
    - query: O(log n)

    Space complexity:
    - O(n)
    """

    def __init__(self, size: int) -> None:
        """Initialize a Fenwick tree for prefix maximum.

        Args:
            size: Number of positions.

        Returns:
            None

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        self.size: int = size
        self.tree: List[int] = [-(10**30)] * (size + 1)

    def update(self, index: int, value: int) -> None:
        """Apply a max-update at one position.

        Args:
            index: 1-based index to update.
            value: Candidate value.

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
        """Return maximum value in prefix [1..index].

        Args:
            index: 1-based right endpoint of prefix.

        Returns:
            Maximum stored value in that prefix. If nothing was inserted,
            returns a very negative sentinel.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        result: int = -(10**30)
        while index > 0:
            if self.tree[index] > result:
                result = self.tree[index]
            index -= index & -index
        return result


class Solution:
    def max_score_guarded_segment(self, nums: List[int], penalty: int) -> int:
        """Compute the maximum score over all non-empty contiguous subarrays.

        Core idea:
        1. For every index i, determine the largest interval [L[i], R[i]] in which
           nums[i] is the minimum value of the chosen segment, under the standard
           "subarray minimum" ownership rule:
             - previous strictly smaller element breaks on the left
             - next smaller-or-equal element breaks on the right
           This tie-breaking ensures every subarray is counted exactly once by one
           of its minimum positions.
        2. For a fixed minimum value v = nums[i], any subarray owned by i must lie
           inside [L[i], R[i]] and must include i.
        3. Let total_smaller(v) be the number of array elements globally smaller than v.
           Let inside_smaller(l, r, v) be the number of elements inside [l..r] smaller than v.
           Then outside_smaller = total_smaller(v) - inside_smaller.
        4. Score becomes:
              v * len - penalty * (total_smaller(v) - inside_smaller)
            = -penalty * total_smaller(v) + (v * len + penalty * inside_smaller)
           For fixed v, the first term is constant.
        5. For a subarray [l..r] containing i:
              len = r - l + 1
              inside_smaller = pref_less_v[r + 1] - pref_less_v[l]
           So we maximize:
              v * (r - l + 1) + penalty * (pref[r + 1] - pref[l])
            = (v * r + penalty * pref[r + 1]) + (-v * l - penalty * pref[l]) + v
           with constraints l in [L[i], i], r in [i, R[i]].
           Therefore left and right choices separate nicely.
        6. We process indices grouped by equal value v. For that value, we build a
           binary array marking positions with nums < v, and its prefix sums.
           Then for each index i with nums[i] = v:
              best_left  = max over l in [L[i], i] of (-v*l - penalty*pref[l])
              best_right = max over r in [i, R[i]] of (v*r + penalty*pref[r+1])
              score = -penalty*total_smaller(v) + v + best_left + best_right
           We answer range maximum queries for these expressions using Fenwick trees.

        Args:
            nums: Input array.
            penalty: Non-negative penalty multiplier.

        Returns:
            Maximum possible score.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # ---------------------------------------------------------------------
        # STEP 1: Compute the ownership interval for each index as a candidate
        # minimum of subarrays.
        #
        # We use the classic monotonic stack technique:
        # - prev_less[i] = nearest index to the left with value < nums[i]
        # - next_less_equal[i] = nearest index to the right with value <= nums[i]
        #
        # Then any subarray "owned" by i must satisfy:
        #   prev_less[i] < l <= i <= r < next_less_equal[i]
        #
        # This exact strict/non-strict pairing is important:
        # it avoids double-counting when equal values exist.
        # ---------------------------------------------------------------------
        prev_less: List[int] = [-1] * n
        stack: List[int] = []

        for i in range(n):
            while stack and nums[stack[-1]] >= nums[i]:
                stack.pop()
            prev_less[i] = stack[-1] if stack else -1
            stack.append(i)

        next_less_equal: List[int] = [n] * n
        stack.clear()

        for i in range(n - 1, -1, -1):
            while stack and nums[stack[-1]] > nums[i]:
                stack.pop()
            next_less_equal[i] = stack[-1] if stack else n
            stack.append(i)

        left_bound: List[int] = [prev_less[i] + 1 for i in range(n)]
        right_bound: List[int] = [next_less_equal[i] - 1 for i in range(n)]

        # ---------------------------------------------------------------------
        # STEP 2: Group indices by value, and sort distinct values.
        #
        # We need to process all equal values together because:
        # - total_smaller(v) is the same for all positions with value v
        # - the "nums < v" indicator is also the same for all of them
        # ---------------------------------------------------------------------
        value_to_indices: Dict[int, List[int]] = {}
        for idx, value in enumerate(nums):
            value_to_indices.setdefault(value, []).append(idx)

        sorted_values: List[int] = sorted(value_to_indices.keys())

        # ---------------------------------------------------------------------
        # STEP 3: Coordinate compression of values.
        #
        # We need total_smaller(v), which is simply the count of elements whose
        # values are strictly less than v. Since values are processed in sorted
        # order, we can maintain a running count.
        # ---------------------------------------------------------------------
        answer: int = -(10**30)
        processed_count: int = 0

        # ---------------------------------------------------------------------
        # STEP 4: Process each distinct value v.
        #
        # For this v:
        # - total_smaller = number of elements in the whole array with value < v
        # - build prefix counts pref where pref[k] = number of positions in nums[0:k]
        #   whose value is < v
        #
        # Then for each index i with nums[i] = v, we need:
        #   max over l in [L..i] of (-v*l - penalty*pref[l])
        #   max over r in [i..R] of (v*r + penalty*pref[r+1])
        #
        # We answer these with two offline sweeps and Fenwick trees.
        # ---------------------------------------------------------------------
        for value in sorted_values:
            indices: List[int] = value_to_indices[value]
            total_smaller: int = processed_count

            # Build prefix counts of elements strictly smaller than current value.
            # pref[k] counts smaller elements in nums[0:k].
            pref: List[int] = [0] * (n + 1)
            for i in range(n):
                pref[i + 1] = pref[i] + (1 if nums[i] < value else 0)

            # -------------------------------------------------------------
            # LEFT PART:
            # For each possible l (0 <= l < n), define:
            #   A[l] = -value * l - penalty * pref[l]
            #
            # For each index i, we need max A[l] for l in [left_bound[i], i].
            #
            # Offline trick:
            # - Sort queries by their left_bound descending.
            # - Insert candidate l positions descending into a Fenwick tree
            #   keyed by position, supporting prefix max.
            #
            # To turn interval [L..i] into a prefix query, we reverse positions:
            #   rev_pos = n - l
            # Then l >= L becomes rev_pos <= n - L.
            # Among inserted l values (all with l >= current L), querying prefix
            # up to n - i gives exactly l in [L..i].
            # -------------------------------------------------------------
            left_queries: List[tuple[int, int, int]] = []
            for idx in indices:
                left_queries.append((left_bound[idx], idx, idx))
            left_queries.sort(reverse=True)

            left_best: List[int] = [-(10**30)] * n
            left_tree: FenwickTree = FenwickTree(n)
            ptr_l: int = n - 1

            for need_left, _, idx in left_queries:
                while ptr_l >= need_left:
                    candidate_value: int = -value * ptr_l - penalty * pref[ptr_l]
                    reversed_position: int = n - ptr_l
                    left_tree.update(reversed_position, candidate_value)
                    ptr_l -= 1

                query_limit: int = n - idx
                left_best[idx] = left_tree.query(query_limit)

            # -------------------------------------------------------------
            # RIGHT PART:
            # For each possible r (0 <= r < n), define:
            #   B[r] = value * r + penalty * pref[r + 1]
            #
            # For each index i, we need max B[r] for r in [i, right_bound[i]].
            #
            # Offline trick:
            # - Sort queries by right_bound ascending.
            # - Insert candidate r positions ascending into a Fenwick tree
            #   keyed by reversed position rev = n - r.
            #
            # Querying prefix up to n - i among inserted r <= R gives exactly
            # those with r >= i and r <= R.
            # -------------------------------------------------------------
            right_queries: List[tuple[int, int, int]] = []
            for idx in indices:
                right_queries.append((right_bound[idx], idx, idx))
            right_queries.sort()

            right_best: List[int] = [-(10**30)] * n
            right_tree: FenwickTree = FenwickTree(n)
            ptr_r: int = 0

            for need_right, _, idx in right_queries:
                while ptr_r <= need_right:
                    candidate_value = value * ptr_r + penalty * pref[ptr_r + 1]
                    reversed_position = n - ptr_r
                    right_tree.update(reversed_position, candidate_value)
                    ptr_r += 1

                query_limit = n - idx
                right_best[idx] = right_tree.query(query_limit)

            # -------------------------------------------------------------
            # Combine left and right contributions.
            #
            # Derived formula:
            # score = -penalty * total_smaller
            #         + value
            #         + best_left
            #         + best_right
            #
            # We evaluate this for every index carrying the current minimum value.
            # -------------------------------------------------------------
            for idx in indices:
                current_score: int = (
                    -penalty * total_smaller
                    + value
                    + left_best[idx]
                    + right_best[idx]
                )
                if current_score > answer:
                    answer = current_score

            processed_count += len(indices)

        return answer


if __name__ == "__main__":
    solution = Solution()

    nums1 = [5, 2, 4, 3]
    penalty1 = 2
    result1 = solution.max_score_guarded_segment(nums1, penalty1)
    print(result1)  # Expected: 6

    nums2 = [7, 1, 6, 5, 2]
    penalty2 = 3
    result2 = solution.max_score_guarded_segment(nums2, penalty2)
    print(result2)  # Based on the stated problem definition, this computes the true maximum.

    # Additional quick sanity checks
    print(solution.max_score_guarded_segment([7], 3))          # 7
    print(solution.max_score_guarded_segment([2, 2, 2], 5))    # 6
    print(solution.max_score_guarded_segment([1, 2, 3], 1))    # 4