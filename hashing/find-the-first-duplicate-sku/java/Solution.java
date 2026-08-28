import java.util.*;

/*
 * Title: Find the First Duplicate SKU
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of product SKU codes representing items scanned at a warehouse receiving station,
 * in the exact order they were scanned. A SKU code is a string containing letters, digits, or hyphens.
 * Your task is to return the first SKU that appears more than once in the scan history.
 *
 * The phrase first duplicate means the duplicate whose second appearance happens earliest in the list.
 * In other words, scan the list from left to right and return the first SKU that has already been seen before.
 * If no SKU appears twice, return an empty string.
 *
 * This problem is useful for detecting the earliest repeated item in a real-time stream of inventory events.
 * An efficient solution should avoid comparing every pair of strings and should instead use a hash-based
 * structure to track which SKUs have already appeared.
 *
 * Constraints:
 * - 1 <= skus.length <= 100000
 * - 1 <= skus[i].length <= 50
 * - Each skus[i] consists of English letters, digits, and '-' only
 * - Comparison is case-sensitive, so "ab-1" and "AB-1" are different
 *
 * Example 1:
 * Input: skus = ["BX-12", "A7", "Q9", "A7", "BX-12"]
 * Output: "A7"
 * Explanation: "A7" is the first SKU whose second occurrence is encountered while scanning from left to right.
 *
 * Example 2:
 * Input: skus = ["P1", "R2", "S3", "T4"]
 * Output: ""
 * Explanation: No SKU is repeated, so return an empty string.
 */

public class Solution {

    /**
     * Finds the first duplicate SKU in the order the SKUs are scanned.
     *
     * The key idea is:
     * - Keep a hash set of all SKU codes we have already seen.
     * - Scan the array from left to right.
     * - For each SKU:
     *   - If it is already in the set, then this is the first duplicate whose
     *     second appearance is encountered earliest, so return it immediately.
     *   - Otherwise, add it to the set and continue.
     *
     * This works because we process the scan history in exact order, and the first
     * time we encounter a repeated SKU, that repeated SKU is exactly the answer.
     *
     * @param skus the array of scanned SKU codes in the exact order they were scanned
     * @return the first SKU whose second appearance occurs earliest; returns an empty string if no duplicate exists
     * Time complexity: O(n), where n is the number of SKU codes, because each lookup/add in the hash set is O(1) on average
     * Space complexity: O(n), because in the worst case we may store all SKU codes in the hash set
     */
    public String findFirstDuplicateSKU(String[] skus) {
        // Create a HashSet to remember every SKU we have already seen.
        // Why a HashSet?
        // Because it gives us very fast average-time operations:
        // - contains(...) to check whether a SKU was seen before
        // - add(...) to record a newly seen SKU
        Set<String> seen = new HashSet<>();

        // Go through the SKU list one by one, from left to right.
        // This order is extremely important because the problem asks for the duplicate
        // whose SECOND appearance happens earliest.
        for (String sku : skus) {
            // Step 1:
            // Check whether this SKU has already been seen earlier in the scan history.
            if (seen.contains(sku)) {
                // If yes, then this is the first moment we have found a repeated SKU
                // while scanning from left to right.
                //
                // That means this SKU is the correct answer immediately.
                //
                // Example:
                // ["BX-12", "A7", "Q9", "A7", "BX-12"]
                // Seen after first three items: {"BX-12", "A7", "Q9"}
                // Next item is "A7"
                // "A7" is already in seen, so return "A7"
                return sku;
            }

            // Step 2:
            // If the SKU was not seen before, add it to the set so future occurrences
            // can be recognized as duplicates.
            seen.add(sku);
        }

        // If we finish the entire scan without finding any repeated SKU,
        // then there is no duplicate at all.
        return "";
    }

    /**
     * Helper method to print an array of SKU strings in a readable format.
     *
     * @param skus the array of SKU codes to print
     * @return a string representation of the SKU array
     * Time complexity: O(n), where n is the number of SKU codes
     * Space complexity: O(n), due to building the output string
     */
    public String arrayToString(String[] skus) {
        return Arrays.toString(skus);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Also includes a couple of extra examples to help beginners verify behavior.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per test case, where n is the size of that test array
     * Space complexity: O(n) per test case, due to the hash set used by the algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1 from the problem statement
        String[] skus1 = {"BX-12", "A7", "Q9", "A7", "BX-12"};
        String result1 = solution.findFirstDuplicateSKU(skus1);
        System.out.println("Input:  " + solution.arrayToString(skus1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: A7");
        System.out.println();

        // Sample Input 2 from the problem statement
        String[] skus2 = {"P1", "R2", "S3", "T4"};
        String result2 = solution.findFirstDuplicateSKU(skus2);
        System.out.println("Input:  " + solution.arrayToString(skus2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: ");
        System.out.println();

        // Extra Example 3:
        // The first duplicate encountered is "X-1" when it appears the second time.
        String[] skus3 = {"X-1", "Y-2", "Z-3", "X-1", "Y-2"};
        String result3 = solution.findFirstDuplicateSKU(skus3);
        System.out.println("Input:  " + solution.arrayToString(skus3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: X-1");
        System.out.println();

        // Extra Example 4:
        // Case-sensitive comparison: "ab-1" and "AB-1" are different.
        // The first actual duplicate is the second "ab-1".
        String[] skus4 = {"ab-1", "AB-1", "ab-1"};
        String result4 = solution.findFirstDuplicateSKU(skus4);
        System.out.println("Input:  " + solution.arrayToString(skus4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: ab-1");
    }
}