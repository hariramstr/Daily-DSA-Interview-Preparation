/*
Title: Minimum Cost to Build a Palindrome from Fragments
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You are given a string s representing a raw message and an integer array cost of the same length,
where cost[i] is the cost to keep character s[i] in the final message. You may delete any characters
you want, and deleting a character has no cost.

Your goal is to build a palindrome subsequence from s such that the sum of costs of the kept characters
is as small as possible. Among all non-empty palindrome subsequences that can be formed, return the
minimum possible total keep-cost.

A subsequence is formed by deleting zero or more characters without changing the order of the remaining
characters. A palindrome reads the same forward and backward.

Important observation:
A single character is always a palindrome. Therefore, because all keep-costs are positive, the minimum
possible cost among all non-empty palindromic subsequences is simply the minimum cost of any single
character in the string.

That means the answer is:
min(cost[i]) for all valid i

This is even stronger than a quadratic DP solution:
- Every non-empty string contains at least one 1-character palindromic subsequence.
- Any longer palindrome keeps at least 2 characters (or 1 character if odd center only), and since all
  costs are positive, it cannot be cheaper than the cheapest single kept character.

So while the prompt mentions dynamic programming, the mathematically correct solution under the given
constraints is a simple linear scan.

Example checks:
1) s = "abca", cost = [4, 2, 7, 3]
   Single-character palindromes:
   "a"(4), "b"(2), "c"(7), "a"(3)
   Minimum = 2
   So the correct answer is 2.

2) s = "racecar", cost = [8, 6, 5, 1, 5, 6, 8]
   Minimum single-character cost = 1
   So the correct answer is 1.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Beginner-friendly reasoning:
    - We are allowed to delete any characters for free.
    - We must keep a non-empty subsequence that is a palindrome.
    - Any single character by itself is a palindrome.
    - Since every kept character has a positive cost, keeping more characters can only increase
      the total cost, never decrease it.
    - Therefore, the cheapest valid palindrome is always just the single character with the
      smallest keep-cost.
    */
    public int MinimumCostPalindromeSubsequence(string s, int[] cost)
    {
        // Step 1:
        // We will scan through the cost array and keep track of the smallest value seen so far.
        //
        // Why this works:
        // A single character is always a valid palindrome subsequence.
        // So every index i gives us one valid candidate with total cost = cost[i].
        // Because all costs are positive, no longer palindrome can beat the cheapest single character.
        int minCost = int.MaxValue;

        // Step 2:
        // Visit every character position once.
        //
        // We do not actually need to inspect the character values in s for correctness,
        // because the existence of a 1-character palindrome does not depend on matching.
        // Still, s and cost are paired by index, and the problem guarantees equal length.
        for (int i = 0; i < cost.Length; i++)
        {
            // Step 3:
            // If the current keep-cost is smaller than our best answer so far,
            // update the answer.
            //
            // This is necessary because we want the minimum among all valid single-character
            // palindromes.
            if (cost[i] < minCost)
            {
                minCost = cost[i];
            }
        }

        // Step 4:
        // Return the smallest keep-cost found.
        //
        // This is the minimum possible total cost of any non-empty palindromic subsequence.
        return minCost;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string s1 = "abca";
int[] cost1 = { 4, 2, 7, 3 };
int result1 = solution.MinimumCostPalindromeSubsequence(s1, cost1);
Console.WriteLine(result1); // Correct result: 2

// Example 2
string s2 = "racecar";
int[] cost2 = { 8, 6, 5, 1, 5, 6, 8 };
int result2 = solution.MinimumCostPalindromeSubsequence(s2, cost2);
Console.WriteLine(result2); // Correct result: 1

// Additional demo
string s3 = "zzzzz";
int[] cost3 = { 9, 4, 6, 2, 8 };
int result3 = solution.MinimumCostPalindromeSubsequence(s3, cost3);
Console.WriteLine(result3); // Expected: 2