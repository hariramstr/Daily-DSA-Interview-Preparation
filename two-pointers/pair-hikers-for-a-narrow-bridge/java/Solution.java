import java.util.*;

/*
 * Title: Pair Hikers for a Narrow Bridge
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * A hiking club needs to send people across a narrow suspension bridge. For safety,
 * at most two hikers can be on the bridge at the same time, and the combined weight
 * of any pair on the bridge cannot exceed a given limit. Each hiker must cross exactly
 * once. If a hiker cannot be paired safely, they must cross alone.
 *
 * You are given an integer array weights where weights[i] is the weight of the i-th hiker,
 * and an integer limit representing the maximum total weight allowed for one crossing.
 * Return the minimum number of crossings needed to get everyone across.
 *
 * This is a practical optimization problem: using too many solo crossings wastes time,
 * but unsafe pairings are not allowed. An efficient solution should take advantage of
 * sorting and a two-pointer strategy to decide whether the lightest remaining hiker can
 * share a crossing with the heaviest remaining hiker.
 *
 * Constraints:
 * - 1 <= weights.length <= 50000
 * - 1 <= weights[i] <= limit <= 100000
 * - Every hiker must be assigned to exactly one crossing
 * - A crossing may contain one or two hikers only
 *
 * Example 1:
 * Input: weights = [70, 50, 80, 50], limit = 100
 * Output: 3
 * Explanation: One optimal plan is [50, 50], [70], [80]. So the minimum number of crossings is 3.
 *
 * Example 2:
 * Input: weights = [40, 60, 60, 40, 30], limit = 90
 * Output: 3
 * Explanation: One optimal plan is [30, 60], [40, 40], [60]. No arrangement can do it in fewer than 3 crossings.
 */

public class Solution {

    /**
     * Computes the minimum number of bridge crossings needed so that every hiker crosses exactly once.
     *
     * Strategy:
     * 1. Sort the weights.
     * 2. Use two pointers:
     *    - left points to the lightest remaining hiker
     *    - right points to the heaviest remaining hiker
     * 3. Always try to place the heaviest remaining hiker into the current crossing.
     *    - If the lightest remaining hiker can join without exceeding the limit,
     *      pair them together.
     *    - Otherwise, the heaviest hiker must cross alone.
     * 4. Count one crossing for each decision.
     *
     * Why this works:
     * Pairing the heaviest hiker with anyone heavier than the lightest would only make
     * the total weight larger. So if the heaviest cannot pair with the lightest, then
     * the heaviest cannot pair with anyone and must go alone.
     *
     * @param weights an array where weights[i] is the weight of the i-th hiker
     * @param limit the maximum allowed total weight for one crossing
     * @return the minimum number of crossings required
     *
     * Time complexity: O(n log n), because sorting dominates the runtime
     * Space complexity: O(1) extra space apart from the sorting implementation details
     */
    public int minCrossings(int[] weights, int limit) {
        // Sort the array so we can efficiently match the lightest and heaviest hikers.
        Arrays.sort(weights);

        // left starts at the lightest hiker not yet assigned to a crossing.
        int left = 0;

        // right starts at the heaviest hiker not yet assigned to a crossing.
        int right = weights.length - 1;

        // This will store the total number of crossings used.
        int crossings = 0;

        // Continue until all hikers have been assigned.
        while (left <= right) {
            // We are definitely going to use one crossing for the heaviest remaining hiker.
            // The only question is:
            // - can the lightest remaining hiker join them?
            // - or must the heaviest go alone?
            if (weights[left] + weights[right] <= limit) {
                // The lightest and heaviest can safely cross together.
                // So we use up both hikers.
                left++;
                right--;
            } else {
                // The heaviest cannot pair with the lightest.
                // Since the array is sorted, the heaviest also cannot pair with anyone else.
                // Therefore, the heaviest must cross alone.
                right--;
            }

            // In either case, exactly one crossing has been used.
            crossings++;
        }

        return crossings;
    }

    /**
     * A helper method that demonstrates the algorithm in a beginner-friendly way by
     * printing the sorted input and the final answer.
     *
     * @param weights the array of hiker weights
     * @param limit the maximum allowed total weight for one crossing
     * @return the computed minimum number of crossings
     *
     * Time complexity: O(n log n), because it calls the main algorithm on a copied array
     * Space complexity: O(n), due to copying the input for display and safe reuse
     */
    public int demonstrateAndSolve(int[] weights, int limit) {
        // Make a copy so the original input remains unchanged for display purposes.
        int[] copy = Arrays.copyOf(weights, weights.length);

        System.out.println("Weights: " + Arrays.toString(copy));
        System.out.println("Limit: " + limit);

        int answer = minCrossings(copy, limit);

        System.out.println("Minimum crossings: " + answer);
        System.out.println();

        return answer;
    }

    /**
     * Main method to run sample demonstrations.
     *
     * This method verifies the examples from the problem statement:
     * Example 1:
     * - Input: [70, 50, 80, 50], limit = 100
     * - Expected output: 3
     *
     * Example 2:
     * - Input: [40, 60, 60, 40, 30], limit = 90
     * - Expected output: 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n log n) per demonstration
     * Space complexity: O(n) for copied arrays used in demonstrations
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement.
        int[] weights1 = {70, 50, 80, 50};
        int limit1 = 100;
        int result1 = solution.demonstrateAndSolve(weights1, limit1);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println("Matches expected: " + (result1 == 3));
        System.out.println();

        // Example 2 from the problem statement.
        int[] weights2 = {40, 60, 60, 40, 30};
        int limit2 = 90;
        int result2 = solution.demonstrateAndSolve(weights2, limit2);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println("Matches expected: " + (result2 == 3));
        System.out.println();

        // Additional small test for clarity.
        int[] weights3 = {50};
        int limit3 = 100;
        int result3 = solution.demonstrateAndSolve(weights3, limit3);
        System.out.println("Expected: 1, Actual: " + result3);
        System.out.println("Matches expected: " + (result3 == 1));
    }
}