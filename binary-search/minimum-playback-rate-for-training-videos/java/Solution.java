/*
Title: Minimum Playback Rate for Training Videos
Difficulty: Medium
Topic: Binary Search

Problem Description:
A company needs to finish a sequence of employee training videos within a fixed number of hours before a compliance deadline. You are given an integer array videos where videos[i] is the length of the i-th video in minutes, and an integer h representing the total number of hours available. During each hour, the player runs at a constant integer playback rate r (in minutes of video watched per hour). If a video has length x, it takes ceil(x / r) hours to finish because a partially watched final hour still counts as a full hour block for scheduling purposes. Videos must be completed one by one in the given order, but the playback rate is the same for all videos.

Return the minimum integer playback rate r such that all videos can be finished within h hours. If it is impossible even with an arbitrarily large rate because h is smaller than the number of videos, return -1.

This problem is designed to be solved efficiently. A brute-force scan of all possible rates may be too slow when video lengths are large, so you should exploit the monotonic relationship between playback rate and total required hours.

Constraints:
- 1 <= videos.length <= 100000
- 1 <= videos[i] <= 1000000000
- 1 <= h <= 1000000000

Example 1:
Input: videos = [90, 120, 75], h = 6
Output: 60
Explanation: At rate 45, the required time is ceil(90/45) + ceil(120/45) + ceil(75/45) = 2 + 3 + 2 = 7, which is too slow. At rate 60, the time is 2 + 2 + 2 = 6, so 60 works. At rate 59, the time is also 2 + 3 + 2 = 7. Therefore the minimum valid rate is 60.

Example 2:
Input: videos = [30, 11, 23, 4, 20], h = 5
Output: 30
Explanation: Since there are 5 videos and only 5 hours, each video must fit in a single hour. The longest video is 30 minutes, so the minimum feasible rate is 30.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the minimum integer playback rate needed to finish all videos within h hours.
     *
     * The key idea is binary search on the answer:
     * - If a certain rate works, then any larger rate will also work.
     * - If a certain rate does not work, then any smaller rate will also not work.
     * This monotonic behavior makes binary search the correct and efficient approach.
     *
     * @param videos an array where videos[i] is the length of the i-th video in minutes
     * @param h the total number of hours available
     * @return the minimum integer playback rate that allows finishing all videos within h hours;
     *         returns -1 if it is impossible because h is smaller than the number of videos
     * Time complexity: O(n log M), where n is videos.length and M is the maximum video length
     * Space complexity: O(1), ignoring input storage
     */
    public int minPlaybackRate(int[] videos, int h) {
        // If there are fewer available hours than videos,
        // then it is impossible no matter how large the playback rate becomes.
        // Why? Because each video takes at least 1 hour due to the ceiling rule.
        if (videos == null || videos.length == 0) {
            return -1;
        }
        if (h < videos.length) {
            return -1;
        }

        // The minimum possible playback rate is 1 minute per hour.
        int left = 1;

        // The maximum necessary playback rate is the length of the longest video.
        // At that rate, every video can finish in at most 1 hour.
        int right = getMax(videos);

        // We will binary search for the smallest rate that is feasible.
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Compute how many hours are needed if we use playback rate = mid.
            long neededHours = computeRequiredHours(videos, mid);

            // If the required hours fit within h, then this rate works.
            // Since we want the minimum valid rate, we keep searching on the left side,
            // including mid itself.
            if (neededHours <= h) {
                right = mid;
            } else {
                // Otherwise, this rate is too slow, so we must search larger rates.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the minimum feasible rate.
        return left;
    }

    /**
     * Computes the total number of hours needed to finish all videos at a given playback rate.
     *
     * For each video of length x, the time needed is ceil(x / rate).
     * We compute ceiling division using integer arithmetic:
     * ceil(x / rate) = (x + rate - 1) / rate
     *
     * A long is used for the total because the sum can exceed the range of int.
     *
     * @param videos an array of video lengths
     * @param rate the playback rate in minutes of video watched per hour
     * @return the total number of hours required to finish all videos at the given rate
     * Time complexity: O(n), where n is videos.length
     * Space complexity: O(1)
     */
    public long computeRequiredHours(int[] videos, int rate) {
        long totalHours = 0L;

        // Process each video independently.
        for (int length : videos) {
            // Ceiling division:
            // Example: length = 75, rate = 60
            // (75 + 60 - 1) / 60 = 134 / 60 = 2
            totalHours += (length + (long) rate - 1L) / (long) rate;
        }

        return totalHours;
    }

    /**
     * Returns the maximum value in the videos array.
     *
     * This is used as the upper bound for binary search because a playback rate equal
     * to the longest video length guarantees that each video can be completed in at most 1 hour.
     *
     * @param videos an array of video lengths
     * @return the maximum video length in the array
     * Time complexity: O(n), where n is videos.length
     * Space complexity: O(1)
     */
    public int getMax(int[] videos) {
        int max = 0;

        for (int length : videos) {
            if (length > max) {
                max = length;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log M) per demonstrated test case
     * Space complexity: O(1), ignoring input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] videos1 = {90, 120, 75};
        int h1 = 6;
        int result1 = solution.minPlaybackRate(videos1, h1);
        System.out.println("Example 1:");
        System.out.println("videos = " + Arrays.toString(videos1) + ", h = " + h1);
        System.out.println("Minimum playback rate = " + result1);
        System.out.println("Expected = 60");
        System.out.println();

        int[] videos2 = {30, 11, 23, 4, 20};
        int h2 = 5;
        int result2 = solution.minPlaybackRate(videos2, h2);
        System.out.println("Example 2:");
        System.out.println("videos = " + Arrays.toString(videos2) + ", h = " + h2);
        System.out.println("Minimum playback rate = " + result2);
        System.out.println("Expected = 30");
        System.out.println();

        int[] videos3 = {100, 200, 300};
        int h3 = 2;
        int result3 = solution.minPlaybackRate(videos3, h3);
        System.out.println("Impossible case:");
        System.out.println("videos = " + Arrays.toString(videos3) + ", h = " + h3);
        System.out.println("Minimum playback rate = " + result3);
        System.out.println("Expected = -1");
    }
}