import java.util.*;

/*
 * Title: Minimum Energy to Read a Shelf of Books
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given an array energy where energy[i] represents the energy cost required
 * to read book i on a shelf. You want to finish reading all books by reaching the last book.
 *
 * You start before the first book, and on each move you may read either the next book
 * or skip one book and read the book after that. In other words, if you are currently
 * at position i, your next position can be i + 1 or i + 2. When you read a book, you
 * must pay its energy cost. Your goal is to minimize the total energy spent to reach
 * the last book.
 *
 * Return the minimum total energy needed to finish at the last book.
 *
 * This is a dynamic programming problem because the best cost to reach a book depends
 * on the best costs to reach the previous one or two books.
 *
 * Constraints:
 * - 1 <= energy.length <= 1000
 * - 0 <= energy[i] <= 10^4
 * - You must end on the last book
 * - You begin before index 0, so your first read can be book 0 or book 1 if it exists
 *
 * Example 1:
 * Input: energy = [4, 2, 7, 3]
 * Output: 5
 * Explanation: Start by reading book 1 (cost 2), then read book 3 (cost 3). Total energy = 2 + 3 = 5.
 *
 * Example 2:
 * Input: energy = [5, 1, 2, 10, 1]
 * Output: 4
 * Explanation: One optimal path is to read book 1 (cost 1), book 2 (cost 2), and book 4 (cost 1). Total energy = 4.
 */

public class Solution {

    /**
     * Computes the minimum total energy needed to finish at the last book.
     *
     * The idea:
     * - Let dp[i] represent the minimum energy required to land on book i.
     * - To reach book i, we must come from:
     *   1) book i - 1
     *   2) book i - 2
     * - Therefore:
     *   dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])
     *
     * Base cases:
     * - If there is only one book, we must read it:
     *   dp[0] = energy[0]
     * - If there are at least two books:
     *   dp[1] = energy[1]
     *   because we are allowed to start before index 0 and directly read book 1.
     *
     * @param energy an array where energy[i] is the energy cost to read book i
     * @return the minimum total energy required to end exactly on the last book
     * Time complexity: O(n), where n is the number of books
     * Space complexity: O(n), due to the dynamic programming array
     */
    public int minEnergy(int[] energy) {
        // Defensive check for completeness.
        // The problem guarantees at least one element, but this makes the method safer.
        if (energy == null || energy.length == 0) {
            return 0;
        }

        int n = energy.length;

        // If there is only one book, there is no choice:
        // we must read book 0 and pay its energy cost.
        if (n == 1) {
            return energy[0];
        }

        // dp[i] will store the minimum energy needed to reach book i.
        int[] dp = new int[n];

        // Base case for the first book:
        // Starting before the shelf, we can choose to read book 0 first.
        dp[0] = energy[0];

        // Base case for the second book:
        // Since we may start by reading book 1 directly, the minimum cost to reach book 1
        // is simply energy[1], not energy[0] + energy[1].
        dp[1] = energy[1];

        // Fill the DP table from left to right.
        for (int i = 2; i < n; i++) {
            // To land on book i, we must come from either:
            // - book i - 1
            // - book i - 2
            //
            // We choose the cheaper of those two ways, then add the cost of reading book i.
            dp[i] = energy[i] + Math.min(dp[i - 1], dp[i - 2]);
        }

        // The answer is the minimum energy needed to land on the last book.
        return dp[n - 1];
    }

    /**
     * Computes the minimum total energy needed to finish at the last book
     * using constant extra space.
     *
     * This method is an optimized version of the DP solution.
     * Instead of storing the entire dp array, we only keep the last two values,
     * because each state depends only on the previous two states.
     *
     * @param energy an array where energy[i] is the energy cost to read book i
     * @return the minimum total energy required to end exactly on the last book
     * Time complexity: O(n), where n is the number of books
     * Space complexity: O(1), ignoring the input array
     */
    public int minEnergyOptimized(int[] energy) {
        // Defensive check for completeness.
        if (energy == null || energy.length == 0) {
            return 0;
        }

        int n = energy.length;

        // If there is only one book, we must read it.
        if (n == 1) {
            return energy[0];
        }

        // prev2 represents dp[i - 2]
        int prev2 = energy[0];

        // prev1 represents dp[i - 1]
        // For index 1, we can start directly at book 1.
        int prev1 = energy[1];

        // Process books from index 2 onward.
        for (int i = 2; i < n; i++) {
            // Current minimum cost to reach book i:
            // cost of this book + cheaper of the previous two reachable states.
            int current = energy[i] + Math.min(prev1, prev2);

            // Shift the window forward:
            // old prev1 becomes new prev2,
            // current becomes new prev1.
            prev2 = prev1;
            prev1 = current;
        }

        // After the loop, prev1 holds the minimum cost to reach the last book.
        return prev1;
    }

    /**
     * Converts an integer array into a readable string representation.
     *
     * This helper method is used only for demonstration in main.
     *
     * @param arr the array to convert to a string
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n), due to the created string content
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few extra cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations shown here
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample input 1 from the problem statement.
        int[] energy1 = {4, 2, 7, 3};
        int result1 = solution.minEnergy(energy1);
        int result1Optimized = solution.minEnergyOptimized(energy1);

        System.out.println("Input: energy = " + solution.arrayToString(energy1));
        System.out.println("Minimum energy (DP array): " + result1);
        System.out.println("Minimum energy (optimized): " + result1Optimized);
        System.out.println("Expected: 5");
        System.out.println();

        // Sample input 2 from the problem statement.
        int[] energy2 = {5, 1, 2, 10, 1};
        int result2 = solution.minEnergy(energy2);
        int result2Optimized = solution.minEnergyOptimized(energy2);

        System.out.println("Input: energy = " + solution.arrayToString(energy2));
        System.out.println("Minimum energy (DP array): " + result2);
        System.out.println("Minimum energy (optimized): " + result2Optimized);
        System.out.println("Expected: 4");
        System.out.println();

        // Extra test: only one book.
        int[] energy3 = {8};
        System.out.println("Input: energy = " + solution.arrayToString(energy3));
        System.out.println("Minimum energy (DP array): " + solution.minEnergy(energy3));
        System.out.println("Minimum energy (optimized): " + solution.minEnergyOptimized(energy3));
        System.out.println("Expected: 8");
        System.out.println();

        // Extra test: two books, can start at either 0 or 1, but must end on last book.
        int[] energy4 = {9, 3};
        System.out.println("Input: energy = " + solution.arrayToString(energy4));
        System.out.println("Minimum energy (DP array): " + solution.minEnergy(energy4));
        System.out.println("Minimum energy (optimized): " + solution.minEnergyOptimized(energy4));
        System.out.println("Expected: 3");
    }
}