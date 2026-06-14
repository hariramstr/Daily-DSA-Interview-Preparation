import java.util.*;

/*
Problem Title: Maximum Uniform Delay for Train Departures

Problem Description:
A railway operator has scheduled n trains to depart in nondecreasing order of planned times,
given by an integer array departures where departures[i] is the planned departure minute of
the i-th train. Due to maintenance, each train may be delayed by any integer number of minutes
from 0 up to maxDelay, independently of the others. After delays are chosen, the actual departure
times must still be strictly increasing, and the gap between any two consecutive actual departures
must be at least gap minutes.

Your task is to compute the maximum integer value of gap such that it is possible to assign delays
to all trains while respecting the delay limit maxDelay.

Formally, choose integers actual[i] such that:
1. departures[i] <= actual[i] <= departures[i] + maxDelay
2. actual[i] < actual[i + 1] for all valid i
3. actual[i + 1] - actual[i] >= gap for all valid i

Return the largest feasible gap.

This is an optimization problem where the answer is not constructed directly. Instead, you must
determine whether a candidate gap is feasible and use that to search for the maximum valid answer
efficiently.

Constraints:
- 2 <= n <= 200000
- 0 <= departures[i] <= 10^14
- departures is sorted in nondecreasing order
- 0 <= maxDelay <= 10^14
- The answer fits in a 64-bit signed integer

Example 1:
Input: departures = [2, 4, 7], maxDelay = 3
Output: 4
Explanation: One valid assignment is actual = [2, 6, 10]. Each train is delayed by at most
3 minutes, and consecutive gaps are 4 and 4. A gap of 5 is impossible because after placing
the first train no later choices can keep every train within its allowed window.

Example 2:
Input: departures = [1, 1, 1, 1], maxDelay = 5
Output: 1
Explanation: The trains all start with the same planned time, but each can be shifted within [1, 6].
A valid assignment is [1, 2, 3, 4], giving minimum consecutive gap 1. A gap of 2 would require
times [1, 3, 5, 7], but 7 exceeds the allowed latest departure time of 6 for the last train.
*/
public class Solution {

    /**
     * Computes the maximum feasible minimum gap between consecutive actual train departures.
     *
     * Core idea:
     * - For a fixed candidate gap g, we check whether it is possible to assign actual departure
     *   times so that every train stays within its allowed interval [departures[i], departures[i] + maxDelay]
     *   and every consecutive pair differs by at least g.
     * - This feasibility check is monotonic:
     *   if a gap g is feasible, then every smaller gap is also feasible.
     *   Therefore, we can binary search for the largest feasible gap.
     *
     * Greedy feasibility strategy:
     * - Always place each train as early as possible while respecting:
     *   1) its own earliest allowed time,
     *   2) the required gap from the previous actual departure.
     * - If even this earliest valid placement exceeds the train's latest allowed time,
     *   then the candidate gap is impossible.
     *
     * @param departures sorted array of planned departure times
     * @param maxDelay maximum allowed delay for each train
     * @return the largest integer gap that can be achieved
     * Time complexity: O(n log R), where n is the number of trains and R is the search range of the answer
     * Space complexity: O(1) extra space
     */
    public long maximumUniformDelay(long[] departures, long maxDelay) {
        int n = departures.length;

        /*
         * We binary search on the answer.
         *
         * Lower bound:
         * - 0 is always a safe lower bound for the search space.
         *
         * Upper bound:
         * - A very safe upper bound is:
         *   (latest possible time of last train) - (earliest possible time of first train)
         *   because no single consecutive gap can exceed the total span.
         *
         * latest possible time of last train = departures[n - 1] + maxDelay
         * earliest possible time of first train = departures[0]
         */
        long low = 0L;
        long high = (departures[n - 1] + maxDelay) - departures[0];
        long answer = 0L;

        while (low <= high) {
            long mid = low + (high - low) / 2L;

            if (canAchieveGap(departures, maxDelay, mid)) {
                answer = mid;
                low = mid + 1L;
            } else {
                high = mid - 1L;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given minimum gap is feasible.
     *
     * Detailed greedy reasoning:
     * - For the first train, the best choice is to place it at its earliest possible time:
     *   actual[0] = departures[0]
     *   This is optimal because placing a train earlier never hurts future trains when we are trying
     *   to maintain a minimum gap; it only gives more room to later trains.
     *
     * - For each next train i:
     *   It must be at least:
     *     a) departures[i]          -> because it cannot leave before its planned time
     *     b) previousActual + gap   -> because we need at least the candidate gap
     *
     *   Therefore the earliest valid choice is:
     *     actual[i] = max(departures[i], previousActual + gap)
     *
     * - If this chosen time is greater than departures[i] + maxDelay, then train i cannot be placed
     *   inside its allowed window, so the candidate gap is impossible.
     *
     * Because we always choose the earliest feasible time for each train, this greedy process is correct:
     * if the earliest possible placement fails, any later placement would only make future constraints harder.
     *
     * @param departures sorted array of planned departure times
     * @param maxDelay maximum allowed delay for each train
     * @param gap candidate minimum gap to test
     * @return true if the candidate gap can be achieved, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean canAchieveGap(long[] departures, long maxDelay, long gap) {
        /*
         * Place the first train at the earliest possible time.
         * This maximizes flexibility for all later trains.
         */
        long previousActual = departures[0];

        /*
         * Process each remaining train from left to right.
         */
        for (int i = 1; i < departures.length; i++) {
            long earliestAllowed = departures[i];
            long latestAllowed = departures[i] + maxDelay;

            /*
             * To satisfy the candidate gap, this train must depart no earlier than:
             * previousActual + gap
             *
             * But it also cannot depart before its own planned time.
             *
             * So the earliest valid actual departure is the maximum of those two values.
             */
            long requiredTime = previousActual + gap;
            long currentActual = Math.max(earliestAllowed, requiredTime);

            /*
             * If even the earliest valid placement is beyond the latest allowed time,
             * then this candidate gap is impossible.
             */
            if (currentActual > latestAllowed) {
                return false;
            }

            /*
             * Otherwise, commit this earliest feasible placement and continue.
             */
            previousActual = currentActual;
        }

        return true;
    }

    /**
     * Convenience overload for users who may have int arrays.
     *
     * @param departures sorted array of planned departure times as int values
     * @param maxDelay maximum allowed delay for each train
     * @return the largest integer gap that can be achieved
     * Time complexity: O(n log R)
     * Space complexity: O(n) due to conversion to long[]
     */
    public long maximumUniformDelay(int[] departures, long maxDelay) {
        long[] converted = new long[departures.length];
        for (int i = 0; i < departures.length; i++) {
            converted[i] = departures[i];
        }
        return maximumUniformDelay(converted, maxDelay);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 4
     * - Example 2: 1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration inputs, excluding the called algorithm
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] departures1 = {2, 4, 7};
        long maxDelay1 = 3;
        long result1 = solution.maximumUniformDelay(departures1, maxDelay1);
        System.out.println(result1);

        long[] departures2 = {1, 1, 1, 1};
        long maxDelay2 = 5;
        long result2 = solution.maximumUniformDelay(departures2, maxDelay2);
        System.out.println(result2);

        /*
         * Quick internal reasoning check for Example 1:
         * departures = [2, 4, 7], maxDelay = 3
         *
         * gap = 4:
         * train 0 -> 2
         * train 1 -> max(4, 2 + 4) = 6   within [4, 7]
         * train 2 -> max(7, 6 + 4) = 10  within [7, 10]
         * feasible
         *
         * gap = 5:
         * train 0 -> 2
         * train 1 -> max(4, 7) = 7       within [4, 7]
         * train 2 -> max(7, 12) = 12     exceeds [7, 10]
         * impossible
         *
         * Therefore answer = 4.
         *
         * Quick internal reasoning check for Example 2:
         * departures = [1, 1, 1, 1], maxDelay = 5
         * allowed interval for each train is [1, 6]
         *
         * gap = 1:
         * 1, 2, 3, 4 -> feasible
         *
         * gap = 2:
         * 1, 3, 5, 7 -> last train exceeds 6 -> impossible
         *
         * Therefore answer = 1.
         */
    }
}