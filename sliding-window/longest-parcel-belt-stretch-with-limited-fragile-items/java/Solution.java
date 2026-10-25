import java.util.*;

/*
 * Title: Longest Parcel Belt Stretch With Limited Fragile Items
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A warehouse conveyor belt carries parcels in a fixed order. Each parcel is labeled with a
 * non-negative integer type code. A parcel is considered fragile if its type code appears in
 * a given set fragileTypes. The operations team wants to identify the longest contiguous stretch
 * of the belt that can be processed by a standard robot arm, which may handle at most k fragile
 * parcels before requiring a manual reset.
 *
 * Given an integer array parcels, an integer array fragileTypes containing distinct type codes,
 * and an integer k, return the length of the longest contiguous subarray of parcels that contains
 * at most k parcels whose type belongs to fragileTypes.
 *
 * A contiguous stretch means you must choose a single continuous segment of the array. The fragile
 * type list is not necessarily sorted, and parcel codes may repeat many times. If no parcel can be
 * included under the limit, return 0.
 *
 * Constraints:
 * - 1 <= parcels.length <= 200000
 * - 0 <= parcels[i] <= 10^9
 * - 0 <= fragileTypes.length <= 200000
 * - fragileTypes contains distinct values
 * - 0 <= fragileTypes[i] <= 10^9
 * - 0 <= k <= parcels.length
 *
 * Example 1:
 * Input: parcels = [4, 7, 2, 7, 9, 3, 7, 5], fragileTypes = [7, 9], k = 2
 * Output: 4
 *
 * Example 2:
 * Input: parcels = [1, 8, 6, 8, 2, 4, 6], fragileTypes = [8, 4], k = 1
 * Output: 3
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains at most k fragile parcels.
     *
     * The method uses the classic sliding window technique:
     * - Expand the right boundary one element at a time.
     * - Count how many fragile parcels are currently inside the window.
     * - If the count becomes greater than k, move the left boundary forward until the window
     *   becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * @param parcels the array of parcel type codes in conveyor belt order
     * @param fragileTypes the distinct parcel type codes considered fragile
     * @param k the maximum number of fragile parcels allowed in a valid contiguous stretch
     * @return the maximum length of a contiguous subarray containing at most k fragile parcels
     *
     * Time complexity: O(n + m), where n is parcels.length and m is fragileTypes.length
     * Space complexity: O(m), for storing fragile types in a hash set
     */
    public int longestParcelBeltStretch(int[] parcels, int[] fragileTypes, int k) {
        // Build a fast lookup structure so we can answer:
        // "Is this parcel type fragile?" in average O(1) time.
        Set<Integer> fragileSet = buildFragileSet(fragileTypes);

        // left marks the start of the current sliding window.
        int left = 0;

        // fragileCount stores how many fragile parcels are currently inside the window [left..right].
        int fragileCount = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right from the beginning to the end of the array.
        // At each step, we include parcels[right] into the current window.
        for (int right = 0; right < parcels.length; right++) {
            // If the newly added parcel is fragile, increase the fragile count.
            if (isFragile(parcels[right], fragileSet)) {
                fragileCount++;
            }

            // If the window now contains too many fragile parcels,
            // it is invalid and must be shrunk from the left.
            //
            // We keep moving left forward until fragileCount <= k again.
            while (fragileCount > k) {
                // Before removing parcels[left] from the window,
                // check whether it is fragile.
                if (isFragile(parcels[left], fragileSet)) {
                    fragileCount--;
                }

                // Actually remove the leftmost element by advancing left.
                left++;
            }

            // At this point, the window [left..right] is guaranteed valid:
            // it contains at most k fragile parcels.
            //
            // Compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * Builds a hash set containing all fragile parcel types for fast membership checks.
     *
     * @param fragileTypes the array of distinct fragile type codes
     * @return a set containing all fragile type codes
     *
     * Time complexity: O(m), where m is fragileTypes.length
     * Space complexity: O(m)
     */
    public Set<Integer> buildFragileSet(int[] fragileTypes) {
        Set<Integer> fragileSet = new HashSet<>();

        // Insert every fragile type into the set.
        // Because the input guarantees distinct values, duplicates are not a concern,
        // but HashSet would handle them safely anyway.
        for (int type : fragileTypes) {
            fragileSet.add(type);
        }

        return fragileSet;
    }

    /**
     * Checks whether a parcel type is fragile by looking it up in the set.
     *
     * @param parcelType the parcel type code to check
     * @param fragileSet the set of fragile type codes
     * @return true if the parcel type is fragile; false otherwise
     *
     * Time complexity: O(1) average
     * Space complexity: O(1)
     */
    public boolean isFragile(int parcelType, Set<Integer> fragileSet) {
        return fragileSet.contains(parcelType);
    }

    /**
     * Runs the sample demonstrations from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demo inputs, excluding the called algorithm
     * Space complexity: O(1), excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] parcels1 = {4, 7, 2, 7, 9, 3, 7, 5};
        int[] fragileTypes1 = {7, 9};
        int k1 = 2;

        int result1 = solution.longestParcelBeltStretch(parcels1, fragileTypes1, k1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 4");

        // Example 2
        int[] parcels2 = {1, 8, 6, 8, 2, 4, 6};
        int[] fragileTypes2 = {8, 4};
        int k2 = 1;

        int result2 = solution.longestParcelBeltStretch(parcels2, fragileTypes2, k2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 3");

        // Additional quick checks for edge cases

        // No fragile types at all: entire array is valid.
        int[] parcels3 = {5, 1, 9, 2};
        int[] fragileTypes3 = {};
        int k3 = 0;
        int result3 = solution.longestParcelBeltStretch(parcels3, fragileTypes3, k3);
        System.out.println("Edge Case 1 Result: " + result3);
        System.out.println("Expected: 4");

        // k = 0, only stretches with no fragile parcels are allowed.
        int[] parcels4 = {7, 1, 2, 7, 3, 4};
        int[] fragileTypes4 = {7};
        int k4 = 0;
        int result4 = solution.longestParcelBeltStretch(parcels4, fragileTypes4, k4);
        System.out.println("Edge Case 2 Result: " + result4);
        System.out.println("Expected: 3");
    }
}