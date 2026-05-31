```python
"""
Title: Find All Symmetric Pairs in a Contact List
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given a list of n contact pairs where each pair [caller, receiver]
represents a phone call from caller to receiver. A symmetric pair exists when
both [A, B] and [B, A] appear in the list. Your task is to return all unique
symmetric pairs found in the contact list.

A pair [A, B] and its symmetric counterpart [B, A] should only be reported
once in the output (report the version where the smaller value comes first).
If the same pair appears multiple times in the input, it should still only
generate one symmetric result.

Constraints:
- 1 <= n <= 10^5
- 1 <= caller, receiver <= 10^6
- caller != receiver
- The input list may contain duplicate pairs.
"""

from typing import List, Dict, Set, Tuple


class Solution:
    def find_symmetric_pairs(self, contacts: List[List[int]]) -> List[List[int]]:
        """
        Find all unique symmetric pairs in a contact list.

        A symmetric pair is when both [A, B] and [B, A] exist in the list.
        Each symmetric pair is reported only once, with the smaller value first.

        Args:
            contacts: A list of [caller, receiver] pairs representing phone calls.

        Returns:
            A list of unique symmetric pairs, each reported with the smaller
            value first.

        Time Complexity: O(n) - We iterate through the contacts list once,
                         and all hash set/dict operations are O(1) average.
        Space Complexity: O(n) - We store up to n unique pairs in our hash set.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Create a set to store all unique pairs we've seen.
        # We use a set of tuples because:
        #   - Sets give O(1) average lookup time (hash-based)
        #   - Tuples are hashable (unlike lists), so they can be stored in sets
        #   - Using a set automatically handles duplicate pairs in the input
        # -----------------------------------------------------------------------
        seen_pairs: Set[Tuple[int, int]] = set()

        # -----------------------------------------------------------------------
        # STEP 2: Create a set to store the results.
        # We use a set here to avoid adding the same symmetric pair multiple times.
        # For example, if [1,2] and [2,1] both appear multiple times, we only
        # want to report [1,2] once.
        # We'll store results as tuples (min, max) to ensure consistent ordering.
        # -----------------------------------------------------------------------
        result_set: Set[Tuple[int, int]] = set()

        # -----------------------------------------------------------------------
        # STEP 3: Iterate through each contact pair in the input list.
        # For each pair [caller, receiver], we check if its reverse [receiver, caller]
        # has already been seen. If yes, we have found a symmetric pair.
        # -----------------------------------------------------------------------
        for pair in contacts:
            # Extract the caller and receiver from the current pair
            caller: int = pair[0]
            receiver: int = pair[1]

            # -------------------------------------------------------------------
            # STEP 3a: Check if the reverse of the current pair has been seen.
            # The reverse of [caller, receiver] is [receiver, caller].
            # We look up (receiver, caller) in our seen_pairs set.
            # This lookup is O(1) average due to hashing.
            # -------------------------------------------------------------------
            reverse_pair: Tuple[int, int] = (receiver, caller)

            if reverse_pair in seen_pairs:
                # ---------------------------------------------------------------
                # STEP 3b: We found a symmetric pair!
                # To report it consistently (smaller value first), we use:
                #   min(caller, receiver) as the first element
                #   max(caller, receiver) as the second element
                # We add this normalized tuple to result_set.
                # Using a set ensures we don't add duplicates even if the same
                # symmetric relationship is discovered multiple times.
                # ---------------------------------------------------------------
                normalized: Tuple[int, int] = (min(caller, receiver), max(caller, receiver))
                result_set.add(normalized)

            # -------------------------------------------------------------------
            # STEP 3c: Add the current pair (as a tuple) to seen_pairs.
            # We do this AFTER the check above so that a pair like [1, 1]
            # (which is excluded by constraints, but just for safety) doesn't
            # match itself. More importantly, we record this pair so future
            # pairs can check against it.
            # Note: Since seen_pairs is a set, adding a duplicate pair has no effect,
            # which correctly handles duplicate entries in the input.
            # -------------------------------------------------------------------
            seen_pairs.add((caller, receiver))

        # -----------------------------------------------------------------------
        # STEP 4: Convert the result set to a list of lists for the final output.
        # We convert each tuple (a, b) back to a list [a, b] as required by
        # the problem's output format.
        # -----------------------------------------------------------------------
        result: List[List[int]] = [list(pair) for pair in result_set]

        return result


# ---------------------------------------------------------------------------
# TRACE THROUGH EXAMPLES TO VERIFY CORRECTNESS:
#
# Example 1: contacts = [[1,2],[3,4],[2,1],[5,6],[4,3],[7,8]]
#
# Iteration 1: pair=[1,2], caller=1, receiver=2
#   - reverse=(2,1), not in seen_pairs={}
#   - seen_pairs={(1,2)}
#
# Iteration 2: pair=[3,4], caller=3, receiver=4
#   - reverse=(4,3), not in seen_pairs={(1,2)}
#   - seen_pairs={(1,2),(3,4)}
#
# Iteration 3: pair=[2,1], caller=2, receiver=1
#   - reverse=(1,2), IS in seen_pairs={(1,2),(3,4)} → SYMMETRIC FOUND!
#   - normalized=(min(2,1), max(2,1))=(1,2)
#   - result_set={(1,2)}
#   - seen_pairs={(1,2),(3,4),(2,1)}
#
# Iteration 4: pair=[5,6], caller=5, receiver=6
#   - reverse=(6,5), not in seen_pairs
#   - seen_pairs={(1,2),(3,4),(2,1),(5,6)}
#
# Iteration 5: pair=[4,3], caller=4, receiver=3
#   - reverse=(3,4), IS in seen_pairs → SYMMETRIC FOUND!
#   - normalized=(min(4,3), max(4,3))=(3,4)
#   - result_set={(1,2),(3,4)}
#   - seen_pairs={(1,2),(3,4),(2,1),(5,6),(4,3)}
#
# Iteration 6: pair=[7,8], caller=7, receiver=8
#   - reverse=(8,7), not in seen_pairs
#   - seen_pairs updated
#
# Final result: [[1,2],[3,4]] ✓ (order may vary, but content matches)
#
# Example 2: contacts = [[10,20],[20,10],[10,20],[30,40]]
#
# Iteration 1: pair=[10,20]
#   - reverse=(20,10), not in seen_pairs={}
#   - seen_pairs={(10,20)}
#
# Iteration 2: pair=[20,10]
#   - reverse=(10,20), IS in seen_pairs → SYMMETRIC FOUND!
#   - normalized=(10,20)
#   - result_set={(10,20)}
#   - seen_pairs={(10,20),(20,10)}
#
# Iteration 3: pair=[10,20]
#   - reverse=(20,10), IS in seen_pairs → SYMMETRIC FOUND!
#   - normalized=(10,20) → already in result_set, no duplicate added
#   - seen_pairs={(10,20),(20,10)} (no change, already present)
#
# Iteration 4: pair=[30,40]
#   - reverse=(40,30), not in seen_pairs
#   - seen_pairs updated
#
# Final result: [[10,20]] ✓
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    # Create a Solution instance
    solution = Solution()

    # -----------------------------------------------------------------------
    # Test Case 1: Basic example with two symmetric pairs
    # Expected Output: [[1, 2], [3, 4]] (order may vary)
    # -----------------------------------------------------------------------
    contacts1 = [[1, 2], [3, 4], [2, 1], [5, 6], [4, 3], [7, 8]]
    result1 = solution.find_symmetric_pairs(contacts1)
    print("Test Case 1:")
    print(f"  Input:    {contacts1}")
    print(f"  Output:   {sorted(result1)}")
    print(f"  Expected: [[1, 2], [3, 4]]")
    print()

    # -----------------------------------------------------------------------
    # Test Case 2: Duplicate pairs in input — should still report only once
    # Expected Output: [[10, 20]]
    # -----------------------------------------------------------------------
    contacts2 = [[10, 20], [20, 10], [10, 20], [30, 40]]
    result2 = solution.find_symmetric_pairs(contacts2)
    print("Test Case 2:")
    print(f"  Input:    {contacts2}")
    print(f"  Output:   {sorted(result2)}")
    print(f"  Expected: [[10, 20]]")
    print()

    # -----------------------------------------------------------------------
    # Test Case 3: No symmetric pairs
    # Expected Output: []
    # -----------------------------------------------------------------------
    contacts3 = [[1, 2], [3, 4], [5, 6]]
    result3 = solution.find_symmetric_pairs(contacts3)
    print("Test Case 3:")
    print(f"  Input:    {contacts3}")
    print(f"  Output:   {sorted(result3)}")
    print(f"  Expected: []")
    print()

    # -----------------------------------------------------------------------
    # Test Case 4: All pairs are symmetric
    # Expected Output: [[1, 2], [3, 4], [5, 6]]
    # -----------------------------------------------------------------------
    contacts4 = [[1, 2], [2, 1], [3, 4], [4, 3], [5, 6], [6, 5]]
    result4 = solution.find_symmetric_pairs(contacts4)
    print("Test Case 4:")
    print(f"  Input:    {contacts4}")
    print(f"  Output:   {sorted(result4)}")
    print(f"  Expected: [[1, 2], [3, 4], [5, 6]]")
    print()

    # -----------------------------------------------------------------------
    # Test Case 5: Single pair — no symmetric possible
    # Expected Output: []
    # -----------------------------------------------------------------------
    contacts5 = [[100, 200]]
    result5 = solution.find_symmetric_pairs(contacts5)
    print("Test Case 5:")
    print(f"  Input:    {contacts5}")
    print(f"  Output:   {sorted(result5)}")
    print(f"  Expected: []")
    print()

    # -----------------------------------------------------------------------
    # Test Case 6: Multiple duplicates of symmetric pairs
    # Expected Output: [[5, 10]]
    # -----------------------------------------------------------------------
    contacts6 = [[5, 10], [10, 5], [5, 10], [10, 5], [5, 10]]
    result6 = solution.find_symmetric_pairs(contacts6)
    print("Test Case 6:")
    print(f"  Input:    {contacts6}")
    print(f"  Output:   {sorted(result6)}")
    print(f"  Expected: [[5, 10]]")
    print()
```