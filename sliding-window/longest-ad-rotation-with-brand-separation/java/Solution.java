import java.util.*;

/*
Problem Title: Longest Ad Rotation With Brand Separation

Problem Description:
You are given an array brands where brands[i] is the brand ID of the i-th advertisement shown in a video stream, in chronological order.
A stream segment is considered valid if, for every brand that appears in that segment, any two consecutive ads from the same brand
inside the segment are at least gap + 1 positions apart. In other words, if a brand appears multiple times in the chosen contiguous
segment, there must be at least gap other ads between its repeated appearances.

Your task is to return the length of the longest valid contiguous segment.

This models an ad-serving system that wants to avoid showing the same brand too frequently while still analyzing the longest
uninterrupted portion of a schedule that obeys the cooldown rule.

A segment of length 1 is always valid. If gap = 0, then every segment is valid because repeated brands may be adjacent.

Constraints:
- 1 <= brands.length <= 200000
- 1 <= brands[i] <= 1000000000
- 0 <= gap <= brands.length

Example 1:
Input: brands = [4, 1, 2, 4, 3, 1, 5], gap = 2
Output: 7

Explanation:
In the full segment, repeated brand 4 appears at indices 0 and 3, with 2 ads between them,
and repeated brand 1 appears at indices 1 and 5, with 3 ads between them.
All repeats satisfy the rule, so the whole array is valid.

Example 2:
Input: brands = [7, 2, 7, 3, 4, 7], gap = 2
Output: 4

Explanation:
The full array is invalid because the first two occurrences of brand 7 are only 1 ad apart.
One longest valid segment is [7, 3, 4, 7], whose repeated 7s have 2 ads between them, so its length is 4.
*/

public class Solution {

    /**
     * Returns the length of the longest contiguous segment such that for every brand,
     * consecutive occurrences inside the segment are separated by at least {@code gap}
     * other ads.
     *
     * The key observation is:
     * if the same brand appears at indices prev and current, then inside a valid window
     * we must have:
     * current - prev - 1 >= gap
     * which is equivalent to:
     * current - prev > gap
     *
     * If this condition is violated, then the current window cannot include both prev and current.
     * Therefore, the left boundary of the sliding window must move to prev + 1 or further.
     *
     * We track the most recent index of each brand using a hash map.
     *
     * @param brands the array of brand IDs in chronological order
     * @param gap the minimum number of other ads required between consecutive occurrences of the same brand
     * @return the maximum length of a valid contiguous segment
     *
     * Time complexity: O(n), where n is brands.length, because each index is processed once.
     * Space complexity: O(k), where k is the number of distinct brands stored in the map.
     */
    public int longestValidSegment(int[] brands, int gap) {
        // Map from brand ID -> most recent index where that brand appeared.
        Map<Integer, Integer> lastSeenIndex = new HashMap<>();

        // Left boundary of the current sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Expand the window one element at a time using 'right'.
        for (int right = 0; right < brands.length; right++) {
            int brand = brands[right];

            // If we have seen this brand before, check whether the previous occurrence
            // is too close to the current one.
            if (lastSeenIndex.containsKey(brand)) {
                int previousIndex = lastSeenIndex.get(brand);

                // Distance rule:
                // We need at least 'gap' ads in between, so:
                // right - previousIndex - 1 >= gap
                // <=> right - previousIndex > gap
                //
                // If right - previousIndex <= gap, then both occurrences cannot stay
                // inside the same valid window.
                if (right - previousIndex <= gap) {
                    // Move the left boundary just past the previous occurrence.
                    //
                    // We use Math.max because left only moves forward.
                    // There may have been earlier violations that already pushed left
                    // beyond previousIndex + 1.
                    left = Math.max(left, previousIndex + 1);
                }
            }

            // Update the most recent position of the current brand.
            lastSeenIndex.put(brand, right);

            // Current valid window is [left, right].
            int currentLength = right - left + 1;

            // Update the best answer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * Helper method that prints an array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is arr.length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(k) per demonstrated test case for the hash map.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] brands1 = {4, 1, 2, 4, 3, 1, 5};
        int gap1 = 2;
        int result1 = solution.longestValidSegment(brands1, gap1);
        System.out.println("Example 1");
        System.out.println("brands = " + solution.arrayToString(brands1));
        System.out.println("gap = " + gap1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        int[] brands2 = {7, 2, 7, 3, 4, 7};
        int gap2 = 2;
        int result2 = solution.longestValidSegment(brands2, gap2);
        System.out.println("Example 2");
        System.out.println("brands = " + solution.arrayToString(brands2));
        System.out.println("gap = " + gap2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        int[] brands3 = {1};
        int gap3 = 5;
        int result3 = solution.longestValidSegment(brands3, gap3);
        System.out.println("Additional Test 1");
        System.out.println("brands = " + solution.arrayToString(brands3));
        System.out.println("gap = " + gap3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        int[] brands4 = {5, 5, 5, 5};
        int gap4 = 0;
        int result4 = solution.longestValidSegment(brands4, gap4);
        System.out.println("Additional Test 2");
        System.out.println("brands = " + solution.arrayToString(brands4));
        System.out.println("gap = " + gap4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 4");
        System.out.println();

        int[] brands5 = {1, 2, 1, 2, 1, 2};
        int gap5 = 1;
        int result5 = solution.longestValidSegment(brands5, gap5);
        System.out.println("Additional Test 3");
        System.out.println("brands = " + solution.arrayToString(brands5));
        System.out.println("gap = " + gap5);
        System.out.println("Output = " + result5);
        System.out.println("Expected = 6");
    }
}