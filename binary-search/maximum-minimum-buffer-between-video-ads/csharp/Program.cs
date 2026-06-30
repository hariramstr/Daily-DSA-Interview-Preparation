/*
Title: Maximum Minimum Buffer Between Video Ads

Problem Description:
A streaming platform wants to insert exactly k advertisement breaks into a video of total length L seconds.
You are given a sorted array candidateTimes where each value represents a second mark at which an ad break
is allowed to start. You may only place ad breaks at these candidate times, and each chosen time must be distinct.

The platform wants the ads to feel evenly spaced, so it defines the quality of a placement as the minimum
distance in seconds between any two consecutive chosen ad breaks.

Your task is to compute the largest possible quality value.

In other words, choose exactly k values from candidateTimes so that the minimum difference between adjacent
chosen values is as large as possible, and return that maximum possible minimum difference.

This is a decision-and-optimization problem:
- For a guessed minimum gap g, determine whether it is possible to pick k ad breaks such that every
  consecutive pair is at least g seconds apart.
- Use this property to find the optimal answer efficiently.

Constraints:
- 2 <= candidateTimes.length <= 100000
- 0 <= candidateTimes[i] <= 1000000000
- candidateTimes is sorted in strictly increasing order
- 2 <= k <= candidateTimes.length
- 1 <= L <= 1000000000
- All candidateTimes[i] are within the video, i.e. 0 <= candidateTimes[i] <= L

Example 1:
Input: candidateTimes = [5, 11, 18, 26, 39], k = 3, L = 45
Output: 13
Explanation:
Choose ad breaks at 5, 18, and 39.
The consecutive gaps are 13 and 21, so the minimum gap is 13.
No other selection of 3 positions can achieve a larger minimum gap.

Example 2:
Input: candidateTimes = [2, 4, 7, 10, 14, 19], k = 4, L = 20
Correct Output: 5
Explanation:
Choose ad breaks at 2, 7, 14, and 19.
The consecutive gaps are 5, 7, and 5, so the minimum gap is 5.

Important note:
Although the sample text briefly says "Output: 4", the explanation proves 5 is achievable,
so the correct answer is 5. The algorithm below correctly returns 5.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log D)
      where:
      n = number of candidate times
      D = candidateTimes[last] - candidateTimes[first]
    Explanation:
    - We binary search the answer (the minimum allowed gap).
    - For each guessed gap, we do one linear greedy scan to check feasibility.

    Space Complexity:
    - O(1) extra space
    Explanation:
    - We only use a few variables and do not allocate extra data structures
      proportional to the input size.
    */
    public int MaximizeMinimumBuffer(int[] candidateTimes, int k, int L)
    {
        // ------------------------------------------------------------
        // STEP 1: Define the search range for the answer.
        // ------------------------------------------------------------
        // We are searching for the largest minimum gap between consecutive
        // chosen ad breaks.
        //
        // The smallest possible minimum gap is 0 in a very general sense,
        // but because candidateTimes is strictly increasing and we must pick
        // distinct positions, the actual answer will be at least 1 when k >= 2.
        // Still, using 0 as the lower bound is perfectly safe and simple.
        //
        // The largest possible minimum gap cannot exceed the distance between
        // the first and last candidate positions.
        //
        // Example:
        // candidateTimes = [5, 11, 18, 26, 39]
        // max possible gap cannot be more than 39 - 5 = 34
        int left = 0;
        int right = candidateTimes[candidateTimes.Length - 1] - candidateTimes[0];

        // This variable stores the best feasible answer found so far.
        // Whenever a guessed gap is possible, we save it here and try larger.
        int best = 0;

        // ------------------------------------------------------------
        // STEP 2: Binary search on the answer.
        // ------------------------------------------------------------
        // Why binary search works:
        // - If a gap g is feasible, then every smaller gap is also feasible.
        // - If a gap g is not feasible, then every larger gap is also not feasible.
        //
        // This "true/false changes only once" pattern is exactly what binary
        // search on the answer is designed for.
        while (left <= right)
        {
            // Compute the middle gap safely.
            int mid = left + (right - left) / 2;

            // --------------------------------------------------------
            // STEP 3: Check whether it is possible to choose exactly k
            // ad breaks such that every consecutive chosen pair has
            // distance at least 'mid'.
            // --------------------------------------------------------
            if (CanPlaceWithMinimumGap(candidateTimes, k, mid))
            {
                // If 'mid' is feasible, it means:
                // - 'mid' is a valid minimum gap
                // - maybe we can do even better
                //
                // So we record it and search to the right for a larger answer.
                best = mid;
                left = mid + 1;
            }
            else
            {
                // If 'mid' is not feasible, then this gap is too large.
                // We must search smaller values.
                right = mid - 1;
            }
        }

        // After binary search finishes, 'best' holds the largest feasible gap.
        return best;
    }

    private bool CanPlaceWithMinimumGap(int[] candidateTimes, int k, int requiredGap)
    {
        // ------------------------------------------------------------
        // GREEDY FEASIBILITY CHECK
        // ------------------------------------------------------------
        // Goal:
        // Determine whether we can pick at least k positions from candidateTimes
        // so that each newly picked position is at least 'requiredGap' away from
        // the previously picked one.
        //
        // Why greedy works:
        // - We always pick the earliest possible valid candidate.
        // - Picking earlier leaves as much room as possible for future picks.
        // - Therefore, if this greedy strategy cannot place k ads, no other
        //   strategy can.
        //
        // This is a classic greedy argument used in "maximize minimum distance"
        // problems.

        // We always pick the first candidate time first.
        // Why?
        // - It is the earliest possible placement.
        // - Starting as early as possible gives maximum remaining space.
        int countChosen = 1;
        int lastChosen = candidateTimes[0];

        // Scan through the remaining candidate times from left to right.
        for (int i = 1; i < candidateTimes.Length; i++)
        {
            // Current candidate time we are considering.
            int current = candidateTimes[i];

            // Check whether current is far enough from the last chosen ad break.
            // We need:
            // current - lastChosen >= requiredGap
            //
            // If true, we can safely choose this position.
            if (current - lastChosen >= requiredGap)
            {
                countChosen++;
                lastChosen = current;

                // As soon as we have chosen k positions, the answer for this
                // feasibility check is true.
                if (countChosen >= k)
                {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k chosen positions,
        // then this required gap is not achievable.
        return false;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] candidateTimes1 = { 5, 11, 18, 26, 39 };
int k1 = 3;
int L1 = 45;
int result1 = solution.MaximizeMinimumBuffer(candidateTimes1, k1, L1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 13

// Example 2
int[] candidateTimes2 = { 2, 4, 7, 10, 14, 19 };
int k2 = 4;
int L2 = 20;
int result2 = solution.MaximizeMinimumBuffer(candidateTimes2, k2, L2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 5

// Additional demo
int[] candidateTimes3 = { 1, 2, 8, 12, 17, 25 };
int k3 = 3;
int L3 = 30;
int result3 = solution.MaximizeMinimumBuffer(candidateTimes3, k3, L3);
Console.WriteLine("Example 3 Result: " + result3); // One optimal choice: 1, 12, 25 => min gap = 11