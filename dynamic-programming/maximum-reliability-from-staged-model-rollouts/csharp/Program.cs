/*
Title: Maximum Reliability from Staged Model Rollouts
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A machine learning platform plans to deploy models over the next n days. On day i, the platform may either skip deployment or launch exactly one candidate model for that day. Each candidate model belongs to one of m architecture families, represented by an integer family[i], and provides an immediate reliability gain gain[i].

However, repeatedly deploying models from the same family too close together causes hidden coupling risk. To control this, the platform defines a cooldown window k: if you deploy a model from family x on day i, then deploying another model from the same family on any of the next k days is not allowed. Deploying a different family is always allowed, and skipping a day resets nothing.

Your task is to compute the maximum total reliability gain achievable over all n days.

Formally, choose a subset of days to deploy such that for any two chosen days a < b with family[a] == family[b], we must have b - a > k. The score is the sum of gain[i] over all chosen days. Return the maximum possible score.

Constraints:
- 1 <= n <= 200000
- 1 <= m <= 200000
- 1 <= family[i] <= m
- 1 <= gain[i] <= 10^9
- 0 <= k <= n

Examples:
1)
family = [1, 2, 1, 3, 2, 1]
gain   = [5, 4, 7, 3, 6, 10]
k = 2
Correct output: 23
One optimal valid choice is days 2, 4, 5:
- day 2 -> family 1, gain 7
- day 4 -> family 2, gain 6
- day 5 -> family 1, gain 10
Family 1 appears on days 2 and 5, distance = 3 > 2, so it is allowed.
Total = 7 + 6 + 10 = 23.

2)
family = [4, 4, 4, 2, 2]
gain   = [8, 1, 9, 5, 7]
k = 1
Correct output: 24
One optimal valid choice is days 0, 2, 4:
- family 4 on days 0 and 2, distance = 2 > 1
- family 2 on day 4
Total = 8 + 9 + 7 = 24.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n + m)

    Idea in plain words:
    --------------------
    Let dp[i] mean:
        the maximum total reliability gain we can achieve using only days 0..i.

    For each day i, we have two choices:
    1) Skip day i:
       then the best value stays dp[i - 1].
    2) Deploy on day i:
       then we add gain[i] to the best plan from earlier days that does NOT violate
       the cooldown rule for family[i].

    The challenge is:
       "What is the best earlier dp value we are allowed to combine with day i
        for this specific family?"

    If we deploy family x on day i, then the previous deployment of family x
    (if any) must be at most day i - k - 1 or earlier.
    So we need the best dp[t] where t <= i - k - 1, but we must also ensure that
    the chosen plan ending at t can be safely extended with family x.

    A very useful reformulation:
    ----------------------------
    Process days from left to right.
    Before handling day i, define:
        limit = i - k - 1

    Any day <= limit is now "old enough" so that if a plan ends there, we are allowed
    to deploy the same family again on day i.

    For each family f, we maintain:
        bestReady[f] = maximum dp[t] among all processed "ready" indices t
                       where day t itself belongs to family f.

    We also maintain:
        globalReadyBest = maximum dp[t] among all processed ready indices t,
                          regardless of family.

    Now consider deploying on day i with family x.

    There are two ways a valid previous plan can look:
    A) The previous chosen deployment (if any) is from a different family than x.
       Then any ready plan is okay, so we can use globalReadyBest.
    B) The previous chosen deployment is also family x, but old enough.
       Then we specifically need a ready plan that can end with family x,
       which is represented by bestReady[x].

    There is also the possibility that day i is the very first deployment:
       value = gain[i].

    So:
        take = gain[i] + max(0, globalReadyBest, bestReady[x])

    Finally:
        dp[i] = max(dp[i - 1], take)

    Why this works:
    ---------------
    Every valid plan has some last chosen day.
    When we choose day i as the last chosen day, the part before it must come from
    days that are old enough to respect the cooldown for family[i].
    The best such prefix is captured by the ready structures.

    Important implementation detail:
    --------------------------------
    A day t becomes "ready" exactly when we reach day i = t + k + 1.
    So while iterating i from 0 to n - 1, we can activate:
        readyIndex = i - k - 1
    If readyIndex >= 0, then dp[readyIndex] is now allowed to be used as a prefix
    for future same-family deployments.

    This gives a clean O(n) solution.
    */
    public long MaximumReliability(int[] family, int[] gain, int k)
    {
        int n = family.Length;

        // dp[i] = best answer considering days 0..i
        long[] dp = new long[n];

        // We need one slot per family id.
        // The problem states family[i] is in [1..m], but m is not passed directly.
        // We can safely size this by the maximum family id present in the input.
        int maxFamily = 0;
        for (int i = 0; i < n; i++)
        {
            if (family[i] > maxFamily)
            {
                maxFamily = family[i];
            }
        }

        // bestReady[f] stores the best dp[t] among "ready" days t whose family is f.
        // "Ready" means t is far enough in the past so that using family f again now
        // would not violate the cooldown.
        long[] bestReady = new long[maxFamily + 1];

        // globalReadyBest stores the best dp[t] over all ready days, regardless of family.
        long globalReadyBest = 0;

        for (int i = 0; i < n; i++)
        {
            // Step 1:
            // Activate the day that has just become old enough to be safely reused
            // with the same family after the cooldown.
            //
            // A day t becomes ready when current day i satisfies:
            //     i - t > k
            // which is equivalent to:
            //     t <= i - k - 1
            //
            // Since we process one day at a time, the newly activated index is:
            //     readyIndex = i - k - 1
            int readyIndex = i - k - 1;
            if (readyIndex >= 0)
            {
                int readyFamily = family[readyIndex];
                long readyValue = dp[readyIndex];

                // Update the best ready value for this specific family.
                // This means: among all old-enough plans whose last considered day is readyIndex
                // and whose day readyIndex belongs to readyFamily, keep the best dp value.
                if (readyValue > bestReady[readyFamily])
                {
                    bestReady[readyFamily] = readyValue;
                }

                // Also update the best ready value across all families.
                if (readyValue > globalReadyBest)
                {
                    globalReadyBest = readyValue;
                }
            }

            int currentFamily = family[i];
            long currentGain = gain[i];

            // Step 2:
            // Compute the best total if we choose to deploy on day i.
            //
            // We can always start a brand-new plan with only this day:
            //     currentGain
            //
            // Or we can append this day to a previously ready plan.
            // The best appendable prefix is the maximum of:
            // - globalReadyBest: best ready plan overall
            // - bestReady[currentFamily]: best ready plan specifically tracked for this family
            // - 0: meaning "take no previous day"
            //
            // Including both globalReadyBest and bestReady[currentFamily] is harmless and clear.
            // In practice, bestReady[currentFamily] is already one candidate among all ready plans,
            // but keeping it explicit makes the family-specific reasoning easier to understand.
            long bestPrefix = globalReadyBest;
            if (bestReady[currentFamily] > bestPrefix)
            {
                bestPrefix = bestReady[currentFamily];
            }
            if (bestPrefix < 0)
            {
                bestPrefix = 0;
            }

            long take = currentGain + bestPrefix;

            // Step 3:
            // Compute the best answer up to day i.
            //
            // Option A: skip day i -> dp[i - 1]
            // Option B: deploy on day i -> take
            if (i == 0)
            {
                dp[i] = take;
            }
            else
            {
                dp[i] = dp[i - 1] > take ? dp[i - 1] : take;
            }
        }

        return dp[n - 1];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] family1 = { 1, 2, 1, 3, 2, 1 };
int[] gain1 = { 5, 4, 7, 3, 6, 10 };
int k1 = 2;
long result1 = solution.MaximumReliability(family1, gain1, k1);
Console.WriteLine(result1); // Expected: 23

// Example 2
int[] family2 = { 4, 4, 4, 2, 2 };
int[] gain2 = { 8, 1, 9, 5, 7 };
int k2 = 1;
long result2 = solution.MaximumReliability(family2, gain2, k2);
Console.WriteLine(result2); // Expected: 24