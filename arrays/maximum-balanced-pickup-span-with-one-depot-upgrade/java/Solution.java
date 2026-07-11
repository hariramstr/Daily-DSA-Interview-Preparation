import java.util.*;

/*
 * Title: Maximum Balanced Pickup Span with One Depot Upgrade
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * A logistics company records the pickup capacity of depots along a highway in an integer array capacities,
 * where capacities[i] is the number of packages depot i can process in one hour.
 *
 * A contiguous group of depots is called balanced if the difference between the maximum and minimum capacity
 * inside that group is at most limit.
 *
 * The company is allowed to perform at most one upgrade operation on a single depot inside the chosen group.
 * In one upgrade, you may increase that depot's capacity by any value from 0 up to upgrade.
 * The upgraded value is used only for checking whether the chosen group is balanced.
 * You may also choose not to use the upgrade.
 *
 * Return the maximum possible length of a contiguous balanced group after applying at most one such upgrade.
 *
 * Important notes:
 * - You may upgrade only one element in the chosen subarray.
 * - Capacities can only be increased, never decreased.
 * - The final subarray must satisfy max(subarray) - min(subarray) <= limit after the optional upgrade.
 * - Because only increases are allowed, the upgraded depot may help by raising a too-small value,
 *   but it cannot reduce a too-large value.
 *
 * Constraints:
 * - 1 <= capacities.length <= 2 * 10^5
 * - 0 <= capacities[i] <= 10^9
 * - 0 <= limit <= 10^9
 * - 0 <= upgrade <= 10^9
 *
 * Key observation:
 * Since we can only increase one element, we can only help the subarray by fixing a too-small minimum value.
 * Therefore, a window is valid if and only if one of the following holds:
 *
 * 1) It is already balanced:
 *      max - min <= limit
 *
 * 2) It can be made balanced by upgrading exactly one occurrence of the minimum:
 *      - There must be exactly one element equal to the minimum in the window.
 *        If there are two or more minima, upgrading only one still leaves another unchanged minimum.
 *      - Let secondMin be the second smallest value in the window
 *        (or effectively +infinity if the window size is 1).
 *      - After raising the unique minimum to some value x, the new minimum becomes secondMin
 *        (or x if window size is 1), and the maximum stays max unless x exceeds it.
 *      - To keep spread <= limit, it is enough and necessary that:
 *            max - secondMin <= limit
 *        and we can raise min enough:
 *            secondMin - min <= upgrade
 *
 * So for each sliding window we need:
 * - maximum value
 * - minimum value
 * - count of the minimum value
 * - second minimum value
 *
 * We maintain these with:
 * - a decreasing deque for maximums
 * - an increasing deque for minimums
 * - a TreeMap of value frequencies to query the second minimum when needed
 *
 * This gives an O(n log n) solution, which is efficient for n up to 2 * 10^5.
 */
public class Solution {

    /**
     * Computes the maximum possible length of a contiguous balanced subarray after applying
     * at most one upgrade to one element inside the chosen subarray.
     *
     * @param capacities the array of depot capacities
     * @param limit the allowed maximum difference between max and min in the final subarray
     * @param upgrade the maximum amount by which one chosen element may be increased
     * @return the maximum valid subarray length
     *
     * Time complexity: O(n log n), because each element enters/leaves the sliding window once,
     * and TreeMap operations are O(log n).
     * Space complexity: O(n), for the deques and TreeMap in the worst case.
     */
    public int maximumBalancedPickupSpan(int[] capacities, int limit, int upgrade) {
        int n = capacities.length;

        // Deque storing indices in decreasing order of values.
        // The front always holds the index of the current window maximum.
        Deque<Integer> maxDeque = new ArrayDeque<>();

        // Deque storing indices in increasing order of values.
        // The front always holds the index of the current window minimum.
        Deque<Integer> minDeque = new ArrayDeque<>();

        // Frequency map of values currently inside the window.
        // We use it to know:
        // - the minimum value (firstKey)
        // - how many times the minimum appears
        // - the second minimum value (higherKey(min))
        TreeMap<Integer, Integer> freq = new TreeMap<>();

        int left = 0;
        int answer = 0;

        // Expand the window one element at a time using 'right'.
        for (int right = 0; right < n; right++) {
            int value = capacities[right];

            // Insert current value into maxDeque:
            // Remove all smaller-or-equal values from the back because they can never become
            // the maximum while this new value remains in the window.
            while (!maxDeque.isEmpty() && capacities[maxDeque.peekLast()] <= value) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            // Insert current value into minDeque:
            // Remove all larger-or-equal values from the back because they can never become
            // the minimum while this new value remains in the window.
            while (!minDeque.isEmpty() && capacities[minDeque.peekLast()] >= value) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            // Add to frequency map.
            freq.put(value, freq.getOrDefault(value, 0) + 1);

            // Shrink from the left until the current window becomes valid.
            while (!isWindowValid(capacities, limit, upgrade, left, right, maxDeque, minDeque, freq)) {
                int removeValue = capacities[left];

                // If the outgoing index is currently at the front of maxDeque, remove it.
                if (!maxDeque.isEmpty() && maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }

                // If the outgoing index is currently at the front of minDeque, remove it.
                if (!minDeque.isEmpty() && minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }

                // Remove one occurrence from frequency map.
                int count = freq.get(removeValue);
                if (count == 1) {
                    freq.remove(removeValue);
                } else {
                    freq.put(removeValue, count - 1);
                }

                left++;
            }

            // Now [left, right] is valid, so update the best answer.
            answer = Math.max(answer, right - left + 1);
        }

        return answer;
    }

    /**
     * Checks whether the current sliding window [left, right] is valid.
     *
     * A window is valid if:
     * 1) It is already balanced: max - min <= limit
     * OR
     * 2) It can be made balanced by upgrading exactly one unique minimum value enough so that
     *    the new minimum becomes the second minimum, and the spread then fits within limit.
     *
     * @param capacities the original capacities array
     * @param limit the allowed final spread
     * @param upgrade the maximum allowed increase for one element
     * @param left the left boundary of the current window
     * @param right the right boundary of the current window
     * @param maxDeque deque maintaining window maximum
     * @param minDeque deque maintaining window minimum
     * @param freq frequency map of values in the current window
     * @return true if the window is valid, otherwise false
     *
     * Time complexity: O(log n), due to TreeMap higherKey lookup.
     * Space complexity: O(1) extra beyond the provided data structures.
     */
    public boolean isWindowValid(
            int[] capacities,
            int limit,
            int upgrade,
            int left,
            int right,
            Deque<Integer> maxDeque,
            Deque<Integer> minDeque,
            TreeMap<Integer, Integer> freq
    ) {
        int maxValue = capacities[maxDeque.peekFirst()];
        int minValue = capacities[minDeque.peekFirst()];

        // Case 1: already balanced without any upgrade.
        if ((long) maxValue - minValue <= limit) {
            return true;
        }

        // If not already balanced, the only possible fix is to raise the minimum.
        // But if the minimum appears multiple times, upgrading only one copy does not remove
        // the old minimum from the window, so it cannot help.
        int minCount = freq.get(minValue);
        if (minCount != 1) {
            return false;
        }

        // Find the second minimum value in the window.
        // Since minCount == 1 and window size >= 2 in any interesting failing case,
        // higherKey(minValue) should exist. Still, we handle null safely.
        Integer secondMinObj = freq.higherKey(minValue);

        // If there is no second minimum, the window has size 1 and would already be valid,
        // so this path is practically unreachable. We keep it for completeness.
        if (secondMinObj == null) {
            return true;
        }

        int secondMin = secondMinObj;

        // To make the unique minimum stop being the minimum, we must raise it to at least secondMin.
        // That requires secondMin - minValue upgrade amount.
        if ((long) secondMin - minValue > upgrade) {
            return false;
        }

        // After doing that, the new minimum becomes secondMin.
        // The maximum cannot be reduced, so the final spread is maxValue - secondMin.
        return (long) maxValue - secondMin <= limit;
    }

    /**
     * Convenience wrapper using long parameters for callers that may prefer wider numeric types.
     * Internally the constraints fit in int for array values, but the logic uses long where needed
     * to avoid overflow in subtraction.
     *
     * @param capacities the array of depot capacities
     * @param limit the allowed maximum difference between max and min in the final subarray
     * @param upgrade the maximum amount by which one chosen element may be increased
     * @return the maximum valid subarray length
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int maximumBalancedPickupSpan(int[] capacities, long limit, long upgrade) {
        return maximumBalancedPickupSpan(capacities, (int) limit, (int) upgrade);
    }

    /**
     * Demonstrates the solution on sample-style inputs and a few extra checks.
     *
     * @param args command-line arguments, not used
     * @return nothing
     *
     * Time complexity: O(total input size log n) across the demo calls.
     * Space complexity: O(n) per call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] capacities1 = {5, 3, 6, 4, 7};
        int limit1 = 2;
        int upgrade1 = 2;
        int result1 = solution.maximumBalancedPickupSpan(capacities1, limit1, upgrade1);
        System.out.println(result1); // Expected: 4

        int[] capacities2 = {8, 2, 2, 2, 8};
        int limit2 = 3;
        int upgrade2 = 6;
        int result2 = solution.maximumBalancedPickupSpan(capacities2, limit2, upgrade2);
        System.out.println(result2); // Correct expected: 3

        int[] capacities3 = {1};
        int limit3 = 0;
        int upgrade3 = 100;
        int result3 = solution.maximumBalancedPickupSpan(capacities3, limit3, upgrade3);
        System.out.println(result3); // Expected: 1

        int[] capacities4 = {4, 1, 5};
        int limit4 = 1;
        int upgrade4 = 3;
        int result4 = solution.maximumBalancedPickupSpan(capacities4, limit4, upgrade4);
        System.out.println(result4); // Expected: 2

        int[] capacities5 = {2, 5, 3, 4};
        int limit5 = 2;
        int upgrade5 = 2;
        int result5 = solution.maximumBalancedPickupSpan(capacities5, limit5, upgrade5);
        System.out.println(result5); // One possible expected: 4
    }
}