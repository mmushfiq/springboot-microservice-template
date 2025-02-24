package com.company.project.template.common;

import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

public final class TestUtil {

    private TestUtil() {
    }

    public static String json(String fileName) throws IOException {
        return FileUtils.readFileToString(ResourceUtils.getFile("classpath:__Files/" + fileName), defaultCharset());
    }

}
