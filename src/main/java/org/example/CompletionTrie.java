package org.example;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeMap;

public class CompletionTrie {
    final TrieNode root = new TrieNode("");

    void insert(String word) {
        root.put(new StringCharacterIterator(word));
    }

    void insertAll(String... words) {
        for (var word : words) {
            this.insert(word);
        }
    }

    void delete(String word) {
        root.remove(new StringCharacterIterator(word));
    }

    ArrayList<String> search(String word) {
        return root.get(word);
    }
}

class TrieNode {
    final TreeMap<Character, TrieNode> children;
    boolean isEnd = false;
    final String prefix;

    public TrieNode(String prefix) {
        this.children = new TreeMap<>();
        this.prefix = prefix;
    }

    public void put(CharacterIterator text) {
        var current_node = this;
        StringBuilder prefix = new StringBuilder();
        var character = text.current();
        while (character != CharacterIterator.DONE) {
            prefix.append(character);
            if (!current_node.children.containsKey(character)) {
                current_node.children.put(character, new TrieNode(prefix.toString()));
            }
            current_node = current_node.children.get(character);
            character = text.next();
        }
        current_node.isEnd = true;
    }

    public ArrayList<String> get(String prefix) {
        var current_node = this;
        for (var character : prefix.toCharArray()) {
            if (!current_node.children.containsKey(character)) {
                return new ArrayList<>();
            }
            current_node = current_node.children.get(character);
        }
        ArrayList<String> result = new ArrayList<>();
        if (current_node.isEnd) {
            result.add(current_node.prefix);
        }
        current_node.getAllChildWords(result);
        return result;
    }

    public Optional<String> remove(CharacterIterator word) {
        var character = word.current();
        if (character == CharacterIterator.DONE) {
            if (this.isEnd) {
                this.isEnd = false;
                return Optional.of(this.prefix);
            }
            return Optional.empty();
        }
        if (!this.children.containsKey(character)) {
            return Optional.empty();
        }
        var child = this.children.get(character);
        word.next();
        var result = child.remove(word);
        if (result.isPresent()) {
            if (child.children.isEmpty()) {
                this.children.remove(character);
            }
        }
        return result;
    }

    public void getAllChildWords(ArrayList<String> result) {
        for (var child : this.children.values()) {
            if (child.isEnd) {
                result.add(child.prefix);
            }
            child.getAllChildWords(result);
        }
    }
}
