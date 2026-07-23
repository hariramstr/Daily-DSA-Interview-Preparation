import java.util.*;

/*
 * Title: Minimum Station Range to Relay a Mountain Convoy
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A rescue convoy must travel from checkpoint 0 to checkpoint D along a one-dimensional mountain road.
 * Along the road, there are n possible relay station locations given in sorted order by the array positions,
 * where positions[i] is the distance of station i from checkpoint 0. Each station can be activated or skipped.
 * If activated, a station with transmission range R can relay commands to any other activated station or checkpoint
 * whose distance is at most R away. Checkpoint 0 and checkpoint D behave like fixed endpoints that are always
 * available, but they do not count toward the station limit.
 *
 * You are also given an integer k. Due to budget constraints, you may activate at most k stations.
 * Your task is to compute the minimum integer range R such that commands can be relayed from checkpoint 0 to checkpoint D
 * using at most k activated stations.
 *
 * In other words, after choosing at most k stations, there must exist a chain starting at 0 and ending at D
 * where every consecutive pair in the chain is at distance at most R.
 *
 * Return the minimum possible R.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= D <= 10^18
 * - 0 < positions[i] < D
 * - positions is strictly increasing
 * - 0 <= k <= n
 * - All answers fit in a 64-bit signed integer
 *
 * Key idea:
 * We binary-search the answer R.
 *
 * For a fixed R, we must determine whether it is possible to go from 0 to D using at most k stations,
 * where each hop length is at most R.
 *
 * This feasibility question can be rephrased:
 * - Consider all usable points: 0, chosen stations, D.
 * - We want the minimum number of chosen stations needed so that consecutive chosen points are at most R apart.
 *
 * Greedy observation:
 * - Starting from the current point, to minimize the number of stations used, always jump as far right as possible
 *   among stations within distance R.
 * - If D is already within distance R, we are done.
 * - Otherwise, if no station is reachable, then this R is impossible.
 *
 * Why this greedy works:
 * - Choosing the farthest reachable next station can never hurt, because it leaves us at least as far along the road
 *   as any other choice while using the same number of stations.
 * - Therefore it minimizes the number of stations needed for the fixed R.
 *
 * Complexity:
 * - Feasibility check for one R: O(n)
 * - Binary search over R in [0, D]: O(log D)
 * - Total: O(n log D)
 */

public class Solution {

    /**
     * Computes the minimum integer transmission range R such that commands can be relayed
     * from checkpoint 0 to checkpoint D using at most k activated stations.
     *
     * We binary-search the smallest feasible R.
     *
     * @param positions sorted array of possible station locations, strictly increasing
     * @param D the destination checkpoint position
     * @param k maximum number of stations that may be activated
     * @return the minimum feasible integer range R
     * Time complexity: O(n log D)
     * Space complexity: O(1) extra space
     */
    public long minimumRange(int[] positions, long D, int k) {
        long low = 0L;
        long high = D;

        // Standard binary search on the answer:
        // - low..high is the current search interval
        // - we maintain that the answer lies inside this interval
        while (low < high) {
            long mid = low + (high - low) / 2;

            // If mid is sufficient, try to find an even smaller feasible range.
            if (canRelayWithAtMostKStations(positions, D, k, mid)) {
                high = mid;
            } else {
                // Otherwise, we need a larger range.
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether a given range R is sufficient to relay from 0 to D
     * using at most k activated stations.
     *
     * Greedy strategy:
     * 1. Start at current position = 0.
     * 2. If D is within R, finish immediately.
     * 3. Otherwise, among all stations reachable within distance R from current position,
     *    activate the farthest one.
     * 4. Repeat until reaching D or getting stuck.
     *
     * This greedy minimizes the number of stations used for the fixed R.
     *
     * @param positions sorted array of possible station locations
     * @param D destination checkpoint
     * @param k maximum allowed number of activated stations
     * @param R candidate transmission range
     * @return true if relay is possible with at most k stations, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean canRelayWithAtMostKStations(int[] positions, long D, int k, long R) {
        long current = 0L;
        int usedStations = 0;
        int i = 0;
        int n = positions.length;

        // We continue until we either:
        // - can directly reach D, or
        // - fail because no next station is reachable, or
        // - exceed k stations.
        while (true) {
            // If destination is directly reachable from current point, we are done.
            if (D - current <= R) {
                return true;
            }

            // We will scan all stations that are reachable from "current"
            // and remember the farthest such station.
            long farthestReachableStation = -1L;

            // Because positions is sorted and i only moves forward,
            // every station is processed at most once across the whole check.
            while (i < n && (long) positions[i] - current <= R) {
                farthestReachableStation = positions[i];
                i++;
            }

            // If no station is reachable, then we are stuck before reaching D.
            if (farthestReachableStation == -1L) {
                return false;
            }

            // Activate that farthest reachable station.
            current = farthestReachableStation;
            usedStations++;

            // If we already used too many stations, this R is not feasible.
            if (usedStations > k) {
                return false;
            }
        }
    }

    /**
     * Utility method to run one demonstration case and print the result.
     *
     * @param positions sorted station positions
     * @param D destination checkpoint
     * @param k maximum number of stations allowed
     * @return the computed minimum range
     * Time complexity: O(n log D)
     * Space complexity: O(1) extra space
     */
    public long solveAndPrint(int[] positions, long D, int k) {
        long answer = minimumRange(positions, D, k);
        System.out.println("positions = " + Arrays.toString(positions) + ", D = " + D + ", k = " + k);
        System.out.println("Minimum range = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The narrative examples in the prompt contain inconsistencies.
     * This program prints the mathematically correct answers produced by the algorithm.
     *
     * Example 1:
     * positions = [2, 5, 8, 12], D = 15, k = 2
     * Correct minimum range is 7 via 0 -> 5 -> 12 -> 15.
     *
     * Example 2:
     * positions = [4, 9, 14, 20, 27], D = 30, k = 3
     * Correct minimum range is 10 via 0 -> 9 -> 20 -> 30 or 0 -> 4 -> 14 -> 20 -> 30.
     *
     * @param args command-line arguments, unused
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] positions1 = {2, 5, 8, 12};
        long D1 = 15L;
        int k1 = 2;
        solution.solveAndPrint(positions1, D1, k1); // Expected: 7

        int[] positions2 = {4, 9, 14, 20, 27};
        long D2 = 30L;
        int k2 = 3;
        solution.solveAndPrint(positions2, D2, k2); // Expected: 10

        // Additional quick sanity checks for beginners:

        // No stations allowed: must jump directly from 0 to D.
        int[] positions3 = {3, 6, 9};
        long D3 = 10L;
        int k3 = 0;
        solution.solveAndPrint(positions3, D3, k3); // Expected: 10

        // Enough stations to use every helpful point.
        int[] positions4 = {1, 2, 3, 4, 5};
        long D4 = 6L;
        int k4 = 5;
        solution.solveAndPrint(positions4, D4, k4); // Expected: 1
    }
}