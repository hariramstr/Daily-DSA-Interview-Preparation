import java.util.*;

/*
 * Title: Pair Contestants for a Canoe Ride
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are organizing a team-building event where contestants will ride in two-person canoes.
 * Each canoe can carry at most 2 people, and the combined weight of the two people in the same
 * canoe cannot exceed a given limit. Some contestants may need to ride alone if no valid partner
 * is available.
 *
 * Given an integer array weights where weights[i] is the weight of the i-th contestant, and an
 * integer limit representing the maximum allowed total weight in one canoe, return the minimum
 * number of canoes needed to carry everyone.
 *
 * An efficient strategy is:
 * 1. Sort the weights.
 * 2. Use two pointers:
 *    - one pointer at the lightest remaining contestant
 *    - one pointer at the heaviest remaining contestant
 * 3. If the lightest and heaviest can share a canoe, place them together.
 * 4. Otherwise, the heaviest contestant must ride alone.
 *
 * Constraints:
 * - 1 <= weights.length <= 50000
 * - 1 <= weights[i] <= limit <= 30000
 * - Each canoe can carry at most 2 contestants
 *
 * Example 1:
 * Input: weights = [70, 50, 80, 50], limit = 100
 * Output: 3
 * Explanation: One optimal arrangement is (50, 50), (70), (80), so 3 canoes are required.
 *
 * Example 2:
 * Input: weights = [40, 60, 55, 45], limit = 100
 * Output: 2
 * Explanation: One optimal arrangement is (40, 60) and (45, 55), so only 2 canoes are needed.
 */

public class Solution {

    /**
     * Computes the minimum number of canoes needed to carry all contestants.
     *
     * The method sorts the weights and then uses a two-pointer greedy strategy:
     * - Try to pair the lightest remaining contestant with the heaviest remaining contestant.
     * - If they fit within the limit, they share one canoe.
     * - Otherwise, the heaviest contestant must go alone.
     *
     * This greedy choice is optimal because if the heaviest contestant cannot pair with the
     * lightest contestant, then the heaviest contestant cannot pair with anyone else either
     * (everyone else is heavier than the lightest).
     *
     * @param weights the array of contestant weights
     * @param limit the maximum total weight allowed in one canoe
     * @return the minimum number of canoes required to transport everyone
     * Time complexity: O(n log n) due to sorting, where n is the number of contestants.
     * Space complexity: O(1) extra space beyond the sorting cost if sorting in place is considered;
     * practically O(log n) due to Java's sorting implementation stack usage for primitives.
     */
    public int minCanoes(int[] weights, int limit) {
        // Sort the array so we can efficiently consider the lightest and heaviest people.
        Arrays.sort(weights);

        // left points to the lightest contestant not yet assigned to a canoe.
        int left = 0;

        // right points to the heaviest contestant not yet assigned to a canoe.
        int right = weights.length - 1;

        // This will count how many canoes we use.
        int canoes = 0;

        // Continue until all contestants have been assigned.
        while (left <= right) {
            // We will always use one canoe for the heaviest remaining contestant.
            // The only question is whether the lightest remaining contestant can join them.
            int heaviest = weights[right];
            int lightest = weights[left];

            // Check whether the lightest and heaviest together fit within the limit.
            if (lightest + heaviest <= limit) {
                // They can share a canoe.
                // So we move both pointers inward because both contestants are now assigned.
                left++;
                right--;
            } else {
                // They cannot share.
                // That means the heaviest contestant must go alone.
                // We only move the right pointer because only the heaviest contestant is assigned.
                right--;
            }

            // In either case, exactly one canoe has been used in this iteration.
            canoes++;
        }

        return canoes;
    }

    /**
     * Helper method to run one demonstration case and print the result.
     *
     * @param weights the array of contestant weights for the test case
     * @param limit the canoe weight limit for the test case
     * @return the computed minimum number of canoes for the given input
     * Time complexity: O(n log n), because it delegates to the main algorithm.
     * Space complexity: O(1) extra space beyond sorting-related overhead.
     */
    public int demonstrateCase(int[] weights, int limit) {
        // We copy the input array so the original demo data remains unchanged when sorting occurs.
        int[] copy = Arrays.copyOf(weights, weights.length);

        int result = minCanoes(copy, limit);

        System.out.println("weights = " + Arrays.toString(weights) + ", limit = " + limit);
        System.out.println("Minimum canoes needed = " + result);
        System.out.println();

        return result;
    }

    /**
     * Main method to demonstrate the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration case.
     * Space complexity: O(1) extra space beyond sorting-related overhead and copied demo arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: [70, 50, 80, 50], limit = 100
        // Sorted: [50, 50, 70, 80]
        // 50 + 80 = 130 > 100 -> 80 alone
        // 50 + 70 = 120 > 100 -> 70 alone
        // 50 + 50 = 100 <= 100 -> together
        // Total = 3
        int result1 = solution.demonstrateCase(new int[]{70, 50, 80, 50}, 100);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println();

        // Example 2:
        // Input: [40, 60, 55, 45], limit = 100
        // Sorted: [40, 45, 55, 60]
        // 40 + 60 = 100 <= 100 -> together
        // 45 + 55 = 100 <= 100 -> together
        // Total = 2
        int result2 = solution.demonstrateCase(new int[]{40, 60, 55, 45}, 100);
        System.out.println("Expected: 2, Actual: " + result2);
        System.out.println();

        // Additional beginner-friendly test cases.
        int result3 = solution.demonstrateCase(new int[]{100}, 100);
        System.out.println("Expected: 1, Actual: " + result3);
        System.out.println();

        int result4 = solution.demonstrateCase(new int[]{30, 30, 40, 60}, 60);
        System.out.println("Expected: 3, Actual: " + result4);
    }
}