package com.yelloco.payment.data.tagstore;

/**
 * A write only interface for writing tags.
 */
public interface TagWriter {

    /**
     * Sets tag in tag store using a byte[].
     *
     * @param tagId - EMV tag id
     * @param value - tag value
     */
    void setTag(String tagId, byte[] value);

    /**
     * Sets tag in tag store using hex String.
     *
     * @param tagId - EMV tag id
     * @param value - tag value
     */
    void setTag(String tagId, String value);
}