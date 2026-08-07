import java.util.*;

/*
 * Title: Count Pairs of Orders With the Same Item Set
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of customer orders from an online store. Each order is represented as a list of item IDs.
 * The same item ID may appear multiple times inside one order if the customer bought more than one copy of that item.
 * Two orders are considered equivalent if they contain exactly the same distinct item IDs, regardless of the order
 * of items in the list and regardless of how many times each item appears. In other words, each order should be
 * treated as a set of item IDs, not a multiset.
 *
 * Your task is to return the number of unordered pairs of equivalent orders.
 *
 * For example, the orders [4, 2, 4, 7], [7, 2, 4], and [2, 7, 7, 4] are all equivalent because their distinct item
 * set is {2, 4, 7}. Each pair among these orders should be counted.
 *
 * Design an efficient solution using hashing. A common approach is to normalize each order into a canonical
 * representation of its distinct items, then count how many times each representation appears.
 *
 * Constraints:
 * - 1 <= orders.length <= 100000
 * - 1 <= total number of item IDs across all orders <= 200000
 * - 1 <= item IDs <= 1000000000
 * - Each order contains at least 1 item
 *
 * Example 1:
 * Input: orders = [[1,2,2,3],[3,1,2],[4,4],[4,5],[5,4,4]]
 * Output: 2
 * Explanation: The first two orders both map to the set {1,2,3}, contributing 1 pair.
 * The last two orders both map to the set {4,5}, contributing 1 pair. Total = 2.
 *
 * Example 2:
 * Input: orders = [[8],[8,8],[1,2],[2,1],[1,1,2,2],[3]]
 * Output: 4
 * Explanation: [8] and [8,8] form 1 equivalent pair.
 * The three orders [1,2], [2,1], and [1,1,2,2] all map to {1,2}, contributing 3 pairs.
 * [3] has no match. Total = 4.
 */

public class Solution {

    /**
     * Counts the number of unordered pairs of equivalent orders.
     *
     * The core idea is:
     * 1. Convert each order into a canonical representation of its distinct item IDs.
     * 2. Use a hash map to count how many times each canonical representation appears.
     * 3. If a representation has frequency f, then it contributes f * (f - 1) / 2 pairs.
     *
     * We build the answer incrementally:
     * - When we see a canonical representation that has already appeared k times,
     *   the current order forms exactly k new pairs with those previous orders.
     * - Then we increase its frequency in the map.
     *
     * @param orders the list of orders, where each order is an array of item IDs
     * @return the number of unordered pairs of equivalent orders
     * Time complexity: O(T log M), where T is the total number of item IDs across all orders,
     * and M is the size of an individual order due to sorting distinct items
     * Space complexity: O(T), for storing canonical representations in the hash map
     */
    public long countEquivalentOrderPairs(int[][] orders) {
        // This map stores:
        // key   -> canonical string representation of the distinct item set of an order
        // value -> how many previous orders had exactly this same item set
        Map<String, Integer> frequencyMap = new HashMap<>();

        // We use long because the number of pairs can be large.
        // For example, if many orders are equivalent, the number of pairs can exceed int range.
        long pairs = 0L;

        // Process each order one by one.
        for (int[] order : orders) {
            // Convert the current order into a canonical representation.
            // Orders that are equivalent must produce exactly the same key.
            String key = normalizeOrder(order);

            // If this key has appeared 'seen' times before,
            // then the current order forms 'seen' new unordered pairs.
            int seen = frequencyMap.getOrDefault(key, 0);
            pairs += seen;

            // Record that we have now seen one more order with this key.
            frequencyMap.put(key, seen + 1);
        }

        return pairs;
    }

    /**
     * Converts one order into a canonical representation of its distinct item IDs.
     *
     * Important details:
     * - Duplicates inside the order must be ignored.
     * - Item order must not matter.
     * - Therefore, we:
     *   1. Insert all items into a set to remove duplicates.
     *   2. Copy the distinct items into a list.
     *   3. Sort the list so equivalent sets always appear in the same order.
     *   4. Build a string key from the sorted distinct items.
     *
     * Example:
     * - [4, 2, 4, 7] -> distinct set {2,4,7} -> sorted [2,4,7] -> "2#4#7#"
     * - [7, 2, 4]    -> distinct set {2,4,7} -> sorted [2,4,7] -> "2#4#7#"
     *
     * @param order one customer order represented as an array of item IDs
     * @return a canonical string key representing the distinct item set of the order
     * Time complexity: O(k log k), where k is the number of items in the order
     * Space complexity: O(k), for the set, list, and resulting key
     */
    public String normalizeOrder(int[] order) {
        // Step 1: Remove duplicates by inserting all item IDs into a HashSet.
        // If an item appears multiple times in the order, the set keeps only one copy.
        Set<Integer> distinctItems = new HashSet<>();
        for (int item : order) {
            distinctItems.add(item);
        }

        // Step 2: Move the distinct items into a list so we can sort them.
        List<Integer> sortedDistinctItems = new ArrayList<>(distinctItems);

        // Step 3: Sort the distinct item IDs.
        // This ensures that equivalent sets always produce the same order in the key.
        Collections.sort(sortedDistinctItems);

        // Step 4: Build a canonical string.
        // We append a separator after each number to avoid ambiguity.
        // For example:
        // [1, 23]  -> "1#23#"
        // [12, 3]  -> "12#3#"
        // These are clearly different.
        StringBuilder keyBuilder = new StringBuilder();
        for (int item : sortedDistinctItems) {
            keyBuilder.append(item).append('#');
        }

        return keyBuilder.toString();
    }

    /**
     * Utility method to print a 2D int array in a readable format.
     *
     * @param orders the 2D array of orders to print
     * @return a string representation of the orders
     * Time complexity: O(T), where T is the total number of item IDs printed
     * Space complexity: O(T), for the generated string
     */
    public String ordersToString(int[][] orders) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        for (int i = 0; i < orders.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(Arrays.toString(orders[i]));
        }

        sb.append(']');
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * The expected outputs are:
     * - Example 1 -> 2
     * - Example 2 -> 4
     *
     * We also include one extra demonstration based on the description:
     * - [4,2,4,7], [7,2,4], [2,7,7,4]
     *   All three are equivalent, so the number of unordered pairs is 3.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size used in the demo)
     * Space complexity: O(total input size used in the demo)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] orders1 = {
            {1, 2, 2, 3},
            {3, 1, 2},
            {4, 4},
            {4, 5},
            {5, 4, 4}
        };

        int[][] orders2 = {
            {8},
            {8, 8},
            {1, 2},
            {2, 1},
            {1, 1, 2, 2},
            {3}
        };

        int[][] orders3 = {
            {4, 2, 4, 7},
            {7, 2, 4},
            {2, 7, 7, 4}
        };

        long result1 = solution.countEquivalentOrderPairs(orders1);
        long result2 = solution.countEquivalentOrderPairs(orders2);
        long result3 = solution.countEquivalentOrderPairs(orders3);

        System.out.println("Example 1:");
        System.out.println("Orders: " + solution.ordersToString(orders1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        System.out.println("Example 2:");
        System.out.println("Orders: " + solution.ordersToString(orders2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        System.out.println("Description Example:");
        System.out.println("Orders: " + solution.ordersToString(orders3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 3");
    }
}