package cm.nzock.services;

import cm.platform.basecommerce.core.chat.CellMemoryModel;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;

public interface CellMemoryService {

    /**
     * Save the value in the id cell
     * @param id
     * @param value
     */
    void save(String id, String value) ;

    /**
     * Forget mean delete entry with ID in the cell memory table
     * @param id
     */
    void forget(String id) ;

    /**
     * Read the content of theentry with id
     * @param id
     * @return
     */
    CellMemoryModel read(String id) ;
}
