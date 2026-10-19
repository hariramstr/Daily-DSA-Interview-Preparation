import java.util.*;

/*
 * Title: Count Lifeboat Crews Within Capacity
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an integer array weights where weights[i] is the weight of the i-th rescue worker,
 * and an integer limit representing the maximum total weight a small lifeboat can safely carry.
 * A valid crew consists of exactly two different workers whose combined weight is less than or equal to limit.
 *
 * Your task is to return the total number of distinct valid crews that can be formed.
 * Two crews are considered different if they use a different pair of indices, even if the workers have the same weight.
 *
 * For example, if three workers all weigh 40 and the limit is 80, then all three index pairs are valid crews.
 *
 * The input array is not guaranteed to be sorted. An efficient solution is expected; a quadratic scan
 * over all pairs may time out for large inputs. Think carefully about how sorting and a two-pointer
 * strategy can help count many valid pairs at once.
 *
 * Constraints:
 * - 2 <= weights.length <= 200000
 * - 1 <= weights[i] <= 10^9
 * - 1 <= limit <= 2 * 10^9
 * - Return the answer as a 64-bit integer if your language requires it
 *
 * Example 1:
 * Input: weights = [70, 50, 80, 40], limit = 120
 * Output: 4
 * Explanation: After sorting, the weights are [40, 50, 70, 80].
 * Valid pairs are (40,50), (40,70), (40,80), and (50,70).
 *
 * Example 2:
 * Input: weights = [30, 30, 60, 90, 50], limit = 90
 * Output: 5
 * Explanation: Valid pairs are formed by indices corresponding to weights (30,30), (30,60), (30,60)
 * using the other 30, and (30,50), (30,50) using the other 30. Pairs involving 90 are invalid,
 * and 50 + 60 exceeds the limit.
 */

public class Solution {

    /**
     * Counts how many distinct pairs of workers can form a valid crew whose total weight
     * does not exceed the given limit.
     *
     * The method sorts the array and then uses a two-pointer strategy:
     * - One pointer starts at the lightest worker.
     * - One pointer starts at the heaviest worker.
     *
     * Key observation:
     * If weights[left] + weights[right] <= limit, then weights[left] can pair with
     * every worker from left + 1 through right, because the array is sorted and all of
     * those workers are no heavier than weights[right].
     *
     * So instead of checking each of those pairs one by one, we can count all of them at once.
     *
     * @param weights the array of worker weights; each element represents one worker
     * @param limit the maximum allowed total weight for a crew of exactly two workers
     * @return the total number of distinct valid pairs as a 64-bit integer
     *
     * Time complexity: O(n log n), due to sorting; the two-pointer scan itself is O(n)
     * Space complexity: O(1) extra space beyond the sorting implementation details used by Arrays.sort for primitives
     */
    public long countValidCrews(int[] weights, int limit) {
        // Sort the weights first.
        // This is the crucial preparation step that makes the two-pointer technique possible.
        // After sorting:
        // - smaller values are on the left
        // - larger values are on the right
        Arrays.sort(weights);

        // 'left' points to the current lightest worker we are considering.
        int left = 0;

        // 'right' points to the current heaviest worker we are considering.
        int right = weights.length - 1;

        // We use long because the number of valid pairs can be large.
        // In the worst case, if all pairs are valid and n = 200000,
        // the answer is n * (n - 1) / 2 = 19,999,900,000, which does not fit in int.
        long count = 0L;

        // Continue while there are at least two different workers available.
        while (left < right) {
            // Compute the sum carefully.
            // Casting one operand to long avoids any risk of integer overflow,
            // even though the given constraints already fit within int addition.
            long sum = (long) weights[left] + weights[right];

            // Case 1:
            // The lightest current worker and the heaviest current worker can form a valid crew.
            if (sum <= limit) {
                // Because the array is sorted, weights[left] is the smallest among the current range.
                //
                // If weights[left] + weights[right] <= limit, then:
                // weights[left] + weights[k] <= limit for every k in [left + 1, right]
                // because weights[k] <= weights[right].
                //
                // That means the worker at 'left' can pair with ALL workers from left+1 to right.
                //
                // Number of such pairs:
                // (right - left)
                count += (right - left);

                // We have now counted every valid pair that starts with this 'left' worker.
                // So we move left forward to consider the next lightest worker.
                left++;
            } else {
                // Case 2:
                // weights[left] + weights[right] > limit
                //
                // The heaviest current worker is too heavy to pair with the lightest current worker.
                // Since every worker between left and right is at least as heavy as weights[left],
                // the worker at 'right' also cannot pair with any worker from left to right-1.
                //
                // Therefore, no valid pair includes the current 'right' worker within this range.
                // We must move 'right' leftward to try a lighter worker.
                right--;
            }
        }

        return count;
    }

    /**
     * Helper method to run one demonstration test case and print the result.
     *
     * @param weights the worker weights for the test case
     * @param limit the maximum allowed total weight
     * @return the computed number of valid crews
     *
     * Time complexity: O(n log n), because it delegates to the counting method
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public long demonstrateCase(int[] weights, int limit) {
        // We clone the array so that the original sample input remains unchanged for display purposes.
        int[] copy = weights.clone();

        long result = countValidCrews(copy, limit);

        System.out.println("weights = " + Arrays.toString(weights));
        System.out.println("limit = " + limit);
        System.out.println("valid crews = " + result);
        System.out.println();

        return result;
    }

    /**
     * Main method demonstrating the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n log n) per demonstrated test case
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        // Input: [70, 50, 80, 40], limit = 120
        // Sorted: [40, 50, 70, 80]
        // Valid pairs:
        // (40,50), (40,70), (40,80), (50,70)
        // Expected output: 4
        solution.demonstrateCase(new int[]{70, 50, 80, 40}, 120);

        // Sample 2
        // Input: [30, 30, 60, 90, 50], limit = 90
        // Valid pairs by indices correspond to:
        // (30,30), (30,60), (30,60), (30,50), (30,50)
        // Expected output: 5
        solution.demonstrateCase(new int[]{30, 30, 60, 90, 50}, 90);

        // Additional quick checks for beginner-friendly understanding.

        // All equal and all valid:
        // Three workers of weight 40, limit 80
        // Distinct index pairs = 3 choose 2 = 3
        solution.demonstrateCase(new int[]{40, 40, 40}, 80);

        // No valid pairs:
        solution.demonstrateCase(new int[]{100, 110, 120}, 50);
    }
}