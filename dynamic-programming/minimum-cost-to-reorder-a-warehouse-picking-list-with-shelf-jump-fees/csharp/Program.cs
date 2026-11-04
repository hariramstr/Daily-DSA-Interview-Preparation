/*
Minimum Cost to Reorder a Warehouse Picking List with Shelf Jump Fees

Problem Description:
A warehouse robot must collect items in the exact order given by a picking list. The warehouse has shelves labeled from 1 to m, and each item type may appear on several shelves. For the i-th request in the list, you are given all shelves that currently contain a valid copy of that requested item. The robot starts before processing the first request and may begin on any shelf that stores the first requested item without paying a movement cost.

If the robot picks the i-th item from shelf a and the (i+1)-th item from shelf b, then it pays a movement cost of |a - b|. In addition, changing movement direction is expensive: if the previous move was to a higher-numbered shelf and the next move is to a lower-numbered shelf, or vice versa, an extra turn fee T is paid for that transition. The very first move does not incur a turn fee because there is no previous direction yet. Staying on the same shelf counts as direction 0 and does not itself create a turn; however, after staying still, the next nonzero move is compared against the last nonzero direction used.

Your task is to compute the minimum total cost to fulfill the entire picking list.

Formally, you are given an array positions where positions[i] is a sorted list of distinct shelf numbers that can satisfy the i-th request, and an integer T. Choose one shelf from each positions[i] so that the sum of movement costs and turn fees is minimized.

Constraints:
- 1 <= n <= 2 * 10^5, where n = positions.length
- 1 <= total number of shelf entries across all positions[i] <= 2 * 10^5
- 1 <= shelf number <= 10^9
- 0 <= T <= 10^9
- Each positions[i] is non-empty, sorted, and contains distinct integers
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Time Complexity:
    // Let S be the total number of shelf entries across all requests.
    // The algorithm runs in O(S) total time because each transition between consecutive
    // request lists is processed with linear two-pointer style scans over the two lists.
    //
    // Space Complexity:
    // O(max size of one request list) for the DP arrays of the current and previous layer.
    //
    // Important idea:
    // For every chosen shelf at the current request, we keep three DP values:
    //   dpNone[pos] = minimum cost if we are at this shelf and have not made any nonzero move yet
    //   dpUp[pos]   = minimum cost if we are at this shelf and the last nonzero move direction is upward
    //   dpDown[pos] = minimum cost if we are at this shelf and the last nonzero move direction is downward
    //
    // Why these three states are enough:
    // The future only cares about the last nonzero direction, because:
    // - movement cost depends only on current shelf and next shelf
    // - turn fee depends only on whether the next nonzero move changes direction
    // - zero moves do not erase the last nonzero direction
    //
    // The challenge:
    // A naive transition from every previous shelf to every current shelf would be quadratic.
    // We avoid that by exploiting sorted shelf lists and rewriting each transition as a minimum
    // of expressions that can be maintained by prefix/suffix minima.
    public long MinCost(IList<IList<int>> positions, long T)
    {
        int n = positions.Count;
        if (n == 1)
        {
            // If there is only one request, we may start directly on any valid shelf for free.
            return 0;
        }

        const long INF = long.MaxValue / 4;

        // For the first request:
        // We can start on any valid shelf with zero cost.
        // No move has happened yet, so the "last nonzero direction" does not exist.
        // Therefore:
        //   dpNone = 0 for every shelf in positions[0]
        //   dpUp   = INF
        //   dpDown = INF
        int[] prevPos = ToArray(positions[0]);
        long[] prevNone = new long[prevPos.Length];
        long[] prevUp = new long[prevPos.Length];
        long[] prevDown = new long[prevPos.Length];

        for (int i = 0; i < prevPos.Length; i++)
        {
            prevNone[i] = 0;
            prevUp[i] = INF;
            prevDown[i] = INF;
        }

        // Process requests one by one.
        for (int step = 1; step < n; step++)
        {
            int[] curPos = ToArray(positions[step]);
            int mPrev = prevPos.Length;
            int mCur = curPos.Length;

            long[] curNone = new long[mCur];
            long[] curUp = new long[mCur];
            long[] curDown = new long[mCur];

            for (int i = 0; i < mCur; i++)
            {
                curNone[i] = INF;
                curUp[i] = INF;
                curDown[i] = INF;
            }

            // ------------------------------------------------------------
            // Transition 1: zero move (same shelf value exists in both lists)
            // ------------------------------------------------------------
            //
            // If prev shelf == current shelf, movement cost is 0 and direction is 0.
            // A zero move does NOT create a new nonzero direction and does NOT add turn fee.
            // So the direction state simply stays the same.
            //
            // Because both lists are sorted and distinct, we can find equal values with two pointers.
            int p1 = 0, p2 = 0;
            while (p1 < mPrev && p2 < mCur)
            {
                if (prevPos[p1] == curPos[p2])
                {
                    curNone[p2] = Math.Min(curNone[p2], prevNone[p1]);
                    curUp[p2] = Math.Min(curUp[p2], prevUp[p1]);
                    curDown[p2] = Math.Min(curDown[p2], prevDown[p1]);
                    p1++;
                    p2++;
                }
                else if (prevPos[p1] < curPos[p2])
                {
                    p1++;
                }
                else
                {
                    p2++;
                }
            }

            // ------------------------------------------------------------
            // Transition 2: upward move (prev shelf < current shelf)
            // ------------------------------------------------------------
            //
            // Suppose we move from previous shelf x to current shelf y with x < y.
            // Then movement cost is y - x and the new last nonzero direction becomes UP.
            //
            // What previous states can lead here?
            // - prevNone: first nonzero move, no turn fee
            // - prevUp: continue upward, no turn fee
            // - prevDown: direction changes from down to up, so add T
            //
            // Therefore:
            // curUp[y] = min over x < y of
            //   min(
            //     prevNone[x] + (y - x),
            //     prevUp[x]   + (y - x),
            //     prevDown[x] + T + (y - x)
            //   )
            //
            // Rewrite:
            // curUp[y] = y + min over x < y of
            //   min(
            //     prevNone[x] - x,
            //     prevUp[x]   - x,
            //     prevDown[x] + T - x
            //   )
            //
            // Since prevPos is sorted and curPos is sorted, we can sweep left to right.
            // We maintain the best value among all previous shelves strictly less than current y.
            long bestUpBase = INF;
            int iPrev = 0;
            for (int iCur = 0; iCur < mCur; iCur++)
            {
                int y = curPos[iCur];

                while (iPrev < mPrev && prevPos[iPrev] < y)
                {
                    long x = prevPos[iPrev];

                    bestUpBase = Math.Min(bestUpBase, prevNone[iPrev] - x);
                    bestUpBase = Math.Min(bestUpBase, prevUp[iPrev] - x);
                    bestUpBase = Math.Min(bestUpBase, prevDown[iPrev] + T - x);

                    iPrev++;
                }

                if (bestUpBase < INF)
                {
                    curUp[iCur] = Math.Min(curUp[iCur], bestUpBase + y);
                }
            }

            // ------------------------------------------------------------
            // Transition 3: downward move (prev shelf > current shelf)
            // ------------------------------------------------------------
            //
            // Suppose we move from previous shelf x to current shelf y with x > y.
            // Then movement cost is x - y and the new last nonzero direction becomes DOWN.
            //
            // What previous states can lead here?
            // - prevNone: first nonzero move, no turn fee
            // - prevDown: continue downward, no turn fee
            // - prevUp: direction changes from up to down, so add T
            //
            // Therefore:
            // curDown[y] = min over x > y of
            //   min(
            //     prevNone[x] + (x - y),
            //     prevDown[x] + (x - y),
            //     prevUp[x]   + T + (x - y)
            //   )
            //
            // Rewrite:
            // curDown[y] = -y + min over x > y of
            //   min(
            //     prevNone[x] + x,
            //     prevDown[x] + x,
            //     prevUp[x]   + T + x
            //   )
            //
            // We can compute this with a right-to-left sweep.
            long bestDownBase = INF;
            iPrev = mPrev - 1;
            for (int iCur = mCur - 1; iCur >= 0; iCur--)
            {
                int y = curPos[iCur];

                while (iPrev >= 0 && prevPos[iPrev] > y)
                {
                    long x = prevPos[iPrev];

                    bestDownBase = Math.Min(bestDownBase, prevNone[iPrev] + x);
                    bestDownBase = Math.Min(bestDownBase, prevDown[iPrev] + x);
                    bestDownBase = Math.Min(bestDownBase, prevUp[iPrev] + T + x);

                    iPrev--;
                }

                if (bestDownBase < INF)
                {
                    curDown[iCur] = Math.Min(curDown[iCur], bestDownBase - y);
                }
            }

            // Move current layer into previous layer for the next iteration.
            prevPos = curPos;
            prevNone = curNone;
            prevUp = curUp;
            prevDown = curDown;
        }

        // Final answer:
        // After processing all requests, we may end in any shelf and any direction-state.
        long answer = INF;
        for (int i = 0; i < prevPos.Length; i++)
        {
            answer = Math.Min(answer, prevNone[i]);
            answer = Math.Min(answer, prevUp[i]);
            answer = Math.Min(answer, prevDown[i]);
        }

        return answer;
    }

    private static int[] ToArray(IList<int> list)
    {
        int[] arr = new int[list.Count];
        for (int i = 0; i < list.Count; i++)
        {
            arr[i] = list[i];
        }
        return arr;
    }
}

// Demo code

var solution = new Solution();

// Example 1
var positions1 = new List<IList<int>>
{
    new List<int> { 2, 8 },
    new List<int> { 3 },
    new List<int> { 7, 10 },
    new List<int> { 6 }
};
long T1 = 5;
long result1 = solution.MinCost(positions1, T1);
Console.WriteLine(result1);

// Example 2
var positions2 = new List<IList<int>>
{
    new List<int> { 1, 5 },
    new List<int> { 4, 6 },
    new List<int> { 2, 8 },
    new List<int> { 3 }
};
long T2 = 2;
long result2 = solution.MinCost(positions2, T2);
Console.WriteLine(result2);

// Additional small sanity checks

// Single request: start directly there for free
var positions3 = new List<IList<int>>
{
    new List<int> { 10, 20, 30 }
};
long T3 = 100;
Console.WriteLine(solution.MinCost(positions3, T3)); // expected 0

// Repeated same shelf, all zero moves
var positions4 = new List<IList<int>>
{
    new List<int> { 5 },
    new List<int> { 5 },
    new List<int> { 5 }
};
long T4 = 7;
Console.WriteLine(solution.MinCost(positions4, T4)); // expected 0