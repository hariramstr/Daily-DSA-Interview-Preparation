import java.util.*;

/*
 * Title: Longest Toll Segment With Exact Pass Usage
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array roads where roads[i] is the toll cost of the i-th road segment
 * on a highway trip. You also have a special pass that can be used to make any segment with toll
 * cost less than or equal to passLimit free. However, the pass must be used on exactly k segments
 * within one contiguous part of the trip.
 *
 * Your task is to find the length of the longest contiguous subarray such that within that subarray,
 * there are exactly k elements whose value is less than or equal to passLimit.
 *
 * Return the maximum possible length of such a subarray. If no valid subarray exists, return 0.
 *
 * This problem models finding the longest stretch of a route where your pass is used an exact
 * number of times. Note that the pass can only be applied to segments that qualify, and every
 * qualifying segment inside the chosen subarray counts toward the total. Segments with toll cost
 * greater than passLimit do not use the pass.
 *
 * Constraints:
 * - 1 <= roads.length <= 100000
 * - 0 <= roads[i] <= 1000000000
 * - 0 <= passLimit <= 1000000000
 * - 0 <= k <= roads.length
 *
 * Example 1:
 * Input: roads = [8, 2, 5, 1, 9, 3, 7], passLimit = 3, k = 2
 * Output: 5
 * Explanation:
 * The longest valid subarray is [8, 2, 5, 1, 9], which contains exactly two qualifying segments:
 * 2 and 1. So the answer is 5.
 *
 * Example 2:
 * Input: roads = [4, 6, 8, 10], passLimit = 5, k = 1
 * Output: 4
 * Explanation:
 * The whole subarray [4, 6, 8, 10] contains exactly one value less than or equal to 5, so the
 * answer is 4.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray containing exactly k elements
     * whose value is less than or equal to passLimit.
     *
     * Core idea:
     * 1. Convert the problem into counting "qualifying" elements:
     *    - A road qualifies if roads[i] <= passLimit.
     * 2. We need the longest subarray with exactly k qualifying elements.
     * 3. This can be solved in linear time using the standard identity:
     *      countExactly(k) window length = longest window with at most k qualifying elements,
     *      but here we need the maximum length with exactly k, not the number of windows.
     *
     * A direct and very clean linear approach is:
     * - Track the indices of all qualifying elements.
     * - If we want exactly k qualifying elements in a subarray, then:
     *   * choose any consecutive block of k qualifying indices
     *   * expand left until just after the previous qualifying index
     *   * expand right until just before the next qualifying index
     * - That gives the longest subarray for that chosen group of k qualifying elements.
     *
     * Special case:
     * - If k == 0, then we want the longest subarray containing no qualifying elements at all.
     *   That is simply the longest consecutive run of values > passLimit.
     *
     * @param roads the array of toll costs for road segments
     * @param passLimit the maximum toll cost that qualifies for pass usage
     * @param k the exact number of qualifying segments required inside the chosen subarray
     * @return the maximum length of a contiguous subarray containing exactly k values <= passLimit;
     *         returns 0 if no such subarray exists
     *
     * Time complexity: O(n), where n is roads.length
     * Space complexity: O(n) in the worst case for storing qualifying indices
     */
    public int longestTollSegmentWithExactPassUsage(int[] roads, int passLimit, int k) {
        int n = roads.length;

        // Special handling when we need exactly 0 qualifying elements.
        // In that case, every element in the chosen subarray must be > passLimit.
        if (k == 0) {
            return longestRunOfNonQualifying(roads, passLimit);
        }

        // Store the indices of all elements that qualify for pass usage,
        // meaning roads[i] <= passLimit.
        List<Integer> qualifyingIndices = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (roads[i] <= passLimit) {
                qualifyingIndices.add(i);
            }
        }

        // If there are fewer than k qualifying elements in the entire array,
        // it is impossible to form any subarray with exactly k qualifying elements.
        if (qualifyingIndices.size() < k) {
            return 0;
        }

        int maxLength = 0;
        int totalQualifying = qualifyingIndices.size();

        // We examine every consecutive group of exactly k qualifying elements.
        //
        // Suppose the chosen group starts at qualifyingIndices[i]
        // and ends at qualifyingIndices[i + k - 1].
        //
        // To keep the subarray containing exactly these k qualifying elements:
        // - The left boundary can go as far left as:
        //     0, if there is no previous qualifying element
        //     otherwise previousQualifyingIndex + 1
        //
        // - The right boundary can go as far right as:
        //     n - 1, if there is no next qualifying element
        //     otherwise nextQualifyingIndex - 1
        //
        // This gives the longest possible subarray for that block of k qualifying elements.
        for (int i = 0; i + k - 1 < totalQualifying; i++) {
            int firstIndexInBlock = qualifyingIndices.get(i);
            int lastIndexInBlock = qualifyingIndices.get(i + k - 1);

            int leftBoundary;
            if (i == 0) {
                // No qualifying element before this block,
                // so we may start from the very beginning.
                leftBoundary = 0;
            } else {
                // Must start after the previous qualifying element,
                // otherwise we would include more than k qualifying elements.
                leftBoundary = qualifyingIndices.get(i - 1) + 1;
            }

            int rightBoundary;
            if (i + k - 1 == totalQualifying - 1) {
                // No qualifying element after this block,
                // so we may extend to the very end.
                rightBoundary = n - 1;
            } else {
                // Must end before the next qualifying element,
                // otherwise we would include more than k qualifying elements.
                rightBoundary = qualifyingIndices.get(i + k) - 1;
            }

            int currentLength = rightBoundary - leftBoundary + 1;
            maxLength = Math.max(maxLength, currentLength);
        }

        return maxLength;
    }

    /**
     * Computes the longest contiguous run of elements that do NOT qualify,
     * meaning values strictly greater than passLimit.
     *
     * This helper is used for the special case k == 0.
     *
     * @param roads the array of toll costs
     * @param passLimit the maximum toll cost that qualifies for pass usage
     * @return the maximum length of a contiguous subarray containing zero values <= passLimit
     *
     * Time complexity: O(n), where n is roads.length
     * Space complexity: O(1)
     */
    public int longestRunOfNonQualifying(int[] roads, int passLimit) {
        int maxLength = 0;
        int currentLength = 0;

        for (int value : roads) {
            if (value > passLimit) {
                // This value does not qualify, so it can be part of a valid k == 0 segment.
                currentLength++;
                maxLength = Math.max(maxLength, currentLength);
            } else {
                // This value qualifies, so any k == 0 segment must stop before it.
                currentLength = 0;
            }
        }

        return maxLength;
    }

    /**
     * A convenience wrapper matching the problem statement terminology.
     *
     * @param roads the array of toll costs for road segments
     * @param passLimit the maximum toll cost that qualifies for pass usage
     * @param k the exact number of qualifying segments required
     * @return the maximum valid subarray length
     *
     * Time complexity: O(n), where n is roads.length
     * Space complexity: O(n)
     */
    public int solve(int[] roads, int passLimit, int k) {
        return longestTollSegmentWithExactPassUsage(roads, passLimit, k);
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(n) per demonstration call in the worst case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] roads1 = {8, 2, 5, 1, 9, 3, 7};
        int passLimit1 = 3;
        int k1 = 2;
        int result1 = solution.solve(roads1, passLimit1, k1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 5");

        int[] roads2 = {4, 6, 8, 10};
        int passLimit2 = 5;
        int k2 = 1;
        int result2 = solution.solve(roads2, passLimit2, k2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 4");

        int[] roads3 = {7, 8, 9};
        int passLimit3 = 5;
        int k3 = 0;
        int result3 = solution.solve(roads3, passLimit3, k3);
        System.out.println("Edge Case k=0 Result: " + result3);
        System.out.println("Expected: 3");

        int[] roads4 = {1, 2, 3};
        int passLimit4 = 3;
        int k4 = 4;
        int result4 = solution.solve(roads4, passLimit4, k4);
        System.out.println("Impossible Case Result: " + result4);
        System.out.println("Expected: 0");
    }
}