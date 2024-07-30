package cm.nzock.preprocessor;

import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;

import java.util.regex.Pattern;

public class StringPrePreprocessor implements TokenPreProcess {

    private static final Pattern punctPattern = Pattern.compile("[\\.:,\"'\\(\\)\\[\\]|/?!;]+");
    @Override
    public String preProcess(String s) {
        return punctPattern.matcher(s).replaceAll(StringUtils.SPACE);
    }
}
