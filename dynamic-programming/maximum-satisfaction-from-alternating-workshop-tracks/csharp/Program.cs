/*
Title: Maximum Satisfaction from Alternating Workshop Tracks

Problem Description:
A conference offers a sequence of workshops over N time slots. For each slot i, you may attend either the Engineering track or the Design track.
- If you attend Engineering in slot i, you gain engineering[i] satisfaction points.
- If you attend Design in slot i, you gain design[i] satisfaction points.

Constraint:
You may not attend more than K consecutive workshops from the same track.

In other words:
- Every consecutive run of Engineering choices must have length at most K.
- Every consecutive run of Design choices must have length at most K.

Goal:
Compute the maximum total satisfaction possible across all N slots.

This is a dynamic programming problem because the best decision at each slot depends on:
1. Which track you chose previously
2. How many times in a row you have already chosen that same track

Constraints:
- 1 <= N <= 100000
- 1 <= K <= N
- engineering.length == design.length == N
- 0 <= engineering[i], design[i] <= 10000
- At least one valid schedule exists
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(N * K)
      We process each of the N slots, and for each slot we may update up to K states
      for Engineering and K states for Design.

    Space Complexity:
    - O(K)
      We only keep the previous slot's DP states and the current slot's DP states,
      instead of storing all N rows.

    Beginner-friendly DP idea:
    --------------------------------
    We define states based on:
    - which track we chose at the current slot
    - how many consecutive times that track has been chosen

    Example:
    eng[c] = best total satisfaction so far if the current slot ends with Engineering
             and the current Engineering streak length is exactly c

    des[c] = best total satisfaction so far if the current slot ends with Design
             and the current Design streak length is exactly c

    Why do we need streak length?
    Because the rule says we cannot exceed K consecutive choices of the same track.
    So when we choose the same track again, the streak increases by 1.
    When we switch tracks, the streak resets to 1.
    */
    public int MaxSatisfaction(int[] engineering, int[] design, int k)
    {
        int n = engineering.Length;

        // We use a very small number to represent an impossible state.
        // We choose a long-based negative infinity to safely handle additions.
        long negativeInfinity = long.MinValue / 4;

        // engPrev[c]:
        // Best total after processing previous slot,
        // ending with Engineering and having an Engineering streak of length c.
        long[] engPrev = new long[k + 1];

        // desPrev[c]:
        // Best total after processing previous slot,
        // ending with Design and having a Design streak of length c.
        long[] desPrev = new long[k + 1];

        // Initialize all states as impossible.
        for (int c = 0; c <= k; c++)
        {
            engPrev[c] = negativeInfinity;
            desPrev[c] = negativeInfinity;
        }

        // Base case for the first slot:
        // If we choose Engineering at slot 0, streak length becomes 1.
        engPrev[1] = engineering[0];

        // If we choose Design at slot 0, streak length becomes 1.
        desPrev[1] = design[0];

        // Process slots from index 1 to n - 1.
        for (int i = 1; i < n; i++)
        {
            // These arrays will store the DP states for the current slot i.
            long[] engCurr = new long[k + 1];
            long[] desCurr = new long[k + 1];

            // Start by marking every current state as impossible.
            for (int c = 0; c <= k; c++)
            {
                engCurr[c] = negativeInfinity;
                desCurr[c] = negativeInfinity;
            }

            // ------------------------------------------------------------
            // Step 1: Extend an existing Engineering streak
            // ------------------------------------------------------------
            // If previous slot also ended with Engineering streak length c-1,
            // then choosing Engineering again makes the new streak length c.
            //
            // This is only valid for c from 2 to K.
            // We cannot extend beyond K because that would violate the rule.
            for (int c = 2; c <= k; c++)
            {
                if (engPrev[c - 1] != negativeInfinity)
                {
                    engCurr[c] = Math.Max(engCurr[c], engPrev[c - 1] + engineering[i]);
                }
            }

            // ------------------------------------------------------------
            // Step 2: Extend an existing Design streak
            // ------------------------------------------------------------
            // Same logic as above, but for Design.
            for (int c = 2; c <= k; c++)
            {
                if (desPrev[c - 1] != negativeInfinity)
                {
                    desCurr[c] = Math.Max(desCurr[c], desPrev[c - 1] + design[i]);
                }
            }

            // ------------------------------------------------------------
            // Step 3: Switch from Design to Engineering
            // ------------------------------------------------------------
            // If we choose Engineering now after previously ending with Design,
            // then the Engineering streak resets to 1.
            //
            // We need the best possible previous Design-ending state,
            // regardless of its streak length, because any Design streak can switch.
            long bestPreviousDesign = negativeInfinity;
            for (int c = 1; c <= k; c++)
            {
                if (desPrev[c] > bestPreviousDesign)
                {
                    bestPreviousDesign = desPrev[c];
                }
            }

            if (bestPreviousDesign != negativeInfinity)
            {
                engCurr[1] = Math.Max(engCurr[1], bestPreviousDesign + engineering[i]);
            }

            // ------------------------------------------------------------
            // Step 4: Switch from Engineering to Design
            // ------------------------------------------------------------
            // If we choose Design now after previously ending with Engineering,
            // then the Design streak resets to 1.
            long bestPreviousEngineering = negativeInfinity;
            for (int c = 1; c <= k; c++)
            {
                if (engPrev[c] > bestPreviousEngineering)
                {
                    bestPreviousEngineering = engPrev[c];
                }
            }

            if (bestPreviousEngineering != negativeInfinity)
            {
                desCurr[1] = Math.Max(desCurr[1], bestPreviousEngineering + design[i]);
            }

            // ------------------------------------------------------------
            // Step 5: Move current states into previous states
            // ------------------------------------------------------------
            // We have finished processing slot i.
            // For the next iteration, today's states become yesterday's states.
            engPrev = engCurr;
            desPrev = desCurr;
        }

        // ------------------------------------------------------------
        // Final answer:
        // After processing all slots, the answer is the best value among:
        // - all Engineering-ending states
        // - all Design-ending states
        // ------------------------------------------------------------
        long answer = 0;

        for (int c = 1; c <= k; c++)
        {
            if (engPrev[c] > answer)
            {
                answer = engPrev[c];
            }

            if (desPrev[c] > answer)
            {
                answer = desPrev[c];
            }
        }

        return (int)answer;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] engineering1 = { 8, 3, 5, 7 };
int[] design1 = { 4, 6, 2, 9 };
int k1 = 2;
int result1 = solution.MaxSatisfaction(engineering1, design1, k1);
Console.WriteLine(result1); // Expected: 28

// Example 2
int[] engineering2 = { 10, 10, 1, 10, 10 };
int[] design2 = { 1, 1, 20, 1, 1 };
int k2 = 2;
int result2 = solution.MaxSatisfaction(engineering2, design2, k2);
Console.WriteLine(result2); // Expected: 60

// Additional quick demo
int[] engineering3 = { 5, 5, 5 };
int[] design3 = { 1, 10, 1 };
int k3 = 1;
int result3 = solution.MaxSatisfaction(engineering3, design3, k3);
Console.WriteLine(result3); // One valid best schedule alternates every slot