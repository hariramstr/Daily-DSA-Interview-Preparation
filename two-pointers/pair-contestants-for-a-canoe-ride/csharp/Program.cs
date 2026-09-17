/*
Title: Pair Contestants for a Canoe Ride

Problem Description:
You are organizing a team-building event where contestants will ride in two-person canoes.
Each canoe can carry at most 2 people, and the combined weight of the two people in the same
canoe cannot exceed a given limit. Some contestants may need to ride alone if no valid partner
is available.

Given an integer array weights where weights[i] is the weight of the i-th contestant, and an
integer limit representing the maximum allowed total weight in one canoe, return the minimum
number of canoes needed to carry everyone.

Efficient Strategy:
- Sort the weights.
- Use two pointers:
  - One pointer starts at the lightest remaining contestant.
  - One pointer starts at the heaviest remaining contestant.
- If the lightest and heaviest can share a canoe, pair them together.
- Otherwise, the heaviest contestant must ride alone.
- In either case, one canoe is used each step.

Constraints:
- 1 <= weights.length <= 50000
- 1 <= weights[i] <= limit <= 30000
- Each canoe can carry at most 2 contestants

Example 1:
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation:
Sorted: [50, 50, 70, 80]
- 50 + 80 = 130 > 100, so 80 goes alone
- 50 + 70 = 120 > 100, so 70 goes alone
- 50 + 50 = 100 <= 100, so they share
Total canoes = 3

Example 2:
Input: weights = [40, 60, 55, 45], limit = 100
Output: 2
Explanation:
Sorted: [40, 45, 55, 60]
- 40 + 60 = 100 <= 100, pair them
- 45 + 55 = 100 <= 100, pair them
Total canoes = 2
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)

    Space Complexity: O(1) extra space
    - Aside from the sorting implementation details, the algorithm itself only uses a few variables.
    - We sort the input array in place and then scan it with two pointers.
    */
    public int NumRescueBoats(int[] weights, int limit)
    {
        // Step 1:
        // Sort the weights in non-decreasing order.
        //
        // Why this is necessary:
        // Sorting lets us make a greedy decision using the lightest and heaviest remaining contestants.
        // This is the key idea of the two-pointer approach.
        //
        // After sorting:
        // - The left pointer will represent the lightest person not yet assigned to a canoe.
        // - The right pointer will represent the heaviest person not yet assigned to a canoe.
        //
        // This arrangement makes it easy to test the best possible partner for the heaviest person:
        // if even the lightest person cannot fit with the heaviest, then nobody can fit with the heaviest.
        Array.Sort(weights);

        // Step 2:
        // Initialize two pointers:
        // - left starts at the beginning of the sorted array (lightest contestant)
        // - right starts at the end of the sorted array (heaviest contestant)
        int left = 0;
        int right = weights.Length - 1;

        // Step 3:
        // This variable counts how many canoes we use.
        int canoes = 0;

        // Step 4:
        // Process contestants until all have been assigned to a canoe.
        //
        // The loop condition left <= right means:
        // - left < right: at least two people remain
        // - left == right: exactly one person remains
        while (left <= right)
        {
            // At every iteration, we will definitely use exactly one canoe.
            //
            // Why?
            // Because the heaviest remaining contestant at index 'right' must be placed now:
            // - either alone
            // - or paired with the lightest remaining contestant at index 'left'
            canoes++;

            // Check whether the lightest and heaviest remaining contestants can share one canoe.
            //
            // Why compare these two?
            // The heaviest person is the hardest to place.
            // If the lightest person can fit with the heaviest, then pairing them is always a good greedy choice.
            // If the lightest person cannot fit with the heaviest, then no one else can fit with the heaviest either,
            // because everyone else is heavier than the lightest.
            if (weights[left] + weights[right] <= limit)
            {
                // They can share a canoe.
                //
                // So we move the left pointer forward because the lightest contestant has now been assigned.
                left++;
            }

            // Whether paired or alone, the heaviest contestant at 'right' has now been assigned to a canoe.
            // So we always move the right pointer backward.
            right--;
        }

        // After the loop finishes, every contestant has been assigned.
        // The number of canoes used is the minimum possible due to the greedy pairing strategy.
        return canoes;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1
int[] weights1 = { 70, 50, 80, 50 };
int limit1 = 100;
int result1 = solution.NumRescueBoats(weights1, limit1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int[] weights2 = { 40, 60, 55, 45 };
int limit2 = 100;
int result2 = solution.NumRescueBoats(weights2, limit2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional demo
int[] weights3 = { 30, 70, 20, 50, 50 };
int limit3 = 100;
int result3 = solution.NumRescueBoats(weights3, limit3);
Console.WriteLine($"Additional Example Result: {result3}"); // One optimal answer: 3