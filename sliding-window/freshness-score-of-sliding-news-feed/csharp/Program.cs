/*
 * Title: Freshness Score of a Sliding News Feed
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are building a news aggregator that monitors a stream of articles.
 * Each article has a freshness score represented as a non-negative integer.
 * Your task is to find the maximum average freshness score across any contiguous
 * window of exactly k articles in the feed.
 *
 * Given an integer array scores where scores[i] represents the freshness score
 * of the i-th article, and an integer k, return the maximum average value of any
 * contiguous subarray of length k. Your answer will be accepted if it is within
 * 10^-5 of the actual answer.
 *
 * Constraints:
 *   - 1 <= k <= scores.length <= 10^5
 *   - 0 <= scores[i] <= 10^4
 *
 * Example 1:
 *   Input:  scores = [3, 7, 2, 9, 4, 6, 1], k = 3
 *   Output: 6.66667
 *   Explanation: The subarray [9, 4, 6] has sum 19, average 19/3 ≈ 6.66667
 *
 * Example 2:
 *   Input:  scores = [5, 5, 5, 5], k = 2
 *   Output: 5.00000
 *   Explanation: Every window of size 2 has sum 10 and average 5.0
 */

using System;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class — contains the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Finds the maximum average of any contiguous subarray of length k.
    ///
    /// Time Complexity:  O(n)  — we visit each element at most twice
    ///                           (once when the window expands, once when it slides).
    ///                           Actually each element is touched exactly once overall.
    /// Space Complexity: O(1)  — we only store a handful of scalar variables;
    ///                           no extra arrays or data structures are needed.
    ///
    /// Approach — Sliding Window:
    ///   Instead of recomputing the sum of every k-element window from scratch
    ///   (which would be O(n*k)), we maintain a "running window sum".
    ///   When the window slides one position to the right:
    ///     • Add the new element that just entered the right edge of the window.
    ///     • Subtract the old element that just left the left edge of the window.
    ///   This keeps the sum up-to-date in O(1) per step.
    /// </summary>
    public double FindMaxAverage(int[] scores, int k)
    {
        // ── Step 1: Build the initial window (first k elements) ──────────────
        // Before we can slide, we need a starting sum for the very first window
        // that covers indices 0 through k-1.
        // We use a long to avoid any integer overflow when scores are large and
        // k is large (max possible sum = 10^4 * 10^5 = 10^9, fits in int, but
        // using long is a safe, explicit habit).
        long windowSum = 0;

        for (int i = 0; i < k; i++)
        {
            // Accumulate each score in the first window.
            // Why: we need a baseline sum before we start sliding.
            windowSum += scores[i];
        }

        // ── Step 2: Record the best (maximum) sum seen so far ────────────────
        // We track the maximum *sum* rather than the maximum *average* because
        // dividing by k is a constant operation — maximising the sum is
        // equivalent to maximising the average.  We convert to average only once
        // at the very end, saving repeated division.
        long maxSum = windowSum;

        // ── Step 3: Slide the window across the rest of the array ────────────
        // The window's right edge starts at index k (the (k+1)-th element).
        // The window's left edge (the element being removed) is at index i - k.
        //
        // Visual example for scores = [3, 7, 2, 9, 4, 6, 1], k = 3:
        //
        //   i=3: window [7, 2, 9]  → add scores[3]=9, remove scores[0]=3
        //   i=4: window [2, 9, 4]  → add scores[4]=4, remove scores[1]=7
        //   i=5: window [9, 4, 6]  → add scores[5]=6, remove scores[2]=2
        //   i=6: window [4, 6, 1]  → add scores[6]=1, remove scores[3]=9

        for (int i = k; i < scores.Length; i++)
        {
            // ── Step 3a: Slide the window one position to the right ──────────
            // Add the element entering the window on the right side.
            // Subtract the element leaving the window on the left side.
            // The element leaving is exactly k positions behind the new element.
            windowSum += scores[i];          // new right-edge element enters
            windowSum -= scores[i - k];      // old left-edge element leaves

            // Why subtract scores[i - k]?
            // When i = k,   the element at index 0   (= i - k) falls out.
            // When i = k+1, the element at index 1   (= i - k) falls out.
            // In general, the element at index (i - k) is always the one that
            // was at the left boundary of the previous window.

            // ── Step 3b: Update the maximum sum if this window is better ─────
            // We only need to keep the single largest sum we've seen.
            if (windowSum > maxSum)
            {
                maxSum = windowSum;
            }
        }

        // ── Step 4: Convert the maximum sum to an average and return ─────────
        // Dividing by k gives the average freshness score of the best window.
        // We cast to double so the division is floating-point, not integer.
        return (double)maxSum / k;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code  (top-level statements — no Main method needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

Solution solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Expected output: 6.66667
// Best window: [9, 4, 6] → sum = 19 → average = 19/3 ≈ 6.66667
int[] scores1 = { 3, 7, 2, 9, 4, 6, 1 };
int k1 = 3;
double result1 = solution.FindMaxAverage(scores1, k1);
Console.WriteLine("=== Example 1 ===");
Console.WriteLine($"Input:    scores = [{string.Join(", ", scores1)}], k = {k1}");
Console.WriteLine($"Output:   {result1:F5}");   // F5 → 5 decimal places
Console.WriteLine($"Expected: 6.66667");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Expected output: 5.00000
// Every window of size 2 has sum 10 → average = 5.0
int[] scores2 = { 5, 5, 5, 5 };
int k2 = 2;
double result2 = solution.FindMaxAverage(scores2, k2);
Console.WriteLine("=== Example 2 ===");
Console.WriteLine($"Input:    scores = [{string.Join(", ", scores2)}], k = {k2}");
Console.WriteLine($"Output:   {result2:F5}");
Console.WriteLine($"Expected: 5.00000");
Console.WriteLine();

// ── Extra edge-case: single element window ───────────────────────────────────
// k = 1 means each window is just one element; the max is the max element.
int[] scores3 = { 1, 12, 3, 4 };
int k3 = 1;
double result3 = solution.FindMaxAverage(scores3, k3);
Console.WriteLine("=== Edge Case: k = 1 ===");
Console.WriteLine($"Input:    scores = [{string.Join(", ", scores3)}], k = {k3}");
Console.WriteLine($"Output:   {result3:F5}");
Console.WriteLine($"Expected: 12.00000");
Console.WriteLine();

// ── Extra edge-case: window spans entire array ───────────────────────────────
// k == scores.Length → only one possible window; answer is the overall average.
int[] scores4 = { 2, 4, 6, 8 };
int k4 = 4;
double result4 = solution.FindMaxAverage(scores4, k4);
Console.WriteLine("=== Edge Case: k = scores.Length ===");
Console.WriteLine($"Input:    scores = [{string.Join(", ", scores4)}], k = {k4}");
Console.WriteLine($"Output:   {result4:F5}");
Console.WriteLine($"Expected: 5.00000");   // (2+4+6+8)/4 = 20/4 = 5.0