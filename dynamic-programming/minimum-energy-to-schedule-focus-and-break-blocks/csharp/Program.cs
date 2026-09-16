/*
Title: Minimum Energy to Schedule Focus and Break Blocks

Problem Description:
A productivity app plans a day as a sequence of tasks. For each task i, you are given two energy costs:
focusCost[i] and breakCost[i]. You must assign every task to exactly one mode: Focus or Break.

The total energy spent is the sum of the chosen costs, but there is an additional rule:
no more than k consecutive tasks may be assigned to Focus mode, because long uninterrupted
focus sessions are not allowed.

Your goal is to compute the minimum total energy needed to schedule all tasks while respecting
the consecutive-focus limit.

Formally, given two integer arrays focusCost and breakCost of length n and an integer k,
choose for each index i either focusCost[i] or breakCost[i]. If you choose Focus for task i,
it contributes focusCost[i] to the total. If you choose Break for task i, it contributes
breakCost[i] to the total. In the final assignment, every maximal consecutive run of Focus
tasks must have length at most k.

Return the minimum possible total energy.

Constraints:
- 1 <= n <= 100000
- 1 <= k <= n
- focusCost.length == breakCost.length == n
- 1 <= focusCost[i], breakCost[i] <= 1000000000

Examples:
1)
focusCost = [3, 2, 5, 1]
breakCost = [4, 6, 1, 7]
k = 2
Output: 7
Explanation:
Choose Focus, Focus, Break, Focus.
Total energy = 3 + 2 + 1 + 1 = 7.

2)
focusCost = [1, 1, 1, 1]
breakCost = [10, 10, 10, 10]
k = 2
Output: 13
Explanation:
All Focus is invalid because that would create a run of 4 consecutive Focus tasks.
At least one Break must be inserted, and the minimum valid total is 13.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n * k)
    Space Complexity: O(k)

    Beginner-friendly idea:
    -----------------------
    We process tasks from left to right.

    Dynamic Programming state:
    dp[s] = minimum total energy after processing the current prefix of tasks,
            where s means the current suffix (ending streak) has exactly s consecutive Focus tasks.

    Meaning of s:
    - s = 0  -> the current task was assigned Break, so the current Focus streak length is 0
    - s = 1  -> the current task was Focus, and the current streak length is 1
    - ...
    - s = k  -> the current task was Focus, and the current streak length is k

    Transition for each task i:
    1. Assign task i as Break:
       - No matter what the previous streak length was, a Break resets the streak to 0.
       - So newDp[0] = min(dp[0], dp[1], ..., dp[k]) + breakCost[i]

    2. Assign task i as Focus:
       - We can only extend a previous streak of length s-1 into length s
       - Therefore for s from 1 to k:
         newDp[s] = dp[s - 1] + focusCost[i]

    Why this works:
    - The only information needed from the past is how many consecutive Focus tasks we currently have.
    - We do not need the full assignment history.
    - This is a classic "DP by last streak length" pattern.

    Important correctness note:
    - Example 2 in the prompt contains a warning that the correct answer for length 4 is 13, not 12.
    - This DP enforces the constraint exactly, so it returns 13 for that example.
    */
    public long MinimumEnergy(int[] focusCost, int[] breakCost, int k)
    {
        int n = focusCost.Length;

        // We use a very large number to represent an unreachable / not-yet-computed state.
        // long is required because:
        // - each cost can be up to 1e9
        // - n can be up to 1e5
        // - total can therefore be up to 1e14, which does not fit in int
        const long INF = long.MaxValue / 4;

        // dp[s] means:
        // after processing the tasks seen so far,
        // the minimum total energy where the current consecutive Focus streak length is exactly s.
        long[] dp = new long[k + 1];

        // Initially, before processing any tasks:
        // - streak length 0 is valid with cost 0
        // - all other streak lengths are impossible
        dp[0] = 0;
        for (int s = 1; s <= k; s++)
        {
            dp[s] = INF;
        }

        // Process each task one by one.
        for (int i = 0; i < n; i++)
        {
            // newDp will store the DP values after deciding mode for task i.
            long[] newDp = new long[k + 1];

            // Start by marking all states as unreachable.
            for (int s = 0; s <= k; s++)
            {
                newDp[s] = INF;
            }

            // ------------------------------------------------------------
            // Step 1: Compute the best previous cost among all streak lengths.
            // ------------------------------------------------------------
            // Why do we need this?
            // If we assign the current task as Break, then the Focus streak resets to 0.
            // A Break can follow ANY previous state, so we need:
            // min(dp[0], dp[1], ..., dp[k])
            long bestPrevious = INF;
            for (int s = 0; s <= k; s++)
            {
                if (dp[s] < bestPrevious)
                {
                    bestPrevious = dp[s];
                }
            }

            // ------------------------------------------------------------
            // Step 2: Assign current task as Break.
            // ------------------------------------------------------------
            // This is always allowed.
            // The streak becomes 0 because a Break interrupts any Focus run.
            newDp[0] = bestPrevious + breakCost[i];

            // ------------------------------------------------------------
            // Step 3: Assign current task as Focus.
            // ------------------------------------------------------------
            // If the new streak length is s, then the previous streak length must have been s - 1.
            // This is only valid for s in [1..k].
            // We are NOT allowed to create streak length k+1, so the loop stops at k.
            for (int s = 1; s <= k; s++)
            {
                if (dp[s - 1] != INF)
                {
                    newDp[s] = dp[s - 1] + focusCost[i];
                }
            }

            // ------------------------------------------------------------
            // Step 4: Move to the next task.
            // ------------------------------------------------------------
            // The newly computed states become the current states.
            dp = newDp;
        }

        // ------------------------------------------------------------
        // Final answer:
        // ------------------------------------------------------------
        // After all tasks are processed, any ending streak length from 0..k is valid.
        // So the answer is the minimum among all dp[s].
        long answer = INF;
        for (int s = 0; s <= k; s++)
        {
            if (dp[s] < answer)
            {
                answer = dp[s];
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] focusCost1 = { 3, 2, 5, 1 };
int[] breakCost1 = { 4, 6, 1, 7 };
int k1 = 2;
long result1 = solution.MinimumEnergy(focusCost1, breakCost1, k1);
Console.WriteLine(result1); // Expected: 7

// Example 2
int[] focusCost2 = { 1, 1, 1, 1 };
int[] breakCost2 = { 10, 10, 10, 10 };
int k2 = 2;
long result2 = solution.MinimumEnergy(focusCost2, breakCost2, k2);
Console.WriteLine(result2); // Expected: 13

// Additional quick sanity check
int[] focusCost3 = { 5, 5, 5 };
int[] breakCost3 = { 1, 1, 1 };
int k3 = 1;
long result3 = solution.MinimumEnergy(focusCost3, breakCost3, k3);
Console.WriteLine(result3); // One optimal schedule is Break, Break, Break => 3