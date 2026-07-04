import java.util.*;

/*
 * Title: Longest Note Sequence With Limited Pitch Jumps
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A music learning app records a student's practice session as an array of integers called notes,
 * where notes[i] is the pitch played at time i. The app wants to find the longest contiguous segment
 * that is still considered smooth enough to review as a single phrase. A segment is smooth if the
 * difference between the highest pitch and the lowest pitch inside that segment is at most limit.
 *
 * Your task is to return the length of the longest contiguous subarray of notes such that:
 *
 *     max(notes[l..r]) - min(notes[l..r]) <= limit
 *
 * This is a realistic streaming-style problem: as you extend the right end of the window, the valid
 * left boundary may need to move forward to keep the pitch range within the allowed jump limit.
 * An efficient solution is expected.
 *
 * Constraints:
 * - 1 <= notes.length <= 200000
 * - 0 <= notes[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: notes = [12, 14, 13, 18, 15, 16], limit = 3
 * Output: 3
 * Explanation: One valid longest segment is [12, 14, 13], where max = 14 and min = 12, so the range is 2.
 * Any longer segment exceeds the allowed range of 3.
 *
 * Example 2:
 * Input: notes = [7, 7, 8, 9, 6, 7, 8], limit = 2
 * Output: 4
 * Explanation: The segment [7, 7, 8, 9] has max = 9 and min = 7, so the range is 2.
 * Another valid segment is [9, 6, 7, 8]? No, its range is 3, so it is invalid.
 * Therefore the maximum length is 4.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose maximum value minus minimum value
     * is at most the given limit.
     *
     * This method uses a sliding window together with two monotonic deques:
     * 1. One deque keeps values in decreasing order so its front is always the current maximum.
     * 2. One deque keeps values in increasing order so its front is always the current minimum.
     *
     * As we expand the window to the right, we update both deques.
     * If the window becomes invalid (max - min > limit), we move the left boundary to the right
     * until the window becomes valid again.
     *
     * @param notes the array of note pitches recorded over time
     * @param limit the maximum allowed difference between the highest and lowest pitch in a valid segment
     * @return the maximum length of a contiguous smooth segment
     *
     * Time complexity: O(n), because each element is added to and removed from each deque at most once.
     * Space complexity: O(n), in the worst case for the deques.
     */
    public int longestSmoothSegment(int[] notes, int limit) {
        // Defensive handling. The problem guarantees at least one element,
        // but this makes the method safer and easier for beginners to reuse.
        if (notes == null || notes.length == 0) {
            return 0;
        }

        // maxDeque will store indices of elements in decreasing order of values.
        // That means:
        // - notes[maxDeque.peekFirst()] is always the maximum value in the current window.
        // - When a new value comes in, we remove smaller values from the back because
        //   they can never become the maximum while the new larger value remains in the window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // minDeque will store indices of elements in increasing order of values.
        // That means:
        // - notes[minDeque.peekFirst()] is always the minimum value in the current window.
        // - When a new value comes in, we remove larger values from the back because
        //   they can never become the minimum while the new smaller value remains in the window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right one step at a time, expanding the window.
        for (int right = 0; right < notes.length; right++) {
            // Current value entering the window.
            int current = notes[right];

            // -----------------------------
            // Step 1: Update maxDeque
            // -----------------------------
            // Remove all indices from the back whose values are smaller than the current value.
            // Why?
            // Because if current is larger, those smaller values are useless for future maximum queries.
            while (!maxDeque.isEmpty() && notes[maxDeque.peekLast()] < current) {
                maxDeque.pollLast();
            }
            // Add the current index to the back.
            maxDeque.offerLast(right);

            // -----------------------------
            // Step 2: Update minDeque
            // -----------------------------
            // Remove all indices from the back whose values are larger than the current value.
            // Why?
            // Because if current is smaller, those larger values are useless for future minimum queries.
            while (!minDeque.isEmpty() && notes[minDeque.peekLast()] > current) {
                minDeque.pollLast();
            }
            // Add the current index to the back.
            minDeque.offerLast(right);

            // -----------------------------
            // Step 3: Shrink window if invalid
            // -----------------------------
            // The window is invalid if:
            // current maximum - current minimum > limit
            //
            // The maximum is always at notes[maxDeque.peekFirst()]
            // The minimum is always at notes[minDeque.peekFirst()]
            //
            // We keep moving left forward until the window becomes valid again.
            while ((long) notes[maxDeque.peekFirst()] - (long) notes[minDeque.peekFirst()] > limit) {
                // If the leftmost index is exactly the one stored at the front of maxDeque,
                // it is leaving the window, so remove it from the deque.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // Similarly, if the leftmost index is the front of minDeque,
                // remove it because it is no longer inside the window.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Actually move the left boundary of the window forward by one.
                left++;
            }

            // -----------------------------
            // Step 4: Update best answer
            // -----------------------------
            // At this point, the window [left..right] is valid.
            // Its length is right - left + 1.
            int windowLength = right - left + 1;
            best = Math.max(best, windowLength);
        }

        return best;
    }

    /**
     * A second public method with the exact same logic, named more generically.
     * This can be useful if the caller thinks of the input as a normal integer array
     * rather than specifically as musical notes.
     *
     * @param nums the input array
     * @param limit the maximum allowed difference between max and min inside a valid subarray
     * @return the length of the longest valid contiguous subarray
     *
     * Time complexity: O(n), because each index is processed a constant number of times.
     * Space complexity: O(n), due to the deques in the worst case.
     */
    public int longestSubarray(int[] nums, int limit) {
        return longestSmoothSegment(nums, limit);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call, where n is the array length.
     * Space complexity: O(n) per demonstration call in the worst case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] notes1 = {12, 14, 13, 18, 15, 16};
        int limit1 = 3;
        int result1 = solution.longestSmoothSegment(notes1, limit1);
        System.out.println("Example 1:");
        System.out.println("notes = " + Arrays.toString(notes1));
        System.out.println("limit = " + limit1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        int[] notes2 = {7, 7, 8, 9, 6, 7, 8};
        int limit2 = 2;
        int result2 = solution.longestSmoothSegment(notes2, limit2);
        System.out.println("Example 2:");
        System.out.println("notes = " + Arrays.toString(notes2));
        System.out.println("limit = " + limit2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        int[] notes3 = {8};
        int limit3 = 0;
        int result3 = solution.longestSmoothSegment(notes3, limit3);
        System.out.println("Additional Example 3:");
        System.out.println("notes = " + Arrays.toString(notes3));
        System.out.println("limit = " + limit3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        int[] notes4 = {4, 2, 2, 2, 4, 4, 2, 2};
        int limit4 = 0;
        int result4 = solution.longestSmoothSegment(notes4, limit4);
        System.out.println("Additional Example 4:");
        System.out.println("notes = " + Arrays.toString(notes4));
        System.out.println("limit = " + limit4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 3");
    }
}