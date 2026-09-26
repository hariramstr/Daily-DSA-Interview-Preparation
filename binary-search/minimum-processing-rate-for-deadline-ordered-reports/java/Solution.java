import java.util.*;

/*
Problem Title: Minimum Processing Rate for Deadline Ordered Reports

Problem Description:
You are given a list of report jobs that must be processed in the given order. The i-th job contains reports[i] pages and must be fully completed no later than time deadlines[i]. A single processor works at a constant integer rate r pages per hour. The processor may switch to the next job immediately after finishing the current one, but jobs cannot be reordered, split across multiple processors, or processed in parallel.

For a chosen rate r, the time needed for job i is ceil(reports[i] / r). Because jobs must be processed sequentially, the completion time of each job is the sum of the rounded-up processing times of all jobs from 0 to i. A rate r is feasible if every job finishes by its corresponding deadline.

Return the minimum integer rate r such that all jobs can be completed on time, or -1 if no integer rate can satisfy the deadlines.

This is not a simple per-job check: even if each job individually fits its own deadline, earlier jobs may delay later ones. You need to exploit the monotonic property of feasibility with respect to the processing rate and design an efficient solution.

Constraints:
- 1 <= n == reports.length == deadlines.length <= 2 * 10^5
- 1 <= reports[i] <= 10^12
- 1 <= deadlines[i] <= 10^18
- deadlines is not guaranteed to be sorted, but represents the required completion time of each job in the given order
- The answer, if it exists, is at most 10^12

Example 1:
Input: reports = [8, 5, 10], deadlines = [2, 4, 7]
Output: 4

Example 2:
Input: reports = [9, 9, 9], deadlines = [1, 2, 2]
Output: -1
*/

public class Solution {

    /**
     * Finds the minimum integer processing rate such that all report jobs,
     * processed strictly in the given order, finish by their corresponding deadlines.
     *
     * Core idea:
     * - If a rate r is feasible, then any larger rate is also feasible.
     * - This monotonic property allows binary search on the answer.
     * - We first check whether any solution exists at all using the maximum allowed answer bound.
     *
     * @param reports the number of pages in each job; reports[i] is the size of the i-th job
     * @param deadlines the required completion time for each job in order; deadlines[i] is the latest time by which job i must be finished
     * @return the minimum feasible integer rate, or -1 if no integer rate can satisfy all deadlines
     *
     * Time complexity: O(n log M), where n is the number of jobs and M = 10^12 is the search range for the rate.
     * Space complexity: O(1) extra space, excluding input storage.
     */
    public long minimumProcessingRate(long[] reports, long[] deadlines) {
        int n = reports.length;

        // Defensive validation for beginner-friendliness.
        // The problem guarantees equal lengths, but checking makes the method safer.
        if (n != deadlines.length || n == 0) {
            return -1;
        }

        // Important impossibility observation:
        // Even with an extremely large rate, each non-empty job still takes at least 1 hour
        // because time is ceil(reports[i] / r), and reports[i] >= 1.
        //
        // Therefore, the absolute best possible cumulative completion times are:
        // 1, 2, 3, ..., n
        //
        // If for any job i, deadline[i] < i + 1, then it is impossible no matter how large r is.
        long minimumPossibleCompletion = 0;
        for (int i = 0; i < n; i++) {
            minimumPossibleCompletion++;
            if (minimumPossibleCompletion > deadlines[i]) {
                return -1;
            }
        }

        // The statement guarantees that if an answer exists, it is at most 10^12.
        long left = 1L;
        long right = 1_000_000_000_000L;

        // Before binary search, verify that some feasible rate exists within the allowed bound.
        // If even the maximum allowed answer is not feasible, return -1.
        if (!isFeasible(reports, deadlines, right)) {
            return -1;
        }

        // Standard binary search for the first feasible rate.
        while (left < right) {
            long mid = left + (right - left) / 2;

            if (isFeasible(reports, deadlines, mid)) {
                // mid works, so the answer is in [left, mid]
                right = mid;
            } else {
                // mid does not work, so the answer is in [mid + 1, right]
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Checks whether a given processing rate is sufficient to finish all jobs on time.
     *
     * Detailed logic:
     * - Jobs must be processed in order.
     * - Time for one job = ceil(reports[i] / rate).
     * - Completion time of job i = sum of all job times from 0 to i.
     * - The rate is feasible only if every completion time is <= deadlines[i].
     *
     * Early exit:
     * - As soon as one job misses its deadline, we can immediately return false.
     *
     * @param reports the number of pages in each job
     * @param deadlines the deadline for each job's completion
     * @param rate the candidate integer processing rate in pages per hour
     * @return true if all jobs finish by their deadlines at this rate; false otherwise
     *
     * Time complexity: O(n), where n is the number of jobs.
     * Space complexity: O(1) extra space.
     */
    public boolean isFeasible(long[] reports, long[] deadlines, long rate) {
        long currentTime = 0L;

        for (int i = 0; i < reports.length; i++) {
            // Compute ceil(reports[i] / rate) safely using integer arithmetic:
            // ceil(a / b) = (a + b - 1) / b
            long jobTime = ceilDiv(reports[i], rate);

            // Add this job's processing time to the cumulative completion time.
            currentTime += jobTime;

            // If the cumulative completion time already exceeds this job's deadline,
            // then this rate is not feasible.
            if (currentTime > deadlines[i]) {
                return false;
            }
        }

        // If every job met its deadline, the rate is feasible.
        return true;
    }

    /**
     * Computes ceil(a / b) for positive long integers using integer arithmetic.
     *
     * @param a the numerator; expected to be positive
     * @param b the denominator; expected to be positive
     * @return the mathematical ceiling of a / b
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(n log M) per demonstration call.
     * Space complexity: O(1) extra space, excluding input arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] reports1 = {8, 5, 10};
        long[] deadlines1 = {2, 4, 7};
        long answer1 = solution.minimumProcessingRate(reports1, deadlines1);
        System.out.println("Example 1 Output: " + answer1);
        // Expected: 4
        //
        // Quick trace:
        // rate = 4
        // job times = ceil(8/4)=2, ceil(5/4)=2, ceil(10/4)=3
        // cumulative = 2, 4, 7 -> all meet deadlines [2,4,7]
        //
        // rate = 3
        // job times = 3, 2, 4
        // cumulative = 3, 5, 9 -> first job already misses deadline 2
        //
        // Therefore minimum feasible rate is 4.

        long[] reports2 = {9, 9, 9};
        long[] deadlines2 = {1, 2, 2};
        long answer2 = solution.minimumProcessingRate(reports2, deadlines2);
        System.out.println("Example 2 Output: " + answer2);
        // Expected: -1
        //
        // Quick trace:
        // Even with arbitrarily large rate, each job takes at least 1 hour.
        // Best possible cumulative completion times are 1, 2, 3.
        // Third deadline is 2, but best possible completion is 3 -> impossible.

        // Additional small sanity check.
        long[] reports3 = {1, 1, 1};
        long[] deadlines3 = {1, 2, 3};
        long answer3 = solution.minimumProcessingRate(reports3, deadlines3);
        System.out.println("Additional Example Output: " + answer3);
        // Expected: 1
    }
}