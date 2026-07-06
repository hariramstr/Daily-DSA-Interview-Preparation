import java.util.*;

/*
 * Maximum Consecutive Days After Moving One Holiday
 *
 * Problem Description:
 * A company tracks employee availability over a planning horizon of n days, numbered from 1 to n.
 * Some days are already marked as holidays. You are given a strictly increasing integer array holidays,
 * where each value represents a holiday day. Employees want the longest possible uninterrupted block
 * of working days, where a working day is any day that is not a holiday.
 *
 * You are allowed to move at most one existing holiday to another day that is currently a working day.
 * After the move, the total number of holidays must remain the same, and no two holidays may occupy
 * the same day. Your task is to return the maximum possible length of a consecutive block of working
 * days after performing at most one such move.
 *
 * Moving a holiday means choosing one day from holidays, removing it from its current position, and
 * placing it on a different day between 1 and n that is not already a holiday. You may also choose
 * not to move any holiday.
 *
 * Constraints:
 * - 1 <= n <= 10^9
 * - 1 <= holidays.length <= min(n, 2 * 10^5)
 * - 1 <= holidays[i] <= n
 * - holidays is strictly increasing
 *
 * Key idea:
 * We never iterate over all days 1..n because n can be huge.
 * Instead, we only reason about the working-day gaps between holidays.
 *
 * If we remove one holiday at position h[i]:
 * - the working block on its left has length leftGap
 * - the working block on its right has length rightGap
 * - removing that holiday merges them and also turns that holiday day itself into a working day
 *   so the merged block length becomes leftGap + 1 + rightGap
 *
 * However, because the total number of holidays must stay the same, we must place that moved holiday
 * somewhere else on a currently working day.
 *
 * There are two possibilities:
 * 1) There exists some working day outside the merged block.
 *    Then we can place the moved holiday outside the merged block, so the merged block remains intact.
 *    Candidate answer = leftGap + 1 + rightGap
 *
 * 2) There is no working day outside the merged block.
 *    Then the moved holiday must be placed inside that merged block, which breaks it by one day.
 *    Candidate answer = leftGap + rightGap
 *
 * To test whether there exists a working day outside the merged block, we only need to know whether
 * there exists any original working block (gap) other than the two adjacent ones whose length is > 0.
 * We can answer that efficiently by precomputing prefix/suffix maximum gap lengths.
 */
public class Solution {

    /**
     * Computes the maximum possible length of a consecutive working-day block
     * after moving at most one holiday to another currently working day.
     *
     * Time complexity: O(m), where m = holidays.length
     * Space complexity: O(m)
     *
     * @param n the total number of days, numbered from 1 to n
     * @param holidays a strictly increasing array of holiday days
     * @return the maximum achievable length of a consecutive block of working days
     */
    public int maxConsecutiveDaysAfterMovingOneHoliday(int n, int[] holidays) {
        int m = holidays.length;

        // gaps[i] represents the number of working days in the i-th working block.
        //
        // There are exactly m + 1 working blocks:
        // gap 0: before the first holiday
        // gap 1: between holiday 0 and holiday 1
        // ...
        // gap m-1: between holiday m-2 and holiday m-1
        // gap m: after the last holiday
        //
        // Example: n=10, holidays=[3,8]
        // gaps = [2,4,2]
        // because:
        // days 1..2 => 2 working days
        // days 4..7 => 4 working days
        // days 9..10 => 2 working days
        int[] gaps = buildGaps(n, holidays);

        // prefixMax[i] = maximum gap value among gaps[0..i]
        int[] prefixMax = new int[m + 1];
        prefixMax[0] = gaps[0];
        for (int i = 1; i <= m; i++) {
            prefixMax[i] = Math.max(prefixMax[i - 1], gaps[i]);
        }

        // suffixMax[i] = maximum gap value among gaps[i..m]
        int[] suffixMax = new int[m + 1];
        suffixMax[m] = gaps[m];
        for (int i = m - 1; i >= 0; i--) {
            suffixMax[i] = Math.max(suffixMax[i + 1], gaps[i]);
        }

        // Baseline answer: do not move any holiday.
        // Then the best consecutive working block is simply the largest existing gap.
        int answer = prefixMax[m];

        // Now try removing each holiday one by one.
        for (int i = 0; i < m; i++) {
            // The holiday holidays[i] sits between:
            // - left working block gaps[i]
            // - right working block gaps[i + 1]
            int leftGap = gaps[i];
            int rightGap = gaps[i + 1];

            // If we remove this holiday, these two blocks merge and the holiday day itself
            // becomes a working day, so the merged block length is:
            int merged = leftGap + 1 + rightGap;

            // We must place the moved holiday somewhere else on a currently working day.
            // We want to know whether there exists at least one working day outside this merged block.
            //
            // The merged block consumes exactly the two adjacent gaps gaps[i] and gaps[i+1].
            // So any working day outside the merged block must come from some other gap.
            //
            // We therefore compute the maximum gap among all gaps except i and i+1.
            int bestOtherGap = 0;

            // Consider gaps strictly to the left of gaps[i].
            if (i - 1 >= 0) {
                bestOtherGap = Math.max(bestOtherGap, prefixMax[i - 1]);
            }

            // Consider gaps strictly to the right of gaps[i+1].
            if (i + 2 <= m) {
                bestOtherGap = Math.max(bestOtherGap, suffixMax[i + 2]);
            }

            // If bestOtherGap > 0, then there exists at least one working day outside the merged block.
            // We can place the moved holiday there and keep the merged block intact.
            //
            // Otherwise, every working day belongs to the merged block itself, so we are forced to place
            // the moved holiday inside it, reducing the best possible consecutive block by 1.
            int candidate;
            if (bestOtherGap > 0) {
                candidate = merged;
            } else {
                candidate = merged - 1;
            }

            answer = Math.max(answer, candidate);
        }

        return answer;
    }

    /**
     * Builds the array of working-day gap lengths between holidays.
     *
     * There are m holidays and therefore m + 1 working blocks.
     *
     * Time complexity: O(m), where m = holidays.length
     * Space complexity: O(m)
     *
     * @param n the total number of days
     * @param holidays a strictly increasing array of holiday days
     * @return an array gaps of length holidays.length + 1, where each value is the size of a working block
     */
    public int[] buildGaps(int n, int[] holidays) {
        int m = holidays.length;
        int[] gaps = new int[m + 1];

        // Gap before the first holiday:
        // days 1 .. holidays[0]-1
        gaps[0] = holidays[0] - 1;

        // Gaps between consecutive holidays:
        // between holidays[i-1] and holidays[i], the working days are:
        // holidays[i-1] + 1 .. holidays[i] - 1
        // count = holidays[i] - holidays[i-1] - 1
        for (int i = 1; i < m; i++) {
            gaps[i] = holidays[i] - holidays[i - 1] - 1;
        }

        // Gap after the last holiday:
        // days holidays[m-1]+1 .. n
        gaps[m] = n - holidays[m - 1];

        return gaps;
    }

    /**
     * Runs sample demonstrations from the problem statement and a few extra checks.
     *
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(total input size of demonstrated examples)
     *
     * @param args command-line arguments (unused)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 10;
        int[] holidays1 = {3, 8};
        int result1 = solution.maxConsecutiveDaysAfterMovingOneHoliday(n1, holidays1);
        System.out.println("Example 1:");
        System.out.println("n = " + n1 + ", holidays = " + Arrays.toString(holidays1));
        System.out.println("Output = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        int n2 = 15;
        int[] holidays2 = {4, 8, 12};
        int result2 = solution.maxConsecutiveDaysAfterMovingOneHoliday(n2, holidays2);
        System.out.println("Example 2:");
        System.out.println("n = " + n2 + ", holidays = " + Arrays.toString(holidays2));
        System.out.println("Output = " + result2);
        System.out.println("Expected = 7");
        System.out.println();

        int n3 = 5;
        int[] holidays3 = {1};
        int result3 = solution.maxConsecutiveDaysAfterMovingOneHoliday(n3, holidays3);
        System.out.println("Extra Example 3:");
        System.out.println("n = " + n3 + ", holidays = " + Arrays.toString(holidays3));
        System.out.println("Output = " + result3);
        System.out.println();

        int n4 = 5;
        int[] holidays4 = {3};
        int result4 = solution.maxConsecutiveDaysAfterMovingOneHoliday(n4, holidays4);
        System.out.println("Extra Example 4:");
        System.out.println("n = " + n4 + ", holidays = " + Arrays.toString(holidays4));
        System.out.println("Output = " + result4);
        System.out.println();

        int n5 = 20;
        int[] holidays5 = {2, 6, 14};
        int result5 = solution.maxConsecutiveDaysAfterMovingOneHoliday(n5, holidays5);
        System.out.println("Extra Example 5:");
        System.out.println("n = " + n5 + ", holidays = " + Arrays.toString(holidays5));
        System.out.println("Output = " + result5);
    }
}