import java.util.*;

/*
 * Title: Minimum Cost to Schedule Study Topics with Revision Gaps
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are preparing a study plan for an exam over n days. On day i, you must study exactly one topic
 * chosen from m possible topics. The cost of studying topic j on day i is given by costs[i][j].
 * However, repeatedly studying the same topic too soon is mentally exhausting, so a topic can only
 * be chosen again if at least gap[j] full days have passed since the last day that same topic was studied.
 *
 * More formally, if topic j is studied on day a and again on day b where a < b, then b - a - 1
 * must be at least gap[j]. Equivalently, topic j cannot be used again within the next gap[j] days
 * after it is chosen.
 *
 * Return the minimum total cost to complete all n days, or -1 if it is impossible to build a valid schedule.
 *
 * This is a dynamic programming problem because the best choice for the current day depends on which
 * topics were used recently and when they become available again. A correct solution should efficiently
 * explore valid schedules without brute-forcing all possible sequences.
 *
 * Constraints:
 * - 1 <= n <= 100
 * - 1 <= m <= 8
 * - costs.length == n
 * - costs[i].length == m
 * - 1 <= costs[i][j] <= 10^4
 * - 0 <= gap[j] <= 7
 *
 * Example 1:
 * Input: n = 4, costs = [[3,8],[5,2],[6,4],[1,7]], gap = [1,0]
 * Output: 10
 *
 * Example 2:
 * Input: n = 3, costs = [[4,1],[2,3],[5,6]], gap = [2,2]
 * Output: -1
 */

public class Solution {

    /**
     * A large value used to represent an unreachable / impossible DP state.
     * We keep it safely below Long.MAX_VALUE so additions do not overflow.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum total cost to schedule exactly one topic per day while respecting
     * each topic's revision gap constraint.
     *
     * Core idea:
     * We process days from left to right using dynamic programming.
     *
     * For each topic, we track a small "cooldown" value:
     * - 0 means the topic is available today.
     * - positive value x means the topic is blocked for x more days.
     *
     * Because each gap[j] <= 7 and m <= 8, the full state space is manageable:
     * each topic has at most gap[j] + 1 possible cooldown values, so the total number
     * of states is product(gap[j] + 1), which is at most 8^8 = 16,777,216 in the absolute
     * theoretical worst case, but typically much smaller. We only store states that are
     * actually reachable using hash maps.
     *
     * Transition for a chosen topic t on a day:
     * 1. Topic t must currently have cooldown 0 (available).
     * 2. After using topic t today:
     *    - its cooldown becomes gap[t] for the next day
     *    - every other topic's cooldown decreases by 1 if it was positive
     *
     * This exactly models "cannot be used again within the next gap[t] days".
     *
     * @param costs costs[i][j] is the cost of studying topic j on day i
     * @param gap gap[j] is the number of full days topic j must wait before reuse
     * @return the minimum total cost, or -1 if no valid schedule exists
     * Time complexity note:
     * Let S be the number of reachable cooldown states. For each day, for each reachable state,
     * we try up to m topics, and each transition updates m cooldown values.
     * Therefore the time complexity is O(n * S * m * m), which is practical here because m <= 8.
     * Space complexity note:
     * O(S) for the current and next DP maps.
     */
    public long minimumCost(int[][] costs, int[] gap) {
        validateInput(costs, gap);

        int n = costs.length;
        int m = costs[0].length;

        /*
         * We encode each cooldown vector into a single long key.
         *
         * Why encoding works:
         * - Topic j cooldown ranges from 0 to gap[j].
         * - So we can treat the full vector like a mixed-radix number.
         * - base[j] = gap[j] + 1
         *
         * Example:
         * If gap = [1, 0, 2], then cooldown ranges are:
         * topic 0: 0..1  => base 2
         * topic 1: 0..0  => base 1
         * topic 2: 0..2  => base 3
         *
         * A state [c0, c1, c2] can be encoded uniquely.
         */
        int[] base = new int[m];
        long[] multiplier = new long[m];
        multiplier[0] = 1L;
        for (int j = 0; j < m; j++) {
            base[j] = gap[j] + 1;
            if (j > 0) {
                multiplier[j] = multiplier[j - 1] * base[j - 1];
            }
        }

        /*
         * Initial state before day 0:
         * Every topic is available, so every cooldown is 0.
         * Encoded key for all zeros is simply 0.
         */
        Map<Long, Long> dp = new HashMap<>();
        dp.put(0L, 0L);

        /*
         * Process each day one by one.
         */
        for (int day = 0; day < n; day++) {
            Map<Long, Long> nextDp = new HashMap<>();

            /*
             * For every reachable cooldown configuration before this day,
             * try choosing every topic that is currently available.
             */
            for (Map.Entry<Long, Long> entry : dp.entrySet()) {
                long stateKey = entry.getKey();
                long currentCost = entry.getValue();

                /*
                 * Decode the current state's cooldowns into an array so we can inspect and update them.
                 */
                int[] cooldown = decodeState(stateKey, base, multiplier);

                /*
                 * Try each topic as today's choice.
                 */
                for (int topic = 0; topic < m; topic++) {
                    /*
                     * A topic can be chosen today only if its cooldown is 0,
                     * meaning it is currently available.
                     */
                    if (cooldown[topic] != 0) {
                        continue;
                    }

                    /*
                     * Build the next day's cooldown vector after choosing this topic today.
                     *
                     * Step-by-step:
                     * 1. Every blocked topic gets one day closer to becoming available.
                     *    So positive cooldowns decrease by 1.
                     * 2. The chosen topic becomes blocked for exactly gap[topic] days.
                     *
                     * Important subtlety:
                     * We first conceptually move from "before today" to "before tomorrow".
                     * That means all existing cooldowns tick down by one day,
                     * then the chosen topic is reset to its full gap.
                     */
                    int[] nextCooldown = new int[m];
                    for (int j = 0; j < m; j++) {
                        if (cooldown[j] > 0) {
                            nextCooldown[j] = cooldown[j] - 1;
                        } else {
                            nextCooldown[j] = 0;
                        }
                    }
                    nextCooldown[topic] = gap[topic];

                    long nextKey = encodeState(nextCooldown, multiplier);
                    long nextCost = currentCost + costs[day][topic];

                    /*
                     * Standard DP relaxation:
                     * keep only the minimum cost for each resulting state.
                     */
                    long bestKnown = nextDp.getOrDefault(nextKey, INF);
                    if (nextCost < bestKnown) {
                        nextDp.put(nextKey, nextCost);
                    }
                }
            }

            /*
             * Move to the next day.
             * If no states are reachable, scheduling is impossible.
             */
            dp = nextDp;
            if (dp.isEmpty()) {
                return -1L;
            }
        }

        /*
         * After scheduling all days, any remaining reachable state is acceptable.
         * We simply need the minimum total cost among them.
         */
        long answer = INF;
        for (long totalCost : dp.values()) {
            answer = Math.min(answer, totalCost);
        }

        return answer >= INF ? -1L : answer;
    }

    /**
     * Encodes a cooldown vector into a single long using mixed-radix representation.
     *
     * @param cooldown cooldown[j] is the current cooldown of topic j
     * @param multiplier multiplier[j] is the positional multiplier for topic j
     * @return encoded state key
     * Time complexity note: O(m)
     * Space complexity note: O(1) extra space
     */
    public long encodeState(int[] cooldown, long[] multiplier) {
        long key = 0L;
        for (int j = 0; j < cooldown.length; j++) {
            key += (long) cooldown[j] * multiplier[j];
        }
        return key;
    }

    /**
     * Decodes an encoded state key back into the cooldown vector.
     *
     * @param key encoded state
     * @param base base[j] = gap[j] + 1, i.e. number of possible cooldown values for topic j
     * @param multiplier positional multipliers used during encoding
     * @return decoded cooldown array
     * Time complexity note: O(m)
     * Space complexity note: O(m)
     */
    public int[] decodeState(long key, int[] base, long[] multiplier) {
        int m = base.length;
        int[] cooldown = new int[m];

        /*
         * Because this is mixed-radix encoding, digit j is:
         * (key / multiplier[j]) % base[j]
         */
        for (int j = 0; j < m; j++) {
            cooldown[j] = (int) ((key / multiplier[j]) % base[j]);
        }

        return cooldown;
    }

    /**
     * Validates the input arrays according to the problem constraints.
     *
     * @param costs cost matrix
     * @param gap gap array
     * @return nothing; throws IllegalArgumentException if input is invalid
     * Time complexity note: O(n * m)
     * Space complexity note: O(1)
     */
    public void validateInput(int[][] costs, int[] gap) {
        if (costs == null || costs.length == 0) {
            throw new IllegalArgumentException("costs must be non-null and contain at least one day.");
        }
        if (gap == null || gap.length == 0) {
            throw new IllegalArgumentException("gap must be non-null and contain at least one topic.");
        }

        int n = costs.length;
        int m = gap.length;

        for (int i = 0; i < n; i++) {
            if (costs[i] == null || costs[i].length != m) {
                throw new IllegalArgumentException("Each row of costs must have exactly gap.length columns.");
            }
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Note:
     * The first example's written explanation in the prompt contains an inconsistency:
     * it claims the schedule [topic 0, topic 1, topic 1, topic 0] has total cost 10,
     * but with the provided matrix [[3,8],[5,2],[6,4],[1,7]], that schedule actually costs:
     * 3 + 2 + 4 + 1 = 10 only if day 3 topic 0 costs 1 and day 2 topic 1 costs 4, which matches.
     * Also, the schedule is valid because topic 1 has gap 0 and may repeat immediately,
     * while topic 0 has gap 1 and is repeated with one full day in between.
     *
     * The algorithm below correctly returns:
     * - Example 1: 10
     * - Example 2: -1
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity note: dominated by the calls to minimumCost
     * Space complexity note: dominated by the calls to minimumCost
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] costs1 = {
            {3, 8},
            {5, 2},
            {6, 4},
            {1, 7}
        };
        int[] gap1 = {1, 0};
        long result1 = solution.minimumCost(costs1, gap1);
        System.out.println("Example 1 Output: " + result1); // Expected: 10

        int[][] costs2 = {
            {4, 1},
            {2, 3},
            {5, 6}
        };
        int[] gap2 = {2, 2};
        long result2 = solution.minimumCost(costs2, gap2);
        System.out.println("Example 2 Output: " + result2); // Expected: -1
    }
}