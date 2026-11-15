"""
Title: Minimum Reader Speed for Chapter Deadlines

Problem Description:
You are given an array chapters where chapters[i] is the number of pages in the i-th
chapter of an online course, and an integer h representing the total number of hours
available before all chapter deadlines pass.

A student must read the chapters in the given order, but within any single hour they
can work on only one chapter. If the student's reading speed is s pages per hour,
then finishing a chapter with p pages takes ceil(p / s) whole hours. The student may
start the next chapter only after the current one is fully finished.

Return the minimum integer reading speed s such that the student can finish all
chapters within h hours.

It is guaranteed that an answer exists.

This is a search problem over the answer space: a higher reading speed never increases
the total time needed, so the feasibility of a speed is monotonic.

Constraints:
- 1 <= chapters.length <= 100000
- 1 <= chapters[i] <= 1000000000
- chapters.length <= h <= 1000000000
- The answer fits in a 32-bit signed integer
"""

from typing import List


class Solution:
    def hours_needed(self, chapters: List[int], speed: int, limit: int) -> int:
        """
        Compute how many total whole hours are needed to finish all chapters
        at a given reading speed.

        Args:
            chapters: List of chapter page counts.
            speed: Candidate reading speed in pages per hour.
            limit: Hour limit used for early stopping when total already exceeds it.

        Returns:
            The total number of hours required at the given speed. If the running
            total exceeds limit, the method returns early with a value greater than limit.

        Time complexity:
            O(n), where n is the number of chapters.

        Space complexity:
            O(1), excluding input storage.
        """
        total_hours: int = 0

        # We process chapters one by one because the student must finish each chapter
        # before moving to the next. However, for total time calculation, only the sum
        # of per-chapter required hours matters.
        for pages in chapters:
            # Integer-only ceiling division:
            # ceil(pages / speed) == (pages + speed - 1) // speed
            #
            # We use this instead of math.ceil(pages / speed) to:
            # 1. Avoid floating-point operations
            # 2. Keep the solution fast and precise for very large integers
            total_hours += (pages + speed - 1) // speed

            # Early stopping optimization:
            # If we already exceeded the allowed hours, there is no need to continue.
            # This helps performance on large inputs during binary search.
            if total_hours > limit:
                return total_hours

        return total_hours

    def minReadingSpeed(self, chapters: List[int], h: int) -> int:
        """
        Find the minimum integer reading speed that allows all chapters
        to be finished within h hours.

        Args:
            chapters: List of chapter page counts.
            h: Total available hours.

        Returns:
            The minimum valid integer reading speed.

        Time complexity:
            O(n log m), where:
            - n is the number of chapters
            - m is the maximum chapter size
            This comes from binary searching the answer space and checking feasibility
            in linear time for each candidate speed.

        Space complexity:
            O(1), excluding input storage.
        """
        # Binary search over the answer space:
        #
        # Minimum possible speed:
        # - At least 1 page per hour.
        #
        # Maximum possible speed:
        # - max(chapters) pages per hour is always enough, because then each chapter
        #   takes at most 1 hour, and the constraints guarantee h >= len(chapters).
        left: int = 1
        right: int = max(chapters)

        # We want the smallest speed that is feasible.
        #
        # Feasibility is monotonic:
        # - If a speed works, any larger speed also works.
        # - If a speed does not work, any smaller speed also does not work.
        #
        # This is exactly the pattern where binary search is appropriate.
        while left < right:
            # Midpoint speed candidate.
            mid: int = left + (right - left) // 2

            # Calculate how many hours this speed would require.
            required_hours: int = self.hours_needed(chapters, mid, h)

            if required_hours <= h:
                # This speed is fast enough.
                #
                # Since we want the MINIMUM valid speed, we do not stop here.
                # Instead, we continue searching the left half, including mid itself,
                # because there may be a smaller speed that still works.
                right = mid
            else:
                # This speed is too slow.
                #
                # Therefore, all speeds <= mid are also too slow, so we must search
                # strictly to the right of mid.
                left = mid + 1

        # When the loop ends, left == right, and that value is the smallest feasible speed.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Sample Input 1
    chapters1: List[int] = [120, 90, 150]
    h1: int = 8
    result1: int = solution.minReadingSpeed(chapters1, h1)
    print(f"chapters = {chapters1}, h = {h1}")
    print(f"Minimum reading speed: {result1}")
    # Correct trace:
    # speed 50 -> ceil(120/50)+ceil(90/50)+ceil(150/50) = 3+2+3 = 8 (works)
    # speed 49 -> 3+2+4 = 9 (fails)
    # So the correct minimum is 50.

    print()

    # Sample Input 2
    chapters2: List[int] = [300, 200, 400, 100]
    h2: int = 10
    result2: int = solution.minReadingSpeed(chapters2, h2)
    print(f"chapters = {chapters2}, h = {h2}")
    print(f"Minimum reading speed: {result2}")
    # Correct trace:
    # speed 100 -> 3+2+4+1 = 10 (works)
    # speed 99  -> 4+3+5+2 = 14 (fails)
    # So the correct minimum is 100.

    print()

    # Additional quick sanity checks
    extra_tests = [
        ([1], 1),
        ([30, 11, 23, 4, 20], 6),
        ([30, 11, 23, 4, 20], 5),
        ([1000000000], 1000000000),
    ]

    for chapters, hours in extra_tests:
        result = solution.minReadingSpeed(chapters, hours)
        print(f"chapters = {chapters}, h = {hours}, minimum reading speed = {result}")