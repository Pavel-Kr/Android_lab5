package com.example.lab5

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates

class MyGLRenderer(private val context: Context): GLSurfaceView.Renderer {
    private val vpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewPos = floatArrayOf(0f, 0f, 10f, 1f)

    private var dnoTexture by Delegates.notNull<Int>()

    private val shaderHandler = ShaderHandler()
    private val textureHandler = TextureHandler()

    private var waterShader by Delegates.notNull<Int>()

    private lateinit var waterFrame: Rect

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        waterShader = shaderHandler.loadShaders(context, R.raw.vertex, R.raw.fragment)
        dnoTexture = textureHandler.loadTexture(context, R.drawable.dno)
        waterFrame = Rect(-3f, -3f, 6f, 6f, waterShader, dnoTexture)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
        Matrix.setLookAtM(viewMatrix, 0, viewPos[0], viewPos[1], viewPos[2], 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        waterFrame.draw(vpMatrix)
    }
}