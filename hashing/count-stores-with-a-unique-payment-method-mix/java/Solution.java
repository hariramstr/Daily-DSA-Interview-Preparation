import java.util.*;

/*
 * Title: Count Stores With a Unique Payment Method Mix
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * A retail analytics system records, for each store, the payment methods used during a day.
 * Each store is represented by a list of method names such as "cash", "card", "wallet", or "gift".
 * The same method may appear multiple times for a store because many customers can use it,
 * but for this task only the set of distinct methods matters.
 *
 * Two stores are considered to have the same payment method mix if the set of distinct method names
 * used at both stores is identical, regardless of order or repetition.
 * For example, ["cash", "card", "cash"] and ["card", "cash"] represent the same mix.
 *
 * Given a 2D array methodsUsed where methodsUsed[i] is the list of payment methods recorded for store i,
 * return the number of stores whose payment method mix is unique across all stores.
 * In other words, count how many stores belong to a distinct-method set that appears exactly once.
 *
 * You should design an efficient solution using hashing. A common approach is to normalize each store's
 * method list into a canonical representation of its distinct methods, then count how many times each
 * normalized signature appears.
 *
 * Constraints:
 * - 1 <= methodsUsed.length <= 100000
 * - 0 <= methodsUsed[i].length <= 100
 * - 1 <= total number of method entries across all stores <= 200000
 * - Each method name consists of lowercase English letters and has length from 1 to 20
 *
 * Example 1:
 * Input:
 * methodsUsed = [
 *   ["cash","card","cash"],
 *   ["wallet"],
 *   ["card","cash"],
 *   ["gift","wallet"],
 *   ["wallet","gift"],
 *   ["bank"]
 * ]
 * Output: 2
 * Explanation:
 * The normalized mixes are:
 *   {card,cash}
 *   {wallet}
 *   {card,cash}
 *   {gift,wallet}
 *   {gift,wallet}
 *   {bank}
 * Only {wallet} and {bank} appear exactly once, so the answer is 2.
 *
 * Example 2:
 * Input:
 * methodsUsed = [
 *   ["cash"],
 *   [],
 *   ["card","wallet"],
 *   ["wallet","card","wallet"],
 *   []
 * ]
 * Output: 1
 * Explanation:
 * The normalized mixes are:
 *   {cash}
 *   {}
 *   {card,wallet}
 *   {card,wallet}
 *   {}
 * Only {cash} appears exactly once, so the answer is 1.
 */

public class Solution {

    /**
     * Counts how many stores have a payment method mix that appears exactly once.
     *
     * The key idea is:
     * 1. For each store, reduce its list to the set of distinct method names.
     * 2. Convert that set into a canonical signature so that:
     *    - duplicates are removed
     *    - order does not matter
     * 3. Count how many times each signature appears across all stores.
     * 4. Count how many stores belong to a signature with frequency exactly 1.
     *
     * @param methodsUsed a 2D array where methodsUsed[i] contains the payment methods recorded for store i
     * @return the number of stores whose distinct payment method set appears exactly once
     * Time complexity: O(T + sum(k_i log k_i)), where T is the total number of method entries and k_i is
     * the number of distinct methods for store i. Since each store has at most 100 entries, this is efficient.
     * Space complexity: O(T) in the worst case for storing signatures and frequency counts.
     */
    public int countUniquePaymentMethodMixes(String[][] methodsUsed) {
        // This map stores:
        //   key   = canonical signature of a store's distinct payment methods
        //   value = how many stores have exactly that signature
        Map<String, Integer> signatureFrequency = new HashMap<>();

        // We also keep the signature for each store so that after counting frequencies,
        // we can easily determine whether that specific store belongs to a unique signature.
        String[] storeSignatures = new String[methodsUsed.length];

        // -------------------------------
        // First pass: build signatures and count them
        // -------------------------------
        for (int i = 0; i < methodsUsed.length; i++) {
            // Convert the current store's method list into a canonical normalized signature.
            String signature = buildCanonicalSignature(methodsUsed[i]);

            // Save the signature for later use in the second pass.
            storeSignatures[i] = signature;

            // Increase the frequency count for this signature.
            signatureFrequency.put(signature, signatureFrequency.getOrDefault(signature, 0) + 1);
        }

        // -------------------------------
        // Second pass: count stores whose signature appears exactly once
        // -------------------------------
        int uniqueStoreCount = 0;

        for (String signature : storeSignatures) {
            // If this signature occurs exactly once, then this store's payment method mix is unique.
            if (signatureFrequency.get(signature) == 1) {
                uniqueStoreCount++;
            }
        }

        return uniqueStoreCount;
    }

    /**
     * Builds a canonical string representation for one store's distinct payment methods.
     *
     * Why this works:
     * - We first remove duplicates using a HashSet.
     * - Then we sort the distinct method names so that order becomes consistent.
     * - Finally, we join them with a separator to produce a unique, repeatable signature.
     *
     * Examples:
     * - ["cash", "card", "cash"] -> "card|cash"
     * - ["card", "cash"]         -> "card|cash"
     * - []                       -> ""
     *
     * This ensures that two stores with the same distinct set produce exactly the same signature.
     *
     * @param methods the raw list of payment methods recorded for one store
     * @return a canonical signature representing the set of distinct methods for that store
     * Time complexity: O(m + d log d), where m is the number of entries in the store list and d is the
     * number of distinct methods in that list
     * Space complexity: O(d) for the set and sorted list of distinct methods
     */
    public String buildCanonicalSignature(String[] methods) {
        // Step 1:
        // Use a HashSet to remove duplicates.
        // If the same method appears many times, it will still exist only once in the set.
        Set<String> distinctMethods = new HashSet<>();

        for (String method : methods) {
            distinctMethods.add(method);
        }

        // Step 2:
        // Convert the set into a list so we can sort it.
        List<String> sortedMethods = new ArrayList<>(distinctMethods);

        // Step 3:
        // Sort alphabetically.
        // This is crucial because sets do not preserve order.
        // Without sorting, ["cash", "card"] and ["card", "cash"] might produce different outputs.
        Collections.sort(sortedMethods);

        // Step 4:
        // Join the sorted distinct methods into one string using a separator.
        // We use "|" because method names contain only lowercase English letters,
        // so "|" cannot conflict with valid method names.
        return String.join("|", sortedMethods);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration size, excluding the called algorithm
     * Space complexity: O(1) excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[][] methodsUsed1 = {
            {"cash", "card", "cash"},
            {"wallet"},
            {"card", "cash"},
            {"gift", "wallet"},
            {"wallet", "gift"},
            {"bank"}
        };

        int result1 = solution.countUniquePaymentMethodMixes(methodsUsed1);
        System.out.println("Example 1 Output: " + result1);
        // Expected: 2

        // Example 2
        String[][] methodsUsed2 = {
            {"cash"},
            {},
            {"card", "wallet"},
            {"wallet", "card", "wallet"},
            {}
        };

        int result2 = solution.countUniquePaymentMethodMixes(methodsUsed2);
        System.out.println("Example 2 Output: " + result2);
        // Expected: 1

        // Additional small sanity check
        String[][] methodsUsed3 = {
            {"cash", "cash"},
            {"cash"},
            {"card"},
            {"wallet", "gift"},
            {"gift", "wallet", "wallet"}
        };

        int result3 = solution.countUniquePaymentMethodMixes(methodsUsed3);
        System.out.println("Additional Example Output: " + result3);
        // Normalized mixes:
        // {cash}, {cash}, {card}, {gift,wallet}, {gift,wallet}
        // Only {card} is unique, so expected: 1
    }
}