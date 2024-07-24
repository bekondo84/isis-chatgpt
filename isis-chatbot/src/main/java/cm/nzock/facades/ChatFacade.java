package cm.nzock.facades;

import cm.nzock.beans.ChatData;
import cm.nzock.beans.DomainData;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;

import java.util.List;
import java.util.Map;

public interface ChatFacade {

    ChatData converse(final Long session, final String uuid, final Long domain, final String text) ;

    Map<String, String> getGeneralSettings() ;

    String generateUuid() throws ModelServiceException;

    void userReview(final Long pk, final Boolean value) throws ModelServiceException;

    String getTitle() ;

    List<DomainData> getDomains();

    DomainData defaultDomain() ;

}
