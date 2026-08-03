import java.util.*;

/*
Problem Title: Minimum Printer Rate for Deadline Reports

Problem Description:
You are managing a shared office printer that must print several reports in a fixed order.
The i-th report has pages[i] pages and must be fully printed no later than deadline[i],
measured in whole minutes from the start of the day.

The printer works at a constant integer rate of r pages per minute, and it prints the reports
one after another in the given order without interruption. If a report starts in the middle of
a minute, printing still continues normally; the time needed for a report is pages[i] / r minutes.

A rate r is considered feasible if, after printing reports 0 through i in order, the completion
time of every report i is at most deadline[i].

Your task is to find the minimum integer printer rate r such that all reports can be completed
by their respective deadlines. If it is impossible even with an arbitrarily large rate, return -1.

A useful observation is that feasibility is monotonic: if some rate r works, then any rate larger
than r also works. This makes the problem a good candidate for binary search over the answer.

Constraints:
- 1 <= pages.length == deadline.length <= 100000
- 1 <= pages[i] <= 10^9
- 1 <= deadline[i] <= 10^9
- deadline is not guaranteed to be sorted, but deadlines apply to the reports in the given order
- Return the minimum integer rate, or -1 if no such rate exists

Important correctness note:
For report i, the completion time is:
    (pages[0] + pages[1] + ... + pages[i]) / r

So the condition for every i is:
    prefixPages[i] / r <= deadline[i]
which is equivalent to:
    prefixPages[i] <= r * deadline[i]

Therefore, for each i with deadline[i] > 0:
    r >= ceil(prefixPages[i] / deadline[i])

If deadline[i] == 0 and prefixPages[i] > 0, the task is impossible.

This leads to a direct O(n) solution by taking the maximum required rate over all reports.
A binary-search-based feasibility method is also included because the problem highlights monotonicity.
*/
public class Solution {

    /**
     * Computes the minimum integer printer rate using a direct mathematical observation.
     *
     * For each report i, let prefixPages be the total number of pages from report 0 through i.
     * Since the printer works continuously at rate r pages/minute, the completion time of report i is:
     *     prefixPages / r
     *
     * To meet the deadline:
     *     prefixPages / r <= deadline[i]
     * Rearranging:
     *     r >= prefixPages / deadline[i]
     * Since r must be an integer:
     *     r >= ceil(prefixPages / deadline[i])
     *
     * Therefore, the minimum feasible rate is simply the maximum of these required rates over all i.
     * If any deadline[i] is 0 while prefixPages is positive, then no finite rate can satisfy it.
     *
     * @param pages the number of pages in each report, printed in the given order
     * @param deadline the deadline for each report's completion time
     * @return the minimum integer printer rate that satisfies all deadlines, or -1 if impossible
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minimumPrinterRate(int[] pages, int[] deadline) {
        // Basic defensive validation.
        // The problem guarantees valid input sizes, but this makes the method safer and beginner-friendly.
        if (pages == null || deadline == null || pages.length != deadline.length || pages.length == 0) {
            return -1;
        }

        // prefixPages stores the total number of pages that must have been printed
        // by the time report i is completed.
        long prefixPages = 0L;

        // answer will track the largest lower bound on the rate encountered so far.
        long answer = 0L;

        // Process reports in order because deadlines apply to the cumulative completion times.
        for (int i = 0; i < pages.length; i++) {
            prefixPages += pages[i];

            // If the deadline is 0, then the completion time must be <= 0.
            // But prefixPages is positive because pages[i] >= 1, so this is impossible.
            if (deadline[i] == 0) {
                return -1;
            }

            // We need:
            //     r >= ceil(prefixPages / deadline[i])
            //
            // Integer ceiling division formula:
            //     ceil(a / b) = (a + b - 1) / b
            // for positive integers a and b.
            long requiredRate = ceilDiv(prefixPages, deadline[i]);

            // The global minimum feasible rate must satisfy every report,
            // so we take the maximum required rate across all reports.
            answer = Math.max(answer, requiredRate);
        }

        return (int) answer;
    }

    /**
     * Computes the minimum integer printer rate using binary search on the answer.
     *
     * This method is included because the problem explicitly emphasizes monotonic feasibility:
     * if a rate r works, then any larger rate also works.
     *
     * The search range is:
     * - low = 1
     * - high = minimum feasible rate found by the direct formula
     *
     * We first detect impossibility using the same necessary condition:
     * if any deadline is 0 while positive work must already be completed, return -1.
     *
     * @param pages the number of pages in each report, printed in the given order
     * @param deadline the deadline for each report's completion time
     * @return the minimum integer printer rate that satisfies all deadlines, or -1 if impossible
     * Time complexity: O(n log A), where A is the answer range
     * Space complexity: O(1)
     */
    public int minimumPrinterRateBinarySearch(int[] pages, int[] deadline) {
        if (pages == null || deadline == null || pages.length != deadline.length || pages.length == 0) {
            return -1;
        }

        // First, compute an upper bound that is guaranteed to be feasible if the problem is possible.
        // The direct formula gives the exact answer, so it is certainly a valid upper bound.
        int upperBound = minimumPrinterRate(pages, deadline);
        if (upperBound == -1) {
            return -1;
        }

        int low = 1;
        int high = upperBound;

        // Standard binary search for the first feasible rate.
        while (low < high) {
            int mid = low + (high - low) / 2;

            // If mid is feasible, try smaller rates.
            if (isFeasible(pages, deadline, mid)) {
                high = mid;
            } else {
                // Otherwise, we must increase the rate.
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether a given printer rate is feasible.
     *
     * A rate is feasible if every report i finishes by deadline[i].
     * Since reports are printed sequentially, the completion time of report i is:
     *     (pages[0] + ... + pages[i]) / rate
     *
     * To avoid floating-point precision issues, we compare using multiplication:
     *     prefixPages / rate <= deadline[i]
     * is equivalent to:
     *     prefixPages <= rate * deadline[i]
     *
     * All values are handled using long to avoid overflow.
     *
     * @param pages the number of pages in each report
     * @param deadline the deadline for each report
     * @param rate the candidate printer rate in pages per minute
     * @return true if the given rate satisfies all deadlines, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean isFeasible(int[] pages, int[] deadline, int rate) {
        long prefixPages = 0L;

        for (int i = 0; i < pages.length; i++) {
            // Add the current report's pages to the cumulative total.
            prefixPages += pages[i];

            // Check whether the cumulative work can be completed by this deadline.
            // We compare:
            //     prefixPages <= rate * deadline[i]
            //
            // Using long arithmetic is essential because:
            // - prefixPages can be as large as 1e5 * 1e9 = 1e14
            // - rate * deadline[i] can also be large
            long maxPrintableByDeadline = (long) rate * deadline[i];

            if (prefixPages > maxPrintableByDeadline) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the ceiling of a / b for positive integers using integer arithmetic.
     *
     * Example:
     * - ceilDiv(7, 3) = 3
     * - ceilDiv(6, 3) = 2
     *
     * @param a the numerator
     * @param b the denominator, assumed positive
     * @return ceil(a / b)
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding method calls
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt:
        // pages = [6, 8, 3], deadline = [2, 5, 6]
        //
        // Correct trace:
        // prefix pages:
        //   i=0 -> 6, need r >= ceil(6/2) = 3
        //   i=1 -> 14, need r >= ceil(14/5) = 3
        //   i=2 -> 17, need r >= ceil(17/6) = 3
        // So the minimum feasible rate is 3.
        int[] pages1 = {6, 8, 3};
        int[] deadline1 = {2, 5, 6};
        System.out.println(solution.minimumPrinterRate(pages1, deadline1)); // Expected: 3
        System.out.println(solution.minimumPrinterRateBinarySearch(pages1, deadline1)); // Expected: 3

        // Sample 2 from the prompt:
        // pages = [5, 5], deadline = [0, 10]
        //
        // The first report has positive pages and must finish by time 0, which is impossible.
        int[] pages2 = {5, 5};
        int[] deadline2 = {0, 10};
        System.out.println(solution.minimumPrinterRate(pages2, deadline2)); // Expected: -1
        System.out.println(solution.minimumPrinterRateBinarySearch(pages2, deadline2)); // Expected: -1

        // Additional example:
        // pages = [10], deadline = [3]
        // Need r >= ceil(10/3) = 4
        int[] pages3 = {10};
        int[] deadline3 = {3};
        System.out.println(solution.minimumPrinterRate(pages3, deadline3)); // Expected: 4
        System.out.println(solution.minimumPrinterRateBinarySearch(pages3, deadline3)); // Expected: 4

        // Additional example:
        // pages = [4, 4, 4], deadline = [1, 2, 3]
        // prefix pages: 4, 8, 12
        // required rates: 4, 4, 4 => answer 4
        int[] pages4 = {4, 4, 4};
        int[] deadline4 = {1, 2, 3};
        System.out.println(solution.minimumPrinterRate(pages4, deadline4)); // Expected: 4
        System.out.println(solution.minimumPrinterRateBinarySearch(pages4, deadline4)); // Expected: 4
    }
}