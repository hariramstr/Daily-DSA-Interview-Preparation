"""
Title: Minimum Printer Rate for Deadline-Ordered Reports

Problem Description:
A company has one high-speed printer that must print a list of reports in the given order.
The i-th report has pages[i] pages, and all reports must be fully printed within h hours total.

The printer works at a constant integer rate of r pages per hour. During each hour, it can
print up to r pages from the current report only. If a report finishes before the hour ends,
the remaining time in that hour is wasted and the next report cannot start until the next hour.

Therefore, each report with x pages requires:
    ceil(x / r)
whole hours.

Task:
Find the minimum integer printing rate r such that all reports can be completed within h hours.

Constraints:
- 1 <= pages.length <= 100000
- 1 <= pages[i] <= 1000000000
- pages.length <= h <= 1000000000000
- Reports must be printed in the given order
- The answer is guaranteed to exist

Examples:
1)
Input: pages = [300, 200, 400, 100], h = 8
Output: 150

2)
Input: pages = [30, 11, 23, 4, 20], h = 6
Output: 23
"""

from typing import List


class Solution:
    def hours_needed(self, pages: List[int], rate: int) -> int:
        """
        Compute the total number of whole hours needed to print all reports
        at a given constant printing rate.

        Args:
            pages: A list where pages[i] is the number of pages in the i-th report.
            rate: The printer speed in pages per hour.

        Returns:
            The total whole hours required to print every report in order.

        Time Complexity:
            O(n), where n is the number of reports.

        Space Complexity:
            O(1), excluding input storage.
        """
        # We accumulate the total hours required across all reports.
        total_hours: int = 0

        # We process each report independently because the problem states that
        # even if a report finishes early within an hour, the remaining time is wasted.
        # That means each report consumes a whole-number count of hours:
        # ceil(report_pages / rate)
        for report_pages in pages:
            # Instead of using floating-point math with ceil(report_pages / rate),
            # we use the standard integer formula:
            #     ceil(a / b) = (a + b - 1) // b
            #
            # This is:
            # - faster
            # - exact
            # - avoids floating-point precision issues
            total_hours += (report_pages + rate - 1) // rate

        return total_hours

    def min_print_rate(self, pages: List[int], h: int) -> int:
        """
        Find the minimum integer printing rate that allows all reports
        to be completed within h hours.

        Args:
            pages: A list where pages[i] is the number of pages in the i-th report.
            h: The maximum total number of hours allowed.

        Returns:
            The smallest integer rate r such that the total required hours
            is less than or equal to h.

        Time Complexity:
            O(n log m), where:
            - n is the number of reports
            - m is the maximum page count in pages

        Space Complexity:
            O(1), excluding input storage.
        """
        # -----------------------------
        # Why binary search works here:
        # -----------------------------
        # If we increase the printing rate, the total hours needed can only stay the same
        # or decrease. It can never increase.
        #
        # That means the condition:
        #     "Can we finish within h hours at rate r?"
        # changes monotonically:
        #
        # - For small rates: usually False
        # - For large enough rates: True
        #
        # This "False ... False, True ... True" pattern is exactly what binary search needs
        # to efficiently find the smallest valid rate.

        # The minimum possible rate is 1 page per hour.
        left: int = 1

        # The maximum necessary rate is max(pages).
        # Why?
        # At this speed, every report finishes in at most 1 hour.
        # Since h >= len(pages), the answer is guaranteed to exist.
        right: int = max(pages)

        # We will shrink the search range until left == right.
        # At that point, it will be the smallest valid rate.
        while left < right:
            # Pick the middle candidate rate.
            mid: int = (left + right) // 2

            # Calculate how many hours this candidate rate would require.
            required_hours: int = self.hours_needed(pages, mid)

            # If this rate is fast enough, it is a valid answer candidate.
            # But we still want the MINIMUM valid rate, so we continue searching
            # on the left half, including mid itself.
            if required_hours <= h:
                right = mid
            else:
                # If this rate is too slow, then mid cannot be the answer,
                # and neither can any smaller rate.
                # So we search strictly to the right of mid.
                left = mid + 1

        # When the loop ends, left == right and points to the smallest valid rate.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    pages1: List[int] = [300, 200, 400, 100]
    h1: int = 8
    result1: int = solution.min_print_rate(pages1, h1)
    print("Example 1:")
    print(f"pages = {pages1}, h = {h1}")
    print(f"Minimum printer rate = {result1}")
    print()

    # Example 2
    pages2: List[int] = [30, 11, 23, 4, 20]
    h2: int = 6
    result2: int = solution.min_print_rate(pages2, h2)
    print("Example 2:")
    print(f"pages = {pages2}, h = {h2}")
    print(f"Minimum printer rate = {result2}")
    print()

    # Additional beginner-friendly sanity check
    pages3: List[int] = [1, 1, 1, 1]
    h3: int = 4
    result3: int = solution.min_print_rate(pages3, h3)
    print("Additional Test:")
    print(f"pages = {pages3}, h = {h3}")
    print(f"Minimum printer rate = {result3}")