import java.util.*;

/*
Title: Minimum Processor Count for Deadline-Sorted Builds

Problem Description:
You are given a list of software build jobs that must be executed in the given order.
The i-th job takes buildTimes[i] minutes and must finish no later than deadlines[i]
minutes from time 0.

You may provision k identical processors, where each processor can run at most one
job at a time, and preemption is not allowed. Jobs are assigned online in the fixed
order: when considering a job, you may choose any processor, but the relative order
of jobs in the input cannot be changed.

Your task is to return the minimum number of processors needed so that all jobs can
be completed before or at their respective deadlines.

A schedule is valid if every job starts after the previous job assigned to the same
processor finishes, and each job's completion time is at most its deadline. If no
number of processors from 1 to n can satisfy the deadlines, return -1.

This problem is designed around a monotonic feasibility condition: if k processors are
enough, then any number greater than k is also enough. An efficient solution should
combine binary search on the answer with a fast feasibility check using an appropriate
data structure.

Constraints:
- 1 <= n == buildTimes.length == deadlines.length <= 200000
- 1 <= buildTimes[i] <= 1000000000
- 1 <= deadlines[i] <= 1000000000000000000
- Jobs must be considered in the given order
- Processor count can be between 1 and n

Example 1:
Input: buildTimes = [3, 2, 4, 1], deadlines = [4, 5, 8, 6]
Output: 2

Explanation:
With 1 processor, completion times are 3, 5, 9, 10, so job 3 misses its deadline.
With 2 processors, assign jobs as follows:
- processor A gets jobs 0 and 3 (finishes at 3 and 4)
- processor B gets jobs 1 and 2 (finishes at 2 and 6)
All deadlines are met, so the answer is 2.

Example 2:
Input: buildTimes = [5, 5, 5], deadlines = [4, 10, 15]
Output: -1

Explanation:
The first job alone takes 5 minutes but must finish by time 4. Since even an isolated
processor cannot complete it in time, no feasible schedule exists for any processor count.
*/

/**
 * A complete runnable solution for finding the minimum number of processors needed
 * to execute jobs in input order while meeting individual deadlines.
 *
 * Core idea:
 * 1. The feasibility is monotonic:
 *    - If k processors are enough, then k+1, k+2, ... are also enough.
 * 2. Therefore, we can binary search the minimum feasible k.
 * 3. To test feasibility for a fixed k, we greedily place each job on the processor
 *    that becomes available the earliest.
 *
 * Why the greedy feasibility check is correct:
 * - When jobs must be considered in fixed order, the only decision for each job is:
 *   which processor should receive it?
 * - To maximize future flexibility and minimize this job's completion time, we should
 *   always assign the current job to the processor with the smallest current load
 *   (earliest finish time so far).
 * - If even this earliest available processor would make the job miss its deadline,
 *   then any other processor would finish no earlier, so no valid assignment exists
 *   for this job under the current k.
 */
public class Solution {

    /**
     * Returns the minimum number of processors needed so that all jobs can be completed
     * by their deadlines while jobs are considered in the given order.
     *
     * The method first checks whether the instance is feasible at all using n processors.
     * If not, it returns -1.
     *
     * Otherwise, it binary searches the smallest k in [1, n] such that the schedule
     * is feasible.
     *
     * @param buildTimes the duration of each job
     * @param deadlines the deadline of each job measured from time 0
     * @return the minimum number of processors needed, or -1 if impossible
     *
     * Time complexity:
     * - Feasibility check for a fixed k: O(n log k)
     * - Binary search over k: O(log n)
     * - Total: O(n log n log n) in the worst case, which is efficient enough for n <= 200000
     *
     * Space complexity:
     * - O(k) for the priority queue during a feasibility check
     * - O(n) worst-case across the search when k can be as large as n
     */
    public int minimumProcessorCount(int[] buildTimes, long[] deadlines) {
        int n = buildTimes.length;

        // Quick impossibility check:
        // If even with n processors (effectively every job can start at time 0),
        // some job still cannot finish by its own deadline, then no solution exists.
        if (!canSchedule(buildTimes, deadlines, n)) {
            return -1;
        }

        int left = 1;
        int right = n;
        int answer = n;

        // Standard binary search on the minimum feasible processor count.
        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (canSchedule(buildTimes, deadlines, mid)) {
                // mid processors are enough, so try to do even better.
                answer = mid;
                right = mid - 1;
            } else {
                // mid processors are not enough, need more.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether all jobs can be scheduled on exactly k processors while respecting:
     * 1. Jobs are considered in the given input order
     * 2. Each processor executes its assigned jobs sequentially
     * 3. Every job must finish by its deadline
     *
     * Greedy strategy:
     * - Maintain the current finish time of each processor.
     * - For each job, always assign it to the processor with the smallest current finish time.
     * - This gives the earliest possible completion time for the current job.
     *
     * Why this works:
     * - Suppose the earliest available processor has finish time tMin.
     * - Any other processor has finish time >= tMin.
     * - So assigning the current job anywhere else cannot make it finish earlier.
     * - Therefore, if tMin + buildTime > deadline, then no assignment can satisfy this job.
     *
     * Implementation details:
     * - We use a min-heap (priority queue) storing processor finish times.
     * - Initially, all k processors are free at time 0, so the heap starts with k zeros.
     * - For each job:
     *   a) Extract the smallest finish time
     *   b) Add the job duration
     *   c) Check against the job's deadline
     *   d) Put the updated finish time back into the heap
     *
     * @param buildTimes the duration of each job
     * @param deadlines the deadline of each job
     * @param k the number of processors to test
     * @return true if scheduling is feasible with k processors, false otherwise
     *
     * Time complexity:
     * - O(n log k), because each of n jobs performs one poll and one offer on a heap of size k
     *
     * Space complexity:
     * - O(k) for the heap
     */
    public boolean canSchedule(int[] buildTimes, long[] deadlines, int k) {
        PriorityQueue<Long> minHeap = new PriorityQueue<>();

        // Initially, every processor is idle at time 0.
        for (int i = 0; i < k; i++) {
            minHeap.offer(0L);
        }

        // Process jobs strictly in input order.
        for (int i = 0; i < buildTimes.length; i++) {
            long earliestAvailableTime = minHeap.poll();

            // If we place the current job on the earliest available processor,
            // this is the earliest completion time achievable for this job.
            long completionTime = earliestAvailableTime + buildTimes[i];

            // If even this best possible completion time misses the deadline,
            // then no valid assignment exists for this job under k processors.
            if (completionTime > deadlines[i]) {
                return false;
            }

            // Update that processor's finish time and put it back into the heap.
            minHeap.offer(completionTime);
        }

        return true;
    }

    /**
     * Convenience overload that accepts deadlines as int[].
     * This is useful for small demonstrations, but the main algorithm internally
     * works with long[] because deadlines can be as large as 1e18.
     *
     * @param buildTimes the duration of each job
     * @param deadlines the deadline of each job as int values
     * @return the minimum number of processors needed, or -1 if impossible
     *
     * Time complexity:
     * - O(n log n log n), same as the main method after conversion
     *
     * Space complexity:
     * - O(n) for the converted deadline array plus heap usage
     */
    public int minimumProcessorCount(int[] buildTimes, int[] deadlines) {
        long[] converted = new long[deadlines.length];
        for (int i = 0; i < deadlines.length; i++) {
            converted[i] = deadlines[i];
        }
        return minimumProcessorCount(buildTimes, converted);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 2
     * - Example 2: -1
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] buildTimes1 = {3, 2, 4, 1};
        long[] deadlines1 = {4L, 5L, 8L, 6L};
        int result1 = solution.minimumProcessorCount(buildTimes1, deadlines1);
        System.out.println(result1); // Expected: 2

        // Example 2
        int[] buildTimes2 = {5, 5, 5};
        long[] deadlines2 = {4L, 10L, 15L};
        int result2 = solution.minimumProcessorCount(buildTimes2, deadlines2);
        System.out.println(result2); // Expected: -1

        // Additional small sanity check
        int[] buildTimes3 = {1, 2, 3};
        long[] deadlines3 = {1L, 2L, 3L};
        int result3 = solution.minimumProcessorCount(buildTimes3, deadlines3);
        System.out.println(result3); // One possible expected result: 2
    }
}