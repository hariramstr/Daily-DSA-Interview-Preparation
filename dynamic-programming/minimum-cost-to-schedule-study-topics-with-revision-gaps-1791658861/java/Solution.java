import java.util.*;

/*
 * Title: Minimum Cost to Schedule Study Topics With Revision Gaps
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are preparing for an exam over the next n days. On day i, you may either study exactly one topic or skip the day.
 * There are m topics, numbered from 0 to m - 1. Studying topic j on day i gives you a learning cost cost[i][j].
 * Lower cost means that topic is easier to study on that day because of your energy, available notes, or class schedule.
 *
 * However, to retain information properly, each topic j has a required revision gap gap[j]. If you study topic j on some day d,
 * then the next time you study the same topic must be at least gap[j] + 1 days later. In other words, if you last studied topic j
 * on day p, then you may study it again on day d only if d - p > gap[j].
 *
 * You are also given an array need where need[j] is the exact number of times topic j must be studied by the end of day n - 1.
 * You may skip any number of days, but all required study sessions must be completed. Return the minimum total learning cost,
 * or -1 if it is impossible.
 *
 * Design an algorithm that works efficiently for small-to-moderate numbers of topics, where the challenge is choosing both when
 * to study and which topic to assign to each day while respecting per-topic spacing constraints.
 *
 * Constraints:
 * - 1 <= n <= 30
 * - 1 <= m <= 5
 * - 0 <= need[j] <= n
 * - 0 <= gap[j] <= n
 * - 1 <= cost[i][j] <= 10^4
 * - Sum of need[j] over all topics is at most n
 *
 * Example 1:
 * Input:
 * n = 5
 * m = 2
 * cost = [[3,8],[2,5],[4,1],[6,3],[2,7]]
 * need = [2,1]
 * gap = [1,0]
 * Output: 5
 *
 * Example 2:
 * Input:
 * n = 4
 * m = 2
 * cost = [[5,2],[4,3],[3,6],[2,1]]
 * need = [2,2]
 * gap = [2,1]
 * Output: -1
 */

public class Solution {

    /**
     * A very large value used as "infinity" for minimization DP.
     * We keep it safely below Integer.MAX_VALUE to avoid overflow when adding costs.
     */
    private static final int INF = 1_000_000_000;

    /**
     * Computes the minimum total learning cost to schedule all required study sessions
     * while respecting per-topic revision gap constraints.
     *
     * Core idea:
     * We process days from left to right using dynamic programming.
     *
     * A DP state must remember:
     * 1) How many times each topic has already been studied.
     * 2) For each topic, how many more days must pass before it becomes available again.
     *
     * Because m <= 5 and n <= 30, this state space is manageable with memoization.
     *
     * State definition:
     * dfs(day, doneCounts, cooldowns) = minimum extra cost from this day onward.
     *
     * Transitions on each day:
     * - Skip the day.
     * - Study one topic j if:
     *   a) we still need more sessions of topic j
     *   b) its cooldown is 0, meaning it is currently allowed
     *
     * After each day:
     * - All positive cooldowns decrease by 1.
     * - If we study topic j today, then after the day ends its cooldown becomes gap[j].
     *   Why gap[j] and not gap[j] + 1?
     *   Because tomorrow is one day later. If gap[j] = 1, then tomorrow is blocked,
     *   and the following day becomes allowed. Representing the remaining blocked future days
     *   as gap[j] after today's action is exactly correct.
     *
     * We also use pruning:
     * - If remaining required sessions exceed remaining days, impossible.
     * - For each topic, if even the earliest possible future placements cannot fit the remaining
     *   required sessions because of cooldown spacing, impossible.
     *
     * @param n total number of days
     * @param m total number of topics
     * @param cost cost[i][j] = cost of studying topic j on day i
     * @param need need[j] = exact number of times topic j must be studied
     * @param gap gap[j] = required gap for topic j
     * @return minimum total cost, or -1 if no valid schedule exists
     * Time complexity note:
     * In the worst case, the number of memoized states is exponential in m and bounded by
     * O(n * product(need[j] + 1) * product(gap[j] + 2)), with up to (m + 1) transitions per state.
     * Given m <= 5 and n <= 30, this is practical.
     * Space complexity note:
     * O(number of memoized states) for the hash map and recursion stack O(n).
     */
    public int minimumCost(int n, int m, int[][] cost, int[] need, int[] gap) {
        // Basic sanity checks for beginner-friendliness and robustness.
        if (n < 0 || m < 0 || cost == null || need == null || gap == null) {
            return -1;
        }
        if (m != need.length || m != gap.length || cost.length != n) {
            return -1;
        }
        for (int i = 0; i < n; i++) {
            if (cost[i] == null || cost[i].length != m) {
                return -1;
            }
        }

        // Quick impossible check:
        // If total required sessions exceed total days, we can immediately return -1.
        int totalNeed = 0;
        for (int x : need) {
            totalNeed += x;
        }
        if (totalNeed > n) {
            return -1;
        }

        // Another quick impossible check per topic:
        // To place need[j] sessions of one topic with gap[j] between consecutive sessions,
        // the minimum number of days needed is:
        // 1 + (need[j] - 1) * (gap[j] + 1)
        // This is only a necessary condition for that topic alone, but still useful.
        for (int j = 0; j < m; j++) {
            if (need[j] > 0) {
                long minSpan = 1L + (long) (need[j] - 1) * (gap[j] + 1L);
                if (minSpan > n) {
                    return -1;
                }
            }
        }

        // Precompute mixed-radix multipliers for encoding "done counts" into one integer.
        // Example:
        // done[0] in [0..need[0]], done[1] in [0..need[1]], ...
        // We encode them into a single integer key.
        int[] doneBase = new int[m];
        int doneStateSize = 1;
        for (int j = 0; j < m; j++) {
            doneBase[j] = doneStateSize;
            doneStateSize *= (need[j] + 1);
        }

        // Precompute mixed-radix multipliers for encoding cooldowns.
        // Cooldown for topic j can be from 0 to gap[j].
        int[] coolBase = new int[m];
        int coolStateSize = 1;
        for (int j = 0; j < m; j++) {
            coolBase[j] = coolStateSize;
            coolStateSize *= (gap[j] + 1);
        }

        // Initial state:
        // - day = 0
        // - done counts all zero
        // - cooldowns all zero (all topics initially available)
        int initialDoneCode = 0;
        int initialCoolCode = 0;

        Map<Long, Integer> memo = new HashMap<>();
        int answer = dfs(0, n, m, cost, need, gap, doneBase, coolBase, initialDoneCode, initialCoolCode, memo);
        return answer >= INF ? -1 : answer;
    }

    /**
     * Recursive memoized DP.
     *
     * @param day current day index
     * @param n total number of days
     * @param m total number of topics
     * @param cost study cost matrix
     * @param need required study counts
     * @param gap required revision gaps
     * @param doneBase mixed-radix multipliers for done-count encoding
     * @param coolBase mixed-radix multipliers for cooldown encoding
     * @param doneCode encoded counts of how many times each topic has already been studied
     * @param coolCode encoded cooldown values for each topic
     * @param memo memoization map
     * @return minimum extra cost from this state, or INF if impossible
     * Time complexity note:
     * Each distinct state is solved once, and each state tries at most m + 1 actions.
     * Space complexity note:
     * O(number of memoized states) plus recursion depth O(n).
     */
    public int dfs(
            int day,
            int n,
            int m,
            int[][] cost,
            int[] need,
            int[] gap,
            int[] doneBase,
            int[] coolBase,
            int doneCode,
            int coolCode,
            Map<Long, Integer> memo
    ) {
        // Build a unique long key from (day, doneCode, coolCode).
        // We use bit packing with safe shifts because state sizes are small here.
        long key = (((long) day) << 40) ^ (((long) doneCode) << 20) ^ (long) coolCode;

        Integer cached = memo.get(key);
        if (cached != null) {
            return cached;
        }

        // Decode the compact state into arrays so we can reason about it clearly.
        int[] done = decode(doneCode, need, doneBase);
        int[] cool = decode(coolCode, gap, coolBase);

        // Count how many sessions are still required overall.
        int remainingSessions = 0;
        for (int j = 0; j < m; j++) {
            remainingSessions += (need[j] - done[j]);
        }

        // Base case:
        // If we have processed all days, the schedule is valid only if nothing remains.
        if (day == n) {
            int result = (remainingSessions == 0) ? 0 : INF;
            memo.put(key, result);
            return result;
        }

        // Pruning 1:
        // If there are not enough days left to place all remaining sessions, impossible.
        int daysLeft = n - day;
        if (remainingSessions > daysLeft) {
            memo.put(key, INF);
            return INF;
        }

        // Pruning 2:
        // For each topic independently, check whether the remaining required sessions can still fit
        // in the remaining timeline considering current cooldown and future spacing.
        //
        // Suppose for topic j:
        // - rem = need[j] - done[j]
        // - current cooldown = cool[j]
        //
        // The earliest possible next study day for this topic is:
        // - today if cool[j] == 0
        // - after cool[j] days otherwise
        //
        // If we need rem sessions, then the earliest finishing pattern occupies:
        // first session at earliest day,
        // then each next session needs (gap[j] + 1) more days.
        //
        // Therefore the last required session would occur after:
        // cool[j] + (rem - 1) * (gap[j] + 1)
        // counted from today.
        //
        // This must be <= daysLeft - 1.
        for (int j = 0; j < m; j++) {
            int rem = need[j] - done[j];
            if (rem == 0) {
                continue;
            }
            long earliestLastOffset = (long) cool[j] + (long) (rem - 1) * (gap[j] + 1L);
            if (earliestLastOffset > daysLeft - 1L) {
                memo.put(key, INF);
                return INF;
            }
        }

        int best = INF;

        // Transition 1: Skip this day.
        //
        // If we skip, then no done count changes.
        // All positive cooldowns decrease by 1 because one day passes.
        int[] nextCoolSkip = advanceCooldowns(cool);
        int nextCoolSkipCode = encode(nextCoolSkip, coolBase);
        best = Math.min(best, dfs(day + 1, n, m, cost, need, gap, doneBase, coolBase, doneCode, nextCoolSkipCode, memo));

        // Transition 2: Study one allowed topic.
        for (int j = 0; j < m; j++) {
            // We can only study topic j if:
            // 1) we still need more sessions of it
            // 2) it is currently available (cooldown == 0)
            if (done[j] < need[j] && cool[j] == 0) {
                // Create next done counts.
                int[] nextDone = done.clone();
                nextDone[j]++;

                // First, one day passes so all cooldowns decrease.
                int[] nextCool = advanceCooldowns(cool);

                // Then, because we studied topic j today, its future blocked days become exactly gap[j].
                nextCool[j] = gap[j];

                int nextDoneCode = encode(nextDone, doneBase);
                int nextCoolCode = encode(nextCool, coolBase);

                int future = dfs(day + 1, n, m, cost, need, gap, doneBase, coolBase, nextDoneCode, nextCoolCode, memo);
                if (future < INF) {
                    best = Math.min(best, cost[day][j] + future);
                }
            }
        }

        memo.put(key, best);
        return best;
    }

    /**
     * Decodes a mixed-radix encoded integer state into an array.
     *
     * For "done counts", the radix for topic j is need[j] + 1.
     * For "cooldowns", the radix for topic j is gap[j] + 1.
     *
     * @param code encoded state
     * @param limits array whose values determine each digit's maximum
     * @param base mixed-radix multipliers
     * @return decoded array representation
     * Time complexity note: O(m)
     * Space complexity note: O(m)
     */
    public int[] decode(int code, int[] limits, int[] base) {
        int m = limits.length;
        int[] arr = new int[m];

        // We extract each digit independently using:
        // digit_j = (code / base[j]) % (limits[j] + 1)
        for (int j = 0; j < m; j++) {
            arr[j] = (code / base[j]) % (limits[j] + 1);
        }
        return arr;
    }

    /**
     * Encodes an array into a mixed-radix integer state.
     *
     * @param arr array to encode
     * @param base mixed-radix multipliers
     * @return encoded integer
     * Time complexity note: O(m)
     * Space complexity note: O(1) excluding input
     */
    public int encode(int[] arr, int[] base) {
        int code = 0;
        for (int j = 0; j < arr.length; j++) {
            code += arr[j] * base[j];
        }
        return code;
    }

    /**
     * Advances cooldowns by one day.
     *
     * If a topic currently has cooldown c > 0, after one day it becomes c - 1.
     * If it is already 0, it stays 0.
     *
     * @param cool current cooldown array
     * @return new cooldown array after one day passes
     * Time complexity note: O(m)
     * Space complexity note: O(m)
     */
    public int[] advanceCooldowns(int[] cool) {
        int[] next = new int[cool.length];
        for (int j = 0; j < cool.length; j++) {
            next[j] = Math.max(0, cool[j] - 1);
        }
        return next;
    }

    /**
     * Convenience overload that infers m from the input arrays.
     *
     * @param n total number of days
     * @param cost cost matrix
     * @param need required study counts
     * @param gap revision gap requirements
     * @return minimum total cost, or -1 if impossible
     * Time complexity note: Same as the main minimumCost method.
     * Space complexity note: Same as the main minimumCost method.
     */
    public int minimumCost(int n, int[][] cost, int[] need, int[] gap) {
        int m = need.length;
        return minimumCost(n, m, cost, need, gap);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 5
     * Example 2 -> -1
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity note: Depends on the sample sizes; negligible here.
     * Space complexity note: Negligible beyond the solver's internal DP.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 5;
        int m1 = 2;
        int[][] cost1 = {
                {3, 8},
                {2, 5},
                {4, 1},
                {6, 3},
                {2, 7}
        };
        int[] need1 = {2, 1};
        int[] gap1 = {1, 0};

        int result1 = solution.minimumCost(n1, m1, cost1, need1, gap1);
        System.out.println(result1); // Expected: 5

        // Example 2
        int n2 = 4;
        int m2 = 2;
        int[][] cost2 = {
                {5, 2},
                {4, 3},
                {3, 6},
                {2, 1}
        };
        int[] need2 = {2, 2};
        int[] gap2 = {2, 1};

        int result2 = solution.minimumCost(n2, m2, cost2, need2, gap2);
        System.out.println(result2); // Expected