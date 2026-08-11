/*
Title: Count Equivalent Access Windows by Relative Time Gaps
Difficulty: Hard
Topic: Hashing

Problem Description:
You are given an integer array timestamps representing event times in nondecreasing order, and an integer k.
Consider every contiguous window of exactly k timestamps.

Two windows are considered equivalent if their internal pattern of time gaps is identical.
For a window [t[i], t[i+1], ..., t[i+k-1]], define its gap signature as:
[t[i+1]-t[i], t[i+2]-t[i+1], ..., t[i+k-1]-t[i+k-2]]

Two length-k windows are equivalent if these gap signatures are exactly the same element by element,
even if the absolute starting times differ.

Return the number of unordered pairs of distinct windows that are equivalent.

Important special case:
- When k = 1, every window has an empty gap signature, so all single-element windows are equivalent.

Constraints:
- 1 <= timestamps.length <= 200000
- 1 <= k <= timestamps.length
- 0 <= timestamps[i] <= 10^18
- timestamps is sorted in nondecreasing order

Examples:

Example 1:
Input: timestamps = [2, 5, 9, 12, 15, 19], k = 3
Windows:
[2,5,9]   -> gaps [3,4]
[5,9,12]  -> gaps [4,3]
[9,12,15] -> gaps [3,3]
[12,15,19]-> gaps [3,4]
Only the 1st and 4th windows match, so the answer is 1.

Example 2:
Input: timestamps = [7, 7, 10, 13, 13, 16], k = 2
Windows:
[7,7]   -> gaps [0]
[7,10]  -> gaps [3]
[10,13] -> gaps [3]
[13,13] -> gaps [0]
[13,16] -> gaps [3]
Signature [0] appears 2 times -> 1 pair
Signature [3] appears 3 times -> 3 pairs
Total = 4
*/

using System;
using System.Collections.Generic;

class Solution
{
    // Time Complexity:
    // - O(n), where n is timestamps.Length
    //   We build the gap array once, precompute powers once, and process each window in O(1) time
    //   using rolling hash.
    //
    // Space Complexity:
    // - O(n)
    //   We store the gap array, power arrays, prefix hashes, and the hash frequency map.
    public long CountEquivalentAccessWindows(long[] timestamps, int k)
    {
        int n = timestamps.Length;

        // ------------------------------------------------------------
        // Step 1: Handle the special case k == 1.
        // ------------------------------------------------------------
        // Why?
        // A window of length 1 contains only one timestamp, so it has no internal gaps.
        // That means every such window has the exact same "empty" signature.
        //
        // If there are n windows and all are equivalent, then the number of unordered pairs is:
        // n choose 2 = n * (n - 1) / 2
        // ------------------------------------------------------------
        if (k == 1)
        {
            return (long)n * (n - 1) / 2;
        }

        // ------------------------------------------------------------
        // Step 2: Convert timestamps into a gap array.
        // ------------------------------------------------------------
        // Example:
        // timestamps = [2, 5, 9, 12, 15, 19]
        // gaps       = [3, 4, 3, 3, 4]
        //
        // Why do this?
        // A window of k timestamps corresponds exactly to a subarray of length (k - 1)
        // in the gap array.
        //
        // For example, if k = 3:
        // window [2,5,9]   -> gaps [3,4]
        // window [5,9,12]  -> gaps [4,3]
        // window [9,12,15] -> gaps [3,3]
        // window [12,15,19]-> gaps [3,4]
        //
        // So instead of comparing timestamp windows directly, we compare fixed-length
        // subarrays in the gap array.
        // ------------------------------------------------------------
        int gapCount = n - 1;
        long[] gaps = new long[gapCount];
        for (int i = 0; i < gapCount; i++)
        {
            gaps[i] = timestamps[i + 1] - timestamps[i];
        }

        // ------------------------------------------------------------
        // Step 3: Determine how many windows exist and what signature length is.
        // ------------------------------------------------------------
        // Number of timestamp windows of size k:
        // n - k + 1
        //
        // Each such window has a gap signature of length:
        // k - 1
        // ------------------------------------------------------------
        int windowCount = n - k + 1;
        int signatureLength = k - 1;

        // ------------------------------------------------------------
        // Step 4: Build double rolling hashes for the gap array.
        // ------------------------------------------------------------
        // Why hashing?
        // We want to group equal signatures efficiently.
        // Comparing every pair of windows directly would be too slow:
        // O(number_of_windows^2 * signatureLength), which is impossible for n up to 200000.
        //
        // Instead, we compute a compact hash for each signature in O(1) after O(n) preprocessing.
        //
        // Why double hash?
        // A single hash can theoretically collide.
        // Using two independent hashes makes collisions astronomically unlikely in practice.
        //
        // We use unsigned 64-bit arithmetic with natural overflow.
        // This is fast and works well for rolling hash.
        // ------------------------------------------------------------
        const ulong Base1 = 1469598103934665603UL;
        const ulong Mul1 = 1099511628211UL;

        const ulong Base2 = 7809847782465536322UL;
        const ulong Mul2 = 14029467366897019727UL;

        // ------------------------------------------------------------
        // Step 5: Precompute powers and prefix hashes.
        // ------------------------------------------------------------
        // Standard polynomial rolling hash idea:
        //
        // prefix[i + 1] = prefix[i] * base + value
        //
        // Then any subarray hash can be extracted in O(1):
        // hash(l..r) = prefix[r + 1] - prefix[l] * powBase[length]
        //
        // We do this twice with different bases.
        //
        // Important detail:
        // Gap values can be zero and can be large.
        // To avoid ambiguity around zero, we map each gap value x to x + 1 before hashing.
        // Since gaps are in [0, 1e18], x + 1 still fits in ulong.
        // ------------------------------------------------------------
        ulong[] pow1 = new ulong[gapCount + 1];
        ulong[] pow2 = new ulong[gapCount + 1];
        ulong[] pref1 = new ulong[gapCount + 1];
        ulong[] pref2 = new ulong[gapCount + 1];

        pow1[0] = 1;
        pow2[0] = 1;

        for (int i = 0; i < gapCount; i++)
        {
            pow1[i + 1] = pow1[i] * Mul1;
            pow2[i + 1] = pow2[i] * Mul2;

            ulong value = (ulong)gaps[i] + 1UL;

            pref1[i + 1] = pref1[i] * Mul1 + (value ^ Base1);
            pref2[i + 1] = pref2[i] * Mul2 + (value ^ Base2);
        }

        // ------------------------------------------------------------
        // Step 6: Iterate over every signature window, hash it, and count frequencies.
        // ------------------------------------------------------------
        // Each timestamp window of size k corresponds to a gap subarray of length signatureLength.
        //
        // If a certain signature appears c times, then it contributes:
        // c choose 2 = c * (c - 1) / 2
        //
        // We can compute this incrementally:
        // - When we see a hash that has already appeared 'seen' times,
        //   the new occurrence forms exactly 'seen' new pairs with the previous ones.
        //
        // So:
        // answer += seen
        // then increment frequency
        //
        // This avoids a second pass over the dictionary.
        // ------------------------------------------------------------
        long answer = 0;
        var frequency = new Dictionary<HashKey, long>(windowCount * 2);

        for (int start = 0; start < windowCount; start++)
        {
            int endExclusive = start + signatureLength;

            // --------------------------------------------------------
            // Extract hash of gaps[start .. endExclusive - 1] in O(1).
            // --------------------------------------------------------
            ulong hash1 = pref1[endExclusive] - pref1[start] * pow1[signatureLength];
            ulong hash2 = pref2[endExclusive] - pref2[start] * pow2[signatureLength];

            var key = new HashKey(hash1, hash2);

            if (frequency.TryGetValue(key, out long seen))
            {
                // ----------------------------------------------------
                // This signature has been seen before.
                // The current window forms one new pair with each
                // previously seen identical signature.
                // ----------------------------------------------------
                answer += seen;
                frequency[key] = seen + 1;
            }
            else
            {
                // ----------------------------------------------------
                // First time we see this signature.
                // ----------------------------------------------------
                frequency[key] = 1;
            }
        }

        return answer;
    }

    private readonly record struct HashKey(ulong H1, ulong H2);
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
long[] timestamps1 = { 2, 5, 9, 12, 15, 19 };
int k1 = 3;
long result1 = solution.CountEquivalentAccessWindows(timestamps1, k1);
Console.WriteLine(result1); // Expected: 1

// Example 2
long[] timestamps2 = { 7, 7, 10, 13, 13, 16 };
int k2 = 2;
long result2 = solution.CountEquivalentAccessWindows(timestamps2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional demo: k = 1, all single-element windows are equivalent
long[] timestamps3 = { 1, 10, 100, 1000 };
int k3 = 1;
long result3 = solution.CountEquivalentAccessWindows(timestamps3, k3);
Console.WriteLine(result3); // Expected: 6

// Additional demo: all windows share same signature
long[] timestamps4 = { 0, 2, 4, 6, 8 };
int k4 = 3;
// Windows:
// [0,2,4] -> [2,2]
// [2,4,6] -> [2,2]
// [4,6,8] -> [2,2]
// 3 windows => 3 choose 2 = 3
long result4 = solution.CountEquivalentAccessWindows(timestamps4, k4);
Console.WriteLine(result4); // Expected: 3