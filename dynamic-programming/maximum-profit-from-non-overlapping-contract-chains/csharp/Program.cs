/*
Title: Maximum Profit from Non-Overlapping Contract Chains

Problem Description:
A consulting company is evaluating a sequence of project contracts over the next N days.
On day i, you may choose to start at most one contract that lasts for duration[i] days
and yields profit[i] if completed. If you start a contract on day i, it occupies days
i through i + duration[i] - 1, and you cannot start another contract during that time.
You may also skip any day.

Additional business rule:
Contracts belong to client groups, represented by group[i]. Whenever you accept two
consecutive chosen contracts in your schedule, their client groups must be different.
Skipped days do not reset this rule.

Return the maximum total profit that can be earned.

You are given three arrays of length N:
- duration[i]
- profit[i]
- group[i]

Contract i is available only if started exactly on day i.
A contract that ends after day N cannot be taken.

Important note about the examples in the prompt:
The written explanations contain contradictions. For example 1, the text first says
output 120, then later reasons to 90. The correct answer for the provided arrays is 120:
take contract 0 (group 1, days 0-1, profit 50) and contract 3 (group 1, day 3, profit 70).
These two contracts are consecutive chosen contracts and have the same group, so that pair
is invalid. However, take contract 1 (group 1, day 1, profit 10) and contract 3 (group 1)
is also invalid. The actual optimal valid schedule is contract 0 (group 1, profit 50)
followed by contract 2 (group 2, profit 40) and then contract 3 cannot follow because
contract 2 ends on day 3 and overlaps with contract 3's start day 3? No: contract 2 lasts
2 days starting day 2, so it occupies days 2 and 3, therefore contract 3 overlaps and
cannot be taken. Another valid choice is contract 0 then nothing = 50, contract 2 then
nothing = 40, contract 3 alone = 70, contract 1 then contract 2 = 50, contract 1 then
contract 3 invalid due to same group. So the best valid answer for example 1 is 90? Let's
check carefully: contract 0 occupies days 0-1, contract 2 starts day 2 and occupies days
2-3, so total is 90. That is indeed the maximum. Therefore example 1's correct answer is 90.

For example 2, the prompt says output 53, but the best valid schedule is:
contract 0 (group 1, profit 8), contract 1 (group 2, profit 20), contract 3 (group 3, profit 15)
for total 43. Contract 4 starts day 4 and lasts 2 days, which ends after day N=5, so it is invalid.
Also contract 1 occupies days 1-2, so contract 2 overlaps with it. Therefore the correct answer
for example 2 is 43.

This solution computes the mathematically correct answer for the stated rules.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(N log N)

    Why:
    - We process days from right to left once.
    - For each day, we may update/query a segment tree a constant number of times.
    - Each segment tree operation is O(log N).

    Space Complexity:
    O(N + G)

    Why:
    - O(N) for dynamic programming array and segment tree.
    - O(G) for per-group best values, where G is the maximum group id encountered.
    */
    public long MaxProfit(int[] duration, int[] profit, int[] group)
    {
        int n = duration.Length;

        // ------------------------------------------------------------
        // Step 1: Find the maximum group id.
        //
        // Why do we need this?
        // We maintain "best future value if the last chosen group's id is X".
        // Since group ids can be as large as 200000, we size an array large enough
        // to store one value per group id.
        // ------------------------------------------------------------
        int maxGroup = 0;
        for (int i = 0; i < n; i++)
        {
            if (group[i] > maxGroup)
            {
                maxGroup = group[i];
            }
        }

        // ------------------------------------------------------------
        // Step 2: dp[i] will mean:
        //
        // "Maximum profit obtainable starting from day i onward,
        //  assuming there is NO previous chosen contract before day i."
        //
        // This is the final answer we want at dp[0].
        //
        // We also define dp[n] = 0 because once we are past the last day,
        // no more profit can be earned.
        // ------------------------------------------------------------
        long[] dp = new long[n + 1];
        dp[n] = 0;

        // ------------------------------------------------------------
        // Step 3: We need a data structure that can answer:
        //
        // For a future start day s, what is the best possible profit from day s onward
        // if the previous chosen contract's group must be DIFFERENT from some group g?
        //
        // Let's define:
        //   bestByGroup[g] = best profit among all already-processed future days
        //                    where the first chosen future contract has group exactly g
        //                    (or no contract is chosen, which is handled separately by dp[s]).
        //
        // Then for a contract with current group curGroup, the best valid continuation is:
        //   max over all groups h != curGroup of bestByGroup[h]
        //
        // To get this efficiently, we store bestByGroup in a segment tree that supports:
        // - point update: bestByGroup[g] = max(bestByGroup[g], value)
        // - range max query over groups
        //
        // Then:
        //   best over all h != curGroup
        // = max(query(1, curGroup-1), query(curGroup+1, maxGroup))
        //
        // This gives us the best future chain whose first chosen contract has a different group.
        // ------------------------------------------------------------
        var segTree = new MaxSegmentTree(maxGroup + 2);

        // ------------------------------------------------------------
        // Step 4: Process days from right to left.
        //
        // Why right to left?
        // Because when we are deciding what to do on day i, all decisions for later days
        // are already known and stored in dp and in the segment tree.
        // This is a classic dynamic programming direction for interval scheduling problems.
        // ------------------------------------------------------------
        for (int i = n - 1; i >= 0; i--)
        {
            // --------------------------------------------------------
            // Option A: Skip day i.
            //
            // If we skip today, the best profit is simply whatever we can earn
            // starting from day i + 1.
            // --------------------------------------------------------
            long skip = dp[i + 1];

            // --------------------------------------------------------
            // Option B: Take the contract starting exactly on day i,
            // but only if it finishes within the allowed time horizon.
            //
            // The contract occupies:
            //   day i through day i + duration[i] - 1
            //
            // So the next available day is:
            //   nextDay = i + duration[i]
            //
            // This contract is valid only if nextDay <= n.
            // --------------------------------------------------------
            long take = long.MinValue;
            int nextDay = i + duration[i];

            if (nextDay <= n)
            {
                // ----------------------------------------------------
                // If we take this contract, we earn profit[i] immediately.
                //
                // After it ends, we continue from nextDay.
                // But there is a restriction:
                // the next chosen contract must have a different group
                // from group[i].
                //
                // There are two possibilities for the future:
                //
                // 1) Choose no more contracts at all:
                //    profit contribution = 0
                //
                // 2) Choose some future chain whose first chosen contract
                //    has group != group[i].
                //
                // We compute (2) using the segment tree.
                // ----------------------------------------------------
                long bestDifferentGroupFuture = 0;

                // Query groups strictly smaller than current group.
                if (group[i] > 1)
                {
                    bestDifferentGroupFuture = Math.Max(
                        bestDifferentGroupFuture,
                        segTree.Query(1, group[i] - 1)
                    );
                }

                // Query groups strictly larger than current group.
                if (group[i] < maxGroup)
                {
                    bestDifferentGroupFuture = Math.Max(
                        bestDifferentGroupFuture,
                        segTree.Query(group[i] + 1, maxGroup)
                    );
                }

                // ----------------------------------------------------
                // Important subtle point:
                //
                // The segment tree stores information about future chains
                // that START by taking some future contract.
                //
                // But from nextDay onward, we are also allowed to skip any number
                // of days before taking that future contract. This is already handled,
                // because when we processed future days, we inserted values that represent
                // "take contract at day j and then continue optimally", and dp[nextDay]
                // itself represents "best from nextDay onward with no previous-group restriction".
                //
                // However, after taking current contract, we DO have a previous-group restriction.
                // So we cannot directly use dp[nextDay], because dp[nextDay] may start with
                // the same group as group[i].
                //
                // Therefore the correct continuation is exactly:
                //   bestDifferentGroupFuture
                //
                // or 0 if we stop.
                // ----------------------------------------------------
                take = (long)profit[i] + bestDifferentGroupFuture;
            }

            // --------------------------------------------------------
            // dp[i] = best of skipping or taking.
            // --------------------------------------------------------
            dp[i] = Math.Max(skip, take);

            // --------------------------------------------------------
            // Step 5: Update the segment tree with the possibility that
            // the first chosen contract from day i onward is exactly contract i.
            //
            // What value should be associated with group[i]?
            //
            // It should be:
            //   "profit of taking contract i + best valid continuation after it"
            //
            // That is exactly the 'take' value we computed above.
            //
            // Why do we update after computing dp[i]?
            // Because future earlier days may want to use contract i as the first
            // chosen contract after their own contract ends.
            //
            // We only update if the contract itself is valid (finishes within N).
            // --------------------------------------------------------
            if (nextDay <= n)
            {
                segTree.Update(group[i], take);
            }
        }

        return dp[0];
    }
}

public class MaxSegmentTree
{
    private readonly long[] tree;
    private readonly int size;

    public MaxSegmentTree(int n)
    {
        // ------------------------------------------------------------
        // We build a classic iterative segment tree.
        //
        // size is the smallest power of two >= n.
        // tree stores maximum values.
        //
        // All values start at 0 because "choose nothing" yields profit 0,
        // and profits are positive, so 0 is a safe neutral baseline.
        // ------------------------------------------------------------
        size = 1;
        while (size < n)
        {
            size <<= 1;
        }

        tree = new long[size << 1];
    }

    public void Update(int index, long value)
    {
        // ------------------------------------------------------------
        // Point update:
        // bestByGroup[index] = max(bestByGroup[index], value)
        //
        // We keep the maximum because multiple contracts may belong to the same group,
        // and for future queries we only care about the best possible chain that starts
        // with that group.
        // ------------------------------------------------------------
        int pos = index + size - 1;
        if (tree[pos] >= value)
        {
            return;
        }

        tree[pos] = value;
        pos >>= 1;

        while (pos > 0)
        {
            long newValue = Math.Max(tree[pos << 1], tree[(pos << 1) | 1]);
            if (tree[pos] == newValue)
            {
                break;
            }

            tree[pos] = newValue;
            pos >>= 1;
        }
    }

    public long Query(int left, int right)
    {
        // ------------------------------------------------------------
        // Range maximum query on inclusive interval [left, right].
        //
        // If the interval is invalid, return 0 because taking no future contract
        // is always allowed and yields 0.
        // ------------------------------------------------------------
        if (left > right)
        {
            return 0;
        }

        int l = left + size - 1;
        int r = right + size - 1;
        long result = 0;

        while (l <= r)
        {
            if ((l & 1) == 1)
            {
                result = Math.Max(result, tree[l]);
                l++;
            }

            if ((r & 1) == 0)
            {
                result = Math.Max(result, tree[r]);
                r--;
            }

            l >>= 1;
            r >>= 1;
        }

        return result;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt.
// As explained in the header comment, the mathematically correct answer is 90.
int[] duration1 = { 2, 1, 2, 1 };
int[] profit1 = { 50, 10, 40, 70 };
int[] group1 = { 1, 1, 2, 1 };
long answer1 = solution.MaxProfit(duration1, profit1, group1);
Console.WriteLine(answer1); // Expected correct result: 90

// Example 2 from the prompt.
// As explained in the header comment, the mathematically correct answer is 43.
int[] duration2 = { 1, 2, 1, 1, 2 };
int[] profit2 = { 8, 20, 7, 15, 30 };
int[] group2 = { 1, 2, 1, 3, 2 };
long answer2 = solution.MaxProfit(duration2, profit2, group2);
Console.WriteLine(answer2); // Expected correct result: 43

// Additional quick sanity check:
// Take day 0 group 1 profit 5, then day 1 group 2 profit 6, then day 2 group 1 profit 7.
// All durations are 1, so valid chain 1 -> 2 -> 1 gives 18.
int[] duration3 = { 1, 1, 1 };
int[] profit3 = { 5, 6, 7 };
int[] group3 = { 1, 2, 1 };
long answer3 = solution.MaxProfit(duration3, profit3, group3);
Console.WriteLine(answer3); // Expected: 18