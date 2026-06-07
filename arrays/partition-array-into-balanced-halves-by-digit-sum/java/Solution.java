/*
 * Title: Partition Array into Balanced Halves by Digit Sum
 *
 * Problem Description:
 * You are given an integer array `nums` of even length `n`. Your task is to partition
 * the array into two groups of exactly `n / 2` elements each, such that the absolute
 * difference between the total digit sum of the first group and the total digit sum
 * of the second group is minimized.
 *
 * The digit sum of a number is the sum of all its individual digits.
 * For example, the digit sum of 123 is 1 + 2 + 3 = 6.
 *
 * Return the minimum possible absolute difference between the total digit sums
 * of the two groups.
 *
 * Constraints:
 * - 2 <= nums.length <= 40 and nums.length is even
 * - 1 <= nums[i] <= 10^5
 *
 * Example 1:
 * Input: nums = [14, 21, 35, 9]
 * Output: 1
 *
 * Example 2:
 * Input: nums = [10, 22, 33, 45, 50, 67]
 * Output: 0
 */

import java.util.*;

/**
 * Solution class for partitioning an array into two balanced halves by digit sum.
 *
 * <p>Approach: Meet-in-the-Middle
 * Since n can be up to 40, a brute-force approach of checking all 2^40 subsets is too slow.
 * Instead, we use the "Meet in the Middle" technique:
 * 1. Split the digit-sum array into two halves.
 * 2. Enumerate all subsets of each half, recording the sum and count of chosen elements.
 * 3. For each subset of the first half that picks k elements, we need n/2 - k elements
 *    from the second half. We look up the best matching subset sum from the second half.
 * 4. The answer is the minimum |totalDigitSum - 2 * combinedGroupSum| over all valid splits.
 */
public class Solution {

    /**
     * Computes the digit sum of a non-negative integer.
     *
     * @param num the integer whose digit sum is to be computed (>= 1)
     * @return the sum of all digits of num
     * Time complexity: O(d) where d is the number of digits in num
     * Space complexity: O(1)
     */
    public int digitSum(int num) {
        int sum = 0;
        // Keep extracting the last digit and add it to sum
        while (num > 0) {
            sum += num % 10; // last digit
            num /= 10;       // remove last digit
        }
        return sum;
    }

    /**
     * Finds the minimum absolute difference between the total digit sums of two groups,
     * where each group has exactly n/2 elements.
     *
     * <p>Uses Meet-in-the-Middle:
     * - Split digit sums into left half and right half.
     * - Enumerate all subsets of each half, storing (count, sum) pairs.
     * - For each left subset choosing k elements with sum s, find right subsets
     *   choosing (n/2 - k) elements, and minimize |totalSum - 2*(s + rightSum)|.
     *
     * @param nums the input integer array of even length
     * @return the minimum absolute difference between the two groups' total digit sums
     * Time complexity: O(2^(n/2) * log(2^(n/2))) = O(2^(n/2) * n/2)
     * Space complexity: O(2^(n/2)) for storing subset information
     */
    public int minimumDifference(int[] nums) {
        int n = nums.length;
        int half = n / 2;

        // Step 1: Compute digit sums for all elements
        int[] ds = new int[n];
        int totalDigitSum = 0;
        for (int i = 0; i < n; i++) {
            ds[i] = digitSum(nums[i]);
            totalDigitSum += ds[i];
        }

        // Step 2: Split digit sums into left half (indices 0..half-1)
        // and right half (indices half..n-1)
        int leftSize = half;  // number of elements in left portion
        int rightSize = n - half; // number of elements in right portion

        // Step 3: Enumerate all subsets of the RIGHT half
        // For each subset, record how many elements are chosen and their total digit sum.
        // We store: for each count k, a sorted list of achievable sums.
        // This allows binary search later.
        // rightMap: count -> sorted list of sums
        Map<Integer, List<Integer>> rightMap = new HashMap<>();

        // There are 2^rightSize subsets of the right half
        int rightSubsets = 1 << rightSize;
        for (int mask = 0; mask < rightSubsets; mask++) {
            int count = 0;
            int sum = 0;
            // Iterate over each bit of mask
            for (int bit = 0; bit < rightSize; bit++) {
                if ((mask & (1 << bit)) != 0) {
                    // This element (at index half + bit) is included in the subset
                    count++;
                    sum += ds[half + bit];
                }
            }
            // Store this (count, sum) pair
            rightMap.computeIfAbsent(count, x -> new ArrayList<>()).add(sum);
        }

        // Step 4: Sort each list in rightMap for binary search
        for (List<Integer> list : rightMap.values()) {
            Collections.sort(list);
        }

        // Step 5: Enumerate all subsets of the LEFT half
        // For each left subset choosing k elements with digit sum s,
        // we need to pick (half - k) elements from the right half.
        // The combined group 1 sum = s + rightSum.
        // We want to minimize |totalDigitSum - 2 * (s + rightSum)|.
        int minDiff = Integer.MAX_VALUE;

        int leftSubsets = 1 << leftSize;
        for (int mask = 0; mask < leftSubsets; mask++) {
            int leftCount = 0;
            int leftSum = 0;
            // Compute count and sum for this left subset
            for (int bit = 0; bit < leftSize; bit++) {
                if ((mask & (1 << bit)) != 0) {
                    leftCount++;
                    leftSum += ds[bit];
                }
            }

            // We need to pick (half - leftCount) elements from the right half
            int needed = half - leftCount;

            // Check if there are right subsets with exactly 'needed' elements
            if (!rightMap.containsKey(needed)) {
                continue; // no valid right subset for this left subset
            }

            List<Integer> rightSums = rightMap.get(needed);

            // We want to minimize |totalDigitSum - 2 * (leftSum + rightSum)|
            // Let target = totalDigitSum - 2 * leftSum
            // We want rightSum as close to target / 2 as possible,
            // i.e., minimize |target - 2 * rightSum|
            // Equivalently, find rightSum closest to target / 2.0

            // Use binary search to find the best rightSum
            // target = totalDigitSum - 2 * leftSum
            // We want rightSum closest to target / 2.0
            // But since we're dealing with integers, let's search for the value
            // that minimizes |totalDigitSum - 2*(leftSum + rightSum)|

            // Binary search: find the insertion point for (totalDigitSum - 2*leftSum) / 2.0
            // We search for the value closest to idealRightSum = (totalDigitSum - 2*leftSum) / 2.0

            // Let's compute the ideal right sum (as a double for comparison)
            double idealRightSum = (totalDigitSum - 2.0 * leftSum) / 2.0;

            // Binary search for the position where idealRightSum would be inserted
            int lo = 0, hi = rightSums.size() - 1;
            int pos = Collections.binarySearch(rightSums, (int) Math.floor(idealRightSum));
            if (pos < 0) {
                // binarySearch returns -(insertion point) - 1 when not found
                pos = -pos - 1; // insertion point
            }

            // Check candidates around the insertion point (pos-1 and pos)
            for (int p = Math.max(0, pos - 1); p <= Math.min(rightSums.size() - 1, pos + 1); p++) {
                int rightSum = rightSums.get(p);
                int diff = Math.abs(totalDigitSum - 2 * (leftSum + rightSum));
                minDiff = Math.min(minDiff, diff);
            }
        }

        return minDiff;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // -------------------------------------------------------
        // Example 1: nums = [14, 21, 35, 9]
        // Digit sums: [5, 3, 8, 9], total = 25
        // Best partition: Group1=[14,35] sums=5+8=13, Group2=[21,9] sums=3+9=12
        // |13 - 12| = 1
        // -------------------------------------------------------
        int[] nums1 = {14, 21, 35, 9};
        int result1 = sol.minimumDifference(nums1);
        System.out.println("Example 1:");
        System.out.println("Input: nums = [14, 21, 35, 9]");
        System.out.println("Expected Output: 1");
        System.out.println("Actual Output:   " + result1);
        System.out.println();

        // -------------------------------------------------------
        // Example 2: nums = [10, 22, 33, 45, 50, 67]
        // Digit sums: [1, 4, 6, 9, 5, 13], total = 38
        // Best partition achieves difference 0
        // -------------------------------------------------------
        int[] nums2 = {10, 22, 33, 45, 50, 67};
        int result2 = sol.minimumDifference(nums2);
        System.out.println("Example 2:");
        System.out.println("Input: nums = [10, 22, 33, 45, 50, 67]");
        System.out.println("Expected Output: 0");
        System.out.println("Actual Output:   " + result2);
        System.out.println();

        // -------------------------------------------------------
        // Additional test: nums = [1, 2, 3, 4]
        // Digit sums: [1, 2, 3, 4], total = 10
        // Best: Group1=[2,3]=5, Group2=[1,4]=5 -> diff=0
        // -------------------------------------------------------
        int[] nums3 = {1, 2, 3, 4};
        int result3 = sol.minimumDifference(nums3);
        System.out.println("Additional Test 1:");
        System.out.println("Input: nums = [1, 2, 3, 4]");
        System.out.println("Expected Output: 0");
        System.out.println("Actual Output:   " + result3);
        System.out.println();

        // -------------------------------------------------------
        // Additional test: nums = [100000, 99999]
        // Digit sums: [1, 45], total = 46
        // Only one partition: Group1=[100000]=1, Group2=[99999]=45
        // |1 - 45| = 44
        // -------------------------------------------------------
        int[] nums4 = {100000, 99999};
        int result4 = sol.minimumDifference(nums4);
        System.out.println("Additional Test 2:");
        System.out.println("Input: nums = [100000, 99999]");
        System.out.println("Expected Output: 44");
        System.out.println("Actual Output:   " + result4);
        System.out.println();

        // -------------------------------------------------------
        // Additional test: larger array of length 8
        // nums = [11, 22, 33, 44, 55, 66, 77, 88]
        // Digit sums: [2, 4, 6, 8, 10, 12, 14, 16], total = 72
        // We need 4 elements per group summing as close to 36 each as possible.
        // 2+4+14+16=36, 6+8+10+12=36 -> diff=0
        // -------------------------------------------------------
        int[] nums5 = {11, 22, 33, 44, 55, 66, 77, 88};
        int result5 = sol.minimumDifference(nums5);
        System.out.println("Additional Test 3:");
        System.out.println("Input: nums = [11, 22, 33, 44, 55, 66, 77, 88]");
        System.out.println("Expected Output: 0");
        System.out.println("Actual Output:   " + result5);
    }
}