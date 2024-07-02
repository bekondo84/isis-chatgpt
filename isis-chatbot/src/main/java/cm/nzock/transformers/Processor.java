package cm.nzock.transformers;

public interface Processor {

    /**
     * Process the text to enriched it with the result of the processor
     * @param text
     * @return
     * @throws Exception
     */
    String proceed(final String text) throws Exception;
}
