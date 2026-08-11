/*
Title: Check if a Status Code Is a Power-of-Two Flag

Problem Description:
In a monitoring system, each valid standalone status flag is encoded as a positive integer
with exactly one bit set in its binary representation. For example, 1 (binary 1), 2 (binary 10),
4 (binary 100), and 8 (binary 1000) are valid standalone flags. A number like 10 (binary 1010)
is not, because it has more than one set bit. Given an integer code, determine whether it
represents a valid standalone status flag.

Return true if the code is a positive power of two, and false otherwise.

A simple and efficient bit manipulation solution is expected. Try to solve it in O(1) time
using bitwise operators rather than loops over all bits.

Constraints:
- -2^31 <= code <= 2^31 - 1
- The input is a single integer.
- 0 and all negative numbers are not valid standalone flags.

Example 1:
Input: code = 16
Output: true
Explanation: 16 in binary is 10000, which contains exactly one set bit, so it is a valid standalone flag.

Example 2:
Input: code = 18
Output: false
Explanation: 18 in binary is 10010, which contains two set bits, so it is not a power of two.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(1)
    Space Complexity: O(1)

    We use a classic bit manipulation rule:
    - A positive power of two has exactly one bit set to 1.
    - For such a number n, the expression (n & (n - 1)) becomes 0.

    Why does this work?
    - Suppose n = 16, which is binary 10000.
    - Then n - 1 = 15, which is binary 01111.
    - Performing AND:
          10000
        & 01111
        = 00000
      So the result is 0.

    But for a number with more than one set bit, such as 18:
    - 18 is binary 10010
    - 17 is binary 10001
    - AND:
          10010
        & 10001
        = 10000
      This is not 0, so 18 is not a power of two.

    Important edge case:
    - 0 is NOT a power of two.
    - Negative numbers are also NOT valid, based on the problem statement.
    */
    public bool IsPowerOfTwoFlag(int code)
    {
        // Step 1:
        // First, we must reject all non-positive values.
        //
        // Why is this necessary?
        // - The problem clearly says only positive integers can be valid standalone flags.
        // - 0 has no set bits in the sense required here.
        // - Negative numbers are not considered valid flags for this problem.
        //
        // If code is 0 or less, we can immediately return false.
        if (code <= 0)
        {
            return false;
        }

        // Step 2:
        // Apply the key bit manipulation test:
        //     code & (code - 1)
        //
        // What does this do?
        // - Subtracting 1 from a positive integer flips:
        //   * the rightmost 1 bit to 0
        //   * all bits to the right of it to 1
        //
        // Why is this useful?
        // - If the original number had exactly one set bit,
        //   then removing that bit means no set bits remain in common.
        // - Therefore, the AND result becomes 0.
        //
        // Example: code = 16
        //   code     = 10000
        //   code - 1 = 01111
        //   AND      = 00000
        //
        // Example: code = 18
        //   code     = 10010
        //   code - 1 = 10001
        //   AND      = 10000
        //
        // So:
        // - result == 0  => exactly one set bit => power of two
        // - result != 0  => more than one set bit => not a power of two
        return (code & (code - 1)) == 0;
    }
}

// Demo code:
// We create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1 from the problem description:
// 16 in binary is 10000, which has exactly one set bit.
// Expected output: true
int code1 = 16;
bool result1 = solution.IsPowerOfTwoFlag(code1);
Console.WriteLine($"Input: code = {code1}");
Console.WriteLine($"Output: {result1}");
Console.WriteLine();

// Example 2 from the problem description:
// 18 in binary is 10010, which has two set bits.
// Expected output: false
int code2 = 18;
bool result2 = solution.IsPowerOfTwoFlag(code2);
Console.WriteLine($"Input: code = {code2}");
Console.WriteLine($"Output: {result2}");
Console.WriteLine();

// Additional beginner-friendly test cases:

// 1 is binary 1, which has exactly one set bit.
// Expected: true
int code3 = 1;
bool result3 = solution.IsPowerOfTwoFlag(code3);
Console.WriteLine($"Input: code = {code3}");
Console.WriteLine($"Output: {result3}");
Console.WriteLine();

// 0 is not positive, so it is not a valid standalone flag.
// Expected: false
int code4 = 0;
bool result4 = solution.IsPowerOfTwoFlag(code4);
Console.WriteLine($"Input: code = {code4}");
Console.WriteLine($"Output: {result4}");
Console.WriteLine();

// Negative numbers are not valid.
// Expected: false
int code5 = -8;
bool result5 = solution.IsPowerOfTwoFlag(code5);
Console.WriteLine($"Input: code = {code5}");
Console.WriteLine($"Output: {result5}");
Console.WriteLine();

// 64 is binary 1000000, exactly one set bit.
// Expected: true
int code6 = 64;
bool result6 = solution.IsPowerOfTwoFlag(code6);
Console.WriteLine($"Input: code = {code6}");
Console.WriteLine($"Output: {result6}");
Console.WriteLine();

// 10 is binary 1010, which has two set bits.
// Expected: false
int code7 = 10;
bool result7 = solution.IsPowerOfTwoFlag(code7);
Console.WriteLine($"Input: code = {code7}");
Console.WriteLine($"Output: {result7}");