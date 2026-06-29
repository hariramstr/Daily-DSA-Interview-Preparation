import java.util.*;

/*
 * Title: Longest Caption Feed With Limited Hashtag Overload
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A social media analytics team is reviewing a chronological feed of post captions.
 * Each caption is represented by an integer in the array hashtags, where the value
 * is the hashtag ID used in that post. The team defines a feed segment as valid if
 * no hashtag appears more than limit times inside that contiguous segment.
 *
 * Your task is to return the length of the longest valid contiguous segment.
 *
 * In other words, given an array hashtags and an integer limit, find the maximum
 * size of a subarray such that for every distinct hashtag ID inside the subarray,
 * its frequency is at most limit.
 *
 * This problem should be solved efficiently for large inputs. A brute-force approach
 * that checks every subarray will be too slow. Think about maintaining a moving
 * window and updating frequencies as the window expands or shrinks.
 *
 * Constraints:
 * - 1 <= hashtags.length <= 200000
 * - 1 <= hashtags[i] <= 1000000000
 * - 1 <= limit <= hashtags.length
 *
 * Example 1:
 * Input: hashtags = [4, 1, 4, 2, 2, 4, 3], limit = 2
 * Output: 5
 * Explanation:
 * One longest valid segment is [1, 4, 2, 2, 4].
 * In this segment, hashtag 4 appears 2 times and hashtag 2 appears 2 times,
 * which is allowed. Extending the segment further would make hashtag 4 appear 3 times.
 *
 * Example 2:
 * Input: hashtags = [7, 7, 7, 8, 8, 9], limit = 1
 * Output: 3
 * Explanation:
 * A valid longest segment is [7, 8, 9].
 * Since each hashtag may appear at most once, the answer is the longest subarray
 * with all distinct values.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray such that no value appears
     * more than {@code limit} times inside that subarray.
     *
     * The algorithm uses the classic sliding window technique:
     * 1. Expand the right boundary one element at a time.
     * 2. Track frequencies of values inside the current window.
     * 3. If adding the new element makes its frequency exceed {@code limit},
     *    shrink the left boundary until the window becomes valid again.
     * 4. After each valid state, update the best answer.
     *
     * Why this works:
     * - At every step, the window [left, right] is maintained as valid.
     * - Each index enters the window once and leaves the window once.
     * - Therefore, the total work is linear.
     *
     * @param hashtags the array of hashtag IDs representing the chronological feed
     * @param limit the maximum allowed frequency for any hashtag inside a valid segment
     * @return the length of the longest valid contiguous segment
     * Time complexity: O(n), where n is hashtags.length
     * Space complexity: O(k), where k is the number of distinct hashtag IDs in the current/overall feed
     */
    public int longestValidSegment(int[] hashtags, int limit) {
        // Frequency map:
        // key   -> hashtag ID
        // value -> how many times that hashtag currently appears inside the sliding window
        Map<Integer, Integer> frequency = new HashMap<>();

        // left marks the start of the current window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We move right from 0 to the end of the array, expanding the window one step at a time.
        for (int right = 0; right < hashtags.length; right++) {
            int currentHashtag = hashtags[right];

            // Add the new rightmost element into the window.
            frequency.put(currentHashtag, frequency.getOrDefault(currentHashtag, 0) + 1);

            /*
             * Important observation:
             * Before adding hashtags[right], the window was valid.
             * After adding it, the only possible violation is that this specific hashtag
             * might now appear too many times.
             *
             * So we only need to shrink while frequency of currentHashtag exceeds limit.
             */
            while (frequency.get(currentHashtag) > limit) {
                int leftHashtag = hashtags[left];

                // Remove the leftmost element from the window because we are shrinking it.
                frequency.put(leftHashtag, frequency.get(leftHashtag) - 1);

                // Optional cleanup:
                // If a frequency becomes zero, remove it from the map to keep the map tidy.
                if (frequency.get(leftHashtag) == 0) {
                    frequency.remove(leftHashtag);
                }

                // Move the left boundary to the right.
                left++;
            }

            // At this point, the window [left, right] is valid again.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger than anything seen before.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A helper method that runs one demonstration case and prints the result.
     *
     * @param hashtags the input array of hashtag IDs
     * @param limit the maximum allowed frequency for any hashtag in a valid segment
     * @return the computed longest valid segment length
     * Time complexity: O(n), where n is hashtags.length
     * Space complexity: O(k), where k is the number of distinct hashtag IDs
     */
    public int demonstrateCase(int[] hashtags, int limit) {
        int result = longestValidSegment(hashtags, limit);
        System.out.println("hashtags = " + Arrays.toString(hashtags));
        System.out.println("limit = " + limit);
        System.out.println("Longest valid segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on the sample inputs from the problem statement.
     *
     * Verified sample outputs:
     * - [4, 1, 4, 2, 2, 4, 3], limit = 2 -> 5
     * - [7, 7, 7, 8, 8, 9], limit = 1 -> 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(k) per demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] hashtags1 = {4, 1, 4, 2, 2, 4, 3};
        int limit1 = 2;
        int result1 = solution.demonstrateCase(hashtags1, limit1);
        System.out.println("Expected: 5");
        System.out.println("Matches expected: " + (result1 == 5));
        System.out.println();

        // Sample 2
        int[] hashtags2 = {7, 7, 7, 8, 8, 9};
        int limit2 = 1;
        int result2 = solution.demonstrateCase(hashtags2, limit2);
        System.out.println("Expected: 3");
        System.out.println("Matches expected: " + (result2 == 3));
        System.out.println();

        // Additional quick sanity checks
        int[] hashtags3 = {1, 2, 3, 4};
        int limit3 = 1;
        solution.demonstrateCase(hashtags3, limit3); // Expected 4

        int[] hashtags4 = {5, 5, 5, 5};
        int limit4 = 2;
        solution.demonstrateCase(hashtags4, limit4); // Expected 2
    }
}