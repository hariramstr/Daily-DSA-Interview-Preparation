import java.util.*;

/*
 * Title: Minimum Cost to Partition a Transcript into Consistent Speaker Blocks
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a transcript of a meeting represented by an array labels of length n,
 * where labels[i] is the speaker ID of the i-th utterance. You want to split the
 * transcript into one or more contiguous blocks. For each block, you will assign exactly
 * one speaker as the block's "owner". Every utterance in that block spoken by the owner
 * is considered consistent and costs 0, while every utterance spoken by a different
 * speaker is considered a correction and costs 1.
 *
 * In addition to correction costs, creating a block from index l to r incurs a fixed
 * overhead overhead. Therefore, the total cost of a partition is the sum, over all blocks, of:
 *
 * overhead + (block length - maximum frequency of any speaker inside that block).
 *
 * Your task is to return the minimum possible total cost to partition the entire transcript.
 *
 * Intuitively, each block should be assigned to the speaker who appears most often in that
 * block, and all other utterances in the block are treated as mismatches. The challenge is
 * to decide where to cut the transcript so that the sum of block overheads and mismatch
 * counts is minimized.
 *
 * Constraints:
 * - 1 <= n <= 5000
 * - 1 <= labels[i] <= 5000
 * - 0 <= overhead <= 10^9
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: labels = [1, 2, 1, 1, 3], overhead = 2
 * Output: 4
 * Explanation:
 * One partition [1, 2, 1, 1] | [3] gives:
 * - first block: 2 + (4 - 3) = 3
 * - second block: 2 + (1 - 1) = 2
 * total = 5
 * But taking the whole array as one block gives:
 * 2 + (5 - 3) = 4
 *
 * Example 2:
 * Input: labels = [4, 4, 2, 2, 2, 4, 4], overhead = 1
 * Output: 3
 * Explanation:
 * Partition [4, 4] | [2, 2, 2] | [4, 4]
 * Each block is perfectly consistent, so each costs only 1.
 * Total = 3
 * A single block would cost 1 + (7 - 4) = 4
 */

public class Solution {

    /**
     * Computes the minimum total cost to partition the transcript into contiguous blocks.
     *
     * The key dynamic programming idea:
     * - Let dp[i] be the minimum cost to partition the prefix labels[0..i-1].
     * - Then for every possible ending position i, we try every possible starting position j
     *   of the last block, meaning the last block is labels[j..i-1].
     * - The cost of that last block is:
     *     overhead + (length of block - maximum frequency in the block)
     * - Therefore:
     *     dp[i] = min over j in [0, i-1] of dp[j] + blockCost(j, i-1)
     *
     * To evaluate all blocks ending at i-1 efficiently enough for n <= 5000:
     * - We iterate j backwards from i-1 to 0.
     * - While extending the block to the left, we maintain:
     *   1) frequency of each speaker in the current block
     *   2) current maximum frequency
     *   3) current block length
     * - This lets us compute each block cost in O(1) amortized update time,
     *   leading to an overall O(n^2) algorithm.
     *
     * @param labels the speaker IDs for each utterance
     * @param overhead the fixed overhead cost for creating each block
     * @return the minimum possible total partition cost
     * Time complexity: O(n^2)
     * Space complexity: O(n + V), where V is the maximum speaker ID value used for frequency counting
     */
    public long minimumPartitionCost(int[] labels, long overhead) {
        int n = labels.length;

        // dp[i] = minimum cost to partition the first i utterances, i.e. labels[0..i-1]
        long[] dp = new long[n + 1];

        // Initialize all states to a very large number so we can safely take minimums.
        Arrays.fill(dp, Long.MAX_VALUE / 4);

        // Base case:
        // Partitioning an empty prefix costs 0 because there is nothing to cover.
        dp[0] = 0L;

        // Speaker IDs are constrained to be <= 5000, but to keep the method robust and beginner-friendly,
        // we compute the maximum label actually present and size the frequency array accordingly.
        int maxLabel = 0;
        for (int label : labels) {
            maxLabel = Math.max(maxLabel, label);
        }

        // Outer loop:
        // We compute dp[end] for end = 1..n.
        // Here, "end" means the prefix length, so the last included index is end - 1.
        for (int end = 1; end <= n; end++) {
            // Frequency array for the current sweep of blocks ending at end - 1.
            // freq[speaker] = how many times that speaker appears in the current block [start..end-1].
            int[] freq = new int[maxLabel + 1];

            // maxFreq tracks the highest frequency of any speaker in the current block.
            int maxFreq = 0;

            // We now try every possible start position for the last block.
            // We move start backward so that the block grows one element at a time:
            // [end-1..end-1], [end-2..end-1], [end-3..end-1], ...
            for (int start = end - 1; start >= 0; start--) {
                int speaker = labels[start];

                // Include labels[start] into the current block.
                freq[speaker]++;

                // Update the maximum frequency seen in this block.
                if (freq[speaker] > maxFreq) {
                    maxFreq = freq[speaker];
                }

                // Current block length is from start to end-1 inclusive.
                int length = end - start;

                // Mismatch/correction count:
                // If the best owner appears maxFreq times, then all other utterances
                // in the block are mismatches, so mismatches = length - maxFreq.
                long mismatches = (long) length - maxFreq;

                // Total cost of this block.
                long blockCost = overhead + mismatches;

                // Candidate total cost:
                // best cost to partition prefix [0..start-1] plus cost of last block [start..end-1]
                long candidate = dp[start] + blockCost;

                // Relax dp[end].
                if (candidate < dp[end]) {
                    dp[end] = candidate;
                }
            }
        }

        return dp[n];
    }

    /**
     * Convenience overload that accepts the overhead as an int.
     *
     * @param labels the speaker IDs for each utterance
     * @param overhead the fixed overhead cost for creating each block
     * @return the minimum possible total partition cost
     * Time complexity: O(n^2)
     * Space complexity: O(n + V), where V is the maximum speaker ID value used for frequency counting
     */
    public long minimumPartitionCost(int[] labels, int overhead) {
        return minimumPartitionCost(labels, (long) overhead);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * This method also prints the expected values so it is easy to visually verify correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1), excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] labels1 = {1, 2, 1, 1, 3};
        long overhead1 = 2;
        long result1 = solution.minimumPartitionCost(labels1, overhead1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 4");

        // Example 2
        int[] labels2 = {4, 4, 2, 2, 2, 4, 4};
        long overhead2 = 1;
        long result2 = solution.minimumPartitionCost(labels2, overhead2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 3");

        // Additional small sanity checks

        // Single utterance:
        // One block, perfectly consistent => overhead + (1 - 1) = overhead
        int[] labels3 = {7};
        long overhead3 = 5;
        long result3 = solution.minimumPartitionCost(labels3, overhead3);
        System.out.println("Single utterance result: " + result3);
        System.out.println("Expected: 5");

        // All same speaker:
        // Best is usually one block if overhead is non-negative.
        int[] labels4 = {2, 2, 2, 2};
        long overhead4 = 3;
        long result4 = solution.minimumPartitionCost(labels4, overhead4);
        System.out.println("All same speaker result: " + result4);
        System.out.println("Expected: 3");

        // Alternating speakers with zero overhead:
        // Since overhead is zero, splitting into singletons can eliminate all mismatches.
        int[] labels5 = {1, 2, 1, 2};
        long overhead5 = 0;
        long result5 = solution.minimumPartitionCost(labels5, overhead5);
        System.out.println("Zero overhead alternating result: " + result5);
        System.out.println("Expected: 0");
    }
}