uniform float u_time;
uniform vec3 u_centerPosition;
uniform vec3 u_velocity;
attribute float a_lifetime;
attribute vec3 a_startPosition;
attribute vec3 a_endPosition;
varying float v_lifetime;
varying float v_velocity;
void main()
{
    //gl_Position.y = a_endPosition.y;
    gl_Position.xyz = a_endPosition;
    gl_Position.w = 1.0;
    gl_PointSize = 30;
    v_lifetime = a_lifetime;
}