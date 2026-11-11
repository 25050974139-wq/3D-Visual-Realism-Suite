package com.grafkom;

import org.lwjgl.BufferUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture2D {
    private int textureID;

    public Texture2D(String resourcePath) {
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Setting Wrapping & Filtering OpenGL
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        try {
            // Coba muat dari Classpath Resource
            InputStream in = getClass().getResourceAsStream(resourcePath);
            
            // Fallback otomatis jika nama ekstensi berbeda (wood.jpg vs wood.png)
            if (in == null) {
                in = getClass().getResourceAsStream("/textures/wood.png");
            }
            if (in == null) {
                in = getClass().getResourceAsStream("/textures/wood.jpg");
            }

            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                int width = img.getWidth();
                int height = img.getHeight();

                int[] pixels = new int[width * height];
                img.getRGB(0, 0, width, height, pixels, 0, width);

                ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

                // Read pixels & Flip Y-axis (OpenGL coordinate space)
                for (int y = height - 1; y >= 0; y--) {
                    for (int x = 0; x < width; x++) {
                        int pixel = pixels[y * width + x];
                        buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                        buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                        buffer.put((byte) (pixel & 0xFF));         // B
                        buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
                    }
                }
                buffer.flip();

                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
                glGenerateMipmap(GL_TEXTURE_2D);
                System.out.println("Tekstur " + resourcePath + " BERHASIL dimuat!");
            } else {
                System.err.println("File gambar tidak ditemukan di resources/textures/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }
}