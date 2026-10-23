import java.util.*;

/*
 * Title: Shortest Warehouse Cart Span Covering All Pick Zones
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A warehouse management system records the sequence of pick zones visited by a cart during a single route.
 * Each zone is represented by a string such as "A1", "Cold", or "Fragile". You are given an array zones,
 * where zones[i] is the zone visited at step i, and another array requiredZones listing distinct zones that
 * must all appear in a valid segment.
 *
 * Your task is to find the length of the shortest contiguous segment of the route that contains every zone
 * in requiredZones at least once. If no such segment exists, return -1.
 *
 * This problem models a common optimization task: finding the smallest portion of an event stream that
 * satisfies a complete set of operational requirements. The segment must be contiguous, and extra zones
 * not in requiredZones are allowed inside the segment.
 *
 * Return only the minimum length, not the segment itself.
 *
 * Constraints:
 * - 1 <= zones.length <= 200000
 * - 1 <= requiredZones.length <= 5000
 * - requiredZones contains distinct strings
 * - 1 <= zones[i].length, requiredZones[i].length <= 20
 * - Strings consist of letters, digits, underscores, or hyphens
 *
 * Example 1:
 * Input: zones = ["Inbound","A1","Cold","B2","Fragile","Cold","A1","Packing"],
 *        requiredZones = ["A1","Cold","Fragile"]
 * Output: 3
 * Explanation: The shortest valid segment is ["A1","Cold","Fragile"], which has length 3.
 *
 * Example 2:
 * Input: zones = ["Dock","A1","B2","Dock","Packing"], requiredZones = ["A1","Cold"]
 * Output: -1
 * Explanation: The zone "Cold" never appears in the route, so no valid contiguous segment exists.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous segment in {@code zones} that contains
     * every zone listed in {@code requiredZones} at least once.
     *
     * The algorithm uses the classic sliding window / two-pointers technique:
     * 1. Expand the right pointer to include more elements.
     * 2. Track how many required zone types are currently satisfied in the window.
     * 3. Once all required zones are present, move the left pointer forward as much as possible
     *    while still keeping the window valid.
     * 4. Record the minimum valid window length seen.
     *
     * @param zones the full route of visited warehouse zones
     * @param requiredZones the distinct set of zones that must all appear in the segment
     * @return the minimum length of a contiguous segment containing all required zones,
     *         or -1 if no such segment exists
     *
     * Time complexity: O(n + m), where n = zones.length and m = requiredZones.length
     * Space complexity: O(m), for the hash-based structures storing required zones and counts
     */
    public int shortestWarehouseCartSpan(String[] zones, String[] requiredZones) {
        if (zones == null || requiredZones == null || zones.length == 0 || requiredZones.length == 0) {
            return -1;
        }

        // Store all required zones in a set so we can quickly check whether a zone matters.
        Set<String> requiredSet = new HashSet<>();
        for (String zone : requiredZones) {
            requiredSet.add(zone);
        }

        // This map stores how many times each required zone currently appears inside the window.
        Map<String, Integer> windowCount = new HashMap<>();

        // Number of distinct required zone types that are currently satisfied in the window.
        // A required zone becomes "formed" when its count in the window goes from 0 to 1.
        int formed = 0;

        // Total number of distinct required zone types we need to satisfy.
        int need = requiredSet.size();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far. Start with a very large number.
        int minLength = Integer.MAX_VALUE;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < zones.length; right++) {
            String currentZone = zones[right];

            // Only required zones affect the validity of the window.
            if (requiredSet.contains(currentZone)) {
                int newCount = windowCount.getOrDefault(currentZone, 0) + 1;
                windowCount.put(currentZone, newCount);

                // If this zone was absent before and now appears once,
                // we have satisfied one more required zone type.
                if (newCount == 1) {
                    formed++;
                }
            }

            // If the current window contains all required zone types,
            // try to shrink it from the left to make it as short as possible.
            while (formed == need && left <= right) {
                // Current window length is inclusive: right - left + 1
                int currentLength = right - left + 1;
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                String leftZone = zones[left];

                // We are about to remove zones[left] from the window by moving left forward.
                // If it is a required zone, update its count.
                if (requiredSet.contains(leftZone)) {
                    int updatedCount = windowCount.get(leftZone) - 1;
                    windowCount.put(leftZone, updatedCount);

                    // If the count drops to 0, the window is no longer valid,
                    // because one required zone type is now missing.
                    if (updatedCount == 0) {
                        formed--;
                    }
                }

                // Actually shrink the window.
                left++;
            }
        }

        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * A convenience wrapper method using the exact problem wording.
     *
     * @param zones the sequence of visited zones during the cart route
     * @param requiredZones the distinct zones that must all appear in the chosen segment
     * @return the length of the shortest valid contiguous segment, or -1 if impossible
     *
     * Time complexity: O(n + m), where n = zones.length and m = requiredZones.length
     * Space complexity: O(m)
     */
    public int minSegmentLength(String[] zones, String[] requiredZones) {
        return shortestWarehouseCartSpan(zones, requiredZones);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(n + m) per demonstration call
     * Space complexity: O(m) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] zones1 = {"Inbound", "A1", "Cold", "B2", "Fragile", "Cold", "A1", "Packing"};
        String[] required1 = {"A1", "Cold", "Fragile"};
        int result1 = solution.shortestWarehouseCartSpan(zones1, required1);
        System.out.println(result1); // Expected: 3

        String[] zones2 = {"Dock", "A1", "B2", "Dock", "Packing"};
        String[] required2 = {"A1", "Cold"};
        int result2 = solution.shortestWarehouseCartSpan(zones2, required2);
        System.out.println(result2); // Expected: -1

        // Additional quick sanity checks for beginner-friendly demonstration.
        String[] zones3 = {"A1"};
        String[] required3 = {"A1"};
        int result3 = solution.shortestWarehouseCartSpan(zones3, required3);
        System.out.println(result3); // Expected: 1

        String[] zones4 = {"X", "A1", "Y", "Cold", "Z", "Fragile"};
        String[] required4 = {"A1", "Cold", "Fragile"};
        int result4 = solution.shortestWarehouseCartSpan(zones4, required4);
        System.out.println(result4); // Expected: 5
    }
}