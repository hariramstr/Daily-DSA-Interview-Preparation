/*
 * Title: Minimum Bandwidth Allocation for K Data Streams
 *
 * Problem Description:
 * You are managing a network server that must handle `n` data streams, where each stream
 * has a bandwidth requirement given in the array `streams` (streams[i] is the bandwidth
 * needed by stream i in Mbps). You have exactly `k` network channels available, and you
 * must assign every stream to exactly one channel. Each channel's total load is the sum
 * of bandwidths of all streams assigned to it.
 *
 * To ensure fair distribution and avoid bottlenecks, you want to minimize the maximum
 * total load across all channels. However, there is a constraint: streams must be assigned
 * to channels in contiguous groups (i.e., streams[0..j1] go to channel 1,
 * streams[j1+1..j2] go to channel 2, etc.).
 *
 * Return the minimum possible value of the maximum channel load.
 *
 * Constraints:
 * - 1 <= k <= n <= 10^5
 * - 1 <= streams[i] <= 10^9
 * - It is guaranteed that k <= n
 *
 * Example 1:
 * Input: streams = [10, 20, 30, 40, 50], k = 3
 * Output: 60
 * Explanation: Split as [10, 20, 30], [40], [50]. Loads are 60, 40, 50. Max = 60.
 *
 * Example 2:
 * Input: streams = [7, 2, 5, 10, 8], k = 2
 * Output: 18
 * Explanation: Split as [7, 2, 5] and [10, 8]. Loads are 14 and 18. Max = 18.
 */

import java.util.*;

/**
 * Solution class for the Minimum Bandwidth Allocation for K Data Streams problem.
 *
 * <p>Core Idea (Binary Search on Answer):
 * Instead of directly finding the optimal partition, we binary search on the answer itself.
 * We ask: "Is it possible to split the streams into at most k contiguous groups such that
 * no group's sum exceeds a given value 'mid'?"
 *
 * <p>Binary Search Range:
 * - Lower bound (lo): The maximum single stream value. We can never do better than this
 *   because every stream must be assigned to some channel.
 * - Upper bound (hi): The total sum of all streams. This is the worst case where all
 *   streams go to one channel.
 *
 * <p>Feasibility Check:
 * Given a candidate maximum load 'maxLoad', greedily assign streams to channels:
 * - Keep adding streams to the current channel as long as the sum doesn't exceed maxLoad.
 * - When adding the next stream would exceed maxLoad, start a new channel.
 * - If we need more than k channels, then maxLoad is too small (infeasible).
 * - Otherwise, it's feasible.
 */
public class Solution {

    /**
     * Finds the minimum possible value of the maximum channel load when splitting
     * n streams into exactly k contiguous groups.
     *
     * <p>Algorithm Overview:
     * 1. Binary search on the answer (the maximum load value).
     * 2. For each candidate answer 'mid', check if it's feasible using a greedy approach.
     * 3. Narrow down the search space until we find the minimum feasible value.
     *
     * @param streams array of bandwidth requirements for each stream
     * @param k       number of available network channels
     * @return the minimum possible maximum channel load
     *
     * Time Complexity:  O(n * log(sum)) where sum is the total of all stream values.
     *                   Binary search runs O(log(sum)) iterations, each feasibility
     *                   check is O(n).
     * Space Complexity: O(1) — only a constant number of extra variables are used.
     */
    public int minimumBandwidthAllocation(int[] streams, int k) {
        // -----------------------------------------------------------------------
        // Step 1: Determine the binary search boundaries.
        //
        // The answer must be at least the maximum single stream value (lo),
        // because we cannot split a single stream across channels.
        //
        // The answer is at most the total sum of all streams (hi),
        // because putting everything in one channel always works.
        // -----------------------------------------------------------------------
        long lo = 0;   // Will hold the maximum single element
        long hi = 0;   // Will hold the total sum of all elements

        for (int bandwidth : streams) {
            // lo tracks the largest single stream — we can never go below this
            lo = Math.max(lo, bandwidth);
            // hi accumulates the total sum — the absolute worst-case maximum load
            hi += bandwidth;
        }

        // -----------------------------------------------------------------------
        // Step 2: Binary search on the answer.
        //
        // We search for the smallest value 'mid' such that isFeasible(streams, k, mid)
        // returns true.
        //
        // Invariant:
        //   - Everything < lo is infeasible (too small to accommodate all streams in k groups)
        //   - Everything >= hi is feasible (we can always use 1 channel for everything)
        //   - We want the smallest feasible value.
        // -----------------------------------------------------------------------
        while (lo < hi) {
            // Compute the midpoint without integer overflow (using long arithmetic)
            long mid = lo + (hi - lo) / 2;

            // -----------------------------------------------------------------------
            // Step 3: Check if 'mid' is a feasible maximum load.
            //
            // If feasible: the answer could be 'mid' or something smaller,
            //              so we move the upper bound down to 'mid'.
            // If not feasible: 'mid' is too small, so we move the lower bound up to mid+1.
            // -----------------------------------------------------------------------
            if (isFeasible(streams, k, mid)) {
                // mid works — try to find something smaller
                hi = mid;
            } else {
                // mid doesn't work — we need a larger maximum load
                lo = mid + 1;
            }
        }

        // -----------------------------------------------------------------------
        // Step 4: When lo == hi, we've converged on the minimum feasible value.
        // -----------------------------------------------------------------------
        return (int) lo;
    }

    /**
     * Checks whether it is possible to split the streams into at most k contiguous
     * groups such that no group's total bandwidth exceeds maxLoad.
     *
     * <p>Greedy Strategy:
     * Scan from left to right. Keep adding streams to the current channel.
     * When adding the next stream would exceed maxLoad, "cut" here and start a new channel.
     * Count how many channels are needed. If it's <= k, the split is feasible.
     *
     * @param streams array of bandwidth requirements
     * @param k       maximum number of channels allowed
     * @param maxLoad the candidate maximum load per channel
     * @return true if we can split streams into at most k groups with each group sum <= maxLoad
     *
     * Time Complexity:  O(n) — single pass through the array
     * Space Complexity: O(1) — only a few variables used
     */
    private boolean isFeasible(int[] streams, int k, long maxLoad) {
        // -----------------------------------------------------------------------
        // channelsUsed: how many channels we've opened so far.
        // We always need at least 1 channel, so start at 1.
        // -----------------------------------------------------------------------
        int channelsUsed = 1;

        // -----------------------------------------------------------------------
        // currentLoad: the running sum of bandwidths assigned to the current channel.
        // -----------------------------------------------------------------------
        long currentLoad = 0;

        // -----------------------------------------------------------------------
        // Greedy scan: try to pack as many streams as possible into each channel
        // without exceeding maxLoad.
        // -----------------------------------------------------------------------
        for (int bandwidth : streams) {
            // Check if adding this stream to the current channel would exceed maxLoad
            if (currentLoad + bandwidth > maxLoad) {
                // This stream doesn't fit in the current channel.
                // Open a new channel and assign this stream to it.
                channelsUsed++;
                currentLoad = bandwidth; // New channel starts with just this stream

                // Early termination: if we already need more channels than allowed,
                // this maxLoad value is infeasible — no need to continue.
                if (channelsUsed > k) {
                    return false;
                }
            } else {
                // This stream fits in the current channel — add it.
                currentLoad += bandwidth;
            }
        }

        // If we've processed all streams and used at most k channels, it's feasible.
        return channelsUsed <= k;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // streams = [10, 20, 30, 40, 50], k = 3
        // Expected Output: 60
        //
        // Trace:
        //   lo = max(10,20,30,40,50) = 50
        //   hi = 10+20+30+40+50 = 150
        //
        //   Iteration 1: mid = 50 + (150-50)/2 = 100
        //     isFeasible([10,20,30,40,50], 3, 100)?
        //       ch=1, cur=0
        //       +10 -> cur=10
        //       +20 -> cur=30
        //       +30 -> cur=60
        //       +40 -> cur=100
        //       +50 -> 100+50=150 > 100, open ch=2, cur=50
        //       Result: 2 channels <= 3 → feasible
        //     hi = 100
        //
        //   Iteration 2: mid = 50 + (100-50)/2 = 75
        //     isFeasible([10,20,30,40,50], 3, 75)?
        //       ch=1, cur=0
        //       +10 -> cur=10
        //       +20 -> cur=30
        //       +30 -> cur=60
        //       +40 -> 60+40=100 > 75, open ch=2, cur=40
        //       +50 -> 40+50=90 > 75, open ch=3, cur=50
        //       Result: 3 channels <= 3 → feasible
        //     hi = 75
        //
        //   Iteration 3: mid = 50 + (75-50)/2 = 62
        //     isFeasible([10,20,30,40,50], 3, 62)?
        //       ch=1, cur=0
        //       +10 -> cur=10
        //       +20 -> cur=30
        //       +30 -> cur=60
        //       +40 -> 60+40=100 > 62, open ch=2, cur=40
        //       +50 -> 40+50=90 > 62, open ch=3, cur=50
        //       Result: 3 channels <= 3 → feasible
        //     hi = 62
        //
        //   Iteration 4: mid = 50 + (62-50)/2 = 56
        //     isFeasible([10,20,30,40,50], 3, 56)?
        //       ch=1, cur=0
        //       +10 -> cur=10
        //       +20 -> cur=30
        //       +30 -> 30+30=60 > 56, open ch=2, cur=30
        //       +40 -> 30+40=70 > 56, open ch=3, cur=40
        //       +50 -> 40+50=90 > 56, open ch=4 > 3 → infeasible
        //     lo = 57
        //
        //   Iteration 5: mid = 57 + (62-57)/2 = 59
        //     isFeasible([10,20,30,40,50], 3, 59)?
        //       ch=1, cur=0
        //       +10 -> cur=10
        //       +20 -> cur=30
        //       +30 -> 30+30=60 > 59, open ch=2, cur=30
        //       +40 -> 30+40=70 > 59, open ch=3, cur=40
        //       +50 -> 40+50=90 > 59, open ch=4 > 3 → infeasible
        //     lo = 60
        //
        //   Iteration 6: mid = 60 + (62-60)/2 = 61
        //     isFeasible([10,20,30,40,50], 3, 61)?
        //       ch=1, cur=0
        //       +10 -> cur=10
        //       +20 -> cur=30
        //       +30 -> cur=60
        //       +40 -> 60+40=100 > 61, open ch=2, cur=40
        //       +50 -> 40+50=90 > 61, open ch=3, cur=50
        //       Result: 3 channels <= 3 → feasible
        //     hi = 61
        //
        //   Iteration 7: mid = 60 + (61-60)/2 = 60
        //     isFeasible([10,20,30,40,50], 3, 60)?
        //       ch=1, cur=0
        //       +10 -> cur=10
        //       +20 -> cur=30
        //       +30 -> cur=60
        //       +40 -> 60+40=100 > 60, open ch=2, cur=40
        //       +50 -> 40+50=90 > 60, open ch=3, cur=50
        //       Result: 3 channels <= 3 → feasible
        //     hi = 60
        //
        //   lo == hi == 60 → Answer: 60 ✓
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[] streams1 = {10, 20, 30, 40, 50};
        int k1 = 3;
        int result1 = solution.minimumBandwidthAllocation(streams1, k1);
        System.out.println("Input: streams = " + Arrays.toString(streams1) + ", k = " + k1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 60");
        System.out.println("Correct: " + (result1 == 60));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // streams = [7, 2, 5, 10, 8], k = 2
        // Expected Output: 18
        //
        // Trace:
        //   lo = max(7,2,5,10,8) = 10
        //   hi = 7+2+5+10+8 = 32
        //
        //   Binary search will converge to 18.
        //   At mid=18: isFeasible([7,2,5,10,8], 2, 18)?
        //     ch=1, cur=0
        //     +7  -> cur=7
        //     +2  -> cur=9
        //     +5  -> cur=14
        //     +10 -> 14+10=24 > 18, open ch=2, cur=10
        //     +8  -> cur=18
        //     Result: 2 channels <= 2 → feasible ✓
        //
        //   At mid=17: isFeasible([7,2,5,10,8], 2, 17)?
        //     ch=1, cur=0
        //     +7  -> cur=7
        //     +2  -> cur=9
        //     +5  -> cur=14
        //     +10 -> 14+10=24 > 17, open ch=2, cur=10
        //     +8  -> 10+8=18