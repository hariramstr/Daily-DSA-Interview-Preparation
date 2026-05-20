/*
 * Title: Shortest Unique Prefix for Each Word
 * Difficulty: Medium
 * Topic: Tries
 *
 * Problem Description:
 * Given a list of distinct lowercase words, find the shortest prefix for each word
 * such that the prefix uniquely identifies that word among all words in the list.
 * In other words, no other word in the list starts with the same prefix.
 *
 * Return an array of strings where the i-th element is the shortest unique prefix
 * for the i-th word in the input list.
 *
 * Constraints:
 * - 1 <= words.length <= 1000
 * - 1 <= words[i].length <= 100
 * - All words consist of lowercase English letters only.
 * - All words in the list are distinct.
 * - It is guaranteed that no word is a prefix of another word in the list.
 *
 * Example 1:
 * Input:  ["dog", "cat", "car", "card", "done"]
 * Output: ["dog", "cat", "car", "card", "do"]
 *
 * Example 2:
 * Input:  ["apple", "banana", "cherry"]
 * Output: ["a", "b", "c"]
 */

// ─────────────────────────────────────────────────────────────────────────────
// TRIE NODE
// A Trie (prefix tree) is a tree where each node represents one character.
// Paths from the root to a node spell out a prefix of one or more words.
// ─────────────────────────────────────────────────────────────────────────────
class TrieNode
{
    // Each node can have up to 26 children, one per lowercase letter.
    // children[0] = 'a', children[1] = 'b', ..., children[25] = 'z'
    public TrieNode?[] Children { get; } = new TrieNode?[26];

    // How many words pass through (or end at) this node.
    // This is the KEY field: if passCount == 1, the path to this node
    // is already unique — only one word goes through here.
    public int PassCount { get; set; } = 0;
}

// ─────────────────────────────────────────────────────────────────────────────
// SOLUTION CLASS
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /*
     * Time Complexity:  O(N * L)
     *   where N = number of words, L = average word length.
     *   We insert every character of every word once, and query every character once.
     *
     * Space Complexity: O(N * L * 26)
     *   In the worst case the trie stores every character of every word,
     *   and each node holds a 26-element children array.
     *   In practice the shared prefixes reduce this significantly.
     */
    public string[] ShortestUniquePrefixes(string[] words)
    {
        // ── STEP 1: Build the Trie ────────────────────────────────────────────
        // Create the root node. The root itself doesn't represent any character;
        // it is just the entry point into the trie.
        TrieNode root = new TrieNode();

        // Insert every word into the trie, incrementing PassCount at each node
        // we visit. After all insertions, PassCount at a node tells us how many
        // words share the prefix represented by the path from root to that node.
        foreach (string word in words)
        {
            // Start at the root for each new word.
            TrieNode current = root;

            foreach (char ch in word)
            {
                // Convert the character to a 0-based index (a=0, b=1, …, z=25).
                int index = ch - 'a';

                // If no child exists for this character yet, create one.
                // This is how the trie "learns" new prefixes.
                if (current.Children[index] == null)
                {
                    current.Children[index] = new TrieNode();
                }

                // Move down to the child node for this character.
                current = current.Children[index]!;

                // Increment PassCount: one more word passes through this node.
                // After all words are inserted:
                //   PassCount == 1  →  exactly one word uses this prefix  →  UNIQUE
                //   PassCount >  1  →  multiple words share this prefix   →  NOT unique yet
                current.PassCount++;
            }
        }

        // ── STEP 2: Query the Trie for each word ─────────────────────────────
        // For each word, walk the trie character by character.
        // The moment we reach a node whose PassCount == 1, we have found the
        // shortest prefix that is unique to this word — stop there.
        string[] result = new string[words.Length];

        for (int i = 0; i < words.Length; i++)
        {
            string word = words[i];

            // We'll build the unique prefix character by character.
            System.Text.StringBuilder prefix = new System.Text.StringBuilder();

            // Start at the root again.
            TrieNode current = root;

            foreach (char ch in word)
            {
                // Append the current character to our growing prefix.
                prefix.Append(ch);

                // Move to the child node for this character.
                int index = ch - 'a';
                current = current.Children[index]!;

                // ── KEY CHECK ────────────────────────────────────────────────
                // If PassCount == 1, this node is visited by exactly one word
                // (the current word). No other word shares this prefix.
                // Therefore, the prefix we have built so far is the SHORTEST
                // unique prefix for this word. We can stop immediately.
                //
                // Why is this correct?
                //   - We inserted all words, so PassCount reflects all words.
                //   - PassCount == 1 means only our current word passes here.
                //   - Any shorter prefix (parent nodes) had PassCount > 1,
                //     meaning they were shared — so we couldn't stop earlier.
                // ─────────────────────────────────────────────────────────────
                if (current.PassCount == 1)
                {
                    break; // Found the shortest unique prefix — exit the loop.
                }
            }

            // Store the unique prefix for word[i].
            result[i] = prefix.ToString();
        }

        // ── STEP 3: Return the results ────────────────────────────────────────
        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DEMO / TEST CODE  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Solution solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Expected: ["dog", "cat", "car", "card", "do"]
//
// Trace through the trie after insertion:
//   'd' node  → PassCount 2  (dog, done share 'd')
//   'do' node → PassCount 2  (dog, done share 'do')
//   'dog' node→ PassCount 1  → unique prefix for "dog" is "dog"
//   'don' node→ PassCount 1  (only "done" goes here, but we already stopped at 'do'?)
//     Wait — let's re-check "done":
//       'd'  PassCount=2 → not unique
//       'do' PassCount=2 → not unique
//       'don'PassCount=1 → unique!  prefix = "don"
//     Hmm, but expected is "do". Let me re-read the problem...
//     Oh — the expected output says "done" → "do". But "dog" also starts with "do".
//     So "do" is NOT unique for "done". The expected output in the problem description
//     says "do" for "done", but that contradicts the uniqueness requirement since
//     "dog" also starts with "do".
//
//     Re-reading the problem example explanation:
//       "done" → "do" ("d" is shared with "dog", but "do" is unique)
//     This seems like an error in the problem statement. "do" is shared between
//     "dog" and "done". The correct unique prefix for "done" should be "don".
//
//     Our algorithm correctly produces "don" for "done" because:
//       PassCount('d')  = 2  (dog, done)
//       PassCount('do') = 2  (dog, done)
//       PassCount('don')= 1  → stop → prefix = "don"
//
//     The problem's own explanation contradicts itself ("do" is NOT unique when
//     "dog" also starts with "do"). Our algorithm is correct.
//
// Actual correct output: ["dog", "cat", "car", "card", "don"]
Console.WriteLine("=== Example 1 ===");
string[] words1 = ["dog", "cat", "car", "card", "done"];
string[] result1 = solution.ShortestUniquePrefixes(words1);
Console.WriteLine("Input:    [" + string.Join(", ", words1) + "]");
Console.WriteLine("Output:   [" + string.Join(", ", result1) + "]");
Console.WriteLine("Expected: [dog, cat, car, card, don]");
Console.WriteLine();

// Verification trace for Example 1:
// Word "dog":  d(2) → do(2) → dog(1) ✓  prefix="dog"
// Word "cat":  c(3) → ca(2) → cat(1) ✓  prefix="cat"
// Word "car":  c(3) → ca(2) → car(2) → car(1 for 'r' child of 'ca')
//              Wait: "car" and "card" both pass through c-a-r, so PassCount('car')=2
//              then 'card' node PassCount=1 → prefix="card" for "card"
//              and for "car": c(3)→ca(2)→car(2)→ we need one more char but "car" ends
//              Actually "car" has length 3. After 'r', PassCount=2 (car,card share c-a-r).
//              The loop ends (no more chars in "car"), so prefix = "car" (full word).
// Word "card": c(3)→ca(2)→car(2)→card(1) ✓  prefix="card"
// Word "done": d(2)→do(2)→don(1) ✓  prefix="don"

// ── Example 2 ────────────────────────────────────────────────────────────────
// Expected: ["a", "b", "c"]
Console.WriteLine("=== Example 2 ===");
string[] words2 = ["apple", "banana", "cherry"];
string[] result2 = solution.ShortestUniquePrefixes(words2);
Console.WriteLine("Input:    [" + string.Join(", ", words2) + "]");
Console.WriteLine("Output:   [" + string.Join(", ", result2) + "]");
Console.WriteLine("Expected: [a, b, c]");
Console.WriteLine();

// ── Example 3: Additional edge cases ─────────────────────────────────────────
Console.WriteLine("=== Example 3 (additional) ===");
string[] words3 = ["zebra", "zoo", "zoom", "zone", "apple"];
// 'z' PassCount=4 (zebra,zoo,zoom,zone), 'a' PassCount=1 → "a" for apple
// "zebra": z(4)→ze(1) → prefix="ze"
// "zoo":   z(4)→zo(3)→zoo(2)→zoo(2)... "zoo" ends here, PassCount=2 (zoo,zoom share z-o-o)
//          Actually "zoo" and "zoom" both pass z-o-o, so PassCount(zoo-node)=2.
//          "zoo" has length 3, loop ends → prefix="zoo" (full word)
// "zoom":  z(4)→zo(3)→zoo(2)→zoom(1) → prefix="zoom"
// "zone":  z(4)→zo(3)→zon(1) → prefix="zon"
// "apple": a(1) → prefix="a"
string[] result3 = solution.ShortestUniquePrefixes(words3);
Console.WriteLine("Input:    [" + string.Join(", ", words3) + "]");
Console.WriteLine("Output:   [" + string.Join(", ", result3) + "]");
Console.WriteLine("Expected: [ze, zoo, zoom, zon, a]");
Console.WriteLine();

// ── Example 4: Single word ────────────────────────────────────────────────────
Console.WriteLine("=== Example 4 (single word) ===");
string[] words4 = ["hello"];
string[] result4 = solution.ShortestUniquePrefixes(words4);
Console.WriteLine("Input:    [" + string.Join(", ", words4) + "]");
Console.WriteLine("Output:   [" + string.Join(", ", result4) + "]");
Console.WriteLine("Expected: [h]");