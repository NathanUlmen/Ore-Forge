#version 330 core

uniform mat4 u_projView;
layout (location = 0) in vec3 a_VertexPos;
layout (location = 1) in vec3 a_normal;
layout (location = 2) in mat4 a_transform;

out vec3 normal;

void main() {
    gl_Position = u_projView * a_transform * vec4(a_VertexPos, 1);
    normal = a_normal;
}
