/*
Title: Find Players With Reordered Card Histories
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given the game histories of several players in an online card platform. Each player history is represented by a list of card IDs in the exact order they were drawn during a match. Two players are considered to have a reordered-equivalent history if their histories contain the same multiset of card IDs, even if the draw order is different. For example, histories [4, 9, 4, 2] and [2, 4, 9, 4] are equivalent, but [4, 9, 2] is not equivalent to [4, 9, 4, 2] because the frequencies differ.

Your task is to return all player IDs that belong to at least one reordered-equivalent group. The result should be sorted in increasing order of player ID.

Player IDs are 0-indexed based on their position in the input array. Each history may have a different length. A player belongs to a reordered-equivalent group if there exists at least one other player with exactly the same card-frequency profile.

A good solution should avoid comparing every pair of histories directly. Think about how to build a canonical signature for each history and use hashing to group equivalent players efficiently.

Constraints:
- 1 <= histories.length <= 100000
- 0 <= histories[i].length <= 100000
- 0 <= card IDs <= 1000000000
- The sum of all history lengths across all players does not exceed 200000
- Return the player IDs in sorted ascending order

Example 1:
Input: histories = [[4,9,4,2],[2,4,9,4],[7,7,1],[1,7,7],[3,5]]
Output: [0,1,2,3]
Explanation: Players 0 and 1 have the same card counts. Players 2 and 3 also have the same card counts. Player 4 does not match anyone.

Example 2:
Input: histories = [[1,2,3],[3,2,1,1],[],[5,5],[]]
Output: [2,4]
Explanation: Player 2 and player 4 both have empty histories, so they form a reordered-equivalent group. The other players do not share the same card-frequency profile with any other player.
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    Let T be the total number of card entries across all players.
    Let U be the total number of distinct (player, card) frequency entries across all players.

    - Counting frequencies for every player's history takes O(T).
    - Sorting the distinct card IDs inside each player's frequency map takes
      O(sum over players of k_i log k_i), where k_i is the number of distinct cards in player i.
    - Building the canonical signature strings takes O(U).
    - Grouping and collecting answers takes O(number of players).

    Overall:
    O(T + sum(k_i log k_i))

    This is efficient because the total input size is bounded, and we avoid
    comparing every pair of players directly.

    Space Complexity:
    O(U + number of players)

    We store:
    - A frequency dictionary for one player at a time
    - A global dictionary from canonical signature to list of player IDs
    - The output list
    */
    public IList<int> FindPlayersWithReorderedHistories(int[][] histories)
    {
        // This dictionary is the heart of the grouping strategy.
        //
        // Key:
        //   A canonical signature string that uniquely represents the multiset
        //   of cards in one player's history.
        //
        // Value:
        //   A list of player IDs whose histories produce exactly that same signature.
        //
        // Why do we need this?
        // Instead of comparing every player with every other player, we convert each
        // history into a "normalized identity". If two histories have the same card
        // counts, they will generate the same signature and land in the same group.
        var groups = new Dictionary<string, List<int>>();

        // We process players in increasing ID order from 0 to histories.Length - 1.
        // This matters because later, when we collect answers from groups, the player IDs
        // inside each group will already be in ascending order.
        for (int playerId = 0; playerId < histories.Length; playerId++)
        {
            // Step 1: Count how many times each card appears in this player's history.
            //
            // Example:
            // history = [4, 9, 4, 2]
            // frequency becomes:
            // 4 -> 2
            // 9 -> 1
            // 2 -> 1
            //
            // Why is this necessary?
            // Because order does NOT matter, but frequency DOES matter.
            // So [4,9,4,2] and [2,4,9,4] should match,
            // while [4,9,2] should not match because it has only one 4.
            var frequency = new Dictionary<int, int>();

            foreach (int cardId in histories[playerId])
            {
                if (frequency.ContainsKey(cardId))
                {
                    frequency[cardId]++;
                }
                else
                {
                    frequency[cardId] = 1;
                }
            }

            // Step 2: Convert the frequency map into a canonical signature.
            //
            // Important idea:
            // A Dictionary does not guarantee a stable order for iteration.
            // That means if we directly serialize the dictionary, two equivalent histories
            // might accidentally produce different strings just because the key iteration
            // order differs.
            //
            // To fix that, we:
            //   1. Extract all distinct card IDs
            //   2. Sort them
            //   3. Append "cardId:count" pairs in sorted order
            //
            // Example:
            // frequency = { 4->2, 9->1, 2->1 }
            // sorted keys = [2, 4, 9]
            // signature = "2:1|4:2|9:1|"
            //
            // Another equivalent history [2,4,9,4] will produce the exact same signature.
            string signature = BuildCanonicalSignature(frequency);

            // Step 3: Place this player into the group for that signature.
            //
            // If this signature has not been seen before, create a new list.
            // Otherwise, append to the existing list.
            if (!groups.ContainsKey(signature))
            {
                groups[signature] = new List<int>();
            }

            groups[signature].Add(playerId);
        }

        // Step 4: Collect all player IDs that belong to a group of size at least 2.
        //
        // Why size >= 2?
        // A player only qualifies if there exists at least one OTHER player
        // with the same card-frequency profile.
        var result = new List<int>();

        foreach (var entry in groups)
        {
            List<int> playerIds = entry.Value;

            if (playerIds.Count >= 2)
            {
                // Add every player in this matching group.
                result.AddRange(playerIds);
            }
        }

        // Because we processed player IDs in ascending order and appended them in that order,
        // and because each player belongs to exactly one group, the final result is already
        // in ascending order in practice if dictionary groups are iterated arbitrarily?
        //
        // Important correction:
        // Dictionary iteration order should NOT be relied upon for sorted output.
        // The problem explicitly requires ascending order.
        //
        // So we sort the final answer to guarantee correctness.
        result.Sort();

        return result;
    }

    private string BuildCanonicalSignature(Dictionary<int, int> frequency)
    {
        // This helper method turns a frequency map into a stable, unique string.
        //
        // Why use a helper method?
        // It keeps the main algorithm easier to read and separates the "how to build
        // a signature" logic from the "how to group players" logic.

        // Extract all distinct card IDs.
        var keys = frequency.Keys.ToList();

        // Sort card IDs so equivalent histories always produce the same order.
        keys.Sort();

        // StringBuilder is used because repeatedly concatenating strings can be inefficient.
        // Since we may append many pieces, StringBuilder is the better data structure here.
        var sb = new StringBuilder();

        foreach (int cardId in keys)
        {
            // We append both the card ID and its count.
            //
            // Delimiters are important to avoid ambiguity.
            // For example:
            // "1:11|"
            // is clearly different from
            // "11:1|"
            sb.Append(cardId);
            sb.Append(':');
            sb.Append(frequency[cardId]);
            sb.Append('|');
        }

        return sb.ToString();
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] histories1 =
{
    new[] { 4, 9, 4, 2 },
    new[] { 2, 4, 9, 4 },
    new[] { 7, 7, 1 },
    new[] { 1, 7, 7 },
    new[] { 3, 5 }
};

var result1 = solution.FindPlayersWithReorderedHistories(histories1);
Console.WriteLine("Example 1 Output: [" + string.Join(",", result1) + "]");

// Expected: [0,1,2,3]
// Quick verification:
// Player 0 -> counts {2:1,4:2,9:1}
// Player 1 -> counts {2:1,4:2,9:1} => match
// Player 2 -> counts {1:1,7:2}
// Player 3 -> counts {1:1,7:2} => match
// Player 4 -> counts {3:1,5:1} => no match
// So output is [0,1,2,3]

// Example 2
int[][] histories2 =
{
    new[] { 1, 2, 3 },
    new[] { 3, 2, 1, 1 },
    Array.Empty<int>(),
    new[] { 5, 5 },
    Array.Empty<int>()
};

var result2 = solution.FindPlayersWithReorderedHistories(histories2);
Console.WriteLine("Example 2 Output: [" + string.Join(",", result2) + "]");

// Expected: [2,4]
// Quick verification:
// Player 0 -> {1:1,2:1,3:1}
// Player 1 -> {1:2,2:1,3:1} => different from player 0
// Player 2 -> {}
// Player 3 -> {5:2}
// Player 4 -> {}
// So only players 2 and 4 match

// Additional small demo
int[][] histories3 =
{
    new[] { 10 },
    new[] { 10, 10 },
    new[] { 10 },
    new[] { 20, 30 },
    new[] { 30, 20 }
};

var result3 = solution.FindPlayersWithReorderedHistories(histories3);
Console.WriteLine("Additional Demo Output: [" + string.Join(",", result3) + "]");

// Expected:
// Players 0 and 2 match => [0,2]
// Players 3 and 4 match => [3,4]
// Final sorted output => [0,2,3,4]