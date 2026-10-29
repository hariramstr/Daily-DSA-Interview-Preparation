/*
Minimum Initial Fuel for Timed Delivery Route

Problem Description:
A courier drone must complete a fixed delivery route consisting of n checkpoints in order,
from checkpoint 0 to checkpoint n - 1.

To travel from checkpoint i to checkpoint i + 1, the drone consumes fuel equal to cost[i].
At some checkpoints, the drone may also recharge by collecting a battery pack worth refill[i]
units immediately after arriving there.

The drone starts at checkpoint 0 with an unknown initial amount of fuel F, and it may never
let its fuel drop below 0 at any point.

However, there is an additional timing rule. Each segment i has a maximum allowed carry limit
limit[i]: if the drone begins segment i with more than limit[i] fuel, it becomes too heavy and
misses the time window for that segment. In other words, just before paying cost[i], the current
fuel must be between 0 and limit[i], inclusive.

You must compute the minimum integer initial fuel F that allows the drone to complete the entire
route, or return -1 if no such F exists.

The route is fixed and battery packs cannot be skipped if collected; upon arriving at checkpoint
i + 1, the drone's fuel increases by refill[i + 1]. Assume refill[0] is also available before
starting, so the drone begins with F + refill[0] fuel and must still satisfy the carry limit
for segment 0.

Constraints:
- 1 <= n <= 200000
- cost.length == n - 1
- refill.length == n
- limit.length == n - 1
- 0 <= cost[i], refill[i], limit[i] <= 10^18
- The answer, if it exists, fits in a signed 64-bit integer

Intended approach:
Use binary search on the initial fuel F, and for each candidate F perform a linear simulation
to check whether the route is feasible.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the minimum initial fuel F that allows the drone to complete the route.
     *
     * Core idea:
     * 1. If a route is feasible with some initial fuel F, then it is also feasible with any larger
     *    initial fuel? Not always directly, because larger fuel may violate a carry limit.
     *
     *    However, due to the deterministic process:
     *    currentFuelBeforeSegment(i) = F + prefixContribution(i),
     *    where prefixContribution(i) depends only on refills and costs before i.
     *
     *    Therefore every segment imposes:
     *      costNeed(i) <= F + prefixContribution(i) <= limit[i]
     *
     *    This means each segment contributes an interval of valid F values.
     *    The overall valid F values are the intersection of all these intervals.
     *    So the minimum feasible F is simply the left endpoint of the final intersection.
     *
     * 2. The problem statement asks for binary search + linear feasibility check. We provide that
     *    exact approach. The feasibility check is monotone over the interval intersection's lower
     *    side when we search for the smallest F inside the feasible region. To guarantee correctness,
     *    we first compute a safe upper bound from the carry limits:
     *      F + refill[0] <= limit[0]  =>  F <= limit[0] - refill[0]
     *    If this upper bound is negative, no answer exists.
     *
     * 3. We binary search F in [0, upperBound]. For each candidate, we simulate the route.
     *
     * Important note:
     * Because carry limits can make very large F invalid, we do not search beyond the maximum
     * possible valid starting fuel implied by segment 0.
     *
     * @param cost   fuel cost for each segment; cost[i] is used from checkpoint i to i + 1
     * @param refill refill gained at each checkpoint; refill[0] is collected before starting
     * @param limit  maximum allowed fuel before each segment
     * @return the minimum integer initial fuel F, or -1 if impossible
     * Time complexity: O(n log U), where U is the binary search range size
     * Space complexity: O(1) extra space
     */
    public long minimumInitialFuel(long[] cost, long[] refill, long[] limit) {
        int n = refill.length;

        // Special case:
        // If there is only one checkpoint, there are no segments to travel.
        // The route is already complete. The minimum initial fuel is 0.
        if (n == 1) {
            return 0L;
        }

        // The drone starts with F + refill[0] fuel before segment 0.
        // To satisfy the carry limit for segment 0:
        //   F + refill[0] <= limit[0]
        // Therefore:
        //   F <= limit[0] - refill[0]
        //
        // This gives a natural upper bound for any feasible answer.
        long upperBound = limit[0] - refill[0];
        if (upperBound < 0) {
            return -1L;
        }

        // Before binary search, quickly check whether ANY feasible F exists at all.
        // If even the largest possible valid F for segment 0 cannot complete the route,
        // then no answer exists.
        if (!canComplete(upperBound, cost, refill, limit)) {
            return -1L;
        }

        long left = 0L;
        long right = upperBound;
        long answer = upperBound;

        // Standard binary search for the smallest feasible initial fuel.
        while (left <= right) {
            long mid = left + ((right - left) >>> 1);

            if (canComplete(mid, cost, refill, limit)) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given initial fuel F allows the drone to complete the route.
     *
     * Simulation details:
     * - Before segment 0, the drone has F + refill[0].
     * - For each segment i:
     *   1. Current fuel must be <= limit[i]
     *   2. Current fuel must be >= cost[i] so the drone can pay the travel cost
     *   3. After paying cost[i], fuel decreases
     *   4. Upon arrival at checkpoint i + 1, refill[i + 1] is added immediately
     *
     * Overflow safety:
     * Values can be up to 1e18, and repeated additions could overflow long if done carelessly.
     * The problem guarantees the answer fits in signed 64-bit integer, and our search range keeps
     * F bounded. Still, during simulation we use safe addition logic where appropriate.
     *
     * @param initialFuel the candidate starting fuel F before collecting refill[0]
     * @param cost        fuel cost for each segment
     * @param refill      refill gained at each checkpoint
     * @param limit       maximum allowed fuel before each segment
     * @return true if the route can be completed with this initial fuel, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canComplete(long initialFuel, long[] cost, long[] refill, long[] limit) {
        int n = refill.length;

        // Fuel before the first segment includes the starting refill at checkpoint 0.
        long fuel = safeAdd(initialFuel, refill[0]);

        // Process each segment in order.
        for (int i = 0; i < n - 1; i++) {
            // Rule 1: the drone must not exceed the carry limit before starting this segment.
            if (fuel > limit[i]) {
                return false;
            }

            // Rule 2: the drone must have enough fuel to pay the segment cost.
            if (fuel < cost[i]) {
                return false;
            }

            // Pay the travel cost.
            fuel -= cost[i];

            // Arrive at the next checkpoint and collect its refill immediately.
            fuel = safeAdd(fuel, refill[i + 1]);
        }

        return true;
    }

    /**
     * Safely adds two non-negative long values.
     *
     * In this problem all fuel-related values are non-negative. If an addition would overflow,
     * we clamp to Long.MAX_VALUE. This is sufficient because any overflowed value would only mean
     * "extremely large fuel", which will fail a future carry-limit check anyway if relevant.
     *
     * @param a first non-negative operand
     * @param b second non-negative operand
     * @return a + b if it fits in long, otherwise Long.MAX_VALUE
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long safeAdd(long a, long b) {
        if (Long.MAX_VALUE - a < b) {
            return Long.MAX_VALUE;
        }
        return a + b;
    }

    /**
     * Convenience helper to build a long array from literals in the demo.
     *
     * @param values values to place into the array
     * @return a long array containing the provided values
     * Time complexity: O(k), where k is the number of provided values
     * Space complexity: O(k)
     */
    public static long[] arr(long... values) {
        return values;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 3
     * Example 2 -> -1
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demo size, excluding algorithm calls
     * Space complexity: O(1) extra space, excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        long[] cost1 = arr(5, 4, 3);
        long[] refill1 = arr(2, 6, 1, 0);
        long[] limit1 = arr(10, 8, 7);
        long result1 = solution.minimumInitialFuel(cost1, refill1, limit1);
        System.out.println(result1); // Expected: 3

        // Example 2
        long[] cost2 = arr(4, 4);
        long[] refill2 = arr(5, 10, 0);
        long[] limit2 = arr(3, 20);
        long result2 = solution.minimumInitialFuel(cost2, refill2, limit2);
        System.out.println(result2); // Expected: -1
    }
}