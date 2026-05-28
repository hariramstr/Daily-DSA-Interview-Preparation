```java
/*
 * Title: Vacation Itinerary Collision Finder
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * A travel company manages bookings for multiple tourists. Each tourist has an itinerary
 * represented as an ordered list of city codes (strings). Two tourists are said to have a
 * "collision" if they visit at least k consecutive cities in the exact same order at any
 * point in their respective itineraries.
 *
 * Given a list of itineraries (each a list of city codes) and an integer k, return a list
 * of all unique pairs [i, j] (where i < j) such that tourist i and tourist j have a collision.
 * Return the pairs sorted in ascending order by i, then by j.
 *
 * Constraints:
 * - 2 <= itineraries.length <= 200
 * - 1 <= itineraries[i].length <= 500
 * - 1 <= k <= 50
 * - Each city code is a non-empty string of uppercase letters with length between 1 and 5.
 * - The same city may appear multiple times in an itinerary.
 *
 * Example 1:
 * Input: itineraries = [["NYC","LAX","CHI","MIA"],["SEA","NYC","LAX","CHI"],["BOS","MIA","DFW"]], k = 3
 * Output: [[0, 1]]
 * Explanation: Tourist 0 and Tourist 1 share the consecutive sequence ["NYC","LAX","CHI"] of length 3.
 *
 * Example 2:
 * Input: itineraries = [["A","B","C"],["B","C","D"],["A","B","C","D"]], k = 2
 * Output: [[0, 1], [0, 2], [1, 2]]
 * Explanation: Pair (0,1) shares ["B","C"], pair (0,2) shares ["A","B","C"],
 *              and pair (1,2) shares ["B","C","D"].
 */

import java.util.*;

/**
 * Solution class for the Vacation Itinerary Collision Finder problem.
 * Uses hashing to efficiently find pairs of tourists who share at least k consecutive cities.
 */
public class Solution {

    /**
     * Finds all pairs of tourists whose itineraries share at least k consecutive cities
     * in the same order.
     *
     * <p>Algorithm Overview:
     * For each itinerary, we extract all "windows" (substrings of length k) and store them
     * in a map from window -> set of tourist indices. Then, any window that appears in 2+
     * itineraries means those tourists have a collision.
     *
     * @param itineraries A list of itineraries, each being a list of city codes.
     * @param k           The minimum number of consecutive cities required for a collision.
     * @return A sorted list of pairs [i, j] (i < j) where tourists i and j have a collision.
     *         Time complexity: O(n * m * k) where n = number of itineraries, m = max itinerary length
     *         Space complexity: O(n * m) for storing all windows in the hash map
     */
    public List<List<Integer>> findCollisions(List<List<String>> itineraries, int k) {
        // Step 1: Create a map from "window string" -> set of tourist indices that have that window.
        // A "window" is a sequence of exactly k consecutive cities joined by a delimiter.
        // We use a delimiter (e.g., "#") that won't appear in city codes to avoid false matches.
        Map<String, Set<Integer>> windowToTourists = new HashMap<>();

        // Step 2: Iterate over each tourist's itinerary
        for (int i = 0; i < itineraries.size(); i++) {
            List<String> itinerary = itineraries.get(i);

            // Step 3: If this itinerary is shorter than k, no window of size k can be formed.
            // Skip it entirely.
            if (itinerary.size() < k) {
                continue;
            }

            // Step 4: Slide a window of size k over the itinerary.
            // For each starting position, build a string representing the window.
            // The number of valid starting positions is (itinerary.size() - k + 1).
            for (int start = 0; start <= itinerary.size() - k; start++) {
                // Step 5: Build the window string by joining k cities with "#" delimiter.
                // Example: cities ["NYC","LAX","CHI"] -> "NYC#LAX#CHI"
                StringBuilder sb = new StringBuilder();
                for (int pos = start; pos < start + k; pos++) {
                    if (pos > start) {
                        sb.append("#"); // delimiter between city codes
                    }
                    sb.append(itinerary.get(pos));
                }
                String windowKey = sb.toString();

                // Step 6: Add tourist index i to the set for this window key.
                // computeIfAbsent creates a new HashSet if the key doesn't exist yet.
                windowToTourists.computeIfAbsent(windowKey, key -> new HashSet<>()).add(i);
            }
        }

        // Step 7: Now find all pairs. We use a Set<String> to track already-found pairs
        // to avoid duplicates (since multiple shared windows could produce the same pair).
        Set<String> foundPairs = new HashSet<>();
        List<List<Integer>> result = new ArrayList<>();

        // Step 8: For each window that appears in 2 or more itineraries,
        // generate all pairs from the set of tourist indices.
        for (Map.Entry<String, Set<Integer>> entry : windowToTourists.entrySet()) {
            Set<Integer> tourists = entry.getValue();

            // Only process windows shared by at least 2 tourists
            if (tourists.size() < 2) {
                continue;
            }

            // Step 9: Convert the set to a sorted list so we can generate pairs in order.
            List<Integer> touristList = new ArrayList<>(tourists);
            Collections.sort(touristList);

            // Step 10: Generate all pairs (i, j) with i < j from this list.
            for (int a = 0; a < touristList.size(); a++) {
                for (int b = a + 1; b < touristList.size(); b++) {
                    int ti = touristList.get(a); // smaller index
                    int tj = touristList.get(b); // larger index

                    // Step 11: Create a unique key for this pair to avoid duplicates.
                    // Since ti < tj always (due to sorted order), "ti,tj" is a unique key.
                    String pairKey = ti + "," + tj;

                    // Step 12: Only add the pair if we haven't seen it before.
                    if (!foundPairs.contains(pairKey)) {
                        foundPairs.add(pairKey);
                        result.add(Arrays.asList(ti, tj));
                    }
                }
            }
        }

        // Step 13: Sort the result list by first element (i), then by second element (j).
        // This ensures the output is in ascending order as required.
        result.sort((p1, p2) -> {
            if (!p1.get(0).equals(p2.get(0))) {
                return p1.get(0) - p2.get(0); // sort by i first
            }
            return p1.get(1) - p2.get(1); // then sort by j
        });

        // Step 14: Return the final sorted list of collision pairs.
        return result;
    }

    /**
     * Helper method to convert a 2D array of strings to a List of Lists of Strings.
     * This makes it easier to call the main method with array literals.
     *
     * @param arr A 2D array of strings representing itineraries.
     * @return A List of Lists of Strings.
     *         Time complexity: O(n * m)
     *         Space complexity: O(n * m)
     */
    public static List<List<String>> toListOfLists(String[][] arr) {
        List<List<String>> result = new ArrayList<>();
        for (String[] row : arr) {
            result.add(new ArrayList<>(Arrays.asList(row)));
        }
        return result;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem description.
     * Traces through both examples and prints results.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // itineraries = [["NYC","LAX","CHI","MIA"],["SEA","NYC","LAX","CHI"],["BOS","MIA","DFW"]]
        // k = 3
        // Expected Output: [[0, 1]]
        // Explanation: Tourist 0 and Tourist 1 share ["NYC","LAX","CHI"]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        String[][] arr1 = {
            {"NYC", "LAX", "CHI", "MIA"},
            {"SEA", "NYC", "LAX", "CHI"},
            {"BOS", "MIA", "DFW"}
        };
        List<List<String>> itineraries1 = toListOfLists(arr1);
        int k1 = 3;

        System.out.println("Itineraries:");
        for (int i = 0; i < itineraries1.size(); i++) {
            System.out.println("  Tourist " + i + ": " + itineraries1.get(i));
        }
        System.out.println("k = " + k1);

        List<List<Integer>> result1 = solution.findCollisions(itineraries1, k1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: [[0, 1]]");
        System.out.println("Match: " + result1.toString().equals("[[0, 1]]"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // itineraries = [["A","B","C"],["B","C","D"],["A","B","C","D"]]
        // k = 2
        // Expected Output: [[0, 1], [0, 2], [1, 2]]
        // Explanation:
        //   Pair (0,1): share ["B","C"]
        //   Pair (0,2): share ["A","B"] and ["B","C"] (both are windows of size 2)
        //   Pair (1,2): share ["B","C"] and ["C","D"]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        String[][] arr2 = {
            {"A", "B", "C"},
            {"B", "C", "D"},
            {"A", "B", "C", "D"}
        };
        List<List<String>> itineraries2 = toListOfLists(arr2);
        int k2 = 2;

        System.out.println("Itineraries:");
        for (int i = 0; i < itineraries2.size(); i++) {
            System.out.println("  Tourist " + i + ": " + itineraries2.get(i));
        }
        System.out.println("k = " + k2);

        List<List<Integer>> result2 = solution.findCollisions(itineraries2, k2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: [[0, 1], [0, 2], [1, 2]]");
        System.out.println("Match: " + result2.toString().equals("[[0, 1], [0, 2], [1, 2]]"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test: No collisions
        // itineraries = [["A","B","C"],["D","E","F"]]
        // k = 2
        // Expected Output: []
        // -----------------------------------------------------------------------
        System.out.println("=== Additional Test: No Collisions ===");
        String[][] arr3 = {
            {"A", "B", "C"},
            {"D", "E", "F"}
        };
        List<List<String>> itineraries3 = toListOfLists(arr3);
        int k3 = 2;

        System.out.println("Itineraries:");
        for (int i = 0; i < itineraries3.size(); i++) {
            System.out.println("  Tourist " + i + ": " + itineraries3.get(i));
        }
        System.out.println("k = " + k3);

        List<List<Integer>> result3 = solution.findCollisions(itineraries3, k3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: []");
        System.out.println("Match: " + result3.isEmpty());
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test: k = 1 (every shared city is a collision)
        // itineraries = [["A","B"],["B","C"],["C","D"]]
        // k = 1
        // Expected: (0,1) share "B", (1,2) share "C" -> [[0,1],[1,2]]
        // -----------------------------------------------------------------------
        System.out.println("=== Additional Test: k=1 ===");
        String[][] arr4 = {
            {"A", "B"},
            {"B", "C"},
            {"C", "D"}
        };
        List<List<String>> itineraries4 = toListOfLists(arr4);
        int k4 = 1;

        System.out.println("Itineraries:");
        for (int i = 0; i < itineraries4.size(); i++) {
            System.out.println("  Tourist " + i + ": " + itineraries4.get(i));
        }
        System.out.println("k = " + k4);

        List<List<Integer>> result4 = solution.findCollisions(itineraries4, k4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: [[0, 1], [1, 2]]");
        System.out.println("Match: " + result4.toString().equals("[[0, 1], [1, 2]]"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test: k larger than all itineraries
        // itineraries = [["A","B","C"],["A","B","C"]]
        // k = 10
        // Expected: [] (no itinerary has 10 cities)
        // -----------------------------------------------------------------------
        System.out.println("=== Additional Test: k larger than itinerary length ===");
        String[][] arr5 = {
            {"A", "B", "C"},
            {"A", "B", "C"}
        };
        List<List<String>> itineraries5 = toListOfLists(arr5);
        int k5 = 10;

        System.out.println("Itineraries:");
        for (int i = 0; i < itineraries5.size(); i++) {
            System.out.println("  Tourist " + i + ": " + itineraries5.get(i));
        }
        System.out.println("k = " + k5);

        List<List<Integer>> result5 = solution.findCollisions(itineraries5, k5);
        System.out.println("Output: " + result5);
        System.out.println("Expected: []");
        System.out.println("Match: " + result5.isEmpty());
    }
}
```