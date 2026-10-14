/*
Title: Minimum Tap Strength for Garden Row Coverage
Difficulty: Medium
Topic: Binary Search

Problem Description:
A landscaping company manages a straight garden row represented by positions from 0 to L on a number line.
There are n sprinklers installed along the row. The i-th sprinkler is fixed at position positions[i] and can
water every point within distance s from its location if all sprinklers are configured with the same tap strength s.
In other words, with strength s, sprinkler i covers the interval [positions[i] - s, positions[i] + s].

Your task is to find the minimum integer tap strength s such that the entire garden row from 0 to L is covered
by at least one sprinkler. Coverage is continuous: every point in the interval [0, L] must lie inside the watering
range of some sprinkler. If it is impossible to cover the full row even with arbitrarily large strength because
there are no sprinklers, return -1.

This problem is designed to reward an efficient solution. A direct simulation over all possible strengths is too slow
when L is large. Instead, determine whether a candidate strength is feasible, and use binary search on the answer.

Constraints:
- 0 <= L <= 10^9
- 0 <= n == positions.length <= 2 * 10^5
- 0 <= positions[i] <= L
- positions may be unsorted
- Return the minimum integer strength

Example 1:
Input: L = 10, positions = [1, 4, 8]
Output: 2
Explanation: With strength 2, the covered intervals are [-1,3], [2,6], and [6,10], which together cover the entire row [0,10].
Strength 1 leaves gaps.

Example 2:
Input: L = 12, positions = [3, 9]
Output: 3
Explanation: With strength 3, the intervals are [0,6] and [6,12], so the full row is covered.
With strength 2, coverage is [1,5] and [7,11], leaving uncovered points.
*/

using System;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the sprinkler positions takes O(n log n)
    - Each feasibility check scans the array once, so it takes O(n)
    - Binary search over the answer range [0, L] takes O(log L) checks
    - Total: O(n log n + n log L)

    Space Complexity:
    - If we sort a copied array, that uses O(n) extra space
    - Aside from that, only a few variables are used
    - Total: O(n)
    */
    public int MinimumTapStrength(int L, int[] positions)
    {
        // If the garden length is 0, the interval [0, 0] is just a single point.
        // We still need that point to be covered by at least one sprinkler.
        // If there are no sprinklers at all, coverage is impossible.
        if (positions == null || positions.Length == 0)
        {
            return -1;
        }

        // We create a sorted copy of the positions.
        // Why sort?
        // Because coverage along a line is easiest to reason about from left to right.
        // Once sorted, we can check whether the sprinkler intervals connect together
        // without leaving any uncovered gap.
        int[] sorted = positions.ToArray();
        Array.Sort(sorted);

        // Binary search will be used on the answer itself.
        // We want the minimum integer strength s that works.
        //
        // Lower bound:
        // - 0 is the smallest possible strength.
        //
        // Upper bound:
        // - L is always sufficient when there is at least one sprinkler in [0, L].
        //   Why? Because every sprinkler position is between 0 and L.
        //   If a sprinkler has strength L, then:
        //   left end <= 0 and right end >= L
        //   so that single sprinkler covers the whole garden.
        int left = 0;
        int right = L;
        int answer = -1;

        // Standard binary search for the first feasible value.
        while (left <= right)
        {
            // Compute the middle carefully.
            int mid = left + (right - left) / 2;

            // Check whether this candidate strength is enough to cover [0, L].
            if (CanCover(L, sorted, mid))
            {
                // This strength works, so record it.
                answer = mid;

                // But we are looking for the minimum working strength,
                // so continue searching on the left half.
                right = mid - 1;
            }
            else
            {
                // This strength is too small, so we must try larger values.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool CanCover(int L, int[] sortedPositions, int strength)
    {
        // We will greedily track the farthest point on the garden row [0, L]
        // that is currently covered continuously from the left side.
        //
        // At the beginning, we have covered nothing yet.
        // Think of "coveredUntil" as:
        // "Every point from 0 up to coveredUntil is known to be covered."
        //
        // We use long to be extra safe when computing position +/- strength,
        // even though the given constraints fit in int.
        long coveredUntil = 0;

        // We process sprinklers from left to right.
        // Because the positions are sorted, their intervals also appear in left-to-right order.
        foreach (int pos in sortedPositions)
        {
            long intervalLeft = (long)pos - strength;
            long intervalRight = (long)pos + strength;

            // Step 1:
            // If this sprinkler's interval ends before 0, it contributes nothing useful
            // to covering the garden row [0, L]. We can skip it.
            if (intervalRight < 0)
            {
                continue;
            }

            // Step 2:
            // If this sprinkler starts after the point we have already covered,
            // then there is a gap.
            //
            // Why compare intervalLeft > coveredUntil?
            // Suppose coveredUntil = 5.
            // That means every point in [0, 5] is covered.
            // To continue coverage without a gap, the next interval must start at or before 5.
            //
            // If it starts at 5 exactly, that is okay:
            // point 5 is covered by both the previous coverage and this interval,
            // so the coverage remains continuous.
            //
            // If it starts at 5.1 (conceptually), then points between 5 and 5.1 are uncovered.
            // Since all values here are integers but intervals represent continuous ranges,
            // the strict ">" check is the correct one.
            if (intervalLeft > coveredUntil)
            {
                return false;
            }

            // Step 3:
            // This interval overlaps or touches the already-covered prefix,
            // so it can extend our continuous coverage farther to the right.
            //
            // We update coveredUntil to the farthest right endpoint seen so far
            // among intervals that connect to the current covered region.
            if (intervalRight > coveredUntil)
            {
                coveredUntil = intervalRight;
            }

            // Step 4:
            // If we have already covered up to at least L,
            // then the entire garden row [0, L] is covered.
            if (coveredUntil >= L)
            {
                return true;
            }
        }

        // After processing all sprinklers, if we still have not reached L,
        // then full coverage is impossible with this strength.
        return coveredUntil >= L;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int L1 = 10;
int[] positions1 = { 1, 4, 8 };
int result1 = solution.MinimumTapStrength(L1, positions1);
Console.WriteLine($"Example 1: L = {L1}, positions = [{string.Join(", ", positions1)}], minimum strength = {result1}");

// Example 2
int L2 = 12;
int[] positions2 = { 3, 9 };
int result2 = solution.MinimumTapStrength(L2, positions2);
Console.WriteLine($"Example 2: L = {L2}, positions = [{string.Join(", ", positions2)}], minimum strength = {result2}");

// Additional demo: unsorted input
int L3 = 10;
int[] positions3 = { 8, 1, 4 };
int result3 = solution.MinimumTapStrength(L3, positions3);
Console.WriteLine($"Example 3: L = {L3}, positions = [{string.Join(", ", positions3)}], minimum strength = {result3}");

// Additional demo: single sprinkler
int L4 = 7;
int[] positions4 = { 3 };
int result4 = solution.MinimumTapStrength(L4, positions4);
Console.WriteLine($"Example 4: L = {L4}, positions = [{string.Join(", ", positions4)}], minimum strength = {result4}");

// Additional demo: no sprinklers
int L5 = 5;
int[] positions5 = Array.Empty<int>();
int result5 = solution.MinimumTapStrength(L5, positions5);
Console.WriteLine($"Example 5: L = {L5}, positions = [{string.Join(", ", positions5)}], minimum strength = {result5}");