import java.util.*;

/*
Problem Title: Minimum Batch Size for Deadline-Limited Jobs

Problem Description:
A data processing service must execute a list of jobs in the given order. The i-th job contains jobs[i] records and must be fully processed no later than deadline[i], measured in whole days from the start of processing. The service uses a fixed batch size B, meaning it can process at most B records per day. If a job is not finished on one day, its remaining records continue on the next day. Jobs cannot be reordered, and the service starts job i+1 only after job i is complete.

Your task is to find the minimum integer batch size B such that every job finishes on or before its corresponding deadline.

More formally, if prefix = jobs[0] + jobs[1] + ... + jobs[i], then job i finishes after ceil(prefix / B) days. This value must be less than or equal to deadline[i] for every i.

Return the smallest possible B. You may assume that the deadlines are positive integers and that a valid answer always exists.

Constraints:
- 1 <= jobs.length <= 200000
- 1 <= jobs[i] <= 1000000000
- 1 <= deadline[i] <= 1000000000
- deadline.length == jobs.length
- deadlines are not necessarily sorted, but they correspond to the jobs in input order
- A valid batch size always exists

Example 1:
Input: jobs = [5, 8, 6], deadline = [2, 4, 5]
Output: 4

Explanation:
With B = 5, cumulative work is [5, 13, 19]. The finish days are [ceil(5/5)=1, ceil(13/5)=3, ceil(19/5)=4], which satisfy [2, 4, 5].
With B = 4, cumulative work is [5, 13, 19]. The finish days are [ceil(5/4)=2, ceil(13/4)=4, ceil(19/4)=5], which also satisfy [2, 4, 5].
With B = 3, finish days are [2, 5, 7], which violate deadlines.
Therefore the minimum valid batch size is 4.

Example 2:
Input: jobs = [7, 2, 9, 4], deadline = [1, 2, 5, 6]
Output: 7

Explanation:
For B = 7, cumulative work is [7, 9, 18, 22], so finish days are [1, 2, 3, 4], all within deadline.
For B = 6, the first job alone needs ceil(7/6)=2 days, which misses deadline 1.
So the answer is 7.

Key Insight:
The feasibility condition is monotonic:
- If a batch size B is sufficient, then any larger batch size is also sufficient.
This allows binary search on the answer.
*/

public class Solution {

    /**
     * Finds the minimum integer batch size B such that every job finishes
     * on or before its corresponding deadline.
     *
     * The method uses binary search over the possible batch size range.
     * For each candidate batch size, it performs a linear feasibility check.
     *
     * @param jobs the number of records in each job, processed in the given order
     * @param deadline the deadline for each job, where job i must finish no later than deadline[i]
     * @return the smallest valid batch size that satisfies all deadlines
     *
     * Time complexity: O(n log S), where n is jobs.length and S is the search range of batch sizes
     * Space complexity: O(1), excluding input storage
     */
    public long minimumBatchSize(int[] jobs, int[] deadline) {
        // Defensive validation for beginner-friendliness.
        // The problem guarantees valid input, but these checks make the method safer and clearer.
        if (jobs == null || deadline == null || jobs.length != deadline.length || jobs.length == 0) {
            throw new IllegalArgumentException("jobs and deadline must be non-null, same length, and non-empty.");
        }

        // We binary search the answer B.
        //
        // Lower bound:
        // The smallest possible batch size is at least 1.
        long left = 1L;

        // Upper bound:
        // A simple always-valid upper bound is the maximum single job size.
        // Why?
        // If B >= max(jobs[i]), then each individual job can be processed in at most one day.
        // More importantly, since the problem guarantees a valid answer exists, binary search
        // only needs some sufficiently large valid upper bound.
        //
        // Another safe upper bound is the total sum of all jobs, but max(job) is often smaller.
        long right = 1L;
        for (int job : jobs) {
            right = Math.max(right, job);
        }

        // Because the statement guarantees that a valid answer always exists,
        // and because some deadlines may still require a batch size larger than max(job),
        // we should ensure the upper bound is truly feasible.
        //
        // Example:
        // jobs = [5, 5], deadlines = [1, 1]
        // max(job) = 5, but total prefix for second job is 10, so B must be 10.
        //
        // Therefore, max(job) alone is NOT always enough.
        // A universally safe upper bound is the total sum of all jobs:
        // with B = totalSum, every prefix completes in at most 1 day.
        long totalSum = 0L;
        for (int job : jobs) {
            totalSum += job;
        }
        right = totalSum;

        // Standard binary search for the first feasible value.
        while (left < right) {
            // Midpoint chosen this way to avoid overflow.
            long mid = left + (right - left) / 2;

            // If this batch size works, try to find an even smaller valid one.
            if (isFeasible(jobs, deadline, mid)) {
                right = mid;
            } else {
                // Otherwise, we must increase the batch size.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the minimum feasible batch size.
        return left;
    }

    /**
     * Checks whether a given batch size B is sufficient to finish every job
     * on or before its deadline.
     *
     * For each job i:
     * - Compute the cumulative records processed so far: prefix
     * - The finish day of job i is ceil(prefix / batchSize)
     * - This must be <= deadline[i]
     *
     * The check stops early as soon as one deadline is violated.
     *
     * @param jobs the number of records in each job
     * @param deadline the deadline for each job
     * @param batchSize the candidate batch size being tested
     * @return true if all jobs meet their deadlines with this batch size, false otherwise
     *
     * Time complexity: O(n), where n is jobs.length
     * Space complexity: O(1)
     */
    public boolean isFeasible(int[] jobs, int[] deadline, long batchSize) {
        // Running cumulative sum of all records up to the current job.
        long prefix = 0L;

        // Process jobs in the required order.
        for (int i = 0; i < jobs.length; i++) {
            prefix += jobs[i];

            // Compute ceil(prefix / batchSize) using integer arithmetic:
            // ceil(a / b) = (a + b - 1) / b for positive integers.
            long finishDay = (prefix + batchSize - 1) / batchSize;

            // If this job finishes after its deadline, the batch size is not sufficient.
            if (finishDay > deadline[i]) {
                return false;
            }
        }

        // If every job met its deadline, the batch size works.
        return true;
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param array the array to convert to string form
     * @return a readable string representation of the array
     *
     * Time complexity: O(n), where n is array.length
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the results can be visually verified.
     *
     * @param args command-line arguments, not used
     *
     * Time complexity: O(n log S) per demonstration case
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] jobs1 = {5, 8, 6};
        int[] deadline1 = {2, 4, 5};
        long result1 = solution.minimumBatchSize(jobs1, deadline1);

        System.out.println("Sample 1");
        System.out.println("jobs = " + solution.arrayToString(jobs1));
        System.out.println("deadline = " + solution.arrayToString(deadline1));
        System.out.println("Minimum batch size = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Sample 2
        int[] jobs2 = {7, 2, 9, 4};
        int[] deadline2 = {1, 2, 5, 6};
        long result2 = solution.minimumBatchSize(jobs2, deadline2);

        System.out.println("Sample 2");
        System.out.println("jobs = " + solution.arrayToString(jobs2));
        System.out.println("deadline = " + solution.arrayToString(deadline2));
        System.out.println("Minimum batch size = " + result2);
        System.out.println("Expected = 7");
        System.out.println();

        // Additional quick sanity check
        int[] jobs3 = {1};
        int[] deadline3 = {1};
        long result3 = solution.minimumBatchSize(jobs3, deadline3);

        System.out.println("Additional Test");
        System.out.println("jobs = " + solution.arrayToString(jobs3));
        System.out.println("deadline = " + solution.arrayToString(deadline3));
        System.out.println("Minimum batch size = " + result3);
        System.out.println("Expected = 1");
    }
}