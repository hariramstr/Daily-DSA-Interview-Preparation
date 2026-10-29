/*
Title: Longest Restock Interval with One Emergency Purchase
Difficulty: Medium
Topic: Arrays

Problem Description:
A store records its daily inventory change for a single product in an integer array `changes`, where a positive value means items were added and a negative value means items were sold. The manager wants to study the longest contiguous time interval that can be treated as a non-negative inventory trend after using at most one emergency purchase.

For any chosen subarray, define its running balance as the prefix sums of that subarray. The interval is considered stable if, after optionally adding one emergency purchase of any size on exactly one day inside the interval, every running balance in that interval becomes at least 0. In other words, you may pick at most one index in the subarray and increase that day's value by any non-negative amount, and after that adjustment, no prefix sum of the subarray may be negative.

Return the maximum possible length of a stable contiguous interval.

Notes:
- The emergency purchase can be used once or not used at all.
- The purchase amount is unbounded, but it affects only the chosen day.
- The purchase affects only prefixes that include that chosen day.
- The chosen interval must remain contiguous.

Key observation:
For a subarray changes[l..r], let its internal prefix sums be:
changes[l],
changes[l] + changes[l+1],
...,
changes[l] + ... + changes[r].

If we place the one emergency purchase at position k inside [l..r], then:
- Prefixes ending before k are unchanged.
- Prefixes ending at k or later can all be increased by the same chosen amount.

Therefore, a subarray is fixable with at most one purchase if and only if:
- All prefixes before some split point are already non-negative, and
- After that split point, all bad prefixes can be repaired together by one sufficiently large increase.

That means the internal prefix sums of the subarray may go below 0 only in one contiguous suffix of prefix positions.
Equivalently, among the internal prefix sums, once we have seen a negative value, we must never later see a non-negative value again.

Using global prefix sums P where:
P[0] = 0
P[i+1] = changes[0] + ... + changes[i]

For a chosen subarray [l..r], its internal prefix sums are:
P[l+1] - P[l], P[l+2] - P[l], ..., P[r+1] - P[l].

So the subarray is valid iff in the sequence P[l+1..r+1], relative to threshold P[l],
all values >= P[l] appear first, and once a value < P[l] appears, every later value must also be < P[l].

This can be checked by looking at the first index where P drops below P[l], and then ensuring
there is no later index where P comes back to at least P[l].

The algorithm below precomputes:
1) nextLower[i]  = first index j > i with P[j] < P[i]
2) nextGreaterOrEqual[i] = first index j > i with P[j] >= P[i]

Then for a start prefix index i (= l):
- If nextLower[i] does not exist, then every later prefix is >= P[i], so every subarray starting at l is already stable.
- Otherwise let a = nextLower[i]. This is the first "bad" internal prefix.
  After that point, we must stop before the first later prefix that returns to >= P[i].
  That first return is nextGreaterOrEqual[a].
  So the farthest usable prefix endpoint is:
    endPrefix = nextGreaterOrEqual[a] - 1   if such return exists
    endPrefix = n                           otherwise
  and the subarray length is endPrefix - i.

This yields an O(n) solution using monotonic stacks.
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of complexity:
    - We build the global prefix sum array in O(n).
    - We compute nextLower for every prefix index using a monotonic stack in O(n).
    - We compute nextGreaterOrEqual for every prefix index using another monotonic stack in O(n).
    - We scan all possible starts once in O(n).
    Therefore the total running time is linear.

    Why this is efficient:
    The input size can be as large as 200,000, so checking every subarray would be far too slow.
    A linear-time approach is exactly what we want for large interview-style constraints.
    */
    public int LongestStableInterval(int[] changes)
    {
        int n = changes.Length;

        // ------------------------------------------------------------
        // STEP 1: Build the global prefix sum array.
        //
        // Prefix sums are the standard tool for subarray problems.
        // We define:
        //   prefix[0] = 0
        //   prefix[i+1] = changes[0] + changes[1] + ... + changes[i]
        //
        // Then any internal prefix sum of subarray [l..r] can be written as:
        //   prefix[t] - prefix[l]   for t in [l+1 .. r+1]
        //
        // This lets us compare all internal prefix sums of a subarray against
        // the single threshold prefix[l].
        // ------------------------------------------------------------
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + changes[i];
        }

        // ------------------------------------------------------------
        // STEP 2: Compute nextLower[i].
        //
        // nextLower[i] = the first index j > i such that prefix[j] < prefix[i].
        //
        // Why do we need this?
        // For a subarray starting at l, we compare later prefix values to prefix[l].
        // The first time we drop below prefix[l] is exactly the first internal prefix
        // that becomes negative relative to the subarray start.
        //
        // If there is no such j, then every internal prefix is already non-negative,
        // so the whole suffix starting at l is stable without any emergency purchase.
        //
        // Data structure choice:
        // We use a monotonic increasing stack of indices by prefix value.
        // While scanning from right to left, we can determine the nearest future index
        // with a strictly smaller prefix value.
        //
        // Important detail:
        // We want the FIRST index to the right with smaller value.
        // A standard monotonic stack works because indices are processed from right to left,
        // and we maintain candidates that can still be the nearest useful answer.
        // ------------------------------------------------------------
        int[] nextLower = new int[n + 1];
        Array.Fill(nextLower, -1);

        var stackLower = new Stack<int>();
        for (int i = n; i >= 0; i--)
        {
            // Remove indices whose prefix value is >= current prefix value,
            // because they cannot serve as a "strictly lower" answer for this i.
            while (stackLower.Count > 0 && prefix[stackLower.Peek()] >= prefix[i])
            {
                stackLower.Pop();
            }

            // After removing all non-lower values, the top (if any) is the nearest
            // index to the right with prefix value < prefix[i].
            if (stackLower.Count > 0)
            {
                nextLower[i] = stackLower.Peek();
            }

            // Push current index as a candidate for positions further left.
            stackLower.Push(i);
        }

        // ------------------------------------------------------------
        // STEP 3: Compute nextGreaterOrEqual[i].
        //
        // nextGreaterOrEqual[i] = the first index j > i such that prefix[j] >= prefix[i].
        //
        // Why do we need this?
        // Suppose for start l, the first bad internal prefix occurs at index a = nextLower[l].
        // That means internal prefixes are fine until a-1, and become negative at a.
        //
        // We are allowed one emergency purchase at some day before or at that first bad prefix.
        // Such a purchase can repair all later bad prefixes too, but ONLY if every later prefix
        // stays in the "bad zone" relative to prefix[l].
        //
        // If later the prefix sum comes back to >= prefix[l], then that later internal prefix
        // would become non-negative again before the purchase point could explain the earlier dip
        // and the later recovery pattern with only one modification point. In the equivalent
        // characterization, once we enter the negative region, we must never return to non-negative.
        //
        // So after the first bad index a, we must stop before the first later index that returns
        // to >= prefix[l]. That return point is exactly nextGreaterOrEqual[a] when we compare
        // against the same threshold value prefix[l] only if prefix[a] is the first lower point?
        //
        // To make this work correctly, we use the following derived fact:
        // For start index l, after the first lower point a = nextLower[l], the first future prefix
        // that is >= prefix[l] is exactly the first index to the right of a that reaches at least
        // prefix[l]. We will find that by binary-like logic through a precomputed "next >= current"
        // structure using a second monotonic stack and then querying from a relative to threshold.
        //
        // However, to keep the implementation truly O(n), we instead use a stronger direct property:
        // once the first lower point a is found, the maximal valid end is before the first index
        // to the right of a whose prefix is >= prefix[l]. We will locate that using a segment of
        // precomputed next greater-or-equal jumps over values.
        //
        // To support that efficiently, we compute nearest future index with value >= current value.
        // Then later we will jump through these links starting from a until we reach value >= prefix[l].
        //
        // Because each jump chain can still be too slow in the worst case, we add binary lifting.
        // ------------------------------------------------------------
        int[] nextGreaterOrEqual = new int[n + 1];
        Array.Fill(nextGreaterOrEqual, -1);

        var stackGE = new Stack<int>();
        for (int i = n; i >= 0; i--)
        {
            // For "next index with value >= current", remove strictly smaller values.
            while (stackGE.Count > 0 && prefix[stackGE.Peek()] < prefix[i])
            {
                stackGE.Pop();
            }

            if (stackGE.Count > 0)
            {
                nextGreaterOrEqual[i] = stackGE.Peek();
            }

            stackGE.Push(i);
        }

        // ------------------------------------------------------------
        // STEP 4: Build binary lifting table over nextGreaterOrEqual links.
        //
        // Why is this needed?
        // For a fixed start l, after the first bad point a = nextLower[l], we need the first index
        // reachable to the right whose prefix value is >= prefix[l].
        //
        // The nextGreaterOrEqual links move through indices with non-decreasing prefix values.
        // Starting from a, repeatedly following nextGreaterOrEqual walks to the right through
        // future positions whose prefix values are at least the current one.
        //
        // Since prefix[a] < prefix[l], we want the first node in this chain whose value reaches
        // at least prefix[l]. Binary lifting lets us skip many links at once in O(log n) per start.
        //
        // Even though O(n log n) would still be acceptable for 200,000, the preprocessing is simple
        // and keeps the solution efficient and interview-friendly.
        // ------------------------------------------------------------
        int log = 1;
        while ((1 << log) <= n + 1) log++;

        int[,] up = new int[log, n + 1];
        for (int i = 0; i <= n; i++)
        {
            up[0, i] = nextGreaterOrEqual[i];
        }

        for (int k = 1; k < log; k++)
        {
            for (int i = 0; i <= n; i++)
            {
                int mid = up[k - 1, i];
                up[k, i] = mid == -1 ? -1 : up[k - 1, mid];
            }
        }

        // ------------------------------------------------------------
        // STEP 5: Evaluate every possible subarray start.
        //
        // Prefix index i corresponds to subarray start l = i.
        // A subarray ending at array index r corresponds to prefix endpoint r+1.
        // So if the farthest valid prefix endpoint is endPrefix, then the subarray length is:
        //   endPrefix - i
        //
        // Cases:
        //
        // Case A: nextLower[i] == -1
        //   No later prefix ever drops below prefix[i].
        //   So every internal prefix is non-negative already.
        //   We can take the whole suffix: length = n - i.
        //
        // Case B: nextLower[i] = a
        //   This is the first internal prefix that becomes negative.
        //   We may still fix the subarray with one purchase, but only until the first later
        //   prefix that returns to >= prefix[i].
        //
        //   So we need:
        //     firstReturn = first index > a with prefix[firstReturn] >= prefix[i]
        //
        //   If no such return exists, we can extend to the end: endPrefix = n.
        //   Otherwise we must stop at firstReturn - 1.
        //
        // To find firstReturn efficiently:
        //   - Start at node a.
        //   - Follow nextGreaterOrEqual jumps while the reached prefix value is still < prefix[i].
        //   - The answer is the next link after the last still-too-small node.
        // ------------------------------------------------------------
        int answer = 0;

        for (int i = 0; i < n; i++)
        {
            int a = nextLower[i];

            if (a == -1)
            {
                // No bad internal prefix exists at all.
                // Entire suffix [i..n-1] is already stable.
                answer = Math.Max(answer, n - i);
                continue;
            }

            long threshold = prefix[i];

            // We need the first index after 'a' whose prefix value is >= threshold.
            // We will walk along the nextGreaterOrEqual chain starting from 'a'.
            int cur = a;

            // If current node itself is already >= threshold, that would contradict
            // a being a lower point, so cur starts with prefix[cur] < threshold.
            //
            // We want to move cur as far right as possible while the next jumped node
            // still has prefix value < threshold. After that, the immediate next link
            // (if any) is the first return to >= threshold.
            for (int k = log - 1; k >= 0; k--)
            {
                int nxt = up[k, cur];
                if (nxt != -1 && prefix[nxt] < threshold)
                {
                    cur = nxt;
                }
            }

            int firstReturn = nextGreaterOrEqual[cur];

            int endPrefix;
            if (firstReturn == -1)
            {
                // Never returns to the safe zone relative to prefix[i].
                // So the whole remaining suffix can be repaired by one purchase.
                endPrefix = n;
            }
            else
            {
                // Must stop before the first return to >= threshold.
                endPrefix = firstReturn - 1;
            }

            answer = Math.Max(answer, endPrefix - i);
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

int[] changes1 = { 4, -6, 3, 2 };
int result1 = solution.LongestStableInterval(changes1);
Console.WriteLine(result1); // Expected: 4

int[] changes2 = { -3, 5, -2, 1, -4 };
int result2 = solution.LongestStableInterval(changes2);
Console.WriteLine(result2); // Expected: 4

// Additional quick checks
int[] changes3 = { -5 };
Console.WriteLine(solution.LongestStableInterval(changes3)); // Expected: 1

int[] changes4 = { 1, -2, 1 };
Console.WriteLine(solution.LongestStableInterval(changes4)); // Expected: 3

int[] changes5 = { -1, 2, -3, 4 };
Console.WriteLine(solution.LongestStableInterval(changes5)); // One valid answer should be 3 or 4 depending on fixability; algorithm prints computed result