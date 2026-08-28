import java.util.*;

/*
 * Title: Find Insertion Slot for a Sorted Event Timeline
 * Difficulty: Easy
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given a sorted array `times` representing event start times in minutes from the beginning
 * of the day. The array is sorted in non-decreasing order, and duplicate values may exist because
 * multiple events can start at the same minute. You are also given an integer `target`, representing
 * the start time of a new event.
 *
 * Return the index where `target` should be inserted so that the array remains sorted after insertion.
 * If `target` already exists, return the leftmost index where it appears. In other words, you must
 * find the first position `i` such that `times[i] >= target`. If no such position exists, return
 * `times.length`.
 *
 * Your solution should run in O(log n) time, which makes binary search the intended approach.
 *
 * Constraints:
 * - 0 <= times.length <= 100000
 * - 0 <= times[i] <= 1440
 * - times is sorted in non-decreasing order
 * - 0 <= target <= 1440
 *
 * Example 1:
 * Input: times = [15, 30, 30, 45, 90], target = 30
 * Output: 1
 * Explanation: The value 30 already exists, and the leftmost valid insertion position is index 1.
 *
 * Example 2:
 * Input: times = [10, 20, 40, 80], target = 35
 * Output: 2
 * Explanation: Inserting 35 at index 2 gives [10, 20, 35, 40, 80], which is still sorted.
 */

public class Solution {

    /**
     * Finds the leftmost index where the target can be inserted into the sorted array
     * so that the array remains sorted.
     *
     * This method returns the first index i such that times[i] >= target.
     * If every value in the array is smaller than target, it returns times.length.
     *
     * @param times the sorted array of event start times in non-decreasing order
     * @param target the new event start time to insert
     * @return the leftmost valid insertion index for target
     *
     * Time Complexity: O(log n), because binary search halves the search range each step.
     * Space Complexity: O(1), because only a constant amount of extra space is used.
     */
    public int searchInsert(int[] times, int target) {
        // We use a classic "lower bound" binary search.
        //
        // Goal:
        // Find the FIRST index where times[index] >= target.
        //
        // Why this works:
        // - If target already exists multiple times, we want the LEFTMOST one.
        // - If target does not exist, we want the position where it can be inserted.
        //
        // Search range is [left, right), meaning:
        // - left is inclusive
        // - right is exclusive
        //
        // Initially:
        // - left starts at 0
        // - right starts at times.length
        //
        // This setup is very convenient because:
        // - it naturally handles empty arrays
        // - it naturally returns times.length when target is larger than all elements
        int left = 0;
        int right = times.length;

        // Continue searching while there is still a valid range to inspect.
        while (left < right) {
            // Compute the middle index safely.
            // This avoids potential overflow compared to (left + right) / 2,
            // although overflow is not a practical issue here due to constraints.
            int mid = left + (right - left) / 2;

            // Now compare the middle value with the target.
            if (times[mid] >= target) {
                // If times[mid] is greater than or equal to target,
                // then mid could be the answer.
                //
                // But there might be an even earlier valid index on the left side.
                // So we keep mid in the search space by moving right to mid.
                right = mid;
            } else {
                // If times[mid] is strictly less than target,
                // then mid cannot be the answer,
                // and neither can anything to the left of mid.
                //
                // So we move left to mid + 1.
                left = mid + 1;
            }
        }

        // At the end of the loop, left == right.
        // This is the first position where times[index] >= target,
        // or times.length if no such position exists.
        return left;
    }

    /**
     * Builds and returns a new array showing what the timeline would look like
     * after inserting target at the correct sorted position.
     *
     * This helper method is used only for demonstration in main.
     *
     * @param times the original sorted array of event start times
     * @param target the new event start time to insert
     * @return a new sorted array with target inserted at the correct position
     *
     * Time Complexity: O(n), because elements may need to be copied into a new array.
     * Space Complexity: O(n), because a new array of size n + 1 is created.
     */
    public int[] insertAtCorrectPosition(int[] times, int target) {
        // First, find the correct insertion index using binary search.
        int index = searchInsert(times, target);

        // Create a new array one element larger than the original.
        int[] result = new int[times.length + 1];

        // Copy all elements before the insertion index.
        for (int i = 0; i < index; i++) {
            result[i] = times[i];
        }

        // Place the target at the insertion index.
        result[index] = target;

        // Copy the remaining original elements after the inserted target.
        for (int i = index; i < times.length; i++) {
            result[i + 1] = times[i];
        }

        return result;
    }

    /**
     * Converts an integer array into a readable string representation.
     *
     * This helper method is used for clean output in the demonstration.
     *
     * @param array the array to convert to a string
     * @return a string representation of the array, such as [1, 2, 3]
     *
     * Time Complexity: O(n), because each element is processed once.
     * Space Complexity: O(n), because the output string stores all elements.
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time Complexity: O(log n) for each search demonstration, plus O(n) for any array printing
     * or insertion visualization.
     * Space Complexity: O(1) for search only, or O(n) when creating demonstration arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // times = [15, 30, 30, 45, 90], target = 30
        // Expected output: 1
        int[] times1 = {15, 30, 30, 45, 90};
        int target1 = 30;
        int result1 = solution.searchInsert(times1, target1);

        System.out.println("Example 1");
        System.out.println("times = " + solution.arrayToString(times1));
        System.out.println("target = " + target1);
        System.out.println("Insertion index = " + result1);
        System.out.println("Timeline after insertion = " + solution.arrayToString(solution.insertAtCorrectPosition(times1, target1)));
        System.out.println("Expected = 1");
        System.out.println();

        // Example 2 from the problem statement:
        // times = [10, 20, 40, 80], target = 35
        // Expected output: 2
        int[] times2 = {10, 20, 40, 80};
        int target2 = 35;
        int result2 = solution.searchInsert(times2, target2);

        System.out.println("Example 2");
        System.out.println("times = " + solution.arrayToString(times2));
        System.out.println("target = " + target2);
        System.out.println("Insertion index = " + result2);
        System.out.println("Timeline after insertion = " + solution.arrayToString(solution.insertAtCorrectPosition(times2, target2)));
        System.out.println("Expected = 2");
        System.out.println();

        // Additional beginner-friendly checks:

        // Case 3: Insert at the beginning
        int[] times3 = {20, 40, 60};
        int target3 = 10;
        System.out.println("Case 3");
        System.out.println("times = " + solution.arrayToString(times3));
        System.out.println("target = " + target3);
        System.out.println("Insertion index = " + solution.searchInsert(times3, target3));
        System.out.println("Timeline after insertion = " + solution.arrayToString(solution.insertAtCorrectPosition(times3, target3)));
        System.out.println();

        // Case 4: Insert at the end
        int[] times4 = {20, 40, 60};
        int target4 = 100;
        System.out.println("Case 4");
        System.out.println("times = " + solution.arrayToString(times4));
        System.out.println("target = " + target4);
        System.out.println("Insertion index = " + solution.searchInsert(times4, target4));
        System.out.println("Timeline after insertion = " + solution.arrayToString(solution.insertAtCorrectPosition(times4, target4)));
        System.out.println();

        // Case 5: Empty array
        int[] times5 = {};
        int target5 = 50;
        System.out.println("Case 5");
        System.out.println("times = " + solution.arrayToString(times5));
        System.out.println("target = " + target5);
        System.out.println("Insertion index = " + solution.searchInsert(times5, target5));
        System.out.println("Timeline after insertion = " + solution.arrayToString(solution.insertAtCorrectPosition(times5, target5)));
        System.out.println();

        // Case 6: All values equal to target
        int[] times6 = {30, 30, 30, 30};
        int target6 = 30;
        System.out.println("Case 6");
        System.out.println("times = " + solution.arrayToString(times6));
        System.out.println("target = " + target6);
        System.out.println("Insertion index = " + solution.searchInsert(times6, target6));
        System.out.println("Timeline after insertion = " + solution.arrayToString(solution.insertAtCorrectPosition(times6, target6)));
        System.out.println();
    }
}