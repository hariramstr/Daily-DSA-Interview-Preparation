import java.util.*;

/*
 * Shortest Alert Window With Severity Debt
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array alerts of length n, where each element is a pair [serviceId, severity].
 * The monitoring team wants to isolate the shortest contiguous time window that is "actionable".
 *
 * A window is actionable if it satisfies both conditions:
 * 1. It contains alerts from at least m distinct services.
 * 2. Let peak be the maximum severity inside the window. For every distinct service that appears
 *    in the window, consider only that service's highest severity within the same window.
 *    The total severity debt of the window is the sum of
 *    (peak - highestSeverityOfThatService) over all distinct services in the window.
 *    The window is valid only if this total debt is at most budget.
 *
 * Return the length of the shortest actionable window. If no such window exists, return -1.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 1 <= serviceId <= 2 * 10^5
 * - 1 <= severity <= 10^9
 * - 1 <= m <= n
 * - 0 <= budget <= 10^14
 *
 * Example 1:
 * Input: alerts = [[1,4],[2,2],[1,6],[3,5]], m = 3, budget = 5
 * Output: 4
 * Explanation:
 * The full window has distinct services {1,2,3}. Its peak severity is 6.
 * Per-service highest severities are 6, 2, and 5, so debt = (6-6) + (6-2) + (6-5) = 5.
 * No shorter window contains 3 distinct services, so the answer is 4.
 *
 * Example 2:
 * Input: alerts = [[1,8],[2,7],[2,3],[3,8],[1,5]], m = 3, budget = 1
 * Output: 4
 * Explanation:
 * Window [[2,7],[2,3],[3,8],[1,5]] contains services {1,2,3}. Peak = 8.
 * Per-service highest severities are 5, 7, and 8, so debt = 3. This window is invalid.
 * The shortest valid window is [[1,8],[2,7],[2,3],[3,8]], where peak = 8 and
 * per-service highest severities are 8, 7, and 8, giving debt = 1.
 *
 * --------------------------------------------------------------------
 * Key observation used in this solution:
 *
 * For a fixed window:
 *   debt = sum(peak - maxSeverityForService)
 *        = distinctCount * peak - sumOfPerServiceMaximums
 *
 * So if we can dynamically maintain:
 *   1) number of distinct services in the window
 *   2) current peak severity in the window
 *   3) sum of each service's maximum severity inside the window
 *
 * then we can test validity quickly.
 *
 * The challenge is that when the window moves, a service's maximum severity may change,
 * and the global peak may also change. To support this efficiently:
 *
 * - For each service, we maintain a multiset of severities currently inside the window.
 *   We only need to know that service's current maximum.
 * - Globally, we maintain another multiset over the current per-service maxima.
 *   The largest key in this structure is the window peak.
 * - We also maintain the sum of all per-service maxima.
 *
 * Every add/remove operation updates:
 *   - the service-local multiset
 *   - the global multiset of service maxima
 *   - the running sum of service maxima
 *
 * Then window validity is:
 *   distinctCount >= m AND distinctCount * peak - sumMax <= budget
 *
 * This supports a standard two-pointer sliding window.
 */
public class Solution {

    /**
     * A simple multiset implemented with TreeMap.
     * It supports:
     * - add(value)
     * - remove(value)
     * - get maximum key
     * - check if empty
     *
     * We use counts because duplicate severities can appear.
     */
    private static class MultiSet {
        private final TreeMap<Integer, Integer> map = new TreeMap<>();

        /**
         * Adds one occurrence of value.
         *
         * @param value the value to insert
         * @return nothing
         * Time complexity: O(log k), where k is the number of distinct keys in this multiset.
         * Space complexity: O(1) extra per call.
         */
        public void add(int value) {
            map.merge(value, 1, Integer::sum);
        }

        /**
         * Removes one occurrence of value.
         * Assumes the value exists.
         *
         * @param value the value to remove
         * @return nothing
         * Time complexity: O(log k), where k is the number of distinct keys in this multiset.
         * Space complexity: O(1) extra per call.
         */
        public void remove(int value) {
            int count = map.get(value);
            if (count == 1) {
                map.remove(value);
            } else {
                map.put(value, count - 1);
            }
        }

        /**
         * Returns the maximum value currently stored.
         *
         * @return the largest key
         * Time complexity: O(log k)
         * Space complexity: O(1)
         */
        public int max() {
            return map.lastKey();
        }

        /**
         * Checks whether this multiset is empty.
         *
         * @return true if empty, false otherwise
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        public boolean isEmpty() {
            return map.isEmpty();
        }
    }

    /**
     * Maintains all information about the current sliding window.
     *
     * For each service:
     * - a multiset of severities currently inside the window
     * - from that, we know the service's current maximum severity
     *
     * Globally:
     * - a multiset of all current per-service maxima
     * - sum of all current per-service maxima
     * - number of distinct services
     *
     * This allows us to compute:
     *   debt = distinctCount * peak - sumOfServiceMaxima
     */
    private static class WindowState {
        private final Map<Integer, MultiSet> serviceToSeverities = new HashMap<>();
        private final MultiSet maximaOfServices = new MultiSet();
        private long sumOfServiceMaxima = 0L;
        private int distinctCount = 0;

        /**
         * Adds one alert [serviceId, severity] into the current window.
         *
         * @param serviceId the service id of the alert
         * @param severity the severity of the alert
         * @return nothing
         * Time complexity: O(log n) amortized
         * Space complexity: O(1) extra per call, excluding stored window data
         */
        public void addAlert(int serviceId, int severity) {
            MultiSet severities = serviceToSeverities.get(serviceId);

            // Case 1:
            // This service is not currently present in the window.
            // After adding this alert:
            // - distinct service count increases by 1
            // - this service's maximum becomes exactly 'severity'
            if (severities == null) {
                severities = new MultiSet();
                severities.add(severity);
                serviceToSeverities.put(serviceId, severities);

                distinctCount++;
                maximaOfServices.add(severity);
                sumOfServiceMaxima += severity;
                return;
            }

            // Case 2:
            // This service already exists in the window.
            // We need to see whether its maximum changes after adding this severity.
            int oldMax = severities.max();
            severities.add(severity);
            int newMax = severities.max();

            // If the service maximum changed, we must update the global structures.
            if (newMax != oldMax) {
                maximaOfServices.remove(oldMax);
                maximaOfServices.add(newMax);
                sumOfServiceMaxima += (long) newMax - oldMax;
            }
        }

        /**
         * Removes one alert [serviceId, severity] from the current window.
         * Assumes that exact alert is currently inside the window.
         *
         * @param serviceId the service id of the alert
         * @param severity the severity of the alert
         * @return nothing
         * Time complexity: O(log n) amortized
         * Space complexity: O(1) extra per call
         */
        public void removeAlert(int serviceId, int severity) {
            MultiSet severities = serviceToSeverities.get(serviceId);
            int oldMax = severities.max();

            // Remove this severity from the service-local multiset.
            severities.remove(severity);

            // If the service disappears completely from the window:
            // - distinct count decreases
            // - its old maximum must be removed from global maxima
            if (severities.isEmpty()) {
                serviceToSeverities.remove(serviceId);
                distinctCount--;
                maximaOfServices.remove(oldMax);
                sumOfServiceMaxima -= oldMax;
                return;
            }

            // Otherwise the service still exists, but its maximum may have changed.
            int newMax = severities.max();
            if (newMax != oldMax) {
                maximaOfServices.remove(oldMax);
                maximaOfServices.add(newMax);
                sumOfServiceMaxima += (long) newMax - oldMax;
            }
        }

        /**
         * Returns the number of distinct services currently in the window.
         *
         * @return number of distinct services
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        public int getDistinctCount() {
            return distinctCount;
        }

        /**
         * Returns the current peak severity in the window.
         * If the window is empty, returns 0.
         *
         * @return the maximum severity among all alerts in the current window
         * Time complexity: O(log n) due to TreeMap.lastKey()
         * Space complexity: O(1)
         */
        public int getPeak() {
            if (distinctCount == 0) {
                return 0;
            }
            return maximaOfServices.max();
        }

        /**
         * Computes the current severity debt:
         *   distinctCount * peak - sumOfServiceMaxima
         *
         * @return the current debt
         * Time complexity: O(log n), because getPeak() uses TreeMap.lastKey()
         * Space complexity: O(1)
         */
        public long getDebt() {
            if (distinctCount == 0) {
                return 0L;
            }
            long peak = getPeak();
            return peak * distinctCount - sumOfServiceMaxima;
        }

        /**
         * Checks whether the current window is actionable.
         *
         * @param m minimum required number of distinct services
         * @param budget maximum allowed severity debt
         * @return true if the current window is valid, false otherwise
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        public boolean isValid(int m, long budget) {
            if (distinctCount < m) {
                return false;
            }
            return getDebt() <= budget;
        }
    }

    /**
     * Returns the length of the shortest actionable contiguous window.
     *
     * Detailed idea:
     * 1. Use two pointers: left and right.
     * 2. Expand right one step at a time, adding alerts[right] into the window.
     * 3. After each expansion, while the current window is valid:
     *    - update the answer with current length
     *    - shrink from the left to try to find an even shorter valid window
     * 4. If no valid window is ever found, return -1.
     *
     * Why this works:
     * - We examine every possible right boundary exactly once.
     * - For each right boundary, we move left forward as much as possible while preserving validity.
     * - Therefore, every candidate minimal valid window is considered.
     *
     * @param alerts the array of alerts, where each alert is [serviceId, severity]
     * @param m the minimum number of distinct services required in the window
     * @param budget the maximum allowed severity debt
     * @return the length of the shortest actionable window, or -1 if none exists
     * Time complexity: O(n log n) amortized
     * Space complexity: O(n)
     */
    public int shortestAlertWindow(int[][] alerts, int m, long budget) {
        int n = alerts.length;

        WindowState window = new WindowState();

        int left = 0;
        int answer = Integer.MAX_VALUE;

        // Move the right pointer from left to right across the array.
        for (int right = 0; right < n; right++) {
            int serviceId = alerts[right][0];
            int severity = alerts[right][1];

            // Step 1: include alerts[right] in the current window.
            window.addAlert(serviceId, severity);

            // Step 2:
            // As long as the current window is valid, it is a candidate answer.
            // Then try shrinking from the left to make it shorter.
            while (left <= right && window.isValid(m, budget)) {
                answer = Math.min(answer, right - left + 1);

                int leftService = alerts[left][0];
                int leftSeverity = alerts[left][1];
                window.removeAlert(leftService, leftSeverity);
                left++;
            }
        }

        return answer == Integer.MAX_VALUE ? -1 : answer;
    }

    /**
     * A helper method to print an alerts array in a readable format.
     *
     * @param alerts the alerts array
     * @return a string representation of the alerts
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String alertsToString(int[][] alerts) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < alerts.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("[").append(alerts[i][0]).append(",").append(alerts[i][1]).append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size * log n) for the demonstrated examples
     * Space complexity: O(total input size)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] alerts1 = {
            {1, 4},
            {2, 2},
            {1, 6},
            {3, 5}
        };
        int m1 = 3;
        long budget1 = 5L;
        int result1 = solution.shortestAlertWindow(alerts1, m1, budget1);

        System.out.println("Example 1:");
        System.out.println("alerts = " + solution.alertsToString(alerts1));
        System.out.println("m = " + m1 + ", budget = " + budget1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        int[][] alerts2 = {
            {1, 8},
            {2, 7},
            {2, 3},
            {3, 8},
            {1, 5}
        };
        int m2 = 3;
        long budget2 = 1L;
        int result2 = solution.shortestAlertWindow(alerts2, m2, budget2);

        System.out.println("Example 2:");
        System.out.println("alerts = " + solution.alertsToString(alerts2));
        System.out.println("m = " + m2 + ", budget = " + budget2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        int[][] alerts3 = {
            {1, 10},
            {1, 1},
            {2, 9},
            {3, 8}
        };
        int m3 = 3;
        long budget3 = 3L;
        int result3 = solution.shortestAlertWindow(alerts3, m3, budget3);

        System.out.println("Additional Example:");
        System.out.println("alerts = " + solution.alertsToString(alerts3));
        System.out.println("m = " + m3 + ", budget = " + budget3);
        System.out.println("Output = " + result3);
    }
}