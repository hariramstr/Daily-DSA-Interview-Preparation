import java.util.*;

/*
 * Title: Maximum Consecutive Seats After Filling One Gap
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an array seats where seats[i] is either 0 or 1. A value of 1 means the seat is already occupied,
 * and 0 means it is empty. The event organizer is allowed to assign exactly one currently empty seat to a late guest.
 * After doing so, the organizer wants to know the maximum possible length of a consecutive block of occupied seats
 * anywhere in the row.
 *
 * Return the largest number of consecutive 1s that can appear after changing at most one 0 into a 1.
 * If the row is already fully occupied, return the length of the entire array.
 *
 * This problem models a common interview scenario involving array scanning and window maintenance.
 * A correct solution should work efficiently for large rows and should not try every possible seat independently.
 *
 * Constraints:
 * - 1 <= seats.length <= 200000
 * - seats[i] is either 0 or 1
 * - You may change at most one 0 to 1
 *
 * Example 1:
 * Input: seats = [1,1,0,1,1,1,0,1]
 * Output: 6
 * Explanation: Fill the empty seat at index 2. The row becomes [1,1,1,1,1,1,0,1], which contains a consecutive occupied block of length 6.
 *
 * Example 2:
 * Input: seats = [0,0,1,0,1,1,0]
 * Output: 4
 * Explanation: Fill the empty seat at index 3. The row becomes [0,0,1,1,1,1,0], giving a longest consecutive occupied block of length 4.
 */

public class Solution {

    /**
     * Computes the maximum possible length of consecutive occupied seats after changing at most one 0 into 1.
     *
     * The key idea is to maintain a sliding window that contains at most one zero.
     * Any such window can be turned into an all-ones window by flipping that single zero.
     * Therefore, the largest valid window length is exactly the answer.
     *
     * @param seats the array representing seats, where 1 means occupied and 0 means empty
     * @return the maximum number of consecutive 1s obtainable after flipping at most one 0
     * Time complexity: O(n), where n is the length of the array, because each index is visited at most a constant number of times.
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int maxConsecutiveSeatsAfterOneFill(int[] seats) {
        // "left" marks the beginning of the current sliding window.
        int left = 0;

        // "zeroCount" stores how many zeros are currently inside the window [left, right].
        // We are allowed to have at most one zero in the window, because that one zero can be flipped to 1.
        int zeroCount = 0;

        // "best" stores the maximum valid window length found so far.
        int best = 0;

        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < seats.length; right++) {
            // If the new element entering the window is 0, record it.
            if (seats[right] == 0) {
                zeroCount++;
            }

            // If the window now contains more than one zero, it is no longer valid.
            // Why invalid?
            // Because we are allowed to flip at most one zero, so a window with two or more zeros
            // cannot become all 1s with a single change.
            //
            // Therefore, we shrink the window from the left until it again contains at most one zero.
            while (zeroCount > 1) {
                // If the element leaving the window is a zero, reduce the zero count.
                if (seats[left] == 0) {
                    zeroCount--;
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // At this point, the window [left, right] contains at most one zero.
            // That means this entire window can be made consecutive 1s by flipping that zero
            // (or doing nothing if there is no zero at all).
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger than any previous one.
            best = Math.max(best, currentLength);
        }

        return best;
    }

    /**
     * Alternative implementation using prefix/suffix consecutive-one counts.
     *
     * For every zero position, we imagine flipping it and joining:
     * - the consecutive 1s immediately to its left
     * - the flipped seat itself
     * - the consecutive 1s immediately to its right
     *
     * This method is also correct, but it uses extra arrays.
     * It is included for educational purposes.
     *
     * @param seats the array representing seats, where 1 means occupied and 0 means empty
     * @return the maximum number of consecutive 1s obtainable after flipping at most one 0
     * Time complexity: O(n), because we build prefix and suffix arrays in linear time and scan once more.
     * Space complexity: O(n), because two helper arrays are used.
     */
    public int maxConsecutiveSeatsAfterOneFillPrefixSuffix(int[] seats) {
        int n = seats.length;

        // leftOnes[i] = number of consecutive 1s ending exactly at index i
        int[] leftOnes = new int[n];

        // rightOnes[i] = number of consecutive 1s starting exactly at index i
        int[] rightOnes = new int[n];

        // Build consecutive counts from left to right.
        for (int i = 0; i < n; i++) {
            if (seats[i] == 1) {
                leftOnes[i] = (i == 0) ? 1 : leftOnes[i - 1] + 1;
            } else {
                leftOnes[i] = 0;
            }
        }

        // Build consecutive counts from right to left.
        for (int i = n - 1; i >= 0; i--) {
            if (seats[i] == 1) {
                rightOnes[i] = (i == n - 1) ? 1 : rightOnes[i + 1] + 1;
            } else {
                rightOnes[i] = 0;
            }
        }

        // Start with the case where the array may already be all ones.
        int best = 0;
        boolean hasZero = false;

        for (int seat : seats) {
            if (seat == 0) {
                hasZero = true;
                break;
            }
        }

        // If there is no zero, the whole array is already one consecutive occupied block.
        if (!hasZero) {
            return n;
        }

        // Try flipping each zero and compute the merged block length.
        for (int i = 0; i < n; i++) {
            if (seats[i] == 0) {
                int leftPart = (i > 0) ? leftOnes[i - 1] : 0;
                int rightPart = (i < n - 1) ? rightOnes[i + 1] : 0;

                // Flip this zero, so total block length becomes:
                // consecutive ones on left + 1 flipped seat + consecutive ones on right
                int merged = leftPart + 1 + rightPart;
                best = Math.max(best, merged);
            }
        }

        return best;
    }

    /**
     * Converts an int array to a readable string.
     *
     * @param arr the input array
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to the created string content.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) across all demonstrated test cases, where k is the number of examples shown.
     * Space complexity: O(1) extra beyond the sample arrays themselves.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] seats1 = {1, 1, 0, 1, 1, 1, 0, 1};
        int[] seats2 = {0, 0, 1, 0, 1, 1, 0};

        int result1 = solution.maxConsecutiveSeatsAfterOneFill(seats1);
        int result2 = solution.maxConsecutiveSeatsAfterOneFill(seats2);

        System.out.println("Sample Input 1: " + solution.arrayToString(seats1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        System.out.println("Sample Input 2: " + solution.arrayToString(seats2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        int[] seats3 = {1, 1, 1, 1};
        int[] seats4 = {0};
        int[] seats5 = {1, 0, 1};
        int[] seats6 = {0, 0, 0, 0};
        int[] seats7 = {1, 0, 1, 1, 0, 1, 1, 1};

        System.out.println("Additional Test 1: " + solution.arrayToString(seats3));
        System.out.println("Output: " + solution.maxConsecutiveSeatsAfterOneFill(seats3));
        System.out.println();

        System.out.println("Additional Test 2: " + solution.arrayToString(seats4));
        System.out.println("Output: " + solution.maxConsecutiveSeatsAfterOneFill(seats4));
        System.out.println();

        System.out.println("Additional Test 3: " + solution.arrayToString(seats5));
        System.out.println("Output: " + solution.maxConsecutiveSeatsAfterOneFill(seats5));
        System.out.println();

        System.out.println("Additional Test 4: " + solution.arrayToString(seats6));
        System.out.println("Output: " + solution.maxConsecutiveSeatsAfterOneFill(seats6));
        System.out.println();

        System.out.println("Additional Test 5: " + solution.arrayToString(seats7));
        System.out.println("Output: " + solution.maxConsecutiveSeatsAfterOneFill(seats7));
        System.out.println();
    }
}