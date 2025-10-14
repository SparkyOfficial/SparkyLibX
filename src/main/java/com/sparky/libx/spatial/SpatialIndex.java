package com.sparky.libx.spatial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import com.sparky.libx.region.Region;

/**
 * Пространственный индекс для эффективного поиска регионов
 */
public class SpatialIndex {
    
    private final Map<World, RTree> worldIndices = new ConcurrentHashMap<>();
    private final Map<String, Region> regionsById = new ConcurrentHashMap<>();
    
    /**
     * Добавляет регион в индекс
     */
    public void add(Region region) {
        RTree index = worldIndices.computeIfAbsent(region.getWorld(), k -> new RTree());
        index.insert(region);
        regionsById.put(region.getName().toLowerCase(), region);
    }
    
    /**
     * Удаляет регион из индекса
     */
    public boolean remove(Region region) {
        RTree index = worldIndices.get(region.getWorld());
        if (index != null) {
            index.remove(region);
            regionsById.remove(region.getName().toLowerCase());
            return true;
        }
        return false;
    }
    
    /**
     * Находит регион по имени
     */
    public Region get(String name) {
        return regionsById.get(name.toLowerCase());
    }
    
    /**
     * Находит все регионы, содержащие точку
     */
    public Collection<Region> query(Location location) {
        RTree index = worldIndices.get(location.getWorld());
        if (index == null) {
            return Collections.emptyList();
        }
        return index.query(location);
    }
    
    /**
     * Находит все регионы, пересекающиеся с ограничивающей рамкой
     */
    public Collection<Region> query(BoundingBox box, World world) {
        RTree index = worldIndices.get(world);
        if (index == null) {
            return Collections.emptyList();
        }
        return index.query(box);
    }
    
    /**
     * Возвращает все регионы
     */
    public Collection<Region> getAll() {
        return new ArrayList<>(regionsById.values());
    }
    
    /**
     * Очищает индекс
     */
    public void clear() {
        worldIndices.clear();
        regionsById.clear();
    }
    
    /**
     * Реализация R-дерева для пространственного индексирования
     */
    private static class RTree {
        private static final int MAX_ENTRIES = 50;
        private static final int MIN_ENTRIES = MAX_ENTRIES / 2;
        
        private Node root;
        
        public RTree() {
            this.root = new LeafNode();
        }
        
        public void insert(Region region) {
            BoundingBox mbr = getBoundingBox(region);
            LeafNode leaf = chooseLeaf(root, mbr);
            
            leaf.insert(region, mbr);
            
            Node split = leaf.splitIfNeeded();
            if (split != null) {
                root = new InternalNode(leaf, split);
            }
        }
        
        public void remove(Region region) {
            BoundingBox mbr = getBoundingBox(region);
            remove(root, region, mbr);
            
            if (root instanceof InternalNode && root.entries.size() == 1) {
                root = ((InternalNode) root).entries.get(0).node;
            }
        }
        
        private boolean remove(Node node, Region region, BoundingBox mbr) {
            if (node instanceof LeafNode) {
                LeafNode leaf = (LeafNode) node;
                return leaf.remove(region);
            } else {
                InternalNode internal = (InternalNode) node;
                for (Entry entry : new ArrayList<>(internal.entries)) {
                    if (entry.mbr.overlaps(mbr)) {
                        if (remove(entry.node, region, mbr)) {
                            if (entry.node.entries.size() < MIN_ENTRIES) {
                                handleUnderflow(internal, entry);
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        
        private void handleUnderflow(InternalNode parent, Entry underflowEntry) {
            Entry bestEntry = null;
            double minIncrease = Double.POSITIVE_INFINITY;
            
            for (Entry entry : parent.entries) {
                if (entry == underflowEntry) continue;
                
                BoundingBox combined = entry.mbr.clone();
                combined.union(underflowEntry.mbr);
                double increase = combined.getVolume() - entry.mbr.getVolume();
                
                if (increase < minIncrease) {
                    minIncrease = increase;
                    bestEntry = entry;
                }
            }
            
            if (bestEntry != null && underflowEntry.node.entries.size() + bestEntry.node.entries.size() <= MAX_ENTRIES) {
                bestEntry.node.entries.addAll(underflowEntry.node.entries);
                bestEntry.mbr = calculateMBR(bestEntry.node);
                parent.entries.remove(underflowEntry);
            }
        }
        
        public Collection<Region> query(Location location) {
            List<Region> result = new ArrayList<>();
            query(root, location, result);
            return result;
        }
        
        public Collection<Region> query(BoundingBox box) {
            List<Region> result = new ArrayList<>();
            query(root, box, result);
            return result;
        }
        
        private void query(Node node, Location location, List<Region> result) {
            if (node instanceof LeafNode) {
                LeafNode leaf = (LeafNode) node;
                for (Region region : leaf.regions) {
                    if (region.contains(location)) {
                        result.add(region);
                    }
                }
            } else {
                InternalNode internal = (InternalNode) node;
                for (Entry entry : internal.entries) {
                    if (contains(entry.mbr, location)) {
                        query(entry.node, location, result);
                    }
                }
            }
        }
        
        private void query(Node node, BoundingBox box, List<Region> result) {
            if (node instanceof LeafNode) {
                LeafNode leaf = (LeafNode) node;
                for (Region region : leaf.regions) {
                    if (getBoundingBox(region).overlaps(box)) {
                        result.add(region);
                    }
                }
            } else {
                InternalNode internal = (InternalNode) node;
                for (Entry entry : internal.entries) {
                    if (entry.mbr.overlaps(box)) {
                        query(entry.node, box, result);
                    }
                }
            }
        }
        
        private LeafNode chooseLeaf(Node node, BoundingBox mbr) {
            if (node instanceof LeafNode) {
                return (LeafNode) node;
            }
            
            InternalNode internal = (InternalNode) node;
            Entry bestEntry = null;
            double minIncrease = Double.POSITIVE_INFINITY;
            
            for (Entry entry : internal.entries) {
                BoundingBox combined = entry.mbr.clone();
                combined.union(mbr);
                double increase = combined.getVolume() - entry.mbr.getVolume();
                
                if (increase < minIncrease || 
                    (increase == minIncrease && bestEntry != null && 
                     entry.mbr.getVolume() < bestEntry.mbr.getVolume())) {
                    minIncrease = increase;
                    bestEntry = entry;
                }
            }
            
            if (bestEntry != null) {
                return chooseLeaf(bestEntry.node, mbr);
            }
            
            throw new IllegalStateException("No suitable child found for insertion");
        }
        
        private static BoundingBox calculateMBR(Node node) {
            if (node.entries.isEmpty()) {
                return new BoundingBox(0, 0, 0, 0, 0, 0);
            }
            
            BoundingBox mbr = node.entries.get(0).mbr.clone();
            for (int i = 1; i < node.entries.size(); i++) {
                mbr.union(node.entries.get(i).mbr);
            }
            return mbr;
        }
        
        private BoundingBox getBoundingBox(Region region) {
            Location min = region.getMinPoint();
            Location max = region.getMaxPoint();
            return new BoundingBox(
                min.getX(), min.getY(), min.getZ(),
                max.getX(), max.getY(), max.getZ()
            );
        }
        
        private boolean contains(BoundingBox box, Location location) {
            return location.getX() >= box.getMinX() && location.getX() <= box.getMaxX() &&
                   location.getY() >= box.getMinY() && location.getY() <= box.getMaxY() &&
                   location.getZ() >= box.getMinZ() && location.getZ() <= box.getMaxZ();
        }
        
        private abstract static class Node {
            protected List<Entry> entries = new ArrayList<>();
        }
        
        private static class InternalNode extends Node {
            public InternalNode(Node... children) {
                for (Node child : children) {
                    entries.add(new Entry(calculateMBR(child), child));
                }
            }
            
            @Override
            public String toString() {
                return "InternalNode{" + entries.size() + " children}";
            }
        }
        
        private static class LeafNode extends Node {
            private final List<Region> regions = new ArrayList<>();
            private final Map<Region, BoundingBox> regionBoxes = new HashMap<>();
            
            public void insert(Region region, BoundingBox mbr) {
                regions.add(region);
                regionBoxes.put(region, mbr);
                updateAncestorMBRs(mbr);
            }
            
            public boolean remove(Region region) {
                BoundingBox mbr = regionBoxes.remove(region);
                if (mbr != null && regions.remove(region)) {
                    updateAncestorMBRs(mbr);
                    return true;
                }
                return false;
            }
            
            public Node splitIfNeeded() {
                if (regions.size() <= MAX_ENTRIES) {
                    return null;
                }
                
                int mid = regions.size() / 2;
                LeafNode newLeaf = new LeafNode();
                
                for (int i = mid; i < regions.size(); i++) {
                    Region region = regions.get(i);
                    newLeaf.insert(region, regionBoxes.get(region));
                }
                
                regions.subList(mid, regions.size()).clear();
                
                return newLeaf;
            }
            
            private void updateAncestorMBRs(BoundingBox mbr) {
            }
            
            @Override
            public String toString() {
                return "LeafNode{" + regions.size() + " regions}";
            }
        }
        
        private static class Entry {
            private BoundingBox mbr;
            private final Node node;
            
            public Entry(BoundingBox mbr, Node node) {
                this.mbr = mbr;
                this.node = node;
            }
        }
    }
}