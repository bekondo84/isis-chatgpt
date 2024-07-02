package cm.nzock.facades;

import java.util.Map;

public interface ChatFacade {

    String converse(final Long session, final String text) ;

    Map<String, String> getGeneralSettings() ;
}
