/*
 * Maximum Distinct Prime Factors in a Subarray Window
 * =====================================================
 * Difficulty: Hard | Topic: Sliding Window
 *
 * Problem Description:
 * You are given an integer array `nums` and two integers `k` and `maxUnique`.
 * A subarray of length exactly `k` is called **prime-rich** if the total count
 * of distinct prime factors across ALL elements in the subarray is at most `maxUnique`.
 *
 * A prime factor of a number `n` is any prime `p` such that `n % p == 0`.
 * For example, 12 has prime factors {2, 3}, and 30 has prime factors {2, 3, 5}.
 *
 * Your task is to find the MAXIMUM SUM of a subarray of length exactly `k`
 * such that the subarray is prime-rich (union of all distinct prime factors
 * of all elements does not exceed `maxUnique` distinct primes).
 *
 * If no such subarray exists, return -1.
 *
 * Constraints:
 *   1 <= nums.length <= 10^5
 *   1 <= nums[i] <= 10^5
 *   1 <= k <= nums.length
 *   1 <= maxUnique <= 15
 *
 * Example 1:
 *   Input:  nums = [6, 10, 15, 4, 9, 14], k = 3, maxUnique = 3
 *   Output: 31
 *   Explanation: [6,10,15] → 6={2,3}, 10={2,5}, 15={3,5} → union={2,3,5}, size=3 ≤ 3, sum=31
 *
 * Example 2:
 *   Input:  nums = [2, 3, 5, 7, 11, 13], k = 2, maxUnique = 2
 *   Output: 24
 *   Explanation: Each element is prime. Every window of size 2 has exactly 2 distinct primes.
 *                Maximum sum window is [11, 13] with sum 24.
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    // =========================================================================
    // GetPrimeFactors
    // =========================================================================
    // Helper method: returns the SET of distinct prime factors of a given number.
    //
    // Why a set? Because we only care about DISTINCT primes (e.g., 12 = 2*2*3
    // still only contributes {2, 3}, not {2, 2, 3}).
    //
    // Algorithm: Trial division up to sqrt(n).
    //   - Divide out all 2s first (handles the only even prime).
    //   - Then try odd divisors 3, 5, 7, ... up to sqrt(remaining n).
    //   - If anything remains > 1 after the loop, it is itself prime.
    //
    // Time:  O(sqrt(n)) per number
    // Space: O(number of distinct prime factors) — at most ~6 for n ≤ 10^5
    // =========================================================================
    private static HashSet<int> GetPrimeFactors(int n)
    {
        var factors = new HashSet<int>();

        // Step 1: Handle the prime factor 2 separately.
        // This lets us skip all even numbers in the loop below (optimization).
        if (n % 2 == 0)
        {
            factors.Add(2);
            // Divide out all 2s so we don't re-detect them
            while (n % 2 == 0)
                n /= 2;
        }

        // Step 2: Try odd divisors starting at 3, up to sqrt(n).
        // After removing all 2s, any remaining factor must be odd.
        // We only need to go up to sqrt(n) because if n has a factor > sqrt(n),
        // then n itself must be prime (its complementary factor would be < sqrt(n)
        // and would have been found already).
        for (int i = 3; (long)i * i <= n; i += 2)
        {
            if (n % i == 0)
            {
                factors.Add(i);
                // Divide out all copies of this prime factor
                while (n % i == 0)
                    n /= i;
            }
        }

        // Step 3: If n is still > 1 after the loop, it is a prime factor itself.
        // Example: n=13 → loop finds nothing → 13 is prime, add it.
        if (n > 1)
            factors.Add(n);

        return factors;
    }

    // =========================================================================
    // MaxSumPrimeRichSubarray
    // =========================================================================
    // Main algorithm: Sliding Window of fixed size k.
    //
    // TIME COMPLEXITY:
    //   - Precomputation of prime factors: O(N * sqrt(M)) where M = max(nums[i]) = 10^5
    //   - Sliding window: O(N * P) where P = max distinct primes per number (~6)
    //   - Overall: O(N * sqrt(M))
    //
    // SPACE COMPLEXITY:
    //   - O(N * P) for storing prime factor sets per element
    //   - O(D) for the frequency map where D = number of distinct primes seen
    //
    // KEY INSIGHT:
    //   We maintain a sliding window of exactly k elements.
    //   We track a FREQUENCY MAP of primes: primeCount[p] = how many elements
    //   in the current window have p as a prime factor.
    //   The number of DISTINCT primes in the window = primeCount.Count
    //   (keys with count > 0).
    //
    //   When we slide the window:
    //     - Add the new right element's primes (increment their counts).
    //     - Remove the old left element's primes (decrement; remove key if count→0).
    //   This keeps the distinct prime count updated in O(P) per slide.
    // =========================================================================
    public int MaxSumPrimeRichSubarray(int[] nums, int k, int maxUnique)
    {
        int n = nums.Length;

        // ── Step 1: Precompute prime factors for every element ────────────────
        // We do this upfront so the sliding window loop is fast.
        // primeFactors[i] = set of distinct prime factors of nums[i]
        var primeFactors = new HashSet<int>[n];
        for (int i = 0; i < n; i++)
            primeFactors[i] = GetPrimeFactors(nums[i]);

        // ── Step 2: Initialize the first window [0 .. k-1] ───────────────────
        // primeCount maps each prime → how many elements in the current window
        // have that prime as a factor.
        // Example: window=[6,10,15], 6={2,3},10={2,5},15={3,5}
        //   primeCount = {2:2, 3:2, 5:2} → 3 distinct primes
        var primeCount = new Dictionary<int, int>();

        long windowSum = 0; // Use long to avoid overflow with large sums

        for (int i = 0; i < k; i++)
        {
            // Add nums[i] to the running sum
            windowSum += nums[i];

            // Register each prime factor of nums[i] in the frequency map
            foreach (int p in primeFactors[i])
            {
                if (!primeCount.ContainsKey(p))
                    primeCount[p] = 0;
                primeCount[p]++;
            }
        }

        // ── Step 3: Check the first window and record its sum if valid ────────
        // The window is valid if the number of distinct primes ≤ maxUnique.
        // primeCount.Count gives us exactly the number of distinct primes
        // currently in the window (since we remove keys when count drops to 0).
        long maxSum = long.MinValue;
        bool found = false;

        if (primeCount.Count <= maxUnique)
        {
            maxSum = windowSum;
            found = true;
        }

        // ── Step 4: Slide the window from left=0 to left=n-k-1 ───────────────
        // At each step:
        //   - 'left'  is the index being REMOVED from the window
        //   - 'right' is the index being ADDED to the window (= left + k)
        for (int left = 0; left < n - k; left++)
        {
            int right = left + k; // The new element entering the window

            // ── 4a: Add the new right element ─────────────────────────────────
            // Increase the window sum by nums[right]
            windowSum += nums[right];

            // Register each prime factor of nums[right] in the frequency map
            foreach (int p in primeFactors[right])
            {
                if (!primeCount.ContainsKey(p))
                    primeCount[p] = 0;
                primeCount[p]++;
            }

            // ── 4b: Remove the old left element ───────────────────────────────
            // Decrease the window sum by nums[left]
            windowSum -= nums[left];

            // Decrement the count for each prime factor of nums[left].
            // If a prime's count drops to 0, REMOVE it from the dictionary.
            // This is crucial: primeCount.Count must reflect only primes
            // that are ACTUALLY present in the current window.
            foreach (int p in primeFactors[left])
            {
                primeCount[p]--;
                if (primeCount[p] == 0)
                    primeCount.Remove(p);
            }

            // ── 4c: Check validity and update maximum ─────────────────────────
            // The current window is [left+1 .. right].
            // It is valid if the number of distinct primes ≤ maxUnique.
            if (primeCount.Count <= maxUnique)
            {
                if (!found || windowSum > maxSum)
                {
                    maxSum = windowSum;
                    found = true;
                }
            }
        }

        // ── Step 5: Return result ─────────────────────────────────────────────
        // If no valid window was found, return -1 as specified.
        return found ? (int)maxSum : -1;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (Top-Level Statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

Console.WriteLine("=== Maximum Distinct Prime Factors in a Subarray Window ===");
Console.WriteLine();

// ── Example 1 ────────────────────────────────────────────────────────────────
// nums = [6, 10, 15, 4, 9, 14], k = 3, maxUnique = 3
// Windows of size 3:
//   [6,10,15]  → 6={2,3}, 10={2,5}, 15={3,5} → union={2,3,5} size=3 ≤ 3, sum=31  ✓
//   [10,15,4]  → 10={2,5},15={3,5}, 4={2}    → union={2,3,5} size=3 ≤ 3, sum=29  ✓
//   [15,4,9]   → 15={3,5}, 4={2},   9={3}    → union={2,3,5} size=3 ≤ 3, sum=28  ✓
//   [4,9,14]   → 4={2},    9={3},  14={2,7}  → union={2,3,7} size=3 ≤ 3, sum=27  ✓
// Maximum valid sum = 31
int[] nums1 = { 6, 10, 15, 4, 9, 14 };
int result1 = solution.MaxSumPrimeRichSubarray(nums1, k: 3, maxUnique: 3);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input:    nums=[6,10,15,4,9,14], k=3, maxUnique=3");
Console.WriteLine($"  Expected: 31");
Console.WriteLine($"  Got:      {result1}");
Console.WriteLine($"  Pass:     {result1 == 31}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// nums = [2, 3, 5, 7, 11, 13], k = 2, maxUnique = 2
// Each element is prime, so each contributes exactly 1 distinct prime.
// Every window of size 2 has exactly 2 distinct primes ≤ maxUnique=2.
// Windows and sums:
//   [2,3]=5, [3,5]=8, [5,7]=12, [7,11]=18, [11,13]=24
// Maximum = 24
int[] nums2 = { 2, 3, 5, 7, 11, 13 };
int result2 = solution.MaxSumPrimeRichSubarray(nums2, k: 2, maxUnique: 2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input:    nums=[2,3,5,7,11,13], k=2, maxUnique=2");
Console.WriteLine($"  Expected: 24");
Console.WriteLine($"  Got:      {result2}");
Console.WriteLine($"  Pass:     {result2 == 24}");
Console.WriteLine();

// ── Example 3: No valid window ────────────────────────────────────────────────
// nums = [2, 3, 5, 7], k = 4, maxUnique = 2
// Only window: [2,3,5,7] → union={2,3,5,7} size=4 > 2 → invalid
// Expected: -1
int[] nums3 = { 2, 3, 5, 7 };
int result3 = solution.MaxSumPrimeRichSubarray(nums3, k: 4, maxUnique: 2);
Console.WriteLine("Example 3 (no valid window):");
Console.WriteLine($"  Input:    nums=[2,3,5,7], k=4, maxUnique=2");
Console.WriteLine($"  Expected: -1");
Console.WriteLine($"  Got:      {result3}");
Console.WriteLine($"  Pass:     {result3 == -1}");
Console.WriteLine();

// ── Example 4: Single element windows ────────────────────────────────────────
// nums = [30, 12, 7, 100], k = 1, maxUnique = 3
// 30={2,3,5} size=3 ≤ 3 → sum=30 ✓
// 12={2,3}   size=2 ≤ 3 → sum=12 ✓
//  7={7}     size=1 ≤ 3 → sum=7  ✓
// 100={2,5}  size=2 ≤ 3 → sum=100 ✓
// Maximum = 100
int[] nums4 = { 30, 12, 7, 100 };
int result4 = solution.MaxSumPrimeRichSubarray(nums4, k: 1, maxUnique: 3);
Console.WriteLine("Example 4 (single element windows):");
Console.WriteLine($"  Input:    nums=[30,12,7,100], k=1, maxUnique=3");
Console.WriteLine($"  Expected: 100