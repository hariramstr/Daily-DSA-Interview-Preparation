import java.util.*;

/*
 * Title: Pair Students for a Tandem Study Desk
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * A library has a row of students waiting to use tandem study desks. Each desk can seat exactly two students,
 * and the combined backpack sizes of the two students assigned to the same desk must not exceed a given limit.
 * You are given an integer array backpackSizes where backpackSizes[i] is the size of the i-th student's backpack,
 * and an integer deskLimit representing the maximum total size allowed at one desk.
 *
 * Return the minimum number of tandem desks needed to seat all students.
 *
 * Each desk may hold either one student or two students. If two students are paired together, their combined
 * backpack sizes must be less than or equal to deskLimit. You may rearrange students in any way before assigning desks.
 *
 * Your goal is to use as few desks as possible.
 *
 * This is an interview-friendly problem that rewards sorting and a two-pointer strategy. After sorting the backpack sizes,
 * you can try to pair the student with the smallest backpack with the student with the largest backpack still unassigned.
 * If they fit together, place them at one desk. Otherwise, the largest backpack must sit alone.
 *
 * Constraints:
 * - 1 <= backpackSizes.length <= 50000
 * - 1 <= backpackSizes[i] <= deskLimit <= 100000
 * - Every student must be assigned to exactly one desk
 *
 * Example 1:
 * Input: backpackSizes = [2, 3, 4, 5], deskLimit = 6
 * Output: 3
 * Explanation:
 * Pair students with backpack sizes 2 and 4 at one desk.
 * The students with sizes 3 and 5 cannot sit together because 3 + 5 = 8, which exceeds the limit.
 * So they use two more desks. Total desks needed = 3.
 *
 * Example 2:
 * Input: backpackSizes = [1, 2, 2, 3], deskLimit = 3
 * Correct Output: 3
 * Explanation:
 * The best possible arrangement still needs 3 desks:
 * - Pair 1 with 2
 * - Seat 2 alone
 * - Seat 3 alone
 *
 * Core Idea:
 * 1. Sort the backpack sizes.
 * 2. Use two pointers:
 *    - left points to the smallest unassigned backpack
 *    - right points to the largest unassigned backpack
 * 3. Always try to pair the lightest remaining student with the heaviest remaining student.
 *    - If they fit, assign them together and move both pointers.
 *    - If they do not fit, the heaviest student must sit alone, so move only the right pointer.
 * 4. Every iteration uses exactly one desk.
 */

public class Solution {

    /**
     * Computes the minimum number of tandem desks needed to seat all students.
     *
     * The method sorts the backpack sizes and then uses a greedy two-pointer strategy:
     * always try to pair the smallest remaining backpack with the largest remaining backpack.
     * If they fit within the desk limit, they share one desk. Otherwise, the largest backpack
     * must use a desk alone.
     *
     * @param backpackSizes an array where backpackSizes[i] is the size of the i-th student's backpack
     * @param deskLimit the maximum allowed combined backpack size for two students sharing one desk
     * @return the minimum number of desks required to seat all students
     *
     * Time complexity: O(n log n), due to sorting the array
     * Space complexity: O(1) extra space ignoring the sorting implementation details;
     * in practice, Arrays.sort(int[]) is efficient and uses implementation-dependent stack space
     */
    public int minDesks(int[] backpackSizes, int deskLimit) {
        // Defensive handling:
        // The problem guarantees at least one student, but this makes the method safer and beginner-friendly.
        if (backpackSizes == null || backpackSizes.length == 0) {
            return 0;
        }

        // Step 1: Sort the backpack sizes in non-decreasing order.
        // After sorting:
        // - the smallest remaining backpack will be at the left side
        // - the largest remaining backpack will be at the right side
        Arrays.sort(backpackSizes);

        // left  -> points to the lightest unassigned student
        // right -> points to the heaviest unassigned student
        int left = 0;
        int right = backpackSizes.length - 1;

        // This will count how many desks we use.
        int desks = 0;

        // Step 2: Process students until all have been assigned.
        //
        // Important observation:
        // In every loop iteration, we always assign the student at "right"
        // because that student is the heaviest remaining one.
        //
        // Why is this greedy choice correct?
        // - If the heaviest student cannot pair with the lightest student,
        //   then the heaviest student cannot pair with anyone at all,
        //   because everyone else is heavier than the lightest student.
        //   So that heaviest student must sit alone.
        // - If the heaviest student can pair with the lightest student,
        //   that is a safe and optimal pairing because it preserves as much room
        //   as possible for the remaining students.
        while (left <= right) {
            // We are about to use one desk for at least the heaviest remaining student.
            desks++;

            // Case 1:
            // If left == right, only one student remains.
            // That student must sit alone, and this desk is already counted.
            if (left == right) {
                break;
            }

            // Compute the combined size of the lightest and heaviest remaining students.
            int combined = backpackSizes[left] + backpackSizes[right];

            // Case 2:
            // If they fit together, place both at the same desk.
            if (combined <= deskLimit) {
                // Move left forward because the lightest student has now been assigned.
                left++;
            }

            // In both cases (paired or not paired), the heaviest student at "right"
            // has definitely been assigned to the current desk.
            right--;
        }

        return desks;
    }

    /**
     * Helper method to run one demonstration test case and print the result clearly.
     *
     * @param backpackSizes the backpack sizes for the test case
     * @param deskLimit the desk limit for the test case
     * @return the computed minimum number of desks
     *
     * Time complexity: O(n log n), because it delegates to the main algorithm
     * Space complexity: O(1) extra space ignoring sorting implementation details
     */
    public int demonstrateCase(int[] backpackSizes, int deskLimit) {
        // We copy the array so that printing the original input remains intuitive
        // and so that sorting inside minDesks does not modify the caller's original array.
        int[] copy = Arrays.copyOf(backpackSizes, backpackSizes.length);

        int result = minDesks(copy, deskLimit);

        System.out.println("backpackSizes = " + Arrays.toString(backpackSizes) + ", deskLimit = " + deskLimit);
        System.out.println("Minimum desks needed = " + result);
        System.out.println();

        return result;
    }

    /**
     * Main method to demonstrate the solution on sample inputs and a few additional cases.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: Depends on the number and size of demonstration test cases
     * Space complexity: Depends on copied arrays used for demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1 from the problem statement
        // Sorted: [2, 3, 4, 5]
        // Try 2 + 5 = 7 > 6 -> 5 sits alone
        // Try 2 + 4 = 6 <= 6 -> pair them
        // 3 remains alone
        // Total = 3 desks
        int result1 = solution.demonstrateCase(new int[]{2, 3, 4, 5}, 6);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println();

        // Corrected Example 2 from the problem statement
        // Sorted: [1, 2, 2, 3]
        // Try 1 + 3 = 4 > 3 -> 3 sits alone
        // Try 1 + 2 = 3 <= 3 -> pair them
        // 2 remains alone
        // Total = 3 desks
        int result2 = solution.demonstrateCase(new int[]{1, 2, 2, 3}, 3);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println();

        // Additional beginner-friendly checks
        int result3 = solution.demonstrateCase(new int[]{3}, 3);
        System.out.println("Expected: 1, Actual: " + result3);
        System.out.println();

        int result4 = solution.demonstrateCase(new int[]{1, 1, 1, 1}, 2);
        System.out.println("Expected: 2, Actual: " + result4);
        System.out.println();

        int result5 = solution.demonstrateCase(new int[]{5, 5, 5, 5}, 5);
        System.out.println("Expected: 4, Actual: " + result5);
    }
}