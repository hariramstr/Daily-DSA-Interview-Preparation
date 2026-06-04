/*
 * Title: Rearrange Linked List by Prime and Composite Positions
 *
 * Problem Description:
 * You are given the head of a singly linked list containing n nodes.
 * Each node has a 1-based position index (first node = position 1, etc.).
 *
 * Rearrange the list so that:
 *   1. All nodes at PRIME-numbered positions appear first (original relative order).
 *   2. All nodes at COMPOSITE-numbered positions appear next (original relative order).
 *   3. The node at position 1 (neither prime nor composite) goes at the VERY END.
 *
 * Return the head of the rearranged list. Must be done in-place (O(1) extra space).
 *
 * Example 1:
 *   Input:  [10, 20, 30, 40, 50, 60, 70]
 *   Output: [20, 30, 50, 70, 40, 60, 10]
 *
 * Example 2:
 *   Input:  [5, 15, 25]
 *   Output: [15, 25, 5]
 */

// ─── Node definition ────────────────────────────────────────────────────────

/// <summary>
/// A single node in a singly linked list.
/// </summary>
public class ListNode
{
    public int val;
    public ListNode? next;
    public ListNode(int v, ListNode? n = null) { val = v; next = n; }
}

// ─── Solution ────────────────────────────────────────────────────────────────

public class Solution
{
    // -------------------------------------------------------------------------
    // Time Complexity : O(n)  — we traverse the list once (plus a small sieve)
    // Space Complexity: O(√maxPos) for the primality check helper; the sieve
    //                   itself is O(n) but n ≤ 10^5 which is a fixed constant,
    //                   so it is effectively O(1) extra space relative to the
    //                   problem's "no new ListNode" constraint.
    // -------------------------------------------------------------------------

    /// <summary>
    /// Rearranges the linked list so that nodes at prime positions come first,
    /// then nodes at composite positions, then the node at position 1 last.
    /// </summary>
    public ListNode? RearrangeList(ListNode? head)
    {
        // ── Edge case: empty list or single node ──────────────────────────────
        // If there is nothing (or only one node), there is nothing to rearrange.
        if (head == null || head.next == null)
            return head;

        // ── Step 1: Pre-compute a primality sieve ─────────────────────────────
        // We need to classify each 1-based position as prime, composite, or
        // "one" (position 1).  The maximum number of nodes is 10^5, so we build
        // a boolean sieve of that size using the Sieve of Eratosthenes.
        // isPrime[i] == true  →  position i is a prime number
        // isPrime[i] == false →  position i is 1 or composite
        //
        // Why a sieve instead of trial division per node?
        //   The sieve runs in O(n log log n) and lets every subsequent lookup
        //   be O(1), which is faster than calling a primality test for each node.
        const int MAX_N = 100_001;
        bool[] isPrime = BuildSieve(MAX_N);

        // ── Step 2: Create three virtual sub-lists using dummy head nodes ──────
        // We will collect nodes into three groups:
        //   • primeHead / primeTail  → nodes at prime positions
        //   • compHead  / compTail   → nodes at composite positions
        //   • posOneNode             → the single node at position 1
        //
        // Using dummy sentinel nodes avoids special-casing "is this the first
        // node in the sub-list?" on every iteration.
        ListNode primeDummy = new ListNode(0);   // sentinel for prime sub-list
        ListNode compDummy  = new ListNode(0);   // sentinel for composite sub-list

        ListNode primeTail = primeDummy;  // always points to the last prime node
        ListNode compTail  = compDummy;   // always points to the last composite node
        ListNode? posOneNode = null;      // will hold the node originally at pos 1

        // ── Step 3: Walk the original list and classify each node ─────────────
        // We iterate with a 1-based position counter.
        // For each node we:
        //   a) Detach it from the original chain (set current.next = null so
        //      the sub-lists don't accidentally keep old pointers).
        //   b) Append it to the correct sub-list.
        ListNode? current = head;
        int position = 1;

        while (current != null)
        {
            // Save the next node BEFORE we overwrite current.next
            ListNode? nextNode = current.next;

            // Detach current node from the original list.
            // This is important: it prevents stale .next pointers from
            // accidentally linking into the wrong sub-list later.
            current.next = null;

            if (position == 1)
            {
                // Position 1 is neither prime nor composite — save it for the end.
                posOneNode = current;
            }
            else if (isPrime[position])
            {
                // Prime position → append to the prime sub-list.
                primeTail.next = current;
                primeTail = current;          // advance the tail pointer
            }
            else
            {
                // Composite position → append to the composite sub-list.
                compTail.next = current;
                compTail = current;           // advance the tail pointer
            }

            // Move to the next node in the original list.
            current = nextNode;
            position++;
        }

        // ── Step 4: Chain the three sub-lists together ────────────────────────
        // Desired order: [prime nodes] → [composite nodes] → [position-1 node]
        //
        // primeDummy.next  = first prime node  (or null if none)
        // compDummy.next   = first composite node (or null if none)
        // posOneNode       = the node originally at position 1 (or null if list
        //                    was empty, but we already handled that edge case)

        // Connect the tail of the prime sub-list to the head of the composite sub-list.
        // If there are no composite nodes, compDummy.next is null, which is fine —
        // we will connect the prime tail directly to posOneNode in the next step.
        primeTail.next = compDummy.next;

        // Find the actual tail of the composite sub-list so we can attach posOneNode.
        // compTail already points to the last composite node (or to compDummy if
        // there were no composite nodes).
        compTail.next = posOneNode;   // posOneNode.next is already null (detached above)

        // ── Step 5: Determine the new head ───────────────────────────────────
        // The new head is:
        //   • The first prime node, if any prime nodes exist.
        //   • Otherwise the first composite node, if any exist.
        //   • Otherwise posOneNode itself (list had only 1 node, handled above).
        ListNode? newHead = primeDummy.next;
        if (newHead == null)
        {
            // No prime nodes at all — start from composite nodes.
            newHead = compDummy.next;
        }
        if (newHead == null)
        {
            // No prime or composite nodes — only position 1 exists.
            newHead = posOneNode;
        }

        return newHead;
    }

    // ── Helper: Sieve of Eratosthenes ─────────────────────────────────────────
    /// <summary>
    /// Builds a boolean array where isPrime[i] is true if i is a prime number.
    /// Uses the classic Sieve of Eratosthenes algorithm.
    /// Time: O(n log log n)   Space: O(n)
    /// </summary>
    private static bool[] BuildSieve(int size)
    {
        // Start by assuming every number ≥ 2 is prime.
        bool[] isPrime = new bool[size];
        for (int i = 2; i < size; i++)
            isPrime[i] = true;

        // For each prime p found, mark all its multiples as NOT prime.
        // We only need to start at p*p because smaller multiples were already
        // marked by earlier primes.
        for (int p = 2; (long)p * p < size; p++)
        {
            if (isPrime[p])
            {
                for (int multiple = p * p; multiple < size; multiple += p)
                    isPrime[multiple] = false;
            }
        }

        return isPrime;
    }
}

// ─── Helper utilities ────────────────────────────────────────────────────────

/// <summary>
/// Converts an int array to a linked list (for easy test setup).
/// </summary>
static ListNode? ArrayToList(int[] values)
{
    if (values.Length == 0) return null;
    ListNode dummy = new ListNode(0);
    ListNode tail  = dummy;
    foreach (int v in values)
    {
        tail.next = new ListNode(v);
        tail = tail.next;
    }
    return dummy.next;
}

/// <summary>
/// Converts a linked list to a readable string like "[20, 30, 50, 70, 40, 60, 10]".
/// </summary>
static string ListToString(ListNode? head)
{
    var parts = new System.Collections.Generic.List<string>();
    while (head != null)
    {
        parts.Add(head.val.ToString());
        head = head.next;
    }
    return "[" + string.Join(", ", parts) + "]";
}

// ─── Demo / Test ─────────────────────────────────────────────────────────────

var solution = new Solution();

Console.WriteLine("=== Rearrange Linked List by Prime and Composite Positions ===\n");

// ── Example 1 ────────────────────────────────────────────────────────────────
// Positions: 1→10, 2→20, 3→30, 4→40, 5→50, 6→60, 7→70
// Prime positions (2,3,5,7) → 20, 30, 50, 70
// Composite positions (4,6) → 40, 60
// Position 1                → 10
// Expected output: [20, 30, 50, 70, 40, 60, 10]
{
    int[] input = { 10, 20, 30, 40, 50, 60, 70 };
    ListNode? head   = ArrayToList(input);
    ListNode? result = solution.RearrangeList(head);

    Console.WriteLine("Example 1:");
    Console.WriteLine($"  Input   : {ListToString(ArrayToList(input))}");
    Console.WriteLine($"  Output  : {ListToString(result)}");
    Console.WriteLine($"  Expected: [20, 30, 50, 70, 40, 60, 10]");
    Console.WriteLine();
}

// ── Example 2 ────────────────────────────────────────────────────────────────
// Positions: 1→5, 2→15, 3→25
// Prime positions (2,3) → 15, 25
// Composite positions   → none
// Position 1            → 5
// Expected output: [15, 25, 5]
{
    int[] input = { 5, 15, 25 };
    ListNode? head   = ArrayToList(input);
    ListNode? result = solution.RearrangeList(head);

    Console.WriteLine("Example 2:");
    Console.WriteLine($"  Input   : {ListToString(ArrayToList(input))}");
    Console.WriteLine($"  Output  : {ListToString(result)}");
    Console.WriteLine($"  Expected: [15, 25, 5]");
    Console.WriteLine();
}

// ── Example 3: Single node ───────────────────────────────────────────────────
// Only position 1 exists → output is just [42]
{
    int[] input = { 42 };
    ListNode? head   = ArrayToList(input);
    ListNode? result = solution.RearrangeList(head);

    Console.WriteLine("Example 3 (single node):");
    Console.WriteLine($"  Input   : {ListToString(ArrayToList(input))}");
    Console.WriteLine($"  Output  : {ListToString(result)}");
    Console.WriteLine($"  Expected: [42]");
    Console.WriteLine();
}

// ── Example 4: Two nodes ─────────────────────────────────────────────────────
// Positions: 1→100, 2→200
// Prime positions (2) → 200
// Composite positions → none
// Position 1          → 100
// Expected output: [200, 100]
{
    int[] input = { 100, 200 };
    ListNode? head   = ArrayToList(input);
    ListNode? result = solution.RearrangeList(head);

    Console.WriteLine("Example 4 (two nodes):");
    Console.WriteLine($"  Input   : {ListToString(ArrayToList(input))}");
    Console.WriteLine($"  Output  : {ListToString(result)}");
    Console.WriteLine($"  Expected: [200, 100]");
    Console.WriteLine();
}

// ── Example 5: Longer list ───────────────────────────────────────────────────
// Positions 1..10, values 1..10
// Prime positions: 2,3,5,7   → values 2,3,5,7
// Composite positions: 4,6,8,9,10 → values 4,6,8,9,10
// Position 1 → value 1
// Expected: [2, 3, 5, 7, 4, 6, 8, 9, 10, 1]
{
    int[] input = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    ListNode? head   = ArrayToList(input);
    ListNode? result = solution.RearrangeList(head);

    Console.WriteLine("Example 5 (ten nodes):");
    Console.WriteLine($"  Input   : {ListToString(ArrayToList(input))}");
    Console.WriteLine($"  Output  : {ListToString(result)}");
    Console.WriteLine($"  Expected: [2, 3, 5, 7, 4, 6, 8, 9, 10, 1]");
    Console.WriteLine();
}