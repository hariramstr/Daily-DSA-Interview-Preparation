import java.util.*;

/*
 * Title: Maximum Consecutive Shelves After One Label Fix
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A warehouse stores boxes on shelves labeled with positive integers. Ideally, the labels
 * should form a consecutive run such as [7, 8, 9, 10]. However, one shelf label may have
 * been printed incorrectly.
 *
 * You are given an integer array labels, where labels[i] is the label currently printed on
 * the i-th shelf in order from left to right. You may change the value of at most one element
 * in the array to any positive integer you want.
 *
 * Return the maximum possible length of a contiguous subarray that can be made strictly
 * consecutive with step 1 after performing at most one change. A subarray is valid if,
 * after the optional change, its values can become [x, x+1, x+2, ..., x+len-1] for some
 * positive integer x, while keeping the same order of positions.
 *
 * In other words, within the chosen contiguous segment, every adjacent difference must be
 * exactly 1 after changing at most one element in that segment.
 *
 * Your task is to compute the longest such segment.
 *
 * Constraints:
 * - 1 <= labels.length <= 200000
 * - 1 <= labels[i] <= 1000000000
 * - You may modify at most one element in the chosen subarray
 * - The replacement value must be a positive integer
 *
 * Example 1:
 * Input: labels = [4, 5, 9, 7, 8]
 * Output: 5
 * Explanation: Change the third value 9 to 6. The entire array becomes [4, 5, 6, 7, 8], which is consecutive.
 *
 * Example 2:
 * Input: labels = [10, 11, 13, 14, 20]
 * Output: 4
 * Explanation: The subarray [10, 11, 13, 14] can be fixed by changing 13 to 12, giving [10, 11, 12, 13].
 * No length-5 segment can be made consecutive with only one change.
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous subarray that can be turned into a strictly
     * consecutive sequence with step 1 by changing at most one element.
     *
     * Core idea:
     * For a chosen subarray [l..r], there must exist some starting value x such that
     * labels[i] = x + (i - l) for all positions except possibly one position.
     *
     * Rearranging:
     * labels[i] - i = x - l
     *
     * Therefore, inside a valid subarray, the transformed values:
     *     b[i] = labels[i] - i
     * must all be equal, except possibly one index that we may change.
     *
     * So the problem becomes:
     * Find the longest contiguous subarray in b where all values are the same except at most one.
     *
     * We solve this with a sliding window that tracks frequencies of b[i].
     * A window is valid if:
     *     windowLength - maxFrequency <= 1
     * because then all but at most one element already match the same target value.
     *
     * One more important detail:
     * The replacement value must be positive.
     * If we choose the target transformed value c, then the resulting consecutive sequence is:
     *     a'[i] = c + i
     * For the leftmost index L in the window, the first value becomes c + L.
     * Since all values must be positive, we need:
     *     c + L >= 1
     *
     * In any valid window, the target c must be one of the existing transformed values in the window
     * if the window length is at least 2, because at least windowLength - 1 elements already equal c.
     * So we only need to know whether among the values currently present in the window there exists
     * some c with:
     *     c >= 1 - L
     *
     * We maintain:
     * - frequency map of transformed values
     * - TreeMap from frequency -> count of how many distinct values have that frequency
     *   so we can get maxFrequency efficiently
     * - TreeSet of transformed values currently present, so we can get the largest present value
     *   and check positivity feasibility efficiently
     *
     * @param labels the array of shelf labels
     * @return the maximum valid contiguous subarray length
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int maximumConsecutiveShelvesAfterOneFix(int[] labels) {
        int n = labels.length;
        if (n <= 1) {
            return n;
        }

        long[] transformed = new long[n];
        for (int i = 0; i < n; i++) {
            transformed[i] = (long) labels[i] - i;
        }

        Map<Long, Integer> freq = new HashMap<>();
        TreeMap<Integer, Integer> freqCount = new TreeMap<>();
        TreeSet<Long> presentValues = new TreeSet<>();

        int left = 0;
        int answer = 1;

        for (int right = 0; right < n; right++) {
            addValue(transformed[right], freq, freqCount, presentValues);

            /*
             * We shrink the window until it satisfies BOTH conditions:
             *
             * 1) At most one mismatch relative to some common transformed value:
             *      windowLength - maxFrequency <= 1
             *
             * 2) Positivity is feasible:
             *    There exists some transformed value c currently present in the window such that
             *      c >= 1 - left
             *    because then the first value of the consecutive run at index 'left' is:
             *      c + left >= 1
             *
             * Why checking only present values is enough:
             * In a valid window of length >= 2, the target transformed value must match at least
             * windowLength - 1 elements, so it is definitely present in the window.
             */
            while (!isWindowValid(left, right, freqCount, presentValues)) {
                removeValue(transformed[left], freq, freqCount, presentValues);
                left++;
            }

            answer = Math.max(answer, right - left + 1);
        }

        return answer;
    }

    /**
     * Adds one transformed value into the current sliding window data structures.
     *
     * @param value the transformed value to add
     * @param freq frequency map for transformed values
     * @param freqCount map from frequency -> how many distinct values currently have that frequency
     * @param presentValues sorted set of transformed values currently present in the window
     * @return nothing
     * Time complexity: O(log n)
     * Space complexity: O(1) extra beyond maintained structures
     */
    public void addValue(long value,
                         Map<Long, Integer> freq,
                         TreeMap<Integer, Integer> freqCount,
                         TreeSet<Long> presentValues) {
        int oldFreq = freq.getOrDefault(value, 0);
        int newFreq = oldFreq + 1;

        if (oldFreq > 0) {
            decrementCount(freqCount, oldFreq);
        }

        freq.put(value, newFreq);
        incrementCount(freqCount, newFreq);
        presentValues.add(value);
    }

    /**
     * Removes one transformed value from the current sliding window data structures.
     *
     * @param value the transformed value to remove
     * @param freq frequency map for transformed values
     * @param freqCount map from frequency -> how many distinct values currently have that frequency
     * @param presentValues sorted set of transformed values currently present in the window
     * @return nothing
     * Time complexity: O(log n)
     * Space complexity: O(1) extra beyond maintained structures
     */
    public void removeValue(long value,
                            Map<Long, Integer> freq,
                            TreeMap<Integer, Integer> freqCount,
                            TreeSet<Long> presentValues) {
        int oldFreq = freq.get(value);
        int newFreq = oldFreq - 1;

        decrementCount(freqCount, oldFreq);

        if (newFreq == 0) {
            freq.remove(value);
            presentValues.remove(value);
        } else {
            freq.put(value, newFreq);
            incrementCount(freqCount, newFreq);
        }
    }

    /**
     * Checks whether the current sliding window [left..right] is valid.
     *
     * A window is valid if:
     * 1) All transformed values are equal except at most one:
     *      windowLength - maxFrequency <= 1
     * 2) The resulting consecutive sequence can start with a positive integer:
     *      there exists a present transformed value c such that c + left >= 1
     *      equivalently c >= 1 - left
     *
     * @param left left boundary of the window
     * @param right right boundary of the window
     * @param freqCount map from frequency -> count of distinct values with that frequency
     * @param presentValues sorted set of transformed values currently present in the window
     * @return true if the window is valid, false otherwise
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public boolean isWindowValid(int left,
                                 int right,
                                 TreeMap<Integer, Integer> freqCount,
                                 TreeSet<Long> presentValues) {
        int windowLength = right - left + 1;
        int maxFrequency = freqCount.lastKey();

        if (windowLength - maxFrequency > 1) {
            return false;
        }

        long requiredMinTransformed = 1L - left;
        Long largestPresent = presentValues.last();

        return largestPresent >= requiredMinTransformed;
    }

    /**
     * Increments the count associated with a key in a TreeMap.
     *
     * @param map the map to update
     * @param key the key whose count should be incremented
     * @return nothing
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public void incrementCount(TreeMap<Integer, Integer> map, int key) {
        map.put(key, map.getOrDefault(key, 0) + 1);
    }

    /**
     * Decrements the count associated with a key in a TreeMap and removes the key if the count becomes zero.
     *
     * @param map the map to update
     * @param key the key whose count should be decremented
     * @return nothing
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public void decrementCount(TreeMap<Integer, Integer> map, int key) {
        int count = map.get(key);
        if (count == 1) {
            map.remove(key);
        } else {
            map.put(key, count - 1);
        }
    }

    /**
     * Convenience wrapper matching a shorter problem-style method name.
     *
     * @param labels the array of shelf labels
     * @return the maximum valid contiguous subarray length
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int solve(int[] labels) {
        return maximumConsecutiveShelvesAfterOneFix(labels);
    }

    /**
     * Demonstrates the solution on the sample test cases and a few extra cases.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total input size log n) across demonstrated examples
     * Space complexity: O(n) for each call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] labels1 = {4, 5, 9, 7, 8};
        int[] labels2 = {10, 11, 13, 14, 20};
        int[] labels3 = {1};
        int[] labels4 = {5, 6, 7, 8};
        int[] labels5 = {3, 10, 5, 6};

        System.out.println(solution.maximumConsecutiveShelvesAfterOneFix(labels1)); // Expected: 5
        System.out.println(solution.maximumConsecutiveShelvesAfterOneFix(labels2)); // Expected: 4
        System.out.println(solution.maximumConsecutiveShelvesAfterOneFix(labels3)); // Expected: 1
        System.out.println(solution.maximumConsecutiveShelvesAfterOneFix(labels4)); // Expected: 4
        System.out.println(solution.maximumConsecutiveShelvesAfterOneFix(labels5)); // Example extra case
    }
}