import java.util.*;

/*
 * Title: Count Concert Duos Within Stage Width
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A concert organizer wants to place pairs of performers on the same movable stage.
 * Each performer has a preferred standing position represented by an integer in the
 * array positions. Two performers can share the stage if the absolute difference
 * between their preferred positions is less than or equal to widthLimit.
 *
 * Your task is to count how many distinct performer pairs can be placed together.
 *
 * Given an integer array positions and an integer widthLimit, return the number of
 * index pairs (i, j) such that i < j and |positions[i] - positions[j]| <= widthLimit.
 *
 * The array is not guaranteed to be sorted. A correct and efficient solution is
 * expected for large inputs, so a brute-force O(n^2) approach may time out.
 * Think about how sorting and a two-pointers sweep can help count many valid pairs at once.
 *
 * Constraints:
 * - 1 <= positions.length <= 200000
 * - -1000000000 <= positions[i] <= 1000000000
 * - 0 <= widthLimit <= 1000000000
 * - The answer may be larger than 32-bit integer range, so use a 64-bit integer type where necessary.
 *
 * Example 1:
 * Input: positions = [1, 3, 4, 9], widthLimit = 3
 * Output: 3
 * Explanation: Valid pairs are (1,3), (1,4), and (3,4). Any pair involving 9 exceeds the allowed stage width.
 *
 * Example 2:
 * Input: positions = [8, 2, 2, 5, 11], widthLimit = 0
 * Output: 1
 * Explanation: Only the two performers with preferred position 2 can share the stage,
 * because widthLimit is 0 and equal positions are required.
 */

public class Solution {

    /**
     * Counts how many distinct index pairs (i, j) satisfy:
     * i < j and |positions[i] - positions[j]| <= widthLimit.
     *
     * Core idea:
     * 1. Sort the array.
     * 2. After sorting, for any fixed right pointer, all valid left-side elements
     *    form one continuous range.
     * 3. Use a sliding window / two-pointers technique to maintain the smallest
     *    left index such that the difference with the current right index is within the limit.
     * 4. If the current valid window is [left, right], then the number of valid pairs
     *    ending at right is exactly (right - left), because every index from left to right - 1
     *    can pair with right.
     *
     * Why sorting helps:
     * Once sorted, for right > left, we know positions[right] >= positions[left].
     * Therefore:
     * |positions[right] - positions[left]| = positions[right] - positions[left]
     * So we only need to check one directional difference.
     *
     * @param positions the preferred standing positions of performers
     * @param widthLimit the maximum allowed absolute difference between two performers
     * @return the total number of valid pairs as a long
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer sweep
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public long countConcertDuos(int[] positions, int widthLimit) {
        // Defensive handling:
        // If there are fewer than 2 performers, no pair can exist.
        if (positions == null || positions.length < 2) {
            return 0L;
        }

        // Sort the positions so that nearby values become adjacent.
        // This transforms the absolute-difference condition into a simple window condition.
        Arrays.sort(positions);

        // This will store the final number of valid pairs.
        // We use long because the number of pairs can exceed Integer.MAX_VALUE.
        long totalPairs = 0L;

        // 'left' marks the beginning of the current valid window.
        // For each 'right', we will move 'left' forward until:
        // positions[right] - positions[left] <= widthLimit
        int left = 0;

        // Expand the window by moving 'right' from left to right across the sorted array.
        for (int right = 0; right < positions.length; right++) {

            // While the current window is invalid, shrink it from the left.
            //
            // Because the array is sorted:
            // - positions[right] is the largest value in the current window
            // - positions[left] is the smallest value in the current window
            //
            // If their difference is too large, then 'left' cannot pair with 'right',
            // and neither can any even smaller left index (which we already passed).
            // So we move 'left' forward until the window becomes valid again.
            while ((long) positions[right] - positions[left] > widthLimit) {
                left++;
            }

            // Now the window [left, right] is valid:
            // For every index k in [left, right - 1], we have:
            // positions[right] - positions[k] <= widthLimit
            //
            // Therefore, the current 'right' can form a valid pair with exactly:
            // right - left
            // previous elements.
            //
            // Example:
            // if left = 2 and right = 5,
            // then valid partners for index 5 are indices 2, 3, 4 -> total 3 = 5 - 2
            totalPairs += (right - left);
        }

        return totalPairs;
    }

    /**
     * A helper method that demonstrates the same algorithm but first copies the input array.
     * This is useful when callers want to preserve the original order of the input.
     *
     * @param positions the preferred standing positions of performers
     * @param widthLimit the maximum allowed absolute difference between two performers
     * @return the total number of valid pairs as a long
     * Time complexity: O(n log n)
     * Space complexity: O(n) because of the copied array
     */
    public long countConcertDuosPreserveInput(int[] positions, int widthLimit) {
        if (positions == null) {
            return 0L;
        }

        int[] copy = Arrays.copyOf(positions, positions.length);
        return countConcertDuos(copy, widthLimit);
    }

    /**
     * Runs sample demonstrations from the problem statement and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo calls, excluding the algorithm work inside each call
     * Space complexity: O(1), excluding the algorithm work inside each call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // positions = [1, 3, 4, 9], widthLimit = 3
        // Valid pairs:
        // (1,3), (1,4), (3,4) => total = 3
        int[] positions1 = {1, 3, 4, 9};
        int widthLimit1 = 3;
        long result1 = solution.countConcertDuosPreserveInput(positions1, widthLimit1);
        System.out.println("Example 1 Result: " + result1); // Expected: 3

        // Example 2:
        // positions = [8, 2, 2, 5, 11], widthLimit = 0
        // After sorting: [2, 2, 5, 8, 11]
        // Only the two 2s form a valid pair => total = 1
        int[] positions2 = {8, 2, 2, 5, 11};
        int widthLimit2 = 0;
        long result2 = solution.countConcertDuosPreserveInput(positions2, widthLimit2);
        System.out.println("Example 2 Result: " + result2); // Expected: 1

        // Additional beginner-friendly checks:

        // Single element -> no pairs
        int[] positions3 = {7};
        int widthLimit3 = 5;
        long result3 = solution.countConcertDuosPreserveInput(positions3, widthLimit3);
        System.out.println("Single Element Result: " + result3); // Expected: 0

        // All equal values, widthLimit = 0
        // Number of pairs among 4 equal elements = 4 * 3 / 2 = 6
        int[] positions4 = {5, 5, 5, 5};
        int widthLimit4 = 0;
        long result4 = solution.countConcertDuosPreserveInput(positions4, widthLimit4);
        System.out.println("All Equal Result: " + result4); // Expected: 6

        // Mixed negative and positive values
        int[] positions5 = {-3, -1, 2, 4};
        int widthLimit5 = 3;
        long result5 = solution.countConcertDuosPreserveInput(positions5, widthLimit5);
        System.out.println("Mixed Values Result: " + result5); // Expected: 2
        // Valid pairs are (-3,-1) and (2,4)
    }
}