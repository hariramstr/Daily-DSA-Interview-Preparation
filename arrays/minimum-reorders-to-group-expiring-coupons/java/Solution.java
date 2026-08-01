import java.util.*;

/*
 * Title: Minimum Reorders to Group Expiring Coupons
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array coupons where each value represents the expiration day of a coupon
 * in a checkout system. The coupons are displayed in a fixed row, and you want all coupons with the
 * same expiration day to appear in one contiguous block. The relative order of coupons inside a block
 * does not matter, and the blocks themselves may appear in any order.
 *
 * In one operation, you may pick any single coupon from its current position and insert it at any
 * other position in the row. This shifts the surrounding elements as needed. Return the minimum
 * number of such operations required so that, in the final arrangement, all equal expiration days
 * are grouped together.
 *
 * For example, if the row is [3, 1, 3, 2, 1], a valid final arrangement could be [3, 3, 1, 1, 2]
 * or [2, 1, 1, 3, 3]. Your goal is not to construct the arrangement, but to compute the fewest
 * insert operations needed.
 *
 * Constraints:
 * - 1 <= coupons.length <= 200
 * - 1 <= coupons[i] <= 20
 * - The answer fits in a 32-bit integer.
 *
 * Example 1:
 * Input: coupons = [3, 1, 3, 2, 1]
 * Output: 2
 * Explanation: Move the coupon with value 2 to the front, and move one coupon with value 1 next to
 * the other 1. After 2 operations, all equal values can be made contiguous.
 *
 * Example 2:
 * Input: coupons = [4, 4, 2, 2, 3]
 * Output: 0
 * Explanation: Coupons with the same expiration day are already grouped into contiguous blocks:
 * [4, 4], [2, 2], and [3].
 *
 * Core Idea:
 * We want to maximize how many coupons can stay in their current positions while the final array
 * becomes a concatenation of value-blocks, one block per distinct value, in some order.
 *
 * If we choose an order of distinct values, then the final arrangement consists of consecutive
 * segments whose lengths are exactly the frequencies of those values. For each segment, the best
 * we can do is keep in place the coupons already equal to that segment's value. Therefore:
 *
 *   maximum kept = maximum, over all block orders, of
 *                  sum of "how many positions in each assigned segment already contain that value"
 *
 * Then:
 *
 *   minimum operations = n - maximum kept
 *
 * Because one insertion move can relocate one coupon, every coupon not kept in place can be moved
 * into the correct block arrangement.
 *
 * We solve this with:
 * 1) Compress distinct values.
 * 2) Count frequency of each value.
 * 3) Precompute, for every value and every possible segment [l, r), how many occurrences of that
 *    value already lie in that segment.
 * 4) Use DP over subsets:
 *      dp[mask] = maximum number of coupons that can stay in place after arranging exactly the
 *                 set of values in 'mask' as the first blocks.
 *
 * Since coupon values are in [1, 20], the number of distinct values is at most 20, so subset DP
 * is feasible.
 */
public class Solution {

    /**
     * Computes the minimum number of insert operations needed so that all equal coupon values
     * become grouped into contiguous blocks.
     *
     * The method uses dynamic programming over subsets of distinct values.
     *
     * Detailed high-level steps:
     * 1. Identify the distinct coupon values.
     * 2. Count how many times each distinct value appears.
     * 3. Build prefix counts so we can quickly ask:
     *      "How many occurrences of value v are already inside positions [l, r)?"
     * 4. For every subset of values, determine how many positions are already occupied by the
     *    blocks represented by that subset. This gives the starting index of the next block.
     * 5. Transition DP by appending one more value-block at the end.
     * 6. The best DP value tells us how many coupons can remain in place.
     * 7. Answer = total coupons - maximum kept in place.
     *
     * @param coupons the array of coupon expiration days
     * @return the minimum number of insert operations required
     * Time complexity: O(k * 2^k + n * k), where k is the number of distinct values and n is the array length
     * Space complexity: O(k * 2^k + n * k)
     */
    public int minimumReorders(int[] coupons) {
        int n = coupons.length;

        // Step 1:
        // Gather distinct values and map each original coupon value to a compact index [0..k-1].
        //
        // Why do this?
        // Because subset DP works naturally on small indices. The actual coupon values can be any
        // integers in the allowed range, but we only care about distinct groups.
        Map<Integer, Integer> valueToIndex = new HashMap<>();
        List<Integer> distinctValues = new ArrayList<>();

        for (int value : coupons) {
            if (!valueToIndex.containsKey(value)) {
                valueToIndex.put(value, distinctValues.size());
                distinctValues.add(value);
            }
        }

        int k = distinctValues.size();

        // Step 2:
        // Count frequency of each distinct value.
        //
        // freq[i] = how many coupons belong to value with compressed index i.
        int[] freq = new int[k];
        for (int value : coupons) {
            int idx = valueToIndex.get(value);
            freq[idx]++;
        }

        // Step 3:
        // Build prefix occurrence counts.
        //
        // prefix[i][pos] = number of occurrences of compressed value i in coupons[0..pos-1]
        //
        // Then the number of occurrences of value i inside segment [l, r) is:
        // prefix[i][r] - prefix[i][l]
        //
        // This is crucial because if we decide that value i occupies final block [l, r),
        // then exactly those occurrences already sitting in [l, r) can stay in place.
        int[][] prefix = new int[k][n + 1];
        for (int pos = 0; pos < n; pos++) {
            int currentIndex = valueToIndex.get(coupons[pos]);
            for (int i = 0; i < k; i++) {
                prefix[i][pos + 1] = prefix[i][pos];
            }
            prefix[currentIndex][pos + 1]++;
        }

        // Step 4:
        // Precompute subset sizes in terms of total block lengths.
        //
        // blockLengthSum[mask] = total number of coupons covered by the blocks in 'mask'
        //
        // If 'mask' represents the set of values already placed first in the final arrangement,
        // then blockLengthSum[mask] tells us how many positions are already occupied.
        int totalMasks = 1 << k;
        int[] blockLengthSum = new int[totalMasks];

        for (int mask = 1; mask < totalMasks; mask++) {
            // Extract one set bit to build from a smaller subset.
            int bit = mask & -mask;
            int idx = Integer.numberOfTrailingZeros(bit);
            int previousMask = mask ^ bit;
            blockLengthSum[mask] = blockLengthSum[previousMask] + freq[idx];
        }

        // Step 5:
        // DP over subsets.
        //
        // dp[mask] = maximum number of coupons that can remain in place after arranging exactly
        //            the values in 'mask' as the first blocks of the final array.
        //
        // Initialization:
        // dp[0] = 0 because with no blocks placed, no coupons are kept yet.
        //
        // Transition:
        // Suppose we are at subset 'mask'. The next block starts at:
        //   start = blockLengthSum[mask]
        //
        // If we append value 'next' (not yet in mask), its block length is freq[next], so it occupies:
        //   [start, start + freq[next])
        //
        // The number of coupons of that value already in this segment is:
        //   prefix[next][start + freq[next]] - prefix[next][start]
        //
        // Those coupons can stay in place, so:
        //   dp[mask | (1 << next)] = max(dp[mask | (1 << next)], dp[mask] + keptHere)
        int[] dp = new int[totalMasks];
        Arrays.fill(dp, -1);
        dp[0] = 0;

        for (int mask = 0; mask < totalMasks; mask++) {
            if (dp[mask] == -1) {
                // This subset state is unreachable, so skip it.
                continue;
            }

            int start = blockLengthSum[mask];

            for (int next = 0; next < k; next++) {
                if ((mask & (1 << next)) != 0) {
                    // This value is already placed in the arrangement prefix.
                    continue;
                }

                int end = start + freq[next];

                // Count how many occurrences of 'next' are already inside the exact segment
                // where its final block would be placed.
                int keptHere = prefix[next][end] - prefix[next][start];

                int nextMask = mask | (1 << next);
                dp[nextMask] = Math.max(dp[nextMask], dp[mask] + keptHere);
            }
        }

        // Step 6:
        // The full mask means all value-blocks have been placed.
        // dp[fullMask] is the maximum number of coupons that can stay in place.
        int maxKept = dp[totalMasks - 1];

        // Step 7:
        // Every coupon not kept in place must be moved by an insertion operation.
        return n - maxKept;
    }

    /**
     * Helper method that returns how many occurrences of a compressed value appear in a half-open
     * interval [left, right) using a prefix-count table.
     *
     * This method is not required by the main algorithm, but it is useful for clarity and for
     * educational purposes.
     *
     * @param prefix the prefix count table where prefix[valueIndex][pos] stores occurrences in [0, pos)
     * @param valueIndex the compressed value index to query
     * @param left the inclusive left boundary of the segment
     * @param right the exclusive right boundary of the segment
     * @return the number of occurrences of the given value inside [left, right)
     * Time complexity: O(1)
     * Space complexity: O(1) auxiliary
     */
    public int countInSegment(int[][] prefix, int valueIndex, int left, int right) {
        return prefix[valueIndex][right] - prefix[valueIndex][left];
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement and a few
     * additional sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * 2^k + n * k) per demonstrated test case
     * Space complexity: O(k * 2^k + n * k) per demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] coupons1 = {3, 1, 3, 2, 1};
        int result1 = solution.minimumReorders(coupons1);
        System.out.println("Input: " + Arrays.toString(coupons1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        int[] coupons2 = {4, 4, 2, 2, 3};
        int result2 = solution.minimumReorders(coupons2);
        System.out.println("Input: " + Arrays.toString(coupons2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional checks:
        int[] coupons3 = {1};
        System.out.println("Input: " + Arrays.toString(coupons3));
        System.out.println("Output: " + solution.minimumReorders(coupons3));
        System.out.println("Expected: 0");
        System.out.println();

        int[] coupons4 = {1, 2, 1, 2};
        System.out.println("Input: " + Arrays.toString(coupons4));
        System.out.println("Output: " + solution.minimumReorders(coupons4));
        System.out.println("Expected: 1");
        System.out.println();

        int[] coupons5 = {1, 2, 3, 1, 2, 3};
        System.out.println("Input: " + Arrays.toString(coupons5));
        System.out.println("Output: " + solution.minimumReorders(coupons5));
        System.out.println("Expected: 3");
    }
}