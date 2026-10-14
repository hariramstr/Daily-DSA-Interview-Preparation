import java.util.*;

/*
Problem Title: Minimum Tap Strength for Garden Row Coverage

Problem Description:
A landscaping company manages a straight garden row represented by positions from 0 to L on a number line.
There are n sprinklers installed along the row. The i-th sprinkler is fixed at position positions[i] and can
water every point within distance s from its location if all sprinklers are configured with the same tap
strength s. In other words, with strength s, sprinkler i covers the interval [positions[i] - s, positions[i] + s].

Your task is to find the minimum integer tap strength s such that the entire garden row from 0 to L is covered
by at least one sprinkler. Coverage is continuous: every point in the interval [0, L] must lie inside the
watering range of some sprinkler. If it is impossible to cover the full row even with arbitrarily large strength
because there are no sprinklers, return -1.

This problem is designed to reward an efficient solution. A direct simulation over all possible strengths is too
slow when L is large. Instead, determine whether a candidate strength is feasible, and use binary search on the answer.

Constraints:
- 0 <= L <= 10^9
- 0 <= n == positions.length <= 2 * 10^5
- 0 <= positions[i] <= L
- positions may be unsorted
- Return the minimum integer strength

Example 1:
Input: L = 10, positions = [1, 4, 8]
Output: 2
Explanation: With strength 2, the covered intervals are [-1,3], [2,6], and [6,10], which together cover the
entire row [0,10]. Strength 1 leaves gaps.

Example 2:
Input: L = 12, positions = [3, 9]
Output: 3
Explanation: With strength 3, the intervals are [0,6] and [6,12], so the full row is covered. With strength 2,
coverage is [1,5] and [7,11], leaving uncovered points.
*/

public class Solution {

    /**
     * Computes the minimum integer tap strength needed so that the union of all sprinkler
     * coverage intervals fully covers the garden row [0, L].
     *
     * The method first sorts the sprinkler positions. Then it uses binary search on the answer:
     * for a candidate strength s, it checks whether the intervals [positions[i] - s, positions[i] + s]
     * cover the whole target interval [0, L].
     *
     * @param L the right endpoint of the garden row; the row is the interval [0, L]
     * @param positions the fixed positions of sprinklers; may be unsorted
     * @return the minimum integer strength required to cover [0, L], or -1 if there are no sprinklers
     * Time complexity: O(n log n + n log L)
     * Space complexity: O(n) due to copying the input array for sorting
     */
    public int minimumTapStrength(int L, int[] positions) {
        // If there are no sprinklers at all, coverage is impossible unless the problem explicitly
        // allowed "empty interval only" handling. The statement says to return -1 if there are no
        // sprinklers and full coverage is impossible even with arbitrarily large strength because
        // there are no sprinklers. So we follow that rule directly.
        if (positions == null || positions.length == 0) {
            return -1;
        }

        // Work on a copy so the original input remains unchanged.
        int[] sorted = Arrays.copyOf(positions, positions.length);
        Arrays.sort(sorted);

        // Binary search range:
        // - Minimum possible strength is 0.
        // - A safe upper bound is L.
        //
        // Why is L always enough when at least one sprinkler exists?
        // Because every sprinkler position lies in [0, L].
        // For any sprinkler at position p, interval [p - L, p + L] always contains [0, L]:
        //   p - L <= 0   because p <= L
        //   p + L >= L   because p >= 0
        // So with strength L, even a single sprinkler covers the whole row.
        int left = 0;
        int right = L;
        int answer = L;

        // Standard binary search for the minimum feasible strength.
        while (left <= right) {
            int mid = left + (right - left) / 2;

            // If strength mid is enough, try to find a smaller feasible strength.
            if (canCover(L, sorted, mid)) {
                answer = mid;
                right = mid - 1;
            } else {
                // Otherwise, we need more strength.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given common sprinkler strength is sufficient to cover the entire garden row [0, L].
     *
     * Since positions are sorted, their coverage intervals are processed from left to right.
     * We maintain the farthest point covered continuously so far. If the next interval starts after
     * that covered frontier, a gap exists and coverage fails. Otherwise, we extend the frontier.
     *
     * @param L the right endpoint of the garden row [0, L]
     * @param sortedPositions sprinkler positions sorted in nondecreasing order
     * @param strength the candidate common tap strength to test
     * @return true if [0, L] is fully covered with this strength; false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canCover(int L, int[] sortedPositions, int strength) {
        // This variable represents the farthest point on the number line that is covered
        // continuously starting from 0.
        //
        // More precisely:
        // - At any moment, we know that every point in [0, coveredUntil] is covered.
        // - If coveredUntil >= L, then the whole garden row is covered.
        long coveredUntil = 0;

        // Process each sprinkler interval in sorted order of sprinkler position.
        for (int position : sortedPositions) {
            // Compute the interval covered by this sprinkler.
            long start = (long) position - strength;
            long end = (long) position + strength;

            // If this interval ends before 0, it contributes nothing useful to covering [0, L].
            if (end < 0) {
                continue;
            }

            // If this interval starts after the current covered frontier, then there is a gap:
            // [coveredUntil, start) is not fully covered.
            //
            // Example:
            // coveredUntil = 5, next interval starts at 7
            // Then points between 5 and 7 are uncovered, so full continuous coverage fails.
            if (start > coveredUntil) {
                return false;
            }

            // Otherwise, this interval touches or overlaps the already covered prefix,
            // so we can extend the covered frontier if this interval reaches farther.
            if (end > coveredUntil) {
                coveredUntil = end;
            }

            // Early exit: once we already cover up to at least L, the whole row [0, L] is covered.
            if (coveredUntil >= L) {
                return true;
            }
        }

        // After processing all intervals, coverage succeeds only if we reached at least L.
        return coveredUntil >= L;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement and a few
     * additional sanity checks.
     *
     * @param args command-line arguments; not used
     * @return nothing
     * Time complexity: O(total input size for demonstrated examples)
     * Space complexity: O(total copied array size for demonstrated examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int L1 = 10;
        int[] positions1 = {1, 4, 8};
        int result1 = solution.minimumTapStrength(L1, positions1);
        System.out.println("Example 1:");
        System.out.println("L = " + L1 + ", positions = " + Arrays.toString(positions1));
        System.out.println("Minimum tap strength = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        int L2 = 12;
        int[] positions2 = {3, 9};
        int result2 = solution.minimumTapStrength(L2, positions2);
        System.out.println("Example 2:");
        System.out.println("L = " + L2 + ", positions = " + Arrays.toString(positions2));
        System.out.println("Minimum tap strength = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        int L3 = 0;
        int[] positions3 = {0};
        int result3 = solution.minimumTapStrength(L3, positions3);
        System.out.println("Additional Test 1:");
        System.out.println("L = " + L3 + ", positions = " + Arrays.toString(positions3));
        System.out.println("Minimum tap strength = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        int L4 = 7;
        int[] positions4 = {};
        int result4 = solution.minimumTapStrength(L4, positions4);
        System.out.println("Additional Test 2:");
        System.out.println("L = " + L4 + ", positions = " + Arrays.toString(positions4));
        System.out.println("Minimum tap strength = " + result4);
        System.out.println("Expected = -1");
        System.out.println();

        int L5 = 10;
        int[] positions5 = {8, 1, 4};
        int result5 = solution.minimumTapStrength(L5, positions5);
        System.out.println("Additional Test 3 (unsorted input):");
        System.out.println("L = " + L5 + ", positions = " + Arrays.toString(positions5));
        System.out.println("Minimum tap strength = " + result5);
        System.out.println("Expected = 2");
    }
}