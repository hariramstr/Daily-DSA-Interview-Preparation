import java.util.*;

/*
 * Title: Minimum Launch Power for Satellite Relay Windows
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A space operations team needs to transmit data to a sequence of orbital relay windows.
 * The i-th relay window opens at time windows[i], and sending to that window requires
 * power[i] units of launch energy. A single transmitter can only handle windows in
 * chronological order. If the transmitter is configured with a launch power limit X,
 * then it may send to any relay window whose required power is at most X. However,
 * skipped windows are lost forever, and the team must still successfully transmit to
 * at least k relay windows in order.
 *
 * Your task is to find the minimum integer launch power limit X such that it is possible
 * to transmit to at least k relay windows.
 *
 * The arrays windows and power are both length n. The windows array is strictly increasing,
 * but the actual times only determine the order of processing; you may not reorder windows.
 * Since a higher power limit always allows transmitting to every window that was possible
 * under a lower limit, the answer is monotonic and can be found efficiently.
 *
 * Return the minimum integer X that allows at least k successful transmissions.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= k <= n
 * - 1 <= windows[i] <= 10^9
 * - windows is strictly increasing
 * - 1 <= power[i] <= 10^9
 *
 * Example 1:
 * Input: windows = [2, 5, 9, 12], power = [7, 3, 6, 4], k = 3
 * Output: 6
 * Explanation:
 * With X = 5, the team can transmit only to windows with required power 3 and 4,
 * so only 2 transmissions are possible.
 * With X = 6, the team can transmit to windows requiring 3, 6, and 4, reaching 3
 * transmissions. Therefore the minimum valid power limit is 6.
 *
 * Example 2:
 * Input: windows = [1, 4, 8, 10, 15], power = [9, 2, 5, 8, 1], k = 4
 * Output: 8
 * Explanation:
 * With X = 7, only windows with required power 2, 5, and 1 can be used, for a total
 * of 3 transmissions.
 * With X = 8, windows requiring 2, 5, 8, and 1 become available, so 4 transmissions
 * are possible. Thus the answer is 8.
 */

public class Solution {

    /**
     * Finds the minimum integer launch power limit X such that at least k relay windows
     * can be successfully transmitted to.
     *
     * Important observation:
     * Because windows must be processed in chronological order and skipping is allowed,
     * for a fixed power limit X we can simply count how many indices i satisfy power[i] <= X.
     * If that count is at least k, then X is feasible.
     *
     * This feasibility condition is monotonic:
     * - If X works, then any larger value also works.
     * - If X does not work, then any smaller value also does not work.
     *
     * That monotonicity makes binary search the correct and efficient approach.
     *
     * @param windows the strictly increasing relay window times; only their order matters here
     * @param power the required launch power for each corresponding relay window
     * @param k the minimum number of successful transmissions required
     * @return the minimum integer launch power limit that allows at least k transmissions
     * Time complexity: O(n log M), where n is the number of windows and M is the search range of power values
     * Space complexity: O(1) extra space
     */
    public int minimumLaunchPower(int[] windows, int[] power, int k) {
        validateInput(windows, power, k);

        // We binary search on the answer X.
        // The smallest possible X is the minimum required power in the array.
        // The largest possible X is the maximum required power in the array.
        //
        // Why is this enough?
        // - If X is smaller than every power requirement, maybe zero windows are possible.
        // - If X is at least the maximum power requirement, then every window is possible.
        // Since k <= n, the answer must lie within [minPower, maxPower].
        int minPower = Integer.MAX_VALUE;
        int maxPower = Integer.MIN_VALUE;

        for (int value : power) {
            minPower = Math.min(minPower, value);
            maxPower = Math.max(maxPower, value);
        }

        int left = minPower;
        int right = maxPower;
        int answer = maxPower;

        // Standard binary search for the first feasible value.
        while (left <= right) {
            // Use this form to avoid overflow:
            int mid = left + (right - left) / 2;

            // Check whether this power limit allows at least k transmissions.
            if (canTransmitAtLeastK(power, k, mid)) {
                // mid works, so it is a candidate answer.
                answer = mid;

                // But we still want the minimum working value,
                // so continue searching on the left half.
                right = mid - 1;
            } else {
                // mid does not work, so we need a larger power limit.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given launch power limit allows at least k successful transmissions.
     *
     * Step-by-step logic:
     * 1. Traverse the relay windows in their original order.
     * 2. For each window, if its required power is <= limit, then that window can be used.
     * 3. Count how many such windows exist.
     * 4. If the count reaches k, return true immediately.
     *
     * Because the transmitter only needs to process windows in chronological order and skipping
     * is allowed, every usable window can simply be taken. There is no conflict between choices.
     *
     * @param power the required launch power for each relay window
     * @param k the target number of successful transmissions
     * @param limit the candidate launch power limit being tested
     * @return true if at least k windows can be transmitted to with this limit, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canTransmitAtLeastK(int[] power, int k, int limit) {
        int count = 0;

        // Scan all windows in order.
        for (int required : power) {
            // If this window's required power is within the allowed limit,
            // then this transmission is possible.
            if (required <= limit) {
                count++;

                // Early exit:
                // As soon as we know at least k transmissions are possible,
                // there is no need to continue scanning.
                if (count >= k) {
                    return true;
                }
            }
        }

        // If we finish the scan without reaching k, then this limit is insufficient.
        return false;
    }

    /**
     * Validates the input according to the problem statement.
     *
     * This method is helpful for beginner-friendly robustness in a runnable program.
     * It ensures:
     * - arrays are not null
     * - arrays have equal non-zero length
     * - k is within valid bounds
     * - windows is strictly increasing
     *
     * @param windows the relay window times
     * @param power the required launch powers
     * @param k the required number of transmissions
     * @return nothing; throws IllegalArgumentException if input is invalid
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public void validateInput(int[] windows, int[] power, int k) {
        if (windows == null || power == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }

        if (windows.length != power.length) {
            throw new IllegalArgumentException("windows and power must have the same length.");
        }

        if (windows.length == 0) {
            throw new IllegalArgumentException("Input arrays must not be empty.");
        }

        if (k < 1 || k > windows.length) {
            throw new IllegalArgumentException("k must be between 1 and n inclusive.");
        }

        for (int i = 1; i < windows.length; i++) {
            if (windows[i] <= windows[i - 1]) {
                throw new IllegalArgumentException("windows must be strictly increasing.");
            }
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 6
     * - Example 2: 8
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log M) per demonstration call
     * Space complexity: O(1) extra space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] windows1 = {2, 5, 9, 12};
        int[] power1 = {7, 3, 6, 4};
        int k1 = 3;
        int result1 = solution.minimumLaunchPower(windows1, power1, k1);
        System.out.println("Example 1 Output: " + result1);

        int[] windows2 = {1, 4, 8, 10, 15};
        int[] power2 = {9, 2, 5, 8, 1};
        int k2 = 4;
        int result2 = solution.minimumLaunchPower(windows2, power2, k2);
        System.out.println("Example 2 Output: " + result2);

        // Additional quick sanity check:
        // If k = 1, the answer should be the minimum power value,
        // because we only need one successful transmission.
        int[] windows3 = {3, 6, 11};
        int[] power3 = {10, 4, 7};
        int k3 = 1;
        int result3 = solution.minimumLaunchPower(windows3, power3, k3);
        System.out.println("Additional Example Output: " + result3);
    }
}