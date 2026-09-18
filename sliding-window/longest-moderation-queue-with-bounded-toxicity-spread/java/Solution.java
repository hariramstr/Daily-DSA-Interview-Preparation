import java.util.*;

/*
 * Title: Longest Moderation Queue With Bounded Toxicity Spread
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A social platform stores the toxicity score of each newly posted comment in chronological order.
 * You are given an integer array scores, where scores[i] is the toxicity score of the i-th comment,
 * and an integer limit. A contiguous block of comments is considered reviewable if the difference
 * between the maximum toxicity score and the minimum toxicity score inside that block is at most limit.
 *
 * Your task is to return the length of the longest reviewable contiguous block.
 *
 * Unlike a simple fixed-size window problem, the optimal block may start and end anywhere, and scores
 * can be large, repeated, or highly irregular. An efficient solution is required for very large inputs,
 * so approaches that recompute the minimum and maximum for every candidate window will time out.
 *
 * Formally, find the maximum value of (r - l + 1) such that for some 0 <= l <= r < scores.length:
 * max(scores[l..r]) - min(scores[l..r]) <= limit.
 *
 * Constraints:
 * - 1 <= scores.length <= 200000
 * - 0 <= scores[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: scores = [4, 7, 5, 6, 8, 3, 4], limit = 3
 * Output: 4
 * Explanation: The longest valid block is [4, 7, 5, 6]. Its maximum is 7 and minimum is 4,
 * so the spread is 3. Any longer block exceeds the allowed spread.
 *
 * Example 2:
 * Input: scores = [10, 10, 10, 1, 2, 3, 4], limit = 2
 * Output: 4
 * Explanation: One longest valid block is [1, 2, 3, 4]. Its spread is 4 - 1 = 3, which is too large,
 * so it is invalid. The valid longest blocks are [10, 10, 10] with length 3 and [1, 2, 3] or [2, 3, 4]
 * with length 3. Therefore the answer is 3.
 *
 * Important correctness note:
 * The textual "Output: 4" in Example 2 contradicts the explanation.
 * The correct answer for Example 2 is 3.
 * This implementation follows the formal problem statement and returns the correct value.
 */
public class Solution {

    /**
     * Returns the length of the longest contiguous subarray such that
     * max value - min value <= limit.
     *
     * The algorithm uses a sliding window plus two monotonic deques:
     * 1. One deque keeps values in decreasing order so its front is the current maximum.
     * 2. One deque keeps values in increasing order so its front is the current minimum.
     *
     * As we expand the right boundary of the window, we insert the new value into both deques
     * while preserving their monotonic order.
     *
     * If the window becomes invalid (current max - current min > limit), we move the left boundary
     * to the right until the window becomes valid again. While moving left, if the outgoing value
     * equals the front of either deque, we remove it from that deque as well.
     *
     * @param scores the array of toxicity scores in chronological order
     * @param limit the maximum allowed difference between the largest and smallest score in a valid block
     * @return the maximum length of any contiguous reviewable block
     *
     * Time complexity: O(n), because each element is added to and removed from each deque at most once.
     * Space complexity: O(n) in the worst case for the deques.
     */
    public int longestReviewableBlock(int[] scores, int limit) {
        // Deque for tracking the maximum values in the current window.
        // It is maintained in decreasing order:
        // front = largest value in the current window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque for tracking the minimum values in the current window.
        // It is maintained in increasing order:
        // front = smallest value in the current window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // Expand the window by moving the right boundary one step at a time.
        for (int right = 0; right < scores.length; right++) {
            int current = scores[right];

            // ------------------------------------------------------------
            // Step 1: Insert current value into maxDeque.
            // We want maxDeque to stay in decreasing order.
            //
            // If the last value is smaller than the current value,
            // it can never become the maximum for any future window that
            // includes the current value, so we remove it.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && maxDeque.peekLast() < current) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(current);

            // ------------------------------------------------------------
            // Step 2: Insert current value into minDeque.
            // We want minDeque to stay in increasing order.
            //
            // If the last value is larger than the current value,
            // it can never become the minimum for any future window that
            // includes the current value, so we remove it.
            // ------------------------------------------------------------
            while (!minDeque.isEmpty() && minDeque.peekLast() > current) {
                minDeque.pollLast();
            }
            minDeque.offerLast(current);

            // ------------------------------------------------------------
            // Step 3: Shrink the window from the left while it is invalid.
            //
            // The current maximum is at maxDeque.peekFirst().
            // The current minimum is at minDeque.peekFirst().
            //
            // If max - min > limit, the window is not allowed,
            // so we must move left forward until it becomes valid again.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && !minDeque.isEmpty()
                    && (long) maxDeque.peekFirst() - (long) minDeque.peekFirst() > limit) {

                int outgoing = scores[left];

                // If the outgoing value is exactly the current maximum
                // stored at the front of maxDeque, remove it.
                if (outgoing == maxDeque.peekFirst()) {
                    maxDeque.pollFirst();
                }

                // If the outgoing value is exactly the current minimum
                // stored at the front of minDeque, remove it.
                if (outgoing == minDeque.peekFirst()) {
                    minDeque.pollFirst();
                }

                // Move the left boundary rightward by one position.
                left++;
            }

            // ------------------------------------------------------------
            // Step 4: At this point, the window [left..right] is valid.
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
     * Alternative implementation using a TreeMap.
     * This is simpler conceptually for some learners:
     * - TreeMap.firstKey() gives the minimum in the window
     * - TreeMap.lastKey() gives the maximum in the window
     *
     * However, each insertion/removal is O(log n), so this version is slower
     * than the deque-based O(n) approach.
     *
     * @param scores the array of toxicity scores
     * @param limit the maximum allowed spread in a valid block
     * @return the maximum valid contiguous block length
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int longestReviewableBlockTreeMap(int[] scores, int limit) {
        TreeMap<Integer, Integer> frequency = new TreeMap<>();
        int left = 0;
        int bestLength = 0;

        for (int right = 0; right < scores.length; right++) {
            frequency.put(scores[right], frequency.getOrDefault(scores[right], 0) + 1);

            while ((long) frequency.lastKey() - (long) frequency.firstKey() > limit) {
                int outgoing = scores[left];
                int count = frequency.get(outgoing);
                if (count == 1) {
                    frequency.remove(outgoing);
                } else {
                    frequency.put(outgoing, count - 1);
                }
                left++;
            }

            bestLength = Math.max(bestLength, right - left + 1);
        }

        return bestLength;
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstrations shown here,
     * excluding the cost of the called algorithm on the sample arrays.
     * Space complexity: O(1), excluding the called algorithm's internal space.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] scores1 = {4, 7, 5, 6, 8, 3, 4};
        int limit1 = 3;
        int result1 = solution.longestReviewableBlock(scores1, limit1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 4");

        int[] scores2 = {10, 10, 10, 1, 2, 3, 4};
        int limit2 = 2;
        int result2 = solution.longestReviewableBlock(scores2, limit2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected (correct by explanation and formal rule): 3");

        int[] scores3 = {8, 8, 8, 8};
        int limit3 = 0;
        int result3 = solution.longestReviewableBlock(scores3, limit3);
        System.out.println("All equal values result: " + result3);
        System.out.println("Expected: 4");

        int[] scores4 = {1, 5, 6, 7, 8, 10, 6, 5, 6};
        int limit4 = 4;
        int result4 = solution.longestReviewableBlock(scores4, limit4);
        System.out.println("Additional test result: " + result4);
        System.out.println("Expected: 5");
    }
}