/*
 * Vacation Itinerary Collision Finder
 * ====================================
 * A travel company manages bookings for multiple tourists. Each tourist has an itinerary
 * represented as an ordered list of city codes (strings). Two tourists are said to have a
 * "collision" if they visit at least k consecutive cities in the exact same order at any
 * point in their respective itineraries.
 *
 * Given a list of itineraries (each a list of city codes) and an integer k, return a list
 * of all unique pairs [i, j] (where i < j) such that tourist i and tourist j have a collision.
 * Return the pairs sorted in ascending order by i, then by j.
 *
 * Example 1:
 *   Input: itineraries = [["NYC","LAX","CHI","MIA"],["SEA","NYC","LAX","CHI"],["BOS","MIA","DFW"]], k = 3
 *   Output: [[0, 1]]
 *
 * Example 2:
 *   Input: itineraries = [["A","B","C"],["B","C","D"],["A","B","C","D"]], k = 2
 *   Output: [[0, 1], [0, 2], [1, 2]]
 */

using System;
using System.Collections.Generic;
using System.Text;

/// <summary>
/// Solution class containing the algorithm to find itinerary collisions between tourists.
/// </summary>
class Solution
{
    /// <summary>
    /// Finds all pairs of tourists who share at least k consecutive cities in the same order.
    ///
    /// Time Complexity:  O(n^2 * L * k) where n = number of itineraries, L = max itinerary length
    ///                   For each pair (n^2/2 pairs), we extract all k-length windows from both
    ///                   itineraries (up to L windows each), hash them (O(k) per window), and
    ///                   check for intersection using a HashSet.
    ///
    /// Space Complexity: O(n * L * k) in the worst case for storing all window hashes across
    ///                   all itineraries, where each hash string is O(k) characters long.
    /// </summary>
    public List<List<int>> FindCollisions(List<List<string>> itineraries, int k)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Precompute all k-length window hashes for each itinerary.
        //
        // WHY: Instead of comparing every possible window pair between two itineraries
        //      naively (which would be very slow), we use hashing to represent each
        //      consecutive sequence of k cities as a single string key. This lets us
        //      use a HashSet for O(1) average-case lookup.
        //
        // DATA STRUCTURE CHOICE: We use a HashSet<string> per itinerary so that
        //      checking whether a window from itinerary j exists in itinerary i
        //      takes O(1) average time (after O(k) time to build the key).
        // -----------------------------------------------------------------------

        int n = itineraries.Count;

        // windowSets[i] will hold all unique k-length window hash strings for itinerary i.
        // We store them in a HashSet so membership queries are fast.
        var windowSets = new List<HashSet<string>>(n);

        for (int i = 0; i < n; i++)
        {
            // Create a new HashSet for this tourist's itinerary
            var set = new HashSet<string>();
            var itinerary = itineraries[i];

            // -----------------------------------------------------------------------
            // STEP 1a: Slide a window of size k across the current itinerary.
            //
            // For an itinerary of length L, there are (L - k + 1) possible windows
            // of size k. For example, if L=4 and k=3: windows start at index 0 and 1.
            //
            // WHY: We want to capture every possible consecutive subsequence of length k
            //      so we don't miss any potential collision point.
            // -----------------------------------------------------------------------
            int len = itinerary.Count;

            // Only generate windows if the itinerary is long enough to contain k cities
            if (len >= k)
            {
                for (int start = 0; start <= len - k; start++)
                {
                    // -----------------------------------------------------------------------
                    // STEP 1b: Build a canonical string key for the window [start, start+k).
                    //
                    // We join the city codes with a separator "|" to avoid false collisions.
                    // For example, ["AB","C"] and ["A","BC"] would both join to "AB|C" and
                    // "A|BC" respectively — the separator ensures they are distinct.
                    //
                    // WHY USE A SEPARATOR: Without a separator, "NYC" + "LA" = "NYCLA" and
                    //      "NY" + "CLA" = "NYCLA" — these are different sequences but would
                    //      produce the same hash! The "|" character (not in city codes) prevents this.
                    // -----------------------------------------------------------------------
                    var sb = new StringBuilder();
                    for (int c = start; c < start + k; c++)
                    {
                        if (c > start)
                            sb.Append('|'); // separator between city codes
                        sb.Append(itinerary[c]);
                    }
                    set.Add(sb.ToString()); // add the window key to this itinerary's set
                }
            }

            windowSets.Add(set);
        }

        // -----------------------------------------------------------------------
        // STEP 2: Compare every pair of itineraries (i, j) where i < j.
        //
        // WHY: We need to check all unique pairs. Since collision is symmetric
        //      (if i collides with j, then j collides with i), we only need to
        //      check each pair once, hence i < j.
        //
        // APPROACH: For each pair (i, j), we check whether any k-length window
        //      from itinerary j exists in the precomputed HashSet for itinerary i.
        //      If yes, the pair has a collision.
        // -----------------------------------------------------------------------

        var result = new List<List<int>>();

        for (int i = 0; i < n - 1; i++)
        {
            for (int j = i + 1; j < n; j++)
            {
                // -----------------------------------------------------------------------
                // STEP 2a: Check if any window from itinerary j appears in itinerary i's set.
                //
                // We iterate over all k-length windows of itinerary j and check membership
                // in windowSets[i]. As soon as we find one match, we know there's a collision
                // and we can stop early (no need to check further windows).
                //
                // WHY CHECK j's WINDOWS AGAINST i's SET (not the other way):
                //      Either direction works because if window W appears in both i and j,
                //      then W is in windowSets[i] AND in windowSets[j]. Checking j's windows
                //      against i's set is equivalent to checking i's windows against j's set.
                //      We pick one direction for simplicity.
                // -----------------------------------------------------------------------

                bool collisionFound = false;
                var itineraryJ = itineraries[j];
                int lenJ = itineraryJ.Count;

                if (lenJ >= k && windowSets[i].Count > 0)
                {
                    for (int start = 0; start <= lenJ - k; start++)
                    {
                        // Build the window key for this window of itinerary j
                        var sb = new StringBuilder();
                        for (int c = start; c < start + k; c++)
                        {
                            if (c > start)
                                sb.Append('|');
                            sb.Append(itineraryJ[c]);
                        }
                        string windowKey = sb.ToString();

                        // Check if this window exists in itinerary i's set
                        if (windowSets[i].Contains(windowKey))
                        {
                            // Found a collision! Record this pair and stop checking more windows.
                            collisionFound = true;
                            break;
                        }
                    }
                }

                // -----------------------------------------------------------------------
                // STEP 2b: If a collision was found, add the pair [i, j] to the result.
                //
                // We always store i < j (guaranteed by our loop structure), so the output
                // is naturally sorted by i first, then by j.
                // -----------------------------------------------------------------------
                if (collisionFound)
                {
                    result.Add(new List<int> { i, j });
                }
            }
        }

        // -----------------------------------------------------------------------
        // STEP 3: Return the result list.
        //
        // The result is already sorted by i (outer loop) and then by j (inner loop)
        // because we iterate i from 0 to n-2 and j from i+1 to n-1.
        // -----------------------------------------------------------------------
        return result;
    }
}

// ===============================================================================
// DEMO CODE — Verifies the solution against the provided examples
// ===============================================================================

var solution = new Solution();

// -----------------------------------------------------------------------
// Example 1:
//   itineraries = [["NYC","LAX","CHI","MIA"],["SEA","NYC","LAX","CHI"],["BOS","MIA","DFW"]]
//   k = 3
//   Expected Output: [[0, 1]]
//
// Trace:
//   windowSets[0] (k=3): "NYC|LAX|CHI", "LAX|CHI|MIA"
//   windowSets[1] (k=3): "SEA|NYC|LAX", "NYC|LAX|CHI"
//   windowSets[2] (k=3): "BOS|MIA|DFW"
//
//   Pair (0,1): Check j=1 windows against set[0]:
//     "SEA|NYC|LAX" in set[0]? No
//     "NYC|LAX|CHI" in set[0]? YES → collision!
//   Pair (0,2): Check j=2 windows against set[0]:
//     "BOS|MIA|DFW" in set[0]? No → no collision
//   Pair (1,2): Check j=2 windows against set[1]:
//     "BOS|MIA|DFW" in set[1]? No → no collision
//
//   Result: [[0, 1]] ✓
// -----------------------------------------------------------------------

Console.WriteLine("=== Example 1 ===");
var itineraries1 = new List<List<string>>
{
    new List<string> { "NYC", "LAX", "CHI", "MIA" },
    new List<string> { "SEA", "NYC", "LAX", "CHI" },
    new List<string> { "BOS", "MIA", "DFW" }
};
int k1 = 3;
var result1 = solution.FindCollisions(itineraries1, k1);
Console.WriteLine($"Input k = {k1}");
Console.WriteLine("Itineraries:");
for (int i = 0; i < itineraries1.Count; i++)
    Console.WriteLine($"  Tourist {i}: [{string.Join(", ", itineraries1[i])}]");
Console.Write("Output: [");
for (int i = 0; i < result1.Count; i++)
{
    if (i > 0) Console.Write(", ");
    Console.Write($"[{result1[i][0]}, {result1[i][1]}]");
}
Console.WriteLine("]");
Console.WriteLine("Expected: [[0, 1]]");
Console.WriteLine();

// -----------------------------------------------------------------------
// Example 2:
//   itineraries = [["A","B","C"],["B","C","D"],["A","B","C","D"]]
//   k = 2
//   Expected Output: [[0, 1], [0, 2], [1, 2]]
//
// Trace:
//   windowSets[0] (k=2): "A|B", "B|C"
//   windowSets[1] (k=2): "B|C", "C|D"
//   windowSets[2] (k=2): "A|B", "B|C", "C|D"
//
//   Pair (0,1): Check j=1 windows against set[0]:
//     "B|C" in set[0]? YES → collision!
//   Pair (0,2): Check j=2 windows against set[0]:
//     "A|B" in set[0]? YES → collision!
//   Pair (1,2): Check j=2 windows against set[1]:
//     "A|B" in set[1]? No
//     "B|C" in set[1]? YES → collision!
//
//   Result: [[0, 1], [0, 2], [1, 2]] ✓
// -----------------------------------------------------------------------

Console.WriteLine("=== Example 2 ===");
var itineraries2 = new List<List<string>>
{
    new List<string> { "A", "B", "C" },
    new List<string> { "B", "C", "D" },
    new List<string> { "A", "B", "C", "D" }
};
int k2 = 2;
var result2 = solution.FindCollisions(itineraries2, k2);
Console.WriteLine($"Input k = {k2}");
Console.WriteLine("Itineraries:");
for (int i = 0; i < itineraries2.Count; i++)
    Console.WriteLine($"  Tourist {i}: [{string.Join(", ", itineraries2[i])}]");
Console.Write("Output: [");
for (int i = 0; i < result2.Count; i++)
{
    if (i > 0) Console.Write(", ");
    Console.Write($"[{result2[i][0]}, {result2[i][1]}]");
}
Console.WriteLine("]");
Console.WriteLine("Expected: [[0, 1], [0, 2], [1, 2]]");
Console.WriteLine();

// -----------------------------------------------------------------------
// Additional Edge Case: k larger than some itineraries
//   itineraries = [["A","B"],["A","B","C"],["X","Y"]]
//   k = 3
//   Expected: [[]] — only tourist 1 has length >= 3, tourist 0 has length 2
//   Pair (0,1): set[0] has no windows (len=2 < k=3), so no collision
//   Pair (0,2): set[0] empty, no collision
//   Pair (1,2): set[1] = {"A|B|C"}, j=2 windows: "X|Y|..." — len=2 < k=3, no collision
//   Result: [] ✓
// -----------------------------------------------------------------------

Console.WriteLine("=== Edge Case: k larger than some itineraries ===");
var itineraries3 = new List<List<string>>
{
    new List<string> { "A", "B" },
    new List<string> { "A", "B", "C" },
    new List<string> { "X", "Y" }
};
int k3 = 3;
var result3 = solution.FindCollisions(itineraries3, k3);
Console.WriteLine($"Input k = {k3}");
Console.WriteLine("Itineraries:");
for (int i = 0; i < itineraries3.Count; i++)
    Console.WriteLine($"  Tourist {i}: [{string.Join(", ", itineraries3[i])}]");
Console.Write("Output: [");
for (int i = 0; i < result3.Count; i++)
{
    if (i > 0) Console.Write(", ");
    Console.Write($"[{result3[i][0]}, {result3[i][1]}]");
}
Console.WriteLine("]");
Console.WriteLine("Expected: []");