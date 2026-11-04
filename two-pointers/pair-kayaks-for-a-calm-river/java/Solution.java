import java.util.*;

/*
 * Title: Pair Kayaks for a Calm River
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * A river tour company has a list of tourists waiting to use two-person kayaks.
 * Each tourist has a weight, and every kayak can hold at most two people as long
 * as their combined weight does not exceed a given limit. You want to use as few
 * kayaks as possible while sending everyone on the river.
 *
 * Given an integer array weights where weights[i] is the weight of the ith tourist,
 * and an integer limit representing the maximum total weight a kayak can safely carry,
 * return the minimum number of kayaks needed to carry all tourists.
 *
 * Each kayak may carry either one tourist or two tourists. A tourist cannot be split
 * across kayaks, and every tourist must be assigned to exactly one kayak.
 *
 * This problem is intended to be solved efficiently using sorting and two pointers.
 * After sorting the weights, try pairing the lightest remaining tourist with the
 * heaviest remaining tourist whenever possible. If they cannot share a kayak, the
 * heaviest tourist must go alone.
 *
 * Constraints:
 * - 1 <= weights.length <= 50000
 * - 1 <= weights[i] <= limit <= 100000
 * - It is always possible to place each tourist in some kayak individually.
 *
 * Example 1:
 * Input: weights = [70, 50, 80, 50], limit = 100
 * Output: 3
 * Explanation: One optimal arrangement is (50, 50), (70), (80). So 3 kayaks are needed.
 *
 * Example 2:
 * Input: weights = [40, 60, 55, 45, 30], limit = 90
 * Output: 3
 * Explanation: One optimal arrangement is (30, 60), (40, 45), (55). This uses the fewest possible kayaks.
 */

public class Solution {

    /**
     * Computes the minimum number of kayaks needed to carry all tourists.
     *
     * The strategy is:
     * 1. Sort the weights.
     * 2. Use two pointers:
     *    - left points to the lightest remaining tourist
     *    - right points to the heaviest remaining tourist
     * 3. Try to pair the lightest and heaviest:
     *    - If their combined weight is within the limit, they share one kayak.
     *    - Otherwise, the heaviest must go alone.
     * 4. In either case, one kayak is used per iteration.
     *
     * Why this works:
     * Pairing the heaviest tourist with the lightest possible tourist is the best greedy choice.
     * If the heaviest cannot pair with the lightest, then the heaviest cannot pair with anyone,
     * because everyone else is heavier than the lightest. So the heaviest must go alone.
     *
     * @param weights the array of tourist weights
     * @param limit the maximum total weight allowed in one kayak
     * @return the minimum number of kayaks required
     *
     * Time complexity: O(n log n), due to sorting the array
     * Space complexity: O(1) extra space excluding the sorting implementation details used by Java
     */
    public int minKayaks(int[] weights, int limit) {
        // Sort the array so we can efficiently match the lightest and heaviest remaining tourists.
        Arrays.sort(weights);

        // left starts at the lightest tourist.
        int left = 0;

        // right starts at the heaviest tourist.
        int right = weights.length - 1;

        // This will count how many kayaks we use.
        int kayaks = 0;

        // Continue until all tourists have been assigned to a kayak.
        while (left <= right) {
            // We always need at least one kayak for the heaviest remaining tourist.
            // So each loop iteration will consume exactly one kayak.
            kayaks++;

            // If left == right, there is only one tourist left.
            // That tourist must take one kayak alone.
            if (left == right) {
                break;
            }

            // Check whether the lightest and heaviest remaining tourists can share a kayak.
            if (weights[left] + weights[right] <= limit) {
                // They can share:
                // - move left forward because the lightest tourist is now assigned
                // - move right backward because the heaviest tourist is now assigned
                left++;
                right--;
            } else {
                // They cannot share:
                // The heaviest tourist must go alone.
                // We only move right backward because only that tourist is assigned.
                right--;
            }
        }

        return kayaks;
    }

    /**
     * A helper method that prints a test case and its result in a beginner-friendly format.
     *
     * @param weights the array of tourist weights for the test case
     * @param limit the maximum total weight allowed in one kayak
     * @return the computed minimum number of kayaks for the given input
     *
     * Time complexity: O(n log n), because it delegates to the main algorithm
     * Space complexity: O(1) extra space excluding sorting implementation details
     */
    public int demonstrateCase(int[] weights, int limit) {
        // We clone the array so that printing the original input remains intuitive,
        // since the algorithm sorts the array in-place.
        int[] copyForSolving = weights.clone();

        int result = minKayaks(copyForSolving, limit);

        System.out.println("weights = " + Arrays.toString(weights) + ", limit = " + limit);
        System.out.println("Minimum kayaks needed = " + result);
        System.out.println();

        return result;
    }

    /**
     * Runs sample demonstrations of the solution.
     *
     * This main method verifies the examples from the problem statement:
     * Example 1:
     * - Input: [70, 50, 80, 50], limit = 100
     * - Sorted: [50, 50, 70, 80]
     * - Pair 50 + 80 = 130 > 100, so 80 goes alone
     * - Pair 50 + 70 = 120 > 100, so 70 goes alone
     * - Pair 50 + 50 = 100, so they share
     * - Total = 3
     *
     * Example 2:
     * - Input: [40, 60, 55, 45, 30], limit = 90
     * - Sorted: [30, 40, 45, 55, 60]
     * - Pair 30 + 60 = 90, share
     * - Pair 40 + 55 = 95 > 90, so 55 goes alone
     * - Pair 40 + 45 = 85, share
     * - Total = 3
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n log n) per demonstrated test case
     * Space complexity: O(1) extra space excluding sorting implementation details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement
        int[] weights1 = {70, 50, 80, 50};
        int limit1 = 100;
        int result1 = solution.demonstrateCase(weights1, limit1);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println("Matches expected? " + (result1 == 3));
        System.out.println();

        // Example 2 from the problem statement
        int[] weights2 = {40, 60, 55, 45, 30};
        int limit2 = 90;
        int result2 = solution.demonstrateCase(weights2, limit2);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println("Matches expected? " + (result2 == 3));
        System.out.println();

        // Additional small demonstration
        int[] weights3 = {20, 20, 50, 70};
        int limit3 = 90;
        int result3 = solution.demonstrateCase(weights3, limit3);
        System.out.println("Additional example result: " + result3);
    }
}