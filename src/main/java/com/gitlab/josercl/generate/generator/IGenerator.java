package com.gitlab.josercl.generate.generator;

import java.io.IOException;

public interface IGenerator  {
    void generate(String entityName, String basePackage) throws IOException;
}
