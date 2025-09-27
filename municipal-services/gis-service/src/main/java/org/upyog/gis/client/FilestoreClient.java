package org.upyog.gis.client;

import org.springframework.web.multipart.MultipartFile;

/**
 * Client interface for filestore operations
 */
public interface FilestoreClient {

    /**
     * Upload a file to filestore
     *
     * @param file the file to upload
     * @param tenantId the tenant ID
     * @param module the module name
     * @param tag optional tag for the file
     * @return filestore response containing fileStoreId
     * @throws Exception if upload fails
     */
    String uploadFile(MultipartFile file, String tenantId, String module, String tag) throws Exception;

}

