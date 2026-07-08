package com.netease.easyml.local.mllib;

import com.netease.easyml.common.collection.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Created by eddielin on 2018/6/3.
 */
public class Graph<T> implements Serializable {
    private static Logger log = LoggerFactory.getLogger(Graph.class);
    private Map<T, List<T>> nodeNeighbors;
    private Map<Tuple<T, T>, Double> edgeWeights;

    public Graph(Map<T, List<T>> nodeNeighbors, Map<Tuple<T, T>, Double> edgeWeights) {
        this.nodeNeighbors = nodeNeighbors;
        this.edgeWeights = edgeWeights;
    }

    public Graph() {
        this(new HashMap<>(), new HashMap<>());
    }

    public List<T> getNodes() {
        return Collections.unmodifiableList(new ArrayList<>(nodeNeighbors.keySet()));
    }

    public List<Tuple<T, T>> getEdges() {
        return Collections.unmodifiableList(new ArrayList<>(edgeWeights.keySet()));
    }

    public List<T> getNeighbors(T node) {
        if (hasNode(node)) {
            return nodeNeighbors.get(node);
        } else {
            log.error(String.format("Node: %s don't exist...", node.toString()));
            return Collections.emptyList();
        }
    }

    public double getWeight(Tuple<T, T> edge) {
        if (hasEdge(edge)) {
            return edgeWeights.get(edge);
        } else {
//            log.warn(String.format("Edge: %s don't exist...", edge.toString()));
            return 0.0;
        }
    }

    public Graph addNode(T node) {
        if (!nodeNeighbors.containsKey(node))
            nodeNeighbors.put(node, new ArrayList<>());
        return this;
    }

    public Graph addEdge(Tuple<T, T> edge, double weight) {
        if (edgeWeights.containsKey(edge)) {
            log.debug(String.format("edge: %s already exist...", edge));
            return this;
        }
        T node1 = edge.v1();
        T node2 = edge.v2();
        addNode(node1);
        addNode(node2);
        if (!(getNeighbors(node1).contains(node2) || getNeighbors(node2).contains(node1))) {
            getNeighbors(node1).add(node2);
            edgeWeights.put(edge, weight);
            if (!node1.equals(node2)) {
                getNeighbors(node2).add(node1);
                edgeWeights.put(Tuple.tuple(node2, node1), weight);
            }
        } else {
            log.error(String.format("Edge: %s already exist...", edge.toString()));
        }
        return this;
    }

    public Graph addEdge(T node1, T node2, double weight) {
        return addEdge(Tuple.tuple(node1, node2), weight);
    }

    public boolean hasNode(T node) {
        return nodeNeighbors.containsKey(node);
    }

    public boolean hasEdge(Tuple<T, T> edge) {
        return edgeWeights.containsKey(edge) && edgeWeights.containsKey(Tuple.tuple(edge.v2(), edge.v1()));
    }

    /*
    public Graph delNode(T node) {
        if (hasNode(node)) {
            for (T n : getNeighbors(node)) {
                edgeWeights.remove(Tuple.tuple(n, node));
                if (!n.equals(node)) {
                    nodeNeighbors.get(n).remove(node);
                    edgeWeights.remove(new Tuple<>(node, n));
                }
//              delEdge(Tuple.tuple(n, node));
            }
            nodeNeighbors.remove(node);
        }
        return this;
    }
    */

    public Graph delNode(T node) {
        if (hasNode(node)) {
            for (T n : getNeighbors(node)) {
                edgeWeights.remove(Tuple.tuple(n, node));
                if (!n.equals(node)) {
                    nodeNeighbors.get(n).remove(node);
                    edgeWeights.remove(new Tuple<>(node, n));
                }
//              delEdge(Tuple.tuple(n, node));
            }
            nodeNeighbors.remove(node);
        }
        return this;
    }

    public Graph delEdge(Tuple<T, T> edge) {
        T node1 = edge.v1();
        T node2 = edge.v2();
        if (hasNode(node1) && hasNode(node2)) {
            nodeNeighbors.get(node1).remove(node2);
            edgeWeights.remove(edge);
            if (!node1.equals(node2)) {
                nodeNeighbors.get(node2).remove(node1);
                edgeWeights.remove(new Tuple<>(node2, node1));
            }
        } else {
            log.error(String.format("Edge: %s don't exist...", edge.toString()));
        }
        return this;
    }

    public Graph delEdge(T node1, T node2) {
        return delEdge(Tuple.tuple(node1, node2));
    }
}
