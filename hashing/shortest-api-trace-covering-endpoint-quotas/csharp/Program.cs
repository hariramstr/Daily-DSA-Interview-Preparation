/*
Title: Shortest API Trace Covering Endpoint Quotas

Problem Description:
You are given an API trace represented by an array `trace`, where `trace[i]` is the endpoint name called at time `i`.
You are also given a list of required endpoint quotas as pairs `(endpoint, count)`, meaning a valid incident window
must contain that endpoint at least `count` times.

Return the length of the shortest contiguous subarray of `trace` that satisfies all required quotas.
If no such subarray exists, return `-1`.

Unlike a simple coverage problem, the trace can be very large, endpoint names are arbitrary strings, and the quota list
may contain repeated endpoint requirements that should be combined. Your solution should be efficient enough for
production-scale logs.

Formally, if `need[x]` is the required number of occurrences of endpoint `x`, then a window `[l, r]` is valid if for
every required endpoint `x`, the number of indices `i` in `[l, r]` with `trace[i] == x` is at least `need[x]`.

Constraints:
- 1 <= trace.length <= 2 * 10^5
- 1 <= quotas.length <= 2 * 10^5
- trace[i] and endpoint names in `quotas` are non-empty strings of lowercase English letters, digits, `_`, or `/`
- The sum of all endpoint name lengths across input is at most 10^6
- Quotas may contain duplicate endpoint names; they should be added together

Examples:
1)
trace = ["/login","/feed","/cart","/login","/feed","/pay"]
quotas = [["/login",2],["/feed",1],["/pay",1]]
Output: 6

2)
trace = ["a","x","b","a","c","b","a"]
quotas = [["a",2],["b",1]]
Output: 4
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the required quota map: O(q), where q = number of quota pairs
    - Sliding window over the trace: O(n), where n = trace.Length
    - Overall: O(n + q) average time using hash tables

    Space Complexity:
    - O(k), where k = number of distinct required endpoints
    - We store:
      1) the combined required counts
      2) the current counts inside the sliding window
    */
    public int ShortestTraceCoveringQuotas(string[] trace, string[][] quotas)
    {
        // Step 1:
        // Build a dictionary named "need" that stores the total required count for each endpoint.
        //
        // Why this is necessary:
        // The input quota list may contain duplicate endpoint names. For example:
        // quotas = [["a", 1], ["b", 2], ["a", 3]]
        // This really means:
        // need["a"] = 4
        // need["b"] = 2
        //
        // A dictionary is the natural data structure here because:
        // - endpoint names are strings
        // - we need fast average O(1) lookup by endpoint name
        var need = new Dictionary<string, int>(StringComparer.Ordinal);

        foreach (var pair in quotas)
        {
            // Each quota pair is expected to be:
            // pair[0] = endpoint name
            // pair[1] = required count as string
            string endpoint = pair[0];
            int count = int.Parse(pair[1]);

            if (need.ContainsKey(endpoint))
            {
                need[endpoint] += count;
            }
            else
            {
                need[endpoint] = count;
            }
        }

        // Step 2:
        // If there are no required endpoints after combining, then the shortest valid window would be 0.
        // However, by problem constraints quotas.length >= 1, so this is mostly defensive programming.
        if (need.Count == 0)
        {
            return 0;
        }

        // Step 3:
        // Create another dictionary to track how many times each required endpoint appears
        // inside the current sliding window [left, right].
        //
        // Important detail:
        // We only care about endpoints that appear in "need".
        // If trace contains many unrelated endpoints, we do not need to store counts for them.
        var window = new Dictionary<string, int>(StringComparer.Ordinal);

        // Step 4:
        // "requiredKinds" = how many distinct endpoint names must meet their quota.
        // Example:
        // need = { "a": 2, "b": 1, "c": 5 }
        // Then requiredKinds = 3
        int requiredKinds = need.Count;

        // Step 5:
        // "formedKinds" = how many distinct endpoint names currently satisfy their required quota
        // inside the current window.
        //
        // Example:
        // need = { "a": 2, "b": 1 }
        // If current window has:
        // a -> 2, b -> 0
        // then formedKinds = 1 because only "a" is satisfied.
        int formedKinds = 0;

        // Step 6:
        // Standard sliding window pointers.
        // "left" is the start of the current window.
        int left = 0;

        // Step 7:
        // Track the best (smallest) valid window length found so far.
        // We initialize to int.MaxValue to mean "not found yet".
        int bestLength = int.MaxValue;

        // Step 8:
        // Expand the window by moving "right" from left to right across the trace.
        for (int right = 0; right < trace.Length; right++)
        {
            string currentEndpoint = trace[right];

            // Step 8a:
            // Only process this endpoint if it is actually required.
            //
            // Why:
            // Endpoints not present in "need" do not help satisfy quotas.
            // They can still be inside the window, but we do not need to count them.
            if (need.ContainsKey(currentEndpoint))
            {
                // Increase the count of this endpoint in the current window.
                if (window.ContainsKey(currentEndpoint))
                {
                    window[currentEndpoint]++;
                }
                else
                {
                    window[currentEndpoint] = 1;
                }

                // Step 8b:
                // If after adding this endpoint, its count exactly reaches the required quota,
                // then one more required endpoint kind has become satisfied.
                //
                // We use "==" here, not ">=":
                // - When count goes from need-1 to need, we newly satisfy it.
                // - When count goes from need to need+1, it was already satisfied before,
                //   so formedKinds should not increase again.
                if (window[currentEndpoint] == need[currentEndpoint])
                {
                    formedKinds++;
                }
            }

            // Step 9:
            // If all required endpoint kinds are satisfied, then the current window [left, right] is valid.
            //
            // While it remains valid, we try to shrink it from the left to make it as short as possible.
            //
            // This is the heart of the sliding window technique:
            // - expand right until valid
            // - shrink left while still valid
            // This guarantees near-linear time because each pointer moves at most n times.
            while (formedKinds == requiredKinds)
            {
                // Current window length
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // We are about to remove trace[left] from the window,
                // so first identify that endpoint.
                string leftEndpoint = trace[left];

                // Only required endpoints affect validity tracking.
                if (need.ContainsKey(leftEndpoint))
                {
                    // Decrease its count in the current window because left is moving forward.
                    window[leftEndpoint]--;

                    // If the count drops below the required quota,
                    // then the window is no longer valid after this removal.
                    //
                    // Example:
                    // need["a"] = 2
                    // window["a"] was 2, then after decrement becomes 1
                    // So "a" is no longer satisfied.
                    if (window[leftEndpoint] < need[leftEndpoint])
                    {
                        formedKinds--;
                    }
                }

                // Finally move the left boundary forward by one position.
                left++;
            }
        }

        // Step 10:
        // If bestLength was never updated, then no valid window exists.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] trace1 =
{
    "/login", "/feed", "/cart", "/login", "/feed", "/pay"
};

string[][] quotas1 =
{
    new[] { "/login", "2" },
    new[] { "/feed", "1" },
    new[] { "/pay", "1" }
};

int result1 = solution.ShortestTraceCoveringQuotas(trace1, quotas1);
Console.WriteLine(result1); // Expected: 6

// Example 2
string[] trace2 =
{
    "a", "x", "b", "a", "c", "b", "a"
};

string[][] quotas2 =
{
    new[] { "a", "2" },
    new[] { "b", "1" }
};

int result2 = solution.ShortestTraceCoveringQuotas(trace2, quotas2);
Console.WriteLine(result2); // Expected: 4

// Extra demo: duplicate quotas should be combined
string[] trace3 =
{
    "svc1", "svc2", "svc1", "svc3", "svc2", "svc1"
};

string[][] quotas3 =
{
    new[] { "svc1", "1" },
    new[] { "svc2", "1" },
    new[] { "svc1", "1" } // Combined need for svc1 becomes 2
};

int result3 = solution.ShortestTraceCoveringQuotas(trace3, quotas3);
Console.WriteLine(result3); // Expected: 3 (["svc2","svc1","svc3"] is not valid; shortest valid is ["svc1","svc2","svc1"] or length 3)

// Extra demo: impossible case
string[] trace4 =
{
    "a", "b", "c"
};

string[][] quotas4 =
{
    new[] { "a", "1" },
    new[] { "d", "1" }
};

int result4 = solution.ShortestTraceCoveringQuotas(trace4, quotas4);
Console.WriteLine(result4); // Expected: -1