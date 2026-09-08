"""
Title: Longest Study Window With Limited Difficult Problems

Problem Description:
You are given an array problems where problems[i] is the difficulty rating of the i-th
practice problem in the order a student solved them. You are also given two integers
threshold and k. A problem is considered difficult if its difficulty rating is greater
than or equal to threshold.

Find the length of the longest contiguous window of solved problems that contains at
most k difficult problems.

In other words, you may choose any consecutive segment of the array, but inside that
segment the number of values greater than or equal to threshold must not exceed k.
Return the maximum possible length of such a segment.

This models a realistic interview-style scenario where you want to identify the longest
sustained practice streak that does not contain too many high-difficulty interruptions.

Constraints:
- 1 <= problems.length <= 200000
- 0 <= problems[i] <= 1000000000
- 0 <= k <= problems.length
- 0 <= threshold <= 1000000000

Examples:
1)
Input: problems = [2, 7, 3, 9, 4, 8, 1], threshold = 7, k = 2
Output: 5

2)
Input: problems = [10, 1, 1, 10, 1, 10, 1, 1], threshold = 10, k = 1
Output: 4
"""

from typing import List


class Solution:
    def longest_study_window(self, problems: List[int], threshold: int, k: int) -> int:
        """
        Find the maximum length of a contiguous subarray containing at most k difficult problems.

        A problem is difficult if its value is greater than or equal to threshold.

        Args:
            problems: List of difficulty ratings in solved order.
            threshold: Minimum value that makes a problem difficult.
            k: Maximum allowed number of difficult problems inside the chosen window.

        Returns:
            The length of the longest valid contiguous window.

        Time complexity:
            O(n), where n is the length of problems.
            Each element is processed at most twice: once by the right pointer and
            once by the left pointer.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # We use the classic "sliding window" technique because:
        # 1. We need a contiguous segment.
        # 2. We need to maintain a condition on that segment:
        #    "number of difficult problems <= k".
        # 3. We want the longest such segment efficiently.
        #
        # The main idea:
        # - Expand the window to the right one element at a time.
        # - Track how many difficult problems are currently inside the window.
        # - If the window becomes invalid (too many difficult problems),
        #   move the left side forward until the window becomes valid again.
        # - At every step where the window is valid, update the best length seen so far.
        #
        # This works in linear time because each pointer only moves forward.

        left: int = 0
        difficult_count: int = 0
        best_length: int = 0

        # Iterate with "right" as the end of the current window.
        for right in range(len(problems)):
            # Step 1: Include problems[right] into the current window.
            #
            # If this new problem is difficult, increase our count.
            # We define "difficult" exactly as value >= threshold.
            if problems[right] >= threshold:
                difficult_count += 1

            # Step 2: If the window is invalid, shrink it from the left.
            #
            # The window is invalid when it contains more than k difficult problems.
            # While invalid, keep moving "left" forward.
            #
            # Important detail:
            # When removing problems[left] from the window, if that element was difficult,
            # we must decrease difficult_count because it is no longer inside the window.
            while difficult_count > k:
                if problems[left] >= threshold:
                    difficult_count -= 1
                left += 1

            # Step 3: At this point, the window [left, right] is guaranteed valid.
            # So we can compute its length and compare it with the best answer so far.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # After scanning the full array, best_length stores the maximum valid window size.
        return best_length

    def longestStudyWindow(self, problems: List[int], threshold: int, k: int) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            problems: List of difficulty ratings in solved order.
            threshold: Minimum value that makes a problem difficult.
            k: Maximum allowed number of difficult problems inside the chosen window.

        Returns:
            The length of the longest valid contiguous window.

        Time complexity:
            O(n), where n is the length of problems.

        Space complexity:
            O(1).
        """
        return self.longest_study_window(problems, threshold, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    problems_1: List[int] = [2, 7, 3, 9, 4, 8, 1]
    threshold_1: int = 7
    k_1: int = 2
    result_1: int = solution.longest_study_window(problems_1, threshold_1, k_1)
    print("Example 1 Result:", result_1)  # Expected: 5

    # Example 2
    problems_2: List[int] = [10, 1, 1, 10, 1, 10, 1, 1]
    threshold_2: int = 10
    k_2: int = 1
    result_2: int = solution.longest_study_window(problems_2, threshold_2, k_2)
    print("Example 2 Result:", result_2)  # Expected: 4

    # Additional quick checks
    problems_3: List[int] = [1, 2, 3, 4]
    threshold_3: int = 10
    k_3: int = 0
    result_3: int = solution.longest_study_window(problems_3, threshold_3, k_3)
    print("Additional Check 1:", result_3)  # Expected: 4

    problems_4: List[int] = [10, 10, 10]
    threshold_4: int = 10
    k_4: int = 0
    result_4: int = solution.longest_study_window(problems_4, threshold_4, k_4)
    print("Additional Check 2:", result_4)  # Expected: 0