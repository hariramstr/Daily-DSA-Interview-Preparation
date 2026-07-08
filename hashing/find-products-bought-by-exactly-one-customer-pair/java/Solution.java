import java.util.*;

/*
 * Title: Find Products Bought by Exactly One Customer Pair
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given purchase records from an online store. Each record is a pair [customerId, productId],
 * meaning that the customer bought that product at least once. The same customer may appear multiple
 * times with the same product in the input due to duplicate logs, but for this problem, repeated
 * purchases of the same product by the same customer should count only once.
 *
 * A product is called pair-exclusive if it was purchased by exactly two distinct customers, and no
 * other customer purchased it. Your task is to return all pair-exclusive products grouped by the
 * customer pair that bought them.
 *
 * More formally, for every product, consider the set of distinct customers who purchased it. If the
 * size of that set is exactly 2, let those customers be a and b. Then that product belongs to the
 * pair (min(a, b), max(a, b)). Build a mapping from each such customer pair to the list of productIds
 * that are pair-exclusive for that pair.
 *
 * Return the result as a list of entries in the form [customerA, customerB, sortedProductIds], sorted
 * first by customerA, then by customerB. Each sortedProductIds list must be in increasing order.
 *
 * Constraints:
 * - 1 <= records.length <= 200000
 * - 1 <= customerId, productId <= 10^9
 * - Duplicate records may exist
 * - The total number of distinct (customerId, productId) pairs is at most 200000
 *
 * Example 1:
 * Input: records = [[1,101],[2,101],[1,102],[3,102],[2,103],[3,103],[2,104],[1,104],[2,104]]
 * Output: [[1,2,[101,104]],[1,3,[102]],[2,3,[103]]]
 *
 * Example 2:
 * Input: records = [[5,200],[7,200],[8,200],[5,201],[7,201],[5,202],[5,202],[9,203],[10,203]]
 * Output: [[5,7,[201]],[9,10,[203]]]
 */

public class Solution {

    /**
     * Small helper object that stores the distinct customers seen for one product.
     *
     * We only care whether a product was bought by:
     * 1) one distinct customer,
     * 2) exactly two distinct customers,
     * 3) more than two distinct customers.
     *
     * So instead of storing a full set for every product, we keep a compact structure:
     * - firstCustomer: the first distinct customer seen
     * - secondCustomer: the second distinct customer seen
     * - tooManyCustomers: becomes true once we detect a third distinct customer
     *
     * This is memory-efficient and still fully correct.
     */
    private static class ProductCustomerState {
        int firstCustomer = -1;
        int secondCustomer = -1;
        boolean tooManyCustomers = false;
    }

    /**
     * Represents one output entry:
     * [customerA, customerB, sortedProductIds]
     */
    public static class ResultEntry {
        int customerA;
        int customerB;
        List<Integer> productIds;

        ResultEntry(int customerA, int customerB, List<Integer> productIds) {
            this.customerA = customerA;
            this.customerB = customerB;
            this.productIds = productIds;
        }

        @Override
        public String toString() {
            return "[" + customerA + "," + customerB + "," + productIds + "]";
        }
    }

    /**
     * Finds all pair-exclusive products and groups them by the unique customer pair that bought them.
     *
     * Step-by-step idea:
     * 1. Read every record [customerId, productId].
     * 2. For each product, track the distinct customers who bought it.
     *    - Duplicate logs like [2,104] appearing multiple times must count only once.
     *    - If a product ends up with more than two distinct customers, it is disqualified.
     * 3. After processing all records, scan every product:
     *    - If it has exactly two distinct customers, normalize the pair as (min, max).
     *    - Add the product to that pair's list.
     * 4. Sort each product list in increasing order.
     * 5. Sort the final entries by customerA, then customerB.
     *
     * Important correctness detail:
     * We do NOT need to store every duplicate purchase. If the same customer appears again for the same
     * product, we simply ignore it because the problem counts distinct customers only.
     *
     * @param records the purchase records, where each element is [customerId, productId]
     * @return a list of grouped results in the form [customerA, customerB, sortedProductIds], sorted by pair
     * Time complexity: O(n + p log p + k log k), where n is the number of records, p is the total number
     * of pair-exclusive products being sorted inside groups, and k is the number of output customer pairs.
     * In practice, this is dominated by hashing plus sorting the final grouped products.
     * Space complexity: O(d), where d is the number of distinct products plus the output size.
     */
    public List<ResultEntry> findPairExclusiveProducts(int[][] records) {
        // Map from productId -> compact state describing which distinct customers bought it.
        Map<Integer, ProductCustomerState> productToCustomers = new HashMap<>();

        // ---------------------------------------------------------------------
        // First pass:
        // Build the distinct-customer information for each product.
        // ---------------------------------------------------------------------
        for (int[] record : records) {
            int customerId = record[0];
            int productId = record[1];

            // Get or create the state object for this product.
            ProductCustomerState state = productToCustomers.computeIfAbsent(productId, k -> new ProductCustomerState());

            // If we already know this product has more than two distinct customers,
            // then it can never be pair-exclusive. We can safely ignore further logs.
            if (state.tooManyCustomers) {
                continue;
            }

            // Case 1: no customer recorded yet for this product.
            if (state.firstCustomer == -1) {
                state.firstCustomer = customerId;
                continue;
            }

            // If this log is a duplicate of the first customer, ignore it.
            if (state.firstCustomer == customerId) {
                continue;
            }

            // Case 2: first distinct customer already exists, second does not.
            if (state.secondCustomer == -1) {
                state.secondCustomer = customerId;
                continue;
            }

            // If this log is a duplicate of the second customer, ignore it.
            if (state.secondCustomer == customerId) {
                continue;
            }

            // If we reach here, we found a third distinct customer.
            // That means this product is no longer pair-exclusive.
            state.tooManyCustomers = true;
        }

        // Map from normalized customer pair -> list of productIds.
        //
        // We encode the pair (a, b) into one long key:
        // key = ((long) a << 32) | (b & 0xffffffffL)
        //
        // Since customerId <= 1e9, int is safe, and this encoding is collision-free for ordered pairs.
        Map<Long, List<Integer>> pairToProducts = new HashMap<>();

        // ---------------------------------------------------------------------
        // Second pass over products:
        // Keep only products bought by exactly two distinct customers.
        // ---------------------------------------------------------------------
        for (Map.Entry<Integer, ProductCustomerState> entry : productToCustomers.entrySet()) {
            int productId = entry.getKey();
            ProductCustomerState state = entry.getValue();

            // A valid pair-exclusive product must:
            // - not have too many customers
            // - have first customer present
            // - have second customer present
            //
            // If secondCustomer == -1, then only one distinct customer bought it,
            // so it must be excluded.
            if (state.tooManyCustomers || state.firstCustomer == -1 || state.secondCustomer == -1) {
                continue;
            }

            // Normalize the pair so that smaller customer id comes first.
            int a = Math.min(state.firstCustomer, state.secondCustomer);
            int b = Math.max(state.firstCustomer, state.secondCustomer);

            long pairKey = encodePair(a, b);

            // Add this product to the list for that customer pair.
            pairToProducts.computeIfAbsent(pairKey, k -> new ArrayList<>()).add(productId);
        }

        // ---------------------------------------------------------------------
        // Build final answer:
        // - sort product ids inside each pair
        // - convert map entries into ResultEntry objects
        // - sort final entries by customerA, then customerB
        // ---------------------------------------------------------------------
        List<ResultEntry> result = new ArrayList<>();

        for (Map.Entry<Long, List<Integer>> entry : pairToProducts.entrySet()) {
            long pairKey = entry.getKey();
            List<Integer> productIds = entry.getValue();

            // Product ids for each pair must be in increasing order.
            Collections.sort(productIds);

            int customerA = decodeFirst(pairKey);
            int customerB = decodeSecond(pairKey);

            result.add(new ResultEntry(customerA, customerB, productIds));
        }

        // Sort final output by customerA, then by customerB.
        result.sort((x, y) -> {
            if (x.customerA != y.customerA) {
                return Integer.compare(x.customerA, y.customerA);
            }
            return Integer.compare(x.customerB, y.customerB);
        });

        return result;
    }

    /**
     * Encodes an ordered pair of integers into one long key.
     *
     * @param a the first customer id
     * @param b the second customer id
     * @return a unique long key representing the ordered pair (a, b)
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long encodePair(int a, int b) {
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    /**
     * Decodes the first integer from an encoded pair key.
     *
     * @param key the encoded pair key
     * @return the first customer id
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int decodeFirst(long key) {
        return (int) (key >>> 32);
    }

    /**
     * Decodes the second integer from an encoded pair key.
     *
     * @param key the encoded pair key
     * @return the second customer id
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int decodeSecond(long key) {
        return (int) key;
    }

    /**
     * Converts the result entries into a nested list structure:
     * [customerA, customerB, sortedProductIds]
     *
     * This is useful if you want a structure closer to the problem statement.
     *
     * @param entries the result entries produced by the algorithm
     * @return a nested list representation of the result
     * Time complexity: O(m), where m is the total size of the output
     * Space complexity: O(m), for the constructed nested lists
     */
    public List<List<Object>> toNestedList(List<ResultEntry> entries) {
        List<List<Object>> nested = new ArrayList<>();

        for (ResultEntry entry : entries) {
            List<Object> row = new ArrayList<>();
            row.add(entry.customerA);
            row.add(entry.customerB);
            row.add(new ArrayList<>(entry.productIds));
            nested.add(row);
        }

        return nested;
    }

    /**
     * Pretty-prints the result in the exact visual style used by the examples.
     *
     * @param entries the result entries to print
     * @return a string like [[1,2,[101,104]],[1,3,[102]]]
     * Time complexity: O(m), where m is the total size of the output
     * Space complexity: O(m), due to the StringBuilder content
     */
    public String formatResult(List<ResultEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < entries.size(); i++) {
            ResultEntry entry = entries.get(i);
            if (i > 0) {
                sb.append(",");
            }

            sb.append("[")
              .append(entry.customerA)
              .append(",")
              .append(entry.customerB)
              .append(",")
              .append(entry.productIds)
              .append("]");
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Verified manually:
     *
     * Example 1:
     * records = [[1,101],[2,101],[1,102],[3,102],[2,103],[3,103],[2,104],[1,104],[2,104]]
     * - 101 -> customers {1,2} => pair (1,2)
     * - 102 -> customers {1,3} => pair (1,3)
     * - 103 -> customers {2,3} => pair (2,3)
     * - 104 -> customers {2,1} with duplicate [2,104] => still pair (1,2)
     * Output: [[1,2,[101,104]],[1,3,[102]],[2,3,[103]]]
     *
     * Example 2:
     * records = [[5,200],[7,200],[8,200],[5,201],[7,201],[5,202],[5,202],[9,203],[10,203]]
     * - 200 -> customers {5,7,8} => excluded
     * - 201 -> customers {5,7} => pair (5,7)
     * - 202 -> customers {5} => excluded
     * - 203 -> customers {9,10} => pair (9,10)
     * Output: [[5,7,[201]],[9,10,[203]]]
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) outside the calls to the main algorithm
     * Space complexity: O(1) outside the data created for demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] records1 = {
            {1, 101}, {2, 101}, {1, 102}, {3, 102},
            {2, 103}, {3, 103}, {2, 104}, {1, 104}, {2, 104}
        };

        int[][] records2 = {
            {5, 200}, {7, 200}, {8, 200}, {5, 201},
            {7, 201}, {5, 202}, {5, 202}, {9, 203}, {10, 203}
        };

        List<ResultEntry> result1 = solution.findPairExclusiveProducts(records1);
        List<ResultEntry> result2 = solution.findPairExclusiveProducts(records2);

        System.out.println("Example 1 Output:");
        System.out.println(solution.formatResult(result1));
        System.out.println("Expected:");
        System.out.println("[[1,2,[101, 104]],[1,3,[102]],[2,3,[103]]]");

        System.out.println();

        System.out.println("Example 2 Output:");
        System.out.println(solution.formatResult(result2));
        System.out.println("Expected:");
        System.out.println("[[5,7,[201]],[9,10,[203]]]");
    }
}