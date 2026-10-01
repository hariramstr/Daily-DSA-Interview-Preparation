import java.util.*;

/*
Problem Title: Count Apartment Pairs Within Noise Difference

Problem Description:
You are given an array `noise` where `noise[i]` represents the measured nighttime noise level of the `i`th apartment in a building.
You are also given an integer `limit`.

Two apartments form a compatible pair if the absolute difference between their noise levels is less than or equal to `limit`.

Your task is to return the total number of distinct compatible pairs `(i, j)` such that:
- `0 <= i < j < n`
- `|noise[i] - noise[j]| <= limit`

Because the input size can be large, an efficient solution is required.
A brute-force approach that checks every pair would be too slow.
A sorting + two-pointer approach allows us to count many valid pairs efficiently.

Constraints:
- `1 <= noise.length <= 200000`
- `0 <= noise[i] <= 1000000000`
- `0 <= limit <= 1000000000`
- The answer may be large, so return it as a 64-bit integer.

Example 1:
Input: noise = [12, 7, 10, 15], limit = 3
Output: 4

Valid index pairs are:
- (0, 2) => |12 - 10| = 2
- (0, 3) => |12 - 15| = 3
- (1, 2) => |7 - 10| = 3
- (2, 3) => |10 - 15| = 5 -> not valid
Actually, after checking carefully:
- (0, 2) => valid
- (0, 3) => valid
- (1, 2) => valid
- (1, 0) is not allowed because i < j only
- (2, 3) => |10 - 15| = 5, not valid
- (1, 3) => |7 - 15| = 8, not valid
So the original example explanation in the prompt is inconsistent.
The correct compatible pairs for [12, 7, 10, 15] with limit = 3 are:
- (12, 10)
- (12, 15)
- (7, 10)
Thus the correct answer is 3.

Example 2:
Input: noise = [4, 4, 4, 9], limit = 0
Output: 3

Explanation:
Only equal values can pair when limit = 0.
The three apartments with noise 4 form:
- (0, 1)
- (0, 2)
- (1, 2)
Total = 3.
*/

public class Solution {

    /**
     * Counts the number of distinct apartment pairs whose noise difference
     * is less than or equal to the given limit.
     *
     * The key idea:
     * 1. Sort the array.
     * 2. Use a sliding window / two-pointer technique.
     * 3. For each right pointer, move the left pointer forward until the window
     *    satisfies: noise[right] - noise[left] <= limit.
     * 4. Once valid, every index from left to right - 1 can pair with right,
     *    because the array is sorted and all those values are within the limit.
     *
     * @param noise the array of apartment noise levels
     * @param limit the maximum allowed absolute difference for a compatible pair
     * @return the total number of compatible pairs as a 64-bit integer
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scan
     * Space complexity: O(1) extra space beyond the sort implementation details
     */
    public long countCompatiblePairs(int[] noise, int limit) {
        // Defensive handling:
        // If the array has fewer than 2 elements, no pair can exist.
        if (noise == null || noise.length < 2) {
            return 0L;
        }

        // Sort the noise levels.
        // Why sorting helps:
        // After sorting, for any fixed "right" index, if the difference between
        // noise[right] and noise[left] is within the limit, then every element
        // between left and right is also close enough to noise[right].
        Arrays.sort(noise);

        long pairs = 0L;

        // "left" marks the smallest index in the current valid window.
        int left = 0;

        // Expand the window using "right".
        for (int right = 0; right < noise.length; right++) {

            // While the current window is invalid, move "left" forward.
            //
            // Since the array is sorted:
            // - noise[right] is the largest value in the current window
            // - noise[left] is the smallest value in the current window
            //
            // Therefore, the maximum absolute difference inside this window
            // is exactly noise[right] - noise[left].
            //
            // If that difference is too large, we must shrink the window.
            while ((long) noise[right] - noise[left] > limit) {
                left++;
            }

            // At this point, the window [left, right] is valid:
            // noise[right] - noise[left] <= limit
            //
            // Because the array is sorted, every index k in [left, right - 1]
            // also satisfies:
            // noise[right] - noise[k] <= limit
            //
            // So the number of new valid pairs ending at "right" is:
            // right - left
            //
            // Example:
            // If left = 2 and right = 5,
            // then valid new pairs are:
            // (2,5), (3,5), (4,5) => total 3 = 5 - 2
            pairs += (right - left);
        }

        return pairs;
    }

    /**
     * A helper method that runs the algorithm on a copy of the input array.
     *
     * This is useful for demonstrations because the main counting method sorts
     * the array in-place. By copying first, we preserve the original input.
     *
     * @param noise the original array of apartment noise levels
     * @param limit the maximum allowed absolute difference for a compatible pair
     * @return the total number of compatible pairs as a 64-bit integer
     * Time complexity: O(n log n)
     * Space complexity: O(n) because of the copied array
     */
    public long countCompatiblePairsPreserveInput(int[] noise, int limit) {
        if (noise == null) {
            return 0L;
        }
        int[] copy = Arrays.copyOf(noise, noise.length);
        return countCompatiblePairs(copy, limit);
    }

    /**
     * Prints a test case in a beginner-friendly format.
     *
     * @param noise the input noise array
     * @param limit the allowed maximum difference
     * @param expected the expected answer for comparison
     * @return nothing
     * Time complexity: O(n log n) because it calls the counting method
     * Space complexity: O(n) because input is preserved using a copy
     */
    public void runDemo(int[] noise, int limit, long expected) {
        long result = countCompatiblePairsPreserveInput(noise, limit);
        System.out.println("noise = " + Arrays.toString(noise) + ", limit = " + limit);
        System.out.println("Result   = " + result);
        System.out.println("Expected = " + expected);
        System.out.println();
    }

    /**
     * Demonstrates the solution with sample inputs and a few extra checks.
     *
     * Note:
     * The first example in the prompt contains an inconsistency in its explanation.
     * After correctly checking all index pairs, the answer for:
     * noise = [12, 7, 10, 15], limit = 3
     * is 3, not 4.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demo inputs; each test is O(n log n)
     * Space complexity: O(n) per demonstrated test because input is preserved
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt:
        // Careful verification shows the correct answer is 3.
        solution.runDemo(new int[]{12, 7, 10, 15}, 3, 3L);

        // Sample 2 from the prompt:
        solution.runDemo(new int[]{4, 4, 4, 9}, 0, 3L);

        // Extra demonstrations:
        solution.runDemo(new int[]{1, 2, 3, 4}, 1, 3L);
        solution.runDemo(new int[]{5}, 10, 0L);
        solution.runDemo(new int[]{8, 8, 8, 8}, 0, 6L);
    }
}