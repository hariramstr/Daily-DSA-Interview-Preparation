import java.util.*;

/*
Problem Title: Minimum Router Count for Deadline-Limited Packet Waves

Problem Description:
A data center receives packet waves in a fixed order. The i-th wave contains packets[i] packets and arrives at time i.
All packets from wave i must be fully processed no later than deadline[i], where deadline is a non-decreasing array
and deadline[i] >= i.

You may deploy k identical routers. Each router can process exactly 1 packet per unit of time, can work on at most
one wave at a time, and packet processing is preemptive: a router may stop processing one wave and later continue the
same or another wave. However, packets cannot be processed before their wave arrives.

Your task is to find the minimum number of routers needed so that every wave can be completed by its deadline.

Formally, at any real time t, at most k packets total can be processed across all routers per unit time, and only
packets from waves with arrival time <= t may be processed. For every prefix of waves 0..i, the total amount of work
finished by time deadline[i] must be enough to complete those waves.

Return the smallest integer k that makes the schedule feasible.

Key idea:
- Feasibility is monotone: if k routers are enough, then any larger number is also enough.
- Therefore we can binary search the answer.
- The main challenge is the feasibility check.

Correct feasibility condition:
For every suffix of waves ending at i, say waves j..i, all of that work must be processed inside the time window
[j, deadline[i]], because:
- wave x in [j..i] cannot start before time x >= j
- all those waves must finish by time deadline[i] because deadline is non-decreasing

Thus for every pair j <= i:
    sum(packets[j..i]) <= k * (deadline[i] - j + 1)

Equivalently:
    k >= sum(packets[j..i]) / (deadline[i] - j + 1)

We need the maximum such ratio over all j, i.

Rewriting:
Let prefix[t] = packets[0] + ... + packets[t-1], with prefix[0] = 0.
Then:
    sum(packets[j..i]) = prefix[i+1] - prefix[j]

So for fixed i:
    prefix[i+1] - prefix[j] <= k * (deadline[i] - j + 1)
    prefix[i+1] - k * (deadline[i] + 1) <= prefix[j] - k * j

Therefore for each i we only need:
    prefix[i+1] - k * (deadline[i] + 1) <= min over j in [0..i] of (prefix[j] - k * j)

This gives an O(n) feasibility check for a fixed k.

Then binary search over k.

This implementation uses long everywhere because values can be large.
*/
public class Solution {

    /**
     * Computes the minimum number of routers needed so that all packet waves can be processed
     * by their deadlines.
     *
     * Algorithm:
     * 1. Binary search on the answer k.
     * 2. For each candidate k, run a linear feasibility check.
     *
     * Why binary search works:
     * - If k routers are sufficient, then k+1, k+2, ... are also sufficient.
     * - This monotonic property allows binary search.
     *
     * @param packets  packets[i] is the number of packets in wave i
     * @param deadline deadline[i] is the latest time by which wave i must be fully processed
     * @return the smallest feasible number of routers
     * Time complexity: O(n log A), where A is the answer range (fits in 64-bit signed integer)
     * Space complexity: O(n) for prefix sums
     */
    public long minimumRouterCount(int[] packets, int[] deadline) {
        int n = packets.length;

        // Build prefix sums:
        // prefix[0] = 0
        // prefix[i+1] = packets[0] + ... + packets[i]
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + packets[i];
        }

        // Find an upper bound for binary search by doubling.
        // Start with 1 router and keep doubling until it becomes feasible.
        long low = 1;
        long high = 1;
        while (!isFeasible(packets, deadline, prefix, high)) {
            high <<= 1;
        }

        // Standard binary search for the first feasible value.
        while (low < high) {
            long mid = low + ((high - low) >>> 1);
            if (isFeasible(packets, deadline, prefix, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether k routers are enough.
     *
     * Detailed derivation:
     * For every i and every j in [0..i], waves j..i must all fit in the interval [j, deadline[i]].
     * That interval has length:
     *     deadline[i] - j + 1
     * With k routers, total capacity in that interval is:
     *     k * (deadline[i] - j + 1)
     * Required work is:
     *     prefix[i+1] - prefix[j]
     *
     * So feasibility requires:
     *     prefix[i+1] - prefix[j] <= k * (deadline[i] - j + 1)
     *
     * Rearranging:
     *     prefix[i+1] - k * (deadline[i] + 1) <= prefix[j] - k * j
     *
     * For fixed i, the right side only needs the minimum value among j = 0..i:
     *     minValue = min(prefix[j] - k * j)
     *
     * Then we check:
     *     prefix[i+1] - k * (deadline[i] + 1) <= minValue
     *
     * We scan i from left to right while maintaining the running minimum of:
     *     prefix[j] - k * j
     *
     * @param packets  packets array
     * @param deadline deadline array
     * @param prefix   prefix sums of packets
     * @param k        candidate number of routers
     * @return true if k routers are sufficient, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1) extra beyond the provided prefix array
     */
    public boolean isFeasible(int[] packets, int[] deadline, long[] prefix, long k) {
        int n = packets.length;

        // This variable stores:
        // min(prefix[j] - k * j) for all j processed so far.
        //
        // Initially, before processing any i, the only valid j is 0.
        long minValue = prefix[0] - k * 0L; // equals 0

        for (int i = 0; i < n; i++) {
            // Left side of the transformed inequality for this i:
            // prefix[i+1] - k * (deadline[i] + 1)
            long need = prefix[i + 1] - k * ((long) deadline[i] + 1L);

            // If need > minValue, then for every j in [0..i]:
            // prefix[i+1] - k*(deadline[i]+1) > prefix[j] - k*j
            // which means some suffix constraint is violated.
            if (need > minValue) {
                return false;
            }

            // Now include j = i+1 for future iterations.
            // For the next positions, j may be any value up to i+1.
            long candidate = prefix[i + 1] - k * (long) (i + 1);
            if (candidate < minValue) {
                minValue = candidate;
            }
        }

        return true;
    }

    /**
     * Convenience overload that accepts arrays and internally builds prefix sums for feasibility.
     * Useful for demonstrations or direct testing of a specific k.
     *
     * @param packets  packets array
     * @param deadline deadline array
     * @param k        candidate number of routers
     * @return true if k routers are sufficient, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public boolean isFeasible(int[] packets, int[] deadline, long k) {
        int n = packets.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + packets[i];
        }
        return isFeasible(packets, deadline, prefix, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1: 2
     * Example 2: 8
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n log A) across the examples
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] packets1 = {3, 2, 4};
        int[] deadline1 = {2, 3, 5};
        long answer1 = solution.minimumRouterCount(packets1, deadline1);
        System.out.println(answer1); // Expected: 2

        int[] packets2 = {5, 6, 4};
        int[] deadline2 = {1, 2, 2};
        long answer2 = solution.minimumRouterCount(packets2, deadline2);
        System.out.println(answer2); // Expected: 8

        // Additional quick sanity checks:
        // Single wave: need enough routers to finish packets[0] within deadline[0] - 0 + 1 time units.
        int[] packets3 = {10};
        int[] deadline3 = {4};
        long answer3 = solution.minimumRouterCount(packets3, deadline3);
        System.out.println(answer3); // Expected: ceil(10 / 5) = 2

        // Tight arrival/deadline case:
        // wave 0 at time 0, wave 1 at time 1, both due by 1
        // total work 3 in window [0,1] of length 2 => need 2 routers
        // but wave 1 alone has 1 packet in [1,1], okay with 1 router for that suffix
        int[] packets4 = {2, 1};
        int[] deadline4 = {1, 1};
        long answer4 = solution.minimumRouterCount(packets4, deadline4);
        System.out.println(answer4); // Expected: 2
    }
}