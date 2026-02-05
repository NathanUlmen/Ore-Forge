#version 330 core

//in vec2 v_uv;
//in vec4 v_tint;

//uniform sampler2D u_texture;

in vec3 normal;
out vec4 fragColor;

void main() {
//    vec4 base = texture(u_texture, v_uv);
   fragColor = vec4(normal.x, 0, 1, 1);
}
