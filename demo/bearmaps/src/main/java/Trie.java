/**
 * Created by ekhumalo on 4/19/16.
 */
public class Trie<Value> {
    private TrieNode<Value> root;

    public void put(String key, Value val) {
        root = put(root, key, val, 0);
    }

    private TrieNode put(TrieNode x, String key, Value val, int d) {
        char aChar = key.charAt(d);
        if (x == null) {
            x = new TrieNode();
            x.aChar = aChar;
        }
        if (aChar < x.aChar) {
            x.left = put(x.left, key, val, d);
        } else if (aChar > x.aChar) {
            x.right = put(x.right, key, val, d);
        } else if (d < key.length() - 1) {
            x.mid = put(x.mid, key, val, d + 1);
        } else {
            x.val = val;
        }
        return x;
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    public Value get(String key) {
        TrieNode<Value> x = get(root, key, 0);
        if (x == null) {
            return null;
        }
        return x.val;
    }

    private TrieNode<Value> get(TrieNode<Value> x, String key, int d) {
        if (x == null) {
            return null;
        }
        char aChar = key.charAt(d);
        if (aChar < x.aChar) {
            return get(x.left, key, d);
        } else if (aChar > x.aChar) {
            return get(x.right, key, d);
        } else if (d < key.length() - 1) {
            return get(x.mid, key, d + 1);
        } else {
            return x;
        }
    }
}
