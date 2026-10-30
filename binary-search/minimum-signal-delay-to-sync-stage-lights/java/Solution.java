import java.util.*;

/*
 * Title: Minimum Signal Delay to Sync Stage Lights
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A concert venue controls n stage lights placed along a straight truss. The i-th light is mounted
 * at position positions[i] and can only start its animation at one of the times listed in times[i],
 * where times[i] is sorted in non-decreasing order. To create a synchronized visual effect, the
 * lighting engineer wants to choose exactly one start time for each light so that the difference
 * between the earliest chosen time and the latest chosen time is as small as possible.
 *
 * Return the minimum possible synchronization delay, defined as max(chosenTimes) - min(chosenTimes).
 *
 * You are given:
 * - an integer array positions of length n (positions are not directly used in the calculation,
 *   but identify the lights and guarantee corresponding time lists belong to different devices), and
 * - a 2D integer array times where times[i] contains the allowed start times for light i.
 *
 * A solution always exists because each times[i] contains at least one value.
 *
 * This problem is well suited for binary search on the answer. For a candidate delay D, determine
 * whether there exists a common time window of length D that contains at least one allowed start
 * time from every light. If yes, try a smaller delay; otherwise, try a larger one.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - positions.length == n
 * - times.length == n
 * - 1 <= times[i].length <= 100000
 * - Sum of all times[i].length over all lights <= 200000
 * - 0 <= positions[i] <= 10^9
 * - 0 <= times[i][j] <= 10^9
 * - Each times[i] is sorted in non-decreasing order
 *
 * Example 1:
 * Input: positions = [2, 5, 9], times = [[1,4,8],[2,6],[5,7,10]]
 * Output: 2
 * Explanation: Choose times 4, 6, and 5. The earliest is 4 and the latest is 6, so the delay is 2.
 * No selection can achieve delay 1.
 *
 * Example 2:
 * Input: positions = [0, 3, 8, 12], times = [[3],[1,4,9],[5,6],[2,8,11]]
 * Output: 3
 * Explanation: One optimal choice is 3, 4, 5, and 2. The earliest chosen time is 2 and the latest
 * is 5, so the delay is 3. It is impossible to pick one time from every list within any window of
 * length 2.
 */

public class Solution {

    /**
     * Computes the minimum possible synchronization delay.
     *
     * Core idea:
     * We binary search the answer D = maximum allowed width of the chosen time window.
     * For a fixed D, we check whether there exists some interval [L, L + D] that contains
     * at least one allowed time from every light.
     *
     * Why the check works:
     * - If such a window exists, then we can choose one time from each list inside that window,
     *   so the difference between the minimum and maximum chosen times is at most D.
     * - If no such window exists, then delay D is impossible.
     *
     * @param positions positions of the lights; not used in the computation, but included to match the problem input
     * @param times sorted allowed start times for each light
     * @return the minimum possible synchronization delay
     *
     * Time complexity:
     * Let M be the total number of values across all lists.
     * Binary search performs O(log R) checks, where R is the value range (up to 1e9).
     * Each check runs in O(M log M) due to sorting interval events.
     * Total: O(M log M log R)
     *
     * Space complexity:
     * O(M) for storing interval events during a feasibility check.
     */
    public int minimumSignalDelay(int[] positions, int[][] times) {
        int globalMin = Integer.MAX_VALUE;
        int globalMax = Integer.MIN_VALUE;

        // Find the smallest and largest allowed times across all lights.
        // This gives a safe binary search range for the answer:
        // minimum delay is at least 0, maximum delay is at most globalMax - globalMin.
        for (int[] list : times) {
            globalMin = Math.min(globalMin, list[0]);
            globalMax = Math.max(globalMax, list[list.length - 1]);
        }

        int left = 0;
        int right = globalMax - globalMin;
        int answer = right;

        // Standard binary search on the answer.
        // If delay "mid" is feasible, try smaller.
        // Otherwise, try larger.
        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (canSynchronizeWithinDelay(times, mid)) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether there exists a time window of length delay that contains
     * at least one allowed time from every light.
     *
     * Detailed idea:
     * For one specific allowed time t from a light, if we want t to lie inside a window [L, L + delay],
     * then L must satisfy:
     *
     *     L <= t <= L + delay
     *
     * Rearranging:
     *
     *     t - delay <= L <= t
     *
     * So each allowed time t contributes a valid interval of possible window-start values L:
     *
     *     [t - delay, t]
     *
     * For a given light, we only need L to belong to at least one of its intervals.
     * Therefore, for each light, we merge all of its intervals [t - delay, t].
     * After merging, we get a union of disjoint intervals representing all window starts L
     * that work for that light.
     *
     * The delay is feasible if and only if there exists some L that belongs to the union
     * for every light simultaneously.
     *
     * To test that efficiently:
     * - Convert each merged interval [a, b] into two sweep-line events:
     *      +1 at a
     *      -1 at b + 1
     * - Sweep from left to right over event positions.
     * - Maintain how many lights currently cover the current region.
     * - If coverage ever reaches n, then some L works for all lights.
     *
     * Important implementation detail:
     * We merge intervals separately for each light first. This prevents counting the same light
     * multiple times at the same L when several of its allowed times overlap.
     *
     * @param times sorted allowed start times for each light
     * @param delay candidate synchronization delay
     * @return true if delay is feasible, false otherwise
     *
     * Time complexity:
     * O(M + K log K), where M is the total number of times and K is the number of merged intervals/events.
     * Since K <= M, this is O(M log M) in the worst case.
     *
     * Space complexity:
     * O(M) for the event list.
     */
    public boolean canSynchronizeWithinDelay(int[][] times, int delay) {
        int n = times.length;

        // Each event is stored as:
        // [position, delta]
        // delta = +1 means coverage starts
        // delta = -1 means coverage ends just before this position
        List<long[]> events = new ArrayList<>();

        // Process each light independently.
        for (int[] list : times) {
            // Because list is sorted, the intervals [t - delay, t] are also sorted by start and end.
            // We merge overlapping intervals for this single light.
            long currentStart = (long) list[0] - delay;
            long currentEnd = list[0];

            for (int i = 1; i < list.length; i++) {
                long start = (long) list[i] - delay;
                long end = list[i];

                // If the next interval overlaps or touches the current merged interval,
                // merge them into one larger interval.
                //
                // We use start <= currentEnd because intervals are inclusive.
                // Example:
                // current = [2, 5], next = [5, 7]
                // They share point 5, so they should be merged into [2, 7].
                if (start <= currentEnd) {
                    currentEnd = Math.max(currentEnd, end);
                } else {
                    // Finalize the previous merged interval.
                    addIntervalEvents(events, currentStart, currentEnd);

                    // Start a new merged interval.
                    currentStart = start;
                    currentEnd = end;
                }
            }

            // Add the last merged interval for this light.
            addIntervalEvents(events, currentStart, currentEnd);
        }

        // Sort events by position.
        // If two events share the same position, process +1 before -1.
        // This is a safe tie-breaker, although with our [start, end+1) encoding,
        // exact correctness already follows from the half-open interpretation.
        events.sort((a, b) -> {
            if (a[0] != b[0]) {
                return Long.compare(a[0], b[0]);
            }
            return Long.compare(b[1], a[1]);
        });

        int coveredLights = 0;
        int index = 0;

        // Sweep through all distinct event positions.
        while (index < events.size()) {
            long position = events.get(index)[0];

            // Apply all events at this same position together.
            while (index < events.size() && events.get(index)[0] == position) {
                coveredLights += (int) events.get(index)[1];
                index++;
            }

            // After applying events at "position", coverage count describes the half-open segment
            // [position, nextEventPosition). If coveredLights == n, then every light allows some
            // common window start L in this region, so delay is feasible.
            if (coveredLights == n) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds sweep-line events for one inclusive interval [start, end].
     *
     * We convert the inclusive interval into half-open form [start, end + 1):
     * - +1 at start
     * - -1 at end + 1
     *
     * This makes the sweep logic simple and avoids off-by-one mistakes.
     *
     * @param events list of sweep events
     * @param start inclusive interval start
     * @param end inclusive interval end
     * @return nothing; events are appended to the list
     *
     * Time complexity:
     * O(1)
     *
     * Space complexity:
     * O(1) additional, aside from the appended events
     */
    public void addIntervalEvents(List<long[]> events, long start, long end) {
        events.add(new long[]{start, 1});
        events.add(new long[]{end + 1, -1});
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 2
     * Example 2 -> 3
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity:
     * Depends on the sample sizes; negligible for demonstration.
     *
     * Space complexity:
     * Depends on the sample sizes; negligible for demonstration.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] positions1 = {2, 5, 9};
        int[][] times1 = {
            {1, 4, 8},
            {2, 6},
            {5, 7, 10}
        };
        int result1 = solution.minimumSignalDelay(positions1, times1);
        System.out.println(result1); // Expected: 2

        int[] positions2 = {0, 3, 8, 12};
        int[][] times2 = {
            {3},
            {1, 4, 9},
            {5, 6},
            {2, 8, 11}
        };
        int result2 = solution.minimumSignalDelay(positions2, times2);
        System.out.println(result2); // Expected: 3
    }
}