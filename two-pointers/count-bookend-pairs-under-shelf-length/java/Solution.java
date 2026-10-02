import java.util.*;

/*
Problem Title: Count Bookend Pairs Under Shelf Length

Problem Description:
A library is arranging decorative bookends in a display. You are given an integer array lengths
where lengths[i] is the length of the i-th bookend, and an integer shelfLimit representing the
maximum total length that can fit comfortably on one shelf section.

Two different bookends can be placed together if their combined length is less than or equal to
shelfLimit.

Return the number of distinct pairs of bookends (i, j) such that:
0 <= i < j < lengths.length
and
lengths[i] + lengths[j] <= shelfLimit

Your solution should be efficient enough for large inputs. A brute-force O(n^2) approach may work
for very small arrays, but interviewers expect you to recognize that sorting the array and using
two pointers can count valid pairs much faster.

Constraints:
- 1 <= lengths.length <= 100000
- 1 <= lengths[i] <= 1000000000
- 1 <= shelfLimit <= 2000000000
- The answer can be large, so use a 64-bit integer type if needed.

Example 1:
Input: lengths = [1, 3, 2, 2], shelfLimit = 4
Output: 4

Explanation:
After sorting, lengths becomes [1, 2, 2, 3].
Valid pairs are:
- 1 + 2
- 1 + 2
- 1 + 3
- 2 + 2
Total = 4

Example 2:
Input: lengths = [5, 1, 4, 2], shelfLimit = 5
Output: 2

Explanation:
Valid pairs are:
- 1 + 4
- 1 + 2
Total = 2
*/

public class Solution {

    /**
     * Counts how many distinct index pairs (i, j) satisfy:
     * i < j and lengths[i] + lengths[j] <= shelfLimit.
     *
     * This method uses the optimal interview-friendly approach:
     * 1. Sort the array.
     * 2. Use two pointers:
     *    - one pointer at the smallest value
     *    - one pointer at the largest value
     * 3. Count many pairs at once when possible.
     *
     * Why this works:
     * After sorting, if lengths[left] + lengths[right] <= shelfLimit,
     * then lengths[left] can also pair with every element between left+1 and right,
     * because all of them are <= lengths[right].
     *
     * @param lengths the array of bookend lengths
     * @param shelfLimit the maximum allowed combined length for a pair
     * @return the number of valid distinct pairs as a long
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scan
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public long countBookendPairs(int[] lengths, int shelfLimit) {
        // Defensive handling for completeness.
        // The constraints guarantee at least one element, but if the array is null
        // or has fewer than two elements, there cannot be any valid pair.
        if (lengths == null || lengths.length < 2) {
            return 0L;
        }

        // Sort the array so we can use the two-pointer strategy.
        // Example:
        // [1, 3, 2, 2] -> [1, 2, 2, 3]
        Arrays.sort(lengths);

        // left starts at the smallest value.
        int left = 0;

        // right starts at the largest value.
        int right = lengths.length - 1;

        // Use long because the number of pairs can be large.
        // For n = 100000, the maximum number of pairs is n * (n - 1) / 2,
        // which is about 5,000,000,000 and does not fit in int.
        long pairCount = 0L;

        // Continue while there are at least two different positions to form a pair.
        while (left < right) {
            // Use long for the sum to be extra safe, even though int would still fit
            // under the given constraints. This avoids overflow concerns in general.
            long sum = (long) lengths[left] + lengths[right];

            // Case 1:
            // If the smallest remaining value plus the largest remaining value
            // is within the limit, then the smallest value can pair with every
            // element from left+1 through right.
            if (sum <= shelfLimit) {
                // Number of valid pairs contributed by lengths[left]:
                // (left, left+1), (left, left+2), ..., (left, right)
                // Count = right - left
                pairCount += (right - left);

                // Move left forward to consider the next smallest value.
                // We have already counted all pairs involving the current left.
                left++;
            } else {
                // Case 2:
                // If lengths[left] + lengths[right] is too large,
                // then lengths[right] cannot pair with lengths[left],
                // and it also cannot pair with anything to the right of left
                // that is larger than or equal to lengths[left].
                //
                // So we must reduce the sum by moving right inward.
                right--;
            }
        }

        return pairCount;
    }

    /**
     * A simple brute-force method for verification and learning.
     * This checks every possible pair and counts the valid ones.
     *
     * This is not efficient for large inputs, but it is useful for:
     * - understanding the problem
     * - testing the optimized method on small examples
     *
     * @param lengths the array of bookend lengths
     * @param shelfLimit the maximum allowed combined length for a pair
     * @return the number of valid distinct pairs as a long
     * Time complexity: O(n^2)
     * Space complexity: O(1)
     */
    public long countBookendPairsBruteForce(int[] lengths, int shelfLimit) {
        if (lengths == null || lengths.length < 2) {
            return 0L;
        }

        long count = 0L;

        for (int i = 0; i < lengths.length; i++) {
            for (int j = i + 1; j < lengths.length; j++) {
                if ((long) lengths[i] + lengths[j] <= shelfLimit) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Creates a copy of the given array so demonstration code can preserve
     * the original input when the optimized method sorts the array in-place.
     *
     * @param array the input array to copy
     * @return a copied array, or null if the input is null
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] copyArray(int[] array) {
        if (array == null) {
            return null;
        }
        return Arrays.copyOf(array, array.length);
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * It also compares the optimized result with the brute-force result
     * for confidence in correctness on the sample cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demo inputs; dominated by the called methods
     * Space complexity: Depends on copied arrays used in the demo
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] lengths1 = {1, 3, 2, 2};
        int shelfLimit1 = 4;

        long optimized1 = solution.countBookendPairs(solution.copyArray(lengths1), shelfLimit1);
        long bruteForce1 = solution.countBookendPairsBruteForce(lengths1, shelfLimit1);

        System.out.println("Sample 1:");
        System.out.println("Input lengths = " + Arrays.toString(lengths1) + ", shelfLimit = " + shelfLimit1);
        System.out.println("Optimized result = " + optimized1);
        System.out.println("Brute-force result = " + bruteForce1);
        System.out.println("Expected result = 4");
        System.out.println();

        // Sample 2
        int[] lengths2 = {5, 1, 4, 2};
        int shelfLimit2 = 5;

        long optimized2 = solution.countBookendPairs(solution.copyArray(lengths2), shelfLimit2);
        long bruteForce2 = solution.countBookendPairsBruteForce(lengths2, shelfLimit2);

        System.out.println("Sample 2:");
        System.out.println("Input lengths = " + Arrays.toString(lengths2) + ", shelfLimit = " + shelfLimit2);
        System.out.println("Optimized result = " + optimized2);
        System.out.println("Brute-force result = " + bruteForce2);
        System.out.println("Expected result = 2");
        System.out.println();

        // Additional quick demonstration
        int[] lengths3 = {2, 2, 2, 2};
        int shelfLimit3 = 4;

        long optimized3 = solution.countBookendPairs(solution.copyArray(lengths3), shelfLimit3);

        System.out.println("Additional Example:");
        System.out.println("Input lengths = " + Arrays.toString(lengths3) + ", shelfLimit = " + shelfLimit3);
        System.out.println("Optimized result = " + optimized3);
        System.out.println("Expected result = 6");
    }
}