#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform UniformBufferObject {
    mat4 model;
    mat4 view;
    mat4 proj;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec3 inColor;

layout(location = 0) out vec3 fragColor;

vec2 positions[3] = vec2[](
    vec2(0.0, -0.5),
    vec2(0.5, 0.5),
    vec2(-0.5, 0.5)
);

void main() {
	gl_PointSize = 140.0;
    vec4 pos = ubo.proj * ubo.view * ubo.model * vec4(inPosition.xy, 0.0, 1.0);
    vec4 pos2 = ubo.proj * ubo.view * ubo.model * vec4(inColor, 1.0);
    gl_Position = pos;
    fragColor = inColor;
}