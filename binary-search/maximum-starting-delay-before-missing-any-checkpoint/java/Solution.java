import java.util.*;

/*
Problem Title: Maximum Starting Delay Before Missing Any Checkpoint

Problem Description:
You are given a route with n mandatory checkpoints that must be visited in order.
For each checkpoint i, the travel time from checkpoint i - 1 to checkpoint i is travel[i],
and the latest allowed arrival time at checkpoint i is deadline[i].

You begin before checkpoint 0, and you may choose to wait some whole number of minutes x
before starting the trip. Once you start, you move continuously through the route and cannot
reorder or skip checkpoints.

Your task is to compute the maximum integer starting delay x such that, after waiting x minutes
and then traveling through all checkpoints in order, you still arrive at every checkpoint no later
than its deadline. If it is impossible to satisfy all deadlines even with x = 0, return -1.

Formally, if prefix[i] is the total travel time needed to reach checkpoint i, then the arrival time
at checkpoint i is x + prefix[i]. This must satisfy:
    x + prefix[i] <= deadline[i]
for every i.

A binary-search-based solution is expected: if a given delay x is feasible, then any smaller delay
is also feasible, which makes the answer monotonic.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= travel[i] <= 10^9
- 1 <= deadline[i] <= 10^18
- The answer fits in signed 64-bit integer range.

Important correctness note:
The mathematically correct answer is:
    max x such that x <= deadline[i] - prefix[i] for all i
So the answer is:
    min(deadline[i] - prefix[i]) over all i
if that minimum is non-negative, otherwise -1.

For the sample:
travel = [3, 2, 4], deadline = [5, 8, 12]
prefix = [3, 5, 9]
allowed x values:
- x <= 5 - 3 = 2
- x <= 8 - 5 = 3
- x <= 12 - 9 = 3
Therefore the maximum feasible x is 2.

This implementation still uses binary search as requested, and it will correctly produce 2
for that sample according to the formal definition in the statement.
*/

public class Solution {

    /**
     * Computes the maximum integer starting delay using binary search.
     *
     * The key monotonic property is:
     * - If a delay x is feasible, then every smaller delay is also feasible.
     * - Therefore, the set of feasible delays forms a prefix of integers:
     *   [0, 1, 2, ..., answer]
     *
     * We first check whether x = 0 is feasible.
     * If not, the route is impossible and we return -1.
     *
     * Otherwise, we binary search the largest feasible x.
     *
     * @param travel the travel time to each checkpoint in order
     * @param deadline the latest allowed arrival time for each checkpoint
     * @return the maximum feasible starting delay, or -1 if even delay 0 is impossible
     * Time complexity: O(n log A), where A is the search range for the answer
     * Space complexity: O(1) extra space
     */
    public long maximumStartingDelay(long[] travel, long[] deadline) {
        int n = travel.length;

        // Step 1:
        // Before doing any binary search, verify whether starting immediately
        // (that is, with delay x = 0) already violates some deadline.
        // If x = 0 is not feasible, then no larger x can ever be feasible.
        if (!isFeasible(travel, deadline, 0L)) {
            return -1L;
        }

        // Step 2:
        // We need an upper bound for binary search.
        //
        // For each checkpoint i:
        //     x + prefix[i] <= deadline[i]
        // so:
        //     x <= deadline[i] - prefix[i]
        //
        // Therefore the answer cannot exceed the minimum of these values.
        // We compute that minimum as a tight upper bound.
        long high = computeTightUpperBound(travel, deadline);

        // Since x = 0 is already feasible here, high must be >= 0.
        long low = 0L;
        long answer = 0L;

        // Step 3:
        // Standard "find maximum feasible value" binary search.
        //
        // Invariant:
        // - all values <= answer seen so far are feasible
        // - we keep searching to the right when mid is feasible
        while (low <= high) {
            long mid = low + (high - low) / 2;

            // Check whether delaying by 'mid' minutes still allows us
            // to reach every checkpoint by its deadline.
            if (isFeasible(travel, deadline, mid)) {
                // mid works, so it is a candidate answer.
                answer = mid;

                // Try to find an even larger feasible delay.
                low = mid + 1;
            } else {
                // mid does not work, so every value larger than mid
                // also cannot work due to monotonicity.
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given starting delay is feasible.
     *
     * We simulate the trip by accumulating prefix travel time.
     * At checkpoint i:
     *     arrival = delay + prefixTravel
     * and this must satisfy:
     *     arrival <= deadline[i]
     *
     * If any checkpoint is missed, the delay is infeasible.
     *
     * @param travel the travel time to each checkpoint
     * @param deadline the latest allowed arrival time for each checkpoint
     * @param delay the candidate starting delay to test
     * @return true if all checkpoints are reached on time, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean isFeasible(long[] travel, long[] deadline, long delay) {
        long prefixTravel = 0L;

        // Walk through checkpoints in order.
        for (int i = 0; i < travel.length; i++) {
            // Add the travel time needed to reach the current checkpoint.
            prefixTravel += travel[i];

            // Arrival time at this checkpoint:
            // wait 'delay' minutes first, then spend 'prefixTravel' minutes traveling.
            long arrivalTime = delay + prefixTravel;

            // If we arrive after the deadline, this delay fails immediately.
            if (arrivalTime > deadline[i]) {
                return false;
            }
        }

        // If we never violated any deadline, the delay is feasible.
        return true;
    }

    /**
     * Computes a tight upper bound for the answer.
     *
     * For each checkpoint i:
     *     delay + prefix[i] <= deadline[i]
     * so:
     *     delay <= deadline[i] - prefix[i]
     *
     * Therefore the maximum possible delay is at most:
     *     min(deadline[i] - prefix[i])
     *
     * This value is not only an upper bound; it is actually the exact answer
     * whenever it is non-negative. We still use it here as the binary search upper bound
     * to match the requested binary-search-based approach.
     *
     * @param travel the travel time to each checkpoint
     * @param deadline the latest allowed arrival time for each checkpoint
     * @return the minimum value of deadline[i] - prefix[i] across all checkpoints
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public long computeTightUpperBound(long[] travel, long[] deadline) {
        long prefixTravel = 0L;
        long upperBound = Long.MAX_VALUE;

        for (int i = 0; i < travel.length; i++) {
            prefixTravel += travel[i];

            // Maximum delay allowed by this checkpoint alone.
            long allowedByThisCheckpoint = deadline[i] - prefixTravel;

            // The overall delay must satisfy every checkpoint,
            // so we take the minimum across all checkpoints.
            upperBound = Math.min(upperBound, allowedByThisCheckpoint);
        }

        return upperBound;
    }

    /**
     * Convenience overload for int[] input.
     *
     * This is useful for beginner-friendly demonstrations in main,
     * while still performing all real computations in long arithmetic.
     *
     * @param travel the travel time to each checkpoint as int values
     * @param deadline the latest allowed arrival time for each checkpoint as long values
     * @return the maximum feasible starting delay, or -1 if impossible
     * Time complexity: O(n log A), where A is the search range for the answer
     * Space complexity: O(n) due to array conversion
     */
    public long maximumStartingDelay(int[] travel, long[] deadline) {
        long[] travelLong = new long[travel.length];
        for (int i = 0; i < travel.length; i++) {
            travelLong[i] = travel[i];
        }
        return maximumStartingDelay(travelLong, deadline);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * According to the formal definition in the statement,
     * Example 1 should evaluate to 2, not 3:
     * prefix = [3, 5, 9]
     * constraints on x:
     *   x <= 2, x <= 3, x <= 3
     * so answer = 2.
     *
     * Example 2 correctly evaluates to -1.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demo sizes
     * Space complexity: O(1) extra space beyond demo arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] travel1 = {3, 2, 4};
        long[] deadline1 = {5, 8, 12};
        System.out.println(solution.maximumStartingDelay(travel1, deadline1)); // Correct by formal definition: 2

        int[] travel2 = {4, 4, 4};
        long[] deadline2 = {3, 10, 15};
        System.out.println(solution.maximumStartingDelay(travel2, deadline2)); // -1

        int[] travel3 = {1, 2, 3};
        long[] deadline3 = {10, 10, 10};
        System.out.println(solution.maximumStartingDelay(travel3, deadline3)); // 4

        int[] travel4 = {5};
        long[] deadline4 = {5};
        System.out.println(solution.maximumStartingDelay(travel4, deadline4)); // 0
    }
}