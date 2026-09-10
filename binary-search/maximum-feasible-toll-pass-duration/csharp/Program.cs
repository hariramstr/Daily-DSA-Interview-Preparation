/*
Title: Maximum Feasible Toll Pass Duration
Difficulty: Hard
Topic: Binary Search

Problem Description:
A logistics company operates on a straight highway with toll gates placed at strictly increasing mile markers.
A driver starts at mile 0 and must finish at mile L.

The company wants to sell a reusable toll pass that lasts for exactly D miles:
once activated, it covers every toll gate whose mile marker lies in the interval [x, x + D]
for some chosen activation point x.

The driver may buy at most K such passes during the trip, and passes may overlap.

You are given an array gates where gates[i] is the mile marker of the i-th toll gate,
sorted in strictly increasing order, along with integers L and K.

Determine the maximum integer duration D such that it is possible to choose at most K
activation intervals of length D and cover every toll gate on the route.
The activation points do not need to be toll locations; they may be any real values,
but the answer D must be an integer.

Important correctness note:
For this exact mathematical model, feasibility is monotonic in D:
- If a duration D works, then any larger duration also works, because longer intervals
  can simulate shorter ones and possibly cover even more gates.
Therefore, the "maximum feasible D" over the range [0, L] is always L whenever K >= 1.
Since the constraints guarantee K >= 1, the correct answer is always L.

This means the sample outputs shown in the prompt are inconsistent with the stated problem.
A correct implementation must follow the formal problem statement, not the inconsistent examples.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(1)
    Space Complexity: O(1)

    Why O(1)?
    - Under the exact statement of the problem, K >= 1.
    - A single pass of duration L can be activated at x = 0, covering [0, L].
    - Since every gate lies within [1, L], that one pass covers all gates.
    - Therefore the largest feasible integer duration in [0, L] is simply L.

    This method still accepts the full input signature expected by the problem.
    */
    public long MaximumFeasibleTollPassDuration(long[] gates, long L, int K)
    {
        // Step 1:
        // We are asked for the largest integer D in the range [0, L]
        // such that all toll gates can be covered using at most K intervals of length D.
        //
        // Because K >= 1, we are allowed to buy at least one pass.

        // Step 2:
        // Consider choosing D = L.
        // If we activate that pass at x = 0, the covered interval is:
        // [0, 0 + L] = [0, L]
        //
        // Every gate position gates[i] satisfies 1 <= gates[i] <= L,
        // so every gate lies inside [0, L].

        // Step 3:
        // Therefore D = L is always feasible with exactly one pass.
        // Since the answer is restricted to the range [0, L],
        // no value larger than L is allowed.
        //
        // So L is not only feasible, it is also the maximum possible answer.

        return L;
    }
}

// Demo code
var solution = new Solution();

long[] gates1 = { 2, 5, 6, 11, 14 };
long L1 = 20;
int K1 = 2;
Console.WriteLine(solution.MaximumFeasibleTollPassDuration(gates1, L1, K1));

long[] gates2 = { 1, 4, 8, 9, 15 };
long L2 = 20;
int K2 = 3;
Console.WriteLine(solution.MaximumFeasibleTollPassDuration(gates2, L2, K2));