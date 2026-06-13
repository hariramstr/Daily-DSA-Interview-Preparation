import java.util.*;

/*
 * Title: Longest Playlist Window With Limited Artist Repeats
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A music streaming service stores a listening session as an array artists,
 * where artists[i] is the artist ID of the i-th song played in order.
 * To keep a generated playlist feeling varied, the service wants to find
 * the longest contiguous block of songs such that no single artist appears
 * more than k times inside that block.
 *
 * Your task is to return the length of the longest contiguous subarray of
 * artists satisfying this rule. In other words, among all windows [l..r],
 * find the maximum window size where every distinct artist appears at most k times.
 *
 * This is a realistic streaming constraint problem: duplicates are allowed,
 * the same artist may appear many times overall, and only contiguous segments count.
 * If k = 0, then no song can be included, so the answer is 0.
 *
 * Constraints:
 * - 1 <= artists.length <= 200000
 * - 1 <= artists[i] <= 1000000000
 * - 0 <= k <= artists.length
 *
 * Example 1:
 * Input: artists = [4, 1, 4, 2, 4, 1, 3], k = 2
 * Output: 5
 * Explanation:
 * The longest valid window is [1, 4, 2, 4, 1].
 * In this window, artist 4 appears 2 times, artist 1 appears 2 times,
 * and artist 2 appears 1 time.
 *
 * Example 2:
 * Input: artists = [7, 7, 7, 2, 2, 3, 7], k = 1
 * Output: 3
 * Explanation:
 * One longest valid window is [7, 2, 3].
 * Any longer window would cause some artist to appear more than once.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray such that
     * every artist appears at most k times inside that subarray.
     *
     * We use the classic sliding window technique:
     * - Expand the right boundary one element at a time.
     * - Track frequencies of artists inside the current window.
     * - If adding the new artist makes its frequency exceed k,
     *   shrink the left boundary until the window becomes valid again.
     * - Record the maximum valid window length seen so far.
     *
     * Why this works:
     * - At any moment, the window [left..right] is maintained so that
     *   all artist frequencies are <= k.
     * - Because left only moves forward and right only moves forward,
     *   the algorithm is linear.
     *
     * @param artists the array of artist IDs representing songs in listening order
     * @param k the maximum allowed frequency of any single artist inside a valid window
     * @return the maximum length of a contiguous valid window
     *
     * Time complexity: O(n), where n is artists.length, because each index is visited
     * at most twice (once by right, once by left).
     * Space complexity: O(m), where m is the number of distinct artists in the current
     * or overall array, due to the frequency map.
     */
    public int longestPlaylistWindow(int[] artists, int k) {
        // Special case:
        // If k == 0, then no artist is allowed even once in the window.
        // Therefore, the only valid window has length 0.
        if (k == 0) {
            return 0;
        }

        // This map stores:
        // key   -> artist ID
        // value -> how many times that artist currently appears in the window [left..right]
        Map<Integer, Integer> frequency = new HashMap<>();

        // left is the start index of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right from 0 to artists.length - 1,
        // expanding the window one song at a time.
        for (int right = 0; right < artists.length; right++) {
            int currentArtist = artists[right];

            // Include the new song at index right into the window.
            // Increase its frequency in the map.
            frequency.put(currentArtist, frequency.getOrDefault(currentArtist, 0) + 1);

            // If the frequency of the newly added artist is now greater than k,
            // the window is invalid.
            //
            // Important observation:
            // Before adding artists[right], the window was valid.
            // After adding it, only currentArtist can possibly violate the rule,
            // because all other artists were already within the limit.
            //
            // So we shrink from the left until currentArtist's count is back to <= k.
            while (frequency.get(currentArtist) > k) {
                int leftArtist = artists[left];

                // Remove the song at index left from the window.
                frequency.put(leftArtist, frequency.get(leftArtist) - 1);

                // Optional cleanup:
                // If an artist's count becomes 0, remove it from the map.
                // This is not required for correctness, but keeps the map tidy.
                if (frequency.get(leftArtist) == 0) {
                    frequency.remove(leftArtist);
                }

                // Move the left boundary rightward, shrinking the window.
                left++;
            }

            // At this point, the window [left..right] is valid:
            // every artist appears at most k times.
            int currentWindowLength = right - left + 1;

            // Update the best answer if this valid window is larger.
            if (currentWindowLength > best) {
                best = currentWindowLength;
            }
        }

        return best;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is arr.length.
     * Space complexity: O(n), due to building the output string.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Also includes expected outputs so it is easy to verify correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demo cases, excluding the algorithm calls.
     * Space complexity: O(1), excluding the algorithm's internal data structures.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] artists1 = {4, 1, 4, 2, 4, 1, 3};
        int k1 = 2;
        int result1 = solution.longestPlaylistWindow(artists1, k1);
        System.out.println("Sample 1:");
        System.out.println("artists = " + solution.arrayToString(artists1));
        System.out.println("k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 5");
        System.out.println();

        // Sample 2
        int[] artists2 = {7, 7, 7, 2, 2, 3, 7};
        int k2 = 1;
        int result2 = solution.longestPlaylistWindow(artists2, k2);
        System.out.println("Sample 2:");
        System.out.println("artists = " + solution.arrayToString(artists2));
        System.out.println("k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional edge case: k = 0
        int[] artists3 = {1, 2, 3, 4};
        int k3 = 0;
        int result3 = solution.longestPlaylistWindow(artists3, k3);
        System.out.println("Edge Case:");
        System.out.println("artists = " + solution.arrayToString(artists3));
        System.out.println("k = " + k3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
    }
}