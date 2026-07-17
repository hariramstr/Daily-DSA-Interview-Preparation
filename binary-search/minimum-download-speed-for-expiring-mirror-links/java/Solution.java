import java.util.*;

/*
Problem Title: Minimum Download Speed for Expiring Mirror Links

Problem Description:
You are given a list of file downloads that must be completed using a single downloader.
The downloader processes the files in the given order, and it can work on only one file at a time.
File i has size sizes[i] megabytes and an expiration time expires[i] minutes.

If you choose a constant download speed of S megabytes per minute, then file i requires
ceil(sizes[i] / S) whole minutes to finish.

A file is considered successful only if the cumulative time spent downloading files 0 through i
is less than or equal to expires[i].

Your task is to find the minimum integer download speed S such that all files can be completed
before their mirror links expire. If no speed can make the schedule feasible, return -1.

The order of files cannot be changed, and partial minutes still count as a full minute for each
individual file. This makes the feasibility condition non-trivial, because increasing S changes
multiple rounded durations at once.

Key observation:
If a speed S is feasible, then any speed greater than S is also feasible.
That monotonic property allows binary search over the answer.

Constraints:
- 1 <= n == sizes.length == expires.length <= 2 * 10^5
- 1 <= sizes[i] <= 10^12
- 1 <= expires[i] <= 10^18
- sizes and expires contain integers

Example 1:
Input: sizes = [8, 5, 10], expires = [3, 5, 9]
Output: 3

Explanation:
At speed 3:
ceil(8/3)=3, ceil(5/3)=2, ceil(10/3)=4
Cumulative times: 3, 5, 9
All deadlines are met exactly.

At speed 2:
ceil(8/2)=4, so the first file already misses deadline 3.
Therefore the minimum feasible speed is 3.

Example 2:
Input: sizes = [4, 4, 4], expires = [1, 2, 2]
Output: -1

Explanation:
Even with arbitrarily large speed, each file still takes at least 1 whole minute.
So the cumulative completion times are at least 1, 2, 3.
The third file cannot finish by time 2, so no valid speed exists.
*/

public class Solution {

    /**
     * Finds the minimum integer download speed that allows all files to finish
     * no later than their corresponding expiration times.
     *
     * The method first performs a necessary feasibility check:
     * even with "infinite" speed, each file still needs at least 1 whole minute,
     * so the earliest possible cumulative completion times are 1, 2, 3, ..., n.
     * If any expiration time is smaller than that minimum possible completion time,
     * then the answer is immediately -1.
     *
     * Otherwise, we binary search the smallest speed S such that the schedule is feasible.
     *
     * @param sizes the sizes of the files in megabytes
     * @param expires the expiration times in minutes for each file
     * @return the minimum feasible integer speed, or -1 if no speed can work
     * Time complexity: O(n log M), where M is the maximum file size
     * Space complexity: O(1) extra space
     */
    public long minimumDownloadSpeed(long[] sizes, long[] expires) {
        int n = sizes.length;

        // Step 1:
        // Before doing any binary search, check whether the task is fundamentally impossible.
        //
        // Why this works:
        // Even if the speed is extremely large, each individual file still takes at least
        // 1 whole minute because the duration is ceil(size / speed), and size > 0.
        //
        // Therefore:
        // - file 0 cannot finish earlier than minute 1
        // - file 1 cannot finish earlier than minute 2
        // - file 2 cannot finish earlier than minute 3
        // ...
        // - file i cannot finish earlier than minute (i + 1)
        //
        // If expires[i] < i + 1 for any i, then no speed can possibly satisfy that file.
        for (int i = 0; i < n; i++) {
            long minimumPossibleCompletionTime = i + 1L;
            if (expires[i] < minimumPossibleCompletionTime) {
                return -1;
            }
        }

        // Step 2:
        // Establish binary search bounds.
        //
        // Lower bound:
        // The speed must be at least 1.
        long left = 1L;

        // Upper bound:
        // max(sizes) is always sufficient if the instance is feasible.
        //
        // Reason:
        // If S >= sizes[i], then ceil(sizes[i] / S) = 1 for every file i.
        // So all files take exactly 1 minute each, and cumulative times become 1,2,3,...,n.
        // Since we already checked expires[i] >= i+1, this speed is guaranteed feasible.
        long right = 1L;
        for (long size : sizes) {
            right = Math.max(right, size);
        }

        long answer = -1L;

        // Step 3:
        // Standard binary search on the answer.
        //
        // Monotonicity:
        // If a speed S works, then any larger speed also works because each file's
        // rounded duration ceil(size / S) can only stay the same or decrease.
        while (left <= right) {
            long mid = left + (right - left) / 2;

            if (isFeasible(sizes, expires, mid)) {
                // mid works, so it is a candidate answer.
                answer = mid;

                // But we want the minimum feasible speed,
                // so continue searching on the left half.
                right = mid - 1;
            } else {
                // mid does not work, so we need a larger speed.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given constant download speed is sufficient to complete
     * all files by their expiration times.
     *
     * For each file:
     * - compute its required whole minutes as ceil(size / speed)
     * - add that to the cumulative time
     * - verify the cumulative time does not exceed the file's expiration time
     *
     * The method exits early as soon as any deadline is missed.
     *
     * @param sizes the sizes of the files in megabytes
     * @param expires the expiration times in minutes for each file
     * @param speed the candidate download speed in megabytes per minute
     * @return true if all files can be completed on time at this speed, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean isFeasible(long[] sizes, long[] expires, long speed) {
        long cumulativeTime = 0L;

        for (int i = 0; i < sizes.length; i++) {
            // Compute ceil(sizes[i] / speed) safely using integer arithmetic.
            //
            // Formula:
            // ceil(a / b) = (a + b - 1) / b
            //
            // This is valid for positive integers a and b.
            long timeForCurrentFile = ceilDiv(sizes[i], speed);

            // Add the current file's time to the running total.
            cumulativeTime += timeForCurrentFile;

            // If the cumulative completion time exceeds this file's expiration time,
            // then this speed is not feasible.
            if (cumulativeTime > expires[i]) {
                return false;
            }
        }

        // If we never missed any deadline, the speed works.
        return true;
    }

    /**
     * Computes ceil(a / b) for positive long integers using integer arithmetic.
     *
     * @param a the numerator, must be positive
     * @param b the denominator, must be positive
     * @return the ceiling of a divided by b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called methods
     * Space complexity: O(1) extra space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        long[] sizes1 = {8, 5, 10};
        long[] expires1 = {3, 5, 9};
        long result1 = solution.minimumDownloadSpeed(sizes1, expires1);
        System.out.println("Example 1 Output: " + result1);
        // Expected: 3

        // Example 2
        long[] sizes2 = {4, 4, 4};
        long[] expires2 = {1, 2, 2};
        long result2 = solution.minimumDownloadSpeed(sizes2, expires2);
        System.out.println("Example 2 Output: " + result2);
        // Expected: -1

        // Additional quick sanity checks

        // Single file: speed 5 makes ceil(10/5)=2, deadline 2 => feasible
        long[] sizes3 = {10};
        long[] expires3 = {2};
        long result3 = solution.minimumDownloadSpeed(sizes3, expires3);
        System.out.println("Additional Test 1 Output: " + result3);
        // Expected: 5

        // Already feasible only at high enough speed
        long[] sizes4 = {100, 1};
        long[] expires4 = {10, 11};
        long result4 = solution.minimumDownloadSpeed(sizes4, expires4);
        System.out.println("Additional Test 2 Output: " + result4);

        // Trace verification for Example 1:
        // speed = 3
        // file 0: ceil(8/3)=3, cumulative=3 <= 3
        // file 1: ceil(5/3)=2, cumulative=5 <= 5
        // file 2: ceil(10/3)=4, cumulative=9 <= 9
        // feasible
        //
        // speed = 2
        // file 0: ceil(8/2)=4, cumulative=4 > 3
        // not feasible
        //
        // Therefore answer is 3, which matches the expected output.

        // Trace verification for Example 2:
        // Minimum possible cumulative times are 1, 2, 3.
        // Deadlines are 1, 2, 2.
        // Third file would need completion time <= 2, but minimum is 3.
        // Therefore impossible, answer is -1.
    }
}