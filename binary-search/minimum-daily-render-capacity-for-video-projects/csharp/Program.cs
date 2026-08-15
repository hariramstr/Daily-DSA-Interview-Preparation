/*
Title: Minimum Daily Render Capacity for Video Projects

Problem Description:
A media studio needs to render a sequence of video projects on a shared render farm.
You are given an array `frames`, where `frames[i]` is the number of frame-units required
by the `i`th project. The projects must be rendered in the given order, and a single day
can process only a contiguous group of projects.

If the render farm has daily capacity `C`, then the total frame-units assigned to any one
day cannot exceed `C`.

Given `frames` and an integer `d`, return the minimum daily render capacity needed to finish
all projects in at most `d` days.

Rules:
- You cannot split a single project across multiple days.
- You cannot reorder projects.
- Therefore, we need the smallest capacity that still allows all projects to be completed
  within the allowed number of days.

Key idea:
- For any fixed capacity, we can simulate how many days are needed.
- If a capacity works, then any larger capacity also works.
- This "works / does not work" pattern is monotonic, which makes Binary Search the ideal tool.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log(S))
      where:
      n = number of projects
      S = search range of capacities, from max(frames) to sum(frames)

    Why?
    - Each binary search step checks feasibility in O(n) time by scanning the array once.
    - The number of binary search steps is O(log(S)).

    Space Complexity:
    - O(1) extra space
      We only use a few variables and do not allocate extra data structures proportional to input size.
    */
    public long MinimumDailyRenderCapacity(int[] frames, int d)
    {
        // -----------------------------
        // STEP 1: Establish the binary search boundaries.
        // -----------------------------
        //
        // We need to search for the minimum feasible daily capacity.
        //
        // Lower bound:
        // - The capacity can never be smaller than the largest single project.
        // - Reason: a project cannot be split across days, so at minimum we must be able
        //   to fit the biggest project into one day.
        //
        // Upper bound:
        // - The capacity can always be the sum of all projects.
        // - Reason: with that capacity, we can render everything in one day.
        //
        // We use long because:
        // - frames[i] can be as large as 1,000,000,000
        // - there can be up to 100,000 projects
        // - the total sum can exceed int range
        long left = 0;
        long right = 0;

        foreach (int frame in frames)
        {
            if (frame > left)
            {
                left = frame;
            }

            right += frame;
        }

        // -----------------------------
        // STEP 2: Binary search over the answer space.
        // -----------------------------
        //
        // We are not searching in the array itself.
        // We are searching in the range of possible capacities.
        //
        // Invariant:
        // - Any capacity < answer is not feasible.
        // - Any capacity >= answer is feasible.
        //
        // Therefore, we can use classic binary search to find the first feasible capacity.
        while (left < right)
        {
            // Compute the middle capacity safely.
            // Using this form avoids overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate capacity is enough to finish within d days.
            if (CanFinishWithinDays(frames, d, mid))
            {
                // If mid works, it might be the answer,
                // but there could still be a smaller feasible capacity.
                // So we keep searching the left half, including mid.
                right = mid;
            }
            else
            {
                // If mid does not work, then every smaller capacity also does not work.
                // So we must search strictly to the right of mid.
                left = mid + 1;
            }
        }

        // When left == right, binary search has converged to the smallest feasible capacity.
        return left;
    }

    private bool CanFinishWithinDays(int[] frames, int d, long capacity)
    {
        // -----------------------------
        // STEP 3: Simulate the rendering process for a fixed capacity.
        // -----------------------------
        //
        // Goal:
        // - Determine whether all projects can be rendered in at most d days
        //   if each day has the given capacity.
        //
        // Strategy:
        // - Process projects in order, exactly as required by the problem.
        // - Keep adding projects to the current day while they fit.
        // - If the next project would exceed the capacity, start a new day.
        //
        // Why this greedy strategy is correct:
        // - Since projects must remain in order and each day must contain a contiguous block,
        //   the best way to minimize the number of days for a fixed capacity is to pack each day
        //   as much as possible before moving to the next day.
        //
        // Data structure choice:
        // - No extra data structure is needed.
        // - A simple running sum and day counter are enough.
        int daysUsed = 1;
        long currentDayLoad = 0;

        foreach (int frame in frames)
        {
            // If adding this project would exceed today's capacity,
            // we must start a new day.
            if (currentDayLoad + frame > capacity)
            {
                daysUsed++;
                currentDayLoad = frame;

                // Small optimization:
                // If we already exceeded the allowed number of days,
                // there is no need to continue scanning.
                if (daysUsed > d)
                {
                    return false;
                }
            }
            else
            {
                // Otherwise, place the project into the current day.
                currentDayLoad += frame;
            }
        }

        // If we finished scanning all projects and used at most d days,
        // then this capacity is feasible.
        return true;
    }
}

// --------------------------------------------------
// Demo code
// --------------------------------------------------

var solution = new Solution();

// Example 1
int[] frames1 = { 30, 10, 20, 40, 25 };
int d1 = 3;
long result1 = solution.MinimumDailyRenderCapacity(frames1, d1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] frames2 = { 8, 15, 7, 12, 10 };
int d2 = 2;
long result2 = solution.MinimumDailyRenderCapacity(frames2, d2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick checks
int[] frames3 = { 5 };
int d3 = 1;
long result3 = solution.MinimumDailyRenderCapacity(frames3, d3);
Console.WriteLine($"Single Project Result: {result3}");

int[] frames4 = { 10, 20, 30, 40 };
int d4 = 4;
long result4 = solution.MinimumDailyRenderCapacity(frames4, d4);
Console.WriteLine($"One Project Per Day Result: {result4}");

int[] frames5 = { 10, 20, 30, 40 };
int d5 = 1;
long result5 = solution.MinimumDailyRenderCapacity(frames5, d5);
Console.WriteLine($"All In One Day Result: {result5}");