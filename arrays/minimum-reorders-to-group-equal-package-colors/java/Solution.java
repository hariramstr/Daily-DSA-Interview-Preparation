import java.util.*;

/*
 * Title: Minimum Reorders to Group Equal Package Colors
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A warehouse conveyor outputs packages as an array colors, where colors[i] is the color code
 * of the ith package. The same color may appear many times in different positions.
 * The sorting machine wants all packages of the same color to appear in one contiguous block,
 * but the relative order of those color blocks does not matter.
 *
 * In one operation, you may remove a single package from its current position and insert it
 * at any position in the array. Return the minimum number of such operations needed so that,
 * in the final array, every distinct color appears in exactly one contiguous segment.
 *
 * You are not asked to output the final arrangement, only the minimum number of moves.
 *
 * Key Insight:
 * A package can stay in place if it belongs to some subsequence that already matches a valid
 * final arrangement consisting of color blocks in some order. Therefore:
 *
 *   minimum moves = n - maximum length of a subsequence that can be written as
 *                   [all occurrences of color c1][all occurrences of color c2]...[all occurrences of color ck]
 *                   for some ordering of distinct colors.
 *
 * For a chosen order of colors, if a color is kept, then all of its occurrences must be kept,
 * because in the final arrangement each color appears in exactly one block containing all items
 * of that color. Thus we want the largest set of colors whose full occurrence ranges can be
 * arranged in a non-overlapping order.
 *
 * Let:
 *   first[color] = first index where color appears
 *   last[color]  = last index where color appears
 *   count[color] = total occurrences of color
 *
 * If we keep colors in some order, then for consecutive kept colors a -> b we must have:
 *   last[a] < first[b]
 * because every kept occurrence of a must appear before every kept occurrence of b in the
 * original array subsequence.
 *
 * So the problem becomes:
 *   Choose a chain of colors with strictly increasing non-overlapping intervals
 *   [first[color], last[color]], maximizing the sum of counts.
 *
 * This is weighted interval scheduling on intervals defined by each distinct color.
 * Since intervals are based on array positions, we can solve it efficiently by sorting colors
 * by last position and using DP with binary search.
 *
 * Example:
 * colors = [3, 1, 3, 2, 1, 2]
 * color 3 -> first=0, last=2, count=2
 * color 1 -> first=1, last=4, count=2
 * color 2 -> first=3, last=5, count=2
 *
 * Compatible chains:
 *   [3] weight 2
 *   [1] weight 2
 *   [2] weight 2
 *   [3,2] because last[3]=2 < first[2]=3, total weight 4
 *
 * Best keep = 4, so minimum moves = 6 - 4 = 2.
 *
 * Note:
 * The sample explanation in the prompt says 3 for this example, but that is inconsistent with
 * the operation model. Keeping subsequence [3,3,2,2] and moving the two 1s yields [3,3,1,1,2,2]
 * in exactly 2 moves. Therefore the correct answer for that example is 2.
 */
public class Solution {

    /**
     * Small helper structure representing one distinct color as an interval in the array.
     * The interval spans from its first occurrence to its last occurrence, and its weight
     * is the number of times that color appears.
     */
    private static class ColorInterval {
        int color;
        int first;
        int last;
        int count;

        ColorInterval(int color, int first, int last, int count) {
            this.color = color;
            this.first = first;
            this.last = last;
            this.count = count;
        }
    }

    /**
     * Computes the minimum number of remove-and-insert operations required so that every
     * distinct color appears in exactly one contiguous block.
     *
     * Core idea:
     * 1. For each distinct color, compute:
     *      - first occurrence index
     *      - last occurrence index
     *      - total frequency
     * 2. Treat each color as a weighted interval [first, last] with weight = frequency.
     * 3. We want the maximum total weight of a set of non-overlapping intervals, where
     *    interval A can come before interval B only if last[A] < first[B].
     * 4. That maximum weight equals the largest number of packages we can keep in place.
     * 5. Answer = n - maximumKeep.
     *
     * Why this is correct:
     * - If a color is kept, all of its occurrences can remain and form its block in the final
     *   arrangement only if all kept colors can be ordered so their occurrences do not interleave.
     * - That is exactly the non-overlapping interval condition.
     * - Any package not in this optimal kept set must be moved.
     *
     * @param colors the array of package color codes
     * @return the minimum number of operations needed
     * Time complexity: O(n + m log m), where n is array length and m is number of distinct colors
     * Space complexity: O(m)
     */
    public int minimumReorders(int[] colors) {
        int n = colors.length;

        // Maps each color to:
        // - its first occurrence index
        // - its last occurrence index
        // - its total count
        //
        // We use HashMap because color values can be sparse.
        Map<Integer, Integer> firstMap = new HashMap<>();
        Map<Integer, Integer> lastMap = new HashMap<>();
        Map<Integer, Integer> countMap = new HashMap<>();

        // Step 1: Scan the array once and collect interval information for each color.
        for (int i = 0; i < n; i++) {
            int color = colors[i];

            // Record first occurrence only once.
            firstMap.putIfAbsent(color, i);

            // Always update last occurrence to the current position.
            lastMap.put(color, i);

            // Count frequency.
            countMap.put(color, countMap.getOrDefault(color, 0) + 1);
        }

        // Step 2: Convert the maps into a list of intervals, one per distinct color.
        List<ColorInterval> intervals = new ArrayList<>(countMap.size());
        for (int color : countMap.keySet()) {
            intervals.add(new ColorInterval(
                    color,
                    firstMap.get(color),
                    lastMap.get(color),
                    countMap.get(color)
            ));
        }

        // Step 3: Sort intervals by ending position (last occurrence).
        // This is the standard ordering used in weighted interval scheduling.
        intervals.sort(Comparator.comparingInt(a -> a.last));

        int m = intervals.size();

        // ends[i] = last position of the i-th interval in sorted order.
        // This array lets us binary-search the rightmost interval that ends
        // before the current interval starts.
        int[] ends = new int[m];
        for (int i = 0; i < m; i++) {
            ends[i] = intervals.get(i).last;
        }

        // dp[i] = maximum total kept packages considering the first i intervals
        //         in the sorted list.
        //
        // We use 1-based DP indexing for clarity:
        //   dp[0] = 0
        //   dp[i] corresponds to intervals[0..i-1]
        long[] dp = new long[m + 1];

        // Step 4: Weighted interval scheduling DP.
        for (int i = 1; i <= m; i++) {
            ColorInterval current = intervals.get(i - 1);

            // Find the number of intervals we can consider before taking "current".
            // We need the rightmost interval with end < current.first.
            int prevCount = findLastNonOverlappingCount(ends, current.first);

            // Option 1: skip current interval
            long skip = dp[i - 1];

            // Option 2: take current interval
            long take = dp[prevCount] + current.count;

            // Best of the two choices
            dp[i] = Math.max(skip, take);
        }

        long maximumKeep = dp[m];
        return (int) (n - maximumKeep);
    }

    /**
     * Binary-search helper.
     *
     * Given a sorted array of interval end positions, returns how many intervals from the
     * beginning have end < startIndex.
     *
     * In other words, if it returns k, then:
     * - intervals[0..k-1] all end before startIndex
     * - intervals[k] is the first one that does not satisfy that condition (if k < m)
     *
     * This value is directly usable as a 1-based DP prefix length.
     *
     * @param ends sorted array of interval end positions
     * @param startIndex the start position of the current interval
     * @return number of intervals whose end is strictly less than startIndex
     * Time complexity: O(log m)
     * Space complexity: O(1)
     */
    public int findLastNonOverlappingCount(int[] ends, int startIndex) {
        int left = 0;
        int right = ends.length;

        // We search for the first position where ends[pos] >= startIndex.
        // Therefore all positions before it satisfy ends[pos] < startIndex.
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (ends[mid] < startIndex) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        // "left" is exactly the count of valid previous intervals.
        return left;
    }

    /**
     * Convenience wrapper for demonstration output.
     *
     * @param colors input array
     * @return formatted result string
     * Time complexity: O(n + m log m)
     * Space complexity: O(m)
     */
    public String solveAndFormat(int[] colors) {
        return "colors = " + Arrays.toString(colors) + " -> minimum moves = " + minimumReorders(colors);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * Important note about Example 1 from the prompt:
     * The prompt states output 3 for [3, 1, 3, 2, 1, 2], but under the allowed operation
     * "remove one package and insert it anywhere", the true minimum is 2.
     *
     * One valid sequence:
     *   [3, 1, 3, 2, 1, 2]
     *   move the 1 at index 1 after the other 1  -> [3, 3, 2, 1, 1, 2]
     *   move the 2 at index 2 to the end         -> [3, 3, 1, 1, 2, 2]
     *
     * Thus only 2 moves are needed.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size log distinctColors) across demonstrated cases
     * Space complexity: O(distinctColors) per case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] example1 = {3, 1, 3, 2, 1, 2};
        int[] example2 = {4, 4, 2, 2, 3, 3};

        // Additional sanity checks
        int[] test1 = {2, 1, 2, 1};       // keep either [2,2] or [1,1], answer 2
        int[] test2 = {1, 1, 1};          // already one block, answer 0
        int[] test3 = {1, 2, 3, 1, 2, 3}; // best keep any one chain like [1] or [2] or [3], answer 4
        int[] test4 = {1, 2, 2, 1};       // keep [2,2], answer 2
        int[] test5 = {5, 5, 1, 2, 2, 1}; // keep [5,5,2,2] via colors 5 then 2, answer 2

        System.out.println(solution.solveAndFormat(example1));
        System.out.println("Expected under correct operation model: 2");
        System.out.println();

        System.out.println(solution.solveAndFormat(example2));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println(solution.solveAndFormat(test1));
        System.out.println(solution.solveAndFormat(test2));
        System.out.println(solution.solveAndFormat(test3));
        System.out.println(solution.solveAndFormat(test4));
        System.out.println(solution.solveAndFormat(test5));
    }
}