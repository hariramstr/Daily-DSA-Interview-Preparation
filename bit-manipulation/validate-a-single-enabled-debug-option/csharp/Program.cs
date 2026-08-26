/*
Title: Validate a Single Enabled Debug Option

Problem Description:
A monitoring tool stores debug settings for a service in one non-negative integer called `mask`.
Each bit in `mask` represents whether a specific debug option is enabled (`1`) or disabled (`0`).

For safety reasons, the service is considered valid only when exactly one debug option is enabled at a time.

Given an integer `mask`, return `true` if it contains exactly one set bit in its binary representation.
Otherwise, return `false`.

This is a bit manipulation problem. A direct loop over all bits works, but there is also a simple
constant-time trick using bitwise operators. The solution must correctly handle `0`, since a value
of `0` means no options are enabled and therefore is not valid.

Constraints:
- 0 <= mask <= 2^31 - 1
- The expected solution should use O(1) extra space.
- Any solution running in O(number of bits) or better is acceptable.

Example 1:
Input: mask = 8
Output: true
Explanation: 8 in binary is 1000, which has exactly one set bit.

Example 2:
Input: mask = 10
Output: false
Explanation: 10 in binary is 1010, which has two set bits, so more than one debug option is enabled.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(1)
    Space Complexity: O(1)

    We use a classic bit manipulation trick:
    - A positive number that has exactly one set bit is a power of two.
    - For any such number n, the expression (n & (n - 1)) becomes 0.

    Why does that work?
    - Example: n = 8
      Binary:      1000
      n - 1 = 7 -> 0111
      AND result:  0000

    If a number has more than one set bit, this expression will not be 0.
    - Example: n = 10
      Binary:      1010
      n - 1 = 9 -> 1001
      AND result:  1000  (not zero)

    Important special case:
    - n = 0 must return false, because zero has no set bits.
    */
    public bool HasExactlyOneSetBit(int mask)
    {
        // Step 1:
        // First, we must reject 0 immediately.
        //
        // Why is this necessary?
        // - The rule says "exactly one enabled debug option".
        // - A value of 0 means no bits are set at all.
        // - So 0 is definitely invalid.
        //
        // This check also protects the bit trick from incorrectly treating 0 as valid,
        // because:
        //   0 - 1 = -1
        //   0 & -1 = 0
        // Without this explicit check, we might accidentally return true for 0.
        if (mask == 0)
        {
            return false;
        }

        // Step 2:
        // Apply the bit manipulation trick: (mask & (mask - 1)) == 0
        //
        // What is this step doing?
        // - It removes the lowest set bit from the number.
        // - If the number had exactly one set bit, removing that bit leaves 0.
        // - If the number had two or more set bits, at least one set bit remains.
        //
        // Why is this necessary?
        // - It gives us a very efficient way to test whether the number is a power of two,
        //   which is exactly the same as "has exactly one set bit" for positive integers.
        //
        // Data structure choice:
        // - No extra data structure is needed here.
        // - We only use integer variables and bitwise operations.
        // - This keeps space usage constant: O(1).
        //
        // Example trace for mask = 8:
        //   mask      = 1000
        //   mask - 1  = 0111
        //   AND       = 0000
        //   result    = true
        //
        // Example trace for mask = 10:
        //   mask      = 1010
        //   mask - 1  = 1001
        //   AND       = 1000
        //   result    = false
        return (mask & (mask - 1)) == 0;
    }
}

// Demo code:
// We create sample inputs, call the solution method, and print the results.

var solution = new Solution();

// Example 1 from the problem description:
// mask = 8
// Binary: 1000
// Exactly one set bit -> expected true
int mask1 = 8;
bool result1 = solution.HasExactlyOneSetBit(mask1);
Console.WriteLine($"mask = {mask1}, result = {result1}");

// Example 2 from the problem description:
// mask = 10
// Binary: 1010
// Two set bits -> expected false
int mask2 = 10;
bool result2 = solution.HasExactlyOneSetBit(mask2);
Console.WriteLine($"mask = {mask2}, result = {result2}");

// Additional check for the important edge case:
// mask = 0
// Binary: 0
// No set bits -> expected false
int mask3 = 0;
bool result3 = solution.HasExactlyOneSetBit(mask3);
Console.WriteLine($"mask = {mask3}, result = {result3}");

// A few more beginner-friendly demonstrations:
int mask4 = 1;   // Binary: 1 -> exactly one set bit
int mask5 = 16;  // Binary: 10000 -> exactly one set bit
int mask6 = 18;  // Binary: 10010 -> two set bits

Console.WriteLine($"mask = {mask4}, result = {solution.HasExactlyOneSetBit(mask4)}");
Console.WriteLine($"mask = {mask5}, result = {solution.HasExactlyOneSetBit(mask5)}");
Console.WriteLine($"mask = {mask6}, result = {solution.HasExactlyOneSetBit(mask6)}");