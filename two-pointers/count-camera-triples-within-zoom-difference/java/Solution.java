import java.util.*;

/*
 * Title: Count Camera Triples Within Zoom Difference
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an integer array zoomLevels representing the zoom capability of different cameras
 * in a warehouse. You want to form inspection bundles of exactly three distinct cameras.
 * A bundle is considered compatible if the difference between the highest zoom level and the
 * lowest zoom level in that bundle is less than or equal to a given integer maxGap.
 *
 * Return the number of compatible bundles.
 *
 * Two bundles are different if they use a different set of indices, even if the zoom values are
 * the same. The input array is not guaranteed to be sorted.
 *
 * Your task is to design an efficient solution. A brute-force O(n^3) approach will be too slow
 * for the largest inputs. The intended solution uses sorting together with a two-pointer or
 * sliding-window idea to count, for each right endpoint, how many earlier cameras can join it
 * while keeping the zoom range within maxGap.
 *
 * Constraints:
 * - 3 <= zoomLevels.length <= 100000
 * - 0 <= zoomLevels[i] <= 1000000000
 * - 0 <= maxGap <= 1000000000
 * - The answer can be large, so return it as a 64-bit integer value
 *
 * Example 1:
 * Input: zoomLevels = [4, 1, 3, 6, 5], maxGap = 2
 * Output: 3
 * Explanation: After sorting, the values are [1, 3, 4, 5, 6]. The compatible triples are
 * (3,4,5), (3,4,6), and (4,5,6) in terms of values. Each has max - min <= 2.
 *
 * Example 2:
 * Input: zoomLevels = [2, 2, 2, 2], maxGap = 0
 * Output: 4
 * Explanation: Any choice of 3 cameras is valid because all zoom levels are equal.
 * The number of ways to choose 3 out of 4 cameras is 4.
 *
 * Note: You only need to return the count, not the actual triples.
 */

public class Solution {

    /**
     * Counts how many triples of distinct indices form a compatible bundle, meaning:
     * max value in the triple - min value in the triple <= maxGap.
     *
     * Core idea:
     * 1. Sort the array.
     * 2. Use a sliding window with two pointers.
     * 3. For each position r treated as the largest element of the triple:
     *    - Move the left pointer l forward until zoomLevels[r] - zoomLevels[l] <= maxGap.
     *    - Then every index in [l, r - 1] can pair with r while keeping the range valid.
     *    - We need triples of size 3, and r is fixed as one member.
     *    - So we choose any 2 indices from the window size (r - l) previous elements.
     *    - Add C(r - l, 2) to the answer.
     *
     * Why this works:
     * In sorted order, if the smallest and largest values in a chosen triple differ by at most maxGap,
     * then the middle value automatically also lies within that range.
     *
     * @param zoomLevels the array of camera zoom levels; cameras are distinct by index
     * @param maxGap the maximum allowed difference between highest and lowest zoom in a triple
     * @return the number of compatible triples as a 64-bit integer
     *
     * Time complexity: O(n log n) due to sorting, plus O(n) for the sliding window
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public long countCompatibleBundles(int[] zoomLevels, int maxGap) {
        // Defensive check: although constraints guarantee length >= 3,
        // this makes the method safer and beginner-friendly.
        if (zoomLevels == null || zoomLevels.length < 3) {
            return 0L;
        }

        // Sort the zoom levels so that:
        // - values increase from left to right
        // - for any triple, the minimum is the leftmost chosen value
        // - the maximum is the rightmost chosen value
        Arrays.sort(zoomLevels);

        long answer = 0L;

        // Left boundary of the current valid window.
        // For each right boundary r, we maintain the smallest l such that:
        // zoomLevels[r] - zoomLevels[l] <= maxGap
        int l = 0;

        // Iterate over each index as the rightmost / largest value of the triple.
        for (int r = 0; r < zoomLevels.length; r++) {

            // Shrink the window from the left until the range becomes valid.
            // After this loop:
            // zoomLevels[r] - zoomLevels[l] <= maxGap
            while (zoomLevels[r] - zoomLevels[l] > maxGap) {
                l++;
            }

            // Number of earlier elements in the valid window that can be used with r.
            // These are indices l, l+1, ..., r-1
            int available = r - l;

            // To form a triple with r fixed as one element,
            // we must choose any 2 of these 'available' earlier elements.
            //
            // Number of ways = C(available, 2) = available * (available - 1) / 2
            //
            // If available < 2, this naturally contributes 0.
            if (available >= 2) {
                answer += combinationsOfTwo(available);
            }
        }

        return answer;
    }

    /**
     * Computes the number of ways to choose 2 items from n items.
     *
     * Formula:
     * C(n, 2) = n * (n - 1) / 2
     *
     * We return long because the result may be large.
     *
     * @param n the number of available items
     * @return the number of unordered pairs that can be formed
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long combinationsOfTwo(int n) {
        return (long) n * (n - 1) / 2;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 3
     * Example 2 -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding method calls
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] zoomLevels1 = {4, 1, 3, 6, 5};
        int maxGap1 = 2;
        long result1 = solution.countCompatibleBundles(zoomLevels1, maxGap1);
        System.out.println("Example 1 Result: " + result1); // Expected: 3

        int[] zoomLevels2 = {2, 2, 2, 2};
        int maxGap2 = 0;
        long result2 = solution.countCompatibleBundles(zoomLevels2, maxGap2);
        System.out.println("Example 2 Result: " + result2); // Expected: 4

        // Additional quick sanity check:
        // Sorted would be [1, 2, 10], maxGap = 3
        // No triple can work because only one triple exists and 10 - 1 = 9 > 3
        int[] zoomLevels3 = {10, 1, 2};
        int maxGap3 = 3;
        long result3 = solution.countCompatibleBundles(zoomLevels3, maxGap3);
        System.out.println("Additional Test Result: " + result3); // Expected: 0
    }
}