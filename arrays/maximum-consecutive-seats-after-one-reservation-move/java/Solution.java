import java.util.*;

/*
Problem Title: Maximum Consecutive Seats After One Reservation Move

Problem Description:
A theater keeps a row of seats represented by a binary array seats, where seats[i] = 1
means seat i is currently reserved and seats[i] = 0 means it is empty. To improve group
seating, the manager is allowed to perform at most one reservation move: choose one
reserved seat and move that reservation to any empty seat. After the move, the total
number of reserved seats stays the same. Your task is to return the maximum possible
length of a consecutive block of reserved seats that can be formed.

You may also choose not to move any reservation if the current arrangement is already
optimal. The move is not a swap: one 1 is removed from its current position and placed
into one 0 position. This means a move can connect two separated reserved blocks only if
there is a gap structure that makes it beneficial, and the answer depends on whether
there is at least one extra reserved seat elsewhere to relocate.

Return the largest number of consecutive 1s obtainable after at most one move.

Constraints:
- 1 <= seats.length <= 2 * 10^5
- seats[i] is either 0 or 1
- The solution should run in linear time or close to it

Examples:
1) seats = [1,1,0,1,0,1,1,1]
   Output: 4

2) seats = [1,0,1,1,0,1]
   Output: 3

Key Insight:
This problem is equivalent to finding the maximum consecutive 1s obtainable after
swapping at most one 0 with one 1 somewhere else, but phrased as "move one reservation".
For every zero position, we can ask:
- How many consecutive 1s are immediately on its left?
- How many consecutive 1s are immediately on its right?

If we fill that zero, the local block length becomes left + right + 1.
However, this is only fully possible if there exists at least one extra reserved seat
outside those left/right blocks to move into this zero. If not, then we cannot increase
the total size beyond the total number of 1s already participating, so the achievable
length is capped by the total number of 1s in the entire array.

Therefore, for each zero:
candidate = min(totalOnes, leftOnes + rightOnes + 1)

The answer is the maximum such candidate over all zeros.
If there are no zeros, the whole array is already all 1s.
*/
public class Solution {

    /**
     * Computes the maximum possible length of a consecutive block of reserved seats (1s)
     * after performing at most one reservation move.
     *
     * Detailed idea:
     * 1. Count the total number of reserved seats in the entire array.
     * 2. Precompute, for every index:
     *    - left[i]  = number of consecutive 1s ending at index i
     *    - right[i] = number of consecutive 1s starting at index i
     * 3. For every zero position i:
     *    - Let leftOnes  = consecutive 1s immediately to the left of i
     *    - Let rightOnes = consecutive 1s immediately to the right of i
     *    - If we place a moved reservation into this zero, the local merged block would
     *      look like leftOnes + 1 + rightOnes
     *    - But we cannot create more 1s than exist globally, so cap by totalOnes
     *    - candidate = min(totalOnes, leftOnes + rightOnes + 1)
     * 4. Also handle the special case where the array contains no zero:
     *    - Then no move is needed, and the answer is the full array length.
     *
     * Why this is correct:
     * - Filling one zero can only connect the consecutive 1-block directly touching it
     *   on the left and right.
     * - If there is an extra 1 elsewhere, we can move that extra 1 into this zero
     *   without reducing the merged block.
     * - If there is no extra 1 elsewhere, then all 1s are already part of those touching
     *   blocks, so the best achievable merged size is exactly totalOnes.
     *
     * @param seats the binary array where 1 means reserved and 0 means empty
     * @return the maximum possible length of consecutive 1s after at most one move
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int maxConsecutiveSeatsAfterOneMove(int[] seats) {
        int n = seats.length;

        // Count total number of reserved seats (total number of 1s).
        int totalOnes = 0;
        for (int seat : seats) {
            if (seat == 1) {
                totalOnes++;
            }
        }

        // If there are no reserved seats at all, the best consecutive block is 0.
        if (totalOnes == 0) {
            return 0;
        }

        // left[i] = number of consecutive 1s ending exactly at index i.
        int[] left = new int[n];

        // Build the left consecutive counts.
        // Example:
        // seats = [1,1,0,1]
        // left  = [1,2,0,1]
        for (int i = 0; i < n; i++) {
            if (seats[i] == 1) {
                left[i] = (i > 0 ? left[i - 1] : 0) + 1;
            } else {
                left[i] = 0;
            }
        }

        // right[i] = number of consecutive 1s starting exactly at index i.
        int[] right = new int[n];

        // Build the right consecutive counts.
        // Example:
        // seats = [1,1,0,1]
        // right = [2,1,0,1]
        for (int i = n - 1; i >= 0; i--) {
            if (seats[i] == 1) {
                right[i] = (i + 1 < n ? right[i + 1] : 0) + 1;
            } else {
                right[i] = 0;
            }
        }

        // Track whether the array contains any zero.
        boolean hasZero = false;

        // This will store the best answer found.
        int answer = 0;

        // First, the current arrangement itself may already contain some long block of 1s.
        // Since we are allowed to do "at most one move", we should consider not moving too.
        for (int value : left) {
            answer = Math.max(answer, value);
        }

        // Now try treating each zero as the place where we move one reservation into.
        for (int i = 0; i < n; i++) {
            if (seats[i] == 0) {
                hasZero = true;

                // Number of consecutive 1s immediately to the left of this zero.
                int leftOnes = (i > 0) ? left[i - 1] : 0;

                // Number of consecutive 1s immediately to the right of this zero.
                int rightOnes = (i + 1 < n) ? right[i + 1] : 0;

                // If we fill this zero, the local merged block would be:
                // left block + this filled seat + right block
                int mergedIfFilled = leftOnes + rightOnes + 1;

                // But we cannot exceed the total number of 1s available in the whole array.
                // This cap is crucial when there is no extra 1 outside these adjacent blocks.
                int candidate = Math.min(totalOnes, mergedIfFilled);

                answer = Math.max(answer, candidate);
            }
        }

        // If there is no zero, the entire array is already all 1s.
        if (!hasZero) {
            return n;
        }

        return answer;
    }

    /**
     * Convenience wrapper using the exact problem wording.
     *
     * @param seats the binary seat reservation array
     * @return the largest possible consecutive reserved block after at most one move
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int solve(int[] seats) {
        return maxConsecutiveSeatsAfterOneMove(seats);
    }

    /**
     * Converts an int array to a readable string.
     *
     * @param arr the input array
     * @return a string representation such as [1, 0, 1]
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size across demonstrations)
     * Space complexity: O(n) per solve call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] seats1 = {1, 1, 0, 1, 0, 1, 1, 1};
        int[] seats2 = {1, 0, 1, 1, 0, 1};

        System.out.println("Sample 1:");
        System.out.println("Input:  " + solution.arrayToString(seats1));
        System.out.println("Output: " + solution.solve(seats1));
        System.out.println("Expected: 4");
        System.out.println();

        System.out.println("Sample 2:");
        System.out.println("Input:  " + solution.arrayToString(seats2));
        System.out.println("Output: " + solution.solve(seats2));
        System.out.println("Expected: 3");
        System.out.println();

        int[] seats3 = {1, 1, 1, 1};
        int[] seats4 = {0, 0, 0, 0};
        int[] seats5 = {1};
        int[] seats6 = {0};
        int[] seats7 = {1, 0, 1};
        int[] seats8 = {1, 1, 0, 1, 1};

        System.out.println("Additional Test 1:");
        System.out.println("Input:  " + solution.arrayToString(seats3));
        System.out.println("Output: " + solution.solve(seats3));
        System.out.println("Expected: 4");
        System.out.println();

        System.out.println("Additional Test 2:");
        System.out.println("Input:  " + solution.arrayToString(seats4));
        System.out.println("Output: " + solution.solve(seats4));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println("Additional Test 3:");
        System.out.println("Input:  " + solution.arrayToString(seats5));
        System.out.println("Output: " + solution.solve(seats5));
        System.out.println("Expected: 1");
        System.out.println();

        System.out.println("Additional Test 4:");
        System.out.println("Input:  " + solution.arrayToString(seats6));
        System.out.println("Output: " + solution.solve(seats6));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println("Additional Test 5:");
        System.out.println("Input:  " + solution.arrayToString(seats7));
        System.out.println("Output: " + solution.solve(seats7));
        System.out.println("Expected: 2");
        System.out.println();

        System.out.println("Additional Test 6:");
        System.out.println("Input:  " + solution.arrayToString(seats8));
        System.out.println("Output: " + solution.solve(seats8));
        System.out.println("Expected: 4");
    }
}