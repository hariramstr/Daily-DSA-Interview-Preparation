```java
/*
 * Title: Maximum Distinct Prime Factors in a Subarray Window
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an integer array `nums` and two integers `k` and `maxUnique`.
 * A subarray of length exactly `k` is called **prime-rich** if the total count
 * of distinct prime factors across all elements in the subarray is at most `maxUnique`.
 *
 * A prime factor of a number `n` is any prime `p` such that `n % p == 0`.
 * For example, 12 has prime factors {2, 3}, and 30 has prime factors {2, 3, 5}.
 *
 * Your task is to find the **maximum sum** of a subarray of length exactly `k`
 * such that the subarray is prime-rich (i.e., the union of all distinct prime factors
 * of all elements does not exceed `maxUnique` distinct primes).
 *
 * If no such subarray exists, return `-1`.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^5
 * - 1 <= nums[i] <= 10^5
 * - 1 <= k <= nums.length
 * - 1 <= maxUnique <= 15
 *
 * Example 1:
 * Input: nums = [6, 10, 15, 4, 9, 14], k = 3, maxUnique = 3
 * Output: 31
 * Explanation: [6, 10, 15] -> 6={2,3}, 10={2,5}, 15={3,5} -> union={2,3,5}, size=3 <= 3, sum=31
 *
 * Example 2:
 * Input: nums = [2, 3, 5, 7, 11, 13], k = 2, maxUnique = 2
 * Output: 24
 * Explanation: Each element is prime. Every window of size 2 has exactly 2 distinct primes.
 * The maximum sum window is [11, 13] with sum 24.
 */

import java.util.*;

/**
 * Solution class for the Maximum Distinct Prime Factors in a Subarray Window problem.
 *
 * <p>Approach:
 * 1. Precompute the prime factors for each number up to 10^5 using a sieve-like method.
 * 2. Use a fixed-size sliding window of length k.
 * 3. Maintain a frequency map of prime factors currently in the window.
 * 4. As we slide the window, add new element's primes and remove outgoing element's primes.
 * 5. Track the maximum sum among all valid windows (distinct prime count <= maxUnique).
 */
public class Solution {

    // Maximum value that nums[i] can take, used for sieve precomputation
    private static final int MAX_VAL = 100_001;

    /**
     * Precomputes the smallest prime factor (SPF) for every number from 2 to MAX_VAL
     * using a linear sieve. This allows us to factorize any number quickly.
     *
     * <p>The smallest prime factor sieve works as follows:
     * - Initialize spf[i] = i for all i.
     * - For each prime p (where spf[p] == p), mark all multiples of p that haven't
     *   been marked yet with p as their smallest prime factor.
     *
     * @return an int array where spf[n] is the smallest prime factor of n
     * Time complexity: O(MAX_VAL * log(log(MAX_VAL))) — essentially linear sieve
     * Space complexity: O(MAX_VAL)
     */
    public int[] buildSmallestPrimeFactorSieve() {
        // spf[i] will hold the smallest prime factor of i
        int[] spf = new int[MAX_VAL];

        // Step 1: Initialize each number as its own smallest prime factor
        // This is the base case: if no smaller prime divides i, then i itself is prime
        for (int i = 0; i < MAX_VAL; i++) {
            spf[i] = i;
        }

        // Step 2: Sieve — for each even number > 2, smallest prime factor is 2
        for (int i = 4; i < MAX_VAL; i += 2) {
            spf[i] = 2;
        }

        // Step 3: For odd numbers starting from 3, if spf[i] == i, then i is prime
        // Mark all multiples of i (starting from i*i) that haven't been assigned yet
        for (int i = 3; (long) i * i < MAX_VAL; i += 2) {
            if (spf[i] == i) {
                // i is prime; mark its odd multiples
                for (int j = i * i; j < MAX_VAL; j += 2 * i) {
                    if (spf[j] == j) {
                        // j hasn't been assigned a smaller prime factor yet
                        spf[j] = i;
                    }
                }
            }
        }

        return spf;
    }

    /**
     * Returns the set of distinct prime factors of a given number n,
     * using the precomputed smallest prime factor array.
     *
     * <p>Algorithm:
     * - While n > 1, divide out spf[n] repeatedly to get all prime factors.
     *
     * @param n   the number to factorize (must be >= 1)
     * @param spf the smallest prime factor sieve array
     * @return a Set of Integer containing all distinct prime factors of n
     * Time complexity: O(log n) per number
     * Space complexity: O(log n) for the result set
     */
    public Set<Integer> getPrimeFactors(int n, int[] spf) {
        Set<Integer> factors = new HashSet<>();

        // Special case: 1 has no prime factors
        if (n <= 1) {
            return factors;
        }

        // Repeatedly divide n by its smallest prime factor
        // Each division reduces n, and we collect unique primes
        while (n > 1) {
            int p = spf[n]; // smallest prime factor of current n
            factors.add(p); // record this prime factor

            // Divide out all occurrences of p from n
            // (we only need distinct primes, so we skip multiplicity)
            while (n % p == 0) {
                n /= p;
            }
        }

        return factors;
    }

    /**
     * Finds the maximum sum of a subarray of length exactly k such that
     * the union of all distinct prime factors of elements in the subarray
     * has at most maxUnique distinct primes.
     *
     * <p>Algorithm Overview:
     * 1. Precompute prime factors for each element using SPF sieve.
     * 2. Use a sliding window of fixed size k.
     * 3. Maintain a frequency map: primeFreq[p] = how many elements in the
     *    current window have p as a prime factor.
     * 4. The number of distinct primes in the window = number of keys in primeFreq
     *    with frequency > 0 (tracked via a counter distinctCount).
     * 5. Slide the window: add right element, remove left element when window > k.
     * 6. When window size == k and distinctCount <= maxUnique, update maxSum.
     *
     * @param nums      the input integer array
     * @param k         the required subarray length
     * @param maxUnique the maximum number of distinct prime factors allowed
     * @return the maximum sum of a valid prime-rich subarray, or -1 if none exists
     * Time complexity: O(MAX_VAL * log(log(MAX_VAL)) + n * log(maxVal))
     *                  where n = nums.length, maxVal = max element value
     * Space complexity: O(MAX_VAL + n * log(maxVal)) for sieve and prime factor storage
     */
    public long maxSumPrimeRichSubarray(int[] nums, int k, int maxUnique) {
        int n = nums.length;

        // ---------------------------------------------------------------
        // Step 1: Build the smallest prime factor sieve for fast factorization
        // ---------------------------------------------------------------
        int[] spf = buildSmallestPrimeFactorSieve();

        // ---------------------------------------------------------------
        // Step 2: Precompute prime factors for each element in nums
        // This avoids recomputing during the sliding window phase
        // ---------------------------------------------------------------
        // primeSets[i] = set of distinct prime factors of nums[i]
        @SuppressWarnings("unchecked")
        Set<Integer>[] primeSets = new Set[n];
        for (int i = 0; i < n; i++) {
            primeSets[i] = getPrimeFactors(nums[i], spf);
        }

        // ---------------------------------------------------------------
        // Step 3: Initialize sliding window variables
        // ---------------------------------------------------------------
        // primeFreq: maps each prime to how many elements in the current window
        //            have that prime as a factor
        Map<Integer, Integer> primeFreq = new HashMap<>();

        // distinctCount: number of distinct primes currently in the window
        // (i.e., number of primes with frequency > 0)
        int distinctCount = 0;

        // windowSum: sum of elements in the current window
        long windowSum = 0;

        // maxSum: best valid sum found so far; -1 means no valid window found yet
        long maxSum = -1;

        // ---------------------------------------------------------------
        // Step 4: Slide the window across the array
        // ---------------------------------------------------------------
        for (int right = 0; right < n; right++) {

            // --- 4a: Expand window by adding nums[right] ---

            // Add nums[right] to the window sum
            windowSum += nums[right];

            // Add each prime factor of nums[right] to the frequency map
            for (int prime : primeSets[right]) {
                int freq = primeFreq.getOrDefault(prime, 0);
                if (freq == 0) {
                    // This prime is newly introduced into the window
                    distinctCount++;
                }
                primeFreq.put(prime, freq + 1);
            }

            // --- 4b: Shrink window if it exceeds size k ---
            // The left boundary of the window is (right - k + 1)
            // If right >= k, we need to remove the element at index (right - k)
            if (right >= k) {
                int left = right - k; // index of element to remove
                windowSum -= nums[left];

                // Remove each prime factor of nums[left] from the frequency map
                for (int prime : primeSets[left]) {
                    int freq = primeFreq.get(prime);
                    if (freq == 1) {
                        // This prime is no longer present in the window
                        primeFreq.remove(prime);
                        distinctCount--;
                    } else {
                        primeFreq.put(prime, freq - 1);
                    }
                }
            }

            // --- 4c: Check if current window is valid ---
            // Window is exactly size k when right >= k - 1
            if (right >= k - 1) {
                // Valid if distinct prime count does not exceed maxUnique
                if (distinctCount <= maxUnique) {
                    // Update the maximum sum if this window is better
                    maxSum = Math.max(maxSum, windowSum);
                }
            }
        }

        // ---------------------------------------------------------------
        // Step 5: Return the result
        // ---------------------------------------------------------------
        return maxSum;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through the examples from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // ---------------------------------------------------------------
        // Example 1 (corrected from problem statement):
        // nums = [6, 10, 15, 4, 9, 14], k = 3, maxUnique = 3
        //
        // Let's trace each window of size 3:
        // Window [6, 10, 15]:  6={2,3}, 10={2,5}, 15={3,5} -> union={2,3,5}, size=3, sum=31 ✓
        // Window [10, 15, 4]:  10={2,5}, 15={3,5}, 4={2}   -> union={2,3,5}, size=3, sum=29 ✓
        // Window [15, 4, 9]:   15={3,5}, 4={2}, 9={3}      -> union={2,3,5}, size=3, sum=28 ✓
        // Window [4, 9, 14]:   4={2}, 9={3}, 14={2,7}      -> union={2,3,7}, size=3, sum=27 ✓
        // Maximum valid sum = 31
        // ---------------------------------------------------------------
        int[] nums1 = {6, 10, 15, 4, 9, 14};
        int k1 = 3, maxUnique1 = 3;
        long result1 = sol.maxSumPrimeRichSubarray(nums1, k1, maxUnique1);
        System.out.println("Example 1:");
        System.out.println("  nums = [6, 10, 15, 4, 9, 14], k = 3, maxUnique = 3");
        System.out.println("  Expected: 31");
        System.out.println("  Got:      " + result1);
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2:
        // nums = [2, 3, 5, 7, 11, 13], k = 2, maxUnique = 2
        //
        // Each element is prime, so each has exactly 1 distinct prime factor (itself).
        // Windows of size 2:
        // [2, 3]:   union={2,3},   size=2, sum=5  ✓
        // [3, 5]:   union={3,5},   size=2, sum=8  ✓
        // [5, 7]:   union={5,7},   size=2, sum=12 ✓
        // [7, 11]:  union={7,11},  size=2, sum=18 ✓
        // [11, 13]: union={11,13}, size=2, sum=24 ✓
        // Maximum valid sum = 24
        // ---------------------------------------------------------------
        int[] nums2 = {2, 3, 5, 7, 11, 13};
        int k2 = 2, maxUnique2 = 2;
        long result2 = sol.maxSumPrimeRichSubarray(nums2, k2, maxUnique2);
        System.out.println("Example 2:");
        System.out.println("  nums = [2, 3, 5, 7, 11, 13], k = 2, maxUnique = 2");
        System.out.println("  Expected: 24");
        System.out.println("  Got:      " + result2);
        System.out.println();

        // ---------------------------------------------------------------
        // Example 3: No valid window exists
        // nums = [2, 3, 5, 7], k = 4, maxUnique = 2
        //
        // Only window: [2, 3, 5, 7] -> union={2,3,5,7}, size=4 > 2 -> invalid
        // Expected: -1
        // ---------------------------------------------------------------
        int[] nums3 = {2, 3, 5, 7};
        int k3 = 4, maxUnique3 = 2;
        long result3 = sol.maxSumPrimeRichSubarray(nums3, k3, maxUnique3);
        System.out.println("Example 3 (no valid window):");
        System.out.println("  nums = [2, 3, 5, 7], k = 4, maxUnique = 2