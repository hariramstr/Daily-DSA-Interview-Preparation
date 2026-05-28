```java
/*
 * Title: Maximum Score from Deleting Overlapping Intervals in a Window
 *
 * Problem Description:
 * You are given a list of n sensor readings, where each reading is represented as an integer array
 * readings[i] = [start, end, value]. The start and end values represent a time range (inclusive)
 * during which the sensor is active, and value represents the score contributed by that sensor.
 *
 * You are also given an integer W, representing the size of a sliding time window. For each window
 * of size W (i.e., every contiguous time range [t, t+W-1]), you must select a non-overlapping
 * subset of sensors that are fully contained within the window (i.e., start >= t and end <= t+W-1).
 * The goal is to maximize the total score from the selected non-overlapping sensors.
 *
 * Return an array result where result[i] is the maximum score achievable in the window starting at time i.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - 0 <= start <= end <= 10^4
 * - 1 <= value <= 10^4
 * - 1 <= W <= 10^4
 * - Time ranges start from 0 and the last window starts at max(end) - W + 1
 *
 * Example 1:
 * Input: readings = [[0,2,10],[1,3,5],[2,4,8],[3,5,6]], W = 4
 * Output: [18, 14, 14]
 *
 * Example 2:
 * Input: readings = [[0,1,7],[0,3,10],[2,3,6],[4,5,9]], W = 4
 * Output: [13, 15, 9]
 */

import java.util.*;

/**
 * Solution class for Maximum Score from Deleting Overlapping Intervals in a Window.
 *
 * <p>Approach:
 * For each sliding window [t, t+W-1], we:
 * 1. Filter sensors fully contained within the window.
 * 2. Use dynamic programming (weighted interval scheduling) to find the maximum
 *    score from non-overlapping sensors within that window.
 *
 * <p>The weighted interval scheduling DP works as follows:
 * - Sort intervals by end time.
 * - For each interval i, find the latest interval j that ends before interval i starts.
 * - dp[i] = max(dp[i-1], value[i] + dp[j])
 */
public class Solution {

    /**
     * Computes the maximum score for each sliding window of size W.
     *
     * <p>For each window starting at time t (from 0 to maxEnd - W + 1), we:
     * 1. Collect all sensors fully inside [t, t+W-1].
     * 2. Run weighted interval scheduling DP on those sensors.
     * 3. Store the result.
     *
     * @param readings 2D array where readings[i] = [start, end, value]
     * @param W        the size of the sliding time window
     * @return an int array where result[i] is the max score for window starting at i
     *
     * Time complexity: O(T * (n log n + n^2)) where T = number of windows, n = number of readings
     * Space complexity: O(n) for the filtered list and DP array per window
     */
    public int[] maxScoreWindows(int[][] readings, int W) {
        // -----------------------------------------------------------------------
        // Step 1: Find the maximum end time across all readings.
        // This determines how many windows we need to process.
        // -----------------------------------------------------------------------
        int maxEnd = 0;
        for (int[] r : readings) {
            maxEnd = Math.max(maxEnd, r[1]);
        }

        // -----------------------------------------------------------------------
        // Step 2: Determine the number of windows.
        // Windows start at t = 0, 1, 2, ..., (maxEnd - W + 1).
        // If maxEnd < W - 1, there's only 1 window starting at 0.
        // -----------------------------------------------------------------------
        int numWindows = maxEnd - W + 2; // number of valid window start positions
        if (numWindows <= 0) {
            numWindows = 1; // at least one window
        }

        int[] result = new int[numWindows];

        // -----------------------------------------------------------------------
        // Step 3: For each window starting at time t, compute the max score.
        // -----------------------------------------------------------------------
        for (int t = 0; t < numWindows; t++) {
            int windowStart = t;
            int windowEnd = t + W - 1;

            // -------------------------------------------------------------------
            // Step 3a: Filter sensors that are FULLY contained in [windowStart, windowEnd].
            // A sensor [s, e, v] is fully contained if s >= windowStart AND e <= windowEnd.
            // -------------------------------------------------------------------
            List<int[]> contained = new ArrayList<>();
            for (int[] r : readings) {
                if (r[0] >= windowStart && r[1] <= windowEnd) {
                    contained.add(r);
                }
            }

            // -------------------------------------------------------------------
            // Step 3b: If no sensors are contained, score is 0.
            // -------------------------------------------------------------------
            if (contained.isEmpty()) {
                result[t] = 0;
                continue;
            }

            // -------------------------------------------------------------------
            // Step 3c: Run weighted interval scheduling DP on the contained sensors.
            // -------------------------------------------------------------------
            result[t] = weightedIntervalScheduling(contained);
        }

        return result;
    }

    /**
     * Solves the weighted interval scheduling problem using dynamic programming.
     *
     * <p>Algorithm:
     * 1. Sort intervals by their end time.
     * 2. Build a DP array where dp[i] = max score using intervals from index 0..i.
     * 3. For each interval i, we either:
     *    a. Skip it: dp[i] = dp[i-1]
     *    b. Include it: dp[i] = value[i] + dp[p(i)], where p(i) is the index of the
     *       last interval that ends strictly before interval i starts (binary search).
     * 4. Return dp[n-1].
     *
     * @param intervals list of [start, end, value] arrays
     * @return the maximum total value from a non-overlapping subset
     *
     * Time complexity: O(n log n) for sorting + O(n log n) for binary searches = O(n log n)
     * Space complexity: O(n) for the DP array
     */
    private int weightedIntervalScheduling(List<int[]> intervals) {
        int n = intervals.size();

        // -----------------------------------------------------------------------
        // Step 1: Sort intervals by end time (ascending).
        // This is the standard approach for interval scheduling DP.
        // -----------------------------------------------------------------------
        intervals.sort((a, b) -> {
            if (a[1] != b[1]) return a[1] - b[1]; // sort by end time
            return a[0] - b[0];                    // tie-break by start time
        });

        // -----------------------------------------------------------------------
        // Step 2: Extract end times into an array for binary search.
        // We need to quickly find the latest interval that ends before a given start.
        // -----------------------------------------------------------------------
        int[] endTimes = new int[n];
        for (int i = 0; i < n; i++) {
            endTimes[i] = intervals.get(i)[1];
        }

        // -----------------------------------------------------------------------
        // Step 3: Build the DP array.
        // dp[i] = maximum score considering intervals 0..i (1-indexed for convenience).
        // dp[0] = 0 means "no intervals selected" base case.
        // dp[i] corresponds to intervals.get(i-1).
        // -----------------------------------------------------------------------
        int[] dp = new int[n + 1];
        dp[0] = 0; // base case: no intervals, score = 0

        for (int i = 1; i <= n; i++) {
            int[] curr = intervals.get(i - 1); // current interval (0-indexed)
            int currStart = curr[0];
            int currEnd   = curr[1];
            int currValue = curr[2];

            // -------------------------------------------------------------------
            // Step 3a: Find p(i) = the largest j such that interval j ends
            // strictly BEFORE currStart (i.e., endTimes[j-1] < currStart).
            // We use binary search on endTimes[0..i-2] (the first i-1 end times).
            //
            // Two intervals [s1,e1] and [s2,e2] are NON-overlapping if e1 < s2 or e2 < s1.
            // Since we sorted by end time, we look for the rightmost interval
            // whose end time is strictly less than currStart.
            // -------------------------------------------------------------------
            int p = findLatestNonOverlapping(endTimes, i - 1, currStart);
            // p is the 1-indexed position in dp: dp[p] = best score using intervals 1..p

            // -------------------------------------------------------------------
            // Step 3b: DP recurrence:
            // Option A: Don't include current interval → dp[i] = dp[i-1]
            // Option B: Include current interval → dp[i] = currValue + dp[p]
            // Take the maximum.
            // -------------------------------------------------------------------
            int includeScore = currValue + dp[p];
            int excludeScore = dp[i - 1];
            dp[i] = Math.max(includeScore, excludeScore);
        }

        // -----------------------------------------------------------------------
        // Step 4: The answer is dp[n], the best score using all n intervals.
        // -----------------------------------------------------------------------
        return dp[n];
    }

    /**
     * Binary search to find the latest interval (by index) whose end time is
     * strictly less than the given start time.
     *
     * <p>We search in endTimes[0..count-1] for the rightmost value < targetStart.
     *
     * @param endTimes    array of end times (sorted ascending)
     * @param count       number of valid entries to consider (0..count-1)
     * @param targetStart the start time of the current interval
     * @return 1-indexed position p such that dp[p] is the best score for
     *         intervals ending before targetStart; returns 0 if none found
     *
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    private int findLatestNonOverlapping(int[] endTimes, int count, int targetStart) {
        // Binary search for the rightmost index where endTimes[index] < targetStart.
        // We search in [0, count-1].
        int lo = 0, hi = count - 1, result = 0;

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (endTimes[mid] < targetStart) {
                // endTimes[mid] < targetStart means interval mid+1 (1-indexed) is compatible.
                // Record this as a candidate and try to find a later one.
                result = mid + 1; // convert to 1-indexed dp position
                lo = mid + 1;
            } else {
                // endTimes[mid] >= targetStart means this interval overlaps; go left.
                hi = mid - 1;
            }
        }

        return result;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // readings = [[0,2,10],[1,3,5],[2,4,8],[3,5,6]], W = 4
        // Expected Output: [18, 14, 14]
        //
        // Let's trace through:
        // maxEnd = 5, numWindows = 5 - 4 + 2 = 3 → windows at t=0,1,2
        //
        // Window [0,3]: sensors with start>=0, end<=3:
        //   [0,2,10], [1,3,5]
        //   Sort by end: [0,2,10], [1,3,5]
        //   dp[1] = max(dp[0], 10+dp[0]) = max(0,10) = 10
        //   For [1,3,5]: find latest ending < 1 → none → p=0
        //   dp[2] = max(dp[1], 5+dp[0]) = max(10, 5) = 10
        //   Result = 10
        //
        // Hmm, but expected is 18. Let me re-read the problem...
        // The example explanation says "select [0,2,10] + [3,5,6]" but then says
        // [3,5,6] ends at 5 which is outside [0,3]. The explanation seems inconsistent.
        // Let me re-check: window [0,3] with W=4 means [0, 0+4-1] = [0,3].
        // Sensors fully inside: start>=0, end<=3: [0,2,10] and [1,3,5].
        // Best non-overlapping: [0,2,10] alone=10, [1,3,5] alone=5,
        // Can we pick both? [0,2] and [1,3] overlap (1<=2), so no.
        // Score = 10.
        //
        // But expected output says 18 for window 0. Let me re-examine...
        // Perhaps the problem uses a different overlap definition or the example
        // output in the problem description has errors. Let me check window [1,4]:
        // Sensors: start>=1, end<=4: [1,3,5], [2,4,8]
        // [1,3] and [2,4] overlap → can't pick both.
        // Best = max(5, 8) = 8. But expected says 14.
        //
        // Window [2,5]: start>=2, end<=5: [2,4,8], [3,5,6]
        // [2,4] and [3,5]: do they overlap? 3<=4, yes they overlap.
        // Best = max(8,6) = 8. But expected says 14.
        //
        // The expected output [18,14,14] doesn't match standard interval scheduling.
        // Perhaps the problem allows touching intervals (end == start) to be non-overlapping?
        // Let's try: two intervals [s1,e1] and [s2,e2] are non-overlapping if e1 < s2 or e2 < s1.
        // That's what we use. Let me try e1 <= s2 (touching allowed):
        //
        // Window [0,3]: [0,2,10] and [1,3,5]: e1=2, s2=1 → 2 <= 1? No. Still overlap.
        //
        // Hmm. Let me try another interpretation: maybe intervals [s,e] mean the sensor
        // occupies time points s, s+1, ..., e, and two sensors are non-overlapping if
        // their time ranges don't share any point. That's the same as e1 < s2.
        //
        // Let me try: non-overlapping means e1 <= s2 (end of one <= start of next):
        // Window [0,3]: [0,2,10] and [1,3,5]: 2 <= 1? No.
        // Still can't combine them.
        //
        // Wait - maybe the example output in the problem is wrong/inconsistent with
        // the explanation. Let me just implement the correct algorithm and verify
        // with Example 2 which has a clearer explanation.
        //
        // Example 2: readings = [[0,1,7],[0,3,10],[2,3,6],[4,5,9]], W = 4
        // Expected: [13, 15, 9]
        // maxEnd=5, numWindows = 5-4+2 = 3 → t=0,1,2
        //
        // Window [