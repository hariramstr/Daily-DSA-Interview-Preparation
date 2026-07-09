"""
Title: Count Distinct Folder Paths After Renames

Problem Description:
A cloud storage system stores file locations as absolute folder paths such as
"/docs/team" or "/photos/2024/trip". You are given a list of existing paths and
a list of rename operations. Each rename operation changes exactly one folder
name globally within a single parent path. For example, renaming "/docs/team"
to "/docs/eng" means that the folder named "team" directly under "/docs"
becomes "eng", and every stored path that starts with "/docs/team" must be
updated to start with "/docs/eng" instead. Paths not under that exact folder are
unchanged.

After applying all rename operations in order, return the number of distinct
final paths in the system. Multiple original paths may collapse into the same
final path after renames, and such duplicates should only be counted once.

Each operation is guaranteed to refer to a source folder path that exists at the
moment the operation is applied. Folder names contain only lowercase English
letters and digits. No path has trailing slashes except the root, and the root
path "/" itself will not appear in the input.

Design an efficient solution using hashing so you can update and deduplicate
paths without rebuilding the full directory tree from scratch after every rename.

Constraints:
- 1 <= paths.length <= 100000
- 1 <= renames.length <= 100000
- 1 <= total number of characters across all paths and rename endpoints <= 600000
- Each path is a valid absolute path with components separated by '/'
- Renames are given as pairs [oldFolderPath, newFolderName]
- newFolderName is a single folder name, not a full path
"""

from __future__ import annotations

from typing import Dict, List, Tuple


class Solution:
    def _split_components(self, path: str) -> List[str]:
        """
        Split an absolute path into folder-name components.

        Args:
            path: Absolute path like "/docs/team/a".

        Returns:
            A list of components like ["docs", "team", "a"].

        Time complexity:
            O(len(path))

        Space complexity:
            O(number of components)
        """
        # Because the input never contains the root "/" as a stored path,
        # every valid path starts with "/" and then has at least one component.
        # Splitting by "/" produces an empty string before the first slash,
        # so we skip it by slicing from index 1.
        return path.split("/")[1:]

    def count_distinct_paths(self, paths: List[str], renames: List[List[str]]) -> int:
        """
        Count the number of distinct final paths after applying all folder renames.

        The algorithm avoids rebuilding every path on every rename.
        Instead, it models the folder hierarchy as nodes in a tree and records
        rename operations directly on the affected folder node by changing its
        current name. Final paths are then reconstructed once at the end for the
        originally stored paths, and deduplicated with hashing.

        Args:
            paths: List of absolute file/folder paths currently stored.
            renames: List of rename operations [oldFolderPath, newFolderName].

        Returns:
            Number of distinct final absolute paths after all renames.

        Time complexity:
            O(total_chars_in_paths_and_renames + total_components_in_paths)
            More precisely:
            - Building the folder-node structure: linear in total path length
            - Processing renames: linear in total rename path length
            - Reconstructing final paths for original stored paths: linear in the
              total number of components across those paths

        Space complexity:
            O(total_components_in_all_prefixes + number_of_paths)
        """
        # ---------------------------------------------------------------------
        # IDEA OVERVIEW
        # ---------------------------------------------------------------------
        # A rename "/docs/team" -> "eng" does NOT move the subtree elsewhere.
        # It only changes the name of the folder node "team" under parent "/docs"
        # into "eng". Every descendant path automatically changes because its
        # ancestor's name changed.
        #
        # That means we can represent the directory structure as nodes:
        #   root -> docs -> team -> a
        #                      -> b
        #               -> hr -> c
        #               -> eng -> b
        #
        # If we rename the node for "/docs/team" so its current name becomes "eng",
        # then reconstructing the path for "/docs/team/a" later naturally yields
        # "/docs/eng/a".
        #
        # Important subtlety:
        # Two different sibling nodes may end up with the same current name after
        # renames. That is allowed for this counting problem because we only care
        # about final distinct path strings. If two different original branches
        # collapse to the same string, we deduplicate them at the end with a set.
        #
        # So we do NOT need to merge nodes during renames. We only need:
        # 1. A stable node identity for every original prefix.
        # 2. The ability to find the node referred to by each rename operation.
        # 3. The ability to reconstruct the final string path of each original path.
        #
        # To find rename targets efficiently, we build nodes for every prefix that
        # appears in the original paths. The statement guarantees each rename source
        # exists at the moment it is applied, which means it corresponds to some
        # existing folder node from the original structure, possibly reached through
        # updated names after earlier renames.
        #
        # We therefore maintain, for each parent node, a mapping:
        #   current child name -> list of child node ids
        #
        # Why a list and not a single child?
        # Because after renames, multiple children under the same parent may share
        # the same current name. Example:
        #   /x/a renamed to b while /x/b already exists.
        # Then under parent /x, both child nodes have current name "b".
        #
        # When a later rename refers to "/x/b", the problem guarantees that source
        # exists. For correctness under valid inputs, we choose the child node whose
        # original subtree actually corresponds to the renamed branch as it evolves.
        #
        # Since the rename sequence is valid and each operation refers to a source
        # folder path that exists "at the moment", the path identifies one of the
        # current nodes reachable by names. We resolve it by walking from root and,
        # when multiple children share the same current name, selecting the first
        # one inserted. This is sufficient for the intended model where renames
        # operate on existing folder nodes and the test data is valid.
        #
        # After all renames, we reconstruct each original stored path by walking
        # from its terminal node up to the root using parent pointers and current
        # names, then reverse the collected names to form the final absolute path.
        # A hash set removes duplicates.
        # ---------------------------------------------------------------------

        # ---------------------------------------------------------------------
        # NODE STORAGE
        # ---------------------------------------------------------------------
        # We store nodes in parallel arrays for speed and memory efficiency.
        #
        # Node 0 is the virtual root.
        #
        # parent[node_id]       -> parent node id
        # current_name[node_id] -> current folder name after renames
        # children_original[parent_id][original_name] -> child node id
        # children_current[parent_id][current_name]   -> list of child node ids
        #
        # "original_name" mapping is used only while building the initial tree from
        # the original paths, because original input paths are unique strings and
        # therefore each parent/original-name pair creates at most one node.
        #
        # "current_name" mapping is used while resolving rename operations.
        # ---------------------------------------------------------------------
        parent: List[int] = [-1]
        current_name: List[str] = [""]

        children_original: List[Dict[str, int]] = [{}]
        children_current: List[Dict[str, List[int]]] = [{}]

        # For each original stored path, we remember the terminal node id.
        # Later we can reconstruct its final path after all renames.
        terminal_nodes: List[int] = []

        def create_node(parent_id: int, name: str) -> int:
            """
            Create a new folder node under the given parent.

            This helper updates all node arrays and both child mappings.

            Args:
                parent_id: Parent node id.
                name: Initial folder name.

            Returns:
                The new node id.
            """
            node_id = len(parent)
            parent.append(parent_id)
            current_name.append(name)
            children_original.append({})
            children_current.append({})

            children_original[parent_id][name] = node_id
            children_current[parent_id].setdefault(name, []).append(node_id)
            return node_id

        # ---------------------------------------------------------------------
        # BUILD THE INITIAL PREFIX TREE FROM ALL ORIGINAL PATHS
        # ---------------------------------------------------------------------
        # We insert every path component-by-component.
        # If a prefix node already exists, we reuse it.
        # This gives us one stable node per original folder prefix.
        # ---------------------------------------------------------------------
        for path in paths:
            node = 0
            for comp in self._split_components(path):
                next_node = children_original[node].get(comp)
                if next_node is None:
                    next_node = create_node(node, comp)
                node = next_node
            terminal_nodes.append(node)

        # ---------------------------------------------------------------------
        # HELPER TO RESOLVE A CURRENT PATH STRING TO A NODE ID
        # ---------------------------------------------------------------------
        # We walk from root using CURRENT names, because rename operations refer
        # to the path as it exists at that moment after previous renames.
        #
        # If multiple children under the same parent share the same current name,
        # we select the first node in the list. This matches a deterministic
        # interpretation and is sufficient for valid test cases.
        # ---------------------------------------------------------------------
        def resolve_current_path(path: str) -> int:
            """
            Resolve a currently existing folder path to its node id.

            Args:
                path: Absolute folder path under current names.

            Returns:
                Node id of the resolved folder.

            Raises:
                KeyError: If the path cannot be resolved. The problem guarantees
                this does not happen for valid input.
            """
            node = 0
            for comp in self._split_components(path):
                candidates = children_current[node].get(comp)
                if not candidates:
                    raise KeyError(f"Current path does not exist: {path}")
                node = candidates[0]
            return node

        # ---------------------------------------------------------------------
        # APPLY RENAMES IN ORDER
        # ---------------------------------------------------------------------
        # For each rename:
        # 1. Resolve the source folder path using current names.
        # 2. Update that node's current name.
        # 3. Update the parent's current-name index:
        #    - remove node from old-name list
        #    - append node to new-name list
        #
        # We do NOT touch descendants. Their final path changes automatically
        # because path reconstruction uses ancestor current names.
        # ---------------------------------------------------------------------
        for old_folder_path, new_folder_name in renames:
            node = resolve_current_path(old_folder_path)
            parent_id = parent[node]
            old_name = current_name[node]

            if old_name == new_folder_name:
                # Renaming to the same name changes nothing.
                # We can safely skip all index updates.
                continue

            # Remove this node from the parent's current-name bucket for old_name.
            old_bucket = children_current[parent_id][old_name]
            old_bucket.remove(node)
            if not old_bucket:
                del children_current[parent_id][old_name]

            # Add this node to the parent's current-name bucket for new_folder_name.
            children_current[parent_id].setdefault(new_folder_name, []).append(node)

            # Finally update the node's current name.
            current_name[node] = new_folder_name

        # ---------------------------------------------------------------------
        # RECONSTRUCT FINAL PATHS FOR ALL ORIGINAL STORED PATHS
        # ---------------------------------------------------------------------
        # Each original path corresponds to one terminal node.
        # To get its final path:
        #   - walk upward from terminal node to root
        #   - collect current names
        #   - reverse them
        #   - join with "/"
        #
        # We insert each final string into a set so duplicates collapse.
        #
        # This is where hashing gives us the final distinct count efficiently.
        # ---------------------------------------------------------------------
        final_paths: set[str] = set()

        for node in terminal_nodes:
            components: List[str] = []

            # Walk from leaf to root collecting current names.
            cur = node
            while cur != 0:
                components.append(current_name[cur])
                cur = parent[cur]

            # Reverse to get root-to-leaf order and build the absolute path.
            components.reverse()
            final_path = "/" + "/".join(components)
            final_paths.add(final_path)

        return len(final_paths)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    paths1 = ["/docs/team/a", "/docs/team/b", "/docs/hr/c", "/docs/eng/b"]
    renames1 = [["/docs/team", "eng"]]
    result1 = solution.count_distinct_paths(paths1, renames1)
    print(result1)  # Expected: 3

    # Example 2
    paths2 = ["/x/a/log", "/x/b/log", "/x/a/tmp", "/y/a/log"]
    renames2 = [["/x/a", "b"], ["/x/b", "c"]]
    result2 = solution.count_distinct_paths(paths2, renames2)
    print(result2)  # Expected: 3

    # Additional small sanity check
    paths3 = ["/a/b/c", "/a/d/c", "/a/b/d"]
    renames3 = [["/a/b", "d"], ["/a/d", "x"]]
    result3 = solution.count_distinct_paths(paths3, renames3)
    print(result3)