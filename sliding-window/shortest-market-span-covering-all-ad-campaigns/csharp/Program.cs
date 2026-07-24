/*
Title: Shortest Market Span Covering All Ad Campaigns

Problem Description:
You are given a chronological stream of website visits represented by an array visits,
where visits[i] is the campaign ID that influenced the i-th visit.

You are also given an integer array required of length m, where required[c] indicates
how many visits influenced by campaign c must appear inside a valid analytics window.
Campaign IDs in visits are in the range [0, m - 1].

Your task is to find the length of the shortest contiguous subarray of visits that
satisfies all campaign requirements simultaneously. In other words, for every campaign c,
the chosen window must contain campaign c at least required[c] times.

This is harder than a standard minimum-cover problem because:
- Some campaigns may require multiple occurrences
- Some campaigns may require zero occurrences
- The input size is large enough that brute force enumeration of all subarrays will time out

An efficient sliding window solution is expected.

Return the minimum possible window length.
If no such window exists, return -1.

Examples:
1)
visits   = [2,0,1,2,0,1,2,1]
required = [1,2,2]
Output   = 5

Explanation:
Campaign 0 must appear at least once, campaign 1 at least twice, and campaign 2 at least twice.
The shortest valid window is [1,2,0,1,2], which has length 5.

2)
visits   = [3,1,3,2,1,0,2,3]
required = [1,1,2,1]
Output   = 6

Explanation:
We need at least one 0, one 1, two 2s, and one 3.
The shortest valid window is [3,2,1,0,2,3], which has length 6.
No shorter contiguous span contains all required counts.

Approach Summary:
We use the classic sliding window / two-pointer technique.

Key idea:
- Expand the right pointer to include more visits until the current window satisfies all requirements.
- Then shrink the left pointer as much as possible while still keeping the window valid.
- Track the minimum valid window length seen.

Important detail:
A campaign is considered "satisfied" when the count inside the current window reaches
its required amount. We count how many campaigns are currently satisfied, and when that
number equals the number of campaigns that actually require something (> 0), the window is valid.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n + m)
    - O(m) to inspect requirements and optionally verify feasibility
    - O(n) for the sliding window because each pointer only moves forward

    Space Complexity:
    O(m)
    - We store counts for campaigns currently inside the window
    - We also store total counts to quickly detect impossible cases
    */
    public int ShortestMarketSpan(int[] visits, int[] required)
    {
        // -----------------------------
        // Step 1: Basic input sizes
        // -----------------------------
        // visits.Length = number of website visits in chronological order
        // required.Length = number of campaign IDs, also the valid ID range size
        int n = visits.Length;
        int m = required.Length;

        // ---------------------------------------------------------
        // Step 2: Count how many campaigns actually matter
        // ---------------------------------------------------------
        // If required[c] == 0, that campaign places no restriction on the window.
        // So we only need to track campaigns with required[c] > 0.
        int campaignsNeedingCoverage = 0;

        // ---------------------------------------------------------
        // Step 3: Feasibility check using total frequencies
        // ---------------------------------------------------------
        // Before doing the sliding window, we can quickly determine whether the
        // entire visits array even contains enough occurrences of each campaign.
        //
        // Why this is useful:
        // If the whole array does not satisfy the requirement for some campaign,
        // then no subarray can possibly satisfy it either.
        //
        // We build totalCounts[c] = total number of times campaign c appears in visits.
        int[] totalCounts = new int[m];
        foreach (int campaign in visits)
        {
            totalCounts[campaign]++;
        }

        for (int c = 0; c < m; c++)
        {
            if (required[c] > 0)
            {
                campaignsNeedingCoverage++;

                // If the full array does not have enough of campaign c,
                // the answer is immediately impossible.
                if (totalCounts[c] < required[c])
                {
                    return -1;
                }
            }
        }

        // -------------------------------------------------------------------
        // Step 4: Handle the special case where nothing is required
        // -------------------------------------------------------------------
        // If every required[c] is 0, then the empty requirement is already satisfied.
        // In many algorithmic contexts, the shortest window would be length 0.
        // Since the problem asks for the shortest contiguous subarray satisfying all
        // requirements, and no campaign is required, length 0 is the mathematically
        // correct minimum.
        if (campaignsNeedingCoverage == 0)
        {
            return 0;
        }

        // -------------------------------------------------------------------
        // Step 5: Prepare sliding window state
        // -------------------------------------------------------------------
        // windowCounts[c] = how many times campaign c appears in the current window [left..right]
        int[] windowCounts = new int[m];

        // satisfiedCampaigns = number of campaigns c such that:
        // windowCounts[c] >= required[c], for campaigns with required[c] > 0
        //
        // More precisely, we increment this exactly when a campaign transitions from
        // "not enough yet" to "just enough".
        int satisfiedCampaigns = 0;

        // left pointer of the sliding window
        int left = 0;

        // best answer found so far; start with a very large number
        int bestLength = int.MaxValue;

        // -------------------------------------------------------------------
        // Step 6: Expand the window by moving right from 0 to n - 1
        // -------------------------------------------------------------------
        // At each step:
        // 1. Add visits[right] into the window
        // 2. If that addition makes a campaign newly satisfied, update satisfiedCampaigns
        // 3. While the window is valid, try shrinking from the left to minimize it
        for (int right = 0; right < n; right++)
        {
            int campaignAdded = visits[right];

            // Add the new rightmost campaign into the current window count.
            windowCounts[campaignAdded]++;

            // -------------------------------------------------------------
            // Why check equality here?
            // -------------------------------------------------------------
            // Suppose required[campaignAdded] = 2.
            // - When count goes from 0 -> 1, still not satisfied
            // - When count goes from 1 -> 2, it becomes satisfied exactly now
            // - When count goes from 2 -> 3, it was already satisfied before
            //
            // So we only increment satisfiedCampaigns when the count becomes
            // exactly equal to the required amount.
            if (required[campaignAdded] > 0 &&
                windowCounts[campaignAdded] == required[campaignAdded])
            {
                satisfiedCampaigns++;
            }

            // -----------------------------------------------------------------
            // Step 7: If all needed campaigns are satisfied, the window is valid
            // -----------------------------------------------------------------
            // Now we try to shrink it from the left as much as possible.
            //
            // This is the heart of the sliding window technique:
            // - right only moves forward
            // - left only moves forward
            // Therefore total work stays linear
            while (satisfiedCampaigns == campaignsNeedingCoverage)
            {
                // Current window is [left..right], inclusive
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // ---------------------------------------------------------
                // Try removing visits[left] and see if the window can remain valid
                // ---------------------------------------------------------
                int campaignRemoved = visits[left];

                // Before decrementing, check whether removing this campaign will
                // break satisfaction for that campaign.
                //
                // If windowCounts[campaignRemoved] == required[campaignRemoved],
                // then after decrementing it will become one less than required,
                // meaning that campaign is no longer satisfied.
                if (required[campaignRemoved] > 0 &&
                    windowCounts[campaignRemoved] == required[campaignRemoved])
                {
                    satisfiedCampaigns--;
                }

                // Actually remove the leftmost element from the window
                windowCounts[campaignRemoved]--;

                // Move left forward to continue shrinking
                left++;
            }
        }

        // ---------------------------------------------------------
        // Step 8: Return the best answer found
        // ---------------------------------------------------------
        // Because we already performed a feasibility check, bestLength should
        // have been updated for all satisfiable cases. Still, this fallback is safe.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// ---------------------------------------------------------
// Demo code
// ---------------------------------------------------------

var solution = new Solution();

// Example 1
int[] visits1 = { 2, 0, 1, 2, 0, 1, 2, 1 };
int[] required1 = { 1, 2, 2 };
int result1 = solution.ShortestMarketSpan(visits1, required1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] visits2 = { 3, 1, 3, 2, 1, 0, 2, 3 };
int[] required2 = { 1, 1, 2, 1 };
int result2 = solution.ShortestMarketSpan(visits2, required2);
Console.WriteLine(result2); // Expected: 6

// Additional demo: impossible case
int[] visits3 = { 0, 1, 1, 2 };
int[] required3 = { 1, 2, 2 };
int result3 = solution.ShortestMarketSpan(visits3, required3);
Console.WriteLine(result3); // Expected: -1

// Additional demo: no requirements
int[] visits4 = { 1, 2, 3 };
int[] required4 = { 0, 0, 0, 0 };
int result4 = solution.ShortestMarketSpan(visits4, required4);
Console.WriteLine(result4); // Expected: 0