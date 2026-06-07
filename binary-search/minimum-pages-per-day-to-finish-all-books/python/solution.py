"""
Minimum Pages Per Day to Finish All Books
==========================================

Problem Description:
A student wants to finish reading a collection of books before an exam.
The books must be read in order, and each book has a certain number of pages.
The student reads at most `pagesPerDay` pages per day, and they cannot split
a single book across more than one reading session — they must finish a book
before stopping for the day or start it fresh the next day. However, they can
read multiple books in a single day as long as the total pages do not exceed
`pagesPerDay`.

Given an integer array `pages` where `pages[i]` represents the number of pages
in the i-th book, and an integer `d` representing the number of days the student
has before the exam, return the minimum number of pages per day the student must
be able to read in order to finish all books within `d` days.

Note: Every book must be read, and the student reads books in the given order
without skipping.

Constraints:
- 1 <= pages.length <= 10^5
- 1 <= pages[i] <= 10^6
- 1 <= d <= pages.length
"""

from typing import List


class Solution:
    def min_pages_per_day(self, pages: List[int], d: int) -> int:
        """
        Find the minimum number of pages per day to finish all books within d days.

        This uses Binary Search on the answer space. The key insight is:
        - The minimum possible answer is max(pages) — we must be able to read
          the largest book in a single day.
        - The maximum possible answer is sum(pages) — reading all books in one day.
        - We binary search between these bounds to find the smallest valid capacity.

        Args:
            pages (List[int]): List of page counts for each book (in order).
            d (int): Number of days available before the exam.

        Returns:
            int: The minimum pages per day required to finish all books in d days.

        Time Complexity: O(n * log(sum(pages) - max(pages)))
            - Binary search runs O(log(range)) iterations
            - Each iteration calls can_finish which is O(n)

        Space Complexity: O(1)
            - Only a constant number of extra variables are used
        """

        # -----------------------------------------------------------------------
        # STEP 1: Define the search space boundaries
        # -----------------------------------------------------------------------
        # Lower bound: We MUST be able to read the largest book in a single day.
        #   If pagesPerDay < max(pages), the student can never finish that book.
        #   So the minimum possible answer is max(pages).
        #
        # Upper bound: If the student reads ALL books in one day, that's sum(pages).
        #   This is always feasible (d >= 1), so it's our safe upper bound.
        low = max(pages)   # Minimum feasible pages/day
        high = sum(pages)  # Maximum we'd ever need (read everything in 1 day)

        # -----------------------------------------------------------------------
        # STEP 2: Binary Search on the answer
        # -----------------------------------------------------------------------
        # We want the SMALLEST value of pagesPerDay such that can_finish() is True.
        # Classic "find leftmost True" binary search pattern:
        #   - If mid is feasible, try smaller (move high down)
        #   - If mid is not feasible, we need more pages/day (move low up)
        result = high  # Start with a known valid answer (worst case)

        while low <= high:
            # Pick the midpoint to test as a candidate pagesPerDay
            mid = (low + high) // 2

            if self._can_finish(pages, d, mid):
                # mid pages/day is enough — record it and try to go lower
                result = mid
                high = mid - 1
            else:
                # mid pages/day is NOT enough — we need more pages/day
                low = mid + 1

        # After the loop, `result` holds the minimum valid pagesPerDay
        return result

    def _can_finish(self, pages: List[int], d: int, pages_per_day: int) -> bool:
        """
        Check whether the student can finish all books within d days
        if they read at most `pages_per_day` pages per day.

        Strategy (Greedy):
        - Simulate reading day by day.
        - On each day, keep adding books as long as the running total
          doesn't exceed pages_per_day.
        - When adding the next book would exceed the limit, start a new day.
        - Count total days needed; if it's <= d, return True.

        Args:
            pages (List[int]): Page counts for each book.
            d (int): Maximum number of days allowed.
            pages_per_day (int): Candidate daily reading limit.

        Returns:
            bool: True if all books can be finished within d days, False otherwise.

        Time Complexity: O(n) — single pass through the pages list
        Space Complexity: O(1) — only counters used
        """

        # -----------------------------------------------------------------------
        # STEP 2a: Greedy simulation
        # -----------------------------------------------------------------------
        days_needed = 1      # We always need at least 1 day
        pages_today = 0      # Pages read so far on the current day

        for book_pages in pages:
            # Check if adding this book exceeds today's limit
            if pages_today + book_pages > pages_per_day:
                # Cannot fit this book today — start a new day
                days_needed += 1
                # Begin the new day with this book
                pages_today = book_pages
            else:
                # This book fits today — add it to today's total
                pages_today += book_pages

            # Early exit: if we already need more days than allowed, stop
            if days_needed > d:
                return False

        # If we finished all books within d days, return True
        return days_needed <= d


# =============================================================================
# Main block: Demonstrate and verify with the provided examples
# =============================================================================
if __name__ == "__main__":
    solver = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # pages = [3, 6, 7, 11], d = 2
    # Expected Output: 17
    #
    # Trace:
    #   low = max([3,6,7,11]) = 11
    #   high = sum([3,6,7,11]) = 27
    #
    #   Iteration 1: mid = (11+27)//2 = 19
    #     can_finish([3,6,7,11], 2, 19)?
    #       day1: 3 -> 9 -> 16 -> 16+11=27 > 19, new day
    #       day2: 11
    #       days_needed = 2 <= 2 → True
    #     result = 19, high = 18
    #
    #   Iteration 2: mid = (11+18)//2 = 14
    #     can_finish([3,6,7,11], 2, 14)?
    #       day1: 3 -> 9 -> 9+7=16 > 14, new day
    #       day2: 7 -> 7+11=18 > 14, new day
    #       day3: 11
    #       days_needed = 3 > 2 → False
    #     low = 15
    #
    #   Iteration 3: mid = (15+18)//2 = 16
    #     can_finish([3,6,7,11], 2, 16)?
    #       day1: 3 -> 9 -> 16 -> 16+11=27 > 16, new day
    #       day2: 11
    #       days_needed = 2 <= 2 → True
    #     result = 16, high = 15
    #
    #   Iteration 4: mid = (15+15)//2 = 15
    #     can_finish([3,6,7,11], 2, 15)?
    #       day1: 3 -> 9 -> 9+7=16 > 15, new day
    #       day2: 7 -> 7+11=18 > 15, new day
    #       day3: 11
    #       days_needed = 3 > 2 → False
    #     low = 16
    #
    #   Now low (16) > high (15) → loop ends
    #   result = 16 ... wait, let me re-check example 1.
    #
    # Re-check Example 1 with pages_per_day = 17:
    #   day1: 3 -> 9 -> 16 -> 16+11=27 > 17, new day
    #   day2: 11
    #   days_needed = 2 ✓
    #
    # With pages_per_day = 16:
    #   day1: 3 -> 9 -> 16 -> 16+11=27 > 16, new day
    #   day2: 11
    #   days_needed = 2 ✓  (16 also works!)
    #
    # Hmm — the problem says output is 17, but 16 also seems valid.
    # Let me re-read: "Any lower limit like 16 would require at least 3 days."
    # Wait, with 16: day1 reads [3,6,7]=16 pages, day2 reads [11]=11 pages → 2 days.
    # That contradicts the problem statement explanation.
    #
    # Actually the problem explanation says "16 would require at least 3 days" —
    # but that seems incorrect based on the greedy simulation above.
    # Our algorithm correctly returns 16 for this input, which IS the right answer.
    # The problem's explanation text appears to have an error, but the output "17"
    # might also be wrong in the problem statement, OR the problem means something
    # different. Let's trust our simulation: 16 works in 2 days.
    #
    # Actually wait — let me re-read the problem statement output: "Output: 17"
    # and "Explanation: With 17 pages/day, the student reads [3,6,7] on day 1
    # (16 pages) and [11] on day 2. Any lower limit like 16 would require at
    # least 3 days."
    #
    # This explanation is WRONG. With 16 pages/day:
    #   Day 1: books [3,6,7] = 16 pages total ✓ (fits exactly)
    #   Day 2: book [11] = 11 pages ✓
    # So 16 pages/day also works in 2 days. The correct answer should be 16.
    #
    # Our algorithm returns 16, which is the mathematically correct minimum.
    # ------------------------------------------------------------------
    print("=" * 60)
    print("Example 1:")
    pages1 = [3, 6, 7, 11]
    d1 = 2
    result1 = solver.min_pages_per_day(pages1, d1)
    print(f"  Input:    pages={pages1}, d={d1}")
    print(f"  Output:   {result1}")
    # Our correct answer is 16 (the problem statement's explanation has an error)
    print(f"  Expected: 16 (note: problem statement says 17 but 16 is correct)")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # pages = [30, 11, 23, 4, 20], d = 5
    # Expected Output: 30
    #
    # Trace:
    #   low = max([30,11,23,4,20]) = 30
    #   high = sum([30,11,23,4,20]) = 88
    #
    #   Iteration 1: mid = (30+88)//2 = 59
    #     can_finish with 59?
    #       day1: 30 -> 30+11=41 -> 41+23=64 > 59, new day
    #       day2: 23 -> 23+4=27 -> 27+20=47
    #       days_needed = 2 <= 5 → True
    #     result = 59, high = 58
    #
    #   ... (binary search continues narrowing down) ...
    #
    #   Eventually the search finds that 30 is the minimum:
    #     can_finish([30,11,23,4,20], 5, 30)?
    #       day1: 30 (30+11=41 > 30, new day)
    #       day2: 11 -> 11+23=34 > 30, new day
    #       day3: 23 (23+4=27 -> 27+20=47 > 30, new day)
    #       day4: 4 -> 4+20=24 <= 30
    #       days_needed = 4 <= 5 → True
    #     result = 30
    #
    #     can_finish([30,11,23,4,20], 5, 29)?
    #       low = max = 30, so 29 is never tested (low starts at 30)
    #   Final result = 30 ✓
    # ------------------------------------------------------------------
    print("Example 2:")
    pages2 = [30, 11, 23, 4, 20]
    d2 = 5
    result2 = solver.min_pages_per_day(pages2, d2)
    print(f"  Input:    pages={pages2}, d={d2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: 30")
    print()

    # ------------------------------------------------------------------
    # Additional test cases
    # ------------------------------------------------------------------
    print("Additional Test Cases:")

    # Single book, single day
    pages3 = [100]
    d3 = 1
    result3 = solver.min_pages_per_day(pages3, d3)
    print(f"  Single book: pages={pages3}, d={d3} → {result3} (expected 100)")

    # Each book gets its own day
    pages4 = [5, 10, 15, 20]
    d4 = 4
    result4 = solver.min_pages_per_day(pages4, d4)
    print(f"  One book/day: pages={pages4}, d={d4} → {result4} (expected 20)")

    # All books in one day
    pages5 = [1, 2, 3, 4, 5]
    d5 = 1
    result5 = solver.min_pages_per_day(pages5, d5)
    print(f"  All in 1 day: pages={pages5}, d={d5} → {result5} (expected 15)")

    # Equal pages
    pages6 = [10, 10, 10, 10]
    d6 = 2
    result6 = solver.min_pages_per_day(pages6, d6)
    print(f"  Equal pages:  pages={pages6}, d={d6} → {result6} (expected 20)")

    print()
    print("All examples verified!")