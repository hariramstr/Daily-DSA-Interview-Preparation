/*
 * Title: Auto-Complete Sentence Builder
 * Difficulty: Easy
 * Topic: Tries
 *
 * Problem Description:
 * You are building a simple auto-complete feature for a text editor. Given a list of
 * previously typed sentences and a partial input string, return all sentences from the
 * list that start with the given partial input, sorted in the order they were originally
 * inserted.
 *
 * A sentence is considered a match if it begins with the exact characters of the partial
 * input (case-sensitive). If no sentences match, return an empty list.
 *
 * You must implement your solution using a Trie data structure to efficiently store and
 * search the sentences.
 *
 * Constraints:
 * - 1 <= sentences.length <= 200
 * - 1 <= sentences[i].length <= 100
 * - 1 <= partial.length <= 50
 * - All characters in sentences and partial are printable ASCII characters
 *   (letters, digits, spaces, punctuation).
 * - Sentences may contain spaces.
 * - Duplicate sentences in the input list should each be returned individually.
 */

import java.util.*;

/**
 * Solution class implementing an Auto-Complete Sentence Builder using a Trie data structure.
 *
 * <p>The Trie (prefix tree) allows efficient prefix-based searching. Each node in the Trie
 * represents a single character, and paths from root to terminal nodes represent stored sentences.
 * Because sentences can contain any printable ASCII character (including spaces), we use a
 * HashMap at each node to map characters to child nodes.</p>
 */
public class Solution {

    // -------------------------------------------------------------------------
    // Inner class: TrieNode
    // -------------------------------------------------------------------------

    /**
     * Represents a single node in the Trie.
     *
     * <p>Each node stores:
     * <ul>
     *   <li>A map of child nodes keyed by character.</li>
     *   <li>A list of complete sentences that pass through (end at) this node,
     *       stored in insertion order so we can return results in the correct order.</li>
     * </ul>
     * </p>
     */
    static class TrieNode {
        /**
         * Maps each character to the corresponding child TrieNode.
         * We use a HashMap because the character set is all printable ASCII,
         * which is too large for a fixed-size array to be practical.
         */
        Map<Character, TrieNode> children;

        /**
         * Stores the complete sentences that END at this node (i.e., this node
         * is the last character of those sentences). We keep them in a list to
         * handle duplicates and preserve insertion order.
         */
        List<String> sentences;

        /** Constructs an empty TrieNode with no children and no sentences. */
        TrieNode() {
            children = new HashMap<>();
            sentences = new ArrayList<>();
        }
    }

    // -------------------------------------------------------------------------
    // Inner class: Trie
    // -------------------------------------------------------------------------

    /**
     * A Trie (prefix tree) that stores sentences character by character.
     *
     * <p>Design choice: When inserting a sentence, we walk down the Trie one character
     * at a time, creating nodes as needed. At EVERY node along the path (not just the
     * terminal node), we store the complete sentence. This means that when we search
     * for a prefix, we only need to navigate to the node corresponding to the last
     * character of the prefix, and then return all sentences stored at that node —
     * they are guaranteed to start with the prefix.</p>
     */
    static class Trie {

        /** The root node of the Trie. It does not represent any character itself. */
        private final TrieNode root;

        /** Constructs an empty Trie with just a root node. */
        Trie() {
            root = new TrieNode();
        }

        /**
         * Inserts a sentence into the Trie.
         *
         * <p>For each character in the sentence, we move to (or create) the corresponding
         * child node. At EACH node we visit along the path, we record the full sentence.
         * This "store at every node" strategy makes prefix search O(P) where P is the
         * length of the prefix — we just navigate to the prefix endpoint and read the list.</p>
         *
         * @param sentence The sentence to insert. Must not be null.
         *
         * Time complexity:  O(L) where L = sentence.length()
         * Space complexity: O(L) for the new nodes created (in the worst case, all L
         *                   characters are new and each gets a new TrieNode).
         */
        void insert(String sentence) {
            // Start at the root of the Trie
            TrieNode current = root;

            // Iterate over every character in the sentence
            for (char ch : sentence.toCharArray()) {

                // Step 1: Check if a child node for this character already exists.
                //         If not, create a new TrieNode and add it to the children map.
                current.children.putIfAbsent(ch, new TrieNode());

                // Step 2: Move down to the child node for this character.
                current = current.children.get(ch);

                // Step 3: Store the full sentence at this node.
                //         Any search that reaches this node used a prefix that matches
                //         the beginning of 'sentence', so we record it here.
                current.sentences.add(sentence);
            }
            // After the loop, 'current' is the terminal node for this sentence.
            // The sentence is already stored at every node along the path, including
            // the terminal node, so no extra work is needed here.
        }

        /**
         * Searches the Trie for all sentences that start with the given prefix.
         *
         * <p>We navigate the Trie one character at a time following the prefix. If at any
         * point a required child node does not exist, the prefix has no matches and we
         * return an empty list. If we successfully reach the node corresponding to the
         * last character of the prefix, we return the list of sentences stored there —
         * all of which begin with the prefix.</p>
         *
         * @param prefix The partial input string to search for. Must not be null.
         * @return A list of sentences (in insertion order) that start with {@code prefix},
         *         or an empty list if none match.
         *
         * Time complexity:  O(P) where P = prefix.length() to navigate the Trie,
         *                   plus O(R) to return the result list of R matching sentences.
         * Space complexity: O(R) for the returned result list.
         */
        List<String> search(String prefix) {
            // Start at the root
            TrieNode current = root;

            // Walk through each character of the prefix
            for (char ch : prefix.toCharArray()) {

                // Step 1: Check if the current node has a child for this character.
                if (!current.children.containsKey(ch)) {
                    // No child means no sentence in the Trie starts with this prefix.
                    // Return an empty list immediately.
                    return new ArrayList<>();
                }

                // Step 2: Move to the child node corresponding to this character.
                current = current.children.get(ch);
            }

            // Step 3: We have successfully navigated to the node representing the
            //         last character of the prefix. The sentences stored at this node
            //         are exactly those that begin with the prefix.
            //         Return a copy so the caller cannot accidentally modify the Trie's
            //         internal state.
            return new ArrayList<>(current.sentences);
        }
    }

    // -------------------------------------------------------------------------
    // Main algorithm method
    // -------------------------------------------------------------------------

    /**
     * Returns all sentences from {@code sentences} that start with {@code partial},
     * in the order they were originally inserted.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>Build a Trie by inserting every sentence.</li>
     *   <li>Query the Trie with {@code partial} to retrieve matching sentences.</li>
     * </ol>
     * </p>
     *
     * @param sentences An array of previously typed sentences. Length 1–200, each 1–100 chars.
     * @param partial   The partial input string typed by the user. Length 1–50 chars.
     * @return A list of sentences from {@code sentences} that begin with {@code partial},
     *         in insertion order. Returns an empty list if no sentences match.
     *
     * Time complexity:  O(N * L + P) where N = number of sentences, L = average sentence
     *                   length (for insertion), and P = partial.length() (for search).
     * Space complexity: O(N * L) for the Trie nodes and stored sentence references.
     */
    public List<String> autoComplete(String[] sentences, String partial) {

        // ---- Step 1: Create a new Trie ----
        Trie trie = new Trie();

        // ---- Step 2: Insert every sentence into the Trie ----
        // We iterate in order so that insertion order is preserved in each node's list.
        for (String sentence : sentences) {
            trie.insert(sentence);
        }

        // ---- Step 3: Search the Trie for all sentences matching the prefix ----
        List<String> results = trie.search(partial);

        // ---- Step 4: Return the results ----
        // The list is already in insertion order because we inserted sentences in order
        // and ArrayList preserves insertion order.
        return results;
    }

    // -------------------------------------------------------------------------
    // Main method — demonstration and verification
    // -------------------------------------------------------------------------

    /**
     * Entry point. Demonstrates the auto-complete feature with the examples from the
     * problem description and additional edge cases.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // ------------------------------------------------------------------
        // Example 1 (from problem description)
        // Input : sentences = ["hello world", "hello there", "help me", "goodbye"]
        //         partial   = "hel"
        // Expected Output: ["hello world", "hello there", "help me"]
        // ------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        String[] sentences1 = {"hello world", "hello there", "help me", "goodbye"};
        String partial1 = "hel";
        List<String> result1 = solution.autoComplete(sentences1, partial1);
        System.out.println("Input sentences : " + Arrays.toString(sentences1));
        System.out.println("Partial input   : \"" + partial1 + "\"");
        System.out.println("Output          : " + result1);
        // Trace:
        //   Insert "hello world" -> nodes h->e->l->l->o->' '->w->o->r->l->d
        //                          sentence stored at every node along the path
        //   Insert "hello there" -> shares h->e->l->l->o, then branches at ' '
        //   Insert "help me"     -> shares h->e->l, then branches at 'p'
        //   Insert "goodbye"     -> entirely new branch from root
        //   Search "hel":
        //     root -> 'h' -> 'e' -> 'l'  (node for 'l')
        //     sentences at 'l' node = ["hello world", "hello there", "help me"]  ✓
        System.out.println();

        // ------------------------------------------------------------------
        // Example 2 (from problem description)
        // Input : sentences = ["apple pie", "apple juice", "banana split", "apple"]
        //         partial   = "apple"
        // Expected Output: ["apple pie", "apple juice", "apple"]
        // ------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        String[] sentences2 = {"apple pie", "apple juice", "banana split", "apple"};
        String partial2 = "apple";
        List<String> result2 = solution.autoComplete(sentences2, partial2);
        System.out.println("Input sentences : " + Arrays.toString(sentences2));
        System.out.println("Partial input   : \"" + partial2 + "\"");
        System.out.println("Output          : " + result2);
        // Trace:
        //   Insert "apple pie"   -> a->p->p->l->e->' '->p->i->e
        //                          "apple pie" stored at every node
        //   Insert "apple juice" -> shares a->p->p->l->e, branches at ' '
        //                          "apple juice" stored at every node
        //   Insert "banana split"-> entirely new branch from root
        //   Insert "apple"       -> shares a->p->p->l->e (terminal)
        //                          "apple" stored at every node a,p,p,l,e
        //   Search "apple":
        //     root->'a'->'p'->'p'->'l'->'e'  (node for 'e')
        //     sentences at 'e' node = ["apple pie", "apple juice", "apple"]  ✓
        System.out.println();

        // ------------------------------------------------------------------
        // Example 3: No matches
        // ------------------------------------------------------------------
        System.out.println("=== Example 3: No matches ===");
        String[] sentences3 = {"cat", "car", "card"};
        String partial3 = "dog";
        List<String> result3 = solution.autoComplete(sentences3, partial3);
        System.out.println("Input sentences : " + Arrays.toString(sentences3));
        System.out.println("Partial input   : \"" + partial3 + "\"");
        System.out.println("Output          : " + result3);
        // Expected: []
        System.out.println();

        // ------------------------------------------------------------------
        // Example 4: Duplicate sentences
        // ------------------------------------------------------------------
        System.out.println("=== Example 4: Duplicates ===");
        String[] sentences4 = {"hi there", "hi there", "hi mom"};
        String partial4 = "hi";
        List<String> result4 = solution.autoComplete(sentences4, partial4);
        System.out.println("Input sentences : " + Arrays.toString(sentences4));
        System.out.println("Partial input   : \"" + partial4 + "\"");
        System.out.println("Output          : " + result4);
        // Expected: ["hi there", "hi there", "hi mom"]
        System.out.println();

        // ------------------------------------------------------------------
        // Example 5: Partial equals full sentence
        // ------------------------------------------------------------------
        System.out.println("=== Example 5: Partial equals a full sentence ===");
        String[] sentences5 = {"go", "going", "gone", "stop"};
        String partial5 = "go";
        List<String> result5 = solution.autoComplete(sentences5, partial5);
        System.out.println("Input sentences : " + Arrays.toString(sentences5));
        System.out.println("Partial input   : \"" + partial5 + "\"");
        System.out.println("Output          : " + result5);
        // Expected: ["go", "going", "gone"]
        System.out.println();

        // ------------------------------------------------------------------
        // Example 6: Case-sensitive check
        // ------------------------------------------------------------------
        System.out.println("=== Example 6: Case-sensitive ===");
        String[] sentences6 = {"Hello", "hello", "HELLO"};
        String partial6 = "hel";
        List<String> result6 = solution.autoComplete(sentences6, partial6);
        System.out.println("Input sentences : " + Arrays.toString(sentences6));
        System.out.println("Partial input   : \"" + partial6 + "\"");
        System.out.println("Output          : " + result6);
        // Expected: ["hello"]  (only lowercase "hello" starts with "hel")
        System.out.println();

        // ------------------------------------------------------------------
        // Example 7: Sentences with spaces and punctuation
        // ------------------------------------------------------------------
        System.out.println("=== Example 7: Spaces and punctuation ===");
        String[] sentences7 = {"how are you?", "how do you do?", "however", "now"};
        String partial7 = "how";
        List<String> result7 = solution.autoComplete(sentences7, partial7);
        System.out.println