package dev.woc.elgame

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL41
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.io.path.Path
import kotlin.io.path.readText

object KatEngine {
    fun clearColor(color: Vector4f) {
        clearColor(color.x, color.y, color.z, color.w)
    }

    fun clearColor(r: Float, g: Float, b: Float, a: Float) {
        glClearColor(r, g, b, a)
    }

    fun clear(color: Vector4f) {
        clearColor(color)
        clear()
    }

    fun clear(r: Float, g: Float, b: Float, a: Float) {
        clearColor(r, g, b, a)
        clear()
    }

    fun clear() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
    }

    fun drawArrays(vao: VertexArray, drawMode: DrawMode, count: Int, first: Int = 0) {
        vao.bind()
        glDrawArrays(drawMode.i, first, count)
        VertexArray.unbind()
    }

    fun drawArrays(vao: VertexArray, count: Int, first: Int = 0) {
        drawArrays(vao, DrawMode.Triangles, count, first)
    }
}

enum class DrawMode(val i: Int) {
    Points(GL_POINTS),
    Lines(GL_LINES),
    LinesAdjacency(GL_LINES_ADJACENCY),
    LineStrip(GL_LINE_STRIP),
    LineStripAdjacency(GL_LINE_STRIP_ADJACENCY),
    LineLoop(GL_LINE_LOOP),
    Triangles(GL_TRIANGLES),
    TrianglesAdjacency(GL_TRIANGLES_ADJACENCY),
    TriangleStrip(GL_TRIANGLE_STRIP),
    TriangleStripAdjacency(GL_TRIANGLE_STRIP_ADJACENCY),
    TriangleFan(GL_TRIANGLE_FAN),
    Patches(GL_PATCHES)
}

enum class BufferUsage(val i: Int) {
    StaticDraw(GL_STATIC_DRAW),
    StaticRead(GL_STATIC_READ),
    StaticCopy(GL_STATIC_COPY),
    DynamicDraw(GL_DYNAMIC_DRAW),
    DynamicRead(GL_DYNAMIC_READ),
    DynamicCopy(GL_DYNAMIC_COPY),
    StreamDraw(GL_STREAM_DRAW),
    StreamRead(GL_STREAM_READ),
    StreamCopy(GL_STREAM_COPY)
}

class VertexBuffer() {
    val buffer: Int = glCreateBuffers()

    init {
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    constructor(values: List<Float>, usage: BufferUsage = BufferUsage.StaticDraw) : this() {
        memoryStack {
            glNamedBufferData(buffer, values.toBuffer(it), usage.i)
        }
    }

    fun bind() {
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
    }

    fun dispose() {
        glDeleteBuffers(buffer)
    }
}

class VertexArray() {
    val array: Int = glCreateVertexArrays()
    private var nextAttrib = 0
    private var nextBinding = 0

    init {
        glBindVertexArray(array)
        glBindVertexArray(0)
    }

    fun dispose() {
        glDeleteVertexArrays(array)
    }

    fun vertexBuffer(buf: VertexBuffer, attribs: List<Int>): VertexArray {
        var stride = 0
        for (i in attribs) {
            glVertexArrayAttribFormat(array, nextAttrib, i, GL_FLOAT, false, stride)
            glVertexArrayAttribBinding(array, nextAttrib, nextBinding)
            glEnableVertexArrayAttrib(array, nextAttrib++)
            stride += i * Float.SIZE_BYTES
        }

        glVertexArrayVertexBuffer(array, nextBinding++, buf.buffer, 0, stride)
        return this
    }

    fun bind() {
        glBindVertexArray(array)
    }

    companion object {
        fun unbind() {
            glBindVertexArray(0)
        }
    }
}

class ShaderModule(source: String, val type: ShaderModule.Type) {
    fun dispose() {
        glDeleteShader(module)
    }

    enum class Type(val i: Int) {
        Geometry(GL_GEOMETRY_SHADER),
        Vertex(GL_VERTEX_SHADER),
        TessControl(GL_TESS_CONTROL_SHADER),
        TessEvaluation(GL_TESS_EVALUATION_SHADER),
        Fragment(GL_FRAGMENT_SHADER),

        Compute(GL_COMPUTE_SHADER)
    }

    val module = glCreateShader(type.i)

    init {
        glShaderSource(module, source)
        glCompileShader(module)

        if (glGetShaderi(module, GL_COMPILE_STATUS) != GL_TRUE) {
            println(glGetShaderInfoLog(module))
            error("Failed to compile shader module")
        }
    }

    companion object {
        fun loadExternal(path: String, type: Type): ShaderModule {
            return ShaderModule(Path(path).readText(), type)
        }

        fun loadInternal(path: String, type: Type): ShaderModule {
            return ShaderModule(ShaderModule::class.java.classLoader.getResource(path)?.readText()?:error("Unable to find shader `$path` in classpath"), type)
        }
    }
}

class Shader(modules: List<ShaderModule>) {
    val shader = glCreateProgram()

    init {
        modules.forEach {
            glAttachShader(shader, it.module)
        }

        glLinkProgram(shader)

        if (glGetProgrami(shader, GL_LINK_STATUS) != GL_TRUE) {
            println(glGetProgramInfoLog(shader))
            error("Failed to link shader")
        }
    }

    fun use() {
        glUseProgram(shader)
    }

    fun uniform1f(name: String, x: Float) {
        glProgramUniform1f(shader, glGetUniformLocation(shader, name), x)
    }

    fun uniform2f(name: String, x: Float, y: Float) {
        glProgramUniform2f(shader, glGetUniformLocation(shader, name), x, y)
    }

    fun uniform3f(name: String, x: Float, y: Float, z: Float) {
        glProgramUniform3f(shader, glGetUniformLocation(shader, name), x, y, z)
    }

    fun uniform4f(name: String, x: Float, y: Float, z: Float, w: Float) {
        glProgramUniform4f(shader, glGetUniformLocation(shader, name), x, y, z, w)
    }

    fun uniform2f(name: String, v: Vector2f) {
        glProgramUniform2f(shader, glGetUniformLocation(shader, name), v.x, v.y)
    }

    fun uniform3f(name: String, v: Vector3f) {
        glProgramUniform3f(shader, glGetUniformLocation(shader, name), v.x, v.y, v.z)
    }

    fun uniform4f(name: String, v: Vector4f) {
        glProgramUniform4f(shader, glGetUniformLocation(shader, name), v.x, v.y, v.z, v.w)
    }

    private val matInterm = BufferUtils.createFloatBuffer(16)

    fun uniformMatrix4f(name: String, m: Matrix4f) {
        m.get(matInterm.rewind())
        glProgramUniformMatrix4fv(shader, glGetUniformLocation(shader, name), false, matInterm.rewind())
    }

    class Builder {
        data class Def(val path: String, val type: ShaderModule.Type, val internal: Boolean = true)

        private val defs = mutableListOf<Def>()

        fun internal(path: String, type: ShaderModule.Type): Builder {
            defs.add(Def(path, type, true))
            return this
        }

        fun external(path: String, type: ShaderModule.Type): Builder {
            defs.add(Def(path, type, false))
            return this
        }

        fun build(): Shader {
            val mods = mutableListOf<ShaderModule>()
            for (def in defs) {
                if (def.internal) {
                    mods.add(ShaderModule.loadInternal(def.path, def.type))
                } else {
                    mods.add(ShaderModule.loadExternal(def.path, def.type))
                }
            }
            val s = Shader(mods)
            mods.forEach(ShaderModule::dispose)
            return s
        }
    }

    fun dispose() {
        glDeleteProgram(shader)
    }
}

sealed interface Colors {
    companion object {
        val RED = Vector4f(1f, 0f, 0f, 1f)
        val GREEN = Vector4f(0f, 1f, 0f, 1f)
        val BLUE = Vector4f(0f, 0f, 1f, 1f)
        val WHITE = Vector4f(1f, 1f, 1f, 1f)
        val BLACK = Vector4f(0f, 0f, 0f, 1f)
        val YELLOW = Vector4f(1f, 1f, 0f, 1f)
        val CYAN = Vector4f(0f, 1f, 1f, 1f)
        val MAGENTA = Vector4f(1f, 0f, 1f, 1f)
    }
}

fun <T> memoryStack(f: (MemoryStack) -> T): T {
    MemoryStack.stackPush().use {
        return f(it)
    }
}

fun List<Float>.toBuffer(stack: MemoryStack): FloatBuffer {
    val b = stack.mallocFloat(this.size)
    b.put(0, toFloatArray())
    return b.rewind()
}

fun List<Int>.toBuffer(stack: MemoryStack): IntBuffer {
    val b = stack.mallocInt(this.size)
    b.put(0, toIntArray())
    return b.rewind()
}

fun List<Float>.toBuffer(): FloatBuffer {
    val b = BufferUtils.createFloatBuffer(this.size)
    b.put(0, toFloatArray())
    return b.rewind()
}

fun List<Int>.toBuffer(): IntBuffer {
    val b = BufferUtils.createIntBuffer(this.size)
    b.put(0, toIntArray())
    return b.rewind()
}
