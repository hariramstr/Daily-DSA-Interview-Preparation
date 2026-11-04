import java.util.*;

/*
 * Minimum Cost to Reorder a Warehouse Picking List with Shelf Jump Fees
 *
 * Problem Description:
 * A warehouse robot must collect items in the exact order given by a picking list.
 * The warehouse has shelves labeled from 1 to m, and each item type may appear on several shelves.
 * For the i-th request in the list, you are given all shelves that currently contain a valid copy
 * of that requested item. The robot starts before processing the first request and may begin on any
 * shelf that stores the first requested item without paying a movement cost.
 *
 * If the robot picks the i-th item from shelf a and the (i+1)-th item from shelf b, then it pays
 * a movement cost of |a - b|. In addition, changing movement direction is expensive: if the previous
 * move was to a higher-numbered shelf and the next move is to a lower-numbered shelf, or vice versa,
 * an extra turn fee T is paid for that transition. The very first move does not incur a turn fee
 * because there is no previous direction yet. Staying on the same shelf counts as direction 0 and
 * does not itself create a turn; however, after staying still, the next nonzero move is compared
 * against the last nonzero direction used.
 *
 * Your task is to compute the minimum total cost to fulfill the entire picking list.
 *
 * Formally, you are given an array positions where positions[i] is a sorted list of distinct shelf
 * numbers that can satisfy the i-th request, and an integer T. Choose one shelf from each positions[i]
 * so that the sum of movement costs and turn fees is minimized.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5, where n = positions.length
 * - 1 <= total number of shelf entries across all positions[i] <= 2 * 10^5
 * - 1 <= shelf number <= 10^9
 * - 0 <= T <= 10^9
 * - Each positions[i] is non-empty, sorted, and contains distinct integers
 *
 * Notes:
 * - The examples in the prompt contain inconsistent arithmetic in the explanation text.
 *   This implementation follows the formal problem statement exactly.
 * - Under the formal rules, for Example 1 the true minimum is 11.
 * - For Example 2 the minimum is 6.
 */
public class Solution {

    /** A large value used as infinity for long DP values. */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Solves the problem for a list-of-lists input.
     *
     * <p>State idea:
     * For each chosen shelf at the current request, we keep three DP values:
     * <ul>
     *     <li>dir = 0: no nonzero direction has been established yet</li>
     *     <li>dir = 1: last nonzero movement direction was down (-1)</li>
     *     <li>dir = 2: last nonzero movement direction was up (+1)</li>
     * </ul>
     *
     * <p>Transition:
     * Suppose we move from previous shelf p to current shelf x.
     * <ul>
     *     <li>If x == p, movement cost is 0 and the remembered last nonzero direction does not change.</li>
     *     <li>If x > p, movement cost is x - p, new direction becomes up.
     *         If previous remembered direction was down, add turn fee T.</li>
     *     <li>If x < p, movement cost is p - x, new direction becomes down.
     *         If previous remembered direction was up, add turn fee T.</li>
     * </ul>
     *
     * <p>The challenge is to compute transitions between two sorted shelf lists efficiently.
     * A naive all-pairs transition is too slow.
     *
     * <p>For a fixed target shelf x, transitions from previous shelves p split into:
     * <ul>
     *     <li>p <= x : move up or stay</li>
     *     <li>p >= x : move down or stay</li>
     * </ul>
     *
     * <p>For each target x we need:
     * <ul>
     *     <li>Best previous cost among p <= x for producing dir 0 / up</li>
     *     <li>Best previous cost among p >= x for producing dir 0 / down</li>
     * </ul>
     *
     * <p>Because the lists are sorted, these can be maintained by prefix and suffix minima.
     *
     * @param positions positions[i] is the sorted list of shelves that can satisfy request i
     * @param T turn fee paid when the sign of consecutive nonzero moves changes
     * @return minimum total cost to fulfill all requests
     * Time complexity: O(S), where S is the total number of shelf entries across all requests
     * Space complexity: O(M), where M is the maximum size of a single request list
     */
    public long minCost(List<List<Integer>> positions, int T) {
        int n = positions.size();
        if (n == 0) {
            return 0L;
        }

        // Convert the first request into arrays and initialize DP.
        // Starting on any shelf of the first request costs 0 and no direction exists yet.
        int[] prevPos = toIntArray(positions.get(0));
        int mPrev = prevPos.length;

        long[] prevNone = new long[mPrev];
        long[] prevDown = new long[mPrev];
        long[] prevUp = new long[mPrev];

        Arrays.fill(prevNone, 0L);
        Arrays.fill(prevDown, INF);
        Arrays.fill(prevUp, INF);

        // Process each next request one by one.
        for (int i = 1; i < n; i++) {
            int[] curPos = toIntArray(positions.get(i));
            int mCur = curPos.length;

            long[] curNone = new long[mCur];
            long[] curDown = new long[mCur];
            long[] curUp = new long[mCur];
            Arrays.fill(curNone, INF);
            Arrays.fill(curDown, INF);
            Arrays.fill(curUp, INF);

            // -----------------------------------------------------------------
            // Build prefix helper arrays over previous shelves.
            //
            // For transitions where previous shelf p <= current shelf x:
            // - If p < x, the move direction is UP.
            // - If p == x, the move direction is STAY.
            //
            // For each previous index j, define:
            //   bestPrefixNone[j] = min over k in [0..j] of prevNone[k] - prevPos[k]
            //   bestPrefixDown[j] = min over k in [0..j] of (prevDown[k] + T) - prevPos[k]
            //   bestPrefixUp[j]   = min over k in [0..j] of prevUp[k] - prevPos[k]
            //
            // Then for a target x and all p <= x:
            //   moving UP from remembered NONE costs x + bestPrefixNone
            //   moving UP from remembered DOWN costs x + bestPrefixDown
            //   moving UP from remembered UP costs x + bestPrefixUp
            //
            // We also separately handle exact equality p == x for STAY transitions.
            // -----------------------------------------------------------------
            long[] bestPrefixNone = new long[mPrev];
            long[] bestPrefixDown = new long[mPrev];
            long[] bestPrefixUp = new long[mPrev];

            long runNone = INF;
            long runDown = INF;
            long runUp = INF;

            for (int j = 0; j < mPrev; j++) {
                runNone = Math.min(runNone, prevNone[j] - prevPos[j]);
                runDown = Math.min(runDown, prevDown[j] + (long) T - prevPos[j]);
                runUp = Math.min(runUp, prevUp[j] - prevPos[j]);

                bestPrefixNone[j] = runNone;
                bestPrefixDown[j] = runDown;
                bestPrefixUp[j] = runUp;
            }

            // -----------------------------------------------------------------
            // Build suffix helper arrays over previous shelves.
            //
            // For transitions where previous shelf p >= current shelf x:
            // - If p > x, the move direction is DOWN.
            // - If p == x, the move direction is STAY.
            //
            // For each previous index j, define:
            //   bestSuffixNone[j] = min over k in [j..mPrev-1] of prevNone[k] + prevPos[k]
            //   bestSuffixDown[j] = min over k in [j..mPrev-1] of prevDown[k] + prevPos[k]
            //   bestSuffixUp[j]   = min over k in [j..mPrev-1] of prevUp[k] + T + prevPos[k]
            //
            // Then for a target x and all p >= x:
            //   moving DOWN from remembered NONE costs bestSuffixNone - x
            //   moving DOWN from remembered DOWN costs bestSuffixDown - x
            //   moving DOWN from remembered UP costs bestSuffixUp - x
            // -----------------------------------------------------------------
            long[] bestSuffixNone = new long[mPrev];
            long[] bestSuffixDown = new long[mPrev];
            long[] bestSuffixUp = new long[mPrev];

            runNone = INF;
            runDown = INF;
            runUp = INF;

            for (int j = mPrev - 1; j >= 0; j--) {
                runNone = Math.min(runNone, prevNone[j] + prevPos[j]);
                runDown = Math.min(runDown, prevDown[j] + prevPos[j]);
                runUp = Math.min(runUp, prevUp[j] + (long) T + prevPos[j]);

                bestSuffixNone[j] = runNone;
                bestSuffixDown[j] = runDown;
                bestSuffixUp[j] = runUp;
            }

            // -----------------------------------------------------------------
            // Sweep through current shelves and use binary-search-like two pointers
            // because both previous and current shelf arrays are sorted.
            //
            // For each current shelf x:
            // - Let leftCount be number of previous shelves <= x.
            // - Let rightStart be first previous index with shelf >= x.
            //
            // Then:
            //   * prefix data up to leftCount-1 handles all p <= x
            //   * suffix data from rightStart handles all p >= x
            //
            // Exact equality p == x is handled by checking whether such a previous
            // shelf exists and then carrying over the remembered direction unchanged.
            // -----------------------------------------------------------------
            int leftPtr = 0;   // first index with prevPos[leftPtr] > x after advancing
            int eqPtr = 0;     // used to detect equality efficiently

            for (int idx = 0; idx < mCur; idx++) {
                int x = curPos[idx];

                while (leftPtr < mPrev && prevPos[leftPtr] <= x) {
                    leftPtr++;
                }

                while (eqPtr < mPrev && prevPos[eqPtr] < x) {
                    eqPtr++;
                }

                // 1) Transitions from p < x or p <= x contributing to UP / STAY.
                if (leftPtr - 1 >= 0) {
                    int j = leftPtr - 1;

                    // Move UP from remembered NONE / DOWN / UP.
                    curUp[idx] = Math.min(curUp[idx], x + bestPrefixNone[j]);
                    curUp[idx] = Math.min(curUp[idx], x + bestPrefixDown[j]);
                    curUp[idx] = Math.min(curUp[idx], x + bestPrefixUp[j]);
                }

                // 2) Transitions from p > x or p >= x contributing to DOWN / STAY.
                if (leftPtr < mPrev) {
                    int j = leftPtr;

                    // Move DOWN from remembered NONE / DOWN / UP.
                    curDown[idx] = Math.min(curDown[idx], bestSuffixNone[j] - x);
                    curDown[idx] = Math.min(curDown[idx], bestSuffixDown[j] - x);
                    curDown[idx] = Math.min(curDown[idx], bestSuffixUp[j] - x);
                }

                // 3) Exact equality p == x means STAY.
                // Staying keeps the remembered last nonzero direction unchanged.
                if (eqPtr < mPrev && prevPos[eqPtr] == x) {
                    curNone[idx] = Math.min(curNone[idx], prevNone[eqPtr]);
                    curDown[idx] = Math.min(curDown[idx], prevDown[eqPtr]);
                    curUp[idx] = Math.min(curUp[idx], prevUp[eqPtr]);
                }
            }

            // Move current layer into previous layer for the next iteration.
            prevPos = curPos;
            prevNone = curNone;
            prevDown = curDown;
            prevUp = curUp;
            mPrev = mCur;
        }

        // Final answer is the minimum over all ending shelves and remembered directions.
        long ans = INF;
        for (int i = 0; i < mPrev; i++) {
            ans = Math.min(ans, prevNone[i]);
            ans = Math.min(ans, prevDown[i]);
            ans = Math.min(ans, prevUp[i]);
        }
        return ans;
    }

    /**
     * Convenience overload for int[][] input.
     *
     * @param positions positions[i] is the sorted array of shelves for request i
     * @param T turn fee
     * @return minimum total cost
     * Time complexity: O(S), where S is the total number of shelf entries
     * Space complexity: O(M), where M is the maximum row length
     */
    public long minCost(int[][] positions, int T) {
        List<List<Integer>> list = new ArrayList<>(positions.length);
        for (int[] row : positions) {
            List<Integer> cur = new ArrayList<>(row.length);
            for (int x : row) {
                cur.add(x);
            }
            list.add(cur);
        }
        return minCost(list, T);
    }

    /**
     * Converts a List<Integer> into a primitive int[].
     *
     * @param list input list of integers
     * @return primitive array with the same values
     * Time complexity: O(k), where k is list.size()
     * Space complexity: O(k)
     */
    public int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    /**
     * Demonstrates the algorithm on sample inputs.
     *
     * <p>Important:
     * The first sample's explanation in the prompt is internally inconsistent.
     * Under the formal rules, this implementation returns 11 for Example 1.
     * For Example 2 it returns 6.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demo sizes
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int[][] positions1 = {
                {2, 8},
                {3},
                {7, 10},
                {6}
        };
        int T1 = 5;
        long ans1 = sol.minCost(positions1, T1);
        System.out.println("Example 1 result (formal rules): " + ans1);

        int[][] positions2 = {
                {1, 5},
                {4, 6},
                {2, 8},
                {3}
        };
        int T2 = 2;
        long ans2 = sol.minCost(positions2, T2);
        System.out.println("Example 2 result: " + ans2);

        int[][] extra = {
                {5},
                {5},
                {7},
                {7},
                {4}
        };
        int T3 = 3;
        long ans3 = sol.minCost(extra, T3);
        System.out.println("Extra test result: " + ans3);
    }
}