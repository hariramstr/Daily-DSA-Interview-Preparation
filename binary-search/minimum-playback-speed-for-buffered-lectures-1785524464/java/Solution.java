import java.util.*;

/*
 * Title: Minimum Playback Speed for Buffered Lectures
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given a list of lecture video lengths in minutes, where lectures must be watched
 * in the given order. A student has exactly H hours before an exam and wants to finish all
 * lectures on time. The player supports variable playback speed, but the same speed must be
 * used for every lecture.
 *
 * If the playback speed is s, a lecture with length x minutes takes ceil(x / s) minutes to
 * finish, because even a partially watched final minute still consumes a full minute block
 * in the study planner. For example, at speed 3, a 7-minute lecture takes ceil(7 / 3) = 3
 * minutes.
 *
 * Return the minimum positive integer playback speed s such that the total time needed to
 * watch all lectures is at most H * 60 minutes. If it is impossible even with arbitrarily
 * large integer speed under this rounding rule, return -1.
 *
 * This problem is designed to test whether you can recognize a monotonic condition and search
 * over the answer space efficiently.
 *
 * Constraints:
 * - 1 <= lectures.length <= 100000
 * - 1 <= lectures[i] <= 10^9
 * - 1 <= H <= 10^9
 * - All values are integers.
 *
 * Notes:
 * - The student cannot split a lecture across different speeds.
 * - Because of the ceiling rule, each lecture requires at least 1 minute, no matter how large
 *   the speed is.
 * - Therefore, if lectures.length > H * 60, the answer is immediately -1.
 *
 * Example 1:
 * Input: lectures = [30, 11, 23, 4, 20], H = 1
 * Output: 2
 * Explanation:
 * Total available time = 1 * 60 = 60 minutes.
 * At speed 1: 30 + 11 + 23 + 4 + 20 = 88 minutes -> too slow.
 * At speed 2: ceil(30/2) + ceil(11/2) + ceil(23/2) + ceil(4/2) + ceil(20/2)
 *           = 15 + 6 + 12 + 2 + 10
 *           = 45 minutes -> valid.
 * Since speed 1 is invalid and speed 2 is valid, the minimum valid speed is 2.
 *
 * Example 2:
 * Input: lectures = [100, 200, 300], H = 0
 * Output: -1
 * Explanation:
 * Available time is 0 minutes, so it is impossible to watch any positive-length lecture.
 */

public class Solution {

    /**
     * Finds the minimum positive integer playback speed needed to finish all lectures
     * within H hours.
     *
     * The key observation is monotonicity:
     * - If a speed s is sufficient, then any speed greater than s is also sufficient.
     * - If a speed s is not sufficient, then any speed smaller than s is also not sufficient.
     *
     * This monotonic behavior allows us to use binary search on the answer.
     *
     * @param lectures an array where lectures[i] is the length of the i-th lecture in minutes
     * @param H the number of available hours before the exam
     * @return the minimum feasible positive integer playback speed, or -1 if impossible
     * Time complexity: O(n log M), where n is the number of lectures and M is the maximum lecture length
     * Space complexity: O(1), ignoring input storage
     */
    public int minPlaybackSpeed(int[] lectures, int H) {
        // Convert available hours into total available minutes.
        // We use long because H can be large, and H * 60 should be protected from overflow.
        long availableMinutes = (long) H * 60L;

        // If there are no available minutes, it is impossible to watch any positive-length lecture.
        if (availableMinutes <= 0) {
            return -1;
        }

        // Important impossibility check:
        // Even with extremely large speed, each lecture still takes at least 1 minute
        // because of the ceiling rule.
        // Therefore, the absolute minimum total time is exactly the number of lectures.
        if (lectures.length > availableMinutes) {
            return -1;
        }

        // The minimum possible speed is 1.
        int left = 1;

        // The maximum lecture length is always a sufficient upper bound:
        // At speed = maxLectureLength, every lecture takes at most 1 minute,
        // so total time becomes exactly number of lectures, which we already know fits.
        int right = 0;
        for (int lecture : lectures) {
            right = Math.max(right, lecture);
        }

        // Standard binary search for the first valid speed.
        while (left < right) {
            // Midpoint chosen this way to avoid overflow.
            int mid = left + (right - left) / 2;

            // Check whether this candidate speed is fast enough.
            if (canFinish(lectures, availableMinutes, mid)) {
                // If mid works, try to find an even smaller valid speed.
                right = mid;
            } else {
                // If mid does not work, we must search larger speeds.
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right and points to the minimum valid speed.
        return left;
    }

    /**
     * Checks whether all lectures can be finished within the given number of available minutes
     * using a fixed playback speed.
     *
     * For each lecture of length x, the required time is ceil(x / speed).
     * To compute ceiling division using integers safely:
     * ceil(x / speed) = (x + speed - 1) / speed
     *
     * We also stop early if the accumulated time already exceeds the limit.
     *
     * @param lectures an array of lecture lengths in minutes
     * @param availableMinutes the total number of minutes available
     * @param speed the candidate playback speed to test
     * @return true if all lectures can be finished within availableMinutes, otherwise false
     * Time complexity: O(n), where n is the number of lectures
     * Space complexity: O(1)
     */
    public boolean canFinish(int[] lectures, long availableMinutes, int speed) {
        long totalRequiredMinutes = 0L;

        // Process each lecture one by one and accumulate the total required time.
        for (int lecture : lectures) {
            // Ceiling division:
            // Example: lecture = 11, speed = 2
            // (11 + 2 - 1) / 2 = 12 / 2 = 6
            long minutesForThisLecture = (lecture + (long) speed - 1L) / (long) speed;
            totalRequiredMinutes += minutesForThisLecture;

            // Early exit optimization:
            // As soon as we exceed the allowed time, we know this speed is not feasible.
            if (totalRequiredMinutes > availableMinutes) {
                return false;
            }
        }

        return totalRequiredMinutes <= availableMinutes;
    }

    /**
     * Runs a demonstration of the algorithm using sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n log M) for k demonstrations
     * Space complexity: O(1), ignoring input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement.
        // lectures = [30, 11, 23, 4, 20], H = 1
        // Available time = 60 minutes
        // Speed 1 -> 88 minutes (invalid)
        // Speed 2 -> 45 minutes (valid)
        // Therefore answer = 2
        int[] lectures1 = {30, 11, 23, 4, 20};
        int H1 = 1;
        System.out.println("Example 1 result: " + solution.minPlaybackSpeed(lectures1, H1)); // Expected: 2

        // Example 2 from the problem statement.
        // lectures = [100, 200, 300], H = 0
        // Available time = 0 minutes
        // Impossible to watch any positive-length lecture
        int[] lectures2 = {100, 200, 300};
        int H2 = 0;
        System.out.println("Example 2 result: " + solution.minPlaybackSpeed(lectures2, H2)); // Expected: -1

        // Additional demonstration:
        // 3 lectures, 1 hour = 60 minutes
        // At speed 1, total = 10 + 20 + 30 = 60, so answer should be 1
        int[] lectures3 = {10, 20, 30};
        int H3 = 1;
        System.out.println("Additional test 1 result: " + solution.minPlaybackSpeed(lectures3, H3)); // Expected: 1

        // Additional demonstration:
        // 61 lectures, only 1 hour = 60 minutes
        // Since each lecture needs at least 1 minute, impossible
        int[] lectures4 = new int[61];
        Arrays.fill(lectures4, 1);
        int H4 = 1;
        System.out.println("Additional test 2 result: " + solution.minPlaybackSpeed(lectures4, H4)); // Expected: -1
    }
}