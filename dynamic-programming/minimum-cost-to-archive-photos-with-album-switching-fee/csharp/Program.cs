/*
Title: Minimum Cost to Archive Photos with Album Switching Fee

Problem Description:
A cloud photo service needs to archive a sequence of photos in the exact order they were taken.
Each photo belongs to one of m visual categories, numbered from 1 to m.

You must place every photo into one of two archive albums, Album A or Album B, while preserving
the original order inside each album.

When a photo of category x is appended to an album, the service pays a storage adjustment cost
based on the previous category already stored in that same album.
- If the album is empty, the cost is 0.
- Otherwise, if the previous category in that album was y, the cost is switchCost[y][x].

Your task is to assign every photo to either Album A or Album B so that the total cost is minimized.

More formally:
- You are given an array photos of length n, where photos[i] is the category of the i-th photo.
- You are also given an m x m matrix switchCost.
- Process photos from left to right.
- For each photo, choose Album A or Album B.
- The relative order of photos assigned to the same album must remain the same as in the original sequence.
- Return the minimum possible total adjustment cost.

Key idea:
A strong dynamic programming solution does NOT need to remember the full contents of both albums.
It is enough to remember the most recent photo placed into each album, because future cost only
depends on the last category already stored in the chosen album.

Constraints:
- 1 <= n <= 2000
- 1 <= m <= 50
- 1 <= photos[i] <= m
- 0 <= switchCost[a][b] <= 10^6
- The answer fits in a 64-bit signed integer
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n^2)

    Why?
    We process the photos from left to right.
    For each position i, we may iterate over all possible "last index in the other album" states,
    which is at most i states. Summed over all i, this is O(n^2).

    Space Complexity:
    O(n)

    Why?
    We only keep DP values for the current step and the next step.
    Each DP array has size about n + 1.
    */
    public long MinimumArchiveCost(int[] photos, int[][] switchCost)
    {
        int n = photos.Length;

        // If there are no photos or only one photo, no album ever receives a second photo,
        // so no transition cost is paid.
        if (n <= 1)
        {
            return 0;
        }

        // We will use 0-based categories inside the algorithm because C# arrays are 0-based.
        // The input categories are 1..m, so we subtract 1 once here.
        int[] a = new int[n];
        for (int i = 0; i < n; i++)
        {
            a[i] = photos[i] - 1;
        }

        // A very large number used as "infinity" for minimization.
        long INF = long.MaxValue / 4;

        // DP state design:
        //
        // After processing photos[0..i], one of the albums must end with photo i,
        // because the current photo must be placed somewhere and therefore becomes
        // the most recent photo of that chosen album.
        //
        // So instead of storing both last indices explicitly, we store:
        // dp[j] = minimum cost after processing up to current i,
        //         where:
        //         - one album's last photo is exactly i
        //         - the other album's last photo is j
        //
        // Special meaning of j:
        // - j == -1 means the other album is still empty.
        //
        // Since arrays cannot use -1 directly as an index, we shift by +1:
        // index 0   <=> j = -1 (empty)
        // index 1   <=> j = 0
        // index 2   <=> j = 1
        // ...
        // index n   <=> j = n - 1
        //
        // At the start, after processing only photo 0:
        // - one album contains photo 0
        // - the other album is empty
        // - cost is 0
        //
        // Therefore:
        // dp[0] = 0
        long[] dp = new long[n + 1];
        Array.Fill(dp, INF);
        dp[0] = 0;

        // We now process photos one by one, starting from the second photo.
        for (int i = 1; i < n; i++)
        {
            long[] next = new long[n + 1];
            Array.Fill(next, INF);

            // We are transitioning from states that describe processing up to i - 1
            // into states that describe processing up to i.
            //
            // In the old state:
            // - one album ends at photo i - 1
            // - the other album ends at j (or is empty if j = -1)
            //
            // We now need to place photo i into one of the two albums.
            for (int shifted = 0; shifted <= n; shifted++)
            {
                long currentCost = dp[shifted];
                if (currentCost == INF)
                {
                    continue;
                }

                int j = shifted - 1;

                // ------------------------------------------------------------
                // Option 1:
                // Put photo i into the SAME album that currently ends at i - 1.
                //
                // Why is this valid?
                // Because that album's order is preserved automatically:
                // photo i comes after photo i - 1 in the original sequence.
                //
                // What cost is added?
                // The previous category in that album is a[i - 1],
                // and the new category is a[i].
                // So we add switchCost[a[i - 1]][a[i]].
                //
                // What is the new state?
                // - This album now ends at i.
                // - The other album still ends at j.
                // So next[j] is updated.
                // ------------------------------------------------------------
                long costSameAlbum = currentCost + switchCost[a[i - 1]][a[i]];
                if (costSameAlbum < next[shifted])
                {
                    next[shifted] = costSameAlbum;
                }

                // ------------------------------------------------------------
                // Option 2:
                // Put photo i into the OTHER album.
                //
                // Why is this valid?
                // The other album currently ends at j, or is empty if j = -1.
                // Appending photo i there preserves order because i is the newest
                // processed photo overall.
                //
                // What cost is added?
                // - If the other album is empty (j == -1), cost is 0.
                // - Otherwise, we add switchCost[a[j]][a[i]].
                //
                // What is the new state?
                // - The album that used to end at i - 1 does NOT receive photo i,
                //   so it still ends at i - 1.
                // - The other album now ends at i.
                //
                // Our DP convention always stores the state as:
                // "one album ends at current i, the other ends at something else".
                //
                // Therefore the "something else" becomes i - 1.
                // So we update next[(i - 1) + 1] = next[i].
                // ------------------------------------------------------------
                long add = 0;
                if (j != -1)
                {
                    add = switchCost[a[j]][a[i]];
                }

                long costOtherAlbum = currentCost + add;
                int newShifted = i; // because this represents j = i - 1, and shifted = j + 1 = i
                if (costOtherAlbum < next[newShifted])
                {
                    next[newShifted] = costOtherAlbum;
                }
            }

            dp = next;
        }

        // After all photos are processed, every valid final arrangement is represented
        // by some dp state. We simply take the minimum over all possible "other album last index".
        long answer = INF;
        for (int shifted = 0; shifted <= n; shifted++)
        {
            if (dp[shifted] < answer)
            {
                answer = dp[shifted];
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] photos1 = { 1, 2, 1 };
int[][] switchCost1 =
{
    new[] { 0, 5 },
    new[] { 4, 0 }
};

long result1 = solution.MinimumArchiveCost(photos1, switchCost1);
Console.WriteLine(result1); // Expected: 0

// Example 2
int[] photos2 = { 1, 3, 2, 3 };
int[][] switchCost2 =
{
    new[] { 0, 7, 2 },
    new[] { 6, 0, 5 },
    new[] { 4, 1, 0 }
};

long result2 = solution.MinimumArchiveCost(photos2, switchCost2);
Console.WriteLine(result2); // Expected: 1

// Additional small sanity check
int[] photos3 = { 2 };
int[][] switchCost3 =
{
    new[] { 0, 1 },
    new[] { 2, 0 }
};

long result3 = solution.MinimumArchiveCost(photos3, switchCost3);
Console.WriteLine(result3); // Expected: 0