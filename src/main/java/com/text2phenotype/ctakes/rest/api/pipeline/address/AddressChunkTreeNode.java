package com.text2phenotype.ctakes.rest.api.pipeline.address;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.CONST;
import org.apache.ctakes.dictionary.lookup2.textspan.TextSpan;

public class AddressChunkTreeNode implements Comparable<AddressChunkTreeNode> {

    private AddressChunk data;
    private TreeSet<AddressChunkTreeNode> next = new TreeSet<>();

    public AddressChunkTreeNode(AddressChunk chunk) {
        if (chunk == null){
            throw new NullPointerException();
        }
        this.data = chunk;
    }

    public AddressChunk getData() {
        return data;
    }

    public Iterable<AddressChunkTreeNode> getNext() {
        return next;
    }

    public boolean addChunkNode(AddressChunkTreeNode node) {
        boolean result = false;
        // node stay before than current node -> -1
        if (this.compareTo(node) < 0) {
            for (AddressChunkTreeNode nextNode: next) {
                result = result | nextNode.addChunkNode(node);
            }

            if (!result &&
                    node.data.getSpan().getStart() - data.getSpan().getStart() <= CONST.MAX_TOKENS_DISTANCE_BETWEEN_ADDRESSES) {
                // remove all nodes which are part of the node.
                TextSpan nodeDataSpan = node.data.getSpan();
                next.removeIf(nextNode ->
                        nextNode.data.getSpan().getStart() >= nodeDataSpan.getStart() &&
                        nextNode.data.getSpan().getEnd() <= nodeDataSpan.getEnd()
                );
                next.add(node);
                result = true;
            }
        }
        return result;
    }

    public List<List<AddressChunkTreeNode>> getBranches() {
        List<List<AddressChunkTreeNode>> result = new ArrayList<>();
        if (next.size() > 0) {
            for (AddressChunkTreeNode nextNode : next) {
                result.addAll(nextNode.getBranches());
            }

        } else {
            result.add(new ArrayList<>());
        }
        result.forEach(l -> l.add(0,this));
        return result;
    }

    /**
     * Do compatison.
     * 0 - One of them totally overlaps others and they have the same chunk type.
     * 1 - 'y' span stays after 'x'
     * -1 - otherwise
     * @param y
     * @return
     */
    @Override
    public int compareTo(AddressChunkTreeNode y) {
        String xType = data.getChunk().getChunkType();
        String yType = y.data.getChunk().getChunkType();
        int xStart = data.getSpan().getStart();
        int xEnd = data.getSpan().getEnd();
        int yStart = y.data.getSpan().getStart();
        int yEnd = y.data.getSpan().getEnd();

        if (xType.equals(yType)) {
            if (xStart>=yStart && xEnd <= yEnd)
                return 0;

            if (yStart>=xStart && yEnd <= xEnd)
                return 0;
        }

        if (yStart >= xEnd) {
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        return String.format("%s [%d:%d]", this.data.getChunk().getCoveredText(), this.data.getSpan().getStart(), this.data.getSpan().getEnd());
    }
}
