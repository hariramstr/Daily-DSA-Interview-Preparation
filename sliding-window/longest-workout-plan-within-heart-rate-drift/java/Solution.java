import java.util.*;

/*
 * Title: Longest Workout Plan Within Heart Rate Drift
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array heartRate where heartRate[i] is the athlete's heart rate
 * recorded during the i-th minute of a workout. A workout segment is considered
 * stable if the difference between the maximum and minimum heart rate inside that
 * contiguous segment is at most limit.
 *
 * Your task is to return the length of the longest stable contiguous segment.
 *
 * In other words, find the maximum window size such that for some indices l and r,
 * the subarray heartRate[l...r] satisfies:
 * max(heartRate[l...r]) - min(heartRate[l...r]) <= limit.
 *
 * This problem models fitness tracking systems that try to identify the longest
 * period of steady exertion without large spikes or drops in heart rate.
 *
 * Constraints:
 * - 1 <= heartRate.length <= 100000
 * - 1 <= heartRate[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: heartRate = [120, 123, 121, 126, 124, 122], limit = 4
 * Correct Output: 3
 *
 * Explanation:
 * Although some candidate segments may look close, we must check both:
 * 1) contiguity
 * 2) max - min <= limit
 *
 * Valid longest segments include:
 * [120, 123, 121] -> max = 123, min = 120, difference = 3
 * [126, 124, 122] -> max = 126, min = 122, difference = 4
 *
 * No contiguous segment of length 4 satisfies the condition, so the answer is 3.
 *
 * Example 2:
 * Input: heartRate = [98, 100, 101, 99, 102, 100, 99], limit = 3
 * Correct Output: 5
 *
 * Explanation:
 * The entire array is not valid because max = 102 and min = 98, so difference = 4.
 * One longest valid segment is:
 * [100, 101, 99, 102, 100] -> max = 102, min = 99, difference = 3
 * Therefore the answer is 5.
 */

public class Solution {

    /**
     * Returns the length of the longest contiguous subarray such that
     * the difference between the maximum and minimum value in that subarray
     * is at most the given limit.
     *
     * We use a sliding window together with two monotonic deques:
     * - one deque keeps values in decreasing order so its front is the current maximum
     * - one deque keeps values in increasing order so its front is the current minimum
     *
     * As we expand the right side of the window, we update both deques.
     * If the window becomes invalid (max - min > limit), we move the left side
     * forward until the window becomes valid again.
     *
     * @param heartRate the array of heart rate readings per minute
     * @param limit the maximum allowed difference between the maximum and minimum
     *              heart rate inside a stable segment
     * @return the maximum length of a stable contiguous segment
     *
     * Time complexity: O(n), because each element is added to and removed from each deque at most once.
     * Space complexity: O(n), in the worst case for the deques.
     */
    public int longestStableSegment(int[] heartRate, int limit) {
        // Deque for indices of elements in decreasing order of values.
        // The front always stores the index of the maximum value in the current window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque for indices of elements in increasing order of values.
        // The front always stores the index of the minimum value in the current window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < heartRate.length; right++) {
            // ------------------------------------------------------------
            // STEP 1: Insert heartRate[right] into the max deque.
            //
            // We want maxDeque to remain decreasing by value.
            // So while the last element in the deque is smaller than the new value,
            // it can never become the maximum for any future window that includes
            // the new value, so we remove it.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && heartRate[maxDeque.peekLast()] < heartRate[right]) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert heartRate[right] into the min deque.
            //
            // We want minDeque to remain increasing by value.
            // So while the last element in the deque is larger than the new value,
            // it can never become the minimum for any future window that includes
            // the new value, so we remove it.
            // ------------------------------------------------------------
            while (!minDeque.isEmpty() && heartRate[minDeque.peekLast()] > heartRate[right]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window from the left while it is invalid.
            //
            // The current maximum is at heartRate[maxDeque.peekFirst()]
            // The current minimum is at heartRate[minDeque.peekFirst()]
            //
            // If max - min > limit, this window is not stable.
            // So we move 'left' forward until the condition becomes valid again.
            // ------------------------------------------------------------
            while ((long) heartRate[maxDeque.peekFirst()] - (long) heartRate[minDeque.peekFirst()] > limit) {
                // If the element leaving the window is currently the maximum,
                // remove it from the front of maxDeque.
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the element leaving the window is currently the minimum,
                // remove it from the front of minDeque.
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Actually move the left boundary of the window forward.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: At this point, the window [left..right] is valid.
            // Update the best answer using the current window length.
            // ------------------------------------------------------------
            int currentLength = right - left + 1;
            bestLength = Math.max(bestLength, currentLength);
        }

        return bestLength;
    }

    /**
     * Helper method to print an array in a readable format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * This main method also includes the expected outputs so that a beginner can
     * compare the actual result with the intended answer.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(n) per demonstrated test case in the worst case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] heartRate1 = {120, 123, 121, 126, 124, 122};
        int limit1 = 4;
        int result1 = solution.longestStableSegment(heartRate1, limit1);
        System.out.println("Example 1:");
        System.out.println("heartRate = " + solution.arrayToString(heartRate1));
        System.out.println("limit = " + limit1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        int[] heartRate2 = {98, 100, 101, 99, 102, 100, 99};
        int limit2 = 3;
        int result2 = solution.longestStableSegment(heartRate2, limit2);
        System.out.println("Example 2:");
        System.out.println("heartRate = " + solution.arrayToString(heartRate2));
        System.out.println("limit = " + limit2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 5");
        System.out.println();

        int[] extra1 = {8, 2, 4, 7};
        int extraLimit1 = 4;
        int extraResult1 = solution.longestStableSegment(extra1, extraLimit1);
        System.out.println("Extra Test 1:");
        System.out.println("heartRate = " + solution.arrayToString(extra1));
        System.out.println("limit = " + extraLimit1);
        System.out.println("Output = " + extraResult1);
        System.out.println("Expected = 2");
        System.out.println();

        int[] extra2 = {10, 1, 2, 4, 7, 2};
        int extraLimit2 = 5;
        int extraResult2 = solution.longestStableSegment(extra2, extraLimit2);
        System.out.println("Extra Test 2:");
        System.out.println("heartRate = " + solution.arrayToString(extra2));
        System.out.println("limit = " + extraLimit2);
        System.out.println("Output = " + extraResult2);
        System.out.println("Expected = 4");
    }
}