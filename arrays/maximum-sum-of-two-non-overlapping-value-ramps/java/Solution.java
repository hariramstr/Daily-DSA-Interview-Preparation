import java.util.*;

/*
 * Title: Maximum Sum of Two Non-Overlapping Value Ramps
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums of length n. A value ramp is a pair of indices (i, j)
 * such that i < j and nums[i] < nums[j]. The score of that ramp is defined as nums[j] - nums[i].
 * You must choose exactly two value ramps, (i1, j1) and (i2, j2), such that their index intervals
 * do not overlap. In other words, either j1 < i2 or j2 < i1. Your task is to return the maximum
 * possible total score of the two ramps. If it is impossible to choose two non-overlapping valid
 * ramps, return -1.
 *
 * This problem is not asking for the widest ramp or the single best ramp. The challenge is to
 * optimize the sum of two disjoint increasing pairs, where each pair may use any two indices as
 * long as the left value is strictly smaller than the right value. The two ramps may appear in
 * either order in the array, but their covered index ranges cannot share any index.
 *
 * Constraints:
 * - 2 <= n <= 2 * 10^5
 * - -10^9 <= nums[i] <= 10^9
 * - Indices are 0-based
 * - A ramp requires strict inequality: nums[left] < nums[right]
 *
 * Example 1:
 * Input: nums = [4, 1, 7, 2, 9, 3, 8]
 * Output: 13
 * Explanation:
 * Choose ramps (1, 2) with score 7 - 1 = 6 and (3, 4) with score 9 - 2 = 7.
 * These intervals [1,2] and [3,4] do not overlap, so the total is 13.
 *
 * Example 2:
 * Input: nums = [9, 8, 7, 6, 5, 10]
 * Output: -1
 * Explanation:
 * Although there are valid ramps ending at index 5, they all occupy the same final region,
 * so it is impossible to choose two non-overlapping valid ramps.
 *
 * Core idea of this solution:
 * 1) For every prefix [0..r], compute the best single ramp fully contained in that prefix.
 * 2) For every suffix [l..n-1], compute the best single ramp fully contained in that suffix.
 * 3) Try every split point s between indices s and s+1:
 *      - first ramp must lie completely in [0..s]
 *      - second ramp must lie completely in [s+1..n-1]
 *    The best answer is the maximum sum of those two values.
 *
 * The difficult part is computing the best single ramp for every prefix/suffix efficiently.
 *
 * Prefix computation:
 * - Sweep from left to right.
 * - Maintain all previous values in a Fenwick tree after coordinate compression.
 * - For current nums[r], the best ramp ending at r is:
 *       nums[r] - minimum previous value strictly smaller than nums[r]
 * - Then prefixBest[r] = max(prefixBest[r-1], bestEndingAtR)
 *
 * Suffix computation:
 * - Sweep from right to left.
 * - Maintain all future values in another Fenwick tree that can answer:
 *       maximum future value strictly greater than nums[l]
 * - Then the best ramp starting at l is:
 *       maximum future value strictly greater than nums[l] - nums[l]
 * - suffixBest[l] = max(suffixBest[l+1], bestStartingAtL)
 *
 * Both Fenwick trees work on compressed values, giving O(n log n) time.
 */
public class Solution {

    /**
     * A very negative sentinel used to represent "no valid ramp exists".
     */
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    /**
     * Solves the problem: returns the maximum total score of exactly two non-overlapping value ramps.
     *
     * Algorithm overview:
     * 1. Coordinate-compress the values because nums[i] can be as large as 1e9 in magnitude.
     * 2. Build prefixBest:
     *    - prefixBest[r] = best score of one valid ramp fully inside nums[0..r]
     * 3. Build suffixBest:
     *    - suffixBest[l] = best score of one valid ramp fully inside nums[l..n-1]
     * 4. Try every split s where left side is [0..s] and right side is [s+1..n-1].
     *    If both sides contain at least one valid ramp, combine them.
     *
     * Why this is correct:
     * - Any two non-overlapping ramps must have one entirely before the other.
     * - Therefore there exists a split point between them.
     * - For that split, the optimal left ramp is exactly captured by prefixBest,
     *   and the optimal right ramp is exactly captured by suffixBest.
     * - Taking the maximum over all splits yields the global optimum.
     *
     * @param nums the input array
     * @return the maximum possible total score of two non-overlapping valid ramps, or -1 if impossible
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maxSumOfTwoNonOverlappingValueRamps(int[] nums) {
        int n = nums.length;
        if (n < 4) {
            // With fewer than 4 indices, it is impossible to form two disjoint pairs.
            return -1;
        }

        int[] sortedUnique = compressValues(nums);

        long[] prefixBest = buildPrefixBest(nums, sortedUnique);
        long[] suffixBest = buildSuffixBest(nums, sortedUnique);

        long answer = NEG_INF;

        // Try every split:
        // left ramp must be fully inside [0..split]
        // right ramp must be fully inside [split+1..n-1]
        for (int split = 0; split < n - 1; split++) {
            if (prefixBest[split] != NEG_INF && suffixBest[split + 1] != NEG_INF) {
                answer = Math.max(answer, prefixBest[split] + suffixBest[split + 1]);
            }
        }

        return answer == NEG_INF ? -1 : answer;
    }

    /**
     * Builds prefixBest where prefixBest[r] is the best score of a single valid ramp
     * fully contained in nums[0..r].
     *
     * Detailed logic:
     * - We process indices from left to right.
     * - At position r, we want the best ramp ending at r.
     * - That means we need the minimum value among all previous nums[i] with nums[i] < nums[r].
     * - If that minimum exists, the best score ending at r is nums[r] - minPreviousSmaller.
     * - Then prefixBest[r] is the maximum of:
     *      a) prefixBest[r - 1]  -> best ramp seen earlier
     *      b) best score ending exactly at r
     *
     * Data structure:
     * - Fenwick tree storing prefix minimums over compressed values.
     * - Query all compressed ranks strictly smaller than current rank.
     *
     * @param nums the input array
     * @param sortedUnique sorted unique values used for coordinate compression
     * @return array prefixBest
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixBest(int[] nums, int[] sortedUnique) {
        int n = nums.length;
        long[] prefixBest = new long[n];
        Arrays.fill(prefixBest, NEG_INF);

        FenwickMin fenwickMin = new FenwickMin(sortedUnique.length);

        for (int r = 0; r < n; r++) {
            int rank = lowerBound(sortedUnique, nums[r]) + 1; // 1-based rank

            // Query the minimum previous value among all values strictly smaller than nums[r].
            // Since compressed ranks are sorted by value, "strictly smaller" means ranks [1 .. rank-1].
            long minSmaller = fenwickMin.query(rank - 1);

            long bestEndingHere = NEG_INF;
            if (minSmaller != FenwickMin.INF) {
                bestEndingHere = (long) nums[r] - minSmaller;
            }

            if (r == 0) {
                prefixBest[r] = bestEndingHere;
            } else {
                prefixBest[r] = Math.max(prefixBest[r - 1], bestEndingHere);
            }

            // After processing ramps ending at r, insert nums[r] as a candidate left endpoint
            // for future positions.
            fenwickMin.update(rank, nums[r]);
        }

        return prefixBest;
    }

    /**
     * Builds suffixBest where suffixBest[l] is the best score of a single valid ramp
     * fully contained in nums[l..n-1].
     *
     * Detailed logic:
     * - We process indices from right to left.
     * - At position l, we want the best ramp starting at l.
     * - That means we need the maximum value among all future nums[j] with nums[j] > nums[l].
     * - If that maximum exists, the best score starting at l is maxFutureGreater - nums[l].
     * - Then suffixBest[l] is the maximum of:
     *      a) suffixBest[l + 1] -> best ramp seen later
     *      b) best score starting exactly at l
     *
     * Data structure:
     * - Fenwick tree storing suffix maximums over compressed values.
     * - To query values strictly greater than current value efficiently with a Fenwick tree,
     *   we reverse the rank order:
     *      reversedRank = m - rank + 1
     * - Then values greater than current correspond to reversed ranks smaller than current reversed rank.
     *
     * @param nums the input array
     * @param sortedUnique sorted unique values used for coordinate compression
     * @return array suffixBest
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long[] buildSuffixBest(int[] nums, int[] sortedUnique) {
        int n = nums.length;
        int m = sortedUnique.length;
        long[] suffixBest = new long[n];
        Arrays.fill(suffixBest, NEG_INF);

        FenwickMax fenwickMax = new FenwickMax(m);

        for (int l = n - 1; l >= 0; l--) {
            int rank = lowerBound(sortedUnique, nums[l]) + 1; // 1-based rank
            int reversedRank = m - rank + 1;

            // Values strictly greater than nums[l] correspond to original ranks (rank+1 .. m),
            // which become reversed ranks (1 .. reversedRank-1).
            long maxGreater = fenwickMax.query(reversedRank - 1);

            long bestStartingHere = NEG_INF;
            if (maxGreater != FenwickMax.NEG_INF) {
                bestStartingHere = maxGreater - nums[l];
            }

            if (l == n - 1) {
                suffixBest[l] = bestStartingHere;
            } else {
                suffixBest[l] = Math.max(suffixBest[l + 1], bestStartingHere);
            }

            // Insert nums[l] as a future right endpoint candidate for earlier positions.
            fenwickMax.update(reversedRank, nums[l]);
        }

        return suffixBest;
    }

    /**
     * Coordinate-compresses the values of nums into a sorted unique array.
     *
     * Example:
     * nums = [4, 1, 7, 2, 9, 3, 8]
     * sortedUnique = [1, 2, 3, 4, 7, 8, 9]
     *
     * We do not directly return ranks here because both prefix and suffix builders
     * can compute ranks using binary search on this sorted unique array.
     *
     * @param nums the input array
     * @return sorted array of unique values
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int[] compressValues(int[] nums) {
        int[] copy = nums.clone();
        Arrays.sort(copy);

        int uniqueCount = 0;
        for (int value : copy) {
            if (uniqueCount == 0 || copy[uniqueCount - 1] != value) {
                copy[uniqueCount++] = value;
            }
        }

        return Arrays.copyOf(copy, uniqueCount);
    }

    /**
     * Standard lower bound:
     * returns the first index i such that arr[i] >= target.
     *
     * Since target is guaranteed to exist in the compressed array when used here,
     * this effectively returns the exact compressed position of target.
     *
     * @param arr sorted array
     * @param target value to search
     * @return first index with arr[index] >= target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] arr, int target) {
        int left = 0;
        int right = arr.length;

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return left;
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(k * n log n) for k demo test cases
     * Space complexity: O(n) per test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {4, 1, 7, 2, 9, 3, 8};
        long result1 = solution.maxSumOfTwoNonOverlappingValueRamps(nums1);
        System.out.println("Input: " + Arrays.toString(nums1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 13");
        System.out.println();

        int[] nums2 = {9, 8, 7, 6, 5, 10};
        long result2 = solution.maxSumOfTwoNonOverlappingValueRamps(nums2);
        System.out.println("Input: " + Arrays.toString(nums2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: -1");
        System.out.println();

        int[] nums3 = {1, 5, 2, 6};
        long result3 = solution.maxSumOfTwoNonOverlappingValueRamps(nums3);
        System.out.println("Input: " + Arrays.toString(nums3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 8");
        System.out.println();

        int[] nums4 = {5, 1, 4, 2, 7};
        long result4 = solution.maxSumOfTwoNonOverlappingValueRamps(nums4);
        System.out.println("Input: " + Arrays.toString(nums4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 8");
    }

    /**
     * Fenwick tree (Binary Indexed Tree) for prefix minimum queries.
     *
     * We use it like this:
     * - update(position, value): store the minimum value seen at that compressed rank
     * - query(position): minimum over ranks [1..position]
     */
    static class FenwickMin {
        static final long INF = Long.MAX_VALUE / 4;
        private final long[] tree;

        /**
         * Creates a Fenwick tree for minimum queries.
         *
         * @param size number of compressed ranks
         */
        FenwickMin(int size) {
            tree = new long[size + 2];
            Arrays.fill(tree, INF);
        }

        /**
         * Updates one position with a candidate value, keeping the minimum.
         *
         * @param index 1-based index
         * @param value candidate value
         */
        void update(int index, long value) {
            int i = index;
            while (i < tree.length) {
                tree[i] = Math.min(tree[i], value);
                i += i & -i;
            }
        }

        /**
         * Returns the minimum value in the prefix [1..index].
         *
         * @param index 1-based index
         * @return minimum value in prefix, or INF if none exists
         */
        long query(int index) {
            long result = INF;
            int i = index;
            while (i > 0) {
                result = Math.min(result, tree[i]);
                i -= i & -i;
            }
            return result;
        }
    }

    /**
     * Fenwick tree (Binary Indexed Tree) for prefix maximum queries.
     *
     * We use it on reversed ranks so that querying "strictly greater values"
     * becomes a normal prefix query.
     */
    static class FenwickMax {
        static final long NEG_INF = Long.MIN_VALUE / 4;
        private final long[] tree;

        /**
         * Creates a Fenwick tree for maximum queries.
         *
         * @param size number of compressed ranks
         */
        FenwickMax(int size) {
            tree = new long[size + 2];
            Arrays.fill(tree, NEG_INF);
        }

        /**
         * Updates one position with a candidate value, keeping the maximum.
         *
         * @param index 1-based index
         * @param value candidate value
         */
        void update(int index, long value) {
            int i = index;
            while (i < tree.length) {
                tree[i] = Math.max(tree[i], value);
                i += i & -i;
            }
        }

        /**
         * Returns the maximum value in the prefix [1..index].
         *
         * @param index 1-based index
         * @return maximum value in prefix, or NEG_INF if none exists
         */
        long query(int index) {
            long result = NEG_INF;
            int i = index;
            while (i > 0) {
                result = Math.max(result, tree[i]);
                i -= i & -i;
            }
            return result;
        }
    }
}