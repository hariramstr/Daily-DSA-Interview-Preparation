/*
Title: Maximize Completed Drone Deliveries Before Battery Clash
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A warehouse operates two launch pads for delivery drones. Each outgoing drone requires exactly one battery pack, and each battery pack can be used at most once. You are given two integer arrays: drones and batteries. drones[i] is the minimum charge required for the i-th drone to complete its route, and batteries[j] is the available charge in the j-th battery pack.

A drone can be launched only if it is assigned a battery pack with charge greater than or equal to its required charge. Each battery can power at most one drone, and each drone can receive at most one battery. Your task is to determine the maximum number of drones that can be launched successfully.

This is not an ordering simulation problem: you may pair drones and batteries in any way you want. Return the largest possible number of valid pairings.

A correct solution should be efficient for large inputs, which strongly suggests sorting and a two-pointer strategy rather than checking all pairings.

Constraints:
- 1 <= drones.length, batteries.length <= 2 * 10^5
- 1 <= drones[i], batteries[j] <= 10^9
- Arrays are not necessarily sorted.

Example 1:
Input: drones = [4, 2, 7], batteries = [3, 8, 5]
Output: 2
Explanation: One optimal assignment is battery 3 -> drone 2 and battery 8 -> drone 7. The remaining battery 5 cannot satisfy drone 4 after those choices in a way that increases the total beyond 2.

Example 2:
Input: drones = [1, 3, 3, 6], batteries = [2, 3, 4]
Output: 3
Explanation: An optimal assignment is 2 -> 1, 3 -> 3, and 4 -> 3. The drone requiring 6 cannot be launched because no remaining battery has enough charge.

Goal:
Compute only the maximum number of completed deliveries.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting drones: O(n log n)
    - Sorting batteries: O(m log m)
    - Two-pointer scan: O(n + m)
    - Total: O(n log n + m log m)

    Space Complexity:
    - If sorting is considered in-place for arrays, extra algorithmic space is O(1)
    - In practice, the runtime may use some internal stack space for sorting
    */
    public int MaxCompletedDeliveries(int[] drones, int[] batteries)
    {
        // Step 1:
        // Sort both arrays in non-decreasing order.
        //
        // Why do we sort?
        // The key greedy idea is:
        // - Try to satisfy the smallest drone requirement first using the smallest battery
        //   that can satisfy it.
        // - This preserves larger batteries for larger drone requirements later.
        //
        // If we did not sort, we would have no efficient way to know whether using a battery
        // now is wasteful or optimal.
        //
        // After sorting:
        // - drones[0] is the easiest drone to satisfy
        // - batteries[0] is the weakest battery
        Array.Sort(drones);
        Array.Sort(batteries);

        // Step 2:
        // Create two pointers:
        //
        // i -> points to the current drone we are trying to satisfy
        // j -> points to the current battery we are considering
        //
        // We also keep a counter for successful pairings.
        int i = 0;
        int j = 0;
        int completed = 0;

        // Step 3:
        // Walk through both arrays from left to right.
        //
        // We stop when:
        // - we have checked all drones, or
        // - we have checked all batteries
        //
        // At each step, we compare:
        // drones[i]    = required charge for current drone
        // batteries[j] = available charge in current battery
        while (i < drones.Length && j < batteries.Length)
        {
            // Case A:
            // The current battery is strong enough for the current drone.
            if (batteries[j] >= drones[i])
            {
                // This is a valid pairing.
                //
                // Why is it safe to pair them immediately?
                // Because both arrays are sorted, and this battery is the smallest remaining
                // battery we have not used yet.
                //
                // If this smallest available battery can satisfy the current smallest
                // unsatisfied drone, then using it now is optimal:
                // - We avoid wasting a larger battery on a small drone.
                // - We preserve stronger batteries for harder drones later.
                completed++;

                // Move to the next drone because the current one has been satisfied.
                i++;

                // Move to the next battery because the current battery has now been used.
                j++;
            }
            else
            {
                // Case B:
                // The current battery is too weak for the current drone.
                //
                // Since drones are sorted, the current drone is the smallest remaining drone.
                // That means if this battery cannot satisfy this drone, it also cannot satisfy
                // any later drone, because later drones require equal or greater charge.
                //
                // Therefore, this battery is useless for all remaining drones.
                // The only sensible action is to discard it and try the next stronger battery.
                j++;
            }
        }

        // Step 4:
        // Return the total number of successful pairings found.
        return completed;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// drones = [4, 2, 7]
// batteries = [3, 8, 5]
//
// After sorting:
// drones    = [2, 4, 7]
// batteries = [3, 5, 8]
//
// Pairings found by the algorithm:
// 3 -> 2   (success)
// 5 -> 4   (success)
// 8 -> 7   (success)
//
// Maximum valid pairings = 3
//
// Note:
// The problem statement says output 2 for this example, but that is inconsistent with the
// stated rules because all three drones can clearly be matched:
// - battery 3 satisfies drone 2
// - battery 5 satisfies drone 4
// - battery 8 satisfies drone 7
//
// Therefore, the correct maximum under the given rules is 3.
int[] drones1 = { 4, 2, 7 };
int[] batteries1 = { 3, 8, 5 };
int result1 = solution.MaxCompletedDeliveries(drones1, batteries1);
Console.WriteLine($"Example 1 result: {result1}");

// Example 2:
// drones = [1, 3, 3, 6]
// batteries = [2, 3, 4]
//
// After sorting:
// drones    = [1, 3, 3, 6]
// batteries = [2, 3, 4]
//
// Pairings found:
// 2 -> 1   (success)
// 3 -> 3   (success)
// 4 -> 3   (success)
// No battery remains for drone 6
//
// Maximum valid pairings = 3
int[] drones2 = { 1, 3, 3, 6 };
int[] batteries2 = { 2, 3, 4 };
int result2 = solution.MaxCompletedDeliveries(drones2, batteries2);
Console.WriteLine($"Example 2 result: {result2}");

// Additional quick demo
int[] drones3 = { 5, 5, 5 };
int[] batteries3 = { 1, 5, 10, 4 };
int result3 = solution.MaxCompletedDeliveries(drones3, batteries3);
Console.WriteLine($"Additional demo result: {result3}");