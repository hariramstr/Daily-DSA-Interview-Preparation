import java.util.*;

/*
 * Title: Minimum Energy to Process a Sensor Queue
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A monitoring device must process a queue of sensor packets in order. The packets are represented
 * by an integer array load, where load[i] is the energy cost of processing the i-th packet by itself.
 * To reduce overhead, the device is allowed to process either one packet alone or two consecutive
 * packets together as a batch.
 *
 * If the device processes packet i alone, it spends load[i] energy.
 * If it processes packets i and i+1 together, it spends max(load[i], load[i+1]) + penalty energy,
 * where penalty is a fixed non-negative integer representing batching overhead.
 *
 * Every packet must be processed exactly once, and batches cannot overlap.
 * Return the minimum total energy needed to process the entire queue.
 *
 * Constraints:
 * - 1 <= load.length <= 100000
 * - 0 <= load[i] <= 1000000000
 * - 0 <= penalty <= 1000000000
 * - The answer fits in a 64-bit signed integer.
 *
 * Notes about the examples:
 * For Example 1, the correct minimum is actually 19:
 *   load = [4, 7, 2, 9], penalty = 1
 * Possible useful plans:
 *   - (0,1) together = 8, 2 alone = 2, 3 alone = 9 => 19
 *   - 0 alone = 4, (1,2) together = 8, 3 alone = 9 => 21
 *   - (0,1) together = 8, (2,3) together = 10 => 18
 * Therefore the true minimum is 18, not 19.
 *
 * So the problem statement's narrative contains an inconsistency.
 * This implementation follows the actual rules of the problem and computes the true minimum.
 */

public class Solution {

    /**
     * Computes the minimum total energy needed to process all packets.
     *
     * Dynamic Programming idea:
     * Let dp[i] represent the minimum energy needed to process the first i packets,
     * meaning packets in the index range [0, i - 1].
     *
     * Then:
     * 1. Process the last packet alone:
     *      dp[i] = dp[i - 1] + load[i - 1]
     *
     * 2. Process the last two packets together (only if i >= 2):
     *      dp[i] = dp[i - 2] + max(load[i - 2], load[i - 1]) + penalty
     *
     * We take the minimum of these choices.
     *
     * Because each state depends only on the previous two states, we can optimize
     * the space from O(n) to O(1).
     *
     * @param load the array where load[i] is the energy cost of processing packet i alone
     * @param penalty the extra overhead added when batching two consecutive packets
     * @return the minimum total energy required to process the entire queue
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long minimumEnergy(int[] load, int penalty) {
        int n = load.length;

        // Base case:
        // dp[0] = 0
        // Meaning: processing zero packets costs zero energy.
        long dpPrev2 = 0L;

        // Base case for one packet:
        // dp[1] = load[0]
        // With only one packet available, the only valid action is to process it alone.
        long dpPrev1 = load[0];

        // If there is only one packet, we can return immediately.
        if (n == 1) {
            return dpPrev1;
        }

        // We now build the answer for prefixes of length 2, 3, 4, ..., n.
        for (int i = 2; i <= n; i++) {
            // Option 1:
            // Process packet (i - 1) alone.
            // Then the first (i - 1) packets must already have been processed optimally.
            long costProcessAlone = dpPrev1 + load[i - 1];

            // Option 2:
            // Process packets (i - 2) and (i - 1) together as one batch.
            // Then the first (i - 2) packets must already have been processed optimally.
            long batchCost = Math.max((long) load[i - 2], (long) load[i - 1]) + (long) penalty;
            long costProcessPair = dpPrev2 + batchCost;

            // The optimal cost for the first i packets is the cheaper of the two choices.
            long current = Math.min(costProcessAlone, costProcessPair);

            // Shift the rolling DP window forward:
            // old dp[i - 1] becomes new dp[i - 2]
            // current dp[i] becomes new dp[i - 1]
            dpPrev2 = dpPrev1;
            dpPrev1 = current;
        }

        // After the loop, dpPrev1 stores dp[n], the answer for all packets.
        return dpPrev1;
    }

    /**
     * Computes the minimum total energy using a full DP array.
     *
     * This version is more explicit and beginner-friendly for learning,
     * because it stores the answer for every prefix.
     *
     * Let dp[i] = minimum energy to process the first i packets.
     *
     * Transition:
     * - Process packet i-1 alone:
     *     dp[i] = dp[i-1] + load[i-1]
     * - Process packets i-2 and i-1 together:
     *     dp[i] = dp[i-2] + max(load[i-2], load[i-1]) + penalty
     *
     * @param load the array where load[i] is the energy cost of processing packet i alone
     * @param penalty the extra overhead added when batching two consecutive packets
     * @return the minimum total energy required to process the entire queue
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long minimumEnergyWithDpArray(int[] load, int penalty) {
        int n = load.length;

        // dp[i] means the minimum cost to process the first i packets.
        long[] dp = new long[n + 1];

        // Processing zero packets costs zero.
        dp[0] = 0L;

        // Processing the first packet:
        // only one valid choice exists -> process it alone.
        dp[1] = load[0];

        // Fill the DP table from smaller prefixes to larger prefixes.
        for (int i = 2; i <= n; i++) {
            // Choice 1: process the last packet alone.
            long alone = dp[i - 1] + load[i - 1];

            // Choice 2: process the last two packets together.
            long pair = dp[i - 2] + Math.max((long) load[i - 2], (long) load[i - 1]) + (long) penalty;

            // Store the better choice.
            dp[i] = Math.min(alone, pair);
        }

        return dp[n];
    }

    /**
     * Runs a single demonstration test case and prints the result.
     *
     * @param load the packet energy array
     * @param penalty the batching penalty
     * @return the computed minimum energy
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long runDemo(int[] load, int penalty) {
        long result = minimumEnergy(load, penalty);
        System.out.println("load = " + Arrays.toString(load) + ", penalty = " + penalty);
        System.out.println("Minimum energy = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method demonstrating the solution on sample inputs.
     *
     * Note:
     * The first sample in the prompt contains an inconsistency in its explanation.
     * According to the stated rules, the correct minimum for [4, 7, 2, 9] with penalty 1 is 18,
     * achieved by batching (0,1) and batching (2,3):
     *   max(4,7)+1 = 8
     *   max(2,9)+1 = 10
     *   total = 18
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] load1 = {4, 7, 2, 9};
        int penalty1 = 1;
        long result1 = solution.runDemo(load1, penalty1);

        int[] load2 = {5, 1, 5, 1};
        int penalty2 = 0;
        long result2 = solution.runDemo(load2, penalty2);

        // Additional small sanity checks.
        int[] load3 = {10};
        int penalty3 = 5;
        long result3 = solution.runDemo(load3, penalty3);

        int[] load4 = {3, 8};
        int penalty4 = 2;
        long result4 = solution.runDemo(load4, penalty4);

        System.out.println("Summary:");
        System.out.println("Example 1 computed result = " + result1 + " (true optimum under the stated rules)");
        System.out.println("Example 2 computed result = " + result2);
        System.out.println("Single packet example result = " + result3);
        System.out.println("Two packet example result = " + result4);
    }
}