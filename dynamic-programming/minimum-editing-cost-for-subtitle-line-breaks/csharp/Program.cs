/*
Title: Minimum Editing Cost for Subtitle Line Breaks

Problem Description:
A video platform stores a spoken transcript as an array of words. To generate subtitles,
the transcript must be split into lines while preserving the original word order.

You may place a line break between any two adjacent words, but each subtitle line must
contain at most maxWidth characters, including single spaces between consecutive words
on the same line.

The cost of a line is defined as the square of its unused space:
    (maxWidth - usedCharacters)^2

However, the final line is displayed without padding penalties, so its cost is 0
as long as it fits within maxWidth.

Your task is to return the minimum total editing cost needed to split the transcript
into valid subtitle lines.

Formally, if words i...j are placed on one line, then the line length is:
    sum(length(words[k])) for k in [i..j] + (j - i) spaces

This line is valid only if that total is less than or equal to maxWidth.

Return the minimum possible sum of line costs over all lines.

Constraints:
- 1 <= words.length <= 500
- 1 <= words[i].length <= maxWidth <= 1000
- words[i] contains only English letters, digits, or punctuation without spaces.

Important note about the examples:
The written explanations in the prompt contain arithmetic inconsistencies.
For example:
- "AI helps" actually uses 8 characters, but "teams ship" uses 10 characters,
  making the shown total inconsistent with the stated output.
- "code review" uses 11 characters and "needs clear" also uses 11, so the shown
  arrangement totals 2, not 17.

This solution follows the formal problem statement exactly:
- Every non-final line pays (unused space)^2
- The final line pays 0 if it fits
- We compute the true minimum according to those rules
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^2)
    Space Complexity: O(n)

    Why:
    - There are n starting positions.
    - For each starting position i, we try extending the line to j = i, i+1, ...
      until the width limit is exceeded.
    - In the worst case, that creates O(n^2) total checks.
    - The DP array stores one value per index, so O(n) extra space.
    */
    public int MinimumEditingCost(string[] words, int maxWidth)
    {
        int n = words.Length;

        // dp[i] will store the minimum total cost needed to format the suffix
        // starting from word index i.
        //
        // In other words:
        // - We still need to place words[i], words[i+1], ..., words[n-1]
        // - dp[i] is the best possible cost for doing that
        //
        // This is a classic dynamic programming state:
        // "What is the best answer from position i onward?"
        int[] dp = new int[n + 1];

        // Base case:
        // If i == n, there are no words left to place.
        // That means no more lines are needed, so the cost is 0.
        dp[n] = 0;

        // We fill the DP table from right to left.
        //
        // Why right to left?
        // Because dp[i] depends on dp[j + 1]:
        // if we place words i..j on the current line,
        // then the remaining cost is whatever it takes to format from j+1 onward.
        //
        // Since dp[i] needs future positions, we compute those future positions first.
        for (int i = n - 1; i >= 0; i--)
        {
            // Start with a very large number so any valid arrangement will be smaller.
            int best = int.MaxValue;

            // currentLineLength will track the total characters used if we place
            // words[i..j] on the same line.
            //
            // We build this incrementally instead of recomputing from scratch each time.
            int currentLineLength = 0;

            // Try every possible ending word j for the current line, starting at i.
            for (int j = i; j < n; j++)
            {
                // Add the next word to the line.
                //
                // If j == i, this is the first word on the line, so no leading space.
                // If j > i, we need exactly one space before this word.
                if (j == i)
                {
                    currentLineLength = words[j].Length;
                }
                else
                {
                    currentLineLength += 1 + words[j].Length;
                }

                // If the line is too long, then any larger j will also be too long,
                // because adding more words only increases the length.
                //
                // So we can stop early here.
                if (currentLineLength > maxWidth)
                {
                    break;
                }

                int currentCost;

                // If j is the last word in the entire transcript,
                // then this line is the final line.
                //
                // The problem explicitly says the final line has cost 0
                // as long as it fits within maxWidth.
                if (j == n - 1)
                {
                    currentCost = 0;
                }
                else
                {
                    // Otherwise, this is a non-final line.
                    // Its penalty is the square of the unused space.
                    int unused = maxWidth - currentLineLength;
                    currentCost = unused * unused;
                }

                // The total cost for choosing words i..j on this line is:
                //   cost of this line + best cost for the remaining suffix
                //
                // The remaining suffix starts at j + 1.
                int totalCost = currentCost + dp[j + 1];

                // Keep the minimum over all valid choices of j.
                if (totalCost < best)
                {
                    best = totalCost;
                }
            }

            // Store the best answer for suffix starting at i.
            dp[i] = best;
        }

        // The full problem starts at word 0, so dp[0] is the final answer.
        return dp[0];
    }
}

// Demo code
var solution = new Solution();

// Example 1 from the prompt
string[] words1 = ["AI", "helps", "teams", "ship", "faster"];
int maxWidth1 = 10;
int result1 = solution.MinimumEditingCost(words1, maxWidth1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2 from the prompt
string[] words2 = ["code", "review", "needs", "clear", "notes"];
int maxWidth2 = 12;
int result2 = solution.MinimumEditingCost(words2, maxWidth2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional small sanity check
string[] words3 = ["a", "bb", "ccc"];
int maxWidth3 = 6;
int result3 = solution.MinimumEditingCost(words3, maxWidth3);
Console.WriteLine("Example 3 Result: " + result3);