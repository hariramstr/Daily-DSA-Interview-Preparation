/*
Title: Shortest Unique Signature Segment

Problem Description:
You are given an array of lowercase strings `events`, where `events[i]` is the event code recorded at time `i`.
A contiguous segment `events[l...r]` is called a unique signature segment for index `i` if `l <= i <= r` and
the multiset of strings inside that segment does not appear as the multiset of any other contiguous segment of
the same length in the entire array. Order does not matter when comparing two segments; only the frequency of
each string inside the segment matters.

For every index `i`, compute the minimum possible length of a contiguous segment containing `i` that is a unique
signature segment. If no such segment exists, return `-1` for that index.

Two segments of equal length are considered identical if every event code appears the same number of times in both
segments. For example, `["login", "pay", "login"]` and `["pay", "login", "login"]` are identical because both
contain `login` twice and `pay` once.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= events[i].length <= 20
- events[i] consists only of lowercase English letters
- The sum of all string lengths does not exceed 4 * 10^5

Key idea:
A window is identified only by the frequency of values inside it, not by order.
So for a fixed length L, every window can be represented by a frequency signature.
We need to know which windows of length L have a signature that appears exactly once.
Then every index covered by such a window can potentially get answer L if it is the smallest so far.

Efficient strategy used here:
1. Compress strings to integer ids.
2. Assign each distinct id two independent random 64-bit weights.
3. For a window, its multiset signature is the pair of sums of weights of its elements.
   Because addition ignores order, equal multisets produce equal sums.
4. For a fixed length L:
   - compute all window signatures in O(n)
   - count occurrences of each signature
   - windows whose signature count is 1 are unique
   - mark coverage of indices by those unique windows using a difference array
5. To find the minimum length for every index efficiently, process lengths in increasing order.
   But checking all lengths 1..n would be too slow.
6. We use divide-and-conquer on answer ranges:
   - For a group of indices whose answer is still unknown within [lo, hi],
     test the middle length mid.
   - If an index is covered by some unique window of length mid, then its answer is <= mid.
     Otherwise its answer is > mid.
   - Recurse on the two halves.
   This reduces the number of tested lengths to O(log n) layers, and each tested length is processed once.

This is a Las Vegas / Monte Carlo style hashing solution:
Using two independent 64-bit sums makes collision probability negligibly small in practice.
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    private readonly Dictionary<int, bool[]> _coverageCache = new();

    /*
    Time Complexity:
    - Let n be the number of events.
    - For each tested length L, we process all windows of that length in O(n).
    - The divide-and-conquer over answer ranges tests O(log n) layers, and each distinct length is computed at most once.
    - Total expected time: O(n log n)

    Space Complexity:
    - O(n) for compressed ids, temporary arrays, and cached coverage arrays for tested lengths.
    - Additional O(k) for distinct event ids and hash maps, where k <= n.
    */
    public int[] ShortestUniqueSignatureSegment(string[] events)
    {
        int n = events.Length;

        // Step 1:
        // Convert strings into compact integer ids.
        // Why?
        // Working with integers is much faster than repeatedly hashing strings while sliding windows.
        // Also, we only care whether two event codes are equal, so integer compression is perfect here.
        int[] ids = Compress(events, out int distinctCount);

        // Step 2:
        // Give every distinct event id two random-looking 64-bit weights.
        // Why two?
        // A single 64-bit additive hash is already strong, but using two independent sums makes accidental
        // collisions astronomically unlikely.
        ulong[] w1 = new ulong[distinctCount];
        ulong[] w2 = new ulong[distinctCount];
        FillDeterministicRandomWeights(w1, w2);

        // Step 3:
        // Prepare answer array. We will fill it with the minimum valid length for each index.
        // Start with -1 meaning "not found yet".
        int[] answer = Enumerable.Repeat(-1, n).ToArray();

        // Step 4:
        // We solve all indices together using divide-and-conquer on the answer length.
        // Initially every index could have answer in [1, n].
        int[] allIndices = Enumerable.Range(0, n).ToArray();
        SolveRange(ids, w1, w2, allIndices, 1, n, answer);

        return answer;
    }

    private void SolveRange(int[] ids, ulong[] w1, ulong[] w2, int[] indices, int lo, int hi, int[] answer)
    {
        // If there are no indices in this subproblem, there is nothing to do.
        if (indices.Length == 0) return;

        // Base case:
        // If lo == hi, then every index in this group has only one possible answer length left.
        // We still must verify that such a unique window actually exists for that length.
        // In our recursion logic, indices only reach here if they were always routed consistently,
        // but for safety and clarity we directly assign lo.
        if (lo == hi)
        {
            bool[] covered = GetCoverage(ids, w1, w2, lo);
            foreach (int idx in indices)
            {
                answer[idx] = covered[idx] ? lo : -1;
            }
            return;
        }

        // Choose the middle length.
        int mid = lo + (hi - lo) / 2;

        // Compute which indices are covered by at least one unique window of length mid.
        // This is the key query used to split the search space.
        bool[] coverage = GetCoverage(ids, w1, w2, mid);

        // Partition indices:
        // - left group: answer <= mid, because index is covered by some unique window of length mid
        // - right group: answer > mid, because no unique window of length mid covers it
        List<int> left = new(indices.Length);
        List<int> right = new(indices.Length);

        foreach (int idx in indices)
        {
            if (coverage[idx]) left.Add(idx);
            else right.Add(idx);
        }

        // Recurse on the smaller answer half first.
        SolveRange(ids, w1, w2, left.ToArray(), lo, mid, answer);
        SolveRange(ids, w1, w2, right.ToArray(), mid + 1, hi, answer);
    }

    private bool[] GetCoverage(int[] ids, ulong[] w1, ulong[] w2, int len)
    {
        // Memoization:
        // If we already computed coverage for this length earlier in recursion, reuse it.
        // This avoids repeated O(n) work for the same length.
        if (_coverageCache.TryGetValue(len, out var cached))
            return cached;

        int n = ids.Length;
        bool[] covered = new bool[n];

        // If the requested window length is invalid, no index can be covered.
        if (len <= 0 || len > n)
        {
            _coverageCache[len] = covered;
            return covered;
        }

        int windowCount = n - len + 1;

        // Step A:
        // Compute the additive multiset hash for every window of this fixed length.
        // Because the signature depends only on frequencies, summing per-element weights works:
        // if two windows contain the same multiset, their sums are identical regardless of order.
        Sig[] sigs = new Sig[windowCount];

        ulong sum1 = 0;
        ulong sum2 = 0;

        // Build the first window [0 .. len-1].
        for (int i = 0; i < len; i++)
        {
            int id = ids[i];
            sum1 += w1[id];
            sum2 += w2[id];
        }
        sigs[0] = new Sig(sum1, sum2);

        // Slide the window one step at a time.
        // Remove the outgoing element's weights and add the incoming element's weights.
        for (int start = 1; start < windowCount; start++)
        {
            int outId = ids[start - 1];
            int inId = ids[start + len - 1];

            sum1 = sum1 - w1[outId] + w1[inId];
            sum2 = sum2 - w2[outId] + w2[inId];

            sigs[start] = new Sig(sum1, sum2);
        }

        // Step B:
        // Count how many times each signature appears among all windows of this length.
        // A window is unique exactly when its signature appears once.
        Dictionary<Sig, int> freq = new(windowCount * 2);
        for (int i = 0; i < windowCount; i++)
        {
            freq.TryGetValue(sigs[i], out int count);
            freq[sigs[i]] = count + 1;
        }

        // Step C:
        // We need to mark every index that belongs to at least one unique window.
        // Doing this naively by touching all len positions of every unique window would be too slow.
        // Instead, use a difference array:
        // for a unique window [l, r], do diff[l]++, diff[r+1]--.
        // After prefix sum, positions with positive value are covered.
        int[] diff = new int[n + 1];

        for (int start = 0; start < windowCount; start++)
        {
            if (freq[sigs[start]] == 1)
            {
                int l = start;
                int r = start + len - 1;
                diff[l]++;
                if (r + 1 < diff.Length) diff[r + 1]--;
            }
        }

        // Step D:
        // Convert difference array into actual coverage booleans.
        int active = 0;
        for (int i = 0; i < n; i++)
        {
            active += diff[i];
            covered[i] = active > 0;
        }

        _coverageCache[len] = covered;
        return covered;
    }

    private static int[] Compress(string[] events, out int distinctCount)
    {
        Dictionary<string, int> map = new(events.Length);
        int[] ids = new int[events.Length];
        int next = 0;

        for (int i = 0; i < events.Length; i++)
        {
            if (!map.TryGetValue(events[i], out int id))
            {
                id = next++;
                map[events[i]] = id;
            }
            ids[i] = id;
        }

        distinctCount = next;
        return ids;
    }

    private static void FillDeterministicRandomWeights(ulong[] w1, ulong[] w2)
    {
        // We use a deterministic SplitMix64 generator so the demo is reproducible.
        ulong seed = 0x9E3779B97F4A7C15UL;

        for (int i = 0; i < w1.Length; i++)
        {
            w1[i] = NextSplitMix64(ref seed);
            w2[i] = NextSplitMix64(ref seed);

            // Avoid zero weights just to keep every event contributing something visible.
            if (w1[i] == 0) w1[i] = 0xA0761D6478BD642FUL;
            if (w2[i] == 0) w2[i] = 0xE7037ED1A0B428DBUL;
        }
    }

    private static ulong NextSplitMix64(ref ulong x)
    {
        x += 0x9E3779B97F4A7C15UL;
        ulong z = x;
        z = (z ^ (z >> 30)) * 0xBF58476D1CE4E5B9UL;
        z = (z ^ (z >> 27)) * 0x94D049BB133111EBUL;
        return z ^ (z >> 31);
    }

    private readonly record struct Sig(ulong A, ulong B);
}

// Demo code

var solver = new Solution();

string[] events1 = ["a", "b", "a", "c"];
int[] result1 = solver.ShortestUniqueSignatureSegment(events1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", events1)}]");
Console.WriteLine($"Output: [{string.Join(", ", result1)}]");
Console.WriteLine("Expected: [2, 2, 2, 1]");
Console.WriteLine();

string[] events2 = ["x", "y", "x", "y"];
int[] result2 = solver.ShortestUniqueSignatureSegment(events2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", events2)}]");
Console.WriteLine($"Output: [{string.Join(", ", result2)}]");
Console.WriteLine("Expected: [3, 3, 3, 3]");
Console.WriteLine();

string[] events3 = ["login", "pay", "login"];
int[] result3 = solver.ShortestUniqueSignatureSegment(events3);
Console.WriteLine("Extra Demo:");
Console.WriteLine($"Input: [{string.Join(", ", events3)}]");
Console.WriteLine($"Output: [{string.Join(", ", result3)}]");