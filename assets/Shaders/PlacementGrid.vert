attribute vec3 a_position;
attribute vec3 a_normal;

uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;

varying vec3 v_worldPos;

void main() {
    vec4 world = u_worldTrans * vec4(a_position, 1.0);
    v_worldPos = world.xyz;

    gl_Position = u_projTrans * world;
}
