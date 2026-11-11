package com.grafkom;

import static org.lwjgl.opengl.GL20.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertPath, String fragPath) throws Exception {
        int vertShader = createShader(vertPath, GL_VERTEX_SHADER);
        int fragShader = createShader(fragPath, GL_FRAGMENT_SHADER);

        programId = glCreateProgram();
        glAttachShader(programId, vertShader);
        glAttachShader(programId, fragShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        glDetachShader(programId, vertShader);
        glDetachShader(programId, fragShader);
        glDeleteShader(vertShader);
        glDeleteShader(fragShader);
    }

    private int createShader(String resourcePath, int shaderType) throws Exception {
        String shaderCode;
        try (InputStream in = getClass().getResourceAsStream(resourcePath);
             Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
            shaderCode = scanner.useDelimiter("\\A").next();
        }

        int shaderId = glCreateShader(shaderType);
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader " + resourcePath + ": " + glGetShaderInfoLog(shaderId, 1024));
        }

        return shaderId;
    }

    public void bind() { glUseProgram(programId); }
    public void unbind() { glUseProgram(0); }
    public int getProgramId() { return programId; }
}