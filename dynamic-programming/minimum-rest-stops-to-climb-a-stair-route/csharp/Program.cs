/*
Title: Minimum Rest Stops to Climb a Stair Route

Problem Description:
You are planning a climb along a stair route with n steps, numbered from 0 to n.
You start at step 0 and want to reach step n.

From any step, you may move either:
- 1 step forward, or
- 2 steps forward

Some steps are marked as rest stops, represented by a binary array rests of length n + 1:
- rests[i] = 1 means step i has a rest platform
- rests[i] = 0 means it does not

Step 0 and step n may also be rest stops.

Your goal is to reach step n while using the minimum possible number of rest stops visited,
including the destination if it is a rest stop.

A rest stop is counted only when you land on that step.

Some steps may be blocked, represented by another binary array blocked of length n + 1:
- blocked[i] = 1 means you cannot stand on step i
- blocked[i] = 0 means you can stand on step i

You may assume blocked[0] = 0.

Return the minimum number of rest stops needed to reach step n.
If it is impossible to reach the top, return -1.

This is a dynamic programming problem because the best answer for each step depends on
the best answers for the previous one or two steps.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Beginner-friendly idea:
    - Let dp[i] mean:
      "the minimum number of rest stops visited to reach step i"
    - To reach step i, we can only come from:
      - step i - 1
      - step i - 2
    - So we choose the better of those two previous reachable states.
    - If step i is blocked, then dp[i] is impossible.
    - If step i has a rest stop, we add 1 when we land there.
    */
    public int MinRestStops(int n, int[] rests, int[] blocked)
    {
        // We use a very large number to represent "impossible to reach".
        // We do not use int.MaxValue directly because later we may add 1,
        // and adding 1 to int.MaxValue would overflow.
        const int INF = 1_000_000_000;

        // dp[i] will store the minimum number of rest stops needed to reach step i.
        // If dp[i] stays INF, that means step i cannot be reached.
        int[] dp = new int[n + 1];

        // First, initialize every step as unreachable.
        // This gives us a clean starting point.
        for (int i = 0; i <= n; i++)
        {
            dp[i] = INF;
        }

        // Base case:
        // We start on step 0.
        // The problem says a rest stop is counted only when you land on that step.
        // Since step 0 is the starting position, we do NOT count rests[0].
        dp[0] = 0;

        // Now process each step from 1 to n.
        // We build answers from smaller steps to larger steps.
        for (int i = 1; i <= n; i++)
        {
            // If this step is blocked, we are not allowed to stand on it.
            // That means no path may end here, so we leave dp[i] as INF.
            if (blocked[i] == 1)
            {
                continue;
            }

            // We want the best previous reachable step.
            // Since we can move only 1 or 2 steps, the only candidates are:
            // - i - 1
            // - i - 2
            int bestPrevious = INF;

            // Check if step i - 1 exists and is reachable.
            // If yes, it is one possible way to arrive at step i.
            if (i - 1 >= 0)
            {
                bestPrevious = Math.Min(bestPrevious, dp[i - 1]);
            }

            // Check if step i - 2 exists and is reachable.
            // This is the other possible way to arrive at step i.
            if (i - 2 >= 0)
            {
                bestPrevious = Math.Min(bestPrevious, dp[i - 2]);
            }

            // If both previous options were unreachable,
            // then this step is also unreachable.
            if (bestPrevious == INF)
            {
                continue;
            }

            // If we can reach this step, then the cost to stand here is:
            // best previous cost + 1 if this step is a rest stop, else + 0
            //
            // Why do we add rests[i] here?
            // Because the problem says a rest stop is counted when we LAND on that step.
            dp[i] = bestPrevious + rests[i];
        }

        // If step n is still unreachable, return -1.
        if (dp[n] == INF)
        {
            return -1;
        }

        // Otherwise, dp[n] is the minimum number of rest stops needed.
        return dp[n];
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// n = 5
// rests = [0,1,0,1,0,1]
// blocked = [0,0,0,0,0,0]
//
// One optimal path: 0 -> 2 -> 4 -> 5
// Rest stops visited: only step 5
// Expected output: 1
int n1 = 5;
int[] rests1 = { 0, 1, 0, 1, 0, 1 };
int[] blocked1 = { 0, 0, 0, 0, 0, 0 };
int result1 = solution.MinRestStops(n1, rests1, blocked1);
Console.WriteLine(result1);

// Example 2:
// n = 6
// rests = [0,1,1,0,1,0,1]
// blocked = [0,0,1,0,0,1,0]
//
// Important note:
// The written explanation in the prompt says:
// path 0 -> 1 -> 3 -> 4 -> 6 visits only step 1 as a rest stop.
// But step 4 is also a rest stop in the given array, so that path would count 2 rest stops.
// The dynamic programming algorithm correctly computes the true minimum from the given arrays.
//
// Let's verify quickly:
// - Step 2 is blocked
// - Step 5 is blocked
// Reachable path: 0 -> 1 -> 3 -> 4 -> 6
// Rest stops landed on: step 1 and step 4 = 2
// Another path is not possible with fewer rest stops under these exact arrays.
// Therefore the correct result for the provided arrays is 2.
int n2 = 6;
int[] rests2 = { 0, 1, 1, 0, 1, 0, 1 };
int[] blocked2 = { 0, 0, 1, 0, 0, 1, 0 };
int result2 = solution.MinRestStops(n2, rests2, blocked2);
Console.WriteLine(result2);

// Additional demo where reaching the top is impossible.
// Step 1 and step 2 are both blocked, so from step 0 we cannot move anywhere.
int n3 = 3;
int[] rests3 = { 0, 0, 1, 0 };
int[] blocked3 = { 0, 1, 1, 0 };
int result3 = solution.MinRestStops(n3, rests3, blocked3);
Console.WriteLine(result3);