import java.util.*;

/*
Problem Title: Maximum Consecutive Days Within a Sleep Debt Budget

Problem Description:
You are given an integer array sleepHours where sleepHours[i] represents how many hours a person slept on day i,
and an integer target representing the recommended number of sleep hours per day.

For any day, the sleep debt for that day is:
    max(0, target - sleepHours[i])

In other words:
- Sleeping at least target hours creates no debt.
- Sleeping less than target hours adds debt equal to the shortage.

Your task is to find the length of the longest contiguous block of days whose total accumulated sleep debt
is at most budget.

Formally, for a subarray sleepHours[l..r], define its total debt as:
    sum of max(0, target - sleepHours[i]) for all i in [l, r]

Return the maximum possible value of (r - l + 1) such that this total debt is less than or equal to budget.

This problem models finding the longest sustainable streak of days where overall sleep shortage stays within
an allowed limit. Note that days with extra sleep do not reduce previously accumulated debt; they simply
contribute 0 debt for that day.

Constraints:
- 1 <= sleepHours.length <= 100000
- 0 <= sleepHours[i] <= 24
- 1 <= target <= 24
- 0 <= budget <= 1000000000

Example 1:
Input: sleepHours = [7, 5, 8, 4, 6, 7], target = 7, budget = 3
Output: 3

Explanation:
Debt per day = [0, 2, 0, 3, 1, 0]
Valid length-3 examples:
- [7, 5, 8] -> debt = 0 + 2 + 0 = 2
- [5, 8, 4] -> debt = 2 + 0 + 3 = 5 (invalid)
- [8, 4, 6] -> debt = 0 + 3 + 1 = 4 (invalid)
- [4, 6, 7] -> debt = 3 + 1 + 0 = 4 (invalid)
So the best valid length is 3.

Example 2:
Input: sleepHours = [6, 6, 7, 7, 5, 8, 6], target = 7, budget = 2
Output: 4

Explanation:
Debt per day = [1, 1, 0, 0, 2, 0, 1]
The subarray [6, 6, 7, 7] has total debt 2, so length 4 is valid.
Any longer contiguous block exceeds the budget.
Hence the answer is 4.
*/

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray whose total sleep debt
     * is at most the given budget.
     *
     * The key observation is that each day's contribution to the total debt is always
     * non-negative:
     *     debt[i] = max(0, target - sleepHours[i])
     *
     * Because all values added to the window are non-negative, we can use the classic
     * sliding window / two-pointer technique:
     * - Expand the right pointer to include more days.
     * - Keep track of the current total debt in the window.
     * - If the debt becomes too large, move the left pointer rightward until the
     *   window becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * @param sleepHours the array where sleepHours[i] is the number of hours slept on day i
     * @param target the recommended number of sleep hours per day
     * @param budget the maximum allowed total sleep debt for a contiguous block of days
     * @return the maximum length of a contiguous subarray whose total sleep debt is at most budget
     *
     * Time complexity: O(n), where n is the length of sleepHours, because each index
     * is visited at most twice (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), ignoring input storage, because only a few variables are used.
     */
    public int longestConsecutiveDaysWithinBudget(int[] sleepHours, int target, int budget) {
        int left = 0;

        // We use long for safety, even though the constraints fit in int,
        // because cumulative sums are conceptually safer in a wider type.
        long currentDebt = 0;

        // This will store the best (maximum) valid window length found so far.
        int bestLength = 0;

        // Move the right pointer from left to right, one day at a time.
        for (int right = 0; right < sleepHours.length; right++) {
            // Step 1: Compute the debt contributed by the newly included day.
            // If sleepHours[right] >= target, debt is 0.
            // Otherwise, debt is the shortage: target - sleepHours[right].
            int addedDebt = dailyDebt(sleepHours[right], target);

            // Step 2: Add this day's debt to the running total for the current window [left..right].
            currentDebt += addedDebt;

            // Step 3: If the window is invalid (debt > budget), shrink it from the left.
            // Because all daily debts are non-negative, removing days from the left can only
            // decrease or keep the same total debt, so this process is safe and efficient.
            while (currentDebt > budget) {
                // Remove the debt contribution of the day at index 'left'
                // because that day is leaving the window.
                currentDebt -= dailyDebt(sleepHours[left], target);

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 4: At this point, the window [left..right] is guaranteed valid:
            // currentDebt <= budget
            int currentLength = right - left + 1;

            // Step 5: Update the best answer if this valid window is longer.
            if (currentLength > bestLength) {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }

    /**
     * Computes the sleep debt for a single day.
     *
     * Debt is defined as:
     *     max(0, target - slept)
     *
     * Examples:
     * - slept = 8, target = 7 -> debt = 0
     * - slept = 7, target = 7 -> debt = 0
     * - slept = 5, target = 7 -> debt = 2
     *
     * @param slept the number of hours slept on a given day
     * @param target the recommended number of sleep hours
     * @return the non-negative debt for that day
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int dailyDebt(int slept, int target) {
        return Math.max(0, target - slept);
    }

    /**
     * Builds and returns the debt array corresponding to the given sleepHours array.
     * This helper is useful for demonstration, debugging, and understanding how the
     * main algorithm interprets the input.
     *
     * @param sleepHours the array of slept hours per day
     * @param target the recommended number of sleep hours
     * @return an array where each element is max(0, target - sleepHours[i])
     *
     * Time complexity: O(n), where n is the length of sleepHours
     * Space complexity: O(n), for the returned debt array
     */
    public int[] buildDebtArray(int[] sleepHours, int target) {
        int[] debt = new int[sleepHours.length];
        for (int i = 0; i < sleepHours.length; i++) {
            debt[i] = dailyDebt(sleepHours[i], target);
        }
        return debt;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * This method also prints the derived debt arrays so that a beginner can clearly
     * see how the original sleepHours input is transformed into the non-negative values
     * used by the sliding window algorithm.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n) overall for the demonstrated examples
     * Space complexity: O(n) due to printing helper arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] sleepHours1 = {7, 5, 8, 4, 6, 7};
        int target1 = 7;
        int budget1 = 3;

        int[] debt1 = solution.buildDebtArray(sleepHours1, target1);
        int result1 = solution.longestConsecutiveDaysWithinBudget(sleepHours1, target1, budget1);

        System.out.println("Example 1:");
        System.out.println("sleepHours = " + Arrays.toString(sleepHours1));
        System.out.println("target = " + target1 + ", budget = " + budget1);
        System.out.println("debt = " + Arrays.toString(debt1));
        System.out.println("Longest valid length = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Example 2
        int[] sleepHours2 = {6, 6, 7, 7, 5, 8, 6};
        int target2 = 7;
        int budget2 = 2;

        int[] debt2 = solution.buildDebtArray(sleepHours2, target2);
        int result2 = solution.longestConsecutiveDaysWithinBudget(sleepHours2, target2, budget2);

        System.out.println("Example 2:");
        System.out.println("sleepHours = " + Arrays.toString(sleepHours2));
        System.out.println("target = " + target2 + ", budget = " + budget2);
        System.out.println("debt = " + Arrays.toString(debt2));
        System.out.println("Longest valid length = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional quick sanity checks
        int[] sleepHours3 = {7, 7, 7, 7};
        int target3 = 7;
        int budget3 = 0;
        System.out.println("Additional Test 1:");
        System.out.println("sleepHours = " + Arrays.toString(sleepHours3));
        System.out.println("target = " + target3 + ", budget = " + budget3);
        System.out.println("Longest valid length = " +
                solution.longestConsecutiveDaysWithinBudget(sleepHours3, target3, budget3));
        System.out.println("Expected = 4");
        System.out.println();

        int[] sleepHours4 = {1, 1, 1};
        int target4 = 7;
        int budget4 = 5;
        System.out.println("Additional Test 2:");
        System.out.println("sleepHours = " + Arrays.toString(sleepHours4));
        System.out.println("target = " + target4 + ", budget = " + budget4);
        System.out.println("Longest valid length = " +
                solution.longestConsecutiveDaysWithinBudget(sleepHours4, target4, budget4));
        System.out.println("Expected = 0");
    }
}