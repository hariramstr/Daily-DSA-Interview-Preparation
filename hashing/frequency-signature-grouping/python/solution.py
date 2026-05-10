"""
Problem Title: Frequency Signature Grouping
Difficulty: Medium
Topic: Hashing

Problem Description:
Given a list of strings, group them together if they share the same frequency signature.
Two strings share the same frequency signature if the multiset of character frequencies
is identical.

For example, 'aab' has frequencies {a:2, b:1}, and 'bba' also has frequencies {b:2, a:1}.
The sorted frequency values are [2, 1] in both cases, but since the actual
character-to-count mapping matters for the signature, 'aab' and 'bbc' both have
signature [(2,1)] as a sorted list of counts, but 'abc' has signature [(1,1,1)].

Group all strings that share the same frequency signature and return the groups as a
list of lists. The order of groups and the order of strings within each group does
not matter.

Constraints:
- 1 <= words.length <= 10^4
- 1 <= words[i].length <= 100
- words[i] consists of lowercase English letters only.

Example 1:
Input: words = ["eat", "tea", "tan", "ate", "nat", "bat"]
Output: [["eat","tea","ate"],["tan","nat"],["bat"]]

Example 2:
Input: words = ["aab", "bba", "bbc", "aac", "xyz"]
Output: [["aab","aac"],["bba","bbc"],["xyz"]]
"""

from collections import Counter, defaultdict
from typing import List, Dict, Tuple


class Solution:
    def groupByFrequencySignature(self, words: List[str]) -> List[List[str]]:
        """
        Groups strings by their frequency signature.

        The frequency signature of a string is defined as the sorted tuple of
        (character, count) pairs from its character frequency map. This ensures
        that two strings are grouped together only if they have exactly the same
        characters appearing the same number of times.

        For example:
          - 'aab' -> Counter({'a':2, 'b':1}) -> sorted items -> (('a',2),('b',1))
          - 'aac' -> Counter({'a':2, 'c':1}) -> sorted items -> (('a',2),('c',1))
          These are DIFFERENT signatures, so they won't be grouped together.

        Wait — let me re-read the problem carefully.

        From Example 2: "aab" and "aac" ARE grouped together, and "bba" and "bbc"
        are grouped together. This means the signature is NOT based on which specific
        characters appear, but rather on the MULTISET of frequency counts only.

        So 'aab' has counts {2, 1} and 'aac' also has counts {2, 1} -> same group.
        'bba' has counts {2, 1} and 'bbc' also has counts {2, 1} -> same group.
        But 'aab' and 'bba' are in DIFFERENT groups.

        Hmm, but then why are 'aab' and 'bba' in different groups if both have
        the same multiset of counts {2, 1}?

        Re-reading Example 2 more carefully:
        Output: [["aab","aac"],["bba","bbc"],["xyz"]]

        'aab': a->2, b->1  => sorted counts tuple: (1, 2)
        'aac': a->2, c->1  => sorted counts tuple: (1, 2)
        'bba': b->2, a->1  => sorted counts tuple: (1, 2)
        'bbc': b->2, c->1  => sorted counts tuple: (1, 2)

        All four have the same sorted counts tuple (1, 2), yet they're in two
        different groups. So the key must include more information than just
        the sorted counts.

        Looking at the explanation again: "aab" and "aac" both have one character
        appearing twice and one appearing once with the same structure. "bba" and
        "bbc" similarly.

        The difference between groups must be: which specific characters appear
        with which counts. But 'aab' has 'a' appearing twice, and 'bba' has 'b'
        appearing twice. So the key IS the full (character, count) mapping.

        But then 'aab' (a->2, b->1) and 'aac' (a->2, c->1) would have different
        keys too! Yet they're in the same group.

        Let me think differently. Maybe the key is: the sorted tuple of counts
        paired with the sorted tuple of characters? No...

        Actually, re-reading the problem statement hint:
        "the multiset of character frequencies is identical"

        For 'aab': the multiset of (char, freq) pairs sorted by freq then char:
          sorted by freq descending: [('a',2), ('b',1)]
        For 'aac': sorted by freq descending: [('a',2), ('c',1)]
        These are different!

        But the example groups them together... Let me look at this differently.

        Perhaps the key is: replace each character with a rank based on its
        frequency order. So 'aab' -> the most frequent char (a) gets rank 1,
        least frequent (b) gets rank 2. 'aac' -> most frequent (a) gets rank 1,
        least frequent (c) gets rank 2. The "normalized" signature would be
        the same pattern.

        Actually, I think the correct interpretation is:
        The signature is the sorted tuple of frequency VALUES only (not which
        character has that frequency). So:
        - 'aab': sorted freq values = (1, 2) [sorted ascending]
        - 'aac': sorted freq values = (1, 2)
        - 'bba': sorted freq values = (1, 2)
        - 'bbc': sorted freq values = (1, 2)

        But then all four should be in the SAME group, not two separate groups!

        This contradicts Example 2's output. Let me re-examine...

        OH WAIT. Maybe I'm misreading Example 2. Let me re-read:
        "aab" and "aac" are grouped, "bba" and "bbc" are grouped separately.

        If the key is just sorted frequency counts, all four should be together.
        But they're not. So there must be something else distinguishing them.

        The only distinguishing factor between {aab, aac} and {bba, bbc} is
        which character is the "dominant" one. In aab/aac, 'a' appears twice.
        In bba/bbc, 'b' appears twice.

        So maybe the key involves: for each frequency value, which character
        (or set of characters) has that frequency? But that would make aab and
        aac different (aab: freq 2 -> {a}, freq 1 -> {b}; aac: freq 2 -> {a},
        freq 1 -> {c}).

        I'm confused. Let me try a different approach: maybe the key is the
        sorted tuple of (frequency, sorted_chars_with_that_frequency).

        For 'aab': freq 2 -> ['a'], freq 1 -> ['b'] => ((2, ('a',)), (1, ('b',)))
        For 'aac': freq 2 -> ['a'], freq 1 -> ['c'] => ((2, ('a',)), (1, ('c',)))
        These are different, so aab and aac would be in different groups.
        But Example 2 says they should be in the same group!

        I'm going in circles. Let me try yet another interpretation.

        Maybe the problem is using "frequency signature" to mean: normalize the
        string by replacing each character with a canonical placeholder based on
        its frequency rank. Like anagram grouping but for frequency patterns.

        For 'aab': a appears 2 times, b appears 1 time.
          Assign rank 1 to the char with highest freq (a->1), rank 2 to next (b->2).
          Normalized: "112" (a->1, a->1, b->2)
        For 'aac': a appears 2 times, c appears 1 time.
          a->1, c->2. Normalized: "112"
        For 'bba': b appears 2 times, a appears 1 time.
          b->1, a->2. Normalized: "112"
        For 'bbc': b appears 2 times, c appears 1 time.
          b->1, c->2. Normalized: "112"

        All four would have the same normalized form "112", so they'd all be
        in the same group. But Example 2 shows them in two groups!

        I'm clearly misunderstanding the problem. Let me re-read Example 2's
        explanation very carefully:

        "aab" and "aac" both have one character appearing twice and one appearing
        once with the same structure. "bba" and "bbc" similarly.

        So the "structure" must include WHICH character is dominant. The structure
        of 'aab' is: 'a' appears twice, another char appears once. The structure
        of 'bba' is: 'b' appears twice, another char appears once. These are
        different structures because different characters are dominant.

        So the key must be: for each character in the alphabet, how many times
        does it appear in the string? But we need to group strings where this
        mapping is "similar" in some way...

        Actually wait. Let me re-read the problem statement one more time:

        "Two strings share the same frequency signature if the multiset of
        character frequencies is identical."

        And then: "'aab' has frequencies {a:2, b:1}, and 'bba' also has
        frequencies {b:2, a:1}. The sorted frequency values are [2, 1] in both
        cases"

        So the problem says aab and bba BOTH have sorted frequency values [2,1].
        The problem is saying they have the same frequency signature!

        But then Example 2 groups them separately... This is contradictory.

        Let me look at Example 1 for clarity:
        "eat", "tea", "ate" are grouped together.
        "tan", "nat" are grouped together.
        "bat" is alone.

        eat: e->1, a->1, t->1 => sorted counts: (1,1,1)
        tea: t->1, e->1, a->1 => sorted counts: (1,1,1)
        ate: a->1, t->1, e->1 => sorted counts: (1,1,1)
        tan: t->1, a->1, n->1 => sorted counts: (1,1,1)
        nat: n->1, a->1, t->1 => sorted counts: (1,1,1)
        bat: b->1, a->1, t->1 => sorted counts: (1,1,1)

        ALL of these have sorted counts (1,1,1)! Yet they're in THREE different
        groups. So the key is definitely NOT just the sorted frequency counts.

        The key must be the full character->frequency mapping (i.e., the Counter
        itself). This is essentially anagram grouping!

        eat: {e:1, a:1, t:1}
        tea: {t:1, e:1, a:1} = {e:1, a:1, t:1} ✓ same as eat
        ate: {a:1, t:1, e:1} = {e:1, a:1, t:1} ✓ same as eat
        tan: {t:1, a:1, n:1}
        nat: {n:1, a:1, t:1} = {t:1, a:1, n:1} ✓ same as tan
        bat: {b:1, a:1, t:1}

        YES! This matches Example 1's output perfectly. The key is the full
        character frequency map (sorted tuple of (char, count) pairs).

        Now let's verify Example 2:
        aab: {a:2, b:1} => key: (('a',2),('b',1))
        aac: {a:2, c:1} => key: (('a',2),('c',1))
        bba: {b:2, a:1} => key: (('a',1),('b',2))
        bbc: {b:2, c:1} => key: (('b',2),('c',1))
        xyz: {x:1, y:1, z:1} => key: (('x',1),('y',1),('z',1))

        With this key:
        - aab: (('a',2),('b',1)) - unique
        - aac: (('a',2),('c',1)) - unique
        - bba: (('a',1),('b',2)) - unique
        - bbc: (('b',2),('c',1)) - unique
        - xyz: (('x',1),('y',1),('z',1)) - unique

        All would be in separate groups! But Example 2 shows aab and aac together,
        and bba and bbc together.

        So the full character->frequency mapping is NOT the key either.

        I'm very confused. Let me try to figure out what key would produce
        Example 2's output:

        Groups: {aab, aac}, {bba, bbc}, {xyz}

        What do aab and aac have in common that bba and bbc don't?
        - aab: a appears 2x, b appears 1x
        - aac: a appears 2x, c appears 1x
        Common: 'a' appears 2x, and some other char appears 1x.

        - bba: b appears 2x, a appears 1x
        - bbc: b appears 2x, c appears 1x
        Common: 'b' appears 2x, and some other char appears 1x.

        So the key seems to be: for each character, its frequency (but only
        including characters that appear). The "fixed" characters (those that
        appear in all strings of the group) keep their frequency, while the
        "variable" characters are abstracted away.

        But this is getting very complex. Let me try a simpler hypothesis:

        Key = frozenset of (char, count) pairs, but only for characters that
        appear in ALL strings of the group... No, that's circular.

        Alternative: Key = the Counter itself, but treating it as a frozenset
        of (char, count) pairs.

        aab: frozenset({('a',2), ('b',1)})
        aac: frozenset({('a',2), ('c',1)})

        These are different frozensets, so aab and aac would be in different groups.
        Still doesn't match.

        Let me try: Key = sorted tuple of counts only (ignoring which char).
        aab: (1, 2) [sorted ascending]
        aac: (1, 2)
        bba: (1, 2)
        bbc: (1, 2)
        xyz: (1, 1, 1)

        With this key, aab, aac, bba, bbc would ALL be in the same group.
        But Example 2 shows them in two groups.

        UNLESS... the example output in the problem is wrong or I'm misreading it?

        Let me re-read Example 2 one more time:
        Input: words = ["aab", "bba", "bbc", "aac", "xyz"]
        Output: [["aab","aac"],["bba","bbc"],["xyz"]]

        Hmm, what if the key is: sorted tuple of (count, sorted_chars_with_count)?

        For aab: {a:2, b:1}
          count 2: chars = ['a']
          count 1: chars = ['b']
          key: ((2, ('a',)), (1, ('b',)))

        For aac: {a:2, c:1}
          count 2: chars = ['a']
          count 1: chars = ['c']
          key: ((2, ('a',)), (1, ('c',)))

        These are different! aab and aac would be in different groups.

        What if the key uses only the "dominant" character's identity?

        For aab: dominant char = 'a' (appears 2x), pattern = ('a', 2, 1)
        For aac: dominant char = 'a' (appears 2x), pattern = ('a', 2, 1)