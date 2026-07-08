package com.github.zhifac.crf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by linjiuning on 2020/6/22.
 */
public class Example {
    public List<List<String>> x_;
    public List<List<Node>> node_;
    public List<Integer> answer_;
    public List<Integer> result_;
    public double Z_;
    public double cost_;
    public int feature_id_;
    public PriorityQueue<TaggerImpl.QueueElement> agenda_;
    public List<List<Double>> penalty_;
    public List<List<Integer>> featureCache_;

    public Example() {
        x_ = new ArrayList<>();
        node_ = new ArrayList<>();
        answer_ = new ArrayList<>();
        result_ = new ArrayList<>();
        agenda_ = null;
        penalty_ = new ArrayList<>();
        featureCache_ = new ArrayList<>();
    }
}
