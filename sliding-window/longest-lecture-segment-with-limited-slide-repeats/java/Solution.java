import java.util.*;

/*
 * Title: Longest Lecture Segment With Limited Slide Repeats
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array slides where slides[i] is the ID of the slide shown at minute i
 * during a recorded lecture. Because instructors sometimes revisit the same slide multiple
 * times, the video platform wants to extract the longest contiguous segment that is still
 * easy for students to review. A segment is considered review-friendly if no slide ID
 * appears more than k times inside that segment.
 *
 * Return the length of the longest contiguous subarray of slides that is review-friendly.
 *
 * In other words, find the maximum window [l, r] such that for every distinct slide ID in
 * slides[l...r], its frequency within that window is at most k.
 *
 * This is a realistic stream-processing problem: the answer must be based on a contiguous
 * time interval, not on reordering or deleting arbitrary elements outside the chosen interval.
 *
 * Constraints:
 * - 1 <= slides.length <= 200000
 * - 1 <= slides[i] <= 1000000000
 * - 1 <= k <= slides.length
 * - The solution should run efficiently for large inputs.
 *
 * Example 1:
 * Input: slides = [4, 2, 4, 3, 2, 4, 2, 5], k = 2
 * Output: 5
 * Explanation: One optimal segment is [4, 3, 2, 4, 2], which has slide 4 appearing 2 times
 * and slide 2 appearing 2 times. No slide appears more than 2 times, so the segment length is 5.
 *
 * Example 2:
 * Input: slides = [7, 7, 7, 1, 2, 1, 2, 3], k = 1
 * Output: 4
 * Explanation: One optimal segment is [7, 1, 2, 3]. With k = 1, all slide IDs in the chosen
 * segment must be unique, so the maximum valid length is 4.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray such that no value appears more than k times.
     *
     * The algorithm uses the classic sliding window technique:
     * - Expand the right boundary one element at a time.
     * - Track frequencies of elements inside the current window.
     * - If adding the new element causes its frequency to exceed k, move the left boundary
     *   rightward until the window becomes valid again.
     * - Record the maximum valid window length seen during the process.
     *
     * @param slides the array of slide IDs shown over time
     * @param k the maximum allowed frequency of any slide ID inside a valid segment
     * @return the length of the longest contiguous review-friendly segment
     *
     * Time complexity: O(n), where n is slides.length, because each index moves at most once
     * through the sliding window.
     * Space complexity: O(m), where m is the number of distinct slide IDs currently tracked
     * in the frequency map, up to O(n) in the worst case.
     */
    public int longestReviewFriendlySegment(int[] slides, int k) {
        // Frequency map:
        // key   -> slide ID
        // value -> how many times that slide ID appears in the current window [left, right]
        Map<Integer, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // We expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < slides.length; right++) {
            int currentSlide = slides[right];

            // Include the new slide at position 'right' into the window.
            frequency.put(currentSlide, frequency.getOrDefault(currentSlide, 0) + 1);

            // If the frequency of the newly added slide exceeds k,
            // the window is no longer valid.
            //
            // Important observation:
            // Before adding slides[right], the window was valid.
            // After adding slides[right], only the count of currentSlide changed.
            // Therefore, only currentSlide can possibly violate the rule.
            //
            // So we shrink the window from the left until currentSlide's count is back to <= k.
            while (frequency.get(currentSlide) > k) {
                int leftSlide = slides[left];

                // Remove the leftmost slide from the current window.
                frequency.put(leftSlide, frequency.get(leftSlide) - 1);

                // Optional cleanup:
                // If a slide's count becomes zero, remove it from the map.
                // This is not required for correctness, but keeps the map cleaner.
                if (frequency.get(leftSlide) == 0) {
                    frequency.remove(leftSlide);
                }

                // Move the left boundary rightward, making the window smaller.
                left++;
            }

            // At this point, the window [left, right] is valid:
            // every slide ID appears at most k times.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger.
            maxLength = Math.max(maxLength, currentLength);
        }

        return maxLength;
    }

    /**
     * A convenience wrapper that validates basic assumptions and then delegates
     * to the main sliding window implementation.
     *
     * @param slides the array of slide IDs
     * @param k the maximum allowed occurrences of any slide ID in the chosen segment
     * @return the maximum valid segment length
     *
     * Time complexity: O(n), where n is slides.length.
     * Space complexity: O(m), where m is the number of distinct values tracked.
     */
    public int solve(int[] slides, int k) {
        if (slides == null || slides.length == 0) {
            return 0;
        }
        return longestReviewFriendlySegment(slides, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call.
     * Space complexity: O(m) per demonstration call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] slides1 = {4, 2, 4, 3, 2, 4, 2, 5};
        int k1 = 2;
        int result1 = solution.solve(slides1, k1);
        System.out.println("Example 1 Output: " + result1);
        // Expected: 5

        int[] slides2 = {7, 7, 7, 1, 2, 1, 2, 3};
        int k2 = 1;
        int result2 = solution.solve(slides2, k2);
        System.out.println("Example 2 Output: " + result2);
        // Expected: 4

        // Additional quick checks
        int[] slides3 = {1, 1, 1, 1};
        int k3 = 2;
        int result3 = solution.solve(slides3, k3);
        System.out.println("Additional Check 1 Output: " + result3);
        // Expected: 2

        int[] slides4 = {1, 2, 3, 4, 5};
        int k4 = 1;
        int result4 = solution.solve(slides4, k4);
        System.out.println("Additional Check 2 Output: " + result4);
        // Expected: 5
    }
}