/*
 * Maximum Flavor Score in a Tasting Window
 * =========================================
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A food critic is tasting dishes served in a sequence. Each dish has a flavor score,
 * and the critic can only evaluate a contiguous segment of dishes at a time. However,
 * due to palate fatigue, the critic's tasting window has a constraint: the difference
 * between the maximum and minimum flavor scores within the window must not exceed a
 * given threshold `t`.
 *
 * Given an integer array `flavors` where `flavors[i]` represents the flavor score of
 * the i-th dish, and an integer `t`, return the maximum number of dishes the critic
 * can taste in a single contiguous window such that the difference between the maximum
 * and minimum flavor scores in that window is at most `t`.
 *
 * Constraints:
 * - 1 <= flavors.length <= 10^5
 * - 0 <= flavors[i] <= 10^4
 * - 0 <= t <= 10^4
 *
 * Example 1:
 * Input: flavors = [4, 8, 5, 1, 7, 9, 6], t = 4
 * Output: 4
 * Explanation: The window [5, 7, 9, 6] (indices 2..6 => [5,1,7,9,6]? Let's check:
 *   index 2=5, 3=1, 4=7, 5=9, 6=6 => max=9, min=1, diff=8 > 4. Not valid.
 *   Best valid: [7, 9, 6] length 3, [4, 8, 5] length 3, [5, 7, 9, 6]? 
 *   Actually indices 3..6 = [1,7,9,6] max=9,min=1,diff=8>4. 
 *   Indices 4..6 = [7,9,6] max=9,min=6,diff=3<=4, length=3.
 *   Wait, the problem says answer is 4 via window [5,7,9,6]... 
 *   Let me recheck: flavors=[4,8,5,1,7,9,6]
 *   indices 2..5 = [5,1,7,9] max=9,min=1,diff=8>4
 *   indices 3..6 = [1,7,9,6] max=9,min=1,diff=8>4
 *   indices 4..6 = [7,9,6] length=3
 *   indices 0..2 = [4,8,5] max=8,min=4,diff=4<=4, length=3
 *   Hmm, the problem explanation itself says answer=4 via [5,7,9,6] but that seems wrong.
 *   The problem's own explanation is contradictory. Let me trust the final answer=4
 *   and find a window of length 4:
 *   [4,8,5,1]: max=8,min=1,diff=7>4. No.
 *   [8,5,1,7]: max=8,min=1,diff=7>4. No.
 *   [5,1,7,9]: max=9,min=1,diff=8>4. No.
 *   [1,7,9,6]: max=9,min=1,diff=8>4. No.
 *   No window of length 4 is valid! So the answer should be 3, not 4.
 *   The problem statement has an error. We'll implement the correct algorithm and
 *   our answer for Example 1 will be 3.
 *
 * Example 2:
 * Input: flavors = [10, 10, 10, 10], t = 0
 * Output: 4
 */

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Solution class for the "Maximum Flavor Score in a Tasting Window" problem.
 *
 * <p>Approach: Sliding Window with two Monotonic Deques
 * We maintain a window [left, right] and use two deques:
 * - maxDeque: keeps indices in decreasing order of flavors (front = current max)
 * - minDeque: keeps indices in increasing order of flavors (front = current min)
 * When the window's (max - min) exceeds t, we shrink from the left.
 */
public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray (window) such that
     * the difference between the maximum and minimum values in that window
     * is at most {@code t}.
     *
     * <p>Algorithm: Sliding Window + Two Monotonic Deques
     * <ul>
     *   <li>Expand the right boundary one element at a time.</li>
     *   <li>Maintain a max-deque (descending) and a min-deque (ascending).</li>
     *   <li>If max - min > t, advance the left pointer and clean up deques.</li>
     *   <li>Track the maximum window size seen so far.</li>
     * </ul>
     *
     * @param flavors integer array of flavor scores (non-negative)
     * @param t       the maximum allowed difference between max and min in the window
     * @return the length of the longest valid contiguous window
     *
     * @implNote Time complexity:  O(n) — each element is added/removed from each deque at most once.
     *           Space complexity: O(n) — in the worst case both deques hold all indices.
     */
    public int maxTastingWindow(int[] flavors, int t) {
        int n = flavors.length;

        // Edge case: empty array
        if (n == 0) return 0;

        // ---------------------------------------------------------------
        // Step 1: Set up two monotonic deques.
        //
        // maxDeque stores indices such that flavors[maxDeque.peekFirst()]
        // is always the MAXIMUM of the current window.
        // We keep it in DECREASING order of flavor values.
        //
        // minDeque stores indices such that flavors[minDeque.peekFirst()]
        // is always the MINIMUM of the current window.
        // We keep it in INCREASING order of flavor values.
        // ---------------------------------------------------------------
        Deque<Integer> maxDeque = new ArrayDeque<>(); // front = index of current max
        Deque<Integer> minDeque = new ArrayDeque<>(); // front = index of current min

        // ---------------------------------------------------------------
        // Step 2: Initialize the sliding window pointers and result.
        // 'left'  = left boundary of the current window (inclusive)
        // 'right' = right boundary (we expand this in the loop)
        // 'maxLen' = best (longest) valid window length found so far
        // ---------------------------------------------------------------
        int left = 0;
        int maxLen = 0;

        // ---------------------------------------------------------------
        // Step 3: Expand the window by moving 'right' from 0 to n-1.
        // ---------------------------------------------------------------
        for (int right = 0; right < n; right++) {
            int currentFlavor = flavors[right];

            // -----------------------------------------------------------
            // Step 3a: Update maxDeque.
            // Remove all indices from the BACK of maxDeque whose flavor
            // values are LESS THAN OR EQUAL TO currentFlavor, because
            // they can never be the maximum while currentFlavor is in
            // the window (currentFlavor is both larger and more recent).
            // -----------------------------------------------------------
            while (!maxDeque.isEmpty() && flavors[maxDeque.peekLast()] <= currentFlavor) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right); // add current index to the back

            // -----------------------------------------------------------
            // Step 3b: Update minDeque.
            // Remove all indices from the BACK of minDeque whose flavor
            // values are GREATER THAN OR EQUAL TO currentFlavor, because
            // they can never be the minimum while currentFlavor is in
            // the window.
            // -----------------------------------------------------------
            while (!minDeque.isEmpty() && flavors[minDeque.peekLast()] >= currentFlavor) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right); // add current index to the back

            // -----------------------------------------------------------
            // Step 3c: Check the window validity.
            // The current window is [left, right].
            // max of window = flavors[maxDeque.peekFirst()]
            // min of window = flavors[minDeque.peekFirst()]
            // If their difference exceeds t, we must shrink from the left.
            // -----------------------------------------------------------
            while (flavors[maxDeque.peekFirst()] - flavors[minDeque.peekFirst()] > t) {
                // The window [left, right] is invalid; move left forward.
                left++;

                // Remove stale indices from the fronts of both deques.
                // An index is "stale" if it is now to the left of 'left'.
                if (maxDeque.peekFirst() < left) {
                    maxDeque.pollFirst();
                }
                if (minDeque.peekFirst() < left) {
                    minDeque.pollFirst();
                }
            }

            // -----------------------------------------------------------
            // Step 3d: At this point, [left, right] is a valid window.
            // Update maxLen if this window is longer than any seen before.
            // Window length = right - left + 1
            // -----------------------------------------------------------
            int windowLength = right - left + 1;
            if (windowLength > maxLen) {
                maxLen = windowLength;
            }
        }

        // ---------------------------------------------------------------
        // Step 4: Return the length of the longest valid window found.
        // ---------------------------------------------------------------
        return maxLen;
    }

    // ===================================================================
    // Helper: print a labeled test result
    // ===================================================================
    private static void printResult(String label, int[] flavors, int t, int result) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < flavors.length; i++) {
            sb.append(flavors[i]);
            if (i < flavors.length - 1) sb.append(", ");
        }
        sb.append("]");
        System.out.println(label);
        System.out.println("  flavors = " + sb + ", t = " + t);
        System.out.println("  Result  = " + result);
        System.out.println();
    }

    /**
     * Demonstrates the solution with several test cases and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // ------------------------------------------------------------------
        // Example 1 from the problem:
        // flavors = [4, 8, 5, 1, 7, 9, 6], t = 4
        //
        // Let's manually trace the algorithm:
        //
        // right=0, val=4: maxD=[0], minD=[0], window=[4], max=4,min=4,diff=0<=4 -> len=1
        // right=1, val=8: maxD=[1], minD=[0,1], window=[4,8], max=8,min=4,diff=4<=4 -> len=2
        // right=2, val=5: maxD=[1,2], minD=[0,2], window=[4,8,5], max=8,min=4,diff=4<=4 -> len=3
        // right=3, val=1: maxD=[1,2,3], minD=[3], window=[4,8,5,1], max=8,min=1,diff=7>4
        //   -> left=1, maxD front=1>=1 ok, minD front=3>=1 ok
        //   -> window=[8,5,1], max=8,min=1,diff=7>4
        //   -> left=2, maxD front=1<2 -> poll -> maxD=[2,3], minD front=3>=2 ok
        //   -> window=[5,1], max=5,min=1,diff=4<=4 -> len=2
        // right=4, val=7: maxD=[4], minD=[3,4]? 
        //   Actually: maxD before adding: [2,3], val=7 > flavors[3]=1 and flavors[2]=5
        //     remove 3 (val=1<=7), remove 2 (val=5<=7) -> maxD=[4]
        //   minD before adding: [3], val=7 >= flavors[3]=1? No, 7>=1 so remove? 
        //     Wait minDeque removes from back if flavors[back] >= currentFlavor.
        //     flavors[3]=1, currentFlavor=7: 1 >= 7? No. So minD=[3,4].
        //   window=[5,1,7], max=7,min=1,diff=6>4
        //   -> left=3, maxD front=4>=3 ok, minD front=3>=3 ok
        //   -> window=[1,7], max=7,min=1,diff=6>4
        //   -> left=4, maxD front=4>=4 ok, minD front=3<4 -> poll -> minD=[4]
        //   -> window=[7], max=7,min=7,diff=0<=4 -> len=1
        // right=5, val=9: maxD: remove 4(val=7<=9) -> maxD=[5]; minD: 7>=9? No -> minD=[4,5]
        //   window=[7,9], max=9,min=7,diff=2<=4 -> len=2
        // right=6, val=6: maxD: 6<=9? No -> maxD=[5,6]; minD: 9>=6 yes remove 5, 7>=6 yes remove 4 -> minD=[6]
        //   window=[7,9,6], max=9,min=6,diff=3<=4 -> len=3
        //
        // Maximum length = 3.
        // Note: The problem statement claims 4, but no window of length 4 satisfies diff<=4.
        // Our algorithm correctly returns 3.
        // ------------------------------------------------------------------
        int[] flavors1 = {4, 8, 5, 1, 7, 9, 6};
        int t1 = 4;
        int result1 = sol.maxTastingWindow(flavors1, t1);
        printResult("Example 1 (problem says 4, but correct answer is 3):", flavors1, t1, result1);

        // ------------------------------------------------------------------
        // Example 2 from the problem:
        // flavors = [10, 10, 10, 10], t = 0
        // All values equal, diff always 0 <= 0. Entire array valid. Answer = 4.
        // ------------------------------------------------------------------
        int[] flavors2 = {10, 10, 10, 10};
        int t2 = 0;
        int result2 = sol.maxTastingWindow(flavors2, t2);
        printResult("Example 2 (expected 4):", flavors2, t2, result2);

        // ------------------------------------------------------------------
        // Additional test: single element
        // flavors = [5], t = 0 -> answer = 1
        // ------------------------------------------------------------------
        int[] flavors3 = {5};
        int t3 = 0;
        int result3 = sol.maxTastingWindow(flavors3, t3);
        printResult("Single element (expected 1):", flavors3, t3, result3);

        // ------------------------------------------------------------------
        // Additional test: all same
        // flavors = [3, 3, 3, 3, 3], t = 0 -> answer = 5
        // ------------------------------------------------------------------
        int[] flavors4 = {3, 3, 3, 3, 3};
        int t4 = 0;
        