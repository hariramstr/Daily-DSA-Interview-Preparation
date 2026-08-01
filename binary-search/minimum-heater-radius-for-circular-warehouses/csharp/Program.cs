/*
Title: Minimum Heater Radius for Circular Warehouses
Difficulty: Medium
Topic: Binary Search

Problem Description:
A logistics company stores goods in warehouses placed around a circular ring road of total length L.
The positions of the warehouses are given as integers in the range [0, L - 1], measured clockwise
from a fixed origin. The company wants to install heaters at some existing warehouse locations.

Each heater warms all warehouses within clockwise or counterclockwise road distance at most R,
where distance on the ring is the shorter of the two circular paths.

You are given a sorted array warehouses of unique warehouse positions, an integer L, and an integer k
representing the maximum number of heaters that may be installed. Return the minimum integer radius R
such that all warehouses can be covered by at most k heaters.

A heater may only be placed at one of the given warehouse positions. Coverage wraps around the circle,
so a heater near position 0 may also cover warehouses near position L - 1.

Constraints:
- 1 <= k <= n <= 2 * 10^5
- 1 <= L <= 10^9
- 0 <= warehouses[i] < L
- warehouses is sorted in strictly increasing order
- All answers fit in a 32-bit signed integer

Examples:
1) warehouses = [1, 4, 8, 11], L = 12, k = 2
   Output: 2

2) warehouses = [2, 6, 9, 14], L = 20, k = 1
   Output: 6
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Binary search over the answer R: O(log L)
    - Each feasibility check:
        * Build jump table: O(n log n)
        * Try all possible starting cut positions using binary lifting: O(n log n)
      So each check is O(n log n)
    - Total: O(n log n log L)

    Space Complexity:
    - O(n log n) for the binary lifting jump table and duplicated arrays

    Beginner-friendly high-level idea:
    ----------------------------------
    This is a "binary search on the answer" problem.

    If a radius R is enough to cover all warehouses using at most k heaters,
    then any larger radius is also enough. That monotonic property lets us binary search
    for the minimum valid R.

    The hard part is the feasibility check on a circle.

    Key transformation:
    - A heater placed at warehouse position p covers all points on the circle whose circular
      distance to p is at most R.
    - On the circle, that means a continuous arc of length 2R centered at p.
    - If we "cut" the circle at some place and unwrap it into a line, then the problem becomes:
      cover a consecutive sequence of warehouse points on a line using at most k intervals,
      where each interval must be centered at one of the warehouse positions and has radius R.

    For a fixed linear order of points:
    - If we start from the leftmost uncovered warehouse at index i,
      the best greedy choice is to place a heater at the farthest warehouse position <= warehouses[i] + R.
      Why? Because such a heater still covers warehouses[i], and among all heaters that can cover it,
      this one reaches farthest to the right, up to position heater + R.
    - This greedy step is optimal for interval covering on a line.

    To handle the circle efficiently:
    - Duplicate the warehouse positions by appending each position + L.
      This lets every circular segment of n consecutive warehouses appear as a normal linear segment.
    - For each possible starting warehouse s (which represents where we cut the circle),
      we need to know whether k greedy heater placements can cover warehouses from s to s + n - 1.
    - We precompute "next index after one heater" for every starting index.
    - Then we use binary lifting to jump k times quickly.

    This approach is correct for the examples:
    - Example 1: answer is 2
    - Example 2: answer is 6
    */
    public int MinHeaterRadius(int[] warehouses, int L, int k)
    {
        int n = warehouses.Length;

        // Trivial case:
        // If we are allowed to place at least one heater per warehouse,
        // radius 0 is enough because a heater can be placed exactly at each warehouse.
        if (k >= n)
        {
            return 0;
        }

        // Binary search boundaries:
        // - Minimum possible radius is 0.
        // - Maximum useful radius can be L, which is certainly enough.
        //   (In practice L/2 is enough for arbitrary points on a circle, but L is a simple safe upper bound.)
        int left = 0;
        int right = L;

        while (left < right)
        {
            int mid = left + (right - left) / 2;

            // If radius mid is feasible, try to find an even smaller radius.
            if (CanCoverAll(warehouses, L, k, mid))
            {
                right = mid;
            }
            else
            {
                // Otherwise we must increase the radius.
                left = mid + 1;
            }
        }

        return left;
    }

    private bool CanCoverAll(int[] warehouses, int L, int k, int R)
    {
        int n = warehouses.Length;
        int m = 2 * n;

        // Step 1: Duplicate the circular positions into a linear array.
        //
        // Why this is necessary:
        // On a circle, coverage can wrap around from near L-1 back to near 0.
        // Duplicating positions as [x0, x1, ..., x(n-1), x0+L, x1+L, ..., x(n-1)+L]
        // lets us represent any circular run of n warehouses as one contiguous segment on a line.
        //
        // Example:
        // warehouses = [1, 4, 8, 11], L = 12
        // duplicated = [1, 4, 8, 11, 13, 16, 20, 23]
        //
        // If we "cut" the circle just before warehouse 8, then the circular order
        // [8, 11, 1, 4] becomes the linear segment [8, 11, 13, 16].
        long[] extended = new long[m];
        for (int i = 0; i < n; i++)
        {
            extended[i] = warehouses[i];
            extended[i + n] = (long)warehouses[i] + L;
        }

        // Step 2: For each index i in the duplicated array, compute where one greedy heater takes us.
        //
        // Meaning of nextIndex[i]:
        // Starting with warehouse i as the leftmost uncovered warehouse on the line,
        // place one heater optimally (greedy), and then nextIndex[i] is the first warehouse
        // index that remains uncovered after that heater.
        //
        // Greedy rule on a line:
        // - The leftmost uncovered warehouse is at position extended[i].
        // - To cover it, the heater center must be at a warehouse position <= extended[i] + R.
        // - Among all such warehouse positions, choose the farthest one to the right.
        //   This maximizes how far right the heater can cover.
        // - If the chosen heater is at position h, it covers up to h + R.
        // - Therefore all warehouses with position <= h + R become covered.
        //
        // We can compute this efficiently with two pointers because the array is sorted.
        int[] nextIndex = new int[m];
        int centerPtr = 0; // farthest warehouse index usable as heater center for current i
        int coverPtr = 0;  // first warehouse index beyond the right coverage limit

        for (int i = 0; i < m; i++)
        {
            if (centerPtr < i) centerPtr = i;
            if (coverPtr < i) coverPtr = i;

            long leftmost = extended[i];

            // Move centerPtr to the farthest warehouse whose position is <= leftmost + R.
            // Such a warehouse can still cover the leftmost uncovered warehouse.
            while (centerPtr + 1 < m && extended[centerPtr + 1] <= leftmost + R)
            {
                centerPtr++;
            }

            // The heater is placed at extended[centerPtr].
            // It covers up to extended[centerPtr] + R.
            long rightReach = extended[centerPtr] + R;

            // Move coverPtr to the first warehouse strictly beyond rightReach.
            while (coverPtr < m && extended[coverPtr] <= rightReach)
            {
                coverPtr++;
            }

            nextIndex[i] = coverPtr;
        }

        // Step 3: Build binary lifting table.
        //
        // Why this is necessary:
        // We need to know whether k heaters can cover n consecutive warehouses,
        // for every possible starting cut position s.
        //
        // If we repeatedly apply nextIndex:
        // - after 1 heater: nextIndex[s]
        // - after 2 heaters: nextIndex[nextIndex[s]]
        // - after 4 heaters: ...
        //
        // Binary lifting lets us jump many heater placements in O(log k) time instead of O(k),
        // which is crucial for large constraints.
        int maxLog = 1;
        while ((1L << maxLog) <= k) maxLog++;

        int[][] jump = new int[maxLog][];
        jump[0] = nextIndex;

        for (int p = 1; p < maxLog; p++)
        {
            jump[p] = new int[m + 1];

            for (int i = 0; i < m; i++)
            {
                int mid = jump[p - 1][i];

                // If one half-jump already goes beyond the array, clamp safely.
                // In practice, indices remain within [0, m], but this makes the code robust.
                jump[p][i] = mid < m ? jump[p - 1][mid] : m;
            }
        }

        // Step 4: Try every possible starting warehouse as the cut point of the circle.
        //
        // For a starting index s in [0, n-1], the n warehouses on the circle correspond to
        // the linear segment of indices [s, s + n - 1] in the duplicated array.
        //
        // We ask:
        // After using at most k heaters greedily starting from s,
        // do we reach index >= s + n ?
        //
        // If yes for any s, then radius R is feasible on the circle.
        for (int s = 0; s < n; s++)
        {
            int current = s;
            int remaining = k;
            int bit = 0;

            // Apply exactly k greedy heater placements using binary lifting.
            while (remaining > 0)
            {
                if ((remaining & 1) != 0)
                {
                    current = current < m ? jump[bit][current] : m;
                }

                remaining >>= 1;
                bit++;
            }

            // If we have advanced past the last warehouse in this length-n segment,
            // then all n warehouses are covered.
            if (current >= s + n)
            {
                return true;
            }
        }

        return false;
    }
}

// Demo code requested by the problem statement.
// This is fully runnable and prints the sample outputs.

var solution = new Solution();

int[] warehouses1 = { 1, 4, 8, 11 };
int L1 = 12;
int k1 = 2;
int result1 = solution.MinHeaterRadius(warehouses1, L1, k1);
Console.WriteLine(result1); // Expected: 2

int[] warehouses2 = { 2, 6, 9, 14 };
int L2 = 20;
int k2 = 1;
int result2 = solution.MinHeaterRadius(warehouses2, L2, k2);
Console.WriteLine(result2); // Expected: 6