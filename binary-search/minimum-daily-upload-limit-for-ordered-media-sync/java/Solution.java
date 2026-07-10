import java.util.*;

/*
Problem Title: Minimum Daily Upload Limit for Ordered Media Sync

Problem Description:
A media company needs to synchronize a sequence of video segments to a remote archive.
The segments must be uploaded in the given order, and each segment is indivisible:
it must be uploaded entirely on a single day. The company has exactly d days to finish
the synchronization.

For each segment i, uploading it consumes uploadSizes[i] units of bandwidth on the day
it is assigned. If the daily upload limit is L, then the total size of all segments
assigned to any single day cannot exceed L. Because the upload order cannot change,
each day receives a contiguous block of segments.

Your task is to find the minimum possible daily upload limit L that allows all segments
to be uploaded within at most d days.

This problem has a monotonic property:
- If a daily limit L is sufficient, then any larger limit is also sufficient.
- If a daily limit L is not sufficient, then any smaller limit is also not sufficient.

That monotonic behavior makes binary search the correct optimization technique.

Constraints:
- 1 <= uploadSizes.length <= 200000
- 1 <= uploadSizes[i] <= 1000000000
- 1 <= d <= uploadSizes.length
- The answer fits in a 64-bit signed integer.
*/
public class Solution {

    /**
     * Finds the minimum daily upload limit needed to upload all segments in order
     * within at most d days.
     *
     * Core idea:
     * 1. The minimum possible limit cannot be smaller than the largest single segment,
     *    because a segment cannot be split across days.
     * 2. The maximum possible limit can be the sum of all segments, which means
     *    uploading everything in one day.
     * 3. We binary search this answer range.
     * 4. For each candidate limit, we greedily simulate how many days are needed.
     *    If the candidate works within d days, we try smaller values.
     *    Otherwise, we must try larger values.
     *
     * @param uploadSizes the sizes of the video segments that must be uploaded in order
     * @param d the maximum number of days allowed
     * @return the minimum daily upload limit that makes the upload possible
     * Time complexity: O(n log S), where n is uploadSizes.length and S is the search range
     * Space complexity: O(1), excluding input storage
     */
    public long minimumDailyUploadLimit(int[] uploadSizes, int d) {
        // The lower bound of the answer:
        // At minimum, the daily limit must be at least the largest single segment,
        // because no segment can be split.
        long left = 0L;

        // The upper bound of the answer:
        // In the worst case, we can upload everything in one day,
        // so the sum of all segment sizes is always a valid upper bound.
        long right = 0L;

        // Build the binary search range.
        for (int size : uploadSizes) {
            left = Math.max(left, size);
            right += size;
        }

        // Standard binary search on the answer space.
        // We are looking for the smallest feasible limit.
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate daily limit is enough.
            if (canUploadWithinDays(uploadSizes, d, mid)) {
                // If mid works, it might be the answer,
                // but there may be an even smaller feasible value.
                right = mid;
            } else {
                // If mid does not work, every smaller value also fails,
                // so we must search the larger half.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the minimum feasible limit.
        return left;
    }

    /**
     * Determines whether all segments can be uploaded in order within at most d days
     * if the daily upload limit is limit.
     *
     * Greedy simulation:
     * - Process segments from left to right.
     * - Keep adding segments to the current day while the total does not exceed limit.
     * - If adding the next segment would exceed limit, start a new day.
     * - Count how many days are needed.
     *
     * Why greedy works:
     * - Since order is fixed and segments are indivisible, the best way to minimize
     *   the number of days for a given limit is to pack each day as much as possible
     *   before moving to the next day.
     *
     * @param uploadSizes the sizes of the video segments
     * @param d the maximum number of days allowed
     * @param limit the candidate daily upload limit being tested
     * @return true if all segments can be uploaded within at most d days, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canUploadWithinDays(int[] uploadSizes, int d, long limit) {
        // Start with day 1 because we begin placing segments immediately.
        int daysUsed = 1;

        // Tracks the total upload size assigned to the current day.
        long currentDayLoad = 0L;

        // Process each segment in order.
        for (int size : uploadSizes) {
            // Safety check:
            // If a single segment is larger than the limit, this limit is impossible.
            // In our binary search, limit is always >= max segment size,
            // but keeping this check makes the helper method robust and self-contained.
            if (size > limit) {
                return false;
            }

            // If the current segment fits in the current day, add it.
            if (currentDayLoad + size <= limit) {
                currentDayLoad += size;
            } else {
                // Otherwise, we must start a new day for this segment.
                daysUsed++;
                currentDayLoad = size;

                // Early exit:
                // If we already exceeded the allowed number of days,
                // there is no need to continue.
                if (daysUsed > d) {
                    return false;
                }
            }
        }

        // If we finished processing all segments without exceeding d days,
        // then this limit is feasible.
        return true;
    }

    /**
     * Utility method to print an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1:
     * uploadSizes = [7, 2, 5, 10, 8], d = 2
     * Answer = 18
     *
     * Example 2:
     * uploadSizes = [4, 4, 4, 4, 4, 4, 4], d = 3
     * Answer = 12
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log S) across the demonstrated examples
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] uploadSizes1 = {7, 2, 5, 10, 8};
        int d1 = 2;
        long result1 = solution.minimumDailyUploadLimit(uploadSizes1, d1);

        System.out.println("Example 1:");
        System.out.println("uploadSizes = " + solution.arrayToString(uploadSizes1));
        System.out.println("d = " + d1);
        System.out.println("Minimum daily upload limit = " + result1);
        System.out.println("Expected = 18");
        System.out.println();

        // Example 2
        int[] uploadSizes2 = {4, 4, 4, 4, 4, 4, 4};
        int d2 = 3;
        long result2 = solution.minimumDailyUploadLimit(uploadSizes2, d2);

        System.out.println("Example 2:");
        System.out.println("uploadSizes = " + solution.arrayToString(uploadSizes2));
        System.out.println("d = " + d2);
        System.out.println("Minimum daily upload limit = " + result2);
        System.out.println("Expected = 12");
        System.out.println();

        // Additional quick sanity checks for beginners:
        // 1) If d equals the number of segments, answer should be the maximum segment size.
        int[] uploadSizes3 = {3, 1, 9, 2};
        int d3 = 4;
        long result3 = solution.minimumDailyUploadLimit(uploadSizes3, d3);

        System.out.println("Additional Check 1:");
        System.out.println("uploadSizes = " + solution.arrayToString(uploadSizes3));
        System.out.println("d = " + d3);
        System.out.println("Minimum daily upload limit = " + result3);
        System.out.println("Expected = 9");
        System.out.println();

        // 2) If d is 1, answer should be the total sum.
        int[] uploadSizes4 = {3, 1, 9, 2};
        int d4 = 1;
        long result4 = solution.minimumDailyUploadLimit(uploadSizes4, d4);

        System.out.println("Additional Check 2:");
        System.out.println("uploadSizes = " + solution.arrayToString(uploadSizes4));
        System.out.println("d = " + d4);
        System.out.println("Minimum daily upload limit = " + result4);
        System.out.println("Expected = 15");
    }
}