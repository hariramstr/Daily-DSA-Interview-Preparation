import java.util.*;

/*
 * Title: Minimum XOR Merges to Isolate a Signature
 * Difficulty: Hard
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an array nums of n non-negative integers representing packet signatures.
 * In one operation, you may choose any adjacent pair nums[i] and nums[i+1], remove both values,
 * and replace them with a single value equal to their bitwise XOR. This reduces the array length by 1.
 * You may repeat this operation any number of times until only one value remains, or stop earlier.
 *
 * Your task is to find the minimum number of merge operations required so that the value x appears
 * somewhere in the array at least once after performing the operations. If it is impossible, return -1.
 *
 * A merge can only be performed on adjacent elements, and each merge changes the array structure,
 * so the order of remaining segments must always be consistent with the original order.
 * Equivalently, after several merges, every remaining element corresponds to the XOR of some contiguous
 * subarray of the original array.
 *
 * Return the minimum number of merges needed to make at least one remaining segment have XOR exactly x.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 0 <= nums[i] < 2^30
 * - 0 <= x < 2^30
 * - The solution is expected to be better than O(n^2).
 *
 * Key Observation:
 * If a contiguous subarray nums[l..r] has XOR exactly x, then we can merge that whole subarray into
 * one segment using exactly (r - l) merges, which is (length - 1).
 *
 * Therefore, the problem becomes:
 * Find the shortest contiguous subarray whose XOR is x.
 * If its length is L, then the answer is L - 1.
 * If no such subarray exists, return -1.
 *
 * Prefix XOR Fact:
 * Let prefix[i] be XOR of nums[0..i-1], with prefix[0] = 0.
 * Then XOR of subarray nums[l..r] is:
 *     prefix[r + 1] ^ prefix[l]
 *
 * We want:
 *     prefix[r + 1] ^ prefix[l] = x
 * which means:
 *     prefix[l] = prefix[r + 1] ^ x
 *
 * So for each position r, we need the latest index l with prefix[l] = prefix[r + 1] ^ x,
 * because the latest such l gives the shortest subarray ending at r.
 */

public class Solution {

    /**
     * Computes the minimum number of adjacent XOR-merge operations needed so that
     * some remaining segment has value exactly x.
     *
     * The method transforms the problem into finding the shortest contiguous subarray
     * whose XOR equals x. If such a subarray has length L, then it can be merged into
     * one value using exactly L - 1 merge operations.
     *
     * Detailed idea:
     * 1. Build prefix XOR values on the fly.
     * 2. For each current prefix XOR = prefix[i], we want a previous prefix value:
     *        target = prefix[i] ^ x
     *    because if prefix[j] = target, then subarray nums[j..i-1] has XOR x.
     * 3. To minimize subarray length (i - j), we want the largest possible j.
     *    Therefore, for each prefix XOR value, we store its latest index.
     *
     * @param nums the input array of non-negative integers
     * @param x the target XOR value that must appear in some remaining segment
     * @return the minimum number of merges required, or -1 if impossible
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minimumXorMerges(int[] nums, int x) {
        // Map from a prefix XOR value to the latest index where it appears.
        //
        // Important indexing convention:
        // - prefix index i means XOR of first i elements: nums[0..i-1]
        // - So prefix[0] = 0 before processing any element.
        //
        // If we are currently at prefix index i, and some earlier prefix index j satisfies:
        //     prefix[j] = prefix[i] ^ x
        // then subarray nums[j..i-1] has XOR x.
        Map<Integer, Integer> latestIndexByPrefixXor = new HashMap<>();

        // Before reading any elements, prefix XOR is 0 at index 0.
        latestIndexByPrefixXor.put(0, 0);

        int prefixXor = 0;

        // We will track the shortest subarray length whose XOR is x.
        int minLength = Integer.MAX_VALUE;

        // Iterate through the array.
        // After processing nums[i - 1], we are at prefix index i.
        for (int i = 1; i <= nums.length; i++) {
            // Extend the prefix XOR by including the next array element.
            prefixXor ^= nums[i - 1];

            // We need an earlier prefix value equal to prefixXor ^ x.
            // If such a prefix existed at index j, then:
            //     prefix[j] ^ prefixXor = x
            // so subarray nums[j..i-1] has XOR x.
            int neededPrefix = prefixXor ^ x;

            // If we have seen that needed prefix before, then a valid subarray exists.
            if (latestIndexByPrefixXor.containsKey(neededPrefix)) {
                int j = latestIndexByPrefixXor.get(neededPrefix);
                int length = i - j;

                // Keep the shortest valid subarray found so far.
                if (length < minLength) {
                    minLength = length;
                }
            }

            // Store/update the latest occurrence of the current prefix XOR.
            // We store the latest index because for future positions, using the latest
            // matching prefix gives the shortest possible subarray.
            latestIndexByPrefixXor.put(prefixXor, i);
        }

        // If no valid subarray was found, it is impossible.
        if (minLength == Integer.MAX_VALUE) {
            return -1;
        }

        // A subarray of length L can be merged into one segment using exactly L - 1 merges.
        return minLength - 1;
    }

    /**
     * A helper method that returns the length of the shortest contiguous subarray
     * whose XOR equals x.
     *
     * This is the direct subproblem behind the merge interpretation.
     *
     * @param nums the input array
     * @param x the target XOR value
     * @return the minimum subarray length with XOR x, or -1 if none exists
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int shortestSubarrayWithXor(int[] nums, int x) {
        Map<Integer, Integer> latestIndexByPrefixXor = new HashMap<>();
        latestIndexByPrefixXor.put(0, 0);

        int prefixXor = 0;
        int minLength = Integer.MAX_VALUE;

        for (int i = 1; i <= nums.length; i++) {
            prefixXor ^= nums[i - 1];
            int neededPrefix = prefixXor ^ x;

            Integer j = latestIndexByPrefixXor.get(neededPrefix);
            if (j != null) {
                minLength = Math.min(minLength, i - j);
            }

            latestIndexByPrefixXor.put(prefixXor, i);
        }

        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(total distinct prefix XOR values per example)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // nums = [5, 1, 4, 1], x = 4
        //
        // Valid shortest subarray:
        // [5, 1] -> 5 XOR 1 = 4, length = 2
        // So minimum merges = 2 - 1 = 1
        int[] nums1 = {5, 1, 4, 1};
        int x1 = 4;
        int result1 = solution.minimumXorMerges(nums1, x1);
        System.out.println("Sample 1 result: " + result1); // Expected: 1

        // Sample 2:
        // nums = [2, 7, 2, 7], x = 0
        //
        // Entire array XOR = 0, length = 4
        // No shorter subarray has XOR 0
        // So minimum merges = 4 - 1 = 3
        int[] nums2 = {2, 7, 2, 7};
        int x2 = 0;
        int result2 = solution.minimumXorMerges(nums2, x2);
        System.out.println("Sample 2 result: " + result2); // Expected: 3

        // Additional check:
        // If an element already equals x, answer should be 0.
        int[] nums3 = {8, 3, 6};
        int x3 = 3;
        int result3 = solution.minimumXorMerges(nums3, x3);
        System.out.println("Already present result: " + result3); // Expected: 0

        // Additional check:
        // Impossible case.
        int[] nums4 = {1, 2, 4};
        int x4 = 7;
        int result4 = solution.minimumXorMerges(nums4, x4);
        System.out.println("Impossible case result: " + result4); // Expected: -1

        // Additional check:
        // [1, 1] XOR = 0, so answer is 1 merge.
        int[] nums5 = {1, 1};
        int x5 = 0;
        int result5 = solution.minimumXorMerges(nums5, x5);
        System.out.println("Two-element merge result: " + result5); // Expected: 1
    }
}