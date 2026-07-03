"""
Title: Longest Annotation Span With Limited Reviewer Handoffs

Problem Description:
A document review platform records which reviewer handled each consecutive annotation
in a long editing session. You are given an array reviewers where reviewers[i] is the
reviewer ID responsible for the i-th annotation, in chronological order.

A handoff happens between two adjacent annotations when their reviewer IDs are different.

Your task is to find the length of the longest contiguous span of annotations that
contains at most k handoffs. In other words, within the chosen subarray, count how
many indices j satisfy reviewers[j] != reviewers[j - 1] for adjacent elements inside
that subarray. That count must be less than or equal to k.

Return the maximum possible length of such a contiguous span.

This problem models finding the longest stable review segment where ownership changes
are limited. A span with repeated reviewer IDs may still contain handoffs if the
reviewer changes and later changes back.

Constraints:
- 1 <= reviewers.length <= 200000
- 1 <= reviewers[i] <= 1000000000
- 0 <= k < reviewers.length

Example 1:
Input: reviewers = [5,5,2,2,2,7,7,2], k = 2
Output: 7

Example 2:
Input: reviewers = [1,3,3,4,4,4,2,2,5], k = 1
Output: 5
"""

from typing import List


class Solution:
    def longest_annotation_span(self, reviewers: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous subarray containing at most k handoffs.

        A handoff is counted for each adjacent pair inside the current window where
        the reviewer ID changes.

        Args:
            reviewers: List of reviewer IDs in chronological order.
            k: Maximum allowed number of handoffs inside the chosen span.

        Returns:
            The length of the longest contiguous span with at most k handoffs.

        Time complexity:
            O(n), where n is the length of reviewers. Each pointer moves at most n times.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        n: int = len(reviewers)

        # The sliding window is represented by [left, right], inclusive.
        # We will expand 'right' one step at a time and maintain how many handoffs
        # exist inside the current window.
        left: int = 0

        # This stores the number of adjacent changes currently inside the window.
        #
        # Important detail:
        # For a window [left, right], the handoffs are checked at positions:
        # left+1, left+2, ..., right
        # where a handoff exists if reviewers[i] != reviewers[i - 1].
        handoffs: int = 0

        # This keeps track of the best valid window length seen so far.
        best: int = 0

        # Move the right boundary from left to right across the array.
        for right in range(n):
            # When we add reviewers[right] into the window, a NEW adjacent pair is formed
            # only if right > left? Actually, the new pair to consider is (right - 1, right),
            # and it belongs to the window whenever right > 0 and both indices are inside.
            #
            # Since right is always inside after expansion, we only need to check whether
            # right - 1 exists. If reviewers[right] differs from reviewers[right - 1],
            # then the total handoff count inside the window increases by 1.
            #
            # This is correct because every adjacent pair is added exactly once:
            # when its right endpoint enters the window.
            if right > 0 and reviewers[right] != reviewers[right - 1]:
                handoffs += 1

            # If the window now has too many handoffs, we must shrink it from the left
            # until it becomes valid again.
            #
            # Why does shrinking from the left work?
            # Because we want the longest contiguous valid window ending at 'right'.
            # If the current window is invalid, the only way to fix it while keeping
            # 'right' fixed is to move 'left' forward.
            while handoffs > k:
                # Before incrementing left, we are removing reviewers[left] from the window.
                # The only adjacent pair that disappears because of this removal is
                # (left, left + 1), provided left + 1 is still within the current window.
                #
                # If reviewers[left] != reviewers[left + 1], that pair contributed one handoff,
                # so we must subtract it.
                if left + 1 <= right and reviewers[left] != reviewers[left + 1]:
                    handoffs -= 1

                # Actually remove the leftmost element by moving the boundary forward.
                left += 1

            # At this point, the window [left, right] is guaranteed to have at most k handoffs.
            # So it is a valid candidate answer.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best

    def solve(self, reviewers: List[int], k: int) -> int:
        """
        Wrapper method that calls the main algorithm.

        Args:
            reviewers: List of reviewer IDs.
            k: Maximum allowed handoffs.

        Returns:
            Length of the longest valid contiguous span.

        Time complexity:
            O(n), where n is the number of annotations.

        Space complexity:
            O(1).
        """
        return self.longest_annotation_span(reviewers, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    reviewers1: List[int] = [5, 5, 2, 2, 2, 7, 7, 2]
    k1: int = 2
    result1: int = solution.solve(reviewers1, k1)
    print("Example 1:")
    print("reviewers =", reviewers1)
    print("k =", k1)
    print("Output =", result1)
    print("Expected = 7")
    print()

    # Example 2
    reviewers2: List[int] = [1, 3, 3, 4, 4, 4, 2, 2, 5]
    k2: int = 1
    result2: int = solution.solve(reviewers2, k2)
    print("Example 2:")
    print("reviewers =", reviewers2)
    print("k =", k2)
    print("Output =", result2)
    print("Expected = 5")
    print()

    # Additional small sanity checks for beginners
    reviewers3: List[int] = [9]
    k3: int = 0
    result3: int = solution.solve(reviewers3, k3)
    print("Sanity Check 1:")
    print("reviewers =", reviewers3)
    print("k =", k3)
    print("Output =", result3)
    print("Expected = 1")
    print()

    reviewers4: List[int] = [1, 2, 3, 4]
    k4: int = 0
    result4: int = solution.solve(reviewers4, k4)
    print("Sanity Check 2:")
    print("reviewers =", reviewers4)
    print("k =", k4)
    print("Output =", result4)
    print("Expected = 1")
    print()

    reviewers5: List[int] = [7, 7, 7, 7]
    k5: int = 0
    result5: int = solution.solve(reviewers5, k5)
    print("Sanity Check 3:")
    print("reviewers =", reviewers5)
    print("k =", k5)
    print("Output =", result5)
    print("Expected = 4")