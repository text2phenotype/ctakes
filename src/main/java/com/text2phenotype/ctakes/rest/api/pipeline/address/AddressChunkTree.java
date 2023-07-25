package com.text2phenotype.ctakes.rest.api.pipeline.address;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddressChunkTree {

    private List<AddressChunkTreeNode> nodes = new ArrayList<>();
    public AddressChunkTree() {

    }

    public void addChunk(final AddressChunk chunk) {
        final AddressChunkTreeNode newNode = new AddressChunkTreeNode(chunk);
        boolean isAdded = false;
        for (AddressChunkTreeNode node : nodes) {
            isAdded = isAdded | node.addChunkNode(newNode);
        }

        if (!isAdded) {
            nodes.add(newNode);
        }
    }

    public List<List<AddressChunk>> getBranches() {
        List<List<AddressChunkTreeNode>> result = new ArrayList<>();
        for (AddressChunkTreeNode node : nodes) {
            result.addAll(node.getBranches());
        }
        return result
                .stream()
                .map(list -> list.stream()
                        .map(AddressChunkTreeNode::getData)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
