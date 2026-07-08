package com.netease.yuanqi.common.pojo.archive;

import com.netease.yuanqi.common.utils.Preconditions;

public class ArchiveFormatRow {
    private final String archiveDir;
    private final Long archiveTime;
    private final String data;

    public ArchiveFormatRow(String archiveDir, Long archiveTime, String data) {
        this.archiveDir = archiveDir;
        this.archiveTime = archiveTime;
        this.data = Preconditions.checkNotNull(data);
    }

    public String getArchiveDir() {
        return archiveDir;
    }

    public Long getArchiveTime() {
        return archiveTime;
    }

    public String getData() {
        return data;
    }

    public static ArchiveFormatRowBuilder builder() {
        return new ArchiveFormatRowBuilder();
    }

    public static class ArchiveFormatRowBuilder {
        private String archiveDir;
        private Long archiveTime;
        private String data;

        public ArchiveFormatRowBuilder() {}

        public ArchiveFormatRowBuilder setArchiveDir(String archiveDir) {
            this.archiveDir = archiveDir;
            return this;
        }

        public ArchiveFormatRowBuilder setArchiveTime(Long archiveTime) {
            this.archiveTime = archiveTime;
            return this;
        }

        public ArchiveFormatRowBuilder setData(String data) {
            this.data = data;
            return this;
        }

        public ArchiveFormatRow build() {
            return new ArchiveFormatRow(archiveDir, archiveTime, data);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"archiveDir\":\""
                + archiveDir
                + '\"'
                + ",\"archiveTime\":"
                + archiveTime
                + ",\"data\":"
                + data
                + "}";
    }
}
