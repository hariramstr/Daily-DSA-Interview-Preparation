import java.util.*;

/*
 * Title: Longest Checkout Stretch With One Coupon Reset
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A retail system records the coupon code used for each consecutive customer in a checkout line.
 * The array coupons represents these coupon codes in order, where coupons[i] is the code used
 * by the i-th customer.
 *
 * A checkout stretch is considered consistent if all customers in that contiguous stretch use
 * the same coupon code.
 *
 * You are allowed to perform at most one reset operation:
 * choose exactly one customer in the stretch and change that customer's coupon code to match
 * the coupon code of the rest of the stretch.
 *
 * Your task is to return the length of the longest contiguous stretch that can be made
 * consistent using at most one such reset.
 *
 * In other words, find the maximum length of a subarray that can be turned into all equal
 * values after modifying at most one element inside that subarray.
 *
 * Constraints:
 * - 1 <= coupons.length <= 200000
 * - 1 <= coupons[i] <= 1000000000
 *
 * Example 1:
 * Input: coupons = [5,5,3,5,5,5]
 * Output: 6
 * Explanation: Change the 3 at index 2 to 5. The entire array becomes a consistent stretch of length 6.
 *
 * Example 2:
 * Input: coupons = [1,2,2,3,2,2,2]
 * Output: 6
 * Explanation: Consider the subarray [2,2,3,2,2,2]. By changing the 3 to 2, this stretch becomes all 2's,
 * so the answer is 6.
 *
 * Core Idea:
 * A valid subarray that can become all equal after changing at most one element must look like:
 * - all same already, or
 * - a block of value x, then exactly one different value, then another block of value x
 *
 * Therefore, for each distinct value, we only need to know the lengths of its consecutive runs.
 * If two runs of the same value are separated by exactly one element, then we can merge them
 * by changing that one middle element.
 *
 * Example:
 * [2,2,3,2,2,2]
 * runs of 2 are:
 * - length 2
 * - length 3
 * They are separated by exactly one element (the 3), so we can merge:
 * 2 + 1 + 3 = 6
 *
 * If there is no such bridge, the best answer may simply be the longest single run.
 */
public class Solution {

    /**
     * Returns the maximum length of a contiguous subarray that can be made entirely equal
     * by modifying at most one element inside that subarray.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param coupons the array of coupon codes in checkout order
     * @return the maximum possible consistent stretch length after at most one reset
     */
    public int longestCheckoutStretchWithOneCouponReset(int[] coupons) {
        int n = coupons.length;

        // Edge case:
        // If there is only one customer, the answer is obviously 1.
        if (n == 1) {
            return 1;
        }

        // We will compress the array into runs.
        //
        // A "run" means a maximal consecutive segment of the same value.
        //
        // Example:
        // coupons = [5,5,3,5,5,5]
        // runs become:
        // value:  5   3   5
        // len:    2   1   3
        //
        // Why run compression helps:
        // A valid answer with at most one modification can only:
        // 1) stay inside one run, or
        // 2) merge two runs of the same value if they are separated by exactly one element
        //    (which means the middle run must have length 1).
        List<Integer> values = new ArrayList<>();
        List<Integer> lengths = new ArrayList<>();

        int i = 0;
        while (i < n) {
            int value = coupons[i];
            int start = i;

            // Move forward while the same value continues.
            while (i < n && coupons[i] == value) {
                i++;
            }

            // The run length is current index minus start index.
            int len = i - start;

            values.add(value);
            lengths.add(len);
        }

        int answer = 1;

        // First, any single run is already a valid consistent stretch
        // without using any reset.
        for (int len : lengths) {
            answer = Math.max(answer, len);
        }

        // Now check whether we can merge two runs of the same value
        // with exactly one different element between them.
        //
        // Pattern in runs:
        // run[k-1] = x with length a
        // run[k]   = y with length 1
        // run[k+1] = x with length b
        //
        // Then we can change that one y into x and get a + 1 + b.
        //
        // This exactly matches the problem requirement:
        // one contiguous subarray, and at most one modified element.
        for (int k = 1; k < values.size() - 1; k++) {
            int middleRunLength = lengths.get(k);

            // The middle run must be exactly one element,
            // because we are allowed to change at most one element.
            if (middleRunLength != 1) {
                continue;
            }

            int leftValue = values.get(k - 1);
            int rightValue = values.get(k + 1);

            // The left and right runs must have the same value
            // so that after changing the middle element,
            // the whole merged segment becomes uniform.
            if (leftValue == rightValue) {
                int mergedLength = lengths.get(k - 1) + 1 + lengths.get(k + 1);
                answer = Math.max(answer, mergedLength);
            }
        }

        return answer;
    }

    /**
     * A convenience wrapper with a shorter name.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param coupons the array of coupon codes
     * @return the longest consistent stretch obtainable with at most one reset
     */
    public int solve(int[] coupons) {
        return longestCheckoutStretchWithOneCouponReset(coupons);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional cases.
     *
     * Time complexity: O(total input size across demonstrations)
     * Space complexity: O(n) per call to the solver
     *
     * @param args command-line arguments (not used)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] coupons1 = {5, 5, 3, 5, 5, 5};
        int result1 = solution.longestCheckoutStretchWithOneCouponReset(coupons1);
        System.out.println("Input: " + Arrays.toString(coupons1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        int[] coupons2 = {1, 2, 2, 3, 2, 2, 2};
        int result2 = solution.longestCheckoutStretchWithOneCouponReset(coupons2);
        System.out.println("Input: " + Arrays.toString(coupons2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 6");
        System.out.println();

        int[] coupons3 = {7};
        int result3 = solution.longestCheckoutStretchWithOneCouponReset(coupons3);
        System.out.println("Input: " + Arrays.toString(coupons3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        int[] coupons4 = {4, 4, 4, 4};
        int result4 = solution.longestCheckoutStretchWithOneCouponReset(coupons4);
        System.out.println("Input: " + Arrays.toString(coupons4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 4");
        System.out.println();

        int[] coupons5 = {1, 1, 2, 3, 1, 1};
        int result5 = solution.longestCheckoutStretchWithOneCouponReset(coupons5);
        System.out.println("Input: " + Arrays.toString(coupons5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 2");
        System.out.println();

        int[] coupons6 = {2, 2, 9, 2};
        int result6 = solution.longestCheckoutStretchWithOneCouponReset(coupons6);
        System.out.println("Input: " + Arrays.toString(coupons6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 4");
    }
}