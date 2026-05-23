/*
 * ============================================================
 * Title: Playlist Song Popularity Tracker
 * Difficulty: Easy
 * Topic: Hashing
 * ============================================================
 *
 * Problem Description:
 * You are building a music streaming app. Users submit a log of song plays,
 * where each entry is a string representing the song title. Your task is to
 * determine which songs were played MORE THAN ONCE and return them sorted by
 * their play count in DESCENDING order. If two songs have the same play count,
 * sort them ALPHABETICALLY (ascending).
 *
 * Given a list of strings `plays` where each element is the title of a song
 * that was played, return a list of song titles that appear more than once,
 * sorted as described above.
 *
 * Constraints:
 * - 1 <= plays.length <= 10^4
 * - 1 <= plays[i].length <= 50
 * - plays[i] consists of lowercase English letters and spaces.
 * - Song titles are case-sensitive.
 *
 * Example 1:
 * Input:  ["shape of you", "blinding lights", "shape of you",
 *          "blinding lights", "shape of you", "levitating"]
 * Output: ["shape of you", "blinding lights"]
 * Explanation: "shape of you" played 3 times, "blinding lights" played 2 times.
 *              "levitating" played only once → excluded.
 *
 * Example 2:
 * Input:  ["song a", "song b", "song c", "song b", "song a", "song d", "song d"]
 * Output: ["song a", "song b", "song d"]
 * Explanation: All three songs played exactly 2 times → sorted alphabetically.
 * ============================================================
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ---------------------------------------------------------------
// Solution class containing the core algorithm
// ---------------------------------------------------------------
public class Solution
{
    /*
     * Method: GetPopularSongs
     *
     * Time Complexity:  O(n + k log k)
     *   - n = number of play entries  → one pass to build the frequency map
     *   - k = number of unique songs that appear more than once → sorting step
     *   In the worst case k ≈ n/2, so overall O(n log n).
     *
     * Space Complexity: O(u)
     *   - u = number of unique song titles stored in the dictionary.
     *   In the worst case every song is unique → O(n).
     */
    public List<string> GetPopularSongs(List<string> plays)
    {
        // -------------------------------------------------------
        // STEP 1: Create a frequency (count) map using a Dictionary.
        //
        // WHY a Dictionary?
        //   A Dictionary<string, int> gives us O(1) average-time
        //   lookups and insertions via hashing. This lets us count
        //   how many times each song title appears without nested
        //   loops (which would be O(n²)).
        //
        // Key   = song title  (string)
        // Value = play count  (int)
        // -------------------------------------------------------
        Dictionary<string, int> playCount = new Dictionary<string, int>();

        // Iterate over every play entry in the input list.
        foreach (string song in plays)
        {
            // Check whether we have already seen this song title.
            if (playCount.ContainsKey(song))
            {
                // Song already exists in the map → increment its count.
                playCount[song]++;
            }
            else
            {
                // First time we see this song → add it with count = 1.
                playCount[song] = 1;
            }
        }
        // After this loop, playCount holds the total play count for
        // every unique song title that appeared in the input.

        // -------------------------------------------------------
        // STEP 2: Filter — keep only songs played MORE THAN ONCE.
        //
        // WHY filter first?
        //   The problem explicitly asks us to exclude songs with
        //   a play count of exactly 1. Filtering before sorting
        //   reduces the number of elements we need to sort.
        // -------------------------------------------------------
        // Use LINQ's Where clause to select entries whose Value > 1.
        // ToList() materialises the lazy query into a concrete list
        // so we can sort it in the next step.
        List<KeyValuePair<string, int>> popularSongs =
            playCount
                .Where(entry => entry.Value > 1)
                .ToList();

        // -------------------------------------------------------
        // STEP 3: Sort the filtered songs with a two-level comparison.
        //
        // Primary sort   : play count DESCENDING  (higher count first)
        // Secondary sort : song title ASCENDING   (alphabetical A→Z)
        //                  applied when two songs share the same count
        //
        // WHY two-level sort?
        //   The problem specifies both criteria. Using OrderByDescending
        //   followed by ThenBy chains them cleanly and readably.
        // -------------------------------------------------------
        List<KeyValuePair<string, int>> sortedSongs =
            popularSongs
                .OrderByDescending(entry => entry.Value)   // primary:   count desc
                .ThenBy(entry => entry.Key)                // secondary: title asc
                .ToList();

        // -------------------------------------------------------
        // STEP 4: Extract just the song titles into the result list.
        //
        // WHY extract only titles?
        //   The problem asks us to return a list of song title strings,
        //   not key-value pairs. We use Select to project each pair
        //   down to its Key (the song title).
        // -------------------------------------------------------
        List<string> result = sortedSongs
            .Select(entry => entry.Key)
            .ToList();

        // Return the final sorted list of popular song titles.
        return result;
    }
}

// ---------------------------------------------------------------
// Demo / Driver Code
// ---------------------------------------------------------------

Solution solution = new Solution();

// ----------------------------
// Example 1
// ----------------------------
// "shape of you"    → 3 plays
// "blinding lights" → 2 plays
// "levitating"      → 1 play  (excluded)
// Expected output: ["shape of you", "blinding lights"]
List<string> plays1 = new List<string>
{
    "shape of you", "blinding lights", "shape of you",
    "blinding lights", "shape of you", "levitating"
};

List<string> result1 = solution.GetPopularSongs(plays1);

Console.WriteLine("=== Example 1 ===");
Console.WriteLine("Input:    [" + string.Join(", ", plays1) + "]");
Console.WriteLine("Output:   [" + string.Join(", ", result1) + "]");
Console.WriteLine("Expected: [shape of you, blinding lights]");
Console.WriteLine();

// ----------------------------
// Example 2
// ----------------------------
// "song a" → 2 plays
// "song b" → 2 plays
// "song c" → 1 play  (excluded)
// "song d" → 2 plays
// All three popular songs have the same count → alphabetical order
// Expected output: ["song a", "song b", "song d"]
List<string> plays2 = new List<string>
{
    "song a", "song b", "song c", "song b",
    "song a", "song d", "song d"
};

List<string> result2 = solution.GetPopularSongs(plays2);

Console.WriteLine("=== Example 2 ===");
Console.WriteLine("Input:    [" + string.Join(", ", plays2) + "]");
Console.WriteLine("Output:   [" + string.Join(", ", result2) + "]");
Console.WriteLine("Expected: [song a, song b, song d]");
Console.WriteLine();

// ----------------------------
// Example 3 — Edge case: all songs played once
// ----------------------------
// No song qualifies → empty list expected
List<string> plays3 = new List<string> { "alpha", "beta", "gamma" };

List<string> result3 = solution.GetPopularSongs(plays3);

Console.WriteLine("=== Example 3 (Edge: all once) ===");
Console.WriteLine("Input:    [" + string.Join(", ", plays3) + "]");
Console.WriteLine("Output:   [" + string.Join(", ", result3) + "]");
Console.WriteLine("Expected: []");
Console.WriteLine();

// ----------------------------
// Example 4 — Edge case: single song repeated many times
// ----------------------------
List<string> plays4 = new List<string>
{
    "one hit wonder", "one hit wonder", "one hit wonder"
};

List<string> result4 = solution.GetPopularSongs(plays4);

Console.WriteLine("=== Example 4 (Edge: single repeated song) ===");
Console.WriteLine("Input:    [" + string.Join(", ", plays4) + "]");
Console.WriteLine("Output:   [" + string.Join(", ", result4) + "]");
Console.WriteLine("Expected: [one hit wonder]");