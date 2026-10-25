/*
Title: Minimum Oven Capacity for Batch Cookies

Problem Description:
A bakery receives cookie orders in a fixed sequence. The i-th order requires trays[i]
cookie trays to be baked, and all trays assigned to the same baking session must be
processed together in the original order. The bakery has exactly d baking sessions
available for the day. In one session, the oven can bake a contiguous group of orders
as long as the total number of trays in that session does not exceed the oven capacity.

Your task is to find the minimum oven capacity needed so that all orders can be
completed within at most d sessions.

You may not reorder orders, split a single order across multiple sessions, or skip any
order. A larger oven capacity always makes the schedule at least as feasible as a
smaller one, so an efficient solution should take advantage of this monotonic property.

Return the smallest integer oven capacity that allows all orders to be baked within d
sessions.

Constraints:
- 1 <= trays.length <= 100000
- 1 <= trays[i] <= 1000000
- 1 <= d <= trays.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: trays = [8, 3, 5, 7, 2], d = 3
Output: 10

Example 2:
Input: trays = [4, 4, 4, 4], d = 2
Output: 8
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log S)
      where n is the number of orders and S is the size of the search range
      between the largest single order and the total sum of all orders.
      For each binary search guess, we scan the array once to check feasibility.

    Space Complexity:
    - O(1)
      We only use a few variables and do not allocate extra data structures
      proportional to the input size.
    */
    public long MinimumOvenCapacity(int[] trays, int d)
    {
        // Step 1:
        // We need to determine the search boundaries for binary search.
        //
        // Why do we need boundaries?
        // Binary search works on a sorted/monotonic answer space. Here, the answer
        // is the oven capacity. If a certain capacity works, then any larger capacity
        // will also work. That gives us the monotonic property needed for binary search.
        //
        // The smallest possible valid capacity cannot be less than the largest single order,
        // because we are not allowed to split an order across sessions.
        //
        // The largest possible capacity we would ever need is the sum of all orders,
        // because with that capacity we could bake everything in one session.
        long left = 0;
        long right = 0;

        foreach (int trayCount in trays)
        {
            // Update the lower bound to ensure the oven can at least fit the biggest order.
            left = Math.Max(left, trayCount);

            // Update the upper bound as the total sum of all orders.
            right += trayCount;
        }

        // Step 2:
        // Perform binary search on the answer space [left, right].
        //
        // Invariant:
        // - left is always a candidate capacity that might be the answer.
        // - right is always a candidate capacity that might be the answer.
        //
        // Goal:
        // Shrink the range until left == right, which will be the minimum feasible capacity.
        while (left < right)
        {
            // Compute the middle capacity carefully.
            // We use this form to avoid overflow in general:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate capacity "mid" is enough to finish all orders
            // within at most d sessions.
            //
            // If it is feasible:
            //   We try to find an even smaller valid capacity, so move right to mid.
            //
            // If it is not feasible:
            //   We need a larger capacity, so move left to mid + 1.
            if (CanFinishWithinSessions(trays, d, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // When the loop ends, left == right, and that value is the minimum feasible capacity.
        return left;
    }

    private bool CanFinishWithinSessions(int[] trays, int d, long capacity)
    {
        // Step 1:
        // We simulate the baking process greedily.
        //
        // Why greedy works here:
        // For a fixed capacity, the best way to minimize the number of sessions is to pack
        // each session as much as possible from left to right while preserving order.
        //
        // Since orders must remain contiguous and cannot be split, the greedy strategy is:
        // - Keep adding the next order to the current session if it fits.
        // - Otherwise, start a new session.
        //
        // This greedy process gives the minimum number of sessions needed for this capacity.
        int sessionsUsed = 1;   // We start with the first session.
        long currentLoad = 0;   // Current total trays assigned to the ongoing session.

        // Step 2:
        // Process each order in the original sequence.
        foreach (int trayCount in trays)
        {
            // Safety check:
            // If a single order is bigger than the candidate capacity, then this capacity
            // is impossible immediately because orders cannot be split.
            if (trayCount > capacity)
            {
                return false;
            }

            // Step 3:
            // Try to place this order into the current session.
            //
            // If adding it stays within capacity, we keep it in the current session.
            if (currentLoad + trayCount <= capacity)
            {
                currentLoad += trayCount;
            }
            else
            {
                // Otherwise, we must start a new session for this order.
                sessionsUsed++;
                currentLoad = trayCount;

                // Early exit optimization:
                // If we already used more than d sessions, then this capacity is not feasible.
                if (sessionsUsed > d)
                {
                    return false;
                }
            }
        }

        // If we finish processing all orders using at most d sessions, the capacity works.
        return true;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// trays = [8, 3, 5, 7, 2], d = 3
// Expected output: 10
//
// Quick trace:
// Capacity 10 works with grouping:
// [8], [3,5], [7,2]
// session sums are 8, 8, 9 => at most 10
//
// Capacity 9 fails:
// [8], [3,5], [7,2] seems okay at first glance because 7+2=9,
// but greedy packing from left with capacity 9 gives:
// [8], [3,5], [7,2] => actually 3 sessions and valid.
// So let's verify the true minimum carefully:
// Capacity 8:
// [8], [3,5], [7], [2] => 4 sessions, too many
// Therefore minimum is 9, not 10.
//
// Since correctness is mandatory, we trust the algorithmic definition:
// minimum capacity to finish within at most d sessions.
// For this input, the correct answer is 9.
int[] trays1 = { 8, 3, 5, 7, 2 };
int d1 = 3;
long result1 = solution.MinimumOvenCapacity(trays1, d1);
Console.WriteLine(result1);

// Example 2:
// trays = [4, 4, 4, 4], d = 2
// Expected output: 8
//
// Trace:
// Capacity 8 works: [4,4], [4,4]
// Capacity 7 fails: each 4 must be alone, so 4 sessions needed
int[] trays2 = { 4, 4, 4, 4 };
int d2 = 2;
long result2 = solution.MinimumOvenCapacity(trays2, d2);
Console.WriteLine(result2);

// Additional demo:
int[] trays3 = { 1, 2, 3, 4, 5 };
int d3 = 2;
// One optimal split is [1,2,3] and [4,5], so answer is 9.
long result3 = solution.MinimumOvenCapacity(trays3, d3);
Console.WriteLine(result3);