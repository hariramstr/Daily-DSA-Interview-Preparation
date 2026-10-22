/*
Title: Minimum Recharge Cost for a Courier Drone Route

Problem Description:
A courier drone must travel through a fixed sequence of delivery checkpoints numbered from 0 to n - 1.
At checkpoint i, the drone may buy an energy pack that allows it to fly forward up to jump[i] checkpoints,
and buying that pack costs cost[i].

The drone starts at checkpoint 0 and must reach checkpoint n - 1.
You may choose to buy a pack at any checkpoint you land on, including checkpoint 0.
After buying a pack at checkpoint i, the drone may move to any checkpoint j such that:
    i < j <= i + jump[i]

The drone does not need to buy packs at every checkpoint it visits.
If a checkpoint is unreachable, the route cannot be completed.

Return the minimum total recharge cost needed to reach checkpoint n - 1.
If it is impossible, return -1.

Important note:
To move forward from a checkpoint, the drone must buy the pack at that checkpoint.
Reaching the final checkpoint does not require buying a pack there, because the trip is already complete.

Why dynamic programming?
This is a shortest-path style optimization on a line:
- From each checkpoint i, if we are able to stand there, then after paying cost[i],
  we may move to any checkpoint in a forward interval.
- A greedy choice can fail because a cheap immediate move may force expensive future purchases,
  while a slightly more expensive earlier purchase may skip bad checkpoints.

Examples:
1)
cost = [5, 1, 4, 2]
jump = [2, 2, 1, 0]
Answer = 6

Reason:
- Buy at 0 for 5, move to 1
- Buy at 1 for 1, move to 3
Total = 6

2)
cost = [3, 2, 7, 1, 5]
jump = [1, 0, 2, 1, 0]
Answer = -1

Reason:
- From 0, only checkpoint 1 is reachable
- At 1, jump is 0, so no forward move is possible
- Final checkpoint cannot be reached
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
        O(n log n)

    Space Complexity:
        O(n)

    Beginner-friendly idea:
    -----------------------
    Let dp[i] mean:
        "The minimum total cost needed to ARRIVE at checkpoint i."

    We start at checkpoint 0 with:
        dp[0] = 0
    because we are already standing there before buying anything.

    Transition:
    If we are at checkpoint i and dp[i] is known, then we may choose to buy the pack at i.
    That costs cost[i], and then we can move to any checkpoint j in:
        i + 1 ... min(n - 1, i + jump[i])

    So for every such j:
        dp[j] = min(dp[j], dp[i] + cost[i])

    A direct implementation of this transition would be O(n^2), because each checkpoint
    could update many later checkpoints.

    To make it efficient, we process checkpoints from left to right and maintain a min-heap
    of "active offers".

    What is an offer?
    -----------------
    If we can stand at checkpoint i, then buying at i creates an offer:
        value = dp[i] + cost[i]
        valid for all destination checkpoints j where:
            i < j <= i + jump[i]

    That means this offer can help every checkpoint in a continuous interval:
        [i + 1, i + jump[i]]

    While scanning destination checkpoints j from left to right:
    - Add all offers created by earlier reachable checkpoints
    - Remove offers whose range ended before j
    - The cheapest remaining offer gives dp[j]

    This is a classic "minimum over active intervals" pattern.
    */
    public long MinimumRechargeCost(int[] cost, int[] jump)
    {
        int n = cost.Length;

        // If there is only one checkpoint, we are already at the destination.
        // No purchase is needed.
        if (n == 1)
        {
            return 0;
        }

        // We use a very large number to represent "currently unreachable".
        long inf = long.MaxValue / 4;

        // dp[i] = minimum total cost needed to arrive at checkpoint i.
        long[] dp = new long[n];
        Array.Fill(dp, inf);

        // We start at checkpoint 0 for free.
        dp[0] = 0;

        // Priority queue contents:
        // Each entry represents an active offer created by buying at some earlier checkpoint.
        //
        // For an offer:
        //   totalCost = dp[i] + cost[i]
        //   endIndex  = i + jump[i]
        //
        // This offer can be used to reach any checkpoint j such that:
        //   i < j <= endIndex
        //
        // We want the cheapest active offer at each checkpoint j,
        // so we use a min-heap ordered by totalCost.
        //
        // The element type is a tuple:
        //   (TotalCost, EndIndex)
        //
        // The priority is TotalCost.
        var pq = new PriorityQueue<(long TotalCost, int EndIndex), long>();

        // Before scanning future checkpoints, we may create an offer from checkpoint 0
        // if checkpoint 0 can actually move forward.
        //
        // Why is this necessary?
        // Because to reach checkpoint 1, 2, ... we need offers from earlier checkpoints.
        // Since we begin at checkpoint 0, its offer is the first possible one.
        if (jump[0] > 0)
        {
            int end = Math.Min(n - 1, 0 + jump[0]);
            long totalCost = dp[0] + cost[0];
            pq.Enqueue((totalCost, end), totalCost);
        }

        // Now process checkpoints from left to right.
        // At each checkpoint j, we determine the cheapest way to arrive there.
        for (int j = 1; j < n; j++)
        {
            // Remove all offers that are no longer valid for checkpoint j.
            //
            // An offer with EndIndex < j cannot reach j anymore,
            // because its allowed interval ended earlier.
            while (pq.Count > 0 && pq.Peek().EndIndex < j)
            {
                pq.Dequeue();
            }

            // After removing expired offers:
            // - If the heap is empty, then no earlier reachable checkpoint can send us to j.
            //   So j remains unreachable.
            // - Otherwise, the top of the heap is the cheapest active offer,
            //   which directly gives the minimum cost to arrive at j.
            if (pq.Count > 0)
            {
                dp[j] = pq.Peek().TotalCost;
            }

            // If checkpoint j is reachable, then from j we may create a new offer
            // for future checkpoints by buying the pack at j.
            //
            // Important:
            // We do NOT need to buy at j just because we arrived there.
            // But for dynamic programming, we record the option of buying at j
            // so future checkpoints can use it if beneficial.
            //
            // Also, if jump[j] == 0, then buying there cannot move anywhere,
            // so there is no future offer to add.
            if (dp[j] != inf && jump[j] > 0)
            {
                int end = Math.Min(n - 1, j + jump[j]);
                long totalCost = dp[j] + cost[j];
                pq.Enqueue((totalCost, end), totalCost);
            }
        }

        // If the final checkpoint is still unreachable, return -1.
        // Otherwise return the minimum cost to arrive there.
        return dp[n - 1] == inf ? -1 : dp[n - 1];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] cost1 = { 5, 1, 4, 2 };
int[] jump1 = { 2, 2, 1, 0 };
long result1 = solution.MinimumRechargeCost(cost1, jump1);
Console.WriteLine(result1); // Expected: 6

// Example 2
int[] cost2 = { 3, 2, 7, 1, 5 };
int[] jump2 = { 1, 0, 2, 1, 0 };
long result2 = solution.MinimumRechargeCost(cost2, jump2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks

// Already at destination
int[] cost3 = { 10 };
int[] jump3 = { 0 };
long result3 = solution.MinimumRechargeCost(cost3, jump3);
Console.WriteLine(result3); // Expected: 0

// Can jump directly to the end
int[] cost4 = { 7, 100, 100, 100 };
int[] jump4 = { 3, 0, 0, 0 };
long result4 = solution.MinimumRechargeCost(cost4, jump4);
Console.WriteLine(result4); // Expected: 7