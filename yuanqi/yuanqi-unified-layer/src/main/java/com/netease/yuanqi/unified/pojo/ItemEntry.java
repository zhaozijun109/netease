package com.netease.yuanqi.unified.pojo;

public class ItemEntry {
    private final String itemId;
    private final String itemType;
    private final String entryType;

    public ItemEntry(String itemId, String itemType, String entryType) {
        this.itemId = itemId;
        this.itemType = itemType;
        this.entryType = entryType;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public String getEntryType() {
        return entryType;
    }

    public static ItemEntryBuilder builder() {
        return new ItemEntryBuilder();
    }

    public static class ItemEntryBuilder {
        private String itemId;
        private String itemType;
        private String entryType;

        public ItemEntryBuilder() {}

        public ItemEntryBuilder setItemId(String itemId) {
            this.itemId = itemId;
            return this;
        }

        public ItemEntryBuilder setItemType(String itemType) {
            this.itemType = itemType;
            return this;
        }

        public ItemEntryBuilder setEntryType(String entryType) {
            this.entryType = entryType;
            return this;
        }

        public ItemEntry build() {
            return new ItemEntry(itemId, itemType, entryType);
        }
    }

    @Override
    public String toString() {
        return "{"
                + "\"itemId\":\""
                + itemId
                + '\"'
                + ",\"itemType\":\""
                + itemType
                + '\"'
                + ",\"entryType\":\""
                + entryType
                + '\"'
                + "}";
    }
}
