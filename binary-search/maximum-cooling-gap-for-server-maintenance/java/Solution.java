import java.util.*;

/*
 * Title: Maximum Cooling Gap for Server Maintenance
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given a sorted list of rack positions in a data center, where each position contains a server
 * that must be assigned to one of two maintenance crews. To reduce thermal interference, the company
 * wants the minimum distance between any two servers handled by the same crew to be as large as possible.
 * Every server must be assigned to exactly one of the two crews, and each crew may handle any number of servers.
 *
 * Formally, given an array positions of length n in strictly increasing order, partition the servers into
 * two groups A and B such that for each group, the distance between every pair of consecutive servers in
 * that group is at least D. Your task is to compute the maximum possible value of D for which such a partition exists.
 *
 * This is a decision-and-optimization problem: for a candidate D, determine whether the servers can be
 * 2-colored so that no two adjacent servers in the same color class are closer than D, then find the largest feasible D.
 *
 * Return that maximum D.
 *
 * Constraints:
 * - 2 <= n <= 2 * 10^5
 * - 0 <= positions[i] <= 10^18
 * - positions is strictly increasing
 * - The answer fits in a 64-bit signed integer
 *
 * Example 1:
 * Input: positions = [1, 2, 4, 8]
 * Output: 3
 * Explanation: One optimal assignment is crew A -> [1, 4, 8], crew B -> [2].
 * In crew A, consecutive gaps are 3 and 4, both at least 3.
 * It is impossible to achieve D = 4 because servers at positions 1, 2, and 4
 * would force two too-close servers into the same crew.
 *
 * Example 2:
 * Input: positions = [0, 3, 5, 9, 14]
 * Output: 5
 * Explanation: Assign crew A -> [0, 5, 14] and crew B -> [3, 9].
 * The minimum same-crew consecutive gap is min(5, 9, 6) = 5.
 * No valid partition exists for D = 6.
 */

public class Solution {

    /**
     * Computes the maximum feasible minimum same-crew consecutive gap.
     *
     * The method uses binary search on the answer D.
     * For each candidate D, it runs a linear feasibility check.
     *
     * @param positions strictly increasing server positions
     * @return the largest value D such that the positions can be partitioned into two groups
     *         where consecutive positions inside each group differ by at least D
     * Time complexity: O(n log R), where n is the number of positions and
     *                  R = positions[n - 1] - positions[0]
     * Space complexity: O(n)
     */
    public long maximumCoolingGap(long[] positions) {
        int n = positions.length;
        if (n <= 2) {
            return positions[n - 1] - positions[0];
        }

        long low = 0L;
        long high = positions[n - 1] - positions[0];

        while (low < high) {
            long mid = low + (high - low + 1) / 2;
            if (canPartition(positions, mid)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return low;
    }

    /**
     * Checks whether a valid 2-group partition exists for a fixed candidate gap D.
     *
     * Core idea:
     * We process positions from left to right.
     * For each server i, if another server j is within distance < D from i,
     * then i and j cannot be assigned to the same crew.
     *
     * Because positions are sorted, for each i the conflicting earlier positions form a suffix:
     * all indices in [L[i], i - 1], where L[i] is the first index with positions[i] - positions[L[i]] < D.
     *
     * Therefore, when assigning color to i, it must be different from every color used in that suffix.
     * Since we only have two colors, this is possible if and only if all already-colored vertices in that suffix
     * use at most one color.
     *
     * We maintain a sliding window [left, right - 1] of positions whose distance to positions[right] is < D.
     * Inside that window, we only need to know:
     * - whether color 0 appears
     * - whether color 1 appears
     *
     * Greedy assignment:
     * - if only color 0 appears in the conflict window, assign color 1
     * - if only color 1 appears, assign color 0
     * - if neither appears, assign either color (we choose 0)
     * - if both appear, impossible
     *
     * This greedy strategy is correct because the current position only constrains future positions through
     * its chosen color, and choosing any allowed color preserves feasibility whenever feasibility exists.
     *
     * @param positions strictly increasing server positions
     * @param d candidate minimum same-crew consecutive gap
     * @return true if a valid partition exists for this d, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public boolean canPartition(long[] positions, long d) {
        int n = positions.length;

        // color[i] will be 0 or 1 depending on which crew position i is assigned to.
        int[] color = new int[n];

        // count[c] = how many positions currently inside the active conflict window have color c.
        int[] count = new int[2];

        // left points to the first index still conflicting with the current right index.
        int left = 0;

        for (int right = 0; right < n; right++) {
            /*
             * Before assigning a color to positions[right], we must build the exact set of earlier positions
             * that are too close to it, meaning:
             *
             *     positions[right] - positions[j] < d
             *
             * Since positions is sorted, all such j form a contiguous suffix among earlier indices.
             *
             * We maintain that the active window [left, right - 1] contains exactly those conflicting indices.
             * So we move left forward while a position is far enough away and therefore no longer conflicts.
             */
            while (left < right && positions[right] - positions[left] >= d) {
                count[color[left]]--;
                left++;
            }

            /*
             * Now the active window [left, right - 1] contains all earlier positions that cannot share
             * the same color with positions[right].
             *
             * There are only four possibilities:
             *
             * 1) No colors appear in the window:
             *    - no restriction, choose color 0
             *
             * 2) Only color 0 appears:
             *    - current must be color 1
             *
             * 3) Only color 1 appears:
             *    - current must be color 0
             *
             * 4) Both colors appear:
             *    - current would need to differ from both colors at once, impossible with only two colors
             */
            boolean hasColor0 = count[0] > 0;
            boolean hasColor1 = count[1] > 0;

            if (hasColor0 && hasColor1) {
                return false;
            }

            if (hasColor0) {
                color[right] = 1;
            } else {
                color[right] = 0;
            }

            count[color[right]]++;
        }

        return true;
    }

    /**
     * Convenience overload for int[] input.
     *
     * @param positions strictly increasing server positions as int values
     * @return the largest feasible minimum same-crew consecutive gap
     * Time complexity: O(n log R)
     * Space complexity: O(n)
     */
    public long maximumCoolingGap(int[] positions) {
        long[] arr = new long[positions.length];
        for (int i = 0; i < positions.length; i++) {
            arr[i] = positions[i];
        }
        return maximumCoolingGap(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the fixed demo sizes
     * Space complexity: O(1) excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] positions1 = {1, 2, 4, 8};
        long result1 = solution.maximumCoolingGap(positions1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 3");

        long[] positions2 = {0, 3, 5, 9, 14};
        long result2 = solution.maximumCoolingGap(positions2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 5");

        long[] positions3 = {0, 10};
        long result3 = solution.maximumCoolingGap(positions3);
        System.out.println("Additional test result: " + result3);
        System.out.println("Expected: 10");
    }
}