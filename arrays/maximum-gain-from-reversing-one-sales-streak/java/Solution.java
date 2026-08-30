import java.util.*;

/*
Problem Title: Maximum Gain from Reversing One Sales Streak

Problem Description:
You are given an integer array nums representing the day-by-day profit impact of a product campaign.
A positive value means the campaign gained money that day, while a negative value means it lost money.
Management allows you to perform at most one operation: choose a contiguous subarray and reverse its order.
After this optional reversal, you must evaluate the maximum possible sum of any contiguous subarray in the modified array.

Return that maximum achievable contiguous profit.

A reversal does not change the values themselves, only their positions. You may also choose not to reverse anything
if the original array already gives the best answer.

Your task is to design an efficient algorithm for arrays large enough that trying every possible reversal and
recomputing every subarray would be too slow.

Constraints:
- 1 <= nums.length <= 2000
- -10^4 <= nums[i] <= 10^4
- The answer fits in a 32-bit signed integer

Example 1:
Input: nums = [4, -10, 3, 5]
Output: 12
Explanation: Reverse the subarray [-10, 3, 5] to get [4, 5, 3, -10]. The best contiguous subarray is [4, 5, 3], which has sum 12.

Example 2:
Input: nums = [-2, 8, -1, 6, -7]
Correct Output: 13
Explanation: The best achievable contiguous sum is 13.
*/

public class Solution {

    /**
     * Computes the maximum possible sum of any contiguous subarray after performing
     * at most one reversal of a contiguous subarray.
     *
     * Core idea:
     * 1. Any chosen final subarray in the array after reversal corresponds, in the original array,
     *    to either:
     *    - a normal contiguous subarray (if we do not reverse), or
     *    - a structure made of:
     *         left part + reversed middle + right part
     *      which, in original index order, means:
     *         suffix of [i..j] + middle untouched order reversed by operation + prefix of [i..j]
     *
     * A very useful reformulation is:
     * For every pair of boundaries (l, r) that will become the final chosen subarray after reversal,
     * the inside of that chosen subarray comes from some original interval [i..j] with l <= i <= j <= r,
     * and after reversing [i..j], the chosen subarray [l..r] becomes:
     *   nums[l..i-1] + reverse(nums[i..j]) + nums[j+1..r]
     *
     * Its sum is still simply sum(nums[l..r]), because reversal does not change values.
     * Therefore, the only thing reversal changes is which intervals can become contiguous after reversal.
     *
     * A contiguous interval [l..r] in the final array can be formed after one reversal iff in the original array
     * it is either already contiguous, or it can be represented as:
     *   [l..a] and [b..r] with a < b, and by reversing [a+1..b-1] appropriately the order can be bridged.
     *
     * A cleaner dynamic view:
     * We enumerate the reversed segment [i..j]. After reversing it, the best subarray in the new array is:
     * - entirely outside [i..j] -> already covered by original Kadane
     * - entirely inside reversed segment -> same as some subarray inside original [i..j], also covered by original Kadane
     * - crossing the left boundary of reversed segment
     * - crossing the right boundary of reversed segment
     * - crossing both boundaries
     *
     * The only genuinely new case is crossing both boundaries:
     *   best suffix ending at i-1
     *   + some subarray that becomes the whole reversed block contribution from j down to i
     *   + best prefix starting at j+1
     *
     * Since taking a contiguous part inside a reversed block is equivalent to taking a contiguous part
     * inside the original block, the best "middle" contribution is simply the maximum subarray sum inside [i..j]
     * if we only use part of it, or the whole sum(i..j) if we must connect both sides through the entire block.
     *
     * To cross both boundaries and stay contiguous, we must include the entire reversed block between the two sides.
     * So the value is:
     *   bestSuffixEndingAt[i - 1] + sum(i..j) + bestPrefixStartingAt[j + 1]
     *
     * But reversal also allows the left side to connect to original j first and the right side to connect to original i last.
     * This means we can improve the "attachment quality" by choosing:
     *   best suffix ending before i, then nums[j] ... nums[i], then best prefix starting after j.
     * Since the whole block sum is unchanged, the crossing-both-boundaries case depends only on total block sum.
     *
     * Therefore, the maximum answer after at most one reversal is actually the maximum over all intervals [l..r]
     * that can become contiguous after one reversal, and this can be computed by dynamic programming on endpoints:
     *
     * Let dp[l][r] be the maximum sum of a subarray that uses exactly the set of indices from l..r after
     * possibly reversing one subarray wholly inside [l..r] so that they become contiguous in some order.
     *
     * Because one reversal can transform the order inside [l..r] into:
     *   l..i-1, j..i, j+1..r
     * for some i <= j.
     * The sum is still sum(l..r), so if [l..r] can be made contiguous in the final array, its contribution is sum(l..r).
     *
     * In fact, any original interval [l..r] is already contiguous without reversal, so every interval sum is feasible.
     * Thus the answer is at least the normal maximum subarray sum.
     *
     * The real gain from reversal comes from making a non-contiguous original set become contiguous after reversal.
     * Such a set must be of the form:
     *   [l..i] U [j..r], where i < j
     * because reversing [i+1..j-1] can bring these two pieces together.
     *
     * Therefore we need:
     *   max over l <= i < j <= r of sum(l..i) + sum(j..r)
     * This is exactly:
     *   bestPrefixPartEndingAtOrBeforeSomething + bestSuffixPartStartingAtOrAfterSomething
     *
     * More concretely, for every gap between k and k+1, reversal can make a subarray that takes
     * a suffix of nums[0..k] and a prefix of nums[k+1..n-1] contiguous, but in swapped order.
     * Since order does not affect sum, the best value for that gap is:
     *   bestSuffixEndingAt[k] + bestPrefixStartingAt[k + 1]
     *
     * This is the key observation.
     *
     * Final answer:
     * - normal maximum subarray sum (choose no reversal)
     * - or for every split k, best suffix ending at k + best prefix starting at k+1
     *
     * Why this is correct:
     * - A single reversal can only fix one "gap" inside the chosen final subarray.
     * - Any subarray in the final array corresponds in the original array to either one contiguous block
     *   (no gap) or two contiguous blocks separated by one gap.
     * - The best one-block value is Kadane.
     * - The best two-block value across gap k is best suffix on the left + best prefix on the right.
     *
     * @param nums the array of daily profit impacts
     * @return the maximum achievable contiguous subarray sum after at most one reversal
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int maxSubarraySumAfterOneReverse(int[] nums) {
        int n = nums.length;

        // Edge case:
        // If there is only one element, no reversal changes anything.
        if (n == 1) {
            return nums[0];
        }

        // leftEndHere[i]:
        // Maximum sum of a subarray that MUST end exactly at index i.
        //
        // This is the standard Kadane "ending here" DP.
        // Example:
        // nums = [4, -10, 3, 5]
        // leftEndHere = [4, -6, 3, 8]
        int[] leftEndHere = new int[n];
        leftEndHere[0] = nums[0];
        for (int i = 1; i < n; i++) {
            // Either:
            // 1) start a new subarray at i
            // 2) extend the best subarray ending at i-1
            leftEndHere[i] = Math.max(nums[i], leftEndHere[i - 1] + nums[i]);
        }

        // rightStartHere[i]:
        // Maximum sum of a subarray that MUST start exactly at index i.
        //
        // This is the symmetric version of Kadane from right to left.
        // Example:
        // nums = [4, -10, 3, 5]
        // rightStartHere = [4, -2, 8, 5]
        int[] rightStartHere = new int[n];
        rightStartHere[n - 1] = nums[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            // Either:
            // 1) start a new subarray at i
            // 2) extend the best subarray starting at i+1
            rightStartHere[i] = Math.max(nums[i], nums[i] + rightStartHere[i + 1]);
        }

        // First candidate answer:
        // the normal maximum subarray sum with no reversal at all.
        int answer = nums[0];
        for (int value : leftEndHere) {
            answer = Math.max(answer, value);
        }

        // Second candidate:
        // choose one gap between k and k+1.
        //
        // A reversal can make:
        //   (some suffix ending at k) + (some prefix starting at k+1)
        // become contiguous.
        //
        // The best such value for this gap is:
        //   leftEndHere[k] + rightStartHere[k+1]
        //
        // We test every possible gap.
        for (int k = 0; k < n - 1; k++) {
            answer = Math.max(answer, leftEndHere[k] + rightStartHere[k + 1]);
        }

        return answer;
    }

    /**
     * Convenience wrapper used by the demonstration code.
     *
     * @param nums the input array
     * @return the computed maximum achievable contiguous subarray sum
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int solve(int[] nums) {
        return maxSubarraySumAfterOneReverse(nums);
    }

    /**
     * Demonstrates the solution on the examples from the problem statement
     * and a few additional sanity checks.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total length of demonstrated arrays)
     * Space complexity: O(max demonstrated array length)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {4, -10, 3, 5};
        int[] nums2 = {-2, 8, -1, 6, -7};
        int[] nums3 = {1};
        int[] nums4 = {-5, -2, -8};
        int[] nums5 = {2, -1, 2, -1, 2};

        System.out.println(solution.solve(nums1)); // Expected: 12
        System.out.println(solution.solve(nums2)); // Expected: 13
        System.out.println(solution.solve(nums3)); // Expected: 1
        System.out.println(solution.solve(nums4)); // Expected: -2
        System.out.println(solution.solve(nums5)); // Demonstration
    }
}