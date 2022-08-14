package dev.woc.elgame

import org.joml.Vector2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL

class Window(size: Vector2i, title: String) {
    var vsyncInterval: Int = 1
        set(value) {
            glfwSwapInterval(value)
            field = value
        }

    val window: Long

    init {
        glfwSwapInterval(1)
        glfwInit()
        glfwWindowHint(GLFW_RESIZABLE, 1)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6)

        window = glfwCreateWindow(size.x, size.y, title, 0L, 0L)
        glfwMakeContextCurrent(window)
        GL.createCapabilities()
    }

    fun isOpen(): Boolean {
        return !glfwWindowShouldClose(window)
    }

    fun swap() {
        glfwSwapBuffers(window)
    }

    fun whileOpen(func: Window.(Double) -> Unit) {
        var thisFrame = glfwGetTime()
        var lastFrame = thisFrame - (1f / 60f)
        var dt = thisFrame - lastFrame

        while (isOpen()) {
            pollEvents()

            func(this, dt)

            swap()
            lastFrame = thisFrame
            thisFrame = glfwGetTime()
            dt = thisFrame - lastFrame
        }
    }

    companion object {
        fun terminate() {
            glfwTerminate()
        }

        fun pollEvents() {
            glfwPollEvents()
        }
    }
}