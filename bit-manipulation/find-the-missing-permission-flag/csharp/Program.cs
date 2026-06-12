/*
Title: Find the Missing Permission Flag
Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
In a software system, each permission is represented by a power of two: 1, 2, 4, 8, 16, and so on.
A complete permission bundle should contain every flag from 2^0 up to 2^(n-1) exactly once.
However, due to a deployment issue, one permission flag is missing from the bundle.

You are given an integer array `flags` of length `n - 1`. The array contains distinct values,
and each value is a power of two. The full bundle was supposed to contain all powers of two
from `1` to `2^(n-1)`, but exactly one of them is absent. Your task is to return the missing
permission flag.

You should solve this using bit manipulation. A linear-time solution with constant extra space is expected.

Constraints:
- 2 <= n <= 30
- flags.length == n - 1
- Each flags[i] is a power of two
- All values in flags are distinct
- Every flags[i] belongs to the set {1, 2, 4, ..., 2^(n-1)}
- Exactly one flag from the complete set is missing

Example 1:
Input: flags = [1, 2, 8, 16]
Output: 4
Explanation: The complete set for n = 5 is [1, 2, 4, 8, 16]. The missing flag is 4.

Example 2:
Input: flags = [2, 4, 8, 16, 32, 1]
Output: 64
Explanation: The complete set for n = 7 is [1, 2, 4, 8, 16, 32, 64]. Only 64 is missing.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Idea:
    We use XOR, which is a classic bit manipulation tool for "find the missing value" problems.

    Important XOR properties:
    1. a ^ a = 0
       If the same number appears twice in an XOR chain, it cancels out.
    2. a ^ 0 = a
       XOR with zero changes nothing.
    3. XOR is commutative and associative
       This means order does not matter:
       a ^ b ^ c == c ^ a ^ b

    Because the full bundle should contain:
    1, 2, 4, 8, ..., 2^(n-1)

    and the input contains all of them except one,
    if we XOR:
    - every expected flag
    - every actual flag in the array

    then every matching value cancels out, and only the missing flag remains.
    */
    public int FindMissingPermissionFlag(int[] flags)
    {
        // The input array has length n - 1.
        // Therefore, the full bundle size n is:
        int n = flags.Length + 1;

        // This variable will accumulate XOR results.
        // We start from 0 because:
        // x ^ 0 = x
        int xorResult = 0;

        // STEP 1:
        // XOR all expected permission flags from the complete bundle.
        //
        // The complete bundle should be:
        // 2^0, 2^1, 2^2, ..., 2^(n-1)
        // which is:
        // 1, 2, 4, 8, ..., (1 << (n - 1))
        //
        // Why do this?
        // We want xorResult to first contain the XOR of every value that SHOULD exist.
        //
        // Example 1:
        // n = 5
        // expected flags = 1, 2, 4, 8, 16
        //
        // Example 2:
        // n = 7
        // expected flags = 1, 2, 4, 8, 16, 32, 64
        for (int i = 0; i < n; i++)
        {
            // (1 << i) computes 2^i using bit shifting.
            //
            // Examples:
            // i = 0 -> 0001 -> 1
            // i = 1 -> 0010 -> 2
            // i = 2 -> 0100 -> 4
            // i = 3 -> 1000 -> 8
            //
            // This is a natural fit for this problem because every permission flag
            // is a power of two.
            int expectedFlag = 1 << i;

            // Add this expected flag into the XOR chain.
            xorResult ^= expectedFlag;
        }

        // STEP 2:
        // XOR all flags that are actually present in the input array.
        //
        // Why does this work?
        // Every flag that exists both in the expected set and in the input
        // will appear twice in the total XOR expression:
        //
        // expected ^ actual
        //
        // Since x ^ x = 0, those values cancel out.
        //
        // The only value that does NOT get canceled is the missing one,
        // because it appears in the expected set but not in the input.
        foreach (int flag in flags)
        {
            xorResult ^= flag;
        }

        // STEP 3:
        // After all cancellations, xorResult now holds exactly the missing flag.
        //
        // Let's verify with Example 1:
        // expected XOR = 1 ^ 2 ^ 4 ^ 8 ^ 16
        // actual XOR   = 1 ^ 2 ^ 8 ^ 16
        //
        // combined:
        // (1 ^ 2 ^ 4 ^ 8 ^ 16) ^ (1 ^ 2 ^ 8 ^ 16)
        // = (1 ^ 1) ^ (2 ^ 2) ^ 4 ^ (8 ^ 8) ^ (16 ^ 16)
        // = 0 ^ 0 ^ 4 ^ 0 ^ 0
        // = 4
        //
        // Example 2:
        // expected XOR = 1 ^ 2 ^ 4 ^ 8 ^ 16 ^ 32 ^ 64
        // actual XOR   = 2 ^ 4 ^ 8 ^ 16 ^ 32 ^ 1
        //
        // combined:
        // all matching values cancel, leaving 64
        return xorResult;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Create an instance of the solution class.
var solution = new Solution();

// Example 1:
// Full set should be [1, 2, 4, 8, 16]
// Given flags are [1, 2, 8, 16]
// Missing flag should be 4
int[] flags1 = { 1, 2, 8, 16 };
int result1 = solution.FindMissingPermissionFlag(flags1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2:
// Full set should be [1, 2, 4, 8, 16, 32, 64]
// Given flags are [2, 4, 8, 16, 32, 1]
// Missing flag should be 64
int[] flags2 = { 2, 4, 8, 16, 32, 1 };
int result2 = solution.FindMissingPermissionFlag(flags2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 64

// Additional demo:
// Full set for n = 4 is [1, 2, 4, 8]
// Given [1, 4, 8]
// Missing should be 2
int[] flags3 = { 1, 4, 8 };
int result3 = solution.FindMissingPermissionFlag(flags3);
Console.WriteLine("Example 3 Result: " + result3); // Expected: 2