import java.util.*;

/*
Problem Title: Maximum Profit from Non-Overlapping Contract Chains

Problem Description:
A consulting company is evaluating a sequence of project contracts over the next N days.
On day i, you may choose to start at most one contract that lasts for duration[i] days and
yields profit[i] if completed. If you start a contract on day i, it occupies days
i through i + duration[i] - 1, and you cannot start another contract during that time.
You may also skip any day.

However, there is an additional business rule: contracts belong to client groups,
represented by group[i]. The company wants to build long-term relationships, so whenever
you accept two consecutive chosen contracts in your schedule, their client groups must be
different. In other words, if the last accepted contract was from group X, the next accepted
contract must be from a group other than X. Skipped days do not reset this rule.

Return the maximum total profit that can be earned.

You are given three arrays of length N: duration, profit, and group. Contract i is available
only if started exactly on day i. A contract that ends after day N cannot be taken.

Constraints:
- 1 <= N <= 2 * 10^5
- 1 <= duration[i] <= N
- 1 <= profit[i] <= 10^9
- 1 <= group[i] <= 2 * 10^5
- Sum of all values fits in 64-bit signed integer
*/

public class Solution {

    /**
     * A very small helper structure that stores:
     * 1) the best DP value seen so far
     * 2) the group that produced that best value
     *
     * Why do we need this?
     * Because when we want to start a new contract of group G, we need the best previous
     * completed schedule whose last chosen contract group is NOT G.
     *
     * If we know the global best and the second global best among all groups, then:
     * - if the global best comes from a different group, we can use it
     * - otherwise we must use the second best
     */
    private static class BestTwo {
        long bestValue1 = 0L;
        int bestGroup1 = -1;

        long bestValue2 = Long.MIN_VALUE;
        int bestGroup2 = -1;

        /**
         * Inserts a candidate pair (value, group) into the "top two distinct groups" structure.
         *
         * If the same group already exists as best1 or best2, we simply keep the larger value.
         * Otherwise, we may need to shift entries to preserve the top two values.
         *
         * @param value candidate DP value
         * @param group candidate last-group
         */
        void add(long value, int group) {
            if (bestGroup1 == group) {
                if (value > bestValue1) {
                    bestValue1 = value;
                }
                return;
            }

            if (bestGroup2 == group) {
                if (value > bestValue2) {
                    bestValue2 = value;
                    if (bestValue2 > bestValue1) {
                        long tv = bestValue1;
                        int tg = bestGroup1;
                        bestValue1 = bestValue2;
                        bestGroup1 = bestGroup2;
                        bestValue2 = tv;
                        bestGroup2 = tg;
                    }
                }
                return;
            }

            if (value > bestValue1) {
                bestValue2 = bestValue1;
                bestGroup2 = bestGroup1;
                bestValue1 = value;
                bestGroup1 = group;
            } else if (value > bestValue2) {
                bestValue2 = value;
                bestGroup2 = group;
            }
        }

        /**
         * Returns the best value among schedules whose last group is NOT forbiddenGroup.
         *
         * Important detail:
         * The empty schedule is always allowed and has value 0.
         * So even if no previous contract exists, we can start a new chain with profit[i].
         *
         * @param forbiddenGroup the group we are not allowed to continue from
         * @return best previous value with last-group != forbiddenGroup
         */
        long bestExcluding(int forbiddenGroup) {
            if (bestGroup1 != forbiddenGroup) {
                return bestValue1;
            }
            return Math.max(0L, bestValue2);
        }
    }

    /**
     * Computes the maximum total profit under:
     * 1) non-overlapping contracts
     * 2) consecutive chosen contracts must have different groups
     *
     * Core DP idea:
     * Let dp[i] be the best profit of any valid schedule that ends by taking contract i as the last chosen contract.
     *
     * If contract i ends at day end = i + duration[i], then any previous chosen contract must end on or before day i.
     * Among all schedules available by day i, we need the best one whose last group != group[i].
     *
     * To support this efficiently:
     * - We sweep days from left to right.
     * - We maintain all DP states of contracts that have already finished and are therefore available
     *   as predecessors for future contracts.
     * - Among those available states, we only need the best and second-best values by distinct last-group.
     *
     * This avoids an O(N^2) transition and gives an O(N) overall solution.
     *
     * Detailed sweep logic:
     * - "availableAtDay[d]" stores all completed contract states that become usable starting on day d.
     *   A contract started at i with duration t occupies days [i, i+t-1], so it finishes just before day i+t.
     *   Therefore it becomes available as a predecessor for contracts starting on day i+t.
     * - Before processing day i, we activate all states in availableAtDay[i].
     * - Then, if contract i fits within N days, we compute:
     *       dp[i] = profit[i] + bestPreviousValueExcluding(group[i])
     * - Finally, we schedule this dp[i] to become available at day i + duration[i].
     *
     * @param duration duration[i] is the number of days occupied by contract i
     * @param profit profit[i] is the profit earned by completing contract i
     * @param group group[i] is the client group of contract i
     * @return maximum total profit achievable
     *
     * Time complexity: O(N)
     * Space complexity: O(N)
     */
    public long maxProfit(int[] duration, int[] profit, int[] group) {
        int n = duration.length;

        /*
         * availableAtDay[d] contains a list of DP states that become available exactly when we reach day d.
         *
         * Each state is represented as a long[2]:
         *   state[0] = dp value
         *   state[1] = group
         *
         * We use ArrayList for each day because multiple contracts can finish and become available on the same day.
         */
        @SuppressWarnings("unchecked")
        List<long[]>[] availableAtDay = new ArrayList[n + 1];
        for (int i = 0; i <= n; i++) {
            availableAtDay[i] = new ArrayList<>();
        }

        /*
         * bestFinished tracks the best completed schedules that are already eligible to be extended
         * by a contract starting on the current day.
         */
        BestTwo bestFinished = new BestTwo();

        long answer = 0L;

        /*
         * Sweep through each day in chronological order.
         */
        for (int day = 0; day < n; day++) {

            /*
             * Step 1: Activate all contracts that have fully finished before this day starts.
             *
             * If a contract becomes available at this day, it means it ended on the previous day,
             * so it can now serve as the predecessor of a contract starting today.
             */
            for (long[] state : availableAtDay[day]) {
                long value = state[0];
                int g = (int) state[1];
                bestFinished.add(value, g);
            }

            /*
             * Step 2: Check whether the contract starting today is valid.
             *
             * A contract starting at 'day' with duration 'duration[day]' occupies:
             *   day, day+1, ..., day+duration[day]-1
             *
             * Therefore it is valid only if:
             *   day + duration[day] <= n
             *
             * because the first day after it finishes is exactly day + duration[day].
             */
            int nextAvailableDay = day + duration[day];
            if (nextAvailableDay <= n) {

                /*
                 * Step 3: Find the best previous completed schedule whose last group differs from group[day].
                 *
                 * If no previous contract is chosen, the empty schedule contributes 0.
                 */
                long previousBest = bestFinished.bestExcluding(group[day]);

                /*
                 * Step 4: Build the DP value for taking today's contract as the last chosen contract.
                 */
                long currentDp = previousBest + profit[day];

                /*
                 * Step 5: This newly formed schedule becomes available to future contracts
                 * starting on 'nextAvailableDay'.
                 */
                availableAtDay[nextAvailableDay].add(new long[]{currentDp, group[day]});

                /*
                 * Step 6: Update the global answer.
                 */
                if (currentDp > answer) {
                    answer = currentDp;
                }
            }
        }

        return answer;
    }

    /**
     * Convenience wrapper that accepts lists, useful for demonstrations or interview-style testing.
     *
     * @param durationList list of durations
     * @param profitList list of profits
     * @param groupList list of groups
     * @return maximum total profit achievable
     *
     * Time complexity: O(N)
     * Space complexity: O(N)
     */
    public long maxProfit(List<Integer> durationList, List<Integer> profitList, List<Integer> groupList) {
        int n = durationList.size();
        int[] duration = new int[n];
        int[] profit = new int[n];
        int[] group = new int[n];

        for (int i = 0; i < n; i++) {
            duration[i] = durationList.get(i);
            profit[i] = profitList.get(i);
            group[i] = groupList.get(i);
        }

        return maxProfit(duration, profit, group);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The textual examples in the prompt contain inconsistencies in their stated outputs.
     * This main method prints the mathematically correct results produced by the algorithm.
     *
     * Example 1:
     * duration = [2,1,2,1]
     * profit   = [50,10,40,70]
     * group    = [1,1,2,1]
     *
     * Valid optimal schedule:
     * - take contract 0: days 0-1, group 1, profit 50
     * - take contract 3: day 3, group 1, profit 70  -> invalid because same consecutive group
     * - take contract 0 then 2 is impossible because 0 occupies days 0-1 and 2 occupies days 2-3, actually non-overlapping,
     *   so total 90 is valid with groups 1 -> 2
     * - take contract 1 then 3 is invalid because groups 1 -> 1
     * Best valid answer is 90.
     *
     * Example 2:
     * duration = [1,2,1,1,2]
     * profit   = [8,20,7,15,30]
     * group    = [1,2,1,3,2]
     *
     * The optimal valid schedule is:
     * - contract 0 (group 1, profit 8)
     * - contract 1 (group 2, profit 20)
     * - contract 3 (group 3, profit 15)
     * Total = 43
     *
     * Contract 4 starts on day 4 and lasts 2 days, so it ends after day N=5 and is invalid.
     *
     * @param args command-line arguments, unused
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] duration1 = {2, 1, 2, 1};
        int[] profit1 = {50, 10, 40, 70};
        int[] group1 = {1, 1, 2, 1};
        System.out.println(solution.maxProfit(duration1, profit1, group1)); // Correct result: 90

        int[] duration2 = {1, 2, 1, 1, 2};
        int[] profit2 = {8, 20, 7, 15, 30};
        int[] group2 = {1, 2, 1, 3, 2};
        System.out.println(solution.maxProfit(duration2, profit2, group2)); // Correct result: 43

        List<Integer> duration3 = Arrays.asList(1, 1, 1, 1);
        List<Integer> profit3 = Arrays.asList(5, 6, 7, 8);
        List<Integer> group3 = Arrays.asList(1, 2, 1, 2);
        System.out.println(solution.maxProfit(duration3, profit3, group3)); // 26
    }
}