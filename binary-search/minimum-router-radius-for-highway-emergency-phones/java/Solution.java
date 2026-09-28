import java.util.*;

/*
 * Title: Minimum Router Radius for Highway Emergency Phones
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A transportation agency is installing wireless routers along a straight highway to provide service
 * to emergency phones. The highway is modeled as a number line. You are given two integer arrays:
 * phones, where phones[i] is the position of the i-th emergency phone, and routers, where routers[j]
 * is the position of the j-th router installation point. A router with signal radius r covers every
 * phone whose distance from that router is at most r.
 *
 * Your task is to return the minimum integer radius r such that every emergency phone is covered by
 * at least one router.
 *
 * The arrays are not guaranteed to be sorted. Positions may be negative, and multiple phones or
 * routers may share the same position. You should design an efficient solution that scales to large
 * inputs. A brute-force comparison of every phone against every router will be too slow.
 *
 * A common approach is to sort the router positions and, for each phone, use binary search to find
 * the nearest router on the left or right. The answer is the maximum among the minimum distances from
 * each phone to its closest router.
 *
 * Constraints:
 * - 1 <= phones.length, routers.length <= 2 * 10^5
 * - -10^9 <= phones[i], routers[j] <= 10^9
 * - The result fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: phones = [2, 10, 15], routers = [1, 5, 14]
 * Output: 4
 * Explanation:
 * - Phone 2 is closest to router 1, distance 1.
 * - Phone 10 is closest to router 14 or 5, distances 4 and 5, so minimum is 4.
 * - Phone 15 is closest to router 14, distance 1.
 * The maximum of these minimum distances is max(1, 4, 1) = 4.
 *
 * Example 2:
 * Input: phones = [-8, -3, 0, 7], routers = [-10, 2]
 * Output: 5
 * Explanation:
 * The closest-router distances are:
 * - Phone -8 -> router -10, distance 2
 * - Phone -3 -> closest router is 2 or -10, distances 5 and 7, so minimum is 5
 * - Phone 0 -> router 2, distance 2
 * - Phone 7 -> router 2, distance 5
 * Therefore, the minimum radius needed is 5.
 */

public class Solution {

    /**
     * Computes the minimum integer router radius required so that every phone is covered
     * by at least one router.
     *
     * Strategy:
     * 1. Sort the router positions.
     * 2. For each phone, use binary search to locate where that phone would be inserted
     *    into the sorted router array.
     * 3. The closest router can only be:
     *    - the router immediately to the left of that insertion point, or
     *    - the router immediately to the right of that insertion point.
     * 4. Compute the minimum distance to those candidates.
     * 5. The answer is the maximum such minimum distance over all phones.
     *
     * @param phones the positions of emergency phones along the highway
     * @param routers the positions of routers along the highway
     * @return the minimum integer radius needed so every phone is covered
     * Time complexity: O(m log m + n log m), where n = phones.length and m = routers.length
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public int findMinimumRadius(int[] phones, int[] routers) {
        // Sort router positions first.
        // This is essential because binary search only works on sorted data.
        Arrays.sort(routers);

        // This variable will store the final answer.
        // For each phone, we compute its distance to the nearest router.
        // The largest of those distances is the minimum radius required.
        int requiredRadius = 0;

        // Process each phone independently.
        for (int phone : phones) {
            // Find the distance from this phone to its nearest router.
            int nearestDistance = distanceToClosestRouter(phone, routers);

            // The overall radius must be large enough to cover this phone too.
            // So we keep the maximum nearest distance seen so far.
            requiredRadius = Math.max(requiredRadius, nearestDistance);
        }

        return requiredRadius;
    }

    /**
     * Finds the distance from a single phone to its closest router using binary search.
     *
     * Detailed idea:
     * - After sorting routers, binary search tells us the insertion position of the phone.
     * - If insertion index is i:
     *   - router at i is the first router >= phone (right candidate), if i exists
     *   - router at i - 1 is the last router < phone (left candidate), if i - 1 exists
     * - The closest router must be one of these two neighbors.
     *
     * @param phone the position of one emergency phone
     * @param sortedRouters router positions sorted in non-decreasing order
     * @return the minimum distance from the phone to any router
     * Time complexity: O(log m), where m = sortedRouters.length
     * Space complexity: O(1)
     */
    public int distanceToClosestRouter(int phone, int[] sortedRouters) {
        // Java's Arrays.binarySearch works as follows:
        // - If the value is found, it returns its index.
        // - If not found, it returns -(insertionPoint) - 1.
        //
        // The insertion point is the index where the value would be inserted
        // to keep the array sorted.
        int index = Arrays.binarySearch(sortedRouters, phone);

        // If binary search found an exact match, distance is zero,
        // because a router is exactly at the phone's position.
        if (index >= 0) {
            return 0;
        }

        // Recover the insertion point from the negative result.
        int insertionPoint = -index - 1;

        // We will compare the nearest router on the left and on the right.
        // Start with a very large value so that missing sides do not affect the minimum.
        long leftDistance = Long.MAX_VALUE;
        long rightDistance = Long.MAX_VALUE;

        // Check if there is a router on the left side.
        // That router would be at insertionPoint - 1.
        if (insertionPoint - 1 >= 0) {
            leftDistance = (long) phone - sortedRouters[insertionPoint - 1];
            if (leftDistance < 0) {
                leftDistance = -leftDistance;
            }
        }

        // Check if there is a router on the right side.
        // That router would be at insertionPoint.
        if (insertionPoint < sortedRouters.length) {
            rightDistance = (long) sortedRouters[insertionPoint] - phone;
            if (rightDistance < 0) {
                rightDistance = -rightDistance;
            }
        }

        // The closest router is whichever side gives the smaller distance.
        long best = Math.min(leftDistance, rightDistance);

        // The problem guarantees the result fits in a 32-bit signed integer.
        return (int) best;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(m log m + n log m) per demonstration call
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] phones1 = {2, 10, 15};
        int[] routers1 = {1, 5, 14};
        int result1 = solution.findMinimumRadius(phones1, routers1);
        System.out.println("Example 1:");
        System.out.println("phones = " + Arrays.toString(phones1));
        System.out.println("routers = " + Arrays.toString(routers1));
        System.out.println("Minimum radius = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Example 2
        int[] phones2 = {-8, -3, 0, 7};
        int[] routers2 = {-10, 2};
        int result2 = solution.findMinimumRadius(phones2, routers2);
        System.out.println("Example 2:");
        System.out.println("phones = " + Arrays.toString(phones2));
        System.out.println("routers = " + Arrays.toString(routers2));
        System.out.println("Minimum radius = " + result2);
        System.out.println("Expected = 5");
        System.out.println();

        // Additional demonstration: exact overlap and duplicates
        int[] phones3 = {1, 1, 1, 8};
        int[] routers3 = {1, 4, 4, 10};
        int result3 = solution.findMinimumRadius(phones3, routers3);
        System.out.println("Additional Example:");
        System.out.println("phones = " + Arrays.toString(phones3));
        System.out.println("routers = " + Arrays.toString(routers3));
        System.out.println("Minimum radius = " + result3);
    }
}