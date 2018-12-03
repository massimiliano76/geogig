/* Copyright (c) 2012-2016 Boundless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 * Gabriel Roldan (Boundless) - initial implementation
 */
package org.locationtech.geogig.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.locationtech.geogig.model.Bucket;
import org.locationtech.geogig.model.Node;
import org.locationtech.geogig.model.ObjectId;
import org.locationtech.geogig.model.RevObjects;
import org.locationtech.geogig.model.RevTree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMap.Builder;

import lombok.NonNull;

abstract class RevTreeImpl extends AbstractRevObject implements RevTree {

    static final class LeafTree extends RevTreeImpl {

        private final Node[] features;

        private final Node[] trees;

        public LeafTree(final ObjectId id, final long size, final @Nullable Node[] features,
                @Nullable Node[] trees) {
            super(id, size);
            this.features = features;
            this.trees = trees;
        }

        public @Override ImmutableList<Node> features() {
            return features == null ? ImmutableList.of() : ImmutableList.copyOf(features);
        }

        public @Override ImmutableList<Node> trees() {
            return trees == null ? ImmutableList.of() : ImmutableList.copyOf(trees);
        }

        public @Override int numTrees() {
            return trees == null ? 0 : trees.length;
        }

        public @Override int treesSize() {
            return numTrees();// being a leaf tree, numTrees and treesSize are the same
        }

        public @Override Node getTree(int index) {
            return trees[index];
        }

        public @Override void forEachTree(Consumer<Node> consumer) {
            if (trees != null) {
                for (int i = 0; i < trees.length; i++) {
                    consumer.accept(trees[i]);
                }
            }
        }

        public @Override int featuresSize() {
            return features == null ? 0 : features.length;
        }

        public @Override Node getFeature(int index) {
            return features[index];
        }

        public @Override void forEachFeature(Consumer<Node> consumer) {
            if (features != null) {
                for (int i = 0; i < features.length; i++) {
                    consumer.accept(features[i]);
                }
            }
        }

    }

    static final class NodeTree extends RevTreeImpl {

        private final int childTreeCount;

        private final IndexedBucket[] ibuckets;

        public NodeTree(final ObjectId id, final long size, final int childTreeCount,
                final @NonNull SortedMap<Integer, Bucket> innerTrees) {
            super(id, size);
            this.childTreeCount = childTreeCount;
            ArrayList<IndexedBucket> ibucketl = new ArrayList<>(innerTrees.size());
            innerTrees.forEach((i, b) -> ibucketl.add(new IndexedBucket(i, b)));
            this.ibuckets = ibucketl.toArray(new IndexedBucket[ibucketl.size()]);
        }

        private static class IndexedBucket {
            final int index;

            final Bucket bucket;

            IndexedBucket(int index, Bucket b) {
                this.index = index;
                this.bucket = b;
            }
        }

        public @Override ImmutableSortedMap<Integer, Bucket> buckets() {
            Builder<Integer, Bucket> builder = ImmutableSortedMap.naturalOrder();
            for (IndexedBucket ib : this.ibuckets) {
                builder.put(Integer.valueOf(ib.index), ib.bucket);
            }
            return builder.build();
        }

        public @Override int numTrees() {
            return childTreeCount;
        }

        public @Override int bucketsSize() {
            return ibuckets.length;
        }

        public @Override void forEachBucket(BiConsumer<Integer, Bucket> consumer) {
            for (int i = 0; i < ibuckets.length; i++) {
                IndexedBucket indexedBucket = ibuckets[i];
                consumer.accept(Integer.valueOf(indexedBucket.index), indexedBucket.bucket);
            }
        }

        public @Override Optional<Bucket> getBucket(int bucketIndex) {
            int index = Arrays.binarySearch(ibuckets, new IndexedBucket(bucketIndex, null),
                    (b1, b2) -> Integer.compare(b1.index, b2.index));

            return index < 0 ? Optional.empty() : Optional.of(ibuckets[index].bucket);
        }
    }

    private final long size;

    private RevTreeImpl(ObjectId id, long size) {
        super(id);
        this.size = size;
    }

    public final @Override long size() {
        return size;
    }

    public @Override ImmutableList<Node> features() {
        return ImmutableList.of();
    }

    public @Override ImmutableList<Node> trees() {
        return ImmutableList.of();
    }

    public @Override ImmutableSortedMap<Integer, Bucket> buckets() {
        return ImmutableSortedMap.of();
    }

    public @Override String toString() {
        return RevObjects.toString(this);
    }
}