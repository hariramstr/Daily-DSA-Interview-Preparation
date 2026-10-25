/*
Title: Minimum Cost to Schedule Server Reboots with Cluster Penalties

Problem Description:
You are given a sequence of n servers in a data center, indexed from 0 to n - 1.
Each server must be rebooted exactly once, and the reboot for server i can be
scheduled in one of two maintenance windows:

- Early window with cost earlyCost[i]
- Late window with cost lateCost[i]

For every adjacent pair (i - 1, i):
- If both servers are assigned to Early, add earlyPenalty[i]
- If both servers are assigned to Late, add latePenalty[i]
- If they are assigned to different windows, add no penalty

Additionally, exactly k servers must be assigned to the Early window.

Return the minimum total cost.

Constraints:
- 1 <= n <= 2000
- 0 <= k <= n
- Costs and penalties can be as large as 1e9, so 64-bit integers are required.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * k)
      For each server, we iterate through all possible counts of how many servers
      have been assigned to the Early window so far, and for each count we consider
      the two possible states of the previous server (Early or Late).

    Space Complexity:
    - O(k)
      We only keep DP information for the previous server and the current server,
      instead of storing all n layers.

    Beginner-friendly idea:
    -----------------------
    This is a dynamic programming problem on a line of servers.

    Why DP works:
    - The cost for server i depends on:
      1. whether server i is Early or Late
      2. whether server i - 1 was Early or Late (because of adjacency penalties)
      3. how many Early assignments we have used so far

    So our DP state needs exactly those pieces of information.

    DP definition:
    Let:
      dpEarly[e] = minimum total cost after processing the current prefix of servers,
                   using exactly e Early assignments, and with the current server
                   assigned to Early.

      dpLate[e]  = same idea, but the current server is assigned to Late.

    Transition:
    When processing server i:
    - If we place server i in Early:
        - from previous Early: add earlyCost[i] + earlyPenalty[i]
        - from previous Late : add earlyCost[i]
      and Early count increases by 1.

    - If we place server i in Late:
        - from previous Late : add lateCost[i] + latePenalty[i]
        - from previous Early: add lateCost[i]
      and Early count does not increase.

    Base case:
    For server 0:
    - If server 0 is Early: cost = earlyCost[0], Early count = 1
    - If server 0 is Late : cost = lateCost[0],  Early count = 0

    Final answer:
    After processing all servers, answer is:
      min(dpEarly[k], dpLate[k])
    */
    public long MinCost(int n, int k, long[] earlyCost, long[] lateCost, long[] earlyPenalty, long[] latePenalty)
    {
        // A very large number used as "infinity".
        // We choose a value safely below long.MaxValue so that adding costs to it
        // will not overflow.
        const long INF = long.MaxValue / 4;

        // If k is impossible, return a sentinel.
        // The problem constraints imply 0 <= k <= n, but this guard makes the method robust.
        if (k < 0 || k > n)
        {
            return -1;
        }

        // dpEarly[e] and dpLate[e] describe the best cost after processing the current server.
        // We only need the previous layer to compute the next one, so we use rolling arrays.
        long[] prevEarly = new long[k + 1];
        long[] prevLate = new long[k + 1];
        long[] currEarly = new long[k + 1];
        long[] currLate = new long[k + 1];

        // Initialize all states to INF, meaning "currently unreachable".
        for (int e = 0; e <= k; e++)
        {
            prevEarly[e] = INF;
            prevLate[e] = INF;
            currEarly[e] = INF;
            currLate[e] = INF;
        }

        // Base case for server 0:
        // Option 1: assign server 0 to Early.
        // This uses exactly 1 Early assignment and costs earlyCost[0].
        if (k >= 1)
        {
            prevEarly[1] = earlyCost[0];
        }

        // Option 2: assign server 0 to Late.
        // This uses 0 Early assignments and costs lateCost[0].
        prevLate[0] = lateCost[0];

        // Process servers from index 1 to n - 1.
        for (int i = 1; i < n; i++)
        {
            // Before filling the current layer, reset it to INF.
            // This is necessary because we are reusing the same arrays for every server.
            for (int e = 0; e <= k; e++)
            {
                currEarly[e] = INF;
                currLate[e] = INF;
            }

            // After processing server i, the number of Early assignments used so far
            // can be anywhere from 0 to min(i + 1, k).
            int maxEarlyUsedSoFar = Math.Min(i + 1, k);

            for (int e = 0; e <= maxEarlyUsedSoFar; e++)
            {
                // ------------------------------------------------------------
                // Case 1: assign current server i to Early
                // ------------------------------------------------------------
                // If server i is Early, then we must have used one fewer Early
                // assignment before processing this server.
                //
                // So previous Early count must be e - 1.
                if (e >= 1)
                {
                    long best = INF;

                    // Transition from previous server also being Early:
                    // - previous state: prevEarly[e - 1]
                    // - current server cost: earlyCost[i]
                    // - same-window adjacency penalty: earlyPenalty[i]
                    if (prevEarly[e - 1] != INF)
                    {
                        long candidate = prevEarly[e - 1] + earlyCost[i] + earlyPenalty[i];
                        if (candidate < best)
                        {
                            best = candidate;
                        }
                    }

                    // Transition from previous server being Late:
                    // - previous state: prevLate[e - 1]
                    // - current server cost: earlyCost[i]
                    // - no adjacency penalty because windows differ
                    if (prevLate[e - 1] != INF)
                    {
                        long candidate = prevLate[e - 1] + earlyCost[i];
                        if (candidate < best)
                        {
                            best = candidate;
                        }
                    }

                    currEarly[e] = best;
                }

                // ------------------------------------------------------------
                // Case 2: assign current server i to Late
                // ------------------------------------------------------------
                // If server i is Late, then the number of Early assignments used
                // does not change, so previous Early count is also e.
                {
                    long best = INF;

                    // Transition from previous server also being Late:
                    // - previous state: prevLate[e]
                    // - current server cost: lateCost[i]
                    // - same-window adjacency penalty: latePenalty[i]
                    if (prevLate[e] != INF)
                    {
                        long candidate = prevLate[e] + lateCost[i] + latePenalty[i];
                        if (candidate < best)
                        {
                            best = candidate;
                        }
                    }

                    // Transition from previous server being Early:
                    // - previous state: prevEarly[e]
                    // - current server cost: lateCost[i]
                    // - no adjacency penalty because windows differ
                    if (prevEarly[e] != INF)
                    {
                        long candidate = prevEarly[e] + lateCost[i];
                        if (candidate < best)
                        {
                            best = candidate;
                        }
                    }

                    currLate[e] = best;
                }
            }

            // Move current layer into previous layer for the next iteration.
            // We swap references instead of copying values to keep the implementation efficient.
            var tempEarly = prevEarly;
            prevEarly = currEarly;
            currEarly = tempEarly;

            var tempLate = prevLate;
            prevLate = currLate;
            currLate = tempLate;
        }

        // After all servers are processed, we need exactly k Early assignments.
        // The last server may be either Early or Late, so we take the minimum.
        long answer = Math.Min(prevEarly[k], prevLate[k]);

        // Under valid constraints, answer should always be reachable.
        // Still, returning -1 for unreachable states makes the method safer.
        return answer >= INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int n1 = 4;
int k1 = 2;
long[] earlyCost1 = { 3, 8, 2, 6 };
long[] lateCost1 = { 5, 1, 7, 3 };
long[] earlyPenalty1 = { 0, 4, 5, 2 };
long[] latePenalty1 = { 0, 6, 1, 3 };

// Correct minimum is 9:
// [Early, Late, Early, Late] => 3 + 1 + 2 + 3 = 9, no same-window adjacency penalties.
long result1 = solution.MinCost(n1, k1, earlyCost1, lateCost1, earlyPenalty1, latePenalty1);
Console.WriteLine(result1);

// Example 2
int n2 = 5;
int k2 = 3;
long[] earlyCost2 = { 4, 2, 9, 3, 5 };
long[] lateCost2 = { 6, 7, 1, 8, 2 };
long[] earlyPenalty2 = { 0, 3, 4, 6, 1 };
long[] latePenalty2 = { 0, 2, 5, 2, 7 };

// The problem statement's narrative contains a contradiction.
// It first says output 18, then shows an assignment with total 15, and concludes minimum is 15.
// The correct minimum is 15.
long result2 = solution.MinCost(n2, k2, earlyCost2, lateCost2, earlyPenalty2, latePenalty2);
Console.WriteLine(result2);