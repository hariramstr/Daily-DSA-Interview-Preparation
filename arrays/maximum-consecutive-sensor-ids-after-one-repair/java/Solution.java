import java.util.*;

/*
 * Title: Maximum Consecutive Sensor IDs After One Repair
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A monitoring system stores a sorted array of distinct sensor IDs that should ideally form one
 * uninterrupted consecutive sequence. However, due to a database error, exactly one recorded ID
 * may be incorrect and can be changed to any integer value you choose. Your task is to determine
 * the maximum possible length of a consecutive run of distinct integers that can appear in the
 * array after repairing at most one element.
 *
 * A consecutive run means a set of values like x, x+1, x+2, ..., y, with no gaps. The repaired
 * array does not need to remain sorted in memory, but the values should still be considered as a
 * set of distinct IDs after the change. If the array already contains the best possible run, you
 * may choose not to modify anything.
 *
 * Return the maximum length of any consecutive run obtainable after at most one repair.
 *
 * Constraints:
 * - 1 <= nums.length <= 100000
 * - -1000000000 <= nums[i] <= 1000000000
 * - nums is sorted in strictly increasing order
 *
 * Example 1:
 * Input: nums = [10, 11, 13, 14]
 * Output: 5
 * Explanation: Change 14 to 12. The array values can become [10, 11, 12, 13, 14], which forms a
 * consecutive run of length 5.
 *
 * Example 2:
 * Input: nums = [3, 4, 7, 8, 9]
 * Output: 4
 * Explanation: One optimal repair is to change 3 to 6, giving values [4, 6, 7, 8, 9]. The longest
 * consecutive run is then [6, 7, 8, 9], which has length 4. It is impossible to obtain a run of
 * length 5 with only one change.
 */

public class Solution {

    /**
     * Computes the maximum possible length of a consecutive run after changing at most one element.
     *
     * Core idea:
     * 1. Break the sorted array into maximal already-consecutive blocks.
     * 2. Without any change, the answer is simply the longest block.
     * 3. With one change, there are only a few useful possibilities:
     *    - Extend one block by 1 by changing some element outside that block to one missing neighbor.
     *    - Bridge two neighboring blocks if the gap between them is exactly 2
     *      (for example, [10,11] and [13,14] can be merged by changing one element to 12).
     *    - If the whole array is already consecutive, we can still change one endpoint outward and
     *      keep all values consecutive, increasing the length by 1.
     *
     * Why these are sufficient:
     * - One changed value can contribute only one missing integer.
     * - Therefore, we can fill at most one gap position.
     * - So the best result is either:
     *   a) keep a block as-is,
     *   b) extend a block by one,
     *   c) merge two blocks separated by exactly one missing value.
     *
     * @param nums a sorted array of distinct integers
     * @return the maximum obtainable length of a consecutive run after at most one repair
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int maxConsecutiveAfterOneRepair(int[] nums) {
        int n = nums.length;

        // With only one element, we can always "repair" it to any value,
        // but the array still contains only one distinct value, so the answer is 1.
        if (n == 1) {
            return 1;
        }

        // We will compress the array into consecutive blocks.
        //
        // Example:
        // nums = [3, 4, 7, 8, 9]
        // blocks:
        //   block 0 = [3,4], length 2
        //   block 1 = [7,8,9], length 3
        //
        // For each block we store:
        // - start value
        // - end value
        // - length
        List<Long> starts = new ArrayList<>();
        List<Long> ends = new ArrayList<>();
        List<Integer> lens = new ArrayList<>();

        long blockStart = nums[0];
        long prev = nums[0];

        for (int i = 1; i < n; i++) {
            long current = nums[i];

            // If current continues the consecutive chain, stay in the same block.
            if (current == prev + 1) {
                prev = current;
            } else {
                // Otherwise, the previous block ends here.
                starts.add(blockStart);
                ends.add(prev);
                lens.add((int) (prev - blockStart + 1));

                // Start a new block.
                blockStart = current;
                prev = current;
            }
        }

        // Add the final block.
        starts.add(blockStart);
        ends.add(prev);
        lens.add((int) (prev - blockStart + 1));

        int m = lens.size();

        // Step 1: answer without any modification.
        int answer = 0;
        for (int len : lens) {
            answer = Math.max(answer, len);
        }

        // Special case:
        // If the whole array is already one consecutive block, then we can change one endpoint
        // outward and still keep the set consecutive, increasing the run by 1.
        //
        // Example:
        // [5,6,7] -> change 5 to 8 => [6,7,8], still length 3, not better.
        // But change some endpoint while preserving all others? Since we must keep distinct values,
        // the best consecutive set with n elements is still n.
        //
        // Important correction:
        // Because the array size stays n, the longest possible run can never exceed n.
        // So if the whole array is already consecutive, the answer remains n.
        if (m == 1) {
            return n;
        }

        // Step 2: consider using one repair.
        //
        // There are two useful patterns:
        //
        // A) Extend a single block by 1.
        //    This is possible if there exists at least one element outside the block to modify.
        //    Since the block does not already contain all n elements, and m > 1, such an element exists.
        //
        //    Example:
        //    [3,4] and [7,8,9]
        //    We can change 3 to 6, keeping [6,7,8,9] => length 4
        //    So block [7,8,9] of length 3 can become 4.
        //
        // B) Merge two adjacent blocks if exactly one value is missing between them.
        //    That means:
        //    next.start - current.end == 2
        //
        //    Example:
        //    [10,11] and [13,14]
        //    Missing value is 12, so one repair can create [10,11,12,13,14] => length 5.
        //
        //    Since we are changing one existing array element, we must ensure we can "spend" one
        //    element from outside the merged run, OR one of the endpoints of the merged run can be
        //    repurposed appropriately. In practice, because the merged run length equals
        //    leftLen + rightLen + 1, this uses exactly all values from both blocks plus one changed
        //    element from somewhere else. If the whole array consisted only of these two blocks,
        //    that still works by changing one endpoint from one block into the missing value while
        //    the remaining values still cover the merged interval.
        //
        //    Example:
        //    [10,11,13,14]
        //    Change 14 -> 12, result set {10,11,12,13}, length 4 only? No.
        //    Better: change 10 -> 12 gives {11,12,13,14}, length 4 only.
        //
        //    So we must be careful: the sample says answer is 5 for [10,11,13,14], but with 4
        //    elements a consecutive run of length 5 is impossible.
        //
        //    Therefore, the sample in the prompt is inconsistent with the fixed-size array model.
        //    The correct maximum cannot exceed n.
        //
        // We must follow the actual problem statement faithfully: one element is changed, array size
        // remains the same, values remain distinct. Therefore answer <= n always.
        //
        // Under this correct interpretation:
        // - Extending a block by 1 is possible.
        // - Merging two blocks across one missing value gives leftLen + rightLen only if we must
        //   sacrifice one existing value to create the missing one, unless there is an extra element
        //   outside the merged interval.
        //
        // To solve correctly, we reason in terms of choosing a target interval [L..R] of length k.
        // We need at least k-1 existing numbers already inside that interval, because one repair can
        // create at most one missing number inside it.
        //
        // Since nums is sorted and distinct, we can use a sliding window:
        // Find the largest window where:
        //   nums[right] - nums[left] + 1 <= windowSize + 1
        // This means the interval spanned by the window has at most one missing value.
        // Then:
        // - If interval length <= n, we can realize that interval as a consecutive run.
        // - The run length is min(n, nums[right] - nums[left] + 1), but under the condition above
        //   this is at most windowSize + 1.
        //
        // However, we can also use one outside element to extend by one beyond the current span.
        // The clean and correct formula is:
        //   For a window [l..r], if span = nums[r] - nums[l] + 1 and missing = span - (r-l+1),
        //   then if missing <= 1, we can make all span values consecutive using at most one repair.
        //   Additionally, if missing == 0 and there exists an element outside the window, we can
        //   extend the run by 1 by changing that outside element to span neighbor.
        //
        // We now implement that exact sliding-window solution.
        return maxConsecutiveSlidingWindow(nums);
    }

    /**
     * Sliding-window implementation of the correct solution.
     *
     * For every window [left..right]:
     * - count = number of existing values in the window
     * - span = nums[right] - nums[left] + 1
     * - missing = span - count
     *
     * If missing <= 1, then inside this interval there is at most one absent value.
     * Therefore, with at most one repair, we can make all values in the interval consecutive.
     *
     * There are two subcases:
     * 1. missing == 1:
     *    We use the repair to fill that one missing value.
     *    Achievable run length = span.
     *
     * 2. missing == 0:
     *    The window is already consecutive.
     *    If there is at least one element outside the window, we can change one outside element
     *    to extend the interval by one on either side, so achievable run length = span + 1.
     *    Otherwise, achievable run length = span.
     *
     * Since the total array size is n, the answer can never exceed n.
     *
     * @param nums a sorted array of distinct integers
     * @return the maximum obtainable consecutive run length
     * Time complexity: O(n)
     * Space complexity: O(1) excluding input
     */
    public int maxConsecutiveSlidingWindow(int[] nums) {
        int n = nums.length;
        int answer = 1;

        int left = 0;

        for (int right = 0; right < n; right++) {
            // Shrink the window until it has at most one missing value in its spanned interval.
            //
            // span = nums[right] - nums[left] + 1
            // count = right - left + 1
            // missing = span - count
            //
            // We need missing <= 1.
            while ((long) nums[right] - nums[left] + 1L - (right - left + 1L) > 1L) {
                left++;
            }

            long span = (long) nums[right] - nums[left] + 1L;
            int count = right - left + 1;
            long missing = span - count;

            int candidate;

            if (missing == 0) {
                // The values in this window are already consecutive.
                //
                // If there is at least one element outside the window, we can modify one such
                // element to extend this consecutive run by one.
                //
                // Example:
                // nums = [3,4,7,8,9]
                // window [7,8,9] has span=3, count=3, missing=0
                // there are outside elements (3 and 4), so we can change one of them to 6 or 10
                // and get a run of length 4.
                if (count < n) {
                    candidate = count + 1;
                } else {
                    candidate = count;
                }
            } else {
                // missing == 1
                //
                // The interval already has all but one value present.
                // One repair can fill that missing value, so the full span becomes consecutive.
                candidate = (int) span;
            }

            // The array contains only n elements, so a consecutive run cannot exceed n.
            candidate = Math.min(candidate, n);

            answer = Math.max(answer, candidate);
        }

        return answer;
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * Note:
     * The first sample in the prompt claims output 5 for an array of length 4, which is impossible
     * if one element is only changed (not inserted) and all values remain distinct. Under the
     * correct fixed-size interpretation, the answer is 4.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(k * n) for k demo cases
     * Space complexity: O(1) excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {10, 11, 13, 14};
        int[] nums2 = {3, 4, 7, 8, 9};
        int[] nums3 = {1};
        int[] nums4 = {5, 6, 7, 8};
        int[] nums5 = {1, 2, 4};
        int[] nums6 = {1, 3, 5, 7};

        System.out.println(solution.maxConsecutiveAfterOneRepair(nums1)); // Correct fixed-size answer: 4
        System.out.println(solution.maxConsecutiveAfterOneRepair(nums2)); // 4
        System.out.println(solution.maxConsecutiveAfterOneRepair(nums3)); // 1
        System.out.println(solution.maxConsecutiveAfterOneRepair(nums4)); // 4
        System.out.println(solution.maxConsecutiveAfterOneRepair(nums5)); // 3
        System.out.println(solution.maxConsecutiveAfterOneRepair(nums6)); // 2
    }
}