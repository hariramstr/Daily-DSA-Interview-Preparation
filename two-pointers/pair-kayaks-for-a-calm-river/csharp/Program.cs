/*
Title: Pair Kayaks for a Calm River
Difficulty: Easy
Topic: Two Pointers

Problem Description:
A river tour company has a list of tourists waiting to use two-person kayaks. Each tourist has a weight, and every kayak can hold at most two people as long as their combined weight does not exceed a given limit. You want to use as few kayaks as possible while sending everyone on the river.

Given an integer array weights where weights[i] is the weight of the ith tourist, and an integer limit representing the maximum total weight a kayak can safely carry, return the minimum number of kayaks needed to carry all tourists.

Each kayak may carry either one tourist or two tourists. A tourist cannot be split across kayaks, and every tourist must be assigned to exactly one kayak.

This problem is intended to be solved efficiently using sorting and two pointers. After sorting the weights, try pairing the lightest remaining tourist with the heaviest remaining tourist whenever possible. If they cannot share a kayak, the heaviest tourist must go alone.

Constraints:
- 1 <= weights.length <= 50000
- 1 <= weights[i] <= limit <= 100000
- It is always possible to place each tourist in some kayak individually.

Example 1:
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation: One optimal arrangement is (50, 50), (70), (80). So 3 kayaks are needed.

Example 2:
Input: weights = [40, 60, 55, 45, 30], limit = 90
Output: 3
Explanation: One optimal arrangement is (30, 60), (40, 45), (55). This uses the fewest possible kayaks.
*/

using System;

public class Solution
{
    public int NumRescueBoats(int[] weights, int limit)
    {
        /*
        Time Complexity: O(n log n)
        - We sort the array once, which costs O(n log n).
        - Then we scan it with two pointers in O(n).

        Space Complexity: O(1) extra space (ignoring the sorting implementation details)
        - We sort the input array in place.
        - We only use a few integer variables besides the input array.
        */

        // Step 1: Sort the weights in non-decreasing order.
        //
        // Why do we sort?
        // Sorting lets us efficiently compare the lightest remaining tourist
        // and the heaviest remaining tourist.
        //
        // This is the key idea of the two-pointer strategy:
        // - If the lightest and heaviest can share a kayak, that is always a good choice.
        // - If they cannot share, then the heaviest person cannot share with anyone else either,
        //   because everyone else is at least as heavy as the lightest or heavier.
        //
        // So sorting transforms the problem into one where greedy pairing becomes easy and correct.
        Array.Sort(weights);

        // Step 2: Create two pointers:
        // - left points to the lightest tourist not yet assigned to a kayak.
        // - right points to the heaviest tourist not yet assigned to a kayak.
        //
        // We will move these pointers inward as we assign tourists to kayaks.
        int left = 0;
        int right = weights.Length - 1;

        // Step 3: Count how many kayaks we use.
        //
        // Every loop iteration will assign at least the heaviest remaining tourist,
        // so each iteration corresponds to exactly one kayak being used.
        int kayaks = 0;

        // Step 4: Process tourists until all have been assigned.
        //
        // The condition left <= right means there is at least one unassigned tourist left.
        while (left <= right)
        {
            // We are definitely going to use one kayak for the heaviest remaining tourist.
            //
            // Why start from the heaviest?
            // The heaviest tourist is the hardest to pair because they have the least remaining
            // weight capacity available for a partner. Handling them first is a classic greedy move.
            kayaks++;

            // If left == right, there is exactly one tourist left.
            //
            // That tourist must take a kayak alone.
            // We can stop after assigning them.
            if (left == right)
            {
                break;
            }

            // Step 5: Check whether the lightest remaining tourist can share a kayak
            // with the heaviest remaining tourist.
            //
            // This is the most efficient possible partner to try for the heaviest tourist,
            // because the lightest person gives us the best chance to stay within the limit.
            if (weights[left] + weights[right] <= limit)
            {
                // They fit together, so assign both to the same kayak.
                //
                // Why is this safe and optimal?
                // Because if the heaviest person can pair with anyone, pairing them with the
                // lightest remaining person preserves as much flexibility as possible for the
                // remaining middle-weight tourists.
                left++;
                right--;
            }
            else
            {
                // They do NOT fit together.
                //
                // This means the heaviest tourist cannot pair with the lightest tourist.
                // Since every other remaining tourist is heavier than or equal to the lightest,
                // the heaviest tourist cannot pair with any of them either.
                //
                // Therefore, the heaviest tourist must go alone in a kayak.
                right--;
            }
        }

        // Step 6: Return the total number of kayaks used.
        return kayaks;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] weights1 = { 70, 50, 80, 50 };
int limit1 = 100;
int result1 = solution.NumRescueBoats((int[])weights1.Clone(), limit1);
Console.WriteLine("Example 1");
Console.WriteLine($"Weights: [{string.Join(", ", weights1)}]");
Console.WriteLine($"Limit: {limit1}");
Console.WriteLine($"Minimum kayaks needed: {result1}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Example 2
int[] weights2 = { 40, 60, 55, 45, 30 };
int limit2 = 90;
int result2 = solution.NumRescueBoats((int[])weights2.Clone(), limit2);
Console.WriteLine("Example 2");
Console.WriteLine($"Weights: [{string.Join(", ", weights2)}]");
Console.WriteLine($"Limit: {limit2}");
Console.WriteLine($"Minimum kayaks needed: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo
int[] weights3 = { 30, 30, 40, 50 };
int limit3 = 60;
int result3 = solution.NumRescueBoats((int[])weights3.Clone(), limit3);
Console.WriteLine("Additional Demo");
Console.WriteLine($"Weights: [{string.Join(", ", weights3)}]");
Console.WriteLine($"Limit: {limit3}");
Console.WriteLine($"Minimum kayaks needed: {result3}");
Console.WriteLine("One optimal arrangement: (30, 30), (40), (50) => 3");