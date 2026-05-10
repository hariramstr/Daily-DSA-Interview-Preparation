```python
"""
Title: Balancing Bracket Distances
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given a string `s` consisting only of characters '(' and ')'. The string is
guaranteed to be a valid bracket sequence (i.e., every opening bracket has a matching
closing bracket and they are properly nested).

For each matched pair of brackets (s[i], s[j]) where s[i] = '(' and s[j] = ')',
define the distance of that pair as j - i. Your task is to find the minimum possible
sum of distances across all matched pairs after you are allowed to swap any two
characters in the string at most once.

Note: After the swap, the resulting string must still be a valid bracket sequence.
If no beneficial swap exists, return the original sum of distances.

Constraints:
- 2 <= s.length <= 10^5
- s.length is even
- s consists only of '(' and ')'
- s is a valid bracket sequence

Example 1:
Input: s = "(())"
Output: 4
Explanation: The matched pairs are (0,3) and (1,2) with distances 3 and 1,
giving a sum of 4. No valid swap reduces this sum further.

Example 2:
Input: s = "()(())"
Output: 6
Explanation: Original matched pairs are (0,1), (2,5), (3,4) with distances 1, 3, 1
summing to 5. After swapping index 1 and 2 we get "(()()" — invalid. The optimal
valid configuration keeps the sum at 6 after evaluating all valid single swaps.
"""

from typing import List, Tuple


class Solution:
    def match_brackets(self, s: str) -> List[Tuple[int, int]]:
        """
        Find all matched bracket pairs in a valid bracket sequence.

        Uses a stack to match each '(' with its corresponding ')'.

        Args:
            s: A valid bracket sequence string.

        Returns:
            A list of (open_index, close_index) tuples for each matched pair.

        Time complexity: O(n) where n is the length of s.
        Space complexity: O(n) for the stack and result list.
        """
        # Stack to keep track of unmatched '(' indices
        stack: List[int] = []
        # Result list of matched pairs
        pairs: List[Tuple[int, int]] = []

        for i, ch in enumerate(s):
            if ch == '(':
                # Push the index of '(' onto the stack
                stack.append(i)
            else:
                # ')' found: pop the most recent unmatched '(' and form a pair
                open_idx = stack.pop()
                pairs.append((open_idx, i))

        return pairs

    def compute_sum_of_distances(self, pairs: List[Tuple[int, int]]) -> int:
        """
        Compute the total sum of distances for all matched bracket pairs.

        Args:
            pairs: List of (open_index, close_index) tuples.

        Returns:
            The sum of (close_index - open_index) for all pairs.

        Time complexity: O(n) where n is the number of pairs.
        Space complexity: O(1).
        """
        return sum(close - open_idx for open_idx, close in pairs)

    def is_valid(self, s: str) -> bool:
        """
        Check if a bracket string is a valid bracket sequence.

        Uses a counter approach: increment for '(', decrement for ')'.
        If counter ever goes negative, the sequence is invalid.

        Args:
            s: A string of '(' and ')' characters.

        Returns:
            True if the string is a valid bracket sequence, False otherwise.

        Time complexity: O(n).
        Space complexity: O(1).
        """
        count = 0
        for ch in s:
            if ch == '(':
                count += 1
            else:
                count -= 1
            # If count goes negative, we have an unmatched ')'
            if count < 0:
                return False
        # Valid if all brackets are matched (count == 0)
        return count == 0

    def minSumAfterSwap(self, s: str) -> int:
        """
        Find the minimum possible sum of bracket pair distances after at most one swap.

        Strategy:
        1. Compute the original sum of distances.
        2. Try all O(n^2) pairs of swaps, but only those that could be beneficial:
           - Swapping a ')' at position i with a '(' at position j where i < j
             (moving a ')' to the right or a '(' to the left increases distances,
              so we want to move ')' left or '(' right — but carefully).
           Actually, the key insight is:
           - To reduce the sum, we want to move '(' characters to the right
             or ')' characters to the left.
           - A swap of s[i] and s[j] where s[i] != s[j] is the only non-trivial swap.
           - Specifically, swapping ')' at position i with '(' at position j (i < j)
             moves ')' right and '(' left — this INCREASES distances.
           - Swapping '(' at position i with ')' at position j (i < j)
             moves '(' right and ')' left — this DECREASES distances.
        3. For efficiency with n up to 10^5, we need a smarter approach.

        Key Insight for Efficient Solution:
        The sum of distances equals sum(close_i - open_i) for all pairs.
        This can be rewritten as: sum(close_i) - sum(open_i).

        When we swap positions i and j (i < j):
        - If s[i] == s[j]: no change (same characters, sequence unchanged).
        - If s[i] == '(' and s[j] == ')': we're swapping '(' at i with ')' at j.
          The new string has ')' at i and '(' at j. We need to check validity.
        - If s[i] == ')' and s[j] == '(': we're swapping ')' at i with '(' at j.
          The new string has '(' at i and ')' at j. We need to check validity.

        For large n, we use the observation that only swaps near the "boundary"
        between outer and inner brackets matter. We use a two-pointer approach
        to find the best candidate swap.

        Two-Pointer Approach:
        - Use left pointer starting from the left, right pointer from the right.
        - Find the leftmost ')' that could be swapped with a '(' to its right,
          or find the rightmost '(' that could be swapped with a ')' to its left.
        - The most beneficial swap is: swap the leftmost ')' with the rightmost '('
          that appears after it (if valid).

        Actually, let's think more carefully:
        The sum = sum(j - i) for all matched pairs (i, j).
        = sum(j) - sum(i) where j are close indices and i are open indices.

        A swap of positions p and q (p < q) where s[p]='(' and s[q]=')':
        After swap: s[p]=')' and s[q]='('.
        This changes the matching. The effect on the sum depends on the new matching.

        For simplicity and correctness, given n <= 10^5, we can try all O(n^2/2)
        swaps but that's 5*10^9 operations — too slow.

        Better approach: Only swaps of '(' with ')' matter (different characters).
        The beneficial direction is swapping '(' at position i with ')' at position j
        where i < j — but this might invalidate the sequence.

        Actually the most impactful swap is:
        - Find the leftmost ')' (let's call it at position r) and the rightmost '('
          (let's call it at position l) where l < r. Swapping them moves ')' left
          and '(' right — wait, that increases distances.

        Let me reconsider. To MINIMIZE sum of distances:
        - We want '(' characters to be as far right as possible (larger index)
          and ')' characters to be as far left as possible (smaller index).
        - Wait no: distance = close - open. To minimize, we want close - open small,
          meaning '(' and ')' pairs should be close together.
        - The overall sum = sum(close_j) - sum(open_i).
          To minimize this, we want to maximize sum(open_i) and minimize sum(close_j).
          i.e., move '(' characters to higher indices and ')' characters to lower indices.

        So the beneficial swap is: swap a '(' at position i with a ')' at position j
        where j < i (move ')' left and '(' right). But for i < j in the original,
        we'd swap s[i]='(' with s[j]=')' where i < j — this moves '(' to position j
        (higher) and ')' to position i (lower). This IS beneficial!

        But we need the result to be valid. Let's check: if s[i]='(' and s[j]=')'
        with i < j, after swap we have ')' at i and '(' at j. This is likely invalid
        unless the surrounding structure supports it.

        The key observation: for the string to remain valid after swapping s[i]='('
        and s[j]=')', the characters between i and j must form a valid sequence on
        their own (since we're essentially removing the outer pair and the inner
        part must be self-contained).

        Two-pointer strategy:
        - left pointer: scan from left to find the first '(' that is "outermost"
          (i.e., the leftmost unmatched '(' from the perspective of the prefix).
          Actually, find the leftmost '(' whose match is as far right as possible.
        - right pointer: scan from right to find the rightmost ')' whose match
          is as far left as possible.

        For the problem constraints, let me implement a clean O(n^2) solution
        that tries all valid swaps, which should work for n <= 10^5 if we're smart
        about pruning. But O(n^2) is 10^10 — too slow.

        Let me think about the O(n) or O(n log n) approach.

        CORRECT EFFICIENT APPROACH:
        The sum of distances for a valid bracket sequence of length 2n equals:
        sum over all positions p of: (contribution of position p to the sum)

        Actually, there's a beautiful formula:
        For a valid bracket sequence, sum of distances = sum over all positions i of
        |balance_change_at_i * something|...

        Let me use a different angle. Define for each position i:
        - If s[i] = '(': it contributes -i to the sum (as an open bracket)
        - If s[i] = ')': it contributes +i to the sum (as a close bracket)
        Wait: sum(close - open) = sum(close) - sum(open) = sum(i * [s[i]==')']) - sum(i * [s[i]=='('])
        = sum over all i of: i * (1 if s[i]==')' else -1)
        = sum over all i of: i * sign(i)

        where sign(i) = +1 if s[i]=')' and -1 if s[i]='('.

        So the sum = sum_i [ i * (1 if s[i]==')' else -1) ]

        When we swap positions p and q (p < q):
        - If s[p] = s[q]: no change.
        - If s[p] = '(' and s[q] = ')':
          Change in sum = p*(+1) + q*(-1) - (p*(-1) + q*(+1))
                        = p + (-q) - (-p + q)
                        = p - q + p - q
                        = 2p - 2q
                        = 2(p - q) < 0 (since p < q)
          So this swap ALWAYS decreases the sum! But we need to check validity.

        - If s[p] = ')' and s[q] = '(':
          Change in sum = p*(-1) + q*(+1) - (p*(+1) + q*(-1))
                        = -p + q - p + q... wait let me redo.
          Original contribution: p*(+1) + q*(-1) = p - q
          After swap (s[p]='(' and s[q]=')'): p*(-1) + q*(+1) = -p + q
          Change = (-p + q) - (p - q) = -2p + 2q = 2(q - p) > 0
          This INCREASES the sum. So we never want this swap.

        CONCLUSION: We only want to swap '(' at position p with ')' at position q
        where p < q. This always decreases the sum by 2(q - p).
        To maximize the decrease, we want to maximize (q - p), i.e., find the
        leftmost '(' and the rightmost ')' such that the swap keeps the sequence valid.

        Now, when is swapping s[p]='(' and s[q]=')' (p < q) valid?
        After the swap, s becomes: ...)'...('...
        For validity, we need the resulting string to be a valid bracket sequence.

        The condition for validity after swapping '(' at p with ')' at q:
        The substring s[p+1..q-1] must be a valid bracket sequence on its own.
        (Because after the swap, the ')' at p needs to be matched by something
        before p, and the '(' at q needs to be matched by something after q.)

        Wait, let me think again. After swap:
        - Position p has ')' 
        - Position q has '('
        For the whole string to be valid:
        - The ')' at p must be matched with some '(' before p.
        - The '(' at q must be matched with some ')' after q.
        - The substring between p and q must be balanced.

        For the ')' at p to be matchable, there must be an unmatched '(' before p.
        For the '(' at q to be matchable, there must be an unmatched ')' after q.

        This is getting complex. Let me use the validity check directly.

        Since we want to maximize q - p, the best candidates are:
        - p = leftmost '(' in s
        - q = rightmost ')' in s
        Try this swap first. If valid, that's our answer.
        If not, try next candidates.

        But in the worst case, this could still be O(n^2).

        SIMPLER OBSERVATION for the two-pointer approach:
        The leftmost '(' is always at index 0 (since the sequence is valid and starts
        with '('). The rightmost ')' is always at index n-1.
        Swapping s[0]='(' with s[n-1]=')' gives ')...(' which is NEVER valid
        (starts with ')').

        So we need to be smarter. Let's think about which swaps are valid.

        After swapping '(' at p with ')' at q (p < q):
        The new string is valid iff:
        1. The prefix s[0..p-1] has equal '(' and ')' counts... no wait.

        Let me just use the is_valid check and try candidates smartly.

        For the two-pointer approach:
        - Start with left = 0, right = n-1
        - Move left to the right until we find a '(' 
        - Move right to the left until we find a ')'
        - If left < right and s[left]='(' and s[right]=')':
          Try this swap. If valid, compute new sum.
          The new sum = original_sum + 2*(left - right) = original_sum - 2*(right - left)
          This is the maximum possible decrease.
          Move left++ and right-- and continue to find next best.
        - Keep track of minimum sum found.

        But we need to verify validity. Let me implement this.

        Args:
            s: A valid bracket sequence string.

        Returns:
            The minimum possible sum of distances after at most one swap.

        Time complexity: O(n^2) worst case, O(n) best case with two-pointer pruning.
        Space complexity: O(n) for storing the modified string.
        """
        n = len(s)

        # -----------------------------------------------------------------------
        # Step 1: Compute the original sum of distances.
        # Using the formula: sum = sum_i [ i * (1 if s[i]==')' else -1) ]
        # -----------------------------------------------------------------------