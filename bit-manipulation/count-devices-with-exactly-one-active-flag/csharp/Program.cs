/*
Title: Count Devices with Exactly One Active Flag
Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
You are given an array `states` where each integer represents the status flags of a device.
In the binary representation of `states[i]`, each bit indicates whether a particular feature
is enabled (`1`) or disabled (`0`).

A device is considered "simple-active" if it has exactly one enabled feature, meaning its
binary form contains exactly one set bit.

Your task is to return how many devices in the array are simple-active.

Examples:
- 1  -> 0001 -> simple-active
- 2  -> 0010 -> simple-active
- 4  -> 0100 -> simple-active
- 8  -> 1000 -> simple-active
- 0  -> 0000 -> not simple-active
- 3  -> 0011 -> not simple-active
- 6  -> 0110 -> not simple-active
- 10 -> 1010 -> not simple-active

Key Bit Manipulation Observation:
A positive number `x` has exactly one set bit if and only if:
    (x & (x - 1)) == 0

Why this works:
- If `x` has exactly one set bit, subtracting 1 flips that bit to 0 and turns all lower bits to 1.
- Therefore, `x` and `x - 1` will share no common set bits.
- Their bitwise AND becomes 0.
- We must also ensure `x > 0`, because 0 would incorrectly satisfy:
    (0 & -1) == 0
  but 0 has no set bits, not one.

Constraints:
- 1 <= states.length <= 100000
- 0 <= states[i] <= 10^9
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We examine each number in the array exactly once.
    - For each number, the bit check is O(1).

    Space Complexity: O(1)
    - We only use a few extra variables regardless of input size.
    */
    public int CountSimpleActiveDevices(int[] states)
    {
        // This variable will store the total number of devices
        // that have exactly one active flag (one set bit).
        int count = 0;

        // We loop through every device state in the input array.
        // This is necessary because the problem asks us to count
        // how many values satisfy the condition.
        for (int i = 0; i < states.Length; i++)
        {
            // Read the current device state.
            // Storing it in a local variable makes the code easier to read
            // and avoids repeatedly indexing into the array.
            int currentState = states[i];

            // Step 1: Check that the number is positive.
            //
            // Why is this necessary?
            // - The value 0 has no set bits.
            // - However, the expression (x & (x - 1)) == 0 is also true for x = 0.
            // - So we must explicitly exclude 0.
            //
            // Step 2: Apply the classic bit trick:
            //     currentState & (currentState - 1)
            //
            // Why does this identify numbers with exactly one set bit?
            // Example:
            //   currentState = 8  -> binary 1000
            //   currentState - 1 = 7 -> binary 0111
            //   1000 & 0111 = 0000
            //
            // For a number with more than one set bit:
            //   currentState = 10 -> binary 1010
            //   currentState - 1 = 9  -> binary 1001
            //   1010 & 1001 = 1000 (not zero)
            //
            // So the condition below is true exactly when the number
            // has one and only one set bit.
            if (currentState > 0 && (currentState & (currentState - 1)) == 0)
            {
                // If the current value is simple-active,
                // increase our answer by 1.
                count++;
            }
        }

        // After checking all device states, return the total count.
        return count;
    }
}

// Demo code:
// We create sample inputs, call the solution method, and print the results.

// Create an instance of the solution class so we can call the algorithm method.
var solution = new Solution();

// Example 1:
// states = [1, 2, 3, 4, 6]
// 1 -> one set bit  -> count
// 2 -> one set bit  -> count
// 3 -> two set bits -> do not count
// 4 -> one set bit  -> count
// 6 -> two set bits -> do not count
// Expected output: 3
int[] states1 = { 1, 2, 3, 4, 6 };
int result1 = solution.CountSimpleActiveDevices(states1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// states = [0, 7, 8, 16, 18]
// 0  -> zero set bits      -> do not count
// 7  -> three set bits     -> do not count
// 8  -> one set bit        -> count
// 16 -> one set bit        -> count
// 18 -> two set bits       -> do not count
// Expected output: 2
int[] states2 = { 0, 7, 8, 16, 18 };
int result2 = solution.CountSimpleActiveDevices(states2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick demo:
// states = [1, 2, 4, 8, 0, 3, 6, 10]
// simple-active values are 1, 2, 4, 8
// Expected output: 4
int[] states3 = { 1, 2, 4, 8, 0, 3, 6, 10 };
int result3 = solution.CountSimpleActiveDevices(states3);
Console.WriteLine($"Additional Demo Result: {result3}");