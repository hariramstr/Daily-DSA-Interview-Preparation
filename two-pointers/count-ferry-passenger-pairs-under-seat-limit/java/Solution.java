import java.util.*;

/*
 * Title: Count Ferry Passenger Pairs Under Seat Limit
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A ferry operator wants to offer a discounted ticket to every pair of passengers who can share one bench.
 * You are given an integer array weights where weights[i] is the weight of the i-th passenger, and an integer
 * limit representing the maximum total weight that a single bench can safely support. Each discounted pair
 * must consist of two different passengers, and a pair is considered valid if the sum of their weights is
 * less than or equal to limit.
 *
 * Return the total number of distinct valid pairs (i, j) such that 0 <= i < j < n and
 * weights[i] + weights[j] <= limit.
 *
 * Your solution should be efficient enough for large inputs. A brute-force O(n^2) approach may be too slow
 * when the passenger list is long. Think about how sorting and a two-pointer strategy can help count many
 * pairs at once.
 *
 * Constraints:
 * - 2 <= weights.length <= 200000
 * - 1 <= weights[i] <= 1000000000
 * - 1 <= limit <= 2000000000
 * - The answer can be large, so use a 64-bit integer type if your language requires it.
 *
 * Example 1:
 * Input: weights = [2, 3, 4, 5], limit = 7
 * Output: 4
 * Explanation: Valid pairs are (2,3), (2,4), (2,5), and (3,4). Pair (3,5) and (4,5) exceed the limit.
 *
 * Example 2:
 * Input: weights = [1, 1, 2, 2, 3], limit = 3
 * Output: 3
 * Explanation:
 * The valid pairs are:
 * - one pair formed by the two 1s
 * - four pairs combining a 1 with a 2 would be possible only if there were two 1s and two 2s, but each such
 *   pair sums to 3 and is valid
 * - no pair involving 3 is valid because 1 + 3 = 4 > 3
 *
 * For the given input [1, 1, 2, 2, 3] and limit = 3, the actual valid pairs are:
 * - (1 at index 0, 1 at index 1)
 * - (1 at index 0, 2 at index 2)
 * - (1 at index 0, 2 at index 3)
 * - (1 at index 1, 2 at index 2)
 * - (1 at index 1, 2 at index 3)
 * Total = 5
 *
 * Therefore, the stated output "6" in the prompt is inconsistent with the input and rule.
 * The correct output for Example 2 is 5, and this implementation follows the problem rule exactly:
 * count all distinct pairs whose sum is <= limit.
 */

public class Solution {

    /**
     * Counts the number of distinct passenger pairs whose combined weight does not exceed the bench limit.
     *
     * Algorithm overview:
     * 1. Sort the array so lighter passengers are on the left and heavier passengers are on the right.
     * 2. Use two pointers:
     *    - left starts at the lightest passenger
     *    - right starts at the heaviest passenger
     * 3. If weights[left] + weights[right] <= limit, then:
     *    - weights[left] can pair with every passenger from left + 1 through right
     *    - because the array is sorted, all those passengers are <= weights[right]
     *    - therefore all those sums are also <= limit
     *    - so we add (right - left) pairs at once
     *    - then move left forward to consider the next lightest passenger
     * 4. Otherwise, if the sum is too large:
     *    - the heaviest passenger at right cannot pair with the current lightest passenger
     *    - and since everyone between left and right is at least as heavy as weights[left],
     *      the heaviest passenger also cannot pair with any heavier left-side candidate
     *    - so we must move right backward
     *
     * This avoids checking every pair individually and runs efficiently for large inputs.
     *
     * @param weights the array of passenger weights
     * @param limit the maximum allowed total weight for a pair sharing one bench
     * @return the total number of distinct valid pairs as a long
     * Time complexity: O(n log n) due to sorting, where n is the number of passengers
     * Space complexity: O(log n) to O(n) depending on the sorting implementation details used by Java
     */
    public long countValidPairs(int[] weights, int limit) {
        // Defensive handling: although constraints guarantee at least 2 elements,
        // this makes the method safer and easier to reuse.
        if (weights == null || weights.length < 2) {
            return 0L;
        }

        // Sort the weights first.
        // This is the key step that makes the two-pointer counting strategy possible.
        Arrays.sort(weights);

        // We use long for the answer because the number of pairs can be large.
        // For n = 200000, the maximum number of pairs is n * (n - 1) / 2,
        // which is about 19,999,900,000 and does not fit in int.
        long pairCount = 0L;

        // left points to the lightest remaining passenger.
        int left = 0;

        // right points to the heaviest remaining passenger.
        int right = weights.length - 1;

        // Continue until the pointers cross.
        // We only consider pairs where left < right.
        while (left < right) {
            // Use long when adding to avoid any risk of integer overflow,
            // even though the given constraints are still safe for int addition.
            long currentSum = (long) weights[left] + weights[right];

            if (currentSum <= limit) {
                /*
                 * Very important counting insight:
                 *
                 * Since the array is sorted:
                 * weights[left] <= weights[left + 1] <= ... <= weights[right]
                 *
                 * We already know:
                 * weights[left] + weights[right] <= limit
                 *
                 * Therefore:
                 * weights[left] + weights[k] <= limit for every k in [left + 1, right]
                 * because weights[k] <= weights[right]
                 *
                 * That means the passenger at index left can form valid pairs with:
                 * left + 1, left + 2, ..., right
                 *
                 * Number of such pairs = right - left
                 *
                 * We add all of them at once instead of checking one by one.
                 */
                pairCount += (right - left);

                // After counting all pairs involving the current left passenger,
                // move left forward to process the next passenger.
                left++;
            } else {
                /*
                 * If the lightest + heaviest is already too heavy,
                 * then the heaviest passenger cannot pair with the current left passenger.
                 *
                 * Because the array is sorted, any passenger between left and right
                 * is at least as heavy as weights[left].
                 *
                 * So replacing weights[left] with any passenger to its right
                 * would only keep the sum the same or make it larger.
                 *
                 * Therefore, the passenger at index right cannot form a valid pair
                 * with anyone from left to right - 1.
                 *
                 * So we move right backward to try a lighter heaviest passenger.
                 */
                right--;
            }
        }

        return pairCount;
    }

    /**
     * Convenience method that preserves the caller's original array by working on a copy.
     * This is useful in demonstrations or when the input array should not be modified.
     *
     * @param weights the array of passenger weights
     * @param limit the maximum allowed total weight for a pair sharing one bench
     * @return the total number of distinct valid pairs as a long
     * Time complexity: O(n log n) due to sorting
     * Space complexity: O(n) for the copied array
     */
    public long countValidPairsWithoutModifyingInput(int[] weights, int limit) {
        if (weights == null) {
            return 0L;
        }

        int[] copy = Arrays.copyOf(weights, weights.length);
        return countValidPairs(copy, limit);
    }

    /**
     * Runs a single demonstration test case and prints the input and result.
     *
     * @param weights the array of passenger weights
     * @param limit the maximum allowed total weight for a pair
     * @return no return value
     * Time complexity: O(n log n) because it calls the counting method
     * Space complexity: O(n) because it preserves the original input for display
     */
    public static void runDemo(int[] weights, int limit) {
        Solution solution = new Solution();

        long result = solution.countValidPairsWithoutModifyingInput(weights, limit);

        System.out.println("weights = " + Arrays.toString(weights));
        System.out.println("limit = " + limit);
        System.out.println("valid pair count = " + result);
        System.out.println();
    }

    /**
     * Demonstrates the solution using sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return no return value
     * Time complexity: Depends on the demo inputs; each test is O(n log n)
     * Space complexity: O(n) per test due to copying for display safety
     */
    public static void main(String[] args) {
        // Example 1 from the prompt:
        // weights = [2, 3, 4, 5], limit = 7
        // Valid pairs:
        // (2,3), (2,4), (2,5), (3,4) => total 4
        runDemo(new int[]{2, 3, 4, 5}, 7);

        // Example 2 from the prompt contains an inconsistency.
        // For weights = [1, 1, 2, 2, 3], limit = 3,
        // the correct valid pairs are:
        // (1,1), and the four (1,2) combinations => total 5
        runDemo(new int[]{1, 1, 2, 2, 3}, 3);

        // Additional beginner-friendly checks:

        // All pairs valid:
        // [1,2,3], limit 10 => 3 pairs
        runDemo(new int[]{1, 2, 3}, 10);

        // No pairs valid:
        // [5,6,7], limit 4 => 0 pairs
        runDemo(new int[]{5, 6, 7}, 4);

        // Duplicate values:
        // [3,3,3,3], limit 6 => all 6 pairs are valid
        runDemo(new int[]{3, 3, 3, 3}, 6);
    }
}