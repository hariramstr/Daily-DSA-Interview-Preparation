import java.util.*;

/*
 * Title: Longest Stable Altitude Stretch
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array altitudes where altitudes[i] represents the altitude reading
 * of a drone at the i-th second. A flight segment is considered stable if the difference between
 * the highest and lowest altitude inside that contiguous segment is at most limit.
 *
 * Your task is to return the length of the longest stable contiguous segment.
 *
 * In other words, find the maximum length of a subarray altitudes[l..r] such that:
 * max(altitudes[l..r]) - min(altitudes[l..r]) <= limit
 *
 * This models a real monitoring system where a drone is considered to be flying smoothly only
 * when its altitude fluctuations stay within a safe tolerance.
 *
 * You should design an algorithm that works efficiently for large inputs.
 * A brute-force solution that checks every subarray will be too slow.
 *
 * Constraints:
 * - 1 <= altitudes.length <= 100000
 * - -1000000000 <= altitudes[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 *
 * Example 1:
 * Input: altitudes = [30, 32, 31, 35, 34, 33], limit = 3
 * Output: 3
 * Explanation:
 * Segments of length 4 such as [32, 31, 35, 34] and [31, 35, 34, 33] have max-min = 4,
 * so they are not stable. Valid longest segments have length 3, such as [30, 32, 31]
 * or [35, 34, 33].
 *
 * Example 2:
 * Input: altitudes = [100, 100, 100, 100], limit = 0
 * Output: 4
 * Explanation:
 * All readings are identical, so every contiguous segment is stable.
 * The whole array is the longest stable segment.
 *
 * Notes:
 * - A segment must be contiguous.
 * - Negative altitude values are allowed because the drone may fly below a chosen reference level.
 * - An O(n log n) or O(n) sliding window approach is expected.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose maximum value minus minimum value
     * is at most the given limit.
     *
     * We use a sliding window plus two monotonic deques:
     * 1. One deque keeps values in decreasing order, so its front is the maximum in the window.
     * 2. One deque keeps values in increasing order, so its front is the minimum in the window.
     *
     * As we expand the right side of the window:
     * - We insert the new element into both deques while preserving monotonic order.
     * - If the window becomes invalid (max - min > limit), we move the left side forward
     *   until the window becomes valid again.
     * - During shrinking, if the element leaving the window is currently at the front of a deque,
     *   we remove it from that deque.
     *
     * @param altitudes the array of altitude readings
     * @param limit the maximum allowed difference between highest and lowest altitude in a stable segment
     * @return the maximum length of a stable contiguous segment
     * Time complexity: O(n), because each element is added and removed from each deque at most once.
     * Space complexity: O(n), in the worst case for the deques.
     */
    public int longestStableAltitudeStretch(int[] altitudes, int limit) {
        // Defensive handling for completeness.
        // According to constraints, altitudes.length >= 1, but this makes the method safer.
        if (altitudes == null || altitudes.length == 0) {
            return 0;
        }

        // maxDeque will store values in decreasing order.
        // Front of deque = current maximum in the window.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // minDeque will store values in increasing order.
        // Front of deque = current minimum in the window.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // left marks the start of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Expand the window by moving right from 0 to n - 1.
        for (int right = 0; right < altitudes.length; right++) {
            int current = altitudes[right];

            // ------------------------------------------------------------
            // STEP 1: Insert current value into maxDeque.
            // We want maxDeque to remain in decreasing order.
            //
            // Why remove smaller values from the back?
            // Because if current is larger than them, those smaller values can never become
            // the maximum for any future window that includes current.
            // So they are useless and can be discarded.
            // ------------------------------------------------------------
            while (!maxDeque.isEmpty() && maxDeque.peekLast() < current) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(current);

            // ------------------------------------------------------------
            // STEP 2: Insert current value into minDeque.
            // We want minDeque to remain in increasing order.
            //
            // Why remove larger values from the back?
            // Because if current is smaller than them, those larger values can never become
            // the minimum for any future window that includes current.
            // ------------------------------------------------------------
            while (!minDeque.isEmpty() && minDeque.peekLast() > current) {
                minDeque.pollLast();
            }
            minDeque.offerLast(current);

            // ------------------------------------------------------------
            // STEP 3: If the current window is invalid, shrink it from the left.
            //
            // The window is invalid when:
            // current maximum - current minimum > limit
            //
            // maxDeque.peekFirst() gives the maximum in the current window.
            // minDeque.peekFirst() gives the minimum in the current window.
            //
            // We cast to long before subtraction to be extra safe, even though the given
            // constraints still fit in int for this difference.
            // ------------------------------------------------------------
            while ((long) maxDeque.peekFirst() - (long) minDeque.peekFirst() > limit) {
                int leaving = altitudes[left];

                // If the value leaving the window is exactly the current maximum
                // stored at the front of maxDeque, remove it from maxDeque.
                if (leaving == maxDeque.peekFirst()) {
                    maxDeque.pollFirst();
                }

                // If the value leaving the window is exactly the current minimum
                // stored at the front of minDeque, remove it from minDeque.
                if (leaving == minDeque.peekFirst()) {
                    minDeque.pollFirst();
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: At this point, the window [left..right] is valid.
            // Update the best answer using its length.
            // ------------------------------------------------------------
            int windowLength = right - left + 1;
            best = Math.max(best, windowLength);
        }

        return best;
    }

    /**
     * A second public method that solves the same problem using a TreeMap.
     * This version is slightly slower than the deque solution but still efficient.
     *
     * It maintains counts of values inside the current sliding window:
     * - map.firstKey() is the minimum value in the window
     * - map.lastKey() is the maximum value in the window
     *
     * @param altitudes the array of altitude readings
     * @param limit the maximum allowed difference between highest and lowest altitude in a stable segment
     * @return the maximum length of a stable contiguous segment
     * Time complexity: O(n log n), because each insertion/removal in TreeMap costs O(log n).
     * Space complexity: O(n), in the worst case for the map.
     */
    public int longestStableAltitudeStretchTreeMap(int[] altitudes, int limit) {
        if (altitudes == null || altitudes.length == 0) {
            return 0;
        }

        TreeMap<Integer, Integer> frequency = new TreeMap<>();
        int left = 0;
        int best = 0;

        for (int right = 0; right < altitudes.length; right++) {
            frequency.put(altitudes[right], frequency.getOrDefault(altitudes[right], 0) + 1);

            while ((long) frequency.lastKey() - (long) frequency.firstKey() > limit) {
                int leaving = altitudes[left];
                int count = frequency.get(leaving);

                if (count == 1) {
                    frequency.remove(leaving);
                } else {
                    frequency.put(leaving, count - 1);
                }

                left++;
            }

            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Runs sample demonstrations from the problem statement and prints the results.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the fixed demo calls, excluding the algorithm calls themselves.
     * Space complexity: O(1), excluding the algorithm calls themselves.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] altitudes1 = {30, 32, 31, 35, 34, 33};
        int limit1 = 3;
        int result1 = solution.longestStableAltitudeStretch(altitudes1, limit1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 3");

        int[] altitudes2 = {100, 100, 100, 100};
        int limit2 = 0;
        int result2 = solution.longestStableAltitudeStretch(altitudes2, limit2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 4");

        // Additional quick checks for beginner-friendly demonstration.
        int[] altitudes3 = {8};
        int limit3 = 10;
        int result3 = solution.longestStableAltitudeStretch(altitudes3, limit3);
        System.out.println("Single element result: " + result3);
        System.out.println("Expected: 1");

        int[] altitudes4 = {1, 5, 6, 7, 8, 10, 6, 5, 6};
        int limit4 = 4;
        int result4 = solution.longestStableAltitudeStretch(altitudes4, limit4);
        System.out.println("Additional test result: " + result4);

        // Demonstrate the TreeMap version too.
        int treeMapResult1 = solution.longestStableAltitudeStretchTreeMap(altitudes1, limit1);
        System.out.println("Example 1 result using TreeMap: " + treeMapResult1);
        System.out.println("Expected: 3");
    }
}