import java.util.*;

/*
 * Title: Maximum Visible Booths Between Taller Buildings
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array heights where heights[i] represents the height of the i-th building
 * in a straight line. A pop-up booth can be placed on top of any building i. From building i, the booth
 * is considered enclosed if there exists at least one taller building on its left and at least one taller
 * building on its right.
 *
 * The visibility score of building i is the number of consecutive buildings strictly between the nearest
 * taller building on the left and the nearest taller building on the right.
 *
 * More formally:
 * - Let L be the closest index less than i such that heights[L] > heights[i]
 * - Let R be the closest index greater than i such that heights[R] > heights[i]
 * - If both L and R exist, then score(i) = R - L - 1
 * - Otherwise, score(i) = 0
 *
 * The task is to return the maximum visibility score among all buildings.
 *
 * Important note:
 * Equal-height buildings are NOT considered taller. Only strictly greater heights qualify.
 *
 * Constraints:
 * - 1 <= heights.length <= 200000
 * - 1 <= heights[i] <= 1000000000
 *
 * Efficient linear-time processing is required.
 */

public class Solution {

    /**
     * Computes the maximum visibility score among all buildings.
     *
     * The core idea is to find, for every index:
     * 1. The nearest strictly taller building on the left
     * 2. The nearest strictly taller building on the right
     *
     * We do this in linear time using a monotonic stack:
     * - First pass from left to right computes nearest greater on the left
     * - Second pass from right to left computes nearest greater on the right
     *
     * Once both boundaries are known for each building:
     * - If both exist, score = rightIndex - leftIndex - 1
     * - Otherwise, score = 0
     *
     * @param heights the array of building heights
     * @return the maximum visibility score among all buildings
     * Time complexity: O(n), where n is the number of buildings
     * Space complexity: O(n), for the boundary arrays and stack
     */
    public int maximumVisibilityScore(int[] heights) {
        int n = heights.length;

        // leftGreater[i] will store the index of the nearest strictly taller building to the left of i.
        // If no such building exists, it will remain -1.
        int[] leftGreater = new int[n];
        Arrays.fill(leftGreater, -1);

        // rightGreater[i] will store the index of the nearest strictly taller building to the right of i.
        // If no such building exists, it will remain -1.
        int[] rightGreater = new int[n];
        Arrays.fill(rightGreater, -1);

        // We use a stack of indices.
        // The stack helps us efficiently maintain candidates for "nearest greater".
        Deque<Integer> stack = new ArrayDeque<>();

        // ------------------------------------------------------------
        // PASS 1: Find nearest strictly taller building on the LEFT
        // ------------------------------------------------------------
        //
        // We scan from left to right.
        //
        // For current index i:
        // - While the stack top has height <= heights[i], it cannot be the nearest strictly taller
        //   building for i, so we remove it.
        // - After removals, if stack is not empty, the top is the nearest index to the left
        //   with height > heights[i].
        // - Then we push i onto the stack for future elements.
        //
        // Why pop while <= ?
        // Because equal height is NOT taller, and shorter height is also not taller.
        // So neither can serve as a valid enclosing boundary.
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && heights[stack.peek()] <= heights[i]) {
                stack.pop();
            }

            if (!stack.isEmpty()) {
                leftGreater[i] = stack.peek();
            }

            stack.push(i);
        }

        // Clear the stack so we can reuse it for the right-side pass.
        stack.clear();

        // ------------------------------------------------------------
        // PASS 2: Find nearest strictly taller building on the RIGHT
        // ------------------------------------------------------------
        //
        // We scan from right to left.
        //
        // For current index i:
        // - While the stack top has height <= heights[i], it cannot be the nearest strictly taller
        //   building for i on the right, so we remove it.
        // - After removals, if stack is not empty, the top is the nearest index to the right
        //   with height > heights[i].
        // - Then we push i onto the stack for future elements to the left.
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && heights[stack.peek()] <= heights[i]) {
                stack.pop();
            }

            if (!stack.isEmpty()) {
                rightGreater[i] = stack.peek();
            }

            stack.push(i);
        }

        // ------------------------------------------------------------
        // FINAL STEP: Compute the maximum score
        // ------------------------------------------------------------
        //
        // For each building i:
        // - If both leftGreater[i] and rightGreater[i] exist,
        //   then score = rightGreater[i] - leftGreater[i] - 1
        // - Otherwise score = 0
        //
        // We keep track of the maximum score seen.
        int maxScore = 0;

        for (int i = 0; i < n; i++) {
            if (leftGreater[i] != -1 && rightGreater[i] != -1) {
                int score = rightGreater[i] - leftGreater[i] - 1;
                maxScore = Math.max(maxScore, score);
            }
        }

        return maxScore;
    }

    /**
     * Computes the nearest strictly taller building index on the left for every position.
     *
     * This helper method is educational and can be used independently to inspect intermediate results.
     *
     * @param heights the array of building heights
     * @return an array where result[i] is the nearest index j < i such that heights[j] > heights[i],
     *         or -1 if no such index exists
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] nearestGreaterToLeft(int[] heights) {
        int n = heights.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);

        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && heights[stack.peek()] <= heights[i]) {
                stack.pop();
            }

            if (!stack.isEmpty()) {
                result[i] = stack.peek();
            }

            stack.push(i);
        }

        return result;
    }

    /**
     * Computes the nearest strictly taller building index on the right for every position.
     *
     * This helper method is educational and can be used independently to inspect intermediate results.
     *
     * @param heights the array of building heights
     * @return an array where result[i] is the nearest index j > i such that heights[j] > heights[i],
     *         or -1 if no such index exists
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] nearestGreaterToRight(int[] heights) {
        int n = heights.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);

        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && heights[stack.peek()] <= heights[i]) {
                stack.pop();
            }

            if (!stack.isEmpty()) {
                result[i] = stack.peek();
            }

            stack.push(i);
        }

        return result;
    }

    /**
     * Builds the score array for all buildings.
     *
     * For each index i:
     * - If both nearest taller boundaries exist, score[i] = right - left - 1
     * - Otherwise, score[i] = 0
     *
     * @param heights the array of building heights
     * @return an array of visibility scores for each building
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] visibilityScores(int[] heights) {
        int[] left = nearestGreaterToLeft(heights);
        int[] right = nearestGreaterToRight(heights);
        int[] scores = new int[heights.length];

        for (int i = 0; i < heights.length; i++) {
            if (left[i] != -1 && right[i] != -1) {
                scores[i] = right[i] - left[i] - 1;
            } else {
                scores[i] = 0;
            }
        }

        return scores;
    }

    /**
     * Prints a detailed demonstration for a given input array.
     *
     * This method is intended for beginner-friendly tracing:
     * - prints the heights
     * - prints nearest greater indices on both sides
     * - prints per-building scores
     * - prints the final maximum score
     *
     * @param heights the array of building heights to demonstrate
     * @return nothing
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public void demonstrate(int[] heights) {
        int[] left = nearestGreaterToLeft(heights);
        int[] right = nearestGreaterToRight(heights);
        int[] scores = visibilityScores(heights);
        int answer = maximumVisibilityScore(heights);

        System.out.println("Heights: " + Arrays.toString(heights));
        System.out.println("Nearest taller on left indices:  " + Arrays.toString(left));
        System.out.println("Nearest taller on right indices: " + Arrays.toString(right));
        System.out.println("Visibility scores:               " + Arrays.toString(scores));
        System.out.println("Maximum visibility score: " + answer);
        System.out.println();
    }

    /**
     * Main method demonstrating the solution on sample and additional test cases.
     *
     * Note about the provided examples:
     * The textual explanations in the prompt correctly derive:
     * - [5, 2, 4, 3, 6] -> 3
     * - [7, 1, 5, 2, 4, 8] -> 4
     *
     * Those are the correct outputs according to the formal definition.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size across demonstrations)
     * Space complexity: O(n) per demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sample1 = {5, 2, 4, 3, 6};
        int[] sample2 = {7, 1, 5, 2, 4, 8};

        System.out.println("Sample 1:");
        solution.demonstrate(sample1);
        System.out.println("Expected maximum score: 3");
        System.out.println();

        System.out.println("Sample 2:");
        solution.demonstrate(sample2);
        System.out.println("Expected maximum score: 4");
        System.out.println();

        int[] test1 = {1};
        int[] test2 = {3, 1, 3};
        int[] test3 = {9, 7, 5, 3, 1};
        int[] test4 = {1, 3, 5, 7, 9};
        int[] test5 = {6, 2, 5, 4, 3, 7};

        System.out.println("Additional Test 1:");
        solution.demonstrate(test1);

        System.out.println("Additional Test 2:");
        solution.demonstrate(test2);

        System.out.println("Additional Test 3:");
        solution.demonstrate(test3);

        System.out.println("Additional Test 4:");
        solution.demonstrate(test4);

        System.out.println("Additional Test 5:");
        solution.demonstrate(test5);
    }
}