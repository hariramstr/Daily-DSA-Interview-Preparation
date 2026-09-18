/*
Title: Find the First Day Inventory Never Drops
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an integer array stock where stock[i] represents the inventory level of a product at the end of day i.
A day is considered stable if its inventory is greater than or equal to the inventory of the previous day.
Your task is to return the index of the first day from which the inventory never drops again for the rest of the array.

More formally, find the smallest index i such that for every j where i < j < n, stock[j] >= stock[j - 1].
In other words, the subarray stock[i...n-1] must be non-decreasing.
If the entire array is already non-decreasing, return 0.

This problem models a real inventory dashboard where managers want to know the first day after which stock levels stop declining and only stay the same or increase.

Constraints:
- 1 <= stock.length <= 100000
- -1000000000 <= stock[i] <= 1000000000
- The answer is always a valid index from 0 to n - 1

Example 1:
Input: stock = [9, 7, 8, 8, 10]
Output: 1
Explanation:
Starting from index 1, the values are [7, 8, 8, 10], which is non-decreasing.
Index 0 does not work because 7 < 9, so inventory drops between day 0 and day 1.

Example 2:
Input: stock = [5, 6, 4, 7, 9]
Output: 2
Explanation:
The suffix starting at index 2 is [4, 7, 9], which is non-decreasing.
Any earlier starting index fails because there is a drop from 6 to 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan the array once from right to left.
    - Each element is processed a constant number of times.

    Space Complexity: O(1)
    - We only use a few extra variables.
    - No additional arrays or collections are needed.
    */
    public int FirstStableDay(int[] stock)
    {
        // If the array has only one day, then that single day is automatically the answer.
        // Why? Because there are no later days where a drop could happen.
        if (stock.Length == 1)
        {
            return 0;
        }

        // This variable will store the earliest index we currently know
        // from which the suffix is non-decreasing.
        //
        // We begin by assuming the last index is valid.
        // Why is the last index always valid?
        // Because a suffix containing only one element is always non-decreasing.
        int answer = stock.Length - 1;

        // We scan from right to left.
        //
        // Why right to left?
        // Because we want to know whether stock[i...n-1] is non-decreasing.
        // If we already know that stock[i+1...n-1] is non-decreasing,
        // then to decide whether stock[i...n-1] is also non-decreasing,
        // we only need to check one new condition:
        //
        // stock[i] <= stock[i + 1]
        //
        // If that is true, then adding stock[i] in front keeps the suffix non-decreasing.
        // If that is false, then the suffix cannot start at i.
        for (int i = stock.Length - 2; i >= 0; i--)
        {
            // Current step:
            // We compare the current day's inventory with the next day's inventory.
            //
            // Why this comparison?
            // For a sequence to be non-decreasing, every next value must be
            // greater than or equal to the previous one.
            // So when looking at positions i and i+1, we need:
            //
            // stock[i + 1] >= stock[i]
            //
            // which is equivalent to:
            //
            // stock[i] <= stock[i + 1]
            if (stock[i] <= stock[i + 1])
            {
                // If this condition is true, then the pair (i, i+1) does not create a drop.
                //
                // Also, because we are scanning from right to left, we already know
                // that the suffix starting at i+1 is non-decreasing whenever answer == i+1
                // or answer is even further left due to previous successful checks.
                //
                // Therefore, if stock[i] <= stock[i+1], the suffix starting at i
                // is also non-decreasing, so we can move the answer left to i.
                answer = i;
            }
            else
            {
                // If stock[i] > stock[i + 1], then there is a drop between day i and day i+1.
                //
                // That means index i cannot be the start of a non-decreasing suffix.
                // It also means no index earlier than i can currently be accepted
                // unless a later valid suffix start exists after this drop.
                //
                // We do not update answer here.
                // We simply continue scanning left.
            }
        }

        // After finishing the scan, answer holds the smallest index
        // from which the array never drops again.
        return answer;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

int[] stock1 = { 9, 7, 8, 8, 10 };
int result1 = solution.FirstStableDay(stock1);
Console.WriteLine(result1); // Expected: 1

int[] stock2 = { 5, 6, 4, 7, 9 };
int result2 = solution.FirstStableDay(stock2);
Console.WriteLine(result2); // Expected: 2

int[] stock3 = { 1, 2, 3, 4 };
int result3 = solution.FirstStableDay(stock3);
Console.WriteLine(result3); // Expected: 0

int[] stock4 = { 4, 3, 2, 1 };
int result4 = solution.FirstStableDay(stock4);
Console.WriteLine(result4); // Expected: 3

int[] stock5 = { 8 };
int result5 = solution.FirstStableDay(stock5);
Console.WriteLine(result5); // Expected: 0