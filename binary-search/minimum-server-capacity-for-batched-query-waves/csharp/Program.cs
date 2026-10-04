/*
Title: Minimum Server Capacity for Batched Query Waves
Difficulty: Hard
Topic: Binary Search

Problem Description:
You are given an array requests where requests[i] is the number of queries arriving in the i-th minute, in chronological order.
A backend service processes traffic in fixed consecutive deployment waves. Each wave must cover a contiguous block of minutes,
and the total number of queries assigned to any single wave cannot exceed the chosen server capacity C.

Minutes cannot be split across waves: all queries from one minute must stay in the same wave.

You are also given an integer k, the maximum number of waves your operations team is willing to create.

Your task is to compute the minimum integer server capacity C such that the entire traffic timeline can be partitioned into
at most k contiguous waves, where the sum of requests in every wave is at most C.

Return the smallest possible capacity.

This is a decision-optimization problem: for a candidate capacity C, determine whether the requests can be grouped into at most
k valid contiguous waves. Then use that monotonic property to find the minimum feasible capacity efficiently.

Constraints:
- 1 <= requests.length <= 200000
- 1 <= requests[i] <= 1000000000
- 1 <= k <= requests.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: requests = [7,2,5,10,8], k = 2
Output: 18

Example 2:
Input: requests = [1,4,4,3,2], k = 3
Output: 5
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log S)
    - n is the number of minutes / request entries.
    - S is the size of the search range for the answer, specifically:
      from max(requests) up to sum(requests).
    - For each candidate capacity tested by binary search, we scan the array once
      to check feasibility, which costs O(n).
    - Binary search performs O(log S) checks.

    Space Complexity: O(1)
    - We only use a few variables regardless of input size.
    - No extra arrays or complex data structures are needed.
    */
    public long MinimumServerCapacity(int[] requests, int k)
    {
        // We will binary search the answer.
        //
        // Why binary search works:
        // - Suppose a capacity C is feasible, meaning we can split the timeline into
        //   at most k contiguous waves where each wave sum <= C.
        // - Then any larger capacity C' > C is also feasible, because giving ourselves
        //   more allowed capacity can never make the partitioning harder.
        // - This creates a monotonic true/false pattern:
        //     false false false ... true true true
        // - Binary search is the standard tool for finding the first true value
        //   in such a monotonic search space.

        // The minimum possible capacity cannot be smaller than the largest single minute,
        // because a minute cannot be split across waves.
        long left = 0;

        // The maximum possible capacity is the sum of all requests,
        // which corresponds to putting everything into one single wave.
        long right = 0;

        // We compute both bounds in one pass.
        foreach (int request in requests)
        {
            if (request > left)
            {
                left = request;
            }

            right += request;
        }

        // Binary search over the inclusive range [left, right].
        //
        // Goal:
        // Find the smallest capacity that is feasible.
        while (left < right)
        {
            // Use this overflow-safe midpoint formula.
            long mid = left + (right - left) / 2;

            // Check whether capacity "mid" is enough.
            if (CanPartitionWithinKWaveLimit(requests, k, mid))
            {
                // If mid is feasible, it might already be the answer,
                // but there could still be a smaller feasible capacity.
                // So we keep searching on the left half, including mid.
                right = mid;
            }
            else
            {
                // If mid is not feasible, then every smaller capacity is also not feasible.
                // So we must search strictly to the right of mid.
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right and points to the smallest feasible capacity.
        return left;
    }

    private bool CanPartitionWithinKWaveLimit(int[] requests, int k, long capacity)
    {
        // This helper answers the decision question:
        // "If the server capacity is exactly 'capacity', can we split the requests
        //  into at most k contiguous waves such that each wave sum <= capacity?"
        //
        // We use a greedy strategy:
        // - Build the current wave by adding consecutive minutes as long as doing so
        //   does not exceed the capacity.
        // - The moment adding the next minute would exceed capacity, we must start a new wave.
        //
        // Why greedy is correct here:
        // - For a fixed capacity, packing as many consecutive minutes as possible into the current wave
        //   minimizes the number of waves used.
        // - If even this minimal-wave greedy approach needs more than k waves,
        //   then no other partitioning can do it in at most k waves.
        //
        // Data structure choice:
        // - We do not need any extra data structure.
        // - A few scalar variables are enough:
        //   * waveCount: how many waves we have created so far
        //   * currentWaveSum: total requests currently assigned to the active wave

        long currentWaveSum = 0;
        int waveCount = 1; // We start with one wave and keep extending it.

        foreach (int request in requests)
        {
            // If adding this minute's requests stays within capacity,
            // we keep it in the current wave.
            if (currentWaveSum + request <= capacity)
            {
                currentWaveSum += request;
            }
            else
            {
                // Otherwise, this minute cannot fit in the current wave.
                // Because minutes must remain contiguous and cannot be split,
                // the only valid action is to start a new wave beginning with this minute.
                waveCount++;
                currentWaveSum = request;

                // Early exit optimization:
                // If we already exceeded the allowed number of waves,
                // there is no need to continue scanning.
                if (waveCount > k)
                {
                    return false;
                }
            }
        }

        // If we finished processing all minutes using at most k waves,
        // then this capacity is feasible.
        return true;
    }
}

// Demo code:
// We create the sample inputs from the problem statement,
// call the solution, and print the results.

// Example 1:
// requests = [7,2,5,10,8], k = 2
// Expected output: 18
//
// Quick trace:
// - Capacity 18 works with partition [7,2,5] and [10,8] => sums 14 and 18.
// - Any smaller capacity fails for at most 2 waves.
// Therefore the answer is 18.
var solution = new Solution();

int[] requests1 = { 7, 2, 5, 10, 8 };
int k1 = 2;
long result1 = solution.MinimumServerCapacity(requests1, k1);
Console.WriteLine(result1);

// Example 2:
// requests = [1,4,4,3,2], k = 3
// Expected output: 5
//
// Quick trace:
// - Capacity 5 works with partition [1,4], [4], [3,2] => sums 5, 4, 5.
// - Capacity 4 does not work within 3 waves.
// Therefore the answer is 5.
int[] requests2 = { 1, 4, 4, 3, 2 };
int k2 = 3;
long result2 = solution.MinimumServerCapacity(requests2, k2);
Console.WriteLine(result2);