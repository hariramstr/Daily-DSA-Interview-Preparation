import java.util.*;

/*
 * Title: Minimum WiFi Router Radius for Linear Offices
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A company has rented a long hallway with offices placed along a straight line.
 * The positions of the offices are given in a sorted integer array offices,
 * where offices[i] is the coordinate of the i-th office.
 *
 * The company can install WiFi routers only at locations listed in another sorted
 * integer array routers, where routers[j] is the coordinate of a possible router location.
 * Every installed router uses the same signal radius r, and it covers every office
 * whose distance from that router is at most r.
 *
 * You may use any number of the available router locations, including all of them.
 * Your task is to compute the minimum integer radius r such that every office is
 * covered by at least one router.
 *
 * Return that minimum radius.
 *
 * A solution is expected to use binary search efficiently rather than checking every
 * radius one by one. For a candidate radius, you can determine whether all offices
 * are coverable by checking the nearest router position for each office.
 *
 * Constraints:
 * - 1 <= offices.length, routers.length <= 2 * 10^5
 * - 0 <= offices[i], routers[j] <= 10^9
 * - offices is sorted in non-decreasing order
 * - routers is sorted in non-decreasing order
 * - The answer fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: offices = [1, 5, 9], routers = [2, 8]
 * Output: 3
 * Explanation: With radius 3, router 2 covers office 1 and 5, and router 8 covers office 9.
 * Radius 2 is not enough because office 5 would be too far from both routers.
 *
 * Example 2:
 * Input: offices = [2, 4, 6, 14], routers = [1, 7, 15]
 * Output: 2
 * Explanation: Office 2 is covered by router 1, offices 4 and 6 are covered by router 7,
 * and office 14 is covered by router 15. Radius 1 fails because office 4 is not within
 * distance 1 of any router.
 */

public class Solution {

    /**
     * Computes the minimum integer radius needed so that every office is covered
     * by at least one available router location.
     *
     * The method uses binary search on the answer:
     * - If a radius r is sufficient to cover all offices, then any radius larger than r
     *   is also sufficient.
     * - If a radius r is not sufficient, then any smaller radius is also not sufficient.
     *
     * This monotonic property makes binary search appropriate.
     *
     * @param offices sorted array of office coordinates
     * @param routers sorted array of possible router coordinates
     * @return the minimum integer radius that covers all offices
     * Time complexity: O(n log M), where n = offices.length and M is the search range of radius.
     * Space complexity: O(1)
     */
    public int minRouterRadius(int[] offices, int[] routers) {
        // The smallest possible radius is 0.
        int left = 0;

        // A safe upper bound:
        // Since coordinates are within [0, 1e9], the maximum possible distance
        // between an office and a router is at most 1e9.
        int right = 1_000_000_000;

        // Standard binary search for the first radius that works.
        while (left < right) {
            // Use this form to avoid overflow:
            int mid = left + (right - left) / 2;

            // If mid is enough to cover every office, try to find a smaller valid radius.
            if (canCoverAllOffices(offices, routers, mid)) {
                right = mid;
            } else {
                // Otherwise, we must increase the radius.
                left = mid + 1;
            }
        }

        // At loop end, left == right and points to the minimum feasible radius.
        return left;
    }

    /**
     * Checks whether all offices can be covered using the given radius.
     *
     * Because both arrays are sorted, we can scan through offices from left to right
     * while advancing a pointer through routers. For each office:
     * - Move the router pointer forward while the next router is at least as close
     *   to the current office as the current router.
     * - After that movement, the current router pointer represents the nearest router
     *   among the scanned candidates for this office.
     * - If the distance from that nearest router to the office is greater than radius,
     *   then this office is not covered, so the answer is false.
     *
     * This works in linear time because the router pointer only moves forward,
     * never backward.
     *
     * @param offices sorted array of office coordinates
     * @param routers sorted array of possible router coordinates
     * @param radius candidate radius to test
     * @return true if every office is within distance radius of some router; false otherwise
     * Time complexity: O(offices.length + routers.length)
     * Space complexity: O(1)
     */
    public boolean canCoverAllOffices(int[] offices, int[] routers, int radius) {
        // Pointer to the current best router candidate.
        int routerIndex = 0;

        // Process offices from left to right.
        for (int office : offices) {

            // We try to move routerIndex to the router that is closest to this office.
            //
            // Why this works:
            // - Arrays are sorted.
            // - As office positions increase, the nearest router index never needs to move backward.
            // - So we can greedily advance the router pointer.
            //
            // We compare:
            //   current distance = |routers[routerIndex] - office|
            //   next distance    = |routers[routerIndex + 1] - office|
            //
            // If the next router is closer or equally close, move forward.
            while (routerIndex + 1 < routers.length
                    && Math.abs((long) routers[routerIndex + 1] - office)
                    <= Math.abs((long) routers[routerIndex] - office)) {
                routerIndex++;
            }

            // After the loop, routers[routerIndex] is the nearest router for this office
            // among the reachable candidates in our monotonic scan.
            long distanceToNearestRouter = Math.abs((long) routers[routerIndex] - office);

            // If even the nearest router is too far away, this radius fails.
            if (distanceToNearestRouter > radius) {
                return false;
            }
        }

        // Every office was covered.
        return true;
    }

    /**
     * Alternative helper that computes the answer directly without binary search.
     *
     * For each office, find the nearest router using the same two-pointer idea,
     * then take the maximum of those nearest distances. That maximum is exactly
     * the minimum required radius.
     *
     * This method is included for educational completeness, but the main requested
     * approach is the binary-search-based method above.
     *
     * @param offices sorted array of office coordinates
     * @param routers sorted array of possible router coordinates
     * @return the minimum radius required to cover all offices
     * Time complexity: O(offices.length + routers.length)
     * Space complexity: O(1)
     */
    public int minRouterRadiusDirect(int[] offices, int[] routers) {
        int routerIndex = 0;
        long answer = 0;

        for (int office : offices) {
            while (routerIndex + 1 < routers.length
                    && Math.abs((long) routers[routerIndex + 1] - office)
                    <= Math.abs((long) routers[routerIndex] - office)) {
                routerIndex++;
            }

            long distanceToNearestRouter = Math.abs((long) routers[routerIndex] - office);
            answer = Math.max(answer, distanceToNearestRouter);
        }

        return (int) answer;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding called method costs
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] offices1 = {1, 5, 9};
        int[] routers1 = {2, 8};
        int result1 = solution.minRouterRadius(offices1, routers1);
        System.out.println("Example 1 result: " + result1); // Expected: 3

        int[] offices2 = {2, 4, 6, 14};
        int[] routers2 = {1, 7, 15};
        int result2 = solution.minRouterRadius(offices2, routers2);
        System.out.println("Example 2 result: " + result2); // Expected: 2

        // Additional verification using the direct method.
        System.out.println("Example 1 direct check: " + solution.minRouterRadiusDirect(offices1, routers1)); // 3
        System.out.println("Example 2 direct check: " + solution.minRouterRadiusDirect(offices2, routers2)); // 2
    }
}