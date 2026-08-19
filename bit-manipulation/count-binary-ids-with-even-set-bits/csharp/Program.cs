/*
Title: Count Binary IDs With Even Set Bits
Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
A warehouse system stores item IDs as non-negative integers. For a quick integrity check, an ID is called valid if its binary representation contains an even number of 1 bits. Given an integer array ids, return how many IDs are valid.

For example, the number 10 is binary 1010, which contains two 1 bits, so it is valid. The number 7 is binary 111, which contains three 1 bits, so it is not valid.

Your task is to scan the array and count how many values have even bit parity. A straightforward solution can examine each number independently and count its set bits using bit manipulation operations such as shifting or repeatedly clearing the lowest set bit.

Constraints:
- 1 <= ids.length <= 100000
- 0 <= ids[i] <= 10^9
- The answer fits in a 32-bit integer

Example 1:
Input: ids = [0, 1, 2, 3, 4]
Output: 2
Explanation: 0 has 0 set bits (even), 1 has 1, 2 has 1, 3 has 2 (even), and 4 has 1. So only 0 and 3 are valid.

Example 2:
Input: ids = [5, 6, 7, 8, 15]
Output: 3
Explanation: 5 is 101 (2 set bits), 6 is 110 (2 set bits), 7 is 111 (3 set bits), 8 is 1000 (1 set bit), and 15 is 1111 (4 set bits). The valid IDs are 5, 6, and 15.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of IDs.
    - For each number, we repeatedly clear its lowest set bit.
    - That loop runs once per 1 bit in the number.
    - In the worst case for values up to 10^9, this is still very small (at most around 30 bits).
    - So the total complexity is O(n * number_of_set_bits_per_value), which is effectively O(n) for these constraints.

    Space Complexity:
    - O(1)
    - We only use a few integer variables and do not allocate extra data structures proportional to input size.
    */
    public int CountValidIds(int[] ids)
    {
        // This variable will store the final answer:
        // how many numbers in the array have an even number of 1 bits.
        int validCount = 0;

        // We examine each ID one by one.
        // This is necessary because the validity of each number depends only on its own binary representation.
        for (int i = 0; i < ids.Length; i++)
        {
            // Read the current number from the array.
            int currentId = ids[i];

            // This variable will count how many 1 bits are present in currentId.
            int setBitCount = 0;

            // We use a classic bit manipulation trick:
            // x & (x - 1) removes the lowest set bit from x.
            //
            // Why this is useful:
            // - It lets us count set bits efficiently.
            // - Instead of checking every bit position one by one,
            //   we only loop once for each actual 1 bit.
            //
            // Example:
            // currentId = 10 -> binary 1010
            // 1010 & 1001 = 1000
            // 1000 & 0111 = 0000
            // We performed the loop exactly 2 times, which matches the number of 1 bits.
            while (currentId > 0)
            {
                // Count the set bit that we are about to remove.
                setBitCount++;

                // Remove the lowest set bit.
                currentId = currentId & (currentId - 1);
            }

            // After counting all 1 bits, we check whether the count is even.
            //
            // Why modulo 2?
            // - Even numbers leave remainder 0 when divided by 2.
            // - Odd numbers leave remainder 1.
            //
            // If setBitCount is even, this ID is valid according to the problem statement.
            if (setBitCount % 2 == 0)
            {
                validCount++;
            }
        }

        // After processing every ID, return the total number of valid IDs.
        return validCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement:
// ids = [0, 1, 2, 3, 4]
//
// Trace:
// 0 -> binary 0    -> 0 set bits -> even  -> valid
// 1 -> binary 1    -> 1 set bit  -> odd   -> not valid
// 2 -> binary 10   -> 1 set bit  -> odd   -> not valid
// 3 -> binary 11   -> 2 set bits -> even  -> valid
// 4 -> binary 100  -> 1 set bit  -> odd   -> not valid
//
// Total valid = 2
int[] ids1 = { 0, 1, 2, 3, 4 };
int result1 = solution.CountValidIds(ids1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2 from the problem statement:
// ids = [5, 6, 7, 8, 15]
//
// Trace:
// 5  -> binary 101   -> 2 set bits -> even -> valid
// 6  -> binary 110   -> 2 set bits -> even -> valid
// 7  -> binary 111   -> 3 set bits -> odd  -> not valid
// 8  -> binary 1000  -> 1 set bit  -> odd  -> not valid
// 15 -> binary 1111  -> 4 set bits -> even -> valid
//
// Total valid = 3
int[] ids2 = { 5, 6, 7, 8, 15 };
int result2 = solution.CountValidIds(ids2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional small demo:
// 10 -> binary 1010 -> 2 set bits -> valid
// 7  -> binary 111  -> 3 set bits -> not valid
int[] ids3 = { 10, 7 };
int result3 = solution.CountValidIds(ids3);
Console.WriteLine("Additional Demo Result: " + result3);