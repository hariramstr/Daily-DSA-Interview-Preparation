import java.util.*;

/*
 * Title: Minimum Cost to Reconfigure a Data Center Rack Row
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A data center has a row of n server racks, numbered from 0 to n - 1.
 * Each rack must end up assigned to exactly one power profile: profile A, profile B, or profile C.
 * Reconfiguring rack i to profile p has a given cost cost[i][p].
 *
 * The row must be partitioned into exactly k contiguous zones.
 * Inside each zone, all racks must use the same power profile.
 * Adjacent zones must use different profiles.
 * In addition, the length of every zone must be between minLen and maxLen inclusive.
 *
 * Task:
 * Compute the minimum total reconfiguration cost to assign profiles to all racks
 * while satisfying all rules. If no valid partition exists, return -1.
 *
 * Formally:
 * Choose exactly k segments that cover the entire array without overlap,
 * where each segment is contiguous, each segment length is in [minLen, maxLen],
 * every segment is assigned one of the 3 profiles, all racks in a segment share that profile,
 * and neighboring segments have different profiles.
 * The cost of a segment from l to r using profile p is:
 * sum(cost[i][p]) for l <= i <= r.
 *
 * Constraints:
 * - 1 <= n <= 5000
 * - 1 <= k <= 200
 * - cost.length == n
 * - cost[i].length == 3
 * - 0 <= cost[i][p] <= 10^9
 * - 1 <= minLen <= maxLen <= n
 *
 * Key idea:
 * Use prefix sums to query any segment cost in O(1).
 * Then use dynamic programming:
 *
 * dp[t][i][p] = minimum cost to cover the first i racks (indices 0..i-1)
 *               using exactly t zones, where the t-th (last) zone uses profile p.
 *
 * Transition:
 * The last zone ends at i and starts at s, where zone length L = i - s is in [minLen, maxLen].
 * Then:
 * dp[t][i][p] = segmentCost(s, i-1, p) + min(dp[t-1][s][q]) for q != p
 *
 * A direct implementation would be too slow because for every state we would scan all valid starts.
 * We optimize by maintaining, for each profile p, a sliding window minimum over:
 *
 * bestPrev[s][p] = min(dp[t-1][s][q]) for q != p
 *
 * Then:
 * dp[t][i][p] = prefix[p][i] + min over valid s of (bestPrev[s][p] - prefix[p][s])
 *
 * For each profile, the valid starts s for a fixed i form a sliding interval:
 * [i - maxLen, i - minLen]
 *
 * Therefore we can maintain the minimum of (bestPrev[s][p] - prefix[p][s]) in O(1) amortized
 * using a monotonic deque, giving total complexity O(k * n * 3).
 */

public class Solution {

    /**
     * A large sentinel value representing "impossible".
     * We keep it safely below Long.MAX_VALUE to avoid overflow during additions.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum total reconfiguration cost.
     *
     * The algorithm:
     * 1. Build prefix sums for each of the 3 profiles so segment costs can be queried in O(1).
     * 2. Use dynamic programming over:
     *    - number of formed zones
     *    - covered prefix length
     *    - profile of the last zone
     * 3. Optimize transitions with monotonic deques so each DP layer runs in linear time.
     *
     * @param cost the reconfiguration cost matrix where cost[i][p] is the cost to assign rack i to profile p
     * @param k the exact number of contiguous zones required
     * @param minLen the minimum allowed length of every zone
     * @param maxLen the maximum allowed length of every zone
     * @return the minimum total cost if a valid partition exists; otherwise -1
     * Time complexity: O(k * n)
     * Space complexity: O(n)
     */
    public long minimumCost(int[][] cost, int k, int minLen, int maxLen) {
        int n = cost.length;

        // Quick feasibility check based only on lengths:
        // We need exactly k segments, each of length at least minLen and at most maxLen.
        // Therefore the total covered length n must satisfy:
        // k * minLen <= n <= k * maxLen
        long minPossible = (long) k * minLen;
        long maxPossible = (long) k * maxLen;
        if (n < minPossible || n > maxPossible) {
            return -1;
        }

        // Prefix sums:
        // prefix[p][i] = total cost of assigning racks [0 .. i-1] to profile p
        // This allows segment cost [l .. r] for profile p to be:
        // prefix[p][r+1] - prefix[p][l]
        long[][] prefix = buildPrefixSums(cost);

        // prev[i][p]:
        // minimum cost to cover first i racks using exactly (t-1) zones,
        // with the last zone profile = p
        long[][] prev = new long[n + 1][3];
        long[][] curr = new long[n + 1][3];

        // Initialize all states as impossible.
        fill2D(prev, INF);
        fill2D(curr, INF);

        // Base case for t = 1:
        // One single zone must cover the first i racks, so its length must be in [minLen, maxLen].
        // Since there is no previous zone, any profile is allowed.
        for (int i = minLen; i <= Math.min(maxLen, n); i++) {
            for (int p = 0; p < 3; p++) {
                prev[i][p] = prefix[p][i];
            }
        }

        // If k == 1, answer is simply min over profiles at position n.
        if (k == 1) {
            long ans = min3(prev[n][0], prev[n][1], prev[n][2]);
            return ans >= INF ? -1 : ans;
        }

        // Process DP layers for t = 2..k
        for (int t = 2; t <= k; t++) {
            fill2D(curr, INF);

            // For each profile p, we maintain a deque of candidate start positions s.
            // The deque stores indices s in increasing order, and values:
            // value(s, p) = bestPrev[s][p] - prefix[p][s]
            //
            // where:
            // bestPrev[s][p] = min(prev[s][q]) for q != p
            //
            // Then:
            // curr[i][p] = prefix[p][i] + min value(s, p) over valid starts s
            //
            // Valid starts for a segment ending at i are:
            // s in [i - maxLen, i - minLen]
            //
            // Because this interval slides as i increases, a monotonic deque gives O(1) amortized updates.
            MinDeque[] deques = new MinDeque[3];
            for (int p = 0; p < 3; p++) {
                deques[p] = new MinDeque(n + 5);
            }

            // We iterate i from 0 to n.
            // For each i:
            // 1. Add the new start position s = i - minLen into the valid window.
            // 2. Remove expired starts s < i - maxLen.
            // 3. Query the best candidate for each profile.
            for (int i = 0; i <= n; i++) {
                int addStart = i - minLen;
                if (addStart >= 0) {
                    // This start position now becomes eligible for a segment ending at i.
                    // We compute, for each target profile p, the best previous cost with a different profile.
                    long prevForA = min2(prev[addStart][1], prev[addStart][2]);
                    long prevForB = min2(prev[addStart][0], prev[addStart][2]);
                    long prevForC = min2(prev[addStart][0], prev[addStart][1]);

                    if (prevForA < INF) {
                        long value = prevForA - prefix[0][addStart];
                        deques[0].push(addStart, value);
                    }
                    if (prevForB < INF) {
                        long value = prevForB - prefix[1][addStart];
                        deques[1].push(addStart, value);
                    }
                    if (prevForC < INF) {
                        long value = prevForC - prefix[2][addStart];
                        deques[2].push(addStart, value);
                    }
                }

                int minValidStart = i - maxLen;

                // Remove starts that are too far left and would make the segment longer than maxLen.
                for (int p = 0; p < 3; p++) {
                    deques[p].popWhileIndexLessThan(minValidStart);
                }

                // Now compute curr[i][p] if there is at least one valid start in the deque.
                for (int p = 0; p < 3; p++) {
                    long best = deques[p].minValue();
                    if (best < INF) {
                        curr[i][p] = prefix[p][i] + best;
                    }
                }
            }

            // Move current layer into prev for the next iteration.
            long[][] temp = prev;
            prev = curr;
            curr = temp;
        }

        long answer = min3(prev[n][0], prev[n][1], prev[n][2]);
        return answer >= INF ? -1 : answer;
    }

    /**
     * Builds prefix sums for the 3 profiles.
     *
     * prefix[p][i] stores the total cost of assigning the first i racks
     * (indices 0 through i-1) to profile p.
     *
     * This means the cost of assigning a segment [l, r] to profile p is:
     * prefix[p][r + 1] - prefix[p][l]
     *
     * @param cost the input cost matrix
     * @return a 2D prefix sum array of size [3][n+1]
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[][] buildPrefixSums(int[][] cost) {
        int n = cost.length;
        long[][] prefix = new long[3][n + 1];
        for (int i = 0; i < n; i++) {
            for (int p = 0; p < 3; p++) {
                prefix[p][i + 1] = prefix[p][i] + cost[i][p];
            }
        }
        return prefix;
    }

    /**
     * Fills a 2D long array with the given value.
     *
     * @param array the 2D array to fill
     * @param value the value to write into every cell
     * @return nothing
     * Time complexity: O(rows * cols)
     * Space complexity: O(1) extra
     */
    public void fill2D(long[][] array, long value) {
        for (long[] row : array) {
            Arrays.fill(row, value);
        }
    }

    /**
     * Returns the minimum of two long values.
     *
     * @param a first value
     * @param b second value
     * @return the smaller value
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long min2(long a, long b) {
        return Math.min(a, b);
    }

    /**
     * Returns the minimum of three long values.
     *
     * @param a first value
     * @param b second value
     * @param c third value
     * @return the smallest value
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long min3(long a, long b, long c) {
        return Math.min(a, Math.min(b, c));
    }

    /**
     * Demonstrates the solution on sample inputs.
     *
     * Note:
     * The first example statement contains inconsistent arithmetic in its explanation.
     * The algorithm below computes the true minimum according to the formal problem definition.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size for the demonstrated examples)
     * Space complexity: O(total input size for the demonstrated examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] cost1 = {
            {1, 5, 3},
            {2, 4, 6},
            {7, 1, 2},
            {3, 8, 4},
            {6, 2, 5},
            {4, 3, 1}
        };
        int k1 = 3;
        int minLen1 = 2;
        int maxLen1 = 2;
        System.out.println(solution.minimumCost(cost1, k1, minLen1, maxLen1));

        int[][] cost2 = {
            {3, 1, 9},
            {2, 5, 4},
            {8, 2, 3},
            {6, 1, 7},
            {4, 3, 2}
        };
        int k2 = 2;
        int minLen2 = 3;
        int maxLen2 = 3;
        System.out.println(solution.minimumCost(cost2, k2, minLen2, maxLen2));
    }

    /**
     * A monotonic deque specialized for maintaining the minimum value over a sliding window.
     *
     * We store pairs (index, value).
     * - Indices are pushed in increasing order.
     * - Values are kept in nondecreasing order.
     *
     * Therefore:
     * - The front always contains the minimum value among currently stored candidates.
     * - Expired indices can be removed from the front efficiently.
     */
    static class MinDeque {
        private final int[] indices;
        private final long[] values;
        private int head;
        private int tail;

        /**
         * Creates an empty deque with fixed capacity.
         *
         * @param capacity maximum number of elements that may be stored
         */
        MinDeque(int capacity) {
            this.indices = new int[capacity];
            this.values = new long[capacity];
            this.head = 0;
            this.tail = 0;
        }

        /**
         * Pushes a new candidate (index, value).
         *
         * Before inserting, we remove all elements from the back whose value
         * is greater than or equal to the new value, because they can never
         * become the minimum while the new element remains in the deque.
         *
         * @param index the position associated with this candidate
         * @param value the candidate value
         */
        void push(int index, long value) {
            while (head < tail && values[tail - 1] >= value) {
                tail--;
            }
            indices[tail] = index;
            values[tail] = value;
            tail++;
        }

        /**
         * Removes elements from the front while their index is less than the given threshold.
         *
         * This is used to discard starts that are no longer valid for the current sliding window.
         *
         * @param minIndex the smallest allowed index
         */
        void popWhileIndexLessThan(int minIndex) {
            while (head < tail && indices[head] < minIndex) {
                head++;
            }
        }

        /**
         * Returns the minimum value currently stored in the deque.
         *
         * @return the minimum value, or INF if the deque is empty
         */
        long minValue() {
            return head < tail ? values[head] : INF;
        }
    }
}