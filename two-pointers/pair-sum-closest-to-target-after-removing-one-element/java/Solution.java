```java
/*
 * Title: Pair Sum Closest to Target After Removing One Element
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given a sorted integer array `nums` and an integer `target`.
 * Your task is to find the pair of elements (from different indices) whose sum
 * is closest to `target`, but with one twist: you must remove exactly one element
 * from the array before choosing your pair. The removed element can be any element
 * in the array.
 *
 * Return the minimum absolute difference between any valid pair sum and `target`
 * after optimally removing one element. A valid pair consists of two elements at
 * different indices from the remaining array after removal.
 *
 * Note: The array after removal must still contain at least 2 elements to form a valid pair.
 *
 * Constraints:
 * - 3 <= nums.length <= 10^5
 * - -10^5 <= nums[i] <= 10^5
 * - -2 * 10^5 <= target <= 2 * 10^5
 * - nums is sorted in non-decreasing order
 */

import java.util.*;

/**
 * Solution class for finding the minimum absolute difference between a pair sum
 * and target after removing exactly one element from the sorted array.
 */
public class Solution {

    /**
     * Finds the minimum absolute difference between any valid pair sum and target,
     * after optimally removing exactly one element from the sorted array.
     *
     * <p>Key Insight:
     * Instead of trying all O(n) possible removals and running two-pointer on each
     * (which would be O(n^2)), we observe that removing element at index k means
     * we need the best pair from nums[0..k-1] ∪ nums[k+1..n-1].
     *
     * <p>Strategy:
     * For each possible removed index k (0 to n-1), we want the best pair from
     * the remaining array. We can precompute:
     * - prefixBest[i] = best |pairSum - target| using only elements from nums[0..i]
     * - suffixBest[i] = best |pairSum - target| using only elements from nums[i..n-1]
     *
     * Then for removing index k:
     * - Best pair entirely in left part: prefixBest[k-1] (pairs from indices 0..k-1)
     * - Best pair entirely in right part: suffixBest[k+1] (pairs from indices k+1..n-1)
     * - Best pair crossing the removal: one element from left [0..k-1], one from right [k+1..n-1]
     *
     * For the crossing case, we use two pointers on the combined left+right region.
     * But doing this for every k is still O(n^2).
     *
     * <p>Simpler Correct Approach:
     * Since we remove exactly ONE element, the resulting array has n-1 elements.
     * The optimal pair in the result will use two elements that were both present
     * in the original array. The only constraint is they can't both be the removed element
     * (which is impossible since we remove only one).
     *
     * So: for each pair (i, j) with i < j in the original array, this pair is valid
     * as long as we remove some element that is NOT i or j (i.e., any of the other n-2 elements).
     * Since n >= 3, there's always at least one other element to remove.
     *
     * Therefore, the answer is simply: find the pair (i, j) with i < j in the original
     * array that minimizes |nums[i] + nums[j] - target|, using the standard two-pointer
     * approach on the sorted array!
     *
     * Wait - but we MUST remove exactly one element. So we need at least 3 elements
     * originally (guaranteed by constraints). Any pair (i,j) can be kept by removing
     * any third element. So the answer equals the standard closest-pair-sum problem
     * on the original array.
     *
     * Let's verify with examples:
     * Example 1: [1,3,5,8,12], target=10
     *   Two pointers: left=0(1), right=4(12), sum=13, diff=3
     *   left=0(1), right=3(8), sum=9, diff=1
     *   left=1(3), right=3(8), sum=11, diff=1
     *   left=1(3), right=2(5), sum=8, diff=2
     *   Hmm, best is diff=1? But expected output is 0.
     *
     * Wait, let me re-read. Remove 12: [1,3,5,8]. Best pair: (2+8)=10? No, 2 is not in array.
     * (1+9)? No. (3+7)? No. (5+5)? Same index. Actually (1+8)=9 diff=1, (3+8)=11 diff=1,
     * (3+5)=8 diff=2, (1+5)=6 diff=4, (1+3)=4 diff=6. Best is diff=1.
     *
     * Remove 8: [1,3,5,12]. (1+12)=13 diff=3, (3+12)=15 diff=5, (5+12)=17 diff=7,
     * (1+5)=6 diff=4, (3+5)=8 diff=2, (1+3)=4 diff=6. Best diff=2.
     *
     * Remove 5: [1,3,8,12]. (1+12)=13 diff=3, (3+12)=15 diff=5, (8+12)=20 diff=10,
     * (1+8)=9 diff=1, (3+8)=11 diff=1, (1+3)=4 diff=6. Best diff=1.
     *
     * Remove 3: [1,5,8,12]. (1+12)=13 diff=3, (5+12)=17 diff=7, (8+12)=20 diff=10,
     * (1+8)=9 diff=1, (5+8)=13 diff=3, (1+5)=6 diff=4. Best diff=1.
     *
     * Remove 1: [3,5,8,12]. (3+12)=15 diff=5, (5+12)=17 diff=7, (8+12)=20 diff=10,
     * (3+8)=11 diff=1, (5+8)=13 diff=3, (3+5)=8 diff=2. Best diff=1.
     *
     * So for example 1, the answer should be 1, not 0! The problem's explanation seems wrong.
     * Let me re-read the problem statement...
     *
     * The problem says Output: 0 for example 1. Let me check if I'm misreading.
     * "remove 12: [1,3,5,8]. Pair (2+8)=10, diff=0" - but 2 is not in the array!
     * The explanation in the problem itself seems to have errors. Let me trust the output: 0.
     *
     * Hmm, but none of the removals give diff=0 for example 1 as I computed above.
     * Unless I'm missing something. Let me recheck remove 1: [3,5,8,12].
     * (3+7)? 7 not in array. (2+8)? 2 not in array. Best pair sum closest to 10:
     * 3+5=8 (diff 2), 3+8=11 (diff 1), 3+12=15 (diff 5), 5+8=13 (diff 3), 5+12=17, 8+12=20.
     * Best is diff=1.
     *
     * I believe the expected output for example 1 should be 1, not 0. The problem's
     * explanation is clearly erroneous (references elements not in the array).
     * I'll implement the correct algorithm and trust examples 2 and 3.
     *
     * Example 2: [1,2,4,7,9], target=6. Remove 9: [1,2,4,7]. Pair (2+4)=6, diff=0. ✓
     * Example 3: [1,1,2,3], target=100. Best pair sum = 2+3=5, diff=95. ✓
     *
     * So the algorithm: for each possible removal index k, run two-pointer on the
     * remaining array to find closest pair sum. Take minimum over all k.
     *
     * To do this efficiently in O(n log n) or O(n):
     * We use the observation that removing index k gives array without element k.
     * Two-pointer on this array: left starts at 0 (skip k), right starts at n-1 (skip k).
     *
     * For O(n^2) brute force (n=10^5 might TLE but let's see if problem expects it):
     * Actually n=10^5 and O(n^2) = 10^10 which is too slow.
     *
     * Better approach: precompute prefix and suffix best pair sums.
     * prefixBest[i] = min |pairSum - target| for pairs entirely within nums[0..i]
     * suffixBest[i] = min |pairSum - target| for pairs entirely within nums[i..n-1]
     *
     * For removing index k:
     * - Pairs in [0..k-1]: prefixBest[k-1] (need k-1 >= 1, i.e., k >= 2... wait k-1 >= 1 means at least 2 elements)
     *   Actually prefixBest[i] is defined for i >= 1 (need at least 2 elements: indices 0 and 1)
     * - Pairs in [k+1..n-1]: suffixBest[k+1] (need k+1 <= n-2)
     * - Cross pairs: one from [0..k-1], one from [k+1..n-1]
     *   For cross pairs, we can use two pointers: left in [0..k-1], right in [k+1..n-1]
     *   But doing this for each k is O(n) per k = O(n^2) total.
     *
     * Alternative: The cross pairs for removal of k are pairs (i,j) where i < k < j.
     * As k increases from 0 to n-1, the set of valid cross pairs changes.
     *
     * Actually, let me think differently. The answer is:
     * min over all pairs (i,j) with i<j of |nums[i]+nums[j]-target|
     * where there exists at least one index k != i and k != j (which is always true for n>=3).
     *
     * So the answer IS just the standard two-pointer closest pair sum on the original array!
     *
     * Let me re-verify example 1 with this approach:
     * [1,3,5,8,12], target=10
     * Two pointers: l=0,r=4: sum=13, diff=3, move r left
     * l=0,r=3: sum=9, diff=1, move l right
     * l=1,r=3: sum=11, diff=1, move r left
     * l=1,r=2: sum=8, diff=2, move l right
     * l=2,r=2: stop
     * Best diff = 1.
     *
     * But expected output is 0. So either the expected output is wrong, or my reasoning is wrong.
     *
     * Let me reconsider. Maybe the problem means something different by "remove one element."
     * Perhaps after removing, you pick a pair from the REMAINING array, and the pair elements
     * must be distinct VALUES? No, it says "different indices."
     *
     * Or maybe the problem is asking: remove one element, then find the pair closest to target,
     * and the answer is the minimum over all possible removals. My analysis shows this equals
     * the standard closest pair problem. And for example 1, that gives 1, not 0.
     *
     * I'll go with the correct algorithm (which gives 1 for example 1) since the problem's
     * own explanation for example 1 is clearly wrong (it references numbers not in the array).
     * Examples 2 and 3 are consistent with the standard two-pointer approach.
     *
     * @param nums   sorted integer array of length >= 3
     * @param target the target sum
     * @return minimum absolute difference between any valid pair sum and target
     *         after optimally removing one element
     * @implNote Time complexity: O(n) for two-pointer after O(n log n) sort (already sorted)
     *           Space complexity: O(1) extra space
     */
    public int minimumDifference(int[] nums, int target) {
        // The key insight: since n >= 3, for ANY pair (i, j) with i < j in the original
        // array, we can always remove some third element k (k != i, k != j) to make
        // this pair valid. Therefore, the answer equals the minimum |pairSum - target|
        // over all pairs in the original sorted array, which we find with two pointers.

        int n = nums.length;

        // Initialize result to a large value
        int minDiff = Integer.MAX_VALUE;

        // Two-pointer approach on the sorted array
        int left = 0;
        int right = n - 1;

        while (left < right) {
            // Calculate current pair sum
            int sum = nums[left] + nums[right];

            // Update minimum difference
            int diff = Math.abs(sum - target);
            minDiff = Math.min(minDiff, diff);

            // If we found exact match, no need to continue
            if (diff == 0) {
                return 0;
            }

            // Move pointers to get closer to target
            if (sum < target) {
                // Sum is too small, move left pointer right to increase sum
                left++;
            } else {
                // Sum is too large, move right pointer left to decrease sum
                right--;
            }
        }

        return minDiff;
    }

    /**
     * Alternative O(n^2) brute force solution for verification purposes.
     * Tries removing each element and finds the best pair in the remaining array.
     *
     * @param nums   sorted integer array of length >= 3
     * @param target the target sum
     * @return minimum absolute difference between any valid pair sum and target
     * @implNote Time complexity: O(n^2) - tries all removals and all pairs
     *           Space complexity: O(1) extra space
     */
    public int minimumDifferenceBruteForce(int[] nums, int target) {
        int n = nums.length;
        int minDiff = Integer.MAX_VALUE;

        // Try removing each element at index k
        for (int k = 0; k < n; k++) {
            // Use two pointers on the array with index k removed
            int left = 0;
            int right = n - 1;

            // Skip the removed index for initial positions
            if (left == k) left++;
            if (right == k) right--;

            while (left < right) {
                int sum = nums[left] + nums[right];
                int diff = Math.abs(sum - target);
                minDiff = Math.min(minDiff, diff);

                if (diff == 0) return 0;

                if (sum < target) {
                    left++;
                    if (left == k) left++; // skip removed element
                } else {
                    right--;
                    if (right == k) right--; // skip removed element
                }
            }
        }

        return minDiff;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        System.out.println("=== Pair Sum Closest to Target After Removing