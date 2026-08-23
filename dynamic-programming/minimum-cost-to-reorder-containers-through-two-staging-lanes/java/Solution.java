import java.util.*;

/*
 * Title: Minimum Cost to Reorder Containers Through Two Staging Lanes
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A port receives containers in a fixed arrival order. Each container has a positive integer weight.
 * Before loading them onto a ship, the port may route every arriving container into exactly one of
 * two staging lanes, A or B. Containers assigned to the same lane must remain in their original
 * relative order. After all containers are assigned, the ship is loaded by repeatedly taking the
 * front container from either lane until both lanes are empty.
 *
 * The final loading order must be nondecreasing by weight. If a container of weight w is placed
 * immediately after a container of weight p in the final sequence, the loading cost increases by
 * |w - p|. The first loaded container adds no cost.
 *
 * Your task is to compute the minimum possible total loading cost, or return -1 if no valid
 * nondecreasing loading order can be formed using exactly these two staging lanes.
 *
 * In other words, you must partition the original sequence into two subsequences, preserving order
 * within each subsequence, so that they can be merged into one nondecreasing sequence. Among all
 * such feasible partitions and merges, minimize the sum of absolute differences between consecutive
 * loaded weights.
 *
 * Constraints:
 * - 1 <= n <= 3000
 * - 1 <= weights[i] <= 10^9
 * - The input is a single array weights of length n.
 * - An O(n^2) dynamic programming solution is expected.
 *
 * Example 1:
 * Input: weights = [4, 1, 3, 2]
 * Output: 3
 * Explanation: Put [4, 3] in lane A and [1, 2] in lane B. A valid merged nondecreasing loading
 * order is [1, 2, 3, 4], with cost |2-1| + |3-2| + |4-3| = 3.
 *
 * Example 2:
 * Input: weights = [3, 1, 2, 1]
 * Output: -1
 * Explanation: No matter how the containers are split into two order-preserving lanes, the two lane
 * fronts cannot be merged into a fully nondecreasing sequence containing all containers. Therefore
 * the answer is -1.
 *
 * Notes:
 * - The merge may switch between lanes arbitrarily many times.
 * - Equal weights are allowed in the final loading order.
 * - A feasible solution exists exactly when the sequence can be expressed as the merge of two
 *   nondecreasing subsequences, but you must still minimize the loading cost among all feasible
 *   choices.
 */
public class Solution {

    /**
     * A large value used as "infinity" for impossible DP states.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum possible total loading cost, or -1 if no valid nondecreasing loading
     * order can be formed using exactly two staging lanes.
     *
     * Core idea:
     * We process containers in their original arrival order. Each container must be assigned to one
     * of two lanes. Inside each lane, order is preserved. A final nondecreasing merge exists if and
     * only if each lane itself is nondecreasing, because the merge of two nondecreasing sequences
     * can always be performed into a nondecreasing overall sequence.
     *
     * Therefore, the problem becomes:
     * 1) Partition the array into two nondecreasing subsequences.
     * 2) Among all such partitions, minimize the total cost of the final merged nondecreasing
     *    sequence.
     *
     * Important simplification:
     * Any valid final loading order must be the globally sorted order of the original multiset of
     * weights. Since the final sequence must be nondecreasing and must contain exactly the same
     * elements, the sequence of values is fixed after sorting. Therefore the total cost is simply:
     * sum over consecutive values in the sorted array of their difference, which telescopes to
     * maxValue - minValue when n >= 1.
     *
     * So the optimization part disappears:
     * - If a feasible partition into two nondecreasing subsequences exists, the minimum cost is
     *   exactly sorted[n - 1] - sorted[0].
     * - Otherwise answer is -1.
     *
     * Thus we only need to test feasibility:
     * Can the sequence be partitioned into at most two nondecreasing subsequences?
     *
     * This is equivalent, by Dilworth/Mirsky style duality for sequences, to checking whether the
     * longest strictly decreasing subsequence has length at most 2.
     *
     * Since the problem explicitly expects an O(n^2) dynamic programming solution, we compute the
     * length of the longest strictly decreasing subsequence (LDS) with classic O(n^2) DP:
     *   dec[i] = 1 + max(dec[j]) over all j < i with weights[j] > weights[i]
     *
     * If max(dec[i]) >= 3, then we would need at least 3 nondecreasing subsequences, so two lanes
     * are not enough.
     *
     * @param weights the arrival-order array of container weights
     * @return the minimum loading cost if feasible; otherwise -1
     * Time complexity: O(n^2)
     * Space complexity: O(n)
     */
    public long minimumCost(int[] weights) {
        if (weights == null || weights.length == 0) {
            return 0L;
        }

        int n = weights.length;

        // Step 1:
        // Check whether the sequence can be partitioned into two nondecreasing subsequences.
        // We do this by computing the length of the longest strictly decreasing subsequence.
        int lds = longestStrictlyDecreasingSubsequenceLength(weights);

        // If the longest strictly decreasing subsequence has length 3 or more,
        // then two nondecreasing lanes are not enough.
        if (lds > 2) {
            return -1L;
        }

        // Step 2:
        // Since any valid final loading order must be the sorted multiset of weights,
        // the minimum (and actually unique) total cost is:
        // sortedMax - sortedMin
        //
        // Why?
        // In a nondecreasing sequence s0 <= s1 <= ... <= s(n-1),
        // sum |s[i] - s[i-1]| = sum (s[i] - s[i-1]) = s(n-1) - s0.
        long min = weights[0];
        long max = weights[0];
        for (int w : weights) {
            if (w < min) {
                min = w;
            }
            if (w > max) {
                max = w;
            }
        }

        return max - min;
    }

    /**
     * Computes the length of the longest strictly decreasing subsequence using classic O(n^2)
     * dynamic programming.
     *
     * Detailed DP definition:
     * - dec[i] means: the length of the longest strictly decreasing subsequence that ends exactly
     *   at index i.
     *
     * Transition:
     * - Every single element alone forms a decreasing subsequence of length 1.
     * - For each earlier index j < i:
     *     if weights[j] > weights[i],
     *     then we may append weights[i] after a decreasing subsequence ending at j.
     *     So:
     *         dec[i] = max(dec[i], dec[j] + 1)
     *
     * The answer is max(dec[i]) over all i.
     *
     * @param weights the input array
     * @return length of the longest strictly decreasing subsequence
     * Time complexity: O(n^2)
     * Space complexity: O(n)
     */
    public int longestStrictlyDecreasingSubsequenceLength(int[] weights) {
        int n = weights.length;
        int[] dec = new int[n];
        int best = 0;

        // We process each position as the possible ending point of a decreasing subsequence.
        for (int i = 0; i < n; i++) {
            // Base case:
            // The subsequence containing only weights[i].
            dec[i] = 1;

            // Try to extend every valid decreasing subsequence ending earlier.
            for (int j = 0; j < i; j++) {
                // Strictly decreasing means earlier value must be larger.
                if (weights[j] > weights[i]) {
                    dec[i] = Math.max(dec[i], dec[j] + 1);
                }
            }

            best = Math.max(best, dec[i]);
        }

        return best;
    }

    /**
     * Convenience wrapper that returns the answer as an int when it is guaranteed to fit.
     *
     * @param weights the arrival-order array of container weights
     * @return the minimum loading cost if feasible; otherwise -1
     * Time complexity: O(n^2)
     * Space complexity: O(n)
     */
    public int minimumCostAsInt(int[] weights) {
        long ans = minimumCost(weights);
        if (ans < Integer.MIN_VALUE || ans > Integer.MAX_VALUE) {
            throw new ArithmeticException("Answer does not fit in int.");
        }
        return (int) ans;
    }

    /**
     * Runs a single demonstration test and prints the input and output.
     *
     * @param weights the test input array
     * @return nothing
     * Time complexity: O(n^2)
     * Space complexity: O(n)
     */
    public void runDemo(int[] weights) {
        System.out.println("weights = " + Arrays.toString(weights));
        System.out.println("minimum cost = " + minimumCost(weights));
        System.out.println();
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional sanity checks.
     *
     * Note:
     * For the stated problem, the mathematically correct answer for Example 1 [4,1,3,2] is -1,
     * because the sequence contains a strictly decreasing subsequence of length 3: 4 > 3 > 2,
     * so it cannot be partitioned into only two nondecreasing subsequences.
     *
     * Also, the example partition [4,3] and [1,2] is invalid because lane A would not be
     * nondecreasing, and therefore no nondecreasing merge can output 3 before 4 while preserving
     * lane order.
     *
     * The algorithm below follows the formal problem statement exactly and returns the correct
     * result under that statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(k * n^2) across demonstrated tests
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample inputs from the prompt.
        solution.runDemo(new int[]{4, 1, 3, 2}); // Correct under the formal statement: -1
        solution.runDemo(new int[]{3, 1, 2, 1}); // -1

        // Additional examples.
        solution.runDemo(new int[]{1, 2, 3, 4}); // feasible, cost = 4 - 1 = 3
        solution.runDemo(new int[]{2, 1, 2});    // feasible, cost = 2 - 1 = 1
        solution.runDemo(new int[]{5});          // feasible, cost = 0
        solution.runDemo(new int[]{2, 2, 2});    // feasible, cost = 0
        solution.runDemo(new int[]{3, 2, 1});    // not feasible with two nondecreasing lanes => -1
    }
}