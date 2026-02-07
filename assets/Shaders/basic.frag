#version 330 core

in vec3 normal;
in vec4 tangent;
in vec2 uv;

out vec4 fragColor;

void main() {
    vec3 n = normalize(normal);
    vec3 normalColor = n * 0.5 + 0.5;

    vec3 t = normalize(tangent.xyz);
    float handed = tangent.w;

    // float-safe "xor" checker
    float a = step(0.5, fract(uv.x * 8.0));
    float b = step(0.5, fract(uv.y * 8.0));
    float checker = abs(a - b);          // 0 or 1

    float tInfluence = 0.08 * handed;
    float uvInfluence = mix(-0.03, 0.03, checker);

    vec3 combined = normalColor + t * tInfluence + vec3(uvInfluence);

    fragColor = vec4(clamp(combined, 0.0, 1.0), 1.0);
}



