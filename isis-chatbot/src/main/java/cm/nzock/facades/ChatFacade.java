package cm.nzock.facades;

import cm.nzock.beans.ChatData;

import java.util.Map;

public interface ChatFacade {

    ChatData converse(final Long session, final String uuid, final String text) ;

    Map<String, String> getGeneralSettings() ;

    String generateUuid();
}
