import java.util.*;

/*
Title: Match Guests to Spare Umbrellas
Difficulty: Easy
Topic: Two Pointers

Problem Description:
You are organizing an outdoor event. Some guests arrived without umbrellas, and you have a collection of spare umbrellas of different sizes. Each guest has a minimum umbrella size they are willing to use, and each spare umbrella has a fixed size. A guest can be assigned at most one umbrella, and each umbrella can be given to at most one guest. An umbrella can be assigned to a guest only if the umbrella size is greater than or equal to that guest's minimum required size.

Return the maximum number of guests who can receive a suitable umbrella.

This problem is about making as many valid assignments as possible, not about minimizing waste. A simple and efficient strategy is to sort both lists and use two pointers to scan through them. If the current smallest available umbrella can satisfy the current guest, assign it and move both pointers. Otherwise, skip that umbrella and try a larger one.

Constraints:
- 1 <= guests.length, umbrellas.length <= 100000
- 1 <= guests[i], umbrellas[i] <= 1000000000
- guests[i] is the minimum size required by the i-th guest
- umbrellas[i] is the size of the i-th spare umbrella

Example 1:
Input: guests = [2, 3, 1], umbrellas = [1, 1, 3]
Output: 2
Explanation: Sort both arrays: guests = [1, 2, 3], umbrellas = [1, 1, 3].
Assign size 1 to guest 1, skip the remaining size 1 for guest 2, and assign size 3 to guest 3. So 2 guests can be served.

Example 2:
Input: guests = [4, 2, 5], umbrellas = [2, 3, 4, 6]
Output: 3
Explanation: After sorting, guests = [2, 4, 5] and umbrellas = [2, 3, 4, 6].
Assign 2 to guest 2, assign 4 to guest 4, and assign 6 to guest 5. All 3 guests receive suitable umbrellas.
*/

public class Solution {

    /**
     * Computes the maximum number of guests who can receive a suitable umbrella.
     *
     * The idea is:
     * 1. Sort the guests by their minimum required umbrella size.
     * 2. Sort the umbrellas by their size.
     * 3. Use two pointers:
     *    - One pointer scans guests from the smallest requirement upward.
     *    - One pointer scans umbrellas from the smallest size upward.
     * 4. If the current umbrella is large enough for the current guest, assign it.
     *    Then move both pointers forward.
     * 5. Otherwise, the current umbrella is too small, so it cannot satisfy this guest
     *    or any later guest (because later guests need the same or larger size after sorting).
     *    Therefore, skip this umbrella and move only the umbrella pointer forward.
     *
     * This greedy strategy is optimal because we always try to satisfy the smallest
     * remaining guest with the smallest possible umbrella that works.
     *
     * @param guests the array where guests[i] is the minimum umbrella size required by the i-th guest
     * @param umbrellas the array where umbrellas[i] is the size of the i-th spare umbrella
     * @return the maximum number of guests that can be assigned suitable umbrellas
     * Time complexity: O(n log n + m log m), where n = guests.length and m = umbrellas.length
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public int maxGuestsWithUmbrellas(int[] guests, int[] umbrellas) {
        // Defensive handling for null input.
        // The problem constraints guarantee valid arrays, but this makes the method safer.
        if (guests == null || umbrellas == null) {
            return 0;
        }

        // Sort guests so we process the smallest requirement first.
        // This is important because it allows a greedy matching strategy.
        Arrays.sort(guests);

        // Sort umbrellas so we try the smallest available umbrella first.
        Arrays.sort(umbrellas);

        // guestIndex points to the current guest we are trying to satisfy.
        int guestIndex = 0;

        // umbrellaIndex points to the current umbrella we are considering.
        int umbrellaIndex = 0;

        // matchedCount stores how many successful assignments we have made.
        int matchedCount = 0;

        // Continue while we still have guests to serve and umbrellas to consider.
        while (guestIndex < guests.length && umbrellaIndex < umbrellas.length) {
            // Current guest's minimum required size.
            int requiredSize = guests[guestIndex];

            // Current umbrella's actual size.
            int availableSize = umbrellas[umbrellaIndex];

            // Case 1:
            // If the current umbrella is large enough, we can assign it to this guest.
            if (availableSize >= requiredSize) {
                // We make one valid assignment.
                matchedCount++;

                // Move to the next guest because this guest has now received an umbrella.
                guestIndex++;

                // Move to the next umbrella because this umbrella has now been used.
                umbrellaIndex++;
            } else {
                // Case 2:
                // The current umbrella is too small for the current guest.
                //
                // Because guests are sorted in non-decreasing order, all future guests
                // will require at least as much as the current guest.
                // So this umbrella cannot satisfy the current guest or any later guest.
                //
                // Therefore, the only sensible action is to discard/skip this umbrella
                // and try a larger one.
                umbrellaIndex++;
            }
        }

        // matchedCount is the maximum number of guests we managed to serve.
        return matchedCount;
    }

    /**
     * Creates and returns a copy of the given array.
     *
     * This helper is useful in demonstrations so that sorting inside the algorithm
     * does not modify the original sample arrays before we print them.
     *
     * @param array the input array to copy
     * @return a new array containing the same values as the input array
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n)
     */
    public int[] copyArray(int[] array) {
        if (array == null) {
            return null;
        }
        return Arrays.copyOf(array, array.length);
    }

    /**
     * Converts an integer array into a readable string representation.
     *
     * @param array the array to convert
     * @return a string representation of the array
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the original input arrays
     * - the computed result
     * - the expected result
     *
     * This method also implicitly verifies the examples:
     * Example 1 should produce 2.
     * Example 2 should produce 3.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n + m log m) per demonstration due to sorting
     * Space complexity: O(n + m) for copied arrays used in printing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] guests1 = {2, 3, 1};
        int[] umbrellas1 = {1, 1, 3};

        int result1 = solution.maxGuestsWithUmbrellas(
                solution.copyArray(guests1),
                solution.copyArray(umbrellas1)
        );

        System.out.println("Example 1");
        System.out.println("Guests: " + solution.arrayToString(guests1));
        System.out.println("Umbrellas: " + solution.arrayToString(umbrellas1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        // Example 2
        int[] guests2 = {4, 2, 5};
        int[] umbrellas2 = {2, 3, 4, 6};

        int result2 = solution.maxGuestsWithUmbrellas(
                solution.copyArray(guests2),
                solution.copyArray(umbrellas2)
        );

        System.out.println("Example 2");
        System.out.println("Guests: " + solution.arrayToString(guests2));
        System.out.println("Umbrellas: " + solution.arrayToString(umbrellas2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional quick demonstration
        int[] guests3 = {5, 5, 5};
        int[] umbrellas3 = {1, 5, 5, 10};

        int result3 = solution.maxGuestsWithUmbrellas(
                solution.copyArray(guests3),
                solution.copyArray(umbrellas3)
        );

        System.out.println("Additional Example");
        System.out.println("Guests: " + solution.arrayToString(guests3));
        System.out.println("Umbrellas: " + solution.arrayToString(umbrellas3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 3");
    }
}