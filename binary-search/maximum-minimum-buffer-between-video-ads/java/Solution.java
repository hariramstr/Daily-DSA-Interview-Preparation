import java.util.*;

/*
Problem Title: Maximum Minimum Buffer Between Video Ads

Problem Description:
A streaming platform wants to insert exactly k advertisement breaks into a video of total length L seconds.
You are given a sorted array candidateTimes where each value represents a second mark at which an ad break
is allowed to start. You may only place ad breaks at these candidate times, and each chosen time must be distinct.

The platform wants the ads to feel evenly spaced, so it defines the quality of a placement as the minimum
distance in seconds between any two consecutive chosen ad breaks.

Your task is to compute the largest possible quality value.

In other words, choose exactly k values from candidateTimes so that the minimum difference between adjacent
chosen values is as large as possible, and return that maximum possible minimum difference.

This is a decision-and-optimization problem: for a guessed minimum gap g, determine whether it is possible
to pick k ad breaks such that every consecutive pair is at least g seconds apart. Use this property to find
the optimal answer efficiently.

Constraints:
- 2 <= candidateTimes.length <= 100000
- 0 <= candidateTimes[i] <= 1000000000
- candidateTimes is sorted in strictly increasing order
- 2 <= k <= candidateTimes.length
- 1 <= L <= 1000000000
- All candidateTimes[i] are within the video, i.e. 0 <= candidateTimes[i] <= L

Example 1:
Input: candidateTimes = [5, 11, 18, 26, 39], k = 3, L = 45
Output: 13
Explanation: Choose ad breaks at 5, 18, and 39. The consecutive gaps are 13 and 21, so the minimum gap is 13.
No other selection of 3 positions can achieve a larger minimum gap.

Example 2:
Input: candidateTimes = [2, 4, 7, 10, 14, 19], k = 4, L = 20
Output: 5
Explanation: One optimal choice is 2, 7, 14, 19. The gaps are 5, 7, and 5, so the minimum gap is 5.

Approach:
- This is the classic "maximize the minimum distance" pattern.
- If a minimum gap g is feasible, then every smaller gap is also feasible.
- That monotonic property allows binary search on the answer.
- To test feasibility for a gap g:
  greedily pick the earliest possible candidate time, then keep picking the next candidate
  that is at least g away from the last chosen one.
- If we can pick at least k ad breaks, then g is feasible.
*/

public class Solution {

    /**
     * Computes the largest possible minimum distance between consecutive chosen ad break times.
     *
     * The input array is already sorted in strictly increasing order, which is exactly what we need
     * for the greedy feasibility check and binary search over the answer.
     *
     * @param candidateTimes sorted array of valid ad break start times
     * @param k the exact number of ad breaks that must be chosen
     * @param L total video length in seconds; included by the problem statement, but not required
     *          by the algorithm because all valid choices are already represented in candidateTimes
     * @return the maximum possible minimum distance between consecutive chosen ad breaks
     *
     * Time complexity: O(n log R), where n is candidateTimes.length and
     * R = candidateTimes[n - 1] - candidateTimes[0]
     * Space complexity: O(1)
     */
    public int maximumMinimumBuffer(int[] candidateTimes, int k, int L) {
        // Defensive handling for completeness.
        // Based on constraints, these cases should not occur, but keeping the method beginner-friendly
        // and robust is still a good practice.
        if (candidateTimes == null || candidateTimes.length == 0 || k <= 1) {
            return 0;
        }

        // The smallest possible minimum gap is 0.
        // In this problem candidateTimes is strictly increasing, so the true answer will be at least 1
        // when k >= 2, but using 0 keeps the binary search template simple and safe.
        int low = 0;

        // The largest possible minimum gap cannot exceed the distance between the first and last
        // candidate times.
        int high = candidateTimes[candidateTimes.length - 1] - candidateTimes[0];

        // This variable stores the best feasible answer found so far.
        int answer = 0;

        // Standard binary search on the answer space.
        // We search for the largest gap that is still feasible.
        while (low <= high) {
            // Use this form to avoid overflow:
            // mid = low + (high - low) / 2
            int mid = low + (high - low) / 2;

            // Check whether it is possible to choose at least k ad breaks
            // such that every consecutive chosen pair is at least 'mid' apart.
            if (canPlaceWithGap(candidateTimes, k, mid)) {
                // If 'mid' is feasible, it is a valid candidate answer.
                answer = mid;

                // Since we want the maximum feasible gap, try larger values.
                low = mid + 1;
            } else {
                // If 'mid' is not feasible, any larger gap will also be impossible.
                // So we must search smaller values.
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Determines whether it is possible to choose at least k ad break times such that
     * every consecutive chosen pair has distance at least requiredGap.
     *
     * Greedy strategy:
     * 1. Always choose the earliest available candidate time first.
     * 2. Then repeatedly choose the next earliest candidate that is at least requiredGap away
     *    from the last chosen time.
     *
     * Why greedy works:
     * - Choosing earlier leaves as much room as possible for future choices.
     * - If this greedy process cannot place k ad breaks, no other placement can.
     *
     * @param candidateTimes sorted array of valid ad break start times
     * @param k number of ad breaks we need to place
     * @param requiredGap the minimum allowed distance between consecutive chosen ad breaks
     * @return true if placing k ad breaks is possible, otherwise false
     *
     * Time complexity: O(n), where n is candidateTimes.length
     * Space complexity: O(1)
     */
    public boolean canPlaceWithGap(int[] candidateTimes, int k, int requiredGap) {
        // We always pick the first candidate time.
        // This is the earliest possible placement and is optimal for the greedy check.
        int chosenCount = 1;
        int lastChosenTime = candidateTimes[0];

        // Scan through the remaining candidate times from left to right.
        for (int i = 1; i < candidateTimes.length; i++) {
            // If the current candidate is far enough from the last chosen one,
            // we can safely choose it.
            if (candidateTimes[i] - lastChosenTime >= requiredGap) {
                chosenCount++;
                lastChosenTime = candidateTimes[i];

                // The moment we have chosen k ad breaks, we can stop early.
                if (chosenCount >= k) {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k chosen times,
        // then this gap is not feasible.
        return false;
    }

    /**
     * A convenience wrapper with a shorter name, useful for demonstrations or interview-style calls.
     *
     * @param candidateTimes sorted array of valid ad break start times
     * @param k the exact number of ad breaks to choose
     * @param L total video length in seconds
     * @return the maximum possible minimum distance between consecutive chosen ad breaks
     *
     * Time complexity: O(n log R)
     * Space complexity: O(1)
     */
    public int solve(int[] candidateTimes, int k, int L) {
        return maximumMinimumBuffer(candidateTimes, k, L);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(n log R) per demonstration call
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] candidateTimes1 = {5, 11, 18, 26, 39};
        int k1 = 3;
        int L1 = 45;
        int result1 = solution.maximumMinimumBuffer(candidateTimes1, k1, L1);
        System.out.println("Example 1 Result: " + result1);
        // Expected: 13

        // Example 2
        int[] candidateTimes2 = {2, 4, 7, 10, 14, 19};
        int k2 = 4;
        int L2 = 20;
        int result2 = solution.maximumMinimumBuffer(candidateTimes2, k2, L2);
        System.out.println("Example 2 Result: " + result2);
        // Expected: 5

        // Additional quick checks
        int[] candidateTimes3 = {1, 2, 4, 8, 9};
        int k3 = 3;
        int L3 = 10;
        int result3 = solution.maximumMinimumBuffer(candidateTimes3, k3, L3);
        System.out.println("Additional Example Result: " + result3);
        // One optimal choice is 1, 4, 8 -> minimum gap = 3
    }
}