/*
Title: Maximum Credits from Course Plan with Prerequisite Chains
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
You are given a list of university courses numbered from 0 to n - 1. Each course i has a credit value credits[i] and may depend on at most one prerequisite course prereq[i]. If prereq[i] = -1, then course i has no prerequisite. A course can only be taken if its prerequisite, and that course's prerequisite, and so on, have all been taken. In other words, selecting a course requires selecting the entire chain leading to it.

You are also given an integer maxCourses representing the maximum number of courses a student can take this semester. Your task is to return the maximum total credits the student can earn by choosing at most maxCourses courses while satisfying all prerequisite rules.

The prerequisite graph is guaranteed to contain no cycles. Because each course has at most one prerequisite, the graph forms a collection of rooted chains and trees. A valid selection must be closed under prerequisites: if a course is chosen, every ancestor on its prerequisite path must also be chosen.

Design an algorithm that computes the best achievable total credits.

Constraints:
- 1 <= n <= 1000
- 1 <= credits[i] <= 10^4
- -1 <= prereq[i] < n
- prereq[i] != i
- The prerequisite graph is acyclic
- 1 <= maxCourses <= n

Example 1:
Input: credits = [3, 5, 4, 8], prereq = [-1, 0, 1, -1], maxCourses = 3
Output: 16
Explanation:
Course 2 requires course 1, which requires course 0.
Valid selections of up to 3 courses include:
- [3] => 8
- [0, 1, 2] => 12
- [0, 1, 3] => 16
The best valid selection is [0, 1, 3] with total 16 credits.

Example 2:
Input: credits = [2, 7, 6, 4, 5], prereq = [-1, 0, 0, 1, 1], maxCourses = 3
Output: 15
Explanation:
One optimal choice is [0, 1, 2], which gives 2 + 7 + 6 = 15.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Time Complexity:
    // O(n * maxCourses^2) in the worst case due to tree knapsack merges.
    //
    // Why:
    // - We build a forest from the prerequisite array.
    // - We run a DFS-based dynamic programming on each node.
    // - During each merge of a child subtree into its parent, we try combinations
    //   of "how many courses already chosen" and "how many courses taken from child".
    // - Across the whole tree/forest, this is the standard tree-knapsack complexity.
    //
    // Space Complexity:
    // O(n * maxCourses)
    //
    // Why:
    // - Each node stores a DP array of size up to maxCourses + 1.
    // - We also store the adjacency list for the forest.
    public int MaxCredits(int[] credits, int[] prereq, int maxCourses)
    {
        int n = credits.Length;

        // Step 1: Build the forest of prerequisite relationships.
        //
        // Because each course has at most one prerequisite, every node has at most one parent.
        // That means the graph is a forest (a collection of rooted trees), since the input is acyclic.
        //
        // We store children for each node:
        // if prereq[i] = p, then p -> i is a tree edge.
        //
        // Roots are courses with no prerequisite (prereq[i] == -1).
        var children = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            children[i] = new List<int>();
        }

        var roots = new List<int>();
        for (int i = 0; i < n; i++)
        {
            if (prereq[i] == -1)
            {
                roots.Add(i);
            }
            else
            {
                children[prereq[i]].Add(i);
            }
        }

        // We use a large negative number to represent "impossible".
        // We cannot use int.MinValue directly because later we add credits to it,
        // which could overflow. So we choose a safely small value.
        const int NEG = -1_000_000_000;

        // We will solve the forest by introducing a "virtual super root".
        //
        // Why this is useful:
        // - The original graph may have multiple roots.
        // - A tree DP is easiest when there is exactly one root.
        // - The super root has credit 0 and all original roots as children.
        // - We are allowed to choose 0 courses from the super root's subtree,
        //   and any chosen real course must still satisfy prerequisite closure.
        //
        // Important interpretation:
        // - For a real node u:
        //   If we choose anything from u's subtree, we must choose u itself first.
        // - For the super root:
        //   We do NOT count it as a course, and we may choose any combination
        //   of courses from its children subtrees.
        //
        // To implement this cleanly, we use two DFS methods:
        // - DfsReal(u): DP for a real course node u
        // - Then we merge all roots into a global knapsack as if attached to a super root

        // globalDp[k] = maximum credits obtainable by selecting exactly k courses
        // from all processed root trees so far.
        var globalDp = new int[maxCourses + 1];
        Array.Fill(globalDp, NEG);
        globalDp[0] = 0;

        // Process each root tree independently, then merge into the global answer.
        foreach (int root in roots)
        {
            var (rootDp, rootSize) = DfsReal(root, children, credits, maxCourses, NEG);

            // Merge this root tree into the global forest DP.
            //
            // globalDp currently represents the best answer using previously processed roots.
            // rootDp[t] represents the best answer using exactly t courses from this root's subtree,
            // with prerequisite closure enforced inside that subtree.
            //
            // We combine them like a standard knapsack convolution.
            var next = new int[maxCourses + 1];
            Array.Fill(next, NEG);

            int maxAlready = maxCourses;
            int maxTakeFromRoot = Math.Min(rootSize, maxCourses);

            for (int used = 0; used <= maxAlready; used++)
            {
                if (globalDp[used] == NEG) continue;

                for (int take = 0; take + used <= maxCourses && take <= maxTakeFromRoot; take++)
                {
                    // Special case:
                    // rootDp[0] is impossible for a real node, because taking 0 courses
                    // from a real node's subtree while "using that subtree DP" is not valid
                    // in the real-node definition.
                    //
                    // But when merging a root tree into the forest, taking 0 from this tree
                    // should absolutely be allowed.
                    //
                    // So:
                    // - if take == 0, contribution is 0
                    // - otherwise, contribution is rootDp[take]
                    int add = (take == 0) ? 0 : rootDp[take];
                    if (take > 0 && add == NEG) continue;

                    next[used + take] = Math.Max(next[used + take], globalDp[used] + add);
                }
            }

            globalDp = next;
        }

        // We are allowed to take AT MOST maxCourses, not necessarily exactly maxCourses.
        // So the final answer is the maximum over all k from 0..maxCourses.
        int answer = 0;
        for (int k = 0; k <= maxCourses; k++)
        {
            answer = Math.Max(answer, globalDp[k]);
        }

        return answer;
    }

    private (int[] dp, int size) DfsReal(
        int u,
        List<int>[] children,
        int[] credits,
        int maxCourses,
        int NEG)
    {
        // This DFS computes DP for a REAL course node u.
        //
        // Meaning of dp[k]:
        // dp[k] = maximum credits obtainable by selecting exactly k courses
        //         from the subtree rooted at u,
        //         under the rule that if we select any course in this subtree,
        //         we must include all prerequisites.
        //
        // Since u is the root of this subtree:
        // - If we take anything from this subtree, we must take u.
        // - Therefore dp[0] is impossible for a real node.
        // - The smallest valid choice is taking only u, so dp[1] = credits[u].
        //
        // We then merge each child subtree one by one.
        //
        // Why this works:
        // - A child course depends on u, so once u is selected, we may optionally
        //   take a valid closed set from that child's subtree.
        // - Each child subtree is independent from the others except for the shared
        //   course-count budget, so a knapsack merge is the correct tool.

        var dp = new int[maxCourses + 1];
        Array.Fill(dp, NEG);

        // Base case for a real node:
        // Taking exactly 1 course by selecting only u.
        dp[1] = credits[u];

        // currentSize tracks how many courses from this subtree can possibly be used
        // after processing some children.
        int currentSize = 1;

        foreach (int v in children[u])
        {
            // Recursively solve the child subtree first.
            var (childDp, childSize) = DfsReal(v, children, credits, maxCourses, NEG);

            // next will store the merged result after considering child v.
            var next = new int[maxCourses + 1];
            Array.Fill(next, NEG);

            // We can choose:
            // - 0 courses from child v's subtree, meaning we ignore that child entirely
            // - or t >= 1 courses from child v's subtree, where childDp[t] is valid
            //
            // Since child v depends on u, and u is already included in every valid dp state
            // for this real node, taking from child is allowed.
            int maxUsedHere = Math.Min(currentSize, maxCourses);
            int maxTakeFromChild = Math.Min(childSize, maxCourses);

            for (int used = 1; used <= maxUsedHere; used++)
            {
                if (dp[used] == NEG) continue;

                // Option 1: take 0 courses from this child subtree.
                next[used] = Math.Max(next[used], dp[used]);

                // Option 2: take t >= 1 courses from this child subtree.
                for (int take = 1; take <= maxTakeFromChild && used + take <= maxCourses; take++)
                {
                    if (childDp[take] == NEG) continue;

                    next[used + take] = Math.Max(
                        next[used + take],
                        dp[used] + childDp[take]
                    );
                }
            }

            dp = next;
            currentSize = Math.Min(maxCourses, currentSize + childSize);
        }

        return (dp, currentSize);
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] credits1 = { 3, 5, 4, 8 };
int[] prereq1 = { -1, 0, 1, -1 };
int maxCourses1 = 3;
int result1 = solution.MaxCredits(credits1, prereq1, maxCourses1);
Console.WriteLine(result1); // Expected: 16

// Example 2
int[] credits2 = { 2, 7, 6, 4, 5 };
int[] prereq2 = { -1, 0, 0, 1, 1 };
int maxCourses2 = 3;
int result2 = solution.MaxCredits(credits2, prereq2, maxCourses2);
Console.WriteLine(result2); // Expected: 15

// Additional quick sanity check
int[] credits3 = { 10 };
int[] prereq3 = { -1 };
int maxCourses3 = 1;
int result3 = solution.MaxCredits(credits3, prereq3, maxCourses3);
Console.WriteLine(result3); // Expected: 10