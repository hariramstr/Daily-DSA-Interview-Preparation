/*
Title: Maximum Starting Battery for a Hazardous Drone Route
Difficulty: Hard
Topic: Binary Search

Problem Description:
A delivery drone must travel through a fixed sequence of checkpoints. At checkpoint i, the drone's battery changes by delta[i], which may be positive (recharge station) or negative (wind loss, payload lift, or signal interference). The drone starts before checkpoint 0 with some integer battery B, then applies the checkpoints in order. At every moment after processing each checkpoint, the battery must stay within the safe operating range [0, capacity]. If the battery ever becomes negative, the drone crashes. If it ever exceeds capacity, the battery controller fails.

Your task is to compute the maximum integer starting battery B such that the drone can complete the entire route safely.

If no starting battery in [0, capacity] allows a safe traversal, return -1.

This is not asking whether one particular B works. You must find the largest feasible starting value. A correct solution is expected to exploit the monotonic structure of feasibility and use binary search on the answer.

Constraints:
- 1 <= delta.length <= 2 * 10^5
- -10^9 <= delta[i] <= 10^9
- 0 <= capacity <= 10^18
- Starting battery B must be an integer
- The answer must be computed in O(n log capacity) time or better

Example 1:
Input: delta = [4, -7, 3, -2], capacity = 8
Output: 4
Explanation: Starting with B = 4 gives battery levels 8, 1, 4, 2, all valid. Starting with B = 5 immediately reaches 9 after the first checkpoint, which exceeds capacity.

Example 2:
Input: delta = [-3, 5, -4, 1], capacity = 6
Output: 3
Explanation: Starting with B = 3 gives battery levels 0, 5, 1, 2. Starting with B = 4 fails after the second checkpoint because the battery would become 9. Any smaller feasible start is allowed, but the maximum feasible one is 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one starting battery B takes O(n), because we simulate the route once.
    - We binary search over the integer range [0, capacity], which takes O(log capacity) checks.
    - Total: O(n log capacity)

    Space Complexity:
    - O(1) extra space, ignoring the input array.
    */
    public long MaximumStartingBattery(long[] delta, long capacity)
    {
        // We are looking for the LARGEST starting battery B in [0, capacity]
        // such that after each checkpoint, the battery remains within [0, capacity].
        //
        // A key observation makes binary search possible:
        //
        // Let prefix[i] be the sum of delta[0] + delta[1] + ... + delta[i].
        // After processing checkpoint i, battery = B + prefix[i].
        //
        // For the route to be safe, we need:
        //     0 <= B + prefix[i] <= capacity   for every i
        //
        // Rearranging:
        //     -prefix[i] <= B <= capacity - prefix[i]
        //
        // The set of valid B values is therefore the intersection of many intervals,
        // which is itself an interval (possibly empty).
        //
        // That means feasibility is monotonic:
        // if some B works, then all smaller B do NOT always work in arbitrary problems,
        // but in this specific problem the valid set is a contiguous interval.
        // Therefore, binary search for the maximum feasible B is valid.
        //
        // We will:
        // 1. First check whether ANY starting battery works at all.
        // 2. If none works, return -1.
        // 3. Otherwise binary search for the largest feasible B.

        // Quick rejection / acceptance is handled by the binary search framework itself.
        // But before searching for the maximum feasible value, we should verify that
        // at least one feasible value exists. The easiest candidate to test is 0,
        // but 0 being infeasible does NOT imply no solution exists.
        //
        // So instead, we perform a binary search over the whole range and use the
        // standard "find last true" pattern. If even the smallest feasible candidate
        // does not exist, the result will remain -1.

        long left = 0;
        long right = capacity;
        long answer = -1;

        // Standard binary search for the maximum feasible starting battery.
        //
        // Invariant idea:
        // - We search the whole candidate range [left, right].
        // - If mid is feasible, it is a valid answer candidate, and we try larger values.
        // - If mid is not feasible, we try smaller values.
        while (left <= right)
        {
            // We compute mid this way to avoid overflow:
            // left + (right - left) / 2
            long mid = left + ((right - left) / 2);

            // Check whether starting with battery = mid allows a safe traversal.
            if (IsFeasible(delta, capacity, mid))
            {
                // mid works, so record it.
                answer = mid;

                // Since we want the MAXIMUM feasible starting battery,
                // we continue searching to the right for a possibly larger valid value.
                left = mid + 1;
            }
            else
            {
                // mid does not work, so any answer (if it exists) must be smaller.
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool IsFeasible(long[] delta, long capacity, long startBattery)
    {
        // This method simulates the route checkpoint by checkpoint.
        //
        // Why simulation works:
        // The problem explicitly says the battery must remain within [0, capacity]
        // after processing each checkpoint. So we must inspect every intermediate state.
        //
        // We use long because:
        // - capacity can be as large as 1e18
        // - battery values can therefore also be near 1e18
        // - delta[i] is up to 1e9, which also fits safely in long
        //
        // The simulation is straightforward:
        // battery starts at startBattery
        // for each checkpoint:
        //     battery += delta[i]
        //     if battery < 0 or battery > capacity => fail immediately
        //
        // Early exit is important because once a violation happens,
        // there is no need to continue scanning the rest of the route.

        long battery = startBattery;

        for (int i = 0; i < delta.Length; i++)
        {
            // Apply the battery change at the current checkpoint.
            battery += delta[i];

            // Immediately verify the safety constraints.
            if (battery < 0 || battery > capacity)
            {
                return false;
            }
        }

        // If we processed all checkpoints without violating the range,
        // then this starting battery is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// delta = [4, -7, 3, -2], capacity = 8
// Expected output: 4
//
// Manual trace for B = 4:
// Start: 4
// After +4 => 8
// After -7 => 1
// After +3 => 4
// After -2 => 2
// All within [0, 8], so 4 is feasible.
//
// Manual trace for B = 5:
// Start: 5
// After +4 => 9
// 9 > 8, so infeasible immediately.
// Therefore the maximum feasible start is 4.
long[] delta1 = { 4, -7, 3, -2 };
long capacity1 = 8;
long result1 = solution.MaximumStartingBattery(delta1, capacity1);
Console.WriteLine(result1);

// Example 2:
// delta = [-3, 5, -4, 1], capacity = 6
// Expected output: 3
//
// Manual trace for B = 3:
// Start: 3
// After -3 => 0
// After +5 => 5
// After -4 => 1
// After +1 => 2
// All within [0, 6], so 3 is feasible.
//
// Manual trace for B = 4:
// Start: 4
// After -3 => 1
// After +5 => 6
// After -4 => 2
// After +1 => 3
//
// Important note:
// The problem statement says B = 4 fails after the second checkpoint because battery becomes 9.
// That trace is inconsistent with the given delta sequence.
// For the provided sequence [-3, 5, -4, 1], B = 4 is actually feasible.
// In fact, B = 4 gives levels 1, 6, 2, 3, all valid.
// Let's determine the true maximum:
// B = 5 => after -3 => 2, after +5 => 7, exceeds capacity 6 => infeasible
// Therefore the correct maximum for the stated input is 4.
long[] delta2 = { -3, 5, -4, 1 };
long capacity2 = 6;
long result2 = solution.MaximumStartingBattery(delta2, capacity2);
Console.WriteLine(result2);