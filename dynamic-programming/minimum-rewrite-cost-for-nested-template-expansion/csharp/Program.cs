/*
Title: Minimum Rewrite Cost for Nested Template Expansion
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
A documentation platform stores a final rendered page as a string target. The page was produced by expanding reusable templates.
You are given m templates, where template i is a non-empty string templates[i] with an associated rewrite cost cost[i].

Starting from an empty page, you may build target from left to right using the following operations:

1. Append any single lowercase letter at cost appendCost.
2. Insert one template templates[i] at the current end of the page at cost cost[i].
3. If the suffix currently built ends with a string equal to some previously used template, you may reuse that same template again
   immediately for an additional discounted chaining cost chainCost[i] instead of cost[i].

A template may be used any number of times. Chaining only applies when the most recent appended block was exactly templates[i]
and you place the same template again with no gap. Single-letter appends break the chain.

Return the minimum total cost to build exactly target, or -1 if it is impossible.

Constraints:
- 1 <= target.length <= 10^5
- 1 <= m <= 2 * 10^4
- 1 <= templates[i].length <= 50
- Sum of all template lengths does not exceed 2 * 10^5
- target and every template consist only of lowercase English letters
- 1 <= appendCost, cost[i], chainCost[i] <= 10^9

Important note about the examples:
The written explanations in the prompt contain arithmetic inconsistencies:
- Example 1 says Output = 7, but the explanation itself computes 9 and that is the true minimum.
- Example 2 says Output = 11, but the explanation itself computes 12 and that is the true minimum.

This implementation follows the actual problem rules exactly, so it returns:
- 9 for Example 1
- 12 for Example 2
*/

using System;
using System.Collections.Generic;

class Solution
{
    private const long INF = long.MaxValue / 4;

    private class Match
    {
        public int TemplateId;
        public int Length;
    }

    private class TrieNode
    {
        public int[] Next = new int[26];
        public List<int> TemplateIds = new List<int>();

        public TrieNode()
        {
            Array.Fill(Next, -1);
        }
    }

    /*
    Time Complexity:
    - Building the trie: O(sum of template lengths)
    - Finding all template matches in target:
      Since every template length is at most 50, from each target position we walk at most 50 trie edges.
      Therefore this step is O(target.Length * 50).
    - Dynamic programming:
      For each position, we process all templates that end there.
      Total number of such matches is also O(target.Length * 50) in the worst case.
    Overall: O(sumTemplateLengths + n * 50)

    Space Complexity:
    - Trie: O(sum of template lengths)
    - Match lists for each ending position: O(total matches)
    - DP arrays: O(n + m)
    Overall: O(sumTemplateLengths + totalMatches + n + m)
    */
    public long MinimumRewriteCost(string target, string[] templates, long[] cost, long[] chainCost, long appendCost)
    {
        int n = target.Length;
        int m = templates.Length;

        // ------------------------------------------------------------
        // Step 1: Build a trie of all templates.
        //
        // Why do we need this?
        // We want to know, for every position in target, which templates
        // end at that position. A trie lets us scan forward from each
        // starting position and discover all matching templates efficiently.
        //
        // Because every template length is at most 50, scanning from each
        // position is cheap: at most 50 characters.
        // ------------------------------------------------------------
        var trie = new List<TrieNode> { new TrieNode() };

        for (int i = 0; i < m; i++)
        {
            int node = 0;
            foreach (char ch in templates[i])
            {
                int c = ch - 'a';
                if (trie[node].Next[c] == -1)
                {
                    trie[node].Next[c] = trie.Count;
                    trie.Add(new TrieNode());
                }
                node = trie[node].Next[c];
            }
            trie[node].TemplateIds.Add(i);
        }

        // ------------------------------------------------------------
        // Step 2: Precompute all template matches that end at each target position.
        //
        // endMatches[pos] will contain every template that matches the substring
        // target[pos - len .. pos - 1].
        //
        // This is the key preprocessing that allows the DP to know which
        // template transitions are possible at each prefix length.
        // ------------------------------------------------------------
        var endMatches = new List<Match>[n + 1];
        for (int i = 0; i <= n; i++)
        {
            endMatches[i] = new List<Match>();
        }

        for (int start = 0; start < n; start++)
        {
            int node = 0;

            // We only need to walk up to 50 characters because template lengths are <= 50.
            for (int end = start; end < n && end < start + 50; end++)
            {
                int c = target[end] - 'a';
                int next = trie[node].Next[c];
                if (next == -1)
                {
                    break;
                }

                node = next;

                if (trie[node].TemplateIds.Count > 0)
                {
                    int len = end - start + 1;
                    foreach (int templateId in trie[node].TemplateIds)
                    {
                        endMatches[end + 1].Add(new Match
                        {
                            TemplateId = templateId,
                            Length = len
                        });
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // Step 3: Define DP state.
        //
        // We need to remember not only how many characters have been built,
        // but also whether the LAST block used was a specific template.
        //
        // Let:
        //   dpAny[pos] = minimum cost to build exactly target[0..pos-1],
        //                regardless of what the last operation was.
        //
        //   dpEndTemplate[pos][i] conceptually = minimum cost to build prefix pos
        //                where the most recent block used is exactly template i.
        //
        // Storing the full 2D table would be too large: n * m.
        //
        // Instead, we observe something very important:
        // A state "prefix ends with template i" can only exist at positions where
        // template i actually matches the suffix of target ending there.
        //
        // So we store only the best known value for each template at the CURRENT
        // ending position when we process matches there.
        //
        // To support chaining, we need to know:
        //   bestEndAtPosTemplate[i] = minimum cost for some processed position
        //                             where the built prefix ends exactly with template i.
        //
        // But chaining from template i to template i only matters when the next
        // position is exactly currentPos + len(i), and target matches again.
        //
        // A clean way to do this is:
        // For every position pos, when template i ends here, compute:
        //   normal use: dpAny[pos - len(i)] + cost[i]
        //   chained use: endState[pos - len(i), i] + chainCost[i]
        //
        // Therefore we need access to end-state values at earlier positions.
        //
        // We store them sparsely in dictionaries by ending position:
        //   endStateAtPos[pos] : Dictionary<templateId, bestCost>
        //
        // Since only matching templates can end at a position, this remains manageable.
        // ------------------------------------------------------------
        long[] dpAny = new long[n + 1];
        Array.Fill(dpAny, INF);
        dpAny[0] = 0;

        var endStateAtPos = new Dictionary<int, Dictionary<int, long>>();

        // ------------------------------------------------------------
        // Step 4: Process prefixes from left to right.
        //
        // For each prefix length pos:
        //   A) We can always append one character from pos-1 to pos.
        //      This breaks any chain, but dpAny already represents "best regardless
        //      of last block", so:
        //         dpAny[pos] = min(dpAny[pos], dpAny[pos-1] + appendCost)
        //
        //   B) For every template that ends at pos:
        //      Let len be its length and prev = pos - len.
        //
        //      We can place it normally:
        //         dpAny[prev] + cost[i]
        //
        //      Or chain it if the previous block was the same template i:
        //         endStateAtPos[prev][i] + chainCost[i]
        //
        //      The best of those becomes an end-state for (pos, i), and also
        //      contributes to dpAny[pos].
        // ------------------------------------------------------------
        for (int pos = 1; pos <= n; pos++)
        {
            // --------------------------------------------------------
            // Option 1: append one single character.
            //
            // This is always possible because the problem allows appending
            // any single lowercase letter, and we simply choose the needed
            // target character at this position.
            // --------------------------------------------------------
            if (dpAny[pos - 1] < INF)
            {
                dpAny[pos] = Math.Min(dpAny[pos], dpAny[pos - 1] + appendCost);
            }

            Dictionary<int, long>? currentEndStates = null;

            // --------------------------------------------------------
            // Option 2 and 3: use templates that end exactly at this position.
            // --------------------------------------------------------
            foreach (var match in endMatches[pos])
            {
                int templateId = match.TemplateId;
                int len = match.Length;
                int prev = pos - len;

                long best = INF;

                // ----------------------------------------------------
                // Normal insertion of this template.
                //
                // We can always start a fresh use of the template from any
                // completed prefix of length prev.
                // ----------------------------------------------------
                if (dpAny[prev] < INF)
                {
                    best = Math.Min(best, dpAny[prev] + cost[templateId]);
                }

                // ----------------------------------------------------
                // Chained insertion of the same template.
                //
                // This is only allowed if the immediately previous block
                // was exactly the same template.
                //
                // That means we need an end-state at position prev for
                // this same templateId.
                // ----------------------------------------------------
                if (endStateAtPos.TryGetValue(prev, out var prevStates) &&
                    prevStates.TryGetValue(templateId, out long prevTemplateCost))
                {
                    best = Math.Min(best, prevTemplateCost + chainCost[templateId]);
                }

                if (best >= INF)
                {
                    continue;
                }

                // ----------------------------------------------------
                // Record the best end-state for (pos, templateId).
                //
                // Multiple identical template strings could exist in input
                // with different costs. We treat each template index as a
                // separate chainable identity, exactly as the statement says:
                // chaining applies to "that same template again".
                // ----------------------------------------------------
                currentEndStates ??= new Dictionary<int, long>();

                if (!currentEndStates.TryGetValue(templateId, out long existing) || best < existing)
                {
                    currentEndStates[templateId] = best;
                }

                // ----------------------------------------------------
                // Any end-state is also a valid completed prefix state.
                // ----------------------------------------------------
                if (best < dpAny[pos])
                {
                    dpAny[pos] = best;
                }
            }

            if (currentEndStates != null)
            {
                endStateAtPos[pos] = currentEndStates;
            }
        }

        return dpAny[n] >= INF ? -1 : dpAny[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------
var solution = new Solution();

// Example 1 from the prompt.
// The prompt's stated output is inconsistent with its own explanation.
// Correct minimum under the stated rules is 9:
// "ab" for 5, then chain "ab" for 2, then chain "ab" for 2.
string target1 = "ababab";
string[] templates1 = { "ab", "aba" };
long[] cost1 = { 5, 8 };
long[] chainCost1 = { 2, 6 };
long appendCost1 = 4;
Console.WriteLine(solution.MinimumRewriteCost(target1, templates1, cost1, chainCost1, appendCost1)); // 9

// Example 2 from the prompt.
// Correct minimum under the stated rules is 12:
// "code" for 7, chain "code" for 3, append 'x' for 2.
string target2 = "codecodex";
string[] templates2 = { "code", "x" };
long[] cost2 = { 7, 10 };
long[] chainCost2 = { 3, 1 };
long appendCost2 = 2;
Console.WriteLine(solution.MinimumRewriteCost(target2, templates2, cost2, chainCost2, appendCost2)); // 12

// Additional small sanity check:
// target = "aaaaa"
// template "aa": first use 5, chain 1, append one 'a' for 2 => total 8
string target3 = "aaaaa";
string[] templates3 = { "aa" };
long[] cost3 = { 5 };
long[] chainCost3 = { 1 };
long appendCost3 = 2;
Console.WriteLine(solution.MinimumRewriteCost(target3, templates3, cost3, chainCost3, appendCost3)); // 8