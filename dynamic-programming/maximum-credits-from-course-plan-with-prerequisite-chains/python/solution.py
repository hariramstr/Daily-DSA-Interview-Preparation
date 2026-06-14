"""
Title: Maximum Credits from Course Plan with Prerequisite Chains

Problem Description:
You are given a list of university courses numbered from 0 to n - 1. Each course i has
a credit value credits[i] and may depend on at most one prerequisite course prereq[i].
If prereq[i] = -1, then course i has no prerequisite.

A course can only be taken if its prerequisite, and that course's prerequisite, and so on,
have all been taken. In other words, selecting a course requires selecting the entire chain
leading to it.

You are also given an integer maxCourses representing the maximum number of courses a student
can take this semester. Your task is to return the maximum total credits the student can earn
by choosing at most maxCourses courses while satisfying all prerequisite rules.

The prerequisite graph is guaranteed to contain no cycles. Because each course has at most one
prerequisite, the graph forms a collection of rooted chains and trees. A valid selection must
be closed under prerequisites: if a course is chosen, every ancestor on its prerequisite path
must also be chosen.

Design an algorithm that computes the best achievable total credits.

Typical approach:
Use tree-based dynamic programming with a knapsack-style merge over subtrees, where
dp[u][k] represents the maximum credits obtainable by selecting exactly k courses from the
subtree rooted at u while respecting prerequisite closure.
"""

from typing import List


class Solution:
    def max_credits(self, credits: List[int], prereq: List[int], maxCourses: int) -> int:
        """
        Compute the maximum total credits obtainable by selecting at most maxCourses courses
        while satisfying prerequisite closure.

        The prerequisite structure is a forest. We attach all roots to a virtual super-root,
        then run tree DP. For each node u:
        - dp[u][k] = maximum credits from subtree of u using exactly k selected courses
          under the rule that if any course in u's subtree is selected, then u must also
          be selected (except for the virtual root, which contributes 0 and is always present).

        Args:
            credits: credits[i] is the credit value of course i.
            prereq: prereq[i] is the prerequisite of course i, or -1 if none.
            maxCourses: maximum number of courses allowed.

        Returns:
            The maximum total credits achievable.

        Time complexity:
            O(n * maxCourses^2) in the worst case due to knapsack-style subtree merges.

        Space complexity:
            O(n * maxCourses) for DP storage.
        """
        n: int = len(credits)

        # Build children lists for the prerequisite forest.
        # Since each node has at most one parent, this is naturally a rooted forest.
        children: List[List[int]] = [[] for _ in range(n + 1)]
        virtual_root: int = n

        # Every real root (course with no prerequisite) becomes a child of the virtual root.
        # This lets us solve the whole forest as one tree.
        for course in range(n):
            parent = prereq[course]
            if parent == -1:
                children[virtual_root].append(course)
            else:
                children[parent].append(course)

        # subtree_size[u] will store how many real courses exist in the subtree rooted at u.
        # For the virtual root, this will become n.
        subtree_size: List[int] = [0] * (n + 1)

        # NEG_INF is used for impossible DP states.
        NEG_INF: int = -10**18

        def dfs(u: int) -> List[int]:
            """
            Perform a postorder DFS and compute DP for subtree rooted at u.

            For a real course node u:
            - dp[0] is impossible, because selecting anything from this subtree while not
              selecting u would violate prerequisite closure for descendants.
            - dp[1] starts as credits[u], meaning we select only u.
            - Then we merge each child's DP, deciding how many courses to take from that child.

            For the virtual root:
            - dp[0] = 0, meaning we may choose nothing from the entire forest.
            - The virtual root itself is not a real course and contributes no credits.
            - Its children are independent root trees, so we merge them like a standard forest knapsack.

            Args:
                u: current node index. Real courses are 0..n-1, virtual root is n.

            Returns:
                A DP array where dp[k] is the best credits using exactly k real courses
                from u's subtree under valid prerequisite closure.

            Time complexity:
                O(size_of_subtree * maxCourses^2) across all merges in the subtree.

            Space complexity:
                O(maxCourses) for the returned DP array, plus recursion stack.
            """
            if u == virtual_root:
                # For the virtual root, selecting 0 courses is valid and yields 0 credits.
                dp: List[int] = [NEG_INF] * (maxCourses + 1)
                dp[0] = 0
                current_size = 0
            else:
                # For a real node:
                # - selecting 0 courses from its subtree is represented as impossible in this DP,
                #   because this DP is defined around the idea that if we use this subtree,
                #   we must include the root node u.
                # - selecting exactly 1 course means taking only u.
                dp = [NEG_INF] * (maxCourses + 1)
                dp[1] = credits[u]
                current_size = 1

            # Process each child one by one and merge its possibilities into the current DP.
            for v in children[u]:
                child_dp = dfs(v)
                child_size = subtree_size[v]

                # new_dp will hold the merged result after considering child v.
                # We only need to consider up to maxCourses total selections.
                new_limit = min(maxCourses, current_size + child_size)
                new_dp: List[int] = [NEG_INF] * (maxCourses + 1)

                # Try all ways to split the total selected courses between:
                # - what we already selected from previously processed parts
                # - what we now select from child v
                #
                # Important detail:
                # For child subtrees, taking 0 courses is always allowed during merge,
                # meaning "ignore this child entirely".
                for already_taken in range(current_size + 1):
                    if dp[already_taken] == NEG_INF:
                        continue

                    # Option 1: take 0 courses from this child subtree.
                    # This is always valid because we can simply skip the child branch.
                    if dp[already_taken] > new_dp[already_taken]:
                        new_dp[already_taken] = dp[already_taken]

                    # Option 2: take t >= 1 courses from the child subtree.
                    # child_dp[t] already guarantees that if any descendant is taken,
                    # the child itself is also taken, so prerequisite closure is preserved.
                    max_take_from_child = min(child_size, maxCourses - already_taken)
                    for take_from_child in range(1, max_take_from_child + 1):
                        if child_dp[take_from_child] == NEG_INF:
                            continue
                        candidate = dp[already_taken] + child_dp[take_from_child]
                        total_taken = already_taken + take_from_child
                        if candidate > new_dp[total_taken]:
                            new_dp[total_taken] = candidate

                dp = new_dp
                current_size = new_limit

            # Store subtree size in terms of real courses.
            if u == virtual_root:
                subtree_size[u] = sum(subtree_size[v] for v in children[u])
            else:
                subtree_size[u] = 1 + sum(subtree_size[v] for v in children[u])

            return dp

        # Run DP from the virtual root to cover the entire forest.
        final_dp = dfs(virtual_root)

        # We are allowed to take AT MOST maxCourses courses, not necessarily exactly maxCourses.
        # Therefore the answer is the maximum over all valid counts 0..maxCourses.
        return max(final_dp)

    def maximumCredits(self, credits: List[int], prereq: List[int], maxCourses: int) -> int:
        """
        Wrapper method using a camelCase name for convenience.

        Args:
            credits: credits[i] is the credit value of course i.
            prereq: prereq[i] is the prerequisite of course i, or -1 if none.
            maxCourses: maximum number of courses allowed.

        Returns:
            The maximum total credits achievable.

        Time complexity:
            O(n * maxCourses^2)

        Space complexity:
            O(n * maxCourses)
        """
        return self.max_credits(credits, prereq, maxCourses)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    credits1 = [3, 5, 4, 8]
    prereq1 = [-1, 0, 1, -1]
    max_courses1 = 3
    result1 = solution.maximumCredits(credits1, prereq1, max_courses1)
    print("Example 1 Result:", result1)  # Expected: 16

    # Example 2
    credits2 = [2, 7, 6, 4, 5]
    prereq2 = [-1, 0, 0, 1, 1]
    max_courses2 = 3
    result2 = solution.maximumCredits(credits2, prereq2, max_courses2)
    print("Example 2 Result:", result2)  # Expected: 15

    # Additional quick sanity checks
    credits3 = [10]
    prereq3 = [-1]
    max_courses3 = 1
    result3 = solution.maximumCredits(credits3, prereq3, max_courses3)
    print("Single Course Result:", result3)  # Expected: 10

    credits4 = [5, 100, 6]
    prereq4 = [-1, 0, -1]
    max_courses4 = 1
    result4 = solution.maximumCredits(credits4, prereq4, max_courses4)
    print("At Most One Course Result:", result4)  # Expected: 6, because course 1 requires course 0