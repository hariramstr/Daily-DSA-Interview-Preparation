import java.util.*;

/*
 * Title: Shortest Restock Span Covering All Essential Products
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A warehouse records the product ID of each item restocked during a day in the order they arrive.
 * You are given an integer array restocks, where restocks[i] is the product ID of the i-th restocked item,
 * and an integer array essentials containing the list of product IDs that must all appear in a report.
 * Your task is to return the length of the shortest contiguous span of restocks that contains every product ID
 * from essentials at least once. If no such span exists, return -1.
 *
 * Duplicate values may appear in restocks, but essentials contains distinct product IDs.
 * The order of products inside the chosen span does not matter.
 * The goal is to find the smallest window in the array that covers the full required set.
 *
 * This problem models a realistic inventory monitoring task where analysts need the smallest time interval
 * that proves all critical products were replenished.
 *
 * Constraints:
 * - 1 <= restocks.length <= 200000
 * - 1 <= essentials.length <= 200000
 * - 1 <= product ID <= 1000000000
 * - essentials contains distinct values
 *
 * Example 1:
 * Input: restocks = [7, 2, 5, 2, 9, 5, 1, 9, 7], essentials = [5, 1, 7]
 * Output: 4
 * Explanation: The shortest valid span is [5, 1, 9, 7], which has length 4.
 *
 * Example 2:
 * Input: restocks = [4, 4, 3, 8, 6, 3, 2], essentials = [3, 2, 5]
 * Output: -1
 * Explanation: Product 5 never appears in restocks, so no contiguous span can cover all essential products.
 *
 * Return only the minimum length, not the subarray itself.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray of restocks that contains
     * every product ID listed in essentials at least once.
     *
     * The method uses a classic sliding window approach:
     * 1. Put all essential product IDs into a set for O(1) membership checks.
     * 2. Expand the right side of the window one element at a time.
     * 3. Track counts of essential products currently inside the window.
     * 4. Once the window contains all required products, try shrinking from the left
     *    to make the window as small as possible while still valid.
     * 5. Keep the minimum valid window length found.
     *
     * @param restocks the array representing the order of restocked product IDs during the day
     * @param essentials the distinct product IDs that must all appear in the chosen span
     * @return the minimum length of a contiguous span containing all essential product IDs,
     *         or -1 if no such span exists
     *
     * Time complexity: O(n + m), where n is restocks.length and m is essentials.length,
     * because each pointer moves at most n times and set/map operations are average O(1).
     * Space complexity: O(m), used by the set of essentials and the frequency map for the window.
     */
    public int shortestRestockSpan(int[] restocks, int[] essentials) {
        // Defensive handling for unexpected null inputs.
        // The problem constraints guarantee valid arrays, but this makes the method safer.
        if (restocks == null || essentials == null || restocks.length == 0 || essentials.length == 0) {
            return -1;
        }

        // Step 1:
        // Store all required product IDs in a HashSet.
        // This allows us to quickly answer:
        // "Is this restocked product one of the essential products we care about?"
        Set<Integer> requiredProducts = new HashSet<>();
        for (int productId : essentials) {
            requiredProducts.add(productId);
        }

        // If there are more unique required products than total restocks,
        // it is impossible to cover them all in any window.
        if (requiredProducts.size() > restocks.length) {
            return -1;
        }

        // Step 2:
        // This map stores how many times each essential product appears
        // inside the current sliding window [left, right].
        Map<Integer, Integer> windowCounts = new HashMap<>();

        // Step 3:
        // 'formed' tells us how many distinct essential products are currently present
        // in the window with count >= 1.
        int formed = 0;

        // Total number of distinct essential products we need to cover.
        int requiredDistinctCount = requiredProducts.size();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        // Start with a very large number so any real valid window will be smaller.
        int minLength = Integer.MAX_VALUE;

        // Step 4:
        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < restocks.length; right++) {
            int currentProduct = restocks[right];

            // We only care about products that are essential.
            if (requiredProducts.contains(currentProduct)) {
                // Increase the count of this essential product in the current window.
                int newCount = windowCounts.getOrDefault(currentProduct, 0) + 1;
                windowCounts.put(currentProduct, newCount);

                // If this product count became 1, it means this essential product
                // has just been newly covered by the current window.
                if (newCount == 1) {
                    formed++;
                }
            }

            // Step 5:
            // If formed == requiredDistinctCount, the current window contains
            // every essential product at least once.
            // Now we try to shrink the window from the left to make it as short as possible.
            while (formed == requiredDistinctCount && left <= right) {
                // Current window length is right - left + 1.
                int currentWindowLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentWindowLength < minLength) {
                    minLength = currentWindowLength;
                }

                // We are about to remove restocks[left] from the window,
                // so inspect that product first.
                int leftProduct = restocks[left];

                // Only essential products affect validity of the window.
                if (requiredProducts.contains(leftProduct)) {
                    int updatedCount = windowCounts.get(leftProduct) - 1;

                    // Store the reduced count back into the map.
                    windowCounts.put(leftProduct, updatedCount);

                    // If the count becomes 0, then after moving left forward,
                    // this essential product is no longer covered by the window.
                    // Therefore the window stops being valid.
                    if (updatedCount == 0) {
                        formed--;
                    }
                }

                // Actually shrink the window by moving the left boundary rightward.
                left++;
            }
        }

        // If minLength was never updated, no valid window was found.
        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * Runs the sample demonstrations from the problem statement and prints the results.
     *
     * This method is useful for quick manual verification and beginner-friendly testing.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed sample demonstrations, excluding the called method cost.
     * Space complexity: O(1), excluding the called method's auxiliary space.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // restocks = [7, 2, 5, 2, 9, 5, 1, 9, 7]
        // essentials = [5, 1, 7]
        //
        // Valid shortest span is [5, 1, 9, 7], length = 4.
        int[] restocks1 = {7, 2, 5, 2, 9, 5, 1, 9, 7};
        int[] essentials1 = {5, 1, 7};
        int result1 = solution.shortestRestockSpan(restocks1, essentials1);
        System.out.println(result1); // Expected: 4

        // Example 2:
        // restocks = [4, 4, 3, 8, 6, 3, 2]
        // essentials = [3, 2, 5]
        //
        // Product 5 never appears, so answer is -1.
        int[] restocks2 = {4, 4, 3, 8, 6, 3, 2};
        int[] essentials2 = {3, 2, 5};
        int result2 = solution.shortestRestockSpan(restocks2, essentials2);
        System.out.println(result2); // Expected: -1
    }
}