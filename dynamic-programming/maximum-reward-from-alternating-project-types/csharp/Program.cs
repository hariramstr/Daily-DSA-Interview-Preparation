/*
Title: Maximum Reward from Alternating Project Types

Problem Description:
A consulting firm has a backlog of projects to complete over the next several weeks. Each project belongs to one of two types: type A or type B. You are given two arrays, aReward and bReward, both of length n. On week i (0-indexed), the firm may choose exactly one of the following options: complete the type A project for reward aReward[i], complete the type B project for reward bReward[i], or skip the week and earn 0 reward.

However, the firm wants to avoid doing the same type of project in consecutive worked weeks because that causes team fatigue. This means if the last non-skipped week used type A, then the next worked week must use type B, and vice versa. Skipped weeks do not reset the rule; they simply mean no project was done that week.

Return the maximum total reward the firm can earn.

Constraints:
- 1 <= n <= 200000
- 0 <= aReward[i], bReward[i] <= 10^9
- The answer fits in a 64-bit signed integer.

Examples:
1)
aReward = [5, 1, 7, 4]
bReward = [2, 10, 3, 8]
Output: 30

2)
aReward = [9, 1, 1, 9]
bReward = [1, 8, 8, 1]
The mathematically correct optimum under the stated rules is 26:
- A on week 0 => 9
- B on week 1 => 8
- skip week 2
- A on week 3 => 9
Total = 26
So the example text in the prompt is inconsistent, but the algorithm below correctly follows the stated rules.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We only scan the arrays once, and for each week we update a constant number of DP states.
    This is efficient enough for n up to 200,000.
    */
    public long MaxReward(int[] aReward, int[] bReward)
    {
        if (aReward == null || bReward == null || aReward.Length != bReward.Length)
        {
            throw new ArgumentException("Input arrays must be non-null and have the same length.");
        }

        int n = aReward.Length;

        // We maintain three dynamic programming states while scanning from left to right.
        //
        // 1) none:
        //    The best total reward we can have after processing the current week
        //    if we choose to have done no worked weeks at all so far.
        //
        //    Because all rewards are non-negative, this state will remain 0.
        //    We keep it explicitly because it makes the transitions easier to understand:
        //    if we have never worked before, then we are allowed to start with either A or B.
        //
        // 2) endWithA:
        //    The best total reward after processing the current week such that
        //    the last non-skipped worked week used project type A.
        //
        //    Important:
        //    This does NOT mean we necessarily worked this exact week.
        //    We may have skipped some recent weeks, but the most recent worked week
        //    among all processed weeks is still type A.
        //
        // 3) endWithB:
        //    Symmetric meaning: the best total reward after processing the current week
        //    such that the last non-skipped worked week used project type B.
        //
        // Why these states are enough:
        // The rule only depends on the type of the last worked week.
        // It does not depend on how many skips happened in between.
        // Therefore, remembering only the last worked type is sufficient.
        long none = 0;
        long endWithA = long.MinValue / 4;
        long endWithB = long.MinValue / 4;

        for (int i = 0; i < n; i++)
        {
            // Before updating for week i, store the old values.
            // This is necessary because all transitions for the current week
            // must be based on the states from the previous week, not partially updated ones.
            long prevNone = none;
            long prevEndWithA = endWithA;
            long prevEndWithB = endWithB;

            // Option 1: Skip this week.
            //
            // If we skip, the "last worked type" does not change.
            // So:
            // - none stays none
            // - endWithA can remain endWithA
            // - endWithB can remain endWithB
            //
            // Since none is always 0 and skipping never creates a better "none" than 0,
            // we can simply keep it unchanged.
            long newNone = prevNone;

            // Option 2: Work on type A this week.
            //
            // We are allowed to do A only if:
            // - we have never worked before (prevNone), or
            // - the previous last worked type was B (prevEndWithB)
            //
            // We are NOT allowed to come from prevEndWithA because that would place
            // A after A in consecutive worked weeks, which violates the rule.
            //
            // Also, we may choose to skip this week instead, so the best new endWithA
            // is the maximum of:
            // - staying with prevEndWithA by skipping
            // - starting from prevNone and taking A now
            // - alternating from prevEndWithB and taking A now
            long newEndWithA = Math.Max(
                prevEndWithA,
                Math.Max(prevNone + aReward[i], prevEndWithB + aReward[i])
            );

            // Option 3: Work on type B this week.
            //
            // Symmetric logic:
            // We can do B only if:
            // - we have never worked before (prevNone), or
            // - the previous last worked type was A (prevEndWithA)
            //
            // We cannot come from prevEndWithB when taking B this week.
            long newEndWithB = Math.Max(
                prevEndWithB,
                Math.Max(prevNone + bReward[i], prevEndWithA + bReward[i])
            );

            // Commit the updates for week i.
            none = newNone;
            endWithA = newEndWithA;
            endWithB = newEndWithB;
        }

        // The answer is the best among:
        // - never having worked at all (0)
        // - ending with A as the last worked type
        // - ending with B as the last worked type
        //
        // Since rewards are non-negative, one of endWithA/endWithB will usually be best,
        // but taking the max with none keeps the logic complete and safe.
        return Math.Max(none, Math.Max(endWithA, endWithB));
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] aReward1 = { 5, 1, 7, 4 };
int[] bReward1 = { 2, 10, 3, 8 };
long result1 = solution.MaxReward(aReward1, bReward1);
Console.WriteLine(result1); // Expected: 30

// Example 2
// Note:
// The prompt's stated output "34" is inconsistent with the written rules.
// Under the actual rules, the correct maximum is 26.
int[] aReward2 = { 9, 1, 1, 9 };
int[] bReward2 = { 1, 8, 8, 1 };
long result2 = solution.MaxReward(aReward2, bReward2);
Console.WriteLine(result2); // Correct under the stated rules: 26

// Additional quick sanity check
int[] aReward3 = { 10 };
int[] bReward3 = { 20 };
long result3 = solution.MaxReward(aReward3, bReward3);
Console.WriteLine(result3); // Expected: 20