import java.util.*;

/*
Problem Title: Minimum Printer Rate for Deadline-Ordered Reports

Problem Description:
A company has one high-speed printer that must print a list of reports in the given order.
The i-th report has pages[i] pages, and all reports must be fully printed within h hours total.

The printer works at a constant integer rate of r pages per hour.
During each hour, it can print up to r pages from the current report only.
If a report finishes before the hour ends, the remaining time in that hour is wasted and
the next report cannot start until the next hour.

In other words, each report with x pages requires ceil(x / r) whole hours.

Your task is to find the minimum integer printing rate r such that all reports can be
completed within h hours.

This problem is designed to be solved efficiently. A brute-force scan over all possible
rates may be too slow when page counts are large. Think about how the total required hours
changes as the printing rate increases.

Return the smallest integer r that satisfies the deadline.

Constraints:
- 1 <= pages.length <= 100000
- 1 <= pages[i] <= 1000000000
- pages.length <= h <= 1000000000000
- Reports must be printed in the given order
- The answer is guaranteed to exist

Example 1:
Input: pages = [300, 200, 400, 100], h = 8
Output: 150
Explanation:
At rate 150, the hours needed are:
ceil(300/150)=2
ceil(200/150)=2
ceil(400/150)=3
ceil(100/150)=1
Total = 8 hours

Any smaller rate requires more than 8 hours.

Example 2:
Input: pages = [30, 11, 23, 4, 20], h = 6
Output: 23
Explanation:
With rate 23, the total time is:
2 + 1 + 1 + 1 + 1 = 6 hours

At rate 22, the total becomes:
2 + 1 + 2 + 1 + 1 = 7 hours

So 23 is the minimum valid rate.
*/

public class Solution {

    /**
     * Finds the minimum integer printing rate needed to finish all reports within h hours.
     *
     * The key idea:
     * - If the printer rate is very small, the total required hours will be large.
     * - If the printer rate increases, the total required hours never increases; it only stays
     *   the same or decreases.
     * - That monotonic behavior makes binary search the correct and efficient approach.
     *
     * Search range:
     * - Minimum possible rate = 1 page/hour
     * - Maximum possible rate = max(pages), because printing faster than the largest report size
     *   in one hour does not reduce any single report below 1 hour.
     *
     * @param pages the number of pages in each report, printed in the given order
     * @param h the maximum total number of hours allowed
     * @return the smallest integer printing rate that allows all reports to finish within h hours
     *
     * Time complexity: O(n log M), where n is pages.length and M is the maximum value in pages
     * Space complexity: O(1), ignoring input storage
     */
    public int minPrintingRate(int[] pages, long h) {
        // Step 1:
        // Determine the upper bound for binary search.
        // The fastest useful rate is the size of the largest report.
        // At that rate, every report finishes in at most 1 hour.
        int maxPages = 0;
        for (int pageCount : pages) {
            maxPages = Math.max(maxPages, pageCount);
        }

        // Step 2:
        // Set up the binary search boundaries.
        // left  = smallest possible rate
        // right = largest necessary rate
        int left = 1;
        int right = maxPages;

        // Step 3:
        // Binary search for the minimum valid rate.
        // We maintain the invariant that the answer lies somewhere in [left, right].
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Step 4:
            // Compute how many total hours are needed if the printer runs at rate = mid.
            long neededHours = calculateRequiredHours(pages, mid, h);

            // Step 5:
            // If we can finish within h hours, then mid is a valid rate.
            // But we still want the minimum valid rate, so we search the left half,
            // including mid itself.
            if (neededHours <= h) {
                right = mid;
            } else {
                // Otherwise, mid is too slow, so we must search rates larger than mid.
                left = mid + 1;
            }
        }

        // Step 6:
        // When left == right, binary search has converged to the smallest valid rate.
        return left;
    }

    /**
     * Calculates the total number of whole hours required to print all reports
     * at a given constant rate.
     *
     * For each report with x pages:
     * required hours = ceil(x / rate)
     *
     * Integer arithmetic trick:
     * ceil(x / rate) can be computed as:
     * (x + rate - 1) / rate
     *
     * We also include an early-exit optimization:
     * - If the running total already exceeds the allowed limit, we can stop early.
     * - This improves performance in many cases during binary search.
     *
     * @param pages the number of pages in each report
     * @param rate the printer rate in pages per hour
     * @param limit the hour limit used for early stopping
     * @return the total hours needed to print all reports at the given rate
     *
     * Time complexity: O(n) in the worst case
     * Space complexity: O(1)
     */
    public long calculateRequiredHours(int[] pages, int rate, long limit) {
        long totalHours = 0L;

        // Process each report in order.
        for (int pageCount : pages) {
            // Each report consumes whole hours only.
            // Even if a report finishes early in an hour, the remaining time is wasted.
            long hoursForThisReport = (pageCount + (long) rate - 1L) / (long) rate;
            totalHours += hoursForThisReport;

            // Early exit:
            // If we already exceed the allowed limit, there is no need to continue.
            if (totalHours > limit) {
                return totalHours;
            }
        }

        return totalHours;
    }

    /**
     * Convenience overload that calculates total hours without an early-stop limit.
     * This is useful for demonstration, testing, and tracing examples.
     *
     * @param pages the number of pages in each report
     * @param rate the printer rate in pages per hour
     * @return the total hours needed to print all reports at the given rate
     *
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long calculateRequiredHours(int[] pages, int rate) {
        return calculateRequiredHours(pages, rate, Long.MAX_VALUE);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     * Also prints a few verification values to make the behavior easy to understand.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n log M) for each demonstration call
     * Space complexity: O(1), ignoring input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] pages1 = {300, 200, 400, 100};
        long h1 = 8;
        int answer1 = solution.minPrintingRate(pages1, h1);

        System.out.println("Sample 1:");
        System.out.println("pages = " + Arrays.toString(pages1) + ", h = " + h1);
        System.out.println("Minimum printing rate = " + answer1);
        System.out.println("Hours needed at rate 150 = " + solution.calculateRequiredHours(pages1, 150));
        System.out.println("Hours needed at rate 149 = " + solution.calculateRequiredHours(pages1, 149));
        System.out.println("Expected: 150");
        System.out.println();

        // Sample 2
        int[] pages2 = {30, 11, 23, 4, 20};
        long h2 = 6;
        int answer2 = solution.minPrintingRate(pages2, h2);

        System.out.println("Sample 2:");
        System.out.println("pages = " + Arrays.toString(pages2) + ", h = " + h2);
        System.out.println("Minimum printing rate = " + answer2);
        System.out.println("Hours needed at rate 23 = " + solution.calculateRequiredHours(pages2, 23));
        System.out.println("Hours needed at rate 22 = " + solution.calculateRequiredHours(pages2, 22));
        System.out.println("Expected: 23");
        System.out.println();

        // Additional small demonstration
        int[] pages3 = {10, 10, 10};
        long h3 = 3;
        int answer3 = solution.minPrintingRate(pages3, h3);

        System.out.println("Additional Demo:");
        System.out.println("pages = " + Arrays.toString(pages3) + ", h = " + h3);
        System.out.println("Minimum printing rate = " + answer3);
    }
}