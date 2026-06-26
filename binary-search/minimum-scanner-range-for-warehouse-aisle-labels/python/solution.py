"""
Title: Minimum Scanner Range for Warehouse Aisle Labels
Difficulty: Medium
Topic: Binary Search

Problem Description:
A warehouse is organized as a long straight line of aisles, numbered by their distance
from the entrance. Some aisles contain fixed barcode scanners, and some aisles contain
labels that must be readable by at least one scanner.

You are given two integer arrays: scanners and labels. scanners[i] is the position of
the i-th scanner, and labels[j] is the position of the j-th label. A scanner with range
r can read every label whose position is within distance r from that scanner. Your task
is to find the minimum integer range r such that every label is readable by at least one
scanner.

Positions may be unsorted and may include duplicates. A scanner and a label can appear
at the same position. You must return the smallest possible range that covers all labels.

A common interview approach is to sort the positions and use binary search on the answer.
For a candidate range r, determine whether all labels can be covered efficiently.

Constraints:
- 1 <= scanners.length, labels.length <= 2 * 10^5
- 0 <= scanners[i], labels[j] <= 10^9
- The answer fits in a 32-bit signed integer.

Example 1:
Input: scanners = [2, 10], labels = [1, 5, 11]
Output: 3
Explanation: With range 3, scanner 2 covers labels at 1 and 5, and scanner 10 covers
label 11. Range 2 is not enough because label 5 would be uncovered.

Example 2:
Input: scanners = [15, 4, 20], labels = [3, 8, 14, 21]
Output: 4
Explanation: After sorting, the nearest scanner distances for the labels are 1, 4, 1,
and 1. Therefore the minimum range that covers every label is 4.
"""

from typing import List


class Solution:
    def can_cover_all_labels(
        self,
        scanners: List[int],
        labels: List[int],
        radius: int,
    ) -> bool:
        """
        Check whether every label can be covered by at least one scanner
        using the given scanner range.

        Args:
            scanners: Sorted list of scanner positions.
            labels: Sorted list of label positions.
            radius: Candidate scanner range to test.

        Returns:
            True if all labels are covered, otherwise False.

        Time complexity:
            O(len(scanners) + len(labels))

        Space complexity:
            O(1)
        """
        # We use a classic two-pointer sweep over the sorted arrays.
        #
        # Why this works:
        # - Each scanner at position s covers the interval [s - radius, s + radius].
        # - Since both arrays are sorted, we can move from left to right and greedily
        #   verify whether each label falls inside the coverage interval of the current
        #   scanner or some later scanner.
        #
        # Pointer meanings:
        # - scanner_index points to the current scanner we are considering.
        # - label_index points to the next label that still needs to be checked.
        scanner_index: int = 0
        label_index: int = 0

        # Continue until either:
        # - all labels are covered, or
        # - we run out of scanners.
        while scanner_index < len(scanners) and label_index < len(labels):
            scanner_position: int = scanners[scanner_index]

            # Compute the full coverage interval of the current scanner.
            left_reach: int = scanner_position - radius
            right_reach: int = scanner_position + radius

            # If the current label is to the LEFT of this scanner's coverage,
            # then this label cannot be covered by this scanner.
            #
            # Important reasoning:
            # Because scanners are sorted, every future scanner is even farther to the right.
            # That means future scanners will have left boundaries that are >= this scanner's
            # left boundary, so they also cannot reach this too-far-left label.
            #
            # Therefore, if labels[label_index] < left_reach, coverage is impossible.
            if labels[label_index] < left_reach:
                return False

            # If the current label lies inside this scanner's coverage interval,
            # then this scanner may cover not just one label, but potentially many
            # consecutive labels to the right.
            #
            # We advance label_index while labels remain within [left_reach, right_reach].
            while label_index < len(labels) and labels[label_index] <= right_reach:
                label_index += 1

            # Move to the next scanner after using the current one as much as possible.
            scanner_index += 1

        # If label_index reached the end, then every label was covered.
        # Otherwise, some labels remain uncovered.
        return label_index == len(labels)

    def minimum_scanner_range(self, scanners: List[int], labels: List[int]) -> int:
        """
        Find the minimum integer scanner range needed so that every label
        is covered by at least one scanner.

        Args:
            scanners: Positions of scanners.
            labels: Positions of labels.

        Returns:
            The smallest integer range that covers all labels.

        Time complexity:
            O((len(scanners) + len(labels)) * log M),
            where M is the search range for the answer.

        Space complexity:
            O(1) extra space beyond sorting if the sort implementation details
            are ignored; practically, Python's sort uses additional internal space.
        """
        # Step 1: Sort both arrays.
        #
        # Why sorting is necessary:
        # - The binary search checks a candidate answer many times.
        # - For each candidate range, we want an efficient linear-time coverage test.
        # - A left-to-right sweep only works correctly when positions are sorted.
        scanners.sort()
        labels.sort()

        # Step 2: Establish binary search boundaries for the answer.
        #
        # Lower bound:
        # - The minimum possible range is 0.
        #
        # Upper bound:
        # - In the worst case, one scanner may need to cover a label very far away.
        # - A safe upper bound is the maximum possible distance between any label
        #   and any scanner endpoint:
        #       max(
        #           abs(labels[0] - scanners[-1]),
        #           abs(labels[-1] - scanners[0])
        #       )
        #
        # This safely contains the true answer.
        left: int = 0
        right: int = max(
            abs(labels[0] - scanners[-1]),
            abs(labels[-1] - scanners[0]),
        )

        # Step 3: Binary search for the smallest radius that works.
        #
        # Binary search invariant:
        # - Any radius < answer is invalid.
        # - Any radius >= answer is valid.
        #
        # So we search for the first valid radius.
        while left < right:
            mid: int = (left + right) // 2

            # Test whether this candidate radius is enough.
            if self.can_cover_all_labels(scanners, labels, mid):
                # mid works, so the answer is <= mid.
                # Keep searching the left half to find a smaller valid radius.
                right = mid
            else:
                # mid does not work, so the answer must be larger.
                left = mid + 1

        # When the loop ends, left == right and points to the minimum valid radius.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    scanners_1: List[int] = [2, 10]
    labels_1: List[int] = [1, 5, 11]
    result_1: int = solution.minimum_scanner_range(scanners_1, labels_1)
    print("Example 1 Result:", result_1)  # Expected: 3

    # Example 2
    scanners_2: List[int] = [15, 4, 20]
    labels_2: List[int] = [3, 8, 14, 21]
    result_2: int = solution.minimum_scanner_range(scanners_2, labels_2)
    print("Example 2 Result:", result_2)  # Expected: 4

    # Additional quick sanity checks
    scanners_3: List[int] = [5]
    labels_3: List[int] = [5, 5, 5]
    result_3: int = solution.minimum_scanner_range(scanners_3, labels_3)
    print("Additional Test 1 Result:", result_3)  # Expected: 0

    scanners_4: List[int] = [1, 100]
    labels_4: List[int] = [50]
    result_4: int = solution.minimum_scanner_range(scanners_4, labels_4)
    print("Additional Test 2 Result:", result_4)  # Expected: 49