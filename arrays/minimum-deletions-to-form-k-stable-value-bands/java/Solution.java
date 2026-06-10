import java.util.*;

/*
Title: Minimum Deletions to Form K Stable Value Bands

Problem Description:
You are given an integer array nums of length n and an integer k. A subsequence of nums is called stable if the difference between its maximum and minimum value is at most 1. You may delete any number of elements from nums, and the remaining elements must be partitioned into exactly k non-empty stable groups. Each remaining element must belong to exactly one group, and the order of elements inside a group does not matter. Your task is to return the minimum number of deletions required so that such a partition is possible. If it is impossible to form exactly k non-empty stable groups from any subsequence of nums, return -1.

A stable group may contain repeated values, and it may also contain two adjacent values such as x and x+1, but it cannot contain values whose difference is 2 or more. Note that the groups are defined by values, not by contiguous positions in the original array. In other words, after deleting elements, you are free to assign the remaining elements into groups in any way that respects the stability rule.

Constraints:
- 1 <= n <= 200000
- -10^9 <= nums[i] <= 10^9
- 1 <= k <= n

Example 1:
Input: nums = [1,1,2,2,3,5,5], k = 3
Output: 1
Explanation: Delete the value 3. The remaining elements can be partitioned into three stable groups: [1,1,2,2], [5], [5]. Each group has max-min <= 1, so the answer is 1.

Example 2:
Input: nums = [4,4,4,7,8], k = 2
Output: 0
Explanation: No deletion is needed. One valid partition is [4,4,4] and [7,8]. Both groups are stable, non-empty, and together use all remaining elements.
*/

public class Solution {

    /**
     * Computes the minimum number of deletions required so that the remaining elements
     * can be partitioned into exactly k non-empty stable groups.
     *
     * Core idea:
     * 1. Count frequencies of each distinct value.
     * 2. Sort distinct values.
     * 3. Values that differ by at least 2 cannot appear in the same stable group.
     *    Therefore, the sorted distinct values split naturally into independent "blocks"
     *    of consecutive integers.
     * 4. For one block with frequencies c1, c2, ..., cm:
     *      - A stable group can use only one value i, or two adjacent values i and i+1.
     *      - If we decide to keep all elements in this block, the maximum number of groups
     *        we can form equals sum(ci), because every element can be its own group.
     *      - The minimum number of groups needed to keep all elements in this block equals
     *        max(ci), because each group can contain at most one "copy slot" from each value,
     *        and adjacent values may share groups.
     *      - In fact, for any group count g in [max(ci), sum(ci)], it is possible to keep
     *        all elements of the block and partition them into exactly g stable groups.
     * 5. Across different blocks, groups cannot mix. So each block contributes some number
     *    of groups within an interval [L, R].
     * 6. We want to maximize the number of kept elements while achieving total groups exactly k.
     *    This becomes a knapsack-like DP over blocks:
     *      dp[t] = maximum kept elements after processing some blocks and forming exactly t groups.
     *    For each block:
     *      - We may skip the block entirely (keep 0 elements, add 0 groups).
     *      - Or keep all its elements and choose any group count in [L, R], gaining blockSum elements.
     * 7. Answer = n - maximum kept elements for exactly k groups, or -1 if impossible.
     *
     * @param nums the input array of integers
     * @param k the exact number of non-empty stable groups required
     * @return the minimum deletions needed, or -1 if impossible
     * Time complexity: O(n log n + B * k), where B is the number of consecutive-value blocks
     * Space complexity: O(k + m), where m is the number of distinct values
     */
    public int minimumDeletions(int[] nums, int k) {
        int n = nums.length;

        // If we need more groups than elements, impossible immediately.
        // This cannot happen under constraints because k <= n, but keeping the check is harmless.
        if (k > n) {
            return -1;
        }

        // Step 1: Count frequency of each value.
        // TreeMap keeps keys sorted automatically, which is convenient for building consecutive blocks.
        TreeMap<Integer, Integer> freq = new TreeMap<>();
        for (int value : nums) {
            freq.merge(value, 1, Integer::sum);
        }

        // Step 2: Convert sorted frequencies into independent blocks of consecutive values.
        // For each block we store:
        //   blockSum = total number of elements in the block
        //   blockMinGroups = minimum groups needed to keep all elements in the block = max frequency in the block
        //   blockMaxGroups = maximum groups possible while keeping all elements in the block = blockSum
        List<int[]> blocks = buildBlocks(freq);

        // Step 3: DP over blocks.
        // dp[g] = maximum number of elements we can keep using exactly g groups after processing some prefix of blocks.
        // Use a very negative number to represent impossible states.
        int NEG = Integer.MIN_VALUE / 4;
        int[] dp = new int[k + 1];
        Arrays.fill(dp, NEG);
        dp[0] = 0;

        for (int[] block : blocks) {
            int blockSum = block[0];
            int minGroups = block[1];
            int maxGroups = block[2];

            // next starts as "skip this block" transitions:
            // if we skip the block, group count and kept elements do not change.
            int[] next = Arrays.copyOf(dp, dp.length);

            // If we keep this block, we must choose a group count gBlock in [minGroups, maxGroups].
            // Keeping the block always contributes exactly blockSum kept elements.
            //
            // Transition:
            //   next[used + gBlock] = max(next[used + gBlock], dp[used] + blockSum)
            //
            // Because all choices in [minGroups, maxGroups] have the same value gain,
            // this is an interval transition. For clarity and beginner-friendliness,
            // we implement it directly. This is efficient enough because the total number
            // of blocks is at most the number of distinct values, and k <= 200000.
            for (int used = 0; used <= k; used++) {
                if (dp[used] == NEG) {
                    continue;
                }

                int from = used + minGroups;
                if (from > k) {
                    continue;
                }

                int to = Math.min(k, used + maxGroups);
                int candidate = dp[used] + blockSum;

                for (int totalGroups = from; totalGroups <= to; totalGroups++) {
                    if (candidate > next[totalGroups]) {
                        next[totalGroups] = candidate;
                    }
                }
            }

            dp = next;
        }

        // If exactly k groups is impossible, return -1.
        if (dp[k] < 0) {
            return -1;
        }

        // Otherwise, minimize deletions = total elements - maximum kept elements.
        return n - dp[k];
    }

    /**
     * Builds consecutive-value blocks from the sorted frequency map.
     *
     * For one block of consecutive values with frequencies:
     *   c1, c2, ..., cm
     *
     * We compute:
     * - blockSum = c1 + c2 + ... + cm
     * - minGroups = max(c1, c2, ..., cm)
     * - maxGroups = blockSum
     *
     * Why is minGroups = max frequency?
     * Because each group can contain at most one copy of a specific value if we want
     * the group to remain a set/multiset whose values differ by at most 1 and we are
     * free to place repeated equal values together. More importantly, to realize all
     * copies of the most frequent value, we need at least that many groups available
     * as "containers" when adjacent values are merged flexibly. This lower bound is
     * also achievable for a consecutive block.
     *
     * Why is every group count between minGroups and maxGroups achievable?
     * Starting from the partition with each element alone (blockSum groups), we can
     * repeatedly merge groups involving adjacent values while preserving stability,
     * until reaching the minimum possible count. Therefore the whole interval is feasible.
     *
     * @param freq sorted map from value to frequency
     * @return a list of blocks, where each block is int[]{blockSum, minGroups, maxGroups}
     * Time complexity: O(m), where m is the number of distinct values
     * Space complexity: O(b), where b is the number of blocks
     */
    public List<int[]> buildBlocks(TreeMap<Integer, Integer> freq) {
        List<int[]> blocks = new ArrayList<>();
        if (freq.isEmpty()) {
            return blocks;
        }

        Integer prevValue = null;

        int currentSum = 0;
        int currentMaxFreq = 0;

        for (Map.Entry<Integer, Integer> entry : freq.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();

            // If this value is not consecutive to the previous one,
            // the current block ends and a new block begins.
            if (prevValue != null && value != prevValue + 1) {
                blocks.add(new int[]{currentSum, currentMaxFreq, currentSum});
                currentSum = 0;
                currentMaxFreq = 0;
            }

            currentSum += count;
            currentMaxFreq = Math.max(currentMaxFreq, count);
            prevValue = value;
        }

        // Add the final block.
        blocks.add(new int[]{currentSum, currentMaxFreq, currentSum});

        return blocks;
    }

    /**
     * A small helper to run one demonstration case and print the result.
     *
     * @param nums the input array
     * @param k the required number of groups
     * @return the computed answer
     * Time complexity: O(n log n + B * k)
     * Space complexity: O(k + m)
     */
    public int demo(int[] nums, int k) {
        int answer = minimumDeletions(nums, k);
        System.out.println("nums = " + Arrays.toString(nums) + ", k = " + k + " -> " + answer);
        return answer;
    }

    /**
     * Demonstrates the solution on the sample inputs and a few extra sanity checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: depends on the demonstration inputs
     * Space complexity: depends on the demonstration inputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // nums = [1,1,2,2,3,5,5], k = 3
        // Delete 3, keep [1,1,2,2,5,5], partition as [1,1,2,2], [5], [5]
        // Expected: 1
        solution.demo(new int[]{1, 1, 2, 2, 3, 5, 5}, 3);

        // Sample 2:
        // nums = [4,4,4,7,8], k = 2
        // Keep all, partition as [4,4,4], [7,8]
        // Expected: 0
        solution.demo(new int[]{4, 4, 4, 7, 8}, 2);

        // Extra checks:
        // One group can keep all if overall max-min <= 1 after choosing a subsequence.
        solution.demo(new int[]{10, 10, 11, 11}, 1); // Expected: 0

        // Need many groups; can always split kept elements more, up to number of kept elements.
        solution.demo(new int[]{1, 2, 3}, 3); // Expected: 0

        // Impossible to form exactly 1 stable group using all values, but can delete one side.
        solution.demo(new int[]{1, 3}, 1); // Expected: 1

        // If exact k is impossible even after deletions, return -1.
        // Here k=3 with only two elements.
        solution.demo(new int[]{5, 6}, 3); // Expected: -1
    }
}