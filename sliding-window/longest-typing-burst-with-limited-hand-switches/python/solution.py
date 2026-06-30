"""
Title: Longest Typing Burst With Limited Hand Switches

Problem Description:
You are given a string `s` representing a sequence of keys typed on a custom keyboard.
Each character belongs to either the left hand or the right hand. You are also given
a mapping string `handMap` of length 26, where `handMap[i]` is either 'L' or 'R',
indicating whether the lowercase letter ('a' + i) is typed with the left or right hand.

A contiguous substring of `s` is called a smooth typing burst if the number of times
the typist switches hands between adjacent characters in that substring is at most `k`.

Return the length of the longest smooth typing burst.

A hand switch is counted only between neighboring characters inside the chosen substring.
A substring of length 0 has length 0, and a substring of length 1 always has 0 switches.

Constraints:
- 1 <= s.length <= 2 * 10^5
- 0 <= k < s.length
- s contains only lowercase English letters
- handMap.length == 26
- Every character in handMap is either 'L' or 'R'
"""

from typing import List


class Solution:
    def _build_hand_sequence(self, s: str, hand_map: str) -> List[str]:
        """
        Convert the input string into a list of hand labels ('L' or 'R').

        Args:
            s: The typed string consisting of lowercase English letters.
            hand_map: A 26-character string where each position tells whether
                the corresponding letter is typed with the left or right hand.

        Returns:
            A list where each element is 'L' or 'R' for the matching character in `s`.

        Time complexity:
            O(n), where n is len(s)

        Space complexity:
            O(n), for the returned hand sequence
        """
        # For each character in s, compute its alphabet index with:
        # ord(ch) - ord('a')
        # Then use that index to look up whether it belongs to the left or right hand.
        return [hand_map[ord(ch) - ord("a")] for ch in s]

    def longest_typing_burst(self, s: str, k: int, handMap: str) -> int:
        """
        Find the length of the longest contiguous substring whose number of
        adjacent hand switches is at most k.

        Args:
            s: The typed string consisting of lowercase English letters.
            k: Maximum allowed number of hand switches inside the chosen substring.
            handMap: A 26-character mapping string of 'L' and 'R'.

        Returns:
            The maximum valid substring length.

        Time complexity:
            O(n), where n is len(s), because each pointer moves at most n times

        Space complexity:
            O(n), due to the hand sequence list
        """
        # Edge case:
        # If the string is empty, the answer is 0.
        # The problem constraints say length >= 1, but handling this makes the method robust.
        if not s:
            return 0

        # Step 1:
        # Convert every character in the string into its hand label.
        #
        # Example:
        #   s = "abca"
        #   hand sequence might become ['L', 'L', 'R', 'L']
        #
        # This makes the later logic much easier, because we only care whether
        # adjacent positions use the same hand or different hands.
        hands: List[str] = self._build_hand_sequence(s, handMap)

        # Step 2:
        # Use a sliding window [left, right].
        #
        # We will expand `right` one step at a time.
        # While doing so, we maintain how many hand switches exist INSIDE the window.
        #
        # Important detail:
        # A hand switch is counted between adjacent characters.
        # So for a window [left, right], the relevant adjacent pairs are:
        #   (left, left+1), (left+1, left+2), ..., (right-1, right)
        #
        # Therefore, when we add a new character at position `right`,
        # the only new adjacent pair introduced is (right-1, right).
        left: int = 0
        switches_in_window: int = 0
        best: int = 1

        # Step 3:
        # Expand the window by moving `right` from left to right across the string.
        for right in range(len(s)):
            # If right > 0, then adding position `right` creates one new adjacent pair:
            # (right - 1, right)
            #
            # If the hands differ, that pair contributes one hand switch.
            if right > 0 and hands[right] != hands[right - 1]:
                switches_in_window += 1

            # Step 4:
            # If the window now has too many switches, shrink it from the left
            # until it becomes valid again.
            #
            # Why does shrinking work?
            # Because when we move `left` forward by 1, we remove exactly one adjacent pair
            # from the window: the pair (left, left + 1), assuming left < right.
            #
            # If that removed pair was a hand switch, we must subtract 1.
            while switches_in_window > k:
                # Before moving left forward, check whether the pair being removed
                # contributes a switch.
                #
                # Current window includes the pair (left, left + 1) if left < right.
                # Since the while loop only matters when the window has at least 2 elements
                # causing too many switches, this condition is safe and explicit.
                if left < right and hands[left] != hands[left + 1]:
                    switches_in_window -= 1

                # Actually shrink the window.
                left += 1

            # Step 5:
            # At this point, the window [left, right] is valid:
            # it contains at most k hand switches.
            #
            # So we can update the best answer with its length.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        # After scanning all possible right endpoints, `best` is the answer.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt
    s1 = "abacabad"
    k1 = 2
    hand_map1 = "LLRLRRLLRLRLRLRLRLRLRLRLRL"
    result1 = solution.longest_typing_burst(s1, k1, hand_map1)
    print(result1)

    # Sample 2 from the prompt
    s2 = "zzxyyx"
    k2 = 1
    hand_map2 = "LRLRLRLRLRLRLRLRLRLRLRLRLR"
    result2 = solution.longest_typing_burst(s2, k2, hand_map2)
    print(result2)