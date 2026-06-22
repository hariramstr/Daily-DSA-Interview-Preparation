/*
Title: Longest Compliance Window with Forbidden Pair Threshold
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A security team monitors a sequence of access events represented by an integer array events, where each value denotes the policy group of the event.
Some pairs of policy groups are considered incompatible when they appear together too often in a short time span.

You are also given a list forbiddenPairs, where each element [a, b] means groups a and b form a forbidden pair, and an integer limit.

For any contiguous window of events, define its conflict count as the total number of index pairs (i, j) inside the window such that:
- i < j
- events[i] = a
- events[j] = b
- {a, b} is a forbidden pair

If a forbidden pair is [x, x], then every pair of equal x values inside the window contributes to the conflict count.
The order of values in forbiddenPairs does not matter: [2, 5] and [5, 2] describe the same rule.

Return the length of the longest contiguous subarray whose conflict count is at most limit.

Constraints:
- 1 <= events.length <= 200000
- 1 <= events[i] <= 200000
- 1 <= forbiddenPairs.length <= 200000
- 1 <= a, b <= 200000
- 0 <= limit <= 10^15
- forbiddenPairs may contain duplicates; duplicates should be treated as a single forbidden rule.

Key idea:
We use a sliding window and maintain the total number of conflicting index pairs currently inside the window.
When we add a new value x to the right side of the window, the number of newly created conflicts is exactly:
- count[x] if (x, x) is forbidden
- plus count[y] for every distinct forbidden neighbor y of x with y != x

Why this works:
Every new conflicting pair created by appending x must end at this new position.
So we only need to count how many earlier values already in the window can pair with x under the forbidden rules.

When we remove a value x from the left side of the window, we subtract the number of conflicting pairs that involved that removed occurrence.
At removal time, that occurrence is the earliest element in the window, so every conflicting pair involving it is with some later element still in the window.
That number is:
- (count[x] - 1) if (x, x) is forbidden, because after removal there are count[x] - 1 remaining x values that were paired with it
- plus count[y] for every distinct forbidden neighbor y of x with y != x

This lets us update the conflict count incrementally without recomputing the whole window.

Important note about the examples:
Using the exact problem definition ("the order of values in forbiddenPairs does not matter"), Example 1's stated explanation undercounts.
For events = [1,2,1,3,2,1], forbiddenPairs = [[1,2],[2,3]], limit = 2:
- Window [1,3,2,1] actually has 3 conflicts:
  - (1 at index 2, 2 at index 4)
  - (3 at index 3, 2 at index 4)
  - (2 at index 4, 1 at index 5)
So the correct longest valid length under the stated definition is 3, not 4.
Example 2 is consistent and yields 3.
The algorithm below follows the formal problem statement exactly.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Time Complexity:
    // Let deg(v) be the number of distinct forbidden neighbors of value v.
    // Building the graph takes O(m), where m = forbiddenPairs.Length (after deduplication effect through HashSet).
    // During the sliding window, each event is added once and removed once.
    // Each add/remove scans the forbidden-neighbor list of that event value.
    // Total complexity is O(m + sum over window operations of deg(events[i]))).
    // In practice this is efficient for the given constraints and is the intended incremental approach.
    //
    // Space Complexity:
    // O(U + m), where:
    // - U is the maximum value range used for counts (up to 200000 here)
    // - m is the number of distinct forbidden rules stored in adjacency lists
    public int LongestComplianceWindow(int[] events, int[][] forbiddenPairs, long limit)
    {
        // ------------------------------------------------------------
        // STEP 1: Build a graph of forbidden relationships.
        //
        // We need to quickly answer:
        // "For a value x, which other values y form a forbidden pair with x?"
        //
        // Because the pair order does not matter, [a,b] and [b,a] are the same rule.
        // Also duplicates in forbiddenPairs must be ignored.
        //
        // We will store:
        // - selfForbidden[x] = true if [x,x] exists
        // - neighbors[x] = list of distinct y != x such that {x,y} is forbidden
        //
        // Why split self-pairs from normal neighbors?
        // Because [x,x] has a special counting rule:
        // every pair of equal x values contributes, which is based on combinations among count[x].
        // For incremental updates, adding one x creates count[x] new equal-value conflicts.
        // ------------------------------------------------------------
        const int MaxValue = 200000;

        var selfForbidden = new bool[MaxValue + 1];
        var neighborSets = new HashSet<int>[MaxValue + 1];

        foreach (var pair in forbiddenPairs)
        {
            int a = pair[0];
            int b = pair[1];

            if (a == b)
            {
                // This marks that equal values x with x form forbidden pairs.
                selfForbidden[a] = true;
            }
            else
            {
                // Ensure adjacency containers exist before inserting.
                neighborSets[a] ??= new HashSet<int>();
                neighborSets[b] ??= new HashSet<int>();

                // Add both directions because the rule is unordered.
                neighborSets[a].Add(b);
                neighborSets[b].Add(a);
            }
        }

        // Convert HashSet adjacency into List adjacency.
        // Why do this?
        // - HashSet was useful for deduplication while building.
        // - List is slightly lighter and faster to iterate during the sliding window.
        var neighbors = new List<int>[MaxValue + 1];
        for (int v = 1; v <= MaxValue; v++)
        {
            if (neighborSets[v] != null)
            {
                neighbors[v] = new List<int>(neighborSets[v]);
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Prepare sliding window state.
        //
        // count[x] = how many times value x currently appears in the window [left..right]
        // conflicts = total number of forbidden index pairs currently inside the window
        //
        // We will expand the right boundary one step at a time.
        // If conflicts becomes too large, we shrink from the left until the window is valid again.
        // ------------------------------------------------------------
        var count = new int[MaxValue + 1];
        long conflicts = 0;
        int left = 0;
        int best = 0;

        // ------------------------------------------------------------
        // STEP 3: Standard sliding window expansion.
        //
        // For each new right endpoint:
        //   1) Add events[right] into the window
        //   2) Update conflicts by counting all newly created forbidden pairs ending at right
        //   3) While conflicts > limit, remove events[left] and move left forward
        //   4) Record the maximum valid window length
        // ------------------------------------------------------------
        for (int right = 0; right < events.Length; right++)
        {
            int x = events[right];

            // --------------------------------------------------------
            // ADDING x TO THE RIGHT
            //
            // We now count how many new forbidden pairs are created by placing this x at the end.
            //
            // Why can we do this locally?
            // Because every newly created pair must include the new element at index right.
            // All other pairs among old elements were already counted before.
            //
            // Newly created conflicts:
            // 1) Equal-value conflicts for [x,x]:
            //    If selfForbidden[x] is true, then each existing x in the window pairs with the new x.
            //    There are count[x] such existing x values.
            //
            // 2) Cross-value conflicts for every forbidden neighbor y of x:
            //    Every existing y in the window forms one new pair with this new x.
            //    There are count[y] such values.
            // --------------------------------------------------------
            if (selfForbidden[x])
            {
                conflicts += count[x];
            }

            var list = neighbors[x];
            if (list != null)
            {
                foreach (int y in list)
                {
                    conflicts += count[y];
                }
            }

            // After counting the new pairs, we can safely include x in the window frequency table.
            count[x]++;

            // --------------------------------------------------------
            // SHRINK FROM THE LEFT WHILE THE WINDOW IS INVALID
            //
            // If conflicts > limit, the current window is not allowed.
            // We must remove elements from the left until the conflict count is small enough.
            // --------------------------------------------------------
            while (conflicts > limit)
            {
                int removeValue = events[left];

                // ----------------------------------------------------
                // REMOVING removeValue FROM THE LEFT
                //
                // This is the subtle part.
                //
                // We need to subtract exactly the number of conflicting pairs that involved
                // the leftmost occurrence being removed.
                //
                // Why is this easy from the left side?
                // Because that removed occurrence is the earliest element in the current window.
                // Therefore, every pair involving it is with some later element still in the window.
                //
                // We first decrement count[removeValue], so count[] now represents the window
                // AFTER removal. Then:
                //
                // 1) If [x,x] is forbidden:
                //    the removed x had a forbidden pair with every remaining x.
                //    After decrement, there are count[x] remaining x values.
                //
                // 2) For each forbidden neighbor y != x:
                //    the removed x had one forbidden pair with each remaining y.
                //    There are count[y] such values after removal.
                //
                // Subtracting these values removes exactly the pairs that disappear.
                // ----------------------------------------------------
                count[removeValue]--;

                if (selfForbidden[removeValue])
                {
                    conflicts -= count[removeValue];
                }

                var removeList = neighbors[removeValue];
                if (removeList != null)
                {
                    foreach (int y in removeList)
                    {
                        conflicts -= count[y];
                    }
                }

                left++;
            }

            // --------------------------------------------------------
            // At this point, the window [left..right] is valid:
            // conflicts <= limit
            //
            // So we can update the best answer.
            // --------------------------------------------------------
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// According to the formal statement (unordered forbidden pairs), the correct answer is 3.
int[] events1 = { 1, 2, 1, 3, 2, 1 };
int[][] forbiddenPairs1 =
{
    new[] { 1, 2 },
    new[] { 2, 3 }
};
long limit1 = 2;
int result1 = solution.LongestComplianceWindow(events1, forbiddenPairs1, limit1);
Console.WriteLine(result1);

// Example 2 from the prompt. Expected: 3
int[] events2 = { 4, 4, 4, 2, 4 };
int[][] forbiddenPairs2 =
{
    new[] { 4, 4 },
    new[] { 2, 4 }
};
long limit2 = 3;
int result2 = solution.LongestComplianceWindow(events2, forbiddenPairs2, limit2);
Console.WriteLine(result2);