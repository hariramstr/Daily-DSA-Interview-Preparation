/*
Title: Minimum Edits to Form Alternating Difficulty Chapters

Problem Description:
You are given a string `chapters` of length `n` representing the draft order of book chapters.
Each character is either 'E' (easy) or 'H' (hard).

The publisher wants the final book to be split into exactly `k` non-empty contiguous chapter groups.

After grouping:
1. Each group must be uniform:
   - every chapter in that group must have the same difficulty label
2. Adjacent groups must alternate:
   - if one group is all 'E', the next must be all 'H', then 'E', and so on

You may edit any chapter by changing 'E' to 'H' or 'H' to 'E'.
Each such change costs 1.

Return the minimum total number of edits needed to transform the draft into exactly `k`
alternating uniform groups. If it is impossible, return -1.

Constraints:
- 1 <= n <= 2000
- 1 <= k <= n
- chapters[i] is either 'E' or 'H'
- Groups must be contiguous and non-empty
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(k * n^2)
    Space Complexity: O(n)

    Beginner-friendly idea:
    -----------------------
    We want exactly k contiguous non-empty groups.
    Every group must be all one letter, and neighboring groups must alternate.

    A very useful observation:
    If we know:
    - how many groups we have already formed
    - where the current prefix ends
    - what letter the last group must be

    then we can use dynamic programming.

    We also need a fast way to know:
    "How many edits are needed to turn substring chapters[l..r] into all 'E'?"
    and similarly for all 'H'.

    We precompute prefix counts so each substring cost can be answered in O(1).
    */
    public int MinEdits(string chapters, int k)
    {
        int n = chapters.Length;

        // If we need more groups than characters, it is impossible because every group must be non-empty.
        if (k > n)
        {
            return -1;
        }

        // ------------------------------------------------------------
        // STEP 1: Build prefix counts
        // ------------------------------------------------------------
        // prefixE[i] = number of 'E' characters in chapters[0..i-1]
        // prefixH[i] = number of 'H' characters in chapters[0..i-1]
        //
        // Why do this?
        // Because later we will repeatedly ask:
        // "In substring [l..r], how many characters are NOT 'E'?"
        // That number is exactly the cost to make the whole substring all 'E'.
        //
        // Similarly:
        // "In substring [l..r], how many characters are NOT 'H'?"
        // That is the cost to make the whole substring all 'H'.
        //
        // With prefix sums, each such query becomes O(1).
        int[] prefixE = new int[n + 1];
        int[] prefixH = new int[n + 1];

        for (int i = 0; i < n; i++)
        {
            prefixE[i + 1] = prefixE[i] + (chapters[i] == 'E' ? 1 : 0);
            prefixH[i + 1] = prefixH[i] + (chapters[i] == 'H' ? 1 : 0);
        }

        // Local helper:
        // cost to convert substring chapters[l..r] into all target characters.
        int CostToMakeUniform(int l, int r, char target)
        {
            int length = r - l + 1;

            if (target == 'E')
            {
                int countE = prefixE[r + 1] - prefixE[l];
                return length - countE; // all non-'E' chars must be edited
            }
            else
            {
                int countH = prefixH[r + 1] - prefixH[l];
                return length - countH; // all non-'H' chars must be edited
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Dynamic Programming definition
        // ------------------------------------------------------------
        // We use two DP arrays for the current number of groups:
        //
        // dpE[i] = minimum edits needed to split chapters[0..i] into exactly g groups,
        //          where the g-th (last) group is all 'E'
        //
        // dpH[i] = same idea, but the last group is all 'H'
        //
        // We only keep the previous group count and current group count,
        // so memory stays O(n).
        //
        // Important:
        // Because groups must alternate, if the current last group is 'E',
        // then the previous group's last label must be 'H', and vice versa.

        const int INF = 1_000_000_000;

        int[] prevE = new int[n];
        int[] prevH = new int[n];
        Array.Fill(prevE, INF);
        Array.Fill(prevH, INF);

        // ------------------------------------------------------------
        // STEP 3: Base case for exactly 1 group
        // ------------------------------------------------------------
        // If we use exactly 1 group to cover chapters[0..i],
        // then the whole prefix must become uniform.
        //
        // So:
        // prevE[i] = cost to make chapters[0..i] all 'E'
        // prevH[i] = cost to make chapters[0..i] all 'H'
        for (int i = 0; i < n; i++)
        {
            prevE[i] = CostToMakeUniform(0, i, 'E');
            prevH[i] = CostToMakeUniform(0, i, 'H');
        }

        // ------------------------------------------------------------
        // STEP 4: Build answers for group counts 2..k
        // ------------------------------------------------------------
        // For each desired number of groups g:
        //   We compute currE and currH.
        //
        // Transition:
        // Suppose the last group starts at position t and ends at i.
        // Then the previous part is chapters[0..t-1], which must already be split
        // into exactly g-1 groups.
        //
        // If current last group is 'E':
        //   previous last group must be 'H'
        //   cost = prevH[t-1] + cost to make chapters[t..i] all 'E'
        //
        // If current last group is 'H':
        //   previous last group must be 'E'
        //   cost = prevE[t-1] + cost to make chapters[t..i] all 'H'
        //
        // We try every valid t.
        for (int g = 2; g <= k; g++)
        {
            int[] currE = new int[n];
            int[] currH = new int[n];
            Array.Fill(currE, INF);
            Array.Fill(currH, INF);

            // To split the first i+1 characters into exactly g non-empty groups,
            // we need at least g characters, so i must be at least g-1.
            for (int i = g - 1; i < n; i++)
            {
                // The last group starts at t.
                // Since we need g-1 groups before t, the prefix length t must be at least g-1.
                // Therefore t ranges from g-1 to i.
                for (int t = g - 1; t <= i; t++)
                {
                    // Previous prefix is [0..t-1], which ends at index t-1.
                    // That prefix must already be split into exactly g-1 groups.

                    // Make last group [t..i] all 'E'
                    if (prevH[t - 1] != INF)
                    {
                        int candidate = prevH[t - 1] + CostToMakeUniform(t, i, 'E');
                        if (candidate < currE[i])
                        {
                            currE[i] = candidate;
                        }
                    }

                    // Make last group [t..i] all 'H'
                    if (prevE[t - 1] != INF)
                    {
                        int candidate = prevE[t - 1] + CostToMakeUniform(t, i, 'H');
                        if (candidate < currH[i])
                        {
                            currH[i] = candidate;
                        }
                    }
                }
            }

            prevE = currE;
            prevH = currH;
        }

        // ------------------------------------------------------------
        // STEP 5: Final answer
        // ------------------------------------------------------------
        // We need exactly k groups covering the entire string, so we look at index n-1.
        // The last group may end with either 'E' or 'H', so we take the minimum.
        int answer = Math.Min(prevE[n - 1], prevH[n - 1]);

        return answer >= INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

string chapters1 = "EEHHE";
int k1 = 3;
int result1 = solution.MinEdits(chapters1, k1);
Console.WriteLine($"chapters = \"{chapters1}\", k = {k1} => {result1}");

string chapters2 = "EHEEEH";
int k2 = 4;
int result2 = solution.MinEdits(chapters2, k2);
Console.WriteLine($"chapters = \"{chapters2}\", k = {k2} => {result2}");

string chapters3 = "EEE";
int k3 = 2;
int result3 = solution.MinEdits(chapters3, k3);
Console.WriteLine($"chapters = \"{chapters3}\", k = {k3} => {result3}");

string chapters4 = "H";
int k4 = 1;
int result4 = solution.MinEdits(chapters4, k4);
Console.WriteLine($"chapters = \"{chapters4}\", k = {k4} => {result4}");

string chapters5 = "EHHHEE";
int k5 = 3;
int result5 = solution.MinEdits(chapters5, k5);
Console.WriteLine($"chapters = \"{chapters5}\", k = {k5} => {result5}");