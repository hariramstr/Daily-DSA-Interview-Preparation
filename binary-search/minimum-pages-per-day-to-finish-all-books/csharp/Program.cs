/*
 * Title: Minimum Pages Per Day to Finish All Books
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A student wants to finish reading a collection of books before an exam.
 * The books must be read in order, and each book has a certain number of pages.
 * The student reads at most `pagesPerDay` pages per day, and they cannot split
 * a single book across more than one reading session — they must finish a book
 * before stopping for the day or start it fresh the next day. However, they can
 * read multiple books in a single day as long as the total pages do not exceed
 * `pagesPerDay`.
 *
 * Given an integer array `pages` where `pages[i]` represents the number of pages
 * in the i-th book, and an integer `d` representing the number of days the student
 * has before the exam, return the minimum number of pages per day the student must
 * be able to read in order to finish all books within `d` days.
 *
 * Constraints:
 * - 1 <= pages.length <= 10^5
 * - 1 <= pages[i] <= 10^6
 * - 1 <= d <= pages.length
 *
 * Example 1:
 * Input: pages = [3, 6, 7, 11], d = 2
 * Output: 17
 *
 * Example 2:
 * Input: pages = [30, 11, 23, 4, 20], d = 5
 * Output: 30
 */

using System;
using System.Linq;

/// <summary>
/// Solution class containing the binary search algorithm to find the minimum
/// pages per day needed to finish all books within the given number of days.
/// </summary>
public class Solution
{
    /// <summary>
    /// Finds the minimum number of pages per day the student must read to finish
    /// all books within 'd' days.
    ///
    /// Time Complexity:  O(n * log(sum)) where n = number of books,
    ///                   and sum = total pages across all books.
    ///                   - Binary search runs O(log(sum)) iterations.
    ///                   - Each iteration calls CanFinish which is O(n).
    ///
    /// Space Complexity: O(1) — we only use a constant amount of extra variables.
    /// </summary>
    /// <param name="pages">Array where pages[i] is the page count of book i.</param>
    /// <param name="d">Number of days available to read all books.</param>
    /// <returns>The minimum pages-per-day limit to finish all books in d days.</returns>
    public int MinPagesPerDay(int[] pages, int d)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Establish the search space boundaries.
        //
        // WHY: We need to binary search over all possible "pages per day" values.
        //      The answer must lie within a specific range:
        //
        //      Lower bound (lo): The maximum single book's page count.
        //        - Reason: The student must read every book. If any book has more
        //          pages than the daily limit, that book can never be finished.
        //          So the minimum possible answer is at least max(pages).
        //
        //      Upper bound (hi): The sum of all pages.
        //        - Reason: If the student can read ALL pages in one day, they
        //          definitely finish in 1 day (which is <= d days). This is the
        //          worst-case upper bound — we never need to go higher.
        // -----------------------------------------------------------------------
        int lo = pages.Max();   // Minimum viable daily limit (largest single book)
        int hi = pages.Sum();   // Maximum possible daily limit (all books in one day)

        // -----------------------------------------------------------------------
        // STEP 2: Binary search for the smallest valid pages-per-day value.
        //
        // WHY BINARY SEARCH: The "feasibility" of a given pages-per-day value
        // has a monotonic property:
        //   - If X pages/day is enough to finish in d days → X+1, X+2, ... are also enough.
        //   - If X pages/day is NOT enough → X-1, X-2, ... are also not enough.
        //
        // This monotonic (sorted) structure is exactly what binary search needs.
        // We search for the smallest X where CanFinish(pages, d, X) returns true.
        // -----------------------------------------------------------------------
        while (lo < hi)
        {
            // Calculate the midpoint to avoid integer overflow.
            // Using lo + (hi - lo) / 2 instead of (lo + hi) / 2 is safer.
            int mid = lo + (hi - lo) / 2;

            // -------------------------------------------------------------------
            // STEP 3: Check if 'mid' pages per day is sufficient.
            //
            // WHY: We test the middle value of our current search range.
            //      - If mid is sufficient (student finishes in <= d days):
            //          The answer could be mid itself, or something smaller.
            //          So we move hi down to mid (keeping mid as a candidate).
            //      - If mid is NOT sufficient (student needs more than d days):
            //          mid is too small; we need a larger daily limit.
            //          So we move lo up to mid + 1 (excluding mid).
            // -------------------------------------------------------------------
            if (CanFinish(pages, d, mid))
            {
                // mid works — try to find something smaller (move upper bound down)
                hi = mid;
            }
            else
            {
                // mid doesn't work — we need more pages/day (move lower bound up)
                lo = mid + 1;
            }
        }

        // -----------------------------------------------------------------------
        // STEP 4: Return the result.
        //
        // When the loop ends, lo == hi, and both point to the smallest value
        // for which CanFinish returns true. That is our answer.
        // -----------------------------------------------------------------------
        return lo;
    }

    /// <summary>
    /// Helper method: Determines whether the student can finish all books
    /// within 'd' days if they read at most 'limit' pages per day.
    ///
    /// Strategy: Greedily assign as many books as possible to each day
    /// without exceeding the daily page limit. Count how many days are needed.
    /// If the total days needed <= d, return true.
    ///
    /// Time Complexity: O(n) — single pass through the books array.
    /// Space Complexity: O(1) — only a few integer variables used.
    /// </summary>
    /// <param name="pages">Array of page counts for each book.</param>
    /// <param name="d">Maximum number of days allowed.</param>
    /// <param name="limit">The pages-per-day limit being tested.</param>
    /// <returns>True if all books can be read within d days; false otherwise.</returns>
    private bool CanFinish(int[] pages, int d, int limit)
    {
        // -------------------------------------------------------------------
        // We simulate the reading process greedily:
        //   - Start on day 1.
        //   - Keep a running total of pages read today.
        //   - For each book, try to add it to the current day.
        //       * If adding it would exceed the limit, start a new day.
        //   - Count total days used.
        // -------------------------------------------------------------------

        int daysNeeded = 1;      // We always need at least 1 day
        int pagesReadToday = 0;  // Pages accumulated on the current day

        foreach (int bookPages in pages)
        {
            // -----------------------------------------------------------------
            // Check if adding this book to today's reading would exceed the limit.
            //
            // WHY: The student cannot exceed 'limit' pages in a single day.
            //      If adding this book would go over, we must start a new day
            //      and begin reading this book fresh tomorrow.
            // -----------------------------------------------------------------
            if (pagesReadToday + bookPages > limit)
            {
                // This book doesn't fit today — start a new day
                daysNeeded++;
                pagesReadToday = 0; // Reset the daily page counter
            }

            // Add this book's pages to today's total.
            // (If we just started a new day, this book begins that new day.)
            pagesReadToday += bookPages;
        }

        // -----------------------------------------------------------------
        // After processing all books, check if we stayed within the day limit.
        // If daysNeeded <= d, the student can finish all books in time.
        // -----------------------------------------------------------------
        return daysNeeded <= d;
    }
}

// =============================================================================
// DEMO CODE — Verifying with the provided examples
// =============================================================================

var solution = new Solution();

// ---------------------------------------------------------------------------
// Example 1:
// Input:  pages = [3, 6, 7, 11], d = 2
// Expected Output: 17
//
// Trace through CanFinish with limit = 17:
//   Day 1: book[0]=3  → pagesReadToday=3
//          book[1]=6  → pagesReadToday=9
//          book[2]=7  → pagesReadToday=16
//          book[3]=11 → 16+11=27 > 17 → new day! daysNeeded=2, pagesReadToday=11
//   Total days = 2 <= 2 ✓ → feasible
//
// Trace through CanFinish with limit = 16:
//   Day 1: book[0]=3  → pagesReadToday=3
//          book[1]=6  → pagesReadToday=9
//          book[2]=7  → pagesReadToday=16
//          book[3]=11 → 16+11=27 > 16 → new day! daysNeeded=2, pagesReadToday=11
//   Total days = 2 <= 2 ✓ → feasible
//   Wait — let's check limit=16 more carefully with lo=11 (max book):
//   Binary search will find 17 because lo starts at max(pages)=11.
//   Actually with limit=16: day1=[3,6,7]=16, day2=[11]=11 → 2 days ✓
//   Hmm, that means 16 also works. Let me re-check the problem...
//   The problem says "Any lower limit like 16 would require at least 3 days."
//   Let's re-trace limit=16:
//     book[0]=3  → today=3
//     book[1]=6  → today=9
//     book[2]=7  → today=16
//     book[3]=11 → 16+11=27>16 → new day, daysNeeded=2, today=11
//   Result: 2 days. So 16 also works! The answer should be 17 per problem,
//   but our algorithm gives 16. Let me re-read the problem...
//   Actually re-reading: the problem says output is 17 but 16 also works.
//   Let me re-check: pages=[3,6,7,11], d=2.
//   With limit=16: [3,6,7]=16 on day1, [11] on day2 → 2 days. Works!
//   With limit=11: [3,6,7] → 3+6=9, 9+7=16>11 → new day. [3,6]=9 day1, [7]=7 day2, [11]=11 day3 → 3 days. Fails.
//   With limit=13: [3,6]=9 day1 (9+7=16>13→no), [3,6]=9, +7=16>13→new day. day1=[3,6]=9? 
//   Wait: book[0]=3→today=3, book[1]=6→today=9, book[2]=7→9+7=16>13→new day(2),today=7, book[3]=11→7+11=18>13→new day(3),today=11. 3 days. Fails.
//   With limit=14: book[0]=3,book[1]=6→9,book[2]=7→9+7=16>14→new day(2),today=7,book[3]=11→7+11=18>14→new day(3),today=11. 3 days. Fails.
//   With limit=16: works in 2 days as shown. So answer should be 16, not 17.
//   The problem statement example explanation may have a typo. Our algorithm correctly returns 16.
// ---------------------------------------------------------------------------

int[] pages1 = { 3, 6, 7, 11 };
int d1 = 2;
int result1 = solution.MinPagesPerDay(pages1, d1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  Input:    pages = [3, 6, 7, 11], d = 2");
Console.WriteLine($"  Output:   {result1}");
Console.WriteLine($"  Expected: 17 (problem states) / 16 (mathematically correct)");
Console.WriteLine($"  Note: With limit=16, day1=[3,6,7]=16 pages, day2=[11]=11 pages → 2 days ✓");
Console.WriteLine();

// ---------------------------------------------------------------------------
// Example 2:
// Input:  pages = [30, 11, 23, 4, 20], d = 5
// Expected Output: 30
//
// lo = max(pages) = 30, hi = sum(pages) = 88
// Binary search:
//   mid = 30 + (88-30)/2 = 30 + 29 = 59
//   CanFinish([30,11,23,4,20], 5, 59):
//     book=30 → today=30
//     book=11 → today=41
//     book=23 → today=64>59 → new day(2), today=23
//     book=4  → today=27
//     book=20 → today=47
//     daysNeeded=2 <= 5 ✓ → hi=59
//   mid = 30 + (59-30)/2 = 30+14 = 44
//   CanFinish(..., 44): book=30→30, book=11→41, book=23→41+23=64>44→day2,today=23, book=4→27, book=20→47>44→day3,today=20. days=3<=5 ✓ → hi=44
//   mid = 30 + (44-30)/2 = 30+7 = 37
//   CanFinish(..., 37): book=30→30, book=11→41>37→day2,today=11, book=23→34, book=4→38>37→day3,today=4, book=20→24. days=3<=5 ✓ → hi=37
//   mid = 30 + (37-30)/2 = 30+3 = 33
//   CanFinish(..., 33): book=30→30, book=11→41>33→day2,today=11, book=23→34>33→day3,today=23, book=4→27, book=20→47>33→day4,today=20. days=4<=5 ✓ → hi=33
//   mid = 30 + (33-30)/2 = 30+1 = 31
//   CanFinish(..., 31): book=30→30, book=11→41>31→day2,today=11, book=23→34>31→day3,today=23, book=4→27, book=20→47>31→day4,today=20. days=4<=5 ✓ → hi=31
//   mid = 30 + (31-30)/2 = 30+0 = 30
//   CanFinish(..., 30): book=30→30, book=11→41>30→day2,today=11, book=23→34>30→day3,today=23, book=4→27, book=20→47>30→day4,today=20. days=4<=5 ✓ → hi=30
//   lo==hi==30 → return 30 ✓
// ---------------------------------------------------------------------------

int[] pages2 = { 30, 11, 23, 4, 20 };