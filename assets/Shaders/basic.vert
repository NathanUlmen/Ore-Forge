#version 330 core

uniform mat4 u_projView;
layout (location = 0) in vec3 a_VertexPos;
layout (location = 1) in vec3 a_normal;
layout (location = 2) in vec4 a_tangent;
layout (location = 3) in vec2 a_uv;
layout (location = 4) in mat4 a_transform;

out vec3 normal;
out vec4 tangent;
out vec2 uv;

void main() {
    gl_Position = u_projView * a_transform * vec4(a_VertexPos, 1);
    normal = a_normal;
    tangent = a_tangent;
    uv = a_uv;
}
