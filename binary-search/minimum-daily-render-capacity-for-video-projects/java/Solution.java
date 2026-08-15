import java.util.*;

/*
 * Title: Minimum Daily Render Capacity for Video Projects
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A media studio needs to render a sequence of video projects on a shared render farm.
 * You are given an array frames, where frames[i] is the number of frame-units required
 * by the ith project. The projects must be rendered in the given order, and a single day
 * can process only a contiguous group of projects. If the render farm has daily capacity C,
 * then the total frame-units assigned to any one day cannot exceed C.
 *
 * Given frames and an integer d, return the minimum daily render capacity needed to finish
 * all projects in at most d days.
 *
 * You are not allowed to split a single project across multiple days, and you cannot reorder
 * projects. This makes the answer depend on finding the smallest feasible capacity that satisfies
 * the day limit. A straightforward simulation can test whether a chosen capacity works, and the
 * final solution should efficiently search over the possible capacities.
 *
 * Constraints:
 * - 1 <= frames.length <= 100000
 * - 1 <= frames[i] <= 1000000000
 * - 1 <= d <= frames.length
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: frames = [30, 10, 20, 40, 25], d = 3
 * Output: 65
 *
 * Example 2:
 * Input: frames = [8, 15, 7, 12, 10], d = 2
 * Output: 30
 */

public class Solution {

    /**
     * Finds the minimum daily render capacity required to finish all projects
     * in at most d days while preserving project order and without splitting projects.
     *
     * Core idea:
     * 1. The minimum possible capacity cannot be smaller than the largest single project,
     *    because one project cannot be split across days.
     * 2. The maximum possible capacity can be the sum of all projects, which means
     *    everything is rendered in one day.
     * 3. For any chosen capacity, we can greedily simulate how many days are needed.
     * 4. If a capacity works, then any larger capacity also works.
     *    This monotonic behavior makes binary search applicable.
     *
     * @param frames the array where frames[i] is the frame-unit requirement of the ith project
     * @param d the maximum number of days allowed
     * @return the minimum daily render capacity needed
     * Time complexity: O(n log S), where n is frames.length and S is the search range of capacities
     * Space complexity: O(1)
     */
    public long minimumDailyRenderCapacity(int[] frames, int d) {
        // Lower bound:
        // At minimum, capacity must be at least the largest single project,
        // because a project cannot be split across multiple days.
        long left = 0L;

        // Upper bound:
        // At maximum, capacity can be the sum of all projects,
        // which would allow all work to be done in one day.
        long right = 0L;

        // Compute both bounds in one pass.
        for (int frame : frames) {
            left = Math.max(left, frame);
            right += frame;
        }

        // Binary search for the smallest feasible capacity.
        // Invariant:
        // - Any capacity < answer is infeasible.
        // - Any capacity >= answer is feasible.
        while (left < right) {
            // Midpoint capacity candidate.
            // We use this form to avoid overflow:
            // left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this capacity is enough to finish within d days.
            if (canFinishWithinDays(frames, d, mid)) {
                // If mid works, try to find an even smaller feasible capacity.
                right = mid;
            } else {
                // If mid does not work, we must increase capacity.
                left = mid + 1;
            }
        }

        // When left == right, we have found the minimum feasible capacity.
        return left;
    }

    /**
     * Determines whether all projects can be rendered within the given number of days
     * using the specified daily capacity.
     *
     * Greedy simulation:
     * - Process projects in order.
     * - Keep adding projects to the current day while the total does not exceed capacity.
     * - If adding the next project would exceed capacity, start a new day.
     * - Count how many days are needed.
     *
     * Why greedy works:
     * Since projects must stay in order and each day must take a contiguous block,
     * the best way to minimize the number of days for a fixed capacity is to pack
     * each day as much as possible before moving to the next day.
     *
     * @param frames the array of project frame-units
     * @param d the maximum allowed number of days
     * @param capacity the candidate daily capacity to test
     * @return true if all projects can be completed in at most d days, otherwise false
     * Time complexity: O(n), where n is frames.length
     * Space complexity: O(1)
     */
    public boolean canFinishWithinDays(int[] frames, int d, long capacity) {
        // Start with day 1 because if there is at least one project,
        // we will need at least one day.
        int daysUsed = 1;

        // Tracks the total frame-units assigned to the current day.
        long currentDayLoad = 0L;

        // Process each project in the given order.
        for (int frame : frames) {
            // Safety check:
            // If a single project exceeds capacity, this capacity is impossible.
            // In the main binary search this should not happen because left starts
            // at the maximum project size, but keeping this check makes the method robust.
            if (frame > capacity) {
                return false;
            }

            // If the current project fits in the current day, add it.
            if (currentDayLoad + frame <= capacity) {
                currentDayLoad += frame;
            } else {
                // Otherwise, we must start a new day for this project.
                daysUsed++;
                currentDayLoad = frame;

                // Early exit:
                // If we already exceeded the allowed number of days,
                // there is no need to continue the simulation.
                if (daysUsed > d) {
                    return false;
                }
            }
        }

        // If we finished processing all projects without exceeding d days,
        // then this capacity is feasible.
        return true;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log S) per demonstration call
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] frames1 = {30, 10, 20, 40, 25};
        int d1 = 3;
        long result1 = solution.minimumDailyRenderCapacity(frames1, d1);
        System.out.println("Example 1:");
        System.out.println("frames = " + Arrays.toString(frames1) + ", d = " + d1);
        System.out.println("Minimum daily render capacity = " + result1);
        System.out.println("Expected = 65");
        System.out.println();

        int[] frames2 = {8, 15, 7, 12, 10};
        int d2 = 2;
        long result2 = solution.minimumDailyRenderCapacity(frames2, d2);
        System.out.println("Example 2:");
        System.out.println("frames = " + Arrays.toString(frames2) + ", d = " + d2);
        System.out.println("Minimum daily render capacity = " + result2);
        System.out.println("Expected = 30");
        System.out.println();

        // Additional quick sanity checks for beginners:
        // 1) If d == number of projects, answer is the maximum single project.
        int[] frames3 = {5, 9, 3, 7};
        int d3 = 4;
        long result3 = solution.minimumDailyRenderCapacity(frames3, d3);
        System.out.println("Additional Test 1:");
        System.out.println("frames = " + Arrays.toString(frames3) + ", d = " + d3);
        System.out.println("Minimum daily render capacity = " + result3);
        System.out.println("Expected = 9");
        System.out.println();

        // 2) If d == 1, answer is the sum of all projects.
        int[] frames4 = {5, 9, 3, 7};
        int d4 = 1;
        long result4 = solution.minimumDailyRenderCapacity(frames4, d4);
        System.out.println("Additional Test 2:");
        System.out.println("frames = " + Arrays.toString(frames4) + ", d = " + d4);
        System.out.println("Minimum daily render capacity = " + result4);
        System.out.println("Expected = 24");
    }
}