package com.thealgorithms.datastructures.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class KDTreeDeletionTest {
    @Test
    void deleteLeavesOnBothSides() {
        for (int coordinate : new int[] {10, 30}) {
            List<KDTree.Point> points = pointsOf(20, 10, 30);
            KDTree tree = insertedTree(points);
            KDTree.Point deleted = pointOf(coordinate);
            tree.delete(deleted);
            points.remove(deleted);
            assertContents(tree, points);
            assertFalse(tree.search(deleted).isPresent());
        }
    }

    @Test
    void deleteNonRootWithDescendants() {
        for (int coordinate : new int[] {10, 30}) {
            List<KDTree.Point> points = pointsOf(20, 10, 30, 5, 15, 25, 35, 13, 27);
            KDTree tree = insertedTree(points);
            KDTree.Point deleted = pointOf(coordinate);
            tree.delete(deleted);
            points.remove(deleted);
            assertContents(tree, points);
            assertFalse(tree.search(deleted).isPresent());
        }
    }

    @Test
    void deleteRootWithOnlyLeftSubtree() {
        List<KDTree.Point> points = pointsOf(20, 10, 15, 5);
        KDTree tree = insertedTree(points);
        KDTree.Point deleted = points.removeFirst();
        tree.delete(deleted);
        assertContents(tree, points);
        assertFalse(tree.search(deleted).isPresent());
        KDTree.Point inserted = pointOf(12);
        tree.insert(inserted);
        points.add(inserted);
        assertContents(tree, points);
        tree.delete(inserted);
        points.remove(inserted);
        assertContents(tree, points);
    }

    @Test
    void deleteRootWithNestedRightSuccessor() {
        List<KDTree.Point> points = pointsOf(20, 30, 25, 27, 40);
        KDTree tree = insertedTree(points);
        KDTree.Point deleted = points.removeFirst();
        tree.delete(deleted);
        assertContents(tree, points);
        assertFalse(tree.search(deleted).isPresent());
        KDTree.Point successor = pointOf(25);
        assertEquals(successor, tree.getRoot().getPoint());
        tree.delete(successor);
        points.remove(successor);
        assertContents(tree, points);
        assertFalse(tree.search(successor).isPresent());
    }

    @Test
    void deletingAbsentPointPreservesContents() {
        List<KDTree.Point> points = pointsOf(20, 10, 30);
        KDTree tree = insertedTree(points);
        assertThrows(IllegalArgumentException.class, () -> tree.delete(pointOf(15)));
        assertContents(tree, points);
        for (KDTree.Point point : List.copyOf(points)) {
            tree.delete(point);
            points.remove(point);
            assertContents(tree, points);
        }
        assertNull(tree.getRoot());
        assertThrows(IllegalArgumentException.class, () -> tree.delete(pointOf(20)));
        assertNull(tree.getRoot());
    }

    @Test
    void deletionOrdersPreserveBuiltAndInsertedTrees() {
        for (int dimension : new int[] {1, 2, 3}) {
            List<KDTree.Point> initial = new ArrayList<>();
            for (int index = 0; index < 15; index++) {
                int[] coordinates = new int[dimension];
                for (int axis = 0; axis < dimension; axis++) {
                    coordinates[axis] = ((index * (axis + 1)) % 17) - 8;
                }
                initial.add(new KDTree.Point(coordinates));
            }
            for (boolean bulkBuild : new boolean[] {false, true}) {
                for (int shift : new int[] {0, 4, 9}) {
                    List<KDTree.Point> remaining = new ArrayList<>(initial);
                    List<KDTree.Point> order = new ArrayList<>(initial);
                    Collections.rotate(order, shift);
                    KDTree tree = bulkBuild ? new KDTree(initial.toArray(KDTree.Point[] ::new)) : insertedTree(initial);
                    assertContents(tree, remaining);
                    for (KDTree.Point point : order) {
                        tree.delete(point);
                        remaining.remove(point);
                        assertContents(tree, remaining);
                        assertFalse(tree.search(point).isPresent());
                    }
                    assertNull(tree.getRoot());
                }
            }
        }
    }

    private static KDTree.Point pointOf(int coordinate) {
        return new KDTree.Point(new int[] {coordinate, coordinate});
    }

    private static List<KDTree.Point> pointsOf(int... coordinates) {
        List<KDTree.Point> points = new ArrayList<>();
        for (int coordinate : coordinates) {
            points.add(pointOf(coordinate));
        }
        return points;
    }

    private static KDTree insertedTree(List<KDTree.Point> points) {
        KDTree tree = new KDTree(points.getFirst().getDimension());
        for (KDTree.Point point : points) {
            tree.insert(point);
        }
        return tree;
    }

    private static void assertContents(KDTree tree, List<KDTree.Point> expected) {
        List<KDTree.Point> actual = new ArrayList<>();
        collectPoints(tree.getRoot(), actual);
        assertEquals(expected.size(), actual.size());
        assertEquals(Set.copyOf(expected), Set.copyOf(actual));
        for (KDTree.Point point : expected) {
            assertTrue(tree.search(point).isPresent(), () -> "Surviving point is unreachable: " + point);
        }
        if (!expected.isEmpty()) {
            for (int axis = 0; axis < expected.getFirst().getDimension(); axis++) {
                int minimum = Integer.MAX_VALUE;
                int maximum = Integer.MIN_VALUE;
                for (KDTree.Point point : expected) {
                    minimum = Math.min(minimum, point.getCoordinate(axis));
                    maximum = Math.max(maximum, point.getCoordinate(axis));
                }
                assertEquals(minimum, tree.findMin(axis).getCoordinate(axis));
                assertEquals(maximum, tree.findMax(axis).getCoordinate(axis));
            }
        }
    }

    private static void collectPoints(KDTree.Node node, List<KDTree.Point> points) {
        if (node != null) {
            points.add(node.getPoint());
            collectPoints(node.getLeft(), points);
            collectPoints(node.getRight(), points);
        }
    }
}
