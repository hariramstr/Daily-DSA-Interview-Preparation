import java.util.*;

/*
Problem Title: Minimum Peak Load Limit for Batch Servers

Problem Description:
You are given an array jobs where jobs[i] is the processing time of the i-th job in a fixed arrival order,
and an integer m representing the number of servers available. Each server must process one contiguous
block of jobs, and every job must be assigned to exactly one server. Some servers may remain unused,
but the order of jobs cannot be changed.

Your task is to compute the minimum possible peak load L such that all jobs can be partitioned into at
most m contiguous groups, where the sum of each group is at most L.

However, there is an additional deployment rule: every used server must receive at least one job, and if
a partition is possible for a candidate load L, it is considered valid as long as the number of groups
used is between 1 and m inclusive.

Return the smallest integer L for which such an assignment exists.

This is a realistic scheduling problem where preserving order matters, and the answer is not the partition
itself but the minimum worst-case server load. A brute-force search over all partitions is too slow for
large inputs, so you must exploit the monotonic nature of feasibility with respect to L.

Constraints:
- 1 <= jobs.length <= 200000
- 1 <= jobs[i] <= 1000000000
- 1 <= m <= jobs.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: jobs = [7, 2, 5, 10, 8], m = 2
Output: 18
Explanation: One optimal partition is [7, 2, 5] and [10, 8]. The group sums are 14 and 18, so the peak
load is 18. No smaller limit can support a valid partition into at most 2 contiguous groups.

Example 2:
Input: jobs = [1, 4, 4, 3, 2], m = 3
Output: 5
Explanation: A valid partition is [1, 4], [4], [3, 2]. The maximum group sum is 5. Trying L = 4 fails
because at least one group would exceed the limit or more than 3 groups would be required.
*/
public class Solution {

    /**
     * Computes the minimum possible peak load limit such that the jobs can be split into
     * at most m contiguous groups, with each group's sum not exceeding that limit.
     *
     * The key idea is:
     * 1. If a load limit L is feasible, then any larger limit is also feasible.
     * 2. This monotonic behavior allows binary search on the answer.
     *
     * @param jobs the processing times of jobs in fixed order; each job must be assigned exactly once
     * @param m the maximum number of contiguous groups (servers used) allowed
     * @return the smallest possible maximum group sum as a long
     *
     * Time complexity: O(n log S), where n is jobs.length and S is the search range of sums
     * Space complexity: O(1), excluding input storage
     */
    public long minimumPeakLoad(int[] jobs, int m) {
        // The smallest possible answer cannot be less than the largest single job,
        // because every job must belong to some group, and no group can split a job.
        long left = 0;

        // The largest possible answer is the sum of all jobs,
        // which corresponds to putting everything into one group.
        long right = 0;

        // Build the binary search boundaries carefully using long,
        // because sums can exceed the 32-bit integer range.
        for (int job : jobs) {
            left = Math.max(left, job);
            right += job;
        }

        // Standard binary search on the answer space:
        // We are looking for the smallest feasible load limit.
        while (left < right) {
            // Use this form to avoid overflow:
            long mid = left + (right - left) / 2;

            // If we can partition jobs into at most m groups under limit mid,
            // then mid is a valid candidate, and we try to do even better.
            if (canPartition(jobs, m, mid)) {
                right = mid;
            } else {
                // Otherwise, mid is too small, so we must search larger values.
                left = mid + 1;
            }
        }

        // At loop end, left == right and points to the minimum feasible limit.
        return left;
    }

    /**
     * Checks whether it is possible to partition the jobs into at most m contiguous groups
     * such that the sum of each group does not exceed the given limit.
     *
     * Greedy strategy:
     * - Scan jobs from left to right.
     * - Keep adding jobs to the current group while the sum stays within limit.
     * - As soon as adding the next job would exceed limit, start a new group.
     *
     * Why greedy works here:
     * - For a fixed limit, this strategy produces the minimum number of groups needed.
     * - If even this minimum exceeds m, then no valid partition exists for this limit.
     *
     * @param jobs the processing times of jobs in fixed order
     * @param m the maximum number of groups allowed
     * @param limit the candidate maximum allowed sum for any group
     * @return true if partitioning into at most m groups is possible; false otherwise
     *
     * Time complexity: O(n), where n is jobs.length
     * Space complexity: O(1)
     */
    public boolean canPartition(int[] jobs, int m, long limit) {
        // Start with one group, because if there is at least one job,
        // we must place it somewhere.
        int groupsUsed = 1;

        // Running sum of the current group's load.
        long currentGroupSum = 0;

        // Process jobs in order, preserving contiguity.
        for (int job : jobs) {
            // Safety check:
            // If a single job is larger than the limit, then no partition can work.
            if (job > limit) {
                return false;
            }

            // If adding this job keeps the current group within the limit,
            // we continue extending the current contiguous block.
            if (currentGroupSum + job <= limit) {
                currentGroupSum += job;
            } else {
                // Otherwise, we must start a new group beginning with this job.
                groupsUsed++;
                currentGroupSum = job;

                // Early exit:
                // If we already need more than m groups, then this limit is not feasible.
                if (groupsUsed > m) {
                    return false;
                }
            }
        }

        // If we finish with groupsUsed <= m, then the partition is valid.
        return true;
    }

    /**
     * Convenience wrapper matching a common interview-style method name.
     *
     * @param jobs the processing times of jobs in fixed order
     * @param m the maximum number of contiguous groups allowed
     * @return the minimum possible peak load
     *
     * Time complexity: O(n log S), where n is jobs.length and S is the search range
     * Space complexity: O(1)
     */
    public long splitArray(int[] jobs, int m) {
        return minimumPeakLoad(jobs, m);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 18
     * - Example 2: 5
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n log S) per demonstration call
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] jobs1 = {7, 2, 5, 10, 8};
        int m1 = 2;
        long result1 = solution.minimumPeakLoad(jobs1, m1);
        System.out.println("Example 1 Output: " + result1);

        int[] jobs2 = {1, 4, 4, 3, 2};
        int m2 = 3;
        long result2 = solution.minimumPeakLoad(jobs2, m2);
        System.out.println("Example 2 Output: " + result2);

        // Additional quick sanity checks for beginners:
        int[] jobs3 = {5};
        int m3 = 1;
        System.out.println("Single job Output: " + solution.minimumPeakLoad(jobs3, m3));

        int[] jobs4 = {1, 2, 3, 4, 5};
        int m4 = 5;
        System.out.println("One job per server Output: " + solution.minimumPeakLoad(jobs4, m4));
    }
}