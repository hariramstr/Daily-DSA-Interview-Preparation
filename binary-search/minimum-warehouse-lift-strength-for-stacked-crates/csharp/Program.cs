/*
Title: Minimum Warehouse Lift Strength for Stacked Crates
Difficulty: Medium
Topic: Binary Search

Problem Description:
A warehouse uses an automated lift to move crates in the given order from left to right.
The weight of the i-th crate is weights[i]. The lift has a strength limit S.
It may load consecutive crates into the same trip as long as the total weight of that trip
does not exceed S. Once a trip starts, crates cannot be reordered or skipped, and every crate
must be moved exactly once.

You are also given an integer maxTrips, the maximum number of trips the warehouse is willing
to allow in one shift. Your task is to compute the minimum lift strength S such that all crates
can be moved in at most maxTrips trips.

This is a decision/optimization problem: for any candidate strength S, you can check whether it
is possible to partition the array into at most maxTrips contiguous groups where each group sum
is at most S. The answer is the smallest such S.

Return that minimum possible strength.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= 1000000000
- 1 <= maxTrips <= weights.length
- The answer fits in a 64-bit signed integer

Example 1:
Input: weights = [7,2,5,10,8], maxTrips = 2
Output: 18

Example 2:
Input: weights = [4,4,4,4,4], maxTrips = 3
Output: 8
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log(sum(weights)))
      Explanation:
      1. We binary search on the answer range from max(weights) to sum(weights).
      2. For each candidate strength, we scan the array once to count how many trips are needed.
      3. So each check is O(n), and we do that O(log(sum(weights))) times.

    Space Complexity:
    - O(1)
      Explanation:
      We only use a few variables and do not allocate extra arrays proportional to input size.
    */
    public long MinimumLiftStrength(int[] weights, int maxTrips)
    {
        // We will search for the minimum valid lift strength.
        // To do that correctly, we first need a lower bound and an upper bound.

        // "left" is the smallest possible answer.
        // Why?
        // Because the lift must at least be able to carry the heaviest single crate.
        // If one crate weighs 10, then any strength below 10 is impossible immediately.
        long left = 0;

        // "right" is the largest possible answer.
        // Why?
        // Because if the lift strength equals the sum of all crate weights,
        // then the lift can carry everything in one trip, which is always valid
        // as long as maxTrips >= 1 (and the constraints guarantee that).
        long right = 0;

        // We compute both bounds in one pass:
        // - left becomes the maximum element
        // - right becomes the total sum
        foreach (int weight in weights)
        {
            if (weight > left)
            {
                left = weight;
            }

            right += weight;
        }

        // We now perform a classic binary search on the answer space.
        // Important idea:
        // If a strength S works, then any larger strength also works.
        // This "monotonic" property is exactly what makes binary search possible.
        while (left < right)
        {
            // We choose the middle candidate strength.
            // Using this form avoids overflow in general:
            // left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // We ask:
            // "If the lift strength were mid, could we move all crates
            // in at most maxTrips trips?"
            if (CanMoveWithinTrips(weights, maxTrips, mid))
            {
                // If mid works, then it might already be the minimum answer,
                // or there might be an even smaller valid strength.
                // So we keep searching the left half, including mid itself.
                right = mid;
            }
            else
            {
                // If mid does NOT work, then the answer must be larger than mid.
                // So we discard mid and everything smaller.
                left = mid + 1;
            }
        }

        // When left == right, binary search has converged to the smallest valid strength.
        return left;
    }

    private bool CanMoveWithinTrips(int[] weights, int maxTrips, long strength)
    {
        // This helper method greedily simulates loading crates from left to right.
        //
        // Why greedy works here:
        // For a fixed strength, the best way to minimize the number of trips is to keep
        // adding crates to the current trip until adding the next crate would exceed the limit.
        // Starting a new trip earlier would never help reduce the number of trips.
        //
        // We count how many trips are needed under this strength and then check whether
        // that count is <= maxTrips.

        // We start with one trip because if there is at least one crate,
        // we will need at least one trip.
        int tripsUsed = 1;

        // "currentTripWeight" stores the total weight currently loaded in the ongoing trip.
        long currentTripWeight = 0;

        // Process crates in the required order.
        foreach (int weight in weights)
        {
            // If adding this crate would exceed the allowed strength,
            // we must start a new trip.
            if (currentTripWeight + weight > strength)
            {
                tripsUsed++;

                // If we already exceeded the allowed number of trips,
                // we can stop early and return false.
                // This is a small optimization that avoids unnecessary work.
                if (tripsUsed > maxTrips)
                {
                    return false;
                }

                // The new trip starts with the current crate.
                currentTripWeight = weight;
            }
            else
            {
                // Otherwise, we can safely place this crate into the current trip.
                currentTripWeight += weight;
            }
        }

        // If we finished processing all crates without exceeding maxTrips,
        // then this strength is sufficient.
        return true;
    }
}

// Demo code:
// We create the sample inputs from the problem statement,
// call the solution, and print the results.

var solution = new Solution();

// Example 1:
// weights = [7,2,5,10,8], maxTrips = 2
// Expected output: 18
int[] weights1 = { 7, 2, 5, 10, 8 };
int maxTrips1 = 2;
long result1 = solution.MinimumLiftStrength(weights1, maxTrips1);
Console.WriteLine(result1);

// Example 2:
// weights = [4,4,4,4,4], maxTrips = 3
// Expected output: 8
int[] weights2 = { 4, 4, 4, 4, 4 };
int maxTrips2 = 3;
long result2 = solution.MinimumLiftStrength(weights2, maxTrips2);
Console.WriteLine(result2);