import java.util.*;

/*
 * Title: Maximum Consecutive Shelves After One Book Relocation
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A library stores books on a long shelf, represented by an array shelves of length n.
 * Each element is either 0 or 1, where 1 means the position currently contains a featured
 * book and 0 means it is empty. The librarian may relocate at most one featured book from
 * any position containing 1 to any position containing 0. After this optional relocation,
 * determine the maximum possible length of a consecutive block of featured books.
 *
 * A relocation removes one existing 1 and places it into one existing 0, so the total number
 * of featured books stays the same. You may also choose not to relocate any book. Your task
 * is to return the largest number of consecutive 1s that can appear after performing at most
 * one such move.
 *
 * This problem models a realistic array optimization scenario: one local change can merge two
 * nearby runs, extend an existing run, or be useless if the array is already optimal. Be careful
 * when a 0 separates two runs of 1s: you can only fill that gap if there is at least one extra
 * 1 somewhere else to move.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - shelves[i] is either 0 or 1
 *
 * Example 1:
 * Input: shelves = [1,1,0,1,1,0,1]
 * Output: 5
 * Explanation: Move the last 1 into the zero at index 2. The array can become
 * [1,1,1,1,1,0,0], giving a consecutive block of length 5.
 *
 * Example 2:
 * Input: shelves = [1,0,1,1,0,1]
 * Output: 4
 * Explanation: Fill the zero at index 4 using the 1 at index 0. One possible result is
 * [0,0,1,1,1,1], so the maximum consecutive block is 4.
 */

public class Solution {

    /**
     * Computes the maximum possible length of consecutive 1s after relocating at most one 1
     * to one 0.
     *
     * Core idea:
     * We examine every index that contains 0 and imagine filling that 0 with a moved 1.
     * If we fill a zero at position i, then the consecutive block created there is:
     *
     *     consecutive 1s immediately on the left of i
     *   + 1 for the filled position itself
     *   + consecutive 1s immediately on the right of i
     *
     * However, this is only fully possible if there exists some extra 1 elsewhere to move.
     * If the left block and right block already use all existing 1s in the array, then there
     * is no outside 1 available to relocate, so the best we can do is just keep all existing
     * 1s together, which equals left + right.
     *
     * We precompute:
     * - left[i]  = number of consecutive 1s ending at index i
     * - right[i] = number of consecutive 1s starting at index i
     *
     * Then for each zero:
     * - leftOnes  = consecutive 1s ending at i - 1
     * - rightOnes = consecutive 1s starting at i + 1
     * - merged = leftOnes + rightOnes
     *
     * If totalOnes > merged, then there is at least one extra 1 outside these adjacent runs,
     * so we can move it into this zero and obtain merged + 1.
     * Otherwise, all 1s are already part of those adjacent runs, so the best possible block
     * is just merged.
     *
     * We also handle edge cases:
     * - If the array has no 1s, answer is 0.
     * - If the array has no 0s, answer is total number of 1s, because no move is possible
     *   and the array is already all 1s.
     *
     * @param shelves the binary array representing featured books (1) and empty positions (0)
     * @return the maximum possible length of consecutive 1s after at most one relocation
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int maxConsecutiveShelvesAfterOneRelocation(int[] shelves) {
        int n = shelves.length;

        // Count total number of 1s in the entire array.
        // This is essential because a relocation does not create a new 1;
        // it only moves an existing one.
        int totalOnes = 0;
        for (int value : shelves) {
            if (value == 1) {
                totalOnes++;
            }
        }

        // If there are no featured books at all, we cannot form any consecutive block of 1s.
        if (totalOnes == 0) {
            return 0;
        }

        // left[i] will store the number of consecutive 1s ending exactly at index i.
        // Example:
        // shelves = [1,1,0,1]
        // left    = [1,2,0,1]
        int[] left = new int[n];

        // Build the left array from left to right.
        for (int i = 0; i < n; i++) {
            if (shelves[i] == 1) {
                // If current cell is 1, then:
                // - it starts a new streak of length 1 if i == 0
                // - otherwise it extends the streak ending at i - 1
                left[i] = (i > 0 ? left[i - 1] : 0) + 1;
            } else {
                // A zero breaks any consecutive streak of 1s.
                left[i] = 0;
            }
        }

        // right[i] will store the number of consecutive 1s starting exactly at index i.
        // Example:
        // shelves = [1,1,0,1]
        // right   = [2,1,0,1]
        int[] right = new int[n];

        // Build the right array from right to left.
        for (int i = n - 1; i >= 0; i--) {
            if (shelves[i] == 1) {
                // If current cell is 1, then:
                // - it starts a new streak of length 1 if i == n - 1
                // - otherwise it extends the streak starting at i + 1
                right[i] = (i + 1 < n ? right[i + 1] : 0) + 1;
            } else {
                // A zero breaks any consecutive streak of 1s.
                right[i] = 0;
            }
        }

        // Initialize answer with the best existing streak without any move.
        // This ensures "at most one move" is respected, because we are allowed to do nothing.
        int answer = 0;
        for (int i = 0; i < n; i++) {
            answer = Math.max(answer, left[i]);
        }

        // Now evaluate every zero as a candidate position to receive a moved 1.
        for (int i = 0; i < n; i++) {
            if (shelves[i] == 0) {
                // Count consecutive 1s immediately to the left of this zero.
                int leftOnes = (i > 0) ? left[i - 1] : 0;

                // Count consecutive 1s immediately to the right of this zero.
                int rightOnes = (i + 1 < n) ? right[i + 1] : 0;

                // If we conceptually connect both sides through this zero,
                // the adjacent runs contribute this many existing 1s.
                int merged = leftOnes + rightOnes;

                // Important correctness detail:
                // We can only fill this zero if there exists some 1 somewhere to move.
                //
                // Case 1: totalOnes > merged
                // There is at least one extra 1 outside these adjacent runs.
                // So we can move that extra 1 into this zero and get merged + 1.
                //
                // Case 2: totalOnes == merged
                // All 1s are already exactly in these adjacent runs.
                // There is no outside 1 to move into this zero.
                // If we move one of these 1s, we would break one side while filling the zero,
                // so the best achievable consecutive block remains merged.
                if (totalOnes > merged) {
                    answer = Math.max(answer, merged + 1);
                } else {
                    answer = Math.max(answer, merged);
                }
            }
        }

        return answer;
    }

    /**
     * A second public method that performs the same computation.
     * This wrapper is included to keep the API beginner-friendly and explicit.
     *
     * @param shelves the binary array representing featured books and empty positions
     * @return the maximum possible consecutive block length after at most one relocation
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int solve(int[] shelves) {
        return maxConsecutiveShelvesAfterOneRelocation(shelves);
    }

    /**
     * Utility method to print an array in a readable format.
     *
     * @param arr the array to convert into a string
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) for k demo test cases
     * Space complexity: O(n) per test case due to the algorithm's helper arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] shelves1 = {1, 1, 0, 1, 1, 0, 1};
        int result1 = solution.solve(shelves1);
        System.out.println("Input:  " + solution.arrayToString(shelves1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 5");
        System.out.println();

        int[] shelves2 = {1, 0, 1, 1, 0, 1};
        int result2 = solution.solve(shelves2);
        System.out.println("Input:  " + solution.arrayToString(shelves2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        int[] shelves3 = {1, 1, 1, 1};
        int result3 = solution.solve(shelves3);
        System.out.println("Input:  " + solution.arrayToString(shelves3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 4");
        System.out.println();

        int[] shelves4 = {0, 0, 0, 0};
        int result4 = solution.solve(shelves4);
        System.out.println("Input:  " + solution.arrayToString(shelves4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
        System.out.println();

        int[] shelves5 = {1, 0, 1};
        int result5 = solution.solve(shelves5);
        System.out.println("Input:  " + solution.arrayToString(shelves5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 2");
        System.out.println();

        int[] shelves6 = {0, 1, 1, 0, 1, 1, 1, 0};
        int result6 = solution.solve(shelves6);
        System.out.println("Input:  " + solution.arrayToString(shelves6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 5");
    }
}