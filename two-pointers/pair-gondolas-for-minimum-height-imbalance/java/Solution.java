import java.util.*;

/*
 * Pair Gondolas for Minimum Height Imbalance
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A mountain resort is loading visitors into 2-person gondolas. Each gondola must contain exactly two people,
 * and the resort wants the ride experience to feel as balanced as possible. You are given an integer array
 * heights where heights[i] is the height of the i-th visitor. The number of visitors is always even.
 *
 * Your task is to pair up all visitors so that every visitor is used exactly once, and the maximum height
 * difference inside any single gondola is as small as possible. Return that minimum possible maximum difference.
 *
 * For example, if one pairing creates gondolas with height differences [1, 4, 2], then the quality of that
 * pairing is 4 because the worst gondola has a difference of 4. Among all valid pairings, you must minimize
 * this worst-case value.
 *
 * A common interview insight is that after sorting the array, the best strategy is not arbitrary matching.
 * The challenge is to reason about which visitors should be paired together so the largest gap is minimized globally.
 *
 * Constraints:
 * - 2 <= heights.length <= 200000
 * - heights.length is even
 * - 1 <= heights[i] <= 1000000000
 *
 * Example 1:
 * Input: heights = [150, 160, 155, 168]
 * Output: 8
 * Explanation:
 * Sort the heights to [150, 155, 160, 168].
 * If we pair adjacent visitors, we get differences 5 and 8, so the maximum is 8.
 * Other pairings such as (150, 168) and (155, 160) give differences 18 and 5, so the maximum becomes 18.
 * Thus the minimum possible maximum difference is 8.
 *
 * Example 2:
 * Input: heights = [172, 180, 169, 171, 175, 178]
 * Output: 3
 * Explanation:
 * After sorting we get [169, 171, 172, 175, 178, 180].
 * Pairing adjacent visitors gives (169,171), (172,175), and (178,180), with differences 2, 3, and 2.
 * The maximum difference is 3.
 * No other complete pairing can make the worst gondola have a smaller difference, so the answer is 3.
 */

public class Solution {

    /**
     * Computes the minimum possible value of the maximum height difference among all gondolas.
     *
     * Core idea:
     * 1. Sort the heights.
     * 2. Pair adjacent people in the sorted order.
     * 3. The answer is the largest difference among those adjacent pairs.
     *
     * Why this works:
     * In a sorted array, any "crossing" pairing can be transformed into non-crossing pairings without making
     * the maximum pair difference worse. Repeating this reasoning leads to pairing neighbors together.
     * Therefore, the optimal arrangement is:
     * (a[0], a[1]), (a[2], a[3]), (a[4], a[5]), ...
     *
     * @param heights the array of visitor heights; length is guaranteed to be even
     * @return the minimum possible maximum difference inside any gondola
     * Time complexity: O(n log n), due to sorting
     * Space complexity: O(1) extra space beyond the sort's internal usage; Java's Arrays.sort(int[]) is in-place for primitives
     */
    public int minimumHeightImbalance(int[] heights) {
        // Defensive check for beginner-friendliness.
        // The problem guarantees valid input, but this makes the method safer if reused elsewhere.
        if (heights == null || heights.length == 0 || (heights.length % 2 != 0)) {
            throw new IllegalArgumentException("Input array must be non-null, non-empty, and have even length.");
        }

        // Step 1: Sort the heights.
        // After sorting, nearby values are the closest in magnitude.
        // This is the key setup that allows us to minimize the worst pair difference.
        Arrays.sort(heights);

        // Step 2: Walk through the sorted array two elements at a time.
        // We pair:
        // heights[0] with heights[1]
        // heights[2] with heights[3]
        // heights[4] with heights[5]
        // ...
        //
        // For each pair, compute the difference and track the largest one.
        int answer = 0;

        for (int i = 0; i < heights.length; i += 2) {
            // Since the array is sorted, heights[i + 1] >= heights[i].
            int difference = heights[i + 1] - heights[i];

            // The quality of a complete pairing is the maximum difference among all pairs.
            // So we keep updating the running maximum.
            answer = Math.max(answer, difference);
        }

        // Step 3: Return the smallest possible worst-case difference.
        return answer;
    }

    /**
     * A helper method that prints the sorted adjacent pairing used by the optimal strategy.
     * This is purely for demonstration in main and helps beginners see exactly what is happening.
     *
     * @param heights the array of visitor heights; this method does not modify the caller's original array
     * @return a human-readable string showing the sorted array, each pair, and the final maximum difference
     * Time complexity: O(n log n), due to sorting
     * Space complexity: O(n), because we clone the array and build output text
     */
    public String explainPairing(int[] heights) {
        if (heights == null || heights.length == 0 || (heights.length % 2 != 0)) {
            throw new IllegalArgumentException("Input array must be non-null, non-empty, and have even length.");
        }

        // Clone so that this explanation method does not mutate the original input.
        int[] copy = heights.clone();
        Arrays.sort(copy);

        StringBuilder sb = new StringBuilder();
        sb.append("Sorted heights: ").append(Arrays.toString(copy)).append(System.lineSeparator());
        sb.append("Pairs:").append(System.lineSeparator());

        int maxDifference = 0;

        for (int i = 0; i < copy.length; i += 2) {
            int left = copy[i];
            int right = copy[i + 1];
            int difference = right - left;
            maxDifference = Math.max(maxDifference, difference);

            sb.append("(")
              .append(left)
              .append(", ")
              .append(right)
              .append(") -> difference = ")
              .append(difference)
              .append(System.lineSeparator());
        }

        sb.append("Minimum possible maximum difference = ").append(maxDifference);
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments; not used
     * @return nothing
     * Time complexity: O(n log n) per demonstration case
     * Space complexity: O(n) for explanation output and cloned arrays in the demo
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement
        int[] heights1 = {150, 160, 155, 168};
        int result1 = solution.minimumHeightImbalance(heights1.clone());
        System.out.println("Example 1 Input: " + Arrays.toString(heights1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected Output: 8");
        System.out.println(solution.explainPairing(heights1));
        System.out.println();

        // Example 2 from the problem statement
        int[] heights2 = {172, 180, 169, 171, 175, 178};
        int result2 = solution.minimumHeightImbalance(heights2.clone());
        System.out.println("Example 2 Input: " + Arrays.toString(heights2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected Output: 3");
        System.out.println(solution.explainPairing(heights2));
        System.out.println();

        // Additional quick demo
        int[] heights3 = {5, 1, 9, 2, 6, 7};
        int result3 = solution.minimumHeightImbalance(heights3.clone());
        System.out.println("Additional Demo Input: " + Arrays.toString(heights3));
        System.out.println("Additional Demo Output: " + result3);
        System.out.println(solution.explainPairing(heights3));
    }
}