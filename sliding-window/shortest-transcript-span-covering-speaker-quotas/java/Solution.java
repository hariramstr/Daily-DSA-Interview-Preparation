import java.util.*;

/*
 * Title: Shortest Transcript Span Covering Speaker Quotas
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a meeting transcript represented by an array speakers, where speakers[i]
 * is the speaker ID of the person who spoke the i-th utterance. You are also given a list
 * of quota requirements requirements, where each element is a pair [speakerId, minCount]
 * meaning that a valid excerpt must contain at least minCount utterances from that speaker.
 *
 * Your task is to find the shortest contiguous span of the transcript that satisfies all
 * speaker quotas. If multiple spans have the same minimum length, return the one with the
 * smallest starting index. If no such span exists, return [-1, -1].
 *
 * This problem models real interview scenarios such as extracting the smallest meeting
 * segment that contains enough participation from required stakeholders. The challenge is
 * that speaker IDs may be large, repeated many times, and only some speakers are constrained
 * by quotas. An efficient solution should avoid checking every subarray.
 *
 * Return the answer as a pair [start, end] using 0-based indices.
 *
 * Constraints:
 * - 1 <= speakers.length <= 2 * 10^5
 * - 1 <= requirements.length <= 10^5
 * - 1 <= speakerId <= 10^9
 * - 1 <= minCount <= speakers.length
 * - All speakerId values in requirements are distinct
 *
 * Example 1:
 * Input:
 * speakers = [4, 2, 7, 2, 4, 2, 9, 7, 4]
 * requirements = [[2, 2], [4, 2], [7, 1]]
 * Output:
 * [0, 4]
 *
 * Example 2:
 * Input:
 * speakers = [5, 1, 5, 3, 1, 5, 2, 3]
 * requirements = [[1, 2], [3, 2], [2, 1]]
 * Output:
 * [3, 7]
 */

public class Solution {

    /**
     * Finds the shortest contiguous transcript span that satisfies all speaker quotas.
     *
     * The algorithm uses the classic sliding window / two-pointer technique:
     * 1. Expand the right pointer to include more utterances.
     * 2. Track counts only for speakers that actually appear in the requirements.
     * 3. Once all quotas are satisfied, try to shrink from the left to make the window minimal.
     * 4. Record the best answer by shortest length, and if tied, smallest start index.
     *
     * This is efficient because each index is processed at most a constant number of times:
     * once when the right pointer enters it, and once when the left pointer leaves it.
     *
     * @param speakers the transcript as an array of speaker IDs, where speakers[i] is the speaker
     *                 who spoke the i-th utterance
     * @param requirements an array of pairs [speakerId, minCount] describing the minimum number
     *                     of utterances required from each constrained speaker
     * @return an int array of length 2 containing [start, end] of the shortest valid span,
     *         or [-1, -1] if no valid span exists
     * Time complexity: O(n + m), where n = speakers.length and m = requirements.length
     * Space complexity: O(m), because we store maps only for required speakers
     */
    public int[] shortestTranscriptSpan(int[] speakers, int[][] requirements) {
        // Edge case handling:
        // If there are no requirements, the smallest valid span is conceptually empty.
        // However, the problem constraints imply requirements.length >= 1.
        // Still, we handle it defensively.
        if (requirements == null || requirements.length == 0) {
            return new int[] {0, -1};
        }

        // requiredCount:
        // Maps each required speaker ID -> minimum number of times that speaker must appear.
        Map<Integer, Integer> requiredCount = new HashMap<>();

        // windowCount:
        // Maps each required speaker ID -> current count inside the sliding window.
        Map<Integer, Integer> windowCount = new HashMap<>();

        // Build the requirement map.
        for (int[] req : requirements) {
            requiredCount.put(req[0], req[1]);
        }

        // totalRequiredSpeakers:
        // Number of distinct speakers whose quotas must be satisfied.
        int totalRequiredSpeakers = requiredCount.size();

        // formed:
        // Number of distinct required speakers for which the current window already meets quota.
        int formed = 0;

        // Best answer tracking.
        int bestStart = -1;
        int bestEnd = -1;
        int bestLength = Integer.MAX_VALUE;

        // Left boundary of the sliding window.
        int left = 0;

        // Expand the window by moving 'right' from left to right across the transcript.
        for (int right = 0; right < speakers.length; right++) {
            int currentSpeaker = speakers[right];

            // We only care about speakers that are actually constrained by requirements.
            if (requiredCount.containsKey(currentSpeaker)) {
                int newCount = windowCount.getOrDefault(currentSpeaker, 0) + 1;
                windowCount.put(currentSpeaker, newCount);

                // If this increment makes the count exactly reach the required quota,
                // then one more required speaker is now satisfied.
                if (newCount == requiredCount.get(currentSpeaker)) {
                    formed++;
                }
            }

            // If all required speakers are satisfied, the current window [left, right] is valid.
            // Now we try to shrink it from the left as much as possible while keeping it valid.
            while (formed == totalRequiredSpeakers && left <= right) {
                int currentLength = right - left + 1;

                // Update best answer if:
                // 1. This window is shorter, or
                // 2. Same length but smaller starting index.
                if (currentLength < bestLength || (currentLength == bestLength && left < bestStart)) {
                    bestLength = currentLength;
                    bestStart = left;
                    bestEnd = right;
                }

                int leftSpeaker = speakers[left];

                // We are about to remove speakers[left] from the window by moving left forward.
                // If that speaker is required, update the window count carefully.
                if (requiredCount.containsKey(leftSpeaker)) {
                    int countBeforeRemoval = windowCount.get(leftSpeaker);

                    // If the count is currently exactly at the required threshold,
                    // removing one occurrence will make this speaker no longer satisfied.
                    if (countBeforeRemoval == requiredCount.get(leftSpeaker)) {
                        formed--;
                    }

                    windowCount.put(leftSpeaker, countBeforeRemoval - 1);
                }

                // Actually shrink the window.
                left++;
            }
        }

        // If bestStart was never updated, no valid window exists.
        if (bestStart == -1) {
            return new int[] {-1, -1};
        }

        return new int[] {bestStart, bestEnd};
    }

    /**
     * Convenience wrapper that prints the input and output for a single test case.
     *
     * @param speakers the transcript speaker array
     * @param requirements the quota requirements
     * @return the computed answer [start, end]
     * Time complexity: O(n + m), delegated to shortestTranscriptSpan
     * Space complexity: O(m), delegated to shortestTranscriptSpan
     */
    public int[] runAndPrintExample(int[] speakers, int[][] requirements) {
        int[] result = shortestTranscriptSpan(speakers, requirements);
        System.out.println("speakers     = " + Arrays.toString(speakers));
        System.out.println("requirements = " + deepToString(requirements));
        System.out.println("result       = " + Arrays.toString(result));
        System.out.println();
        return result;
    }

    /**
     * Converts a 2D int array into a readable string form.
     *
     * @param array the 2D integer array to convert
     * @return a string representation such as [[1, 2], [3, 4]]
     * Time complexity: O(k), where k is the total number of integers printed
     * Space complexity: O(k) for the produced string
     */
    public static String deepToString(int[][] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(array[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Verified manually:
     * Example 1:
     * speakers = [4, 2, 7, 2, 4, 2, 9, 7, 4]
     * requirements = [[2, 2], [4, 2], [7, 1]]
     * Correct answer = [0, 4]
     *
     * Example 2:
     * speakers = [5, 1, 5, 3, 1, 5, 2, 3]
     * requirements = [[1, 2], [3, 2], [2, 1]]
     * Correct answer = [3, 7]
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total input size across demonstrations)
     * Space complexity: O(m) per demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] speakers1 = {4, 2, 7, 2, 4, 2, 9, 7, 4};
        int[][] requirements1 = {
            {2, 2},
            {4, 2},
            {7, 1}
        };

        int[] result1 = solution.runAndPrintExample(speakers1, requirements1);
        System.out.println("Expected Example 1: [0, 4]");
        System.out.println("Matches Expected?  " + Arrays.equals(result1, new int[] {0, 4}));
        System.out.println();

        int[] speakers2 = {5, 1, 5, 3, 1, 5, 2, 3};
        int[][] requirements2 = {
            {1, 2},
            {3, 2},
            {2, 1}
        };

        int[] result2 = solution.runAndPrintExample(speakers2, requirements2);
        System.out.println("Expected Example 2: [3, 7]");
        System.out.println("Matches Expected?  " + Arrays.equals(result2, new int[] {3, 7}));
        System.out.println();

        // Additional quick sanity check: impossible case.
        int[] speakers3 = {1, 2, 3};
        int[][] requirements3 = {
            {1, 1},
            {4, 1}
        };

        int[] result3 = solution.runAndPrintExample(speakers3, requirements3);
        System.out.println("Expected Impossible Case: [-1, -1]");
        System.out.println("Matches Expected?         " + Arrays.equals(result3, new int[] {-1, -1}));
    }
}