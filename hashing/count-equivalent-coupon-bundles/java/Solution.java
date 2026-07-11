import java.util.*;

/*
Title: Count Equivalent Coupon Bundles
Difficulty: Medium
Topic: Hashing

Problem Description:
An e-commerce platform stores promotional bundles as arrays of coupon codes. Two bundles are considered equivalent if they contain exactly the same coupon codes with the same frequencies, regardless of order. For example, ["SAVE10", "FREESHIP", "SAVE10"] is equivalent to ["SAVE10", "SAVE10", "FREESHIP"], but not to ["SAVE10", "FREESHIP"] or ["SAVE10", "FREESHIP", "BONUS"].

You are given a list of bundles, where each bundle is an array of strings. Return the number of unordered pairs of indices (i, j) such that i < j and bundles[i] is equivalent to bundles[j].

Because coupon codes are strings and bundle order does not matter, a direct comparison of every pair is too slow for large inputs. You should design an efficient solution using hashing or canonical representations.

Constraints:
- 1 <= bundles.length <= 100000
- 1 <= bundles[i].length <= 20
- 1 <= couponCodes[i].length <= 20
- Each coupon code consists of uppercase English letters and digits.
- The total number of coupon codes across all bundles does not exceed 200000.

Example 1:
Input: bundles = [["SAVE10","FREESHIP"],["FREESHIP","SAVE10"],["BONUS"],["SAVE10","SAVE10","FREESHIP"],["FREESHIP","SAVE10","SAVE10"]]
Output: 2
Explanation: Bundles 0 and 1 are equivalent. Bundles 3 and 4 are equivalent. No other pair matches.

Example 2:
Input: bundles = [["A","B","A"],["A","A","B"],["B","A"],["C"],["C"],["C","C"]]
Output: 2
Explanation: One matching pair comes from the first two bundles. Another matching pair comes from the two single-element ["C"] bundles.

Task:
Count all equivalent bundle pairs efficiently.
*/
public class Solution {

    /**
     * Counts how many unordered pairs of bundles are equivalent.
     *
     * The key idea is:
     * 1. Convert each bundle into a canonical representation that is identical
     *    for all equivalent bundles.
     * 2. Count how many times each canonical representation appears.
     * 3. If a representation appears k times, then it contributes k * (k - 1) / 2 pairs.
     *
     * We build the canonical representation by:
     * - copying the bundle,
     * - sorting the coupon codes,
     * - joining them into a single string with separators.
     *
     * Sorting works because equivalent bundles must contain the same elements
     * with the same multiplicities. After sorting, equivalent bundles become
     * exactly the same ordered sequence.
     *
     * @param bundles the list of coupon bundles, where each bundle is an array of strings
     * @return the number of unordered equivalent pairs
     * Time complexity: O(T log M), where T is the total number of coupon codes across all bundles
     * and M is the maximum bundle size (at most 20). More precisely, each bundle of size k costs
     * O(k log k) to sort, and the total across all bundles is efficient under the constraints.
     * Space complexity: O(N + T) for the hash map and canonical keys, where N is the number of bundles
     */
    public long countEquivalentBundles(String[][] bundles) {
        // This map stores:
        // canonicalBundleRepresentation -> how many times we have seen it so far
        Map<String, Long> frequencyMap = new HashMap<>();

        // We will process each bundle one by one.
        for (String[] bundle : bundles) {
            // Convert the current bundle into a canonical form.
            // Any two equivalent bundles will produce the exact same key.
            String key = buildCanonicalKey(bundle);

            // Increase the count for this canonical representation.
            frequencyMap.put(key, frequencyMap.getOrDefault(key, 0L) + 1L);
        }

        // Now compute the number of unordered pairs.
        // If a key appears count times, then the number of pairs is:
        // count choose 2 = count * (count - 1) / 2
        long pairs = 0L;
        for (long count : frequencyMap.values()) {
            pairs += count * (count - 1L) / 2L;
        }

        return pairs;
    }

    /**
     * Builds a canonical string key for a bundle.
     *
     * Why this works:
     * - Order does not matter, so we sort the coupon codes.
     * - Frequency does matter, so duplicates remain present after sorting.
     * - After sorting, two bundles are equivalent if and only if their sorted sequences match exactly.
     *
     * We also use explicit separators and lengths to make the representation safe and unambiguous.
     * For example, without careful separators, strings like ["AB", "C"] and ["A", "BC"]
     * could accidentally collide if simply concatenated.
     *
     * A safe format for each token is:
     * length + "#" + token + "|"
     *
     * Example:
     * ["SAVE10", "FREESHIP", "SAVE10"]
     * sorted -> ["FREESHIP", "SAVE10", "SAVE10"]
     * key -> "8#FREESHIP|6#SAVE10|6#SAVE10|"
     *
     * @param bundle one coupon bundle
     * @return a canonical representation of the bundle
     * Time complexity: O(k log k), where k is the number of coupon codes in the bundle
     * Space complexity: O(k + L), where L is the total length of strings in the bundle
     */
    public String buildCanonicalKey(String[] bundle) {
        // We do not want to modify the original input array,
        // so we create a copy before sorting.
        String[] copy = Arrays.copyOf(bundle, bundle.length);

        // Sort coupon codes lexicographically.
        // After this step, equivalent bundles will have identical ordering.
        Arrays.sort(copy);

        // Build a safe string representation.
        StringBuilder sb = new StringBuilder();

        // Append each sorted coupon code with its length and separators.
        for (String code : copy) {
            sb.append(code.length()).append('#').append(code).append('|');
        }

        return sb.toString();
    }

    /**
     * Utility method to print a 2D string array in a readable format.
     *
     * @param bundles the bundles to print
     * @return a human-readable string representation
     * Time complexity: O(T), where T is the total number of coupon codes printed
     * Space complexity: O(T) for the generated string
     */
    public String bundlesToString(String[][] bundles) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < bundles.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(bundles[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * This main method also implicitly verifies correctness for the provided examples:
     *
     * Example 1:
     * - ["SAVE10","FREESHIP"] and ["FREESHIP","SAVE10"] match -> 1 pair
     * - ["SAVE10","SAVE10","FREESHIP"] and ["FREESHIP","SAVE10","SAVE10"] match -> 1 pair
     * Total = 2
     *
     * Example 2:
     * - ["A","B","A"] and ["A","A","B"] match -> 1 pair
     * - ["C"] and ["C"] match -> 1 pair
     * Total = 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(T log M) across the demonstrated test cases
     * Space complexity: O(N + T) across the demonstrated test cases
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[][] bundles1 = {
            {"SAVE10", "FREESHIP"},
            {"FREESHIP", "SAVE10"},
            {"BONUS"},
            {"SAVE10", "SAVE10", "FREESHIP"},
            {"FREESHIP", "SAVE10", "SAVE10"}
        };

        long result1 = solution.countEquivalentBundles(bundles1);
        System.out.println("Example 1 Input: " + solution.bundlesToString(bundles1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        String[][] bundles2 = {
            {"A", "B", "A"},
            {"A", "A", "B"},
            {"B", "A"},
            {"C"},
            {"C"},
            {"C", "C"}
        };

        long result2 = solution.countEquivalentBundles(bundles2);
        System.out.println("Example 2 Input: " + solution.bundlesToString(bundles2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: 2");
        System.out.println();

        String[][] extraBundles = {
            {"X"},
            {"Y"},
            {"X"},
            {"X", "Y"},
            {"Y", "X"},
            {"X", "X"},
            {"X", "X"}
        };

        long extraResult = solution.countEquivalentBundles(extraBundles);
        System.out.println("Extra Example Input: " + solution.bundlesToString(extraBundles));
        System.out.println("Extra Example Output: " + extraResult);
        System.out.println("Expected: 3");
    }
}