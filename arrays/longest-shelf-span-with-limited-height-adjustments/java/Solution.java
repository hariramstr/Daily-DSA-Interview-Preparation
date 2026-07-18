/*
Problem Title: Longest Shelf Span With Limited Height Adjustments

Problem Description:
A warehouse stores products on a long row of shelves. The height of the product stack on shelf i is given by heights[i].
For a promotional display, the manager wants to choose one contiguous span of shelves and make all stack heights in that
span equal. You are allowed to increase shelf heights, but you cannot decrease any shelf. Increasing a shelf by 1 unit
costs 1 adjustment.

Given an integer array heights and a long integer budget, return the maximum length of a contiguous subarray that can be
made to have the same height using at most budget total adjustments.

When choosing a span, you may raise shorter shelves to match the tallest shelf already inside that span. Because
decreasing is not allowed, the final common height must be at least the maximum value in the chosen subarray, and
choosing the current maximum is always optimal.

Your task is to compute the largest possible span length.

Constraints:
- 1 <= heights.length <= 100000
- 1 <= heights[i] <= 1000000000
- 0 <= budget <= 100000000000000

Examples:
1) heights = [3,1,2,2,4], budget = 3
   Correct output for the contiguous version is 3.
   Valid longest spans include [3,1,2] raised to 3 with cost 3, and [1,2,2] raised to 2 with cost 1.
   No contiguous span of length 4 fits within budget 3.

2) heights = [5,5,5,5], budget = 0
   Output: 4

Important note:
The original prompt text contains an inconsistency claiming Example 1 outputs 4, but that is impossible for the
contiguous version under budget 3. This implementation solves the stated contiguous problem correctly, and for
Example 1 it returns 3.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum length of a contiguous subarray that can be made equal
     * by only increasing elements, with total cost at most {@code budget}.
     *
     * Core idea:
     * For any fixed subarray [l..r], the cheapest final height is exactly the maximum
     * value inside that subarray. So the cost is:
     *
     *     max(heights[l..r]) * length - sum(heights[l..r])
     *
     * We need the longest contiguous subarray whose cost is <= budget.
     *
     * To do this efficiently:
     * 1. Binary search on the answer length.
     * 2. For a candidate length len, check whether any window of size len is feasible.
     * 3. During the feasibility check, maintain:
     *    - the window sum
     *    - the window maximum using a monotonic deque
     *
     * This gives O(n log n) time overall.
     *
     * @param heights the shelf heights
     * @param budget the maximum total amount of allowed increases
     * @return the maximum feasible contiguous span length
     * Time complexity: O(n log n)
     * Space complexity: O(n) in the worst case due to the deque
     */
    public int longestShelfSpan(int[] heights, long budget) {
        int n = heights.length;

        int left = 1;
        int right = n;
        int answer = 1;

        // Standard binary search on the span length.
        // If some length "mid" is feasible, then every smaller length is also feasible,
        // so we can search to the right for a larger answer.
        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (canMakeSpanOfLength(heights, budget, mid)) {
                answer = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether there exists at least one contiguous subarray of length {@code len}
     * that can be made equal using at most {@code budget} total increases.
     *
     * For each window:
     * - Let maxVal be the maximum in the window.
     * - Let sum be the sum of the window.
     * - Required cost = maxVal * len - sum
     *
     * We slide the window across the array:
     * - Update the sum in O(1)
     * - Maintain the maximum with a decreasing deque in amortized O(1)
     *
     * @param heights the shelf heights
     * @param budget the maximum total amount of allowed increases
     * @param len the candidate window length to test
     * @return true if some window of size {@code len} is feasible, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(n) in the worst case
     */
    public boolean canMakeSpanOfLength(int[] heights, long budget, int len) {
        int n = heights.length;
        if (len <= 0 || len > n) {
            return false;
        }

        // This deque stores indices.
        // We maintain heights[deque[0]] >= heights[deque[1]] >= ...
        // Therefore, the front always holds the index of the maximum element
        // in the current window.
        Deque<Integer> deque = new ArrayDeque<>();

        long windowSum = 0L;

        // Build the first window [0 .. len-1].
        for (int i = 0; i < len; i++) {
            windowSum += heights[i];

            // Remove smaller or equal values from the back because they can never
            // become the maximum while the current element remains in the window.
            while (!deque.isEmpty() && heights[deque.peekLast()] <= heights[i]) {
                deque.pollLast();
            }
            deque.offerLast(i);
        }

        // Check the first window.
        long maxVal = heights[deque.peekFirst()];
        long cost = maxVal * len - windowSum;
        if (cost <= budget) {
            return true;
        }

        // Slide the window one step at a time.
        for (int right = len; right < n; right++) {
            int left = right - len;

            // Remove the element leaving the window from the sum.
            windowSum -= heights[left];

            // If the outgoing index is at the front of the deque,
            // remove it because it is no longer inside the window.
            if (!deque.isEmpty() && deque.peekFirst() == left) {
                deque.pollFirst();
            }

            // Add the new element to the sum.
            windowSum += heights[right];

            // Maintain decreasing order in the deque.
            while (!deque.isEmpty() && heights[deque.peekLast()] <= heights[right]) {
                deque.pollLast();
            }
            deque.offerLast(right);

            // The current maximum is at the front.
            maxVal = heights[deque.peekFirst()];
            cost = maxVal * len - windowSum;

            if (cost <= budget) {
                return true;
            }
        }

        return false;
    }

    /**
     * Convenience wrapper matching the problem statement naming style.
     *
     * @param heights the shelf heights
     * @param budget the maximum total amount of allowed increases
     * @return the maximum feasible contiguous span length
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int solve(int[] heights, long budget) {
        return longestShelfSpan(heights, budget);
    }

    /**
     * Demonstrates the algorithm on sample and additional test cases.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called methods
     * Space complexity: O(1), excluding called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] heights1 = {3, 1, 2, 2, 4};
        long budget1 = 3L;
        System.out.println("Example 1:");
        System.out.println("heights = " + Arrays.toString(heights1) + ", budget = " + budget1);
        System.out.println("Maximum contiguous span length = " + solution.solve(heights1, budget1));
        System.out.println("Note: For the contiguous problem, the correct result is 3.");

        int[] heights2 = {5, 5, 5, 5};
        long budget2 = 0L;
        System.out.println();
        System.out.println("Example 2:");
        System.out.println("heights = " + Arrays.toString(heights2) + ", budget = " + budget2);
        System.out.println("Maximum contiguous span length = " + solution.solve(heights2, budget2));

        int[] heights3 = {1, 2, 3, 4};
        long budget3 = 3L;
        System.out.println();
        System.out.println("Additional Example 3:");
        System.out.println("heights = " + Arrays.toString(heights3) + ", budget = " + budget3);
        System.out.println("Maximum contiguous span length = " + solution.solve(heights3, budget3));

        int[] heights4 = {10};
        long budget4 = 100L;
        System.out.println();
        System.out.println("Additional Example 4:");
        System.out.println("heights = " + Arrays.toString(heights4) + ", budget = " + budget4);
        System.out.println("Maximum contiguous span length = " + solution.solve(heights4, budget4));
    }
}