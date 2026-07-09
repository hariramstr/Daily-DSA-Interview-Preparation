/*
Title: Minimum Cost to Segment a Route into Rechargeable Legs

Problem Description:
A delivery drone must travel through a sequence of checkpoints from west to east.
The checkpoints are numbered from 0 to n - 1, and the drone starts at checkpoint 0
and must end at checkpoint n - 1.

The distance between checkpoint i - 1 and checkpoint i is given by dist[i] for 1 <= i < n,
so the total distance of a leg from checkpoint j to checkpoint i is the sum of distances between them.

The drone cannot recharge while flying. Instead, you may choose some checkpoints as recharge stops.
If the drone flies one leg whose total distance is L, then that leg costs batteryCost * L^2 units
of energy because longer uninterrupted flights are disproportionately expensive.

In addition, every checkpoint i used as a recharge stop (except checkpoint 0 and checkpoint n - 1)
adds a fixed service fee fee[i].

Return the minimum total cost to reach checkpoint n - 1.

Formally, choose an increasing sequence of checkpoints 0 = p0 < p1 < ... < pk = n - 1.
The total cost is:
1. Sum over all legs t from 1 to k of batteryCost * (distance from p(t-1) to p(t))^2
2. Plus the sum of fee[p1] + fee[p2] + ... + fee[p(k-1)]

You need to compute the minimum possible total cost.

Constraints:
- 2 <= n <= 200000
- 1 <= dist[i] <= 10^6 for 1 <= i < n
- 0 <= fee[i] <= 10^12 for 0 <= i < n
- fee[0] = fee[n - 1] = 0
- 1 <= batteryCost <= 10^6
- The answer fits in a signed 64-bit integer.
*/

using System;
using System.Collections.Generic;

class Solution
{
    // Time Complexity: O(n log n)
    // Space Complexity: O(n)
    //
    // Idea:
    // Let pos[i] be the prefix distance from checkpoint 0 to checkpoint i.
    //
    // Define dp[i] = minimum total cost to reach checkpoint i.
    //
    // Transition:
    // dp[i] = min over j < i of:
    //         dp[j] + batteryCost * (pos[i] - pos[j])^2 + (i == n - 1 ? 0 : fee[i])
    //
    // Expand the square:
    // dp[i] = batteryCost * pos[i]^2 + (i == n - 1 ? 0 : fee[i]) +
    //         min over j < i of:
    //         (dp[j] + batteryCost * pos[j]^2 - 2 * batteryCost * pos[j] * pos[i])
    //
    // For fixed j, this is a line in x = pos[i]:
    // y = m * x + b
    // where:
    // m = -2 * batteryCost * pos[j]
    // b = dp[j] + batteryCost * pos[j]^2
    //
    // So we need the minimum value among lines at increasing x values.
    // Since pos[i] is strictly increasing (all dist[i] >= 1), slopes are added in decreasing order,
    // and queries are also in increasing x order. This allows a classic Convex Hull Trick deque
    // with amortized O(1) per insertion/query, giving total O(n).
    //
    // We still write it carefully using integer arithmetic with long.
    public long MinimumCost(int n, int[] dist, long[] fee, long batteryCost)
    {
        // Step 1:
        // Build prefix positions.
        //
        // pos[i] = total distance from checkpoint 0 to checkpoint i.
        // This converts "distance between checkpoints" into a coordinate on a line.
        // Then the distance from checkpoint j to checkpoint i is simply:
        // pos[i] - pos[j]
        //
        // This is necessary because our DP transition depends on leg length,
        // and prefix sums let us compute any leg length in O(1).
        long[] pos = new long[n];
        for (int i = 1; i < n; i++)
        {
            pos[i] = pos[i - 1] + dist[i];
        }

        // Step 2:
        // dp[i] will store the minimum cost to arrive at checkpoint i.
        long[] dp = new long[n];
        dp[0] = 0;

        // Step 3:
        // Prepare the Convex Hull Trick structure.
        //
        // Each previous checkpoint j contributes one line:
        // y = m * x + b
        // m = -2 * batteryCost * pos[j]
        // b = dp[j] + batteryCost * pos[j] * pos[j]
        //
        // When we compute dp[i], we query the hull at x = pos[i].
        //
        // Because:
        // dp[i] = batteryCost * pos[i]^2 + extraFee + minLineValue(pos[i])
        //
        // We use a deque-like list with a moving head pointer.
        // This is efficient because:
        // 1. Slopes are inserted in monotonic order.
        // 2. Query x values are also monotonic.
        var hull = new List<Line>(n);
        int head = 0;

        // Insert the line corresponding to j = 0.
        AddLine(hull, new Line(
            m: -2L * batteryCost * pos[0],
            b: dp[0] + batteryCost * pos[0] * pos[0]
        ));

        // Step 4:
        // Process checkpoints from left to right.
        //
        // For each checkpoint i:
        // - Query the best previous recharge point j
        // - Compute dp[i]
        // - Insert the line for this i so future checkpoints can use it
        for (int i = 1; i < n; i++)
        {
            long x = pos[i];

            // Step 4a:
            // Query the hull for the minimum line value at x = pos[i].
            //
            // Since x values are increasing, if the next line is better than the current line,
            // we can permanently move the head forward.
            while (head + 1 < hull.Count && hull[head + 1].ValueAt(x) <= hull[head].ValueAt(x))
            {
                head++;
            }

            long best = hull[head].ValueAt(x);

            // Step 4b:
            // Add the fixed fee of checkpoint i only if it is an intermediate recharge stop.
            //
            // Important detail:
            // The fee is paid when checkpoint i is chosen as a stop.
            // In this DP formulation, every time we "arrive" at i and may continue later,
            // we include fee[i], except for the final destination n - 1 where no recharge is needed.
            long extraFee = (i == n - 1) ? 0L : fee[i];

            // Step 4c:
            // Compute dp[i] using the transformed formula.
            dp[i] = batteryCost * x * x + extraFee + best;

            // Step 4d:
            // Insert the line representing transitions from checkpoint i to future checkpoints.
            //
            // This line encodes:
            // dp[i] + batteryCost * pos[i]^2 - 2 * batteryCost * pos[i] * futureX
            //
            // We only need to add it after dp[i] is known.
            AddLine(hull, new Line(
                m: -2L * batteryCost * x,
                b: dp[i] + batteryCost * x * x
            ));

            // If head has fallen behind due to removals from the back, it remains valid.
            // No extra adjustment is needed because we only remove from the back.
        }

        return dp[n - 1];
    }

    // Adds a new line to the convex hull while preserving the lower hull for minimum queries.
    //
    // We remove the previous last line if it becomes useless after adding the new line.
    // This is the standard convex hull trick maintenance for monotonic slopes.
    private static void AddLine(List<Line> hull, Line line)
    {
        while (hull.Count >= 2 && IsBad(hull[hull.Count - 2], hull[hull.Count - 1], line))
        {
            hull.RemoveAt(hull.Count - 1);
        }
        hull.Add(line);
    }

    // Returns true if line b is never better than line a or line c,
    // meaning b can be removed from the hull.
    //
    // We avoid floating point arithmetic by cross multiplication.
    //
    // For minimum hull with monotonic slopes:
    // intersection(a, b) >= intersection(b, c)  => b is useless
    //
    // (b.b - a.b) / (a.m - b.m) >= (c.b - b.b) / (b.m - c.m)
    //
    // Cross multiply carefully using Int128 to avoid overflow.
    private static bool IsBad(Line a, Line b, Line c)
    {
        Int128 left = (Int128)(b.B - a.B) * (b.M - c.M);
        Int128 right = (Int128)(c.B - b.B) * (a.M - b.M);
        return left >= right;
    }

    private readonly struct Line
    {
        public long M { get; }
        public long B { get; }

        public Line(long m, long b)
        {
            M = m;
            B = b;
        }

        public long ValueAt(long x)
        {
            return M * x + B;
        }
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1
int n1 = 5;
int[] dist1 = { 0, 3, 2, 4, 2 };
long[] fee1 = { 0, 5, 1, 6, 0 };
long batteryCost1 = 1;
long result1 = solution.MinimumCost(n1, dist1, fee1, batteryCost1);
Console.WriteLine(result1);

// Example 2
int n2 = 4;
int[] dist2 = { 0, 1, 1, 1 };
long[] fee2 = { 0, 100, 100, 0 };
long batteryCost2 = 2;
long result2 = solution.MinimumCost(n2, dist2, fee2, batteryCost2);
Console.WriteLine(result2);