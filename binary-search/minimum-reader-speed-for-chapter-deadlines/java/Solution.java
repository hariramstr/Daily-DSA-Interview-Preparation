import java.util.*;

/*
 * Title: Minimum Reader Speed for Chapter Deadlines
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given an array chapters where chapters[i] is the number of pages in the i-th chapter
 * of an online course, and an integer h representing the total number of hours available before
 * all chapter deadlines pass. A student must read the chapters in the given order, but within
 * any single hour they can work on only one chapter. If the student's reading speed is s pages
 * per hour, then finishing a chapter with p pages takes ceil(p / s) whole hours. The student may
 * start the next chapter only after the current one is fully finished.
 *
 * Return the minimum integer reading speed s such that the student can finish all chapters within h hours.
 *
 * It is guaranteed that an answer exists.
 *
 * This is a search problem over the answer space: a higher reading speed never increases the total
 * time needed, so the feasibility of a speed is monotonic.
 *
 * Constraints:
 * - 1 <= chapters.length <= 100000
 * - 1 <= chapters[i] <= 1000000000
 * - chapters.length <= h <= 1000000000
 * - The answer fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: chapters = [120, 90, 150], h = 8
 * Output: 50
 * Explanation:
 * - At speed 60, required hours = ceil(120/60) + ceil(90/60) + ceil(150/60) = 2 + 2 + 3 = 7, which fits.
 * - At speed 59, required hours = 3 + 2 + 3 = 8, which also fits.
 * - At speed 58, required hours = 3 + 2 + 3 = 8, which still fits.
 * - At speed 50, required hours = 3 + 2 + 3 = 8, which fits.
 * - At speed 45, required hours = 3 + 2 + 4 = 9, which does not fit.
 * So the minimum valid speed is 50.
 *
 * Example 2:
 * Input: chapters = [300, 200, 400, 100], h = 10
 * Output: 100
 * Explanation:
 * At speed 100, the total time is 3 + 2 + 4 + 1 = 10 hours exactly. Any speed below 100 causes
 * at least one chapter to take longer, making the total exceed 10.
 */

public class Solution {

    /**
     * Finds the minimum integer reading speed needed to finish all chapters within h hours.
     *
     * The key idea is binary search on the answer:
     * - If a speed works, then every larger speed also works.
     * - If a speed does not work, then every smaller speed also does not work.
     * This monotonic behavior makes binary search the correct and efficient approach.
     *
     * @param chapters an array where chapters[i] is the number of pages in the i-th chapter
     * @param h the maximum total number of hours allowed
     * @return the minimum integer speed (pages per hour) that allows finishing within h hours
     * Time complexity: O(n log M), where n is chapters.length and M is the maximum chapter size
     * Space complexity: O(1)
     */
    public int minReadingSpeed(int[] chapters, int h) {
        // The minimum possible speed is 1 page per hour.
        int left = 1;

        // The maximum necessary speed never needs to exceed the largest chapter size.
        // Why? Because if speed equals the largest chapter size, every chapter takes at most 1 hour.
        int right = getMaxChapterPages(chapters);

        // We will shrink the search space until left == right.
        // At that point, left will be the smallest feasible speed.
        while (left < right) {
            // Use this form to avoid integer overflow:
            // mid = left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Check whether this candidate speed is enough.
            if (canFinish(chapters, h, mid)) {
                // If mid works, it might be the answer,
                // but maybe there is an even smaller valid speed.
                // So keep searching on the left half, including mid.
                right = mid;
            } else {
                // If mid does not work, every speed smaller than mid also does not work.
                // So discard the left half and search strictly above mid.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the minimum feasible speed.
        return left;
    }

    /**
     * Checks whether all chapters can be finished within h hours at the given reading speed.
     *
     * For each chapter with p pages, the number of hours needed is ceil(p / speed).
     * To compute this using integer arithmetic safely and efficiently:
     * ceil(p / speed) = (p + speed - 1) / speed
     *
     * We use a long for the running total because the sum of hours can exceed int range
     * during intermediate computation for large inputs.
     *
     * @param chapters an array of chapter page counts
     * @param h the maximum allowed hours
     * @param speed the candidate reading speed in pages per hour
     * @return true if all chapters can be completed within h hours at this speed; false otherwise
     * Time complexity: O(n), where n is chapters.length
     * Space complexity: O(1)
     */
    public boolean canFinish(int[] chapters, int h, int speed) {
        long totalHours = 0L;

        // Process chapters in order. The order does not change the formula here,
        // because each chapter must be completed fully before the next begins,
        // and each chapter contributes its own rounded-up hour count.
        for (int pages : chapters) {
            // Compute ceil(pages / speed) using integer math.
            totalHours += (pages + (long) speed - 1) / speed;

            // Important optimization:
            // As soon as totalHours exceeds h, we already know this speed fails.
            // No need to continue scanning the remaining chapters.
            if (totalHours > h) {
                return false;
            }
        }

        // If we never exceeded h, then this speed is feasible.
        return totalHours <= h;
    }

    /**
     * Returns the maximum number of pages among all chapters.
     *
     * This value is used as the upper bound for binary search because a speed equal to the
     * largest chapter size guarantees each chapter takes at most one hour.
     *
     * @param chapters an array of chapter page counts
     * @return the maximum chapter size
     * Time complexity: O(n), where n is chapters.length
     * Space complexity: O(1)
     */
    public int getMaxChapterPages(int[] chapters) {
        int max = 0;

        for (int pages : chapters) {
            if (pages > max) {
                max = pages;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log M) per demonstration call
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] chapters1 = {120, 90, 150};
        int h1 = 8;
        int result1 = solution.minReadingSpeed(chapters1, h1);
        System.out.println("Sample 1 result: " + result1);
        System.out.println("Expected: 50");
        System.out.println();

        // Sample 2
        int[] chapters2 = {300, 200, 400, 100};
        int h2 = 10;
        int result2 = solution.minReadingSpeed(chapters2, h2);
        System.out.println("Sample 2 result: " + result2);
        System.out.println("Expected: 100");
        System.out.println();

        // Additional quick checks for beginner-friendly demonstration
        int[] chapters3 = {1, 1, 1, 1};
        int h3 = 4;
        int result3 = solution.minReadingSpeed(chapters3, h3);
        System.out.println("Additional test 1 result: " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        int[] chapters4 = {1000000000};
        int h4 = 1;
        int result4 = solution.minReadingSpeed(chapters4, h4);
        System.out.println("Additional test 2 result: " + result4);
        System.out.println("Expected: 1000000000");
    }
}