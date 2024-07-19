#version 460
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform usampler2D tex;

layout(location = 0) in vec2 fragTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
	outColor = texture(tex, fragTexCoord) / 255.0;
}
