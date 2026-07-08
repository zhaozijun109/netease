package com.github.zhifac.crf4j;

import java.io.*;
import java.util.*;

/**
 * Created by zhifac on 2017/3/19.
 */
public class TaggerImpl extends Tagger {
    class QueueElement {
        Node node;
        QueueElement next;
        double fx;
        double gx;
    }

    public enum Mode {TEST, LEARN}

    public enum ReadStatus {SUCCESS, EOF, ERROR}

    Mode mode_ = Mode.TEST;
    int vlevel_ = 0;
    int nbest_ = 0;
    int ysize_;
    double cost_;
    double Z_;
    int feature_id_;
    int thread_id_;
    FeatureIndex feature_index_;
    List<List<String>> x_;
    List<List<Node>> node_;
    List<Integer> answer_;
    List<Integer> result_;
    String lastError;
    PriorityQueue<QueueElement> agenda_;
    List<List<Double>> penalty_;
    List<List<Integer>> featureCache_;

    public TaggerImpl(Mode mode) {
        mode_ = mode;
        vlevel_ = 0;
        nbest_ = 0;
        ysize_ = 0;
        Z_ = 0;
        feature_id_ = 0;
        thread_id_ = 0;
        lastError = null;
        feature_index_ = null;
        x_ = new ArrayList<List<String>>();
        node_ = new ArrayList<List<Node>>();
        answer_ = new ArrayList<Integer>();
        result_ = new ArrayList<Integer>();
        agenda_ = null;
        penalty_ = new ArrayList<List<Double>>();
        featureCache_ = new ArrayList<List<Integer>>();
    }

    public void clearNodes() {
        if (node_ != null && !node_.isEmpty()) {
            for (List<Node> n : node_) {
                for (int i = 0; i < n.size(); i++) {
                    if (n.get(i) != null) {
                        n.get(i).clear();
                        n.set(i, null);
                    }
                }
            }
        }
    }

    public void setPenalty(int i, int j, double penalty) {
        if (penalty_.isEmpty()) {
            for (int s = 0; s < node_.size(); s++) {
                List<Double> penaltys = Arrays.asList(new Double[ysize_]);
                penalty_.add(penaltys);
            }
        }
        penalty_.get(i).set(j, penalty);
    }

    public double penalty(int i, int j) {
        return penalty_.isEmpty() ? 0.0 : penalty_.get(i).get(j);
    }

    public void forwardbackward() {
        if (!x_.isEmpty()) {
            for (int i = 0; i < x_.size(); i++) {
                for (int j = 0; j < ysize_; j++) {
                    node_.get(i).get(j).calcAlpha();
                }
            }
            for (int i = x_.size() - 1; i >= 0; i--) {
                for (int j = 0; j < ysize_; j++) {
                    node_.get(i).get(j).calcBeta();
                }
            }
            Z_ = 0.0;
            for (int j = 0; j < ysize_; j++) {
                Z_ = Node.logsumexp(Z_, node_.get(0).get(j).beta, j == 0);
            }
        }
    }

    public void forwardbackward(Example example) {
        if (!example.x_.isEmpty()) {
            for (int i = 0; i < example.x_.size(); i++) {
                for (int j = 0; j < ysize_; j++) {
                    example.node_.get(i).get(j).calcAlpha();
                }
            }
            for (int i = example.x_.size() - 1; i >= 0; i--) {
                for (int j = 0; j < ysize_; j++) {
                    example.node_.get(i).get(j).calcBeta();
                }
            }
            example.Z_ = 0.0;
            for (int j = 0; j < ysize_; j++) {
                example.Z_ = Node.logsumexp(example.Z_, example.node_.get(0).get(j).beta, j == 0);
            }
        }
    }

    public void viterbi() {
        for (int i = 0; i < x_.size(); i++) {
            for (int j = 0; j < ysize_; j++) {
                double bestc = -1e37;
                Node best = null;
                List<Path> lpath = node_.get(i).get(j).lpath;
                for (Path p : lpath) {
                    double cost = p.lnode.bestCost + p.cost + node_.get(i).get(j).cost;
                    if (cost > bestc) {
                        bestc = cost;
                        best = p.lnode;
                    }
                }
                node_.get(i).get(j).prev = best;
                node_.get(i).get(j).bestCost = best != null ? bestc : node_.get(i).get(j).cost;
            }
        }
        double bestc = -1e37;
        Node best = null;
        int s = x_.size() - 1;
        for (int j = 0; j < ysize_; j++) {
            if (bestc < node_.get(s).get(j).bestCost) {
                best = node_.get(s).get(j);
                bestc = node_.get(s).get(j).bestCost;
            }
        }
        for (Node n = best; n != null; n = n.prev) {
            result_.set(n.x, n.y);
        }
        cost_ = -node_.get(x_.size() - 1).get(result_.get(x_.size() - 1)).bestCost;
    }

    public void viterbi(Example example) {
        for (int i = 0; i < example.x_.size(); i++) {
            for (int j = 0; j < ysize_; j++) {
                double bestc = -1e37;
                Node best = null;
                List<Path> lpath = example.node_.get(i).get(j).lpath;
                for (Path p : lpath) {
                    double cost = p.lnode.bestCost + p.cost + example.node_.get(i).get(j).cost;
                    if (cost > bestc) {
                        bestc = cost;
                        best = p.lnode;
                    }
                }
                example.node_.get(i).get(j).prev = best;
                example.node_.get(i).get(j).bestCost = best != null ? bestc : example.node_.get(i).get(j).cost;
            }
        }
        double bestc = -1e37;
        Node best = null;
        int s = example.x_.size() - 1;
        for (int j = 0; j < ysize_; j++) {
            if (bestc < example.node_.get(s).get(j).bestCost) {
                best = example.node_.get(s).get(j);
                bestc = example.node_.get(s).get(j).bestCost;
            }
        }
        for (Node n = best; n != null; n = n.prev) {
            example.result_.set(n.x, n.y);
        }
        example.cost_ = -example.node_.get(example.x_.size() - 1).get(example.result_.get(example.x_.size() - 1)).bestCost;
    }

    public void buildLattice() {
        if (!x_.isEmpty()) {
            feature_index_.rebuildFeatures(this);
            for (int i = 0; i < x_.size(); i++) {
                for (int j = 0; j < ysize_; j++) {
                    feature_index_.calcCost(node_.get(i).get(j));
                    List<Path> lpath = node_.get(i).get(j).lpath;
                    for (Path p : lpath) {
                        feature_index_.calcCost(p);
                    }
                }
            }

            // Add penalty for Dual decomposition.
            if (!penalty_.isEmpty()) {
                for (int i = 0; i < x_.size(); i++) {
                    for (int j = 0; j < ysize_; j++) {
                        node_.get(i).get(j).cost += penalty_.get(i).get(j);
                    }
                }
            }
        }
    }

    public void buildLattice(Example example) {
        if (!example.x_.isEmpty()) {
            feature_index_.rebuildFeatures(example, this);
            for (int i = 0; i < example.x_.size(); i++) {
                for (int j = 0; j < ysize_; j++) {
                    feature_index_.calcCost(example.node_.get(i).get(j));
                    List<Path> lpath = example.node_.get(i).get(j).lpath;
                    for (Path p : lpath) {
                        feature_index_.calcCost(p);
                    }
                }
            }

            // Add penalty for Dual decomposition.
            if (!example.penalty_.isEmpty()) {
                for (int i = 0; i < example.x_.size(); i++) {
                    for (int j = 0; j < ysize_; j++) {
                        example.node_.get(i).get(j).cost += example.penalty_.get(i).get(j);
                    }
                }
            }
        }
    }

    public boolean initNbest() {
        if (agenda_ == null) {
            agenda_ = new PriorityQueue<QueueElement>(10, new Comparator<QueueElement>() {
                public int compare(QueueElement o1, QueueElement o2) {
                    return (int) (o1.fx - o2.fx);
                }
            });
        }
        agenda_.clear();
        int k = x_.size() - 1;
        for (int i = 0; i < ysize_; i++) {
            QueueElement eos = new QueueElement();
            eos.node = node_.get(k).get(i);
            eos.fx = -node_.get(k).get(i).bestCost;
            eos.gx = -node_.get(k).get(i).cost;
            eos.next = null;
            agenda_.add(eos);
        }
        return true;
    }

    public boolean initNbest(Example example) {
        if (example.agenda_ == null) {
            example.agenda_ = new PriorityQueue<QueueElement>(10, new Comparator<QueueElement>() {
                public int compare(QueueElement o1, QueueElement o2) {
                    return (int) (o1.fx - o2.fx);
                }
            });
        }
        example.agenda_.clear();
        int k = example.x_.size() - 1;
        for (int i = 0; i < ysize_; i++) {
            QueueElement eos = new QueueElement();
            eos.node = example.node_.get(k).get(i);
            eos.fx = -example.node_.get(k).get(i).bestCost;
            eos.gx = -example.node_.get(k).get(i).cost;
            eos.next = null;
            example.agenda_.add(eos);
        }
        return true;
    }

    public Node node(int i, int j) {
        return node_.get(i).get(j);
    }

    public Node node(Example example, int i, int j) {
        return example.node_.get(i).get(j);
    }

    public void set_node(Node n, int i, int j) {
        node_.get(i).set(j, n);
    }

    public void set_node(Example example, Node n, int i, int j) {
        example.node_.get(i).set(j, n);
    }

    public int eval() {
        int err = 0;
        for (int i = 0; i < x_.size(); i++) {
            if (!answer_.get(i).equals(result_.get(i))) {
                err++;
            }
        }
        return err;
    }

    public int eval(Example example) {
        int err = 0;
        for (int i = 0; i < example.x_.size(); i++) {
            if (!example.answer_.get(i).equals(example.result_.get(i))) {
                err++;
            }
        }
        return err;
    }

    public double gradient(double[] expected) {
        if (x_.isEmpty()) {
            return 0.0;
        }
        buildLattice();
        forwardbackward();
        double s = 0.0;

        for (int i = 0; i < x_.size(); i++) {
            for (int j = 0; j < ysize_; j++) {
                node_.get(i).get(j).calcExpectation(expected, Z_, ysize_);
            }
        }
        for (int i = 0; i < x_.size(); i++) {
            List<Integer> fvector = node_.get(i).get(answer_.get(i)).fVector;
            for (int j = 0; fvector.get(j) != -1; j++) {
                int idx = fvector.get(j) + answer_.get(i);
                expected[idx]--;
            }
            s += node_.get(i).get(answer_.get(i)).cost; //UNIGRAM COST
            List<Path> lpath = node_.get(i).get(answer_.get(i)).lpath;
            for (Path p : lpath) {
                if (p.lnode.y == answer_.get(p.lnode.x)) {
                    for (int k = 0; p.fvector.get(k) != -1; k++) {
                        int idx = p.fvector.get(k) + p.lnode.y * ysize_ + p.rnode.y;
                        expected[idx]--;
                    }
                    s += p.cost;  // BIGRAM COST
                    break;
                }
            }
        }

        viterbi();
        return Z_ - s;
    }

    public double collins(List<Double> collins) {
        if (x_.isEmpty()) {
            return 0.0;
        }
        buildLattice();
        viterbi();  // call for finding argmax y
        double s = 0.0;

        int num = 0;
        for (int i = 0; i < x_.size(); i++) {
            if (answer_.get(i).equals(result_.get(i))) {
                num++;
            }
        }
        if (num == x_.size()) {
            // if correct parse, do not run forward + backward
            return 0.0;
        }

        for (int i = 0; i < x_.size(); i++) {
            // answer
            s += node_.get(i).get(answer_.get(i)).cost;
            List<Integer> fvector = node_.get(i).get(answer_.get(i)).fVector;
            for (int k = 0; fvector.get(k) != -1; k++) {
                int idx = fvector.get(k) + answer_.get(i);
                collins.set(idx, collins.get(idx) + 1);
            }
            List<Path> lpath = node_.get(i).get(answer_.get(i)).lpath;
            for (Path p : lpath) {
                if (p.lnode.y == answer_.get(p.lnode.x)) {
                    for (int j = 0; p.fvector.get(j) != -1; j++) {
                        int idx = p.fvector.get(j) + p.lnode.y * ysize_ + p.rnode.y;
                        collins.set(idx, collins.get(i) + 1);
                    }
                    s += p.cost;
                    break;
                }
            }

            // result
            s -= node_.get(i).get(result_.get(i)).cost;
            List<Integer> fvectorR = node_.get(i).get(result_.get(i)).fVector;
            for (int k = 0; fvectorR.get(k) != -1; k++) {
                int idx = fvector.get(k) + result_.get(i);
                collins.set(idx, collins.get(idx) - 1);
            }
            List<Path> lpathR = node_.get(i).get(result_.get(i)).lpath;
            for (Path p : lpathR) {
                if (p.lnode.y == result_.get(p.lnode.x)) {
                    for (int j = 0; p.fvector.get(j) != -1; j++) {
                        int idx = p.fvector.get(j) + p.lnode.y * ysize_ + p.rnode.y;
                        collins.set(idx, collins.get(i) - 1);
                    }
                    s -= p.cost;
                    break;
                }
            }
        }

        return -s;
    }

    public boolean shrink() {
        if (!feature_index_.buildFeatures(this)) {
            System.err.println("build features failed");
            return false;
        }
        return true;
    }

    public ReadStatus read(BufferedReader br) {
        clear();
        ReadStatus status = ReadStatus.SUCCESS;
        try {
            String line;
            while (true) {
                if ((line = br.readLine()) == null) {
                    return ReadStatus.EOF;
                } else if (line.length() == 0) {
                    break;
                }
                if (!add(line)) {
                    System.err.println("fail to add line: " + line);
                    return ReadStatus.ERROR;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error reading stream");
            return ReadStatus.ERROR;
        }
        return status;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (nbest_ < 1) {
            if (vlevel_ >= 1) {
                sb.append("# ");
                sb.append(prob());
                sb.append("\n");
            }
            for (int i = 0; i < x_.size(); i++) {
                for (String s : x_.get(i)) {
                    sb.append(s);
                    sb.append("\t");
                }
                sb.append(yname(y(i)));
                if (vlevel_ >= 1) {
                    sb.append("/");
                    sb.append(prob(i));
                }
                if (vlevel_ >= 2) {
                    for (int j = 0; j < ysize_; j++) {
                        sb.append("\t");
                        sb.append(yname(j));
                        sb.append("/");
                        sb.append(prob(i, j));
                    }
                }
                sb.append("\n");
            }
            sb.append("\n");
        } else {
            for (int n = 0; n < nbest_; n++) {
                if (!next()) {
                    break;
                }
                sb.append("# ").append(n).append(" ").append(prob()).append("\n");
                for (int i = 0; i < x_.size(); ++i) {
                    for (String s : x_.get(i)) {
                        sb.append(s).append('\t');
                    }
                    sb.append(yname(y(i)));
                    if (vlevel_ >= 1) {
                        sb.append('/').append(prob(i));
                    }
                    if (vlevel_ >= 2) {
                        for (int j = 0; j < ysize_; ++j) {
                            sb.append('\t').append(yname(j)).append('/').append(prob(i, j));
                        }
                    }
                    sb.append('\n');
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public boolean open(FeatureIndex featureIndex) {
        mode_ = Mode.LEARN;
        feature_index_ = featureIndex;
        ysize_ = feature_index_.ysize();
        return true;
    }

    public boolean open(String filename) {
        return true;
    }

    public boolean setModel(ModelImpl model) {
        mode_ = Mode.TEST;
        feature_index_ = model.getFeatureIndex_();
        nbest_ = model.getNbest_();
        vlevel_ = model.getVlevel_();
        ysize_ = feature_index_.ysize();
        return true;
    }

    public void close() {
    }

    public boolean add(String line) {
        int xsize = feature_index_.getXsize_();
        String[] cols = line.split("[\t ]", -1);
        int size = cols.length;
        if ((mode_ == Mode.LEARN && cols.length < xsize + 1) ||
                (mode_ == Mode.TEST && cols.length < xsize)) {
            System.err.println("# x is small: size=" + cols.length + " xsize=" + xsize);
            return false;
        }
        List<String> tmpX = Arrays.asList(cols);
        x_.add(tmpX);
        result_.add(0);
        int tmpAnswer = 0;
        if (mode_ == Mode.LEARN) {
            int r = ysize_;
            for (int i = 0; i < ysize_; i++) {
                if (cols[xsize].equals(yname(i))) {
                    r = i;
                }
            }
            if (r == ysize_) {
                System.err.println("cannot find answer");
                return false;
            }
            tmpAnswer = r;
        }
        answer_.add(tmpAnswer);
        List<Node> l = Arrays.asList(new Node[ysize_]);
        node_.add(l);
        return true;
    }

    public boolean add(Example example, String line) {
        int xsize = feature_index_.getXsize_();
        String[] cols = line.split("[\t ]", -1);
        int size = cols.length;
        if ((mode_ == Mode.LEARN && cols.length < xsize + 1) ||
                (mode_ == Mode.TEST && cols.length < xsize)) {
            System.err.println("# x is small: size=" + cols.length + " xsize=" + xsize);
            return false;
        }
        List<String> tmpX = Arrays.asList(cols);
        example.x_.add(tmpX);
        example.result_.add(0);
        int tmpAnswer = 0;
        if (mode_ == Mode.LEARN) {
            int r = ysize_;
            for (int i = 0; i < ysize_; i++) {
                if (cols[xsize].equals(yname(i))) {
                    r = i;
                }
            }
            if (r == ysize_) {
                System.err.println("cannot find answer");
                return false;
            }
            tmpAnswer = r;
        }
        example.answer_.add(tmpAnswer);
        List<Node> l = Arrays.asList(new Node[ysize_]);
        example.node_.add(l);
        return true;
    }

    public List<List<Integer>> getFeatureCache_() {
        return featureCache_;
    }

    public List<List<Integer>> getFeatureCache_(Example example) {
        return example.featureCache_;
    }

    public void setFeatureCache_(List<List<Integer>> featureCache_) {
        this.featureCache_ = featureCache_;
    }

    public int size() {
        return x_.size();
    }

    public int size(Example example) {
        return example.x_.size();
    }

    public int xsize() {
        return feature_index_.getXsize_();
    }

    public int dsize() {
        return feature_index_.size();
    }

    public float[] weightVector() {
        return feature_index_.getAlphaFloat_();
    }

    public boolean empty() {
        return x_.isEmpty();
    }

    public boolean empty(Example example) {
        return example.x_.isEmpty();
    }

    public double prob() {
        return Math.exp(-cost_ - Z_);
    }

    public double prob(Example example) {
        return Math.exp(-example.cost_ - example.Z_);
    }

    public double prob(int i, int j) {
        return toProb(node_.get(i).get(j), Z_);
    }

    public double prob(Example example, int i, int j) {
        return toProb(example.node_.get(i).get(j), example.Z_);
    }

    public double prob(int i) {
        return toProb(node_.get(i).get(result_.get(i)), Z_);
    }

    public double prob(Example example, int i) {
        return toProb(example.node_.get(i).get(example.result_.get(i)), example.Z_);
    }

    public double alpha(int i, int j) {
        return node_.get(i).get(j).alpha;
    }

    public double alpha(Example example, int i, int j) {
        return example.node_.get(i).get(j).alpha;
    }

    public double beta(int i, int j) {
        return node_.get(i).get(j).beta;
    }

    public double beta(Example example, int i, int j) {
        return example.node_.get(i).get(j).beta;
    }

    public double emissionCost(int i, int j) {
        return node_.get(i).get(j).cost;
    }

    public double emissionCost(Example example, int i, int j) {
        return example.node_.get(i).get(j).cost;
    }

    public double nextTransitionCost(int i, int j, int k) {
        return node_.get(i).get(j).rpath.get(k).cost;
    }

    public double nextTransitionCost(Example example, int i, int j, int k) {
        return example.node_.get(i).get(j).rpath.get(k).cost;
    }

    public double prevTransitionCost(int i, int j, int k) {
        return node_.get(i).get(j).lpath.get(k).cost;
    }

    public double prevTransitionCost(Example example, int i, int j, int k) {
        return example.node_.get(i).get(j).lpath.get(k).cost;
    }

    public double bestCost(int i, int j) {
        return node_.get(i).get(j).bestCost;
    }

    public double bestCost(Example example, int i, int j) {
        return example.node_.get(i).get(j).bestCost;
    }

    public List<Integer> emissionVector(int i, int j) {
        return node_.get(i).get(j).fVector;
    }

    public List<Integer> emissionVector(Example example, int i, int j) {
        return example.node_.get(i).get(j).fVector;
    }

    public List<Integer> nextTransitionVector(int i, int j, int k) {
        return node_.get(i).get(j).rpath.get(k).fvector;
    }

    public List<Integer> nextTransitionVector(Example example, int i, int j, int k) {
        return example.node_.get(i).get(j).rpath.get(k).fvector;
    }

    public List<Integer> prevTransitionVector(int i, int j, int k) {
        return node_.get(i).get(j).lpath.get(k).fvector;
    }

    public List<Integer> prevTransitionVector(Example example, int i, int j, int k) {
        return example.node_.get(i).get(j).lpath.get(k).fvector;
    }

    public int answer(int i) {
        return answer_.get(i);
    }

    public int answer(Example example, int i) {
        return example.answer_.get(i);
    }

    public int result(int i) {
        return result_.get(i);
    }

    public int result(Example example, int i) {
        return example.result_.get(i);
    }

    public int y(int i) {
        return result_.get(i);
    }

    public int y(Example example, int i) {
        return example.result_.get(i);
    }

    public String yname(int i) {
        return feature_index_.getY_().get(i);
    }

    public String y2(int i) {
        return yname(result_.get(i));
    }

    public String y2(Example example, int i) {
        return yname(example.result_.get(i));
    }

    public String x(int i, int j) {
        return x_.get(i).get(j);
    }

    public String x(Example example, int i, int j) {
        return example.x_.get(i).get(j);
    }

    public List<String> x(int i) {
        return x_.get(i);
    }

    public List<String> x(Example example, int i) {
        return example.x_.get(i);
    }

    public String parse(String s) {
        return "";
    }

    public String parse(String s, int i) {
        return "";
    }

    public String parse(String s, int i, String s2, int j) {
        return "";
    }

    public boolean parse() {
        if (!feature_index_.buildFeatures(this)) {
            System.err.println("fail to build featureIndex");
            return false;
        }
        if (x_.isEmpty()) {
            return true;
        }
        buildLattice();
        if (nbest_ != 0 || vlevel_ >= 1) {
            forwardbackward();
        }
        viterbi();
        if (nbest_ != 0) {
            initNbest();
        }
        return true;
    }

    public boolean parse(Example example) {
        if (!feature_index_.buildFeatures(example, this)) {
            System.err.println("fail to build featureIndex");
            return false;
        }
        if (example.x_.isEmpty()) {
            return true;
        }
        buildLattice(example);
        if (nbest_ != 0 || vlevel_ >= 1) {
            forwardbackward(example);
        }
        viterbi(example);
        if (nbest_ != 0) {
            initNbest(example);
        }
        return true;
    }


    public boolean clear() {
        if (mode_ == Mode.TEST) {
            feature_index_.clear();
        }
        lastError = null;
        x_.clear();
        node_.clear();
        answer_.clear();
        result_.clear();
        featureCache_.clear();
        Z_ = cost_ = 0.0;
        return true;
    }

    public boolean next() {
        while (!agenda_.isEmpty()) {
            QueueElement top = agenda_.peek();
            Node rnode = top.node;
            agenda_.remove(top);
            if (rnode.x == 0) {
                for (QueueElement n = top; n != null; n = n.next) {
                    result_.set(n.node.x, n.node.y);
                }
                cost_ = top.gx;
                return true;
            }
            for (Path p : rnode.lpath) {
                QueueElement n = new QueueElement();
                n.node = p.lnode;
                n.gx = -p.lnode.cost - p.cost + top.gx;
                n.fx = -p.lnode.bestCost - p.cost + top.gx;
                n.next = top;
                agenda_.add(n);
            }
        }
        return false;
    }


    public float costFactor() {
        return (float) feature_index_.getCostFactor_();
    }

    void setCostFactor(float cost_factor) {
        if (cost_factor > 0)
            feature_index_.setCostFactor_(cost_factor);
    }

    void setNbest(int nbest) {
        nbest_ = nbest;
    }

    private static double toProb(Node n, double Z) {
        return Math.exp(n.alpha + n.beta - n.cost - Z);
    }

    public boolean open(FeatureIndex featureIndex, int nbest, int vlevel) {
        return open(featureIndex, nbest, vlevel, 1.0);
    }

    public boolean open(FeatureIndex featureIndex, int nbest, int vlevel, double costFactor) {
        if (costFactor <= 0.0) {
            System.err.println("cost factor must be positive");
            return false;
        }
        nbest_ = nbest;
        vlevel_ = vlevel;
        feature_index_ = featureIndex;
        feature_index_.setCostFactor_(costFactor);
        ysize_ = feature_index_.ysize();
        return true;
    }

    public boolean open(InputStream stream, int nbest, int vlevel, double costFactor) {
        if (costFactor <= 0.0) {
            System.err.println("cost factor must be positive");
            return false;
        }
        feature_index_ = new DecoderFeatureIndex();
        if (!feature_index_.open(stream)) {
            System.err.println("Failed to open model file ");
            return false;
        }
        nbest_ = nbest;
        vlevel_ = vlevel;
        feature_index_.setCostFactor_(costFactor);
        ysize_ = feature_index_.ysize();
        return true;
    }

    public Mode getMode_() {
        return mode_;
    }

    public void setMode_(Mode mode_) {
        this.mode_ = mode_;
    }

    public int getVlevel_() {
        return vlevel_;
    }

    public void setVlevel_(int vlevel_) {
        this.vlevel_ = vlevel_;
    }

    public int getNbest_() {
        return nbest_;
    }

    public void setNbest_(int nbest_) {
        this.nbest_ = nbest_;
    }

    public int getYsize_() {
        return ysize_;
    }

    public void setYsize_(int ysize_) {
        this.ysize_ = ysize_;
    }

    public double getCost_() {
        return cost_;
    }

    public void setCost_(double cost_) {
        this.cost_ = cost_;
    }

    public double getZ_() {
        return Z_;
    }

    public void setZ_(double z_) {
        Z_ = z_;
    }

    public int getFeature_id_() {
        return feature_id_;
    }

    public int getFeature_id_(Example example) {
        return example.feature_id_;
    }

    public void setFeature_id_(int feature_id_) {
        this.feature_id_ = feature_id_;
    }

    public void setFeature_id_(Example example, int feature_id_) {
        example.feature_id_ = feature_id_;
    }

    public int getThread_id_() {
        return thread_id_;
    }

    public void setThread_id_(int thread_id_) {
        this.thread_id_ = thread_id_;
    }

    public FeatureIndex getFeature_index_() {
        return feature_index_;
    }

    public void setFeature_index_(FeatureIndex feature_index_) {
        this.feature_index_ = feature_index_;
    }

    public List<List<String>> getX_() {
        return x_;
    }

    public void setX_(List<List<String>> x_) {
        this.x_ = x_;
    }

    public List<List<Node>> getNode_() {
        return node_;
    }

    public void setNode_(List<List<Node>> node_) {
        this.node_ = node_;
    }

    public List<Integer> getAnswer_() {
        return answer_;
    }

    public void setAnswer_(List<Integer> answer_) {
        this.answer_ = answer_;
    }

    public List<Integer> getResult_() {
        return result_;
    }

    public void setResult_(List<Integer> result_) {
        this.result_ = result_;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            return;
        }

        TaggerImpl tagger = new TaggerImpl(Mode.TEST);
        File f = new File(args[0]);
        FileInputStream stream = null;
        if (f.exists()) {
            stream = new FileInputStream(f);
        }
        if (stream != null && !tagger.open(stream, 2, 0, 1.0)) {
            System.err.println("open error");
            return;
        }
        System.out.println("Done reading model");

        if (args.length >= 2) {
            FileInputStream fis = new FileInputStream(args[1]);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            while (true) {
                ReadStatus status = tagger.read(br);
                if (ReadStatus.ERROR == status) {
                    System.err.println("read error");
                    return;
                } else if (ReadStatus.EOF == status) {
                    break;
                }
                if (tagger.getX_().isEmpty()) {
                    break;
                }
                if (!tagger.parse()) {
                    System.err.println("parse error");
                    return;
                }
                System.out.print(tagger.toString());
            }
            br.close();
        }
    }
}
