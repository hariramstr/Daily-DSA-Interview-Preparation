import java.util.*;

/*
Problem Title: Minimum Processor Speed for Sequential Simulation Batches

Problem Description:
A research platform must run n simulation batches in the given order. The i-th batch contains
workloads[i] compute units and must be fully processed no later than deadlines[i] minutes from time 0.
All batches are executed on a single processor, one after another, without reordering and without
preemption. If the processor speed is S compute units per minute, then batch i takes
ceil(workloads[i] / S) minutes to finish.

Your task is to find the minimum positive integer processor speed S such that every batch finishes
by its deadline. If no speed can satisfy the deadlines, return -1.

More formally, let finish[i] be the cumulative running time of batches 0 through i. A speed S is
feasible if for every i, finish[i] <= deadlines[i]. You must compute the smallest feasible S.

This problem is designed for a binary-search-on-answer approach. The challenge is to test feasibility
efficiently while handling very large workloads and deadlines. Be careful with integer overflow when
summing running times and computing ceil division.

Constraints:
- 1 <= n <= 200000
- 1 <= workloads[i] <= 10^12
- 1 <= deadlines[i] <= 10^18
- deadlines is not guaranteed to be sorted, but it represents the required completion time of each prefix in the given order
- Return -1 if even an arbitrarily large speed cannot make all batches meet their deadlines

Example 1:
Input: workloads = [7, 11, 5], deadlines = [4, 8, 10]
Output: 3

Explanation:
At speed 4, batch times are [2, 3, 2], so cumulative finish times are [2, 5, 7], all within [4, 8, 10].
At speed 3, times are [3, 4, 2], cumulative finish times [3, 7, 9], all within [4, 8, 10].
At speed 2, times are [4, 6, 3], cumulative [4, 10, 13], violating deadline 8.
Therefore the minimum feasible speed is 3.

Example 2:
Input: workloads = [9, 9, 9], deadlines = [0, 5, 8]
Output: -1

Explanation:
The first batch needs at least 1 minute for any finite positive integer speed, but its deadline is 0,
so no valid speed exists.
*/

public class Solution {

    /**
     * Computes the minimum positive integer processor speed that allows all batches
     * to finish by their respective deadlines when processed sequentially.
     *
     * The method uses binary search on the answer:
     * - If a speed S is feasible, then any larger speed is also feasible.
     * - If a speed S is not feasible, then any smaller speed is also not feasible.
     *
     * This monotonic property makes binary search valid.
     *
     * @param workloads the compute units required by each batch, processed in order
     * @param deadlines the deadline for each prefix completion time
     * @return the minimum feasible positive integer speed, or -1 if no finite speed can satisfy all deadlines
     * Time complexity: O(n log M), where n is the number of batches and M is the search range of speeds
     * Space complexity: O(1), excluding input storage
     */
    public long minimumProcessorSpeed(long[] workloads, long[] deadlines) {
        if (workloads == null || deadlines == null || workloads.length != deadlines.length || workloads.length == 0) {
            return -1;
        }

        int n = workloads.length;

        // Immediate impossibility check:
        // Even with arbitrarily large speed, each batch still takes at least 1 minute,
        // because time = ceil(workload / S) and workload >= 1.
        //
        // Therefore, after processing the first (i + 1) batches, the absolute minimum
        // possible cumulative time is (i + 1) minutes.
        //
        // If any deadline[i] is smaller than (i + 1), then no finite speed can ever work.
        for (int i = 0; i < n; i++) {
            if (deadlines[i] < (long) (i + 1)) {
                return -1;
            }
        }

        // We now know a solution may exist.
        //
        // Binary search needs a search interval [low, high] such that:
        // - low is definitely possible candidate space start
        // - high is guaranteed feasible
        //
        // low starts at 1 because speed must be positive.
        long low = 1L;

        // To find a guaranteed feasible upper bound, we use exponential search:
        // start with high = 1 and keep doubling until it becomes feasible.
        //
        // This avoids having to derive a complicated exact upper bound and is safe
        // because if a solution exists, some finite speed must be feasible.
        long high = 1L;
        while (!isFeasible(workloads, deadlines, high)) {
            // Prevent overflow while doubling.
            // If high is already huge and still not feasible, but we passed the earlier
            // impossibility check, in practice a feasible speed must exist before overflow.
            // Still, we guard carefully.
            if (high > Long.MAX_VALUE / 2) {
                // As a final fallback, test Long.MAX_VALUE.
                high = Long.MAX_VALUE;
                if (!isFeasible(workloads, deadlines, high)) {
                    return -1;
                }
                break;
            }
            high *= 2;
        }

        // Standard binary search for the first feasible speed.
        //
        // Invariant:
        // - There exists at least one feasible speed in [low, high]
        while (low < high) {
            long mid = low + (high - low) / 2;

            if (isFeasible(workloads, deadlines, mid)) {
                // mid works, so the answer is in [low, mid]
                high = mid;
            } else {
                // mid does not work, so the answer is in [mid + 1, high]
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether a given processor speed is sufficient to finish all batches
     * by their deadlines.
     *
     * For each batch:
     * - processing time = ceil(workloads[i] / speed)
     * - cumulative finish time is updated
     * - if cumulative finish time exceeds deadlines[i], the speed is not feasible
     *
     * Important implementation details:
     * - Ceil division is computed as (a + b - 1) / b, but that can overflow for very large values.
     * - Since workloads[i] <= 10^12 and speed >= 1, overflow is not an issue here,
     *   but we still use a safer equivalent form:
     *     a / b + (a % b == 0 ? 0 : 1)
     * - Cumulative time can grow large, so we guard against overflow by capping early
     *   whenever it already exceeds the current deadline.
     *
     * @param workloads the compute units required by each batch
     * @param deadlines the deadline for each prefix completion time
     * @param speed the processor speed to test
     * @return true if the speed is feasible, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean isFeasible(long[] workloads, long[] deadlines, long speed) {
        if (speed <= 0) {
            return false;
        }

        long cumulativeTime = 0L;

        for (int i = 0; i < workloads.length; i++) {
            // Compute ceil(workloads[i] / speed) safely.
            long batchTime = ceilDiv(workloads[i], speed);

            // Before adding batchTime, check whether cumulativeTime + batchTime would exceed deadline[i].
            // This avoids overflow and allows early exit.
            //
            // If cumulativeTime > deadlines[i] - batchTime, then:
            // cumulativeTime + batchTime > deadlines[i]
            if (cumulativeTime > deadlines[i] - batchTime) {
                return false;
            }

            cumulativeTime += batchTime;

            // This explicit check is logically redundant after the guarded addition above,
            // but it improves readability for beginners.
            if (cumulativeTime > deadlines[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Computes ceil(a / b) for positive long integers.
     *
     * Uses:
     *   a / b + (a % b == 0 ? 0 : 1)
     * instead of:
     *   (a + b - 1) / b
     * to avoid possible overflow in the addition.
     *
     * @param a the numerator, expected to be positive
     * @param b the denominator, expected to be positive
     * @return the ceiling of a divided by b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long ceilDiv(long a, long b) {
        long quotient = a / b;
        long remainder = a % b;
        return quotient + (remainder == 0 ? 0 : 1);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called algorithm work
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] workloads1 = {7, 11, 5};
        long[] deadlines1 = {4, 8, 10};
        System.out.println(solution.minimumProcessorSpeed(workloads1, deadlines1)); // Expected: 3

        long[] workloads2 = {9, 9, 9};
        long[] deadlines2 = {0, 5, 8};
        System.out.println(solution.minimumProcessorSpeed(workloads2, deadlines2)); // Expected: -1

        // Additional quick sanity checks.
        long[] workloads3 = {1};
        long[] deadlines3 = {1};
        System.out.println(solution.minimumProcessorSpeed(workloads3, deadlines3)); // Expected: 1

        long[] workloads4 = {1000000000000L, 1000000000000L};
        long[] deadlines4 = {1000000000000L, 2000000000000L};
        System.out.println(solution.minimumProcessorSpeed(workloads4, deadlines4)); // Expected: 1
    }
}