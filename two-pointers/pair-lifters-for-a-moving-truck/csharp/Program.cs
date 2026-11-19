/*
Title: Pair Lifters for a Moving Truck
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A moving company has a list of furniture items, where weights[i] is the weight of the ith item.
Each trip with a hand truck can carry either one item or two items, but the total weight on a
single trip cannot exceed limit. Every item must be transported exactly once.

Your task is to return the minimum number of trips required to move all items.

This is not just a counting problem: an optimal solution should decide when to pair a light item
with a heavy item and when an item must go alone. The input array is not guaranteed to be sorted.
A good solution is expected to use sorting together with a two-pointer strategy.

Implement a function that takes an integer array weights and an integer limit, and returns the
minimum number of trips needed.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= limit <= 1000000000
- Each trip may carry at most 2 items
- All items must be moved

Example 1:
Input: weights = [4, 2, 3, 5], limit = 6
Output: 3
Explanation: One optimal plan is (4,2), (3), (5). It is impossible to do it in 2 trips because
the item weighing 5 cannot be paired with any other item.

Example 2:
Input: weights = [1, 2, 2, 3, 4], limit = 4
Output: 3
Explanation: One optimal plan is (1,3), (2,2), (4). This uses 3 trips, which is the minimum possible.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - We sort the array once, which costs O(n log n).
    - Then we scan it with two pointers in O(n).

    Space Complexity: O(1) extra space (ignoring the sorting implementation details)
    - We sort the input array in place.
    - We only use a few integer variables after that.

    Beginner-friendly idea:
    1. Sort the weights.
    2. Try to pair the heaviest remaining item with the lightest remaining item.
    3. If they fit together, send them in one trip.
    4. If they do not fit, the heaviest item must go alone.
    5. Repeat until all items are assigned to trips.

    Why this greedy strategy works:
    - The heaviest remaining item is the hardest one to pair.
    - If even the lightest remaining item cannot pair with it, then nothing can pair with it,
      so it must go alone.
    - If it can pair with the lightest, that is always a safe and efficient choice because it
      saves a trip while leaving larger items for later pair checks.
    */
    public int MinTrips(int[] weights, int limit)
    {
        // Step 1: Sort the array so we can efficiently compare the lightest and heaviest items.
        //
        // Why sorting helps:
        // - After sorting, the lightest item is at the beginning.
        // - The heaviest item is at the end.
        // - This makes it possible to use the two-pointer technique:
        //   one pointer starts from the left (lightest),
        //   one pointer starts from the right (heaviest).
        //
        // Without sorting, we would not know which items are best candidates to pair,
        // and we might end up trying many combinations, which would be too slow.
        Array.Sort(weights);

        // Step 2: Initialize two pointers.
        //
        // left  -> points to the lightest remaining item not yet assigned to a trip
        // right -> points to the heaviest remaining item not yet assigned to a trip
        //
        // At every step, we will always assign the item at 'right' to a trip,
        // because the heaviest remaining item must be handled now.
        int left = 0;
        int right = weights.Length - 1;

        // This variable counts how many trips we need in total.
        int trips = 0;

        // Step 3: Process items until all have been assigned.
        //
        // The condition left <= right means:
        // - If left < right, there are at least two items remaining.
        // - If left == right, there is exactly one item remaining.
        while (left <= right)
        {
            // Step 4: We are definitely going to use one trip for the heaviest remaining item.
            //
            // Why?
            // - Every iteration removes the heaviest remaining item from consideration.
            // - That item either goes:
            //   a) alone, or
            //   b) paired with the lightest remaining item.
            trips++;

            // Step 5: Check whether the lightest and heaviest remaining items can share a trip.
            //
            // If weights[left] + weights[right] <= limit:
            // - They fit together.
            // - This is good because we use one trip for two items instead of one.
            //
            // If the sum is greater than limit:
            // - They do not fit together.
            // - Since weights[left] is the lightest remaining item, no other item can pair
            //   with weights[right] either, because every other remaining item is heavier.
            // - Therefore, the heaviest item must go alone.
            if (weights[left] + weights[right] <= limit)
            {
                // The pair fits, so we use up the lightest item as well.
                //
                // Move left forward because that item has now been assigned to a trip.
                left++;
            }

            // Step 6: In all cases, the heaviest item at 'right' has now been assigned.
            //
            // Move right backward to consider the next heaviest remaining item.
            right--;
        }

        // Step 7: Return the total number of trips.
        return trips;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// weights = [4, 2, 3, 5], limit = 6
// Sorted: [2, 3, 4, 5]
//
// Trace:
// left=0 (2), right=3 (5): 2+5=7 > 6, so 5 goes alone. trips=1
// left=0 (2), right=2 (4): 2+4=6 <= 6, pair them. trips=2
// left=1 (3), right=1 (3): one item remains, it goes alone. trips=3
//
// Final answer: 3
int[] weights1 = { 4, 2, 3, 5 };
int limit1 = 6;
int result1 = solution.MinTrips(weights1, limit1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// weights = [1, 2, 2, 3, 4], limit = 4
// Sorted: [1, 2, 2, 3, 4]
//
// Trace:
// left=0 (1), right=4 (4): 1+4=5 > 4, so 4 goes alone. trips=1
// left=0 (1), right=3 (3): 1+3=4 <= 4, pair them. trips=2
// left=1 (2), right=2 (2): 2+2=4 <= 4, pair them. trips=3
//
// Final answer: 3
int[] weights2 = { 1, 2, 2, 3, 4 };
int limit2 = 4;
int result2 = solution.MinTrips(weights2, limit2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo cases for learning:

// Single item: must take exactly one trip.
int[] weights3 = { 7 };
int limit3 = 10;
int result3 = solution.MinTrips(weights3, limit3);
Console.WriteLine($"Single Item Result: {result3}");

// All items can pair perfectly.
int[] weights4 = { 1, 1, 2, 2 };
int limit4 = 3;
int result4 = solution.MinTrips(weights4, limit4);
Console.WriteLine($"Perfect Pairing Result: {result4}");

// No items can pair, so each goes alone.
int[] weights5 = { 5, 5, 5 };
int limit5 = 5;
int result5 = solution.MinTrips(weights5, limit5);
Console.WriteLine($"No Pairing Possible Result: {result5}");