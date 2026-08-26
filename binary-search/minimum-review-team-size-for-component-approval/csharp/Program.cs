/*
Title: Minimum Review Team Size for Component Approval

Problem Description:
You are planning code reviews for a large release made up of n software components.
Component i requires reviews[i] independent review comments before it can be approved.

You have a pool of engineers, and every engineer can review a contiguous block of
components during the release window. Because of domain knowledge limits, a single
engineer can cover at most span consecutive components, but while assigned to that block,
the engineer contributes exactly 1 review to every component in the block.

You may choose any number of engineers and assign each engineer to any contiguous block
of length at most span. Multiple engineers may review overlapping blocks.

A component is approved if the total number of engineers whose assigned blocks include
that component is at least reviews[i].

Return the minimum number of engineers needed so that every component is approved.

Key idea:
- If it is possible with X engineers, then it is also possible with any number > X.
- That monotonic property allows binary search on the answer.
- For a fixed candidate number of engineers K, we need an efficient feasibility check.

Important observation for the feasibility check:
- To maximize usefulness, when we decide to add engineers at position i, we should start
  them at i and let them cover as far right as possible, i.e. the block [i, min(n-1, i+span-1)].
- This greedy choice is optimal because it satisfies the current component immediately
  while also helping the largest possible number of future components.
- We can simulate active coverage using a difference-array / sweep-line technique so that
  each feasibility check runs in O(n).

Example verification:
1) reviews = [1,2,2,1], span = 2
   Minimum answer is 3.
2) reviews = [3,0,1,4,2], span = 3
   Minimum answer is 6.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Let n be reviews.Length.
    - Each feasibility check runs in O(n).
    - Binary search runs in O(log S), where S = sum(reviews).
    - Total: O(n log S)

    Space Complexity:
    - O(n) for the difference array used in the feasibility check.
    */
    public long MinimumReviewTeamSize(int[] reviews, int span)
    {
        int n = reviews.Length;

        // We binary search on the minimum number of engineers.
        //
        // Lower bound:
        // - 0 engineers might be enough if every reviews[i] is 0.
        //
        // Upper bound:
        // - sum(reviews) is always sufficient:
        //   for each component i, we could assign reviews[i] engineers to the single-element
        //   block [i, i] (length 1 <= span), so every component gets enough reviews.
        //
        // We use long because reviews[i] can be up to 1e9 and n up to 2e5,
        // so the sum can be as large as 2e14.
        long low = 0;
        long high = 0;

        foreach (int need in reviews)
        {
            high += need;
        }

        // Standard binary search for the first feasible value.
        while (low < high)
        {
            long mid = low + (high - low) / 2;

            // If it is possible to satisfy all components using at most mid engineers,
            // then the true answer is <= mid, so we search the left half.
            if (CanApproveWithKEngineers(reviews, span, mid))
            {
                high = mid;
            }
            else
            {
                // Otherwise we need more engineers.
                low = mid + 1;
            }
        }

        return low;
    }

    private bool CanApproveWithKEngineers(int[] reviews, int span, long k)
    {
        int n = reviews.Length;

        // We sweep from left to right.
        //
        // activeCoverage:
        // - how many currently active engineer assignments are covering the current index i
        //
        // diff array:
        // - when we add x engineers starting at i, they contribute +x coverage immediately
        //   and continue for up to 'span' positions.
        // - To remove their effect after their coverage ends, we schedule a subtraction at
        //   index i + span.
        //
        // We allocate n + 1 so that writing diff[end] is always safe when end == n.
        long[] diff = new long[n + 1];
        long activeCoverage = 0;
        long usedEngineers = 0;

        for (int i = 0; i < n; i++)
        {
            // Step 1: apply any scheduled coverage removals/additions at this index.
            //
            // Why this is necessary:
            // - activeCoverage must always represent exactly how many previously assigned
            //   engineers still cover component i.
            activeCoverage += diff[i];

            // Step 2: check whether the current component already has enough reviews.
            //
            // If activeCoverage >= reviews[i], then previous assignments already satisfy
            // this component, so we do not need to add anything here.
            if (activeCoverage >= reviews[i])
            {
                continue;
            }

            // Step 3: compute how many additional engineers are required right now.
            //
            // Since we are processing left to right, once we leave index i we can never
            // come back and increase its coverage using assignments that start later.
            //
            // Therefore, if component i is short by 'needMore', we MUST add exactly that
            // many engineers now.
            long needMore = reviews[i] - activeCoverage;

            // Step 4: check the engineer budget.
            //
            // If adding these required engineers would exceed k, then this candidate k
            // is not feasible.
            usedEngineers += needMore;
            if (usedEngineers > k)
            {
                return false;
            }

            // Step 5: greedily assign those engineers to the longest possible block
            // starting at i.
            //
            // Why this greedy choice is optimal:
            // - Every one of these new engineers must cover i.
            // - Among all blocks that cover i and start no later than i, starting at i and
            //   extending as far right as possible helps the maximum number of future
            //   components.
            // - This can never hurt feasibility and is the best local choice.
            activeCoverage += needMore;

            int end = i + span;
            if (end <= n)
            {
                // Schedule removal after the block stops covering positions.
                diff[end] -= needMore;
            }
        }

        // If we processed every component without exceeding k engineers,
        // then k is feasible.
        return true;
    }
}

// Demo code
var solution = new Solution();

int[] reviews1 = { 1, 2, 2, 1 };
int span1 = 2;
long result1 = solution.MinimumReviewTeamSize(reviews1, span1);
Console.WriteLine(result1); // Expected: 3

int[] reviews2 = { 3, 0, 1, 4, 2 };
int span2 = 3;
long result2 = solution.MinimumReviewTeamSize(reviews2, span2);
Console.WriteLine(result2); // Expected: 6