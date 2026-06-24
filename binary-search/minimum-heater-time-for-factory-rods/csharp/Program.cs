/*
Title: Minimum Heater Time for Factory Rods
Difficulty: Medium
Topic: Binary Search

Problem Description:
A factory needs to soften metal rods before cutting them. You are given an array rods where rods[i] is the length of the i-th rod, and an integer machines representing the number of identical heating machines available.

In one minute, a machine can heat exactly one rod segment of length t, where t is the chosen heating time for that minute.
If a rod has length L and the factory uses heating time t, that rod requires ceil(L / t) machine-minutes to finish because it can be processed in multiple equal-sized segments over time.

Different rods may be processed in parallel across machines, but the total number of machine-minutes available is machines.

Your task is to find the minimum positive integer heating time t such that all rods can be fully processed using at most machines total machine-minutes.

In other words, find the smallest integer t where:
sum(ceil(rods[i] / t)) <= machines

This problem is designed to be solved efficiently. A brute-force search over all possible t values may be too slow for large inputs, but the feasibility condition is monotonic:
- If a given heating time t works, then any larger heating time also works.
- That monotonic behavior makes binary search the correct tool.

Constraints:
- 1 <= rods.length <= 100000
- 1 <= rods[i] <= 1000000000
- rods.length <= machines <= 1000000000
- t must be a positive integer

Example 1:
Input: rods = [8, 5, 10], machines = 7
Output: 4

Explanation:
With t = 4:
ceil(8/4) + ceil(5/4) + ceil(10/4) = 2 + 2 + 3 = 7
This fits exactly.

With t = 3:
ceil(8/3) + ceil(5/3) + ceil(10/3) = 3 + 2 + 4 = 9
This is too much.
So the minimum valid answer is 4.

Example 2:
Input: rods = [12, 15, 6], machines = 6
Output: 6

Explanation:
With t = 6:
ceil(12/6) + ceil(15/6) + ceil(6/6) = 2 + 3 + 1 = 6
This works.

With t = 5:
ceil(12/5) + ceil(15/5) + ceil(6/5) = 3 + 3 + 2 = 8
This does not work.
So the minimum valid answer is 6.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
      where:
      n = number of rods
      M = maximum rod length
    Why:
    - Binary search checks at most log M candidate heating times.
    - Each check scans all rods once to compute the total required machine-minutes.

    Space Complexity:
    - O(1) extra space
    Why:
    - We only use a few variables regardless of input size.
    */
    public int MinimumHeaterTime(int[] rods, int machines)
    {
        // Step 1:
        // We need to search for the smallest positive integer t such that:
        // sum(ceil(rods[i] / t)) <= machines
        //
        // Because larger t makes each ceil(rods[i] / t) smaller or equal,
        // the total required machine-minutes never increases as t grows.
        //
        // That means the answer space is monotonic:
        // - too small t => not feasible
        // - large enough t => feasible
        //
        // This exact pattern is ideal for binary search.

        // Step 2:
        // Establish the search boundaries.
        //
        // Lowest possible t:
        // - t must be positive, so the smallest possible value is 1.
        int left = 1;

        // Highest possible t:
        // - If t is at least the maximum rod length, then every rod needs only 1 machine-minute.
        // - Since machines >= rods.Length by constraint, that will always be feasible.
        //
        // So max rod length is a safe upper bound for binary search.
        int right = 0;
        foreach (int rod in rods)
        {
            if (rod > right)
            {
                right = rod;
            }
        }

        // Step 3:
        // Perform a standard "find first true" binary search.
        //
        // Invariant idea:
        // - We are looking for the smallest t that is feasible.
        // - If mid is feasible, the answer could be mid or something smaller.
        // - If mid is not feasible, we must go larger.
        while (left < right)
        {
            // Use this midpoint formula to avoid overflow:
            // left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check whether this candidate heating time "mid" is feasible.
            //
            // We must compute:
            // total = sum(ceil(rods[i] / mid))
            //
            // If total <= machines, then mid works.
            // Otherwise, mid is too small.
            if (CanProcessAllRods(rods, machines, mid))
            {
                // mid is feasible.
                //
                // Since we want the MINIMUM feasible t,
                // we keep mid in the search range and continue left.
                right = mid;
            }
            else
            {
                // mid is not feasible.
                //
                // Therefore every value <= mid is also not feasible,
                // so we must search strictly to the right.
                left = mid + 1;
            }
        }

        // Step 5:
        // When left == right, binary search has converged to the smallest feasible t.
        return left;
    }

    private bool CanProcessAllRods(int[] rods, int machines, int t)
    {
        // We use long here instead of int because:
        // - There can be up to 100000 rods.
        // - Each ceil(rods[i] / t) can be large.
        // - The sum could exceed int range during intermediate computation.
        //
        // Using long prevents overflow and keeps the logic correct.
        long requiredMachineMinutes = 0;

        // We scan every rod and compute how many machine-minutes it needs
        // under the current heating time t.
        foreach (int rod in rods)
        {
            // We need ceil(rod / t), but rod and t are integers.
            //
            // A common integer-only formula for ceiling division is:
            // ceil(a / b) = (a + b - 1) / b
            //
            // Example:
            // rod = 10, t = 4
            // (10 + 4 - 1) / 4 = 13 / 4 = 3
            //
            // This avoids floating-point arithmetic and is exact.
            requiredMachineMinutes += (rod + (long)t - 1) / t;

            // Important optimization:
            // If we already exceeded the allowed machine count,
            // there is no need to continue scanning.
            //
            // This early exit can save time on large inputs.
            if (requiredMachineMinutes > machines)
            {
                return false;
            }
        }

        // If after processing all rods the total required machine-minutes
        // is within the available machine budget, then t is feasible.
        return requiredMachineMinutes <= machines;
    }
}

// Demo code:
// Create the sample inputs from the problem statement,
// call the solution method, and print the outputs.

var solution = new Solution();

// Example 1:
// rods = [8, 5, 10], machines = 7
// Expected answer: 4
int[] rods1 = { 8, 5, 10 };
int machines1 = 7;
int result1 = solution.MinimumHeaterTime(rods1, machines1);
Console.WriteLine(result1);

// Example 2:
// rods = [12, 15, 6], machines = 6
// Expected answer: 6
int[] rods2 = { 12, 15, 6 };
int machines2 = 6;
int result2 = solution.MinimumHeaterTime(rods2, machines2);
Console.WriteLine(result2);