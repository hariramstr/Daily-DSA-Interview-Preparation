/*
Minimum Latency to Decode a Layered Signal Tape

Problem Description:
A telemetry system stores a long signal tape as a string s of length n, where each character
is an uppercase letter representing a frequency band.

To decode the tape, a hardware decoder may process any contiguous segment [l, r] in one pass
if the first and last characters of that segment are the same.

During that pass:
- the decoder resolves both matching endpoints together
- the inside of the segment may be decoded before, after, or split across additional passes

The latency of one pass is equal to the length of the segment being processed, i.e. r - l + 1.

Goal:
Compute the minimum total latency required to fully decode the entire tape.

Important interpretation:
A valid full decoding forms a recursive / non-crossing structure:
- a single character can always be decoded alone with cost 1
- if s[l] == s[r], then we may decode l and r together by paying (r - l + 1),
  while the inside [l + 1, r - 1] is decoded recursively
- we may also split the interval into independent parts

This naturally leads to interval dynamic programming.

Examples:
1) s = "ABCA"
   Optimal:
   - decode outer A...A with cost 4
   - decode B with cost 1
   - decode C with cost 1
   Total = 6

2) s = "ABBA"
   Optimal:
   - decode outer A...A with cost 4
   - decode inner B...B with cost 2
   Total = 6
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^3)
    Space Complexity: O(n^2)

    Explanation of complexity:
    - We use interval DP over all substrings.
    - There are O(n^2) substrings.
    - For each substring [l, r], we may try every split point k between l and r,
      which is O(n) work.
    - Therefore total time is O(n^3), which is acceptable for n <= 400.
    */
    public int MinimumLatency(string s)
    {
        int n = s.Length;

        // dp[l, r] will store the minimum total latency needed to fully decode
        // the substring s[l..r], inclusive.
        //
        // Why interval DP?
        // Because the operation is defined on contiguous segments, and the effect
        // of decoding a pair of matching endpoints naturally refers to the inside
        // substring. This is the classic sign that "substrings as states" is the
        // right approach.
        int[,] dp = new int[n, n];

        // We fill the DP table by increasing substring length.
        //
        // This order is necessary because:
        // - dp[l, r] may depend on dp[l + 1, r - 1] (the inside interval)
        // - dp[l, r] may also depend on dp[l, k] and dp[k + 1, r] (smaller intervals)
        //
        // Therefore, before computing a larger interval, all smaller intervals
        // must already be known.
        for (int len = 1; len <= n; len++)
        {
            for (int l = 0; l + len - 1 < n; l++)
            {
                int r = l + len - 1;

                // Base case: a single character can always be decoded alone.
                // The segment length is 1, so the cost is exactly 1.
                if (l == r)
                {
                    dp[l, r] = 1;
                    continue;
                }

                // Start with a very large value so we can minimize over all choices.
                int best = int.MaxValue;

                // Option 1: Split the interval into two independent parts.
                //
                // If we choose a split point k, then:
                // - decode s[l..k] optimally
                // - decode s[k+1..r] optimally
                //
                // Since these are independent subproblems, total cost is the sum.
                //
                // This option is necessary because the optimal solution may not use
                // the endpoints l and r together. Sometimes the best structure is
                // simply to divide the interval into smaller pieces.
                for (int k = l; k < r; k++)
                {
                    int candidate = dp[l, k] + dp[k + 1, r];
                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                // Option 2: Decode the two endpoints together, but only if they match.
                //
                // If s[l] == s[r], then we are allowed to process the whole segment [l, r]
                // in one pass. That pass resolves the endpoints l and r together and costs
                // exactly (r - l + 1), the segment length.
                //
                // After paying that cost, the inside substring [l+1, r-1] still needs to be
                // fully decoded. Because the problem allows the inside to be decoded before,
                // after, or split across additional passes, the minimum additional cost is
                // exactly dp[l+1, r-1].
                //
                // So the total cost for this "wrap the interval with matching endpoints"
                // choice is:
                //   (r - l + 1) + dp[l+1, r-1]
                //
                // Special case:
                // If len == 2, then the inside interval is empty, so its cost is 0.
                if (s[l] == s[r])
                {
                    int insideCost = (l + 1 <= r - 1) ? dp[l + 1, r - 1] : 0;
                    int candidate = (r - l + 1) + insideCost;
                    if (candidate < best)
                    {
                        best = candidate;
                    }
                }

                // Store the best answer for this interval.
                dp[l, r] = best;
            }
        }

        // The answer for the whole string is the optimal cost for interval [0, n-1].
        return dp[0, n - 1];
    }
}

// Demo code:
// We create the sample inputs from the statement, run the solution, and print results.

var solution = new Solution();

string s1 = "ABCA";
int result1 = solution.MinimumLatency(s1);
Console.WriteLine($"Input: {s1}");
Console.WriteLine($"Minimum total latency: {result1}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

string s2 = "ABBA";
int result2 = solution.MinimumLatency(s2);
Console.WriteLine($"Input: {s2}");
Console.WriteLine($"Minimum total latency: {result2}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

string s3 = "A";
int result3 = solution.MinimumLatency(s3);
Console.WriteLine($"Input: {s3}");
Console.WriteLine($"Minimum total latency: {result3}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

string s4 = "AAAA";
int result4 = solution.MinimumLatency(s4);
Console.WriteLine($"Input: {s4}");
Console.WriteLine($"Minimum total latency: {result4}");
Console.WriteLine();

string s5 = "ABCBA";
int result5 = solution.MinimumLatency(s5);
Console.WriteLine($"Input: {s5}");
Console.WriteLine($"Minimum total latency: {result5}");