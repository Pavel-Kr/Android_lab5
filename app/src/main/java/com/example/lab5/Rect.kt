package com.example.lab5

import android.opengl.GLES20
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Rect(x: Float, y: Float, width: Float, height: Float, private val shaderProgram: Int, private val textureId: Int) {

    val coords = floatArrayOf(
        x, y,
        x + width, y,
        x + width, y + height,
        x, y + height
    )

    val texCoords = floatArrayOf(
        0f, 0f,
        1f, 0f,
        1f, 1f,
        0f, 1f
    )

    private val coordsStride = 2 * Float.SIZE_BYTES
    private val textureStride = 2 * Float.SIZE_BYTES

    private val color = floatArrayOf(
        1.0f, 0.0f, 1.0f, 1.0f
    )

    private var positionHandle: Int = 0
    private var textureHandle: Int = 0
    private var colorHandle: Int = 0
    private var textureUnitLocation: Int = 0
    private var timeHandle: Int = 0

    private var drawOrder = shortArrayOf(0, 1, 2, 2, 3, 0)

    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(coords.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(coords)
                position(0)
            }
        }

    private val textureBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(texCoords.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(texCoords)
                position(0)
            }
        }

    private val indexBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(drawOrder.size * Short.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }

    init {
        Matrix.setIdentityM(modelMatrix, 0)

        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
        textureHandle = GLES20.glGetAttribLocation(shaderProgram, "aTexCoord")
        colorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor")
        textureUnitLocation = GLES20.glGetUniformLocation(shaderProgram, "texSampler")
        timeHandle = GLES20.glGetUniformLocation(shaderProgram, "sysTime")
    }

    fun draw(vpMatrix: FloatArray){
        GLES20.glUseProgram(shaderProgram)

        GLES20.glVertexAttribPointer(
            positionHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            coordsStride,
            vertexBuffer
        )

        GLES20.glVertexAttribPointer(
            textureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            textureStride,
            textureBuffer
        )

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(textureHandle)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glUniform1i(textureUnitLocation, 0)

        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        val time = (SystemClock.uptimeMillis() / 50) % 1000
        println("Time: " + time)
        GLES20.glUniform1f(timeHandle, time.toFloat())

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
        }

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }
}