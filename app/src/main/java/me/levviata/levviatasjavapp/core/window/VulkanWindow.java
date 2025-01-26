package me.levviata.levviatasjavapp.core.window;

import me.levviata.levviatasjavapp.JavaApp;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVulkan;

import org.lwjgl.vulkan.VkInstance;
import org.slf4j.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class VulkanWindow {
    private long window;
    private GLFWFramebufferSizeCallback frameBufferSizeCallback;
    private final Logger logger = JavaApp.getLogger();

    // Vulkan
    private VkInstance vkInstance;
    private long surface; // Vulkan Surface

    public void run() {
        initWindow();
        loop();
        cleanup();
    }

    private void initWindow() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Check Vulkan support
        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new IllegalStateException("Vulkan not supported on this system");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API); // Do not create an OpenGL context
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(800, 600, "Vulkan Window", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Set minimum window size
        glfwSetWindowSizeLimits(window, 400, 300, GLFW_DONT_CARE, GLFW_DONT_CARE); // Minimum 400x300, no maximum limit

        // Set up frame buffer size callback
        frameBufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                String format = String.format("Window resized to: %dx%d%n", width, height);
                logger.debug(format);
            }
        };
        glfwSetFramebufferSizeCallback(window, frameBufferSizeCallback);
    }

    private void loop() {
        // Main loop
        while (!glfwWindowShouldClose(window)) {
            /*
            // Wait for events or timeout after 16 ms (~60 FPS)
            // Hybrid method balancing responsiveness with resource efficiency, could be used for something else.
            glfwWaitEventsTimeout(0.016);
            */

            // Wait for events
            glfwWaitEvents();
        }
    }

    private void cleanup() {
        if (frameBufferSizeCallback != null) {
            frameBufferSizeCallback.free(); // Free the callback
            frameBufferSizeCallback = null; // Dereference to allow garbage collection
        }
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        new VulkanWindow().run();
    }
}

