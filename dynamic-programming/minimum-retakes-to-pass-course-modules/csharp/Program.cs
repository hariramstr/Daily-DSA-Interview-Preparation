/*
Title: Minimum Retakes to Pass Course Modules

Problem Description:
You are given an array modules of length n, where modules[i] is the score you would earn on the i-th course module if you attempt it.
You must process the modules from left to right. For each module, you have two choices:
1. Keep the score as-is
2. Spend one retake to improve that module's score by exactly d points

Each module can be retaken at most once.

Your goal is to make the final sequence of scores non-decreasing, meaning the score of every module must be at least the score of the previous module after all retake decisions are applied.

Return the minimum number of retakes needed to achieve this.
If it is impossible, return -1.

You are not allowed to reorder modules, skip modules, or retake a module multiple times.

Constraints:
- 1 <= n <= 100000
- 0 <= modules[i] <= 1000000000
- 0 <= d <= 1000000000

Examples:
1) modules = [4, 2, 5, 5], d = 3
   Output: 1
   Explanation:
   Retake the second module only.
   Final scores become [4, 5, 5, 5], which is non-decreasing.

2) modules = [7, 3, 2], d = 4
   Output: -1
   Explanation:
   Possible values are:
   - First:  7 or 11
   - Second: 3 or 7
   - Third:  2 or 6
   No combination forms a non-decreasing sequence.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Idea:
    For each module, there are only two possible final values:
    - modules[i]
    - modules[i] + d

    We use dynamic programming with exactly two states per position:
    - dpKeep:   minimum retakes needed so far if current module ends as modules[i]
    - dpRetake: minimum retakes needed so far if current module ends as modules[i] + d

    Transition rule:
    A current choice is valid only if its final value is >= the previous final value.
    Since the previous module also had only two possible final values, we only need to
    check transitions from those two previous states.

    This is enough because the problem only asks for the minimum number of retakes,
    and the only information needed to continue is:
    1. what final value we ended with at the previous position
    2. the minimum retake count to achieve that
    */
    public int MinimumRetakes(int[] modules, int d)
    {
        // A very large number used as "infinity".
        // If a state keeps this value, it means that state is impossible.
        const int INF = int.MaxValue / 4;

        // Convert to long when computing values to be extra safe.
        // Even though 1e9 + 1e9 fits in int, using long avoids accidental overflow
        // if future modifications are made.
        long firstKeepValue = modules[0];
        long firstRetakeValue = (long)modules[0] + d;

        // Base case for the first module:
        // - If we keep it, we used 0 retakes.
        // - If we retake it, we used 1 retake.
        int prevKeepCost = 0;
        int prevRetakeCost = 1;

        // These store the actual final values associated with the two previous states.
        long prevKeepValue = firstKeepValue;
        long prevRetakeValue = firstRetakeValue;

        // Process modules from left to right, because the order is fixed by the problem.
        for (int i = 1; i < modules.Length; i++)
        {
            // The current module also has exactly two possible final values:
            // 1) keep original score
            // 2) use one retake and add d
            long currentKeepValue = modules[i];
            long currentRetakeValue = (long)modules[i] + d;

            // Start by assuming both current states are impossible.
            int currentKeepCost = INF;
            int currentRetakeCost = INF;

            // ------------------------------------------------------------
            // STEP 1: Try to end current module with "keep" value.
            // ------------------------------------------------------------
            // We can come from previous "keep" state if:
            // currentKeepValue >= prevKeepValue
            // because the sequence must be non-decreasing.
            if (prevKeepCost < INF && currentKeepValue >= prevKeepValue)
            {
                // No extra retake is used for current module in this branch.
                currentKeepCost = Math.Min(currentKeepCost, prevKeepCost);
            }

            // We can also come from previous "retake" state if:
            // currentKeepValue >= prevRetakeValue
            if (prevRetakeCost < INF && currentKeepValue >= prevRetakeValue)
            {
                currentKeepCost = Math.Min(currentKeepCost, prevRetakeCost);
            }

            // ------------------------------------------------------------
            // STEP 2: Try to end current module with "retake" value.
            // ------------------------------------------------------------
            // If we retake the current module, we spend exactly one more retake.
            // Again, the non-decreasing condition must hold.

            // Transition from previous "keep" state.
            if (prevKeepCost < INF && currentRetakeValue >= prevKeepValue)
            {
                currentRetakeCost = Math.Min(currentRetakeCost, prevKeepCost + 1);
            }

            // Transition from previous "retake" state.
            if (prevRetakeCost < INF && currentRetakeValue >= prevRetakeValue)
            {
                currentRetakeCost = Math.Min(currentRetakeCost, prevRetakeCost + 1);
            }

            // ------------------------------------------------------------
            // STEP 3: Move current states into "previous" variables.
            // ------------------------------------------------------------
            // We only need the immediately previous position for the next step,
            // so we overwrite the old DP values. This is why the space usage is O(1).
            prevKeepCost = currentKeepCost;
            prevRetakeCost = currentRetakeCost;
            prevKeepValue = currentKeepValue;
            prevRetakeValue = currentRetakeValue;
        }

        // The answer is the cheaper of the two valid ways to finish the last module.
        int answer = Math.Min(prevKeepCost, prevRetakeCost);

        // If both states are impossible, return -1.
        return answer >= INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] modules1 = { 4, 2, 5, 5 };
int d1 = 3;
int result1 = solution.MinimumRetakes(modules1, d1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 1

// Example 2
int[] modules2 = { 7, 3, 2 };
int d2 = 4;
int result2 = solution.MinimumRetakes(modules2, d2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: -1

// Additional demo cases

// Already non-decreasing, no retakes needed
int[] modules3 = { 1, 2, 2, 9 };
int d3 = 5;
int result3 = solution.MinimumRetakes(modules3, d3);
Console.WriteLine($"Example 3 Result: {result3}"); // Expected: 0

// Need multiple retakes
int[] modules4 = { 3, 1, 1, 1 };
int d4 = 2;
int result4 = solution.MinimumRetakes(modules4, d4);
Console.WriteLine($"Example 4 Result: {result4}");

// Edge case: single module
int[] modules5 = { 10 };
int d5 = 100;
int result5 = solution.MinimumRetakes(modules5, d5);
Console.WriteLine($"Example 5 Result: {result5}"); // Expected: 0