import java.util.*;

/*
 * Title: Shortest Booth Segment Covering All Payment Methods
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * At a large outdoor festival, booths are arranged in a single row. Each booth supports exactly one
 * payment method, represented by a string in the array methods, where methods[i] is the payment
 * method accepted by the i-th booth. Festival organizers want to place a help desk so that visitors
 * can walk to a contiguous segment of booths that includes every distinct payment method used anywhere
 * in the festival at least once.
 *
 * Your task is to return the length of the shortest contiguous segment of booths that contains all
 * distinct payment methods present in the entire array. If the array is empty, return 0.
 *
 * This is a practical optimization problem: the answer is not asking for all such segments, only the
 * minimum possible length. The payment methods are case-sensitive, so "Card" and "card" are different
 * methods.
 *
 * Constraints:
 * - 0 <= methods.length <= 100000
 * - 1 <= methods[i].length <= 20
 * - methods[i] consists of English letters, digits, underscores, or hyphens
 * - The expected solution should run in O(n) time using a two-pointers / sliding window approach
 *
 * Example 1:
 * Input: methods = ["cash", "card", "voucher", "cash", "qr", "card", "qr"]
 * Output: 4
 * Explanation: The distinct payment methods are {"cash", "card", "voucher", "qr"}.
 * One shortest valid segment is ["voucher", "cash", "qr", "card"], which has length 4.
 *
 * Example 2:
 * Input: methods = ["applepay", "applepay", "cash"]
 * Output: 2
 * Explanation: The distinct payment methods are {"applepay", "cash"}.
 * The shortest segment covering both is ["applepay", "cash"].
 */

public class Solution {

    /**
     * Returns the length of the shortest contiguous segment that contains every distinct
     * payment method appearing anywhere in the input array.
     *
     * The algorithm uses the classic sliding window / two-pointers technique:
     * 1. First, determine how many distinct payment methods exist in the whole array.
     * 2. Expand the right pointer to include booths into the current window.
     * 3. Track frequencies of methods inside the current window.
     * 4. Once the window contains all distinct methods, try shrinking from the left
     *    while still keeping the window valid.
     * 5. Record the minimum valid window length found.
     *
     * @param methods the array of payment methods for each booth; methods[i] is the method
     *                accepted by the i-th booth
     * @return the minimum length of a contiguous segment containing all distinct payment methods;
     *         returns 0 if the array is empty
     *
     * Time complexity: O(n), because each pointer moves from left to right at most once.
     * Space complexity: O(k), where k is the number of distinct payment methods.
     */
    public int shortestSegmentCoveringAllMethods(String[] methods) {
        // If the input array is null or empty, there is no segment to consider.
        // According to the problem statement, the correct answer is 0.
        if (methods == null || methods.length == 0) {
            return 0;
        }

        // Step 1:
        // Find all distinct payment methods that appear anywhere in the array.
        // We need every valid window to contain each of these at least once.
        Set<String> distinctMethods = new HashSet<>();
        for (String method : methods) {
            distinctMethods.add(method);
        }

        // This is the total number of unique payment methods that must be covered.
        int requiredDistinctCount = distinctMethods.size();

        // Step 2:
        // Prepare a frequency map for the current sliding window.
        // windowCount.get(x) will tell us how many times method x appears
        // inside the current window [left, right].
        Map<String, Integer> windowCount = new HashMap<>();

        // This variable tracks how many distinct payment methods are currently
        // represented in the window with frequency >= 1.
        int formedDistinctCount = 0;

        // Left boundary of the sliding window.
        int left = 0;

        // Initialize answer with the largest possible window length.
        int minLength = Integer.MAX_VALUE;

        // Step 3:
        // Expand the window by moving the right pointer from left to right.
        for (int right = 0; right < methods.length; right++) {
            String currentMethod = methods[right];

            // Add the current method to the window frequency map.
            int newCount = windowCount.getOrDefault(currentMethod, 0) + 1;
            windowCount.put(currentMethod, newCount);

            // If this method's count became 1, it means this distinct method
            // has just been newly included in the current window.
            if (newCount == 1) {
                formedDistinctCount++;
            }

            // Step 4:
            // If the current window already contains all distinct payment methods,
            // then it is a valid candidate. We should try to shrink it from the left
            // to make it as short as possible while keeping it valid.
            while (formedDistinctCount == requiredDistinctCount) {
                // Current window is [left, right], so its length is:
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                // We now attempt to remove methods[left] from the window
                // and move left forward by one step.
                String leftMethod = methods[left];
                int updatedCount = windowCount.get(leftMethod) - 1;

                if (updatedCount == 0) {
                    // If the count drops to zero, this method is no longer present
                    // in the window, so the window will stop being valid after we move left.
                    windowCount.remove(leftMethod);
                    formedDistinctCount--;
                } else {
                    // Otherwise, just update the reduced frequency.
                    windowCount.put(leftMethod, updatedCount);
                }

                // Actually shrink the window from the left.
                left++;
            }
        }

        // Because the entire array itself always contains all distinct methods,
        // minLength will definitely be updated for any non-empty array.
        return minLength;
    }

    /**
     * A helper method that returns one shortest contiguous segment containing all distinct
     * payment methods. This is not required to solve the problem, but it is useful for
     * demonstration in main and for beginner understanding.
     *
     * If multiple shortest segments exist, this method returns the first one found
     * during the left-to-right scan.
     *
     * @param methods the array of payment methods for each booth
     * @return a String array representing one shortest valid segment; returns an empty
     *         array if the input is null or empty
     *
     * Time complexity: O(n), because it uses the same sliding window idea.
     * Space complexity: O(k), where k is the number of distinct payment methods.
     */
    public String[] shortestSegmentExample(String[] methods) {
        if (methods == null || methods.length == 0) {
            return new String[0];
        }

        Set<String> distinctMethods = new HashSet<>();
        for (String method : methods) {
            distinctMethods.add(method);
        }

        int requiredDistinctCount = distinctMethods.size();
        Map<String, Integer> windowCount = new HashMap<>();
        int formedDistinctCount = 0;
        int left = 0;

        int bestStart = 0;
        int bestLength = Integer.MAX_VALUE;

        for (int right = 0; right < methods.length; right++) {
            String currentMethod = methods[right];
            int newCount = windowCount.getOrDefault(currentMethod, 0) + 1;
            windowCount.put(currentMethod, newCount);

            if (newCount == 1) {
                formedDistinctCount++;
            }

            while (formedDistinctCount == requiredDistinctCount) {
                int currentLength = right - left + 1;
                if (currentLength < bestLength) {
                    bestLength = currentLength;
                    bestStart = left;
                }

                String leftMethod = methods[left];
                int updatedCount = windowCount.get(leftMethod) - 1;
                if (updatedCount == 0) {
                    windowCount.remove(leftMethod);
                    formedDistinctCount--;
                } else {
                    windowCount.put(leftMethod, updatedCount);
                }
                left++;
            }
        }

        String[] result = new String[bestLength];
        for (int i = 0; i < bestLength; i++) {
            result[i] = methods[bestStart + i];
        }
        return result;
    }

    /**
     * Converts a String array into a readable array-like format for printing.
     *
     * @param array the input String array
     * @return a human-readable representation such as ["a", "b", "c"]
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to the StringBuilder output.
     */
    public String arrayToString(String[] array) {
        if (array == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(array[i]).append("\"");
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call to the algorithm.
     * Space complexity: O(k) per demonstration call, where k is the number of distinct methods.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1
        String[] methods1 = {"cash", "card", "voucher", "cash", "qr", "card", "qr"};
        int answer1 = solution.shortestSegmentCoveringAllMethods(methods1);
        String[] segment1 = solution.shortestSegmentExample(methods1);

        System.out.println("Example 1 Input: " + solution.arrayToString(methods1));
        System.out.println("Example 1 Output: " + answer1);
        System.out.println("Example 1 Shortest Segment: " + solution.arrayToString(segment1));
        System.out.println("Expected Output: 4");
        System.out.println();

        // Sample Input 2
        String[] methods2 = {"applepay", "applepay", "cash"};
        int answer2 = solution.shortestSegmentCoveringAllMethods(methods2);
        String[] segment2 = solution.shortestSegmentExample(methods2);

        System.out.println("Example 2 Input: " + solution.arrayToString(methods2));
        System.out.println("Example 2 Output: " + answer2);
        System.out.println("Example 2 Shortest Segment: " + solution.arrayToString(segment2));
        System.out.println("Expected Output: 2");
        System.out.println();

        // Edge Case: Empty array
        String[] methods3 = {};
        int answer3 = solution.shortestSegmentCoveringAllMethods(methods3);

        System.out.println("Edge Case Input (empty): " + solution.arrayToString(methods3));
        System.out.println("Edge Case Output: " + answer3);
        System.out.println("Expected Output: 0");
        System.out.println();

        // Edge Case: All methods are the same
        String[] methods4 = {"cash", "cash", "cash"};
        int answer4 = solution.shortestSegmentCoveringAllMethods(methods4);
        String[] segment4 = solution.shortestSegmentExample(methods4);

        System.out.println("All Same Input: " + solution.arrayToString(methods4));
        System.out.println("All Same Output: " + answer4);
        System.out.println("All Same Shortest Segment: " + solution.arrayToString(segment4));
        System.out.println("Expected Output: 1");
    }
}