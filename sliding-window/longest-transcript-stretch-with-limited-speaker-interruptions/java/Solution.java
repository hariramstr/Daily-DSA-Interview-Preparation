import java.util.*;

/*
 * Title: Longest Transcript Stretch With Limited Speaker Interruptions
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a conversation transcript represented by an array speakers,
 * where speakers[i] is the ID of the person speaking at second i.
 *
 * A continuous segment of the transcript is considered smooth if the number
 * of speaker interruptions inside that segment is at most k.
 *
 * An interruption occurs whenever two adjacent seconds in the segment are
 * spoken by different people.
 *
 * Example:
 * In the segment [2, 2, 5, 5, 5, 2], there are 2 interruptions:
 * - between the second and third elements (2 -> 5)
 * - between the fifth and sixth elements (5 -> 2)
 *
 * Your task is to return the length of the longest smooth contiguous segment.
 *
 * Constraints:
 * - 1 <= speakers.length <= 200000
 * - 1 <= speakers[i] <= 1000000000
 * - 0 <= k < speakers.length
 *
 * Example 1:
 * Input: speakers = [1, 1, 2, 2, 2, 3, 3], k = 1
 * Output: 5
 *
 * Explanation:
 * The segment [1, 1, 2, 2, 2] has exactly 1 interruption, so its length is 5.
 * Any longer valid segment would include the change from 2 to 3, creating 2 interruptions.
 *
 * Example 2:
 * Input: speakers = [4, 7, 7, 4, 4, 4, 9], k = 2
 * Output: 6
 *
 * Explanation:
 * The segment [4, 7, 7, 4, 4, 4] has 2 interruptions:
 * - 4 -> 7
 * - 7 -> 4
 * Its length is 6, which is the longest valid smooth segment.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous segment whose number of speaker
     * interruptions is at most k.
     *
     * Core idea:
     * A segment [left, right] contains one interruption for each index i where
     * left < i <= right and speakers[i] != speakers[i - 1].
     *
     * We use a sliding window:
     * - Expand the right boundary one step at a time.
     * - When adding a new element at position right, we check whether it creates
     *   a new interruption with the previous element at right - 1.
     * - If the window becomes invalid (interruptions > k), we move left forward
     *   until the window becomes valid again.
     *
     * Why shrinking works:
     * When left moves from L to L + 1, the only adjacency that leaves the window
     * is the pair (L, L + 1). If those two speakers were different, one interruption
     * disappears from the window count.
     *
     * @param speakers the transcript as an array where speakers[i] is the speaker ID at second i
     * @param k the maximum number of allowed interruptions inside the segment
     * @return the maximum length of a contiguous segment with at most k interruptions
     *
     * Time complexity: O(n), where n is speakers.length, because each pointer moves at most n times.
     * Space complexity: O(1), ignoring input storage, because only a few variables are used.
     */
    public int longestSmoothSegment(int[] speakers, int k) {
        int n = speakers.length;

        // left marks the start of the current sliding window.
        int left = 0;

        // interruptions stores how many adjacent changes currently exist
        // inside the window [left, right].
        int interruptions = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right from left to right across the array, expanding the window.
        for (int right = 0; right < n; right++) {

            // If right > 0, then the new element speakers[right] forms an adjacent pair
            // with speakers[right - 1].
            //
            // If those two values are different, then by including speakers[right]
            // in the window, we add exactly one new interruption.
            if (right > 0 && speakers[right] != speakers[right - 1]) {
                interruptions++;
            }

            // If the window now has too many interruptions, shrink it from the left.
            //
            // Important detail:
            // When we move left forward by one position, the pair (left, left + 1)
            // is no longer inside the window.
            //
            // If speakers[left] != speakers[left + 1], then that pair contributed
            // one interruption, so we must subtract it.
            while (interruptions > k) {
                if (left + 1 <= right && speakers[left] != speakers[left + 1]) {
                    interruptions--;
                }
                left++;
            }

            // At this point, the window [left, right] is valid:
            // it has at most k interruptions.
            //
            // Compute its length and update the answer.
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * A helper method that prints an input array in a readable format and shows
     * the computed answer.
     *
     * @param speakers the transcript speaker array
     * @param k the maximum allowed interruptions
     * @return the computed longest smooth segment length
     *
     * Time complexity: O(n), due to the call to longestSmoothSegment and array-to-string printing.
     * Space complexity: O(1) auxiliary, excluding the string created for printing.
     */
    public int demonstrateCase(int[] speakers, int k) {
        int result = longestSmoothSegment(speakers, k);
        System.out.println("speakers = " + Arrays.toString(speakers));
        System.out.println("k = " + k);
        System.out.println("Longest smooth segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the algorithm on the examples from the problem statement
     * and a few additional cases.
     *
     * Verified examples:
     * - [1, 1, 2, 2, 2, 3, 3], k = 1 -> 5
     * - [4, 7, 7, 4, 4, 4, 9], k = 2 -> 6
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated test cases).
     * Space complexity: O(1) auxiliary, excluding printing-related internals.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement.
        // Interruptions:
        // 1 -> 1 : no
        // 1 -> 2 : yes
        // 2 -> 2 : no
        // 2 -> 2 : no
        // 2 -> 3 : yes
        // 3 -> 3 : no
        //
        // The longest segment with at most 1 interruption is [1, 1, 2, 2, 2], length 5.
        int[] speakers1 = {1, 1, 2, 2, 2, 3, 3};
        int k1 = 1;
        int result1 = solution.demonstrateCase(speakers1, k1);
        System.out.println("Expected: 5, Actual: " + result1);
        System.out.println();

        // Example 2 from the problem statement.
        // Segment [4, 7, 7, 4, 4, 4] has interruptions:
        // 4 -> 7 : yes
        // 7 -> 7 : no
        // 7 -> 4 : yes
        // 4 -> 4 : no
        // 4 -> 4 : no
        // Total = 2, length = 6.
        int[] speakers2 = {4, 7, 7, 4, 4, 4, 9};
        int k2 = 2;
        int result2 = solution.demonstrateCase(speakers2, k2);
        System.out.println("Expected: 6, Actual: " + result2);
        System.out.println();

        // Additional beginner-friendly checks.

        // No interruptions allowed: longest block of equal values.
        int[] speakers3 = {5, 5, 5, 1, 1, 2, 2, 2, 2};
        int k3 = 0;
        int result3 = solution.demonstrateCase(speakers3, k3);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println();

        // Entire array valid because allowed interruptions are large enough.
        int[] speakers4 = {1, 2, 1, 2, 1};
        int k4 = 4;
        int result4 = solution.demonstrateCase(speakers4, k4);
        System.out.println("Expected: 5, Actual: " + result4);
        System.out.println();

        // Single element array.
        int[] speakers5 = {42};
        int k5 = 0;
        int result5 = solution.demonstrateCase(speakers5, k5);
        System.out.println("Expected: 1, Actual: " + result5);
    }
}