import java.util.*;

/*
Problem Title: Minimum Processing Power for Alternating GPU Batches

Problem Description:
A machine learning platform must execute a sequence of training batches in the given order.
The i-th batch requires work[i] units of computation. You are provisioning identical GPU nodes,
each with the same processing power P. A single node can process a contiguous group of batches,
and the time needed for that group is the sum of its work values divided by P.

Because of thermal balancing rules, nodes are assigned in alternating modes: the 1st used node
is in "hot" mode, the 2nd in "cool" mode, the 3rd in "hot" mode again, and so on. A hot-mode
node may be assigned batches whose total work is at most hotLimit, while a cool-mode node may
be assigned batches whose total work is at most coolLimit. You may split the batch list into
any number of contiguous groups, but the mode of each group is determined by its position among
the groups. Every group must respect both its mode limit and the node's processing power deadline:
its processing time must be at most T, meaning groupWork <= P * T.

Return the minimum integer processing power P such that all batches can be completed using at most m nodes.

If it is impossible for any processing power to satisfy the alternating mode limits, return -1.

Constraints:
- 1 <= n == work.length <= 200000
- 1 <= work[i] <= 10^9
- 1 <= m <= 200000
- 1 <= hotLimit, coolLimit <= 10^18
- 1 <= T <= 10^9
- P must be a positive integer

Notes:
- Each node handles a contiguous segment of the batch array.
- The first segment uses hot mode, the second cool mode, the 3rd hot mode, etc.
- For a fixed P, feasibility is monotonic, so an O(n log answer) solution is expected.
*/

public class Solution {

    /**
     * Computes the minimum integer processing power P such that the batch array can be partitioned
     * into at most m contiguous non-empty groups, where:
     * 1) Group 1 uses hot mode, group 2 uses cool mode, group 3 hot, ...
     * 2) Each group's sum must be <= its mode limit.
     * 3) Each group's sum must also be <= P * T.
     *
     * If no processing power can ever make the schedule valid, returns -1.
     *
     * Core idea:
     * - For a fixed P, the effective capacity of a hot group is min(hotLimit, P*T),
     *   and the effective capacity of a cool group is min(coolLimit, P*T).
     * - Feasibility is monotonic in P, so we binary search the answer.
     * - The difficult part is checking whether the array can be covered by at most m alternating groups.
     *
     * Feasibility check:
     * - We greedily build the farthest possible partition for every possible number of groups up to m,
     *   but we do it efficiently using dynamic programming on "how far can we reach".
     * - Let reach[k] be the farthest prefix length that can be covered using exactly k groups.
     * - Since the mode of group k is fixed by parity, from reach[k-1] we can extend one more group
     *   using the corresponding capacity.
     * - Because capacities alternate between only two values, and reach[k] is nondecreasing,
     *   we can compute each transition in O(1) amortized with precomputed next positions via two pointers.
     *
     * @param work the work required by each batch, in order
     * @param m the maximum number of nodes/groups allowed
     * @param hotLimit maximum total work allowed for a hot-mode group
     * @param coolLimit maximum total work allowed for a cool-mode group
     * @param T the time limit per node; each group must satisfy groupWork <= P * T
     * @return the minimum positive integer processing power P, or -1 if impossible
     * Time complexity: O(n log U), where U is the searched answer range
     * Space complexity: O(n)
     */
    public long minimumProcessingPower(int[] work, int m, long hotLimit, long coolLimit, long T) {
        int n = work.length;

        // Absolute impossibility check:
        // Even with arbitrarily large P, the time constraint disappears, so only alternating mode limits remain.
        // Since the first group is hot, every individual batch that appears in a hot-positioned singleton group
        // must be <= hotLimit if needed. But more fundamentally, if work[0] > hotLimit, the first non-empty group
        // cannot even start. More generally, our full feasibility check with "infinite" P will detect impossibility.
        long infiniteP = upperBoundPower(work, T);
        if (!canFinish(work, m, hotLimit, coolLimit, T, infiniteP)) {
            return -1L;
        }

        long left = 1L;
        long right = infiniteP;

        // Standard binary search on the minimum feasible processing power.
        while (left < right) {
            long mid = left + ((right - left) >>> 1);
            if (canFinish(work, m, hotLimit, coolLimit, T, mid)) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Checks whether a given processing power P is sufficient.
     *
     * Detailed logic:
     * - A hot group can carry at most hotCap = min(hotLimit, P*T).
     * - A cool group can carry at most coolCap = min(coolLimit, P*T).
     * - We must partition the array into at most m non-empty contiguous groups,
     *   where group 1 uses hotCap, group 2 uses coolCap, etc.
     *
     * Efficient DP:
     * - Let nextHot[i] = farthest index j such that the subarray work[i..j-1] fits in one hot group.
     * - Let nextCool[i] = farthest index j such that the subarray work[i..j-1] fits in one cool group.
     * - Then:
     *      reach[0] = 0
     *      reach[k] = nextHot[reach[k-1]] if k is odd
     *      reach[k] = nextCool[reach[k-1]] if k is even
     * - If for some k <= m, reach[k] == n, then feasible.
     *
     * Why this greedy transition is correct:
     * - For a fixed starting index and fixed capacity, taking the longest possible valid group is always optimal
     *   for maximizing covered prefix after that group.
     * - Since each next group's mode is predetermined by parity, the best state after exactly k groups is simply
     *   the farthest prefix reachable by repeatedly taking the longest valid group each time.
     *
     * @param work the work array
     * @param m maximum number of groups
     * @param hotLimit hot-mode limit
     * @param coolLimit cool-mode limit
     * @param T time limit
     * @param P candidate processing power
     * @return true if P is sufficient, false otherwise
     * Time complexity: O(n + m)
     * Space complexity: O(n)
     */
    public boolean canFinish(int[] work, int m, long hotLimit, long coolLimit, long T, long P) {
        long timeCap;
        if (P > Long.MAX_VALUE / T) {
            timeCap = Long.MAX_VALUE;
        } else {
            timeCap = P * T;
        }

        long hotCap = Math.min(hotLimit, timeCap);
        long coolCap = Math.min(coolLimit, timeCap);

        int n = work.length;

        // If any single batch exceeds both possible capacities for the position where it might be needed,
        // the precomputed transitions will naturally fail. Still, a quick first-batch check is useful:
        // the first group is hot and must be non-empty.
        if ((long) work[0] > hotCap) {
            return false;
        }

        int[] nextHot = buildNext(work, hotCap);
        int[] nextCool = buildNext(work, coolCap);

        int reach = 0;

        // We try using exactly 1, 2, ..., m groups.
        // The moment we cover all n batches, we are done.
        for (int groups = 1; groups <= m; groups++) {
            if ((groups & 1) == 1) {
                // Odd-numbered group => hot mode.
                reach = nextHot[reach];
            } else {
                // Even-numbered group => cool mode.
                reach = nextCool[reach];
            }

            if (reach == n) {
                return true;
            }

            // If we cannot advance at all, adding more groups later will never help,
            // because every group must be non-empty.
            if (groups < m) {
                int nextReach = ((groups + 1) & 1) == 1 ? nextHot[reach] : nextCool[reach];
                if (nextReach == reach && reach < n) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Builds an array next[] where next[i] is the farthest index j (i <= j <= n)
     * such that the contiguous subarray work[i..j-1] has sum <= cap.
     *
     * This is computed with the classic sliding window / two-pointer technique:
     * - Maintain a window [i, r) whose sum is <= cap.
     * - For each i, extend r as far as possible.
     * - Record next[i] = r.
     * - Then remove work[i] before moving to i+1.
     *
     * Important:
     * - If work[i] > cap, then next[i] == i, meaning no non-empty group can start at i under this cap.
     *
     * @param work the work array
     * @param cap maximum allowed sum for one group
     * @return next-position array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildNext(int[] work, long cap) {
        int n = work.length;
        int[] next = new int[n + 1];
        next[n] = n;

        long sum = 0L;
        int r = 0;

        for (int i = 0; i < n; i++) {
            // Ensure the right pointer is at least i.
            if (r < i) {
                r = i;
                sum = 0L;
            }

            // Extend the window as far as possible while staying within the capacity.
            while (r < n && sum + work[r] <= cap) {
                sum += work[r];
                r++;
            }

            // Record the farthest reachable end for a group starting at i.
            next[i] = r;

            // Before moving i forward, remove work[i] from the current window if it is inside.
            if (r > i) {
                sum -= work[i];
            }
        }

        return next;
    }

    /**
     * Computes a safe upper bound for the answer.
     *
     * We need a value of P large enough that increasing P further cannot help with the time constraint.
     * Once P*T >= totalWork, the time limit is effectively irrelevant for every possible group,
     * because no group can exceed totalWork anyway.
     *
     * Therefore, it is enough to choose:
     *     P >= ceil(totalWork / T)
     *
     * This guarantees that P*T >= totalWork.
     *
     * @param work the work array
     * @param T time limit
     * @return a safe upper bound for binary search
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long upperBoundPower(int[] work, long T) {
        long total = 0L;
        for (int x : work) {
            total += x;
        }
        return Math.max(1L, ceilDiv(total, T));
    }

    /**
     * Computes ceil(a / b) for positive long values.
     *
     * @param a numerator
     * @param b denominator
     * @return ceiling of a divided by b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 6
     * Example 2 -> -1
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n log U) per demonstration call
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int[] work1 = {8, 5, 6, 4, 7};
        int m1 = 3;
        long hotLimit1 = 13L;
        long coolLimit1 = 11L;
        long T1 = 2L;
        System.out.println(sol.minimumProcessingPower(work1, m1, hotLimit1, coolLimit1, T1)); // 6

        int[] work2 = {9, 9, 9};
        int m2 = 2;
        long hotLimit2 = 8L;
        long coolLimit2 = 20L;
        long T2 = 3L;
        System.out.println(sol.minimumProcessingPower(work2, m2, hotLimit2, coolLimit2, T2)); // -1
    }
}