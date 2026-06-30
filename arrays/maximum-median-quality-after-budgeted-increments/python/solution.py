"""
Title: Maximum Median Quality After Budgeted Increments

Problem Description:
You are given an integer array quality where quality[i] is the current quality score
of the i-th manufactured part. You are also given an integer budget representing the
total number of increment operations available. In one operation, you may choose any
single part and increase its quality by 1. You may distribute the operations across
the array in any way.

Your goal is to maximize the median quality score of the array after using at most
budget operations. The median is defined as the middle element after sorting the array
in non-decreasing order. For an array of length n, use the element at index n // 2
in 0-based indexing after sorting. For example, when n = 5, the median is the 3rd
element; when n = 6, use the 4th element after sorting.

You may reorder the array only for the purpose of evaluating the median; the actual
increment operations can be applied to any indices. Return the maximum possible median
value.

Constraints:
- 1 <= quality.length <= 200000
- 1 <= quality[i] <= 1000000000
- 0 <= budget <= 1000000000000
"""

from typing import List


class Solution:
    def max_median_quality(self, quality: List[int], budget: int) -> int:
        """
        Compute the maximum possible median after at most `budget` increment operations.

        The key idea is:
        1. Sort the array.
        2. The median position is fixed at index n // 2 after sorting.
        3. To make the median at least some target value X, every element from the
           median index to the end that is below X must be raised up to X.
        4. This gives a monotonic feasibility check, so we can binary search the answer.

        Args:
            quality: List of current quality scores.
            budget: Total number of +1 operations available.

        Returns:
            The maximum achievable median value.

        Time complexity:
            O(n log M), where:
            - n is the length of the array
            - M is the search range of the answer (roughly budget + max value)

        Space complexity:
            O(n) due to sorting a copy of the input.
        """
        # Step 1: Sort the array because the median is defined on the sorted order.
        # Once sorted, the median position is fixed at index n // 2.
        arr: List[int] = sorted(quality)
        n: int = len(arr)
        mid: int = n // 2

        # Step 2: Define the search boundaries for binary search.
        #
        # Lower bound:
        # - At minimum, we can always keep the current median as-is.
        #
        # Upper bound:
        # - In the absolute best case, if we spent all budget on the median alone,
        #   the median could not exceed arr[mid] + budget.
        # - This is a safe upper bound for binary search.
        left: int = arr[mid]
        right: int = arr[mid] + budget

        # Step 3: Binary search for the maximum feasible median value.
        #
        # We use the "search on answer" pattern:
        # - If a target median is feasible, try larger.
        # - Otherwise, try smaller.
        while left < right:
            # We bias upward with +1 so that the loop converges correctly
            # when searching for the maximum feasible value.
            target: int = (left + right + 1) // 2

            # Check whether we can make the median at least `target`
            # using at most `budget` increments.
            if self._can_make_median(arr, mid, target, budget):
                left = target
            else:
                right = target - 1

        # At loop end, left == right and is the maximum feasible median.
        return left

    def _can_make_median(
        self,
        arr: List[int],
        mid: int,
        target: int,
        budget: int,
    ) -> bool:
        """
        Check whether it is possible to make the sorted-array median at least `target`
        using at most `budget` increments.

        Important reasoning:
        - The median is the element at index `mid` after sorting.
        - To ensure that element is at least `target`, we need enough elements in the
          upper half (from `mid` to end) to be at least `target`.
        - Since the array is sorted and we can only increment values, the cheapest way
          is to raise every element in arr[mid:] that is below `target` up to `target`.
        - If the total required increments is <= budget, then `target` is feasible.

        Args:
            arr: Sorted array of quality scores.
            mid: Median index in the sorted array.
            target: Candidate median value to test.
            budget: Maximum allowed total increments.

        Returns:
            True if `target` is achievable, otherwise False.

        Time complexity:
            O(n - mid), which is O(n) in the worst case.

        Space complexity:
            O(1) extra space.
        """
        required: int = 0

        # We only care about the median and everything to its right.
        #
        # Why?
        # - Elements to the left of the median do not help increase the median.
        # - The sorted median depends on the upper half being large enough.
        # - If any value in arr[mid:] is below target, it may become the blocking
        #   value that prevents the median from reaching target.
        for i in range(mid, len(arr)):
            if arr[i] < target:
                required += target - arr[i]

                # Early stopping:
                # As soon as we exceed the budget, there is no need to continue.
                if required > budget:
                    return False

        return required <= budget


if __name__ == "__main__":
    solution = Solution()

    # Sample 1
    # Correct interpretation under the standard median-maximization rule:
    # sorted = [1, 3, 5], budget = 4
    # To make median at least 6:
    # raise 3 -> 6 (3 ops), 5 -> 6 (1 op), total = 4, feasible
    # To make median at least 7:
    # raise 3 -> 7 (4 ops), 5 -> 7 (2 ops), total = 6, not feasible
    # Therefore the correct maximum median is 6.
    quality1: List[int] = [1, 3, 5]
    budget1: int = 4
    result1: int = solution.max_median_quality(quality1, budget1)
    print(f"Input: quality = {quality1}, budget = {budget1}")
    print(f"Output: {result1}")
    print("Expected (by correct standard definition): 6")
    print()

    # Sample 2
    # sorted = [2, 2, 8, 9, 9], budget = 6
    # To make median at least 10:
    # raise 8 -> 10 (2), 9 -> 10 (1), 9 -> 10 (1), total = 4, feasible
    # To make median at least 11:
    # raise 8 -> 11 (3), 9 -> 11 (2), 9 -> 11 (2), total = 7, not feasible
    # Therefore answer is 10.
    quality2: List[int] = [2, 2, 8, 9, 9]
    budget2: int = 6
    result2: int = solution.max_median_quality(quality2, budget2)
    print(f"Input: quality = {quality2}, budget = {budget2}")
    print(f"Output: {result2}")
    print("Expected: 10")