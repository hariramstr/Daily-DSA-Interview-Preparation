"""
Title: Visible Customers After Each Line Update

Problem Description:
A store manager tracks the heights of customers standing in a single checkout line
from front to back. For staffing analysis, the manager wants to know, for each
customer, how many customers in front of them are visible.

A customer can see another customer in front if every customer standing between
them is strictly shorter than both of those two customers. If a taller or equal-height
customer appears first, visibility stops there, but that blocking customer is still visible.

Given an array heights where heights[i] is the height of the i-th customer in line
(0-indexed, from front to back), return an array answer of the same length where
answer[i] is the number of customers in front of customer i that are visible to them.

You should design an efficient solution using stack-based processing rather than
checking every pair directly.

Constraints:
- 1 <= heights.length <= 200000
- 1 <= heights[i] <= 1000000000
- The answer for each position fits in a 32-bit integer

Example 1:
Input: heights = [10,6,8,5,11,9]
Output: [0,1,2,1,4,1]

Example 2:
Input: heights = [5,5,4,7,6]
Output: [0,1,1,3,1]
"""

from typing import List


class Solution:
    def canSeePersonsCount(self, heights: List[int]) -> List[int]:
        """
        Compute how many customers in front are visible for each customer.

        We process the line from front to back while maintaining a monotonic stack
        of groups. Each group stores:
        - a height
        - how many consecutive customers of that exact height are currently active
        - for the nearest customer of that height, how many visible people they had
          among the active structure before this height group was added

        This lets us correctly handle:
        - shorter people that are directly visible and then popped
        - equal-height blocking behavior
        - taller blocking behavior
        - chains of equal heights without double counting

        Args:
            heights: List of customer heights from front to back.

        Returns:
            A list where answer[i] is the number of customers in front of customer i
            that are visible.

        Time complexity:
            O(n), because each height is pushed and popped at most once.

        Space complexity:
            O(n), for the stack and output array.
        """
        n: int = len(heights)

        # This will store the final answer for each customer.
        answer: List[int] = [0] * n

        # Each stack entry is a list of three integers:
        # [height, count_of_same_height, visible_before_group]
        #
        # Meaning:
        # - height: the height represented by this group
        # - count_of_same_height: how many consecutive active customers of this exact
        #   height are represented together
        # - visible_before_group: for the nearest customer in this group, how many
        #   customers were visible before this group's first member was added
        #
        # Why group equal heights?
        # Equal heights are special because:
        # - the nearest equal-height customer is visible
        # - that equal-height customer blocks all further view
        # Grouping lets us handle this efficiently and correctly.
        stack: List[List[int]] = []

        # We scan from front to back because each customer only looks toward earlier
        # customers. At step i, the stack summarizes the "visible skyline" formed by
        # customers 0..i-1.
        for i, current_height in enumerate(heights):
            # This variable will accumulate how many people the current customer can see.
            visible_count: int = 0

            # Step 1:
            # Remove all strictly shorter height groups from the top of the stack.
            #
            # Why can the current customer see them?
            # Because they are on top of the active structure, so there is no taller or
            # equal person between the current customer and those shorter visible peaks.
            #
            # Why do we pop them?
            # Because once a taller current customer arrives, those shorter groups can no
            # longer block future customers behind the current one. The current customer
            # dominates them.
            #
            # For each popped group, the current customer sees exactly one representative
            # customer from that group chain as exposed by the monotonic structure.
            while stack and stack[-1][0] < current_height:
                visible_count += 1
                stack.pop()

            # Step 2:
            # After removing all shorter groups, there are three possibilities:
            #
            # A) Stack is empty:
            #    Nobody remains in front that can block or be additionally visible.
            #
            # B) Top group has the same height:
            #    The nearest equal-height customer is visible, and then visibility stops.
            #    However, because equal heights may have been grouped, we need a careful
            #    formula to recover the correct count for the current customer.
            #
            # C) Top group is taller:
            #    That nearest taller customer is visible, and visibility stops there.
            if stack:
                top_height, top_count, top_visible_before = stack[-1]

                if top_height == current_height:
                    # Equal-height case:
                    #
                    # The current customer can see:
                    # 1. The nearest equal-height customer itself.
                    # 2. Every shorter "peak" that was visible to that nearest equal-height
                    #    customer before the equal-height group started.
                    #
                    # Why does this work?
                    # Consider a chain like [10, 6, 8, 5, 11, 9] processed from front to back.
                    # When another 5 or 8 appears, the nearest equal height blocks further
                    # view, but whatever that nearest equal-height customer could see before
                    # its own equal-height group started is exactly the set still visible
                    # behind it until the equal-height block.
                    #
                    # So:
                    # current sees 1 (the nearest equal-height person)
                    # plus the "visible_before_group" stored for that equal-height group.
                    visible_count += 1 + top_visible_before

                    # We merge the current customer into the existing equal-height group.
                    # The group's stored visible_before_group does not change, because it
                    # describes the visibility context before the first member of the group.
                    stack[-1][1] += 1
                else:
                    # Taller blocking case:
                    #
                    # The nearest taller customer is visible, and then visibility stops.
                    visible_count += 1

                    # Push the current customer as a new group.
                    #
                    # The value "visible_before_group" for a new group should represent how
                    # many visible peaks existed before this group was added, excluding the
                    # group itself. For a fresh group, that is exactly:
                    # visible_count - 1
                    #
                    # Why subtract 1?
                    # Because visible_count currently includes the nearest blocking taller
                    # customer, but visible_before_group should describe what was visible
                    # before this group's own first member was inserted.
                    stack.append([current_height, 1, visible_count - 1])
            else:
                # No one is in front after popping shorter groups, so the current customer
                # sees nobody else beyond what was already counted from popped groups.
                #
                # Since the stack is empty here, visible_count is exactly the number of
                # shorter groups popped. We now create a new group for current_height.
                #
                # Because there was nobody left before this group, visible_before_group is 0.
                stack.append([current_height, 1, 0])

            # Store the computed result for this customer.
            answer[i] = visible_count

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [10, 6, 8, 5, 11, 9]
    result_1: List[int] = solution.canSeePersonsCount(sample_1)
    print("Input:", sample_1)
    print("Output:", result_1)
    print("Expected:", [0, 1, 2, 1, 4, 1])
    print()

    sample_2: List[int] = [5, 5, 4, 7, 6]
    result_2: List[int] = solution.canSeePersonsCount(sample_2)
    print("Input:", sample_2)
    print("Output:", result_2)
    print("Expected:", [0, 1, 1, 3, 1])