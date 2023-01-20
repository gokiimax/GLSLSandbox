package dev.max.glslsandbox;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;

public class GLSLSandboxShader {

    private final int programId;
    private final int timeUniform;
    private final int resolutionUniform;
    private final int mouseUniform;

    public GLSLSandboxShader(String fragmentShaderLocation) throws IOException {
        int program = glCreateProgram();

        glAttachShader(program, createShader(GLSLSandboxShader.class.getResourceAsStream("/passthrough.vsh"), GL_VERTEX_SHADER));
        glAttachShader(program, createShader(GLSLSandboxShader.class.getResourceAsStream(fragmentShaderLocation), GL_FRAGMENT_SHADER));

        glLinkProgram(program);
        int linked = glGetProgrami(program, GL_LINK_STATUS);

        // if linking failed
        if(linked == 0) {
            System.err.println(glGetProgramInfoLog(program));

            throw new IllegalStateException("Shader failed to link");
        }

        this.programId = program;
        
        // Setup uniforms
        glUseProgram(program);
        
        this.timeUniform = glGetUniformLocation(program, "time");
        this.mouseUniform = glGetUniformLocation(program, "mouse");
        this.resolutionUniform = glGetUniformLocation(program, "resolution");

        glUseProgram(0);

    }

    public void useShader(int width, int height, float mouseX, float mouseY, float time) {
        glUseProgram(this.programId);

        glUniform2f(this.resolutionUniform, width, height);
        glUniform2f(this.mouseUniform, mouseX / width, 1.0f - mouseY / height);
        glUniform1f(this.timeUniform, time);

    }

    private int createShader(InputStream inputStream, int shaderType) throws IOException {
        int shader = glCreateShader(shaderType);

        ByteBuffer shaderSource = readStreamToByteBuffer(inputStream);

        PointerBuffer strings = BufferUtils.createPointerBuffer(1);
        IntBuffer length = BufferUtils.createIntBuffer(1);

        strings.put(0, shaderSource);
        length.put(0, shaderSource.remaining());

        glShaderSource(shader, strings, length);

        glCompileShader(shader);

        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);

        // if compilation failed
        if(compiled == 0) {
            String shaderLog = glGetShaderInfoLog(shader);

            System.err.println(shaderLog);

            throw new IllegalStateException("Failed to compile shader");
        }
        return shader;
    }

    private ByteBuffer readStreamToByteBuffer(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buffer = new byte[512];

        int read;
        while((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }

        byte[] sourceArray = out.toByteArray();

        ByteBuffer buf = BufferUtils.createByteBuffer(sourceArray.length);

        buf.put(sourceArray);

        // Set the index of the buffer to 0
        buf.flip();

        return buf;
    }

}
