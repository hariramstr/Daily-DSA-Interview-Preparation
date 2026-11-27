/*
Title: Find the Earliest Repeated Temperature Reading
Difficulty: Easy
Topic: Arrays

Problem Description:
A weather station stores hourly temperature readings in an integer array temps, where temps[i]
is the recorded temperature at hour i.

Your task is to find the first temperature value that appears more than once when scanning
the array from left to right. Return the temperature whose second occurrence happens earliest.

If no temperature is repeated, return -1.

Important clarification:
- We are NOT looking for the smallest repeated value.
- We are NOT looking for the most frequent value.
- We ARE looking for the first value that becomes a duplicate during a left-to-right scan.

Example:
temps = [18, 21, 19, 21, 18]
Scan from left to right:
- 18 -> first time seen
- 21 -> first time seen
- 19 -> first time seen
- 21 -> already seen before, so this is the earliest repeated reading
Answer: 21

Constraints:
- 1 <= temps.length <= 100000
- -1000000 <= temps[i] <= 1000000
- The solution should run efficiently for large arrays.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array once.
    - Each HashSet operation (Contains/Add) is O(1) on average.

    Space Complexity: O(n)
    - In the worst case, if all temperatures are unique, we store all of them in the HashSet.

    Beginner-friendly idea:
    We keep track of every temperature we have already seen.
    As soon as we find a temperature that is already in our "seen" collection,
    that means we have found the earliest repeated temperature by scan order.
    */
    public int FindEarliestRepeatedTemperature(int[] temps)
    {
        // This HashSet will store every temperature value we have seen so far
        // while scanning from left to right.
        //
        // Why use a HashSet?
        // - We need to quickly answer the question:
        //   "Have we seen this temperature before?"
        // - HashSet is ideal for this because Contains and Add are very fast on average.
        //
        // Example:
        // If seen = {18, 21, 19} and current temperature is 21,
        // then seen.Contains(21) is true, so 21 is a duplicate.
        HashSet<int> seen = new HashSet<int>();

        // We now scan the array from the first reading to the last reading.
        //
        // This left-to-right order is extremely important because the problem asks for
        // the temperature whose SECOND occurrence happens earliest.
        //
        // That means the first time we encounter a duplicate during this scan,
        // we should immediately return it.
        for (int i = 0; i < temps.Length; i++)
        {
            // Read the current temperature at index i.
            int currentTemp = temps[i];

            // Step 1: Check whether this temperature has already appeared earlier.
            //
            // Why is this necessary?
            // - If it has appeared before, then the current position is its second
            //   (or later) occurrence.
            // - Since we are scanning from left to right, the FIRST duplicate we detect
            //   is exactly the answer the problem wants.
            if (seen.Contains(currentTemp))
            {
                // We found a temperature that was already seen before.
                //
                // This means:
                // - currentTemp appeared at some earlier index
                // - and now it appears again at index i
                //
                // Because we are moving left to right, this is the earliest moment
                // any value becomes repeated.
                //
                // Example trace for [18, 21, 19, 21, 18]:
                // i=0 -> 18 not seen, add it
                // i=1 -> 21 not seen, add it
                // i=2 -> 19 not seen, add it
                // i=3 -> 21 already seen -> return 21
                return currentTemp;
            }

            // Step 2: If the temperature was not seen before, record it now.
            //
            // Why do we add it?
            // - So that if the same value appears later, we can recognize it as a duplicate.
            //
            // Example:
            // If currentTemp = 18 and it is not in seen,
            // after seen.Add(18), any future 18 will be detected as repeated.
            seen.Add(currentTemp);
        }

        // If we finish the entire scan without finding any duplicate,
        // then no temperature ever appeared more than once.
        //
        // Example:
        // [5, 7, 8, 9]
        // - 5 not seen, add
        // - 7 not seen, add
        // - 8 not seen, add
        // - 9 not seen, add
        // End of loop -> no duplicate found -> return -1
        return -1;
    }
}

// Demo code

Solution solution = new Solution();

// Example 1 from the problem description
int[] temps1 = { 18, 21, 19, 21, 18 };
int result1 = solution.FindEarliestRepeatedTemperature(temps1);
Console.WriteLine(result1); // Expected: 21

// Example 2 from the problem description
int[] temps2 = { 5, 7, 8, 9 };
int result2 = solution.FindEarliestRepeatedTemperature(temps2);
Console.WriteLine(result2); // Expected: -1

// Additional demo cases

// Duplicate appears immediately
int[] temps3 = { 10, 10, 12, 13 };
int result3 = solution.FindEarliestRepeatedTemperature(temps3);
Console.WriteLine(result3); // Expected: 10

// Negative temperatures
int[] temps4 = { -3, -1, -2, -3, -1 };
int result4 = solution.FindEarliestRepeatedTemperature(temps4);
Console.WriteLine(result4); // Expected: -3

// Multiple repeats, but earliest second occurrence decides the answer
int[] temps5 = { 30, 25, 30, 25, 25 };
int result5 = solution.FindEarliestRepeatedTemperature(temps5);
Console.WriteLine(result5); // Expected: 30