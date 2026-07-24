import java.util.*;

/*
 * Shortest API Trace Covering Endpoint Quotas
 *
 * Problem Description:
 * You are given an API trace represented by an array trace, where trace[i] is the endpoint
 * name called at time i. You are also given a list of required endpoint quotas as pairs
 * (endpoint, count), meaning a valid incident window must contain that endpoint at least
 * count times.
 *
 * Return the length of the shortest contiguous subarray of trace that satisfies all required
 * quotas. If no such subarray exists, return -1.
 *
 * Unlike a simple coverage problem, the trace can be very large, endpoint names are arbitrary
 * strings, and the quota list may contain repeated endpoint requirements that should be combined.
 * Your solution should be efficient enough for production-scale logs.
 *
 * Formally, if need[x] is the required number of occurrences of endpoint x, then a window [l, r]
 * is valid if for every required endpoint x, the number of indices i in [l, r] with trace[i] == x
 * is at least need[x].
 *
 * Constraints:
 * - 1 <= trace.length <= 2 * 10^5
 * - 1 <= quotas.length <= 2 * 10^5
 * - trace[i] and endpoint names in quotas are non-empty strings of lowercase English letters,
 *   digits, '_', or '/'
 * - The sum of all endpoint name lengths across input is at most 10^6
 * - Quotas may contain duplicate endpoint names; they should be added together
 *
 * Example 1:
 * trace = ["/login","/feed","/cart","/login","/feed","/pay"]
 * quotas = [["/login",2],["/feed",1],["/pay",1]]
 * Output: 6
 *
 * Example 2:
 * trace = ["a","x","b","a","c","b","a"]
 * quotas = [["a",2],["b",1]]
 * Output: 4
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray of the API trace that satisfies
     * all required endpoint quotas.
     *
     * The algorithm uses:
     * 1. A hash map to combine duplicate quota requirements into a single required count per endpoint.
     * 2. A sliding window with two pointers to maintain the current candidate subarray.
     * 3. Another hash map to count how many times each required endpoint appears in the current window.
     * 4. A counter that tracks how many distinct required endpoints currently meet their quota.
     *
     * Key idea:
     * - Expand the right pointer until the window becomes valid.
     * - Once valid, shrink from the left as much as possible while keeping it valid.
     * - Record the minimum valid window length seen.
     *
     * @param trace the full API trace, where each element is an endpoint name
     * @param quotas a 2D array where each row is [endpoint, countAsString]
     * @return the length of the shortest valid contiguous subarray, or -1 if impossible
     * Time complexity: O(n + q), where n = trace.length and q = quotas.length, assuming average O(1) hash operations
     * Space complexity: O(k), where k = number of distinct required endpoints
     */
    public int shortestTraceCover(String[] trace, String[][] quotas) {
        if (trace == null || quotas == null || trace.length == 0 || quotas.length == 0) {
            return -1;
        }

        // Step 1:
        // Build the "need" map.
        // If quotas contain duplicate endpoint names, we must add their counts together.
        //
        // Example:
        // quotas = [["/login","1"],["/login","2"],["/pay","1"]]
        // need becomes:
        // "/login" -> 3
        // "/pay"   -> 1
        Map<String, Integer> need = buildNeedMap(quotas);

        // This is the number of distinct endpoint names whose quota must be satisfied.
        int requiredKinds = need.size();

        // Step 2:
        // Before running the sliding window, we can do a quick feasibility check:
        // count total occurrences in the entire trace for only the required endpoints.
        // If even the full trace does not contain enough of some endpoint, answer is immediately -1.
        if (!canSatisfy(trace, need)) {
            return -1;
        }

        // Step 3:
        // Sliding window state.
        //
        // windowCount stores counts of required endpoints currently inside [left, right].
        // We only track endpoints that matter (those present in "need").
        Map<String, Integer> windowCount = new HashMap<>();

        // formedKinds tells us how many distinct required endpoints currently meet their quota.
        //
        // For example, if:
        // need = {a:2, b:1, c:3}
        // and current window has:
        // a:2, b:1, c:1
        // then formedKinds = 2 because a and b are satisfied, c is not.
        int formedKinds = 0;

        int left = 0;
        int minLen = Integer.MAX_VALUE;

        // Step 4:
        // Move the right pointer from left to right across the trace.
        // Each step expands the window by including trace[right].
        for (int right = 0; right < trace.length; right++) {
            String endpoint = trace[right];

            // Only update counts if this endpoint is actually required.
            if (need.containsKey(endpoint)) {
                int newCount = windowCount.getOrDefault(endpoint, 0) + 1;
                windowCount.put(endpoint, newCount);

                // If this increment causes the endpoint to reach exactly its required quota,
                // then one more required kind is now satisfied.
                //
                // We use "==" here, not ">=":
                // - When count goes from need-1 to need, it becomes newly satisfied.
                // - If it goes above need, formedKinds should not increase again.
                if (newCount == need.get(endpoint)) {
                    formedKinds++;
                }
            }

            // Step 5:
            // If all required endpoint kinds are satisfied, the current window is valid.
            // Now try to shrink it from the left to make it as short as possible.
            while (formedKinds == requiredKinds) {
                // Current window is [left, right], inclusive.
                int currentLen = right - left + 1;
                if (currentLen < minLen) {
                    minLen = currentLen;
                }

                String leftEndpoint = trace[left];

                // We are about to remove trace[left] from the window.
                // If it is a required endpoint, update the window count.
                if (need.containsKey(leftEndpoint)) {
                    int currentCount = windowCount.get(leftEndpoint);

                    // If the count is exactly equal to the needed amount before removal,
                    // then removing one will make this endpoint no longer satisfied.
                    if (currentCount == need.get(leftEndpoint)) {
                        formedKinds--;
                    }

                    // Decrease the count in the window.
                    windowCount.put(leftEndpoint, currentCount - 1);
                }

                // Move left boundary rightward to try a smaller window.
                left++;
            }
        }

        return minLen == Integer.MAX_VALUE ? -1 : minLen;
    }

    /**
     * Builds the combined quota map from the raw quota pairs.
     *
     * Duplicate endpoint names are merged by summing their required counts.
     *
     * @param quotas a 2D array where each row is [endpoint, countAsString]
     * @return a map from endpoint name to total required count
     * Time complexity: O(q), where q = quotas.length
     * Space complexity: O(k), where k = number of distinct endpoints in quotas
     */
    public Map<String, Integer> buildNeedMap(String[][] quotas) {
        Map<String, Integer> need = new HashMap<>();

        for (String[] quota : quotas) {
            String endpoint = quota[0];
            int count = Integer.parseInt(quota[1]);

            // Combine duplicate requirements by summing them.
            need.put(endpoint, need.getOrDefault(endpoint, 0) + count);
        }

        return need;
    }

    /**
     * Checks whether the full trace contains enough occurrences of every required endpoint.
     * This allows an early return of -1 when the task is impossible.
     *
     * @param trace the full API trace
     * @param need the combined required counts per endpoint
     * @return true if the full trace can satisfy all quotas, otherwise false
     * Time complexity: O(n), where n = trace.length
     * Space complexity: O(k), where k = number of distinct required endpoints
     */
    public boolean canSatisfy(String[] trace, Map<String, Integer> need) {
        Map<String, Integer> total = new HashMap<>();

        // Count only endpoints that matter.
        for (String endpoint : trace) {
            if (need.containsKey(endpoint)) {
                total.put(endpoint, total.getOrDefault(endpoint, 0) + 1);
            }
        }

        // Verify every required endpoint appears enough times overall.
        for (Map.Entry<String, Integer> entry : need.entrySet()) {
            String endpoint = entry.getKey();
            int required = entry.getValue();
            int available = total.getOrDefault(endpoint, 0);

            if (available < required) {
                return false;
            }
        }

        return true;
    }

    /**
     * Convenience overload that accepts quotas as endpoint/count integer pairs represented by
     * a list of entries. This is useful for callers who want a more programmatic API.
     *
     * @param trace the full API trace
     * @param quotaList list of quota entries where each entry contains an endpoint and required count
     * @return the length of the shortest valid contiguous subarray, or -1 if impossible
     * Time complexity: O(n + q), where n = trace.length and q = quotaList.size()
     * Space complexity: O(k), where k = number of distinct required endpoints
     */
    public int shortestTraceCover(String[] trace, List<Quota> quotaList) {
        String[][] quotas = new String[quotaList.size()][2];
        for (int i = 0; i < quotaList.size(); i++) {
            quotas[i][0] = quotaList.get(i).endpoint;
            quotas[i][1] = String.valueOf(quotaList.get(i).count);
        }
        return shortestTraceCover(trace, quotas);
    }

    /**
     * Simple data holder for a quota requirement.
     */
    public static class Quota {
        String endpoint;
        int count;

        /**
         * Creates a quota entry.
         *
         * @param endpoint endpoint name
         * @param count required number of occurrences
         * @return no return value
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        public Quota(String endpoint, int count) {
            this.endpoint = endpoint;
            this.count = count;
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a duplicate-quota example.
     *
     * Expected outputs:
     * Example 1 -> 6
     * Example 2 -> 4
     * Duplicate quota example -> 5
     *
     * @param args command-line arguments (unused)
     * @return no return value
     * Time complexity: O(total input size of the demonstrations)
     * Space complexity: O(number of distinct required endpoints in each demonstration)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        // trace = ["/login","/feed","/cart","/login","/feed","/pay"]
        // quotas = [["/login",2],["/feed",1],["/pay",1]]
        //
        // Let's verify carefully:
        // - Need 2 "/login", 1 "/feed", 1 "/pay"
        // - "/pay" appears only at index 5, so any valid window must include index 5
        // - To get 2 "/login", we need indices 0 and 3
        // - Therefore the window must include index 0 through 5, length 6
        // So the correct answer is 6.
        String[] trace1 = {"/login", "/feed", "/cart", "/login", "/feed", "/pay"};
        String[][] quotas1 = {
                {"/login", "2"},
                {"/feed", "1"},
                {"/pay", "1"}
        };
        System.out.println(solution.shortestTraceCover(trace1, quotas1)); // 6

        // Example 2
        // trace = ["a","x","b","a","c","b","a"]
        // quotas = [["a",2],["b",1]]
        //
        // Window [0..3] = ["a","x","b","a"] has:
        // a = 2, b = 1 => valid, length 4
        // No valid length 3 window exists.
        // So answer is 4.
        String[] trace2 = {"a", "x", "b", "a", "c", "b", "a"};
        String[][] quotas2 = {
                {"a", "2"},
                {"b", "1"}
        };
        System.out.println(solution.shortestTraceCover(trace2, quotas2)); // 4

        // Extra example: duplicate quota names should be combined.
        // quotas = [["a",1],["b",1],["a",1]] means need a:2, b:1
        // trace = ["z","a","b","x","a","y"]
        // shortest valid window is ["a","b","x","a"] length 4
        String[] trace3 = {"z", "a", "b", "x", "a", "y"};
        String[][] quotas3 = {
                {"a", "1"},
                {"b", "1"},
                {"a", "1"}
        };
        System.out.println(solution.shortestTraceCover(trace3, quotas3)); // 4

        // Extra impossible example
        String[] trace4 = {"a", "b", "c"};
        String[][] quotas4 = {
                {"a", "1"},
                {"d", "1"}
        };
        System.out.println(solution.shortestTraceCover(trace4, quotas4)); // -1
    }
}