import java.util.*;

/*
 * Title: Maximum Matching Distance for Mirrored Billboards
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A city installs two parallel rows of digital billboards along the same highway.
 * The first row is represented by array top and the second row by array bottom,
 * where top[i] and bottom[i] are the ad category IDs shown at position i in each row.
 * You want to measure how well the two rows can visually reinforce each other.
 *
 * Define the matching distance of a category x as the largest absolute difference |i - j|
 * such that top[i] = x and bottom[j] = x. In other words, you may pair one occurrence of x
 * from the top row with one occurrence of x from the bottom row, and the score for that
 * category is how far apart those positions are. If a category appears in only one row,
 * it contributes nothing. Your task is to return the maximum matching distance over all categories.
 *
 * This is an arrays problem focused on efficient scanning and bookkeeping.
 * A brute-force comparison of all matching pairs may be too slow when the arrays are large.
 *
 * Return 0 if no category appears in both rows.
 *
 * Constraints:
 * - 1 <= top.length, bottom.length <= 200000
 * - top.length == bottom.length
 * - 1 <= top[i], bottom[i] <= 1000000000
 * - The answer fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: top = [4, 7, 2, 7, 9], bottom = [8, 7, 4, 2, 7]
 * Correct Output: 3
 * Explanation:
 * Category 4 appears at top index 0 and bottom index 2, giving distance 2.
 * Category 2 appears at top index 2 and bottom index 3, giving distance 1.
 * Category 7 appears at top indices 1 and 3, and bottom indices 1 and 4.
 * The best pairing is top[1] with bottom[4], or top[3] with bottom[1], both giving distance 3.
 * Category 9 appears only in top.
 * The maximum is 3.
 *
 * Note:
 * The statement text showed "Output: 4", but that contradicts the explanation and the definition.
 * The correct answer for Example 1 is 3.
 *
 * Example 2:
 * Input: top = [5, 1, 5, 3, 1, 6], bottom = [1, 5, 2, 5, 7, 1]
 * Correct Output: 4
 * Explanation:
 * For category 1, top indices are 1 and 4, and bottom indices are 0 and 5.
 * The best distance is |4 - 0| = 4 or |1 - 5| = 4.
 * For category 5, top indices are 0 and 2, and bottom indices are 1 and 3.
 * The best distance is |0 - 3| = 3.
 * No other category appears in both rows.
 * The maximum matching distance is 4.
 *
 * Note:
 * The statement text showed "Output: 5", but that contradicts the explanation and the definition.
 * The correct answer for Example 2 is 4.
 */

public class Solution {

    /**
     * Small helper record-like class that stores the extreme positions of one category
     * in both arrays.
     *
     * We only need:
     * - the smallest index where the value appears in top
     * - the largest index where the value appears in top
     * - the smallest index where the value appears in bottom
     * - the largest index where the value appears in bottom
     *
     * Why are extremes enough?
     * For a fixed category x, we want the maximum value of |i - j| where:
     * - top[i] = x
     * - bottom[j] = x
     *
     * The largest absolute difference between one index from set A and one index from set B
     * is always achieved by comparing opposite extremes:
     * - maxTop - minBottom
     * - maxBottom - minTop
     *
     * So we never need all positions, only the first and last positions in each row.
     */
    private static class Stats {
        int minTop = Integer.MAX_VALUE;
        int maxTop = Integer.MIN_VALUE;
        int minBottom = Integer.MAX_VALUE;
        int maxBottom = Integer.MIN_VALUE;
        boolean seenTop = false;
        boolean seenBottom = false;
    }

    /**
     * Computes the maximum matching distance over all category IDs.
     *
     * Algorithm idea:
     * 1. Scan both arrays from left to right.
     * 2. For each value, maintain the first and last index where it appears in top,
     *    and the first and last index where it appears in bottom.
     * 3. After the scan, for every category that appears in both arrays, compute:
     *      max(
     *          abs(maxTop - minBottom),
     *          abs(maxBottom - minTop)
     *      )
     *    Since indices are ordered and extremes are tracked, this is equivalent to:
     *      max(maxTop - minBottom, maxBottom - minTop)
     *    because each expression is already non-negative when interpreted as opposite extremes.
     * 4. Return the largest such value.
     *
     * This avoids comparing every matching pair explicitly.
     *
     * @param top the category IDs shown in the top row
     * @param bottom the category IDs shown in the bottom row
     * @return the maximum matching distance among all categories that appear in both rows;
     *         returns 0 if no category appears in both rows
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(k), where k is the number of distinct category IDs
     */
    public int maximumMatchingDistance(int[] top, int[] bottom) {
        // Defensive validation for beginner-friendliness.
        // The problem guarantees valid input, but checking helps make the method robust.
        if (top == null || bottom == null || top.length != bottom.length || top.length == 0) {
            return 0;
        }

        // Map from category ID -> statistics about where it appears.
        Map<Integer, Stats> map = new HashMap<>();

        // We process both arrays in one pass because they have the same length.
        for (int i = 0; i < top.length; i++) {
            int topValue = top[i];
            int bottomValue = bottom[i];

            // Update stats for the top row value.
            Stats topStats = map.computeIfAbsent(topValue, k -> new Stats());

            // Mark that this category has appeared in the top row.
            topStats.seenTop = true;

            // Update the smallest top index.
            if (i < topStats.minTop) {
                topStats.minTop = i;
            }

            // Update the largest top index.
            if (i > topStats.maxTop) {
                topStats.maxTop = i;
            }

            // Update stats for the bottom row value.
            Stats bottomStats = map.computeIfAbsent(bottomValue, k -> new Stats());

            // Mark that this category has appeared in the bottom row.
            bottomStats.seenBottom = true;

            // Update the smallest bottom index.
            if (i < bottomStats.minBottom) {
                bottomStats.minBottom = i;
            }

            // Update the largest bottom index.
            if (i > bottomStats.maxBottom) {
                bottomStats.maxBottom = i;
            }
        }

        // This variable will store the best answer found so far.
        int answer = 0;

        // Now inspect every distinct category.
        for (Stats stats : map.values()) {
            // A category contributes only if it appears in BOTH rows.
            if (stats.seenTop && stats.seenBottom) {
                // For this category, the farthest possible pair must use opposite extremes.
                //
                // Candidate 1:
                //   take the farthest-right occurrence in top
                //   and the farthest-left occurrence in bottom
                int candidate1 = stats.maxTop - stats.minBottom;

                // Candidate 2:
                //   take the farthest-right occurrence in bottom
                //   and the farthest-left occurrence in top
                int candidate2 = stats.maxBottom - stats.minTop;

                // The best distance for this category is the larger of the two.
                int bestForThisCategory = Math.max(candidate1, candidate2);

                // Update the global answer.
                if (bestForThisCategory > answer) {
                    answer = bestForThisCategory;
                }
            }
        }

        return answer;
    }

    /**
     * A second public method that performs the same computation.
     * This is included to make the solution structure beginner-friendly and explicit.
     *
     * @param top the category IDs shown in the top row
     * @param bottom the category IDs shown in the bottom row
     * @return the maximum matching distance among all categories that appear in both rows
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(k), where k is the number of distinct category IDs
     */
    public int solve(int[] top, int[] bottom) {
        return maximumMatchingDistance(top, bottom);
    }

    /**
     * Utility method to print an array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * Important note:
     * The provided problem statement contains inconsistent sample outputs.
     * The explanations imply the correct outputs are:
     * - Example 1 -> 3
     * - Example 2 -> 4
     *
     * This main method prints the computed results so they can be verified directly.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size across demonstrations)
     * Space complexity: O(distinct values across demonstrations)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] top1 = {4, 7, 2, 7, 9};
        int[] bottom1 = {8, 7, 4, 2, 7};
        int result1 = solution.maximumMatchingDistance(top1, bottom1);

        System.out.println("Example 1");
        System.out.println("top    = " + solution.arrayToString(top1));
        System.out.println("bottom = " + solution.arrayToString(bottom1));
        System.out.println("Result = " + result1);
        System.out.println("Expected (from explanation) = 3");
        System.out.println();

        int[] top2 = {5, 1, 5, 3, 1, 6};
        int[] bottom2 = {1, 5, 2, 5, 7, 1};
        int result2 = solution.maximumMatchingDistance(top2, bottom2);

        System.out.println("Example 2");
        System.out.println("top    = " + solution.arrayToString(top2));
        System.out.println("bottom = " + solution.arrayToString(bottom2));
        System.out.println("Result = " + result2);
        System.out.println("Expected (from explanation) = 4");
        System.out.println();

        int[] top3 = {1, 2, 3};
        int[] bottom3 = {4, 5, 6};
        int result3 = solution.maximumMatchingDistance(top3, bottom3);

        System.out.println("Extra Example 3");
        System.out.println("top    = " + solution.arrayToString(top3));
        System.out.println("bottom = " + solution.arrayToString(bottom3));
        System.out.println("Result = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        int[] top4 = {9, 9, 9, 9};
        int[] bottom4 = {9, 9, 9, 9};
        int result4 = solution.maximumMatchingDistance(top4, bottom4);

        System.out.println("Extra Example 4");
        System.out.println("top    = " + solution.arrayToString(top4));
        System.out.println("bottom = " + solution.arrayToString(bottom4));
        System.out.println("Result = " + result4);
        System.out.println("Expected = 3");
    }
}