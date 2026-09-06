import java.util.*;

/*
Title: Count Pairs of Receipts With the Same Tax Breakdown
Difficulty: Medium
Topic: Hashing

Problem Description:
A retail platform stores each receipt as a list of purchased line items. Every line item is represented by a pair [category, amount], where category is a lowercase string such as "food" or "electronics", and amount is a positive integer in cents. Two receipts are considered tax-equivalent if, after summing amounts by category, they produce exactly the same category-to-total mapping. The order of line items does not matter, and repeated categories within the same receipt should be merged by addition before comparison.

Your task is to return the number of unordered pairs of receipts that are tax-equivalent.

For example, the receipts [["food", 200], ["book", 500], ["food", 300]] and [["book", 500], ["food", 500]] are tax-equivalent because both reduce to {"food": 500, "book": 500}. However, receipts with the same set of categories but different totals are not equivalent.

Design an efficient solution using hashing. A common approach is to convert each receipt into a canonical signature that uniquely represents its aggregated category totals, then count how many times each signature appears.

Constraints:
- 1 <= receipts.length <= 100000
- 1 <= total number of line items across all receipts <= 200000
- 1 <= category.length <= 20
- category consists of lowercase English letters
- 1 <= amount <= 100000
- The answer may not fit in 32-bit integer; use 64-bit arithmetic

Example 1:
Input: receipts = [
  [["food",200],["book",500],["food",300]],
  [["book",500],["food",500]],
  [["food",200],["book",400]],
  [["book",500],["food",500],["toy",100]]
]
Output: 1
Explanation: Only the first and second receipts reduce to the same aggregated mapping.

Example 2:
Input: receipts = [
  [["a",10],["b",5],["a",5]],
  [["b",5],["a",15]],
  [["a",15]],
  [["c",7]],
  [["c",7]],
  [["b",5],["a",15]]
]
Output: 4
Explanation: Receipts 0, 1, and 5 are equivalent, contributing 3 pairs. Receipts 3 and 4 contribute 1 more pair. Total = 4.
*/

public class Solution {

    /**
     * Counts the number of unordered pairs of receipts that are tax-equivalent.
     *
     * A receipt is first reduced into its aggregated category-to-total mapping.
     * Then that mapping is converted into a canonical signature string.
     * Receipts with the same signature are equivalent, so if a signature appears
     * k times, it contributes k * (k - 1) / 2 unordered pairs.
     *
     * @param receipts a list of receipts; each receipt is a list of line items,
     *                 and each line item is represented as a String array of length 2:
     *                 [category, amountAsString]
     * @return the number of unordered equivalent receipt pairs as a long
     *
     * Time complexity:
     * O(T + sum over receipts of U log U), where T is the total number of line items
     * and U is the number of distinct categories in a receipt. Sorting is needed to
     * build a canonical order for the signature.
     *
     * Space complexity:
     * O(T) in the worst case for temporary aggregation maps and the global signature count map.
     */
    public long countEquivalentReceiptPairs(List<List<String[]>> receipts) {
        // This map stores:
        // key   = canonical signature of one aggregated receipt
        // value = how many receipts seen so far have exactly that signature
        Map<String, Long> signatureFrequency = new HashMap<>();

        // We will process each receipt independently.
        for (List<String[]> receipt : receipts) {
            // Step 1:
            // Aggregate repeated categories inside this single receipt.
            //
            // Example:
            // [["food","200"],["book","500"],["food","300"]]
            // becomes:
            // {"food"=500, "book"=500}
            Map<String, Long> categoryTotals = aggregateReceipt(receipt);

            // Step 2:
            // Convert the aggregated map into a canonical signature.
            //
            // Why canonical?
            // Because HashMap iteration order is not guaranteed, and the original
            // line item order should not matter.
            //
            // So we sort categories and build a stable string like:
            // "book#500|food#500|"
            String signature = buildCanonicalSignature(categoryTotals);

            // Step 3:
            // Count how many times this exact signature has appeared.
            signatureFrequency.put(signature, signatureFrequency.getOrDefault(signature, 0L) + 1L);
        }

        // Step 4:
        // For each signature that appeared k times, the number of unordered pairs is:
        // k choose 2 = k * (k - 1) / 2
        long pairs = 0L;
        for (long count : signatureFrequency.values()) {
            pairs += count * (count - 1L) / 2L;
        }

        return pairs;
    }

    /**
     * Aggregates one receipt by summing amounts for repeated categories.
     *
     * @param receipt one receipt represented as a list of line items;
     *                each line item is a String array [category, amountAsString]
     * @return a map from category to total amount for that receipt
     *
     * Time complexity:
     * O(m), where m is the number of line items in the receipt.
     *
     * Space complexity:
     * O(u), where u is the number of distinct categories in the receipt.
     */
    public Map<String, Long> aggregateReceipt(List<String[]> receipt) {
        // This map will hold the merged totals for the current receipt only.
        Map<String, Long> totals = new HashMap<>();

        // Visit every line item and add its amount into the correct category bucket.
        for (String[] item : receipt) {
            // item[0] = category
            // item[1] = amount as string
            String category = item[0];
            long amount = Long.parseLong(item[1]);

            // Merge the amount into the running total for this category.
            totals.put(category, totals.getOrDefault(category, 0L) + amount);
        }

        return totals;
    }

    /**
     * Builds a canonical signature string for an aggregated receipt map.
     *
     * The signature must be identical for logically equivalent receipts.
     * To guarantee that, we:
     * 1. Extract all categories
     * 2. Sort them lexicographically
     * 3. Append each category and its total in sorted order
     *
     * Example:
     * {"food"=500, "book"=500} -> "book#500|food#500|"
     *
     * @param categoryTotals aggregated category-to-total mapping for one receipt
     * @return a canonical string signature for hashing and equality comparison
     *
     * Time complexity:
     * O(u log u), where u is the number of distinct categories in the receipt,
     * due to sorting the category names.
     *
     * Space complexity:
     * O(u) for the list of keys and the resulting signature builder output.
     */
    public String buildCanonicalSignature(Map<String, Long> categoryTotals) {
        // Extract all category names so we can sort them.
        List<String> categories = new ArrayList<>(categoryTotals.keySet());

        // Sorting is the key step that removes any dependence on insertion order.
        Collections.sort(categories);

        // Build the signature carefully.
        //
        // We use separators so that different mappings cannot accidentally produce
        // the same concatenated text.
        //
        // Example:
        // category "ab", total 12  -> "ab#12|"
        // category "a", total 212  -> "a#212|"
        //
        // These remain distinct because of the separators.
        StringBuilder signature = new StringBuilder();

        for (String category : categories) {
            signature.append(category)
                     .append('#')
                     .append(categoryTotals.get(category))
                     .append('|');
        }

        return signature.toString();
    }

    /**
     * Convenience helper that converts a 3D Object array into the input structure
     * expected by the main algorithm.
     *
     * Each line item must be represented as:
     * new Object[] { "category", amountInteger }
     *
     * This method is only used for demonstration in main.
     *
     * @param rawReceipts raw nested array representation of receipts
     * @return a List-based structure suitable for countEquivalentReceiptPairs
     *
     * Time complexity:
     * O(T), where T is the total number of line items.
     *
     * Space complexity:
     * O(T), for the constructed list structure.
     */
    public List<List<String[]>> buildReceipts(Object[][][] rawReceipts) {
        List<List<String[]>> receipts = new ArrayList<>();

        for (Object[][] rawReceipt : rawReceipts) {
            List<String[]> receipt = new ArrayList<>();

            for (Object[] rawItem : rawReceipt) {
                String category = (String) rawItem[0];
                String amount = String.valueOf(rawItem[1]);
                receipt.add(new String[]{category, amount});
            }

            receipts.add(receipt);
        }

        return receipts;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     *
     * Time complexity:
     * O(1) for the fixed demonstration size, excluding the algorithm calls themselves.
     *
     * Space complexity:
     * O(1) auxiliary space for demonstration setup, excluding input storage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        Object[][][] rawReceipts1 = new Object[][][]{
            {
                {"food", 200},
                {"book", 500},
                {"food", 300}
            },
            {
                {"book", 500},
                {"food", 500}
            },
            {
                {"food", 200},
                {"book", 400}
            },
            {
                {"book", 500},
                {"food", 500},
                {"toy", 100}
            }
        };

        List<List<String[]>> receipts1 = solution.buildReceipts(rawReceipts1);
        long result1 = solution.countEquivalentReceiptPairs(receipts1);
        System.out.println(result1); // Expected: 1

        // Example 2
        Object[][][] rawReceipts2 = new Object[][][]{
            {
                {"a", 10},
                {"b", 5},
                {"a", 5}
            },
            {
                {"b", 5},
                {"a", 15}
            },
            {
                {"a", 15}
            },
            {
                {"c", 7}
            },
            {
                {"c", 7}
            },
            {
                {"b", 5},
                {"a", 15}
            }
        };

        List<List<String[]>> receipts2 = solution.buildReceipts(rawReceipts2);
        long result2 = solution.countEquivalentReceiptPairs(receipts2);
        System.out.println(result2); // Expected: 4
    }
}