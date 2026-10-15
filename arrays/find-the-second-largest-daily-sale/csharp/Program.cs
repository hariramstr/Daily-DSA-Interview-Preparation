/*
Title: Find the Second Largest Daily Sale
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an integer array sales where sales[i] represents the total sales made on day i.
Your task is to return the second largest distinct value in the array.

A value is considered distinct only if it is different from the largest value.
If there is no second largest distinct value, return -1.

This problem is about scanning an array efficiently while keeping track of the two best
distinct values seen so far. Duplicate values should not be counted multiple times.
For example, if the largest value appears several times, you still need the next smaller
distinct value.

Constraints:
- 1 <= sales.length <= 10^5
- -10^9 <= sales[i] <= 10^9
- The solution should run in O(n) time.
- Extra space should be O(1), not counting the input array.

Examples:
1) sales = [12, 7, 19, 19, 5, 14]
   Output: 14
   Explanation:
   Distinct values are 12, 7, 19, 5, 14.
   Largest distinct value = 19
   Second largest distinct value = 14

2) sales = [8, 8, 8]
   Output: -1
   Explanation:
   There is only one distinct value, so no second largest distinct value exists.

Important Notes:
- The solution must correctly handle negative numbers.
- The solution must correctly handle repeated values.
- The solution must correctly handle arrays of length 1.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - Each element is processed in constant time.

    Space Complexity: O(1)
    - We only store a few variables: the largest and second largest distinct values.
    - We do not use any extra data structure that grows with input size.
    */
    public int FindSecondLargestDistinct(int[] sales)
    {
        // We use nullable integers here because we need a clean way to represent:
        // "We have not found a valid value yet."
        //
        // Why nullable instead of using int.MinValue as a placeholder?
        // Because the input itself is allowed to contain int values as low as -1,000,000,000,
        // and in general using a special numeric sentinel can be confusing for beginners.
        // Nullable values make the intent very clear:
        // - largest == null means no largest distinct value has been recorded yet
        // - secondLargest == null means no second largest distinct value has been recorded yet
        int? largest = null;
        int? secondLargest = null;

        // We now scan every sale value exactly once.
        // The goal during this scan is to always maintain:
        // - largest      = the biggest distinct value seen so far
        // - secondLargest = the second biggest distinct value seen so far
        //
        // "Distinct" is the key word:
        // duplicates of the current largest should NOT become the second largest.
        foreach (int sale in sales)
        {
            // STEP 1:
            // If we have not recorded any largest value yet,
            // then this current sale automatically becomes the largest.
            //
            // Why is this necessary?
            // Because on the very first element, we need to initialize our tracking variables.
            if (largest == null)
            {
                largest = sale;
                continue;
            }

            // STEP 2:
            // If the current sale is exactly equal to the current largest,
            // we skip it.
            //
            // Why?
            // Because the problem asks for the second largest DISTINCT value.
            // So repeated copies of the largest value do not help us.
            //
            // Example:
            // sales = [19, 19, 14]
            // The second 19 should not be treated as second largest.
            if (sale == largest.Value)
            {
                continue;
            }

            // STEP 3:
            // If the current sale is greater than the current largest,
            // then we have found a new largest distinct value.
            //
            // When that happens:
            // - the old largest becomes the second largest
            // - the current sale becomes the new largest
            //
            // Why is this correct?
            // Because before seeing this sale, largest was the biggest distinct value.
            // If sale is even bigger, then the previous largest is now exactly the
            // second biggest distinct value seen so far.
            if (sale > largest.Value)
            {
                secondLargest = largest;
                largest = sale;
                continue;
            }

            // STEP 4:
            // At this point, we already know:
            // - sale is NOT equal to largest
            // - sale is NOT greater than largest
            //
            // Therefore, sale must be smaller than largest.
            // That means it is a candidate for secondLargest.
            //
            // We should update secondLargest if:
            // 1) secondLargest has not been set yet, OR
            // 2) sale is bigger than the current secondLargest
            //
            // Why?
            // Because secondLargest should always store the biggest value
            // that is still smaller than largest.
            if (secondLargest == null || sale > secondLargest.Value)
            {
                secondLargest = sale;
            }

            // Note:
            // We do NOT need a separate duplicate check against secondLargest here.
            //
            // Example:
            // If secondLargest is already 14 and we see another 14,
            // the condition sale > secondLargest.Value will be false,
            // so nothing changes.
        }

        // After scanning the entire array:
        // - If secondLargest is still null, then no second distinct value was found.
        //   Return -1 as required by the problem.
        // - Otherwise return the second largest distinct value.
        return secondLargest ?? -1;
    }
}

// Demo code:
// We create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1 from the problem description
int[] sales1 = { 12, 7, 19, 19, 5, 14 };
int result1 = solution.FindSecondLargestDistinct(sales1);
Console.WriteLine($"Input: [{string.Join(", ", sales1)}]");
Console.WriteLine($"Second largest distinct sale: {result1}");
Console.WriteLine("Expected: 14");
Console.WriteLine();

// Example 2 from the problem description
int[] sales2 = { 8, 8, 8 };
int result2 = solution.FindSecondLargestDistinct(sales2);
Console.WriteLine($"Input: [{string.Join(", ", sales2)}]");
Console.WriteLine($"Second largest distinct sale: {result2}");
Console.WriteLine("Expected: -1");
Console.WriteLine();

// Additional demo: negative numbers
int[] sales3 = { -10, -3, -3, -20, -7 };
int result3 = solution.FindSecondLargestDistinct(sales3);
Console.WriteLine($"Input: [{string.Join(", ", sales3)}]");
Console.WriteLine($"Second largest distinct sale: {result3}");
Console.WriteLine("Expected: -7");
Console.WriteLine();

// Additional demo: array of length 1
int[] sales4 = { 42 };
int result4 = solution.FindSecondLargestDistinct(sales4);
Console.WriteLine($"Input: [{string.Join(", ", sales4)}]");
Console.WriteLine($"Second largest distinct sale: {result4}");
Console.WriteLine("Expected: -1");
Console.WriteLine();

// Additional demo: largest appears multiple times
int[] sales5 = { 5, 9, 9, 9, 4, 8 };
int result5 = solution.FindSecondLargestDistinct(sales5);
Console.WriteLine($"Input: [{string.Join(", ", sales5)}]");
Console.WriteLine($"Second largest distinct sale: {result5}");
Console.WriteLine("Expected: 8");