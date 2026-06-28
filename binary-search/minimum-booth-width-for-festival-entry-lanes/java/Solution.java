import java.util.*;

/*
 * Title: Minimum Booth Width for Festival Entry Lanes
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A music festival is setting up several entry lanes for attendees. Each group of attendees
 * must stay together in the same lane, and groups must be processed in the given order
 * because their tickets are tied to scheduled arrival windows.
 *
 * You are given an array groups where groups[i] is the number of people in the i-th arriving
 * group, and an integer m representing the number of available entry lanes.
 *
 * Every lane can handle a contiguous sequence of groups, and the total number of people
 * assigned to a single lane cannot exceed that lane's booth width capacity.
 *
 * Your task is to compute the minimum booth width needed so that all groups can be assigned
 * to at most m lanes.
 *
 * In other words, partition the array into at most m contiguous parts while minimizing
 * the maximum part sum.
 *
 * Return the smallest integer booth width that makes such an assignment possible.
 *
 * Constraints:
 * - 1 <= groups.length <= 100000
 * - 1 <= groups[i] <= 1000000000
 * - 1 <= m <= groups.length
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: groups = [12, 7, 15, 9, 10], m = 3
 * Output: 22
 *
 * Note:
 * If we carefully evaluate this example, a booth width of 19 is actually feasible:
 * [12, 7], [15], [9, 10] -> sums are 19, 15, 19, which uses exactly 3 lanes.
 * Therefore, the mathematically correct minimum answer for this input is 19.
 *
 * Example 2:
 * Input: groups = [5, 5, 5, 5, 5, 5], m = 2
 * Output: 15
 *
 * This problem has a monotonic feasibility property:
 * - If a certain booth width works, then any larger booth width also works.
 * That allows binary search on the answer, combined with a greedy feasibility check.
 */
public class Solution {

    /**
     * Computes the minimum booth width needed so that the groups can be split into
     * at most m contiguous lanes, while minimizing the maximum lane sum.
     *
     * The key idea is:
     * 1. The answer cannot be smaller than the largest single group, because a group
     *    cannot be split across lanes.
     * 2. The answer cannot be larger than the sum of all groups, because one lane
     *    could theoretically take everything.
     * 3. For any proposed booth width, we can greedily count how many lanes are needed.
     * 4. Because feasibility is monotonic, we can binary search for the smallest width
     *    that works.
     *
     * @param groups the array of group sizes, where each value represents one indivisible group
     * @param m the maximum number of lanes allowed
     * @return the minimum possible booth width as a long
     * Time complexity: O(n log S), where n is groups.length and S is the search range of sums
     * Space complexity: O(1), excluding input storage
     */
    public long minimumBoothWidth(int[] groups, int m) {
        // The smallest possible answer must be at least the largest single group.
        long left = 0L;

        // The largest possible answer is the sum of all groups.
        long right = 0L;

        // Build the binary search bounds.
        for (int group : groups) {
            left = Math.max(left, group);
            right += group;
        }

        // Standard binary search on the answer space.
        // We search for the smallest feasible booth width.
        while (left < right) {
            // Midpoint chosen this way to avoid overflow.
            long mid = left + (right - left) / 2;

            // If width 'mid' is enough, try to find an even smaller feasible width.
            if (canAssignWithinLanes(groups, m, mid)) {
                right = mid;
            } else {
                // If 'mid' is not enough, we must increase the width.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the minimum feasible width.
        return left;
    }

    /**
     * Checks whether all groups can be assigned into at most m contiguous lanes,
     * such that no lane sum exceeds the given booth width.
     *
     * Greedy strategy:
     * - Keep adding groups to the current lane while the total stays within width.
     * - As soon as adding the next group would exceed width, start a new lane.
     * - This greedy approach minimizes the number of lanes needed for that width.
     *
     * Why greedy is correct here:
     * - For a fixed maximum allowed lane sum, packing each lane as much as possible
     *   never hurts. Starting a new lane earlier would only use the same or more lanes.
     *
     * @param groups the array of group sizes
     * @param m the maximum number of lanes allowed
     * @param width the proposed booth width to test
     * @return true if the groups can be assigned using at most m lanes; false otherwise
     * Time complexity: O(n), where n is groups.length
     * Space complexity: O(1)
     */
    public boolean canAssignWithinLanes(int[] groups, int m, long width) {
        // We start with one lane already in use, because the first group must go somewhere.
        int lanesUsed = 1;

        // Running sum of the current lane.
        long currentLaneLoad = 0L;

        // Process groups in order, because order must be preserved.
        for (int group : groups) {
            // Safety check:
            // If a single group is larger than the proposed width, assignment is impossible.
            if (group > width) {
                return false;
            }

            // If adding this group would exceed the allowed width for the current lane,
            // we must open a new lane and place this group there.
            if (currentLaneLoad + group > width) {
                lanesUsed++;
                currentLaneLoad = group;

                // Early exit:
                // If we already need more than m lanes, this width is not feasible.
                if (lanesUsed > m) {
                    return false;
                }
            } else {
                // Otherwise, safely add the group to the current lane.
                currentLaneLoad += group;
            }
        }

        // If we finished using at most m lanes, the width works.
        return true;
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to convert to string form
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * Important note about Example 1:
     * The problem statement claims the answer is 22 for [12, 7, 15, 9, 10] with m = 3.
     * However, the correct minimum is 19, because:
     * [12, 7] = 19
     * [15] = 15
     * [9, 10] = 19
     * This uses exactly 3 lanes and respects contiguity.
     *
     * Example 2 is correct as stated.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log S) per demonstration call
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] groups1 = {12, 7, 15, 9, 10};
        int m1 = 3;
        long result1 = solution.minimumBoothWidth(groups1, m1);

        System.out.println("Example 1:");
        System.out.println("groups = " + solution.arrayToString(groups1) + ", m = " + m1);
        System.out.println("Computed minimum booth width = " + result1);
        System.out.println("Verification: the mathematically correct answer is 19.");
        System.out.println();

        int[] groups2 = {5, 5, 5, 5, 5, 5};
        int m2 = 2;
        long result2 = solution.minimumBoothWidth(groups2, m2);

        System.out.println("Example 2:");
        System.out.println("groups = " + solution.arrayToString(groups2) + ", m = " + m2);
        System.out.println("Computed minimum booth width = " + result2);
        System.out.println("Expected answer = 15");
        System.out.println();

        // Additional quick sanity checks for beginners:
        int[] groups3 = {10};
        int m3 = 1;
        System.out.println("Additional Test 1:");
        System.out.println("groups = " + solution.arrayToString(groups3) + ", m = " + m3);
        System.out.println("Computed minimum booth width = " + solution.minimumBoothWidth(groups3, m3));
        System.out.println("Expected answer = 10");
        System.out.println();

        int[] groups4 = {1, 2, 3, 4, 5};
        int m4 = 5;
        System.out.println("Additional Test 2:");
        System.out.println("groups = " + solution.arrayToString(groups4) + ", m = " + m4);
        System.out.println("Computed minimum booth width = " + solution.minimumBoothWidth(groups4, m4));
        System.out.println("Expected answer = 5");
        System.out.println();

        int[] groups5 = {1, 2, 3, 4, 5};
        int m5 = 1;
        System.out.println("Additional Test 3:");
        System.out.println("groups = " + solution.arrayToString(groups5) + ", m = " + m5);
        System.out.println("Computed minimum booth width = " + solution.minimumBoothWidth(groups5, m5));
        System.out.println("Expected answer = 15");
    }
}