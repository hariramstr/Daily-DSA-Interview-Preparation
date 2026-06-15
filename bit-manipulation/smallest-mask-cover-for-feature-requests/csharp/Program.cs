/*
Title: Smallest Mask Cover for Feature Requests
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
A product team tracks each customer request as a non-negative integer bitmask. In a mask, the i-th bit is 1 if the request needs feature i, and 0 otherwise. Given an array requests, you must choose exactly one request mask x from the array and measure how many extra feature bits need to be turned on so that x can cover every request in the array.

A mask y covers a request r if every bit set in r is also set in y. In other words, r is covered when (r & y) == r. Starting from a chosen x, you are allowed to turn on additional bits but never turn bits off. The cost of choosing x is the minimum number of bit positions you must turn on so that the resulting upgraded mask covers all requests.

Return the minimum possible cost over all choices of x.

Equivalently, if OR is the bitwise OR of all values in requests, then for a chosen x the required cost is the number of 1-bits in (OR ^ x), assuming x is already a subset of OR, which is always true because x comes from the array.

Constraints:
- 1 <= requests.length <= 100000
- 0 <= requests[i] <= 10^9
- requests contains at least one value

Example 1:
Input: requests = [5, 1, 7]
Output: 0
Explanation: The bitwise OR of all requests is 7 (111 in binary). Since 7 already appears in the array, choosing x = 7 requires turning on 0 extra bits.

Example 2:
Input: requests = [10, 12, 8]
Output: 1
Explanation: The OR of all requests is 14 (1110 in binary). Choosing x = 12 (1100) requires turning on only bit 1 to reach 14, so the cost is 1. Choosing 10 or 8 would require turning on 1 or 2 extra bits respectively, and the minimum is 1.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why O(n)?
    1. We make one full pass to compute the bitwise OR of all request masks.
    2. We make another full pass to evaluate the cost for each candidate mask.
    3. Counting set bits for a 32-bit integer is effectively constant time.

    Why O(1) extra space?
    We only store a few integer variables regardless of input size.
    */
    public int MinimumExtraBits(int[] requests)
    {
        // Step 1: Compute the global OR of all request masks.
        //
        // What this does:
        // - Builds a mask containing every feature bit that appears in at least one request.
        //
        // Why this is necessary:
        // - To cover every request, the final upgraded mask must contain every bit that appears
        //   anywhere in the array.
        // - The smallest mask that can cover all requests is exactly the bitwise OR of all requests.
        //
        // Example:
        // requests = [5, 1, 7]
        // 5 = 101
        // 1 = 001
        // 7 = 111
        // OR = 111 = 7
        //
        // So any chosen request must eventually be upgraded to at least 7.
        int globalOr = 0;
        foreach (int request in requests)
        {
            globalOr |= request;
        }

        // Step 2: Try each request mask as the starting choice x.
        //
        // What this does:
        // - For each candidate x from the array, determine how many bits are missing compared to globalOr.
        //
        // Why this works:
        // - Since x comes from the array, every 1-bit in x must also be present in globalOr.
        //   That means x is always a subset of globalOr in terms of set bits.
        // - Therefore, the bits we need to turn on are exactly the bits that are 1 in globalOr
        //   but 0 in x.
        //
        // How to compute missing bits:
        // - missing = globalOr ^ x
        //
        // Why XOR is valid here:
        // - Because x has no 1-bits outside globalOr.
        // - So XOR marks exactly the positions where globalOr has 1 and x has 0.
        //
        // Equivalent expression:
        // - missing = globalOr & ~x
        //
        // Then the cost is simply the number of 1-bits in missing.
        int bestCost = int.MaxValue;

        foreach (int request in requests)
        {
            // Step 2a: Find which required bits are absent from this request.
            //
            // Example:
            // globalOr = 14 = 1110
            // request  = 12 = 1100
            // missing  = 0010
            // cost = 1
            int missingBits = globalOr ^ request;

            // Step 2b: Count how many bits must be turned on.
            //
            // Why popcount / bit count:
            // - Each missing 1-bit corresponds to one feature bit we must enable.
            int cost = BitOperationsHelper.PopCount(missingBits);

            // Step 2c: Keep the minimum cost across all choices.
            //
            // Why this is necessary:
            // - The problem asks for the minimum possible cost over all request masks in the array.
            if (cost < bestCost)
            {
                bestCost = cost;
            }
        }

        // Step 3: Return the best answer found.
        return bestCost;
    }
}

public static class BitOperationsHelper
{
    public static int PopCount(int value)
    {
        // This method counts the number of 1-bits in a non-negative integer.
        //
        // We use Brian Kernighan's algorithm:
        // - Repeatedly remove the lowest set bit using: value &= (value - 1)
        // - Each iteration removes exactly one 1-bit
        // - The number of iterations equals the number of set bits
        //
        // Example:
        // value = 10 = 1010
        // iteration 1: 1010 -> 1000
        // iteration 2: 1000 -> 0000
        // count = 2
        int count = 0;
        while (value != 0)
        {
            value &= (value - 1);
            count++;
        }
        return count;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// requests = [5, 1, 7]
// global OR = 7
// Choosing 7 means no extra bits are needed.
// Expected output: 0
int[] requests1 = { 5, 1, 7 };
int result1 = solution.MinimumExtraBits(requests1);
Console.WriteLine(result1);

// Example 2:
// requests = [10, 12, 8]
// 10 = 1010
// 12 = 1100
//  8 = 1000
// OR = 1110 = 14
//
// Costs:
// choose 10 -> missing 0100 -> 1 bit
// choose 12 -> missing 0010 -> 1 bit
// choose  8 -> missing 0110 -> 2 bits
//
// Minimum = 1
// Expected output: 1
int[] requests2 = { 10, 12, 8 };
int result2 = solution.MinimumExtraBits(requests2);
Console.WriteLine(result2);

// Additional quick demo:
// requests = [0, 0, 0]
// OR = 0
// Any choice already covers all requests.
// Expected output: 0
int[] requests3 = { 0, 0, 0 };
int result3 = solution.MinimumExtraBits(requests3);
Console.WriteLine(result3);