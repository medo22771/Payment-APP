package com.yelloco.payment.data.tagstore;

import com.yelloco.payment.utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation for EMV tags.
 */
public final class EmvTagStore implements TagStore {

    private final Map<String, byte[]> tags;

    public EmvTagStore() {
        tags = new LinkedHashMap<>();
    }

    @Override
    public void setTag(String tagId, byte[] value) {
        tags.put(tagId, value);
    }

    @Override
    public void setTag(String tagId, String value) {
        tags.put(tagId, Utils.hexStringToByteArray(value));
    }

    @Override
    public byte[] getTag(String tagId) {
        return tags.get(tagId);
    }

    @Override
    public Map<String, byte[]> getTags(String[] tagIds) {
        Map<String, byte[]> ret = new HashMap<>();
        if (tagIds != null) {
            for (String tag : tagIds) {
                byte[] value = this.tags.get(tag);
                if (value != null) {
                    ret.put(tag, value);
                }
            }
        }
        return ret;
    }

    @Override
    public Map<String, byte[]> getAllTags() {
        return Collections.unmodifiableMap(tags);
    }

    @Override
    public boolean hasTags() {
        return !tags.isEmpty();
    }

    @Override
    public Map<String, byte[]> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("EmvTagStore{\n");
        for (Map.Entry<String, byte[]> entry: tags.entrySet()) {
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(Utils.bytesToHex(entry.getValue()));
            builder.append("\n");
        }
        builder.append("}");
        return builder.toString();
    }
}