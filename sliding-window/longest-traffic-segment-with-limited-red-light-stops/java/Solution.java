import java.util.*;

/*
 * Title: Longest Traffic Segment With Limited Red-Light Stops
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a road represented by an array lights of length n, where lights[i] is either 0 or 1.
 * A value of 1 means the traffic light at intersection i is green when you arrive, and 0 means it is red.
 * You are also given an integer k and an array penalty of length n, where penalty[i] is the delay cost
 * of stopping at intersection i if that light is red. A driver wants to choose one contiguous road segment [l, r]
 * to travel through.
 *
 * A segment is considered feasible if:
 * 1. It contains at most k red lights, and
 * 2. Among the red lights inside the segment, the maximum penalty minus the minimum penalty is at most limit.
 *
 * Return the length of the longest feasible contiguous segment.
 *
 * In other words, you must find the largest window where the number of red lights is bounded, and the red-light
 * penalties inside that same window stay tightly clustered. Green lights do not affect the penalty range and
 * should be ignored for the second condition.
 *
 * If a segment contains no red lights, it is always valid as long as condition 1 holds.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - lights.length == penalty.length == n
 * - lights[i] is 0 or 1
 * - 0 <= penalty[i] <= 10^9
 * - 0 <= k <= n
 * - 0 <= limit <= 10^9
 *
 * Example 1:
 * Input: lights = [1,0,0,1,0,1,0], penalty = [5,8,10,3,9,4,11], k = 3, limit = 2
 * Output: 5
 * Explanation: The segment [0,4] contains red penalties [8,10,9]. There are 3 red lights, and max-min = 10-8 = 2,
 * so the segment is valid and has length 5. Any longer segment violates either the red-light count or the
 * penalty-range condition.
 *
 * Example 2:
 * Input: lights = [0,1,0,0,1,0], penalty = [7,2,20,9,1,10], k = 2, limit = 3
 * Output: 3
 * Explanation: The segment [3,5] has red penalties [9,10], so max-min = 1 and there are 2 red lights.
 * Its length is 3. Longer windows either contain 3 red lights or have red penalties whose spread exceeds 3.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous segment such that:
     * 1) the segment contains at most k red lights, and
     * 2) among red lights in the segment, max penalty - min penalty <= limit.
     *
     * The algorithm uses a classic sliding window with two monotonic deques:
     * - one deque keeps red-light penalties in increasing order so its front is the minimum,
     * - one deque keeps red-light penalties in decreasing order so its front is the maximum.
     *
     * Important detail:
     * Green lights do not participate in the penalty range condition, so they are completely ignored
     * by the deques. Only red-light indices are inserted into the deques.
     *
     * As we expand the right end of the window:
     * - if the new element is red, we add it to both deques while preserving monotonic order,
     * - we increment the red count.
     *
     * Then, while the window is invalid:
     * - either red count > k
     * - or current max red penalty - current min red penalty > limit
     * we move the left end forward and remove outdated red indices from the deques.
     *
     * Because each index enters and leaves each deque at most once, the total work is linear.
     *
     * @param lights  binary array where 0 means red light and 1 means green light
     * @param penalty penalty[i] is the stop-delay cost if intersection i is red
     * @param k       maximum allowed number of red lights inside the chosen segment
     * @param limit   maximum allowed difference between largest and smallest red-light penalties in the segment
     * @return the maximum feasible segment length
     *
     * Time complexity: O(n), because every index is added to and removed from each deque at most once.
     * Space complexity: O(n) in the worst case for the deques.
     */
    public int longestTrafficSegment(int[] lights, int[] penalty, int k, int limit) {
        int n = lights.length;

        // Deque for minimum red penalty in current window.
        // It stores indices of RED lights only.
        // The penalties at these indices are in increasing order.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Deque for maximum red penalty in current window.
        // It stores indices of RED lights only.
        // The penalties at these indices are in decreasing order.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        int left = 0;
        int redCount = 0;
        int best = 0;

        // Expand the window one position at a time by moving "right".
        for (int right = 0; right < n; right++) {

            // If the new position is a red light, it affects both constraints:
            // - red count
            // - penalty range among red lights
            if (lights[right] == 0) {
                redCount++;

                // Maintain minDeque in increasing order of penalty.
                // While the last stored red penalty is >= current red penalty,
                // it can never become the minimum for any future window that also contains "right",
                // so we remove it.
                while (!minDeque.isEmpty() && penalty[minDeque.peekLast()] >= penalty[right]) {
                    minDeque.pollLast();
                }
                minDeque.offerLast(right);

                // Maintain maxDeque in decreasing order of penalty.
                // While the last stored red penalty is <= current red penalty,
                // it can never become the maximum for any future window that also contains "right",
                // so we remove it.
                while (!maxDeque.isEmpty() && penalty[maxDeque.peekLast()] <= penalty[right]) {
                    maxDeque.pollLast();
                }
                maxDeque.offerLast(right);
            }

            // Shrink the window from the left until it becomes valid again.
            // The window is invalid if:
            // 1) it contains too many red lights, OR
            // 2) among red lights, max penalty - min penalty is too large.
            while (isInvalid(redCount, k, minDeque, maxDeque, penalty, limit)) {

                // If the element leaving the window is red, it may affect:
                // - red count
                // - the fronts of the deques if that exact red index is currently stored there
                if (lights[left] == 0) {
                    redCount--;

                    // Remove from minDeque if the outgoing red index is at the front.
                    if (!minDeque.isEmpty() && minDeque.peekFirst() == left) {
                        minDeque.pollFirst();
                    }

                    // Remove from maxDeque if the outgoing red index is at the front.
                    if (!maxDeque.isEmpty() && maxDeque.peekFirst() == left) {
                        maxDeque.pollFirst();
                    }
                }

                // Move left boundary rightward by one.
                left++;
            }

            // At this point the window [left, right] is valid.
            // Update the best answer.
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Checks whether the current sliding window violates any rule.
     *
     * The window is invalid if:
     * - the number of red lights exceeds k, or
     * - the spread of red penalties exceeds limit.
     *
     * If there are no red lights in the window, both deques are empty and the penalty-range condition
     * is automatically satisfied.
     *
     * @param redCount current number of red lights in the window
     * @param k        maximum allowed red lights
     * @param minDeque deque whose front stores the index of the minimum red penalty in the window
     * @param maxDeque deque whose front stores the index of the maximum red penalty in the window
     * @param penalty  penalty array
     * @param limit    maximum allowed penalty spread among red lights
     * @return true if the window is invalid; false otherwise
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isInvalid(int redCount, int k, Deque<Integer> minDeque, Deque<Integer> maxDeque, int[] penalty, int limit) {
        if (redCount > k) {
            return true;
        }

        // If there are no red lights, there is no penalty spread to check.
        if (minDeque.isEmpty()) {
            return false;
        }

        long minPenalty = penalty[minDeque.peekFirst()];
        long maxPenalty = penalty[maxDeque.peekFirst()];

        return maxPenalty - minPenalty > limit;
    }

    /**
     * Runs the sample demonstrations from the problem statement.
     *
     * @param args command-line arguments, not used
     *
     * @return nothing
     *
     * Time complexity: O(1) for the fixed sample demonstrations, excluding the called algorithm.
     * Space complexity: O(1), excluding the called algorithm.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] lights1 = {1, 0, 0, 1, 0, 1, 0};
        int[] penalty1 = {5, 8, 10, 3, 9, 4, 11};
        int k1 = 3;
        int limit1 = 2;
        int result1 = solution.longestTrafficSegment(lights1, penalty1, k1, limit1);
        System.out.println("Example 1 Output: " + result1); // Expected: 5

        int[] lights2 = {0, 1, 0, 0, 1, 0};
        int[] penalty2 = {7, 2, 20, 9, 1, 10};
        int k2 = 2;
        int limit2 = 3;
        int result2 = solution.longestTrafficSegment(lights2, penalty2, k2, limit2);
        System.out.println("Example 2 Output: " + result2); // Expected: 3

        // Additional quick sanity checks.

        // All green lights: entire array is valid.
        int[] lights3 = {1, 1, 1, 1};
        int[] penalty3 = {10, 20, 30, 40};
        System.out.println("All green Output: " + solution.longestTrafficSegment(lights3, penalty3, 0, 0)); // Expected: 4

        // k = 0, only windows with no red lights are allowed.
        int[] lights4 = {1, 0, 1, 1, 0, 1};
        int[] penalty4 = {5, 7, 2, 3, 9, 1};
        System.out.println("k = 0 Output: " + solution.longestTrafficSegment(lights4, penalty4, 0, 100)); // Expected: 2

        // Single element.
        int[] lights5 = {0};
        int[] penalty5 = {42};
        System.out.println("Single element Output: " + solution.longestTrafficSegment(lights5, penalty5, 1, 0)); // Expected: 1
    }
}