package cm.nzock.transformers;

import java.util.Map;

public interface Processor {

    public static final String TEXT_ENTRY="TEXT";
    public static final String ENDLABEL_ENTRY="ENDLABEL";

    /**
     * Process the text to enriched it with the result of the processor
     * @param text
     * @return
     * @throws Exception
     */
    Map proceed(final String text) throws Exception;
}
