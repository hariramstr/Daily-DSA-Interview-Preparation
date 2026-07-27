import java.util.*;

/*
Title: Minimum Swaps to Group Fragile Packages
Difficulty: Medium
Topic: Arrays

Problem Description:
A warehouse stores packages in a single row represented by an integer array packages,
where packages[i] = 1 means the package at position i is fragile and packages[i] = 0
means it is not fragile.

For safety inspection, all fragile packages should be placed next to each other in one
contiguous block. In one operation, you may swap the contents of any two positions in
the array.

Return the minimum number of swaps needed to group all fragile packages together.

You are not required to preserve the relative order of packages. If there are zero or
one fragile packages, the answer is 0 because they are already trivially grouped.

Key Observation:
If there are k fragile packages in total, then after grouping, those k fragile packages
must occupy some contiguous window of length k.

For any chosen window of length k:
- Every fragile package already inside the window is in a "good" position.
- Every non-fragile package (0) inside the window must be swapped out.
- Each such 0 can be swapped with a fragile package (1) outside the window.

Therefore:
minimum swaps = minimum number of zeros inside any window of length k
              = k - maximum number of ones inside any window of length k

So the task becomes:
1. Count total fragile packages k.
2. Slide a window of size k across the array.
3. Find the window containing the maximum number of fragile packages.
4. Return k - that maximum.

Constraints:
- 1 <= packages.length <= 100000
- packages[i] is either 0 or 1

Example 1:
Input: packages = [1,0,1,0,1]
Output: 1

Explanation:
There are 3 fragile packages total, so we inspect windows of length 3:
- [1,0,1] -> 2 fragile
- [0,1,0] -> 1 fragile
- [1,0,1] -> 2 fragile
Best window has 2 fragile packages, so swaps = 3 - 2 = 1.

Example 2:
Input: packages = [0,0,1,0,1,1,0]
Output: 1

Explanation:
There are 3 fragile packages total, so inspect windows of length 3:
- [0,0,1] -> 1 fragile
- [0,1,0] -> 1 fragile
- [1,0,1] -> 2 fragile
- [0,1,1] -> 2 fragile
- [1,1,0] -> 2 fragile
Best window has 2 fragile packages, so swaps = 3 - 2 = 1.
*/

public class Solution {

    /**
     * Computes the minimum number of swaps needed to group all fragile packages
     * (represented by 1s) into one contiguous block.
     *
     * The algorithm uses a sliding window:
     * 1. Count how many fragile packages exist in total. Let that count be k.
     * 2. Any final grouped arrangement must occupy a window of length k.
     * 3. For each window of length k, count how many fragile packages are already inside it.
     * 4. The best window is the one with the maximum number of fragile packages.
     * 5. The remaining positions in that window are non-fragile packages that must be swapped out.
     *
     * @param packages the array where 1 represents a fragile package and 0 represents a non-fragile package
     * @return the minimum number of swaps required to group all fragile packages together
     *
     * Time complexity: O(n), where n is the length of the array, because we scan the array a constant number of times.
     * Space complexity: O(1), because only a few integer variables are used.
     */
    public int minSwaps(int[] packages) {
        // Step 1:
        // Count the total number of fragile packages (1s) in the entire array.
        // This count tells us the exact size of the window we need to examine.
        int totalFragile = countFragilePackages(packages);

        // Step 2:
        // If there are no fragile packages or only one fragile package,
        // they are already trivially grouped, so no swaps are needed.
        if (totalFragile <= 1) {
            return 0;
        }

        // Step 3:
        // Build the first window of size totalFragile.
        // We count how many fragile packages are already inside this first window.
        int currentFragileInWindow = 0;
        for (int i = 0; i < totalFragile; i++) {
            currentFragileInWindow += packages[i];
        }

        // This variable stores the maximum number of fragile packages found
        // in any window of the required size.
        int maxFragileInAnyWindow = currentFragileInWindow;

        // Step 4:
        // Slide the window one position at a time from left to right.
        //
        // At each move:
        // - One element leaves the window from the left side.
        // - One element enters the window from the right side.
        //
        // Instead of recounting the whole window every time,
        // we update the count efficiently:
        // current = current - outgoing + incoming
        for (int right = totalFragile; right < packages.length; right++) {
            int left = right - totalFragile;

            // Remove the value that is no longer in the window.
            currentFragileInWindow -= packages[left];

            // Add the new value entering the window.
            currentFragileInWindow += packages[right];

            // Update the best answer seen so far.
            if (currentFragileInWindow > maxFragileInAnyWindow) {
                maxFragileInAnyWindow = currentFragileInWindow;
            }
        }

        // Step 5:
        // In the best window of size totalFragile:
        // - maxFragileInAnyWindow positions already contain fragile packages.
        // - The remaining positions are non-fragile packages that must be swapped out.
        //
        // Therefore, minimum swaps = totalFragile - maxFragileInAnyWindow.
        return totalFragile - maxFragileInAnyWindow;
    }

    /**
     * Counts how many fragile packages (1s) exist in the array.
     *
     * @param packages the array of package indicators, where 1 means fragile and 0 means not fragile
     * @return the total number of fragile packages in the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(1), because only a counter is used.
     */
    public int countFragilePackages(int[] packages) {
        int count = 0;

        // Traverse the entire array and add each value.
        // Since values are only 0 or 1, this directly counts the number of 1s.
        for (int value : packages) {
            count += value;
        }

        return count;
    }

    /**
     * Converts an integer array into a readable string form.
     * This helper is used only for demonstration output in main.
     *
     * @param arr the input integer array
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to the created string content.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration cases, excluding the cost of each method call.
     * Space complexity: O(1), excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        int[] packages1 = {1, 0, 1, 0, 1};
        int result1 = solution.minSwaps(packages1);
        System.out.println("Input:  packages = " + solution.arrayToString(packages1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 1");
        System.out.println();

        // Sample Example 2
        int[] packages2 = {0, 0, 1, 0, 1, 1, 0};
        int result2 = solution.minSwaps(packages2);
        System.out.println("Input:  packages = " + solution.arrayToString(packages2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional test: already grouped
        int[] packages3 = {0, 1, 1, 1, 0};
        int result3 = solution.minSwaps(packages3);
        System.out.println("Input:  packages = " + solution.arrayToString(packages3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional test: no fragile packages
        int[] packages4 = {0, 0, 0, 0};
        int result4 = solution.minSwaps(packages4);
        System.out.println("Input:  packages = " + solution.arrayToString(packages4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional test: one fragile package
        int[] packages5 = {0, 0, 1, 0, 0};
        int result5 = solution.minSwaps(packages5);
        System.out.println("Input:  packages = " + solution.arrayToString(packages5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 0");
        System.out.println();

        // Additional test: alternating pattern
        int[] packages6 = {1, 0, 1, 0, 1, 0, 1};
        int result6 = solution.minSwaps(packages6);
        System.out.println("Input:  packages = " + solution.arrayToString(packages6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 2");
    }
}