import java.util.*;

/*
Problem Title: Minimum Swaps to Group Delayed Flights

Problem Description:
An airport operations dashboard stores the status of flights in a binary array `flights`,
where `flights[i] = 1` means the `i`-th flight is delayed and `flights[i] = 0` means it is on time.
For reporting purposes, the airport wants all delayed flights to appear together in one contiguous
block in the array. You may swap the values at any two different indices, and each swap counts
as one operation.

Return the minimum number of swaps needed to group all delayed flights together.

If there are no delayed flights, or there is only one delayed flight, the answer is `0`
because they are already trivially grouped.

A useful way to think about the problem is that if there are `k` delayed flights in total,
then the final grouped block must have length `k`. For any candidate block of length `k`,
every on-time flight inside that block would need to be swapped with a delayed flight outside
the block. Your task is to find the best such block.

Constraints:
- 1 <= flights.length <= 100000
- flights[i] is either 0 or 1

Example 1:
Input: flights = [1,0,1,0,1]
Output: 1

Example 2:
Input: flights = [0,0,1,0,1,1,0]
Output: 1
*/

/**
 * Beginner-friendly solution for finding the minimum number of swaps needed
 * to group all delayed flights (1s) together in a binary array.
 */
public class Solution {

    /**
     * Computes the minimum number of swaps needed to group all delayed flights together.
     *
     * Core idea:
     * 1. Count how many delayed flights exist in total. Let that count be k.
     * 2. If k is 0 or 1, they are already trivially grouped, so answer is 0.
     * 3. Any final grouped arrangement must occupy some contiguous window of length k.
     * 4. Inside such a window, every 0 is a "bad" value because we want only 1s there.
     * 5. Each 0 inside the chosen window can be swapped with a 1 outside the window.
     * 6. Therefore, for every window of length k, the number of swaps needed equals
     *    the number of 0s inside that window.
     * 7. So we slide a window of length k across the array and find the minimum number
     *    of 0s in any such window.
     *
     * @param flights the binary array where 1 means delayed and 0 means on time
     * @return the minimum number of swaps required to group all 1s together
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), ignoring input storage
     */
    public int minSwaps(int[] flights) {
        // Step 1:
        // Count the total number of delayed flights (1s).
        // This tells us the exact size of the contiguous block we want in the end.
        int delayedCount = countOnes(flights);

        // Step 2:
        // If there are no delayed flights, or only one delayed flight,
        // then they are already grouped by definition.
        if (delayedCount <= 1) {
            return 0;
        }

        // Step 3:
        // We now examine every window of length delayedCount.
        // For each window, we count how many 0s are inside it.
        // That count equals the number of swaps needed for that window.
        int windowSize = delayedCount;

        // Step 4:
        // Build the first window [0 ... windowSize - 1].
        // We count how many on-time flights (0s) are inside this initial window.
        int zerosInWindow = 0;
        for (int i = 0; i < windowSize; i++) {
            if (flights[i] == 0) {
                zerosInWindow++;
            }
        }

        // Step 5:
        // Initialize the answer with the first window's zero count.
        // This is our current best (minimum swaps found so far).
        int minSwapsNeeded = zerosInWindow;

        // Step 6:
        // Slide the window one position at a time.
        //
        // For each new position:
        // - One element leaves the window from the left.
        // - One element enters the window from the right.
        //
        // We update zerosInWindow efficiently instead of recounting the whole window.
        for (int right = windowSize; right < flights.length; right++) {
            int left = right - windowSize;

            // If the element leaving the window is 0,
            // then the zero count inside the window decreases by 1.
            if (flights[left] == 0) {
                zerosInWindow--;
            }

            // If the new element entering the window is 0,
            // then the zero count inside the window increases by 1.
            if (flights[right] == 0) {
                zerosInWindow++;
            }

            // Update the best answer seen so far.
            minSwapsNeeded = Math.min(minSwapsNeeded, zerosInWindow);
        }

        // Step 7:
        // The smallest number of 0s in any valid window is exactly
        // the minimum number of swaps required.
        return minSwapsNeeded;
    }

    /**
     * Counts how many delayed flights (1s) are present in the array.
     *
     * @param flights the binary array
     * @return the total number of elements equal to 1
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1)
     */
    public int countOnes(int[] flights) {
        int count = 0;

        // Visit every element and count how many are 1.
        for (int flight : flights) {
            if (flight == 1) {
                count++;
            }
        }

        return count;
    }

    /**
     * Converts an int array to a readable string representation.
     * This helper is used only for demonstration output in main.
     *
     * @param arr the array to convert
     * @return a string like [1, 0, 1]
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n) for the created string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(1) extra, excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // flights = [1,0,1,0,1]
        // Total 1s = 3, so we inspect windows of length 3:
        // [1,0,1] -> one 0
        // [0,1,0] -> two 0s
        // [1,0,1] -> one 0
        // Minimum = 1
        int[] flights1 = {1, 0, 1, 0, 1};
        System.out.println("Input:  " + solution.arrayToString(flights1));
        System.out.println("Output: " + solution.minSwaps(flights1));
        System.out.println("Expected: 1");
        System.out.println();

        // Sample 2:
        // flights = [0,0,1,0,1,1,0]
        // Total 1s = 3, so windows of length 3:
        // [0,0,1] -> two 0s
        // [0,1,0] -> two 0s
        // [1,0,1] -> one 0
        // [0,1,1] -> one 0
        // [1,1,0] -> one 0
        // Minimum = 1
        int[] flights2 = {0, 0, 1, 0, 1, 1, 0};
        System.out.println("Input:  " + solution.arrayToString(flights2));
        System.out.println("Output: " + solution.minSwaps(flights2));
        System.out.println("Expected: 1");
        System.out.println();

        // Edge case: no delayed flights
        int[] flights3 = {0, 0, 0, 0};
        System.out.println("Input:  " + solution.arrayToString(flights3));
        System.out.println("Output: " + solution.minSwaps(flights3));
        System.out.println("Expected: 0");
        System.out.println();

        // Edge case: one delayed flight
        int[] flights4 = {0, 1, 0, 0};
        System.out.println("Input:  " + solution.arrayToString(flights4));
        System.out.println("Output: " + solution.minSwaps(flights4));
        System.out.println("Expected: 0");
        System.out.println();

        // Edge case: already grouped
        int[] flights5 = {0, 1, 1, 1, 0};
        System.out.println("Input:  " + solution.arrayToString(flights5));
        System.out.println("Output: " + solution.minSwaps(flights5));
        System.out.println("Expected: 0");
        System.out.println();
    }
}