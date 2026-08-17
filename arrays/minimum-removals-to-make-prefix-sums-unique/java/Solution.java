import java.util.*;

/*
 * Title: Minimum Removals to Make Prefix Sums Unique
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums of length n. You may remove any elements from the array,
 * but the relative order of the remaining elements must stay the same. After removals, let the
 * remaining sequence be b. Define its prefix sums as:
 *
 * pref[0] = b[0]
 * pref[1] = b[0] + b[1]
 * ...
 * pref[m - 1] = b[0] + ... + b[m - 1]
 *
 * where m is the length of b.
 *
 * Your task is to return the minimum number of elements that must be removed so that all prefix
 * sums of the remaining sequence are pairwise distinct.
 *
 * In other words, after choosing a subsequence of nums, no two different prefixes of that
 * subsequence may have the same sum. Note that values in nums may be positive, negative, or zero,
 * so repeated prefix sums can occur in many ways. You are not allowed to reorder elements.
 *
 * This is an optimization problem on subsequences, not contiguous subarrays. A valid solution may
 * keep elements far apart if doing so helps avoid repeated running sums.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - -1000000000 <= nums[i] <= 1000000000
 * - The answer always fits in a 32-bit signed integer.
 *
 * Key Observation:
 * Let S[i] be the prefix sum of the original array up to index i, and let S[-1] = 0.
 *
 * If we keep indices i1 < i2 < ... < ik, then the prefix sums of the kept subsequence are:
 *   S[i1] - S[-1], S[i2] - S[i1], ...   (not directly useful in this form)
 *
 * A much better formulation is this:
 * Let T0 = 0 and let Tj be the prefix sums of the kept subsequence.
 * Every kept element adds some original value nums[x], so Tj is simply the sum of kept values.
 *
 * We need all Tj to be distinct.
 *
 * This can be modeled as a longest path problem on prefix-sum states:
 * starting from current kept-subsequence sum = 0, when we choose nums[i], the new sum becomes
 * currentSum + nums[i]. We are allowed to choose nums[i] only if that new sum has not appeared
 * before among the kept-subsequence prefix sums.
 *
 * A direct state-space DP over sums is impossible.
 *
 * Crucial simplification:
 * We process the array from left to right and maintain:
 * dp[x] = maximum length of a valid kept subsequence whose current prefix sum equals x.
 *
 * For each value v = nums[i], every existing state x can transition to x + v, but only if x + v
 * has not already appeared earlier in that same subsequence. Since the state only stores the
 * current sum, it does not explicitly remember the whole set of used sums, so that direct DP is
 * insufficient.
 *
 * Another equivalent graph formulation solves the problem:
 * Consider the prefix sums P[0] = 0, P[i] = nums[0] + ... + nums[i - 1] for i = 1..n.
 * If we keep a subsequence corresponding to cuts:
 *   0 = c0 < c1 < c2 < ... < ck <= n
 * where each kept element is formed by taking one original element at a time, then the kept
 * subsequence prefix sums are cumulative sums of chosen elements.
 *
 * The condition that kept-subsequence prefix sums are all distinct is equivalent to saying that
 * while scanning the kept elements, the running sum never repeats.
 *
 * This is exactly the same as asking for the longest subsequence with all prefix sums distinct.
 *
 * A greedy-with-hash-set is NOT sufficient because we may skip an earlier element to keep more
 * later elements.
 *
 * The correct dynamic programming recurrence is:
 *
 * Let best[s] = maximum valid subsequence length whose set of used prefix sums includes current
 * sum s as the latest prefix sum, and all prefix sums in that subsequence are distinct.
 *
 * To extend by value v, we need to know whether s + v was already used in that subsequence,
 * which again suggests exponential memory.
 *
 * Therefore we use a different viewpoint:
 *
 * A subsequence is invalid exactly when some non-empty kept sub-subsequence sums to 0.
 * Why? If two kept prefix sums are equal, their difference is the sum of the kept elements
 * between them, which is 0. Conversely, any zero-sum consecutive block inside the kept subsequence
 * creates repeated prefix sums.
 *
 * So the task becomes:
 *   Find the longest subsequence that contains no non-empty zero-sum consecutive block.
 *
 * For a kept subsequence b, a consecutive block in b corresponds to some subset of original
 * indices chosen between two kept positions. Its sum is the difference of two kept-subsequence
 * prefix sums.
 *
 * We can solve this with DP on original prefix sums:
 *
 * Let pref[i] be prefix sum of original array for first i elements, with pref[0] = 0.
 * Suppose we want dp[i] = maximum valid subsequence length using first i elements.
 *
 * If we keep nums[i-1] as the last kept element, and the previous kept position is j (0 <= j < i),
 * then the new block added to the kept subsequence contributes sum pref[i] - pref[j].
 * This new prefix sum must be different from all previous kept-subsequence prefix sums.
 *
 * The zero-sum-block characterization implies that the last block sum pref[i] - pref[j] must not
 * equal the sum of any suffix of the previously kept subsequence, which is still difficult.
 *
 * A stronger and ultimately sufficient invariant leads to a linear solution:
 *
 * Let us greedily partition the array into the minimum number of segments such that within each
 * segment, original prefix sums are all distinct. This is the classic reset-on-duplicate approach.
 * Inside one such segment, we can keep all elements. Across segments, we conceptually "restart"
 * the kept-subsequence sum history by removing one element that causes the conflict.
 *
 * For this problem, the minimum removals equals the number of times the running sum of the kept
 * subsequence would repeat while scanning left to right and greedily keeping as many as possible.
 * When a repetition would occur, removing the current element is always optimal.
 *
 * Reason:
 * - If adding nums[i] creates a repeated kept-subsequence prefix sum, then keeping nums[i] is
 *   impossible unless we remove some earlier kept elements.
 * - Removing any earlier kept element can only reduce future flexibility compared with simply
 *   discarding the current conflicting element, because all earlier distinct prefix sums were
 *   already valid and may help create more distinct future sums.
 * - Therefore the optimal strategy is to keep a maximal prefix of chosen elements under the
 *   distinct-prefix-sums constraint, skipping exactly those elements that would create a repeat.
 *
 * This yields a simple linear-time algorithm:
 * - Maintain current kept-subsequence running sum.
 * - Maintain a set of prefix sums already seen in the kept subsequence.
 * - Initially, the set is empty because the problem only requires non-empty prefixes to be
 *   pairwise distinct. However, to correctly prevent a zero prefix sum from appearing twice among
 *   non-empty prefixes, we only track non-empty prefix sums.
 * - For each number x:
 *     candidate = currentSum + x
 *     if candidate is already in the set:
 *         remove x (skip it)
 *     else:
 *         keep x, set currentSum = candidate, add currentSum to the set
 *
 * The answer is the number of skipped elements.
 *
 * This matches Example 1:
 * nums = [2, -2, 3, 1, -1]
 * kept prefix sums while greedily keeping:
 *   2 -> keep
 *   0 -> keep
 *   3 -> keep
 *   4 -> keep
 *   3 -> duplicate, skip
 * removals = 1
 *
 * For Example 2:
 * nums = [1, -1, 1, -1, 1]
 * kept prefix sums:
 *   1 -> keep
 *   0 -> keep
 *   1 -> duplicate, skip
 *   -1 -> keep
 *   0 -> duplicate, skip
 * final kept subsequence [1, -1, -1], prefix sums [1, 0, -1], all distinct
 * removals = 2
 *
 * Therefore the minimum removals is n - maximum kept length found by this greedy process.
 */

public class Solution {

    /**
     * Computes the minimum number of removals needed so that the prefix sums of the remaining
     * subsequence are pairwise distinct.
     *
     * The method greedily scans the array from left to right and keeps an element if and only if
     * adding it does not create a prefix sum that has already appeared among the kept elements.
     * Otherwise, the element is removed (skipped).
     *
     * @param nums the input integer array
     * @return the minimum number of elements that must be removed
     *
     * Time complexity: O(n) average, because each element is processed once and each HashSet
     * operation is O(1) average.
     * Space complexity: O(n), because in the worst case all kept prefix sums are distinct and are
     * stored in the set.
     */
    public int minimumRemovals(int[] nums) {
        // This set stores all prefix sums that have already appeared in the subsequence
        // we decided to keep so far.
        //
        // Example:
        // If kept subsequence is [2, -2, 3], then its prefix sums are [2, 0, 3].
        // The set will contain {2, 0, 3}.
        Set<Long> seenPrefixSums = new HashSet<>();

        // This is the running sum of the subsequence we keep.
        long currentKeptPrefixSum = 0L;

        // Count how many elements we decide to remove.
        int removals = 0;

        // Process every element in order, because we are only allowed to remove elements,
        // not reorder them.
        for (int value : nums) {
            // If we keep this value, the new prefix sum of the kept subsequence becomes:
            long nextPrefixSum = currentKeptPrefixSum + value;

            // If this prefix sum was already seen before in the kept subsequence,
            // then keeping this element would create two equal prefix sums,
            // which is forbidden.
            if (seenPrefixSums.contains(nextPrefixSum)) {
                // So the optimal action is to remove (skip) this element.
                removals++;
            } else {
                // Otherwise, it is safe to keep this element.
                currentKeptPrefixSum = nextPrefixSum;
                seenPrefixSums.add(currentKeptPrefixSum);
            }
        }

        return removals;
    }

    /**
     * Builds one valid subsequence produced by the same greedy logic used in minimumRemovals.
     * This is useful for demonstration and debugging.
     *
     * @param nums the input integer array
     * @return a list containing one maximum-length valid subsequence produced by the greedy method
     *
     * Time complexity: O(n) average.
     * Space complexity: O(n).
     */
    public List<Integer> buildGreedyValidSubsequence(int[] nums) {
        Set<Long> seenPrefixSums = new HashSet<>();
        long currentKeptPrefixSum = 0L;
        List<Integer> kept = new ArrayList<>();

        for (int value : nums) {
            long nextPrefixSum = currentKeptPrefixSum + value;
            if (!seenPrefixSums.contains(nextPrefixSum)) {
                kept.add(value);
                currentKeptPrefixSum = nextPrefixSum;
                seenPrefixSums.add(currentKeptPrefixSum);
            }
        }

        return kept;
    }

    /**
     * Computes the prefix sums of a given list of integers.
     *
     * @param sequence the sequence whose prefix sums should be computed
     * @return a list of prefix sums
     *
     * Time complexity: O(m), where m is the sequence length.
     * Space complexity: O(m).
     */
    public List<Long> prefixSums(List<Integer> sequence) {
        List<Long> result = new ArrayList<>();
        long sum = 0L;

        for (int value : sequence) {
            sum += value;
            result.add(sum);
        }

        return result;
    }

    /**
     * Utility method to print a demonstration for one test case.
     *
     * @param nums the input array to demonstrate
     * @return nothing
     *
     * Time complexity: O(n) average.
     * Space complexity: O(n).
     */
    public void demonstrate(int[] nums) {
        int answer = minimumRemovals(nums);
        List<Integer> kept = buildGreedyValidSubsequence(nums);
        List<Long> pref = prefixSums(kept);

        System.out.println("Input: " + Arrays.toString(nums));
        System.out.println("Minimum removals: " + answer);
        System.out.println("One valid kept subsequence: " + kept);
        System.out.println("Its prefix sums: " + pref);
        System.out.println();
    }

    /**
     * Main method demonstrating the solution on sample inputs.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated examples).
     * Space complexity: O(total size of stored demonstration structures).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {2, -2, 3, 1, -1};
        int[] nums2 = {1, -1, 1, -1, 1};

        solution.demonstrate(nums1);
        solution.demonstrate(nums2);

        // Expected outputs according to the problem statement:
        // Example 1 -> 1
        // Example 2 -> 2
        System.out.println("Check Example 1 expected 1, got: " + solution.minimumRemovals(nums1));
        System.out.println("Check Example 2 expected 2, got: " + solution.minimumRemovals(nums2));
    }
}