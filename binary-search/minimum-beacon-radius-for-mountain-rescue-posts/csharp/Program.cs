/*
Title: Minimum Beacon Radius for Mountain Rescue Posts

Problem Description:
A rescue agency maintains emergency beacon posts along a long mountain trail.
The trail is modeled as a one-dimensional line.

You are given two integer arrays:
- posts, where posts[i] is the position of the i-th rescue post
- shelters, where shelters[j] is the position of the j-th shelter that must be covered

A rescue post with signal radius R covers every shelter whose distance from that post
is at most R.

Your task is to compute the minimum integer radius R such that every shelter is covered
by at least one post.

Important details:
- The arrays are not guaranteed to be sorted
- Positions may be negative
- Multiple posts or shelters may share the same coordinate
- Inputs can be very large, so an O(n * m) solution is too slow

Efficient idea:
1. Sort the post positions and shelter positions
2. Binary search the answer R
3. For a candidate radius R, check if all shelters can be covered efficiently

Example 1:
posts = [1, 8, 15]
shelters = [2, 6, 10, 14]
Output: 2

Example 2:
posts = [-5, 0, 9]
shelters = [-8, -4, 3, 11]
Output: 4
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting posts: O(p log p)
    - Sorting shelters: O(s log s)
    - Feasibility check for one radius: O(p + s)
    - Binary search over radius range: O(log M), where M is the numeric search range
    Total: O(p log p + s log s + (p + s) log M)

    Space Complexity:
    - O(1) extra space beyond the sorting implementation details
      (If we ignore the internal stack/implementation details of Array.Sort)
    */
    public int FindMinimumRadius(int[] posts, int[] shelters)
    {
        // We sort both arrays because sorted order lets us check coverage in a single linear pass.
        // Without sorting, it would be much harder to efficiently determine whether every shelter
        // is covered by some post for a given radius.
        Array.Sort(posts);
        Array.Sort(shelters);

        // We will binary search the smallest radius that works.
        //
        // Lower bound:
        // - Radius can never be negative, so 0 is a valid lower bound.
        //
        // Upper bound:
        // - A safe upper bound is the maximum possible distance between any shelter and any post.
        // - Since coordinates are within [-1e9, 1e9], the maximum distance is at most 2e9.
        // - We use long during calculations to avoid overflow in intermediate arithmetic.
        long left = 0;
        long right = 2_000_000_000L;

        // Standard binary search for the first "good" value.
        // If a radius works, we try smaller.
        // If it does not work, we must try larger.
        while (left < right)
        {
            long mid = left + (right - left) / 2;

            // Check whether every shelter can be covered using this candidate radius.
            if (CanCoverAllShelters(posts, shelters, mid))
            {
                // mid works, so the answer is <= mid
                right = mid;
            }
            else
            {
                // mid does not work, so the answer must be > mid
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the smallest feasible radius.
        return (int)left;
    }

    private bool CanCoverAllShelters(int[] posts, int[] shelters, long radius)
    {
        // This method answers:
        // "If every post has signal radius = radius, are all shelters covered?"
        //
        // Because both arrays are sorted, we can scan from left to right.
        //
        // Key idea:
        // For each post, its coverage interval is:
        // [post - radius, post + radius]
        //
        // Since shelters are sorted, we can keep a pointer to the first shelter
        // not yet confirmed as covered. Then for each post, we advance that pointer
        // while the current shelter lies inside this post's coverage interval.
        //
        // If we reach the end of the shelters array, then every shelter is covered.
        // If after processing all posts some shelters remain, then this radius is not enough.

        int shelterIndex = 0;
        int shelterCount = shelters.Length;

        // Process each post from left to right.
        for (int i = 0; i < posts.Length && shelterIndex < shelterCount; i++)
        {
            long postPosition = posts[i];

            // Compute the left and right ends of this post's coverage interval.
            // We use long to safely handle arithmetic like postPosition +/- radius.
            long leftReach = postPosition - radius;
            long rightReach = postPosition + radius;

            // If the current shelter is left of leftReach, this post cannot cover it.
            // But maybe a later post can.
            //
            // However, because posts are sorted, later posts are even farther to the right.
            // That means if a shelter is too far left to be covered by the current post,
            // it also cannot be covered by any later post if we have already passed the best chance.
            //
            // In practice, the logic below naturally handles this:
            // - We only advance shelterIndex when a shelter is covered.
            // - If a shelter remains uncovered and all posts are processed, we return false.

            // Skip all shelters that are covered by this post.
            //
            // Why does this work?
            // - shelters are sorted
            // - once a shelter is covered, we never need to look at it again
            // - if the current shelter is within [leftReach, rightReach], then this post covers it
            // - and maybe it also covers the next shelter, and the next, etc.
            while (shelterIndex < shelterCount)
            {
                long shelterPosition = shelters[shelterIndex];

                if (shelterPosition < leftReach)
                {
                    // This shelter lies to the left of the current post's coverage interval.
                    //
                    // Since posts are processed in increasing order, all future posts are even farther right.
                    // Therefore, this shelter can never be covered by any future post either.
                    //
                    // That means the candidate radius fails immediately.
                    return false;
                }

                if (shelterPosition <= rightReach)
                {
                    // This shelter is inside the current post's coverage interval,
                    // so it is covered. Move to the next shelter.
                    shelterIndex++;
                }
                else
                {
                    // This shelter is to the right of the current post's coverage interval.
                    // The current post cannot cover it, so we stop and move to the next post.
                    break;
                }
            }
        }

        // If we covered every shelter, shelterIndex reached the end.
        return shelterIndex == shelterCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] posts1 = { 1, 8, 15 };
int[] shelters1 = { 2, 6, 10, 14 };
int result1 = solution.FindMinimumRadius(posts1, shelters1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] posts2 = { -5, 0, 9 };
int[] shelters2 = { -8, -4, 3, 11 };
int result2 = solution.FindMinimumRadius(posts2, shelters2);
Console.WriteLine(result2); // Expected: 4