"""
Title: Minimum Energy to Paint Fence Posts

Problem Description:
You are repainting a straight fence with n posts. Each post must be painted either
red, blue, or green. The cost of painting post i with a given color is provided in
a 2D array costs, where:
- costs[i][0] is the cost for red
- costs[i][1] is the cost for blue
- costs[i][2] is the cost for green

For appearance reasons, no two adjacent fence posts are allowed to have the same
color. The task is to return the minimum total energy cost needed to paint all posts
while following this rule.

This is a classic dynamic programming problem:
For each post and each color, we only need to know the minimum total cost of painting
the previous post with one of the other two colors.

Constraints:
- 1 <= n <= 1000
- costs.length == n
- costs[i].length == 3
- 1 <= costs[i][j] <= 10^4

Example 1:
Input: costs = [[1,5,3],[2,9,4]]
Output: 5
Explanation:
- Paint post 0 red: cost = 1
- Paint post 1 green: cost = 4
Total = 5

Example 2:
Input: costs = [[7,6,2],[5,8,4],[3,9,1],[6,2,7]]
Output: 10
Explanation:
One optimal painting is:
- Post 0 -> green = 2
- Post 1 -> red = 5
- Post 2 -> green = 1
- Post 3 -> blue = 2
Total = 2 + 5 + 1 + 2 = 10
"""

from typing import List


class Solution:
    def min_cost(self, costs: List[List[int]]) -> int:
        """
        Compute the minimum total cost to paint all fence posts such that
        no two adjacent posts have the same color.

        Args:
            costs: A 2D list where costs[i][0], costs[i][1], and costs[i][2]
                represent the cost of painting post i red, blue, and green.

        Returns:
            The minimum possible total painting cost.

        Time Complexity:
            O(n), where n is the number of posts, because we process each post once.

        Space Complexity:
            O(1), because we only keep track of the previous post's three DP values.
        """
        # Defensive check:
        # The problem guarantees at least one post, but handling an empty input
        # makes the method safer and more reusable.
        if not costs:
            return 0

        # Base case:
        # For the very first post, the minimum cost of ending with each color
        # is simply the direct painting cost of that color, because there is
        # no previous post to conflict with.
        #
        # prev_red   = minimum total cost to paint posts up to the current point
        #              where the latest post is painted red
        # prev_blue  = same idea, latest post painted blue
        # prev_green = same idea, latest post painted green
        prev_red: int = costs[0][0]
        prev_blue: int = costs[0][1]
        prev_green: int = costs[0][2]

        # We now process each remaining post one by one.
        # For each color choice on the current post, we must come from one of
        # the OTHER two colors on the previous post, because adjacent posts
        # cannot share the same color.
        for i in range(1, len(costs)):
            # If we paint the current post red, then the previous post must
            # have been blue or green. We choose the cheaper of those two
            # previously computed totals, then add the current red cost.
            current_red: int = costs[i][0] + min(prev_blue, prev_green)

            # If we paint the current post blue, then the previous post must
            # have been red or green.
            current_blue: int = costs[i][1] + min(prev_red, prev_green)

            # If we paint the current post green, then the previous post must
            # have been red or blue.
            current_green: int = costs[i][2] + min(prev_red, prev_blue)

            # Move the "current" results into the "previous" variables so that
            # the next loop iteration can build on them.
            #
            # This is the key space optimization:
            # We do not need a full DP table because each row depends only on
            # the immediately previous row.
            prev_red = current_red
            prev_blue = current_blue
            prev_green = current_green

        # After processing all posts, the answer is the cheapest total among
        # the three possibilities for the final post's color.
        return min(prev_red, prev_blue, prev_green)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    costs1: List[List[int]] = [[1, 5, 3], [2, 9, 4]]
    result1: int = solution.min_cost(costs1)
    print("Example 1 Output:", result1)  # Expected: 5

    # Example 2
    costs2: List[List[int]] = [[7, 6, 2], [5, 8, 4], [3, 9, 1], [6, 2, 7]]
    result2: int = solution.min_cost(costs2)
    print("Example 2 Output:", result2)  # Expected: 10

    # Additional simple test: one post only
    costs3: List[List[int]] = [[8, 3, 6]]
    result3: int = solution.min_cost(costs3)
    print("Single Post Output:", result3)  # Expected: 3