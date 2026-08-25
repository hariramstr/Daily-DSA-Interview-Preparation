/*
Title: Count Sensor Readings With Odd Parity
Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
You are given an integer array readings, where each value represents a compact binary reading produced by a sensor.
A reading is considered odd-parity if its binary representation contains an odd number of 1 bits.
Your task is to return how many readings in the array are odd-parity.

For example:
- 5 in binary is 101, which contains two 1 bits, so it is NOT odd-parity.
- 7 in binary is 111, which contains three 1 bits, so it IS odd-parity.

We must count how many numbers in the array have an odd number of set bits (1 bits).

Important correctness check against the examples:
Example 1:
readings = [1, 2, 3, 4]
- 1  -> 1    -> 1 set bit  -> odd
- 2  -> 10   -> 1 set bit  -> odd
- 3  -> 11   -> 2 set bits -> even
- 4  -> 100  -> 1 set bit  -> odd
Answer = 3

Example 2:
readings = [0, 5, 7, 8, 10]
- 0  -> 0    -> 0 set bits -> even
- 5  -> 101  -> 2 set bits -> even
- 7  -> 111  -> 3 set bits -> odd
- 8  -> 1000 -> 1 set bit  -> odd
- 10 -> 1010 -> 2 set bits -> even
Answer = 2

Note:
The written example says Output: 1, but the explanation clearly counts 7 and 8 as odd-parity,
so the correct output is 2. The algorithm below follows the correct definition and explanation.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * number_of_bits)
    - We visit each reading once.
    - For each reading, we inspect its bits using bit manipulation.
    - Since readings[i] <= 10^9, each number has at most about 30 significant bits.

    Space Complexity:
    O(1)
    - We only use a few integer variables.
    - No extra data structures that grow with input size.
    */
    public int CountOddParityReadings(int[] readings)
    {
        // This variable will store the final answer:
        // how many readings have an odd number of 1 bits.
        int oddParityCount = 0;

        // We process each reading one by one.
        // This is necessary because the parity of each number is independent.
        for (int i = 0; i < readings.Length; i++)
        {
            // Get the current sensor reading.
            int value = readings[i];

            // This variable will count how many 1 bits appear in the current number.
            int setBitCount = 0;

            // We make a copy-like working variable by using 'value' directly.
            // The loop continues until all bits have been removed.
            //
            // Why does this work?
            // - Each iteration checks the least significant bit (the rightmost bit).
            // - Then we shift the number right by one position.
            // - Eventually the number becomes 0, meaning there are no more bits to inspect.
            while (value > 0)
            {
                // Step 1: Check whether the current least significant bit is 1.
                //
                // (value & 1) isolates the rightmost bit:
                // - If the result is 1, the rightmost bit was 1.
                // - If the result is 0, the rightmost bit was 0.
                //
                // Example:
                // value = 10 (binary 1010)
                // 1010 & 0001 = 0000 -> rightmost bit is 0
                //
                // value = 7 (binary 0111)
                // 0111 & 0001 = 0001 -> rightmost bit is 1
                if ((value & 1) == 1)
                {
                    // If the rightmost bit is 1, increase the set bit count.
                    setBitCount++;
                }

                // Step 2: Shift the number right by 1 bit.
                //
                // This discards the bit we just processed and moves the next bit
                // into the least significant position so we can inspect it next.
                //
                // Example:
                // 13 = 1101
                // after shifting right once -> 110 = 6
                value >>= 1;
            }

            // After counting all 1 bits in the current reading,
            // we determine whether that count is odd.
            //
            // A number is odd if dividing by 2 leaves remainder 1.
            // Using bit manipulation, (setBitCount & 1) checks that efficiently:
            // - 1 means odd
            // - 0 means even
            if ((setBitCount & 1) == 1)
            {
                // If the current reading has odd parity,
                // include it in the final answer.
                oddParityCount++;
            }
        }

        // Return the total number of odd-parity readings.
        return oddParityCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement
int[] readings1 = { 1, 2, 3, 4 };
int result1 = solution.CountOddParityReadings(readings1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", readings1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Example 2 from the problem statement
// Important: the explanation shows the correct answer is 2, not 1.
int[] readings2 = { 0, 5, 7, 8, 10 };
int result2 = solution.CountOddParityReadings(readings2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", readings2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected based on explanation: 2");
Console.WriteLine();

// Additional small demo
int[] readings3 = { 15, 16, 31, 32, 33 };
// 15 -> 1111  -> 4 set bits -> even
// 16 -> 10000 -> 1 set bit  -> odd
// 31 -> 11111 -> 5 set bits -> odd
// 32 -> 100000 -> 1 set bit -> odd
// 33 -> 100001 -> 2 set bits -> even
// Total odd-parity readings = 3
int result3 = solution.CountOddParityReadings(readings3);
Console.WriteLine("Additional Demo:");
Console.WriteLine($"Input: [{string.Join(", ", readings3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 3");