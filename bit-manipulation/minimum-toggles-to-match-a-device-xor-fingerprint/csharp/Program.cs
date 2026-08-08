/*
Title: Minimum Toggles to Match a Device XOR Fingerprint
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
A hardware lab stores the state of n devices as an integer array states, where states[i] is a non-negative 32-bit integer.
The lab wants the XOR of all device states to become exactly target.

In one operation, you may choose any single device and toggle exactly one bit in its binary representation
(change a 0 to 1 or a 1 to 0 at one bit position).

Return the minimum number of bit toggles required so that the XOR of the entire array equals target.

This is not asking you to transform each number into a specific value. You may toggle bits on any devices in any order,
and only the final XOR of all numbers matters. A toggle on one device affects the global XOR at exactly that bit position.
Because XOR is independent across bit positions, the answer depends only on which bits differ between the current overall XOR and target.

Formally, let current = states[0] XOR states[1] XOR ... XOR states[n - 1].
Find the minimum number of single-bit toggles needed to make current become target.

Constraints:
- 1 <= n <= 200000
- 0 <= states[i] <= 10^9
- 0 <= target <= 10^9
- Your solution should run in O(n) time and use O(1) extra space, excluding input storage.

Example 1:
Input: states = [5, 1, 2], target = 0
Output: 2
Explanation:
current XOR = 5 XOR 1 XOR 2 = 6.
Binary 6 is 110, while target 0 is 000.
Two bit positions differ, so two single-bit toggles are sufficient.

Example 2:
Input: states = [7, 7, 7], target = 7
Output: 0
Explanation:
current XOR = 7 XOR 7 XOR 7 = 7, which already matches target, so no operation is needed.

Key Insight:
- Toggling one bit in one device flips exactly one bit in the overall XOR.
- Therefore, each differing bit between current XOR and target requires exactly one toggle.
- So the answer is the number of set bits in (current XOR target), also called the Hamming distance in binary.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array once to compute the XOR of all device states.
    - Then we count the set bits in one integer, which takes O(1) time in practice for 32-bit integers.

    Space Complexity: O(1)
    - We use only a few integer variables.
    - No extra data structures are needed beyond the input array.
    */
    public int MinimumToggles(int[] states, int target)
    {
        // This variable will store the XOR of all values in the array.
        // We start from 0 because 0 is the identity value for XOR:
        // x ^ 0 = x
        int currentXor = 0;

        // Step 1:
        // Compute the XOR of the entire array.
        //
        // Why this is necessary:
        // The problem only cares about the XOR of all device states, not the exact final value of each device.
        // So instead of thinking about changing many numbers individually, we reduce the whole array
        // to one combined value: the current global XOR.
        //
        // Data structure choice:
        // We do not need any extra data structure here.
        // A single running integer is enough because XOR can be accumulated incrementally.
        foreach (int state in states)
        {
            // Combine the next device state into the running XOR.
            currentXor ^= state;
        }

        // Step 2:
        // Find which bit positions differ between the current XOR and the target.
        //
        // Why XOR again?
        // For any bit position:
        // - If currentXor and target have the same bit, then no change is needed at that bit.
        // - If they differ, then exactly one toggle is needed somewhere in the array at that bit position.
        //
        // currentXor ^ target produces a number whose set bits are exactly the differing positions.
        int difference = currentXor ^ target;

        // Step 3:
        // Count how many bits are set to 1 in 'difference'.
        //
        // Why this gives the answer:
        // Each set bit represents one bit position where the current global XOR does not match the target.
        // One single-device, single-bit toggle flips exactly one bit in the global XOR.
        // Therefore, each differing bit requires one operation, and the minimum number of operations
        // is exactly the number of set bits in 'difference'.
        int togglesNeeded = 0;

        // We use Brian Kernighan's bit-counting technique.
        //
        // How it works:
        // - For any positive integer x, the expression x & (x - 1) removes the lowest set bit.
        // - So each loop iteration removes one 1-bit.
        // - Therefore, the number of iterations equals the number of set bits.
        //
        // Why this is a good choice:
        // - It is simple and efficient.
        // - It avoids checking all 32 bit positions one by one, though that would also be acceptable.
        while (difference != 0)
        {
            // Remove the lowest set bit from 'difference'.
            difference &= (difference - 1);

            // Since we removed exactly one set bit, we count one required toggle.
            togglesNeeded++;
        }

        // After all differing bits have been counted, this is the minimum number of operations.
        return togglesNeeded;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Create an instance of the solution class.
var solution = new Solution();

// Example 1:
// states = [5, 1, 2], target = 0
// current XOR = 5 ^ 1 ^ 2 = 6
// 6 in binary is 110
// target 0 in binary is 000
// Two bits differ, so answer should be 2
int[] states1 = { 5, 1, 2 };
int target1 = 0;
int result1 = solution.MinimumToggles(states1, target1);
Console.WriteLine(result1); // Expected: 2

// Example 2:
// states = [7, 7, 7], target = 7
// current XOR = 7 ^ 7 ^ 7 = 7
// Already equal to target, so answer should be 0
int[] states2 = { 7, 7, 7 };
int target2 = 7;
int result2 = solution.MinimumToggles(states2, target2);
Console.WriteLine(result2); // Expected: 0

// Additional demo example:
// states = [1, 2, 3], target = 4
// current XOR = 1 ^ 2 ^ 3 = 0
// difference = 0 ^ 4 = 4, which has one set bit
// So answer should be 1
int[] states3 = { 1, 2, 3 };
int target3 = 4;
int result3 = solution.MinimumToggles(states3, target3);
Console.WriteLine(result3); // Expected: 1