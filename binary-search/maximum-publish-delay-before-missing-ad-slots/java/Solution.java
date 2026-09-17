import java.util.*;

/*
Problem Title: Maximum Publish Delay Before Missing Ad Slots

Problem Description:
A news platform has already reserved several ad slots during the day. The i-th slot opens at time slotStart[i]
and remains available until time slotEnd[i], inclusive. Before each slot can be used, the publishing system
must finish generating a page variant, which takes exactly renderTime minutes. The system processes all
reserved slots in the given order, and it can start working on the first page only after waiting for some
initial delay D minutes.

Once the first page starts after delay D, the system renders page 0, then page 1, then page 2, and so on,
back-to-back with no idle time between pages. A slot is successfully used if the render for its page finishes
at some time t such that slotStart[i] <= t <= slotEnd[i].

Your task is to find the maximum integer delay D such that every reserved slot can still be used successfully.
If even D = 0 is impossible, return -1.

Formally, page i finishes at time D + (i + 1) * renderTime. This finish time must lie inside the i-th interval.

Constraints:
- 1 <= n <= 200000
- slotStart.length == slotEnd.length == n
- 1 <= slotStart[i] <= slotEnd[i] <= 10^18
- 1 <= renderTime <= 10^18
- The intervals are given in the required processing order and are not necessarily sorted by start or end value.

Example 1:
Input: slotStart = [5, 11, 17], slotEnd = [9, 15, 21], renderTime = 3
Output: 6

Example 2:
Input: slotStart = [4, 8, 10], slotEnd = [5, 9, 11], renderTime = 3
Output: -1
*/

public class Solution {

    /**
     * Finds the maximum non-negative integer delay D such that for every page i,
     * the finish time D + (i + 1) * renderTime lies inside [slotStart[i], slotEnd[i]].
     *
     * Core idea:
     * For each index i, D must satisfy:
     *   slotStart[i] <= D + (i + 1) * renderTime <= slotEnd[i]
     *
     * Rearranging:
     *   slotStart[i] - (i + 1) * renderTime <= D <= slotEnd[i] - (i + 1) * renderTime
     *
     * So every page contributes a valid interval for D.
     * We need the largest integer D that belongs to the intersection of all these intervals
     * and also satisfies D >= 0.
     *
     * Although this can be solved directly by interval intersection, this implementation
     * intentionally uses a feasibility check plus binary search, as requested.
     *
     * @param slotStart array where slotStart[i] is the inclusive opening time of slot i
     * @param slotEnd array where slotEnd[i] is the inclusive closing time of slot i
     * @param renderTime exact rendering time for each page
     * @return the maximum valid delay D, or -1 if even D = 0 is impossible
     * Time complexity: O(n log U), where U is the binary-search range (up to about 10^18)
     * Space complexity: O(1) extra space
     */
    public long maximumPublishDelay(long[] slotStart, long[] slotEnd, long renderTime) {
        int n = slotStart.length;

        // First, check the smallest allowed delay: D = 0.
        // If even this fails, then no non-negative delay can work.
        if (!canUseAllSlots(slotStart, slotEnd, renderTime, 0L)) {
            return -1L;
        }

        // We now know at least one answer exists.
        // We need an upper bound for binary search.
        //
        // For each i:
        //   D <= slotEnd[i] - (i + 1) * renderTime
        //
        // Therefore the true answer cannot exceed the minimum of those values.
        // We compute that minimum safely.
        long high = Long.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            long finishOffset = safeMultiply(i + 1L, renderTime);
            long upperForThisIndex = slotEnd[i] - finishOffset;
            if (upperForThisIndex < high) {
                high = upperForThisIndex;
            }
        }

        // Since D must be non-negative and D=0 already works, high should be >= 0.
        // But for safety, if something unexpected happens, clamp it.
        if (high < 0) {
            return -1L;
        }

        long low = 0L;
        long answer = 0L;

        // Standard binary search on the answer.
        // We search for the largest D such that canUseAllSlots(...) is true.
        while (low <= high) {
            long mid = low + ((high - low) >>> 1);

            if (canUseAllSlots(slotStart, slotEnd, renderTime, mid)) {
                // mid works, so it is a candidate answer.
                // Try to push delay even larger.
                answer = mid;
                low = mid + 1;
            } else {
                // mid fails, so all larger values also fail.
                // Why? Because increasing D shifts every finish time later,
                // making it harder to stay within upper bounds.
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given delay D allows every page to finish inside its corresponding slot.
     *
     * For page i, the finish time is:
     *   finish = delay + (i + 1) * renderTime
     *
     * This finish time must satisfy:
     *   slotStart[i] <= finish <= slotEnd[i]
     *
     * Important monotonicity property:
     * If a certain delay D works, then any smaller non-negative delay may or may not work.
     * But if D fails because some finish time is too late, any larger delay will also fail.
     * This gives the monotone structure needed for binary search over the maximum valid D.
     *
     * @param slotStart array of inclusive slot start times
     * @param slotEnd array of inclusive slot end times
     * @param renderTime exact rendering time per page
     * @param delay candidate initial delay D
     * @return true if all pages finish within their slots, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean canUseAllSlots(long[] slotStart, long[] slotEnd, long renderTime, long delay) {
        int n = slotStart.length;

        // We evaluate each page in order.
        // For page i:
        //   finishTime = delay + (i + 1) * renderTime
        //
        // Then we simply verify whether finishTime lies inside [slotStart[i], slotEnd[i]].
        for (int i = 0; i < n; i++) {
            long finishOffset = safeMultiply(i + 1L, renderTime);
            long finishTime = delay + finishOffset;

            // If the finish time is earlier than the slot opening,
            // then this page is not ready in time to use the slot.
            if (finishTime < slotStart[i]) {
                return false;
            }

            // If the finish time is later than the slot closing,
            // then the slot has already been missed.
            if (finishTime > slotEnd[i]) {
                return false;
            }
        }

        // Every page matched its slot window.
        return true;
    }

    /**
     * Safely multiplies two non-negative long values under the problem's constraints.
     *
     * In this problem, values are intended to fit in signed 64-bit arithmetic for valid test data.
     * This helper exists mainly to make the code clearer and to centralize the multiplication step.
     *
     * @param a first factor
     * @param b second factor
     * @return a * b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long safeMultiply(long a, long b) {
        return a * b;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 6
     * Example 2 -> -1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log U) across the demonstrations
     * Space complexity: O(1) extra space excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] slotStart1 = {5, 11, 17};
        long[] slotEnd1 = {9, 15, 21};
        long renderTime1 = 3;
        long result1 = solution.maximumPublishDelay(slotStart1, slotEnd1, renderTime1);
        System.out.println(result1); // Expected: 6

        long[] slotStart2 = {4, 8, 10};
        long[] slotEnd2 = {5, 9, 11};
        long renderTime2 = 3;
        long result2 = solution.maximumPublishDelay(slotStart2, slotEnd2, renderTime2);
        System.out.println(result2); // Expected: -1
    }
}