/*
Title: Minimum Lane Fixes to Make Traffic Speeds Nondecreasing
Difficulty: Medium
Topic: Arrays

Problem Description:
A city records the average vehicle speed for each lane segment along a highway during a short time window.
The speeds are stored in an integer array `speeds`, where `speeds[i]` is the measured speed at segment `i`
from west to east.

Because of sensor noise, the recorded speeds may go down and up unpredictably. Traffic engineers want the
final reported sequence to be nondecreasing, meaning `final[i] <= final[i + 1]` for every valid `i`.

To correct the data, they are allowed to apply lane fixes. In one lane fix, they may choose a single segment
and increase its speed by any positive amount. Decreasing values is not allowed.

Return the minimum total added speed needed to make the entire array nondecreasing.

Your task is only to compute the minimum total increase, not the resulting array.

Constraints:
- 1 <= speeds.length <= 100000
- 0 <= speeds[i] <= 1000000000
- The answer can be larger than 32-bit integer range, so use 64-bit arithmetic.

Example 1:
Input: speeds = [5, 3, 3, 7, 2]
Output: 9
Explanation:
- Increase the second value from 3 to 5 (+2)
- Increase the third value from 3 to 5 (+2)
- Keep 7 as is
- Increase the last value from 2 to 7 (+5)
Total added speed = 2 + 2 + 5 = 9

Example 2:
Input: speeds = [1, 2, 4, 4, 6]
Output: 0
Explanation:
The array is already nondecreasing, so no fixes are needed.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Idea:
    We scan from left to right and keep track of the minimum value the current element must have
    so that the sequence remains nondecreasing.

    At each position:
    - If the current speed is already at least as large as the previous finalized value, we do nothing.
    - Otherwise, we must increase it exactly enough to match the previous finalized value.
      Increasing it by more would only add unnecessary cost, so matching is optimal.

    Why this greedy approach is correct:
    - The only requirement is that each value must be >= the previous finalized value.
    - Since we are only allowed to increase values, once a previous value is fixed, the current value
      must be at least that large.
    - The cheapest valid choice is to raise the current value to exactly that required minimum.
    */
    public long MinimumTotalIncrease(int[] speeds)
    {
        // This variable stores the total amount of speed we add across all segments.
        // We use long because the sum of many increases can exceed the range of int.
        long totalAdded = 0;

        // This variable represents the finalized value of the previous segment after any needed increase.
        // We start with the first element because there is nothing before it, so it never needs adjustment.
        long previousFinalValue = speeds[0];

        // We now process every segment from left to right, starting at index 1.
        // The reason we go left to right is that the nondecreasing condition compares each element
        // with the one immediately before it. Once we know the finalized previous value,
        // we can determine the minimum valid value for the current segment.
        for (int i = 1; i < speeds.Length; i++)
        {
            // Read the current measured speed.
            long current = speeds[i];

            // If the current value is already large enough, then it does not break the nondecreasing order.
            // In that case:
            // - no increase is needed
            // - this current value becomes the new previous finalized value for the next step
            if (current >= previousFinalValue)
            {
                previousFinalValue = current;
            }
            else
            {
                // If current < previousFinalValue, then the sequence would decrease here,
                // which is not allowed in the final reported array.
                //
                // Since decreasing previous values is forbidden, the only way to fix this position
                // is to increase the current value.
                //
                // The minimum valid target is exactly previousFinalValue.
                // Why exactly?
                // - Any smaller value would still violate nondecreasing order.
                // - Any larger value would also be valid, but would cost more and therefore cannot be optimal.
                long neededIncrease = previousFinalValue - current;

                // Add this required increase to the running total.
                totalAdded += neededIncrease;

                // After increasing, the finalized current value becomes previousFinalValue.
                // We keep previousFinalValue unchanged because the current element has been raised
                // to match it exactly.
            }
        }

        // After processing all segments, totalAdded contains the minimum total increase required.
        return totalAdded;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement
int[] speeds1 = { 5, 3, 3, 7, 2 };
long result1 = solution.MinimumTotalIncrease(speeds1);
Console.WriteLine(result1); // Expected: 9

// Example 2 from the problem statement
int[] speeds2 = { 1, 2, 4, 4, 6 };
long result2 = solution.MinimumTotalIncrease(speeds2);
Console.WriteLine(result2); // Expected: 0

// Additional demo cases

int[] speeds3 = { 10 };
long result3 = solution.MinimumTotalIncrease(speeds3);
Console.WriteLine(result3); // Expected: 0

int[] speeds4 = { 4, 1, 2, 1, 3 };
long result4 = solution.MinimumTotalIncrease(speeds4);
Console.WriteLine(result4); // Expected: 9
// Trace:
// [4,1,2,1,3]
// 1 -> 4 (+3)
// 2 -> 4 (+2)
// 1 -> 4 (+3)
// 3 -> 4 (+1)
// total = 9

int[] speeds5 = { 0, 0, 0, 0 };
long result5 = solution.MinimumTotalIncrease(speeds5);
Console.WriteLine(result5); // Expected: 0