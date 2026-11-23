import java.util.*;

/*
 * Title: Maximum Reliable Prefix After One Sensor Removal
 *
 * Problem Description:
 * You are given an array readings of length n, where readings[i] is the signal quality
 * reported by the i-th sensor in a fixed processing order. A prefix of the array is considered
 * reliable if the greatest common divisor (GCD) of every pair of adjacent values in that prefix
 * is greater than 1.
 *
 * You may permanently remove at most one element from the array before evaluating reliability.
 * After removing zero or one element, the remaining elements keep their original order.
 *
 * Return the maximum possible length of a reliable prefix of the resulting array.
 *
 * A prefix always starts at index 0 of the resulting array.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 1 <= readings[i] <= 10^9
 * - Expected solution: O(n) or O(n log V), where V is the maximum reading value
 *
 * Key observation:
 * A reliable prefix of length L means that all adjacent pairs among its first L elements
 * have gcd > 1. If we remove one element at index k, then:
 * - all pairs before k stay the same,
 * - all pairs after k shift left but stay the same relative to each other,
 * - one new "bridge" pair may appear between readings[k - 1] and readings[k + 1].
 *
 * We must maximize the prefix length after removing at most one element.
 */

public class Solution {

    /**
     * Computes the maximum possible length of a reliable prefix after removing at most one element.
     *
     * The algorithm works in linear time:
     * 1. Build an array good[] where good[i] tells whether gcd(readings[i], readings[i + 1]) > 1.
     * 2. Build prefix/suffix helper arrays over good[] so we can quickly test whether a whole range
     *    of adjacent pairs is valid.
     * 3. Try:
     *    - removing nothing,
     *    - removing each possible index k,
     *    and compute the longest reliable prefix of the resulting array.
     *
     * Important detail:
     * For a fixed deletion index k, the reliable prefix in the resulting array can extend:
     * - through all original valid pairs before k,
     * - then possibly across the new bridge (k - 1, k + 1),
     * - then through valid original pairs after k.
     *
     * @param readings the sensor readings in original order
     * @return the maximum reliable prefix length after removing at most one reading
     * Time complexity: O(n log V), where V is the maximum reading value
     * Space complexity: O(n)
     */
    public int maximumReliablePrefix(int[] readings) {
        int n = readings.length;

        // If there is only one element, the whole array is always a reliable prefix.
        if (n == 1) {
            return 1;
        }

        // good[i] corresponds to the adjacency between readings[i] and readings[i + 1].
        // good[i] == true means gcd(readings[i], readings[i + 1]) > 1.
        boolean[] good = new boolean[n - 1];
        for (int i = 0; i < n - 1; i++) {
            good[i] = gcd(readings[i], readings[i + 1]) > 1;
        }

        // prefixGood[i] = true if all good[0..i] are true.
        boolean[] prefixGood = new boolean[n - 1];
        prefixGood[0] = good[0];
        for (int i = 1; i < n - 1; i++) {
            prefixGood[i] = prefixGood[i - 1] && good[i];
        }

        // suffixGood[i] = true if all good[i..n-2] are true.
        boolean[] suffixGood = new boolean[n - 1];
        suffixGood[n - 2] = good[n - 2];
        for (int i = n - 3; i >= 0; i--) {
            suffixGood[i] = good[i] && suffixGood[i + 1];
        }

        // nextBad[i] = smallest index j >= i such that good[j] == false.
        // If no such j exists, nextBad[i] = n - 1 (a sentinel one past the last good index).
        //
        // This is extremely useful:
        // If we are currently at adjacency index start in the resulting array and all those
        // adjacencies correspond to original good[start..something], then the first failure
        // is exactly nextBad[start]. Therefore the number of consecutive valid adjacencies
        // from start is nextBad[start] - start.
        int[] nextBad = new int[n - 1];
        int sentinel = n - 1;
        int nearestBad = sentinel;
        for (int i = n - 2; i >= 0; i--) {
            if (!good[i]) {
                nearestBad = i;
            }
            nextBad[i] = nearestBad;
        }

        // Case 1: remove nothing.
        // The longest reliable prefix is determined by the first bad adjacency.
        int answer = reliablePrefixWithoutRemoval(good);

        // Case 2: remove one element at every possible index.
        // We carefully compute the resulting reliable prefix length.
        for (int k = 0; k < n; k++) {
            int candidate = reliablePrefixAfterRemovingIndex(readings, good, nextBad, k);
            answer = Math.max(answer, candidate);
        }

        return answer;
    }

    /**
     * Computes the reliable prefix length when no element is removed.
     *
     * If the first bad adjacency is at index i, then the reliable prefix contains elements
     * 0 through i, so its length is i + 1.
     * If all adjacencies are good, the whole array is reliable.
     *
     * @param good adjacency validity array where good[i] means gcd(a[i], a[i+1]) > 1
     * @return reliable prefix length without deletion
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int reliablePrefixWithoutRemoval(boolean[] good) {
        for (int i = 0; i < good.length; i++) {
            if (!good[i]) {
                return i + 1;
            }
        }
        return good.length + 1;
    }

    /**
     * Computes the reliable prefix length after removing the element at index k.
     *
     * Detailed reasoning:
     *
     * Let the original array be:
     *   a[0], a[1], ..., a[k-1], a[k], a[k+1], ..., a[n-1]
     *
     * After removing a[k], the new array is:
     *   a[0], a[1], ..., a[k-1], a[k+1], ..., a[n-1]
     *
     * The reliable prefix starts at the beginning, so we only care how far we can go from the front.
     *
     * There are three structural parts:
     * 1. Pairs strictly before k:
     *      (a[0],a[1]), (a[1],a[2]), ..., (a[k-2],a[k-1])
     *    These are exactly original good[0..k-2].
     *
     * 2. The bridge pair created by deletion (only if k has both neighbors):
     *      (a[k-1], a[k+1])
     *
     * 3. Pairs after k:
     *      (a[k+1],a[k+2]), (a[k+2],a[k+3]), ...
     *    These correspond to original good[k+1], good[k+2], ...
     *
     * We must stop at the first failing pair among these parts.
     *
     * @param readings original readings array
     * @param good adjacency validity array
     * @param nextBad helper array giving first bad adjacency at or after each position
     * @param k index to remove
     * @return reliable prefix length after removing readings[k]
     * Time complexity: O(log V) due to one gcd call
     * Space complexity: O(1)
     */
    public int reliablePrefixAfterRemovingIndex(int[] readings, boolean[] good, int[] nextBad, int k) {
        int n = readings.length;

        // After removing one element from an array of length n, the new length is n - 1.
        // If n == 1 this method is never called because the main method returns early.
        int newLength = n - 1;

        // Removing the first element:
        // The resulting array starts at original index 1.
        // Its adjacencies are original good[1], good[2], ..., good[n-2].
        if (k == 0) {
            if (n == 2) {
                return 1;
            }
            int firstBad = nextBad[1];
            if (firstBad == n - 1) {
                return newLength;
            }
            // If original good[firstBad] is the first failure among the shifted array,
            // then the reliable prefix length in the new array is:
            // number of valid adjacencies before it + 1
            // = (firstBad - 1) + 1
            // = firstBad
            return firstBad;
        }

        // Removing the last element:
        // The resulting array is simply original indices [0..n-2].
        // Its adjacencies are original good[0..n-3].
        if (k == n - 1) {
            for (int i = 0; i <= n - 3; i++) {
                if (!good[i]) {
                    return i + 1;
                }
            }
            return newLength;
        }

        // General case: 0 < k < n - 1.
        //
        // Step 1:
        // Check whether all pairs before k are valid.
        // These are good[0..k-2].
        //
        // If some pair there fails, then deletion at k cannot help because the prefix starts at 0
        // and must pass through those pairs unchanged.
        for (int i = 0; i <= k - 2; i++) {
            if (!good[i]) {
                return i + 1;
            }
        }

        // Step 2:
        // All pairs before k are fine, so we have already included original elements 0..k-1
        // in the reliable prefix of the resulting array.
        //
        // Now we need to check the new bridge pair between readings[k - 1] and readings[k + 1].
        if (gcd(readings[k - 1], readings[k + 1]) == 1) {
            // The bridge fails immediately.
            // Then the reliable prefix ends at the element corresponding to original index k - 1.
            // That means length = k.
            return k;
        }

        // Step 3:
        // The bridge is valid, so we can include original index k + 1 as well.
        // Current reliable prefix length is at least k + 1 in the resulting array.
        //
        // Now continue through pairs after the bridge.
        // Those are original good[k+1], good[k+2], ..., good[n-2].
        if (k + 1 > n - 2) {
            // There are no further pairs after the bridge.
            return newLength;
        }

        int firstBad = nextBad[k + 1];
        if (firstBad == n - 1) {
            // No bad pair after the bridge, so the whole resulting array is reliable.
            return newLength;
        }

        // Count how many valid original adjacencies we can take after index k+1 before the first bad one.
        int validAfterBridge = firstBad - (k + 1);

        // Prefix length calculation:
        // - Before deletion and bridge, we have included k elements: original 0..k-1
        // - The valid bridge lets us include one more element: original k+1
        //   => now length = k + 1
        // - Then each additional valid adjacency after the bridge adds one more element
        //   => total = (k + 1) + validAfterBridge
        return (k + 1) + validAfterBridge;
    }

    /**
     * Computes the greatest common divisor using the iterative Euclidean algorithm.
     *
     * @param a first number
     * @param b second number
     * @return gcd(a, b)
     * Time complexity: O(log(min(a, b)))
     * Space complexity: O(1)
     */
    public int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    /**
     * Convenience wrapper matching the problem statement naming style.
     *
     * @param readings the sensor readings
     * @return maximum reliable prefix length after removing at most one element
     * Time complexity: O(n log V)
     * Space complexity: O(n)
     */
    public int solve(int[] readings) {
        return maximumReliablePrefix(readings);
    }

    /**
     * Demonstrates the solution on the corrected examples and a few extra edge cases.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size * log V) for the demonstrated examples
     * Space complexity: O(total input size)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] readings1 = {6, 10, 15, 7, 14};
        int[] readings2 = {8, 12, 18, 25, 35, 49};
        int[] readings3 = {5};
        int[] readings4 = {2, 4, 8, 16};
        int[] readings5 = {7, 9};
        int[] readings6 = {6, 35, 10};

        System.out.println(solution.solve(readings1)); // Expected: 3
        System.out.println(solution.solve(readings2)); // Expected: 3
        System.out.println(solution.solve(readings3)); // Expected: 1
        System.out.println(solution.solve(readings4)); // Expected: 4
        System.out.println(solution.solve(readings5)); // Expected: 1
        System.out.println(solution.solve(readings6)); // Expected: 2
    }
}