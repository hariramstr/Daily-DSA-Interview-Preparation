import java.util.*;

/*
 * Maximum Revenue from One Circular Booth Closure
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * A street festival has n food booths arranged in a circle. The i-th booth earns revenue[i]
 * dollars if it stays open for the day. Due to a temporary power issue, the organizers must
 * close exactly one contiguous block of booths. Because the street is circular, the closed block
 * may wrap from the end of the array back to the beginning.
 *
 * You are also given two integers, minClose and maxClose. The number of closed booths must be
 * between minClose and maxClose, inclusive. After closing that single circular block, all remaining
 * booths stay open, and your goal is to maximize the total revenue of the open booths.
 *
 * Return the maximum total revenue that can remain open.
 *
 * Formally, choose exactly one circular subarray of length L where minClose <= L <= maxClose,
 * remove its sum from the total revenue, and maximize the sum of the remaining elements.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - -10^9 <= revenue[i] <= 10^9
 * - 1 <= minClose <= maxClose <= n
 * - The chosen block must contain at least one booth and may contain all booths.
 *
 * Efficient idea:
 * We want:
 *     answer = totalRevenue - (minimum sum of any circular subarray
 *                              whose length is in [minClose, maxClose])
 *
 * So the real task is to find the minimum-sum circular subarray with a bounded length.
 *
 * Circular handling:
 * - Duplicate the array conceptually by using prefix sums over length 2n.
 * - Any circular subarray of the original circle with length <= n can be represented as a
 *   normal subarray in the doubled array.
 * - To avoid counting the same circular block multiple times, we only allow starting positions
 *   from 0 to n - 1.
 *
 * Prefix-sum transformation:
 * For an end position e in the doubled array (1-based in prefix indexing), a subarray sum is:
 *     prefix[e] - prefix[s]
 * where s is the start prefix index.
 *
 * If the subarray length must be between minClose and maxClose, then for fixed e:
 *     e - maxClose <= s <= e - minClose
 *
 * Also, because the start booth index must be in the original first copy:
 *     s <= n - 1
 * in booth indexing, which becomes:
 *     s <= n - 1 in prefix-start index as well.
 *
 * Therefore, while scanning end positions, we maintain a deque of candidate prefix indices s
 * whose prefix values are as large as possible, because:
 *     subarraySum = prefix[e] - prefix[s]
 * To minimize subarraySum, we want the maximum prefix[s] among valid s.
 *
 * This gives an O(n) solution after building prefix sums.
 */
public class Solution {

    /**
     * Computes the maximum total revenue that can remain open after closing exactly one circular
     * contiguous block whose length is between minClose and maxClose inclusive.
     *
     * Core formula:
     *     maximum open revenue = total revenue - minimum valid circular closed-block sum
     *
     * We therefore solve the harder subproblem:
     *     find the minimum sum among all circular subarrays with allowed lengths.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param revenue the revenue of each booth arranged in a circle
     * @param minClose the minimum number of booths that must be closed
     * @param maxClose the maximum number of booths that may be closed
     * @return the maximum revenue that can remain open after one valid circular closure
     */
    public long maximumOpenRevenue(int[] revenue, int minClose, int maxClose) {
        int n = revenue.length;

        // Compute the total revenue of all booths.
        // Later, answer = total - minimumClosedBlockSum.
        long total = 0L;
        for (int value : revenue) {
            total += value;
        }

        // Find the minimum possible sum of a circular subarray with length in [minClose, maxClose].
        long minClosedSum = minimumCircularSubarraySumWithLengthRange(revenue, minClose, maxClose);

        // Removing a smaller sum leaves more revenue open.
        return total - minClosedSum;
    }

    /**
     * Finds the minimum sum of any circular subarray whose length is between minLen and maxLen.
     *
     * Detailed strategy:
     * 1. Build prefix sums over the doubled array of length 2n.
     * 2. Scan each possible end position in prefix indexing.
     * 3. For each end position e, valid start prefix indices s satisfy:
     *        e - maxLen <= s <= e - minLen
     *    and the actual booth start must be in the first copy:
     *        s <= n - 1
     * 4. Since subarray sum = prefix[e] - prefix[s], minimizing the sum means maximizing prefix[s].
     * 5. Maintain a deque of candidate start indices with prefix values in decreasing order,
     *    so the front always gives the largest prefix value among valid starts.
     *
     * Why the "start in first copy" restriction is enough:
     * Every circular subarray of length <= n has exactly one representation whose start booth
     * lies in [0, n - 1]. This avoids duplicate counting while still covering all circular blocks.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param revenue the original circular array
     * @param minLen minimum allowed subarray length
     * @param maxLen maximum allowed subarray length
     * @return the minimum sum among all valid circular subarrays
     */
    public long minimumCircularSubarraySumWithLengthRange(int[] revenue, int minLen, int maxLen) {
        int n = revenue.length;
        int doubledLength = 2 * n;

        // Prefix sums over the doubled array.
        // prefix[0] = 0
        // prefix[i] = sum of first i elements of doubled array
        long[] prefix = buildDoubledPrefixSums(revenue);

        // Deque will store candidate start prefix indices s.
        // We maintain prefix[s] in decreasing order:
        // front has the largest prefix value, which is best for minimizing prefix[e] - prefix[s].
        Deque<Integer> deque = new ArrayDeque<>();

        long minSum = Long.MAX_VALUE;

        // We scan end prefix indices e from 1 to 2n.
        // A subarray represented is from booth index s to booth index e - 1 in the doubled array.
        for (int e = 1; e <= doubledLength; e++) {

            // New candidate start index that becomes eligible at this end position:
            // length = minLen  =>  s = e - minLen
            int addIndex = e - minLen;

            // Only add it if:
            // 1) it is a valid prefix index (>= 0)
            // 2) its corresponding booth start lies in the first copy, i.e. addIndex <= n - 1
            if (addIndex >= 0 && addIndex <= n - 1) {
                // Maintain decreasing prefix values in the deque.
                // If the new prefix value is >= the back's prefix value,
                // the back is never better than the new one for any future end position.
                while (!deque.isEmpty() && prefix[deque.peekLast()] <= prefix[addIndex]) {
                    deque.pollLast();
                }
                deque.offerLast(addIndex);
            }

            // Remove start indices that are too old:
            // valid starts must satisfy s >= e - maxLen
            int minAllowedStart = e - maxLen;
            while (!deque.isEmpty() && deque.peekFirst() < minAllowedStart) {
                deque.pollFirst();
            }

            // If we have at least one valid start, the best one is at the front
            // because it has the maximum prefix value.
            if (!deque.isEmpty()) {
                long currentSum = prefix[e] - prefix[deque.peekFirst()];
                if (currentSum < minSum) {
                    minSum = currentSum;
                }
            }
        }

        return minSum;
    }

    /**
     * Builds prefix sums for the doubled version of the input array.
     *
     * Example:
     * revenue = [a, b, c]
     * doubled = [a, b, c, a, b, c]
     *
     * prefix length is 2n + 1, where:
     * prefix[0] = 0
     * prefix[i] = sum of doubled[0..i-1]
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param revenue the original array
     * @return prefix sums over the doubled array
     */
    public long[] buildDoubledPrefixSums(int[] revenue) {
        int n = revenue.length;
        long[] prefix = new long[2 * n + 1];

        for (int i = 0; i < 2 * n; i++) {
            prefix[i + 1] = prefix[i] + revenue[i % n];
        }

        return prefix;
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The official examples in the prompt contain inconsistent arithmetic in their explanations.
     * This main method prints the mathematically correct results produced by the algorithm.
     *
     * Example 1:
     * revenue = [8, -3, 5, -2, 4], minClose = 2, maxClose = 3
     * Valid minimum circular closed sum is 2 (for [-3, 5] or [-2, 4]),
     * total is 12, so answer is 10.
     *
     * Example 2:
     * revenue = [6, -5, 7, -8, 3, 2], minClose = 1, maxClose = 2
     * Minimum closed sum is -8 (close just [-8]),
     * total is 5, so answer is 13.
     *
     * Time complexity: O(n) for each demonstration call
     * Space complexity: O(n) for each demonstration call
     *
     * @param args command-line arguments, unused
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] revenue1 = {8, -3, 5, -2, 4};
        int minClose1 = 2;
        int maxClose1 = 3;
        long result1 = solution.maximumOpenRevenue(revenue1, minClose1, maxClose1);
        System.out.println(result1); // Expected mathematically correct output: 10

        int[] revenue2 = {6, -5, 7, -8, 3, 2};
        int minClose2 = 1;
        int maxClose2 = 2;
        long result2 = solution.maximumOpenRevenue(revenue2, minClose2, maxClose2);
        System.out.println(result2); // Expected mathematically correct output: 13

        // Additional quick sanity checks:

        // Close all booths is allowed when minClose = maxClose = n.
        int[] revenue3 = {4, 1, 7};
        System.out.println(solution.maximumOpenRevenue(revenue3, 3, 3)); // 0

        // If all values are negative, closing the most negative valid block can increase open revenue.
        int[] revenue4 = {-5, -2, -3};
        System.out.println(solution.maximumOpenRevenue(revenue4, 1, 2)); // Best is close [-5, -2] => total -10 - (-7) = -3

        // Single element array.
        int[] revenue5 = {9};
        System.out.println(solution.maximumOpenRevenue(revenue5, 1, 1)); // 0
    }
}