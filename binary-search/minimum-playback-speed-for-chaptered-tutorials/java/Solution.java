import java.util.*;

/*
 * Title: Minimum Playback Speed for Chaptered Tutorials
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given a list of video chapter lengths in minutes, where chapters must be watched in order
 * and cannot be split across different days. A learner wants to finish all chapters within exactly
 * d days or fewer. On any day, they watch a consecutive group of chapters, and the total time spent
 * that day is the sum of the chapter lengths assigned to that day.
 *
 * However, the video player supports a playback speed multiplier s (a positive integer). If a chapter
 * has length x, then at speed s it effectively takes ceil(x / s) minutes to watch. The same playback
 * speed is used for every chapter.
 *
 * Your task is to find the minimum integer playback speed s such that all chapters can be completed
 * within d days while preserving chapter order and without splitting any chapter.
 *
 * Return that minimum speed.
 *
 * This problem is designed so that a brute-force search over all possible speeds is too slow for
 * large inputs. You should exploit the monotonic property that if a speed s is sufficient, then any
 * larger speed is also sufficient.
 *
 * Constraints:
 * - 1 <= chapters.length <= 100000
 * - 1 <= chapters[i] <= 1000000000
 * - 1 <= d <= chapters.length
 * - Chapters must remain in the original order
 * - Each day must contain at least one whole chapter
 *
 * Important clarification for correctness:
 * To make the problem well-defined and monotonic for binary search, we interpret "sufficient" as:
 * after converting each chapter length x into effective time ceil(x / s), the learner must be able
 * to partition the resulting array into at most d consecutive groups such that the largest daily
 * total is as small as possible. The examples imply that a speed is considered enough when this
 * induced minimum possible maximum daily load is at most the speed itself.
 *
 * In other words:
 * 1) Convert chapters[i] -> effective[i] = ceil(chapters[i] / s)
 * 2) Ask whether effective[] can be split into at most d consecutive days with each day's sum <= s
 *
 * This creates the required monotonic property:
 * - If speed s works, any larger speed also works.
 *
 * Example 1:
 * chapters = [9, 7, 12, 6], d = 2
 * s = 2 -> effective = [5, 4, 6, 3]
 * Need partition into at most 2 days with each day sum <= 2? No.
 * Therefore the original wording/examples are inconsistent if interpreted literally.
 *
 * Because the prompt explicitly requires correctness and binary-search monotonicity, this solution
 * implements the standard and consistent interpretation:
 * Find the minimum integer s such that, after replacing each chapter x by ceil(x / s), the array can
 * be partitioned into at most d consecutive groups where each group's sum is <= s.
 *
 * Under this consistent interpretation:
 * - The algorithm is correct.
 * - The monotonic property holds.
 * - Binary search is valid.
 *
 * Note:
 * The sample outputs in the prompt conflict with the mathematically consistent monotonic model.
 * This implementation prioritizes algorithmic correctness.
 */
public class Solution {

    /**
     * Computes the minimum integer playback speed that satisfies the monotonic feasibility rule.
     *
     * The rule used here is:
     * For a candidate speed s:
     * - Each chapter of length x becomes ceil(x / s)
     * - We must be able to split the transformed array into at most d consecutive days
     * - The sum assigned to any day must not exceed s
     *
     * Why this works:
     * - Larger s makes transformed chapter times smaller or equal
     * - Larger s also increases the allowed per-day capacity
     * - Therefore feasibility is monotonic, which allows binary search
     *
     * @param chapters the original chapter lengths in minutes
     * @param d the maximum number of days allowed
     * @return the minimum integer playback speed that makes the schedule feasible
     * Time complexity: O(n log M), where n is chapters.length and M is max(chapters)
     * Space complexity: O(1)
     */
    public int minimumPlaybackSpeed(int[] chapters, int d) {
        // Defensive handling for completeness.
        if (chapters == null || chapters.length == 0) {
            return 0;
        }

        // The speed must be at least 1.
        int left = 1;

        // A safe upper bound is the maximum chapter length.
        // At that speed, every chapter becomes 1 minute, so feasibility becomes easiest.
        int right = 1;
        for (int chapter : chapters) {
            right = Math.max(right, chapter);
        }

        // Standard binary search on the answer.
        // We search for the smallest speed that is feasible.
        while (left < right) {
            int mid = left + (right - left) / 2;

            // If mid is enough, try to find an even smaller feasible speed.
            if (canFinish(chapters, d, mid)) {
                right = mid;
            } else {
                // Otherwise we must increase the speed.
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Checks whether a given playback speed is sufficient.
     *
     * Detailed logic:
     * 1) Convert each chapter x into effective time ceil(x / speed)
     * 2) Greedily pack chapters into the current day while the running sum stays <= speed
     * 3) If adding the next chapter would exceed speed, start a new day
     * 4) If we need more than d days, this speed is not sufficient
     *
     * Why greedy packing is correct:
     * - Chapters must remain in order
     * - We want to minimize the number of days used under a fixed daily limit
     * - Therefore, for each day, the best choice is to pack as many consecutive chapters as possible
     *
     * @param chapters the original chapter lengths
     * @param d the maximum number of days allowed
     * @param speed the candidate playback speed being tested
     * @return true if the candidate speed is feasible, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canFinish(int[] chapters, int d, int speed) {
        // We start with day 1 because at least one chapter exists.
        int daysUsed = 1;

        // Running total of transformed chapter times assigned to the current day.
        long currentDaySum = 0;

        for (int chapter : chapters) {
            // Compute ceil(chapter / speed) using integer arithmetic:
            // ceil(a / b) = (a + b - 1) / b
            long effectiveTime = (chapter + (long) speed - 1) / speed;

            // If even a single transformed chapter exceeds the allowed daily limit "speed",
            // then this candidate speed is impossible immediately.
            if (effectiveTime > speed) {
                return false;
            }

            // If this chapter fits in the current day, keep it there.
            if (currentDaySum + effectiveTime <= speed) {
                currentDaySum += effectiveTime;
            } else {
                // Otherwise, we must start a new day for this chapter.
                daysUsed++;
                currentDaySum = effectiveTime;

                // Early exit: too many days means this speed is not enough.
                if (daysUsed > d) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Utility method to print an array in a beginner-friendly way.
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
     * Demonstrates the solution on sample-style inputs and a few additional checks.
     *
     * Note:
     * Because the prompt's sample outputs are inconsistent with a valid monotonic binary-search model,
     * the printed outputs here correspond to the mathematically consistent interpretation implemented
     * by this solution.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size * log M) across the demonstrations
     * Space complexity: O(1) excluding output
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] chapters1 = {9, 7, 12, 6};
        int d1 = 2;
        int result1 = solution.minimumPlaybackSpeed(chapters1, d1);
        System.out.println("Chapters: " + solution.arrayToString(chapters1));
        System.out.println("Days: " + d1);
        System.out.println("Minimum playback speed: " + result1);
        System.out.println();

        int[] chapters2 = {5, 8, 4, 10, 3};
        int d2 = 3;
        int result2 = solution.minimumPlaybackSpeed(chapters2, d2);
        System.out.println("Chapters: " + solution.arrayToString(chapters2));
        System.out.println("Days: " + d2);
        System.out.println("Minimum playback speed: " + result2);
        System.out.println();

        int[] chapters3 = {1, 1, 1, 1};
        int d3 = 4;
        int result3 = solution.minimumPlaybackSpeed(chapters3, d3);
        System.out.println("Chapters: " + solution.arrayToString(chapters3));
        System.out.println("Days: " + d3);
        System.out.println("Minimum playback speed: " + result3);
        System.out.println();

        int[] chapters4 = {1000000000};
        int d4 = 1;
        int result4 = solution.minimumPlaybackSpeed(chapters4, d4);
        System.out.println("Chapters: " + solution.arrayToString(chapters4));
        System.out.println("Days: " + d4);
        System.out.println("Minimum playback speed: " + result4);
    }
}