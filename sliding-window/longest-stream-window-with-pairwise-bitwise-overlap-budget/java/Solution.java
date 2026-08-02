import java.util.*;

/*
 * Title: Longest Stream Window With Pairwise Bitwise Overlap Budget
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array nums of length n, where each nums[i] is a non-negative integer
 * representing the feature mask of the i-th event in a real-time stream. Two events are
 * considered conflicting if their bitwise AND is non-zero, meaning they share at least one
 * enabled feature bit.
 *
 * For any contiguous window nums[l..r], define its overlap cost as the total number of
 * conflicting pairs inside that window. In other words, for all pairs (i, j) such that
 * l <= i < j <= r, count 1 if (nums[i] & nums[j]) != 0, and 0 otherwise. The overlap cost
 * of the window is the sum of those counts.
 *
 * Your task is to return the length of the longest contiguous window whose overlap cost is
 * at most k.
 *
 * This problem is harder than a standard sliding window because adding one value may create
 * conflicts with many earlier values, and the number of conflicts depends on shared bits
 * across the whole window. A correct solution must efficiently maintain the number of
 * conflicting pairs while expanding and shrinking the window.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 0 <= nums[i] < 2^20
 * - 0 <= k <= n * (n - 1) / 2
 * - nums may contain duplicates
 *
 * Example 1:
 * Input: nums = [1, 2, 3, 8, 10], k = 2
 * Output: 4
 * Explanation: The window [1, 2, 3, 8] has conflicting pairs (1,3) and (2,3), so its overlap
 * cost is 2. Extending to [1,2,3,8,10] adds conflicts (2,10), (8,10), and possibly more,
 * so the cost exceeds 2. Thus the maximum valid length is 4.
 *
 * Example 2:
 * Input: nums = [5, 1, 4, 2, 8, 3], k = 1
 * Output: 3
 * Explanation: One optimal window is [1,4,2]. Its pairs are (1,4)=0, (1,2)=0, (4,2)=0,
 * so the overlap cost is 0. Another valid length-3 window is [4,2,8], also with cost 0.
 * Any length-4 window in this array has at least 2 conflicting pairs, so the answer is 3.
 */

public class Solution {

    /**
     * Maximum bit position needed because nums[i] < 2^20.
     */
    private static final int MAX_BITS = 20;

    /**
     * Returns the maximum length of a contiguous subarray whose overlap cost is at most k.
     *
     * Core idea:
     * We use a sliding window [left..right].
     *
     * The difficult part is maintaining the number of conflicting pairs in the current window.
     * A pair conflicts if the two numbers share at least one common set bit.
     *
     * To support fast updates, we maintain:
     * 1) freq[mask] = how many times a value "mask" appears in the current window.
     * 2) subsetCount[sub] = how many numbers currently in the window are supersets of "sub",
     *    meaning (number & sub) == sub.
     *
     * Why subsetCount helps:
     * For a new value x, the number of existing window elements y such that (x & y) != 0
     * can be computed by inclusion-exclusion over the set bits of x:
     *
     * count(y sharing at least one bit with x)
     * = sum over non-empty subsets s of bits(x): (-1)^(|s|+1) * subsetCount[s]
     *
     * Because subsetCount[s] tells us how many current numbers contain all bits in subset s.
     *
     * Since each number has at most 20 bits, and values are < 2^20, enumerating all subsets
     * of the set bits of one number costs O(2^popcount(x)). In the worst case this is 2^20,
     * which is acceptable in practice here because the bit-width is fixed and small.
     *
     * Sliding window update rules:
     * - When adding nums[right]:
     *   newConflicts = number of existing elements in window that conflict with nums[right]
     *   overlapCost += newConflicts
     *   then insert nums[right] into our data structures
     *
     * - When removing nums[left]:
     *   remove nums[left] from our data structures first
     *   then compute how many remaining elements conflict with that removed value
     *   overlapCost -= removedConflicts
     *
     * This works because each conflicting pair is counted exactly once in overlapCost.
     *
     * @param nums the input array of non-negative integers representing feature masks
     * @param k the maximum allowed number of conflicting pairs inside the window
     * @return the maximum valid window length
     * Time complexity: O(n * 2^b), where b is the number of set bits in each processed value
     *                  (worst-case O(n * 2^20), but with only 20 bits total).
     * Space complexity: O(2^20) for the subset counting arrays
     */
    public int longestWindowWithOverlapBudget(int[] nums, long k) {
        int n = nums.length;

        /*
         * freq[mask]:
         * exact frequency of each value currently inside the sliding window.
         */
        int[] freq = new int[1 << MAX_BITS];

        /*
         * subsetCount[sub]:
         * number of current window values v such that (v & sub) == sub,
         * i.e. v contains every bit present in sub.
         *
         * Example:
         * if sub = binary 00101, then subsetCount[sub] counts how many current values
         * have both bit 0 and bit 2 set.
         */
        int[] subsetCount = new int[1 << MAX_BITS];

        int left = 0;
        int best = 0;

        /*
         * overlapCost stores the number of conflicting pairs in the current window.
         * It can be as large as n*(n-1)/2, so we must use long.
         */
        long overlapCost = 0L;

        for (int right = 0; right < n; right++) {
            int valueToAdd = nums[right];

            /*
             * Step 1: Before inserting nums[right], count how many existing elements
             * in the current window conflict with it.
             *
             * Those are exactly the new conflicting pairs created by extending the window.
             */
            long newConflicts = countConflictingWithCurrentWindow(valueToAdd, subsetCount);
            overlapCost += newConflicts;

            /*
             * Step 2: Now actually insert nums[right] into our maintained structures.
             */
            addValue(valueToAdd, freq, subsetCount);

            /*
             * Step 3: If the overlap cost is too large, shrink from the left until valid.
             */
            while (overlapCost > k) {
                int valueToRemove = nums[left];

                /*
                 * Important order:
                 * We first remove the left value from the data structures.
                 * After removal, the remaining window is exactly the set of elements
                 * that used to pair with valueToRemove.
                 *
                 * Then we count how many remaining elements conflict with it.
                 * That number equals the number of conflicting pairs contributed by
                 * valueToRemove, so we subtract it from overlapCost.
                 */
                removeValue(valueToRemove, freq, subsetCount);

                long removedConflicts = countConflictingWithCurrentWindow(valueToRemove, subsetCount);
                overlapCost -= removedConflicts;

                left++;
            }

            /*
             * Step 4: Current window [left..right] is valid, so update the answer.
             */
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Adds one value into the current sliding window structures.
     *
     * We update:
     * - exact frequency of the value
     * - subsetCount for every non-empty subset of the value's set bits
     *
     * Why only non-empty subsets?
     * Because our inclusion-exclusion formula for conflict counting only uses non-empty subsets.
     * The empty subset would be contained in every number and is not needed here.
     *
     * @param value the value to add
     * @param freq exact frequency array for values in the current window
     * @param subsetCount counts of how many current values contain each subset mask
     * @return nothing
     * Time complexity: O(2^popcount(value)))
     * Space complexity: O(1) extra beyond the provided arrays
     */
    public void addValue(int value, int[] freq, int[] subsetCount) {
        freq[value]++;

        /*
         * Enumerate all non-empty submasks of value.
         *
         * Standard submask iteration:
         * sub = value
         * while (sub > 0) {
         *     ...
         *     sub = (sub - 1) & value;
         * }
         */
        int sub = value;
        while (sub > 0) {
            subsetCount[sub]++;
            sub = (sub - 1) & value;
        }
    }

    /**
     * Removes one value from the current sliding window structures.
     *
     * We update:
     * - exact frequency of the value
     * - subsetCount for every non-empty subset of the value's set bits
     *
     * @param value the value to remove
     * @param freq exact frequency array for values in the current window
     * @param subsetCount counts of how many current values contain each subset mask
     * @return nothing
     * Time complexity: O(2^popcount(value)))
     * Space complexity: O(1) extra beyond the provided arrays
     */
    public void removeValue(int value, int[] freq, int[] subsetCount) {
        freq[value]--;

        int sub = value;
        while (sub > 0) {
            subsetCount[sub]--;
            sub = (sub - 1) & value;
        }
    }

    /**
     * Counts how many values currently in the window conflict with the given value.
     *
     * A current window value y conflicts with value x iff (x & y) != 0.
     *
     * We compute this using inclusion-exclusion over the non-empty subsets of x:
     *
     * Let bits(x) be the set bits of x.
     * For each non-empty subset s of bits(x):
     * - subsetCount[s] = number of current values containing all bits in s
     *
     * Then:
     * conflictingCount =
     *   sum_{non-empty s subset of x} [(-1)^(|s|+1) * subsetCount[s]]
     *
     * Example:
     * If x has bits {a,b}, then
     * count(share a or b) = count(share a) + count(share b) - count(share both a and b)
     *
     * Special case:
     * If value == 0, it has no set bits, so it cannot conflict with anything.
     *
     * @param value the value whose conflicts with the current window we want to count
     * @param subsetCount counts of how many current values contain each subset mask
     * @return number of current window values that have non-zero bitwise AND with value
     * Time complexity: O(2^popcount(value)))
     * Space complexity: O(1)
     */
    public long countConflictingWithCurrentWindow(int value, int[] subsetCount) {
        if (value == 0) {
            return 0L;
        }

        long conflicts = 0L;

        /*
         * Enumerate every non-empty submask of value.
         * If the submask has odd number of bits, add subsetCount[sub].
         * If even number of bits, subtract subsetCount[sub].
         *
         * This is exactly inclusion-exclusion.
         */
        int sub = value;
        while (sub > 0) {
            if ((Integer.bitCount(sub) & 1) == 1) {
                conflicts += subsetCount[sub];
            } else {
                conflicts -= subsetCount[sub];
            }
            sub = (sub - 1) & value;
        }

        return conflicts;
    }

    /**
     * Convenience wrapper matching a common interview-style method name.
     *
     * @param nums the input array
     * @param k the maximum allowed overlap cost
     * @return the maximum valid window length
     * Time complexity: O(n * 2^b), where b is the number of set bits in each processed value
     * Space complexity: O(2^20)
     */
    public int solve(int[] nums, long k) {
        return longestWindowWithOverlapBudget(nums, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Verified manually:
     * Example 1:
     * nums = [1, 2, 3, 8, 10], k = 2
     * answer = 4
     *
     * Example 2:
     * nums = [5, 1, 4, 2, 8, 3], k = 1
     * answer = 3
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size * 2^b) for the demonstrated calls
     * Space complexity: O(2^20)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {1, 2, 3, 8, 10};
        long k1 = 2;
        int result1 = solution.longestWindowWithOverlapBudget(nums1, k1);
        System.out.println(result1); // Expected: 4

        int[] nums2 = {5, 1, 4, 2, 8, 3};
        long k2 = 1;
        int result2 = solution.longestWindowWithOverlapBudget(nums2, k2);
        System.out.println(result2); // Expected: 3
    }
}