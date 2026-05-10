/*
 * Title: Frequency Signature Grouping
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * Given a list of strings, group them together if they share the same frequency signature.
 * Two strings share the same frequency signature if the multiset of character frequencies
 * is identical (i.e., the full character-to-count mapping is the same).
 *
 * For example:
 * - 'aab' has frequencies {a:2, b:1} -> signature key: "a2b1"
 * - 'bba' has frequencies {b:2, a:1} -> signature key: "a1b2"
 * These are DIFFERENT signatures because the character-to-count mapping differs.
 *
 * - 'aab' and 'aac' both have {one_char:2, another_char:1} but with different chars,
 *   so they share the same SORTED FREQUENCY VALUES signature [2,1].
 *
 * Wait — re-reading the problem carefully:
 * Example 2 shows "aab" and "aac" grouped together, and "bba" and "bbc" grouped together.
 * This means the key is the SORTED LIST OF FREQUENCY VALUES (not which character has which count).
 * "aab" -> {a:2, b:1} -> sorted counts: [2,1]
 * "aac" -> {a:2, c:1} -> sorted counts: [2,1]  => same group
 * "bba" -> {b:2, a:1} -> sorted counts: [2,1]  => same group as aab? No, they're separate!
 *
 * But Example 2 shows "aab","aac" in one group and "bba","bbc" in another.
 * All four have sorted counts [2,1]. So sorted counts alone can't be the key.
 *
 * Looking more carefully: the key must be the full character-frequency mapping sorted by character.
 * "aab" -> a:2, b:1 -> "a2b1"
 * "aac" -> a:2, c:1 -> "a2c1"  -- these are DIFFERENT!
 *
 * Hmm, but Example 2 groups "aab" and "aac" together...
 *
 * Let me re-read: "aab" and "aac" both have one character appearing twice and one appearing once
 * with the same structure. So the key is the SORTED FREQUENCY VALUES: [2,1].
 * But then "bba" and "bbc" also have [2,1] and should be in the same group as "aab"/"aac"...
 *
 * Unless the key is: sorted list of (char, count) pairs where chars are normalized/anonymized.
 * Actually the simplest interpretation that matches Example 2:
 * The key = sorted list of frequency counts only (not which char).
 * But then all of "aab","aac","bba","bbc" would be in one group [2,1].
 *
 * Example 1: "eat","tea","ate" all have [1,1,1] and "tan","nat" also have [1,1,1] and "bat" has [1,1,1].
 * But they're in separate groups! So sorted counts alone is NOT the key.
 *
 * The key for Example 1 must include which characters appear. So it's the full char->count map.
 * "eat" -> {e:1,a:1,t:1} -> "a1e1t1"
 * "tea" -> {t:1,e:1,a:1} -> "a1e1t1" (same)
 * "tan" -> {t:1,a:1,n:1} -> "a1n1t1" (different)
 * "bat" -> {b:1,a:1,t:1} -> "a1b1t1" (different)
 * This matches Example 1!
 *
 * For Example 2 with full char->count map:
 * "aab" -> "a2b1", "aac" -> "a2c1" -- DIFFERENT, so they'd be in separate groups.
 * But Example 2 says they should be grouped together!
 *
 * There's a contradiction. Let me re-read Example 2 explanation more carefully.
 * "aab" and "aac" both have one character appearing twice and one appearing once with the same structure.
 * "bba" and "bbc" similarly.
 *
 * So for Example 2, the grouping is by sorted frequency values only: [2,1].
 * But for Example 1, "eat","tea","ate" vs "tan","nat" vs "bat" are separated even though all have [1,1,1].
 *
 * This is contradictory unless... the problem means:
 * The signature IS the full char->count mapping (sorted by char).
 * Example 2 might just be wrong/misleading in the problem statement,
 * OR Example 2 is using a different definition.
 *
 * Actually wait - let me re-read Example 2 output: [["aab","aac"],["bba","bbc"],["xyz"]]
 * If key = full char->count map:
 * "aab" -> a2b1, "aac" -> a2c1, "bba" -> a1b2, "bbc" -> b2c1, "xyz" -> x1y1z1
 * All separate groups! That doesn't match either.
 *
 * I think the problem has an inconsistency. Let me try to find a key that satisfies BOTH examples.
 *
 * For Example 1 to work (eat/tea/ate together, tan/nat together, bat alone):
 * The key must distinguish {a,e,t} from {a,n,t} from {a,b,t}.
 * So the key includes which characters are present.
 *
 * For Example 2 to work (aab/aac together, bba/bbc together):
 * "aab" has chars {a,b} with counts {2,1}
 * "aac" has chars {a,c} with counts {2,1}
 * These are grouped together, so the key does NOT include which specific chars.
 *
 * Contradiction! Unless the key is something like: sorted list of counts, AND the set of chars is considered equivalent if they have the same count pattern...
 *
 * Actually, I think I need to look at this differently.
 * Maybe the key is: for each character in the string, replace it with a canonical label based on frequency rank.
 *
 * Or maybe: the key is just the sorted list of frequency counts (ignoring which char has which count).
 * Then for Example 1: eat, tea, ate, tan, nat, bat ALL have [1,1,1] -> all one group.
 * But Example 1 shows 3 groups. So this can't be right either.
 *
 * I'm going to go with: the key is the full character->count mapping (sorted by character).
 * This correctly handles Example 1.
 * For Example 2, the expected output in the problem might be wrong, or I'm misreading it.
 *
 * Actually, let me re-read Example 2 one more time...
 * Input: ["aab", "bba", "bbc", "aac", "xyz"]
 * Output: [["aab","aac"],["bba","bbc"],["xyz"]]
 *
 * Hmm. "aab": a appears 2 times, b appears 1 time.
 * "aac": a appears 2 times, c appears 1 time.
 * These are grouped. The common factor: 'a' appears twice in both.
 *
 * "bba": b appears 2 times, a appears 1 time.
 * "bbc": b appears 2 times, c appears 1 time.
 * These are grouped. The common factor: 'b' appears twice in both.
 *
 * So the key IS the full char->count mapping! "aab" and "aac" share a->2 as the dominant char.
 * Wait: "aab" -> {a:2, b:1} and "aac" -> {a:2, c:1}.
 * If key = sorted by char: "a2b1" vs "a2c1" -- different.
 *
 * Unless the key normalizes the characters that appear only once?
 * Like: keep the chars that appear more than once, and just note the count of chars appearing once?
 *
 * OR: the key is built by sorting entries by (count DESC, char ASC) and then encoding?
 * "aab": sorted by count desc: (a,2),(b,1) -> "a2b1"
 * "aac": sorted by count desc: (a,2),(c,1) -> "a2c1" -- still different
 *
 * I wonder if the problem intends: replace each character with its frequency, then sort the resulting string.
 * "aab" -> "221" -> sorted: "122"
 * "aac" -> "221" -> sorted: "122" -- SAME!
 * "bba" -> "221" -> sorted: "122" -- SAME!
 * "bbc" -> "221" -> sorted: "122" -- SAME!
 * "xyz" -> "111" -> sorted: "111"
 * Then all of aab,aac,bba,bbc would be in one group. But Example 2 shows them in two groups.
 *
 * I'm going in circles. Let me try yet another interpretation:
 * The key = sorted list of (count, char) pairs, where we sort by count descending then char ascending.
 * No wait, that's essentially the same as full char->count map.
 *
 * Let me try: key = the string itself sorted (anagram key).
 * "aab" sorted: "aab"
 * "aac" sorted: "aac" -- different, so separate groups. Doesn't match Example 2.
 *
 * OK here's another idea. What if the problem means:
 * Replace each character in the string with a canonical placeholder based on the ORDER of first appearance,
 * and the frequency of that character?
 *
 * "aab": a appears first (freq 2), b appears second (freq 1) -> pattern: "2,1" or encode as "XXY"
 * "bba": b appears first (freq 2), a appears second (freq 1) -> pattern: "2,1" or encode as "XXY"
 * "aac": a appears first (freq 2), c appears second (freq 1) -> pattern: "2,1" or encode as "XXY"
 * "bbc": b appears first (freq 2), c appears second (freq 1) -> pattern: "2,1" or encode as "XXY"
 * All four would have the same key "XXY" or "2,1".
 * But Example 2 separates them into two groups!
 *
 * I genuinely cannot find a consistent rule that satisfies both examples as stated.
 * The problem description itself says in Example 2: "aab" and "aac" are grouped, "bba" and "bbc" are grouped separately.
 *
 * Wait... maybe I should look at this from a different angle.
 * What if the key is: sorted list of (char, count) pairs, sorted by COUNT then by CHAR?
 * "aab": {a:2, b:1} -> sorted by count asc then char: [(b,1),(a,2)] -> "b1a2"
 * "aac": {a:2, c:1} -> sorted by count asc then char: [(c,1),(a,2)] -> "c1a2"
 * Still different.
 *
 * What if we sort by count DESC then char ASC?
 * "aab": [(a,2),(b,1)] -> "a2b1"
 * "aac": [(a,2),(c,1)] -> "a2c1"
 * Still different.
 *
 * I think there might be an error in the problem statement for Example 2.
 * The most natural and consistent interpretation (matching Example 1) is:
 * Key = full character->count mapping (sorted by character alphabetically).
 *
 * With this key:
 * Example 1: eat->a1e1t1, tea->a1e1t1, ate->a1e1t1 (same group) ✓
 *            tan->a1n1t1, nat->a1n1t1 (same group) ✓
 *            bat->a1b1t1 (alone) ✓
 *
 * Example 2: aab->a2b1, aac->a2c1, bba->a1b2, bbc->b2c1, xyz->x1y1z1
 *            All separate groups. Doesn't match stated output.
 *
 * Since Example 1 is the "classic anagram grouping" problem and makes perfect sense,
 * I'll implement the full char->count map as the key (which is equivalent to sorting the string,
 * since anagrams have the same sorted form).
 *
 * Actually wait - sorting the string IS the standard anagram key!
 * "eat" sorted = "aet", "tea" sorted = "aet", "ate" sorted = "aet" -> same group ✓
 * "tan" sorted = "ant", "nat" sorted = "ant" -> same group ✓
 * "bat" sorted = "abt" -> alone ✓
 *
 * For Example 2:
 * "aab" sorted = "aab", "aac" sorted = "aac" -> different groups
 * But Example 2 says they should be in the same group!
 *
 * So Example 2 is NOT anagram grouping. It's something else.
 *
 * The problem title says "Frequency Signature Grouping" and the description says
 * "the multiset of character frequencies is identical."
 * Multiset of frequencies = just the counts, not which char has which count.
 * "aab": multiset of frequencies = {2, 1}
 * "aac": multiset of frequencies = {2, 1} -> SAME
 * "bba": multiset of frequencies = {2, 1} -> SAME
 * "bbc": multiset of frequencies = {2, 1} -> SAME
 * All four would be in one group with key [2,1].
 *
 * But Example 2 shows them in TWO groups (aab/aac) and (bba/bbc).
 * And Example 1 shows eat/tea/ate, tan/nat, bat in THREE groups even though all have multiset {1,1,1}.
 *
 * The problem is genuinely contradictory. I'll go with what makes the most sense algorithmically
 * and matches Example 1 (which is the cleaner example): use the sorted string as the key (anagram grouping).
 * This is the standard LeetCode "Group Anagrams" problem.
 *
 * For Example 2, with sorted string key:
 * "aab"->"aab", "bba"->"abb", "bbc"->"bbc", "aac"->"aac", "xyz"->"xyz"
 * All in separate groups. Still doesn't match.
 *
 * FINAL DECISION: I'll implement TWO approaches and use the one that matches Example 1.
 * The key = sorted character array of the string (standard anagram grouping).
 * This matches Example 1 perfectly.
 * For Example 2, I'll note the discrepancy but implement what's consistent.
 *
 * Actually, you know what, let me try one more thing for Example 2.
 * What if the key is: sorted list of frequencies, but also considering which CHARACTER is the most frequent?
 * "aab": most frequent char = 'a' (count 2), others: [1] -> key = "a:2,_:1"
 * "aac": most frequent char = 'a' (count 2), others: [1] -> key = "a:2,_:1" SAME!
 * "bba": most frequent char = 'b' (count 2), others: [1] -> key = "b:2,_:1" DIFFERENT from aab!
 * "bbc": most frequent char = 'b' (count 2), others: [1