import java.util.*;

/*
Problem Title: Process Print Jobs by Shortest Remaining Pages

Problem Description:
A shared office printer receives print jobs throughout the day. Each job is described by two integers:
arrivalTime and pages. The printer can work on at most one job at a time, and once it starts a job,
it prints the entire job before switching to another one. If multiple jobs are available, the printer
always chooses the job with the fewest pages remaining to minimize average waiting time. If there is
a tie, choose the job with the smaller original index.

You are given a 0-indexed array jobs where jobs[i] = [arrivalTime_i, pages_i]. Return the order of
job indices in which the printer processes the jobs.

The printer may be idle if no jobs have arrived yet. When the printer becomes free, it may only choose
among jobs whose arrivalTime is less than or equal to the current time. A job that arrives while another
job is being printed must wait until the current job finishes.

Your task is to simulate this scheduling policy efficiently.

Constraints:
- 1 <= jobs.length <= 100000
- 0 <= arrivalTime_i <= 10^9
- 1 <= pages_i <= 10^9
- All jobs fit in 64-bit signed integer time calculations.

Example 1:
Input: jobs = [[1,4],[2,3],[3,1],[10,2]]
Output: [0,2,1,3]

Explanation:
At time 1, only job 0 is available, so it starts first and finishes at time 5.
By then, jobs 1 and 2 are waiting; job 2 has fewer pages, so it is processed next,
then job 1. The printer is idle from time 9 to 10, then processes job 3.

Example 2:
Input: jobs = [[0,5],[0,2],[0,2],[1,1]]
Output: [1,2,3,0]

Explanation:
At time 0, jobs 0, 1, and 2 are available. The printer picks the shortest jobs first,
so jobs 1 and 2 are chosen before job 0. After job 1 finishes, job 3 has arrived and
has only 1 page, so it is processed next. Ties between jobs 1 and 2 are broken by smaller index.

Approach Summary:
1. Attach each job's original index so we can return processing order.
2. Sort all jobs by arrival time.
3. Use a min-heap (priority queue) to store all jobs that have already arrived but are not yet processed.
4. The heap orders jobs by:
   - smaller pages first
   - if tied, smaller original index first
5. Simulate time:
   - If heap is empty, jump time forward to the next job's arrival.
   - Add all jobs whose arrival time is <= current time into the heap.
   - Remove the best job from the heap, process it fully, and advance time by its pages.
6. Continue until all jobs are processed.

This is efficient because each job is inserted into and removed from the heap exactly once.
*/

public class Solution {

    /**
     * Small helper class to store a print job together with its original index.
     */
    private static class Job {
        long arrivalTime;
        long pages;
        int index;

        Job(long arrivalTime, long pages, int index) {
            this.arrivalTime = arrivalTime;
            this.pages = pages;
            this.index = index;
        }
    }

    /**
     * Returns the order of job indices in which the printer processes the jobs.
     *
     * The algorithm works by:
     * 1. Converting each input job into a Job object that also stores its original index.
     * 2. Sorting jobs by arrival time so we can scan them from earliest to latest.
     * 3. Using a priority queue to always choose the available job with the fewest pages.
     *    If two jobs have the same number of pages, the smaller original index is chosen.
     * 4. Simulating the printer's current time:
     *    - If no job is available, jump time to the next arrival.
     *    - Otherwise, process the best available job completely.
     *
     * @param jobs a 2D array where jobs[i][0] is the arrival time and jobs[i][1] is the number of pages
     * @return an array containing the indices of jobs in the exact order they are processed
     * Time complexity: O(n log n), where n is the number of jobs
     * Space complexity: O(n), for the sorted job list, heap, and output array
     */
    public int[] getPrintOrder(int[][] jobs) {
        int n = jobs.length;

        // Convert the raw input into Job objects so that we can:
        // - keep arrival time
        // - keep pages
        // - remember original index for the final answer
        Job[] allJobs = new Job[n];
        for (int i = 0; i < n; i++) {
            allJobs[i] = new Job(jobs[i][0], jobs[i][1], i);
        }

        // Sort jobs by arrival time so we can add them into the heap in chronological order.
        // If arrival times tie, we can optionally tie-break by index for determinism.
        Arrays.sort(allJobs, (a, b) -> {
            if (a.arrivalTime != b.arrivalTime) {
                return Long.compare(a.arrivalTime, b.arrivalTime);
            }
            return Integer.compare(a.index, b.index);
        });

        // Min-heap of currently available jobs.
        // Priority rule:
        // 1. Fewer pages first
        // 2. If tied, smaller original index first
        PriorityQueue<Job> availableJobs = new PriorityQueue<>((a, b) -> {
            if (a.pages != b.pages) {
                return Long.compare(a.pages, b.pages);
            }
            return Integer.compare(a.index, b.index);
        });

        int[] order = new int[n];

        // Pointer into the sorted allJobs array.
        // It tells us which jobs have not yet been added to the heap.
        int nextJobToArrive = 0;

        // Pointer into the answer array.
        int processedCount = 0;

        // Current simulation time.
        // Must be long because arrival times and pages can be large,
        // and repeated additions may exceed int range.
        long currentTime = 0L;

        // Continue until we have processed every job.
        while (processedCount < n) {

            // If there are no available jobs to process right now,
            // the printer is idle. In that case, we must jump time forward
            // to the next job's arrival time.
            //
            // This is important because the printer cannot process anything
            // before a job has arrived.
            if (availableJobs.isEmpty() && nextJobToArrive < n && currentTime < allJobs[nextJobToArrive].arrivalTime) {
                currentTime = allJobs[nextJobToArrive].arrivalTime;
            }

            // Add every job that has arrived by currentTime into the heap.
            //
            // Why do this in a loop?
            // Because multiple jobs may arrive at the same time, or several jobs
            // may have arrived while the printer was busy processing a previous job.
            while (nextJobToArrive < n && allJobs[nextJobToArrive].arrivalTime <= currentTime) {
                availableJobs.offer(allJobs[nextJobToArrive]);
                nextJobToArrive++;
            }

            // Now choose the best available job according to the rules:
            // shortest pages first, then smaller index.
            Job currentJob = availableJobs.poll();

            // Record the original index in the output order.
            order[processedCount] = currentJob.index;
            processedCount++;

            // The printer processes the entire job without interruption.
            // So we simply advance time by the number of pages.
            currentTime += currentJob.pages;
        }

        return order;
    }

    /**
     * Convenience wrapper method with a shorter name for demonstration purposes.
     *
     * @param jobs a 2D array where each element contains [arrivalTime, pages]
     * @return the processing order of original job indices
     * Time complexity: O(n log n), where n is the number of jobs
     * Space complexity: O(n)
     */
    public int[] processPrintJobs(int[][] jobs) {
        return getPrintOrder(jobs);
    }

    /**
     * Converts an int array into a readable string like [1, 2, 3].
     *
     * @param arr the array to convert
     * @return a human-readable string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to string construction
     */
    public static String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input jobs
     * - the computed processing order
     * - the expected order for easy comparison
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: O(n) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] jobs1 = {
            {1, 4},
            {2, 3},
            {3, 1},
            {10, 2}
        };
        int[] result1 = solution.processPrintJobs(jobs1);
        System.out.println("Example 1 Input: [[1,4],[2,3],[3,1],[10,2]]");
        System.out.println("Example 1 Output:   " + arrayToString(result1));
        System.out.println("Example 1 Expected: [0, 2, 1, 3]");
        System.out.println();

        int[][] jobs2 = {
            {0, 5},
            {0, 2},
            {0, 2},
            {1, 1}
        };
        int[] result2 = solution.processPrintJobs(jobs2);
        System.out.println("Example 2 Input: [[0,5],[0,2],[0,2],[1,1]]");
        System.out.println("Example 2 Output:   " + arrayToString(result2));
        System.out.println("Example 2 Expected: [1, 2, 3, 0]");
        System.out.println();

        int[][] jobs3 = {
            {5, 3}
        };
        int[] result3 = solution.processPrintJobs(jobs3);
        System.out.println("Additional Test Input: [[5,3]]");
        System.out.println("Additional Test Output:   " + arrayToString(result3));
        System.out.println("Additional Test Expected: [0]");
    }
}