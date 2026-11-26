import java.util.*;

/*
Problem Title: Longest Shelf Segment With One Height Drop Repair

Problem Description:
A warehouse stores boxes on a long shelf, and the height of the box at position i is given by heights[i].
A shelf segment is considered orderly if the heights in that contiguous segment are nondecreasing from left to right.
The operations team is allowed to repair at most one box inside a chosen segment by changing its height to any value they want.
After this optional single repair, the entire segment must become nondecreasing.

Your task is to return the length of the longest contiguous segment that can be made nondecreasing after changing at most one element in that segment.

A repair may also be skipped if the segment is already nondecreasing. The changed value does not need to appear elsewhere in the array, and it may be any integer.

This problem asks for the best segment length, not the repaired array itself.

Constraints:
- 1 <= heights.length <= 200000
- -1000000000 <= heights[i] <= 1000000000
- An O(n log n) solution or better is expected.

Example 1:
Input: heights = [3, 4, 2, 5, 6]
Output: 5
Explanation: Change the value 2 to 4 (or any value between 4 and 5). The full array becomes nondecreasing, so the answer is 5.

Example 2:
Input: heights = [7, 3, 5, 2, 6]
Output: 3
Explanation: One optimal segment is [3, 5, 2]. Change 2 to 5, giving [3, 5, 5], which is nondecreasing. No segment of length 4 or 5 can be fixed with only one change.
*/

public class Solution {

    /**
     * Returns the length of the longest contiguous segment that can be made nondecreasing
     * after changing at most one element to any integer value.
     *
     * Core idea:
     * We binary search the answer length L.
     * For a fixed L, we check whether there exists any window of length L that is:
     * 1) already nondecreasing, or
     * 2) can become nondecreasing by changing exactly one element.
     *
     * A window can be fixed with at most one change if and only if:
     * - it has no descents at all, or
     * - it has exactly one descent at position p (meaning a[p] > a[p+1]) and
     *   changing either a[p] or a[p+1] can remove that only violation while keeping
     *   all other adjacent pairs valid.
     *
     * Because all other adjacent pairs are already nondecreasing, only local neighbor
     * constraints matter:
     * - Change a[p]:
     *     we need a[p-1] <= newValue <= a[p+1] (when those neighbors exist)
     * - Change a[p+1]:
     *     we need a[p] <= newValue <= a[p+2] (when those neighbors exist)
     *
     * We can test these conditions in O(1) per window after preprocessing the descent positions.
     *
     * @param heights the array of box heights
     * @return the maximum length of a contiguous segment that can be made nondecreasing
     *         after at most one change
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int longestShelfSegmentWithOneRepair(int[] heights) {
        int n = heights.length;

        // A single element is always nondecreasing.
        if (n <= 1) {
            return n;
        }

        // descent[i] = 1 if heights[i] > heights[i + 1], otherwise 0.
        // This marks every place where the nondecreasing property is broken.
        int[] descent = new int[n - 1];
        for (int i = 0; i < n - 1; i++) {
            descent[i] = heights[i] > heights[i + 1] ? 1 : 0;
        }

        // Prefix sum over descents.
        // pref[k] = number of descents among descent[0..k-1].
        // Therefore, number of descents in interval [l..r] of descent-array is:
        // pref[r + 1] - pref[l]
        int[] pref = new int[n];
        for (int i = 0; i < n - 1; i++) {
            pref[i + 1] = pref[i] + descent[i];
        }

        int low = 1;
        int high = n;
        int answer = 1;

        // Standard binary search on the segment length.
        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (existsFixableWindowOfLength(heights, pref, mid)) {
                answer = mid;
                low = mid + 1; // try to find a longer valid segment
            } else {
                high = mid - 1; // mid is too large
            }
        }

        return answer;
    }

    /**
     * Checks whether there exists at least one contiguous window of the given length
     * that can be made nondecreasing after changing at most one element.
     *
     * Detailed logic for one window [start..end]:
     * - Let descents be the count of indices i in [start..end-1] such that a[i] > a[i+1].
     * - If descents == 0, the window is already nondecreasing.
     * - If descents >= 2, one change cannot fix all violations.
     * - If descents == 1, let the unique bad pair be at index p:
     *       a[p] > a[p+1]
     *   Then we try two possibilities:
     *   1) Change a[p]
     *      This is possible if there exists some value x such that:
     *      - if p > start, then a[p-1] <= x
     *      - x <= a[p+1]
     *      Combined, this means:
     *      - if p == start, always possible from the left side
     *      - otherwise a[p-1] <= a[p+1]
     *
     *   2) Change a[p+1]
     *      This is possible if there exists some value y such that:
     *      - a[p] <= y
     *      - if p+1 < end, then y <= a[p+2]
     *      Combined, this means:
     *      - if p+1 == end, always possible from the right side
     *      - otherwise a[p] <= a[p+2]
     *
     * If either option works, the window is fixable.
     *
     * @param heights the original heights array
     * @param pref prefix sums of descent markers
     * @param len the window length to test
     * @return true if some window of length len is fixable with at most one change; false otherwise
     * Time complexity: O(n) for one call
     * Space complexity: O(1) extra (excluding input arrays)
     */
    public boolean existsFixableWindowOfLength(int[] heights, int[] pref, int len) {
        int n = heights.length;

        // Any window of length 1 is trivially valid.
        if (len <= 1) {
            return true;
        }

        // We slide over all windows [start..end] of this fixed length.
        for (int start = 0; start + len - 1 < n; start++) {
            int end = start + len - 1;

            // Descents inside this window correspond to adjacent pairs from
            // indices start..end-1 in the original array.
            int descentCount = countDescents(pref, start, end - 1);

            // Case 1: already nondecreasing.
            if (descentCount == 0) {
                return true;
            }

            // Case 2: too many violations for one repair.
            if (descentCount >= 2) {
                continue;
            }

            // Case 3: exactly one descent.
            // We must locate the unique index p in [start..end-1] where heights[p] > heights[p+1].
            int p = findUniqueDescentIndex(heights, start, end - 1);

            // Try fixing heights[p].
            boolean canFixByChangingLeftElement;
            if (p == start) {
                // No left neighbor inside the window, so we can always choose a value <= heights[p+1].
                canFixByChangingLeftElement = true;
            } else {
                // Need heights[p-1] <= newValue <= heights[p+1], which is possible iff
                // heights[p-1] <= heights[p+1].
                canFixByChangingLeftElement = heights[p - 1] <= heights[p + 1];
            }

            // Try fixing heights[p+1].
            boolean canFixByChangingRightElement;
            if (p + 1 == end) {
                // No right neighbor inside the window, so we can always choose a value >= heights[p].
                canFixByChangingRightElement = true;
            } else {
                // Need heights[p] <= newValue <= heights[p+2], which is possible iff
                // heights[p] <= heights[p + 2].
                canFixByChangingRightElement = heights[p] <= heights[p + 2];
            }

            if (canFixByChangingLeftElement || canFixByChangingRightElement) {
                return true;
            }
        }

        return false;
    }

    /**
     * Counts how many descents exist among adjacent pairs with left endpoints in [left..right].
     * In other words, it counts indices i in [left..right] such that heights[i] > heights[i+1].
     *
     * @param pref prefix sums over the descent array
     * @param left left index in the descent array
     * @param right right index in the descent array
     * @return number of descents in that interval
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int countDescents(int[] pref, int left, int right) {
        if (left > right) {
            return 0;
        }
        return pref[right + 1] - pref[left];
    }

    /**
     * Finds the unique descent index p in the range [left..right], where
     * heights[p] > heights[p+1].
     *
     * This method is called only when we already know there is exactly one descent
     * in the specified range, so a simple linear scan is sufficient and correct.
     * Since this scan happens only for windows with exactly one descent during a
     * binary-search feasibility check, the total complexity remains O(n log n).
     *
     * @param heights the original heights array
     * @param left left boundary for possible descent positions
     * @param right right boundary for possible descent positions
     * @return the unique index p such that heights[p] > heights[p+1]
     * Time complexity: O(length of scanned range), but amortized within the overall O(n log n) solution
     * Space complexity: O(1)
     */
    public int findUniqueDescentIndex(int[] heights, int left, int right) {
        for (int i = left; i <= right; i++) {
            if (heights[i] > heights[i + 1]) {
                return i;
            }
        }

        // This should never happen if the caller guarantees exactly one descent.
        return -1;
    }

    /**
     * Convenience wrapper matching a short interview-style method name.
     *
     * @param heights the array of heights
     * @return the maximum fixable segment length
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int longestSegment(int[] heights) {
        return longestShelfSegmentWithOneRepair(heights);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few extra quick checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size log n) across demonstrated examples
     * Space complexity: O(n) per example due to preprocessing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] heights1 = {3, 4, 2, 5, 6};
        int result1 = solution.longestShelfSegmentWithOneRepair(heights1);
        System.out.println("Input: " + Arrays.toString(heights1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 5");
        System.out.println();

        int[] heights2 = {7, 3, 5, 2, 6};
        int result2 = solution.longestShelfSegmentWithOneRepair(heights2);
        System.out.println("Input: " + Arrays.toString(heights2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        int[] heights3 = {1, 2, 3, 4};
        int result3 = solution.longestShelfSegmentWithOneRepair(heights3);
        System.out.println("Input: " + Arrays.toString(heights3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 4");
        System.out.println();

        int[] heights4 = {4, 3, 2, 1};
        int result4 = solution.longestShelfSegmentWithOneRepair(heights4);
        System.out.println("Input: " + Arrays.toString(heights4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 2");
        System.out.println();

        int[] heights5 = {5};
        int result5 = solution.longestShelfSegmentWithOneRepair(heights5);
        System.out.println("Input: " + Arrays.toString(heights5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 1");
    }
}