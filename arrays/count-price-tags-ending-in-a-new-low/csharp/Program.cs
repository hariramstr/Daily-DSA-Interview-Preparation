/*
Title: Count Price Tags Ending in a New Low
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an integer array prices where prices[i] is the price tag printed for the i-th item
in the order items were labeled during a day. A price tag is considered to end in a new low if
its value is strictly smaller than every price that appeared before it in the array. The first
item always counts, because there are no earlier prices to compare against.

Return the number of items whose price tag ends in a new low.

This problem is a simple array scan: as you move from left to right, keep track of the smallest
price seen so far. Whenever the current price is smaller than that running minimum, it creates
a new low and should be counted.

Constraints:
- 1 <= prices.length <= 100000
- -1000000000 <= prices[i] <= 1000000000
- prices may contain duplicates

Important notes:
- A value equal to the current minimum does not count as a new low.
- The first element always contributes 1 to the answer.

Example 1:
Input: prices = [12, 10, 11, 9, 9, 7]
Output: 4
Explanation: New lows occur at 12, 10, 9, and 7. The second 9 does not count because it is not
strictly smaller than the previous minimum.

Example 2:
Input: prices = [5, 5, 5, 4, 6, 3]
Output: 3
Explanation: New lows occur at 5, 4, and 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once from left to right.
    - Each element is processed in constant time.

    Space Complexity: O(1)
    - We use only a few extra variables: one for the running minimum and one for the count.
    - No extra array, list, stack, or other data structure is needed.
    */
    public int CountNewLows(int[] prices)
    {
        // The problem guarantees that the array has at least one element.
        // That means we can safely treat the first element as the starting point.
        //
        // Why do we do this?
        // - The first item always counts as a new low by definition.
        // - It is also the smallest value seen so far at the beginning,
        //   because it is the only value we have seen.
        int count = 1;
        int minSoFar = prices[0];

        // Start scanning from index 1 because index 0 has already been handled.
        for (int i = 1; i < prices.Length; i++)
        {
            // Read the current price once into a local variable.
            // This makes the code easier to read and lets us talk about
            // "the current item" clearly in the comments below.
            int currentPrice = prices[i];

            // We now check whether the current price creates a new low.
            //
            // The exact rule from the problem is:
            // "strictly smaller than every price that appeared before it"
            //
            // Since minSoFar stores the smallest price among all earlier elements,
            // the condition "currentPrice < minSoFar" is enough.
            //
            // Why is this enough?
            // - If currentPrice is smaller than the smallest earlier value,
            //   then it is automatically smaller than all earlier values.
            // - If it is equal to minSoFar, it does NOT count, because the rule says
            //   strictly smaller, not smaller-or-equal.
            if (currentPrice < minSoFar)
            {
                // We found a new low, so increase the answer.
                count++;

                // Update the running minimum to this new smaller value.
                //
                // Why is this necessary?
                // - Future elements must be compared against the smallest value seen so far.
                // - Since currentPrice is now the new smallest value, it becomes the new baseline.
                minSoFar = currentPrice;
            }

            // If currentPrice is not smaller than minSoFar, we do nothing.
            //
            // That means:
            // - If currentPrice > minSoFar, it is clearly not a new low.
            // - If currentPrice == minSoFar, it still does not count because equality
            //   is not strictly smaller.
        }

        // After scanning the whole array, count contains the number of items
        // whose price tag ended in a new low.
        return count;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement:
// prices = [12, 10, 11, 9, 9, 7]
// New lows: 12, 10, 9, 7 => answer = 4
int[] prices1 = { 12, 10, 11, 9, 9, 7 };
int result1 = solution.CountNewLows(prices1);
Console.WriteLine($"Input: [{string.Join(", ", prices1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

// Example 2 from the problem statement:
// prices = [5, 5, 5, 4, 6, 3]
// New lows: 5, 4, 3 => answer = 3
int[] prices2 = { 5, 5, 5, 4, 6, 3 };
int result2 = solution.CountNewLows(prices2);
Console.WriteLine($"Input: [{string.Join(", ", prices2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo: strictly decreasing array
// Every element is a new low.
int[] prices3 = { 8, 7, 6, 5, 4 };
int result3 = solution.CountNewLows(prices3);
Console.WriteLine($"Input: [{string.Join(", ", prices3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

// Additional demo: all equal values
// Only the first counts.
int[] prices4 = { 9, 9, 9, 9 };
int result4 = solution.CountNewLows(prices4);
Console.WriteLine($"Input: [{string.Join(", ", prices4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 1");