/*
Title: Minimum Scanner Range for Warehouse Aisle Labels
Difficulty: Medium
Topic: Binary Search

Problem Description:
A warehouse has several aisle labels placed along a straight corridor. The positions of the labels are given in a sorted integer array `labels`, where `labels[i]` is the position of the i-th label on the corridor. You are also given an integer `k`, the number of handheld scanners available. Each scanner can be placed at any real-valued position and can read every label whose distance from the scanner is at most `R`, where `R` is the scanner's reading range. All scanners use the same range.

Your task is to find the minimum integer value `R` such that all labels can be covered using at most `k` scanners.

A scanner covers a continuous interval [x - R, x + R], so once a scanner is placed, it may cover multiple nearby labels. You may choose scanner positions optimally. Return the smallest possible integer `R`.

This problem should be solved efficiently for large inputs. A common approach is to binary search the answer `R` and greedily check whether all labels can be covered with at most `k` scanners.

Constraints:
- 1 <= labels.length <= 100000
- 0 <= labels[i] <= 1000000000
- labels is sorted in non-decreasing order
- 1 <= k <= labels.length
- Return an integer answer

Examples:
1) labels = [1, 2, 8, 12, 17], k = 2
   Output: 4

2) labels = [0, 5, 6, 7, 20], k = 3
   Output: 1
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Binary search tries O(log D) possible answers, where D is the search range of R.
    - For each candidate R, we do one linear greedy scan through the labels array: O(n).
    - Total: O(n log D)

    Space Complexity:
    - O(1) extra space, not counting the input array.

    Beginner-friendly intuition:
    - If a certain range R is enough to cover all labels with at most k scanners,
      then any larger range will also be enough.
    - That "yes/no" behavior is monotonic, which is exactly what binary search needs.
    - So we binary search the smallest R that works.
    */
    public int MinimumScannerRange(int[] labels, int k)
    {
        // Edge case:
        // If there are no labels, the answer would conceptually be 0.
        // The problem guarantees at least 1 label, but keeping this check makes the method safer.
        if (labels == null || labels.Length == 0)
        {
            return 0;
        }

        // We will binary search the answer R.
        //
        // Lowest possible integer range:
        // - 0 means a scanner only covers labels exactly at its chosen position.
        int left = 0;

        // Highest possible integer range:
        // - In the worst case, one scanner might need to cover labels from the first to the last.
        // - A scanner with range R covers an interval of length 2R.
        // - So to cover a span of (last - first), we need 2R >= span, meaning R >= span / 2.
        // - Using the full span as an upper bound is always safe, even if not the tightest.
        int right = labels[^1] - labels[0];

        // This variable will store the best (smallest working) answer found so far.
        int answer = right;

        // Standard binary search on the answer space.
        while (left <= right)
        {
            // Compute the middle candidate carefully.
            // This avoids overflow compared to (left + right) / 2.
            int mid = left + (right - left) / 2;

            // Check whether range = mid is sufficient.
            if (CanCoverAllLabels(labels, k, mid))
            {
                // If mid works, it is a valid answer.
                // But we want the minimum valid answer, so we record it
                // and continue searching on the smaller half.
                answer = mid;
                right = mid - 1;
            }
            else
            {
                // If mid does NOT work, then every smaller range also does not work.
                // So we must search on the larger half.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool CanCoverAllLabels(int[] labels, int k, int range)
    {
        // This method answers:
        // "If every scanner has reading range = range,
        //  can we cover all labels using at most k scanners?"

        // Greedy strategy:
        // - Start from the leftmost uncovered label.
        // - Place a scanner as far to the right as possible while still covering that label.
        // - If the leftmost uncovered label is at position p,
        //   then placing the scanner at p + range is optimal.
        // - That scanner covers from p to p + 2*range.
        // - This maximizes how many future labels we cover with this one scanner.
        //
        // Why greedy is correct:
        // - The current leftmost uncovered label MUST be covered by some scanner.
        // - To cover it, the scanner center cannot be placed to the right of p + range.
        // - So placing it exactly at p + range gives the farthest possible right coverage.
        // - Therefore, no other placement that still covers p can cover more labels to the right.

        int usedScanners = 0;
        int i = 0;
        int n = labels.Length;

        while (i < n)
        {
            // We are about to place one new scanner to cover labels[i],
            // which is currently the leftmost uncovered label.
            usedScanners++;

            // If we already used more than k scanners, this candidate range fails immediately.
            if (usedScanners > k)
            {
                return false;
            }

            // Let p be the leftmost uncovered label.
            int p = labels[i];

            // If we place the scanner at position (p + range),
            // then its coverage interval becomes:
            // [ (p + range) - range, (p + range) + range ] = [ p, p + 2*range ]
            //
            // We only need the right endpoint to skip all labels covered by this scanner.
            long coverRight = (long)p + 2L * range;

            // Move i forward while labels[i] is still covered by the current scanner.
            //
            // We use long for coverRight because positions can be up to 1e9,
            // and 2*range can also be large. Using long avoids overflow concerns.
            while (i < n && labels[i] <= coverRight)
            {
                i++;
            }

            // After this loop:
            // - all labels before index i are covered
            // - labels[i] (if it exists) is the next uncovered label
            // Then we repeat with another scanner.
        }

        // If we finished the loop, all labels were covered using at most k scanners.
        return true;
    }
}

// Demo code:
// We create the sample inputs from the problem statement,
// call the solution, and print the results.

var solution = new Solution();

// Example 1:
// labels = [1, 2, 8, 12, 17], k = 2
// Expected output: 4
int[] labels1 = { 1, 2, 8, 12, 17 };
int k1 = 2;
int result1 = solution.MinimumScannerRange(labels1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// labels = [0, 5, 6, 7, 20], k = 3
// Expected output: 1
int[] labels2 = { 0, 5, 6, 7, 20 };
int k2 = 3;
int result2 = solution.MinimumScannerRange(labels2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional small sanity checks:

// If k equals the number of labels, each label can have its own scanner.
// Then range 0 is enough.
int[] labels3 = { 3, 10, 25 };
int k3 = 3;
int result3 = solution.MinimumScannerRange(labels3, k3);
Console.WriteLine($"Additional Test 1 Result: {result3}");

// If one scanner must cover all labels [0, 10], minimum integer R is 5.
int[] labels4 = { 0, 10 };
int k4 = 1;
int result4 = solution.MinimumScannerRange(labels4, k4);
Console.WriteLine($"Additional Test 2 Result: {result4}");