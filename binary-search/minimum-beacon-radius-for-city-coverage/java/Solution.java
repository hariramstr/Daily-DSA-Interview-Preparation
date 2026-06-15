import java.util.*;

/*
 * Title: Minimum Beacon Radius for City Coverage
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A city has several fixed beacon towers placed along a straight highway. Each tower can broadcast
 * a signal equally in both directions, and all towers must use the same broadcast radius R.
 * You are given two integer arrays: houses, where houses[i] is the position of the i-th house
 * on the highway, and beacons, where beacons[j] is the position of the j-th beacon tower.
 * A house is considered covered if there exists at least one beacon whose distance to that house
 * is at most R.
 *
 * Your task is to return the minimum integer radius R such that every house is covered.
 *
 * The input arrays are not guaranteed to be sorted. Positions may be negative, zero, or positive.
 * Multiple houses or beacons may share the same position. You should design an efficient solution
 * that works for large inputs.
 *
 * A common approach is to sort the beacon positions and, for each house, determine the nearest
 * beacon using binary search. The answer is the maximum among these nearest distances.
 * Equivalent binary-search-on-answer solutions are also acceptable if implemented efficiently.
 *
 * Constraints:
 * - 1 <= houses.length <= 200000
 * - 1 <= beacons.length <= 200000
 * - -10^9 <= houses[i], beacons[j] <= 10^9
 * - The result fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: houses = [1, 5, 9], beacons = [2, 8]
 * Output: 3
 * Explanation:
 * House 1 is covered by beacon 2 with radius 1.
 * House 5 is distance 3 from beacon 2 and distance 3 from beacon 8.
 * House 9 is distance 1 from beacon 8.
 * The smallest radius that covers all houses is 3.
 *
 * Example 2:
 * Input: houses = [-4, 0, 7, 15], beacons = [-2, 10]
 * Output: 5
 * Explanation:
 * Distances to the nearest beacon are 2, 2, 3, and 5 respectively.
 * Therefore, the minimum radius required is 5.
 */

public class Solution {

    /**
     * Computes the minimum integer radius needed so that every house is covered by at least one beacon.
     *
     * Algorithm idea:
     * 1. Sort the beacon positions.
     * 2. For each house, use binary search to find the insertion position in the sorted beacon array.
     * 3. The nearest beacon must be either:
     *    - the beacon just to the left of the insertion position, or
     *    - the beacon at the insertion position itself.
     * 4. Compute the distance to the nearest beacon for that house.
     * 5. The answer is the maximum such nearest distance across all houses.
     *
     * @param houses the positions of all houses along the highway
     * @param beacons the positions of all beacon towers along the highway
     * @return the minimum integer radius required to cover every house
     * Time complexity: O(m log m + n log m), where n = houses.length and m = beacons.length
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public int minimumBeaconRadius(int[] houses, int[] beacons) {
        // Defensive handling is not strictly necessary under the given constraints,
        // but this makes the method safer and easier to understand for beginners.
        if (houses == null || houses.length == 0) {
            return 0;
        }
        if (beacons == null || beacons.length == 0) {
            throw new IllegalArgumentException("At least one beacon is required to cover houses.");
        }

        // Step 1:
        // Sort beacon positions so we can use binary search efficiently.
        // After sorting, nearby beacons in terms of position are also nearby in the array.
        Arrays.sort(beacons);

        // This variable will store the final answer:
        // the largest "nearest beacon distance" among all houses.
        int requiredRadius = 0;

        // Step 2:
        // Process each house independently.
        for (int house : houses) {
            // For the current house, find the distance to its nearest beacon.
            int nearestDistance = distanceToNearestBeacon(house, beacons);

            // Step 3:
            // Since one global radius must work for every house,
            // we must choose a radius at least as large as the worst-case house.
            requiredRadius = Math.max(requiredRadius, nearestDistance);
        }

        return requiredRadius;
    }

    /**
     * Finds the distance from a single house to its nearest beacon using binary search.
     *
     * Detailed logic:
     * - We binary search for the first beacon position that is >= house.
     * - Let that index be "rightIndex".
     * - Then the nearest beacon can only be:
     *   1. beacons[rightIndex]     (the first beacon not smaller than the house), or
     *   2. beacons[rightIndex - 1] (the last beacon smaller than the house)
     * - We compute both distances when valid and return the smaller one.
     *
     * @param house the position of the house
     * @param sortedBeacons beacon positions sorted in non-decreasing order
     * @return the minimum distance from the house to any beacon
     * Time complexity: O(log m), where m = sortedBeacons.length
     * Space complexity: O(1)
     */
    public int distanceToNearestBeacon(int house, int[] sortedBeacons) {
        // We want the first index i such that sortedBeacons[i] >= house.
        // This is a classic "lower bound" binary search.
        int left = 0;
        int right = sortedBeacons.length - 1;
        int firstGreaterOrEqualIndex = sortedBeacons.length; // default means "not found"

        while (left <= right) {
            // Standard midpoint calculation that avoids overflow.
            int mid = left + (right - left) / 2;

            if (sortedBeacons[mid] >= house) {
                // This beacon is a candidate for the first beacon >= house.
                firstGreaterOrEqualIndex = mid;

                // But there may be an even earlier such beacon, so continue searching left half.
                right = mid - 1;
            } else {
                // This beacon is too far left (strictly smaller than house),
                // so the desired index must be to the right.
                left = mid + 1;
            }
        }

        // We now examine at most two candidate beacons:
        // 1) the beacon at firstGreaterOrEqualIndex, if it exists
        // 2) the beacon immediately before it, if it exists
        long bestDistance = Long.MAX_VALUE;

        // Candidate on the right side (or exactly at the house position).
        if (firstGreaterOrEqualIndex < sortedBeacons.length) {
            bestDistance = Math.min(bestDistance,
                    Math.abs((long) sortedBeacons[firstGreaterOrEqualIndex] - house));
        }

        // Candidate on the left side.
        if (firstGreaterOrEqualIndex - 1 >= 0) {
            bestDistance = Math.min(bestDistance,
                    Math.abs((long) sortedBeacons[firstGreaterOrEqualIndex - 1] - house));
        }

        return (int) bestDistance;
    }

    /**
     * Alternative solution using binary search on the answer.
     *
     * This method is included for educational purposes.
     * It searches for the smallest radius R such that all houses are coverable.
     *
     * High-level idea:
     * - Sort both houses and beacons.
     * - Binary search the radius R.
     * - For each candidate radius, greedily verify whether every house is covered.
     *
     * @param houses the positions of all houses
     * @param beacons the positions of all beacons
     * @return the minimum radius that covers all houses
     * Time complexity: O((n log n + m log m) + (n + m) log D), where D is the search range of radius
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public int minimumBeaconRadiusBinarySearchOnAnswer(int[] houses, int[] beacons) {
        if (houses == null || houses.length == 0) {
            return 0;
        }
        if (beacons == null || beacons.length == 0) {
            throw new IllegalArgumentException("At least one beacon is required to cover houses.");
        }

        Arrays.sort(houses);
        Arrays.sort(beacons);

        int low = 0;
        int high = 2_000_000_000; // safe upper bound because positions are within [-1e9, 1e9]

        while (low < high) {
            int mid = low + (high - low) / 2;

            if (canCoverAllHouses(houses, beacons, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether a given radius is sufficient to cover all houses.
     *
     * Since both arrays are sorted:
     * - We walk through houses from left to right.
     * - We advance the beacon pointer whenever the current beacon is too far left to cover the house.
     * - If the current beacon can cover the house, we continue.
     * - If no beacon can cover a house, the radius is insufficient.
     *
     * @param sortedHouses sorted house positions
     * @param sortedBeacons sorted beacon positions
     * @param radius candidate radius
     * @return true if every house is covered, otherwise false
     * Time complexity: O(n + m)
     * Space complexity: O(1)
     */
    public boolean canCoverAllHouses(int[] sortedHouses, int[] sortedBeacons, int radius) {
        int beaconIndex = 0;

        for (int house : sortedHouses) {
            // Move beaconIndex forward while the current beacon is too far left
            // to cover this house.
            while (beaconIndex < sortedBeacons.length
                    && (long) sortedBeacons[beaconIndex] + radius < house) {
                beaconIndex++;
            }

            // If we ran out of beacons, this house cannot be covered.
            if (beaconIndex == sortedBeacons.length) {
                return false;
            }

            // Now check whether the current beacon covers the house.
            // If even this beacon is too far right, then no beacon can cover the house,
            // because all later beacons are even farther right.
            if ((long) sortedBeacons[beaconIndex] - radius > house) {
                return false;
            }
        }

        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called methods
     * Space complexity: O(1), excluding the called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] houses1 = {1, 5, 9};
        int[] beacons1 = {2, 8};
        int result1 = solution.minimumBeaconRadius(houses1.clone(), beacons1.clone());
        System.out.println("Example 1 Result: " + result1); // Expected: 3

        // Example 2
        int[] houses2 = {-4, 0, 7, 15};
        int[] beacons2 = {-2, 10};
        int result2 = solution.minimumBeaconRadius(houses2.clone(), beacons2.clone());
        System.out.println("Example 2 Result: " + result2); // Expected: 5

        // Demonstrate the alternative binary-search-on-answer method as well.
        int result1Alt = solution.minimumBeaconRadiusBinarySearchOnAnswer(houses1.clone(), beacons1.clone());
        int result2Alt = solution.minimumBeaconRadiusBinarySearchOnAnswer(houses2.clone(), beacons2.clone());
        System.out.println("Example 1 Result (Alternative Method): " + result1Alt); // Expected: 3
        System.out.println("Example 2 Result (Alternative Method): " + result2Alt); // Expected: 5
    }
}