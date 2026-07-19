import java.util.*;

/*
 * Title: Count Products With a Unique Reviewer Set
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * An e-commerce platform stores product reviews as pairs of integers [productId, userId].
 * A product may be reviewed multiple times by the same user due to edits or updated ratings,
 * but for this problem only the set of distinct users who reviewed each product matters.
 * Two products are considered equivalent if they were reviewed by exactly the same set of users,
 * regardless of review order or duplicate entries.
 *
 * Your task is to return the number of products whose reviewer set is unique among all products
 * that appear in the input. In other words, build the distinct reviewer set for every product,
 * group products by these sets, and count how many products belong to a group of size 1.
 *
 * A product should be considered only if it appears in at least one review record.
 * Duplicate review records for the same (productId, userId) pair do not change the reviewer set.
 *
 * Constraints:
 * - 1 <= reviews.length <= 2 * 10^5
 * - 1 <= productId, userId <= 10^9
 * - Each review is a pair [productId, userId]
 * - The answer must be computed in near-linear time relative to the number of review records
 *
 * Example 1:
 * Input: reviews = [[101,1],[101,2],[102,2],[102,1],[103,3],[103,3],[104,4]]
 * Output: 2
 * Explanation:
 * Product 101 has reviewer set {1,2}
 * Product 102 also has reviewer set {1,2}
 * Product 103 has reviewer set {3}
 * Product 104 has reviewer set {4}
 * Only products 103 and 104 have reviewer sets that no other product has.
 *
 * Example 2:
 * Input: reviews = [[10,7],[10,8],[11,7],[12,8],[12,8],[13,9],[13,10],[14,9],[14,10]]
 * Output: 3
 * Explanation:
 * Product 10 has reviewer set {7,8}
 * Product 11 has reviewer set {7}
 * Product 12 has reviewer set {8}
 * Product 13 has reviewer set {9,10}
 * Product 14 has reviewer set {9,10}
 * Unique reviewer-set groups are:
 * - {7,8}  -> product 10 only
 * - {7}    -> product 11 only
 * - {8}    -> product 12 only
 * Group {9,10} contains products 13 and 14, so those are not counted.
 * Therefore the correct answer is 3.
 */

public class Solution {

    /**
     * Counts how many products have a reviewer set that is unique among all products in the input.
     *
     * The algorithm works in two major phases:
     * 1. Build the distinct reviewer set for each product.
     * 2. Convert each product's reviewer set into a canonical representation and count how many
     *    products share that exact representation.
     * 3. Count how many products belong to a representation that appears exactly once.
     *
     * Important correctness details:
     * - Duplicate review records for the same (productId, userId) pair are ignored naturally
     *   by storing reviewers in a Set.
     * - Review order does not matter because we sort the distinct reviewer IDs before building
     *   the canonical key.
     * - Two products are considered equivalent if and only if their sorted distinct reviewer lists
     *   are identical.
     *
     * @param reviews a 2D array where each element is [productId, userId]
     * @return the number of products whose reviewer set is unique among all products
     * @implNote Time complexity: O(n + totalDistinctReviewers * log(totalDistinctReviewersPerProduct)) in total,
     * where n is the number of review records. More precisely, if product i has k_i distinct reviewers,
     * sorting contributes sum of O(k_i log k_i). This is efficient for the given constraints.
     * @implNote Space complexity: O(totalDistinctPairs + numberOfProducts), due to storing reviewer sets
     * for products and canonical keys.
     */
    public int countProductsWithUniqueReviewerSet(int[][] reviews) {
        // Step 1:
        // Build a mapping from productId -> set of distinct userIds who reviewed that product.
        //
        // Why a Set?
        // Because the problem explicitly says duplicate reviews by the same user for the same product
        // should not change the reviewer set. A HashSet removes duplicates automatically.
        Map<Integer, Set<Integer>> productToReviewers = new HashMap<>();

        for (int[] review : reviews) {
            int productId = review[0];
            int userId = review[1];

            // If this product has not been seen before, create an empty reviewer set for it.
            productToReviewers.computeIfAbsent(productId, k -> new HashSet<>()).add(userId);
        }

        // Step 2:
        // For each product, convert its reviewer set into a canonical form.
        //
        // We cannot directly rely on HashSet's iteration order because it is not stable and not sorted.
        // Two equal sets may iterate in different orders, so we must sort the reviewer IDs.
        //
        // Example:
        // Product A reviewers: {2, 1}
        // Product B reviewers: {1, 2}
        // After sorting both become [1, 2], which gives the same canonical key.
        //
        // We then count how many products share each canonical reviewer-set key.
        Map<String, Integer> reviewerSetFrequency = new HashMap<>();

        // Also store each product's canonical key so we can later determine whether that product
        // belongs to a unique group.
        Map<Integer, String> productToCanonicalKey = new HashMap<>();

        for (Map.Entry<Integer, Set<Integer>> entry : productToReviewers.entrySet()) {
            int productId = entry.getKey();
            Set<Integer> reviewerSet = entry.getValue();

            String canonicalKey = buildCanonicalKey(reviewerSet);

            productToCanonicalKey.put(productId, canonicalKey);
            reviewerSetFrequency.put(canonicalKey, reviewerSetFrequency.getOrDefault(canonicalKey, 0) + 1);
        }

        // Step 3:
        // Count how many products belong to a reviewer-set group of size exactly 1.
        //
        // If a canonical key appears once, that means exactly one product has that reviewer set,
        // so that product should be counted.
        int uniqueProductsCount = 0;

        for (String canonicalKey : productToCanonicalKey.values()) {
            if (reviewerSetFrequency.get(canonicalKey) == 1) {
                uniqueProductsCount++;
            }
        }

        return uniqueProductsCount;
    }

    /**
     * Builds a canonical string representation for a set of reviewer IDs.
     *
     * The canonical representation must satisfy:
     * - Equal reviewer sets produce exactly the same string.
     * - Different reviewer sets produce different strings.
     *
     * To achieve this:
     * 1. Copy the set into a list.
     * 2. Sort the list in ascending order.
     * 3. Join the numbers with a delimiter that avoids ambiguity.
     *
     * Example:
     * - Set {2, 1} becomes "1#2#"
     * - Set {3} becomes "3#"
     *
     * The trailing delimiter is harmless and keeps the construction simple.
     *
     * @param reviewerSet the distinct reviewers of one product
     * @return a deterministic string key representing the reviewer set
     * @implNote Time complexity: O(k log k), where k is the number of distinct reviewers in the set
     * @implNote Space complexity: O(k), for the temporary list and output builder
     */
    public String buildCanonicalKey(Set<Integer> reviewerSet) {
        // Copy set elements into a list so we can sort them.
        List<Integer> sortedReviewers = new ArrayList<>(reviewerSet);

        // Sorting is the key step that removes dependence on insertion order or hash iteration order.
        Collections.sort(sortedReviewers);

        // Build a compact canonical string.
        // We use '#' as a separator to avoid ambiguity:
        // [1, 23] -> "1#23#"
        // [12, 3] -> "12#3#"
        // These are clearly different.
        StringBuilder keyBuilder = new StringBuilder();

        for (int userId : sortedReviewers) {
            keyBuilder.append(userId).append('#');
        }

        return keyBuilder.toString();
    }

    /**
     * Utility method to print a 2D review array in a readable format.
     *
     * @param reviews the review pairs to print
     * @return a string representation of the 2D array
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n), due to the generated string
     */
    public String reviewsToString(int[][] reviews) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        for (int i = 0; i < reviews.length; i++) {
            sb.append(Arrays.toString(reviews[i]));
            if (i + 1 < reviews.length) {
                sb.append(',');
            }
        }

        sb.append(']');
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * This method also verifies the expected outputs by printing both the computed result
     * and the expected result for easy comparison.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * @implNote Time complexity: O(total input size of the demo cases)
     * @implNote Space complexity: O(total input size of the demo cases)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] reviews1 = {
            {101, 1},
            {101, 2},
            {102, 2},
            {102, 1},
            {103, 3},
            {103, 3},
            {104, 4}
        };

        int[][] reviews2 = {
            {10, 7},
            {10, 8},
            {11, 7},
            {12, 8},
            {12, 8},
            {13, 9},
            {13, 10},
            {14, 9},
            {14, 10}
        };

        int result1 = solution.countProductsWithUniqueReviewerSet(reviews1);
        int result2 = solution.countProductsWithUniqueReviewerSet(reviews2);

        System.out.println("Example 1 Input:  " + solution.reviewsToString(reviews1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        System.out.println("Example 2 Input:  " + solution.reviewsToString(reviews2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional small sanity check:
        // Product 1 -> {5}
        // Product 2 -> {5}
        // Product 3 -> {6}
        // Only product 3 is unique, so answer should be 1.
        int[][] reviews3 = {
            {1, 5},
            {1, 5},
            {2, 5},
            {3, 6}
        };

        int result3 = solution.countProductsWithUniqueReviewerSet(reviews3);

        System.out.println("Additional Test Input:  " + solution.reviewsToString(reviews3));
        System.out.println("Additional Test Output: " + result3);
        System.out.println("Expected: 1");
    }
}