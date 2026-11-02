/*
Title: Pair Hikers for a Narrow Bridge

Problem Description:
A hiking club needs to send people across a narrow suspension bridge. For safety, at most two hikers can be on the bridge at the same time, and the combined weight of any pair on the bridge cannot exceed a given limit. Each hiker must cross exactly once. If a hiker cannot be paired safely, they must cross alone.

You are given an integer array weights where weights[i] is the weight of the i-th hiker, and an integer limit representing the maximum total weight allowed for one crossing. Return the minimum number of crossings needed to get everyone across.

This is a practical optimization problem: using too many solo crossings wastes time, but unsafe pairings are not allowed. An efficient solution should take advantage of sorting and a two-pointer strategy to decide whether the lightest remaining hiker can share a crossing with the heaviest remaining hiker.

Constraints:
- 1 <= weights.length <= 50000
- 1 <= weights[i] <= limit <= 100000
- Every hiker must be assigned to exactly one crossing
- A crossing may contain one or two hikers only

Example 1:
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation: One optimal plan is [50, 50], [70], [80]. So the minimum number of crossings is 3.

Example 2:
Input: weights = [40, 60, 60, 40, 30], limit = 90
Output: 3
Explanation: One optimal plan is [30, 60], [40, 40], [60]. No arrangement can do it in fewer than 3 crossings.
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
    - O(1) extra space if we ignore the space used internally by the sort implementation
    - We sort the input array in place and only use a few variables
    */
    public int MinCrossings(int[] weights, int limit)
    {
        // Step 1:
        // Sort the hikers by weight from lightest to heaviest.
        //
        // Why do we do this?
        // The key greedy idea is:
        // - Always consider the heaviest remaining hiker first, because that hiker is the hardest to pair.
        // - If the lightest remaining hiker cannot pair with the heaviest one,
        //   then nobody else can pair with that heaviest hiker either, because everyone else is heavier.
        //
        // Sorting makes it easy to access:
        // - the lightest remaining hiker at the left side
        // - the heaviest remaining hiker at the right side
        Array.Sort(weights);

        // 'left' points to the lightest hiker not yet assigned to a crossing.
        int left = 0;

        // 'right' points to the heaviest hiker not yet assigned to a crossing.
        int right = weights.Length - 1;

        // This will count how many crossings we need in total.
        int crossings = 0;

        // Step 2:
        // Process hikers until all of them have been assigned.
        //
        // The condition left <= right means:
        // - If left < right, there are at least two hikers left.
        // - If left == right, exactly one hiker remains and must cross alone.
        while (left <= right)
        {
            // At this moment, the heaviest remaining hiker is at index 'right'.
            // We must assign that hiker to a crossing now.
            //
            // Why focus on the heaviest?
            // Because the heaviest hiker has the fewest pairing options.
            // If we delay them, we risk making poor choices.
            int heaviest = weights[right];

            // We will check whether the lightest remaining hiker can safely share
            // the crossing with the heaviest remaining hiker.
            int lightest = weights[left];

            // Step 3:
            // If the lightest + heaviest fit within the limit,
            // then pairing them is always a good choice.
            //
            // Why is this greedy choice correct?
            // - We want to use as many valid pairs as possible, because each valid pair
            //   saves one crossing compared to sending both hikers alone.
            // - If the lightest person can pair with the heaviest, we should do it now.
            //   This uses up the heaviest hiker efficiently while preserving the middle weights
            //   for future possible pairings.
            if (lightest + heaviest <= limit)
            {
                // The lightest hiker is now assigned together with the heaviest hiker.
                // So we move the left pointer inward.
                left++;
            }

            // Whether paired or alone, the heaviest hiker is definitely assigned now.
            // So we always move the right pointer inward.
            right--;

            // We used exactly one crossing in this iteration:
            // - either one pair crossed
            // - or one heavy hiker crossed alone
            crossings++;
        }

        // After the loop, every hiker has been assigned to exactly one crossing.
        // The variable 'crossings' now stores the minimum number of crossings needed.
        return crossings;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] weights1 = { 70, 50, 80, 50 };
int limit1 = 100;
int result1 = solution.MinCrossings((int[])weights1.Clone(), limit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
int[] weights2 = { 40, 60, 60, 40, 30 };
int limit2 = 90;
int result2 = solution.MinCrossings((int[])weights2.Clone(), limit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional demo
int[] weights3 = { 50 };
int limit3 = 100;
int result3 = solution.MinCrossings((int[])weights3.Clone(), limit3);
Console.WriteLine("Additional Demo 1 Result: " + result3); // Expected: 1

int[] weights4 = { 20, 30, 50, 70 };
int limit4 = 80;
int result4 = solution.MinCrossings((int[])weights4.Clone(), limit4);
Console.WriteLine("Additional Demo 2 Result: " + result4); // Expected: 3

// Quick correctness trace in comments:
//
// Example 1 sorted: [50, 50, 70, 80], limit = 100
// left=0, right=3 -> 50+80=130 > 100, so 80 goes alone, crossings=1
// left=0, right=2 -> 50+70=120 > 100, so 70 goes alone, crossings=2
// left=0, right=1 -> 50+50=100 <= 100, pair them, crossings=3
// Answer = 3
//
// Example 2 sorted: [30, 40, 40, 60, 60], limit = 90
// left=0, right=4 -> 30+60=90 <= 90, pair, crossings=1
// left=1, right=3 -> 40+60=100 > 90, so 60 goes alone, crossings=2
// left=1, right=2 -> 40+40=80 <= 90, pair, crossings=3
// Answer = 3