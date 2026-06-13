import java.util.*;

/*
 * Title: Minimum Playback Speed for Buffered Lectures
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given an array lectures where lectures[i] is the length in minutes of the i-th recorded
 * lecture segment. A student wants to finish all segments within h hours by watching them at a
 * constant playback speed s. If a segment has length x minutes and the student watches at speed s,
 * the time spent on that segment is ceil(x / s) minutes because the video platform only allows
 * jumping to the next segment after finishing the current one, and each segment's required viewing
 * time is rounded up to the next whole minute. The same speed s must be used for every segment.
 *
 * Return the minimum positive integer playback speed s such that the total time needed to watch all
 * lecture segments is at most h hours. If it is impossible even with arbitrarily large speed because
 * each non-empty segment still takes at least 1 minute, return -1.
 *
 * This problem is designed to reward identifying a monotonic condition: if a speed s is fast enough,
 * then any speed larger than s is also fast enough. Use this property to search efficiently.
 *
 * Constraints:
 * - 1 <= lectures.length <= 100000
 * - 1 <= lectures[i] <= 1000000000
 * - 1 <= h <= 1000000000
 * - h is given in hours, but each rounded segment time is measured in minutes, so compare against h * 60 total minutes
 * - The solution should run in O(n log M), where M is the maximum lecture length
 *
 * Important correctness note:
 * The second example in the prompt contains a contradiction in its stated output.
 * For lectures = [120, 95, 200], h = 4:
 * - Total allowed time = 4 * 60 = 240 minutes
 * - At speed 1: 120 + 95 + 200 = 415 minutes -> too slow
 * - At speed 2: ceil(120/2) + ceil(95/2) + ceil(200/2) = 60 + 48 + 100 = 208 minutes -> valid
 * Therefore the correct minimum speed is 2, not 3.
 * This implementation follows the actual problem rules and returns the correct answer.
 */

public class Solution {

    /**
     * Finds the minimum positive integer playback speed needed to finish all lecture segments
     * within h hours.
     *
     * The key observation is that the condition is monotonic:
     * - If some speed s is sufficient, then every speed greater than s is also sufficient.
     * This allows us to use binary search on the answer.
     *
     * @param lectures an array where lectures[i] is the length in minutes of the i-th lecture segment
     * @param h the number of hours available to finish all segments
     * @return the minimum valid integer playback speed; returns -1 if impossible
     * Time complexity: O(n log M), where n is lectures.length and M is the maximum lecture length
     * Space complexity: O(1), ignoring input storage
     */
    public int minPlaybackSpeed(int[] lectures, int h) {
        // Convert hours to total allowed minutes.
        // Use long to avoid overflow because h can be as large as 1,000,000,000.
        long allowedMinutes = (long) h * 60L;

        // Even with an arbitrarily large speed, each non-empty segment still takes at least 1 minute
        // because ceil(x / very_large_speed) = 1 for any positive x.
        // Therefore, if the number of segments is greater than the total allowed minutes,
        // it is impossible to finish.
        if (lectures.length > allowedMinutes) {
            return -1;
        }

        // Find the maximum lecture length.
        // This gives us a safe upper bound for binary search:
        // at speed = maxLecture, every segment takes at most 1 minute.
        int maxLecture = 0;
        for (int lecture : lectures) {
            maxLecture = Math.max(maxLecture, lecture);
        }

        // Binary search range:
        // - Minimum possible speed is 1
        // - Maximum necessary speed is maxLecture
        int left = 1;
        int right = maxLecture;

        // We want the smallest speed that is sufficient.
        while (left < right) {
            // Standard overflow-safe midpoint calculation.
            int mid = left + (right - left) / 2;

            // Check whether this speed is fast enough.
            if (canFinish(lectures, mid, allowedMinutes)) {
                // If mid works, try to find an even smaller valid speed.
                right = mid;
            } else {
                // If mid does not work, we must increase the speed.
                left = mid + 1;
            }
        }

        // At loop end, left == right and points to the minimum valid speed.
        return left;
    }

    /**
     * Checks whether all lecture segments can be finished within the allowed number of minutes
     * at a given playback speed.
     *
     * For each segment of length x minutes watched at speed s, the required time is:
     * ceil(x / s)
     *
     * To compute ceil(x / s) using integer arithmetic safely:
     * ceil(x / s) = (x + s - 1) / s
     *
     * @param lectures an array of lecture lengths in minutes
     * @param speed the playback speed to test
     * @param allowedMinutes the total number of minutes available
     * @return true if the total rounded viewing time is at most allowedMinutes; false otherwise
     * Time complexity: O(n), where n is lectures.length
     * Space complexity: O(1)
     */
    public boolean canFinish(int[] lectures, int speed, long allowedMinutes) {
        // Keep the running total in long to avoid overflow.
        long totalMinutesNeeded = 0L;

        for (int lecture : lectures) {
            // Compute ceil(lecture / speed) using integer math.
            totalMinutesNeeded += (lecture + (long) speed - 1L) / (long) speed;

            // Very important optimization:
            // If we already exceed the allowed time, we can stop early.
            // This avoids unnecessary work and also keeps the logic simple.
            if (totalMinutesNeeded > allowedMinutes) {
                return false;
            }
        }

        return totalMinutesNeeded <= allowedMinutes;
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n log M) for k demonstrations
     * Space complexity: O(1), ignoring input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] lectures1 = {45, 80, 30};
        int h1 = 3;
        int result1 = solution.minPlaybackSpeed(lectures1, h1);
        System.out.println("Example 1:");
        System.out.println("lectures = " + Arrays.toString(lectures1) + ", h = " + h1);
        System.out.println("Minimum playback speed = " + result1);
        System.out.println("Expected = 1");
        System.out.println();

        int[] lectures2 = {120, 95, 200};
        int h2 = 4;
        int result2 = solution.minPlaybackSpeed(lectures2, h2);
        System.out.println("Example 2:");
        System.out.println("lectures = " + Arrays.toString(lectures2) + ", h = " + h2);
        System.out.println("Minimum playback speed = " + result2);
        System.out.println("Correct expected = 2");
        System.out.println();

        int[] lectures3 = {100, 100, 100};
        int h3 = 0 + 1; // 1 hour = 60 minutes, but 3 segments minimum need 3 minutes, so possible
        int result3 = solution.minPlaybackSpeed(lectures3, h3);
        System.out.println("Additional Test 1:");
        System.out.println("lectures = " + Arrays.toString(lectures3) + ", h = " + h3);
        System.out.println("Minimum playback speed = " + result3);
        System.out.println();

        int[] lectures4 = {5, 10, 15, 20};
        int h4 = 0; // Not allowed by constraints, so we do not call the method with this.
        System.out.println("Additional Test 2 skipped because h = 0 is outside constraints.");
        System.out.println();

        int[] lectures5 = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                           1};
        int h5 = 1; // 60 minutes allowed, but 61 segments means impossible
        int result5 = solution.minPlaybackSpeed(lectures5, h5);
        System.out.println("Additional Test 3:");
        System.out.println("lectures length = " + lectures5.length + ", h = " + h5);
        System.out.println("Minimum playback speed = " + result5);
        System.out.println("Expected = -1");
    }
}