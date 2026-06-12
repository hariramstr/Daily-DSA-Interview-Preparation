/*
Title: Shortest Badge Sequence Covering All Teams

Problem Description:
A company records the order of employee badge scans during a large conference.
Each scan is represented by an integer team ID in the array scans, where scans[i]
is the team of the i-th person who entered a room. You are also given an integer m
representing the total number of distinct teams, labeled from 1 to m.

Find the length of the shortest contiguous subarray of scans that contains at least
one badge scan from every team 1 through m. If no such subarray exists, return -1.

This problem models finding the smallest time window in which all required groups
were represented. The array may contain repeated team IDs, and some teams may appear
many times while others appear rarely. Your solution should be efficient enough for
large inputs.

Return only the minimum length, not the subarray itself.

Constraints:
- 1 <= scans.length <= 200000
- 1 <= m <= 100000
- 1 <= scans[i] <= m
- The input array may be unsorted
- If one or more team IDs from 1 to m never appear in scans, the answer is -1

Examples:
1) scans = [2, 1, 3, 2, 4, 1, 3], m = 4
   Output: 4
   Explanation: The shortest valid subarray is [1, 3, 2, 4].

2) scans = [1, 2, 2, 1, 3], m = 4
   Output: -1
   Explanation: Team 4 never appears, so no valid subarray exists.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the sliding window once by the right pointer.
    - Each element is removed from the sliding window at most once by the left pointer.
    - Therefore, the total work is linear in the size of the array.

    Space Complexity: O(m)
    - We store the frequency of each team ID from 1 to m in an array.
    */
    public int ShortestBadgeSequenceCoveringAllTeams(int[] scans, int m)
    {
        // Defensive check:
        // If the input array is null or empty, there is no subarray at all,
        // so it is impossible to cover every team.
        if (scans == null || scans.Length == 0)
        {
            return -1;
        }

        // This array will store how many times each team appears inside the current window.
        // We use size m + 1 so that team IDs 1..m can be used directly as indexes.
        // Index 0 is unused.
        int[] frequency = new int[m + 1];

        // "coveredTeams" means:
        // how many distinct required teams currently appear at least once in the window.
        //
        // Example:
        // If m = 4 and the current window contains teams {1, 2, 4},
        // then coveredTeams = 3.
        int coveredTeams = 0;

        // This will store the best (smallest) valid window length found so far.
        // We start with int.MaxValue to mean "no valid answer found yet".
        int minLength = int.MaxValue;

        // The left boundary of our sliding window.
        int left = 0;

        // We now expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < scans.Length; right++)
        {
            // Step 1: Include scans[right] into the current window.
            //
            // This is the "expand" step of the sliding window.
            // We are growing the window to the right to try to collect all teams.
            int rightTeam = scans[right];
            frequency[rightTeam]++;

            // If the count became 1, that means this team was previously absent
            // from the window and is now newly covered.
            //
            // Why this matters:
            // We only increase coveredTeams when a team appears for the first time
            // in the current window.
            if (frequency[rightTeam] == 1)
            {
                coveredTeams++;
            }

            // Step 2: If the current window covers all teams 1..m,
            // try to shrink it from the left while keeping it valid.
            //
            // This is the key optimization:
            // Once we have a valid window, we do not stop immediately.
            // We keep removing unnecessary elements from the left to make it as short as possible.
            while (coveredTeams == m)
            {
                // The current window is [left..right], inclusive.
                // Its length is right - left + 1.
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentLength < minLength)
                {
                    minLength = currentLength;
                }

                // We will now try to remove scans[left] from the window.
                // This is the "shrink" step.
                int leftTeam = scans[left];
                frequency[leftTeam]--;

                // If the count becomes 0, then removing this element caused
                // that team to disappear completely from the window.
                //
                // That means the window is no longer valid, because it no longer
                // contains all teams.
                if (frequency[leftTeam] == 0)
                {
                    coveredTeams--;
                }

                // Move the left boundary one step to the right.
                left++;
            }
        }

        // If minLength was never updated, then no valid window was found.
        // That means at least one team never appeared in the array.
        if (minLength == int.MaxValue)
        {
            return -1;
        }

        return minLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] scans1 = { 2, 1, 3, 2, 4, 1, 3 };
int m1 = 4;
int result1 = solution.ShortestBadgeSequenceCoveringAllTeams(scans1, m1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] scans2 = { 1, 2, 2, 1, 3 };
int m2 = 4;
int result2 = solution.ShortestBadgeSequenceCoveringAllTeams(scans2, m2);
Console.WriteLine(result2); // Expected: -1

// Additional demo
int[] scans3 = { 1, 2, 3, 4 };
int m3 = 4;
int result3 = solution.ShortestBadgeSequenceCoveringAllTeams(scans3, m3);
Console.WriteLine(result3); // Expected: 4

int[] scans4 = { 1, 1, 1, 2, 3, 4, 2, 1 };
int m4 = 4;
int result4 = solution.ShortestBadgeSequenceCoveringAllTeams(scans4, m4);
Console.WriteLine(result4); // Expected: 4