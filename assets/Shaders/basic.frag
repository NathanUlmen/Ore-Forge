#version 330 core

uniform sampler2D u_sampler;
in vec3 normal;
in vec4 tangent;
in vec2 uv;


out vec4 fragColor;

void main() {
    fragColor = texture2D(u_sampler, uv);
}



