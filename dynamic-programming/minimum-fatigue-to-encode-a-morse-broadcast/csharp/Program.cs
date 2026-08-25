/*
Title: Minimum Fatigue to Encode a Morse Broadcast

Problem Description:
A rescue team needs to send a long emergency message using a custom telegraph key. The message is represented as a string s consisting only of lowercase English letters. Each letter must be encoded using standard Morse code. For example, a = ".-", b = "-...", c = "-.-.", and so on for all 26 letters.

Pressing the key has a fatigue cost. If two consecutive Morse symbols in the final transmitted stream are the same, the second symbol costs sameCost fatigue; otherwise it costs switchCost fatigue. The very first symbol of the entire transmission always costs startCost, regardless of whether it is . or -.

Before transmitting, you may partition the original string into any number of non-empty contiguous groups. For each group, you are allowed to reverse the order of letters inside that group exactly once or leave it unchanged. After choosing orientations for all groups, concatenate the groups in their original group order and transmit the resulting Morse stream.

Your task is to compute the minimum possible total fatigue.

In other words, you may cut the string into segments, optionally reverse each segment, and then encode the resulting letter order into Morse code. The Morse symbols themselves may not be changed, only the order of letters through segment reversals.

Constraints:
- 1 <= s.length <= 300
- 1 <= startCost, sameCost, switchCost <= 10^6
- s contains only lowercase English letters
- Standard Morse code mapping for the 26 lowercase letters must be used
*/

using System;

public class Solution
{
    private static readonly string[] Morse =
    {
        ".-","-...","-.-.","-..",".","..-.","--.","....","..",
        ".---","-.-",".-..","--","-.","---",".--.","--.-",".-.",
        "...","-","..-","...-",".--","-..-","-.--","--.."
    };

    // Time Complexity: O(n^3)
    // Space Complexity: O(n^2)
    //
    // Why O(n^3)?
    // 1. We precompute information for every substring [l..r], which is O(n^2).
    // 2. For each substring, we may scan through its letters to build Morse-stream properties.
    //    Across all substrings this is O(n^3) in the straightforward beginner-friendly implementation.
    // 3. The final DP over partitions is O(n^3) in the worst case as well:
    //    for each ending position, we try all previous cut positions and both orientations.
    //
    // With n <= 300, this is fully acceptable.
    public long MinimumFatigue(string s, long startCost, long sameCost, long switchCost)
    {
        int n = s.Length;

        // ------------------------------------------------------------
        // STEP 1: Precompute Morse information for each single letter.
        // ------------------------------------------------------------
        //
        // For each character:
        // - its Morse string
        // - its first Morse symbol
        // - its last Morse symbol
        // - the internal fatigue cost of transmitting that letter alone
        //   when its first symbol is NOT charged yet
        //
        // Why "first symbol not charged yet"?
        // Because when we combine letters into a larger stream, the first symbol
        // of a letter is paid based on whether it matches the previous symbol.
        // So for a letter, we separate:
        //   - the cost of entering its first symbol from outside
        //   - the cost of transitions inside the letter after that first symbol
        //
        // Example:
        // Morse for 'b' is "-..."
        // Symbols: -, ., ., .
        // Internal-after-first cost:
        //   '-' -> '.' : switchCost
        //   '.' -> '.' : sameCost
        //   '.' -> '.' : sameCost
        //
        string[] letterMorse = new string[n];
        char[] letterFirst = new char[n];
        char[] letterLast = new char[n];
        long[] letterInternalAfterFirst = new long[n];

        for (int i = 0; i < n; i++)
        {
            string code = Morse[s[i] - 'a'];
            letterMorse[i] = code;
            letterFirst[i] = code[0];
            letterLast[i] = code[^1];

            long internalCost = 0;
            for (int j = 1; j < code.Length; j++)
            {
                internalCost += code[j] == code[j - 1] ? sameCost : switchCost;
            }
            letterInternalAfterFirst[i] = internalCost;
        }

        // ------------------------------------------------------------
        // STEP 2: Precompute substring transmission summaries.
        // ------------------------------------------------------------
        //
        // For every substring s[l..r], we need two versions:
        //   0 = forward order   : s[l], s[l+1], ..., s[r]
        //   1 = reversed order  : s[r], s[r-1], ..., s[l]
        //
        // For each version we store:
        //   first symbol of the whole Morse stream
        //   last symbol of the whole Morse stream
        //   cost of the whole stream EXCLUDING the very first symbol
        //
        // This representation is extremely useful:
        // If a segment is the first segment in the whole transmission,
        // total cost = startCost + segmentCostExcludingFirst
        //
        // If a segment comes after another segment whose last symbol is prevLast,
        // then total added cost =
        //   (prevLast == segmentFirst ? sameCost : switchCost)
        //   + segmentCostExcludingFirst
        //
        // So each segment becomes a compact "block" that can be joined easily.
        //
        char[,,] segFirst = new char[n, n, 2];
        char[,,] segLast = new char[n, n, 2];
        long[,,] segCostExclFirst = new long[n, n, 2];

        // Build forward summaries.
        //
        // We fix a left boundary l, then extend r from l to n-1.
        // This lets us incrementally append one more letter to the right.
        for (int l = 0; l < n; l++)
        {
            bool initialized = false;
            char currentFirst = '\0';
            char currentLast = '\0';
            long currentCostExclFirst = 0;

            for (int r = l; r < n; r++)
            {
                if (!initialized)
                {
                    // Substring of exactly one letter.
                    currentFirst = letterFirst[r];
                    currentLast = letterLast[r];
                    currentCostExclFirst = letterInternalAfterFirst[r];
                    initialized = true;
                }
                else
                {
                    // We append letter r to the existing stream.
                    //
                    // Existing stream ends with currentLast.
                    // New letter starts with letterFirst[r].
                    // So the first symbol of the new letter costs:
                    //   sameCost if equal, otherwise switchCost.
                    //
                    // Then we add the internal transitions inside that letter.
                    currentCostExclFirst += (currentLast == letterFirst[r] ? sameCost : switchCost);
                    currentCostExclFirst += letterInternalAfterFirst[r];
                    currentLast = letterLast[r];
                }

                segFirst[l, r, 0] = currentFirst;
                segLast[l, r, 0] = currentLast;
                segCostExclFirst[l, r, 0] = currentCostExclFirst;
            }
        }

        // Build reversed summaries.
        //
        // For substring [l..r] in reversed order, the letter sequence is:
        // s[r], s[r-1], ..., s[l]
        //
        // We again fix l and extend r, but now the reversed stream starts from r
        // and then prepends earlier letters conceptually. A simpler beginner-friendly
        // way is to directly build it by scanning from r down to l.
        //
        // Since n is only 300, this direct approach is still fast enough.
        for (int l = 0; l < n; l++)
        {
            for (int r = l; r < n; r++)
            {
                bool initialized = false;
                char currentFirst = '\0';
                char currentLast = '\0';
                long currentCostExclFirst = 0;

                for (int k = r; k >= l; k--)
                {
                    if (!initialized)
                    {
                        currentFirst = letterFirst[k];
                        currentLast = letterLast[k];
                        currentCostExclFirst = letterInternalAfterFirst[k];
                        initialized = true;
                    }
                    else
                    {
                        currentCostExclFirst += (currentLast == letterFirst[k] ? sameCost : switchCost);
                        currentCostExclFirst += letterInternalAfterFirst[k];
                        currentLast = letterLast[k];
                    }
                }

                segFirst[l, r, 1] = currentFirst;
                segLast[l, r, 1] = currentLast;
                segCostExclFirst[l, r, 1] = currentCostExclFirst;
            }
        }

        // ------------------------------------------------------------
        // STEP 3: Dynamic Programming over partitions.
        // ------------------------------------------------------------
        //
        // We process the original string from left to right.
        //
        // dp[i, t] = minimum fatigue to transmit some valid transformation of s[0..i]
        //            such that the final Morse symbol of the whole transmitted prefix
        //            is:
        //              t = 0 => '.'
        //              t = 1 => '-'
        //
        // Why track the last Morse symbol?
        // Because when we attach the next segment, the joining cost depends only on:
        //   previous last symbol
        //   next segment first symbol
        //
        // This is the classic DP idea:
        // keep exactly the information needed for future transitions.
        //
        long INF = long.MaxValue / 4;
        long[,] dp = new long[n, 2];

        for (int i = 0; i < n; i++)
        {
            dp[i, 0] = INF;
            dp[i, 1] = INF;
        }

        // We try every possible segment [l..r] as the LAST segment of prefix ending at r.
        for (int r = 0; r < n; r++)
        {
            for (int l = 0; l <= r; l++)
            {
                for (int orient = 0; orient < 2; orient++)
                {
                    char firstSym = segFirst[l, r, orient];
                    char lastSym = segLast[l, r, orient];
                    long blockCostExclFirst = segCostExclFirst[l, r, orient];
                    int lastIndex = lastSym == '.' ? 0 : 1;

                    if (l == 0)
                    {
                        // This segment is the first segment of the entire transmission.
                        //
                        // The very first Morse symbol of the whole message costs startCost.
                        // Everything else inside the segment is already represented by
                        // blockCostExclFirst.
                        long total = startCost + blockCostExclFirst;
                        if (total < dp[r, lastIndex])
                        {
                            dp[r, lastIndex] = total;
                        }
                    }
                    else
                    {
                        // There is a previous prefix s[0..l-1].
                        // We try both possibilities for the previous final symbol.
                        for (int prevLastIndex = 0; prevLastIndex < 2; prevLastIndex++)
                        {
                            long prevCost = dp[l - 1, prevLastIndex];
                            if (prevCost >= INF) continue;

                            char prevLastSym = prevLastIndex == 0 ? '.' : '-';

                            // Cost to enter the first symbol of this segment from the previous segment.
                            long joinCost = prevLastSym == firstSym ? sameCost : switchCost;

                            long total = prevCost + joinCost + blockCostExclFirst;
                            if (total < dp[r, lastIndex])
                            {
                                dp[r, lastIndex] = total;
                            }
                        }
                    }
                }
            }
        }

        return Math.Min(dp[n - 1, 0], dp[n - 1, 1]);
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

string s1 = "cab";
long startCost1 = 3;
long sameCost1 = 1;
long switchCost1 = 4;
long answer1 = solution.MinimumFatigue(s1, startCost1, sameCost1, switchCost1);
Console.WriteLine(answer1);

string s2 = "azaz";
long startCost2 = 2;
long sameCost2 = 5;
long switchCost2 = 1;
long answer2 = solution.MinimumFatigue(s2, startCost2, sameCost2, switchCost2);
Console.WriteLine(answer2);