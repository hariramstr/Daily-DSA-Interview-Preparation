import java.util.*;

/*
 * Title: Pair Guests for a Tandem Zipline
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * An adventure park runs a tandem zipline where each ride must carry either one guest alone
 * or two guests together. For safety, the combined weight of any paired guests cannot exceed
 * a given limit. You are given an integer array weights where weights[i] is the weight of the
 * i-th guest, and an integer limit representing the maximum total weight allowed on one tandem ride.
 *
 * Return the minimum number of rides needed to send all guests down the zipline.
 *
 * Each ride can take at most two guests. A guest may ride alone, and every guest must be assigned
 * to exactly one ride. Your goal is to minimize the total number of rides.
 *
 * This problem is designed to reward an efficient pairing strategy rather than brute force search.
 * A common approach is to sort the guest weights and use two pointers to try pairing the lightest
 * remaining guest with the heaviest remaining guest whenever possible.
 *
 * Constraints:
 * - 1 <= weights.length <= 100000
 * - 1 <= weights[i] <= 1000000000
 * - 1 <= limit <= 1000000000
 * - It is guaranteed that every individual guest can ride alone, so weights[i] <= limit for all i
 *
 * Example 1:
 * Input: weights = [70, 50, 80, 50], limit = 100
 * Output: 3
 * Explanation: Pair the two guests weighing 50 and 50 together. The guests weighing 70 and 80
 * must ride alone. So the minimum number of rides is 3.
 *
 * Example 2:
 * Input: weights = [40, 60, 55, 45, 80], limit = 100
 * Output: 3
 * Explanation: One optimal assignment is (40, 60), (45, 55), and (80). No solution can use fewer
 * than 3 rides because there are 5 guests and each ride holds at most 2 guests.
 */

public class Solution {

    /**
     * Computes the minimum number of rides needed to carry all guests.
     *
     * Strategy:
     * 1. Sort the weights.
     * 2. Use two pointers:
     *    - left points to the lightest remaining guest
     *    - right points to the heaviest remaining guest
     * 3. Always try to pair the heaviest guest with the lightest guest.
     *    - If they fit together, move both pointers.
     *    - Otherwise, the heaviest guest must ride alone, so move only the right pointer.
     * 4. Every iteration uses exactly one ride.
     *
     * Why this works:
     * Pairing the heaviest remaining guest is the most urgent decision.
     * If the heaviest guest cannot pair with the lightest guest, then that heaviest guest
     * cannot pair with anyone else either, because everyone else is heavier than the lightest.
     * So sending that heaviest guest alone is forced and optimal.
     *
     * @param weights the array of guest weights
     * @param limit the maximum total weight allowed on one ride
     * @return the minimum number of rides required to transport all guests
     * Time complexity: O(n log n) because of sorting; the two-pointer scan is O(n)
     * Space complexity: O(1) extra space beyond the sort's internal usage if sorting in place
     */
    public int minRides(int[] weights, int limit) {
        // Sort the array so we can efficiently match light and heavy guests.
        Arrays.sort(weights);

        // left starts at the lightest guest not yet assigned to a ride.
        int left = 0;

        // right starts at the heaviest guest not yet assigned to a ride.
        int right = weights.length - 1;

        // This will count how many rides we use in total.
        int rides = 0;

        // Continue until all guests have been assigned.
        while (left <= right) {
            // We are definitely going to use one ride for the heaviest remaining guest
            // at index 'right'. The only question is:
            // Can we also place the lightest remaining guest at index 'left' with them?
            //
            // There are two cases:
            //
            // Case 1: left == right
            // Only one guest remains, so they must ride alone.
            //
            // Case 2: left < right
            // Try pairing the lightest and heaviest remaining guests.
            if (left == right) {
                // Exactly one guest remains.
                // They take one ride alone.
                rides++;
                break;
            }

            // Compute the combined weight carefully.
            // We cast to long to be extra safe, although int is sufficient here because
            // max sum is 2 * 1_000_000_000, which still fits in int.
            long combinedWeight = (long) weights[left] + weights[right];

            if (combinedWeight <= limit) {
                // The lightest and heaviest guests can share one ride.
                //
                // This is ideal because:
                // - we successfully place the heaviest guest
                // - we also avoid wasting a separate ride on the lightest guest
                //
                // Move both pointers inward because both guests are now assigned.
                left++;
                right--;
            } else {
                // The heaviest guest cannot pair with the lightest guest.
                //
                // Since the array is sorted, every other unassigned guest is at least as heavy
                // as weights[left]. That means the heaviest guest cannot pair with anyone.
                //
                // Therefore, the heaviest guest must ride alone.
                // We assign only the guest at 'right' and move that pointer inward.
                right--;
            }

            // In both cases above, we used exactly one ride.
            rides++;
        }

        return rides;
    }

    /**
     * Helper method to create a readable string for an integer array.
     *
     * @param array the input integer array
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n log n) across the demonstrated test cases
     * Space complexity: O(1) extra space beyond sorting and output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] weights1 = {70, 50, 80, 50};
        int limit1 = 100;
        int result1 = solution.minRides(weights1.clone(), limit1);
        System.out.println("Example 1");
        System.out.println("weights = " + solution.arrayToString(weights1));
        System.out.println("limit = " + limit1);
        System.out.println("Minimum rides = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Example 2
        int[] weights2 = {40, 60, 55, 45, 80};
        int limit2 = 100;
        int result2 = solution.minRides(weights2.clone(), limit2);
        System.out.println("Example 2");
        System.out.println("weights = " + solution.arrayToString(weights2));
        System.out.println("limit = " + limit2);
        System.out.println("Minimum rides = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional beginner-friendly checks
        int[] weights3 = {100};
        int limit3 = 100;
        int result3 = solution.minRides(weights3.clone(), limit3);
        System.out.println("Additional Test 1");
        System.out.println("weights = " + solution.arrayToString(weights3));
        System.out.println("limit = " + limit3);
        System.out.println("Minimum rides = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        int[] weights4 = {30, 30, 30, 30};
        int limit4 = 60;
        int result4 = solution.minRides(weights4.clone(), limit4);
        System.out.println("Additional Test 2");
        System.out.println("weights = " + solution.arrayToString(weights4));
        System.out.println("limit = " + limit4);
        System.out.println("Minimum rides = " + result4);
        System.out.println("Expected = 2");
        System.out.println();

        int[] weights5 = {20, 50, 50, 80};
        int limit5 = 100;
        int result5 = solution.minRides(weights5.clone(), limit5);
        System.out.println("Additional Test 3");
        System.out.println("weights = " + solution.arrayToString(weights5));
        System.out.println("limit = " + limit5);
        System.out.println("Minimum rides = " + result5);
        System.out.println("Expected = 3");
    }
}