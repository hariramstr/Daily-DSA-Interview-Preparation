import java.util.*;

/*
Problem Title: Minimum Scanner Range for Warehouse Aisle Labels

Problem Description:
A warehouse has several aisle labels placed along a straight corridor. The positions of the labels are given in a sorted integer array labels, where labels[i] is the position of the i-th label on the corridor. You are also given an integer k, the number of handheld scanners available. Each scanner can be placed at any real-valued position and can read every label whose distance from the scanner is at most R, where R is the scanner's reading range. All scanners use the same range.

Your task is to find the minimum integer value R such that all labels can be covered using at most k scanners.

A scanner covers a continuous interval [x - R, x + R], so once a scanner is placed, it may cover multiple nearby labels. You may choose scanner positions optimally. Return the smallest possible integer R.

This problem should be solved efficiently for large inputs. A common approach is to binary search the answer R and greedily check whether all labels can be covered with at most k scanners.

Constraints:
- 1 <= labels.length <= 100000
- 0 <= labels[i] <= 1000000000
- labels is sorted in non-decreasing order
- 1 <= k <= labels.length
- Return an integer answer

Example 1:
Input: labels = [1, 2, 8, 12, 17], k = 2
Output: 4
Explanation: Place one scanner at position 4 to cover labels in [0, 8], covering 1, 2, and 8. Place the second scanner at position 13 to cover [9, 17], covering 12 and 17. Range 3 is not enough, so the minimum range is 4.

Example 2:
Input: labels = [0, 5, 6, 7, 20], k = 3
Output: 1
Explanation: One scanner at 0 covers label 0. One scanner at 6 covers labels 5, 6, and 7. One scanner at 20 covers label 20. Therefore range 1 is sufficient, and range 0 would not cover both 5 and 7 with a single scanner.
*/

public class Solution {

    /**
     * Finds the minimum integer scanner range R such that all label positions can be covered
     * using at most k scanners.
     *
     * Core idea:
     * 1. Binary search on the answer R.
     * 2. For a fixed R, greedily determine how many scanners are needed.
     * 3. If k scanners are enough, try a smaller R; otherwise try a larger R.
     *
     * Why the greedy check works:
     * - Suppose we start from the leftmost uncovered label at position p.
     * - To maximize coverage, place the scanner as far right as possible while still covering p.
     *   That means placing it at position p + R.
     * - Then this scanner covers every label up to p + 2R.
     * - This is optimal for covering the maximum number of labels starting from p.
     *
     * @param labels sorted array of label positions along the corridor
     * @param k maximum number of scanners available
     * @return the smallest integer range R that allows all labels to be covered
     * Time complexity: O(n log D), where n is labels.length and D is labels[n-1] - labels[0]
     * Space complexity: O(1)
     */
    public int minimumScannerRange(int[] labels, int k) {
        int n = labels.length;

        // If we have at least as many scanners as labels, each label can get its own scanner.
        // In that case, range 0 is enough.
        if (k >= n) {
            return 0;
        }

        // The answer must lie between:
        // - 0 (minimum possible range)
        // - labels[last] - labels[first] (safe upper bound)
        //
        // Why is this upper bound safe?
        // With one scanner, a range equal to the full spread is definitely enough,
        // because we can place the scanner somewhere to cover all labels.
        int left = 0;
        int right = labels[n - 1] - labels[0];

        // Standard binary search for the minimum feasible value.
        while (left < right) {
            int mid = left + (right - left) / 2;

            // If range mid is sufficient, try to shrink the answer.
            if (canCover(labels, k, mid)) {
                right = mid;
            } else {
                // Otherwise, mid is too small, so we must go larger.
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Checks whether all labels can be covered using at most k scanners,
     * when every scanner has range r.
     *
     * Greedy strategy:
     * - Start from the first uncovered label.
     * - Let that label be at position start.
     * - Place a scanner at position start + r.
     *   This is the furthest-right placement that still covers start.
     * - That scanner then covers every label up to start + 2*r.
     * - Skip all labels within that covered interval.
     * - Repeat until all labels are covered or we exceed k scanners.
     *
     * This greedy approach is optimal for a fixed r because each scanner is used
     * to cover as many labels as possible from left to right.
     *
     * @param labels sorted array of label positions
     * @param k maximum number of scanners allowed
     * @param r candidate scanner range to test
     * @return true if all labels can be covered with at most k scanners, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canCover(int[] labels, int k, int r) {
        int n = labels.length;
        int scannersUsed = 0;
        int index = 0;

        // Process labels from left to right.
        while (index < n) {
            scannersUsed++;

            // If we already used too many scanners, this range is not sufficient.
            if (scannersUsed > k) {
                return false;
            }

            // The current leftmost uncovered label.
            long start = labels[index];

            // If we place the scanner at start + r, it covers up to start + 2*r.
            // Use long to avoid overflow when positions are large.
            long coveredUntil = start + 2L * r;

            // Skip every label that lies within this scanner's coverage.
            //
            // Detailed reasoning:
            // - labels[index] is definitely covered because it equals start.
            // - Any later label <= coveredUntil is also covered.
            // - The first label > coveredUntil becomes the next uncovered label.
            while (index < n && labels[index] <= coveredUntil) {
                index++;
            }
        }

        // If we finished the loop, all labels were covered using at most k scanners.
        return true;
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo calls, excluding the called algorithm
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] labels1 = {1, 2, 8, 12, 17};
        int k1 = 2;
        int result1 = solution.minimumScannerRange(labels1, k1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 4");

        int[] labels2 = {0, 5, 6, 7, 20};
        int k2 = 3;
        int result2 = solution.minimumScannerRange(labels2, k2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 1");

        // Additional quick sanity checks for beginners:
        int[] labels3 = {5};
        int k3 = 1;
        System.out.println("Single label result: " + solution.minimumScannerRange(labels3, k3));
        System.out.println("Expected: 0");

        int[] labels4 = {1, 10};
        int k4 = 1;
        System.out.println("Two labels, one scanner result: " + solution.minimumScannerRange(labels4, k4));
        System.out.println("Expected: 5");
    }
}