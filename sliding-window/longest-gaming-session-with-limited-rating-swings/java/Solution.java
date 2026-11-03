import java.util.*;

/*
 * Title: Longest Gaming Session With Limited Rating Swings
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array ratings where ratings[i] is the skill rating of the opponent faced
 * in the i-th consecutive match of a gaming session. A session segment is considered stable
 * if the difference between the highest and lowest opponent ratings inside that contiguous
 * segment is at most limit.
 *
 * Your task is to return the length of the longest stable contiguous segment of matches.
 *
 * In other words, find the maximum size of a subarray ratings[l..r] such that
 * max(ratings[l..r]) - min(ratings[l..r]) <= limit.
 *
 * This models a real matchmaking analysis problem: a player is said to be in a consistent
 * queue environment only when the opponent difficulty does not swing too much over a stretch
 * of consecutive games.
 *
 * You should design an algorithm efficient enough for large inputs. A brute-force approach
 * that checks every subarray will be too slow.
 *
 * Constraints:
 * - 1 <= ratings.length <= 200000
 * - 1 <= ratings[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: ratings = [1200, 1215, 1190, 1220, 1265, 1230], limit = 40
 * Output: 4
 * Explanation: The longest stable segment is [1200, 1215, 1190, 1220].
 * Its maximum is 1220 and minimum is 1190, so the difference is 30, which is within the limit.
 * Any longer segment includes 1265, making the rating swing exceed 40.
 *
 * Example 2:
 * Input: ratings = [1500, 1500, 1500, 1510, 1498, 1499], limit = 12
 * Output: 6
 * Explanation: The entire array is stable. The maximum rating is 1510 and the minimum is 1498,
 * so the difference is exactly 12.
 *
 * Return only the maximum length of such a contiguous segment.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray such that the difference
     * between the maximum and minimum values in that subarray is at most {@code limit}.
     *
     * This method uses a classic sliding window technique together with two monotonic deques:
     * one deque keeps track of possible maximum values in decreasing order,
     * and the other deque keeps track of possible minimum values in increasing order.
     *
     * Why this works:
     * - As the right boundary expands, we insert the new value into both deques while preserving order.
     * - The front of the max deque always stores the index of the current maximum in the window.
     * - The front of the min deque always stores the index of the current minimum in the window.
     * - If max - min becomes greater than limit, we move the left boundary rightward until the window is valid again.
     * - During that shrinking process, if the leftmost index is at the front of either deque, we remove it.
     *
     * @param ratings the array of opponent ratings for consecutive matches
     * @param limit the maximum allowed difference between the highest and lowest rating in a stable segment
     * @return the maximum length of a contiguous stable segment
     *
     * Time complexity: O(n), because each index is added to and removed from each deque at most once.
     * Space complexity: O(n), in the worst case for the deques.
     */
    public int longestStableSegment(int[] ratings, int limit) {
        // Deque for indices of elements in decreasing order of values.
        // The front always points to the maximum value in the current window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque for indices of elements in increasing order of values.
        // The front always points to the minimum value in the current window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // Expand the window by moving the right boundary one step at a time.
        for (int right = 0; right < ratings.length; right++) {
            int currentValue = ratings[right];

            // ------------------------------------------------------------
            // STEP 1: Insert current index into maxDeque.
            //
            // We want maxDeque to store indices whose values are in decreasing order.
            // That means:
            // - If the new value is greater than or equal to values at the back,
            //   those smaller/equal values can never become the maximum while the new
            //   value remains in the window, so we remove them.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && ratings[maxDeque.peekLast()] <= currentValue) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert current index into minDeque.
            //
            // We want minDeque to store indices whose values are in increasing order.
            // That means:
            // - If the new value is less than or equal to values at the back,
            //   those larger/equal values can never become the minimum while the new
            //   value remains in the window, so we remove them.
            // ------------------------------------------------------------
            while (!minDeque.isEmpty() && ratings[minDeque.peekLast()] >= currentValue) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // ------------------------------------------------------------
            // STEP 3: If the current window is invalid, shrink it from the left.
            //
            // The current maximum is at ratings[maxDeque.peekFirst()]
            // The current minimum is at ratings[minDeque.peekFirst()]
            //
            // If max - min > limit, the window is not stable, so we must move left forward.
            // While moving left:
            // - If the outgoing index is exactly the front of maxDeque, remove it.
            // - If the outgoing index is exactly the front of minDeque, remove it.
            // Then increment left and check again.
            // ------------------------------------------------------------
            while ((long) ratings[maxDeque.peekFirst()] - (long) ratings[minDeque.peekFirst()] > limit) {
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: At this point, the window [left..right] is valid.
            // Compute its length and update the best answer.
            // ------------------------------------------------------------
            int currentLength = right - left + 1;
            if (currentLength > bestLength) {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }

    /**
     * A helper method that runs the algorithm on one test case and prints the result.
     * This is useful for demonstration in the main method.
     *
     * @param ratings the input ratings array
     * @param limit the allowed maximum difference between max and min in a valid segment
     * @return the computed longest stable segment length
     *
     * Time complexity: O(n), where n is the length of {@code ratings}.
     * Space complexity: O(n), due to the deques used by the sliding window algorithm.
     */
    public int demonstrateCase(int[] ratings, int limit) {
        int result = longestStableSegment(ratings, limit);
        System.out.println("Ratings: " + Arrays.toString(ratings));
        System.out.println("Limit: " + limit);
        System.out.println("Longest stable segment length: " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Expected outputs:
     * - Example 1 -> 4
     * - Example 2 -> 6
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(n) per demonstrated test case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] ratings1 = {1200, 1215, 1190, 1220, 1265, 1230};
        int limit1 = 40;
        int result1 = solution.demonstrateCase(ratings1, limit1);
        System.out.println("Expected: 4, Actual: " + result1);
        System.out.println();

        int[] ratings2 = {1500, 1500, 1500, 1510, 1498, 1499};
        int limit2 = 12;
        int result2 = solution.demonstrateCase(ratings2, limit2);
        System.out.println("Expected: 6, Actual: " + result2);
        System.out.println();

        int[] ratings3 = {8, 2, 4, 7};
        int limit3 = 4;
        int result3 = solution.demonstrateCase(ratings3, limit3);
        System.out.println("Additional check - Expected: 2, Actual: " + result3);
        System.out.println();

        int[] ratings4 = {10};
        int limit4 = 0;
        int result4 = solution.demonstrateCase(ratings4, limit4);
        System.out.println("Additional check - Expected: 1, Actual: " + result4);
    }
}