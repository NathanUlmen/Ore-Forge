#ifdef GL_ES
precision mediump float;
#endif

varying vec3 v_worldPos;

void main() {
    float thickness = 0.03;    // line thickness
    vec3 gridColor = vec3(0.2, 0.4, 1.0);  // blue grid
    vec3 baseColor = vec3(0.0);           // background (black)

    // Get the fractional part of world coords
    vec2 f = abs(fract(v_worldPos.xz) - 0.5);

    // Distance to nearest grid line
    float line = min(f.x, f.y);

    // If close to the grid line, draw blue; else background
    float grid = step(line, thickness);

    vec3 color = mix(baseColor, gridColor, grid);

    gl_FragColor = vec4(color, 1.0);
}
