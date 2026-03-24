package com.marvelwargame.engine;

import com.marvelwargame.model.world.Champion;

/**
 * Min-heap style priority queue ordered by Champion speed (fastest first).
 * Backed by a simple sorted insertion array.
 */
public final class PriorityQueue {

    private final Champion[] elements;
    private int size;

    public PriorityQueue(int capacity) {
        elements = new Champion[capacity];
        size = 0;
    }

    /** Insert in descending speed order (fastest at the back, peekMin returns fastest). */
    public void insert(Champion c) {
        int i;
        for (i = size - 1; i >= 0 && c.compareTo(elements[i]) > 0; i--) {
            elements[i + 1] = elements[i];
        }
        elements[i + 1] = c;
        size++;
    }

    /** Remove and return the champion with the highest speed (next to act). */
    public Champion remove() {
        return elements[--size];
    }

    /** Peek at the champion scheduled to act next without removing. */
    public Champion peekMin() {
        return elements[size - 1];
    }

    public boolean isEmpty() { return size == 0; }
    public int size()        { return size; }

    public void remove(Champion c) {
        for (int i = 0; i < size; i++) {
            if (elements[i] == c) {
                System.arraycopy(elements, i + 1, elements, i, size - i - 1);
                size--;
                return;
            }
        }
    }
}
