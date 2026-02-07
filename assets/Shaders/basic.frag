#version 330 core

in vec3 normal;
in vec4 tangent;
in vec2 uv;
out vec4 fragColor;

void main() {
    vec3 n = normalize(normal);

    // Use tangent + uv in a tiny, stable way so they’re considered “live”
    vec3 t = normalize(tangent.xyz);

    float u = uv.x * 0.001;
    float v = uv.y * 0.001;

    // Slightly perturb n using t and a uv-derived scalar (very small effect)
    vec3 finalN = normalize(n + t * (u - v));

    fragColor = vec4(finalN * 0.5 + 0.5, 1.0);
}

