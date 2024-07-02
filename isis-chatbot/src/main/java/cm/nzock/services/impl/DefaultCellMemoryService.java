package cm.nzock.services.impl;

import cm.nzock.services.CellMemoryService;
import cm.platform.basecommerce.core.chat.CellMemoryModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DefaultCellMemoryService implements CellMemoryService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCellMemoryService.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;

    @Override
    public void save(String id, String value) {
        final CellMemoryModel cell = new CellMemoryModel();
        cell.setId(id);
        cell.setValue(value);

        final Optional dbCellContent = flexibleSearchService.find(cell);

        dbCellContent.ifPresent(cellmemory -> cell.setValue(StringUtils.joinWith(" ", ((CellMemoryModel)cellmemory).getValue(), value)));

        //Save the cellmemory content
        try {
            modelService.save(cell);
        } catch (ModelServiceException e) {
            LOG.error(String.format("Enable to update or create cell memory entry for user %s : ", id), e);
        }
    }

    @Override
    public void forget(String id)  {
          Optional cellmemorycontent = flexibleSearchService.find(id, "id", CellMemoryModel._TYPECODE);

          if (cellmemorycontent.isPresent()) {
              try {
                  modelService.delete(cellmemorycontent.get());
              } catch (ModelServiceException e) {
                  LOG.error(String.format("Enable to disable cell memory for user %s : ", id), e);
              }
          }
    }

    @Override
    public CellMemoryModel read(String id) {
        return (CellMemoryModel) flexibleSearchService.find(id, "id", CellMemoryModel._TYPECODE).orElse(null);
    }


}
