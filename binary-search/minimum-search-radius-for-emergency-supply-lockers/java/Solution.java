import java.util.*;

/*
 * Title: Minimum Search Radius for Emergency Supply Lockers
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A city is planning emergency supply coverage along a very long straight highway.
 * There are n neighborhoods located at integer positions in the array homes, and
 * m supply lockers located at integer positions in the array lockers.
 *
 * A neighborhood is considered covered if there exists at least one locker whose
 * distance from that neighborhood is at most R.
 *
 * Your task is to find the minimum integer radius R such that every neighborhood
 * is covered by at least one locker.
 *
 * The arrays are not guaranteed to be sorted. Positions may be large, and multiple
 * homes or lockers may share the same position. You should design an algorithm
 * efficient enough for large inputs. A brute-force comparison of every home with
 * every locker will be too slow.
 *
 * Return the smallest possible integer R.
 *
 * Constraints:
 * - 1 <= homes.length, lockers.length <= 2 * 10^5
 * - 0 <= homes[i], lockers[i] <= 10^9
 * - The answer fits in a 32-bit signed integer.
 *
 * Example 1:
 * Input: homes = [2, 10, 14], lockers = [4, 12]
 * Output: 2
 * Explanation:
 * With radius 2, home 2 is covered by locker 4, home 10 is covered by locker 12,
 * and home 14 is covered by locker 12. Radius 1 is not enough because home 2
 * would be uncovered.
 *
 * Example 2:
 * Input: homes = [1, 5, 9, 15], lockers = [6]
 * Output: 9
 * Explanation:
 * A single locker at position 6 must cover all homes. The farthest home is at
 * position 15, which is distance 9 away, so the minimum valid radius is 9.
 *
 * Approach:
 * 1. Sort both arrays.
 * 2. Binary search the answer R.
 * 3. For each candidate R, check whether all homes can be covered.
 *
 * Monotonic Property:
 * - If radius R is enough to cover all homes, then any radius larger than R is
 *   also enough.
 * - This makes binary search on the answer valid.
 */

public class Solution {

    /**
     * Computes the minimum integer radius needed so that every home is within
     * distance radius of at least one locker.
     *
     * The algorithm:
     * 1. Sort homes and lockers.
     * 2. Binary search the smallest radius that works.
     * 3. Use a linear two-pointer coverage check for each candidate radius.
     *
     * @param homes the positions of neighborhoods along the highway
     * @param lockers the positions of supply lockers along the highway
     * @return the minimum integer radius required to cover all homes
     *
     * Time complexity: O((n + m) log M + n log n + m log m),
     * where n = homes.length, m = lockers.length, and M is the search range
     * of the answer (at most about 1e9)
     *
     * Space complexity: O(n + m) if counting the cloned arrays used for sorting;
     * otherwise O(1) extra beyond sorting internals
     */
    public int minimumRadius(int[] homes, int[] lockers) {
        // We clone the arrays so the original input is not modified.
        // This is beginner-friendly and avoids surprising side effects.
        int[] sortedHomes = homes.clone();
        int[] sortedLockers = lockers.clone();

        // Sorting is essential because our coverage check relies on scanning
        // from left to right in order.
        Arrays.sort(sortedHomes);
        Arrays.sort(sortedLockers);

        // Binary search boundaries:
        // - The minimum possible radius is 0.
        // - A safe maximum is the farthest possible distance between any home
        //   and any locker among the extremes.
        //
        // Since positions are up to 1e9, using long during intermediate
        // calculations is a good habit to avoid overflow.
        int left = 0;
        int right = computeUpperBound(sortedHomes, sortedLockers);

        // Standard binary search for the first valid radius.
        while (left < right) {
            int mid = left + (right - left) / 2;

            // If mid is enough, try to find an even smaller valid radius.
            if (canCoverAllHomes(sortedHomes, sortedLockers, mid)) {
                right = mid;
            } else {
                // Otherwise, mid is too small, so we must search larger values.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the smallest valid radius.
        return left;
    }

    /**
     * Checks whether every home can be covered by at least one locker using
     * the given radius.
     *
     * This method assumes both arrays are already sorted.
     *
     * Core idea:
     * - Each locker at position x covers the interval [x - radius, x + radius].
     * - Because both arrays are sorted, we can scan homes from left to right
     *   and move through lockers only forward.
     * - For each home, we advance the locker pointer while the current locker
     *   is too far left to cover that home.
     * - Then we test whether the current locker can cover the home.
     *
     * Why this works:
     * - Once a locker is too far left for the current home, it will also be too
     *   far left for every later home, so we never need to revisit it.
     * - This makes the check linear.
     *
     * @param homes sorted array of home positions
     * @param lockers sorted array of locker positions
     * @param radius candidate radius to test
     * @return true if all homes are covered, false otherwise
     *
     * Time complexity: O(n + m)
     * Space complexity: O(1)
     */
    public boolean canCoverAllHomes(int[] homes, int[] lockers, int radius) {
        int lockerIndex = 0;
        int lockerCount = lockers.length;

        // Process homes from left to right.
        for (int home : homes) {
            // Move lockerIndex forward while the current locker's coverage ends
            // before this home starts.
            //
            // Current locker covers:
            // [lockers[lockerIndex] - radius, lockers[lockerIndex] + radius]
            //
            // If lockers[lockerIndex] + radius < home, then this locker cannot
            // cover the current home, and because future homes are even farther
            // right, this locker will never be useful again.
            while (lockerIndex < lockerCount && (long) lockers[lockerIndex] + radius < home) {
                lockerIndex++;
            }

            // If we ran out of lockers, then this home cannot be covered.
            if (lockerIndex == lockerCount) {
                return false;
            }

            // Now lockerIndex points to the first locker whose right coverage
            // endpoint is at least home.
            //
            // We still must verify that the home is not too far left of this
            // locker's coverage interval.
            //
            // If home < lockers[lockerIndex] - radius, then this locker starts
            // covering only after the home, and because this is the first locker
            // not too far left, no earlier locker can help either.
            if ((long) home < (long) lockers[lockerIndex] - radius) {
                return false;
            }

            // Otherwise, this home is covered, and we continue to the next home.
        }

        // If every home passed the test, the radius works.
        return true;
    }

    /**
     * Computes a safe upper bound for the binary search.
     *
     * Since arrays are sorted, the maximum necessary radius must be enough to
     * cover the extreme homes using some extreme locker. A safe bound is the
     * maximum distance between:
     * - leftmost home and leftmost locker
     * - leftmost home and rightmost locker
     * - rightmost home and leftmost locker
     * - rightmost home and rightmost locker
     *
     * In practice, the true answer will never exceed this value.
     *
     * @param homes sorted array of home positions
     * @param lockers sorted array of locker positions
     * @return a safe upper bound for the answer
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int computeUpperBound(int[] homes, int[] lockers) {
        long leftHome = homes[0];
        long rightHome = homes[homes.length - 1];
        long leftLocker = lockers[0];
        long rightLocker = lockers[lockers.length - 1];

        long maxDistance = 0;
        maxDistance = Math.max(maxDistance, Math.abs(leftHome - leftLocker));
        maxDistance = Math.max(maxDistance, Math.abs(leftHome - rightLocker));
        maxDistance = Math.max(maxDistance, Math.abs(rightHome - leftLocker));
        maxDistance = Math.max(maxDistance, Math.abs(rightHome - rightLocker));

        return (int) maxDistance;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem
     * statement and prints the results.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(1) for the demonstration itself, excluding the
     * algorithm calls
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] homes1 = {2, 10, 14};
        int[] lockers1 = {4, 12};
        int result1 = solution.minimumRadius(homes1, lockers1);
        System.out.println("Example 1 Result: " + result1); // Expected: 2

        int[] homes2 = {1, 5, 9, 15};
        int[] lockers2 = {6};
        int result2 = solution.minimumRadius(homes2, lockers2);
        System.out.println("Example 2 Result: " + result2); // Expected: 9

        // Additional quick sanity checks.
        int[] homes3 = {1, 2, 3};
        int[] lockers3 = {2};
        int result3 = solution.minimumRadius(homes3, lockers3);
        System.out.println("Additional Test 1 Result: " + result3); // Expected: 1

        int[] homes4 = {1, 1000000000};
        int[] lockers4 = {0, 999999999};
        int result4 = solution.minimumRadius(homes4, lockers4);
        System.out.println("Additional Test 2 Result: " + result4); // Expected: 1
    }
}