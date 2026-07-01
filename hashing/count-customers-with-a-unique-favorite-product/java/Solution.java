import java.util.*;

/*
 * Title: Count Customers with a Unique Favorite Product
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of customer purchase preferences. Each element in the list
 * represents the favorite product chosen by one customer, identified by a string
 * product code. A product is considered unique if exactly one customer selected it
 * as their favorite.
 *
 * Your task is to return the number of customers whose favorite product is unique
 * across the entire list.
 *
 * In other words, count how many entries in the array belong to product codes that
 * appear exactly once. This is a common analytics task when identifying niche
 * product preferences that are not shared by other customers.
 *
 * Implement a function that takes an array of strings favorites and returns an integer.
 *
 * Constraints:
 * - 1 <= favorites.length <= 100000
 * - 1 <= favorites[i].length <= 50
 * - favorites[i] consists of lowercase English letters, digits, and underscores
 * - The answer should be computed in O(n) time on average using hashing
 *
 * Example 1:
 * Input: favorites = ["phone_case", "charger", "phone_case", "notebook", "pen"]
 * Output: 3
 * Explanation: "charger", "notebook", and "pen" each appear exactly once, so there are
 * 3 customers whose favorite product is unique.
 *
 * Example 2:
 * Input: favorites = ["mouse", "mouse", "keyboard", "keyboard", "monitor"]
 * Output: 1
 * Explanation: Only "monitor" appears once. All other product codes appear more than once.
 *
 * Approach:
 * Use a hash map to count how many times each product code appears.
 * Then count how many product codes have frequency exactly 1.
 * Since each unique product appears in exactly one customer's entry, the number of
 * unique products is the same as the number of customers whose favorite product is unique.
 */

public class Solution {

    /**
     * Counts how many customers have a favorite product code that appears exactly once
     * in the entire input array.
     *
     * The method works in two clear phases:
     * 1. Build a frequency map where each product code is mapped to its number of appearances.
     * 2. Scan the original array again and count how many entries have frequency 1.
     *
     * Counting by scanning the original array is beginner-friendly because it directly answers:
     * "For this customer's favorite product, is it unique?"
     *
     * @param favorites the array of favorite product codes, where each element represents one customer's choice
     * @return the number of customers whose favorite product code appears exactly once in the array
     *
     * Time complexity: O(n) on average, where n is the length of the favorites array,
     * because hash map insertions and lookups are O(1) on average.
     * Space complexity: O(n) in the worst case, if all product codes are different.
     */
    public int countUniqueFavoriteCustomers(String[] favorites) {
        // Create a hash map to store frequency counts.
        // Key   -> product code
        // Value -> number of times that product code appears in the array
        Map<String, Integer> frequencyMap = new HashMap<>();

        // -----------------------------
        // First pass: count frequencies
        // -----------------------------
        // We visit every customer's favorite product exactly once.
        // For each product code:
        // - If it has not been seen before, start its count at 1.
        // - If it has been seen before, increase its count by 1.
        for (String product : favorites) {
            frequencyMap.put(product, frequencyMap.getOrDefault(product, 0) + 1);
        }

        // -----------------------------------------
        // Second pass: count entries with count == 1
        // -----------------------------------------
        // Now that we know the total frequency of every product code,
        // we can determine whether each customer's favorite product is unique.
        int uniqueCustomerCount = 0;

        for (String product : favorites) {
            // If this product appears exactly once in the whole array,
            // then this customer should be counted.
            if (frequencyMap.get(product) == 1) {
                uniqueCustomerCount++;
            }
        }

        // Return the total number of customers whose favorite product is unique.
        return uniqueCustomerCount;
    }

    /**
     * Alternative implementation that counts unique product codes directly from the frequency map.
     *
     * This also produces the correct answer because each product code with frequency 1
     * corresponds to exactly one customer.
     *
     * @param favorites the array of favorite product codes
     * @return the number of product codes that appear exactly once, which is equal to the
     * number of customers whose favorite product is unique
     *
     * Time complexity: O(n) on average.
     * Space complexity: O(n) in the worst case.
     */
    public int countUniqueFavoriteCustomersFromMap(String[] favorites) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        // Build the frequency map exactly as in the main solution.
        for (String product : favorites) {
            frequencyMap.put(product, frequencyMap.getOrDefault(product, 0) + 1);
        }

        int uniqueCount = 0;

        // Iterate through the frequencies of all distinct product codes.
        // Every product code with frequency 1 contributes exactly one customer.
        for (int count : frequencyMap.values()) {
            if (count == 1) {
                uniqueCount++;
            }
        }

        return uniqueCount;
    }

    /**
     * Prints an array of strings in a readable format.
     *
     * @param favorites the array to print
     * @return a formatted string representation of the array
     *
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(String[] favorites) {
        return Arrays.toString(favorites);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified examples:
     * Example 1:
     * ["phone_case", "charger", "phone_case", "notebook", "pen"]
     * Frequencies:
     * - phone_case -> 2
     * - charger    -> 1
     * - notebook   -> 1
     * - pen        -> 1
     * Unique customers = 3
     *
     * Example 2:
     * ["mouse", "mouse", "keyboard", "keyboard", "monitor"]
     * Frequencies:
     * - mouse    -> 2
     * - keyboard -> 2
     * - monitor  -> 1
     * Unique customers = 1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(n) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] favorites1 = {"phone_case", "charger", "phone_case", "notebook", "pen"};
        int result1 = solution.countUniqueFavoriteCustomers(favorites1);
        System.out.println("Input: " + solution.arrayToString(favorites1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 3");
        System.out.println();

        String[] favorites2 = {"mouse", "mouse", "keyboard", "keyboard", "monitor"};
        int result2 = solution.countUniqueFavoriteCustomers(favorites2);
        System.out.println("Input: " + solution.arrayToString(favorites2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 1");
        System.out.println();

        String[] favorites3 = {"a"};
        int result3 = solution.countUniqueFavoriteCustomers(favorites3);
        System.out.println("Input: " + solution.arrayToString(favorites3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        String[] favorites4 = {"x", "x", "y", "z", "z", "w"};
        int result4 = solution.countUniqueFavoriteCustomers(favorites4);
        System.out.println("Input: " + solution.arrayToString(favorites4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 2");
    }
}