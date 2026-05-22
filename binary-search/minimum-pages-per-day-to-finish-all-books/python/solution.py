```python
"""
Title: Minimum Pages Per Day to Finish All Books
Difficulty: Medium
Topic: Binary Search

Problem Description:
A student wants to read a list of books in order, where each book has a certain number
of pages. The student reads exactly d days and must read books in the given order without
skipping. Each day, the student reads a contiguous segment of books, and the number of
pages read per day cannot exceed a chosen daily limit k. The student must read every book
and cannot split a single book across days (each book must be read entirely in one day).

Given an integer array pages where pages[i] is the number of pages in the i-th book,
and an integer d representing the number of days available, return the minimum possible
daily page limit k such that the student can finish all books within d days.

If it is impossible to finish all books in d days (e.g., d is less than the number of
books), return -1.

Constraints:
- 1 <= pages.length <= 10^5
- 1 <= pages[i] <= 10^6
- 1 <= d <= 10^5
"""

from typing import List


class Solution:
    def min_pages_per_day(self, pages: List[int], d: int) -> int:
        """
        Find the minimum daily page limit so the student can finish all books in d days.

        The key insight is: if we can finish all books with a daily limit of k pages,
        we can also finish with any limit > k. This monotonic property means we can
        use binary search on the answer (the daily limit k).

        Args:
            pages: List of integers where pages[i] is the number of pages in book i.
            d: Number of days available to read all books.

        Returns:
            The minimum daily page limit k, or -1 if it's impossible.

        Time Complexity: O(n * log(sum(pages))) where n = len(pages).
            - Binary search runs O(log(sum(pages))) iterations.
            - Each iteration calls can_finish which is O(n).

        Space Complexity: O(1) — only a constant amount of extra space is used.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Handle the impossible case.
        # If d < len(pages), it's impossible because each book must be read on
        # a separate day at minimum (you can't split a book across days, and you
        # need at least one day per book). So if there are more books than days,
        # return -1 immediately.
        # -----------------------------------------------------------------------
        n = len(pages)
        if d < n:
            return -1

        # -----------------------------------------------------------------------
        # STEP 2: Define the search space for binary search.
        #
        # Lower bound (lo): The minimum possible daily limit is max(pages).
        #   Why? Because the student must read every book, and a single book
        #   cannot be split. So the daily limit must be at least as large as
        #   the largest book.
        #
        # Upper bound (hi): The maximum possible daily limit is sum(pages).
        #   Why? If d == 1, the student reads all books in one day, so the
        #   limit must equal the total pages. This is the worst case upper bound.
        # -----------------------------------------------------------------------
        lo = max(pages)       # Minimum feasible daily limit
        hi = sum(pages)       # Maximum needed daily limit (read everything in 1 day)

        # -----------------------------------------------------------------------
        # STEP 3: Binary search for the minimum valid daily limit.
        #
        # We search for the smallest k in [lo, hi] such that can_finish(pages, d, k)
        # returns True.
        #
        # Binary search strategy:
        #   - Compute mid = (lo + hi) // 2
        #   - If we CAN finish with limit mid, try a smaller limit → hi = mid
        #   - If we CANNOT finish with limit mid, we need a larger limit → lo = mid + 1
        #
        # We use the "left boundary" binary search pattern to find the minimum k.
        # -----------------------------------------------------------------------
        while lo < hi:
            # Calculate the midpoint (avoids integer overflow compared to (lo+hi)//2
            # though in Python this isn't strictly necessary, it's good practice)
            mid = (lo + hi) // 2

            # Check if it's feasible to finish all books within d days with limit mid
            if self._can_finish(pages, d, mid):
                # mid works! But maybe something smaller also works.
                # Narrow the search to the left half (including mid).
                hi = mid
            else:
                # mid doesn't work. We need a larger limit.
                # Narrow the search to the right half (excluding mid).
                lo = mid + 1

        # -----------------------------------------------------------------------
        # STEP 4: Return the result.
        # After the loop, lo == hi, and this value is the minimum daily limit
        # that allows finishing all books within d days.
        # -----------------------------------------------------------------------
        return lo

    def _can_finish(self, pages: List[int], d: int, limit: int) -> bool:
        """
        Check whether the student can finish all books within d days given a daily limit.

        This is a greedy helper function. We simulate the reading process:
        - Start on day 1 with 0 pages read so far today.
        - For each book, try to add it to the current day's reading.
        - If adding the book would exceed the daily limit, start a new day.
        - If we need more than d days, return False.

        Args:
            pages: List of page counts for each book.
            d: Maximum number of days allowed.
            limit: The daily page limit to test.

        Returns:
            True if all books can be read within d days with the given limit,
            False otherwise.

        Time Complexity: O(n) where n = len(pages).
        Space Complexity: O(1).
        """

        # days_used tracks how many days we've used so far (start on day 1)
        days_used = 1

        # pages_today tracks how many pages we've read on the current day
        pages_today = 0

        # Iterate through each book in order
        for book_pages in pages:
            # ---------------------------------------------------------------
            # Check if adding this book to today's reading would exceed the limit.
            # If it would, we must start a new day for this book.
            # ---------------------------------------------------------------
            if pages_today + book_pages > limit:
                # Start a new day
                days_used += 1
                # This book is the first book of the new day
                pages_today = book_pages

                # Early exit: if we've already exceeded d days, no need to continue
                if days_used > d:
                    return False
            else:
                # This book fits in today's reading — add it
                pages_today += book_pages

        # If we've gone through all books without exceeding d days, it's feasible
        return True


# -------------------------------------------------------------------------------
# Main block: Test the solution with the provided examples and additional cases.
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ---------------------------------------------------------------------------
    # Example 1:
    # pages = [3, 6, 7, 11, 8], d = 3
    # Expected output: 17
    #
    # Trace with limit = 17:
    #   Day 1: Read books [3, 6, 7] → 3+6+7 = 16 ≤ 17 ✓
    #   Day 2: Read book [11] → 11 ≤ 17 ✓
    #   Day 3: Read book [8] → 8 ≤ 17 ✓
    #   Total days used = 3 = d ✓
    #
    # Trace with limit = 16:
    #   Day 1: Read books [3, 6, 7] → 16 ≤ 16 ✓
    #   Day 2: Read book [11] → 11 ≤ 16 ✓
    #   Day 3: Read book [8] → 8 ≤ 16 ✓
    #   Total days used = 3 = d ✓  → So 16 also works!
    #
    # Wait, let's re-check. max(pages) = 11, so lo starts at 11.
    # With limit = 11:
    #   Day 1: 3 → 3+6=9 → 9+7=16 > 11, so new day. days=2, pages_today=7
    #   Day 2: 7+11=18 > 11, so new day. days=3, pages_today=11
    #   Day 3: 11+8=19 > 11, so new day. days=4 > 3 → False
    # With limit = 14:
    #   Day 1: 3 → 9 → 9+7=16 > 14, new day. days=2, pages_today=7
    #   Day 2: 7+11=18 > 14, new day. days=3, pages_today=11
    #   Day 3: 11+8=19 > 14, new day. days=4 > 3 → False
    # With limit = 16:
    #   Day 1: 3 → 9 → 9+7=16 ≤ 16 → pages_today=16
    #   Day 2: 16+11=27 > 16, new day. days=2, pages_today=11
    #   Day 3: 11+8=19 > 16, new day. days=3, pages_today=8
    #   All books read, days=3 ≤ 3 → True
    # With limit = 13:
    #   Day 1: 3 → 9 → 9+7=16 > 13, new day. days=2, pages_today=7
    #   Day 2: 7+11=18 > 13, new day. days=3, pages_today=11
    #   Day 3: 11+8=19 > 13, new day. days=4 > 3 → False
    # With limit = 15:
    #   Day 1: 3 → 9 → 9+7=16 > 15, new day. days=2, pages_today=7
    #   Day 2: 7+11=18 > 15, new day. days=3, pages_today=11
    #   Day 3: 11+8=19 > 15, new day. days=4 > 3 → False
    # So minimum is 16.
    # ---------------------------------------------------------------------------
    pages1 = [3, 6, 7, 11, 8]
    d1 = 3
    result1 = solution.min_pages_per_day(pages1, d1)
    print(f"Example 1: pages={pages1}, d={d1}")
    print(f"  Result: {result1}")
    print(f"  Expected: 16")
    print()

    # ---------------------------------------------------------------------------
    # Example 2:
    # pages = [10, 20, 30], d = 1
    # Expected output: 60
    #
    # With only 1 day, the student must read all 3 books in one day.
    # Total pages = 10 + 20 + 30 = 60.
    # So the daily limit must be at least 60.
    # ---------------------------------------------------------------------------
    pages2 = [10, 20, 30]
    d2 = 1
    result2 = solution.min_pages_per_day(pages2, d2)
    print(f"Example 2: pages={pages2}, d={d2}")
    print(f"  Result: {result2}")
    print(f"  Expected: 60")
    print()

    # ---------------------------------------------------------------------------
    # Example 3: Impossible case
    # pages = [5, 10, 15], d = 2
    # There are 3 books but only 2 days → impossible → return -1
    # ---------------------------------------------------------------------------
    pages3 = [5, 10, 15]
    d3 = 2
    result3 = solution.min_pages_per_day(pages3, d3)
    print(f"Example 3 (impossible): pages={pages3}, d={d3}")
    print(f"  Result: {result3}")
    print(f"  Expected: -1")
    print()

    # ---------------------------------------------------------------------------
    # Example 4: Single book, single day
    # pages = [42], d = 1
    # Only one book, one day → limit must be at least 42.
    # ---------------------------------------------------------------------------
    pages4 = [42]
    d4 = 1
    result4 = solution.min_pages_per_day(pages4, d4)
    print(f"Example 4 (single book): pages={pages4}, d={d4}")
    print(f"  Result: {result4}")
    print(f"  Expected: 42")
    print()

    # ---------------------------------------------------------------------------
    # Example 5: More days than books
    # pages = [5, 10], d = 5
    # 2 books, 5 days → each book gets its own day (extra days are unused).
    # Minimum limit = max(pages) = 10.
    # ---------------------------------------------------------------------------
    pages5 = [5, 10]
    d5 = 5
    result5 = solution.min_pages_per_day(pages5, d5)
    print(f"Example 5 (more days than books): pages={pages5}, d={d5}")
    print(f"  Result: {result5}")
    print(f"  Expected: 10")
    print()

    # ---------------------------------------------------------------------------
    # Example 6: All books same size
    # pages = [4, 4, 4, 4], d = 2
    # With limit = 8: Day 1 → [4,4]=8, Day 2 → [4,4]=8 → works!
    # With limit = 7: Day 1 → [4] (4+4=8>7, so just [4]), Day 2 → [4], Day 3 → [4,4]=8>7
    #   Day 3 → [4], Day 4 → [4] → 4 days > 2 → False
    # So minimum is 8.
    # ---------------------------------------------------------------------------
    pages6 = [4, 4, 4, 4]
    d6 = 2
    result6 = solution.min_pages_per_day(pages6, d6)
    print(f"Example 6 (all same): pages={pages6}, d={d6}")
    print(f"  Result: {result6}")
    print(f"  Expected: 8")
    print()
```