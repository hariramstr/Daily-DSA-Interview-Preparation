import java.util.*;

/*
Problem Title: Minimum Pair Merges to Clear Duplicate Bit Flags

Problem Description:
You are given an integer array flags where each value represents a device configuration mask.
A bit set to 1 means that capability is enabled for that device.

You may repeatedly perform the following operation:
- choose any two different indices i and j such that flags[i] and flags[j] share at least one common set bit,
- remove both values,
- and insert their bitwise OR, that is, flags[i] | flags[j].

Your goal is to make the final array bit-disjoint, meaning that for every pair of remaining values a and b,
(a & b) == 0.

Return the minimum number of merge operations required.

A merge can combine overlapping capabilities from multiple devices into a single mask, which may reduce future conflicts.
Two masks that do not overlap cannot be merged directly. The order of merges matters, because merging one pair may create
a larger mask that still overlaps with others.

This problem can be viewed as grouping masks into the smallest number of final components such that every original mask
in the same component is connected through shared bits, possibly indirectly.

Constraints:
- 1 <= flags.length <= 100000
- 0 <= flags[i] <= 10^9
- The answer fits in a 32-bit signed integer.

Examples:
1) flags = [3, 5, 8]
   Output: 1

2) flags = [10, 3, 12, 1]
   Output: 2
*/

public class Solution {

    /**
     * Computes the minimum number of merge operations needed so that all remaining masks
     * are pairwise bit-disjoint.
     *
     * Core idea:
     * We process numbers from left to right and maintain a partition of the already-seen
     * numbers into "current groups". Each group represents one final merged mask that could
     * remain at the end. For each group, we only need its OR-mask.
     *
     * When a new value x arrives:
     * - If x shares no bit with any existing group mask, it can stay as a new separate group.
     * - If x overlaps with one or more existing groups, then all those overlapping groups and x
     *   must ultimately become one single group. Why? Because x cannot coexist with any group it
     *   overlaps, and after merging x with one such group, the resulting OR-mask still contains
     *   all bits from x, so it will continue to overlap with every other group that x overlapped.
     *   Therefore all overlapping groups are forced to collapse together with x.
     *
     * If x overlaps with k existing groups, then the number of groups decreases by (k - 1):
     * we remove k groups and add back 1 merged group.
     *
     * Initially, each element could be its own group, so we can think in terms of counting how
     * many final groups remain. The minimum number of merges is:
     *
     *     totalElements - numberOfFinalGroups
     *
     * because every merge reduces the number of array elements by exactly 1.
     *
     * Important note about zero:
     * - A zero mask has no set bits, so it never overlaps with anything.
     * - Therefore every zero always remains its own separate final group.
     *
     * This algorithm is correct because the family of current group masks is always pairwise
     * disjoint after each step, and each new number either starts a new disjoint group or
     * necessarily merges all groups it touches into one.
     *
     * @param flags the array of device capability masks
     * @return the minimum number of merge operations required
     * Time complexity: O(n * g), where g is the current number of groups. Since masks use at most
     * 30 bits (values <= 1e9), the number of pairwise-disjoint non-zero groups is at most 30,
     * so this is effectively O(30n) = O(n).
     * Space complexity: O(g), which is O(30) for non-zero groups plus zeros counted separately,
     * so effectively O(1) auxiliary space.
     */
    public int minimumPairMerges(int[] flags) {
        // This list stores the OR-mask of each current non-zero group.
        // Invariant:
        // - Every two masks in this list are pairwise disjoint.
        // - Together, they represent the best possible partition of processed non-zero values
        //   into final groups.
        List<Integer> groups = new ArrayList<>();

        // Count of zero values.
        // Each zero is always its own final group because:
        // - it cannot merge with anything (0 shares no set bit with any number),
        // - and it is already disjoint from everything.
        int zeroCount = 0;

        // Process each flag one by one.
        for (int value : flags) {
            // Special handling for zero.
            if (value == 0) {
                zeroCount++;
                continue;
            }

            // We will find all existing groups whose masks overlap with this value.
            // Since current groups are pairwise disjoint, these overlaps are easy to reason about:
            // if value touches multiple groups, then those groups must all be merged together with value.
            int mergedMask = value;

            // We build a new list of groups after inserting this value.
            List<Integer> nextGroups = new ArrayList<>(groups.size() + 1);

            // Step through every existing group.
            for (int mask : groups) {
                // If there is at least one common set bit, these two cannot remain separate.
                if ((mask & mergedMask) != 0) {
                    // Therefore they are forced into the same final group.
                    // We accumulate them by OR-ing into mergedMask.
                    mergedMask |= mask;
                } else {
                    // No overlap, so this group can remain separate.
                    nextGroups.add(mask);
                }
            }

            // After absorbing every overlapping group, add the resulting merged group.
            nextGroups.add(mergedMask);

            // Replace the old group list with the updated one.
            groups = nextGroups;
        }

        // Final number of groups:
        // - one group for each zero
        // - one group for each non-zero disjoint mask in 'groups'
        int finalGroupCount = zeroCount + groups.size();

        // Starting from n elements, each merge reduces the count by exactly 1.
        // To end with finalGroupCount elements, we need:
        // n - finalGroupCount merges.
        return flags.length - finalGroupCount;
    }

    /**
     * Alias method matching a common interview-style naming convention.
     *
     * @param flags the array of device capability masks
     * @return the minimum number of merge operations required
     * Time complexity: O(n)
     * Space complexity: O(1) auxiliary, ignoring the tiny bounded group list
     */
    public int minOperations(int[] flags) {
        return minimumPairMerges(flags);
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param arr the array to convert to string
     * @return a readable string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few extra sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size across demo cases)
     * Space complexity: O(1) auxiliary, aside from tiny bounded group storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sample1 = {3, 5, 8};
        int[] sample2 = {10, 3, 12, 1};

        System.out.println("Sample 1: " + solution.arrayToString(sample1));
        System.out.println("Minimum merges = " + solution.minimumPairMerges(sample1));
        System.out.println("Expected = 1");
        System.out.println();

        System.out.println("Sample 2: " + solution.arrayToString(sample2));
        System.out.println("Minimum merges = " + solution.minimumPairMerges(sample2));
        System.out.println("Expected = 2");
        System.out.println();

        // Extra checks
        int[] extra1 = {1, 2, 4, 8}; // already pairwise disjoint
        int[] extra2 = {3, 6, 12};   // chain of overlaps, all collapse into one => 2 merges
        int[] extra3 = {0, 0, 1, 3}; // zeros stay separate, 1 and 3 merge => 1 merge
        int[] extra4 = {7};          // single element => 0 merges

        System.out.println("Extra 1: " + solution.arrayToString(extra1));
        System.out.println("Minimum merges = " + solution.minimumPairMerges(extra1));
        System.out.println("Expected = 0");
        System.out.println();

        System.out.println("Extra 2: " + solution.arrayToString(extra2));
        System.out.println("Minimum merges = " + solution.minimumPairMerges(extra2));
        System.out.println("Expected = 2");
        System.out.println();

        System.out.println("Extra 3: " + solution.arrayToString(extra3));
        System.out.println("Minimum merges = " + solution.minimumPairMerges(extra3));
        System.out.println("Expected = 1");
        System.out.println();

        System.out.println("Extra 4: " + solution.arrayToString(extra4));
        System.out.println("Minimum merges = " + solution.minimumPairMerges(extra4));
        System.out.println("Expected = 0");
    }
}