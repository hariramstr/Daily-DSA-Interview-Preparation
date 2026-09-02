/*
Title: Minimum Lane Shifts to Group VIP Cars

Problem Description:
You are given an array lanes where each element is either 0 or 1. A value of 1 represents a VIP car, and a value of 0 represents a regular car. The cars are parked in a single row, and you want all VIP cars to end up occupying consecutive positions somewhere in the row.

In one operation, you may choose a VIP car and shift it left or right by one position, swapping it with the adjacent car. The cost of each adjacent swap is 1. Your task is to return the minimum total number of adjacent swaps required to make all VIP cars contiguous.

The relative order of VIP cars does not matter beyond what is implied by adjacent swaps, and you may choose any final block of consecutive positions for them. If the array contains 0 or 1 VIP car, the answer is 0.

This problem asks you to compute the minimum movement cost efficiently for large inputs. A brute-force attempt over all possible target blocks will be too slow, so you need to exploit the structure of VIP positions in the array.

Constraints:
- 1 <= lanes.length <= 100000
- lanes[i] is either 0 or 1
- The answer fits in a 64-bit integer

Example 1:
Input: lanes = [1,0,0,1,0,1]
Output: 3
Explanation: The VIP cars are at indices 0, 3, and 5. One optimal result is to move them to indices 2, 3, and 4. That takes 2 swaps for the first VIP car and 1 swap for the last VIP car, for a total of 3.

Example 2:
Input: lanes = [0,1,0,1,0,0,1,0]
Output: 4
Explanation: The VIP cars are at indices 1, 3, and 6. An optimal final block is indices 2, 3, and 4. The first VIP car moves 1 step right, the second stays, and the third moves 2 steps left, so the total cost is 4.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan the array once to collect the positions of all VIP cars.
    - Then we scan the list of VIP positions once more to build the transformed values.
    - Finding the median and summing distances is linear in the number of VIP cars.
    - Since the number of VIP cars is at most n, the total time is O(n).

    Space Complexity: O(k)
    - We store the indices of the VIP cars, where k is the number of 1s in the array.
    - We also store a transformed list of size k.
    - Therefore the extra space is O(k), which is O(n) in the worst case.
    */
    public long MinAdjacentSwapsToGroupVipCars(int[] lanes)
    {
        // Step 1:
        // Collect the indices of every VIP car (every position where lanes[i] == 1).
        //
        // Why do we do this?
        // Because only the VIP cars matter for the movement cost.
        // Regular cars (0s) are just the spaces that VIP cars swap through.
        // If we know where all VIP cars currently are, we can reason directly
        // about how far they must move to become consecutive.
        //
        // Data structure choice:
        // We use List<int> because:
        // - We do not know in advance how many VIP cars there are.
        // - We want to append positions as we discover them.
        var positions = new List<int>();

        for (int i = 0; i < lanes.Length; i++)
        {
            if (lanes[i] == 1)
            {
                positions.Add(i);
            }
        }

        // Step 2:
        // Handle the easy edge case.
        //
        // If there are 0 or 1 VIP cars, they are already "grouped" by definition.
        // No swaps are needed because:
        // - 0 VIP cars means nothing to move
        // - 1 VIP car is already contiguous by itself
        int k = positions.Count;
        if (k <= 1)
        {
            return 0;
        }

        // Step 3:
        // Transform the positions.
        //
        // This is the key idea of the problem.
        //
        // Suppose the VIP cars end up in some consecutive block:
        // start, start + 1, start + 2, ..., start + (k - 1)
        //
        // If the original VIP positions are:
        // positions[0], positions[1], positions[2], ..., positions[k - 1]
        //
        // Then the total movement cost would be:
        // |positions[0] - (start + 0)| +
        // |positions[1] - (start + 1)| +
        // |positions[2] - (start + 2)| + ...
        //
        // Rearranging each term:
        // |(positions[i] - i) - start|
        //
        // So instead of searching over all possible target blocks directly,
        // we transform each VIP position into:
        // transformed[i] = positions[i] - i
        //
        // Then the problem becomes:
        // Choose a single value "start" that minimizes
        // sum of |transformed[i] - start|
        //
        // A classic result in algorithms/math:
        // The value that minimizes the sum of absolute distances is the median.
        //
        // So after this transformation, we only need the median of transformed[].
        var transformed = new List<long>(k);

        for (int i = 0; i < k; i++)
        {
            transformed.Add((long)positions[i] - i);
        }

        // Step 4:
        // Find the median of the transformed values.
        //
        // Why is the median correct?
        // Because for any set of numbers, the sum of absolute differences
        // to a chosen target is minimized when the target is a median.
        //
        // Why can we directly take transformed[k / 2]?
        // Because positions[] is naturally sorted in increasing index order,
        // and subtracting i preserves sorted order for this specific sequence:
        // positions[i+1] >= positions[i] + 1 for distinct VIP positions,
        // so transformed[i+1] = positions[i+1] - (i+1) >= positions[i] - i.
        //
        // That means transformed[] is already sorted, so no extra sorting is needed.
        long median = transformed[k / 2];

        // Step 5:
        // Compute the total minimum cost using the median.
        //
        // Each term |transformed[i] - median| corresponds exactly to the number
        // of adjacent swaps needed for that VIP car, after accounting for the fact
        // that the final VIP cars must occupy consecutive positions.
        //
        // We use long because:
        // - n can be as large as 100000
        // - the total movement can be large
        // - the problem explicitly says the answer fits in 64-bit integer
        long totalSwaps = 0;

        for (int i = 0; i < k; i++)
        {
            totalSwaps += Math.Abs(transformed[i] - median);
        }

        // Step 6:
        // Return the minimum total number of adjacent swaps.
        return totalSwaps;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// lanes = [1,0,0,1,0,1]
// VIP positions = [0,3,5]
// One optimal final block is [2,3,4]
// Cost = |0-2| + |3-3| + |5-4| = 2 + 0 + 1 = 3
int[] lanes1 = { 1, 0, 0, 1, 0, 1 };
long result1 = solution.MinAdjacentSwapsToGroupVipCars(lanes1);
Console.WriteLine(result1); // Expected: 3

// Example 2:
// lanes = [0,1,0,1,0,0,1,0]
// VIP positions = [1,3,6]
// One optimal final block is [2,3,4]
// Cost = |1-2| + |3-3| + |6-4| = 1 + 0 + 2 = 3
//
// Important note:
// The problem statement says the output is 4, but the movement described in the
// statement itself adds up to 1 + 0 + 2 = 3, not 4.
// Therefore the correct minimum answer for this example is 3.
int[] lanes2 = { 0, 1, 0, 1, 0, 0, 1, 0 };
long result2 = solution.MinAdjacentSwapsToGroupVipCars(lanes2);
Console.WriteLine(result2); // Correct result: 3

// Additional quick checks

int[] lanes3 = { 0, 0, 0, 0 };
Console.WriteLine(solution.MinAdjacentSwapsToGroupVipCars(lanes3)); // Expected: 0

int[] lanes4 = { 1 };
Console.WriteLine(solution.MinAdjacentSwapsToGroupVipCars(lanes4)); // Expected: 0

int[] lanes5 = { 1, 0, 1, 0, 1 };
Console.WriteLine(solution.MinAdjacentSwapsToGroupVipCars(lanes5)); // Expected: 2