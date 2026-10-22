"""
Title: Find the Last Passed Practice Exam
Difficulty: Easy
Topic: Binary Search

Problem Description:
You are given a sorted array `scores` where `scores[i]` is the score of the `i`-th
practice exam taken by a student. The array is sorted in non-decreasing order, so
later values may be equal to earlier ones. You are also given an integer `passMark`,
representing the minimum score required to pass.

Your task is to return the index of the last exam whose score is less than or equal
to `passMark` if the goal is to find the final exam result that still counts as
passing under a given cutoff. If no exam score is less than or equal to `passMark`,
return `-1`.

Because the scores are already sorted, the expected solution should use binary search
instead of scanning the entire array. This makes the solution efficient even when the
number of exams is large.

Constraints:
- 1 <= scores.length <= 100000
- 0 <= scores[i] <= 10^9
- scores is sorted in non-decreasing order
- 0 <= passMark <= 10^9

Example 1:
Input: scores = [35, 42, 42, 58, 71, 88], passMark = 58
Output: 3
Explanation: The last score that is less than or equal to 58 is 58, which appears at index 3.

Example 2:
Input: scores = [60, 75, 80], passMark = 50
Output: -1
Explanation: Every score is greater than 50, so there is no valid passing exam under this cutoff.
"""

from typing import List


class Solution:
    def find_last_passed_exam(self, scores: List[int], passMark: int) -> int:
        """
        Find the index of the last score that is less than or equal to passMark.

        This method uses binary search on the sorted input list to efficiently locate
        the rightmost valid index whose value does not exceed the given cutoff.

        Args:
            scores: A list of exam scores sorted in non-decreasing order.
            passMark: The maximum allowed score for the search condition.

        Returns:
            The index of the last score less than or equal to passMark, or -1 if no
            such score exists.

        Time complexity:
            O(log n), where n is the number of scores.

        Space complexity:
            O(1), because only a constant amount of extra space is used.
        """
        # We will search within the full range of valid indices in the array.
        # `left` marks the beginning of the current search space.
        # `right` marks the end of the current search space.
        left: int = 0
        right: int = len(scores) - 1

        # This variable stores the best answer found so far.
        # We start with -1 because that is the required result when no score
        # is less than or equal to passMark.
        answer: int = -1

        # Continue searching while there is still a valid range to inspect.
        # The condition `left <= right` means there is at least one candidate index left.
        while left <= right:
            # Compute the middle index safely.
            # In Python, overflow is not a concern, but this style is standard and clear.
            mid: int = left + (right - left) // 2

            # If the middle score is less than or equal to passMark, then:
            # 1. This index is a valid candidate answer.
            # 2. There may still be another valid score farther to the right.
            # So we record `mid` and continue searching the right half.
            if scores[mid] <= passMark:
                answer = mid
                left = mid + 1
            else:
                # If the middle score is greater than passMark, then this index
                # cannot be part of the answer, and neither can anything to its right,
                # because the array is sorted in non-decreasing order.
                # Therefore, we discard the right half and continue on the left half.
                right = mid - 1

        # After the loop ends, `answer` contains the rightmost valid index found,
        # or -1 if no valid score existed.
        return answer


def run_example(scores: List[int], passMark: int) -> None:
    """
    Run one example test case and print the result.

    Args:
        scores: A sorted list of exam scores.
        passMark: The cutoff value used for the search.

    Returns:
        None

    Time complexity:
        O(log n), due to the binary search call.

    Space complexity:
        O(1), excluding input storage.
    """
    solution = Solution()
    result = solution.find_last_passed_exam(scores, passMark)
    print(f"scores = {scores}, passMark = {passMark} -> result = {result}")


if __name__ == "__main__":
    # Example 1 from the problem statement:
    # scores = [35, 42, 42, 58, 71, 88], passMark = 58
    # The last score <= 58 is 58 at index 3, so expected output is 3.
    run_example([35, 42, 42, 58, 71, 88], 58)

    # Example 2 from the problem statement:
    # scores = [60, 75, 80], passMark = 50
    # No score is <= 50, so expected output is -1.
    run_example([60, 75, 80], 50)

    # Additional beginner-friendly checks:
    run_example([10], 10)          # Expected: 0
    run_example([10], 5)           # Expected: -1
    run_example([1, 2, 2, 2, 3], 2)  # Expected: 3