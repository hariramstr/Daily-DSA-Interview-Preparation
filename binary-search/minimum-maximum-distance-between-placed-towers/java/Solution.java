```java
/*
 * Title: Minimum Maximum Distance Between Placed Towers
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given a list of n candidate positions along a straight road, represented as a
 * sorted array of distinct integers `positions`. You must place exactly k communication
 * towers among these candidate positions. Your goal is to minimize the maximum distance
 * between any two consecutively placed towers (including the first and last positions,
 * which must always have a tower).
 *
 * Given that the first position positions[0] and the last position positions[n-1] must
 * always contain a tower, place the remaining k - 2 towers at any of the other candidate
 * positions to minimize the maximum gap between any two adjacent towers.
 *
 * Return the minimum possible value of the maximum distance between any two consecutively
 * placed towers. The answer should be returned as a floating-point number, rounded to 5
 * decimal places.
 *
 * Constraints:
 * - 2 <= k <= n <= 10^5
 * - 0 <= positions[i] <= 10^9
 * - positions is sorted in strictly increasing order.
 * - The first and last positions must always have a tower.
 *
 * Key Insight:
 * Binary search on the answer (the maximum allowed gap). For a given maximum gap D,
 * we check if we can place towers such that no consecutive gap exceeds D, using at most
 * k towers total (with first and last fixed).
 *
 * For each segment between consecutive candidate positions, if the segment length is L,
 * we need ceil(L / D) - 1 additional towers inside that segment to ensure no gap > D.
 * We sum these up across all segments. If total towers needed <= k - 2 (since first and
 * last are fixed), then D is feasible.
 *
 * We binary search on D over the range [0, max_gap] with sufficient precision.
 */

import java.util.*;

/**
 * Solution class for the Minimum Maximum Distance Between Placed Towers problem.
 * Uses binary search on the answer (the maximum gap value).
 */
public class Solution {

    /**
     * Checks whether it is feasible to place towers such that the maximum gap
     * between any two consecutive towers does not exceed `maxGap`, using at most
     * `k` towers total (first and last positions are fixed).
     *
     * For each consecutive pair of candidate positions (positions[i], positions[i+1]),
     * the segment length is gap = positions[i+1] - positions[i].
     * To ensure no sub-gap exceeds maxGap within this segment, we need to place
     * ceil(gap / maxGap) - 1 intermediate towers inside this segment.
     * (We divide the segment into ceil(gap/maxGap) equal parts, each <= maxGap.)
     *
     * We sum the required intermediate towers across all segments. If the total
     * required intermediate towers <= k - 2, then maxGap is feasible.
     *
     * @param positions  sorted array of candidate tower positions
     * @param k          total number of towers to place (including first and last)
     * @param maxGap     the maximum allowed gap between consecutive towers
     * @return           true if it's feasible to place k towers with max gap <= maxGap
     *
     * Time Complexity: O(n) where n = positions.length
     * Space Complexity: O(1)
     */
    public boolean isFeasible(int[] positions, int k, double maxGap) {
        // Edge case: if maxGap is essentially zero, only feasible if k >= n
        // (but we handle this naturally below)

        // Count how many additional (intermediate) towers we need beyond the first and last
        int towersNeeded = 0;

        // Iterate over each consecutive pair of candidate positions
        for (int i = 0; i < positions.length - 1; i++) {
            // Calculate the length of this segment
            double segmentLength = positions[i + 1] - positions[i];

            // How many sub-intervals do we need to split this segment into?
            // Each sub-interval must be <= maxGap.
            // Number of sub-intervals = ceil(segmentLength / maxGap)
            // Number of intermediate towers needed = ceil(segmentLength / maxGap) - 1
            // (We need one tower at each internal division point)

            // Use Math.ceil for the number of sub-intervals
            int subIntervals = (int) Math.ceil(segmentLength / maxGap);

            // Intermediate towers needed for this segment = subIntervals - 1
            // (The endpoints of the segment are already accounted for by adjacent segments
            //  or the fixed first/last towers)
            towersNeeded += subIntervals - 1;

            // Early termination: if we already need more than k-2 intermediate towers,
            // this maxGap is not feasible
            if (towersNeeded > k - 2) {
                return false;
            }
        }

        // If total intermediate towers needed <= k - 2, it's feasible
        // (We have k - 2 "free" towers to place between the fixed first and last)
        return towersNeeded <= k - 2;
    }

    /**
     * Finds the minimum possible value of the maximum distance between any two
     * consecutively placed towers, given that exactly k towers must be placed
     * among the candidate positions, with the first and last positions fixed.
     *
     * Algorithm:
     * 1. Binary search on the answer D (the maximum gap).
     * 2. The search range is [0, positions[n-1] - positions[0]].
     * 3. For each candidate D (midpoint of current range), check feasibility.
     * 4. If feasible, try a smaller D (move right boundary down).
     * 5. If not feasible, try a larger D (move left boundary up).
     * 6. After sufficient iterations (for floating-point precision), return the answer.
     *
     * @param positions  sorted array of candidate tower positions (distinct integers)
     * @param k          total number of towers to place (2 <= k <= n)
     * @return           minimum possible maximum gap, rounded to 5 decimal places
     *
     * Time Complexity: O(n * log(maxVal / epsilon)) where maxVal = max gap, epsilon = precision
     *                  With ~100 iterations and n=10^5, this is about 10^7 operations.
     * Space Complexity: O(1) extra space
     */
    public double minimizeMaxDistance(int[] positions, int k) {
        int n = positions.length;

        // Step 1: Define the binary search range.
        // The minimum possible max gap is 0 (if k >= n, we can cover all positions).
        // The maximum possible max gap is the total span (all towers at endpoints).
        double lo = 0.0;
        double hi = positions[n - 1] - positions[0];

        // Step 2: If k >= n, we must place a tower at every candidate position.
        // The answer is the maximum gap between consecutive candidates.
        // (This is handled naturally by the binary search, but let's note it.)

        // Step 3: Binary search with sufficient iterations for floating-point precision.
        // We run ~100 iterations to get precision well beyond 5 decimal places.
        // After 100 iterations, the range shrinks by factor 2^100 ≈ 10^30, far beyond needed.
        for (int iter = 0; iter < 100; iter++) {
            // Calculate the midpoint of the current search range
            double mid = (lo + hi) / 2.0;

            // Step 4: Check if mid is a feasible maximum gap
            if (isFeasible(positions, k, mid)) {
                // mid is feasible: we might be able to do even better (smaller max gap)
                // So we move the upper bound down
                hi = mid;
            } else {
                // mid is not feasible: we need a larger max gap
                // So we move the lower bound up
                lo = mid;
            }
        }

        // Step 5: hi (or lo, they're essentially equal now) is our answer.
        // Round to 5 decimal places.
        double answer = hi;

        // Round to 5 decimal places using standard rounding
        answer = Math.round(answer * 100000.0) / 100000.0;

        return answer;
    }

    /**
     * Formats a double to exactly 5 decimal places for display.
     *
     * @param value  the double value to format
     * @return       string representation with exactly 5 decimal places
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public String formatAnswer(double value) {
        return String.format("%.5f", value);
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through examples from the problem description.
     *
     * @param args  command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        System.out.println("=== Minimum Maximum Distance Between Placed Towers ===");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 1: positions = [1, 3, 6, 7, 12, 19], k = 3
        // We must place towers at positions[0]=1 and positions[5]=19 (fixed).
        // We have k-2 = 1 additional tower to place.
        //
        // Let's enumerate options for the middle tower:
        //   - Place at 3: gaps = [2, 16] → max = 16
        //   - Place at 6: gaps = [5, 13] → max = 13
        //   - Place at 7: gaps = [6, 12] → max = 12
        //   - Place at 12: gaps = [11, 7] → max = 11
        //
        // The minimum of these maxima is 11 (place at position 12).
        // Expected output: 11.00000
        // -----------------------------------------------------------------------
        int[] positions1 = {1, 3, 6, 7, 12, 19};
        int k1 = 3;
        double result1 = sol.minimizeMaxDistance(positions1, k1);
        System.out.println("Example 1:");
        System.out.println("  positions = [1, 3, 6, 7, 12, 19], k = 3");
        System.out.println("  Candidate placements for 1 middle tower:");
        System.out.println("    At 3:  gaps = [2, 16], max = 16");
        System.out.println("    At 6:  gaps = [5, 13], max = 13");
        System.out.println("    At 7:  gaps = [6, 12], max = 12");
        System.out.println("    At 12: gaps = [11, 7], max = 11  <-- optimal");
        System.out.println("  Expected: 11.00000");
        System.out.println("  Got:      " + sol.formatAnswer(result1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: positions = [0, 5, 10, 15, 20], k = 4
        // Fixed: positions[0]=0 and positions[4]=20.
        // We have k-2 = 2 additional towers to place.
        //
        // Segments between candidates: [0-5], [5-10], [10-15], [15-20], each length 5.
        // We need to pick 2 of the 3 interior candidates {5, 10, 15}.
        //
        // Options:
        //   - Place at 5, 10: towers at 0,5,10,20 → gaps [5,5,10] → max = 10
        //   - Place at 5, 15: towers at 0,5,15,20 → gaps [5,10,5] → max = 10
        //   - Place at 10, 15: towers at 0,10,15,20 → gaps [10,5,5] → max = 10
        //
        // All options give max = 10. Expected output: 10.00000
        // -----------------------------------------------------------------------
        int[] positions2 = {0, 5, 10, 15, 20};
        int k2 = 4;
        double result2 = sol.minimizeMaxDistance(positions2, k2);
        System.out.println("Example 2:");
        System.out.println("  positions = [0, 5, 10, 15, 20], k = 4");
        System.out.println("  Candidate placements for 2 middle towers:");
        System.out.println("    At 5,10:  towers at 0,5,10,20  → gaps [5,5,10]  → max = 10");
        System.out.println("    At 5,15:  towers at 0,5,15,20  → gaps [5,10,5]  → max = 10");
        System.out.println("    At 10,15: towers at 0,10,15,20 → gaps [10,5,5]  → max = 10");
        System.out.println("  Expected: 10.00000");
        System.out.println("  Got:      " + sol.formatAnswer(result2));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: k = n (must use all positions)
        // positions = [0, 3, 7, 10], k = 4
        // All positions used: gaps = [3, 4, 3] → max = 4
        // Expected: 4.00000
        // -----------------------------------------------------------------------
        int[] positions3 = {0, 3, 7, 10};
        int k3 = 4;
        double result3 = sol.minimizeMaxDistance(positions3, k3);
        System.out.println("Example 3 (k = n, all positions used):");
        System.out.println("  positions = [0, 3, 7, 10], k = 4");
        System.out.println("  All positions used: gaps = [3, 4, 3] → max = 4");
        System.out.println("  Expected: 4.00000");
        System.out.println("  Got:      " + sol.formatAnswer(result3));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 4: k = 2 (only endpoints)
        // positions = [0, 10, 20, 30], k = 2
        // Only endpoints: towers at 0 and 30 → gap = 30
        // Expected: 30.00000
        // -----------------------------------------------------------------------
        int[] positions4 = {0, 10, 20, 30};
        int k4 = 2;
        double result4 = sol.minimizeMaxDistance(positions4, k4);
        System.out.println("Example 4 (k = 2, only endpoints):");
        System.out.println("  positions = [0, 10, 20, 30], k = 2");
        System.out.println("  Only endpoints: towers at 0 and 30 → gap = 30");
        System.out.println("  Expected: 30.00000");
        System.out.println("  Got:      " + sol.formatAnswer(result4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 5: Larger example
        // positions = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], k = 5
        // Fixed: 1 and 10. Place 3 more from {2,3,4,5,6,7,8,9}.
        // Total span = 9, with 5 towers, ideally gap = 9/4 = 2.25
        // Best we can do with candidates: place at 1,3,5,7,10 → gaps [2,2,2,3] → max=3
        // Or 1,3,5,8,10 → gaps [2,2,3,2] → max=3
        // Or 1,3,6,8,10 → gaps [2,3,2,2] → max=3
        