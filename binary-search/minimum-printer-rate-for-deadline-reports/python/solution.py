"""
Title: Minimum Printer Rate for Deadline Reports

Problem Description:
You are managing a shared office printer that must print several reports in a fixed order.
The i-th report has pages[i] pages and must be fully printed no later than deadline[i],
measured in whole minutes from the start of the day.

The printer works at a constant integer rate of r pages per minute, and it prints the
reports one after another in the given order without interruption.

If a report starts in the middle of a minute, printing still continues normally; the time
needed for a report is pages[i] / r minutes.

A rate r is considered feasible if, after printing reports 0 through i in order, the
completion time of every report i is at most deadline[i].

Your task is to find the minimum integer printer rate r such that all reports can be
completed by their respective deadlines. If it is impossible even with an arbitrarily
large rate, return -1.

A useful observation is that feasibility is monotonic: if some rate r works, then any
rate larger than r also works. This makes the problem a good candidate for binary search
over the answer.

Constraints:
- 1 <= pages.length == deadline.length <= 100000
- 1 <= pages[i] <= 10^9
- 1 <= deadline[i] <= 10^9
- deadline is not guaranteed to be sorted, but deadlines apply to the reports in the given order
- Return the minimum integer rate, or -1 if no such rate exists
"""

from fractions import Fraction
from typing import List


class Solution:
    def _is_feasible(self, pages: List[int], deadline: List[int], rate: int) -> bool:
        """
        Check whether a given printer rate can finish every report by its deadline.

        Args:
            pages: List of page counts for each report, printed in the given order.
            deadline: List of deadlines for each report.
            rate: Candidate integer printer rate in pages per minute.

        Returns:
            True if all reports finish by their deadlines at this rate, otherwise False.

        Time complexity:
            O(n), where n is the number of reports.

        Space complexity:
            O(1) auxiliary space, ignoring the small constant-sized Fraction objects.
        """
        # We track the exact cumulative printing time after each report.
        #
        # Why exact arithmetic?
        # ---------------------
        # Deadlines are integers, but completion times are rational values because each
        # report takes pages[i] / rate minutes. Using floating-point arithmetic could
        # introduce tiny rounding errors for large values, which is risky in a strict
        # comparison problem. To avoid that completely, we use Fraction from Python's
        # standard library, which stores exact rational numbers.
        #
        # Example:
        #   If total time is 17/3, Fraction stores it exactly as 17/3 rather than an
        #   approximate decimal like 5.666666666...
        current_time: Fraction = Fraction(0, 1)

        # Process reports in the required fixed order.
        for i, page_count in enumerate(pages):
            # Add the exact time needed for the current report at the given rate.
            current_time += Fraction(page_count, rate)

            # The report is feasible only if its completion time is at most its deadline.
            # If even one report misses its deadline, this rate is not feasible.
            if current_time > deadline[i]:
                return False

        # If we never violated any deadline, the rate works.
        return True

    def min_printer_rate(self, pages: List[int], deadline: List[int]) -> int:
        """
        Find the minimum integer printer rate that satisfies all report deadlines.

        Args:
            pages: List of page counts for each report.
            deadline: List of deadlines for each report.

        Returns:
            The minimum feasible integer rate, or -1 if no finite rate can satisfy
            the deadlines.

        Time complexity:
            O(n log U), where n is the number of reports and U is the search range
            for the answer. Here U is bounded by the computed upper bound.

        Space complexity:
            O(1) auxiliary space, excluding temporary Fraction objects.
        """
        # Number of reports.
        n: int = len(pages)

        # ------------------------------------------------------------
        # Step 1: Quick impossibility check using the "infinite speed" idea
        # ------------------------------------------------------------
        # Even if the printer rate became arbitrarily large, each report still cannot
        # finish before the total time needed for all previous reports approaches 0.
        #
        # Since every pages[i] > 0, the completion time of report 0 is always strictly
        # greater than 0 for any finite rate. Therefore:
        #   - If deadline[0] <= 0, it is impossible.
        #
        # More generally, as rate -> infinity, the completion time of each report tends
        # to 0 from above. So any deadline[i] < 0 would also be impossible, but the
        # constraints already guarantee deadlines are >= 1 except the example includes 0.
        #
        # We keep the check simple and directly handle the only meaningful impossible
        # case under the stated constraints: a non-positive deadline for the first report.
        if deadline[0] <= 0:
            return -1

        # ------------------------------------------------------------
        # Step 2: Build a safe upper bound for binary search
        # ------------------------------------------------------------
        # We need some rate "high" such that if a solution exists, then that solution
        # is <= high.
        #
        # For report i to finish by deadline[i], we need:
        #   (pages[0] + pages[1] + ... + pages[i]) / rate <= deadline[i]
        #
        # Rearranging:
        #   rate >= prefix_sum_pages / deadline[i]
        #
        # Since rate must be an integer, the minimum integer satisfying this for report i is:
        #   ceil(prefix_sum_pages / deadline[i])
        #
        # Therefore, if all deadlines are positive, the overall answer must be at most:
        #   max over i of ceil(prefix_sum_pages / deadline[i])
        #
        # This is a very useful bound because it guarantees our binary search interval
        # contains the true answer whenever a solution exists.
        prefix_sum: int = 0
        upper_bound: int = 1

        for i in range(n):
            prefix_sum += pages[i]

            # If any deadline is <= 0, then because completion time is always positive
            # after printing a positive number of pages, no finite rate can satisfy it.
            if deadline[i] <= 0:
                return -1

            # Compute ceil(prefix_sum / deadline[i]) using integer arithmetic:
            #   ceil(a / b) = (a + b - 1) // b
            required_rate_for_this_prefix: int = (prefix_sum + deadline[i] - 1) // deadline[i]

            # Keep the maximum requirement across all prefixes.
            if required_rate_for_this_prefix > upper_bound:
                upper_bound = required_rate_for_this_prefix

        # ------------------------------------------------------------
        # Step 3: Binary search for the minimum feasible rate
        # ------------------------------------------------------------
        # Why binary search works:
        # - If a rate r is feasible, then any larger rate is also feasible because
        #   printing faster can only reduce completion times.
        # - This creates a monotonic True/False pattern:
        #       False False False ... True True True
        # - Binary search can find the first True efficiently.
        left: int = 1
        right: int = upper_bound

        while left < right:
            # Standard middle point calculation.
            mid: int = (left + right) // 2

            # Check whether this candidate rate is enough.
            if self._is_feasible(pages, deadline, mid):
                # mid works, so the minimum feasible rate is in [left, mid].
                right = mid
            else:
                # mid does not work, so we must search larger rates.
                left = mid + 1

        # At loop end, left == right and points to the smallest feasible rate.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # Important correctness note:
    # The prompt's stated output says 4, but its own explanation shows that rate 3 works:
    #   completion times at rate 3 are:
    #   - 6/3 = 2
    #   - (6+8)/3 = 14/3 = 4.666...
    #   - (6+8+3)/3 = 17/3 = 5.666...
    # These are all <= [2, 5, 6], so the true minimum is 3.
    pages1 = [6, 8, 3]
    deadline1 = [2, 5, 6]
    result1 = solution.min_printer_rate(pages1, deadline1)
    print("Example 1 result:", result1)  # Expected correct result: 3

    # Example 2 from the prompt.
    pages2 = [5, 5]
    deadline2 = [0, 10]
    result2 = solution.min_printer_rate(pages2, deadline2)
    print("Example 2 result:", result2)  # Expected: -1

    # Additional small sanity check.
    pages3 = [10]
    deadline3 = [3]
    result3 = solution.min_printer_rate(pages3, deadline3)
    print("Additional example result:", result3)  # Expected: 4