/*
Title: Minimum Repaints to Form Three Color Districts

Problem Description:
A city boulevard is decorated with a row of buildings, represented by a string colors of length n.
Each character is one of 'R', 'G', or 'B', indicating the current paint color of a building.

The mayor wants the boulevard to be divided into exactly three contiguous non-empty districts from left to right:
- the first district must be entirely red ('R')
- the second district must be entirely green ('G')
- the third district must be entirely blue ('B')

In one operation, you may repaint any single building to any of the three colors.

Return the minimum number of repaint operations required to transform the boulevard into a valid arrangement
of the form R...RG...GB...B, where all three districts are contiguous and each district contains at least one building.

Constraints:
- 3 <= n <= 200000
- colors.length == n
- colors[i] is one of 'R', 'G', or 'B'

Examples:
1) colors = "RGRBB"
   Output: 1

2) colors = "BBRGRG"
   Output: 3

Key idea:
We must choose two split points:
- first district:  [0 .. i]
- second district: [i+1 .. j]
- third district:  [j+1 .. n-1]
with 0 <= i < j < n-1 so that all three districts are non-empty.

A direct brute-force search over all (i, j) pairs would be O(n^2), which is too slow for n up to 200000.

Efficient approach:
Use prefix counts to quickly compute:
- repaint cost to make any prefix all 'R'
- repaint cost to make any middle segment all 'G'
- repaint cost to make any suffix all 'B'

Then scan all valid split positions in O(n).
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of complexity:
    - We build three prefix count arrays in O(n).
    - Then we scan all possible first split positions in O(n).
    - Every segment repaint cost is computed in O(1) using prefix counts.
    - Total is linear in the length of the string.
    */
    public int MinimumRepaints(string colors)
    {
        int n = colors.Length;

        // These prefix arrays store how many times each color appears
        // from the beginning of the string up to each position.
        //
        // prefixR[i] = number of 'R' characters in colors[0..i-1]
        // prefixG[i] = number of 'G' characters in colors[0..i-1]
        // prefixB[i] = number of 'B' characters in colors[0..i-1]
        //
        // We use length n + 1 so that:
        // - prefixX[0] means "empty prefix"
        // - prefixX[k] means first k characters
        //
        // This is a standard and very useful trick because it lets us compute
        // counts inside any substring [l..r] in O(1):
        // count = prefix[r+1] - prefix[l]
        int[] prefixR = new int[n + 1];
        int[] prefixG = new int[n + 1];
        int[] prefixB = new int[n + 1];

        // Build prefix counts.
        // At each step, we copy previous totals and then add the current character.
        for (int i = 0; i < n; i++)
        {
            prefixR[i + 1] = prefixR[i];
            prefixG[i + 1] = prefixG[i];
            prefixB[i + 1] = prefixB[i];

            if (colors[i] == 'R')
            {
                prefixR[i + 1]++;
            }
            else if (colors[i] == 'G')
            {
                prefixG[i + 1]++;
            }
            else
            {
                prefixB[i + 1]++;
            }
        }

        // We will try every valid pair of districts indirectly.
        //
        // Let:
        // - first district be [0..i]
        // - second district be [i+1..j]
        // - third district be [j+1..n-1]
        //
        // Instead of checking all j for every i (which would be O(n^2)),
        // we precompute the best possible "green then blue" cost starting from each position.
        //
        // Specifically:
        // bestGBStart[k] = minimum repaint cost to transform substring colors[k..n-1]
        // into:
        //   one non-empty all-'G' segment followed by one non-empty all-'B' segment
        //
        // Then the final answer becomes:
        // min over i from 0 to n-3:
        //   cost(make [0..i] all R) + bestGBStart[i+1]
        //
        // This reduces the full problem to a single linear scan.
        int[] bestGBStart = new int[n];

        // To compute bestGBStart[k], we need to choose a split j such that:
        // - green segment = [k..j]
        // - blue segment  = [j+1..n-1]
        // and both are non-empty, so j must satisfy k <= j <= n-2.
        //
        // Cost of making [k..j] all G:
        //   length - number_of_G
        //
        // Cost of making [j+1..n-1] all B:
        //   length - number_of_B
        //
        // Doing this naively for every k would still be too slow.
        //
        // We derive a formula:
        //
        // costG(k, j) = (j - k + 1) - countG(k, j)
        // costB(j+1, n-1) = (n - 1 - (j+1) + 1) - countB(j+1, n-1)
        //                 = (n - j - 1) - countB(j+1, n-1)
        //
        // Total:
        //   (j-k+1) - countG(k,j) + (n-j-1) - countB(j+1,n-1)
        // = (n-k) - countG(k,j) - countB(j+1,n-1)
        //
        // Since (n-k) is fixed for a given k, minimizing repaint cost is equivalent to
        // maximizing:
        //   countG(k,j) + countB(j+1,n-1)
        //
        // Rewrite using prefix counts:
        // countG(k,j) = prefixG[j+1] - prefixG[k]
        // countB(j+1,n-1) = prefixB[n] - prefixB[j+1]
        //
        // Sum:
        //   prefixG[j+1] - prefixG[k] + prefixB[n] - prefixB[j+1]
        // = (prefixG[j+1] - prefixB[j+1]) + (prefixB[n] - prefixG[k])
        //
        // For fixed k, only the first parenthesized part depends on j+1.
        //
        // So if we precompute suffix maxima of:
        //   prefixG[t] - prefixB[t]
        // for valid t = j+1 in [k+1 .. n-1],
        // then we can compute bestGBStart[k] in O(1).
        //
        // Why t only goes to n-1:
        // Because j <= n-2, so j+1 <= n-1.
        // This ensures the blue segment [j+1..n-1] is non-empty.
        int[] suffixMaxDiff = new int[n + 1];

        // Initialize with a very small number.
        // We only care about indices 1..n-1 as valid split boundaries between G and B.
        const int NEG_INF = int.MinValue / 4;
        for (int i = 0; i <= n; i++)
        {
            suffixMaxDiff[i] = NEG_INF;
        }

        // Build suffix maxima for diff[t] = prefixG[t] - prefixB[t].
        //
        // suffixMaxDiff[t] will store:
        //   max(diff[x]) for x in [t..n-1]
        //
        // Again, we stop at n-1 because x = n would mean the blue segment is empty,
        // which is not allowed.
        for (int t = n - 1; t >= 1; t--)
        {
            int diff = prefixG[t] - prefixB[t];
            suffixMaxDiff[t] = Math.Max(diff, suffixMaxDiff[t + 1]);
        }

        // Compute bestGBStart[k] for every possible start k of the "G then B" suffix.
        //
        // Valid k values for the full problem are at least 1 and at most n-2,
        // because:
        // - first district must be non-empty, so k = i+1 >= 1
        // - there must still be room for both G and B districts, so k <= n-2
        //
        // We fill the whole array for convenience, but only valid positions will be used.
        for (int k = 0; k < n; k++)
        {
            // Need at least one character for G and one for B.
            // So if k > n-2, it is impossible.
            if (k > n - 2)
            {
                bestGBStart[k] = int.MaxValue / 4;
                continue;
            }

            // We need the best split boundary t = j+1 in [k+1 .. n-1].
            //
            // suffixMaxDiff[k+1] gives:
            //   max(prefixG[t] - prefixB[t]) for t in [k+1 .. n-1]
            int bestDiff = suffixMaxDiff[k + 1];

            // Using the derived formula:
            // minimum repaint cost for colors[k..n-1] as G...GB...B is:
            //
            // (n - k) - (prefixB[n] - prefixG[k] + bestDiff)
            //
            // This is exactly:
            // total length of suffix
            // minus
            // maximum number of positions already matching the desired G/B arrangement.
            bestGBStart[k] = (n - k) - (prefixB[n] - prefixG[k] + bestDiff);
        }

        int answer = int.MaxValue;

        // Now choose the end i of the red district.
        //
        // Valid i range:
        // - i >= 0 because red district must be non-empty
        // - i <= n-3 because we still need at least one building for G and one for B
        for (int i = 0; i <= n - 3; i++)
        {
            // Cost to make prefix [0..i] all red:
            // segment length - number of already-red buildings in that segment
            //
            // length = i + 1
            // countR = prefixR[i+1]
            int redCost = (i + 1) - prefixR[i + 1];

            // The remaining suffix starts at i+1 and must become G...GB...B.
            int gbCost = bestGBStart[i + 1];

            int total = redCost + gbCost;
            if (total < answer)
            {
                answer = total;
            }
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

string colors1 = "RGRBB";
int result1 = solution.MinimumRepaints(colors1);
Console.WriteLine($"Input: {colors1}");
Console.WriteLine($"Minimum repaints: {result1}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

string colors2 = "BBRGRG";
int result2 = solution.MinimumRepaints(colors2);
Console.WriteLine($"Input: {colors2}");
Console.WriteLine($"Minimum repaints: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

string colors3 = "RGB";
int result3 = solution.MinimumRepaints(colors3);
Console.WriteLine($"Input: {colors3}");
Console.WriteLine($"Minimum repaints: {result3}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

string colors4 = "RRRGGGBBB";
int result4 = solution.MinimumRepaints(colors4);
Console.WriteLine($"Input: {colors4}");
Console.WriteLine($"Minimum repaints: {result4}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

string colors5 = "BBBBB";
int result5 = solution.MinimumRepaints(colors5);
Console.WriteLine($"Input: {colors5}");
Console.WriteLine($"Minimum repaints: {result5}");
Console.WriteLine("Expected: 2");
Console.WriteLine();