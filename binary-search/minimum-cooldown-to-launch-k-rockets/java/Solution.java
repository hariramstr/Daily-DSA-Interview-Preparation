import java.util.*;

/*
 * Title: Minimum Cooldown to Launch K Rockets
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A spaceport has n launch pads arranged along a straight line. The position of the i-th pad
 * is given by pads[i], and the array is sorted in non-decreasing order. You want to schedule
 * exactly k rocket launches, choosing k distinct pads. For safety reasons, any two chosen pads
 * must be at least d units apart, where d is a global cooldown distance applied to the entire
 * schedule.
 *
 * Your task is to compute the largest possible value of d such that it is still possible to
 * choose k pads satisfying the distance requirement. In other words, maximize the minimum
 * pairwise distance between consecutive selected launch pads.
 *
 * Return the maximum feasible cooldown distance.
 *
 * This problem is intended to be solved efficiently for large inputs. A brute-force search over
 * all subsets of pads will not pass. Think carefully about how the feasibility of a candidate
 * distance changes as the distance increases, and how that property can be exploited.
 *
 * Constraints:
 * - 2 <= n <= 200000
 * - 2 <= k <= n
 * - 0 <= pads[i] <= 10^18
 * - pads is sorted in non-decreasing order
 * - Multiple pads may share the same position, but only one rocket can be launched from each pad index
 *
 * Example 1:
 * Input: pads = [1, 2, 8, 12, 17], k = 3
 * Output: 7
 * Explanation: Choose pads at positions 1, 8, and 17. The minimum distance between consecutive
 * chosen pads is min(7, 9) = 7. No larger cooldown distance is feasible for 3 launches.
 *
 * Example 2:
 * Input: pads = [0, 0, 4, 9, 13, 18], k = 4
 * Output: 4
 * Explanation: One optimal choice is positions 0, 4, 9, and 13, where the consecutive gaps are
 * 4, 5, and 4. So the largest valid cooldown distance is 4.
 */

public class Solution {

    /**
     * Computes the maximum feasible cooldown distance d such that we can choose exactly k pads
     * and every pair of consecutive chosen pads is at least d units apart.
     *
     * Core idea:
     * 1. If a distance d is feasible, then every smaller distance is also feasible.
     * 2. This monotonic property allows us to binary search on the answer.
     * 3. For each candidate distance d, we greedily place rockets from left to right:
     *    always choose the earliest possible next pad. This greedy strategy maximizes how many
     *    pads we can place for that d, so it is the correct feasibility check.
     *
     * @param pads sorted array of pad positions
     * @param k number of rockets / pads to choose
     * @return the largest cooldown distance that is feasible
     * Time complexity: O(n log R), where R = pads[n - 1] - pads[0]
     * Space complexity: O(1)
     */
    public long maximumCooldown(long[] pads, int k) {
        int n = pads.length;

        // The answer cannot be negative.
        long low = 0L;

        // The largest possible minimum distance is the full spread between the first and last pad.
        long high = pads[n - 1] - pads[0];

        // We will store the best feasible answer found so far.
        long answer = 0L;

        // Standard binary search on the answer space.
        while (low <= high) {
            // Use this form to avoid overflow:
            long mid = low + (high - low) / 2;

            // Check whether it is possible to choose at least k pads
            // such that adjacent chosen pads differ by at least mid.
            if (canPlaceKLaunches(pads, k, mid)) {
                // mid is feasible, so record it.
                answer = mid;

                // Try to find an even larger feasible distance.
                low = mid + 1;
            } else {
                // mid is not feasible, so we must search smaller distances.
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Greedily checks whether we can choose at least k pads such that each chosen pad is at least
     * minDistance away from the previously chosen pad.
     *
     * Why greedy works:
     * - We always pick the leftmost possible pad.
     * - Picking earlier leaves the most room for future selections.
     * - Therefore, if the greedy strategy cannot place k launches, no other strategy can.
     *
     * Example:
     * pads = [1, 2, 8, 12, 17], k = 3, minDistance = 7
     * - pick 1
     * - next must be >= 8 -> pick 8
     * - next must be >= 15 -> pick 17
     * - placed 3, so feasible
     *
     * @param pads sorted array of pad positions
     * @param k number of pads we want to choose
     * @param minDistance candidate cooldown distance to test
     * @return true if at least k pads can be chosen, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canPlaceKLaunches(long[] pads, int k, long minDistance) {
        // Always choose the first pad initially.
        int placed = 1;
        long lastChosenPosition = pads[0];

        // Scan from left to right and greedily choose every pad that is far enough.
        for (int i = 1; i < pads.length; i++) {
            // If the current pad is at least minDistance away from the last chosen pad,
            // we can safely choose it.
            if (pads[i] - lastChosenPosition >= minDistance) {
                placed++;
                lastChosenPosition = pads[i];

                // As soon as we have placed k launches, the candidate distance is feasible.
                if (placed >= k) {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k placements, it is not feasible.
        return placed >= k;
    }

    /**
     * Convenience overload that accepts an int array and internally converts it to long[].
     * This is useful for small demonstrations and beginner-friendly usage.
     *
     * @param pads sorted array of pad positions as int values
     * @param k number of rockets / pads to choose
     * @return the largest cooldown distance that is feasible
     * Time complexity: O(n log R), where R = max(pads) - min(pads)
     * Space complexity: O(n) due to conversion from int[] to long[]
     */
    public long maximumCooldown(int[] pads, int k) {
        long[] converted = new long[pads.length];
        for (int i = 0; i < pads.length; i++) {
            converted[i] = pads[i];
        }
        return maximumCooldown(converted, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 7
     * Example 2 -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log R) per demonstration call
     * Space complexity: O(1) additional, excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] pads1 = {1, 2, 8, 12, 17};
        int k1 = 3;
        long result1 = solution.maximumCooldown(pads1, k1);
        System.out.println("Example 1 Result: " + result1);

        long[] pads2 = {0, 0, 4, 9, 13, 18};
        int k2 = 4;
        long result2 = solution.maximumCooldown(pads2, k2);
        System.out.println("Example 2 Result: " + result2);

        // Additional quick sanity checks.
        long[] pads3 = {5, 5, 5, 5};
        int k3 = 2;
        long result3 = solution.maximumCooldown(pads3, k3);
        System.out.println("All same positions, k=2 Result: " + result3);

        long[] pads4 = {0, 3, 6, 9, 12};
        int k4 = 5;
        long result4 = solution.maximumCooldown(pads4, k4);
        System.out.println("Choose all pads Result: " + result4);
    }
}