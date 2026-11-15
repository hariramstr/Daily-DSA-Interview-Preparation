import java.util.*;

/*
 * Title: Count Courier Pairs Within Delivery Radius
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A logistics company stores the home coordinates of its couriers along a straight road.
 * You are given an integer array positions, where positions[i] is the location of the ith
 * courier on that road, and an integer radius. Two couriers can be assigned to the same
 * local dispatch zone if the absolute difference between their positions is less than or
 * equal to radius.
 *
 * Your task is to return the total number of distinct courier pairs (i, j) such that
 * i < j and |positions[i] - positions[j]| <= radius.
 *
 * The input array is not guaranteed to be sorted. Because the number of couriers can be
 * large, an O(n^2) solution may time out. Design an efficient algorithm using sorting
 * and the two-pointers technique.
 *
 * Constraints:
 * - 1 <= positions.length <= 200000
 * - -10^9 <= positions[i] <= 10^9
 * - 0 <= radius <= 10^9
 * - Return the answer as a 64-bit integer if your language requires it, since the number
 *   of valid pairs may exceed 32-bit range.
 *
 * Example 1:
 * Input: positions = [4, 1, 7, 3, 9], radius = 3
 * Output: 4
 * Explanation:
 * After sorting, positions = [1, 3, 4, 7, 9].
 * Valid pairs are (1,3), (1,4), (3,4), and (7,9).
 *
 * Example 2:
 * Input: positions = [10, 10, 10, 15], radius = 0
 * Output: 3
 * Explanation:
 * Only couriers at the exact same location can form a valid pair.
 * The three couriers at position 10 create 3 pairs.
 */

public class Solution {

    /**
     * Counts the number of distinct courier pairs whose distance is at most the given radius.
     *
     * The algorithm works as follows:
     * 1. Sort the positions so that nearby values become adjacent.
     * 2. Use two pointers:
     *    - left marks the beginning of a valid window
     *    - right expands the window one courier at a time
     * 3. For each right, move left forward until the window satisfies:
     *      positions[right] - positions[left] <= radius
     * 4. Once valid, every index from left to right - 1 can pair with right.
     *    That contributes (right - left) new valid pairs.
     *
     * @param positions the array of courier positions along the road
     * @param radius the maximum allowed distance between two couriers
     * @return the total number of valid distinct pairs as a long
     * Time complexity: O(n log n) due to sorting, where n is positions.length
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public long countCourierPairs(int[] positions, int radius) {
        // Defensive handling is not strictly necessary under the given constraints,
        // but this makes the method safer and beginner-friendly.
        if (positions == null || positions.length < 2) {
            return 0L;
        }

        // Sort the array first.
        // Why sorting helps:
        // After sorting, if positions[right] - positions[left] is too large,
        // then any index even further left will also be too far from positions[right].
        // This monotonic behavior is exactly what makes the two-pointer approach efficient.
        Arrays.sort(positions);

        long pairCount = 0L;

        // left is the start of the current valid window.
        int left = 0;

        // right is the end of the current window.
        // We expand right from left to right across the sorted array.
        for (int right = 0; right < positions.length; right++) {

            // While the current window is invalid, move left forward.
            //
            // We cast to long before subtraction to be completely safe,
            // even though the difference of two ints in the given range still fits in int.
            // Using long here is a good habit when dealing with large numeric constraints.
            while ((long) positions[right] - positions[left] > radius) {
                left++;
            }

            // At this point, the window [left, right] is valid:
            // positions[right] - positions[left] <= radius
            //
            // Because the array is sorted, that means every index k in [left, right - 1]
            // also satisfies:
            // positions[right] - positions[k] <= radius
            //
            // Therefore, positions[right] can form a valid pair with exactly:
            // right - left
            // previous elements.
            pairCount += (right - left);
        }

        return pairCount;
    }

    /**
     * A convenience wrapper with a descriptive name matching the problem statement.
     *
     * @param positions the array of courier positions
     * @param radius the maximum allowed distance between two couriers
     * @return the number of valid courier pairs
     * Time complexity: O(n log n)
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public long solve(int[] positions, int radius) {
        return countCourierPairs(positions, radius);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints the sorted-input interpretation indirectly through the result,
     * and verifies that the outputs match the expected values.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) for each demonstration call
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] positions1 = {4, 1, 7, 3, 9};
        int radius1 = 3;
        long result1 = solution.solve(positions1, radius1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 4");

        // Example 2
        int[] positions2 = {10, 10, 10, 15};
        int radius2 = 0;
        long result2 = solution.solve(positions2, radius2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 3");

        // Additional quick sanity checks for beginners:

        // Single courier -> no pairs
        int[] positions3 = {5};
        int radius3 = 10;
        long result3 = solution.solve(positions3, radius3);
        System.out.println("Single courier test: " + result3);
        System.out.println("Expected: 0");

        // All couriers close enough -> all pairs valid
        int[] positions4 = {1, 2, 3, 4};
        int radius4 = 10;
        long result4 = solution.solve(positions4, radius4);
        System.out.println("All pairs valid test: " + result4);
        System.out.println("Expected: 6");

        // No couriers close enough
        int[] positions5 = {1, 10, 20, 30};
        int radius5 = 5;
        long result5 = solution.solve(positions5, radius5);
        System.out.println("No valid pairs test: " + result5);
        System.out.println("Expected: 0");
    }
}