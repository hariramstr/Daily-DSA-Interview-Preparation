```python
"""
Title: First Non-Repeating Character Per Stream Snapshot
Difficulty: Easy
Topic: Hashing

Problem Description:
You are given a string `stream` representing a sequence of characters arriving one at a time.
After processing each character, you must record the first non-repeating character seen so far
in the stream. If no such character exists at that point, record '#' instead.

Return a string `result` of the same length as `stream`, where `result[i]` is the first
non-repeating character after processing the first i+1 characters of `stream`.

A character is considered non-repeating if it has appeared exactly once in the portion
of the stream processed so far.

Constraints:
- 1 <= stream.length <= 10^5
- stream consists of only lowercase English letters.

Example 1:
Input: stream = "aabccb"
Output: "a#bbb#"

Example 2:
Input: stream = "abcd"
Output: "aaaa"
"""

from collections import OrderedDict
from typing import Dict


class Solution:
    def firstNonRepeatingCharStream(self, stream: str) -> str:
        """
        For each prefix of the stream, find the first non-repeating character.

        Approach:
        - We maintain a frequency count (hash map) to track how many times
          each character has appeared so far.
        - We also maintain an OrderedDict to keep track of characters that
          are currently non-repeating (count == 1), preserving insertion order.
          This allows O(1) access to the first non-repeating character at any time.
        - For each new character in the stream:
            1. Increment its count in the frequency map.
            2. If count == 1, add it to the OrderedDict (it's a new non-repeating char).
            3. If count == 2, remove it from the OrderedDict (it's now repeating).
            4. The first key in the OrderedDict is our answer, or '#' if empty.

        Why OrderedDict?
        - It preserves insertion order, so the first key is always the character
          that appeared earliest in the stream and is still non-repeating.
        - Adding and removing from an OrderedDict is O(1) on average.

        Args:
            stream (str): The input string representing the character stream.

        Returns:
            str: A string of the same length where each character is the first
                 non-repeating character after processing each prefix, or '#'.

        Time Complexity: O(n) where n is the length of the stream.
            - We iterate through each character once.
            - Each dictionary operation (insert, delete, peek) is O(1).

        Space Complexity: O(1) — at most 26 lowercase English letters can be stored
            in our data structures, so space is bounded by the alphabet size (constant).
        """

        # -----------------------------------------------------------------------
        # Step 1: Initialize data structures
        # -----------------------------------------------------------------------

        # frequency_map: tracks how many times each character has been seen so far.
        # Key = character, Value = count (integer)
        frequency_map: Dict[str, int] = {}

        # non_repeating_order: an OrderedDict that stores characters with count == 1,
        # in the order they first appeared. This lets us quickly find the first
        # non-repeating character by looking at the first key.
        # Key = character, Value = True (we only care about order, not the value)
        non_repeating_order: OrderedDict = OrderedDict()

        # result_chars: a list to collect the answer character for each position.
        # Using a list and joining at the end is more efficient than string concatenation.
        result_chars = []

        # -----------------------------------------------------------------------
        # Step 2: Process each character in the stream one by one
        # -----------------------------------------------------------------------

        for char in stream:
            # ------------------------------------------------------------------
            # Step 2a: Update the frequency count for the current character.
            # ------------------------------------------------------------------
            # If the character hasn't been seen before, initialize its count to 0,
            # then increment. This is equivalent to: frequency_map[char] = frequency_map.get(char, 0) + 1
            frequency_map[char] = frequency_map.get(char, 0) + 1

            # ------------------------------------------------------------------
            # Step 2b: Update the non_repeating_order based on the new count.
            # ------------------------------------------------------------------

            if frequency_map[char] == 1:
                # This character just appeared for the first time.
                # It is currently non-repeating, so add it to our OrderedDict.
                # It will be placed at the END of the OrderedDict (most recently seen
                # non-repeating character), preserving insertion order.
                non_repeating_order[char] = True

            elif frequency_map[char] == 2:
                # This character has now appeared twice — it is no longer non-repeating.
                # Remove it from our OrderedDict.
                # We only need to remove when count becomes 2 (the transition from
                # non-repeating to repeating). For count > 2, it's already been removed.
                if char in non_repeating_order:
                    del non_repeating_order[char]

            # Note: if frequency_map[char] > 2, the character was already removed
            # from non_repeating_order when its count became 2, so we do nothing.

            # ------------------------------------------------------------------
            # Step 2c: Determine the first non-repeating character at this point.
            # ------------------------------------------------------------------

            if non_repeating_order:
                # The OrderedDict is not empty. The first key (in insertion order)
                # is the earliest-seen character that is still non-repeating.
                # next(iter(...)) efficiently retrieves the first key in O(1).
                first_non_repeating = next(iter(non_repeating_order))
            else:
                # No non-repeating character exists at this point in the stream.
                first_non_repeating = '#'

            # Append the result for this position.
            result_chars.append(first_non_repeating)

        # -----------------------------------------------------------------------
        # Step 3: Join the result list into a single string and return it.
        # -----------------------------------------------------------------------
        # ''.join(list) is O(n) and more efficient than repeated string concatenation.
        return ''.join(result_chars)


# =============================================================================
# Manual Trace / Verification
# =============================================================================
#
# Example 1: stream = "aabccb"
# -----------------------------------------------------------------------
# i=0, char='a': freq={a:1}, non_rep={a}, first='a'  → result="a"
# i=1, char='a': freq={a:2}, non_rep={},  first='#'  → result="a#"
# i=2, char='b': freq={a:2,b:1}, non_rep={b}, first='b' → result="a#b"
# i=3, char='c': freq={a:2,b:1,c:1}, non_rep={b,c}, first='b' → result="a#bb"
# i=4, char='c': freq={a:2,b:1,c:2}, non_rep={b}, first='b' → result="a#bbb"
# i=5, char='b': freq={a:2,b:2,c:2}, non_rep={}, first='#' → result="a#bbb#"
# Final: "a#bbb#" ✓ matches expected output
#
# Example 2: stream = "abcd"
# -----------------------------------------------------------------------
# i=0, char='a': freq={a:1}, non_rep={a}, first='a' → result="a"
# i=1, char='b': freq={a:1,b:1}, non_rep={a,b}, first='a' → result="aa"
# i=2, char='c': freq={a:1,b:1,c:1}, non_rep={a,b,c}, first='a' → result="aaa"
# i=3, char='d': freq={a:1,b:1,c:1,d:1}, non_rep={a,b,c,d}, first='a' → result="aaaa"
# Final: "aaaa" ✓ matches expected output
# =============================================================================


if __name__ == "__main__":
    # Create a Solution instance
    solution = Solution()

    # -------------------------------------------------------------------------
    # Test Case 1: From the problem description
    # -------------------------------------------------------------------------
    stream1 = "aabccb"
    expected1 = "a#bbb#"
    result1 = solution.firstNonRepeatingCharStream(stream1)
    print(f"Test Case 1:")
    print(f"  Input   : stream = \"{stream1}\"")
    print(f"  Expected: \"{expected1}\"")
    print(f"  Got     : \"{result1}\"")
    print(f"  Pass    : {result1 == expected1}")
    print()

    # -------------------------------------------------------------------------
    # Test Case 2: From the problem description
    # -------------------------------------------------------------------------
    stream2 = "abcd"
    expected2 = "aaaa"
    result2 = solution.firstNonRepeatingCharStream(stream2)
    print(f"Test Case 2:")
    print(f"  Input   : stream = \"{stream2}\"")
    print(f"  Expected: \"{expected2}\"")
    print(f"  Got     : \"{result2}\"")
    print(f"  Pass    : {result2 == expected2}")
    print()

    # -------------------------------------------------------------------------
    # Test Case 3: Single character stream
    # -------------------------------------------------------------------------
    stream3 = "z"
    expected3 = "z"
    result3 = solution.firstNonRepeatingCharStream(stream3)
    print(f"Test Case 3 (single character):")
    print(f"  Input   : stream = \"{stream3}\"")
    print(f"  Expected: \"{expected3}\"")
    print(f"  Got     : \"{result3}\"")
    print(f"  Pass    : {result3 == expected3}")
    print()

    # -------------------------------------------------------------------------
    # Test Case 4: All same characters — always '#' after first character
    # -------------------------------------------------------------------------
    stream4 = "aaaa"
    expected4 = "a###"
    result4 = solution.firstNonRepeatingCharStream(stream4)
    print(f"Test Case 4 (all same characters):")
    print(f"  Input   : stream = \"{stream4}\"")
    print(f"  Expected: \"{expected4}\"")
    print(f"  Got     : \"{result4}\"")
    print(f"  Pass    : {result4 == expected4}")
    print()

    # -------------------------------------------------------------------------
    # Test Case 5: Alternating characters
    # stream = "ababab"
    # i=0 'a': freq={a:1}, non_rep={a}, first='a'
    # i=1 'b': freq={a:1,b:1}, non_rep={a,b}, first='a'
    # i=2 'a': freq={a:2,b:1}, non_rep={b}, first='b'
    # i=3 'b': freq={a:2,b:2}, non_rep={}, first='#'
    # i=4 'a': freq={a:3,b:2}, non_rep={}, first='#'
    # i=5 'b': freq={a:3,b:3}, non_rep={}, first='#'
    # Expected: "aab###"
    # -------------------------------------------------------------------------
    stream5 = "ababab"
    expected5 = "aab###"
    result5 = solution.firstNonRepeatingCharStream(stream5)
    print(f"Test Case 5 (alternating characters):")
    print(f"  Input   : stream = \"{stream5}\"")
    print(f"  Expected: \"{expected5}\"")
    print(f"  Got     : \"{result5}\"")
    print(f"  Pass    : {result5 == expected5}")
    print()

    # -------------------------------------------------------------------------
    # Test Case 6: Longer stream to verify performance
    # -------------------------------------------------------------------------
    import time
    stream6 = "abcdefghijklmnopqrstuvwxyz" * 3846  # ~100,000 characters
    start = time.time()
    result6 = solution.firstNonRepeatingCharStream(stream6)
    elapsed = time.time() - start
    print(f"Test Case 6 (performance, length={len(stream6)}):")
    print(f"  First 26 chars of result: \"{result6[:26]}\"")
    print(f"  Time elapsed: {elapsed:.4f} seconds")
    print()
```