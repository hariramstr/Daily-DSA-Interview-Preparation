import java.util.*;

/*
Problem Title: Maximum Median Quality After Budgeted Increments

Problem Description:
You are given an integer array quality where quality[i] is the current quality score of the i-th manufactured part.
You are also given an integer budget representing the total number of increment operations available.
In one operation, you may choose any single part and increase its quality by 1.
You may distribute the operations across the array in any way.

Your goal is to maximize the median quality score of the array after using at most budget operations.
The median is defined as the middle element after sorting the array in non-decreasing order.
For an array of length n, use the element at index n / 2 in 0-based indexing after sorting.
For example, when n = 5, the median is the 3rd element; when n = 6, use the 4th element after sorting.

You may reorder the array only for the purpose of evaluating the median; the actual increment operations can be applied to any indices.
Return the maximum possible median value.

This is a hard array problem because a naive simulation of all possible increments is too slow for large inputs.
An efficient solution typically relies on sorting and using binary search on the answer, or greedily raising the median
and the values to its right while tracking the required cost.

Constraints:
- 1 <= quality.length <= 200000
- 1 <= quality[i] <= 1000000000
- 0 <= budget <= 1000000000000

Important correctness note:
For this standard median-maximization problem, to make the sorted median at least X, every element from index n/2 to n-1
that is below X must be raised up to X. Therefore:
- Example 1: quality = [1, 3, 5], budget = 4 -> answer is 7
  Sorted: [1, 3, 5]
  Need median index 1 to be at least X.
  Cost for X = 7 is:
    raise 3 -> 7 : 4
    raise 5 -> 7 : 2
    total = 6 > 4, so 7 is NOT achievable.
  Cost for X = 5 is:
    raise 3 -> 5 : 2
    raise 5 -> 5 : 0
    total = 2 <= 4, so 5 is achievable.
  Cost for X = 6 is:
    raise 3 -> 6 : 3
    raise 5 -> 6 : 1
    total = 4 <= 4, so 6 is achievable.
  Thus the correct answer is 6, not 7.

- Example 2: quality = [2, 2, 8, 9, 9], budget = 6 -> answer is 10
  Cost for X = 10:
    raise 8 -> 10 : 2
    raise 9 -> 10 : 1
    raise 9 -> 10 : 1
    total = 4 <= 6, so 10 is achievable.
  Cost for X = 11:
    3 + 2 + 2 = 7 > 6, so 11 is not achievable.
  Thus the correct answer is 10.

This implementation follows the mathematically correct interpretation and therefore prints:
- 6 for Example 1
- 10 for Example 2
*/

public class Solution {

    /**
     * Computes the maximum possible median after performing at most {@code budget} increment operations.
     *
     * Algorithm overview:
     * 1. Sort the array.
     * 2. The median position is {@code n / 2}.
     * 3. We binary search the answer value {@code targetMedian}.
     * 4. For a candidate median value X, we compute how many increments are required to make
     *    every element from the median position to the end at least X.
     *    Why this works:
     *    - After sorting, the median is the element at index n/2.
     *    - If any element in the upper half is still below X, then after sorting it can occupy the median
     *      or an earlier position in the upper half and prevent the median from reaching X.
     *    - Therefore, all elements in the upper half that are below X must be raised to X.
     * 5. If the required cost is within budget, X is feasible; otherwise it is not.
     * 6. Return the largest feasible X.
     *
     * @param quality the array of quality scores
     * @param budget the maximum number of increment operations allowed
     * @return the maximum achievable median value
     * Time complexity: O(n log n + n log(budget + maxValue))
     * Space complexity: O(log n) due to sorting stack usage in Java's primitive array sort implementation
     */
    public long maximizeMedian(int[] quality, long budget) {
        Arrays.sort(quality);

        int n = quality.length;
        int medianIndex = n / 2;

        // The median can never decrease, so the lower bound starts at the current median.
        long left = quality[medianIndex];

        // A safe upper bound:
        // In the absolute best case, if we spent every increment only on the median element,
        // the median could not exceed currentMedian + budget.
        // This is always a valid upper bound for binary search.
        long right = quality[medianIndex] + budget;

        long answer = left;

        // Standard binary search on the answer.
        // We search for the largest median value that can be achieved within the budget.
        while (left <= right) {
            long mid = left + (right - left) / 2;

            if (canAchieveMedian(quality, budget, mid)) {
                // If mid is feasible, record it and try for a larger median.
                answer = mid;
                left = mid + 1;
            } else {
                // If mid is not feasible, we must try smaller values.
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether it is possible to make the median at least {@code targetMedian}
     * using at most {@code budget} increment operations.
     *
     * Detailed reasoning:
     * - The array is assumed to already be sorted.
     * - Let m = n / 2 be the median index.
     * - To ensure the sorted median is at least targetMedian, every element from index m to n - 1
     *   that is currently smaller than targetMedian must be increased up to targetMedian.
     * - The total required increments are:
     *     sum(max(0, targetMedian - quality[i])) for i in [m, n-1]
     * - If this sum is <= budget, then targetMedian is achievable.
     *
     * Early stopping:
     * - As soon as the running cost exceeds budget, we can return false immediately.
     *   This avoids unnecessary work on large inputs.
     *
     * @param sortedQuality the sorted quality array
     * @param budget the maximum number of increment operations allowed
     * @param targetMedian the candidate median value to test
     * @return true if the target median is achievable within budget; false otherwise
     * Time complexity: O(n) in the worst case for each check
     * Space complexity: O(1)
     */
    public boolean canAchieveMedian(int[] sortedQuality, long budget, long targetMedian) {
        int n = sortedQuality.length;
        int medianIndex = n / 2;

        long required = 0L;

        // We only care about the median and everything to its right.
        // Elements to the left of the median do not help increase the median after sorting.
        for (int i = medianIndex; i < n; i++) {
            if (sortedQuality[i] < targetMedian) {
                required += targetMedian - sortedQuality[i];

                // If we already exceeded the budget, no need to continue.
                if (required > budget) {
                    return false;
                }
            }
        }

        return required <= budget;
    }

    /**
     * A small helper method to run one demonstration case and print the result.
     *
     * @param quality the input quality array
     * @param budget the available increment budget
     * @return the computed maximum median value
     * Time complexity: O(n log n + n log(budget + maxValue))
     * Space complexity: O(log n)
     */
    public long runExample(int[] quality, long budget) {
        int[] copy = Arrays.copyOf(quality, quality.length);
        return maximizeMedian(copy, budget);
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demonstration inputs
     * Space complexity: Depends on the demonstration inputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] quality1 = {1, 3, 5};
        long budget1 = 4;
        long result1 = solution.runExample(quality1, budget1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Note: Under the correct median-maximization rules, the answer is 6.");

        int[] quality2 = {2, 2, 8, 9, 9};
        long budget2 = 6;
        long result2 = solution.runExample(quality2, budget2);
        System.out.println("Example 2 result: " + result2);

        int[] quality3 = {5};
        long budget3 = 10;
        long result3 = solution.runExample(quality3, budget3);
        System.out.println("Single element example result: " + result3);

        int[] quality4 = {1, 1, 1, 1, 1, 1};
        long budget4 = 9;
        long result4 = solution.runExample(quality4, budget4);
        System.out.println("Even length example result: " + result4);

        int[] quality5 = {1, 2, 100};
        long budget5 = 50;
        long result5 = solution.runExample(quality5, budget5);
        System.out.println("Mixed values example result: " + result5);
    }
}