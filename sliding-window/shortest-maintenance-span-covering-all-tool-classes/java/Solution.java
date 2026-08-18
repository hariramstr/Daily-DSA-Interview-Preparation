import java.util.*;

/*
 * Title: Shortest Maintenance Span Covering All Tool Classes
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A factory records the sequence of tools used during a long maintenance session.
 * Each tool use is represented by an integer tool class ID in the array tools,
 * where tools[i] is the class of the i-th tool used.
 *
 * You are also given an integer array required, where required[j] is a tool class
 * that must appear at least once inside a valid contiguous span. The required array
 * may contain duplicates, meaning the span must include that many occurrences of the
 * corresponding class. For example, required = [2, 2, 5] means a valid span must
 * contain at least two class-2 tools and at least one class-5 tool.
 *
 * Return the length of the shortest contiguous subarray of tools that satisfies all
 * requirements. If no such span exists, return -1.
 *
 * This problem is designed for large inputs, so solutions that check every subarray
 * will time out. A correct solution should efficiently maintain counts while expanding
 * and shrinking a window.
 *
 * Constraints:
 * - 1 <= tools.length <= 200000
 * - 1 <= required.length <= 200000
 * - 1 <= tools[i], required[i] <= 10^9
 * - The answer fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: tools = [7,2,3,2,5,2,1,5], required = [2,5,2]
 * Output: 3
 * Explanation: The shortest valid span is [2,5,2], which has two occurrences of
 * tool class 2 and one occurrence of tool class 5.
 *
 * Example 2:
 * Input: tools = [4,1,4,3,6,1,3], required = [1,3,3]
 * Output: -1
 * Explanation: Any valid span would need two occurrences of tool class 3, but tools
 * contains only one 3, so no contiguous span can satisfy the requirement.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray of {@code tools} that contains
     * all tool classes required by {@code required}, including duplicate frequency demands.
     *
     * The core idea is a classic sliding window:
     * 1. Build a frequency map of what is required.
     * 2. Expand the right side of the window until all required occurrences are covered.
     * 3. Once valid, shrink the left side as much as possible while keeping the window valid.
     * 4. Track the minimum valid window length seen.
     *
     * Important detail:
     * If required = [2, 2, 5], then the window must contain:
     * - tool 2 at least twice
     * - tool 5 at least once
     *
     * We measure "how many required occurrences are currently satisfied" using a counter:
     * - totalRequired = required.length
     * - formed = number of required occurrences currently matched in the window
     *
     * Every time we add a tool that is still needed, formed increases by 1.
     * Every time we remove a tool that causes the window to fall below a needed count,
     * formed decreases by 1.
     *
     * @param tools the full sequence of tool class IDs used during maintenance
     * @param required the multiset of required tool class IDs; duplicates mean multiple occurrences are needed
     * @return the length of the shortest valid contiguous span, or -1 if no such span exists
     *
     * Time complexity: O(n + m), where n = tools.length and m = required.length.
     * Each array is processed linearly, and each window pointer moves at most n times.
     *
     * Space complexity: O(k), where k is the number of distinct tool classes appearing
     * in the required array (and tracked in the window map).
     */
    public int shortestMaintenanceSpan(int[] tools, int[] required) {
        // Defensive handling. The problem constraints guarantee lengths >= 1,
        // but this makes the method safer and more beginner-friendly.
        if (tools == null || required == null || tools.length == 0 || required.length == 0) {
            return -1;
        }

        // If the required multiset is larger than the entire tools array,
        // it is impossible to satisfy all occurrences.
        if (required.length > tools.length) {
            return -1;
        }

        // needCount:
        // Maps each required tool class -> how many times it must appear.
        //
        // Example:
        // required = [2, 2, 5]
        // needCount = {2=2, 5=1}
        Map<Integer, Integer> needCount = new HashMap<>();
        for (int toolClass : required) {
            needCount.put(toolClass, needCount.getOrDefault(toolClass, 0) + 1);
        }

        // windowCount:
        // Tracks how many times each relevant tool class appears in the current window.
        //
        // We only care about classes that are in needCount.
        Map<Integer, Integer> windowCount = new HashMap<>();

        // totalRequired is the total number of required occurrences, counting duplicates.
        // Example: required = [2, 2, 5] => totalRequired = 3
        int totalRequired = required.length;

        // formed counts how many required occurrences are currently satisfied in the window.
        //
        // Example:
        // needCount = {2=2, 5=1}
        // If window currently has one 2 and one 5, then formed = 2
        // because we have satisfied:
        // - one of the two needed 2's
        // - the one needed 5
        //
        // The window is valid exactly when formed == totalRequired.
        int formed = 0;

        // Standard sliding window pointers:
        // left = start of current window
        // right = end of current window (advanced in the loop)
        int left = 0;

        // Store the best answer found so far.
        // Start with a very large value so any real valid window will be smaller.
        int minLength = Integer.MAX_VALUE;

        // Expand the window by moving right from 0 to tools.length - 1.
        for (int right = 0; right < tools.length; right++) {
            int currentTool = tools[right];

            // Only update counts if this tool class is actually relevant to the requirement.
            // Irrelevant values can still be inside the window, but they do not help satisfy it.
            if (needCount.containsKey(currentTool)) {
                int newCount = windowCount.getOrDefault(currentTool, 0) + 1;
                windowCount.put(currentTool, newCount);

                // If after adding this tool, the count of currentTool in the window
                // does not exceed what is needed, then this newly added occurrence
                // contributes to satisfying one required occurrence.
                //
                // Example:
                // needCount[2] = 2
                // windowCount[2] goes from 0 -> 1 => formed++
                // windowCount[2] goes from 1 -> 2 => formed++
                // windowCount[2] goes from 2 -> 3 => formed does NOT increase
                if (newCount <= needCount.get(currentTool)) {
                    formed++;
                }
            }

            // Now that we expanded the window to include tools[right],
            // try to shrink from the left while the window remains valid.
            //
            // A valid window means we have satisfied every required occurrence.
            while (formed == totalRequired) {
                // Current window is [left, right], inclusive.
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                int leftTool = tools[left];

                // We are about to remove tools[left] from the window by moving left forward.
                // If that tool is relevant, update the window counts carefully.
                if (needCount.containsKey(leftTool)) {
                    int existingCount = windowCount.get(leftTool);

                    // If the current count is <= needed count, then removing one occurrence
                    // will make us lose one satisfied required occurrence.
                    //
                    // Example:
                    // needCount[5] = 1
                    // windowCount[5] = 1
                    // Removing this 5 means the window no longer satisfies that requirement,
                    // so formed must decrease.
                    //
                    // Another example:
                    // needCount[2] = 2
                    // windowCount[2] = 3
                    // Removing one 2 changes count 3 -> 2, which still satisfies the need,
                    // so formed does NOT decrease.
                    if (existingCount <= needCount.get(leftTool)) {
                        formed--;
                    }

                    // Actually remove the left tool from the window count.
                    if (existingCount == 1) {
                        windowCount.remove(leftTool);
                    } else {
                        windowCount.put(leftTool, existingCount - 1);
                    }
                }

                // Move the left boundary rightward to try making the valid window smaller.
                left++;
            }
        }

        // If minLength was never updated, no valid window exists.
        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * Convenience wrapper that runs the algorithm and prints the input and result.
     *
     * @param tools the sequence of tool class IDs
     * @param required the required multiset of tool class IDs
     * @return the computed shortest valid span length
     *
     * Time complexity: O(n + m), delegated to {@link #shortestMaintenanceSpan(int[], int[])}.
     * Space complexity: O(k), delegated to {@link #shortestMaintenanceSpan(int[], int[])}.
     */
    public int demonstrate(int[] tools, int[] required) {
        int result = shortestMaintenanceSpan(tools, required);
        System.out.println("tools    = " + Arrays.toString(tools));
        System.out.println("required = " + Arrays.toString(required));
        System.out.println("result   = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the solution on the sample test cases from the problem statement,
     * and also includes a few extra sanity checks.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total input size of demonstrated test cases).
     * Space complexity: O(k) per test case for the internal maps.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        // tools = [7,2,3,2,5,2,1,5], required = [2,5,2]
        // Expected output: 3
        // One shortest valid span is [2,5,2].
        solution.demonstrate(
                new int[]{7, 2, 3, 2, 5, 2, 1, 5},
                new int[]{2, 5, 2}
        );

        // Sample 2
        // tools = [4,1,4,3,6,1,3], required = [1,3,3]
        // Expected output: -1
        // There is only one occurrence of 3 in some positions? Actually the array has two 3s? Let's verify:
        // [4,1,4,3,6,1,3] contains 3 at indices 3 and 6, so there are two 3s total.
        // Combined with one 1, the whole array can satisfy [1,3,3], and the shortest valid span is [3,6,1,3] length 4.
        // However, the problem statement says expected output is -1 and claims only one 3 exists.
        // That statement is inconsistent with the provided array.
        //
        // Because correctness is mandatory, the algorithm follows the actual input values.
        // For the given array, the correct result is 4.
        solution.demonstrate(
                new int[]{4, 1, 4, 3, 6, 1, 3},
                new int[]{1, 3, 3}
        );

        // Extra sanity check: impossible because required frequency exceeds available frequency.
        solution.demonstrate(
                new int[]{1, 2, 3},
                new int[]{2, 2}
        );

        // Extra sanity check: exact full-array match.
        solution.demonstrate(
                new int[]{5, 5, 7},
                new int[]{5, 7, 5}
        );

        // Extra sanity check: shortest window in the middle.
        solution.demonstrate(
                new int[]{9, 1, 2, 2, 5, 8},
                new int[]{2, 5, 2}
        );
    }
}