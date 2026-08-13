/*
Title: Shortest Browser Session Covering Required Domains
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A security team analyzes a user's browsing history as an array visits, where visits[i] is the domain opened at minute i.
For an investigation, the team is given a requirement map need describing how many times each important domain must
appear inside a single contiguous session. Your task is to find the length of the shortest contiguous subarray of visits
that satisfies all domain requirements.

A window is valid if for every domain d in need, the window contains at least need[d] occurrences of d.
Domains not listed in need may appear any number of times and do not affect validity.
If no valid session exists, return -1.

This is not just a basic coverage problem: the input size is large, domain names may repeat heavily, and the solution
must scale close to linear time. An O(n^2) solution will time out.

Return the minimum possible length of a valid contiguous session.

Constraints:
- 1 <= visits.length <= 200000
- 1 <= need.size <= 50000
- Sum of all required counts in need <= visits.length
- Each domain name consists of lowercase English letters, digits, dots, and hyphens
- 1 <= domain name length <= 30
- 1 <= need[d] <= 100000

Example 1:
visits = ["news.com","mail.com","shop.com","news.com","video.com","mail.com","news.com"]
need = {"news.com": 2, "mail.com": 1}
Output: 4

Example 2:
visits = ["a.com","b.com","a.com","c.com","b.com"]
need = {"a.com": 2, "b.com": 2, "d.com": 1}
Output: -1
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m), where:
      n = visits.Length
      m = number of keys in need
    Explanation:
    - We move the right pointer from left to right once across the visits array.
    - We move the left pointer from left to right at most once as well.
    - Dictionary operations are average O(1).
    - Therefore the total work is linear in the size of the input.

    Space Complexity:
    - O(m)
    Explanation:
    - We store required counts from need.
    - We store current counts only for domains that matter.
    - So extra memory is proportional to the number of required domains.
    */
    public int MinSessionLength(string[] visits, Dictionary<string, int> need)
    {
        // If there are no requirements, the shortest valid window would conceptually be 0.
        // The problem constraints imply need.size >= 1, but this guard makes the method robust.
        if (need == null || need.Count == 0)
        {
            return 0;
        }

        // This dictionary tracks how many times each required domain currently appears
        // inside the sliding window [left, right].
        //
        // Important design choice:
        // We only track domains that appear in "need".
        // Domains not required do not affect validity, so storing them would waste memory
        // and slow down processing unnecessarily.
        var windowCounts = new Dictionary<string, int>(need.Count);

        // "requiredKinds" means how many distinct domain names have requirements.
        // Example:
        // need = { "news.com": 2, "mail.com": 1 }
        // requiredKinds = 2
        int requiredKinds = need.Count;

        // "formedKinds" means how many distinct required domains are currently satisfied.
        // A domain becomes "formed" when its count in the current window reaches
        // at least the required count.
        //
        // Example:
        // If need["news.com"] = 2, then:
        // - count 0 or 1 => not formed
        // - count 2 or more => formed
        int formedKinds = 0;

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        // We initialize to int.MaxValue to mean "no valid window found yet".
        int bestLength = int.MaxValue;

        // Expand the window by moving "right" one step at a time.
        for (int right = 0; right < visits.Length; right++)
        {
            string currentDomain = visits[right];

            // Step 1: Include visits[right] into the current window.
            //
            // We only care if this domain is one of the required domains.
            // If it is not required, it can still be inside the window,
            // but it does not help or hurt validity.
            if (need.TryGetValue(currentDomain, out int requiredCountForCurrent))
            {
                // Increase the count of this required domain in the current window.
                if (!windowCounts.TryAdd(currentDomain, 1))
                {
                    windowCounts[currentDomain]++;
                }

                // Step 2: Check whether this domain has just become satisfied.
                //
                // This is a very important subtle point:
                // We only increment formedKinds when the count becomes EXACTLY equal
                // to the required count.
                //
                // Why exactly equal?
                // Because if the count goes from requiredCount to requiredCount + 1,
                // the domain was already satisfied before, so we must not count it twice.
                if (windowCounts[currentDomain] == requiredCountForCurrent)
                {
                    formedKinds++;
                }
            }

            // Step 3: If all required domains are satisfied, try to shrink the window
            // from the left side to make it as short as possible while staying valid.
            //
            // This is the heart of the sliding window technique:
            // - Expand right until valid
            // - Then shrink left while still valid
            // This guarantees we consider each index only a constant number of times.
            while (formedKinds == requiredKinds)
            {
                // The current window [left, right] is valid.
                // Compute its length and update the best answer if this one is smaller.
                int currentLength = right - left + 1;
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // We are about to remove visits[left] from the window,
                // so first remember what it is.
                string leftDomain = visits[left];

                // Only required domains matter for validity bookkeeping.
                if (need.TryGetValue(leftDomain, out int requiredCountForLeft))
                {
                    // Decrease the count because this domain is leaving the window.
                    windowCounts[leftDomain]--;

                    // If after decrementing, the count falls BELOW the required amount,
                    // then this domain is no longer satisfied.
                    //
                    // Example:
                    // need["mail.com"] = 1
                    // window had 1 mail.com => satisfied
                    // remove it => count becomes 0 => no longer satisfied
                    //
                    // Therefore formedKinds must decrease.
                    if (windowCounts[leftDomain] < requiredCountForLeft)
                    {
                        formedKinds--;
                    }
                }

                // Finally move the left boundary forward to continue shrinking.
                left++;
            }
        }

        // If bestLength was never updated, no valid window exists.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string[] visits1 =
{
    "news.com", "mail.com", "shop.com", "news.com", "video.com", "mail.com", "news.com"
};

var need1 = new Dictionary<string, int>
{
    ["news.com"] = 2,
    ["mail.com"] = 1
};

int result1 = solution.MinSessionLength(visits1, need1);
Console.WriteLine(result1); // Expected: 4

// Example 2
string[] visits2 =
{
    "a.com", "b.com", "a.com", "c.com", "b.com"
};

var need2 = new Dictionary<string, int>
{
    ["a.com"] = 2,
    ["b.com"] = 2,
    ["d.com"] = 1
};

int result2 = solution.MinSessionLength(visits2, need2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity check
string[] visits3 =
{
    "x.com", "y.com", "x.com", "z.com", "y.com", "x.com"
};

var need3 = new Dictionary<string, int>
{
    ["x.com"] = 2,
    ["y.com"] = 1
};

int result3 = solution.MinSessionLength(visits3, need3);
Console.WriteLine(result3); // One valid shortest window is ["y.com","x.com","z.com","y.com","x.com"]? length 5, but ["x.com","z.com","y.com","x.com"] length 4 => Expected: 4