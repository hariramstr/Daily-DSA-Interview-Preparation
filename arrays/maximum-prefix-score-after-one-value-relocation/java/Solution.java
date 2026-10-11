import java.util.*;

/*
Title: Maximum Prefix Score After One Value Relocation

Problem Description:
You are given an integer array nums of length n. Define the prefix score of the array as the number of indices i
(0 <= i < n) such that the sum of the first i + 1 elements is strictly greater than 0.
In other words, if prefix[i] = nums[0] + nums[1] + ... + nums[i], then index i contributes to the score if prefix[i] > 0.

You may perform at most one relocation operation. In a relocation, choose one element nums[j], remove it from its
current position, and insert it at any other position in the array (including the beginning or the end).
The relative order of all other elements must stay the same.

Return the maximum possible prefix score after applying at most one relocation.

This problem asks you to reason about how moving a single value changes many prefix sums at once.
A brute-force solution that tries every move and recomputes all prefix sums will be too slow.

Constraints:
- 1 <= n <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- The answer fits in a 32-bit signed integer.

Example 1:
Input: nums = [4, -5, 2, 1]
Output: 4
Explanation: Move -5 from index 1 to the end, producing [4, 2, 1, -5].
The prefix sums become [4, 6, 7, 2], all of which are positive, so the score is 4.
Without any move, the prefix sums are [4, -1, 1, 2], so the score is only 3.

Example 2:
Input: nums = [-2, 3, -1, 2]
Output: 3
Explanation: Move -2 from index 0 to index 2, producing [3, -1, -2, 2].
The prefix sums become [3, 2, 0, 2]. Positive prefixes occur at indices 0, 1, and 3, so the score is 3.
No relocation can make all 4 prefix sums strictly positive.
*/

public class Solution {

    /**
     * Computes the maximum possible prefix score after performing at most one relocation.
     *
     * Core idea:
     * We analyze two useful move families that are sufficient for the optimum:
     *
     * 1) Move one negative value nums[j] to a later position.
     *    - For every prefix ending before j, nothing changes.
     *    - For prefixes from j to the insertion point - 1, we REMOVE nums[j] from those prefixes.
     *      Since nums[j] is negative, removing it increases those prefix sums by -nums[j].
     *    - After the insertion point, total prefix sums become the same as before.
     *    Therefore, this move only helps on one contiguous suffix-like interval [j, k-1].
     *    Best choice for a fixed j is to extend that interval as far right as possible, i.e. move j to the end.
     *
     * 2) Move one positive value nums[j] to an earlier position.
     *    - For prefixes before the new position, nothing changes.
     *    - For prefixes from the new position to j-1, we ADD nums[j] to those prefixes.
     *      Since nums[j] is positive, those prefix sums increase.
     *    - After j, total prefix sums become the same as before.
     *    Therefore, this move only helps on one interval [k, j-1].
     *    Best choice for a fixed j is to start that interval as far left as possible, i.e. move j to the front.
     *
     * So the answer is the maximum among:
     * - no move
     * - move one negative element to the end
     * - move one positive element to the front
     *
     * We can evaluate these efficiently using:
     * - prefix sums
     * - counts of positive prefix sums
     * - offline queries of the form:
     *     count of prefix sums in a range that are > threshold
     *
     * @param nums the input array
     * @return the maximum achievable prefix score
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int maxPrefixScoreAfterOneRelocation(int[] nums) {
        int n = nums.length;

        // Prefix sums stored in long to safely handle sums up to n * 1e9.
        long[] prefix = buildPrefixSums(nums);

        // positivePrefixCount[i] = number of indices t in [0..i] such that prefix[t] > 0.
        int[] positivePrefixCount = buildPositivePrefixCount(prefix);

        // Base answer: do nothing.
        int answer = positivePrefixCount[n - 1];

        // Evaluate all moves of a negative element to the end.
        answer = Math.max(answer, bestByMovingNegativeToEnd(nums, prefix, positivePrefixCount));

        // Evaluate all moves of a positive element to the front.
        answer = Math.max(answer, bestByMovingPositiveToFront(nums, prefix, positivePrefixCount));

        return answer;
    }

    /**
     * Builds prefix sums.
     *
     * @param nums the input array
     * @return prefix sums where prefix[i] = nums[0] + ... + nums[i]
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] nums) {
        int n = nums.length;
        long[] prefix = new long[n];
        long sum = 0L;

        for (int i = 0; i < n; i++) {
            sum += nums[i];
            prefix[i] = sum;
        }

        return prefix;
    }

    /**
     * Builds cumulative counts of positive prefix sums.
     *
     * @param prefix the prefix sums array
     * @return an array count where count[i] is the number of positive prefix sums in prefix[0..i]
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildPositivePrefixCount(long[] prefix) {
        int n = prefix.length;
        int[] count = new int[n];
        int running = 0;

        for (int i = 0; i < n; i++) {
            if (prefix[i] > 0) {
                running++;
            }
            count[i] = running;
        }

        return count;
    }

    /**
     * Computes the best score obtainable by moving exactly one negative element to the end.
     *
     * Detailed effect for moving nums[j] (where nums[j] < 0) to the end:
     * - Prefix indices [0 .. j-1] are unchanged.
     * - Prefix indices [j .. n-2] increase by -nums[j].
     * - Prefix index n-1 (the full-array sum) is unchanged.
     *
     * So the new score is:
     *   positives in [0 .. j-1] using original threshold > 0
     * + positives in [j .. n-2] after adding -nums[j], i.e. original prefix[i] > nums[j]
     * + positivity of the final total sum (same as original prefix[n-1] > 0)
     *
     * We answer the middle part using an offline Fenwick-tree query:
     * count values prefix[i] in range [j .. n-2] that are > nums[j].
     *
     * @param nums the input array
     * @param prefix prefix sums
     * @param positivePrefixCount cumulative counts of positive prefix sums
     * @return best score among all such moves
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int bestByMovingNegativeToEnd(int[] nums, long[] prefix, int[] positivePrefixCount) {
        int n = nums.length;
        if (n == 1) {
            return positivePrefixCount[0];
        }

        // We need to query over prefix indices [j .. n-2].
        // If j == n-1, moving last element to end changes nothing, so it can be ignored.
        List<ValueIndex> values = new ArrayList<>();
        for (int i = 0; i <= n - 2; i++) {
            values.add(new ValueIndex(prefix[i], i));
        }

        // Sort prefix values descending so we can activate all values > threshold.
        values.sort((a, b) -> Long.compare(b.value, a.value));

        List<ThresholdQuery> queries = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            if (nums[j] < 0 && j <= n - 2) {
                queries.add(new ThresholdQuery(nums[j], j, j, n - 2));
            }
        }

        // Sort queries by threshold descending.
        queries.sort((a, b) -> Long.compare(b.threshold, a.threshold));

        Fenwick fenwick = new Fenwick(n);
        int ptr = 0;
        int best = positivePrefixCount[n - 1];

        for (ThresholdQuery q : queries) {
            // Activate all prefix values strictly greater than q.threshold.
            while (ptr < values.size() && values.get(ptr).value > q.threshold) {
                fenwick.add(values.get(ptr).index + 1, 1);
                ptr++;
            }

            int leftUnchanged = q.left == 0 ? 0 : positivePrefixCount[q.left - 1];
            int improvedMiddle = fenwick.rangeSum(q.left + 1, q.right + 1);
            int finalPrefixPositive = prefix[n - 1] > 0 ? 1 : 0;

            int candidate = leftUnchanged + improvedMiddle + finalPrefixPositive;
            if (candidate > best) {
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Computes the best score obtainable by moving exactly one positive element to the front.
     *
     * Detailed effect for moving nums[j] (where nums[j] > 0) to the front:
     * - New prefix at index 0 becomes nums[j].
     * - For new indices 1 .. j, the corresponding original prefixes are prefix[t-1] + nums[j].
     *   Equivalently, original indices i in [0 .. j-1] become positive if prefix[i] > -nums[j].
     * - Prefix indices corresponding to original [j .. n-1] are unchanged.
     *
     * So the new score is:
     *   count of original prefix[i] in [0 .. j-1] with prefix[i] > -nums[j]
     * + count of original positive prefixes in [j .. n-1]
     *
     * We answer the first part using an offline Fenwick-tree query:
     * count values prefix[i] in range [0 .. j-1] that are > -nums[j].
     *
     * @param nums the input array
     * @param prefix prefix sums
     * @param positivePrefixCount cumulative counts of positive prefix sums
     * @return best score among all such moves
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int bestByMovingPositiveToFront(int[] nums, long[] prefix, int[] positivePrefixCount) {
        int n = nums.length;

        List<ValueIndex> values = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            values.add(new ValueIndex(prefix[i], i));
        }

        // Sort prefix values descending so we can activate all values > threshold.
        values.sort((a, b) -> Long.compare(b.value, a.value));

        List<ThresholdQuery> queries = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            if (nums[j] > 0 && j >= 1) {
                queries.add(new ThresholdQuery(-1L * nums[j], j, 0, j - 1));
            }
        }

        queries.sort((a, b) -> Long.compare(b.threshold, a.threshold));

        Fenwick fenwick = new Fenwick(n);
        int ptr = 0;
        int best = positivePrefixCount[n - 1];

        for (ThresholdQuery q : queries) {
            // Activate all prefix values strictly greater than q.threshold.
            while (ptr < values.size() && values.get(ptr).value > q.threshold) {
                fenwick.add(values.get(ptr).index + 1, 1);
                ptr++;
            }

            int improvedLeft = fenwick.rangeSum(q.left + 1, q.right + 1);
            int unchangedRight = positivePrefixCount[n - 1] - positivePrefixCount[q.right];

            int candidate = improvedLeft + unchangedRight;
            if (candidate > best) {
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement and a few extra cases.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(k * n log n) for k demonstrations
     * Space complexity: O(n) per demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {4, -5, 2, 1};
        int[] nums2 = {-2, 3, -1, 2};
        int[] nums3 = {1};
        int[] nums4 = {-5, -1, -2};
        int[] nums5 = {2, -1, -1, 2};

        System.out.println(solution.maxPrefixScoreAfterOneRelocation(nums1)); // Expected: 4
        System.out.println(solution.maxPrefixScoreAfterOneRelocation(nums2)); // Expected: 3
        System.out.println(solution.maxPrefixScoreAfterOneRelocation(nums3)); // Expected: 1
        System.out.println(solution.maxPrefixScoreAfterOneRelocation(nums4)); // Expected: 0
        System.out.println(solution.maxPrefixScoreAfterOneRelocation(nums5)); // Example extra case
    }

    /**
     * Simple helper structure storing a numeric value and its index.
     */
    static class ValueIndex {
        long value;
        int index;

        ValueIndex(long value, int index) {
            this.value = value;
            this.index = index;
        }
    }

    /**
     * Helper structure for an offline threshold query:
     * count values > threshold inside index range [left, right].
     */
    static class ThresholdQuery {
        long threshold;
        int elementIndex;
        int left;
        int right;

        ThresholdQuery(long threshold, int elementIndex, int left, int right) {
            this.threshold = threshold;
            this.elementIndex = elementIndex;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Fenwick Tree / Binary Indexed Tree for point updates and prefix/range sums.
     */
    static class Fenwick {
        private final int[] tree;

        /**
         * Creates a Fenwick tree of given size.
         *
         * @param size number of positions
         */
        Fenwick(int size) {
            this.tree = new int[size + 1];
        }

        /**
         * Adds delta at 1-based index.
         *
         * @param index 1-based index
         * @param delta value to add
         */
        void add(int index, int delta) {
            int n = tree.length - 1;
            while (index <= n) {
                tree[index] += delta;
                index += index & -index;
            }
        }

        /**
         * Returns prefix sum from 1 to index inclusive.
         *
         * @param index 1-based index
         * @return prefix sum
         */
        int sum(int index) {
            int result = 0;
            while (index > 0) {
                result += tree[index];
                index -= index & -index;
            }
            return result;
        }

        /**
         * Returns range sum from left to right inclusive, both 1-based.
         *
         * @param left left boundary, 1-based
         * @param right right boundary, 1-based
         * @return range sum
         */
        int rangeSum(int left, int right) {
            if (left > right) {
                return 0;
            }
            return sum(right) - sum(left - 1);
        }
    }
}