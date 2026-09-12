/*
Title: Count Rescue Boat Pairs Within Safe Weight Range
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given an integer array weights where weights[i] is the weight of the i-th passenger waiting for evacuation.
A rescue boat can carry exactly two passengers, and for safety reasons the combined weight of the pair must be
between lowLimit and highLimit, inclusive.

Your task is to count how many distinct pairs of passengers (i, j) with i < j can be assigned to the same boat.

Two pairs are considered distinct if they use different passenger indices, even if the weights are equal.
Each passenger may appear in many counted pairs because you are only asked to count all valid possible pairings,
not to build a final non-overlapping assignment.

Design an algorithm faster than O(n^2). A typical solution sorts the array and uses a two-pointer strategy
combined with range counting.

Constraints:
- 2 <= weights.length <= 2 * 10^5
- 1 <= weights[i] <= 10^9
- 1 <= lowLimit <= highLimit <= 2 * 10^9

Example 1:
Input: weights = [2, 3, 5, 6, 8], lowLimit = 7, highLimit = 10
Output: 5
Explanation: Valid pairs are (2,5), (2,6), (2,8), (3,5), and (3,6). Their sums are 7, 8, 10, 8, and 9.

Example 2:
Input: weights = [1, 1, 4, 4, 7], lowLimit = 5, highLimit = 8
Output: 7
Explanation:
- Four pairs from choosing one 1 and one 4:
  indices (0,2), (0,3), (1,2), (1,3)
- Two pairs from choosing one 1 and 7:
  indices (0,4), (1,4)
- One pair from the two 4s:
  indices (2,3)
Total = 7

Important note:
The originally stated output text in the prompt first says 6, then correctly explains the total is actually 7.
This solution is written to produce the correct answer: 7.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer counting pass takes O(n)
    - We call the helper twice, so total remains O(n log n)

    Space Complexity:
    - O(1) extra space beyond the sort implementation details
      (Array.Sort may use some internal stack space, but algorithmically we use constant extra space)

    Core idea:
    Count how many pairs have sum <= highLimit,
    then subtract how many pairs have sum <= lowLimit - 1.

    That gives:
    pairs with lowLimit <= sum <= highLimit
    */
    public long CountValidPairs(int[] weights, int lowLimit, int highLimit)
    {
        // We sort the array first because two-pointer techniques rely on ordered data.
        // Once the numbers are sorted, we can reason about how sums change when pointers move.
        Array.Sort(weights);

        // Count all pairs whose sum is at most highLimit.
        long atMostHigh = CountPairsWithSumAtMost(weights, highLimit);

        // Count all pairs whose sum is strictly less than lowLimit.
        // A convenient way to express "strictly less than lowLimit" is "at most lowLimit - 1".
        long belowLow = CountPairsWithSumAtMost(weights, (long)lowLimit - 1);

        // The valid range is inclusive: [lowLimit, highLimit].
        // So we subtract the pairs that are too small from the pairs that are not too large.
        return atMostHigh - belowLow;
    }

    /*
    This helper counts the number of index pairs (i, j), i < j, such that:
    weights[i] + weights[j] <= limit

    Why this helper is useful:
    Counting pairs inside a range [low, high] directly can be tricky.
    But counting "at most X" is much easier with two pointers.
    Then we use:
        count in [low, high] = count <= high - count <= (low - 1)
    */
    private long CountPairsWithSumAtMost(int[] weights, long limit)
    {
        // Left pointer starts at the lightest passenger.
        int left = 0;

        // Right pointer starts at the heaviest passenger.
        int right = weights.Length - 1;

        // We use long because the number of valid pairs can be very large.
        // For n = 200,000, the number of pairs can be about 20 billion,
        // which does not fit in int.
        long count = 0;

        // We continue while left is strictly before right,
        // because a pair must use two different indices.
        while (left < right)
        {
            // Compute current sum as long to avoid overflow.
            long sum = (long)weights[left] + weights[right];

            if (sum <= limit)
            {
                // Current step:
                // We found that the lightest remaining passenger at index "left"
                // can pair with the passenger at index "right" without exceeding the limit.

                // Why this is powerful:
                // Because the array is sorted, every passenger between left+1 and right
                // has weight <= weights[right].
                //
                // Actually, for fixed "left":
                // if weights[left] + weights[right] <= limit,
                // then weights[left] + weights[k] <= limit for every k in [left+1, right].
                //
                // That means ALL pairs:
                // (left, left+1), (left, left+2), ..., (left, right)
                // are valid for the "at most limit" condition.

                // Number of such pairs is exactly:
                // right - left
                count += right - left;

                // Why move left forward?
                // We have already counted every valid pair that starts with this "left".
                // There is nothing more to gain by keeping it.
                // So we advance to the next passenger.
                left++;
            }
            else
            {
                // Current step:
                // The sum is too large, so the heaviest passenger at "right"
                // cannot pair with the lightest passenger at "left" under this limit.

                // Why move right backward?
                // Since the array is sorted, using the same "right" with any index >= left
                // would only keep the partner weight the same or larger.
                // Therefore, this "right" is too heavy for the current "left",
                // and to reduce the sum we must try a smaller right value.
                right--;
            }
        }

        return count;
    }
}

// -------------------------
// Demo / sample test code
// -------------------------

var solution = new Solution();

// Example 1
int[] weights1 = { 2, 3, 5, 6, 8 };
int lowLimit1 = 7;
int highLimit1 = 10;
long result1 = solution.CountValidPairs(weights1, lowLimit1, highLimit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] weights2 = { 1, 1, 4, 4, 7 };
int lowLimit2 = 5;
int highLimit2 = 8;
long result2 = solution.CountValidPairs(weights2, lowLimit2, highLimit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 7

// Additional quick sanity check
int[] weights3 = { 3, 3, 3 };
int lowLimit3 = 6;
int highLimit3 = 6;
long result3 = solution.CountValidPairs(weights3, lowLimit3, highLimit3);
Console.WriteLine("Additional Test Result: " + result3); // Expected: 3