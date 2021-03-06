package com.marklogic.appdeployer.pkg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;

/**
 * Not a great use for an abstract class, just making this to avoid duplication for now.
 */
public abstract class AbstractPackageMerger {

    private TransformerFactory transformerFactory;

    protected String loadStringFromClasspath(String path) {
        path = ClassUtils.addResourcePathToPackagePath(getClass(), path);
        try {
            return new String(FileCopyUtils.copyToByteArray(new ClassPathResource(path).getInputStream()));
        } catch (IOException ie) {
            throw new RuntimeException("Unable to load string from classpath resource at: " + path + "; cause: "
                    + ie.getMessage(), ie);
        }
    }

    public String mergePackages(String initialPackageXml, Source stylesheetSource, List<String> mergePackageFilePaths) {
        try {
            if (transformerFactory == null) {
                transformerFactory = TransformerFactory.newInstance();
            }

            Transformer t = transformerFactory.newTransformer(stylesheetSource);

            String xml = initialPackageXml;
            for (String path : mergePackageFilePaths) {
                t.setParameter("mergePackageFilePath", path);
                StringWriter sw = new StringWriter();
                ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
                t.transform(new StreamSource(bais), new StreamResult(sw));
                xml = sw.toString();
            }

            return xml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
