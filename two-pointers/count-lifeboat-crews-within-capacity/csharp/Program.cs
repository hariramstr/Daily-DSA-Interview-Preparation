/*
Title: Count Lifeboat Crews Within Capacity
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given an integer array weights where weights[i] is the weight of the i-th rescue worker,
and an integer limit representing the maximum total weight a small lifeboat can safely carry.

A valid crew consists of exactly two different workers whose combined weight is less than or equal to limit.

Your task is to return the total number of distinct valid crews that can be formed.
Two crews are considered different if they use a different pair of indices, even if the workers have the same weight.

For example, if three workers all weigh 40 and the limit is 80, then all three index pairs are valid crews.

The input array is not guaranteed to be sorted. An efficient solution is expected; a quadratic scan
over all pairs may time out for large inputs. Think carefully about how sorting and a two-pointer
strategy can help count many valid pairs at once.

Constraints:
- 2 <= weights.length <= 200000
- 1 <= weights[i] <= 10^9
- 1 <= limit <= 2 * 10^9
- Return the answer as a 64-bit integer if your language requires it

Example 1:
Input: weights = [70, 50, 80, 40], limit = 120
Output: 4
Explanation: After sorting, the weights are [40, 50, 70, 80].
Valid pairs are (40,50), (40,70), (40,80), and (50,70).

Example 2:
Input: weights = [30, 30, 60, 90, 50], limit = 90
Output: 5
Explanation: Valid pairs are formed by indices corresponding to weights (30,30), (30,60), (30,60)
using the other 30, and (30,50), (30,50) using the other 30. Pairs involving 90 are invalid,
and 50 + 60 exceeds the limit.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra space used by our algorithm logic is O(1)
      (ignoring any internal implementation details of the sorting routine)
    */
    public long CountValidCrews(int[] weights, int limit)
    {
        // Step 1:
        // Sort the array so that worker weights are in non-decreasing order.
        //
        // Why this is necessary:
        // The two-pointer technique depends on sorted order.
        // Once sorted, if weights[left] + weights[right] is valid,
        // then weights[left] can also pair with every worker between left+1 and right,
        // because all of those workers are lighter than or equal to weights[right].
        //
        // This is the key observation that lets us count many pairs at once
        // instead of checking every pair individually.
        Array.Sort(weights);

        // This variable stores the total number of valid pairs.
        // We use long because the number of pairs can be very large.
        // For n = 200000, the maximum number of pairs is n * (n - 1) / 2,
        // which does not fit safely in a 32-bit int.
        long validCrewCount = 0;

        // Step 2:
        // Set up two pointers:
        // - left starts at the lightest worker
        // - right starts at the heaviest worker
        //
        // We will move these pointers inward based on whether the current pair fits.
        int left = 0;
        int right = weights.Length - 1;

        // Step 3:
        // Continue while left is strictly less than right,
        // because a crew must contain exactly two different workers.
        while (left < right)
        {
            // Compute the sum using long to avoid any overflow concerns,
            // even though the given constraints are still safe for int addition.
            long currentSum = (long)weights[left] + weights[right];

            // Case A:
            // If the lightest remaining worker and the heaviest remaining worker
            // can fit together within the limit...
            if (currentSum <= limit)
            {
                // Then weights[left] can pair with EVERY worker from left+1 through right.
                //
                // Why?
                // Because the array is sorted:
                // weights[left + 1], weights[left + 2], ..., weights[right]
                // are all <= weights[right].
                //
                // So if weights[left] + weights[right] <= limit,
                // then certainly:
                // weights[left] + weights[k] <= limit for every k in [left+1, right].
                //
                // Number of such pairs:
                // (right - left)
                //
                // We add all of them at once.
                validCrewCount += right - left;

                // After counting all pairs that use weights[left],
                // we move left forward to consider the next lightest worker.
                //
                // Why this is correct:
                // We have already counted every valid pair involving the current left worker
                // with all possible partners up to right.
                left++;
            }
            else
            {
                // Case B:
                // If weights[left] + weights[right] is too heavy,
                // then weights[right] cannot pair with weights[left].
                //
                // Because the array is sorted, any worker between left and right-1
                // is >= weights[left].
                //
                // That means:
                // weights[k] + weights[right] >= weights[left] + weights[right] > limit
                // for all k >= left.
                //
                // So the current heaviest worker cannot form a valid pair with anyone
                // in the current range. Therefore, we must move right inward.
                right--;
            }
        }

        // Step 4:
        // Return the total number of valid crews found.
        return validCrewCount;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// weights = [70, 50, 80, 40], limit = 120
// Sorted: [40, 50, 70, 80]
// Valid pairs:
// 40+50 = 90
// 40+70 = 110
// 40+80 = 120
// 50+70 = 120
// Total = 4
int[] weights1 = { 70, 50, 80, 40 };
int limit1 = 120;
long result1 = solution.CountValidCrews(weights1, limit1);
Console.WriteLine(result1); // Expected: 4

// Example 2:
// weights = [30, 30, 60, 90, 50], limit = 90
// Sorted: [30, 30, 50, 60, 90]
// Valid pairs:
// first 30 + second 30
// first 30 + 50
// first 30 + 60
// second 30 + 50
// second 30 + 60
// Total = 5
int[] weights2 = { 30, 30, 60, 90, 50 };
int limit2 = 90;
long result2 = solution.CountValidCrews(weights2, limit2);
Console.WriteLine(result2); // Expected: 5

// Additional quick check:
// Three workers all weigh 40, limit = 80
// Pairs are all valid: choose any 2 out of 3 => 3 pairs
int[] weights3 = { 40, 40, 40 };
int limit3 = 80;
long result3 = solution.CountValidCrews(weights3, limit3);
Console.WriteLine(result3); // Expected: 3