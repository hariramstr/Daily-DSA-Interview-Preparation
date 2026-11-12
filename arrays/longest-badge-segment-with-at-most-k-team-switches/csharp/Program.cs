/*
Title: Longest Badge Segment With At Most K Team Switches

Problem Description:
You are given an array teams where teams[i] is the team ID shown on the i-th badge scan in chronological order.
A security analyst wants to find the longest contiguous time segment that is operationally stable, where the
number of times the team ID changes between adjacent scans is at most k.

Formally, for a subarray teams[l...r], count how many indices i satisfy l < i <= r and teams[i] != teams[i - 1].
This count is called the number of team switches inside the segment. Your task is to return the maximum possible
length of a contiguous segment whose number of team switches is at most k.

This is not the same as counting distinct team IDs. For example, the segment [4, 4, 7, 7, 4] has 2 team switches,
even though it contains only 2 distinct values.

Return the length of the longest valid segment.

Constraints:
- 1 <= teams.length <= 200000
- 0 <= k < teams.length
- 1 <= teams[i] <= 1000000000

Example 1:
Input: teams = [5,5,2,2,2,9,9,5], k = 2
Output: 7

Example 2:
Input: teams = [1,3,1,3,1,3], k = 3
Output: 4
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Explanation of complexity:
    - We use a sliding window with two pointers: left and right.
    - The right pointer moves from left to right exactly once.
    - The left pointer also only moves forward, never backward.
    - Because each index is processed at most a constant number of times, the total work is linear.
    - We do not use any extra arrays, hash maps, or other data structures that grow with input size.
    */
    public int LongestStableSegment(int[] teams, int k)
    {
        // If the array has at least one element, the answer is at least 1,
        // because any single element segment has zero switches.
        int n = teams.Length;

        // "left" marks the beginning of the current sliding window.
        int left = 0;

        // "switches" stores how many adjacent changes currently exist
        // inside the window teams[left..right].
        //
        // Important detail:
        // A switch is counted at position i when teams[i] != teams[i - 1].
        // So for a window [left..right], valid switch positions are i where left < i <= right.
        int switches = 0;

        // "best" stores the maximum valid window length found so far.
        int best = 1;

        // Expand the window one element at a time by moving "right".
        for (int right = 0; right < n; right++)
        {
            // Step 1:
            // When we extend the window to include teams[right], we need to check
            // whether this newly added element creates a new team switch with the
            // previous element teams[right - 1].
            //
            // This only matters when right > 0, because index 0 has no previous element.
            //
            // Why this is necessary:
            // The number of switches in the window depends on adjacent comparisons.
            // The only new adjacent pair introduced by extending the window to the right
            // is (right - 1, right). So we update the switch count using only that pair.
            if (right > 0 && teams[right] != teams[right - 1])
            {
                switches++;
            }

            // Step 2:
            // If the window now has too many switches, it is invalid.
            // We must shrink it from the left until it becomes valid again.
            //
            // Why shrinking works:
            // This is a classic sliding window pattern for constraints that can be
            // maintained incrementally. Once the window violates the rule
            // "switches <= k", moving left forward can only remove elements and
            // eventually reduce the number of switches enough.
            while (switches > k)
            {
                // Before moving "left" forward, we need to determine whether
                // removing teams[left] from the window also removes a switch.
                //
                // The only adjacent pair affected when left moves to left + 1 is
                // the pair (left, left + 1), because that pair stops being inside
                // the window after the move.
                //
                // If teams[left] != teams[left + 1], then that pair contributed
                // exactly one switch to the current window, so we must subtract it.
                //
                // We can safely access left + 1 here because if switches > k,
                // then the window must have at least two elements. A single-element
                // window always has zero switches and could never violate the rule.
                if (teams[left] != teams[left + 1])
                {
                    switches--;
                }

                // Actually shrink the window by moving its left boundary rightward.
                left++;
            }

            // Step 3:
            // At this point, the window teams[left..right] is valid:
            // it contains at most k switches.
            //
            // So we compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning all possible right endpoints, "best" is the maximum
        // length of any valid contiguous segment.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] teams1 = { 5, 5, 2, 2, 2, 9, 9, 5 };
int k1 = 2;
int result1 = solution.LongestStableSegment(teams1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 7

// Example 2
int[] teams2 = { 1, 3, 1, 3, 1, 3 };
int k2 = 3;
int result2 = solution.LongestStableSegment(teams2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick checks
int[] teams3 = { 4, 4, 7, 7, 4 };
int k3 = 2;
int result3 = solution.LongestStableSegment(teams3, k3);
Console.WriteLine("Additional Check 1 Result: " + result3); // Expected: 5

int[] teams4 = { 8 };
int k4 = 0;
int result4 = solution.LongestStableSegment(teams4, k4);
Console.WriteLine("Additional Check 2 Result: " + result4); // Expected: 1

int[] teams5 = { 2, 2, 2, 2 };
int k5 = 0;
int result5 = solution.LongestStableSegment(teams5, k5);
Console.WriteLine("Additional Check 3 Result: " + result5); // Expected: 4