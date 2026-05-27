/*
 * Checkered Flag Bit Counter
 * ==========================
 * Difficulty: Easy
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given a list of race car IDs represented as non-negative integers.
 * A race car is considered 'flagged' if the number of set bits (1s) in its
 * binary representation is strictly greater than the number of unset bits (0s)
 * when considering only the bits from position 0 up to the position of the
 * most significant bit (inclusive).
 *
 * For example:
 *   - 7  in binary is '111'  → 3 set bits, 0 unset bits → flagged
 *   - 5  in binary is '101'  → 2 set bits, 1 unset bit  → flagged
 *   - 4  in binary is '100'  → 1 set bit,  2 unset bits → NOT flagged
 *   - 6  in binary is '110'  → 2 set bits, 1 unset bit  → flagged
 *   - 1  in binary is '1'    → 1 set bit,  0 unset bits → flagged
 *   - 0  is considered NOT flagged (special case, no set bits)
 *
 * Given an integer array carIds, return the count of flagged car IDs.
 *
 * Constraints:
 *   - 1 <= carIds.length <= 10^4
 *   - 0 <= carIds[i] <= 10^6
 *   - The number 0 is considered NOT flagged
 *
 * Example 1:
 *   Input:  carIds = [7, 5, 4, 6, 1]
 *   Output: 4
 *   Explanation: 7 (flagged), 5 (flagged), 4 (not flagged), 6 (flagged), 1 (flagged)
 *
 * Example 2:
 *   Input:  carIds = [0, 2, 8, 15]
 *   Output: 1
 *   Explanation: 0 (not flagged), 2 (not flagged), 8 (not flagged), 15 (flagged)
 */

public class Solution {

    /**
     * Counts how many car IDs in the array are "flagged".
     *
     * A car ID is flagged if the count of set bits (1s) in its binary
     * representation is STRICTLY GREATER than the count of unset bits (0s),
     * considering only bits from position 0 up to the most significant bit.
     *
     * Special case: 0 is never flagged.
     *
     * @param carIds  an array of non-negative integers representing car IDs
     * @return        the number of flagged car IDs
     *
     * Time Complexity:  O(n * log(max_value)) where n = carIds.length and
     *                   log(max_value) represents the number of bits needed
     *                   to represent the largest value (at most ~20 bits for 10^6)
     * Space Complexity: O(1) — only a few integer variables are used
     */
    public int countFlaggedCars(int[] carIds) {
        // Step 1: Initialize a counter to keep track of how many cars are flagged
        int flaggedCount = 0;

        // Step 2: Iterate through each car ID in the array
        for (int carId : carIds) {
            // Step 3: Check if this particular car ID is flagged
            if (isFlagged(carId)) {
                // Step 4: If it is flagged, increment our counter
                flaggedCount++;
            }
        }

        // Step 5: Return the total count of flagged cars
        return flaggedCount;
    }

    /**
     * Determines whether a single car ID is "flagged".
     *
     * A number is flagged if:
     *   - It is NOT zero (0 is never flagged by definition)
     *   - The number of 1-bits (set bits) is strictly greater than
     *     the number of 0-bits (unset bits) within the range from
     *     bit position 0 to the most significant bit position (inclusive).
     *
     * How we determine the total bit length:
     *   We use Integer.toBinaryString(n) which gives us the binary string
     *   WITHOUT leading zeros. So the length of this string is exactly
     *   the number of bits from position 0 to the most significant bit.
     *
     * Example trace for carId = 6 ('110'):
     *   - binaryString = "110"
     *   - totalBits    = 3
     *   - setBits      = Integer.bitCount(6) = 2  (two 1s)
     *   - unsetBits    = 3 - 2 = 1
     *   - 2 > 1 → true → flagged!
     *
     * Example trace for carId = 4 ('100'):
     *   - binaryString = "100"
     *   - totalBits    = 3
     *   - setBits      = Integer.bitCount(4) = 1  (one 1)
     *   - unsetBits    = 3 - 1 = 2
     *   - 1 > 2 → false → NOT flagged
     *
     * Example trace for carId = 0:
     *   - Special case: immediately return false
     *
     * @param carId  a single non-negative integer car ID
     * @return       true if the car ID is flagged, false otherwise
     *
     * Time Complexity:  O(log(carId)) — proportional to the number of bits
     * Space Complexity: O(log(carId)) — for the binary string representation
     */
    public boolean isFlagged(int carId) {
        // Step 1: Handle the special case — 0 is never flagged
        if (carId == 0) {
            return false;
        }

        // Step 2: Get the binary string representation of carId.
        // Integer.toBinaryString() returns the binary without leading zeros.
        // For example: toBinaryString(6) = "110", toBinaryString(1) = "1"
        String binaryString = Integer.toBinaryString(carId);

        // Step 3: The total number of bits we consider is the length of the
        // binary string (from bit 0 up to and including the most significant bit).
        // Example: "110" has length 3, so we consider 3 bits total.
        int totalBits = binaryString.length();

        // Step 4: Count the number of set bits (1s) using Java's built-in method.
        // Integer.bitCount(n) efficiently counts the number of 1-bits in n.
        // Example: Integer.bitCount(6) = 2 because 6 = 110 in binary (two 1s)
        int setBits = Integer.bitCount(carId);

        // Step 5: Calculate the number of unset bits (0s).
        // Since totalBits = setBits + unsetBits, we get:
        // unsetBits = totalBits - setBits
        // Example: totalBits=3, setBits=2 → unsetBits = 3 - 2 = 1
        int unsetBits = totalBits - setBits;

        // Step 6: The car is flagged if set bits are STRICTLY GREATER than unset bits.
        // Return true if setBits > unsetBits, false otherwise.
        return setBits > unsetBits;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through both provided examples and verifies correctness.
     *
     * @param args  command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Create an instance of Solution to call our methods
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1: carIds = [7, 5, 4, 6, 1]
        // Expected Output: 4
        // ---------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[] carIds1 = {7, 5, 4, 6, 1};

        // Let's trace through each car ID manually:
        System.out.println("Tracing each car ID:");
        for (int id : carIds1) {
            if (id == 0) {
                System.out.println("  carId=" + id + " → special case, NOT flagged");
            } else {
                String bin = Integer.toBinaryString(id);
                int total = bin.length();
                int set = Integer.bitCount(id);
                int unset = total - set;
                boolean flagged = solution.isFlagged(id);
                System.out.println("  carId=" + id
                        + " binary='" + bin + "'"
                        + " setBits=" + set
                        + " unsetBits=" + unset
                        + " → " + (flagged ? "FLAGGED" : "not flagged"));
            }
        }

        int result1 = solution.countFlaggedCars(carIds1);
        System.out.println("Result: " + result1);
        System.out.println("Expected: 4");
        System.out.println("Correct: " + (result1 == 4));
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2: carIds = [0, 2, 8, 15]
        // Expected Output: 1
        // ---------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int[] carIds2 = {0, 2, 8, 15};

        // Let's trace through each car ID manually:
        System.out.println("Tracing each car ID:");
        for (int id : carIds2) {
            if (id == 0) {
                System.out.println("  carId=" + id + " → special case, NOT flagged");
            } else {
                String bin = Integer.toBinaryString(id);
                int total = bin.length();
                int set = Integer.bitCount(id);
                int unset = total - set;
                boolean flagged = solution.isFlagged(id);
                System.out.println("  carId=" + id
                        + " binary='" + bin + "'"
                        + " setBits=" + set
                        + " unsetBits=" + unset
                        + " → " + (flagged ? "FLAGGED" : "not flagged"));
            }
        }

        int result2 = solution.countFlaggedCars(carIds2);
        System.out.println("Result: " + result2);
        System.out.println("Expected: 1");
        System.out.println("Correct: " + (result2 == 1));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional edge case tests
        // ---------------------------------------------------------------
        System.out.println("=== Additional Edge Cases ===");

        // Edge case: single element array with 0
        int[] edgeCase1 = {0};
        System.out.println("carIds=[0] → Result: " + solution.countFlaggedCars(edgeCase1)
                + " (Expected: 0)");

        // Edge case: single element array with 1 ('1' → 1 set, 0 unset → flagged)
        int[] edgeCase2 = {1};
        System.out.println("carIds=[1] → Result: " + solution.countFlaggedCars(edgeCase2)
                + " (Expected: 1)");

        // Edge case: number 2 ('10' → 1 set, 1 unset → NOT flagged, not strictly greater)
        int[] edgeCase3 = {2};
        System.out.println("carIds=[2] → Result: " + solution.countFlaggedCars(edgeCase3)
                + " (Expected: 0)");

        // Edge case: number 3 ('11' → 2 set, 0 unset → flagged)
        int[] edgeCase4 = {3};
        System.out.println("carIds=[3] → Result: " + solution.countFlaggedCars(edgeCase4)
                + " (Expected: 1)");

        // Edge case: large number 1000000 in binary
        int largeNum = 1000000;
        String largeBin = Integer.toBinaryString(largeNum);
        int largeSet = Integer.bitCount(largeNum);
        int largeUnset = largeBin.length() - largeSet;
        System.out.println("carId=1000000 binary='" + largeBin + "'"
                + " setBits=" + largeSet
                + " unsetBits=" + largeUnset
                + " → " + (solution.isFlagged(largeNum) ? "FLAGGED" : "not flagged"));
    }
}