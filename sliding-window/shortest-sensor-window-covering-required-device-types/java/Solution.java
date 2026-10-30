import java.util.*;

/*
 * Title: Shortest Sensor Window Covering Required Device Types
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a stream of sensor events collected in chronological order.
 * Each event belongs to a device type represented by a string in the array events,
 * where events[i] is the type of the i-th event. You are also given a map required
 * where required[t] is the minimum number of events of device type t that must appear
 * inside a valid contiguous window.
 *
 * Your task is to return the length of the shortest contiguous subarray of events
 * that satisfies all required device-type quotas. A window [l, r] is valid if for
 * every device type t in required, the number of occurrences of t inside events[l..r]
 * is at least required[t]. If no such window exists, return -1.
 *
 * This problem is harder than a standard minimum-cover window because the stream can
 * be very large, device type names are arbitrary strings, and multiple quotas may need
 * to be met simultaneously. An efficient solution is expected.
 *
 * Constraints:
 * - 1 <= events.length <= 200000
 * - 1 <= events[i].length <= 20
 * - events[i] consists of lowercase English letters, digits, or underscores
 * - 1 <= required.size() <= 50000
 * - 1 <= required[t] <= 100000
 * - The sum of all required counts may be greater than events.length
 * - Only device types present in required matter for validity; other event types may appear anywhere in the window
 *
 * Example 1:
 * Input: events = ["cam","temp","cam","door","temp","cam","door"], required = {"cam":2,"door":1}
 * Output: 4
 * Explanation:
 * The shortest valid contiguous window is events[2..5] = ["cam","door","temp","cam"], length 4.
 * No length-3 contiguous window contains two "cam" and one "door".
 *
 * Example 2:
 * Input: events = ["a","b","c","a","b","a","d"], required = {"a":2,"b":2,"d":1}
 * Output: 6
 * Explanation:
 * The shortest valid window is events[1..6] = ["b","c","a","b","a","d"], which contains
 * two "a", two "b", and one "d". No shorter window satisfies all quotas.
 */

public class Solution {

    /**
     * Computes the length of the shortest contiguous subarray whose counts satisfy
     * all required device-type quotas.
     *
     * The algorithm uses the classic sliding window technique:
     * 1. Expand the right boundary to include more events.
     * 2. Track counts only for device types that matter (those present in required).
     * 3. Once all quotas are satisfied, try shrinking the left boundary as much as possible
     *    while keeping the window valid.
     * 4. Record the minimum valid window length seen.
     *
     * Important idea:
     * Instead of repeatedly checking every required key to know whether the current window
     * is valid, we maintain a counter called "satisfiedTypes":
     * - When a device type's count reaches exactly its required quota, that type becomes satisfied.
     * - When shrinking the window causes a type's count to drop below its quota, that type becomes unsatisfied.
     * If satisfiedTypes == required.size(), then the current window is valid.
     *
     * @param events the chronological stream of sensor event device types
     * @param required a map where each key is a required device type and each value is the minimum
     *                 number of times that type must appear in a valid window
     * @return the minimum length of a valid contiguous window, or -1 if no such window exists
     * Time complexity: O(n), where n = events.length, because each index is visited at most twice
     *                  (once by the right pointer and once by the left pointer)
     * Space complexity: O(k), where k = required.size(), for storing counts of relevant device types
     */
    public int shortestSensorWindow(String[] events, Map<String, Integer> required) {
        // Defensive handling for unusual inputs.
        // The problem guarantees valid sizes, but this keeps the method robust and beginner-friendly.
        if (events == null || required == null || events.length == 0 || required.isEmpty()) {
            return -1;
        }

        // Quick impossibility check:
        // Count total occurrences in the entire events array for only the required types.
        // If any required type does not appear enough times globally, then no window can ever satisfy the requirement.
        if (!canPossiblySatisfy(events, required)) {
            return -1;
        }

        // This map stores the current counts of relevant device types inside the sliding window.
        Map<String, Integer> windowCounts = new HashMap<>();

        // Total number of distinct required device types.
        int totalRequiredTypes = required.size();

        // Number of required device types whose current count in the window
        // has reached or exceeded the required quota.
        int satisfiedTypes = 0;

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int minLength = Integer.MAX_VALUE;

        // Expand the window by moving the right boundary from left to right.
        for (int right = 0; right < events.length; right++) {
            String currentEvent = events[right];

            // Only required device types matter for validity.
            // Irrelevant types can stay in the window, but we do not need to count them.
            if (required.containsKey(currentEvent)) {
                // Add the current event to the window count.
                int newCount = windowCounts.getOrDefault(currentEvent, 0) + 1;
                windowCounts.put(currentEvent, newCount);

                // If this type has JUST reached its required quota,
                // then one more required type is now satisfied.
                if (newCount == required.get(currentEvent)) {
                    satisfiedTypes++;
                }
            }

            // While the current window satisfies all required quotas,
            // try shrinking it from the left to make it as short as possible.
            while (satisfiedTypes == totalRequiredTypes) {
                // The current window [left, right] is valid.
                // Update the best answer.
                int currentLength = right - left + 1;
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                // We now attempt to remove events[left] and move left forward.
                String leftEvent = events[left];

                if (required.containsKey(leftEvent)) {
                    int currentCount = windowCounts.get(leftEvent);

                    // Remove one occurrence of this required type from the window.
                    int newCount = currentCount - 1;
                    windowCounts.put(leftEvent, newCount);

                    // If after removal the count drops BELOW the required quota,
                    // then the window is no longer valid for this type.
                    if (newCount < required.get(leftEvent)) {
                        satisfiedTypes--;
                    }
                }

                // Move the left boundary rightward to continue shrinking.
                left++;
            }
        }

        // If minLength was never updated, no valid window exists.
        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * Checks whether it is even possible to satisfy the required quotas using the full events array.
     * This is an optimization and also makes the logic clearer:
     * if the entire stream does not contain enough occurrences of some required type,
     * then no contiguous subarray can satisfy the requirement.
     *
     * @param events the full sensor event stream
     * @param required the required quotas for device types
     * @return true if every required type appears at least as many times as needed in the full array;
     *         false otherwise
     * Time complexity: O(n + k), where n = events.length and k = required.size()
     * Space complexity: O(k), for counting only required types
     */
    public boolean canPossiblySatisfy(String[] events, Map<String, Integer> required) {
        Map<String, Integer> totalCounts = new HashMap<>();

        // Count only events that are relevant to the requirement.
        for (String event : events) {
            if (required.containsKey(event)) {
                totalCounts.put(event, totalCounts.getOrDefault(event, 0) + 1);
            }
        }

        // Verify every required type has enough total occurrences.
        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            String type = entry.getKey();
            int needed = entry.getValue();
            int available = totalCounts.getOrDefault(type, 0);

            if (available < needed) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper method to build a map from alternating key/value pairs.
     * This is used only in the demo main method to keep sample construction concise.
     *
     * Example:
     * buildRequiredMap("cam", 2, "door", 1)
     *
     * @param pairs alternating String key and Integer value objects
     * @return a map containing the provided key/value pairs
     * Time complexity: O(k), where k is the number of pairs
     * Space complexity: O(k)
     */
    public static Map<String, Integer> buildRequiredMap(Object... pairs) {
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < pairs.length; i += 2) {
            String key = (String) pairs[i];
            Integer value = (Integer) pairs[i + 1];
            map.put(key, value);
        }

        return map;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demo setup, excluding the called algorithm
     * Space complexity: O(1) for the fixed demo setup, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[] events1 = {"cam", "temp", "cam", "door", "temp", "cam", "door"};
        Map<String, Integer> required1 = buildRequiredMap("cam", 2, "door", 1);
        int result1 = solution.shortestSensorWindow(events1, required1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 4");

        // Example 2
        String[] events2 = {"a", "b", "c", "a", "b", "a", "d"};
        Map<String, Integer> required2 = buildRequiredMap("a", 2, "b", 2, "d", 1);
        int result2 = solution.shortestSensorWindow(events2, required2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 6");

        // Additional quick checks
        String[] events3 = {"x", "y", "z"};
        Map<String, Integer> required3 = buildRequiredMap("x", 1, "z", 1);
        System.out.println("Additional example 3 result: " + solution.shortestSensorWindow(events3, required3));
        System.out.println("Expected: 3");

        String[] events4 = {"cam", "cam", "temp"};
        Map<String, Integer> required4 = buildRequiredMap("cam", 2, "door", 1);
        System.out.println("Additional example 4 result: " + solution.shortestSensorWindow(events4, required4));
        System.out.println("Expected: -1");
    }
}