import java.util.*;

/*
Problem Title: Minimum Router Signal to Reach All Offices

Problem Description:
A company has opened offices along a straight highway. The positions of the offices are given in a sorted integer array offices,
where offices[i] is the kilometer marker of the i-th office. You may install exactly k Wi-Fi routers. Every router uses the same
signal strength r, and a router placed at position x covers all offices whose positions lie in the inclusive range [x - r, x + r].

You may place routers at any real-valued position, not necessarily at an office location. Your task is to compute the minimum
integer signal strength r needed so that all offices are covered using at most k routers.

This is an optimization problem: for a fixed signal strength, you must determine whether it is possible to cover all offices with
k or fewer routers, and then find the smallest feasible value.

Return the minimum integer r.

Constraints:
- 1 <= offices.length <= 100000
- 1 <= k <= offices.length
- 0 <= offices[i] <= 1000000000
- offices is sorted in non-decreasing order

Example 1:
Input: offices = [1, 2, 8, 12, 17], k = 2
Output: 4
Explanation: Place one router at 5 to cover offices at 1, 2, and 8, and another at 13 to cover 12 and 17.
With signal strength 4, every office is covered. A smaller integer strength cannot cover all offices with only 2 routers.

Example 2:
Input: offices = [0, 4, 9, 15], k = 3
Output: 2
Explanation: One valid placement is routers centered near 2, 9, and 15. Signal strength 2 is enough to cover all offices using 3 routers.
Signal strength 1 is not sufficient because office 0 and office 4 cannot be covered by the same router.
*/

public class Solution {

    /**
     * Computes the minimum integer signal strength needed so that all offices can be covered
     * using at most k routers.
     *
     * Core idea:
     * 1. If we fix a candidate signal strength r, we can greedily determine the minimum number
     *    of routers required to cover all offices.
     * 2. If a certain r works, then any larger signal strength also works.
     *    This monotonic property makes binary search applicable.
     *
     * @param offices sorted array of office positions along the highway
     * @param k maximum number of routers allowed
     * @return the smallest integer signal strength r that allows covering all offices
     *
     * Time complexity: O(n log D), where n is offices.length and D is the search range of positions
     * Space complexity: O(1), excluding input storage
     */
    public int minimumSignalStrength(int[] offices, int k) {
        // Defensive handling for completeness.
        // Based on constraints, offices has at least one element and k >= 1.
        if (offices == null || offices.length == 0) {
            return 0;
        }

        // The minimum possible integer radius is 0.
        int left = 0;

        // A safe upper bound:
        // If one router must cover everything, the needed radius is at most
        // ceil((maxPosition - minPosition) / 2).
        // Using the full span as an upper bound is also valid and simpler.
        int right = offices[offices.length - 1] - offices[0];

        // Standard binary search on the answer.
        // We search for the smallest radius that is feasible.
        while (left < right) {
            int mid = left + (right - left) / 2;

            // If we can cover all offices with <= k routers using radius mid,
            // then mid is feasible, so try to find an even smaller feasible radius.
            if (canCoverAll(offices, k, mid)) {
                right = mid;
            } else {
                // Otherwise, mid is too small, so we must search larger radii.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the minimum feasible radius.
        return left;
    }

    /**
     * Checks whether all offices can be covered using at most k routers, where every router
     * has the same fixed signal strength radius.
     *
     * Greedy strategy:
     * - Start from the leftmost uncovered office.
     * - To maximize coverage, place a router as far right as possible while still covering
     *   that leftmost uncovered office.
     * - If the leftmost uncovered office is at position p, then the best router center is p + radius.
     * - That router covers up to p + 2 * radius.
     * - Then skip all offices within that covered range and repeat.
     *
     * Why this greedy approach is correct:
     * - Once we decide to cover the current leftmost uncovered office, placing the router any further
     *   left would only reduce rightward coverage.
     * - Placing it any further right would fail to cover that office.
     * - Therefore, placing it at p + radius is optimal for maximizing progress.
     *
     * @param offices sorted array of office positions
     * @param k maximum number of routers available
     * @param radius candidate signal strength to test
     * @return true if all offices can be covered with at most k routers, otherwise false
     *
     * Time complexity: O(n), where n is offices.length
     * Space complexity: O(1)
     */
    public boolean canCoverAll(int[] offices, int k, int radius) {
        int n = offices.length;

        // This index points to the first office that is not yet covered.
        int i = 0;

        // Count how many routers we have used so far.
        int routersUsed = 0;

        // Continue until every office is covered.
        while (i < n) {
            // We are about to place one router to cover offices starting from offices[i].
            routersUsed++;

            // If we already exceeded the allowed number of routers, we can stop early.
            if (routersUsed > k) {
                return false;
            }

            // Let the leftmost uncovered office be at position offices[i].
            // To cover it and maximize rightward reach, place the router center at:
            // center = offices[i] + radius
            //
            // Then the router covers:
            // [center - radius, center + radius]
            // = [offices[i], offices[i] + 2 * radius]
            //
            // We only need the right endpoint to skip covered offices.
            long coveredRight = (long) offices[i] + 2L * radius;

            // Move i forward while offices are within the covered interval.
            while (i < n && offices[i] <= coveredRight) {
                i++;
            }
        }

        // If we finished the loop, all offices were covered using <= k routers.
        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(n log D) per demonstration call
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] offices1 = {1, 2, 8, 12, 17};
        int k1 = 2;
        int result1 = solution.minimumSignalStrength(offices1, k1);
        System.out.println("Example 1:");
        System.out.println("Offices = " + Arrays.toString(offices1) + ", k = " + k1);
        System.out.println("Minimum signal strength = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        int[] offices2 = {0, 4, 9, 15};
        int k2 = 3;
        int result2 = solution.minimumSignalStrength(offices2, k2);
        System.out.println("Example 2:");
        System.out.println("Offices = " + Arrays.toString(offices2) + ", k = " + k2);
        System.out.println("Minimum signal strength = " + result2);
        System.out.println("Expected = 2");
        System.out.println();

        // Additional quick sanity checks for beginners:
        int[] offices3 = {5};
        int k3 = 1;
        System.out.println("Additional Test 1:");
        System.out.println("Offices = " + Arrays.toString(offices3) + ", k = " + k3);
        System.out.println("Minimum signal strength = " + solution.minimumSignalStrength(offices3, k3));
        System.out.println("Expected = 0");
        System.out.println();

        int[] offices4 = {1, 3, 6, 10};
        int k4 = 4;
        System.out.println("Additional Test 2:");
        System.out.println("Offices = " + Arrays.toString(offices4) + ", k = " + k4);
        System.out.println("Minimum signal strength = " + solution.minimumSignalStrength(offices4, k4));
        System.out.println("Expected = 0");
    }
}