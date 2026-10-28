import java.util.*;

/*
 * Title: Maximum Consecutive Seats After Ignoring One Outlier Reservation
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A theater stores its reserved seat numbers for a show in an integer array seats.
 * The numbers are not sorted, may contain duplicates due to repeated sync events,
 * and may include gaps because many seats were never reserved.
 *
 * You are allowed to ignore at most one distinct seat number as a data outlier.
 * Ignoring a seat number means removing all occurrences of that exact value from
 * consideration.
 *
 * After optionally ignoring one value, determine the maximum length of a consecutive
 * block of reserved seat numbers that can remain.
 *
 * A consecutive block is a set of integers like [12, 13, 14, 15], where every number
 * from the minimum to the maximum is present at least once. Duplicates do not increase
 * the block length.
 *
 * Important observation:
 * Removing one distinct value cannot create a new missing seat number, and it also
 * cannot bridge a gap larger than 1 between existing distinct values. Therefore,
 * deleting one value can only keep the same best consecutive run length or possibly
 * shorten some run. So the answer is simply the length of the longest consecutive run
 * among the distinct seat values.
 *
 * Example:
 * seats = [10, 11, 12, 14, 14, 15, 16]
 * Distinct sorted values = [10, 11, 12, 14, 15, 16]
 * Runs are [10, 11, 12] and [14, 15, 16], so the answer is 3.
 */

public class Solution {

    /**
     * Computes the maximum length of a consecutive block of reserved seat numbers
     * after optionally ignoring all occurrences of at most one distinct value.
     *
     * Key idea:
     * Since ignoring a value only removes data and never adds missing numbers,
     * it cannot merge two runs separated by a gap. Therefore, the best possible
     * answer is exactly the longest consecutive run among the distinct values.
     *
     * @param seats the input array of reserved seat numbers; may be unsorted and contain duplicates
     * @return the maximum length of a consecutive block among distinct seat values
     * Time complexity: O(n) average, where n is seats.length
     * Space complexity: O(n)
     */
    public int maxConsecutiveSeatsAfterIgnoringOneOutlier(int[] seats) {
        // Step 1:
        // Put every seat number into a HashSet.
        //
        // Why?
        // - We only care whether a seat number exists at least once.
        // - Duplicates do not help extend a consecutive block.
        // - A HashSet gives average O(1) lookup time.
        Set<Integer> distinct = new HashSet<>();
        for (int seat : seats) {
            distinct.add(seat);
        }

        // Step 2:
        // We now find the longest consecutive sequence in the set.
        //
        // Standard technique:
        // For each value x, only start counting if x - 1 is NOT present.
        // That means x is the beginning of a run.
        //
        // Example:
        // If the set contains 4, 5, 6, 8, 9:
        // - 4 starts a run because 3 is absent
        // - 5 does not start a run because 4 exists
        // - 6 does not start a run because 5 exists
        // - 8 starts a run because 7 is absent
        // - 9 does not start a run because 8 exists
        int best = 0;

        for (int value : distinct) {
            // Only begin expanding from true run starts.
            if (!distinct.contains(value - 1)) {
                int current = value;
                int length = 1;

                // Step 3:
                // Expand forward while the next consecutive value exists.
                while (distinct.contains(current + 1)) {
                    current++;
                    length++;
                }

                // Step 4:
                // Update the best answer seen so far.
                best = Math.max(best, length);
            }
        }

        return best;
    }

    /**
     * Helper method that builds and returns the sorted distinct seat values.
     * This is useful for demonstration and tracing examples in main.
     *
     * @param seats the input array of reserved seat numbers
     * @return a sorted list containing each distinct seat value exactly once
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public List<Integer> getSortedDistinctSeats(int[] seats) {
        Set<Integer> set = new HashSet<>();
        for (int seat : seats) {
            set.add(seat);
        }

        List<Integer> result = new ArrayList<>(set);
        Collections.sort(result);
        return result;
    }

    /**
     * Helper method that returns all consecutive runs from the distinct sorted values.
     * This is only for educational output in main so that the examples can be traced clearly.
     *
     * @param seats the input array of reserved seat numbers
     * @return a list of runs, where each run is represented as a list of consecutive integers
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public List<List<Integer>> getConsecutiveRuns(int[] seats) {
        List<Integer> sortedDistinct = getSortedDistinctSeats(seats);
        List<List<Integer>> runs = new ArrayList<>();

        if (sortedDistinct.isEmpty()) {
            return runs;
        }

        List<Integer> currentRun = new ArrayList<>();
        currentRun.add(sortedDistinct.get(0));

        for (int i = 1; i < sortedDistinct.size(); i++) {
            int previous = sortedDistinct.get(i - 1);
            int current = sortedDistinct.get(i);

            if (current == previous + 1) {
                currentRun.add(current);
            } else {
                runs.add(currentRun);
                currentRun = new ArrayList<>();
                currentRun.add(current);
            }
        }

        runs.add(currentRun);
        return runs;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding method internals
     * Space complexity: O(1) for the fixed demo inputs, excluding method internals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        // The prompt text initially mentions output 4, but then explicitly corrects it to 3.
        // We must follow the corrected interpretation.
        int[] seats1 = {10, 11, 12, 14, 14, 15, 16};
        System.out.println("Example 1 input: " + Arrays.toString(seats1));
        System.out.println("Distinct sorted seats: " + solution.getSortedDistinctSeats(seats1));
        System.out.println("Consecutive runs: " + solution.getConsecutiveRuns(seats1));
        System.out.println("Maximum consecutive block length: "
                + solution.maxConsecutiveSeatsAfterIgnoringOneOutlier(seats1));
        System.out.println("Expected: 3");
        System.out.println();

        // Example 2 from the prompt.
        int[] seats2 = {3, 4, 5, 7, 8, 9, 20};
        System.out.println("Example 2 input: " + Arrays.toString(seats2));
        System.out.println("Distinct sorted seats: " + solution.getSortedDistinctSeats(seats2));
        System.out.println("Consecutive runs: " + solution.getConsecutiveRuns(seats2));
        System.out.println("Maximum consecutive block length: "
                + solution.maxConsecutiveSeatsAfterIgnoringOneOutlier(seats2));
        System.out.println("Expected: 3");
        System.out.println();

        // Additional demonstration from the description.
        int[] seats3 = {4, 5, 6, 8, 9};
        System.out.println("Additional example input: " + Arrays.toString(seats3));
        System.out.println("Distinct sorted seats: " + solution.getSortedDistinctSeats(seats3));
        System.out.println("Consecutive runs: " + solution.getConsecutiveRuns(seats3));
        System.out.println("Maximum consecutive block length: "
                + solution.maxConsecutiveSeatsAfterIgnoringOneOutlier(seats3));
        System.out.println("Expected: 3");
        System.out.println();

        // A case with many duplicates.
        int[] seats4 = {7, 7, 7, 8, 8, 9, 10, 10};
        System.out.println("Duplicate-heavy input: " + Arrays.toString(seats4));
        System.out.println("Distinct sorted seats: " + solution.getSortedDistinctSeats(seats4));
        System.out.println("Consecutive runs: " + solution.getConsecutiveRuns(seats4));
        System.out.println("Maximum consecutive block length: "
                + solution.maxConsecutiveSeatsAfterIgnoringOneOutlier(seats4));
        System.out.println("Expected: 4");
        System.out.println();

        // A single-element case.
        int[] seats5 = {42};
        System.out.println("Single-element input: " + Arrays.toString(seats5));
        System.out.println("Distinct sorted seats: " + solution.getSortedDistinctSeats(seats5));
        System.out.println("Consecutive runs: " + solution.getConsecutiveRuns(seats5));
        System.out.println("Maximum consecutive block length: "
                + solution.maxConsecutiveSeatsAfterIgnoringOneOutlier(seats5));
        System.out.println("Expected: 1");
    }
}