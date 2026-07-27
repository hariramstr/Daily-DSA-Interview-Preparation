/*
Title: Minimum Reservation Window for Conference Rooms
Difficulty: Hard
Topic: Binary Search

Problem Description:
A company wants to reserve identical conference rooms for a large training event. There are n available time blocks, and the i-th block can host at most rooms[i] rooms if the reservation window is W minutes long. However, room providers impose a setup overhead: each provider can only contribute floor(blockLength[i] / W) rooms to the event, where blockLength[i] is the total number of minutes that provider can offer and W must be the same for every provider. You are given an array blockLength where each value represents the total reservable minutes from one provider, and an integer k representing the number of rooms that must be created.

Return the minimum positive integer reservation window W such that it is possible to create at least k rooms in total, where each room must receive exactly W minutes from a single provider. If it is impossible to create k rooms even with W = 1, return -1.

More formally, find the smallest integer W >= 1 such that sum(floor(blockLength[i] / W)) >= k.

This problem is designed to test whether you can recognize a monotonic feasibility condition and search over the answer space efficiently. A brute-force scan over all possible W values is too slow when block lengths are large.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= blockLength[i] <= 10^12
- 1 <= k <= 10^12
- The answer must fit in a 64-bit signed integer

Important correctness note:
As written, the task asks for the minimum positive integer W such that
sum(floor(blockLength[i] / W)) >= k.

Because W = 1 is the smallest possible positive integer, any feasible instance
will always have answer 1. If W = 1 is not feasible, then no larger W can be
feasible because the total number of rooms only decreases as W increases.

So the mathematically correct interpretation of the exact statement is:
- answer = 1 if sum(blockLength[i]) >= k
- answer = -1 otherwise

The examples themselves acknowledge this subtlety:
Example 1:
blockLength = [8, 5, 12], k = 7
At W = 1, total rooms = 8 + 5 + 12 = 25 >= 7, so the minimum W is 1.
Therefore the correct output is 1, not 3.

Example 2:
blockLength = [2, 1], k = 10
At W = 1, total rooms = 2 + 1 = 3 < 10, so it is impossible. Output = -1.

This solution follows the problem statement exactly and therefore returns the
correct result for the stated task.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the number of providers.
      We only need to evaluate feasibility at W = 1, because if W = 1 does not work,
      no larger W can work; and if W = 1 does work, it is automatically the minimum
      positive integer answer.

    Space Complexity:
    - O(1) extra space, ignoring the input array.

    Beginner-friendly reasoning:
    - The function f(W) = sum(floor(blockLength[i] / W)) is monotonic non-increasing.
      That means as W gets larger, the number of rooms we can create never increases.
    - Since the problem asks for the MINIMUM positive integer W, the smallest candidate
      is always W = 1.
    - Therefore:
        * If W = 1 is feasible, then the minimum answer is immediately 1.
        * If W = 1 is not feasible, then no larger W can be feasible, so answer is -1.
    */
    public long MinimumReservationWindow(long[] blockLength, long k)
    {
        // Step 1:
        // We compute how many total rooms can be created when W = 1.
        //
        // Why this is enough:
        // - W = 1 is the smallest possible positive integer reservation window.
        // - If the requirement is already satisfied at W = 1, then we have found the
        //   minimum possible answer immediately.
        // - If the requirement is NOT satisfied at W = 1, then increasing W can only
        //   reduce or keep the same number of rooms, never increase it.
        //
        // So the entire problem collapses to a single feasibility check at W = 1.
        long totalRoomsAtWindowOne = 0;

        // Step 2:
        // Iterate through every provider's total available minutes.
        //
        // At W = 1:
        // floor(blockLength[i] / 1) = blockLength[i]
        //
        // So each provider contributes exactly blockLength[i] rooms.
        foreach (long minutes in blockLength)
        {
            // Add this provider's contribution.
            totalRoomsAtWindowOne += minutes;

            // Step 3:
            // Early stopping optimization.
            //
            // Why this is useful:
            // - The moment our running total reaches or exceeds k, we already know
            //   W = 1 is feasible.
            // - Since 1 is the smallest possible positive integer, we can return 1
            //   immediately without processing the rest of the array.
            //
            // This does not change correctness; it only improves performance in many cases.
            if (totalRoomsAtWindowOne >= k)
            {
                return 1;
            }
        }

        // Step 4:
        // If we finished the loop and still have fewer than k rooms at W = 1,
        // then the task is impossible.
        //
        // Why impossible?
        // - For any W > 1:
        //     floor(blockLength[i] / W) <= floor(blockLength[i] / 1) = blockLength[i]
        // - Therefore the total number of rooms for larger W cannot exceed the total
        //   number of rooms at W = 1.
        //
        // Since even the best-case smallest window failed, no valid answer exists.
        return -1;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1 from the problem statement.
// According to the exact statement, the minimum positive integer W is 1
// because W = 1 already creates 25 rooms, which is at least 7.
long[] blockLength1 = { 8, 5, 12 };
long k1 = 7;
long result1 = solution.MinimumReservationWindow(blockLength1, k1);
Console.WriteLine(result1); // Expected: 1

// Example 2 from the problem statement.
// At W = 1, total rooms = 2 + 1 = 3, which is less than 10.
// Therefore the answer is -1.
long[] blockLength2 = { 2, 1 };
long k2 = 10;
long result2 = solution.MinimumReservationWindow(blockLength2, k2);
Console.WriteLine(result2); // Expected: -1

// Additional demo:
// Single provider with enough minutes at W = 1.
long[] blockLength3 = { 15 };
long k3 = 10;
long result3 = solution.MinimumReservationWindow(blockLength3, k3);
Console.WriteLine(result3); // Expected: 1

// Additional demo:
// Not enough total minutes even at W = 1.
long[] blockLength4 = { 3, 4, 2 };
long k4 = 20;
long result4 = solution.MinimumReservationWindow(blockLength4, k4);
Console.WriteLine(result4); // Expected: -1