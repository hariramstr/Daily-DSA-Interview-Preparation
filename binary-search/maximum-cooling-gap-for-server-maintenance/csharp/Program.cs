/*
Maximum Cooling Gap for Server Maintenance

You are given a sorted list of rack positions in a data center, where each position contains a server
that must be assigned to one of two maintenance crews. To reduce thermal interference, the company
wants the minimum distance between any two servers handled by the same crew to be as large as possible.

Formally, given an array positions of length n in strictly increasing order, partition the servers into
two groups A and B such that for each group, the distance between every pair of consecutive servers in
that group is at least D. Your task is to compute the maximum possible value of D for which such a
partition exists.

This is a decision-and-optimization problem: for a candidate D, determine whether the servers can be
2-colored so that no two adjacent servers in the same color class are closer than D, then find the
largest feasible D.

Constraints:
- 2 <= n <= 2 * 10^5
- 0 <= positions[i] <= 10^18
- positions is strictly increasing
- The answer fits in a 64-bit signed integer

Examples:
1) positions = [1, 2, 4, 8]
   answer = 3
   One valid partition for D = 3:
   crew A -> [1, 4, 8]
   crew B -> [2]

2) positions = [0, 3, 5, 9, 14]
   answer = 5
   One valid partition for D = 5:
   crew A -> [0, 5, 14]
   crew B -> [3, 9]
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
    - Binary search over the answer range: O(log(maxPosition - minPosition))
    - Feasibility check for one candidate D: O(n)
    - Total: O(n log R), where R = positions[n - 1] - positions[0]

    Space Complexity:
    - O(n) for the deque used by the feasibility check
    */
    public long MaximumCoolingGap(long[] positions)
    {
        int n = positions.Length;

        // The answer is at least 0.
        long low = 0;

        // The answer cannot exceed the total span from first to last position.
        long high = positions[n - 1] - positions[0];

        // Standard "find maximum feasible value" binary search.
        while (low < high)
        {
            // We bias upward so that when mid is feasible, we keep it.
            long mid = low + (high - low + 1) / 2;

            // If we can partition servers into two crews while keeping same-crew
            // consecutive gaps at least mid, then mid is feasible and we try bigger.
            if (CanPartitionWithGap(positions, mid))
            {
                low = mid;
            }
            else
            {
                // Otherwise mid is too large, so the answer is smaller.
                high = mid - 1;
            }
        }

        return low;
    }

    /*
    Core idea of the feasibility check:

    For a fixed D, define a graph on indices 0..n-1:
    connect i and j if positions[j] - positions[i] < D.

    Why does this graph matter?
    - If two servers are closer than D, they cannot belong to the same crew.
    - Therefore they must receive different colors.
    - So the question becomes: is this graph bipartite?

    Very important structural property:
    - Because positions is sorted, for each i the neighbors to the right form a contiguous range.
    - Thus the graph is a special interval-like graph, and we can test bipartiteness in linear time.

    We sweep from left to right and maintain the set of "active" earlier indices j such that
    positions[i] - positions[j] < D. These are exactly the previous vertices adjacent to i.

    In any valid 2-coloring:
    - All active neighbors of i must have the opposite color from i.
    - Therefore all active neighbors must themselves have the same color.
    - If active neighbors ever contain both colors, feasibility fails immediately.

    We maintain active vertices in index order inside a deque.
    Because the active set always forms a suffix of processed indices, removing expired vertices
    happens only from the front, and adding the new vertex happens at the back.

    We also keep counts of how many active vertices currently have color 0 and color 1.
    Then:
    - if both counts are positive -> impossible
    - if only color 0 is active -> current vertex must be color 1
    - if only color 1 is active -> current vertex must be color 0
    - if no active vertex exists -> current vertex can be arbitrarily color 0

    This greedy coloring is correct because when the active set is monochromatic, the current color
    is forced (or free if the active set is empty). No future decision can repair a violation where
    active neighbors already contain both colors.
    */
    private bool CanPartitionWithGap(long[] positions, long d)
    {
        int n = positions.Length;

        // color[i] will be 0 or 1.
        int[] color = new int[n];

        // We implement a deque using a plain array plus head/tail pointers.
        // It stores indices of currently active previous vertices.
        int[] deque = new int[n];
        int head = 0;
        int tail = 0; // elements are in deque[head..tail-1]

        // activeCountByColor[c] = how many active vertices currently have color c.
        int activeColor0 = 0;
        int activeColor1 = 0;

        for (int i = 0; i < n; i++)
        {
            // Step 1: Remove vertices that are no longer adjacent to i.
            //
            // A previous index j is adjacent to i exactly when positions[i] - positions[j] < d.
            // Once positions[i] - positions[j] >= d, j will never be adjacent to any later vertex either,
            // because positions only increase. So we can safely remove it forever.
            while (head < tail && positions[i] - positions[deque[head]] >= d)
            {
                int expired = deque[head++];
                if (color[expired] == 0)
                {
                    activeColor0--;
                }
                else
                {
                    activeColor1--;
                }
            }

            // Step 2: Check whether the active neighbors are already contradictory.
            //
            // Every active vertex is adjacent to i, so all of them must have the opposite color from i.
            // That is only possible if all active vertices share the same color.
            if (activeColor0 > 0 && activeColor1 > 0)
            {
                return false;
            }

            // Step 3: Assign a color to the current vertex.
            //
            // - If active neighbors are color 0, current must be color 1.
            // - If active neighbors are color 1, current must be color 0.
            // - If there are no active neighbors, current is unconstrained right now.
            //   We choose color 0 by default. This arbitrary choice is safe because the component
            //   has not yet imposed any restriction.
            if (activeColor0 > 0)
            {
                color[i] = 1;
            }
            else if (activeColor1 > 0)
            {
                color[i] = 0;
            }
            else
            {
                color[i] = 0;
            }

            // Step 4: Add the current vertex into the active set for future vertices.
            deque[tail++] = i;
            if (color[i] == 0)
            {
                activeColor0++;
            }
            else
            {
                activeColor1++;
            }
        }

        // If we never encountered a contradiction, the partition exists.
        return true;
    }
}

// Demo code
var solution = new Solution();

long[] positions1 = { 1, 2, 4, 8 };
long answer1 = solution.MaximumCoolingGap(positions1);
Console.WriteLine(answer1); // Expected: 3

long[] positions2 = { 0, 3, 5, 9, 14 };
long answer2 = solution.MaximumCoolingGap(positions2);
Console.WriteLine(answer2); // Expected: 5

// Additional quick sanity checks
long[] positions3 = { 0, 10 };
Console.WriteLine(solution.MaximumCoolingGap(positions3)); // Expected: 10

long[] positions4 = { 0, 1, 2 };
Console.WriteLine(solution.MaximumCoolingGap(positions4)); // Expected: 1