package com.gitlab.josercl.init.exception;

public class InitException extends RuntimeException {
    public InitException() {
        super("Usage: gradle initProject -PbasePackage=xxx.yyy.zzz -PprojectName=this_is_the_project_name");
    }
}
