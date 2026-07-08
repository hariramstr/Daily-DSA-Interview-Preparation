import java.util.*;

/*
 * Title: Maximum Uniform Banner Width for Ad Slots
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given an array `slots` where `slots[i]` is the width of the `i`-th advertising
 * space available on a website. A design team wants to create banner creatives of one
 * uniform integer width `w`, and each slot can be split into multiple banners as long as
 * every produced banner has width exactly `w`. Any leftover width in a slot is discarded
 * and cannot be combined with leftover width from another slot.
 *
 * Given an integer `k`, return the maximum possible integer banner width `w` such that
 * the total number of banners produced across all slots is at least `k`. If it is impossible
 * to produce `k` banners even with width `1`, return `0`.
 *
 * Formally, for a chosen width `w`, slot `i` contributes floor(slots[i] / w) banners.
 * You must find the largest `w` for which the sum of these values over all slots is at least `k`.
 *
 * This problem is intended to be solved efficiently for large inputs. A linear scan over all
 * possible widths will be too slow when slot widths are large, so you should take advantage
 * of the monotonic relationship between banner width and the number of banners that can be produced.
 *
 * Constraints:
 * - 1 <= slots.length <= 100000
 * - 1 <= slots[i] <= 1000000000
 * - 1 <= k <= 1000000000
 *
 * Example 1:
 * Input: slots = [9, 7, 5], k = 5
 * Output: 3
 * Explanation:
 * With width 3, the slots produce:
 * - 9 / 3 = 3 banners
 * - 7 / 3 = 2 banners
 * - 5 / 3 = 1 banner
 * Total = 6, which is enough.
 *
 * With width 4, the slots produce:
 * - 9 / 4 = 2 banners
 * - 7 / 4 = 1 banner
 * - 5 / 4 = 1 banner
 * Total = 4, which is not enough.
 *
 * So the maximum valid width is 3.
 *
 * Example 2:
 * Input: slots = [2, 3], k = 10
 * Output: 0
 * Explanation:
 * Even with width 1, the total number of banners is:
 * - 2 / 1 = 2
 * - 3 / 1 = 3
 * Total = 5, which is less than 10.
 * Therefore, producing 10 banners is impossible.
 */

public class Solution {

    /**
     * Finds the maximum possible uniform integer banner width such that at least k banners
     * can be produced from the given slots.
     *
     * The key idea is binary search:
     * - If a width w is feasible (we can produce at least k banners),
     *   then every smaller width is also feasible.
     * - If a width w is not feasible, then every larger width is also not feasible.
     *
     * This monotonic behavior allows us to search efficiently for the largest feasible width.
     *
     * @param slots the widths of available advertising slots
     * @param k the minimum number of banners that must be produced
     * @return the maximum integer banner width that allows producing at least k banners;
     *         returns 0 if producing k banners is impossible even with width 1
     *
     * Time complexity: O(n log M), where n is slots.length and M is the maximum slot width
     * Space complexity: O(1), excluding input storage
     */
    public int maximumBannerWidth(int[] slots, int k) {
        // Step 1:
        // Before doing binary search, we determine the largest slot width.
        // This gives us the maximum possible candidate width.
        int maxWidth = 0;

        // We also compute the total width sum using long.
        // Why long?
        // Because slots.length can be up to 100000 and each slots[i] can be up to 1e9,
        // so the total can be as large as 1e14, which does not fit in int.
        long totalWidth = 0L;

        for (int slot : slots) {
            maxWidth = Math.max(maxWidth, slot);
            totalWidth += slot;
        }

        // Step 2:
        // Quick impossibility check.
        // If width = 1, each slot contributes exactly slot banners.
        // So the total number of banners possible at width 1 is totalWidth.
        // If even that is less than k, then the answer must be 0.
        if (totalWidth < k) {
            return 0;
        }

        // Step 3:
        // Binary search over possible widths.
        //
        // Search space:
        // - Minimum possible width is 1
        // - Maximum possible width is maxWidth
        //
        // We want the LARGEST feasible width.
        int left = 1;
        int right = maxWidth;
        int answer = 0;

        while (left <= right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check whether this candidate width is feasible.
            if (canProduceAtLeastK(slots, mid, k)) {
                // If width mid works, it is a valid candidate answer.
                answer = mid;

                // But we are asked for the MAXIMUM valid width,
                // so we try to go larger.
                left = mid + 1;
            } else {
                // If width mid does not work, then any larger width also won't work.
                // So we must search the smaller half.
                right = mid - 1;
            }
        }

        // Step 5:
        // answer stores the largest feasible width found.
        return answer;
    }

    /**
     * Checks whether a given banner width can produce at least k banners in total.
     *
     * For each slot, the number of banners produced is floor(slot / width).
     * We sum these values and stop early as soon as the total reaches or exceeds k.
     *
     * Early stopping is helpful because:
     * - It avoids unnecessary work once feasibility is already confirmed.
     * - It keeps the method efficient in practice.
     *
     * @param slots the widths of available advertising slots
     * @param width the candidate uniform banner width to test
     * @param k the required minimum number of banners
     * @return true if at least k banners can be produced using the given width; false otherwise
     *
     * Time complexity: O(n) in the worst case, where n is slots.length
     * Space complexity: O(1)
     */
    public boolean canProduceAtLeastK(int[] slots, int width, int k) {
        // Use long because the total number of banners can exceed int during accumulation.
        long count = 0L;

        // Step through every slot and count how many banners of size "width" it can produce.
        for (int slot : slots) {
            count += slot / width;

            // Very important optimization:
            // As soon as we know count >= k, we can return true immediately.
            // There is no need to continue counting.
            if (count >= k) {
                return true;
            }
        }

        // If we finish the loop and still have fewer than k banners, width is not feasible.
        return false;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * This method prints:
     * - the input arrays
     * - the value of k
     * - the computed maximum banner width
     *
     * It also includes the expected outputs so the behavior is easy to verify.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n log M) per demonstration call
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] slots1 = {9, 7, 5};
        int k1 = 5;
        int result1 = solution.maximumBannerWidth(slots1, k1);

        System.out.println("Example 1:");
        System.out.println("slots = " + Arrays.toString(slots1) + ", k = " + k1);
        System.out.println("Maximum uniform banner width = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Example 2
        int[] slots2 = {2, 3};
        int k2 = 10;
        int result2 = solution.maximumBannerWidth(slots2, k2);

        System.out.println("Example 2:");
        System.out.println("slots = " + Arrays.toString(slots2) + ", k = " + k2);
        System.out.println("Maximum uniform banner width = " + result2);
        System.out.println("Expected = 0");
        System.out.println();

        // Additional quick sanity checks for beginner-friendly demonstration

        // If we need exactly 3 banners from [8], the best width is 2
        // because 8 / 2 = 4 banners, but 8 / 3 = 2 banners which is not enough.
        int[] slots3 = {8};
        int k3 = 3;
        int result3 = solution.maximumBannerWidth(slots3, k3);

        System.out.println("Additional Example 3:");
        System.out.println("slots = " + Arrays.toString(slots3) + ", k = " + k3);
        System.out.println("Maximum uniform banner width = " + result3);
        System.out.println("Expected = 2");
        System.out.println();

        // If we need 1 banner from [100], the best width is 100.
        int[] slots4 = {100};
        int k4 = 1;
        int result4 = solution.maximumBannerWidth(slots4, k4);

        System.out.println("Additional Example 4:");
        System.out.println("slots = " + Arrays.toString(slots4) + ", k = " + k4);
        System.out.println("Maximum uniform banner width = " + result4);
        System.out.println("Expected = 100");
    }
}