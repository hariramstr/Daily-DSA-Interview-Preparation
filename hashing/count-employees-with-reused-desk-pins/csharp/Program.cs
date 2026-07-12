/*
Title: Count Employees With Reused Desk PINs
Difficulty: Easy
Topic: Hashing

Problem Description:
A company assigns each employee a temporary desk PIN for one day. You are given an array `pins`
where `pins[i]` is the PIN used by the `i`-th employee in the check-in list. Some PINs may appear
more than once because employees accidentally reused an existing temporary PIN instead of generating
a new one.

Your task is to return the number of employees whose PIN is not unique in the list. In other words,
count how many positions belong to a PIN value that appears at least twice.

For example, if the PIN list is [4312, 9981, 4312, 7777, 9981], then PIN 4312 is used by 2 employees
and PIN 9981 is used by 2 employees, so the answer is 4. The employee with PIN 7777 is not counted
because that PIN appears only once.

This problem should be solved efficiently using hashing. A straightforward approach is to count the
frequency of each PIN, then sum the frequencies of all PINs that occur more than once.

Constraints:
- 1 <= pins.length <= 100000
- 0 <= pins[i] <= 1000000000
- The answer fits in a 32-bit integer.

Example 1:
Input: pins = [4312, 9981, 4312, 7777, 9981]
Output: 4
Explanation: Two employees used 4312 and two employees used 9981, so 4 employees are counted.

Example 2:
Input: pins = [12, 34, 56, 78]
Output: 0
Explanation: Every PIN appears exactly once, so no employee reused a PIN.

We return the total number of employees whose desk PIN appears more than once anywhere in the array.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We make one pass to count how many times each PIN appears.
    - We make another pass over the frequency map to add counts for duplicated PINs.
    - Since each employee is processed a constant number of times, the total work is linear.

    Space Complexity: O(n)
    - In the worst case, every PIN is different, so the dictionary stores up to n entries.
    - This extra storage is used to remember frequencies.
    */
    public int CountEmployeesWithReusedPins(int[] pins)
    {
        // This dictionary is our hashing-based data structure.
        // Key   = a PIN value
        // Value = how many times that PIN appears in the input array
        //
        // Why use a Dictionary?
        // - We need fast lookup and update by PIN value.
        // - A dictionary gives average O(1) time for checking whether a PIN
        //   has been seen before and for increasing its count.
        var frequency = new Dictionary<int, int>();

        // STEP 1: Count the frequency of every PIN.
        //
        // What this loop is doing:
        // - We visit each employee's PIN one by one.
        // - For each PIN, we either:
        //   a) create a new entry with count 1 if this is the first time we see it
        //   b) increase the existing count if we have seen it before
        //
        // Why this step is necessary:
        // - We cannot know whether a PIN is reused until we know its total count.
        // - So first we build a complete frequency table.
        foreach (int pin in pins)
        {
            // Check whether this PIN is already present in the dictionary.
            if (frequency.ContainsKey(pin))
            {
                // If yes, one more employee used the same PIN,
                // so we increase its frequency by 1.
                frequency[pin]++;
            }
            else
            {
                // If no, this is the first employee using this PIN,
                // so we start its count at 1.
                frequency[pin] = 1;
            }
        }

        // This variable will store the final answer:
        // the total number of employees whose PIN appears more than once.
        int reusedEmployeeCount = 0;

        // STEP 2: Add up frequencies for only the duplicated PINs.
        //
        // What this loop is doing:
        // - We inspect each (PIN, count) pair in the frequency dictionary.
        // - If a PIN appears at least twice, then every employee using that PIN
        //   should be counted.
        //
        // Why this step is necessary:
        // - The problem does NOT ask for the number of duplicated PIN values.
        // - It asks for the number of employees whose PIN is part of a duplicate group.
        // - So if a PIN appears 3 times, we add 3, not 1.
        foreach (KeyValuePair<int, int> entry in frequency)
        {
            int count = entry.Value;

            // If the count is greater than 1, this PIN was reused.
            if (count > 1)
            {
                // Add the full count because every employee with this PIN
                // belongs to a non-unique PIN group.
                reusedEmployeeCount += count;
            }
        }

        // STEP 3: Return the computed total.
        //
        // At this point:
        // - Every unique PIN contributed 0
        // - Every duplicated PIN contributed its full frequency
        //
        // Example trace:
        // pins = [4312, 9981, 4312, 7777, 9981]
        // frequency becomes:
        //   4312 -> 2
        //   9981 -> 2
        //   7777 -> 1
        // Sum counts > 1:
        //   2 + 2 = 4
        //
        // Another example:
        // pins = [12, 34, 56, 78]
        // frequency becomes:
        //   12 -> 1
        //   34 -> 1
        //   56 -> 1
        //   78 -> 1
        // No count is greater than 1, so answer = 0
        return reusedEmployeeCount;
    }
}

// Demo code:
// We create sample inputs, call the solution method, and print the results.

// Create an instance of the solution class.
var solution = new Solution();

// Example 1 from the problem description.
// PIN 4312 appears twice.
// PIN 9981 appears twice.
// PIN 7777 appears once.
// Therefore, 2 + 2 = 4 employees should be counted.
int[] pins1 = { 4312, 9981, 4312, 7777, 9981 };
int result1 = solution.CountEmployeesWithReusedPins(pins1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2 from the problem description.
// Every PIN appears exactly once, so no employee is counted.
int[] pins2 = { 12, 34, 56, 78 };
int result2 = solution.CountEmployeesWithReusedPins(pins2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo:
// PIN 5 appears 3 times, so all 3 employees using PIN 5 are counted.
// PIN 9 appears once, so it is not counted.
// Answer should be 3.
int[] pins3 = { 5, 9, 5, 5 };
int result3 = solution.CountEmployeesWithReusedPins(pins3);
Console.WriteLine($"Additional Example Result: {result3}");