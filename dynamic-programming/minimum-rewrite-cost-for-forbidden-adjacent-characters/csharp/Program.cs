/*
Title: Minimum Rewrite Cost for Forbidden Adjacent Characters

Problem Description:
You are given a string s of length n consisting of lowercase English letters. Some pairs of letters are incompatible and are not allowed to appear next to each other in the final string. You are also given a non-negative cost matrix changeCost of size 26 x 26, where changeCost[a][b] is the cost to rewrite character a into character b. Rewriting a character does not affect any other position, and every position must end as exactly one lowercase letter.

Your task is to compute the minimum total rewrite cost needed to transform s into a new string t such that for every adjacent pair t[i - 1], t[i], that ordered pair is allowed. If it is impossible to produce any valid final string, return -1.

The incompatibility rules are provided as a list of forbidden ordered pairs. If (x, y) is forbidden, then x cannot be immediately followed by y. Note that (x, y) and (y, x) are different constraints.

This is a global optimization problem: choosing the cheapest letter for one position may force expensive choices later, so greedy methods do not work in general.

Constraints:
- 1 <= n <= 100000
- s consists only of lowercase English letters
- 0 <= changeCost[i][j] <= 10^9
- 0 <= number of forbidden pairs <= 26 * 26
- The answer may exceed 32-bit integer range
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the allowed/forbidden table: O(26 * 26 + F), where F is the number of forbidden pairs
    - Dynamic programming over the string: O(n * 26 * 26)
      For each position, for each possible current letter, we may inspect all 26 previous letters.
    - Since 26 is a fixed constant, this is effectively linear in n for practical purposes.

    Space Complexity:
    - O(26 * 26) for the adjacency rules
    - O(26) for the rolling DP arrays
    - Total auxiliary space is O(1) with respect to n, because alphabet size is fixed at 26
    */
    public long MinimumRewriteCost(string s, List<(char from, char to)> forbiddenPairs, long[][] changeCost)
    {
        int n = s.Length;

        // We will represent whether an ordered pair (a, b) is allowed.
        // allowed[x, y] == true means letter x can be immediately followed by letter y.
        //
        // We start by assuming every ordered pair is allowed, then mark forbidden ones as false.
        // This is convenient because the input gives us forbidden pairs, not allowed pairs.
        bool[,] allowed = new bool[26, 26];
        for (int i = 0; i < 26; i++)
        {
            for (int j = 0; j < 26; j++)
            {
                allowed[i, j] = true;
            }
        }

        // Apply the forbidden rules.
        // Because the problem states ordered pairs, (x, y) and (y, x) are independent.
        foreach (var pair in forbiddenPairs)
        {
            int a = pair.from - 'a';
            int b = pair.to - 'a';
            allowed[a, b] = false;
        }

        // We use a very large value to mean "currently impossible".
        // We choose a value safely below long.MaxValue so additions do not overflow.
        const long INF = long.MaxValue / 4;

        // prev[c] will store:
        // the minimum total cost to rewrite the prefix processed so far,
        // with the LAST chosen letter being c.
        //
        // curr[c] will store the same idea for the current position.
        long[] prev = new long[26];
        long[] curr = new long[26];

        // Base case: position 0.
        // There is no previous character yet, so any final letter is allowed here.
        // The only cost is rewriting s[0] into that chosen letter.
        int firstOriginal = s[0] - 'a';
        for (int c = 0; c < 26; c++)
        {
            prev[c] = changeCost[firstOriginal][c];
        }

        // Process positions 1 through n - 1.
        for (int i = 1; i < n; i++)
        {
            int original = s[i] - 'a';

            // Before computing the current row of DP, initialize all states as impossible.
            for (int c = 0; c < 26; c++)
            {
                curr[c] = INF;
            }

            // We now decide the final letter at position i.
            // Let currentLetter be the chosen final letter for this position.
            for (int currentLetter = 0; currentLetter < 26; currentLetter++)
            {
                // Cost to rewrite the original character s[i] into currentLetter.
                long rewriteCost = changeCost[original][currentLetter];

                // To end at currentLetter, the previous position could have been any letter prevLetter
                // such that the ordered pair (prevLetter, currentLetter) is allowed.
                //
                // So we try all 26 possibilities and keep the minimum.
                long best = INF;

                for (int prevLetter = 0; prevLetter < 26; prevLetter++)
                {
                    // If the previous state was impossible, skip it.
                    if (prev[prevLetter] == INF)
                    {
                        continue;
                    }

                    // If this adjacent pair is forbidden, we cannot transition this way.
                    if (!allowed[prevLetter, currentLetter])
                    {
                        continue;
                    }

                    // Candidate total cost:
                    // cost to build a valid prefix ending with prevLetter
                    // + cost to rewrite current position into currentLetter
                    long candidate = prev[prevLetter] + rewriteCost;

                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                curr[currentLetter] = best;
            }

            // Move current row into prev for the next iteration.
            // We swap references instead of copying values to keep the code efficient.
            var temp = prev;
            prev = curr;
            curr = temp;
        }

        // After processing all positions, the answer is the minimum cost among all possible ending letters.
        long answer = INF;
        for (int c = 0; c < 26; c++)
        {
            if (prev[c] < answer)
            {
                answer = prev[c];
            }
        }

        // If every ending state is impossible, return -1.
        return answer == INF ? -1 : answer;
    }
}

// ---------------------------
// Demo code
// ---------------------------

var solution = new Solution();

// Example 1 setup:
// s = "abca"
// forbiddenPairs = [("a","b"), ("b","c")]
// changeCost = identity cost 0
// and additionally:
// a->c = 2, b->a = 3, b->d = 1, c->a = 4
// all other non-diagonal changes = 5
string s1 = "abca";
var forbidden1 = new List<(char from, char to)>
{
    ('a', 'b'),
    ('b', 'c')
};

long[][] changeCost1 = new long[26][];
for (int i = 0; i < 26; i++)
{
    changeCost1[i] = new long[26];
    for (int j = 0; j < 26; j++)
    {
        changeCost1[i][j] = i == j ? 0 : 5;
    }
}

changeCost1['a' - 'a']['c' - 'a'] = 2;
changeCost1['b' - 'a']['a' - 'a'] = 3;
changeCost1['b' - 'a']['d' - 'a'] = 1;
changeCost1['c' - 'a']['a' - 'a'] = 4;

// Under this exact matrix, one optimal valid result is "adca":
// a->a = 0, b->d = 1, c->c = 0, a->a = 0, total = 1
// Adjacent pairs are (a,d), (d,c), (c,a), all allowed.
// So the mathematically correct minimum for this concrete matrix is 1.
long result1 = solution.MinimumRewriteCost(s1, forbidden1, changeCost1);
Console.WriteLine(result1);

// Example 2 setup:
// s = "aaa"
// every ordered pair over all 26 letters is forbidden
string s2 = "aaa";
var forbidden2 = new List<(char from, char to)>();
for (char a = 'a'; a <= 'z'; a++)
{
    for (char b = 'a'; b <= 'z'; b++)
    {
        forbidden2.Add((a, b));
    }
}

// Any matrix works; use identity 0 and 1 for all non-diagonal changes.
long[][] changeCost2 = new long[26][];
for (int i = 0; i < 26; i++)
{
    changeCost2[i] = new long[26];
    for (int j = 0; j < 26; j++)
    {
        changeCost2[i][j] = i == j ? 0 : 1;
    }
}

long result2 = solution.MinimumRewriteCost(s2, forbidden2, changeCost2);
Console.WriteLine(result2);