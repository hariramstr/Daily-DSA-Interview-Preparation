import java.util.*;

/*
 * Title: Minimum Merges to Form a Mountain Array
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums representing daily measurements. In one operation,
 * you may merge any two adjacent elements into a single element whose value is their sum.
 * After a merge, the array becomes shorter by one, and the relative order of all remaining
 * elements stays the same.
 *
 * Your goal is to transform the array into a mountain array using the minimum number of
 * merge operations.
 *
 * An array is considered a mountain array if there exists an index p such that:
 * - 0 < p < length - 1
 * - values strictly increase from index 0 to p
 * - values strictly decrease from index p to length - 1
 *
 * In other words, the final array must have at least 3 elements and exactly one peak,
 * with no equal adjacent values in either slope.
 *
 * Return the minimum number of adjacent merges needed to make nums a mountain array.
 * If it is impossible, return -1.
 *
 * A merge can combine already-merged segments again later, so each final element corresponds
 * to the sum of some contiguous block of the original array.
 *
 * Constraints:
 * - 3 <= nums.length <= 200
 * - 1 <= nums[i] <= 10^6
 *
 * Example 1:
 * Input: nums = [1, 2, 1]
 * Output: 0
 * Explanation: The array is already a mountain with peak at index 1.
 *
 * Example 2:
 * Input: nums = [2, 1, 1, 2]
 * Output: -1
 * Explanation: No sequence of adjacent merges can produce a valid mountain array.
 */

public class Solution {

    /**
     * Computes the minimum number of adjacent merges required to transform the input array
     * into a mountain array.
     *
     * Core idea:
     * Every final element after all merges is the sum of one contiguous block of the original array.
     * Therefore, the problem is equivalent to:
     * "Partition the array into the maximum possible number of contiguous blocks such that
     *  the sequence of block sums forms a mountain."
     *
     * If we can keep k final blocks, then the number of merges is:
     * n - k
     * because each merge reduces the array length by exactly 1.
     *
     * So we maximize the number of blocks in a valid mountain partition.
     *
     * Dynamic programming strategy:
     * 1. Precompute prefix sums for O(1) range-sum queries.
     * 2. Build:
     *    - inc[i][j] = maximum number of blocks in nums[i..j] whose block sums are strictly increasing.
     *    - dec[i][j] = maximum number of blocks in nums[i..j] whose block sums are strictly decreasing.
     * 3. Try every possible peak block [l..r].
     *    - Left side nums[0..l-1] must be partitionable into a strictly increasing sequence
     *      whose last block sum is < peakSum.
     *    - Right side nums[r+1..n-1] must be partitionable into a strictly decreasing sequence
     *      whose first block sum is < peakSum.
     * 4. Maximize total number of blocks = leftBlocks + 1 + rightBlocks.
     *
     * Because n <= 200, an O(n^4) dynamic programming solution is acceptable.
     *
     * @param nums the original array of positive integers
     * @return the minimum number of merges needed to form a mountain array, or -1 if impossible
     * Time complexity: O(n^4)
     * Space complexity: O(n^2)
     */
    public int minimumMountainMerges(int[] nums) {
        int n = nums.length;

        // A mountain must have at least 3 final elements.
        if (n < 3) {
            return -1;
        }

        // Prefix sums allow us to compute any contiguous block sum in O(1).
        long[] prefix = buildPrefixSums(nums);

        // inc[i][j]:
        // maximum number of blocks that partition subarray nums[i..j]
        // such that the sequence of block sums is strictly increasing.
        //
        // If impossible, value stays -1.
        int[][] inc = new int[n][n];

        // dec[i][j]:
        // maximum number of blocks that partition subarray nums[i..j]
        // such that the sequence of block sums is strictly decreasing.
        //
        // If impossible, value stays -1.
        int[][] dec = new int[n][n];

        for (int i = 0; i < n; i++) {
            Arrays.fill(inc[i], -1);
            Arrays.fill(dec[i], -1);
        }

        // Base case:
        // Any single element subarray can always be viewed as one block.
        // A single block is trivially both increasing and decreasing as a sequence of length 1.
        for (int i = 0; i < n; i++) {
            inc[i][i] = 1;
            dec[i][i] = 1;
        }

        // Build DP by increasing subarray length.
        for (int len = 2; len <= n; len++) {
            for (int start = 0; start + len - 1 < n; start++) {
                int end = start + len - 1;

                // -----------------------------
                // Compute inc[start][end]
                // -----------------------------
                //
                // We try every possible last cut position "mid".
                // Then:
                // - left part nums[start..mid]
                // - last block nums[mid+1..end]
                //
                // If left part can be partitioned into a strictly increasing sequence,
                // and the sum of its last block is strictly less than the sum of the final block,
                // then we can extend that sequence by one more block.
                //
                // To know whether the last block sum of the left partition is < current last block sum,
                // we must try every possible start position of the left partition's last block.
                //
                // More directly:
                // Let the final two pieces be:
                //   nums[start..k] partitioned increasingly
                //   nums[k+1..end] as the last block
                //
                // But inc[start][k] alone does not tell us the last block sum.
                // So we reconstruct by trying the previous cut t:
                //   nums[start..t] increasing
                //   nums[t+1..k] previous last block
                //   nums[k+1..end] current last block
                //
                // However, to keep the implementation simpler and still within O(n^4),
                // we instead define the transition by trying the first block.
                //
                // For increasing:
                // nums[start..end] can be:
                //   first block = nums[start..cut]
                //   remaining increasing partition on nums[cut+1..end]
                // with firstBlockSum < first block sum of the remaining partition.
                //
                // But again, remaining partition does not store its first block sum.
                //
                // Therefore, we use a more explicit DP helper approach below:
                // for each subarray, we compute the best partition by trying all first cuts
                // and recursively matching block sums through auxiliary scans.
                //
                // Since n is small, we can do this with direct enumeration.

                inc[start][end] = computeBestIncreasingBlocks(start, end, prefix, inc);

                // -----------------------------
                // Compute dec[start][end]
                // -----------------------------
                //
                // Symmetric logic for strictly decreasing block sums.
                dec[start][end] = computeBestDecreasingBlocks(start, end, prefix, dec);
            }
        }

        int bestBlocks = -1;

        // Try every possible peak block [l..r].
        // The final mountain will look like:
        //   increasing blocks on the left
        //   one peak block [l..r]
        //   decreasing blocks on the right
        //
        // We need at least one block on each side.
        for (int l = 1; l < n - 1; l++) {
            for (int r = l; r < n - 1; r++) {
                long peakSum = rangeSum(prefix, l, r);

                // Find the best increasing partition of nums[0..l-1]
                // whose LAST block sum is strictly less than peakSum.
                int leftBest = bestIncreasingEndingBelow(0, l - 1, peakSum, prefix);

                if (leftBest == -1) {
                    continue;
                }

                // Find the best decreasing partition of nums[r+1..n-1]
                // whose FIRST block sum is strictly less than peakSum.
                int rightBest = bestDecreasingStartingBelow(r + 1, n - 1, peakSum, prefix);

                if (rightBest == -1) {
                    continue;
                }

                // Total blocks = left side blocks + peak block + right side blocks.
                int totalBlocks = leftBest + 1 + rightBest;

                // A mountain must have at least 3 blocks total.
                if (totalBlocks >= 3) {
                    bestBlocks = Math.max(bestBlocks, totalBlocks);
                }
            }
        }

        if (bestBlocks == -1) {
            return -1;
        }

        return n - bestBlocks;
    }

    /**
     * Builds prefix sums for the given array.
     *
     * prefix[i + 1] = sum of nums[0..i]
     *
     * @param nums the input array
     * @return prefix sum array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] nums) {
        long[] prefix = new long[nums.length + 1];
        for (int i = 0; i < nums.length; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        return prefix;
    }

    /**
     * Returns the sum of nums[left..right] using prefix sums.
     *
     * @param prefix prefix sum array
     * @param left left index, inclusive
     * @param right right index, inclusive
     * @return sum of the subarray
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long rangeSum(long[] prefix, int left, int right) {
        return prefix[right + 1] - prefix[left];
    }

    /**
     * Computes the maximum number of blocks in nums[start..end] whose block sums are strictly increasing.
     *
     * This helper uses memoized DFS over:
     * - current position
     * - previous block sum
     *
     * However, because previous block sums can vary widely, we do not memoize by sum directly here.
     * Instead, for the small constraint n <= 200, we enumerate all possible first blocks and recurse.
     *
     * This method is used only while building a coarse DP table and is intentionally simple.
     *
     * @param start start index of the subarray
     * @param end end index of the subarray
     * @param prefix prefix sums
     * @param inc unused coarse table parameter retained for clarity of structure
     * @return maximum number of increasing blocks, or -1 if impossible
     * Time complexity: O(n^2) per call in the worst case
     * Space complexity: O(n^2) due to recursion support structures
     */
    public int computeBestIncreasingBlocks(int start, int end, long[] prefix, int[][] inc) {
        Map<Long, Integer>[] memo = createMemoArray(end - start + 2);
        return dfsIncreasing(start, end, -1L, prefix, memo);
    }

    /**
     * Computes the maximum number of blocks in nums[start..end] whose block sums are strictly decreasing.
     *
     * @param start start index of the subarray
     * @param end end index of the subarray
     * @param prefix prefix sums
     * @param dec unused coarse table parameter retained for clarity of structure
     * @return maximum number of decreasing blocks, or -1 if impossible
     * Time complexity: O(n^2) per call in the worst case
     * Space complexity: O(n^2) due to recursion support structures
     */
    public int computeBestDecreasingBlocks(int start, int end, long[] prefix, int[][] dec) {
        Map<Long, Integer>[] memo = createMemoArray(end - start + 2);
        return dfsDecreasing(start, end, Long.MAX_VALUE, prefix, memo);
    }

    /**
     * Finds the maximum number of blocks in nums[start..end] forming a strictly increasing sequence
     * such that the LAST block sum is strictly less than limit.
     *
     * We enumerate the last block boundary:
     * - previous part nums[start..cut-1] must be increasing
     * - last block nums[cut..end] must have sum < limit
     * - and the previous part's last block sum must be < last block sum
     *
     * To keep the implementation beginner-friendly and correct, we solve this by explicit DFS
     * over possible block boundaries.
     *
     * @param start start index
     * @param end end index
     * @param limit strict upper bound for the last block sum
     * @param prefix prefix sums
     * @return maximum number of blocks, or -1 if impossible
     * Time complexity: O(n^2) to O(n^3) depending on branching
     * Space complexity: O(n^2)
     */
    public int bestIncreasingEndingBelow(int start, int end, long limit, long[] prefix) {
        Map<String, Integer> memo = new HashMap<>();
        return dfsIncreasingEndBound(start, end, limit, prefix, memo);
    }

    /**
     * Finds the maximum number of blocks in nums[start..end] forming a strictly decreasing sequence
     * such that the FIRST block sum is strictly less than limit.
     *
     * @param start start index
     * @param end end index
     * @param limit strict upper bound for the first block sum
     * @param prefix prefix sums
     * @return maximum number of blocks, or -1 if impossible
     * Time complexity: O(n^2) to O(n^3) depending on branching
     * Space complexity: O(n^2)
     */
    public int bestDecreasingStartingBelow(int start, int end, long limit, long[] prefix) {
        Map<String, Integer> memo = new HashMap<>();
        return dfsDecreasingStartBound(start, end, limit, prefix, memo);
    }

    /**
     * Depth-first search for maximum number of increasing blocks in nums[pos..end],
     * where every chosen block sum must be strictly greater than prevSum.
     *
     * Special convention:
     * - prevSum == -1 means "no previous block yet", so any positive block sum is allowed.
     *
     * @param pos current start position
     * @param end end index of the active subarray
     * @param prevSum previous block sum
     * @param prefix prefix sums
     * @param memo memoization by position and prevSum
     * @return maximum number of blocks from pos to end, or -1 if impossible
     * Time complexity: exponential without memoization; practical with memoization for n <= 200
     * Space complexity: O(number of memo states)
     */
    public int dfsIncreasing(int pos, int end, long prevSum, long[] prefix, Map<Long, Integer>[] memo) {
        if (pos > end) {
            return 0;
        }

        int memoIndex = pos;
        Integer cached = memo[memoIndex].get(prevSum);
        if (cached != null) {
            return cached;
        }

        int best = -1;

        for (int cut = pos; cut <= end; cut++) {
            long currentSum = rangeSum(prefix, pos, cut);

            if (prevSum == -1L || currentSum > prevSum) {
                int next = dfsIncreasing(cut + 1, end, currentSum, prefix, memo);
                if (next != -1) {
                    best = Math.max(best, 1 + next);
                }
            }
        }

        memo[memoIndex].put(prevSum, best);
        return best;
    }

    /**
     * Depth-first search for maximum number of decreasing blocks in nums[pos..end],
     * where every chosen block sum must be strictly less than prevSum.
     *
     * @param pos current start position
     * @param end end index of the active subarray
     * @param prevSum previous block sum upper bound
     * @param prefix prefix sums
     * @param memo memoization by position and prevSum
     * @return maximum number of blocks from pos to end, or -1 if impossible
     * Time complexity: exponential without memoization; practical with memoization for n <= 200
     * Space complexity: O(number of memo states)
     */
    public int dfsDecreasing(int pos, int end, long prevSum, long[] prefix, Map<Long, Integer>[] memo) {
        if (pos > end) {
            return 0;
        }

        int memoIndex = pos;
        Integer cached = memo[memoIndex].get(prevSum);
        if (cached != null) {
            return cached;
        }

        int best = -1;

        for (int cut = pos; cut <= end; cut++) {
            long currentSum = rangeSum(prefix, pos, cut);

            if (currentSum < prevSum) {
                int next = dfsDecreasing(cut + 1, end, currentSum, prefix, memo);
                if (next != -1) {
                    best = Math.max(best, 1 + next);
                }
            }
        }

        memo[memoIndex].put(prevSum, best);
        return best;
    }

    /**
     * DFS for an increasing partition of nums[start..end] whose final block sum is < limit.
     *
     * We build the sequence from left to right while carrying the previous block sum.
     * The final chosen partition is valid only if the last block sum is < limit.
     *
     * @param start start index
     * @param end end index
     * @param limit strict upper bound for the last block sum
     * @param prefix prefix sums
     * @param memo memoization map
     * @return maximum number of blocks, or -1 if impossible
     * Time complexity: O(n^3) in the worst case
     * Space complexity: O(number of memo states)
     */
    public int dfsIncreasingEndBound(int start, int end, long limit, long[] prefix, Map<String, Integer> memo) {
        return dfsIncreasingEndBoundHelper(start, end, -1L, limit, prefix, memo);
    }

    /**
     * Helper for increasing partition with a bound on the final block.
     *
     * @param pos current position
     * @param end end