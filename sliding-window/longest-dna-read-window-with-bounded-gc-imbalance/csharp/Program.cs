/*
Title: Longest DNA Read Window With Bounded GC Imbalance
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are analyzing a long DNA read represented by a string s consisting only of the characters 'A', 'C', 'G', and 'T'.
In genome quality control, a contiguous segment is considered stable if both of the following conditions hold:

1. The absolute difference between the number of 'G' bases and the number of 'C' bases in the segment is at most k.
2. The total number of 'A' and 'T' bases in the segment is at most m.

Return the length of the longest stable contiguous segment.

A segment may be empty, but the answer should be the maximum length among all contiguous non-empty segments unless
no valid non-empty segment exists, in which case return 0.

Constraints:
- 1 <= s.length <= 2 * 10^5
- 0 <= k <= s.length
- 0 <= m <= s.length
- s[i] is one of 'A', 'C', 'G', 'T'

Example 1:
Input: s = "GCGATCGG", k = 1, m = 2
Output: 6
Explanation: The substring "CGATCG" has G = 2, C = 2, so |G - C| = 0 <= 1, and A + T = 2 <= 2.
Its length is 6. No longer valid substring exists.

Example 2:
Input: s = "ATATGGCCG", k = 0, m = 1
Output: 5
Explanation: The substring "TGGCC" is valid because G = 2, C = 2, so |G - C| = 0, and it contains only one base
from {A, T}. Its length is 5. Longer windows violate at least one constraint.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n log n), where n is the length of the string.

    Why:
    - We binary search on the answer length from 1 to n.
    - For each candidate length L, we scan all windows of length L in O(n).
    - Therefore total complexity is O(n log n).

    Space Complexity:
    O(1)

    Why:
    - We only store a few counters for the current sliding window.
    - No extra arrays proportional to input size are needed.

    Important note for learners:
    --------------------------------
    A classic "expand left/right and shrink while invalid" sliding window works only when the validity condition is
    monotonic with respect to shrinking the window. Here that is NOT true because of the condition |G - C| <= k.

    Example:
    - A window may be invalid because G - C is too large.
    - Removing a 'C' from the left can make the imbalance even worse.
    - So simply shrinking until valid does not guarantee correctness.

    Because of that, we use:
    1. Binary search on the answer length
    2. A fixed-size sliding window check

    This is still a sliding window solution, but in a more advanced and fully correct form.
    */
    public int LongestStableSegment(string s, int k, int m)
    {
        // If the string is empty, there is no non-empty valid segment.
        // The constraints say length >= 1, but this guard makes the method more robust.
        if (string.IsNullOrEmpty(s))
        {
            return 0;
        }

        int n = s.Length;

        // We binary search for the largest length L such that
        // there exists at least one valid window of length L.
        //
        // Why binary search is allowed:
        // If there exists a valid window of length L,
        // then every smaller length is also achievable:
        // take that valid window and remove characters from one or both ends.
        // Removing characters cannot increase:
        // - the number of A/T characters
        // - the absolute imbalance |G - C| beyond what it already was
        //
        // Therefore, "is there a valid window of length L?" is monotonic:
        // true for small enough L, false for large enough L.
        int left = 1;
        int right = n;
        int best = 0;

        while (left <= right)
        {
            // Standard binary search midpoint.
            int mid = left + (right - left) / 2;

            // Check whether any substring of exact length mid is valid.
            if (ExistsValidWindowOfLength(s, mid, k, m))
            {
                // If yes, mid is feasible.
                // We record it and try to find an even larger valid length.
                best = mid;
                left = mid + 1;
            }
            else
            {
                // If no window of length mid is valid,
                // then no larger length can be valid either.
                right = mid - 1;
            }
        }

        return best;
    }

    private bool ExistsValidWindowOfLength(string s, int windowLength, int k, int m)
    {
        // This helper checks all windows of one fixed size.
        //
        // We maintain counts for the current window:
        // - gCount = number of 'G'
        // - cCount = number of 'C'
        // - atCount = number of 'A' or 'T'
        //
        // Then a window is valid if:
        // - Math.Abs(gCount - cCount) <= k
        // - atCount <= m
        //
        // Since the window size is fixed, we can slide it in O(1) per move:
        // - add the new right character
        // - remove the old left character

        int n = s.Length;

        // Safety guard. In our binary search this should not happen,
        // but it makes the helper self-contained.
        if (windowLength <= 0 || windowLength > n)
        {
            return false;
        }

        int gCount = 0;
        int cCount = 0;
        int atCount = 0;

        // Step 1:
        // Build the very first window: s[0 .. windowLength - 1]
        //
        // We count how many G, C, and A/T characters it contains.
        for (int i = 0; i < windowLength; i++)
        {
            AddChar(s[i], ref gCount, ref cCount, ref atCount);
        }

        // After building the first window, immediately test whether it is valid.
        if (IsValid(gCount, cCount, atCount, k, m))
        {
            return true;
        }

        // Step 2:
        // Slide the fixed-size window one position at a time.
        //
        // For each new position:
        // - remove the character that leaves from the left
        // - add the character that enters from the right
        //
        // Window ending at index 'right' starts at index 'right - windowLength + 1'.
        for (int right = windowLength; right < n; right++)
        {
            int left = right - windowLength;

            // Remove the old leftmost character because it is no longer inside the window.
            RemoveChar(s[left], ref gCount, ref cCount, ref atCount);

            // Add the new rightmost character because it has just entered the window.
            AddChar(s[right], ref gCount, ref cCount, ref atCount);

            // Check whether this new window satisfies both problem constraints.
            if (IsValid(gCount, cCount, atCount, k, m))
            {
                return true;
            }
        }

        // If we checked every window of this length and none were valid,
        // then this length is not feasible.
        return false;
    }

    private static bool IsValid(int gCount, int cCount, int atCount, int k, int m)
    {
        // A window is valid exactly when both constraints hold:
        // 1. GC imbalance is bounded by k
        // 2. total A/T count is bounded by m
        return Math.Abs(gCount - cCount) <= k && atCount <= m;
    }

    private static void AddChar(char ch, ref int gCount, ref int cCount, ref int atCount)
    {
        // This helper updates the counters when a character enters the window.
        //
        // We use simple integer counters because:
        // - there are only four possible characters
        // - this is faster and simpler than using a dictionary
        switch (ch)
        {
            case 'G':
                gCount++;
                break;
            case 'C':
                cCount++;
                break;
            case 'A':
            case 'T':
                atCount++;
                break;
        }
    }

    private static void RemoveChar(char ch, ref int gCount, ref int cCount, ref int atCount)
    {
        // This helper updates the counters when a character leaves the window.
        // It is the exact reverse of AddChar.
        switch (ch)
        {
            case 'G':
                gCount--;
                break;
            case 'C':
                cCount--;
                break;
            case 'A':
            case 'T':
                atCount--;
                break;
        }
    }
}

// Demo code
var solution = new Solution();

// Example 1
string s1 = "GCGATCGG";
int k1 = 1;
int m1 = 2;
int result1 = solution.LongestStableSegment(s1, k1, m1);
Console.WriteLine($"Input: s = \"{s1}\", k = {k1}, m = {m1}");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Example 2
string s2 = "ATATGGCCG";
int k2 = 0;
int m2 = 1;
int result2 = solution.LongestStableSegment(s2, k2, m2);
Console.WriteLine($"Input: s = \"{s2}\", k = {k2}, m = {m2}");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

// Additional demo cases
string s3 = "GGGG";
int k3 = 4;
int m3 = 0;
int result3 = solution.LongestStableSegment(s3, k3, m3);
Console.WriteLine($"Input: s = \"{s3}\", k = {k3}, m = {m3}");
Console.WriteLine($"Output: {result3}");
Console.WriteLine();

string s4 = "ATAT";
int k4 = 0;
int m4 = 0;
int result4 = solution.LongestStableSegment(s4, k4, m4);
Console.WriteLine($"Input: s = \"{s4}\", k = {k4}, m = {m4}");
Console.WriteLine($"Output: {result4}");
Console.WriteLine();

string s5 = "CGCGCG";
int k5 = 0;
int m5 = 0;
int result5 = solution.LongestStableSegment(s5, k5, m5);
Console.WriteLine($"Input: s = \"{s5}\", k = {k5}, m = {m5}");
Console.WriteLine($"Output: {result5}");