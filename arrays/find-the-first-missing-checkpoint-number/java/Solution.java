import java.util.*;

/*
Problem Title: Find the First Missing Checkpoint Number

Problem Description:
A delivery company labels route checkpoints with positive integer IDs starting from 1.
After syncing data from a driver's device, you receive an unsorted array checkpoints
containing the IDs that were recorded during the trip. Some IDs may appear more than
once because of duplicate scans, and some values may be invalid, such as 0 or negative
numbers.

Your task is to return the smallest positive checkpoint ID that does not appear in the
array. In other words, find the first missing positive integer in the recorded data.

This problem is useful for validating whether the earliest expected checkpoint was skipped
or never uploaded. Only positive IDs matter. Duplicates do not change the answer, and
invalid values should be ignored.

You should design a solution that works efficiently for typical interview constraints.

Constraints:
- 1 <= checkpoints.length <= 10^5
- -10^5 <= checkpoints[i] <= 10^5
- The array may contain duplicates
- The array is not guaranteed to be sorted

Example 1:
Input: checkpoints = [3, 4, -1, 1]
Output: 2
Explanation: Positive IDs 1, 3, and 4 are present, but 2 is missing, so the answer is 2.

Example 2:
Input: checkpoints = [1, 2, 2, 5]
Output: 3
Explanation: IDs 1 and 2 are present. The smallest missing positive checkpoint ID is 3.
*/

public class Solution {

    /**
     * Finds the smallest missing positive checkpoint ID.
     *
     * This method uses the classic in-place "cyclic placement" idea:
     * every positive value x that is in the useful range [1, n] should ideally
     * be placed at index x - 1. After that rearrangement, the first index i
     * where checkpoints[i] != i + 1 reveals the missing positive number.
     *
     * @param checkpoints the unsorted array of recorded checkpoint IDs; may contain
     *                    duplicates, zeros, and negative values
     * @return the smallest positive checkpoint ID that does not appear in the array
     * Time complexity: O(n), because each value is moved at most a constant number of times
     * Space complexity: O(1), because the rearrangement is done in-place
     */
    public int firstMissingPositive(int[] checkpoints) {
        int n = checkpoints.length;

        // We will iterate through the array and try to place each valid number
        // into its "correct" position.
        //
        // Correct position rule:
        // - Value 1 belongs at index 0
        // - Value 2 belongs at index 1
        // - Value 3 belongs at index 2
        // ...
        // - Value n belongs at index n - 1
        //
        // Values that are <= 0 or > n cannot help us determine the first missing
        // positive in the range [1, n + 1], so we ignore them.
        //
        // Why is [1, n] the only useful range?
        // Because for an array of length n, the answer must be in [1, n + 1].
        // If all values 1 through n are present, then the answer is n + 1.
        int i = 0;
        while (i < n) {
            int currentValue = checkpoints[i];

            // Check whether the current value is useful:
            // 1) It must be positive
            // 2) It must be within array-placement range [1, n]
            //
            // If it is useful, then its target index is currentValue - 1.
            if (currentValue >= 1 && currentValue <= n) {
                int correctIndex = currentValue - 1;

                // We only swap if the current value is not already in its correct place
                // AND the target place does not already contain the same value.
                //
                // The second condition is extremely important for duplicates.
                // Example: [1, 2, 2, 5]
                // If we tried to keep swapping a 2 with another 2, we could loop forever.
                if (checkpoints[i] != checkpoints[correctIndex]) {
                    swap(checkpoints, i, correctIndex);

                    // After swapping, we do NOT increment i yet.
                    // Reason:
                    // A new value has arrived at index i, and we must examine it too.
                    continue;
                }
            }

            // If the current value is:
            // - already in the correct place, or
            // - invalid (<= 0), or
            // - too large (> n), or
            // - a duplicate already occupying its correct target slot,
            // then we simply move forward.
            i++;
        }

        // After the placement phase, the array should ideally look like:
        // index:      0  1  2  3  ...
        // value:      1  2  3  4  ...
        //
        // The first place where this pattern breaks tells us the answer.
        for (int index = 0; index < n; index++) {
            // If index 0 does not contain 1, then 1 is missing.
            // If index 1 does not contain 2, then 2 is missing.
            // ...
            if (checkpoints[index] != index + 1) {
                return index + 1;
            }
        }

        // If every position 0..n-1 contains exactly 1..n,
        // then the smallest missing positive is n + 1.
        return n + 1;
    }

    /**
     * Swaps two elements inside the given array.
     *
     * @param array the array in which two positions should be exchanged
     * @param firstIndex the first index to swap
     * @param secondIndex the second index to swap
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void swap(int[] array, int firstIndex, int secondIndex) {
        int temp = array[firstIndex];
        array[firstIndex] = array[secondIndex];
        array[secondIndex] = temp;
    }

    /**
     * Converts an int array to a readable string for demonstration output.
     *
     * @param array the array to convert into string form
     * @return a human-readable string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * Verified examples:
     * - [3, 4, -1, 1] -> 2
     * - [1, 2, 2, 5] -> 3
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     * Time complexity: O(k * n) across all demonstrations, where k is the number of test cases
     * Space complexity: O(1) extra per algorithm call, excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // Input: [3, 4, -1, 1]
        // Present positive IDs are 1, 3, 4
        // Missing smallest positive is 2
        int[] checkpoints1 = {3, 4, -1, 1};
        int result1 = solution.firstMissingPositive(checkpoints1.clone());
        System.out.println("Input: " + solution.arrayToString(checkpoints1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        // Example 2 from the problem statement:
        // Input: [1, 2, 2, 5]
        // Present positive IDs are 1, 2, 5
        // Missing smallest positive is 3
        int[] checkpoints2 = {1, 2, 2, 5};
        int result2 = solution.firstMissingPositive(checkpoints2.clone());
        System.out.println("Input: " + solution.arrayToString(checkpoints2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional test:
        // If 1 is missing immediately, answer should be 1.
        int[] checkpoints3 = {7, 8, 9, 11, 12};
        int result3 = solution.firstMissingPositive(checkpoints3.clone());
        System.out.println("Input: " + solution.arrayToString(checkpoints3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional test:
        // All first few positives are present, so answer becomes next one.
        int[] checkpoints4 = {1, 2, 3, 4};
        int result4 = solution.firstMissingPositive(checkpoints4.clone());
        System.out.println("Input: " + solution.arrayToString(checkpoints4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 5");
        System.out.println();

        // Additional test:
        // Duplicates and invalid values should not affect correctness.
        int[] checkpoints5 = {0, -2, 2, 2, 1};
        int result5 = solution.firstMissingPositive(checkpoints5.clone());
        System.out.println("Input: " + solution.arrayToString(checkpoints5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 3");
    }
}