"""
Title: Longest Shipping Lane With Limited Hazard Labels

Problem Description:
A logistics company records the hazard label attached to each package loaded onto a
conveyor belt. The labels are given as an array of strings `labels`, where `labels[i]`
is the hazard category of the `i`-th package in loading order. For safety, a supervisor
wants to inspect the longest contiguous block of packages such that the block contains
at most `k` distinct hazard categories.

Your task is to return the length of the longest contiguous subarray of `labels` that
contains no more than `k` distinct values.

This models a real monitoring problem: when too many hazard categories appear together,
the inspection procedure becomes too complex, so the company wants the largest continuous
stretch that still stays within the allowed variety.

A contiguous block means you may only choose packages that appear next to each other in
the original order.

Constraints:
- 1 <= labels.length <= 100000
- 1 <= labels[i].length <= 20
- labels[i] consists of uppercase English letters, digits, or underscores
- 1 <= k <= labels.length

Example 1:
Input: labels = ["FLAMMABLE", "CORROSIVE", "FLAMMABLE", "TOXIC", "CORROSIVE", "CORROSIVE"], k = 2
Output: 3

Example 2:
Input: labels = ["A", "A", "B", "B", "C", "B", "B", "A"], k = 2
Output: 5
"""

from typing import Dict, List


class Solution:
    def longest_shipping_lane(self, labels: List[str], k: int) -> int:
        """
        Return the length of the longest contiguous subarray containing at most k distinct labels.

        Args:
            labels: List of hazard label strings in conveyor-belt order.
            k: Maximum number of distinct hazard categories allowed in the window.

        Returns:
            The maximum length of a contiguous block with at most k distinct labels.

        Time Complexity:
            O(n), where n is the number of labels.
            Each label is added to the sliding window once and removed at most once.

        Space Complexity:
            O(k) on average for the frequency map of labels currently in the window.
            In the worst case, it can be O(n) if k is as large as n.
        """
        # This dictionary stores how many times each label appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is ["A", "B", "A"], then:
        # counts = {"A": 2, "B": 1}
        #
        # Why do we need counts instead of just a set?
        # Because when we move the left side of the window forward,
        # we need to know whether a label still exists somewhere else
        # in the window. A set alone cannot tell us that safely.
        counts: Dict[str, int] = {}

        # `left` marks the beginning of the current window.
        # The window will always be labels[left:right+1].
        left: int = 0

        # This stores the best (maximum) valid window length found so far.
        max_length: int = 0

        # We expand the window one element at a time by moving `right`.
        for right, label in enumerate(labels):
            # Include the new label at position `right` into the window.
            # If the label is not already present, start its count at 0 first.
            counts[label] = counts.get(label, 0) + 1

            # At this point, the window is labels[left:right+1].
            # However, after adding the new label, the number of distinct labels
            # may have become larger than k.
            #
            # If that happens, the window is invalid and we must shrink it
            # from the left until it becomes valid again.
            while len(counts) > k:
                # Identify the label that is leaving the window.
                left_label: str = labels[left]

                # Decrease its frequency because we are removing one occurrence
                # from the left side of the window.
                counts[left_label] -= 1

                # If its count becomes 0, that means this label no longer exists
                # anywhere in the current window, so we remove it from the dictionary.
                #
                # This is important because the number of distinct labels in the
                # window is exactly len(counts).
                if counts[left_label] == 0:
                    del counts[left_label]

                # Move the left boundary one step to the right,
                # making the window smaller.
                left += 1

            # Now the window is guaranteed to be valid:
            # it contains at most k distinct labels.
            #
            # Compute its length:
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer
            # than any valid window we have seen before.
            if current_length > max_length:
                max_length = current_length

        # After processing all positions, max_length holds the answer.
        return max_length

    def length_of_longest_subarray_with_at_most_k_distinct(
        self, labels: List[str], k: int
    ) -> int:
        """
        Wrapper method with a more descriptive generic name.

        Args:
            labels: List of hazard label strings.
            k: Maximum allowed number of distinct labels.

        Returns:
            Length of the longest valid contiguous subarray.

        Time Complexity:
            O(n), where n is the number of labels.

        Space Complexity:
            O(k) on average for the frequency dictionary.
        """
        return self.longest_shipping_lane(labels, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    labels1: List[str] = [
        "FLAMMABLE",
        "CORROSIVE",
        "FLAMMABLE",
        "TOXIC",
        "CORROSIVE",
        "CORROSIVE",
    ]
    k1: int = 2
    result1: int = solution.longest_shipping_lane(labels1, k1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2 from the problem statement.
    labels2: List[str] = ["A", "A", "B", "B", "C", "B", "B", "A"]
    k2: int = 2
    result2: int = solution.longest_shipping_lane(labels2, k2)
    print("Example 2 Result:", result2)  # Expected: 5

    # Additional quick sanity check.
    labels3: List[str] = ["X"]
    k3: int = 1
    result3: int = solution.longest_shipping_lane(labels3, k3)
    print("Additional Test Result:", result3)  # Expected: 1