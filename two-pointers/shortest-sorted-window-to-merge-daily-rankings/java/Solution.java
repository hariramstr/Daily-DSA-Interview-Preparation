import java.util.*;

/*
 * Title: Shortest Sorted Window to Merge Daily Rankings
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A product team stores yesterday's leaderboard scores in a non-decreasing integer array
 * `yesterday` and today's newly processed score updates in another non-decreasing integer
 * array `today`. The two arrays are already sorted individually, but the team wants to
 * publish a single combined ranking stream without fully merging both arrays.
 *
 * Your task is to find the length of the shortest contiguous window in the virtual merged
 * array (the array that would result from merging `yesterday` and `today` in sorted order)
 * whose sum is at least `target`.
 *
 * You are not allowed to explicitly build the merged array if you want an efficient solution
 * for large inputs. Instead, design an algorithm that uses the sorted structure of both arrays
 * and a two-pointer / sliding window strategy over the virtual merge.
 *
 * Return the minimum possible window length. If no contiguous window in the merged order has
 * sum at least `target`, return `-1`.
 *
 * Notes:
 * - All values are positive integers, which guarantees that expanding the window increases
 *   or preserves its sum and shrinking it decreases or preserves its sum.
 * - The window must be contiguous in the merged sorted order, not separately inside one input array.
 * - The merged array is conceptual; an optimal solution should process elements as if they were
 *   being merged on the fly.
 *
 * Constraints:
 * - 1 <= yesterday.length, today.length <= 10^5
 * - 1 <= yesterday[i], today[i] <= 10^4
 * - Both yesterday and today are sorted in non-decreasing order
 * - 1 <= target <= 10^9
 * - Expected time complexity: O(n + m) where n = yesterday.length and m = today.length
 * - Expected extra space: O(1) or O(log(n+m)) depending on implementation details
 *
 * Example 1:
 * Input: yesterday = [1, 4, 7], today = [2, 3, 8], target = 11
 * Output: 2
 * Explanation:
 * The virtual merged array is [1, 2, 3, 4, 7, 8].
 * The shortest contiguous window with sum at least 11 is [3, 8] or [4, 7], both of length 2.
 *
 * Example 2:
 * Input: yesterday = [2, 2, 5], today = [1, 6, 9], target = 15
 * Output: 2
 * Explanation:
 * The virtual merged array is [1, 2, 2, 5, 6, 9].
 * A shortest valid window is [6, 9], whose sum is 15, so the answer is 2.
 */

public class Solution {

    /**
     * Finds the minimum length of a contiguous window in the virtual merged sorted array
     * whose sum is at least the given target.
     *
     * The key idea is:
     * 1. We conceptually merge the two sorted arrays without actually building the merged array.
     * 2. We use a sliding window over that conceptual merged order.
     * 3. Because all numbers are positive, once the current window sum reaches the target,
     *    we can safely try to shrink the window from the left to find a shorter valid window.
     *
     * To support shrinking from the left while still avoiding construction of the full merged array,
     * we store only the current window values in a deque. Each merged element is:
     * - generated exactly once when the right side expands
     * - removed at most once when the left side shrinks
     *
     * Therefore the total work remains linear.
     *
     * @param yesterday the first sorted array representing yesterday's scores
     * @param today the second sorted array representing today's scores
     * @param target the minimum required sum for the window
     * @return the length of the shortest contiguous window in merged sorted order whose sum is at least target;
     *         returns -1 if no such window exists
     * Time complexity: O(n + m), where n = yesterday.length and m = today.length
     * Space complexity: O(k), where k is the current window size, worst-case O(n + m)
     */
    public int shortestSortedWindow(int[] yesterday, int[] today, int target) {
        int i = 0; // Pointer for yesterday array during virtual merge
        int j = 0; // Pointer for today array during virtual merge

        // This deque stores exactly the values currently inside the sliding window.
        // Why do we need it?
        // Because when the window sum becomes large enough, we must remove values from the left.
        // Since we are not building the full merged array, the deque gives us access to the
        // leftmost current value so we can shrink correctly.
        Deque<Integer> window = new ArrayDeque<>();

        long currentSum = 0L; // Use long to avoid overflow when many values are added
        int best = Integer.MAX_VALUE;

        // We keep generating the next element in merged sorted order until both arrays are exhausted.
        while (i < yesterday.length || j < today.length) {
            int nextValue = getNextMergedValue(yesterday, today, i, j);

            // Advance the pointer in whichever array supplied the next merged value.
            // If both arrays still have elements, we choose the smaller one.
            // On ties, we take from yesterday first. This still produces a valid merged order.
            if (i < yesterday.length && j < today.length) {
                if (yesterday[i] <= today[j]) {
                    i++;
                } else {
                    j++;
                }
            } else if (i < yesterday.length) {
                i++;
            } else {
                j++;
            }

            // Expand the sliding window to the right by adding the new merged value.
            window.addLast(nextValue);
            currentSum += nextValue;

            // Because all values are positive:
            // - if currentSum >= target, the current window is valid
            // - removing elements from the left can only decrease the sum
            // So we repeatedly shrink while the window remains valid, updating the answer.
            while (currentSum >= target) {
                best = Math.min(best, window.size());

                // Remove the leftmost value to try to make the valid window even shorter.
                int removed = window.removeFirst();
                currentSum -= removed;
            }
        }

        return best == Integer.MAX_VALUE ? -1 : best;
    }

    /**
     * Returns the next value in the conceptual merge of two sorted arrays, based on the
     * current positions i and j.
     *
     * This method does not move the pointers; it only determines which value should appear next
     * in merged sorted order.
     *
     * @param yesterday the first sorted array
     * @param today the second sorted array
     * @param i current index in yesterday
     * @param j current index in today
     * @return the next value that would appear in the merged sorted array
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int getNextMergedValue(int[] yesterday, int[] today, int i, int j) {
        if (i == yesterday.length) {
            return today[j];
        }
        if (j == today.length) {
            return yesterday[i];
        }
        return yesterday[i] <= today[j] ? yesterday[i] : today[j];
    }

    /**
     * Utility method to print a test case and its computed answer in a beginner-friendly format.
     *
     * @param yesterday the first sorted array
     * @param today the second sorted array
     * @param target the target sum
     * @return the computed shortest valid window length
     * Time complexity: O(n + m)
     * Space complexity: O(k), worst-case O(n + m)
     */
    public int demonstrateCase(int[] yesterday, int[] today, int target) {
        int answer = shortestSortedWindow(yesterday, today, target);
        System.out.println("yesterday = " + Arrays.toString(yesterday));
        System.out.println("today     = " + Arrays.toString(today));
        System.out.println("target    = " + target);
        System.out.println("answer    = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement and a few
     * additional sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demonstration inputs
     * Space complexity: Depends on the demonstration inputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        // Virtual merged array: [1, 2, 3, 4, 7, 8]
        // Shortest window with sum >= 11 has length 2, for example [4, 7] or [3, 8]
        int[] yesterday1 = {1, 4, 7};
        int[] today1 = {2, 3, 8};
        solution.demonstrateCase(yesterday1, today1, 11);

        // Sample 2
        // Virtual merged array: [1, 2, 2, 5, 6, 9]
        // Shortest window with sum >= 15 is [6, 9], length 2
        int[] yesterday2 = {2, 2, 5};
        int[] today2 = {1, 6, 9};
        solution.demonstrateCase(yesterday2, today2, 15);

        // Additional check: single element already reaches target
        int[] yesterday3 = {1, 2, 10};
        int[] today3 = {3, 4, 5};
        solution.demonstrateCase(yesterday3, today3, 10);

        // Additional check: impossible case
        int[] yesterday4 = {1, 1};
        int[] today4 = {1, 1};
        solution.demonstrateCase(yesterday4, today4, 10);
    }
}