"""
Title: Minimum Scanner Range for Warehouse Aisle Labels

Problem Description:
A warehouse has several aisle labels placed along a straight corridor. The positions
of the labels are given in a sorted integer array `labels`, where `labels[i]` is the
position of the i-th label on the corridor. You are also given an integer `k`, the
number of handheld scanners available. Each scanner can be placed at any real-valued
position and can read every label whose distance from the scanner is at most `R`,
where `R` is the scanner's reading range. All scanners use the same range.

Your task is to find the minimum integer value `R` such that all labels can be covered
using at most `k` scanners.

A scanner covers a continuous interval [x - R, x + R], so once a scanner is placed,
it may cover multiple nearby labels. You may choose scanner positions optimally.
Return the smallest possible integer `R`.

This problem should be solved efficiently for large inputs. A common approach is to
binary search the answer `R` and greedily check whether all labels can be covered
with at most `k` scanners.

Constraints:
- 1 <= labels.length <= 100000
- 0 <= labels[i] <= 1000000000
- labels is sorted in non-decreasing order
- 1 <= k <= labels.length
- Return an integer answer

Example 1:
Input: labels = [1, 2, 8, 12, 17], k = 2
Output: 4

Example 2:
Input: labels = [0, 5, 6, 7, 20], k = 3
Output: 1
"""

from typing import List


class Solution:
    def can_cover(self, labels: List[int], k: int, radius: int) -> bool:
        """
        Check whether all labels can be covered using at most k scanners
        when every scanner has reading range `radius`.

        Args:
            labels: Sorted list of label positions along the corridor.
            k: Maximum number of scanners available.
            radius: Candidate scanner range being tested.

        Returns:
            True if all labels can be covered with at most k scanners, else False.

        Time complexity:
            O(n), where n is the number of labels.

        Space complexity:
            O(1), ignoring input storage.
        """
        # This variable counts how many scanners we have used so far.
        scanners_used: int = 0

        # This index walks through the labels from left to right.
        i: int = 0
        n: int = len(labels)

        # We greedily cover labels from left to right.
        #
        # Why greedy works:
        # - Suppose labels[i] is the leftmost uncovered label.
        # - To cover it while also maximizing how far right we can reach,
        #   the best scanner placement is centered at labels[i] + radius.
        # - That scanner then covers the interval:
        #       [labels[i], labels[i] + 2 * radius]
        #   because its left edge is (labels[i] + radius - radius) = labels[i]
        #   and its right edge is (labels[i] + radius + radius) = labels[i] + 2R.
        # - Any other placement that still covers labels[i] cannot extend farther
        #   to the right than this.
        #
        # Therefore, each time we encounter the next uncovered label, we place one
        # scanner in the best possible way and skip every label it covers.
        while i < n:
            scanners_used += 1

            # If we already used more than k scanners, this radius is not enough.
            if scanners_used > k:
                return False

            # The current scanner starts by covering the leftmost uncovered label.
            # We place it at labels[i] + radius, so the farthest right label it can
            # cover is any label <= labels[i] + 2 * radius.
            cover_right: int = labels[i] + 2 * radius

            # Move forward while labels are still inside this scanner's coverage.
            while i < n and labels[i] <= cover_right:
                i += 1

        # If we finished all labels using at most k scanners, the radius works.
        return True

    def minScannerRange(self, labels: List[int], k: int) -> int:
        """
        Find the minimum integer scanner range needed to cover all labels
        using at most k scanners.

        Args:
            labels: Sorted list of label positions along the corridor.
            k: Number of scanners available.

        Returns:
            The smallest integer radius R such that all labels are covered.

        Time complexity:
            O(n log D), where n is the number of labels and
            D = labels[-1] - labels[0].

        Space complexity:
            O(1), ignoring input storage.
        """
        # Edge case:
        # If we have at least as many scanners as labels, we can place one scanner
        # directly on each label, so radius 0 is enough.
        if k >= len(labels):
            return 0

        # Binary search over the answer.
        #
        # Why binary search is valid:
        # - If a radius R works, then any larger radius also works.
        # - If a radius R does not work, then any smaller radius also does not work.
        # This monotonic property makes binary search appropriate.
        #
        # Lower bound:
        # - 0 is the smallest possible integer radius.
        #
        # Upper bound:
        # - labels[-1] - labels[0] is always enough, because one scanner with that
        #   radius can certainly cover the entire span if placed appropriately.
        left: int = 0
        right: int = labels[-1] - labels[0]

        # Standard binary search for the first feasible radius.
        while left < right:
            mid: int = (left + right) // 2

            # Test whether this candidate radius is sufficient.
            if self.can_cover(labels, k, mid):
                # If it works, try to find an even smaller valid radius.
                right = mid
            else:
                # If it does not work, we must increase the radius.
                left = mid + 1

        # At loop end, left == right and points to the minimum feasible radius.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    labels1: List[int] = [1, 2, 8, 12, 17]
    k1: int = 2
    result1: int = solution.minScannerRange(labels1, k1)
    print("Example 1:")
    print(f"labels = {labels1}, k = {k1}")
    print(f"Minimum scanner range = {result1}")
    print("Expected = 4")
    print()

    # Example 2
    labels2: List[int] = [0, 5, 6, 7, 20]
    k2: int = 3
    result2: int = solution.minScannerRange(labels2, k2)
    print("Example 2:")
    print(f"labels = {labels2}, k = {k2}")
    print(f"Minimum scanner range = {result2}")
    print("Expected = 1")
    print()

    # Additional quick sanity checks
    labels3: List[int] = [5]
    k3: int = 1
    result3: int = solution.minScannerRange(labels3, k3)
    print("Additional Test 1:")
    print(f"labels = {labels3}, k = {k3}")
    print(f"Minimum scanner range = {result3}")
    print("Expected = 0")
    print()

    labels4: List[int] = [1, 10, 20, 30]
    k4: int = 2
    result4: int = solution.minScannerRange(labels4, k4)
    print("Additional Test 2:")
    print(f"labels = {labels4}, k = {k4}")
    print(f"Minimum scanner range = {result4}")