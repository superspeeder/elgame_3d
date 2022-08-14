@file:JvmName("ElGame")

package dev.woc.elgame

import org.joml.Vector2i

fun main(args: Array<String>) {
    println("Hello!")

    val window = Window(Vector2i(800, 800), "Not Minecraft")
    window.vsyncInterval = 0

    val vbo = VertexBuffer(listOf(
        0f,0f,0f,
        1f,0f,0f,
        1f,1f,0f,
        0f,1f,0f
    ))
    val vao = VertexArray().vertexBuffer(vbo, listOf(3))

    val shader = Shader.Builder()
        .internal("shaders/main/frag.glsl", ShaderModule.Type.Fragment)
        .internal("shaders/main/vert.glsl", ShaderModule.Type.Vertex)
        .build()

    window.whileOpen { dt ->
        println("FPS: ${1f / dt}")

        KatEngine.clear(Colors.CYAN)

        shader.use()
        shader.uniform4f("uColor", Colors.MAGENTA)
        KatEngine.drawArrays(vao, DrawMode.TriangleFan, 4)

    }
}