import java.util.*;

/*
 * Title: Minimum Rafts to Carry River Hikers
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A tour company needs to transport hikers across a river using small rafts.
 * Each raft can carry at most two hikers, and the combined weight of the hikers
 * on a raft cannot exceed a given limit. You are given an integer array weights
 * where weights[i] is the weight of the i-th hiker, and an integer limit
 * representing the maximum total weight a single raft can safely carry.
 *
 * Return the minimum number of rafts required to carry every hiker across the river.
 *
 * A raft may carry either one hiker or two hikers. Every hiker must be assigned
 * to exactly one raft. You may pair hikers in any way you want, as long as the
 * weight limit is not exceeded.
 *
 * This problem is designed to test whether you can recognize when sorting and a
 * two-pointer strategy lead to an optimal greedy solution. A correct solution
 * should be efficient enough for large inputs.
 *
 * Constraints:
 * - 1 <= weights.length <= 100000
 * - 1 <= weights[i] <= limit <= 100000
 * - It is guaranteed that each individual hiker can fit on a raft alone.
 *
 * Example 1:
 * Input: weights = [3, 5, 3, 4], limit = 5
 * Output: 4
 * Explanation: No valid pair can include the hiker with weight 5, and the two
 * hikers with weight 3 cannot share a raft because 3 + 3 > 5. So each hiker
 * must ride alone.
 *
 * Example 2:
 * Input: weights = [1, 2, 2, 3], limit = 3
 * Output: 3
 * Explanation: One optimal assignment is (1,2), (2), and (3). That uses 3 rafts,
 * which is the minimum possible.
 */

public class Solution {

    /**
     * Computes the minimum number of rafts needed to carry all hikers.
     *
     * The strategy is:
     * 1. Sort the weights in non-decreasing order.
     * 2. Use two pointers:
     *    - left points to the lightest remaining hiker
     *    - right points to the heaviest remaining hiker
     * 3. Always place the heaviest remaining hiker on a raft.
     *    - If the lightest remaining hiker can share the raft with the heaviest
     *      without exceeding the limit, pair them together.
     *    - Otherwise, the heaviest rides alone.
     * 4. Count one raft for each step until all hikers are assigned.
     *
     * Why this greedy approach works:
     * - The heaviest hiker is the hardest to place.
     * - If the heaviest cannot pair with the lightest, then the heaviest cannot
     *   pair with anyone else either, because everyone else is heavier than the lightest.
     * - If the heaviest can pair with the lightest, doing so is safe and optimal,
     *   because it preserves as much room as possible among the remaining hikers.
     *
     * @param weights the array of hiker weights
     * @param limit the maximum total weight a single raft can carry
     * @return the minimum number of rafts required
     * Time complexity: O(n log n) due to sorting
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public int minRafts(int[] weights, int limit) {
        // Sort the array so we can efficiently try to match the lightest and heaviest hikers.
        Arrays.sort(weights);

        // 'left' starts at the lightest hiker not yet assigned to a raft.
        int left = 0;

        // 'right' starts at the heaviest hiker not yet assigned to a raft.
        int right = weights.length - 1;

        // This will count how many rafts we use in total.
        int rafts = 0;

        // Continue until all hikers have been assigned.
        while (left <= right) {
            // We will always use one raft for the heaviest remaining hiker.
            // So count one raft now.
            rafts++;

            // Check whether the lightest and heaviest remaining hikers can share a raft.
            if (weights[left] + weights[right] <= limit) {
                // They can share.
                // Move 'left' forward because the lightest hiker is now assigned.
                left++;
            }

            // Whether paired or alone, the heaviest hiker is always assigned in this step.
            // Move 'right' backward.
            right--;
        }

        return rafts;
    }

    /**
     * A helper method that demonstrates the algorithm on a copy of the input
     * and prints the sorted array and final answer. This is useful for beginners
     * who want to see the result clearly without modifying the original sample array.
     *
     * @param weights the array of hiker weights
     * @param limit the maximum total weight a single raft can carry
     * @return the minimum number of rafts required
     * Time complexity: O(n log n)
     * Space complexity: O(n) because this method creates a copy for demonstration
     */
    public int minRaftsWithDisplay(int[] weights, int limit) {
        int[] copy = Arrays.copyOf(weights, weights.length);
        int answer = minRafts(copy, limit);
        System.out.println("Weights: " + Arrays.toString(weights));
        System.out.println("Limit: " + limit);
        System.out.println("Minimum rafts needed: " + answer);
        return answer;
    }

    /**
     * Runs sample demonstrations from the problem statement.
     *
     * This method also verifies the expected outputs for the provided examples:
     * - Example 1 should produce 4
     * - Example 2 should produce 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: O(n) in the display helper due to array copying
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // weights = [3, 5, 3, 4], limit = 5
        // Sorted: [3, 3, 4, 5]
        // 5 cannot pair with 3 -> alone
        // 4 cannot pair with 3 -> alone
        // 3 cannot pair with 3 -> alone
        // last 3 -> alone
        // Total = 4
        int[] weights1 = {3, 5, 3, 4};
        int limit1 = 5;
        int result1 = solution.minRaftsWithDisplay(weights1, limit1);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result1);
        System.out.println();

        // Example 2:
        // weights = [1, 2, 2, 3], limit = 3
        // Sorted: [1, 2, 2, 3]
        // 3 cannot pair with 1 -> alone
        // 2 can pair with 1 -> together
        // remaining 2 -> alone
        // Total = 3
        int[] weights2 = {1, 2, 2, 3};
        int limit2 = 3;
        int result2 = solution.minRaftsWithDisplay(weights2, limit2);
        System.out.println("Expected: 3");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // Additional demonstration
        int[] weights3 = {1, 2};
        int limit3 = 3;
        int result3 = solution.minRaftsWithDisplay(weights3, limit3);
        System.out.println("Expected: 1");
        System.out.println("Actual:   " + result3);
    }
}