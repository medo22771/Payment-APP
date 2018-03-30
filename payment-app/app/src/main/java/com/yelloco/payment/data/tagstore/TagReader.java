package com.yelloco.payment.data.tagstore;

import java.util.Map;

/**
 * A read only interface for retrieving tags.
 */
public interface TagReader {

    /**
     * Gets tag from tag store as a byte[]. All data from card are read after select
     * application
     *
     * @param tagId The String id of the tag to get
     *
     * @return tag value as a byte[]
     */
    byte[] getTag(String tagId);

    /**
     * Gets tag from tag store. All data from card are read after select
     * application
     *
     * @param tagIds The tag ids to fetch values for
     * @return Tag index to tag value map
     */
    Map<String, byte[]> getTags(String[] tagIds);

    /**
     * Get all the tags in this store.
     *
     * @return A map of the tag key value pairs.
     */
    Map<String, byte[]> getAllTags();

    /**
     * @return True if there is at least one tag.
     */
    boolean hasTags();

    Map<String, byte[]> getTags();
}