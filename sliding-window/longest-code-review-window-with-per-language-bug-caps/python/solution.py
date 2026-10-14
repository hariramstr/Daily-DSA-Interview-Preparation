"""
Title: Longest Code Review Window With Per-Language Bug Caps

Problem Description:
You are given a stream of code review findings in chronological order. Each finding
belongs to a programming language and has a bug severity score. The i-th finding is
represented by two arrays: languages[i] is a string such as "cpp", "java", or "py",
and severities[i] is a positive integer. You are also given an integer limit and an
integer budget.

A contiguous review window is considered valid if it satisfies both rules:
1. For every language, the number of findings from that language inside the window
   is at most limit.
2. Let maxSeverity be the maximum severity in the window and minSeverity be the
   minimum severity in the window. Then maxSeverity - minSeverity must be at most
   budget.

Return the length of the longest valid contiguous window.

This problem models a review dashboard that should not become dominated by too many
findings from the same language, while also keeping the severity spread manageable
enough for a single triage session.

Constraints:
- 1 <= languages.length == severities.length <= 200000
- 1 <= severities[i] <= 10^9
- 1 <= limit <= languages.length
- 0 <= budget <= 10^9
- languages[i] consists of lowercase English letters and has length between 1 and 10
"""

from collections import defaultdict, deque
from typing import Deque, Dict, List


class Solution:
    def longest_review_window(
        self,
        languages: List[str],
        severities: List[int],
        limit: int,
        budget: int,
    ) -> int:
        """
        Find the length of the longest contiguous window that satisfies:
        1) Each language appears at most `limit` times.
        2) max(severity) - min(severity) <= `budget`.

        Args:
            languages: List of language names for each finding.
            severities: List of severity values aligned with `languages`.
            limit: Maximum allowed count per language inside a valid window.
            budget: Maximum allowed severity spread inside a valid window.

        Returns:
            The maximum valid window length.

        Time complexity:
            O(n), where n is the number of findings.
            Each index is added and removed from the sliding window at most once,
            and each severity index enters/leaves each deque at most once.

        Space complexity:
            O(n) in the worst case for the monotonic deques, plus O(k) for language
            counts where k is the number of distinct languages in the current window.
        """
        n: int = len(languages)

        # This dictionary stores how many times each language currently appears
        # inside the sliding window [left, right].
        #
        # Example:
        # If the current window contains ["py", "java", "py"], then:
        # counts["py"] == 2
        # counts["java"] == 1
        #
        # We use defaultdict(int) so missing keys automatically start at 0.
        counts: Dict[str, int] = defaultdict(int)

        # We need to efficiently know the minimum and maximum severity inside
        # the current window at all times.
        #
        # A naive approach would scan the whole window every time, which would
        # be too slow for up to 200,000 elements.
        #
        # Instead, we use two monotonic deques storing indices:
        #
        # 1) min_deque:
        #    - severities at stored indices are in increasing order
        #    - front of deque always points to the minimum severity in the window
        #
        # 2) max_deque:
        #    - severities at stored indices are in decreasing order
        #    - front of deque always points to the maximum severity in the window
        #
        # Why store indices instead of values?
        # Because when the left side of the window moves forward, we need to know
        # whether the oldest min/max candidate has fallen out of the window.
        min_deque: Deque[int] = deque()
        max_deque: Deque[int] = deque()

        # `left` is the start of the current sliding window.
        left: int = 0

        # `best` stores the longest valid window length seen so far.
        best: int = 0

        # We expand the window by moving `right` from left to right across the arrays.
        for right in range(n):
            current_language: str = languages[right]
            current_severity: int = severities[right]

            # ---------------------------------------------------------------
            # Step 1: Add the new element at index `right` into the window.
            # ---------------------------------------------------------------

            # Increase the count for this language because it is now included
            # in the current window.
            counts[current_language] += 1

            # ---------------------------------------------------------------
            # Step 2: Update the monotonic deque for minimum severity.
            # ---------------------------------------------------------------
            #
            # We want min_deque to keep severities in increasing order.
            # If the new severity is smaller than or equal to the severities
            # at the back, those back elements can never become the minimum
            # for any future window that also includes the new element.
            #
            # So we remove them.
            while min_deque and severities[min_deque[-1]] >= current_severity:
                min_deque.pop()

            # After removing larger/equal elements from the back, append the
            # current index. The deque remains increasing by severity.
            min_deque.append(right)

            # ---------------------------------------------------------------
            # Step 3: Update the monotonic deque for maximum severity.
            # ---------------------------------------------------------------
            #
            # We want max_deque to keep severities in decreasing order.
            # If the new severity is larger than or equal to the severities
            # at the back, those back elements can never become the maximum
            # for any future window that also includes the new element.
            #
            # So we remove them.
            while max_deque and severities[max_deque[-1]] <= current_severity:
                max_deque.pop()

            # Append the current index. The deque remains decreasing by severity.
            max_deque.append(right)

            # ---------------------------------------------------------------
            # Step 4: Shrink the window from the left until it becomes valid.
            # ---------------------------------------------------------------
            #
            # The window is invalid if either:
            # A) The count of the newly added language exceeds `limit`.
            #    Why only check the newly added language?
            #    Because before adding `right`, the window was already valid.
            #    Therefore, only the count we just increased can newly violate
            #    the per-language rule.
            #
            # B) The severity spread exceeds `budget`.
            #    We can get current min and max in O(1) from the fronts of the deques.
            #
            # We keep moving `left` forward until both conditions are satisfied.
            while (
                counts[current_language] > limit
                or severities[max_deque[0]] - severities[min_deque[0]] > budget
            ):
                left_language: str = languages[left]

                # Remove the leftmost language from the count map because that
                # element is leaving the window.
                counts[left_language] -= 1

                # If the index at the front of min_deque is exactly `left`,
                # that means the current minimum element is leaving the window.
                # So we must remove it from the deque front.
                if min_deque and min_deque[0] == left:
                    min_deque.popleft()

                # Similarly, if the current maximum element is leaving the window,
                # remove it from the front of max_deque.
                if max_deque and max_deque[0] == left:
                    max_deque.popleft()

                # Finally move the left boundary one step to the right.
                left += 1

            # ---------------------------------------------------------------
            # Step 5: At this point the window [left, right] is valid.
            # Update the best answer.
            # ---------------------------------------------------------------
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    languages1: List[str] = ["py", "java", "py", "cpp", "java", "py"]
    severities1: List[int] = [4, 7, 5, 6, 8, 5]
    limit1: int = 2
    budget1: int = 3
    result1: int = solution.longest_review_window(languages1, severities1, limit1, budget1)
    print(result1)  # Expected: 4

    # Example 2
    languages2: List[str] = ["go", "go", "rs", "go", "rs", "ts", "rs"]
    severities2: List[int] = [10, 3, 6, 4, 7, 5, 8]
    limit2: int = 2
    budget2: int = 4
    result2: int = solution.longest_review_window(languages2, severities2, limit2, budget2)
    print(result2)  # Expected: 5