/*
Title: Minimum Lane Changes to Collect Ordered Checkpoints

Problem Description:
You are given a straight road divided into n positions, numbered from 0 to n - 1, and exactly 3 lanes numbered 1 to 3.
A vehicle starts at position 0 in lane 2. Some positions contain a checkpoint token in one of the lanes, and some positions
may also contain a blocked lane due to road work.

Two arrays are provided:

- checkpoints, where checkpoints[i] is either 0 (no token at position i) or a lane number 1..3 indicating that a token
  must be collected at that position.
- blocked, where blocked[i] is either 0 (no blocked lane at position i) or a lane number 1..3 indicating that lane cannot
  be occupied at that position.

The vehicle moves from left to right, one position at a time. At each step, it may stay in the same lane or switch to another
lane before entering the next position. Every lane switch costs 1. Moving forward costs 0. A token at position i is collected
only if the vehicle is in checkpoints[i] when passing that position. You must collect all required tokens in increasing position
order, which happens automatically if you visit the required lane at each token position.

Return the minimum number of lane changes needed to reach position n - 1 while collecting every token. If it is impossible,
return -1.

Constraints:
- 1 <= n <= 100000
- checkpoints.length == blocked.length == n
- checkpoints[i] is in {0, 1, 2, 3}
- blocked[i] is in {0, 1, 2, 3}
- If checkpoints[i] != 0, then checkpoints[i] != blocked[i]
- blocked[0] != 2
*/

using System;

public class Solution
{
    // Time Complexity: O(n)
    // Space Complexity: O(1)
    public int MinLaneChanges(int[] checkpoints, int[] blocked)
    {
        // We use a very large number to represent an impossible state.
        // This is safer than int.MaxValue because later we may add 1 to a value,
        // and we do not want integer overflow.
        const int INF = 1_000_000_000;

        int n = checkpoints.Length;

        // Defensive check: if the arrays do not have the same length, the input is invalid.
        // The problem guarantees equal lengths, but this makes the method more robust.
        if (blocked.Length != n)
        {
            return -1;
        }

        // dp[lane] will mean:
        // "the minimum number of lane changes needed to be at the CURRENT position
        //  in this lane, while satisfying all token requirements up to this position."
        //
        // We will use indexes 1..3 for convenience because lane numbers are also 1..3.
        // Index 0 is unused.
        int[] dp = new int[4] { INF, INF, INF, INF };

        // Initial state at position 0:
        // The vehicle starts in lane 2 at position 0 with cost 0.
        // Lanes 1 and 3 are not reachable at position 0 because the problem states
        // lane changes happen before entering the next position, not at the starting position itself.
        dp[2] = 0;

        // Before processing movement, we must validate position 0 itself.
        //
        // Why?
        // Because the vehicle is already standing at position 0 in lane 2.
        // So:
        // - if lane 2 is blocked at position 0, the route is impossible immediately
        // - if there is a token at position 0 in a lane other than 2, we cannot collect it
        //   because we do not get a chance to switch lanes before position 0
        if (blocked[0] == 2)
        {
            return -1;
        }

        if (checkpoints[0] != 0 && checkpoints[0] != 2)
        {
            return -1;
        }

        // If position 0 has a token in lane 2, that is fine because we start there.
        // No extra action is needed.

        // Now process positions from 1 to n - 1.
        // At each new position:
        // 1. We decide which lane we enter.
        // 2. Entering the same lane costs 0.
        // 3. Switching from one lane to another before entering this position costs 1.
        // 4. The chosen lane must not be blocked at this position.
        // 5. If there is a token at this position, we must be in that exact lane.
        for (int pos = 1; pos < n; pos++)
        {
            // next[lane] will store the minimum cost to be in each lane at this new position.
            int[] next = new int[4] { INF, INF, INF, INF };

            // We try every possible previous lane.
            for (int prevLane = 1; prevLane <= 3; prevLane++)
            {
                // If previous state is impossible, skip it.
                if (dp[prevLane] == INF)
                {
                    continue;
                }

                // From the previous lane, we can choose any current lane 1..3.
                // Staying in the same lane costs 0.
                // Switching to a different lane costs 1.
                for (int currLane = 1; currLane <= 3; currLane++)
                {
                    // Step 1: respect the blocked lane at this position.
                    //
                    // If currLane is blocked at this position, we are not allowed
                    // to occupy it, so this transition is invalid.
                    if (blocked[pos] == currLane)
                    {
                        continue;
                    }

                    // Step 2: respect the checkpoint requirement at this position.
                    //
                    // If there is a token here, we must pass through exactly that lane.
                    // Any other lane would fail to collect the token, so it is invalid.
                    if (checkpoints[pos] != 0 && checkpoints[pos] != currLane)
                    {
                        continue;
                    }

                    // Step 3: compute the transition cost.
                    //
                    // - same lane: no lane change, cost +0
                    // - different lane: one lane change before entering this position, cost +1
                    int cost = dp[prevLane] + (prevLane == currLane ? 0 : 1);

                    // Step 4: keep the best cost for reaching currLane at this position.
                    if (cost < next[currLane])
                    {
                        next[currLane] = cost;
                    }
                }
            }

            // Move to the next position by replacing dp with next.
            dp = next;
        }

        // At the final position, we may end in any lane that is valid and has collected all tokens.
        // So the answer is the minimum among dp[1], dp[2], dp[3].
        int answer = Math.Min(dp[1], Math.Min(dp[2], dp[3]));

        // If all lanes are impossible, return -1.
        return answer >= INF ? -1 : answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] checkpoints1 = { 0, 0, 1, 0, 3 };
int[] blocked1 = { 0, 3, 0, 2, 0 };
int result1 = solution.MinLaneChanges(checkpoints1, blocked1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] checkpoints2 = { 0, 2, 0, 1 };
int[] blocked2 = { 0, 0, 2, 1 };
int result2 = solution.MinLaneChanges(checkpoints2, blocked2);
Console.WriteLine(result2); // Expected: -1