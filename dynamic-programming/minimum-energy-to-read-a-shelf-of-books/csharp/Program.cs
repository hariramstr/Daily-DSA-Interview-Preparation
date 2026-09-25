/*
Title: Minimum Energy to Read a Shelf of Books

Problem Description:
You are given an array `energy` where `energy[i]` represents the energy cost required to read book `i` on a shelf.
You want to finish reading all books by reaching the last book.

You start before the first book, and on each move you may read either the next book or skip one book and read the
book after that. In other words, if you are currently at position `i`, your next position can be `i + 1` or `i + 2`.
When you read a book, you must pay its energy cost. Your goal is to minimize the total energy spent to reach the last book.

Return the minimum total energy needed to finish at the last book.

Why Dynamic Programming:
The minimum energy needed to reach book `i` depends on the minimum energy needed to reach book `i - 1` or `i - 2`.
That overlapping structure makes this a classic dynamic programming problem.

Examples:
1) energy = [4, 2, 7, 3]
   Best path: read book 1 (2), then book 3 (3)
   Total = 5

2) energy = [5, 1, 2, 10, 1]
   One best path: read book 1 (1), then book 2 (2), then book 4 (1)
   Total = 4
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each book exactly once.

    Space Complexity: O(1)
    - We only keep track of the minimum costs for the previous two positions,
      instead of storing a full DP array.

    Beginner-friendly idea:
    Let dp[i] mean:
    "the minimum total energy required to land on book i"

    Since you can only come to book i from:
    - book i - 1
    - book i - 2

    the transition is:
    dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])

    Base cases:
    - If there is only one book, you must read it.
    - For book 0, cost is simply energy[0].
    - For book 1, because you start before the shelf, you may begin directly at book 1,
      so the minimum cost to reach book 1 is just energy[1].
    */
    public int MinEnergy(int[] energy)
    {
        // Step 1:
        // Handle the smallest possible input.
        // If there is only one book, there is no choice:
        // you must read the last book, which is also the first book.
        if (energy.Length == 1)
        {
            return energy[0];
        }

        // Step 2:
        // Define the DP meaning using constant space.
        //
        // prev2 will represent the minimum energy to reach book i - 2
        // prev1 will represent the minimum energy to reach book i - 1
        //
        // At the beginning:
        // - Minimum cost to reach book 0 is energy[0]
        // - Minimum cost to reach book 1 is energy[1]
        //
        // Why is book 1 just energy[1] instead of energy[0] + energy[1]?
        // Because the problem says we start before index 0, and our first move
        // can be to book 0 or directly to book 1 if it exists.
        int prev2 = energy[0];
        int prev1 = energy[1];

        // Step 3:
        // Process every book from index 2 up to the last book.
        // For each book i, compute the cheapest way to land on it.
        for (int i = 2; i < energy.Length; i++)
        {
            // Step 3a:
            // To reach book i, there are only two legal previous positions:
            // - i - 1
            // - i - 2
            //
            // If we come from i - 1, total cost is:
            // prev1 + energy[i]
            //
            // If we come from i - 2, total cost is:
            // prev2 + energy[i]
            //
            // We choose the smaller one because we want the minimum total energy.
            int current = energy[i] + Math.Min(prev1, prev2);

            // Step 3b:
            // Shift our rolling DP window forward.
            //
            // Before moving on:
            // - prev2 should become the old prev1
            // - prev1 should become current
            //
            // This keeps the meaning consistent for the next iteration.
            prev2 = prev1;
            prev1 = current;
        }

        // Step 4:
        // After the loop ends, prev1 holds the minimum energy needed
        // to reach the last book.
        return prev1;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] energy1 = { 4, 2, 7, 3 };
int result1 = solution.MinEnergy(energy1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", energy1)}]");
Console.WriteLine($"Minimum energy: {result1}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

// Example 2
int[] energy2 = { 5, 1, 2, 10, 1 };
int result2 = solution.MinEnergy(energy2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", energy2)}]");
Console.WriteLine($"Minimum energy: {result2}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

// Additional small edge case: only one book
int[] energy3 = { 8 };
int result3 = solution.MinEnergy(energy3);
Console.WriteLine("Edge Case:");
Console.WriteLine($"Input: [{string.Join(", ", energy3)}]");
Console.WriteLine($"Minimum energy: {result3}");
Console.WriteLine("Expected: 8");