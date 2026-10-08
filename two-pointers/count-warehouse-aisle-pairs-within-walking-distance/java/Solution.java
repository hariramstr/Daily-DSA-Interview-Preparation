import java.util.*;

/*
 * Title: Count Warehouse Aisle Pairs Within Walking Distance
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A warehouse stores picking stations along one long aisle. You are given an integer array positions
 * where positions[i] is the location of the i-th station measured in meters from the start of the aisle.
 * You are also given an integer maxDistance.
 *
 * Two stations form a valid pair if the absolute difference between their positions is less than or equal
 * to maxDistance. Your task is to return the total number of distinct valid pairs (i, j) such that i < j.
 *
 * The input array is not guaranteed to be sorted. An efficient solution is expected for large inputs,
 * so a brute-force O(n^2) approach may time out. This problem is intended to test whether you can combine
 * sorting with a two-pointers scanning strategy to count many pairs at once.
 *
 * Return the number of valid pairs.
 *
 * Constraints:
 * - 1 <= positions.length <= 200000
 * - -10^9 <= positions[i] <= 10^9
 * - 0 <= maxDistance <= 10^9
 * - The answer can be as large as n * (n - 1) / 2, so use a 64-bit integer type where needed.
 *
 * Example 1:
 * Input: positions = [8, 1, 4, 10, 6], maxDistance = 3
 * Output: 4
 * Explanation:
 * After sorting, positions = [1, 4, 6, 8, 10].
 * Valid pairs are:
 * (1,4) -> distance 3
 * (4,6) -> distance 2
 * (6,8) -> distance 2
 * (8,10) -> distance 2
 * Total = 4
 *
 * Example 2:
 * Input: positions = [5, 5, 5, 9], maxDistance = 0
 * Output: 3
 * Explanation:
 * Only stations at exactly the same location can be paired.
 * The three stations at position 5 produce 3 pairs:
 * choose any 2 out of the 3 identical positions.
 */

public class Solution {

    /**
     * Counts the number of distinct pairs of stations whose distance is at most maxDistance.
     *
     * The algorithm works in two major phases:
     * 1) Sort the array so that nearby values become adjacent.
     * 2) Use a sliding window / two-pointers scan:
     *    - Maintain a left pointer and expand the right pointer one step at a time.
     *    - For each right index, move left forward until the window satisfies:
     *      positions[right] - positions[left] <= maxDistance
     *    - Once the window is valid, every index from left to right - 1 forms a valid pair with right.
     *      That contributes (right - left) pairs at once.
     *
     * This is much faster than checking every pair individually.
     *
     * @param positions the array of station positions along the aisle; may be unsorted
     * @param maxDistance the maximum allowed distance between two stations for them to form a valid pair
     * @return the total number of valid pairs as a long
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scan
     * Space complexity: O(1) extra space beyond the sorting implementation details used by Java
     */
    public long countPairsWithinDistance(int[] positions, int maxDistance) {
        // Defensive handling:
        // If the array has fewer than 2 elements, no pair can exist.
        if (positions == null || positions.length < 2) {
            return 0L;
        }

        // Step 1: Sort the positions.
        // Why sorting helps:
        // After sorting, if positions[right] - positions[left] is too large,
        // then any index even further left will also be too large.
        // This monotonic behavior is exactly what makes two pointers efficient.
        Arrays.sort(positions);

        // This variable stores the final answer.
        // We use long because the number of pairs can be as large as n * (n - 1) / 2,
        // which does not fit in int for large n.
        long pairCount = 0L;

        // left marks the beginning of the current valid window.
        int left = 0;

        // Step 2: Expand the window using right.
        // For each right index, we adjust left until the distance condition is satisfied.
        for (int right = 0; right < positions.length; right++) {

            // While the current window is invalid, move left forward.
            //
            // Important detail:
            // We cast to long before subtraction to avoid any risk of integer overflow,
            // even though the given constraints are still safe for int subtraction.
            while ((long) positions[right] - positions[left] > maxDistance) {
                left++;
            }

            // At this point, the window [left, right] is valid:
            // positions[right] - positions[left] <= maxDistance
            //
            // Because the array is sorted, that means:
            // positions[right] - positions[k] <= maxDistance for every k in [left, right]
            // and specifically for every k in [left, right - 1].
            //
            // Therefore, the current right element forms valid pairs with all previous elements
            // inside the window:
            // (left, right), (left+1, right), ..., (right-1, right)
            //
            // Number of such pairs = right - left
            pairCount += (right - left);
        }

        return pairCount;
    }

    /**
     * A helper method that runs the algorithm on a copy of the input array.
     *
     * This is useful for demonstrations because the main algorithm sorts the array in-place.
     * By copying the array first, we preserve the original input for printing or reuse.
     *
     * @param positions the original unsorted positions array
     * @param maxDistance the maximum allowed distance between two stations
     * @return the total number of valid pairs as a long
     * Time complexity: O(n log n)
     * Space complexity: O(n) because of the copied array
     */
    public long countPairsWithoutModifyingInput(int[] positions, int maxDistance) {
        if (positions == null) {
            return 0L;
        }
        int[] copy = Arrays.copyOf(positions, positions.length);
        return countPairsWithinDistance(copy, maxDistance);
    }

    /**
     * Demonstrates the solution with the sample inputs from the problem statement
     * and prints the results.
     *
     * This method also includes expected outputs so a beginner can easily compare
     * the computed result with the intended answer.
     *
     * @param args command-line arguments; not used
     * @return nothing
     * Time complexity: O(n log n) per demonstration case
     * Space complexity: O(n) for copied arrays used in demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] positions1 = {8, 1, 4, 10, 6};
        int maxDistance1 = 3;
        long result1 = solution.countPairsWithoutModifyingInput(positions1, maxDistance1);

        System.out.println("Example 1:");
        System.out.println("positions = " + Arrays.toString(positions1));
        System.out.println("maxDistance = " + maxDistance1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Example 2
        int[] positions2 = {5, 5, 5, 9};
        int maxDistance2 = 0;
        long result2 = solution.countPairsWithoutModifyingInput(positions2, maxDistance2);

        System.out.println("Example 2:");
        System.out.println("positions = " + Arrays.toString(positions2));
        System.out.println("maxDistance = " + maxDistance2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional quick sanity checks for beginners.

        // Single element -> no pairs
        int[] positions3 = {42};
        int maxDistance3 = 10;
        long result3 = solution.countPairsWithoutModifyingInput(positions3, maxDistance3);

        System.out.println("Additional Test 1:");
        System.out.println("positions = " + Arrays.toString(positions3));
        System.out.println("maxDistance = " + maxDistance3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        // All close together -> every pair is valid
        int[] positions4 = {1, 2, 3, 4};
        int maxDistance4 = 10;
        long result4 = solution.countPairsWithoutModifyingInput(positions4, maxDistance4);

        System.out.println("Additional Test 2:");
        System.out.println("positions = " + Arrays.toString(positions4));
        System.out.println("maxDistance = " + maxDistance4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 6");
    }
}