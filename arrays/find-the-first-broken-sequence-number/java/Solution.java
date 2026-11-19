import java.util.*;

/*
Title: Find the First Broken Sequence Number
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an array nums representing event sequence numbers recorded by a service during one batch.
The sequence numbers are supposed to start at 1 and increase by exactly 1 each time with no gaps.
However, due to a logging issue, one positive sequence number in that expected range is missing from the array.
Your task is to return the smallest missing positive sequence number.

The array may be unsorted, and values that are 0, negative, or larger than the array length may appear
because of corrupted records. Those values should be ignored when determining the answer.
For example, if the batch has length n, then the missing answer must be in the range [1, n + 1].

Write a function that returns the first positive integer that does not appear in nums.

Constraints:
- 1 <= nums.length <= 100000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [3, 4, -1, 1]
Output: 2
Explanation: The positive numbers 1 and 3 and 4 appear, but 2 is the smallest missing positive number.

Example 2:
Input: nums = [1, 2, 0]
Output: 3
Explanation: The sequence contains 1 and 2, so the first missing positive number is 3.
*/

/**
 * Beginner-friendly solution class for finding the first missing positive integer.
 */
public class Solution {

    /**
     * Finds the smallest positive integer that does not appear in the given array.
     *
     * The key observation is:
     * - If the array length is n, then the answer must be in the range [1, n + 1].
     * - Therefore, we only care about values from 1 through n.
     * - We can record which of those values appear using a boolean array.
     *
     * Example:
     * nums = [3, 4, -1, 1], n = 4
     * Relevant values in range [1, 4] are 3, 4, and 1
     * Missing smallest positive is 2
     *
     * @param nums the input array that may contain positive, negative, zero, and out-of-range values
     * @return the smallest positive integer missing from the array
     * Time complexity: O(n), because we scan the array once to mark seen values and once more to find the answer
     * Space complexity: O(n), because we use an extra boolean array of size n + 1
     */
    public int firstMissingPositive(int[] nums) {
        // The length of the array determines the only range of values we need to care about.
        // If the array has length n, then the first missing positive must be between 1 and n + 1.
        int n = nums.length;

        // seen[value] will be true if "value" appears in the array.
        // We create size n + 1 so that indices 1 through n are directly usable.
        // Index 0 is unused because 0 is not a positive integer we care about.
        boolean[] seen = new boolean[n + 1];

        // Step 1:
        // Go through every number in the input array.
        // We only mark numbers that are useful:
        // - positive
        // - at most n
        //
        // Why ignore others?
        // - numbers <= 0 cannot be the first missing positive
        // - numbers > n also cannot affect the answer in [1, n + 1]
        for (int num : nums) {
            if (num >= 1 && num <= n) {
                seen[num] = true;
            }
        }

        // Step 2:
        // Starting from 1, find the first positive integer that was never seen.
        // The first such number is the answer.
        for (int candidate = 1; candidate <= n; candidate++) {
            if (!seen[candidate]) {
                return candidate;
            }
        }

        // Step 3:
        // If every number from 1 to n was present, then the smallest missing positive is n + 1.
        return n + 1;
    }

    /**
     * Utility method to print an array in a readable form.
     *
     * @param nums the array to convert to text
     * @return a string representation of the array
     * Time complexity: O(n), because each element is visited once while building the string
     * Space complexity: O(n), because the output string stores all printed characters
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Runs a single demonstration test case:
     * prints the input array and the computed answer.
     *
     * @param nums the test input array
     * @return the computed first missing positive integer
     * Time complexity: O(n), dominated by the call to firstMissingPositive
     * Space complexity: O(n), dominated by the boolean array used in firstMissingPositive
     */
    public int runDemo(int[] nums) {
        System.out.println("Input:  " + arrayToString(nums));
        int result = firstMissingPositive(nums);
        System.out.println("Output: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method demonstrating the solution on the sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * Verified examples:
     * - [3, 4, -1, 1] -> 2
     * - [1, 2, 0] -> 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) across all demo cases, where k is the number of test cases
     * Space complexity: O(n) per test case during execution of firstMissingPositive
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // nums = [3, 4, -1, 1]
        // Relevant positives within range [1, 4] are 1, 3, 4
        // The smallest missing positive is 2
        solution.runDemo(new int[]{3, 4, -1, 1});

        // Example 2 from the problem statement:
        // nums = [1, 2, 0]
        // Relevant positives within range [1, 3] are 1 and 2
        // The smallest missing positive is 3
        solution.runDemo(new int[]{1, 2, 0});

        // Additional tests for clarity:

        // Missing 1 immediately
        solution.runDemo(new int[]{2, 3, 4});

        // All first few positives exist, so answer is next one
        solution.runDemo(new int[]{1, 2, 3, 4});

        // Duplicates and invalid values should not break the logic
        solution.runDemo(new int[]{1, 1, 2, 2, -5, 100});

        // Single-element case
        solution.runDemo(new int[]{1});
    }
}