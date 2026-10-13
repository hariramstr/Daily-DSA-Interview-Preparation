import java.util.*;

/*
 * Title: Minimum Launch Day for Staggered Marketing Campaigns
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A company is preparing n marketing campaigns. The i-th campaign becomes ready on day readyDay[i].
 * Once a campaign is ready, it stays ready forever. The company wants to form exactly m launch groups,
 * and each launch group must contain k consecutive campaigns in the original lineup. A campaign can
 * belong to at most one group.
 *
 * Your task is to return the minimum day on which it is possible to form at least m such launch groups.
 * If it is impossible even after all campaigns are ready, return -1.
 *
 * Consecutive means adjacent by index, not by ready day. For example, if campaigns 3, 4, and 5 are all
 * ready by a certain day, they may form one group of size 3. However, campaigns 3 and 5 cannot be grouped
 * together without campaign 4.
 *
 * This problem is designed for an efficient solution using binary search on the answer. For any candidate
 * day D, you can check whether enough valid consecutive groups can be formed using only campaigns with
 * readyDay[i] <= D. The feasibility condition is monotonic: if a day works, any later day also works.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= readyDay[i] <= 1000000000
 * - 1 <= m <= 100000
 * - 1 <= k <= n
 * - n == readyDay.length
 *
 * Examples:
 * Example 1:
 * Input: readyDay = [5, 2, 4, 6, 3, 7], m = 2, k = 2
 * Correct Output: 6
 *
 * Example 2:
 * Input: readyDay = [1, 10, 3, 10, 2], m = 3, k = 1
 * Output: 3
 *
 * Important note:
 * If m * k > n, it is impossible to form enough non-overlapping groups, so the answer is -1.
 */

public class Solution {

    /**
     * Returns the minimum day on which it is possible to form at least {@code m} non-overlapping
     * launch groups, where each group contains exactly {@code k} consecutive campaigns.
     *
     * The method uses binary search on the answer:
     * - If a certain day works, then any later day also works.
     * - If a certain day does not work, then any earlier day also does not work.
     *
     * This monotonic behavior makes binary search the correct and efficient approach.
     *
     * @param readyDay array where readyDay[i] is the day the i-th campaign becomes ready
     * @param m the required number of launch groups
     * @param k the number of consecutive campaigns required in each group
     * @return the minimum feasible day, or -1 if it is impossible
     * Time complexity: O(n log R), where n is the number of campaigns and R is the range of days
     * Space complexity: O(1), excluding input storage
     */
    public int minDays(int[] readyDay, int m, int k) {
        int n = readyDay.length;

        // Immediate impossibility check:
        // We need exactly m groups, each of size k, and groups cannot overlap.
        // Therefore, we need at least m * k campaigns in total.
        // Use long to avoid integer overflow when m and k are large.
        if ((long) m * k > n) {
            return -1;
        }

        // Find the minimum and maximum day in the input.
        // These define the binary search range.
        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;

        for (int day : readyDay) {
            left = Math.min(left, day);
            right = Math.max(right, day);
        }

        // Binary search for the smallest day that is feasible.
        while (left < right) {
            // Standard overflow-safe midpoint calculation.
            int mid = left + (right - left) / 2;

            // If we can form at least m groups by day mid,
            // then mid might be the answer, so search the left half including mid.
            if (canMakeGroups(readyDay, m, k, mid)) {
                right = mid;
            } else {
                // Otherwise, mid is too early, so search strictly after mid.
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right and points to the minimum feasible day.
        return left;
    }

    /**
     * Checks whether it is possible to form at least {@code m} non-overlapping groups by a given day.
     *
     * A campaign is usable if readyDay[i] <= targetDay.
     * We scan from left to right and count consecutive usable campaigns.
     * Every time we reach a streak of length k, we form one group immediately and reset the streak to 0.
     *
     * Why reset to 0?
     * Because groups cannot overlap, so once k consecutive campaigns are used for one group,
     * those campaigns cannot be reused in another group.
     *
     * Example:
     * readyDay = [1, 2, 4, 9, 3], targetDay = 4, k = 2
     * usable     = [T, T, T, F, T]
     * We can form one group from the first two campaigns.
     * The third usable campaign alone is not enough to form another group before the streak breaks.
     *
     * @param readyDay array where readyDay[i] is the day the i-th campaign becomes ready
     * @param m the required number of groups
     * @param k the size of each consecutive group
     * @param targetDay the day being tested for feasibility
     * @return true if at least m groups can be formed by targetDay, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canMakeGroups(int[] readyDay, int m, int k, int targetDay) {
        // Number of groups formed so far.
        int groupsFormed = 0;

        // Length of the current streak of consecutive ready campaigns.
        int consecutiveReady = 0;

        // Scan every campaign in order because adjacency is based on index.
        for (int day : readyDay) {
            if (day <= targetDay) {
                // This campaign is ready by targetDay, so it extends the current consecutive streak.
                consecutiveReady++;

                // Once we have exactly k consecutive ready campaigns,
                // we can form one non-overlapping group.
                if (consecutiveReady == k) {
                    groupsFormed++;

                    // Early exit optimization:
                    // As soon as we have enough groups, no need to continue scanning.
                    if (groupsFormed >= m) {
                        return true;
                    }

                    // Reset to 0 because these k campaigns are now used up in one group.
                    // This enforces the "non-overlapping groups" rule.
                    consecutiveReady = 0;
                }
            } else {
                // This campaign is not ready by targetDay.
                // Therefore, any consecutive streak is broken here.
                consecutiveReady = 0;
            }
        }

        // If we finish scanning and still have fewer than m groups, targetDay is not feasible.
        return false;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called method costs
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        // readyDay = [5, 2, 4, 6, 3, 7], m = 2, k = 2
        // By day 5:
        // ready = [T, T, T, F, T, F]
        // Maximum groups of size 2 from consecutive ready campaigns = 1
        // By day 6:
        // ready = [T, T, T, T, T, F]
        // We can form [0,1] and [2,3], so answer = 6
        int[] readyDay1 = {5, 2, 4, 6, 3, 7};
        int m1 = 2;
        int k1 = 2;
        System.out.println(solution.minDays(readyDay1, m1, k1)); // Expected: 6

        // Example 2
        // readyDay = [1, 10, 3, 10, 2], m = 3, k = 1
        // Each ready campaign alone forms a group.
        // By day 3, campaigns at indexes 0, 2, and 4 are ready, so answer = 3
        int[] readyDay2 = {1, 10, 3, 10, 2};
        int m2 = 3;
        int k2 = 1;
        System.out.println(solution.minDays(readyDay2, m2, k2)); // Expected: 3

        // Additional impossible case
        // Need 3 groups of size 2 => total 6 campaigns required, but only 5 exist.
        int[] readyDay3 = {1, 2, 3, 4, 5};
        int m3 = 3;
        int k3 = 2;
        System.out.println(solution.minDays(readyDay3, m3, k3)); // Expected: -1
    }
}