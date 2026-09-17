import java.util.*;

/*
 * Title: Longest Citation Window With Per-Author Cap
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a list of citations appearing in a research draft, in the exact order they occur
 * in the document. Each citation is represented by an author ID string in the array authors,
 * where authors[i] is the author cited at position i. To reduce over-reliance on a small set of
 * sources, the editor requires that in any accepted contiguous passage, no author may appear more
 * than limit times.
 *
 * Your task is to return the length of the longest contiguous subarray of authors such that every
 * distinct author appears at most limit times inside that window.
 *
 * This is a hard version because the input can be very large, author IDs are arbitrary strings,
 * and a correct solution must efficiently maintain frequency constraints while expanding and
 * shrinking a moving window.
 *
 * Formally, find the maximum value of r - l + 1 over all indices 0 <= l <= r < authors.length
 * such that for every author ID x, the number of occurrences of x in authors[l..r] is at most
 * limit.
 *
 * Constraints:
 * - 1 <= authors.length <= 2 * 10^5
 * - 1 <= authors[i].length <= 20
 * - authors[i] consists of lowercase English letters, digits, or underscores
 * - 1 <= limit <= authors.length
 *
 * Example 1:
 * Input: authors = ["lee","kim","lee","patel","kim","lee","ng"], limit = 2
 * Output: 5
 * Explanation: The longest valid window is ["lee","kim","lee","patel","kim"], where "lee"
 * appears 2 times and "kim" appears 2 times. Extending one more position would make "lee"
 * appear 3 times, which violates the cap.
 *
 * Example 2:
 * Input: authors = ["a","b","a","c","a","b","b","d"], limit = 1
 * Output: 3
 * Explanation: One longest valid window is ["a","c","b"]. Every author appears at most once.
 * Any longer contiguous window contains a repeated author.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray in which every distinct author appears
     * at most {@code limit} times.
     *
     * The algorithm uses the classic sliding window technique:
     * 1. Expand the right boundary one element at a time.
     * 2. Track frequencies of authors inside the current window using a hash map.
     * 3. If adding the new author causes its frequency to exceed {@code limit}, shrink the left
     *    boundary until the window becomes valid again.
     * 4. After each valid state, update the best window length found so far.
     *
     * Why this works:
     * - The window always remains contiguous.
     * - Each index enters the window once and leaves the window at most once.
     * - Therefore, the total work is linear in the number of citations.
     *
     * @param authors the array of author IDs in citation order
     * @param limit the maximum allowed frequency of any single author inside a valid window
     * @return the maximum length of a contiguous valid window
     * Time complexity: O(n), where n is authors.length
     * Space complexity: O(k), where k is the number of distinct authors in the current map
     */
    public int longestCitationWindow(String[] authors, int limit) {
        // Frequency map:
        // key   -> author ID
        // value -> how many times that author currently appears inside the window [left, right]
        Map<String, Integer> frequency = new HashMap<>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length seen so far.
        int best = 0;

        // Move right from 0 to authors.length - 1, expanding the window one citation at a time.
        for (int right = 0; right < authors.length; right++) {
            String currentAuthor = authors[right];

            // Include authors[right] into the window by increasing its count.
            frequency.put(currentAuthor, frequency.getOrDefault(currentAuthor, 0) + 1);

            // If the newly added author now appears too many times, the window is invalid.
            // Important observation:
            // Only currentAuthor can become invalid at this moment, because all other counts
            // were already valid before we added this one new element.
            //
            // So we shrink the window from the left until currentAuthor's count is back within
            // the allowed limit.
            while (frequency.get(currentAuthor) > limit) {
                String leftAuthor = authors[left];

                // Remove the leftmost author from the window.
                int updatedCount = frequency.get(leftAuthor) - 1;

                if (updatedCount == 0) {
                    // Clean up zero-count entries to keep the map tidy.
                    frequency.remove(leftAuthor);
                } else {
                    frequency.put(leftAuthor, updatedCount);
                }

                // Move left boundary rightward, effectively shrinking the window.
                left++;
            }

            // At this point, the window [left, right] is guaranteed valid:
            // every author appears at most limit times.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A helper method that prints an input array in a readable format and shows the computed
     * longest valid citation window length.
     *
     * @param authors the array of author IDs to test
     * @param limit the per-author maximum allowed frequency
     * @return the computed longest valid window length
     * Time complexity: O(n), where n is authors.length
     * Space complexity: O(k), where k is the number of distinct authors tracked
     */
    public int demonstrateCase(String[] authors, int limit) {
        int result = longestCitationWindow(authors, limit);
        System.out.println("Authors: " + Arrays.toString(authors));
        System.out.println("Limit: " + limit);
        System.out.println("Longest valid window length: " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem statement and prints the results.
     *
     * This main method verifies the provided examples:
     * - Example 1 should print 5
     * - Example 2 should print 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated cases)
     * Space complexity: O(k) per test case for the frequency map
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement.
        String[] authors1 = {"lee", "kim", "lee", "patel", "kim", "lee", "ng"};
        int limit1 = 2;
        int result1 = solution.demonstrateCase(authors1, limit1);
        System.out.println("Expected: 5");
        System.out.println("Matches expected: " + (result1 == 5));
        System.out.println();

        // Example 2 from the problem statement.
        String[] authors2 = {"a", "b", "a", "c", "a", "b", "b", "d"};
        int limit2 = 1;
        int result2 = solution.demonstrateCase(authors2, limit2);
        System.out.println("Expected: 3");
        System.out.println("Matches expected: " + (result2 == 3));
        System.out.println();

        // Additional small sanity checks for beginners.
        String[] authors3 = {"x"};
        int limit3 = 1;
        solution.demonstrateCase(authors3, limit3);

        String[] authors4 = {"m", "m", "m", "m"};
        int limit4 = 2;
        solution.demonstrateCase(authors4, limit4);

        String[] authors5 = {"u", "v", "w", "x", "y"};
        int limit5 = 1;
        solution.demonstrateCase(authors5, limit5);
    }
}