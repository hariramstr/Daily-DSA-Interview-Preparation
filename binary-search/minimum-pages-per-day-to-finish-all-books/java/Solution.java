/*
 * Title: Minimum Pages Per Day to Finish All Books
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
 * Note: Every book must be read, and the student reads books in the given order
 * without skipping.
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

import java.util.*;

public class Solution {

    /**
     * Finds the minimum number of pages per day required to finish all books within d days.
     *
     * <p>Core Idea (Binary Search on Answer):
     * Instead of trying every possible pages-per-day value one by one, we use binary search.
     * - The MINIMUM possible answer is the largest single book (we must read at least that many
     *   pages/day to even start reading the biggest book).
     * - The MAXIMUM possible answer is the sum of all pages (read everything in one day).
     * - For any candidate value `mid`, we check: "Can the student finish all books in <= d days
     *   if they read `mid` pages per day?" This check is done greedily.
     * - We binary search for the smallest `mid` where the check passes.
     *
     * @param pages an integer array where pages[i] is the number of pages in book i
     * @param d     the number of days available to finish all books
     * @return the minimum pages per day needed to finish all books within d days
     *
     * Time Complexity:  O(n * log(sum(pages))) where n = pages.length
     *                   - Binary search runs O(log(sum)) iterations
     *                   - Each feasibility check is O(n)
     * Space Complexity: O(1) — only a constant number of extra variables used
     */
    public int minPagesPerDay(int[] pages, int d) {

        // -----------------------------------------------------------------------
        // STEP 1: Determine the search space boundaries.
        // -----------------------------------------------------------------------

        // The lower bound (lo) is the maximum single book page count.
        // Reason: If pagesPerDay < max(pages), we can never finish the largest book
        // in a single day, which violates the constraint.
        int lo = 0;

        // The upper bound (hi) is the total sum of all pages.
        // Reason: If pagesPerDay = sum(pages), the student reads all books in 1 day,
        // which is always feasible (since d >= 1).
        long hi = 0;

        for (int p : pages) {
            // Track the maximum page count among all books
            lo = Math.max(lo, p);
            // Accumulate total pages for the upper bound
            hi += p;
        }

        // `result` will store the best (minimum) feasible pages-per-day found so far.
        // We initialize it to hi (worst case: read everything in one day).
        long result = hi;

        // -----------------------------------------------------------------------
        // STEP 2: Binary search over the candidate pages-per-day values.
        // -----------------------------------------------------------------------

        // We search in the range [lo, hi] inclusive.
        while (lo <= hi) {

            // `mid` is our current candidate for pages per day.
            // We use (lo + hi) / 2 but written safely to avoid overflow:
            long mid = lo + (hi - lo) / 2;

            // -----------------------------------------------------------------------
            // STEP 3: Check if `mid` pages/day is feasible (can finish in <= d days).
            // -----------------------------------------------------------------------

            if (canFinish(pages, d, mid)) {
                // If feasible, `mid` is a valid answer — record it.
                result = mid;

                // Try to find a smaller feasible value by searching the left half.
                hi = mid - 1;
            } else {
                // If not feasible, we need more pages/day — search the right half.
                lo = (int) (mid + 1);
            }
        }

        // -----------------------------------------------------------------------
        // STEP 4: Return the minimum feasible pages-per-day found.
        // -----------------------------------------------------------------------
        return (int) result;
    }

    /**
     * Checks whether the student can finish all books within `d` days
     * if they read at most `pagesPerDay` pages per day.
     *
     * <p>Greedy Strategy:
     * Go through each book in order. Keep a running total of pages read today.
     * If adding the next book would exceed `pagesPerDay`, start a new day.
     * Count how many days are needed in total.
     *
     * @param pages       the array of page counts for each book
     * @param d           the maximum number of days allowed
     * @param pagesPerDay the candidate pages-per-day limit to test
     * @return true if all books can be finished within d days; false otherwise
     *
     * Time Complexity:  O(n) — single pass through the pages array
     * Space Complexity: O(1) — only a few integer variables used
     */
    private boolean canFinish(int[] pages, int d, long pagesPerDay) {

        // `daysNeeded` counts how many days the student needs.
        // We start on day 1.
        int daysNeeded = 1;

        // `pagesReadToday` tracks how many pages have been read on the current day.
        long pagesReadToday = 0;

        // Iterate through each book in order.
        for (int bookPages : pages) {

            // Check if adding this book to today's reading would exceed the daily limit.
            if (pagesReadToday + bookPages > pagesPerDay) {
                // Cannot fit this book today — start a new day.
                daysNeeded++;

                // Begin the new day with this book.
                pagesReadToday = bookPages;

                // Early exit: if we already need more days than allowed, return false.
                if (daysNeeded > d) {
                    return false;
                }
            } else {
                // This book fits in today's reading — add its pages.
                pagesReadToday += bookPages;
            }
        }

        // If total days needed is within the allowed limit, it's feasible.
        return daysNeeded <= d;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // pages = [3, 6, 7, 11], d = 2
        // Expected Output: 17
        //
        // Trace:
        // lo = max(3,6,7,11) = 11, hi = 3+6+7+11 = 27
        //
        // Iteration 1: mid = 11 + (27-11)/2 = 11 + 8 = 19
        //   canFinish([3,6,7,11], 2, 19)?
        //   Day 1: 3 -> 9 -> 16 -> 16+11=27 > 19, new day. daysNeeded=2, today=11
        //   End: daysNeeded=2 <= 2 → true
        //   result=19, hi=18
        //
        // Iteration 2: mid = 11 + (18-11)/2 = 11 + 3 = 14
        //   canFinish([3,6,7,11], 2, 14)?
        //   Day 1: 3 -> 9 -> 16 > 14, new day. daysNeeded=2, today=6
        //          6+7=13 -> 13
        //   Day 2: 13+11=24 > 14, new day. daysNeeded=3 > 2 → false
        //   lo=15
        //
        // Iteration 3: mid = 15 + (18-15)/2 = 15 + 1 = 16
        //   canFinish([3,6,7,11], 2, 16)?
        //   Day 1: 3 -> 9 -> 16 -> 16+11=27 > 16, new day. daysNeeded=2, today=11
        //   End: daysNeeded=2 <= 2 → true
        //   result=16, hi=15
        //
        // Iteration 4: mid = 15 + (15-15)/2 = 15
        //   canFinish([3,6,7,11], 2, 15)?
        //   Day 1: 3 -> 9 -> 16 > 15, new day. daysNeeded=2, today=6
        //          6+7=13 -> 13
        //   Day 2: 13+11=24 > 15, new day. daysNeeded=3 > 2 → false
        //   lo=16
        //
        // Now lo=16 > hi=15, loop ends. result=16... 
        // Wait, let me re-trace more carefully.
        //
        // Actually let me re-trace Example 1 carefully:
        // pages=[3,6,7,11], d=2
        // lo=11, hi=27
        //
        // mid=19: canFinish with 19?
        //   today=0, days=1
        //   book=3: 0+3=3<=19, today=3
        //   book=6: 3+6=9<=19, today=9
        //   book=7: 9+7=16<=19, today=16
        //   book=11: 16+11=27>19 → days=2, today=11
        //   return 2<=2 → true. result=19, hi=18
        //
        // mid=14: canFinish with 14?
        //   today=0, days=1
        //   book=3: 3<=14, today=3
        //   book=6: 9<=14, today=9
        //   book=7: 16>14 → days=2, today=7
        //   book=11: 18>14 → days=3>2 → false. lo=15
        //
        // mid=16: canFinish with 16?
        //   today=0, days=1
        //   book=3: today=3
        //   book=6: today=9
        //   book=7: today=16
        //   book=11: 27>16 → days=2, today=11
        //   return 2<=2 → true. result=16, hi=15
        //
        // mid=15: canFinish with 15?
        //   today=0, days=1
        //   book=3: today=3
        //   book=6: today=9
        //   book=7: 16>15 → days=2, today=7
        //   book=11: 18>15 → days=3>2 → false. lo=16
        //
        // lo=16 > hi=15 → stop. result=16.
        //
        // Hmm, the expected answer is 17 but we get 16.
        // Let me verify: with 16 pages/day and d=2:
        //   Day 1: [3,6,7] = 16 pages ✓ (exactly 16)
        //   Day 2: [11] = 11 pages ✓
        //   Total days = 2 ✓
        // So 16 IS actually feasible! The problem says "Any lower limit like 16 would
        // require at least 3 days" — but that seems wrong in the problem statement.
        // Let's verify with 16: Day1=[3,6,7]=16, Day2=[11]. That's 2 days. So 16 works!
        // Our answer of 16 is actually CORRECT. The problem explanation has an error.
        // -----------------------------------------------------------------------

        System.out.println("=== Minimum Pages Per Day to Finish All Books ===");
        System.out.println();

        // Example 1
        int[] pages1 = {3, 6, 7, 11};
        int d1 = 2;
        int result1 = solution.minPagesPerDay(pages1, d1);
        System.out.println("Example 1:");
        System.out.println("  Input:  pages = " + Arrays.toString(pages1) + ", d = " + d1);
        System.out.println("  Output: " + result1);
        System.out.println("  Explanation: With " + result1 + " pages/day:");
        System.out.println("    Day 1: [3, 6, 7] = 16 pages");
        System.out.println("    Day 2: [11] = 11 pages");
        System.out.println("    Total: 2 days (within limit of " + d1 + ")");
        System.out.println();

        // Example 2
        int[] pages2 = {30, 11, 23, 4, 20};
        int d2 = 5;
        int result2 = solution.minPagesPerDay(pages2, d2);
        System.out.println("Example 2:");
        System.out.println("  Input:  pages = " + Arrays.toString(pages2) + ", d = " + d2);
        System.out.println("  Output: " + result2);
        System.out.println("  Explanation: With " + result2 + " pages/day:");
        System.out.println("    Day 1: [30] = 30 pages");
        System.out.println("    Day 2: [11] = 11 pages");
        System.out.println("    Day 3: [23] = 23 pages");
        System.out.println("    Day 4: [4]  = 4 pages");
        System.out.println("    Day 5: [20] = 20 pages");
        System.out.println("    Total: 5 days (within limit of " + d2 + ")");
        System.out.println();

        // Additional test cases
        System.out.println("=== Additional Test Cases ===");
        System.out.println();

        // Test: Single book, single day
        int[] pages3 = {100};
        int d3 = 1;
        int result3 = solution.minPagesPerDay(pages3, d3);
        System.out.println("Test 3 (single book, single day):");
        System.out.println("  Input:  pages = " + Arrays.toString(pages3) + ", d = " + d3);
        System.out.println("  Output: " + result3 + " (expected: 100)");
        System.out.println();

        // Test: All books same size
        int[] pages4 = {5, 5, 5, 5};
        int d4 = 2;
        int result4 = solution.minPagesPerDay(pages4, d4);
        System.out.println("Test 4 (all books same size):");
        System.out.println("  Input:  