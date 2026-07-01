import java.util.*;

/*
 * Title: Longest Session Window With Bounded Error Dominance
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array events representing the event code recorded for each second of an
 * application session. Each events[i] is a positive integer. A contiguous window of the session
 * is called stable if no single event code is too dominant inside that window.
 *
 * More formally, for a window events[l..r], let len = r - l + 1, and let maxFreq be the highest
 * frequency of any event code inside the window. The window is considered stable if:
 *
 *     maxFreq * 2 <= len + k
 *
 * where k is a given non-negative integer tolerance.
 *
 * Your task is to return the length of the longest stable contiguous window.
 *
 * This models a monitoring system where a session is suspicious if one error or action code
 * overwhelms the rest of the activity. The tolerance k allows a limited amount of dominance
 * before the window is rejected.
 *
 * Constraints:
 * - 1 <= events.length <= 2 * 10^5
 * - 1 <= events[i] <= 10^9
 * - 0 <= k <= events.length
 *
 * Example 1:
 * Input: events = [4, 1, 4, 2, 4, 3, 2], k = 1
 * Output: 5
 *
 * Example 2:
 * Input: events = [7, 7, 7, 2, 3, 7, 4, 5], k = 0
 * Output: 5
 *
 * Notes:
 * - The answer depends on contiguous windows only.
 * - Different windows may have different dominant codes.
 * - The challenge is maintaining the window while tracking the current maximum frequency efficiently.
 */
public class Solution {

    /**
     * Computes the length of the longest stable contiguous window.
     *
     * Core idea:
     * We use binary search on the answer length L.
     * For a fixed L, we check whether there exists any window of length L such that:
     *
     *     2 * maxFreq <= L + k
     *
     * Rearranging:
     *
     *     maxFreq <= floor((L + k) / 2)
     *
     * So for each fixed length L, we only need to know whether every event code frequency
     * in some window stays at or below the allowed threshold.
     *
     * To check a fixed length efficiently, we slide a window of size L and maintain:
     * - a map from event code -> current frequency in the window
     * - a second map from frequency -> how many distinct event codes currently have that frequency
     * - the current maximum frequency in the window
     *
     * This allows each add/remove operation to be handled in expected O(1), and each feasibility
     * check for a fixed L runs in O(n).
     *
     * Binary search over L from 1 to n gives total expected O(n log n).
     *
     * @param events the array of event codes recorded per second
     * @param k the non-negative tolerance for dominance
     * @return the maximum length of a stable contiguous window
     * Time complexity: O(n log n) expected, where n = events.length
     * Space complexity: O(n) in the worst case due to frequency maps
     */
    public int longestStableWindow(int[] events, int k) {
        int n = events.length;

        int left = 1;
        int right = n;
        int answer = 0;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (existsStableWindowOfLength(events, k, mid)) {
                answer = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether there exists at least one stable window of exactly the given length.
     *
     * Stability condition for a fixed window length len:
     *
     *     2 * maxFreq <= len + k
     *
     * Equivalently:
     *
     *     maxFreq <= (len + k) / 2   (integer division is safe for threshold comparison)
     *
     * We slide a window of size len across the array and maintain the exact current maximum
     * frequency using two hash maps:
     *
     * 1) valueCount:
     *    event code -> its count in the current window
     *
     * 2) freqCount:
     *    frequency -> how many distinct event codes currently appear exactly that many times
     *
     * Why do we need freqCount?
     * Because when removing an element, the current maximum frequency might decrease.
     * If we only tracked a single max value, we would not know whether some other event code
     * still has that same maximum frequency. freqCount lets us update maxFreq exactly.
     *
     * @param events the array of event codes
     * @param k the tolerance
     * @param len the fixed window length to test
     * @return true if at least one stable window of size len exists; false otherwise
     * Time complexity: O(n) expected
     * Space complexity: O(n) in the worst case
     */
    public boolean existsStableWindowOfLength(int[] events, int k, int len) {
        if (len == 0) {
            return true;
        }

        int n = events.length;
        if (len > n) {
            return false;
        }

        Map<Integer, Integer> valueCount = new HashMap<>();
        Map<Integer, Integer> freqCount = new HashMap<>();

        int maxFreq = 0;

        for (int i = 0; i < n; i++) {
            maxFreq = addValue(events[i], valueCount, freqCount, maxFreq);

            if (i >= len) {
                maxFreq = removeValue(events[i - len], valueCount, freqCount, maxFreq);
            }

            if (i >= len - 1) {
                int allowedMaxFreq = (len + k) / 2;

                if (maxFreq <= allowedMaxFreq) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds one occurrence of a value into the current sliding window and updates all bookkeeping.
     *
     * Detailed update process:
     * - Read the old frequency of the value.
     * - If old frequency > 0, then one event code is no longer contributing to old frequency,
     *   so decrement freqCount[oldFreq].
     * - Increase the value's frequency by 1.
     * - Increment freqCount[newFreq].
     * - Update maxFreq if needed.
     *
     * @param value the event code being added to the window
     * @param valueCount map from event code to its current frequency
     * @param freqCount map from frequency to number of event codes having that frequency
     * @param maxFreq the current maximum frequency before the add
     * @return the updated maximum frequency after the add
     * Time complexity: O(1) expected
     * Space complexity: O(1) auxiliary, excluding map storage
     */
    public int addValue(int value,
                        Map<Integer, Integer> valueCount,
                        Map<Integer, Integer> freqCount,
                        int maxFreq) {

        int oldFreq = valueCount.getOrDefault(value, 0);

        if (oldFreq > 0) {
            decrementFrequencyBucket(freqCount, oldFreq);
        }

        int newFreq = oldFreq + 1;
        valueCount.put(value, newFreq);
        incrementFrequencyBucket(freqCount, newFreq);

        if (newFreq > maxFreq) {
            maxFreq = newFreq;
        }

        return maxFreq;
    }

    /**
     * Removes one occurrence of a value from the current sliding window and updates all bookkeeping.
     *
     * Detailed update process:
     * - Read the old frequency of the value. It must be at least 1.
     * - Decrement freqCount[oldFreq] because this event code will no longer have oldFreq.
     * - Decrease the value's frequency by 1.
     * - If the new frequency becomes 0, remove the value from valueCount entirely.
     * - Otherwise, increment freqCount[newFreq].
     * - If the removed frequency was equal to maxFreq and no event code remains at maxFreq,
     *   then repeatedly decrease maxFreq until we find a frequency bucket that still exists.
     *
     * @param value the event code being removed from the window
     * @param valueCount map from event code to its current frequency
     * @param freqCount map from frequency to number of event codes having that frequency
     * @param maxFreq the current maximum frequency before the removal
     * @return the updated maximum frequency after the removal
     * Time complexity: O(1) expected amortized
     * Space complexity: O(1) auxiliary, excluding map storage
     */
    public int removeValue(int value,
                           Map<Integer, Integer> valueCount,
                           Map<Integer, Integer> freqCount,
                           int maxFreq) {

        int oldFreq = valueCount.get(value);

        decrementFrequencyBucket(freqCount, oldFreq);

        int newFreq = oldFreq - 1;
        if (newFreq == 0) {
            valueCount.remove(value);
        } else {
            valueCount.put(value, newFreq);
            incrementFrequencyBucket(freqCount, newFreq);
        }

        while (maxFreq > 0 && !freqCount.containsKey(maxFreq)) {
            maxFreq--;
        }

        return maxFreq;
    }

    /**
     * Increments the number of event codes that currently have the given frequency.
     *
     * @param freqCount map from frequency to number of values with that frequency
     * @param freq the frequency bucket to increment
     * @return nothing
     * Time complexity: O(1) expected
     * Space complexity: O(1)
     */
    public void incrementFrequencyBucket(Map<Integer, Integer> freqCount, int freq) {
        freqCount.put(freq, freqCount.getOrDefault(freq, 0) + 1);
    }

    /**
     * Decrements the number of event codes that currently have the given frequency.
     * If the bucket count becomes zero, the bucket is removed to keep the map clean.
     *
     * @param freqCount map from frequency to number of values with that frequency
     * @param freq the frequency bucket to decrement
     * @return nothing
     * Time complexity: O(1) expected
     * Space complexity: O(1)
     */
    public void decrementFrequencyBucket(Map<Integer, Integer> freqCount, int freq) {
        int count = freqCount.get(freq);
        if (count == 1) {
            freqCount.remove(freq);
        } else {
            freqCount.put(freq, count - 1);
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 5
     * - Example 2: 5
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n log n) for each demonstration call
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] events1 = {4, 1, 4, 2, 4, 3, 2};
        int k1 = 1;
        int result1 = solution.longestStableWindow(events1, k1);
        System.out.println("Example 1 result: " + result1);

        int[] events2 = {7, 7, 7, 2, 3, 7, 4, 5};
        int k2 = 0;
        int result2 = solution.longestStableWindow(events2, k2);
        System.out.println("Example 2 result: " + result2);

        int[] extra1 = {1};
        int extraK1 = 0;
        System.out.println("Extra test 1 result: " + solution.longestStableWindow(extra1, extraK1));

        int[] extra2 = {5, 5, 5, 5};
        int extraK2 = 4;
        System.out.println("Extra test 2 result: " + solution.longestStableWindow(extra2, extraK2));
    }
}