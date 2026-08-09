import java.util.*;

/*
 * Title: Shortest Segment With Target XOR
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an array nums of non-negative integers and an integer target.
 * A contiguous segment of the array is called valid if the bitwise XOR of all
 * values in that segment is exactly equal to target.
 *
 * Return the length of the shortest valid segment. If no such segment exists,
 * return -1.
 *
 * A segment must contain at least one element. The XOR of a segment nums[l..r]
 * is defined as:
 * nums[l] ^ nums[l+1] ^ ... ^ nums[r]
 *
 * Constraints:
 * - 1 <= nums.length <= 2 * 10^5
 * - 0 <= nums[i] <= 10^9
 * - 0 <= target <= 10^9
 *
 * Example 1:
 * Input: nums = [5, 1, 2, 1, 5], target = 3
 * Output: 2
 * Explanation: The segment [1, 2] has XOR 1 ^ 2 = 3, so a valid answer is 2.
 * No segment of length 1 has XOR 3.
 *
 * Example 2:
 * Input: nums = [4, 7, 4, 7], target = 0
 * Output: 4
 * Explanation:
 * - [4, 7] has XOR 3
 * - [7, 4] has XOR 3
 * - [7, 4, 7] has XOR 4
 * - [4, 7, 4, 7] has XOR 0
 * Therefore the shortest valid contiguous segment has length 4.
 *
 * Key Idea:
 * Let prefixXor[i] be the XOR of the first i elements.
 * Then XOR of subarray nums[l..r] is:
 * prefixXor[r + 1] ^ prefixXor[l]
 *
 * We want:
 * prefixXor[r + 1] ^ prefixXor[l] = target
 *
 * Rearranging:
 * prefixXor[l] = prefixXor[r + 1] ^ target
 *
 * So for each ending position r, if we know the most recent index l where
 * prefixXor[l] equals (currentPrefixXor ^ target), then we can form the
 * shortest subarray ending at r with XOR equal to target.
 *
 * To minimize length, for each prefix XOR value we should remember the LARGEST
 * index where it appeared most recently.
 */
public class Solution {

    /**
     * Finds the length of the shortest contiguous segment whose XOR equals target.
     *
     * The algorithm uses prefix XOR and a hash map:
     * - prefixXor represents XOR of elements from index 0 to current index.
     * - If a subarray nums[l..r] has XOR = target, then:
     *   prefixXorAtRPlus1 ^ prefixXorAtL = target
     * - Therefore:
     *   prefixXorAtL = prefixXorAtRPlus1 ^ target
     *
     * For each position, we look up whether the needed previous prefix XOR exists.
     * To get the shortest segment, we store the most recent index for each prefix XOR.
     *
     * @param nums the input array of non-negative integers
     * @param target the required XOR value for a valid segment
     * @return the length of the shortest valid segment, or -1 if no such segment exists
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int shortestSegmentWithTargetXor(int[] nums, int target) {
        // This map stores:
        // key   = a prefix XOR value
        // value = the latest prefix index where this XOR was seen
        //
        // Important detail:
        // We store PREFIX INDICES, not array indices.
        //
        // Prefix index meaning:
        // - prefix index 0 means XOR of zero elements = 0
        // - prefix index 1 means XOR of nums[0]
        // - prefix index 2 means XOR of nums[0..1]
        // and so on.
        //
        // If we are currently at prefix index i, then the subarray length formed
        // with an earlier prefix index j is i - j.
        Map<Integer, Integer> latestIndexByPrefixXor = new HashMap<>();

        // Before processing any elements, prefix XOR is 0 at prefix index 0.
        // This allows subarrays starting from index 0 to be handled naturally.
        latestIndexByPrefixXor.put(0, 0);

        int prefixXor = 0;

        // We will keep the smallest valid length found so far.
        int answer = Integer.MAX_VALUE;

        // We iterate through the array.
        // Let i be the array index.
        // Then the corresponding prefix index after including nums[i] is i + 1.
        for (int i = 0; i < nums.length; i++) {
            // Step 1:
            // Extend the running prefix XOR by including nums[i].
            prefixXor ^= nums[i];

            // Current prefix index after processing nums[i].
            int currentPrefixIndex = i + 1;

            // Step 2:
            // We want a previous prefix XOR value such that:
            // previousPrefixXor ^ currentPrefixXor = target
            //
            // Rearranged:
            // previousPrefixXor = currentPrefixXor ^ target
            int neededPrefixXor = prefixXor ^ target;

            // Step 3:
            // If such a previous prefix XOR exists, then we can form a valid subarray.
            // To get the SHORTEST one ending here, we want the LATEST such prefix index.
            Integer previousPrefixIndex = latestIndexByPrefixXor.get(neededPrefixXor);
            if (previousPrefixIndex != null) {
                int length = currentPrefixIndex - previousPrefixIndex;
                if (length < answer) {
                    answer = length;
                }
            }

            // Step 4:
            // Update the latest occurrence of the current prefix XOR.
            //
            // Why overwrite instead of keeping the earliest?
            // Because we want the shortest subarray.
            // For a fixed ending position, using the most recent matching prefix index
            // gives the smallest length.
            latestIndexByPrefixXor.put(prefixXor, currentPrefixIndex);
        }

        // If answer was never updated, no valid segment exists.
        return answer == Integer.MAX_VALUE ? -1 : answer;
    }

    /**
     * A helper method that prints an array in a readable format.
     *
     * @param nums the array to convert to string form
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of elements across demo cases)
     * Space complexity: O(n) for the largest demo case due to the hash map
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] nums1 = {5, 1, 2, 1, 5};
        int target1 = 3;
        int result1 = solution.shortestSegmentWithTargetXor(nums1, target1);
        System.out.println("Example 1:");
        System.out.println("nums = " + solution.arrayToString(nums1));
        System.out.println("target = " + target1);
        System.out.println("Shortest valid segment length = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Sample 2
        int[] nums2 = {4, 7, 4, 7};
        int target2 = 0;
        int result2 = solution.shortestSegmentWithTargetXor(nums2, target2);
        System.out.println("Example 2:");
        System.out.println("nums = " + solution.arrayToString(nums2));
        System.out.println("target = " + target2);
        System.out.println("Shortest valid segment length = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional test: single element matches target
        int[] nums3 = {8, 2, 6};
        int target3 = 2;
        int result3 = solution.shortestSegmentWithTargetXor(nums3, target3);
        System.out.println("Additional Test 1:");
        System.out.println("nums = " + solution.arrayToString(nums3));
        System.out.println("target = " + target3);
        System.out.println("Shortest valid segment length = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        // Additional test: no valid segment
        int[] nums4 = {1, 1, 1};
        int target4 = 4;
        int result4 = solution.shortestSegmentWithTargetXor(nums4, target4);
        System.out.println("Additional Test 2:");
        System.out.println("nums = " + solution.arrayToString(nums4));
        System.out.println("target = " + target4);
        System.out.println("Shortest valid segment length = " + result4);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional test: target zero with repeated prefix XORs
        int[] nums5 = {1, 2, 3, 2, 1};
        int target5 = 0;
        int result5 = solution.shortestSegmentWithTargetXor(nums5, target5);
        System.out.println("Additional Test 3:");
        System.out.println("nums = " + solution.arrayToString(nums5));
        System.out.println("target = " + target5);
        System.out.println("Shortest valid segment length = " + result5);
        System.out.println("Expected = 3");
    }
}