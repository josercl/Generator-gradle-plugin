package com.gitlab.josercl.generator.base;

import java.io.IOException;

public interface IGenerator  {
    void generate(String entityName, String basePackage) throws IOException;
}
