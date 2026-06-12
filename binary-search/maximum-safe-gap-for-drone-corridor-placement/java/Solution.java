import java.util.*;

/*
 * Title: Maximum Safe Gap for Drone Corridor Placement
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A city wants to place exactly k drone recharge beacons along a straight aerial corridor.
 * The corridor already has n approved mounting points, given as a sorted array positions,
 * where positions[i] is the distance in meters from the start of the corridor.
 * You may place at most one beacon at each mounting point.
 *
 * For safety reasons, the city wants the minimum distance between any two placed beacons
 * to be as large as possible. Your task is to return the largest possible value d such that
 * it is possible to place exactly k beacons and every pair of consecutive placed beacons is
 * at least d meters apart.
 *
 * This is an optimization problem: you are not asked to output the placement itself,
 * only the maximum achievable minimum gap.
 *
 * Constraints:
 * - 2 <= n <= 100000
 * - 2 <= k <= n
 * - 0 <= positions[i] <= 1000000000
 * - positions is sorted in strictly increasing order
 *
 * Example 1:
 * Input: positions = [1, 2, 8, 12, 17], k = 3
 * Output: 7
 * Explanation: Place beacons at 1, 8, and 17. The gaps are 7 and 9, so the minimum gap is 7.
 * No arrangement can achieve a minimum gap larger than 7.
 *
 * Example 2:
 * Input: positions = [3, 6, 14, 20, 25, 31], k = 4
 * Correct Output: 6
 *
 * Important note about Example 2:
 * The statement text contains contradictory intermediate reasoning, but the final correct answer is 6.
 * A feasibility check confirms:
 * - Minimum gap 6 is achievable, for example with positions 3, 14, 20, 31
 *   (gaps: 11, 6, 11)
 * - Minimum gap 7 is not achievable for 4 beacons
 * Therefore the maximum feasible minimum gap is 6.
 *
 * Intended Solution:
 * Use binary search on the answer (the minimum allowed gap), and for each candidate gap,
 * use a greedy scan to check whether it is possible to place at least k beacons.
 */
public class Solution {

    /**
     * Computes the largest possible minimum distance between any two consecutive placed beacons.
     *
     * The key idea is:
     * 1. If we can place k beacons with minimum gap = d, then we can also place them with any smaller gap.
     * 2. This monotonic property allows binary search over the answer.
     * 3. For each candidate gap, we greedily place beacons as early as possible.
     *
     * @param positions sorted array of distinct mounting point positions along the corridor
     * @param k exact number of beacons to place
     * @return the maximum achievable minimum gap between consecutive placed beacons
     * Time complexity: O(n log R), where R = positions[n - 1] - positions[0]
     * Space complexity: O(1)
     */
    public int maximumSafeGap(int[] positions, int k) {
        // The smallest possible minimum gap is 0.
        // This is safe as a lower bound for binary search.
        int low = 0;

        // The largest possible minimum gap cannot exceed the distance
        // between the first and last mounting points.
        int high = positions[positions.length - 1] - positions[0];

        // This variable stores the best feasible answer found so far.
        int answer = 0;

        // Standard binary search on the answer space.
        while (low <= high) {
            // Compute the middle candidate gap.
            // Using this form avoids overflow in general:
            int mid = low + (high - low) / 2;

            // Check whether it is possible to place at least k beacons
            // such that every consecutive pair is at least 'mid' apart.
            if (canPlaceBeacons(positions, k, mid)) {
                // If 'mid' is feasible, it is a valid answer.
                answer = mid;

                // Since we want the maximum possible gap,
                // try searching for a larger feasible value.
                low = mid + 1;
            } else {
                // If 'mid' is not feasible, any larger gap will also be infeasible.
                // So we must search smaller values.
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether it is possible to place at least k beacons such that
     * the distance between every consecutive placed beacon is at least minGap.
     *
     * Greedy strategy:
     * - Always place the first beacon at the earliest available position.
     * - Then continue scanning from left to right.
     * - Whenever the current position is at least minGap away from the last placed beacon,
     *   place another beacon there.
     *
     * Why greedy works:
     * - Placing a beacon as early as possible leaves the most room for future placements.
     * - Therefore, if this greedy method cannot place k beacons, no other method can.
     *
     * @param positions sorted array of distinct mounting point positions
     * @param k number of beacons we want to place
     * @param minGap candidate minimum required gap between consecutive beacons
     * @return true if placing at least k beacons is possible, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canPlaceBeacons(int[] positions, int k, int minGap) {
        // We always place the first beacon at the first available mounting point.
        int placedCount = 1;

        // Track the position of the most recently placed beacon.
        int lastPlacedPosition = positions[0];

        // Scan through the remaining mounting points one by one.
        for (int i = 1; i < positions.length; i++) {
            // If the current mounting point is far enough from the last placed beacon,
            // we can safely place another beacon here.
            if (positions[i] - lastPlacedPosition >= minGap) {
                placedCount++;
                lastPlacedPosition = positions[i];

                // As soon as we have placed k beacons, we know the candidate gap is feasible.
                if (placedCount >= k) {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k beacons,
        // then this candidate minimum gap is not feasible.
        return false;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding method calls
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] positions1 = {1, 2, 8, 12, 17};
        int k1 = 3;
        int result1 = solution.maximumSafeGap(positions1, k1);
        System.out.println("Example 1:");
        System.out.println("positions = " + Arrays.toString(positions1) + ", k = " + k1);
        System.out.println("Maximum safe gap = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        int[] positions2 = {3, 6, 14, 20, 25, 31};
        int k2 = 4;
        int result2 = solution.maximumSafeGap(positions2, k2);
        System.out.println("Example 2:");
        System.out.println("positions = " + Arrays.toString(positions2) + ", k = " + k2);
        System.out.println("Maximum safe gap = " + result2);
        System.out.println("Expected = 6");
        System.out.println();

        // Additional quick sanity checks for beginners to see behavior.
        int[] positions3 = {0, 5};
        int k3 = 2;
        int result3 = solution.maximumSafeGap(positions3, k3);
        System.out.println("Additional Test 1:");
        System.out.println("positions = " + Arrays.toString(positions3) + ", k = " + k3);
        System.out.println("Maximum safe gap = " + result3);
        System.out.println("Expected = 5");
        System.out.println();

        int[] positions4 = {1, 3, 4, 7, 9, 13};
        int k4 = 3;
        int result4 = solution.maximumSafeGap(positions4, k4);
        System.out.println("Additional Test 2:");
        System.out.println("positions = " + Arrays.toString(positions4) + ", k = " + k4);
        System.out.println("Maximum safe gap = " + result4);
    }
}