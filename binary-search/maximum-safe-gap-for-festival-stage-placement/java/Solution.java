import java.util.*;

/*
Problem Title: Maximum Safe Gap for Festival Stage Placement

Problem Description:
A city is preparing a long riverside walkway for a music festival. There are n approved
installation spots along the walkway, and the ith spot is located at position positions[i]
meters from the western entrance. You must place exactly k outdoor stages at these approved spots.

For safety and crowd flow, the organizers want the minimum distance between any two placed
stages to be as large as possible. In other words, if you place stages at some k positions,
define the arrangement score as the smallest absolute difference between every pair of
consecutive chosen stage positions after sorting them. Your task is to return the maximum
possible arrangement score.

You may assume stages can only be placed at the given approved spots, and each spot can hold
at most one stage. The input positions are not guaranteed to be sorted.

This problem is designed to test whether you can recognize a monotonic decision condition:
if it is possible to place k stages with minimum gap d, then it is also possible for any
smaller gap. That makes binary search on the answer possible.

Constraints:
- 2 <= n <= 10^5
- 2 <= k <= n
- 0 <= positions[i] <= 10^9
- All values in positions are distinct.

Example 1:
Input: positions = [2, 11, 6, 17, 25], k = 3
Output: 9
Explanation: One optimal placement is at positions [2, 11, 25]. The gaps are 9 and 14,
so the minimum gap is 9. No arrangement can achieve a minimum gap of 10.

Example 2:
Input: positions = [1, 3, 4, 8, 13, 17], k = 4
Output: 4
Explanation: One optimal placement is [1, 8, 13, 17]. The gaps are 7, 5, and 4, so the
arrangement score is 4. It is impossible to place 4 stages with every adjacent gap at least 5.
*/

public class Solution {

    /**
     * Computes the maximum possible minimum distance between any two consecutively placed stages
     * after choosing exactly k approved positions.
     *
     * The core idea is:
     * 1. Sort the positions.
     * 2. Binary search on the answer (the minimum allowed gap).
     * 3. For each candidate gap, greedily check whether we can place at least k stages.
     *
     * @param positions array of distinct approved installation positions; may be unsorted
     * @param k exact number of stages to place
     * @return the largest possible minimum gap between consecutive chosen stage positions
     *
     * Time complexity: O(n log n + n log R), where R = maxPosition - minPosition
     * Space complexity: O(1) extra space beyond the sort implementation
     */
    public int maximumSafeGap(int[] positions, int k) {
        // Step 1:
        // Sort the positions so that we can reason about distances from left to right.
        // Once sorted, if we greedily place a stage as early as possible, we maximize
        // the remaining room for future stages.
        Arrays.sort(positions);

        // Step 2:
        // Define the binary search range for the answer.
        //
        // The minimum possible gap is 0 in general, but because all positions are distinct
        // and k >= 2, the best answer will be at least 1 when positions differ by integers.
        // Still, using 0 is safe and simple.
        //
        // The maximum possible gap cannot exceed the distance between the smallest and largest
        // approved positions.
        int left = 0;
        int right = positions[positions.length - 1] - positions[0];

        // This variable stores the best valid answer found so far.
        int best = 0;

        // Step 3:
        // Standard binary search on the answer space.
        //
        // Invariant:
        // - If a gap "mid" is feasible, then every smaller gap is also feasible.
        // - If a gap "mid" is not feasible, then every larger gap is also not feasible.
        while (left <= right) {
            // Use this form to avoid overflow, although values are within int range here.
            int mid = left + (right - left) / 2;

            // Check whether it is possible to place k stages such that every chosen stage
            // is at least "mid" away from the previously chosen one.
            if (canPlaceStages(positions, k, mid)) {
                // If feasible, record it as a candidate answer.
                best = mid;

                // Try to do even better by searching the larger half.
                left = mid + 1;
            } else {
                // If not feasible, we must reduce the required gap.
                right = mid - 1;
            }
        }

        return best;
    }

    /**
     * Determines whether it is possible to place at least k stages in sorted positions such that
     * the distance between every pair of consecutive placed stages is at least minGap.
     *
     * Greedy strategy:
     * - Always place the first stage at the leftmost position.
     * - Then scan from left to right and place the next stage at the earliest position that
     *   is at least minGap away from the last placed stage.
     *
     * Why greedy works:
     * - Placing a stage earlier never hurts future choices; it only leaves more room to the right.
     * - Therefore, if this greedy method cannot place k stages, no other method can.
     *
     * @param sortedPositions positions sorted in non-decreasing order
     * @param k exact number of stages we want to place
     * @param minGap required minimum distance between consecutive placed stages
     * @return true if placing k stages is possible with at least minGap separation; false otherwise
     *
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canPlaceStages(int[] sortedPositions, int k, int minGap) {
        // We always place the first stage at the first available sorted position.
        int placedCount = 1;
        int lastPlacedPosition = sortedPositions[0];

        // Walk through the remaining positions.
        for (int i = 1; i < sortedPositions.length; i++) {
            // If the current position is far enough from the last placed stage,
            // we can safely place another stage here.
            if (sortedPositions[i] - lastPlacedPosition >= minGap) {
                placedCount++;
                lastPlacedPosition = sortedPositions[i];

                // As soon as we have placed k stages, we can stop early.
                if (placedCount >= k) {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k stages, it is impossible.
        return false;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n)
     * Space complexity: O(n) for the generated string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected answers so the output can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n log n) per demonstration call due to sorting and binary search
     * Space complexity: O(1) extra space beyond sorting implementation
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] positions1 = {2, 11, 6, 17, 25};
        int k1 = 3;
        int result1 = solution.maximumSafeGap(positions1, k1);
        System.out.println("Example 1:");
        System.out.println("positions = " + solution.arrayToString(positions1) + ", k = " + k1);
        System.out.println("Output   = " + result1);
        System.out.println("Expected = 9");
        System.out.println();

        // Example 2
        int[] positions2 = {1, 3, 4, 8, 13, 17};
        int k2 = 4;
        int result2 = solution.maximumSafeGap(positions2, k2);
        System.out.println("Example 2:");
        System.out.println("positions = " + solution.arrayToString(positions2) + ", k = " + k2);
        System.out.println("Output   = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional quick sanity check
        int[] positions3 = {0, 5};
        int k3 = 2;
        int result3 = solution.maximumSafeGap(positions3, k3);
        System.out.println("Additional Test:");
        System.out.println("positions = " + solution.arrayToString(positions3) + ", k = " + k3);
        System.out.println("Output   = " + result3);
        System.out.println("Expected = 5");
    }
}