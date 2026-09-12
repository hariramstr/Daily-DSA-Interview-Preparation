import java.util.*;

/*
 * Title: Count Rescue Boat Pairs Within Safe Weight Range
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an integer array weights where weights[i] is the weight of the i-th passenger
 * waiting for evacuation. A rescue boat can carry exactly two passengers, and for safety reasons
 * the combined weight of the pair must be between lowLimit and highLimit, inclusive.
 *
 * Your task is to count how many distinct pairs of passengers (i, j) with i < j can be assigned
 * to the same boat.
 *
 * Two pairs are considered distinct if they use different passenger indices, even if the weights
 * are equal. Each passenger may appear in many counted pairs because you are only asked to count
 * all valid possible pairings, not to build a final non-overlapping assignment.
 *
 * Design an algorithm faster than O(n^2). A typical solution sorts the array and uses a two-pointer
 * strategy combined with range counting.
 *
 * Constraints:
 * - 2 <= weights.length <= 2 * 10^5
 * - 1 <= weights[i] <= 10^9
 * - 1 <= lowLimit <= highLimit <= 2 * 10^9
 *
 * Example 1:
 * Input: weights = [2, 3, 5, 6, 8], lowLimit = 7, highLimit = 10
 * Output: 5
 * Explanation: Valid pairs are (2,5), (2,6), (2,8), (3,5), and (3,6). Their sums are 7, 8, 10, 8, and 9.
 *
 * Example 2:
 * Input: weights = [1, 1, 4, 4, 7], lowLimit = 5, highLimit = 8
 * Output: 7
 * Explanation:
 * - Four pairs formed by choosing one 1 and one 4
 * - Two pairs formed by choosing one 1 and 7
 * - One pair formed by the two 4s
 * Total = 7
 */

public class Solution {

    /**
     * Counts how many distinct index pairs (i, j), where i < j, have a sum in the inclusive range
     * [lowLimit, highLimit].
     *
     * The key idea is:
     * 1. Count how many pairs have sum <= highLimit.
     * 2. Count how many pairs have sum <= lowLimit - 1.
     * 3. Subtract the second count from the first.
     *
     * This works because:
     * - pairs with sum <= highLimit includes everything below or equal to the upper bound
     * - pairs with sum <= lowLimit - 1 includes everything strictly below the lower bound
     * - subtracting leaves exactly the pairs whose sums are in [lowLimit, highLimit]
     *
     * @param weights the array of passenger weights
     * @param lowLimit the minimum allowed combined weight of a valid pair, inclusive
     * @param highLimit the maximum allowed combined weight of a valid pair, inclusive
     * @return the number of valid distinct pairs
     * Time complexity: O(n log n) because of sorting, plus O(n) for the two-pointer scans
     * Space complexity: O(n) if counting the cloned sorted array, or O(1) extra beyond the clone implementation details
     */
    public long countRescueBoatPairs(int[] weights, int lowLimit, int highLimit) {
        // We sort a copy so the original input remains unchanged for demonstration or reuse.
        int[] sorted = weights.clone();
        Arrays.sort(sorted);

        // Count pairs with sum <= highLimit.
        long atMostHigh = countPairsWithSumAtMost(sorted, highLimit);

        // Count pairs with sum <= lowLimit - 1.
        // We cast to long before subtracting to avoid any risk around integer boundaries.
        long belowLow = countPairsWithSumAtMost(sorted, (long) lowLimit - 1);

        // The difference gives the number of pairs whose sums lie inside the desired inclusive range.
        return atMostHigh - belowLow;
    }

    /**
     * Counts how many distinct pairs (i, j), where i < j, have sum <= limit.
     *
     * This method assumes the array is already sorted in non-decreasing order.
     *
     * Two-pointer logic:
     * - Start one pointer at the left end and one at the right end.
     * - If sorted[left] + sorted[right] <= limit, then:
     *   because the array is sorted, every index from left+1 through right can pair with left
     *   and still satisfy the limit.
     *   So we add (right - left) pairs at once, then move left forward.
     * - Otherwise, the sum is too large, so we must reduce it by moving right backward.
     *
     * This is much faster than checking all O(n^2) pairs individually.
     *
     * @param sortedWeights the sorted array of passenger weights
     * @param limit the maximum allowed sum for the counted pairs
     * @return the number of distinct pairs with sum <= limit
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long countPairsWithSumAtMost(int[] sortedWeights, long limit) {
        int left = 0;
        int right = sortedWeights.length - 1;
        long count = 0L;

        // Continue while there are at least two different indices available.
        while (left < right) {
            long sum = (long) sortedWeights[left] + sortedWeights[right];

            if (sum <= limit) {
                /*
                 * Very important observation:
                 *
                 * Since the array is sorted:
                 * sortedWeights[left] <= sortedWeights[left + 1] <= ... <= sortedWeights[right]
                 *
                 * If sortedWeights[left] + sortedWeights[right] <= limit,
                 * then for the same 'left', pairing with any index between left+1 and right
                 * will also be <= limit, because those values are <= sortedWeights[right].
                 *
                 * Therefore, all these pairs are valid:
                 * (left, left+1), (left, left+2), ..., (left, right)
                 *
                 * Number of such pairs = right - left
                 *
                 * After counting them, we move left forward because we have already counted
                 * every valid pair that starts with this left index.
                 */
                count += (right - left);
                left++;
            } else {
                /*
                 * The current sum is too large.
                 *
                 * Because sortedWeights[right] is the largest candidate currently available,
                 * keeping 'right' and moving 'left' to the right would only make the sum stay
                 * large or become even larger.
                 *
                 * So the only useful move is to decrease 'right' to try a smaller value.
                 */
                right--;
            }
        }

        return count;
    }

    /**
     * Helper method to print a test case in a beginner-friendly format.
     *
     * @param weights the passenger weights
     * @param lowLimit the inclusive lower bound for pair sums
     * @param highLimit the inclusive upper bound for pair sums
     * @return the computed number of valid pairs
     * Time complexity: O(n log n)
     * Space complexity: O(n) due to sorting a clone
     */
    public long demonstrate(int[] weights, int lowLimit, int highLimit) {
        long result = countRescueBoatPairs(weights, lowLimit, highLimit);
        System.out.println("weights = " + Arrays.toString(weights));
        System.out.println("lowLimit = " + lowLimit + ", highLimit = " + highLimit);
        System.out.println("Valid pair count = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the algorithm on the sample inputs and a few extra checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the number and size of demonstration cases
     * Space complexity: Depends on the input arrays used in the demo
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        // weights = [2, 3, 5, 6, 8], lowLimit = 7, highLimit = 10
        // Valid pairs:
        // (2,5)=7, (2,6)=8, (2,8)=10, (3,5)=8, (3,6)=9 => total 5
        long result1 = solution.demonstrate(new int[]{2, 3, 5, 6, 8}, 7, 10);
        System.out.println("Expected = 5, Actual = " + result1);
        System.out.println();

        // Sample 2 corrected by the prompt's own explanation.
        // weights = [1, 1, 4, 4, 7], lowLimit = 5, highLimit = 8
        // Valid pairs:
        // 1 with 4: 2 * 2 = 4 pairs
        // 1 with 7: 2 * 1 = 2 pairs
        // 4 with 4: 1 pair
        // Total = 7
        long result2 = solution.demonstrate(new int[]{1, 1, 4, 4, 7}, 5, 8);
        System.out.println("Expected = 7, Actual = " + result2);
        System.out.println();

        // Extra quick sanity checks.
        long result3 = solution.demonstrate(new int[]{5, 5, 5}, 10, 10);
        System.out.println("Expected = 3, Actual = " + result3);
        System.out.println();

        long result4 = solution.demonstrate(new int[]{1, 2, 3, 4}, 100, 200);
        System.out.println("Expected = 0, Actual = " + result4);
    }
}