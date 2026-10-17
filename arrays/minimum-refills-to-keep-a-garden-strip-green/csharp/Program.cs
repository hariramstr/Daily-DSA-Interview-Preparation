/*
Title: Minimum Refills to Keep a Garden Strip Green
Difficulty: Medium
Topic: Arrays

Problem Description:
A city park has a long straight garden strip divided into n sections. You are given an integer array waterNeed where waterNeed[i] is the amount of water required by section i. A maintenance cart starts with a watering tank of capacity cap units, and it moves from left to right, watering sections in order.

Watering section i reduces the current water in the tank by waterNeed[i]. If the cart does not have enough water to fully water the next section, it must return to the refill station, instantly refill the tank back to cap, and then continue. Returning for a refill can happen only before watering a section, never in the middle of one. Your task is to compute the minimum number of refills needed to water all sections in order.

However, there is one optimization: exactly once during the entire process, the cart may skip refilling even if the tank is insufficient, but only if the deficit for that section is at most bonus units. In that case, think of it as a temporary emergency reserve that can be used once and only once. After using this reserve, the tank becomes 0 after watering that section, and future watering still follows the normal refill rules.

Return the minimum number of refills required.

Constraints:
- 1 <= n <= 200000
- 1 <= waterNeed[i] <= cap <= 1000000000
- 0 <= bonus <= 1000000000
- The emergency reserve can be used at most once

Example 1:
Input: waterNeed = [2, 4, 3, 5, 2], cap = 6, bonus = 2
Output: 2

Example 2:
Input: waterNeed = [5, 1, 4, 4], cap = 5, bonus = 0
Output: 2
*/

using System;

public class Solution
{
    /*
        Time Complexity: O(n)
        Space Complexity: O(1)

        Core idea:
        We scan from left to right and keep two dynamic states:

        1) "unused" state:
           We have NOT used the one-time emergency reserve yet.
           For this state, we track:
           - the minimum number of refills needed so far
           - among all ways achieving that minimum, the maximum remaining water

        2) "used" state:
           We HAVE already used the reserve at some earlier section (or exactly now).
           For this state, we also track:
           - the minimum number of refills needed so far
           - among all ways achieving that minimum, the maximum remaining water

        Why keep "maximum remaining water" for a fixed refill count?
        Because if two ways use the same number of refills up to the current point,
        the one with more water left is always at least as good for the future.
        So for each refill count category, only the best remaining water matters.

        This gives a very compact DP with constant memory.
    */
    public int MinimumRefills(int[] waterNeed, int cap, int bonus)
    {
        const long INF = long.MaxValue / 4;

        // "unusedRefills" = minimum refills so far if reserve has not been used yet.
        long unusedRefills = 0;

        // "unusedWater" = among all ways with exactly "unusedRefills" refills and reserve unused,
        // keep the maximum remaining water after processing the current prefix.
        long unusedWater = cap;

        // "usedRefills" = minimum refills so far if reserve has already been used.
        // Start as impossible because before processing anything, we have not used it yet.
        long usedRefills = INF;

        // "usedWater" = best remaining water among ways with "usedRefills" refills and reserve already used.
        long usedWater = -1;

        // Process each section from left to right, exactly in the required order.
        foreach (int needInt in waterNeed)
        {
            long need = needInt;

            // These will become the next DP states after watering the current section.
            long nextUnusedRefills = INF;
            long nextUnusedWater = -1;

            long nextUsedRefills = INF;
            long nextUsedWater = -1;

            // ------------------------------------------------------------
            // Transition 1: continue from the state where reserve is still unused.
            // ------------------------------------------------------------
            if (unusedRefills < INF)
            {
                // Case 1A:
                // We have enough water already, so we simply water this section.
                // No refill is needed, reserve remains unused.
                if (unusedWater >= need)
                {
                    long candidateRefills = unusedRefills;
                    long candidateWater = unusedWater - need;

                    UpdateBest(ref nextUnusedRefills, ref nextUnusedWater, candidateRefills, candidateWater);
                }
                else
                {
                    // Case 1B:
                    // We do NOT have enough water currently.
                    // Normal rule says we must refill before watering this section.
                    // After refill, tank becomes full (= cap), then we water this section.
                    // Since constraints guarantee need <= cap, this is always feasible.
                    {
                        long candidateRefills = unusedRefills + 1;
                        long candidateWater = (long)cap - need;

                        UpdateBest(ref nextUnusedRefills, ref nextUnusedWater, candidateRefills, candidateWater);
                    }

                    // Case 1C:
                    // Instead of refilling, we may use the one-time emergency reserve NOW,
                    // but only if the deficit is at most "bonus".
                    //
                    // Deficit = need - currentWater
                    // If deficit <= bonus, we can finish this section without refilling.
                    // After using the reserve, the tank becomes exactly 0.
                    long deficit = need - unusedWater;
                    if (deficit <= bonus)
                    {
                        long candidateRefills = unusedRefills;
                        long candidateWater = 0;

                        UpdateBest(ref nextUsedRefills, ref nextUsedWater, candidateRefills, candidateWater);
                    }
                }
            }

            // ------------------------------------------------------------
            // Transition 2: continue from the state where reserve has already been used.
            // ------------------------------------------------------------
            if (usedRefills < INF)
            {
                // Since reserve is already spent, only normal watering/refill rules remain.

                // Case 2A:
                // Enough water already, so just water this section.
                if (usedWater >= need)
                {
                    long candidateRefills = usedRefills;
                    long candidateWater = usedWater - need;

                    UpdateBest(ref nextUsedRefills, ref nextUsedWater, candidateRefills, candidateWater);
                }
                else
                {
                    // Case 2B:
                    // Not enough water, so we must refill before this section.
                    long candidateRefills = usedRefills + 1;
                    long candidateWater = (long)cap - need;

                    UpdateBest(ref nextUsedRefills, ref nextUsedWater, candidateRefills, candidateWater);
                }
            }

            // Move to the next section.
            unusedRefills = nextUnusedRefills;
            unusedWater = nextUnusedWater;

            usedRefills = nextUsedRefills;
            usedWater = nextUsedWater;
        }

        // Final answer:
        // We may finish either without ever using the reserve, or after using it once.
        long answer = Math.Min(unusedRefills, usedRefills);
        return (int)answer;
    }

    // Helper method:
    // Keep the better state according to:
    // 1) smaller refill count is always better
    // 2) if refill count ties, larger remaining water is better
    private void UpdateBest(ref long bestRefills, ref long bestWater, long candidateRefills, long candidateWater)
    {
        if (candidateRefills < bestRefills)
        {
            bestRefills = candidateRefills;
            bestWater = candidateWater;
        }
        else if (candidateRefills == bestRefills && candidateWater > bestWater)
        {
            bestWater = candidateWater;
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] waterNeed1 = { 2, 4, 3, 5, 2 };
int cap1 = 6;
int bonus1 = 2;
int result1 = solution.MinimumRefills(waterNeed1, cap1, bonus1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] waterNeed2 = { 5, 1, 4, 4 };
int cap2 = 5;
int bonus2 = 0;
int result2 = solution.MinimumRefills(waterNeed2, cap2, bonus2);
Console.WriteLine(result2); // Expected: 2

// Additional quick checks
int[] waterNeed3 = { 3, 3, 3 };
int cap3 = 3;
int bonus3 = 1;
int result3 = solution.MinimumRefills(waterNeed3, cap3, bonus3);
Console.WriteLine(result3); // Expected: 2

int[] waterNeed4 = { 2, 5 };
int cap4 = 5;
int bonus4 = 2;
int result4 = solution.MinimumRefills(waterNeed4, cap4, bonus4);
Console.WriteLine(result4); // Expected: 0 (use reserve on second section after first leaves 3? Actually first leaves 3, second needs 5, deficit 2 => reserve, no refill)

int[] waterNeed5 = { 1, 2, 3, 4 };
int cap5 = 5;
int bonus5 = 10;
int result5 = solution.MinimumRefills(waterNeed5, cap5, bonus5);
Console.WriteLine(result5);