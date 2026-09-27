import java.util.*;

/*
 * Title: Longest Transcript Window With Speaker Dominance Cap
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a transcript of a meeting as an array speakers, where speakers[i]
 * is the speaker ID of the person who spoke the i-th utterance. A contiguous window
 * of the transcript is called balanced if no single speaker accounts for more than
 * cap percent of the utterances inside that window.
 *
 * Formally, for a window speakers[l..r] of length len = r - l + 1, let freq[x] be
 * the number of times speaker x appears in the window. The window is valid if for
 * every speaker x:
 *
 *     100 * freq[x] <= cap * len
 *
 * Your task is to return the length of the longest balanced contiguous window.
 *
 * Constraints:
 * - 1 <= speakers.length <= 2 * 10^5
 * - 1 <= speakers[i] <= 10^9
 * - 1 <= cap <= 100
 *
 * Notes:
 * - Speaker IDs are not necessarily small or consecutive.
 * - A window of length 1 is valid only if cap >= 100.
 * - The answer is the maximum length among all contiguous valid windows.
 *
 * Examples:
 *
 * Example 1:
 * Input: speakers = [4, 1, 4, 2, 1, 2, 3], cap = 50
 * Output: 7
 *
 * Example 2:
 * Input: speakers = [8, 8, 8, 2, 3, 8, 4, 5], cap = 40
 * Output: 5
 */

/**
 * Complete runnable solution for finding the longest balanced contiguous window.
 *
 * Core idea:
 * A window is valid if its maximum speaker frequency is not too large:
 *
 *     maxFreq <= floor(cap * len / 100)
 *
 * So the problem becomes:
 * Find the longest subarray whose maximum frequency satisfies the above inequality.
 *
 * This is not a standard sliding-window problem because when the left side moves,
 * the maximum frequency can decrease, and maintaining that exactly in O(1) is hard.
 *
 * Efficient strategy used here:
 * 1. Binary search on the answer length L.
 * 2. For a fixed length L, check whether there exists any window of length L
 *    whose maximum frequency is <= allowed, where:
 *
 *        allowed = floor(cap * L / 100)
 *
 * 3. To check all windows of a fixed length efficiently, maintain:
 *    - frequency of each speaker in the current window
 *    - how many speakers currently have frequency f
 *
 *    Then we can maintain the exact current maximum frequency in amortized O(1).
 *
 * Total complexity:
 * O(n log n), which is efficient for n up to 2 * 10^5.
 */
public class Solution {

    /**
     * Returns the length of the longest balanced contiguous window.
     *
     * We binary search on the window length. For each candidate length, we test
     * whether at least one valid window of that exact length exists.
     *
     * Why binary search works:
     * If a window of length L is valid, then every smaller positive length is also
     * feasible in the existential sense:
     * - Take any valid window of length L.
     * - Any subwindow of length s <= L cannot have a speaker count larger than the
     *   original window for that speaker, and the threshold scales with length.
     *
     * More directly and more safely for this problem:
     * For a fixed cap, the minimum possible maximum frequency in any window of length L
     * is at least ceil(L / distinctCount), but the actual existential feasibility
     * over lengths is monotone enough for binary search because if some length L is
     * achievable, then by taking a contiguous subwindow of that valid window, we can
     * always obtain smaller lengths that remain valid under the same cap constraint.
     *
     * @param speakers the transcript speaker IDs
     * @param cap the percentage cap; no speaker may exceed this percentage in a valid window
     * @return the maximum valid window length
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int longestBalancedWindow(int[] speakers, int cap) {
        int n = speakers.length;

        int low = 0;
        int high = n;

        while (low < high) {
            // We bias upward so the loop converges correctly when low + 1 == high.
            int mid = low + (high - low + 1) / 2;

            if (existsValidWindowOfLength(speakers, cap, mid)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return low;
    }

    /**
     * Checks whether there exists at least one balanced window of exactly the given length.
     *
     * For a fixed length len:
     * A window is valid iff:
     *
     *     100 * maxFreq <= cap * len
     *
     * Let:
     *
     *     allowed = floor(cap * len / 100)
     *
     * Then validity is equivalent to:
     *
     *     maxFreq <= allowed
     *
     * We slide a window of size len across the array and maintain:
     * - freqBySpeaker: current count of each speaker in the window
     * - countOfFrequency[f]: how many speakers currently appear exactly f times
     * - currentMaxFreq: the exact maximum frequency in the current window
     *
     * Updating currentMaxFreq:
     * - When adding a speaker and its count increases to newFreq, currentMaxFreq may increase.
     * - When removing a speaker and the old maximum bucket becomes empty, we decrease
     *   currentMaxFreq until we find a non-empty bucket.
     *
     * This gives an exact maximum frequency for every window.
     *
     * @param speakers the transcript speaker IDs
     * @param cap the percentage cap
     * @param len the exact window length to test
     * @return true if at least one valid window of this length exists, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public boolean existsValidWindowOfLength(int[] speakers, int cap, int len) {
        if (len == 0) {
            return true;
        }

        int n = speakers.length;
        if (len > n) {
            return false;
        }

        // Maximum allowed frequency in any valid window of this exact length.
        int allowed = (cap * len) / 100;

        // If allowed == 0 and len > 0, no non-empty window can be valid because every
        // window has at least one speaker appearing at least once.
        if (allowed == 0) {
            return false;
        }

        Map<Integer, Integer> freqBySpeaker = new HashMap<>();

        // countOfFrequency[f] = number of distinct speakers that currently appear exactly f times.
        // Frequency never exceeds len, so an array of size len + 1 is enough.
        int[] countOfFrequency = new int[len + 1];

        int currentMaxFreq = 0;

        // Build the first window [0 .. len-1].
        for (int i = 0; i < len; i++) {
            int speaker = speakers[i];

            int oldFreq = freqBySpeaker.getOrDefault(speaker, 0);
            int newFreq = oldFreq + 1;

            // Speaker leaves the old frequency bucket.
            if (oldFreq > 0) {
                countOfFrequency[oldFreq]--;
            }

            // Speaker enters the new frequency bucket.
            countOfFrequency[newFreq]++;
            freqBySpeaker.put(speaker, newFreq);

            // Update exact maximum frequency.
            if (newFreq > currentMaxFreq) {
                currentMaxFreq = newFreq;
            }
        }

        // Check the first window.
        if (currentMaxFreq <= allowed) {
            return true;
        }

        // Slide the window one position at a time.
        for (int right = len; right < n; right++) {
            int left = right - len;

            // ------------------------------------------------------------
            // Step 1: remove the outgoing speaker at index 'left'
            // ------------------------------------------------------------
            int outgoingSpeaker = speakers[left];
            int oldFreqOut = freqBySpeaker.get(outgoingSpeaker);
            int newFreqOut = oldFreqOut - 1;

            // Remove one speaker from the old frequency bucket.
            countOfFrequency[oldFreqOut]--;

            if (newFreqOut == 0) {
                // Speaker is no longer present in the window.
                freqBySpeaker.remove(outgoingSpeaker);
            } else {
                // Speaker now belongs to the lower frequency bucket.
                countOfFrequency[newFreqOut]++;
                freqBySpeaker.put(outgoingSpeaker, newFreqOut);
            }

            // If the previous maximum frequency bucket became empty,
            // shrink currentMaxFreq downward until it points to a non-empty bucket.
            while (currentMaxFreq > 0 && countOfFrequency[currentMaxFreq] == 0) {
                currentMaxFreq--;
            }

            // ------------------------------------------------------------
            // Step 2: add the incoming speaker at index 'right'
            // ------------------------------------------------------------
            int incomingSpeaker = speakers[right];
            int oldFreqIn = freqBySpeaker.getOrDefault(incomingSpeaker, 0);
            int newFreqIn = oldFreqIn + 1;

            if (oldFreqIn > 0) {
                countOfFrequency[oldFreqIn]--;
            }

            countOfFrequency[newFreqIn]++;
            freqBySpeaker.put(incomingSpeaker, newFreqIn);

            if (newFreqIn > currentMaxFreq) {
                currentMaxFreq = newFreqIn;
            }

            // ------------------------------------------------------------
            // Step 3: test the current window
            // ------------------------------------------------------------
            if (currentMaxFreq <= allowed) {
                return true;
            }
        }

        return false;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param arr the array to convert to string
     * @return a readable string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 7
     * - Example 2: 5
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] speakers1 = {4, 1, 4, 2, 1, 2, 3};
        int cap1 = 50;
        int result1 = solution.longestBalancedWindow(speakers1, cap1);
        System.out.println("Example 1");
        System.out.println("speakers = " + solution.arrayToString(speakers1));
        System.out.println("cap = " + cap1);
        System.out.println("Longest balanced window length = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        int[] speakers2 = {8, 8, 8, 2, 3, 8, 4, 5};
        int cap2 = 40;
        int result2 = solution.longestBalancedWindow(speakers2, cap2);
        System.out.println("Example 2");
        System.out.println("speakers = " + solution.arrayToString(speakers2));
        System.out.println("cap = " + cap2);
        System.out.println("Longest balanced window length = " + result2);
        System.out.println("Expected = 5");
        System.out.println();

        // Additional quick sanity checks.
        int[] speakers3 = {1};
        int cap3 = 100;
        System.out.println("Sanity Check 1");
        System.out.println("speakers = " + solution.arrayToString(speakers3));
        System.out.println("cap = " + cap3);
        System.out.println("Longest balanced window length = " + solution.longestBalancedWindow(speakers3, cap3));
        System.out.println("Expected = 1");
        System.out.println();

        int[] speakers4 = {1};
        int cap4 = 99;
        System.out.println("Sanity Check 2");
        System.out.println("speakers = " + solution.arrayToString(speakers4));
        System.out.println("cap = " + cap4);
        System.out.println("Longest balanced window length = " + solution.longestBalancedWindow(speakers4, cap4));
        System.out.println("Expected = 0");
    }
}