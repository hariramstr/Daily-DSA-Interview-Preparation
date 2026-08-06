/*
Title: Maximum XOR Gap After One Removal
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
You are given an array of non-negative integers nums. Define the XOR gap of a set of numbers as the maximum value of a XOR b over all distinct pairs (a, b) in that set. Your task is to remove exactly one element from nums so that the XOR gap of the remaining elements is as large as possible. Return that maximum possible XOR gap.

In other words, for each possible index i, imagine deleting nums[i], then compute the maximum XOR of any two different remaining values. Among all choices of i, return the largest such result.

If after removing one element fewer than two numbers remain, the XOR gap is defined to be 0.

A brute-force solution that recomputes the best pair after every removal is too slow for large inputs. A strong solution should take advantage of binary representations and shared prefixes between numbers.

Constraints:
- 1 <= nums.length <= 10^5
- 0 <= nums[i] <= 10^9
- Values may repeat

Examples:
1) nums = [3, 10, 5, 25]
   Remove 10 -> remaining [3, 5, 25]
   Best pair is 5 XOR 25 = 28
   Answer = 28

2) nums = [8, 1, 2]
   Remove 1 -> remaining [8, 2]
   Best pair is 8 XOR 2 = 10
   Answer = 10
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private const int MaxBit = 30; // nums[i] <= 1e9, so bits 30..0 are enough

    private sealed class TrieNode
    {
        public TrieNode? Zero;
        public TrieNode? One;
        public int Count;
    }

    private readonly TrieNode _root = new();

    // Time Complexity:
    // - Building the trie: O(n * B), where B = 31 bits
    // - For each number, querying best XOR partner: O(B)
    // - Total: O(n * B), which is effectively O(n)
    //
    // Space Complexity:
    // - Trie storage: O(n * B) in the worst case
    //
    // Key idea:
    // We do NOT need to simulate every possible removal separately.
    // After removing one element, any pair of two OTHER elements still remains.
    // Therefore, if the original array contains at least 3 elements, we can always remove
    // some element that is not one of the best XOR pair, and keep that best pair intact.
    //
    // So:
    // - If n < 3, after removing one element fewer than two numbers remain => answer is 0
    // - Otherwise, the answer is simply the maximum XOR of any pair in the original array
    //
    // This reduces the problem to the classic "maximum XOR of two numbers in an array".
    public int MaximumXorGapAfterOneRemoval(int[] nums)
    {
        // If we start with fewer than 3 numbers:
        // - n = 1 -> after removing one, 0 numbers remain -> no pair -> 0
        // - n = 2 -> after removing one, 1 number remains -> no pair -> 0
        if (nums.Length < 3)
        {
            return 0;
        }

        // Step 1:
        // Insert every number into a binary trie.
        //
        // Why a trie?
        // XOR becomes large when corresponding bits differ, especially at higher bits.
        // A binary trie lets us greedily try to go to the opposite bit at each position,
        // which maximizes the XOR value from the most significant bit downward.
        //
        // Each path from root to leaf represents one number's 31-bit binary form.
        foreach (int num in nums)
        {
            Insert(num);
        }

        int answer = 0;

        // Step 2:
        // For each number, ask:
        // "What is the best possible XOR value I can get with some number in the trie?"
        //
        // Because all numbers are already inserted, the trie contains all candidates.
        // Matching with itself is harmless:
        // - If duplicates exist, it may match another equal value.
        // - If it matches itself, XOR is 0, which would not beat a better partner anyway.
        // Since we are looking for the global maximum over all numbers, this still correctly
        // finds the maximum XOR pair in the whole array.
        foreach (int num in nums)
        {
            int bestWithThisNum = QueryBestXor(num);
            if (bestWithThisNum > answer)
            {
                answer = bestWithThisNum;
            }
        }

        return answer;
    }

    private void Insert(int num)
    {
        TrieNode current = _root;
        current.Count++;

        // We process from the most significant relevant bit down to the least significant bit.
        // This ensures the trie structure aligns with binary prefixes.
        for (int bit = MaxBit; bit >= 0; bit--)
        {
            int currentBit = (num >> bit) & 1;

            if (currentBit == 0)
            {
                current.Zero ??= new TrieNode();
                current = current.Zero;
            }
            else
            {
                current.One ??= new TrieNode();
                current = current.One;
            }

            current.Count++;
        }
    }

    private int QueryBestXor(int num)
    {
        TrieNode current = _root;
        int xorValue = 0;

        // We greedily build the maximum XOR bit by bit.
        //
        // At each bit position:
        // - If num has bit 0, we prefer to pair with bit 1
        // - If num has bit 1, we prefer to pair with bit 0
        //
        // Why greedy works:
        // Higher bits contribute more to the final numeric value than lower bits.
        // So if we can make the current higher bit of XOR equal to 1, that is always better
        // than sacrificing it for any combination of lower bits.
        for (int bit = MaxBit; bit >= 0; bit--)
        {
            int currentBit = (num >> bit) & 1;

            if (currentBit == 0)
            {
                // Preferred branch is 1 because 0 XOR 1 = 1
                if (current.One != null && current.One.Count > 0)
                {
                    xorValue |= (1 << bit);
                    current = current.One;
                }
                else
                {
                    current = current.Zero!;
                }
            }
            else
            {
                // Preferred branch is 0 because 1 XOR 0 = 1
                if (current.Zero != null && current.Zero.Count > 0)
                {
                    xorValue |= (1 << bit);
                    current = current.Zero;
                }
                else
                {
                    current = current.One!;
                }
            }
        }

        return xorValue;
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 3, 10, 5, 25 };
int result1 = solution.MaximumXorGapAfterOneRemoval(nums1);
Console.WriteLine(result1); // Expected: 28

var solution2 = new Solution();
int[] nums2 = { 8, 1, 2 };
int result2 = solution2.MaximumXorGapAfterOneRemoval(nums2);
Console.WriteLine(result2); // Expected: 10

var solution3 = new Solution();
int[] nums3 = { 1, 2 };
int result3 = solution3.MaximumXorGapAfterOneRemoval(nums3);
Console.WriteLine(result3); // Expected: 0

var solution4 = new Solution();
int[] nums4 = { 7, 7, 7 };
int result4 = solution4.MaximumXorGapAfterOneRemoval(nums4);
Console.WriteLine(result4); // Expected: 0