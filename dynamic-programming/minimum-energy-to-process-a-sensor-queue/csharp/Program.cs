/*
Title: Minimum Energy to Process a Sensor Queue
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A monitoring device must process a queue of sensor packets in order. The packets are represented by an integer array load, where load[i] is the energy cost of processing the i-th packet by itself. To reduce overhead, the device is allowed to process either one packet alone or two consecutive packets together as a batch.

If the device processes packet i alone, it spends load[i] energy. If it processes packets i and i+1 together, it spends max(load[i], load[i+1]) + penalty energy, where penalty is a fixed non-negative integer representing batching overhead. Every packet must be processed exactly once, and batches cannot overlap. Your task is to return the minimum total energy needed to process the entire queue.

This is an optimization problem over prefixes of the array. At each position, the best choice may depend on the minimum energy needed for earlier packets, so a dynamic programming solution is expected. A greedy choice such as always batching the two smallest neighboring packets does not always lead to the global optimum.

Constraints:
- 1 <= load.length <= 100000
- 0 <= load[i] <= 1000000000
- 0 <= penalty <= 1000000000
- The answer fits in a 64-bit signed integer.

Important note about the examples:
The written explanation in Example 1 contains arithmetic inconsistencies.
Using the stated rules, the true minimum for load = [4, 7, 2, 9], penalty = 1 is 17:
- Process packet 0 alone: 4
- Process packets 1 and 2 together: max(7,2)+1 = 8
- Process packet 3 alone: 9
Total = 4 + 8 + 9 = 21? No.
That plan is 21, so not optimal.
Better:
- Process packet 0 alone: 4
- Process packet 1 alone: 7
- Process packet 2 alone: 2
- Process packet 3 alone: 9
Total = 22
Better:
- Process packets 0 and 1 together: 8
- Process packet 2 alone: 2
- Process packet 3 alone: 9
Total = 19
Best:
- Process packet 0 alone: 4
- Process packets 1 and 2 together: 8
- Process packet 3 alone: 9
Total = 21
Also:
- Process packet 0 alone: 4
- Process packet 1 alone: 7
- Process packets 2 and 3 together: 10
Total = 21
- Process packets 0 and 1 together: 8
- Process packets 2 and 3 together: 10
Total = 18

Therefore, the actual minimum is 18, not 19.
The algorithm below follows the problem rules exactly and will compute the correct minimum.
*/

using System;

public class Solution
{
    // Time Complexity: O(n)
    // Space Complexity: O(1)
    public long MinEnergy(int[] load, int penalty)
    {
        // We will solve this with dynamic programming over prefixes of the array.
        //
        // Core idea:
        // Let dp[i] mean:
        // "the minimum energy required to process the first i packets"
        //
        // That means:
        // dp[0] = 0 because processing zero packets costs nothing.
        //
        // To compute dp[i], we look at the final action used to finish the first i packets:
        //
        // Option 1:
        // The i-th packet (0-based index i-1) is processed alone.
        // Then the cost is:
        // dp[i - 1] + load[i - 1]
        //
        // Option 2:
        // The last two packets (indices i-2 and i-1) are processed together.
        // Then the cost is:
        // dp[i - 2] + max(load[i - 2], load[i - 1]) + penalty
        //
        // So the recurrence is:
        // dp[i] = min(
        //     dp[i - 1] + load[i - 1],
        //     dp[i - 2] + max(load[i - 2], load[i - 1]) + penalty   if i >= 2
        // )
        //
        // Because each dp[i] depends only on dp[i-1] and dp[i-2],
        // we do not need a full array. We can keep only the last two values.
        //
        // This reduces memory from O(n) to O(1), which is especially useful
        // because n can be as large as 100,000.

        int n = load.Length;

        // Base case:
        // dp[0] = 0
        // This means no packets processed yet.
        long dpPrev2 = 0;

        // Base case:
        // dp[1] = load[0]
        // If there is exactly one packet to process, the only possible action
        // is to process it alone.
        long dpPrev1 = load[0];

        // If the array has only one packet, we can return immediately.
        if (n == 1)
        {
            return dpPrev1;
        }

        // We now compute dp[2], dp[3], ..., dp[n].
        //
        // The loop variable i means:
        // "we are computing the answer for the first i packets"
        //
        // Since dp[1] is already known, we start from i = 2.
        for (int i = 2; i <= n; i++)
        {
            // The last packet in this prefix is at array index i - 1.
            // If we process that packet alone, then the total cost is:
            // minimum cost for first i-1 packets + cost of processing packet i-1 alone.
            long processAlone = dpPrev1 + load[i - 1];

            // If we process the last two packets together, those packets are
            // at indices i-2 and i-1.
            //
            // Their batch cost is:
            // max(load[i-2], load[i-1]) + penalty
            //
            // And before that batch, we must already have optimally processed
            // the first i-2 packets, which costs dpPrev2.
            long processAsBatch = dpPrev2 + Math.Max((long)load[i - 2], (long)load[i - 1]) + penalty;

            // The optimal answer for the first i packets is the cheaper of the two choices.
            long current = Math.Min(processAlone, processAsBatch);

            // Shift the rolling DP window forward:
            //
            // Before shift:
            // dpPrev2 = dp[i-2]
            // dpPrev1 = dp[i-1]
            //
            // After shift:
            // dpPrev2 should become old dp[i-1]
            // dpPrev1 should become newly computed dp[i]
            dpPrev2 = dpPrev1;
            dpPrev1 = current;
        }

        // After the loop finishes, dpPrev1 holds dp[n],
        // which is the minimum energy to process all packets.
        return dpPrev1;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// According to the actual stated rules, the correct minimum is 18.
int[] load1 = { 4, 7, 2, 9 };
int penalty1 = 1;
long result1 = solution.MinEnergy(load1, penalty1);
Console.WriteLine($"Example 1 result: {result1}");

// Example 2 from the prompt.
// Batch (5,1) for 5 and (5,1) for 5 => total 10.
int[] load2 = { 5, 1, 5, 1 };
int penalty2 = 0;
long result2 = solution.MinEnergy(load2, penalty2);
Console.WriteLine($"Example 2 result: {result2}");

// Additional small sanity checks for beginners to inspect.

// Single packet: must process alone.
int[] load3 = { 8 };
int penalty3 = 100;
long result3 = solution.MinEnergy(load3, penalty3);
Console.WriteLine($"Single packet result: {result3}");

// Two packets: compare alone+alone vs batch.
int[] load4 = { 3, 10 };
int penalty4 = 2;
// Alone: 13
// Batch: max(3,10)+2 = 12
// Answer: 12
long result4 = solution.MinEnergy(load4, penalty4);
Console.WriteLine($"Two packets result: {result4}");