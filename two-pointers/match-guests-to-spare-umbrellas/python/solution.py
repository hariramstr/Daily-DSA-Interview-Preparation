"""
Title: Match Guests to Spare Umbrellas

Problem Description:
You are organizing an outdoor event. Some guests arrived without umbrellas, and you
have a collection of spare umbrellas of different sizes. Each guest has a minimum
umbrella size they are willing to use, and each spare umbrella has a fixed size.

A guest can be assigned at most one umbrella, and each umbrella can be given to at
most one guest. An umbrella can be assigned to a guest only if the umbrella size is
greater than or equal to that guest's minimum required size.

Return the maximum number of guests who can receive a suitable umbrella.

This problem is about making as many valid assignments as possible, not about
minimizing waste. A simple and efficient strategy is to sort both lists and use two
pointers to scan through them. If the current smallest available umbrella can satisfy
the current guest, assign it and move both pointers. Otherwise, skip that umbrella
and try a larger one.

Constraints:
- 1 <= guests.length, umbrellas.length <= 100000
- 1 <= guests[i], umbrellas[i] <= 1000000000
- guests[i] is the minimum size required by the i-th guest
- umbrellas[i] is the size of the i-th spare umbrella

Example 1:
Input: guests = [2, 3, 1], umbrellas = [1, 1, 3]
Output: 2

Example 2:
Input: guests = [4, 2, 5], umbrellas = [2, 3, 4, 6]
Output: 3
"""

from typing import List


class Solution:
    def max_guests_with_umbrellas(self, guests: List[int], umbrellas: List[int]) -> int:
        """
        Compute the maximum number of guests who can receive a suitable umbrella.

        The method sorts both input lists and then uses two pointers:
        one pointer walks through the guests in increasing order of required size,
        and the other pointer walks through the umbrellas in increasing order of size.

        By always trying to satisfy the current guest with the smallest umbrella
        that can work, we preserve larger umbrellas for guests who may need them later.

        Args:
            guests: A list where each value is the minimum umbrella size required by a guest.
            umbrellas: A list where each value is the size of an available spare umbrella.

        Returns:
            The maximum number of valid guest-to-umbrella assignments.

        Time complexity:
            O(n log n + m log m), where n is len(guests) and m is len(umbrellas),
            due to sorting both lists. The two-pointer scan itself is O(n + m).

        Space complexity:
            O(n + m) in the worst case if sorted copies are created.
        """
        # We sort the guests by their minimum required umbrella size.
        # Why?
        # Because handling the smallest requirements first makes greedy matching possible.
        # If a small umbrella can satisfy a small-requirement guest, we should use it there
        # instead of wasting a larger umbrella that might be needed later.
        sorted_guests: List[int] = sorted(guests)

        # We also sort the umbrellas from smallest to largest.
        # This allows us to scan through available umbrella sizes in order and decide
        # whether each one can satisfy the current guest.
        sorted_umbrellas: List[int] = sorted(umbrellas)

        # guest_index points to the current guest we are trying to satisfy.
        # We start with the guest who has the smallest requirement.
        guest_index: int = 0

        # umbrella_index points to the current umbrella we are considering.
        # We start with the smallest umbrella available.
        umbrella_index: int = 0

        # This counter tracks how many successful assignments we have made.
        matches: int = 0

        # Continue while there are still guests left to consider
        # and umbrellas left to try.
        #
        # The loop ends as soon as one list is exhausted:
        # - If we run out of guests, we are done.
        # - If we run out of umbrellas, no more assignments are possible.
        while guest_index < len(sorted_guests) and umbrella_index < len(sorted_umbrellas):
            # Read the current guest's minimum required size.
            current_guest_requirement: int = sorted_guests[guest_index]

            # Read the current umbrella's size.
            current_umbrella_size: int = sorted_umbrellas[umbrella_index]

            # Case 1:
            # If the current umbrella is large enough for the current guest,
            # we should assign it immediately.
            #
            # Why is this greedy choice correct?
            # Because this is the smallest umbrella that can satisfy this guest
            # among the umbrellas we have not skipped. Using it now leaves larger
            # umbrellas available for guests with larger requirements.
            if current_umbrella_size >= current_guest_requirement:
                # We have found one valid assignment.
                matches += 1

                # Move to the next guest, because the current guest has now been served.
                guest_index += 1

                # Move to the next umbrella, because each umbrella can be used at most once.
                umbrella_index += 1
            else:
                # Case 2:
                # The current umbrella is too small for the current guest.
                #
                # Since both lists are sorted, this umbrella will also be too small
                # for every later guest, because later guests have equal or larger
                # requirements than the current guest.
                #
                # Therefore, this umbrella can never be used in any valid future match,
                # so the best action is to skip it and try the next larger umbrella.
                umbrella_index += 1

        # After the loop finishes, matches contains the maximum number of guests
        # that can receive suitable umbrellas.
        return matches


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    guests1: List[int] = [2, 3, 1]
    umbrellas1: List[int] = [1, 1, 3]
    result1: int = solution.max_guests_with_umbrellas(guests1, umbrellas1)
    print("Example 1 Result:", result1)  # Expected: 2

    # Example 2
    guests2: List[int] = [4, 2, 5]
    umbrellas2: List[int] = [2, 3, 4, 6]
    result2: int = solution.max_guests_with_umbrellas(guests2, umbrellas2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick check
    guests3: List[int] = [5, 5, 5]
    umbrellas3: List[int] = [1, 5, 6]
    result3: int = solution.max_guests_with_umbrellas(guests3, umbrellas3)
    print("Additional Example Result:", result3)  # Expected: 2