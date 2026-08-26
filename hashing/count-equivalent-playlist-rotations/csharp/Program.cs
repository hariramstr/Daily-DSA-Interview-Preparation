/*
Title: Count Equivalent Playlist Rotations
Difficulty: Medium
Topic: Hashing

Problem Description:
A music platform stores many playlists, where each playlist is represented as an array of song IDs.
Two playlists are considered equivalent if one can be obtained from the other by a circular rotation.

Examples:
- [4, 7, 9, 4] and [9, 4, 4, 7] are equivalent
- [4, 7, 9] and [7, 9, 8] are not equivalent

You are given a list of playlists, where each playlist may have a different length.
Count how many unordered pairs of playlists are equivalent under circular rotation.
Playlists of different lengths can never be equivalent.

Constraints:
- 1 <= playlists.length <= 100000
- 1 <= total number of song IDs across all playlists <= 200000
- 1 <= playlists[i].length <= 200000
- 0 <= song IDs <= 1000000000
- The sum of all playlist lengths does not exceed 200000

Key idea:
Two arrays are rotation-equivalent if and only if their lexicographically smallest rotation is identical.
So for each playlist:
1. Compute its canonical form = lexicographically smallest rotation
2. Convert that canonical form into a hashable key
3. Count how many times each key appears
4. For each group of size k, add k * (k - 1) / 2 pairs
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    Let S be the total number of song IDs across all playlists.
    - Computing the minimum rotation for one playlist of length m takes O(m) using Booth's algorithm.
    - Building the canonical key for that playlist also takes O(m).
    Therefore, total time across all playlists is O(S).

    Space Complexity:
    - O(S) total for storing canonical keys in the dictionary in the worst case.
    - O(1) extra algorithmic space per playlist, excluding the output key string.
    */
    public long CountEquivalentPairs(IList<IList<int>> playlists)
    {
        // This dictionary groups playlists by their canonical rotation signature.
        // Key   = canonical representation of the playlist
        // Value = how many playlists seen so far belong to this equivalence class
        //
        // Why a dictionary?
        // Because once we reduce every playlist to a single canonical form,
        // the problem becomes a standard "count equal items" problem.
        var counts = new Dictionary<string, long>();

        // We will accumulate the answer incrementally.
        // If a canonical form has already appeared c times, then the next playlist
        // with the same canonical form creates exactly c new unordered pairs.
        long pairs = 0;

        // Process every playlist independently.
        foreach (var playlist in playlists)
        {
            // Convert the incoming IList<int> to an array for fast indexed access.
            // Booth's algorithm needs random access by index many times.
            int[] arr = playlist as int[] ?? playlist.ToArray();

            // Compute the canonical signature of this playlist.
            // Two playlists are equivalent under rotation if and only if
            // their canonical signatures are exactly the same.
            string key = BuildCanonicalKey(arr);

            // If we have already seen this canonical form before,
            // then each previous occurrence forms one new pair with the current playlist.
            if (counts.TryGetValue(key, out long seen))
            {
                pairs += seen;
                counts[key] = seen + 1;
            }
            else
            {
                counts[key] = 1;
            }
        }

        return pairs;
    }

    private string BuildCanonicalKey(int[] arr)
    {
        // Step 1:
        // Find the starting index of the lexicographically smallest rotation.
        //
        // Example:
        // arr = [2,3,1]
        // rotations:
        //   [2,3,1]
        //   [3,1,2]
        //   [1,2,3]  <- smallest
        // start index = 2
        int start = LexicographicallySmallestRotationIndex(arr);

        // Step 2:
        // Build a string key from that rotation.
        //
        // We include the length first to make the representation explicit,
        // although different lengths would already produce different sequences.
        //
        // We also use a separator character that cannot be confused with digits.
        // This avoids accidental collisions like:
        // [1, 23] vs [12, 3]
        // Both would become "123" if concatenated naively, which would be wrong.
        var sb = new StringBuilder();
        sb.Append(arr.Length);
        sb.Append('#');

        for (int i = 0; i < arr.Length; i++)
        {
            int value = arr[(start + i) % arr.Length];
            sb.Append(value);
            sb.Append(',');
        }

        return sb.ToString();
    }

    private int LexicographicallySmallestRotationIndex(int[] arr)
    {
        int n = arr.Length;

        // For a single-element playlist, every rotation is the same.
        // The only valid starting index is 0.
        if (n == 1)
        {
            return 0;
        }

        // We use Booth's algorithm.
        //
        // What problem does Booth's algorithm solve?
        // It finds the starting position of the lexicographically smallest rotation
        // in linear time O(n), which is much faster than generating all rotations.
        //
        // Core idea:
        // Compare candidate starting positions i and j.
        // Advance a matched-length counter k while elements are equal.
        // When a mismatch happens, discard the worse candidate.
        //
        // This works because if one rotation is proven larger at the first differing
        // position, it can never be the minimum rotation.
        int i = 0; // first candidate start
        int j = 1; // second candidate start
        int k = 0; // number of matched elements so far

        while (i < n && j < n && k < n)
        {
            // Compare the k-th element of the rotation starting at i
            // with the k-th element of the rotation starting at j.
            //
            // We use modulo n because rotations wrap around circularly.
            int a = arr[(i + k) % n];
            int b = arr[(j + k) % n];

            if (a == b)
            {
                // So far these two candidate rotations are identical for one more step.
                // Continue comparing deeper into the rotation.
                k++;
            }
            else if (a > b)
            {
                // Rotation starting at i is lexicographically larger than rotation at j.
                // Therefore i cannot be the smallest rotation.
                //
                // All starts from i through i+k are also invalid as the minimum candidate
                // for the same reason, so we skip them in one jump.
                i = i + k + 1;

                // If both pointers collide, move one forward so they remain distinct.
                if (i == j)
                {
                    i++;
                }

                // Reset matched prefix length because we are comparing new candidates now.
                k = 0;
            }
            else
            {
                // Symmetric case:
                // Rotation at j is larger, so j cannot be the minimum.
                j = j + k + 1;

                if (i == j)
                {
                    j++;
                }

                k = 0;
            }
        }

        // The smaller surviving candidate index is the answer.
        int result = Math.Min(i, j);

        // Safety normalization, though Booth's algorithm already guarantees
        // the result is within [0, n-1] when used correctly.
        return result % n;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// playlists = [[1,2,3],[2,3,1],[3,1,2],[1,3,2],[5],[5]]
// Equivalent groups:
// [1,2,3], [2,3,1], [3,1,2] => 3 choose 2 = 3 pairs
// [5], [5] => 1 pair
// Total = 4
var playlists1 = new List<IList<int>>
{
    new List<int> { 1, 2, 3 },
    new List<int> { 2, 3, 1 },
    new List<int> { 3, 1, 2 },
    new List<int> { 1, 3, 2 },
    new List<int> { 5 },
    new List<int> { 5 }
};

long result1 = solution.CountEquivalentPairs(playlists1);
Console.WriteLine(result1); // Expected: 4

// Example 2:
// playlists = [[8,8,1],[8,1,8],[1,8,8],[2,2],[2,2],[2],[3,4,3]]
// Equivalent groups:
// [8,8,1], [8,1,8], [1,8,8] => 3 pairs
// [2,2], [2,2] => 1 pair
// [2] is different length, so not equivalent to [2,2]
// [3,4,3] stands alone
// Total = 4
var playlists2 = new List<IList<int>>
{
    new List<int> { 8, 8, 1 },
    new List<int> { 8, 1, 8 },
    new List<int> { 1, 8, 8 },
    new List<int> { 2, 2 },
    new List<int> { 2, 2 },
    new List<int> { 2 },
    new List<int> { 3, 4, 3 }
};

long result2 = solution.CountEquivalentPairs(playlists2);
Console.WriteLine(result2); // Expected: 4

// Additional quick sanity checks

var playlists3 = new List<IList<int>>
{
    new List<int> { 4, 7, 9, 4 },
    new List<int> { 9, 4, 4, 7 }, // rotation-equivalent to first
    new List<int> { 4, 7, 9 },    // different length from above
    new List<int> { 7, 9, 8 }     // not equivalent to [4,7,9]
};

long result3 = solution.CountEquivalentPairs(playlists3);
Console.WriteLine(result3); // Expected: 1