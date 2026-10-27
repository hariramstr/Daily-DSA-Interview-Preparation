import java.util.*;

/*
Problem Title: Maximum Launch Delay Before Missing Any Satellite Window

Problem Description:
A spacecraft must complete a fixed sequence of communication passes with ground stations.
There are n passes, and pass i must be started no later than windowClose[i].
The passes must be completed in order.

Traveling from pass i to pass i + 1 takes travel[i] minutes, and performing pass i itself
takes duration[i] minutes.

Before the first pass begins, the entire mission may be delayed by some integer number of
minutes D due to launch scheduling. After that, the timeline is deterministic:
- pass 0 starts at time D
- it finishes after duration[0]
- then the craft spends travel[0] minutes to reach the next station
- then performs pass 1
- and so on

A delay D is feasible if every pass i starts at or before windowClose[i].

Task:
Compute the maximum feasible integer launch delay D.
If even D = 0 is impossible, return -1.

Constraints:
- 1 <= n <= 200000
- 0 <= duration[i] <= 10^9
- 0 <= travel[i] <= 10^9 for 0 <= i < n - 1
- 0 <= windowClose[i] <= 10^18
- All values are integers
- The answer must fit in a signed 64-bit integer

Key Observation:
If we know the start time of pass 0 is D, then the start time of pass i is:

start[i] = D + prefixOffset[i]

where prefixOffset[i] is the total time spent before starting pass i:
duration[0] + travel[0] + duration[1] + travel[1] + ... + duration[i - 1] + travel[i - 1]

For D to be feasible, we need:
D + prefixOffset[i] <= windowClose[i] for every i

So:
D <= windowClose[i] - prefixOffset[i] for every i

Therefore, the maximum feasible D is the minimum of all:
windowClose[i] - prefixOffset[i]

If that value is negative, then even D = 0 is impossible, so answer is -1.

Although the problem is designed around binary search, we also provide a binary-search-based
solution exactly as requested, using a feasibility check.
*/

public class Solution {

    /**
     * Computes the maximum feasible integer launch delay using binary search.
     *
     * The idea is:
     * 1. If a delay D works, then every smaller delay also works.
     *    This monotonic property makes binary search valid.
     * 2. We binary search the largest D such that all pass start times are within their windows.
     *
     * @param duration the time spent performing each pass; duration[i] is the duration of pass i
     * @param travel the travel time between consecutive passes; travel[i] is time from pass i to pass i+1
     * @param windowClose the latest allowed start time for each pass; pass i must start at or before windowClose[i]
     * @return the maximum feasible integer launch delay D, or -1 if even D = 0 is impossible
     *
     * Time complexity: O(n log M), where n is the number of passes and M is the search range
     * Space complexity: O(1) extra space
     */
    public long maximumLaunchDelay(long[] duration, long[] travel, long[] windowClose) {
        validateInput(duration, travel, windowClose);

        // First, check whether delay 0 is feasible.
        // If not, the problem statement requires us to return -1.
        if (!isFeasible(0L, duration, travel, windowClose)) {
            return -1L;
        }

        // We need an upper bound for binary search.
        //
        // A safe and tight upper bound can be derived from the first pass:
        // pass 0 starts exactly at time D, so we must have D <= windowClose[0].
        //
        // Therefore, the answer can never exceed windowClose[0].
        long low = 0L;
        long high = windowClose[0];
        long answer = 0L;

        // Standard "find maximum feasible value" binary search.
        while (low <= high) {
            // Use this form to avoid overflow:
            long mid = low + (high - low) / 2;

            // If mid is feasible, try to push delay larger.
            if (isFeasible(mid, duration, travel, windowClose)) {
                answer = mid;
                low = mid + 1;
            } else {
                // Otherwise, mid is too large, so search smaller values.
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given launch delay D is feasible.
     *
     * A delay is feasible if every pass starts at or before its corresponding windowClose.
     *
     * Detailed timeline logic:
     * - pass 0 starts at time D
     * - after pass 0, time increases by duration[0]
     * - then travel[0] is added before pass 1 starts
     * - and so on
     *
     * We only care about start times, not finish times.
     *
     * @param delay the candidate launch delay D to test
     * @param duration the duration of each pass
     * @param travel the travel time between passes
     * @param windowClose the latest allowed start time for each pass
     * @return true if the given delay is feasible; false otherwise
     *
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean isFeasible(long delay, long[] duration, long[] travel, long[] windowClose) {
        // "currentStart" will represent the start time of the current pass.
        long currentStart = delay;

        // We process passes in order because the mission order is fixed.
        for (int i = 0; i < duration.length; i++) {
            // Step 1:
            // Check whether pass i starts on time.
            // The rule is: start time must be <= windowClose[i].
            if (currentStart > windowClose[i]) {
                return false;
            }

            // Step 2:
            // If this is not the last pass, compute the start time of the next pass.
            //
            // nextStart = currentStart + duration[i] + travel[i]
            //
            // We use safe addition because values can be large.
            if (i < duration.length - 1) {
                currentStart = safeAdd(currentStart, duration[i]);
                currentStart = safeAdd(currentStart, travel[i]);
            }
        }

        // If every pass met its window, the delay is feasible.
        return true;
    }

    /**
     * Computes the maximum feasible integer launch delay directly in O(n),
     * without binary search, using the mathematical observation:
     *
     * For each pass i:
     * D + prefixOffset[i] <= windowClose[i]
     * so
     * D <= windowClose[i] - prefixOffset[i]
     *
     * Therefore, the maximum feasible D is the minimum over all passes of:
     * windowClose[i] - prefixOffset[i]
     *
     * If that minimum is negative, then even D = 0 is impossible.
     *
     * This method is included for educational completeness and cross-checking.
     *
     * @param duration the duration of each pass
     * @param travel the travel time between passes
     * @param windowClose the latest allowed start time for each pass
     * @return the maximum feasible integer launch delay D, or -1 if impossible
     *
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public long maximumLaunchDelayDirect(long[] duration, long[] travel, long[] windowClose) {
        validateInput(duration, travel, windowClose);

        long prefixOffset = 0L;
        long best = Long.MAX_VALUE;

        for (int i = 0; i < duration.length; i++) {
            long candidate = windowClose[i] - prefixOffset;
            best = Math.min(best, candidate);

            if (i < duration.length - 1) {
                prefixOffset = safeAdd(prefixOffset, duration[i]);
                prefixOffset = safeAdd(prefixOffset, travel[i]);
            }
        }

        return best < 0 ? -1L : best;
    }

    /**
     * Validates the input arrays.
     *
     * @param duration the duration array
     * @param travel the travel array
     * @param windowClose the window close array
     * @return nothing
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(long[] duration, long[] travel, long[] windowClose) {
        if (duration == null || travel == null || windowClose == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }

        int n = duration.length;

        if (n == 0) {
            throw new IllegalArgumentException("duration and windowClose must have length at least 1.");
        }

        if (windowClose.length != n) {
            throw new IllegalArgumentException("duration and windowClose must have the same length.");
        }

        if (travel.length != n - 1) {
            throw new IllegalArgumentException("travel must have length n - 1.");
        }
    }

    /**
     * Safely adds two non-negative long values.
     *
     * In this problem, values are designed so the final answer fits in signed 64-bit integer.
     * Still, this helper protects the timeline computation from accidental overflow.
     *
     * @param a the first value
     * @param b the second value
     * @return a + b if it fits in long; otherwise Long.MAX_VALUE
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long safeAdd(long a, long b) {
        if (a > Long.MAX_VALUE - b) {
            return Long.MAX_VALUE;
        }
        return a + b;
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(n log M) per demonstrated binary-search call
     * Space complexity: O(1) extra space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt:
        // duration = [3, 2, 4]
        // travel = [5, 1]
        // windowClose = [4, 12, 20]
        //
        // Start times with D:
        // pass 0: D
        // pass 1: D + 3 + 5 = D + 8
        // pass 2: D + 3 + 5 + 2 + 1 = D + 11
        //
        // Constraints:
        // D <= 4
        // D + 8 <= 12  => D <= 4
        // D + 11 <= 20 => D <= 9
        //
        // So maximum D = 4.
        long[] duration1 = {3, 2, 4};
        long[] travel1 = {5, 1};
        long[] windowClose1 = {4, 12, 20};

        long result1 = solution.maximumLaunchDelay(duration1, travel1, windowClose1);
        System.out.println(result1); // Expected: 4

        // Example 2 corrected impossible version from the prompt notes:
        // duration = [5, 3]
        // travel = [4]
        // windowClose = [3, 8]
        //
        // With D = 0:
        // pass 0 starts at 0, okay since 0 <= 3
        // pass 1 starts at 0 + 5 + 4 = 9, but 9 > 8, so impossible
        //
        // Therefore answer = -1.
        long[] duration2 = {5, 3};
        long[] travel2 = {4};
        long[] windowClose2 = {3, 8};

        long result2 = solution.maximumLaunchDelay(duration2, travel2, windowClose2);
        System.out.println(result2); // Expected: -1

        // Optional extra demonstration:
        // Single pass:
        // start time is exactly D, so answer is simply windowClose[0].
        long[] duration3 = {10};
        long[] travel3 = {};
        long[] windowClose3 = {25};

        long result3 = solution.maximumLaunchDelay(duration3, travel3, windowClose3);
        System.out.println(result3); // Expected: 25

        // Cross-check binary search result with direct O(n) formula on Example 1.
        long directCheck = solution.maximumLaunchDelayDirect(duration1, travel1, windowClose1);
        System.out.println(directCheck); // Expected: 4
    }
}