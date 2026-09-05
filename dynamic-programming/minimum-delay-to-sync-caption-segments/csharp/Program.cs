/*
Title: Minimum Delay to Sync Caption Segments
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A video platform stores an automatically generated caption track as a string s of lowercase English letters,
where each character represents the dominant word category spoken during one second of video.

We want to split the string into contiguous segments.
Each segment length must be between minLen and maxLen inclusive.

For any chosen segment, its delay cost is the minimum number of character changes needed to make
the whole segment consist of one repeated character.

Example:
Segment "abaca" has cost 2, because we can change the two non-'a' characters to 'a'.

The total synchronization delay is the sum of segment costs over all chosen segments.

Goal:
Return the minimum possible total synchronization delay needed to partition the entire string
into valid segments. If it is impossible, return -1.

Constraints:
- 1 <= s.length <= 5000
- s contains only lowercase English letters
- 1 <= minLen <= maxLen <= 100
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Building prefix counts: O(26 * n)
    - Dynamic programming transitions: O(n * (maxLen - minLen + 1) * 26)
    - Since maxLen <= 100 and alphabet size is 26, this is efficient for n <= 5000.

    More simply:
    O(26n + 26n(maxLen - minLen + 1))

    Space Complexity:
    - Prefix counts: O(26 * (n + 1))
    - DP array: O(n + 1)
    */
    public int MinimumSynchronizationDelay(string s, int minLen, int maxLen)
    {
        int n = s.Length;

        // If the string is empty, the cost would be 0.
        // The problem constraints say length >= 1, but this guard makes the method robust.
        if (n == 0)
        {
            return 0;
        }

        // ------------------------------------------------------------
        // STEP 1: Build prefix frequency counts for each of the 26 letters.
        // ------------------------------------------------------------
        //
        // Why do we need this?
        // For any segment s[l..r], we need to know:
        //   cost = segmentLength - (maximum frequency of any single character in that segment)
        //
        // Example:
        //   segment = "abaca", length = 5
        //   counts: a=3, b=1, c=1
        //   max frequency = 3
        //   cost = 5 - 3 = 2
        //
        // To compute counts quickly for many segments, we use prefix sums.
        //
        // prefix[c, i] = number of times character c appears in s[0..i-1]
        //
        // Then frequency of character c in substring s[l..r] is:
        //   prefix[c, r+1] - prefix[c, l]
        //
        // This lets us compute each segment cost in O(26), which is fast enough.
        int[,] prefix = new int[26, n + 1];

        for (int i = 0; i < n; i++)
        {
            // First copy all previous counts forward to the next position.
            for (int c = 0; c < 26; c++)
            {
                prefix[c, i + 1] = prefix[c, i];
            }

            // Then add the current character.
            int currentCharIndex = s[i] - 'a';
            prefix[currentCharIndex, i + 1]++;
        }

        // ------------------------------------------------------------
        // STEP 2: Dynamic Programming definition.
        // ------------------------------------------------------------
        //
        // dp[i] = minimum total cost to partition the first i characters, i.e. s[0..i-1]
        //
        // So:
        //   dp[0] = 0   because an empty prefix needs no cost
        //   dp[i] = min over all valid last segment lengths len:
        //           dp[i - len] + cost of segment s[i-len .. i-1]
        //
        // If no valid partition reaches i, dp[i] stays INF.
        //
        // At the end:
        //   answer = dp[n]
        //   if dp[n] is INF, return -1
        int INF = int.MaxValue / 4;
        int[] dp = new int[n + 1];
        Array.Fill(dp, INF);
        dp[0] = 0;

        // ------------------------------------------------------------
        // STEP 3: Fill the DP table from left to right.
        // ------------------------------------------------------------
        //
        // We compute dp[i] for every prefix length i from 1 to n.
        // For each i, we try every possible segment length len in [minLen, maxLen].
        //
        // If len is valid and dp[i-len] is reachable, then the last segment is:
        //   s[i-len .. i-1]
        //
        // We compute its normalization cost and update dp[i].
        for (int i = 1; i <= n; i++)
        {
            // Try every allowed segment length as the final segment ending at position i-1.
            for (int len = minLen; len <= maxLen; len++)
            {
                int start = i - len;
                int endExclusive = i;

                // If start < 0, then this segment would extend before the beginning of the string.
                // That means this length cannot be used for this i.
                if (start < 0)
                {
                    continue;
                }

                // If dp[start] is INF, then the prefix before this segment cannot be partitioned validly.
                // So this candidate is impossible and we skip it.
                if (dp[start] == INF)
                {
                    continue;
                }

                // ------------------------------------------------------------
                // STEP 3A: Compute the cost of making s[start .. i-1] all one character.
                // ------------------------------------------------------------
                //
                // The best target character is simply the most frequent character in the segment,
                // because we keep all occurrences of that character and change the rest.
                //
                // segmentCost = len - maxFrequencyInSegment
                int maxFrequency = 0;

                for (int c = 0; c < 26; c++)
                {
                    int frequency = prefix[c, endExclusive] - prefix[c, start];
                    if (frequency > maxFrequency)
                    {
                        maxFrequency = frequency;
                    }
                }

                int segmentCost = len - maxFrequency;

                // ------------------------------------------------------------
                // STEP 3B: Transition the DP.
                // ------------------------------------------------------------
                //
                // If we partition the first 'start' characters optimally with cost dp[start],
                // and then add this final segment with cost segmentCost,
                // total candidate cost becomes:
                //   dp[start] + segmentCost
                //
                // We keep the minimum over all valid choices.
                int candidate = dp[start] + segmentCost;
                if (candidate < dp[i])
                {
                    dp[i] = candidate;
                }
            }
        }

        // If dp[n] was never updated, then no valid partition exists.
        return dp[n] == INF ? -1 : dp[n];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string s1 = "abacbc";
int minLen1 = 2;
int maxLen1 = 3;
int result1 = solution.MinimumSynchronizationDelay(s1, minLen1, maxLen1);
Console.WriteLine($"Input: s = \"{s1}\", minLen = {minLen1}, maxLen = {maxLen1}");
Console.WriteLine($"Output: {result1}");
// Expected: 2
// One optimal partition is "aba" + "cbc".
// "aba" -> make all 'a' with 1 change
// "cbc" -> make all 'c' with 1 change
// Total = 2

Console.WriteLine();

// Example 2
string s2 = "aaabbbcc";
int minLen2 = 3;
int maxLen2 = 3;
int result2 = solution.MinimumSynchronizationDelay(s2, minLen2, maxLen2);
Console.WriteLine($"Input: s = \"{s2}\", minLen = {minLen2}, maxLen = {maxLen2}");
Console.WriteLine($"Output: {result2}");
// Expected: -1
// Because with fixed segment length 3, total length 8 cannot be fully partitioned into segments of length 3.

Console.WriteLine();

// Additional demo
string s3 = "aaaaab";
int minLen3 = 2;
int maxLen3 = 4;
int result3 = solution.MinimumSynchronizationDelay(s3, minLen3, maxLen3);
Console.WriteLine($"Input: s = \"{s3}\", minLen = {minLen3}, maxLen = {maxLen3}");
Console.WriteLine($"Output: {result3}");