package org.jetbrains.jps

public interface IJavaSdkProvider {
    IJavaSdk findByName(String name);
    IJavaSdk findByPath(String path);
}