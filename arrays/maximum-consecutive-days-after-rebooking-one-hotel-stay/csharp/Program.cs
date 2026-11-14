/*
Title: Maximum Consecutive Days After Rebooking One Hotel Stay

Problem Description:
You are given a strictly increasing array bookedDays, where each value represents a calendar day on which a traveler already has a hotel reservation.
The traveler wants the longest possible streak of consecutive booked days.

To improve the itinerary, they are allowed to rebook exactly one existing reservation to any other day number,
as long as the final set of booked days still contains no duplicates.

Return the maximum number of consecutive calendar days that can appear in the final set after moving at most one reservation.

A rebooking means choosing one value from bookedDays, removing it, and inserting a different positive integer day.
You may also choose not to move any reservation if the array is already optimal.
The relative order of the array does not matter after the move; only the set of booked days matters.

This is an arrays problem focused on analyzing gaps between consecutive values.
A correct solution should run efficiently for large inputs.

Constraints:
- 1 <= bookedDays.length <= 200000
- 1 <= bookedDays[i] <= 1000000000
- bookedDays is strictly increasing

Examples:
1) bookedDays = [2, 4, 5, 7]
   Best move: move 7 -> 3
   Final set: [2, 3, 4, 5]
   Longest consecutive streak = 4

2) bookedDays = [10, 11, 14, 15, 16, 20]
   Best move: move 20 -> 13
   Final set: [10, 11, 13, 14, 15, 16]
   Longest consecutive streak = 4
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Idea in simple words:
    1. Break the sorted days into maximal consecutive blocks.
       Example: [10, 11, 14, 15, 16, 20] -> blocks [10,11], [14,15,16], [20]
       Their lengths are [2, 3, 1].

    2. With one move, there are only a few meaningful possibilities:
       - Extend one block by 1 by moving some reservation from somewhere else.
       - Fill a gap of exactly 1 missing day between two neighboring blocks, which merges them.
         Example: [2] gap [3] [4,5] -> moving some other reservation to 3 merges into length 4.
       - If the gap between two neighboring blocks is exactly 2, then there is exactly one missing day,
         so filling it merges both blocks completely.
       - If the gap is larger than 2, one move can only attach to one side, not fully bridge the gap.

    3. Important subtlety:
       When we "take one reservation from somewhere else", we must make sure removing that reservation
       does not destroy the very block we are trying to count.
       The safest source is:
       - any block not involved in the target construction, or
       - a block of length at least 2 if removing one endpoint still leaves something.
       To handle this cleanly, we track how many blocks exist and the maximum block length outside a small set.

    4. We evaluate:
       - no move: answer is current maximum block length
       - extend each block by 1 if there exists a usable donor reservation outside that block
       - merge neighboring blocks across a single missing day if there exists a usable donor outside those two blocks
         (or if one of those blocks itself has extra length and can donate an endpoint without reducing the merged result)

    This gives a correct O(n) solution.
    */
    public int MaxConsecutiveDaysAfterOneRebooking(int[] bookedDays)
    {
        int n = bookedDays.Length;

        // If there is only one reservation, the longest consecutive streak is always 1.
        // We may move it somewhere else, but we still only have one booked day.
        if (n == 1)
            return 1;

        // -----------------------------
        // STEP 1: Build consecutive blocks.
        // -----------------------------
        // Each block is a maximal run of consecutive integers.
        // Example:
        // bookedDays = [2, 4, 5, 7]
        // blocks:
        //   [2] length 1
        //   [4,5] length 2
        //   [7] length 1
        //
        // We store:
        // - start day of each block
        // - end day of each block
        // - length of each block
        //
        // Why this is useful:
        // The entire problem becomes "how can one move affect these blocks?"
        // Since only gaps matter, block compression is the natural representation.
        var starts = new List<int>();
        var ends = new List<int>();
        var lens = new List<int>();

        int blockStart = bookedDays[0];
        int prev = bookedDays[0];

        for (int i = 1; i < n; i++)
        {
            if (bookedDays[i] == prev + 1)
            {
                // Still inside the same consecutive block.
                prev = bookedDays[i];
            }
            else
            {
                // The current block ended at "prev".
                starts.Add(blockStart);
                ends.Add(prev);
                lens.Add(prev - blockStart + 1);

                // Start a new block.
                blockStart = bookedDays[i];
                prev = bookedDays[i];
            }
        }

        // Add the final block after the loop.
        starts.Add(blockStart);
        ends.Add(prev);
        lens.Add(prev - blockStart + 1);

        int m = lens.Count;

        // If everything is already one consecutive block:
        // We can choose not to move any reservation, so answer is n.
        if (m == 1)
            return n;

        // -----------------------------
        // STEP 2: Precompute prefix/suffix maximum block lengths.
        // -----------------------------
        // These arrays let us quickly ask:
        // "What is the largest block length outside a certain index or outside a pair of indices?"
        //
        // This is important because when we want to extend/merge a block,
        // we often need a donor reservation from some OTHER block.
        //
        // prefixMax[i] = maximum lens[0..i]
        // suffixMax[i] = maximum lens[i..m-1]
        int[] prefixMax = new int[m];
        int[] suffixMax = new int[m];

        prefixMax[0] = lens[0];
        for (int i = 1; i < m; i++)
            prefixMax[i] = Math.Max(prefixMax[i - 1], lens[i]);

        suffixMax[m - 1] = lens[m - 1];
        for (int i = m - 2; i >= 0; i--)
            suffixMax[i] = Math.Max(suffixMax[i + 1], lens[i]);

        // Helper local function:
        // Returns the maximum block length among all blocks except possibly one or two excluded indices.
        //
        // Why we need this:
        // Suppose we want to extend block i by moving a reservation from another block.
        // We need to know whether some other block exists.
        // If the largest outside block has length >= 1, then there is at least one reservation available outside.
        //
        // For merging blocks i and i+1, we may need a donor outside both.
        int MaxOutsideOne(int exclude)
        {
            int best = 0;
            if (exclude - 1 >= 0) best = Math.Max(best, prefixMax[exclude - 1]);
            if (exclude + 1 < m) best = Math.Max(best, suffixMax[exclude + 1]);
            return best;
        }

        int MaxOutsideTwoAdjacent(int leftIndex)
        {
            // Excludes leftIndex and leftIndex + 1.
            int best = 0;
            if (leftIndex - 1 >= 0) best = Math.Max(best, prefixMax[leftIndex - 1]);
            if (leftIndex + 2 < m) best = Math.Max(best, suffixMax[leftIndex + 2]);
            return best;
        }

        // Start with the best "do nothing" answer:
        // the largest existing consecutive block.
        int answer = prefixMax[m - 1];

        // -----------------------------
        // STEP 3: Try extending a single block by 1.
        // -----------------------------
        // For any block, if there exists at least one reservation outside this block,
        // we can move that reservation to either just before the block or just after the block,
        // provided that target day is not already occupied.
        //
        // Because blocks are maximal, the day immediately before/after the block is not inside the block.
        // It might be blocked by a neighboring block only if the gap is zero, which cannot happen between blocks.
        //
        // So every block can potentially become length + 1, as long as some donor reservation exists elsewhere.
        for (int i = 0; i < m; i++)
        {
            int outsideMax = MaxOutsideOne(i);

            // If outsideMax >= 1, then there is at least one reservation in another block.
            // We can move one such reservation to extend this block by one day.
            if (outsideMax >= 1)
            {
                answer = Math.Max(answer, lens[i] + 1);
            }
        }

        // -----------------------------
        // STEP 4: Try merging neighboring blocks.
        // -----------------------------
        // Consider neighboring blocks i and i+1.
        // Let gap = starts[i+1] - ends[i] - 1
        //
        // Cases:
        // A) gap == 1
        //    There is exactly one missing day between them.
        //    If we can place one moved reservation into that missing day,
        //    the two blocks merge into lens[i] + 1 + lens[i+1].
        //
        //    Example:
        //    [2] and [4,5] have gap day 3.
        //    Move some other reservation to 3 -> [2,3,4,5], total 4.
        //
        // B) gap > 1
        //    One move cannot fill all missing days.
        //    So we cannot fully merge them.
        //    The best involving these two is just extending one side by 1,
        //    which Step 3 already covers.
        //
        // Donor logic for gap == 1:
        // We need one reservation to move into the missing day.
        // That donor can come from:
        // - any block outside these two, or
        // - from one of these two blocks if that block has length >= 2 and we move an endpoint.
        //
        // Why length >= 2 matters:
        // If a block has length 1 and we remove its only element, that whole side disappears,
        // so the intended merged total would not be preserved.
        //
        // If a block has length >= 2, removing an endpoint shortens it by 1,
        // but adding the bridge day increases by 1, so the total merged size remains lens[i] + lens[i+1].
        // However, if we are claiming lens[i] + 1 + lens[i+1], that +1 is the bridge itself.
        // Removing one endpoint from one involved block reduces total by 1, so the actual result becomes lens[i] + lens[i+1].
        //
        // Therefore, to achieve the full merged size lens[i] + 1 + lens[i+1],
        // the donor must come from OUTSIDE these two blocks.
        //
        // If no outside donor exists, but one involved block has length >= 2,
        // then we can still move an endpoint from that block into the gap,
        // and the resulting merged size is lens[i] + lens[i+1].
        for (int i = 0; i + 1 < m; i++)
        {
            int gap = starts[i + 1] - ends[i] - 1;

            if (gap == 1)
            {
                int outsideMax = MaxOutsideTwoAdjacent(i);

                // Best case: donor exists outside both blocks.
                // Then we keep both blocks intact and add the missing bridge day.
                if (outsideMax >= 1)
                {
                    answer = Math.Max(answer, lens[i] + 1 + lens[i + 1]);
                }
                else
                {
                    // No outside donor exists.
                    // Then the only possible donor would be from one of these two blocks.
                    // If one of them has length >= 2, we can move an endpoint into the gap.
                    // That shortens one side by 1 and adds the bridge by 1, so total becomes lens[i] + lens[i+1].
                    if (lens[i] >= 2 || lens[i + 1] >= 2)
                    {
                        answer = Math.Max(answer, lens[i] + lens[i + 1]);
                    }
                }
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

int[] bookedDays1 = { 2, 4, 5, 7 };
int result1 = solution.MaxConsecutiveDaysAfterOneRebooking(bookedDays1);
Console.WriteLine($"Input: [{string.Join(", ", bookedDays1)}]");
Console.WriteLine($"Maximum consecutive booked days after at most one rebooking: {result1}");
Console.WriteLine();

int[] bookedDays2 = { 10, 11, 14, 15, 16, 20 };
int result2 = solution.MaxConsecutiveDaysAfterOneRebooking(bookedDays2);
Console.WriteLine($"Input: [{string.Join(", ", bookedDays2)}]");
Console.WriteLine($"Maximum consecutive booked days after at most one rebooking: {result2}");
Console.WriteLine();

int[] bookedDays3 = { 1 };
int result3 = solution.MaxConsecutiveDaysAfterOneRebooking(bookedDays3);
Console.WriteLine($"Input: [{string.Join(", ", bookedDays3)}]");
Console.WriteLine($"Maximum consecutive booked days after at most one rebooking: {result3}");
Console.WriteLine();

int[] bookedDays4 = { 1, 2, 3, 10 };
int result4 = solution.MaxConsecutiveDaysAfterOneRebooking(bookedDays4);
Console.WriteLine($"Input: [{string.Join(", ", bookedDays4)}]");
Console.WriteLine($"Maximum consecutive booked days after at most one rebooking: {result4}");
Console.WriteLine();

int[] bookedDays5 = { 1, 3 };
int result5 = solution.MaxConsecutiveDaysAfterOneRebooking(bookedDays5);
Console.WriteLine($"Input: [{string.Join(", ", bookedDays5)}]");
Console.WriteLine($"Maximum consecutive booked days after at most one rebooking: {result5}");