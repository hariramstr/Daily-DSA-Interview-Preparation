/*
Title: Minimum Cost to Assemble a Playlist with Genre Switch Fees

Problem Description:
A music app wants to build a playlist of exactly k songs from a catalog shown in a fixed order.
For each song i, you are given:
- duration[i]
- enjoy[i]
- genre[i]

You may either skip a song or include it in the playlist, but the relative order of chosen songs
must remain the same as in the catalog.

The total cost of a playlist is:

1. For every selected song, you pay its duration.
2. For every pair of consecutive selected songs with different genres, you pay an additional switch fee s.
3. You receive a discount equal to the sum of enjoyment scores of all selected songs.

So the final playlist cost is:

(sum of durations) + (number of genre changes between consecutive chosen songs) * s - (sum of enjoyment scores)

Task:
Return the minimum possible total cost of selecting exactly k songs.
If it is impossible to choose exactly k songs, return -1.

Important note:
The examples in the prompt contain inconsistencies in their stated outputs and explanations.
This implementation follows the formula exactly as defined above, which is the source of truth.

Key observation:
For each chosen song, its direct contribution is:
    duration[i] - enjoy[i]

Then, whenever the genre changes between two consecutive chosen songs, we add:
    + s

This naturally leads to dynamic programming where we track:
- how many songs have been chosen so far
- what the genre of the last chosen song is

Because genre[i] is in the range [1, 50], we can efficiently keep DP by last genre.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * k * G)
    where:
    - n = number of songs
    - k = exact number of songs to choose
    - G = number of possible genres (at most 50)

    More precisely:
    For each song, for each chosen-count from k down to 1, we may scan all possible previous genres.
    Since G <= 50, this is efficient for n <= 200.

    Space Complexity:
    O(k * G)

    Explanation of the DP:
    dp[count, g] = minimum cost to choose exactly 'count' songs so far,
                   with the last chosen song having genre 'g'.

    Transition when considering song i with:
    - baseCost = duration[i] - enjoy[i]
    - currentGenre = genre[i]

    If this song becomes the first chosen song:
        dp[1, currentGenre] = min(dp[1, currentGenre], baseCost)

    If this song is appended after a previous chosen song whose last genre was prevGenre:
        newCost = dp[count - 1, prevGenre] + baseCost + (prevGenre == currentGenre ? 0 : s)

    We process count in descending order so each song is used at most once.
    */
    public long MinPlaylistCost(int[] duration, int[] enjoy, int[] genre, int k, int s)
    {
        int n = duration.Length;

        // If we need to choose more songs than exist, it is impossible.
        if (k > n) return -1;

        // The problem states genre values are between 1 and 50.
        // We use 1..50 directly as indices for clarity.
        const int MaxGenre = 50;

        // A very large value used to represent "unreachable state".
        // We choose a safely large long value to avoid overflow when adding costs.
        const long INF = long.MaxValue / 4;

        // dp[count, g] means:
        // minimum cost after processing some prefix of songs,
        // choosing exactly 'count' songs,
        // and the last chosen song has genre g.
        //
        // Initially, no states are reachable because we have not chosen any song yet.
        long[,] dp = new long[k + 1, MaxGenre + 1];

        // Fill every state with INF to mean "not yet possible".
        for (int count = 0; count <= k; count++)
        {
            for (int g = 0; g <= MaxGenre; g++)
            {
                dp[count, g] = INF;
            }
        }

        // We now process songs one by one in the given fixed order.
        // This is essential because the playlist must preserve the original order.
        for (int i = 0; i < n; i++)
        {
            int currentGenre = genre[i];

            // Each selected song contributes:
            // duration - enjoy
            //
            // Why?
            // Because the total formula is:
            //   sum(duration) + switchFees - sum(enjoy)
            //
            // So for each selected song, its direct contribution is:
            //   duration[i] - enjoy[i]
            long baseCost = (long)duration[i] - enjoy[i];

            // We iterate chosen count backwards.
            //
            // Why backwards?
            // Because this is a standard 0/1 knapsack-style update:
            // each song can be chosen at most once.
            //
            // If we iterated forwards, the current song could accidentally be used
            // multiple times in the same iteration.
            for (int count = k; count >= 1; count--)
            {
                // Case 1:
                // This song is the very first chosen song.
                //
                // Then there is no previous chosen song, so:
                // - no switch fee applies
                // - total cost is simply baseCost
                if (count == 1)
                {
                    if (baseCost < dp[1, currentGenre])
                    {
                        dp[1, currentGenre] = baseCost;
                    }
                }
                else
                {
                    // Case 2:
                    // This song is appended after an existing playlist of length count - 1.
                    //
                    // We must know the genre of the previous last chosen song,
                    // because the switch fee depends on whether the genre changes.
                    for (int prevGenre = 1; prevGenre <= MaxGenre; prevGenre++)
                    {
                        long previousCost = dp[count - 1, prevGenre];

                        // If the previous state is unreachable, skip it.
                        if (previousCost == INF) continue;

                        // Add switch fee only when the genre changes between
                        // consecutive chosen songs.
                        long switchFee = (prevGenre == currentGenre) ? 0L : s;

                        long candidate = previousCost + baseCost + switchFee;

                        if (candidate < dp[count, currentGenre])
                        {
                            dp[count, currentGenre] = candidate;
                        }
                    }
                }
            }
        }

        // The answer is the best cost among all possible last genres
        // after choosing exactly k songs.
        long answer = INF;

        for (int g = 1; g <= MaxGenre; g++)
        {
            if (dp[k, g] < answer)
            {
                answer = dp[k, g];
            }
        }

        // If no state is reachable, return -1.
        return answer == INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt.
// Important: the prompt's stated output/explanation is inconsistent.
// We compute according to the formula exactly.
int[] duration1 = { 4, 6, 3, 5 };
int[] enjoy1 = { 2, 5, 1, 4 };
int[] genre1 = { 1, 2, 2, 1 };
int k1 = 2;
int s1 = 3;

long result1 = solution.MinPlaylistCost(duration1, enjoy1, genre1, k1, s1);
Console.WriteLine(result1);

// Example 2 from the prompt.
// Again, the prompt's text contains inconsistencies.
// We compute according to the formula exactly.
int[] duration2 = { 7, 2, 6, 3, 4 };
int[] enjoy2 = { 1, 3, 2, 5, 1 };
int[] genre2 = { 1, 1, 3, 3, 2 };
int k2 = 3;
int s2 = 4;

long result2 = solution.MinPlaylistCost(duration2, enjoy2, genre2, k2, s2);
Console.WriteLine(result2);

// Additional small sanity check:
// Choose exactly 1 song => just minimize duration[i] - enjoy[i].
int[] duration3 = { 10, 2, 8 };
int[] enjoy3 = { 1, 5, 3 };
int[] genre3 = { 1, 2, 1 };
int k3 = 1;
int s3 = 100;

long result3 = solution.MinPlaylistCost(duration3, enjoy3, genre3, k3, s3);
Console.WriteLine(result3);