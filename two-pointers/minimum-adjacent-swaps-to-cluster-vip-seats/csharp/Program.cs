/*
Title: Minimum Adjacent Swaps to Cluster VIP Seats

Problem Description:
You are given a row of seats represented by a binary array seats, where seats[i] = 1
means seat i is occupied by a VIP guest and seats[i] = 0 means it is occupied by a
regular guest. In one operation, you may swap two adjacent guests. Your goal is to
make all VIP guests sit in one contiguous block using the minimum total number of
adjacent swaps.

Unlike simply counting empty seats between VIPs, each adjacent swap moves exactly one
guest by one position, so the true cost depends on how far each VIP must travel after
choosing an optimal final block. You must return the minimum number of adjacent swaps
required.

If there are 0 or 1 VIP guests, the answer is 0.

A correct hard solution is expected to exploit the structure of the VIP positions and
avoid simulating swaps. In particular, the optimal block is centered around the median
VIP after normalizing positions, and an efficient implementation should run in linear
time after collecting VIP indices.

Constraints:
- 1 <= seats.length <= 200000
- seats[i] is either 0 or 1
- The answer fits in a 64-bit signed integer

Example 1:
Input: seats = [1,0,0,1,0,1]
Output: 3
Explanation: VIPs are at positions [0,3,5]. After rearranging with adjacent swaps,
they can be grouped into seats [2,3,4] using 3 swaps in total.

Example 2:
Input: seats = [0,1,0,1,0,0,1,0,1]
Output: 5
Explanation: VIPs are at positions [1,3,6,8]. The best final contiguous block has
length 4. One optimal target is seats [3,4,5,6], which requires 5 adjacent swaps overall.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is seats.Length
      We scan the array once to collect VIP positions, then process the collected list once.

    Space Complexity:
    - O(k), where k is the number of VIP guests
      We store the indices of VIP seats and their normalized positions.

    Key Idea:
    - Suppose the VIPs are currently at positions p[0], p[1], ..., p[k-1].
    - If we want them to end up in a contiguous block of length k starting at position s,
      then their target positions are:
          s, s+1, s+2, ..., s+k-1
    - The total adjacent swaps needed is:
          |p[0] - s| + |p[1] - (s+1)| + ... + |p[k-1] - (s+k-1)|
    - Rearranging:
          |(p[0] - 0) - s| + |(p[1] - 1) - s| + ... + |(p[k-1] - (k-1)) - s|
    - So if we define:
          normalized[i] = p[i] - i
      then we need to choose s minimizing:
          sum |normalized[i] - s|
    - A classic result says this sum is minimized when s is the median of normalized[].
    - Therefore:
      1. Collect VIP positions.
      2. Build normalized positions.
      3. Pick the median normalized value.
      4. Sum distances to that median.
    */
    public long MinimumAdjacentSwapsToClusterVIPs(int[] seats)
    {
        // Step 1:
        // Collect the indices of all VIP guests (all positions where seats[i] == 1).
        //
        // Why this is necessary:
        // The only people we truly care about are the VIP guests, because the goal is to
        // make all VIPs consecutive. Regular guests simply get displaced as needed by swaps.
        //
        // Data structure choice:
        // We use List<int> because:
        // - We do not know in advance how many VIPs there are.
        // - We want to append positions as we scan from left to right.
        var vipPositions = new List<int>();

        for (int i = 0; i < seats.Length; i++)
        {
            if (seats[i] == 1)
            {
                vipPositions.Add(i);
            }
        }

        // Step 2:
        // Handle the easy edge cases.
        //
        // If there are 0 or 1 VIP guests, they are already trivially "grouped",
        // because a single VIP is already a contiguous block by itself.
        int k = vipPositions.Count;
        if (k <= 1)
        {
            return 0L;
        }

        // Step 3:
        // Build the normalized positions array.
        //
        // What does "normalized" mean?
        // If VIPs end up in consecutive seats, then the j-th VIP in sorted order must go to:
        // start, start+1, start+2, ..., start+(k-1)
        //
        // So for VIP currently at vipPositions[j], the movement cost is:
        // |vipPositions[j] - (start + j)|
        //
        // Rearranged:
        // |(vipPositions[j] - j) - start|
        //
        // This means each VIP contributes based on vipPositions[j] - j, and the only unknown
        // is the chosen block start position "start".
        //
        // Therefore we define:
        // normalized[j] = vipPositions[j] - j
        //
        // Why this is powerful:
        // The problem becomes "choose one value start that minimizes the sum of absolute
        // differences to all normalized values", and that is solved by the median.
        //
        // Important note:
        // vipPositions is already sorted because we scanned the seats array from left to right.
        // Therefore normalized is also non-decreasing:
        // vipPositions[j+1] >= vipPositions[j] + 1? Not always by +1, but definitely > or =.
        // Since we subtract increasing j, normalized remains sorted enough for median logic.
        // More formally:
        // normalized[j+1] - normalized[j]
        // = (vipPositions[j+1] - (j+1)) - (vipPositions[j] - j)
        // = (vipPositions[j+1] - vipPositions[j]) - 1
        // Since vipPositions[j+1] > vipPositions[j], the difference is at least 0.
        long[] normalized = new long[k];
        for (int i = 0; i < k; i++)
        {
            normalized[i] = (long)vipPositions[i] - i;
        }

        // Step 4:
        // Choose the median normalized value.
        //
        // Why the median?
        // For any array, the value minimizing sum of absolute distances
        // sum |a[i] - x| is any median of the array.
        //
        // Since normalized is already sorted, the median is simply normalized[k / 2].
        // For even k, either middle value works; choosing normalized[k / 2] is valid.
        long median = normalized[k / 2];

        // Step 5:
        // Sum the distances from every normalized value to the median.
        //
        // This gives the minimum total number of adjacent swaps.
        //
        // Why does this equal the number of adjacent swaps?
        // Each adjacent swap moves one VIP by exactly one seat relative to the final arrangement.
        // Summing the absolute seat movements of VIPs gives the exact total number of swaps needed
        // when preserving VIP order, which is optimal because VIPs should never cross each other.
        long totalSwaps = 0;

        for (int i = 0; i < k; i++)
        {
            totalSwaps += Math.Abs(normalized[i] - median);
        }

        return totalSwaps;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] seats1 = { 1, 0, 0, 1, 0, 1 };
long result1 = solution.MinimumAdjacentSwapsToClusterVIPs(seats1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] seats2 = { 0, 1, 0, 1, 0, 0, 1, 0, 1 };
long result2 = solution.MinimumAdjacentSwapsToClusterVIPs(seats2);
Console.WriteLine(result2); // Expected: 5

// Additional quick checks
int[] seats3 = { 0, 0, 0, 0 };
Console.WriteLine(solution.MinimumAdjacentSwapsToClusterVIPs(seats3)); // Expected: 0

int[] seats4 = { 0, 1, 0, 0 };
Console.WriteLine(solution.MinimumAdjacentSwapsToClusterVIPs(seats4)); // Expected: 0

int[] seats5 = { 1, 0, 1 };
Console.WriteLine(solution.MinimumAdjacentSwapsToClusterVIPs(seats5)); // Expected: 1