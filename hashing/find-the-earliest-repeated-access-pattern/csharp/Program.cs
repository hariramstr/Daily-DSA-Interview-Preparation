/*
Title: Find the Earliest Repeated Access Pattern

Problem Description:
A security system records each employee's building access events as an array of lowercase strings,
where each string is a room code visited in order during one day. You are also given an integer
windowSize. For every contiguous block of exactly windowSize room codes, define its access pattern
as the ordered sequence of those room codes. Your task is to return the starting index of the
earliest window whose exact access pattern appears again later in the array. If multiple windows
repeat, choose the one with the smallest starting index. If no length-windowSize pattern appears
at least twice, return -1.

Two windows are considered the same only if they have the same length and every position contains
the same room code. Overlapping windows are allowed. For example, with windowSize = 3, the windows
starting at indices 1 and 3 may match even if they overlap.

Design an efficient solution using hashing so that large inputs can be processed quickly. A naive
comparison of every pair of windows will be too slow.

Constraints:
- 1 <= accessLog.length <= 100000
- 1 <= windowSize <= accessLog.length
- accessLog[i] consists of lowercase English letters
- The total number of characters across all room codes is at most 200000
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
        Time Complexity:
        - O(n), where n is accessLog.Length
          Explanation:
          1. We assign each distinct room code a small integer id in O(n) average time using a dictionary.
          2. We compute prefix hashes and powers in O(n).
          3. We evaluate every window exactly once in O(1) per window using rolling hash math.
          Overall this is linear.

        Space Complexity:
        - O(n)
          Explanation:
          We store:
          1. The integer id for each room code
          2. Prefix hash arrays
          3. Power arrays
          4. A dictionary from window hash to earliest starting index
    */
    public int FindEarliestRepeatedAccessPattern(string[] accessLog, int windowSize)
    {
        int n = accessLog.Length;

        // If the window size is larger than the array, no valid window exists.
        // The constraints say windowSize <= accessLog.Length, but this guard makes the method safer.
        if (windowSize > n)
        {
            return -1;
        }

        // ------------------------------------------------------------
        // STEP 1: Convert each room code string into a compact integer id.
        // ------------------------------------------------------------
        // Why do this?
        // Working directly with strings inside every window hash would be slower and more memory-heavy.
        // By mapping each unique string to an integer, we can build a fast rolling hash over integers.
        //
        // Example:
        // ["lab","hall","vault","lab"] might become [1,2,3,1]
        //
        // Data structure choice:
        // Dictionary<string, int> gives average O(1) insert/lookup time.
        var idMap = new Dictionary<string, int>();
        int[] ids = new int[n];
        int nextId = 1;

        for (int i = 0; i < n; i++)
        {
            string room = accessLog[i];

            // If this room code has not been seen before, assign it a new integer id.
            if (!idMap.TryGetValue(room, out int id))
            {
                id = nextId++;
                idMap[room] = id;
            }

            ids[i] = id;
        }

        // ------------------------------------------------------------
        // STEP 2: Build double rolling hashes for the entire integer array.
        // ------------------------------------------------------------
        // Why double hashing?
        // A single hash can theoretically collide: two different windows might produce the same hash.
        // Using two independent hashes makes collisions extremely unlikely in practice.
        //
        // We will compute prefix hashes so any window hash can be extracted in O(1).
        //
        // Hash formula for sequence a[0..i-1]:
        // prefix[i] = prefix[i-1] * Base + value
        //
        // Then hash of subarray [l..r] can be extracted using:
        // hash(l, r) = prefix[r+1] - prefix[l] * power[r-l+1]
        //
        // We use ulong arithmetic intentionally.
        // Unsigned integer overflow in C# wraps around in unchecked context,
        // which effectively gives us modulo 2^64 behavior and is very fast.
        const ulong Base1 = 911382323UL;
        const ulong Base2 = 972663749UL;

        ulong[] prefix1 = new ulong[n + 1];
        ulong[] prefix2 = new ulong[n + 1];
        ulong[] power1 = new ulong[n + 1];
        ulong[] power2 = new ulong[n + 1];

        // The power of base^0 is 1.
        power1[0] = 1;
        power2[0] = 1;

        for (int i = 0; i < n; i++)
        {
            power1[i + 1] = power1[i] * Base1;
            power2[i + 1] = power2[i] * Base2;

            // Add 1 to the id before hashing.
            // This is not strictly required because ids already start at 1,
            // but keeping values away from zero is a common hashing habit.
            ulong value = (ulong)ids[i] + 1UL;

            prefix1[i + 1] = prefix1[i] * Base1 + value;
            prefix2[i + 1] = prefix2[i] * Base2 + value;
        }

        // ------------------------------------------------------------
        // STEP 3: Scan every window of length windowSize and remember
        //         the earliest index where each exact pattern hash appears.
        // ------------------------------------------------------------
        // Number of windows = n - windowSize + 1
        //
        // For each window:
        // 1. Compute its double hash in O(1)
        // 2. If we have seen this hash before, then the earlier index and current index
        //    represent the same access pattern (with extremely high probability).
        // 3. We want the smallest starting index whose pattern repeats later.
        //    Therefore:
        //    - When a repeat is found, candidate answer is the earliest stored index.
        //    - Keep the minimum such index across all repeats.
        //
        // Data structure choice:
        // Dictionary<(ulong, ulong), int> maps a window hash to the earliest starting index
        // where that pattern was first seen.
        var firstSeen = new Dictionary<(ulong, ulong), int>();
        int answer = int.MaxValue;

        int lastStart = n - windowSize;

        for (int start = 0; start <= lastStart; start++)
        {
            int endExclusive = start + windowSize;

            // Extract hash for window [start, endExclusive - 1].
            ulong windowHash1 = prefix1[endExclusive] - prefix1[start] * power1[windowSize];
            ulong windowHash2 = prefix2[endExclusive] - prefix2[start] * power2[windowSize];

            var key = (windowHash1, windowHash2);

            // If this exact pattern hash has appeared before,
            // then the earlier window repeats at the current position.
            if (firstSeen.TryGetValue(key, out int earliestIndexForThisPattern))
            {
                // We need the globally earliest starting index among all repeating patterns.
                if (earliestIndexForThisPattern < answer)
                {
                    answer = earliestIndexForThisPattern;
                }
            }
            else
            {
                // Store only the first occurrence.
                // This is important because the problem asks for the earliest starting index.
                firstSeen[key] = start;
            }
        }

        // If no repeated pattern was found, return -1.
        return answer == int.MaxValue ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string[] accessLog1 = { "lab", "hall", "vault", "lab", "hall", "vault", "exit" };
int windowSize1 = 3;
int result1 = solution.FindEarliestRepeatedAccessPattern(accessLog1, windowSize1);
Console.WriteLine(result1); // Expected: 0

// Example 2
string[] accessLog2 = { "a", "b", "a", "b", "c" };
int windowSize2 = 2;
int result2 = solution.FindEarliestRepeatedAccessPattern(accessLog2, windowSize2);
Console.WriteLine(result2); // Expected: 0

// Additional demo: no repeated window
string[] accessLog3 = { "x", "y", "z", "w" };
int windowSize3 = 2;
int result3 = solution.FindEarliestRepeatedAccessPattern(accessLog3, windowSize3);
Console.WriteLine(result3); // Expected: -1