package com.grafkom;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class MainApp {
    private long window;
    private int width = 800, height = 600;

    private ShaderProgram shaderProgram;
    private Texture2D woodTexture;

    private int cubeVaoId, cubeVboId;
    private int floorVaoId, floorVboId;

    // Toggle Fitur Interaktif (B, C, Z)
    private boolean useBlinnPhong = true;
    private boolean enableDepthTest = true;
    private boolean enableCulling = true;

    // Debounce tombol
    private boolean keyBState = false;
    private boolean keyCState = false;
    private boolean keyZState = false;

    private float rotationAngle = 0.0f;

    public void run() {
        init();
        loop();

        glDeleteVertexArrays(cubeVaoId);
        glDeleteBuffers(cubeVboId);
        glDeleteVertexArrays(floorVaoId);
        glDeleteBuffers(floorVboId);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("Gagal inisialisasi GLFW");

        window = glfwCreateWindow(width, height, "3D Visual Realism Suite - Minggu 11", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Gagal membuat GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        // Inisialisasi HSR & Culling awal
        HSRManager.setupDepthTest(enableDepthTest);
        HSRManager.setupBackFaceCulling(enableCulling);

        // Load Shaders
        try {
            shaderProgram = new ShaderProgram("/shaders/phong.vert", "/shaders/phong.frag");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load Tekstur Kayu
        try {
            woodTexture = new Texture2D("/textures/wood.png");
        } catch (Exception e) {
            System.err.println("Gagal memuat tekstur kayu: " + e.getMessage());
        }

        // Setup Geometri 3D (Kubus & Lantai)
        setupCube();
        setupFloor();
    }

    private void setupCube() {
        // Data Geometri Kubus (Posisi, Normal, UV Coord 0.0 - 1.0 per Sisi)
        float[] vertices = {
            // Depan
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f,
             0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 0.0f,
             0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f,
             0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f,
            // Belakang
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
             0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
             0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
             0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
            // Atas
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
             0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
             0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
             0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
            // Bawah
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
             0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
             0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
             0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
            // Kanan
             0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
             0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
             0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
             0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
             0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
             0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
            // Kiri
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
            -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
            -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f
        };

        cubeVaoId = glGenVertexArrays();
        cubeVboId = glGenBuffers();

        glBindVertexArray(cubeVaoId);
        glBindBuffer(GL_ARRAY_BUFFER, cubeVboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int stride = 8 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void setupFloor() {
        // Tiling UV dikurangi dari 4.0 ke 2.0 agar serat kayu pada lantai berukuran pas & tajam
        float[] floorVertices = {
             4.0f, -0.5f,  4.0f,   0.0f, 1.0f, 0.0f,    2.0f, 0.0f,
            -4.0f, -0.5f,  4.0f,   0.0f, 1.0f, 0.0f,    0.0f, 0.0f,
            -4.0f, -0.5f, -4.0f,   0.0f, 1.0f, 0.0f,    0.0f, 2.0f,

             4.0f, -0.5f,  4.0f,   0.0f, 1.0f, 0.0f,    2.0f, 0.0f,
            -4.0f, -0.5f, -4.0f,   0.0f, 1.0f, 0.0f,    0.0f, 2.0f,
             4.0f, -0.5f, -4.0f,   0.0f, 1.0f, 0.0f,    2.0f, 2.0f
        };

        floorVaoId = glGenVertexArrays();
        floorVboId = glGenBuffers();

        glBindVertexArray(floorVaoId);
        glBindBuffer(GL_ARRAY_BUFFER, floorVboId);
        glBufferData(GL_ARRAY_BUFFER, floorVertices, GL_STATIC_DRAW);

        int stride = 8 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void handleInputs() {
        // Tombol B: Toggle Blinn-Phong Shading vs Phong Standard
        if (glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS) {
            if (!keyBState) {
                useBlinnPhong = !useBlinnPhong;
                System.out.println("Blinn-Phong Mode: " + (useBlinnPhong ? "ON" : "OFF (Phong)"));
                keyBState = true;
            }
        } else keyBState = false;

        // Tombol C: Toggle Back-Face Culling
        if (glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS) {
            if (!keyCState) {
                enableCulling = !enableCulling;
                HSRManager.setupBackFaceCulling(enableCulling);
                System.out.println("Back-Face Culling: " + (enableCulling ? "ON" : "OFF"));
                keyCState = true;
            }
        } else keyCState = false;

        // Tombol Z: Toggle Z-Buffer Depth Test
        if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS) {
            if (!keyZState) {
                enableDepthTest = !enableDepthTest;
                HSRManager.setupDepthTest(enableDepthTest);
                System.out.println("Z-Buffer Depth Test: " + (enableDepthTest ? "ON" : "OFF"));
                keyZState = true;
            }
        } else keyZState = false;
    }

    private void loop() {
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);

        Matrix4f model = new Matrix4f();
        Matrix4f view = new Matrix4f().lookAt(new Vector3f(0.0f, 2.0f, 4.5f), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0f), (float) width / height, 0.1f, 100.0f);

        while (!glfwWindowShouldClose(window)) {
            handleInputs();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            rotationAngle += 0.01f;

            shaderProgram.bind();

            if (woodTexture != null) {
                woodTexture.bind(0);
            }

            int pId = shaderProgram.getProgramId();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                glUniformMatrix4fv(glGetUniformLocation(pId, "view"), false, view.get(fb));
                glUniformMatrix4fv(glGetUniformLocation(pId, "projection"), false, projection.get(fb));

                glUniform3f(glGetUniformLocation(pId, "lightPos"), 2.0f, 3.0f, 2.0f);
                glUniform3f(glGetUniformLocation(pId, "viewPos"), 0.0f, 2.0f, 4.5f);
                glUniform3f(glGetUniformLocation(pId, "lightColor"), 1.0f, 1.0f, 1.0f);
                glUniform1i(glGetUniformLocation(pId, "useBlinnPhong"), useBlinnPhong ? 1 : 0);

                // 1. Render Lantai
                model.identity();
                glUniformMatrix4fv(glGetUniformLocation(pId, "model"), false, model.get(fb));
                glUniform3f(glGetUniformLocation(pId, "objectColor"), 1.0f, 1.0f, 1.0f);

                glBindVertexArray(floorVaoId);
                glDrawArrays(GL_TRIANGLES, 0, 6);

                // 2. Render Kubus Berputar
                model.identity().translate(0.0f, 0.3f, 0.0f).rotate(rotationAngle, new Vector3f(0.5f, 1.0f, 0.0f));
                glUniformMatrix4fv(glGetUniformLocation(pId, "model"), false, model.get(fb));
                glUniform3f(glGetUniformLocation(pId, "objectColor"), 1.0f, 1.0f, 1.0f);

                glBindVertexArray(cubeVaoId);
                glDrawArrays(GL_TRIANGLES, 0, 36);
                glBindVertexArray(0);
            }

            shaderProgram.unbind();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new MainApp().run();
    }
}