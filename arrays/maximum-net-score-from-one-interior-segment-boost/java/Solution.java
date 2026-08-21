import java.util.*;

/*
Problem Title: Maximum Net Score from One Interior Segment Boost

Problem Description:
You are given an integer array scores representing daily performance values for a product team.
Positive values help the team's quarterly score, while negative values hurt it.

Management is allowed to apply exactly one temporary boost to a contiguous interior segment of days.
If a segment from index l to r is boosted, where 0 < l <= r < n - 1, then every value inside that
segment contributes twice to the final total, while values outside the segment contribute normally.

Your task is to return the maximum possible final total score after choosing one valid interior
segment to boost.

In other words, if total is the sum of all elements in scores, and segSum is the sum of the chosen
segment, then the final score is total + segSum. You must choose a segment that does not include
the first or last element of the array.

This problem asks you to optimize over all valid contiguous interior segments. A brute-force
solution that checks every segment will be too slow for large inputs.

Constraints:
- 3 <= scores.length <= 200000
- -100000 <= scores[i] <= 100000
- The chosen boosted segment must satisfy 1 <= l <= r <= n - 2

Examples:
1) scores = [4, -2, 3, -1, 5]
   Total sum = 9
   Best interior segment = [3] with sum = 3
   Final score = 9 + 3 = 12

2) scores = [7, -5, 4, 6, -2, 8]
   Total sum = 18
   Best interior segment = [4, 6] with sum = 10
   Final score = 18 + 10 = 28
*/

public class Solution {

    /**
     * Computes the maximum possible final total score after boosting exactly one
     * contiguous interior segment.
     *
     * Core idea:
     * The final score is:
     *     totalSum + chosenInteriorSegmentSum
     *
     * So the problem becomes:
     *     Find the maximum-sum contiguous subarray inside the interior range
     *     [1, n - 2].
     *
     * This is a direct application of Kadane's algorithm, but only over the
     * valid interior indices.
     *
     * @param scores the input array of daily performance values
     * @return the maximum final total score after boosting one valid interior segment
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maximumNetScore(int[] scores) {
        // Step 1:
        // Compute the normal total sum of the entire array.
        // We use long because:
        // - n can be as large as 200000
        // - each value can be as large as 100000 in magnitude
        // Therefore the total sum can exceed the int range.
        long totalSum = 0L;
        for (int value : scores) {
            totalSum += value;
        }

        // Step 2:
        // We must choose exactly one contiguous segment fully inside the array,
        // meaning it can only use indices from 1 to n - 2 inclusive.
        //
        // We now find the maximum subarray sum on that restricted range.
        //
        // Kadane's algorithm maintains:
        // - currentBestEndingHere: best sum of a subarray that MUST end at current index
        // - bestInteriorSegmentSum: best sum seen anywhere so far in the interior
        //
        // Initialization:
        // Since the interior is guaranteed non-empty because n >= 3,
        // index 1 always exists and is a valid starting point.
        long currentBestEndingHere = scores[1];
        long bestInteriorSegmentSum = scores[1];

        // Step 3:
        // Process each remaining interior element from index 2 to n - 2.
        for (int i = 2; i <= scores.length - 2; i++) {
            // For the current position i, there are exactly two possibilities:
            //
            // 1) Start a brand-new segment at i
            //    Sum = scores[i]
            //
            // 2) Extend the previous best segment that ended at i - 1
            //    Sum = currentBestEndingHere + scores[i]
            //
            // We choose whichever is larger.
            currentBestEndingHere = Math.max(scores[i], currentBestEndingHere + scores[i]);

            // Update the global best interior segment sum if the current one is better.
            bestInteriorSegmentSum = Math.max(bestInteriorSegmentSum, currentBestEndingHere);
        }

        // Step 4:
        // The final boosted score is:
        // normal total + best interior segment sum
        return totalSum + bestInteriorSegmentSum;
    }

    /**
     * Helper method that returns the maximum-sum contiguous subarray restricted to
     * the interior indices [1, n - 2].
     *
     * This method is separated for clarity and educational value.
     *
     * @param scores the input array
     * @return the maximum sum of any valid interior contiguous segment
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maxInteriorSegmentSum(int[] scores) {
        long currentBestEndingHere = scores[1];
        long bestInteriorSegmentSum = scores[1];

        for (int i = 2; i <= scores.length - 2; i++) {
            currentBestEndingHere = Math.max(scores[i], currentBestEndingHere + scores[i]);
            bestInteriorSegmentSum = Math.max(bestInteriorSegmentSum, currentBestEndingHere);
        }

        return bestInteriorSegmentSum;
    }

    /**
     * Helper method that computes the total sum of the array.
     *
     * @param scores the input array
     * @return the sum of all elements in the array
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long totalSum(int[] scores) {
        long sum = 0L;
        for (int value : scores) {
            sum += value;
        }
        return sum;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding method internals
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] scores1 = {4, -2, 3, -1, 5};
        long result1 = solution.maximumNetScore(scores1);
        System.out.println("Input: " + Arrays.toString(scores1));
        System.out.println("Total sum: " + solution.totalSum(scores1));
        System.out.println("Best interior segment sum: " + solution.maxInteriorSegmentSum(scores1));
        System.out.println("Maximum final score: " + result1);
        System.out.println("Expected: 12");
        System.out.println();

        int[] scores2 = {7, -5, 4, 6, -2, 8};
        long result2 = solution.maximumNetScore(scores2);
        System.out.println("Input: " + Arrays.toString(scores2));
        System.out.println("Total sum: " + solution.totalSum(scores2));
        System.out.println("Best interior segment sum: " + solution.maxInteriorSegmentSum(scores2));
        System.out.println("Maximum final score: " + result2);
        System.out.println("Expected: 28");
        System.out.println();

        int[] scores3 = {1, -10, 2};
        long result3 = solution.maximumNetScore(scores3);
        System.out.println("Input: " + Arrays.toString(scores3));
        System.out.println("Total sum: " + solution.totalSum(scores3));
        System.out.println("Best interior segment sum: " + solution.maxInteriorSegmentSum(scores3));
        System.out.println("Maximum final score: " + result3);
        System.out.println("Expected: -7");
        System.out.println();

        int[] scores4 = {5, 1, 2, 3, 4};
        long result4 = solution.maximumNetScore(scores4);
        System.out.println("Input: " + Arrays.toString(scores4));
        System.out.println("Total sum: " + solution.totalSum(scores4));
        System.out.println("Best interior segment sum: " + solution.maxInteriorSegmentSum(scores4));
        System.out.println("Maximum final score: " + result4);
        System.out.println("Expected: 20");
    }
}