import java.util.*;

/*
 * Title: Minimum Heater Power for Cold Storage Crates
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A warehouse stores temperature-sensitive crates along a straight hallway.
 * The position of each crate and each portable heater is given as an integer
 * coordinate on the same number line. Every heater uses the same power setting,
 * and a heater with power r can keep all crates within distance r from its position safe.
 *
 * You are given two integer arrays, crates and heaters, where crates[i] is the
 * position of the i-th crate and heaters[j] is the position of the j-th heater.
 * Your task is to find the minimum integer power r such that every crate is covered
 * by at least one heater.
 *
 * A crate at position x is covered if there exists some heater at position h with
 * |x - h| <= r.
 *
 * The answer must be the smallest possible integer r. The input arrays are not
 * guaranteed to be sorted. A typical efficient solution sorts the heater positions
 * and, for each crate, uses binary search to find the nearest heater on the left
 * and right.
 *
 * Constraints:
 * - 1 <= crates.length, heaters.length <= 2 * 10^5
 * - -10^9 <= crates[i], heaters[j] <= 10^9
 * - Positions may contain duplicates
 * - The expected solution should run in O((n + m) log m) or better after sorting
 *
 * Example 1:
 * Input: crates = [2, 8, 14], heaters = [4, 10]
 * Output: 4
 * Explanation:
 * - Crate 2 is 2 units from heater 4
 * - Crate 8 is 2 units from heater 10 or 4 units from heater 4
 * - Crate 14 is 4 units from heater 10
 * So power 4 is enough, and no smaller power covers crate 14.
 *
 * Example 2:
 * Input: crates = [1, 5, 9, 15], heaters = [6]
 * Output: 9
 * Explanation:
 * With only one heater at position 6, the farthest crate is at position 15,
 * which is 9 units away. Therefore the minimum required power is 9.
 */

public class Solution {

    /**
     * Computes the minimum heater power required so that every crate is covered
     * by at least one heater.
     *
     * The main idea:
     * 1. Sort heater positions so binary search can be used.
     * 2. For each crate, find the insertion position of that crate in the sorted
     *    heater array.
     * 3. The nearest heater must be either:
     *    - the heater immediately to the left of the insertion position, or
     *    - the heater at the insertion position itself (the first heater >= crate)
     * 4. The minimum distance from the crate to a heater is the smaller of those
     *    two distances.
     * 5. Since every crate must be covered, the answer is the maximum among all
     *    these minimum distances.
     *
     * @param crates the positions of the crates along the hallway
     * @param heaters the positions of the heaters along the hallway
     * @return the smallest integer power needed so every crate is within range of some heater
     * Time complexity: O(m log m + n log m), where n = crates.length and m = heaters.length
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public int findMinimumHeaterPower(int[] crates, int[] heaters) {
        // Sort the heater positions first.
        // This is essential because binary search only works on sorted data.
        Arrays.sort(heaters);

        // This variable will store the final answer.
        // For each crate, we compute the distance to its nearest heater.
        // The required power must be large enough for the "worst" crate,
        // so we keep track of the maximum such distance.
        long requiredPower = 0;

        // Process each crate independently.
        for (int crate : crates) {
            // Find the first heater position that is >= crate.
            // This is often called the "lower bound" index.
            int rightIndex = lowerBound(heaters, crate);

            // We now consider up to two candidate heaters:
            // 1. The heater at rightIndex (first heater >= crate), if it exists
            // 2. The heater just before it, at rightIndex - 1 (largest heater < crate), if it exists
            //
            // The nearest heater must be one of these two.
            long distanceToRight = Long.MAX_VALUE;
            long distanceToLeft = Long.MAX_VALUE;

            // If rightIndex is inside the array, then there is a heater on the right
            // (or exactly at the crate position).
            if (rightIndex < heaters.length) {
                distanceToRight = Math.abs((long) heaters[rightIndex] - crate);
            }

            // If rightIndex - 1 is valid, then there is a heater on the left.
            if (rightIndex - 1 >= 0) {
                distanceToLeft = Math.abs((long) crate - heaters[rightIndex - 1]);
            }

            // The nearest heater for this crate is whichever side gives the smaller distance.
            long nearestHeaterDistance = Math.min(distanceToLeft, distanceToRight);

            // Update the global answer:
            // the heater power must be at least this large for this crate,
            // and we need enough power for every crate.
            requiredPower = Math.max(requiredPower, nearestHeaterDistance);
        }

        // The problem guarantees the answer is an integer.
        // Distances are computed using long for safety, but the final result fits in int
        // because coordinates are within [-1e9, 1e9], so max distance is at most 2e9.
        return (int) requiredPower;
    }

    /**
     * Finds the first index i in the sorted array such that array[i] >= target.
     * If no such index exists, returns array.length.
     *
     * This is the classic "lower bound" binary search.
     *
     * Example:
     * array = [4, 10], target = 8
     * - first value >= 8 is 10 at index 1, so return 1
     *
     * Example:
     * array = [4, 10], target = 14
     * - no value >= 14 exists, so return 2
     *
     * @param array a sorted integer array
     * @param target the value to search for
     * @return the first index where array[index] >= target, or array.length if none exists
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] array, int target) {
        int left = 0;
        int right = array.length; // exclusive upper bound

        // We search in the half-open interval [left, right).
        // Invariant:
        // - all indices < left are known to be < target
        // - all indices >= right are known to be >= target
        while (left < right) {
            // Safe midpoint calculation to avoid overflow.
            int mid = left + (right - left) / 2;

            // If array[mid] is less than target, then mid cannot be the answer,
            // and neither can anything to its left.
            if (array[mid] < target) {
                left = mid + 1;
            } else {
                // Otherwise, mid might be the answer, so keep it in the range
                // and discard only the strictly right part.
                right = mid;
            }
        }

        // At loop end, left == right, and this is the first index with array[index] >= target.
        return left;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints the expected values so the output can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(m log m + n log m) per demonstration call
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] crates1 = {2, 8, 14};
        int[] heaters1 = {4, 10};
        int result1 = solution.findMinimumHeaterPower(crates1, heaters1);
        System.out.println("Example 1:");
        System.out.println("Crates: " + Arrays.toString(crates1));
        System.out.println("Heaters: " + Arrays.toString(heaters1));
        System.out.println("Minimum heater power = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Example 2
        int[] crates2 = {1, 5, 9, 15};
        int[] heaters2 = {6};
        int result2 = solution.findMinimumHeaterPower(crates2, heaters2);
        System.out.println("Example 2:");
        System.out.println("Crates: " + Arrays.toString(crates2));
        System.out.println("Heaters: " + Arrays.toString(heaters2));
        System.out.println("Minimum heater power = " + result2);
        System.out.println("Expected = 9");
        System.out.println();

        // Additional beginner-friendly demonstration
        int[] crates3 = {-10, -2, 0, 7, 20};
        int[] heaters3 = {-5, 5, 15};
        int result3 = solution.findMinimumHeaterPower(crates3, heaters3);
        System.out.println("Additional Example:");
        System.out.println("Crates: " + Arrays.toString(crates3));
        System.out.println("Heaters: " + Arrays.toString(heaters3));
        System.out.println("Minimum heater power = " + result3);
    }
}