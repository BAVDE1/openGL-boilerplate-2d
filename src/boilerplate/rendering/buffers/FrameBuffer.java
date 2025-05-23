package boilerplate.rendering.buffers;

import boilerplate.rendering.Texture;
import boilerplate.utility.Logging;
import org.lwjgl.opengl.GL45;

import java.awt.*;
import java.util.ArrayList;

/**
 * Frame Buffer is a section of the GPUs memory that holds buffers (colour buffer, depth buffer, stencil buffer).
 * <p>
 * A default frame buffer is created by GLFW.
 * Custom Frame Buffers can be used for, say, mirrors or post-processing and the like.
 * <p>
 * Render buffer object are write only, cannot be read (cannot be sampled), which makes them fast.
 * If you're not sampling the values in your shader, use render buffer attachment, otherwise use a texture attachment.
 */
public class FrameBuffer {
    public static class RenderBuffer {
        private Integer id;
        public Integer attachment;

        public void createBuffer(Dimension size, int format, int attachment) {
            this.attachment = attachment;
            bind();
            GL45.glRenderbufferStorage(GL45.GL_RENDERBUFFER, format, size.width, size.height);
        }

        public void bind() {
            GL45.glBindRenderbuffer(GL45.GL_RENDERBUFFER, id);
        }

        public static void unbind() {
            GL45.glBindRenderbuffer(GL45.GL_RENDERBUFFER, 0);
        }

        public void genId() {
            if (id != null) {
                Logging.warn("Attempting to re-generate already generated render buffer id, aborting");
                return;
            }
            id = GL45.glGenRenderbuffers();
        }

        public int getId() {
            return id;
        }
    }

    public static int defaultColourBuffFormat = GL45.GL_RGBA;

    private static int boundFrameBuffer = 0;
    protected Integer bufferId;

    public ArrayList<Texture> colourBuffers = new ArrayList<>();
    public Texture depthBuffer;
    public Texture stencilBuffer;
    public Texture depthStencilBuffer;
    public RenderBuffer renderBuffer;

    public FrameBuffer() {

    }

    public FrameBuffer(boolean genId) {
        if (genId) genId();
    }

    public void attachColourBuffer(Texture colourBuff) {
        if (boundFrameBuffer != bufferId) bind();
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_COLOR_ATTACHMENT0 + colourBuffers.size(), GL45.GL_TEXTURE_2D, colourBuff.getId(), 0);
        colourBuffers.add(colourBuff);
    }

    public void attachDepthBuffer(Texture depthBuff) {
        if (boundFrameBuffer != bufferId) bind();
        this.depthBuffer = depthBuff;
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_DEPTH_ATTACHMENT, GL45.GL_TEXTURE_2D, depthBuff.getId(), 0);
    }

    public void attachStencilBuffer(Texture stencilBuff) {
        if (boundFrameBuffer != bufferId) bind();
        this.stencilBuffer = stencilBuff;
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_STENCIL_ATTACHMENT, GL45.GL_TEXTURE_2D, stencilBuff.getId(), 0);
    }

    public void attachDepthStencilBuffer(Texture depthStencilBuff) {
        if (boundFrameBuffer != bufferId) bind();
        this.depthStencilBuffer = depthStencilBuff;
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_DEPTH_STENCIL_ATTACHMENT, GL45.GL_TEXTURE_2D, depthStencilBuff.getId(), 0);
    }

    public void attachRenderBuffer(RenderBuffer renderBuff) {
        if (boundFrameBuffer != bufferId) bind();
        this.renderBuffer = renderBuff;
        GL45.glFramebufferRenderbuffer(GL45.GL_FRAMEBUFFER, renderBuff.attachment, GL45.GL_RENDERBUFFER, renderBuff.getId());
    }

    public static Texture setupDefaultColourBuffer(Dimension size) {
        Texture t = setupTextureBuffer(size, defaultColourBuffFormat);
        t.useDefaultInterpolation();
        return t;
    }

    public static Texture setupDefaultDepthBuffer(Dimension size) {
        return setupTextureBuffer(size, GL45.GL_DEPTH_COMPONENT);
    }

    public static Texture setupDefaultStencilBuffer(Dimension size) {
        return setupTextureBuffer(size, GL45.GL_STENCIL_INDEX);
    }

    public static Texture setupDefaultDepthStencilBuffer(Dimension size) {
        return setupTextureBuffer(size, GL45.GL_DEPTH24_STENCIL8, GL45.GL_DEPTH_STENCIL, GL45.GL_UNSIGNED_INT_24_8);
    }

    public static Texture setupTextureBuffer(Dimension size, int format) {
        return setupTextureBuffer(size, format, format, GL45.GL_UNSIGNED_BYTE);
    }

    public static Texture setupTextureBuffer(Dimension size, int storedFormat, int givenFormat, int textureType) {
        Texture buff = new Texture(size, true);
        buff.textureType = textureType;
        buff.bind();
        buff.createTexture(storedFormat, givenFormat, null);
        return buff;
    }

    public static RenderBuffer setupDefaultRenderBuffer(Dimension size) {
        RenderBuffer rb = new RenderBuffer();
        rb.genId();
        rb.createBuffer(size, GL45.GL_DEPTH24_STENCIL8, GL45.GL_DEPTH_STENCIL_ATTACHMENT);
        return rb;
    }

    public void genId() {
        if (bufferId != null) {
            Logging.warn("Attempting to re-generate already generated frame buffer id, aborting");
            return;
        }
        bufferId = GL45.glGenFramebuffers();
    }

    public int getId() {
        return bufferId;
    }

    public void bind() {
        if (boundFrameBuffer == bufferId) return;
        boundFrameBuffer = bufferId;
        GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, bufferId);
    }

    /**
     * 0 reverts to use the default frame buffer, set by the windowing system (GLFW)
     */
    public static void unbind() {
        if (boundFrameBuffer == 0) return;
        boundFrameBuffer = 0;
        GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, 0);
    }

    public void delete() {
        if (boundFrameBuffer == bufferId) unbind();
        GL45.glDeleteFramebuffers(bufferId);
    }

    public boolean isCompletelyBuilt() {
        if (boundFrameBuffer != bufferId) bind();
        return GL45.glCheckFramebufferStatus(GL45.GL_FRAMEBUFFER) == GL45.GL_FRAMEBUFFER_COMPLETE;
    }

    public void checkCompletionOrError() {
        if (!isCompletelyBuilt()) Logging.warn("The frame buffer is not complete.");
    }
}
