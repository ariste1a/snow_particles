uniform mat4 u_MVPMatrix; // A constant representing the combined model/view/projection matrix.
attribute vec4 a_Position;
attribute vec4 a_Color;
varying vec4 v_Color;
void main()
{
    v_Color = a_Color;
     gl_Position = u_MVPMatrix;

}

