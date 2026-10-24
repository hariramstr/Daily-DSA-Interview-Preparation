import java.util.*;

/*
 * Title: Longest Toll Segment Within Daily Pass Limit
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A driver is traveling along a highway divided into consecutive road segments.
 * For each segment i, you are given tolls[i], the toll cost required to drive
 * through that segment. The driver also owns a special daily pass that can fully
 * waive the toll on at most k segments within one continuous part of the trip.
 *
 * Your task is to find the maximum number of consecutive segments the driver can
 * travel such that, after choosing at most k segments in that interval to waive
 * with the pass, the total amount actually paid is no more than budget.
 *
 * Formally, for any contiguous subarray tolls[l..r], the driver may select up to
 * k elements in that subarray and treat their cost as 0. The remaining segment
 * costs must sum to at most budget. Return the largest possible length of such a
 * subarray.
 *
 * This models a realistic route-planning system where a subscription can cover
 * only a limited number of toll booths in a chosen stretch, and the driver wants
 * the longest uninterrupted drive without exceeding a spending cap.
 *
 * Constraints:
 * - 1 <= tolls.length <= 200000
 * - 0 <= tolls[i] <= 1000000000
 * - 0 <= budget <= 100000000000000
 * - 0 <= k <= tolls.length
 * - The answer must be computed efficiently for large inputs.
 *
 * Example 1:
 * Input: tolls = [7, 2, 5, 10, 3], budget = 10, k = 1
 * Output: 3
 * Explanation: Consider the window [7, 2, 5]. If the pass is used on 7, the paid
 * total is 2 + 5 = 7, which is within budget. No window of length 4 can be made
 * affordable with only one waived segment.
 *
 * Example 2:
 * Input: tolls = [4, 8, 1, 9, 2, 6], budget = 11, k = 2
 * Output: 5
 * Explanation: For the window [8, 1, 9, 2, 6], waive the two most expensive tolls,
 * 9 and 8. The paid total becomes 1 + 2 + 6 = 9, so length 5 is valid. The full
 * array of length 6 would still cost 13 after waiving two segments, so it is not valid.
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous subarray such that after waiving
     * at most k tolls inside that subarray, the remaining paid sum is at most budget.
     *
     * Core idea:
     * For any fixed window, the best way to minimize paid cost is to waive the k
     * largest tolls in that window (or fewer if the window has fewer than k elements).
     * Therefore:
     *
     * paidCost(window) = totalSum(window) - sumOfLargestKElements(window)
     *
     * We binary search the answer length L.
     * For each candidate L, we check whether there exists any window of length L
     * whose paid cost is <= budget.
     *
     * To support this efficiently, we maintain:
     * 1) total sum of the current window
     * 2) a dynamic split of window elements into:
     *    - "large": the largest up to k elements currently chosen to be waived
     *    - "small": all remaining elements
     * 3) sumLarge = sum of elements in "large"
     *
     * Then paid cost = windowSum - sumLarge.
     *
     * @param tolls the toll cost for each road segment
     * @param budget the maximum total amount that may actually be paid
     * @param k the maximum number of segments whose toll can be waived
     * @return the largest valid contiguous segment length
     * Time complexity: O(n log^2 n), where n = tolls.length
     * Space complexity: O(n)
     */
    public int longestTollSegment(int[] tolls, long budget, int k) {
        int n = tolls.length;

        int left = 0;
        int right = n;
        int answer = 0;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (existsValidWindowOfLength(tolls, budget, k, mid)) {
                answer = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether there exists at least one contiguous window of the given length
     * such that after waiving at most k tolls, the paid sum is at most budget.
     *
     * We slide a fixed-size window across the array.
     * For each window:
     * - windowSum stores the total sum of all elements in the window
     * - WaiverTracker stores the largest up to k elements and their sum
     * - paidCost = windowSum - waivedSum
     *
     * If paidCost <= budget for any window, this length is feasible.
     *
     * @param tolls the toll array
     * @param budget the maximum allowed paid sum
     * @param k the maximum number of waived tolls
     * @param len the candidate window length to test
     * @return true if some window of length len is valid, otherwise false
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public boolean existsValidWindowOfLength(int[] tolls, long budget, int k, int len) {
        if (len == 0) {
            return true;
        }

        WaiverTracker tracker = new WaiverTracker(k);
        long windowSum = 0L;

        for (int i = 0; i < tolls.length; i++) {
            int valueToAdd = tolls[i];

            windowSum += valueToAdd;
            tracker.add(valueToAdd);

            if (i >= len) {
                int valueToRemove = tolls[i - len];
                windowSum -= valueToRemove;
                tracker.remove(valueToRemove);
            }

            if (i >= len - 1) {
                long paidCost = windowSum - tracker.getWaivedSum();

                if (paidCost <= budget) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Helper data structure that maintains the largest up to k elements of the current
     * multiset, along with their sum.
     *
     * We use two TreeMaps as multisets:
     * - small: elements not currently waived
     * - large: elements currently waived (these are the largest up to k elements)
     *
     * Invariants:
     * 1) large contains at most k elements
     * 2) Every element in large is >= every element in small
     * 3) Therefore, large is exactly the set of largest up to k elements
     *
     * This lets us compute the waived sum in O(1), while add/remove/rebalance
     * operations take O(log n).
     */
    static class WaiverTracker {
        private final int k;
        private final TreeMap<Integer, Integer> small;
        private final TreeMap<Integer, Integer> large;
        private int smallSize;
        private int largeSize;
        private long sumLarge;

        /**
         * Creates a tracker for maintaining the largest up to k elements.
         *
         * @param k the maximum number of elements that may be waived
         */
        WaiverTracker(int k) {
            this.k = k;
            this.small = new TreeMap<>();
            this.large = new TreeMap<>();
            this.smallSize = 0;
            this.largeSize = 0;
            this.sumLarge = 0L;
        }

        /**
         * Adds one value into the current multiset and restores the invariants.
         *
         * Detailed behavior:
         * - If k == 0, nothing can be waived, so everything goes to small.
         * - Otherwise:
         *   1) If large has room, put the new value into large.
         *   2) If large is full:
         *      - Compare the new value with the smallest value currently in large.
         *      - If the new value is bigger, it deserves to be among the waived values:
         *        move the smallest waived value from large to small, and insert the new
         *        value into large.
         *      - Otherwise, insert the new value into small.
         * - Finally rebalance to guarantee all invariants.
         *
         * @param value the value to add
         * Time complexity: O(log n)
         * Space complexity: O(1) extra beyond stored elements
         */
        public void add(int value) {
            if (k == 0) {
                addToMap(small, value);
                smallSize++;
                return;
            }

            if (largeSize < k) {
                addToMap(large, value);
                largeSize++;
                sumLarge += value;
            } else {
                int smallestLarge = large.firstKey();

                if (value > smallestLarge) {
                    removeFromMap(large, smallestLarge);
                    largeSize--;
                    sumLarge -= smallestLarge;

                    addToMap(small, smallestLarge);
                    smallSize++;

                    addToMap(large, value);
                    largeSize++;
                    sumLarge += value;
                } else {
                    addToMap(small, value);
                    smallSize++;
                }
            }

            rebalance();
        }

        /**
         * Removes one occurrence of a value from the current multiset and restores
         * the invariants.
         *
         * Detailed behavior:
         * - If k == 0, all values are in small, so remove from small.
         * - Otherwise:
         *   1) If the value exists in large, remove it from large and subtract it
         *      from sumLarge.
         *   2) Else remove it from small.
         * - Then rebalance:
         *   - If large has fewer than k elements and small is non-empty, move the
         *     largest element from small into large.
         *   - If ordering is violated (some small element is larger than some large
         *     element), swap boundary elements until fixed.
         *
         * @param value the value to remove
         * Time complexity: O(log n)
         * Space complexity: O(1) extra beyond stored elements
         */
        public void remove(int value) {
            if (k == 0) {
                removeFromMap(small, value);
                smallSize--;
                return;
            }

            if (large.containsKey(value)) {
                removeFromMap(large, value);
                largeSize--;
                sumLarge -= value;
            } else {
                removeFromMap(small, value);
                smallSize--;
            }

            rebalance();
        }

        /**
         * Returns the sum of the currently waived elements, i.e. the largest up to k
         * elements in the current window.
         *
         * @return the sum of waived tolls
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        public long getWaivedSum() {
            return sumLarge;
        }

        /**
         * Restores the two key invariants:
         *
         * Invariant A:
         * large must contain exactly min(k, totalSize) elements whenever possible.
         *
         * Invariant B:
         * every element in large must be >= every element in small.
         *
         * We enforce them in two phases:
         *
         * Phase 1: Size balancing
         * - If large has too many elements, move the smallest from large to small.
         * - If large has too few elements, move the largest from small to large.
         *
         * Phase 2: Order balancing
         * - If the largest element in small is greater than the smallest element in large,
         *   they are on the wrong sides. Swap them.
         * - Repeat until the order property is satisfied.
         *
         * Because TreeMap gives us boundary elements efficiently, each move/swap is
         * O(log n).
         */
        private void rebalance() {
            while (largeSize > k) {
                int move = large.firstKey();
                removeFromMap(large, move);
                largeSize--;
                sumLarge -= move;

                addToMap(small, move);
                smallSize++;
            }

            while (largeSize < k && smallSize > 0) {
                int move = small.lastKey();
                removeFromMap(small, move);
                smallSize--;

                addToMap(large, move);
                largeSize++;
                sumLarge += move;
            }

            while (!small.isEmpty() && !large.isEmpty() && small.lastKey() > large.firstKey()) {
                int fromSmall = small.lastKey();
                int fromLarge = large.firstKey();

                removeFromMap(small, fromSmall);
                smallSize--;

                removeFromMap(large, fromLarge);
                largeSize--;
                sumLarge -= fromLarge;

                addToMap(small, fromLarge);
                smallSize++;

                addToMap(large, fromSmall);
                largeSize++;
                sumLarge += fromSmall;
            }
        }

        /**
         * Adds one occurrence of value into the given TreeMap-based multiset.
         *
         * @param map the multiset
         * @param value the value to insert
         */
        private void addToMap(TreeMap<Integer, Integer> map, int value) {
            map.put(value, map.getOrDefault(value, 0) + 1);
        }

        /**
         * Removes one occurrence of value from the given TreeMap-based multiset.
         *
         * @param map the multiset
         * @param value the value to remove
         */
        private void removeFromMap(TreeMap<Integer, Integer> map, int value) {
            int count = map.get(value);
            if (count == 1) {
                map.remove(value);
            } else {
                map.put(value, count - 1);
            }
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 3
     * Example 2 -> 5
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log^2 n) across the demonstrated calls
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] tolls1 = {7, 2, 5, 10, 3};
        long budget1 = 10L;
        int k1 = 1;
        int result1 = solution.longestTollSegment(tolls1, budget1, k1);
        System.out.println("Example 1 result: " + result1);

        int[] tolls2 = {4, 8, 1, 9, 2, 6};
        long budget2 = 11L;
        int k2 = 2;
        int result2 = solution.longestTollSegment(tolls2, budget2, k2);
        System.out.println("Example 2 result: " + result2);
    }
}