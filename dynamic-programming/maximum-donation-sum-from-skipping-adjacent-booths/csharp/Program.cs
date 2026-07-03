/*
Title: Maximum Donation Sum from Skipping Adjacent Booths
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are organizing a charity fair with a row of donation booths. Each booth has a non-negative integer amount representing how much money can be collected from that booth. However, due to staffing limits, you cannot operate two adjacent booths on the same day. If you open booth i, then booths i - 1 and i + 1 must remain closed.

Your task is to return the maximum total donation amount that can be collected from the row of booths while following this rule.

This is a classic one-dimensional decision problem: for each booth, you can either skip it or open it, but opening it prevents using the previous booth. An efficient solution should use dynamic programming to build the best answer from smaller prefixes of the array.

Constraints:
- 1 <= booths.length <= 100
- 0 <= booths[i] <= 1000

Input format:
- An integer array `booths` where `booths[i]` is the donation amount available at booth `i`.

Output format:
- Return a single integer: the maximum donation sum obtainable without choosing adjacent booths.

Example 1:
Input: booths = [5, 1, 2, 10, 6]
Output: 15
Explanation: The best valid choice is 5 + 10 = 15. Choosing 5 + 2 + 6 gives 13, which is smaller.

Example 2:
Input: booths = [4, 7, 3, 9]
Output: 16
Explanation: The best choice is booths with amounts 7 and 9. They are not adjacent, so the total is 16.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each booth exactly once, so the running time grows linearly with the number of booths.

    Space Complexity: O(1)
    - We do not need a full DP array here.
    - We only keep track of the best answers for the previous two positions.
    */
    public int MaxDonation(int[] booths)
    {
        // This check makes the method safer and easier to understand for beginners.
        // The problem guarantees at least one element, but defensive programming is still a good habit.
        if (booths == null || booths.Length == 0)
        {
            return 0;
        }

        // If there is only one booth, there is no adjacency conflict at all.
        // So the best answer is simply to open that booth.
        if (booths.Length == 1)
        {
            return booths[0];
        }

        // We use dynamic programming, but in a space-optimized way.
        //
        // Core idea:
        // For each booth, we have exactly two choices:
        // 1. Skip the current booth
        // 2. Open the current booth
        //
        // If we skip the current booth:
        // - Then our total stays equal to the best answer up to the previous booth.
        //
        // If we open the current booth:
        // - We are NOT allowed to open the previous booth.
        // - So we add the current booth's donation to the best answer up to two booths back.
        //
        // Therefore, the recurrence is:
        // dp[i] = max(dp[i - 1], dp[i - 2] + booths[i])
        //
        // Instead of storing the entire dp array, we only store:
        // - prevTwo = dp[i - 2]
        // - prevOne = dp[i - 1]
        //
        // This works because each new state depends only on the previous two states.

        // For the first booth (index 0), the best we can do is open it.
        int prevTwo = booths[0];

        // For the first two booths (indices 0 and 1), we cannot open both because they are adjacent.
        // So the best answer is the larger of the two donation amounts.
        int prevOne = Math.Max(booths[0], booths[1]);

        // Now we process booths starting from index 2, because the first two cases are already handled.
        for (int i = 2; i < booths.Length; i++)
        {
            // Option 1: Skip the current booth.
            // Then the best total remains whatever was best up to the previous booth.
            int skipCurrent = prevOne;

            // Option 2: Open the current booth.
            // Because adjacent booths cannot both be opened, we must combine the current booth
            // with the best answer from two positions back.
            int takeCurrent = prevTwo + booths[i];

            // The best answer at this booth is whichever option gives a larger total.
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Move the sliding window forward:
            // - The old prevOne becomes the new prevTwo
            // - The newly computed currentBest becomes the new prevOne
            //
            // This update is necessary so that on the next iteration:
            // - prevTwo correctly represents dp[i - 1]
            // - prevOne correctly represents dp[i]
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all booths, prevOne holds the best possible answer for the full array.
        return prevOne;
    }
}

// Demo code
var solution = new Solution();

// Example 1:
// booths = [5, 1, 2, 10, 6]
// DP trace:
// index 0 -> 5
// index 1 -> max(5, 1) = 5
// index 2 -> max(5, 5 + 2 = 7) = 7
// index 3 -> max(7, 5 + 10 = 15) = 15
// index 4 -> max(15, 7 + 6 = 13) = 15
// Final answer = 15
int[] booths1 = { 5, 1, 2, 10, 6 };
int result1 = solution.MaxDonation(booths1);
Console.WriteLine($"Input: [{string.Join(", ", booths1)}]");
Console.WriteLine($"Maximum donation sum: {result1}");
Console.WriteLine("Expected: 15");
Console.WriteLine();

// Example 2:
// booths = [4, 7, 3, 9]
// DP trace:
// index 0 -> 4
// index 1 -> max(4, 7) = 7
// index 2 -> max(7, 4 + 3 = 7) = 7
// index 3 -> max(7, 7 + 9 = 16) = 16
// Final answer = 16
int[] booths2 = { 4, 7, 3, 9 };
int result2 = solution.MaxDonation(booths2);
Console.WriteLine($"Input: [{string.Join(", ", booths2)}]");
Console.WriteLine($"Maximum donation sum: {result2}");
Console.WriteLine("Expected: 16");
Console.WriteLine();

// Additional demo cases for learning and verification
int[] booths3 = { 8 };
int result3 = solution.MaxDonation(booths3);
Console.WriteLine($"Input: [{string.Join(", ", booths3)}]");
Console.WriteLine($"Maximum donation sum: {result3}");
Console.WriteLine("Expected: 8");
Console.WriteLine();

int[] booths4 = { 2, 1, 1, 2 };
int result4 = solution.MaxDonation(booths4);
Console.WriteLine($"Input: [{string.Join(", ", booths4)}]");
Console.WriteLine($"Maximum donation sum: {result4}");
Console.WriteLine("Expected: 4");