import java.util.*;

/*
Problem Title: Locate First Warehouse Shelf With Required Capacity

Problem Description:
A warehouse stores bins on shelves arranged from left to right. The shelves are indexed from 0 to n - 1,
and the capacity of each shelf is given in a non-decreasing integer array capacities, where capacities[i]
is the maximum weight that shelf i can safely hold. Because the array is already sorted, lighter shelves
appear before heavier ones.

Given capacities and an integer requiredWeight, return the index of the first shelf whose capacity is
greater than or equal to requiredWeight. If no shelf can hold that weight, return -1.

Your solution should be efficient and take advantage of the sorted order of the array. A linear scan works,
but the intended solution uses binary search.

Constraints:
- 1 <= capacities.length <= 100000
- 1 <= capacities[i] <= 1000000000
- capacities is sorted in non-decreasing order
- 1 <= requiredWeight <= 1000000000

Example 1:
Input: capacities = [5, 8, 8, 12, 15], requiredWeight = 8
Output: 1
Explanation: Shelf 1 is the first shelf with capacity at least 8. Although shelf 2 also has capacity 8,
index 1 is the earliest valid answer.

Example 2:
Input: capacities = [3, 4, 6, 9], requiredWeight = 10
Output: -1
Explanation: Every shelf has capacity less than 10, so there is no valid shelf.

This problem tests whether you can find a lower bound in a sorted array, which is a common binary search
pattern in interviews.
*/

public class Solution {

    /**
     * Finds the index of the first shelf whose capacity is greater than or equal to the required weight.
     *
     * This method uses the classic "lower bound" binary search pattern:
     * we search for the leftmost position where capacities[index] >= requiredWeight.
     *
     * @param capacities the sorted array of shelf capacities in non-decreasing order
     * @param requiredWeight the weight that needs to be supported
     * @return the index of the first shelf with capacity >= requiredWeight; returns -1 if no such shelf exists
     *
     * Time complexity: O(log n), because each binary search step halves the search range.
     * Space complexity: O(1), because only a constant amount of extra memory is used.
     */
    public int findFirstShelfWithRequiredCapacity(int[] capacities, int requiredWeight) {
        // We will search within the full array range:
        // left points to the beginning of the current search interval,
        // right points to the end of the current search interval.
        int left = 0;
        int right = capacities.length - 1;

        // This variable stores the best answer found so far.
        // We initialize it to -1, meaning "no valid shelf found yet".
        int answer = -1;

        // Continue searching while the interval is valid.
        // When left becomes greater than right, the search is finished.
        while (left <= right) {
            // Compute the middle index safely.
            // We use this form instead of (left + right) / 2 to avoid overflow in general.
            int mid = left + (right - left) / 2;

            // If the middle shelf can hold the required weight,
            // then mid is a valid candidate answer.
            if (capacities[mid] >= requiredWeight) {
                // Record this index as a possible answer.
                answer = mid;

                // But we are not done yet:
                // we need the FIRST such shelf, so we continue searching on the LEFT side
                // to see if there is an earlier valid index.
                right = mid - 1;
            } else {
                // If capacities[mid] < requiredWeight, then this shelf is too weak.
                // Because the array is sorted, every shelf to the LEFT of mid is also
                // less than or equal to capacities[mid], so none of them can work either.
                // Therefore, we must search on the RIGHT side.
                left = mid + 1;
            }
        }

        // If we found at least one valid shelf, answer holds the first such index.
        // Otherwise, answer remains -1.
        return answer;
    }

    /**
     * A simple linear scan version for comparison and educational purposes.
     * This is not the intended optimal approach, but it is useful for understanding
     * the problem before learning binary search.
     *
     * @param capacities the sorted array of shelf capacities in non-decreasing order
     * @param requiredWeight the weight that needs to be supported
     * @return the index of the first shelf with capacity >= requiredWeight; returns -1 if no such shelf exists
     *
     * Time complexity: O(n), because in the worst case we may inspect every element.
     * Space complexity: O(1), because only a constant amount of extra memory is used.
     */
    public int findFirstShelfWithRequiredCapacityLinear(int[] capacities, int requiredWeight) {
        // Check each shelf from left to right.
        for (int i = 0; i < capacities.length; i++) {
            // The first shelf that can support the weight is the answer.
            if (capacities[i] >= requiredWeight) {
                return i;
            }
        }

        // If no shelf is strong enough, return -1.
        return -1;
    }

    /**
     * Prints an array in a beginner-friendly format.
     *
     * @param array the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), because each element is processed once.
     * Space complexity: O(n), because the resulting string stores all elements.
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Example verification:
     * 1) capacities = [5, 8, 8, 12, 15], requiredWeight = 8
     *    The first index with capacity >= 8 is 1, so output should be 1.
     *
     * 2) capacities = [3, 4, 6, 9], requiredWeight = 10
     *    No capacity is >= 10, so output should be -1.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(log n) per demonstrated binary-search call.
     * Space complexity: O(1) extra space per demonstrated binary-search call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1
        int[] capacities1 = {5, 8, 8, 12, 15};
        int requiredWeight1 = 8;
        int result1 = solution.findFirstShelfWithRequiredCapacity(capacities1, requiredWeight1);

        System.out.println("Example 1:");
        System.out.println("capacities = " + solution.arrayToString(capacities1));
        System.out.println("requiredWeight = " + requiredWeight1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 1");
        System.out.println();

        // Sample Input 2
        int[] capacities2 = {3, 4, 6, 9};
        int requiredWeight2 = 10;
        int result2 = solution.findFirstShelfWithRequiredCapacity(capacities2, requiredWeight2);

        System.out.println("Example 2:");
        System.out.println("capacities = " + solution.arrayToString(capacities2));
        System.out.println("requiredWeight = " + requiredWeight2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional demonstration cases for learning
        int[] capacities3 = {2, 2, 2, 2, 2};
        int requiredWeight3 = 2;
        int result3 = solution.findFirstShelfWithRequiredCapacity(capacities3, requiredWeight3);

        System.out.println("Additional Example 3:");
        System.out.println("capacities = " + solution.arrayToString(capacities3));
        System.out.println("requiredWeight = " + requiredWeight3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        int[] capacities4 = {1, 3, 5, 7, 9};
        int requiredWeight4 = 6;
        int result4 = solution.findFirstShelfWithRequiredCapacity(capacities4, requiredWeight4);

        System.out.println("Additional Example 4:");
        System.out.println("capacities = " + solution.arrayToString(capacities4));
        System.out.println("requiredWeight = " + requiredWeight4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 3");
    }
}