package cm.nzock.facades;

import cm.nzock.beans.ChatLabelData;

import java.util.Map;

public interface ChatFacade {

    ChatLabelData converse(final Long session, final String uuid, final String text) ;

    Map<String, String> getGeneralSettings() ;

    String generateUuid();
}
