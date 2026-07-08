package com.netease.yuanqi.common.pojo.ods.binlog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netease.yuanqi.common.utils.Preconditions;
import java.util.Map;

/** Fork from com.netease.wm.hubble.common.BinlogRow. */
public class BinlogRow {
    private final String table;
    private final Integer op;
    private final Long opTime;
    private final Long seqno;
    private final Integer partitionId;
    private final Map<String, Object> data;
    private final Map<String, Object> old;
    private final String _tbl;
    private final Integer _bin_op;
    private final Long _bin_op_time;
    private final Long _bin_op_seqno;
    private final Map<String, Object> _bin_old;

    @JsonCreator
    public BinlogRow(
            @JsonProperty("table") String table,
            @JsonProperty("op") Integer op,
            @JsonProperty("opTime") Long opTime,
            @JsonProperty("seqno") Long seqno,
            @JsonProperty("partitionId") Integer partitionId,
            @JsonProperty("data") Map<String, Object> data,
            @JsonProperty("old") Map<String, Object> old,
            @JsonProperty("_tbl") String _tbl,
            @JsonProperty("_bin_op") Integer _bin_op,
            @JsonProperty("_bin_op_time") Long _bin_op_time,
            @JsonProperty("_bin_op_seqno") Long _bin_op_seqno,
            @JsonProperty("_bin_old") Map<String, Object> _bin_old) {
        this.table = Preconditions.checkNotNull(table);
        this.op = Preconditions.checkNotNull(op);
        this.opTime = Preconditions.checkNotNull(opTime);
        this.seqno = Preconditions.checkNotNull(seqno);
        this.partitionId = Preconditions.checkNotNull(partitionId);
        this.data = Preconditions.checkNotNull(data);
        this.old = old;
        this._tbl = Preconditions.checkNotNull(_tbl);
        this._bin_op = Preconditions.checkNotNull(_bin_op);
        this._bin_op_time = Preconditions.checkNotNull(_bin_op_time);
        this._bin_op_seqno = Preconditions.checkNotNull(_bin_op_seqno);
        this._bin_old = _bin_old;
    }

    public String getTable() {
        return table;
    }

    public Integer getOp() {
        return op;
    }

    public Long getOpTime() {
        return opTime;
    }

    public Long getSeqno() {
        return seqno;
    }

    public Integer getPartitionId() {
        return partitionId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, Object> getOld() {
        return old;
    }

    public String get_tbl() {
        return _tbl;
    }

    public Integer get_bin_op() {
        return _bin_op;
    }

    public Long get_bin_op_time() {
        return _bin_op_time;
    }

    public Long get_bin_op_seqno() {
        return _bin_op_seqno;
    }

    public Map<String, Object> get_bin_old() {
        return _bin_old;
    }

    public static BinlogRowBuilder builder() {
        return new BinlogRowBuilder();
    }

    public static class BinlogRowBuilder {
        private String table;
        private Integer op;
        private Long opTime;
        private Long seqno;
        private Integer partitionId;
        private Map<String, Object> data;
        private Map<String, Object> old;
        private String _tbl;
        private Integer _bin_op;
        private Long _bin_op_time;
        private Long _bin_op_seqno;
        private Map<String, Object> _bin_old;

        public BinlogRowBuilder() {}

        public BinlogRowBuilder setTable(String table) {
            this.table = table;
            this._tbl = table;
            return this;
        }

        public BinlogRowBuilder setOp(Integer op) {
            this.op = op;
            this._bin_op = op;
            return this;
        }

        public BinlogRowBuilder setOpTime(Long opTime) {
            this.opTime = opTime;
            this._bin_op_time = opTime;
            return this;
        }

        public BinlogRowBuilder setSeqno(Long seqno) {
            this.seqno = seqno;
            this._bin_op_seqno = seqno;
            return this;
        }

        public BinlogRowBuilder setPartitionId(Integer partitionId) {
            this.partitionId = partitionId;
            return this;
        }

        public BinlogRowBuilder setData(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public BinlogRowBuilder setOld(Map<String, Object> old) {
            this.old = old;
            this._bin_old = old;
            return this;
        }

        public BinlogRow build() {
            // Add built-in parameters of binlog.
            return new BinlogRow(
                    table,
                    op,
                    opTime,
                    seqno,
                    partitionId,
                    data,
                    old,
                    _tbl,
                    _bin_op,
                    _bin_op_time,
                    _bin_op_seqno,
                    _bin_old);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"table\":\""
                + table
                + '\"'
                + ",\"op\":"
                + op
                + ",\"opTime\":"
                + opTime
                + ",\"seqno\":"
                + seqno
                + ",\"partitionId\":"
                + partitionId
                + ",\"data\":"
                + data
                + ",\"old\":"
                + old
                + ",\"_tbl\":\""
                + _tbl
                + '\"'
                + ",\"_bin_op\":"
                + _bin_op
                + ",\"_bin_op_time\":"
                + _bin_op_time
                + ",\"_bin_op_seqno\":"
                + _bin_op_seqno
                + ",\"_bin_old\":"
                + _bin_old
                + "}";
    }
}
